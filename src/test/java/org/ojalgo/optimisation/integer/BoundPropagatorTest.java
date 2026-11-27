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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * Bound propagation, as part of branching
 */
public class BoundPropagatorTest extends OptimisationIntegerTests {

    /**
     * Counts the node problems solved to optimality, and the infeasible nodes, evaluated or not
     */
    private static final class Counter extends ModelStrategy.AbstractStrategy {

        private final int[] myCounts;

        Counter(final ExpressionsBasedModel model, final IntegerStrategy strategy, final int[] counts) {
            super(model, strategy);
            myCounts = counts;
        }

        @Override
        protected void markInfeasible(final NodeKey key, final boolean found, final double incumbentValue) {
            myCounts[1]++;
            super.markInfeasible(key, found, incumbentValue);
        }

        @Override
        protected void onNodeSolved(final NodeKey key, final Optimisation.Result child, final double childObj, final boolean minimisation) {
            myCounts[0]++;
            super.onNodeSolved(key, child, childObj, minimisation);
        }

    }

    private static NodeKey branchUp(final ExpressionsBasedModel model, final int index, final Expression objectiveLimit) {
        BoundPropagator propagator = BoundPropagatorTest.newPropagator(model);
        if (objectiveLimit != null) {
            propagator.setObjectiveLimit(objectiveLimit);
        }
        return new NodeKey(model).createUpperBranch(index, 0.5, 0.0, propagator, null);
    }

    /**
     * Random rows with integer variables between 0 and 5, each with its limit exactly met by a given integer
     * point. Propagating a branch, or new bounds of several variables, that point satisfies must neither find
     * it infeasible nor exclude it, and the same for every other integer point that satisfies the row and the
     * new bounds (in exact arithmetic).
     *
     * @return The number of cases where it did
     */
    private static int countWrong(final int magnitude, final int nbCases, final boolean several) {

        int nbVariables = 4;
        int size = 5;

        Random random = new Random(magnitude);
        int retVal = 0;

        for (int n = 0; n < nbCases; n++) {

            int[] point = new int[nbVariables];
            BigDecimal[] coefficients = new BigDecimal[nbVariables];
            BigDecimal limit = BigDecimal.ZERO;
            for (int j = 0; j < nbVariables; j++) {
                point[j] = random.nextInt(size + 1);
                coefficients[j] = BoundPropagatorTest.randomCoefficient(random, magnitude);
                limit = limit.add(coefficients[j].multiply(BigDecimal.valueOf(point[j])));
            }

            ExpressionsBasedModel model = new ExpressionsBasedModel();
            Expression row = model.newExpression("row");
            for (int j = 0; j < nbVariables; j++) {
                row.set(model.newVariable("x" + j).integer().lower(0).upper(size), coefficients[j]);
            }
            boolean upper = random.nextBoolean();
            if (upper) {
                row.upper(limit);
            } else {
                row.lower(limit);
            }

            int[] lowerBounds = new int[nbVariables];
            int[] upperBounds = new int[nbVariables];
            Arrays.fill(upperBounds, size);

            NodeKey root = new NodeKey(model);
            BoundPropagator propagator = BoundPropagatorTest.newPropagator(model);
            NodeKey node;

            if (several) {
                for (int j = 0; j < nbVariables; j++) {
                    if (random.nextBoolean()) {
                        lowerBounds[j] = random.nextInt(point[j] + 1);
                    }
                    if (random.nextBoolean()) {
                        upperBounds[j] = point[j] + random.nextInt(size - point[j] + 1);
                    }
                }
                node = root.withTightened(lowerBounds.clone(), upperBounds.clone(), propagator);
            } else {
                int branch = random.nextInt(nbVariables);
                int value = point[branch];
                if (value > 0 && (value == size || random.nextBoolean())) {
                    lowerBounds[branch] = value;
                    node = root.createUpperBranch(branch, value - 0.5, 0.0, propagator, null);
                } else {
                    upperBounds[branch] = value;
                    node = root.createLowerBranch(branch, value + 0.5, 0.0, propagator, null);
                }
            }

            boolean wrong = false;
            int[] x = new int[nbVariables];
            int limitPoints = (int) Math.round(Math.pow(size + 1, nbVariables));
            for (int c = 0; c < limitPoints && !wrong; c++) {
                int code = c;
                BigDecimal activity = BigDecimal.ZERO;
                boolean within = true;
                for (int j = 0; j < nbVariables; j++) {
                    x[j] = code % (size + 1);
                    code /= size + 1;
                    activity = activity.add(coefficients[j].multiply(BigDecimal.valueOf(x[j])));
                    within &= x[j] >= lowerBounds[j] && x[j] <= upperBounds[j];
                }
                if (within && (upper ? activity.compareTo(limit) <= 0 : activity.compareTo(limit) >= 0)) {
                    if (node == null || !node.isFeasible()) {
                        wrong = true;
                    } else {
                        for (int j = 0; j < nbVariables; j++) {
                            wrong |= x[j] < node.getLowerBound(j) || x[j] > node.getUpperBound(j);
                        }
                    }
                }
            }
            if (wrong) {
                retVal++;
            }
        }

        return retVal;
    }

