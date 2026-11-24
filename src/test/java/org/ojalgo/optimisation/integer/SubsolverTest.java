/*
 * Copyright 1997-2026 Optimatika
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.ojalgo.optimisation.integer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.UpdatableSolver;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.linear.LinearSolver;

/**
 * The {@link IntegerSolver} delegates to some other solver to solve the node problems, and should work with
 * any of them: whether it can be updated in place or not, and whatever (optional) things it provides.
 */
public class SubsolverTest extends OptimisationIntegerTests {

    /**
     * Solves with a {@link LinearSolver}, but does no more than a solver has to
     */
    private static class DelegatingSolver implements Optimisation.Solver {

        final LinearSolver delegate;
        private final Behaviour myBehaviour;
        /**
         * Solved, and not updated since then
         */
        boolean solved = false;

        DelegatingSolver(final LinearSolver solver, final Behaviour behaviour) {
            super();
            delegate = solver;
            myBehaviour = behaviour;
        }

        @Override
        public Optimisation.Result solve(final Optimisation.Result kickStarter) {
            Optimisation.Result result = delegate.solve(kickStarter);
            solved = result.getState().isOptimal();
            if (!solved && myBehaviour.emptyWhenFailed) {
                return new Optimisation.Result(result.getState(), ArrayR064.make(0));
            }
            return result;
        }

    }

    private static final class DelegatingUpdatableSolver extends DelegatingSolver implements UpdatableSolver {

        private final Behaviour myBehaviour;

        DelegatingUpdatableSolver(final LinearSolver solver, final Behaviour behaviour) {
            super(solver, behaviour);
            myBehaviour = behaviour;
        }

        @Override
        public Optional<ExpressionsBasedModel.EntityMap> getEntityMap() {
            return delegate.getEntityMap();
        }

        @Override
        public double getDualMultiplier(final int index) {
            return delegate.getDualMultiplier(index);
        }

        @Override
        public double getReducedGradient(final int index) {
            if (!myBehaviour.reducedGradient) {
                throw new UnsupportedOperationException();
            }
            if (!solved) {
                throw new IllegalStateException();
            }
            return delegate.getReducedGradient(index);
        }

        @Override
        public boolean updateRange(final int index, final double lower, final double upper) {
            solved = false;
            return delegate.updateRange(index, lower, upper);
        }

    }

    private static final class Behaviour extends ExpressionsBasedModel.Integration<DelegatingSolver> {

        final boolean emptyWhenFailed;
        final boolean reducedGradient;
        private final boolean myUpdatable;

        /**
         * @param updatable       Can it be updated in place?
         * @param reducedGradient Does it provide the reduced gradient (when it can be updated)? Never after an
         *                        update, until it's solved again.
         * @param emptyWhenFailed Are there no values at all when there is no solution?
         */
        Behaviour(final boolean updatable, final boolean reducedGradient, final boolean emptyWhenFailed) {
            super();
            myUpdatable = updatable;
            this.reducedGradient = reducedGradient;
            this.emptyWhenFailed = emptyWhenFailed;
        }

        @Override
        public DelegatingSolver build(final ExpressionsBasedModel model) {
            LinearSolver solver = LinearSolver.INTEGRATION.build(model);
            return myUpdatable ? new DelegatingUpdatableSolver(solver, this) : new DelegatingSolver(solver, this);
        }

        @Override
        public boolean isCapable(final ExpressionsBasedModel model) {
            return LinearSolver.INTEGRATION.isCapable(model);
        }

        @Override
        public Optimisation.Result toModelState(final Optimisation.Result solverState, final ExpressionsBasedModel model) {
            return solverState.size() > 0 ? LinearSolver.INTEGRATION.toModelState(solverState, model) : solverState;
        }

        @Override
        public Optimisation.Result toSolverState(final Optimisation.Result modelState, final ExpressionsBasedModel model) {
            return LinearSolver.INTEGRATION.toSolverState(modelState, model);
        }

        @Override
        public String toString() {
            return "updatable " + myUpdatable + ", reduced gradient " + reducedGradient + ", empty when failed " + emptyWhenFailed;
        }

        @Override
        protected int getIndexInSolver(final ExpressionsBasedModel model, final Variable variable) {
            return ExpressionsBasedModel.Integration.getIndexOfFreeInSolver(model, variable);
        }

    }

