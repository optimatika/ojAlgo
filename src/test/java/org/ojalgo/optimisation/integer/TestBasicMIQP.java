package org.ojalgo.optimisation.integer;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ExpressionsBasedModel.Integration;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.OptimisationCase;
import org.ojalgo.optimisation.TestBasic;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

/**
 * A set of basic MIQP (Mixed Integer QP) test models to test things any/all MIQP solver should be able to
 * handle. Just small models with no extreme numerical difficulties.
 */
public class TestBasicMIQP extends OptimisationIntegerTests implements TestBasic {

    /**
     * Minimise x² + y² - x - 3y with integer variables and bounds [0, 5]. The continuous optimum is at (0.5,
     * 1.5), and the integer optimum is at (1, 1) with objective value -2.
     */
    static OptimisationCase caseSimpleIntegerQP() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("X").lower(0).upper(5).integer(true);
        Variable y = model.addVariable("Y").lower(0).upper(5).integer(true);

        Expression objective = model.addExpression("obj");
        objective.set(x, x, 1.0);
        objective.set(y, y, 1.0);
        objective.set(x, -1.0);
        objective.set(y, -3.0);
        objective.weight(1.0);

        Optimisation.Result result = Optimisation.Result.of(-2.0, State.OPTIMAL, 1, 1);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result).accuracy(NumberContext.of(6));
    }

    @Test
    public void testSimpleIntegerQP() {
        OptimisationCase testCase = TestBasicMIQP.caseSimpleIntegerQP();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    protected List<ExpressionsBasedModel.Integration<?>> integrations() {
        return List.of(IntegerSolver.INTEGRATION);
    }

}
