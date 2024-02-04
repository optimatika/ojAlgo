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
import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.SWAP;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.structure.Structure2D;
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

    public void btran(final Collectable<Double, ? super PhysicalStore<Double>> lhs, final PhysicalStore<Double> solution) {

        lhs.supplyTo(solution);

        this.btran(solution);
    }

    public void btran(final PhysicalStore<Double> arg) {

        MatrixStore<Double> body = this.getInternalStore();

        arg.substituteForwards(body, false, true, false);

        arg.substituteBackwards(body, true, true, false);

        if (myPivot.isModified()) {
            arg.rows(myPivot.reverseOrder()).copy().supplyTo(arg);
        }
    }

    public Double calculateDeterminant(final Access2D<?> matrix) {

        final double[][] data = this.reset(matrix, false);

        this.getInternalStore().fillMatching(matrix);

        this.doDecompose(data, true);

        return this.getDeterminant();
    }

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

    public boolean decompose(final Access2D.Collectable<Double, ? super PhysicalStore<Double>> matrix) {

        final double[][] data = this.reset(matrix, false);

        matrix.supplyTo(this.getInternalStore());

        return this.doDecompose(data, true);
    }

    public boolean decomposeWithoutPivoting(final Collectable<Double, ? super PhysicalStore<Double>> matrix) {

        final double[][] data = this.reset(matrix, false);

        matrix.supplyTo(this.getInternalStore());

        return this.doDecompose(data, false);
    }

    public void ftran(final PhysicalStore<Double> arg) {

        PhysicalStore<Double> rhs = myPivot.isModified() ? arg.copy() : arg;
        PhysicalStore<Double> sol = arg;

        this.getSolution(rhs, sol);
    }

    public Double getDeterminant() {
        final int m = this.getRowDim();
        final int n = this.getColDim();
        if (m != n) {
            throw new IllegalArgumentException("RawStore must be square.");
        }
        final double[][] LU = this.getInternalData();
        double d = myPivot.signum();
        for (int j = 0; j < n; j++) {
            d *= LU[j][j];
        }
        return d;
    }

    public MatrixStore<Double> getInverse() {
        final int tmpRowDim = this.getRowDim();
        return this.doGetInverse(this.allocate(tmpRowDim, tmpRowDim));
    }

    public MatrixStore<Double> getInverse(final PhysicalStore<Double> preallocated) {
        return this.doGetInverse(preallocated);
    }

    public MatrixStore<Double> getL() {
        MatrixStore<Double> logical = this.getInternalStore().triangular(false, true);
        int nbRows = this.getRowDim();
        if (nbRows < this.getColDim()) {
            return logical.limits(nbRows, nbRows);
        }
        return logical;
    }

    public int[] getPivotOrder() {
        return myPivot.getOrder();
    }

    public double getRankThreshold() {

        double largest = this.getInternalStore().aggregateDiagonal(Aggregator.LARGEST);
        double epsilon = this.getDimensionalEpsilon();

        return epsilon * Math.max(MACHINE_SMALLEST, largest);
    }

    public int[] getReversePivotOrder() {
        return myPivot.reverseOrder();
    }

    public MatrixStore<Double> getSolution(final Collectable<Double, ? super PhysicalStore<Double>> rhs) {
        final DecompositionStore<Double> tmpPreallocated = this.allocate(rhs.countRows(), rhs.countColumns());
        return this.getSolution(rhs, tmpPreallocated);
    }

    @Override
    public MatrixStore<Double> getSolution(final Collectable<Double, ? super PhysicalStore<Double>> rhs, final PhysicalStore<Double> preallocated) {

        this.collect(rhs).rows(myPivot.getOrder()).supplyTo(preallocated);

        return this.doSolve(preallocated);
    }

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

    public PhysicalStore<Double> preallocate(final Structure2D template) {
        return this.allocate(template.countRows(), template.countRows());
    }

    public PhysicalStore<Double> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        return this.allocate(templateBody.countRows(), templateRHS.countColumns());
    }

    @Override
    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws RecoverableCondition {

        final double[][] tmpData = this.reset(body, false);

        this.getInternalStore().fillMatching(body);

        this.doDecompose(tmpData, true);

        if (this.isSolvable()) {

            Primitive64Store.FACTORY.makeWrapper(rhs).rows(myPivot.getOrder()).supplyTo(preallocated);

            return this.doSolve(preallocated);

        }
        throw RecoverableCondition.newEquationSystemNotSolvable();
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
