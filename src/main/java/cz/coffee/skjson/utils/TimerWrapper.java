package cz.coffee.skjson.utils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class TimerWrapper implements AutoCloseable{

    private final Timer timer;
    private int count;
    private final int delay;
    private long startTime;

    public TimerWrapper(int delay) {
        timer = new Timer();
        count = 0;
        this.delay = delay;
        startTime = 0;
        start();
    }

    private void start() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                count++;
                if (count == 1) {stop();}
            }
        };

        startTime = System.currentTimeMillis();
        timer.schedule(task, delay, 1);
    }

    private void stop() {
        timer.cancel();
    }

    public long getElapsedTime() {
        if (startTime == 0) {
            return 0;
        }
        return System.currentTimeMillis() - startTime;
    }

    public String toHumanTime() {
        long elapsedTime = getElapsedTime();

        long hours = TimeUnit.MILLISECONDS.toHours(elapsedTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60;
        long milliseconds = elapsedTime % 1000;
        long microseconds = (elapsedTime % 1000) * 1000;

        return String.format("%02dh %02dm %02ds.%03dms'%03dμs", hours, minutes, seconds, milliseconds, microseconds);
    }

    @Override
    public String toString() {
        return "TimerWrapper{"+"count="+count+", delay="+delay+ ", elapsedTime="+toHumanTime()+ '}';
    }

    @Override
    public void close() {
        stop();
    }
}
