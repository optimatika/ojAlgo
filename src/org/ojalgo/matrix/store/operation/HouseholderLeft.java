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

import org.ojalgo.constant.BigMath;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BigFunction;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.scalar.ComplexNumber;

public final class HouseholderLeft extends MatrixOperation {

    public static int THRESHOLD = 128;

    public static void invoke(final BigDecimal[] aData, final int aRowDim, final int aFirstCol, final int aColLimit, final Householder.Big aHouseholder) {

        final BigDecimal[] tmpHouseholderVector = aHouseholder.vector;
        final int tmpFirstNonZero = aHouseholder.first;
        final BigDecimal tmpBeta = aHouseholder.beta;

        BigDecimal tmpScale;
        int tmpIndex;
        for (int j = aFirstCol; j < aColLimit; j++) {
            tmpScale = BigMath.ZERO;
            tmpIndex = tmpFirstNonZero + (j * aRowDim);
            for (int i = tmpFirstNonZero; i < aRowDim; i++) {
                tmpScale = BigFunction.ADD.invoke(tmpScale, BigFunction.MULTIPLY.invoke(tmpHouseholderVector[i], aData[tmpIndex++]));
            }
            tmpScale = BigFunction.MULTIPLY.invoke(tmpScale, tmpBeta);
            tmpIndex = tmpFirstNonZero + (j * aRowDim);
            for (int i = tmpFirstNonZero; i < aRowDim; i++) {
                aData[tmpIndex] = BigFunction.SUBTRACT.invoke(aData[tmpIndex], BigFunction.MULTIPLY.invoke(tmpScale, tmpHouseholderVector[i]));
                tmpIndex++;
            }
        }
    }

    public static void invoke(final ComplexNumber[] aData, final int aRowDim, final int aFirstCol, final int aColLimit, final Householder.Complex aHouseholder) {

        final ComplexNumber[] tmpHouseholderVector = aHouseholder.vector;
        final int tmpFirstNonZero = aHouseholder.first;
        final ComplexNumber tmpBeta = aHouseholder.beta;

        ComplexNumber tmpScale;
        int tmpIndex;
        for (int j = aFirstCol; j < aColLimit; j++) {
            tmpScale = ComplexNumber.ZERO;
            tmpIndex = tmpFirstNonZero + (j * aRowDim);
            for (int i = tmpFirstNonZero; i < aRowDim; i++) {
                tmpScale = tmpScale.add(tmpHouseholderVector[i].conjugate().multiply(aData[tmpIndex++]));
            }
            tmpScale = tmpScale.multiply(tmpBeta);
            tmpIndex = tmpFirstNonZero + (j * aRowDim);
            for (int i = tmpFirstNonZero; i < aRowDim; i++) {
                aData[tmpIndex] = aData[tmpIndex].subtract(tmpScale.multiply(tmpHouseholderVector[i]));
                tmpIndex++;
            }
        }
    }

    public static void invoke(final double[] aData, final int aRowDim, final int aFirstCol, final int aColLimit, final Householder.Primitive aHouseholder) {

        final double[] tmpHouseholderVector = aHouseholder.vector;
        final int tmpFirstNonZero = aHouseholder.first;
        final double tmpBeta = aHouseholder.beta;

        double tmpScale;
        int tmpIndex;
        for (int j = aFirstCol; j < aColLimit; j++) {
            tmpScale = PrimitiveMath.ZERO;
            tmpIndex = tmpFirstNonZero + (j * aRowDim);
            for (int i = tmpFirstNonZero; i < aRowDim; i++) {
                tmpScale += tmpHouseholderVector[i] * aData[tmpIndex++];
            }
            tmpScale *= tmpBeta;
            tmpIndex = tmpFirstNonZero + (j * aRowDim);
            for (int i = tmpFirstNonZero; i < aRowDim; i++) {
                aData[tmpIndex++] -= tmpScale * tmpHouseholderVector[i];
            }
        }
    }

    private HouseholderLeft() {
        super();
    }

}
