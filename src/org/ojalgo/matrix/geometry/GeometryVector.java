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
package org.ojalgo.matrix.geometry;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.store.ElementsConsumer;
import org.ojalgo.matrix.transformation.TransformationMatrix;

abstract class GeometryVector extends ElementsConsumer.ConsumerRegion<Double> implements Access1D<Double>, TransformationMatrix.Transformable<Double> {

    GeometryVector(final FillByMultiplying<Double> multiplier, final long rows, final long columns) {
        super(multiplier, rows, columns);
    }

    public abstract void add(int row, double addend);

    public final void add(final long row, final long col, final double addend) {
        this.add((int) row, addend);
    }

    public final void add(final long row, final long col, final Number addend) {
        this.add((int) row, addend.doubleValue());
    }

    public final long countColumns() {
        return 1L;
    }

    public final long countRows() {
        return this.count();
    }

    public abstract double doubleValue(int index);

    public final double doubleValue(final long index) {
        return this.doubleValue((int) index);
    }

    public final void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
        this.set((int) row, values.doubleValue(valueIndex));
    }

    public final void fillOne(final long row, final long col, final Double value) {
        this.set((int) row, value.doubleValue());
    }

    public final void fillOne(final long row, final long col, final NullaryFunction<Double> supplier) {
        this.set((int) row, supplier.doubleValue());
    }

    public final Double get(final long index) {
        return this.doubleValue((int) index);
    }

    public abstract void modifyOne(int row, UnaryFunction<Double> modifier);

    public final void modifyOne(final long row, final long col, final UnaryFunction<Double> modifier) {
        this.modifyOne((int) row, modifier);
    }

    public abstract void set(int row, double value);

    public final void set(final long row, final long col, final double value) {
        this.set((int) row, value);
    }

    public final void set(final long row, final long col, final Number value) {
        this.set((int) row, value.doubleValue());
    }

}
