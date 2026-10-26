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

import static org.ojalgo.function.constant.PrimitiveMath.ONE;

import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.operation.TRMM;
import org.ojalgo.matrix.operation.TRSM;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.type.SortOrder;

class GeneralisedEvD<N extends Comparable<N>, M extends PhysicalStore<N>> extends DenseEigenvalue<N, M> implements Eigenvalue.Generalised<N> {

    /**
     * With the factor L of a {@link DenseCholesky.R064} decomposition of B, C is formed and the eigenvectors
     * recovered by {@link TRSM} and {@link TRMM}. Otherwise as for the other number types.
     */
    static final class R064 extends GeneralisedEvD<Double, R064Store> {

        /**
         * C
         */
        private transient R064Store myC = null;
        /**
         * The eigenvectors, when they are not recovered in place
         */
        private transient R064Store myV = null;

        R064(final Cholesky<Double> cholesky, final Eigenvalue<Double> eigenvalue, final Eigenvalue.Generalisation type) {
            super(R064Store.FACTORY, cholesky, eigenvalue, type);
        }

        @Override
        MatrixStore<Double> recover(final MatrixStore<Double> reduced, final Cholesky<Double> cholesky, final Eigenvalue.Generalisation type) {

            R064Store factor = cholesky instanceof DenseCholesky.R064 ? ((DenseCholesky.R064) cholesky).getInPlace() : null;

            if (factor == null || factor.getRowDim() != reduced.getRowDim()) {
                return super.recover(reduced, cholesky, type);
            }

            int n = factor.getRowDim();
            int nbVectors = reduced.getColDim();

            R064Store vectors;
            if (type != Eigenvalue.Generalisation.BA && reduced instanceof R064Store) {
                vectors = (R064Store) reduced;
            } else {
                if (myV == null || myV.getRowDim() != n || myV.getColDim() != nbVectors) {
                    myV = R064Store.FACTORY.make(n, nbVectors);
                }
                reduced.supplyTo(myV);
                vectors = myV;
            }

            if (type == Eigenvalue.Generalisation.BA) {
                // V = L*V
                TRMM.invoke(true, false, false, false, n, nbVectors, ONE, factor.data, 0, Math.max(1, n), vectors.data, 0, Math.max(1, n));
            } else {
                // V = inv(L)^T*V
                TRSM.invoke(true, false, true, false, n, nbVectors, ONE, factor.data, 0, Math.max(1, n), vectors.data, 0, Math.max(1, n));
            }

            return vectors;
        }

        @Override
        PhysicalStore<Double> reduce(final Access2D.Collectable<Double, ? super PhysicalStore<Double>> original, final Cholesky<Double> cholesky,
                final Eigenvalue.Generalisation type) {

            R064Store factor = cholesky instanceof DenseCholesky.R064 ? ((DenseCholesky.R064) cholesky).getInPlace() : null;

            if (factor == null || factor.getRowDim() != original.countRows() || factor.getColDim() != original.countColumns()) {
                return super.reduce(original, cholesky, type);
            }

            int n = factor.getRowDim();

            if (myC == null || myC.getRowDim() != n || myC.getColDim() != n) {
                myC = R064Store.FACTORY.make(n, n);
            }
            original.supplyTo(myC);

            if (type == Eigenvalue.Generalisation.A_B) {
                // C = inv(L)*A*inv(L)^T
                TRSM.invoke(true, false, false, false, n, n, ONE, factor.data, 0, Math.max(1, n), myC.data, 0, Math.max(1, n));
                TRSM.invoke(false, false, true, false, n, n, ONE, factor.data, 0, Math.max(1, n), myC.data, 0, Math.max(1, n));
            } else {
                // C = L^T*A*L
                TRMM.invoke(false, false, false, false, n, n, ONE, factor.data, 0, Math.max(1, n), myC.data, 0, Math.max(1, n));
                TRMM.invoke(true, false, true, false, n, n, ONE, factor.data, 0, Math.max(1, n), myC.data, 0, Math.max(1, n));
            }

            return myC;
        }

    }

    private final Cholesky<N> myCholesky;
    private final Eigenvalue<N> myEigenvalue;
    private final PhysicalStore.Factory<N, ? extends M> myFactory;
    private transient PhysicalStore<N> myRecovered = null;
    /**
     * C
     */
    private transient PhysicalStore<N> myReduced = null;
    private final Eigenvalue.Generalisation myType;

    GeneralisedEvD(final PhysicalStore.Factory<N, ? extends M> factory, final Cholesky<N> cholesky, final Eigenvalue<N> eigenvalue,
            final Eigenvalue.Generalisation type) {

        super(factory);

        myFactory = factory;
        myCholesky = cholesky;
        myEigenvalue = eigenvalue;
        myType = type;
    }

    @Override
    public N getDeterminant() {
        return myEigenvalue.getDeterminant();
    }

    @Override
    public SortOrder getOrder() {
        return myEigenvalue.getOrder();
    }

    @Override
    public ComplexNumber getTrace() {
        return myEigenvalue.getTrace();
    }

    @Override
    public boolean isHermitian() {
        return myEigenvalue.isHermitian();
    }

    @Override
    public boolean prepare(final Collectable<N, ? super TransformableRegion<N>> matrixB) {
        return myCholesky.decompose(matrixB);
    }

    @Override
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
    protected boolean doDecompose(final Collectable<N, ? super TransformableRegion<N>> matrix, final boolean valuesOnly) {

        if (myCholesky.isComputed()) {
            myReduced = this.reduce(matrix, myCholesky, myType);
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

    @Override
    protected MatrixStore<N> makeV() {
        MatrixStore<N> subV = myEigenvalue.getV();
        if (myCholesky.isComputed()) {
            return this.recover(subV, myCholesky, myType);
        } else {
            return subV;
        }
    }

    /**
     * The eigenvectors of the generalised problem, from those of the reduced problem (C), with the Cholesky
     * factor L of B.
     */
    MatrixStore<N> recover(final MatrixStore<N> reduced, final Cholesky<N> cholesky, final Eigenvalue.Generalisation type) {

        MatrixStore<N> mtrxL = cholesky.getL();

        switch (type) {

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

    /**
     * The reduced problem: C = inv(L)*A*inv(L)<sup>H</sup> (A_B) or C = L<sup>H</sup>*A*L (AB and BA), with
     * the Cholesky factor L of B.
     */
    PhysicalStore<N> reduce(final Access2D.Collectable<N, ? super PhysicalStore<N>> original, final Cholesky<N> cholesky,
            final Eigenvalue.Generalisation type) {

        MatrixStore<N> mtrxL = cholesky.getL();

        switch (type) {

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
