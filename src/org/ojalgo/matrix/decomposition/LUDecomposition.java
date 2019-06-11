/*
 * Copyright 1997-2019 Optimatika
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
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.context.NumberContext;

abstract class LUDecomposition<N extends Number> extends InPlaceDecomposition<N> implements LU<N> {

    static final class Complex extends LUDecomposition<ComplexNumber> {

        Complex() {
            super(GenericDenseStore.COMPLEX);
        }

    }

    static final class Primitive extends LUDecomposition<Double> {

        Primitive() {
            super(PrimitiveDenseStore.FACTORY);
        }

    }

    static final class Quat extends LUDecomposition<Quaternion> {

        Quat() {
            super(GenericDenseStore.QUATERNION);
        }

    }

    static final class Rational extends LUDecomposition<RationalNumber> {

        Rational() {
            super(GenericDenseStore.RATIONAL);
        }

    }

    private final Pivot myPivot = new Pivot();

    protected LUDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory) {
        super(aFactory);
    }

    public N calculateDeterminant(final Access2D<?> matrix) {
        this.decompose(this.wrap(matrix));
        return this.getDeterminant();
    }

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

    public boolean decompose(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix) {
        return this.doDecompose(matrix, true);
    }

    public boolean decomposeWithoutPivoting(final Collectable<N, ? super PhysicalStore<N>> matrix) {
        return this.doDecompose(matrix, false);
    }

    public N getDeterminant() {

        final AggregatorFunction<N> tmpAggrFunc = this.aggregator().product();

        this.getInPlace().visitDiagonal(0, 0, tmpAggrFunc);

        if (myPivot.signum() == -1) {
            return tmpAggrFunc.toScalar().negate().get();
        } else {
            return tmpAggrFunc.get();
        }
    }

    @Override
    public MatrixStore<N> getInverse(final PhysicalStore<N> preallocated) {

        if (myPivot.isModified()) {
            preallocated.fillAll(this.scalar().zero().get());
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
        return this.getInPlace().logical().triangular(false, true).get();
    }

    public int[] getPivotOrder() {
        return myPivot.getOrder();
    }

    public double getRankThreshold() {

        N largest = this.getInPlace().aggregateDiagonal(Aggregator.LARGEST);
        double epsilon = this.getDimensionalEpsilon();

        return epsilon * Math.max(MACHINE_SMALLEST, largest.doubleValue());
    }

    public final MatrixStore<N> getSolution(final Collectable<N, ? super PhysicalStore<N>> rhs) {
        return this.getSolution(rhs, this.preallocate(this.getInPlace(), rhs));
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
     */
    @Override
    public MatrixStore<N> getSolution(final Collectable<N, ? super PhysicalStore<N>> rhs, final PhysicalStore<N> preallocated) {

        //preallocated.fillMatching(new RowsStore<N>(new WrapperStore<>(preallocated.factory(), rhs), myPivot.getOrder()));
        preallocated.fillMatching(this.collect(rhs).logical().row(myPivot.getOrder()).get());

        final DecompositionStore<N> tmpBody = this.getInPlace();

        preallocated.substituteForwards(tmpBody, true, false, false);

        preallocated.substituteBackwards(tmpBody, false, false, false);

        return preallocated;
    }

    public MatrixStore<N> getU() {
        //return new UpperTriangularStore<N>(this.getInPlace(), false);
        return this.getInPlace().logical().triangular(true, false).get();
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

    public boolean isPivoted() {
        return myPivot.isModified();
    }

    public PhysicalStore<N> preallocate(final Structure2D template) {
        final long tmpCountRows = template.countRows();
        return this.allocate(tmpCountRows, tmpCountRows);
    }

    public PhysicalStore<N> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        return this.allocate(templateRHS.countRows(), templateRHS.countColumns());
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

    private final boolean doDecompose(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix, final boolean pivoting) {

        this.reset();

        final DecompositionStore<N> tmpInPlace = this.setInPlace(matrix);

        final int tmpRowDim = this.getRowDim();
        this.getColDim();
        final int tmpMinDim = this.getMinDim();

        myPivot.reset(tmpRowDim);

        final BasicArray<N> tmpMultipliers = this.makeArray(tmpRowDim);

        // Main loop - along the diagonal
        for (int ij = 0; ij < tmpMinDim; ij++) {

            if (pivoting) {
                // Find next pivot row
                final int tmpPivotRow = (int) tmpInPlace.indexOfLargestInColumn(ij, ij);

                // Pivot?
                if (tmpPivotRow != ij) {
                    tmpInPlace.exchangeRows(tmpPivotRow, ij);
                    myPivot.change(tmpPivotRow, ij);
                }
            }

            // Do the calculations...
            // if (!tmpInPlace.isZero(ij, ij)) {
            // if (tmpInPlace.doubleValue(ij, ij) != PrimitiveMath.ZERO) {
            if (NumberContext.compare(tmpInPlace.doubleValue(ij, ij), PrimitiveMath.ZERO) != 0) {

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

    @Override
    protected boolean checkSolvability() {
        return this.isSquare() && this.isFullRank();
    }

    int[] getReducedPivots() {

        final int[] retVal = new int[this.getRank()];
        final int[] tmpFullPivots = this.getPivotOrder();

        final DecompositionStore<N> tmpInPlace = this.getInPlace();

        int tmpRedInd = 0;
        for (int ij = 0; ij < tmpFullPivots.length; ij++) {
            if (!tmpInPlace.isSmall(ij, ij, PrimitiveMath.ONE)) {
                retVal[tmpRedInd++] = tmpFullPivots[ij];
            }
        }

        return retVal;
    }

}
