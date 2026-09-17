package org.ojalgo.optimisation.integer;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ExpressionsBasedModel.Integration;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
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
     * <pre>
     * min  -2*x0 - 3*x1 + x2 - 5*x3
     * s.t. 2*x0 + 3*x1 +  x2 + 2*x3 = 17    (equality)
     *      3*x0 +  x1        + 2*x3 <= 9      (inequality 0)
     *       x0       + 2*x2 + 3*x3 <= 8       (inequality 1)
     *      0 <= x0 <= 4
     *      1 <= x1 <= 5
     *      0 <= x2 <= 3
     *      0 <= x3 <= 3
     *      x0, x1, x2, x3 integer)
     * </pre>
     */
    public static OptimisationCase caseBranchAndBoundSubSolverTest() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x0 = model.newVariable("x0").lower(0).upper(4).weight(-2).integer();
        Variable x1 = model.newVariable("x1").lower(1).upper(5).weight(-3).integer();
        Variable x2 = model.newVariable("x2").lower(0).upper(3).weight(1).integer();
        Variable x3 = model.newVariable("x3").lower(0).upper(3).weight(-5).integer();

        Expression equality = model.newExpression("equality").lower(17).upper(17);
        equality.set(x0, 2);
        equality.set(x1, 3);
        equality.set(x2, 1);
        equality.set(x3, 2);

        Expression ineq0 = model.newExpression("ineq0").upper(9);
        ineq0.set(x0, 3);
        ineq0.set(x1, 1);
        ineq0.set(x3, 2);

        Expression ineq1 = model.newExpression("ineq1").upper(8);
        ineq1.set(x0, 1);
        ineq1.set(x2, 2);
        ineq1.set(x3, 3);

        Result result = Optimisation.Result.of(-21, State.OPTIMAL, 0, 4, 1, 2);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result);
    }

    /**
     * GitHub issue #682: A feasible binary model incorrectly reported as INFEASIBLE when GMI root cuts are
     * active. Four binary variables, three constraints. Known optimal maximisation solution is
     * {alternative=0, first=1, second=1, active=1} with objective 10.
     */
    static OptimisationCase caseGitHub682() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable alternative = model.newVariable("alternative").binary().weight(7);
        Variable first = model.newVariable("first").binary().weight(3);
        Variable second = model.newVariable("second").binary().weight(7);
        Variable active = model.newVariable("active").binary();

        model.addExpression("group-min").lower(0).set(first, 1).set(second, 1).set(active, -2);
        model.addExpression("group-max").upper(0).set(first, 1).set(second, 1).set(active, -10);
        model.addExpression("choose-one").level(1).set(alternative, 1).set(first, 1);

        Result result = Result.of(10.0, State.OPTIMAL, 0.0, 1.0, 1.0, 1.0);

        return OptimisationCase.of(model, Optimisation.Sense.MAX, result);
    }

    /**
     * MIP with integer and continuous variables where the objective references only integer variables with
     * fractional coefficients. {@code getIntegerStep()} on the objective returns 0.5 (GCD of 1.5 and 0.5).
     * <p>
     * minimize 1.5*x1 + 0.5*x2 subject to x1 + x2 >= 3, x1 + y1 = 4, x2 + y2 = 5, all >= 0, x1 and x2
     * integer. Optimal: x1=0, x2=3, y1=4, y2=2 with objective value 1.5.
     */
    static OptimisationCase caseMixedIntegerContinuous() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x1 = model.addVariable("x1").lower(0.0).integer().weight(1.5);
        Variable x2 = model.addVariable("x2").lower(0.0).integer().weight(0.5);
        Variable y1 = model.addVariable("y1").lower(0.0);
        Variable y2 = model.addVariable("y2").lower(0.0);

        model.addExpression("intReq").set(x1, 1.0).set(x2, 1.0).lower(3.0);
        model.addExpression("link1").set(x1, 1.0).set(y1, 1.0).level(4.0);
        model.addExpression("link2").set(x2, 1.0).set(y2, 1.0).level(5.0);

        Result result = Result.of(1.5, State.OPTIMAL, 0.0, 3.0, 4.0, 2.0);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result);
    }

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
    public void testBranchAndBoundSubSolverTest() {
        OptimisationCase testCase = TestBasicMIP.caseBranchAndBoundSubSolverTest();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testGitHub682() {
        OptimisationCase testCase = TestBasicMIP.caseGitHub682();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testMixedIntegerContinuous() {

        OptimisationCase testCase = TestBasicMIP.caseMixedIntegerContinuous();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
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
