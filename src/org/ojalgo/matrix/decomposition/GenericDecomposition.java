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
import org.ojalgo.type.context.NumberContext;

/**
 * AbstractDecomposition
 *
 * @author apete
 */
abstract class GenericDecomposition<N extends Number> extends AbstractDecomposition<N> {

    private final PhysicalStore.Factory<N, ? extends DecompositionStore<N>> myFactory;

    @SuppressWarnings("unused")
    private GenericDecomposition() {
        this(null);
    }

    protected GenericDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> factory) {

        super();

        myFactory = factory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object someObj) {
        if (someObj instanceof MatrixStore) {
            return this.equals((MatrixStore<N>) someObj, NumberContext.getGeneral(6));
        } else {
            return super.equals(someObj);
        }
    }

    protected final AggregatorSet<N> aggregator() {
        return myFactory.aggregator();
    }

    protected final DecompositionStore<N> copy(final Access2D<?> source) {
        return myFactory.copy(source);
    }

    protected final FunctionSet<N> function() {
        return myFactory.function();
    }

    protected final BasicArray<N> makeArray(final int length) {
        return myFactory.makeArray(length);
    }

    protected final DecompositionStore<N> makeEye(final int numberOfRows, final int numberOfColumns) {
        return myFactory.makeEye(numberOfRows, numberOfColumns);
    }

    protected final Householder<N> makeHouseholder(final int dimension) {
        return myFactory.makeHouseholder(dimension);
    }

    protected final MatrixStore.Builder<N> makeIdentity(final int dimension) {
        return myFactory.builder().makeIdentity(dimension);
    }

    protected final Rotation<N> makeRotation(final int aLow, final int aHigh, final double aCos, final double aSin) {
        return myFactory.makeRotation(aLow, aHigh, aCos, aSin);
    }

    protected final Rotation<N> makeRotation(final int aLow, final int aHigh, final N aCos, final N aSin) {
        return myFactory.makeRotation(aLow, aHigh, aCos, aSin);
    }

    protected final DecompositionStore<N> makeZero(final int numberOfRows, final int numberOfColumns) {
        return myFactory.makeZero(numberOfRows, numberOfColumns);
    }

    @Override
    protected final DecompositionStore<N> preallocate(final long numberOfRows, final long numberOfColumns) {
        return myFactory.makeZero(numberOfRows, numberOfColumns);
    }

    protected final Scalar.Factory<N> scalar() {
        return myFactory.scalar();
    }

    protected final MatrixStore.Builder<N> wrap(final Access2D<?> source) {
        return myFactory.builder().makeWrapper(source);
    }

}
