/*
 * Copyright 1997-2024 Optimatika
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
package org.ojalgo.array.operation;

import java.math.BigDecimal;

import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;

/**
 * The ?dot routines perform a vector-vector reduction operation defined as Equation where xi and yi are
 * elements of vectors x and y.
 *
 * @author apete
 */
public abstract class DOT implements ArrayOperation {

    public static int THRESHOLD = 128;

    public static double invoke(final Access1D<?> array1, final int offset1, final double[] array2, final int offset2, final int first, final int limit) {
        double retVal = PrimitiveMath.ZERO;
        for (int i = first; i < limit; i++) {
            retVal += array1.doubleValue(offset1 + i) * array2[offset2 + i];
        }
        return retVal;
    }

    public static float invoke(final Access1D<?> array1, final int offset1, final float[] array2, final int offset2, final int first, final int limit) {
        float retVal = 0F;
        for (int i = first; i < limit; i++) {
            retVal += array1.floatValue(offset1 + i) * array2[offset2 + i];
        }
        return retVal;
    }

    public static <N extends Scalar<N>> N invoke(final Access1D<N> array2, final int offset2, final N[] array1, final int offset1, final int first,
            final int limit, final Scalar.Factory<N> factory) {
        return DOT.invoke(array1, offset1, array2, offset2, first, limit, factory);
    }

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

    public static double invoke(final double[] array1, final int offset1, final Access1D<?> array2, final int offset2, final int first, final int limit) {
        double retVal = PrimitiveMath.ZERO;
        for (int i = first; i < limit; i++) {
            retVal += array1[offset1 + i] * array2.doubleValue(offset2 + i);
        }
        return retVal;
    }

    public static double invoke(final double[] array1, final int offset1, final double[] array2, final int offset2, final int first, final int limit) {
        return DOT.unrolled04(array1, offset1, array2, offset2, first, limit);
    }

    public static float invoke(final float[] array1, final int offset1, final Access1D<?> array2, final int offset2, final int first, final int limit) {
        float retVal = 0F;
        for (int i = first; i < limit; i++) {
            retVal += array1[offset1 + i] * array2.floatValue(offset2 + i);
        }
        return retVal;
    }

    public static float invoke(final float[] array1, final int offset1, final float[] array2, final int offset2, final int first, final int limit) {
        return DOT.unrolled04(array1, offset1, array2, offset2, first, limit);
    }

    public static <N extends Scalar<N>> N invoke(final N[] array1, final int offset1, final Access1D<N> array2, final int offset2, final int first,
            final int limit, final Scalar.Factory<N> factory) {
        Scalar<N> retVal = factory.zero();
        for (int i = first; i < limit; i++) {
            retVal = retVal.add(array1[offset1 + i].multiply(array2.get(offset2 + i)));
        }
        return retVal.get();
    }

    public static <N extends Scalar<N>> N invoke(final N[] array1, final int offset1, final N[] array2, final int offset2, final int first, final int limit,
            final Scalar.Factory<N> factory) {
        Scalar<N> retVal = factory.zero();
        for (int i = first; i < limit; i++) {
            retVal = retVal.add(array1[offset1 + i].multiply(array2[offset2 + i]));
        }
        return retVal.get();
    }

    public static <N extends Scalar<N>> N invokeG(final Access1D<N> array1, final int offset1, final Access1D<N> array2, final int offset2, final int first,
            final int limit, final Scalar.Factory<N> scalar) {
        Scalar<N> retVal = scalar.zero();
        for (int i = first; i < limit; i++) {
            retVal = retVal.add(array1.get(offset1 + i).multiply(array2.get(offset2 + i)));
        }
        return retVal.get();
    }

    public static double invokeP64(final Access1D<?> array1, final int offset1, final Access1D<?> array2, final int offset2, final int first, final int limit) {
        double retVal = PrimitiveMath.ZERO;
        for (int i = first; i < limit; i++) {
            retVal += array1.doubleValue(offset1 + i) * array2.doubleValue(offset2 + i);
        }
        return retVal;
    }

    static double plain(final double[] array1, final int offset1, final double[] array2, final int offset2, final int first, final int limit) {

        double retVal = 0F;
        for (int i = first; i < limit; i++) {
            retVal += array1[offset1 + i] * array2[offset2 + i];
        }
        return retVal;
    }

