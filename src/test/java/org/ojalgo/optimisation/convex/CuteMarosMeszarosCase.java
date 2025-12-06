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
import org.ojalgo.array.ArrayR256;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
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

        /**
         * The fraction of variables that are quadratic.
         *
         * @return [0,1]
         */
        public double getRatioQP() {
            return (double) QN / (double) N;
        }

        /**
         * All variables are quadratic
         */
        public boolean isPureQP() {
            return QN == N;
        }

        /**
         * The quadratic problem is called separable if Q has no off-diagonal nonzeros.
         */
        public boolean isSeparable() {
            return QNZ == 0;
        }

        /**
         * Number of variables and constraints are less than 1000 – can then use the community edition of
         * CPLEX.
         */
        public boolean isSmall() {
            return M <= 1_000 && N <= 1_000;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ModelInfo [M=");
            builder.append(M);
            builder.append(", N=");
            builder.append(N);
            builder.append(", NZ=");
            builder.append(NZ);
            builder.append(", QN=");
            builder.append(QN);
            builder.append(", QNZ=");
            builder.append(QNZ);
            builder.append(", OPT=");
            builder.append(OPT);
            builder.append("]");
            return builder.toString();
        }

    }

    /**
     * The optimal objective function value is given with 8 digits in the file 00README.QP, but they don't
     * seem to be exact.
     */
    private static final NumberContext ACCURACY = NumberContext.of(6);

    private static final Map<String, ModelInfo> MODEL_INFO;

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

    public static Map<String, ModelInfo> getModelInfo() {
        return MODEL_INFO;
    }

    public static ModelInfo getModelInfo(final String model) {
        String key = model.toUpperCase();
        key = key.replace("_", "");
        int dotIndex = key.indexOf(".");
        if (dotIndex > 0) {
            key = key.substring(0, dotIndex);
        }
        return MODEL_INFO.get(key);
    }

    public static ExpressionsBasedModel makeModel(final String name) {
        return ModelFileTest.makeModel("marosmeszaros", name, false);
    }

    private static void doTest(final String name, final NumberContext accuracy) {

        ModelInfo modelInfo = CuteMarosMeszarosCase.getModelInfo(name);

        String expMinValString = modelInfo.OPT.toPlainString();
        ExpressionsBasedModel model = ModelFileTest.makeModel("marosmeszaros", name, false);

        if (DEBUG) {

            BasicLogger.debug("ModelInfo: {}", modelInfo);
            BasicLogger.debug("Model vars={} expr={}", model.countVariables(), model.countExpressions());

            // model.options.debug(Optimisation.Solver.class);
            // model.options.debug(IntegerSolver.class);
            // model.options.debug(ConvexSolver.class);
            // model.options.convex().extendedPrecision(true);
            // model.options.debug(LinearSolver.class);
            // model.options.progress(IntegerSolver.class);
            // model.options.validate = false;
            // model.options.mip_defer = 0.25;
            // model.options.mip_gap = 1.0E-5;

        }

        //        model.options.convex().solverSPD(LU.R064::make);
        //        model.options.convex().projection(Boolean.TRUE);

        ModelFileTest.assertValues(model, expMinValString, null, accuracy != null ? accuracy : ACCURACY);

        //  BasicLogger.debug(CuteMarosMeszarosCase.getModelInfo(name));

    }

    static void doTest(final String name) {
        CuteMarosMeszarosCase.doTest(name, ACCURACY);
    }

    /**
     * <p>
     */
    @Test
    @Tag("unstable")
    @Tag("slow")
    public void testCVXQP1_M() {
        CuteMarosMeszarosCase.doTest("CVXQP1_M.SIF");
    }

    @Test
    public void testCVXQP1_S() {
        CuteMarosMeszarosCase.doTest("CVXQP1_S.SIF");
    }

    /**
     * <p>
     */
    @Test
    @Tag("unstable")
    @Tag("slow")
    public void testCVXQP2_M() {
        CuteMarosMeszarosCase.doTest("CVXQP2_M.SIF");
    }

    @Test
    public void testCVXQP2_S() {
        CuteMarosMeszarosCase.doTest("CVXQP2_S.SIF");
    }

    /**
     * <p>
     * The QP solver ends up (de)activating inequality constraints "forever"...
     */
    @Test
    public void testCVXQP3_M() {
        CuteMarosMeszarosCase.doTest("CVXQP3_M.SIF");
    }

    @Test
    public void testCVXQP3_S() {
        CuteMarosMeszarosCase.doTest("CVXQP3_S.SIF");
    }

    @Test
    public void testDUAL1() {
        CuteMarosMeszarosCase.doTest("DUAL1.SIF");
    }

    @Test
    public void testDUAL2() {
        CuteMarosMeszarosCase.doTest("DUAL2.SIF");
    }

    @Test
    public void testDUAL3() {
        CuteMarosMeszarosCase.doTest("DUAL3.SIF");
    }

    @Test
    public void testDUAL4() {
        CuteMarosMeszarosCase.doTest("DUAL4.SIF");
    }

    /**
     * <p>
     */
    @Test
    public void testDUALC1() {
        CuteMarosMeszarosCase.doTest("DUALC1.SIF");
    }

    /**
     * <p>
     */
    @Test
    public void testDUALC2() {
        CuteMarosMeszarosCase.doTest("DUALC2.SIF", ACCURACY.withPrecision(7));
    }

    /**
     * <p>
     */
    @Test
    public void testDUALC5() {
        CuteMarosMeszarosCase.doTest("DUALC5.SIF");
    }

    /**
     * <p>
     */
    @Test
    public void testDUALC8() {
        CuteMarosMeszarosCase.doTest("DUALC8.SIF");
    }

    /**
     * <p>
     */
    @Test
    public void testGENHS28() {
        CuteMarosMeszarosCase.doTest("GENHS28.SIF");
    }

    @Test
    public void testHS118() {
        CuteMarosMeszarosCase.doTest("HS118.SIF");
    }

    /**
     * <p>
     */
    @Test
    public void testHS21() {
        CuteMarosMeszarosCase.doTest("HS21.SIF");
    }

    /**
     * <p>
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

        Result proposed = new Result(Optimisation.State.OPTIMAL, ArrayR256.wrap(BigMath.ONE, BigMath.TWO, BigMath.NEG, BigMath.THREE, BigMath.FOUR.negate()));
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

    /**
     * <p>
     */
    @Test
    public void testHS35() {
        CuteMarosMeszarosCase.doTest("HS35.SIF", ACCURACY.withPrecision(6));
    }

    /**
     * <p>
     */
    @Test
    public void testHS35MOD() {
        CuteMarosMeszarosCase.doTest("HS35MOD.SIF");
    }

    /**
     * <p>
     */
    @Test
    public void testHS51() {
        CuteMarosMeszarosCase.doTest("HS51.SIF", ACCURACY.withScale(8));
    }

    /**
     * <p>
     */
    @Test
    public void testHS52() {
        CuteMarosMeszarosCase.doTest("HS52.SIF", ACCURACY.withScale(7));
    }

    /**
     * <p>
     */
    @Test
    public void testHS53() {
        CuteMarosMeszarosCase.doTest("HS53.SIF");
    }

    /**
     * <p>
     */
    @Test
    public void testHS76() {
        CuteMarosMeszarosCase.doTest("HS76.SIF");
    }

    /**
     * <p>
     */
    @Test
    public void testKSIP() {
        CuteMarosMeszarosCase.doTest("KSIP.SIF");
    }

    /**
     * <p>
     * Just takes way too long – 275s
     */
    @Test
    public void testMOSARQP2() {
        CuteMarosMeszarosCase.doTest("MOSARQP2.SIF");
    }

    /**
     * <p>
     */
    @Test
    public void testPRIMAL1() {
        CuteMarosMeszarosCase.doTest("PRIMAL1.SIF", ACCURACY.withScale(6));
    }

    /**
     * <p>
     */
    @Test
    public void testPRIMALC2() {
        CuteMarosMeszarosCase.doTest("PRIMALC2.SIF", ACCURACY.withScale(6));
    }

    /**
     * <p>
     */
    @Test
    public void testPRIMALC8() {
        CuteMarosMeszarosCase.doTest("PRIMALC8.SIF", ACCURACY.withScale(6));
    }

    /**
     * <p>
     */
    @Test
    public void testQPCBLEND() {
        CuteMarosMeszarosCase.doTest("QPCBLEND.SIF");
    }

    /**
     * <p>
     */
    @Test
    public void testQPCBOEI1() {
        CuteMarosMeszarosCase.doTest("QPCBOEI1.SIF", ACCURACY.withScale(7));
    }

    @Test
    public void testQPCBOEI2() {
        CuteMarosMeszarosCase.doTest("QPCBOEI2.SIF", ACCURACY.withScale(7));
    }

    /**
     * <p>
     */
    @Test
    public void testQPCSTAIR() {
        CuteMarosMeszarosCase.doTest("QPCSTAIR.SIF");
    }

    /**
     * <p>
     */
    @Test
    public void testQPTEST() {
        CuteMarosMeszarosCase.doTest("QPTEST.SIF");
    }

    /**
     * <p>
     */
    @Test
    public void testS268() {
        CuteMarosMeszarosCase.doTest("S268.SIF", ACCURACY.withScale(4));
    }

    /**
     * <p>
     */
    @Test
    public void testTAME() {
        CuteMarosMeszarosCase.doTest("TAME.SIF");
    }

    /**
     * <p>
     */
    @Test
    public void testZECEVIC2() {
        CuteMarosMeszarosCase.doTest("ZECEVIC2.SIF");
    }

}
