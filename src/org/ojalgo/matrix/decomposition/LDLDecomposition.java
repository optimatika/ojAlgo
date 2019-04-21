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
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.MatrixStore.LogicalBuilder;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.context.NumberContext;

abstract class LDLDecomposition<N extends Number> extends InPlaceDecomposition<N> implements LDL<N> {

    static class Complex extends LDLDecomposition<ComplexNumber> {

        Complex() {
            super(GenericDenseStore.COMPLEX);
        }

    }

    static class Primitive extends LDLDecomposition<Double> {

        Primitive() {
            super(PrimitiveDenseStore.FACTORY);
        }

    }

    static class Quat extends LDLDecomposition<Quaternion> {

        Quat() {
            super(GenericDenseStore.QUATERNION);
        }

    }

    static class Rational extends LDLDecomposition<RationalNumber> {

        Rational() {
            super(GenericDenseStore.RATIONAL);
        }

    }

    private Pivot myPivot;

    protected LDLDecomposition(PhysicalStore.Factory<N, ? extends DecompositionStore<N>> factory) {
        super(factory);
    }

    public N calculateDeterminant(Access2D<?> matrix) {
        this.decompose(this.wrap(matrix));
        return this.getDeterminant();
    }

    public boolean decomposeWithoutPivoting(Collectable<N, ? super PhysicalStore<N>> matrix) {
        return this.doDecompose(matrix, false);
    }

    public boolean decompose(Access2D.Collectable<N, ? super PhysicalStore<N>> matrix) {
        return this.doDecompose(matrix, true);
    }

    public MatrixStore<N> getD() {
        return this.getInPlace().logical().diagonal(false).get();
    }

    public N getDeterminant() {

        AggregatorFunction<N> aggregator = this.aggregator().product();

        this.getInPlace().visitDiagonal(aggregator);

        if (myPivot.signum() == -1) {
            return aggregator.toScalar().negate().get();
        } else {
            return aggregator.get();
        }
    }

    @Override
    public MatrixStore<N> getInverse(PhysicalStore<N> preallocated) {

        int tmpRowDim = this.getRowDim();
        int[] tmpOrder = myPivot.getOrder();
        boolean tmpModified = myPivot.isModified();

        if (tmpModified) {
            preallocated.fillAll(this.scalar().zero().get());
            for (int i = 0; i < tmpRowDim; i++) {
                preallocated.set(i, tmpOrder[i], PrimitiveMath.ONE);
            }
        }

        DecompositionStore<N> tmpBody = this.getInPlace();

        preallocated.substituteForwards(tmpBody, true, false, !tmpModified);

        BinaryFunction<N> tmpDivide = this.function().divide();
        for (int i = 0; i < tmpRowDim; i++) {
            preallocated.modifyRow(i, 0, tmpDivide.second(tmpBody.doubleValue(i, i)));
        }

        preallocated.substituteBackwards(tmpBody, true, true, false);

        return preallocated.logical().row(myPivot.getInverseOrder()).get();
    }

    public MatrixStore<N> getL() {
        DecompositionStore<N> tmpInPlace = this.getInPlace();
        LogicalBuilder<N> tmpBuilder = tmpInPlace.logical();
        LogicalBuilder<N> tmpTriangular = tmpBuilder.triangular(false, true);
        return tmpTriangular.get();
    }

    public int[] getPivotOrder() {
        return myPivot.getOrder();
    }

    public int getRank() {

        int retVal = 0;

        DecompositionStore<N> tmpInPlace = this.getInPlace();

        AggregatorFunction<N> tmpLargest = this.aggregator().largest();
        tmpInPlace.visitDiagonal(0L, 0L, tmpLargest);
        double tmpLargestValue = tmpLargest.doubleValue();

        int tmpMinDim = this.getMinDim();

        for (int ij = 0; ij < tmpMinDim; ij++) {
            if (!tmpInPlace.isSmall(ij, ij, tmpLargestValue)) {
                retVal++;
            }
        }

        return retVal;
    }

    public MatrixStore<N> getSolution(Collectable<N, ? super PhysicalStore<N>> rhs) {
        return this.getSolution(rhs, this.preallocate(this.getInPlace(), rhs));
    }

