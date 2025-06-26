/*
 * Copyright 1997-2025 Optimatika
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
package org.ojalgo.structure;

import java.util.function.Consumer;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.operation.FillCompatible;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;

/**
 * 2-dimensional mutator methods
 *
 * @author apete
 */
public interface Mutate2D extends Structure2D, Mutate1D {

    /**
     * A few operations with no 1D or AnyD counterpart.
     *
     * @author apete
     */
    interface Exchangeable extends Structure2D {

        void exchangeColumns(long colA, long colB);

        void exchangeRows(long rowA, long rowB);

    }

    interface Fillable<N extends Comparable<N>> extends Mutate2D, Mutate1D.Fillable<N> {

        default void fillColumn(final long col, final Access1D<N> values) {
            this.fillColumn(0L, col, values);
        }

        default void fillColumn(final long row, final long col, final Access1D<N> values) {
            for (long i = row, limit = this.countRows(); i < limit; i++) {
                this.set(i, col, values.get(i - row));
            }
        }

        default void fillColumn(final long row, final long col, final N value) {
            for (long i = row, limit = this.countRows(); i < limit; i++) {
                this.set(i, col, value);
            }
        }

        default void fillColumn(final long row, final long col, final NullaryFunction<?> supplier) {
            for (long i = row, limit = this.countRows(); i < limit; i++) {
                this.set(i, col, supplier.get());
            }
        }

        default void fillColumn(final long col, final N value) {
            this.fillColumn(0L, col, value);
        }

        default void fillColumn(final long col, final NullaryFunction<?> supplier) {
            this.fillColumn(0L, col, supplier);
        }

        /**
         * 'this' needs to be of a size compatible with the 'left' and 'right' matrices. No checks are
         * performed. The term "compatible" refers to MATLAB's rules for "array broadcasting". The result will
         * be the same as if the 'left' and 'right' matrices where expanded (repeated) so that all three where
         * of the same size, and then the operation was performed. The actual implementation may be more
         * efficient than that.
         *
         * @see https://se.mathworks.com/help/matlab/matlab_prog/compatible-array-sizes-for-basic-operations.html
         */
        default void fillCompatible(final Access2D<N> left, final BinaryFunction<N> operator, final Access2D<N> right) {
            FillCompatible.invoke(this, left, operator, right);
        }

        default void fillDiagonal(final Access1D<N> values) {
            this.fillDiagonal(0L, 0L, values);
        }

        default void fillDiagonal(final long row, final long col, final Access1D<N> values) {
            for (long ij = 0L, limit = Math.min(this.countRows() - row, this.countColumns() - col); ij < limit; ij++) {
                this.set(row + ij, col + ij, values.get(ij));
            }
        }

        default void fillDiagonal(final long row, final long col, final N value) {
            for (long ij = 0L, limit = Math.min(this.countRows() - row, this.countColumns() - col); ij < limit; ij++) {
                this.set(row + ij, col + ij, value);
            }
        }

        default void fillDiagonal(final long row, final long col, final NullaryFunction<?> supplier) {
            for (long ij = 0L, limit = Math.min(this.countRows() - row, this.countColumns() - col); ij < limit; ij++) {
                this.set(row + ij, col + ij, supplier.get());
            }
        }

        default void fillDiagonal(final N value) {
            this.fillDiagonal(0L, 0L, value);
        }

        default void fillDiagonal(final NullaryFunction<?> supplier) {
            this.fillDiagonal(0L, 0L, supplier);
        }

        default void fillRow(final long row, final Access1D<N> values) {
            this.fillRow(row, 0L, values);
        }

        default void fillRow(final long row, final long col, final Access1D<N> values) {
            for (long j = col, limit = this.countColumns(); j < limit; j++) {
                this.set(row, j, values.get(j - col));
            }
        }

        default void fillRow(final long row, final long col, final N value) {
            for (long j = col, limit = this.countColumns(); j < limit; j++) {
                this.set(row, j, value);
            }
        }

        default void fillRow(final long row, final long col, final NullaryFunction<?> supplier) {
            for (long j = col, limit = this.countColumns(); j < limit; j++) {
                this.set(row, j, supplier.get());
            }
        }

        default void fillRow(final long row, final N value) {
            this.fillRow(row, 0L, value);
        }

        default void fillRow(final long row, final NullaryFunction<?> supplier) {
            this.fillRow(row, 0L, supplier);
        }

    }

    interface Mixable<N extends Comparable<N>> extends Structure2D, Mutate1D.Mixable<N> {

        @Override
        default double mix(final long index, final BinaryFunction<N> mixer, final double addend) {
            long structure = this.countRows();
            return this.mix(Structure2D.row(index, structure), Structure2D.column(index, structure), mixer, addend);
        }

