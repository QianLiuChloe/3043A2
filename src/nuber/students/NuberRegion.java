package nuber.students;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NuberRegion {

    private NuberDispatch dispatch;
    private String regionName;
    private int maxSimultaneousJobs;
    private int currentJobs = 0;
    private boolean shutdownRequested = false;

    public NuberRegion(NuberDispatch dispatch, String regionName, int maxSimultaneousJobs) {
        this.dispatch = dispatch;
        this.regionName = regionName;
        this.maxSimultaneousJobs = maxSimultaneousJobs;
    }

    public synchronized Future<BookingResult> bookPassenger(Passenger waitingPassenger) {
        if (shutdownRequested || currentJobs >= maxSimultaneousJobs) return null;
        currentJobs++;
        Booking booking = new Booking(dispatch, waitingPassenger);
        return Executors.newSingleThreadExecutor().submit(booking);
    }

    public void shutdown() {
        shutdownRequested = true;
    }
}
