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
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.type.context.NumberContext;

/**
 * User supplied LP models. May or may not have had specific problems. Tests mostly just verify that the
 * problem/model can still be solved.
 */
public class LinearUserFiles extends OptimisationLinearTests implements ModelFileTest {

    private static final NumberContext ACCURACY = NumberContext.of(8, 6);

    private static ExpressionsBasedModel doTest(final String modelName, final String expMinValString, final String expMaxValString) {
        return LinearUserFiles.doTest(modelName, expMinValString, expMaxValString, ACCURACY);
    }

    private static ExpressionsBasedModel doTest(final String modelName, final String expMinValString, final String expMaxValString,
            final NumberContext accuracy) {

        ExpressionsBasedModel model = ModelFileTest.makeModel("usersupplied", modelName, false);

        // model.options.debug(Optimisation.Solver.class);
        // model.options.debug(IntegerSolver.class);
        // model.options.debug(ConvexSolver.class);
        // model.options.debug(LinearSolver.class);
        // model.options.progress(IntegerSolver.class);
        // model.options.validate = false;
        // model.options.mip_defer = 0.25;
        // model.options.mip_gap = 1.0E-5;

        ModelFileTest.assertValues(model, expMinValString, expMaxValString, accuracy);

        return model;
    }

    @Test
    public void testHFLP201501020845() {
        LinearUserFiles.doTest("HFLP201501020845.ebm", null, "874.0050946596");
    }

    @Test
    public void testHFLP201607121500() {
        LinearUserFiles.doTest("HFLP201607121500.ebm", null, "26354.900646936687");
    }

    @Test
    public void testHFLP201706011645() {
        LinearUserFiles.doTest("HFLP201706011645.ebm", null, "13706.761009424292");
    }

    @Test
    public void testHFLP20170601T164500() {
        LinearUserFiles.doTest("HFLP20170601T164500.ebm", null, "13706.761009424285");
    }

    @Test
    public void testHFLP202001081415() {
        LinearUserFiles.doTest("HFLP202001081415.ebm", null, "76529.54481699542");
    }

    @Test
    public void testHFLP202004011500() {
        LinearUserFiles.doTest("HFLP202004011500.ebm", null, "24595.444093682945");
    }

    @Test
    public void testHFLP202004161430() {
        LinearUserFiles.doTest("HFLP202004161430.ebm", null, "93511.14595110042");
    }

    @Test
    public void testHFLP202009161330() {
        LinearUserFiles.doTest("HFLP202009161330.ebm", null, "86638.67833534388");
    }

    @Test
    public void testHFLP202009181715() {
        LinearUserFiles.doTest("HFLP202009181715.ebm", null, "24105.543829900747");
    }

    @Test
    public void testHFLP202009211500() {
        LinearUserFiles.doTest("HFLP202009211500.ebm", null, "10969.13920850186");
    }

    @Test
    public void testHFLP20200921T150000() {
        LinearUserFiles.doTest("HFLP20200921T150000.ebm", null, "10942.159757581489");
    }

    /**
     * A choco-solver LP relaxation (exported via OjAlgoModelRelaxation) that the dual/revised simplex used to
     * report INFEASIBLE. The objective is a single large-magnitude variable (x_0 in [0, ~4.0E7]) while most
     * other variables are binary, so the model is poorly scaled.
     * <p>
     * The trigger is the interaction between integrality and parameter scaling: integer entities are not
     * scaled — both {@code Variable.deriveAdjustmentExponent()} and {@code Expression.deriveAdjustmentExponent()}
     * return 0 when {@code isInteger()}. Because x_0 (and hence the objective expression) are integer, scaling
     * is disabled and the poorly-scaled model reaches the simplex unscaled. The phase-1 feasibility verdict
     * then compared a tiny floating-point residual on a near-zero slack — proportional to the large solution
     * magnitude — against a fixed absolute tolerance and wrongly declared INFEASIBLE. The verdict now judges
     * the residual relative to the solution magnitude.
     * <p>
     * The model is soft-relaxed ({@code relax(true)}): it uses the LP solver rather than the IntegerSolver,
     * but the variables stay flagged integer, so scaling stays disabled — the exact configuration that
     * triggered the failure. Removing the integer flags (a full relax) re-enables scaling and the model
     * solves either way, which is why the failure flipped on/off with the integer property. Must solve to
     * OPTIMAL.
     */
    @Test
    public void testPoorlyScaledLP() {

        ExpressionsBasedModel model = ModelFileTest.makeModel("usersupplied", "PoorlyScaledLP.ebm", false);

        // Soft relax: use the LP solver (not the IntegerSolver) but keep the variables flagged integer.
        // Integer entities are not scaled, so this keeps the poorly-scaled model reaching the simplex
        // unscaled - the configuration that triggered the false infeasibility.
        model.relax(true);

        Optimisation.Result result = model.minimise();

        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(6454624.0, result.getValue(), ACCURACY);
    }

}
