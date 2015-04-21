/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ScheduleBuilder {

    private final TimerTask myTask;
    private Date myStartDate = null;
    private int myRepetitionMeassure;
    private CalendarDateUnit myRepetitionUnit = null;

    public ScheduleBuilder(final TimerTask aTask) {

        super();

        myTask = aTask;
    }

    @SuppressWarnings("unused")
    private ScheduleBuilder() {
        this(null);
    }

    public ScheduleBuilder repetition(final int aRepetitionMeassure, final CalendarDateUnit aRepetitionUnit) {
        myRepetitionMeassure = aRepetitionMeassure;
        myRepetitionUnit = aRepetitionUnit;
        return this;
    }

    public void schedule(final Timer aTimer) {
        if (myStartDate != null) {
            if (myRepetitionUnit != null) {
                aTimer.scheduleAtFixedRate(myTask, myStartDate, myRepetitionMeassure * myRepetitionUnit.size());
            } else {
                aTimer.schedule(myTask, myStartDate);
            }
        } else {
            if (myRepetitionUnit != null) {
                aTimer.scheduleAtFixedRate(myTask, new Date(), myRepetitionMeassure * myRepetitionUnit.size());
            } else {
                aTimer.schedule(myTask, new Date());
            }
        }
    }

    public ScheduleBuilder start(final Date aStartDate) {
        myStartDate = new Date(aStartDate.getTime());
        return this;
    }

    public ScheduleBuilder start(final int aDelayMeassure, final CalendarDateUnit aDelayUnit) {
        myStartDate = new Date(System.currentTimeMillis() + aDelayMeassure * aDelayUnit.size());
        return this;
    }

}