    /**
     * Random models with integer and continuous variables, and constraints that all have limits exactly met
     * by a given point. Neither the root nor a branch with that point may exclude it. (The bounds of the
     * continuous variables that are derived at the root are used for the branches.)
     *
     * @return The number of cases where it did
     */
    private static int countWrongMixed(final int magnitude, final int nbCases) {

        int nbVariables = 6;
        int nbRows = 5;
        int size = 5;

        Random random = new Random(magnitude);
        int retVal = 0;

        for (int n = 0; n < nbCases; n++) {

            ExpressionsBasedModel model = new ExpressionsBasedModel();
            BigDecimal[] point = new BigDecimal[nbVariables];

            // The first 3 are integer
            for (int j = 0; j < nbVariables; j++) {
                Variable variable = model.newVariable("x" + j).lower(0);
                if (j < 3) {
                    point[j] = BigDecimal.valueOf(random.nextInt(size + 1));
                    variable.integer().upper(size);
                } else {
                    point[j] = BigDecimal.valueOf(random.nextInt(5001), 3);
                    if (random.nextBoolean()) {
                        variable.upper(10);
                    }
                }
            }

            for (int i = 0; i < nbRows; i++) {
                Expression row = model.newExpression("row" + i);
                BigDecimal limit = BigDecimal.ZERO;
                for (int j = 0; j < nbVariables; j++) {
                    if (random.nextInt(3) > 0) {
                        BigDecimal coefficient = BoundPropagatorTest.randomCoefficient(random, magnitude);
                        row.set(model.getVariable(j), coefficient);
                        limit = limit.add(coefficient.multiply(point[j]));
                    }
                }
                switch (random.nextInt(3)) {
                case 0:
                    row.lower(limit);
                    break;
                case 1:
                    row.upper(limit);
                    break;
                default:
                    row.level(limit);
                    break;
                }
            }

            BoundPropagator propagator = BoundPropagatorTest.newPropagator(model);
            NodeKey node = NodeKey.newRoot(model, propagator);

            for (int j = 0; j < 3 && node != null && node.isFeasible(); j++) {
                int value = point[j].intValue();
                if (value > node.getLowerBound(j)) {
                    node = node.createUpperBranch(j, value - 0.5, 0.0, propagator, null);
                }
                if (node.isFeasible() && value < node.getUpperBound(j)) {
                    node = node.createLowerBranch(j, value + 0.5, 0.0, propagator, null);
                }
            }

            boolean wrong = node == null || !node.isFeasible();
            for (int j = 0; j < 3 && !wrong; j++) {
                wrong = point[j].intValue() < node.getLowerBound(j) || point[j].intValue() > node.getUpperBound(j);
            }
            if (wrong) {
                retVal++;
            }
        }

        return retVal;
    }

    /**
     * Choose one of 3 values for y: b0 + b1 + b2 = 1 and y = 3 b0 + 5 b1 + 7 b2. Minimise y.
     */
    private static ExpressionsBasedModel newChoice() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable b0 = model.newVariable("b0").binary();
        Variable b1 = model.newVariable("b1").binary();
        Variable b2 = model.newVariable("b2").binary();
        Variable y = model.newVariable("y").integer().lower(0).upper(10).weight(1);

