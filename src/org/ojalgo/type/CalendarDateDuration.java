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

import java.io.Serializable;
import java.time.Duration;
import java.time.Period;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.Collections;
import java.util.List;

import org.ojalgo.constant.PrimitiveMath;

/**
 * <p>
 * Designed to complement {@linkplain CalendarDate}. It is similar to
 * {@linkplain Duration} or {@linkplain Period}, but supports a
 * decimal/fractional measure. It has been retrofitted to implement the
 * {@linkplain TemporalAmount} interface.
 * </p>
 *
 * @see CalendarDate
 * @see CalendarDateUnit
 * @author apete
 */
public final class CalendarDateDuration extends Number
        implements TemporalAmount, Comparable<CalendarDateDuration>, Serializable {

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

    public int compareTo(final CalendarDateDuration reference) {
        final long tmpVal = this.toDurationInMillis();
        final long refVal = reference.toDurationInMillis();
        return Long.signum(tmpVal - refVal);
    }

    /**
     * @deprecated v40 Use something in {@linkplain CalendarDateUnit} instead
     */
    @Deprecated
    public CalendarDateDuration convertTo(final CalendarDateUnit destinationUnit) {
        return new CalendarDateDuration(destinationUnit.convert(measure, unit), destinationUnit);
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

    @Override
    public String toString() {
        return Double.toString(measure) + unit.toString();
    }

    long toDurationInMillis() {
        return (long) (measure * unit.size());
    }

}
