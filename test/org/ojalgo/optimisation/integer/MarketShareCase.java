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
package org.ojalgo.optimisation.integer;

import java.io.File;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.access.Structure1D.IntIndex;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.MathProgSysModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.type.context.NumberContext;

/**
 * Detailed testing of models and solvers using the markshare 5 0 model.
 * http://miplib.zib.de/miplib2010/markshare_5_0.php Objective Value min < MIP < max: 0.00000000e+00 <
 * 1.00000000e+00 < ?
 */
public final class MarketShareCase extends OptimisationIntegerTests {

    private static final BigDecimal OBJECTIVE_MIP = new BigDecimal(1);
    private static final Map<String, BigDecimal> SOLUTION;

    static {

        final HashMap<String, BigDecimal> tmpSolution = new HashMap<>();

        tmpSolution.put("s1", new BigDecimal(0));
        tmpSolution.put("s2", new BigDecimal(0));
        tmpSolution.put("s3", new BigDecimal(0));
        tmpSolution.put("s4", new BigDecimal(1));
        tmpSolution.put("s5", new BigDecimal(0));
        tmpSolution.put("x1", new BigDecimal(1));
        tmpSolution.put("x2", new BigDecimal(1));
        tmpSolution.put("x3", new BigDecimal(1));
        tmpSolution.put("x4", new BigDecimal(1));
        tmpSolution.put("x5", new BigDecimal(1));
        tmpSolution.put("x6", new BigDecimal(0));
        tmpSolution.put("x7", new BigDecimal(1));
        tmpSolution.put("x8", new BigDecimal(0));
        tmpSolution.put("x9", new BigDecimal(0));
        tmpSolution.put("x10", new BigDecimal(0));
        tmpSolution.put("x11", new BigDecimal(0));
        tmpSolution.put("x12", new BigDecimal(1));
        tmpSolution.put("x13", new BigDecimal(1));
        tmpSolution.put("x14", new BigDecimal(1));
        tmpSolution.put("x15", new BigDecimal(0));
        tmpSolution.put("x16", new BigDecimal(1));
        tmpSolution.put("x17", new BigDecimal(1));
        tmpSolution.put("x18", new BigDecimal(0));
        tmpSolution.put("x19", new BigDecimal(0));
        tmpSolution.put("x20", new BigDecimal(0));
        tmpSolution.put("x21", new BigDecimal(0));
        tmpSolution.put("x22", new BigDecimal(1));
        tmpSolution.put("x23", new BigDecimal(0));
        tmpSolution.put("x24", new BigDecimal(1));
        tmpSolution.put("x25", new BigDecimal(0));
        tmpSolution.put("x26", new BigDecimal(0));
        tmpSolution.put("x27", new BigDecimal(0));
        tmpSolution.put("x28", new BigDecimal(1));
        tmpSolution.put("x29", new BigDecimal(1));
        tmpSolution.put("x30", new BigDecimal(1));
        tmpSolution.put("x31", new BigDecimal(0));
        tmpSolution.put("x32", new BigDecimal(1));
        tmpSolution.put("x33", new BigDecimal(1));
        tmpSolution.put("x34", new BigDecimal(0));
        tmpSolution.put("x35", new BigDecimal(0));
        tmpSolution.put("x36", new BigDecimal(1));
        tmpSolution.put("x37", new BigDecimal(0));
        tmpSolution.put("x38", new BigDecimal(0));
        tmpSolution.put("x39", new BigDecimal(1));
        tmpSolution.put("x40", new BigDecimal(0));

        SOLUTION = Collections.unmodifiableMap(tmpSolution);
    }

    private static ExpressionsBasedModel makeModel() {

        final File tmpFile = new File(MipLibCase.PATH + "markshare_5_0.mps");

        final MathProgSysModel tmpMPS = MathProgSysModel.make(tmpFile);

        return tmpMPS.getExpressionsBasedModel();
    }

