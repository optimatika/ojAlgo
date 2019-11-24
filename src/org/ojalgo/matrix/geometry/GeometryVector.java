/*
 * Copyright 1997-2019 Optimatika
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

import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Structure1D;
import org.ojalgo.structure.Structure2D;

abstract class GeometryVector extends TransformableRegion.ReceiverRegion<Double> {

    GeometryVector(final FillByMultiplying<Double> multiplier, final long rows, final long columns) {
        super(multiplier, rows, columns);
    }

    public abstract void add(int row, double addend);

    public final void add(final long index, final Comparable<?> addend) {
        this.add(Structure1D.index(index), Scalar.doubleValue(addend));
    }

    public final void add(final long index, final double addend) {
        this.add(Structure1D.index(index), addend);
    }

    public final void add(final long row, final long col, final Comparable<?> addend) {
        this.add(Structure2D.index(this.structure(), row, col), Scalar.doubleValue(addend));
    }

    public final void add(final long row, final long col, final double addend) {
        this.add(Structure2D.index(this.structure(), row, col), addend);
    }

    public final long count() {
        return this.structure();
    }

    public final long countColumns() {
        return 1L;
    }

    public final long countRows() {
        return this.count();
    }

    public abstract double doubleValue(int index);

    public final double doubleValue(final long index) {
        return this.doubleValue(Structure1D.index(index));
    }

    public final double doubleValue(final long row, final long col) {
        return this.doubleValue(Structure2D.index(this.structure(), row, col));
    }

    public final void fillOne(final long index, final Access1D<?> values, final long valueIndex) {
        this.set(Structure1D.index(index), values.doubleValue(valueIndex));
    }

    public final void fillOne(final long index, final Double value) {
        this.set(Structure1D.index(index), value.doubleValue());
    }

    public final void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
        this.set(Structure2D.index(this.structure(), row, col), values.doubleValue(valueIndex));
    }

    public final void fillOne(final long row, final long col, final Double value) {
        this.set(Structure2D.index(this.structure(), row, col), value.doubleValue());
    }

    public final void fillOne(final long row, final long col, final NullaryFunction<?> supplier) {
        this.set(Structure2D.index(this.structure(), row, col), supplier.doubleValue());
    }

    public final void fillOne(final long index, final NullaryFunction<?> supplier) {
        this.set(Structure1D.index(index), supplier.doubleValue());
    }

    public final Double get(final long index) {
        return this.doubleValue(Structure1D.index(index));
    }

    public final Double get(final long row, final long col) {
        return this.get(Structure2D.index(this.structure(), row, col));
    }

    public abstract void modifyOne(int row, UnaryFunction<Double> modifier);

    public final void modifyOne(final long row, final long col, final UnaryFunction<Double> modifier) {
        this.modifyOne(Structure2D.index(this.structure(), row, col), modifier);
    }

    public final void modifyOne(final long index, final UnaryFunction<Double> modifier) {
        this.modifyOne(Structure1D.index(index), modifier);
    }

    public abstract void set(int row, double value);

    public final void set(final long index, final Comparable<?> value) {
        this.set(Structure1D.index(index), Scalar.doubleValue(value));
    }

    public final void set(final long index, final double addend) {
        this.set(Structure1D.index(index), addend);
    }

    public final void set(final long row, final long col, final Comparable<?> value) {
        this.set(Structure2D.index(this.structure(), row, col), Scalar.doubleValue(value));
    }

    public final void set(final long row, final long col, final double value) {
        this.set(Structure2D.index(this.structure(), row, col), value);
    }

    public final int size() {
        return this.structure();
    }

    @Override
    public final String toString() {
        return Access1D.toString(this);
    }

    abstract int structure();

}
