/*
 * Copyright 1997-2025 Optimatika
 */
package org.ojalgo.matrix.task.iterative;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.convex.ConvexSolver;
import org.ojalgo.random.Uniform;
import org.ojalgo.type.context.NumberContext;

public class MINRESSolverTest {

    /**
     * Creates a 2D Laplacian matrix with Dirichlet boundary conditions
     *
     * @param n Grid dimension (n x n grid)
     * @return R064Store containing the Laplacian matrix
     */
    public static R064Store create2DLaplacianMatrix(final int n) {
        int N = n * n; // Total number of unknowns
        R064Store A = R064Store.FACTORY.make(N, N);

        // Construct the 2D Laplacian matrix with Dirichlet boundary conditions
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int idx = i * n + j;
                A.set(idx, idx, 4.0);

                // Left neighbor
                if (j > 0) {
                    A.set(idx, idx - 1, -1.0);
                }
                // Right neighbor
                if (j < n - 1) {
                    A.set(idx, idx + 1, -1.0);
                }
                // Top neighbor
                if (i > 0) {
                    A.set(idx, idx - n, -1.0);
                }
                // Bottom neighbor
                if (i < n - 1) {
                    A.set(idx, idx + n, -1.0);
                }
            }
        }

        return A;
    }

    /**
     * Creates the RHS vector for the 2D Laplacian problem
     *
     * @param n Grid dimension (n x n grid)
     * @return R064Store containing the RHS vector
     */
    public static R064Store create2DLaplacianRHS(final int n) {
        int N = n * n; // Total number of unknowns
        R064Store b = R064Store.FACTORY.make(N, 1);

        // Set RHS value
        for (int i = 0; i < N; i++) {
            b.set(i, 0, 1.0);
        }

        return b;
    }

    private static R064Store hilbert(final int n) {
        R064Store H = R064Store.FACTORY.make(n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                H.set(i, j, 1.0 / (i + j + 1.0));
            }
        }
        return H;
    }

    private static double residualNorm(final MatrixStore<Double> A, final MatrixStore<Double> x, final MatrixStore<Double> b) {
        MatrixStore<Double> Ax = A.multiply(x);
        double norm = 0.0;
        for (int i = 0; i < (int) b.countRows(); i++) {
            double ri = b.doubleValue(i) - Ax.doubleValue(i);
            norm = Math.hypot(norm, ri);
        }
        return norm;
    }

    private static R064Store symmetrize(final R064Store A) {
        MatrixStore<Double> AT = A.transpose();
        MatrixStore<Double> S = A.add(AT).divide(2.0);
        return R064Store.FACTORY.copy(S);
    }

    @Test
    public void laplacian2DComparisonTest() {
        final int MAX_GRID_SIZE = 15;

        // Create array of preconditioners to compare
        Preconditioner[] preconditioners = new Preconditioner[] { Preconditioner.newIdentity(), Preconditioner.newJacobi(),
                Preconditioner.newSymmetricGaussSeidel(), Preconditioner.newSSOR(1.25) };

        //        BasicLogger.debug("2D Laplacian Preconditioner Comparison");
        //        BasicLogger.debug("========================================");

        // Loop from increasing matrix size
        for (int n = 1; n <= MAX_GRID_SIZE; n++) {
            // BasicLogger.debug("\nGrid size: " + n + "x" + n + " (Matrix size: " + (n * n) + "x" + (n * n) + ")");

            // Create 2D Laplacian matrix and RHS
            MatrixStore<Double> A = MINRESSolverTest.create2DLaplacianMatrix(n);
            MatrixStore<Double> b = MINRESSolverTest.create2DLaplacianRHS(n);

            // Test each preconditioner
            for (Preconditioner P : preconditioners) {

                MINRESSolver solver = new MINRESSolver();
                solver.configurator().accuracy(NumberContext.of(14)).iterations(200).preconditioner(P);
                MatrixStore<Double> solution = solver.solve(A, b).get();

                double residualNorm = MINRESSolverTest.residualNorm(A, solution, b);
                // BasicLogger.debug("  " + P.getClass().getSimpleName() + ": residual = " + residualNorm);
            }
        }
    }

    /**
     * Quadratic model test verifying that {@link MINRESSolver} can function as the iterative sub-solver in
     * the {@link ConvexSolver} active set solver.
     */
    @Test
    public void quadraticTest() {

        double[][] q_ = new double[][] { { 52376.074545264215, 154256.51217212676, 1705.561292552271 },
                { 154256.51217212676, 1.6421719350013012E8, -97037.53141387558 }, { 1705.561292552271, -97037.53141387558, 51821.80732179031 } };

        MatrixStore<Double> q = RawStore.wrap(q_);

        double[][] l_ = new double[][] { { 28143.10628459914 }, { -265258.1426397235 }, { 16688.44367610407 } };

        MatrixStore<Double> l = RawStore.wrap(l_);

        double[][] ae_ = new double[][] { { 1.0, 0.0, 0.0 }, { 0.0, 1.0, 1.0 } };

        MatrixStore<Double> ae = RawStore.wrap(ae_);

        double[][] be_ = new double[][] { { 0.0 }, { 0.0 } };

        MatrixStore<Double> be = RawStore.wrap(be_);

        double[][] ai_ = new double[][] { { -1.0, -0.0, -0.0 }, { -0.0, -1.0, -0.0 }, { -0.0, -0.0, -1.0 } };

        MatrixStore<Double> ai = RawStore.wrap(ai_);

        double[][] bi_ = new double[][] { { 0.050000000000000044 }, { 1.2148895509567436E-4 }, { 0.050000000000000044 } };

        MatrixStore<Double> bi = RawStore.wrap(bi_);

        ConvexSolver.Builder builder = ConvexSolver.newBuilder();
        builder.objective(q, l);
        if (ae != null && be != null) {
            builder.equalities(ae, be);
        }
        if (ai != null && bi != null) {
            builder.inequalities(ai, bi);
        }
        Optimisation.Options options = new Optimisation.Options();
        options.iterations_abort = 100000;
        options.iterations_suffice = 10000;
        options.time_abort = 10000;
        options.time_suffice = 1000;
        options.sparse = true;
        options.validate = true;
        ConvexSolver.Configuration convex = options.convex();
        convex.solverSPD(Cholesky.R064::make).solverGeneral(LU.R064::make).iterative(NumberContext.of(12));
        convex.extendedPrecision(false);
        convex.iterative(MINRESSolver::new, NumberContext.of(12));
        ConvexSolver convexModel = builder.build(options);
        Optimisation.Result startValue = Optimisation.Result.of(0, Optimisation.State.APPROXIMATE, new double[q.getColDim()]);

        Optimisation.Result firstResult = convexModel.solve(startValue);

        TestUtils.assertTrue(firstResult.getState().isSuccess());
    }

    /**
     * Quadratic model test 2 verifying that {@link MINRESSolver} can function with larger problems in the
     * {@link ConvexSolver} active set solver.
     */
    @Test
    public void quadraticTest2() {

        double[][] q_ = new double[][] { { 6668.278705650674, 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 6.315929943942014E12, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 2.3988688378282965E13, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 6.359204638050388E15, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 6.129604959867126E15, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0, 2660643.1998935738 } };

        MatrixStore<Double> q = R064Store.FACTORY.makeWrapper(RawStore.wrap(q_));

        double[][] l_ = new double[][] { { -476147.7777069725 }, { -300667.6690518168 }, { -153392.19762707502 }, { -178047.70825643447 },
                { -59521.3068376414 }, { 4965004.705644156 } };

        MatrixStore<Double> l = R064Store.FACTORY.makeWrapper(RawStore.wrap(l_));

        double[][] ae_ = new double[][] { { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 } };

        MatrixStore<Double> ae = R064Store.FACTORY.makeWrapper(RawStore.wrap(ae_));

        double[][] be_ = new double[][] { { -2.220446049250313E-16 } };

        MatrixStore<Double> be = R064Store.FACTORY.makeWrapper(RawStore.wrap(be_));

        double[][] ai_ = new double[][] { { -1.0, -0.0, -0.0, -0.0, -0.0, -0.0 }, { -0.0, -1.0, -0.0, -0.0, -0.0, -0.0 },
                { -0.0, -0.0, -1.0, -0.0, -0.0, -0.0 }, { -0.0, -0.0, -0.0, -1.0, -0.0, -0.0 }, { -0.0, -0.0, -0.0, -0.0, -1.0, -0.0 },
                { -0.0, -0.0, -0.0, -0.0, -0.0, -1.0 } };

        MatrixStore<Double> ai = R064Store.FACTORY.makeWrapper(RawStore.wrap(ai_));

        double[][] bi_ = new double[][] { { 0.050000000000000044 }, { 1.0521478434747294E-9 }, { 2.7628102075066857E-10 }, { 4.5981121632729495E-14 },
                { 8.51609595643812E-14 }, { 0.0024937500000995 } };

        MatrixStore<Double> bi = R064Store.FACTORY.makeWrapper(RawStore.wrap(bi_));

        ConvexSolver.Builder builder = ConvexSolver.newBuilder();
        builder.objective(q, l);
        if (ae != null && be != null) {
            builder.equalities(ae, be);
        }
        if (ai != null && bi != null) {
            builder.inequalities(ai, bi);
        }
        Optimisation.Options options = new Optimisation.Options();
        options.sparse = true;
        options.validate = true;
        ConvexSolver.Configuration convex = options.convex();
        convex.iterative(MINRESSolver::new, NumberContext.of(12));
        convex.solverSPD(Cholesky.R064::make).solverGeneral(LU.R064::make).iterative(NumberContext.of(12));
        convex.extendedPrecision(false);
        ConvexSolver convexModel = builder.build(options);
        Optimisation.Result startValue = Optimisation.Result.of(0, Optimisation.State.APPROXIMATE, new double[q.getColDim()]);

        Optimisation.Result firstResult = convexModel.solve(startValue);
        for (int i = 0; i < firstResult.size(); i++) {
            TestUtils.assertTrue(Double.isFinite(firstResult.doubleValue(i)));
        }
        TestUtils.assertTrue(Double.isFinite(firstResult.getValue()));
        TestUtils.assertTrue(firstResult.getState().isSuccess());
    }

    @Test
    public void testDenseSymmetric5x5Random() {
        int n = 5;
        Uniform rnd = new Uniform();
        rnd.setSeed(2L);
        R064Store A = R064Store.FACTORY.makeFilled(n, n, rnd);
        A = MINRESSolverTest.symmetrize(A);
        R064Store xTrue = R064Store.FACTORY.makeFilled(n, 1, rnd);
        R064Store b = R064Store.FACTORY.copy(A.multiply(xTrue));

        MINRESSolver solver = new MINRESSolver();
        solver.configurator().accuracy(NumberContext.of(16)).iterations(10);

        MatrixStore<Double> xMINRES = solver.solve(A, b).get();

        double resMINRES = MINRESSolverTest.residualNorm(A, xMINRES, b);
        TestUtils.assertTrue(resMINRES < 1e-15, "Residual too large: " + resMINRES);
    }

    @Test
    public void testHilbertSymmetricSmall() {
        int n = 6;
        R064Store A = MINRESSolverTest.hilbert(n);
        Uniform rnd = new Uniform();
        rnd.setSeed(13L);
        R064Store xTrue = R064Store.FACTORY.makeFilled(n, 1, rnd);
        R064Store b = R064Store.FACTORY.copy(A.multiply(xTrue));

        MINRESSolver solver = new MINRESSolver();
        solver.configurator().accuracy(NumberContext.of(16)).iterations(2 * n);
        MatrixStore<Double> xMINRES = solver.solve(A, b).get();

        LU<Double> lu = LU.R064.make(A);
        lu.decompose(A);
        MatrixStore<Double> xLU = lu.getSolution(b);

        double resMINRES = MINRESSolverTest.residualNorm(A, xMINRES, b);
        double resLU = MINRESSolverTest.residualNorm(A, xLU, b);

        TestUtils.assertTrue(resMINRES <= Math.max(1e-15, 50 * resLU), "MINRES residual too large on Hilbert: " + resMINRES + " vs LU " + resLU);
    }

    // Additional tests from QMRSolverTest adapted for MINRES

    @Test
    public void testIllConditionedNearlySingularSmall() {
        int[] exponents = new int[] { 10, 12, 14, 16 };
        for (int exp : exponents) {
            double eps = Math.pow(10, -exp);

            // Create a symmetric nearly singular matrix
            RawStore A = RawStore.wrap(new double[][] { { 2.0, 1.0 + eps, 1.0 }, { 1.0 + eps, 2.0, 1.0 + eps }, { 1.0, 1.0 + eps, 2.0 } });
            R064Store xTrue = R064Store.FACTORY.column(1.0, 1.0, 1.0);
            R064Store b = R064Store.FACTORY.copy(A.multiply(xTrue));

            MINRESSolver solver = new MINRESSolver();
            solver.configurator().accuracy(NumberContext.of(16)).iterations(10);

            MatrixStore<Double> xMINRES = solver.solve(A, b).get();

            double resMINRES = MINRESSolverTest.residualNorm(A, xMINRES, b);

            TestUtils.assertTrue(resMINRES <= 1e-14, "MINRES residual too large on nearly singular (eps=1e-" + exp + "): " + resMINRES);
        }
    }

    @Test
    public void testLogSpacedDiagonalSymmetric8x8() {
        int n = 8;
        double[][] Aarr = new double[n][n];
        for (int i = 0; i < n; i++) {
            double exp = (6.0) * i / (n - 1);
            Aarr[i][i] = Math.pow(10.0, exp);
        }
        MatrixStore<Double> A = RawStore.wrap(Aarr);

        double[] xVals = new double[n];
        for (int i = 0; i < n; i++) {
            xVals[i] = i + 1;
        }
        R064Store xTrue = R064Store.FACTORY.column(xVals);
        R064Store b = R064Store.FACTORY.copy(A.multiply(xTrue));

        MINRESSolver solver = new MINRESSolver();
        solver.configurator().accuracy(NumberContext.of(16)).iterations(2 * n);
        MatrixStore<Double> xMINRES = solver.solve(A, b).get();

        LU<Double> lu = LU.R064.make(A);
        lu.decompose(A);
        MatrixStore<Double> xLU = lu.getSolution(b);

        double resMINRES = MINRESSolverTest.residualNorm(A, xMINRES, b);
        double resLU = MINRESSolverTest.residualNorm(A, xLU, b);
        TestUtils.assertTrue(resMINRES <= Math.max(1e-7, 50 * resLU), "MINRES residual too large on Log-spaced diagonal 8x8: " + resMINRES + " vs LU " + resLU);
    }

    @Test
    public void testMultipleRandomSymmetricMatrices() {
        Uniform rnd = new Uniform();
        rnd.setSeed(11L);
        for (int n = 2; n <= 8; n++) {
            for (int trial = 0; trial < 5; trial++) {
                R064Store A = R064Store.FACTORY.makeFilled(n, n, rnd);
                A = MINRESSolverTest.symmetrize(A);
                for (int i = 0; i < n; i++) {
                    A.add(i, i, n);
                }
                R064Store xTrue = R064Store.FACTORY.makeFilled(n, 1, rnd);
                R064Store b = R064Store.FACTORY.copy(A.multiply(xTrue));

                MINRESSolver solver = new MINRESSolver();
                solver.configurator().accuracy(NumberContext.of(16)).iterations(2 * n);
                MatrixStore<Double> xMINRES = solver.solve(A, b).get();

                LU<Double> lu = LU.R064.make(A);
                lu.decompose(A);
                MatrixStore<Double> xLU = lu.getSolution(b);

                double resMINRES = MINRESSolverTest.residualNorm(A, xMINRES, b);
                double resLU = MINRESSolverTest.residualNorm(A, xLU, b);
                TestUtils.assertTrue(resMINRES <= Math.max(4e-15, 50 * resLU),
                        "Residual too large for n=" + n + ", trial=" + trial + ": MINRES=" + resMINRES + ", LU=" + resLU);
            }
        }
    }

    @Test
    public void testMultipleRandomSymmetricMatricesAgainstLU() {
        Uniform rnd = new Uniform();
        rnd.setSeed(3L);
        MINRESSolver solver = new MINRESSolver();
        solver.configurator().accuracy(NumberContext.of(16)).iterations(12);

        int maxMatrixSize = 6;
        int numRandomMatrices = 5;

        for (int n = 2; n <= maxMatrixSize; n++) {
            for (int trial = 0; trial < numRandomMatrices; trial++) {
                R064Store A = R064Store.FACTORY.makeFilled(n, n, rnd);
                A = MINRESSolverTest.symmetrize(A);
                R064Store xTrue = R064Store.FACTORY.makeFilled(n, 1, rnd);
                R064Store b = R064Store.FACTORY.copy(A.multiply(xTrue));

                MatrixStore<Double> xMINRES = solver.solve(A, b).get();

                double resMINRES = MINRESSolverTest.residualNorm(A, xMINRES, b);
                TestUtils.assertTrue(resMINRES <= 1e-14, "MINRES residual too large for n=" + n + ", trial=" + trial + ": " + resMINRES);
            }
        }
    }

    @Test
    public void testPoisson1DSymmetricLength12() {
        int n = 12;
        double[][] Aarr = new double[n][n];
        for (int i = 0; i < n; i++) {
            Aarr[i][i] = 2.0;
            if (i > 0) {
                Aarr[i][i - 1] = -1.0;
            }
            if (i < n - 1) {
                Aarr[i][i + 1] = -1.0;
            }
        }
        MatrixStore<Double> A = RawStore.wrap(Aarr);

        double[] xVals = new double[n];
        for (int i = 0; i < n; i++) {
            xVals[i] = i + 1;
        }
        R064Store xTrue = R064Store.FACTORY.column(xVals);
        R064Store b = R064Store.FACTORY.copy(A.multiply(xTrue));

        MINRESSolver solver = new MINRESSolver();
        solver.configurator().accuracy(NumberContext.of(16)).iterations(2 * n);
        MatrixStore<Double> xMINRES = solver.solve(A, b).get();

        LU<Double> lu = LU.R064.make(A);
        lu.decompose(A);
        MatrixStore<Double> xLU = lu.getSolution(b);

        double resMINRES = MINRESSolverTest.residualNorm(A, xMINRES, b);
        double resLU = MINRESSolverTest.residualNorm(A, xLU, b);
        TestUtils.assertTrue(resMINRES <= Math.max(1e-13, 50 * resLU), "MINRES residual too large on Poisson1D: " + resMINRES + " vs LU " + resLU);
    }

    @Test
    public void testRandomSPDSymmetric7x7() {
        int n = 7;
        java.util.Random rnd = new java.util.Random(20251005L);
        double[][] M = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                M[i][j] = rnd.nextDouble() - 0.5;
            }
        }
        double[][] Aarr = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double sum = 0.0;
                for (int k = 0; k < n; k++) {
                    sum += M[k][i] * M[k][j];
                }
                if (i == j) {
                    sum += n;
                }
                Aarr[i][j] = sum;
            }
        }
        MatrixStore<Double> A = RawStore.wrap(Aarr);

        double[] xVals = new double[n];
        for (int i = 0; i < n; i++) {
            xVals[i] = i + 1;
        }
        R064Store xTrue = R064Store.FACTORY.column(xVals);
        R064Store b = R064Store.FACTORY.copy(A.multiply(xTrue));

        MINRESSolver solver = new MINRESSolver();
        solver.configurator().accuracy(NumberContext.of(16)).iterations(2 * n);
        MatrixStore<Double> xMINRES = solver.solve(A, b).get();

        LU<Double> lu = LU.R064.make(A);
        lu.decompose(A);
        MatrixStore<Double> xLU = lu.getSolution(b);

        double resMINRES = MINRESSolverTest.residualNorm(A, xMINRES, b);
        double resLU = MINRESSolverTest.residualNorm(A, xLU, b);
        TestUtils.assertTrue(resMINRES <= Math.max(1e-13, 100 * resLU), "MINRES residual too large on Random SPD 7x7: " + resMINRES + " vs LU " + resLU);
    }

    @Test
    public void testRankOnePerturbedIdentitySymmetric10() {
        int n = 10;
        double alpha = 0.1;
        double[] v = new double[n];
        for (int i = 0; i < n; i++) {
            v[i] = i + 1;
        }
        double vNorm2 = 0.0;
        for (double vi : v) {
            vNorm2 += vi * vi;
        }
        double[][] Aarr = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Aarr[i][j] = (i == j ? 1.0 : 0.0) + alpha * v[i] * v[j];
            }
        }
        MatrixStore<Double> A = RawStore.wrap(Aarr);

        R064Store xTrue = R064Store.FACTORY.column(v);
        double scale = 1.0 + alpha * vNorm2;
        R064Store b = R064Store.FACTORY.make(n, 1);
        for (int i = 0; i < n; i++) {
            b.set(i, scale * v[i]);
        }

        MINRESSolver solver = new MINRESSolver();
        solver.configurator().accuracy(NumberContext.of(16)).iterations(2 * n);
        MatrixStore<Double> xMINRES = solver.solve(A, b).get();

        LU<Double> lu = LU.R064.make(A);
        lu.decompose(A);
        MatrixStore<Double> xLU = lu.getSolution(b);

        double resMINRES = MINRESSolverTest.residualNorm(A, xMINRES, b);
        double resLU = MINRESSolverTest.residualNorm(A, xLU, b);
        TestUtils.assertTrue(resMINRES <= Math.max(2e-13, 50 * resLU),
                "MINRES residual too large on rank-one perturbed identity: " + resMINRES + " vs LU " + resLU);
    }

    @Test
    public void testSmallSymmetric3x3AgainstLU() {
        int n = 3;
        Uniform rnd = new Uniform();
        rnd.setSeed(7L);
        R064Store A = R064Store.FACTORY.makeFilled(n, n, rnd);
        A = MINRESSolverTest.symmetrize(A);
        R064Store xTrue = R064Store.FACTORY.makeFilled(n, 1, rnd);
        R064Store b = R064Store.FACTORY.copy(A.multiply(xTrue));

        MINRESSolver solver = new MINRESSolver();
        solver.configurator().accuracy(NumberContext.of(16)).iterations(2 * n);
        MatrixStore<Double> xMINRES = solver.solve(A, b).get();

        LU<Double> lu = LU.R064.make(A);
        lu.decompose(A);
        MatrixStore<Double> xLU = lu.getSolution(b);

        double resMINRES = MINRESSolverTest.residualNorm(A, xMINRES, b);
        double resLU = MINRESSolverTest.residualNorm(A, xLU, b);

        TestUtils.assertTrue(resMINRES <= 1e-15 || resMINRES <= 5 * resLU, "MINRES residual not sufficiently small: " + resMINRES + " vs LU " + resLU);
    }
}
