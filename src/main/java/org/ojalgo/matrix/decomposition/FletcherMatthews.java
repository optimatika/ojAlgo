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

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064LSC;
import org.ojalgo.matrix.store.R064LSR;
import org.ojalgo.matrix.store.SparseR064;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.context.NumberContext;

/**
 * Implements the Fletcher-Matthews form preserving method for LU factorization updates.
 * <p>
 * This algorithm is a variation of the Bartels-Golub-Reid method that preserves the form of the matrices
 * during column updates. Key characteristics:
 * <ul>
 * <li>Preserves the triangular structure of L and U matrices</li>
 * <li>Handles column updates efficiently by tracking the last non-zero row</li>
 * <li>Performs row and column exchanges to maintain numerical stability</li>
 * <li>Updates both L and U matrices to reflect the changes</li>
 * <li>Maintains the relationship L*U = P*A*Q where P and Q are permutation matrices</li>
 * </ul>
 * <p>
 * The algorithm works by:
 * <ol>
 * <li>Applying forward substitution to transform the new column</li>
 * <li>Finding the last non-zero row in the transformed column</li>
 * <li>Performing column exchanges to position the column correctly</li>
 * <li>Applying row exchanges and updates to maintain triangular form</li>
 * <li>Updating both L and U matrices to reflect all changes</li>
 * </ol>
 * <p>
 * This method is particularly effective for maintaining the structure of sparse matrices during updates, as
 * it minimizes fill-in and preserves sparsity patterns.
 */
abstract class FletcherMatthews {

    private static final boolean DEBUG = false;
    private static final NumberContext PRECISION = NumberContext.of(12);
    private static final NumberContext SAFE = NumberContext.of(4);

    /**
     * Updates the LU decomposition when a column is modified in the original matrix. This version is used
     * when L and U are stored in a combined format.
     *
     * @param rowOrder     Current row permutation vector
     * @param combined     Matrix storing both L and U factors in a combined format
     * @param colOrder     Current column permutation vector
     * @param col          Index of the column being updated
     * @param column       New column values
     * @param preallocated Preallocated workspace for calculations
     * @return true if the update was successful, false if the matrix became singular or numerically unstable
     */
    static <N extends Comparable<N>> boolean update(final Pivot rowOrder, final PhysicalStore<N> combined, final Pivot colOrder, final int col,
            final Access1D.Collectable<N, ? super TransformableRegion<N>> column, final PhysicalStore<N> preallocated) {

        int m = combined.getRowDim();
        int n = combined.getColDim();

        column.supplyTo(preallocated);
        rowOrder.applyPivotOrder(preallocated);
        preallocated.substituteForwards(combined, true, false, false);

        double temp = NaN, diag = NaN;

        int lastRowNonZero = -1;
        for (int i = m - 1; i >= 0; i--) {
            temp = preallocated.doubleValue(i);
            if (!PRECISION.isZero(temp)) {
                lastRowNonZero = i;
                diag = temp;
                break;
            }
        }

        if (lastRowNonZero < col) {

            // This means the updated matrix is singular
            return false;

        } else if (SAFE.isZero(diag)) {

            // Numerically unstable
            return false;

        } else if (lastRowNonZero == col) {

            // Lucky!
            for (int i = 0; i <= col; i++) {
                combined.set(i, col, preallocated.doubleValue(i));
            }

        } else {

            double offLr1, offLc1, offUr1;

            for (int ij = col; ij < lastRowNonZero; ij++) {
                int ijp1 = ij + 1;
                int ijp2 = ij + 2;

                offLr1 = combined.doubleValue(ijp1, ij);

                colOrder.change(ij, ijp1);
                for (int i = 0; i <= ijp1; i++) {
                    combined.set(i, ij, combined.doubleValue(i, ijp1));
                }

                diag = combined.doubleValue(ij, ij);
                offUr1 = combined.doubleValue(ijp1, ij);

                if (Math.abs(offUr1) > Math.abs(diag)) {

                    combined.exchangeRows(ij, ijp1);
                    preallocated.exchangeRows(ij, ijp1);

                    rowOrder.change(ij, ijp1);

                    for (int i = ijp2; i < m; i++) {
                        temp = combined.doubleValue(i, ijp1);
                        combined.set(i, ijp1, combined.doubleValue(i, ij));
                        combined.set(i, ij, temp);
                    }

                    temp = diag;
                    diag = offUr1;
                    offUr1 = temp;

                    offLc1 = offLr1;
                    offLr1 = ZERO;

                    if (!PRECISION.isZero(offLc1)) {

                        for (int i = ijp2; i < m; i++) { // L (ij+1)
                            combined.add(i, ijp1, -offLc1 * combined.doubleValue(i, ij));
                        }

                        for (int j = ij; j < n; j++) { // U (ij)
                            combined.add(ij, j, offLc1 * combined.doubleValue(ijp1, j));
                        }
                        preallocated.add(ij, offLc1 * preallocated.doubleValue(ijp1));
                        diag += offLc1 * offUr1;

                        offLc1 = ZERO;
                    }
                }

                if (!PRECISION.isZero(offUr1)) {

                    temp = offUr1 / diag;

                    combined.set(ijp1, ij, offUr1 = ZERO);
                    for (int j = ijp2; j < n; j++) { // U (ij+1)
                        combined.add(ijp1, j, -temp * combined.doubleValue(ij, j));
                    }
                    preallocated.add(ijp1, -temp * preallocated.doubleValue(ij));

                    combined.set(ijp1, ij, offLr1 + temp); // L (ij)
                    for (int i = ijp2; i < m; i++) {
                        combined.add(i, ij, temp * combined.doubleValue(i, ijp1));
                    }

                } else {

                    combined.set(ijp1, ij, offUr1 = ZERO);
                }

                if (DEBUG) {
                    BasicLogger.debug("Iteration " + ij);
                    BasicLogger.debug("P&Q: {} – {}", rowOrder, colOrder);
                    BasicLogger.debugMatrix("Combined", combined);
                    MatrixStore<N> tmpL = combined.triangular(false, true);
                    MatrixStore<N> tmpU = combined.triangular(true, false);
                    BasicLogger.debugMatrix("L", tmpL);
                    BasicLogger.debugMatrix("U", tmpU);
                }
            }

            for (int i = 0; i <= lastRowNonZero; i++) {
                combined.set(i, lastRowNonZero, preallocated.doubleValue(i));
            }

            if (DEBUG) {
                BasicLogger.debug("Final");
                BasicLogger.debug("P&Q: {} – {}", rowOrder, colOrder);
                BasicLogger.debugMatrix("Combined", combined);
                MatrixStore<N> tmpL = combined.triangular(false, true);
                MatrixStore<N> tmpU = combined.triangular(true, false);
                BasicLogger.debugMatrix("L", tmpL);
                BasicLogger.debugMatrix("U", tmpU);
                BasicLogger.debugMatrix("LU", tmpL.multiply(tmpU));
            }
        }

        return true;
    }

