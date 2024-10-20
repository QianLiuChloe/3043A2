package nuber.students;

import java.util.concurrent.Callable;
import java.util.Date;

public class Booking implements Callable<BookingResult> {
    private static int nextId = 1;
    private final int jobID;
    private final NuberDispatch dispatch;
    private final Passenger passenger;
    private Driver driver;
    private long startTime;

    public Booking(NuberDispatch dispatch, Passenger passenger) {
        this.dispatch = dispatch;
        this.passenger = passenger;
        this.jobID = getNextId();
        this.startTime = new Date().getTime();
    }

    private static synchronized int getNextId() {
        return nextId++;
    }

    @Override
    public BookingResult call() throws Exception {
        // Step 1: Ask Dispatch for an available driver (blocks until a driver is available)
        driver = dispatch.getDriver();

        // Step 2: Log that a driver is starting the booking
        dispatch.logEvent(this, "Starting booking, getting driver");

        // Step 3: Driver picks up the passenger (thread pauses while this happens)
        driver.pickUpPassenger(passenger);
        dispatch.logEvent(this, "Collected passenger, on way to destination");

        // Step 4: Driver drives to the destination (thread pauses while this happens)
        driver.driveToDestination();

        // Step 5: Record the time when the passenger arrives at the destination
        long endTime = new Date().getTime();
        long tripDuration = endTime - startTime;

        // Step 6: Add the driver back to the dispatch's list of available drivers
        dispatch.addDriver(driver);
        dispatch.logEvent(this, "At destination, driver is now free");

        // Step 7: Return a BookingResult object with jobID, passenger, driver, and trip duration
        return new BookingResult(jobID, passenger, driver, tripDuration);
    }

    @Override
    public String toString() {
        return jobID + ":" + (driver != null ? driver.name : "null") + ":" + (passenger != null ? passenger.name : "null");
    }
}