        model.newExpression("one").level(1).set(b0, 1).set(b1, 1).set(b2, 1);
        model.newExpression("value").level(0).set(y, 1).set(b0, -3).set(b1, -5).set(b2, -7);

        return model;
    }

    private static BoundPropagator newPropagator(final ExpressionsBasedModel model) {
        return BoundPropagator.of(model, IntegerStrategy.DEFAULT.newModelStrategy(model));
    }

    /**
     * 3 decimals, and up to 10^magnitude in size
     */
    private static BigDecimal randomCoefficient(final Random random, final int magnitude) {
        long unscaled = 1L + (long) (random.nextDouble() * 1000.0);
        BigDecimal retVal = new BigDecimal(BigInteger.valueOf(unscaled), 3).scaleByPowerOfTen(random.nextInt(magnitude + 1));
        return random.nextBoolean() ? retVal : retVal.negate();
    }

    /**
     * Generalised assignment: 8 jobs to 3 machines with capacities, minimise cost.
     */
    @Test
    public void testAssignment() {

        int[][] cost = { { 9, 2, 7, 8, 4, 6, 3, 5 }, { 6, 4, 3, 7, 8, 2, 9, 4 }, { 5, 8, 1, 8, 3, 7, 4, 6 } };
        int[][] size = { { 5, 7, 4, 6, 3, 8, 5, 4 }, { 6, 5, 7, 4, 5, 3, 6, 7 }, { 4, 6, 5, 7, 6, 5, 3, 5 } };
        int[] capacity = { 15, 15, 14 };

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable[][] x = new Variable[3][8];
        for (int m = 0; m < 3; m++) {
            for (int j = 0; j < 8; j++) {
                x[m][j] = model.newVariable("x" + m + j).binary().weight(cost[m][j]);
            }
        }
        for (int j = 0; j < 8; j++) {
            Expression once = model.newExpression("job" + j).level(1);
            for (int m = 0; m < 3; m++) {
                once.set(x[m][j], 1);
            }
        }
        for (int m = 0; m < 3; m++) {
            Expression load = model.newExpression("machine" + m).upper(capacity[m]);
            for (int j = 0; j < 8; j++) {
                load.set(x[m][j], size[m][j]);
            }
        }

        int expected = Integer.MAX_VALUE;
        for (int code = 0; code < 6561; code++) {
            int[] load = new int[3];
            int total = 0;
            for (int j = 0, rest = code; j < 8; j++, rest /= 3) {
                load[rest % 3] += size[rest % 3][j];
                total += cost[rest % 3][j];
            }
            if (load[0] <= capacity[0] && load[1] <= capacity[1] && load[2] <= capacity[2]) {
                expected = Math.min(expected, total);
            }
        }

        Optimisation.Result result = model.minimise();

        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(expected, result.getValue(), 1E-9);
    }

    @Test
    public void testChoiceConsequences() {

        // b1 = 1 forces b0 = b2 = 0, and y = 5
        NodeKey branch = BoundPropagatorTest.branchUp(BoundPropagatorTest.newChoice(), 1, null);

        TestUtils.assertEquals(0.0, branch.getUpperBound(0));
        TestUtils.assertEquals(1.0, branch.getLowerBound(1));
        TestUtils.assertEquals(0.0, branch.getUpperBound(2));
        TestUtils.assertEquals(5.0, branch.getLowerBound(3));
        TestUtils.assertEquals(5.0, branch.getUpperBound(3));
    }

    @Test
    public void testChoiceInfeasible() {

        // With y <= 4, b1 = 1 (y = 5) is infeasible
        ExpressionsBasedModel model = BoundPropagatorTest.newChoice();
        model.getVariable(3).upper(4);

        TestUtils.assertFalse(BoundPropagatorTest.branchUp(model, 1, null).isFeasible());
        TestUtils.assertTrue(BoundPropagatorTest.branchUp(model, 0, null).isFeasible());
    }

    @Test
    public void testChoiceObjectiveLimit() {

        // An incumbent with y = 4 means y <= 4 from now on (minimisation), so b1 = 1 (y = 5) is infeasible,
        // but b0 = 1 (y = 3) is not
        ExpressionsBasedModel model = BoundPropagatorTest.newChoice();
        Expression limit = model.copy().limitObjective(null, BigDecimal.valueOf(4));

        TestUtils.assertFalse(BoundPropagatorTest.branchUp(model, 1, limit).isFeasible());
        TestUtils.assertTrue(BoundPropagatorTest.branchUp(model, 0, limit).isFeasible());
    }

    /**
     * The bounds of a continuous variable are used, x + c = 5 and c <= 2 b, but not kept: they don't apply to
     * the next node. And neither does anything else.
     */
    @Test
    public void testInstanceReused() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable b = model.newVariable("b").binary();
        Variable x = model.newVariable("x").integer().lower(0).upper(10);
        Variable c = model.newVariable("c").lower(0).upper(10);
        Variable y = model.newVariable("y").integer().lower(0).upper(10);
        model.newExpression("r0").level(5).set(x, 1).set(c, 1);
        model.newExpression("r1").upper(0).set(c, 1).set(b, -2);
        model.newExpression("r2").upper(8).set(x, 1).set(y, 2);

        BoundPropagator propagator = BoundPropagatorTest.newPropagator(model);
        NodeKey root = new NodeKey(model);

        for (int i = 0; i < 3; i++) {

            // c = 0, so x = 5, so y <= 1
            NodeKey down = root.createLowerBranch(0, 0.5, 0.0, propagator, null);
            TestUtils.assertEquals(5.0, down.getLowerBound(1));
            TestUtils.assertEquals(5.0, down.getUpperBound(1));
            TestUtils.assertEquals(1.0, down.getUpperBound(2));

            // c <= 2, so x >= 3, so y <= 2
            NodeKey up = root.createUpperBranch(0, 0.5, 0.0, propagator, null);
            TestUtils.assertEquals(3.0, up.getLowerBound(1));
            TestUtils.assertEquals(5.0, up.getUpperBound(1));
            TestUtils.assertEquals(2.0, up.getUpperBound(2));

            // y >= 5 means x < 0
            TestUtils.assertFalse(root.createUpperBranch(2, 4.5, 0.0, propagator, null).isFeasible());

            // y >= 3 means x <= 2, and c >= 3, but c <= 2 whatever b is
            TestUtils.assertFalse(root.createUpperBranch(2, 2.5, 0.0, propagator.newInstance(), null).isFeasible());
        }

        TestUtils.assertEquals(10.0, root.getUpperBound(1));
        TestUtils.assertEquals(10.0, root.getUpperBound(2));
    }

    /**
     * A node's LP solution has x0 = x1 = 0 (at their lower bounds) and x2 = 5 (at its upper bound), reduced
     * costs 4, 3 and -2, and the gap to the incumbent is 10. Fixing by reduced cost, one variable at a time,
     * gives x0 <= 2 and x1 <= 3 (and nothing for x2). The row 4 x0 + 3 x1 - 2 x2 <= 10 + 0 + 0 - 10 = 0 gives
     * that too, but propagated with a branch it's stronger.
     */
    @Test
    public void testReducedCostRow() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        model.newVariable("x0").integer().lower(0).upper(5);
        model.newVariable("x1").integer().lower(0).upper(5);
        model.newVariable("x2").integer().lower(0).upper(5);

        BoundPropagator propagator = BoundPropagatorTest.newPropagator(model);
        NodeKey root = new NodeKey(model);

        double[] gradient = { 4.0, 3.0, -2.0 };
        double[] solution = { 0.0, 0.0, 5.0 };
        BoundPropagator.Cutoff reducedCost = propagator.newReducedCostCutoff(j -> gradient[j], j -> solution[j], 10.0);

        // x2 >= 5 is where it is, the same as fixing one at a time
        NodeKey up = root.createUpperBranch(2, 4.5, 0.0, propagator, reducedCost);
        TestUtils.assertEquals(2.0, up.getUpperBound(0));
        TestUtils.assertEquals(3.0, up.getUpperBound(1));

        // x2 <= 4 uses 2 of the gap: x1 <= 2
        NodeKey down = root.createLowerBranch(2, 4.5, 0.0, propagator, reducedCost);
        TestUtils.assertEquals(2.0, down.getUpperBound(0));
        TestUtils.assertEquals(2.0, down.getUpperBound(1));

        // x2 <= 3 uses 4: x0 <= 1 and x1 <= 2
        down = root.createLowerBranch(2, 3.5, 0.0, propagator, reducedCost);
        TestUtils.assertEquals(1.0, down.getUpperBound(0));
        TestUtils.assertEquals(2.0, down.getUpperBound(1));

        // x0 >= 1 uses 4 of the gap: x1 <= 2 and x2 >= 2
        up = root.createUpperBranch(0, 0.5, 0.0, propagator, reducedCost);
        TestUtils.assertEquals(1.0, up.getLowerBound(0));
        TestUtils.assertEquals(2.0, up.getUpperBound(0));
        TestUtils.assertEquals(2.0, up.getUpperBound(1));
        TestUtils.assertEquals(2.0, up.getLowerBound(2));

        // x0 >= 2 uses 8: x1 = 0 and x2 >= 4
        up = root.createUpperBranch(0, 1.5, 0.0, propagator, reducedCost);
        TestUtils.assertEquals(0.0, up.getUpperBound(1));
        TestUtils.assertEquals(4.0, up.getLowerBound(2));

        // x0 >= 3 uses 12, more than the gap
        TestUtils.assertFalse(root.createUpperBranch(0, 2.5, 0.0, propagator, reducedCost).isFeasible());

        // Without the row there's nothing to propagate, and it's not kept between calls
        TestUtils.assertTrue(root.createUpperBranch(0, 2.5, 0.0, propagator, null).isFeasible());
        TestUtils.assertEquals(5.0, root.createUpperBranch(0, 0.5, 0.0, propagator, null).getUpperBound(1));
    }

    @Test
    public void testRoot() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable b = model.newVariable("b").integer().lower(0).upper(5);
        Variable x = model.newVariable("x").integer().lower(0).upper(10);
        Variable y = model.newVariable("y").integer().lower(0).upper(10);
        Variable c = model.newVariable("c").lower(0);
        Variable d = model.newVariable("d").lower(0);
        model.newExpression("r0").upper(4).set(c, 1).set(d, 1);
        model.newExpression("r1").upper(0).set(x, 1).set(y, 1).set(c, -1);
        model.newExpression("r2").lower(2).set(b, 1).set(x, -1);
        model.newExpression("r3").upper(8).set(b, 4).set(d, 1);

        // b <= 2, and then b >= 2
        BoundPropagator propagator = BoundPropagatorTest.newPropagator(model);
        NodeKey root = NodeKey.newRoot(model, propagator);

        TestUtils.assertEquals(2.0, root.getLowerBound(0));
        TestUtils.assertEquals(2.0, root.getUpperBound(0));

        // ...so that x = 0. And y <= 4, but that's not fixed
        TestUtils.assertEquals(0.0, root.getUpperBound(1));
        TestUtils.assertEquals(10.0, root.getUpperBound(2));

        // It's known that c <= 4, also when it's other constraints that are propagated
        NodeKey branch = root.createUpperBranch(2, 2.5, 0.0, propagator, null);
        TestUtils.assertEquals(3.0, branch.getLowerBound(2));
        TestUtils.assertEquals(4.0, branch.getUpperBound(2));
        TestUtils.assertFalse(root.createUpperBranch(2, 4.5, 0.0, propagator, null).isFeasible());
        TestUtils.assertFalse(root.createUpperBranch(2, 4.5, 0.0, propagator.newInstance(), null).isFeasible());

        // No solution at all
        model.newExpression("r4").lower(1).set(x, 1).set(d, -1).set(c, -1);
        model.newExpression("r5").lower(5).set(y, 1);
        TestUtils.assertTrue(NodeKey.newRoot(model, BoundPropagatorTest.newPropagator(model)) == null);
        TestUtils.assertEquals(Optimisation.State.INFEASIBLE, model.minimise().getState());
    }

    /**
     * There's no better solution than the one to start with
     */
    @Test
    public void testRootNotBetter() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.newVariable("x").integer().lower(0).upper(10).weight(2).value(BigDecimal.ONE);
        Variable y = model.newVariable("y").integer().lower(0).upper(10).weight(3).value(BigDecimal.ONE);
        model.newExpression("r0").lower(2).set(x, 1).set(y, 1);
        model.newExpression("r1").lower(1).set(y, 1);

        Optimisation.Result result = model.minimise();

        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(5.0, result.getValue(), 1E-9);
    }

    /**
     * The bounds of several variables at once, as with the reduced cost fixings
     */
    @Test
    public void testSeveralBounds() {

        ExpressionsBasedModel model = BoundPropagatorTest.newChoice();
        BoundPropagator propagator = BoundPropagatorTest.newPropagator(model);
        NodeKey root = new NodeKey(model);

        TestUtils.assertTrue(root.withTightened(root.copyLowerBounds(), root.copyUpperBounds(), propagator) == root);

        // b0 = b1 = 0 forces b2 = 1 and y = 7, neither of them does so alone
        int[] upperBounds = root.copyUpperBounds();
        upperBounds[0] = 0;
        upperBounds[1] = 0;
        NodeKey tightened = root.withTightened(root.copyLowerBounds(), upperBounds, propagator);

        TestUtils.assertEquals(root.sequence, tightened.sequence);
        TestUtils.assertEquals(1.0, tightened.getLowerBound(2));
        TestUtils.assertEquals(7.0, tightened.getLowerBound(3));
        TestUtils.assertEquals(7.0, tightened.getUpperBound(3));
        TestUtils.assertEquals(1.0, root.getUpperBound(0));
        TestUtils.assertEquals(0.0, root.getLowerBound(2));

        ModelStrategy strategy = IntegerStrategy.DEFAULT.newModelStrategy(model);
        TestUtils.assertTrue(tightened.isWithinBounds(ArrayR064.wrap(0.0, 0.0, 1.0, 7.0), strategy));
        TestUtils.assertFalse(tightened.isWithinBounds(ArrayR064.wrap(0.5, 0.0, 0.5, 5.0), strategy));
        TestUtils.assertTrue(root.isWithinBounds(ArrayR064.wrap(0.5, 0.0, 0.5, 5.0), strategy));

        // And then b2 = 0 too
        upperBounds = root.copyUpperBounds();
        upperBounds[0] = 0;
        upperBounds[1] = 0;
        upperBounds[2] = 0;
        TestUtils.assertTrue(root.withTightened(root.copyLowerBounds(), upperBounds, propagator) == null);
    }

    /**
     * 0.1 x + M b &lt;= M + 0.3 with b = 1 means x &lt;= 3, also when M is so large that 0.3 is not much more
     * than the rounding errors
     */
    @Test
    public void testLargeCoefficient() {

        for (int magnitude = 4; magnitude <= 13; magnitude++) {

            BigDecimal large = BigDecimal.TEN.pow(magnitude);

            ExpressionsBasedModel model = new ExpressionsBasedModel();
            Variable b = model.newVariable("b").binary();
            Variable x = model.newVariable("x").integer().lower(0).upper(10);
            model.newExpression("row").upper(large.add(new BigDecimal("0.3"))).set(b, large).set(x, new BigDecimal("0.1"));

            NodeKey branch = BoundPropagatorTest.branchUp(model, 0, null);

            TestUtils.assertTrue("M = 1e" + magnitude + ": x <= " + branch.getUpperBound(1), branch.getUpperBound(1) >= 3.0);
        }
    }

    /**
     * Propagation must not cut off any integer solution, however large the coefficients
     */
    @Test
    public void testNeverCutsOffSolutions() {

        for (int magnitude = 0; magnitude <= 15; magnitude += 3) {
            TestUtils.assertEquals("Coefficients up to 1e" + magnitude, 0, BoundPropagatorTest.countWrong(magnitude, 400, false));
            TestUtils.assertEquals("Coefficients up to 1e" + magnitude, 0, BoundPropagatorTest.countWrong(magnitude, 400, true));
            TestUtils.assertEquals("Coefficients up to 1e" + magnitude, 0, BoundPropagatorTest.countWrongMixed(magnitude, 1000));
        }
    }

    /**
     * Small models, with an objective function that gives reduced cost fixings, compared to trying all
     * solutions
     */
    @Test
    public void testRandom() {

        int nbVariables = 7;
        int size = 3;
        int nbPoints = (int) Math.round(Math.pow(size + 1, nbVariables));

        Random random = new Random(42);

        for (int n = 0; n < 300; n++) {

            boolean minimise = random.nextBoolean();
            double[] weights = new double[nbVariables];
            int[][] coefficients = new int[4][nbVariables];
            int[] limits = new int[4];

            ExpressionsBasedModel model = new ExpressionsBasedModel();
            for (int j = 0; j < nbVariables; j++) {
                weights[j] = (1 + random.nextInt(2000)) / 100.0;
                model.newVariable("x" + j).integer().lower(0).upper(size).weight(weights[j]);
            }
            for (int i = 0; i < 4; i++) {
                Expression row = model.newExpression("row" + i);
                int sum = 0;
                for (int j = 0; j < nbVariables; j++) {
                    coefficients[i][j] = random.nextInt(4) == 0 ? -random.nextInt(8) : random.nextInt(20);
                    sum += coefficients[i][j] * size;
                    row.set(model.getVariable(j), coefficients[i][j]);
                }
                limits[i] = sum / 2;
                if (minimise) {
                    row.lower(limits[i]);
                } else {
                    row.upper(limits[i]);
                }
            }

            double expected = minimise ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
            for (int c = 0; c < nbPoints; c++) {
                int code = c;
                double value = 0.0;
                int[] activities = new int[4];
                for (int j = 0; j < nbVariables; j++, code /= size + 1) {
                    value += weights[j] * (code % (size + 1));
                    for (int i = 0; i < 4; i++) {
                        activities[i] += coefficients[i][j] * (code % (size + 1));
                    }
                }
                boolean feasible = true;
                for (int i = 0; i < 4; i++) {
                    feasible &= minimise ? activities[i] >= limits[i] : activities[i] <= limits[i];
                }
                if (feasible) {
                    expected = minimise ? Math.min(expected, value) : Math.max(expected, value);
                }
            }

            Optimisation.Result result = minimise ? model.minimise() : model.maximise();

            if (Double.isInfinite(expected)) {
                TestUtils.assertEquals(Optimisation.State.INFEASIBLE, result.getState());
            } else {
                TestUtils.assertStateNotLessThanOptimal(result);
                TestUtils.assertEquals("Model " + n, expected, result.getValue(), 1E-4 * Math.max(1.0, Math.abs(expected)));
            }
        }
    }

    /**
     * 2a + 2b + 2c + s = 3 with a, b and c binary, and s between 0 and 0.5: no integer solution. At the root
     * that's not known, but with a = 0 both b and c must be 1, and with a = 1 they must be 0. Neither branch is
     * evaluated, but they're reported to the strategy as any other infeasible node.
     */
    @Test
    public void testPartOfTheSearch() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable a = model.newVariable("a").binary().weight(3);
        Variable b = model.newVariable("b").binary().weight(2);
        Variable c = model.newVariable("c").binary().weight(1);
        Variable s = model.newVariable("s").lower(0).upper(0.5);
        model.newExpression("row").level(3).set(a, 2).set(b, 2).set(c, 2).set(s, 1);

        int[] counts = new int[2];

        model.options.parallelism(1);
        model.options.integer(IntegerStrategy.DEFAULT.withCutConfiguration(IntegerStrategy.DEFAULT.getCutConfiguration().withTypes())
                .withModelStrategyFactory((m, strategy) -> new Counter(m, strategy, counts)));

        TestUtils.assertEquals(Optimisation.State.INFEASIBLE, model.minimise().getState());

        TestUtils.assertEquals("Solved", 1, counts[0]);
        TestUtils.assertEquals("Infeasible", 2, counts[1]);
    }

}
