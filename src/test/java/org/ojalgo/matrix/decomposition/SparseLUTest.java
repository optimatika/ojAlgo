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

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import org.junit.jupiter.api.Test;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064LSC;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Uniform;
import org.ojalgo.type.context.NumberContext;

public class SparseLUTest extends MatrixDecompositionTests {

    private static final NumberContext ACCURACY = NumberContext.of(8);

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
}