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

import org.ojalgo.array.BasicArray;
import org.ojalgo.constant.BigMath;
import org.ojalgo.function.BigFunction;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.scalar.Scalar;

/**
 * Given a vector x, the i?amax functions return the position of the vector element x[i] that has the largest
 * absolute value for real flavors, or the largest sum |Re(x[i])|+|Im(x[i])| for complex flavors. If n is not
 * positive, 0 is returned. If more than one vector element is found with the same largest absolute value, the
 * index of the first one encountered is returned.
 *
 * @author apete
 */
public abstract class AMAX implements BLAS1 {

    public static <N extends Number> long invoke(final BasicArray<N> data, final long first, final long limit, final long step) {

        long retVal = first;
        double tmpLargest = 0D;
        double tmpValue;

        for (long i = first; i < limit; i += step) {
            tmpValue = PrimitiveFunction.ABS.invoke(data.doubleValue(i));
            if (tmpValue > tmpLargest) {
                tmpLargest = tmpValue;
                retVal = i;
            }
        }
        return retVal;
    }

    public static int invoke(final BigDecimal[] data, final int first, final int limit, final int step) {

        int retVal = first;
        BigDecimal tmpLargest = BigMath.ZERO;
        BigDecimal tmpValue;

        for (int i = first; i < limit; i += step) {
            tmpValue = BigFunction.ABS.invoke(data[i]);
            if (tmpValue.compareTo(tmpLargest) > 0) {
                tmpLargest = tmpValue;
                retVal = i;
            }
        }
        return retVal;
    }

    public static int invoke(final double[] data, final int first, final int limit, final int step) {

        int retVal = first;
        double largest = 0D;
        double candidate;

        for (int i = first; i < limit; i += step) {
            candidate = PrimitiveFunction.ABS.invoke(data[i]);
            if (candidate > largest) {
                largest = candidate;
                retVal = i;
            }
        }
        return retVal;
    }

    public static int invoke(final float[] data, final int first, final int limit, final int step) {

        int retVal = first;
        float tmpLargest = 0F;
        float tmpValue;

        for (int i = first; i < limit; i += step) {
            tmpValue = Math.abs(data[i]);
            if (tmpValue > tmpLargest) {
                tmpLargest = tmpValue;
                retVal = i;
            }
        }
        return retVal;
    }

    public static <N extends Number & Scalar<N>> int invoke(final N[] data, final int first, final int limit, final int step) {

        int retVal = first;
        double tmpLargest = 0D;
        double tmpValue;

        for (int i = first; i < limit; i += step) {
            tmpValue = data[i].norm();
            if (tmpValue > tmpLargest) {
                tmpLargest = tmpValue;
                retVal = i;
            }
        }
        return retVal;
    }

}
