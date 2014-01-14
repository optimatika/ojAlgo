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

import java.math.BigDecimal;

import org.ojalgo.access.Access2D;
import org.ojalgo.constant.BigMath;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BigFunction;
import org.ojalgo.scalar.ComplexNumber;

public final class SubstituteForwards extends MatrixOperation {

    public static int THRESHOLD = 16;

    public static void invoke(final BigDecimal[] aData, final int aRowDim, final int aFirstCol, final int aColLimit, final Access2D<BigDecimal> aBody,
            final boolean onesOnDiagonal, final boolean zerosAboveDiagonal) {

        final int tmpDiagDim = (int) Math.min(aBody.countRows(), aBody.countColumns());
        final BigDecimal[] tmpBodyRow = new BigDecimal[tmpDiagDim];
        BigDecimal tmpVal;
        int tmpColBaseIndex;

        for (int i = 0; i < tmpDiagDim; i++) {

            for (int j = 0; j <= i; j++) {
                tmpBodyRow[j] = aBody.get(i, j);
            }

            for (int s = aFirstCol; s < aColLimit; s++) {
                tmpColBaseIndex = s * aRowDim;

                tmpVal = BigMath.ZERO;
                for (int j = zerosAboveDiagonal ? s : 0; j < i; j++) {
                    tmpVal = tmpVal.add(tmpBodyRow[j].multiply(aData[j + tmpColBaseIndex]));
                }
                tmpVal = aData[i + tmpColBaseIndex].subtract(tmpVal);
                if (!onesOnDiagonal) {
                    tmpVal = BigFunction.DIVIDE.invoke(tmpVal, tmpBodyRow[i]);
                }

                aData[i + tmpColBaseIndex] = tmpVal;
            }
        }
    }

    public static void invoke(final ComplexNumber[] aData, final int aRowDim, final int aFirstCol, final int aColLimit, final Access2D<ComplexNumber> aBody,
            final boolean onesOnDiagonal, final boolean zerosAboveDiagonal) {

        final int tmpDiagDim = (int) Math.min(aBody.countRows(), aBody.countColumns());
        final ComplexNumber[] tmpBodyRow = new ComplexNumber[tmpDiagDim];
        ComplexNumber tmpVal;
        int tmpColBaseIndex;

        for (int i = 0; i < tmpDiagDim; i++) {

            for (int j = 0; j <= i; j++) {
                tmpBodyRow[j] = aBody.get(i, j);
            }

            for (int s = aFirstCol; s < aColLimit; s++) {
                tmpColBaseIndex = s * aRowDim;

                tmpVal = ComplexNumber.ZERO;
                for (int j = zerosAboveDiagonal ? s : 0; j < i; j++) {
                    tmpVal = tmpVal.add(tmpBodyRow[j].multiply(aData[j + tmpColBaseIndex]));
                }
                tmpVal = aData[i + tmpColBaseIndex].subtract(tmpVal);
                if (!onesOnDiagonal) {
                    tmpVal = tmpVal.divide(tmpBodyRow[i]);
                }

                aData[i + tmpColBaseIndex] = tmpVal;
            }
        }
    }

    public static void invoke(final double[] aData, final int aRowDim, final int aFirstCol, final int aColLimit, final Access2D<Double> aBody,
            final boolean onesOnDiagonal, final boolean zerosAboveDiagonal) {

        final int tmpDiagDim = (int) Math.min(aBody.countRows(), aBody.countColumns());
        final double[] tmpBodyRow = new double[tmpDiagDim];
        double tmpVal;
        int tmpColBaseIndex;

        for (int i = 0; i < tmpDiagDim; i++) {

            for (int j = 0; j <= i; j++) {
                tmpBodyRow[j] = aBody.doubleValue(i, j);
            }

            for (int s = aFirstCol; s < aColLimit; s++) {
                tmpColBaseIndex = s * aRowDim;

                tmpVal = PrimitiveMath.ZERO;
                for (int j = zerosAboveDiagonal ? s : 0; j < i; j++) {
                    tmpVal += tmpBodyRow[j] * aData[j + tmpColBaseIndex];
                }
                tmpVal = aData[i + tmpColBaseIndex] - tmpVal;
                if (!onesOnDiagonal) {
                    tmpVal /= tmpBodyRow[i];
                }

                aData[i + tmpColBaseIndex] = tmpVal;
            }
        }
    }

    private SubstituteForwards() {
        super();
    }

}