        @Override
        default N mix(final long index, final BinaryFunction<N> mixer, final N addend) {
            long structure = this.countRows();
            return this.mix(Structure2D.row(index, structure), Structure2D.column(index, structure), mixer, addend);
        }

        double mix(long row, long col, BinaryFunction<N> mixer, double addend);

        N mix(long row, long col, BinaryFunction<N> mixer, N addend);
    }

    interface Modifiable<N extends Comparable<N>> extends Structure2D, Mutate1D.Modifiable<N> {

        @Override
        default void add(final int index, final byte addend) {
            int structure = this.getRowDim();
            this.add(Structure2D.row(index, structure), Structure2D.column(index, structure), addend);
        }

        @Override
        default void add(final int index, final double addend) {
            int structure = this.getRowDim();
            this.add(Structure2D.row(index, structure), Structure2D.column(index, structure), addend);
        }

        @Override
        default void add(final int index, final float addend) {
            int structure = this.getRowDim();
            this.add(Structure2D.row(index, structure), Structure2D.column(index, structure), addend);
        }

        @Override
        default void add(final int index, final int addend) {
            int structure = this.getRowDim();
            this.add(Structure2D.row(index, structure), Structure2D.column(index, structure), addend);
        }

        @Override
        default void add(final int index, final long addend) {
            int structure = this.getRowDim();
            this.add(Structure2D.row(index, structure), Structure2D.column(index, structure), addend);
        }

        @Override
        default void add(final int index, final short addend) {
            int structure = this.getRowDim();
            this.add(Structure2D.row(index, structure), Structure2D.column(index, structure), addend);
        }

        @Override
        default void add(final long index, final byte addend) {
            long structure = this.countRows();
            this.add(Structure2D.row(index, structure), Structure2D.column(index, structure), addend);
        }

        @Override
        default void add(final long index, final Comparable<?> addend) {
            long structure = this.countRows();
            this.add(Structure2D.row(index, structure), Structure2D.column(index, structure), addend);
        }

        @Override
        default void add(final long index, final double addend) {
            long structure = this.countRows();
            this.add(Structure2D.row(index, structure), Structure2D.column(index, structure), addend);
        }

        @Override
        default void add(final long index, final float addend) {
            long structure = this.countRows();
            this.add(Structure2D.row(index, structure), Structure2D.column(index, structure), addend);
        }

        @Override
        default void add(final long index, final int addend) {
            long structure = this.countRows();
            this.add(Structure2D.row(index, structure), Structure2D.column(index, structure), addend);
        }

        @Override
        default void add(final long index, final long addend) {
            long structure = this.countRows();
            this.add(Structure2D.row(index, structure), Structure2D.column(index, structure), addend);
        }

        default void add(final long row, final long col, final byte addend) {
            this.add(row, col, (short) addend);
        }

        void add(long row, long col, Comparable<?> addend);

        void add(long row, long col, double addend);

        default void add(final long row, final long col, final float addend) {
            this.add(row, col, (double) addend);
        }

        default void add(final long row, final long col, final int addend) {
            this.add(row, col, (long) addend);
        }

        default void add(final long row, final long col, final long addend) {
            this.add(row, col, (double) addend);
        }

        default void add(final long row, final long col, final short addend) {
            this.add(row, col, (int) addend);
        }

        @Override
        default void add(final long index, final short addend) {
            long structure = this.countRows();
            this.add(Structure2D.row(index, structure), Structure2D.column(index, structure), addend);
        }

        default void modifyColumn(final long row, final long col, final UnaryFunction<N> modifier) {
            for (long i = row, limit = this.countRows(); i < limit; i++) {
                this.modifyOne(i, col, modifier);
            }
        }

        default void modifyColumn(final long col, final UnaryFunction<N> modifier) {
            this.modifyColumn(0L, col, modifier);
        }

        default void modifyDiagonal(final long row, final long col, final UnaryFunction<N> modifier) {
            for (long ij = 0L, limit = Math.min(this.countRows() - row, this.countColumns() - col); ij < limit; ij++) {
                this.modifyOne(row + ij, col + ij, modifier);
            }
        }

        default void modifyDiagonal(final UnaryFunction<N> modifier) {
            this.modifyDiagonal(0L, 0L, modifier);
        }

        /**
         * "Matching In Columns" refers to that the supplied, left, data structure will be treated as a
         * column, matching the columns of this structure. Matching columns have the same number of rows.
         * <p>
         * This method will modify all elements of this structure by applying the modifier function to each of
         * them. The left/first argument to the modifier function is taken from the supplied data structure,
         * and the row-index determines which element.
         */
        default void modifyMatchingInColumns(final Access1D<N> left, final BinaryFunction<N> function) {
            for (long r = 0L, limit = Math.min(left.count(), this.countRows()); r < limit; r++) {
                this.modifyRow(r, function.first(left.get(r)));
            }
        }

