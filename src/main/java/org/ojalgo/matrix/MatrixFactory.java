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
package org.ojalgo.matrix;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.matrix.store.ElementsSupplier;
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
import org.ojalgo.tensor.TensorFactory1D;
import org.ojalgo.tensor.TensorFactory2D;
import org.ojalgo.type.math.MathType;

/**
 * MatrixFactory creates instances of classes that implement the {@linkplain org.ojalgo.matrix.BasicMatrix}
 * interface and have a constructor that takes a MatrixStore as input.
 *
 * @author apete
 */
public abstract class MatrixFactory<N extends Comparable<N>, M extends BasicMatrix<N, M>, DR extends Mutate2D.ModifiableReceiver<N> & Factory2D.Builder<M>, SR extends Factory2D.Builder<M>>
        implements Factory2D.MayBeSparse<M, DR, SR> {

    private static Constructor<? extends BasicMatrix<?, ?>> getConstructor(final Class<? extends BasicMatrix<?, ?>> template) {
        try {
            Constructor<? extends BasicMatrix<?, ?>> retVal = template.getDeclaredConstructor(ElementsSupplier.class);
            retVal.setAccessible(true);
            return retVal;
        } catch (SecurityException | NoSuchMethodException cause) {
            throw new ProgrammingError(cause);
        }
    }

    private final Constructor<M> myConstructor;
    private final PhysicalStore.Factory<N, ?> myPhysicalFactory;

    MatrixFactory(final Class<M> template, final PhysicalStore.Factory<N, ?> factory) {

        super();

        myPhysicalFactory = factory;
        myConstructor = (Constructor<M>) MatrixFactory.getConstructor(template);
    }

    @Override
    public M copy(final Access2D<?> source) {
        return this.instantiate(myPhysicalFactory.copy(source));
    }

    @Override
    public FunctionSet<N> function() {
        return myPhysicalFactory.function();
    }

    @Override
    public MathType getMathType() {
        return myPhysicalFactory.getMathType();
    }

    @Override
    public M make(final int nbRows, final int nbCols) {
        return this.instantiate(myPhysicalFactory.makeZero(nbRows, nbCols));
    }

    @Override
    public M make(final long nbRows, final long nbCols) {
        return this.instantiate(myPhysicalFactory.makeZero(nbRows, nbCols));
    }

    public DR makeDense(final int count) {
        return this.newDenseBuilder(count, 1);
    }

    public M makeDiagonal(final Access1D<?> diagonal) {
        return this.instantiate(myPhysicalFactory.makeDiagonal(diagonal).get());
    }

    public M makeDiagonal(final double... diagonal) {
        return this.makeDiagonal(ArrayR064.wrap(diagonal));
    }

    public M makeEye(final int rows, final int columns) {

        final int square = Math.min(rows, columns);

        MatrixStore<N> retVal = myPhysicalFactory.makeIdentity(square);

        if (rows > square) {
            retVal = retVal.below(rows - square);
        } else if (columns > square) {
            retVal = retVal.right(columns - square);
        }

        return this.instantiate(retVal);
    }

    public M makeEye(final Structure2D shape) {
        return this.makeEye(Math.toIntExact(shape.countRows()), Math.toIntExact(shape.countColumns()));
    }

    @Override
    public M makeFilled(final long rows, final long columns, final NullaryFunction<?> supplier) {
        return this.instantiate(myPhysicalFactory.makeFilled(rows, columns, supplier));
    }

    public M makeIdentity(final int dimension) {
        return this.instantiate(myPhysicalFactory.makeIdentity(dimension));
    }

    public M makeSingle(final N element) {
        return this.instantiate(myPhysicalFactory.makeSingle(element));
    }

    public M makeWrapper(final Access2D<?> elements) {
        return this.instantiate(myPhysicalFactory.makeWrapper(elements));
    }

    @Override
    public DR newDenseBuilder(final long nbRows, final long nbCols) {
        return this.dense(myPhysicalFactory.make(nbRows, nbCols));
    }

    @Override
    public SR newSparseBuilder(final long nbRows, final long nbCols) {
        return this.sparse(SparseStore.factory(myPhysicalFactory).newBuilder(nbRows, nbCols));
    }

    @Override
    public Scalar.Factory<N> scalar() {
        return myPhysicalFactory.scalar();
    }

    public TensorFactory1D<N, DR> tensor1D() {
        return TensorFactory1D.of(new Factory1D<DR>() {

            @Override
            public FunctionSet<N> function() {
                return MatrixFactory.this.function();
            }

            @Override
            public MathType getMathType() {
                return MatrixFactory.this.getMathType();
            }

            @Override
            public DR make(final int size) {
                return MatrixFactory.this.newDenseBuilder(size, 1L);
            }

            @Override
            public DR make(final long count) {
                return MatrixFactory.this.newDenseBuilder(count, 1L);
            }

            @Override
            public Factory<N> scalar() {
                return MatrixFactory.this.scalar();
            }

        });
    }

    public TensorFactory2D<N, DR> tensor2D() {
        return TensorFactory2D.of(new Factory2D<DR>() {

            @Override
            public FunctionSet<N> function() {
                return MatrixFactory.this.function();
            }

            @Override
            public MathType getMathType() {
                return MatrixFactory.this.getMathType();
            }

            @Override
            public DR make(final int nbRows, final int nbCols) {
                return MatrixFactory.this.newDenseBuilder(nbRows, nbCols);
            }

            @Override
            public DR make(final long nbRows, final long nbCols) {
                return MatrixFactory.this.newDenseBuilder(nbRows, nbCols);
            }

            @Override
            public Factory<N> scalar() {
                return MatrixFactory.this.scalar();
            }

        });
    }

    abstract DR dense(final PhysicalStore<N> delegate);

    final PhysicalStore.Factory<N, ?> getPhysicalFactory() {
        return myPhysicalFactory;
    }

    /**
     * This method is for internal use only - YOU should NOT use it!
     */
    M instantiate(final ElementsSupplier<N> supplier) {
        try {
            return myConstructor.newInstance(supplier);
        } catch (final IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException cause) {
            throw new ProgrammingError(cause);
        }
    }

    abstract SR sparse(final SparseStore.Builder<N> delegate);

}