    @Test
    @Disabled("Underscored before JUnit 5")
    public void testFullMIP() {

        final ExpressionsBasedModel tmpModel = MarketShareCase.makeModel();

        //        tmpModel.options.debug_stream = BasicLogger.DEBUG;
        //        tmpModel.options.debug_solver = IntegerSolver.class;
        //        tmpModel.options.validate = true;

        final Result tmpResult = tmpModel.minimise();

        TestUtils.assertEquals("OBJECTIVE_MIP", OBJECTIVE_MIP.doubleValue(), tmpResult.getValue(), 1E-14 / PrimitiveMath.THREE);

        for (final Variable tmpVariable : tmpModel.getVariables()) {
            TestUtils.assertEquals(tmpVariable.getName(), SOLUTION.get(tmpVariable.getName()).doubleValue(), tmpVariable.getValue().doubleValue(),
                    1E-14 / PrimitiveMath.THREE);
        }
    }

    @Test
    @Disabled("Underscored before JUnit 5")
    public void testMipButLinearConstrainedToOptimal() {

        final ExpressionsBasedModel tmpModel = MarketShareCase.makeModel();

        for (final Variable tmpVariable : tmpModel.getVariables()) {
            final String tmpName = tmpVariable.getName();
            if (tmpName.startsWith("s")) {
                tmpVariable.level(SOLUTION.get(tmpName));
            }
        }

        final Result tmpResult = tmpModel.minimise();

        TestUtils.assertEquals("OBJECTIVE_MIP", OBJECTIVE_MIP.doubleValue(), tmpResult.getValue(), 1E-14 / PrimitiveMath.THREE);

        for (final Variable tmpVariable : tmpModel.getVariables()) {
            TestUtils.assertEquals(tmpVariable.getName(), SOLUTION.get(tmpVariable.getName()).doubleValue(), tmpVariable.getValue().doubleValue(),
                    1E-14 / PrimitiveMath.THREE);
        }
    }

    @Test
    public void testMipButSomeConstainedToOptimatl() {

        final ExpressionsBasedModel tmpModel = MarketShareCase.makeModel();

        // tmpModel.options.debug(IntegerSolver.class);

        // 37, 20
        final int tmpConstrLimit = 20;
        int tmpConstrCount = 0;
        for (final Variable tmpVariable : tmpModel.getVariables()) {
            final String tmpName = tmpVariable.getName();
            if (tmpConstrCount < tmpConstrLimit) {
                tmpVariable.level(SOLUTION.get(tmpName));
                tmpConstrCount++;
            }
        }

        final Result tmpResult = tmpModel.minimise();

        TestUtils.assertStateNotLessThanOptimal(tmpResult);
        TestUtils.assertTrue(tmpModel.validate(tmpModel.options.feasibility));
        TestUtils.assertTrue(tmpModel.validate(tmpResult, tmpModel.options.feasibility));

        TestUtils.assertEquals("OBJECTIVE_MIP", OBJECTIVE_MIP.doubleValue(), tmpResult.getValue(), tmpModel.options.feasibility);

        final NumberContext tmpContext = tmpModel.options.solution.newScale(13);
        for (final Variable tmpVariable : tmpModel.getVariables()) {
            final String tmpName = tmpVariable.getName();
            final double tmpExpected = SOLUTION.get(tmpName).doubleValue();
            final double tmpActual = tmpVariable.getValue().doubleValue();
            TestUtils.assertEquals(tmpName, tmpExpected, tmpActual, tmpContext);
        }
    }

    @Test
    public void testRedundantC1() {
        this.testRedundant("C1_");
    }

    @Test
    public void testRedundantC2() {
        this.testRedundant("C2_");
    }

    @Test
    public void testRedundantC3() {
        this.testRedundant("C3_");
    }

    @Test
    public void testRedundantC4() {
        this.testRedundant("C4_");
    }

    @Test
    public void testRedundantC5() {
        this.testRedundant("C5_");
    }

