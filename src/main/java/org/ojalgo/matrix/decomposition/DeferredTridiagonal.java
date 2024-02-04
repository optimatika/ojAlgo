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
import org.ojalgo.array.BasicArray;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.HouseholderReference;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access2D;

/**
 * @author apete
 */
abstract class DeferredTridiagonal<N extends Comparable<N>> extends TridiagonalDecomposition<N> {

    static final class C128 extends DeferredTridiagonal<ComplexNumber> {

        C128() {
            super(GenericStore.C128);
        }

        @Override
        Array1D<ComplexNumber> makeReal(final BasicArray<ComplexNumber> offDiagonal) {

            Array1D<ComplexNumber> retVal = Array1D.C128.make(offDiagonal.count());
            retVal.fillAll(ComplexNumber.ONE);

            BasicArray<ComplexNumber> tmpSubdiagonal = offDiagonal; // superDiagonal should be the conjugate of this but it is set to the same value

            ComplexNumber tmpVal = null;
            for (int i = 0; i < tmpSubdiagonal.count(); i++) {

                tmpVal = tmpSubdiagonal.get(i).signum();

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

    static final class H256 extends DeferredTridiagonal<Quaternion> {

        H256() {
            super(GenericStore.H256);
        }

        @Override
        Array1D<Quaternion> makeReal(final BasicArray<Quaternion> offDiagonal) {

            Array1D<Quaternion> retVal = Array1D.H256.make(offDiagonal.count());
            retVal.fillAll(Quaternion.ONE);

            BasicArray<Quaternion> tmpSubdiagonal = offDiagonal; // superDiagonal should be the conjugate of this but it is set to the same value

            Quaternion tmpVal = null;
            for (int i = 0; i < tmpSubdiagonal.count(); i++) {

                tmpVal = tmpSubdiagonal.get(i).signum();

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

    static final class Q128 extends DeferredTridiagonal<RationalNumber> {

        Q128() {
            super(GenericStore.Q128);
        }

        @Override
        Array1D<RationalNumber> makeReal(final BasicArray<RationalNumber> offDiagonal) {
            return null;
        }
    }

    static final class R064 extends DeferredTridiagonal<Double> {

        R064() {
            super(Primitive64Store.FACTORY);
        }

        @Override
        Array1D<Double> makeReal(final BasicArray<Double> offDiagonal) {
            return null;
        }

    }

    static final class R128 extends DeferredTridiagonal<Quadruple> {

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

    protected DeferredTridiagonal(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> factory) {
        super(factory);
    }

    public boolean decompose(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix) {

        this.reset();

        boolean retVal = false;

        try {

            MatrixStore<N> logicallyTriangularMatrix = this.collect(matrix).triangular(false, false);
            //TODO Not optimal code here!
            DecompositionStore<N> inPlace = this.setInPlace(logicallyTriangularMatrix);

            int size = this.getMinDim();

            if (myDiagD == null || myDiagD.count() != size) {
                myDiagD = this.makeArray(size);
                myDiagE = this.makeArray(size);
            }

            Householder<N> tmpHouseholder = this.makeHouseholder(size);

            int limit = size - 2;
            for (int ij = 0; ij < limit; ij++) {
                if (inPlace.generateApplyAndCopyHouseholderColumn(ij + 1, ij, tmpHouseholder)) {
                    inPlace.transformSymmetric(tmpHouseholder);
                }
            }

            myDiagD.fillMatching(inPlace.sliceDiagonal(0, 0));
            myDiagE.fillMatching(inPlace.sliceDiagonal(1, 0));

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

    @Override
    MatrixStore<N> makeD() {
        return this.makeDiagonal(myDiagD).superdiagonal(myDiagE).subdiagonal(myDiagE).get();
    }

    @Override
    DecompositionStore<N> makeQ() {

        DecompositionStore<N> retVal = this.getInPlace();
        int tmpDim = this.getMinDim();

        HouseholderReference<N> tmpReference = HouseholderReference.makeColumn(retVal);

        if (myInitDiagQ != null) {
            retVal.set(tmpDim - 1, tmpDim - 1, myInitDiagQ.get(tmpDim - 1));
            if (tmpDim >= 2) {
                retVal.set(tmpDim - 2, tmpDim - 2, myInitDiagQ.get(tmpDim - 2));
            }
        } else {
            retVal.set(tmpDim - 1, tmpDim - 1, PrimitiveMath.ONE);
            if (tmpDim >= 2) {
                retVal.set(tmpDim - 2, tmpDim - 2, PrimitiveMath.ONE);
            }
        }
        if (tmpDim >= 2) {
            retVal.set(tmpDim - 1, tmpDim - 2, PrimitiveMath.ZERO);
        }

        for (int ij = tmpDim - 3; ij >= 0; ij--) {

            tmpReference.point(ij + 1, ij);

            if (!tmpReference.isZero()) {
                retVal.transformLeft(tmpReference, ij);
            }

            retVal.setToIdentity(ij);
            if (myInitDiagQ != null) {
                retVal.set(ij, ij, myInitDiagQ.get(ij));
            }
        }

        return retVal;
    }

    abstract Array1D<N> makeReal(BasicArray<N> offDiagonal);

}
