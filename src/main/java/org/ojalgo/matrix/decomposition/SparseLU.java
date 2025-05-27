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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.SparseArray;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.matrix.store.DiagonalStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RowsSupplier;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.matrix.transformation.InvertibleFactor;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.structure.Access2D.Sliceable;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.NumberDefinition;

/**
 * A sparse, primitive double based, LU decomposition with support for incremental Forrest-Tomlin updates.
 * <p>
 * Performance priorities in this order:
 * <ol>
 * <li>FTRAN/BTRAN operations
 * <li>Column updates
 * <li>Full decomposition of (new) matrix
 * </ol>
 * <p>
 * Storage format:
 * <ul>
 * <li>L matrix stored in CSC (Compressed Sparse Column) format for efficient column operations
 * <li>U matrix stored in CSR (Compressed Sparse Row) format for efficient row operations
 * <li>Diagonal elements of U stored separately for quick access during updates
 * </ul>
 * <p>
 * The implementation uses the Forrest-Tomlin update algorithm with Suhl's improvement for efficient column
 * modifications.
 */
final class SparseLU extends AbstractDecomposition<Double, R064Store> implements LU<Double> {

    static final class PermutationEta implements InvertibleFactor<Double>, Mutate1D {

        private final int myDim;
        private final SparseArray<Double> myElements;
        private final int myFrom;
        private final int myTo;

        PermutationEta(final int dim, final int from, final int to) {
            super();
            myDim = dim;
            myFrom = from;
            myTo = to;
            myElements = SparseArray.factory(ArrayR064.FACTORY).make(dim);
        }

        @Override
        public void btran(final PhysicalStore<Double> arg) {

            double rowValue = arg.doubleValue(myTo);
            for (NonzeroView<Double> nz : myElements.nonzeros()) {
                arg.add(nz.index(), nz.doubleValue() * rowValue);
            }

            double tmp = arg.doubleValue(myTo);
            for (int i = myTo; i > myFrom; i--) {
                arg.set(i, arg.doubleValue(i - 1));
            }
            arg.set(myFrom, tmp);
        }

        @Override
        public void ftran(final PhysicalStore<Double> arg) {

            double tmp = arg.doubleValue(myFrom);
            for (int i = myFrom; i < myTo; i++) {
                arg.set(i, arg.doubleValue(i + 1));
            }
            arg.set(myTo, tmp);

            double sum = ZERO;
            for (NonzeroView<Double> nz : myElements.nonzeros()) {
                sum += nz.doubleValue() * arg.doubleValue(nz.index());
            }
            arg.add(myTo, sum);
        }

        @Override
        public int getColDim() {
            return myDim;
        }

        @Override
        public int getRowDim() {
            return myDim;
        }

        @Override
        public void reset() {
            myElements.reset();
        }

        @Override
        public void set(final int j, final double value) {
            myElements.set(j, value);
        }

        @Override
        public void set(final long index, final Comparable<?> value) {
            myElements.set(index, NumberDefinition.doubleValue(value));
        }

    }

    private static Access2D.Sliceable<Double> cast(final Collectable<Double, ? super TransformableRegion<Double>> matrix) {

        if (matrix instanceof Access2D.Sliceable<?>) {
            return (Access2D.Sliceable<Double>) matrix;
        } else {
            return matrix.collect(SparseStore.R064);
        }
    }

    private Pivot myColPivot;
    /**
     * U diagonal elements
     */
    private double[] myDiagU;
    private final List<InvertibleFactor<Double>> myFactors = new ArrayList<>();
    private RowsSupplier<Double> myL;
    private final Pivot myPivot;
    private RowsSupplier<Double> myU;
    private transient R064Store myWorkerColumn = null;
    private transient R064Store myWorkerRow = null;

    SparseLU() {

        super(R064Store.FACTORY);

        myPivot = new Pivot();
        myColPivot = null;
    }

