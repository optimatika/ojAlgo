/*
 * Copyright 1997-2018 Optimatika
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
import org.ojalgo.matrix.BasicMatrix.PhysicalBuilder;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Factory2D;

/**
 * MatrixFactory creates instances of classes that implement the {@linkplain org.ojalgo.matrix.BasicMatrix}
 * interface and have a constructor that takes a MatrixStore as input.
 *
 * @author apete
 */
public final class MatrixFactory<N extends Number, I extends BasicMatrix> implements Factory2D<I> {

    final class MatrixBuilder implements PhysicalBuilder<N, I> {

        private boolean mySafe = true;
        private final PhysicalStore<N> myStore;

        protected MatrixBuilder(final PhysicalStore.Factory<N, ?> factory, final int rowDim, final int colDim) {

            super();

            myStore = factory.makeZero(rowDim, colDim);
        }

        MatrixBuilder(final PhysicalStore<N> physicalStore) {

            super();

            myStore = physicalStore;
        }

        public void accept(Access2D<?> supplied) {
            if (mySafe) {
                myStore.accept(supplied);
            } else {
                throw new IllegalStateException();
            }
        }

        public void add(long index, double addend) {
            if (mySafe) {
                myStore.add(index, addend);
            } else {
                throw new IllegalStateException();
            }
        }

