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

import org.ojalgo.array.Array1D;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.BasicArray;
import org.ojalgo.array.ScalarArray;
import org.ojalgo.matrix.operation.ORGTR;
import org.ojalgo.matrix.operation.SYTRD;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access2D;

/**
 * @author apete
 */
abstract class DeferredTridiagonal<N extends Comparable<N>, M extends PhysicalStore<N>> extends DenseTridiagonal<N, M> {

    static final class C128 extends Generic<ComplexNumber> {

        C128() {
            super(GenericStore.C128);
        }

        @Override
        Array1D<ComplexNumber> makeReal(final BasicArray<ComplexNumber> offDiagonal) {

            Array1D<ComplexNumber> retVal = Array1D.C128.make(offDiagonal.count());
            retVal.fillAll(ComplexNumber.ONE);

            BasicArray<ComplexNumber> tmpSubdiagonal = offDiagonal; // superDiagonal should be the conjugate
                                                                    // of this but it is set to the same value

            ComplexNumber tmpVal = null;
            for (int i = 0; i < tmpSubdiagonal.count(); i++) {

                tmpVal = tmpSubdiagonal.get(i).normalised();

                if (!tmpVal.isReal()) {

                    tmpSubdiagonal.set(i, tmpSubdiagonal.get(i).divide(tmpVal));

                    if (i + 1 < tmpSubdiagonal.count()) {
                        tmpSubdiagonal.set(i + 1, tmpSubdiagonal.get(i + 1).multiply(tmpVal));
                    }

                    retVal.set(i + 1, tmpVal);
                }
            }

            return retVal;
        }

    }

    /**
     * The number types other than double, stored in a {@link GenericStore}.
     */
    abstract static class Generic<N extends Scalar<N>> extends DeferredTridiagonal<N, GenericStore<N>> {

        Generic(final GenericStore.Factory<N> factory) {
            super(factory);
        }

        @Override
        void generateQ(final GenericStore<N> inPlace, final BasicArray<N> tau) {
            int n = inPlace.getRowDim();
            ORGTR.invoke(n, inPlace.data, 0, Math.max(1, n), ((ScalarArray<N>) tau).data, 0, inPlace.physical().scalar());
        }

        @Override
        void reduce(final GenericStore<N> inPlace, final BasicArray<N> d, final BasicArray<N> e, final BasicArray<N> tau) {
            int n = inPlace.getRowDim();
            SYTRD.invoke(n, inPlace.data, 0, Math.max(1, n), ((ScalarArray<N>) d).data, 0, ((ScalarArray<N>) e).data, 0, ((ScalarArray<N>) tau).data, 0,
                    inPlace.physical().scalar());
        }

    }

    static final class H256 extends Generic<Quaternion> {

        H256() {
            super(GenericStore.H256);
        }

        @Override
        Array1D<Quaternion> makeReal(final BasicArray<Quaternion> offDiagonal) {

            Array1D<Quaternion> retVal = Array1D.H256.make(offDiagonal.count());
            retVal.fillAll(Quaternion.ONE);

            BasicArray<Quaternion> tmpSubdiagonal = offDiagonal; // superDiagonal should be the conjugate of
                                                                 // this but it is set to the same value

            Quaternion tmpVal = null;
            for (int i = 0; i < tmpSubdiagonal.count(); i++) {

                tmpVal = tmpSubdiagonal.get(i).normalised();

                if (!tmpVal.isReal()) {

                    tmpSubdiagonal.set(i, tmpSubdiagonal.get(i).divide(tmpVal));

                    if (i + 1 < tmpSubdiagonal.count()) {
                        tmpSubdiagonal.set(i + 1, tmpSubdiagonal.get(i + 1).multiply(tmpVal));
                    }

                    retVal.set(i + 1, tmpVal);
                }
            }

            return retVal;
        }

    }

    static final class Q128 extends Generic<RationalNumber> {

        Q128() {
            super(GenericStore.Q128);
        }

        @Override
        Array1D<RationalNumber> makeReal(final BasicArray<RationalNumber> offDiagonal) {
            return null;
        }
    }

    static final class R064 extends DeferredTridiagonal<Double, R064Store> {

