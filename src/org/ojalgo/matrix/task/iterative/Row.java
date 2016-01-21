/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
package org.ojalgo.matrix.task.iterative;

import static org.ojalgo.constant.PrimitiveMath.*;

import org.ojalgo.array.SparseArray;
import org.ojalgo.matrix.store.PhysicalStore;

public final class Row implements Comparable<Row> {

    /**
     * The row index of the original body matrix, [A].
     */
    public final int index;
    /**
     * The nonzero elements of this row
     */
    private final SparseArray<Double> myElements;
    private double myPivot = ZERO;

    public Row(final int row, final long numberOfColumns) {
        super();
        index = row;
        myElements = SparseArray.makePrimitive(numberOfColumns);
    }

    public int compareTo(final Row other) {
        return Integer.compare(index, other.index);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Row)) {
            return false;
        }
        final Row other = (Row) obj;
        if (index != other.index) {
            return false;
        }
        return true;
    }

    public double getPivot() {
        return myPivot;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + index;
        return result;
    }

    public void set(final long index, final double value) {
        myElements.set(index, value);
        if (index == this.index) {
            myPivot = value;
        }
    }

    public void solve(final PhysicalStore<Double> x, final double rhs, final double relaxation) {

        double tmpIncrement = rhs;

        tmpIncrement -= myElements.dot(x);

        tmpIncrement *= relaxation;

        tmpIncrement /= myPivot;

        x.add(index, tmpIncrement);
    }

    @Override
    public String toString() {
        return index + ": " + myElements.toString();
    }

    SparseArray<Double> getElements() {
        return myElements;
    }

}
