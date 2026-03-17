/*
 * Copyright 1997-2025 Optimatika
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
package org.ojalgo.optimisation.linear;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.matrix.store.R064CSR;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.OptimisationCase;
import org.ojalgo.optimisation.UpdatableSolver;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.integer.TestBasicMIP;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.context.NumberContext;

/**
 * Tests {@link LinearSolver} as a branch-and-bound sub-solver, simulating a B&B dive on a small MIP. Uses
 * reduced gradients and dual multipliers to guide branching.
 * <ul>
 * <li>{@link #testLowLevelSolver()} — uses {@link LinearSolver.Builder} and {@link UpdatableSolver} methods
 * directly.
 * <li>{@link #testHighLevelModel()} — uses {@link ExpressionsBasedModel}, reads reduced gradients from the
 * {@link Result}, and modifies variable bounds on the model between solves.
 * </ul>
 */
public class BranchAndBoundSubSolverTest extends OptimisationLinearTests {

    /**
     * Branching decision: variable index and preferred direction.
     */
    static final class BranchDecision {

        final boolean floor;
        final int variable;

        BranchDecision(final int variable, final boolean floor) {
            this.variable = variable;
            this.floor = floor;
        }
    }

    private static final NumberContext ACCURACY = NumberContext.of(7);

    /**
     * Fractionality of a value: distance to the nearest integer.
     */
    static double fractionality(final double value) {
        return Math.min(value - Math.floor(value), Math.ceil(value) - value);
    }

    /**
     * True when every variable value is within tolerance of an integer.
     */
    static boolean isIntegerFeasible(final Result result, final int nbVars, final double tolerance) {
        for (int j = 0; j < nbVars; j++) {
            if (BranchAndBoundSubSolverTest.fractionality(result.doubleValue(j)) > tolerance) {
                return false;
            }
        }
        return true;
    }

    /**
     * Select branching variable and direction using {@link LinearSolver#getReducedGradient(int)}. The
     * magnitude {@code |rc_j| * frac_j} scores candidates; the sign picks the direction (positive → floor,
     * negative → ceil, zero → nearest integer).
     */
    static BranchDecision selectBranch(final LinearSolver solver, final Result result, final int nbVars) {

        int bestVar = -1;
        double bestScore = 0.0;
        boolean bestFloor = true;

        for (int j = 0; j < nbVars; j++) {
            double xj = result.doubleValue(j);
            double frac = BranchAndBoundSubSolverTest.fractionality(xj);
            if (frac <= 1E-6) {
                continue;
            }

            double rc = solver.getReducedGradient(j);

            double score = Math.abs(rc) * frac + frac * 1E-6;

            if (score > bestScore) {
                bestScore = score;
                bestVar = j;

                if (rc > 1E-12) {
                    bestFloor = true;
                } else if (rc < -1E-12) {
                    bestFloor = false;
                } else {
                    bestFloor = xj - Math.floor(xj) <= Math.ceil(xj) - xj;
                }
            }
        }

        return new BranchDecision(bestVar, bestFloor);
    }

    /**
     * Verify dual non-negativity for inequality constraints and complementary slackness (rc=0 for interior
     * variables).
     */
    static void verifyDualProperties(final String tag, final LinearSolver solver, final Result result, final double[] lowerBounds, final double[] upperBounds) {

        for (int i = 0; i < 2; i++) {
            double dual = solver.getDualMultiplier(i);
            TestUtils.assertTrue(tag + " ineq dual" + i + " >= 0", dual >= -1E-8);
        }

        for (int j = 0; j < lowerBounds.length; j++) {
            double xj = result.doubleValue(j);
            double rc = solver.getReducedGradient(j);
            boolean strictlyInterior = xj > lowerBounds[j] + 1E-8 && xj < upperBounds[j] - 1E-8;
            if (strictlyInterior) {
                TestUtils.assertEquals(tag + " rc" + j + " (interior/basic)", 0.0, rc, ACCURACY);
            }
        }
    }

