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
import org.ojalgo.structure.Structure1D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.context.NumberContext;

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

        private final int myCount;
        private final DualSparse myDualBase;
        private final int[] myPivotRow;
        private final List<SparseArray<Double>> myUpdateColumns;
        private final double[] myUpdateDiagonal;
        private final int myUpdateOffset;

        FactorU(final DualSparse dualBase, final int[] pivotRow, final int count, final List<SparseArray<Double>> updateColumns, final double[] updateDiagonal,
                final int updateOffset) {
            super();
            myDualBase = dualBase;
            myPivotRow = pivotRow;
            myCount = count;
            myUpdateColumns = updateColumns;
            myUpdateDiagonal = updateDiagonal;
            myUpdateOffset = updateOffset;
        }

        @Override
        public void btran(final double[] arg) {
            int totalCols = myPivotRow.length;
            double[] baseDiag = myDualBase.getDiagonal();
            int splitEnd = Math.min(myUpdateOffset, totalCols);
            for (int iLogic = 0; iLogic < splitEnd; iLogic++) {
                int pivotRow = myPivotRow[iLogic];
                if (pivotRow < 0) {
                    continue;
                }
                arg[pivotRow] = (arg[pivotRow] - myDualBase.getColumn(iLogic).dot(arg)) / baseDiag[iLogic];
            }
            for (int iLogic = splitEnd; iLogic < totalCols; iLogic++) {
                int pivotRow = myPivotRow[iLogic];
                if (pivotRow < 0) {
                    continue;
                }
                int k = iLogic - myUpdateOffset;
                arg[pivotRow] = (arg[pivotRow] - myUpdateColumns.get(k).dot(arg)) / myUpdateDiagonal[k];
            }
        }

        @Override
        public void ftran(final double[] arg) {
            int totalCols = myPivotRow.length;
            int splitEnd = Math.min(myUpdateOffset, totalCols);
            for (int iLogic = totalCols - 1; iLogic >= splitEnd; iLogic--) {
                int pivotRow = myPivotRow[iLogic];
                if (pivotRow < 0) {
                    continue;
                }
                int k = iLogic - myUpdateOffset;
                double pivotMultiplier = arg[pivotRow] / myUpdateDiagonal[k];
                arg[pivotRow] = pivotMultiplier;
                if (pivotMultiplier != 0.0) {
                    myUpdateColumns.get(k).axpy(-pivotMultiplier, arg);
                }
            }
            double[] baseDiag = myDualBase.getDiagonal();
            for (int iLogic = splitEnd - 1; iLogic >= 0; iLogic--) {
                int pivotRow = myPivotRow[iLogic];
                if (pivotRow < 0) {
                    continue;
                }
                double pivotMultiplier = arg[pivotRow] / baseDiag[iLogic];
                arg[pivotRow] = pivotMultiplier;
                if (pivotMultiplier != 0.0) {
                    myDualBase.getColumn(iLogic).axpy(-pivotMultiplier, arg);
                }
            }
        }

        @Override
        public MatrixStore<Double> get() {
            return myDualBase.getColumns().limits(myCount, myCount).superimpose(DiagonalStore.wrap(Arrays.copyOf(myDualBase.getDiagonal(), myCount)));
        }

        @Override
        public int getColDim() {
            return myCount;
        }

        @Override
        public int getRowDim() {
            return myCount;
        }

    }

    /**
     * R-matrix (Forrest-Tomlin) eta factor for one basis update. Stores ep-based entries:
     * {@code -epPartial[j] * oldPivot} for each non-pivot row j where epPartial is nonzero.
     * <p>
     * ftranFT (gather): {@code v[pivotRow] -= sum(v[index[k]] * value[k])}
     * <p>
     * btranFT (scatter): {@code v[index[k]] -= v[pivotRow] * value[k]}
     */
    static final class RMatrixEta implements InvertibleFactor<Double> {

        private int myCount;
        private final int myDim;
        private int[] myIndices;
        private final int myPivotRow;
        private double[] myValues;

        RMatrixEta(final int pivotRow, final int dim) {
            super();
            myPivotRow = pivotRow;
            myDim = dim;
            myCount = 0;
            myIndices = new int[8];
            myValues = new double[8];
        }

        @Override
        public void btran(final double[] arg) {
            double pivVal = arg[myPivotRow];
            if (pivVal != ZERO) {
                for (int k = 0; k < myCount; k++) {
                    arg[myIndices[k]] -= pivVal * myValues[k];
                }
            }
        }

        @Override
        public void btran(final PhysicalStore<Double> arg) {
            double pivVal = arg.doubleValue(myPivotRow);
            if (pivVal != ZERO) {
                for (int k = 0; k < myCount; k++) {
                    int idx = myIndices[k];
                    arg.set(idx, arg.doubleValue(idx) - pivVal * myValues[k]);
                }
            }
        }

        @Override
        public void ftran(final double[] arg) {
            double value = arg[myPivotRow];
            for (int k = 0; k < myCount; k++) {
                value -= arg[myIndices[k]] * myValues[k];
            }
            arg[myPivotRow] = value;
        }

        @Override
        public void ftran(final PhysicalStore<Double> arg) {
            double value = arg.doubleValue(myPivotRow);
            for (int k = 0; k < myCount; k++) {
                value -= arg.doubleValue(myIndices[k]) * myValues[k];
            }
            arg.set(myPivotRow, value);
        }

        @Override
        public int getColDim() {
            return myDim;
        }

        @Override
        public int getRowDim() {
            return myDim;
        }

        void addEntry(final int row, final double value) {
            if (myCount == myIndices.length) {
                myIndices = Arrays.copyOf(myIndices, myCount * 2);
                myValues = Arrays.copyOf(myValues, myCount * 2);
            }
            myIndices[myCount] = row;
            myValues[myCount] = value;
            myCount++;
        }

        int countNonzeros() {
            return myCount;
        }

    }

    /**
     * Relative-magnitude gate for accepting an FT update pivot. An update is rejected when {@code |alpha|} is
     * small compared to {@code |oldPivot|}; this triggers a refactor before the roundoff in
     * {@code oldPivot*alpha} and {@code 1/alpha} can poison subsequent solves.
     * <p>
     * Tighter than {@link FletcherMatthews#SAFE} ({@code of(4)} ≈ 1e-4) which was found to reject too
     * aggressively on small/medium models, causing excessive refactors on the AGG/GROW/SCTAP family.
     */
    private static final NumberContext UPDATE_GATE = NumberContext.of(6);

    private static Access2D.Sliceable<Double> cast(final Collectable<Double, ? super TransformableRegion<Double>> matrix) {
        if (matrix instanceof Access2D.Sliceable<?>) {
            return (Access2D.Sliceable<Double>) matrix;
        } else {
            return matrix.collect(SparseStore.R064);
        }
    }

    private Pivot myColPivot;
    /**
     * Base U factor: fixed-size columns, rows and diagonal populated during factorisation. Logical indices
     * {@code 0 .. r-1} live here; FT updates never modify {@code myDualBase} structurally but do zero out
     * entries of voided columns/rows in place.
     */
    private DualSparse myDualBase;
    /**
     * Running total of nonzeros across R-factors accumulated since the last factorisation. Incremented in
     * {@link #doUpdateColumn} and reset alongside {@link #myFactors}.
     */
    private int myEtaNonzeros = 0;
    /**
     * Total nonzeros in L + U (off-diagonal) at last factorisation. Used by {@link #countFactorNonzeros()}.
     */
    private int myFactorNonzeros = 0;
    private final List<InvertibleFactor<Double>> myFactors = new ArrayList<>();
    private double myMaxPivotMagnitude = ZERO;
    private double myMinPivotMagnitude = Double.MAX_VALUE;
    /**
     * Diagonal magnitude bounds at the moment of {@link #doFactorFinish()}. Snapshot of the basis-only
     * spread; used as the baseline against which FT-update degradation is measured.
     */
    private double myFactorMaxPivotMagnitude = ZERO;
    private double myFactorMinPivotMagnitude = Double.MAX_VALUE;
    private R064CSR myFixedL;
    private RowsSupplier<Double> myL;
    private final Pivot myPivot;
    /**
     * Number of active (non-voided) logical columns. Equals the basis dimension after factorisation; stays
     * constant through updates (one void + one append per update).
     */
    private int myUActive;
    /**
     * Replacement columns created during FT updates, appended in order. Logical index {@code iLogic >=
     * myUpdateOffset} maps to {@code myUpdateColumns.get(iLogic - myUpdateOffset)}.
     */
    private final List<SparseArray<Double>> myUpdateColumns = new ArrayList<>();
    /**
     * Diagonal elements for update-appended columns, indexed by update offset. Parallel to
     * {@link #myUpdateColumns}. Grown via {@link Arrays#copyOf} as needed.
     */
    private double[] myUpdateDiagonal;
    /**
     * Logical-index boundary between {@link #myDualBase} and the update-appended storage. Set in
     * {@link #reset(int, int)} to {@code r} and never changes until the next reset.
     */
    private int myUpdateOffset;
    /**
     * Physical row → logical column that pivots on this row. Inverse of {@link #myUPivotRow} for active
     * entries. Analogous to HiGHS's {@code u_pivot_lookup}.
     */
    private int[] myUPivotLookup;
    /**
     * Logical column → pivot row. After factorisation, {@code myUPivotRow[j] = j} (identity). During FT
     * updates, voided columns get {@code -1}; appended columns get the new pivot row. This is analogous to
     * HiGHS's {@code u_pivot_index}.
     */
    private int[] myUPivotRow;
    private transient long[] myValuesToSort = null;
    private transient R064Store myWorkerColumn = null;

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

    /**
     * Largest pivot magnitude observed across the U diagonal — both the initial entries from factorisation
     * and the new diagonals produced by FT updates since.
     */
    public double getMaxPivotMagnitude() {
        return myMaxPivotMagnitude;
    }

    /**
     * Smallest pivot magnitude observed across the U diagonal — both the initial entries from factorisation
     * and the new diagonals produced by FT updates since.
     */
    public double getMinPivotMagnitude() {
        return myMinPivotMagnitude;
    }

    /**
     * {@link #getMaxPivotMagnitude()} as snapshotted at the moment of the last factorisation. Captures the
     * basis-only diagonal range, before any FT updates contributed.
     */
    public double getFactorMaxPivotMagnitude() {
        return myFactorMaxPivotMagnitude;
    }

    /**
     * {@link #getMinPivotMagnitude()} as snapshotted at the moment of the last factorisation. Captures the
     * basis-only diagonal range, before any FT updates contributed.
     */
    public double getFactorMinPivotMagnitude() {
        return myFactorMinPivotMagnitude;
    }

    @Override
    public int countSignificant(final double threshold) {

        int significant = 0;
        double[] baseDiag = myDualBase.getDiagonal();
        for (int iLogic = 0; iLogic < myUpdateOffset; iLogic++) {
            if (myUPivotRow[iLogic] >= 0 && Math.abs(baseDiag[iLogic]) > threshold) {
                significant++;
            }
        }
        for (int iLogic = myUpdateOffset, limit = myUPivotRow.length; iLogic < limit; iLogic++) {
            if (myUPivotRow[iLogic] >= 0 && Math.abs(myUpdateDiagonal[iLogic - myUpdateOffset]) > threshold) {
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
        return myUActive;
    }

    @Override
    public Double getDeterminant() {

        double retVal = myPivot.signum();

        double[] baseDiag = myDualBase.getDiagonal();
        for (int iLogic = 0; iLogic < myUpdateOffset; iLogic++) {
            if (myUPivotRow[iLogic] >= 0) {
                retVal *= baseDiag[iLogic];
            }
        }
        for (int iLogic = myUpdateOffset, limit = myUPivotRow.length; iLogic < limit; iLogic++) {
            if (myUPivotRow[iLogic] >= 0) {
                retVal *= myUpdateDiagonal[iLogic - myUpdateOffset];
            }
        }

        return Double.valueOf(retVal);
    }

    /**
     * [A]=[P][L][U][etas...][Q]
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
        double[] baseDiag = myDualBase.getDiagonal();
        for (int iLogic = 0; iLogic < myUpdateOffset; iLogic++) {
            if (myUPivotRow[iLogic] >= 0) {
                largest = Math.max(largest, Math.abs(baseDiag[iLogic]));
            }
        }
        for (int iLogic = myUpdateOffset, limit = myUPivotRow.length; iLogic < limit; iLogic++) {
            if (myUPivotRow[iLogic] >= 0) {
                largest = Math.max(largest, Math.abs(myUpdateDiagonal[iLogic - myUpdateOffset]));
            }
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

        R064Store wCol = this.getWorkerColumn(this.getRowDim());
        newColumn.supplyTo(wCol);

        return this.doUpdateColumn(specifiedColumn, wCol.data);
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

        return this.doUpdateColumn(specifiedColumn, wColData);
    }

    private void btranL(final double[] arg) {
        SubstituteBackwards.invoke(arg, myFixedL);
    }

    private void btranU(final double[] arg) {
        double[] baseDiag = myDualBase.getDiagonal();
        for (int iLogic = 0; iLogic < myUpdateOffset; iLogic++) {
            int pivotRow = myUPivotRow[iLogic];
            if (pivotRow < 0) {
                continue;
            }
            arg[pivotRow] = (arg[pivotRow] - myDualBase.getColumn(iLogic).dot(arg)) / baseDiag[iLogic];
        }
        for (int iLogic = myUpdateOffset, limit = myUPivotRow.length; iLogic < limit; iLogic++) {
            int pivotRow = myUPivotRow[iLogic];
            if (pivotRow < 0) {
                continue;
            }
            int k = iLogic - myUpdateOffset;
            arg[pivotRow] = (arg[pivotRow] - myUpdateColumns.get(k).dot(arg)) / myUpdateDiagonal[k];
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
            myDualBase.putLast(i, j, tmpNumer);
        }
        if (j < r) {
            myDualBase.getDiagonal()[j] = tmpDenom;
            myUPivotRow[j] = j;
            myUPivotLookup[j] = j;
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

        myFactorNonzeros = myFixedL.countNonzeros() + myDualBase.countNonzeros();

        double[] baseDiag = myDualBase.getDiagonal();
        for (int i = 0, limit = baseDiag.length; i < limit; i++) {
            double mag = Math.abs(baseDiag[i]);
            if (mag > myMaxPivotMagnitude) {
                myMaxPivotMagnitude = mag;
            }
            if (mag < myMinPivotMagnitude) {
                myMinPivotMagnitude = mag;
            }
        }
        myFactorMaxPivotMagnitude = myMaxPivotMagnitude;
        myFactorMinPivotMagnitude = myMinPivotMagnitude;

        return this.computed(true);
    }

    /**
     * HiGHS-style Forrest-Tomlin update. Voids the leaving column in U, deletes the leaving row from other U
     * columns, and appends the partially-solved entering column as a new logical column. An ep-based R-factor
     * is stored in {@link #myFactors} to compensate.
     * <p>
     * The key insight: the values stored in U are the intermediate FTRAN result after L and R but before U
     * solve (i.e., R^{-1}*L^{-1}*P*a_q). The alpha for the new pivot comes from the fully-solved result.
     */
    private boolean doUpdateColumn(final int specifiedColumn, final double[] columnData) {

        int m = this.getRowDim();

        // For any basis position, the pivot row is fixed across updates: a replacement column always
        // inherits the leaving column's pivot row. So the pivot row is simply the initial logical column
        // for that basis position — the column permutation applied by {@link #factor(R064CSC, int[])}.
        int leavingPivotRow = myColPivot != null ? myColPivot.locationOf(specifiedColumn) : specifiedColumn;
        int pLogic = myUPivotLookup[leavingPivotRow];
        double oldPivot = pLogic < myUpdateOffset ? myDualBase.getDiagonal()[pLogic] : myUpdateDiagonal[pLogic - myUpdateOffset];

        // Step-by-step FTRAN to capture the intermediate value (R^{-1} L^{-1} P a_q) before the U solve.
        myPivot.applyPivotOrder(columnData);
        this.ftranL(columnData);
        InvertibleFactor.ftran(myFactors, columnData);
        double[] intermediate = Arrays.copyOf(columnData, m);

        // Complete the U solve to obtain alpha (the new pivot value before combining with oldPivot).
        // Reject not only an absolute-zero alpha but also one that is small relative to the leaving pivot:
        // proceeding would amplify roundoff through the new diagonal (oldPivot*alpha) and through 1/alpha
        // in subsequent ftranU calls. This restores develop's effective spike-stability check inside the
        // HiGHS-FT append-only structure.
        this.ftranU(columnData);
        double alpha = columnData[leavingPivotRow];
        if (FletcherMatthews.SAFE.isZero(alpha) || UPDATE_GATE.isSmall(oldPivot, alpha)) {
            return false;
        }

        double newDiagMagnitude = Math.abs(oldPivot * alpha);
        myMaxPivotMagnitude = Math.max(myMaxPivotMagnitude, newDiagMagnitude);
        myMinPivotMagnitude = Math.min(myMinPivotMagnitude, newDiagMagnitude);

        // ep_partial = U^{-T} e_p for the R-factor that compensates the column swap.
        double[] epPartial = new double[m];
        epPartial[leavingPivotRow] = ONE;
        this.btranU(epPartial);

        // Void the leaving logical column and reuse its storage for the new one.
        myUPivotRow[pLogic] = -1;
        SparseArray<Double> oldUCol = pLogic < myUpdateOffset ? myDualBase.getColumn(pLogic) : myUpdateColumns.get(pLogic - myUpdateOffset);
        SparseArray<Double> urLeavingRow = myDualBase.getRow(leavingPivotRow);

        // Delete the leaving row from all other U columns (walking its UR representation).
        for (SparseArray.NonzeroView<Double> nz = urLeavingRow.nonzeros(); nz.hasNext();) {
            nz.next();
            int iLogic = (int) nz.index();
            if (iLogic == pLogic) {
                continue;
            }
            SparseArray<Double> col = iLogic < myUpdateOffset ? myDualBase.getColumn(iLogic) : myUpdateColumns.get(iLogic - myUpdateOffset);
            col.remove(leavingPivotRow);
        }
        // Delete the leaving column from UR and clear both the old column and the leaving row.
        for (SparseArray.NonzeroView<Double> nz = oldUCol.nonzeros(); nz.hasNext();) {
            nz.next();
            myDualBase.getRow((int) nz.index()).remove(pLogic);
        }
        oldUCol.reset();
        urLeavingRow.reset();

        // Build the new replacement column from the intermediate values and append it.
        int newLogic = myUPivotRow.length;
        SparseArray<Double> newUCol = oldUCol;
        for (int row = 0; row < m; row++) {
            double val = intermediate[row];
            if (row != leavingPivotRow && !FletcherMatthews.PRECISION.isZero(val)) {
                newUCol.set(row, val);
                myDualBase.getRow(row).set(newLogic, val);
            }
        }
        myUpdateColumns.add(newUCol);

        // Extend pivot arrays and append the new diagonal.
        myUPivotRow = Arrays.copyOf(myUPivotRow, newLogic + 1);
        myUPivotRow[newLogic] = leavingPivotRow;
        myUPivotLookup[leavingPivotRow] = newLogic;

        int diagonalIndex = newLogic - myUpdateOffset;
        if (diagonalIndex >= myUpdateDiagonal.length) {
            myUpdateDiagonal = Arrays.copyOf(myUpdateDiagonal, Math.max(diagonalIndex + 1, myUpdateDiagonal.length * 2));
        }
        myUpdateDiagonal[diagonalIndex] = oldPivot * alpha;

        // Store the R-factor: -epPartial[j] * oldPivot for j != leavingPivotRow. Keep every nonzero entry
        // produced by btranU; R-eta size is bounded by the existing U sparsity pattern, so dropping small
        // entries saves nothing structural but silently loses correction terms that compound over updates.
        RMatrixEta rEta = new RMatrixEta(leavingPivotRow, m);
        for (int j = 0; j < m; j++) {
            double ep = epPartial[j];
            if (j != leavingPivotRow && ep != ZERO) {
                rEta.addEntry(j, -ep * oldPivot);
            }
        }
        myFactors.add(rEta);
        myEtaNonzeros += rEta.countNonzeros();

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
        int totalCols = myUPivotRow.length;
        for (int iLogic = totalCols - 1; iLogic >= myUpdateOffset; iLogic--) {
            int pivotRow = myUPivotRow[iLogic];
            if (pivotRow < 0) {
                continue;
            }
            int k = iLogic - myUpdateOffset;
            double pivotMultiplier = arg[pivotRow] / myUpdateDiagonal[k];
            arg[pivotRow] = pivotMultiplier;
            if (pivotMultiplier != 0.0) {
                myUpdateColumns.get(k).axpy(-pivotMultiplier, arg);
            }
        }
        double[] baseDiag = myDualBase.getDiagonal();
        for (int iLogic = myUpdateOffset - 1; iLogic >= 0; iLogic--) {
            int pivotRow = myUPivotRow[iLogic];
            if (pivotRow < 0) {
                continue;
            }
            double pivotMultiplier = arg[pivotRow] / baseDiag[iLogic];
            arg[pivotRow] = pivotMultiplier;
            if (pivotMultiplier != 0.0) {
                myDualBase.getColumn(iLogic).axpy(-pivotMultiplier, arg);
            }
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
        return new FactorU(myDualBase, myUPivotRow, myUActive, myUpdateColumns, myUpdateDiagonal, myUpdateOffset);
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

        myDualBase = new DualSparse(m, r);
        myUpdateOffset = r;
        myUActive = r;

        if (myUPivotRow == null || myUPivotRow.length != n) {
            myUPivotRow = Structure1D.newIncreasingRange(0, n);
        } else {
            for (int j = 0; j < n; j++) {
                myUPivotRow[j] = j;
            }
        }

        if (myUPivotLookup == null || myUPivotLookup.length != m) {
            myUPivotLookup = Structure1D.newIncreasingRange(0, m);
        } else {
            for (int i = 0; i < m; i++) {
                myUPivotLookup[i] = i;
            }
        }

        myFactors.clear();
        myEtaNonzeros = 0;
        myMaxPivotMagnitude = ZERO;
        myMinPivotMagnitude = Double.MAX_VALUE;
        myFactorMaxPivotMagnitude = ZERO;
        myFactorMinPivotMagnitude = Double.MAX_VALUE;
        myUpdateColumns.clear();
        if (myUpdateDiagonal == null) {
            myUpdateDiagonal = new double[Math.max(r, 16)];
        }

        myFixedL = null;
    }

    void reset(final Structure2D matrix) {
        this.reset(matrix.getRowDim(), matrix.getColDim());
    }

}
