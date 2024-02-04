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
package org.ojalgo.array.operation;

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.util.Arrays;

import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * Performs Householder transformation from both sides simultaneously assuming that [A] is hermitian (square
 * symmetric) [A] = [A]<sup>H</sup>. Will only read from and write to the lower/left triangular part of [A].
 *
 * @author apete
 */
public abstract class HouseholderHermitian implements ArrayOperation {

    public static void invoke(final double[] data, final Householder.Primitive64 householder, final double[] worker) {

        double[] tmpVector = householder.vector;
        int tmpFirst = householder.first;
        int tmpLength = tmpVector.length;
        double tmpBeta = householder.beta;
        int tmpCount = tmpLength - tmpFirst;

        if (tmpCount > MultiplyHermitianAndVector.THRESHOLD) {

            DivideAndConquer tmpConqurer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    MultiplyHermitianAndVector.invoke(worker, first, limit, data, tmpVector, tmpFirst);
                }
            };

            tmpConqurer.invoke(tmpFirst, tmpLength, MultiplyHermitianAndVector.THRESHOLD);

        } else {

            MultiplyHermitianAndVector.invoke(worker, tmpFirst, tmpLength, data, tmpVector, tmpFirst);
        }

        double tmpVal = PrimitiveMath.ZERO;
        for (int c = tmpFirst; c < tmpLength; c++) {
            tmpVal += tmpVector[c] * worker[c];
        }
        tmpVal *= tmpBeta / PrimitiveMath.TWO;
        for (int c = tmpFirst; c < tmpLength; c++) {
            worker[c] = tmpBeta * (worker[c] - tmpVal * tmpVector[c]);
        }

        if (tmpCount > HermitianRank2Update.THRESHOLD) {

            DivideAndConquer tmpConqurer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    HermitianRank2Update.invoke(data, first, limit, tmpVector, worker);
                }
            };

            tmpConqurer.invoke(tmpFirst, tmpLength, HermitianRank2Update.THRESHOLD);

        } else {

            HermitianRank2Update.invoke(data, tmpFirst, tmpLength, tmpVector, worker);
        }
    }

    public static <N extends Scalar<N>> void invoke(final N[] data, final Householder.Generic<N> householder, final N[] worker,
            final Scalar.Factory<N> scalar) {

        N[] tmpVector = householder.vector;
        int tmpFirst = householder.first;
        int tmpLength = tmpVector.length;
        N tmpBeta = householder.beta;
        int tmpCount = tmpLength - tmpFirst;

        if (tmpCount > MultiplyHermitianAndVector.THRESHOLD) {

            DivideAndConquer tmpConqurer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    MultiplyHermitianAndVector.invoke(worker, first, limit, data, tmpVector, tmpFirst, scalar);
                }
            };

            tmpConqurer.invoke(tmpFirst, tmpLength, MultiplyHermitianAndVector.THRESHOLD);

        } else {

            MultiplyHermitianAndVector.invoke(worker, tmpFirst, tmpLength, data, tmpVector, tmpFirst, scalar);
        }

        Scalar<N> tmpVal = scalar.zero();
        for (int c = tmpFirst; c < tmpLength; c++) {
            //tmpVal += tmpVector[c] * worker[c];
            tmpVal = tmpVal.add(tmpVector[c].conjugate().multiply(worker[c]));
        }
        //tmpVal *= (tmpBeta / TWO);
        tmpVal = tmpVal.multiply(tmpBeta).divide(PrimitiveMath.TWO);
        for (int c = tmpFirst; c < tmpLength; c++) {
            //worker[c] = tmpBeta * (worker[c] - (tmpVal * tmpVector[c]));
            worker[c] = tmpBeta.multiply(worker[c].subtract(tmpVal.multiply(tmpVector[c]))).get();
        }

        if (tmpCount > HermitianRank2Update.THRESHOLD) {

            DivideAndConquer tmpConqurer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    HermitianRank2Update.invoke(data, first, limit, tmpVector, worker);
                }
            };

            tmpConqurer.invoke(tmpFirst, tmpLength, HermitianRank2Update.THRESHOLD);

        } else {

            HermitianRank2Update.invoke(data, tmpFirst, tmpLength, tmpVector, worker);
        }
    }

    /**
     * Ursprung JAMA men refactored till ojAlgos egna strukturer
     */
    public static void tred2j(final double[] data, final double[] d, final double[] e, final boolean yesvecs) {

        /*
         * Symmetric Householder reduction to tridiagonal form. The original version of this code was taken
         * from JAMA. That code is in turn derived from the Algol procedures tred2 by Bowdler, Martin,
         * Reinsch, and Wilkinson, Handbook for Auto. Comp., Vol.ii-Linear Algebra, and the corresponding
         * Fortran subroutine in EISPACK. tred2 is also described in Numerical Recipes. The array d will hold
         * the main diagonal of the tridiagonal result, and e will hold the off (super and sub) diagonals of
         * the tridiagonal result
         */

        int n = d.length; // rows, columns, structure
        int tmpLast = n - 1;

        double scale;
        double h;
        double f;
        double g;
        double tmpVal; // Nothing special, just some transient value

        int tmpRowDim = n;

        // Copy the last column (same as the last row) of z to d
        // The last row/column is the first to be worked on in the main loop
        FillMatchingSingle.invoke(data, tmpRowDim * tmpLast, d, 0, 0, n);

        // Householder reduction to tridiagonal form.
        for (int i = tmpLast; i > 0; i--) { // row index of target householder point
            int l = i - 1; // col index of target householder point

            h = scale = PrimitiveMath.ZERO;

            // Calc the norm of the row/col to zero out - to avoid under/overflow.
            for (int k = 0; k < i; k++) {
                // scale += PrimitiveFunction.ABS.invoke(d[k]);
                scale = PrimitiveMath.MAX.invoke(scale, PrimitiveMath.ABS.invoke(d[k]));
            }

            if (scale == PrimitiveMath.ZERO) {
                // Skip generation, already zero
                e[i] = d[l];
                for (int j = 0; j < i; j++) {
                    d[j] = data[l + tmpRowDim * j];
                    data[i + tmpRowDim * j] = PrimitiveMath.ZERO; // Are both needed?
                    data[j + tmpRowDim * i] = PrimitiveMath.ZERO; // Could cause cache-misses
                }

            } else {
                // Generate Householder vector.

                for (int k = 0; k < i; k++) {
                    tmpVal = d[k] /= scale;
                    h += tmpVal * tmpVal; // d[k] * d[k]
                }
                f = d[l];
                g = PrimitiveMath.SQRT.invoke(h);
                if (f > 0) {
                    g = -g;
                }
                e[i] = scale * g;
                h -= f * g;
                d[l] = f - g;
                Arrays.fill(e, 0, i, PrimitiveMath.ZERO);

                // Apply similarity transformation to remaining columns.
                // Remaing refers to all columns "before" the target col
                for (int j = 0; j < i; j++) {
                    f = d[j];
                    data[j + tmpRowDim * i] = f;
                    g = e[j] + data[j + tmpRowDim * j] * f;
                    for (int k = j + 1; k <= l; k++) {
                        tmpVal = data[k + tmpRowDim * j];
                        g += tmpVal * d[k]; // access the same element in z twice
                        e[k] += tmpVal * f;
                    }
                    e[j] = g;
                }
                f = PrimitiveMath.ZERO;
                for (int j = 0; j < i; j++) {
                    e[j] /= h;
                    f += e[j] * d[j];
                }
                tmpVal = f / (h + h);
                AXPY.invoke(e, 0, -tmpVal, d, 0, 0, i);
                for (int j = 0; j < i; j++) {
                    f = d[j];
                    g = e[j];
                    for (int k = j; k <= l; k++) {
                        data[k + tmpRowDim * j] -= f * e[k] + g * d[k];
                    }
                    d[j] = data[l + tmpRowDim * j];
                    data[i + tmpRowDim * j] = PrimitiveMath.ZERO;
                }
            }
            d[i] = h;
        }

        // Accumulate transformations.
        if (yesvecs) {

            for (int i = 0; i < tmpLast; i++) {

                int l = i + 1;

                data[tmpLast + tmpRowDim * i] = data[i + tmpRowDim * i];
                data[i + tmpRowDim * i] = PrimitiveMath.ONE;
                h = d[l];
                if (h != PrimitiveMath.ZERO) {
                    for (int k = 0; k <= i; k++) {
                        d[k] = data[k + tmpRowDim * l] / h;
                    }
                    for (int j = 0; j <= i; j++) {
                        g = PrimitiveMath.ZERO;
                        for (int k = 0; k <= i; k++) {
                            g += data[k + tmpRowDim * l] * data[k + tmpRowDim * j];
                        }
                        for (int k = 0; k <= i; k++) {
                            data[k + tmpRowDim * j] -= g * d[k];
                        }
                    }
                }
                for (int k = 0; k <= i; k++) {
                    data[k + tmpRowDim * l] = PrimitiveMath.ZERO;
                }
            }
            for (int j = 0; j < n; j++) {
                d[j] = data[tmpLast + tmpRowDim * j];
                data[tmpLast + tmpRowDim * j] = PrimitiveMath.ZERO;
            }
            data[tmpLast + tmpRowDim * tmpLast] = PrimitiveMath.ONE;

            e[0] = PrimitiveMath.ZERO;
        }

        for (int i = 1; i < e.length; i++) {
            e[i - 1] = e[i];
        }
        e[e.length - 1] = ZERO;
    }

    /**
     * Ursprung Numerical Recipies. Samma som tred2j, men är inte lika snabb.
     */
    public static void tred2nr(final double[] data, final double[] d, final double[] e, final boolean yesvecs) {

        int n = d.length;
        int l;
        int tmpRowDim = n;

        double scale;
        double h;
        double hh;
        double g;
        double f;

        for (int i = n - 1; i > 0; i--) {

            l = i - 1;

            scale = PrimitiveMath.ZERO;
            h = PrimitiveMath.ZERO;

            if (l > 0) {

                for (int k = 0; k < i; k++) {
                    scale += PrimitiveMath.ABS.invoke(data[i + k * tmpRowDim]);
                }

                // if (scale == PrimitiveMath.ZERO) {
                if (NumberContext.compare(scale, PrimitiveMath.ZERO) == 0) {
                    e[i] = data[i + l * tmpRowDim];
                } else {
                    for (int k = 0; k < i; k++) {
                        data[i + k * tmpRowDim] /= scale;
                        h += data[i + k * tmpRowDim] * data[i + k * tmpRowDim];
                    }
                    f = data[i + l * tmpRowDim];
                    g = f >= PrimitiveMath.ZERO ? -PrimitiveMath.SQRT.invoke(h) : PrimitiveMath.SQRT.invoke(h);
                    e[i] = scale * g;
                    h -= f * g;
                    data[i + l * tmpRowDim] = f - g;
                    f = PrimitiveMath.ZERO;
                    for (int j = 0; j < i; j++) {
                        if (yesvecs) {
                            data[j + i * tmpRowDim] = data[i + j * tmpRowDim] / h;
                        }
                        g = PrimitiveMath.ZERO;
                        for (int k = 0; k < j + 1; k++) {
                            g += data[j + k * tmpRowDim] * data[i + k * tmpRowDim];
                        }
                        for (int k = j + 1; k < i; k++) {
                            g += data[k + j * tmpRowDim] * data[i + k * tmpRowDim];
                        }
                        e[j] = g / h;
                        f += e[j] * data[i + j * tmpRowDim];
                    }
                    hh = f / (h + h);
                    for (int j = 0; j < i; j++) {
                        f = data[i + j * tmpRowDim];
                        e[j] = g = e[j] - hh * f;
                        for (int k = 0; k < j + 1; k++) {
                            data[j + k * tmpRowDim] -= f * e[k] + g * data[i + k * tmpRowDim];
                        }
                    }
                }
            } else {
                e[i] = data[i + l * tmpRowDim];
            }
            d[i] = h;
        }
        if (yesvecs) {
            d[0] = PrimitiveMath.ZERO;
        }
        e[0] = PrimitiveMath.ZERO;
        for (int i = 0; i < n; i++) {
            if (yesvecs) {
                if (d[i] != PrimitiveMath.ZERO) {
                    for (int j = 0; j < i; j++) {
                        g = PrimitiveMath.ZERO;
                        for (int k = 0; k < i; k++) {
                            g += data[i + k * tmpRowDim] * data[k + j * tmpRowDim];
                        }
                        for (int k = 0; k < i; k++) {
                            data[k + j * tmpRowDim] -= g * data[k + i * tmpRowDim];
                        }
                    }
                }
                d[i] = data[i + i * tmpRowDim];
                data[i + i * tmpRowDim] = PrimitiveMath.ONE;
                for (int j = 0; j < i; j++) {
                    data[i + j * tmpRowDim] = PrimitiveMath.ZERO;
                    data[j + i * tmpRowDim] = PrimitiveMath.ZERO;
                }
            } else {
                d[i] = data[i + i * tmpRowDim];
            }
        }
    }

}
