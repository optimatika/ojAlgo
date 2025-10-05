package org.ojalgo.matrix.task.iterative;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.Random;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.type.context.NumberContext;

public class ConjugateGradientSolverTest extends TaskIterativeTests {

    static final NumberContext ACCURACY = NumberContext.of(8);

    static void doTestAllPreconditioners(final RawStore body, final R064Store expected, final R064Store rhs) {

        for (Supplier<Preconditioner> factory : PRECONDITIONERS) {
            Preconditioner preconditioner = factory.get();
            String name = preconditioner.getClass().getSimpleName();
            if (DEBUG) {
                BasicLogger.debug("Testing " + name);
            }

            ConjugateGradientSolver solver = new ConjugateGradientSolver();
            solver.configurator().iterations(999).preconditioner(preconditioner).relaxation(ONE);
            if (DEBUG) {
                solver.configurator().debug(BasicLogger.DEBUG);
            }

            MatrixStore<Double> actual = solver.solve(body, rhs).get();
            TestUtils.assertEquals(name, expected, actual, ACCURACY);
        }
    }

    @Test
    public void testAllPreconditioneraOnDim5() {

        RawStore body = RawStore.wrap(new double[][] { { 4, 1, 0, 0, 0 }, { 1, 5, 1, 0, 0 }, { 0, 1, 6, 1, 0 }, { 0, 0, 1, 7, 1 }, { 0, 0, 0, 1, 8 } });
        R064Store expected = R064Store.FACTORY.column(1, 2, 3, 4, 5);
        R064Store rhs = TaskIterativeTests.rhs(body, expected);
        ConjugateGradientSolverTest.doTestAllPreconditioners(body, expected, rhs);
    }

