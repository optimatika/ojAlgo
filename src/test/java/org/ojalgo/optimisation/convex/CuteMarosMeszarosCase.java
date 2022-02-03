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
package org.ojalgo.optimisation.convex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.BigArray;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ExpressionsBasedModel.FileFormat;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.type.context.NumberContext;

/**
 * A collection of datasets found here: ftp://ftp.numerical.rl.ac.uk/pub/cutest//marosmeszaros/marmes.html
 * <p>
 * Tests with more than 1000 variables and/or constraints are tagged "slow" (can't be solved with community
 * version of CPLEX)
 * <p>
 * Tests that are otherwise difficult for ojAlgo are tagged "unstable"
 *
 * @author apete
 */
public class CuteMarosMeszarosCase extends OptimisationConvexTests implements ModelFileTest {

    public static final class ModelInfo {

        /**
         * number of rows in A
         */
        public int M;
        /**
         * number of variables
         */
        public int N;
        /**
         * number of nonzeros in A
         */
        public int NZ;
        /**
         * solution value obtained by the default settings of BPMPD solver
         */
        public BigDecimal OPT;
        /**
         * number of quadratic variables
         */
        public int QN;
        /**
         * number of off-diagonal entries in the lower triangular part of Q
         */
        public int QNZ;

    }

    public static final Map<String, ModelInfo> MODEL_INFO;

    /**
     * The correct/optimal objective function value is given with 8 digits in the file 00README.QP.
     */
    private static final NumberContext ACCURACY = NumberContext.of(8, 12);

