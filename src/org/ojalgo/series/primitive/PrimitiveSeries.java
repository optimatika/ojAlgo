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
package org.ojalgo.series.primitive;

import static org.ojalgo.function.PrimitiveFunction.*;

import java.util.Arrays;

import org.ojalgo.access.Access1D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.PrimitiveArray;

public abstract class PrimitiveSeries implements Access1D<Double> {

    public static PrimitiveSeries copy(final Access1D<?> template) {
        return new AccessSeries(Array1D.PRIMITIVE.copy(template));
    }

    public static PrimitiveSeries wrap(final Access1D<?> base) {
        return new AccessSeries(base);
    }

    protected PrimitiveSeries() {
        super();
    }

    public PrimitiveSeries add(final double addend) {
        return new UnaryFunctionSeries(this, ADD.second(addend));
    }

    public PrimitiveSeries add(final PrimitiveSeries addend) {
        return new BinaryFunctionSeries(this, ADD, addend);
    }

    public PrimitiveSeries copy() {
        return this.toDataSeries();
    }

    public long count() {
        return this.size();
    }

    /**
     * period == 1
     */
    public PrimitiveSeries differences() {
        return new DifferencesSeries(this, 1);
    }

    public PrimitiveSeries differences(final int period) {
        return new DifferencesSeries(this, period);
    }

    public PrimitiveSeries divide(final double divisor) {
        return new UnaryFunctionSeries(this, DIVIDE.second(divisor));
    }

    public PrimitiveSeries divide(final PrimitiveSeries divisor) {
        return new BinaryFunctionSeries(this, DIVIDE, divisor);
    }

    public final double doubleValue(final long index) {
        return this.value((int) index);
    }

    public PrimitiveSeries exp() {
        return new UnaryFunctionSeries(this, EXP);
    }

    public final Double get(final int index) {
        return this.value(index);
    }

    public final Double get(final long index) {
        return this.value((int) index);
    }

    public PrimitiveSeries log() {
        return new UnaryFunctionSeries(this, LOG);
    }

    public PrimitiveSeries multiply(final double multiplicand) {
        return new UnaryFunctionSeries(this, MULTIPLY.second(multiplicand));
    }

    public PrimitiveSeries multiply(final PrimitiveSeries multiplicand) {
        return new BinaryFunctionSeries(this, MULTIPLY, multiplicand);
    }

    /**
     * A positive valued shift will prune that many elements off the head of the series. A negative valued
     * shift will prune that many elements off the tail of the series.
     *
     * @param shift
     * @return
     */
    public PrimitiveSeries prune(final int shift) {
        return new PrunedSeries(this, shift);
    }

    /**
     * period == 1
     */
    public PrimitiveSeries quotients() {
        return new QuotientsSeries(this, 1);
    }

    public PrimitiveSeries quotients(final int period) {
        return new QuotientsSeries(this, period);
    }

    public PrimitiveSeries runningProduct(final double initialValue) {

        final int tmpNewSize = this.size() + 1;

        final double[] tmpValues = new double[tmpNewSize];

        double tmpAggrVal = tmpValues[0] = initialValue;
        for (int i = 1; i < tmpNewSize; i++) {
            tmpValues[i] = tmpAggrVal *= this.value(i - 1);
        }

        return DataSeries.wrap(tmpValues);
    }

    public PrimitiveSeries runningSum(final double initialValue) {

        final int tmpNewSize = this.size() + 1;

        final double[] tmpValues = new double[tmpNewSize];

        double tmpAggrVal = tmpValues[0] = initialValue;
        for (int i = 1; i < tmpNewSize; i++) {
            tmpValues[i] = tmpAggrVal += this.value(i - 1);
        }

        return DataSeries.wrap(tmpValues);
    }

    public PrimitiveSeries sample(final int interval) {

        if (interval <= 1) {

            throw new IllegalArgumentException();

        } else {

            final int tmpSampleSize = this.size() / interval;
            final int tmpLastIndex = this.size() - 1;

            final PrimitiveArray tmpValues = PrimitiveArray.make(tmpSampleSize);

            for (int i = 0; i < tmpSampleSize; i++) {
                tmpValues.set(i, tmpLastIndex - (i * interval));
            }

            return PrimitiveSeries.wrap(this);
        }
    }

    public abstract int size();

    public PrimitiveSeries subtract(final double subtrahend) {
        return new UnaryFunctionSeries(this, SUBTRACT.second(subtrahend));
    }

    public PrimitiveSeries subtract(final PrimitiveSeries subtrahend) {
        return new BinaryFunctionSeries(this, SUBTRACT, subtrahend);
    }

    public final DataSeries toDataSeries() {
        return DataSeries.wrap(this.values());
    }

    @Override
    public String toString() {
        return "PrimitiveSeries [values()=" + Arrays.toString(this.values()) + "]";
    }

    public abstract double value(final int index);

    public final double[] values() {

        final int tmpSize = this.size();
        final double[] retVal = new double[tmpSize];

        for (int i = 0; i < tmpSize; i++) {
            retVal[i] = this.value(i);
        }

        return retVal;
    }

}
