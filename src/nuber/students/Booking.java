package nuber.students;

import java.util.Date;
import java.util.concurrent.Callable;

public class Booking implements Callable<BookingResult> {

    private static int globalJobID = 1;
    private int jobID;
    private NuberDispatch dispatch;
    private Passenger passenger;
    private Driver driver;
    private long startTime;

    public Booking(NuberDispatch dispatch, Passenger passenger) {
        this.dispatch = dispatch;
        this.passenger = passenger;
        this.jobID = globalJobID++;
        this.startTime = new Date().getTime();
    }

    @Override
    public BookingResult call() throws Exception {
        driver = dispatch.getDriver();
        driver.pickUpPassenger(passenger);
        driver.driveToDestination();
        long tripDuration = new Date().getTime() - startTime;
        dispatch.addDriver(driver);  // Return the driver to dispatch
        return new BookingResult(jobID, passenger, driver, tripDuration);
    }

    @Override
    public String toString() {
        return jobID + ":" + (driver != null ? driver.name : "null") + ":" + (passenger != null ? passenger.name : "null");
    }
}
