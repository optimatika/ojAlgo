/*
 * Copyright 1997-2026 Optimatika
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

import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.function.UnaryFunction;

public abstract class ModifyAll implements ArrayOperation {

    public static int THRESHOLD = 64;

    private static final DivideAndConquer.Divider DIVIDER = Parallelism.THREADS.newDivider();

    public static void modifyAll(final double[] data, final int structure, final UnaryFunction<Double> function) {

        int nbCols = structure > 0 ? data.length / structure : 0;

        if (nbCols > THRESHOLD) {
            DIVIDER.threshold(THRESHOLD).divide(0, nbCols, (f, l) -> OperationUnary.invoke(data, structure * f, structure * l, 1, data, function));
        } else {
            OperationUnary.invoke(data, 0, structure * nbCols, 1, data, function);
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

    public static <N extends Comparable<N>> void modifyAll(final N[] data, final int structure, final UnaryFunction<N> function) {

        int nbCols = structure > 0 ? data.length / structure : 0;

        if (nbCols > THRESHOLD) {
            DIVIDER.threshold(THRESHOLD).divide(0, nbCols, (f, l) -> ModifyAll.modify(data, structure * f, structure * l, function));
        } else {
            ModifyAll.modify(data, 0, structure * nbCols, function);
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

    private static <N extends Comparable<N>> void modify(final N[] data, final int first, final int limit, final UnaryFunction<N> function) {
        for (int i = first; i < limit; i++) {
            data[i] = function.invoke(data[i]);
        }
    }

}
