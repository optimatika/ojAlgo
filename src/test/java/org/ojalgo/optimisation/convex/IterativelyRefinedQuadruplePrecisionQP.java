package org.ojalgo.optimisation.convex;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.constant.QuadrupleMath;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.type.context.NumberContext;

public class IterativelyRefinedQuadruplePrecisionQP extends OptimisationConvexTests {

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
        double relativeComplementarySlackness2 = y.onMatching(QuadrupleMath.MULTIPLY, (be1.below(bi1))).collect(GenericStore.R128)
                .aggregateAll(Aggregator.LARGEST).doubleValue() / be_Size;
        double relativeComplementarySlackness = Math.max(relativeComplementarySlackness1, relativeComplementarySlackness2 * relativeComplementarySlackness2);
        Quadruple objectiveValue = Q.multiplyBoth(x).divide(2).subtract(x.transpose().multiply(C).get(0));
        return Math.max(relativeComplementarySlackness, Math.max(maxPrimalResidual, relativeGradientResidual));
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

        double precision = IterativelyRefinedQuadruplePrecisionQP.getResidualQuadruplePrecision(x, y, Q, C, AE, BE, AI, BI);
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

        double precision = IterativelyRefinedQuadruplePrecisionQP.getResidualQuadruplePrecision(x, y, Q, C, AE, BE, AI, BI);
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

        double precision = IterativelyRefinedQuadruplePrecisionQP.getResidualQuadruplePrecision(x, y, Q, C, AE, BE, AI, BI);
        TestUtils.assertLessThan(1e-12, precision);
    }

}
