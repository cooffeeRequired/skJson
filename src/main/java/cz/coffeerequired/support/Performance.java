package cz.coffeerequired.support;

import java.util.concurrent.TimeUnit;

public class Performance {
    private long startTime;
    private long endTime;

    public Performance() {
        start();
    }

    public void start() {
        startTime = System.nanoTime(); // Start time in nanoseconds
        endTime = 0;
    }

    public long getElapsedTime() {
        if (startTime == 0) {
            throw new IllegalStateException("Timer has not been started.");
        }
        return (endTime == 0 ? System.nanoTime() : endTime) - startTime;
    }

    public void stop() {
        endTime = System.nanoTime();
    }

    public String toHumanTime() {
        long elapsedTime = getElapsedTime();

        long hours = TimeUnit.NANOSECONDS.toHours(elapsedTime);
        long minutes = TimeUnit.NANOSECONDS.toMinutes(elapsedTime) % 60;
        long seconds = TimeUnit.NANOSECONDS.toSeconds(elapsedTime) % 60;
        long milliseconds = TimeUnit.NANOSECONDS.toMillis(elapsedTime) % 1000;
        long microseconds = TimeUnit.NANOSECONDS.toMicros(elapsedTime) % 1000;
        long nanoseconds = elapsedTime % 1000;

        return String.format("%02dh %02dm %02ds %03dms %03dμs %03dns",
                hours, minutes, seconds, milliseconds, microseconds, nanoseconds);
    }
}