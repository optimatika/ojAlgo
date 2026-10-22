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
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.operation.GenerateApplyAndCopyHouseholderColumn;
import org.ojalgo.matrix.operation.GenerateApplyAndCopyHouseholderRow;
import org.ojalgo.matrix.operation.HouseholderStep;
import org.ojalgo.matrix.store.DiagonalStore;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.HouseholderReference;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access2D;

abstract class DenseBidiagonal<N extends Comparable<N>, M extends PhysicalStore<N>> extends InPlaceDecomposition<N, M> implements Bidiagonal<N> {

    static final class C128 extends DenseBidiagonal<ComplexNumber, GenericStore<ComplexNumber>> {

        C128() {
            this(false);
        }

        C128(final boolean fullSize) {
            super(GenericStore.C128, fullSize, GenerateApplyAndCopyHouseholderColumn::invokeGeneric, GenerateApplyAndCopyHouseholderRow::invokeGeneric);
        }

        @Override
        Array1D<ComplexNumber>[] makeReal() {

            DiagonalStore<ComplexNumber, Array1D<ComplexNumber>> tmpDiagonalAccessD = this.doGetDiagonal();

            Array1D<ComplexNumber> tmpInitDiagQ1 = Array1D.C128.make(tmpDiagonalAccessD.getDimension());
            tmpInitDiagQ1.fillAll(ComplexNumber.ONE);

            Array1D<ComplexNumber> tmpInitDiagQ2 = Array1D.C128.make(tmpDiagonalAccessD.getDimension());
            tmpInitDiagQ2.fillAll(ComplexNumber.ONE);

            boolean tmpUpper = this.isUpper();

            if (tmpUpper) {

                Array1D<ComplexNumber> tmpMainDiagonal = tmpDiagonalAccessD.getMainDiagonal().get();
                Array1D<ComplexNumber> tmpSuperdiagonal = tmpDiagonalAccessD.getSuperdiagonal().get();

                int tmpLimit = (int) tmpSuperdiagonal.count();
                for (int i = 0; i < tmpLimit; i++) {

                    if (!tmpMainDiagonal.get(i).isReal()) {
                        ComplexNumber tmpSignum = tmpMainDiagonal.get(i).normalised();
                        tmpMainDiagonal.set(i, tmpMainDiagonal.get(i).divide(tmpSignum));
                        tmpSuperdiagonal.set(i, tmpSuperdiagonal.get(i).divide(tmpSignum));
                        tmpInitDiagQ1.set(i, tmpSignum);
                    }

                    if (!tmpSuperdiagonal.get(i).isReal()) {
                        ComplexNumber tmpSignum = tmpSuperdiagonal.get(i).normalised();
                        tmpSuperdiagonal.set(i, tmpSuperdiagonal.get(i).divide(tmpSignum));
                        tmpMainDiagonal.set(i + 1, tmpMainDiagonal.get(i + 1).divide(tmpSignum));
                        tmpInitDiagQ2.set(i + 1, tmpSignum.conjugate());
                    }
                }

                if (!tmpMainDiagonal.get(tmpLimit).isReal()) {
                    ComplexNumber tmpSignum = tmpMainDiagonal.get(tmpLimit).normalised();
                    tmpMainDiagonal.set(tmpLimit, tmpMainDiagonal.get(tmpLimit).divide(tmpSignum));
                    tmpInitDiagQ1.set(tmpLimit, tmpSignum);
                }

            } else {

                Array1D<ComplexNumber> tmpMainDiagonal = tmpDiagonalAccessD.getMainDiagonal().get();
                Array1D<ComplexNumber> tmpSubdiagonal = tmpDiagonalAccessD.getSubdiagonal().get();

                int tmpLimit = (int) tmpSubdiagonal.count();
                for (int i = 0; i < tmpLimit; i++) {

                    if (!tmpMainDiagonal.get(i).isReal()) {
                        ComplexNumber tmpSignum = tmpMainDiagonal.get(i).normalised();
                        tmpMainDiagonal.set(i, tmpMainDiagonal.get(i).divide(tmpSignum));
                        tmpSubdiagonal.set(i, tmpSubdiagonal.get(i).divide(tmpSignum));
                        tmpInitDiagQ2.set(i, tmpSignum.conjugate());
                    }

                    if (!tmpSubdiagonal.get(i).isReal()) {
                        ComplexNumber tmpSignum = tmpSubdiagonal.get(i).normalised();
                        tmpSubdiagonal.set(i, tmpSubdiagonal.get(i).divide(tmpSignum));
                        tmpMainDiagonal.set(i + 1, tmpMainDiagonal.get(i + 1).divide(tmpSignum));
                        tmpInitDiagQ1.set(i + 1, tmpSignum);
                    }
                }

                if (!tmpMainDiagonal.get(tmpLimit).isReal()) {
                    ComplexNumber tmpSignum = tmpMainDiagonal.get(tmpLimit).normalised();
                    tmpMainDiagonal.set(tmpLimit, tmpMainDiagonal.get(tmpLimit).divide(tmpSignum));
                    tmpInitDiagQ2.set(tmpLimit, tmpSignum.conjugate());
                }

            }

            return new Array1D[] { tmpInitDiagQ1, tmpInitDiagQ2 };
        }

    }

