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

import org.ojalgo.function.FunctionUtils;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;

/**
 * 2-dimensional mutator methods
 *
 * @author apete
 */
public interface Mutate2D extends Structure2D, Mutate1D {

    interface BiModifiable<N extends Number> extends Structure2D, Mutate1D.BiModifiable<N> {

    }

    interface Fillable<N extends Number> extends Structure2D, Mutate1D.Fillable<N> {

        default void fillColumn(final long row, final long col, final Access1D<N> values) {
            final long tmpLimit = Math.min(this.countRows() - row, values.count());
            for (long i = 0L; i < tmpLimit; i++) {
                this.fillOne(row + i, col, values.get(i));
            }
        }

        default void fillColumn(final long row, final long col, final N value) {
            final long tmpLimit = this.countRows();
            for (long i = row; i < tmpLimit; i++) {
                this.fillOne(i, col, value);
            }
        }

        default void fillColumn(final long row, final long col, final NullaryFunction<N> supplier) {
            final long tmpLimit = this.countRows();
            for (long i = row; i < tmpLimit; i++) {
                this.fillOne(i, col, supplier);
            }
        }

        default void fillDiagonal(final long row, final long col, final Access1D<N> values) {
            final long tmpLimit = FunctionUtils.min(this.countRows() - row, this.countColumns() - col, values.count());
            for (long ij = 0L; ij < tmpLimit; ij++) {
                this.fillOne(row + ij, col + ij, values.get(ij));
            }
        }

        default void fillDiagonal(final long row, final long col, final N value) {
            final long tmpLimit = Math.min(this.countRows() - row, this.countColumns() - col);
            for (long ij = 0L; ij < tmpLimit; ij++) {
                this.fillOne(row + ij, col + ij, value);
            }
        }

        default void fillDiagonal(final long row, final long col, final NullaryFunction<N> supplier) {
            final long tmpLimit = Math.min(this.countRows() - row, this.countColumns() - col);
            for (long ij = 0L; ij < tmpLimit; ij++) {
                this.fillOne(row + ij, col + ij, supplier);
            }
        }

        default void fillOne(final long index, final Access1D<?> values, final long valueIndex) {
            final long tmpStructure = this.countRows();
            this.fillOne(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), values, valueIndex);
        }

        void fillOne(long row, long col, final Access1D<?> values, final long valueIndex);

        void fillOne(long row, long col, N value);

        void fillOne(long row, long col, NullaryFunction<N> supplier);

        default void fillOne(final long index, final N value) {
            final long tmpStructure = this.countRows();
            this.fillOne(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), value);
        }

        default void fillOne(final long index, final NullaryFunction<N> supplier) {
            final long tmpStructure = this.countRows();
            this.fillOne(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), supplier);
        }

        /**
         * @deprecated v41 Use {@link #fillOne(long,long,Access1D,long)} instead
         */
        @Deprecated
        default void fillOneMatching(final long row, final long col, final Access1D<?> values, final long valueIndex) {
            this.fillOne(row, col, values, valueIndex);
        }

        default void fillRow(final long row, final long col, final Access1D<N> values) {
            final long tmpLimit = Math.min(this.countColumns() - col, values.count());
            for (long j = 0L; j < tmpLimit; j++) {
                this.fillOne(row, col + j, values.get(j));
            }
        }

        default void fillRow(final long row, final long col, final N value) {
            final long tmpLimit = this.countColumns();
            for (long j = col; j < tmpLimit; j++) {
                this.fillOne(row, j, value);
            }
        }

        default void fillRow(final long row, final long col, final NullaryFunction<N> supplier) {
            final long tmpLimit = this.countColumns();
            for (long j = col; j < tmpLimit; j++) {
                this.fillOne(row, j, supplier);
            }
        }

    }

    interface Modifiable<N extends Number> extends Structure2D, Mutate1D.Modifiable<N> {

        default void modifyColumn(final long row, final long col, final UnaryFunction<N> modifier) {
            final long tmpLimit = this.countRows();
            for (long i = row; i < tmpLimit; i++) {
                this.modifyOne(i, col, modifier);
            }
        }

        default void modifyDiagonal(final long row, final long col, final UnaryFunction<N> modifier) {
            final long tmpLimit = Math.min(this.countRows() - row, this.countColumns() - col);
            for (long ij = 0L; ij < tmpLimit; ij++) {
                this.modifyOne(row + ij, col + ij, modifier);
            }
        }

        void modifyOne(long row, long col, UnaryFunction<N> modifier);

        default void modifyOne(final long index, final UnaryFunction<N> modifier) {
            final long tmpStructure = this.countRows();
            this.modifyOne(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), modifier);
        }

        default void modifyRow(final long row, final long col, final UnaryFunction<N> modifier) {
            final long tmpCountColumns = this.countColumns();
            for (long j = col; j < tmpCountColumns; j++) {
                this.modifyOne(row, j, modifier);
            }
        }

    }

    /**
     * A few operations with no 1D or AnyD counterpart.
     *
     * @author apete
     */
    interface Special<N extends Number> extends Structure2D {

        void exchangeColumns(final long colA, final long colB);

        void exchangeRows(final long rowA, final long rowB);

    }

    default void add(final long index, final double addend) {
        final long tmpStructure = this.countRows();
        this.add(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), addend);
    }

    void add(long row, long col, double addend);

    void add(long row, long col, Number addend);

    default void add(final long index, final Number addend) {
        final long tmpStructure = this.countRows();
        this.add(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), addend);
    }

    /**
     * Will pass through each matching element position calling the {@code through} function. What happens is
     * entirely dictated by how you implement the callback.
     */
    default <N extends Number> void passMatching(final Access2D<N> from, final Callback2D<N> through) {
        Callback2D.onMatching(from, through, this);
    }

    default void set(final long index, final double addend) {
        final long tmpStructure = this.countRows();
        this.set(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), addend);
    }

    void set(long row, long col, double value);

    void set(long row, long col, Number value);

    default void set(final long index, final Number addend) {
        final long tmpStructure = this.countRows();
        this.set(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure), addend);
    }

}
