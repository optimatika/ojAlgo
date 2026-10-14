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
package org.ojalgo.optimisation;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.Optimisation.ConstraintType;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.keyvalue.EntryPair;
import org.ojalgo.type.keyvalue.EntryPair.KeyedPrimitive;
import org.ojalgo.type.keyvalue.KeyValue;

/**
 * The dual values and the reduced gradient an integration returns are those described at
 * {@link Optimisation.Result#getDualSolution()}: those CPLEX, COPT, HiGHS, MOSEK and Xpress report. The
 * expected values are what those five solvers return, called directly, for the same problems (2026-10-14) –
 * they all agree.
 * <p>
 * The model has a constraint of each kind (upper limit, lower limit, equality and range) and a variable with
 * each kind of bounds (both, upper only, lower only, none and fixed):
 *
 * <pre>{@code
 * x1 in [0, 4], x2 <= 3, x3 >= 1, x4 free, x5 = 2
 * r1: x1 + x2 + 2 x3 <= 10
 * r2: x1 - x2 + x4 >= -2
 * r3: x3 + x4 + x5 = 6
 * r4: -3 <= x2 - x4 <= 1
 * }</pre>
 *
 * Each objective is solved four ways: minimising c, maximising -c (the same optimum, so the dual values and
 * reduced gradient are negated), maximising c and minimising -c. Then there are an infeasible variant, with
 * r2: x1 - x2 + x4 >= 15 (x1 <= 4 and r4's lower limit give at most 7), and an unbounded one, with r1: x1 +
 * x2 + x3 <= 10 (the direction (0, -1, 1, -1, 0) improves c = A when minimising).
 * <p>
 * Subclasses test other integrations by overriding {@link #getLinearIntegrations()} and
 * {@link #getCertifyingIntegrations()}. The models are solved without ojAlgo's presolve, as they were with
 * the native solvers.
 */
public class DualConventionTest extends OptimisationTests {

    /**
     * The extreme rays of the infeasible problem, one for each of its irreducible infeasible subsystems, as
     * dual values (r1, r2, r3, r4) and reduced gradient (x1 ... x5), scaled so that r2's dual value is 1:
     * <ol>
     * <li>r2, r4's lower limit and x1's upper bound – what the native solvers' simplex certificates blame
     * <li>r1, r2, r3, r4's lower limit, x2's upper bound and x5's level
     * <li>r1, r2, r3, r4's lower limit, x3's lower bound and x5's level
     * </ol>
     */
    private static final double[][] EXTREME_DUALS = { { 0, 1, 0, 1 }, { -1, 1, 2, 3 }, { -1, 1, 1, 2 } };
    private static final double[][] EXTREME_GRADIENTS = { { -1, 0, 0, 0, 0 }, { 0, -1, 0, 0, -2 }, { 0, 0, 1, 0, -1 } };
    /**
     * Expected dual values (r1, r2, r3, r4) when maximising c, for each of {@link #OBJECTIVES}. Minimising -c
     * gives the same, negated.
     */
    private static final double[][] MAX_DUALS = { { 0, 0, 1, 0 }, { 0, 0, 0, -1 }, { 0, 0, 0, 1 } };
    /**
     * Expected reduced gradients (x1 ... x5) when maximising c
     */
    private static final double[][] MAX_GRADIENTS = { { 1, 2, -2, 0, -1 }, { 1, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0 } };
    /**
     * Expected dual values when minimising c. Maximising -c gives the same, negated.
     */
    private static final double[][] MIN_DUALS = { { -4, 0, 7, 6 }, { 0, 0, 0, -1 }, { 0, 0, 0, 1 } };
    /**
     * Expected reduced gradients when minimising c
     */
    private static final double[][] MIN_GRADIENTS = { { 5, 0, 0, 0, -7 }, { 1, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0 } };
    private static final String[] OBJECTIVE_NAMES = { "A", "B", "C" };
    /**
     * A exercises several kinds of bounds and constraints, B makes r4's upper limit active when minimising,
     * and C is r4 itself.
     */
    private static final double[][] OBJECTIVES = { { 1, 2, -1, 1, 0 }, { 1, -1, 0, 1, 0 }, { 0, 1, 0, -1, 0 } };
    private static final String[] ROWS = { "r1", "r2", "r3", "r4" };

