/*
 * Copyright 1997-2021 Optimatika
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
import java.util.function.Supplier;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.scalar.Scalar.Factory;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Factory1D;
import org.ojalgo.structure.Factory2D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.structure.Transformation2D;
import org.ojalgo.tensor.TensorFactory1D;
import org.ojalgo.tensor.TensorFactory2D;

/**
 * MatrixFactory creates instances of classes that implement the {@linkplain org.ojalgo.matrix.BasicMatrix}
 * interface and have a constructor that takes a MatrixStore as input.
 *
 * @author apete
 */
public abstract class MatrixFactory<N extends Comparable<N>, M extends BasicMatrix<N, M>, LB extends BasicMatrix.LogicalBuilder<N, M>, DR extends Mutate2D.ModifiableReceiver<N> & Supplier<M>, SR extends Mutate2D.ModifiableReceiver<N> & Supplier<M>>
        implements Factory2D.Dense<M>, Factory2D.MayBeSparse<M, DR, SR> {

    /**
     * Logical
     *
     * @author apete
     * @deprecated v50 Use {@link Pipeline2D} instead
     */
    @Deprecated
    abstract class Logical implements BasicMatrix.LogicalBuilder<N, M> {

        private MatrixStore<N> myDelegate;

        Logical(final MatrixStore<N> delegate) {
            super();
            myDelegate = delegate;
        }

        public LB above(final long numberOfRows) {
            myDelegate = myDelegate.above(numberOfRows);
            return this.self();
        }

        public LB above(final M... above) {
            myDelegate = myDelegate.above(this.cast(above));
            return this.self();
        }

        public LB above(final M matrix) {
            myDelegate = myDelegate.above(this.cast(matrix));
            return this.self();
        }

        public LB below(final long numberOfRows) {
            myDelegate = myDelegate.below(numberOfRows);
            return this.self();
        }

        public LB below(final M... below) {
            myDelegate = myDelegate.below(this.cast(below));
            return this.self();
        }

        public LB below(final M matrix) {
            myDelegate = myDelegate.below(this.cast(matrix));
            return this.self();
        }

        public LB bidiagonal(final boolean upper) {
            myDelegate = myDelegate.bidiagonal(upper);
            return this.self();
        }

        public LB columns(final int[] columns) {
            myDelegate = myDelegate.column(columns);
            return this.self();
        }

        public LB conjugate() {
            myDelegate = myDelegate.conjugate();
            return this.self();
        }

        public long countColumns() {
            return myDelegate.countColumns();
        }

        public long countRows() {
            return myDelegate.countRows();
        }

        public LB diagonal() {
            myDelegate = myDelegate.diagonal();
            return this.self();
        }

        public LB diagonally(final M... diagonally) {
            myDelegate = myDelegate.diagonally(this.cast(diagonally));
            return this.self();
        }

        public M get() {
            return MatrixFactory.this.instantiate(myDelegate.get());
        }

        public LB hermitian(final boolean upper) {
            myDelegate = myDelegate.hermitian(upper);
            return this.self();
        }

        public LB hessenberg(final boolean upper) {
            myDelegate = myDelegate.hessenberg(upper);
            return this.self();
        }

        public LB left(final long numberOfColumns) {
            myDelegate = myDelegate.left(numberOfColumns);
            return this.self();
        }

        public LB left(final M... left) {
            myDelegate = myDelegate.left(this.cast(left));
            return this.self();
        }

        public LB left(final M matrix) {
            myDelegate = myDelegate.left(this.cast(matrix));
            return this.self();
        }

        public LB limits(final long rowLimit, final long columnLimit) {
            myDelegate = myDelegate.limits(rowLimit, columnLimit);
            return this.self();
        }

        public LB offsets(final long rowOffset, final long columnOffset) {
            myDelegate = myDelegate.offsets(rowOffset, columnOffset);
            return this.self();
        }

        public LB repeat(final int rowsRepetitions, final int columnsRepetitions) {
            myDelegate = myDelegate.repeat(rowsRepetitions, columnsRepetitions);
            return this.self();
        }

        public LB right(final long numberOfColumns) {
            myDelegate = myDelegate.right(numberOfColumns);
            return this.self();
        }

        public LB right(final M... right) {
            myDelegate = myDelegate.right(this.cast(right));
            return this.self();
        }

        public LB right(final M matrix) {
            myDelegate = myDelegate.right(this.cast(matrix));
            return this.self();
        }

        public LB rows(final int[] rows) {
            myDelegate = myDelegate.row(rows);
            return this.self();
        }

        public LB superimpose(final long row, final long col, final M matrix) {
            myDelegate = myDelegate.superimpose(row, col, matrix.getStore());
            return this.self();
        }

        public void supplyTo(final PhysicalStore<N> receiver) {
            myDelegate.supplyTo(receiver);
        }

        public LB symmetric(final boolean upper) {
            myDelegate = myDelegate.symmetric(upper);
            return this.self();
        }

        public LB transpose() {
            myDelegate = myDelegate.transpose();
            return this.self();
        }

        public LB triangular(final boolean upper, final boolean assumeOne) {
            myDelegate = myDelegate.triangular(upper, assumeOne);
            return this.self();
        }

        public LB tridiagonal() {
            myDelegate = myDelegate.tridiagonal();
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

        abstract LB self();

    }

    abstract class Mutator<PR extends MatrixStore<N> & Mutate2D.ModifiableReceiver<N>> implements Mutate2D.ModifiableReceiver<N>, Supplier<M> {

        private final PR myDelegate;
        private boolean mySafe = true;

        Mutator(final PR delegate) {

            super();

            myDelegate = delegate;
        }

        public void accept(final Access2D<?> supplied) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.accept(supplied);
        }

        public void add(final long index, final Comparable<?> addend) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.add(index, addend);
        }

        public void add(final long index, final double addend) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.add(index, addend);
        }

        public void add(final long row, final long col, final Comparable<?> value) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.add(row, col, value);
        }

        public void add(final long row, final long col, final double value) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.add(row, col, value);
        }

        /**
         * @deprecated v49 Just use {@link #get()} instead
         */
        @Deprecated
        public M build() {
            return this.get();
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

        public double doubleValue(final long row, final long col) {
            if (mySafe) {
                return myDelegate.doubleValue(row, col);
            }
            throw new IllegalStateException();
        }

        public void exchangeColumns(final long colA, final long colB) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.exchangeColumns(colA, colB);
        }

        public void exchangeRows(final long rowA, final long rowB) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.exchangeRows(rowA, rowB);
        }

        public void fillAll(final N value) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillAll(myDelegate.physical().scalar().cast(value));
        }

        public void fillAll(final NullaryFunction<?> supplier) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillAll(supplier);
        }

        public void fillColumn(final long col, final Access1D<N> values) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillColumn(col, values);
        }

        public void fillColumn(final long row, final long col, final Access1D<N> values) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillColumn(row, col, values);
        }

        public void fillColumn(final long row, final long column, final N value) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillColumn((int) row, (int) column, myDelegate.physical().scalar().cast(value));
        }

        public void fillColumn(final long row, final long col, final NullaryFunction<?> supplier) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillColumn(row, col, supplier);
        }

        public void fillColumn(final long col, final N value) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillColumn(col, value);
        }

        public void fillColumn(final long col, final NullaryFunction<?> supplier) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillColumn(col, supplier);
        }

        public void fillDiagonal(final Access1D<N> values) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillDiagonal(values);
        }

        public void fillDiagonal(final long row, final long col, final Access1D<N> values) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillDiagonal(row, col, values);
        }

        public void fillDiagonal(final long row, final long column, final N value) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillDiagonal(row, column, myDelegate.physical().scalar().cast(value));
        }

        public void fillDiagonal(final long row, final long col, final NullaryFunction<?> supplier) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillDiagonal(row, col, supplier);
        }

        public void fillDiagonal(final N value) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillDiagonal(value);
        }

        public void fillDiagonal(final NullaryFunction<?> supplier) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillDiagonal(supplier);
        }

        public void fillMatching(final Access1D<?> values) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillMatching(values);
        }

        public void fillMatching(final Access1D<N> left, final BinaryFunction<N> function, final Access1D<N> right) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillMatching(left, function, right);
        }

        public void fillMatching(final UnaryFunction<N> function, final Access1D<N> arguments) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillMatching(function, arguments);
        }

        public void fillOne(final long index, final Access1D<?> values, final long valueIndex) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillOne(index, values, valueIndex);
        }

        public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillOne(row, col, values, valueIndex);
        }

        public void fillOne(final long row, final long col, final N value) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillOne(row, col, value);
        }

        public void fillOne(final long row, final long col, final NullaryFunction<?> supplier) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillOne(row, col, supplier);
        }

        public void fillOne(final long index, final N value) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillOne(index, value);
        }

        public void fillOne(final long index, final NullaryFunction<?> supplier) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillOne(index, supplier);
        }

        public void fillRange(final long first, final long limit, final N value) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillRange(first, limit, value);
        }

        public void fillRange(final long first, final long limit, final NullaryFunction<?> supplier) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillRange(first, limit, supplier);
        }

        public void fillRow(final long row, final Access1D<N> values) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillRow(row, values);
        }

        public void fillRow(final long row, final long col, final Access1D<N> values) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillRow(row, col, values);
        }

        public void fillRow(final long row, final long column, final N value) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillRow((int) row, (int) column, myDelegate.physical().scalar().cast(value));
        }

        public void fillRow(final long row, final long col, final NullaryFunction<?> supplier) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillRow(row, col, supplier);
        }

        public void fillRow(final long row, final N value) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillRow(row, value);
        }

        public void fillRow(final long row, final NullaryFunction<?> supplier) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.fillRow(row, supplier);
        }

        @Override
        public M get() {
            mySafe = false;
            return MatrixFactory.this.instantiate(myDelegate);
        }

        public N get(final long row, final long col) {
            if (mySafe) {
                return myDelegate.get(row, col);
            }
            throw new IllegalStateException();
        }

        public void modifyAll(final UnaryFunction<N> modifier) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.modifyAll(modifier);
        }

        public void modifyAny(final Transformation2D<N> modifier) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            modifier.transform(myDelegate);
        }

        public void modifyColumn(final long row, final long col, final UnaryFunction<N> modifier) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.modifyColumn(row, col, modifier);
        }

        public void modifyColumn(final long col, final UnaryFunction<N> modifier) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.modifyColumn(col, modifier);
        }

        public void modifyDiagonal(final long row, final long col, final UnaryFunction<N> modifier) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.modifyDiagonal(row, col, modifier);
        }

        public void modifyDiagonal(final UnaryFunction<N> modifier) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.modifyDiagonal(modifier);
        }

        public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.modifyMatching(left, function);
        }

        public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.modifyMatching(function, right);
        }

        public void modifyMatchingInColumns(final Access1D<N> left, final BinaryFunction<N> function) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.modifyMatchingInColumns(left, function);
        }

        public void modifyMatchingInColumns(final BinaryFunction<N> function, final Access1D<N> right) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.modifyMatchingInColumns(function, right);
        }

        public void modifyMatchingInRows(final Access1D<N> left, final BinaryFunction<N> function) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.modifyMatchingInRows(left, function);
        }

        public void modifyMatchingInRows(final BinaryFunction<N> function, final Access1D<N> right) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.modifyMatchingInRows(function, right);
        }

        public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.modifyOne(row, col, modifier);
        }

        public void modifyOne(final long index, final UnaryFunction<N> modifier) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.modifyOne(index, modifier);
        }

        public void modifyRange(final long first, final long limit, final UnaryFunction<N> modifier) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.modifyRange(first, limit, modifier);
        }

        public void modifyRow(final long row, final long col, final UnaryFunction<N> modifier) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.modifyRow(row, col, modifier);
        }

        public void modifyRow(final long row, final UnaryFunction<N> modifier) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.modifyRow(row, modifier);
        }

        public void reset() {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.reset();
        }

        public void set(final long index, final Comparable<?> value) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.set(index, myDelegate.physical().scalar().cast(value));
        }

        public void set(final long index, final double value) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.set(index, value);
        }

        public void set(final long row, final long col, final Comparable<?> value) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.set(row, col, value);
        }

        public void set(final long row, final long col, final double value) {
            if (!mySafe) {
                throw new IllegalStateException();
            }
            myDelegate.set(row, col, value);
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

    MatrixFactory(final Class<M> template, final PhysicalStore.Factory<N, ?> factory) {

        super();

        myPhysicalFactory = factory;
        myConstructor = (Constructor<M>) MatrixFactory.getConstructor(template);
    }

    public M columns(final Access1D<?>... source) {
        return this.instantiate(myPhysicalFactory.columns(source));
    }

    public M columns(final Comparable<?>[]... source) {
        return this.instantiate(myPhysicalFactory.columns(source));
    }

    public M columns(final double[]... source) {
        return this.instantiate(myPhysicalFactory.columns(source));
    }

    public M columns(final List<? extends Comparable<?>>... source) {
        return this.instantiate(myPhysicalFactory.columns(source));
    }

    public M copy(final Access2D<?> source) {
        return this.instantiate(myPhysicalFactory.copy(source));
    }

    @Override
    public FunctionSet<N> function() {
        return myPhysicalFactory.function();
    }

    public M make(final long rows, final long columns) {
        return this.instantiate(myPhysicalFactory.builder().makeZero((int) rows, (int) columns).get());
    }

    public DR makeDense(final int count) {
        return this.makeDense(count, 1);
    }

    public DR makeDense(final long rows, final long columns) {
        return this.physical(myPhysicalFactory.make(rows, columns));
    }

    public M makeEye(final int rows, final int columns) {

        final int square = Math.min(rows, columns);

        MatrixStore<N> retVal = myPhysicalFactory.builder().makeIdentity(square);

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

    public SR makeSparse(final long rows, final long columns) {
        return this.physical(myPhysicalFactory.builder().makeSparse(rows, columns));
    }

    public SR makeSparse(final Structure2D shape) {
        return this.makeSparse(Math.toIntExact(shape.countRows()), Math.toIntExact(shape.countColumns()));
    }

    public M makeWrapper(final Access2D<?> elements) {
        return this.instantiate(myPhysicalFactory.builder().makeWrapper(elements).get());
    }

    public M rows(final Access1D<?>... source) {
        return this.instantiate(myPhysicalFactory.rows(source));
    }

    public M rows(final Comparable<?>[]... source) {
        return this.instantiate(myPhysicalFactory.rows(source));
    }

    public M rows(final double[]... source) {
        return this.instantiate(myPhysicalFactory.rows(source));
    }

    public M rows(final List<? extends Comparable<?>>... source) {
        return this.instantiate(myPhysicalFactory.rows(source));
    }

    @Override
    public Scalar.Factory<N> scalar() {
        return myPhysicalFactory.scalar();
    }

    public TensorFactory1D<N, DR> tensor1D() {
        return TensorFactory1D.of(new Factory1D<DR>() {

            public FunctionSet<N> function() {
                return MatrixFactory.this.function();
            }

            public DR make(final long count) {
                return MatrixFactory.this.makeDense(count, 1L);
            }

            public Factory<N> scalar() {
                return MatrixFactory.this.scalar();
            }

        });
    }

    public TensorFactory2D<N, DR> tensor2D() {
        return TensorFactory2D.of(new Factory2D<DR>() {

            public FunctionSet<N> function() {
                return MatrixFactory.this.function();
            }

            public DR make(final long rows, final long columns) {
                return MatrixFactory.this.makeDense(rows, columns);
            }

            public Factory<N> scalar() {
                return MatrixFactory.this.scalar();
            }

        });
    }

    /**
     * This method is for internal use only - YOU should NOT use it!
     */
    M instantiate(final MatrixStore<N> store) {
        try {
            return myConstructor.newInstance(store);
        } catch (final IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException anException) {
            throw new ProgrammingError(anException);
        }
    }

    abstract LB logical(final MatrixStore<N> delegate);

    abstract DR physical(final PhysicalStore<N> delegate);

    abstract SR physical(final SparseStore<N> delegate);

    final PhysicalStore.Factory<N, ?> getPhysicalFactory() {
        return myPhysicalFactory;
    }

}