        public void add(final long row, final long col, final double value) {
            if (mySafe) {
                myStore.add(row, col, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void add(final long row, final long col, final Number value) {
            if (mySafe) {
                myStore.add(row, col, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void add(long index, Number addend) {
            if (mySafe) {
                myStore.add(index, addend);
            } else {
                throw new IllegalStateException();
            }
        }

        public long count() {
            return myStore.count();
        }

        public long countColumns() {
            return myStore.countColumns();
        }

        public long countRows() {
            return myStore.countRows();
        }

        public void exchangeColumns(long colA, long colB) {
            if (mySafe) {
                myStore.exchangeColumns(colA, colB);
            } else {
                throw new IllegalStateException();
            }
        }

        public void exchangeRows(long rowA, long rowB) {
            if (mySafe) {
                myStore.exchangeRows(rowA, rowB);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillAll(NullaryFunction<N> supplier) {
            if (mySafe) {
                myStore.fillAll(supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillAll(final Number value) {
            if (mySafe) {
                myStore.fillAll(myStore.physical().scalar().cast(value));
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillColumn(long col, Access1D<N> values) {
            if (mySafe) {
                myStore.fillColumn(col, values);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillColumn(long row, long col, Access1D<N> values) {
            if (mySafe) {
                myStore.fillColumn(row, col, values);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillColumn(long row, long col, NullaryFunction<N> supplier) {
            if (mySafe) {
                myStore.fillColumn(row, col, supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillColumn(final long row, final long column, final Number value) {
            if (mySafe) {
                myStore.fillColumn((int) row, (int) column, myStore.physical().scalar().cast(value));
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillColumn(long col, N value) {
            if (mySafe) {
                myStore.fillColumn(col, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillColumn(long col, NullaryFunction<N> supplier) {
            if (mySafe) {
                myStore.fillColumn(col, supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillDiagonal(Access1D<N> values) {
            if (mySafe) {
                myStore.fillDiagonal(values);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillDiagonal(long row, long col, Access1D<N> values) {
            if (mySafe) {
                myStore.fillDiagonal(row, col, values);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillDiagonal(long row, long col, NullaryFunction<N> supplier) {
            if (mySafe) {
                myStore.fillDiagonal(row, col, supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillDiagonal(final long row, final long column, final Number value) {
            if (mySafe) {
                myStore.fillDiagonal((int) row, (int) column, myStore.physical().scalar().cast(value));
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillDiagonal(N value) {
            if (mySafe) {
                myStore.fillDiagonal(value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillDiagonal(NullaryFunction<N> supplier) {
            if (mySafe) {
                myStore.fillDiagonal(supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillMatching(Access1D<?> values) {
            if (mySafe) {
                myStore.fillMatching(values);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillMatching(Access1D<N> left, BinaryFunction<N> function, Access1D<N> right) {
            if (mySafe) {
                myStore.fillMatching(left, function, right);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillMatching(UnaryFunction<N> function, Access1D<N> arguments) {
            if (mySafe) {
                myStore.fillMatching(function, arguments);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillOne(long index, Access1D<?> values, long valueIndex) {
            if (mySafe) {
                myStore.fillOne(index, values, valueIndex);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillOne(long row, long col, Access1D<?> values, long valueIndex) {
            if (mySafe) {
                myStore.fillOne(row, col, values, valueIndex);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillOne(long row, long col, N value) {
            if (mySafe) {
                myStore.fillOne(row, col, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillOne(long row, long col, NullaryFunction<N> supplier) {
            if (mySafe) {
                myStore.fillOne(row, col, supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillOne(long index, N value) {
            if (mySafe) {
                myStore.fillOne(index, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillOne(long index, NullaryFunction<N> supplier) {
            if (mySafe) {
                myStore.fillOne(index, supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillRange(long first, long limit, N value) {
            if (mySafe) {
                myStore.fillRange(first, limit, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillRange(long first, long limit, NullaryFunction<N> supplier) {
            if (mySafe) {
                myStore.fillRange(first, limit, supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillRow(long row, Access1D<N> values) {
            if (mySafe) {
                myStore.fillRow(row, values);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillRow(long row, long col, Access1D<N> values) {
            if (mySafe) {
                myStore.fillRow(row, col, values);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillRow(long row, long col, NullaryFunction<N> supplier) {
            if (mySafe) {
                myStore.fillRow(row, col, supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillRow(final long row, final long column, final Number value) {
            if (mySafe) {
                myStore.fillRow((int) row, (int) column, myStore.physical().scalar().cast(value));
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillRow(long row, N value) {
            if (mySafe) {
                myStore.fillRow(row, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void fillRow(long row, NullaryFunction<N> supplier) {
            if (mySafe) {
                myStore.fillRow(row, supplier);
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public I get() {
            mySafe = false;
            return MatrixFactory.this.instantiate(myStore);
        }

        public void modifyAll(UnaryFunction<N> modifier) {
            if (mySafe) {
                myStore.modifyAll(modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyColumn(long row, long col, UnaryFunction<N> modifier) {
            if (mySafe) {
                myStore.modifyColumn(row, col, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyColumn(long col, UnaryFunction<N> modifier) {
            if (mySafe) {
                myStore.modifyColumn(col, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyDiagonal(long row, long col, UnaryFunction<N> modifier) {
            if (mySafe) {
                myStore.modifyDiagonal(row, col, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyDiagonal(UnaryFunction<N> modifier) {
            if (mySafe) {
                myStore.modifyDiagonal(modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyMatching(Access1D<N> left, BinaryFunction<N> function) {
            if (mySafe) {
                myStore.modifyMatching(left, function);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyMatching(BinaryFunction<N> function, Access1D<N> right) {
            if (mySafe) {
                myStore.modifyMatching(function, right);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyMatchingInColumns(Access1D<N> left, BinaryFunction<N> function) {
            if (mySafe) {
                myStore.modifyMatchingInColumns(left, function);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyMatchingInColumns(BinaryFunction<N> function, Access1D<N> right) {
            if (mySafe) {
                myStore.modifyMatchingInColumns(function, right);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyMatchingInRows(Access1D<N> left, BinaryFunction<N> function) {
            if (mySafe) {
                myStore.modifyMatchingInRows(left, function);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyMatchingInRows(BinaryFunction<N> function, Access1D<N> right) {
            if (mySafe) {
                myStore.modifyMatchingInRows(function, right);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyOne(long row, long col, UnaryFunction<N> modifier) {
            if (mySafe) {
                myStore.modifyOne(row, col, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyOne(long index, UnaryFunction<N> modifier) {
            if (mySafe) {
                myStore.modifyOne(index, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyRange(long first, long limit, UnaryFunction<N> modifier) {
            if (mySafe) {
                myStore.modifyRange(first, limit, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyRow(long row, long col, UnaryFunction<N> modifier) {
            if (mySafe) {
                myStore.modifyRow(row, col, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void modifyRow(long row, UnaryFunction<N> modifier) {
            if (mySafe) {
                myStore.modifyRow(row, modifier);
            } else {
                throw new IllegalStateException();
            }
        }

        public void reset() {
            if (mySafe) {
                myStore.reset();
            } else {
                throw new IllegalStateException();
            }
        }

        public void set(final long index, final double value) {
            if (mySafe) {
                myStore.set(index, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void set(final long row, final long col, final double value) {
            if (mySafe) {
                myStore.set(row, col, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void set(final long row, final long col, final Number value) {
            if (mySafe) {
                myStore.set(row, col, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public void set(final long index, final Number value) {
            if (mySafe) {
                myStore.set(index, myStore.physical().scalar().cast(value));
            } else {
                throw new IllegalStateException();
            }
        }

        public void supplyTo(PhysicalStore<N> receiver) {
            myStore.supplyTo(receiver);
        }
    }

    private static Constructor<? extends BasicMatrix> getConstructor(final Class<? extends BasicMatrix> aTemplate) {
        try {
            final Constructor<? extends BasicMatrix> retVal = aTemplate.getDeclaredConstructor(MatrixStore.class);
            retVal.setAccessible(true);
            return retVal;
        } catch (final SecurityException anException) {
            return null;
        } catch (final NoSuchMethodException anException) {
            return null;
        }
    }

    private final Constructor<I> myConstructor;
    private final PhysicalStore.Factory<N, ?> myPhysicalFactory;

    @SuppressWarnings("unchecked")
    MatrixFactory(final Class<I> template, final PhysicalStore.Factory<N, ?> factory) {

        super();

        myPhysicalFactory = factory;
        myConstructor = (Constructor<I>) MatrixFactory.getConstructor(template);
    }

    public I columns(final Access1D<?>... source) {
        return this.instantiate(myPhysicalFactory.columns(source));
    }

    public I columns(final double[]... source) {
        return this.instantiate(myPhysicalFactory.columns(source));
    }

    @SuppressWarnings("unchecked")
    public I columns(final List<? extends Number>... source) {
        return this.instantiate(myPhysicalFactory.columns(source));
    }

    public I columns(final Number[]... source) {
        return this.instantiate(myPhysicalFactory.columns(source));
    }

    public I copy(final Access2D<?> source) {
        return this.instantiate(myPhysicalFactory.copy(source));
    }

    @Override
    public FunctionSet<N> function() {
        return myPhysicalFactory.function();
    }

    public PhysicalBuilder<N, I> getBuilder(final int count) {
        return this.getBuilder(count, 1);
    }

    public PhysicalBuilder<N, I> getBuilder(final int rows, final int columns) {
        return new MatrixBuilder(myPhysicalFactory, rows, columns);
    }

    public I makeEye(final long rows, final long columns) {

        final int tmpMinDim = (int) Math.min(rows, columns);

        MatrixStore.LogicalBuilder<N> retVal = myPhysicalFactory.builder().makeIdentity(tmpMinDim);

        if (rows > tmpMinDim) {
            retVal = retVal.below((int) rows - tmpMinDim);
        } else if (columns > tmpMinDim) {
            retVal = retVal.right((int) columns - tmpMinDim);
        }

        return this.instantiate(retVal.get());
    }

    public I makeFilled(final long rows, final long columns, final NullaryFunction<?> supplier) {
        return this.instantiate(myPhysicalFactory.makeFilled(rows, columns, supplier));
    }

    public I makeZero(final long rows, final long columns) {
        //return this.instantiate(new ZeroStore<N>(myPhysicalFactory, (int) rows, (int) columns));
        return this.instantiate(myPhysicalFactory.builder().makeZero((int) rows, (int) columns).get());
    }

    public I rows(final Access1D<?>... source) {
        return this.instantiate(myPhysicalFactory.rows(source));
    }

    public I rows(final double[]... source) {
        return this.instantiate(myPhysicalFactory.rows(source));
    }

    @SuppressWarnings("unchecked")
    public I rows(final List<? extends Number>... source) {
        return this.instantiate(myPhysicalFactory.rows(source));
    }

    public I rows(final Number[]... source) {
        return this.instantiate(myPhysicalFactory.rows(source));
    }

    @Override
    public Scalar.Factory<N> scalar() {
        return myPhysicalFactory.scalar();
    }

    /**
     * This method is for internal use only - YOU should NOT use it!
     */
    I instantiate(final MatrixStore<N> store) {
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

    MatrixBuilder wrap(final PhysicalStore<N> store) {
        return new MatrixBuilder(store);
    }

}
