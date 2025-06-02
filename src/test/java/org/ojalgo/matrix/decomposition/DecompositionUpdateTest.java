package org.ojalgo.matrix.decomposition;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Uniform;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.keyvalue.EntryPair;
import org.ojalgo.type.keyvalue.EntryPair.KeyedPrimitive;

/**
 * Tests primarily used for initial work on updatable LU decompositions.
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

    static class UpdateCase implements Structure2D {

        final int columnIndex;
        final MatrixStore<Double> newColumn;
        final MatrixStore<Double> originalMatrix;

        UpdateCase(final MatrixStore<Double> originalMatrix, final int columnIndex, final MatrixStore<Double> newColumn) {
            super();
            this.originalMatrix = originalMatrix;
            this.columnIndex = columnIndex;
            this.newColumn = newColumn;
        }

        @Override
        public int getColDim() {
            return originalMatrix.getColDim();
        }

        @Override
        public int getRowDim() {
            return originalMatrix.getRowDim();
        }

        MatrixStore<Double> getModifiedMatrix() {
            PhysicalStore<Double> copy = originalMatrix.copy();
            copy.fillColumn(columnIndex, newColumn);
            return copy;
        }

        MatrixStore<Double> getOriginalColumn(final int col) {
            return originalMatrix.column(col);
        }

        MatrixStore<Double> rhs() {
            return DecompositionUpdateTest.rhs(originalMatrix.getRowDim());
        }

    }

    static class UpdateSequence implements Structure2D {

        PhysicalStore<Double> matrix;

        List<KeyedPrimitive<MatrixStore<Double>>> updates;

        UpdateSequence(final PhysicalStore<Double> matrix) {
            this(matrix, new ArrayList<>());
        }

        UpdateSequence(final PhysicalStore<Double> matrix, final List<KeyedPrimitive<MatrixStore<Double>>> updates) {
            super();
            this.matrix = matrix;
            this.updates = updates;
        }

        @Override
        public int getColDim() {
            return matrix.getColDim();
        }

        @Override
        public int getRowDim() {
            return matrix.getRowDim();
        }

        void add(final int index, final MatrixStore<Double> column) {
            updates.add(EntryPair.of(column, index));
        }

        MatrixStore<Double> rhs() {
            return DecompositionUpdateTest.rhs(matrix.getRowDim());
        }

        List<UpdateCase> toIndividualCases() {

            List<UpdateCase> retVal = new ArrayList<>();

            PhysicalStore<Double> originalMatrix = matrix.copy();

            for (KeyedPrimitive<MatrixStore<Double>> update : updates) {

                int columnIndex = update.intValue();
                MatrixStore<Double> newColumn = update.first();

                retVal.add(new UpdateCase(originalMatrix, columnIndex, newColumn));

                originalMatrix = originalMatrix.copy();
                originalMatrix.fillColumn(columnIndex, newColumn);
            }

            return retVal;
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

    static void doTest(final UpdateCase updateCase) {

        MatrixStore<Double> originalMatrix = updateCase.originalMatrix;
        int columnIndex = updateCase.columnIndex;
        MatrixStore<Double> newColumn = updateCase.newColumn;

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

        DecompositionUpdateTest.doOne("Fletcher-Matthews (dense)", originalMatrix, columnIndex, newColumn, modifiedMatrix, pivotOrder, mtrxL, mtrxU,
                DecompositionUpdateTest::doFletcherMatthewsDense, null);

        DecompositionUpdateTest.doOne("DenseLU", originalMatrix, columnIndex, newColumn, modifiedMatrix, pivotOrder, mtrxL, mtrxU, null, DenseLU.R064::new);

        DecompositionUpdateTest.doOne("RawLU", originalMatrix, columnIndex, newColumn, modifiedMatrix, pivotOrder, mtrxL, mtrxU, null, RawLU::new);
    }

    static void doTestTran(final MatrixStore<Double> body, final LU<Double> decomposition, final MatrixStore<Double> rhs) {

        LU<Double> dense = LU.R064.decompose(body);

        if (dense.isSolvable()) {

            PhysicalStore<Double> fDense = rhs.copy();
            PhysicalStore<Double> fSparse = rhs.copy();
            dense.ftran(fDense);
            decomposition.ftran(fSparse);

            TestUtils.assertEquals(rhs, body.multiply(fDense));
            TestUtils.assertEquals(fDense, fSparse);
            TestUtils.assertEquals(rhs, body.multiply(fSparse));

            PhysicalStore<Double> bDense = rhs.copy();
            PhysicalStore<Double> bSparse = rhs.copy();
            dense.btran(bDense);
            decomposition.btran(bSparse);

            TestUtils.assertEquals(rhs, body.transpose().multiply(bDense));
            TestUtils.assertEquals(bDense, bSparse);
            TestUtils.assertEquals(rhs, body.transpose().multiply(bSparse));
        }
    }

    /**
     * Creates a test case for a simple 3x3 matrix update that doesn't require pivoting or spike handling. The
     * matrix has a clear structure with non-zero elements and the update column is designed to maintain the
     * triangular structure without creating spikes. This test case is useful for verifying basic column
     * update functionality.
     */
    static UpdateCase make3x3NoPivotingOrSpikes() {

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
        R064Store column = DecompositionUpdateTest.newEmpty(3, 1);
        column.set(0, 0, 1.0);
        column.set(1, 0, 4.0);
        column.set(2, 0, 2.0);

        return new UpdateCase(matrix, 1, column);
    }

    /**
     * Creates a test case for updating a small 3x3 matrix where the update results in a zero on the diagonal
     * in the U matrix. This test case is specifically designed to test how the decomposition handles updates
     * that could potentially make the matrix singular.
     */
    static UpdateCase make3x3SmallMatrixUpdate() {

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
        return new UpdateCase(matrix, 1, newColumn);
    }

    static UpdateCase make4x4withSpikes() {

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

        return new UpdateCase(matrixWithSpike, 2, newColumnWithSpike);
    }

    /**
     * Creates a test case for a 5x5 matrix update that doesn't require spike handling. The matrix and update
     * column are carefully constructed to maintain the triangular structure without creating spikes. This
     * test case is useful for verifying that the update algorithm correctly handles larger matrices when no
     * special spike handling is needed.
     */
    static UpdateCase make5x5WithoutSpike() {

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
        newColumn.set(4, 0, 2.0);

        // Value for row 4

        // Test the update
        return new UpdateCase(matrix, 2, newColumn);
    }

    /**
     * Creates a set of test cases for a 7x7 matrix with approximately half zero elements and a structure that
     * forces pivoting. The method generates multiple update cases by systematically updating each column with
     * different patterns of non-zero elements. This test suite is useful for verifying that the update
     * algorithm correctly handles sparse matrices and maintains numerical stability through pivoting.
     */
    static List<UpdateCase> make7x7WithZeros() {

        // Create a 7x7 matrix with about half zeros and structure that forces pivoting
        R064Store matrix = DecompositionUpdateTest.newEmpty(7, 7);
        List<UpdateCase> retVal = new ArrayList<>();

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
                MatrixStore<Double> newColumn = DecompositionUpdateTest.newColumn(matrix, lastRowNonZero);
                retVal.add(new UpdateCase(matrix, columnIndex, newColumn));
            }
        }

        return retVal;
    }

    /**
     * Creates a test case specifically designed to have no spikes according to the Bartels-Golub-Reid (BGR)
     * algorithm. The matrix and update column are carefully constructed so that after forward substitution,
     * all elements below the diagonal in the column being updated are exactly zero. This test case is useful
     * for verifying the BGR algorithm's handling of updates that don't require spike elimination.
     */
    static UpdateCase makeBGRNoSpikes() {

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
        return new UpdateCase(matrix, 1, newColumn);
    }

    /**
     * Creates a test case specifically designed to have exactly one spike according to the Bartels-Golub-Reid
     * (BGR) algorithm. The matrix and update column are carefully constructed so that after forward
     * substitution, there is exactly one non-zero element below the diagonal in the column being updated.
     * This test case is useful for verifying the BGR algorithm's spike elimination process.
     */
    static UpdateCase makeBGRWithExactlyOneSpike() {

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
        final MatrixStore<Double> originalMatrix = matrix;
        final int columnIndex1 = columnIndex;
        final MatrixStore<Double> newColumn1 = newColumn;

        // Test the update
        UpdateCase updateCase = new UpdateCase(originalMatrix, columnIndex1, newColumn1);
        return updateCase;
    }

    /**
     * Creates a test case based on a specific 3x3 matrix example that demonstrates the relationship between
     * the original matrix, its LU decomposition, and column updates. The test case includes a known L and U
     * matrix pair along with their corresponding pivot order, making it useful for verifying the correctness
     * of the decomposition and update process.
     */
    static UpdateCase makeCaseGPT() {

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

        MatrixStore<Double> transpose = RawStore.wrap(3.0, 1.0, 2.0).transpose();

        UpdateCase updateCase = new UpdateCase(mtrxA, 1, transpose);
        return updateCase;
    }

    /**
     * Creates a test case with an ill-conditioned 5x5 matrix where the diagonal elements are very close to
     * each other (differing by small amounts). This test case is designed to verify that the update algorithm
     * maintains numerical stability when dealing with matrices that are close to being singular.
     */
    static UpdateCase makeIllConditionedMatrix() {

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
        final MatrixStore<Double> originalMatrix = matrix;
        final MatrixStore<Double> newColumn1 = newColumn;

        // Test column update on an ill-conditioned matrix
        return new UpdateCase(originalMatrix, 2, newColumn1);
    }

    /**
     * Creates a minimal test scenario based on the Afiro model, using a 5x5 identity matrix as the starting
     * point. The sequence includes two updates with sparse columns that mimic the structure of updates in the
     * full Afiro model. This test case is useful for verifying basic update functionality in a controlled,
     * simplified environment.
     */
    static UpdateSequence makeMinimalAfiroUpdateScenario() {

        // Start with a 5x5 identity matrix
        org.ojalgo.matrix.store.R064Store matrix = R064Store.FACTORY.makeEye(5, 5);

        // First update: column 2 with a sparse column (mimics newColumn1 in makeUpdateAfiro)
        org.ojalgo.matrix.store.R064Store col2 = R064Store.FACTORY.make(5, 1);
        col2.set(1, 0, 1.0);
        col2.set(3, 0, 0.5);
        col2.set(4, 0, -0.2);

        // Second update: column 3 with another sparse column (mimics newColumn2 in makeUpdateAfiro)
        org.ojalgo.matrix.store.R064Store col3 = R064Store.FACTORY.make(5, 1);
        col3.set(2, 0, -1.0);
        col3.set(4, 0, 1.0);

        UpdateSequence sequence = new UpdateSequence(matrix);

        sequence.add(2, col2);
        sequence.add(3, col3);

        return sequence;
    }

    /**
     * Creates a test sequence with a 7x7 random matrix and updates for each column. The random nature of this
     * test case helps verify that the update algorithm works correctly across a wide range of matrix
     * structures and values. Each column is updated with a new random column vector.
     */
    static UpdateSequence makeRandomMatrixUpdate() {

        int dim = 7;

        // Create random square matrix
        R064Store matrix = DecompositionUpdateTest.newRandom(dim, dim);
        UpdateSequence retVal = new UpdateSequence(matrix);

        // Test updating each column
        for (int col = 0; col < dim; col++) {
            R064Store newColumn = DecompositionUpdateTest.newRandom(dim, 1);
            retVal.add(col, newColumn);
        }

        return retVal;
    }

    /**
     * Creates a test sequence with a 4x4 matrix and a series of updates designed to force pivoting. Each
     * update column contains a large value in a different position, ensuring that the update algorithm must
     * perform pivoting to maintain numerical stability. This test case verifies that the algorithm correctly
     * handles multiple updates that require pivoting.
     */
    static UpdateSequence makeRepeatedUpdatesWithPivoting() {

        // Create initial 4x4 matrix
        R064Store matrix = R064Store.FACTORY.make(4, 4);
        matrix.fillAll(ONE);
        matrix.set(0, 0, 2.0);
        matrix.set(1, 1, 3.0);
        matrix.set(2, 2, 4.0);
        matrix.set(3, 3, 5.0);

        UpdateSequence sequence = new UpdateSequence(matrix);

        // Create new columns to update
        R064Store newColumn1 = R064Store.FACTORY.make(4, 1);
        newColumn1.fillAll(ONE);
        newColumn1.set(0, 0, 6.0); // Large value to force pivot

        R064Store newColumn2 = R064Store.FACTORY.make(4, 1);
        newColumn2.fillAll(ONE);
        newColumn2.set(1, 0, 7.0); // Large value to force pivot

        R064Store newColumn3 = R064Store.FACTORY.make(4, 1);
        newColumn3.fillAll(ONE);
        newColumn3.set(2, 0, 8.0); // Large value to force pivot

        sequence.add(0, newColumn1);
        sequence.add(1, newColumn2);
        sequence.add(2, newColumn3);

        return sequence;
    }

    /**
     * Creates a test sequence with a 10x10 random matrix and updates for every other column. This test case
     * verifies that the update algorithm correctly handles sequential updates to a larger matrix, ensuring
     * that each update maintains the correct structure and that the decomposition remains valid throughout
     * the sequence.
     */
    static UpdateSequence makeSequentialUpdates() {

        int dim = 10;

        R064Store matrix = DecompositionUpdateTest.newRandom(dim, dim);
        UpdateSequence retVal = new UpdateSequence(matrix);

        for (int col = 0; col < dim; col += 2) {
            R064Store newColumn = DecompositionUpdateTest.newRandom(dim, 1);
            retVal.add(col, newColumn);
        }

        return retVal;
    }

    /**
     * Creates a test sequence based on the Afiro model, using a 24x24 identity matrix as the starting point.
     * The sequence includes two updates with sparse columns that match the structure of updates in the actual
     * Afiro model. This test case is designed to verify that the update algorithm correctly handles the
     * specific update patterns encountered in real-world linear programming problems.
     */
    static UpdateSequence makeUpdateAfiro() {

        // Create initial 4x4 matrix
        R064Store matrix = R064Store.FACTORY.makeEye(24, 24);

        UpdateSequence sequence = new UpdateSequence(matrix);

        // update 17 { 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.109, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        // 1.0, -0.43, 0.0, 0.0, 0.0, 0.0, 0.0 }

        // Create new columns to update
        MatrixStore<Double> newColumn1 = RawStore
                .wrap(0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.109, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, -0.43, 0.0, 0.0, 0.0, 0.0, 0.0).transpose();

        // update 18 { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        // 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }

        MatrixStore<Double> newColumn2 = RawStore
                .wrap(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0).transpose();

        sequence.add(17, newColumn1);
        sequence.add(18, newColumn2);

        return sequence;
    }

    /**
     * Creates a test sequence based on a modified version of the Afiro model update scenario. This test case
     * is similar to makeUpdateAfiro but with a different sequence of updates, designed to verify that the
     * update algorithm correctly handles alternative update patterns that might occur in the Afiro model.
     */
    static UpdateSequence makeUpdateAfiro2() {

        // Create initial 4x4 matrix
        RawStore matrix = RawStore.wrap(
                new double[][] { { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.109, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.43, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 } });

        UpdateSequence sequence = new UpdateSequence(matrix);

        // update 17 { 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.109, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        // 1.0, -0.43, 0.0, 0.0, 0.0, 0.0, 0.0 }

        // update 18 { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        // 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }

        MatrixStore<Double> newColumn2 = RawStore
                .wrap(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0).transpose();

        sequence.add(18, newColumn2);

        return sequence;
    }

    /**
     * Creates a test case with a 3x3 matrix containing very small diagonal elements (1.0E-10) and an update
     * column with a large value (1.0E+10) to force pivoting. This test case is designed to verify that the
     * update algorithm correctly handles numerical scaling issues and maintains stability through appropriate
     * pivoting.
     */
    static UpdateCase makeUpdatesWithSmallDiagonal() {

        // Create initial 3x3 matrix with small diagonal elements
        R064Store original = R064Store.FACTORY.make(3, 3);
        original.fillAll(ONE);
        original.set(0, 0, 1.0E-10);
        original.set(1, 1, 1.0E-10);
        original.set(2, 2, 1.0E-10);

        // Create new column to update
        R064Store newColumn = R064Store.FACTORY.make(3, 1);
        newColumn.fillAll(ONE);
        newColumn.set(0, 0, 1.0E+10); // Large value to force pivot

        return new UpdateCase(original, 0, newColumn);
    }

    /**
     * Creates a test case with a 3x3 matrix containing a zero on the diagonal and an update column that
     * doesn't fix this singularity. This test case is designed to verify that the update algorithm correctly
     * detects and handles attempts to update a matrix that would remain singular after the update.
     */
    static UpdateCase makeUpdatesWithZeroDiagonal() {

        // Create initial 3x3 matrix with zero diagonal
        R064Store original = R064Store.FACTORY.make(3, 3);
        original.fillAll(ONE);
        original.set(0, 0, ZERO); // Zero diagonal element

        // Create new column to update
        R064Store newColumn = R064Store.FACTORY.make(3, 1);
        newColumn.fillAll(ONE);
        newColumn.set(1, 0, FIVE); // Large value to force pivot

        return new UpdateCase(original, 0, newColumn);
    }

    static MatrixStore<Double> rhs(final int nbRows) {

        PhysicalStore<Double> rhs = R064Store.FACTORY.make(nbRows, 1);
        for (int i = 0; i < nbRows; i++) {
            rhs.set(i, 0, i + ONE);
        }

        return rhs;
    }

    @Test
    public void test3x3NoPivotingOrSpikes() {

        UpdateCase updateCase = DecompositionUpdateTest.make3x3NoPivotingOrSpikes();

        DecompositionUpdateTest.doTest(updateCase);
    }

    /**
     * Test method that demonstrates the high-level Forrest-Tomlin update algorithm.
     * <P>
     * Testing with a case that creates a spike:
     */
    @Test
    public void test4x4withSpikes() {

        UpdateCase updateCase = DecompositionUpdateTest.make4x4withSpikes();

        DecompositionUpdateTest.doTest(updateCase);
    }

    /**
     * Tests a simple column update that doesn't require spike handling. This test uses a carefully
     * constructed 5x5 matrix and updates column 2 with values that maintain the triangular structure without
     * creating spikes.
     */
    @Test
    public void test5x5WithoutSpike() {

        UpdateCase updateCase = DecompositionUpdateTest.make5x5WithoutSpike();

        DecompositionUpdateTest.doTest(updateCase);
    }

    @Test
    public void test7x7WithZeros() {

        List<UpdateCase> updateCases = DecompositionUpdateTest.make7x7WithZeros();

        for (UpdateCase updateCase : updateCases) {
            DecompositionUpdateTest.doTest(updateCase);
        }
    }

    /**
     * This test case is specifically designed to have no spikes according to the Bartels-Golub-Reid
     * algorithm. The matrix and update column are carefully constructed so that after forward substitution,
     * all elements below the diagonal in the column being updated are exactly zero.
     */
    @Test
    public void testBGRNoSpikes() {

        UpdateCase updateCase = DecompositionUpdateTest.makeBGRNoSpikes();

        DecompositionUpdateTest.doTest(updateCase);
    }

    /**
     * This test case is specifically designed to have exactly one spike according to the Bartels-Golub-Reid
     * (BGR) algorithm. The matrix and update column are carefully constructed so that after forward
     * substitution, there is exactly one non-zero element below the diagonal in the column being updated.
     * This test case is useful for verifying the BGR algorithm's spike elimination process.
     */
    @Test
    public void testBGRWithExactlyOneSpike() {

        UpdateCase updateCase = DecompositionUpdateTest.makeBGRWithExactlyOneSpike();

        DecompositionUpdateTest.doTest(updateCase);
    }

    @Test
    public void testCaseGPT() {

        UpdateCase updateCase = DecompositionUpdateTest.makeCaseGPT();

        DecompositionUpdateTest.doTest(updateCase);
    }

    /**
     * Tests updateColumn with ill-conditioned matrices.
     */
    @Test
    public void testIllConditionedMatrix() {

        UpdateCase updateCase = DecompositionUpdateTest.makeIllConditionedMatrix();

        DecompositionUpdateTest.doTest(updateCase);
    }

    @Test
    public void testRandomMatrixUpdate() {

        List<UpdateCase> updateCases = DecompositionUpdateTest.makeRandomMatrixUpdate().toIndividualCases();

        for (UpdateCase updateCase : updateCases) {
            DecompositionUpdateTest.doTest(updateCase);
        }
    }

    /**
     * Tests sequential updates of multiple columns.
     */
    @Test
    public void testSequentialUpdates() {

        List<UpdateCase> updateCases = DecompositionUpdateTest.makeSequentialUpdates().toIndividualCases();

        for (UpdateCase updateCase : updateCases) {
            DecompositionUpdateTest.doTest(updateCase);
        }
    }

}
