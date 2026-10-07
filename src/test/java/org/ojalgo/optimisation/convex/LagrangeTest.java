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
package org.ojalgo.optimisation.convex;

import static org.ojalgo.matrix.store.R064Store.FACTORY;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.convex.ConvexSolver.Builder;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.context.NumberContext;

public class LagrangeTest extends OptimisationConvexTests {

    /**
     * The fixed z is the third variable.
     */
    private static void assertFixedInConstraint(final Result result, final Expression constraint, final double x, final double y, final double dual,
            final double fixedRc) {

        NumberContext accuracy = NumberContext.of(8);

        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(x, result.doubleValue(0), accuracy);
        TestUtils.assertEquals(y, result.doubleValue(1), accuracy);

        double actualDual = result.getDualValues().stream().filter(kp -> kp.getKey().left() == constraint).mapToDouble(kp -> kp.doubleValue()).findFirst()
                .orElseThrow();
        TestUtils.assertEquals(dual, actualDual, accuracy);

        TestUtils.assertEquals(fixedRc, result.getReducedGradient().get().get().doubleValue(2), accuracy);
    }

    private static double getDualValue(final Result result, final String name, final Optimisation.ConstraintType type) {
        return result.getDualValues().stream().filter(kp -> name.equals(kp.getKey().left().getName()) && kp.getKey().right() == type)
                .mapToDouble(kp -> kp.doubleValue()).findFirst().orElseThrow();
    }

    /**
     * weight * (x^2 + y^2) / 2 subject to x + y >= 2 and x - y = 0, x and y in [-10, 10]
     */
    private static ExpressionsBasedModel newScaledObjectiveModel(final double weight) {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("x").lower(-10).upper(10);
        Variable y = model.addVariable("y").lower(-10).upper(10);

        model.addExpression("objective").weight(weight).set(x, x, 0.5).set(y, y, 0.5);
        model.addExpression("sum").set(x, 1).set(y, 1).lower(2);
        model.addExpression("difference").set(x, 1).set(y, -1).level(0);

        return model;
    }

    /**
     * Optimise (x^2 + z^2) / 2 - 2 * bound * x (negated when maximising) subject to x <= bound and z >=
     * bound. The solution is x = z = bound, and the multipliers of both bounds are bound. The variables v and
     * w, and the equality constraint between them, don't change that but make it possible to use the
     * null-space projection solver.
     */
    private static Result solveScaledBounds(final double bound, final Boolean projection, final Optimisation.Sense sense) {

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        model.options.convex().projection(projection);

        Variable x = model.addVariable("x").upper(bound);
        Variable z = model.addVariable("z").lower(bound);
        Variable v = model.addVariable("v");
        Variable w = model.addVariable("w");

        double weight = sense == Optimisation.Sense.MAX ? -1.0 : 1.0;
        model.addExpression("objective").weight(weight).set(x, x, 0.5).set(x, -2.0 * bound).set(z, z, 0.5).set(v, v, 0.5).set(w, w, 0.5);
        model.addExpression("balance").set(v, 1).set(w, -1).level(0);

        return sense.solve(model);
    }

    /**
     * Optimise scale * (x^2 + y^2) / 2 (negated when maximising) subject to x + y >= 2 and x - y = 0. The
     * solution is x = y = 1, and the multiplier of the "sum" constraint is scale.
     */
    private static Result solveScaledObjective(final double scale, final Boolean projection, final Optimisation.Sense sense) {

        double weight = sense == Optimisation.Sense.MAX ? -scale : scale;

        ExpressionsBasedModel model = LagrangeTest.newScaledObjectiveModel(weight);
        model.options.convex().projection(projection);

        return sense.solve(model);
    }

