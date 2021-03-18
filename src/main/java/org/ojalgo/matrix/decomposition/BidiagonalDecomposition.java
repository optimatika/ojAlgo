/*
 * Copyright 1997-2021 Optimatika
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
import org.ojalgo.matrix.store.DiagonalStore;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.HouseholderReference;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access2D;

abstract class BidiagonalDecomposition<N extends Comparable<N>> extends InPlaceDecomposition<N> implements Bidiagonal<N> {

    static final class Complex extends BidiagonalDecomposition<ComplexNumber> {

        Complex() {
            this(false);
        }

        Complex(final boolean fullSize) {
            super(GenericStore.COMPLEX, fullSize);
        }

        @Override
        Array1D<ComplexNumber>[] makeReal() {

            final DiagonalStore<ComplexNumber, Array1D<ComplexNumber>> tmpDiagonalAccessD = this.doGetDiagonal();

            final Array1D<ComplexNumber> tmpInitDiagQ1 = Array1D.COMPLEX.makeZero(tmpDiagonalAccessD.getDimension());
            tmpInitDiagQ1.fillAll(ComplexNumber.ONE);

            final Array1D<ComplexNumber> tmpInitDiagQ2 = Array1D.COMPLEX.makeZero(tmpDiagonalAccessD.getDimension());
            tmpInitDiagQ2.fillAll(ComplexNumber.ONE);

            final boolean tmpUpper = this.isUpper();

            if (tmpUpper) {

                final Array1D<ComplexNumber> tmpMainDiagonal = tmpDiagonalAccessD.getMainDiagonal().get();
                final Array1D<ComplexNumber> tmpSuperdiagonal = tmpDiagonalAccessD.getSuperdiagonal().get();

                final int tmpLimit = (int) tmpSuperdiagonal.count();
                for (int i = 0; i < tmpLimit; i++) {

                    if (!tmpMainDiagonal.get(i).isReal()) {
                        final ComplexNumber tmpSignum = tmpMainDiagonal.get(i).signum();
                        tmpMainDiagonal.set(i, tmpMainDiagonal.get(i).divide(tmpSignum));
                        tmpSuperdiagonal.set(i, tmpSuperdiagonal.get(i).divide(tmpSignum));
                        tmpInitDiagQ1.set(i, tmpSignum);
                    }

                    if (!tmpSuperdiagonal.get(i).isReal()) {
                        final ComplexNumber tmpSignum = tmpSuperdiagonal.get(i).signum();
                        tmpSuperdiagonal.set(i, tmpSuperdiagonal.get(i).divide(tmpSignum));
                        tmpMainDiagonal.set(i + 1, tmpMainDiagonal.get(i + 1).divide(tmpSignum));
                        tmpInitDiagQ2.set(i + 1, tmpSignum.conjugate());
                    }
                }

                if (!tmpMainDiagonal.get(tmpLimit).isReal()) {
                    final ComplexNumber tmpSignum = tmpMainDiagonal.get(tmpLimit).signum();
                    tmpMainDiagonal.set(tmpLimit, tmpMainDiagonal.get(tmpLimit).divide(tmpSignum));
                    tmpInitDiagQ1.set(tmpLimit, tmpSignum);
                }

            } else {

                final Array1D<ComplexNumber> tmpMainDiagonal = tmpDiagonalAccessD.getMainDiagonal().get();
                final Array1D<ComplexNumber> tmpSubdiagonal = tmpDiagonalAccessD.getSubdiagonal().get();

                final int tmpLimit = (int) tmpSubdiagonal.count();
                for (int i = 0; i < tmpLimit; i++) {

                    if (!tmpMainDiagonal.get(i).isReal()) {
                        final ComplexNumber tmpSignum = tmpMainDiagonal.get(i).signum();
                        tmpMainDiagonal.set(i, tmpMainDiagonal.get(i).divide(tmpSignum));
                        tmpSubdiagonal.set(i, tmpSubdiagonal.get(i).divide(tmpSignum));
                        tmpInitDiagQ2.set(i, tmpSignum.conjugate());
                    }

                    if (!tmpSubdiagonal.get(i).isReal()) {
                        final ComplexNumber tmpSignum = tmpSubdiagonal.get(i).signum();
                        tmpSubdiagonal.set(i, tmpSubdiagonal.get(i).divide(tmpSignum));
                        tmpMainDiagonal.set(i + 1, tmpMainDiagonal.get(i + 1).divide(tmpSignum));
                        tmpInitDiagQ1.set(i + 1, tmpSignum);
                    }
                }

                if (!tmpMainDiagonal.get(tmpLimit).isReal()) {
                    final ComplexNumber tmpSignum = tmpMainDiagonal.get(tmpLimit).signum();
                    tmpMainDiagonal.set(tmpLimit, tmpMainDiagonal.get(tmpLimit).divide(tmpSignum));
                    tmpInitDiagQ2.set(tmpLimit, tmpSignum.conjugate());
                }

            }

            return new Array1D[] { tmpInitDiagQ1, tmpInitDiagQ2 };
        }

    }

    static final class Primitive extends BidiagonalDecomposition<Double> {

        Primitive() {
            this(false);
        }

        Primitive(final boolean fullSize) {
            super(Primitive64Store.FACTORY, fullSize);
        }

        @Override
        Array1D<Double>[] makeReal() {
            return null;
        }

    }

    static final class Quat extends BidiagonalDecomposition<Quaternion> {

        Quat() {
            this(false);
        }

        Quat(final boolean fullSize) {
            super(GenericStore.QUATERNION, fullSize);
        }

        @Override
        Array1D<Quaternion>[] makeReal() {
            // TODO Implement something similar to what's in "Complex"
            return null;
        }

    }

    static final class Rational extends BidiagonalDecomposition<RationalNumber> {

        Rational() {
            this(false);
        }

        Rational(final boolean fullSize) {
            super(GenericStore.RATIONAL, fullSize);
        }

        @Override
        Array1D<RationalNumber>[] makeReal() {
            return null;
        }

    }

    private transient DiagonalStore<N, Array1D<N>> myDiagonal;
    private final boolean myFullSize;
    private Array1D<N> myInitDiagLQ = null;
    private Array1D<N> myInitDiagRQ = null;
    private transient DecompositionStore<N> myLQ;
    private transient DecompositionStore<N> myRQ;

    protected BidiagonalDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> factory, final boolean fullSize) {
        super(factory);
        myFullSize = fullSize;
    }

    public boolean decompose(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix) {

        this.reset();

        final DecompositionStore<N> storage = this.setInPlace(matrix);

        final int tmpRowDim = this.getRowDim();
        final int tmpColDim = this.getColDim();

        final int tmpLimit = Math.min(tmpRowDim, tmpColDim);

        final Householder<N> tmpHouseholderRow = this.makeHouseholder(tmpColDim);
        final Householder<N> tmpHouseholderCol = this.makeHouseholder(tmpRowDim);

        if (this.isAspectRatioNormal()) {

            for (int ij = 0; ij < tmpLimit; ij++) {

                if (((ij + 1) < tmpRowDim) && storage.generateApplyAndCopyHouseholderColumn(ij, ij, tmpHouseholderCol)) {
                    storage.transformLeft(tmpHouseholderCol, ij + 1);
                }

                if (((ij + 2) < tmpColDim) && storage.generateApplyAndCopyHouseholderRow(ij, ij + 1, tmpHouseholderRow)) {
                    storage.transformRight(tmpHouseholderRow, ij + 1);
                }
            }

            final Array1D<N>[] tmpInitDiags = this.makeReal();
            if (tmpInitDiags != null) {
                myInitDiagLQ = tmpInitDiags[0];
                myInitDiagRQ = tmpInitDiags[1];
            }

        } else {

            for (int ij = 0; ij < tmpLimit; ij++) {

                if (((ij + 1) < tmpColDim) && storage.generateApplyAndCopyHouseholderRow(ij, ij, tmpHouseholderRow)) {
                    storage.transformRight(tmpHouseholderRow, ij + 1);
                }

                if (((ij + 2) < tmpRowDim) && storage.generateApplyAndCopyHouseholderColumn(ij + 1, ij, tmpHouseholderCol)) {
                    storage.transformLeft(tmpHouseholderCol, ij + 1);
                }
            }

            final Array1D<N>[] tmpInitDiags = this.makeReal();
            if (tmpInitDiags != null) {
                myInitDiagLQ = tmpInitDiags[0];
                myInitDiagRQ = tmpInitDiags[1];
            }

        }

        return this.computed(true);
    }

    public MatrixStore<N> getD() {
        MatrixStore<N> retVal = this.getInPlace().logical().bidiagonal(this.isAspectRatioNormal(), false).get();
        if (myFullSize) {
            if (this.getRowDim() > retVal.countRows()) {
                retVal = retVal.logical().below((int) (this.getRowDim() - retVal.countRows())).get();
            } else if (this.getColDim() > retVal.countColumns()) {
                retVal = retVal.logical().right((int) (this.getColDim() - retVal.countColumns())).get();
            }
        }
        return retVal;
    }

    public MatrixStore<N> getLQ() {
        return this.doGetLQ();
    }

    public MatrixStore<N> getRQ() {
        return this.doGetRQ();
    }

    public boolean isFullSize() {
        return myFullSize;
    }

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

        final DecompositionStore<N> storage = this.getInPlace();

        final Array1D<N> diagMain = storage.sliceDiagonal(0, 0);
        Array1D<N> diagSuper;
        Array1D<N> diagSub;

        if (this.isAspectRatioNormal()) {
            diagSuper = storage.sliceDiagonal(0, 1);
            diagSub = null;
        } else {
            diagSuper = null;
            diagSub = storage.sliceDiagonal(1, 0);
        }

        return this.makeDiagonal(diagMain).superdiagonal(diagSuper).subdiagonal(diagSub).get();
    }

    private DecompositionStore<N> makeLQ() {

        final HouseholderReference<N> tmpReference = HouseholderReference.makeColumn(this.getInPlace());

        final int tmpRowDim = this.getRowDim();
        final int tmpMinDim = this.getMinDim();

        DecompositionStore<N> retVal = null;
        if (myInitDiagLQ != null) {
            retVal = this.makeZero(tmpRowDim, myFullSize ? tmpRowDim : tmpMinDim);
            for (int ij = 0; ij < tmpMinDim; ij++) {
                retVal.set(ij, ij, myInitDiagLQ.get(ij));
            }
        } else {
            retVal = this.makeEye(tmpRowDim, myFullSize ? tmpRowDim : tmpMinDim);
        }

        final boolean tmpUpper = this.isUpper();
        for (int ij = (tmpUpper && (tmpRowDim != tmpMinDim)) ? tmpMinDim - 1 : tmpMinDim - 2; ij >= 0; ij--) {

            tmpReference.point(tmpUpper ? ij : ij + 1, ij);

            if (!tmpReference.isZero()) {
                retVal.transformLeft(tmpReference, ij);
            }
        }

        return retVal;
    }

    private DecompositionStore<N> makeRQ() {

        final HouseholderReference<N> tmpReference = HouseholderReference.makeRow(this.getInPlace());

        final int tmpColDim = this.getColDim();
        final int tmpMinDim = this.getMinDim();

        DecompositionStore<N> retVal = null;
        if (myInitDiagRQ != null) {
            retVal = this.makeZero(tmpColDim, myFullSize ? tmpColDim : tmpMinDim);
            for (int ij = 0; ij < tmpMinDim; ij++) {
                retVal.set(ij, ij, myInitDiagRQ.get(ij));
            }
        } else {
            retVal = this.makeEye(tmpColDim, myFullSize ? tmpColDim : tmpMinDim);
        }

        final boolean tmpUpper = this.isUpper();
        for (int ij = tmpUpper ? tmpMinDim - 2 : tmpMinDim - 1; ij >= 0; ij--) {

            tmpReference.point(ij, tmpUpper ? ij + 1 : ij);

            if (!tmpReference.isZero()) {
                retVal.transformLeft(tmpReference, ij);
            }
        }

        return retVal;
    }

    /**
     * Will solve the equation system [aMtrxV][aMtrxD][X]=[aMtrxSimilar]<sup>T</sup> and overwrite the
     * solution [X] to [aV].
     */
    private void solve(final PhysicalStore<N> aMtrxV, final MatrixStore<N> aMtrxD, final DiagonalStore<N, ?> aMtrxSimilar) {

        final int tmpDim = (int) aMtrxV.countRows();
        final int tmpLim = tmpDim - 1;

        double tmpSingular;
        for (int j = 0; j < tmpDim; j++) {
            tmpSingular = aMtrxD.doubleValue(j, j);
            final double value = tmpSingular;
            if (PrimitiveScalar.isSmall(PrimitiveMath.ONE, value)) {
                for (int i = 0; i < tmpDim; i++) {
                    aMtrxV.set(i, j, PrimitiveMath.ZERO);
                }
            } else {
                for (int i = 0; i < tmpLim; i++) {
                    aMtrxV.set(i, j,
                            ((aMtrxSimilar.doubleValue(i, i) * aMtrxV.doubleValue(i, j)) + (aMtrxSimilar.doubleValue(i, i + 1) * aMtrxV.doubleValue(i + 1, j)))
                                    / tmpSingular);
                }
                aMtrxV.set(tmpLim, j, (aMtrxSimilar.doubleValue(tmpLim, tmpLim) * aMtrxV.doubleValue(tmpLim, j)) / tmpSingular);
            }
        }
    }

    private DecompositionStore<N> solve2(final PhysicalStore<N> aMtrxV, final MatrixStore<N> aMtrxD, final DiagonalStore<N, ?> aMtrxSimilar) {

        final int tmpDim = (int) aMtrxV.countRows();
        final int tmpLim = tmpDim - 1;

        final DecompositionStore<N> retVal = this.makeZero(tmpDim, tmpDim);

        double tmpSingular;
        for (int j = 0; j < tmpDim; j++) {
            tmpSingular = aMtrxD.doubleValue(j, j);
            final double value = tmpSingular;
            if (PrimitiveScalar.isSmall(PrimitiveMath.ONE, value)) {
                for (int i = 0; i < tmpDim; i++) {
                    retVal.set(i, j, aMtrxV.doubleValue(i, j));
                }
            } else {
                for (int i = 0; i < tmpLim; i++) {
                    retVal.set(i, j,
                            ((aMtrxSimilar.doubleValue(i, i) * aMtrxV.doubleValue(i, j)) + (aMtrxSimilar.doubleValue(i, i + 1) * aMtrxV.doubleValue(i + 1, j)))
                                    / tmpSingular);
                }
                retVal.set(tmpLim, j, (aMtrxSimilar.doubleValue(tmpLim, tmpLim) * aMtrxV.doubleValue(tmpLim, j)) / tmpSingular);
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

    DecompositionStore<N> doGetLQ() {
        if (myLQ == null) {
            myLQ = this.makeLQ();
        }
        return myLQ;
    }

    DecompositionStore<N> doGetRQ() {
        if (myRQ == null) {
            myRQ = this.makeRQ();
        }
        return myRQ;
    }

    abstract Array1D<N>[] makeReal();

}
