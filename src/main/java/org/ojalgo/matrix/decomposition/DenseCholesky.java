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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.ArrayList;
import java.util.List;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.array.BasicArray;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.matrix.transformation.InvertibleFactor;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;

abstract class DenseCholesky<N extends Comparable<N>> extends InPlaceDecomposition<N> implements Cholesky<N> {

    static final class C128 extends DenseCholesky<ComplexNumber> {

        C128() {
            super(GenericStore.C128);
        }

    }

    static final class H256 extends DenseCholesky<Quaternion> {

        H256() {
            super(GenericStore.H256);
        }

    }

    static final class Q128 extends DenseCholesky<RationalNumber> {

        Q128() {
            super(GenericStore.Q128);
        }

    }

    static final class R064 extends DenseCholesky<Double> {

        R064() {
            super(R064Store.FACTORY);
        }

    }

    static final class R128 extends DenseCholesky<Quadruple> {

        R128() {
            super(GenericStore.R128);
        }

    }

    /**
     * When growing, a new row/column is rejected if its pivot (the new diagonal element of [L], squared) is
     * not larger than this factor times the new diagonal element of the matrix – the new row/column is then
     * (numerically) linearly dependent on the existing ones.
     */
    private static final double DEPENDENT = 1E-12;

    private double myMaxDiag = ONE;
    private double myMinDiag = ZERO;
    private boolean mySPD = false;

    protected DenseCholesky(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory) {
        super(aFactory);
    }

    @Override
    public boolean appendColumn(final Access1D<N> column) {

        int dim = this.isComputed() ? this.getRowDim() : 0;

        if (column.size() != dim + 1) {
            throw new IllegalArgumentException("The column must be of length " + (dim + 1) + "!");
        }

        Scalar.Factory<N> scalar = this.scalar();
        DecompositionStore<N> current = this.getInPlace();

        // [A] = [L][L]^H, and the new row [l] of [L] (without its diagonal element) is given by [L][l]^H = [a]
        List<Scalar<N>> conjugated = new ArrayList<>(dim);
        double sumOfSquares = ZERO;
        for (int i = 0; i < dim; i++) {
            Scalar<N> value = scalar.convert(column.get(i));
            for (int p = 0; p < i; p++) {
                value = value.subtract(scalar.convert(current.get(i, p)).multiply(conjugated.get(p)));
            }
            value = value.divide(current.doubleValue(i, i));
            conjugated.add(value);
            double norm = value.norm();
            sumOfSquares += norm * norm;
        }

        double diagonal = scalar.convert(column.get(dim)).doubleValue();
        double pivot = diagonal - sumOfSquares;

        if (!(pivot > DEPENDENT * diagonal)) {
            return false;
        }

        DecompositionStore<N> extended = this.makeZero(dim + 1, dim + 1);
        for (int j = 0; j < dim; j++) {
            for (int i = j; i < dim; i++) {
                extended.set(i, j, current.get(i, j));
            }
            extended.set(dim, j, conjugated.get(j).conjugate().get());
        }
        extended.set(dim, dim, SQRT.invoke(pivot));

        this.setInPlace(extended);
        this.updateDiagonalRange();
        mySPD = true;

        return this.computed(true);
    }

    @Override
    public void btran(final double[] arg) {
        this.ftran(arg);
    }

    @Override
    public void btran(final PhysicalStore<N> arg) {
        this.ftran(arg);
    }

    @Override
    public N calculateDeterminant(final Access2D<?> matrix) {
        this.decompose(this.wrap(matrix));
        return this.getDeterminant();
    }

    @Override
    public boolean checkAndDecompose(final MatrixStore<N> matrix) {
        return this.compute(matrix, true);
    }

    @Override
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

    @Override
    public boolean decompose(final Access2D.Collectable<N, ? super TransformableRegion<N>> aStore) {
        return this.compute(aStore, false);
    }

    @Override
    public void ftran(final double[] arg) {

        DecompositionStore<N> body = this.getInPlace();

        body.substituteForwards(false, false, arg);
        body.substituteBackwards(true, false, arg);
    }

    @Override
    public void ftran(final PhysicalStore<N> arg) {

        DecompositionStore<N> body = this.getInPlace();

        body.substituteForwards(false, false, arg);
        body.substituteBackwards(true, false, arg);
    }

    @Override
    public N getDeterminant() {

        AggregatorFunction<N> tmpAggrFunc = this.aggregator().product2();

        this.getInPlace().visitDiagonal(0, 0, tmpAggrFunc);

        return tmpAggrFunc.get();
    }

    @Override
    public List<InvertibleFactor<N>> getFactors() {
        DecompositionStore<N> inPlace = this.getInPlace();
        return List.of(new FactorLower<>(inPlace, false), new FactorUpperConjugate<>(inPlace, false));
    }

