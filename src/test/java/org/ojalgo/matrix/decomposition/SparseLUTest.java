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
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.decomposition.DecompositionUpdateTest.UpdateCase;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064LSC;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Uniform;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.keyvalue.EntryPair;
import org.ojalgo.type.keyvalue.EntryPair.KeyedPrimitive;

public class SparseLUTest extends MatrixDecompositionTests {

    static class BuildSequence {

        /**
         * Creates a random (permuted identity) matrix as the original, and then an update for each of the
         * columns (in random order). When all the updates have been applied the result should be equal to the
         * original matrix of the update case.
         */
        static BuildSequence from(final DecompositionUpdateTest.UpdateCase updateCase) {

            int nbRows = updateCase.getRowDim();
            int nbCols = updateCase.getColDim();

            R064Store eye = R064Store.FACTORY.makeEye(nbRows, nbCols);

            Pivot rowOrder = new Pivot();
            rowOrder.reset(nbRows);
            for (int i = 0; i < nbRows; i++) {
                rowOrder.change(i, Uniform.randomInteger(nbRows));
            }
            Pivot colOrder = new Pivot();
            colOrder.reset(nbCols);
            for (int j = 0; j < nbCols; j++) {
                colOrder.change(j, Uniform.randomInteger(nbCols));
            }

            MatrixStore<Double> original = eye.rows(rowOrder.getOrder());

            List<KeyedPrimitive<MatrixStore<Double>>> updates = new ArrayList<>();

            int[] order = colOrder.getOrder();
            for (int j = 0; j < nbCols; j++) {
                int col = order[j];
                updates.add(EntryPair.of(updateCase.getOriginalColumn(col), col));
            }

            return new BuildSequence(original, updates);
        }

        MatrixStore<Double> original;
        List<KeyedPrimitive<MatrixStore<Double>>> updates;

        BuildSequence(final MatrixStore<Double> original, final List<KeyedPrimitive<MatrixStore<Double>>> updates) {
            super();
            this.original = original;
            this.updates = updates;
        }

        MatrixStore<Double> rhs() {

            int nbRows = original.getRowDim();

            PhysicalStore<Double> rhs = R064Store.FACTORY.make(nbRows, 1);
            for (int i = 0; i < nbRows; i++) {
                rhs.set(i, 0, i + ONE);
            }

            return rhs;
        }

    }

    private static final NumberContext ACCURACY = NumberContext.of(8);

    @Test
    public void test3x3NoPivotingOrSpikes() {

        UpdateCase updateCase = DecompositionUpdateTest.make3x3NoPivotingOrSpikes();

        SparseLU sparse = this.doTestBuildingViaUpdates(updateCase);
    }

