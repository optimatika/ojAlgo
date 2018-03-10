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

import java.io.Serializable;
import java.time.Duration;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
public enum CalendarDateUnit implements TemporalUnit, CalendarDate.Resolution, Comparable<CalendarDateUnit>, Serializable {

    /**
     *
     */
    NANOS(ChronoUnit.NANOS, TimeUnit.NANOSECONDS, "ns"),
    /**
     *
     */
    MICROS(ChronoUnit.MICROS, TimeUnit.MICROSECONDS, "micros"),
    /**
     *
     */
    MILLIS(ChronoUnit.MILLIS, TimeUnit.MILLISECONDS, "ms"),
    /**
     *
     */
    SECOND(ChronoUnit.SECONDS, TimeUnit.SECONDS, "s"),
    /**
     *
     */
    MINUTE(ChronoUnit.MINUTES, TimeUnit.MINUTES, "min"),
    /**
     *
     */
    HOUR(ChronoUnit.HOURS, TimeUnit.HOURS, "h"),
    /**
     *
     */
    DAY(ChronoUnit.DAYS, TimeUnit.DAYS, "days"),
    /**
     *
     */
    WEEK(ChronoUnit.WEEKS, 7L * 24L * TypeUtils.MILLIS_PER_HOUR, "weeks"),
    /**
     *
     */
    MONTH(ChronoUnit.MONTHS, (TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR) / 1200L, "months"),
    /**
     *
     */
    QUARTER(null, (TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR) / 400L, "quarters"),
    /**
     *
     */
    YEAR(ChronoUnit.YEARS, (TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR) / 100L, "years"),
    /**
     *
     */
    DECADE(ChronoUnit.DECADES, (TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR) / 10L, "decades"),
    /**
     *
     */
    CENTURY(ChronoUnit.CENTURIES, TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR, "centuries"),
    /**
     *
     */
    MILLENIUM(ChronoUnit.MILLENNIA, 10L * TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR, "millennia");

    private final ChronoUnit myChronoUnit;
    private final long myDurationInMillis;
    private final long myDurationInNanos;
    private final long myHalf;
    private final String myLabel;
    private final TimeUnit myTimeUnit;

    CalendarDateUnit(final ChronoUnit chronoUnit, final long millis, final String label) {
        myChronoUnit = chronoUnit;
        myTimeUnit = null;
        myDurationInMillis = millis;
        myHalf = myDurationInMillis / 2L;
        myDurationInNanos = millis * CalendarDate.NANOS_PER_MILLIS;
        myLabel = label;
    }

    CalendarDateUnit(final ChronoUnit chronoUnit, final TimeUnit timeUnit, final String label) {
        myChronoUnit = chronoUnit;
        myTimeUnit = timeUnit;
        myDurationInMillis = myTimeUnit.toMillis(1L);
        myHalf = myDurationInMillis / 2L;
        myDurationInNanos = timeUnit.toNanos(1L);
        myLabel = label;
    }

    public <R extends Temporal> R addTo(final R temporal, final long amount) {
        if (myChronoUnit != null) {
            return myChronoUnit.addTo(temporal, amount);
        } else { // QUARTER
            return ChronoUnit.MONTHS.addTo(temporal, 3L * amount);
        }
    }

    public CalendarDate adjustInto(final Calendar temporal) {
        return CalendarDate.make(temporal, this);
    }

    public CalendarDate adjustInto(final Date temporal) {
        return CalendarDate.make(temporal, this);
    }

    public CalendarDate adjustInto(final Temporal temporal) {
        if (temporal instanceof CalendarDate) {
            return CalendarDate.make(((CalendarDate) temporal).millis, this);
        } else {
            final long seconds = temporal.getLong(ChronoField.INSTANT_SECONDS);
            final long nanos = temporal.getLong(ChronoField.MILLI_OF_SECOND);
            final long millis = (seconds / CalendarDate.MILLIS_PER_SECOND) + (nanos * (CalendarDate.MILLIS_PER_SECOND / CalendarDate.NANOS_PER_SECOND));
            return CalendarDate.make(millis, this);
        }
    }

    public long between(final Temporal temporal1Inclusive, final Temporal temporal2Exclusive) {
        if (myChronoUnit != null) {
            return myChronoUnit.between(temporal1Inclusive, temporal2Exclusive);
        } else { // QUARTER
            return ChronoUnit.MONTHS.between(temporal1Inclusive, temporal2Exclusive) / 3L;
        }
    }

    public CalendarDateDuration convert(final CalendarDateDuration sourceDuration) {
        return sourceDuration.convertTo(this);
    }

    public double convert(final CalendarDateUnit aSourceDurationUnit) {
        return this.convert(PrimitiveMath.ONE, aSourceDurationUnit);
    }

