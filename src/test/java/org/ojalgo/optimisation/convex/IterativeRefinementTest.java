package org.ojalgo.optimisation.convex;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.TestUtils;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.constant.QuadrupleMath;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

public class IterativeRefinementTest extends OptimisationConvexTests {

    private static final NumberContext HIGH_ACCURACY = NumberContext.of(9);
    private static final Factory<RationalNumber, GenericStore<RationalNumber>> Q128 = GenericStore.Q128;

    private static void doReimplementExample4(final boolean step1AsInExample) {

        try {

            GenericStore<RationalNumber> mtrxQ = Q128.make(2, 2);
            mtrxQ.set(0, 0, RationalNumber.ONE);
            mtrxQ.set(1, 1, RationalNumber.ONE);

            GenericStore<RationalNumber> mtrxC = Q128.make(2, 1);
            mtrxC.set(0, RationalNumber.ONE.negate()); // C-matrix negated to suit ojAlgo (simplify KKT system)
            mtrxC.set(1, RationalNumber.parse("1.000001").negate());

            GenericStore<RationalNumber> mtrxAE = Q128.make(1, 2);
            mtrxAE.set(0, 0, RationalNumber.ONE);
            mtrxAE.set(0, 1, RationalNumber.ONE);
            GenericStore<RationalNumber> mtrxBE = Q128.make(1, 1);
            mtrxBE.set(0, RationalNumber.parse("0.000001"));

            GenericStore<RationalNumber> mtrxAI = Q128.make(2, 2);
            mtrxAI.set(0, 0, RationalNumber.NEG);
            mtrxAI.set(1, 1, RationalNumber.NEG);
            GenericStore<RationalNumber> mtrxBI = Q128.make(2, 1);
            mtrxBI.set(0, RationalNumber.ZERO); // -0.0
            mtrxBI.set(1, RationalNumber.ZERO); // -0.0

            /*
             * The example states an initail (slightly infeasible) solution that is:
             */

            GenericStore<RationalNumber> mtrxX = Q128.make(2, 1); // All zero primal solution
            GenericStore<RationalNumber> mtrxL = Q128.make(1, 1); // Dual variable / Lagrange multiplier for the equality constraint == 1.0
            mtrxL.set(0, RationalNumber.ONE);

            /*
             * That can't be true! It does not satisfy the KKT equation system. Solving that with
             * RationalNumber exactness gives:
             */

            MatrixStore<RationalNumber> body = mtrxQ.right(mtrxAE.transpose()).below(mtrxAE);
            MatrixStore<RationalNumber> rhs = mtrxC.below(mtrxBE); // C-matrix already negated
            SolverTask<RationalNumber> solver = SolverTask.RATIONAL.make(body, rhs);

            MatrixStore<RationalNumber> expected = solver.solve(body, rhs);

            if (DEBUG) {
                BasicLogger.debugMatrix("Initial KKT solution", expected);
            }

            /*
             * Verify that we get the same solution with the x[1] >= 0.0 bound/constraint active.
             */

            MatrixStore<RationalNumber> tmpA = mtrxAE.below(mtrxAI.row(1));
            MatrixStore<RationalNumber> tmpB = mtrxBE.below(mtrxBI.row(1));

            body = mtrxQ.right(tmpA.transpose()).below(tmpA);
            rhs = mtrxC.below(tmpB);

            MatrixStore<RationalNumber> verify = solver.solve(body, rhs);

            if (DEBUG) {
                BasicLogger.debugMatrix("Verified (initial) KKT solution", verify);
            }

            TestUtils.assertEquals(expected.doubleValue(0), verify.doubleValue(0), HIGH_ACCURACY);
            TestUtils.assertEquals(expected.doubleValue(1), verify.doubleValue(1), HIGH_ACCURACY);
            TestUtils.assertEquals(expected.doubleValue(2), verify.doubleValue(2), HIGH_ACCURACY);

            /*
             * Instead it must be -1.0
             */
            mtrxL.set(0, RationalNumber.NEG);

            Result resultStep1 = ConvexSolver.newBuilder(mtrxQ).linear(mtrxC).equalities(mtrxAE, mtrxBE).inequalities(mtrxAI, mtrxBI).solve();

            GenericStore<RationalNumber> step1X = Q128.columns(resultStep1);
            GenericStore<RationalNumber> step1L = Q128.columns(resultStep1.getMultipliers().get());

            if (DEBUG) {
                BasicLogger.debugMatrix("Step 1 Solution", step1X);
                BasicLogger.debugMatrix("Step 1 Dual", step1L);
            }

            if (step1AsInExample) {
                /*
                 * ojAlgo direcly finds the full/exact solution. We need to disregard that and use the
                 * approximate solution given in the example (with the dual variable negated).
                 */
                step1X.set(0, RationalNumber.ZERO);
                step1X.set(1, RationalNumber.ZERO);
                step1L.set(0, RationalNumber.NEG);
            }

            RationalNumber scale = RationalNumber.valueOf(1_000_000);
            RationalNumber inverse = scale.invert(); // Same scale for primal/dual variables

            /*
             * Update formulae based on own derivation. The update formula for C differs from what's in the
             * paper. Believe the one used here is correct (given that we use "-C").
             */

            mtrxC = mtrxC.subtract(mtrxQ.multiply(step1X)).subtract(mtrxAE.transpose().multiply(step1L.row(0))).multiply(scale).collect(Q128);
            mtrxBE = mtrxBE.subtract(mtrxAE.multiply(step1X)).multiply(scale).collect(Q128);
            mtrxBI = step1X.subtract(mtrxBI).multiply(scale).collect(Q128);

            Result resultStep2 = ConvexSolver.newBuilder(mtrxQ).linear(mtrxC).equalities(mtrxAE, mtrxBE).inequalities(mtrxAI, mtrxBI).solve();

            GenericStore<RationalNumber> step2X = Q128.columns(resultStep2);
            GenericStore<RationalNumber> step2L = Q128.columns(resultStep2.getMultipliers().get());

            if (DEBUG) {
                BasicLogger.debugMatrix("Step 2 Solution", step2X);
                BasicLogger.debugMatrix("Step 2 Dual", step2L);
            }

            if (step1AsInExample) {
                TestUtils.assertEquals(1.0, step2X.doubleValue(0), HIGH_ACCURACY);
                TestUtils.assertEquals(0.0, step2X.doubleValue(1), HIGH_ACCURACY);
                TestUtils.assertEquals(-1.0, step2L.doubleValue(0), HIGH_ACCURACY);
            } else {
                TestUtils.assertEquals(0.0, step2X.doubleValue(0), HIGH_ACCURACY);
                TestUtils.assertEquals(0.0, step2X.doubleValue(1), HIGH_ACCURACY);
                TestUtils.assertEquals(0.0, step2L.doubleValue(0), HIGH_ACCURACY);
            }

            mtrxX.fillMatching(step1X.add(step2X.multiply(inverse)));
            mtrxL.fillMatching(step1L.add(step2L.multiply(inverse)));

            if (DEBUG) {
                BasicLogger.debugMatrix("Final/total Solution", mtrxX);
                BasicLogger.debugMatrix("Final/total Dual", mtrxL);
            }

            TestUtils.assertEquals(expected.doubleValue(0), mtrxX.doubleValue(0), HIGH_ACCURACY);
            TestUtils.assertEquals(expected.doubleValue(1), mtrxX.doubleValue(1), HIGH_ACCURACY);
            TestUtils.assertEquals(expected.doubleValue(2), mtrxL.doubleValue(0), HIGH_ACCURACY);

        } catch (RecoverableCondition cause) {
            TestUtils.fail(cause);
        }
    }