        R064() {
            super(R064Store.FACTORY);
        }

        @Override
        void generateQ(final R064Store inPlace, final BasicArray<Double> tau) {
            int n = inPlace.getRowDim();
            ORGTR.invoke(n, inPlace.data, 0, Math.max(1, n), ((ArrayR064) tau).data, 0);
        }

        @Override
        Array1D<Double> makeReal(final BasicArray<Double> offDiagonal) {
            return null;
        }

        @Override
        void reduce(final R064Store inPlace, final BasicArray<Double> d, final BasicArray<Double> e, final BasicArray<Double> tau) {
            int n = inPlace.getRowDim();
            SYTRD.invoke(n, inPlace.data, 0, Math.max(1, n), ((ArrayR064) d).data, 0, ((ArrayR064) e).data, 0, ((ArrayR064) tau).data, 0);
        }

    }

    static final class R128 extends Generic<Quadruple> {

        R128() {
            super(GenericStore.R128);
        }

        @Override
        Array1D<Quadruple> makeReal(final BasicArray<Quadruple> offDiagonal) {
            return null;
        }
    }

    private transient BasicArray<N> myDiagD = null;
    private transient BasicArray<N> myDiagE = null;
    private Array1D<N> myInitDiagQ = null;
    /**
     * tau of the reflectors, as returned by {@link SYTRD}
     */
    private BasicArray<N> myTau = null;

    protected DeferredTridiagonal(final PhysicalStore.Factory<N, ? extends M> factory) {
        super(factory);
    }

    public boolean decompose(final Access2D.Collectable<N, ? super TransformableRegion<N>> matrix) {

        this.reset();

        boolean retVal = false;

        try {

            MatrixStore<N> logicallyTriangularMatrix = this.collect(matrix).triangular(false, false);
            // TODO Not optimal code here!
            M inPlace = this.setInPlace(logicallyTriangularMatrix);

            int size = this.getMinDim();

            if (myDiagD == null || myDiagD.count() != size) {
                myDiagD = this.makeArray(size);
                myDiagE = this.makeArray(size);
                myTau = this.makeArray(size);
            }

            this.reduce(inPlace, myDiagD, myDiagE, myTau);

            myInitDiagQ = this.makeReal(myDiagE);

            retVal = true;

        } catch (Exception xcptn) {

            BasicLogger.error(xcptn.toString());

            this.reset();

            retVal = false;
        }

        return this.computed(retVal);
    }

    @Override
    public void reset() {

        super.reset();

        myInitDiagQ = null;
    }

    @Override
    protected void supplyDiagonalTo(final double[] d, final double[] e) {
        myDiagD.supplyTo(d);
        myDiagE.supplyTo(e);
    }

    /**
     * Overwrites the reflectors in the lower triangle of the reduced matrix with Q, using tau as returned by
     * {@link #reduce(PhysicalStore, BasicArray, BasicArray, BasicArray)}.
     */
    abstract void generateQ(M inPlace, BasicArray<N> tau);

    @Override
    MatrixStore<N> makeD() {
        return this.makeDiagonal(myDiagD).superdiagonal(myDiagE).subdiagonal(myDiagE).get();
    }

    @Override
    M makeQ() {

        M retVal = this.getInPlace();

        this.generateQ(retVal, myTau);

        if (myInitDiagQ != null) {
            // Q * diag(myInitDiagQ)
            Scalar.Factory<N> scalar = this.scalar();
            int n = this.getMinDim();
            for (int j = 0; j < n; j++) {
                N phase = myInitDiagQ.get(j);
                for (int i = 0; i < n; i++) {
                    retVal.set(i, j, scalar.convert(retVal.get(i, j)).multiply(phase).get());
                }
            }
        }

        return retVal;
    }

    abstract Array1D<N> makeReal(BasicArray<N> offDiagonal);

    /**
     * Reduces the lower triangle of the in-place matrix to tridiagonal form, writing its diagonal and
     * subdiagonal to d and e, and tau of the reflectors (that stay in the lower triangle) to tau.
     */
    abstract void reduce(M inPlace, BasicArray<N> d, BasicArray<N> e, BasicArray<N> tau);

}
