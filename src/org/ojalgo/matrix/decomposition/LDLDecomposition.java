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
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.MatrixStore.Builder;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;

abstract class LDLDecomposition<N extends Number> extends InPlaceDecomposition<N> implements LDL<N> {

    static final class Big extends LDLDecomposition<BigDecimal> {

        Big() {
            super(BigDenseStore.FACTORY);
        }

    }

    static final class Complex extends LDLDecomposition<ComplexNumber> {

        Complex() {
            super(ComplexDenseStore.FACTORY);
        }

    }

    static final class Primitive extends LDLDecomposition<Double> {

        Primitive() {
            super(PrimitiveDenseStore.FACTORY);
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

    public boolean decompose(final ElementsSupplier<N> matrix) {

        this.reset();

        final DecompositionStore<N> tmpInPlace = this.setInPlace(matrix);

        final int tmpRowDim = this.getRowDim();
        final int tmpColDim = this.getColDim();
        final int tmpMinDim = this.getMinDim();

        myPivot = new Pivot(tmpRowDim);

        final BasicArray<N> tmpMultipliers = this.makeArray(tmpRowDim);

        // Main loop - along the diagonal
        for (int ij = 0; ij < tmpMinDim; ij++) {

            // Find next pivot row
            final int tmpPivotRow = tmpInPlace.indexOfLargestInDiagonal(ij, ij) / tmpRowDim;

            // Pivot?
            if (tmpPivotRow != ij) {
                tmpInPlace.exchangeHermitian(tmpPivotRow, ij);
                myPivot.change(tmpPivotRow, ij);
            }

            // Do the calculations...
            if (tmpInPlace.doubleValue(ij, ij) != PrimitiveMath.ZERO) {

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
        final Builder<N> tmpBuilder = tmpInPlace.builder();
        final Builder<N> tmpTriangular = tmpBuilder.diagonal(false);
        return tmpTriangular.build();
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

        final int tmpRowDim = this.getRowDim();
        final int[] tmpOrder = myPivot.getOrder();
        final boolean tmpModified = myPivot.isModified();

        if (tmpModified) {
            preallocated.fillAll(this.scalar().zero().getNumber());
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

        return preallocated.builder().row(tmpOrder).build();
    }

    public MatrixStore<N> getL() {
        final DecompositionStore<N> tmpInPlace = this.getInPlace();
        final Builder<N> tmpBuilder = tmpInPlace.builder();
        final Builder<N> tmpTriangular = tmpBuilder.triangular(false, true);
        return tmpTriangular.build();
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

    public boolean isFullSize() {
        return true;
    }

    public boolean isSolvable() {
        return this.isComputed() && this.isSquareAndNotSingular();
    }

    public boolean isSquareAndNotSingular() {

        boolean retVal = this.getRowDim() == this.getColDim();

        final int tmpFirst = 0;
        final int tmpLast = this.getColDim() - 1;

        retVal = retVal && PrimitiveScalar.isSmall(this.getInPlace().doubleValue(tmpFirst, tmpFirst), this.getInPlace().doubleValue(tmpLast, tmpLast));

        return retVal;
    }

    public DecompositionStore<N> preallocate(final Structure2D template) {
        final long tmpCountRows = template.countRows();
        return this.preallocate(tmpCountRows, tmpCountRows);
    }

    public DecompositionStore<N> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        return this.preallocate(templateRHS.countRows(), templateRHS.countColumns());
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

    @Override
    public MatrixStore<N> solve(final ElementsSupplier<N> rhs, final DecompositionStore<N> preallocated) {

        final int tmpRowDim = this.getRowDim();
        final int[] tmpOrder = myPivot.getOrder();

        //        preallocated.fillMatching(new RowsStore<N>(new WrapperStore<>(preallocated.factory(), rhs), tmpOrder));
        preallocated.fillMatching(rhs.get().builder().row(tmpOrder).get());

        final DecompositionStore<N> tmpBody = this.getInPlace();

        preallocated.substituteForwards(tmpBody, true, false, false);

        final BinaryFunction<N> tmpDivide = this.function().divide();
        for (int i = 0; i < tmpRowDim; i++) {
            preallocated.modifyRow(i, 0, tmpDivide.second(tmpBody.doubleValue(i, i)));
        }

        preallocated.substituteBackwards(tmpBody, true, true, false);

        return preallocated.builder().row(tmpOrder).build();
    }

}
