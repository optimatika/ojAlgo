package org.ojalgo.optimisation.convex;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

/**
 * Compares legacy active-set solver with experimental null-space eliminating solver on mixed E&I problems.
 * Focus: variable count reduction and iteration count (if available) plus objective similarity.
 */
public class ExperimentalNullSpaceComparisonTest {

    @Test
    public void testLegacyVsExperimentalSmall() {
        ExpressionsBasedModel model = this.makeSmallMixedModel();
        NumberContext accuracy = NumberContext.of(6);
        // Legacy
        model.options.experimental = false;
        Optimisation.Result legacy = model.minimise();
        // Experimental
        model.options.experimental = true;
        Optimisation.Result experimental = model.minimise();
        TestUtils.assertTrue(legacy.getState().isFeasible());
        TestUtils.assertTrue(experimental.getState().isFeasible());
        TestUtils.assertEquals(legacy.getValue(), experimental.getValue(), accuracy);
    }

    private ExpressionsBasedModel makeSmallMixedModel() {
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

}