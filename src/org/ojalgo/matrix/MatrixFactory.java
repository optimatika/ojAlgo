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
package org.ojalgo.matrix;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.Access2D.Builder;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;

/**
 * MatrixFactory creates instances of classes that implement the {@linkplain org.ojalgo.matrix.BasicMatrix}
 * interface and have a constructor that takes a MatrixStore as input.
 *
 * @author apete
 */
final class MatrixFactory<N extends Number, I extends BasicMatrix> implements BasicMatrix.Factory<I> {

    final class MatrixBuilder implements Access2D.Builder<I> {

        private final PhysicalStore.Factory<N, ?> myFactory;
        private final PhysicalStore<N> myPhysicalStore;
        private boolean mySafe = true;

        @SuppressWarnings("unused")
        private MatrixBuilder() {
            this(null, 0, 0);
        }

        protected MatrixBuilder(final PhysicalStore.Factory<N, ?> aFactory, final int aRowDim, final int aColDim) {

            super();

            myPhysicalStore = aFactory.makeZero(aRowDim, aColDim);
            myFactory = aFactory;
        }

        MatrixBuilder(final PhysicalStore<N> aPhysicalStore) {

            super();

            myPhysicalStore = aPhysicalStore;
            myFactory = aPhysicalStore.factory();
        }

        @Override
        public I build() {
            mySafe = false;
            return MatrixFactory.this.instantiate(myPhysicalStore);
        }

        public long count() {
            return myPhysicalStore.count();
        }

        public long countColumns() {
            return myPhysicalStore.countColumns();
        }

        public long countRows() {
            return myPhysicalStore.countRows();
        }

        public final MatrixBuilder fillAll(final Number value) {
            if (mySafe) {
                myPhysicalStore.fillAll(myFactory.scalar().cast(value));
            } else {
                throw new IllegalStateException();
            }
            return this;
        }

        public final MatrixBuilder fillColumn(final long row, final long column, final Number value) {
            if (mySafe) {
                myPhysicalStore.fillColumn((int) row, (int) column, myFactory.scalar().cast(value));
            } else {
                throw new IllegalStateException();
            }
            return this;
        }

        public final MatrixBuilder fillDiagonal(final long row, final long column, final Number value) {
            if (mySafe) {
                myPhysicalStore.fillDiagonal((int) row, (int) column, myFactory.scalar().cast(value));
            } else {
                throw new IllegalStateException();
            }
            return this;
        }

        public final MatrixBuilder fillRow(final long row, final long column, final Number value) {
            if (mySafe) {
                myPhysicalStore.fillRow((int) row, (int) column, myFactory.scalar().cast(value));
            } else {
                throw new IllegalStateException();
            }
            return this;
        }

        public final MatrixBuilder set(final long index, final double value) {
            if (mySafe) {
                myPhysicalStore.set(index, value);
            } else {
                throw new IllegalStateException();
            }
            return this;
        }

        public final MatrixBuilder set(final long row, final long column, final double value) {
            if (mySafe) {
                myPhysicalStore.set(row, column, value);
            } else {
                throw new IllegalStateException();
            }
            return this;
        }

        public final MatrixBuilder set(final long row, final long column, final Number value) {
            if (mySafe) {
                myPhysicalStore.set(row, column, myFactory.scalar().cast(value));
            } else {
                throw new IllegalStateException();
            }
            return this;
        }

        public final MatrixBuilder set(final long index, final Number value) {
            if (mySafe) {
                myPhysicalStore.set(index, myFactory.scalar().cast(value));
            } else {
                throw new IllegalStateException();
            }
            return this;
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

    @SuppressWarnings("unused")
    private MatrixFactory() {

        this(null, null);

        ProgrammingError.throwForIllegalInvocation();
    }

    MatrixFactory(final Class<I> aTemplate, final PhysicalStore.Factory<N, ?> aPhysical) {

        super();

        myPhysicalFactory = aPhysical;
        myConstructor = (Constructor<I>) MatrixFactory.getConstructor(aTemplate);
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

    public Builder<I> getBuilder(final int count) {
        return this.getBuilder(count, 1);
    }

    public Builder<I> getBuilder(final int rows, final int columns) {
        return new MatrixBuilder(myPhysicalFactory, rows, columns);
    }

    public I makeEye(final long rows, final long columns) {

        final int tmpMinDim = (int) Math.min(rows, columns);

        MatrixStore.Builder<N> retVal = myPhysicalFactory.builder().makeIdentity(tmpMinDim);

        if (rows > tmpMinDim) {
            //retVal = new AboveBelowStore<N>(retVal, new ZeroStore<N>(myPhysicalFactory, , (int) columns));
            retVal = retVal.below((int) rows - tmpMinDim);
        } else if (columns > tmpMinDim) {
            //retVal = new LeftRightStore<N>(retVal, new ZeroStore<N>(myPhysicalFactory, (int) rows, (int) columns - tmpMinDim));
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

    protected final PhysicalStore.Factory<N, ?> getPhysicalFactory() {
        return myPhysicalFactory;
    }

    /**
     * This method is for internal use only - YOU should NOT use it!
     */
    final I instantiate(final MatrixStore<N> aStore) {
        try {
            return myConstructor.newInstance(aStore);
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

    final MatrixBuilder wrap(final PhysicalStore<N> aStore) {
        return new MatrixBuilder(aStore);
    }

}