    @Override
    public MatrixStore<N> getSolution(Collectable<N, ? super PhysicalStore<N>> rhs, PhysicalStore<N> preallocated) {

        int tmpRowDim = this.getRowDim();
        int[] tmpOrder = myPivot.getOrder();
        int[] tmpInvertedOrder = myPivot.getInverseOrder();

        //        preallocated.fillMatching(new RowsStore<N>(new WrapperStore<>(preallocated.factory(), rhs), tmpOrder));
        preallocated.fillMatching(this.collect(rhs).logical().row(tmpOrder).get());

        DecompositionStore<N> tmpBody = this.getInPlace();

        preallocated.substituteForwards(tmpBody, true, false, false);

        BinaryFunction<N> tmpDivide = this.function().divide();
        for (int i = 0; i < tmpRowDim; i++) {
            preallocated.modifyRow(i, 0, tmpDivide.second(tmpBody.doubleValue(i, i)));
        }

        preallocated.substituteBackwards(tmpBody, true, true, false);

        return preallocated.logical().row(tmpInvertedOrder).get();
    }

    public MatrixStore<N> invert(Access2D<?> original) throws RecoverableCondition {

        this.decompose(this.wrap(original));

        if (this.isSolvable()) {
            return this.getInverse();
        } else {
            throw RecoverableCondition.newMatrixNotInvertible();
        }
    }

    public MatrixStore<N> invert(Access2D<?> original, PhysicalStore<N> preallocated) throws RecoverableCondition {

        this.decompose(this.wrap(original));

        if (this.isSolvable()) {
            return this.getInverse(preallocated);
        } else {
            throw RecoverableCondition.newMatrixNotInvertible();
        }
    }

    public boolean isFullRank() {

        int tmpFirst = 0;
        int tmpLast = this.getColDim() - 1;

        return PrimitiveScalar.isSmall(this.getInPlace().doubleValue(tmpFirst, tmpFirst), this.getInPlace().doubleValue(tmpLast, tmpLast));
    }

    public boolean isPivoted() {
        return myPivot.isModified();
    }

    public PhysicalStore<N> preallocate(Structure2D template) {
        long tmpCountRows = template.countRows();
        return this.allocate(tmpCountRows, tmpCountRows);
    }

    public PhysicalStore<N> preallocate(Structure2D templateBody, Structure2D templateRHS) {
        return this.allocate(templateRHS.countRows(), templateRHS.countColumns());
    }

    public MatrixStore<N> solve(Access2D<?> body, Access2D<?> rhs) throws RecoverableCondition {

        this.decompose(this.wrap(body));

        if (this.isSolvable()) {
            return this.getSolution(this.wrap(rhs));
        } else {
            throw RecoverableCondition.newEquationSystemNotSolvable();
        }
    }

    public MatrixStore<N> solve(Access2D<?> body, Access2D<?> rhs, PhysicalStore<N> preallocated) throws RecoverableCondition {

        this.decompose(this.wrap(body));

        if (this.isSolvable()) {
            return this.getSolution(this.wrap(rhs), preallocated);
        } else {
            throw RecoverableCondition.newEquationSystemNotSolvable();
        }
    }

    private boolean doDecompose(Access2D.Collectable<N, ? super PhysicalStore<N>> matrix, boolean pivoting) {

        this.reset();

        DecompositionStore<N> store = this.setInPlace(matrix);

        int dim = this.getMinDim();

        myPivot = new Pivot(dim);

        BasicArray<N> multipliers = this.makeArray(dim);

        // Main loop - along the diagonal
        for (int ij = 0; ij < dim; ij++) {

            if (pivoting) {
                // Find next pivot row
                int pivotRow = (int) store.indexOfLargestOnDiagonal(ij);
                // Pivot?
                if (pivotRow != ij) {
                    store.exchangeHermitian(pivotRow, ij);
                    myPivot.change(pivotRow, ij);
                }
            }

            // Do the calculations...
            if (NumberContext.compare(store.doubleValue(ij, ij), PrimitiveMath.ZERO) != 0) {

                // Calculate multipliers and copy to local column
                // Current column, below the diagonal
                store.divideAndCopyColumn(ij, ij, multipliers);

                // Apply transformations to everything below and to the right of the pivot element
                store.applyLDL(ij, multipliers);

            } else {

                store.set(ij, ij, ZERO);
            }

        }

        return this.computed(true);
    }

    @Override
    protected boolean checkSolvability() {

        boolean retVal = this.getRowDim() == this.getColDim();

        int first = 0;
        int last = this.getColDim() - 1;

        double largest = this.getInPlace().doubleValue(first, first);
        double smallest = this.getInPlace().doubleValue(last, last);

        return retVal && !PrimitiveScalar.isSmall(largest, smallest);
    }

}
