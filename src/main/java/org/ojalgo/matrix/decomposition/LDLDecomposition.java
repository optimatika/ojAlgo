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
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.constant.PrimitiveMath;
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
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.context.NumberContext;

abstract class LDLDecomposition<N extends Comparable<N>> extends InPlaceDecomposition<N> implements LDL<N> {

    static final class C128 extends LDLDecomposition<ComplexNumber> {

        C128() {
            super(GenericStore.C128);
        }

    }

    static final class H256 extends LDLDecomposition<Quaternion> {

        H256() {
            super(GenericStore.H256);
        }

    }

    static final class Q128 extends LDLDecomposition<RationalNumber> {

        Q128() {
            super(GenericStore.Q128);
        }

    }

    static final class R064 extends LDLDecomposition<Double> {

        R064() {
            super(Primitive64Store.FACTORY);
        }

    }

    static final class R128 extends LDLDecomposition<Quadruple> {

        R128() {
            super(GenericStore.R128);
        }

    }

    private final Pivot myPivot = new Pivot();
    private double myThreshold = Double.NaN;

    protected LDLDecomposition(final PhysicalStore.Factory<N, ? extends DecompositionStore<N>> factory) {
        super(factory);
    }

    public final void btran(final PhysicalStore<N> arg) {

        int[] order = myPivot.getOrder();

        if (myPivot.isModified()) {
            arg.rows(order).copy().supplyTo(arg);
        }

        DecompositionStore<N> body = this.getInPlace();

        arg.substituteForwards(body, true, false, false);

        BinaryFunction<N> divide = this.function().divide();
        for (int i = 0; i < order.length; i++) {
            arg.modifyRow(i, divide.by(body.get(i, i)));
        }

        arg.substituteBackwards(body, true, true, false);

        if (myPivot.isModified()) {
            arg.rows(myPivot.reverseOrder()).copy().supplyTo(arg);
        }
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

    public MatrixStore<N> getD() {
        return this.getInPlace().diagonal();
    }

    public N getDeterminant() {

        AggregatorFunction<N> aggregator = this.aggregator().product();

        this.getInPlace().visitDiagonal(aggregator);

        if (myPivot.signum() == -1) {
            return aggregator.toScalar().negate().get();
        }
        return aggregator.get();
    }

    @Override
    public MatrixStore<N> getInverse(final PhysicalStore<N> preallocated) {

        int[] order = myPivot.getOrder();
        boolean modified = myPivot.isModified();

        if (modified) {
            preallocated.fillAll(this.scalar().zero().get());
            for (int i = 0; i < order.length; i++) {
                preallocated.set(i, order[i], PrimitiveMath.ONE);
            }
        }

        DecompositionStore<N> body = this.getInPlace();

        preallocated.substituteForwards(body, true, false, !modified);

        BinaryFunction<N> divide = this.function().divide();
        for (int i = 0; i < order.length; i++) {
            preallocated.modifyRow(i, 0, divide.by(body.doubleValue(i, i)));
        }

        preallocated.substituteBackwards(body, true, true, false);

        return preallocated.rows(myPivot.reverseOrder());
    }

    public MatrixStore<N> getL() {
        DecompositionStore<N> tmpInPlace = this.getInPlace();
        MatrixStore<N> tmpBuilder = tmpInPlace;
        return tmpBuilder.triangular(false, true);
    }

    public int[] getPivotOrder() {
        return myPivot.getOrder();
    }

    public double getRankThreshold() {

        N largest = this.getInPlace().aggregateDiagonal(Aggregator.LARGEST);
        double epsilon = this.getDimensionalEpsilon();

        return epsilon * Math.max(MACHINE_SMALLEST, NumberDefinition.doubleValue(largest));
    }

    public int[] getReversePivotOrder() {
        return myPivot.reverseOrder();
    }

    public MatrixStore<N> getSolution(final Collectable<N, ? super PhysicalStore<N>> rhs) {
        return this.getSolution(rhs, this.preallocate(this.getInPlace(), rhs));
    }

    @Override
    public MatrixStore<N> getSolution(final Collectable<N, ? super PhysicalStore<N>> rhs, final PhysicalStore<N> preallocated) {

        int[] order = myPivot.getOrder();

        preallocated.fillMatching(this.collect(rhs).rows(order));

        DecompositionStore<N> body = this.getInPlace();

        preallocated.substituteForwards(body, true, false, false);

        BinaryFunction<N> divide = this.function().divide();
        for (int i = 0; i < order.length; i++) {
            preallocated.modifyRow(i, divide.by(body.get(i, i)));
        }

        preallocated.substituteBackwards(body, true, true, false);

        return preallocated.rows(myPivot.reverseOrder());
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

    public boolean isPivoted() {
        return myPivot.isModified();
    }

    @Override
    public boolean isSolvable() {
        return super.isSolvable();
    }

    public PhysicalStore<N> preallocate(final Structure2D template) {
        long tmpCountRows = template.countRows();
        return this.allocate(tmpCountRows, tmpCountRows);
    }

    public PhysicalStore<N> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        return this.allocate(templateRHS.countRows(), templateRHS.countColumns());
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

    private boolean doDecompose(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix, final boolean pivoting) {

        this.reset();

        DecompositionStore<N> store = this.setInPlace(matrix);

        int dim = this.getMinDim();

        myPivot.reset(dim);

        BasicArray<N> multipliers = this.makeArray(dim);

        // Main loop - along the diagonal
        for (int ij = 0; ij < dim; ij++) {

            if (pivoting) {
                // Find next pivot row
                int pivotRow = store.indexOfLargestOnDiagonal(ij, ij);
                // Pivot?
                if (pivotRow != ij) {
                    store.exchangeHermitian(pivotRow, ij);
                    myPivot.change(pivotRow, ij);
                }
            }

            double storeDiagVal = store.doubleValue(ij, ij);

            if (Double.isFinite(myThreshold) && myThreshold > ZERO) {

                // double maxColVal = ZERO;
                // for (int i = ij + 1; i < dim; i++) {
                //     maxColVal = Math.max(maxColVal, Math.abs(store.doubleValue(i, ij)));
                // }
                // maxColVal *= myThreshold;
                // maxColVal *= maxColVal;

                double candidate = Math.max(Math.abs(storeDiagVal), myThreshold);

                if (candidate > storeDiagVal) {
                    storeDiagVal = candidate;
                    store.set(ij, ij, storeDiagVal);
                }
            }

            // Do the calculations...
            if (NumberContext.compare(storeDiagVal, PrimitiveMath.ZERO) != 0) {

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
        return this.isSquare() && this.isFullRank();
    }

    void setThreshold(final N threshold) {
        myThreshold = NumberDefinition.doubleValue(threshold);
    }

}
