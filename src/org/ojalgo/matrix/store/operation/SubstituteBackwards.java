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

import java.math.BigDecimal;

import org.ojalgo.access.Access2D;
import org.ojalgo.constant.BigMath;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BigFunction;
import org.ojalgo.scalar.ComplexNumber;

public final class SubstituteBackwards extends MatrixOperation {

    public static int THRESHOLD = 32;

    public static void invoke(final BigDecimal[] aData, final int aRowDim, final int aFirstCol, final int aColLimit, final Access2D<BigDecimal> aBody,
            final boolean conjugated) {

        final int tmpDiagDim = (int) Math.min(aBody.countRows(), aBody.countColumns());
        final BigDecimal[] tmpBodyRow = new BigDecimal[tmpDiagDim];
        BigDecimal tmpVal;
        int tmpColBaseIndex;

        for (int i = tmpDiagDim - 1; i >= 0; i--) {

            for (int j = i; j < tmpDiagDim; j++) {
                tmpBodyRow[j] = conjugated ? aBody.get(j, i) : aBody.get(i, j);
            }

            for (int s = aFirstCol; s < aColLimit; s++) {

                tmpColBaseIndex = s * aRowDim;

                tmpVal = BigMath.ZERO;
                for (int j = i + 1; j < tmpDiagDim; j++) {
                    tmpVal = tmpVal.add(tmpBodyRow[j].multiply(aData[j + tmpColBaseIndex]));
                }
                tmpVal = aData[i + tmpColBaseIndex].subtract(tmpVal);

                aData[i + tmpColBaseIndex] = BigFunction.DIVIDE.invoke(tmpVal, tmpBodyRow[i]);
            }
        }
    }

    public static void invoke(final ComplexNumber[] aData, final int aRowDim, final int aFirstCol, final int aColLimit, final Access2D<ComplexNumber> aBody,
            final boolean conjugated) {

        final int tmpDiagDim = (int) Math.min(aBody.countRows(), aBody.countColumns());
        final ComplexNumber[] tmpBodyRow = new ComplexNumber[tmpDiagDim];
        ComplexNumber tmpVal;
        int tmpColBaseIndex;

        for (int i = tmpDiagDim - 1; i >= 0; i--) {

            for (int j = i; j < tmpDiagDim; j++) {
                tmpBodyRow[j] = conjugated ? aBody.get(j, i).conjugate() : aBody.get(i, j);
            }

            for (int s = aFirstCol; s < aColLimit; s++) {

                tmpColBaseIndex = s * aRowDim;

                tmpVal = ComplexNumber.ZERO;
                for (int j = i + 1; j < tmpDiagDim; j++) {
                    tmpVal = tmpVal.add(tmpBodyRow[j].multiply(aData[j + tmpColBaseIndex]));
                }
                tmpVal = aData[i + tmpColBaseIndex].subtract(tmpVal);

                aData[i + tmpColBaseIndex] = tmpVal.divide(tmpBodyRow[i]);
            }
        }
    }

    public static void invoke(final double[] aData, final int aRowDim, final int aFirstCol, final int aColLimit, final Access2D<Double> aBody,
            final boolean conjugated) {

        final int tmpDiagDim = (int) Math.min(aBody.countRows(), aBody.countColumns());
        final double[] tmpBodyRow = new double[tmpDiagDim];
        double tmpVal;
        int tmpColBaseIndex;

        for (int i = tmpDiagDim - 1; i >= 0; i--) {

            for (int j = i; j < tmpDiagDim; j++) {
                tmpBodyRow[j] = conjugated ? aBody.doubleValue(j, i) : aBody.doubleValue(i, j);
            }

            for (int s = aFirstCol; s < aColLimit; s++) {
                tmpColBaseIndex = s * aRowDim;

                tmpVal = PrimitiveMath.ZERO;
                for (int j = i + 1; j < tmpDiagDim; j++) {
                    tmpVal += tmpBodyRow[j] * aData[j + tmpColBaseIndex];
                }
                tmpVal = aData[i + tmpColBaseIndex] - tmpVal;

                aData[i + tmpColBaseIndex] = tmpVal / tmpBodyRow[i];
            }
        }
    }

    private SubstituteBackwards() {
        super();
    }

}
