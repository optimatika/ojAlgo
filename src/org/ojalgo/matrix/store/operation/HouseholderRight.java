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

import org.ojalgo.constant.BigMath;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BigFunction;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.scalar.ComplexNumber;

public final class HouseholderRight extends MatrixOperation {

    public static int THRESHOLD = 128;

    public static void invoke(final BigDecimal[] aData, final int aFirstRow, final int aRowLimit, final int aColDim, final Householder.Big aHouseholder) {

        final BigDecimal[] tmpVector = aHouseholder.vector;
        final int tmpFirst = aHouseholder.first;
        final BigDecimal tmpBeta = aHouseholder.beta;

        final int tmpRowDim = aData.length / aColDim;

        BigDecimal tmpScale;
        int tmpIndex;
        for (int i = aFirstRow; i < aRowLimit; i++) {
            tmpScale = BigMath.ZERO;
            tmpIndex = i + (tmpFirst * tmpRowDim);
            for (int j = tmpFirst; j < aColDim; j++) {
                tmpScale = BigFunction.ADD.invoke(tmpScale, BigFunction.MULTIPLY.invoke(tmpVector[j], aData[tmpIndex]));
                tmpIndex += tmpRowDim;
            }
            tmpScale = BigFunction.MULTIPLY.invoke(tmpScale, tmpBeta);
            tmpIndex = i + (tmpFirst * tmpRowDim);
            for (int j = tmpFirst; j < aColDim; j++) {
                aData[tmpIndex] = BigFunction.SUBTRACT.invoke(aData[tmpIndex], BigFunction.MULTIPLY.invoke(tmpScale, tmpVector[j]));
                tmpIndex += tmpRowDim;
            }
        }
    }

    public static void invoke(final ComplexNumber[] aData, final int aFirstRow, final int aRowLimit, final int aColDim, final Householder.Complex aHouseholder) {

        final ComplexNumber[] tmpVector = aHouseholder.vector;
        final int tmpFirst = aHouseholder.first;
        final ComplexNumber tmpBeta = aHouseholder.beta;

        final int tmpRowDim = aData.length / aColDim;

        ComplexNumber tmpScale;
        int tmpIndex;
        for (int i = aFirstRow; i < aRowLimit; i++) {
            tmpScale = ComplexNumber.ZERO;
            tmpIndex = i + (tmpFirst * tmpRowDim);
            for (int j = tmpFirst; j < aColDim; j++) {
                tmpScale = tmpScale.add(tmpVector[j].conjugate().multiply(aData[tmpIndex].conjugate()));
                tmpIndex += tmpRowDim;
            }
            tmpScale = tmpScale.multiply(tmpBeta);
            tmpIndex = i + (tmpFirst * tmpRowDim);
            for (int j = tmpFirst; j < aColDim; j++) {
                aData[tmpIndex] = aData[tmpIndex].conjugate().subtract(tmpScale.multiply(tmpVector[j])).conjugate();
                tmpIndex += tmpRowDim;
            }
        }
    }

    public static void invoke(final double[] aData, final int aFirstRow, final int aRowLimit, final int aColDim, final Householder.Primitive aHouseholder) {

        final double[] tmpVector = aHouseholder.vector;
        final int tmpFirst = aHouseholder.first;
        final double tmpBeta = aHouseholder.beta;

        final int tmpRowDim = aData.length / aColDim;

        double tmpScale;
        int tmpIndex;
        for (int i = aFirstRow; i < aRowLimit; i++) {
            tmpScale = PrimitiveMath.ZERO;
            tmpIndex = i + (tmpFirst * tmpRowDim);
            for (int j = tmpFirst; j < aColDim; j++) {
                tmpScale += tmpVector[j] * aData[tmpIndex];
                tmpIndex += tmpRowDim;
            }
            tmpScale *= tmpBeta;
            tmpIndex = i + (tmpFirst * tmpRowDim);
            for (int j = tmpFirst; j < aColDim; j++) {
                aData[tmpIndex] -= tmpScale * tmpVector[j];
                tmpIndex += tmpRowDim;
            }
        }
    }

    private HouseholderRight() {
        super();
    }

}
