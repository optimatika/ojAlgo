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

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.Optimisation.ConstraintType;
import org.ojalgo.optimisation.convex.ConvexSolver;
import org.ojalgo.optimisation.integer.IntegerSolver;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.structure.Structure2D.IntRowColumn;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.keyvalue.EntryPair;
import org.ojalgo.type.keyvalue.EntryPair.KeyedPrimitive;
import org.ojalgo.type.keyvalue.KeyValue;

/**
 * The result an integration returns must be in model terms (see
 * {@link Optimisation.Integration#toModelState(Optimisation.Result, Optimisation.Model)}): a feasible
 * solution, the objective function value in model units, including the objective constant, and the dual
 * values and reduced gradient forming an optimality (KKT) certificate for the model as it is after the solve
 * – presolve may have tightened variable bounds, in place, and the certificate is relative to those derived
 * bounds.
 * <p>
 * In the minimisation form (see {@link Optimisation.Result#getDualValues()} for the sign convention):
 * <ul>
 * <li>The reduced gradient is the objective's gradient plus the multiplier terms of the constraint
 * expressions, so it is non-zero only for variables at an active bound: positive at a lower bound, negative
 * at an upper bound.
 * <li>Inequality multipliers are non-negative, and non-zero only for active constraints.
 * <li>Solvers that model variable bounds as constraint rows also report their multipliers, with the
 * variables. Those must equal the bound part of the reduced gradient.
 * </ul>
 * The results are obtained with {@link ExpressionsBasedModel#prepare(Optimisation.Sense, Function)}, not
 * {@link ExpressionsBasedModel#minimise()} or {@link ExpressionsBasedModel#maximise()} (those recompute the
 * objective function value from the solution). Subclasses test other integrations by overriding
 * {@link #getLinearIntegrations()}, {@link #getQuadraticIntegrations()} and
 * {@link #getQuadraticConstraintIntegrations()}. With quadratic constraints the gradients of those
 * constraints depend on the solution, and are evaluated there.
 */
public class OptimalityCertificateTest extends OptimisationTests {

    private static final BigDecimal CONSTANT = BigDecimal.valueOf(1.5);