    @Override
    public MatrixStore<N> getInverse(final PhysicalStore<N> preallocated) {

        // No need to reset the contents of preallocated

        DecompositionStore<N> body = this.getInPlace();

        // With the last arg true, preallocated is assumed to an identity
        preallocated.substituteForwards(body, false, false, true);
        preallocated.substituteBackwards(body, false, true, true);

        return preallocated.hermitian(false);
    }

    @Override
    public MatrixStore<N> getL() {
        return this.getInPlace().triangular(false, false);
    }

    @Override
    public double getRankThreshold() {
        return TEN * myMaxDiag * this.getDimensionalEpsilon();
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

    @Override
    public MatrixStore<N> invert(final Access2D<?> original, final PhysicalStore<N> preallocated) throws RecoverableCondition {

        this.decompose(this.wrap(original));

        if (this.isSolvable()) {
            return this.getInverse(preallocated);
        } else {
            throw RecoverableCondition.newMatrixNotInvertible();
        }
    }

    public boolean isFullSize() {
        return true;
    }

    @Override
    public boolean isSolvable() {
        return super.isSolvable();
    }

    @Override
    public boolean isSPD() {
        return mySPD;
    }

    @Override
    public PhysicalStore<N> preallocate(final int nbEquations, final int nbVariables, final int nbSolutions) {
        return this.makeZero(nbEquations, nbSolutions);
    }

    @Override
    public void reset() {

        super.reset();

        mySPD = false;
    }

    @Override
    public boolean removeColumn(final int index) {

        int dim = this.isComputed() ? this.getRowDim() : 0;

        if (index < 0 || index >= dim) {
            throw new IllegalArgumentException("Index " + index + " is not in the range [0, " + dim + ")!");
        }

        Scalar.Factory<N> scalar = this.scalar();
        DecompositionStore<N> current = this.getInPlace();
        int newDim = dim - 1;

        // The rows of [L], except the removed one. Those after it get one element after the diagonal.
        List<List<Scalar<N>>> rows = new ArrayList<>(newDim);
        for (int i = 0; i < dim; i++) {
            if (i != index) {
                List<Scalar<N>> row = new ArrayList<>(i + 1);
                for (int j = 0; j <= i; j++) {
                    row.add(scalar.convert(current.get(i, j)));
                }
                rows.add(row);
            }
        }

        // Unitary (Givens-like) transformations of the columns p and p+1 restore the triangular form:
        // [x y] -> [x*g11 + y*g21, x*g12 + y*g22] with [a b] -> [r 0], where [a b] are row p's elements
        for (int p = index; p < newDim; p++) {

            List<Scalar<N>> rowP = rows.get(p);
            Scalar<N> a = rowP.get(p);
            Scalar<N> b = rowP.get(p + 1);
            double absA = a.norm();
            double r = Math.hypot(absA, b.norm());

            Scalar<N> g11 = a.conjugate().divide(r);
            Scalar<N> g21 = b.conjugate().divide(r);
            Scalar<N> g12;
            Scalar<N> g22;
            if (absA > ZERO) {
                g12 = a.invert().multiply(b).multiply(-absA / r);
                g22 = scalar.convert(absA / r);
            } else {
                g12 = scalar.one();
                g22 = scalar.zero();
            }

            rowP.set(p, scalar.convert(r));
            rowP.remove(p + 1);

            for (int q = p + 1; q < newDim; q++) {
                List<Scalar<N>> rowQ = rows.get(q);
                Scalar<N> x = rowQ.get(p);
                Scalar<N> y = rowQ.get(p + 1);
                rowQ.set(p, x.multiply(g11).add(y.multiply(g21)));
                rowQ.set(p + 1, x.multiply(g12).add(y.multiply(g22)));
            }
        }

        DecompositionStore<N> reduced = this.makeZero(newDim, newDim);
        for (int i = 0; i < newDim; i++) {
            List<Scalar<N>> row = rows.get(i);
            for (int j = 0; j <= i; j++) {
                reduced.set(i, j, row.get(j).get());
            }
        }

        this.setInPlace(reduced);
        this.updateDiagonalRange();
        mySPD = true;

        return this.computed(true);
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
        return mySPD && myMinDiag > this.getRankThreshold();
    }

    private void updateDiagonalRange() {
        DecompositionStore<N> inPlace = this.getInPlace();
        myMaxDiag = MACHINE_SMALLEST;
        myMinDiag = MACHINE_LARGEST;
        for (int ij = 0, limit = this.getRowDim(); ij < limit; ij++) {
            double diagonal = inPlace.doubleValue(ij, ij);
            double pivot = diagonal * diagonal;
            myMaxDiag = MAX.invoke(myMaxDiag, pivot);
            myMinDiag = MIN.invoke(myMinDiag, pivot);
        }
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