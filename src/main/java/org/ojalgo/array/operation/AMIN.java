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

import org.ojalgo.function.constant.PrimitiveMath;

/**
 * Given a vector x, the i?amin functions return the position of the vector element x[i] that has the smallest
 * absolute value for real flavors, or the smallest sum |Re(x[i])|+|Im(x[i])| for complex flavors. If n is not
 * positive, 0 is returned. If more than one vector element is found with the same smallest absolute value,
 * the index of the first one encountered is returned.
 *
 * @author apete
 */
public abstract class AMIN implements ArrayOperation {

    public static int THRESHOLD = 128;

    public static int invoke(final double[] data, final int first, final int limit, final int step) {

        int retVal = first;
        double smallest = PrimitiveMath.POSITIVE_INFINITY;
        double candidate;

        for (int i = first; i < limit; i += step) {
            candidate = PrimitiveMath.ABS.invoke(data[i]);
            if (candidate < smallest) {
                smallest = candidate;
                retVal = i;
            }
        }
        return retVal;
    }

}
