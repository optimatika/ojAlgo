/*
 * Copyright 1997-2026 Optimatika
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

import org.ojalgo.ProgrammingError;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access2D;
import org.ojalgo.type.NumberDefinition;

/**
 * Holds the decomposed matrix, in place, in a single physical store.
 *
 * @param <N> the number type
 * @param <M> the type of physical store the decomposition creates, including the store that holds the
 *            decomposed matrix
 */
abstract class InPlaceDecomposition<N extends Comparable<N>, M extends PhysicalStore<N>> extends AbstractDecomposition<N, M> {

    /**
     * Copies a reflector, stored as a column or row of the decomposed matrix, to {@code worker}, with the
     * given beta.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static <N extends Comparable<N>> Householder<N> copyReflector(final Householder<N> worker, final Householder<N> source, final double beta,
            final PhysicalStore.Factory<N, ?> factory) {
        if (worker instanceof Householder.Generic) {
            ((Householder.Generic) worker).copy(source, (Scalar) factory.scalar().cast(beta));
        } else {
            ((Householder.Primitive64) worker).copy((Householder<Double>) source, beta);
        }
        return worker;
    }

    /**
     * The beta of a reflector, as a double.
     */
    static double getBeta(final Householder<?> householder) {
        if (householder instanceof Householder.Generic<?>) {
            return NumberDefinition.doubleValue(((Householder.Generic<?>) householder).beta);
        } else {
            return ((Householder.Primitive64) householder).beta;
        }
    }

    private int myColDim;
    private M myInPlace;
    private int myRowDim;

    InPlaceDecomposition(final PhysicalStore.Factory<N, ? extends M> factory) {
        super(factory);
    }

    @Override
    public int getColDim() {
        return myColDim;
    }

    public MatrixStore<N> getInverse() {
        return this.getInverse(this.makeZero(this.getRowDim(), this.getRowDim()));
    }

    public MatrixStore<N> getInverse(final PhysicalStore<N> preallocated) {
        ProgrammingError.throwForUnsupportedOptionalOperation();
        return null;
    }

    @Override
    public int getRowDim() {
        return myRowDim;
    }

    final M getInPlace() {
        return myInPlace;
    }

    final M setInPlace(final Access2D.Collectable<N, ? super M> matrix) {

        int tmpRowDim = matrix.getRowDim();
        int tmpColDim = matrix.getColDim();

        if (myInPlace != null && myRowDim == tmpRowDim && myColDim == tmpColDim) {

        } else {

            myInPlace = this.makeZero(tmpRowDim, tmpColDim);

            myRowDim = tmpRowDim;
            myColDim = tmpColDim;
        }

        matrix.supplyTo(myInPlace);

        this.computed(false);

        return myInPlace;
    }

}
