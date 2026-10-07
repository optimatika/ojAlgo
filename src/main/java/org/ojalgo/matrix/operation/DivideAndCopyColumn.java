/*
 * Copyright 1997-2026 Optimatika
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
package org.ojalgo.matrix.operation;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.BasicArray;
import org.ojalgo.array.ScalarArray;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.scalar.Scalar;

/**
 * Divides the elements below the (row, column) element by that element, and copies the results to the
 * destination array.
 *
 * @author apete
 */
public abstract class DivideAndCopyColumn implements MatrixOperation {

    public static void invoke(final double[] data, final int structure, final int row, final int column, final double[] destination) {

        int index = row + column * structure;
        double denominator = data[index];

        for (int i = row + 1; i < structure; i++) {
            destination[i] = data[++index] /= denominator;
        }
    }

    public static <N extends Scalar<N>> void invoke(final N[] data, final int structure, final int row, final int column, final N[] destination) {

        int index = row + column * structure;
        N denominator = data[index];

        for (int i = row + 1; i < structure; i++) {
            index++;
            destination[i] = data[index] = data[index].divide(denominator).get();
        }
    }

    public static <N extends Scalar<N>> void invokeGeneric(final PhysicalStore<N> store, final int iterationPoint, final BasicArray<N> multipliers) {
        DivideAndCopyColumn.invoke(((GenericStore<N>) store).data, store.getRowDim(), iterationPoint, iterationPoint, ((ScalarArray<N>) multipliers).data);
    }

    public static void invokeR064(final PhysicalStore<Double> store, final int iterationPoint, final BasicArray<Double> multipliers) {
        DivideAndCopyColumn.invoke(((R064Store) store).data, store.getRowDim(), iterationPoint, iterationPoint, ((ArrayR064) multipliers).data);
    }

}
