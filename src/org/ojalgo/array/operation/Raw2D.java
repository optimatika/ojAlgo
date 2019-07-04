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

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;

import org.ojalgo.structure.Structure2D;

public final class Raw2D extends ArrayOperation {

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
        for (int ij = 0; ((row + ij) < limit) && ((col + ij) < target[row + ij].length); ij++) {
            target[row + ij][col + ij] = value;
        }
    }

    public static void fillDiagonal(final double[][] target, final int row, final int col, final DoubleSupplier supplier) {
        int limit = target.length;
        for (int ij = 0; ((row + ij) < limit) && ((col + ij) < target[row + ij].length); ij++) {
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
        for (int ij = 0; ((row + ij) < tmpLength) && ((col + ij) < target[row + ij].length); ij++) {
            target[row + ij][col + ij] = function.applyAsDouble(target[row + ij][col + ij]);
        }
    }

    public static void modifyRow(final double[][] target, final int row, final int col, final DoubleUnaryOperator function) {
        double[] targetRow = target[row];
        for (int j = col, limit = targetRow.length; j < limit; j++) {
            targetRow[j] = function.applyAsDouble(targetRow[j]);
        }
    }

    public static void visitAll(final double[][] target, final DoubleConsumer visitor) {
        int tmpLength = target.length;
        for (int i = 0; i < tmpLength; i++) {
            int tmpInnerLength = target[i].length;
            for (int j = 0; j < tmpInnerLength; j++) {
                visitor.accept(target[i][j]);
            }
        }
    }

    public static void visitColumn(final double[][] target, final int row, final int col, final DoubleConsumer visitor) {
        for (int i = row, limit = target.length; i < limit; i++) {
            visitor.accept(target[i][col]);
        }
    }

    public static void visitDiagonal(final double[][] target, final int row, final int col, final DoubleConsumer visitor) {
        int limit = target.length;
        for (int ij = 0; ((row + ij) < limit) && ((col + ij) < target[row + ij].length); ij++) {
            visitor.accept(target[row + ij][col + ij]);
        }
    }

    public static void visitRange(final double[][] target, final int first, final int limit, final DoubleConsumer visitor) {
        int tmpStructure = target.length;
        for (int index = first; index < limit; index++) {
            visitor.accept(target[Structure2D.row(index, tmpStructure)][Structure2D.column(index, tmpStructure)]);
        }
    }

    public static void visitRow(final double[][] target, final int row, final int col, final DoubleConsumer visitor) {
        double[] targetRow = target[row];
        for (int j = col, limit = targetRow.length; j < limit; j++) {
            visitor.accept(targetRow[j]);
        }
    }

    public static int THRESHOLD = 128;

    @Override
    public int threshold() {
        return THRESHOLD;
    }

}
