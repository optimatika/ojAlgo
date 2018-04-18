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

import static org.ojalgo.constant.PrimitiveMath.*;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.Access2D.Collectable;
import org.ojalgo.access.Structure2D;
import org.ojalgo.array.BasicArray;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.MatrixStore.LogicalBuilder;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

abstract class LDLDecomposition<N extends Number> extends InPlaceDecomposition<N> implements LDL<N> {

    static final class Complex extends LDLDecomposition<ComplexNumber> {

        Complex() {
            super(GenericDenseStore.COMPLEX);
        }

    }

    static final class Primitive extends LDLDecomposition<Double> {

        Primitive() {
            super(PrimitiveDenseStore.FACTORY);
        }

    }

    static final class Quat extends LDLDecomposition<Quaternion> {

        Quat() {
            super(GenericDenseStore.QUATERNION);
        }

    }

    static final class Rational extends LDLDecomposition<RationalNumber> {

        Rational() {
            super(GenericDenseStore.RATIONAL);
        }

    }

    private Pivot myPivot;

    protected LDLDecomposition(final PhysicalStore.Factory<N, ? extends DecompositionStore<N>> factory) {
        super(factory);
    }

    public N calculateDeterminant(final Access2D<?> matrix) {
        this.decompose(this.wrap(matrix));
        return this.getDeterminant();
    }

    public boolean decompose(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix) {

        this.reset();

        final DecompositionStore<N> tmpInPlace = this.setInPlace(matrix);

        final int tmpRowDim = this.getRowDim();
        this.getColDim();
        final int tmpMinDim = this.getMinDim();

        myPivot = new Pivot(tmpRowDim);

        final BasicArray<N> tmpMultipliers = this.makeArray(tmpRowDim);

        // Main loop - along the diagonal
        for (int ij = 0; ij < tmpMinDim; ij++) {

            // Find next pivot row
            final int tmpPivotRow = (int) tmpInPlace.indexOfLargestOnDiagonal(ij);

            // Pivot?
            if (tmpPivotRow != ij) {
                tmpInPlace.exchangeHermitian(tmpPivotRow, ij);
                myPivot.change(tmpPivotRow, ij);
            }

            // Do the calculations...
            // if (tmpInPlace.doubleValue(ij, ij) != PrimitiveMath.ZERO) {
            if (NumberContext.compare(tmpInPlace.doubleValue(ij, ij), PrimitiveMath.ZERO) != 0) {

                // Calculate multipliers and copy to local column
                // Current column, below the diagonal
                tmpInPlace.divideAndCopyColumn(ij, ij, tmpMultipliers);

                // Apply transformations to everything below and to the right of the pivot element
                tmpInPlace.applyLDL(ij, tmpMultipliers);

            } else {

                tmpInPlace.set(ij, ij, ZERO);
            }

        }

        return this.computed(true);
    }

    public MatrixStore<N> getD() {
        final DecompositionStore<N> tmpInPlace = this.getInPlace();
        final LogicalBuilder<N> tmpBuilder = tmpInPlace.logical();
        final LogicalBuilder<N> tmpTriangular = tmpBuilder.diagonal(false);
        return tmpTriangular.get();
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

        final int tmpRowDim = this.getRowDim();
        final int[] tmpOrder = myPivot.getOrder();
        final boolean tmpModified = myPivot.isModified();

        if (tmpModified) {
            preallocated.fillAll(this.scalar().zero().get());
            for (int i = 0; i < tmpRowDim; i++) {
                preallocated.set(i, tmpOrder[i], PrimitiveMath.ONE);
            }
        }

        final DecompositionStore<N> tmpBody = this.getInPlace();

        preallocated.substituteForwards(tmpBody, true, false, !tmpModified);

        final BinaryFunction<N> tmpDivide = this.function().divide();
        for (int i = 0; i < tmpRowDim; i++) {
            preallocated.modifyRow(i, 0, tmpDivide.second(tmpBody.doubleValue(i, i)));
        }

        preallocated.substituteBackwards(tmpBody, true, true, false);

        return preallocated.logical().row(tmpOrder).get();
    }

    public MatrixStore<N> getL() {
        final DecompositionStore<N> tmpInPlace = this.getInPlace();
        final LogicalBuilder<N> tmpBuilder = tmpInPlace.logical();
        final LogicalBuilder<N> tmpTriangular = tmpBuilder.triangular(false, true);
        return tmpTriangular.get();
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

    public final MatrixStore<N> getSolution(final Collectable<N, ? super PhysicalStore<N>> rhs) {
        return this.getSolution(rhs, this.preallocate(this.getInPlace(), rhs));
    }

    @Override
    public MatrixStore<N> getSolution(final Collectable<N, ? super PhysicalStore<N>> rhs, final PhysicalStore<N> preallocated) {

        final int tmpRowDim = this.getRowDim();
        final int[] tmpOrder = myPivot.getOrder();

        //        preallocated.fillMatching(new RowsStore<N>(new WrapperStore<>(preallocated.factory(), rhs), tmpOrder));
        preallocated.fillMatching(this.collect(rhs).logical().row(tmpOrder).get());

        final DecompositionStore<N> tmpBody = this.getInPlace();

        preallocated.substituteForwards(tmpBody, true, false, false);

        final BinaryFunction<N> tmpDivide = this.function().divide();
        for (int i = 0; i < tmpRowDim; i++) {
            preallocated.modifyRow(i, 0, tmpDivide.second(tmpBody.doubleValue(i, i)));
        }

        preallocated.substituteBackwards(tmpBody, true, true, false);

        return preallocated.logical().row(tmpOrder).get();
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

    public boolean isFullRank() {

        final int tmpFirst = 0;
        final int tmpLast = this.getColDim() - 1;

        return PrimitiveScalar.isSmall(this.getInPlace().doubleValue(tmpFirst, tmpFirst), this.getInPlace().doubleValue(tmpLast, tmpLast));
    }

    public boolean isFullSize() {
        return true;
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

    @Override
    protected boolean checkSolvability() {

        boolean retVal = this.getRowDim() == this.getColDim();

        final int tmpFirst = 0;
        final int tmpLast = this.getColDim() - 1;

        retVal = retVal && PrimitiveScalar.isSmall(this.getInPlace().doubleValue(tmpFirst, tmpFirst), this.getInPlace().doubleValue(tmpLast, tmpLast));

        return retVal;
    }

}
