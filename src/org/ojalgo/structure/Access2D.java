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
package org.ojalgo.structure;

import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * 2-dimensional accessor methods
 *
 * @see Access1D
 * @author apete
 */
public interface Access2D<N extends Comparable<N>> extends Structure2D, Access1D<N> {

    public interface Aggregatable<N extends Comparable<N>> extends Structure2D, Access1D.Aggregatable<N> {

        default N aggregateColumn(final long col, final Aggregator aggregator) {
            return this.aggregateColumn(0L, col, aggregator);
        }

        N aggregateColumn(long row, long col, Aggregator aggregator);

        default N aggregateDiagonal(final Aggregator aggregator) {
            return this.aggregateDiagonal(0L, 0L, aggregator);
        }

        N aggregateDiagonal(long row, long col, Aggregator aggregator);

        default N aggregateRow(final long row, final Aggregator aggregator) {
            return this.aggregateRow(row, 0L, aggregator);
        }

        N aggregateRow(long row, long col, Aggregator aggregator);

        default void reduceColumns(final Aggregator aggregator, final Mutate1D receiver) {
            for (long j = 0L, limit = Math.min(this.countColumns(), receiver.count()); j < limit; j++) {
                receiver.set(j, this.aggregateColumn(j, aggregator));
            }
        }

        default void reduceRows(final Aggregator aggregator, final Mutate1D receiver) {
            for (long i = 0L, limit = Math.min(this.countRows(), receiver.count()); i < limit; i++) {
                receiver.set(i, this.aggregateRow(i, aggregator));
            }
        }

    }

    public interface Collectable<N extends Comparable<N>, R extends Mutate2D.Receiver<N>> extends Structure2D {

        default <I extends R> I collect(final Factory2D<I> factory) {

            final I retVal = factory.make(this.countRows(), this.countColumns());

            this.supplyTo(retVal);

            return retVal;
        }

        void supplyTo(R receiver);

    }

    /**
     * @deprecated v48 Will be removed
     */
    @Deprecated
    public interface Elements extends Structure2D, Access1D.Elements {

        /**
         * @deprecated v48 Will be removed
         */
        @Deprecated
        default boolean isAbsolute(final long index) {
            final long tmpStructure = this.countRows();
            return this.isAbsolute(Structure2D.row(index, tmpStructure), Structure2D.column(index, tmpStructure));
        }

        /**
         * @see Scalar#isAbsolute()
         * @deprecated v48 Will be removed
         */
        @Deprecated
        boolean isAbsolute(long row, long col);

        /**
         * @see Scalar#isSmall(double)
         * @deprecated v48 Will be removed
         */
        @Deprecated
        default boolean isColumnSmall(final long col, final double comparedTo) {
            return this.isColumnSmall(0L, col, comparedTo);
        }

        /**
         * @see Scalar#isSmall(double)
         * @deprecated v48 Will be removed
         */
        @Deprecated
        default boolean isColumnSmall(final long row, final long col, final double comparedTo) {
            boolean retVal = true;
            final long tmpLimit = this.countRows();
            for (long i = row; retVal && (i < tmpLimit); i++) {
                retVal &= this.isSmall(i, col, comparedTo);
            }
            return retVal;
        }

        /**
         * @see Scalar#isSmall(double)
         * @deprecated v48 Will be removed
         */
        @Deprecated
        default boolean isRowSmall(final long row, final double comparedTo) {
            return this.isRowSmall(row, 0L, comparedTo);
        }

        /**
         * @see Scalar#isSmall(double)
         * @deprecated v48 Will be removed
         */
        @Deprecated
        default boolean isRowSmall(final long row, final long col, final double comparedTo) {
            boolean retVal = true;
            final long tmpLimit = this.countColumns();
            for (long j = col; retVal && (j < tmpLimit); j++) {
                retVal &= this.isSmall(row, j, comparedTo);
            }
            return retVal;
        }

        /**
         * @deprecated v48 Will be removed
         */
        @Deprecated
        default boolean isSmall(final long index, final double comparedTo) {
            final long tmpStructure = this.countRows();
            return this.isSmall(Structure2D.row(index, tmpStructure), Structure2D.column(index, tmpStructure), comparedTo);
        }

