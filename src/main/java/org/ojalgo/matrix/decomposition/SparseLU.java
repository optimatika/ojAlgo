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
import org.ojalgo.matrix.store.LinkedR064;
import org.ojalgo.matrix.store.LinkedR064.ElementNode;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064LSC;
import org.ojalgo.matrix.store.R064LSR;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.matrix.transformation.InvertibleFactor;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.structure.Access2D.Sliceable;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.context.NumberContext;

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

    static final class Eta implements InvertibleFactor<Double>, Mutate1D {

        private final int myDim;
        private final int myRow;
        private final SparseArray<Double> myElements;

        Eta(final int dim, final int row) {
            super();
            myDim = dim;
            myRow = row;
            myElements = SparseArray.factory(ArrayR064.FACTORY).make(dim);
        }

        @Override
        public void btran(final PhysicalStore<Double> arg) {

            // For eta matrix, backward substitution is just forward substitution in reverse
            // Only process non-zero elements using the sparse array iterator
            double rowValue = arg.doubleValue(myRow);
            for (NonzeroView<Double> nz : myElements.nonzeros()) {
                arg.add(nz.index(), -nz.doubleValue() * rowValue);
            }
        }

        @Override
        public void ftran(final PhysicalStore<Double> arg) {

            // For eta matrix, forward substitution
            // Only process non-zero elements using the sparse array iterator
            double rowValue = arg.doubleValue(myRow);
            for (NonzeroView<Double> nz : myElements.nonzeros()) {
                arg.add(nz.index(), nz.doubleValue() * rowValue);
            }
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

    static final class Permutation implements InvertibleFactor<Double> {

        private final int myDim;
        private final int myFrom;
        private final int myTo;

        Permutation(final int dim, final int from, final int to) {
            super();
            myDim = dim;
            myFrom = from;
            myTo = to;
        }

        @Override
        public void btran(final PhysicalStore<Double> arg) {

            // For cyclic shift permutation, backward substitution
            // Store the value that will be moved to the end
            double tmp = arg.doubleValue(myFrom);

            // Shift all elements between from and to one position up
            if (myFrom < myTo) {
                for (int i = myFrom; i < myTo; i++) {
                    arg.set(i, arg.doubleValue(i + 1));
                }
            } else {
                for (int i = myFrom; i > myTo; i--) {
                    arg.set(i, arg.doubleValue(i - 1));
                }
            }

            // Place the stored value at the to position
            arg.set(myTo, tmp);
        }

        @Override
        public void ftran(final PhysicalStore<Double> arg) {

            // For cyclic shift permutation, forward substitution
            // Store the value that will be moved to the end
            double tmp = arg.doubleValue(myTo);

            // Shift all elements between from and to one position down
            if (myFrom < myTo) {
                for (int i = myTo; i > myFrom; i--) {
                    arg.set(i, arg.doubleValue(i - 1));
                }
            } else {
                for (int i = myTo; i < myFrom; i++) {
                    arg.set(i, arg.doubleValue(i + 1));
                }
            }

            // Place the stored value at the from position
            arg.set(myFrom, tmp);
        }

        @Override
        public int getColDim() {
            return myDim;
        }

        @Override
        public int getRowDim() {
            return myDim;
        }

    }

    private static final boolean DEBUG = true;
    private static final NumberContext PRECISION = NumberContext.of(12);
    private static final NumberContext SAFE = NumberContext.of(4);

    private static Access2D.Sliceable<Double> cast(final Collectable<Double, ? super TransformableRegion<Double>> matrix) {

        if (matrix instanceof Access2D.Sliceable<?>) {
            return (Access2D.Sliceable<Double>) matrix;
        } else {
            return matrix.collect(R064LSC.FACTORY);
        }
    }

    private Pivot myColPivot;
    /**
     * U diagonal elements
     */
    private double[] myDiagU;
    private R064LSC myL;
    private final Pivot myPivot;
    private R064LSR myU;
    private final List<InvertibleFactor<Double>> myFactors = new ArrayList<>();

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

            ElementNode rowNodeU = myU.getLastInRow(ij);
            while (rowNodeU != null) {
                arg.add(rowNodeU.index, -rowNodeU.value * varJ);
                rowNodeU = rowNodeU.previous;
            }
        }

        for (int i = myFactors.size() - 1; i >= 0; i++) {
            myFactors.get(i).btran(arg);
        }

        for (int ij = r - 1; ij >= 0; ij--) {

            double sum = ZERO;

            ElementNode colNodeL = myL.getFirstInColumn(ij);
            while (colNodeL != null) {
                sum -= colNodeL.value * arg.doubleValue(colNodeL.index);
                colNodeL = colNodeL.next;
            }

            arg.add(ij, sum);
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
        int r = Math.min(m, n);

        R064Store tmpRow = R064Store.FACTORY.make(1, n);
        double[] wRow = tmpRow.data;
        R064Store tmpCol = R064Store.FACTORY.make(m, 1);
        double[] wCol = tmpCol.data;

        for (int j = 0; j < n; j++) {

            columns.sliceColumn(j).supplyTo(wCol);

            // Apply previous transformations.
            myPivot.applyPivotOrder(tmpCol);

            for (int i = 0; i < m; i++) {

                // Most of the time is spent in the following dot product.
                int kmax = Math.min(i, j);
                double sum = ZERO;
                for (int k = 0; k < kmax; k++) {
                    sum += myL.doubleValue(i, k) * wCol[k];
                }
                wCol[i] -= sum;
            }

            // Find pivot and exchange if necessary.

            int p = j;
            for (int i = j + 1; i < m; i++) {
                if (Math.abs(wCol[i]) > Math.abs(wCol[p])) {
                    p = i;
                }
            }
            if (p != j) {

                myPivot.change(p, j);

                double tmpVal = wCol[p];
                wCol[p] = wCol[j];
                wCol[j] = tmpVal;

                myL.exchangeRows(p, j);
            }

            // Copy values to U
            for (int i = 0; i < Math.min(m, j); i++) {
                myU.set(i, j, wCol[i]);
            }
            if (j < r) {
                myDiagU[j] = wCol[j];
            }

            // Compute multipliers.

            if (j < m & wCol[j] != 0.0) {
                for (int i = j + 1; i < m; i++) {
                    myL.set(i, j, wCol[i] / wCol[j]);
                }
            }
        }

        return this.computed(true);
    }

    public boolean decompose2(final Collectable<Double, ? super TransformableRegion<Double>> matrix) {

        this.accept(matrix);

        int m = this.getRowDim();
        int n = this.getColDim();
        int r = this.getMinDim();

        for (int ij = 0; ij < r; ij++) {

            int p = ij;
            ElementNode nodeP = myU.getNode(p, ij);
            double valP = nodeP.doubleValue();
            double magP = Math.abs(valP);

            for (int i = ij + 1; i < m; i++) {
                ElementNode nodeI = myU.getNodeIfExists(i, ij);
                if (nodeI != null) {
                    double valI = nodeI.doubleValue();
                    double magI = Math.abs(valI);
                    if (magI > magP) {
                        p = i;
                        nodeP = nodeI;
                        valP = valI;
                        magP = magI;
                    }
                }
            }

            if (p != ij) {
                myPivot.change(ij, p);
                myL.exchangeRows(ij, p, ij);
                myU.exchangeRows(ij, p);
            }

            if (NumberContext.compare(valP, ZERO) != 0) {

                for (int i = ij + 1; i < m; i++) {

                    ElementNode nodeI = myU.getNodeIfExists(i, ij);

                    if (nodeI != null) {

                        double multiplier = nodeI.doubleValue() / valP;

                        // Get first nodes for both pivot row and target row
                        ElementNode pivotNode = nodeP;
                        ElementNode targetNode = nodeI;
                        ElementNode lastTargetNode = null;

                        // Traverse both rows in parallel
                        while (pivotNode != null) {
                            if (targetNode == null || targetNode.index > pivotNode.index) {
                                // Insert new node in target row
                                if (lastTargetNode == null) {
                                    // Insert at start of row
                                    ElementNode newNode = LinkedR064.newNode(pivotNode.index, -multiplier * pivotNode.value);
                                    newNode.next = targetNode;
                                    if (targetNode != null) {
                                        targetNode.previous = newNode;
                                    } else {
                                        myU.getLastInRow(i).next = newNode;
                                    }
                                    myU.getFirstInRow(i).previous = newNode;
                                    lastTargetNode = newNode;
                                } else {
                                    // Insert after lastTargetNode
                                    lastTargetNode = myU.insertNodeAfter(lastTargetNode, pivotNode.index, -multiplier * pivotNode.value);
                                }
                                pivotNode = pivotNode.next;
                            } else if (targetNode.index < pivotNode.index) {
                                // Skip to next target node
                                lastTargetNode = targetNode;
                                targetNode = targetNode.next;
                            } else {
                                // Update existing node value directly
                                targetNode.value -= multiplier * pivotNode.value;
                                lastTargetNode = targetNode;
                                pivotNode = pivotNode.next;
                                targetNode = targetNode.next;
                            }
                        }

                        myL.set(i, ij, multiplier);
                    }
                }
            }

            myDiagU[ij] = valP;
            myU.remove(ij, nodeP);
        }

        return this.computed(true);
    }

    @Override
    public void ftran(final PhysicalStore<Double> arg) {

        this.applyPivotOrder(myPivot, arg);

        this.ftran(arg, 0);

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

        int nbEquations = this.getRowDim();
        int nbSolutions = this.getColDim();

        preallocated.fillAll(ZERO);
        if (myPivot.isModified()) {
            int[] pivotOrder = myPivot.getOrder();
            for (int i = 0; i < nbEquations; i++) {
                preallocated.set(i, pivotOrder[i], ONE);
            }
        } else {
            preallocated.fillDiagonal(ONE);
        }

        for (int col = 0; col < nbSolutions; col++) {
            this.ftran(preallocated, col);
        }

        if (myColPivot != null) {
            this.applyReverseOrder(myColPivot, preallocated);
        }

        return preallocated;
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
        return myU.getRowDim();
    }

    @Override
    public MatrixStore<Double> getSolution(final Collectable<Double, ? super PhysicalStore<Double>> rhs, final PhysicalStore<Double> preallocated) {

        rhs.supplyTo(preallocated);

        this.applyPivotOrder(myPivot, preallocated);

        int nbSolutions = rhs.getColDim();

        for (int col = 0; col < nbSolutions; col++) {
            this.ftran(preallocated, col);
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
    public boolean updateColumn(final int columnIndex, final Access1D.Collectable<Double, ? super TransformableRegion<Double>> newColumn,
            final PhysicalStore<Double> preallocated2) {

        int m = this.getRowDim();
        int n = this.getColDim();
        int r = Math.min(m, n);

        R064Store tmpRow = R064Store.FACTORY.make(1, n);
        double[] wRow = tmpRow.data;
        R064Store tmpCol = R064Store.FACTORY.make(m, 1);
        double[] wCol = tmpCol.data;

        if (myColPivot == null) {
            myColPivot = new Pivot();
            myColPivot.reset(n);
        }

        if (DEBUG) {
            MatrixStore<Double> mtrxL = this.getL();
            MatrixStore<Double> mtrxU = myU.superimpose(DiagonalStore.wrap(myDiagU));
            BasicLogger.debug("Initial");
            BasicLogger.debug("P: {}", myPivot);
            BasicLogger.debugMatrix("L", mtrxL);
            BasicLogger.debugMatrix("U", mtrxU);
            BasicLogger.debug("Q: {}", myColPivot);
            BasicLogger.debugMatrix("Recreated", mtrxL.multiply(mtrxU).rows(myPivot.reverseOrder()).columns(myColPivot.reverseOrder()));
        }

        // newColumn.supplyTo(tmpCol);

        newColumn.supplyTo(tmpCol);
        myPivot.applyPivotOrder(tmpCol);

        int lastRowNonZero = -1;
        for (int ij = 0; ij < r; ij++) {
            double varI = -tmpCol.doubleValue(ij);
            if (!PRECISION.isZero(varI)) {
                lastRowNonZero = ij;
                LinkedR064.ElementNode colNodeL = myL.getLastInColumn(ij);
                while (colNodeL != null && colNodeL.index > ij) {
                    tmpCol.add(colNodeL.index, colNodeL.value * varI);
                    colNodeL = colNodeL.previous;
                }
            }
        }

        myColPivot.cycle(columnIndex, lastRowNonZero);

        myU.doCyclicFT(columnIndex, tmpRow, lastRowNonZero, tmpCol);
        for (int i = columnIndex; i < lastRowNonZero; i++) {
            myDiagU[i] = myDiagU[i + 1];
        }
        wRow[lastRowNonZero] = tmpCol.doubleValue(columnIndex);

        myFactors.add(new Permutation(r, columnIndex, lastRowNonZero));

        if (DEBUG) {
            MatrixStore<Double> mtrxL = this.getL();
            MatrixStore<Double> mtrxU = myU.superimpose(DiagonalStore.wrap(myDiagU));
            BasicLogger.debug("Transformed shift & replace");
            BasicLogger.debug("P: {}", myPivot);
            BasicLogger.debugMatrix("L", mtrxL);
            BasicLogger.debugMatrix("U", mtrxU);
            BasicLogger.debug("Q: {}", myColPivot);
        }

        Eta eta = new Eta(r, lastRowNonZero);

        for (int ij = columnIndex; ij < lastRowNonZero; ij++) {
            double denom = myDiagU[ij];
            double numer = wRow[ij];
            double ratio = numer / denom;

            eta.set(ij, ratio);

            wRow[ij] = ZERO;

            ElementNode rowNodeU = myU.getFirstInRow(ij);
            while (rowNodeU != null) {
                wRow[rowNodeU.index] -= ratio * rowNodeU.value;
                rowNodeU = rowNodeU.next;
            }
            myU.set(lastRowNonZero, ij, ZERO);
        }

        myDiagU[lastRowNonZero] = wRow[lastRowNonZero];
        for (int j = lastRowNonZero + 1; j < n; j++) {
            myU.set(lastRowNonZero, j, wRow[j]);
        }

        myFactors.add(eta);

        if (DEBUG) {
            MatrixStore<Double> mtrxL = this.getL();
            MatrixStore<Double> mtrxU = myU.superimpose(DiagonalStore.wrap(myDiagU));
            BasicLogger.debug("Final");
            BasicLogger.debug("P: {}", myPivot);
            BasicLogger.debugMatrix("L", mtrxL);
            BasicLogger.debugMatrix("U", mtrxU);
            BasicLogger.debug("Q: {}", myColPivot);
        }

        return false;
    }

    private void ftran(final PhysicalStore<Double> arg, final int col) {

        int r = this.getMinDim();

        for (int ij = 0; ij < r; ij++) {

            double varJ = -arg.doubleValue(ij, col);

            ElementNode colNodeL = myL.getFirstInColumn(ij);
            while (colNodeL != null) {
                arg.add(colNodeL.index, col, colNodeL.value * varJ);
                colNodeL = colNodeL.next;
            }
        }

        for (int i = 0; i < myFactors.size(); i++) {
            myFactors.get(i).ftran(arg);
        }

        for (int ij = r - 1; ij >= 0; ij--) {

            double sum = arg.doubleValue(ij, col);

            ElementNode rowNodeU = myU.getFirstInRow(ij);
            while (rowNodeU != null) {
                sum -= rowNodeU.value * arg.doubleValue(rowNodeU.index, col);
                rowNodeU = rowNodeU.next;
            }

            arg.set(ij, col, sum / myDiagU[ij]);
        }
    }

    @Override
    protected boolean checkSolvability() {
        return this.isSquare() && this.isFullRank();
    }

    void accept(final Collectable<Double, ? super TransformableRegion<Double>> matrix) {

        this.reset(matrix);

        matrix.supplyTo(myU);
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
            myL = R064LSC.FACTORY.make(m, r);
        }

        if (myDiagU != null && myDiagU.length == r) {
            Arrays.fill(myDiagU, ZERO);
        } else {
            myDiagU = new double[r];
        }

        if (myU == null || myU.getRowDim() != m || myU.getColDim() != n) {
            myU = R064LSR.FACTORY.make(m, n);
        }
    }

}
