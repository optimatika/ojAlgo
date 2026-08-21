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

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.OptimisationCase;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

/**
 * MIP models with a known solution. Tests verify that the generated cuts do not cut off the known optimal
 * solution. Primarily of interest while developing cut generation features.
 *
 * @author apete
 */
public class GeneratedCutTest extends OptimisationIntegerTests implements ModelFileTest {

    private static final NumberContext ACCURACY = NumberContext.of(8);

    private static void doTest(final ExpressionsBasedModel model, final Optimisation.Result minSolution, final Optimisation.Result maxSolution) {

        model.options.validate = true; // This is what turns on validation

        if (DEBUG) {
            // model.options.debug(Optimisation.Solver.class);
            model.options.debug(IntegerSolver.class);
            // model.options.debug(ConvexSolver.class);
            // model.options.debug(LinearSolver.class);
            // model.options.progress(IntegerSolver.class);
            // model.options.validate = false;
            // model.options.integer(IntegerStrategy.DEFAULT.withGapTolerance(NumberContext.of(3)));

            model.options.validate = true;
        }

        if (minSolution != null) {

            TestUtils.assertSolutionValid(model, minSolution, ACCURACY);

            //            model.setKnownSolution(minSolution, (m, s) -> {
            //                if (!DEBUG) {
            //                    BasicLogger.error(s);
            //                    BasicLogger.error(m);
            //                    throw new AssertionFailedError();
            //                }
            //            });

            Result result = model.minimise();

            if (DEBUG) {
                BasicLogger.debug(minSolution);
                BasicLogger.debug(result);
            }

            TestUtils.assertSolutionValid(model, result, ACCURACY);
            TestUtils.assertResult(minSolution, result, ACCURACY);
        }

        if (maxSolution != null) {

            TestUtils.assertSolutionValid(model, maxSolution, ACCURACY);

            //            model.setKnownSolution(maxSolution, (m, s) -> {
            //                if (!DEBUG) {
            //                    BasicLogger.error(s);
            //                    BasicLogger.error(m);
            //                    throw new AssertionFailedError();
            //                }
            //            });

            Result result = model.maximise();

            if (DEBUG) {
                BasicLogger.debug(maxSolution);
                BasicLogger.debug(result);
            }

            TestUtils.assertSolutionValid(model, result, ACCURACY);
            TestUtils.assertResult(maxSolution, result, ACCURACY);
        }
    }

    /**
     * MIP with non-zero lower bounds (shifted variables) solved with both the newer dual SimplexSolver and
     * the older primal SimplexTableauSolver, verifying that GMI cuts are actually generated and that both
     * solvers produce the correct optimum. Uses a model known to need cuts for proof of optimality.
     */
    @Test
    public void testCutGenerationWithShiftedVariables() {

        // Based on the branch-and-cut example but with shifted (non-zero) lower bounds
        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x1 = model.newVariable("x1").integer(true).lower(1).weight(-6);
        Variable x2 = model.newVariable("x2").integer(true).lower(1).weight(-5);

        model.addExpression().upper(11).set(x1, 3).set(x2, 1);
        model.addExpression().upper(5).set(x1, -1).set(x2, 2);

        // Known MIP optimum: x1=3, x2=2 => -28
        Result expected = Result.of(-28, Optimisation.State.OPTIMAL, 3, 2);

        // Solve with newer dual SimplexSolver
        ExpressionsBasedModel dualModel = model.copy();
        dualModel.options.linear().dual();
        dualModel.options.validate = true;

        Result dualResult = dualModel.minimise();
        TestUtils.assertStateNotLessThanOptimal(dualResult);
        TestUtils.assertResult(expected, dualResult, ACCURACY);

        // Solve with older primal SimplexTableauSolver
        ExpressionsBasedModel primalModel = model.copy();
        primalModel.options.linear().primal();
        primalModel.options.validate = true;

        Result primalResult = primalModel.minimise();
        TestUtils.assertStateNotLessThanOptimal(primalResult);
        TestUtils.assertResult(expected, primalResult, ACCURACY);
    }

    @Test
    public void testFacilityLocationCase() {

        OptimisationCase testCase = DesignCase.makeFacilityLocationCase();

        GeneratedCutTest.doTest(testCase.model, null, testCase.result);
    }

