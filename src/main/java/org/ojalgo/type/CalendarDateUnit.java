/*
 * Copyright 1997-2026 Optimatika
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
import java.time.Instant;
import java.time.LocalTime;
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
 * Time units from nanoseconds to millennia: the {@link ChronoUnit}s from {@link ChronoUnit#NANOS} to
 * {@link ChronoUnit#MILLENNIA} (except half-days), plus {@link #QUARTER}. Each unit has a fixed length. From
 * {@link #MONTH} up these are average lengths, based on a year of 365.2425 days, equal to
 * {@link ChronoUnit#getDuration()}.
 * <ul>
 * <li>On a {@link CalendarDate} the fixed lengths are used.
 * <li>On other temporals the corresponding {@link ChronoUnit} is used, with {@link #QUARTER} as 3 months.
 * <li>As a {@link CalendarDate.Resolution} a unit divides the time-line into intervals of its length, also
 * working directly on epoch-millisecond longs.
 * </ul>
 *
 * @see CalendarDate
 * @see CalendarDateDuration
 * @author apete
 */
public enum CalendarDateUnit implements TemporalUnit, CalendarDate.Resolution {

    /**
     * A nanosecond, shorter than the millisecond precision of {@link CalendarDate}.
     */
    NANOS(ChronoUnit.NANOS, TimeUnit.NANOSECONDS, "ns"),
    /**
     * A microsecond, shorter than the millisecond precision of {@link CalendarDate}.
     */
    MICROS(ChronoUnit.MICROS, TimeUnit.MICROSECONDS, "µs"),
    /**
     * A millisecond.
     */
    MILLIS(ChronoUnit.MILLIS, TimeUnit.MILLISECONDS, "ms"),
    /**
     * A second.
     */
    SECOND(ChronoUnit.SECONDS, TimeUnit.SECONDS, "s"),
    /**
     * 60 seconds.
     */
    MINUTE(ChronoUnit.MINUTES, TimeUnit.MINUTES, "min"),
    /**
     * 60 minutes.
     */
    HOUR(ChronoUnit.HOURS, TimeUnit.HOURS, "h"),
    /**
     * 24 hours.
     */
    DAY(ChronoUnit.DAYS, TimeUnit.DAYS, "days"),
    /**
     * 7 days.
     */
    WEEK(ChronoUnit.WEEKS, 7L * 24L * TypeUtils.MILLIS_PER_HOUR, "weeks"),
    /**
     * 1/12 year, 30.436875 days on average.
     */
    MONTH(ChronoUnit.MONTHS, TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR / 1200L, "months"),
    /**
     * 3 months, 91.310625 days on average.
     */
    QUARTER(null, TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR / 400L, "quarters"),
    /**
     * 365.2425 days on average.
     */
    YEAR(ChronoUnit.YEARS, TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR / 100L, "years"),
    /**
     * 10 years.
     */
    DECADE(ChronoUnit.DECADES, TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR / 10L, "decades"),
    /**
     * 100 years.
     */
    CENTURY(ChronoUnit.CENTURIES, TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR, "centuries"),
    /**
     * 1000 years.
     */
    MILLENIUM(ChronoUnit.MILLENNIA, 10L * TypeUtils.HOURS_PER_CENTURY * TypeUtils.MILLIS_PER_HOUR, "millennia");

    /**
     * The last day of the period of the given number of years, starting with a year divisible by that number,
     * that contains the temporal.
     */
    private static Temporal lastDayOfPeriod(final Temporal temporal, final long years) {
        long lastYear = Math.floorDiv(temporal.getLong(ChronoField.YEAR), years) * years + years - 1L;
        return temporal.with(ChronoField.YEAR, lastYear).with(TemporalAdjusters.lastDayOfYear());
    }

    private final ChronoUnit myChronoUnit;
    private final long myDurationInMillis;
    private final long myDurationInNanos;
    private final String myLabel;
    private final TimeUnit myTimeUnit;