    private static double getResidualQuadruplePrecision(final MatrixStore<Double> x_in, final MatrixStore<Double> y_in, final MatrixStore<Quadruple> Q_in,
            final MatrixStore<Quadruple> C_in, final MatrixStore<Quadruple> Ae_in, final MatrixStore<Quadruple> be_in, final MatrixStore<Quadruple> Ai_in,
            final MatrixStore<Quadruple> bi_in) {

        double epsPrimal = 1E-15;
        double epsDual = Math.sqrt(1E-15);
        double epsSlack = Math.sqrt(1E-15);

        double be_Size = be_in.aggregateAll(Aggregator.LARGEST).doubleValue();
        be_Size = be_Size > 1E-15 ? be_Size : 1;
        double C_Size = C_in.aggregateAll(Aggregator.LARGEST).doubleValue();
        double Q_Size = Q_in.aggregateAll(Aggregator.LARGEST).doubleValue();

        MatrixStore<Quadruple> x = GenericStore.R128.columns(x_in);
        MatrixStore<Quadruple> y = GenericStore.R128.columns(y_in);

        MatrixStore<Quadruple> Q = GenericStore.R128.makeWrapper(Q_in);
        MatrixStore<Quadruple> C = GenericStore.R128.makeWrapper(C_in);
        MatrixStore<Quadruple> Ae = GenericStore.R128.makeWrapper(Ae_in);
        MatrixStore<Quadruple> Be = GenericStore.R128.makeWrapper(be_in);
        MatrixStore<Quadruple> Ai = GenericStore.R128.makeWrapper(Ai_in);
        MatrixStore<Quadruple> Bi = GenericStore.R128.makeWrapper(bi_in);

        // Compute residuals in Quadruple precission
        MatrixStore<Quadruple> be1 = Be.subtract(Ae.multiply(x));
        double maxEqualityResidual = be1.aggregateAll(Aggregator.LARGEST).doubleValue();
        MatrixStore<Quadruple> bi1 = Bi.subtract(Ai.multiply(x));
        double maxInequalityResidual = bi1.negate().aggregateAll(Aggregator.MAXIMUM).doubleValue();
        double maxPrimalResidual = Math.max(maxEqualityResidual, maxInequalityResidual);
        MatrixStore<Quadruple> C1 = C.subtract(Q.multiply(x)).subtract(Ae.below(Ai).transpose().multiply(y));
        double maxGradientResidual = C1.negate().aggregateAll(Aggregator.LARGEST).norm();
        double relativeGradientResidual = maxGradientResidual / C_Size;
        // SUM_i ABS(C1_i * x_i) / |C1|
        double relativeComplementarySlackness1 = C1.onMatching(QuadrupleMath.MULTIPLY, x).collect(GenericStore.R128).aggregateAll(Aggregator.LARGEST)
                .doubleValue() / C_Size;
        // SUM_i ABS(y0_i * b_i) / |Be|
        double relativeComplementarySlackness2 = y.onMatching(QuadrupleMath.MULTIPLY, be1.below(bi1)).collect(GenericStore.R128)
                .aggregateAll(Aggregator.LARGEST).doubleValue() / be_Size;
        double relativeComplementarySlackness = Math.max(relativeComplementarySlackness1, relativeComplementarySlackness2 * relativeComplementarySlackness2);
        Quadruple objectiveValue = Q.multiplyBoth(x).divide(2).subtract(x.transpose().multiply(C).get(0));
        return Math.max(relativeComplementarySlackness, Math.max(maxPrimalResidual, relativeGradientResidual));
    }

