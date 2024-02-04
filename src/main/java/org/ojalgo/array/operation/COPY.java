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

import java.lang.reflect.Array;

import org.ojalgo.structure.Access2D;

/**
 * The ?copy routines perform a vector-vector operation defined as y = x, where x and y are vectors.
 *
 * @author apete
 */
public abstract class COPY implements ArrayOperation {

    public static int THRESHOLD = 128;

    public static void column(final Access2D<?> source, final long col, final double[] destination, final int first, final int limit) {
        for (int i = first; i < limit; i++) {
            destination[i] = source.doubleValue(i, col);
        }
    }

    public static double[] copyOf(final double[] original) {
        int tmpLength = original.length;
        double[] retVal = new double[tmpLength];
        System.arraycopy(original, 0, retVal, 0, tmpLength);
        return retVal;
    }

    public static float[] copyOf(final float[] original) {
        int tmpLength = original.length;
        float[] retVal = new float[tmpLength];
        System.arraycopy(original, 0, retVal, 0, tmpLength);
        return retVal;
    }

    public static int[] copyOf(final int[] original) {
        int tmpLength = original.length;
        int[] retVal = new int[tmpLength];
        System.arraycopy(original, 0, retVal, 0, tmpLength);
        return retVal;
    }

    public static long[] copyOf(final long[] original) {
        int tmpLength = original.length;
        long[] retVal = new long[tmpLength];
        System.arraycopy(original, 0, retVal, 0, tmpLength);
        return retVal;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] copyOf(final T[] original) {
        int tmpLength = original.length;
        T[] retVal = (T[]) Array.newInstance(original.getClass().getComponentType(), tmpLength);
        System.arraycopy(original, 0, retVal, 0, tmpLength);
        return retVal;
    }

    public static int[] invoke(final int[] source, final int[] destination) {
        int limit = Math.min(source.length, destination.length);
        System.arraycopy(source, 0, destination, 0, limit);
        return destination;
    }

    public static void row(final Access2D<?> source, final long row, final double[] destination, final int first, final int limit) {
        for (int j = first; j < limit; j++) {
            destination[j] = source.doubleValue(row, j);
        }
    }

}
