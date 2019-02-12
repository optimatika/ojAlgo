/*
 * Copyright 1997-2018 Optimatika Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions: The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ojalgo.optimisation.linear;

import java.io.File;
import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.MathProgSysModel;
import org.ojalgo.optimisation.ModelFileMPS;

/**
 * A collection of datasets found here: http://www-new.mcs.anl.gov/otc/Guide/TestProblems/LPtest/
 *
 * @author apete
 */
public class NetlibCase extends OptimisationLinearTests {

    /**
     * OK! 2010-04-19 lp_solve => -30.81214985
     */
    @Test
    public void testBlend() {

        final File tmpFile = new File(ModelFileMPS.LIN_PATH + "blend.mps");
        final MathProgSysModel tmpMPS = MathProgSysModel.make(tmpFile);
        final ExpressionsBasedModel tmpModel = tmpMPS.getExpressionsBasedModel();

        //tmpModel.options.problem = new NumberContext(32, 8, RoundingMode.HALF_EVEN);
        //tmpModel.options.solution = new NumberContext(16, 10, RoundingMode.HALF_EVEN);

        // tmpModel.options.debug(LinearSolver.class);

        ModelFileMPS.assertMinMaxVal(tmpModel, new BigDecimal("-3.0812149846E+01"), null);
    }

    /**
     * OK! 2010-04-19 lp_solve => -315.01872802
     */
    @Test
    public void testBoeing2() {

        final File tmpFile = new File(ModelFileMPS.LIN_PATH + "boeing2.mps");
        final MathProgSysModel tmpMPS = MathProgSysModel.make(tmpFile);
        final ExpressionsBasedModel tmpModel = tmpMPS.getExpressionsBasedModel();

        ModelFileMPS.assertMinMaxVal(tmpModel, new BigDecimal("-3.1501872802E+02"), null);
    }

    /**
     * OK! 2010-04-19 lp_solve => -1749.90012991
     */
    @Test
    public void testKb2() {

        final File tmpFile = new File(ModelFileMPS.LIN_PATH + "kb2.mps");
        final MathProgSysModel tmpMPS = MathProgSysModel.make(tmpFile);
        final ExpressionsBasedModel tmpModel = tmpMPS.getExpressionsBasedModel();

        ModelFileMPS.assertMinMaxVal(tmpModel, new BigDecimal("-1.74990012991E+03"), null);
    }

    /**
     * OK! 2010-04-19 lp_solve => -70.00000000
     */
    @Test
    public void testSc50b() {

        final File tmpFile = new File(ModelFileMPS.LIN_PATH + "sc50b.mps");
        final MathProgSysModel tmpMPS = MathProgSysModel.make(tmpFile);
        final ExpressionsBasedModel tmpModel = tmpMPS.getExpressionsBasedModel();

        ModelFileMPS.assertMinMaxVal(tmpModel, new BigDecimal("-7.0000000000E+01"), null);
    }

    /**
     * OK! 2010-04-19 lp_solve => -415.73224074
     */
    @Test
    public void testShare2b() {

        final File tmpFile = new File(ModelFileMPS.LIN_PATH + "share2b.mps");
        final MathProgSysModel tmpMPS = MathProgSysModel.make(tmpFile);
        final ExpressionsBasedModel tmpModel = tmpMPS.getExpressionsBasedModel();

        ModelFileMPS.assertMinMaxVal(tmpModel, new BigDecimal("-4.1573224074E+02"), null);
    }
}
