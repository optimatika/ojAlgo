/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.RationalNumber;

public abstract class DotProduct extends MatrixOperation {

    public static BigDecimal invoke(final BigDecimal[] array1, final int offset1, final BigDecimal[] array2, final int offset2, final int first, final int limit) {
        BigDecimal retVal = BigMath.ZERO;
        for (int i = first; i < limit; i++) {
            retVal = retVal.add(array1[offset1 + i].multiply(array2[offset2 + i]));
        }
        return retVal;
    }

    public static ComplexNumber invoke(final ComplexNumber[] array1, final int offset1, final ComplexNumber[] array2, final int offset2, final int first,
            final int limit) {
        ComplexNumber retVal = ComplexNumber.ZERO;
        for (int i = first; i < limit; i++) {
            retVal = retVal.add(array1[offset1 + i].multiply(array2[offset2 + i]));
        }
        return retVal;
    }

    public static double invoke(final double[] array1, final int offset1, final double[] array2, final int offset2, final int first, final int limit) {
        double retVal = PrimitiveMath.ZERO;
        for (int i = first; i < limit; i++) {
            retVal += array1[offset1 + i] * array2[offset2 + i];
        }
        return retVal;
    }

    public static RationalNumber invoke(final RationalNumber[] array1, final int offset1, final RationalNumber[] array2, final int offset2, final int limit,
            final int first) {
        RationalNumber retVal = RationalNumber.ZERO;
        for (int i = first; i < limit; i++) {
            retVal = retVal.add(array1[offset1 + i].multiply(array2[offset2 + i]));
        }
        return retVal;
    }

    private DotProduct() {
        super();
    }

}