        /**
         * @see Scalar#isSmall(double)
         * @deprecated v48 Will be removed
         */
        @Deprecated
        boolean isSmall(long row, long col, double comparedTo);

    }

    public static final class ElementView<N extends Comparable<N>> implements ElementView2D<N, ElementView<N>> {

        private final ElementView1D<N, ?> myDelegate;
        private final long myStructure;

        public ElementView(final ElementView1D<N, ?> delegate, final long structure) {

            super();

            myDelegate = delegate;
            myStructure = structure;
        }

        public long column() {
            return Structure2D.column(myDelegate.index(), myStructure);
        }

        public double doubleValue() {
            return myDelegate.doubleValue();
        }

        public long estimateSize() {
            return myDelegate.estimateSize();
        }

        public N get() {
            return myDelegate.get();
        }

        public boolean hasNext() {
            return myDelegate.hasNext();
        }

        public boolean hasPrevious() {
            return myDelegate.hasPrevious();
        }

        public long index() {
            return myDelegate.index();
        }

        public ElementView<N> iterator() {
            return new ElementView<>(myDelegate.iterator(), myStructure);
        }

        public ElementView<N> next() {
            myDelegate.next();
            return this;
        }

        public long nextIndex() {
            return myDelegate.nextIndex();
        }

        public ElementView<N> previous() {
            myDelegate.previous();
            return this;
        }

        public long previousIndex() {
            return myDelegate.previousIndex();
        }

        public long row() {
            return Structure2D.row(myDelegate.index(), myStructure);
        }

        public ElementView<N> trySplit() {

            final ElementView1D<N, ?> delegateSpliterator = myDelegate.trySplit();

            if (delegateSpliterator != null) {
                return new ElementView<>(delegateSpliterator, myStructure);
            } else {
                return null;
            }
        }

    }

    /**
     * @deprecated v48 Will be removed
     */
    @Deprecated
    public interface IndexOf extends Structure2D, Access1D.IndexOf {

        /**
         * @deprecated v48 Will be removed
         */
        @Deprecated
        default long indexOfLargestInColumn(final long col) {
            return this.indexOfLargestInColumn(0L, col);
        }

        /**
         * @param row First row to investigate
         * @param col The column
         * @return The row-index of the largest absolute value in a column, starting at the specified row.
         * @deprecated v48 Will be removed
         */
        @Deprecated
        long indexOfLargestInColumn(final long row, final long col);

        /**
         * @deprecated v48 Will be removed
         */
        @Deprecated
        default long indexOfLargestInRow(final long row) {
            return this.indexOfLargestInRow(row, 0L);
        }

        /**
         * @param row The row
         * @param col The first column to investigate
         * @return The column-index of the largest absolute value in a row, starting at the specified column.
         * @deprecated v48 Will be removed
         */
        @Deprecated
        long indexOfLargestInRow(final long row, final long col);

        /**
         * @deprecated v48 Will be removed
         */
        @Deprecated
        default long indexOfLargestOnDiagonal() {
            return this.indexOfLargestOnDiagonal(0L);
        }

        /**
         * @param first The first row/column to investigate
         * @return The row/column-index of the largest absolute value on the main diagonal, starting at the
         *         specified row/column.
         * @deprecated v48 Will be removed
         */
        @Deprecated
        long indexOfLargestOnDiagonal(final long first);

    }

    public interface Sliceable<N extends Comparable<N>> extends Structure2D, Access1D.Sliceable<N> {

        default Access1D<N> sliceColumn(final long col) {
            return this.sliceColumn(0L, col);
            // return new ColumnView<N>(this, col);
        }

        Access1D<N> sliceColumn(long row, long col);

        default Access1D<N> sliceDiagonal() {
            return this.sliceDiagonal(0L, 0L);
        }

        Access1D<N> sliceDiagonal(long row, long col);

        default Access1D<N> sliceRow(final long row) {
            return this.sliceRow(row, 0L);
        }

        Access1D<N> sliceRow(long row, long col);

    }