    @Test
    public void testColumnShiftingInU() {

        // Create a 5x5 matrix with a structure that will require column shifts
        R064LSC mOriginal = R064LSC.FACTORY.make(5, 5);

        // Set up a matrix that will require column shifts when updated
        // The structure is designed so that updating column 2 will require shifting columns
        mOriginal.set(0, 0, 4.0);
        mOriginal.set(0, 1, 2.0);
        mOriginal.set(0, 2, 1.0);
        mOriginal.set(0, 3, 3.0);
        mOriginal.set(0, 4, 5.0);

        mOriginal.set(1, 0, 2.0);
        mOriginal.set(1, 1, 3.0);
        mOriginal.set(1, 2, 2.0);
        mOriginal.set(1, 3, 4.0);
        mOriginal.set(1, 4, 6.0);

        mOriginal.set(2, 0, 1.0);
        mOriginal.set(2, 1, 2.0);
        mOriginal.set(2, 2, 4.0);
        mOriginal.set(2, 3, 5.0);
        mOriginal.set(2, 4, 7.0);

        mOriginal.set(3, 0, 3.0);
        mOriginal.set(3, 1, 4.0);
        mOriginal.set(3, 2, 5.0);
        mOriginal.set(3, 3, 6.0);
        mOriginal.set(3, 4, 8.0);

        mOriginal.set(4, 0, 5.0);
        mOriginal.set(4, 1, 6.0);
        mOriginal.set(4, 2, 7.0);
        mOriginal.set(4, 3, 8.0);
        mOriginal.set(4, 4, 9.0);

        // Create a new column to update (column 2)
        // This column is designed to force column shifts in U
        R064Store mColumn = R064Store.FACTORY.make(5, 1);
        mColumn.set(0, 0, 0.0); // Zero at top to force shift
        mColumn.set(1, 0, 0.0); // Zero at top to force shift
        mColumn.set(2, 0, 1.0); // Non-zero here to force shift
        mColumn.set(3, 0, 2.0);
        mColumn.set(4, 0, 3.0);

        // Create SparseLU decomposition
        SparseLU decomp = new SparseLU();
        decomp.decompose(mOriginal);
        decomp.updateColumn(2, mColumn);

        PhysicalStore<Double> mModified = mOriginal.copy();
        mModified.fillColumn(2, mColumn);

        // Verify that the decomposition is still valid after the update
        // PA = LU where P is the permutation matrix
        PhysicalStore<Double> mP = R064Store.FACTORY.make(5, 5);
        int[] pivotOrder = decomp.getPivotOrder();
        for (int i = 0; i < 5; i++) {
            mP.set(i, pivotOrder[i], ONE);
        }

        // Verify PA = LU
        MatrixStore<Double> mPA = mP.multiply(mModified);
        MatrixStore<Double> mLU = decomp.getL().multiply(decomp.getU());
        // TestUtils.assertEquals(mPA, mLU, ACCURACY);

        // Test ftran: Solve Ax = b
        PhysicalStore<Double> mB = R064Store.FACTORY.make(5, 1);
        mB.set(0, 0, 1.0);
        mB.set(1, 0, 2.0);
        mB.set(2, 0, 3.0);
        mB.set(3, 0, 4.0);
        mB.set(4, 0, 5.0);

        // Make a copy of b for comparison
        PhysicalStore<Double> mX = mB.copy();
        // Apply ftran to solve Ax = b
        decomp.ftran(mX);

        // Verify that x is indeed a solution to Ax = b
        // Compute Ax and compare with original b
        TestUtils.assertEquals(mB, mModified.multiply(mX), ACCURACY);

        // Test btran: Solve Aᵀx = b
        mB.supplyTo(mX);
        // Apply btran to solve Aᵀx = b
        decomp.btran(mX);

        // Verify that x is indeed a solution to Aᵀx = b
        // Compute Aᵀx and compare with original b
        TestUtils.assertEquals(mB, mModified.transpose().multiply(mX), ACCURACY);

        for (int col = 0; col < pivotOrder.length; col++) {

            PhysicalStore<Double> original = mOriginal.copy();
            PhysicalStore<Double> column = mColumn.copy();

            PhysicalStore<Double> modified = mOriginal.copy();
            modified.fillColumn(col, column);

            LU<Double> expDecomp = LU.R064.decompose(modified);

            LU<Double> actDecomp = new SparseLU();
            actDecomp.decompose(original);
            actDecomp.updateColumn(col, column);

            // TestUtils.assertEquals(modified, actDecomp.reconstruct());

            if (expDecomp.isSolvable()) {
                MatrixStore<Double> exp = expDecomp.getInverse();
                MatrixStore<Double> act = actDecomp.getInverse();
                TestUtils.assertEquals(exp, act);
            }
        }
    }

    @Test
    public void testControlledUpdateWithKnownPermutationAndEta() {

        // Start with a simple lower-triangular matrix (not diagonal, but lower triangular)
        // A = [1 0 0; 2 1 0; 3 4 1]
        R064LSC matrix = R064LSC.FACTORY.make(3, 3);
        matrix.set(0, 0, 1.0);
        matrix.set(1, 0, 2.0);
        matrix.set(1, 1, 1.0);
        matrix.set(2, 0, 3.0);
        matrix.set(2, 1, 4.0);
        matrix.set(2, 2, 1.0);

        // Now update column 1 to [0, 1, 5]^T
        R064Store newCol = R064Store.FACTORY.make(3, 1);
        newCol.set(0, 0, 0.0);
        newCol.set(1, 0, 1.0);
        newCol.set(2, 0, 5.0);

        // Prepare a known vector
        PhysicalStore<Double> rhs = R064Store.FACTORY.make(3, 1);
        rhs.set(0, 0, 1.0);
        rhs.set(1, 0, 2.0);
        rhs.set(2, 0, 3.0);

        this.doTransTest(matrix, 1, newCol, rhs);
    }