    static {

        Map<String, ModelInfo> modelInfo = new HashMap<>();

        String line;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(TestUtils.getResource("optimisation", "marosmeszaros", "00README.CSV")))) {

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split("\\s+");

                String key = parts[0].toUpperCase();

                ModelInfo value = new ModelInfo();

                value.M = Integer.parseInt(parts[1]);
                value.N = Integer.parseInt(parts[2]);
                value.NZ = Integer.parseInt(parts[3]);
                value.QN = Integer.parseInt(parts[4]);
                value.QNZ = Integer.parseInt(parts[5]);
                value.OPT = new BigDecimal(parts[6]);

                modelInfo.put(key, value);
            }

        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }

        // Overide with values known to be better

        modelInfo.get("HS268").OPT = BigMath.ZERO;

        MODEL_INFO = Collections.unmodifiableMap(modelInfo);
    }

    private static void doTest(final String name, final NumberContext accuracy) {
        String expMinValString = MODEL_INFO.get(name.substring(0, name.indexOf("."))).OPT.toPlainString();
        ModelFileTest.makeAndAssert("marosmeszaros", name, FileFormat.MPS, false, expMinValString, null, accuracy != null ? accuracy : ACCURACY);
    }

    private static ExpressionsBasedModel makeModel(final String name) {
        return ModelFileTest.makeModel("marosmeszaros", name, false, FileFormat.MPS);
    }

    static void doTest(final String name) {
        CuteMarosMeszarosCase.doTest(name, ACCURACY);
    }

    /**
     * <ul>
     * <li>
     * </ul>
     */
    @Test
    @Tag("slow")
    public void testAUG2D() {
        CuteMarosMeszarosCase.doTest("AUG2D.SIF");
    }

    @Test
    @Tag("slow")
    public void testAUG2DC() {
        CuteMarosMeszarosCase.doTest("AUG2DC.SIF");
    }

    @Test
    @Tag("slow")
    public void testAUG2DCQP() {
        CuteMarosMeszarosCase.doTest("AUG2DCQP.SIF");
    }

    @Test
    @Tag("slow")
    public void testAUG2DQP() {
        CuteMarosMeszarosCase.doTest("AUG2DQP.SIF");
    }

    @Test
    @Tag("slow")
    public void testAUG3D() {
        CuteMarosMeszarosCase.doTest("AUG3D.SIF");
    }

    @Test
    @Tag("slow")
    public void testAUG3DC() {
        CuteMarosMeszarosCase.doTest("AUG3DC.SIF");
    }

    @Test
    @Tag("slow")
    public void testAUG3DCQP() {
        CuteMarosMeszarosCase.doTest("AUG3DCQP.SIF");
    }

    @Test
    @Tag("slow")
    public void testAUG3DQP() {
        CuteMarosMeszarosCase.doTest("AUG3DQP.SIF");
    }

    @Test
    public void testDUALC1() {
        CuteMarosMeszarosCase.doTest("DUALC1.SIF");
    }

    @Test
    public void testDUALC2() {
        CuteMarosMeszarosCase.doTest("DUALC2.SIF", ACCURACY.withPrecision(7));
    }

    @Test
    public void testDUALC5() {
        CuteMarosMeszarosCase.doTest("DUALC5.SIF");
    }

    @Test
    public void testDUALC8() {
        CuteMarosMeszarosCase.doTest("DUALC8.SIF");
    }

    @Test
    public void testGENHS28() {
        CuteMarosMeszarosCase.doTest("GENHS28.SIF");
    }

    @Test
    public void testHS21() {
        CuteMarosMeszarosCase.doTest("HS21.SIF");
    }

    /**
     * The given objective function value is 5.7310705e-07 but CPLEX gets 1.1702830307e-05 (and ojAlgo
     * 1.9521E-23). The CPLEX solution is:
     *
     * <pre>
    C------1                      0.995735
    C------2                      1.995283
    C------3                     -0.999028
    C------4                      2.989736
    C------5                     -3.982628
     * </pre>
     *
     * Guessing that { 1.0, 2.0, -1.0, 3.0, -4.0 } is the exact/actual optimal solution. That gives the
     * objective value 0.0 (exactly).
     * <p>
     * Find it somewhat surprising that:
     * <ol>
     * <li>The given value is so inexact. It's a small model. You can validate it with pen and paper.
     * <li>CPLEX returns a very inexact solution, it's only correct to 2 digits precision, resulting in a
     * value worse than the given.
     * </ol>
     */
    @Test
    public void testHS268() {

        CuteMarosMeszarosCase.doTest("HS268.SIF", ACCURACY.withScale(4));

        ExpressionsBasedModel model = CuteMarosMeszarosCase.makeModel("HS268.SIF");

        Result proposed = new Result(Optimisation.State.OPTIMAL, BigArray.wrap(BigMath.ONE, BigMath.TWO, BigMath.NEG, BigMath.THREE, BigMath.FOUR.negate()));
        Result cplex = Result.of(Double.NaN, Optimisation.State.OPTIMAL, 0.995735, 1.995283, -0.999028, 2.989736, -3.982628);

        Expression obj = model.objective();

        // Assert that the proposed solution results in a better objective function value.
        BigDecimal propVal = obj.evaluate(proposed);
        BigDecimal cplexVal = obj.evaluate(cplex);

        TestUtils.assertLessThan(5.7310705e-07, propVal.doubleValue()); // Given
        TestUtils.assertLessThan(1.1702830307e-05, propVal.doubleValue()); // CPLEX
        TestUtils.assertTrue(propVal.compareTo(cplexVal) < 0); // From CPLEX solution

        TestUtils.assertEquals(1.1702830307e-05, cplexVal.doubleValue(), ACCURACY.withScale(4));

        // Assert proposed solution valid with very high precision, and that it gives objective function value 0.0
        NumberContext VERY_HIGH_PRECISION = NumberContext.of(24);
        TestUtils.assertTrue(model.validate(cplex, VERY_HIGH_PRECISION));
        TestUtils.assertEquals(BigMath.ZERO, propVal, VERY_HIGH_PRECISION);
    }

    @Test
    public void testHS35() {
        CuteMarosMeszarosCase.doTest("HS35.SIF", ACCURACY.withPrecision(6));
    }

    @Test
    public void testHS35MOD() {
        CuteMarosMeszarosCase.doTest("HS35MOD.SIF");
    }

    @Test
    public void testHS51() {
        CuteMarosMeszarosCase.doTest("HS51.SIF", ACCURACY.withScale(8));
    }

    @Test
    public void testHS52() {
        CuteMarosMeszarosCase.doTest("HS52.SIF", ACCURACY.withScale(7));
    }

    @Test
    public void testHS53() {
        CuteMarosMeszarosCase.doTest("HS53.SIF");
    }

    @Test
    public void testHS76() {
        CuteMarosMeszarosCase.doTest("HS76.SIF");
    }

    @Test
    public void testKSIP() {
        CuteMarosMeszarosCase.doTest("KSIP.SIF");
    }

    /**
     * ojAlgo returns state FAILED
     * <p>
     * There are redundant constraints
     * <p>
     * The fixed variables (from presolve) completely zeros the Q-matrix – no longer suitable for ConvexSolver
     */
    @Test
    @Tag("unstable")
    public void testQBORE3D() {
        CuteMarosMeszarosCase.doTest("QBORE3D.SIF", ACCURACY.withScale(10));
    }

    /**
     * ojAlgo returns state INFEASIBLE
     */
    @Test
    @Tag("unstable")
    public void testQFORPLAN() {
        CuteMarosMeszarosCase.doTest("QFORPLAN.SIF", ACCURACY.withScale(5));
    }

    /**
     * ojAlgo takes forever - ends up in some loop...
     */
    @Test
    @Tag("unstable")
    public void testQGROW22() {
        CuteMarosMeszarosCase.doTest("QGROW22.SIF", ACCURACY.withScale(6));
    }

    /**
     * ojAlgo's optimal value is completely off
     */
    @Test
    @Tag("unstable")
    public void testQPCBOEI1() {
        CuteMarosMeszarosCase.doTest("QPCBOEI1.SIF", ACCURACY.withScale(6));
    }

    /**
     * ojAlgo returns state INFEASIBLE
     * <p>
     * Problem with the LP solver. Switches to phase 2 with many artificials still in the basis, and doesn't
     * get rid of them. Pick non-artificial rows with numer == 0.0 rather than anyof the artificials.
     */
    @Test
    @Tag("unstable")
    public void testQPCSTAIR() {
        CuteMarosMeszarosCase.doTest("QPCSTAIR.SIF");
    }

    @Test
    public void testQPTEST() {
        CuteMarosMeszarosCase.doTest("QPTEST.SIF");
    }

    /**
     * ojAlgo finds a sub-optimal solution
     * <p>
     * Too many constraints are initially activated, making the KKT system unsolvable, and somehow cutting off
     * the optimal value
     * <p>
     * Several iterations with very small step lengths
     */
    @Test
    @Tag("unstable")
    public void testQRECIPE() {
        CuteMarosMeszarosCase.doTest("QRECIPE.SIF", ACCURACY.withScale(7));
    }

    /**
     * ojAlgo fails to solve the problems – returns state FAILED
     * <p>
     * There are redundant constraints
     */
    @Test
    @Tag("unstable")
    public void testQSCORPIO() {
        CuteMarosMeszarosCase.doTest("QSCORPIO.SIF");
    }

    /**
     * ojAlgo finds the problem to be INFEASIBLE
     */
    @Test
    @Tag("unstable")
    public void testQSHARE1B() {
        CuteMarosMeszarosCase.doTest("QSHARE1B.SIF", ACCURACY.withPrecision(3).withScale(8));
    }

    /**
     * ojAlgo finds the problem to be INFEASIBLE
     */
    @Test
    @Tag("unstable")
    public void testQSTAIR() {
        CuteMarosMeszarosCase.doTest("QSTAIR.SIF", ACCURACY.withScale(8));
    }

    @Test
    public void testS268() {
        CuteMarosMeszarosCase.doTest("S268.SIF", ACCURACY.withScale(4));
    }

    @Test
    public void testTAME() {
        CuteMarosMeszarosCase.doTest("TAME.SIF");
    }

    @Test
    public void testZECEVIC2() {
        CuteMarosMeszarosCase.doTest("ZECEVIC2.SIF");
    }

}