    @Override
    public void btran(final PhysicalStore<Double> arg) {

        int r = this.getMinDim();

        if (myColPivot != null) {
            this.applyPivotOrder(myColPivot, arg);
        }

        for (int ij = 0; ij < r; ij++) {
            double varJ = arg.doubleValue(ij) / myDiagU[ij];
            arg.set(ij, varJ);
            myU.getRow(ij).axpy(-varJ, arg);
        }

        for (int i = myFactors.size() - 1; i >= 0; i--) {
            myFactors.get(i).btran(arg);
        }

        for (int ij = r - 1; ij > 0; ij--) {
            double varJ = arg.doubleValue(ij);
            myL.getRow(ij).axpy(-varJ, arg);
        }

        this.applyReverseOrder(myPivot, arg);
    }

    @Override
    public Double calculateDeterminant(final Access2D<?> matrix) {
        this.decompose(this.wrap(matrix));
        return this.getDeterminant();
    }

    @Override
    public int countSignificant(final double threshold) {

        int significant = 0;
        for (int ij = 0, limit = this.getMinDim(); ij < limit; ij++) {
            if (Math.abs(myDiagU[ij]) > threshold) {
                significant++;
            }
        }

        return significant;
    }

    @Override
    public boolean decompose(final Collectable<Double, ? super TransformableRegion<Double>> matrix) {

        this.reset(matrix);

        Sliceable<Double> columns = SparseLU.cast(matrix);

        int m = matrix.getRowDim();
        int n = matrix.getColDim();
        int r = matrix.getMinDim();

        R064Store wCol = this.getWorkerColumn(m);
        double[] wColData = wCol.data;

        for (int j = 0; j < n; j++) {

            Access1D<Double> sliced = columns.sliceColumn(j);
            sliced.supplyTo(wColData);

            myPivot.applyPivotOrder(wCol);

            for (int i = 0; i < m; i++) {
                wColData[i] -= myL.getRow(i).dot(wCol);
            }

            int p = j;
            double magnP = Math.abs(wColData[p]);
            double magnI;
            for (int i = j + 1; i < m; i++) {
                magnI = Math.abs(wColData[i]);
                if (magnI > magnP) {
                    p = i;
                    magnP = magnI;
                }
            }
            if (p != j) {

                myPivot.change(p, j);

                double tmpVal = wColData[p];
                wColData[p] = wColData[j];
                wColData[j] = tmpVal;

                myL.exchangeRows(p, j);
            }

            double tmpNumer, tmpDenom = wColData[j];

            for (int i = 0, limit = Math.min(m, j); i < limit; i++) {
                tmpNumer = wColData[i];
                myU.putLast(i, j, tmpNumer);
            }
            if (j < r) {
                myDiagU[j] = tmpDenom;
            }

            if (j < m && tmpDenom != ZERO) {
                for (int i = j + 1; i < m; i++) {
                    tmpNumer = wColData[i];
                    myL.putLast(i, j, tmpNumer / tmpDenom);
                }
            }
        }

        return this.computed(true);
    }

    @Override
    public void ftran(final PhysicalStore<Double> arg) {

        this.applyPivotOrder(myPivot, arg);

        this.ftranInternal(arg);

        if (myColPivot != null) {
            this.applyReverseOrder(myColPivot, arg);
        }
    }

    @Override
    public int getColDim() {
        return myU.getColDim();
    }

    @Override
    public Double getDeterminant() {

        double retVal = myPivot.signum();

        for (int j = 0, limit = myDiagU.length; j < limit; j++) {
            retVal *= myDiagU[j];
        }

        return Double.valueOf(retVal);
    }

    @Override
    public MatrixStore<Double> getInverse(final PhysicalStore<Double> preallocated) {
        return this.getSolution(R064Store.FACTORY.makeIdentity(this.getMinDim()), preallocated);
    }

    @Override
    public MatrixStore<Double> getL() {
        return myL.triangular(false, true);
    }

    @Override
    public int[] getPivotOrder() {
        return myPivot.getOrder();
    }

