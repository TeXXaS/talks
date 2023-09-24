import java.time.Duration;
import java.util.Random;
import java.util.concurrent.*;

public class App1 {
    public static void main(String[] args) {
        var a = new App1();
        a.runInParallel();
    }

    public void runInParallel (){
        final ThreadFactory factory = Thread.ofVirtual().name("routine-", 0).factory();
        try(var scope = new StructuredTaskScope.ShutdownOnFailure("worker-", factory)){
            for(int i = 0; i < 10_000; i++){
                var b = scope.fork(this::sampleFlow);
            }
            scope.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Integer sampleFlow() {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var userSubtask = scope.fork(this::getUser);
            scope.join().throwIfFailed();
            var user = userSubtask.get();
            var usersPowerSubtask = scope.fork(() -> calculateUsersPower(user));
            var userOptionsSubtask = scope.fork(() -> determineAvailableOptions(user));
            scope.join().throwIfFailed();
            var usersPower = usersPowerSubtask.get();
            var userOptions = userOptionsSubtask.get();
            var offerSubtask = scope.fork(() -> calculateOffer(user, usersPower, userOptions));
            scope.join();
            System.out.println("final offer " + offerSubtask.get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    private String getUser() throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(2));
        Random rand = new Random();
        var user = "user" + rand.nextInt(10000);
        System.out.println("returning user: " + user);
        return user;
    }

    private Long calculateUsersPower(String user) throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(3));
        Random rand = new Random();
        var power = rand.nextLong(100) - 50;
        System.out.println("returning user: " + user +" - power: " + power);
        return power;
    }

    private String determineAvailableOptions(String user) throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(1));
        var options = "long list of options";
        System.out.println("returning user: " + user + " - options: " +options);
        return options;
    }

    private String calculateOffer(String user, Long usersPower, String options) throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(5));
        System.out.println("returning offer for user: " + user + " with power: " + usersPower + " for options: " + options);
        return "the OFFER for " + user;
    }
}