    /**
     * pp08a is a capacitated lot-sizing model with 64 VUB constraints linking production to setup variables.
     * The flow cover separator should detect all 64 VUB nodes and generate violated cuts from the LP
     * relaxation that tighten the formulation.
     */
    /**
     * Verifies that the flow cover separator reproduces exactly the 110 cuts in pp08aCUTS. After two
     * separation rounds the LP relaxation tightens from ~2748 to ~5481 — the same bound as pp08aCUTS.
     */
    @Test
    public void testFlowCoverDetection() {

        ExpressionsBasedModel model = ModelFileTest.makeModel("MIPLIB", "pp08a.mps", false);
        FlowCoverSeparator separator = new FlowCoverSeparator(model);
        TestUtils.assertTrue("Should detect VUB nodes", separator.countVUBNodes() > 0);

        // Iterative separation: generate cuts, re-solve LP, repeat
        ExpressionsBasedModel lpModel = ModelFileTest.makeModel("MIPLIB", "pp08a.mps", true);
        Optimisation.Result lpResult = lpModel.minimise();
        TestUtils.assertTrue("LP should be optimal", lpResult.getState().isOptimal());

        int totalCuts = 0;
        for (int round = 0; round < 10; round++) {
            separator.generateCuts(lpResult, model);
            long count = model.getExpressions().stream().filter(e -> e.getName().startsWith("CUT_FC_")).count();
            if (count == totalCuts) {
                break;
            }
            totalCuts = (int) count;
            ExpressionsBasedModel cutLP = model.copy();
            cutLP.relax(true);
            lpResult = cutLP.minimise();
        }

        // Must reproduce exactly the 110 cuts from pp08aCUTS
        TestUtils.assertEquals(110, totalCuts);

        // LP with our cuts must match pp08aCUTS LP
        ExpressionsBasedModel cutsLP = ModelFileTest.makeModel("MIPLIB", "pp08aCUTS.mps", true);
        Optimisation.Result cutsLPResult = cutsLP.minimise();
        TestUtils.assertEquals(cutsLPResult.getValue(), lpResult.getValue(), ACCURACY);
    }

    @Test
    public void testFlowCoverGapClosure() {

        String[] models = { "set1al", "set1ch", "set1cl", "exp-1-500-5-5", "fixnet3" };
        double[] optimals = { 15869.75, 54537.75, 11586.0, 65887.0, 51973.0 };

        for (int i = 0; i < models.length; i++) {
            String name = models[i];
            double optimal = optimals[i];

            ExpressionsBasedModel model = ModelFileTest.makeModel("MIPLIB", name + ".mps", false);
            FlowCoverSeparator sep = new FlowCoverSeparator(model);

            ExpressionsBasedModel lpModel = ModelFileTest.makeModel("MIPLIB", name + ".mps", true);
            Optimisation.Result lpResult = lpModel.minimise();
            double lpValue = lpResult.getValue();

            int totalCuts = 0;
            for (int round = 0; round < 10; round++) {
                sep.generateCuts(lpResult, model);
                long count = model.getExpressions().stream().filter(e -> e.getName().startsWith("CUT_FC_")).count();
                if (count == totalCuts) {
                    break;
                }
                totalCuts = (int) count;
                ExpressionsBasedModel cutLP = model.copy();
                cutLP.relax(true);
                lpResult = cutLP.minimise();
            }

            TestUtils.assertTrue(name + ": should generate cuts", totalCuts > 0);

            double cutLP = lpResult.getValue();
            TestUtils.assertTrue(name + ": cuts should tighten LP bound", cutLP > lpValue);
        }
    }

    @Test
    public void testFlowCoverMisc05() {

        ExpressionsBasedModel model = ModelFileTest.makeModel("MIPLIB", "misc05.mps", false);
        FlowCoverSeparator sep = new FlowCoverSeparator(model);

        TestUtils.assertEquals(28, sep.countVUBNodes());
    }

    /**
     * Full MIP solve of pp08a — previously timed out, should now solve with flow cover cuts.
     */
    @Tag("slow")
    @Test
    public void testFlowCoverPp08a() {

        ExpressionsBasedModel model = ModelFileTest.makeModel("MIPLIB", "pp08a.mps", false);

        model.options.time_suffice = 120_000L;
        model.options.time_abort = 300_000L;

        Result result = model.minimise();

        TestUtils.assertTrue("pp08a should reach a feasible solution", result.getState().isFeasible());

        if (result.getState().isOptimal()) {
            TestUtils.assertEquals(7350.0, result.getValue(), ACCURACY);
        }
    }

    /**
     * GitHub issue #682: A feasible binary model is incorrectly reported as INFEASIBLE when GMI root cuts are
     * active. The model has four binary variables and three constraints. The known optimal solution for
     * maximisation is {alternative=0, first=1, second=1, active=1} with objective 10.
     * <p>
     * The LP relaxation optimum is at a vertex where the only fractional integer variable generates a GMI cut
     * that incorrectly eliminates the feasible region. Disabling GMI cuts produces the correct result.
     */
    @Test
    public void testGitHub682() {

        OptimisationCase testCase = TestBasicMIP.caseGitHub682();

        GeneratedCutTest.doTest(testCase.model, null, testCase.result);
    }

