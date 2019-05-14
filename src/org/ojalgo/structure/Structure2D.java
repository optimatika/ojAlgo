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

import org.ojalgo.function.aggregator.Aggregator;

/**
 * A (fixed size) 2-dimensional data structure.
 *
 * @author apete
 */
public interface Structure2D extends Structure1D {

    public final class IntRowColumn implements Comparable<IntRowColumn> {

        public final int column;
        public final int row;
        private transient IntIndex myColumn = null;
        private transient IntIndex myRow = null;

        public IntRowColumn(final int aRow, final int aCol) {

            super();

            row = aRow;
            column = aCol;
        }

        public IntRowColumn(final IntIndex aRow, final IntIndex aCol) {

            super();

            row = aRow.index;
            column = aCol.index;

            myRow = aRow;
            myColumn = aCol;
        }

        @SuppressWarnings("unused")
        private IntRowColumn() {
            this(-1, -1);
        }

        public int compareTo(final IntRowColumn ref) {

            if (column == ref.column) {

                return Integer.compare(row, ref.row);

            } else {

                return Integer.compare(column, ref.column);
            }
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            final IntRowColumn other = (IntRowColumn) obj;
            if (column != other.column) {
                return false;
            }
            if (row != other.row) {
                return false;
            }
            return true;
        }

        public IntIndex getColumn() {
            if (myColumn == null) {
                myColumn = IntIndex.of(column);
            }
            return myColumn;
        }

        public IntIndex getRow() {
            if (myRow == null) {
                myRow = IntIndex.of(row);
            }
            return myRow;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + column;
            result = (prime * result) + row;
            return result;
        }

        @Override
        public String toString() {
            return "<" + Integer.toString(row) + "," + Integer.toString(column) + ">";
        }

    }

    @SuppressWarnings("unchecked")
    interface Logical<S extends Structure2D, B extends Logical<S, B>> extends Structure2D {

        B above(long numberOfRows);

        B above(S... above);

        B above(S above);

        B above(S above1, S above2);

        B below(long numberOfRows);

        B below(S... below);

        B below(S below);

        B below(S below1, S below2);

        B bidiagonal(boolean upper);

        /**
         * @see #columns(int[])
         */
        default B column(final int... columns) {
            return this.columns(columns);
        }

        /**
         * @see #columns(int[])
         */
        default B column(final long... columns) {
            return this.columns(Structure1D.toIntIndexes(columns));
        }

        /**
         * A selection (re-ordering) of columns. Note that it's ok to reference the same base column more than
         * once, and any negative column reference/index will translate to a column of zeros. The number of
         * columns in the resulting matrix is the same as the number of elements in the columns index array.
         */
        B columns(int[] columns);

        /**
         * @see #columns(int[])
         */
        default B columns(final long[] columns) {
            return this.columns(Structure1D.toIntIndexes(columns));
        }

        /**
         * @return A square diagonal matrix (main diagonal only)
         */
        B diagonal();

        /**
         * @param maintain Maintain the original matrix dimensions (resulting matrix not necessarily square).
         * @return A diagonal matrix (main diagonal only)
         */
        B diagonal(boolean maintain);

        B diagonally(S... diagonally);

        S get();

        B hessenberg(boolean upper);

        B left(long numberOfColumns);

        B left(S... left);

        B left(S left);

        B left(S left1, S left2);

        /**
         * Setting either limit to &lt; 0 is interpreted as "no limit" (useful when you only want to limit
         * either the rows or columns, and don't know the size of the other)
         */
        B limits(long rowLimit, long columnLimit);

        B offsets(long rowOffset, long columnOffset);

        B right(long numberOfColumns);

        B right(S... right);

        B right(S right);

        B right(S right1, S right2);

        /**
         * @see #rows(int[])
         */
        default B row(final int... rows) {
            return this.rows(rows);
        }

        /**
         * @see #rows(int[])
         */
        default B row(final long... rows) {
            return this.rows(Structure1D.toIntIndexes(rows));
        }

        /**
         * A selection (re-ordering) of rows. Note that it's ok to reference the same base row more than once,
         * and any negative row reference/index will translate to a row of zeros. The number of rows in the
         * resulting matrix is the same as the number of elements in the rows index array.
         */
        B rows(final int[] rows);

        /**
         * @see #rows(int[])
         */
        default B rows(final long[] rows) {
            return this.rows(Structure1D.toIntIndexes(rows));
        }

        B triangular(boolean upper, boolean assumeOne);

        B tridiagonal();

    }

    public final class LongRowColumn implements Comparable<LongRowColumn> {