    /**
     * The example from "Solving quadratic programs to high precision using scaled iterative refinement".
     * <p>
     * Modelled using {@link ExpressionsBasedModel} and with
     * <code>model.options.convex().extendedPrecision(true)</code> this should find the exact solution of that
     * example.
     */
    @Test
    public void testIterativeRefinementSolver() {

        BigDecimal small = new BigDecimal("0.000001");

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x1 = model.newVariable("X1").lower(BigMath.ZERO).weight(BigMath.ONE);
        Variable x2 = model.newVariable("X2").lower(BigMath.ZERO).weight(BigMath.ONE.add(small));

        model.newExpression("Q").set(x1, x1, BigMath.ONE).set(x2, x2, BigMath.ONE).weight(BigMath.HALF);

        model.newExpression("SUM").set(x1, BigMath.ONE).set(x2, BigMath.ONE).level(small);

        model.options.convex().extendedPrecision(true); // Set this to false, and the test may fail

        Result minimise = model.minimise();

        TestUtils.assertStateNotLessThanOptimal(minimise);

        TestUtils.assertEquals(small, minimise.get(0), HIGH_ACCURACY);
        TestUtils.assertEquals(BigMath.ZERO, minimise.get(1), HIGH_ACCURACY);

        TestUtils.assertEquals(small, x1.getValue(), HIGH_ACCURACY);
        TestUtils.assertEquals(BigMath.ZERO, x2.getValue(), HIGH_ACCURACY);
    }

