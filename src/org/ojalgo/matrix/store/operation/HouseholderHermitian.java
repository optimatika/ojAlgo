/*
 * Copyright 1997-2013 Optimatika (www.optimatika.se)
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
package org.ojalgo.matrix.store.operation;

import static org.ojalgo.constant.PrimitiveMath.*;

import java.math.BigDecimal;

import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.constant.BigMath;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BigFunction;
import org.ojalgo.function.ComplexFunction;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.scalar.ComplexNumber;

/**
 * Performs Householder transformation from both sides simultaneously
 * assuming that [A] is hermitian (square symmetric) [A] = [A]<sup>H</sup>.
 * 
 * Will only read from and write to the lower/left triangular part of [A].
 *
 * @author apete
 */
public final class HouseholderHermitian extends MatrixOperation {

    public static int THRESHOLD = 64;

    public static void invoke(final BigDecimal[] aData, final Householder.Big aHouseholder, final BigDecimal[] aWorker) {

        final BigDecimal[] tmpVector = aHouseholder.vector;
        final int tmpFirst = aHouseholder.first;
        final int tmpLength = tmpVector.length;
        final BigDecimal tmpBeta = aHouseholder.beta;
        final int tmpCount = tmpLength - tmpFirst;

        if (tmpCount > THRESHOLD) {

            final DivideAndConquer tmpConqurer = new DivideAndConquer() {

                @Override
                protected void conquer(final int aFirst, final int aLimit) {
                    MultiplyHermitianAndVector.invoke(aWorker, aFirst, aLimit, aData, tmpVector, tmpFirst);
                }
            };

            tmpConqurer.invoke(tmpFirst, tmpLength, THRESHOLD);

        } else {

            MultiplyHermitianAndVector.invoke(aWorker, tmpFirst, tmpLength, aData, tmpVector, tmpFirst);
        }

        BigDecimal tmpVal = BigMath.ZERO;
        for (int c = tmpFirst; c < tmpLength; c++) {
            //tmpVal += tmpVector[c] * aWorker[c];
            tmpVal = tmpVal.add(tmpVector[c].multiply(aWorker[c]));
        }
        //tmpVal *= (tmpBeta / TWO);
        tmpVal = BigFunction.DIVIDE.invoke(tmpVal.multiply(tmpBeta), BigMath.TWO);
        for (int c = tmpFirst; c < tmpLength; c++) {
            //aWorker[c] = tmpBeta * (aWorker[c] - (tmpVal * tmpVector[c]));
            aWorker[c] = tmpBeta.multiply(aWorker[c].subtract(tmpVal.multiply(tmpVector[c])));
        }

        if (tmpCount > THRESHOLD) {

            final DivideAndConquer tmpConqurer = new DivideAndConquer() {

                @Override
                protected void conquer(final int aFirst, final int aLimit) {
                    HermitianRank2Update.invoke(aData, aFirst, aLimit, tmpVector, aWorker);
                }
            };

            tmpConqurer.invoke(tmpFirst, tmpLength, THRESHOLD);

        } else {

            HermitianRank2Update.invoke(aData, tmpFirst, tmpLength, tmpVector, aWorker);
        }
    }

