package org.ojalgo.optimisation.convex;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

/**
 * Basic correctness test comparing the null-space wrapper solver against the existing solver on a tiny
 * problem with both equalities and inequalities.
 */
public class NullSpaceASSTest extends OptimisationConvexTests {

    private static final NumberContext ACCURACY = NumberContext.of(12);

    private static ExpressionsBasedModel makeSmallMixedModel() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("x").lower(0).upper(10);
        Variable y = model.addVariable("y").lower(0).upper(10);
        Variable z = model.addVariable("z").lower(0).upper(10);

        // Equality
        Expression sum = model.addExpression("sum");
        sum.set(x, 1.0);
        sum.set(y, 1.0);
        sum.set(z, 1.0);
        sum.level(12.0);

        // Inequalities
        Expression c1 = model.addExpression("c1");
        c1.set(x, 1.0);
        c1.set(y, 2.0);
        c1.upper(14.0);
        Expression c2 = model.addExpression("c2");
        c2.set(y, 2.0);
        c2.set(z, 1.0);
        c2.upper(15.0);

        // Objective: 0.5*( (x-2)^2 + (y-3)^2 + (z-4)^2 )
        Expression obj = model.newExpression("obj");
        obj.setQuadraticFactors(model.getVariables(), R064Store.FACTORY.copy(RawStore.wrap(new double[][] { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } })));
        obj.set(x, -2.0);
        obj.set(y, -3.0);
        obj.set(z, -4.0);
        obj.weight(1.0); // minimise

        return model;
    }

    /**
     * Compares legacy active-set solver with experimental null-space eliminating solver on mixed E&I
     * problems. Focus: variable count reduction and iteration count (if available) plus objective similarity.
     */
    @Test
    public void testLegacyVsExperimentalSmall() {

        ExpressionsBasedModel model = NullSpaceASSTest.makeSmallMixedModel();

        // Legacy
        model.options.convex().projection(Boolean.FALSE);
        Optimisation.Result legacy = model.minimise();

        // Experimental
        model.options.convex().projection(Boolean.TRUE);
        Optimisation.Result experimental = model.minimise();

        TestUtils.assertTrue(legacy.getState().isFeasible());
        TestUtils.assertTrue(experimental.getState().isFeasible());
        TestUtils.assertEquals(legacy.getValue(), experimental.getValue(), ACCURACY);
    }

    @Test
    public void testSimple() {

        int n = 3;
        int mE = 2;
        int mI = 6;

        ConvexData<Double> data = new ConvexData<>(false, R064Store.FACTORY, n, mE, mI);
        for (int i = 0; i < n; i++) {
            data.setObjective(i, i, ONE); // Q diag = 1
        }
        data.setObjective(0, ONE);
        data.setObjective(1, TWO);
        data.setObjective(2, THREE);
        // AE
        data.setAE(0, 0, ONE);
        data.setAE(0, 1, ONE);
        data.setBE(0, THREE);
        data.setAE(1, 1, ONE);
        data.setAE(1, 2, ONE);
        data.setBE(1, FIVE);
        // Inequalities -x <= 0 and x <= 10
        for (int i = 0; i < n; i++) {
            data.setAI(i, i, -ONE);
            data.setBI(i, ZERO);
        }
        for (int i = 0; i < n; i++) {
            data.setAI(n + i, i, ONE);
            data.setBI(n + i, TEN);
        }

        Optimisation.Options options = new Optimisation.Options();
        Result expected = BasePrimitiveSolver.newSolver(data, options).solve();
        Result actual = new NullSpaceASS(options, data).solve();

        TestUtils.assertEquals(expected.getState(), actual.getState());
        // TestUtils.assertEquals(reference.getValue(), test.getValue(), ACCURACY);
        for (int i = 0; i < n; i++) {
            TestUtils.assertEquals(expected.doubleValue(i), actual.doubleValue(i), ACCURACY);
        }
        // Check equality residual of reconstructed solution
        double e0 = actual.doubleValue(0) + actual.doubleValue(1) - THREE;
        double e1 = actual.doubleValue(1) + actual.doubleValue(2) - FIVE;
        TestUtils.assertEquals(ZERO, e0, ACCURACY);
        TestUtils.assertEquals(ZERO, e1, ACCURACY);
    }

}