        /**
         * Same as {@link #modifyMatchingInColumns(Access1D, BinaryFunction)} but with the arguments to the
         * modifier function swapped.
         */
        default void modifyMatchingInColumns(final BinaryFunction<N> function, final Access1D<N> right) {
            for (long r = 0L, limit = Math.min(this.countRows(), right.count()); r < limit; r++) {
                this.modifyRow(r, function.second(right.get(r)));
            }
        }

        /**
         * Same as {@link #modifyMatchingInColumns(Access1D, BinaryFunction)} but now the supplied left data
         * structure is treated as a row. (Matching rows have the same number of columns.)
         */
        default void modifyMatchingInRows(final Access1D<N> left, final BinaryFunction<N> function) {
            for (long c = 0L, limit = Math.min(left.count(), this.countColumns()); c < limit; c++) {
                this.modifyColumn(c, function.first(left.get(c)));
            }
        }

        /**
         * Same as {@link #modifyMatchingInRows(Access1D, BinaryFunction)} but with the arguments to the
         * modifier function swapped.
         */
        default void modifyMatchingInRows(final BinaryFunction<N> function, final Access1D<N> right) {
            for (long c = 0L, limit = Math.min(this.countColumns(), right.count()); c < limit; c++) {
                this.modifyColumn(c, function.second(right.get(c)));
            }
        }

        void modifyOne(long row, long col, UnaryFunction<N> modifier);

        @Override
        default void modifyOne(final long index, final UnaryFunction<N> modifier) {
            long structure = this.countRows();
            this.modifyOne(Structure2D.row(index, structure), Structure2D.column(index, structure), modifier);
        }

        default void modifyRow(final long row, final long col, final UnaryFunction<N> modifier) {
            for (long j = col, limit = this.countColumns(); j < limit; j++) {
                this.modifyOne(row, j, modifier);
            }
        }

        default void modifyRow(final long row, final UnaryFunction<N> modifier) {
            this.modifyRow(row, 0L, modifier);
        }

    }

    /**
     * Apart from extending {@link org.ojalgo.structure.Mutate2D.Receiver} this interface extends
     * {@link org.ojalgo.structure.Mutate2D.Modifiable} and {@link Exchangeable} which both imply access to
     * existing elements as well as {@link Access2D} that dictates explicit access.
     *
     * @author apete
     */
    interface ModifiableReceiver<N extends Comparable<N>> extends Modifiable<N>, Receiver<N>, Exchangeable, Access2D<N> {

        void modifyAny(Transformation2D<N> modifier);

        /**
         * The "compatible" part of the method name references MATLAB's terminology "Compatible Array Sizes".
         * Here the possible combinations are somewhat limited as 'this' is modified in-place.
         *
         * @see https://se.mathworks.com/help/matlab/matlab_prog/compatible-array-sizes-for-basic-operations.html
         */
        default void modifyCompatible(final Access2D<N> left, final BinaryFunction<N> operator) {

            long nbLeftRows = left.countRows();
            long nbLeftCols = left.countColumns();

            long nbRightRows = this.countRows();
            long nbRightCols = this.countColumns();

            if (nbLeftRows != 1L && nbLeftRows != nbRightRows) {
                throw new IllegalArgumentException("Incompatible row dimensions: " + nbLeftRows + " != " + nbRightRows);
            }

            if (nbLeftCols != 1L && nbLeftCols != nbRightCols) {
                throw new IllegalArgumentException("Incompatible column dimensions: " + nbLeftCols + " != " + nbRightCols);
            }

            if (nbLeftRows == 1L && nbLeftCols == 1L) {
                this.modifyAll(operator.first(left.get(0, 0)));
            } else if (nbLeftRows == 1L && nbLeftCols == nbRightCols) {
                this.modifyMatchingInRows(left, operator);
            } else if (nbLeftCols == 1L && nbLeftRows == nbRightRows) {
                this.modifyMatchingInColumns(left, operator);
            } else {
                FillCompatible.invoke(this, left, operator, this);
            }
        }

        default void modifyCompatible(final BinaryFunction<N> operator, final Access2D<N> right) {

            long nbLeftCols = this.countColumns();
            long nbLeftRows = this.countRows();

            long nbRightRows = right.countRows();
            long nbRightCols = right.countColumns();

            if (nbRightRows != 1L && nbRightRows != nbLeftRows) {
                throw new IllegalArgumentException("Incompatible row dimensions: " + nbLeftRows + " != " + nbRightRows);
            }

            if (nbRightCols != 1L && nbRightCols != nbLeftCols) {
                throw new IllegalArgumentException("Incompatible column dimensions: " + nbLeftCols + " != " + nbRightCols);
            }

            if (nbRightRows == 1L && nbRightCols == 1L) {
                this.modifyAll(operator.second(right.get(0, 0)));
            } else if (nbRightRows == 1L && nbRightCols == nbLeftCols) {
                this.modifyMatchingInRows(operator, right);
            } else if (nbRightCols == 1L && nbRightRows == nbLeftRows) {
                this.modifyMatchingInColumns(operator, right);
            } else {
                FillCompatible.invoke(this, this, operator, right);
            }
        }

    }

