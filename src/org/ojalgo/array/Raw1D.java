/*
 * Copyright 1997-2018 Optimatika
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
package org.ojalgo.array;

import java.lang.reflect.Array;
import java.util.function.DoubleConsumer;

public abstract class Raw1D {

    public static double[] copyOf(final double[] original) {
        final int tmpLength = original.length;
        final double[] retVal = new double[tmpLength];
        System.arraycopy(original, 0, retVal, 0, tmpLength);
        return retVal;
    }

    public static float[] copyOf(final float[] original) {
        final int tmpLength = original.length;
        final float[] retVal = new float[tmpLength];
        System.arraycopy(original, 0, retVal, 0, tmpLength);
        return retVal;
    }

    public static int[] copyOf(final int[] original) {
        final int tmpLength = original.length;
        final int[] retVal = new int[tmpLength];
        System.arraycopy(original, 0, retVal, 0, tmpLength);
        return retVal;
    }

    public static long[] copyOf(final long[] original) {
        final int tmpLength = original.length;
        final long[] retVal = new long[tmpLength];
        System.arraycopy(original, 0, retVal, 0, tmpLength);
        return retVal;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] copyOf(final T[] original) {
        final int tmpLength = original.length;
        final T[] retVal = (T[]) Array.newInstance(original.getClass().getComponentType(), tmpLength);
        System.arraycopy(original, 0, retVal, 0, tmpLength);
        return retVal;
    }

    public static void sort(final long[] primary, final double[] secondary) {

        boolean tmpSwapped;

        final int tmpLimit = Math.min(primary.length, secondary.length) - 1;

        do {
            tmpSwapped = false;
            for (int i = 0; i < tmpLimit; i++) {
                if (primary[i] > primary[i + 1]) {
                    final long tmpPrimVal = primary[i];
                    primary[i] = primary[i + 1];
                    primary[i + 1] = tmpPrimVal;
                    final double tmpSecoVal = secondary[i];
                    secondary[i] = secondary[i + 1];
                    secondary[i + 1] = tmpSecoVal;
                    tmpSwapped = true;
                }
            }
        } while (tmpSwapped);
    }

    public static void sort(final long[] primary, final Object[] secondary) {

        boolean tmpSwapped;

        final int tmpLimit = Math.min(primary.length, secondary.length) - 1;

        do {
            tmpSwapped = false;
            for (int i = 0; i < tmpLimit; i++) {
                if (primary[i] > primary[i + 1]) {
                    final long tmpPrimVal = primary[i];
                    primary[i] = primary[i + 1];
                    primary[i + 1] = tmpPrimVal;
                    final Object tmpSecoVal = secondary[i];
                    secondary[i] = secondary[i + 1];
                    secondary[i + 1] = tmpSecoVal;
                    tmpSwapped = true;
                }
            }
        } while (tmpSwapped);
    }

    public static void visit(final double[] target, final DoubleConsumer visitor) {
        Raw1D.visit(target, 0, target.length, visitor);
    }

    public static void visit(final double[] target, final int first, final int limit, final DoubleConsumer visitor) {
        for (int i = first, lim = Math.min(limit, target.length); i < lim; i++) {
            visitor.accept(target[i]);
        }
    }

}
