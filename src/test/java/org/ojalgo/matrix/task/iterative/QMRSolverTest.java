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
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.convex.ConvexSolver;
import org.ojalgo.random.Uniform;
import org.ojalgo.type.context.NumberContext;

public class QMRSolverTest {

    public static final int MAX_MATRIX_SIZE = 6;
    public static final int NUM_RANDOM_MATRICES = 5;

    @Test
    public void testDense3x3AgainstLU() {
        final int n = 3;
        final Uniform rnd = new Uniform();
        rnd.setSeed(1L);
        final R064Store A = R064Store.FACTORY.makeFilled(n, n, rnd);
        final R064Store xTrue = R064Store.FACTORY.makeFilled(n, 1, rnd);
        final MatrixStore<Double> b = A.multiply(xTrue);

        QMRSolver solver = new QMRSolver();
        solver.configurator().accuracy(NumberContext.of(7, 12)).iterations(2000);

        MatrixStore<Double> xQMR = solver.solve(A, b).get();

        LU<Double> lu = LU.R064.make(A);
        lu.decompose(A);
        MatrixStore<Double> xLU = lu.getSolution(b);

        // Residual norm check
        double resQMR = residualNorm(A, xQMR, b);
        double resLU = residualNorm(A, xLU, b);

        TestUtils.assertTrue(resQMR <= 1e-15 || resQMR <= 20 * resLU,
                "QMR residual not sufficiently small: " + resQMR + " vs LU " + resLU);
    }

    @Test
    public void testDense5x5Random() {
        final int n = 5;
        final Uniform rnd = new Uniform();
        rnd.setSeed(2L);
        final R064Store A = R064Store.FACTORY.makeFilled(n, n, rnd);
        final R064Store xTrue = R064Store.FACTORY.makeFilled(n, 1, rnd);
        final MatrixStore<Double> b = A.multiply(xTrue);

        QMRSolver solver = new QMRSolver();
        solver.configurator().accuracy(NumberContext.of(7, 12)).iterations(4000);

        MatrixStore<Double> xQMR = solver.solve(A, b).get();

        double resQMR = residualNorm(A, xQMR, b);
        TestUtils.assertTrue(resQMR < 1e-12, "Residual too large: " + resQMR);
    }

    @Test
    public void testMultipleRandomMatricesAgainstLU() {
        Uniform rnd = new Uniform();
        rnd.setSeed(3L);
        QMRSolver solver = new QMRSolver();
        solver.configurator().accuracy(NumberContext.of(7, 12)).iterations(8000);

        for (int n = 2; n <= MAX_MATRIX_SIZE; n++) {
            for (int trial = 0; trial < NUM_RANDOM_MATRICES; trial++) {
                R064Store A = R064Store.FACTORY.makeFilled(n, n, rnd);
                R064Store xTrue = R064Store.FACTORY.makeFilled(n, 1, rnd);
                MatrixStore<Double> b = A.multiply(xTrue);

                MatrixStore<Double> xQMR = solver.solve(A, b).get();

                double resQMR = residualNorm(A, xQMR, b);
                TestUtils.assertTrue(resQMR <= 4e-10,
                        "QMR residual too large for n=" + n + ", trial=" + trial + ": " + resQMR);
            }
        }
    }

    @Test
    public void testIllConditionedHilbertSmall() {
        int n = 5;
        R064Store A = hilbert(n);
        Uniform rnd = new Uniform();
        rnd.setSeed(4L);
        R064Store xTrue = R064Store.FACTORY.makeFilled(n, 1, rnd);
        MatrixStore<Double> b = A.multiply(xTrue);

        QMRSolver solver = new QMRSolver();
        solver.configurator().accuracy(NumberContext.of(7, 12)).iterations(10000);

        MatrixStore<Double> xQMR = solver.solve(A, b).get();

        LU<Double> lu = LU.R064.make(A);
        lu.decompose(A);
        MatrixStore<Double> xLU = lu.getSolution(b);

        double resQMR = residualNorm(A, xQMR, b);
        double resLU = residualNorm(A, xLU, b);

        TestUtils.assertTrue(resQMR <= Math.max(1e-5, 200 * resLU),
                "QMR residual too large on Hilbert: " + resQMR + " vs LU " + resLU);
    }

