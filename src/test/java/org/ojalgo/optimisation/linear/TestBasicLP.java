package org.ojalgo.optimisation.linear;

import org.junit.jupiter.api.Test;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ExpressionsBasedModel.Integration;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.OptimisationCase;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.keyvalue.KeyValue;

/**
 * A set of basic LP test models to test things any/all LP solver should be able to handle. Just small models
 * with no extreme numerical difficulties.
 */
public class TestBasicLP extends OptimisationLinearTests {

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

    @Test
    void testPureBoundedLP() {
        OptimisationCase testCase = TestBasicLP.casePureBoundedLP();
        for (KeyValue<String, Integration<LinearSolver>> integration : INTEGRATIONS) {
            testCase.assertResult(integration.getValue());
        }
    }
}