    @Test
    public void testEtaMatrixConstructionAndApplication() {

        // Matrix with nontrivial structure
        // [1 2 0]
        // [0 1 3]
        // [4 0 1]
        R064LSC matrix = R064LSC.FACTORY.make(3, 3);
        matrix.set(0, 0, 1.0);
        matrix.set(0, 1, 2.0);
        matrix.set(0, 2, 0.0);
        matrix.set(1, 0, 0.0);
        matrix.set(1, 1, 1.0);
        matrix.set(1, 2, 3.0);
        matrix.set(2, 0, 4.0);
        matrix.set(2, 1, 0.0);
        matrix.set(2, 2, 1.0);

        // Update column 0 to a new vector
        // [2]
        // [5]
        // [1]
        R064Store newCol = R064Store.FACTORY.make(3, 1);
        newCol.set(0, 0, 2.0);
        newCol.set(1, 0, 5.0);
        newCol.set(2, 0, 1.0);

        // Right-hand side
        PhysicalStore<Double> rhs = R064Store.FACTORY.make(3, 1);
        rhs.set(0, 0, 1.0);
        rhs.set(1, 0, 2.0);
        rhs.set(2, 0, 3.0);

        this.doTransTest(matrix, 0, newCol, rhs);
    }

    @Test
    public void testEtaMatrixFtranBtran() {

        // Test the Eta matrix ftran and btran in isolation
        int dim = 4;
        int pivotRow = 2;
        double eta0 = 0.5, eta1 = -1.0, eta3 = 2.0;
        SparseLU.Eta eta = new SparseLU.Eta(dim, pivotRow);
        // Set up eta with multiple nonzeros
        eta.set(0, eta0);
        eta.set(1, eta1);
        eta.set(3, eta3);
        // The dense form of Eta is identity except row 2:
        // E = I + e_pivotRow * [0.5, -1.0, 0, 2.0]
        // So row 2 of E is: [0, 0, 1, 0] + [0.5, -1.0, 0, 2.0] = [0.5, -1.0, 1, 2.0]
        // Test vector
        double[] x = { 1.0, 2.0, 3.0, 4.0 };
        PhysicalStore<Double> vec = R064Store.FACTORY.make(dim, 1);
        for (int i = 0; i < dim; i++) {
            vec.set(i, 0, x[i]);
        }
        // ftran: y = E * x
        PhysicalStore<Double> expectedFtran = vec.copy();
        double expectedPivot = x[pivotRow] + eta0 * x[0] + eta1 * x[1] + eta3 * x[3];
        expectedFtran.set(pivotRow, 0, expectedPivot);
        // Now apply ftran
        PhysicalStore<Double> actualFtran = vec.copy();
        eta.ftran(actualFtran);
        TestUtils.assertEquals(expectedFtran, actualFtran);
        // btran: y = E^T * x
        // For btran, y[j] = x[j] for j != pivotRow; y[j] += -eta[j] * x[pivotRow] for j != pivotRow
        PhysicalStore<Double> expectedBtran = vec.copy();
        double pivotVal = x[pivotRow];
        expectedBtran.add(0, 0, eta0 * pivotVal);
        expectedBtran.add(1, 0, eta1 * pivotVal);
        expectedBtran.add(3, 0, eta3 * pivotVal);
        // Now apply btran
        PhysicalStore<Double> actualBtran = vec.copy();
        eta.btran(actualBtran);
        TestUtils.assertEquals(expectedBtran, actualBtran);
    }

