package org.ojalgo.optimisation;

import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.Sense;
import org.ojalgo.optimisation.linear.LinearSolver;

public final class OptimisationCase {

    private static final boolean DEBUG = false;

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

    public Optimisation.Result assertResult() {

        if (DEBUG) {
            model.options.debug(LinearSolver.class);
        }

        Optimisation.Result actual = sense.solve(model);
        TestUtils.assertResult(result, actual);
        return actual;
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