    public static void invoke(final ComplexNumber[] aData, final Householder.Complex aHouseholder, final ComplexNumber[] aWorker) {

        final ComplexNumber[] tmpVector = aHouseholder.vector;
        final int tmpFirst = aHouseholder.first;
        final int tmpLength = tmpVector.length;
        final ComplexNumber tmpBeta = aHouseholder.beta;
        final int tmpCount = tmpLength - tmpFirst;

        if (tmpCount > THRESHOLD) {

            final DivideAndConquer tmpConqurer = new DivideAndConquer() {

                @Override
                protected void conquer(final int aFirst, final int aLimit) {
                    MultiplyHermitianAndVector.invoke(aWorker, aFirst, aLimit, aData, tmpVector, tmpFirst);
                }
            };

            tmpConqurer.invoke(tmpFirst, tmpLength, THRESHOLD);

        } else {

            MultiplyHermitianAndVector.invoke(aWorker, tmpFirst, tmpLength, aData, tmpVector, tmpFirst);
        }

        ComplexNumber tmpVal = ComplexNumber.ZERO;
        for (int c = tmpFirst; c < tmpLength; c++) {
            //tmpVal += tmpVector[c] * aWorker[c];
            tmpVal = tmpVal.add(tmpVector[c].conjugate().multiply(aWorker[c]));
        }
        //tmpVal *= (tmpBeta / TWO);
        tmpVal = ComplexFunction.DIVIDE.invoke(tmpVal.multiply(tmpBeta), ComplexNumber.TWO);
        for (int c = tmpFirst; c < tmpLength; c++) {
            //aWorker[c] = tmpBeta * (aWorker[c] - (tmpVal * tmpVector[c]));
            aWorker[c] = tmpBeta.multiply(aWorker[c].subtract(tmpVal.multiply(tmpVector[c])));
        }

        if (tmpCount > THRESHOLD) {

            final DivideAndConquer tmpConqurer = new DivideAndConquer() {

                @Override
                protected void conquer(final int aFirst, final int aLimit) {
                    HermitianRank2Update.invoke(aData, aFirst, aLimit, tmpVector, aWorker);
                }
            };

            tmpConqurer.invoke(tmpFirst, tmpLength, THRESHOLD);

        } else {

            HermitianRank2Update.invoke(aData, tmpFirst, tmpLength, tmpVector, aWorker);
        }
    }

    public static void invoke(final double[] aData, final Householder.Primitive aHouseholder, final double[] aWorker) {

        final double[] tmpVector = aHouseholder.vector;
        final int tmpFirst = aHouseholder.first;
        final int tmpLength = tmpVector.length;
        final double tmpBeta = aHouseholder.beta;
        final int tmpCount = tmpLength - tmpFirst;

        if (tmpCount > THRESHOLD) {

            final DivideAndConquer tmpConqurer = new DivideAndConquer() {

                @Override
                protected void conquer(final int aFirst, final int aLimit) {
                    MultiplyHermitianAndVector.invoke(aWorker, aFirst, aLimit, aData, tmpVector, tmpFirst);
                }
            };

            tmpConqurer.invoke(tmpFirst, tmpLength, THRESHOLD);

        } else {

            MultiplyHermitianAndVector.invoke(aWorker, tmpFirst, tmpLength, aData, tmpVector, tmpFirst);
        }

        double tmpVal = ZERO;
        for (int c = tmpFirst; c < tmpLength; c++) {
            tmpVal += tmpVector[c] * aWorker[c];
        }
        tmpVal *= (tmpBeta / TWO);
        for (int c = tmpFirst; c < tmpLength; c++) {
            aWorker[c] = tmpBeta * (aWorker[c] - (tmpVal * tmpVector[c]));
        }

        if (tmpCount > THRESHOLD) {

            final DivideAndConquer tmpConqurer = new DivideAndConquer() {

                @Override
                protected void conquer(final int aFirst, final int aLimit) {
                    HermitianRank2Update.invoke(aData, aFirst, aLimit, tmpVector, aWorker);
                }
            };

            tmpConqurer.invoke(tmpFirst, tmpLength, THRESHOLD);

        } else {

            HermitianRank2Update.invoke(aData, tmpFirst, tmpLength, tmpVector, aWorker);
        }
    }

