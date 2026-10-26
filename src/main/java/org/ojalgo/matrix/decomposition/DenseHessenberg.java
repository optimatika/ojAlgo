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

import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.BasicArray;
import org.ojalgo.array.ScalarArray;
import org.ojalgo.matrix.operation.GEHRD;
import org.ojalgo.matrix.operation.ORGHR;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access2D;

/**
 * The reduction to upper Hessenberg form is done by {@link GEHRD}, and Q formed by {@link ORGHR}. The lower
 * Hessenberg form is the conjugate transpose of the upper Hessenberg form of the conjugate transpose.
 */
abstract class DenseHessenberg<N extends Comparable<N>, M extends PhysicalStore<N>> extends InPlaceDecomposition<N, M> implements Hessenberg<N> {

    static final class C128 extends Generic<ComplexNumber> {

        C128() {
            super(GenericStore.C128);
        }

    }

    /**
     * The number types other than double, stored in a {@link GenericStore}.
     */
    abstract static class Generic<N extends Scalar<N>> extends DenseHessenberg<N, GenericStore<N>> {

        Generic(final GenericStore.Factory<N> factory) {
            super(factory);
        }

        @Override
        void generateQ(final GenericStore<N> q, final BasicArray<N> tau) {
            int n = q.getRowDim();
            ORGHR.invoke(n, q.data, 0, Math.max(1, n), ((ScalarArray<N>) tau).data, 0, q.physical().scalar());
        }

        @Override
        void reduce(final GenericStore<N> inPlace, final BasicArray<N> tau) {
            int n = inPlace.getRowDim();
            GEHRD.invoke(n, inPlace.data, 0, Math.max(1, n), ((ScalarArray<N>) tau).data, 0, inPlace.physical().scalar());
        }

    }

    static final class H256 extends Generic<Quaternion> {

        H256() {
            super(GenericStore.H256);
        }

    }

    static final class Q128 extends Generic<RationalNumber> {

        Q128() {
            super(GenericStore.Q128);
        }

    }

    static final class R064 extends DenseHessenberg<Double, R064Store> {

        R064() {
            super(R064Store.FACTORY);
        }

        @Override
        void generateQ(final R064Store q, final BasicArray<Double> tau) {
            int n = q.getRowDim();
            ORGHR.invoke(n, q.data, 0, Math.max(1, n), ((ArrayR064) tau).data, 0);
        }

        @Override
        void reduce(final R064Store inPlace, final BasicArray<Double> tau) {
            int n = inPlace.getRowDim();
            GEHRD.invoke(n, inPlace.data, 0, Math.max(1, n), ((ArrayR064) tau).data, 0);
        }

    }

    static final class R128 extends Generic<Quadruple> {

        R128() {
            super(GenericStore.R128);
        }

    }

    private transient M myQ = null;
    /**
     * tau of the reflectors, as returned by {@link GEHRD}
     */
    private BasicArray<N> myTau = null;
    private boolean myUpper = true;

    protected DenseHessenberg(final PhysicalStore.Factory<N, ? extends M> factory) {
        super(factory);
    }

    @Override
    public final boolean compute(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix, final boolean upper) {

        this.reset();

        myUpper = upper;

        M inPlace = upper ? this.setInPlace(matrix) : this.setInPlace(this.collect(matrix).conjugate());

        int n = this.getRowDim();

        if (n != this.getColDim()) {
            return this.computed(false);
        }

        if (myTau == null || myTau.count() != n) {
            myTau = this.makeArray(n);
        }

        this.reduce(inPlace, myTau);

        return this.computed(true);
    }

    @Override
    public final boolean decompose(final Access2D.Collectable<N, ? super TransformableRegion<N>> matrix) {
        return this.compute(matrix, true);
    }

    @Override
    public final MatrixStore<N> getH() {
        MatrixStore<N> upperH = this.getInPlace().hessenberg(true);
        return myUpper ? upperH : upperH.conjugate();
    }

    @Override
    public final MatrixStore<N> getQ() {
        if (myQ == null) {
            M inPlace = this.getInPlace();
            myQ = this.makeZero(inPlace.getRowDim(), inPlace.getColDim());
            inPlace.supplyTo(myQ);
            this.generateQ(myQ, myTau);
        }
        return myQ;
    }

    @Override
    public boolean isUpper() {
        return myUpper;
    }

    @Override
    public void reset() {

        super.reset();

        myQ = null;
        myUpper = true;
    }

    final PhysicalStore<N> doQ(final PhysicalStore<N> aStoreToTransform) {
        this.getQ().multiply(aStoreToTransform).supplyTo(aStoreToTransform);
        return aStoreToTransform;
    }

    /**
     * Overwrites q, a copy of the reduced matrix, with Q, using tau as returned by
     * {@link #reduce(PhysicalStore, BasicArray)}.
     */
    abstract void generateQ(M q, BasicArray<N> tau);

    /**
     * Reduces the in-place matrix to upper Hessenberg form, leaving the reflectors below the first
     * subdiagonal, and writing their tau to tau.
     */
    abstract void reduce(M inPlace, BasicArray<N> tau);

}
