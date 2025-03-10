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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.SWAP;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.type.context.NumberContext;

final class RawLU extends RawDecomposition implements LU<Double> {

    private final Pivot myPivot = new Pivot();

    /**
     * Not recommended to use this constructor directly. Consider using the static factory method
     * {@linkplain org.ojalgo.matrix.decomposition.LU#make(Access2D)} instead.
     */
    RawLU() {
        super();
    }

    @Override
    public void btran(final PhysicalStore<Double> arg) {

        MatrixStore<Double> body = this.getInternalStore();

        arg.substituteForwards(body, false, true, false);

        arg.substituteBackwards(body, true, true, false);

        this.applyReverseOrder(myPivot, arg);
    }

    @Override
    public Double calculateDeterminant(final Access2D<?> matrix) {

        final double[][] data = this.reset(matrix, false);

        this.getInternalStore().fillMatching(matrix);

        this.doDecompose(data, true);

        return this.getDeterminant();
    }

    @Override
    public int countSignificant(final double threshold) {

        RawStore internal = this.getInternalStore();

        int significant = 0;
        for (int ij = 0, limit = this.getMinDim(); ij < limit; ij++) {
            if (Math.abs(internal.doubleValue(ij, ij)) > threshold) {
                significant++;
            }
        }

        return significant;
    }

    @Override
    public boolean decompose(final Access2D.Collectable<Double, ? super TransformableRegion<Double>> matrix) {

        double[][] data = this.reset(matrix, false);

        matrix.supplyTo(this.getInternalStore());

        return this.doDecompose(data, true);
    }

    @Override
    public boolean decomposeWithoutPivoting(final Collectable<Double, ? super TransformableRegion<Double>> matrix) {

        double[][] data = this.reset(matrix, false);

        matrix.supplyTo(this.getInternalStore());

        return this.doDecompose(data, false);
    }

    @Override
    public void ftran(final PhysicalStore<Double> arg) {

        this.applyPivotOrder(myPivot, arg);

        MatrixStore<Double> body = this.getInternalStore();

        arg.substituteForwards(body, true, false, false);

        arg.substituteBackwards(body, false, false, false);
    }

    @Override
    public Double getDeterminant() {

        int m = this.getRowDim();
        int n = this.getColDim();

        if (m != n) {
            throw new IllegalArgumentException("RawStore must be square.");
        }

        double[][] internalData = this.getInternalData();

        double retVal = myPivot.signum();
        for (int j = 0; j < n; j++) {
            retVal *= internalData[j][j];
        }

        return Double.valueOf(retVal);
    }

    @Override
    public MatrixStore<Double> getInverse(final PhysicalStore<Double> preallocated) {
        return this.doGetInverse(preallocated);
    }

    @Override
    public MatrixStore<Double> getL() {
        MatrixStore<Double> logical = this.getInternalStore().triangular(false, true);
        int nbRows = this.getRowDim();
        if (nbRows < this.getColDim()) {
            return logical.limits(nbRows, nbRows);
        }
        return logical;
    }

    @Override
    public int[] getPivotOrder() {
        return myPivot.getOrder();
    }

    @Override
    public double getRankThreshold() {

        double largest = this.getInternalStore().aggregateDiagonal(Aggregator.LARGEST).doubleValue();
        double epsilon = this.getDimensionalEpsilon();

        return epsilon * Math.max(MACHINE_SMALLEST, largest);
    }

    @Override
    public int[] getReversePivotOrder() {
        return myPivot.reverseOrder();
    }

    @Override
    public MatrixStore<Double> getSolution(final Collectable<Double, ? super PhysicalStore<Double>> rhs, final PhysicalStore<Double> preallocated) {

        rhs.supplyTo(preallocated);

        this.applyPivotOrder(myPivot, preallocated);

        return this.doSolve(preallocated);
    }

    @Override
    public MatrixStore<Double> getU() {
        MatrixStore<Double> retVal = this.getInternalStore().triangular(true, false);
        int nbCols = this.getColDim();
        if (this.getRowDim() > nbCols) {
            retVal = retVal.limits(nbCols, nbCols);
        }
        return retVal;
    }

    @Override
    public MatrixStore<Double> invert(final Access2D<?> original, final PhysicalStore<Double> preallocated) throws RecoverableCondition {

        final double[][] tmpData = this.reset(original, false);

        this.getInternalStore().fillMatching(original);

        this.doDecompose(tmpData, true);

        if (this.isSolvable()) {
            return this.getInverse(preallocated);
        } else {
            throw RecoverableCondition.newMatrixNotInvertible();
        }
    }

    @Override
    public boolean isPivoted() {
        return myPivot.isModified();
    }

    @Override
    public boolean isSolvable() {
        return super.isSolvable();
    }

    @Override
    public PhysicalStore<Double> preallocate(final int nbEquations, final int nbVariables, final int nbSolutions) {
        return this.makeZero(nbEquations, nbSolutions);
    }

    @Override
    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws RecoverableCondition {

        double[][] tmpData = this.reset(body, false);

        this.getInternalStore().fillMatching(body);

        this.doDecompose(tmpData, true);

        if (this.isSolvable()) {

            preallocated.fillMatching(rhs);

            this.applyPivotOrder(myPivot, preallocated);

            return this.doSolve(preallocated);

        } else {

            throw RecoverableCondition.newEquationSystemNotSolvable();
        }
    }

    private boolean doDecompose(final double[][] data, final boolean pivoting) {

        final int m = this.getRowDim();
        final int n = this.getColDim();

        myPivot.reset(m);

        double[] rowP;
        double[] rowI;

        double valP;
        double valI;

        // Main loop along the diagonal
        for (int ij = 0, limit = Math.min(m, n); ij < limit; ij++) {

            if (pivoting) {
                int p = ij;
                valP = ABS.invoke(data[p][ij]);
                for (int i = ij + 1; i < m; i++) {
                    valI = ABS.invoke(data[i][ij]);
                    if (valI > valP) {
                        p = i;
                        valP = valI;
                    }
                }
                if (p != ij) {
                    SWAP.exchangeRows(data, ij, p);
                    myPivot.change(ij, p);
                }
            }

            rowP = data[ij];
            valP = rowP[ij];

            if (NumberContext.compare(valP, ZERO) != 0) {
                for (int i = ij + 1; i < m; i++) {

                    rowI = data[i];
                    valI = rowI[ij] / valP;

                    if (NumberContext.compare(valI, ZERO) != 0) {
                        rowI[ij] = valI;
                        AXPY.invoke(rowI, 0, -valI, rowP, 0, ij + 1, n);
                    }
                }
            }
        }

        return this.computed(true);
    }

    private MatrixStore<Double> doGetInverse(final PhysicalStore<Double> preallocated) {

        int[] pivotOrder = myPivot.getOrder();
        int numbRows = this.getRowDim();
        for (int i = 0; i < numbRows; i++) {
            preallocated.set(i, pivotOrder[i], ONE);
        }

        RawStore body = this.getInternalStore();

        preallocated.substituteForwards(body, true, false, !myPivot.isModified());

        preallocated.substituteBackwards(body, false, false, false);

        return preallocated;
    }

    private MatrixStore<Double> doSolve(final PhysicalStore<Double> preallocated) {

        MatrixStore<Double> body = this.getInternalStore();

        preallocated.substituteForwards(body, true, false, false);

        preallocated.substituteBackwards(body, false, false, false);

        return preallocated;
    }

    @Override
    protected boolean checkSolvability() {
        return this.isSquare() && this.isFullRank();
    }

}
