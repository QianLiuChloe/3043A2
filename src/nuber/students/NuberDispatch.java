package nuber.students;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * The core Dispatch class that instantiates and manages everything for Nuber
 * 
 * @author james
 *
 */
public class NuberDispatch {
    private final int MAX_DRIVERS = 999;
    private boolean logEvents = false;
    private Queue<Driver> availableDrivers;
    private HashMap<String, NuberRegion> regions;
    private int bookingsAwaitingDriver;
    private ExecutorService bookingExecutor;
	/**
	 * The maximum number of idle drivers that can be awaiting a booking 
	 */
	
	/**
	 * Creates a new dispatch objects and instantiates the required regions and any other objects required.
	 * It should be able to handle a variable number of regions based on the HashMap provided.
	 * 
	 * @param regionInfo Map of region names and the max simultaneous bookings they can handle
	 * @param logEvents Whether logEvent should print out events passed to it
	 */
	public NuberDispatch(HashMap<String, Integer> regionInfo, boolean logEvents)
	{
        this.logEvents = logEvents;
        this.availableDrivers = new LinkedList<>();
        this.regions = new HashMap<>();
        this.bookingExecutor = Executors.newCachedThreadPool();
        
        // Initialize regions based on the regionInfo map
        for (String regionName : regionInfo.keySet()) {
            regions.put(regionName, new NuberRegion(this, regionName, regionInfo.get(regionName)));
        }
	}
	
	/**
	 * Adds drivers to a queue of idle driver.
	 *  
	 * Must be able to have drivers added from multiple threads.
	 * 
	 * @param The driver to add to the queue.
	 * @return Returns true if driver was added to the queue
	 */
	public boolean addDriver(Driver newDriver)
	{
        if (availableDrivers.size() < MAX_DRIVERS) {
            availableDrivers.offer(newDriver);
            return true;
        }
        return false;
	}
	
	/**
	 * Gets a driver from the front of the queue
	 *  
	 * Must be able to have drivers added from multiple threads.
	 * 
	 * @return A driver that has been removed from the queue
	 */
	public Driver getDriver()
	{
        return availableDrivers.poll();
	}

	/**
	 * Prints out the string
	 * 	    booking + ": " + message
	 * to the standard output only if the logEvents variable passed into the constructor was true
	 * 
	 * @param booking The booking that's responsible for the event occurring
	 * @param message The message to show
	 */
	public void logEvent(Booking booking, String message) {
		
		if (!logEvents) return;
		
		System.out.println(booking + ": " + message);
		
	}

	/**
	 * Books a given passenger into a given Nuber region.
	 * 
	 * Once a passenger is booked, the getBookingsAwaitingDriver() should be returning one higher.
	 * 
	 * If the region has been asked to shutdown, the booking should be rejected, and null returned.
	 * 
	 * @param passenger The passenger to book
	 * @param region The region to book them into
	 * @return returns a Future<BookingResult> object
	 */
	public Future<BookingResult> bookPassenger(Passenger passenger, String region) {
        NuberRegion targetRegion = regions.get(region);
        if (targetRegion == null) {
            logEvent(null, "Region " + region + " not found.");
            return null;
        }

        Future<BookingResult> futureBooking = targetRegion.bookPassenger(passenger);
        if (futureBooking != null) {
            synchronized (this) {
                bookingsAwaitingDriver++;
            }
        }
        return futureBooking;
	}

	/**
	 * Gets the number of non-completed bookings that are awaiting a driver from dispatch
	 * 
	 * Once a driver is given to a booking, the value in this counter should be reduced by one
	 * 
	 * @return Number of bookings awaiting driver, across ALL regions
	 */
	public synchronized int getBookingsAwaitingDriver()
	{
	      return bookingsAwaitingDriver;
	}
	
	/**
	 * Tells all regions to finish existing bookings already allocated, and stop accepting new bookings
	 */
	public void shutdown() {
	    // 先关闭所有的区域
	    for (NuberRegion region : regions.values()) {
	        region.shutdown();
	    }
	    
	    // 记录调度器关闭的日志
	    logEvent(null, "Dispatch is shutting down.");
	}

	
	public synchronized void incrementPendingBookings() {
	    bookingsAwaitingDriver++;
	    // 打印当前状态
	   // printCurrentStatus();
	}

	public synchronized void decrementPendingBookings() {
	    if (bookingsAwaitingDriver > 0) {
	        bookingsAwaitingDriver--;
	    }
	    // 打印当前状态
	   // printCurrentStatus();
	}

}
