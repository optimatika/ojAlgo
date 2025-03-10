/*
 * Copyright 1997-2025 Optimatika
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

import static org.ojalgo.function.constant.PrimitiveMath.MACHINE_SMALLEST;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.HouseholderReference;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.type.NumberDefinition;

abstract class DenseQR<N extends Comparable<N>> extends InPlaceDecomposition<N> implements QR<N> {

    static final class C128 extends DenseQR<ComplexNumber> {

        C128() {
            this(false);
        }

        C128(final boolean fullSize) {
            super(GenericStore.C128, fullSize);
        }

    }

    static final class H256 extends DenseQR<Quaternion> {

        H256() {
            this(false);
        }

        H256(final boolean fullSize) {
            super(GenericStore.H256, fullSize);
        }

    }

    static final class Q128 extends DenseQR<RationalNumber> {

        Q128() {
            this(false);
        }

        Q128(final boolean fullSize) {
            super(GenericStore.Q128, fullSize);
        }

    }

    static final class R064 extends DenseQR<Double> {

        R064() {
            this(false);
        }

        R064(final boolean fullSize) {
            super(R064Store.FACTORY, fullSize);
        }

    }

    static final class R128 extends DenseQR<Quadruple> {

        R128() {
            this(false);
        }

        R128(final boolean fullSize) {
            super(GenericStore.R128, fullSize);
        }

    }

    private final boolean myFullSize;
    private int myNumberOfHouseholderTransformations = 0;

    protected DenseQR(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> factory, final boolean fullSize) {
        super(factory);
        myFullSize = fullSize;
    }

    @Override
    public void btran(final PhysicalStore<N> arg) {

        DecompositionStore<N> body = this.getInPlace();

        arg.substituteForwards(body, false, true, false);

        HouseholderReference<N> reference = HouseholderReference.makeColumn(body);

        for (int j = this.getMinDim() - 1; j >= 0; j--) {

            reference.point(j, j);

            if (!reference.isZero()) {
                arg.transformLeft(reference, 0);
            }
        }
    }

    @Override
    public N calculateDeterminant(final Access2D<?> matrix) {
        this.decompose(this.wrap(matrix));
        return this.getDeterminant();
    }

    @Override
    public int countSignificant(final double threshold) {

        DecompositionStore<N> internal = this.getInPlace();

        int significant = 0;
        for (int ij = 0, limit = this.getMinDim(); ij < limit; ij++) {
            if (Math.abs(internal.doubleValue(ij, ij)) > threshold) {
                significant++;
            }
        }

        return significant;
    }

    @Override
    public boolean decompose(final Access2D.Collectable<N, ? super TransformableRegion<N>> matrix) {

        this.reset();

        DecompositionStore<N> tmpStore = this.setInPlace(matrix);

        int m = this.getRowDim();
        int r = this.getMinDim();

        Householder<N> tmpHouseholder = this.makeHouseholder(m);

        for (int k = 0; k < r; k++) {
            if (k + 1 < m && tmpStore.generateApplyAndCopyHouseholderColumn(k, k, tmpHouseholder)) {
                tmpStore.transformLeft(tmpHouseholder, k + 1);
                myNumberOfHouseholderTransformations++;
            }
        }

        return this.computed(true);
    }

    @Override
    public N getDeterminant() {

        AggregatorFunction<N> aggregator = this.aggregator().product();

        this.getInPlace().visitDiagonal(aggregator);

        if (myNumberOfHouseholderTransformations % 2 != 0) {
            return this.scalar().one().negate().multiply(aggregator.get()).get();
        }

        return aggregator.get();
    }

    @Override
    public MatrixStore<N> getInverse(final PhysicalStore<N> preallocated) {
        return this.getSolution(this.makeIdentity(this.getRowDim()), preallocated);
    }

    @Override
    public MatrixStore<N> getQ() {

        DecompositionStore<N> retVal = this.makeEye(this.getRowDim(), myFullSize ? this.getRowDim() : this.getMinDim());

        HouseholderReference<N> tmpReference = HouseholderReference.makeColumn(this.getInPlace());

        for (int j = this.getMinDim() - 1; j >= 0; j--) {

            tmpReference.point(j, j);

            if (!tmpReference.isZero()) {
                retVal.transformLeft(tmpReference, j);
            }
        }

        return retVal;
    }

    @Override
    public MatrixStore<N> getR() {

        MatrixStore<N> logical = this.getInPlace().triangular(true, false);

        int nbRows = this.getRowDim();
        int nbCols = this.getColDim();

        if (!myFullSize && nbRows > nbCols) {
            return logical.limits(nbCols, -1);
        }

        return logical;
    }

    @Override
    public double getRankThreshold() {

        N largest = this.getInPlace().aggregateDiagonal(Aggregator.LARGEST);
        double epsilon = this.getDimensionalEpsilon();

        return epsilon * Math.max(MACHINE_SMALLEST, NumberDefinition.doubleValue(largest));
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

        DecompositionStore<N> body = this.getInPlace();
        int m = this.getRowDim();
        int n = this.getColDim();

        HouseholderReference<N> reference = HouseholderReference.makeColumn(body);

        for (int j = 0, limit = this.getMinDim(); j < limit; j++) {

            reference.point(j, j);

            if (!reference.isZero()) {
                preallocated.transformLeft(reference, 0);
            }
        }

        preallocated.substituteBackwards(body, false, false, false);

        if (n < m) {
            return preallocated.limits(n, preallocated.getColDim());
        } else if (n > m) {
            return preallocated.below(n - m);
        } else {
            return preallocated;
        }
    }

    @Override
    public MatrixStore<N> invert(final Access2D<?> original) throws RecoverableCondition {

        this.decompose(this.wrap(original));

        if (this.isSolvable()) {
            return this.getInverse();
        }
        throw RecoverableCondition.newMatrixNotInvertible();
    }

    @Override
    public MatrixStore<N> invert(final Access2D<?> original, final PhysicalStore<N> preallocated) throws RecoverableCondition {

        this.decompose(this.wrap(original));

        if (this.isSolvable()) {
            return this.getInverse(preallocated);
        }
        throw RecoverableCondition.newMatrixNotInvertible();
    }

    @Override
    public boolean isFullSize() {
        return myFullSize;
    }

    @Override
    public boolean isSolvable() {
        return super.isSolvable();
    }

    @Override
    public PhysicalStore<N> preallocate(final int nbEquations, final int nbVariables, final int nbSolutions) {
        return this.makeZero(nbEquations, nbSolutions);
    }

    @Override
    public void reset() {

        super.reset();

        myNumberOfHouseholderTransformations = 0;
    }

    @Override
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
        return this.isAspectRatioNormal() && this.isFullRank();
    }

    /**
     * @return L as in R<sup>T</sup>.
     */
    protected DecompositionStore<N> getL() {

        int tmpRowDim = this.getColDim();
        int tmpColDim = this.getMinDim();

        DecompositionStore<N> retVal = this.makeZero(tmpRowDim, tmpColDim);

        DecompositionStore<N> tmpStore = this.getInPlace();
        for (int j = 0; j < tmpColDim; j++) {
            for (int i = j; i < tmpRowDim; i++) {
                retVal.set(i, j, tmpStore.get(j, i));
            }
        }

        return retVal;
    }

}
