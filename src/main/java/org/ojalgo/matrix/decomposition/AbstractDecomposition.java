/*
 * Copyright 1997-2025 Optimatika
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

import static org.ojalgo.function.constant.PrimitiveMath.MACHINE_EPSILON;

import org.ojalgo.array.BasicArray;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.matrix.store.DiagonalStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;

/**
 * @author apete
 */
abstract class AbstractDecomposition<N extends Comparable<N>, M extends PhysicalStore<N>> implements MatrixDecomposition<N> {

    private boolean myComputed = false;
    private final PhysicalStore.Factory<N, ? extends M> myFactory;
    private Boolean mySolvable = null;

    AbstractDecomposition(final PhysicalStore.Factory<N, ? extends M> factory) {
        super();
        myFactory = factory;
    }

    @Override
    public final boolean isComputed() {
        return myComputed;
    }

    @Override
    public void reset() {
        myComputed = false;
        mySolvable = null;
    }

    protected boolean checkSolvability() {
        return false;
    }

    protected boolean isSolvable() {
        if (myComputed && mySolvable == null) {
            if (this instanceof MatrixDecomposition.Solver) {
                mySolvable = Boolean.valueOf(this.checkSolvability());
            } else {
                mySolvable = Boolean.FALSE;
            }
        }
        return myComputed && mySolvable != null && mySolvable.booleanValue();
    }

    final AggregatorSet<N> aggregator() {
        return myFactory.aggregator();
    }

    final void applyPivotOrder(final Pivot pivot, final PhysicalStore<N> matrix) {

        if (pivot.isModified()) {
            if (matrix.getColDim() == 1) {
                pivot.applyPivotOrder(matrix);
            } else {
                matrix.copy().rows(pivot.getOrder()).supplyTo(matrix);
            }
        }
    }

    final void applyReverseOrder(final Pivot pivot, final PhysicalStore<N> matrix) {

        if (pivot.isModified()) {
            if (matrix.getColDim() == 1) {
                pivot.applyReverseOrder(matrix);
            } else {
                matrix.copy().rows(pivot.reverseOrder()).supplyTo(matrix);
            }
        }
    }

    final MatrixStore<N> collect(final Access2D.Collectable<N, ? super M> source) {
        if (source instanceof MatrixStore) {
            return (MatrixStore<N>) source;
        }
        if (source instanceof Access2D) {
            return myFactory.makeWrapper((Access2D<?>) source);
        }
        return source.collect(myFactory);
    }

    final boolean computed(final boolean computed) {
        return myComputed = computed;
    }

    final FunctionSet<N> function() {
        return myFactory.function();
    }

    final double getDimensionalEpsilon() {
        return this.getMaxDim() * MACHINE_EPSILON;
    }

    final boolean isAspectRatioNormal() {
        return this.getRowDim() >= this.getColDim();
    }

    final BasicArray<N> makeArray(final int length) {
        return myFactory.array().make(length);
    }

    final <D extends Access1D<?>> DiagonalStore.Builder<N, D> makeDiagonal(final D mainDiag) {
        return DiagonalStore.builder(myFactory, mainDiag);
    }

    final M makeEye(final int numberOfRows, final int numberOfColumns) {
        return myFactory.makeEye(numberOfRows, numberOfColumns);
    }

    final Householder<N> makeHouseholder(final int dimension) {
        return myFactory.makeHouseholder(dimension);
    }

    final MatrixStore<N> makeIdentity(final int dimension) {
        return myFactory.makeIdentity(dimension);
    }

    final Rotation<N> makeRotation(final int low, final int high, final double cos, final double sin) {
        return myFactory.makeRotation(low, high, cos, sin);
    }

    final Rotation<N> makeRotation(final int low, final int high, final N cos, final N sin) {
        return myFactory.makeRotation(low, high, cos, sin);
    }

    final M makeZero(final int nbRows, final int nbCols) {
        return myFactory.make(nbRows, nbCols);
    }

    final M makeZero(final Structure2D shape) {
        return myFactory.make(shape);
    }

    final Scalar.Factory<N> scalar() {
        return myFactory.scalar();
    }

    final MatrixStore<N> wrap(final Access2D<?> source) {
        return myFactory.makeWrapper(source);
    }

}
