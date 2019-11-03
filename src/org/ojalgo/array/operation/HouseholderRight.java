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
package org.ojalgo.array.operation;

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.scalar.Scalar;

public final class HouseholderRight implements ArrayOperation {

    public static int THRESHOLD = 512;

    public static void invoke(final double[] data, final int structure, final int firstRow, final int rowLimit, final int numberOfColumns,
            final Householder.Primitive64 householder, final double[] work) {

        final double[] vector = householder.vector;
        final int firstNonZero = householder.first;
        final double beta = householder.beta;

        for (int j = firstNonZero; j < numberOfColumns; j++) {
            AXPY.invoke(work, 0, beta * vector[j], data, j * structure, firstRow, rowLimit);
        }
        for (int j = firstNonZero; j < numberOfColumns; j++) {
            AXPY.invoke(data, j * structure, -vector[j], work, 0, firstRow, rowLimit);
        }
    }

    public static void invoke(final float[] data, final int structure, final int firstRow, final int rowLimit, final int numberOfColumns,
            final Householder.Primitive32 householder, final float[] work) {

        final float[] vector = householder.vector;
        final int firstNonZero = householder.first;
        final float beta = householder.beta;

        for (int j = firstNonZero; j < numberOfColumns; j++) {
            AXPY.invoke(work, 0, beta * vector[j], data, j * structure, firstRow, rowLimit);
        }
        for (int j = firstNonZero; j < numberOfColumns; j++) {
            AXPY.invoke(data, j * structure, -vector[j], work, 0, firstRow, rowLimit);
        }
    }

    public static <N extends Scalar<N>> void invoke(final N[] data, final int first, final int limit, final int tmpColDim,
            final Householder.Generic<N> householder, final Scalar.Factory<N> scalar) {

        final N[] tmpHouseholderVector = householder.vector;
        final int tmpFirstNonZero = householder.first;
        final N tmpBeta = householder.beta;

        final int tmpRowDim = data.length / tmpColDim;

        Scalar<N> tmpScale;
        int tmpIndex;
        for (int i = first; i < limit; i++) {
            tmpScale = scalar.zero();
            tmpIndex = i + (tmpFirstNonZero * tmpRowDim);
            for (int j = tmpFirstNonZero; j < tmpColDim; j++) {
                tmpScale = tmpScale.add(tmpHouseholderVector[j].conjugate().multiply(data[tmpIndex].conjugate()));
                tmpIndex += tmpRowDim;
            }
            tmpScale = tmpScale.multiply(tmpBeta);
            tmpIndex = i + (tmpFirstNonZero * tmpRowDim);
            for (int j = tmpFirstNonZero; j < tmpColDim; j++) {
                data[tmpIndex] = data[tmpIndex].conjugate().subtract(tmpScale.multiply(tmpHouseholderVector[j])).conjugate().get();
                tmpIndex += tmpRowDim;
            }
        }
    }

    private static void invoke2old(final double[] data, final int first, final int limit, final int tmpColDim, final Householder.Primitive64 householder) {

        final double[] tmpHouseholderVector = householder.vector;
        final int tmpFirstNonZero = householder.first;
        final double tmpBeta = householder.beta;

        final int tmpRowDim = data.length / tmpColDim;

        double tmpScale;
        int tmpIndex;
        for (int i = first; i < limit; i++) {
            tmpScale = PrimitiveMath.ZERO;
            tmpIndex = i + (tmpFirstNonZero * tmpRowDim);
            for (int j = tmpFirstNonZero; j < tmpColDim; j++) {
                tmpScale += tmpHouseholderVector[j] * data[tmpIndex];
                tmpIndex += tmpRowDim;
            }
            tmpScale *= tmpBeta;
            tmpIndex = i + (tmpFirstNonZero * tmpRowDim);
            for (int j = tmpFirstNonZero; j < tmpColDim; j++) {
                data[tmpIndex] -= tmpScale * tmpHouseholderVector[j];
                tmpIndex += tmpRowDim;
            }
        }
    }

    @Override
    public int threshold() {
        return THRESHOLD;
    }

}
