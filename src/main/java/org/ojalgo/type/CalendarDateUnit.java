/*
 * Copyright 1997-2024 Optimatika
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

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.ojalgo.function.constant.PrimitiveMath;

/**
 * <p>
 * Designed to complement {@linkplain CalendarDate}. It is essentially equivalent to {@linkplain ChronoUnit},
 * but with a slightly smaller set of members (and the additional {@linkplain #QUARTER}). It has been
 * retrofitted to implement the {@linkplain TemporalUnit} interface.
 * </p>
 *
 * @see CalendarDate
 * @see CalendarDateDuration
 * @author apete
 */
public enum CalendarDateUnit implements TemporalUnit, CalendarDate.Resolution {

    /**
     *
     */
    NANOS(ChronoUnit.NANOS, TimeUnit.NANOSECONDS, "ns"),
    /**
     *
     */
    MICROS(ChronoUnit.MICROS, TimeUnit.MICROSECONDS, "µs"),
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
    MONTH(ChronoUnit.MONTHS, TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR / 1200L, "months"),
    /**
     *
     */
    QUARTER(null, TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR / 400L, "quarters"),
    /**
     *
     */
    YEAR(ChronoUnit.YEARS, TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR / 100L, "years"),
    /**
     *
     */
    DECADE(ChronoUnit.DECADES, TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR / 10L, "decades"),
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
        myDurationInMillis = timeUnit.toMillis(1L);
        myHalf = myDurationInMillis / 2L;
        myDurationInNanos = timeUnit.toNanos(1L);
        myLabel = label;
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public <R extends Temporal> R addTo(final R temporal, final long amount) {
        if (temporal instanceof CalendarDate) {
            return (R) new CalendarDate(((CalendarDate) temporal).millis + this.toDurationInMillis());
        } else if (myChronoUnit != null) {
            return myChronoUnit.addTo(temporal, amount);
        } else { // QUARTER
            return ChronoUnit.MONTHS.addTo(temporal, 3L * amount);
        }
    }

    @Override
    public CalendarDate adjustInto(final CalendarDate temporal) {
        return new CalendarDate(this.adjustInto(temporal.millis));
    }

    @Override
    public long adjustInto(final long epochMilli) {
        return epochMilli / myDurationInMillis * myDurationInMillis + myHalf;
    }