    /**
     * A variation of the example from "Solving quadratic programs to high precision using scaled iterative
     * refinement".
     */
    @Test
    public void testIterativeRefinementSolverV2() {

        BigDecimal smallPrim = new BigDecimal("0.000000000001");
        BigDecimal smallDual = new BigDecimal("0.000001");

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        // model.options.convex().iterative(NumberContext.of(12));

        Variable x1 = model.newVariable("X1").lower(BigMath.ZERO).weight(BigMath.ONE);
        Variable x2 = model.newVariable("X2").lower(BigMath.ZERO).weight(BigMath.ONE.add(smallDual));

        model.newExpression("Q").set(x1, x1, BigMath.ONE).set(x2, x2, BigMath.ONE).weight(BigMath.HALF);

        model.newExpression("SUM").set(x1, BigMath.ONE).set(x2, BigMath.ONE).level(smallPrim);

        model.options.convex().extendedPrecision(true); // Set this to false, and the test may fail

        Result minimise = model.minimise();

        TestUtils.assertStateNotLessThanOptimal(minimise);

        if (DEBUG) {
            BasicLogger.debug(minimise.toString());
        }

        TestUtils.assertEquals(smallPrim, minimise.get(0), HIGH_ACCURACY);
        TestUtils.assertEquals(BigMath.ZERO, minimise.get(1), HIGH_ACCURACY);

        TestUtils.assertEquals(smallPrim, x1.getValue(), HIGH_ACCURACY);
        TestUtils.assertEquals(BigMath.ZERO, x2.getValue(), HIGH_ACCURACY);
    }

    /**
     * Attempt to re-implement the numerical example (Example 4) in "Solving quadratic programs to high
     * precision using scaled iterative refinement".
     * <p>
     * Seems to me they made some mistake in this example (in this paper). Either the problem should be stated
     * with "-C" rather than "+C" or the initial dual variable must be -1.0 rather than 1.0 (some combination
     * of error in this area).
     * <p>
     * Further, I believe there is a mistake in the update formula for C. Regradless of "+C" or "-C" the
     * formula in the paper seems wrong.
     * <p>
     * In this re-implementation the problem is stated with "-C", as it is how ojAlgo usually works. The
     * (initial) dual variable is negative (since it has to be), and the C update forula is modified according
     * to my own derivation. Then it works... except that ojAlgo finds the exact optimal solution in the first
     * iteration. To test step 2 of the example we have to pretend that it doesn't.
     */
    @Test
    public void testReimplementExample4() {

        // As in the example
        IterativeRefinementTest.doReimplementExample4(true);

        // With ojAlgo finding the correct and exact solution in the first step.
        // Test that doing the second step anyway doesn't break anyhing
        IterativeRefinementTest.doReimplementExample4(false);
    }

