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
import org.ojalgo.matrix.operation.SubstituteBackwards;
import org.ojalgo.matrix.operation.SubstituteForwards;
import org.ojalgo.matrix.store.DiagonalStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064CSR;
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
 */
final class SparseLU extends AbstractDecomposition<Double, R064Store> implements LU<Double> {

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
        private final Pivot myColPivot;

        FactorU(final RowsSupplier<Double> u, final double[] diagU, final Pivot colPivot) {
            super();
            myBodyMain = u;
            myBodyDiagonal = diagU;
            myColPivot = colPivot;
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
            if (myColPivot != null && myColPivot.isModified()) {
                retVal = retVal.columns(myColPivot.reverseOrder());
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
    private final List<InvertibleFactor<Double>> myFactors = new ArrayList<>();
    private R064CSR myFixedL;
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
    public void btran(final double[] arg) {

        int r = this.getMinDim();

        if (myColPivot != null) {
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

            this.ftranL(wCol);

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

        myFixedL = myL.toCSR();

        return this.computed(true);
    }

    @Override
    public void ftran(final double[] arg) {

        myPivot.applyPivotOrder(arg);

        this.ftranL(arg);

        InvertibleFactor.ftran(myFactors, arg);

        this.ftranU(arg);

        if (myColPivot != null) {
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

        if (myColPivot != null) {
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

            for (int col = 0; col < nbSolutions; col++) {

                column.fillMatching(preallocated.sliceColumn(col));

                this.ftranL(column);

                InvertibleFactor.ftran(myFactors, column);

                this.ftranU(column);

                preallocated.fillColumn(col, column);
            }

        } else {

            this.ftranL(preallocated);

            InvertibleFactor.ftran(myFactors, preallocated);

            this.ftranU(preallocated);
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
        return this.getFactorU().get();
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
        myPivot.applyPivotOrder(wColData);

        this.ftranL(wColData);

        // Apply any existing transformations to the new column
        InvertibleFactor.ftran(myFactors, wColData);

        // After forward substitution is complete, find the last non-zero row
        double diag = NaN;
        int lastRowNonZero = -1;
        for (int i = m - 1; i >= 0; i--) {
            if (!FletcherMatthews.PRECISION.isZero(wColData[i])) {
                lastRowNonZero = i;
                diag = wColData[i];
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
                myU.set(i, columnIndex, wColData[i]);
            }
            myDiagU[columnIndex] = wColData[columnIndex];

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

            if (FletcherMatthews.SAFE.isZero(wRowData[lastRowNonZero])) {
                // zero on diagonal
                return false;
            }

            myDiagU[lastRowNonZero] = wRowData[lastRowNonZero];
            for (int j = lastRowNonZero + 1; j < n; j++) {
                myU.set(lastRowNonZero, j, wRowData[j]);
            }

            myFactors.add(eta);
        }

        return true;
    }

    private void btranL(final double[] arg) {
        SubstituteBackwards.invoke(arg, myFixedL);
    }

    private void btranL(final PhysicalStore<Double> arg) {
        int r = myL.getMinDim();
        for (int ij = r - 1; ij > 0; ij--) {
            double varJ = arg.doubleValue(ij);
            myL.getRow(ij).axpy(-varJ, arg);
        }
    }

    private void btranU(final double[] arg) {
        int r = myDiagU.length;
        for (int ij = 0; ij < r; ij++) {
            double varJ = arg[ij] / myDiagU[ij];
            arg[ij] = varJ;
            myU.getRow(ij).axpy(-varJ, arg);
        }
    }

    private void btranU(final PhysicalStore<Double> arg) {
        int r = myDiagU.length;
        for (int ij = 0; ij < r; ij++) {
            double varJ = arg.doubleValue(ij) / myDiagU[ij];
            arg.set(ij, varJ);
            myU.getRow(ij).axpy(-varJ, arg);
        }
    }

    private void ftranL(final double[] arg) {
        SubstituteForwards.invoke(arg, myFixedL);
    }

    private void ftranL(final PhysicalStore<Double> arg) {
        int r = myL.getMinDim();
        for (int ij = 1; ij < r; ij++) {
            arg.add(ij, 0, -myL.getRow(ij).dot(arg));
        }
    }

    private void ftranU(final double[] arg) {
        for (int ij = myDiagU.length - 1; ij >= 0; ij--) {
            arg[ij] = (arg[ij] - myU.getRow(ij).dot(arg)) / myDiagU[ij];
        }
    }

    private void ftranU(final PhysicalStore<Double> arg) {
        for (int ij = myDiagU.length - 1; ij >= 0; ij--) {
            double sum = arg.doubleValue(ij);
            sum -= myU.getRow(ij).dot(arg);
            arg.set(ij, 0, sum / myDiagU[ij]);
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

        if (myColPivot != null) {
            return Optional.of(new FactorPivot<>(this.makeIdentity(this.getColDim()), myColPivot, false));
        } else {
            return Optional.empty();
        }
    }

    /**
     * [A]=[P][L][U][Q]
     */
    MatrixDecomposition.Factor<Double> getFactorU() {
        return new FactorU(myU, myDiagU, myColPivot);
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

        myFixedL = null;
    }

}
