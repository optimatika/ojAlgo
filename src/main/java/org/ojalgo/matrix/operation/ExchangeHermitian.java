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

import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.scalar.Scalar;

/**
 * Exchanges two rows and the corresponding two columns of a hermitian matrix. Will only read from and write
 * to the lower/left triangular part of the matrix.
 *
 * @author apete
 */
public abstract class ExchangeHermitian implements MatrixOperation {

    public static <N extends Scalar<N>> void invokeGeneric(final PhysicalStore<N> store, final int indexA, final int indexB) {

        GenericStore<N> matrix = (GenericStore<N>) store;

        int indexMin = Math.min(indexA, indexB);
        int indexMax = Math.max(indexA, indexB);

        N tmpVal;

        for (int j = 0; j < indexMin; j++) {
            tmpVal = matrix.get(indexMin, j);
            matrix.set(indexMin, j, matrix.get(indexMax, j));
            matrix.set(indexMax, j, tmpVal);
        }

        tmpVal = matrix.get(indexMin, indexMin);
        matrix.set(indexMin, indexMin, matrix.get(indexMax, indexMax));
        matrix.set(indexMax, indexMax, tmpVal);

        for (int ij = indexMin + 1; ij < indexMax; ij++) {
            tmpVal = matrix.get(ij, indexMin);
            matrix.set(ij, indexMin, matrix.get(indexMax, ij).conjugate().get());
            matrix.set(indexMax, ij, tmpVal.conjugate().get());
        }

        for (int i = indexMax + 1, limit = matrix.getRowDim(); i < limit; i++) {
            tmpVal = matrix.get(i, indexMin);
            matrix.set(i, indexMin, matrix.get(i, indexMax));
            matrix.set(i, indexMax, tmpVal);
        }
    }

    public static void invokeR064(final PhysicalStore<Double> store, final int indexA, final int indexB) {

        R064Store matrix = (R064Store) store;

        int indexMin = Math.min(indexA, indexB);
        int indexMax = Math.max(indexA, indexB);

        double tmpVal;

        for (int j = 0; j < indexMin; j++) {
            tmpVal = matrix.doubleValue(indexMin, j);
            matrix.set(indexMin, j, matrix.doubleValue(indexMax, j));
            matrix.set(indexMax, j, tmpVal);
        }

        tmpVal = matrix.doubleValue(indexMin, indexMin);
        matrix.set(indexMin, indexMin, matrix.doubleValue(indexMax, indexMax));
        matrix.set(indexMax, indexMax, tmpVal);

        for (int ij = indexMin + 1; ij < indexMax; ij++) {
            tmpVal = matrix.doubleValue(ij, indexMin);
            matrix.set(ij, indexMin, matrix.doubleValue(indexMax, ij));
            matrix.set(indexMax, ij, tmpVal);
        }

        for (int i = indexMax + 1, limit = matrix.getRowDim(); i < limit; i++) {
            tmpVal = matrix.doubleValue(i, indexMin);
            matrix.set(i, indexMin, matrix.doubleValue(i, indexMax));
            matrix.set(i, indexMax, tmpVal);
        }
    }

}
