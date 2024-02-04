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

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.operation.MultiplyBoth;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Mutate2D;

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
        public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
            myBase.fillOne(row, myColumns[Math.toIntExact(col)], values, valueIndex);
        }

        @Override
        public void fillOne(final long row, final long col, final N value) {
            myBase.fillOne(row, myColumns[Math.toIntExact(col)], value);
        }

        @Override
        public void fillOne(final long row, final long col, final NullaryFunction<?> supplier) {
            myBase.fillOne(row, myColumns[Math.toIntExact(col)], supplier);
        }

        @Override
        public N get(final long row, final long col) {
            return myBase.get(row, myColumns[Math.toIntExact(col)]);
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
        public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
            myBase.fillOne(row, col, values, valueIndex);
        }

        @Override
        public void fillOne(final long row, final long col, final N value) {
            myBase.fillOne(row, col, value);
        }

        @Override
        public void fillOne(final long row, final long col, final NullaryFunction<?> supplier) {
            myBase.fillOne(row, col, supplier);
        }

        @Override
        public N get(final long row, final long col) {
            return myBase.get(row, col);
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
        public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
            myBase.fillOne(myRowOffset + row, myColumnOffset + col, values, valueIndex);
        }

        @Override
        public void fillOne(final long row, final long col, final N value) {
            myBase.fillOne(myRowOffset + row, myColumnOffset + col, value);
        }

        @Override
        public void fillOne(final long row, final long col, final NullaryFunction<?> supplier) {
            myBase.fillOne(myRowOffset + row, myColumnOffset + col, supplier);
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
        public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
            myBase.fillOne(myRows[Math.toIntExact(row)], col, values, valueIndex);
        }

        @Override
        public void fillOne(final long row, final long col, final N value) {
            myBase.fillOne(myRows[Math.toIntExact(row)], col, value);
        }

        @Override
        public void fillOne(final long row, final long col, final NullaryFunction<?> supplier) {
            myBase.fillOne(myRows[Math.toIntExact(row)], col, supplier);
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
        public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
            myBase.fillOne(col, row, values, valueIndex);
        }

        @Override
        public void fillOne(final long row, final long col, final N value) {
            myBase.fillOne(col, row, value);
        }

        @Override
        public void fillOne(final long row, final long col, final NullaryFunction<?> supplier) {
            myBase.fillOne(col, row, supplier);
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
