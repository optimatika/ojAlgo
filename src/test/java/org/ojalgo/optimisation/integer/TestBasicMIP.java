package org.ojalgo.optimisation.integer;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ExpressionsBasedModel.Integration;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.OptimisationCase;
import org.ojalgo.optimisation.TestBasic;
import org.ojalgo.optimisation.Variable;

/**
 * A set of basic MIP test models to test things any/all MIP solver should be able to handle. Just small
 * models with no extreme numerical difficulties.
 */
public class TestBasicMIP extends OptimisationIntegerTests implements TestBasic {

    /**
     * Simple MIP: minimize x + 2y subject to x + y >= 1, x, y binary. The optimal solution is x = 1, y = 0
     * with objective value 1.
     */
    static OptimisationCase caseSimpleBinaryMIP() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("x").binary().weight(1.0);
        Variable y = model.addVariable("y").binary().weight(2.0);

        model.addExpression("c1").set(x, 1.0).set(y, 1.0).lower(1.0);

        Optimisation.Result result = Optimisation.Result.of(1.0, State.OPTIMAL, 1.0, 0.0);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result);
    }

    @Test
    public void testSimpleBinaryMIP() {
        OptimisationCase testCase = TestBasicMIP.caseSimpleBinaryMIP();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    protected List<ExpressionsBasedModel.Integration<?>> integrations() {
        return List.of(IntegerSolver.INTEGRATION);
    }

}
