/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.matrix.store.MatrixStore;

public abstract class ArrayUtils {

    public static double[] copyOf(final double[] original) {
        final int tmpLength = original.length;
        final double[] retVal = new double[tmpLength];
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
            final int tmpRow = AccessUtils.row(index, tmpLength);
            final int tmpColumn = AccessUtils.column(index, tmpLength);
            target[tmpRow][tmpColumn] = value;
        }
    }

    public static void fillRange(final double[][] target, final int first, final int limit, final DoubleSupplier supplier) {

        final int tmpLength = target.length;

        for (int index = first; index < limit; index++) {
            final int tmpRow = AccessUtils.row(index, tmpLength);
            final int tmpColumn = AccessUtils.column(index, tmpLength);
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

    /**
     * @deprecated v39 There's now a default method in Access1D that does the same thing.
     */
    @Deprecated
    public static double[] toRawCopyOf(final Access1D<?> original) {

        final int tmpLength = (int) original.count();

        final double[] retVal = new double[tmpLength];

        for (int i = tmpLength; i-- != 0;) {
            retVal[i] = original.doubleValue(i);
        }

        return retVal;
    }

    /**
     * @deprecated v39 There's now a default method in Access2D that does the same thing.
     */
    @Deprecated
    public static double[][] toRawCopyOf(final Access2D<?> original) {

        final int tmpRowDim = (int) original.countRows();
        final int tmpColDim = (int) original.countColumns();

        final double[][] retVal = new double[tmpRowDim][tmpColDim];

        double[] tmpRow;
        for (int i = tmpRowDim; i-- != 0;) {
            tmpRow = retVal[i];
            for (int j = tmpColDim; j-- != 0;) {
                tmpRow[j] = original.doubleValue(i, j);
            }
        }

        return retVal;
    }

    /**
     * @deprecated v39 There's now a default method in Access2D that does the same thing.
     */
    @Deprecated
    public static double[][] toRawCopyOf(final MatrixStore<?> original) {
        return ArrayUtils.toRawCopyOf((Access2D<?>) original);
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
            visitor.accept(target[AccessUtils.row(index, tmpStructure)][AccessUtils.column(index, tmpStructure)]);
        }
    }

    public static void visitRow(final double[][] target, final int row, final int column, final DoubleConsumer visitor) {
        final int tmpLength = target.length;
        for (int i = row; i < tmpLength; i++) {
            visitor.accept(target[i][column]);
        }
    }

    public static Access1D<Double> wrapAccess1D(final double[] target) {
        return new Access1D<Double>() {

            public long count() {
                return target.length;
            }

            public double doubleValue(final long index) {
                return target[(int) index];
            }

            public Double get(final long index) {
                return target[(int) index];
            }

        };
    }

    public static <N extends Number> Access1D<N> wrapAccess1D(final List<? extends N> target) {
        return new Access1D<N>() {

            public long count() {
                return target.size();
            }

            public double doubleValue(final long index) {
                return target.get((int) index).doubleValue();
            }

            public N get(final long index) {
                return target.get((int) index);
            }

        };
    }

    public static <N extends Number> Access1D<N> wrapAccess1D(final N[] target) {
        return new Access1D<N>() {

            public long count() {
                return target.length;
            }

            public double doubleValue(final long index) {
                return target[(int) index].doubleValue();
            }

            public N get(final long index) {
                return target[(int) index];
            }

        };
    }

    public static Access2D<Double> wrapAccess2D(final double[][] target) {
        return new Access2D<Double>() {

            public long count() {
                return target.length * target[0].length;
            }

            public long countColumns() {
                return target[0].length;
            }

            public long countRows() {
                return target.length;
            }

            public double doubleValue(final long row, final long column) {
                return target[(int) row][(int) column];
            }

            public Double get(final long row, final long column) {
                return target[(int) row][(int) column];
            }

        };
    }

    public static <N extends Number> Access2D<N> wrapAccess2D(final N[][] target) {
        return new Access2D<N>() {

            public long count() {
                return target.length * target[0].length;
            }

            public long countColumns() {
                return target[0].length;
            }

            public long countRows() {
                return target.length;
            }

            public double doubleValue(final long index) {
                return this.get(index).doubleValue();
            }

            public double doubleValue(final long row, final long column) {
                return this.get(row, column).doubleValue();
            }

            public N get(final long row, final long column) {
                return target[(int) row][(int) column];
            }

        };
    }

    private ArrayUtils() {
        super();
    }

}
