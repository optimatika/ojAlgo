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
 * The ?asum routine computes the sum of the magnitudes of elements of a real vector, or the sum of magnitudes
 * of the real and imaginary parts of elements of a complex vector: res = |Re x1| + |Im x1| + |Re x2| + |Im
 * x2|+ ... + |Re xn| + |Im xn|, where x is a vector with n elements.
 *
 * @author apete
 */
public abstract class ASUM implements ArrayOperation {

    public static int THRESHOLD = 128;

    public static double invoke(final double[] data, final int first, final int limit, final int step) {
        double retVal = 0D;
        for (int i = first; i < limit; i += step) {
            retVal += PrimitiveMath.ABS.invoke(data[i]);
        }
        return retVal;
    }

}
