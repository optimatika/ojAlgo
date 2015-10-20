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

import static org.ojalgo.constant.PrimitiveMath.*;

import java.math.BigDecimal;

import org.ojalgo.access.Access2D;
import org.ojalgo.access.Structure2D;
import org.ojalgo.array.BasicArray;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

abstract class LUDecomposition<N extends Number> extends InPlaceDecomposition<N> implements LU<N> {

    static final class Big extends LUDecomposition<BigDecimal> {

        Big() {
            super(BigDenseStore.FACTORY);
        }

    }

    static final class Complex extends LUDecomposition<ComplexNumber> {

        Complex() {
            super(ComplexDenseStore.FACTORY);
        }

    }

    static final class Primitive extends LUDecomposition<Double> {

        Primitive() {
            super(PrimitiveDenseStore.FACTORY);
        }

    }

    private Pivot myPivot;

    protected LUDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory) {
        super(aFactory);
    }

    public N calculateDeterminant(final Access2D<?> matrix) {
        this.decompose(this.wrap(matrix));
        return this.getDeterminant();
    }

    public boolean computeWithoutPivoting(final ElementsSupplier<N> matrix) {
        return this.compute(matrix, true);
    }

    public boolean decompose(final ElementsSupplier<N> aStore) {
        return this.compute(aStore, false);
    }

    public boolean equals(final MatrixStore<N> aStore, final NumberContext context) {
        return MatrixUtils.equals(aStore, this, context);
    }

    public N getDeterminant() {

        final AggregatorFunction<N> tmpAggrFunc = this.aggregator().product();

        this.getInPlace().visitDiagonal(0, 0, tmpAggrFunc);

        if (myPivot.signum() == -1) {
            return tmpAggrFunc.toScalar().negate().getNumber();
        } else {
            return tmpAggrFunc.getNumber();
        }
    }

    @Override
    public MatrixStore<N> getInverse(final DecompositionStore<N> preallocated) {

        if (myPivot.isModified()) {
            preallocated.fillAll(this.scalar().zero().getNumber());
            final int[] tmpPivotOrder = myPivot.getOrder();
            final int tmpRowDim = this.getRowDim();
            for (int i = 0; i < tmpRowDim; i++) {
                preallocated.set(i, tmpPivotOrder[i], PrimitiveMath.ONE);
            }
        }

        final DecompositionStore<N> tmpBody = this.getInPlace();

        preallocated.substituteForwards(tmpBody, true, false, !myPivot.isModified());

        preallocated.substituteBackwards(tmpBody, false, false, false);

        return preallocated;
    }

    public MatrixStore<N> getL() {
        //return new LowerTriangularStore<N>(this.getInPlace(), true);
        return this.getInPlace().builder().triangular(false, true).build();
    }

    public int[] getPivotOrder() {
        return myPivot.getOrder();
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

    public MatrixStore<N> getU() {
        //return new UpperTriangularStore<N>(this.getInPlace(), false);
        return this.getInPlace().builder().triangular(true, false).build();
    }

    public MatrixStore<N> invert(final Access2D<?> original) {
        this.decompose(this.wrap(original));
        return this.getInverse();
    }

    public MatrixStore<N> invert(final Access2D<?> original, final DecompositionStore<N> preallocated) {
        this.decompose(this.wrap(original));
        return this.getInverse(preallocated);
    }

    public boolean isSolvable() {
        return this.isComputed() && this.isSquareAndNotSingular();
    }

    public final boolean isSquareAndNotSingular() {

        boolean retVal = this.getRowDim() == this.getColDim();

        final DecompositionStore<N> tmpStore = this.getInPlace();
        final int tmpMinDim = (int) Math.min(tmpStore.countRows(), tmpStore.countColumns());

        for (int ij = 0; retVal && (ij < tmpMinDim); ij++) {
            retVal &= !tmpStore.isZero(ij, ij);
        }

        return retVal;
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

        myPivot = null;
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
     * Solves [this][X] = [rhs] by first solving
     *
     * <pre>
     * [L][Y] = [rhs]
     * </pre>
     *
     * and then
     *
     * <pre>
     * [U][X] = [Y]
     * </pre>
     *
     * @param rhs The right hand side
     * @return [X] The solution will be written to "preallocated" and then returned.
     * @see org.ojalgo.matrix.decomposition.GenericDecomposition#doSolve(ElementsSupplier,
     *      org.ojalgo.matrix.decomposition.DecompositionStore)
     */
    @Override
    public MatrixStore<N> solve(final ElementsSupplier<N> rhs, final DecompositionStore<N> preallocated) {

        //preallocated.fillMatching(new RowsStore<N>(new WrapperStore<>(preallocated.factory(), rhs), myPivot.getOrder()));
        preallocated.fillMatching(rhs.get().builder().row(myPivot.getOrder()).get());

        final DecompositionStore<N> tmpBody = this.getInPlace();

        preallocated.substituteForwards(tmpBody, true, false, false);

        preallocated.substituteBackwards(tmpBody, false, false, false);

        return preallocated;
    }

    private final boolean compute(final ElementsSupplier<N> aStore, final boolean assumeNoPivotingRequired) {

        this.reset();

        final DecompositionStore<N> tmpInPlace = this.setInPlace(aStore);

        final int tmpRowDim = this.getRowDim();
        final int tmpColDim = this.getColDim();
        final int tmpMinDim = this.getMinDim();

        myPivot = new Pivot(tmpRowDim);

        final BasicArray<N> tmpMultipliers = this.makeArray(tmpRowDim);

        // Main loop - along the diagonal
        for (int ij = 0; ij < tmpMinDim; ij++) {

            if (!assumeNoPivotingRequired) {
                // Find next pivot row
                final int tmpPivotRow = tmpInPlace.indexOfLargestInColumn(ij, ij);

                // Pivot?
                if (tmpPivotRow != ij) {
                    tmpInPlace.exchangeRows(tmpPivotRow, ij);
                    myPivot.change(tmpPivotRow, ij);
                }
            }

            // Do the calculations...
            // if (!tmpInPlace.isZero(ij, ij)) {
            if (tmpInPlace.doubleValue(ij, ij) != PrimitiveMath.ZERO) {

                // Calculate multipliers and copy to local column
                // Current column, below the diagonal
                tmpInPlace.divideAndCopyColumn(ij, ij, tmpMultipliers);

                // Apply transformations to everything below and to the right of the pivot element
                tmpInPlace.applyLU(ij, tmpMultipliers);

            } else {

                tmpInPlace.set(ij, ij, ZERO);
            }

        }

        return this.computed(true);
    }

    int[] getReducedPivots() {

        final int[] retVal = new int[this.getRank()];
        final int[] tmpFullPivots = this.getPivotOrder();

        final DecompositionStore<N> tmpInPlace = this.getInPlace();

        int tmpRedInd = 0;
        for (int ij = 0; ij < tmpFullPivots.length; ij++) {
            if (!tmpInPlace.isZero(ij, ij)) {
                retVal[tmpRedInd++] = tmpFullPivots[ij];
            }
        }

        return retVal;
    }

}
