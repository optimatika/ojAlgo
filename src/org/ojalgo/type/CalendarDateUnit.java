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

import java.time.Duration;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.ojalgo.constant.PrimitiveMath;

/**
 * <p>
 * Designed to complement {@linkplain CalendarDate}. It is essentially equivalent to {@linkplain ChronoUnit},
 * but with a sligthly smaller set of members (and the addtional {@linkplain #QUARTER}). It has been
 * retrofitted to implement the {@linkplain TemporalUnit} interface.
 * </p>
 *
 * @see CalendarDate
 * @see CalendarDateDuration
 * @author apete
 */
public enum CalendarDateUnit implements TemporalUnit, TemporalAmount, TemporalAdjuster {

    /**
     *
     */
    MILLIS(ChronoUnit.MILLIS, TimeUnit.MILLISECONDS),
    /**
     *
     */
    SECOND(ChronoUnit.SECONDS, TimeUnit.SECONDS),
    /**
     *
     */
    MINUTE(ChronoUnit.MINUTES, TimeUnit.MINUTES),
    /**
     *
     */
    HOUR(ChronoUnit.HOURS, TimeUnit.HOURS),
    /**
     *
     */
    DAY(ChronoUnit.DAYS, 24L * TypeUtils.MILLIS_PER_HOUR),
    /**
     *
     */
    WEEK(ChronoUnit.WEEKS, 7L * 24L * TypeUtils.MILLIS_PER_HOUR),
    /**
     *
     */
    MONTH(ChronoUnit.MONTHS, (TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR) / 1200L),
    /**
     *
     */
    QUARTER(null, (TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR) / 400L),
    /**
     *
     */
    YEAR(ChronoUnit.YEARS, (TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR) / 100L),
    /**
     *
     */
    DECADE(ChronoUnit.DECADES, (TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR) / 10L),
    /**
     *
     */
    CENTURY(ChronoUnit.CENTURIES, TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR),
    /**
     *
     */
    MILLENIUM(ChronoUnit.MILLENNIA, 10L * TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR);

    private final ChronoUnit myChronoUnit;
    private final long myHalf;
    private final long mySize;
    private final TimeUnit myTimeUnit;

    CalendarDateUnit(final ChronoUnit chronoUnit, final long millis) {
        myChronoUnit = chronoUnit;
        myTimeUnit = null;
        mySize = millis;
        myHalf = mySize / 2L;
    }

    CalendarDateUnit(final ChronoUnit chronoUnit, final TimeUnit timeUnit) {
        myChronoUnit = chronoUnit;
        myTimeUnit = timeUnit;
        mySize = myTimeUnit.toMillis(1L);
        myHalf = mySize / 2L;
    }

    public <R extends Temporal> R addTo(final R temporal, final long amount) {
        if (myChronoUnit != null) {
            return myChronoUnit.addTo(temporal, amount);
        } else { // QUARTER
            return ChronoUnit.MONTHS.addTo(temporal, 3L * amount);
        }
    }

    public Temporal addTo(final Temporal temporal) {
        return temporal.plus(mySize, CalendarDateUnit.MILLIS);
    }

    public Temporal adjustInto(final Temporal temporal) {
        return temporal.with(ChronoField.INSTANT_SECONDS, mySize / 1000L).with(ChronoField.MILLI_OF_SECOND, mySize % 1000L);
    }