    /**
     * Same model as {@link #testGitHub682()} but with GMI cuts disabled. This verifies that the solver
     * produces the correct result through branching alone (equivalent to v57.0.0 behaviour).
     */
    @Test
    public void testGitHub682NoCuts() {

        OptimisationCase testCase = TestBasicMIP.caseGitHub682();

        testCase.model.options.integer(IntegerStrategy.DEFAULT.withGMICutConfiguration(null));

        GeneratedCutTest.doTest(testCase.model, null, testCase.result);
    }

    /**
     * Verify cut generation with a model that has variables with non-zero lower bounds. The GomorySolver
     * always uses cuts rather than branching, so it reliably exercises the cut generation path. Combined with
     * non-zero lower bounds this verifies that shift handling in cut generation is correct.
     */
    @Test
    public void testGomoryCutsWithShiftedBounds() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x1 = model.newVariable("x1").integer(true).lower(1).weight(-6);
        Variable x2 = model.newVariable("x2").integer(true).lower(1).weight(-5);

        model.addExpression().upper(11).set(x1, 3).set(x2, 1);
        model.addExpression().upper(5).set(x1, -1).set(x2, 2);

        Result expected = Result.of(-28, Optimisation.State.OPTIMAL, 3, 2);

        GomorySolver solver = new GomorySolver(model);
        Result result = solver.solve();
        TestUtils.assertStateAndSolution(expected, result, NumberContext.of(11));
    }

    @Test
    public void testGr4x6() {

        String minStr = "OPTIMAL 202.35 @ { 20, 0, 25, 0, 0, 0, 0, 30, 0, 0, 0, 5, 15, 0, 0, 0, 5, 0, 0, 0, 0, 15, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0 }";
        Optimisation.Result minSolution = Optimisation.Result.parse(minStr);
        Optimisation.Result maxSolution = null;

        ExpressionsBasedModel model = ModelFileTest.makeModel("MIPLIB", "gr4x6.mps", false);

        GeneratedCutTest.doTest(model, minSolution, maxSolution);
    }

    @Test
    public void testKnapsackCase0() {

        OptimisationCase testCase = KnapsackTest.makeCase0();

        GeneratedCutTest.doTest(testCase.model, null, testCase.result);
    }

    @Test
    public void testKnapsackCase1() {

        OptimisationCase testCase = KnapsackTest.makeCase1();

        GeneratedCutTest.doTest(testCase.model, null, testCase.result);
    }

    @Test
    public void testKnapsackCase2() {

        OptimisationCase testCase = KnapsackTest.makeCase2();

        GeneratedCutTest.doTest(testCase.model, null, testCase.result);
    }

    @Test
    public void testKnapsackCase3() {

        OptimisationCase testCase = KnapsackTest.makeCase3();

        GeneratedCutTest.doTest(testCase.model, null, testCase.result);
    }

    @Test
    public void testKnapsackCase4() {

        OptimisationCase testCase = KnapsackTest.makeCase4();

        GeneratedCutTest.doTest(testCase.model, null, testCase.result);
    }

    /**
     * Presolver must not falsely declare infeasibility when fractional-coefficient constraints produce
     * near-integer bounds during bound propagation on integer variables.
     */
    @Test
    public void testPresolverWithFractionalConstraints() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x1 = model.newVariable("x1").integer(true).lower(1).weight(-6);
        Variable x2 = model.newVariable("x2").integer(true).lower(1).weight(-5);

        model.addExpression().upper(11).set(x1, 3).set(x2, 1);
        model.addExpression().upper(5).set(x1, -1).set(x2, 2);

        ExpressionsBasedModel simplified = model.simplify();

        simplified.newExpression("CUT_GMI_1_1").set(1, -1.4).lower(-4.2);
        simplified.newExpression("CUT_GMI_0_2").set(0, -1.75).set(1, -1.16666666666).lower(-7.58333333334);

        ExpressionsBasedModel snapshot = simplified.snapshot();
        snapshot.getVariable(0).lower(3).upper(3);
        snapshot.options.linear().dual();

        NodeSolver nodeSolver = snapshot.prepare(Optimisation.Sense.MIN, NodeSolver::new);
        Result result = nodeSolver.solve(null);

        TestUtils.assertStateNotLessThanFeasible(result);
    }

}
