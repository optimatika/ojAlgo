package org.ojalgo.optimisation.integer;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

/**
 * Tests GMI cut generation with non-basic integer variables at their upper bounds.
 * <p>
 * Model: max 5*x1 + 4*x2 + 3*x3 subject to 2*x1 + 3*x2 + x3 <= 4, all binary.
 * <p>
 * LP relaxation optimal: x1=1 (at UB), x2=2/3 (fractional, basic), x3=1 (at UB). MIP optimal: x1=1, x2=0,
 * x3=1, obj=8.
 * <p>
 * The GMI cut from x2's fractional value involves non-basic variables x1 and x3 at their upper bounds. Before
 * the fix, RevisedStore used {@code uppers[j] <= 0} for negVar determination, which is wrong for unshifted
 * variables (binary UB=1 gives negVar=false when the variable IS at its upper bound).
 */
public class GMICutDiagnosticTest extends OptimisationIntegerTests {

    private static final NumberContext ACCURACY = NumberContext.of(8);

    private static void doTest(final ExpressionsBasedModel model) {

        Result expected = Result.of(8.0, Optimisation.State.OPTIMAL, 1, 0, 1);

        model.options.validate = true;

        Result result = model.maximise();

        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertResult(expected, result, ACCURACY);
    }

    static ExpressionsBasedModel makeModel() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x1 = model.newVariable("x1").binary().weight(5);
        Variable x2 = model.newVariable("x2").binary().weight(4);
        Variable x3 = model.newVariable("x3").binary().weight(3);

        model.addExpression("c1").upper(4).set(x1, 2).set(x2, 3).set(x3, 1);

        return model;
    }

    @Test
    public void testDualDense() {
        ExpressionsBasedModel model = GMICutDiagnosticTest.makeModel();
        model.options.linear().dual();
        model.options.sparse = Boolean.FALSE;
        GMICutDiagnosticTest.doTest(model);
    }

    @Test
    public void testDualSparse() {
        ExpressionsBasedModel model = GMICutDiagnosticTest.makeModel();
        model.options.linear().dual();
        model.options.sparse = Boolean.TRUE;
        GMICutDiagnosticTest.doTest(model);
    }

    @Test
    public void testPrimalDense() {
        ExpressionsBasedModel model = GMICutDiagnosticTest.makeModel();
        model.options.linear().primal();
        model.options.sparse = Boolean.FALSE;
        GMICutDiagnosticTest.doTest(model);
    }

    @Test
    public void testPrimalSparse() {
        ExpressionsBasedModel model = GMICutDiagnosticTest.makeModel();
        model.options.linear().primal();
        model.options.sparse = Boolean.TRUE;
        GMICutDiagnosticTest.doTest(model);
    }

}