    /**
     * Asserts that an INFEASIBLE result has an infeasibility certificate, as described at
     * {@link Optimisation.Result#getDualSolution()}: dual values {@code y}, non-negative where a lower limit
     * is used and non-positive where an upper limit is, and the reduced gradient {@code -Σ y ∇g(x)} – such
     * that {@code Σ y g(x)}, which must be at least {@code Σ y} times the limits used, can not be that large
     * within the variable bounds. Only linear constraints.
     *
     * @return The reduced gradient
     */
    public static double[] assertInfeasibilityCertificate(final String id, final ExpressionsBasedModel model, final Optimisation.Result result,
            final NumberContext accuracy) {

        TestUtils.assertEquals(id + " state", State.INFEASIBLE, result.getState());
        TestUtils.assertTrue(id + " reduced gradient present", result.getReducedGradient().isPresent());

        List<KeyedPrimitive<EntryPair<ModelEntity<?>, ConstraintType>>> duals = result.getDualValues();
        TestUtils.assertFalse(id + " dual values present", duals.isEmpty());

        double tolerance = accuracy.epsilon();
        int nbVars = model.countVariables();

        double largest = 0.0;
        for (KeyedPrimitive<EntryPair<ModelEntity<?>, ConstraintType>> dual : duals) {
            largest = Math.max(largest, Math.abs(dual.doubleValue()));
        }
        TestUtils.assertTrue(id + " some dual value non-zero", largest > 0.0);

        double[] combined = new double[nbVars];
        double[] magnitudes = new double[nbVars];
        double[] boundPart = new double[nbVars];
        double limits = 0.0;
        double magnitude = 0.0;

        for (KeyedPrimitive<EntryPair<ModelEntity<?>, ConstraintType>> dual : duals) {

            ModelEntity<?> entity = dual.getKey().left();
            double value = dual.doubleValue();

            if (entity instanceof Expression) {

                Expression expression = (Expression) entity;
                TestUtils.assertFalse(id + " linear constraint " + entity.getName(), expression.isAnyQuadraticFactorNonZero());

                for (IntIndex key : expression.getLinearKeySet()) {
                    double term = expression.doubleValue(key, false) * value;
                    combined[key.index] += term;
                    magnitudes[key.index] += Math.abs(term);
                }

                if (value > tolerance * largest) {
                    TestUtils.assertTrue(id + " lower limit of " + entity.getName(), expression.isLowerLimitSet());
                    limits += value * expression.getLowerLimit().doubleValue();
                    magnitude += Math.abs(value * expression.getLowerLimit().doubleValue());
                } else if (value < -tolerance * largest) {
                    TestUtils.assertTrue(id + " upper limit of " + entity.getName(), expression.isUpperLimitSet());
                    limits += value * expression.getUpperLimit().doubleValue();
                    magnitude += Math.abs(value * expression.getUpperLimit().doubleValue());
                }

            } else if (entity instanceof Variable) {

                boundPart[((Variable) entity).getIndex().index] += value;
            }
        }

        double[] reducedGradient = new double[nbVars];
        Access1D<?> gradient = result.getReducedGradient().map(Supplier::get).get();

        double largestCombination = 0.0;

        for (int j = 0; j < nbVars; j++) {

            Variable variable = model.getVariable(j);
            reducedGradient[j] = gradient.doubleValue(j);

            double noise = tolerance * Math.max(magnitudes[j], largest);

            TestUtils.assertEquals(id + " reduced gradient of " + variable.getName(), -combined[j], reducedGradient[j], noise);

            if (!accuracy.isZero(boundPart[j])) {
                TestUtils.assertEquals(id + " bound multiplier of " + variable.getName(), boundPart[j], reducedGradient[j], noise);
            }

            if (combined[j] > noise) {
                TestUtils.assertTrue(id + " upper bound of " + variable.getName(), variable.isUpperLimitSet());
                largestCombination += combined[j] * variable.getUpperLimit().doubleValue();
                magnitude += Math.abs(combined[j] * variable.getUpperLimit().doubleValue());
            } else if (combined[j] < -noise) {
                TestUtils.assertTrue(id + " lower bound of " + variable.getName(), variable.isLowerLimitSet());
                largestCombination += combined[j] * variable.getLowerLimit().doubleValue();
                magnitude += Math.abs(combined[j] * variable.getLowerLimit().doubleValue());
            }
        }

        TestUtils.assertTrue(id + ": " + limits + " is not greater than " + largestCombination,
                limits - largestCombination > tolerance * Math.max(magnitude, largest));

        return reducedGradient;
    }

    /**
     * The sum of the dual values reported with the constraint (a range constraint may be reported as two
     * rows, its lower and its upper limit).
     */
    private static double getDualValue(final Optimisation.Result result, final ModelEntity<?> constraint) {
        double retVal = 0.0;
        for (KeyedPrimitive<EntryPair<ModelEntity<?>, ConstraintType>> dual : result.getDualValues()) {
            if (dual.getKey().left() == constraint) {
                retVal += dual.doubleValue();
            }
        }
        return retVal;
    }