    /**
     * Models with a few integer variables, and the optimal value (or that it's infeasible) found by
     * enumerating all combinations of values
     */
    private static void doTest(final ExpressionsBasedModel.Integration<?> subsolver, final String description) {

        for (int seed = 1; seed <= 100; seed++) {

            Random random = new Random(seed);

            int nbVariables = 3 + random.nextInt(3);
            int nbConstraints = 1 + random.nextInt(4);

            int[] lower = new int[nbVariables];
            int[] upper = new int[nbVariables];
            int[] weights = new int[nbVariables];
            for (int j = 0; j < nbVariables; j++) {
                lower[j] = seed % 2 == 0 ? 0 : -random.nextInt(5);
                upper[j] = lower[j] + 1 + random.nextInt(6);
                weights[j] = random.nextInt(21) - 10;
            }
            int[][] factors = new int[nbConstraints][nbVariables];
            int[] limits = new int[nbConstraints];
            boolean[] equality = new boolean[nbConstraints];
            for (int i = 0; i < nbConstraints; i++) {
                equality[i] = random.nextInt(5) == 0;
                for (int j = 0; j < nbVariables; j++) {
                    factors[i][j] = random.nextInt(15) - 7;
                }
                limits[i] = random.nextInt(21) - 10;
            }

            Optimisation.Sense sense = random.nextBoolean() ? Optimisation.Sense.MAX : Optimisation.Sense.MIN;
            int sign = sense == Optimisation.Sense.MAX ? 1 : -1;

            double best = Double.NEGATIVE_INFINITY;
            int[] values = lower.clone();
            int position = 0;
            while (position < nbVariables) {
                boolean feasible = true;
                for (int i = 0; i < nbConstraints; i++) {
                    int body = 0;
                    for (int j = 0; j < nbVariables; j++) {
                        body += factors[i][j] * values[j];
                    }
                    feasible &= equality[i] ? body == limits[i] : body <= limits[i];
                }
                if (feasible) {
                    int value = 0;
                    for (int j = 0; j < nbVariables; j++) {
                        value += sign * weights[j] * values[j];
                    }
                    best = Math.max(best, value);
                }
                position = 0;
                while (position < nbVariables && ++values[position] > upper[position]) {
                    values[position] = lower[position];
                    position++;
                }
            }

            for (boolean cuts : new boolean[] { true, false }) {

                ExpressionsBasedModel model = SubsolverTest.newModel(subsolver);
                for (int j = 0; j < nbVariables; j++) {
                    model.newVariable("x" + j).integer().lower(lower[j]).upper(upper[j]).weight(weights[j]);
                }
                for (int i = 0; i < nbConstraints; i++) {
                    Expression row = model.newExpression("row" + i).upper(limits[i]);
                    if (equality[i]) {
                        row.lower(limits[i]);
                    }
                    for (int j = 0; j < nbVariables; j++) {
                        row.set(j, factors[i][j]);
                    }
                }
                if (!cuts) {
                    model.options.integer(IntegerStrategy.DEFAULT.withCutConfiguration(IntegerStrategy.DEFAULT.getCutConfiguration().withTypes()));
                }

                Optimisation.Result result = sense == Optimisation.Sense.MAX ? model.maximise() : model.minimise();

                String message = description + ", seed " + seed + ", cuts " + cuts + ": " + sign * best + " != " + result;

                if (Double.isFinite(best)) {
                    TestUtils.assertTrue(message, result.getState().isOptimal());
                    TestUtils.assertEquals(message, sign * best, result.getValue(), 1E-6);
                } else {
                    TestUtils.assertEquals(message, Optimisation.State.INFEASIBLE, result.getState());
                }
            }
        }
    }

    /**
     * With a relaxation that is unbounded the model is infeasible, or unbounded
     */
    private static void doTestUnbounded(final ExpressionsBasedModel.Integration<?> subsolver, final String description) {

        for (boolean infeasible : new boolean[] { false, true }) {

            ExpressionsBasedModel model = SubsolverTest.newModel(subsolver);
            Variable x = model.newVariable("x").integer().lower(0).weight(1);
            Variable y = model.newVariable("y").integer().lower(0).weight(1);
            Variable z = model.newVariable("z").integer().lower(0).upper(5).weight(1);
            model.newExpression("c0").set(x, 1).set(y, -1).set(z, 1).upper(2);
            if (infeasible) {
                model.newExpression("c1").set(x, 2).set(y, -2).set(z, 4).level(1);
            }

            Optimisation.Result result = model.maximise();

            TestUtils.assertEquals(description, infeasible ? Optimisation.State.INFEASIBLE : Optimisation.State.UNBOUNDED, result.getState());
        }
    }

    private static List<Behaviour> newBehaviours() {
        List<Behaviour> retVal = new ArrayList<>();
        for (boolean emptyWhenFailed : new boolean[] { false, true }) {
            retVal.add(new Behaviour(false, false, emptyWhenFailed));
            retVal.add(new Behaviour(true, false, emptyWhenFailed));
            retVal.add(new Behaviour(true, true, emptyWhenFailed));
        }
        return retVal;
    }

    private static List<ExpressionsBasedModel.Integration<?>> newConfigurations() {
        List<ExpressionsBasedModel.Integration<?>> retVal = new ArrayList<>();
        for (Boolean sparse : new Boolean[] { Boolean.FALSE, Boolean.TRUE }) {
            retVal.add(LinearSolver.INTEGRATION.withOptionsModifier(options -> {
                options.linear().dual();
                options.sparse = sparse;
            }));
            retVal.add(LinearSolver.INTEGRATION.withOptionsModifier(options -> {
                options.linear().primal();
                options.sparse = sparse;
            }));
        }
        return retVal;
    }

    private static ExpressionsBasedModel newModel(final ExpressionsBasedModel.Integration<?> subsolver) {
        Optimisation.Environment environment = Optimisation.newEnvironment();
        environment.addIntegration(subsolver);
        return environment.newModel();
    }

    /**
     * Solvers that do no more than they have to
     */
    @Test
    public void testBehaviours() {
        for (Behaviour behaviour : SubsolverTest.newBehaviours()) {
            SubsolverTest.doTest(behaviour, behaviour.toString());
        }
    }

    /**
     * Any of ojAlgo's LP solvers: dual or primal, dense or sparse
     */
    @Test
    public void testConfigurations() {
        int i = 0;
        for (ExpressionsBasedModel.Integration<?> configuration : SubsolverTest.newConfigurations()) {
            SubsolverTest.doTest(configuration, "Configuration " + i);
            SubsolverTest.doTestUnbounded(configuration, "Configuration " + i);
            i++;
        }
    }

    @Test
    public void testUnboundedRelaxation() {
        for (Behaviour behaviour : SubsolverTest.newBehaviours()) {
            SubsolverTest.doTestUnbounded(behaviour, behaviour.toString());
        }
    }

}