    /**
     * A fixed variable, z = 0.5, is removed from the constraints before they reach the solver. The dual value
     * must still be reported with the model's own constraint instance, and the reduced gradient of the fixed
     * variable is reconstructed from it (see {@link Result#getDualValues()} for the sign convention). With
     * quadratic terms in z the objective's gradient with respect to z, at the solution, takes the place of
     * its linear coefficient.
     *
     * <pre>{@code
     * min  (x^2 + y^2) / 2 + 3z             s.t. x + y + z >= 2.5    x=y=1, lambda=1, rc_z = 3 - lambda = 2
     * min  (x^2 + y^2) / 2 - 3x - 3y + 3z   s.t. x + y + z <= 2.5    x=y=1, lambda=2, rc_z = 3 + lambda = 5
     * max  -(x^2 + y^2) / 2 + 3z            s.t. x + y + z = 2.5     x=y=1, lambda=-1, rc_z = -(-3 + lambda) = 4
     * min  f = (x^2 + y^2 + z^2) / 2 + xz + 3z                     s.t. x + y + z >= 2.5
     *      x=0.75, y=1.25, lambda=1.25, rc_z = (z + x + 3) - lambda = 3
     * max  -(f - 3x - 3y)                                           s.t. x + y + z <= 2.5
     *      x=0.75, y=1.25, lambda=1.75, rc_z = -((z + x + 3) + lambda) = -6
     * }</pre>
     */
    @Test
    public void testFixedInConstraint() {

        ExpressionsBasedModel lowerModel = new ExpressionsBasedModel();
        Variable lowerX = lowerModel.addVariable("x").lower(-10).upper(10);
        Variable lowerY = lowerModel.addVariable("y").lower(-10).upper(10);
        Variable lowerZ = lowerModel.addVariable("z").level(0.5);
        lowerModel.addExpression("objective").weight(1).set(lowerX, lowerX, 0.5).set(lowerY, lowerY, 0.5).set(lowerZ, 3);
        Expression lower = lowerModel.addExpression("constraint").set(lowerX, 1).set(lowerY, 1).set(lowerZ, 1).lower(2.5);

        LagrangeTest.assertFixedInConstraint(lowerModel.minimise(), lower, 1.0, 1.0, 1.0, 2.0);

        ExpressionsBasedModel upperModel = new ExpressionsBasedModel();
        Variable upperX = upperModel.addVariable("x").lower(-10).upper(10);
        Variable upperY = upperModel.addVariable("y").lower(-10).upper(10);
        Variable upperZ = upperModel.addVariable("z").level(0.5);
        upperModel.addExpression("objective").weight(1).set(upperX, upperX, 0.5).set(upperX, -3).set(upperY, upperY, 0.5).set(upperY, -3).set(upperZ, 3);
        Expression upper = upperModel.addExpression("constraint").set(upperX, 1).set(upperY, 1).set(upperZ, 1).upper(2.5);

        LagrangeTest.assertFixedInConstraint(upperModel.minimise(), upper, 1.0, 1.0, 2.0, 5.0);

        ExpressionsBasedModel equalityModel = new ExpressionsBasedModel();
        Variable equalityX = equalityModel.addVariable("x").lower(-10).upper(10);
        Variable equalityY = equalityModel.addVariable("y").lower(-10).upper(10);
        Variable equalityZ = equalityModel.addVariable("z").level(0.5);
        equalityModel.addExpression("objective").weight(1).set(equalityX, equalityX, -0.5).set(equalityY, equalityY, -0.5).set(equalityZ, 3);
        Expression equality = equalityModel.addExpression("constraint").set(equalityX, 1).set(equalityY, 1).set(equalityZ, 1).level(2.5);

        LagrangeTest.assertFixedInConstraint(equalityModel.maximise(), equality, 1.0, 1.0, -1.0, 4.0);

        ExpressionsBasedModel minQuadraticModel = new ExpressionsBasedModel();
        Variable minQuadraticX = minQuadraticModel.addVariable("x").lower(-10).upper(10);
        Variable minQuadraticY = minQuadraticModel.addVariable("y").lower(-10).upper(10);
        Variable minQuadraticZ = minQuadraticModel.addVariable("z").level(0.5);
        minQuadraticModel.addExpression("objective").weight(1).set(minQuadraticX, minQuadraticX, 0.5).set(minQuadraticY, minQuadraticY, 0.5)
                .set(minQuadraticZ, minQuadraticZ, 0.5).set(minQuadraticX, minQuadraticZ, 1).set(minQuadraticZ, 3);
        Expression minQuadratic = minQuadraticModel.addExpression("constraint").set(minQuadraticX, 1).set(minQuadraticY, 1).set(minQuadraticZ, 1).lower(2.5);

        LagrangeTest.assertFixedInConstraint(minQuadraticModel.minimise(), minQuadratic, 0.75, 1.25, 1.25, 3.0);

        ExpressionsBasedModel maxQuadraticModel = new ExpressionsBasedModel();
        Variable maxQuadraticX = maxQuadraticModel.addVariable("x").lower(-10).upper(10);
        Variable maxQuadraticY = maxQuadraticModel.addVariable("y").lower(-10).upper(10);
        Variable maxQuadraticZ = maxQuadraticModel.addVariable("z").level(0.5);
        maxQuadraticModel.addExpression("objective").weight(-1).set(maxQuadraticX, maxQuadraticX, 0.5).set(maxQuadraticY, maxQuadraticY, 0.5)
                .set(maxQuadraticZ, maxQuadraticZ, 0.5).set(maxQuadraticX, maxQuadraticZ, 1).set(maxQuadraticZ, 3).set(maxQuadraticX, -3)
                .set(maxQuadraticY, -3);
        Expression maxQuadratic = maxQuadraticModel.addExpression("constraint").set(maxQuadraticX, 1).set(maxQuadraticY, 1).set(maxQuadraticZ, 1).upper(2.5);

        LagrangeTest.assertFixedInConstraint(maxQuadraticModel.maximise(), maxQuadratic, 0.75, 1.25, 1.75, -6.0);
    }

