/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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

import java.math.BigDecimal;

import org.ojalgo.array.Array1D;
import org.ojalgo.array.Array2D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.HouseholderReference;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.type.context.NumberContext;

abstract class BidiagonalDecomposition<N extends Number> extends InPlaceDecomposition<N> implements Bidiagonal<N> {

    static final class Big extends BidiagonalDecomposition<BigDecimal> {

        Big() {
            super(BigDenseStore.FACTORY);
        }

        @Override
        Array1D<BigDecimal>[] makeReal() {
            return null;
        }

    }

    static final class Complex extends BidiagonalDecomposition<ComplexNumber> {

        Complex() {
            super(ComplexDenseStore.FACTORY);
        }

        @Override
        Array1D<ComplexNumber>[] makeReal() {

            final DiagonalAccess<ComplexNumber> tmpDiagonalAccessD = this.getDiagonalAccessD();

            final Array1D<ComplexNumber> tmpInitDiagQ1 = Array1D.COMPLEX.makeZero(tmpDiagonalAccessD.getDimension());
            tmpInitDiagQ1.fillAll(ComplexNumber.ONE);

            final Array1D<ComplexNumber> tmpInitDiagQ2 = Array1D.COMPLEX.makeZero(tmpDiagonalAccessD.getDimension());
            tmpInitDiagQ2.fillAll(ComplexNumber.ONE);

            final boolean tmpUpper = this.isUpper();

            if (tmpUpper) {

                final Array1D<ComplexNumber> tmpMainDiagonal = tmpDiagonalAccessD.mainDiagonal;
                final Array1D<ComplexNumber> tmpSuperdiagonal = tmpDiagonalAccessD.superdiagonal;

                final int tmpLimit = tmpSuperdiagonal.size();
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

                final Array1D<ComplexNumber> tmpMainDiagonal = tmpDiagonalAccessD.mainDiagonal;
                final Array1D<ComplexNumber> tmpSubdiagonal = tmpDiagonalAccessD.subdiagonal;

                final int tmpLimit = tmpSubdiagonal.size();
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
            super(PrimitiveDenseStore.FACTORY);
        }

        @Override
        Array1D<Double>[] makeReal() {
            return null;
        }

    }

    private transient DiagonalAccess<N> myDiagonalAccessD;

    private boolean myFullSize = false;

    private Array1D<N> myInitDiagQ1 = null;
    private Array1D<N> myInitDiagQ2 = null;

    private transient DecompositionStore<N> myQ1;
    private transient DecompositionStore<N> myQ2;

    protected BidiagonalDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory) {
        super(aFactory);
    }

    public boolean decompose(final ElementsSupplier<N> matrix) {

        this.reset();

        final DecompositionStore<N> tmpStore = this.setInPlace(matrix);

        final int tmpRowDim = this.getRowDim();
        final int tmpColDim = this.getColDim();

        final int tmpLimit = Math.min(tmpRowDim, tmpColDim);

        final Householder<N> tmpHouseholderRow = this.makeHouseholder(tmpColDim);
        final Householder<N> tmpHouseholderCol = this.makeHouseholder(tmpRowDim);

        if (this.isAspectRatioNormal()) {

            for (int ij = 0; ij < tmpLimit; ij++) {

                if (((ij + 1) < tmpRowDim)
                        && tmpStore.generateApplyAndCopyHouseholderColumn(ij, ij, tmpHouseholderCol)) {
                    tmpStore.transformLeft(tmpHouseholderCol, ij + 1);
                }

                if (((ij + 2) < tmpColDim)
                        && tmpStore.generateApplyAndCopyHouseholderRow(ij, ij + 1, tmpHouseholderRow)) {
                    tmpStore.transformRight(tmpHouseholderRow, ij + 1);
                }
            }

            final Array1D<N>[] tmpInitDiags = this.makeReal();
            if (tmpInitDiags != null) {
                myInitDiagQ1 = tmpInitDiags[0];
                myInitDiagQ2 = tmpInitDiags[1];
            }

        } else {

            for (int ij = 0; ij < tmpLimit; ij++) {

                if (((ij + 1) < tmpColDim) && tmpStore.generateApplyAndCopyHouseholderRow(ij, ij, tmpHouseholderRow)) {
                    tmpStore.transformRight(tmpHouseholderRow, ij + 1);
                }

                if (((ij + 2) < tmpRowDim)
                        && tmpStore.generateApplyAndCopyHouseholderColumn(ij + 1, ij, tmpHouseholderCol)) {
                    tmpStore.transformLeft(tmpHouseholderCol, ij + 1);
                }
            }

            final Array1D<N>[] tmpInitDiags = this.makeReal();
            if (tmpInitDiags != null) {
                myInitDiagQ1 = tmpInitDiags[0];
                myInitDiagQ2 = tmpInitDiags[1];
            }

        }

        return this.computed(true);
    }

    public boolean equals(final MatrixStore<N> aStore, final NumberContext context) {
        return MatrixUtils.equals(aStore, this, context);
    }

    public MatrixStore<N> getD() {
        return this.getInPlace().logical().bidiagonal(this.isAspectRatioNormal(), false).get();
    }

    public MatrixStore<N> getQ1() {
        if (myQ1 == null) {
            myQ1 = this.makeQ1();
        }
        return myQ1;
    }

