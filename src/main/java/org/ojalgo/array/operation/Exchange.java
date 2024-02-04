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

public abstract class Exchange implements ArrayOperation {

    public static int THRESHOLD = 256;

    public static <N extends Comparable<N>> void exchange(final BasicArray<N> data, final long firstA, final long firstB, final long step, final long count) {

        long indexA = firstA;
        long indexB = firstB;

        N tmpVal;

        for (long i = 0L; i < count; i++) {

            tmpVal = data.get(indexA);
            data.set(indexA, data.get(indexB));
            data.set(indexB, tmpVal);

            indexA += step;
            indexB += step;
        }
    }

    public static void exchange(final byte[] data, final int firstA, final int firstB, final int step, final int count) {

        int indexA = firstA;
        int indexB = firstB;

        byte tmpVal;

        for (int i = 0; i < count; i++) {

            tmpVal = data[indexA];
            data[indexA] = data[indexB];
            data[indexB] = tmpVal;

            indexA += step;
            indexB += step;
        }
    }

    public static void exchange(final double[] data, final int firstA, final int firstB, final int step, final int count) {

        int indexA = firstA;
        int indexB = firstB;

        double tmpVal;

        for (int i = 0; i < count; i++) {

            tmpVal = data[indexA];
            data[indexA] = data[indexB];
            data[indexB] = tmpVal;

            indexA += step;
            indexB += step;
        }
    }

    public static void exchange(final float[] data, final int firstA, final int firstB, final int step, final int count) {

        int indexA = firstA;
        int indexB = firstB;

        float tmpVal;

        for (int i = 0; i < count; i++) {

            tmpVal = data[indexA];
            data[indexA] = data[indexB];
            data[indexB] = tmpVal;

            indexA += step;
            indexB += step;
        }
    }

    public static void exchange(final int[] data, final int firstA, final int firstB, final int step, final int count) {

        int indexA = firstA;
        int indexB = firstB;

        int tmpVal;

        for (int i = 0; i < count; i++) {

            tmpVal = data[indexA];
            data[indexA] = data[indexB];
            data[indexB] = tmpVal;

            indexA += step;
            indexB += step;
        }
    }

    public static void exchange(final long[] data, final int firstA, final int firstB, final int step, final int count) {

        int indexA = firstA;
        int indexB = firstB;

        long tmpVal;

        for (int i = 0; i < count; i++) {

            tmpVal = data[indexA];
            data[indexA] = data[indexB];
            data[indexB] = tmpVal;

            indexA += step;
            indexB += step;
        }
    }

    public static <N extends Comparable<N>> void exchange(final N[] data, final int firstA, final int firstB, final int step, final int count) {

        int indexA = firstA;
        int indexB = firstB;

        N tmpVal;

        for (int i = 0; i < count; i++) {

            tmpVal = data[indexA];
            data[indexA] = data[indexB];
            data[indexB] = tmpVal;

            indexA += step;
            indexB += step;
        }
    }

    public static void exchange(final short[] data, final int firstA, final int firstB, final int step, final int count) {

        int indexA = firstA;
        int indexB = firstB;

        short tmpVal;

        for (int i = 0; i < count; i++) {

            tmpVal = data[indexA];
            data[indexA] = data[indexB];
            data[indexB] = tmpVal;

            indexA += step;
            indexB += step;
        }
    }

}
