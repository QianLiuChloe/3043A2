package nuber.students;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NuberDispatch {
    private final int MAX_DRIVERS = 999;
    private Queue<Driver> driverQueue = new LinkedList<>();
    private HashMap<String, NuberRegion> regions = new HashMap<>();
    private boolean logEvents;
    private int bookingsAwaitingDriver = 0;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public NuberDispatch(HashMap<String, Integer> regionInfo, boolean logEvents) {
        this.logEvents = logEvents;
        for (String regionName : regionInfo.keySet()) {
            regions.put(regionName, new NuberRegion(this, regionName, regionInfo.get(regionName)));
        }
    }

    public synchronized boolean addDriver(Driver newDriver) {
        if (driverQueue.size() < MAX_DRIVERS) {
            driverQueue.offer(newDriver);
            notifyAll(); // Notify threads waiting for a driver
            return true;
        }
        return false;
    }

    public synchronized Driver getDriver() throws InterruptedException {
        while (driverQueue.isEmpty()) {
            wait(); // Wait for a driver to become available
        }
        return driverQueue.poll();
    }

    public synchronized int getBookingsAwaitingDriver() {
        return bookingsAwaitingDriver;
    }

    public synchronized Future<BookingResult> bookPassenger(Passenger passenger, String region) {
        NuberRegion nuberRegion = regions.get(region);
        if (nuberRegion != null) {
            bookingsAwaitingDriver++; // Increment bookings awaiting driver
            return nuberRegion.bookPassenger(passenger);
        }
        return null;
    }

    public void logEvent(Booking booking, String message) {
        if (logEvents) {
            System.out.println(booking + ": " + message);
        }
    }

    public void decrementAwaitingDriverBookings() {
        synchronized (this) {
            bookingsAwaitingDriver--; // Decrement pending bookings when a driver is assigned
        }
    }

    public void shutdown() {
        for (NuberRegion region : regions.values()) {
            region.shutdown();
        }
        executorService.shutdown();
    }
}
