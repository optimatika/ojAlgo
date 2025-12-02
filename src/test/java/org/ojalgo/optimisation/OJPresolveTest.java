package org.ojalgo.optimisation;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class OJPresolveTest extends OptimisationTests {

    static final class DummyIntegration extends ExpressionsBasedModel.Integration<DummySolver> {

        @Override
        public DummySolver build(final ExpressionsBasedModel model) {
            return new DummySolver();
        }

        @Override
        public boolean isCapable(final ExpressionsBasedModel model) {
            return true;
        }

    }

    static final class DummySolver implements Optimisation.Solver {

        @Override
        public Result solve(final Result kickStarter) {
            return Result.of(State.OPTIMAL, 0, 1, 2);
        }

    }

    private static ExpressionsBasedModel getModel() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x1 = model.addVariable("x1");
        Variable x2 = model.addVariable("x2");
        Variable x3 = model.addVariable("x3");

        x1.upper(1).lower(0);
        x2.upper(1).lower(0);
        x3.upper(1).lower(0);

        Expression quadExpr = model.addExpression("QuadraticExpression");
        quadExpr.set(x1, x1, 0.1);
        quadExpr.set(x2, x2, 0.1);
        quadExpr.set(x3, x3, 0.1);
        quadExpr.set(x1, -0.0649319296487038);
        quadExpr.set(x2, -0.035049269354063314);
        quadExpr.set(x3, -0.054569655903981634);

        quadExpr.upper(new BigDecimal("-0.02"));

        return model;
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/647
     * <p>
     * Model presolve incorrectly marked a model as infeasible
     */
    @Test
    public void testQuadraticConstraintNotMarkedInfeasibleByPresolve() {

        ExpressionsBasedModel.addIntegration(new DummyIntegration());

        ExpressionsBasedModel modelWithPresolve = OJPresolveTest.getModel();
        Optimisation.Result withPresolve = modelWithPresolve.minimise();

        TestUtils.assertTrue(withPresolve.getState().isFeasible());

        ExpressionsBasedModel.clearPresolvers();

        ExpressionsBasedModel modelNoPresolve = OJPresolveTest.getModel();
        Optimisation.Result withoutPresolve = modelNoPresolve.minimise();

        TestUtils.assertTrue(withoutPresolve.getState().isFeasible());

        ExpressionsBasedModel.clearIntegrations();
        ExpressionsBasedModel.resetPresolvers();
    }

    @AfterEach
    void cleanUp() {
        ExpressionsBasedModel.clearIntegrations();
        ExpressionsBasedModel.resetPresolvers();
    }

}