    @Test
    void testQP1() {

        Optimisation.Options options = new Optimisation.Options();
        options.sparse = false;
        options.convex().solverSPD(Cholesky.R064::make).solverGeneral(LU.R064::make).iterative(NumberContext.of(16));

        GenericStore<Quadruple> AE = GenericStore.R128.rows(new double[][] { { 1.0, 1.0, 1.0, 1.0, 1.0 } });
        GenericStore<Quadruple> BE = GenericStore.R128.rows(new double[][] { { 8.5E-18 } });

        GenericStore<Quadruple> AI = GenericStore.R128.rows(new double[][] { { -1.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, -1.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, -1.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, -1.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, -1.0 } });
        GenericStore<Quadruple> BI = GenericStore.R128.rows(new double[][] { { 0.7907669085467611 }, { 0.0019999999999900154 }, { 0.20323309144826854 },
                { 0.0019999999999900154 }, { 0.0019999999999903207 } });

        GenericStore<Quadruple> C = GenericStore.R128.rows(
                new double[][] { { -550875.2032141489 }, { -562399.9568628508 }, { -564250.6957511578 }, { -691369.2681449897 }, { -1400521.5234620553 } });
        GenericStore<Quadruple> Q = GenericStore.R128
                .rows(new double[][] { { 10514.489048700269, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 4157254.9979421264, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 40911.20171773545, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 4157254.9979421264, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 4157254.9979414917 } });

        Optimisation.Result result = IterativeRefinementSolver.doSolve(Q, C, AE, BE, AI, BI, options);

        MatrixStore<Double> x = Primitive64Store.FACTORY.columns(result);
        MatrixStore<Double> y = Primitive64Store.FACTORY.columns(result.getMultipliers().get());

        double precision = IterativeRefinementTest.getResidualQuadruplePrecision(x, y, Q, C, AE, BE, AI, BI);
        TestUtils.assertLessThan(1e-15, precision);
    }

    @Test
    void testQP2() {

        Optimisation.Options options = new Optimisation.Options();
        options.sparse = false;
        options.convex().solverSPD(Cholesky.R064::make).solverGeneral(LU.R064::make).iterative(NumberContext.of(16));

        GenericStore<Quadruple> AE = GenericStore.R128.rows(new double[][] { { 1.0, 1.0, 1.0, 1.0, 1.0 } });
        GenericStore<Quadruple> BE = GenericStore.R128.rows(new double[][] { { 0 } });
        GenericStore<Quadruple> AI = GenericStore.R128.rows(new double[][] { { -1.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, -1.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, -1.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, -1.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, -1.0 } });
        GenericStore<Quadruple> BI = GenericStore.R128
                .rows(new double[][] { { 0.199999999999 }, { 0.199999999999 }, { 0.199999999999 }, { 0.199999999999 }, { 0.199999999999 } });
        GenericStore<Quadruple> C = GenericStore.R128.rows(
                new double[][] { { -539445.3637318831 }, { -600689.6904218349 }, { -564117.362709679 }, { -729659.0017039739 }, { -1438811.2570210383 } });
        GenericStore<Quadruple> Q = GenericStore.R128.rows(new double[][] { { 41572.55, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 41572.55, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 41572.55, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 41572.55, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 41572.55 } });

        Optimisation.Result result = IterativeRefinementSolver.doSolve(Q, C, AE, BE, AI, BI, options);

        MatrixStore<Double> x = Primitive64Store.FACTORY.columns(result);
        MatrixStore<Double> y = Primitive64Store.FACTORY.columns(result.getMultipliers().get());

        double precision = IterativeRefinementTest.getResidualQuadruplePrecision(x, y, Q, C, AE, BE, AI, BI);
        TestUtils.assertLessThan(1e-15, precision);
    }

