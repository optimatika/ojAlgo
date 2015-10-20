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
import org.ojalgo.access.Structure2D;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

abstract class QRDecomposition<N extends Number> extends InPlaceDecomposition<N> implements QR<N> {

    static final class Big extends QRDecomposition<BigDecimal> {

        Big() {
            super(BigDenseStore.FACTORY);
        }

    }

    static final class Complex extends QRDecomposition<ComplexNumber> {

        Complex() {
            super(ComplexDenseStore.FACTORY);
        }

    }

    static final class Primitive extends QRDecomposition<Double> {

        Primitive() {
            super(PrimitiveDenseStore.FACTORY);
        }

    }

    private boolean myFullSize = false;

    protected QRDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory) {
        super(aFactory);
    }

    public N calculateDeterminant(final Access2D<?> matrix) {
        this.decompose(this.wrap(matrix));
        return this.getDeterminant();
    }

    public boolean decompose(final ElementsSupplier<N> matrix) {

        this.reset();

        final DecompositionStore<N> tmpStore = this.setInPlace(matrix);

        final int tmpRowDim = this.getRowDim();
        final int tmpColDim = this.getColDim();

        final Householder<N> tmpHouseholder = this.makeHouseholder(tmpRowDim);

        final int tmpLimit = Math.min(tmpRowDim, tmpColDim);

        for (int ij = 0; ij < tmpLimit; ij++) {
            if (((ij + 1) < tmpRowDim) && tmpStore.generateApplyAndCopyHouseholderColumn(ij, ij, tmpHouseholder)) {
                tmpStore.transformLeft(tmpHouseholder, ij + 1);
            }
        }

        return this.computed(true);
    }

    public boolean equals(final MatrixStore<N> aStore, final NumberContext context) {
        return MatrixUtils.equals(aStore, this, context);
    }

    public N getDeterminant() {

        final AggregatorFunction<N> tmpAggrFunc = this.aggregator().product();

        this.getInPlace().visitDiagonal(0, 0, tmpAggrFunc);

        return tmpAggrFunc.getNumber();
    }

    @Override
    public MatrixStore<N> getInverse(final DecompositionStore<N> preallocated) {
        return this.solve(this.makeIdentity(this.getRowDim()), preallocated);
    }

    public MatrixStore<N> getQ() {

        final DecompositionStore<N> retVal = this.makeEye(this.getRowDim(), myFullSize ? this.getRowDim() : this.getMinDim());

        final DecompositionStore.HouseholderReference<N> tmpReference = new DecompositionStore.HouseholderReference<N>(this.getInPlace(), true);

        for (int j = this.getMinDim() - 1; j >= 0; j--) {

            tmpReference.row = j;
            tmpReference.col = j;

            if (!tmpReference.isZero()) {
                retVal.transformLeft(tmpReference, j);
            }
        }

        return retVal;
    }

    public MatrixStore<N> getR() {

        //MatrixStore<N> retVal = new UpperTriangularStore<N>(this.getInPlace(), false);
        MatrixStore<N> retVal = this.getInPlace().builder().triangular(true, false).build();

        final int tmpPadding = this.getRowDim() - this.getColDim();
        if (myFullSize && (tmpPadding < 0)) {
            retVal = retVal.builder().below(tmpPadding).build();
        }

        return retVal;
    }

    public int getRank() {

        int retVal = 0;

        final DecompositionStore<N> tmpInPlace = this.getInPlace();

        final AggregatorFunction<N> tmpLargest = this.aggregator().largest();
        tmpInPlace.visitDiagonal(0L, 0L, tmpLargest);
        final double tmpLargestValue = tmpLargest.doubleValue();

        final int tmpMinDim = this.getMinDim();

        for (int ij = 0; ij < tmpMinDim; ij++) {
            if (!tmpInPlace.isSmall(ij, ij, tmpLargestValue)) {
                retVal++;
            }
        }

        return retVal;
    }

    public MatrixStore<N> invert(final Access2D<?> original) {
        this.decompose(this.wrap(original));
        return this.getInverse();
    }

    public MatrixStore<N> invert(final Access2D<?> original, final DecompositionStore<N> preallocated) {
        this.decompose(this.wrap(original));
        return this.getInverse(preallocated);
    }

    /**
     * @see org.ojalgo.matrix.decomposition.QR#isFullColumnRank()
     */
    public boolean isFullColumnRank() {
        return this.getRank() == this.getMinDim();
    }

    public boolean isFullSize() {
        return myFullSize;
    }

    public boolean isSolvable() {
        return this.isComputed() && this.isFullColumnRank();
    }

    public DecompositionStore<N> preallocate(final Structure2D template) {
        final long tmpCountRows = template.countRows();
        return this.preallocate(tmpCountRows, tmpCountRows);
    }

    public DecompositionStore<N> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        return this.preallocate(templateRHS.countRows(), templateRHS.countColumns());
    }

    public void setFullSize(final boolean fullSize) {
        myFullSize = fullSize;
    }

    public MatrixStore<N> solve(final Access2D<?> body, final Access2D<?> rhs) {
        this.decompose(this.wrap(body));
        return this.solve(this.wrap(rhs));
    }

    public MatrixStore<N> solve(final Access2D<?> body, final Access2D<?> rhs, final DecompositionStore<N> preallocated) {
        this.decompose(this.wrap(body));
        return this.solve(rhs, preallocated);
    }

    public MatrixStore<N> solve(final ElementsSupplier<N> rhs) {
        return this.solve(rhs, this.preallocate(this.getInPlace(), rhs));
    }

    /**
     * Solve [A]*[X]=[B] by first solving [Q]*[Y]=[B] and then [R]*[X]=[Y]. [X] minimises the 2-norm of
     * [Q]*[R]*[X]-[B].
     *
     * @param rhs The right hand side [B]
     * @return [X] "preallocated" is used to form the results, but the solution is in the returned
     *         MatrixStore.
     * @see org.ojalgo.matrix.decomposition.GenericDecomposition#doSolve(ElementsSupplier,
     *      org.ojalgo.matrix.decomposition.DecompositionStore)
     */
    @Override
    public MatrixStore<N> solve(final ElementsSupplier<N> rhs, final DecompositionStore<N> preallocated) {

        rhs.supplyTo(preallocated);

        final DecompositionStore<N> tmpStore = this.getInPlace();
        final int tmpRowDim = this.getRowDim();
        final int tmpColDim = this.getColDim();

        final DecompositionStore.HouseholderReference<N> tmpReference = new DecompositionStore.HouseholderReference<N>(tmpStore, true);

        final int tmpLimit = this.getMinDim();
        for (int j = 0; j < tmpLimit; j++) {

            tmpReference.row = j;
            tmpReference.col = j;

            if (!tmpReference.isZero()) {
                preallocated.transformLeft(tmpReference, 0);
            }
        }

        preallocated.substituteBackwards(tmpStore, false, false, false);

        if (tmpColDim < tmpRowDim) {
            return preallocated.builder().rows(0, tmpColDim).build();
        } else if (tmpColDim > tmpRowDim) {
            return preallocated.builder().below(tmpColDim - tmpRowDim).build();
        } else {
            return preallocated;
        }
    }

    /**
     * @return L as in R<sup>T</sup>.
     */
    protected DecompositionStore<N> getL() {

        final int tmpRowDim = this.getColDim();
        final int tmpColDim = this.getMinDim();

        final DecompositionStore<N> retVal = this.makeZero(tmpRowDim, tmpColDim);

        final DecompositionStore<N> tmpStore = this.getInPlace();
        for (int j = 0; j < tmpColDim; j++) {
            for (int i = j; i < tmpRowDim; i++) {
                retVal.set(i, j, tmpStore.get(j, i));
            }
        }

        return retVal;
    }

}
