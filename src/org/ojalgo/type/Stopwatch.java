/*
 * Copyright 1997-2019 Optimatika
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.ojalgo.type;

/**
 * This stopwatch is always running. It start as soon as you create the instance.
 *
 * @author apete
 */
public class Stopwatch {

    /**
     * Meassure task duration using this class' stopwatch functionality.
     *
     * @param task The task to meassure
     * @return The meassured duration
     */
    public static CalendarDateDuration meassure(final Runnable task) {
        return Stopwatch.meassure(task, CalendarDateUnit.MILLIS);
    }

    public static CalendarDateDuration meassure(final Runnable task, final CalendarDateUnit unit) {

        final Stopwatch timer = new Stopwatch();

        task.run();

        return timer.stop(unit);
    }

    private long myStart;

    public Stopwatch() {

        super();

        myStart = System.nanoTime();
    }

    public long countMillis() {
        return this.countNanos() / CalendarDate.NANOS_PER_MILLIS;
    }

    public long countNanos() {
        return System.nanoTime() - myStart;
    }

    public boolean isLessThan(final CalendarDateDuration duration) {
        return this.isLessThanNanos(duration.toDurationInNanos());
    }

    public boolean isLessThanMillis(final long millis) {
        return this.isLessThanNanos(millis * CalendarDate.NANOS_PER_MILLIS);
    }

    public boolean isLessThanNanos(final long nanos) {
        return this.countNanos() < nanos;
    }

    public boolean isMoreThan(final CalendarDateDuration duration) {
        return this.isMoreThanNanos(duration.toDurationInNanos());
    }

    public boolean isMoreThanMillis(final long millis) {
        return this.isMoreThanNanos(millis * CalendarDate.NANOS_PER_MILLIS);
    }

    public boolean isMoreThanNanos(final long nanos) {
        return this.countNanos() > nanos;
    }

    /**
     * Reset the start-instant
     */
    public void reset() {
        myStart = System.nanoTime();
    }

    /**
     * This method can be called repeatedly without resetting (doesn't actually stop the timing process)
     *
     * @return The duration since instantiation or reset.
     */
    public CalendarDateDuration stop() {
        return CalendarDateDuration.of(this.countNanos());
    }

    public CalendarDateDuration stop(final CalendarDateUnit unit) {
        return this.stop().convertTo(unit);
    }

}
