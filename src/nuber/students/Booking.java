package nuber.students;

import java.util.concurrent.atomic.AtomicInteger;

public class Booking {

    private static final AtomicInteger nextBookingID = new AtomicInteger(0);
    private final int bookingID;
    private final Passenger passenger;
    private final NuberDispatch dispatch;
    private Driver driver;
    private final long startTime;

    public Booking(NuberDispatch dispatch, Passenger passenger) {
        this.bookingID = nextBookingID.incrementAndGet();
        this.dispatch = dispatch;
        this.passenger = passenger;
        this.startTime = System.currentTimeMillis();
    }

    public BookingResult call() throws Exception {
        dispatch.logEvent(this, "Starting booking, getting driver");

        // Indicate that we're waiting for a driver
        dispatch.incrementBookingsAwaitingDriver();

        // Get a driver from dispatch
        driver = dispatch.getDriver();

        // We have a driver now, so we're no longer waiting
        dispatch.decrementBookingsAwaitingDriver();

        dispatch.logEvent(this, "Starting, on way to passenger");
        driver.pickUpPassenger(passenger);
        dispatch.logEvent(this, "Collected passenger, on way to destination");
        driver.driveToDestination();

        long tripDuration = System.currentTimeMillis() - startTime;

        // Return the driver to the dispatch
        dispatch.addDriver(driver);
        dispatch.logEvent(this, "At destination, driver is now free");

        return new BookingResult(bookingID, passenger, driver, tripDuration);
    }


    @Override
    public String toString() {
        String driverName = (driver != null) ? driver.name : "null";
        String passengerName = (passenger != null) ? passenger.name : "null";
        return bookingID + ":" + driverName + ":" + passengerName;
    }
}
