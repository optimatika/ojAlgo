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
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.type.context.NumberContext;

/**
 * Problems/models from "The Easy Set" - instances that can be solved in less than 1h (by top tier native code
 * solvers on modern powerful hardware). https://miplib.zib.de/set_easy.html Here we limit the set further to
 * instances with <=100 variables and constraints that are not infeasible. Problems that CPLEX can't solve
 * within about a minute are also removed from the set. That left 6 problem instances.
 *
 * @author apete
 */
public class MIPLIBTheEasySet extends OptimisationIntegerTests implements ModelFileTest {

    private static final NumberContext ACCURACY = NumberContext.of(8);

    private static void doTest(final String modelName, final String expMinValString, final String expMaxValString) {

        ExpressionsBasedModel model = ModelFileTest.makeModel("miplib", modelName, false);

        // model.options.debug(Optimisation.Solver.class);
        // model.options.debug(IntegerSolver.class);
        // model.options.debug(ConvexSolver.class);
        // model.options.debug(LinearSolver.class);
        // model.options.progress(IntegerSolver.class);
        // model.options.validate = false;
        // model.options.integer(IntegerStrategy.DEFAULT.withGapTolerance(NumberContext.of(3)));

        ModelFileTest.assertValues(model, expMinValString, expMaxValString, ACCURACY);
    }

    /**
     * https://miplib.zib.de/instance_details_b-ball.html
     * <p>
     * Mac Pro (Early 2009)
     * <ul>
     * <li>2019-01-28: 300s suffice with optimal solution
     * </ul>
     * MacBook Pro (16-inch, 2019)
     * <ul>
     * <li>2022-02-11: 300s suffice with optimal solution (actually found after less than 5s)
     * </ul>
     */
    @Test
    @Tag("unstable")
    public void testB_ball() {
        MIPLIBTheEasySet.doTest("b-ball.mps", "-1.5", null);
    }

    /**
     * https://miplib.zib.de/instance_details_flugpl.html
     * <p>
     * Mac Pro (Early 2009)
     * <ul>
     * <li>2019-01-28: 1s finished with optimal solution
     * </ul>
     * MacBook Pro (16-inch, 2019)
     * <ul>
     * <li>2022-02-11: 0s finished with optimal solution
     * </ul>
     */
    @Test
    public void testFlugpl() {
        MIPLIBTheEasySet.doTest("flugpl.mps", "1201500", null);
    }

    /**
     * https://miplib.zib.de/instance_details_gr4x6.html
     * <p>
     * Mac Pro (Early 2009)
     * <ul>
     * <li>2019-01-28: 0s finished with optimal solution
     * </ul>
     * MacBook Pro (16-inch, 2019)
     * <ul>
     * <li>2022-02-11: 0s finished with optimal solution
     * </ul>
     */
    @Test
    @Tag("new_lp_problem")
    public void testGr4x6() {
        MIPLIBTheEasySet.doTest("gr4x6.mps", "202.35", null);
    }

    /**
     * https://miplib.zib.de/instance_details_markshare_4_0.html
     * <p>
     * Mac Pro (Early 2009)
     * <ul>
     * <li>2019-01-28: 15s finished with optimal solution
     * </ul>
     * MacBook Pro (16-inch, 2019)
     * <ul>
     * <li>2022-02-11: 11s finished with optimal solution
     * <li>2022-04-18: 22s finished with optimal solution
     * </ul>
     */
    @Test
    public void testMarkshare_4_0() {
        MIPLIBTheEasySet.doTest("markshare_4_0.mps", "1", null);
    }

    /**
     * https://miplib.zib.de/instance_details_neos5.html
     * <p>
     * 2 or 3 integer solutions, including the optimal solution, is found immediately. Still the branching
     * goes on "forever", and in addition the generated LP branch-problems get harder and harder to solve for
     * the LP solver.
     * <p>
     * Mac Pro (Early 2009)
     * <ul>
     * <li>2019-01-28: 300s suffice with optimal solution
     * </ul>
     * MacBook Pro (16-inch, 2019)
     * <ul>
     * <li>2022-02-11: 300s suffice with optimal solution found (after just a few seconds)
     * </ul>
     */
    @Test
    @Tag("unstable")
    public void testNeos5() {
        MIPLIBTheEasySet.doTest("neos5.mps", "15", null);
    }

    /**
     * https://miplib.zib.de/instance_details_pk1.html
     * <p>
     * Mac Pro (Early 2009)
     * <ul>
     * <li>2013-04-01: (suffice=4h abort=8h) Stopped with optimal integer solution after 1h50min
     * <li>2013-12-08: (suffice=4h abort=8h) Stopped with optimal integer solution after 412s
     * <li>2015-11-07: (suffice=4h abort=8h) Stopped with optimal integer solution after 372s
     * <li>2017-10-20: (suffice=4h abort=8h) Stopped with optimal integer solution after 796s
     * <li>2017-10-20: (suffice=5min abort=1h) Stopped with optimal integer solution after 5min
     * <li>2018-02-07: (suffice=5min, abort=15min, mip_gap=0.001) Suffice with optimal solution
     * <li>2018-02-07: (suffice=15min, abort=15min, mip_gap=0.001) Found optimal solution in 344s
     * <li>2018-04-47: (suffice=5min, abort=15min, mip_gap=0.001) Found optimal solution in 227s
     * <li>2018-08-16: sufficed: <11.0> but was: <14.0>
     * <li>2019-01-28: 300s expected: <11.0> but was: <11.999999999999979>
     * </ul>
     * MacBook Pro (16-inch, 2019)
     * <ul>
     * <li>2022-02-11: Finished with optimal solution in 251s. (optimal solution found after about 150s)
     * <li>2022-03-17: Finished with optimal solution in 159s.
     * </ul>
     */
    @Test
    @Tag("unstable")
    public void testPk1() {
        MIPLIBTheEasySet.doTest("pk1.mps", "1.10000000e+01", null);
    }

}