    public static void tred2j(final double[] z, final double[] d, final double[] e, final boolean yesvecs) {

        /*       
        Symmetric Householder reduction to tridiagonal form.
        The original version of this code was taken from JAMA.
        That code is in turn derived from the Algol procedures tred2
        by Bowdler, Martin, Reinsch, and Wilkinson, Handbook for
        Auto. Comp., Vol.ii-Linear Algebra, and the corresponding
        Fortran subroutine in EISPACK.
        tred2 is also described in Numerical Recipes. Parameters and
        variables are names are choosen to match what is used there.
        
        z is the original matrix [A] that will be overwritten with [Q]
        d will hold the main diagonal of the tridiagonal result
        e will hold the off (super and sub) diagonals of the tridiagonal result
        */

        final int n = d.length;

        double scale;
        double h;
        double f;
        double g;
        double hh;

        final int tmpRowDim = n;
        final int tmpLast = n - 1;

        // Copy the last column (same as the last row) of z to d
        // The last row/column is the first to be worked on in the main loop
        for (int i = 0; i < n; i++) {
            d[i] = z[i + (tmpRowDim * tmpLast)];
        }

        // Householder reduction to tridiagonal form.
        for (int i = tmpLast; i > 0; i--) { // row index of target householder point

            final int l = i - 1; // col index of target householder point

            h = scale = PrimitiveMath.ZERO;

            // Calc the norm of the row/col to zero out
            for (int k = 0; k < i; k++) {
                scale += Math.abs(d[k]);
            }

            if (scale == PrimitiveMath.ZERO) {
                // Skip generation, already zero
                e[i] = d[l];
                for (int j = 0; j < i; j++) {
                    d[j] = z[l + (tmpRowDim * j)];
                    z[i + (tmpRowDim * j)] = PrimitiveMath.ZERO; // Are both needed?
                    z[j + (tmpRowDim * i)] = PrimitiveMath.ZERO; // Could cause cache-misses
                }

            } else {
                // Generate Householder vector.

                for (int k = 0; k < i; k++) {
                    d[k] /= scale;
                    h += d[k] * d[k]; // can be optimised, too many array read/write ops
                }
                f = d[l];
                g = Math.sqrt(h);
                if (f > 0) {
                    g = -g;
                }
                e[i] = scale * g;
                h -= f * g;
                d[l] = f - g;
                for (int j = 0; j < i; j++) {
                    e[j] = PrimitiveMath.ZERO;
                }

                // Apply similarity transformation to remaining columns.
                // Remaing refers to all columns "before" the target col
                for (int j = 0; j < i; j++) {
                    f = d[j];
                    z[j + (tmpRowDim * i)] = f;
                    g = e[j] + (z[j + (tmpRowDim * j)] * f);
                    for (int k = j + 1; k <= l; k++) {
                        g += z[k + (tmpRowDim * j)] * d[k]; // access the same element in z twice
                        e[k] += z[k + (tmpRowDim * j)] * f;
                    }
                    e[j] = g;
                }
                f = PrimitiveMath.ZERO;
                for (int j = 0; j < i; j++) {
                    e[j] /= h;
                    f += e[j] * d[j];
                }
                hh = f / (h + h);
                for (int j = 0; j < i; j++) {
                    e[j] -= hh * d[j];
                }
                for (int j = 0; j < i; j++) {
                    f = d[j];
                    g = e[j];
                    for (int k = j; k <= l; k++) {
                        z[k + (tmpRowDim * j)] -= ((f * e[k]) + (g * d[k]));
                    }
                    d[j] = z[l + (tmpRowDim * j)];
                    z[i + (tmpRowDim * j)] = PrimitiveMath.ZERO;
                }
            }
            d[i] = h;
        }

        // Accumulate transformations.
        if (yesvecs) {

            for (int i = 0; i < tmpLast; i++) {

                final int l = i + 1;

                z[tmpLast + (tmpRowDim * i)] = z[i + (tmpRowDim * i)];
                z[i + (tmpRowDim * i)] = PrimitiveMath.ONE;
                h = d[l];
                if (h != PrimitiveMath.ZERO) {
                    for (int k = 0; k <= i; k++) {
                        d[k] = z[k + (tmpRowDim * l)] / h;
                    }
                    for (int j = 0; j <= i; j++) {
                        g = PrimitiveMath.ZERO;
                        for (int k = 0; k <= i; k++) {
                            g += z[k + (tmpRowDim * l)] * z[k + (tmpRowDim * j)];
                        }
                        for (int k = 0; k <= i; k++) {
                            z[k + (tmpRowDim * j)] -= g * d[k];
                        }
                    }
                }
                for (int k = 0; k <= i; k++) {
                    z[k + (tmpRowDim * l)] = PrimitiveMath.ZERO;
                }
            }
            for (int j = 0; j < n; j++) {
                d[j] = z[tmpLast + (tmpRowDim * j)];
                z[tmpLast + (tmpRowDim * j)] = PrimitiveMath.ZERO;
            }
            z[tmpLast + (tmpRowDim * tmpLast)] = PrimitiveMath.ONE;

            e[0] = PrimitiveMath.ZERO;
        }

    }

