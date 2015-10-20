/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import org.ojalgo.access.Access2D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.Array2D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.MatrixStore.Builder;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

abstract class TridiagonalDecomposition<N extends Number> extends InPlaceDecomposition<N> implements Tridiagonal<N> {

    static final class Big extends TridiagonalDecomposition<BigDecimal> {

        Big() {
            super(BigDenseStore.FACTORY);
        }

        @Override
        Array1D<BigDecimal> makeReal(final DiagonalAccess<BigDecimal> aDiagonalAccessD) {
            return null;
        }
    }

    static final class Complex extends TridiagonalDecomposition<ComplexNumber> {

        Complex() {
            super(ComplexDenseStore.FACTORY);
        }

        @Override
        Array1D<ComplexNumber> makeReal(final DiagonalAccess<ComplexNumber> aDiagonalAccessD) {

            final Array1D<ComplexNumber> retVal = Array1D.COMPLEX.makeZero(aDiagonalAccessD.getDimension());
            retVal.fillAll(ComplexNumber.ONE);

            final Array1D<ComplexNumber> tmpSubdiagonal = aDiagonalAccessD.subdiagonal; // superDiagonal should be the conjugate of this but it is set to the saem value

            ComplexNumber tmpVal = null;
            for (int i = 0; i < tmpSubdiagonal.length; i++) {

                tmpVal = tmpSubdiagonal.get(i).signum();

                if (!tmpVal.isReal()) {

                    tmpSubdiagonal.set(i, tmpSubdiagonal.get(i).divide(tmpVal));

                    if ((i + 1) < tmpSubdiagonal.length) {
                        tmpSubdiagonal.set(i + 1, tmpSubdiagonal.get(i + 1).multiply(tmpVal));
                    }

                    retVal.set(i + 1, tmpVal);
                }
            }

            return retVal;
        }

    }

    static final class Primitive extends TridiagonalDecomposition<Double> {

        Primitive() {
            super(PrimitiveDenseStore.FACTORY);
        }

        @Override
        Array1D<Double> makeReal(final DiagonalAccess<Double> aDiagonalAccessD) {
            return null;
        }
    }

    private transient MatrixStore<N> myD = null;
    private DiagonalAccess<N> myDiagonalAccessD = null;
    private Array1D<N> myInitDiagQ = null;
    private transient MatrixStore<N> myQ = null;

    protected TridiagonalDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory) {
        super(aFactory);
    }

    public final boolean decompose(final ElementsSupplier<N> matrix) {

        this.reset();

        boolean retVal = false;

        try {

            final int tmpRowDim = (int) matrix.countRows(); // Which is also the col-dim.

            final Builder<N> aTriangularMtrx = matrix.get().builder().triangular(false, false);

            final DecompositionStore<N> tmpInPlace = this.setInPlace(aTriangularMtrx);

            final Householder<N> tmpHouseholder = this.makeHouseholder(tmpRowDim);

            final int tmpLimit = tmpRowDim - 2;
            for (int ij = 0; ij < tmpLimit; ij++) {
                if (tmpInPlace.generateApplyAndCopyHouseholderColumn(ij + 1, ij, tmpHouseholder)) {
                    tmpInPlace.transformSymmetric(tmpHouseholder);
                }
            }
            final Array2D<N> tmpArray2D = this.getInPlace().asArray2D();

            final Array1D<N> tmpMain = tmpArray2D.sliceDiagonal(0, 0).copy();
            final Array1D<N> tmpSub = tmpArray2D.sliceDiagonal(1, 0).copy(); // Super differs only in possible conjugate values

            myDiagonalAccessD = new DiagonalAccess<N>(tmpMain, tmpSub, tmpSub, this.scalar().zero().getNumber());
            myInitDiagQ = this.makeReal(myDiagonalAccessD);

            retVal = true;

        } catch (final Exception anException) {

            BasicLogger.error(anException.toString());

            this.reset();

            retVal = false;
        }

        return this.computed(retVal);
    }

    public final boolean equals(final MatrixStore<N> aStore, final NumberContext context) {
        return AccessUtils.equals(this.reconstruct(), aStore, context);
    }

    public final MatrixStore<N> getD() {

        if (myD == null) {
            myD = this.makeD();
        }

        return myD;
    }

    public final MatrixStore<N> getQ() {

        if (myQ == null) {
            myQ = this.makeQ();
        }

        return myQ;
    }

    public final boolean isFullSize() {
        return true;
    }

    public final boolean isSolvable() {
        return false;
    }

    @Override
    public void reset() {

        super.reset();

        myDiagonalAccessD = null;
        myD = null;
        myQ = null;

        myInitDiagQ = null;
    }

    public MatrixStore<N> solve(final Access2D<N> rhs, final DecompositionStore<N> preallocated) {
        throw new UnsupportedOperationException();
    }

    protected final DiagonalAccess<N> getDiagonalAccessD() {
        if (myDiagonalAccessD != null) {
            return myDiagonalAccessD;
        } else {
            throw new IllegalStateException("Decomposition not calculated!");
        }
    }

    protected final MatrixStore<N> makeD() {
        return this.wrap(this.getDiagonalAccessD()).get();
    }

    protected final DecompositionStore<N> makeQ() {

        final DecompositionStore<N> retVal = this.getInPlace();
        final int tmpDim = (int) Math.min(retVal.countRows(), retVal.countColumns());

        final DecompositionStore.HouseholderReference<N> tmpHouseholderReference = new DecompositionStore.HouseholderReference<N>(retVal, true);

        if (myInitDiagQ != null) {
            retVal.set(tmpDim - 1, tmpDim - 1, myInitDiagQ.get(tmpDim - 1));
            retVal.set(tmpDim - 2, tmpDim - 2, myInitDiagQ.get(tmpDim - 2));
        } else {
            retVal.set(tmpDim - 1, tmpDim - 1, PrimitiveMath.ONE);
            retVal.set(tmpDim - 2, tmpDim - 2, PrimitiveMath.ONE);
        }
        retVal.set(tmpDim - 1, tmpDim - 2, PrimitiveMath.ZERO);

        for (int ij = tmpDim - 3; ij >= 0; ij--) {

            tmpHouseholderReference.row = ij + 1;
            tmpHouseholderReference.col = ij;

            if (!tmpHouseholderReference.isZero()) {
                retVal.transformLeft(tmpHouseholderReference, ij);
            }

            retVal.setToIdentity(ij);
            if (myInitDiagQ != null) {
                retVal.set(ij, ij, myInitDiagQ.get(ij));
            }
        }

        return retVal;
    }

    final DecompositionStore<N> doQ() {
        return (DecompositionStore<N>) this.getQ();
    }

    abstract Array1D<N> makeReal(final DiagonalAccess<N> aDiagonalAccessD);

}
