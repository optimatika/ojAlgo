/*
 * Copyright 1997-2018 Optimatika
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

import org.ojalgo.RecoverableCondition;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.Access2D.Collectable;
import org.ojalgo.access.Structure2D;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.HouseholderReference;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;

abstract class QRDecomposition<N extends Number> extends InPlaceDecomposition<N> implements QR<N> {

    static final class Big extends QRDecomposition<BigDecimal> {

        Big() {
            super(BigDenseStore.FACTORY);
        }

    }

    static final class Complex extends QRDecomposition<ComplexNumber> {

        Complex() {
            super(GenericDenseStore.COMPLEX);
        }

    }

    static final class Primitive extends QRDecomposition<Double> {

        Primitive() {
            super(PrimitiveDenseStore.FACTORY);
        }

    }

    static final class Quat extends QRDecomposition<Quaternion> {

        Quat() {
            super(GenericDenseStore.QUATERNION);
        }

    }

    static final class Rational extends QRDecomposition<RationalNumber> {

        Rational() {
            super(GenericDenseStore.RATIONAL);
        }

    }

    private boolean myFullSize = false;
    private int myNumberOfHouseholderTransformations = 0;

    protected QRDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory) {
        super(aFactory);
    }

    public N calculateDeterminant(final Access2D<?> matrix) {
        this.decompose(this.wrap(matrix));
        return this.getDeterminant();
    }

    public boolean decompose(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix) {

        this.reset();

        final DecompositionStore<N> tmpStore = this.setInPlace(matrix);

        final int tmpRowDim = this.getRowDim();
        final int tmpColDim = this.getColDim();

        final Householder<N> tmpHouseholder = this.makeHouseholder(tmpRowDim);

        final int tmpLimit = Math.min(tmpRowDim, tmpColDim);

        for (int ij = 0; ij < tmpLimit; ij++) {
            if (((ij + 1) < tmpRowDim) && tmpStore.generateApplyAndCopyHouseholderColumn(ij, ij, tmpHouseholder)) {
                tmpStore.transformLeft(tmpHouseholder, ij + 1);
                myNumberOfHouseholderTransformations++;
            }
        }

        return this.computed(true);
    }

    public N getDeterminant() {

        final AggregatorFunction<N> aggregator = this.aggregator().product();

        this.getInPlace().visitDiagonal(aggregator);

        if ((myNumberOfHouseholderTransformations % 2) == 1) {
            return this.scalar().one().negate().multiply(aggregator.get()).get();
        } else {
            return aggregator.get();
        }
    }

    @Override
    public MatrixStore<N> getInverse(final PhysicalStore<N> preallocated) {
        return this.getSolution(this.makeIdentity(this.getRowDim()), preallocated);
    }

    public MatrixStore<N> getQ() {

        final DecompositionStore<N> retVal = this.makeEye(this.getRowDim(), myFullSize ? this.getRowDim() : this.getMinDim());

        final HouseholderReference<N> tmpReference = HouseholderReference.makeColumn(this.getInPlace());

        for (int j = this.getMinDim() - 1; j >= 0; j--) {

            tmpReference.point(j, j);

            if (!tmpReference.isZero()) {
                retVal.transformLeft(tmpReference, j);
            }
        }

        return retVal;
    }

    public MatrixStore<N> getR() {

        //MatrixStore<N> retVal = new UpperTriangularStore<N>(this.getInPlace(), false);
        MatrixStore<N> retVal = this.getInPlace().logical().triangular(true, false).get();

        final int tmpPadding = this.getRowDim() - this.getColDim();
        if (myFullSize && (tmpPadding < 0)) {
            retVal = retVal.logical().below(tmpPadding).get();
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

    public MatrixStore<N> getSolution(final Collectable<N, ? super PhysicalStore<N>> rhs) {
        return this.getSolution(rhs, this.preallocate(this.getInPlace(), rhs));
    }

    /**
     * Solve [A]*[X]=[B] by first solving [Q]*[Y]=[B] and then [R]*[X]=[Y]. [X] minimises the 2-norm of
     * [Q]*[R]*[X]-[B].
     *
     * @param rhs The right hand side [B]
     * @return [X] "preallocated" is used to form the results, but the solution is in the returned
     *         MatrixStore.
     */
    @Override
    public MatrixStore<N> getSolution(final Collectable<N, ? super PhysicalStore<N>> rhs, final PhysicalStore<N> preallocated) {

        rhs.supplyTo(preallocated);

        final DecompositionStore<N> tmpStore = this.getInPlace();
        final int tmpRowDim = this.getRowDim();
        final int tmpColDim = this.getColDim();

        final HouseholderReference<N> tmpReference = HouseholderReference.makeColumn(tmpStore);

        final int tmpLimit = this.getMinDim();
        for (int j = 0; j < tmpLimit; j++) {

            tmpReference.point(j, j);

            if (!tmpReference.isZero()) {
                preallocated.transformLeft(tmpReference, 0);
            }
        }

        preallocated.substituteBackwards(tmpStore, false, false, false);

        if (tmpColDim < tmpRowDim) {
            return preallocated.logical().limits(tmpColDim, (int) preallocated.countColumns()).get();
        } else if (tmpColDim > tmpRowDim) {
            return preallocated.logical().below(tmpColDim - tmpRowDim).get();
        } else {
            return preallocated;
        }
    }

    public final MatrixStore<N> invert(final Access2D<?> original) throws RecoverableCondition {

        this.decompose(this.wrap(original));

        if (this.isSolvable()) {
            return this.getInverse();
        } else {
            throw RecoverableCondition.newMatrixNotInvertible();
        }
    }

    public final MatrixStore<N> invert(final Access2D<?> original, final PhysicalStore<N> preallocated) throws RecoverableCondition {

        this.decompose(this.wrap(original));

        if (this.isSolvable()) {
            return this.getInverse(preallocated);
        } else {
            throw RecoverableCondition.newMatrixNotInvertible();
        }
    }

    /**
     * @see org.ojalgo.matrix.decomposition.QR#isFullColumnRank()
     */
    public boolean isFullRank() {
        return this.getRank() == this.getMinDim();
    }

    public boolean isFullSize() {
        return myFullSize;
    }

    public PhysicalStore<N> preallocate(final Structure2D template) {
        final long tmpCountRows = template.countRows();
        return this.allocate(tmpCountRows, tmpCountRows);
    }

    public PhysicalStore<N> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        return this.allocate(templateBody.countRows(), templateRHS.countColumns());
    }

    @Override
    public void reset() {

        super.reset();

        myNumberOfHouseholderTransformations = 0;
    }

    public void setFullSize(final boolean fullSize) {
        myFullSize = fullSize;
    }

    public MatrixStore<N> solve(final Access2D<?> body, final Access2D<?> rhs) throws RecoverableCondition {

        this.decompose(this.wrap(body));

        if (this.isSolvable()) {
            return this.getSolution(this.wrap(rhs));
        } else {
            throw RecoverableCondition.newEquationSystemNotSolvable();
        }
    }

    public MatrixStore<N> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<N> preallocated) throws RecoverableCondition {

        this.decompose(this.wrap(body));

        if (this.isSolvable()) {
            return this.getSolution(this.wrap(rhs), preallocated);
        } else {
            throw RecoverableCondition.newEquationSystemNotSolvable();
        }
    }

    @Override
    protected boolean checkSolvability() {
        return this.isComputed() && this.isFullColumnRank();
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