    public interface Visitable<N extends Comparable<N>> extends Structure2D, Access1D.Visitable<N> {

        default void visitColumn(final long row, final long col, final VoidFunction<N> visitor) {
            this.loopColumn(row, col, (r, c) -> this.visitOne(r, c, visitor));
        }

        default void visitColumn(final long col, final VoidFunction<N> visitor) {
            this.visitColumn(0L, col, visitor);
        }

        default void visitDiagonal(final long row, final long col, final VoidFunction<N> visitor) {
            this.loopDiagonal(row, col, (r, c) -> this.visitOne(r, c, visitor));
        }

        default void visitDiagonal(final VoidFunction<N> visitor) {
            this.visitDiagonal(0L, 0L, visitor);
        }

        void visitOne(long row, long col, VoidFunction<N> visitor);

        default void visitOne(final long index, final VoidFunction<N> visitor) {
            final long tmpStructure = this.countRows();
            this.visitOne(Structure2D.row(index, tmpStructure), Structure2D.column(index, tmpStructure), visitor);
        }

        default void visitRow(final long row, final long col, final VoidFunction<N> visitor) {
            this.loopRow(row, col, (r, c) -> this.visitOne(r, c, visitor));
        }

        default void visitRow(final long row, final VoidFunction<N> visitor) {
            this.visitRow(row, 0L, visitor);
        }

    }