    static float plain(final float[] array1, final int offset1, final float[] array2, final int offset2, final int first, final int limit) {

        float retVal = 0F;
        for (int i = first; i < limit; i++) {
            retVal += array1[offset1 + i] * array2[offset2 + i];
        }
        return retVal;
    }

    static float unrolled02(final float[] array1, final int offset1, final float[] array2, final int offset2, final int first, final int limit) {

        int remainder = (limit - first) % 2;

        float sum0 = 0F;
        float sum1 = 0F;

        int shift10 = offset1 + 0;
        int shift11 = offset1 + 1;

        int shift20 = offset2 + 0;
        int shift21 = offset2 + 1;

        int i = first;
        for (int lim = limit - remainder; i < lim; i += 2) {
            sum0 += array1[shift10 + i] * array2[shift20 + i];
            sum1 += array1[shift11 + i] * array2[shift21 + i];
        }
        for (; i < limit; i++) {
            sum0 += array1[shift10 + i] * array2[shift20 + i];
        }

        return sum0 + sum1;
    }

    static double unrolled04(final double[] array1, final int offset1, final double[] array2, final int offset2, final int first, final int limit) {

        int remainder = (limit - first) % 4;

        double sum0 = 0F;
        double sum1 = 0F;
        double sum2 = 0F;
        double sum3 = 0F;

        int shift10 = offset1 + 0;
        int shift11 = offset1 + 1;
        int shift12 = offset1 + 2;
        int shift13 = offset1 + 3;

        int shift20 = offset2 + 0;
        int shift21 = offset2 + 1;
        int shift22 = offset2 + 2;
        int shift23 = offset2 + 3;

        int i = first;
        for (int lim = limit - remainder; i < lim; i += 4) {
            sum0 += array1[shift10 + i] * array2[shift20 + i];
            sum1 += array1[shift11 + i] * array2[shift21 + i];
            sum2 += array1[shift12 + i] * array2[shift22 + i];
            sum3 += array1[shift13 + i] * array2[shift23 + i];
        }
        for (; i < limit; i++) {
            sum0 += array1[shift10 + i] * array2[shift20 + i];
        }

        return sum0 + sum1 + sum2 + sum3;
    }

    static float unrolled04(final float[] array1, final int offset1, final float[] array2, final int offset2, final int first, final int limit) {

        int remainder = (limit - first) % 4;

        float sum0 = 0F;
        float sum1 = 0F;
        float sum2 = 0F;
        float sum3 = 0F;

        int shift10 = offset1 + 0;
        int shift11 = offset1 + 1;
        int shift12 = offset1 + 2;
        int shift13 = offset1 + 3;

        int shift20 = offset2 + 0;
        int shift21 = offset2 + 1;
        int shift22 = offset2 + 2;
        int shift23 = offset2 + 3;

        int i = first;
        for (int lim = limit - remainder; i < lim; i += 4) {
            sum0 += array1[shift10 + i] * array2[shift20 + i];
            sum1 += array1[shift11 + i] * array2[shift21 + i];
            sum2 += array1[shift12 + i] * array2[shift22 + i];
            sum3 += array1[shift13 + i] * array2[shift23 + i];
        }
        for (; i < limit; i++) {
            sum0 += array1[shift10 + i] * array2[shift20 + i];
        }

        return sum0 + sum1 + sum2 + sum3;
    }

    static float unrolled08(final float[] array1, final int offset1, final float[] array2, final int offset2, final int first, final int limit) {

        int remainder = (limit - first) % 8;

        float sum0 = 0F;
        float sum1 = 0F;
        float sum2 = 0F;
        float sum3 = 0F;
        float sum4 = 0F;
        float sum5 = 0F;
        float sum6 = 0F;
        float sum7 = 0F;

        int shift10 = offset1 + 0;
        int shift11 = offset1 + 1;
        int shift12 = offset1 + 2;
        int shift13 = offset1 + 3;
        int shift14 = offset1 + 4;
        int shift15 = offset1 + 5;
        int shift16 = offset1 + 6;
        int shift17 = offset1 + 7;

        int shift20 = offset2 + 0;
        int shift21 = offset2 + 1;
        int shift22 = offset2 + 2;
        int shift23 = offset2 + 3;
        int shift24 = offset2 + 4;
        int shift25 = offset2 + 5;
        int shift26 = offset2 + 6;
        int shift27 = offset2 + 7;

        int i = first;
        for (int lim = limit - remainder; i < lim; i += 8) {
            sum0 += array1[shift10 + i] * array2[shift20 + i];
            sum1 += array1[shift11 + i] * array2[shift21 + i];
            sum2 += array1[shift12 + i] * array2[shift22 + i];
            sum3 += array1[shift13 + i] * array2[shift23 + i];
            sum4 += array1[shift14 + i] * array2[shift24 + i];
            sum5 += array1[shift15 + i] * array2[shift25 + i];
            sum6 += array1[shift16 + i] * array2[shift26 + i];
            sum7 += array1[shift17 + i] * array2[shift27 + i];
        }
        for (; i < limit; i++) {
            sum0 += array1[shift10 + i] * array2[shift20 + i];
        }

        return sum0 + sum1 + sum2 + sum3 + sum4 + sum5 + sum6 + sum7;
    }

