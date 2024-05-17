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
package org.ojalgo.matrix.store;

import java.util.Arrays;
import java.util.function.Consumer;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.operation.MultiplyBoth;
import org.ojalgo.structure.*;

abstract class Subregion2D<N extends Comparable<N>> implements TransformableRegion<N> {

    static final class ColumnsRegion<N extends Comparable<N>> extends Subregion2D<N> {

        private final TransformableRegion<N> myBase;
        private final int[] myColumns;

        /**
         * @param base
         * @param multiplier
         * @param columns
         */
        ColumnsRegion(final TransformableRegion<N> base, final TransformableRegion.FillByMultiplying<N> multiplier, final int... columns) {
            super(multiplier, base.countRows(), columns.length);
            myBase = base;
            myColumns = columns;
        }

        @Override
        public void add(final long row, final long col, final Comparable<?> addend) {
            myBase.add(row, myColumns[(int) col], addend);
        }

        @Override
        public void add(final long row, final long col, final double addend) {
            myBase.add(row, myColumns[(int) col], addend);
        }

        @Override
        public long countColumns() {
            return myColumns.length;
        }

        @Override
        public long countRows() {
            return myBase.countRows();
        }

        @Override
        public double doubleValue(final int row, final int col) {
            return myBase.doubleValue(row, myColumns[col]);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ColumnsRegion)) {
                return false;
            }
            ColumnsRegion other = (ColumnsRegion) obj;
            if (myBase == null) {
                if (other.myBase != null) {
                    return false;
                }
            } else if (!myBase.equals(other.myBase)) {
                return false;
            }
            if (!Arrays.equals(myColumns, other.myColumns)) {
                return false;
            }
            return true;
        }

        @Override
        public void fillColumn(final long row, final long col, final Access1D<N> values) {
            myBase.fillColumn(row, myColumns[Math.toIntExact(col)], values);
        }

        @Override
        public void fillColumn(final long row, final long col, final N value) {
            myBase.fillColumn(row, myColumns[Math.toIntExact(col)], value);
        }

        @Override
        public void fillColumn(final long row, final long col, final NullaryFunction<?> supplier) {
            myBase.fillColumn(row, myColumns[Math.toIntExact(col)], supplier);
        }

        @Override
        public N get(final long row, final long col) {
            return myBase.get(row, myColumns[Math.toIntExact(col)]);
        }

        @Override
        public int getColDim() {
            return myColumns.length;
        }

        @Override
        public int getRowDim() {
            return myBase.getRowDim();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (myBase == null ? 0 : myBase.hashCode());
            return prime * result + Arrays.hashCode(myColumns);
        }

        @Override
        public void modifyColumn(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyColumn(row, myColumns[(int) col], modifier);
        }

        @Override
        public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyOne(row, myColumns[(int) col], modifier);
        }

        @Override
        public void set(final int row, final int col, final double value) {
            myBase.set(row, myColumns[col], value);
        }

        @Override
        public void set(final long row, final long col, final Comparable<?> value) {
            myBase.set(row, myColumns[Math.toIntExact(col)], value);
        }

    }

    static final class LimitRegion<N extends Comparable<N>> extends Subregion2D<N> {

        private final TransformableRegion<N> myBase;
        private final int myRowLimit, myColumnLimit; // limits

        LimitRegion(final TransformableRegion<N> base, final TransformableRegion.FillByMultiplying<N> multiplier, final int rowLimit, final int columnLimit) {
            super(multiplier, rowLimit, columnLimit);
            myBase = base;
            myRowLimit = rowLimit;
            myColumnLimit = columnLimit;
        }

        @Override
        public void add(final long row, final long col, final Comparable<?> addend) {
            myBase.add(row, col, addend);
        }

        @Override
        public void add(final long row, final long col, final double addend) {
            myBase.add(row, col, addend);
        }

        @Override
        public long countColumns() {
            return myColumnLimit;
        }

        @Override
        public long countRows() {
            return myRowLimit;
        }

        @Override
        public double doubleValue(final int row, final int col) {
            return myBase.doubleValue(row, col);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof LimitRegion)) {
                return false;
            }
            LimitRegion other = (LimitRegion) obj;
            if (myBase == null) {
                if (other.myBase != null) {
                    return false;
                }
            } else if (!myBase.equals(other.myBase)) {
                return false;
            }
            if (myColumnLimit != other.myColumnLimit || myRowLimit != other.myRowLimit) {
                return false;
            }
            return true;
        }

        @Override
        public N get(final long row, final long col) {
            return myBase.get(row, col);
        }

        @Override
        public int getColDim() {
            return myColumnLimit;
        }

        @Override
        public int getRowDim() {
            return myRowLimit;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (myBase == null ? 0 : myBase.hashCode());
            result = prime * result + myColumnLimit;
            return prime * result + myRowLimit;
        }

        @Override
        public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyOne(row, col, modifier);
        }

        @Override
        public void set(final int row, final int col, final double value) {
            myBase.set(row, col, value);
        }

        @Override
        public void set(final long row, final long col, final Comparable<?> value) {
            myBase.set(row, col, value);
        }

    }

    static final class OffsetRegion<N extends Comparable<N>> extends Subregion2D<N> {

        private final TransformableRegion<N> myBase;
        private final int myRowOffset, myColumnOffset; // origin/offset

        OffsetRegion(final TransformableRegion<N> base, final TransformableRegion.FillByMultiplying<N> multiplier, final int rowOffset,
                final int columnOffset) {
            super(multiplier, base.countRows() - rowOffset, base.countColumns() - columnOffset);
            myBase = base;
            myRowOffset = rowOffset;
            myColumnOffset = columnOffset;
        }

        @Override
        public void add(final long row, final long col, final Comparable<?> addend) {
            myBase.add(myRowOffset + row, myColumnOffset + col, addend);
        }

        @Override
        public void add(final long row, final long col, final double addend) {
            myBase.add(myRowOffset + row, myColumnOffset + col, addend);
        }

        @Override
        public long countColumns() {
            return myBase.countColumns() - myColumnOffset;
        }

        @Override
        public long countRows() {
            return myBase.countRows() - myRowOffset;
        }

        @Override
        public double doubleValue(final int row, final int col) {
            return myBase.doubleValue(myRowOffset + row, myColumnOffset + col);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof OffsetRegion)) {
                return false;
            }
            OffsetRegion other = (OffsetRegion) obj;
            if (myBase == null) {
                if (other.myBase != null) {
                    return false;
                }
            } else if (!myBase.equals(other.myBase)) {
                return false;
            }
            if (myColumnOffset != other.myColumnOffset || myRowOffset != other.myRowOffset) {
                return false;
            }
            return true;
        }

        @Override
        public void fillAll(final N value) {
            final long tmpCountColumns = myBase.countColumns();
            for (long j = myColumnOffset; j < tmpCountColumns; j++) {
                myBase.fillColumn(myRowOffset, j, value);
            }
        }

        @Override
        public void fillAll(final NullaryFunction<?> supplier) {
            final long tmpCountColumns = myBase.countColumns();
            for (long j = myColumnOffset; j < tmpCountColumns; j++) {
                myBase.fillColumn(myRowOffset, j, supplier);
            }
        }

        @Override
        public void fillColumn(final long row, final long col, final N value) {
            myBase.fillColumn(myRowOffset + row, myColumnOffset + col, value);
        }

        @Override
        public void fillColumn(final long row, final long col, final NullaryFunction<?> supplier) {
            myBase.fillColumn(myRowOffset + row, myColumnOffset + col, supplier);
        }

        @Override
        public void fillDiagonal(final long row, final long col, final N value) {
            myBase.fillDiagonal(myRowOffset + row, myColumnOffset + col, value);
        }

        @Override
        public void fillDiagonal(final long row, final long col, final NullaryFunction<?> supplier) {
            myBase.fillDiagonal(myRowOffset + row, myColumnOffset + col, supplier);
        }

        @Override
        public void fillRow(final long row, final long col, final N value) {
            myBase.fillRow(myRowOffset + row, myColumnOffset + col, value);
        }

        @Override
        public void fillRow(final long row, final long col, final NullaryFunction<?> supplier) {
            myBase.fillRow(myRowOffset + row, myColumnOffset + col, supplier);
        }

        @Override
        public N get(final long row, final long col) {
            return myBase.get(myRowOffset + row, myColumnOffset + col);
        }

        @Override
        public int getColDim() {
            return myBase.getColDim() - myColumnOffset;
        }

        @Override
        public int getRowDim() {
            return myBase.getRowDim() - myRowOffset;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (myBase == null ? 0 : myBase.hashCode());
            result = prime * result + myColumnOffset;
            return prime * result + myRowOffset;
        }

        @Override
        public void modifyAll(final UnaryFunction<N> modifier) {
            for (long j = myColumnOffset; j < myBase.countColumns(); j++) {
                myBase.modifyColumn(myRowOffset, j, modifier);
            }
        }

        @Override
        public void modifyColumn(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyColumn(myRowOffset + row, myColumnOffset + col, modifier);
        }

        @Override
        public void modifyDiagonal(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyDiagonal(myRowOffset + row, myColumnOffset + col, modifier);
        }

        @Override
        public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyOne(myRowOffset + row, myColumnOffset + col, modifier);
        }

        @Override
        public void modifyRow(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyRow(myRowOffset + row, myColumnOffset + col, modifier);
        }

        @Override
        public void set(final int row, final int col, final double value) {
            myBase.set(myRowOffset + row, myColumnOffset + col, value);
        }

        @Override
        public void set(final long row, final long col, final Comparable<?> value) {
            myBase.set(myRowOffset + row, myColumnOffset + col, value);
        }

    }

    static final class RowsRegion<N extends Comparable<N>> extends Subregion2D<N> {

        private final TransformableRegion<N> myBase;
        private final int[] myRows;

        RowsRegion(final TransformableRegion<N> base, final TransformableRegion.FillByMultiplying<N> multiplier, final int... rows) {
            super(multiplier, rows.length, base.countColumns());
            myBase = base;
            myRows = rows;
        }

        @Override
        public void add(final long row, final long col, final Comparable<?> addend) {
            myBase.add(myRows[(int) row], col, addend);
        }

        @Override
        public void add(final long row, final long col, final double addend) {
            myBase.add(myRows[(int) row], col, addend);
        }

        @Override
        public long countColumns() {
            return myBase.countColumns();
        }

        @Override
        public long countRows() {
            return myRows.length;
        }

        @Override
        public double doubleValue(final int row, final int col) {
            return myBase.doubleValue(myRows[row], col);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof RowsRegion)) {
                return false;
            }
            RowsRegion other = (RowsRegion) obj;
            if (myBase == null) {
                if (other.myBase != null) {
                    return false;
                }
            } else if (!myBase.equals(other.myBase)) {
                return false;
            }
            if (!Arrays.equals(myRows, other.myRows)) {
                return false;
            }
            return true;
        }

        @Override
        public void fillRow(final long row, final long col, final Access1D<N> values) {
            myBase.fillRow(myRows[Math.toIntExact(row)], col, values);
        }

        @Override
        public void fillRow(final long row, final long col, final N value) {
            myBase.fillRow(myRows[Math.toIntExact(row)], col, value);
        }

        @Override
        public void fillRow(final long row, final long col, final NullaryFunction<?> supplier) {
            myBase.fillRow(myRows[Math.toIntExact(row)], col, supplier);
        }

        @Override
        public N get(final long row, final long col) {
            return myBase.get(myRows[Math.toIntExact(row)], col);
        }

        @Override
        public int getColDim() {
            return myBase.getColDim();
        }

        @Override
        public int getRowDim() {
            return myRows.length;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (myBase == null ? 0 : myBase.hashCode());
            return prime * result + Arrays.hashCode(myRows);
        }

        @Override
        public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyOne(myRows[(int) row], col, modifier);
        }

        @Override
        public void modifyRow(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyRow(myRows[(int) row], col, modifier);
        }

        @Override
        public void set(final int row, final int col, final double value) {
            myBase.set(myRows[row], col, value);
        }

        @Override
        public void set(final long row, final long col, final Comparable<?> value) {
            myBase.set(myRows[Math.toIntExact(row)], col, value);
        }

    }

    static final class SynchronizedRegion<N extends Comparable<N>> extends Subregion2D<N> {

        private final TransformableRegion<N> myDelegate;

        SynchronizedRegion(final TransformableRegion<N> base) {
            super(Subregion2D.findMultiplier(base.get(0, 0).getClass(), base.getRowDim(), base.getColDim()), base.getRowDim(), base.getColDim());
            myDelegate = base;
        }

        @Override
        public synchronized void accept(final Access2D<?> supplied) {
            myDelegate.accept(supplied);
        }

        @Override
        public synchronized void add(final long index, final byte addend) {
            myDelegate.add(index, addend);
        }

        @Override
        public synchronized void add(final long index, final Comparable<?> addend) {
            myDelegate.add(index, addend);
        }

        @Override
        public synchronized void add(final long index, final double addend) {
            myDelegate.add(index, addend);
        }

        @Override
        public synchronized void add(final long index, final float addend) {
            myDelegate.add(index, addend);
        }

        @Override
        public synchronized void add(final long index, final int addend) {
            myDelegate.add(index, addend);
        }

        @Override
        public synchronized void add(final long index, final long addend) {
            myDelegate.add(index, addend);
        }

        @Override
        public synchronized void add(final long row, final long col, final byte addend) {
            myDelegate.add(row, col, addend);
        }

        @Override
        public synchronized void add(final long row, final long col, final Comparable<?> addend) {
            myDelegate.add(row, col, addend);
        }

        @Override
        public synchronized void add(final long row, final long col, final double addend) {
            myDelegate.add(row, col, addend);
        }

        @Override
        public synchronized void add(final long row, final long col, final float addend) {
            myDelegate.add(row, col, addend);
        }

        @Override
        public synchronized void add(final long row, final long col, final int addend) {
            myDelegate.add(row, col, addend);
        }

        @Override
        public synchronized void add(final long row, final long col, final long addend) {
            myDelegate.add(row, col, addend);
        }

        @Override
        public synchronized void add(final long row, final long col, final short addend) {
            myDelegate.add(row, col, addend);
        }

        @Override
        public synchronized void add(final long index, final short addend) {
            myDelegate.add(index, addend);
        }

        @Override
        public synchronized Consumer<Access2D<?>> andThen(final Consumer<? super Access2D<?>> after) {
            return myDelegate.andThen(after);
        }

        @Override
        public synchronized <NN extends Comparable<NN>, R extends Mutate1D.Receiver<NN>> Access1D.Collectable<NN, R> asCollectable1D() {
            return myDelegate.asCollectable1D();
        }

        @Override
        public synchronized <NN extends Comparable<NN>, R extends Mutate2D.Receiver<NN>> Collectable<NN, R> asCollectable2D() {
            return myDelegate.asCollectable2D();
        }

        @Override
        public synchronized <K> Keyed1D<K, N> asKeyed1D(final IndexMapper<K> indexMapper) {
            return myDelegate.asKeyed1D(indexMapper);
        }

        @Override
        public synchronized <R, C> Keyed2D<R, C, N> asKeyed2D(final IndexMapper<R> rowMapper, final IndexMapper<C> columnMapper) {
            return myDelegate.asKeyed2D(rowMapper, columnMapper);
        }

        @Override
        public synchronized void axpy(final double a, final org.ojalgo.structure.Mutate1D.Modifiable<?> y) {
            myDelegate.axpy(a, y);
        }

        @Override
        public synchronized byte byteValue(final int index) {
            return myDelegate.byteValue(index);
        }

        @Override
        public synchronized byte byteValue(final int row, final int col) {
            return myDelegate.byteValue(row, col);
        }

        @Override
        public synchronized byte byteValue(final long index) {
            return myDelegate.byteValue(index);
        }

        @Override
        public synchronized byte byteValue(final long row, final long col) {
            return myDelegate.byteValue(row, col);
        }

        @Override
        public synchronized ColumnView<N> columns() {
            return myDelegate.columns();
        }

        @Override
        public synchronized Access2D<N> columns(final int... columns) {
            return myDelegate.columns(columns);
        }

        @Override
        public synchronized Access2D<N> columns(final long... columns) {
            return myDelegate.columns(columns);
        }

        @Override
        public synchronized long count() {
            return myDelegate.count();
        }

        @Override
        public synchronized long countColumns() {
            return myDelegate.countColumns();
        }

        @Override
        public synchronized long countRows() {
            return myDelegate.countRows();
        }

        @Override
        public synchronized double dot(final Access1D<?> vector) {
            return myDelegate.dot(vector);
        }

        @Override
        public synchronized double doubleValue(final int index) {
            return myDelegate.doubleValue(index);
        }

        @Override
        public synchronized double doubleValue(final int row, final int col) {
            return myDelegate.doubleValue(row, col);
        }

        @Override
        public synchronized double doubleValue(final long index) {
            return myDelegate.doubleValue(index);
        }

        @Override
        public synchronized double doubleValue(final long row, final long col) {
            return myDelegate.doubleValue(row, col);
        }

        @Override
        public synchronized ElementView2D<N, ?> elements() {
            return myDelegate.elements();
        }

        @Override
        public synchronized void exchangeColumns(final long colA, final long colB) {
            myDelegate.exchangeColumns(colA, colB);
        }

        @Override
        public synchronized void exchangeRows(final long rowA, final long rowB) {
            myDelegate.exchangeRows(rowA, rowB);
        }

        @Override
        public synchronized void fillAll(final N value) {
            myDelegate.fillAll(value);
        }

        @Override
        public synchronized void fillAll(final NullaryFunction<?> supplier) {
            myDelegate.fillAll(supplier);
        }

        @Override
        public synchronized void fillColumn(final long col, final Access1D<N> values) {
            myDelegate.fillColumn(col, values);
        }

        @Override
        public synchronized void fillColumn(final long row, final long col, final Access1D<N> values) {
            myDelegate.fillColumn(row, col, values);
        }

        @Override
        public synchronized void fillColumn(final long row, final long col, final N value) {
            myDelegate.fillColumn(row, col, value);
        }

        @Override
        public synchronized void fillColumn(final long row, final long col, final NullaryFunction<?> supplier) {
            myDelegate.fillColumn(row, col, supplier);
        }

        @Override
        public synchronized void fillColumn(final long col, final N value) {
            myDelegate.fillColumn(col, value);
        }

        @Override
        public synchronized void fillColumn(final long col, final NullaryFunction<?> supplier) {
            myDelegate.fillColumn(col, supplier);
        }

        @Override
        public synchronized void fillCompatible(final Access1D<N> left, final BinaryFunction<N> operator, final Access1D<N> right) {
            myDelegate.fillCompatible(left, operator, right);
        }

        @Override
        public synchronized void fillCompatible(final Access2D<N> left, final BinaryFunction<N> operator, final Access2D<N> right) {
            myDelegate.fillCompatible(left, operator, right);
        }

        @Override
        public synchronized void fillDiagonal(final Access1D<N> values) {
            myDelegate.fillDiagonal(values);
        }

        @Override
        public synchronized void fillDiagonal(final long row, final long col, final Access1D<N> values) {
            myDelegate.fillDiagonal(row, col, values);
        }

        @Override
        public synchronized void fillDiagonal(final long row, final long col, final N value) {
            myDelegate.fillDiagonal(row, col, value);
        }

        @Override
        public synchronized void fillDiagonal(final long row, final long col, final NullaryFunction<?> supplier) {
            myDelegate.fillDiagonal(row, col, supplier);
        }

        @Override
        public synchronized void fillDiagonal(final N value) {
            myDelegate.fillDiagonal(value);
        }

        @Override
        public synchronized void fillDiagonal(final NullaryFunction<?> supplier) {
            myDelegate.fillDiagonal(supplier);
        }

        @Override
        public synchronized void fillMatching(final Access1D<?> values) {
            myDelegate.fillMatching(values);
        }

        @Override
        public synchronized void fillMatching(final Access1D<N> left, final BinaryFunction<N> function, final Access1D<N> right) {
            myDelegate.fillMatching(left, function, right);
        }

        @Override
        public synchronized void fillMatching(final UnaryFunction<N> function, final Access1D<N> arguments) {
            myDelegate.fillMatching(function, arguments);
        }

        @Override
        public synchronized void fillRange(final long first, final long limit, final N value) {
            myDelegate.fillRange(first, limit, value);
        }

        @Override
        public synchronized void fillRange(final long first, final long limit, final NullaryFunction<?> supplier) {
            myDelegate.fillRange(first, limit, supplier);
        }

        @Override
        public synchronized void fillRow(final long row, final Access1D<N> values) {
            myDelegate.fillRow(row, values);
        }

        @Override
        public synchronized void fillRow(final long row, final long col, final Access1D<N> values) {
            myDelegate.fillRow(row, col, values);
        }

        @Override
        public synchronized void fillRow(final long row, final long col, final N value) {
            myDelegate.fillRow(row, col, value);
        }

        @Override
        public synchronized void fillRow(final long row, final long col, final NullaryFunction<?> supplier) {
            myDelegate.fillRow(row, col, supplier);
        }

        @Override
        public synchronized void fillRow(final long row, final N value) {
            myDelegate.fillRow(row, value);
        }

        @Override
        public synchronized void fillRow(final long row, final NullaryFunction<?> supplier) {
            myDelegate.fillRow(row, supplier);
        }

        @Override
        public synchronized int firstInColumn(final int col) {
            return myDelegate.firstInColumn(col);
        }

        @Override
        public synchronized int firstInRow(final int row) {
            return myDelegate.firstInRow(row);
        }

        @Override
        public synchronized float floatValue(final int index) {
            return myDelegate.floatValue(index);
        }

        @Override
        public synchronized float floatValue(final int row, final int col) {
            return myDelegate.floatValue(row, col);
        }

        @Override
        public synchronized float floatValue(final long index) {
            return myDelegate.floatValue(index);
        }

        @Override
        public synchronized float floatValue(final long row, final long col) {
            return myDelegate.floatValue(row, col);
        }

        @Override
        public synchronized N get(final long index) {
            return myDelegate.get(index);
        }

        @Override
        public synchronized N get(final long row, final long col) {
            return myDelegate.get(row, col);
        }

        @Override
        public synchronized int getColDim() {
            return myDelegate.getColDim();
        }

        @Override
        public synchronized int getMaxDim() {
            return myDelegate.getMaxDim();
        }

        @Override
        public synchronized int getMinDim() {
            return myDelegate.getMinDim();
        }

        @Override
        public synchronized int getRowDim() {
            return myDelegate.getRowDim();
        }

        @Override
        public synchronized int intValue(final int index) {
            return myDelegate.intValue(index);
        }

        @Override
        public synchronized int intValue(final int row, final int col) {
            return myDelegate.intValue(row, col);
        }

        @Override
        public synchronized int intValue(final long index) {
            return myDelegate.intValue(index);
        }

        @Override
        public synchronized int intValue(final long row, final long col) {
            return myDelegate.intValue(row, col);
        }

        @Override
        public synchronized boolean isAcceptable(final Structure2D supplier) {
            return myDelegate.isAcceptable(supplier);
        }

        @Override
        public synchronized boolean isEmpty() {
            return myDelegate.isEmpty();
        }

        @Override
        public synchronized boolean isFat() {
            return myDelegate.isFat();
        }

        @Override
        public synchronized boolean isScalar() {
            return myDelegate.isScalar();
        }

        @Override
        public synchronized boolean isSquare() {
            return myDelegate.isSquare();
        }

        @Override
        public synchronized boolean isTall() {
            return myDelegate.isTall();
        }

        @Override
        public synchronized boolean isVector() {
            return myDelegate.isVector();
        }

        @Override
        public synchronized int limitOfColumn(final int col) {
            return myDelegate.limitOfColumn(col);
        }

        @Override
        public synchronized int limitOfRow(final int row) {
            return myDelegate.limitOfRow(row);
        }

        @Override
        public synchronized long longValue(final int index) {
            return myDelegate.longValue(index);
        }

        @Override
        public synchronized long longValue(final int row, final int col) {
            return myDelegate.longValue(row, col);
        }

        @Override
        public synchronized long longValue(final long index) {
            return myDelegate.longValue(index);
        }

        @Override
        public synchronized long longValue(final long row, final long col) {
            return myDelegate.longValue(row, col);
        }

        @Override
        public synchronized void modifyAll(final UnaryFunction<N> modifier) {
            myDelegate.modifyAll(modifier);
        }

        @Override
        public synchronized void modifyAny(final Transformation2D<N> modifier) {
            myDelegate.modifyAny(modifier);
        }

        @Override
        public synchronized void modifyColumn(final long row, final long col, final UnaryFunction<N> modifier) {
            myDelegate.modifyColumn(row, col, modifier);
        }

        @Override
        public synchronized void modifyColumn(final long col, final UnaryFunction<N> modifier) {
            myDelegate.modifyColumn(col, modifier);
        }

        @Override
        public synchronized void modifyCompatible(final Access2D<N> left, final BinaryFunction<N> operator) {
            myDelegate.modifyCompatible(left, operator);
        }

        @Override
        public synchronized void modifyCompatible(final BinaryFunction<N> operator, final Access2D<N> right) {
            myDelegate.modifyCompatible(operator, right);
        }

        @Override
        public synchronized void modifyDiagonal(final long row, final long col, final UnaryFunction<N> modifier) {
            myDelegate.modifyDiagonal(row, col, modifier);
        }

        @Override
        public synchronized void modifyDiagonal(final UnaryFunction<N> modifier) {
            myDelegate.modifyDiagonal(modifier);
        }

        @Override
        public synchronized void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
            myDelegate.modifyMatching(left, function);
        }

        @Override
        public synchronized void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
            myDelegate.modifyMatching(function, right);
        }

        @Override
        public synchronized void modifyMatchingInColumns(final Access1D<N> left, final BinaryFunction<N> function) {
            myDelegate.modifyMatchingInColumns(left, function);
        }

        @Override
        public synchronized void modifyMatchingInColumns(final BinaryFunction<N> function, final Access1D<N> right) {
            myDelegate.modifyMatchingInColumns(function, right);
        }

        @Override
        public synchronized void modifyMatchingInRows(final Access1D<N> left, final BinaryFunction<N> function) {
            myDelegate.modifyMatchingInRows(left, function);
        }

        @Override
        public synchronized void modifyMatchingInRows(final BinaryFunction<N> function, final Access1D<N> right) {
            myDelegate.modifyMatchingInRows(function, right);
        }

        @Override
        public synchronized void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {
            myDelegate.modifyOne(row, col, modifier);
        }

        @Override
        public synchronized void modifyOne(final long index, final UnaryFunction<N> modifier) {
            myDelegate.modifyOne(index, modifier);
        }

        @Override
        public synchronized void modifyRange(final long first, final long limit, final UnaryFunction<N> modifier) {
            myDelegate.modifyRange(first, limit, modifier);
        }

        @Override
        public synchronized void modifyRow(final long row, final long col, final UnaryFunction<N> modifier) {
            myDelegate.modifyRow(row, col, modifier);
        }

        @Override
        public synchronized void modifyRow(final long row, final UnaryFunction<N> modifier) {
            myDelegate.modifyRow(row, modifier);
        }

        @Override
        public synchronized ElementView2D<N, ?> nonzeros() {
            return myDelegate.nonzeros();
        }

        @Override
        public synchronized TransformableRegion<N> regionByTransposing() {
            return myDelegate.regionByTransposing();
        }

        @Override
        public synchronized void reset() {
            myDelegate.reset();
        }

        @Override
        public synchronized RowView<N> rows() {
            return myDelegate.rows();
        }

        @Override
        public synchronized Access2D<N> rows(final int... rows) {
            return myDelegate.rows(rows);
        }

        @Override
        public synchronized Access2D<N> rows(final long... rows) {
            return myDelegate.rows(rows);
        }

        @Override
        public synchronized Access2D<N> select(final int[] rows, final int[] columns) {
            return myDelegate.select(rows, columns);
        }

        @Override
        public synchronized Access1D<N> select(final long... selection) {
            return myDelegate.select(selection);
        }

        @Override
        public synchronized Access2D<N> select(final long[] rows, final long[] columns) {
            return myDelegate.select(rows, columns);
        }

        @Override
        public synchronized void set(final int index, final byte value) {
            myDelegate.set(index, value);
        }

        @Override
        public synchronized void set(final int index, final double value) {
            myDelegate.set(index, value);
        }

        @Override
        public synchronized void set(final int index, final float value) {
            myDelegate.set(index, value);
        }

        @Override
        public synchronized void set(final int index, final int value) {
            myDelegate.set(index, value);
        }

        @Override
        public synchronized void set(final int row, final int col, final byte value) {
            myDelegate.set(row, col, value);
        }

        @Override
        public synchronized void set(final int row, final int col, final double value) {
            myDelegate.set(row, col, value);
        }

        @Override
        public synchronized void set(final int row, final int col, final float value) {
            myDelegate.set(row, col, value);
        }

        @Override
        public synchronized void set(final int row, final int col, final int value) {
            myDelegate.set(row, col, value);
        }

        @Override
        public synchronized void set(final int row, final int col, final long value) {
            myDelegate.set(row, col, value);
        }

        @Override
        public synchronized void set(final int row, final int col, final short value) {
            myDelegate.set(row, col, value);
        }

        @Override
        public synchronized void set(final int index, final long value) {
            myDelegate.set(index, value);
        }

        @Override
        public synchronized void set(final int index, final short value) {
            myDelegate.set(index, value);
        }

        @Override
        public synchronized void set(final long index, final byte value) {
            myDelegate.set(index, value);
        }

        @Override
        public synchronized void set(final long index, final Comparable<?> value) {
            myDelegate.set(index, value);
        }

        @Override
        public synchronized void set(final long index, final double value) {
            myDelegate.set(index, value);
        }

        @Override
        public synchronized void set(final long index, final float value) {
            myDelegate.set(index, value);
        }

        @Override
        public synchronized void set(final long index, final int value) {
            myDelegate.set(index, value);
        }

        @Override
        public synchronized void set(final long index, final long value) {
            myDelegate.set(index, value);
        }

        @Override
        public synchronized void set(final long row, final long col, final byte value) {
            myDelegate.set(row, col, value);
        }

        @Override
        public synchronized void set(final long row, final long col, final Comparable<?> value) {
            myDelegate.set(row, col, value);
        }

        @Override
        public synchronized void set(final long row, final long col, final double value) {
            myDelegate.set(row, col, value);
        }

        @Override
        public synchronized void set(final long row, final long col, final float value) {
            myDelegate.set(row, col, value);
        }

        @Override
        public synchronized void set(final long row, final long col, final int value) {
            myDelegate.set(row, col, value);
        }

        @Override
        public synchronized void set(final long row, final long col, final long value) {
            myDelegate.set(row, col, value);
        }

        @Override
        public synchronized void set(final long row, final long col, final short value) {
            myDelegate.set(row, col, value);
        }

        @Override
        public synchronized void set(final long index, final short value) {
            myDelegate.set(index, value);
        }

        @Override
        public synchronized short shortValue(final int index) {
            return myDelegate.shortValue(index);
        }

        @Override
        public synchronized short shortValue(final int row, final int col) {
            return myDelegate.shortValue(row, col);
        }

        @Override
        public synchronized short shortValue(final long index) {
            return myDelegate.shortValue(index);
        }

        @Override
        public synchronized short shortValue(final long row, final long col) {
            return myDelegate.shortValue(row, col);
        }

        @Override
        public synchronized int size() {
            return myDelegate.size();
        }

        @Override
        public synchronized void supplyTo(final double[] receiver) {
            myDelegate.supplyTo(receiver);
        }

        @Override
        public synchronized double[] toRawCopy1D() {
            return myDelegate.toRawCopy1D();
        }

        @Override
        public synchronized double[][] toRawCopy2D() {
            return myDelegate.toRawCopy2D();
        }

    }

    static final class TransposedRegion<N extends Comparable<N>> extends Subregion2D<N> {

        private final TransformableRegion<N> myBase;

        TransposedRegion(final TransformableRegion<N> base, final TransformableRegion.FillByMultiplying<N> multiplier) {
            super(multiplier, base.countColumns(), base.countRows());
            myBase = base;
        }

        @Override
        public void add(final long row, final long col, final Comparable<?> addend) {
            myBase.add(col, row, addend);
        }

        @Override
        public void add(final long row, final long col, final double addend) {
            myBase.add(col, row, addend);
        }

        @Override
        public long countColumns() {
            return myBase.countRows();
        }

        @Override
        public long countRows() {
            return myBase.countColumns();
        }

        @Override
        public double doubleValue(final int row, final int col) {
            return myBase.doubleValue(col, row);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TransposedRegion)) {
                return false;
            }
            TransposedRegion other = (TransposedRegion) obj;
            if (myBase == null) {
                if (other.myBase != null) {
                    return false;
                }
            } else if (!myBase.equals(other.myBase)) {
                return false;
            }
            return true;
        }

        @Override
        public void fillColumn(final long row, final long col, final N value) {
            myBase.fillRow(col, row, value);
        }

        @Override
        public void fillColumn(final long row, final long col, final NullaryFunction<?> supplier) {
            myBase.fillRow(col, row, supplier);
        }

        @Override
        public void fillDiagonal(final long row, final long col, final N value) {
            myBase.fillDiagonal(col, row, value);
        }

        @Override
        public void fillDiagonal(final long row, final long col, final NullaryFunction<?> supplier) {
            myBase.fillRow(col, row, supplier);
        }

        @Override
        public void fillRow(final long row, final long col, final N value) {
            myBase.fillDiagonal(col, row, value);
        }

        @Override
        public void fillRow(final long row, final long col, final NullaryFunction<?> supplier) {
            myBase.fillDiagonal(col, row, supplier);
        }

        @Override
        public N get(final long row, final long col) {
            return myBase.get(col, row);
        }

        @Override
        public int getColDim() {
            return myBase.getRowDim();
        }

        @Override
        public int getRowDim() {
            return myBase.getColDim();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            return prime * result + (myBase == null ? 0 : myBase.hashCode());
        }

        @Override
        public void modifyColumn(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyRow(col, row, modifier);
        }

        @Override
        public void modifyDiagonal(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyDiagonal(col, row, modifier);
        }

        @Override
        public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyOne(col, row, modifier);
        }

        @Override
        public void modifyRow(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyColumn(col, row, modifier);
        }

        @Override
        public TransformableRegion<N> regionByTransposing() {
            return myBase;
        }

        @Override
        public void reset() {
            myBase.reset();
        }

        @Override
        public void set(final int row, final int col, final double value) {
            myBase.set(col, row, value);
        }

        @Override
        public void set(final long row, final long col, final Comparable<?> value) {
            myBase.set(col, row, value);
        }

    }

    static final class WrapperRegion<N extends Comparable<N>> extends Subregion2D<N> {

        private final Mutate2D.ModifiableReceiver<N> myBase;

        WrapperRegion(final Mutate2D.ModifiableReceiver<N> base) {
            super(Subregion2D.findMultiplier(base.get(0, 0).getClass(), base.getRowDim(), base.getColDim()), base.getRowDim(), base.getColDim());
            myBase = base;
        }

        @Override
        public void add(final long row, final long col, final Comparable<?> addend) {
            myBase.add(row, col, addend);
        }

        @Override
        public void add(final long row, final long col, final double addend) {
            myBase.add(row, col, addend);
        }

        @Override
        public long countColumns() {
            return myBase.countColumns();
        }

        @Override
        public long countRows() {
            return myBase.countRows();
        }

        @Override
        public double doubleValue(final int row, final int col) {
            return myBase.doubleValue(row, col);
        }

        @Override
        public N get(final long row, final long col) {
            return myBase.get(row, col);
        }

        @Override
        public int getColDim() {
            return myBase.getColDim();
        }

        @Override
        public int getRowDim() {
            return myBase.getRowDim();
        }

        @Override
        public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyOne(row, col, modifier);
        }

        @Override
        public void set(final int row, final int col, final double value) {
            myBase.set(row, col, value);
        }

        @Override
        public void set(final long row, final long col, final Comparable<?> value) {
            myBase.set(row, col, value);
        }

    }

    static <N extends Comparable<N>> TransformableRegion.FillByMultiplying<N> findMultiplier(final Class<?> tmpType, final int rowsCount,
            final int columnsCount) {
        if (tmpType.equals(Double.class)) {
            return (TransformableRegion.FillByMultiplying<N>) MultiplyBoth.newPrimitive64(rowsCount, columnsCount);
        } else {
            return (TransformableRegion.FillByMultiplying<N>) MultiplyBoth.newGeneric(rowsCount, columnsCount);
        }
    }

    private final TransformableRegion.FillByMultiplying<N> myMultiplier;

    @SuppressWarnings("unused")
    private Subregion2D() {
        this(null, 0L, 0L);
    }

    @SuppressWarnings("unchecked")
    Subregion2D(final TransformableRegion.FillByMultiplying<N> multiplier, final long rows, final long columns) {

        super();

        if (multiplier instanceof MultiplyBoth.Primitive) {
            myMultiplier = (TransformableRegion.FillByMultiplying<N>) MultiplyBoth.newPrimitive64(Math.toIntExact(rows), Math.toIntExact(columns));
        } else if (multiplier instanceof MultiplyBoth.Generic) {
            myMultiplier = (TransformableRegion.FillByMultiplying<N>) MultiplyBoth.newGeneric(Math.toIntExact(rows), Math.toIntExact(columns));
        } else {
            myMultiplier = multiplier;
        }
    }

    @Override
    public final void fillByMultiplying(final Access1D<N> left, final Access1D<N> right) {

        int complexity = Math.toIntExact(left.count() / this.countRows());
        if (complexity != Math.toIntExact(right.count() / this.countColumns())) {
            ProgrammingError.throwForMultiplicationNotPossible();
        }

        myMultiplier.invoke(this, left, (int) (left.count() / this.countRows()), right);
    }

    @Override
    public void fillMatching(final Access1D<?> values) {
        for (long i = 0L, limit = Math.min(this.count(), values.count()); i < limit; i++) {
            this.set(i, values.get(i));
        }
    }

    @Override
    public final TransformableRegion<N> regionByColumns(final int... columns) {
        return new Subregion2D.ColumnsRegion<>(this, myMultiplier, columns);
    }

    @Override
    public final TransformableRegion<N> regionByLimits(final int rowLimit, final int columnLimit) {
        return new Subregion2D.LimitRegion<>(this, myMultiplier, rowLimit, columnLimit);
    }

    @Override
    public final TransformableRegion<N> regionByOffsets(final int rowOffset, final int columnOffset) {
        return new Subregion2D.OffsetRegion<>(this, myMultiplier, rowOffset, columnOffset);
    }

    @Override
    public final TransformableRegion<N> regionByRows(final int... rows) {
        return new Subregion2D.RowsRegion<>(this, myMultiplier, rows);
    }

    @Override
    public TransformableRegion<N> regionByTransposing() {
        return new Subregion2D.TransposedRegion<>(this, myMultiplier);
    }

    @Override
    public String toString() {
        return Access1D.toString(this);
    }

}
