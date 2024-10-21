package nuber.students;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Future;

public class NuberDispatch {

    private final int MAX_DRIVERS = 999;
    private Queue<Driver> availableDrivers;
    private HashMap<String, NuberRegion> regions;
    private boolean logEvents;
    private boolean shutdownRequested = false;

    public NuberDispatch(HashMap<String, Integer> regionInfo, boolean logEvents) {
        this.availableDrivers = new LinkedList<>();
        this.regions = new HashMap<>();
        this.logEvents = logEvents;

        for (String regionName : regionInfo.keySet()) {
            regions.put(regionName, new NuberRegion(this, regionName, regionInfo.get(regionName)));
        }
    }

    public boolean addDriver(Driver newDriver) {
        synchronized (availableDrivers) {
            if (!shutdownRequested && availableDrivers.size() < MAX_DRIVERS) {
                availableDrivers.add(newDriver);
                availableDrivers.notify(); // Notify any threads waiting for a driver
                return true;
            }
            return false;
        }
    }


    public Driver getDriver() throws InterruptedException {
        synchronized (availableDrivers) {
            while (availableDrivers.isEmpty()) {
                availableDrivers.wait();
            }
            return availableDrivers.poll();
        }
    }



    public Future<BookingResult> bookPassenger(Passenger passenger, String region) {
        if (shutdownRequested) return null;
        return regions.get(region).bookPassenger(passenger);
    }

    public void logEvent(Booking booking, String message) {
        if (logEvents) {
            System.out.println(booking + ": " + message);
        }
    }

    public void shutdown() {
        shutdownRequested = true;
        for (NuberRegion region : regions.values()) {
            region.shutdown();
        }
    }

    public int getBookingsAwaitingDriver() {
        // This is a placeholder; implement logic as needed
        return 0;
    }
}
