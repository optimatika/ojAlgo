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
package org.ojalgo.matrix.store;

import java.io.Serializable;
import java.util.List;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.array.BasicArray;
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
public interface PhysicalStore<N extends Number> extends MatrixStore<N>, ElementsConsumer<N>, Access2D.IndexOf, Access2D.Special<N> {

    public static final class ColumnsRegion<N extends Number> extends ConsumerRegion<N> {

        private final ElementsConsumer<N> myBase;
        private final int[] myColumns;

        ColumnsRegion(final ElementsConsumer<N> base, final FillByMultiplying<N> multiplier, final int... columns) {
            super(multiplier, base.countRows(), columns.length);
            myBase = base;
            myColumns = columns;
        }

        public void add(final long row, final long column, final double addend) {
            myBase.add(row, myColumns[(int) column], addend);
        }

        public void add(final long row, final long column, final Number addend) {
            myBase.add(row, myColumns[(int) column], addend);
        }

        public long countColumns() {
            return myColumns.length;
        }

        public long countRows() {
            return myBase.countRows();
        }

        public void fillColumn(final long row, final long column, final Access1D<N> values) {
            myBase.fillColumn(row, myColumns[(int) column], values);
        }

        public void fillColumn(final long row, final long column, final N value) {
            myBase.fillColumn(row, myColumns[(int) column], value);
        }

        public void fillColumn(final long row, final long column, final NullaryFunction<N> supplier) {
            myBase.fillColumn(row, myColumns[(int) column], supplier);
        }

        public void fillOne(final long row, final long column, final N value) {
            myBase.fillOne(row, myColumns[(int) column], value);
        }

        public void fillOne(final long row, final long column, final NullaryFunction<N> supplier) {
            myBase.fillOne(row, myColumns[(int) column], supplier);
        }

        public void fillOneMatching(final long row, final long column, final Access1D<?> values, final long valueIndex) {
            myBase.fillOneMatching(row, myColumns[(int) column], values, valueIndex);
        }

        public void modifyColumn(final long row, final long column, final UnaryFunction<N> function) {
            myBase.modifyColumn(row, myColumns[(int) column], function);
        }

        public void modifyOne(final long row, final long column, final UnaryFunction<N> function) {
            myBase.modifyOne(row, myColumns[(int) column], function);
        }

        public void set(final long row, final long column, final double value) {
            myBase.set(row, myColumns[(int) column], value);
        }

        public void set(final long row, final long column, final Number value) {
            myBase.set(row, myColumns[(int) column], value);
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
            for (long i = 0; i < tmpLimit; i++) {
                this.modifyOne(i, function.first(left.get(i)));
            }
        }

        public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
            // TODO very inefficient implemention - must invent something better
            final long tmpLimit = FunctionUtils.min(this.count(), right.count());
            for (long i = 0; i < tmpLimit; i++) {
                this.modifyOne(i, function.second(right.get(i)));
            }
        }

        public final ElementsConsumer<N> regionByColumns(final int... columns) {
            return new ColumnsRegion<N>(this, myMultiplier, columns);
        }

        public final ElementsConsumer<N> regionByLimits(final int rowLimit, final int columnLimit) {
            return new LimitRegion<N>(this, myMultiplier, rowLimit, columnLimit);
        }

        public final ElementsConsumer<N> regionByOffsets(final int rowOffset, final int columnOffset) {
            return new OffsetRegion<N>(this, myMultiplier, rowOffset, columnOffset);
        }

        public final ElementsConsumer<N> regionByRows(final int... rows) {
            return new RowsRegion<N>(this, myMultiplier, rows);
        }

        public ElementsConsumer<N> regionByTransposing() {
            return new TransposedRegion<N>(this, myMultiplier);
        }