    static final class H256 extends DenseBidiagonal<Quaternion, GenericStore<Quaternion>> {

        H256() {
            this(false);
        }

        H256(final boolean fullSize) {
            super(GenericStore.H256, fullSize, GenerateApplyAndCopyHouseholderColumn::invokeGeneric, GenerateApplyAndCopyHouseholderRow::invokeGeneric);
        }

        @Override
        Array1D<Quaternion>[] makeReal() {

            DiagonalStore<Quaternion, Array1D<Quaternion>> tmpDiagonalAccessD = this.doGetDiagonal();

            // [B] = [P][B'][R]^H with [B'] real, [P] and [R] diagonal unitary: left factors (rows) multiply from
            // the left, right factors (columns) from the right
            Array1D<Quaternion> tmpInitDiagQ1 = Array1D.H256.make(tmpDiagonalAccessD.getDimension());
            tmpInitDiagQ1.fillAll(Quaternion.ONE);

            Array1D<Quaternion> tmpInitDiagQ2 = Array1D.H256.make(tmpDiagonalAccessD.getDimension());
            tmpInitDiagQ2.fillAll(Quaternion.ONE);

            Array1D<Quaternion> tmpMainDiagonal = tmpDiagonalAccessD.getMainDiagonal().get();

            if (this.isUpper()) {

                Array1D<Quaternion> tmpSuperdiagonal = tmpDiagonalAccessD.getSuperdiagonal().get();

                int tmpLimit = (int) tmpSuperdiagonal.count();
                for (int i = 0; i < tmpLimit; i++) {

                    if (!tmpMainDiagonal.get(i).isReal()) {
                        Quaternion tmpSignum = tmpMainDiagonal.get(i).normalised();
                        tmpMainDiagonal.set(i, tmpSignum.conjugate().multiply(tmpMainDiagonal.get(i)));
                        tmpSuperdiagonal.set(i, tmpSignum.conjugate().multiply(tmpSuperdiagonal.get(i)));
                        tmpInitDiagQ1.set(i, tmpSignum);
                    }

                    if (!tmpSuperdiagonal.get(i).isReal()) {
                        Quaternion tmpSignum = tmpSuperdiagonal.get(i).normalised();
                        tmpSuperdiagonal.set(i, tmpSuperdiagonal.get(i).multiply(tmpSignum.conjugate()));
                        tmpMainDiagonal.set(i + 1, tmpMainDiagonal.get(i + 1).multiply(tmpSignum.conjugate()));
                        tmpInitDiagQ2.set(i + 1, tmpSignum.conjugate());
                    }
                }

                if (!tmpMainDiagonal.get(tmpLimit).isReal()) {
                    Quaternion tmpSignum = tmpMainDiagonal.get(tmpLimit).normalised();
                    tmpMainDiagonal.set(tmpLimit, tmpSignum.conjugate().multiply(tmpMainDiagonal.get(tmpLimit)));
                    tmpInitDiagQ1.set(tmpLimit, tmpSignum);
                }

            } else {

                Array1D<Quaternion> tmpSubdiagonal = tmpDiagonalAccessD.getSubdiagonal().get();

                int tmpLimit = (int) tmpSubdiagonal.count();
                for (int i = 0; i < tmpLimit; i++) {

                    if (!tmpMainDiagonal.get(i).isReal()) {
                        Quaternion tmpSignum = tmpMainDiagonal.get(i).normalised();
                        tmpMainDiagonal.set(i, tmpMainDiagonal.get(i).multiply(tmpSignum.conjugate()));
                        tmpSubdiagonal.set(i, tmpSubdiagonal.get(i).multiply(tmpSignum.conjugate()));
                        tmpInitDiagQ2.set(i, tmpSignum.conjugate());
                    }

                    if (!tmpSubdiagonal.get(i).isReal()) {
                        Quaternion tmpSignum = tmpSubdiagonal.get(i).normalised();
                        tmpSubdiagonal.set(i, tmpSignum.conjugate().multiply(tmpSubdiagonal.get(i)));
                        tmpMainDiagonal.set(i + 1, tmpSignum.conjugate().multiply(tmpMainDiagonal.get(i + 1)));
                        tmpInitDiagQ1.set(i + 1, tmpSignum);
                    }
                }

                if (!tmpMainDiagonal.get(tmpLimit).isReal()) {
                    Quaternion tmpSignum = tmpMainDiagonal.get(tmpLimit).normalised();
                    tmpMainDiagonal.set(tmpLimit, tmpMainDiagonal.get(tmpLimit).multiply(tmpSignum.conjugate()));
                    tmpInitDiagQ2.set(tmpLimit, tmpSignum.conjugate());
                }
            }

            return new Array1D[] { tmpInitDiagQ1, tmpInitDiagQ2 };
        }

    }

