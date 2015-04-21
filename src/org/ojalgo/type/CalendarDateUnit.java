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

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.ojalgo.constant.PrimitiveMath;

public enum CalendarDateUnit {

    /**
     * 
     */
    MILLIS(TimeUnit.MILLISECONDS),
    /**
     * 
     */
    SECOND(TimeUnit.SECONDS),
    /**
     * 
     */
    MINUTE(TypeUtils.MILLIS_PER_HOUR / 60L),
    /**
     * 
     */
    HOUR(TypeUtils.MILLIS_PER_HOUR),
    /**
     * 
     */
    DAY(24L * TypeUtils.MILLIS_PER_HOUR),
    /**
     * 
     */
    WEEK(7L * 24L * TypeUtils.MILLIS_PER_HOUR),
    /**
     * 
     */
    MONTH((TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR) / 1200L),
    /**
     * 
     */
    QUARTER((TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR) / 400L),
    /**
     * 
     */
    YEAR((TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR) / 100L),
    /**
     * 
     */
    DECADE((TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR) / 10L),
    /**
     * 
     */
    CENTURY(TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR),
    /**
     * 
     */
    MILLENIUM(10L * TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR);

    private final TimeUnit myTimeUnit;
    private final long mySize;
    private final long myHalf;

    CalendarDateUnit(final long aMillis) {
        myTimeUnit = null;
        mySize = aMillis;
        myHalf = mySize / 2L;
    }

    CalendarDateUnit(final TimeUnit aTimeUnit) {
        myTimeUnit = aTimeUnit;
        mySize = myTimeUnit.toMillis(1L);
        myHalf = mySize / 2L;
    }

    public CalendarDateDuration convert(final CalendarDateDuration aSourceDuration) {
        return aSourceDuration.convertTo(this);
    }

    public double convert(final CalendarDateUnit aSourceDurationUnit) {
        return this.convert(PrimitiveMath.ONE, aSourceDurationUnit);
    }

    public double convert(final double aSourceDurationMeasure, final CalendarDateUnit aSourceDurationUnit) {

        final double tmpSourceSize = aSourceDurationUnit.size();
        final double tmpDestinationSize = mySize;

        if (tmpSourceSize > tmpDestinationSize) {

            final double tmpScale = tmpSourceSize / tmpDestinationSize;

            return aSourceDurationMeasure * tmpScale;

        } else if (tmpSourceSize < tmpDestinationSize) {

            final double tmpScale = tmpDestinationSize / tmpSourceSize;

            return aSourceDurationMeasure / tmpScale;

        } else {

            return aSourceDurationMeasure;
        }
    }

    public long convert(final long aSourceDuration, final CalendarDateUnit aSourceUnit) {
        if ((myTimeUnit != null) && (aSourceUnit.getTimeUnit() != null)) {
            return myTimeUnit.convert(aSourceDuration, aSourceUnit.getTimeUnit());
        } else {
            return Math.round(this.convert((double) aSourceDuration, aSourceUnit));
        }
    }

    public long count(final Calendar aFromValue, final Calendar aToValue) {
        return ((myHalf + this.toTimeInMillis(aToValue)) - this.toTimeInMillis(aFromValue)) / mySize;
    }

    public long count(final Date aFromValue, final Date aToValue) {
        return ((myHalf + this.toTimeInMillis(aToValue)) - this.toTimeInMillis(aFromValue)) / mySize;
    }

    public long count(final long aFromValue, final long aToValue) {
        return ((myHalf + this.toTimeInMillis(aToValue)) - this.toTimeInMillis(aFromValue)) / mySize;
    }

    /**
     * Note that this method may, and actually does, return null in many cases.
     */
    public TimeUnit getTimeUnit() {
        return myTimeUnit;
    }

    public boolean isCalendarUnit() {
        return DAY.size() <= this.size();
    }

