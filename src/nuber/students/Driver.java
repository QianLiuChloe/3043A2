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
        if (currentPassenger != null) {
            long travelTime = currentPassenger.getTravelTime();
            Thread.sleep(travelTime);
        }
    }
}
