package org.ojalgo.optimisation;

import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.Sense;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.type.context.NumberContext;

public final class OptimisationCase {

    private static final NumberContext ACCURACY = NumberContext.of(6);

    public static OptimisationCase of(final ExpressionsBasedModel model, final Result result) {
        return new OptimisationCase(model, Optimisation.Sense.MIN, result, ACCURACY, false);
    }

    public static OptimisationCase of(final ExpressionsBasedModel model, final Sense sense, final Result result) {
        return new OptimisationCase(model, sense, result, ACCURACY, false);
    }

    public final ExpressionsBasedModel model;
    public final Optimisation.Result result;
    public final Optimisation.Sense sense;
    private final NumberContext myAccuracy;
    private final boolean myDebug;

    OptimisationCase(final ExpressionsBasedModel model, final Sense sense, final Result result, final NumberContext accuracy, final boolean debug) {
        super();
        this.model = model;
        this.sense = sense;
        this.result = result;
        myAccuracy = accuracy;
        myDebug = debug;
    }

    public OptimisationCase accuracy(final NumberContext accuracy) {
        return new OptimisationCase(model, sense, result, accuracy, myDebug);
    }

    public Optimisation.Result assertResult() {

        if (myDebug) {
            model.options.debug(Optimisation.Solver.class);
        }

        Optimisation.Result actual = sense.solve(model);
        this.doAssertResult(actual);
        return actual;
    }

    public Optimisation.Result assertResult(final ExpressionsBasedModel.Integration<?> integration) {

        if (myDebug) {
            model.options.debug(Optimisation.Solver.class);
        }

        Optimisation.Result actual = sense.solve(model, integration);
        this.doAssertResult(actual);
        return actual;
    }

    public OptimisationCase debug(final boolean debug) {
        return new OptimisationCase(model, sense, result, myAccuracy, debug);
    }

    private void doAssertResult(final Optimisation.Result actual) {

        State expectedState = result.getState();

        if (expectedState.isOptimal()) {
            // For optimal results: compare state, value, solution and multipliers
            TestUtils.assertResult(result, actual, myAccuracy);
        } else if (expectedState == State.UNBOUNDED) {
            // For unbounded: ojAlgo provides a feasible solution, so compare state and solution
            TestUtils.assertStateAndSolution(result, actual, myAccuracy);
        } else if (expectedState.isFeasible()) {
            // For feasible (but not optimal) results: compare state and solution, not value
            TestUtils.assertStateAndSolution(result, actual, myAccuracy);
        } else if (expectedState == State.INFEASIBLE) {
            // For infeasible: just verify the result indicates failure, no solution expected
            TestUtils.assertTrue("Expected INFEASIBLE but got " + actual.getState(), actual.getState().isFailure());
        } else {
            // For other states (INVALID, FAILED, UNEXPLORED): just compare states
            TestUtils.assertEquals("Unexpected state", expectedState, actual.getState());
        }
    }

}
