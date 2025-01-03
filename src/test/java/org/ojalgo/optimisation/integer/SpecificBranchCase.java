/*
 * Copyright 1997-2024 Optimatika
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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.type.context.NumberContext;

public class SpecificBranchCase extends OptimisationIntegerTests implements ModelFileTest {

    private static final NumberContext ACCURACY = NumberContext.ofPrecision(9).withScale(7);

    private static final int[] NOSWOT_INTEGERS = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
            30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66,
            67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99 };

    private static void doTestNode(final String modelPath, final int[] index, final int[] lower, final int[] upper, final Optimisation.State expectedStateLP,
            final State expectedStateMIP) {

        ExpressionsBasedModel modelMIP = SpecificBranchCase.makeModel(modelPath);

        TestUtils.assertTrue(modelMIP.validate());

        // Verify that the proposed lower and upper bounds for the node do not
        // break the original overall model bounds.
        for (int i = 0; i < index.length; i++) {
            Variable variable = modelMIP.getVariable(index[i]);
            BigDecimal lowerBound = new BigDecimal(lower[i]);
            BigDecimal upperBound = new BigDecimal(upper[i]);
            if (variable.isLowerLimitSet()) {
                BigDecimal lowerLimit = variable.getLowerLimit();
                TestUtils.assertFalse(lowerBound.compareTo(lowerLimit) < 0);
                TestUtils.assertFalse(upperBound.compareTo(lowerLimit) < 0);
            }
            if (variable.isUpperLimitSet()) {
                BigDecimal upperLimit = variable.getUpperLimit();
                TestUtils.assertFalse(lowerBound.compareTo(upperLimit) > 0);
                TestUtils.assertFalse(upperBound.compareTo(upperLimit) > 0);
            }
        }

        for (int i = 0; i < index.length; i++) { // Set up the node
            modelMIP.getVariable(index[i]).lower(lower[i]).upper(upper[i]);
        }

        ExpressionsBasedModel modelLP = modelMIP.copy(false);
        modelLP.relax(true); // soft-relax

        if (expectedStateLP != null) {

            if (DEBUG) {
                modelLP.options.debug(LinearSolver.class);
            }

            Optimisation.Result resultLP = modelLP.minimise();

            if (DEBUG) {
                BasicLogger.debug(resultLP);
            }

            if (expectedStateLP.isFeasible()) {
                TestUtils.assertStateNotLessThanFeasible(resultLP);
            } else {
                TestUtils.assertStateLessThanFeasible(resultLP);
            }

            if (expectedStateLP.isOptimal()) {
                TestUtils.assertStateNotLessThanOptimal(resultLP);
            }

            if (resultLP.getState().isFeasible()) {
                TestUtils.assertTrue(modelLP.validate(resultLP, ACCURACY, BasicLogger.DEBUG));
            }
        }

        if (expectedStateMIP != null) {

            if (DEBUG) {
                modelLP.options.debug(IntegerSolver.class);
            }

            Optimisation.Result resultMIP = modelMIP.minimise();

            if (DEBUG) {
                BasicLogger.debug(resultMIP);
            }

            if (expectedStateMIP.isFeasible()) {
                TestUtils.assertStateNotLessThanFeasible(resultMIP);
            } else {
                TestUtils.assertStateLessThanFeasible(resultMIP);
            }

            if (expectedStateMIP.isOptimal()) {
                TestUtils.assertStateNotLessThanOptimal(resultMIP);
            }

            if (resultMIP.getState().isFeasible()) {
                TestUtils.assertTrue(modelMIP.validate(resultMIP, ACCURACY, BasicLogger.DEBUG));
            }
        }

    }

    private static ExpressionsBasedModel makeModel(final String modelPath) {
        return ModelFileTest.makeModel("miplib", modelPath, false);
    }

    /**
     * <pre>
    Branch&Bound Node
    5 (4) 7=0.0833333333333286 1167875.1663773148 [0=3<18, 1=57<72, 2=0<18, 3=57<75, 4=0<18, 5=57<75, 6=0<18, 7=67<67, 8=0<18, 9=71<75, 10=0<18]
    Solutions=0 Nodes/Iterations=3 INVALID Infinity @ { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }
    Node Result: OPTIMAL 8265.302951388889 @ { 60.0, 9.46932870370371, 0.0, 63.46932870370371, 5.203993055555552, 0.0, 62.326388888888886, 13.489583333333336, 0.0, 69.58333333333331, 4.3749999999999964, 0.0, 67.0, 11.0, 50.0, 71.0, 0.0, 1350.0 }
    Node solved to optimality!
    0.30 ! 0 <= ANZ6 <= 0
    Node solution marked as OPTIMAL, but is actually INVALID/INFEASIBLE/FAILED. Stop this branch!
    Integer indices: [1, 3, 4, 6, 7, 9, 10, 12, 13, 15, 16]
    Lower bounds: [3, 57, 0, 57, 0, 57, 0, 67, 0, 71, 0]
    Upper bounds: [18, 72, 18, 75, 18, 75, 18, 67, 18, 75, 18]
    Done 4 IntegerSolver iterations in 0.043330625s with NodeStatistics [I=0, E=0, S=0, A=0]
     * </pre>
     *
     * When solving this node directly with the integer solver it is correctly identified as infeasible, but
     * when this node is reached during the branch and bound process the LP node solver marks it as optimal.
     * (It is only infeasible when taking into account the integer constraints.) This is due to some complex
     * relationship between model pre-solve (at the nodes) and in-place updates of the solver instances.
     */
    @Test
    public void testFlugplN5() {

        int[] integers = { 1, 3, 4, 6, 7, 9, 10, 12, 13, 15, 16 };
        int[] lower = { 3, 57, 0, 57, 0, 57, 0, 67, 0, 71, 0 };
        int[] upper = { 18, 72, 18, 75, 18, 75, 18, 67, 18, 75, 18 };

        SpecificBranchCase.doTestNode("flugpl.mps", integers, lower, upper, State.INFEASIBLE, null);
    }

    /**
     * <pre>
    Branch&Bound Node
    7 (5) 2=0.20399305555555447 1168080.295138889 [0=3<18, 1=57<72, 2=0<5, 3=57<75, 4=0<18, 5=57<75, 6=0<18, 7=67<67, 8=0<18, 9=71<75, 10=0<18]
    Solutions=0 Nodes/Iterations=4 { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }
    Node Result: { 60.0, 9.695987654320987, 0.0, 63.69598765432099, 5.0, 0.0, 62.326388888888886, 13.489583333333336, 0.0, 69.58333333333331, 4.3749999999999964, 0.0, 67.0, 11.0, 50.0, 71.0, 0.0, 1350.0 }
    Node solved to optimality!
    0.30 ! 0 <= ANZ6 <= 0
    Node solution marked as OPTIMAL, but is actually INVALID/INFEASIBLE/FAILED. Stop this branch!
    Integer indices: [1, 3, 4, 6, 7, 9, 10, 12, 13, 15, 16]
    Lower bounds: [3, 57, 0, 57, 0, 57, 0, 67, 0, 71, 0]
    Upper bounds: [18, 72, 5, 75, 18, 75, 18, 67, 18, 75, 18]
    Done 5 IntegerSolver iterations in 0.140962034s with NodeStatistics [I=0, E=0, S=0, A=0]
     * </pre>
     */
    @Test
    public void testFlugplN7() {

        int[] integers = { 1, 3, 4, 6, 7, 9, 10, 12, 13, 15, 16 };
        int[] lower = { 3, 57, 0, 57, 0, 57, 0, 67, 0, 71, 0 };
        int[] upper = { 18, 72, 5, 75, 18, 75, 18, 67, 18, 75, 18 };

        SpecificBranchCase.doTestNode("flugpl.mps", integers, lower, upper, State.INFEASIBLE, null);
    }

    /**
     * Both the (soft) relaxed and the integer model should be feasible and optimal. The overall optimal value
     * is not obtainable from this node. (Verifying that is the reason this test exists.)
     */
    @Test
    public void testMarkshare_4_0N1() {

        int[] integers = { 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33 };
        int[] lower = { 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 1 };
        int[] upper = { 0, 1, 0, 1, 1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1 };

        SpecificBranchCase.doTestNode("markshare_4_0.mps", integers, lower, upper, State.OPTIMAL, State.OPTIMAL);
    }

    /**
     * ojAlgo (at some point) had problems with this node – returning an infeasible node solution marked as
     * OPTIMAL. CPLEX finds an integer solution.
     */
    @Test
    public void testNoswotN1() {

        int[] lower = { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0 };
        int[] upper = { 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1,
                100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1,
                100000 };

        SpecificBranchCase.doTestNode("noswot.mps", NOSWOT_INTEGERS, lower, upper, State.OPTIMAL, null);
    }

    /**
     * ojAlgo (at some point) had problems with this node – returning an infeasible node solution marked as
     * OPTIMAL. CPLEX finds an integer solution.
     */
    @Test
    public void testNoswotN2() {

        int[] lower = { 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0 };
        int[] upper = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1,
                100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1,
                100000 };

        SpecificBranchCase.doTestNode("noswot.mps", NOSWOT_INTEGERS, lower, upper, State.OPTIMAL, null);
    }

    /**
     * ojAlgo (at some point) had problems with this node – returning an infeasible node solution marked as
     * OPTIMAL. CPLEX finds an integer solution.
     */
    @Test
    public void testNoswotN3() {

        int[] lower = { 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0 };
        int[] upper = { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1,
                100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1,
                100000 };

        SpecificBranchCase.doTestNode("noswot.mps", NOSWOT_INTEGERS, lower, upper, State.OPTIMAL, null);
    }

    /**
     * ojAlgo (at some point) had problems with this node – returning an infeasible node solution marked as
     * OPTIMAL. CPLEX finds an integer solution.
     */
    @Test
    public void testNoswotN4() {

        int[] lower = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0 };
        int[] upper = { 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1,
                100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1,
                100000 };

        SpecificBranchCase.doTestNode("noswot.mps", NOSWOT_INTEGERS, lower, upper, State.OPTIMAL, null);
    }

    @Test
    public void testVpm2FirstBranch() {

        ExpressionsBasedModel tmpModel = SpecificBranchCase.makeModel("vpm2.mps");

        TestUtils.assertTrue(tmpModel.validate());

        ExpressionsBasedModel tmpLowerBranchModel = tmpModel.copy(true);
        ExpressionsBasedModel tmpUpperBranchModel = tmpModel.copy(true);

        tmpLowerBranchModel.getVariable(106).upper(BigMath.ZERO);
        tmpUpperBranchModel.getVariable(106).lower(BigMath.ONE);

        Optimisation.Result tmpLowerResult = tmpLowerBranchModel.minimise();
        Optimisation.Result tmpUpperResult = tmpUpperBranchModel.minimise();

        State tmpLowerState = tmpLowerResult.getState();
        State tmpUpperState = tmpUpperResult.getState();

        if (!tmpLowerState.isFeasible() && !tmpUpperState.isFeasible()) {
            TestUtils.fail("Both these branches cannot be infeasible!");
        }

        NumberContext accuracy = NumberContext.of(7, 6);

        if (tmpLowerState.isFeasible() && !tmpLowerBranchModel.validate(accuracy)) {
            TestUtils.fail("Solution not valid!");
        }

        if (tmpUpperState.isFeasible() && !tmpUpperBranchModel.validate(accuracy)) {
            TestUtils.fail("Solution not valid!");
        }
    }

}
