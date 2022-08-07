/*
 * Copyright 1997-2022 Optimatika
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

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.type.NumberDefinition;
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

    public interface Collectable<N extends Comparable<N>, R extends Mutate2D> extends Structure2D {

        default <I extends R> I collect(final Factory2D<I> factory) {

            I retVal = factory.make(this.countRows(), this.countColumns());

            this.supplyTo(retVal);

            return retVal;
        }

        void supplyTo(R receiver);

    }

    public static class ColumnView<N extends Comparable<N>> implements Access1D<N>, Iterable<ColumnView<N>>, Iterator<ColumnView<N>>,
            Spliterator<ColumnView<N>>, Comparable<ColumnView<N>>, Access1D.Collectable<N, Mutate1D> {

        static final int CHARACTERISTICS = Spliterator.CONCURRENT | Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED
                | Spliterator.SIZED | Spliterator.SORTED | Spliterator.SUBSIZED;

        private long myColumn = -1L;
        private final Access2D<N> myDelegate2D;
        private final long myLastColumn;

        private ColumnView(final Access2D<N> access, final long column, final long lastColumn) {

            super();

            myDelegate2D = access;
            myLastColumn = lastColumn;

            myColumn = column;
        }

        protected ColumnView(final Access2D<N> access) {
            this(access, -1L, access.countColumns() - 1L);
        }

        ColumnView(final Access2D<N> access, final long column) {
            this(access, column, access.countColumns() - 1L);
        }

        public int characteristics() {
            return CHARACTERISTICS;
        }

        public long column() {
            return myColumn;
        }

        public int compareTo(final ColumnView<N> other) {
            return Long.compare(myColumn, other.column());
        }

        public long count() {
            return myDelegate2D.countRows();
        }

        public double doubleValue(final long index) {
            return myDelegate2D.doubleValue(index, myColumn);
        }

        public long estimateSize() {
            return myLastColumn - myColumn;
        }

        public void forEachRemaining(final Consumer<? super ColumnView<N>> action) {
            Iterator.super.forEachRemaining(action);
        }

        public N get(final long index) {
            return myDelegate2D.get(index, myColumn);
        }

        public void goToColumn(final long column) {
            myColumn = column;
        }

        public boolean hasNext() {
            return myColumn < myLastColumn;
        }

        public boolean hasPrevious() {
            return myColumn > 0L;
        }

        public ColumnView<N> iterator() {
            return new ColumnView<>(myDelegate2D);
        }

        public ColumnView<N> next() {
            myColumn++;
            return this;
        }

        public ColumnView<N> previous() {
            myColumn--;
            return this;
        }

        public void remove() {
            ProgrammingError.throwForUnsupportedOptionalOperation();
        }

        public Stream<ColumnView<N>> stream() {
            return StreamSupport.stream(this, false);
        }

        public void supplyTo(final Mutate1D receiver) {
            for (long i = 0L, limit = Math.min(myDelegate2D.countRows(), receiver.count()); i < limit; i++) {
                receiver.set(i, myDelegate2D.get(i, myColumn));
            }
        }

        @Override
        public String toString() {
            return Access1D.toString(this);
        }

        public boolean tryAdvance(final Consumer<? super ColumnView<N>> action) {
            if (this.hasNext()) {
                action.accept(this.next());
                return true;
            } else {
                return false;
            }
        }

        public Spliterator<ColumnView<N>> trySplit() {

            final long remaining = myLastColumn - myColumn;

            if (remaining > 1L) {

                final long split = myColumn + (remaining / 2L);

                final ColumnView<N> retVal = new ColumnView<>(myDelegate2D, myColumn, split);

                myColumn = split;

                return retVal;

            } else {

                return null;
            }
        }

    }

    public static class ElementView<N extends Comparable<N>> implements ElementView2D<N, ElementView<N>> {

        private final ElementView1D<N, ?> myDelegate1D;
        private final long myStructure;

        public ElementView(final ElementView1D<N, ?> delegate, final long structure) {

            super();

            myDelegate1D = delegate;
            myStructure = structure;
        }

        public long column() {
            return Structure2D.column(myDelegate1D.index(), myStructure);
        }

        public double doubleValue() {
            return myDelegate1D.doubleValue();
        }

        public long estimateSize() {
            return myDelegate1D.estimateSize();
        }

        public N get() {
            return myDelegate1D.get();
        }

        public boolean hasNext() {
            return myDelegate1D.hasNext();
        }

        public boolean hasPrevious() {
            return myDelegate1D.hasPrevious();
        }

        public long index() {
            return myDelegate1D.index();
        }

        public ElementView<N> iterator() {
            return new ElementView<>(myDelegate1D.iterator(), myStructure);
        }

        public ElementView<N> next() {
            myDelegate1D.next();
            return this;
        }

        public long nextIndex() {
            return myDelegate1D.nextIndex();
        }

        public ElementView<N> previous() {
            myDelegate1D.previous();
            return this;
        }

        public long previousIndex() {
            return myDelegate1D.previousIndex();
        }

        public long row() {
            return Structure2D.row(myDelegate1D.index(), myStructure);
        }

        public ElementView<N> trySplit() {

            ElementView1D<N, ?> delegateSpliterator = myDelegate1D.trySplit();

            if (delegateSpliterator != null) {
                return new ElementView<>(delegateSpliterator, myStructure);
            }
            return null;
        }

    }

    public static class RowView<N extends Comparable<N>> implements Access1D<N>, Iterable<RowView<N>>, Iterator<RowView<N>>, Spliterator<RowView<N>>,
            Comparable<RowView<N>>, Access1D.Collectable<N, Mutate1D> {

        static final int CHARACTERISTICS = Spliterator.CONCURRENT | Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED
                | Spliterator.SIZED | Spliterator.SORTED | Spliterator.SUBSIZED;

        private final Access2D<N> myDelegate2D;
        private final long myLastRow;
        private long myRow = -1L;

        private RowView(final Access2D<N> access, final long row, final long lastRow) {

            super();

            myDelegate2D = access;
            myLastRow = lastRow;

            myRow = row;
        }

        protected RowView(final Access2D<N> access) {
            this(access, -1L, access.countRows() - 1L);
        }

        RowView(final Access2D<N> access, final long row) {
            this(access, row, access.countRows() - 1L);
        }

        public int characteristics() {
            return CHARACTERISTICS;
        }

        public int compareTo(final RowView<N> other) {
            return Long.compare(myRow, other.row());
        }

        public long count() {
            return myDelegate2D.countColumns();
        }

        public double doubleValue(final long index) {
            return myDelegate2D.doubleValue(myRow, index);
        }

        public long estimateSize() {
            return myLastRow - myRow;
        }

        public void forEachRemaining(final Consumer<? super RowView<N>> action) {
            Iterator.super.forEachRemaining(action);
        }

        public N get(final long index) {
            return myDelegate2D.get(myRow, index);
        }

        public void goToRow(final long row) {
            myRow = row;
        }

        public boolean hasNext() {
            return myRow < myLastRow;
        }

        public boolean hasPrevious() {
            return myRow > 0L;
        }

        public RowView<N> iterator() {
            return new RowView<>(myDelegate2D);
        }

        public RowView<N> next() {
            myRow++;
            return this;
        }

        public RowView<N> previous() {
            myRow--;
            return this;
        }

        public void remove() {
            ProgrammingError.throwForUnsupportedOptionalOperation();
        }

        public long row() {
            return myRow;
        }

        public Stream<RowView<N>> stream() {
            return StreamSupport.stream(this, false);
        }

        public void supplyTo(final Mutate1D receiver) {
            for (long j = 0L, limit = Math.min(myDelegate2D.countColumns(), receiver.count()); j < limit; j++) {
                receiver.set(j, myDelegate2D.get(myRow, j));
            }
        }

        @Override
        public String toString() {
            return Access1D.toString(this);
        }

        public boolean tryAdvance(final Consumer<? super RowView<N>> action) {
            if (this.hasNext()) {
                action.accept(this.next());
                return true;
            } else {
                return false;
            }
        }

        public Spliterator<RowView<N>> trySplit() {

            final long remaining = myLastRow - myRow;

            if (remaining > 1L) {

                final long split = myRow + (remaining / 2L);

                final RowView<N> retVal = new RowView<>(myDelegate2D, myRow, split);

                myRow = split;

                return retVal;

            } else {

                return null;
            }
        }

    }

    public static final class SelectionView<N extends Comparable<N>> implements Access2D<N>, Collectable<N, Mutate2D> {

        private final long[] myColumns;
        private final Access2D<N> myFullData;
        private final long[] myRows;

        SelectionView(final Access2D<N> fullData, final long[] rows, final long[] columns) {

            super();

            myFullData = fullData;
            myRows = Structure1D.replaceNullOrEmptyWithFull(rows, fullData.getRowDim());
            myColumns = Structure1D.replaceNullOrEmptyWithFull(columns, fullData.getColDim());
        }

        public long countColumns() {
            return myColumns.length;
        }

        public long countRows() {
            return myRows.length;
        }

        public double doubleValue(final long row, final long col) {
            return myFullData.doubleValue(myRows[Math.toIntExact(row)], myColumns[Math.toIntExact(col)]);
        }

        public N get(final long row, final long col) {
            return myFullData.get(myRows[Math.toIntExact(row)], myColumns[Math.toIntExact(col)]);
        }

        public void supplyTo(final Mutate2D receiver) {
            for (int j = 0; j < myColumns.length; j++) {
                for (int i = 0; i < myRows.length; i++) {
                    receiver.set(i, j, myFullData.get(myRows[i], myColumns[j]));
                }
            }
        }

        @Override
        public String toString() {
            return Access2D.toString(this);
        }

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
            long tmpStructure = this.countRows();
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
        return new Access2D<>() {

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
            public String toString() {
                return Access2D.toString(this);
            }

        };
    }

    static boolean equals(final Access2D<?> accessA, final Access2D<?> accessB, final NumberContext accuracy) {
        return accessA.countRows() == accessB.countRows() && accessA.countColumns() == accessB.countColumns() && Access1D.equals(accessA, accessB, accuracy);
    }

    static <R extends Mutate2D.Receiver<Double>> Access2D.Collectable<Double, R> newPrimitiveColumnCollectable(final Access1D<?> anything1D) {
        return new Access2D.Collectable<>() {

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
        return new Access2D.Collectable<>() {

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

        if (numbRows > 0 && numbCols > 0 && numbRows <= 50 && numbCols <= 50 && numbRows * numbCols <= 200) {

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
        return new Access2D<>() {

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
            public String toString() {
                return Access2D.toString(this);
            }

        };
    }

    static <N extends Comparable<N>> Access2D<N> wrap(final N[][] target) {
        return new Access2D<>() {

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
                return NumberDefinition.doubleValue(this.get(index));
            }

            public double doubleValue(final long row, final long col) {
                return NumberDefinition.doubleValue(this.get(row, col));
            }

            public N get(final long row, final long col) {
                return target[(int) row][(int) col];
            }

            @Override
            public String toString() {
                return Access2D.toString(this);
            }

        };
    }

    default <NN extends Comparable<NN>, R extends Mutate2D.Receiver<NN>> Collectable<NN, R> asCollectable2D() {
        return new Collectable<>() {

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
        long structure = this.countRows();
        return this.byteValue(Structure2D.row(index, structure), Structure2D.column(index, structure));
    }

    default byte byteValue(final long row, final long col) {
        return (byte) this.shortValue(row, col);
    }

    default ColumnView<N> columns() {
        return new ColumnView<>(this);
    }

    default Access2D<N> columns(final int... columns) {
        return this.select(null, columns);
    }

    default Access2D<N> columns(final long... columns) {
        return this.select(null, columns);
    }

    default double doubleValue(final long index) {
        long structure = this.countRows();
        long row = Structure2D.row(index, structure);
        long col = Structure2D.column(index, structure);
        return this.doubleValue(row, col);
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
        long structure = this.countRows();
        return this.floatValue(Structure2D.row(index, structure), Structure2D.column(index, structure));
    }

    default float floatValue(final long row, final long col) {
        return (float) this.doubleValue(row, col);
    }

    default N get(final long index) {
        long tmpStructure = this.countRows();
        return this.get(Structure2D.row(index, tmpStructure), Structure2D.column(index, tmpStructure));
    }

    N get(long row, long col);

    default int intValue(final long index) {
        long structure = this.countRows();
        return this.intValue(Structure2D.row(index, structure), Structure2D.column(index, structure));
    }

    default int intValue(final long row, final long col) {
        return (int) this.longValue(row, col);
    }

    default long longValue(final long index) {
        long structure = this.countRows();
        return this.longValue(Structure2D.row(index, structure), Structure2D.column(index, structure));
    }

    default long longValue(final long row, final long col) {
        return Math.round(this.doubleValue(row, col));
    }

    default ElementView2D<N, ?> nonzeros() {
        return new Access2D.ElementView<>(Access1D.super.nonzeros(), this.countRows());
    }

    default RowView<N> rows() {
        return new RowView<>(this);
    }

    default Access2D<N> rows(final int... rows) {
        return this.select(rows, null);
    }

    default Access2D<N> rows(final long... rows) {
        return this.select(rows, null);
    }

    default Access2D<N> select(final int[] rows, final int[] columns) {
        return new Access2D.SelectionView<>(this, Structure1D.toLongIndexes(rows), Structure1D.toLongIndexes(columns));
    }

    /**
     * Creates a view of the underlying data structure of only the selected elements. If either the rows or
     * columns input arguments are null or empty arrays, then that transaltes to all rows and/or columns.
     */
    default Access2D<N> select(final long[] rows, final long[] columns) {
        return new Access2D.SelectionView<>(this, rows, columns);
    }

    default short shortValue(final long index) {
        long structure = this.countRows();
        return this.shortValue(Structure2D.row(index, structure), Structure2D.column(index, structure));
    }

    default short shortValue(final long row, final long col) {
        return (short) this.intValue(row, col);
    }

    default double[][] toRawCopy2D() {

        int tmpRowDim = (int) this.countRows();
        int tmpColDim = (int) this.countColumns();

        double[][] retVal = new double[tmpRowDim][tmpColDim];

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
