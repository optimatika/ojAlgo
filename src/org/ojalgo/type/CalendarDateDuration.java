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
import java.time.Period;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.ojalgo.constant.PrimitiveMath;

/**
 * <p>
 * Designed to complement {@linkplain CalendarDate}. It is similar to {@linkplain Duration} or
 * {@linkplain Period}, but supports a decimal/fractional measure. It has been retrofitted to implement the
 * {@linkplain TemporalAmount} interface.
 * </p>
 *
 * @see CalendarDate
 * @see CalendarDateUnit
 * @author apete
 */
public final class CalendarDateDuration extends Number implements TemporalAmount, CalendarDate.Resolution, Comparable<CalendarDateDuration>, Serializable {

    public final double measure;
    public final CalendarDateUnit unit;

    public CalendarDateDuration(final double aMeasure, final CalendarDateUnit aUnit) {

        super();

        measure = aMeasure;
        unit = aUnit;
    }

    CalendarDateDuration() {
        this(PrimitiveMath.ZERO, CalendarDateUnit.MILLIS);
    }

    public Temporal addTo(final Temporal temporal) {
        return temporal.plus(this.toDurationInMillis(), CalendarDateUnit.MILLIS);
    }

    public CalendarDate adjustInto(final Calendar temporal) {
        // TODO Auto-generated method stub
        return null;
    }

    public CalendarDate adjustInto(final Date temporal) {
        // TODO Auto-generated method stub
        return null;
    }

    public CalendarDate adjustInto(final Temporal temporal) {
        // TODO Auto-generated method stub
        return null;
    }

    public int compareTo(final CalendarDateDuration reference) {
        final long tmpVal = this.toDurationInMillis();
        final long refVal = reference.toDurationInMillis();
        return Long.signum(tmpVal - refVal);
    }

    public CalendarDateDuration convertTo(final CalendarDateUnit newUnit) {
        final double newMeasure = newUnit.convert(measure, unit);
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
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CalendarDateDuration)) {
            return false;
        }
        final CalendarDateDuration other = (CalendarDateDuration) obj;
        if (Double.doubleToLongBits(measure) != Double.doubleToLongBits(other.measure)) {
            return false;
        }
        if (unit != other.unit) {
            return false;
        }
        return true;
    }

    @Override
    public float floatValue() {
        return (float) measure;
    }

    public long get(final TemporalUnit unit) {
        if (unit == this.unit) {
            return (long) measure;
        } else {
            return 0L;
        }
    }

    public List<TemporalUnit> getUnits() {
        return Collections.singletonList(unit);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(measure);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        result = (prime * result) + ((unit == null) ? 0 : unit.hashCode());
        return result;
    }

    @Override
    public int intValue() {
        return (int) measure;
    }

    @Override
    public long longValue() {
        return (long) measure;
    }

    public Temporal subtractFrom(final Temporal temporal) {
        return temporal.minus(this.toDurationInMillis(), CalendarDateUnit.MILLIS);
    }

    public long toDurationInMillis() {
        return (long) (measure * unit.size());
    }

    public long toDurationInNanos() {
        return (long) (measure * (1_000_000L * unit.size()));
    }

    @Override
    public String toString() {
        return Double.toString(measure) + unit.getLabel();
    }

}
