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

import static org.ojalgo.function.constant.PrimitiveMath.NaN;
import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.context.NumberContext;

/**
 * Implements the Fletcher-Matthews form preserving method for LU factorization updates.
 * <p>
 * This algorithm is a variation of the Bartels-Golub-Reid method that preserves the form of the matrices
 * during column updates. Key characteristics:
 * <ul>
 * <li>Preserves the triangular structure of L and U matrices
 * <li>Handles column updates efficiently by tracking the last non-zero row
 * <li>Performs row and column exchanges to maintain numerical stability
 * <li>Updates both L and U matrices to reflect the changes
 * <li>Maintains the relationship L*U = P*A*Q where P and Q are permutation matrices
 * </ul>
 * <p>
 * The algorithm works by:
 * <ol>
 * <li>Applying forward substitution to transform the new column
 * <li>Finding the last non-zero row in the transformed column
 * <li>Performing column exchanges to position the column correctly
 * <li>Applying row exchanges and updates to maintain triangular form
 * <li>Updating both L and U matrices to reflect all changes
 * </ol>
 * <p>
 * This method is particularly effective for maintaining the structure of sparse matrices during updates, as
 * it minimizes fill-in and preserves sparsity patterns.
 */
abstract class FletcherMatthews {

    private static final boolean DEBUG = false;

    static final NumberContext PRECISION = NumberContext.of(12);
    static final NumberContext SAFE = NumberContext.of(4);

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
    static <N extends Comparable<N>> boolean update(final Pivot rowOrder, final PhysicalStore<N> combined, final Pivot colOrder, final int columnIndex,
            final Access1D.Collectable<N, ? super TransformableRegion<N>> column, final PhysicalStore<N> preallocated) {

        int m = combined.getRowDim();
        int n = combined.getColDim();

        // Get the actual column position after any previous updates
        int col = colOrder.locationOf(columnIndex);

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

                    if (SAFE.isZero(offUr1)) {
                        // zero on diagonal
                        return false;
                    }

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

                if (SAFE.isZero(diag)) {
                    // zero on diagonal
                    return false;
                }

                if (!PRECISION.isZero(offUr1)) {

                    // Check if diag is too small relative to offUr1
                    if (SAFE.isSmall(offUr1, diag)) {
                        return false; // Numerically unstable
                    }

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

            if (SAFE.isZero(preallocated.doubleValue(lastRowNonZero))) {
                // zero on diagonal
                return false;
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
     * As {@link #update(Pivot, PhysicalStore, Pivot, int, Access1D.Collectable, PhysicalStore)}, for other
     * number types. The calculations are done with the elements themselves, and the tolerances are compared
     * with their norms. Multipliers are applied to the rows of U from the left, and to the columns of L from
     * the right, so non-commutative types (quaternions) are handled as well.
     *
     * @param scalar The scalar factory of the number type
     */
    static <N extends Scalar<N>> boolean update(final Pivot rowOrder, final PhysicalStore<N> combined, final Pivot colOrder, final int columnIndex,
            final Access1D.Collectable<N, ? super TransformableRegion<N>> column, final PhysicalStore<N> preallocated, final Scalar.Factory<N> scalar) {

        int m = combined.getRowDim();
        int n = combined.getColDim();

        int col = colOrder.locationOf(columnIndex);

        column.supplyTo(preallocated);
        rowOrder.applyPivotOrder(preallocated);
        preallocated.substituteForwards(combined, true, false, false);

        N zero = scalar.zero().get();
        N temp = zero, diag = zero;

        int lastRowNonZero = -1;
        for (int i = m - 1; i >= 0; i--) {
            temp = preallocated.get(i);
            if (!PRECISION.isZero(temp.norm())) {
                lastRowNonZero = i;
                diag = temp;
                break;
            }
        }

        if (lastRowNonZero < col) {

            return false;

        } else if (SAFE.isZero(diag.norm())) {

            return false;

        } else if (lastRowNonZero == col) {

            for (int i = 0; i <= col; i++) {
                combined.set(i, col, preallocated.get(i));
            }

        } else {

            N offLr1, offLc1, offUr1;

            for (int ij = col; ij < lastRowNonZero; ij++) {
                int ijp1 = ij + 1;
                int ijp2 = ij + 2;

                offLr1 = combined.get(ijp1, ij);

                colOrder.change(ij, ijp1);
                for (int i = 0; i <= ijp1; i++) {
                    combined.set(i, ij, combined.get(i, ijp1));
                }

                diag = combined.get(ij, ij);
                offUr1 = combined.get(ijp1, ij);

                if (offUr1.norm() > diag.norm()) {

                    if (SAFE.isZero(offUr1.norm())) {
                        return false;
                    }

                    combined.exchangeRows(ij, ijp1);
                    preallocated.exchangeRows(ij, ijp1);

                    rowOrder.change(ij, ijp1);

                    for (int i = ijp2; i < m; i++) {
                        temp = combined.get(i, ijp1);
                        combined.set(i, ijp1, combined.get(i, ij));
                        combined.set(i, ij, temp);
                    }

                    temp = diag;
                    diag = offUr1;
                    offUr1 = temp;

                    offLc1 = offLr1;
                    offLr1 = zero;

                    if (!PRECISION.isZero(offLc1.norm())) {

                        for (int i = ijp2; i < m; i++) {
                            combined.set(i, ijp1, combined.get(i, ijp1).subtract(combined.get(i, ij).multiply(offLc1)).get());
                        }

                        for (int j = ij; j < n; j++) {
                            combined.set(ij, j, combined.get(ij, j).add(offLc1.multiply(combined.get(ijp1, j))).get());
                        }
                        preallocated.set(ij, preallocated.get(ij).add(offLc1.multiply(preallocated.get(ijp1))).get());
                        diag = diag.add(offLc1.multiply(offUr1)).get();
                    }
                }

                if (SAFE.isZero(diag.norm())) {
                    return false;
                }

                if (!PRECISION.isZero(offUr1.norm())) {

                    if (SAFE.isSmall(offUr1.norm(), diag.norm())) {
                        return false;
                    }

                    temp = offUr1.divide(diag).get();

                    for (int j = ijp2; j < n; j++) {
                        combined.set(ijp1, j, combined.get(ijp1, j).subtract(temp.multiply(combined.get(ij, j))).get());
                    }
                    preallocated.set(ijp1, preallocated.get(ijp1).subtract(temp.multiply(preallocated.get(ij))).get());

                    combined.set(ijp1, ij, offLr1.add(temp).get());
                    for (int i = ijp2; i < m; i++) {
                        combined.set(i, ij, combined.get(i, ij).add(combined.get(i, ijp1).multiply(temp)).get());
                    }

                } else {

                    combined.set(ijp1, ij, zero);
                }
            }

            if (SAFE.isZero(preallocated.get(lastRowNonZero).norm())) {
                return false;
            }

            for (int i = 0; i <= lastRowNonZero; i++) {
                combined.set(i, lastRowNonZero, preallocated.get(i));
            }
        }

        return true;
    }

}
