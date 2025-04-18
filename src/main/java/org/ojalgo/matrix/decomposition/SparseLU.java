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
import org.ojalgo.matrix.store.SparseR064.ElementNode;
import org.ojalgo.matrix.store.TransformableRegion;
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

    private Pivot myColPivot;
    /**
     * U diagonal elements
     */
    private double[] myDiagU;
    private R064LSC myL;
    private final Pivot myPivot;
    private R064LSR myU;

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
            final PhysicalStore<Double> preallocated) {

        if (myColPivot == null) {
            myColPivot = new Pivot();
            myColPivot.reset(this.getColDim());
        }

        return FletcherMatthews.update(myPivot, myL, myDiagU, myU, myColPivot, columnIndex, newColumn, preallocated);
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

        matrix.supplyTo(myU);
    }

}