    static Access2D<Double> asPrimitive2D(final Access2D<?> access) {
        return new Access2D<Double>() {

            public long count() {
                return access.count();
            }

            public long countColumns() {
                return access.countColumns();
            }

            public long countRows() {
                return access.countRows();
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public double doubleValue(final long row, final long col) {
                return access.doubleValue(row, col);
            }

            public Double get(final long index) {
                return access.doubleValue(index);
            }

            public Double get(final long row, final long col) {
                return access.doubleValue(row, col);
            }

            @Override
            public final String toString() {
                return Access2D.toString(this);
            }

        };
    }

    static boolean equals(final Access2D<?> accessA, final Access2D<?> accessB, final NumberContext context) {
        return (accessA.countRows() == accessB.countRows()) && (accessA.countColumns() == accessB.countColumns()) && Access1D.equals(accessA, accessB, context);
    }

    static <R extends Mutate2D.Receiver<Double>> Access2D.Collectable<Double, R> newPrimitiveColumnCollectable(final Access1D<?> anything1D) {
        return new Access2D.Collectable<Double, R>() {

            public long countColumns() {
                return 1L;
            }

            public long countRows() {
                return anything1D.count();
            }

            public void supplyTo(final R receiver) {
                receiver.reset();
                anything1D.nonzeros().forEach(nz -> receiver.set(nz.index(), 0L, nz.doubleValue()));
            }

        };
    }

    static <R extends Mutate2D.Receiver<Double>> Access2D.Collectable<Double, R> newPrimitiveRowCollectable(final Access1D<?> anything1D) {
        return new Access2D.Collectable<Double, R>() {

            public long countColumns() {
                return anything1D.count();
            }

            public long countRows() {
                return 1L;
            }

            public void supplyTo(final R receiver) {
                receiver.reset();
                anything1D.nonzeros().forEach(nz -> receiver.set(0L, nz.index(), nz.doubleValue()));
            }

        };
    }

    static String toString(final Access2D<?> matrix) {

        StringBuilder builder = new StringBuilder();

        int numbRows = Math.toIntExact(matrix.countRows());
        int numbCols = Math.toIntExact(matrix.countColumns());

        builder.append(matrix.getClass().getName());
        builder.append(' ').append('<').append(' ').append(numbRows).append(' ').append('x').append(' ').append(numbCols).append(' ').append('>');

        if ((numbRows > 0) && (numbCols > 0) && (numbRows <= 50) && (numbCols <= 50) && ((numbRows * numbCols) <= 200)) {

            // First element
            builder.append("\n{ { ").append(matrix.get(0, 0));

            // Rest of the first row
            for (int j = 1; j < numbCols; j++) {
                builder.append(",\t").append(matrix.get(0, j));
            }

            // For each of the remaining rows
            for (int i = 1; i < numbRows; i++) {

                // First column
                builder.append(" },\n{ ").append(matrix.get(i, 0));

                // Remaining columns
                for (int j = 1; j < numbCols; j++) {
                    builder.append(",\t").append(matrix.get(i, j));
                }
            }

            // Finish
            builder.append(" } }");
        }

        return builder.toString();
    }

    static Access2D<Double> wrap(final double[][] target) {
        return new Access2D<Double>() {

            public long count() {
                return Structure2D.count(target.length, target[0].length);
            }

            public long countColumns() {
                return target[0].length;
            }

            public long countRows() {
                return target.length;
            }

            public double doubleValue(final long row, final long col) {
                return target[(int) row][(int) col];
            }

            public Double get(final long row, final long col) {
                return target[(int) row][(int) col];
            }

            @Override
            public final String toString() {
                return Access2D.toString(this);
            }

        };
    }

    static <N extends Comparable<N>> Access2D<N> wrap(final N[][] target) {
        return new Access2D<N>() {

            public long count() {
                return Structure2D.count(target.length, target[0].length);
            }

            public long countColumns() {
                return target[0].length;
            }

            public long countRows() {
                return target.length;
            }

            public double doubleValue(final long index) {
                return Scalar.doubleValue(this.get(index));
            }

            public double doubleValue(final long row, final long col) {
                return Scalar.doubleValue(this.get(row, col));
            }

            public N get(final long row, final long col) {
                return target[(int) row][(int) col];
            }

            @Override
            public final String toString() {
                return Access2D.toString(this);
            }

        };
    }

    default <NN extends Comparable<NN>, R extends Mutate2D.Receiver<NN>> Collectable<NN, R> asCollectable2D() {
        return new Collectable<NN, R>() {

            public long countColumns() {
                return Access2D.this.countColumns();
            }

            public long countRows() {
                return Access2D.this.countRows();
            }

            public void supplyTo(final R receiver) {
                receiver.accept(Access2D.this);
            }

        };
    }

    default byte byteValue(final long index) {
        final long structure = this.countRows();
        return this.byteValue(Structure2D.row(index, structure), Structure2D.column(index, structure));
    }

    default byte byteValue(final long row, final long col) {
        return (byte) this.shortValue(row, col);
    }

    default ColumnView<N> columns() {
        return new ColumnView<>(this);
    }

    default double doubleValue(final long index) {
        final long structure = this.countRows();
        return this.doubleValue(Structure2D.row(index, structure), Structure2D.column(index, structure));
    }

    /**
     * Extracts one element of this matrix as a double.
     *
     * @param row A row index.
     * @param col A column index.
     * @return One matrix element
     */
    double doubleValue(long row, long col);

    default ElementView2D<N, ?> elements() {
        return new Access2D.ElementView<>(Access1D.super.elements(), this.countRows());
    }

    default float floatValue(final long index) {
        final long structure = this.countRows();
        return this.floatValue(Structure2D.row(index, structure), Structure2D.column(index, structure));
    }

    default float floatValue(final long row, final long col) {
        return (float) this.doubleValue(row, col);
    }

    default N get(final long index) {
        final long tmpStructure = this.countRows();
        return this.get(Structure2D.row(index, tmpStructure), Structure2D.column(index, tmpStructure));
    }

    N get(long row, long col);

    default int intValue(final long index) {
        final long structure = this.countRows();
        return this.intValue(Structure2D.row(index, structure), Structure2D.column(index, structure));
    }

    default int intValue(final long row, final long col) {
        return (int) this.longValue(row, col);
    }

    default long longValue(final long index) {
        final long structure = this.countRows();
        return this.longValue(Structure2D.row(index, structure), Structure2D.column(index, structure));
    }

    default long longValue(final long row, final long col) {
        return Math.round(this.doubleValue(row, col));
    }

    default RowView<N> rows() {
        return new RowView<>(this);
    }

    default short shortValue(final long index) {
        final long structure = this.countRows();
        return this.shortValue(Structure2D.row(index, structure), Structure2D.column(index, structure));
    }

    default short shortValue(final long row, final long col) {
        return (short) this.intValue(row, col);
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
