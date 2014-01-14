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
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.ComplexNumber;

public final class MAXPY extends MatrixOperation {

    public static int THRESHOLD = 128;

    public static void invoke(final BigDecimal[] aData, final int aRowDim, final int aFirstCol, final int aColLimit, final BigDecimal aScale,
            final MatrixStore<BigDecimal> aStore) {
        int tmpIndex = aRowDim * aFirstCol;
        for (int j = aFirstCol; j < aColLimit; j++) {
            for (int i = 0; i < aRowDim; i++) {
                aData[tmpIndex] = BigFunction.ADD.invoke(BigFunction.MULTIPLY.invoke(aScale, aStore.get(i, j)), aData[tmpIndex]);
                tmpIndex++;
            }
        }
    }

    public static void invoke(final ComplexNumber[] aData, final int aRowDim, final int aFirstCol, final int aColLimit, final ComplexNumber aScale,
            final MatrixStore<ComplexNumber> aStore) {
        int tmpIndex = aRowDim * aFirstCol;
        for (int j = aFirstCol; j < aColLimit; j++) {
            for (int i = 0; i < aRowDim; i++) {
                aData[tmpIndex] = aScale.multiply(aStore.get(i, j)).add(aData[tmpIndex]);
                tmpIndex++;
            }
        }
    }

    public static void invoke(final double[] aData, final int aRowDim, final int aFirstCol, final int aColLimit, final double aScale,
            final MatrixStore<Double> aStore) {
        int tmpIndex = aRowDim * aFirstCol;
        for (int j = aFirstCol; j < aColLimit; j++) {
            for (int i = 0; i < aRowDim; i++) {
                aData[tmpIndex++] += aScale * aStore.doubleValue(i, j);
            }
        }
    }

    private MAXPY() {
        super();
    }

}
