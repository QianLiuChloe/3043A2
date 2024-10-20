package nuber.students;

import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NuberRegion {
    private final NuberDispatch dispatch;
    private final String regionName;
    private final int maxSimultaneousJobs;
    private ExecutorService regionExecutor;

    public NuberRegion(NuberDispatch dispatch, String regionName, int maxSimultaneousJobs) {
        this.dispatch = dispatch;
        this.regionName = regionName;
        this.maxSimultaneousJobs = maxSimultaneousJobs;
        this.regionExecutor = Executors.newFixedThreadPool(maxSimultaneousJobs);
    }

    public Future<BookingResult> bookPassenger(Passenger waitingPassenger) {
        if (regionExecutor.isShutdown()) {
            dispatch.logEvent(null, "Booking rejected: Region " + regionName + " is shutting down.");
            return null; // Reject new bookings if the region is shutting down
        }
        Booking booking = new Booking(dispatch, waitingPassenger);
        return regionExecutor.submit(booking);
    }

    public void shutdown() {
        regionExecutor.shutdown(); // Stop accepting new tasks and finish pending ones
    }
}