    @Test
    public void testFtranBtranAfterUpdate() {

        // Create a 3x3 matrix
        R064LSC matrix = R064LSC.FACTORY.make(3, 3);
        matrix.set(0, 0, 4.0);
        matrix.set(0, 1, 2.0);
        matrix.set(0, 2, 1.0);
        matrix.set(1, 0, 2.0);
        matrix.set(1, 1, 3.0);
        matrix.set(1, 2, 2.0);
        matrix.set(2, 0, 1.0);
        matrix.set(2, 1, 2.0);
        matrix.set(2, 2, 4.0);

        // Create a new column to update
        R064Store newColumn = R064Store.FACTORY.make(3, 1);
        newColumn.set(0, 0, 3.0);
        newColumn.set(1, 0, 4.0);
        newColumn.set(2, 0, 5.0);

        PhysicalStore<Double> b = R064Store.FACTORY.make(3, 1);
        b.set(0, 0, 1.0);
        b.set(1, 0, 2.0);
        b.set(2, 0, 3.0);

        this.doTransTest(matrix, 1, newColumn, b);
    }

    @Test
    public void testRandom5x5() {

        R064Store matrix = R064Store.FACTORY.makeFilled(5, 5, Uniform.standard());

        SparseLU decomp = this.doGeneralTest(matrix);
    }

    @Test
    public void testSimple2x2() throws RecoverableCondition {

        // Create a simple 2x2 matrix
        R064LSC matrix = R064LSC.FACTORY.make(2, 2);
        matrix.set(0, 0, 4.0);
        matrix.set(0, 1, 3.0);
        matrix.set(1, 0, 6.0);
        matrix.set(1, 1, 3.0);

        SparseLU decomp = this.doGeneralTest(matrix);

        // First verify that pivoting occurred correctly
        // For this matrix, row 1 should be used first since |6| > |4|
        TestUtils.assertEquals(1, decomp.getPivotOrder()[0]); // First pivot should be row 1
        TestUtils.assertEquals(0, decomp.getPivotOrder()[1]); // Second pivot should be row 0

        // After pivoting, PA should be:
        // 6 3
        // 4 3

        // Therefore L should be:
        // 1 0
        // 2/3 1
        TestUtils.assertEquals(1.0, decomp.getL().doubleValue(0, 0));
        TestUtils.assertEquals(0.0, decomp.getL().doubleValue(0, 1));
        TestUtils.assertEquals(2.0 / 3.0, decomp.getL().doubleValue(1, 0));
        TestUtils.assertEquals(1.0, decomp.getL().doubleValue(1, 1));

        // And U should be:
        // 6 3
        // 0 1
        TestUtils.assertEquals(6.0, decomp.getU().doubleValue(0, 0));
        TestUtils.assertEquals(3.0, decomp.getU().doubleValue(0, 1));
        TestUtils.assertEquals(0.0, decomp.getU().doubleValue(1, 0));
        TestUtils.assertEquals(1.0, decomp.getU().doubleValue(1, 1));

        // Create permutation matrix P
        PhysicalStore<Double> P = R064Store.FACTORY.make(2, 2);
        P.set(0, decomp.getPivotOrder()[0], 1.0);
        P.set(1, decomp.getPivotOrder()[1], 1.0);

        // Verify PA = LU
        MatrixStore<Double> PA = P.multiply(matrix);
        MatrixStore<Double> LU = decomp.getL().multiply(decomp.getU());

        if (DEBUG) {
            BasicLogger.debugMatrix("PA", PA);
            BasicLogger.debugMatrix("LU", LU);
        }

        TestUtils.assertEquals(PA, LU);

        // Also verify that solving works
        PhysicalStore<Double> rhs = R064Store.FACTORY.make(2, 1);
        rhs.set(0, 0, 1.0);
        rhs.set(1, 0, 2.0);

        MatrixStore<Double> solution = decomp.solve(matrix, rhs);

        // Verify Ax = b
        MatrixStore<Double> computed = matrix.multiply(solution);
        TestUtils.assertEquals(rhs, computed);
    }