    public MatrixStore<N> getQ2() {
        if (myQ2 == null) {
            myQ2 = this.makeQ2();
        }
        return myQ2;
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

        myQ1 = null;
        myQ2 = null;
        myDiagonalAccessD = null;

        myInitDiagQ1 = null;
        myInitDiagQ2 = null;
    }

    public void setFullSize(final boolean fullSize) {
        myFullSize = fullSize;
    }

    private DiagonalAccess<N> makeDiagonalAccessD() {

        final Array2D<N> tmpArray2D = this.getInPlace().asArray2D();

        final Array1D<N> tmpMain = tmpArray2D.sliceDiagonal(0, 0);
        Array1D<N> tmpSuper;
        Array1D<N> tmpSub;

        if (this.isAspectRatioNormal()) {
            tmpSuper = tmpArray2D.sliceDiagonal(0, 1);
            tmpSub = null;
        } else {
            tmpSub = tmpArray2D.sliceDiagonal(1, 0);
            tmpSuper = null;
        }

        return new DiagonalAccess<>(tmpMain, tmpSuper, tmpSub, this.scalar().zero().getNumber());
    }

    /**
     * Will solve the equation system
     * [aMtrxV][aMtrxD][X]=[aMtrxSimilar]<sup>T</sup> and overwrite the solution
     * [X] to [aV].
     */
    private void solve(final PhysicalStore<N> aMtrxV, final MatrixStore<N> aMtrxD,
            final DiagonalAccess<N> aMtrxSimilar) {

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
                            ((aMtrxSimilar.doubleValue(i, i) * aMtrxV.doubleValue(i, j))
                                    + (aMtrxSimilar.doubleValue(i, i + 1) * aMtrxV.doubleValue(i + 1, j)))
                                    / tmpSingular);
                }
                aMtrxV.set(tmpLim, j,
                        (aMtrxSimilar.doubleValue(tmpLim, tmpLim) * aMtrxV.doubleValue(tmpLim, j)) / tmpSingular);
            }
        }
    }

    private DecompositionStore<N> solve2(final PhysicalStore<N> aMtrxV, final MatrixStore<N> aMtrxD,
            final DiagonalAccess<N> aMtrxSimilar) {

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
                            ((aMtrxSimilar.doubleValue(i, i) * aMtrxV.doubleValue(i, j))
                                    + (aMtrxSimilar.doubleValue(i, i + 1) * aMtrxV.doubleValue(i + 1, j)))
                                    / tmpSingular);
                }
                retVal.set(tmpLim, j,
                        (aMtrxSimilar.doubleValue(tmpLim, tmpLim) * aMtrxV.doubleValue(tmpLim, j)) / tmpSingular);
            }
        }

        return retVal;
    }

    protected DecompositionStore<N> makeQ1() {

        final HouseholderReference<N> tmpHouseholderReference = new HouseholderReference<>(this.getInPlace(), true);

        final int tmpRowDim = this.getRowDim();
        final int tmpMinDim = this.getMinDim();

        DecompositionStore<N> retVal = null;
        if (myInitDiagQ1 != null) {
            retVal = this.makeZero(tmpRowDim, myFullSize ? tmpRowDim : tmpMinDim);
            for (int ij = 0; ij < tmpMinDim; ij++) {
                retVal.set(ij, ij, myInitDiagQ1.get(ij));
            }
        } else {
            retVal = this.makeEye(tmpRowDim, myFullSize ? tmpRowDim : tmpMinDim);
        }

        final boolean tmpUpper = this.isUpper();
        for (int ij = (tmpUpper && (tmpRowDim != tmpMinDim)) ? tmpMinDim - 1 : tmpMinDim - 2; ij >= 0; ij--) {

            tmpHouseholderReference.row = tmpUpper ? ij : ij + 1;
            tmpHouseholderReference.col = ij;

            if (!tmpHouseholderReference.isZero()) {
                retVal.transformLeft(tmpHouseholderReference, ij);
            }
        }

        return retVal;
    }

    protected DecompositionStore<N> makeQ2() {

        final HouseholderReference<N> tmpHouseholderReference = new HouseholderReference<>(this.getInPlace(), false);

        final int tmpColDim = this.getColDim();
        final int tmpMinDim = this.getMinDim();

        DecompositionStore<N> retVal = null;
        if (myInitDiagQ2 != null) {
            retVal = this.makeZero(tmpColDim, myFullSize ? tmpColDim : tmpMinDim);
            for (int ij = 0; ij < tmpMinDim; ij++) {
                retVal.set(ij, ij, myInitDiagQ2.get(ij));
            }
        } else {
            retVal = this.makeEye(tmpColDim, myFullSize ? tmpColDim : tmpMinDim);
        }

        final boolean tmpUpper = this.isUpper();
        for (int ij = tmpUpper ? tmpMinDim - 2 : tmpMinDim - 1; ij >= 0; ij--) {

            tmpHouseholderReference.row = ij;
            tmpHouseholderReference.col = tmpUpper ? ij + 1 : ij;

            if (!tmpHouseholderReference.isZero()) {
                retVal.transformLeft(tmpHouseholderReference, ij);
            }
        }

        return retVal;
    }

    DiagonalAccess<N> getDiagonalAccessD() {
        if (myDiagonalAccessD == null) {
            myDiagonalAccessD = this.makeDiagonalAccessD();
        }
        return myDiagonalAccessD;
    }

    abstract Array1D<N>[] makeReal();

}
