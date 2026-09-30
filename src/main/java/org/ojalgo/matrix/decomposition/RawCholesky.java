/*
 * Copyright 1997-2026 Optimatika
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

import java.util.Arrays;
import java.util.List;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.array.operation.DOT;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.matrix.transformation.InvertibleFactor;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;

final class RawCholesky extends RawDecomposition implements Cholesky<Double> {

    /**
     * When growing, a new row/column is rejected if its pivot (the new diagonal element of [L], squared) is
     * not larger than this factor times the new diagonal element of the matrix – the new row/column is then
     * (numerically) linearly dependent on the existing ones.
     */
    private static final double DEPENDENT = 1E-12;

    /**
     * When growing, the rows of [L] are extended with spare capacity so that they don't have to be copied
     * every time. The first row is always exactly as long as the number of columns – it defines it.
     */
    private static double[] ensureLength(final double[] row, final int length) {
        if (row.length >= length) {
            return row;
        } else {
            return Arrays.copyOf(row, Math.max(length, 2 * row.length));
        }
    }

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

    @Override
    public boolean appendColumn(final Access1D<Double> column) {

        int dim = this.isComputed() ? this.getRowDim() : 0;

        if (column.size() != dim + 1) {
            throw new IllegalArgumentException("The column must be of length " + (dim + 1) + "!");
        }

        double[][] data = this.getInternalData();

        // [L][l] = [a], where [l] is the new row of [L] (without its diagonal element)
        double[] newRow = new double[Math.max(dim + 1, 2 * dim)];
        double sumOfSquares = ZERO;
        for (int i = 0; i < dim; i++) {
            double[] rowI = data[i];
            double value = column.doubleValue(i);
            for (int p = 0; p < i; p++) {
                value -= rowI[p] * newRow[p];
            }
            value /= rowI[i];
            newRow[i] = value;
            sumOfSquares += value * value;
        }

        double diagonal = column.doubleValue(dim);
        double pivot = diagonal - sumOfSquares;

        if (!(pivot > DEPENDENT * diagonal)) {
            return false;
        }

        newRow[dim] = SQRT.invoke(pivot);

        double[][] newData = new double[dim + 1][];
        for (int i = 0; i < dim; i++) {
            newData[i] = RawCholesky.ensureLength(data[i], dim + 1);
        }
        newData[dim] = newRow;
        newData[0] = Arrays.copyOf(newData[0], dim + 1);

        this.setInternalData(newData);

        if (dim == 0) {
            myMaxDiag = pivot;
            myMinDiag = pivot;
        } else {
            myMaxDiag = MAX.invoke(myMaxDiag, pivot);
            myMinDiag = MIN.invoke(myMinDiag, pivot);
        }
        mySPD = true;

        return this.computed(true);
    }

    @Override
    public void btran(final double[] arg) {
        this.ftran(arg);
    }

    @Override
    public void btran(final PhysicalStore<Double> arg) {
        this.ftran(arg);
    }

    @Override
    public Double calculateDeterminant(final Access2D<?> matrix) {

        double[][] retVal = this.reset(matrix, false);

        this.doDecompose(retVal, matrix);

        return this.getDeterminant();
    }

    @Override
    public boolean checkAndDecompose(final MatrixStore<Double> matrix) {

        mySPD = matrix.isHermitian();

        if (mySPD) {

            double[][] retVal = this.reset(matrix, false);

            return this.doDecompose(retVal, matrix);

        }
        return this.computed(false);
    }

    @Override
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

    @Override
    public boolean decompose(final Access2D.Collectable<Double, ? super TransformableRegion<Double>> matrix) {

        double[][] retVal = this.reset(matrix, false);

        RawStore tmpRawInPlaceStore = this.getInternalStore();

        matrix.supplyTo(tmpRawInPlaceStore);

        return this.doDecompose(retVal, tmpRawInPlaceStore);
    }

    @Override
    public void ftran(final double[] arg) {

        RawStore body = this.getInternalStore();

        body.substituteForwards(false, false, arg);
        body.substituteBackwards(true, false, arg);
    }

    @Override
    public void ftran(final PhysicalStore<Double> arg) {

        RawStore body = this.getInternalStore();

        body.substituteForwards(false, false, arg);
        body.substituteBackwards(true, false, arg);
    }

    @Override
    public Double getDeterminant() {

        double[][] tmpData = this.getInternalData();

        int tmpMinDim = this.getMinDim();

        double retVal = ONE;
        double tmpVal;
        for (int ij = 0; ij < tmpMinDim; ij++) {
            tmpVal = tmpData[ij][ij];
            retVal *= tmpVal * tmpVal;
        }

        return Double.valueOf(retVal);
    }

    @Override
    public List<InvertibleFactor<Double>> getFactors() {
        RawStore internalStore = this.getInternalStore();
        return List.of(new FactorLower<>(internalStore, false), new FactorUpperConjugate<>(internalStore, false));
    }

    @Override
    public MatrixStore<Double> getInverse(final PhysicalStore<Double> preallocated) {
        return this.doGetInverse(preallocated);
    }

    @Override
    public MatrixStore<Double> getL() {
        return this.getInternalStore().triangular(false, false);
    }

    @Override
    public double getRankThreshold() {
        return TEN * myMaxDiag * this.getDimensionalEpsilon();
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

    @Override
    public boolean isSPD() {
        return mySPD;
    }

    @Override
    public PhysicalStore<Double> preallocate(final int nbEquations, final int nbVariables, final int nbSolutions) {
        return this.makeZero(nbEquations, nbSolutions);
    }

    @Override
    public boolean removeColumn(final int index) {

        int dim = this.isComputed() ? this.getRowDim() : 0;

        if (index < 0 || index >= dim) {
            throw new IllegalArgumentException("Index " + index + " is not in the range [0, " + dim + ")!");
        }

        double[][] data = this.getInternalData();
        int newDim = dim - 1;

        double[][] newData = new double[newDim][];
        System.arraycopy(data, 0, newData, 0, index);
        System.arraycopy(data, index + 1, newData, index, newDim - index);

        // The rows, from index and on, now have one element after the diagonal.
        // Givens rotations of the columns p and p+1 restore the triangular form.
        for (int p = index; p < newDim; p++) {

            double[] rowP = newData[p];
            double a = rowP[p];
            double b = rowP[p + 1];
            double r = Math.hypot(a, b);
            double c = a / r;
            double s = b / r;

            rowP[p] = r;
            rowP[p + 1] = ZERO;

            for (int q = p + 1; q < newDim; q++) {
                double[] rowQ = newData[q];
                double x = rowQ[p];
                double y = rowQ[p + 1];
                rowQ[p] = c * x + s * y;
                rowQ[p + 1] = c * y - s * x;
            }
        }

        if (newDim > 0) {
            newData[0] = Arrays.copyOf(newData[0], newDim);
        }

        this.setInternalData(newData);

        myMaxDiag = MACHINE_SMALLEST;
        myMinDiag = MACHINE_LARGEST;
        for (int ij = 0; ij < newDim; ij++) {
            double pivot = newData[ij][ij] * newData[ij][ij];
            myMaxDiag = MAX.invoke(myMaxDiag, pivot);
            myMinDiag = MIN.invoke(myMinDiag, pivot);
        }
        mySPD = true;

        return this.computed(true);
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