    @Test
    public void testRelaxedButAllConstrainedToOptimal() {

        final ExpressionsBasedModel tmpModel = MarketShareCase.makeModel();
        tmpModel.relax(true);

        for (final Variable tmpVariable : tmpModel.getVariables()) {
            tmpVariable.level(SOLUTION.get(tmpVariable.getName()));
        }

        final Result tmpResult = tmpModel.minimise();

        TestUtils.assertEquals("OBJECTIVE_MIP", OBJECTIVE_MIP.doubleValue(), tmpResult.getValue(), 1E-14 / PrimitiveMath.THREE);

        for (final Variable tmpVariable : tmpModel.getVariables()) {
            TestUtils.assertEquals(tmpVariable.getName(), SOLUTION.get(tmpVariable.getName()).doubleValue(), tmpVariable.getValue().doubleValue(),
                    1E-14 / PrimitiveMath.THREE);
        }

    }

    @Test
    public void testRelaxedButIntegerConstrainedToOptimal() {

        final ExpressionsBasedModel tmpModel = MarketShareCase.makeModel();
        tmpModel.relax(true);

        for (final Variable tmpVariable : tmpModel.getVariables()) {
            final String tmpName = tmpVariable.getName();
            if (tmpName.startsWith("x")) {
                tmpVariable.level(SOLUTION.get(tmpName));
            }
        }

        final Result tmpResult = tmpModel.minimise();

        TestUtils.assertEquals("OBJECTIVE_MIP", OBJECTIVE_MIP.doubleValue(), tmpResult.getValue(), 1E-14 / PrimitiveMath.THREE);

        for (final Variable tmpVariable : tmpModel.getVariables()) {
            TestUtils.assertEquals(tmpVariable.getName(), SOLUTION.get(tmpVariable.getName()).doubleValue(), tmpVariable.getValue().doubleValue(),
                    1E-14 / PrimitiveMath.THREE);
        }
    }

