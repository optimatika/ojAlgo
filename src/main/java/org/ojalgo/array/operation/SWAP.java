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

/**
 * Given two vectors x and y, the ?swap routines return vectors y and x swapped, each replacing the other.
 *
 * @author apete
 */
public abstract class SWAP implements ArrayOperation {

    public static int THRESHOLD = 128;

    public static void exchangeColumns(final double[][] target, final int colA, final int colB) {
        double tmpElem;
        int limit = target.length;
        for (int i = 0; i < limit; i++) {
            tmpElem = target[i][colA];
            target[i][colA] = target[i][colB];
            target[i][colB] = tmpElem;
        }
    }

    public static void exchangeRows(final double[][] target, final int rowA, final int rowB) {
        double[] tmpRow = target[rowA];
        target[rowA] = target[rowB];
        target[rowB] = tmpRow;
    }

}
