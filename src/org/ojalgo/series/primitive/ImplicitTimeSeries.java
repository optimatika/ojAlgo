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
package org.ojalgo.series.primitive;

import java.util.Calendar;
import java.util.Date;

import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.CalendarDateUnit;

public final class ImplicitTimeSeries extends PrimitiveTimeSeries {

    private final CalendarDateUnit myResolution;
    private final CalendarDate myFirst;

    public ImplicitTimeSeries(final Calendar aFirst, final CalendarDateUnit aResolution, final PrimitiveSeries aValueSeries) {

        super(aValueSeries);

        myFirst = CalendarDate.make(aFirst, aResolution);
        myResolution = aResolution;
    }

    public ImplicitTimeSeries(final CalendarDate aFirst, final CalendarDateUnit aResolution, final PrimitiveSeries aValueSeries) {

        super(aValueSeries);

        myFirst = aFirst.filter(aResolution);
        myResolution = aResolution;
    }

    public ImplicitTimeSeries(final Date aFirst, final CalendarDateUnit aResolution, final PrimitiveSeries aValueSeries) {

        super(aValueSeries);

        myFirst = CalendarDate.make(aFirst, aResolution);
        myResolution = aResolution;
    }

    @Override
    public ImplicitTimeSeries add(final double addend) {
        return new ImplicitTimeSeries(this.first(), this.resolution(), super.add(addend));
    }

    @Override
    public final ImplicitTimeSeries add(final PrimitiveSeries addend) {
        return new ImplicitTimeSeries(this.first(), this.resolution(), super.add(addend));
    }

    @Override
    public ImplicitTimeSeries copy() {
        return new ImplicitTimeSeries(this.first(), this.resolution(), super.copy());
    }

    @Override
    public final ImplicitTimeSeries differences() {
        return new ImplicitTimeSeries(this.first(), this.resolution(), super.differences());
    }

    @Override
    public final ImplicitTimeSeries differences(final int period) {
        return new ImplicitTimeSeries(this.first(), this.resolution(), super.differences(period));
    }

    @Override
    public ImplicitTimeSeries divide(final double divisor) {
        return new ImplicitTimeSeries(this.first(), this.resolution(), super.divide(divisor));
    }

    @Override
    public final ImplicitTimeSeries divide(final PrimitiveSeries divisor) {
        return new ImplicitTimeSeries(this.first(), this.resolution(), super.divide(divisor));
    }

    @Override
    public PrimitiveSeries exp() {
        return new ImplicitTimeSeries(this.first(), this.resolution(), super.exp());
    }

    @Override
    public final CalendarDate first() {
        return myFirst;
    }

    @Override
    public long getAverageStepSize() {
        return myResolution.size();
    }

    @Override
    public final long[] keys() {

        final long[] retVal = new long[this.size()];

        CalendarDate tmpKey = myFirst;
        retVal[0] = tmpKey.millis;
        for (int t = 1; t < retVal.length; t++) {
            tmpKey = tmpKey.step(myResolution);
            retVal[t] = tmpKey.millis;
        }

        return retVal;
    }

    @Override
    public final CalendarDate last() {
        return myFirst.step(this.size() - 1, myResolution);
    }

    @Override
    public PrimitiveSeries log() {
        return new ImplicitTimeSeries(this.first(), this.resolution(), super.log());
    }

    @Override
    public final ImplicitTimeSeries multiply(final double aFactor) {
        return new ImplicitTimeSeries(this.first(), this.resolution(), super.multiply(aFactor));
    }

    @Override
    public ImplicitTimeSeries multiply(final PrimitiveSeries multiplicand) {
        return new ImplicitTimeSeries(this.first(), this.resolution(), super.multiply(multiplicand));
    }

    @Override
    public final ImplicitTimeSeries quotients() {
        return new ImplicitTimeSeries(this.first(), this.resolution(), super.quotients());
    }

    @Override
    public final ImplicitTimeSeries quotients(final int period) {
        return new ImplicitTimeSeries(this.first(), this.resolution(), super.quotients(period));
    }

    public final CalendarDateUnit resolution() {
        return myResolution;
    }

    @Override
    public final ImplicitTimeSeries runningProduct(final double initialValue) {
        return new ImplicitTimeSeries(this.first(), this.resolution(), super.runningProduct(initialValue));
    }

    @Override
    public final ImplicitTimeSeries runningSum(final double initialValue) {
        return new ImplicitTimeSeries(this.first(), this.resolution(), super.runningSum(initialValue));
    }

    @Override
    public ImplicitTimeSeries subtract(final double subtrahend) {
        return new ImplicitTimeSeries(this.first(), this.resolution(), super.subtract(subtrahend));
    }

    @Override
    public ImplicitTimeSeries subtract(final PrimitiveSeries subtrahend) {
        return new ImplicitTimeSeries(this.first(), this.resolution(), super.subtract(subtrahend));
    }

}
