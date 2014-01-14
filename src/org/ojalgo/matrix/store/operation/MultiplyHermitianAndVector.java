/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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

import org.ojalgo.constant.BigMath;
import org.ojalgo.scalar.ComplexNumber;

/**
 * Multiplies an hermitian (square symmetric) matrix with a vector.
 * Will only read from the lower/left triangular part of the matrix,
 * and will only calculate the lower/left part of the results.
 *
 * @author apete
 */
public final class MultiplyHermitianAndVector extends MatrixOperation {

    public static int THRESHOLD = 64;

    public static void invoke(final BigDecimal[] productMtrx, final int aFirst, final int aLimit, final BigDecimal[] aSymmetric, final BigDecimal[] aVector,
            final int aFirstNonZero) {

        final int tmpRowDim = aVector.length;

        BigDecimal tmpVal;
        for (int i = aFirst; i < aLimit; i++) {
            tmpVal = BigMath.ZERO;
            for (int c = aFirstNonZero; c < i; c++) {
                //tmpVal += aSymmetric[i + c * tmpRowDim] * aVector[c];
                tmpVal = tmpVal.add(aSymmetric[i + (c * tmpRowDim)].multiply(aVector[c]));
            }
            for (int c = i; c < tmpRowDim; c++) {
                //tmpVal += aSymmetric[c + i * tmpRowDim] * aVector[c];
                tmpVal = tmpVal.add(aSymmetric[c + (i * tmpRowDim)].multiply(aVector[c]));
            }
            productMtrx[i] = tmpVal;
        }
    }

    public static void invoke(final ComplexNumber[] productMtrx, final int aFirst, final int aLimit, final ComplexNumber[] aSymmetric,
            final ComplexNumber[] aVector, final int aFirstNonZero) {

        final int tmpRowDim = aVector.length;

        ComplexNumber tmpVal;
        for (int i = aFirst; i < aLimit; i++) {
            tmpVal = ComplexNumber.ZERO;
            for (int c = aFirstNonZero; c < i; c++) {
                //tmpVal += aSymmetric[i + c * tmpRowDim] * aVector[c];
                tmpVal = tmpVal.add(aSymmetric[i + (c * tmpRowDim)].multiply(aVector[c]));
            }
            for (int c = i; c < tmpRowDim; c++) {
                //tmpVal += aSymmetric[c + i * tmpRowDim] * aVector[c];
                tmpVal = tmpVal.add(aSymmetric[c + (i * tmpRowDim)].conjugate().multiply(aVector[c]));
            }
            productMtrx[i] = tmpVal;
        }
    }

    public static void invoke(final double[] productMtrx, final int aFirst, final int aLimit, final double[] aSymmetric, final double[] aVector,
            final int aFirstNonZero) {

        final int tmpRowDim = aVector.length;

        double tmpVal;
        for (int i = aFirst; i < aLimit; i++) {
            tmpVal = ZERO;
            for (int c = aFirstNonZero; c < i; c++) {
                tmpVal += aSymmetric[i + (c * tmpRowDim)] * aVector[c];
            }
            for (int c = i; c < tmpRowDim; c++) {
                tmpVal += aSymmetric[c + (i * tmpRowDim)] * aVector[c];
            }
            productMtrx[i] = tmpVal;
        }
    }

    private MultiplyHermitianAndVector() {
        super();
    }

}
