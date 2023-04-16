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
package org.ojalgo.optimisation.convex;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.task.iterative.ConjugateGradientSolver;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.type.context.NumberContext;

/**
 * User supplied QP models. May or may not have had specific problems. Tests mostly just verify that the
 * problem/model can still be solved.
 */
public class ConvexUserFiles extends OptimisationConvexTests implements ModelFileTest {

    private static final NumberContext ACCURACY = NumberContext.of(8);

    private static ExpressionsBasedModel doTest(final String modelName, final String expMinValString, final String expMaxValString) {
        return ConvexUserFiles.doTest(modelName, expMinValString, expMaxValString, ACCURACY);
    }

    private static ExpressionsBasedModel doTest(final String modelName, final String expMinValString, final String expMaxValString,
            final NumberContext accuracy) {

        ExpressionsBasedModel model = ModelFileTest.makeModel("usersupplied", modelName, false);

        // model.options.debug(Optimisation.Solver.class);
        // model.options.debug(IntegerSolver.class);
        // model.options.debug(ConvexSolver.class);
        // model.options.sparse = Boolean.FALSE;
        // model.options.debug(LinearSolver.class);
        // model.options.progress(IntegerSolver.class);
        // model.options.validate = false;
        // model.options.mip_defer = 0.25;
        // model.options.mip_gap = 1.0E-5;

        ModelFileTest.assertValues(model, expMinValString, expMaxValString, accuracy);

        return model;
    }

    /**
     * Numerically rather extreme/difficult model. After some tweaking ojAlgo gets the same solution as CPLEX
     * or GUROBI to 4 digits precision.
     */
    @Test
    public void testTCQPOptimalFail() {

        Result gurobi = Optimisation.Result.of(-1.44827995e-04, Optimisation.State.OPTIMAL, -5.214924976293389E-7, -1.8551050447309855E-10,
                5.219905098684252E-7, -1.5624792288894643E-10, -1.562499998481713E-10);

        Result cplex = Optimisation.Result.of(-1.4482898931605253E-4, Optimisation.State.OPTIMAL, -5.2149285E-7, -1.8541E-10, 5.219906E-7, -1.5609E-10,
                -1.562499999993681E-10);

        ExpressionsBasedModel model = ConvexUserFiles.doTest("TCQPOptimalFail.ebm", "-1.4483E-4", null, NumberContext.of(4));

        TestUtils.assertSolutionValid(model, gurobi);

        TestUtils.assertSolutionValid(model, cplex);
    }

    /**
     * This model was reported to have worked with v51.4.1 but failed with v52.0.0. The change that caused the
     * regression was in the {@link ConjugateGradientSolver}. The underlying difficulty is with the problem
     * formulation, it has rather extreme model parameters.
     */
    @Test
    public void testTCQPWorkAndFail() {

        Result gurobi = Optimisation.Result.of(-2.0788951439317316e+00, Optimisation.State.OPTIMAL, -9.9543353633269049e-05, 8.61193135185848e-05,
                1.8459753694143321e-05, -5.0356252991766056e-06, 1.9421945389085489e-14);

        Result cplex = Optimisation.Result.of(-2.078895163755299, Optimisation.State.OPTIMAL, -0.00009954335379, 0.00008611931362, 0.00001845975373,
                -0.00000503562527, 0);

        ExpressionsBasedModel model = ConvexUserFiles.doTest("TCQPWorkAndFail.ebm", "-2.078895", null);

        TestUtils.assertSolutionValid(model, gurobi);

        TestUtils.assertSolutionValid(model, cplex);
    }

}
