/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
package org.ojalgo.access;

import org.ojalgo.function.VoidFunction;
import org.ojalgo.scalar.Scalar;

/**
 * 2-dimensional accessor methods
 *
 * @author apete
 */
public interface Access2D<N extends Number> extends Structure2D, Access1D<N> {

    public interface Elements extends Structure2D, Access1D.Elements {

        default boolean isAbsolute(final long index) {
            final long tmpStructure = this.countRows();
            return this.isAbsolute(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure));
        }

        /**
         * @see Scalar#isAbsolute()
         */
        boolean isAbsolute(long row, long column);

        default boolean isSmall(final long index, final double comparedTo) {
            final long tmpStructure = this.countRows();
            return this.isSmall(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), comparedTo);
        }

        /**
         * @see Scalar#isSmall(double)
         */
        boolean isSmall(long row, long column, double comparedTo);

    }

    public interface IndexOf extends Structure2D, Access1D.IndexOf {

        /**
         * @param row
         * @param column
         * @return The row-index of the largest absolute value in a column, starting at the specified row.
         */
        long indexOfLargestInColumn(final long row, final long column);

        /**
         * @param row
         * @param column
         * @return The matrix-index of the largest absolute value on a diagonal, starting at the specified
         *         row-column pair.
         */
        long indexOfLargestInDiagonal(final long row, final long column);

        /**
         * @param row
         * @param column
         * @return The column-index of the largest absolute value in a row, starting at the specified column.
         */
        long indexOfLargestInRow(final long row, final long column);

    }

    public interface Sliceable<N extends Number> extends Structure2D, Access1D.Sliceable<N> {

        Access1D<N> sliceColumn(long row, long column);

        Access1D<N> sliceDiagonal(long row, long column);

        Access1D<N> sliceRow(long row, long column);

    }

    /**
     * A few operations with no 1D or AnyD counterpart.
     *
     * @author apete
     */
    public interface Special<N extends Number> extends Structure2D {

        void exchangeColumns(final long colA, final long colB);

        void exchangeRows(final long rowA, final long rowB);

    }

    public interface Visitable<N extends Number> extends Structure2D, Access1D.Visitable<N> {

        void visitColumn(long row, long column, VoidFunction<N> visitor);

        void visitDiagonal(long row, long column, VoidFunction<N> visitor);

        void visitOne(long row, long column, VoidFunction<N> visitor);

        default void visitOne(final long index, final VoidFunction<N> visitor) {
            final long tmpStructure = this.countRows();
            this.visitOne(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), visitor);
        }

        void visitRow(long row, long column, VoidFunction<N> visitor);

    }

    default Iterable<ColumnView<N>> columns() {
        return ColumnView.makeIterable(this);
    }

    default double doubleValue(final long index) {
        final long tmpStructure = this.countRows();
        return this.doubleValue(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure));
    }

    /**
     * Extracts one element of this matrix as a double.
     *
     * @param row A row index.
     * @param col A column index.
     * @return One matrix element
     */
    double doubleValue(long row, long col);

    default N get(final long index) {
        final long tmpStructure = this.countRows();
        return this.get(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure));
    }

    N get(long row, long column);

    /**
     * Will pass through each matching element position calling the {@code through} function. What happens is
     * entirely dictated by how you implement the callback.
     */
    default void passMatching(final Callback2D<N> through, final Mutate2D to) {
        Callback2D.onMatching(this, through, to);
    }

    default Iterable<RowView<N>> rows() {
        return RowView.makeIterable(this);
    }

    default double[][] toRawCopy2D() {

        final int tmpRowDim = (int) this.countRows();
        final int tmpColDim = (int) this.countColumns();

        final double[][] retVal = new double[tmpRowDim][tmpColDim];

        double[] tmpRow;
        for (int i = 0; i < tmpRowDim; i++) {
            tmpRow = retVal[i];
            for (int j = 0; j < tmpColDim; j++) {
                tmpRow[j] = this.doubleValue(i, j);
            }
        }

        return retVal;
    }

}