    private static void assertCertificate(final String id, final ExpressionsBasedModel model, final Optimisation.Result result, final NumberContext accuracy,
            final NumberContext complementarity, final boolean duals) {

        TestUtils.assertStateNotLessThanOptimal(result);

        TestUtils.assertTrue(id + " feasible", model.validate(result, accuracy));

        TestUtils.assertEquals(id + " objective function value", model.objective().evaluate(result).doubleValue(), result.getValue(), accuracy);

        if (!duals) {
            return;
        }

        TestUtils.assertTrue(id + " reduced gradient present", result.getReducedGradient().isPresent());

        int nbVars = model.countVariables();
        double sign = model.getOptimisationSense() == Optimisation.Sense.MAX ? -1.0 : 1.0;
        Access1D<?> reducedGradient = result.getReducedGradient().map(Supplier::get).get();

        double[] x = new double[nbVars];
        for (int j = 0; j < nbVars; j++) {
            x[j] = result.doubleValue(j);
        }

        Expression objective = model.objective();
        double[] residual = new double[nbVars];
        for (int j = 0; j < nbVars; j++) {
            double gradient = objective.doubleValue(new IntIndex(j), false);
            for (IntRowColumn key : objective.getQuadraticKeySet()) {
                double factor = objective.doubleValue(key, false);
                if (key.row == j) {
                    gradient += factor * x[key.column];
                }
                if (key.column == j) {
                    gradient += factor * x[key.row];
                }
            }
            residual[j] = sign * (reducedGradient.doubleValue(j) - gradient);
        }

        double[] boundPart = new double[nbVars];

        for (KeyedPrimitive<EntryPair<ModelEntity<?>, ConstraintType>> dual : result.getDualValues()) {

            ModelEntity<?> entity = dual.getKey().left();
            ConstraintType type = dual.getKey().right();
            double multiplier = dual.doubleValue();
            double direction = type == ConstraintType.LOWER ? -1.0 : 1.0;

            if (type != ConstraintType.EQUALITY) {
                TestUtils.assertTrue(id + " non-negative multiplier for " + entity.getName(), multiplier > -accuracy.epsilon());
            }

            if (entity instanceof Expression) {

                Expression expression = (Expression) entity;
                double activity = 0.0;
                for (IntIndex key : expression.getLinearKeySet()) {
                    double factor = expression.doubleValue(key, false);
                    activity += factor * x[key.index];
                    residual[key.index] -= direction * factor * multiplier;
                }
                for (IntRowColumn key : expression.getQuadraticKeySet()) {
                    double factor = expression.doubleValue(key, false);
                    activity += factor * x[key.row] * x[key.column];
                    residual[key.row] -= direction * factor * x[key.column] * multiplier;
                    residual[key.column] -= direction * factor * x[key.row] * multiplier;
                }

                if (type == ConstraintType.LOWER) {
                    TestUtils.assertTrue(id + " lower limit of " + entity.getName(), expression.isLowerLimitSet());
                    double slack = activity - expression.getLowerLimit().doubleValue();
                    TestUtils.assertEquals(id + " complementary slackness " + entity.getName(), 0.0, multiplier * slack, complementarity);
                } else if (type == ConstraintType.UPPER) {
                    TestUtils.assertTrue(id + " upper limit of " + entity.getName(), expression.isUpperLimitSet());
                    double slack = expression.getUpperLimit().doubleValue() - activity;
                    TestUtils.assertEquals(id + " complementary slackness " + entity.getName(), 0.0, multiplier * slack, complementarity);
                }

            } else if (entity instanceof Variable) {

                boundPart[((Variable) entity).getIndex().index] -= direction * multiplier;
            }
        }

        for (int j = 0; j < nbVars; j++) {

            Variable variable = model.getVariable(j);
            if (variable.isEqualityConstraint()) {
                continue;
            }

            double rc = sign * reducedGradient.doubleValue(j);

            TestUtils.assertEquals(id + " stationarity " + variable.getName(), 0.0, residual[j], accuracy);

            if (!accuracy.isZero(boundPart[j])) {
                TestUtils.assertEquals(id + " bound multiplier " + variable.getName(), boundPart[j], rc, accuracy);
            }

            if (rc > accuracy.epsilon()) {
                TestUtils.assertTrue(id + " positive rc with a lower bound " + variable.getName(), variable.isLowerLimitSet());
                double slack = x[j] - variable.getLowerLimit().doubleValue();
                TestUtils.assertEquals(id + " complementary slackness " + variable.getName(), 0.0, rc * slack, complementarity);
            } else if (rc < -accuracy.epsilon()) {
                TestUtils.assertTrue(id + " negative rc with an upper bound " + variable.getName(), variable.isUpperLimitSet());
                double slack = variable.getUpperLimit().doubleValue() - x[j];
                TestUtils.assertEquals(id + " complementary slackness " + variable.getName(), 0.0, rc * slack, complementarity);
            }
        }
    }

    /**
     * Presolve fixes y at 5 (the bound y <= 5 and the constraint y >= 5).
     */
    /**
     * Minimise x subject to x² + y² <= 1. The solution is (-1, 0), and the multiplier 0.5.
     */
    private static ExpressionsBasedModel makeBall(final double sign) {
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.newVariable("x").weight(sign);
        Variable y = model.newVariable("y");
        model.newExpression("ball").set(x, x, 1).set(y, y, 1).upper(1);
        return model;
    }

    /**
     * The same ball, but as a lower limit on a concave expression: -x² - y² >= -1.
     */
    private static ExpressionsBasedModel makeConcaveLowerLimit(final double sign) {
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.newVariable("x").weight(sign);
        Variable y = model.newVariable("y");
        model.newExpression("ball").set(x, x, -1).set(y, y, -1).lower(-1);
        return model;
    }

    private static ExpressionsBasedModel makeFixedByPresolve(final double sign) {
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.newVariable("x").lower(0).upper(10).weight(sign);
        Variable y = model.newVariable("y").lower(0).upper(5).weight(3 * sign);
        model.newExpression("c1").set(x, 1).set(y, 1).upper(10);
        model.newExpression("c2").set(y, 1).lower(5);
        return model;
    }

    /**
     * The fixed z is removed from the constraint before it reaches the solver.
     */
    private static ExpressionsBasedModel makeFixedInConstraint(final double sign) {
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.newVariable("x").lower(0).upper(10).weight(sign);
        Variable y = model.newVariable("y").lower(0).upper(10).weight(2 * sign);
        Variable z = model.newVariable("z").level(0.5).weight(3 * sign);
        model.newExpression("c").set(x, 1).set(y, 1).set(z, 1).lower(2.5);
        return model;
    }

    /**
     * Presolve tightens both variables' lower bounds to -9, and the optimum is at one of them.
     */
    private static ExpressionsBasedModel makeImpliedBound(final double sign) {
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.newVariable("x").lower(-10).upper(10).weight(3 * sign);
        Variable y = model.newVariable("y").lower(-10).upper(10).weight(6 * sign);
        model.newExpression("c").set(x, 1).set(y, 1).lower(1);
        return model;
    }

