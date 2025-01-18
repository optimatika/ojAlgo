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
package org.ojalgo.optimisation.integer;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.OptimisationCase;
import org.ojalgo.type.context.NumberContext;
import org.opentest4j.AssertionFailedError;

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

            model.setKnownSolution(minSolution, (m, s) -> {
                if (!DEBUG) {
                    BasicLogger.error(s);
                    BasicLogger.error(m);
                    throw new AssertionFailedError();
                }
            });

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

            model.setKnownSolution(maxSolution, (m, s) -> {
                if (!DEBUG) {
                    BasicLogger.error(s);
                    BasicLogger.error(m);
                    throw new AssertionFailedError();
                }
            });

            Result result = model.maximise();

            if (DEBUG) {
                BasicLogger.debug(maxSolution);
                BasicLogger.debug(result);
            }

            TestUtils.assertSolutionValid(model, result, ACCURACY);
            TestUtils.assertResult(maxSolution, result, ACCURACY);
        }
    }

    @Test
    public void testFacilityLocationCase() {

        OptimisationCase testCase = DesignCase.makeFacilityLocationCase();

        GeneratedCutTest.doTest(testCase.model, null, testCase.result);
    }

    @Test
    public void testGr4x6() {

        String minStr = "OPTIMAL 202.35 @ { 20, 0, 25, 0, 0, 0, 0, 30, 0, 0, 0, 5, 15, 0, 0, 0, 5, 0, 0, 0, 0, 15, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0 }";
        Optimisation.Result minSolution = Optimisation.Result.parse(minStr);
        Optimisation.Result maxSolution = null;

        ExpressionsBasedModel model = ModelFileTest.makeModel("miplib", "gr4x6.mps", false);

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

}
