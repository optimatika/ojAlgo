package org.ojalgo.optimisation.conic;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

public class ConicSolverTest {

    @Test
    public void solvesSimpleLP() {
        // minimise x + y subject to x + y = 1, x>=0, y>=0
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.addVariable("x").lower(0).weight(1.0);
        Variable y = model.addVariable("y").lower(0).weight(1.0);
        model.addExpression("sum").set(x, 1).set(y, 1).level(1.0);

        Optimisation.Result baseline = model.minimise();
        final ExpressionsBasedModel model1 = model;

        ConicSolver solver = ConicSolver.INTEGRATION.build(model1); // translate & solve with outline
        Optimisation.Result result = solver.solve();

        // Basic feasibility & objective checks
        TestUtils.assertTrue(result.getState().isFeasible() || result.getState().isApproximate());
        NumberContext tol = NumberContext.of(7, 6);
        double objBaseline = baseline.getValue();
        double objOutline = result.getValue();
        // Both objectives should be close to 1.0 (min possible)
        TestUtils.assertEquals(1.0, tol.enforce(objBaseline));
        TestUtils.assertEquals(1.0, tol.enforce(objOutline));
    }

}