    public double convert(final double sourceDurationMeasure, final CalendarDateUnit sourceDurationUnit) {

        final double sourceDurationInNanos = sourceDurationUnit.toDurationInNanos();
        final double destinationDurationInNanos = myDurationInNanos;

        if (sourceDurationInNanos > destinationDurationInNanos) {

            final double scaleUp = sourceDurationInNanos / destinationDurationInNanos;

            return sourceDurationMeasure * scaleUp;

        } else if (sourceDurationInNanos < destinationDurationInNanos) {

            final double scaleDown = destinationDurationInNanos / sourceDurationInNanos;

            return sourceDurationMeasure / scaleDown;

        } else {

            return sourceDurationMeasure;
        }
    }

    public long convert(final long sourceMeassure, final CalendarDateUnit sourceUnit) {
        final Optional<TimeUnit> tmpTimeUnit = sourceUnit.getTimeUnit();
        if ((myTimeUnit != null) && (tmpTimeUnit.isPresent())) {
            return myTimeUnit.convert(sourceMeassure, tmpTimeUnit.get());
        } else {
            return Math.round(this.convert((double) sourceMeassure, sourceUnit));
        }
    }

    public long count(final Calendar aFromValue, final Calendar aToValue) {
        return ((myHalf + this.toTimeInMillis(aToValue)) - this.toTimeInMillis(aFromValue)) / myDurationInMillis;
    }

    public long count(final Date aFromValue, final Date aToValue) {
        return ((myHalf + this.toTimeInMillis(aToValue)) - this.toTimeInMillis(aFromValue)) / myDurationInMillis;
    }

    public long count(final long aFromValue, final long aToValue) {
        return ((myHalf + this.toTimeInMillis(aToValue)) - this.toTimeInMillis(aFromValue)) / myDurationInMillis;
    }

    public long get(final TemporalUnit unit) {
        if (unit == this) {
            return myDurationInMillis;
        } else {
            return 0;
        }
    }

    public Optional<ChronoUnit> getChronoUnit() {
        return Optional.ofNullable(myChronoUnit);
    }

    public Duration getDuration() {
        if (myChronoUnit != null) {
            return myChronoUnit.getDuration();
        } else { // QUARTER
            return ChronoUnit.MONTHS.getDuration().multipliedBy(3L);
        }
    }

    public String getLabel() {
        return myLabel;
    }

    public Optional<TimeUnit> getTimeUnit() {
        return Optional.ofNullable(myTimeUnit);
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

        if (CalendarDateUnit.MILLIS.size() < myDurationInMillis) {

            calendar.set(Calendar.MILLISECOND, 0);

            if (CalendarDateUnit.SECOND.size() < myDurationInMillis) {

                calendar.set(Calendar.SECOND, 0);

                if (CalendarDateUnit.MINUTE.size() < myDurationInMillis) {

                    calendar.set(Calendar.MINUTE, 0);

                    if (CalendarDateUnit.HOUR.size() < myDurationInMillis) {

                        calendar.set(Calendar.HOUR_OF_DAY, 12);

                        if (CalendarDateUnit.DAY.size() < myDurationInMillis) {

                            if (CalendarDateUnit.WEEK.size() == myDurationInMillis) {

                                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                                calendar.add(Calendar.DAY_OF_WEEK, -1);

                            } else if (CalendarDateUnit.MONTH.size() == myDurationInMillis) {

                                calendar.add(Calendar.MONTH, 1);
                                calendar.set(Calendar.DAY_OF_MONTH, 1);
                                calendar.add(Calendar.DAY_OF_MONTH, -1);

                            } else if (CalendarDateUnit.QUARTER.size() == myDurationInMillis) {

                                calendar.set(Calendar.MONTH, 3 * ((calendar.get(Calendar.MONTH) / 3) + 1));
                                calendar.set(Calendar.DAY_OF_MONTH, 1);
                                calendar.add(Calendar.DAY_OF_MONTH, -1);

                            } else if (CalendarDateUnit.YEAR.size() == myDurationInMillis) {

                                calendar.add(Calendar.YEAR, 1);
                                calendar.set(Calendar.DAY_OF_YEAR, 1);
                                calendar.add(Calendar.DAY_OF_YEAR, -1);

                            } else if (CalendarDateUnit.DECADE.size() == myDurationInMillis) {

                                calendar.set(Calendar.YEAR, 10 + (10 * (calendar.get(Calendar.YEAR) / 10)));
                                calendar.set(Calendar.DAY_OF_YEAR, 1);
                                calendar.add(Calendar.DAY_OF_YEAR, -1);

                            } else if (CalendarDateUnit.CENTURY.size() == myDurationInMillis) {

                                calendar.set(Calendar.YEAR, 100 + (100 * (calendar.get(Calendar.YEAR) / 100)));
                                calendar.set(Calendar.DAY_OF_YEAR, 1);
                                calendar.add(Calendar.DAY_OF_YEAR, -1);

                            } else if (CalendarDateUnit.MILLENIUM.size() == myDurationInMillis) {

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
        return myDurationInMillis;
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

    public long toDurationInMillis() {
        return myDurationInMillis;
    }

    public long toDurationInNanos() {
        return myDurationInNanos;
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
        return ((aTimeInMillis / myDurationInMillis) * myDurationInMillis) + myHalf;
    }

}
