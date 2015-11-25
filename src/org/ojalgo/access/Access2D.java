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
package org.ojalgo.access;

import java.util.List;

import org.ojalgo.array.ArrayUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.scalar.Scalar;

public interface Access2D<N extends Number> extends Structure2D, Access1D<N> {

    /**
     * This interface mimics {@linkplain Fillable}, but methods return the builder instance instead, and then
     * adds the {@link #build()} method.
     *
     * @author apete
     */
    public interface Builder<I extends Access2D<?>> extends Structure2D, Access1D.Builder<I> {

        I build();

        Builder<I> fillColumn(long row, long column, Number value);

        Builder<I> fillDiagonal(long row, long column, Number value);

        Builder<I> fillRow(long row, long column, Number value);

        Builder<I> set(long row, long column, double value);

        Builder<I> set(long row, long column, Number value);

    }

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

        /**
         * @see Scalar#isZero()
         * @deprecated v37
         */
        @Deprecated
        default boolean isZero(final long row, final long column) {
            return this.isSmall(row, column, PrimitiveMath.ONE);
        }

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

    public interface Factory<I extends Access2D<?>> {

        I columns(Access1D<?>... source);

        I columns(double[]... source);

        @SuppressWarnings("unchecked")
        I columns(List<? extends Number>... source);

        I columns(Number[]... source);

        I copy(Access2D<?> source);

        I makeEye(long rows, long columns);

        I makeFilled(long rows, long columns, NullaryFunction<?> supplier);

        I makeZero(long rows, long columns);

        I rows(Access1D<?>... source);

        I rows(double[]... source);

        @SuppressWarnings("unchecked")
        I rows(List<? extends Number>... source);

        I rows(Number[]... source);

    }

    public interface Fillable<N extends Number> extends Settable<N>, Access1D.Fillable<N> {

        default void fillColumn(final long row, final long column, final Access1D<N> values) {
            final long tmpCount = values.count();
            for (long i = 0L; i < tmpCount; i++) {
                this.set(row + i, column, values.get(i));
            }
        }

        void fillColumn(long row, long column, N value);

        void fillColumn(long row, long column, NullaryFunction<N> supplier);

        default void fillDiagonal(final long row, final long column, final Access1D<N> values) {
            for (long ij = 0L; ij < values.count(); ij++) {
                this.set(row + ij, column + ij, values.get(ij));
            }
        }

        void fillDiagonal(long row, long column, N value);

        void fillDiagonal(long row, long column, NullaryFunction<N> supplier);

        void fillOne(long row, long column, N value);

        void fillOne(long row, long column, NullaryFunction<N> supplier);

        default void fillOne(final long index, final N value) {
            final long tmpStructure = this.countRows();
            this.fillOne(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), value);
        }

        default void fillOne(final long index, final NullaryFunction<N> supplier) {
            final long tmpStructure = this.countRows();
            this.fillOne(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), supplier);
        }

        default void fillOneMatching(final long index, final Access1D<?> values, final long valueIndex) {
            final long tmpStructure = this.countRows();
            this.fillOneMatching(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), values, valueIndex);
        }

        void fillOneMatching(long row, long column, final Access1D<?> values, final long valueIndex);

        default void fillRange(final long first, final long limit, final N value) {
            for (long i = first; i < limit; i++) {
                this.fillOne(i, value);
            }
        }

        default void fillRange(final long first, final long limit, final NullaryFunction<N> supplier) {
            for (long i = first; i < limit; i++) {
                this.fillOne(i, supplier);
            }
        }

        default void fillRow(final long row, final long column, final Access1D<N> values) {
            for (long j = 0L; j < values.count(); j++) {
                this.set(row, column + j, values.get(j));
            }
        }

        void fillRow(long row, long column, N value);

        void fillRow(long row, long column, NullaryFunction<N> supplier);

    }

    public interface Iterable2D<N extends Number> extends Access2D<N> {

        default Iterable<Access1D<N>> columns() {
            return ColumnsIterator.make(this);
        }

        default Iterable<Access1D<N>> rows() {
            return RowsIterator.make(this);
        }
    }

    public interface Modifiable<N extends Number> extends Settable<N>, Access1D.Modifiable<N> {

        void modifyColumn(long row, long column, UnaryFunction<N> function);

        void modifyDiagonal(long row, long column, UnaryFunction<N> function);

        void modifyOne(long row, long column, UnaryFunction<N> function);

        default void modifyOne(final long index, final UnaryFunction<N> function) {
            final long tmpStructure = this.countRows();
            this.modifyOne(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), function);
        }

        default void modifyRange(final long first, final long limit, final UnaryFunction<N> function) {
            for (long i = first; i < limit; i++) {
                this.modifyOne(i, function);
            }
        }

        void modifyRow(long row, long column, UnaryFunction<N> function);

    }

    public interface Settable<N extends Number> extends Structure2D, Access1D.Settable<N> {

        default void add(final long index, final double addend) {
            final long tmpStructure = this.countRows();
            this.add(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), addend);
        }

        void add(long row, long column, double addend);

        void add(long row, long column, Number addend);

        default void add(final long index, final Number addend) {
            final long tmpStructure = this.countRows();
            this.add(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), addend);
        }

        default void set(final long index, final double addend) {
            final long tmpStructure = this.countRows();
            this.set(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), addend);
        }

        void set(long row, long column, double value);

        void set(long row, long column, Number value);

        default void set(final long index, final Number addend) {
            final long tmpStructure = this.countRows();
            this.set(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), addend);
        }

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

    default double doubleValue(final long index) {
        final long tmpStructure = this.countRows();
        return this.doubleValue(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure));
    }

    /**
     * Extracts one element of this matrix as a double.
     *
     * @param row A row index.
     * @param column A column index.
     * @return One matrix element
     */
    double doubleValue(long row, long column);

    default N get(final long index) {
        final long tmpStructure = this.countRows();
        return this.get(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure));
    }

    N get(long row, long column);

    default double[][] toRawCopy2D() {
        return ArrayUtils.toRawCopyOf(this);
    }

}