    /**
     * Verify that the result satisfies all original constraints and the (possibly tightened) variable bounds.
     */
    static void verifyFeasibility(final String tag, final Result result, final double[] lowerBounds, final double[] upperBounds) {

        double[] x = new double[lowerBounds.length];
        for (int j = 0; j < lowerBounds.length; j++) {
            x[j] = result.doubleValue(j);
            TestUtils.assertTrue(tag + " x" + j + " >= lb", x[j] >= lowerBounds[j] - 1E-8);
            TestUtils.assertTrue(tag + " x" + j + " <= ub", x[j] <= upperBounds[j] + 1E-8);
        }

        double eqLHS = 2.0 * x[0] + 3.0 * x[1] + x[2] + 2.0 * x[3];
        TestUtils.assertEquals(tag + " equality", 17.0, eqLHS, ACCURACY);

        double iq0LHS = 3.0 * x[0] + x[1] + 2.0 * x[3];
        TestUtils.assertTrue(tag + " ineq0", iq0LHS <= 9.0 + 1E-8);

        double iq1LHS = x[0] + 2.0 * x[2] + 3.0 * x[3];
        TestUtils.assertTrue(tag + " ineq1", iq1LHS <= 8.0 + 1E-8);
    }

    /**
     * Same problem as {@link #testLowLevelSolver()} built via {@link ExpressionsBasedModel}. Solves with
     * {@link ExpressionsBasedModel#minimise()}, reads reduced gradients from the {@link Result}, modifies
     * variable bounds on the model, and re-solves.
     */
    @Test
    public void testHighLevelModel() {

        OptimisationCase testCase = TestBasicMIP.caseBranchAndBoundSubSolverTest();

        testCase.assertResult();

        ExpressionsBasedModel model = testCase.model.snapshot();
        model.options.linear().dual();

        Variable[] vars = model.getVariables().toArray(Variable[]::new);
        int nbVars = vars.length;

        // --- Solve the root LP relaxation ---

        Result result = model.minimise();
        TestUtils.assertStateNotLessThanOptimal(result);

        Access1D<?> reducedGradients = result.getReducedGradient().orElseThrow().get();

        if (DEBUG) {
            BasicLogger.debug("Root (model): x0={}, x1={}, x2={}, x3={}, obj={}", result.doubleValue(0), result.doubleValue(1), result.doubleValue(2),
                    result.doubleValue(3), result.getValue());
            for (int j = 0; j < nbVars; j++) {
                BasicLogger.debug("  {}: value={}, rc={}", vars[j].getName(), result.doubleValue(j), reducedGradients.doubleValue(j));
            }
        }

        BranchAndBoundSubSolverTest.verifyFeasibility("root", result, new double[] { 0, 1, 0, 0 }, new double[] { 4, 5, 3, 3 });

        double parentObj = result.getValue();

        // --- Branch-and-bound dive ---

        int maxDepth = nbVars + 2;

        for (int depth = 1; depth <= maxDepth; depth++) {

            if (BranchAndBoundSubSolverTest.isIntegerFeasible(result, nbVars, 1E-6)) {
                if (DEBUG) {
                    BasicLogger.debug("Integer feasible at depth {}", depth - 1);
                }
                break;
            }

            // Select branch variable and direction using reduced gradients from the Result
            reducedGradients = result.getReducedGradient().orElseThrow().get();

            int bestVar = -1;
            double bestScore = 0.0;
            boolean bestFloor = true;

            for (int j = 0; j < nbVars; j++) {
                double xj = result.doubleValue(j);
                double frac = BranchAndBoundSubSolverTest.fractionality(xj);
                if (frac <= 1E-6) {
                    continue;
                }

                double rc = reducedGradients.doubleValue(j);
                double score = Math.abs(rc) * frac + frac * 1E-6;

                if (score > bestScore) {
                    bestScore = score;
                    bestVar = j;

                    if (rc > 1E-12) {
                        bestFloor = true;
                    } else if (rc < -1E-12) {
                        bestFloor = false;
                    } else {
                        bestFloor = xj - Math.floor(xj) <= Math.ceil(xj) - xj;
                    }
                }
            }

            TestUtils.assertTrue("found fractional variable at depth " + depth, bestVar >= 0);

            double branchValue = result.doubleValue(bestVar);

            // Modify the variable bounds directly on the model
            if (bestFloor) {
                vars[bestVar].upper(Math.floor(branchValue));
            } else {
                vars[bestVar].lower(Math.ceil(branchValue));
            }

            if (DEBUG) {
                BasicLogger.debug("Depth {}: branch {}={}, direction={}, newBound={}", depth, vars[bestVar].getName(), branchValue,
                        bestFloor ? "floor" : "ceil", bestFloor ? Math.floor(branchValue) : Math.ceil(branchValue));
            }

            // Re-solve the modified model
            result = model.minimise();

            if (!result.getState().isFeasible()) {
                if (DEBUG) {
                    BasicLogger.debug("Infeasible at depth {}", depth);
                }
                break;
            }

            if (DEBUG) {
                Access1D<?> rc = result.getReducedGradient().orElseThrow().get();
                BasicLogger.debug("Depth {}: x0={}, x1={}, x2={}, x3={}, obj={}", depth, result.doubleValue(0), result.doubleValue(1), result.doubleValue(2),
                        result.doubleValue(3), result.getValue());
                for (int j = 0; j < nbVars; j++) {
                    BasicLogger.debug("  {}: value={}, rc={}", vars[j].getName(), result.doubleValue(j), rc.doubleValue(j));
                }
            }

            // Monotonicity: child LP bound cannot be better than parent's
            TestUtils.assertTrue("depth " + depth + " obj monotone", result.getValue() >= parentObj - 1E-8);
            parentObj = result.getValue();
        }
    }