    @Test
    @Disabled("Underscored before JUnit 5")
    public void testSpecificBranch_20_25() {

        final PrimitiveDenseStore tmpAE = PrimitiveDenseStore.FACTORY.rows(new double[][] {
                { 0.88, 0.59, 1.0, 0.16, 0.7, 0.7, 0.12, 0.66, 0.85, 0.68, 0.97, 0.33, 0.87, 0.01, 0.6, 0.5, 0.85, 0.09, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 3.0, 0.7, 3.6, 7.3, 1.9, 1.5, 8.1, 5.5, 3.2, 5.3, 4.3, 2.1, 5.9, 5.7, 4.8, 2.8, 9.7, 4.4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 7.5, 9.6, 0.7, 5.4, 3.8, 0.0, 8.7, 0.2, 7.6, 6.3, 9.4, 5.5, 1.9, 4.6, 3.1, 2.4, 8.5, 7.4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.13, 0.97, 0.45, 0.32, 0.96, 0.36, 0.43, 0.96, 0.99, 0.58, 0.87, 0.15, 0.06, 0.26, 0.96, 0.31, 0.77, 0.77, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 5.5, 3.9, 9.6, 8.7, 1.6, 2.7, 0.9, 4.4, 7.9, 1.2, 0.8, 9.5, 3.8, 8.7, 1.5, 5.8, 7.9, 3.7, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 } });

        final PrimitiveDenseStore tmpBE = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 4.49 }, { 18.5 }, { 41.5 }, { 3.44 }, { 33.2 }, { 1.0 }, { 1.0 }, { 1.0 }, { 1.0 }, { 1.0 }, { 1.0 }, { 1.0 }, { 1.0 },
                        { 1.0 }, { 1.0 }, { 1.0 }, { 1.0 }, { 1.0 }, { 1.0 }, { 1.0 }, { 1.0 }, { 1.0 }, { 1.0 } });

        final PrimitiveDenseStore tmpC = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 },
                { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 },
                { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 } });

        final LinearSolver.Builder tmpBuilder = new LinearSolver.Builder(tmpC);
        tmpBuilder.equalities(tmpAE, tmpBE);

        final Optimisation.Options tmpOptions = new Optimisation.Options();
        //tmpOptions.debug(LinearSolver.class);

        final LinearSolver tmpSolver = tmpBuilder.build(tmpOptions);

        final Optimisation.Result tmpResult = tmpSolver.solve();

        TestUtils.assertTrue(tmpResult.getState().isOptimal());

        for (int i = 0; i < tmpResult.size(); i++) {
            final double tmpValue = tmpResult.doubleValue(i);
            TestUtils.assertTrue(!tmpOptions.feasibility.isDifferent(0.0, tmpValue) || !tmpOptions.feasibility.isDifferent(1.0, tmpValue));
        }

    }

    @Test
    public void testSpecificBranch_37_8() {

        final PrimitiveDenseStore tmpAE = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 0.87, 0.01, 0.6, 0.5, 0.85, 0.86, 0.09, 0.86, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 5.9, 5.7, 4.8, 2.8, 9.7, 5.8, 4.4, 3.7, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 1.9, 4.6, 3.1, 2.4, 8.5, 8.5, 7.4, 1.3, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.06, 0.26, 0.96, 0.31, 0.77, 0.1, 0.77, 0.71, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 3.8, 8.7, 1.5, 5.8, 7.9, 6.9, 3.7, 8.8, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 } });

        final PrimitiveDenseStore tmpBE = PrimitiveDenseStore.FACTORY.rows(
                new double[][] { { 2.24 }, { 20.2 }, { 17.4 }, { 0.73 }, { 25.2 }, { 1.0 }, { 1.0 }, { 1.0 }, { 1.0 }, { 1.0 }, { 1.0 }, { 1.0 }, { 1.0 } });

        final PrimitiveDenseStore tmpC = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 },
                { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 } });

        final LinearSolver.Builder tmpBuilder = new LinearSolver.Builder(tmpC);
        tmpBuilder.equalities(tmpAE, tmpBE);

        final Optimisation.Options tmpOptions = new Optimisation.Options();

        //        tmpOptions.debug_stream = BasicLogger.DEBUG;
        //        tmpOptions.debug_solver = LinearSolver.class;
        //        tmpOptions.validate = true;

        final LinearSolver tmpSolver = tmpBuilder.build(tmpOptions);

        final Optimisation.Result tmpResult = tmpSolver.solve();

        TestUtils.assertTrue(tmpResult.getState().isOptimal());

        for (int i = 0; i < tmpResult.size(); i++) {
            final double tmpValue = tmpResult.doubleValue(i);
            TestUtils.assertTrue(!tmpOptions.feasibility.isDifferent(0.0, tmpValue) || !tmpOptions.feasibility.isDifferent(1.0, tmpValue));
        }

    }

    private void testRedundant(final String constraint) {

        final ExpressionsBasedModel tmpModel = MarketShareCase.makeModel();

        final Expression tmpExpression = tmpModel.getExpression(constraint);

        if (DEBUG) {
            BasicLogger.debug("Fix count: {}", tmpExpression.getLinearKeySet().size());
        }

        for (final IntIndex tmpIndex : tmpExpression.getLinearKeySet()) {
            final Variable tmpVariable = tmpModel.getVariable(tmpIndex.index);
            final String tmpName = tmpVariable.getName();
            tmpVariable.level(SOLUTION.get(tmpName));
        }

        final Result tmpResult = tmpModel.minimise();

        final NumberContext tmpContext = new NumberContext(8, 13);
        TestUtils.assertEquals("OBJECTIVE_MIP", OBJECTIVE_MIP.doubleValue(), tmpResult.getValue(), tmpContext);

        for (final Variable tmpVariable : tmpModel.getVariables()) {
            TestUtils.assertEquals(tmpVariable.getName(), SOLUTION.get(tmpVariable.getName()).doubleValue(), tmpVariable.getValue().doubleValue(), tmpContext);
        }
    }
}
