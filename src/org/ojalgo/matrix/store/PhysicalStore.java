/*
 * Copyright 1997-2017 Optimatika
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

import java.io.Serializable;
import java.util.List;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.Factory2D;
import org.ojalgo.access.Mutate2D;
import org.ojalgo.array.BasicArray;
import org.ojalgo.array.DenseArray;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.FunctionUtils;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.matrix.store.BigDenseStore.BigMultiplyBoth;
import org.ojalgo.matrix.store.ComplexDenseStore.ComplexMultiplyBoth;
import org.ojalgo.matrix.store.PrimitiveDenseStore.PrimitiveMultiplyBoth;
import org.ojalgo.matrix.store.operation.MultiplyBoth;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.Scalar;

/**
 * <p>
 * PhysicalStore:s, as opposed to MatrixStore:s, are mutable. The vast majorty of the methods defined here
 * return void and none return {@linkplain PhysicalStore} or {@linkplain MatrixStore}.
 * </p>
 * <p>
 * This interface and its implementations are central to ojAlgo.
 * </p>
 *
 * @author apete
 */
public interface PhysicalStore<N extends Number> extends MatrixStore<N>, Access2D.IndexOf, ElementsConsumer<N>, Mutate2D.Exchangeable {

    public static final class ColumnsRegion<N extends Number> extends ConsumerRegion<N> {

        private final ElementsConsumer<N> myBase;
        private final int[] myColumns;

        ColumnsRegion(final ElementsConsumer<N> base, final FillByMultiplying<N> multiplier, final int... columns) {
            super(multiplier, base.countRows(), columns.length);
            myBase = base;
            myColumns = columns;
        }

        public void add(final long row, final long col, final double addend) {
            myBase.add(row, myColumns[(int) col], addend);
        }

        public void add(final long row, final long col, final Number addend) {
            myBase.add(row, myColumns[(int) col], addend);
        }

        public long countColumns() {
            return myColumns.length;
        }

        public long countRows() {
            return myBase.countRows();
        }

        public void fillColumn(final long row, final long col, final Access1D<N> values) {
            myBase.fillColumn(row, myColumns[(int) col], values);
        }

        public void fillColumn(final long row, final long col, final N value) {
            myBase.fillColumn(row, myColumns[(int) col], value);
        }

        public void fillColumn(final long row, final long col, final NullaryFunction<N> supplier) {
            myBase.fillColumn(row, myColumns[(int) col], supplier);
        }

        public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
            myBase.fillOne(row, myColumns[(int) col], values, valueIndex);
        }

        public void fillOne(final long row, final long col, final N value) {
            myBase.fillOne(row, myColumns[(int) col], value);
        }

        public void fillOne(final long row, final long col, final NullaryFunction<N> supplier) {
            myBase.fillOne(row, myColumns[(int) col], supplier);
        }

        public void modifyColumn(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyColumn(row, myColumns[(int) col], modifier);
        }

