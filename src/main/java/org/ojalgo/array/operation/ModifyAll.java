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

import java.util.function.DoubleUnaryOperator;

public abstract class ModifyAll implements ArrayOperation {

    public static int THRESHOLD = 64;

    public static void modifyAll(final double[][] target, final DoubleUnaryOperator function) {
        int tmpLength = target.length;
        for (int i = 0; i < tmpLength; i++) {
            int tmpInnerLength = target[i].length;
            for (int j = 0; j < tmpInnerLength; j++) {
                target[i][j] = function.applyAsDouble(target[i][j]);
            }
        }
    }

    public static void modifyColumn(final double[][] target, final int row, final int col, final DoubleUnaryOperator function) {
        for (int i = row, limit = target.length; i < limit; i++) {
            target[i][col] = function.applyAsDouble(target[i][col]);
        }
    }

    public static void modifyDiagonal(final double[][] target, final int row, final int col, final DoubleUnaryOperator function) {
        int tmpLength = target.length;
        for (int ij = 0; row + ij < tmpLength && col + ij < target[row + ij].length; ij++) {
            target[row + ij][col + ij] = function.applyAsDouble(target[row + ij][col + ij]);
        }
    }

    public static void modifyRow(final double[][] target, final int row, final int col, final DoubleUnaryOperator function) {
        double[] targetRow = target[row];
        for (int j = col, limit = targetRow.length; j < limit; j++) {
            targetRow[j] = function.applyAsDouble(targetRow[j]);
        }
    }

}
