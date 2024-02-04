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

import java.util.function.DoubleConsumer;

import org.ojalgo.structure.Structure2D;

public abstract class VisitAll implements ArrayOperation {

    public static int THRESHOLD = 128;

    public static void visit(final double[] target, final DoubleConsumer visitor) {
        VisitAll.visit(target, 0, target.length, visitor);
    }

    public static void visit(final double[] target, final int first, final int limit, final DoubleConsumer visitor) {
        for (int i = first, lim = Math.min(limit, target.length); i < lim; i++) {
            visitor.accept(target[i]);
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
        for (int ij = 0; row + ij < limit && col + ij < target[row + ij].length; ij++) {
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

}