    static final class Q128 extends DenseBidiagonal<RationalNumber, GenericStore<RationalNumber>> {

        Q128() {
            this(false);
        }

        Q128(final boolean fullSize) {
            super(GenericStore.Q128, fullSize, GenerateApplyAndCopyHouseholderColumn::invokeGeneric, GenerateApplyAndCopyHouseholderRow::invokeGeneric);
        }

        @Override
        Array1D<RationalNumber>[] makeReal() {
            return null;
        }

    }

    static final class R064 extends DenseBidiagonal<Double, R064Store> {

        R064() {
            this(false);
        }

        R064(final boolean fullSize) {
            super(R064Store.FACTORY, fullSize, GenerateApplyAndCopyHouseholderColumn::invokeR064, GenerateApplyAndCopyHouseholderRow::invokeR064);
        }

        @Override
        Array1D<Double>[] makeReal() {
            return null;
        }

    }

    static final class R128 extends DenseBidiagonal<Quadruple, GenericStore<Quadruple>> {

        R128() {
            this(false);
        }

        R128(final boolean fullSize) {
            super(GenericStore.R128, fullSize, GenerateApplyAndCopyHouseholderColumn::invokeGeneric, GenerateApplyAndCopyHouseholderRow::invokeGeneric);
        }

        @Override
        Array1D<Quadruple>[] makeReal() {
            return null;
        }

    }

    /**
     * beta of the column reflectors (for the left Q), 0 where there is none
     */
    private double[] myBetaLQ = null;
    /**
     * beta of the row reflectors (for the right Q), 0 where there is none
     */
    private double[] myBetaRQ = null;
    private transient DiagonalStore<N, Array1D<N>> myDiagonal;
    private final boolean myFullSize;
    private final HouseholderStep<N, M> myHouseholderColumn;
    private final HouseholderStep<N, M> myHouseholderRow;
    private Array1D<N> myInitDiagLQ = null;
    private Array1D<N> myInitDiagRQ = null;
    private transient M myLQ;
    private transient M myRQ;

    protected DenseBidiagonal(final PhysicalStore.Factory<N, ? extends M> factory, final boolean fullSize, final HouseholderStep<N, M> householderColumn,
            final HouseholderStep<N, M> householderRow) {
        super(factory);
        myFullSize = fullSize;
        myHouseholderColumn = householderColumn;
        myHouseholderRow = householderRow;
    }

