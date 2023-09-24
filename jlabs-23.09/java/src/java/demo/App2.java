package demo;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class App2 {

    public static void main(String[] args) {
        System.out.println("Hello world!");

        teaInPool();
    }

    static void log(String message) {
        System.out.println(Thread.currentThread() + " -> " + message);
    }

    private static Thread virtualThread(String name, Runnable runnable) {
        return Thread.ofVirtual()
                .name(name)
                .start(runnable);
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static Thread prepareTea() {
        return virtualThread(
                "prepare tea",
                () -> {
                    log("I'm going to prepare tea");
                    sleep(Duration.ofMillis(500L));
                    log("tea is ready");
                });
    }

    static Thread boilingWater() {
        return virtualThread(
                "boil water in teapot",
                () -> {
                    log("I'm going to boil some water");
                    sleep(Duration.ofSeconds(1L));
                    log("water boiled");
                });
    }

    static Thread teaCeremony() {
        return virtualThread(
                "tea ceremony",
                () -> {
                    log("I'm going to perform tea ceremony");
                    sleep(Duration.ofSeconds(2L));
                    log("tea was great");
                });
    }

    static void teaTime() throws InterruptedException {
        var prepareTea = prepareTea();
        var boilingWater = boilingWater();
        boilingWater.join();
        prepareTea.join();
        var teaCeremony = teaCeremony();
        teaCeremony.join();
    }

    static void teaInPool() {
        final ThreadFactory factory = Thread.ofVirtual().name("routine-", 0).factory();
        try (var executor = Executors.newThreadPerTaskExecutor(factory)) {
            IntStream.range(0, 5)
                    .forEach(i -> executor.submit(() -> {
                        try {
                            teaTime();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        sleep(Duration.ofSeconds(1L));
                    }));
        }
    }
}
