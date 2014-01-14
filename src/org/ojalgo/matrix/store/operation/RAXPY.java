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

import org.ojalgo.function.BigFunction;
import org.ojalgo.scalar.ComplexNumber;

public final class RAXPY extends MatrixOperation {

    public static int THRESHOLD = 128;

    public static void invoke(final BigDecimal[] aData, final int aDataRow, final BigDecimal[] aMultipliers, final int aMultiplierRow,
            final BigDecimal aScalar, final int aFirst, final int aLimit) {

        final int tmpRowDim = aData.length / aLimit;

        int tmpDataIndex = aDataRow + (aFirst * tmpRowDim);
        int tmpMultiplierIndex = aMultiplierRow + (aFirst * tmpRowDim);

        for (int i = aFirst; i < aLimit; i++) {
            aData[tmpDataIndex] = BigFunction.ADD.invoke(BigFunction.MULTIPLY.invoke(aScalar, aMultipliers[tmpMultiplierIndex]), aData[tmpDataIndex]);
            tmpDataIndex += tmpRowDim;
            tmpMultiplierIndex += tmpRowDim;
        }
    }

    public static void invoke(final ComplexNumber[] aData, final int aDataRow, final ComplexNumber[] aMultipliers, final int aMultiplierRow,
            final ComplexNumber aScalar, final int aFirst, final int aLimit) {

        final int tmpRowDim = aData.length / aLimit;

        int tmpDataIndex = aDataRow + (aFirst * tmpRowDim);
        int tmpMultiplierIndex = aMultiplierRow + (aFirst * tmpRowDim);

        for (int i = aFirst; i < aLimit; i++) {
            aData[tmpDataIndex] = aScalar.multiply(aMultipliers[tmpMultiplierIndex]).add(aData[tmpDataIndex]);
            tmpDataIndex += tmpRowDim;
            tmpMultiplierIndex += tmpRowDim;
        }
    }

    public static void invoke(final double[] aData, final int aDataRow, final double[] aMultipliers, final int aMultiplierRow, final double aScalar,
            final int aFirst, final int aLimit) {

        final int tmpRowDim = aData.length / aLimit;

        int tmpDataIndex = aDataRow + (aFirst * tmpRowDim);
        int tmpMultiplierIndex = aMultiplierRow + (aFirst * tmpRowDim);

        for (int i = aFirst; i < aLimit; i++) {
            aData[tmpDataIndex] += aScalar * aMultipliers[tmpMultiplierIndex];
            tmpDataIndex += tmpRowDim;
            tmpMultiplierIndex += tmpRowDim;
        }
    }

    private RAXPY() {
        super();
    }

}
