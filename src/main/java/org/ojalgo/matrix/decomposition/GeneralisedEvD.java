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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.array.Array1D;
import org.ojalgo.array.ArrayC128;
import org.ojalgo.array.DenseArray;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;

final class GeneralisedEvD<N extends Comparable<N>> extends EigenvalueDecomposition<N> implements Eigenvalue.Generalised<N> {

    private final Cholesky<N> myCholesky;
    private final Eigenvalue<N> myEigenvalue;
    private final PhysicalStore.Factory<N, ? extends DecompositionStore<N>> myFactory;
    private transient PhysicalStore<N> myRecovered = null;
    /**
     * C
     */
    private transient PhysicalStore<N> myReduced = null;
    private final Eigenvalue.Generalisation myType;

    GeneralisedEvD(final PhysicalStore.Factory<N, ? extends DecompositionStore<N>> factory, final Cholesky<N> cholesky, final Eigenvalue<N> eigenvalue,
            final Eigenvalue.Generalisation type) {

        super(factory);

        myFactory = factory;
        myCholesky = cholesky;
        myEigenvalue = eigenvalue;
        myType = type;
    }

    public N getDeterminant() {
        return myEigenvalue.getDeterminant();
    }

    public ComplexNumber getTrace() {
        return myEigenvalue.getTrace();
    }

    public boolean isHermitian() {
        return myEigenvalue.isHermitian();
    }

    public boolean isOrdered() {
        return myEigenvalue.isOrdered();
    }

    public boolean prepare(final Collectable<N, ? super PhysicalStore<N>> matrixB) {
        return myCholesky.decompose(matrixB);
    }

    public MatrixStore<N> reconstruct() {
        if (myReduced == null) {
            myReduced = myEigenvalue.reconstruct().copy();
        }
        return myReduced;
    }

    @Override
    public void reset() {
        super.reset();
        // myCholesky.reset();
        myEigenvalue.reset();
        myReduced = null;
        myRecovered = null;
    }

    @Override
    protected boolean doDecompose(final Collectable<N, ? super PhysicalStore<N>> matrix, final boolean valuesOnly) {

        if (myCholesky.isComputed()) {
            myReduced = this.reduce(matrix);
        } else {
            myReduced = matrix.collect(myFactory);
        }

        if (valuesOnly) {
            return myEigenvalue.computeValuesOnly(myReduced);
        } else {
            return myEigenvalue.decompose(myReduced);
        }
    }

    @Override
    protected MatrixStore<N> makeD() {
        return myEigenvalue.getD();
    }

    @Override
    protected Array1D<ComplexNumber> makeEigenvalues() {
        return myEigenvalue.getEigenvalues();
    }

    public Eigenpair getEigenpair(final int index) {

        ComplexNumber value = ComplexNumber.FACTORY.cast(this.getD().get(index, index));
        DenseArray<ComplexNumber> vector = ArrayC128.FACTORY.copy(this.getV().sliceColumn(index));

        return new Eigenpair(value, vector);
    }

    @Override
    protected MatrixStore<N> makeV() {
        MatrixStore<N> subV = myEigenvalue.getV();
        if (myCholesky.isComputed()) {
            return this.recover(subV);
        } else {
            return subV;
        }
    }

    MatrixStore<N> recover(final MatrixStore<N> reduced) {

        MatrixStore<N> mtrxL = myCholesky.getL();

        switch (myType) {

        case BA:

            if (myRecovered == null) {
                myRecovered = this.makeZero(reduced);
            }

            myRecovered.fillByMultiplying(mtrxL, reduced);

            return myRecovered;

        default:

            if (reduced instanceof PhysicalStore<?>) {
                myRecovered = (PhysicalStore<N>) reduced;
            } else if (myRecovered != null) {
                reduced.supplyTo(myRecovered);
            } else {
                myRecovered = reduced.collect(myFactory);
            }

            myRecovered.substituteBackwards(mtrxL, false, true, false);

            return myRecovered;
        }
    }

    PhysicalStore<N> reduce(final Access2D.Collectable<N, ? super PhysicalStore<N>> original) {

        MatrixStore<N> mtrxL = myCholesky.getL();

        switch (myType) {

        case A_B:

            if (myRecovered != null) {
                original.supplyTo(myRecovered);
            } else {
                myRecovered = original.collect(myFactory);
            }

            myRecovered.substituteForwards(mtrxL, false, false, false);
            myReduced = myRecovered.conjugate().copy();
            myReduced.substituteForwards(mtrxL, false, false, false);

            return myReduced;

        default:

            if (myReduced != null) {
                original.supplyTo(myReduced);
            } else {
                myReduced = original.collect(myFactory);
            }

            if (myRecovered == null) {
                myRecovered = this.makeZero(original);
            }

            myRecovered.fillByMultiplying(myReduced, mtrxL);
            myReduced.fillByMultiplying(mtrxL.conjugate(), myRecovered);

            return myReduced;
        }
    }

}