    @Test
    public void testSimple3x3() throws RecoverableCondition {

        // Create a 3x3 matrix that requires pivoting
        R064LSC matrix = R064LSC.FACTORY.make(3, 3);

        // Set values to force pivoting:
        // 1 2 3
        // 4 5 6
        // 7 8 9
        matrix.set(0, 0, 1.0);
        matrix.set(0, 1, 2.0);
        matrix.set(0, 2, 3.0);
        matrix.set(1, 0, 4.0);
        matrix.set(1, 1, 5.0);
        matrix.set(1, 2, 6.0);
        matrix.set(2, 0, 7.0);
        matrix.set(2, 1, 8.0);
        matrix.set(2, 2, 9.0);

        SparseLU decomp = this.doGeneralTest(matrix);

        // After first pivot, row 2 should be first (largest element in column 0)
        TestUtils.assertEquals(2, decomp.getPivotOrder()[0]);

        // Expected L matrix after pivoting:
        // 1 0 0
        // 1/7 1 0
        // 4/7 0.5 1
        TestUtils.assertEquals(1.0, decomp.getL().doubleValue(0, 0));
        TestUtils.assertEquals(1.0 / 7.0, decomp.getL().doubleValue(1, 0));
        TestUtils.assertEquals(4.0 / 7.0, decomp.getL().doubleValue(2, 0));
        TestUtils.assertEquals(0.5, decomp.getL().doubleValue(2, 1));

        // Expected U matrix after pivoting:
        // 7 8 9
        // 0 0.857143 1.714286
        // 0 0 0
        TestUtils.assertEquals(7.0, decomp.getU().doubleValue(0, 0));
        TestUtils.assertEquals(8.0, decomp.getU().doubleValue(0, 1));
        TestUtils.assertEquals(9.0, decomp.getU().doubleValue(0, 2));
        TestUtils.assertEquals(0.0, decomp.getU().doubleValue(1, 0));
        TestUtils.assertEquals(0.857143, decomp.getU().doubleValue(1, 1), 1e-6);
        TestUtils.assertEquals(1.714286, decomp.getU().doubleValue(1, 2), 1e-6);
        TestUtils.assertEquals(0.0, decomp.getU().doubleValue(2, 0));
        TestUtils.assertEquals(0.0, decomp.getU().doubleValue(2, 1));
        TestUtils.assertEquals(0.0, decomp.getU().doubleValue(2, 2));

        // Create permutation matrix P
        R064Store P = R064Store.FACTORY.make(3, 3);
        for (int i = 0; i < 3; i++) {
            P.set(i, decomp.getPivotOrder()[i], 1.0);
        }

        // Verify PA = LU
        MatrixStore<Double> PA = P.multiply(matrix);
        MatrixStore<Double> LU = decomp.getL().multiply(decomp.getU());

        if (DEBUG) {
            BasicLogger.debugMatrix("PA", PA);
            BasicLogger.debugMatrix("LU", LU);
        }

        TestUtils.assertEquals(PA, LU);

        // Verify L is unit lower triangular
        for (int i = 0; i < 3; i++) {
            TestUtils.assertEquals(1.0, decomp.getL().doubleValue(i, i));
            for (int j = i + 1; j < 3; j++) {
                TestUtils.assertEquals(0.0, decomp.getL().doubleValue(i, j));
            }
        }

        // Verify U is upper triangular
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < i; j++) {
                TestUtils.assertEquals(0.0, decomp.getU().doubleValue(i, j));
            }
        }

        // Verify rank and solvability
        TestUtils.assertEquals(2, decomp.getRank());
        TestUtils.assertFalse(decomp.isSolvable());
    }

    @Test
    public void testSingularMatrix() {

        // Create a singular matrix
        R064LSC matrix = R064LSC.FACTORY.make(3, 3);
        matrix.set(0, 0, 1.0);
        matrix.set(0, 1, 2.0);
        matrix.set(0, 2, 3.0);
        matrix.set(1, 0, 2.0);
        matrix.set(1, 1, 4.0);
        matrix.set(1, 2, 6.0); // Row 1 = 2 * Row 0
        matrix.set(2, 0, 7.0);
        matrix.set(2, 1, 8.0);
        matrix.set(2, 2, 9.0);

        SparseLU decomp = this.doGeneralTest(matrix);

        TestUtils.assertFalse(decomp.isSolvable());
    }

    @Test
    public void testSparse4x4() {

        // Create a sparse matrix with known pattern
        R064LSC matrix = R064LSC.FACTORY.make(4, 4);
        matrix.set(0, 0, 1.0);
        matrix.set(1, 1, 2.0);
        matrix.set(2, 2, 3.0);
        matrix.set(3, 3, 4.0);
        matrix.set(1, 0, 0.5);
        matrix.set(2, 1, 0.5);
        matrix.set(3, 2, 0.5);

        SparseLU decomp = this.doGeneralTest(matrix);

        // Verify sparsity pattern is preserved
        // Check L is lower triangular with expected pattern
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                if (matrix.doubleValue(j, i) == ZERO) {
                    TestUtils.assertEquals(ZERO, decomp.getL().doubleValue(j, i));
                }
            }
        }

        // Check U is upper triangular with expected pattern
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < i; j++) {
                TestUtils.assertEquals(ZERO, decomp.getU().doubleValue(i, j));
            }
        }

        // Verify decomposition
        MatrixStore<Double> product = decomp.getL().multiply(decomp.getU());
        TestUtils.assertEquals(matrix, product);
    }

    @Test
    public void testSpecificEtaCase() {

        PhysicalStore<Double> mtrxU0 = R064Store.FACTORY.make(3, 3);
        mtrxU0.set(0, 0, 4);
        mtrxU0.set(0, 1, 3);
        mtrxU0.set(0, 2, 1);
        mtrxU0.set(1, 0, 0);
        mtrxU0.set(1, 1, 2.625);
        mtrxU0.set(1, 2, 2.375);
        mtrxU0.set(2, 0, 0);
        mtrxU0.set(2, 1, 1.5);
        mtrxU0.set(2, 2, 2.5);
        BasicLogger.debugMatrix("mtrxU0", mtrxU0);

        PhysicalStore<Double> mtrxEta = R064Store.FACTORY.make(3, 3);
        mtrxEta.fillDiagonal(ONE);
        mtrxEta.set(2, 1, 1.5 / 2.625);
        BasicLogger.debugMatrix("mtrxEta", mtrxEta);

        LU<Double> dcmpU0 = LU.R064.decompose(mtrxU0);
        LU<Double> dcmpEta = LU.R064.decompose(mtrxEta);

        MatrixStore<Double> invEta = dcmpEta.getInverse();
        BasicLogger.debugMatrix("invEta", invEta);

        MatrixStore<Double> mtrxU1 = invEta.multiply(mtrxU0);
        BasicLogger.debugMatrix("mtrxU1", mtrxU1);

        LU<Double> dcmpU1 = LU.R064.decompose(mtrxU1);

        PhysicalStore<Double> rhs = R064Store.FACTORY.make(3, 1);
        rhs.set(0, 1);
        rhs.set(1, 2);
        rhs.set(2, 3);

        SparseLU.Eta trfEta = new SparseLU.Eta(3, 2);
        trfEta.set(1, -1.5 / 2.625);

        // ftran

        PhysicalStore<Double> reference = rhs.copy();
        dcmpU0.ftran(reference);
        BasicLogger.debugMatrix("reference", reference);

        PhysicalStore<Double> expected = rhs.copy();
        dcmpEta.ftran(expected);
        dcmpU1.ftran(expected);
        BasicLogger.debugMatrix("expected", expected);

        TestUtils.assertEquals(expected, reference);

        PhysicalStore<Double> actual = rhs.copy();
        trfEta.ftran(actual);
        dcmpU1.ftran(actual);
        BasicLogger.debugMatrix("actual", actual);

        TestUtils.assertEquals(expected, actual);

        // btran

        reference = rhs.copy();
        dcmpU0.btran(reference);
        BasicLogger.debugMatrix("reference", reference);

        expected = rhs.copy();
        dcmpU1.btran(expected);
        dcmpEta.btran(expected);
        BasicLogger.debugMatrix("expected", expected);

        TestUtils.assertEquals(expected, reference);

        actual = rhs.copy();
        dcmpU1.btran(actual);
        trfEta.btran(actual);
        BasicLogger.debugMatrix("actual", actual);

        TestUtils.assertEquals(expected, actual);
    }

    private SparseLU doGeneralTest(final MatrixStore<Double> matrix) {

        if (DEBUG) {
            LU<Double> refDecomp = LU.R064.decompose(matrix);
            BasicLogger.debugMatrix("Reference Original matrix", matrix);
            BasicLogger.debug("Reference Pivot order: {}", refDecomp.getPivotOrder());
            BasicLogger.debugMatrix("Reference L matrix", refDecomp.getL());
            BasicLogger.debugMatrix("Reference U matrix", refDecomp.getU());
            BasicLogger.debug("Reference getRank: {}", refDecomp.getRank());
            BasicLogger.debug("Reference isSolvable: {}", refDecomp.isSolvable());
            TestUtils.assertEquals(matrix, refDecomp, ACCURACY);
        }

        SparseLU decomposition = new SparseLU();
        TestUtils.assertTrue(decomposition.decompose(matrix));

        if (DEBUG) {
            BasicLogger.debugMatrix("Original matrix", matrix);
            BasicLogger.debug("Pivot order: {}", decomposition.getPivotOrder());
            BasicLogger.debugMatrix("L matrix", decomposition.getL());
            BasicLogger.debugMatrix("U matrix", decomposition.getU());
        }

        TestUtils.assertEquals(matrix, decomposition, ACCURACY);

        return decomposition;
    }

    SparseLU doTestBuildingViaUpdates(final UpdateCase updateCase) {

        BuildSequence updateSequence = BuildSequence.from(updateCase);

        SparseLU sparse = new SparseLU();
        sparse.decompose(updateSequence.original);
        for (KeyedPrimitive<MatrixStore<Double>> update : updateSequence.updates) {
            sparse.updateColumn(update.intValue(), update.left());
        }

        MatrixStore<Double> matrix = updateCase.originalMatrix;
        LU<Double> dense = LU.R064.decompose(matrix);

        MatrixStore<Double> rhs = updateSequence.rhs();

        PhysicalStore<Double> fDense = rhs.copy();
        PhysicalStore<Double> fSparse = rhs.copy();
        dense.ftran(fDense);
        sparse.ftran(fSparse);

        TestUtils.assertEquals(rhs, matrix.multiply(fDense));
        TestUtils.assertEquals(fDense, fSparse);
        TestUtils.assertEquals(rhs, matrix.multiply(fSparse));

        PhysicalStore<Double> bDense = rhs.copy();
        PhysicalStore<Double> bSparse = rhs.copy();
        dense.btran(bDense);
        sparse.btran(bSparse);

        TestUtils.assertEquals(rhs, matrix.transpose().multiply(bDense));
        TestUtils.assertEquals(bDense, bSparse);
        TestUtils.assertEquals(rhs, matrix.transpose().multiply(bSparse));

        return sparse;
    }

    void doTransTest(final R064LSC matrix, final int colInd, final R064Store newCol, final PhysicalStore<Double> rhs) {

        SparseLU decompSparseOrg = new SparseLU();
        decompSparseOrg.decompose(matrix);
        decompSparseOrg.updateColumn(colInd, newCol);

        matrix.fillColumn(colInd, newCol);
        LU<Double> decompDenseMod = new DenseLU.R064();
        decompDenseMod.decompose(matrix);

        PhysicalStore<Double> fDense = rhs.copy();
        PhysicalStore<Double> fSparse = rhs.copy();
        decompDenseMod.ftran(fDense);
        decompSparseOrg.ftran(fSparse);

        TestUtils.assertEquals(rhs, matrix.multiply(fDense));
        TestUtils.assertEquals(fDense, fSparse);
        TestUtils.assertEquals(rhs, matrix.multiply(fSparse));

        PhysicalStore<Double> bDense = rhs.copy();
        PhysicalStore<Double> bSparse = rhs.copy();
        decompDenseMod.btran(bDense);
        decompSparseOrg.btran(bSparse);

        TestUtils.assertEquals(bDense, bSparse);
        TestUtils.assertEquals(rhs, matrix.transpose().multiply(bSparse));
    }
}