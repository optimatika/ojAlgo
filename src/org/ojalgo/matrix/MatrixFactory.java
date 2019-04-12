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

        DenseReceiver(PhysicalStore<N> delegate) {
            super(delegate);
        }

    }

    @SuppressWarnings("unchecked")
    abstract class Logical implements BasicMatrix.LogicalBuilder<N, M> {

        private final MatrixStore.LogicalBuilder<N> myDelegate;

        Logical(MatrixStore.LogicalBuilder<N> delegate) {
            super();
            myDelegate = delegate;
        }

        Logical(MatrixStore<N> store) {
            this(store.logical());
        }

        public B above(int numberOfRows) {
            myDelegate.above(numberOfRows);
            return this.self();
        }

        public B above(M... above) {
            myDelegate.above(this.cast(above));
            return this.self();
        }

        public B above(N... elements) {
            myDelegate.above(elements);
            return this.self();
        }

        public B below(int numberOfRows) {
            myDelegate.below(numberOfRows);
            return this.self();
        }

        public B below(M... below) {
            myDelegate.below(this.cast(below));
            return this.self();
        }

        public B below(N... elements) {
            myDelegate.below(elements);
            return this.self();
        }

        public B bidiagonal(boolean upper, boolean assumeOne) {
            myDelegate.bidiagonal(upper, assumeOne);
            return this.self();
        }

        public B column(int... columns) {
            myDelegate.column(columns);
            return this.self();
        }

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

        public B diagonally(M... diagonally) {
            myDelegate.diagonally(this.cast(diagonally));
            return this.self();
        }

        public M get() {
            return MatrixFactory.this.instantiate(myDelegate.get());
        }

        public B hermitian(boolean upper) {
            myDelegate.hermitian(upper);
            return this.self();
        }

        public B hessenberg(boolean upper) {
            myDelegate.hessenberg(upper);
            return this.self();
        }

        public B left(int numberOfColumns) {
            myDelegate.left(numberOfColumns);
            return this.self();
        }

        public B left(M... left) {
            myDelegate.left(this.cast(left));
            return this.self();
        }

        public B left(N... elements) {
            myDelegate.left(elements);
            return this.self();
        }

        public B limits(int rowLimit, int columnLimit) {
            myDelegate.limits(rowLimit, columnLimit);
            return this.self();
        }

        public B offsets(int rowOffset, int columnOffset) {
            myDelegate.offsets(rowOffset, columnOffset);
            return this.self();
        }

        public B right(int numberOfColumns) {
            myDelegate.right(numberOfColumns);
            return this.self();
        }

        public B right(M... right) {
            myDelegate.right(this.cast(right));
            return this.self();
        }

        public B right(N... elements) {
            myDelegate.right(elements);
            return this.self();
        }

        public B row(int... rows) {
            myDelegate.row(rows);
            return this.self();
        }

        public B superimpose(int row, int col, M matrix) {
            myDelegate.superimpose(row, col, matrix.getStore());
            return this.self();
        }

        public B superimpose(int row, int col, Number matrix) {
            myDelegate.superimpose(row, col, matrix);
            return this.self();
        }

        public B superimpose(M matrix) {
            myDelegate.superimpose(matrix.getStore());
            return this.self();
        }

        public void supplyTo(PhysicalStore<N> receiver) {
            myDelegate.supplyTo(receiver);
        }

        public B transpose() {
            myDelegate.transpose();
            return this.self();
        }

        public B triangular(boolean upper, boolean assumeOne) {
            myDelegate.triangular(upper, assumeOne);
            return this.self();
        }

        public B tridiagonal() {
            myDelegate.tridiagonal();
            return this.self();
        }

        MatrixStore<N>[] cast(M[] matrices) {
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

        public void accept(Access2D<?> supplied) {
            if (mySafe) {
                myDelegate.accept(supplied);
            } else {
                throw new IllegalStateException();
            }
        }

        public void add(long index, double addend) {
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

        public void add(long index, Number addend) {
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

        public void exchangeColumns(long colA, long colB) {
            if (mySafe) {
                myDelegate.exchangeColumns(colA, colB);
            } else {
                throw new IllegalStateException();
            }
        }

        public void exchangeRows(long rowA, long rowB) {
            if (mySafe) {
                myDelegate.exchangeRows(rowA, rowB);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillAll(NullaryFunction<N> supplier) {
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

        public void fillColumn(long col, Access1D<N> values) {
            if (mySafe) {
                myDelegate.fillColumn(col, values);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillColumn(long row, long col, Access1D<N> values) {
            if (mySafe) {
                myDelegate.fillColumn(row, col, values);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillColumn(long row, long col, NullaryFunction<N> supplier) {
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

        public void fillColumn(long col, N value) {
            if (mySafe) {
                myDelegate.fillColumn(col, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillColumn(long col, NullaryFunction<N> supplier) {
            if (mySafe) {
                myDelegate.fillColumn(col, supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillDiagonal(Access1D<N> values) {
            if (mySafe) {
                myDelegate.fillDiagonal(values);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillDiagonal(long row, long col, Access1D<N> values) {
            if (mySafe) {
                myDelegate.fillDiagonal(row, col, values);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillDiagonal(long row, long col, NullaryFunction<N> supplier) {
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

        public void fillDiagonal(N value) {
            if (mySafe) {
                myDelegate.fillDiagonal(value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillDiagonal(NullaryFunction<N> supplier) {
            if (mySafe) {
                myDelegate.fillDiagonal(supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillMatching(Access1D<?> values) {
            if (mySafe) {
                myDelegate.fillMatching(values);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillMatching(Access1D<N> left, BinaryFunction<N> function, Access1D<N> right) {
            if (mySafe) {
                myDelegate.fillMatching(left, function, right);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillMatching(UnaryFunction<N> function, Access1D<N> arguments) {
            if (mySafe) {
                myDelegate.fillMatching(function, arguments);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillOne(long index, Access1D<?> values, long valueIndex) {
            if (mySafe) {
                myDelegate.fillOne(index, values, valueIndex);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillOne(long row, long col, Access1D<?> values, long valueIndex) {
            if (mySafe) {
                myDelegate.fillOne(row, col, values, valueIndex);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillOne(long row, long col, N value) {
            if (mySafe) {
                myDelegate.fillOne(row, col, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillOne(long row, long col, NullaryFunction<N> supplier) {
            if (mySafe) {
                myDelegate.fillOne(row, col, supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillOne(long index, N value) {
            if (mySafe) {
                myDelegate.fillOne(index, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillOne(long index, NullaryFunction<N> supplier) {
            if (mySafe) {
                myDelegate.fillOne(index, supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillRange(long first, long limit, N value) {
            if (mySafe) {
                myDelegate.fillRange(first, limit, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillRange(long first, long limit, NullaryFunction<N> supplier) {
            if (mySafe) {
                myDelegate.fillRange(first, limit, supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillRow(long row, Access1D<N> values) {
            if (mySafe) {
                myDelegate.fillRow(row, values);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillRow(long row, long col, Access1D<N> values) {
            if (mySafe) {
                myDelegate.fillRow(row, col, values);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillRow(long row, long col, NullaryFunction<N> supplier) {
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

        public void fillRow(long row, N value) {
            if (mySafe) {
                myDelegate.fillRow(row, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillRow(long row, NullaryFunction<N> supplier) {
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

        public void modifyAll(UnaryFunction<N> modifier) {
            if (mySafe) {
                myDelegate.modifyAll(modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyAny(Transformation2D<N> modifier) {
            if (mySafe) {
                modifier.transform(myDelegate);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyColumn(long row, long col, UnaryFunction<N> modifier) {
            if (mySafe) {
                myDelegate.modifyColumn(row, col, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyColumn(long col, UnaryFunction<N> modifier) {
            if (mySafe) {
                myDelegate.modifyColumn(col, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyDiagonal(long row, long col, UnaryFunction<N> modifier) {
            if (mySafe) {
                myDelegate.modifyDiagonal(row, col, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyDiagonal(UnaryFunction<N> modifier) {
            if (mySafe) {
                myDelegate.modifyDiagonal(modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyMatching(Access1D<N> left, BinaryFunction<N> function) {
            if (mySafe) {
                myDelegate.modifyMatching(left, function);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyMatching(BinaryFunction<N> function, Access1D<N> right) {
            if (mySafe) {
                myDelegate.modifyMatching(function, right);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyMatchingInColumns(Access1D<N> left, BinaryFunction<N> function) {
            if (mySafe) {
                myDelegate.modifyMatchingInColumns(left, function);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyMatchingInColumns(BinaryFunction<N> function, Access1D<N> right) {
            if (mySafe) {
                myDelegate.modifyMatchingInColumns(function, right);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyMatchingInRows(Access1D<N> left, BinaryFunction<N> function) {
            if (mySafe) {
                myDelegate.modifyMatchingInRows(left, function);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyMatchingInRows(BinaryFunction<N> function, Access1D<N> right) {
            if (mySafe) {
                myDelegate.modifyMatchingInRows(function, right);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyOne(long row, long col, UnaryFunction<N> modifier) {
            if (mySafe) {
                myDelegate.modifyOne(row, col, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyOne(long index, UnaryFunction<N> modifier) {
            if (mySafe) {
                myDelegate.modifyOne(index, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyRange(long first, long limit, UnaryFunction<N> modifier) {
            if (mySafe) {
                myDelegate.modifyRange(first, limit, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyRow(long row, long col, UnaryFunction<N> modifier) {
            if (mySafe) {
                myDelegate.modifyRow(row, col, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyRow(long row, UnaryFunction<N> modifier) {
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

        public void supplyTo(PhysicalStore<N> receiver) {
            myDelegate.supplyTo(receiver);
        }
    }

    abstract class SparseReceiver extends Physical<SparseStore<N>> {

        SparseReceiver(SparseStore<N> delegate) {
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
