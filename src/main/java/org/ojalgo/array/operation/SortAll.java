/*
 * Copyright 1997-2025 Optimatika
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

public abstract class SortAll implements ArrayOperation {

    public static void sort(final long[] primary, final double[] secondary) {

        boolean tmpSwapped;

        int tmpLimit = Math.min(primary.length, secondary.length) - 1;

        do {
            tmpSwapped = false;
            for (int i = 0; i < tmpLimit; i++) {
                if (primary[i] > primary[i + 1]) {
                    long tmpPrimVal = primary[i];
                    primary[i] = primary[i + 1];
                    primary[i + 1] = tmpPrimVal;
                    double tmpSecoVal = secondary[i];
                    secondary[i] = secondary[i + 1];
                    secondary[i + 1] = tmpSecoVal;
                    tmpSwapped = true;
                }
            }
        } while (tmpSwapped);
    }

    public static boolean sort(final long[] primary, final int[] secondary) {

        boolean retVal = false;
        boolean swapped;

        int limit = Math.min(primary.length, secondary.length) - 1;

        long p0, p1;
        int tmp;

        do {
            swapped = false;
            for (int i = 0; i < limit; i++) {

                p0 = primary[i];
                p1 = primary[i + 1];
                if (p0 > p1) {

                    primary[i] = p1;
                    primary[i + 1] = p0;

                    tmp = secondary[i];
                    secondary[i] = secondary[i + 1];
                    secondary[i + 1] = tmp;

                    swapped = true;
                }
            }
            retVal |= swapped;
        } while (swapped);

        return retVal;
    }

    public static void sort(final long[] primary, final Object[] secondary) {

        boolean tmpSwapped;

        int tmpLimit = Math.min(primary.length, secondary.length) - 1;

        do {
            tmpSwapped = false;
            for (int i = 0; i < tmpLimit; i++) {
                if (primary[i] > primary[i + 1]) {
                    long tmpPrimVal = primary[i];
                    primary[i] = primary[i + 1];
                    primary[i + 1] = tmpPrimVal;
                    Object tmpSecoVal = secondary[i];
                    secondary[i] = secondary[i + 1];
                    secondary[i + 1] = tmpSecoVal;
                    tmpSwapped = true;
                }
            }
        } while (tmpSwapped);
    }

}