    @Test
    void testQP3() {

        Optimisation.Options options = new Optimisation.Options();
        options.sparse = false;
        options.convex().solverSPD(Cholesky.R064::make).solverGeneral(LU.R064::make).iterative(NumberContext.of(16));

        GenericStore<Quadruple> AE = GenericStore.R128
                .rows(new double[][] { { -0.5, -0.5, 0.0, 0.0, -5.763179164237471 }, { 0.0, 0.0, 0.0, 0.0, 1.0 }, { 0.0, 0.0, 0.0, 1.0, 0.0 },
                        { -3.0067917411849886E-5, 3.0067917411849886E-5, 0.5, 0.0, 0.0 }, { 3.0067917411849886E-5, -3.0067917411849886E-5, 0.5, 0.0, 0.0 } });
        GenericStore<Quadruple> BE = GenericStore.R128.rows(new double[][] { { 5763.179164237471 }, { -0.0 }, { -0.0 }, { -0.0 }, { 1.0 } });
        GenericStore<Quadruple> AI = GenericStore.R128.rows(new double[][] { { -0.0, -0.0, -1.0, -0.0, -0.0 } });
        GenericStore<Quadruple> BI = GenericStore.R128.rows(new double[][] { { 1.0 } });
        GenericStore<Quadruple> C = GenericStore.R128.rows(new double[][] { { -0.0 }, { -0.0 }, { -1.0 }, { -100000.0 }, { -1000.0 } });
        GenericStore<Quadruple> Q = GenericStore.R128.rows(new double[][] { { 1.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 1.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 1.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 1.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 1.0 } });
        Optimisation.Result result = IterativeRefinementSolver.doSolve(Q, C, AE, BE, AI, BI, options);
        MatrixStore<Double> x = Primitive64Store.FACTORY.columns(result);
        MatrixStore<Double> y = Primitive64Store.FACTORY.columns(result.getMultipliers().get());

        double precision = IterativeRefinementTest.getResidualQuadruplePrecision(x, y, Q, C, AE, BE, AI, BI);
        TestUtils.assertLessThan(1e-12, precision);
    }