    public long between(final Temporal temporal1Inclusive, final Temporal temporal2Exclusive) {
        if (myChronoUnit != null) {
            return myChronoUnit.between(temporal1Inclusive, temporal2Exclusive);
        } else { // QUARTER
            return ChronoUnit.MONTHS.between(temporal1Inclusive, temporal2Exclusive) / 3L;
        }
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

    public long get(final TemporalUnit unit) {
        if (unit == this) {
            return mySize;
        } else {
            return 0;
        }
    }

    public Duration getDuration() {
        if (myChronoUnit != null) {
            return myChronoUnit.getDuration();
        } else { // QUARTER
            return ChronoUnit.MONTHS.getDuration().multipliedBy(3L);
        }
    }

    /**
     * Note that this method may, and actually does, return null in many cases.
     */
    public TimeUnit getTimeUnit() {
        return myTimeUnit;
    }

    public List<TemporalUnit> getUnits() {
        return Collections.singletonList(this);
    }

    public boolean isCalendarUnit() {
        return DAY.size() <= this.size();
    }

    public boolean isDateBased() {
        if (myChronoUnit != null) {
            return myChronoUnit.isDateBased();
        } else { // QUARTER
            return true;
        }
    }

    public boolean isDurationEstimated() {
        if (myChronoUnit != null) {
            return myChronoUnit.isDurationEstimated();
        } else { // QUARTER
            return true;
        }
    }

    public boolean isTimeBased() {
        if (myChronoUnit != null) {
            return myChronoUnit.isTimeBased();
        } else { // QUARTER
            return false;
        }
    }

    public void round(final Calendar calendar) {

        if (CalendarDateUnit.MILLIS.size() < mySize) {

            calendar.set(Calendar.MILLISECOND, 0);

            if (CalendarDateUnit.SECOND.size() < mySize) {

                calendar.set(Calendar.SECOND, 0);

                if (CalendarDateUnit.MINUTE.size() < mySize) {

                    calendar.set(Calendar.MINUTE, 0);

                    if (CalendarDateUnit.HOUR.size() < mySize) {

                        calendar.set(Calendar.HOUR_OF_DAY, 12);

                        if (CalendarDateUnit.DAY.size() < mySize) {

                            if (CalendarDateUnit.WEEK.size() == mySize) {

                                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                                calendar.add(Calendar.DAY_OF_WEEK, -1);

                            } else if (CalendarDateUnit.MONTH.size() == mySize) {

                                calendar.add(Calendar.MONTH, 1);
                                calendar.set(Calendar.DAY_OF_MONTH, 1);
                                calendar.add(Calendar.DAY_OF_MONTH, -1);

                            } else if (CalendarDateUnit.QUARTER.size() == mySize) {

                                calendar.set(Calendar.MONTH, 3 * ((calendar.get(Calendar.MONTH) / 3) + 1));
                                calendar.set(Calendar.DAY_OF_MONTH, 1);
                                calendar.add(Calendar.DAY_OF_MONTH, -1);

                            } else if (CalendarDateUnit.YEAR.size() == mySize) {

                                calendar.add(Calendar.YEAR, 1);
                                calendar.set(Calendar.DAY_OF_YEAR, 1);
                                calendar.add(Calendar.DAY_OF_YEAR, -1);

                            } else if (CalendarDateUnit.DECADE.size() == mySize) {

                                calendar.set(Calendar.YEAR, 10 + (10 * (calendar.get(Calendar.YEAR) / 10)));
                                calendar.set(Calendar.DAY_OF_YEAR, 1);
                                calendar.add(Calendar.DAY_OF_YEAR, -1);

                            } else if (CalendarDateUnit.CENTURY.size() == mySize) {

                                calendar.set(Calendar.YEAR, 100 + (100 * (calendar.get(Calendar.YEAR) / 100)));
                                calendar.set(Calendar.DAY_OF_YEAR, 1);
                                calendar.add(Calendar.DAY_OF_YEAR, -1);

                            } else if (CalendarDateUnit.MILLENIUM.size() == mySize) {

                                calendar.set(Calendar.YEAR, 1000 + (1000 * (calendar.get(Calendar.YEAR) / 1000)));
                                calendar.set(Calendar.DAY_OF_YEAR, 1);
                                calendar.add(Calendar.DAY_OF_YEAR, -1);
                            }
                        }
                    }
                }
            }
        }
    }

    public void round(final Date date) {
        date.setTime(this.toTimeInMillis(date));
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

    public Temporal subtractFrom(final Temporal temporal) {
        return temporal.minus(mySize, CalendarDateUnit.MILLIS);
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
