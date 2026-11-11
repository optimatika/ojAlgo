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

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Collections;
import java.util.List;

import org.ojalgo.function.constant.PrimitiveMath;

/**
 * A measure, possibly fractional, of a {@link CalendarDateUnit}, such as 1.5 hours. Similar to
 * {@link Duration} and {@link Period}, but with a single unit and a double measure.
 * <ul>
 * <li>On a {@link CalendarDate} its length, rounded to whole milliseconds, is added or subtracted.
 * <li>On other temporals, and as a {@link TemporalAmount} ({@link #get(TemporalUnit)}), it is the measure
 * rounded to a whole number of the unit.
 * <li>As a {@link CalendarDate.Resolution} it divides the time-line into intervals of its length, also
 * working directly on epoch-millisecond longs.
 * <li>As a number it is the measure.
 * </ul>
 * Instances are compared by length, which is not consistent with equals: 1 hour and 60 minutes compare as
 * equal but are not equal.
 *
 * @see CalendarDate
 * @see CalendarDateUnit
 * @author apete
 */
public final class CalendarDateDuration implements TemporalAmount, CalendarDate.Resolution, ComparableNumber<CalendarDateDuration> {

    /**
     * The duration as a whole number of the largest unit, from {@link CalendarDateUnit#DAY} down to
     * {@link CalendarDateUnit#NANOS}, that it is a multiple of. For example 15 minutes is 15
     * {@link CalendarDateUnit#MINUTE} and 90 seconds is 90 {@link CalendarDateUnit#SECOND}.
     */
    public static CalendarDateDuration valueOf(final Duration duration) {
        for (CalendarDateUnit unit : new CalendarDateUnit[] { CalendarDateUnit.DAY, CalendarDateUnit.HOUR, CalendarDateUnit.MINUTE, CalendarDateUnit.SECOND,
                CalendarDateUnit.MILLIS, CalendarDateUnit.MICROS }) {
            Duration length = unit.getDuration();
            long count = duration.dividedBy(length);
            if (length.multipliedBy(count).equals(duration)) {
                return new CalendarDateDuration(count, unit);
            }
        }
        return new CalendarDateDuration(duration.getSeconds() * (double) CalendarDate.NANOS_PER_SECOND + duration.getNano(), CalendarDateUnit.NANOS);
    }

    static CalendarDateDuration of(final long nanos) {
        return new CalendarDateDuration(nanos, CalendarDateUnit.NANOS);
    }

    /**
     * The measure, in {@link #unit}.
     */
    public final double measure;
    /**
     * The unit of {@link #measure}.
     */
    public final CalendarDateUnit unit;

    public CalendarDateDuration(final double aMeasure, final CalendarDateUnit aUnit) {

        super();

        measure = aMeasure;
        unit = aUnit;
    }

    CalendarDateDuration() {
        this(PrimitiveMath.ZERO, CalendarDateUnit.MILLIS);
    }

    @Override
    public Temporal addTo(final Temporal temporal) {
        if (temporal instanceof CalendarDate) {
            return new CalendarDate(((CalendarDate) temporal).millis + this.toDurationInMillis());
        } else {
            return unit.addTo(temporal, Math.round(measure));
        }
    }

    /**
     * Maps a {@link CalendarDate} to the middle of a fixed-length interval, see
     * {@link CalendarDate.Resolution#adjustInto(long)}. {@link Instant} is handled the same way, at
     * millisecond precision. Other temporals are not supported.
     */
    @Override
    public Temporal adjustInto(final Temporal temporal) {
        if (temporal instanceof CalendarDate) {
            return this.adjustInto((CalendarDate) temporal);
        } else if (temporal instanceof Instant) {
            return Instant.ofEpochMilli(this.adjustInto(((Instant) temporal).toEpochMilli()));
        } else {
            throw new UnsupportedTemporalTypeException("Unsupported temporal: " + temporal.getClass().getName());
        }
    }

    @Override
    public int compareTo(final CalendarDateDuration reference) {
        double tmpVal = CalendarDateUnit.NANOS.convert(measure, unit);
        double refVal = CalendarDateUnit.NANOS.convert(reference.measure, reference.unit);
        return Double.compare(tmpVal, refVal);
    }

    /**
     * The same length in another unit.
     */
    public CalendarDateDuration convertTo(final CalendarDateUnit newUnit) {
        double newMeasure = newUnit.convert(measure, unit);
        return new CalendarDateDuration(newMeasure, newUnit);
    }

    @Override
    public double doubleValue() {
        return measure;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || !(obj instanceof CalendarDateDuration other) || (Double.doubleToLongBits(measure) != Double.doubleToLongBits(other.measure))
                || (unit != other.unit)) {
            return false;
        }
        return true;
    }

    @Override
    public float floatValue() {
        return (float) measure;
    }

    @Override
    public long get(final TemporalUnit unit) {
        if (unit == this.unit) {
            return Math.round(measure);
        } else {
            throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return Collections.singletonList(unit);
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        long temp = Double.doubleToLongBits(measure);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        return (prime * result) + ((unit == null) ? 0 : unit.hashCode());
    }

    @Override
    public int intValue() {
        return (int) measure;
    }

    @Override
    public long longValue() {
        return (long) measure;
    }

    @Override
    public Temporal subtractFrom(final Temporal temporal) {
        if (temporal instanceof CalendarDate) {
            return new CalendarDate(((CalendarDate) temporal).millis - this.toDurationInMillis());
        } else {
            return unit.addTo(temporal, -Math.round(measure));
        }
    }

    /**
     * The length as a {@link Duration}, rounded to whole nanoseconds. From {@link CalendarDateUnit#MONTH} up
     * the units have their average lengths.
     */
    public Duration toDuration() {
        double seconds = CalendarDateUnit.SECOND.convert(measure, unit);
        double wholeSeconds = Math.floor(seconds);
        return Duration.ofSeconds((long) wholeSeconds, Math.round((seconds - wholeSeconds) * CalendarDate.NANOS_PER_SECOND));
    }

    /**
     * The length rounded to whole milliseconds.
     */
    @Override
    public long toDurationInMillis() {
        return Math.round(CalendarDateUnit.MILLIS.convert(measure, unit));
    }

    @Override
    public long toDurationInNanos() {
        return Math.round(CalendarDateUnit.NANOS.convert(measure, unit));
    }

    /**
     * The measure followed by the unit's label, such as "1.5h".
     */
    @Override
    public String toString() {
        return Double.toString(measure) + unit.getLabel();
    }

}
