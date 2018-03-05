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

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;

import org.ojalgo.access.Structure2D;

public class Raw2D {

    public static void exchangeColumns(final double[][] target, final int columnA, final int columnB) {
        double tmpElem;
        final int tmpLength = target.length;
        for (int i = 0; i < tmpLength; i++) {
            tmpElem = target[i][columnA];
            target[i][columnA] = target[i][columnB];
            target[i][columnB] = tmpElem;
        }
    }

    public static void exchangeRows(final double[][] target, final int rowA, final int rowB) {
        final double[] tmpRow = target[rowA];
        target[rowA] = target[rowB];
        target[rowB] = tmpRow;
    }

    public static void fillAll(final double[][] target, final double value) {
        final int tmpLength = target.length;
        for (int i = 0; i < tmpLength; i++) {
            final int tmpInnerLength = target[i].length;
            for (int j = 0; j < tmpInnerLength; j++) {
                target[i][j] = value;
            }
        }
    }

    public static void fillAll(final double[][] target, final DoubleSupplier supplier) {
        final int tmpLength = target.length;
        for (int i = 0; i < tmpLength; i++) {
            final int tmpInnerLength = target[i].length;
            for (int j = 0; j < tmpInnerLength; j++) {
                target[i][j] = supplier.getAsDouble();
            }
        }
    }

    public static void fillColumn(final double[][] target, final int row, final int column, final double value) {
        final int tmpLength = target.length;
        for (int i = row; i < tmpLength; i++) {
            target[i][column] = value;
        }
    }

    public static void fillColumn(final double[][] target, final int row, final int column, final DoubleSupplier supplier) {
        final int tmpLength = target.length;
        for (int i = row; i < tmpLength; i++) {
            target[i][column] = supplier.getAsDouble();
        }
    }

    public static void fillDiagonal(final double[][] target, final int row, final int column, final double value) {
        final int tmpLength = target.length;
        for (int ij = 0; ((row + ij) < tmpLength) && ((column + ij) < target[row + ij].length); ij++) {
            target[row + ij][column + ij] = value;
        }
    }

    public static void fillDiagonal(final double[][] target, final int row, final int column, final DoubleSupplier supplier) {
        final int tmpLength = target.length;
        for (int ij = 0; ((row + ij) < tmpLength) && ((column + ij) < target[row + ij].length); ij++) {
            target[row + ij][column + ij] = supplier.getAsDouble();
        }
    }

    public static void fillMatching(final double[][] target, final double left, final DoubleBinaryOperator function, final double[][] right) {
        final int tmpLength = target.length;
        for (int i = 0; i < tmpLength; i++) {
            final int tmpInnerLength = target[i].length;
            for (int j = 0; j < tmpInnerLength; j++) {
                target[i][j] = function.applyAsDouble(left, right[i][j]);
            }
        }
    }

    public static void fillMatching(final double[][] target, final double[][] left, final DoubleBinaryOperator function, final double right) {
        final int tmpLength = target.length;
        for (int i = 0; i < tmpLength; i++) {
            final int tmpInnerLength = target[i].length;
            for (int j = 0; j < tmpInnerLength; j++) {
                target[i][j] = function.applyAsDouble(left[i][j], right);
            }
        }
    }

    public static void fillMatching(final double[][] target, final double[][] left, final DoubleBinaryOperator function, final double[][] right) {
        final int tmpLength = target.length;
        for (int i = 0; i < tmpLength; i++) {
            final int tmpInnerLength = target[i].length;
            for (int j = 0; j < tmpInnerLength; j++) {
                target[i][j] = function.applyAsDouble(left[i][j], right[i][j]);
            }
        }
    }

    public static void fillRange(final double[][] target, final int first, final int limit, final double value) {

        final int tmpLength = target.length;

        for (int index = first; index < limit; index++) {
            final int tmpRow = Structure2D.row(index, tmpLength);
            final int tmpColumn = Structure2D.column(index, tmpLength);
            target[tmpRow][tmpColumn] = value;
        }
    }

    public static void fillRange(final double[][] target, final int first, final int limit, final DoubleSupplier supplier) {

        final int tmpLength = target.length;

        for (int index = first; index < limit; index++) {
            final int tmpRow = Structure2D.row(index, tmpLength);
            final int tmpColumn = Structure2D.column(index, tmpLength);
            target[tmpRow][tmpColumn] = supplier.getAsDouble();
        }
    }

    public static void fillRow(final double[][] target, final int row, final int column, final double value) {
        final int tmpLength = target[row].length;
        for (int j = column; j < tmpLength; j++) {
            target[row][j] = value;
        }
    }

    public static void fillRow(final double[][] target, final int row, final int column, final DoubleSupplier supplier) {
        final int tmpLength = target[row].length;
        for (int j = column; j < tmpLength; j++) {
            target[row][j] = supplier.getAsDouble();
        }
    }

    public static void modifyAll(final double[][] target, final DoubleUnaryOperator function) {
        final int tmpLength = target.length;
        for (int i = 0; i < tmpLength; i++) {
            final int tmpInnerLength = target[i].length;
            for (int j = 0; j < tmpInnerLength; j++) {
                target[i][j] = function.applyAsDouble(target[i][j]);
            }
        }
    }

    public static void modifyColumn(final double[][] target, final int row, final int column, final DoubleUnaryOperator function) {
        final int tmpLength = target.length;
        for (int i = row; i < tmpLength; i++) {
            target[i][column] = function.applyAsDouble(target[i][column]);
        }
    }

    public static void modifyDiagonal(final double[][] target, final int row, final int column, final DoubleUnaryOperator function) {
        final int tmpLength = target.length;
        for (int ij = 0; ((row + ij) < tmpLength) && ((column + ij) < target[row + ij].length); ij++) {
            target[row + ij][column + ij] = function.applyAsDouble(target[row + ij][column + ij]);
        }
    }

    public static void modifyRow(final double[][] target, final int row, final int column, final DoubleUnaryOperator function) {
        final int tmpLength = target[row].length;
        for (int j = column; j < tmpLength; j++) {
            target[row][j] = function.applyAsDouble(target[row][j]);
        }
    }

    public static void visitAll(final double[][] target, final DoubleConsumer visitor) {
        final int tmpLength = target.length;
        for (int i = 0; i < tmpLength; i++) {
            final int tmpInnerLength = target[i].length;
            for (int j = 0; j < tmpInnerLength; j++) {
                visitor.accept(target[i][j]);
            }
        }
    }

    public static void visitColumn(final double[][] target, final int row, final int column, final DoubleConsumer visitor) {
        final int tmpLength = target[row].length;
        for (int j = column; j < tmpLength; j++) {
            visitor.accept(target[row][j]);
        }
    }

    public static void visitDiagonal(final double[][] target, final int row, final int column, final DoubleConsumer visitor) {
        final int tmpLength = target.length;
        for (int ij = 0; ((row + ij) < tmpLength) && ((column + ij) < target[row + ij].length); ij++) {
            visitor.accept(target[row + ij][column + ij]);
        }
    }

    public static void visitRange(final double[][] target, final int first, final int limit, final DoubleConsumer visitor) {
        final int tmpStructure = target.length;
        for (int index = first; index < limit; index++) {
            visitor.accept(target[Structure2D.row(index, tmpStructure)][Structure2D.column(index, tmpStructure)]);
        }
    }

    public static void visitRow(final double[][] target, final int row, final int column, final DoubleConsumer visitor) {
        final int tmpLength = target.length;
        for (int i = row; i < tmpLength; i++) {
            visitor.accept(target[i][column]);
        }
    }

}