    /**
     * Updates the LU decomposition when a column is modified in the original matrix. This version is used
     * when L and U are stored separately, with U's diagonal elements stored in a separate array.
     *
     * @param rowOrder     Current row permutation vector
     * @param mtrxL        Lower triangular matrix L
     * @param myDiagU      Diagonal elements of upper triangular matrix U
     * @param mtrxU        Upper triangular matrix U (without diagonal)
     * @param colOrder     Current column permutation vector
     * @param col          Index of the column being updated
     * @param column       New column values
     * @param preallocated Preallocated workspace for calculations
     * @return true if the update was successful, false if the matrix became singular or numerically unstable
     */
    static boolean update(final Pivot rowOrder, final R064LSC mtrxL, final double[] myDiagU, final R064LSR mtrxU, final Pivot colOrder, final int col,
            final Access1D.Collectable<Double, ? super TransformableRegion<Double>> column, final PhysicalStore<Double> preallocated) {

        int m = mtrxL.getRowDim();
        int n = mtrxU.getColDim();
        int r = mtrxU.getMinDim();

        column.supplyTo(preallocated);
        rowOrder.applyPivotOrder(preallocated);

        int lastRowNonZero = -1;
        for (int ij = 0; ij < r; ij++) {
            double varI = -preallocated.doubleValue(ij);
            if (!PRECISION.isZero(varI)) {
                lastRowNonZero = ij;
                SparseR064.ElementNode colNodeL = mtrxL.getLastInColumn(ij);
                while (colNodeL != null && colNodeL.index > ij) {
                    preallocated.add(colNodeL.index, colNodeL.value * varI);
                    colNodeL = colNodeL.previous;
                }
            }
        }

        int insertPoint = lastRowNonZero > col ? lastRowNonZero - 1 : col;

        for (int ij = 0; ij < r; ij++) {
            mtrxL.set(ij, ij, ONE);
            mtrxU.set(ij, ij, myDiagU[ij]);
        }

        colOrder.cycle(col, insertPoint);
        mtrxU.removeAndShift(col, insertPoint);
        for (int i = 0; i <= lastRowNonZero; i++) {
            double tmpVal = preallocated.doubleValue(i);
            if (!PRECISION.isZero(tmpVal)) {
                mtrxU.set(i, insertPoint, tmpVal);
            }
        }

        if (DEBUG) {
            BasicLogger.debug("Updated column, and shiftet columns to create Hessenberg");
            BasicLogger.debug("P: {}", rowOrder);
            BasicLogger.debugMatrix("L", mtrxL);
            BasicLogger.debugMatrix("U", mtrxU);
            BasicLogger.debug("Q: {}", colOrder);
            BasicLogger.debugMatrix("Recreated", mtrxL.multiply(mtrxU).rows(rowOrder.reverseOrder()).columns(colOrder.reverseOrder()));
        }

        for (int ij = col, limit = Math.min(insertPoint, m - 2); ij <= limit; ij++) {

            if (Math.abs(mtrxU.doubleValue(ij, ij)) < Math.abs(mtrxU.doubleValue(ij + 1, ij))) {

                mtrxU.exchangeRows(ij, ij + 1);
                mtrxL.exchangeColumns(ij, ij + 1);
                mtrxL.exchangeRows(ij, ij + 1);
                rowOrder.change(ij, ij + 1);

                if (DEBUG) {
                    BasicLogger.debug("Row exchange U ij={}", ij);
                    BasicLogger.debug("P: {}", rowOrder);
                    BasicLogger.debugMatrix("L", mtrxL);
                    BasicLogger.debugMatrix("U", mtrxU);
                    BasicLogger.debug("Q: {}", colOrder);
                    BasicLogger.debugMatrix("Recreated", mtrxL.multiply(mtrxU).rows(rowOrder.reverseOrder()).columns(colOrder.reverseOrder()));
                }

                double offL = mtrxL.doubleValue(ij, ij + 1);

                if (!PRECISION.isZero(offL)) {

                    SparseR064.ElementNode colNodeL = mtrxL.getFirstInColumn(ij);
                    while (colNodeL != null) {
                        mtrxL.add(colNodeL.index, ij + 1, -offL * colNodeL.value);
                        colNodeL = colNodeL.next;
                    }

                    SparseR064.ElementNode rowNodeR = mtrxU.getFirstInRow(ij + 1);
                    while (rowNodeR != null) {
                        mtrxU.add(ij, rowNodeR.index, offL * rowNodeR.value);
                        rowNodeR = rowNodeR.next;
                    }

                    if (DEBUG) {
                        BasicLogger.debug("zero off-L, ij={}", ij);
                        BasicLogger.debug("P: {}", rowOrder);
                        BasicLogger.debugMatrix("L", mtrxL);
                        BasicLogger.debugMatrix("U", mtrxU);
                        BasicLogger.debug("Q: {}", colOrder);
                        BasicLogger.debugMatrix("Recreated", mtrxL.multiply(mtrxU).rows(rowOrder.reverseOrder()).columns(colOrder.reverseOrder()));
                    }
                }
            }

            double offU = mtrxU.doubleValue(ij + 1, ij);
            double diag = mtrxU.doubleValue(ij, ij);
            double fact = offU / diag;

            if (!PRECISION.isZero(offU)) {

                SparseR064.ElementNode rowNodeU = mtrxU.getFirstInRow(ij);
                while (rowNodeU != null) {
                    mtrxU.add(ij + 1, rowNodeU.index, -fact * rowNodeU.value);
                    rowNodeU = rowNodeU.next;
                }

                SparseR064.ElementNode colNodeL = mtrxL.getFirstInColumn(ij + 1);
                while (colNodeL != null) {
                    mtrxL.add(colNodeL.index, ij, fact * colNodeL.value);
                    colNodeL = colNodeL.next;
                }

                if (DEBUG) {
                    BasicLogger.debug("zero off-U, ij={}", ij);
                    BasicLogger.debug("P: {}", rowOrder);
                    BasicLogger.debugMatrix("L", mtrxL);
                    BasicLogger.debugMatrix("U", mtrxU);
                    BasicLogger.debug("Q: {}", colOrder);
                    BasicLogger.debugMatrix("Recreated", mtrxL.multiply(mtrxU).rows(rowOrder.reverseOrder()).columns(colOrder.reverseOrder()));
                }
            }
        }

        for (int ij = 0; ij < r; ij++) {
            myDiagU[ij] = mtrxU.doubleValue(ij, ij);
            mtrxU.set(ij, ij, ZERO);
            mtrxL.set(ij, ij, ZERO);
        }

        return true;
    }

}
