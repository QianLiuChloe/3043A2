package nuber.students;

import java.util.HashMap;
import java.util.concurrent.Future;

public class MyDriverForTesting {

    public static void main(String[] args) throws Exception {

        // Enable the log output of the scheduler for debugging
        boolean logEvents = true;

        // create region information
        HashMap<String, Integer> testRegions = new HashMap<>();
        testRegions.put("Test Region", 10); // A Test Region supports a maximum of 10 simultaneous tasks

        // create the nuberdispatch scheduler
        NuberDispatch dispatch = new NuberDispatch(testRegions, logEvents);

        // create passengers and drivers
        Passenger passenger1 = new Passenger("Alice", 100);
        Passenger passenger2 = new Passenger("Bob", 150);

        Driver driver1 = new Driver("Barbara", 100);
        Driver driver2 = new Driver("Charlie", 120);

        // add the driver to the scheduler
        dispatch.addDriver(driver1);
        dispatch.addDriver(driver2);

        // create and test reservations
        Future<BookingResult> futureBooking1 = dispatch.bookPassenger(passenger1, "Test Region");
        Future<BookingResult> futureBooking2 = dispatch.bookPassenger(passenger2, "Test Region");

        // Wait for the booking to complete and get the results
        if (futureBooking1 != null) {
            BookingResult result1 = futureBooking1.get(); // get the results of your first booking
            System.out.println("booking1 complete: " + result1);
        }

        if (futureBooking2 != null) {
            BookingResult result2 = futureBooking2.get(); // get the result of the second reservation
            System.out.println("booking2 complete: " + result2);
        }

        // turn off the scheduler
        dispatch.shutdown();
    }
}
