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
import org.ojalgo.array.operation.AXPY;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.scalar.Scalar;

public abstract class ApplyCholesky implements MatrixOperation {

    public static int THRESHOLD = 128;

    public static void invoke(final double[] data, final int structure, final int firstColumn, final int columnLimit, final double[] multipliers) {
        for (int j = firstColumn; j < columnLimit; j++) {
            AXPY.invoke(data, j * structure, -multipliers[j], multipliers, 0, j, structure);
        }
    }

    public static <N extends Scalar<N>> void invoke(final N[] data, final int structure, final int firstColumn, final int columnLimit, final N[] multipliers) {
        for (int j = firstColumn; j < columnLimit; j++) {
            AXPY.invoke(data, j * structure, multipliers[j].conjugate().negate().get(), multipliers, 0, j, structure);
        }
    }

    public static <N extends Scalar<N>> void invokeGeneric(final PhysicalStore<N> store, final int iterationPoint, final BasicArray<N> multipliers) {

        GenericStore<N> matrix = (GenericStore<N>) store;
        ScalarArray<N> column = (ScalarArray<N>) multipliers;

        if (matrix.getColDim() - iterationPoint - 1 > THRESHOLD) {

            DivideAndConquer conquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    ApplyCholesky.invoke(matrix.data, matrix.getRowDim(), first, limit, column.data);
                }
            };

            conquerer.invoke(iterationPoint + 1, matrix.getColDim(), THRESHOLD);

        } else {

            ApplyCholesky.invoke(matrix.data, matrix.getRowDim(), iterationPoint + 1, matrix.getColDim(), column.data);
        }
    }

    public static void invokeR064(final PhysicalStore<Double> store, final int iterationPoint, final BasicArray<Double> multipliers) {

        R064Store matrix = (R064Store) store;
        ArrayR064 column = (ArrayR064) multipliers;

        if (matrix.getColDim() - iterationPoint - 1 > THRESHOLD) {

            DivideAndConquer conquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    ApplyCholesky.invoke(matrix.data, matrix.getRowDim(), first, limit, column.data);
                }
            };

            conquerer.invoke(iterationPoint + 1, matrix.getColDim(), THRESHOLD);

        } else {

            ApplyCholesky.invoke(matrix.data, matrix.getRowDim(), iterationPoint + 1, matrix.getColDim(), column.data);
        }
    }

}
