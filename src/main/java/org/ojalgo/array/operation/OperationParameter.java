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
import org.ojalgo.function.ParameterFunction;
import org.ojalgo.structure.Access1D;

public abstract class OperationParameter implements ArrayOperation {

    public static int THRESHOLD = 256;

    public static <N extends Comparable<N>> void invoke(final BasicArray<N> data, final int first, final int limit, final int step, final Access1D<N> value,
            final ParameterFunction<N> function, final int param) {
        for (int i = first; i < limit; i += step) {
            data.set(i, function.invoke(value.doubleValue(i), param));
        }
    }

    public static void invoke(final byte[] data, final int first, final int limit, final int step, final byte[] values,
            final ParameterFunction<Double> function, final int param) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(values[i], param);
        }
    }

    public static void invoke(final double[] data, final int first, final int limit, final int step, final double[] values,
            final ParameterFunction<Double> function, final int param) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(values[i], param);
        }
    }

    public static void invoke(final float[] data, final int first, final int limit, final int step, final float[] values,
            final ParameterFunction<Double> function, final int param) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(values[i], param);
        }
    }

    public static void invoke(final int[] data, final int first, final int limit, final int step, final int[] values, final ParameterFunction<Double> function,
            final int param) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(values[i], param);
        }
    }

    public static void invoke(final long[] data, final int first, final int limit, final int step, final long[] values,
            final ParameterFunction<Double> function, final int param) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(values[i], param);
        }
    }

    public static <N extends Comparable<N>> void invoke(final N[] data, final int first, final int limit, final int step, final N[] values,
            final ParameterFunction<N> function, final int param) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(values[i], param);
        }
    }

    public static void invoke(final short[] data, final int first, final int limit, final int step, final short[] values,
            final ParameterFunction<Double> function, final int param) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(values[i], param);
        }
    }

}