    interface Receiver<N extends Comparable<N>> extends Fillable<N>, Consumer<Access2D<?>> {

        @Override
        default void accept(final Access2D<?> supplied) {

            if (this.isAcceptable(supplied)) {

                long limitRows = Math.min(this.countRows(), supplied.countRows());
                long limitCols = Math.min(this.countColumns(), supplied.countColumns());
                for (long j = 0L; j < limitCols; j++) {
                    for (long i = 0L; i < limitRows; i++) {
                        this.set(i, j, supplied.get(i, j));
                    }
                }

            } else {
                throw new ProgrammingError("Not acceptable!");
            }
        }

        default boolean isAcceptable(final Structure2D supplier) {
            return this.countRows() >= supplier.countRows() && this.countColumns() >= supplier.countColumns();
        }

    }

    @Override
    default void set(final int index, final byte value) {
        long structure = this.countRows();
        this.set(Structure2D.row(index, structure), Structure2D.column(index, structure), value);
    }

    @Override
    default void set(final int index, final double value) {
        long structure = this.countRows();
        this.set(Structure2D.row(index, structure), Structure2D.column(index, structure), value);
    }

    @Override
    default void set(final int index, final float value) {
        long structure = this.countRows();
        this.set(Structure2D.row(index, structure), Structure2D.column(index, structure), value);
    }

    @Override
    default void set(final int index, final int value) {
        long structure = this.countRows();
        this.set(Structure2D.row(index, structure), Structure2D.column(index, structure), value);
    }

    default void set(final int row, final int col, final byte value) {
        this.set(row, col, (short) value);
    }

    void set(int row, int col, double value);

    default void set(final int row, final int col, final float value) {
        this.set(row, col, (double) value);
    }

    default void set(final int row, final int col, final int value) {
        this.set(row, col, (long) value);
    }

    default void set(final int row, final int col, final long value) {
        this.set(row, col, (double) value);
    }

    default void set(final int row, final int col, final short value) {
        this.set(row, col, (int) value);
    }

    @Override
    default void set(final int index, final long value) {
        long structure = this.countRows();
        this.set(Structure2D.row(index, structure), Structure2D.column(index, structure), value);
    }

    @Override
    default void set(final int index, final short value) {
        long structure = this.countRows();
        this.set(Structure2D.row(index, structure), Structure2D.column(index, structure), value);
    }

    @Override
    default void set(final long index, final byte value) {
        long structure = this.countRows();
        this.set(Structure2D.row(index, structure), Structure2D.column(index, structure), value);
    }

    @Override
    default void set(final long index, final Comparable<?> value) {
        long structure = this.countRows();
        this.set(Structure2D.row(index, structure), Structure2D.column(index, structure), value);
    }

    @Override
    default void set(final long index, final double value) {
        long structure = this.countRows();
        this.set(Structure2D.row(index, structure), Structure2D.column(index, structure), value);
    }

    @Override
    default void set(final long index, final float value) {
        long structure = this.countRows();
        this.set(Structure2D.row(index, structure), Structure2D.column(index, structure), value);
    }

    @Override
    default void set(final long index, final int value) {
        long structure = this.countRows();
        this.set(Structure2D.row(index, structure), Structure2D.column(index, structure), value);
    }

    @Override
    default void set(final long index, final long value) {
        long structure = this.countRows();
        this.set(Structure2D.row(index, structure), Structure2D.column(index, structure), value);
    }

    default void set(final long row, final long col, final byte value) {
        this.set(row, col, (short) value);
    }

    void set(long row, long col, Comparable<?> value);

    default void set(final long row, final long col, final double value) {
        this.set(Math.toIntExact(row), Math.toIntExact(col), value);
    }

    default void set(final long row, final long col, final float value) {
        this.set(row, col, (double) value);
    }

    default void set(final long row, final long col, final int value) {
        this.set(row, col, (long) value);
    }

    default void set(final long row, final long col, final long value) {
        this.set(row, col, (double) value);
    }

    default void set(final long row, final long col, final short value) {
        this.set(row, col, (int) value);
    }

    @Override
    default void set(final long index, final short value) {
        long structure = this.countRows();
        this.set(Structure2D.row(index, structure), Structure2D.column(index, structure), value);
    }

}
