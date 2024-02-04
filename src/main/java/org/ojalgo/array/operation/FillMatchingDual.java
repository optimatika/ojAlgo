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

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;

import org.ojalgo.structure.Structure2D;

public abstract class FillMatchingDual implements ArrayOperation {

    /**
     * 2013-10-22: Was set to 128 (based on calibration) but I saw a dip in relative performance (java matrix
     * benchmark) at size 200. So I cahnged it to 256.
     */
    public static int THRESHOLD = 256;

    public static void fillAll(final double[][] target, final double value) {
        int limit = target.length;
        for (int i = 0; i < limit; i++) {
            int tmpInnerLength = target[i].length;
            for (int j = 0; j < tmpInnerLength; j++) {
                target[i][j] = value;
            }
        }
    }

    public static void fillAll(final double[][] target, final DoubleSupplier supplier) {
        int limit = target.length;
        for (int i = 0; i < limit; i++) {
            int tmpInnerLength = target[i].length;
            for (int j = 0; j < tmpInnerLength; j++) {
                target[i][j] = supplier.getAsDouble();
            }
        }
    }

    public static void fillColumn(final double[][] target, final int row, final int col, final double value) {
        for (int i = row, limit = target.length; i < limit; i++) {
            target[i][col] = value;
        }
    }

    public static void fillColumn(final double[][] target, final int row, final int col, final DoubleSupplier supplier) {
        for (int i = row, limit = target.length; i < limit; i++) {
            target[i][col] = supplier.getAsDouble();
        }
    }

    public static void fillDiagonal(final double[][] target, final int row, final int col, final double value) {
        int limit = target.length;
        for (int ij = 0; row + ij < limit && col + ij < target[row + ij].length; ij++) {
            target[row + ij][col + ij] = value;
        }
    }

    public static void fillDiagonal(final double[][] target, final int row, final int col, final DoubleSupplier supplier) {
        int limit = target.length;
        for (int ij = 0; row + ij < limit && col + ij < target[row + ij].length; ij++) {
            target[row + ij][col + ij] = supplier.getAsDouble();
        }
    }

    public static void fillMatching(final double[][] target, final double left, final DoubleBinaryOperator function, final double[][] right) {
        int limit = target.length;
        for (int i = 0; i < limit; i++) {
            int tmpInnerLength = target[i].length;
            for (int j = 0; j < tmpInnerLength; j++) {
                target[i][j] = function.applyAsDouble(left, right[i][j]);
            }
        }
    }

    public static void fillMatching(final double[][] target, final double[][] left, final DoubleBinaryOperator function, final double right) {
        int limit = target.length;
        for (int i = 0; i < limit; i++) {
            int tmpInnerLength = target[i].length;
            for (int j = 0; j < tmpInnerLength; j++) {
                target[i][j] = function.applyAsDouble(left[i][j], right);
            }
        }
    }

    public static void fillMatching(final double[][] target, final double[][] left, final DoubleBinaryOperator function, final double[][] right) {
        int limit = target.length;
        for (int i = 0; i < limit; i++) {
            int tmpInnerLength = target[i].length;
            for (int j = 0; j < tmpInnerLength; j++) {
                target[i][j] = function.applyAsDouble(left[i][j], right[i][j]);
            }
        }
    }

    public static void fillRange(final double[][] target, final int first, final int limit, final double value) {

        int tmpLength = target.length;

        for (int index = first; index < limit; index++) {
            int tmpRow = Structure2D.row(index, tmpLength);
            int tmpcol = Structure2D.column(index, tmpLength);
            target[tmpRow][tmpcol] = value;
        }
    }

    public static void fillRange(final double[][] target, final int first, final int limit, final DoubleSupplier supplier) {

        int tmpLength = target.length;

        for (int index = first; index < limit; index++) {
            int tmpRow = Structure2D.row(index, tmpLength);
            int tmpcol = Structure2D.column(index, tmpLength);
            target[tmpRow][tmpcol] = supplier.getAsDouble();
        }
    }

    public static void fillRow(final double[][] target, final int row, final int col, final double value) {
        double[] targetRow = target[row];
        for (int j = col, limit = targetRow.length; j < limit; j++) {
            targetRow[j] = value;
        }
    }

    public static void fillRow(final double[][] target, final int row, final int col, final DoubleSupplier supplier) {
        double[] targetRow = target[row];
        for (int j = col, limit = targetRow.length; j < limit; j++) {
            targetRow[j] = supplier.getAsDouble();
        }
    }

}
