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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.access.Access2D;
import org.ojalgo.array.BasicArray;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.Scalar;

/**
 * AbstractDecomposition
 *
 * @author apete
 */
abstract class GenericDecomposition<N extends Number> extends AbstractDecomposition<N> {

    private final PhysicalStore.Factory<N, ? extends DecompositionStore<N>> myFactory;
    private final MatrixStore.Factory<N> myBuilder;

    protected GenericDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> factory) {

        super();

        myFactory = factory;
        myBuilder = myFactory.builder();
    }

    protected final AggregatorSet<N> aggregator() {
        return myFactory.aggregator();
    }

    @Override
    protected final DecompositionStore<N> allocate(final long numberOfRows, final long numberOfColumns) {
        return myFactory.makeZero(numberOfRows, numberOfColumns);
    }

    @SuppressWarnings("unchecked")
    protected final MatrixStore<N> collect(final Access2D.Collectable<N, ? super DecompositionStore<N>> source) {
        if (source instanceof MatrixStore) {
            return (MatrixStore<N>) source;
        } else if (source instanceof Access2D) {
            return myBuilder.makeWrapper((Access2D<?>) source).get();
        } else {
            return source.collect(myFactory);
        }
    }

    protected final DecompositionStore<N> copy(final Access2D<?> source) {
        return myFactory.copy(source);
    }

    protected final FunctionSet<N> function() {
        return myFactory.function();
    }

    protected final BasicArray<N> makeArray(final int length) {
        return myFactory.array().makeZero(length);
    }

    protected final DecompositionStore<N> makeEye(final int numberOfRows, final int numberOfColumns) {
        return myFactory.makeEye(numberOfRows, numberOfColumns);
    }

    protected final Householder<N> makeHouseholder(final int dimension) {
        return myFactory.makeHouseholder(dimension);
    }

    protected final MatrixStore.LogicalBuilder<N> makeIdentity(final int dimension) {
        return myBuilder.makeIdentity(dimension);
    }

    protected final Rotation<N> makeRotation(final int low, final int high, final double cos, final double sin) {
        return myFactory.makeRotation(low, high, cos, sin);
    }

    protected final Rotation<N> makeRotation(final int low, final int high, final N cos, final N sin) {
        return myFactory.makeRotation(low, high, cos, sin);
    }

    protected final DecompositionStore<N> makeZero(final int numberOfRows, final int numberOfColumns) {
        return myFactory.makeZero(numberOfRows, numberOfColumns);
    }

    protected final Scalar.Factory<N> scalar() {
        return myFactory.scalar();
    }

    protected final MatrixStore.LogicalBuilder<N> wrap(final Access2D<?> source) {
        return myBuilder.makeWrapper(source);
    }

}
