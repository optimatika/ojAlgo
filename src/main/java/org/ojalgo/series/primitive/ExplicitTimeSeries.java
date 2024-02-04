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
package org.ojalgo.series.primitive;

import org.ojalgo.type.CalendarDate;

public final class ExplicitTimeSeries extends PrimitiveTimeSeries {

    private final long[] myTimes;

    public ExplicitTimeSeries(final long[] someTimes, final PrimitiveSeries valueSeries) {

        super(valueSeries);

        myTimes = someTimes;
    }

    @Override
    public ExplicitTimeSeries add(final double addend) {
        return new ExplicitTimeSeries(this.keys(), super.add(addend));
    }

    @Override
    public ExplicitTimeSeries add(final PrimitiveSeries addend) {
        return new ExplicitTimeSeries(this.keys(), super.add(addend));
    }

    @Override
    public ExplicitTimeSeries copy() {
        return new ExplicitTimeSeries(this.keys(), super.copy());
    }

    @Override
    public ExplicitTimeSeries differences() {
        return new ExplicitTimeSeries(this.keys(), super.differences());
    }

    @Override
    public ExplicitTimeSeries differences(final int period) {
        return new ExplicitTimeSeries(this.keys(), super.differences(period));
    }

    @Override
    public ExplicitTimeSeries divide(final double divisor) {
        return new ExplicitTimeSeries(this.keys(), super.divide(divisor));
    }

    @Override
    public ExplicitTimeSeries divide(final PrimitiveSeries divisor) {
        return new ExplicitTimeSeries(this.keys(), super.divide(divisor));
    }

    @Override
    public PrimitiveSeries exp() {
        return new ExplicitTimeSeries(this.keys(), super.exp());
    }

    @Override
    public CalendarDate first() {
        return new CalendarDate(myTimes[0]);
    }

    @Override
    public long getAverageStepSize() {
        final int tmpIndexOfLast = myTimes.length - 1;
        return (myTimes[tmpIndexOfLast] - myTimes[0]) / tmpIndexOfLast;
    }

    public long key(final int index) {
        return myTimes[index];
    }

    @Override
    public long[] keys() {
        return myTimes;
    }

    @Override
    public CalendarDate last() {
        return new CalendarDate(myTimes[myTimes.length - 1]);
    }

    @Override
    public PrimitiveSeries log() {
        return new ExplicitTimeSeries(this.keys(), super.log());
    }

    @Override
    public ExplicitTimeSeries multiply(final double aFactor) {
        return new ExplicitTimeSeries(this.keys(), super.multiply(aFactor));
    }

    @Override
    public ExplicitTimeSeries multiply(final PrimitiveSeries multiplicand) {
        return new ExplicitTimeSeries(this.keys(), super.multiply(multiplicand));
    }

    @Override
    public ExplicitTimeSeries quotients() {
        return new ExplicitTimeSeries(this.keys(), super.quotients());
    }

    @Override
    public ExplicitTimeSeries quotients(final int period) {
        return new ExplicitTimeSeries(this.keys(), super.quotients(period));
    }

    @Override
    public ExplicitTimeSeries runningProduct(final double initialValue) {
        return new ExplicitTimeSeries(this.keys(), super.runningProduct(initialValue));
    }

    @Override
    public ExplicitTimeSeries runningSum(final double initialValue) {
        return new ExplicitTimeSeries(this.keys(), super.runningSum(initialValue));
    }

    @Override
    public ExplicitTimeSeries subtract(final double subtrahend) {
        return new ExplicitTimeSeries(this.keys(), super.subtract(subtrahend));
    }

    @Override
    public ExplicitTimeSeries subtract(final PrimitiveSeries subtrahend) {
        return new ExplicitTimeSeries(this.keys(), super.subtract(subtrahend));
    }

}
