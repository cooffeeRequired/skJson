package cz.coffee.core.requests;

public class Timer {
    private long _elapsedTime;

    public Timer() {
        _elapsedTime = 0;
    }

    public void addTime(long nanoseconds) {
        _elapsedTime += nanoseconds;
    }

    public void reset() {
        _elapsedTime = 0;
    }

    public long getElapsedTime() {
        return _elapsedTime;
    }

    public String getTime() {
        return toString();
    }

    public String toString() {
        long millis = _elapsedTime / 1000000;
        long micros = _elapsedTime / 1000 % 1000;
        long nanos = _elapsedTime % 1000;
        return String.format("Elapsed time... %d ms, %d μs, %d ns", millis, micros, nanos);
    }
}