    /**
     * Multivariable Quadratic Programming example taken from:
     * https://people.duke.edu/~hpgavin/cee201/LagrangeMultipliers.pdf
     */
    @Test
    public void testGavinAndScruggsExample() {

        NumberContext accuracy = NumberContext.of(2); // Example solutions are very much rounded

        RawStore mtrxQ = RawStore.wrap(new double[][] { { 2, 3 }, { 3, 10 } });
        R064Store mtrxC = FACTORY.column(-0.5, 0); // Defined negated the ojAlgo way

        ConvexSolver unconstrainedSolver = ConvexSolver.newBuilder().objective(mtrxQ, mtrxC).build();

        R064Store unconstrainedX = FACTORY.column(-0.45, 0.14);

        TestUtils.assertEquals(unconstrainedX, unconstrainedSolver.solve(), accuracy);

        RawStore mtrxAI = RawStore.wrap(new double[][] { { 3, 2 }, { 15, -3 } });
        R064Store mtrxBI = FACTORY.column(-2, 1);

        ConvexSolver equalityConstrainedSolver = ConvexSolver.newBuilder().objective(mtrxQ, mtrxC).equalities(mtrxAI, mtrxBI).build();

        R064Store equalityX = FACTORY.column(-0.10, -0.85);
        R064Store equalityL = FACTORY.column(3.55, -0.56);

        Result equalitySolution = equalityConstrainedSolver.solve();
        Access1D<?> equalityMultipliers = equalitySolution.getMultipliers().get();

        TestUtils.assertEquals(equalityX, equalitySolution, accuracy);
        TestUtils.assertEquals(equalityL, equalityMultipliers, accuracy);

        ConvexSolver inequalityConstrainedSolver = ConvexSolver.newBuilder().objective(mtrxQ, mtrxC).inequalities(mtrxAI, mtrxBI).build();

        R064Store inequalityX = FACTORY.column(-0.81, 0.21);
        R064Store inequalityL = FACTORY.column(0.16, 0);

        if (DEBUG) {
            inequalityConstrainedSolver.options.debug(Optimisation.Solver.class);
            inequalityConstrainedSolver.options.validate = true;
        }

        Result inequalitySolution = inequalityConstrainedSolver.solve();
        Access1D<?> inequalityMultipliers = inequalitySolution.getMultipliers().get();

        TestUtils.assertEquals(inequalityX, inequalitySolution, accuracy);
        TestUtils.assertEquals(inequalityL, inequalityMultipliers, accuracy);

        // The first constraint is active, and the second is not
        // Setting the first as an equality constraint, and only the other as an
        // inequality should give the same result.

        ConvexSolver mixConstrainedSolver = ConvexSolver.newBuilder().objective(mtrxQ, mtrxC).equalities(mtrxAI.row(0), mtrxBI.row(0))
                .inequalities(mtrxAI.row(1), mtrxBI.row(1)).build();

        Result mixSolution = mixConstrainedSolver.solve();
        Access1D<?> mixMultipliers = mixSolution.getMultipliers().get();

        TestUtils.assertEquals(inequalityX, mixSolution, accuracy);
        TestUtils.assertEquals(inequalityL, mixMultipliers, accuracy);
    }

