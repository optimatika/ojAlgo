/*
 * Copyright 1997-2022 Optimatika
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
package org.ojalgo.optimisation.linear;

import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.type.NumberDefinition;

abstract class Primitive2D implements Access2D<Double>, Mutate2D {

    public final long countColumns() {
        return this.getColDim();
    }

    public final long countRows() {
        return this.getRowDim();
    }

    public final double doubleValue(final long row, final long col) {
        return this.doubleValue(Math.toIntExact(row), Math.toIntExact(col));
    }

    public final Double get(final long row, final long col) {
        return Double.valueOf(this.doubleValue(Math.toIntExact(row), Math.toIntExact(col)));
    }

    public abstract int getColDim();

    public abstract int getRowDim();

    public final void set(final long row, final long col, final Comparable<?> value) {
        this.set(Math.toIntExact(row), Math.toIntExact(col), NumberDefinition.doubleValue(value));
    }

    public final void set(final long row, final long col, final double value) {
        this.set(Math.toIntExact(row), Math.toIntExact(col), value);
    }

    @Override
    public final String toString() {
        return Access2D.toString(this);
    }

    abstract double doubleValue(final int row, final int col);

    abstract void set(final int row, final int col, final double value);
}
