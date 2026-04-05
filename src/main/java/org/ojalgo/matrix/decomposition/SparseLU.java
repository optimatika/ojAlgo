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
import java.util.Optional;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.SparseArray;
import org.ojalgo.array.operation.SortAll;
import org.ojalgo.matrix.operation.SubstituteBackwards;
import org.ojalgo.matrix.operation.SubstituteForwards;
import org.ojalgo.matrix.store.*;
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
 */
public final class SparseLU extends AbstractDecomposition<Double, R064Store> implements LU<Double> {

    /**
     * [A]=[P][L][U][Q]
     */
    static final class FactorL extends AbstractDecomposition.PrimitiveFactor {

        private final R064CSR myBody;

        FactorL(final R064CSR body) {
            super();
            myBody = body;
        }

        @Override
        public void btran(final double[] arg) {
            SubstituteBackwards.invoke(arg, myBody);
        }

        @Override
        public void ftran(final double[] arg) {
            SubstituteForwards.invoke(arg, myBody);
        }

        @Override
        public MatrixStore<Double> get() {
            return myBody.triangular(false, true);
        }

        @Override
        public int getColDim() {
            return myBody.getMinDim();
        }

        @Override
        public int getRowDim() {
            return myBody.getRowDim();
        }

    }

    /**
     * [A]=[P][L][U][Q]
     */
    static final class FactorU extends AbstractDecomposition.PrimitiveFactor {

        private final double[] myBodyDiagonal;
        private final RowsSupplier<Double> myBodyMain;

        FactorU(final RowsSupplier<Double> u, final double[] diagU) {
            super();
            myBodyMain = u;
            myBodyDiagonal = diagU;
        }

        @Override
        public void btran(final double[] arg) {
            int r = myBodyDiagonal.length;
            for (int ij = 0; ij < r; ij++) {
                double varJ = arg[ij] / myBodyDiagonal[ij];
                arg[ij] = varJ;
                myBodyMain.getRow(ij).axpy(-varJ, arg);
            }
        }

        @Override
        public void ftran(final double[] arg) {
            int r = myBodyDiagonal.length;
            for (int ij = r - 1; ij >= 0; ij--) {
                arg[ij] = (arg[ij] - myBodyMain.getRow(ij).dot(arg)) / myBodyDiagonal[ij];
            }
        }

        @Override
        public MatrixStore<Double> get() {
            MatrixStore<Double> retVal = myBodyMain.triangular(true, false).superimpose(DiagonalStore.wrap(myBodyDiagonal));
            int nbCols = this.getColDim();
            if (this.getRowDim() > nbCols) {
                retVal = retVal.limits(nbCols, nbCols);
            }
            return retVal;
        }

        @Override
        public int getColDim() {
            return myBodyMain.getColDim();
        }

        @Override
        public int getRowDim() {
            return myBodyDiagonal.length;
        }

    }

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
        public void btran(final double[] arg) {

            double rowValue = arg[myTo];
            myElements.axpy(rowValue, arg);

            double tmp = arg[myTo];
            System.arraycopy(arg, myFrom, arg, myFrom + 1, myTo - myFrom);
            arg[myFrom] = tmp;
        }

        @Override
        public void btran(final PhysicalStore<Double> arg) {

            double rowValue = arg.doubleValue(myTo);
            myElements.axpy(rowValue, arg);

            double tmp = arg.doubleValue(myTo);
            for (int i = myTo; i > myFrom; i--) {
                arg.set(i, arg.doubleValue(i - 1));
            }
            arg.set(myFrom, tmp);
        }

        public int countNonzeros() {
            return myElements.countNonzeros();
        }

        @Override
        public void ftran(final double[] arg) {

            double tmp = arg[myFrom];
            System.arraycopy(arg, myFrom + 1, arg, myFrom, myTo - myFrom);
            arg[myTo] = tmp;

            arg[myTo] += myElements.dot(arg);
        }