    @Override
    public boolean decompose(final Access2D.Collectable<N, ? super TransformableRegion<N>> matrix) {

        this.reset();

        M storage = this.setInPlace(matrix);

        int tmpRowDim = this.getRowDim();
        int tmpColDim = this.getColDim();

        int tmpLimit = Math.min(tmpRowDim, tmpColDim);

        Householder<N> tmpHouseholderRow = this.makeHouseholder(tmpColDim);
        Householder<N> tmpHouseholderCol = this.makeHouseholder(tmpRowDim);

        if (myBetaLQ == null || myBetaLQ.length != tmpLimit) {
            myBetaLQ = new double[tmpLimit];
            myBetaRQ = new double[tmpLimit];
        }

        if (this.isAspectRatioNormal()) {

            for (int ij = 0; ij < tmpLimit; ij++) {

                if (ij + 1 < tmpRowDim && myHouseholderColumn.invoke(storage, ij, ij, tmpHouseholderCol)) {
                    storage.transformLeft(tmpHouseholderCol, ij + 1);
                    myBetaLQ[ij] = InPlaceDecomposition.getBeta(tmpHouseholderCol);
                } else {
                    myBetaLQ[ij] = PrimitiveMath.ZERO;
                }

                if (ij + 2 < tmpColDim && myHouseholderRow.invoke(storage, ij, ij + 1, tmpHouseholderRow)) {
                    storage.transformRight(tmpHouseholderRow, ij + 1);
                    myBetaRQ[ij] = InPlaceDecomposition.getBeta(tmpHouseholderRow);
                } else {
                    myBetaRQ[ij] = PrimitiveMath.ZERO;
                }
            }

            Array1D<N>[] tmpInitDiags = this.makeReal();
            if (tmpInitDiags != null) {
                myInitDiagLQ = tmpInitDiags[0];
                myInitDiagRQ = tmpInitDiags[1];
            }

        } else {

            for (int ij = 0; ij < tmpLimit; ij++) {

                if (ij + 1 < tmpColDim && myHouseholderRow.invoke(storage, ij, ij, tmpHouseholderRow)) {
                    storage.transformRight(tmpHouseholderRow, ij + 1);
                    myBetaRQ[ij] = InPlaceDecomposition.getBeta(tmpHouseholderRow);
                } else {
                    myBetaRQ[ij] = PrimitiveMath.ZERO;
                }

                if (ij + 2 < tmpRowDim && myHouseholderColumn.invoke(storage, ij + 1, ij, tmpHouseholderCol)) {
                    storage.transformLeft(tmpHouseholderCol, ij + 1);
                    myBetaLQ[ij] = InPlaceDecomposition.getBeta(tmpHouseholderCol);
                } else {
                    myBetaLQ[ij] = PrimitiveMath.ZERO;
                }
            }

            Array1D<N>[] tmpInitDiags = this.makeReal();
            if (tmpInitDiags != null) {
                myInitDiagLQ = tmpInitDiags[0];
                myInitDiagRQ = tmpInitDiags[1];
            }

        }

        return this.computed(true);
    }