    /**
     * An integer and a continuous variable, and coefficients far from 1 (so that the adjustment factors are
     * not – constraints with only integer variables and coefficients are never adjusted). The optimum is x =
     * 4 and y = 0, with c2 active.
     */
    /**
     * The quadratic constraint is not active at the unconstrained optimum (0.5, 0) of x² + y² - x, so its
     * multiplier is 0.
     */
    private static ExpressionsBasedModel makeInactiveQuadraticConstraint(final double sign) {
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.newVariable("x");
        Variable y = model.newVariable("y");
        model.newExpression("objective").set(x, x, 1).set(y, y, 1).set(x, -1).weight(sign);
        model.newExpression("ball").set(x, x, 1).set(y, y, 1).upper(4);
        return model;
    }

    private static ExpressionsBasedModel makeInteger(final double sign) {
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.newVariable("x").integer().lower(0).upper(10).weight(0.003 * sign);
        Variable y = model.newVariable("y").lower(0).upper(10).weight(0.007 * sign);
        model.newExpression("c1").set(x, 2500).set(y, 1000).lower(9500);
        model.newExpression("c2").set(x, 1000).set(y, -2000).upper(4000);
        return model;
    }

    /**
     * Range and equality constraints, and presolve-tightened bounds (y in [1, 2]). The upper limit of r1 is
     * active when minimising, and the lower limit of r2 when maximising.
     */
    private static ExpressionsBasedModel makeMixed(final double sign) {
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.newVariable("x").lower(0).upper(4).weight(-sign);
        Variable y = model.newVariable("y").lower(0).upper(4).weight(-2 * sign);
        Variable w = model.newVariable("w").lower(1).upper(3).weight(sign);
        model.newExpression("r1").set(x, 1).set(y, 1).set(w, 1).lower(-10).upper(6);
        model.newExpression("r2").set(x, 1).set(w, -1).lower(-1).upper(10);
        model.newExpression("r3").set(y, 2).set(w, 1).level(5);
        return model;
    }

    /**
     * Each model has an objective constant, and its objective is negated when maximising.
     */
    private static Function<Double, ExpressionsBasedModel>[] makers() {
        return new Function[] { (Function<Double, ExpressionsBasedModel>) OptimalityCertificateTest::makeFixedByPresolve,
                (Function<Double, ExpressionsBasedModel>) OptimalityCertificateTest::makeFixedInConstraint,
                (Function<Double, ExpressionsBasedModel>) OptimalityCertificateTest::makeImpliedBound,
                (Function<Double, ExpressionsBasedModel>) OptimalityCertificateTest::makeMixed,
                (Function<Double, ExpressionsBasedModel>) OptimalityCertificateTest::makeSingletonRow };
    }

    /**
     * Coefficients and limits far from 1, so that the adjustment factors are not: 1000 for x and z, 10 for y,
     * 0.001 for the constraints and 10 for the objective. In the linear case the optimum is at a vertex with
     * both constraints and the upper bound of z active. (Tested separately from the other models, so that it
     * can be tagged for integrations that fail it for reasons of their own.)
     */
    private static ExpressionsBasedModel makeScaled(final double sign) {
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.newVariable("x").lower(0.001).upper(0.004).weight(0.3 * sign);
        Variable y = model.newVariable("y").lower(0).upper(0.003).weight(0.15 * sign);
        Variable z = model.newVariable("z").lower(-0.002).upper(0.002).weight(-0.0502 * sign);
        model.newExpression("c1").set(x, 3000).set(y, 1000).lower(9);
        model.newExpression("c2").set(y, -1000).set(z, 1000).upper(1.5);
        return model;
    }

    /**
     * The singleton constraint on y becomes a bound, and x is fixed by its equality constraint.
     */
    private static ExpressionsBasedModel makeSingletonRow(final double sign) {
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.newVariable("x").weight(sign);
        Variable y = model.newVariable("y").upper(5).weight(-sign);
        model.newExpression("c1").set(y, 1).upper(3);
        model.newExpression("eq").set(x, 1).level(0);
        return model;
    }

