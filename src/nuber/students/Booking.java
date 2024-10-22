package nuber.students;

import java.util.Date;
import java.util.concurrent.Callable;

/**
 * 
 * Booking represents the overall "job" for a passenger getting to their destination.
 * 
 * It begins with a passenger, and when the booking is commenced by the region 
 * responsible for it, an available driver is allocated from dispatch. If no driver is 
 * available, the booking must wait until one is. When the passenger arrives at the destination,
 * a BookingResult object is provided with the overall information for the booking.
 * 
 * The Booking must track how long it takes, from the instant it is created, to when the 
 * passenger arrives at their destination. This should be done using Date class' getTime().
 * 
 * Booking's should have a globally unique, sequential ID, allocated on their creation. 
 * This should be multi-thread friendly, allowing bookings to be created from different threads.
 * 
 * @author james
 *
 */
public class Booking implements Callable<BookingResult> {
    private static int nextID = 1;
    private final int bookingID;
    private final NuberDispatch dispatch;
    private final Passenger passenger;
    private Driver driver;
    private long startTime;
		
	/**
	 * Creates a new booking for a given Nuber dispatch and passenger, noting that no
	 * driver is provided as it will depend on whether one is available when the region 
	 * can begin processing this booking.
	 * 
	 * @param dispatch
	 * @param passenger
	 */
	public Booking(NuberDispatch dispatch, Passenger passenger)
	{
        this.dispatch = dispatch;
        this.passenger = passenger;
        this.bookingID = getNextID();
        this.startTime = new Date().getTime();
	}
	
	/**
	 * At some point, the Nuber Region responsible for the booking can start it (has free spot),
	 * and calls the Booking.call() function, which:
	 * 1.	Asks Dispatch for an available driver
	 * 2.	If no driver is currently available, the booking must wait until one is available. 
	 * 3.	Once it has a driver, it must call the Driver.pickUpPassenger() function, with the 
	 * 			thread pausing whilst as function is called.
	 * 4.	It must then call the Driver.driveToDestination() function, with the thread pausing 
	 * 			whilst as function is called.
	 * 5.	Once at the destination, the time is recorded, so we know the total trip duration. 
	 * 6.	The driver, now free, is added back into Dispatch�s list of available drivers. 
	 * 7.	The call() function the returns a BookingResult object, passing in the appropriate 
	 * 			information required in the BookingResult constructor.
	 *
	 * @return A BookingResult containing the final information about the booking 
	 */
	public BookingResult call() {
	    try {
	        // Step 1: Request a driver from dispatch
	        driver = dispatch.getDriver();

	        // Step 2: If no driver is available, wait until one becomes available
	        while (driver == null) {
	            Thread.sleep(100); // Wait for a short time before retrying
	            driver = dispatch.getDriver();
	        }

	        // Step 3: Pickup the passenger
	        dispatch.logEvent(this, "Starting booking, getting driver");
	        driver.pickUpPassenger(passenger);
	        dispatch.logEvent(this, driver.name + ": Collected passenger, on way to destination");

	        // Step 4: Drive to the destination
	        driver.driveToDestination();
	        dispatch.logEvent(this, driver.name + ": At destination, driver is now free");

	        // Step 5: Record the end time and calculate trip duration
	        long endTime = new Date().getTime();
	        long tripDuration = endTime - startTime;

	        // Step 6: Add the driver back into the dispatch's available drivers
	        dispatch.addDriver(driver);

	        // Decrease the pending count in dispatch after the booking is complete
	        synchronized (dispatch) {
	            dispatch.decrementPendingBookings();
	        }

	        // Step 7: Return the BookingResult
	        return new BookingResult(bookingID, passenger, driver, tripDuration);

	    } catch (InterruptedException e) {
	        Thread.currentThread().interrupt(); // Restore the interrupted status
	        return null; // Return null on error or interruption
	    }
	}
	
	/***
	 * Should return the:
	 * - booking ID, 
	 * - followed by a colon, 
	 * - followed by the driver's name (if the driver is null, it should show the word "null")
	 * - followed by a colon, 
	 * - followed by the passenger's name (if the passenger is null, it should show the word "null")
	 * 
	 * @return The compiled string
	 */
	@Override
	public String toString()
	{
        String driverName = (driver == null) ? "null" : driver.name;
        String passengerName = (passenger == null) ? "null" : passenger.name;
        return bookingID + ":" + driverName + ":" + passengerName;
	}
	
    // Synchronized method to ensure thread-safety in generating unique booking IDs
    private synchronized static int getNextID() {
        return nextID++;
    }

}