    CalendarDateUnit(final ChronoUnit chronoUnit, final long millis, final String label) {
        myChronoUnit = chronoUnit;
        myTimeUnit = null;
        myDurationInMillis = millis;
        myDurationInNanos = TimeUnit.MILLISECONDS.toNanos(millis);
        myLabel = label;
    }

    CalendarDateUnit(final ChronoUnit chronoUnit, final TimeUnit timeUnit, final String label) {
        myChronoUnit = chronoUnit;
        myTimeUnit = timeUnit;
        myDurationInMillis = timeUnit.toMillis(1L);
        myDurationInNanos = timeUnit.toNanos(1L);
        myLabel = label;
    }

    /**
     * On a {@link CalendarDate} adds amount times the fixed length, rounded down to whole milliseconds for
     * {@link #NANOS} and {@link #MICROS}. On other temporals adds the corresponding {@link ChronoUnit}.
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public <R extends Temporal> R addTo(final R temporal, final long amount) {
        if (temporal instanceof CalendarDate) {
            return (R) new CalendarDate(this.addTo(((CalendarDate) temporal).millis, amount));
        } else if (myChronoUnit != null) {
            return myChronoUnit.addTo(temporal, amount);
        } else { // QUARTER
            return ChronoUnit.MONTHS.addTo(temporal, 3L * amount);
        }
    }

    /**
     * Maps the temporal to a single representative point in time of the period, of this unit, that contains
     * it.
     * <ul>
     * <li>{@link CalendarDate}: the middle of a fixed-length interval, see {@link #adjustInto(long)}.
     * {@link Instant} is handled the same way, at millisecond precision.
     * <li>Other temporals: units shorter than a day truncate the time of day to the start of the unit (NANOS,
     * MICROS and MILLIS change nothing). DAY and longer set the time of day to noon, and WEEK and longer also
     * set the date to the last day of the period: the Friday ending a Saturday to Friday week, or the last
     * day of the month, quarter, year, decade, century or millennium. Decades, centuries and millennia start
     * with years divisible by 10, 100 and 1000. Temporals without a time of day, such as
     * {@link java.time.LocalDate}, only have their date adjusted.
     * </ul>
     */
    @Override
    public Temporal adjustInto(final Temporal temporal) {

        if (temporal instanceof CalendarDate) {

            return this.adjustInto((CalendarDate) temporal);

        } else if (temporal instanceof Instant) {

            return Instant.ofEpochMilli(this.adjustInto(((Instant) temporal).toEpochMilli()));

        } else {

            Temporal retVal = temporal;

            if (retVal.isSupported(ChronoField.NANO_OF_DAY)) {
                if (CalendarDateUnit.HOUR.toDurationInMillis() < myDurationInMillis) {
                    retVal = retVal.with(LocalTime.NOON);
                } else if (CalendarDateUnit.MILLIS.toDurationInMillis() < myDurationInMillis) {
                    long nanoOfDay = retVal.getLong(ChronoField.NANO_OF_DAY);
                    retVal = retVal.with(ChronoField.NANO_OF_DAY, nanoOfDay - nanoOfDay % myDurationInNanos);
                }
            }

            return switch (this) {
                case WEEK      -> retVal.minus(2L, ChronoUnit.DAYS).with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
                case MONTH     -> retVal.with(TemporalAdjusters.lastDayOfMonth());
                case QUARTER   -> {
                    int lastMonth = 3 + 3 * ((retVal.get(ChronoField.MONTH_OF_YEAR) - 1) / 3);
                    yield retVal.with(ChronoField.MONTH_OF_YEAR, lastMonth).with(TemporalAdjusters.lastDayOfMonth());
                }
                case YEAR      -> retVal.with(TemporalAdjusters.lastDayOfYear());
                case DECADE    -> CalendarDateUnit.lastDayOfPeriod(retVal, 10L);
                case CENTURY   -> CalendarDateUnit.lastDayOfPeriod(retVal, 100L);
                case MILLENIUM -> CalendarDateUnit.lastDayOfPeriod(retVal, 1000L);
                default        -> retVal;
            };
        }
    }

