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
import org.ojalgo.array.BasicArray;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

abstract class CholeskyDecomposition<N extends Number> extends InPlaceDecomposition<N> implements Cholesky<N> {

    static final class Big extends CholeskyDecomposition<BigDecimal> {

        Big() {
            super(BigDenseStore.FACTORY);
        }

    }

    static final class Complex extends CholeskyDecomposition<ComplexNumber> {

        Complex() {
            super(ComplexDenseStore.FACTORY);
        }

    }

    static final class Primitive extends CholeskyDecomposition<Double> {

        Primitive() {
            super(PrimitiveDenseStore.FACTORY);
        }

    }

    private boolean mySPD = false;

    protected CholeskyDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory) {
        super(aFactory);
    }

    public N calculateDeterminant(final Access2D<?> matrix) {
        this.decompose(this.wrap(matrix));
        return this.getDeterminant();
    }

    public boolean checkAndCompute(final MatrixStore<N> matrix) {
        return this.compute(matrix, true);
    }

    public final boolean decompose(final ElementsSupplier<N> aStore) {
        return this.compute(aStore, false);
    }

    public final boolean equals(final MatrixStore<N> aStore, final NumberContext context) {
        return MatrixUtils.equals(aStore, this, context);
    }

    public N getDeterminant() {

        final AggregatorFunction<N> tmpAggrFunc = this.aggregator().product2();

        this.getInPlace().visitDiagonal(0, 0, tmpAggrFunc);

        return tmpAggrFunc.getNumber();
    }

    @Override
    public final MatrixStore<N> getInverse(final DecompositionStore<N> preallocated) {

        final DecompositionStore<N> tmpBody = this.getInPlace();

        preallocated.substituteForwards(tmpBody, false, false, true);
        preallocated.substituteBackwards(tmpBody, false, true, true);

        //return new LowerHermitianStore<>(preallocated);
        return preallocated.builder().hermitian(false).build();
    }

    public MatrixStore<N> getL() {
        return this.getInPlace().builder().triangular(false, false).build();
    }

    public MatrixStore<N> invert(final Access2D<?> original) {
        this.decompose(this.wrap(original));
        return this.getInverse();
    }

    public MatrixStore<N> invert(final Access2D<?> original, final DecompositionStore<N> preallocated) {
        this.decompose(this.wrap(original));
        return this.getInverse(preallocated);
    }

    public final boolean isFullSize() {
        return true;
    }

    public boolean isSolvable() {
        return this.isComputed() && mySPD;
    }

    public boolean isSPD() {
        return mySPD;
    }

    public DecompositionStore<N> preallocate(final Structure2D template) {
        final long tmpCountRows = template.countRows();
        return this.preallocate(tmpCountRows, tmpCountRows);
    }

    public DecompositionStore<N> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        return this.preallocate(templateRHS.countRows(), templateRHS.countColumns());
    }

    @Override
    public void reset() {

        super.reset();

        mySPD = false;
    }

    public MatrixStore<N> solve(final Access2D<?> body, final Access2D<?> rhs) {
        this.decompose(this.wrap(body));
        return this.solve(this.wrap(rhs));
    }

    public MatrixStore<N> solve(final Access2D<?> body, final Access2D<?> rhs, final DecompositionStore<N> preallocated) {
        this.decompose(this.wrap(body));
        return this.solve(rhs, preallocated);
    }

    public final MatrixStore<N> solve(final ElementsSupplier<N> rhs) {
        return this.solve(rhs, this.preallocate(this.getInPlace(), rhs));
    }

    /**
     * Solves [this][X] = [aRHS] by first solving
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
     * @see org.ojalgo.matrix.decomposition.GenericDecomposition#doSolve(ElementsSupplier,
     *      org.ojalgo.matrix.decomposition.DecompositionStore)
     */
    @Override
    public final MatrixStore<N> solve(final ElementsSupplier<N> rhs, final DecompositionStore<N> preallocated) {

        rhs.supplyTo(preallocated);

        final DecompositionStore<N> tmpBody = this.getInPlace();

        preallocated.substituteForwards(tmpBody, false, false, false);
        preallocated.substituteBackwards(tmpBody, false, true, false);

        return preallocated;
    }

    final boolean compute(final ElementsSupplier<N> matrix, final boolean checkHermitian) {

        this.reset();

        final DecompositionStore<N> tmpInPlace = this.setInPlace(matrix);

        final int tmpRowDim = this.getRowDim();
        final int tmpColDim = this.getColDim();
        final int tmpMinDim = Math.min(tmpRowDim, tmpColDim);

        // true if (Hermitian) Positive Definite
        boolean tmpPositiveDefinite = tmpRowDim == tmpColDim;

        final BasicArray<N> tmpMultipliers = this.makeArray(tmpRowDim);

        // Check if hermitian, maybe
        if (tmpPositiveDefinite && checkHermitian) {
            tmpPositiveDefinite &= MatrixUtils.isHermitian(tmpInPlace);
        }

        final UnaryFunction<N> tmpSqrtFunc = this.function().sqrt();

        // Main loop - along the diagonal
        for (int ij = 0; tmpPositiveDefinite && (ij < tmpMinDim); ij++) {

            // Do the calculations...
            if (tmpInPlace.doubleValue(ij, ij) > PrimitiveMath.ZERO) {

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
