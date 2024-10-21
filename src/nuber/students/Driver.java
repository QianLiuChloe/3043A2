package nuber.students;

public class Driver extends Person {

    private Passenger currentPassenger;

    public Driver(String driverName, int maxSleep) {
        super(driverName, maxSleep);
    }

    public void pickUpPassenger(Passenger newPassenger) throws InterruptedException {
        this.currentPassenger = newPassenger;
        long sleepTime = (long) (Math.random() * maxSleep);
        Thread.sleep(sleepTime);
    }

    public void driveToDestination() throws InterruptedException {
        if (currentPassenger == null) {
            throw new IllegalStateException("No passenger to drive to destination.");
        }
        int travelTime = currentPassenger.getTravelTime();
        Thread.sleep(travelTime);
    }
}