    /**
     * Solves the way {@link ExpressionsBasedModel#minimise()} and {@link ExpressionsBasedModel#maximise()}
     * do, including presolve, but with the given integration and without recomputing the objective function
     * value from the solution.
     */
    /**
     * The expected return of a long-only portfolio, with a cap on its variance: linear and quadratic
     * constraints as well as variable bounds.
     */
    private static ExpressionsBasedModel makeVarianceCap(final double sign) {

        double[] returns = { 0.10, 0.07, 0.03 };
        double[][] covariances = { { 0.040, 0.006, 0.002 }, { 0.006, 0.020, 0.001 }, { 0.002, 0.001, 0.005 } };

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable[] weights = new Variable[returns.length];
        for (int i = 0; i < weights.length; i++) {
            weights[i] = model.newVariable("w" + i).lower(0).weight(-sign * returns[i]);
        }

        Expression budget = model.newExpression("budget").level(1);
        Expression variance = model.newExpression("variance").upper(0.01);
        for (int i = 0; i < weights.length; i++) {
            budget.set(weights[i], 1);
            for (int j = 0; j < weights.length; j++) {
                variance.set(weights[i], weights[j], covariances[i][j]);
            }
        }

        return model;
    }

    /**
     * Models with quadratic constraints, convex ones. Each model has an objective constant, and its objective
     * is negated when maximising.
     */
    private static Function<Double, ExpressionsBasedModel>[] quadraticConstraintMakers() {
        return new Function[] { (Function<Double, ExpressionsBasedModel>) OptimalityCertificateTest::makeBall,
                (Function<Double, ExpressionsBasedModel>) OptimalityCertificateTest::makeConcaveLowerLimit,
                (Function<Double, ExpressionsBasedModel>) OptimalityCertificateTest::makeInactiveQuadraticConstraint,
                (Function<Double, ExpressionsBasedModel>) OptimalityCertificateTest::makeVarianceCap };
    }

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

    @Test
    public void testInteger() {

        for (KeyValue<String, ExpressionsBasedModel.Integration<?>> integration : this.getIntegerIntegrations()) {
            for (Optimisation.Sense sense : Optimisation.Sense.values()) {
                double sign = sense == Optimisation.Sense.MAX ? -1.0 : 1.0;

                ExpressionsBasedModel model = OptimalityCertificateTest.makeInteger(sign);
                model.addObjectiveConstant(CONSTANT.multiply(BigDecimal.valueOf(sign)));

                Optimisation.Result result = OptimalityCertificateTest.solve(model, sense, integration.getValue());

                OptimalityCertificateTest.assertCertificate("MIP " + integration.getKey() + " " + sense, model, result, this.getAccuracy(),
                        this.getComplementarityAccuracy(), false);
                TestUtils.assertEquals("MIP " + integration.getKey() + " " + sense + " x", 4.0, result.doubleValue(0), this.getAccuracy());
                TestUtils.assertEquals("MIP " + integration.getKey() + " " + sense + " y", 0.0, result.doubleValue(1), this.getAccuracy());
            }
        }
    }

    @Test
    public void testLinear() {
        Function<Double, ExpressionsBasedModel>[] makers = OptimalityCertificateTest.makers();
        for (int c = 0; c < makers.length; c++) {
            this.doTest("LP", this.getLinearIntegrations(), false, "case " + c, makers[c], this.getAccuracy(), this.getComplementarityAccuracy());
        }
    }

    @Test
    public void testLinearScaled() {
        this.doTest("LP", this.getLinearIntegrations(), false, "scaled", OptimalityCertificateTest::makeScaled, this.getAccuracy(),
                this.getComplementarityAccuracy());
    }

    @Test
    public void testQuadratic() {
        Function<Double, ExpressionsBasedModel>[] makers = OptimalityCertificateTest.makers();
        for (int c = 0; c < makers.length; c++) {
            this.doTest("QP", this.getQuadraticIntegrations(), true, "case " + c, makers[c], this.getAccuracy(), this.getComplementarityAccuracy());
        }
    }

    @Test
    public void testQuadraticConstraints() {
        Function<Double, ExpressionsBasedModel>[] makers = OptimalityCertificateTest.quadraticConstraintMakers();
        for (int c = 0; c < makers.length; c++) {
            this.doTest("QCQP", this.getQuadraticConstraintIntegrations(), false, "case " + c, makers[c], this.getQuadraticConstraintAccuracy(),
                    this.getQuadraticConstraintAccuracy());
        }
    }

    @Test
    public void testQuadraticScaled() {
        this.doTest("QP", this.getQuadraticIntegrations(), true, "scaled", OptimalityCertificateTest::makeScaled, this.getAccuracy(),
                this.getComplementarityAccuracy());
    }

