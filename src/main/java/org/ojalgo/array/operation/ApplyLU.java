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

import org.ojalgo.scalar.Scalar;

public abstract class ApplyLU implements ArrayOperation {

    public static int THRESHOLD = 256;

    public static void invoke(final double[] data, final int structure, final int firstColumn, final int columnLimit, final double[] multipliers,
            final int iterationPoint) {
        for (int j = firstColumn; j < columnLimit; j++) {
            AXPY.invoke(data, j * structure, -data[iterationPoint + j * structure], multipliers, 0, iterationPoint + 1, structure);
        }
    }

    public static <N extends Scalar<N>> void invoke(final N[] data, final int structure, final int firstColumn, final int columnLimit, final N[] multipliers,
            final int iterationPoint) {
        for (int j = firstColumn; j < columnLimit; j++) {
            AXPY.invoke(data, j * structure, data[iterationPoint + j * structure].negate().get(), multipliers, 0, iterationPoint + 1, structure);
        }
    }

}
