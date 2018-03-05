/*
 * Copyright 1997-2018 Optimatika
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
    private long myStop;

    public Stopwatch() {

        super();

        this.reset();
    }

    /**
     * Reset the start-instant
     */
    public void reset() {
        myStart = System.nanoTime();
        myStop = myStart;
    }

    /**
     * This method can be called repeatedly without resetting (doesn't actually stop the timing process)
     *
     * @return The duration since instantiation or reset.
     */
    public CalendarDateDuration stop() {
        return this.stop(CalendarDateUnit.MILLIS);
    }

    public CalendarDateDuration stop(final CalendarDateUnit unit) {
        myStop = System.nanoTime();
        final CalendarDateDuration duration = new CalendarDateDuration(myStop - myStart, CalendarDateUnit.NANOS);
        return duration.convertTo(unit);
    }

}
