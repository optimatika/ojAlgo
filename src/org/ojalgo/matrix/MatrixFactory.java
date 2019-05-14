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
package org.ojalgo.matrix;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Factory2D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.structure.Transformation2D;

/**
 * MatrixFactory creates instances of classes that implement the {@linkplain org.ojalgo.matrix.BasicMatrix}
 * interface and have a constructor that takes a MatrixStore as input.
 *
 * @author apete
 */
abstract class MatrixFactory<N extends Number, M extends BasicMatrix<N, M>, B extends BasicMatrix.LogicalBuilder<N, M>, DR extends BasicMatrix.PhysicalReceiver<N, M>, SR extends BasicMatrix.PhysicalReceiver<N, M>>
        implements Factory2D<M> {

    abstract class DenseReceiver extends Physical<PhysicalStore<N>> {

        DenseReceiver(final PhysicalStore<N> delegate) {
            super(delegate);
        }

    }

    @SuppressWarnings("unchecked")
    abstract class Logical implements BasicMatrix.LogicalBuilder<N, M> {

        private final MatrixStore.LogicalBuilder<N> myDelegate;

        Logical(final MatrixStore.LogicalBuilder<N> delegate) {
            super();
            myDelegate = delegate;
        }

        Logical(final MatrixStore<N> store) {
            this(store.logical());
        }

        public B above(final long numberOfRows) {
            myDelegate.above(numberOfRows);
            return this.self();
        }

        public B above(final M... above) {
            myDelegate.above(this.cast(above));
            return this.self();
        }

        public B above(final M matrix) {
            myDelegate.above(this.cast(matrix));
            return this.self();
        }

        public B above(final M above1, final M above2) {
            myDelegate.above(this.cast(above1, above2));
            return this.self();
        }

        /**
         * @deprecated v48
         */
        @Deprecated
        public B above(final N... elements) {
            myDelegate.above(elements);
            return this.self();
        }

        public B below(final long numberOfRows) {
            myDelegate.below(numberOfRows);
            return this.self();
        }

        public B below(final M... below) {
            myDelegate.below(this.cast(below));
            return this.self();
        }

        public B below(final M matrix) {
            myDelegate.below(this.cast(matrix));
            return this.self();
        }

        public B below(final M below1, final M below2) {
            myDelegate.below(this.cast(below1, below2));
            return this.self();
        }

        /**
         * @deprecated v48
         */
        @Deprecated
        public B below(final N... elements) {
            myDelegate.below(elements);
            return this.self();
        }

        public B bidiagonal(final boolean upper) {
            myDelegate.bidiagonal(upper);
            return this.self();
        }

        /**
         * @deprecated v48 Use {@link #bidiagonal(boolean)} instead
         */
        @Deprecated
        public B bidiagonal(final boolean upper, final boolean assumeOne) {
            myDelegate.bidiagonal(upper, assumeOne);
            return this.self();
        }

        public B columns(final int[] columns) {
            myDelegate.column(columns);
            return this.self();
        }

        /**
         * @deprecated v48 Use {@link BasicMatrix#conjugate()} instead
         */
        @Deprecated
        public B conjugate() {
            myDelegate.conjugate();
            return this.self();
        }

        public long countColumns() {
            return myDelegate.countColumns();
        }

        public long countRows() {
            return myDelegate.countRows();
        }

        public B diagonal() {
            myDelegate.diagonal();
            return this.self();
        }

        public B diagonal(final boolean maintain) {
            myDelegate.diagonal(maintain);
            return this.self();
        }

        public B diagonally(final M... diagonally) {
            myDelegate.diagonally(this.cast(diagonally));
            return this.self();
        }

        public M get() {
            return MatrixFactory.this.instantiate(myDelegate.get());
        }

        /**
         * @deprecated v48
         */
        @Deprecated
        public B hermitian(final boolean upper) {
            myDelegate.hermitian(upper);
            return this.self();
        }

        public B hessenberg(final boolean upper) {
            myDelegate.hessenberg(upper);
            return this.self();
        }

        public B left(final long numberOfColumns) {
            myDelegate.left(numberOfColumns);
            return this.self();
        }

        public B left(final M... left) {
            myDelegate.left(this.cast(left));
            return this.self();
        }

        public B left(final M matrix) {
            myDelegate.left(this.cast(matrix));
            return this.self();
        }

        public B left(final M left1, final M left2) {
            myDelegate.left(this.cast(left1, left2));
            return this.self();
        }

        /**
         * @deprecated v48
         */
        @Deprecated
        public B left(final N... elements) {
            myDelegate.left(elements);
            return this.self();
        }

        public B limits(final long rowLimit, final long columnLimit) {
            myDelegate.limits(rowLimit, columnLimit);
            return this.self();
        }

        public B offsets(final long rowOffset, final long columnOffset) {
            myDelegate.offsets(rowOffset, columnOffset);
            return this.self();
        }

        public B right(final long numberOfColumns) {
            myDelegate.right(numberOfColumns);
            return this.self();
        }

        public B right(final M... right) {
            myDelegate.right(this.cast(right));
            return this.self();
        }

        public B right(final M matrix) {
            myDelegate.right(this.cast(matrix));
            return this.self();
        }

        public B right(final M right1, final M right2) {
            myDelegate.right(this.cast(right1, right2));
            return this.self();
        }

        /**
         * @deprecated v48
         */
        @Deprecated
        public B right(final N... elements) {
            myDelegate.right(elements);
            return this.self();
        }

        public B rows(final int[] rows) {
            myDelegate.row(rows);
            return this.self();
        }

        /**
         * @deprecated v48
         */
        @Deprecated
        public B superimpose(final int row, final int col, final M matrix) {
            myDelegate.superimpose(row, col, matrix.getStore());
            return this.self();
        }

        /**
         * @deprecated v48
         */
        @Deprecated
        public B superimpose(final int row, final int col, final Number matrix) {
            myDelegate.superimpose(row, col, matrix);
            return this.self();
        }

        /**
         * @deprecated v48
         */
        @Deprecated
        public B superimpose(final M matrix) {
            myDelegate.superimpose(matrix.getStore());
            return this.self();
        }

        public void supplyTo(final PhysicalStore<N> receiver) {
            myDelegate.supplyTo(receiver);
        }

        /**
         * @deprecated v48 Use {@link BasicMatrix#transpose()} instead
         */
        @Deprecated
        public B transpose() {
            myDelegate.transpose();
            return this.self();
        }

        public B triangular(final boolean upper, final boolean assumeOne) {
            myDelegate.triangular(upper, assumeOne);
            return this.self();
        }

        public B tridiagonal() {
            myDelegate.tridiagonal();
            return this.self();
        }

        MatrixStore<N> cast(final M matrix) {
            return matrix.getStore();
        }

        MatrixStore<N>[] cast(final M matrix1, final M matrix2) {
            MatrixStore<N>[] retVal = (MatrixStore<N>[]) new MatrixStore<?>[2];
            retVal[0] = matrix1.getStore();
            retVal[1] = matrix2.getStore();
            return retVal;
        }

        MatrixStore<N>[] cast(final M[] matrices) {
            MatrixStore<N>[] retVal = (MatrixStore<N>[]) new MatrixStore<?>[matrices.length];
            for (int i = 0; i < retVal.length; i++) {
                retVal[i] = matrices[i].getStore();
            }
            return retVal;
        }

        abstract B self();

    }

    abstract class Physical<PR extends MatrixStore<N> & Mutate2D.ModifiableReceiver<N>> implements BasicMatrix.PhysicalReceiver<N, M> {

        private final PR myDelegate;
        private boolean mySafe = true;

        Physical(final PR delegate) {

            super();

            myDelegate = delegate;
        }

        public void accept(final Access2D<?> supplied) {
            if (mySafe) {
                myDelegate.accept(supplied);
            } else {
                throw new IllegalStateException();
            }
        }

        public void add(final long index, final double addend) {
            if (mySafe) {
                myDelegate.add(index, addend);
            } else {
                throw new IllegalStateException();
            }
        }

        public void add(final long row, final long col, final double value) {
            if (mySafe) {
                myDelegate.add(row, col, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void add(final long row, final long col, final Number value) {
            if (mySafe) {
                myDelegate.add(row, col, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void add(final long index, final Number addend) {
            if (mySafe) {
                myDelegate.add(index, addend);
            } else {
                throw new IllegalStateException();
            }
        }

        public long count() {
            return myDelegate.count();
        }

        public long countColumns() {
            return myDelegate.countColumns();
        }

        public long countRows() {
            return myDelegate.countRows();
        }

        public void exchangeColumns(final long colA, final long colB) {
            if (mySafe) {
                myDelegate.exchangeColumns(colA, colB);
            } else {
                throw new IllegalStateException();
            }
        }

        public void exchangeRows(final long rowA, final long rowB) {
            if (mySafe) {
                myDelegate.exchangeRows(rowA, rowB);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillAll(final NullaryFunction<N> supplier) {
            if (mySafe) {
                myDelegate.fillAll(supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillAll(final Number value) {
            if (mySafe) {
                myDelegate.fillAll(myDelegate.physical().scalar().cast(value));
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillColumn(final long col, final Access1D<N> values) {
            if (mySafe) {
                myDelegate.fillColumn(col, values);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillColumn(final long row, final long col, final Access1D<N> values) {
            if (mySafe) {
                myDelegate.fillColumn(row, col, values);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillColumn(final long row, final long col, final NullaryFunction<N> supplier) {
            if (mySafe) {
                myDelegate.fillColumn(row, col, supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillColumn(final long row, final long column, final Number value) {
            if (mySafe) {
                myDelegate.fillColumn((int) row, (int) column, myDelegate.physical().scalar().cast(value));
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillColumn(final long col, final N value) {
            if (mySafe) {
                myDelegate.fillColumn(col, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillColumn(final long col, final NullaryFunction<N> supplier) {
            if (mySafe) {
                myDelegate.fillColumn(col, supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillDiagonal(final Access1D<N> values) {
            if (mySafe) {
                myDelegate.fillDiagonal(values);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillDiagonal(final long row, final long col, final Access1D<N> values) {
            if (mySafe) {
                myDelegate.fillDiagonal(row, col, values);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillDiagonal(final long row, final long col, final NullaryFunction<N> supplier) {
            if (mySafe) {
                myDelegate.fillDiagonal(row, col, supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillDiagonal(final long row, final long column, final Number value) {
            if (mySafe) {
                myDelegate.fillDiagonal((int) row, (int) column, myDelegate.physical().scalar().cast(value));
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillDiagonal(final N value) {
            if (mySafe) {
                myDelegate.fillDiagonal(value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillDiagonal(final NullaryFunction<N> supplier) {
            if (mySafe) {
                myDelegate.fillDiagonal(supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillMatching(final Access1D<?> values) {
            if (mySafe) {
                myDelegate.fillMatching(values);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillMatching(final Access1D<N> left, final BinaryFunction<N> function, final Access1D<N> right) {
            if (mySafe) {
                myDelegate.fillMatching(left, function, right);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillMatching(final UnaryFunction<N> function, final Access1D<N> arguments) {
            if (mySafe) {
                myDelegate.fillMatching(function, arguments);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillOne(final long index, final Access1D<?> values, final long valueIndex) {
            if (mySafe) {
                myDelegate.fillOne(index, values, valueIndex);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
            if (mySafe) {
                myDelegate.fillOne(row, col, values, valueIndex);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillOne(final long row, final long col, final N value) {
            if (mySafe) {
                myDelegate.fillOne(row, col, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillOne(final long row, final long col, final NullaryFunction<N> supplier) {
            if (mySafe) {
                myDelegate.fillOne(row, col, supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillOne(final long index, final N value) {
            if (mySafe) {
                myDelegate.fillOne(index, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillOne(final long index, final NullaryFunction<N> supplier) {
            if (mySafe) {
                myDelegate.fillOne(index, supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillRange(final long first, final long limit, final N value) {
            if (mySafe) {
                myDelegate.fillRange(first, limit, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillRange(final long first, final long limit, final NullaryFunction<N> supplier) {
            if (mySafe) {
                myDelegate.fillRange(first, limit, supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillRow(final long row, final Access1D<N> values) {
            if (mySafe) {
                myDelegate.fillRow(row, values);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillRow(final long row, final long col, final Access1D<N> values) {
            if (mySafe) {
                myDelegate.fillRow(row, col, values);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillRow(final long row, final long col, final NullaryFunction<N> supplier) {
            if (mySafe) {
                myDelegate.fillRow(row, col, supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillRow(final long row, final long column, final Number value) {
            if (mySafe) {
                myDelegate.fillRow((int) row, (int) column, myDelegate.physical().scalar().cast(value));
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillRow(final long row, final N value) {
            if (mySafe) {
                myDelegate.fillRow(row, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillRow(final long row, final NullaryFunction<N> supplier) {
            if (mySafe) {
                myDelegate.fillRow(row, supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public M get() {
            mySafe = false;
            return MatrixFactory.this.instantiate(myDelegate);
        }

        public void modifyAll(final UnaryFunction<N> modifier) {
            if (mySafe) {
                myDelegate.modifyAll(modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyAny(final Transformation2D<N> modifier) {
            if (mySafe) {
                modifier.transform(myDelegate);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyColumn(final long row, final long col, final UnaryFunction<N> modifier) {
            if (mySafe) {
                myDelegate.modifyColumn(row, col, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyColumn(final long col, final UnaryFunction<N> modifier) {
            if (mySafe) {
                myDelegate.modifyColumn(col, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyDiagonal(final long row, final long col, final UnaryFunction<N> modifier) {
            if (mySafe) {
                myDelegate.modifyDiagonal(row, col, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyDiagonal(final UnaryFunction<N> modifier) {
            if (mySafe) {
                myDelegate.modifyDiagonal(modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
            if (mySafe) {
                myDelegate.modifyMatching(left, function);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
            if (mySafe) {
                myDelegate.modifyMatching(function, right);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyMatchingInColumns(final Access1D<N> left, final BinaryFunction<N> function) {
            if (mySafe) {
                myDelegate.modifyMatchingInColumns(left, function);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyMatchingInColumns(final BinaryFunction<N> function, final Access1D<N> right) {
            if (mySafe) {
                myDelegate.modifyMatchingInColumns(function, right);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyMatchingInRows(final Access1D<N> left, final BinaryFunction<N> function) {
            if (mySafe) {
                myDelegate.modifyMatchingInRows(left, function);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyMatchingInRows(final BinaryFunction<N> function, final Access1D<N> right) {
            if (mySafe) {
                myDelegate.modifyMatchingInRows(function, right);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {
            if (mySafe) {
                myDelegate.modifyOne(row, col, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyOne(final long index, final UnaryFunction<N> modifier) {
            if (mySafe) {
                myDelegate.modifyOne(index, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyRange(final long first, final long limit, final UnaryFunction<N> modifier) {
            if (mySafe) {
                myDelegate.modifyRange(first, limit, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyRow(final long row, final long col, final UnaryFunction<N> modifier) {
            if (mySafe) {
                myDelegate.modifyRow(row, col, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyRow(final long row, final UnaryFunction<N> modifier) {
            if (mySafe) {
                myDelegate.modifyRow(row, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void reset() {
            if (mySafe) {
                myDelegate.reset();
            } else {
                throw new IllegalStateException();
            }
        }

        public void set(final long index, final double value) {
            if (mySafe) {
                myDelegate.set(index, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void set(final long row, final long col, final double value) {
            if (mySafe) {
                myDelegate.set(row, col, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void set(final long row, final long col, final Number value) {
            if (mySafe) {
                myDelegate.set(row, col, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void set(final long index, final Number value) {
            if (mySafe) {
                myDelegate.set(index, myDelegate.physical().scalar().cast(value));
            } else {
                throw new IllegalStateException();
            }
        }

        public void supplyTo(final PhysicalStore<N> receiver) {
            myDelegate.supplyTo(receiver);
        }
    }

    abstract class SparseReceiver extends Physical<SparseStore<N>> {

        SparseReceiver(final SparseStore<N> delegate) {
            super(delegate);
        }

    }

    private static Constructor<? extends BasicMatrix<?, ?>> getConstructor(final Class<? extends BasicMatrix<?, ?>> aTemplate) {
        try {
            final Constructor<? extends BasicMatrix<?, ?>> retVal = aTemplate.getDeclaredConstructor(MatrixStore.class);
            retVal.setAccessible(true);
            return retVal;
        } catch (final SecurityException | NoSuchMethodException exception) {
            return null;
        }
    }

    private final Constructor<M> myConstructor;
    private final PhysicalStore.Factory<N, ?> myPhysicalFactory;

    @SuppressWarnings("unchecked")
    MatrixFactory(final Class<M> template, final PhysicalStore.Factory<N, ?> factory) {

        super();

        myPhysicalFactory = factory;
        myConstructor = (Constructor<M>) MatrixFactory.getConstructor(template);
    }

    public M columns(final Access1D<?>... source) {
        return this.instantiate(myPhysicalFactory.columns(source));
    }

    public M columns(final double[]... source) {
        return this.instantiate(myPhysicalFactory.columns(source));
    }

    @SafeVarargs
    public final M columns(final List<? extends Number>... source) {
        return this.instantiate(myPhysicalFactory.columns(source));
    }

    public M columns(final Number[]... source) {
        return this.instantiate(myPhysicalFactory.columns(source));
    }

    public M copy(final Access2D<?> source) {
        return this.instantiate(myPhysicalFactory.copy(source));
    }

    @Override
    public FunctionSet<N> function() {
        return myPhysicalFactory.function();
    }

    /**
     * @deprecated v47 Use {@link #makeDense(int)} instead
     */
    @Deprecated
    public DR getBuilder(final int count) {
        return this.makeDense(count);
    }

    /**
     * @deprecated v47 Use {@link #makeDense(int,int)} instead
     */
    @Deprecated
    public DR getBuilder(final int rows, final int columns) {
        return this.makeDense(rows, columns);
    }

    public DR makeDense(final int count) {
        return this.makeDense(count, 1);
    }

    public DR makeDense(final int rows, final int columns) {
        return this.physical(myPhysicalFactory.makeZero(rows, columns));
    }

    public M makeEye(final int rows, final int columns) {

        final int square = Math.min(rows, columns);

        MatrixStore.LogicalBuilder<N> retVal = myPhysicalFactory.builder().makeIdentity(square);

        if (rows > square) {
            retVal = retVal.below(rows - square);
        } else if (columns > square) {
            retVal = retVal.right(columns - square);
        }

        return this.instantiate(retVal.get());
    }

    public M makeEye(final Structure2D shape) {
        return this.makeEye(Math.toIntExact(shape.countRows()), Math.toIntExact(shape.countColumns()));
    }

    public M makeFilled(final long rows, final long columns, final NullaryFunction<?> supplier) {
        return this.instantiate(myPhysicalFactory.makeFilled(rows, columns, supplier));
    }

    public M makeIdentity(final int dimension) {
        return this.instantiate(myPhysicalFactory.builder().makeIdentity(dimension).get());
    }

    public M makeSingle(final N element) {
        return this.instantiate(myPhysicalFactory.builder().makeSingle(element).get());
    }

    public SR makeSparse(final int rows, final int columns) {
        return this.physical(myPhysicalFactory.builder().makeSparse(rows, columns));
    }

    public SR makeSparse(final Structure2D shape) {
        return this.makeSparse(Math.toIntExact(shape.countRows()), Math.toIntExact(shape.countColumns()));
    }

    public M makeWrapper(final Access2D<?> elements) {
        return this.instantiate(myPhysicalFactory.builder().makeWrapper(elements).get());
    }

    public M makeZero(final long rows, final long columns) {
        return this.instantiate(myPhysicalFactory.builder().makeZero((int) rows, (int) columns).get());
    }

    public M rows(final Access1D<?>... source) {
        return this.instantiate(myPhysicalFactory.rows(source));
    }

    public M rows(final double[]... source) {
        return this.instantiate(myPhysicalFactory.rows(source));
    }

    @SuppressWarnings("unchecked")
    public M rows(final List<? extends Number>... source) {
        return this.instantiate(myPhysicalFactory.rows(source));
    }

    public M rows(final Number[]... source) {
        return this.instantiate(myPhysicalFactory.rows(source));
    }

    @Override
    public Scalar.Factory<N> scalar() {
        return myPhysicalFactory.scalar();
    }

    /**
     * This method is for internal use only - YOU should NOT use it!
     */
    M instantiate(final MatrixStore<N> store) {
        try {
            return myConstructor.newInstance(store);
        } catch (final IllegalArgumentException anException) {
            throw new ProgrammingError(anException);
        } catch (final InstantiationException anException) {
            throw new ProgrammingError(anException);
        } catch (final IllegalAccessException anException) {
            throw new ProgrammingError(anException);
        } catch (final InvocationTargetException anException) {
            throw new ProgrammingError(anException);
        }
    }

    abstract B logical(final MatrixStore<N> delegate);

    abstract DR physical(final PhysicalStore<N> delegate);

    abstract SR physical(final SparseStore<N> delegate);

}
