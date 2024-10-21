package nuber.students;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NuberDispatch {

    private final int MAX_DRIVERS = 999;
    private boolean logEvents = false;
    private final BlockingQueue<Driver> driverQueue;
    private final ConcurrentHashMap<String, NuberRegion> regions;
    private final AtomicInteger bookingsAwaitingDriver;

    public NuberDispatch(HashMap<String, Integer> regionInfo, boolean logEvents) {
        this.logEvents = logEvents;
        this.driverQueue = new LinkedBlockingQueue<>(MAX_DRIVERS);
        this.regions = new ConcurrentHashMap<>();
        this.bookingsAwaitingDriver = new AtomicInteger(0);

        for (Map.Entry<String, Integer> entry : regionInfo.entrySet()) {
            String regionName = entry.getKey();
            int maxSimultaneousJobs = entry.getValue();
            NuberRegion region = new NuberRegion(this, regionName, maxSimultaneousJobs);
            regions.put(regionName, region);
        }
    }

    public boolean addDriver(Driver newDriver) {
        if (driverQueue.size() >= MAX_DRIVERS) {
            return false;
        }
        try {
            driverQueue.put(newDriver);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public Driver getDriver() {
        try {
            return driverQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public void logEvent(Booking booking, String message) {
        if (!logEvents) return;
        System.out.println(booking + ": " + message);
    }

    public Future<BookingResult> bookPassenger(Passenger passenger, String region) {
        NuberRegion nuberRegion = regions.get(region);
        if (nuberRegion == null) {
            logEvent(null, "Region not found: " + region);
            return null;
        }

        Future<BookingResult> future = nuberRegion.bookPassenger(passenger);
        if (future == null) {
            logEvent(null, "Booking rejected by region " + region);
        }
        return future;
    }

    public int getBookingsAwaitingDriver() {
        return bookingsAwaitingDriver.get();
    }

    public void incrementBookingsAwaitingDriver() {
        bookingsAwaitingDriver.incrementAndGet();
    }

    public void decrementBookingsAwaitingDriver() {
        bookingsAwaitingDriver.decrementAndGet();
    }

    public void shutdown() {
        for (NuberRegion region : regions.values()) {
            region.shutdown();
        }
    }
}
