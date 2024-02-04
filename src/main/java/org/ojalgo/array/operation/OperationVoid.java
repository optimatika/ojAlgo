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

import org.ojalgo.array.BasicArray;
import org.ojalgo.function.VoidFunction;

public abstract class OperationVoid implements ArrayOperation {

    public static int THRESHOLD = 256;

    public static <N extends Comparable<N>> void invoke(final BasicArray<N> data, final int first, final int limit, final int step,
            final VoidFunction<N> visitor) {
        for (int i = first; i < limit; i += step) {
            visitor.invoke(data.doubleValue(i));
        }
    }

    public static <N extends Comparable<N>> void invoke(final BasicArray<N> data, final long first, final long limit, final long step,
            final VoidFunction<N> visitor) {

        switch (data.getMathType()) {
        case R064:
            for (long i = first; i < limit; i += step) {
                visitor.invoke(data.doubleValue(i));
            }
            break;
        case R032:
            for (long i = first; i < limit; i += step) {
                visitor.invoke(data.floatValue(i));
            }
            break;
        case Z064:
            for (long i = first; i < limit; i += step) {
                visitor.invoke(data.longValue(i));
            }
            break;
        case Z032:
        case Z016:
        case Z008:
            for (long i = first; i < limit; i += step) {
                visitor.invoke(data.intValue(i));
            }
            break;
        default:
            for (long i = first; i < limit; i += step) {
                visitor.invoke(data.get(i));
            }
            break;
        }
    }

    public static void invoke(final byte[] data, final int first, final int limit, final int step, final VoidFunction<Double> visitor) {
        for (int i = first; i < limit; i += step) {
            visitor.invoke(data[i]);
        }
    }

    public static void invoke(final double[] data, final int first, final int limit, final int step, final VoidFunction<Double> visitor) {
        for (int i = first; i < limit; i += step) {
            visitor.invoke(data[i]);
        }
    }

    public static void invoke(final float[] data, final int first, final int limit, final int step, final VoidFunction<Double> visitor) {
        for (int i = first; i < limit; i += step) {
            visitor.invoke(data[i]);
        }
    }

    public static void invoke(final int[] data, final int first, final int limit, final int step, final VoidFunction<Double> visitor) {
        for (int i = first; i < limit; i += step) {
            visitor.invoke(data[i]);
        }
    }

    public static void invoke(final long[] data, final int first, final int limit, final int step, final VoidFunction<Double> visitor) {
        for (int i = first; i < limit; i += step) {
            visitor.invoke(data[i]);
        }
    }

    public static <N extends Comparable<N>> void invoke(final N[] data, final int first, final int limit, final int step, final VoidFunction<N> visitor) {
        for (int i = first; i < limit; i += step) {
            visitor.invoke(data[i]);
        }
    }

    public static void invoke(final short[] data, final int first, final int limit, final int step, final VoidFunction<Double> visitor) {
        for (int i = first; i < limit; i += step) {
            visitor.invoke(data[i]);
        }
    }

}