        public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyOne(row, myColumns[(int) col], modifier);
        }

        public void set(final long row, final long col, final double value) {
            myBase.set(row, myColumns[(int) col], value);
        }

        public void set(final long row, final long col, final Number value) {
            myBase.set(row, myColumns[(int) col], value);
        }

    }

    abstract static class ConsumerRegion<N extends Number> implements ElementsConsumer<N> {

        private final FillByMultiplying<N> myMultiplier;

        @SuppressWarnings("unused")
        private ConsumerRegion() {
            this(null, 0L, 0L);
        }

        @SuppressWarnings("unchecked")
        ConsumerRegion(final FillByMultiplying<N> multiplier, final long rows, final long columns) {

            super();

            if (multiplier instanceof PrimitiveMultiplyBoth) {
                myMultiplier = (FillByMultiplying<N>) MultiplyBoth.getPrimitive(rows, columns);
            } else if (multiplier instanceof ComplexMultiplyBoth) {
                myMultiplier = (FillByMultiplying<N>) MultiplyBoth.getComplex(rows, columns);
            } else if (multiplier instanceof BigMultiplyBoth) {
                myMultiplier = (FillByMultiplying<N>) MultiplyBoth.getBig(rows, columns);
            } else {
                myMultiplier = multiplier;
            }
        }

        public final void fillByMultiplying(final Access1D<N> left, final Access1D<N> right) {
            myMultiplier.invoke(this, left, (int) (left.count() / this.countRows()), right);
        }

        public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
            // TODO very inefficient implemention - must invent something better
            final long tmpLimit = FunctionUtils.min(left.count(), this.count());
            for (long i = 0L; i < tmpLimit; i++) {
                this.modifyOne(i, function.first(left.get(i)));
            }
        }

        public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
            // TODO very inefficient implemention - must invent something better
            final long tmpLimit = FunctionUtils.min(this.count(), right.count());
            for (long i = 0L; i < tmpLimit; i++) {
                this.modifyOne(i, function.second(right.get(i)));
            }
        }

        public final ElementsConsumer<N> regionByColumns(final int... columns) {
            return new ColumnsRegion<>(this, myMultiplier, columns);
        }

        public final ElementsConsumer<N> regionByLimits(final int rowLimit, final int columnLimit) {
            return new LimitRegion<>(this, myMultiplier, rowLimit, columnLimit);
        }

        public final ElementsConsumer<N> regionByOffsets(final int rowOffset, final int columnOffset) {
            return new OffsetRegion<>(this, myMultiplier, rowOffset, columnOffset);
        }

        public final ElementsConsumer<N> regionByRows(final int... rows) {
            return new RowsRegion<>(this, myMultiplier, rows);
        }

        public ElementsConsumer<N> regionByTransposing() {
            return new TransposedRegion<>(this, myMultiplier);
        }

        @Override
        public String toString() {
            return super.toString() + " " + this.countRows() + " x " + this.countColumns();
        }

    }

    public static interface Factory<N extends Number, I extends PhysicalStore<N>> extends Factory2D<I>, Serializable {

        AggregatorSet<N> aggregator();

        DenseArray.Factory<N> array();

        MatrixStore.Factory<N> builder();

        I conjugate(Access2D<?> source);

        FunctionSet<N> function();

        /**
         * @deprecated v44 Use {@link #array()} instead.
         */
        @Deprecated
        default BasicArray<N> makeArray(final int length) {
            return this.array().makeZero(length);
        }

        default ColumnsSupplier<N> makeColumnsSupplier(final int numberOfRows) {
            return new ColumnsSupplier<>(this, numberOfRows);
        }

        Householder<N> makeHouseholder(int length);

        Rotation<N> makeRotation(int low, int high, double cos, double sin);

        Rotation<N> makeRotation(int low, int high, N cos, N sin);

        default RowsSupplier<N> makeRowsSupplier(final int numberOfColumns) {
            return new RowsSupplier<>(this, numberOfColumns);
        }

        Scalar.Factory<N> scalar();

        I transpose(Access2D<?> source);

    }

    public static interface FillByMultiplying<N extends Number> {

        void invoke(ElementsConsumer<N> product, Access1D<N> left, int complexity, Access1D<N> right);

    }

    public static final class LimitRegion<N extends Number> extends ConsumerRegion<N> {

        private final ElementsConsumer<N> myBase;
        private final int myRowLimit, myColumnLimit; // limits

        LimitRegion(final ElementsConsumer<N> base, final FillByMultiplying<N> multiplier, final int rowLimit, final int columnLimit) {
            super(multiplier, rowLimit, columnLimit);
            myBase = base;
            myRowLimit = rowLimit;
            myColumnLimit = columnLimit;
        }

        public void add(final long row, final long col, final double addend) {
            myBase.add(row, col, addend);
        }

        public void add(final long row, final long col, final Number addend) {
            myBase.add(row, col, addend);
        }

        public long countColumns() {
            return myColumnLimit;
        }

        public long countRows() {
            return myRowLimit;
        }

        public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
            myBase.fillOne(row, col, values, valueIndex);
        }

        public void fillOne(final long row, final long col, final N value) {
            myBase.fillOne(row, col, value);
        }

        public void fillOne(final long row, final long col, final NullaryFunction<N> supplier) {
            myBase.fillOne(row, col, supplier);
        }

        public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyOne(row, col, modifier);
        }

        public void set(final long row, final long col, final double value) {
            myBase.set(row, col, value);
        }

        public void set(final long row, final long col, final Number value) {
            myBase.set(row, col, value);
        }

    }

    public static final class OffsetRegion<N extends Number> extends ConsumerRegion<N> {

        private final ElementsConsumer<N> myBase;
        private final int myRowOffset, myColumnOffset; // origin/offset

        OffsetRegion(final ElementsConsumer<N> base, final FillByMultiplying<N> multiplier, final int rowOffset, final int columnOffset) {
            super(multiplier, base.countRows() - rowOffset, base.countColumns() - columnOffset);
            myBase = base;
            myRowOffset = rowOffset;
            myColumnOffset = columnOffset;
        }

        public void add(final long row, final long col, final double addend) {
            myBase.add(myRowOffset + row, myColumnOffset + col, addend);
        }

        public void add(final long row, final long col, final Number addend) {
            myBase.add(myRowOffset + row, myColumnOffset + col, addend);
        }

        public long countColumns() {
            return myBase.countColumns() - myColumnOffset;
        }

        public long countRows() {
            return myBase.countRows() - myRowOffset;
        }

        @Override
        public void fillAll(final N value) {
            final long tmpCountColumns = myBase.countColumns();
            for (long j = myColumnOffset; j < tmpCountColumns; j++) {
                myBase.fillColumn(myRowOffset, j, value);
            }
        }

        @Override
        public void fillAll(final NullaryFunction<N> supplier) {
            final long tmpCountColumns = myBase.countColumns();
            for (long j = myColumnOffset; j < tmpCountColumns; j++) {
                myBase.fillColumn(myRowOffset, j, supplier);
            }
        }

        public void fillColumn(final long row, final long col, final N value) {
            myBase.fillColumn(myRowOffset + row, myColumnOffset + col, value);
        }

        public void fillColumn(final long row, final long col, final NullaryFunction<N> supplier) {
            myBase.fillColumn(myRowOffset + row, myColumnOffset + col, supplier);
        }

        public void fillDiagonal(final long row, final long col, final N value) {
            myBase.fillDiagonal(myRowOffset + row, myColumnOffset + col, value);
        }

        public void fillDiagonal(final long row, final long col, final NullaryFunction<N> supplier) {
            myBase.fillDiagonal(myRowOffset + row, myColumnOffset + col, supplier);
        }

        public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
            myBase.fillOne(myRowOffset + row, myColumnOffset + col, values, valueIndex);
        }

        public void fillOne(final long row, final long col, final N value) {
            myBase.fillOne(myRowOffset + row, myColumnOffset + col, value);
        }

        public void fillOne(final long row, final long col, final NullaryFunction<N> supplier) {
            myBase.fillOne(myRowOffset + row, myColumnOffset + col, supplier);
        }

        public void fillRow(final long row, final long col, final N value) {
            myBase.fillRow(myRowOffset + row, myColumnOffset + col, value);
        }

        public void fillRow(final long row, final long col, final NullaryFunction<N> supplier) {
            myBase.fillRow(myRowOffset + row, myColumnOffset + col, supplier);
        }

        public void modifyAll(final UnaryFunction<N> modifier) {
            for (long j = myColumnOffset; j < myBase.countColumns(); j++) {
                myBase.modifyColumn(myRowOffset, j, modifier);
            }
        }

        public void modifyColumn(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyColumn(myRowOffset + row, myColumnOffset + col, modifier);
        }

        public void modifyDiagonal(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyDiagonal(myRowOffset + row, myColumnOffset + col, modifier);
        }

        public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyOne(myRowOffset + row, myColumnOffset + col, modifier);
        }

        public void modifyRow(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyRow(myRowOffset + row, myColumnOffset + col, modifier);
        }

        public void set(final long row, final long col, final double value) {
            myBase.set(myRowOffset + row, myColumnOffset + col, value);
        }

        public void set(final long row, final long col, final Number value) {
            myBase.set(myRowOffset + row, myColumnOffset + col, value);
        }

    }

    public static final class RowsRegion<N extends Number> extends ConsumerRegion<N> {

        private final ElementsConsumer<N> myBase;
        private final int[] myRows;

        RowsRegion(final ElementsConsumer<N> base, final FillByMultiplying<N> multiplier, final int... rows) {
            super(multiplier, rows.length, base.countColumns());
            myBase = base;
            myRows = rows;
        }

        public void add(final long row, final long col, final double addend) {
            myBase.add(myRows[(int) row], col, addend);
        }

        public void add(final long row, final long col, final Number addend) {
            myBase.add(myRows[(int) row], col, addend);
        }

        public long countColumns() {
            return myBase.countColumns();
        }

        public long countRows() {
            return myRows.length;
        }

        public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
            myBase.fillOne(myRows[(int) row], col, values, valueIndex);
        }

        public void fillOne(final long row, final long col, final N value) {
            myBase.fillOne(myRows[(int) row], col, value);
        }

        public void fillOne(final long row, final long col, final NullaryFunction<N> supplier) {
            myBase.fillOne(myRows[(int) row], col, supplier);
        }

        public void fillRow(final long row, final long col, final Access1D<N> values) {
            myBase.fillRow(myRows[(int) row], col, values);
        }

        public void fillRow(final long row, final long col, final N value) {
            myBase.fillRow(myRows[(int) row], col, value);
        }

        public void fillRow(final long row, final long col, final NullaryFunction<N> supplier) {
            myBase.fillRow(myRows[(int) row], col, supplier);
        }

        public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyOne(myRows[(int) row], col, modifier);
        }

        public void modifyRow(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyRow(myRows[(int) row], col, modifier);
        }

        public void set(final long row, final long col, final double value) {
            myBase.set(myRows[(int) row], col, value);
        }

        public void set(final long row, final long col, final Number value) {
            myBase.set(myRows[(int) row], col, value);
        }

    }

    public static final class TransposedRegion<N extends Number> extends ConsumerRegion<N> {

        private final ElementsConsumer<N> myBase;

        TransposedRegion(final ElementsConsumer<N> base, final FillByMultiplying<N> multiplier) {
            super(multiplier, base.countColumns(), base.countRows());
            myBase = base;
        }

        public void add(final long row, final long col, final double addend) {
            myBase.add(col, row, addend);
        }

        public void add(final long row, final long col, final Number addend) {
            myBase.add(col, row, addend);
        }

        public long countColumns() {
            return myBase.countRows();
        }

        public long countRows() {
            return myBase.countColumns();
        }

        public void fillColumn(final long row, final long col, final N value) {
            myBase.fillRow(col, row, value);
        }

        public void fillColumn(final long row, final long col, final NullaryFunction<N> supplier) {
            myBase.fillRow(col, row, supplier);
        }

        public void fillDiagonal(final long row, final long col, final N value) {
            myBase.fillDiagonal(col, row, value);
        }

        public void fillDiagonal(final long row, final long col, final NullaryFunction<N> supplier) {
            myBase.fillRow(col, row, supplier);
        }

        public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
            myBase.fillOne(col, row, values, valueIndex);
        }

        public void fillOne(final long row, final long col, final N value) {
            myBase.fillOne(col, row, value);
        }

        public void fillOne(final long row, final long col, final NullaryFunction<N> supplier) {
            myBase.fillOne(col, row, supplier);
        }

        public void fillRow(final long row, final long col, final N value) {
            myBase.fillDiagonal(col, row, value);
        }

        public void fillRow(final long row, final long col, final NullaryFunction<N> supplier) {
            myBase.fillDiagonal(col, row, supplier);
        }

        public void modifyColumn(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyRow(col, row, modifier);
        }

        public void modifyDiagonal(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyDiagonal(col, row, modifier);
        }

        @Override
        public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
            myBase.modifyMatching(left, function);
        }

        @Override
        public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
            myBase.modifyMatching(function, right);
        }

        public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyOne(col, row, modifier);
        }

        public void modifyRow(final long row, final long col, final UnaryFunction<N> modifier) {
            myBase.modifyColumn(col, row, modifier);
        }

        @Override
        public ElementsConsumer<N> regionByTransposing() {
            return myBase;
        }

        public void set(final long row, final long col, final double value) {
            myBase.set(col, row, value);
        }

        public void set(final long row, final long col, final Number value) {
            myBase.set(col, row, value);
        }

    }

    /**
     * @return The elements of the physical store as a fixed size (1 dimensional) list. The elements may be
     *         accessed either row or colomn major.
     */
    List<N> asList();

    /**
     * Will solve the equation system [A][X]=[B] where:
     * <ul>
     * <li>[body][this]=[this] is [A][X]=[B] ("this" is the right hand side, and it will be overwritten with
     * the solution).</li>
     * <li>[A] is upper/right triangular</li>
     * </ul>
     *
     * @param body The equation system body parameters [A]
     * @param unitDiagonal TODO
     * @param conjugated true if the upper/right part of body is actually stored in the lower/left part of the
     *        matrix, and the elements conjugated.
     * @param hermitian TODO
     */
    void substituteBackwards(Access2D<N> body, boolean unitDiagonal, boolean conjugated, boolean hermitian);

    /**
     * Will solve the equation system [A][X]=[B] where:
     * <ul>
     * <li>[body][this]=[this] is [A][X]=[B] ("this" is the right hand side, and it will be overwritten with
     * the solution).</li>
     * <li>[A] is lower/left triangular</li>
     * </ul>
     *
     * @param body The equation system body parameters [A]
     * @param unitDiagonal true if body has ones on the diagonal
     * @param conjugated TODO
     * @param identity
     */
    void substituteForwards(Access2D<N> body, boolean unitDiagonal, boolean conjugated, boolean identity);

    void transformLeft(Householder<N> transformation, int firstColumn);

    /**
     * <p>
     * As in {@link MatrixStore#premultiply(Access1D)} where the left/parameter matrix is a plane rotation.
     * </p>
     * <p>
     * Multiplying by a plane rotation from the left means that [this] gets two of its rows updated to new
     * combinations of those two (current) rows.
     * </p>
     * <p>
     * There are two ways to transpose/invert a rotation. Either you negate the angle or you interchange the
     * two indeces that define the rotation plane.
     * </p>
     *
     * @see #transformRight(Rotation)
     */
    void transformLeft(Rotation<N> transformation);

    void transformRight(Householder<N> transformation, int firstRow);

    /**
     * <p>
     * As in {@link MatrixStore#multiply(MatrixStore)} where the right/parameter matrix is a plane rotation.
     * </p>
     * <p>
     * Multiplying by a plane rotation from the right means that [this] gets two of its columns updated to new
     * combinations of those two (current) columns.
     * </p>
     * <p>
     * There result is undefined if the two input indeces are the same (in which case the rotation plane is
     * undefined).
     * </p>
     *
     * @see #transformLeft(Rotation)
     */
    void transformRight(Rotation<N> transformation);

}
