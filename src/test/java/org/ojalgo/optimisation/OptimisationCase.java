package org.ojalgo.optimisation;

import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.Sense;

public final class OptimisationCase {

    public static OptimisationCase of(final ExpressionsBasedModel model, final Sense sense, final Result result) {
        return new OptimisationCase(model, sense, result);
    }

    public final ExpressionsBasedModel model;
    public final Optimisation.Result result;
    public final Optimisation.Sense sense;

    OptimisationCase(final ExpressionsBasedModel model, final Sense sense, final Result result) {
        super();
        this.model = model;
        this.sense = sense;
        this.result = result;
    }

    public void assertResult() {
        TestUtils.assertResult(result, sense.solve(model));
    }

    public ExpressionsBasedModel model() {
        return model;
    }

    public Optimisation.Result result() {
        return result;
    }

    public Optimisation.Sense sense() {
        return sense;
    }

}