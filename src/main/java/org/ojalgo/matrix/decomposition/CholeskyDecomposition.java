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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.array.BasicArray;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.structure.Structure2D;

abstract class CholeskyDecomposition<N extends Comparable<N>> extends InPlaceDecomposition<N> implements Cholesky<N> {

    static final class C128 extends CholeskyDecomposition<ComplexNumber> {

        C128() {
            super(GenericStore.C128);
        }

    }

    static final class H256 extends CholeskyDecomposition<Quaternion> {

        H256() {
            super(GenericStore.H256);
        }

    }

    static final class Q128 extends CholeskyDecomposition<RationalNumber> {

        Q128() {
            super(GenericStore.Q128);
        }

    }

    static final class R064 extends CholeskyDecomposition<Double> {

        R064() {
            super(Primitive64Store.FACTORY);
        }

    }

    static final class R128 extends CholeskyDecomposition<Quadruple> {

        R128() {
            super(GenericStore.R128);
        }

    }

    private double myMaxDiag = ONE;
    private double myMinDiag = ZERO;
    private boolean mySPD = false;

    protected CholeskyDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory) {
        super(aFactory);
    }

    public final void btran(final PhysicalStore<N> arg) {

        DecompositionStore<N> body = this.getInPlace();

        arg.substituteForwards(body, false, false, false);
        arg.substituteBackwards(body, false, true, false);
    }

    public N calculateDeterminant(final Access2D<?> matrix) {
        this.decompose(this.wrap(matrix));
        return this.getDeterminant();
    }

    public boolean checkAndDecompose(final MatrixStore<N> matrix) {
        return this.compute(matrix, true);
    }

    public int countSignificant(final double threshold) {

        double minimum = Math.sqrt(threshold);

        DecompositionStore<N> internal = this.getInPlace();

        int significant = 0;
        for (int ij = 0, limit = this.getMinDim(); ij < limit; ij++) {
            if (internal.doubleValue(ij, ij) > minimum) {
                significant++;
            }
        }

        return significant;
    }

    public boolean decompose(final Access2D.Collectable<N, ? super PhysicalStore<N>> aStore) {
        return this.compute(aStore, false);
    }

    public N getDeterminant() {

        AggregatorFunction<N> tmpAggrFunc = this.aggregator().product2();

        this.getInPlace().visitDiagonal(0, 0, tmpAggrFunc);

        return tmpAggrFunc.get();
    }

    @Override
    public MatrixStore<N> getInverse(final PhysicalStore<N> preallocated) {

        DecompositionStore<N> body = this.getInPlace();

        preallocated.substituteForwards(body, false, false, true);
        preallocated.substituteBackwards(body, false, true, true);

        return preallocated.hermitian(false);
    }

    public MatrixStore<N> getL() {
        return this.getInPlace().triangular(false, false);
    }

    public double getRankThreshold() {
        return TEN * myMaxDiag * this.getDimensionalEpsilon();
    }

    public MatrixStore<N> getSolution(final Collectable<N, ? super PhysicalStore<N>> rhs) {
        return this.getSolution(rhs, this.preallocate(this.getInPlace(), rhs));
    }

    /**
     * Solves [this][X] = [rhs] by first solving
     *
     * <pre>
     * [L][Y] = [RHS]
     * </pre>
     *
     * and then
     *
     * <pre>
     * [U][X] = [Y]
     * </pre>
     *
     * .
     *
     * @param rhs The right hand side
     * @return [X] The solution will be written to "preallocated" and then returned.
     */
    @Override
    public MatrixStore<N> getSolution(final Collectable<N, ? super PhysicalStore<N>> rhs, final PhysicalStore<N> preallocated) {

        rhs.supplyTo(preallocated);

        DecompositionStore<N> body = this.getInPlace();

        preallocated.substituteForwards(body, false, false, false);
        preallocated.substituteBackwards(body, false, true, false);

        return preallocated;
    }

    public MatrixStore<N> invert(final Access2D<?> original) throws RecoverableCondition {

        this.decompose(this.wrap(original));

        if (this.isSolvable()) {
            return this.getInverse();
        }
        throw RecoverableCondition.newMatrixNotInvertible();
    }

    public MatrixStore<N> invert(final Access2D<?> original, final PhysicalStore<N> preallocated) throws RecoverableCondition {

        this.decompose(this.wrap(original));

        if (this.isSolvable()) {
            return this.getInverse(preallocated);
        }
        throw RecoverableCondition.newMatrixNotInvertible();
    }

    public boolean isFullSize() {
        return true;
    }

    @Override
    public boolean isSolvable() {
        return super.isSolvable();
    }

    public boolean isSPD() {
        return mySPD;
    }

    public PhysicalStore<N> preallocate(final Structure2D template) {
        long tmpCountRows = template.countRows();
        return this.allocate(tmpCountRows, tmpCountRows);
    }

    public PhysicalStore<N> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        return this.allocate(templateRHS.countRows(), templateRHS.countColumns());
    }

    @Override
    public void reset() {

        super.reset();

        mySPD = false;
    }

    public MatrixStore<N> solve(final Access2D<?> body, final Access2D<?> rhs) throws RecoverableCondition {

        this.decompose(this.wrap(body));

        if (this.isSolvable()) {
            return this.getSolution(this.wrap(rhs));
        }
        throw RecoverableCondition.newEquationSystemNotSolvable();
    }

    public MatrixStore<N> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<N> preallocated) throws RecoverableCondition {

        this.decompose(this.wrap(body));

        if (this.isSolvable()) {
            return this.getSolution(this.wrap(rhs), preallocated);
        }
        throw RecoverableCondition.newEquationSystemNotSolvable();
    }

    @Override
    protected boolean checkSolvability() {
        return mySPD && myMinDiag > this.getRankThreshold();
    }

    boolean compute(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix, final boolean checkHermitian) {

        this.reset();

        DecompositionStore<N> tmpInPlace = this.setInPlace(matrix);

        int tmpRowDim = this.getRowDim();
        int tmpColDim = this.getColDim();
        int tmpMinDim = Math.min(tmpRowDim, tmpColDim);

        // true if (Hermitian) Positive Definite
        boolean tmpPositiveDefinite = tmpRowDim == tmpColDim;
        myMaxDiag = MACHINE_SMALLEST;
        myMinDiag = MACHINE_LARGEST;

        BasicArray<N> tmpMultipliers = this.makeArray(tmpRowDim);

        // Check if hermitian, maybe
        if (tmpPositiveDefinite && checkHermitian) {
            tmpPositiveDefinite &= tmpInPlace.isHermitian();
        }

        UnaryFunction<N> tmpSqrtFunc = this.function().sqrt();

        // Main loop - along the diagonal
        for (int ij = 0; tmpPositiveDefinite && ij < tmpMinDim; ij++) {

            // Do the calculations...
            double tmpVal = tmpInPlace.doubleValue(ij, ij);
            myMaxDiag = MAX.invoke(myMaxDiag, tmpVal);
            myMinDiag = MIN.invoke(myMinDiag, tmpVal);
            if (tmpVal > ZERO) {

                tmpInPlace.modifyOne(ij, ij, tmpSqrtFunc);

                // Calculate multipliers and copy to local column
                // Current column, below the diagonal
                tmpInPlace.divideAndCopyColumn(ij, ij, tmpMultipliers);

                // Remaining columns, below the diagonal
                tmpInPlace.applyCholesky(ij, tmpMultipliers);

            } else {

                tmpPositiveDefinite = false;
            }
        }

        return this.computed(mySPD = tmpPositiveDefinite);
    }

}
