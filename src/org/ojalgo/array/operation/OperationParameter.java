/*
 * Copyright 1997-2019 Optimatika
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

import org.ojalgo.function.ParameterFunction;

public final class OperationParameter implements ArrayOperation {

    public static int THRESHOLD = 256;

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

    public static <N extends Comparable<N>> void invoke(final N[] data, final int first, final int limit, final int step, final N[] values,
            final ParameterFunction<N> function, final int param) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(values[i], param);
        }
    }

    public int threshold() {
        return THRESHOLD;
    }

}
