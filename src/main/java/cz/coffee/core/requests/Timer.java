package cz.coffee.core.requests; /**
 * This file is part of CoffeeThing.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <<a href="http://www.gnu.org/licenses/">...</a>>.
 * <p>
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: sobota (01.04.2023)
 */
/**
 * This file is part of untitled.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <<a href="http://www.gnu.org/licenses/">...</a>>.
 * <p>
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: středa (29.03.2023)
 */
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