    @Override
    public double getRankThreshold() {

        double largest = MACHINE_SMALLEST;
        for (int j = 0, limit = myDiagU.length; j < limit; j++) {
            largest = Math.max(largest, Math.abs(myDiagU[j]));
        }

        return largest * this.getDimensionalEpsilon();
    }

    @Override
    public int[] getReversePivotOrder() {
        return myPivot.reverseOrder();
    }

    @Override
    public int getRowDim() {
        return myL.getRowDim();
    }

    @Override
    public MatrixStore<Double> getSolution(final Collectable<Double, ? super PhysicalStore<Double>> rhs, final PhysicalStore<Double> preallocated) {

        rhs.supplyTo(preallocated);

        this.applyPivotOrder(myPivot, preallocated);

        int nbSolutions = rhs.getColDim();

        if (nbSolutions > 1) {

            R064Store column = R064Store.FACTORY.make(rhs.getRowDim(), 1);

            for (int col = 0; col < nbSolutions; col++) {

                column.fillMatching(preallocated.sliceColumn(col));
                this.ftranInternal(column);
                preallocated.fillColumn(col, column);
            }

        } else {

            for (int col = 0; col < nbSolutions; col++) {
                this.ftranInternal(preallocated);
            }
        }

        if (myColPivot != null) {
            this.applyReverseOrder(myColPivot, preallocated);
        }

        return preallocated;
    }

    /**
     * If {@link #updateColumn(int, Access1D.Collectable)} or
     * {@link #updateColumn(int, Access1D.Collectable, PhysicalStore)} has been invoked, then this is no
     * longer guaranteed to be triangular.
     */
    @Override
    public MatrixStore<Double> getU() {
        MatrixStore<Double> retVal = myU.triangular(true, false).superimpose(DiagonalStore.wrap(myDiagU));
        int nbCols = this.getColDim();
        if (this.getRowDim() > nbCols) {
            retVal = retVal.limits(nbCols, nbCols);
        }
        if (myColPivot != null && myColPivot.isModified()) {
            retVal = retVal.columns(myColPivot.reverseOrder());
        }
        return retVal;
    }

    @Override
    public MatrixStore<Double> invert(final Access2D<?> original, final PhysicalStore<Double> preallocated) throws RecoverableCondition {

        this.decompose(this.wrap(original));

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

        this.decompose(this.wrap(body));

        if (this.isSolvable()) {
            return this.getSolution(this.wrap(rhs), preallocated);
        } else {
            throw RecoverableCondition.newEquationSystemNotSolvable();
        }
    }