        public final long column;
        public final long row;
        private transient LongIndex myColumn = null;
        private transient LongIndex myRow = null;

        public LongRowColumn(final long aRow, final long aCol) {

            super();

            row = aRow;
            column = aCol;
        }

        public LongRowColumn(final LongIndex aRow, final LongIndex aCol) {

            super();

            row = aRow.index;
            column = aCol.index;

            myRow = aRow;
            myColumn = aCol;
        }

        @SuppressWarnings("unused")
        private LongRowColumn() {
            this(-1L, -1L);
        }

        public int compareTo(final LongRowColumn ref) {

            if (column == ref.column) {

                return Long.compare(row, ref.row);

            } else {

                return Long.compare(column, ref.column);
            }
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof LongRowColumn)) {
                return false;
            }
            final LongRowColumn other = (LongRowColumn) obj;
            if (column != other.column) {
                return false;
            }
            if (row != other.row) {
                return false;
            }
            return true;
        }

        public LongIndex getColumn() {
            if (myColumn == null) {
                myColumn = LongIndex.of(column);
            }
            return myColumn;
        }

        public LongIndex getRow() {
            if (myRow == null) {
                myRow = LongIndex.of(row);
            }
            return myRow;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + (int) (column ^ (column >>> 32));
            result = (prime * result) + (int) (row ^ (row >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "<" + Long.toString(row) + "," + Long.toString(column) + ">";
        }

    }

    public interface ReducibleTo1D<R extends Structure1D> extends Structure2D {

        R reduceColumns(Aggregator aggregator);

        R reduceRows(Aggregator aggregator);

    }

    @FunctionalInterface
    public interface RowColumnCallback {

        /**
         * @param row Row
         * @param col Column
         */
        void call(final long row, final long col);

    }

    class RowColumnKey<R, C> {

        public static <R, C> RowColumnKey<R, C> of(final R row, final C col) {
            return new RowColumnKey<>(row, col);
        }

        public final C column;
        public final R row;

        public RowColumnKey(final R row, final C col) {
            super();
            this.row = row;
            this.column = col;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof RowColumnKey)) {
                return false;
            }
            final RowColumnKey other = (RowColumnKey) obj;
            if (column == null) {
                if (other.column != null) {
                    return false;
                }
            } else if (!column.equals(other.column)) {
                return false;
            }
            if (row == null) {
                if (other.row != null) {
                    return false;
                }
            } else if (!row.equals(other.row)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((column == null) ? 0 : column.hashCode());
            result = (prime * result) + ((row == null) ? 0 : row.hashCode());
            return result;
        }

    }

    class RowColumnMapper<R, C> implements IndexMapper<RowColumnKey<R, C>> {

        private final Structure1D.IndexMapper<C> myColumnMapper;
        private final Structure1D.IndexMapper<R> myRowMapper;
        private final long myStructure;

        protected RowColumnMapper(final Structure2D structure, final Structure1D.IndexMapper<R> rowMapper, final Structure1D.IndexMapper<C> columnMapper) {
            super();
            myStructure = structure.countRows();
            myRowMapper = rowMapper;
            myColumnMapper = columnMapper;
        }

        public C toColumnKey(final long index) {
            final long col = Structure2D.column(index, myStructure);
            return myColumnMapper.toKey(col);
        }

        public long toIndex(final R rowKey, final C colKey) {

            final long row = myRowMapper.toIndex(rowKey);
            final long col = myColumnMapper.toIndex(colKey);

            return Structure2D.index(myStructure, row, col);
        }

        public long toIndex(final RowColumnKey<R, C> key) {
            return this.toIndex(key.row, key.column);
        }

        public RowColumnKey<R, C> toKey(final long index) {
            return RowColumnKey.of(this.toRowKey(index), this.toColumnKey(index));
        }

        public R toRowKey(final long index) {
            final long row = Structure2D.row(index, myStructure);
            return myRowMapper.toKey(row);
        }

    }

    static int column(final int index, final int structure) {
        return index / structure;
    }

    static int column(final int index, final int[] structure) {
        return Structure2D.column(index, structure[0]);
    }

    static int column(final long index, final int structure) {
        return (int) (index / structure);
    }

    static long column(final long index, final long structure) {
        return index / structure;
    }

    static long column(final long index, final long[] structure) {
        return Structure2D.column(index, structure[0]);
    }

    static long count(final long numberOfRows, final long numberOfColumnns) {
        return numberOfRows * numberOfColumnns;
    }

    static int index(final int structure, final int row, final int column) {
        return row + (column * structure);
    }

    static long index(final long structure, final long row, final long column) {
        return row + (column * structure);
    }

    static void loopMatching(final Structure2D structureA, final Structure2D structureB, final RowColumnCallback callback) {
        final long tmpCountRows = Math.min(structureA.countRows(), structureB.countRows());
        final long tmpCountColumns = Math.min(structureA.countColumns(), structureB.countColumns());
        for (long j = 0L; j < tmpCountColumns; j++) {
            for (long i = 0L; i < tmpCountRows; i++) {
                callback.call(i, j);
            }
        }
    }

    static <R, C> RowColumnMapper<R, C> mapperOf(final Structure2D structure, final Structure1D.IndexMapper<R> rowMappwer,
            final Structure1D.IndexMapper<C> columnMappwer) {
        return new RowColumnMapper<>(structure, rowMappwer, columnMappwer);
    }

    static int row(final int index, final int structure) {
        return index % structure;
    }

    static int row(final int index, final int[] structure) {
        return Structure2D.row(index, structure[0]);
    }

    static int row(final long index, final int structure) {
        return (int) (index % structure);
    }

    static long row(final long index, final long structure) {
        return index % structure;
    }

    static long row(final long index, final long[] structure) {
        return Structure2D.row(index, structure[0]);
    }

    /**
     * count() == countRows() * countColumns()
     */
    default long count() {
        return this.countRows() * this.countColumns();
    }

    /**
     * @return The number of columns
     */
    long countColumns();

    /**
     * @return The number of rows
     */
    long countRows();

    /**
     * 2D data structures are either square, tall, fat or empty.
     * <p>
     * m &lt;= 0 or n &lt;= 0
     * </p>
     * Historically some ojAlgo data structures did allow to create "empty" instances. Currently this is not
     * encouraged, but still possible in some instances.
     *
     * @return true if matrix is empty
     */
    default boolean isEmpty() {
        return ((this.countRows() <= 0L) || (this.countColumns() <= 0L));
    }

    /**
     * 2D data structures are either square, tall, fat or empty.
     * <p>
     * 1 &lt;= m &lt; n
     * </p>
     *
     * @return true if matrix is fat
     */
    default boolean isFat() {
        final long tmpCountRows = this.countRows();
        return ((tmpCountRows > 0L) && (tmpCountRows < this.countColumns()));
    }

    /**
     * @return true if both the row and column dimensions are equal to 1.
     */
    default boolean isScalar() {
        return (this.countRows() == 1L) && (this.countColumns() == 1L);
    }

    /**
     * 2D data structures are either square, tall, fat or empty.
     * <p>
     * m = n &lt;&gt; 0
     * </p>
     *
     * @return true if matrix is square
     */
    default boolean isSquare() {
        final long tmpCountRows = this.countRows();
        return ((tmpCountRows > 0L) && (tmpCountRows == this.countColumns()));
    }

    /**
     * 2D data structures are either square, tall, fat or empty.
     * <p>
     * m &lt; n &gt;= 1
     * </p>
     *
     * @return true if matrix is tall
     */
    default boolean isTall() {
        final long tmpCountColumns = this.countColumns();
        return ((tmpCountColumns > 0L) && (this.countRows() > tmpCountColumns));
    }

    /**
     * @return true if either the row or column dimensions are equal to 1.
     */
    default boolean isVector() {
        return ((this.countColumns() == 1L) || (this.countRows() == 1L));
    }

    default void loopAll(final RowColumnCallback callback) {
        final long tmpCountRows = this.countRows();
        final long tmpCountColumns = this.countColumns();
        for (long j = 0L; j < tmpCountColumns; j++) {
            for (long i = 0L; i < tmpCountRows; i++) {
                callback.call(i, j);
            }
        }
    }

    default void loopColumn(final long row, final long col, final RowColumnCallback callback) {
        final long tmpCountRows = this.countRows();
        for (long i = row; i < tmpCountRows; i++) {
            callback.call(i, col);
        }
    }

    default void loopColumn(final long col, final RowColumnCallback callback) {
        this.loopColumn(0L, col, callback);
    }

    default void loopDiagonal(final long row, final long col, final RowColumnCallback callback) {
        final long tmpLimit = Math.min(this.countRows() - row, this.countColumns() - col);
        for (long ij = 0L; ij < tmpLimit; ij++) {
            callback.call(row + ij, col + ij);
        }
    }

    default void loopRow(final long row, final long col, final RowColumnCallback callback) {
        final long tmpCountColumns = this.countColumns();
        for (long j = col; j < tmpCountColumns; j++) {
            callback.call(row, j);
        }
    }

    default void loopRow(final long row, final RowColumnCallback callback) {
        this.loopRow(row, 0L, callback);
    }

}
