package org.ojalgo.optimisation.integer;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ExpressionsBasedModel.Integration;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * Verifies that {@code time_suffice} / {@code time_abort} work correctly: the solver returns a feasible
 * solution without running all the way to the abort limit.
 * <p>
 * Uses a synthetic minimum-weight vertex cover on a random graph. Any solver finds a feasible cover almost
 * immediately (selecting all vertices works), but the LP relaxation has an integrality gap of 2, making
 * proving integer optimality expensive. The model is sized to stay within solver community-edition limits
 * (under 1000 constraints).
 */
public class TimeSufficeTest extends OptimisationIntegerTests {

    static final long TIME_ABORT_MILLIS = 120_000L;
    static final long TIME_SUFFICE_MILLIS = 5_000L;

    static ExpressionsBasedModel newHardToProveOptimalModel() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        int nbVertices = 300;
        int nbEdges = 900;
        Random rng = new Random(42L);

        Variable[] variables = new Variable[nbVertices];
        for (int j = 0; j < nbVertices; j++) {
            variables[j] = model.addVariable("v" + j).binary().weight(rng.nextInt(90) + 10);
        }

        for (int e = 0; e < nbEdges; e++) {
            int u = rng.nextInt(nbVertices);
            int w;
            do {
                w = rng.nextInt(nbVertices);
            } while (w == u);

            Expression edge = model.addExpression("edge" + e);
            edge.set(variables[u], 1);
            edge.set(variables[w], 1);
            edge.lower(1);
        }

        return model;
    }

    @Test
    public void testTimeSuffice() {

        List<Integration<?>> integrations = this.integrations();

        for (Integration<?> integration : integrations) {

            ExpressionsBasedModel model = TimeSufficeTest.newHardToProveOptimalModel();

            model.options.time_suffice = TIME_SUFFICE_MILLIS;
            model.options.time_abort = TIME_ABORT_MILLIS;

            long before = System.currentTimeMillis();
            Optimisation.Result result = Optimisation.Sense.MIN.solve(model, integration);
            long elapsed = System.currentTimeMillis() - before;

            Assertions.assertTrue(result.getState().isFeasible(), integration + ": Expected a feasible solution but got " + result.getState());

            Assertions.assertTrue(elapsed < TIME_ABORT_MILLIS / 2, integration + ": Solve took " + elapsed + "ms — should finish well before the abort limit");
        }
    }

    protected List<Integration<?>> integrations() {
        return List.of(IntegerSolver.INTEGRATION);
    }

}
