package org.ojalgo.optimisation.linear;

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
import org.ojalgo.type.keyvalue.KeyValue;

/**
 * A set of basic LP test models to test things any/all LP solver should be able to handle. Just small models
 * with no extreme numerical difficulties.
 */
public class TestBasicLP extends OptimisationLinearTests implements TestBasic {

    /**
     * Infeasible LP: x + y >= 10 and x + y <= 5 (contradictory)
     */
    static OptimisationCase caseInfeasibleLP() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("x").lower(0.0);
        Variable y = model.addVariable("y").lower(0.0);

        model.addExpression("c1").set(x, 1.0).set(y, 1.0).lower(10.0);
        model.addExpression("c2").set(x, 1.0).set(y, 1.0).upper(5.0);

        x.weight(1.0);

        Optimisation.Result result = Optimisation.Result.of(State.INFEASIBLE);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result);
    }

    /**
     * LP with mixed equalities and bounds: minimize x + 2y subject to x - y = 0, 3x + 2y <= 4, with bounds x
     * ∈ [0, 2], y ∈ [-1, 1]. The optimal solution is at x = y = 0 with objective value 0.
     */
    static OptimisationCase caseMixedEqualitiesAndBounds() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("x").lower(0.0).upper(2.0).weight(1.0);
        Variable y = model.addVariable("y").lower(-1.0).upper(1.0).weight(2.0);

        model.addExpression("eq").set(x, 1.0).set(y, -1.0).level(0.0);
        model.addExpression("ineq").set(x, 3.0).set(y, 2.0).upper(4.0);

        Optimisation.Result result = Optimisation.Result.of(0.0, State.OPTIMAL, 0.0, 0.0);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result);
    }

    static OptimisationCase casePureBoundedLP() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("X").lower(0.0).upper(5.0).weight(1.0);
        Variable y = model.addVariable("Y").lower(1.0).upper(4.0).weight(2.0);

        Expression c1 = model.addExpression("c1");
        c1.set(x, 1.0);
        c1.set(y, 1.0);
        c1.upper(6.0);

        Optimisation.Result result = Optimisation.Result.parse("OPTIMAL 10.0 @ { 2, 4 }");

        return OptimisationCase.of(model, Optimisation.Sense.MAX, result);
    }

    /**
     * Simple LP: minimize x + 2y subject to x + y >= 1, x >= 0, y >= 0. The unique optimal solution is at x =
     * 1, y = 0 with objective value 1.
     */
    static OptimisationCase caseSimpleLP() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("x").lower(0.0).weight(1.0);
        Variable y = model.addVariable("y").lower(0.0).weight(2.0);

        model.addExpression("c1").set(x, 1.0).set(y, 1.0).lower(1.0);

        Optimisation.Result result = Optimisation.Result.of(1.0, State.OPTIMAL, 1.0, 0.0);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result);
    }

    /**
     * Simple LP maximisation: maximize 3x + 2y subject to x + y <= 4, x <= 2, y <= 3
     */
    static OptimisationCase caseSimpleLPMaximise() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("x").lower(0.0).upper(2.0).weight(3.0);
        Variable y = model.addVariable("y").lower(0.0).upper(3.0).weight(2.0);

        model.addExpression("c1").set(x, 1.0).set(y, 1.0).upper(4.0);

        Optimisation.Result result = Optimisation.Result.of(10.0, State.OPTIMAL, 2.0, 2.0);

        return OptimisationCase.of(model, Optimisation.Sense.MAX, result);
    }

    /**
     * Unbounded LP: minimize -x subject to x >= 0, no upper bound. The problem is unbounded since x can
     * increase without limit. ojAlgo returns UNBOUNDED with a feasible solution.
     */
    static OptimisationCase caseUnboundedLP() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("x").lower(0.0).weight(-1.0);

        Optimisation.Result result = Optimisation.Result.of(State.UNBOUNDED, 0.0);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result);
    }

    @Test
    public void testInfeasibleLP() {
        OptimisationCase testCase = TestBasicLP.caseInfeasibleLP();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testMixedEqualitiesAndBounds() {
        OptimisationCase testCase = TestBasicLP.caseMixedEqualitiesAndBounds();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testPureBoundedLP() {
        OptimisationCase testCase = TestBasicLP.casePureBoundedLP();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testSimpleLP() {
        OptimisationCase testCase = TestBasicLP.caseSimpleLP();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testSimpleLPMaximise() {
        OptimisationCase testCase = TestBasicLP.caseSimpleLPMaximise();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testUnboundedLP() {
        OptimisationCase testCase = TestBasicLP.caseUnboundedLP();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    protected List<ExpressionsBasedModel.Integration<?>> integrations() {
        return List.of(INTEGRATIONS.stream().map(KeyValue::getValue).toArray(ExpressionsBasedModel.Integration<?>[]::new));
    }

}