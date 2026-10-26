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

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.util.Arrays;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.BasicArray;
import org.ojalgo.array.ScalarArray;
import org.ojalgo.matrix.operation.GEBRD;
import org.ojalgo.matrix.operation.ORGBR;
import org.ojalgo.matrix.store.DiagonalStore;
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
 * The reduction to upper bidiagonal form is done by {@link GEBRD}, and Q and P formed by {@link ORGBR}. A
 * matrix with more columns than rows is reduced to lower bidiagonal form, the conjugate transpose of the
 * upper bidiagonal form of its conjugate transpose.
 */
abstract class DenseBidiagonal<N extends Comparable<N>, M extends PhysicalStore<N>> extends InPlaceDecomposition<N, M> implements Bidiagonal<N> {

    static final class C128 extends Generic<ComplexNumber> {

        C128() {
            this(false);
        }

        C128(final boolean fullSize) {
            super(GenericStore.C128, fullSize);
        }

    }

    /**
     * The number types other than double, stored in a {@link GenericStore}. The bidiagonal matrix is made
     * real by diagonal unitary scalings, applied to the columns of Q and P.
     */
    abstract static class Generic<N extends Scalar<N>> extends DenseBidiagonal<N, GenericStore<N>> {

        Generic(final GenericStore.Factory<N> factory, final boolean fullSize) {
            super(factory, fullSize);
        }

        /**
         * Whether the imaginary part is exactly 0.
         */
        private static <N extends Scalar<N>> boolean isReal(final Scalar<N> value) {
            return value.subtract(value.conjugate()).norm() == ZERO;
        }

        @Override
        void generateP(final GenericStore<N> p, final int k, final BasicArray<N> taup) {
            int n = p.getRowDim();
            ORGBR.invoke(false, n, n, k, p.data, 0, Math.max(1, n), ((ScalarArray<N>) taup).data, 0, p.physical().scalar());
        }

        @Override
        void generateQ(final GenericStore<N> q, final int k, final BasicArray<N> tauq) {
            int m = q.getRowDim();
            ORGBR.invoke(true, m, q.getColDim(), k, q.data, 0, Math.max(1, m), ((ScalarArray<N>) tauq).data, 0, q.physical().scalar());
        }

        /**
         * B = diag(phasesQ) * B' * diag(phasesP)<sup>H</sup> with B' real: one element at a time, along the
         * diagonal and the superdiagonal, the phase that makes it real is taken out.
         */
        @Override
        boolean makeReal(final BasicArray<N> d, final BasicArray<N> e, final BasicArray<N> phasesQ, final BasicArray<N> phasesP) {

            N[] diagonal = ((ScalarArray<N>) d).data;
            N[] superdiagonal = ((ScalarArray<N>) e).data;
            N[] left = ((ScalarArray<N>) phasesQ).data;
            N[] right = ((ScalarArray<N>) phasesP).data;
            int n = diagonal.length;

            Scalar.Factory<N> scalar = this.scalar();
            N one = scalar.one().get();

            Arrays.fill(left, one);
            Arrays.fill(right, one);
            boolean retVal = false;

            for (int i = 0; i < n; i++) {

                // B(i,i) * phasesP(i)
                Scalar<N> value = diagonal[i].multiply(right[i]);

                if (Generic.isReal(value)) {
                    diagonal[i] = value.get();
                } else {
                    left[i] = value.normalised().get();
                    diagonal[i] = scalar.cast(value.norm());
                    retVal = true;
                }

                if (i + 1 < n) {

                    // conj(phasesQ(i)) * B(i,i+1)
                    Scalar<N> next = left[i].conjugate().multiply(superdiagonal[i]);

                    if (Generic.isReal(next)) {
                        superdiagonal[i] = next.get();
                    } else {
                        right[i + 1] = next.normalised().conjugate().get();
                        superdiagonal[i] = scalar.cast(next.norm());
                        retVal = true;
                    }
                }
            }

            return retVal;
        }

        @Override
        void reduce(final GenericStore<N> inPlace, final BasicArray<N> d, final BasicArray<N> e, final BasicArray<N> tauq, final BasicArray<N> taup) {
            int m = inPlace.getRowDim();
            GEBRD.invoke(m, inPlace.getColDim(), inPlace.data, 0, Math.max(1, m), ((ScalarArray<N>) d).data, 0, ((ScalarArray<N>) e).data, 0,
                    ((ScalarArray<N>) tauq).data, 0, ((ScalarArray<N>) taup).data, 0, inPlace.physical().scalar());
        }

    }

