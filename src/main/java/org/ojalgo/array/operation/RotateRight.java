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

public abstract class RotateRight implements ArrayOperation {

    public static int THRESHOLD = 128;

    public static void invoke(final double[] data, final int structure, final int colA, final int colB, final double cos, final double sin) {

        double oldA;
        double oldB;

        int indexA = colA * structure;
        int indexB = colB * structure;

        for (int i = 0; i < structure; i++) {

            oldA = data[indexA];
            oldB = data[indexB];

            data[indexA] = cos * oldA - sin * oldB;
            data[indexB] = cos * oldB + sin * oldA;

            indexA++;
            indexB++;
        }
    }

    public static void invoke(final float[] data, final int structure, final int colA, final int colB, final float cos, final float sin) {

        float oldA;
        float oldB;

        int indexA = colA * structure;
        int indexB = colB * structure;

        for (int i = 0; i < structure; i++) {

            oldA = data[indexA];
            oldB = data[indexB];

            data[indexA] = cos * oldA - sin * oldB;
            data[indexB] = cos * oldB + sin * oldA;

            indexA++;
            indexB++;
        }
    }

    public static <N extends Scalar<N>> void invoke(final N[] data, final int structure, final int colA, final int colB, final N cos, final N sin) {

        N oldA;
        N oldB;

        int indexA = colA * structure;
        int indexB = colB * structure;

        for (int i = 0; i < structure; i++) {

            oldA = data[indexA];
            oldB = data[indexB];

            data[indexA] = cos.multiply(oldA).subtract(sin.multiply(oldB)).get();
            data[indexB] = cos.multiply(oldB).add(sin.multiply(oldA)).get();

            indexA++;
            indexB++;
        }
    }

}
