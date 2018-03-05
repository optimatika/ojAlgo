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

import org.ojalgo.access.Access1D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.ParameterFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;

public final class DataSeries extends PrimitiveSeries {

    public static DataSeries copy(final Access1D<?> template) {
        return new DataSeries(Array1D.PRIMITIVE64.copy(template));
    }

    public static DataSeries copy(final double[] template) {
        return new DataSeries(Array1D.PRIMITIVE64.copy(template));
    }

    public static DataSeries wrap(final double[] raw) {
        return new DataSeries(Array1D.PRIMITIVE64.wrap(Primitive64Array.wrap(raw)));
    }

    private final Array1D<Double> myValues;

    private DataSeries(final Array1D<Double> values) {

        super();

        myValues = values;
    }

    public final void modify(final BinaryFunction<Double> func, final double right) {
        myValues.modifyAll(func.second(right));
    }

    public final void modify(final double left, final BinaryFunction<Double> func) {
        myValues.modifyAll(func.first(left));
    }

    public final void modify(final ParameterFunction<Double> func, final int param) {
        myValues.modifyAll(func.parameter(param));
    }

    public final void modify(final UnaryFunction<Double> func) {
        myValues.modifyAll(func);
    }

    @Override
    public final int size() {
        return myValues.size();
    }

    @Override
    public final double value(final int index) {
        return myValues.doubleValue(index);
    }

    public final void visit(final AggregatorFunction<Double> visitor) {
        myValues.visitAll(visitor);
    }

}
