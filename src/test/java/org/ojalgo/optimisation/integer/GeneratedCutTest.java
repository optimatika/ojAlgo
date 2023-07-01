/*
 * Copyright 1997-2023 Optimatika
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
        }

        if (minSolution != null) {

            TestUtils.assertSolutionValid(model, minSolution, ACCURACY);

            // BasicLogger.debug(model.minimise());

            model.setKnownSolution(minSolution, (m, s) -> {
                throw new AssertionFailedError();
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

            // BasicLogger.debug(model.maximise());

            model.setKnownSolution(maxSolution);

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
    @Tag("new_lp_problem")
    public void testInitialCutsOfGr4x6() {

        Optimisation.Result maxSolution = null;

        String minStr = "OPTIMAL 202.35 @ { 20, 0, 25, 0, 0, 0, 0, 30, 0, 0, 0, 5, 15, 0, 0, 0, 5, 0, 0, 0, 0, 15, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0 }";
        Optimisation.Result minSolution = Optimisation.Result.parse(minStr);

        ExpressionsBasedModel model = ModelFileTest.makeModel("miplib", "gr4x6.mps", false);

        GeneratedCutTest.doTest(model, minSolution, maxSolution);
    }

}