    static final class H256 extends Generic<Quaternion> {

        H256() {
            this(false);
        }

        H256(final boolean fullSize) {
            super(GenericStore.H256, fullSize);
        }

    }

    static final class Q128 extends Generic<RationalNumber> {

        Q128() {
            this(false);
        }

        Q128(final boolean fullSize) {
            super(GenericStore.Q128, fullSize);
        }

    }

    static final class R064 extends DenseBidiagonal<Double, R064Store> {

        R064() {
            this(false);
        }

        R064(final boolean fullSize) {
            super(R064Store.FACTORY, fullSize);
        }

        @Override
        void generateP(final R064Store p, final int k, final BasicArray<Double> taup) {
            int n = p.getRowDim();
            ORGBR.invoke(false, n, n, k, p.data, 0, Math.max(1, n), ((ArrayR064) taup).data, 0);
        }

        @Override
        void generateQ(final R064Store q, final int k, final BasicArray<Double> tauq) {
            int m = q.getRowDim();
            ORGBR.invoke(true, m, q.getColDim(), k, q.data, 0, Math.max(1, m), ((ArrayR064) tauq).data, 0);
        }

        @Override
        boolean makeReal(final BasicArray<Double> d, final BasicArray<Double> e, final BasicArray<Double> phasesQ, final BasicArray<Double> phasesP) {
            return false;
        }

        @Override
        void reduce(final R064Store inPlace, final BasicArray<Double> d, final BasicArray<Double> e, final BasicArray<Double> tauq,
                final BasicArray<Double> taup) {
            int m = inPlace.getRowDim();
            GEBRD.invoke(m, inPlace.getColDim(), inPlace.data, 0, Math.max(1, m), ((ArrayR064) d).data, 0, ((ArrayR064) e).data, 0, ((ArrayR064) tauq).data, 0,
                    ((ArrayR064) taup).data, 0);
        }

    }

    static final class R128 extends Generic<Quadruple> {

        R128() {
            this(false);
        }

        R128(final boolean fullSize) {
            super(GenericStore.R128, fullSize);
        }

    }

    /**
     * The diagonal and the superdiagonal of the (upper) bidiagonal matrix, as returned by {@link GEBRD}, and
     * made real
     */
    private BasicArray<N> myD = null;
    private transient DiagonalStore<N, BasicArray<N>> myDiagonal = null;
    private BasicArray<N> myE = null;
    private final boolean myFullSize;
    private transient M myLQ = null;
    /**
     * The diagonal unitary scalings of the columns of P and Q, used when {@link #myScaled}
     */
    private BasicArray<N> myPhasesP = null;
    private BasicArray<N> myPhasesQ = null;
    private transient M myRQ = null;
    private boolean myScaled = false;
    /**
     * tau of the reflectors, as returned by {@link GEBRD}
     */
    private BasicArray<N> myTauP = null;
    private BasicArray<N> myTauQ = null;
    private boolean myUpper = true;

    protected DenseBidiagonal(final PhysicalStore.Factory<N, ? extends M> factory, final boolean fullSize) {
        super(factory);
        myFullSize = fullSize;
    }

    @Override
    public boolean decompose(final Access2D.Collectable<N, ? super TransformableRegion<N>> matrix) {

        this.reset();

        myUpper = matrix.countRows() >= matrix.countColumns();

        M inPlace = myUpper ? this.setInPlace(matrix) : this.setInPlace(this.collect(matrix).conjugate());

        int n = inPlace.getColDim();

        if (myD == null || myD.count() != n) {
            myD = this.makeArray(n);
            myE = this.makeArray(Math.max(0, n - 1));
            myTauQ = this.makeArray(n);
            myTauP = this.makeArray(n);
            myPhasesQ = this.makeArray(n);
            myPhasesP = this.makeArray(n);
        }

        this.reduce(inPlace, myD, myE, myTauQ, myTauP);

        myScaled = this.makeReal(myD, myE, myPhasesQ, myPhasesP);

        return this.computed(true);
    }

    @Override
    public int getColDim() {
        return myUpper ? super.getColDim() : super.getRowDim();
    }

    @Override
    public MatrixStore<N> getD() {
        MatrixStore<N> retVal = this.doGetDiagonal();
        if (myFullSize) {
            if (this.getRowDim() > retVal.getRowDim()) {
                retVal = retVal.below(this.getRowDim() - retVal.getRowDim());
            } else if (this.getColDim() > retVal.getColDim()) {
                retVal = retVal.right(this.getColDim() - retVal.getColDim());
            }
        }
        return retVal;
    }

