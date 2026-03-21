/*
 * Copyright 1997-2025 Optimatika
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

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

/**
 * Infinity norm - largest absolute value
 *
 * @author apete
 */
public abstract class NRMINF implements ArrayOperation {

    public static double difference(final double[] array1, final double[] array2) {
        double retVal = ZERO;
        double tmp;
        for (int i = 0, limit = Math.min(array1.length, array2.length); i < limit; i++) {
            tmp = Math.abs(array1[i] - array2[i]);
            if (tmp > retVal) {
                retVal = tmp;
            }
        }
        return retVal;
    }

    public static double invoke(final double[] data) {
        return NRMINF.invoke(data, 0, data.length);
    }

    public static double invoke(final double[] data, final int first, final int limit) {
        double retVal = ZERO;
        double tmp;
        for (int i = first; i < limit; i++) {
            tmp = Math.abs(data[i]);
            if (tmp > retVal) {
                retVal = tmp;
            }
        }
        return retVal;
    }

    public static double product(final double[] array1, final double[] array2) {
        double retVal = ZERO;
        double tmp;
        for (int i = 0, limit = Math.min(array1.length, array2.length); i < limit; i++) {
            tmp = Math.abs(array1[i] * array2[i]);
            if (tmp > retVal) {
                retVal = tmp;
            }
        }
        return retVal;
    }

}
