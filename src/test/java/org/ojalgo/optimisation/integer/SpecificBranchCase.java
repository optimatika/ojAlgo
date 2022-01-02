/*
 * Copyright 1997-2022 Optimatika
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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ExpressionsBasedModel.FileFormat;
import org.ojalgo.optimisation.ModelFileMPS;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.type.context.NumberContext;

public class SpecificBranchCase extends OptimisationIntegerTests implements ModelFileMPS {

    private static final NumberContext ACCURACY = NumberContext.ofPrecision(9).withScale(7);
    private static final String MIPLIB_PATH = ModelFileMPS.OPTIMISATION_RSRC + "miplib/";
    private static final int[] NOSWOT_INTEGERS = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63,
            64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99 };

    private static void doTestNode(final String modelPath, final int[] index, final int[] lower, final int[] upper, final Optimisation.State expectedState) {

        try (InputStream input = ExpressionsBasedModel.class.getResourceAsStream(SpecificBranchCase.MIPLIB_PATH + modelPath)) {

            ExpressionsBasedModel modelMIP = ExpressionsBasedModel.parse(input, FileFormat.MPS);

            TestUtils.assertTrue(modelMIP.validate());

            // Assert that the branch node is valid
            for (int i = 0; i < index.length; i++) {
                Variable variable = modelMIP.getVariable(index[i]);
                BigDecimal lowerBound = new BigDecimal(lower[i]);
                BigDecimal upperBound = new BigDecimal(upper[i]);
                if (variable.isLowerLimitSet()) {
                    BigDecimal lowerLimit = variable.getLowerLimit();
                    TestUtils.assertFalse(lowerBound.compareTo(lowerLimit) == -1);
                    TestUtils.assertFalse(upperBound.compareTo(lowerLimit) == -1);
                }
                if (variable.isUpperLimitSet()) {
                    BigDecimal upperLimit = variable.getUpperLimit();
                    TestUtils.assertFalse(lowerBound.compareTo(upperLimit) == 1);
                    TestUtils.assertFalse(upperBound.compareTo(upperLimit) == 1);
                }
            }

            ExpressionsBasedModel relaxedModel = modelMIP;
            relaxedModel.relax(true);

            for (int i = 0; i < index.length; i++) { // Set up the node
                relaxedModel.getVariable(index[i]).lower(lower[i]).upper(upper[i]);
            }

            if (DEBUG) {
                relaxedModel.options.debug(LinearSolver.class);
            }

            Optimisation.Result result = relaxedModel.minimise();

            if (DEBUG) {
                BasicLogger.debug(result);
            }

            if (expectedState != null) {
                if (expectedState.isFeasible()) {
                    TestUtils.assertStateNotLessThanFeasible(result);
                }
                if (expectedState.isOptimal()) {
                    TestUtils.assertStateNotLessThanOptimal(result);
                }
            }

            if (result.getState().isFeasible()) {
                TestUtils.assertTrue(relaxedModel.validate(result, ACCURACY, BasicLogger.DEBUG));
                // TestUtils.assertTrue(modelMIP.validate(result, ACCURACY, BasicLogger.DEBUG));
            }

        } catch (IOException cause) {
            TestUtils.fail(cause);
        }
    }

    /**
     * ojAlgo (at some point) had problems with this node – returning an infeasible node solution marked as
     * OPTIMAL. CPLEX finds an integer solution.
     */
    @Test
    public void testNoswotN1() {

        int[] lower = new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        int[] upper = new int[] { 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000,
                1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000,
                1, 100000 };

        SpecificBranchCase.doTestNode("noswot.mps", NOSWOT_INTEGERS, lower, upper, State.OPTIMAL);
    }

    /**
     * ojAlgo (at some point) had problems with this node – returning an infeasible node solution marked as
     * OPTIMAL. CPLEX finds an integer solution.
     */
    @Test
    public void testNoswotN2() {

        int[] lower = new int[] { 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        int[] upper = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000,
                1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000,
                1, 100000 };

        SpecificBranchCase.doTestNode("noswot.mps", NOSWOT_INTEGERS, lower, upper, State.OPTIMAL);
    }

    /**
     * ojAlgo (at some point) had problems with this node – returning an infeasible node solution marked as
     * OPTIMAL. CPLEX finds an integer solution.
     */
    @Test
    public void testNoswotN3() {

        int[] lower = new int[] { 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        int[] upper = new int[] { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000,
                1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000,
                1, 100000 };

        SpecificBranchCase.doTestNode("noswot.mps", NOSWOT_INTEGERS, lower, upper, State.OPTIMAL);
    }

    /**
     * ojAlgo (at some point) had problems with this node – returning an infeasible node solution marked as
     * OPTIMAL. CPLEX finds an integer solution.
     */
    @Test
    public void testNoswotN4() {

        int[] lower = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        int[] upper = new int[] { 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000,
                1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000, 1, 100000,
                1, 100000 };

        SpecificBranchCase.doTestNode("noswot.mps", NOSWOT_INTEGERS, lower, upper, State.OPTIMAL);
    }

    @Test
    public void testVpm2FirstBranch() {

        try (InputStream input = ExpressionsBasedModel.class.getResourceAsStream(SpecificBranchCase.MIPLIB_PATH + "vpm2.mps")) {

            ExpressionsBasedModel tmpModel = ExpressionsBasedModel.parse(input, FileFormat.MPS);

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
                TestUtils.fail(ModelFileMPS.SOLUTION_NOT_VALID);
            }

            if (tmpUpperState.isFeasible() && !tmpUpperBranchModel.validate(accuracy)) {
                TestUtils.fail(ModelFileMPS.SOLUTION_NOT_VALID);
            }

        } catch (IOException cause) {
            TestUtils.fail(cause);
        }
    }

}