    @Test
    void testQP4() {
        /**
         * A case where Quadruple solver and Double solver differ. Compare object function values,
         * absolute/relative primal end dual variable values.
         */

        GenericStore<Quadruple> AE = GenericStore.R128
                .rows(new double[][] { { 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 8.352297391034406E-10, 0.8576273045259193, 1.4176006647997381, 0.0, 0.582399335200262, 0.0 },
                        { 0.0, 0.0, 0.9999999991647702, 0.1423726954740807, 0.0, 1.4176006647997381, 0.0, 0.582399335200262 },
                        { 0.0, 0.0, 0.0, 0.0, -1.0, -1.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, -1.0 } });

        GenericStore<Quadruple> BE = GenericStore.R128
                .rows(new double[][] { { -0.0 }, { -0.0 }, { 5.184268104897471E-4 }, { -5.184268104898582E-4 }, { -0.0 }, { -0.0 } });

        GenericStore<Quadruple> AI = GenericStore.R128
                .rows(new double[][] { { -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0 }, { -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0 },
                        { -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0 }, { -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0 },
                        { -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0 }, { -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0 },
                        { -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0 }, { -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0 } });
        GenericStore<Quadruple> BI = GenericStore.R128.rows(new double[][] { { 99999.999 }, { 999.999 }, { 1.4176006647997381 }, { 0.582399335200262 },
                { 8.342297391034407E-10 }, { 0.9999999991637702 }, { 0.8576273045249193 }, { 0.1423726954730807 } });

        GenericStore<Quadruple> C = GenericStore.R128.rows(new double[][] { { -1.0507334266932648E-5 }, { 111.76733530105174 }, { 35972.35323772494 },
                { 3402.691149823142 }, { 241369.47835246887 }, { 39207.7768435451 }, { -4098.6473014047 }, { 4596.891294076649 } });

        GenericStore<Quadruple> Q = GenericStore.R128.rows(new double[][] {
                { 0.001000000000015139, -1.3922723188263615E-7, -1.260916429481889E-5, 1.1365393461671568E-10, 3.833998314967881E-6, -4.89115589330624E-6,
                        -6.213978880671885E-11, 1.3194143917892373E-11 },
                { -1.3922723188263615E-7, 1.2914061800397567, 111.98124430594436, 6.247515358606731, -290.69228055360566, 55.317474119999154,
                        -1.801267486375081, -2.6866831798306836 },
                { -1.260916429481889E-5, 111.98124430594436, 27050.45850812185, -0.010225003292636911, -170266.19960553516, -5763.903555279987,
                        -0.0036948253353555976, 0.010559458763784344 },
                { 1.136539346167157E-10, 6.247515358606731, -0.010225003292636918, 8301.539598682974, -2.495849052310504E-6, 2.367969836412835E-4,
                        1962.2096944694779, -6249.852185851846 },
                { 3.833998314967881E-6, -290.69228055360566, -170266.19960553516, -2.495849052310504E-6, 1.411187156545144E13, -61864.09308016598,
                        -5.160496246346226E-7, 8.324777613205293E-6 },
                { -4.89115589330624E-6, 55.317474119999154, -5763.903555279987, 2.367969836412835E-4, -61864.093080165985, 29507.042005253876,
                        -0.008194872881962646, 0.010231508973073053 },
                { -6.213978880671885E-11, -1.8012674863750813, -0.0036948253353555976, 1962.2096944694779, -5.160496246346225E-7, -0.008194872881962646,
                        8749.126719131198, -1004.5850092088822 },
                { 1.3194143917892375E-11, -2.686683179830683, 0.01055945876378435, -6249.852185851845, 8.324777613205291E-6, 0.010231508973073046,
                        -1004.5850092088823, 34337.13657031502 } });

        Optimisation.Options options = new Optimisation.Options();
        options.sparse = false;
        options.convex().extendedPrecision(true);
        options.convex().solverSPD(Cholesky.R064::make).solverGeneral(LU.R064::make).iterative(NumberContext.of(16));
        Optimisation.Result resultQuadruple = IterativeRefinementSolver.doSolve(Q, C, AE, BE, AI, BI, options);

        MatrixStore<Double> xQ = Primitive64Store.FACTORY.columns(resultQuadruple);
        MatrixStore<Double> yQ = Primitive64Store.FACTORY.columns(resultQuadruple.getMultipliers().get());

        double precisionQuadruple = IterativeRefinementTest.getResidualQuadruplePrecision(xQ, yQ, Q, C, AE, BE, AI, BI);
        //        TestUtils.assertLessThan(1e-15, precision);

        options.convex().extendedPrecision(false);
        ConvexSolver model = ConvexSolver.newBuilder().objective(Q, C).equalities(AE, BE).inequalities(AI, BI).build(options);
        Result resultDouble = model.solve();
        MatrixStore<Double> xD = Primitive64Store.FACTORY.columns(resultDouble);
        MatrixStore<Double> yD = Primitive64Store.FACTORY.columns(resultDouble.getMultipliers().get());
        double precisionDouble = IterativeRefinementTest.getResidualQuadruplePrecision(xD, yD, Q, C, AE, BE, AI, BI);
        double improvement = (resultDouble.getValue() - resultQuadruple.getValue()) / resultDouble.getValue();

        MatrixStore<Double> xAbsolute = xD.subtract(xQ);
        double largestAbsolutePrimalDiff = xAbsolute.aggregateAll(Aggregator.LARGEST).doubleValue();

        PhysicalStore<Double> xRelative = xQ.copy();
        xAbsolute.onMatching(DIVIDE_SAFE, xQ).supplyTo(xRelative);
        double largestRelativePrimalDiff = xRelative.aggregateAll(Aggregator.LARGEST).doubleValue();

        MatrixStore<Double> yAbsolute = yD.subtract(yQ);
        double largestAbsoluteDualDiff = yAbsolute.aggregateAll(Aggregator.LARGEST).doubleValue();

        PhysicalStore<Double> yRelative = yQ.copy();
        yAbsolute.onMatching(DIVIDE_SAFE, yQ).supplyTo(yRelative);
        double largestRelativeDualDiff = yRelative.aggregateAll(Aggregator.LARGEST).doubleValue();
    }

    public static final PrimitiveFunction.Binary DIVIDE_SAFE = (arg1, arg2) -> Math.abs(arg2) < 1e-6 ? 0 : arg1 / arg2;
}
