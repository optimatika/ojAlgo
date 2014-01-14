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

import org.ojalgo.function.BigFunction;
import org.ojalgo.scalar.ComplexNumber;

/**
 * <b>c</b>olumn <b>a</b> <b>x</b> <b>p</b>lus <b>y</b>
 * 
 * @param aData y-data
 * @param aBaseDataIndex y-column base index
 * @param aMultipliers x-data
 * @param aBaseMultiplierIndex x-column base index
 * @param aScalar a
 * @param first First index
 * @param aLimit Index limit 
 */
public final class CAXPY extends MatrixOperation {

    public static int THRESHOLD = Integer.MAX_VALUE;

    public static void invoke(final BigDecimal[] aData, final int aBaseDataIndex, final BigDecimal[] aMultipliers, final int aBaseMultiplierIndex,
            final BigDecimal aScalar, final int aFirst, final int aLimit) {
        for (int i = aFirst; i < aLimit; i++) {
            aData[aBaseDataIndex + i] = BigFunction.ADD.invoke(BigFunction.MULTIPLY.invoke(aScalar, aMultipliers[aBaseMultiplierIndex + i]),
                    aData[aBaseDataIndex + i]); // y += ax
        }
    }

    public static void invoke(final ComplexNumber[] aData, final int aBaseDataIndex, final ComplexNumber[] aMultipliers, final int aBaseMultiplierIndex,
            final ComplexNumber aScalar, final int aFirst, final int aLimit) {
        for (int i = aFirst; i < aLimit; i++) {
            aData[aBaseDataIndex + i] = aScalar.multiply(aMultipliers[aBaseMultiplierIndex + i]).add(aData[aBaseDataIndex + i]); // y += ax
        }
    }

    public static void invoke(final double[] aData, final int aBaseDataIndex, final double[] aMultipliers, final int aBaseMultiplierIndex,
            final double aScalar, final int aFirst, final int aLimit) {
        for (int i = aFirst; i < aLimit; i++) {
            aData[aBaseDataIndex + i] += aScalar * aMultipliers[aBaseMultiplierIndex + i]; // y += ax
        }
    }

    private CAXPY() {
        super();
    }

}