    public void round(final Calendar aCalendar) {

        if (CalendarDateUnit.MILLIS.size() < mySize) {

            aCalendar.set(Calendar.MILLISECOND, 0);

            if (CalendarDateUnit.SECOND.size() < mySize) {

                aCalendar.set(Calendar.SECOND, 0);

                if (CalendarDateUnit.MINUTE.size() < mySize) {

                    aCalendar.set(Calendar.MINUTE, 0);

                    if (CalendarDateUnit.HOUR.size() < mySize) {

                        aCalendar.set(Calendar.HOUR_OF_DAY, 12);

                        if (CalendarDateUnit.DAY.size() < mySize) {

                            if (CalendarDateUnit.WEEK.size() == mySize) {

                                aCalendar.add(Calendar.WEEK_OF_YEAR, 1);
                                aCalendar.set(Calendar.DAY_OF_WEEK, aCalendar.getFirstDayOfWeek());
                                aCalendar.add(Calendar.DAY_OF_WEEK, -1);

                            } else if (CalendarDateUnit.MONTH.size() == mySize) {

                                aCalendar.add(Calendar.MONTH, 1);
                                aCalendar.set(Calendar.DAY_OF_MONTH, 1);
                                aCalendar.add(Calendar.DAY_OF_MONTH, -1);

                            } else if (CalendarDateUnit.QUARTER.size() == mySize) {

                                aCalendar.set(Calendar.MONTH, 3 * ((aCalendar.get(Calendar.MONTH) / 3) + 1));
                                aCalendar.set(Calendar.DAY_OF_MONTH, 1);
                                aCalendar.add(Calendar.DAY_OF_MONTH, -1);

                            } else if (CalendarDateUnit.YEAR.size() == mySize) {

                                aCalendar.add(Calendar.YEAR, 1);
                                aCalendar.set(Calendar.DAY_OF_YEAR, 1);
                                aCalendar.add(Calendar.DAY_OF_YEAR, -1);

                            } else if (CalendarDateUnit.DECADE.size() == mySize) {

                                aCalendar.set(Calendar.YEAR, 10 + (10 * (aCalendar.get(Calendar.YEAR) / 10)));
                                aCalendar.set(Calendar.DAY_OF_YEAR, 1);
                                aCalendar.add(Calendar.DAY_OF_YEAR, -1);

                            } else if (CalendarDateUnit.CENTURY.size() == mySize) {

                                aCalendar.set(Calendar.YEAR, 100 + (100 * (aCalendar.get(Calendar.YEAR) / 100)));
                                aCalendar.set(Calendar.DAY_OF_YEAR, 1);
                                aCalendar.add(Calendar.DAY_OF_YEAR, -1);

                            } else if (CalendarDateUnit.MILLENIUM.size() == mySize) {

                                aCalendar.set(Calendar.YEAR, 1000 + (1000 * (aCalendar.get(Calendar.YEAR) / 1000)));
                                aCalendar.set(Calendar.DAY_OF_YEAR, 1);
                                aCalendar.add(Calendar.DAY_OF_YEAR, -1);
                            }
                        }
                    }
                }
            }
        }
    }

    public void round(final Date aDate) {
        aDate.setTime(this.toTimeInMillis(aDate));
    }

    public long size() {
        return mySize;
    }

    public Calendar step(final Calendar aCalendar) {
        return this.step(aCalendar, 1);
    }

    public Calendar step(final Calendar aCalendar, final int aStepCount) {

        final Calendar retVal = (Calendar) aCalendar.clone();

        switch (this) {

        case MILLENIUM:

            retVal.set(Calendar.DAY_OF_YEAR, 1);
            retVal.add(Calendar.YEAR, (1000 * aStepCount) + 1);
            retVal.add(Calendar.DAY_OF_YEAR, -1);

            break;

        case CENTURY:

            retVal.set(Calendar.DAY_OF_YEAR, 1);
            retVal.add(Calendar.YEAR, (100 * aStepCount) + 1);
            retVal.add(Calendar.DAY_OF_YEAR, -1);

            break;

        case DECADE:

            retVal.set(Calendar.DAY_OF_YEAR, 1);
            retVal.add(Calendar.YEAR, (10 * aStepCount) + 1);
            retVal.add(Calendar.DAY_OF_YEAR, -1);

            break;

        case YEAR:

            retVal.set(Calendar.DAY_OF_YEAR, 1);
            retVal.add(Calendar.YEAR, aStepCount + 1);
            retVal.add(Calendar.DAY_OF_YEAR, -1);

            break;

        case QUARTER:

            retVal.set(Calendar.DAY_OF_MONTH, 1);
            retVal.add(Calendar.MONTH, (3 * aStepCount) + 1);
            retVal.add(Calendar.DAY_OF_MONTH, -1);

            break;

        case MONTH:

            retVal.set(Calendar.DAY_OF_MONTH, 1);
            retVal.add(Calendar.MONTH, aStepCount + 1);
            retVal.add(Calendar.DAY_OF_MONTH, -1);

            break;

        case WEEK:

            retVal.set(Calendar.DAY_OF_WEEK, aCalendar.getFirstDayOfWeek());
            retVal.add(Calendar.WEEK_OF_YEAR, aStepCount + 1);
            retVal.add(Calendar.DAY_OF_WEEK, -1);

            break;

        case DAY:

            retVal.add(Calendar.DAY_OF_MONTH, aStepCount);
            break;

        case HOUR:

            retVal.add(Calendar.HOUR_OF_DAY, aStepCount);
            break;

        case MINUTE:

            retVal.add(Calendar.MINUTE, aStepCount);
            break;

        case SECOND:

            retVal.add(Calendar.SECOND, aStepCount);
            break;

        case MILLIS:

            retVal.add(Calendar.MILLISECOND, aStepCount);
            break;

        default:

            break;
        }

        this.round(retVal);

        return retVal;
    }

    public Date step(final Date aDate) {
        return this.step(aDate, 1);
    }

    public Date step(final Date aDate, final int aStepCount) {
        return new Date(this.toTimeInMillis(aDate) + (aStepCount * this.size()));
    }

    public long step(final long aTimeInMillis) {
        return this.step(aTimeInMillis, 1);
    }

    public long step(final long aTimeInMillis, final int aStepCount) {
        return this.toTimeInMillis(aTimeInMillis) + (aStepCount * this.size());
    }

    public long toTimeInMillis(final Calendar aCalendar) {
        final Calendar tmpClone = (Calendar) aCalendar.clone();
        this.round(tmpClone);
        return tmpClone.getTimeInMillis();
    }

    public long toTimeInMillis(final Date aDate) {
        return this.toTimeInMillis(aDate.getTime());
    }

    public long toTimeInMillis(final long aTimeInMillis) {
        return ((aTimeInMillis / mySize) * mySize) + myHalf;
    }

}