    /**
     * The number of complete units between the two temporals, negative if the second is before the first.
     * With a {@link CalendarDate} as the first argument the fixed length is used, otherwise the corresponding
     * {@link ChronoUnit}.
     */
    @Override
    public long between(final Temporal temporal1Inclusive, final Temporal temporal2Exclusive) {
        if (temporal1Inclusive instanceof CalendarDate) {
            return this.between(((CalendarDate) temporal1Inclusive).millis, CalendarDate.millis(temporal2Exclusive));
        } else if (myChronoUnit != null) {
            return myChronoUnit.between(temporal1Inclusive, temporal2Exclusive);
        } else { // QUARTER
            return ChronoUnit.MONTHS.between(temporal1Inclusive, temporal2Exclusive) / 3L;
        }
    }

    /**
     * Same as {@link CalendarDateDuration#convertTo(CalendarDateUnit)}.
     */
    public CalendarDateDuration convert(final CalendarDateDuration sourceDuration) {
        return sourceDuration.convertTo(this);
    }

    /**
     * The number of this unit in one source unit. For example {@code HOUR.convert(DAY)} is 24.0.
     */
    public double convert(final CalendarDateUnit aSourceDurationUnit) {
        return this.convert(PrimitiveMath.ONE, aSourceDurationUnit);
    }

    /**
     * Converts a measure of the source unit to this unit, using the fixed lengths.
     */
    public double convert(final double sourceDurationMeasure, final CalendarDateUnit sourceDurationUnit) {

        double sourceDurationInNanos = sourceDurationUnit.toDurationInNanosAsDouble();
        double destinationDurationInNanos = this.toDurationInNanosAsDouble();

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

    /**
     * Converts a measure of the source unit to this unit. Between units that have a {@link TimeUnit},
     * {@link #NANOS} to {@link #DAY}, the result is truncated and saturated as by
     * {@link TimeUnit#convert(long, TimeUnit)}. Otherwise it is rounded.
     */
    public long convert(final long sourceMeassure, final CalendarDateUnit sourceUnit) {
        Optional<TimeUnit> tmpTimeUnit = sourceUnit.getTimeUnit();
        if (myTimeUnit != null && tmpTimeUnit.isPresent()) {
            return myTimeUnit.convert(sourceMeassure, tmpTimeUnit.get());
        } else {
            return Math.round(this.convert((double) sourceMeassure, sourceUnit));
        }
    }

    /**
     * The length in milliseconds if the argument is this unit, otherwise 0.
     */
    public long get(final TemporalUnit unit) {
        if (unit == this) {
            return myDurationInMillis;
        } else {
            return 0;
        }
    }

    /**
     * The corresponding {@link ChronoUnit}, empty for {@link #QUARTER}.
     */
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

    /**
     * A short label such as "ms", "h" or "days", used by {@link CalendarDateDuration#toString()}.
     */
    public String getLabel() {
        return myLabel;
    }

    /**
     * The corresponding {@link TimeUnit}, present for {@link #NANOS} to {@link #DAY}.
     */
    public Optional<TimeUnit> getTimeUnit() {
        return Optional.ofNullable(myTimeUnit);
    }

    /**
     * A list containing only this unit.
     */
    public List<TemporalUnit> getUnits() {
        return Collections.singletonList(this);
    }

    /**
     * Whether this is {@link #DAY} or longer.
     */
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

    /**
     * A duration of the measure in this unit.
     */
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

    /**
     * The duration in nanoseconds, not saturated like {@link #toDurationInNanos()}.
     */
    private double toDurationInNanosAsDouble() {
        if (myDurationInMillis == 0L) {
            return myDurationInNanos;
        } else {
            return myDurationInMillis * (double) CalendarDate.NANOS_PER_MILLIS;
        }
    }

}