    /**
     * B&B dive using {@link LinearSolver.Builder} with the dual simplex. Uses
     * {@link LinearSolver#getReducedGradient} and {@link LinearSolver#getDualMultiplier} after each node, and
     * {@link LinearSolver#updateRange} to tighten bounds in-place.
     *
     * <pre>
     * min  -2*x0 - 3*x1 + x2 - 5*x3
     * s.t. 2*x0 + 3*x1 +  x2 + 2*x3 = 17    (equality)
     *      3*x0 +  x1        + 2*x3 <= 9      (inequality 0)
     *       x0       + 2*x2 + 3*x3 <= 8       (inequality 1)
     *      0 <= x0 <= 4,  1 <= x1 <= 5,  0 <= x2 <= 3,  0 <= x3 <= 3
     *      all integer (relaxed to continuous)
     * </pre>
     */
    @Test
    public void testLowLevelSolver() {

        int nbVars = 4;
        int nbEqus = 1;
        int nbInes = 2;

        // Construct the matrices and arrays

        double[] lower = { 0.0, 1.0, 0.0, 0.0 }; // nbVars
        double[] upper = { 4.0, 5.0, 3.0, 3.0 }; // nbVars
        double[] objective = { -2.0, -3.0, 1.0, -5.0 }; // nbVars

        /*
         * The constraint body matrices need to implement Access2D for the API. Also implementing
         * SparseStructure2D enables internal optimisations. R064CSR with its Builder is just one alternative.
         */

        R064CSR.Builder builderE = R064CSR.newBuilder(nbEqus, nbVars);
        builderE.set(0, 0, 2.0);
        builderE.set(0, 1, 3.0);
        builderE.set(0, 2, 1.0);
        builderE.set(0, 3, 2.0);
        R064CSR bodyE = builderE.build();
        ArrayR064 rhsE = ArrayR064.make(nbEqus);
        rhsE.set(0, 17.0);

        R064CSR.Builder builderI = R064CSR.newBuilder(nbInes, nbVars);
        builderI.set(0, 0, 3.0);
        builderI.set(0, 1, 1.0);
        builderI.set(0, 3, 2.0);
        builderI.set(1, 0, 1.0);
        builderI.set(1, 2, 2.0);
        builderI.set(1, 3, 3.0);
        R064CSR bodyI = builderI.build();
        ArrayR064 rhsI = ArrayR064.make(nbInes);
        rhsI.set(0, 9.0);
        rhsI.set(1, 8.0);

        // Build the LP solver, via a builder

        LinearSolver.Builder builder = LinearSolver.newBuilder(objective);
        builder.lower(lower);
        builder.upper(upper);
        builder.equalities(bodyE, rhsE);
        builder.inequalities(bodyI, rhsI);

        Optimisation.Options options = new Optimisation.Options();
        options.linear().dual();

        LinearSolver solver = builder.build(options);

        // Working copies of bounds — tightened as we branch
        double[] currentLB = lower.clone();
        double[] currentUB = upper.clone();

        // Solve the root LP relaxation

        Result result = solver.solve();
        TestUtils.assertStateNotLessThanOptimal(result);

        if (DEBUG) {
            BasicLogger.debug("Root: x0={}, x1={}, x2={}, x3={}, obj={}", result.doubleValue(0), result.doubleValue(1), result.doubleValue(2),
                    result.doubleValue(3), result.getValue());
            for (int j = 0; j < nbVars; j++) {
                BasicLogger.debug("  x{}: value={}, rc={}", j, result.doubleValue(j), solver.getReducedGradient(j));
            }
            for (int i = 0; i < nbInes; i++) {
                BasicLogger.debug("  ineq {}: dual={}", i, solver.getDualMultiplier(i));
            }
        }

        BranchAndBoundSubSolverTest.verifyFeasibility("root", result, currentLB, currentUB);
        BranchAndBoundSubSolverTest.verifyDualProperties("root", solver, result, currentLB, currentUB);

        double parentObj = result.getValue();

        // Branch-and-bound dive using dual info to guide decisions

        int maxDepth = nbVars + 2;

        for (int depth = 1; depth <= maxDepth; depth++) {

            if (BranchAndBoundSubSolverTest.isIntegerFeasible(result, nbVars, 1E-6)) {
                if (DEBUG) {
                    BasicLogger.debug("Integer feasible at depth {}", depth - 1);
                }
                break;
            }

            // Use reduced gradients to select the branching variable and direction
            BranchDecision branch = BranchAndBoundSubSolverTest.selectBranch(solver, result, nbVars);
            TestUtils.assertTrue("found fractional variable at depth " + depth, branch.variable >= 0);

            int branchVar = branch.variable;
            double branchValue = result.doubleValue(branchVar);
            boolean goFloor = branch.floor;

            double newBound;
            if (goFloor) {
                newBound = Math.floor(branchValue);
                currentUB[branchVar] = newBound;
            } else {
                newBound = Math.ceil(branchValue);
                currentLB[branchVar] = newBound;
            }

            if (DEBUG) {
                BasicLogger.debug("Depth {}: branch x{}={}, direction={}, newBound={}", depth, branchVar, branchValue, goFloor ? "floor" : "ceil", newBound);
            }

            boolean updated = solver.updateRange(branchVar, currentLB[branchVar], currentUB[branchVar]);
            TestUtils.assertTrue("updateRange succeeded at depth " + depth, updated);

            result = solver.solve();

            if (!result.getState().isFeasible()) {
                if (DEBUG) {
                    BasicLogger.debug("Infeasible at depth {}", depth);
                }
                break;
            }

            if (DEBUG) {
                BasicLogger.debug("Depth {}: x0={}, x1={}, x2={}, x3={}, obj={}", depth, result.doubleValue(0), result.doubleValue(1), result.doubleValue(2),
                        result.doubleValue(3), result.getValue());
                for (int j = 0; j < nbVars; j++) {
                    BasicLogger.debug("  x{}: value={}, rc={}", j, result.doubleValue(j), solver.getReducedGradient(j));
                }
                for (int i = 0; i < nbInes; i++) {
                    BasicLogger.debug("  ineq {}: dual={}", i, solver.getDualMultiplier(i));
                }
            }

            // Verify properties at this node

            String nodeTag = "depth " + depth;

            BranchAndBoundSubSolverTest.verifyFeasibility(nodeTag, result, currentLB, currentUB);
            BranchAndBoundSubSolverTest.verifyDualProperties(nodeTag, solver, result, currentLB, currentUB);

            // Monotonicity: child LP bound cannot be better than parent's
            TestUtils.assertTrue(nodeTag + " obj monotone", result.getValue() >= parentObj - 1E-8);
            parentObj = result.getValue();
        }
    }

}
