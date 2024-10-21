package nuber.students;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.RejectedExecutionException;

public class NuberRegion {

    private final String regionName;
    private final int maxSimultaneousJobs;
    private final NuberDispatch dispatch;
    private final ExecutorService executorService;
    private final Semaphore bookingSemaphore;
    private volatile boolean isShuttingDown;

    public NuberRegion(NuberDispatch dispatch, String regionName, int maxSimultaneousJobs) {
        this.dispatch = dispatch;
        this.regionName = regionName;
        this.maxSimultaneousJobs = maxSimultaneousJobs;

        this.executorService = Executors.newCachedThreadPool();
        this.bookingSemaphore = new Semaphore(maxSimultaneousJobs);
    }

    public Future<BookingResult> bookPassenger(Passenger waitingPassenger) {
        if (executorService.isShutdown()) {
            dispatch.logEvent(null, "Rejected booking");
            return null;
        }

        Booking booking = new Booking(dispatch, waitingPassenger);

        Future<BookingResult> future;
        try {
            future = executorService.submit(() -> {
                bookingSemaphore.acquire();
                try {
                    return booking.call();
                } finally {
                    bookingSemaphore.release();
                }
            });
        } catch (RejectedExecutionException e) {
            dispatch.logEvent(null, "Rejected booking due to executor shutdown");
            return null;
        }

        return future;
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
