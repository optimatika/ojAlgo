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
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.matrix.BasicMatrix.Builder;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.Scalar;

/**
 * MatrixFactory creates instances of classes that implement the {@linkplain org.ojalgo.matrix.BasicMatrix}
 * interface and have a constructor that takes a MatrixStore as input.
 *
 * @author apete
 */
final class MatrixFactory<N extends Number, I extends BasicMatrix> implements BasicMatrix.Factory<I> {

    final class MatrixBuilder implements Builder<I> {

        private final PhysicalStore<N> myStore;
        private boolean mySafe = true;

        protected MatrixBuilder(final PhysicalStore.Factory<N, ?> factory, final int rowDim, final int colDim) {

            super();

            myStore = factory.makeZero(rowDim, colDim);
        }

        MatrixBuilder(final PhysicalStore<N> physicalStore) {

            super();

            myStore = physicalStore;
        }

        public final void add(final long row, final long col, final double value) {
            if (mySafe) {
                myStore.add(row, col, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public final void add(final long row, final long col, final Number value) {
            if (mySafe) {
                myStore.add(row, col, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public final long count() {
            return myStore.count();
        }

        public final long countColumns() {
            return myStore.countColumns();
        }

        public final long countRows() {
            return myStore.countRows();
        }

        public final MatrixBuilder fillAll(final Number value) {
            if (mySafe) {
                myStore.fillAll(myStore.physical().scalar().cast(value));
            } else {
                throw new IllegalStateException();
            }
            return this;
        }

        public final MatrixBuilder fillColumn(final long row, final long column, final Number value) {
            if (mySafe) {
                myStore.fillColumn((int) row, (int) column, myStore.physical().scalar().cast(value));
            } else {
                throw new IllegalStateException();
            }
            return this;
        }

        public final MatrixBuilder fillDiagonal(final long row, final long column, final Number value) {
            if (mySafe) {
                myStore.fillDiagonal((int) row, (int) column, myStore.physical().scalar().cast(value));
            } else {
                throw new IllegalStateException();
            }
            return this;
        }

        public final MatrixBuilder fillRow(final long row, final long column, final Number value) {
            if (mySafe) {
                myStore.fillRow((int) row, (int) column, myStore.physical().scalar().cast(value));
            } else {
                throw new IllegalStateException();
            }
            return this;
        }

        @Override
        public final I get() {
            mySafe = false;
            return MatrixFactory.this.instantiate(myStore);
        }

        public final void set(final long index, final double value) {
            if (mySafe) {
                myStore.set(index, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public final void set(final long row, final long col, final double value) {
            if (mySafe) {
                myStore.set(row, col, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public final void set(final long row, final long col, final Number value) {
            if (mySafe) {
                myStore.set(row, col, value);
            } else {
                throw new IllegalStateException();
            }
        }

        public final void set(final long index, final Number value) {
            if (mySafe) {
                myStore.set(index, myStore.physical().scalar().cast(value));
            } else {
                throw new IllegalStateException();
            }
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
    public final FunctionSet<N> function() {
        return myPhysicalFactory.function();
    }

    public Builder<I> getBuilder(final int count) {
        return this.getBuilder(count, 1);
    }

    public Builder<I> getBuilder(final int rows, final int columns) {
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
    public final Scalar.Factory<N> scalar() {
        return myPhysicalFactory.scalar();
    }

    /**
     * This method is for internal use only - YOU should NOT use it!
     */
    final I instantiate(final MatrixStore<N> store) {
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

    final MatrixBuilder wrap(final PhysicalStore<N> store) {
        return new MatrixBuilder(store);
    }

}