    /**
     * Whether the dual values and the reduced gradient, scaled so that r2's dual value is 1, are those of one
     * of the {@link #EXTREME_DUALS extreme rays}.
     */
    private static boolean isExtremeRay(final double[] duals, final double[] gradient, final NumberContext accuracy) {
        for (int k = 0; k < EXTREME_DUALS.length; k++) {
            boolean same = true;
            for (int i = 0; i < duals.length; i++) {
                same &= !accuracy.isDifferent(EXTREME_DUALS[k][i], duals[i]);
            }
            for (int j = 0; j < gradient.length; j++) {
                same &= !accuracy.isDifferent(EXTREME_GRADIENTS[k][j], gradient[j]);
            }
            if (same) {
                return true;
            }
        }
        return false;
    }

    private static double[] scale(final double factor, final double[] values) {
        double[] retVal = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            retVal[i] = factor * values[i];
        }
        return retVal;
    }

    /**
     * Solves the way {@link ExpressionsBasedModel#minimise()} and {@link ExpressionsBasedModel#maximise()}
     * do, including presolve, but with the given integration.
     */
    private static Optimisation.Result solve(final ExpressionsBasedModel model, final Optimisation.Sense sense,
            final ExpressionsBasedModel.Integration<?> integration) {

        IntermediateSolver solver = model.prepare(sense, prepared -> new IntermediateSolver(prepared) {

            @Override
            protected ExpressionsBasedModel.Integration<?> getIntegration() {
                return integration;
            }

        });

        return solver.solve();
    }

    /**
     * With {@link #isCertificateMinimal()} the certificate must also be one of the extreme rays, compared
     * scaled so that r2's dual value is 1 – r2 is part of every irreducible infeasible subsystem.
     */
    @Test
    public void testInfeasible() {

        double[] zero = new double[5];

        for (KeyValue<String, ExpressionsBasedModel.Integration<?>> integration : this.getCertifyingIntegrations()) {
            for (double[] objective : new double[][] { OBJECTIVES[0], zero }) {
                for (Optimisation.Sense sense : Optimisation.Sense.values()) {

                    String id = integration.getKey() + " " + sense + (objective == zero ? " 0" : " A");

                    ExpressionsBasedModel model = this.buildModel(objective, 2, 15);

                    Optimisation.Result result = DualConventionTest.solve(model, sense, integration.getValue());

                    double[] reducedGradient = DualConventionTest.assertInfeasibilityCertificate(id, model, result, this.getAccuracy());

                    if (this.isCertificateMinimal()) {
                        double scale = DualConventionTest.getDualValue(result, model.getExpression("r2"));
                        TestUtils.assertTrue(id + " r2", scale > 0.0);
                        double[] duals = new double[ROWS.length];
                        for (int i = 0; i < ROWS.length; i++) {
                            duals[i] = DualConventionTest.getDualValue(result, model.getExpression(ROWS[i])) / scale;
                        }
                        double[] gradient = DualConventionTest.scale(1.0 / scale, reducedGradient);
                        TestUtils.assertTrue(id + " extreme ray " + Arrays.toString(duals) + " " + Arrays.toString(gradient),
                                DualConventionTest.isExtremeRay(duals, gradient, this.getAccuracy()));
                    }
                }
            }
        }
    }

    @Test
    public void testOptimal() {

        for (KeyValue<String, ExpressionsBasedModel.Integration<?>> integration : this.getLinearIntegrations()) {
            for (int k = 0; k < OBJECTIVES.length; k++) {

                double[] objective = OBJECTIVES[k];
                double[] negated = DualConventionTest.scale(-1, objective);

                this.doTestOptimal(integration, OBJECTIVE_NAMES[k] + " MIN  c", objective, Optimisation.Sense.MIN, MIN_DUALS[k], MIN_GRADIENTS[k]);
                this.doTestOptimal(integration, OBJECTIVE_NAMES[k] + " MAX -c", negated, Optimisation.Sense.MAX, DualConventionTest.scale(-1, MIN_DUALS[k]),
                        DualConventionTest.scale(-1, MIN_GRADIENTS[k]));
                this.doTestOptimal(integration, OBJECTIVE_NAMES[k] + " MAX  c", objective, Optimisation.Sense.MAX, MAX_DUALS[k], MAX_GRADIENTS[k]);
                this.doTestOptimal(integration, OBJECTIVE_NAMES[k] + " MIN -c", negated, Optimisation.Sense.MIN, DualConventionTest.scale(-1, MAX_DUALS[k]),
                        DualConventionTest.scale(-1, MAX_GRADIENTS[k]));
            }
        }
    }

    /**
     * UNBOUNDED, or INVALID from a solver that can only tell that it is unbounded or infeasible. Maximising c
     * is not unbounded.
     */
    @Test
    public void testUnbounded() {

        double[] objective = OBJECTIVES[0];

        for (KeyValue<String, ExpressionsBasedModel.Integration<?>> integration : this.getLinearIntegrations()) {

            String id = integration.getKey() + " unbounded";

            Optimisation.Result minimised = DualConventionTest.solve(this.buildModel(objective, 1, -2), Optimisation.Sense.MIN, integration.getValue());
            TestUtils.assertTrue(id + " MIN c " + minimised.getState(), minimised.getState() == State.UNBOUNDED || minimised.getState() == State.INVALID);

            Optimisation.Result maximised = DualConventionTest.solve(this.buildModel(DualConventionTest.scale(-1, objective), 1, -2), Optimisation.Sense.MAX,
                    integration.getValue());
            TestUtils.assertTrue(id + " MAX -c " + maximised.getState(), maximised.getState() == State.UNBOUNDED || maximised.getState() == State.INVALID);

            Optimisation.Result bounded = DualConventionTest.solve(this.buildModel(objective, 1, -2), Optimisation.Sense.MAX, integration.getValue());
            TestUtils.assertStateNotLessThanOptimal(bounded);
            TestUtils.assertEquals(id + " MAX c", 12.0, bounded.getValue(), this.getAccuracy());
        }
    }

    /**
     * Without ojAlgo's presolve. It could tighten bounds in place (x4 <= 3, from r3 and x3 >= 1), and then
     * the dual values would no longer be unique, nor would the problem be the one the native solvers solved.
     */
    private ExpressionsBasedModel buildModel(final double[] objective, final double r1x3, final double r2lower) {

        Optimisation.Environment environment = Optimisation.newEnvironment();
        environment.clearPresolvers();
        this.configure(environment);

        ExpressionsBasedModel model = environment.newModel();

        Variable x1 = model.newVariable("x1").lower(0).upper(4).weight(objective[0]);
        Variable x2 = model.newVariable("x2").upper(3).weight(objective[1]);
        Variable x3 = model.newVariable("x3").lower(1).weight(objective[2]);
        Variable x4 = model.newVariable("x4").weight(objective[3]);
        Variable x5 = model.newVariable("x5").level(2).weight(objective[4]);

        model.newExpression("r1").set(x1, 1).set(x2, 1).set(x3, r1x3).upper(10);
        model.newExpression("r2").set(x1, 1).set(x2, -1).set(x4, 1).lower(r2lower);
        model.newExpression("r3").set(x3, 1).set(x4, 1).set(x5, 1).level(6);
        model.newExpression("r4").set(x2, 1).set(x4, -1).lower(-3).upper(1);

        return model;
    }

    private void doTestOptimal(final KeyValue<String, ExpressionsBasedModel.Integration<?>> integration, final String name, final double[] objective,
            final Optimisation.Sense sense, final double[] expectedDuals, final double[] expectedGradient) {

        String id = integration.getKey() + " " + name;

        ExpressionsBasedModel model = this.buildModel(objective, 2, -2);

        Optimisation.Result result = DualConventionTest.solve(model, sense, integration.getValue());

        TestUtils.assertStateNotLessThanOptimal(result);

        for (int i = 0; i < ROWS.length; i++) {
            double actual = DualConventionTest.getDualValue(result, model.getExpression(ROWS[i]));
            TestUtils.assertEquals(id + " dual value of " + ROWS[i], expectedDuals[i], actual, this.getAccuracy());
        }

        TestUtils.assertTrue(id + " reduced gradient present", result.getReducedGradient().isPresent());
        Access1D<?> gradient = result.getReducedGradient().map(Supplier::get).get();
        for (int j = 0; j < expectedGradient.length; j++) {
            TestUtils.assertEquals(id + " reduced gradient of x" + (j + 1), expectedGradient[j], gradient.doubleValue(j), this.getAccuracy());
        }
    }

    /**
     * Override to register a solver specific configurator with the models' environment, for instance one that
     * turns off a presolve that hides the certificates. Does nothing by default.
     */
    protected void configure(final Optimisation.Environment environment) {}

    protected NumberContext getAccuracy() {
        return NumberContext.of(8);
    }

    /**
     * The integrations that return an infeasibility certificate for the infeasible problem.
     */
    protected List<KeyValue<String, ExpressionsBasedModel.Integration<?>>> getCertifyingIntegrations() {
        return List.of(KeyValue.of("dual", LinearSolver.INTEGRATION.withOptionsModifier(options -> options.linear().dual())));
    }

    protected List<KeyValue<String, ExpressionsBasedModel.Integration<?>>> getLinearIntegrations() {
        return List.of(KeyValue.of("dual", LinearSolver.INTEGRATION.withOptionsModifier(options -> options.linear().dual())),
                KeyValue.of("primal", LinearSolver.INTEGRATION.withOptionsModifier(options -> options.linear().primal())));
    }

    /**
     * Whether the certificates are extreme rays, each blaming one of the infeasible problem's three
     * irreducible infeasible subsystems. A simplex certificate is, one from an interior point method need not
     * be.
     */
    protected boolean isCertificateMinimal() {
        return true;
    }

}