        @Override
        public void ftran(final PhysicalStore<Double> arg) {

            double tmp = arg.doubleValue(myFrom);
            for (int i = myFrom; i < myTo; i++) {
                arg.set(i, arg.doubleValue(i + 1));
            }
            arg.set(myTo, tmp);

            arg.add(myTo, myElements.dot(arg));
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
    /**
     * Running total of nonzeros across eta factors accumulated since the last factorisation. Incremented in
     * {@link #doUpdateColumn} and reset alongside {@link #myFactors}.
     */
    private int myEtaNonzeros = 0;
    /**
     * Total nonzeros in L + U at last factorisation. Used by {@link #countFactorNonzeros()}.
     */
    private int myFactorNonzeros = 0;
    private final List<InvertibleFactor<Double>> myFactors = new ArrayList<>();
    private R064CSR myFixedL;
    private RowsSupplier<Double> myL;
    private final Pivot myPivot;
    private RowsSupplier<Double> myU;
    private transient long[] myValuesToSort = null;
    private transient R064Store myWorkerColumn = null;
    private transient R064Store myWorkerRow = null;

    public SparseLU() {

        super(R064Store.FACTORY);

        myPivot = new Pivot();
        myColPivot = null;
    }

    @Override
    public void btran(final double[] arg) {

        if (myColPivot != null && myColPivot.isModified()) {
            myColPivot.applyPivotOrder(arg);
        }

        this.btranU(arg);

        InvertibleFactor.btran(myFactors, arg);

        this.btranL(arg);

        myPivot.applyReverseOrder(arg);
    }

    @Override
    public void btran(final PhysicalStore<Double> arg) {
        InvertibleFactor.doPrimitive(arg, this);
    }

    @Override
    public Double calculateDeterminant(final Access2D<?> matrix) {
        this.decompose(this.wrap(matrix));
        return this.getDeterminant();
    }

    /**
     * Total nonzeros across all eta factors accumulated since the last factorisation. Each eta factor
     * corresponds to one Forrest-Tomlin update (column replacement in the basis).
     */
    public int countEtaNonzeros() {
        return myEtaNonzeros;
    }

    /**
     * Total nonzeros in the L and U factors at the time of the last full factorisation (excluding the U
     * diagonal which is always dense). Returns 0 before the first factorisation.
     */
    public int countFactorNonzeros() {
        return myFactorNonzeros;
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

        int m = matrix.getRowDim();
        int n = matrix.getColDim();
        int r = Math.min(m, n);

        Sliceable<Double> columns = SparseLU.cast(matrix);

        double[] wColData = this.getWorkerColumn(m).data;

        for (int j = 0; j < n; j++) {
            columns.sliceColumn(j).supplyTo(wColData);
            this.doFactorColumn(j, m, r, wColData);
        }

        return this.doFactorFinish();
    }

    /**
     * Factorises the basis formed by selecting columns from a CSC matrix. This is the primary entry point for
     * the revised simplex solver.
     *
     * @param matrix  The full constraint matrix in CSC format
     * @param columns The indices of the columns that form the basis
     */
    public boolean factor(final R064CSC matrix, final int[] columns) {

        int m = matrix.getRowDim();
        int n = columns.length;
        int r = Math.min(m, n);

        this.reset(m, n);

        long[] keys = this.getValuesToSort(matrix, columns, n);

        if (myColPivot == null) {
            myColPivot = new Pivot();
        }
        myColPivot.reset(n);
        int[] order = myColPivot.getOrder();

        myColPivot.setModified(SortAll.sort(keys, order));

        double[] wColData = this.getWorkerColumn(m).data;

        for (int j = 0; j < n; j++) {
            matrix.supplyTo(columns[order != null ? order[j] : j], wColData);
            this.doFactorColumn(j, m, r, wColData);
        }

        return this.doFactorFinish();
    }

    @Override
    public void ftran(final double[] arg) {

        myPivot.applyPivotOrder(arg);

        this.ftranL(arg);

        InvertibleFactor.ftran(myFactors, arg);

        this.ftranU(arg);

        if (myColPivot != null && myColPivot.isModified()) {
            myColPivot.applyReverseOrder(arg);
        }
    }

    @Override
    public void ftran(final PhysicalStore<Double> arg) {
        InvertibleFactor.doPrimitive(this, arg);
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

    /**
     * [A]=[P][L][etas...][U][Q]
     */
    @Override
    public List<InvertibleFactor<Double>> getFactors() {

        List<InvertibleFactor<Double>> retVal = new ArrayList<>();
        retVal.add(this.getFactorP());
        retVal.add(this.getFactorL());
        retVal.addAll(myFactors);
        retVal.add(this.getFactorU());

        if (myColPivot != null && myColPivot.isModified()) {
            retVal.add(this.getFactorQ().get());
        }

        return retVal;
    }

    @Override
    public MatrixStore<Double> getInverse(final PhysicalStore<Double> preallocated) {
        return this.getSolution(R064Store.FACTORY.makeIdentity(this.getMinDim()), preallocated);
    }

    @Override
    public MatrixStore<Double> getL() {
        return this.getFactorL().get();
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
            double[] colData = column.data;

            for (int col = 0; col < nbSolutions; col++) {

                column.fillMatching(preallocated.sliceColumn(col));

                this.ftranL(colData);

                InvertibleFactor.ftran(myFactors, colData);

                this.ftranU(colData);

                preallocated.fillColumn(col, column);
            }

        } else {

            double[] data = ((R064Store) preallocated).data;

            this.ftranL(data);

            InvertibleFactor.ftran(myFactors, data);

            this.ftranU(data);
        }

        if (myColPivot != null && myColPivot.isModified()) {
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
        MatrixStore<Double> retVal = this.getFactorU().get();
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
     * The Forrest-Tomlin update algorithm
     */
    @Override
    public boolean updateColumn(final int specifiedColumn, final Access1D.Collectable<Double, ? super TransformableRegion<Double>> newColumn) {

        newColumn.supplyTo(this.getWorkerColumn(this.getRowDim()));

        return this.doUpdateColumn(specifiedColumn);
    }

    /**
     * Forrest-Tomlin update reading the new column directly from a CSC matrix.
     *
     * @param specifiedColumn The basis position being replaced
     * @param matrix          The full constraint matrix in CSC format
     * @param sourceColumn    The column index in the CSC matrix to read
     * @return true if the update succeeded
     */
    public boolean updateColumn(final int specifiedColumn, final R064CSC matrix, final int sourceColumn) {

        double[] wColData = this.getWorkerColumn(this.getRowDim()).data;
        matrix.supplyTo(sourceColumn, wColData);

        return this.doUpdateColumn(specifiedColumn);
    }

    private void btranL(final double[] arg) {
        SubstituteBackwards.invoke(arg, myFixedL);
    }

    private void btranU(final double[] arg) {
        int r = myDiagU.length;
        for (int ij = 0; ij < r; ij++) {
            double varJ = arg[ij] / myDiagU[ij];
            arg[ij] = varJ;
            myU.getRow(ij).axpy(-varJ, arg);
        }
    }

    /**
     * Processes one column during factorisation. Caller must have filled wColData with the raw column values.
     */
    private void doFactorColumn(final int j, final int m, final int r, final double[] column) {

        myPivot.applyPivotOrder(column);

        this.ftranMutableL(column);

        int p = j;
        double maxMag = Math.abs(column[j]);
        for (int i = j + 1; i < m; i++) {
            double mag = Math.abs(column[i]);
            if (mag > maxMag) {
                p = i;
                maxMag = mag;
            }
        }

        if (p != j) {

            myPivot.change(p, j);

            double tmpVal = column[p];
            column[p] = column[j];
            column[j] = tmpVal;

            myL.exchangeRows(p, j);
        }

        double tmpNumer, tmpDenom = column[j];

        for (int i = 0, limit = Math.min(m, j); i < limit; i++) {
            tmpNumer = column[i];
            myU.putLast(i, j, tmpNumer);
        }
        if (j < r) {
            myDiagU[j] = tmpDenom;
        }

        if (j < m && tmpDenom != ZERO) {
            for (int i = j + 1; i < m; i++) {
                tmpNumer = column[i];
                myL.putLast(i, j, tmpNumer / tmpDenom);
            }
        }
    }

    private boolean doFactorFinish() {

        myFixedL = myL.toCSR();

        myFactorNonzeros = myFixedL.countNonzeros() + myU.countNonzeros();

        return this.computed(true);
    }

    /**
     * Shared Forrest-Tomlin update logic. Assumes wColData (from {@link #getWorkerColumn(int)}) is already
     * filled with the raw new column values.
     */
    private boolean doUpdateColumn(final int specifiedColumn) {

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

        myPivot.applyPivotOrder(wColData);

        this.ftranL(wColData);

        InvertibleFactor.ftran(myFactors, wColData);

        double diag = NaN;
        int lastRowNonZero = -1;
        for (int i = m - 1; i >= 0; i--) {
            if (!FletcherMatthews.PRECISION.isZero(wColData[i])) {
                lastRowNonZero = i;
                diag = wColData[i];
                break;
            }
        }

        if (lastRowNonZero < columnIndex) {

            return false;

        } else if (FletcherMatthews.SAFE.isZero(diag)) {

            return false;

        } else if (lastRowNonZero == columnIndex) {

            for (int i = 0; i < columnIndex; i++) {
                myU.set(i, columnIndex, wColData[i]);
            }
            myDiagU[columnIndex] = wColData[columnIndex];

        } else {

            myColPivot.cycle(columnIndex, lastRowNonZero);

            myU.doCyclicFT(columnIndex, wRow, lastRowNonZero, wCol);

            for (int i = columnIndex; i < lastRowNonZero; i++) {
                myDiagU[i] = myDiagU[i + 1];
            }

            PermutationEta eta = new PermutationEta(r, columnIndex, lastRowNonZero);

            for (int ij = columnIndex; ij < lastRowNonZero; ij++) {
                double denom = myDiagU[ij];
                double numer = wRowData[ij];
                double ratio = numer / denom;

                eta.set(ij, -ratio);

                myU.getRow(ij).axpy(-ratio, wRow);
            }

            if (FletcherMatthews.SAFE.isZero(wRowData[lastRowNonZero])) {
                return false;
            }

            myDiagU[lastRowNonZero] = wRowData[lastRowNonZero];
            for (int j = lastRowNonZero + 1; j < n; j++) {
                myU.set(lastRowNonZero, j, wRowData[j]);
            }

            myFactors.add(eta);
            myEtaNonzeros += eta.countNonzeros();
        }

        return true;
    }

    private void ftranL(final double[] arg) {
        SubstituteForwards.invoke(arg, myFixedL);
    }

    /**
     * Forward substitution using the mutable L factor (RowsSupplier). Used during factorisation before
     * myFixedL is available.
     */
    private void ftranMutableL(final double[] arg) {
        int r = myL.getMinDim();
        for (int ij = 1; ij < r; ij++) {
            arg[ij] -= myL.getRow(ij).dot(arg);
        }
    }

    private void ftranU(final double[] arg) {
        for (int ij = myDiagU.length - 1; ij >= 0; ij--) {
            arg[ij] = (arg[ij] - myU.getRow(ij).dot(arg)) / myDiagU[ij];
        }
    }

    @Override
    protected boolean checkSolvability() {
        return this.isSquare() && this.isFullRank();
    }

    /**
     * [A]=[P][L][U][Q]
     */
    MatrixDecomposition.Factor<Double> getFactorL() {
        return new FactorL(myFixedL);
    }

    /**
     * [A]=[P][L][U][Q]
     */
    MatrixDecomposition.Factor<Double> getFactorP() {
        return new FactorPivot<>(this.makeIdentity(this.getRowDim()), myPivot, true);
    }

    /**
     * [A]=[P][L][U][Q]
     */
    Optional<MatrixDecomposition.Factor<Double>> getFactorQ() {

        if (myColPivot != null && myColPivot.isModified()) {
            return Optional.of(new FactorPivot<>(this.makeIdentity(this.getColDim()), myColPivot, false));
        } else {
            return Optional.empty();
        }
    }

    /**
     * [A]=[P][L][U][Q]
     */
    MatrixDecomposition.Factor<Double> getFactorU() {
        return new FactorU(myU, myDiagU);
    }

    /**
     * Builds column-ordering keys from a CSC matrix with a column selection. The key for each selected column
     * encodes (lastRowIndex + 1) in the upper 32 bits and nnz in the lower 32 bits — same encoding as the
     * {@link ColumnsSupplier.Selection} variant.
     */
    long[] getValuesToSort(final R064CSC matrix, final int[] columns, final int n) {

        if (myValuesToSort == null || myValuesToSort.length != n) {
            myValuesToSort = new long[n];
        }

        for (int j = 0; j < n; j++) {
            int col = columns[j];
            int p0 = matrix.pointers[col];
            int pm = matrix.pointers[col + 1];
            int nnz = pm - p0;
            long lastRow = nnz > 0 ? matrix.indices[pm - 1] + 1L : 0L;
            myValuesToSort[j] = (lastRow << 32) | (nnz & 0xFFFFFFFFL);
        }

        return myValuesToSort;
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

    /**
     * Reset for a specified m x n factorisation — used by {@link #factor(R064CSC, int[])}.
     */
    void reset(final int m, final int n) {

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
        myEtaNonzeros = 0;

        myFixedL = null;
    }

    void reset(final Structure2D matrix) {
        this.reset(matrix.getRowDim(), matrix.getColDim());
    }

}
