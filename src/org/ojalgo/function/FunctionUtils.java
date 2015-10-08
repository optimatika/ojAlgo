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
package org.ojalgo.function;

import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.type.TypeUtils;

public abstract class FunctionUtils {

    public static <N extends Number> boolean isZeroModified(final UnaryFunction<N> function) {
        return !TypeUtils.isZero(function.invoke(PrimitiveMath.ZERO));
    }

    public static int max(final int... values) {
        int retVal = values[0];
        for (int i = values.length; i-- != 1;) {
            retVal = values[i] > retVal ? values[i] : retVal;
        }
        return retVal;
    }

    public static int max(final int a, final int b) {
        return Math.max(a, b);
    }

    public static int max(final int a, final int b, final int c) {
        return Math.max(Math.max(a, b), c);
    }

    public static int max(final int a, final int b, final int c, final int d) {
        return Math.max(Math.max(a, b), Math.max(c, d));
    }

    public static long max(final long a, final long b) {
        return Math.max(a, b);
    }

    public static long max(final long a, final long b, final long c) {
        return Math.max(Math.max(a, b), c);
    }

    public static int min(final int... values) {
        int retVal = values[0];
        for (int i = values.length; i-- != 1;) {
            retVal = values[i] < retVal ? values[i] : retVal;
        }
        return retVal;
    }

    public static int min(final int a, final int b) {
        return Math.min(a, b);
    }

    public static int min(final int a, final int b, final int c) {
        return Math.min(Math.min(a, b), c);
    }

    public static int min(final int a, final int b, final int c, final int d) {
        return Math.min(Math.min(a, b), Math.min(c, d));
    }

    public static long min(final long a, final long b) {
        return Math.min(a, b);
    }

    public static long min(final long a, final long b, final long c) {
        return Math.min(Math.min(a, b), c);
    }

    private FunctionUtils() {
        super();
    }

}
