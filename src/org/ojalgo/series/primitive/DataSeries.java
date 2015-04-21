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

import org.ojalgo.access.Access1D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.PrimitiveArray;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.ParameterFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;

public final class DataSeries extends PrimitiveSeries {

    public static DataSeries copy(final Access1D<?> aBase) {
        return new DataSeries(Array1D.PRIMITIVE.copy(aBase));
    }

    public static DataSeries copy(final double[] aRaw) {
        return new DataSeries(Array1D.PRIMITIVE.copy(aRaw));
    }

    public static DataSeries wrap(final double[] aRaw) {
        return new DataSeries(Array1D.PRIMITIVE.wrap(PrimitiveArray.wrap(aRaw)));
    }

    private final Array1D<Double> myValues;

    private DataSeries(final Array1D<Double> aValues) {

        super();

        myValues = aValues;
    }

    public final void modify(final BinaryFunction<Double> aFunc, final Double aNmbr) {
        myValues.modifyAll(aFunc.second(aNmbr));
    }

    public final void modify(final Double aNmbr, final BinaryFunction<Double> aFunc) {
        myValues.modifyAll(aFunc.first(aNmbr));
    }

    public final void modify(final ParameterFunction<Double> aFunc, final int aParam) {
        myValues.modifyAll(aFunc.parameter(aParam));
    }

    public final void modify(final UnaryFunction<Double> aFunc) {
        myValues.modifyAll(aFunc);
    }

    public final int size() {
        return myValues.size();
    }

    @Override
    public final double value(final int index) {
        return myValues.doubleValue(index);
    }

    public final void visit(final AggregatorFunction<Double> aVisitor) {
        myValues.visitAll(aVisitor);
    }

}
