package org.ojalgo.matrix.decomposition;

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.DiagonalStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064LSC;
import org.ojalgo.matrix.store.R064LSR;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Uniform;
import org.ojalgo.type.context.NumberContext;

/**
 * Tests for the updateColumn method in RawLU class.
 */
public class DecompositionUpdateTest extends MatrixDecompositionTests {

    @FunctionalInterface
    interface Calculator {

        Result calculate(int[] rowOrder, final PhysicalStore<Double> mtrxL, final PhysicalStore<Double> mtrxU, final int col, final MatrixStore<Double> column);

    }

    static class Result {

        final int[] colOrder;
        final MatrixStore<Double> mtrxL;
        final MatrixStore<Double> mtrxU;
        final int[] rowOrder;

        Result(final int[] first, final MatrixStore<Double> second, final MatrixStore<Double> third) {
            this(first, second, third, null);
        }

        Result(final int[] first, final MatrixStore<Double> second, final MatrixStore<Double> third, final int[] forth) {
            rowOrder = first;
            mtrxL = second;
            mtrxU = third;
            colOrder = forth;
        }

    }

    private static final NumberContext ACCURACY = NumberContext.of(12);
    private static final Pivot COL_ORDER = new Pivot();
    private static final NumberContext PRINT = NumberContext.of(6);
    private static final Uniform RANDOMIZER = new Uniform(-10, 20);

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
     * This method is particularly effective for maintaining the structure of sparse matrices during updates,
     * as it minimizes fill-in and preserves sparsity patterns.
     *
     * @param pivotOrder  Current row permutation vector
     * @param mtrxL       Current L matrix
     * @param mtrxU       Current U matrix
     * @param columnIndex Index of column being updated
     * @param column      New column values
     * @return Tuple containing updated L, U matrices and permutation vectors
     */
    private static Result doFletcherMatthews(final int[] pivotOrder, final PhysicalStore<Double> mtrxL, final PhysicalStore<Double> mtrxU,
            final int columnIndex, final MatrixStore<Double> column) {

        int m = mtrxL.getRowDim();
        int n = mtrxU.getColDim();

        Pivot rowOrder = new Pivot(pivotOrder);
        Pivot colOrder = new Pivot();
        colOrder.reset(n);

        PhysicalStore<Double> preallocated = R064Store.FACTORY.make(m, 1);
        column.supplyTo(preallocated);
        rowOrder.applyPivotOrder(preallocated);
        preallocated.substituteForwards(mtrxL, true, false, false);

        // After forward substitution is complete, find the last non-zero row
        int lastRowNonZero = -1;
        for (int i = m - 1; i >= 0; i--) {
            if (!ACCURACY.isZero(preallocated.doubleValue(i))) {
                lastRowNonZero = i;
                break; // Stop as soon as we find a non-zero value
            }
        }

        int insertPoint = lastRowNonZero > columnIndex ? lastRowNonZero - 1 : columnIndex;

        mtrxU.fillColumn(columnIndex, preallocated);
        for (int j = columnIndex; j < insertPoint; j++) {
            colOrder.change(j, j + 1);
            mtrxU.exchangeColumns(j, j + 1);
        }

        if (DEBUG) {
            BasicLogger.debug("Updated column, and shiftet columns to create Hessenberg");
            BasicLogger.debug("P: {}", rowOrder);
            BasicLogger.debugMatrix("L", mtrxL);
            BasicLogger.debugMatrix("U", mtrxU);
            BasicLogger.debug("Q: {}", colOrder);
            BasicLogger.debugMatrix("Recreated", mtrxL.multiply(mtrxU).rows(rowOrder.reverseOrder()).columns(colOrder.reverseOrder()));
        }

        for (int ij = columnIndex, limit = Math.min(insertPoint, m - 2); ij <= limit; ij++) {

            if (Math.abs(mtrxU.doubleValue(ij, ij)) < Math.abs(mtrxU.doubleValue(ij + 1, ij))) {

                mtrxU.exchangeRows(ij, ij + 1);
                mtrxL.exchangeColumns(ij, ij + 1);
                mtrxL.exchangeRows(ij, ij + 1);
                rowOrder.change(ij, ij + 1);

                if (DEBUG) {
                    BasicLogger.debug("ij={}", ij);
                    BasicLogger.debugMatrix("L", mtrxL);
                    BasicLogger.debugMatrix("U", mtrxU);
                    BasicLogger.debugMatrix("LU", mtrxL.multiply(mtrxU));
                }

                double offL = mtrxL.doubleValue(ij, ij + 1);

                if (offL != ZERO) {
                    for (int i = ij; i < m; i++) {
                        mtrxL.add(i, ij + 1, -offL * mtrxL.doubleValue(i, ij));
                    }
                    for (int j = ij; j < n; j++) {
                        mtrxU.add(ij, j, offL * mtrxU.doubleValue(ij + 1, j));
                    }
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

            double offU = mtrxU.doubleValue(ij + 1, ij);
            double diag = mtrxU.doubleValue(ij, ij);
            double fact = offU / diag;

            if (offU != ZERO) {
                for (int j = ij; j < n; j++) {
                    mtrxU.add(ij + 1, j, -fact * mtrxU.doubleValue(ij, j));
                }
                for (int i = ij; i < m; i++) {
                    mtrxL.add(i, ij, fact * mtrxL.doubleValue(i, ij + 1));
                }
            }

            if (DEBUG) {
                BasicLogger.debug("ij={}", ij);
                BasicLogger.debugMatrix("L", mtrxL);
                BasicLogger.debugMatrix("U", mtrxU);
                BasicLogger.debugMatrix("LU", mtrxL.multiply(mtrxU));
            }
        }

        return new Result(rowOrder.getOrder(), mtrxL, mtrxU, colOrder.getOrder());
    }

    /**
     * A variant of {@link #doFletcherMatthews(int[], PhysicalStore, PhysicalStore, int, MatrixStore)}
     * specifically target to be used with (copied to) {@link DenseLU}.
     * <p>
     * {@link DenseLU} stores the L and U factors combined in a single {@link R064Store} instance.
     */
    private static Result doFletcherMatthewsDense(final int[] pivotOrder, final PhysicalStore<Double> mtrxL, final PhysicalStore<Double> mtrxU,
            final int columnIndex, final MatrixStore<Double> column) {

        int m = mtrxL.getRowDim();
        int n = mtrxU.getColDim();

        DecompositionStore<Double> combined = R064Store.FACTORY.make(m, n);
        for (int j = 0; j < n; j++) {
            for (int i = j + 1; i < m; i++) {
                combined.set(i, j, mtrxL.doubleValue(i, j));
            }
        }
        for (int j = 0; j < n; j++) {
            for (int i = 0; i <= j; i++) {
                combined.set(i, j, mtrxU.doubleValue(i, j));
            }
        }

        Pivot myPivot = new Pivot(pivotOrder);
        Pivot myColPivot = new Pivot();
        myColPivot.reset(n);

        FletcherMatthews.update(myPivot, combined, myColPivot, columnIndex, column, R064Store.FACTORY.make(m, 1));

        MatrixStore<Double> tmpL = combined.triangular(false, true);
        MatrixStore<Double> tmpU = combined.triangular(true, false);
        return new Result(myPivot.getOrder(), tmpL, tmpU, myColPivot.getOrder());
    }

    /**
     * A variant of {@link #doFletcherMatthews(int[], PhysicalStore, PhysicalStore, int, MatrixStore)}
     * specifically target to be used with (copied to) {@link DenseLU}.
     * <p>
     * {@link DenseLU} stores the L and U factors combined in a single {@link R064Store} instance.
     */
    private static Result doFletcherMatthewsDenseTooSimple(final int[] pivotOrder, final PhysicalStore<Double> mtrxL, final PhysicalStore<Double> mtrxU,
            final int columnIndex, final MatrixStore<Double> column) {

        int m = mtrxL.getRowDim();
        int n = mtrxU.getColDim();
        int r = mtrxL.getMinDim();

        DecompositionStore<Double> combined = R064Store.FACTORY.make(m, n);
        for (int j = 0; j < n; j++) {
            for (int i = j + 1; i < m; i++) {
                combined.set(i, j, mtrxL.doubleValue(i, j));
            }
        }
        for (int j = 0; j < n; j++) {
            for (int i = 0; i <= j; i++) {
                combined.set(i, j, mtrxU.doubleValue(i, j));
            }
        }

        PhysicalStore<Double> preallocated = R064Store.FACTORY.make(m, 1);

        Pivot myPivot = new Pivot(pivotOrder);
        Pivot myColPivot = new Pivot();
        myColPivot.reset(n);

        if (DEBUG) {
            BasicLogger.debug("Input");
            BasicLogger.debug("P: {}", myPivot);
            BasicLogger.debugMatrix("L", mtrxL);
            BasicLogger.debugMatrix("U", mtrxU);
            BasicLogger.debug("Q: {}", myColPivot);
        }

        // Start here (no changes before this)
        // This is a copy of what's in
        // DenseLU.updateColumn(int,Access1D.Collectable,PhysicalStore)
        // except for the final return statement.
        {

            if (DEBUG) {
                BasicLogger.debug("Initial");
                BasicLogger.debug("P: {}", myPivot);
                MatrixStore<Double> tmpL = combined.triangular(false, true);
                MatrixStore<Double> tmpU = combined.triangular(true, false);
                BasicLogger.debugMatrix("Store", combined);
                BasicLogger.debugMatrix("L", tmpL);
                BasicLogger.debugMatrix("U", tmpU);
                BasicLogger.debugMatrix("LU", tmpL.multiply(tmpU));
                BasicLogger.debug("Q: {}", myColPivot);
            }

            column.supplyTo(preallocated);
            myPivot.applyPivotOrder(preallocated);
            preallocated.substituteForwards(combined, true, false, false);

            // After forward substitution is complete, find the last non-zero row
            int lastRowNonZero = -1;
            for (int i = m - 1; i >= 0; i--) {
                if (!ACCURACY.isZero(preallocated.doubleValue(i))) {
                    lastRowNonZero = i;
                    break; // Stop as soon as we find a non-zero value
                }
            }

            for (int ij = columnIndex; ij < lastRowNonZero; ij++) {

                if (DEBUG) {
                    BasicLogger.debug("start: " + ij);
                    MatrixStore<Double> tmpL = combined.triangular(false, true);
                    MatrixStore<Double> tmpU = combined.triangular(true, false);
                    BasicLogger.debugMatrix("Store", combined);
                    BasicLogger.debugMatrix("L", tmpL);
                    BasicLogger.debugMatrix("U", tmpU);
                    BasicLogger.debugMatrix("LU", tmpL.multiply(tmpU));
                }

                combined.exchangeRows(ij, ij + 1);
                combined.exchangeColumns(ij, ij + 1);

                preallocated.exchangeRows(ij, ij + 1);

                myPivot.change(ij, ij + 1);
                myColPivot.change(ij, ij + 1);

                double offL = combined.doubleValue(ij, ij + 1);
                double offU = combined.doubleValue(ij + 1, ij);

                if (DEBUG) {
                    BasicLogger.debug("exchange");
                    MatrixStore<Double> tmpL = combined.triangular(false, true);
                    MatrixStore<Double> tmpU = combined.triangular(true, false);
                    BasicLogger.debugMatrix("Store", combined);
                    BasicLogger.debugMatrix("L", tmpL);
                    BasicLogger.debugMatrix("U", tmpU);
                    BasicLogger.debugMatrix("LU", tmpL.multiply(tmpU));
                }

                if (!ACCURACY.isZero(offL)) {

                    combined.set(ij, ij + 1, ZERO);
                    for (int i = ij + 2; i < m; i++) { // L (ij+1)
                        combined.add(i, ij + 1, -offL * combined.doubleValue(i, ij));
                    }

                    for (int j = ij; j < n; j++) { // U (ij)
                        combined.add(ij, j, offL * combined.doubleValue(ij + 1, j));
                    }
                    preallocated.add(ij, offL * preallocated.doubleValue(ij + 1));

                    if (DEBUG) {
                        BasicLogger.debug("offL");
                        BasicLogger.debug("P: {}", myPivot);
                        MatrixStore<Double> tmpL = combined.triangular(false, true);
                        MatrixStore<Double> tmpU = combined.triangular(true, false);
                        BasicLogger.debugMatrix("Store", combined);
                        BasicLogger.debugMatrix("L", tmpL);
                        BasicLogger.debugMatrix("U", tmpU);
                        BasicLogger.debugMatrix("LU", tmpL.multiply(tmpU));
                        BasicLogger.debug("Q: {}", myColPivot);
                    }

                } else {

                    // combined.add(ij, ij, offL * combined.doubleValue(ij + 1, ij));
                }

                if (!ACCURACY.isZero(offU)) {

                    double diag = combined.doubleValue(ij, ij);
                    double fact = offU / diag;

                    combined.set(ij + 1, ij, ZERO);
                    for (int j = ij + 2; j < n; j++) { // U (ij+1)
                        combined.add(ij + 1, j, -fact * combined.doubleValue(ij, j));
                    }
                    preallocated.add(ij + 1, -fact * preallocated.doubleValue(ij));

                    combined.set(ij + 1, ij, fact); // L (ij)
                    for (int i = ij + 2; i < m; i++) {
                        combined.add(i, ij, fact * combined.doubleValue(i, ij + 1));
                    }

                    if (DEBUG) {
                        BasicLogger.debug("offU");
                        BasicLogger.debug("P: {}", myPivot);
                        MatrixStore<Double> tmpL = combined.triangular(false, true);
                        MatrixStore<Double> tmpU = combined.triangular(true, false);
                        BasicLogger.debugMatrix("Store", combined);
                        BasicLogger.debugMatrix("L", tmpL);
                        BasicLogger.debugMatrix("U", tmpU);
                        BasicLogger.debugMatrix("LU", tmpL.multiply(tmpU));
                        BasicLogger.debug("Q: {}", myColPivot);
                    }

                } else {

                    // combined.add(ij, ij, offL * combined.doubleValue(ij + 1, ij));
                }
            }

            for (int i = 0; i <= lastRowNonZero; i++) {
                combined.set(i, lastRowNonZero, preallocated.doubleValue(i));
            }

            if (DEBUG) {
                BasicLogger.debug("Final");
                BasicLogger.debug("P: {}", myPivot);
                MatrixStore<Double> tmpL = combined.triangular(false, true);
                MatrixStore<Double> tmpU = combined.triangular(true, false);
                BasicLogger.debugMatrix("Store", combined);
                BasicLogger.debugMatrix("L", tmpL);
                BasicLogger.debugMatrix("U", tmpU);
                BasicLogger.debugMatrix("LU", tmpL.multiply(tmpU));
                BasicLogger.debug("Q: {}", myColPivot);
            }
        }
        // Stop here (no changes after this)

        MatrixStore<Double> tmpL = combined.triangular(false, true);
        MatrixStore<Double> tmpU = combined.triangular(true, false);
        return new Result(myPivot.getOrder(), tmpL, tmpU, myColPivot.getOrder());
    }

    /**
     * A variant of {@link #doFletcherMatthews(int[], PhysicalStore, PhysicalStore, int, MatrixStore)}
     * specifically target to be used with (copied to) {@link SparseLU}.
     * <p>
     * {@link SparseLU} stores the L and U factors separately in {@link R064LSC} (L) and {@link R064LSR} (U)
     * instances with the diagonals of U in a separate double[].
     */
    private static Result doFletcherMatthewsSparse(final int[] pivotOrder, final PhysicalStore<Double> mtrxL, final PhysicalStore<Double> mtrxU,
            final int columnIndex, final MatrixStore<Double> column) {

        int m = mtrxL.getRowDim();
        int r = mtrxU.getMinDim();
        int n = mtrxU.getColDim();

        R064LSC sparseL = R064LSC.FACTORY.make(mtrxL);
        sparseL.fillMatching(mtrxL);

        R064LSR sparseU = R064LSR.FACTORY.make(mtrxU);
        sparseU.fillMatching(mtrxU);

        double[] diagU = new double[r];
        for (int ij = 0; ij < r; ij++) {
            double diagVal = sparseU.doubleValue(ij, ij);
            diagU[ij] = diagVal;
            sparseU.set(ij, ij, ZERO);
        }

        Pivot rowPivot = new Pivot(pivotOrder);
        Pivot colPivot = new Pivot(pivotOrder);
        colPivot.reset(n);

        FletcherMatthews.update(rowPivot, sparseL, diagU, sparseU, colPivot, columnIndex, column, R064Store.FACTORY.make(m, 1));

        MatrixStore<Double> tmpL = sparseL.triangular(false, true);
        MatrixStore<Double> tmpU = sparseU.triangular(true, false).superimpose(DiagonalStore.wrap(diagU));

        return new Result(rowPivot.getOrder(), tmpL, tmpU, colPivot.getOrder());
    }

    /**
     * A naive implementation of LU update for comparison and testing.
     * <p>
     * This method takes a brute-force approach by:
     * <ul>
     * <li>Updating the column in U directly
     * <li>Re-decomposing U to restore triangularity
     * <li>Adjusting L to maintain the factorization
     * </ul>
     * <p>
     * While functionally correct, this approach is:
     * <ul>
     * <li>Computationally inefficient
     * <li>Numerically less stable
     * <li>Does not preserve sparsity
     * <li>Mainly useful for testing and verification
     * </ul>
     *
     * @param pivotOrder  Current row permutation vector
     * @param mtrxL       Current L matrix
     * @param mtrxU       Current U matrix
     * @param columnIndex Index of column being updated
     * @param newColumn   New column values
     * @return Tuple containing updated L, U matrices and permutation vectors
     */
    private static Result doItTheStupidWay(final int[] pivotOrder, final PhysicalStore<Double> mtrxL, final PhysicalStore<Double> mtrxU, final int columnIndex,
            final MatrixStore<Double> newColumn) {

        int m = mtrxL.getRowDim();
        int n = mtrxU.getColDim();

        PhysicalStore<Double> transformedColumn = newColumn.rows(pivotOrder).copy();
        transformedColumn.substituteForwards(mtrxL, true, false, false);

        mtrxU.fillColumn(columnIndex, transformedColumn);

        LU<Double> decompU = LU.R064.decompose(mtrxU);
        int[] uOrder = decompU.getPivotOrder();
        MatrixStore<Double> uL = decompU.getL();
        MatrixStore<Double> uU = decompU.getU();

        MatrixStore<Double> tmpL = mtrxL.columns(uOrder).multiply(uL);

        LU<Double> decompL = LU.R064.decompose(tmpL);
        int[] lOrder = decompL.getPivotOrder();
        MatrixStore<Double> lL = decompL.getL();
        MatrixStore<Double> lU = decompL.getU();

        int[] newOrder = new int[m];
        for (int i = 0; i < m; i++) {
            newOrder[i] = pivotOrder[lOrder[i]];
        }
        MatrixStore<Double> newL = lL;
        MatrixStore<Double> newU = lU.multiply(uU);

        COL_ORDER.reset(n);

        return new Result(newOrder, newL, newU, COL_ORDER.getOrder());
    }

    private static Result doUsingDecomposition(final MatrixStore<Double> original, final int columnIndex, final MatrixStore<Double> column,
            final Supplier<LU<Double>> factory) {

        LU<Double> decomposition = factory.get();

        decomposition.decompose(original);

        decomposition.updateColumn(columnIndex, column);

        return new Result(decomposition.getPivotOrder(), decomposition.getL(), decomposition.getU());
    }

    private static MatrixStore<Double> newColumn(final R064Store matrix, final int indexOfLastNonzero) {

        int m = matrix.getRowDim();

        // Get the LU decomposition to determine L and P
        LU<Double> tmpLU = LU.R064.decompose(matrix);
        int[] pivotOrder = tmpLU.getPivotOrder();
        int[] reverseOrder = tmpLU.getReversePivotOrder();
        MatrixStore<Double> L = tmpLU.getL();
        MatrixStore<Double> U = tmpLU.getU();

        if (DEBUG) {
            BasicLogger.debugMatrix("Original A", matrix, PRINT);
            BasicLogger.debug("Pivot order: {}", pivotOrder);
            BasicLogger.debugMatrix("L", L, PRINT);
            BasicLogger.debugMatrix("U", U, PRINT);
        }

        // Create new column that will result in zeros in the last rows, after L⁻¹P transformation
        // First, create the desired transformed column w with zeros in last positions

        R064Store w = DecompositionUpdateTest.newEmpty(m, 1);
        for (int i = 0; i <= indexOfLastNonzero; i++) {
            w.set(i, i + 1);
        }

        MatrixStore<Double> newColumn = L.multiply(w).rows(reverseOrder);

        if (DEBUG) {
            BasicLogger.debug("New column before transformation:");
            BasicLogger.debugMatrix("v", newColumn, PRINT);

            // Verify zeros after transformation
            PhysicalStore<Double> w_check = newColumn.rows(pivotOrder).copy();
            w_check.substituteForwards(L, true, false, false);
            BasicLogger.debug("New column after L⁻¹P transformation:");
            BasicLogger.debugMatrix("w", w_check, PRINT);
        }

        return newColumn;
    }

    private static R064Store newEmpty(final int nbRows, final int nbCols) {
        return R064Store.FACTORY.make(nbRows, nbCols);
    }

    private static R064Store newRandom(final int nbRows, final int nbCols) {
        return R064Store.FACTORY.makeFilled(nbRows, nbCols, RANDOMIZER);
    }

    static void doOne(final String implName, final MatrixStore<Double> originalMatrix, final int columnIndex, final MatrixStore<Double> newColumn,
            final PhysicalStore<Double> modifiedMatrix, final int[] pivotOrder, final MatrixStore<Double> mtrxL, final MatrixStore<Double> mtrxU,
            final Calculator implAlgo, final Supplier<LU<Double>> factory) {

        if (DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("==============================================");
            BasicLogger.debug("Now running " + implName);
            BasicLogger.debug("==============================================");
            BasicLogger.debug();
        }

        Result result = null;
        if (factory != null) {
            result = DecompositionUpdateTest.doUsingDecomposition(originalMatrix.copy(), columnIndex, newColumn.copy(), factory);
        } else if (implAlgo != null) {
            result = implAlgo.calculate(Arrays.copyOf(pivotOrder, pivotOrder.length), mtrxL.copy(), mtrxU.copy(), columnIndex, newColumn.copy());
        } else {
            throw new IllegalArgumentException();
        }

        int m = originalMatrix.getRowDim();
        int n = originalMatrix.getColDim();
        int r = originalMatrix.getMinDim();

        MatrixStore<Double> resL = result.mtrxL;
        MatrixStore<Double> resU = result.mtrxU;

        int[] resRowOrder = result.rowOrder;
        int[] resRowRever = Pivot.reverse(resRowOrder);

        int[] resColOrder = result.colOrder;
        int[] resColRever = resColOrder != null ? Pivot.reverse(resColOrder) : null;

        if (DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("Result " + implName);
            BasicLogger.debug("Row order: {}/{}", resRowOrder, resRowRever);
            BasicLogger.debugMatrix("L", resL, PRINT);
            BasicLogger.debugMatrix("U", resU, PRINT);
            BasicLogger.debug("Col order: {}/{}", resColOrder, resColRever);
        }

        for (int j = 0; j < r; j++) {
            for (int i = 0; i < j; i++) {
                TestUtils.assertEquals("L not triagonal", ZERO, resL.doubleValue(i, j));
            }
        }

        if (resColOrder != null) {
            // If null, the columns of U are already reordered so that U is no longer triangular
            for (int i = 0; i < r; i++) {
                for (int j = 0; j < i; j++) {
                    TestUtils.assertEquals("U not triagonal", ZERO, resU.doubleValue(i, j));
                }
            }
        }

        MatrixStore<Double> reconstructed = resL.multiply(resU).rows(resRowRever);
        if (resColRever != null) {
            reconstructed = reconstructed.columns(resColRever);
        }
        if (DEBUG) {
            BasicLogger.debugMatrix("Reconstructed", reconstructed, PRINT);
        }
        TestUtils.assertEquals(modifiedMatrix, reconstructed, PRINT);

        if (originalMatrix.isSquare()) {

            R064Store rhs = DecompositionUpdateTest.newRandom(m, 1);

            if (DEBUG) {
                BasicLogger.debugMatrix("RHS", rhs, PRINT);
            }

            LU<Double> expectedLU = LU.R064.decompose(modifiedMatrix);

            LU<Double> calculationLU = LU.R064.make(m, n);

            Optional<MatrixStore<Double>> maybe = expectedLU.solve(rhs);
            if (maybe.isPresent()) {
                MatrixStore<Double> expectedSolution = maybe.get();

                if (DEBUG) {
                    BasicLogger.debugMatrix("Expected solution", expectedSolution, PRINT);
                }

                MatrixStore<Double> actualSolution = rhs.rows(resRowOrder);
                try {
                    actualSolution = calculationLU.solve(resL, actualSolution);
                    actualSolution = calculationLU.solve(resU, actualSolution);
                    if (resColRever != null) {
                        actualSolution = actualSolution.rows(resColRever);
                    }
                } catch (RecoverableCondition cause) {
                    TestUtils.fail(cause);
                }

                if (DEBUG) {
                    BasicLogger.debugMatrix("Actual solution", actualSolution, PRINT);
                }

                TestUtils.assertEquals(expectedSolution, actualSolution, PRINT);
            }
        }
    }

    static void doTest(final MatrixStore<Double> originalMatrix, final int columnIndex, final MatrixStore<Double> newColumn) {

        PhysicalStore<Double> modifiedMatrix = originalMatrix.copy();
        modifiedMatrix.fillColumn(columnIndex, newColumn);

        LU<Double> orgDecomp = LU.R064.decompose(originalMatrix);
        LU<Double> modDecomp = LU.R064.decompose(modifiedMatrix);

        int[] pivotOrder = orgDecomp.getPivotOrder();
        MatrixStore<Double> mtrxL = orgDecomp.getL();
        MatrixStore<Double> mtrxU = orgDecomp.getU();

        if (DEBUG) {

            BasicLogger.debug();
            BasicLogger.debug("==============================================");
            BasicLogger.debug("==============================================");
            BasicLogger.debug("NEW CASE");
            BasicLogger.debug("==============================================");
            BasicLogger.debug("==============================================");
            BasicLogger.debug();

            BasicLogger.debugMatrix("Original matrix", originalMatrix, PRINT);
            BasicLogger.debugMatrix("New column for index " + columnIndex, newColumn.transpose(), PRINT);
            BasicLogger.debugMatrix("Modified matrix", modifiedMatrix, PRINT);

            BasicLogger.debug("Original LU");
            BasicLogger.debug("Pivot order: {}/{}", pivotOrder, orgDecomp.getReversePivotOrder());
            BasicLogger.debugMatrix("L", mtrxL, PRINT);
            BasicLogger.debugMatrix("U", mtrxU, PRINT);

            BasicLogger.debug("Modified LU");
            BasicLogger.debug("Pivot order: {}/{}", modDecomp.getPivotOrder(), modDecomp.getReversePivotOrder());
            BasicLogger.debugMatrix("L", modDecomp.getL(), PRINT);
            BasicLogger.debugMatrix("U", modDecomp.getU(), PRINT);
        }

        DecompositionUpdateTest.doOne("The stupid way", originalMatrix, columnIndex, newColumn, modifiedMatrix, pivotOrder, mtrxL, mtrxU,
                DecompositionUpdateTest::doItTheStupidWay, null);

        DecompositionUpdateTest.doOne("Fletcher-Matthews (outline)", originalMatrix, columnIndex, newColumn, modifiedMatrix, pivotOrder, mtrxL, mtrxU,
                DecompositionUpdateTest::doFletcherMatthews, null);

        DecompositionUpdateTest.doOne("Fletcher-Matthews (sparse)", originalMatrix, columnIndex, newColumn, modifiedMatrix, pivotOrder, mtrxL, mtrxU,
                DecompositionUpdateTest::doFletcherMatthewsSparse, null);

        DecompositionUpdateTest.doOne("Fletcher-Matthews (dense)", originalMatrix, columnIndex, newColumn, modifiedMatrix, pivotOrder, mtrxL, mtrxU,
                DecompositionUpdateTest::doFletcherMatthewsDense, null);

        DecompositionUpdateTest.doOne("SparseLU", originalMatrix, columnIndex, newColumn, modifiedMatrix, pivotOrder, mtrxL, mtrxU, null, SparseLU::new);

        DecompositionUpdateTest.doOne("DenseLU", originalMatrix, columnIndex, newColumn, modifiedMatrix, pivotOrder, mtrxL, mtrxU, null, DenseLU.R064::new);

        DecompositionUpdateTest.doOne("RawLU", originalMatrix, columnIndex, newColumn, modifiedMatrix, pivotOrder, mtrxL, mtrxU, null, RawLU::new);

    }

    @Test
    public void test3x3NoPivotingOrSpikes() {

        // Create a simple 3x3 matrix that we know works well
        // Initial matrix with a clear structure
        R064Store matrix = DecompositionUpdateTest.newEmpty(3, 3);
        matrix.set(0, 0, 4.0);
        matrix.set(0, 1, 1.0);
        matrix.set(0, 2, 0.0);
        matrix.set(1, 0, 2.0);
        matrix.set(1, 1, 3.0);
        matrix.set(1, 2, 1.0);
        matrix.set(2, 0, 1.0);
        matrix.set(2, 1, 1.0);
        matrix.set(2, 2, 2.0);

        // Create new column to replace column 1 (index 1)
        R064Store newColumn = DecompositionUpdateTest.newEmpty(3, 1);
        newColumn.set(0, 0, 1.0);
        newColumn.set(1, 0, 4.0);
        newColumn.set(2, 0, 2.0);

        int columnIndex = 1;

        DecompositionUpdateTest.doTest(matrix, columnIndex, newColumn);
    }

    /**
     * Tests updateColumn on a small matrix with a known pattern.
     */
    @Test
    public void test3x3SmallMatrixUpdate() {

        // Create simple 3x3 matrix
        R064Store matrix = DecompositionUpdateTest.newEmpty(3, 3);
        matrix.set(0, 0, 4.0);
        matrix.set(0, 1, 2.0);
        matrix.set(0, 2, 1.0);
        matrix.set(1, 0, 3.0);
        matrix.set(1, 1, 1.0);
        matrix.set(1, 2, 2.0);
        matrix.set(2, 0, 2.0);
        matrix.set(2, 1, 5.0);
        matrix.set(2, 2, 3.0);

        // Create new column to replace column 1
        R064Store newColumn = DecompositionUpdateTest.newEmpty(3, 1);
        newColumn.set(0, 0, 5.0);
        newColumn.set(1, 0, 7.0);
        newColumn.set(2, 0, 9.0);

        // Test the update
        DecompositionUpdateTest.doTest(matrix, 1, newColumn);
    }

    /**
     * Test method that demonstrates the high-level Forrest-Tomlin update algorithm.
     * <P>
     * Testing with a case that creates a spike:
     */
    @Test
    public void test4x4withSpikes() {

        // Create a 4x4 matrix
        R064Store matrixWithSpike = DecompositionUpdateTest.newEmpty(4, 4);

        // Initial matrix
        matrixWithSpike.set(0, 0, 4.0);
        matrixWithSpike.set(0, 1, 2.0);
        matrixWithSpike.set(0, 2, 1.0);
        matrixWithSpike.set(0, 3, 3.0);

        matrixWithSpike.set(1, 0, 3.0);
        matrixWithSpike.set(1, 1, 5.0);
        matrixWithSpike.set(1, 2, 2.0);
        matrixWithSpike.set(1, 3, 1.0);

        matrixWithSpike.set(2, 0, 2.0);
        matrixWithSpike.set(2, 1, 1.0);
        matrixWithSpike.set(2, 2, 6.0);
        matrixWithSpike.set(2, 3, 4.0);

        matrixWithSpike.set(3, 0, 1.0);
        matrixWithSpike.set(3, 1, 3.0);
        matrixWithSpike.set(3, 2, 2.0);
        matrixWithSpike.set(3, 3, 7.0);

        // Create new column with a spike
        R064Store newColumnWithSpike = DecompositionUpdateTest.newEmpty(4, 1);
        newColumnWithSpike.set(0, 0, 2.0);
        newColumnWithSpike.set(1, 0, 1.0);
        newColumnWithSpike.set(2, 0, 3.0); // Diagonal element
        newColumnWithSpike.set(3, 0, 8.0); // Spike - larger than diagonal

        int columnIndex = 2;

        DecompositionUpdateTest.doTest(matrixWithSpike, columnIndex, newColumnWithSpike);
    }

    /**
     * Tests a simple column update that doesn't require spike handling. This test uses a carefully
     * constructed 5x5 matrix and updates column 2 with values that maintain the triangular structure without
     * creating spikes.
     */
    @Test
    public void test5x5WithoutSpike() {

        // Create a 5x5 matrix with a specific structure
        R064Store matrix = DecompositionUpdateTest.newEmpty(5, 5);

        // Initial matrix with a clear structure
        matrix.set(0, 0, 5.0);
        matrix.set(0, 1, 2.0);
        matrix.set(0, 2, 3.0);
        matrix.set(0, 3, 1.0);
        matrix.set(0, 4, 4.0);

        matrix.set(1, 0, 2.0);
        matrix.set(1, 1, 6.0);
        matrix.set(1, 2, 1.0);
        matrix.set(1, 3, 2.0);
        matrix.set(1, 4, 3.0);

        matrix.set(2, 0, 1.0);
        matrix.set(2, 1, 3.0);
        matrix.set(2, 2, 7.0);
        matrix.set(2, 3, 4.0);
        matrix.set(2, 4, 2.0);

        matrix.set(3, 0, 3.0);
        matrix.set(3, 1, 1.0);
        matrix.set(3, 2, 2.0);
        matrix.set(3, 3, 8.0);
        matrix.set(3, 4, 1.0);

        matrix.set(4, 0, 4.0);
        matrix.set(4, 1, 2.0);
        matrix.set(4, 2, 1.0);
        matrix.set(4, 3, 3.0);
        matrix.set(4, 4, 9.0);

        // Create new column to replace column 2 (index 2)
        // These values are chosen to not create spikes
        R064Store newColumn = DecompositionUpdateTest.newEmpty(5, 1);
        newColumn.set(0, 0, 4.0); // Value for row 0
        newColumn.set(1, 0, 2.0); // Value for row 1
        newColumn.set(2, 0, 8.0); // Value for row 2 (diagonal element, larger than others)
        newColumn.set(3, 0, 3.0); // Value for row 3
        newColumn.set(4, 0, 2.0); // Value for row 4

        // Test the update
        DecompositionUpdateTest.doTest(matrix, 2, newColumn);
    }

    @Test
    public void test7x7WithZeros() {

        // Create a 7x7 matrix with about half zeros and structure that forces pivoting
        R064Store matrix = DecompositionUpdateTest.newEmpty(7, 7);

        // Pattern that ensures full rank and forces pivoting (small element in first position)
        matrix.set(0, 0, 0.1);
        matrix.set(0, 3, 1.0);
        matrix.set(0, 6, 2.0);
        matrix.set(1, 0, 2.0);
        matrix.set(1, 1, 5.0);
        matrix.set(1, 3, -1.0);
        matrix.set(2, 1, 3.0);
        matrix.set(2, 2, 6.0);
        matrix.set(2, 4, 2.0);
        matrix.set(3, 2, 2.0);
        matrix.set(3, 3, 7.0);
        matrix.set(3, 5, -1.0);
        matrix.set(4, 3, 1.0);
        matrix.set(4, 4, 4.0);
        matrix.set(4, 6, 3.0);
        matrix.set(5, 0, 1.0);
        matrix.set(5, 5, 5.0);
        matrix.set(6, 1, -1.0);
        matrix.set(6, 6, 6.0);

        for (int columnIndex = 0; columnIndex < 7; columnIndex++) {
            for (int lastRowNonZero = columnIndex; lastRowNonZero < 7; lastRowNonZero++) {

                if (DEBUG) {
                    BasicLogger.debug("Case: {}, {}", columnIndex, lastRowNonZero);
                }

                MatrixStore<Double> newColumn = DecompositionUpdateTest.newColumn(matrix, lastRowNonZero);

                R064Store copy = matrix.copy();
                copy.fillColumn(columnIndex, newColumn);

                LU<Double> decompose = LU.R064.decompose(copy);

                if (DEBUG) {
                    if (!decompose.isSolvable()) {
                        BasicLogger.debug("isSolvable: {}", decompose.isSolvable());
                    }
                }
                // Test the update
                DecompositionUpdateTest.doTest(matrix, columnIndex, newColumn);
            }

        }

    }

    /**
     * This test case is specifically designed to have no spikes according to the Bartels-Golub-Reid
     * algorithm. The matrix and update column are carefully constructed so that after forward substitution,
     * all elements below the diagonal in the column being updated are exactly zero.
     */
    @Test
    public void testBGRNoSpikes() {

        // Create a 3x3 matrix
        R064Store matrix = DecompositionUpdateTest.newEmpty(3, 3);
        matrix.set(0, 0, 4.0);
        matrix.set(0, 1, 2.0);
        matrix.set(0, 2, 1.0);
        matrix.set(1, 0, 2.0);
        matrix.set(1, 1, 3.0);
        matrix.set(1, 2, 2.0);
        matrix.set(2, 0, 1.0);
        matrix.set(2, 1, 2.0);
        matrix.set(2, 2, 4.0);

        // Get the LU decomposition to determine L
        LU<Double> tmpLU = LU.R064.decompose(matrix);
        MatrixStore<Double> L = tmpLU.getL();

        // Extract L values we need for calculation
        double L10 = L.doubleValue(1, 0); // L(1,0)
        double L20 = L.doubleValue(2, 0); // L(2,0)
        double L21 = L.doubleValue(2, 1); // L(2,1)

        if (DEBUG) {
            BasicLogger.debugMatrix("L matrix for original matrix", L, PRINT);
            BasicLogger.debug("L values: L10={}, L20={}, L21={}", L10, L20, L21);
        }

        // We want to update column 1 (index 1)
        int columnIndex = 1;

        // Create a new column that will result in no spikes after forward substitution
        // For this, we need to carefully choose values so that:
        // transformedColumn[2] = 0 (no element below diagonal)

        // After forward substitution:
        // transformedColumn[0] = newColumn[0]
        // transformedColumn[1] = newColumn[1] - L10 * transformedColumn[0]
        // transformedColumn[2] = newColumn[2] - L20 * transformedColumn[0] - L21 * transformedColumn[1]

        // We can choose any value for transformedColumn[0] and transformedColumn[1]
        double t0 = 3.0; // Choose any value
        double t1 = 4.0; // Choose any value

        // To make transformedColumn[2] = 0, we need:
        // newColumn[2] = L20 * transformedColumn[0] + L21 * transformedColumn[1]
        double newCol0 = t0;
        double newCol1 = t1 + L10 * t0;
        double newCol2 = L20 * t0 + L21 * (t1);

        R064Store newColumn = DecompositionUpdateTest.newEmpty(3, 1);
        newColumn.set(0, 0, newCol0);
        newColumn.set(1, 0, newCol1);
        newColumn.set(2, 0, newCol2);

        if (DEBUG) {
            BasicLogger.debug("Calculated new column: [{}, {}, {}]", newCol0, newCol1, newCol2);
        }

        // Test the update
        DecompositionUpdateTest.doTest(matrix, columnIndex, newColumn);
    }

    /**
     * This test case is specifically designed to have exactly one spike according to the Bartels-Golub-Reid
     * algorithm. The matrix and update column are carefully constructed so that after forward substitution,
     * there is exactly one non-zero element below the diagonal in the column being updated.
     */
    @Test
    public void testBGRWithExactlyOneSpike() {

        // Create a 3x3 matrix - using the same matrix as in testBGRNoSpikes
        R064Store matrix = DecompositionUpdateTest.newEmpty(3, 3);
        matrix.set(0, 0, 4.0);
        matrix.set(0, 1, 2.0);
        matrix.set(0, 2, 1.0);
        matrix.set(1, 0, 2.0);
        matrix.set(1, 1, 3.0);
        matrix.set(1, 2, 2.0);
        matrix.set(2, 0, 1.0);
        matrix.set(2, 1, 2.0);
        matrix.set(2, 2, 4.0);

        // Get the LU decomposition to determine L
        LU<Double> tmpLU = LU.R064.decompose(matrix);
        MatrixStore<Double> L = tmpLU.getL();

        // Extract L values we need for calculation
        double L10 = L.doubleValue(1, 0); // L(1,0)
        double L20 = L.doubleValue(2, 0); // L(2,0)
        double L21 = L.doubleValue(2, 1); // L(2,1)

        if (DEBUG) {
            BasicLogger.debug("L matrix for original matrix:");
            BasicLogger.debugMatrix("L", L, PRINT);
            BasicLogger.debug("L values: L10={}, L20={}, L21={}", L10, L20, L21);
        }

        // We want to update column 1 (index 1)
        int columnIndex = 1;

        // Create a new column that will result in exactly one spike after forward substitution
        // For this, we need to carefully choose values so that:
        // transformedColumn[2] = 1.0 (a specific non-zero value below diagonal)

        // After forward substitution:
        // transformedColumn[0] = newColumn[0]
        // transformedColumn[1] = newColumn[1] - L10 * transformedColumn[0]
        // transformedColumn[2] = newColumn[2] - L20 * transformedColumn[0] - L21 * transformedColumn[1]

        // We can choose any value for transformedColumn[0] and transformedColumn[1]
        double t0 = 3.0; // Choose any value
        double t1 = 4.0; // Choose any value
        double t2 = 1.0; // The spike value we want

        // To make transformedColumn[2] = t2, we need:
        // newColumn[2] = t2 + L20 * transformedColumn[0] + L21 * transformedColumn[1]
        double newCol0 = t0;
        double newCol1 = t1 + L10 * t0;
        double newCol2 = t2 + L20 * t0 + L21 * (t1);

        R064Store newColumn = DecompositionUpdateTest.newEmpty(3, 1);
        newColumn.set(0, 0, newCol0);
        newColumn.set(1, 0, newCol1);
        newColumn.set(2, 0, newCol2);

        if (DEBUG) {
            BasicLogger.debug("Calculated new column: [{}, {}, {}]", newCol0, newCol1, newCol2);
        }

        // Test the update
        DecompositionUpdateTest.doTest(matrix, columnIndex, newColumn);
    }

    @Test
    public void testCaseGPT() {

        RawStore mtrxA = RawStore.wrap(new double[][] { { 2.0, 3.0, 4.0 }, { 1.0, 3.0, 4.5 }, { 1.0, 0.75, 1.75 } });

        RawStore mtrxL = RawStore.wrap(new double[][] { { 1.0, 0.0, 0.0 }, { 0.5, 1.0, 0.0 }, { 0.5, -0.5, 1.0 } });
        RawStore mtrxU = RawStore.wrap(new double[][] { { 2.0, 3.0, 4.0 }, { 0.0, 1.5, 2.5 }, { 0.0, 0.0, 1.0 } });
        int[] pivotOrder = { 0, 1, 2 };

        TestUtils.assertEquals(mtrxA, mtrxL.multiply(mtrxU).rows(Pivot.reverse(pivotOrder)));

        LU<Double> tmpLU = LU.R064.decompose(mtrxA);
        TestUtils.assertEquals(mtrxL, tmpLU.getL());
        TestUtils.assertEquals(mtrxU, tmpLU.getU());
        TestUtils.assertEquals(pivotOrder, tmpLU.getPivotOrder());
        tmpLU.reset();

        int colIndex = 1;
        MatrixStore<Double> colElements = RawStore.wrap(3.0, 1.0, 2.0).transpose();

        DecompositionUpdateTest.doTest(mtrxA, colIndex, colElements);
    }

    /**
     * Tests updateColumn with ill-conditioned matrices.
     */
    @Test
    public void testIllConditionedMatrix() {

        // Create an ill-conditioned matrix
        R064Store matrix = DecompositionUpdateTest.newEmpty(5, 5);
        matrix.set(0, 0, 1.0);
        matrix.set(0, 1, 1.0);
        matrix.set(0, 2, 1.0);
        matrix.set(0, 3, 1.0);
        matrix.set(0, 4, 1.0);
        matrix.set(1, 0, 1.0);
        matrix.set(1, 1, 1.001);
        matrix.set(1, 2, 1.0);
        matrix.set(1, 3, 1.0);
        matrix.set(1, 4, 1.0);
        matrix.set(2, 0, 1.0);
        matrix.set(2, 1, 1.0);
        matrix.set(2, 2, 1.002);
        matrix.set(2, 3, 1.0);
        matrix.set(2, 4, 1.0);
        matrix.set(3, 0, 1.0);
        matrix.set(3, 1, 1.0);
        matrix.set(3, 2, 1.0);
        matrix.set(3, 3, 1.003);
        matrix.set(3, 4, 1.0);
        matrix.set(4, 0, 1.0);
        matrix.set(4, 1, 1.0);
        matrix.set(4, 2, 1.0);
        matrix.set(4, 3, 1.0);
        matrix.set(4, 4, 1.004);

        // Create new column
        R064Store newColumn = DecompositionUpdateTest.newEmpty(5, 1);
        newColumn.set(0, 0, 2.0);
        newColumn.set(1, 0, 3.0);
        newColumn.set(2, 0, 4.0);
        newColumn.set(3, 0, 5.0);
        newColumn.set(4, 0, 6.0);

        // Test column update on an ill-conditioned matrix
        DecompositionUpdateTest.doTest(matrix, 2, newColumn);
    }

    @Test
    public void testRandomMatrixUpdate() {

        int dim = 7;

        // Create random square matrix
        R064Store matrix = DecompositionUpdateTest.newRandom(dim, dim);

        // Test updating each column
        for (int col = 0; col < dim; col++) {
            R064Store newColumn = DecompositionUpdateTest.newRandom(dim, 1);
            DecompositionUpdateTest.doTest(matrix, col, newColumn);
        }

    }

    /**
     * Tests sequential updates of multiple columns.
     */
    @Test
    public void testSequentialUpdates() {

        int dim = 10;
        R064Store matrix = DecompositionUpdateTest.newRandom(dim, dim);

        // Make a copy for full decomposition at the end
        R064Store updatedMatrix = matrix.copy();

        // Create a new LU decomposition
        RawLU lu = new RawLU();
        lu.decompose(matrix);

        // Update multiple columns in sequence
        for (int col = 0; col < dim; col += 2) {
            R064Store newColumn = DecompositionUpdateTest.newRandom(dim, 1);

            DecompositionUpdateTest.doTest(matrix, col, newColumn);

            updatedMatrix.fillColumn(col, newColumn);

        }
    }
}