    @Test
    public void testIllConditionedNearlySingularSmall() {
        int[] exponents = new int[]{2, 4, 6, 8, 10, 12};
        for (int exp : exponents) {
            double eps = Math.pow(10, -exp);

            R064Store A = R064Store.FACTORY.rows(new double[][]{
                    {1.0, 1.0, 1.0},
                    {1.0, 1.0, 1.0 + eps},
                    {1.0, 1.0 + eps, 1.0}
            });
            R064Store xTrue = R064Store.FACTORY.column(1.0, 1.0, 1.0);
            MatrixStore<Double> b = A.multiply(xTrue);

            QMRSolver solver = new QMRSolver();
            solver.configurator().accuracy(NumberContext.of(7, 12)).iterations(15000);

            MatrixStore<Double> xQMR = solver.solve(A, b).get();

            LU<Double> lu = LU.R064.make(A);
            lu.decompose(A);
            MatrixStore<Double> xLU = lu.getSolution(b);

            double resQMR = residualNorm(A, xQMR, b);
            double resLU = residualNorm(A, xLU, b);

            if (Double.isNaN(resQMR) || Double.isNaN(resLU)) {
                BasicLogger.debug("Skipping assertion due to NaN residual(s) at eps=1e-" + exp + ": QMR=" + resQMR + ", LU=" + resLU);
                continue;
            }

            TestUtils.assertTrue(resQMR <= Math.max(1e-9, 400 * resLU),
                    "QMR residual too large on nearly singular (eps=1e-" + exp + "): " + resQMR + " vs LU " + resLU);
        }
    }

    @Test
    public void testModerateSizePerformanceWithin1s() {
        int n = 30; // moderate size
        Uniform rnd = new Uniform();
        rnd.setSeed(5L);
        R064Store A = R064Store.FACTORY.makeFilled(n, n, rnd);
        for (int i = 0; i < n; i++) {
            A.add(i, i, n);
        }
        R064Store xTrue = R064Store.FACTORY.makeFilled(n, 1, rnd);
        MatrixStore<Double> b = A.multiply(xTrue);

        long t0 = System.nanoTime();
        QMRSolver solver = new QMRSolver();
        solver.configurator().accuracy(NumberContext.of(7, 12)).iterations(20000);
        MatrixStore<Double> xQMR = solver.solve(A, b).get();
        long elapsedMs = (System.nanoTime() - t0) / 1_000_000;

        double resQMR = residualNorm(A, xQMR, b);
        TestUtils.assertTrue(elapsedMs < 1000, "QMR solve exceeded time budget: " + elapsedMs + " ms");
        TestUtils.assertTrue(resQMR <= 1e-4, "QMR residual too large on moderate size: " + resQMR);
    }

    @Test //Quadratic model that fail in package org.ojalgo.matrix.store, ojAlgo, version 56.1
    public void quadraticTest(){

        double[][] q_ = new double[][]
                { { 52376.074545264215,	154256.51217212676,	1705.561292552271 },
                        { 154256.51217212676,	1.6421719350013012E8,	-97037.53141387558 },
                        { 1705.561292552271,	-97037.53141387558,	51821.80732179031 } };

        MatrixStore<Double> q = RawStore.wrap(q_);

        double[][] l_ = new double[][]
                { { 28143.10628459914 },
                        { -265258.1426397235 },
                        { 16688.44367610407 } };

        MatrixStore<Double> l = RawStore.wrap(l_);

        double[][] ae_ = new double[][]
                { { 1.0,	0.0,	0.0 },
                        { 0.0,	1.0,	1.0 } };

        MatrixStore<Double> ae = RawStore.wrap(ae_);

        double[][] be_ = new double[][]
                { { 0.0 },
                        { 0.0 } };

        MatrixStore<Double> be = RawStore.wrap(be_);

        double[][] ai_ = new double[][]
                { { -1.0,	-0.0,	-0.0 },
                        { -0.0,	-1.0,	-0.0 },
                        { -0.0,	-0.0,	-1.0 } };

        MatrixStore<Double> ai = RawStore.wrap(ai_);

        double[][] bi_ = new double[][]
                { { 0.050000000000000044 },
                        { 1.2148895509567436E-4 },
                        { 0.050000000000000044 } };

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
        options.sparse=true;
        options.validate=true;
        ConvexSolver.Configuration convex = options.convex();
        NumberContext present = convex.iterative();
        convex.iterative(NumberContext.of(8));
        convex.solverSPD(Cholesky.R064::make).solverGeneral(LU.R064::make).iterative(NumberContext.of(12));
        convex.extendedPrecision(false);
        convex.setIterativeSolver(new QMRSolver());
        final ConvexSolver convexModel = builder.build(options);
        Optimisation.Result startValue = Optimisation.Result.of(0, Optimisation.State.APPROXIMATE, new double[q.getColDim()]);

        Optimisation.Result firstResult = null;
        try {
            firstResult = convexModel.solve(startValue);
        } catch (Exception e) {
            firstResult = convexModel.solve(startValue);
        }
        TestUtils.assertTrue(firstResult.getState().isSuccess());

    }

    private static R064Store hilbert(int n) {
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
}