    public static void tred2nr(final double[] z, final double[] d, final double[] e, final boolean yesvecs) {

        final int n = d.length;
        int l;
        final int tmpRowDim = n;

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
                    scale += Math.abs(z[i + (k * tmpRowDim)]);
                }

                if (scale == PrimitiveMath.ZERO) {
                    e[i] = z[i + (l * tmpRowDim)];
                } else {
                    for (int k = 0; k < i; k++) {
                        z[i + (k * tmpRowDim)] /= scale;
                        h += z[i + (k * tmpRowDim)] * z[i + (k * tmpRowDim)];
                    }
                    f = z[i + (l * tmpRowDim)];
                    g = (f >= PrimitiveMath.ZERO) ? -Math.sqrt(h) : Math.sqrt(h);
                    e[i] = scale * g;
                    h -= f * g;
                    z[i + (l * tmpRowDim)] = f - g;
                    f = PrimitiveMath.ZERO;
                    for (int j = 0; j < i; j++) {
                        if (yesvecs) {
                            z[j + (i * tmpRowDim)] = z[i + (j * tmpRowDim)] / h;
                        }
                        g = PrimitiveMath.ZERO;
                        for (int k = 0; k < (j + 1); k++) {
                            g += z[j + (k * tmpRowDim)] * z[i + (k * tmpRowDim)];
                        }
                        for (int k = j + 1; k < i; k++) {
                            g += z[k + (j * tmpRowDim)] * z[i + (k * tmpRowDim)];
                        }
                        e[j] = g / h;
                        f += e[j] * z[i + (j * tmpRowDim)];
                    }
                    hh = f / (h + h);
                    for (int j = 0; j < i; j++) {
                        f = z[i + (j * tmpRowDim)];
                        e[j] = g = e[j] - (hh * f);
                        for (int k = 0; k < (j + 1); k++) {
                            z[j + (k * tmpRowDim)] -= ((f * e[k]) + (g * z[i + (k * tmpRowDim)]));
                        }
                    }
                }
            } else {
                e[i] = z[i + (l * tmpRowDim)];
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
                            g += z[i + (k * tmpRowDim)] * z[k + (j * tmpRowDim)];
                        }
                        for (int k = 0; k < i; k++) {
                            z[k + (j * tmpRowDim)] -= g * z[k + (i * tmpRowDim)];
                        }
                    }
                }
                d[i] = z[i + (i * tmpRowDim)];
                z[i + (i * tmpRowDim)] = PrimitiveMath.ONE;
                for (int j = 0; j < i; j++) {
                    z[i + (j * tmpRowDim)] = PrimitiveMath.ZERO;
                    z[j + (i * tmpRowDim)] = PrimitiveMath.ZERO;
                }
            } else {
                d[i] = z[i + (i * tmpRowDim)];
            }
        }
    }

    private HouseholderHermitian() {
        super();
    }

}