    /**
     * Copy of {@link ConvexProblems#testP20200924()} and then modified...
     * <p>
     * Nocedal & Wright give the multipliers [3, -2] (because they defined KKT that way) but ojAlgo returns
     * [-3, 2].
     * <p>
     * Test for https://github.com/optimatika/ojAlgo/issues/280.
     * <p>
     * 2020-09-24: No multipliers was returned by org.ojalgo.optimisation.convex classes : Test from
     * 'Numerical Optimization', 2ed, (2006), Jorge Nocedal and Stephen J. Wright. QP Example 16.2 p453
     * minimize function F(x1,x2,x3) = 3*x1*x1 + 2*x1*x2 + x1*x3 + 2.5*x2*x2 + 2*x2*x3 + 2*x3*x3 - 8*x1 - 3*x2
     * - 3*x3 constraints x1 + x3 = 3, x2 + x3 = 0 result: x = [2, -1, 1]' multipliers = [3, -2]'
     *
     * @throws RecoverableCondition
     */
    @Test
    public void testNocedalAndWrightExample() throws RecoverableCondition {

        NumberContext accuracy = NumberContext.of(12);

        RawStore mtrxQ = RawStore.wrap(new double[][] { { 6, 2, 1 }, { 2, 5, 2 }, { 1, 2, 4 } });
        R064Store mtrxC = FACTORY.column(8, 3, 3); // Negated, because that how ojAgo expects it
        RawStore mtrxAE = RawStore.wrap(new double[][] { { 1, 0, 1 }, { 0, 1, 1 } });
        R064Store mtrxBE = FACTORY.column(3, 0);

        R064Store expectedX = FACTORY.column(2, -1, 1);
        R064Store expectedDual = FACTORY.column(-3, 2); // Negated, because N&W defined the KKT that way. Don't know why.

        MatrixStore<Double> bodyKKT = mtrxQ.right(mtrxAE.transpose()).below(mtrxAE);
        MatrixStore<Double> rhsKKT = mtrxC.below(mtrxBE);
        MatrixStore<Double> solutionKKT = expectedX.below(expectedDual);

        SolverTask<Double> equationSolver = SolverTask.R064.make(bodyKKT, rhsKKT);
        MatrixStore<Double> combinedSolution = equationSolver.solve(bodyKKT, rhsKKT);

        TestUtils.assertEquals(solutionKKT, combinedSolution, accuracy);

        Builder builder = ConvexSolver.newBuilder();
        builder.objective(mtrxQ, mtrxC);
        builder.equalities(mtrxAE, mtrxBE);

        Result result = builder.solve();

        TestUtils.assertEquals(expectedX, result, accuracy);

        // [A][x] = [b]
        MatrixStore<Double> computedB = mtrxAE.multiply(expectedX);
        TestUtils.assertEquals(mtrxBE, computedB, accuracy);

        // [Q][x] + [A]<sup>T</sup>[L] = [C]
        MatrixStore<Double> computedC = mtrxQ.multiply(expectedX).add(mtrxAE.transpose().multiply(expectedDual));
        TestUtils.assertEquals(mtrxC, computedC, accuracy);

        Optional<Access1D<?>> multipliers = result.getMultipliers();
        TestUtils.assertTrue("No multipliers present", multipliers.isPresent());
        TestUtils.assertEquals("Lagrangian Multipliers differ", expectedDual, multipliers.get(), accuracy);

        //  Test similar system where each equality constraint are converted into two inequality constraints.
        //  The result should be the same.
        Builder altBuilder = ConvexSolver.newBuilder();
        altBuilder.objective(mtrxQ, mtrxC);
        MatrixStore<Double> altAI = mtrxAE.below(mtrxAE.negate());
        MatrixStore<Double> altBI = mtrxBE.below(mtrxBE.negate());
        altBuilder.inequalities(altAI, altBI);

        Optimisation.Options options = new Optimisation.Options();
        Result altResult = altBuilder.build(options).solve();
        TestUtils.assertEquals(expectedX, result, accuracy);

        Optional<Access1D<?>> altMultipliers = altResult.getMultipliers();
        TestUtils.assertTrue("No multipliers present", altMultipliers.isPresent());

        R064Store expectedInequalityDual = FACTORY.column(0, 2, 3, 0);
        TestUtils.assertEquals(expectedInequalityDual, altMultipliers.get(), accuracy);

    }

    /**
     * Variable bounds are modelled as unit coefficient rows with the bound, in model units, as the right hand
     * side. Their multipliers must not be scaled by the variable's adjustment factor (derived from the bound
     * magnitude) when mapped back to the model. Covers the active set solver as well as the null-space
     * projection solver.
     */
    @Test
    public void testScaledBounds() {

        NumberContext accuracy = NumberContext.of(8);

        for (Boolean projection : new Boolean[] { Boolean.FALSE, Boolean.TRUE }) {
            for (Optimisation.Sense sense : Optimisation.Sense.values()) {
                for (double bound : new double[] { 0.001, 1.0, 1000.0 }) {

                    Result result = LagrangeTest.solveScaledBounds(bound, projection, sense);

                    TestUtils.assertStateNotLessThanOptimal(result);
                    TestUtils.assertEquals(bound, result.doubleValue(0), accuracy);
                    TestUtils.assertEquals(bound, result.doubleValue(1), accuracy);

                    TestUtils.assertEquals(bound, LagrangeTest.getDualValue(result, "x", Optimisation.ConstraintType.UPPER), accuracy);
                    TestUtils.assertEquals(bound, LagrangeTest.getDualValue(result, "z", Optimisation.ConstraintType.LOWER), accuracy);
                }
            }
        }
    }