    @Override
    public MatrixStore<N> getD() {
        MatrixStore<N> retVal = this.doGetDiagonal();
        if (myFullSize) {
            if (this.getRowDim() > retVal.getRowDim()) {
                retVal = retVal.below(this.getRowDim() - retVal.countRows());
            } else if (this.getColDim() > retVal.getColDim()) {
                retVal = retVal.right(this.getColDim() - retVal.countColumns());
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
    public boolean isFullSize() {
        return myFullSize;
    }

    @Override
    public boolean isUpper() {
        return this.isAspectRatioNormal();
    }

    @Override
    public void reset() {

        super.reset();

        myLQ = null;
        myRQ = null;
        myDiagonal = null;

        myInitDiagLQ = null;
        myInitDiagRQ = null;
    }

    private DiagonalStore<N, Array1D<N>> makeDiagonal() {

        PhysicalStore<N> storage = this.getInPlace();

        Array1D<N> diagMain = (Array1D<N>) storage.sliceDiagonal(0, 0);
        Array1D<N> diagSuper;
        Array1D<N> diagSub;

        if (this.isAspectRatioNormal()) {
            diagSuper = (Array1D<N>) storage.sliceDiagonal(0, 1);
            diagSub = null;
        } else {
            diagSuper = null;
            diagSub = (Array1D<N>) storage.sliceDiagonal(1, 0);
        }

        return this.makeDiagonal(diagMain).superdiagonal(diagSuper).subdiagonal(diagSub).get();
    }

    private M makeLQ() {

        HouseholderReference<N> tmpReference = HouseholderReference.makeColumn(this.getInPlace());

        int tmpRowDim = this.getRowDim();
        int tmpMinDim = this.getMinDim();

        Householder<N> worker = this.makeHouseholder(tmpRowDim);
        PhysicalStore.Factory<N, ?> factory = this.getInPlace().physical();

        M retVal = null;
        if (myInitDiagLQ != null) {
            retVal = this.makeZero(tmpRowDim, myFullSize ? tmpRowDim : tmpMinDim);
            for (int ij = 0; ij < tmpMinDim; ij++) {
                retVal.set(ij, ij, myInitDiagLQ.get(ij));
            }
        } else {
            retVal = this.makeEye(tmpRowDim, myFullSize ? tmpRowDim : tmpMinDim);
        }

        boolean tmpUpper = this.isUpper();
        for (int ij = tmpUpper && tmpRowDim != tmpMinDim ? tmpMinDim - 1 : tmpMinDim - 2; ij >= 0; ij--) {

            if (myBetaLQ[ij] != PrimitiveMath.ZERO) {
                tmpReference.point(tmpUpper ? ij : ij + 1, ij);
                retVal.transformLeft(InPlaceDecomposition.copyReflector(worker, tmpReference, myBetaLQ[ij], factory), ij);
            }
        }

        return retVal;
    }

    private M makeRQ() {

        HouseholderReference<N> tmpReference = HouseholderReference.makeRow(this.getInPlace());

        int tmpColDim = this.getColDim();
        int tmpMinDim = this.getMinDim();

        Householder<N> worker = this.makeHouseholder(tmpColDim);
        PhysicalStore.Factory<N, ?> factory = this.getInPlace().physical();

        M retVal = null;
        if (myInitDiagRQ != null) {
            retVal = this.makeZero(tmpColDim, myFullSize ? tmpColDim : tmpMinDim);
            for (int ij = 0; ij < tmpMinDim; ij++) {
                retVal.set(ij, ij, myInitDiagRQ.get(ij));
            }
        } else {
            retVal = this.makeEye(tmpColDim, myFullSize ? tmpColDim : tmpMinDim);
        }

        boolean tmpUpper = this.isUpper();
        for (int ij = tmpUpper ? tmpMinDim - 2 : tmpMinDim - 1; ij >= 0; ij--) {

            if (myBetaRQ[ij] != PrimitiveMath.ZERO) {
                tmpReference.point(ij, tmpUpper ? ij + 1 : ij);
                retVal.transformLeft(InPlaceDecomposition.copyReflector(worker, tmpReference, myBetaRQ[ij], factory), ij);
            }
        }

        return retVal;
    }

    /**
     * Will solve the equation system [aMtrxV][aMtrxD][X]=[aMtrxSimilar]<sup>T</sup> and overwrite the
     * solution [X] to [aV].
     */
    private void solve(final PhysicalStore<N> aMtrxV, final MatrixStore<N> aMtrxD, final DiagonalStore<N, ?> aMtrxSimilar) {

        int tmpDim = (int) aMtrxV.countRows();
        int tmpLim = tmpDim - 1;

        double tmpSingular;
        for (int j = 0; j < tmpDim; j++) {
            tmpSingular = aMtrxD.doubleValue(j, j);
            double value = tmpSingular;
            if (PrimitiveScalar.isSmall(PrimitiveMath.ONE, value)) {
                for (int i = 0; i < tmpDim; i++) {
                    aMtrxV.set(i, j, PrimitiveMath.ZERO);
                }
            } else {
                for (int i = 0; i < tmpLim; i++) {
                    aMtrxV.set(i, j,
                            (aMtrxSimilar.doubleValue(i, i) * aMtrxV.doubleValue(i, j) + aMtrxSimilar.doubleValue(i, i + 1) * aMtrxV.doubleValue(i + 1, j))
                                    / tmpSingular);
                }
                aMtrxV.set(tmpLim, j, aMtrxSimilar.doubleValue(tmpLim, tmpLim) * aMtrxV.doubleValue(tmpLim, j) / tmpSingular);
            }
        }
    }

    private PhysicalStore<N> solve2(final PhysicalStore<N> aMtrxV, final MatrixStore<N> aMtrxD, final DiagonalStore<N, ?> aMtrxSimilar) {

        int tmpDim = (int) aMtrxV.countRows();
        int tmpLim = tmpDim - 1;

        PhysicalStore<N> retVal = this.makeZero(tmpDim, tmpDim);

        double tmpSingular;
        for (int j = 0; j < tmpDim; j++) {
            tmpSingular = aMtrxD.doubleValue(j, j);
            double value = tmpSingular;
            if (PrimitiveScalar.isSmall(PrimitiveMath.ONE, value)) {
                for (int i = 0; i < tmpDim; i++) {
                    retVal.set(i, j, aMtrxV.doubleValue(i, j));
                }
            } else {
                for (int i = 0; i < tmpLim; i++) {
                    retVal.set(i, j,
                            (aMtrxSimilar.doubleValue(i, i) * aMtrxV.doubleValue(i, j) + aMtrxSimilar.doubleValue(i, i + 1) * aMtrxV.doubleValue(i + 1, j))
                                    / tmpSingular);
                }
                retVal.set(tmpLim, j, aMtrxSimilar.doubleValue(tmpLim, tmpLim) * aMtrxV.doubleValue(tmpLim, j) / tmpSingular);
            }
        }

        return retVal;
    }

    DiagonalStore<N, Array1D<N>> doGetDiagonal() {
        if (myDiagonal == null) {
            myDiagonal = this.makeDiagonal();
        }
        return myDiagonal;
    }

    M doGetLQ() {
        if (myLQ == null) {
            myLQ = this.makeLQ();
        }
        return myLQ;
    }

    M doGetRQ() {
        if (myRQ == null) {
            myRQ = this.makeRQ();
        }
        return myRQ;
    }

    abstract Array1D<N>[] makeReal();

}
