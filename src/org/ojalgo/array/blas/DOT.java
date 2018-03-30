/*
 * Copyright 1997-2018 Optimatika
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
package org.ojalgo.array.blas;

import java.math.BigDecimal;

import org.ojalgo.constant.BigMath;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;

/**
 * The ?dot routines perform a vector-vector reduction operation defined as Equation where xi and yi are
 * elements of vectors x and y.
 *
 * @author apete
 */
public abstract class DOT implements BLAS1 {

    public static BigDecimal invoke(final BigDecimal[] array1, final int offset1, final BigDecimal[] array2, final int offset2, final int first,
            final int limit) {
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

    public static <N extends Number & Scalar<N>> N invoke(final N[] array1, final int offset1, final N[] array2, final int offset2, final int first,
            final int limit, final Scalar.Factory<N> factory) {
        Scalar<N> retVal = factory.zero();
        for (int i = first; i < limit; i++) {
            retVal = retVal.add(array1[offset1 + i].multiply(array2[offset2 + i]));
        }
        return retVal.get();
    }

    public static double invoke2(final double[] array1, final int offset1, final double[] array2, final int offset2, final int first, final int limit) {

        double retVal = PrimitiveMath.ZERO;

        int i = first;

        for (; (i + 8) < limit; i += 8) {

            final int base1 = offset1 + i;
            final int base2 = offset2 + i;

            final double m0 = array1[base1] * array2[base2];
            final double m1 = array1[base1 + 1] * array2[base2 + 1];
            final double m2 = array1[base1 + 2] * array2[base2 + 2];
            final double m3 = array1[base1 + 3] * array2[base2 + 3];
            final double m4 = array1[base1 + 4] * array2[base2 + 4];
            final double m5 = array1[base1 + 5] * array2[base2 + 5];
            final double m6 = array1[base1 + 6] * array2[base2 + 6];
            final double m7 = array1[base1 + 7] * array2[base2 + 7];

            final double s0 = m0 + m1;
            final double s1 = m2 + m3;
            final double s2 = m4 + m5;
            final double s3 = m6 + m7;

            final double a0 = s0 + s1;
            final double a1 = s2 + s3;

            retVal += (a0 + a1);
        }

        switch ((limit - first) % 8) {

        case 7: {

            final int base1 = offset1 + i;
            final int base2 = offset2 + i;

            final double m0 = array1[base1] * array2[base2];
            final double m1 = array1[base1 + 1] * array2[base2 + 1];
            final double m2 = array1[base1 + 2] * array2[base2 + 2];
            final double m3 = array1[base1 + 3] * array2[base2 + 3];
            final double m4 = array1[base1 + 4] * array2[base2 + 4];
            final double m5 = array1[base1 + 5] * array2[base2 + 5];
            final double m6 = array1[base1 + 6] * array2[base2 + 6];

            final double s0 = m0 + m1;
            final double s1 = m2 + m3;
            final double s2 = m4 + m5;
            final double s3 = m6;

            final double a0 = s0 + s1;
            final double a1 = s2 + s3;

            retVal += (a0 + a1);
        }

            break;

        case 6: {

            final int base1 = offset1 + i;
            final int base2 = offset2 + i;

            final double m0 = array1[base1] * array2[base2];
            final double m1 = array1[base1 + 1] * array2[base2 + 1];
            final double m2 = array1[base1 + 2] * array2[base2 + 2];
            final double m3 = array1[base1 + 3] * array2[base2 + 3];
            final double m4 = array1[base1 + 4] * array2[base2 + 4];
            final double m5 = array1[base1 + 5] * array2[base2 + 5];

            final double s0 = m0 + m1;
            final double s1 = m2 + m3;
            final double s2 = m4 + m5;

            final double a0 = s0 + s1;
            final double a1 = s2;

            retVal += (a0 + a1);
        }

            break;

        case 5: {

            final int base1 = offset1 + i;
            final int base2 = offset2 + i;

            final double m0 = array1[base1] * array2[base2];
            final double m1 = array1[base1 + 1] * array2[base2 + 1];
            final double m2 = array1[base1 + 2] * array2[base2 + 2];
            final double m3 = array1[base1 + 3] * array2[base2 + 3];
            final double m4 = array1[base1 + 4] * array2[base2 + 4];

            final double s0 = m0 + m1;
            final double s1 = m2 + m3;
            final double s2 = m4;

            final double a0 = s0 + s1;
            final double a1 = s2;

            retVal += (a0 + a1);
        }

            break;

        case 4: {

            final int base1 = offset1 + i;
            final int base2 = offset2 + i;

            final double m0 = array1[base1] * array2[base2];
            final double m1 = array1[base1 + 1] * array2[base2 + 1];
            final double m2 = array1[base1 + 2] * array2[base2 + 2];
            final double m3 = array1[base1 + 3] * array2[base2 + 3];

            final double s0 = m0 + m1;
            final double s1 = m2 + m3;

            final double a0 = s0 + s1;

            retVal += a0;
        }

            break;

        case 3: {

            final int base1 = offset1 + i;
            final int base2 = offset2 + i;

            final double m0 = array1[base1] * array2[base2];
            final double m1 = array1[base1 + 1] * array2[base2 + 1];
            final double m2 = array1[base1 + 2] * array2[base2 + 2];

            final double s0 = m0 + m1;
            final double s1 = m2;

            final double a0 = s0 + s1;

            retVal += a0;
        }

            break;

        case 2: {

            final int base1 = offset1 + i;
            final int base2 = offset2 + i;

            final double m0 = array1[base1] * array2[base2];
            final double m1 = array1[base1 + 1] * array2[base2 + 1];

            final double s0 = m0 + m1;

            final double a0 = s0;

            retVal += a0;
        }

            break;

        case 1: {

            final int base1 = offset1 + i;
            final int base2 = offset2 + i;

            final double m0 = array1[base1] * array2[base2];

            final double s0 = m0;

            final double a0 = s0;

            retVal += a0;
        }

            break;

        default:

            throw new IllegalStateException();
        }

        return retVal;
    }

}
