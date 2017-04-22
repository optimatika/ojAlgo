/*
 * Copyright 1997-2017 Optimatika (www.optimatika.se)
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

public final class RotateRight extends MatrixOperation {

    public static final RotateRight SETUP = new RotateRight();

    public static int THRESHOLD = 128;

    public static void invoke(final BigDecimal[] aData, final int aRowDim, final int aColA, final int aColB, final BigDecimal aCos, final BigDecimal aSin) {

        BigDecimal tmpOldA;
        BigDecimal tmpOldB;

        int tmpIndexA = aColA * aRowDim;
        int tmpIndexB = aColB * aRowDim;

        for (int i = 0; i < aRowDim; i++) {

            tmpOldA = aData[tmpIndexA];
            tmpOldB = aData[tmpIndexB];

            aData[tmpIndexA] = BigFunction.SUBTRACT.invoke(BigFunction.MULTIPLY.invoke(aCos, tmpOldA), BigFunction.MULTIPLY.invoke(aSin, tmpOldB));
            aData[tmpIndexB] = BigFunction.ADD.invoke(BigFunction.MULTIPLY.invoke(aCos, tmpOldB), BigFunction.MULTIPLY.invoke(aSin, tmpOldA));

            tmpIndexA++;
            tmpIndexB++;
        }
    }

    public static void invoke(final ComplexNumber[] aData, final int aRowDim, final int aColA, final int aColB, final ComplexNumber aCos,
            final ComplexNumber aSin) {

        ComplexNumber tmpOldA;
        ComplexNumber tmpOldB;

        int tmpIndexA = aColA * aRowDim;
        int tmpIndexB = aColB * aRowDim;

        for (int i = 0; i < aRowDim; i++) {

            tmpOldA = aData[tmpIndexA];
            tmpOldB = aData[tmpIndexB];

            aData[tmpIndexA] = aCos.multiply(tmpOldA).subtract(aSin.multiply(tmpOldB));
            aData[tmpIndexB] = aCos.multiply(tmpOldB).add(aSin.multiply(tmpOldA));

            tmpIndexA++;
            tmpIndexB++;
        }
    }

    public static void invoke(final double[] aData, final int aRowDim, final int aColA, final int aColB, final double aCos, final double aSin) {

        double tmpOldA;
        double tmpOldB;

        int tmpIndexA = aColA * aRowDim;
        int tmpIndexB = aColB * aRowDim;

        for (int i = 0; i < aRowDim; i++) {

            tmpOldA = aData[tmpIndexA];
            tmpOldB = aData[tmpIndexB];

            aData[tmpIndexA] = (aCos * tmpOldA) - (aSin * tmpOldB);
            aData[tmpIndexB] = (aCos * tmpOldB) + (aSin * tmpOldA);

            tmpIndexA++;
            tmpIndexB++;
        }
    }

    private RotateRight() {
        super();
    }

    @Override
    public int threshold() {
        return THRESHOLD;
    }

}