        @Override
        public String toString() {
            return super.toString() + " " + this.countRows() + " x " + this.countColumns();
        }

    }

    public static interface Factory<N extends Number, I extends PhysicalStore<N>> extends Access2D.Factory<I>, Serializable {

        AggregatorSet<N> aggregator();

        MatrixStore.Factory<N> builder();

        I conjugate(Access2D<?> source);

        FunctionSet<N> function();

        BasicArray<N> makeArray(int length);

        Householder<N> makeHouseholder(int length);

        Rotation<N> makeRotation(int low, int high, double cos, double sin);

        Rotation<N> makeRotation(int low, int high, N cos, N sin);

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

        public void add(final long row, final long column, final double addend) {
            myBase.add(row, column, addend);
        }

        public void add(final long row, final long column, final Number addend) {
            myBase.add(row, column, addend);
        }

        public long countColumns() {
            return myColumnLimit;
        }

        public long countRows() {
            return myRowLimit;
        }

        public void fillOne(final long row, final long column, final N value) {
            myBase.fillOne(row, column, value);
        }

        public void fillOne(final long row, final long column, final NullaryFunction<N> supplier) {
            myBase.fillOne(row, column, supplier);
        }

        public void fillOneMatching(final long row, final long column, final Access1D<?> values, final long valueIndex) {
            myBase.fillOneMatching(row, column, values, valueIndex);
        }

        public void modifyOne(final long row, final long column, final UnaryFunction<N> function) {
            myBase.modifyOne(row, column, function);
        }

        public void set(final long row, final long column, final double value) {
            myBase.set(row, column, value);
        }

        public void set(final long row, final long column, final Number value) {
            myBase.set(row, column, value);
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

        public void add(final long row, final long column, final double addend) {
            myBase.add(myRowOffset + row, myColumnOffset + column, addend);
        }

        public void add(final long row, final long column, final Number addend) {
            myBase.add(myRowOffset + row, myColumnOffset + column, addend);
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

        public void fillColumn(final long row, final long column, final N value) {
            myBase.fillColumn(myRowOffset + row, myColumnOffset + column, value);
        }

        public void fillColumn(final long row, final long column, final NullaryFunction<N> supplier) {
            myBase.fillColumn(myRowOffset + row, myColumnOffset + column, supplier);
        }

        public void fillDiagonal(final long row, final long column, final N value) {
            myBase.fillDiagonal(myRowOffset + row, myColumnOffset + column, value);
        }

        public void fillDiagonal(final long row, final long column, final NullaryFunction<N> supplier) {
            myBase.fillDiagonal(myRowOffset + row, myColumnOffset + column, supplier);
        }

        public void fillOne(final long row, final long column, final N value) {
            myBase.fillOne(myRowOffset + row, myColumnOffset + column, value);
        }

        public void fillOne(final long row, final long column, final NullaryFunction<N> supplier) {
            myBase.fillOne(myRowOffset + row, myColumnOffset + column, supplier);
        }

        public void fillOneMatching(final long row, final long column, final Access1D<?> values, final long valueIndex) {
            myBase.fillOneMatching(myRowOffset + row, myColumnOffset + column, values, valueIndex);
        }

        public void fillRow(final long row, final long column, final N value) {
            myBase.fillRow(myRowOffset + row, myColumnOffset + column, value);
        }

        public void fillRow(final long row, final long column, final NullaryFunction<N> supplier) {
            myBase.fillRow(myRowOffset + row, myColumnOffset + column, supplier);
        }

        public void modifyAll(final UnaryFunction<N> function) {
            for (long j = myColumnOffset; j < myBase.countColumns(); j++) {
                myBase.modifyColumn(myRowOffset, j, function);
            }
        }

        public void modifyColumn(final long row, final long column, final UnaryFunction<N> function) {
            myBase.modifyColumn(myRowOffset + row, myColumnOffset + column, function);
        }

        public void modifyDiagonal(final long row, final long column, final UnaryFunction<N> function) {
            myBase.modifyDiagonal(myRowOffset + row, myColumnOffset + column, function);
        }

        public void modifyOne(final long row, final long column, final UnaryFunction<N> function) {
            myBase.modifyOne(myRowOffset + row, myColumnOffset + column, function);
        }

        public void modifyRow(final long row, final long column, final UnaryFunction<N> function) {
            myBase.modifyRow(myRowOffset + row, myColumnOffset + column, function);
        }

        public void set(final long row, final long column, final double value) {
            myBase.set(myRowOffset + row, myColumnOffset + column, value);
        }

        public void set(final long row, final long column, final Number value) {
            myBase.set(myRowOffset + row, myColumnOffset + column, value);
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

        public void add(final long row, final long column, final double addend) {
            myBase.add(myRows[(int) row], column, addend);
        }

        public void add(final long row, final long column, final Number addend) {
            myBase.add(myRows[(int) row], column, addend);
        }

        public long countColumns() {
            return myBase.countColumns();
        }

        public long countRows() {
            return myRows.length;
        }

        public void fillOne(final long row, final long column, final N value) {
            myBase.fillOne(myRows[(int) row], column, value);
        }

        public void fillOne(final long row, final long column, final NullaryFunction<N> supplier) {
            myBase.fillOne(myRows[(int) row], column, supplier);
        }

        public void fillOneMatching(final long row, final long column, final Access1D<?> values, final long valueIndex) {
            myBase.fillOneMatching(myRows[(int) row], column, values, valueIndex);
        }

        public void fillRow(final long row, final long column, final Access1D<N> values) {
            myBase.fillRow(myRows[(int) row], column, values);
        }

        public void fillRow(final long row, final long column, final N value) {
            myBase.fillRow(myRows[(int) row], column, value);
        }

        public void fillRow(final long row, final long column, final NullaryFunction<N> supplier) {
            myBase.fillRow(myRows[(int) row], column, supplier);
        }

        public void modifyOne(final long row, final long column, final UnaryFunction<N> function) {
            myBase.modifyOne(myRows[(int) row], column, function);
        }

        public void modifyRow(final long row, final long column, final UnaryFunction<N> function) {
            myBase.modifyRow(myRows[(int) row], column, function);
        }

        public void set(final long row, final long column, final double value) {
            myBase.set(myRows[(int) row], column, value);
        }

        public void set(final long row, final long column, final Number value) {
            myBase.set(myRows[(int) row], column, value);
        }

    }

    public static final class TransposedRegion<N extends Number> extends ConsumerRegion<N> {

        private final ElementsConsumer<N> myBase;

        TransposedRegion(final ElementsConsumer<N> base, final FillByMultiplying<N> multiplier) {
            super(multiplier, base.countColumns(), base.countRows());
            myBase = base;
        }

        public void add(final long row, final long column, final double addend) {
            myBase.add(column, row, addend);
        }

        public void add(final long row, final long column, final Number addend) {
            myBase.add(column, row, addend);
        }

        public long countColumns() {
            return myBase.countRows();
        }

        public long countRows() {
            return myBase.countColumns();
        }

        public void fillColumn(final long row, final long column, final N value) {
            myBase.fillRow(column, row, value);
        }

        public void fillColumn(final long row, final long column, final NullaryFunction<N> supplier) {
            myBase.fillRow(column, row, supplier);
        }

        public void fillDiagonal(final long row, final long column, final N value) {
            myBase.fillDiagonal(column, row, value);
        }

        public void fillDiagonal(final long row, final long column, final NullaryFunction<N> supplier) {
            myBase.fillRow(column, row, supplier);
        }

        public void fillOne(final long row, final long column, final N value) {
            myBase.fillOne(column, row, value);
        }

        public void fillOne(final long row, final long column, final NullaryFunction<N> supplier) {
            myBase.fillOne(column, row, supplier);
        }

        public void fillOneMatching(final long row, final long column, final Access1D<?> values, final long valueIndex) {
            myBase.fillOneMatching(column, row, values, valueIndex);
        }

        public void fillRow(final long row, final long column, final N value) {
            myBase.fillDiagonal(column, row, value);
        }

        public void fillRow(final long row, final long column, final NullaryFunction<N> supplier) {
            myBase.fillDiagonal(column, row, supplier);
        }

        public void modifyColumn(final long row, final long column, final UnaryFunction<N> function) {
            myBase.modifyRow(column, row, function);
        }

        public void modifyDiagonal(final long row, final long column, final UnaryFunction<N> function) {
            myBase.modifyDiagonal(column, row, function);
        }

        @Override
        public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
            myBase.modifyMatching(left, function);
        }

        @Override
        public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
            myBase.modifyMatching(function, right);
        }

        public void modifyOne(final long row, final long column, final UnaryFunction<N> function) {
            myBase.modifyOne(column, row, function);
        }

        public void modifyRow(final long row, final long column, final UnaryFunction<N> function) {
            myBase.modifyColumn(column, row, function);
        }

        @Override
        public ElementsConsumer<N> regionByTransposing() {
            return myBase;
        }

        public void set(final long row, final long column, final double value) {
            myBase.set(column, row, value);
        }

        public void set(final long row, final long column, final Number value) {
            myBase.set(column, row, value);
        }

    }

    /**
     * @return The elements of the physical store as a fixed size (1 dimensional) list. The elements may be
     *         accessed either row or colomn major.
     */
    List<N> asList();

    /**
     * <p>
     * <b>c</b>olumn <b>a</b> * <b>x</b> <b>p</b>lus <b>y</b>
     * </p>
     * [this(*,aColY)] = aSclrA [this(*,aColX)] + [this(*,aColY)]
     *
     * @deprecated v32 Let me know if you need this
     */
    @Deprecated
    void caxpy(final N scalarA, final int columnX, final int columnY, final int firstRow);

    /**
     * <p>
     * <b>m</b>atrix <b>a</b> * <b>x</b> <b>p</b>lus <b>y</b>
     * </p>
     * [this] = aSclrA [aMtrxX] + [this]
     *
     * @deprecated v32 Let me know if you need this
     */
    @Deprecated
    void maxpy(final N scalarA, final MatrixStore<N> matrixX);

    /**
     * <p>
     * <b>r</b>ow <b>a</b> * <b>x</b> <b>p</b>lus <b>y</b>
     * </p>
     * [this(aRowY,*)] = aSclrA [this(aRowX,*)] + [this(aRowY,*)]
     *
     * @deprecated v32 Let me know if you need this
     */
    @Deprecated
    void raxpy(final N scalarA, final int rowX, final int rowY, final int firstColumn);

    void transformLeft(Householder<N> transformation, int firstColumn);

    /**
     * <p>
     * As in {@link MatrixStore#multiplyLeft(MatrixStore)} where the left/parameter matrix is a plane
     * rotation.
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
     * As in {@link MatrixStore#multiply(Access1D)} where the right/parameter matrix is a plane rotation.
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
