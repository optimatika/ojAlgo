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
package org.ojalgo.matrix.store;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.DenseArray;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access2D;

abstract class PrimitiveFactory<I extends PhysicalStore<Double>> implements PhysicalStore.Factory<Double, I> {

    public final AggregatorSet<Double> aggregator() {
        return PrimitiveAggregator.getSet();
    }

    public DenseArray.Factory<Double> array() {
        return ArrayR064.FACTORY;
    }

    public final I conjugate(final Access2D<?> source) {
        return this.transpose(source);
    }

    public final FunctionSet<Double> function() {
        return PrimitiveFunction.getSet();
    }

    public Householder<Double> makeHouseholder(final int length) {
        return new Householder.Primitive64(length);
    }

    public final Rotation.Primitive makeRotation(final int low, final int high, final double cos, final double sin) {
        return new Rotation.Primitive(low, high, cos, sin);
    }

    public final Rotation.Primitive makeRotation(final int low, final int high, final Double cos, final Double sin) {
        return this.makeRotation(low, high, cos != null ? cos.doubleValue() : Double.NaN, sin != null ? sin.doubleValue() : Double.NaN);
    }

    public final Scalar.Factory<Double> scalar() {
        return PrimitiveScalar.FACTORY;
    }

}