    static float unrolled16(final float[] array1, final int offset1, final float[] array2, final int offset2, final int first, final int limit) {

        int remainder = (limit - first) % 16;

        float sum00 = 0F;
        float sum01 = 0F;
        float sum02 = 0F;
        float sum03 = 0F;
        float sum04 = 0F;
        float sum05 = 0F;
        float sum06 = 0F;
        float sum07 = 0F;
        float sum08 = 0F;
        float sum09 = 0F;
        float sum10 = 0F;
        float sum11 = 0F;
        float sum12 = 0F;
        float sum13 = 0F;
        float sum14 = 0F;
        float sum15 = 0F;

        int shift100 = offset1 + 0;
        int shift101 = offset1 + 1;
        int shift102 = offset1 + 2;
        int shift103 = offset1 + 3;
        int shift104 = offset1 + 4;
        int shift105 = offset1 + 5;
        int shift106 = offset1 + 6;
        int shift107 = offset1 + 7;
        int shift108 = offset1 + 8;
        int shift109 = offset1 + 9;
        int shift110 = offset1 + 10;
        int shift111 = offset1 + 11;
        int shift112 = offset1 + 12;
        int shift113 = offset1 + 13;
        int shift114 = offset1 + 14;
        int shift115 = offset1 + 15;

        int shift200 = offset2 + 0;
        int shift201 = offset2 + 1;
        int shift202 = offset2 + 2;
        int shift203 = offset2 + 3;
        int shift204 = offset2 + 4;
        int shift205 = offset2 + 5;
        int shift206 = offset2 + 6;
        int shift207 = offset2 + 7;
        int shift208 = offset2 + 8;
        int shift209 = offset2 + 9;
        int shift210 = offset2 + 10;
        int shift211 = offset2 + 11;
        int shift212 = offset2 + 12;
        int shift213 = offset2 + 13;
        int shift214 = offset2 + 14;
        int shift215 = offset2 + 15;

        int i = first;
        for (int lim = limit - remainder; i < lim; i += 16) {
            sum00 += array1[shift100 + i] * array2[shift200 + i];
            sum01 += array1[shift101 + i] * array2[shift201 + i];
            sum02 += array1[shift102 + i] * array2[shift202 + i];
            sum03 += array1[shift103 + i] * array2[shift203 + i];
            sum04 += array1[shift104 + i] * array2[shift204 + i];
            sum05 += array1[shift105 + i] * array2[shift205 + i];
            sum06 += array1[shift106 + i] * array2[shift206 + i];
            sum07 += array1[shift107 + i] * array2[shift207 + i];
            sum08 += array1[shift108 + i] * array2[shift208 + i];
            sum09 += array1[shift109 + i] * array2[shift209 + i];
            sum10 += array1[shift110 + i] * array2[shift210 + i];
            sum11 += array1[shift111 + i] * array2[shift211 + i];
            sum12 += array1[shift112 + i] * array2[shift212 + i];
            sum13 += array1[shift113 + i] * array2[shift213 + i];
            sum14 += array1[shift114 + i] * array2[shift214 + i];
            sum15 += array1[shift115 + i] * array2[shift215 + i];
        }
        for (; i < limit; i++) {
            sum00 += array1[shift100 + i] * array2[shift200 + i];
        }

        return sum00 + sum01 + sum02 + sum03 + sum04 + sum05 + sum06 + sum07 + sum08 + sum09 + sum10 + sum11 + sum12 + sum13 + sum14 + sum15;
    }

}
