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
     * Minimise x² + y² - 1.2x - 2.8y with integer variables and bounds [0, 5]. The continuous optimum is at
     * (0.6, 1.4), and the unique integer optimum is at (1, 1) with objective value -2.
     */
    static OptimisationCase caseSimpleIntegerQP() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("X").lower(0).upper(5).integer(true);
        Variable y = model.addVariable("Y").lower(0).upper(5).integer(true);

        Expression objective = model.addExpression("obj");
        objective.set(x, x, 1.0);
        objective.set(y, y, 1.0);
        objective.set(x, -1.2);
        objective.set(y, -2.8);
        objective.weight(1.0);

        Optimisation.Result result = Optimisation.Result.of(-2.0, State.OPTIMAL, 1, 1);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result).accuracy(NumberContext.of(6));
    }

    /**
     * Minimise weight * (x'Qx / 2 + c'x) with 5 integer variables in [0, 5] and one knapsack constraint. The
     * integer optimum, verified by enumeration, is x = (2, 1, 0, 1, 0) with x'Qx / 2 + c'x = -8.735 regardless
     * of the weight. The weight changes the objective's adjustment (scaling) factor, which must not affect the
     * node values the branch-and-bound compares.
     */
    static OptimisationCase caseWeightedIntegerQP(final double weight) {

        double[][] mtrxQ = { { 3.42, -1.4, -0.74, -3.1, -0.82 }, { -1.4, 2.07, 2.23, 2.03, 0.26 }, { -0.74, 2.23, 4.6, 1.92, -0.07 },
                { -3.1, 2.03, 1.92, 4.12, 0.79 }, { -0.82, 0.26, -0.07, 0.79, 0.75 } };
        double[] mtrxC = { -3.0, -3.3, -1.4, -2.4, -1.0 };
        int[] knapsackWeights = { 3, 1, 2, 1, 1 };

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable[] x = new Variable[mtrxC.length];
        for (int i = 0; i < x.length; i++) {
            x[i] = model.addVariable("X" + i).lower(0).upper(5).integer(true);
        }

        Expression objective = model.addExpression("obj").weight(weight);
        Expression knapsack = model.addExpression("knapsack").upper(8);
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x.length; j++) {
                objective.set(x[i], x[j], mtrxQ[i][j] / 2.0);
            }
            objective.set(x[i], mtrxC[i]);
            knapsack.set(x[i], knapsackWeights[i]);
        }

        Optimisation.Result result = Optimisation.Result.of(weight * -8.735, State.OPTIMAL, 2, 1, 0, 1, 0);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result).accuracy(NumberContext.of(6));
    }

    @Test
    public void testSimpleIntegerQP() {
        OptimisationCase testCase = TestBasicMIQP.caseSimpleIntegerQP();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testWeightedIntegerQP() {
        for (double weight : new double[] { 0.001, 1.0, 1000.0 }) {
            OptimisationCase testCase = TestBasicMIQP.caseWeightedIntegerQP(weight);
            for (Integration<?> integration : this.integrations()) {
                testCase.assertResult(integration);
            }
        }
    }

    protected List<ExpressionsBasedModel.Integration<?>> integrations() {
        return List.of(IntegerSolver.INTEGRATION);
    }

}