    /**
     * The objective is scaled (by its adjustment factor) before it is handed to the solver. The dual values
     * and the reduced gradient must be mapped back to model units, so they scale with the objective. Covers
     * the active set solver as well as the null-space projection solver.
     */
    @Test
    public void testScaledObjective() {

        NumberContext accuracy = NumberContext.of(8);

        for (Boolean projection : new Boolean[] { Boolean.FALSE, Boolean.TRUE }) {
            for (Optimisation.Sense sense : Optimisation.Sense.values()) {

                Result reference = LagrangeTest.solveScaledObjective(1.0, projection, sense);

                for (double scale : new double[] { 0.001, 0.01, 1.0, 1000.0 }) {

                    Result result = LagrangeTest.solveScaledObjective(scale, projection, sense);

                    TestUtils.assertStateNotLessThanOptimal(result);
                    TestUtils.assertEquals(1.0, result.doubleValue(0), accuracy);
                    TestUtils.assertEquals(1.0, result.doubleValue(1), accuracy);

                    TestUtils.assertEquals(scale, LagrangeTest.getDualValue(result, "sum", Optimisation.ConstraintType.LOWER), accuracy);

                    if (reference.getReducedGradient().isPresent()) {
                        Access1D<?> expected = reference.getReducedGradient().get().get();
                        Access1D<?> actual = result.getReducedGradient().get().get();
                        for (int j = 0; j < 2; j++) {
                            TestUtils.assertEquals(scale * expected.doubleValue(j), actual.doubleValue(j), accuracy);
                        }
                    }
                }
            }
        }
    }

    /**
     * The objective value reported by each of the convex solvers must be in model units, regardless of how
     * the objective is scaled internally. Solving an {@link ExpressionsBasedModel} re-evaluates the value
     * from the solution, but the branch-and-bound uses the node solvers' values directly. At the solution x =
     * y = 1 the value is scale.
     */
    @Test
    public void testScaledObjectiveValue() {

        NumberContext accuracy = NumberContext.of(8);
        Optimisation.Options options = new Optimisation.Options();

        for (double scale : new double[] { 0.001, 0.01, 1.0, 1000.0 }) {

            ExpressionsBasedModel model = LagrangeTest.newScaledObjectiveModel(scale);

            Result activeSet = BasePrimitiveSolver.newSolver(ConvexSolver.copy(model, R064Store.FACTORY), options).solve();
            TestUtils.assertStateNotLessThanOptimal(activeSet);
            TestUtils.assertEquals(scale, activeSet.getValue(), accuracy);

            Result nullSpace = new NullSpaceASS(options, ConvexSolver.copy(model, R064Store.FACTORY)).solve();
            TestUtils.assertStateNotLessThanOptimal(nullSpace);
            TestUtils.assertEquals(scale, nullSpace.getValue(), accuracy);

            Result extendedPrecision = new IterativeRefinementSolver(options, ConvexSolver.copy(model, GenericStore.R128)).solve();
            TestUtils.assertStateNotLessThanOptimal(extendedPrecision);
            TestUtils.assertEquals(scale, extendedPrecision.getValue(), accuracy);

            AlternatingDirectionSolver.Composer<Double> composer = AlternatingDirectionSolver.build(model, R064Store.FACTORY);
            Result alternatingDirection = new AlternatingDirectionSolver(composer.toProblem(), options, composer.getStructure()).solve();
            TestUtils.assertStateNotLessThanOptimal(alternatingDirection);
            TestUtils.assertEquals(scale, alternatingDirection.getValue(), accuracy);

            Result alternatingDirectionExtended = new IterativeRefinementForAlternatingDirectionSolver(options,
                    AlternatingDirectionSolver.build(model, GenericStore.R128)).solve();
            TestUtils.assertTrue(alternatingDirectionExtended.getState().isFeasible());
            TestUtils.assertEquals(scale, alternatingDirectionExtended.getValue(), accuracy);
        }
    }

}
