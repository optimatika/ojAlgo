/*
 * Copyright 1997-2023 Optimatika
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
package org.ojalgo.matrix.transformation;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.SparseArray;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.structure.Access1D;

public final class ElementaryFactor implements InvertibleFactor<Double> {

    public static ElementaryFactor from(final Access1D<?> values, final int col) {

        SparseArray<Double> sparse = SparseArray.factory(ArrayR064.FACTORY).make(values.count());

        for (int i = 0; i < values.count(); i++) {
            double value = values.doubleValue(i);
            if (value != 0.0) {
                sparse.set(i, value);
            }
        }

        return new ElementaryFactor(sparse, col);
    }

    private final SparseArray<Double> myColumn;
    private final int myIndex;
    private final double myNeagtedDiagonal;

    public ElementaryFactor(final SparseArray<Double> column, final int index) {
        super();
        myColumn = column;
        myIndex = index;
        myNeagtedDiagonal = -myColumn.doubleValue(myIndex);
    }

    public long countColumns() {
        return myColumn.count();
    }

    public long countRows() {
        return myColumn.count();
    }

    public void btran(final PhysicalStore<Double> arg) {

        double f = -arg.doubleValue(myIndex);

        for (NonzeroView<Double> nz : myColumn.nonzeros()) {
            long index = nz.index();
            if (index != myIndex) {
                f += nz.doubleValue() * arg.doubleValue(index);
            }
        }

        if (f != 0.0) {
            f /= myNeagtedDiagonal;
            arg.set(myIndex, f);
        } else {
            arg.set(myIndex, 0.0);
        }
    }

    public void ftran(final PhysicalStore<Double> arg) {

        double d = arg.doubleValue(myIndex);

        if (d == 0.0) {
            return;
        }

        d /= myNeagtedDiagonal;

        for (NonzeroView<Double> nz : myColumn.nonzeros()) {
            long index = nz.index();
            if (index == myIndex) {
                arg.set(index, -d);
            } else {
                arg.add(index, nz.doubleValue() * d);
            }
        }
    }

}