    /**
     * Solves the model, in both senses, with each of the integrations. A quadratic objective is made by
     * adding (x² + y² + ...) / 4.
     */
    private void doTest(final String type, final List<KeyValue<String, ExpressionsBasedModel.Integration<?>>> integrations, final boolean quadratic,
            final String name, final Function<Double, ExpressionsBasedModel> maker, final NumberContext accuracy, final NumberContext complementarity) {

        for (KeyValue<String, ExpressionsBasedModel.Integration<?>> integration : integrations) {
            for (Optimisation.Sense sense : Optimisation.Sense.values()) {
                double sign = sense == Optimisation.Sense.MAX ? -1.0 : 1.0;

                ExpressionsBasedModel model = maker.apply(sign);
                model.addObjectiveConstant(CONSTANT.multiply(BigDecimal.valueOf(sign)));
                if (quadratic) {
                    Expression expression = model.addExpression("quadratic").weight(sign);
                    for (Variable variable : model.getVariables()) {
                        expression.set(variable, variable, 0.25);
                    }
                }

                Optimisation.Result result = OptimalityCertificateTest.solve(model, sense, integration.getValue());

                OptimalityCertificateTest.assertCertificate(type + " " + integration.getKey() + " " + sense + " " + name, model, result, accuracy,
                        complementarity, this.isDualsProvided());
            }
        }
    }

    /**
     * The accuracy of the checks. Override for solvers that solve to a lower accuracy.
     */
    protected NumberContext getAccuracy() {
        return NumberContext.of(8);
    }

    /**
     * The accuracy of the complementary slackness checks: that the products of the multipliers (and reduced
     * gradients) and the corresponding slacks are zero. Override for interior point solvers, which at a
     * degenerate optimum (a zero multiplier on an active constraint) get close to the solution with both the
     * multiplier and the slack small, but non-zero – their product about the duality gap.
     */
    protected NumberContext getComplementarityAccuracy() {
        return this.getAccuracy();
    }

    /**
     * Only the solution's feasibility and the objective function value are checked with these.
     */
    protected List<KeyValue<String, ExpressionsBasedModel.Integration<?>>> getIntegerIntegrations() {
        return List.of(KeyValue.of("B&B", IntegerSolver.INTEGRATION));
    }

    protected List<KeyValue<String, ExpressionsBasedModel.Integration<?>>> getLinearIntegrations() {
        return List.of(KeyValue.of("dual", LinearSolver.INTEGRATION.withOptionsModifier(options -> options.linear().dual())),
                KeyValue.of("primal", LinearSolver.INTEGRATION.withOptionsModifier(options -> options.linear().primal())));
    }

    /**
     * The accuracy of the checks with quadratic constraints. Override for (interior point) solvers that solve
     * such models to a lower accuracy - the gradients of the constraints are evaluated at the solution, and
     * are no more accurate than it.
     */
    protected NumberContext getQuadraticConstraintAccuracy() {
        return this.getAccuracy();
    }

    /**
     * Integrations that handle quadratic constraints. None of ojAlgo's own solvers do.
     */
    protected List<KeyValue<String, ExpressionsBasedModel.Integration<?>>> getQuadraticConstraintIntegrations() {
        return List.of();
    }

    protected List<KeyValue<String, ExpressionsBasedModel.Integration<?>>> getQuadraticIntegrations() {
        return List.of(
                KeyValue.of("active-set",
                        ConvexSolver.INTEGRATION
                                .withOptionsModifier(options -> options.convex().projection(Boolean.FALSE).algorithm(ConvexSolver.Algorithm.ACTIVE_SET))),
                KeyValue.of("null-space",
                        ConvexSolver.INTEGRATION
                                .withOptionsModifier(options -> options.convex().projection(Boolean.TRUE).algorithm(ConvexSolver.Algorithm.ACTIVE_SET))),
                KeyValue.of("ADMM", ConvexSolver.INTEGRATION.withOptionsModifier(options -> options.convex().algorithm(ConvexSolver.Algorithm.ADMM))),
                KeyValue.of("extended",
                        ConvexSolver.INTEGRATION.withOptionsModifier(
                                options -> options.convex().projection(Boolean.FALSE).algorithm(ConvexSolver.Algorithm.ACTIVE_SET).extendedPrecision(true))),
                KeyValue.of("ADMM-extended", ConvexSolver.INTEGRATION
                        .withOptionsModifier(options -> options.convex().algorithm(ConvexSolver.Algorithm.ADMM).extendedPrecision(true))));
    }

    /**
     * Whether the integrations provide dual values and a reduced gradient. If not, only the objective
     * function value is checked.
     */
    protected boolean isDualsProvided() {
        return true;
    }

}