    @Test
    public void testBlockDiagonalScaled() {
        // Block diagonal SPD with very different scaling between blocks
        // B1 (3x3)
        double[][] B1 = { { 4, 1, 0 }, { 1, 3, 1 }, { 0, 1, 2 } };
        // B2 (4x4) tri-diagonal, then scale by factor
        double scale = 1e3; // moderate to still converge
        int m2 = 4;
        double[][] B2 = new double[m2][m2];
        for (int i = 0; i < m2; i++) {
            B2[i][i] = 2.0 * scale;
            if (i > 0) {
                B2[i][i - 1] = NEG * scale;
            }
            if (i < m2 - 1) {
                B2[i][i + 1] = NEG * scale;
            }
        }
        int n = 3 + m2;
        double[][] A = new double[n][n];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                A[i][j] = B1[i][j];
            }
        }
        for (int i = 0; i < m2; i++) {
            for (int j = 0; j < m2; j++) {
                A[3 + i][3 + j] = B2[i][j];
            }
        }
        RawStore body = RawStore.wrap(A);
        double[] xVals = new double[n];
        for (int i = 0; i < n; i++) {
            xVals[i] = i + 1; // 1..n
        }
        R064Store expected = R064Store.FACTORY.column(xVals);
        R064Store rhs = TaskIterativeTests.rhs(body, expected);
        if (DEBUG) {
            BasicLogger.debug("Block-diagonal scaled SPD (3+4) test");
        }
        // Needs a few more iterations for some preconditioners to fully balance blocks
        ConjugateGradientSolverTest.doTestAllPreconditioners(body, expected, rhs);
    }

    @Test
    public void testDiagonalScaledSPD5x5() {

        // Construct a diagonally scaled SPD matrix with varying magnitudes to exercise numerical stability
        double[] diag = { ONE, 10.0, 100.0, 1_000.0, 10_000.0 };
        int n = diag.length;
        double[][] A = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = i == j ? diag[i] : (Math.abs(i - j) == 1 ? 0.01 * Math.min(diag[i], diag[j]) : ZERO);
            }
        }
        RawStore body = RawStore.wrap(A);
        R064Store expected = R064Store.FACTORY.column(1, 2, 3, 4, 5);
        R064Store rhs = TaskIterativeTests.rhs(body, expected);
        if (DEBUG) {
            BasicLogger.debug("Diagonal scaled SPD 5x5 test");
        }
        ConjugateGradientSolverTest.doTestAllPreconditioners(body, expected, rhs);
    }

    @Test
    public void testIllConditionedSpectrum6x6() {
        // Construct SPD A = Q * D * Q^T with eigenvalues spanning 1e-2 .. 1 (cond ~ 1e2)
        int n = 6;
        double[] evals = new double[n];
        for (int i = 0; i < n; i++) {
            evals[i] = Math.pow(10.0, -2.0 + 2.0 * i / (n - 1)); // log-spaced 1e-2..1
        }
        // Random matrix then simple Gram-Schmidt to form Q
        Random rnd = new Random(123456789L);
        double[][] Q = new double[n][n];
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                Q[i][j] = rnd.nextDouble() - 0.5;
            }
            // orthogonalise against previous columns
            for (int k = 0; k < j; k++) {
                double dot = ZERO;
                for (int i = 0; i < n; i++) {
                    dot += Q[i][j] * Q[i][k];
                }
                for (int i = 0; i < n; i++) {
                    Q[i][j] -= dot * Q[i][k];
                }
            }
            // normalise
            double norm = ZERO;
            for (int i = 0; i < n; i++) {
                norm = Math.hypot(norm, Q[i][j]);
            }
            for (int i = 0; i < n; i++) {
                Q[i][j] /= norm;
            }
        }
        double[][] A = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double sum = ZERO;
                for (int k = 0; k < n; k++) {
                    sum += Q[i][k] * evals[k] * Q[j][k];
                }
                A[i][j] = sum;
            }
        }
        RawStore body = RawStore.wrap(A);
        double[] xVals = new double[n];
        for (int i = 0; i < n; i++) {
            xVals[i] = (i + 1) * 0.5; // scaled increasing
        }
        R064Store expected = R064Store.FACTORY.column(xVals);
        R064Store rhs = TaskIterativeTests.rhs(body, expected);
        if (DEBUG) {
            BasicLogger.debug("Ill-conditioned spectrum 6x6 test (cond ~1e2)");
        }
        ConjugateGradientSolverTest.doTestAllPreconditioners(body, expected, rhs);
    }

    @Test
    public void testLogSpacedDiagonal8x8() {
        // Diagonal SPD with log-spaced eigenvalues spanning 1e0..1e6 (condition ~1e6)
        int n = 8;
        double[] diag = new double[n];
        for (int i = 0; i < n; i++) {
            double exp = ZERO + (6.0 - ZERO) * i / (n - 1);
            diag[i] = Math.pow(10.0, exp);
        }
        double[][] A = new double[n][n];
        for (int i = 0; i < n; i++) {
            A[i][i] = diag[i];
        }
        RawStore body = RawStore.wrap(A);
        // Expected x = [1,2,...,n]
        double[] xVals = new double[n];
        for (int i = 0; i < n; i++) {
            xVals[i] = i + 1;
        }
        R064Store expected = R064Store.FACTORY.column(xVals);
        // rhs = A * x
        R064Store rhs = TaskIterativeTests.rhs(body, expected);
        if (DEBUG) {
            BasicLogger.debug("Log-spaced diagonal 8x8 test (cond ~1e6)");
        }
        // High condition number – permit more iterations
        ConjugateGradientSolverTest.doTestAllPreconditioners(body, expected, rhs);
    }

    @Test
    public void testPoisson1DLength12() {
        // 1D Poisson/Laplacian operator (Dirichlet) tri-diagonal SPD size n
        int n = 12; // condition ~ O(n^2) ~ 144 manageable for few iterations
        double[][] A = new double[n][n];
        for (int i = 0; i < n; i++) {
            A[i][i] = 2.0;
            if (i > 0) {
                A[i][i - 1] = NEG;
            }
            if (i < n - 1) {
                A[i][i + 1] = NEG;
            }
        }
        RawStore body = RawStore.wrap(A);
        double[] xVals = new double[n];
        for (int i = 0; i < n; i++) {
            xVals[i] = i + 1; // monotone increasing solution
        }
        R064Store expected = R064Store.FACTORY.column(xVals);
        R064Store rhs = TaskIterativeTests.rhs(body, expected);
        if (DEBUG) {
            BasicLogger.debug("Poisson 1D n=12 test");
        }
        // Allow up to dimension iterations (CG exact in at most n steps)
        ConjugateGradientSolverTest.doTestAllPreconditioners(body, expected, rhs);
    }

    @Test
    public void testRandomSPD7x7() {

        int n = 7;
        long seed = 20251005L;
        Random rnd = new Random(seed);

        // Build random matrix M then A = M^T M + n * I (guaranteed SPD and reasonably conditioned)
        double[][] M = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                M[i][j] = rnd.nextDouble() - 0.5; // centered about 0 for variety
            }
        }
        double[][] A = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double sum = ZERO;
                for (int k = 0; k < n; k++) {
                    sum += M[k][i] * M[k][j]; // (M^T M)_{ij}
                }
                if (i == j) {
                    sum += n; // strengthen diagonal
                }
                A[i][j] = sum;
            }
        }
        RawStore body = RawStore.wrap(A);

        // Expected solution x = [1,2,3,...,n]^T
        double[] xVals = new double[n];
        for (int i = 0; i < n; i++) {
            xVals[i] = i + 1;
        }
        R064Store expected = R064Store.FACTORY.column(xVals);

        // rhs = A * expected
        R064Store rhs = TaskIterativeTests.rhs(body, expected);
        if (DEBUG) {
            BasicLogger.debug("Random SPD 7x7 test");
        }
        ConjugateGradientSolverTest.doTestAllPreconditioners(body, expected, rhs);
    }

    @Test
    public void testRankOnePerturbedIdentity10() {
        // A = I + alpha * v v^T has only 2 distinct eigenvalues -> CG should converge in <=2 iterations (exact arithmetic)
        int n = 10;
        double alpha = 1_000.0; // strong rank-one perturbation
        double[] v = new double[n];
        for (int i = 0; i < n; i++) {
            v[i] = (i + 1); // simple increasing vector
        }
        double vNorm2 = ZERO;
        for (double vi : v) {
            vNorm2 += vi * vi;
        }
        double[][] A = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = (i == j ? ONE : ZERO) + alpha * v[i] * v[j];
            }
        }
        RawStore body = RawStore.wrap(A);
        // Choose expected solution x = v (not proportional to eigenvector of I alone)
        R064Store expected = R064Store.FACTORY.column(v);
        // rhs = A * expected = (I + alpha v v^T) v = v + alpha (v^T v) v = (1 + alpha * ||v||^2) v
        double scale = ONE + alpha * vNorm2;
        R064Store rhs = R064Store.FACTORY.make(n, 1);
        for (int i = 0; i < n; i++) {
            rhs.set(i, scale * v[i]);
        }
        if (DEBUG) {
            BasicLogger.debug("Rank-one perturbed identity n=10 test (two eigenvalues)");
        }
        // Should converge very fast, but preconditioning + large alpha can introduce rounding effects – allow more iterations
        ConjugateGradientSolverTest.doTestAllPreconditioners(body, expected, rhs);
    }

}