    @Override
    public MatrixStore<N> getLQ() {
        return this.doGetLQ();
    }

    @Override
    public MatrixStore<N> getRQ() {
        return this.doGetRQ();
    }

    @Override
    public int getRowDim() {
        return myUpper ? super.getRowDim() : super.getColDim();
    }

    @Override
    public boolean isFullSize() {
        return myFullSize;
    }

    @Override
    public boolean isUpper() {
        return myUpper;
    }

    @Override
    public void reset() {

        super.reset();

        myLQ = null;
        myRQ = null;
        myDiagonal = null;

        myScaled = false;
        myUpper = true;
    }

    /**
     * P, n-by-n, of the reduced m-by-n matrix (m >= n)
     */
    private M makeP() {

        M inPlace = this.getInPlace();
        int m = inPlace.getRowDim();
        int n = inPlace.getColDim();

        M transposedP = this.makeZero(n, n);
        inPlace.limits(n, n).supplyTo(transposedP);
        this.generateP(transposedP, m, myTauP);

        M retVal = this.makeZero(n, n);
        transposedP.conjugate().supplyTo(retVal);

        if (myScaled) {
            this.scaleColumns(retVal, myPhasesP);
        }

        return retVal;
    }

    /**
     * Q, m-by-m if full size, otherwise m-by-n, of the reduced m-by-n matrix (m >= n)
     */
    private M makeQ() {

        M inPlace = this.getInPlace();
        int m = inPlace.getRowDim();
        int n = inPlace.getColDim();

        M retVal = this.makeZero(m, myFullSize ? m : n);
        inPlace.supplyTo(retVal.regionByLimits(m, n));
        this.generateQ(retVal, n, myTauQ);

        if (myScaled) {
            this.scaleColumns(retVal, myPhasesQ);
        }

        return retVal;
    }

    /**
     * Multiplies column j by phases(j), from the right, for the first phases.count() columns.
     */
    private void scaleColumns(final M store, final BasicArray<N> phases) {
        Scalar.Factory<N> scalar = this.scalar();
        int m = store.getRowDim();
        for (int j = 0, limit = (int) phases.count(); j < limit; j++) {
            N phase = phases.get(j);
            for (int i = 0; i < m; i++) {
                store.set(i, j, scalar.convert(store.get(i, j)).multiply(phase).get());
            }
        }
    }

    DiagonalStore<N, BasicArray<N>> doGetDiagonal() {
        if (myDiagonal == null) {
            DiagonalStore.Builder<N, BasicArray<N>> builder = this.makeDiagonal(myD);
            myDiagonal = (myUpper ? builder.superdiagonal(myE) : builder.subdiagonal(myE)).get();
        }
        return myDiagonal;
    }

    M doGetLQ() {
        if (myLQ == null) {
            myLQ = myUpper ? this.makeQ() : this.makeP();
        }
        return myLQ;
    }

    M doGetRQ() {
        if (myRQ == null) {
            myRQ = myUpper ? this.makeP() : this.makeQ();
        }
        return myRQ;
    }

    /**
     * Overwrites p, the first n rows of the reduced matrix, with P<sup>T</sup> (P<sup>H</sup>), using taup as
     * returned by {@link #reduce(PhysicalStore, BasicArray, BasicArray, BasicArray, BasicArray)}. k is the
     * number of rows of the reduced matrix.
     */
    abstract void generateP(M p, int k, BasicArray<N> taup);

    /**
     * Overwrites q, the reduced matrix in its first k columns, with Q, using tauq as returned by
     * {@link #reduce(PhysicalStore, BasicArray, BasicArray, BasicArray, BasicArray)}.
     */
    abstract void generateQ(M q, int k, BasicArray<N> tauq);

    /**
     * Writes diagonal unitary scalings of the columns of Q and P, that make the bidiagonal matrix real, to
     * phasesQ and phasesP, and overwrites d and e with the real values.
     *
     * @return false if the bidiagonal matrix is real already, and phasesQ and phasesP are not used
     */
    abstract boolean makeReal(BasicArray<N> d, BasicArray<N> e, BasicArray<N> phasesQ, BasicArray<N> phasesP);

    /**
     * Reduces the in-place m-by-n matrix (m >= n) to upper bidiagonal form, writing its diagonal and
     * superdiagonal to d and e, and tau of the reflectors to tauq and taup.
     */
    abstract void reduce(M inPlace, BasicArray<N> d, BasicArray<N> e, BasicArray<N> tauq, BasicArray<N> taup);

}
