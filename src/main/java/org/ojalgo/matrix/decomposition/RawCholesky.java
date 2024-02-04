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
import org.ojalgo.array.operation.DOT;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.structure.Structure2D;

final class RawCholesky extends RawDecomposition implements Cholesky<Double> {

    private double myMaxDiag = ONE;
    private double myMinDiag = ZERO;
    private boolean mySPD = false;

    /**
     * Not recommended to use this constructor directly. Consider using the static factory method
     * {@linkplain org.ojalgo.matrix.decomposition.Cholesky#make(Access2D)} instead.
     */
    RawCholesky() {
        super();
    }

    public void btran(final PhysicalStore<Double> arg) {
        this.doSolve(arg);
    }

    public Double calculateDeterminant(final Access2D<?> matrix) {

        double[][] retVal = this.reset(matrix, false);

        this.doDecompose(retVal, matrix);

        return this.getDeterminant();
    }

    public boolean checkAndDecompose(final MatrixStore<Double> matrix) {

        mySPD = matrix.isHermitian();

        if (mySPD) {

            double[][] retVal = this.reset(matrix, false);

            return this.doDecompose(retVal, matrix);

        }
        return this.computed(false);
    }

    public int countSignificant(final double threshold) {

        double minimum = Math.sqrt(threshold);

        RawStore internal = this.getInternalStore();

        int significant = 0;
        for (int ij = 0, limit = this.getMinDim(); ij < limit; ij++) {
            if (internal.doubleValue(ij, ij) > minimum) {
                significant++;
            }
        }

        return significant;
    }

    public boolean decompose(final Access2D.Collectable<Double, ? super PhysicalStore<Double>> matrix) {

        double[][] retVal = this.reset(matrix, false);

        RawStore tmpRawInPlaceStore = this.getInternalStore();

        matrix.supplyTo(tmpRawInPlaceStore);

        return this.doDecompose(retVal, tmpRawInPlaceStore);
    }

    public Double getDeterminant() {

        double[][] tmpData = this.getInternalData();

        int tmpMinDim = this.getMinDim();

        double retVal = ONE;
        double tmpVal;
        for (int ij = 0; ij < tmpMinDim; ij++) {
            tmpVal = tmpData[ij][ij];
            retVal *= tmpVal * tmpVal;
        }

        return retVal;
    }

    public MatrixStore<Double> getInverse() {
        int tmpRowDim = this.getRowDim();
        return this.doGetInverse(this.allocate(tmpRowDim, tmpRowDim));
    }

    public MatrixStore<Double> getInverse(final PhysicalStore<Double> preallocated) {
        return this.doGetInverse(preallocated);
    }

    public MatrixStore<Double> getL() {
        return this.getInternalStore().triangular(false, false);
    }

    public double getRankThreshold() {
        return TEN * myMaxDiag * this.getDimensionalEpsilon();
    }

    public MatrixStore<Double> getSolution(final Collectable<Double, ? super PhysicalStore<Double>> rhs) {
        DecompositionStore<Double> tmpPreallocated = this.allocate(rhs.countRows(), rhs.countColumns());
        return this.getSolution(rhs, tmpPreallocated);
    }

    @Override
    public MatrixStore<Double> getSolution(final Collectable<Double, ? super PhysicalStore<Double>> rhs, final PhysicalStore<Double> preallocated) {

        rhs.supplyTo(preallocated);

        return this.doSolve(preallocated);
    }

    @Override
    public MatrixStore<Double> invert(final Access2D<?> original, final PhysicalStore<Double> preallocated) throws RecoverableCondition {

        double[][] retVal = this.reset(original, false);

        this.doDecompose(retVal, original);

        if (this.isSolvable()) {
            return this.getInverse(preallocated);
        }
        throw RecoverableCondition.newMatrixNotInvertible();
    }

    @Override
    public boolean isSolvable() {
        return super.isSolvable();
    }

    public boolean isSPD() {
        return mySPD;
    }

    public PhysicalStore<Double> preallocate(final Structure2D template) {
        return this.allocate(template.countRows(), template.countRows());
    }

    public PhysicalStore<Double> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        return this.allocate(templateBody.countRows(), templateRHS.countColumns());
    }

    @Override
    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws RecoverableCondition {

        double[][] retVal = this.reset(body, false);

        this.doDecompose(retVal, body);

        if (this.isSolvable()) {

            preallocated.fillMatching(rhs);

            return this.doSolve(preallocated);

        }
        throw RecoverableCondition.newEquationSystemNotSolvable();
    }

    private boolean doDecompose(final double[][] data, final Access2D<?> input) {

        int tmpDiagDim = this.getRowDim();
        mySPD = this.getColDim() == tmpDiagDim;
        myMaxDiag = MACHINE_SMALLEST;
        myMinDiag = MACHINE_LARGEST;

        double[] tmpRowIJ;
        double[] tmpRowI;
        double tmpVal;

        // Main loop.
        for (int ij = 0; mySPD && ij < tmpDiagDim; ij++) { // For each row/column, along the diagonal
            tmpRowIJ = data[ij];

            tmpVal = MAX.invoke(input.doubleValue(ij, ij) - DOT.invoke(tmpRowIJ, 0, tmpRowIJ, 0, 0, ij), ZERO);
            myMaxDiag = MAX.invoke(myMaxDiag, tmpVal);
            myMinDiag = MIN.invoke(myMinDiag, tmpVal);
            tmpVal = tmpRowIJ[ij] = SQRT.invoke(tmpVal);
            mySPD = mySPD && tmpVal > ZERO;

            for (int i = ij + 1; i < tmpDiagDim; i++) { // Update column below current row
                tmpRowI = data[i];

                tmpRowI[ij] = (input.doubleValue(i, ij) - DOT.invoke(tmpRowI, 0, tmpRowIJ, 0, 0, ij)) / tmpVal;
            }
        }

        return this.computed(mySPD);
    }

    private MatrixStore<Double> doGetInverse(final PhysicalStore<Double> preallocated) {

        RawStore body = this.getInternalStore();

        preallocated.substituteForwards(body, false, false, true);
        preallocated.substituteBackwards(body, false, true, true);

        return preallocated.hermitian(false);
    }

    private MatrixStore<Double> doSolve(final PhysicalStore<Double> preallocated) {

        RawStore body = this.getInternalStore();

        preallocated.substituteForwards(body, false, false, false);
        preallocated.substituteBackwards(body, false, true, false);

        return preallocated;
    }

    @Override
    protected boolean checkSolvability() {
        return mySPD && myMinDiag > this.getRankThreshold();
    }

}