    @Override
    public Temporal adjustInto(final Temporal temporal) {

        if (temporal instanceof CalendarDate) {

            return this.adjustInto((CalendarDate) temporal);

        } else {

            Temporal retVal = temporal;

            if (CalendarDateUnit.MILLIS.toDurationInMillis() < myDurationInMillis) {

                retVal = retVal.with(ChronoField.MILLI_OF_SECOND, 0L);

                if (CalendarDateUnit.SECOND.toDurationInMillis() < myDurationInMillis) {

                    retVal = retVal.with(ChronoField.SECOND_OF_MINUTE, 0L);

                    if (CalendarDateUnit.MINUTE.toDurationInMillis() < myDurationInMillis) {

                        retVal = retVal.with(ChronoField.MINUTE_OF_HOUR, 0L);

                        if (CalendarDateUnit.HOUR.toDurationInMillis() < myDurationInMillis) {

                            retVal = retVal.with(ChronoField.HOUR_OF_DAY, 12L);

                            if (CalendarDateUnit.DAY.toDurationInMillis() < myDurationInMillis) {

                                if (CalendarDateUnit.WEEK.toDurationInMillis() == myDurationInMillis) {

                                    retVal = retVal.minus(2L, ChronoUnit.DAYS).with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));

                                } else if (CalendarDateUnit.MONTH.toDurationInMillis() == myDurationInMillis) {

                                    retVal = retVal.with(TemporalAdjusters.lastDayOfMonth());

                                } else if (CalendarDateUnit.QUARTER.toDurationInMillis() == myDurationInMillis) {

                                    int nextMonth = 3 + 3 * (retVal.get(ChronoField.MONTH_OF_YEAR) / 3);
                                    retVal = retVal.with(ChronoField.MONTH_OF_YEAR, nextMonth).with(ChronoField.DAY_OF_MONTH, 1L).minus(1L, ChronoUnit.DAYS);

                                } else if (CalendarDateUnit.YEAR.toDurationInMillis() == myDurationInMillis) {

                                    retVal = retVal.with(TemporalAdjusters.lastDayOfYear());

                                } else if (CalendarDateUnit.DECADE.toDurationInMillis() == myDurationInMillis) {

                                    int nextYear = 10 + 10 * (retVal.get(ChronoField.YEAR) / 10);
                                    retVal = retVal.with(ChronoField.YEAR, nextYear).with(TemporalAdjusters.firstDayOfYear()).minus(1L, ChronoUnit.DAYS);

                                } else if (CalendarDateUnit.CENTURY.toDurationInMillis() == myDurationInMillis) {

                                    int nextYear = 100 + 100 * (retVal.get(ChronoField.YEAR) / 100);
                                    retVal = retVal.with(ChronoField.YEAR, nextYear).with(TemporalAdjusters.firstDayOfYear()).minus(1L, ChronoUnit.DAYS);

                                } else if (CalendarDateUnit.MILLENIUM.toDurationInMillis() == myDurationInMillis) {

                                    int nextYear = 1000 + 1000 * (retVal.get(ChronoField.YEAR) / 1000);
                                    retVal = retVal.with(ChronoField.YEAR, nextYear).with(TemporalAdjusters.firstDayOfYear()).minus(1L, ChronoUnit.DAYS);
                                }
                            }
                        }
                    }
                }
            }

            return retVal;
        }
    }

    @Override
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

        double sourceDurationInNanos = sourceDurationUnit.toDurationInNanos();
        double destinationDurationInNanos = myDurationInNanos;

        if (sourceDurationInNanos > destinationDurationInNanos) {

            double scaleUp = sourceDurationInNanos / destinationDurationInNanos;

            return sourceDurationMeasure * scaleUp;

        } else if (sourceDurationInNanos < destinationDurationInNanos) {

            double scaleDown = destinationDurationInNanos / sourceDurationInNanos;

            return sourceDurationMeasure / scaleDown;

        } else {

            return sourceDurationMeasure;
        }
    }

    public long convert(final long sourceMeassure, final CalendarDateUnit sourceUnit) {
        Optional<TimeUnit> tmpTimeUnit = sourceUnit.getTimeUnit();
        if (myTimeUnit != null && tmpTimeUnit.isPresent()) {
            return myTimeUnit.convert(sourceMeassure, tmpTimeUnit.get());
        } else {
            return Math.round(this.convert((double) sourceMeassure, sourceUnit));
        }
    }

    public long count(final long aFromValue, final long aToValue) {
        return (myHalf + this.adjustInto(aToValue) - this.adjustInto(aFromValue)) / myDurationInMillis;
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

    @Override
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
        return DAY.toDurationInMillis() <= this.toDurationInMillis();
    }

    @Override
    public boolean isDateBased() {
        if (myChronoUnit != null) {
            return myChronoUnit.isDateBased();
        } else { // QUARTER
            return true;
        }
    }

    @Override
    public boolean isDurationEstimated() {
        if (myChronoUnit != null) {
            return myChronoUnit.isDurationEstimated();
        } else { // QUARTER
            return true;
        }
    }

    @Override
    public boolean isTimeBased() {
        if (myChronoUnit != null) {
            return myChronoUnit.isTimeBased();
        } else { // QUARTER
            return false;
        }
    }

    public CalendarDateDuration newDuration(final double meassure) {
        return new CalendarDateDuration(meassure, this);
    }

    @Override
    public long toDurationInMillis() {
        return myDurationInMillis;
    }

    @Override
    public long toDurationInNanos() {
        return myDurationInNanos;
    }

}
