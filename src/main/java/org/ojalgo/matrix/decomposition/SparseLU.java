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

import java.util.Arrays;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.matrix.store.DiagonalStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064LSC;
import org.ojalgo.matrix.store.R064LSR;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.SparseR064;
import org.ojalgo.matrix.store.SparseR064.ElementNode;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
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

    private static final NumberContext ACCURACY = NumberContext.of(12);
    private static final boolean DEBUG = false;

    /**
     * U diagonal elements
     */
    private double[] myDiagU;
    private R064LSC myL;
    private final Pivot myRowPivot;
    private Pivot myColPivot;
    private R064LSR myU;

    SparseLU() {

        super(R064Store.FACTORY);

        myRowPivot = new Pivot();
        myColPivot = null;
    }

    @Override
    public void btran(final PhysicalStore<Double> arg) {

        int r = this.getMinDim();

        // Apply column pivot order if needed
        if (myColPivot != null && myColPivot.isModified()) {
            myColPivot.applyPivotOrder(arg);
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

        for (int ij = r - 1; ij >= 0; ij--) {

            double sum = ZERO;

            ElementNode colNodeL = myL.getFirstInColumn(ij);
            while (colNodeL != null) {
                sum -= colNodeL.value * arg.doubleValue(colNodeL.index);
                colNodeL = colNodeL.next;
            }

            arg.add(ij, sum);
        }

        myRowPivot.applyReverseOrder(arg);
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
                myRowPivot.change(ij, p);
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
                                    ElementNode newNode = new ElementNode(pivotNode.index, -multiplier * pivotNode.value);
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

        myRowPivot.applyPivotOrder(arg);

        this.ftran(arg, 0);

        // Apply column pivot order if needed
        if (myColPivot != null && myColPivot.isModified()) {
            myColPivot.applyReverseOrder(arg);
        }
    }

    @Override
    public int getColDim() {
        return myU.getColDim();
    }

    @Override
    public Double getDeterminant() {

        double retVal = myRowPivot.signum();

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
        if (myRowPivot.isModified()) {
            int[] pivotOrder = myRowPivot.getOrder();
            for (int i = 0; i < nbEquations; i++) {
                preallocated.set(i, pivotOrder[i], ONE);
            }
        } else {
            preallocated.fillDiagonal(ONE);
        }

        for (int col = 0; col < nbSolutions; col++) {
            this.ftran(preallocated, col);
        }

        // Apply reverse column pivot order if needed
        if (myColPivot != null && myColPivot.isModified()) {
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
        return myRowPivot.getOrder();
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
        return myRowPivot.reverseOrder();
    }

    @Override
    public int getRowDim() {
        return myU.getRowDim();
    }

    @Override
    public MatrixStore<Double> getSolution(final Collectable<Double, ? super PhysicalStore<Double>> rhs, final PhysicalStore<Double> preallocated) {

        rhs.supplyTo(preallocated);

        this.applyPivotOrder(myRowPivot, preallocated);

        int nbSolutions = rhs.getColDim();

        for (int col = 0; col < nbSolutions; col++) {
            this.ftran(preallocated, col);
        }

        // Apply reverse column pivot order if needed
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
        MatrixStore<Double> retVal = myU.triangular(true, false).superimpose(DiagonalStore.wrap(myDiagU));
        if (myColPivot != null) {
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
        return myRowPivot.isModified();
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
            final PhysicalStore<Double> preallocated) {

        int m = myL.getRowDim();
        int n = myU.getColDim();
        int r = myU.getMinDim();

        if (myColPivot == null) {
            myColPivot = new Pivot();
            myColPivot.reset(n);
        }

        newColumn.supplyTo(preallocated);
        myRowPivot.applyPivotOrder(preallocated);

        int lastRowNonZero = -1;
        for (int ij = 0; ij < r; ij++) {
            double varI = -preallocated.doubleValue(ij);
            if (!ACCURACY.isZero(varI)) {
                lastRowNonZero = ij;
                SparseR064.ElementNode colNodeL = myL.getLastInColumn(ij);
                while (colNodeL != null && colNodeL.index > ij) {
                    preallocated.add(colNodeL.index, colNodeL.value * varI);
                    colNodeL = colNodeL.previous;
                }
            }
        }

        int insertPoint = lastRowNonZero > columnIndex ? lastRowNonZero - 1 : columnIndex;

        for (int ij = 0; ij < r; ij++) {
            myL.set(ij, ij, ONE);
            myU.set(ij, ij, myDiagU[ij]);
        }

        myColPivot.cycle(columnIndex, insertPoint);
        myU.removeAndShift(columnIndex, insertPoint);
        for (int i = 0; i <= lastRowNonZero; i++) {
            double tmpVal = preallocated.doubleValue(i);
            if (!ACCURACY.isZero(tmpVal)) {
                myU.set(i, insertPoint, tmpVal);
            }
        }

        if (DEBUG) {
            BasicLogger.debug("Updated column, and shiftet columns to create Hessenberg");
            BasicLogger.debug("P: {}", myRowPivot);
            BasicLogger.debugMatrix("L", myL);
            BasicLogger.debugMatrix("U", myU);
            BasicLogger.debug("Q: {}", myColPivot);
            BasicLogger.debugMatrix("Recreated", myL.multiply(myU).rows(myRowPivot.reverseOrder()).columns(myColPivot.reverseOrder()));
        }

        for (int ij = columnIndex, limit = Math.min(insertPoint, m - 2); ij <= limit; ij++) {

            if (Math.abs(myU.doubleValue(ij, ij)) < Math.abs(myU.doubleValue(ij + 1, ij))) {

                myU.exchangeRows(ij, ij + 1);
                myL.exchangeColumns(ij, ij + 1);
                myL.exchangeRows(ij, ij + 1);
                myRowPivot.change(ij, ij + 1);

                if (DEBUG) {
                    BasicLogger.debug("Row exchange U ij={}", ij);
                    BasicLogger.debug("P: {}", myRowPivot);
                    BasicLogger.debugMatrix("L", myL);
                    BasicLogger.debugMatrix("U", myU);
                    BasicLogger.debug("Q: {}", myColPivot);
                    BasicLogger.debugMatrix("Recreated", myL.multiply(myU).rows(myRowPivot.reverseOrder()).columns(myColPivot.reverseOrder()));
                }

                double offL = myL.doubleValue(ij, ij + 1);

                if (!ACCURACY.isZero(offL)) {

                    SparseR064.ElementNode colNodeL = myL.getFirstInColumn(ij);
                    while (colNodeL != null) {
                        myL.add(colNodeL.index, ij + 1, -offL * colNodeL.value);
                        colNodeL = colNodeL.next;
                    }

                    SparseR064.ElementNode rowNodeR = myU.getFirstInRow(ij + 1);
                    while (rowNodeR != null) {
                        myU.add(ij, rowNodeR.index, offL * rowNodeR.value);
                        rowNodeR = rowNodeR.next;
                    }

                    if (DEBUG) {
                        BasicLogger.debug("zero off-L, ij={}", ij);
                        BasicLogger.debug("P: {}", myRowPivot);
                        BasicLogger.debugMatrix("L", myL);
                        BasicLogger.debugMatrix("U", myU);
                        BasicLogger.debug("Q: {}", myColPivot);
                        BasicLogger.debugMatrix("Recreated", myL.multiply(myU).rows(myRowPivot.reverseOrder()).columns(myColPivot.reverseOrder()));
                    }
                }
            }

            double offU = myU.doubleValue(ij + 1, ij);
            double diag = myU.doubleValue(ij, ij);
            double fact = offU / diag;

            if (!ACCURACY.isZero(offU)) {

                SparseR064.ElementNode rowNodeU = myU.getFirstInRow(ij);
                while (rowNodeU != null) {
                    myU.add(ij + 1, rowNodeU.index, -fact * rowNodeU.value);
                    rowNodeU = rowNodeU.next;
                }

                SparseR064.ElementNode colNodeL = myL.getFirstInColumn(ij + 1);
                while (colNodeL != null) {
                    myL.add(colNodeL.index, ij, fact * colNodeL.value);
                    colNodeL = colNodeL.next;
                }

                if (DEBUG) {
                    BasicLogger.debug("zero off-U, ij={}", ij);
                    BasicLogger.debug("P: {}", myRowPivot);
                    BasicLogger.debugMatrix("L", myL);
                    BasicLogger.debugMatrix("U", myU);
                    BasicLogger.debug("Q: {}", myColPivot);
                    BasicLogger.debugMatrix("Recreated", myL.multiply(myU).rows(myRowPivot.reverseOrder()).columns(myColPivot.reverseOrder()));
                }
            }
        }

        for (int ij = 0; ij < r; ij++) {
            myDiagU[ij] = myU.doubleValue(ij, ij);
            myU.set(ij, ij, ZERO);
            myL.set(ij, ij, ZERO);
        }

        return true;
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

    void reset(final Collectable<Double, ? super TransformableRegion<Double>> matrix) {

        int m = matrix.getRowDim();
        int n = matrix.getColDim();
        int r = Math.min(m, n);

        myRowPivot.reset(m);
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

        matrix.supplyTo(myU);
    }

}
