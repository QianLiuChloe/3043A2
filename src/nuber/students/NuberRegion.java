package nuber.students;

import java.util.concurrent.Future;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
/**
 * A single Nuber region that operates independently of other regions, other than getting 
 * drivers from bookings from the central dispatch.
 * 
 * A region has a maxSimultaneousJobs setting that defines the maximum number of bookings 
 * that can be active with a driver at any time. For passengers booked that exceed that 
 * active count, the booking is accepted, but must wait until a position is available, and 
 * a driver is available.
 * 
 * Bookings do NOT have to be completed in FIFO order.
 * 
 * @author james
 *
 */
public class NuberRegion {
    private final NuberDispatch dispatch;
    private final String regionName;
    private final int maxSimultaneousJobs;
    private final Semaphore availableSlots;
    private boolean isShuttingDown;
    private final ExecutorService bookingExecutor;
	/**
	 * Creates a new Nuber region
	 * 
	 * @param dispatch The central dispatch to use for obtaining drivers, and logging events
	 * @param regionName The regions name, unique for the dispatch instance
	 * @param maxSimultaneousJobs The maximum number of simultaneous bookings the region is allowed to process
	 */
	public NuberRegion(NuberDispatch dispatch, String regionName, int maxSimultaneousJobs)
	{
        this.dispatch = dispatch;
        this.regionName = regionName;
        this.maxSimultaneousJobs = maxSimultaneousJobs;
        this.availableSlots = new Semaphore(maxSimultaneousJobs);
        this.isShuttingDown = false;
        this.bookingExecutor = Executors.newCachedThreadPool();

	}
	
	/**
	 * Creates a booking for given passenger, and adds the booking to the 
	 * collection of jobs to process. Once the region has a position available, and a driver is available, 
	 * the booking should commence automatically. 
	 * 
	 * If the region has been told to shutdown, this function should return null, and log a message to the 
	 * console that the booking was rejected.
	 * 
	 * @param waitingPassenger
	 * @return a Future that will provide the final BookingResult object from the completed booking
	 */
	public synchronized Future<BookingResult> bookPassenger(Passenger waitingPassenger) {
	    if (isShuttingDown) {
	        dispatch.logEvent(null, "Region " + regionName + " is shutting down, booking rejected.");
	        return null;
	    }

	    try {
	        availableSlots.acquire();
	        Booking newBooking = new Booking(dispatch, waitingPassenger);
	        dispatch.logEvent(newBooking, "Creating booking");

	        Future<BookingResult> futureBooking = bookingExecutor.submit(newBooking);

	        // 成功创建预订后增加待处理预订计数
	        synchronized (dispatch) {
	            dispatch.incrementPendingBookings();
	        }

	        bookingExecutor.submit(() -> {
	            try {
	                BookingResult result = futureBooking.get();
	                dispatch.logEvent(newBooking, "Booking completed for " + result.passenger.name);
	            } catch (Exception e) {
	                dispatch.logEvent(newBooking, "Booking failed.");
	            } finally {
	                // 预订完成时减少待处理预订计数
	                synchronized (dispatch) {
	                    dispatch.decrementPendingBookings();
	                }
	                availableSlots.release();
	            }
	        });

	        return futureBooking;

	    } catch (InterruptedException e) {
	        Thread.currentThread().interrupt();
	        dispatch.logEvent(null, "Booking interrupted.");
	        return null;
	    }
	}


	
	/**
	 * Called by dispatch to tell the region to complete its existing bookings and stop accepting any new bookings
	 */
	public void shutdown() {
	    isShuttingDown = true;
	    dispatch.logEvent(null, "Region " + regionName + " is shutting down.");

	    try {
	        // 停止接收新任务
	        bookingExecutor.shutdown();
	        // 等待现有任务完成
	        if (!bookingExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
	            bookingExecutor.shutdownNow(); // 超时后强制关闭
	        }
	    } catch (InterruptedException e) {
	        bookingExecutor.shutdownNow();
	        Thread.currentThread().interrupt();
	    }
	}


		
}