    /**
     * Should implement the Forrest-Tomlin update algorithm (maybe with Suhl's improvement).
     */
    @Override
    public boolean updateColumn(final int specifiedColumn, final Access1D.Collectable<Double, ? super TransformableRegion<Double>> newColumn) {

        int m = this.getRowDim();
        int n = this.getColDim();
        int r = this.getMinDim();

        R064Store wRow = this.getWorkerRow(n);
        double[] wRowData = wRow.data;
        R064Store wCol = this.getWorkerColumn(m);
        double[] wColData = wCol.data;

        if (myColPivot == null) {
            myColPivot = new Pivot();
            myColPivot.reset(n);
        }

        int columnIndex = myColPivot.locationOf(specifiedColumn);

        newColumn.supplyTo(wCol);
        myPivot.applyPivotOrder(wCol);

        for (int ij = 1; ij < r; ij++) {
            wCol.add(ij, -myL.getRow(ij).dot(wCol));
        }

        // Apply any existing transformations to the new column
        for (int i = 0; i < myFactors.size(); i++) {
            myFactors.get(i).ftran(wCol);
        }

        // After forward substitution is complete, find the last non-zero row
        double diag = NaN;
        int lastRowNonZero = -1;
        for (int i = m - 1; i >= 0; i--) {
            if (!FletcherMatthews.PRECISION.isZero(wCol.doubleValue(i))) {
                lastRowNonZero = i;
                diag = wCol.doubleValue(i);
                break; // Stop as soon as we find a non-zero value
            }
        }

        if (lastRowNonZero < columnIndex) {

            // This means the updated matrix is singular
            return false;

        } else if (FletcherMatthews.SAFE.isZero(diag)) {

            // Numerically unstable
            return false;

        } else if (lastRowNonZero == columnIndex) {

            // Lucky!
            for (int i = 0; i < columnIndex; i++) {
                myU.set(i, columnIndex, wCol.doubleValue(i));
            }
            myDiagU[columnIndex] = wCol.doubleValue(columnIndex);

        } else {

            myColPivot.cycle(columnIndex, lastRowNonZero);

            myU.doCyclicFT(columnIndex, wRow, lastRowNonZero, wCol);

            for (int i = columnIndex; i < lastRowNonZero; i++) {
                myDiagU[i] = myDiagU[i + 1];
            }

            // Permutation perm = new Permutation(r, columnIndex, lastRowNonZero);
            // myFactors.add(perm);

            PermutationEta eta = new PermutationEta(r, columnIndex, lastRowNonZero);

            for (int ij = columnIndex; ij < lastRowNonZero; ij++) {
                double denom = myDiagU[ij];
                double numer = wRowData[ij];
                double ratio = numer / denom;

                eta.set(ij, -ratio);

                // wRowData[ij] = ZERO;

                myU.getRow(ij).axpy(-ratio, wRow);

            }

            myDiagU[lastRowNonZero] = wRowData[lastRowNonZero];
            for (int j = lastRowNonZero + 1; j < n; j++) {
                myU.set(lastRowNonZero, j, wRowData[j]);
            }

            myFactors.add(eta);
        }

        return true;
    }

    private void ftranInternal(final PhysicalStore<Double> arg) {

        int r = this.getMinDim();

        for (int ij = 1; ij < r; ij++) {
            arg.add(ij, 0, -myL.getRow(ij).dot(arg));
        }

        for (int i = 0; i < myFactors.size(); i++) {
            myFactors.get(i).ftran(arg);
        }

        for (int ij = r - 1; ij >= 0; ij--) {
            double sum = arg.doubleValue(ij);
            sum -= myU.getRow(ij).dot(arg);
            arg.set(ij, 0, sum / myDiagU[ij]);
        }
    }

    @Override
    protected boolean checkSolvability() {
        return this.isSquare() && this.isFullRank();
    }

    R064Store getWorkerColumn(final int nbRows) {
        if (myWorkerColumn == null || myWorkerColumn.getRowDim() != nbRows) {
            myWorkerColumn = R064Store.FACTORY.make(nbRows, 1);
        }
        return myWorkerColumn;
    }

    R064Store getWorkerRow(final int nbCols) {
        if (myWorkerRow == null || myWorkerRow.getColDim() != nbCols) {
            myWorkerRow = R064Store.FACTORY.make(1, nbCols);
        }
        return myWorkerRow;
    }

    void reset(final Structure2D matrix) {

        int m = matrix.getRowDim();
        int n = matrix.getColDim();
        int r = Math.min(m, n);

        myPivot.reset(m);
        if (myColPivot != null) {
            myColPivot.reset(n);
        }

        if (myL != null && myL.getRowDim() == m && myL.getColDim() == r) {
            myL.reset();
        } else {
            myL = R064Store.FACTORY.makeRowsSupplier(r);
            myL.addRows(m);
        }

        if (myDiagU != null && myDiagU.length == r) {
            Arrays.fill(myDiagU, ZERO);
        } else {
            myDiagU = new double[r];
        }

        if (myU != null && myU.getRowDim() == r && myU.getColDim() == n) {
            myU.reset();
        } else {
            myU = R064Store.FACTORY.makeRowsSupplier(n);
            myU.addRows(r);
        }

        myFactors.clear();
    }

}
