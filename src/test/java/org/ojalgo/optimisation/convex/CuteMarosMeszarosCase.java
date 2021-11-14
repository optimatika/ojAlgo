/*
 * Copyright 1997-2021 Optimatika
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

import java.math.BigDecimal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.BigArray;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileMPS;
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
public class CuteMarosMeszarosCase extends OptimisationConvexTests implements ModelFileMPS {

    /**
     * The correct/optimal objective function value is given with 8 digits in the file 00README.QP.
     */
    private static final NumberContext ACCURACY = NumberContext.of(8, 12);

    static void doTest(final String name, final String expMinValString) {
        CuteMarosMeszarosCase.doTest(name, expMinValString, ACCURACY);
    }

    static void doTest(final String name, final String expMinValString, final NumberContext accuracy) {
        ModelFileMPS.makeAndAssert("marosmeszaros", name, expMinValString, null, false, accuracy != null ? accuracy : ACCURACY, null);
    }

    static ExpressionsBasedModel makeModel(final String name) {
        return ModelFileMPS.makeModel("marosmeszaros", name, false);
    }

    /**
     * <ul>
     * <li>2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * <li>2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </ul>
     */
    @Test
    @Tag("slow")
    public void testAUG2D() {
        CuteMarosMeszarosCase.doTest("AUG2D.SIF", "1.6874118e+06");
    }

    @Test
    @Tag("slow")
    public void testAUG2DC() {
        CuteMarosMeszarosCase.doTest("AUG2DC.SIF", "1.8183681e+06");
    }

    @Test
    @Tag("slow")
    public void testAUG2DCQP() {
        CuteMarosMeszarosCase.doTest("AUG2DCQP.SIF", "6.4981348e+06");
    }

    @Test
    @Tag("slow")
    public void testAUG2DQP() {
        CuteMarosMeszarosCase.doTest("AUG2DQP.SIF", "6.2370121e+06");
    }

    @Test
    public void testAUG3D() {
        CuteMarosMeszarosCase.doTest("AUG3D.SIF", "5.5406773e+02");
    }

    @Test
    public void testAUG3DC() {
        CuteMarosMeszarosCase.doTest("AUG3DC.SIF", "7.7126244e+02");
    }

    @Test
    @Tag("unstable")
    public void testAUG3DCQP() {
        CuteMarosMeszarosCase.doTest("AUG3DCQP.SIF", "9.9336215e+02");
    }

    @Test
    @Tag("unstable")
    public void testAUG3DQP() {
        CuteMarosMeszarosCase.doTest("AUG3DQP.SIF", "6.7523767e+02");
    }

    @Test
    public void testDUALC1() {
        CuteMarosMeszarosCase.doTest("DUALC1.SIF", "6.1552508e+03");
    }

    @Test
    public void testDUALC2() {
        CuteMarosMeszarosCase.doTest("DUALC2.SIF", "3.5513077e+03");
    }

    @Test
    public void testDUALC5() {
        CuteMarosMeszarosCase.doTest("DUALC5.SIF", "4.2723233e+02");
    }

    @Test
    public void testDUALC8() {
        CuteMarosMeszarosCase.doTest("DUALC8.SIF", "1.8309359e+04");
    }

    @Test
    public void testGENHS28() {
        CuteMarosMeszarosCase.doTest("GENHS28", "9.2717369e-01");
    }

    @Test
    public void testHS21() {
        CuteMarosMeszarosCase.doTest("HS21.SIF", "-9.9960000e+01");
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
     * <li>CPLEX returns a very inexact solution resulting in a value worse than the given.
     * </ol>
     */
    @Test
    public void testHS268() {

        CuteMarosMeszarosCase.doTest("HS268.SIF", "5.7310705e-07", ACCURACY.withScale(5));

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

        TestUtils.assertEquals(1.1702830307e-05, cplexVal.doubleValue(), ACCURACY.withPrecision(4));

        // Assert proposed solution valid with very high precision, and that it gives objective function value 0.0
        NumberContext VERY_HIGH_PRECISION = NumberContext.of(24);
        TestUtils.assertTrue(model.validate(cplex, VERY_HIGH_PRECISION));
        TestUtils.assertEquals(BigMath.ZERO, propVal, VERY_HIGH_PRECISION);
    }

    @Test
    public void testHS35() {
        CuteMarosMeszarosCase.doTest("HS35.SIF", "1.1111111e-01");
    }

    @Test
    public void testHS35MOD() {
        CuteMarosMeszarosCase.doTest("HS35MOD.SIF", "2.5000000e-01");
    }

    @Test
    public void testHS51() {
        CuteMarosMeszarosCase.doTest("HS51.SIF", "8.8817842e-16", ACCURACY.withScale(8));
    }

    @Test
    public void testHS52() {
        CuteMarosMeszarosCase.doTest("HS52.SIF", "5.3266476e+00", ACCURACY.withScale(7));
    }

    @Test
    public void testHS53() {
        CuteMarosMeszarosCase.doTest("HS53.SIF", "4.0930233e+00");
    }

    @Test
    public void testHS76() {
        CuteMarosMeszarosCase.doTest("HS76.SIF", "-4.6818182e+00");
    }

    @Test
    public void testQPTEST() {
        CuteMarosMeszarosCase.doTest("QPTEST.SIF", "4.3718750e+00");
    }

    @Test
    public void testS268() {
        CuteMarosMeszarosCase.doTest("S268.SIF", "5.7310705e-07", ACCURACY.withScale(5));
    }

    @Test
    public void testTAME() {
        CuteMarosMeszarosCase.doTest("TAME.SIF", "0.0000000e+00");
    }

    @Test
    public void testZECEVIC2() {
        CuteMarosMeszarosCase.doTest("ZECEVIC2.SIF", "-4.1250000e+00");
    }

}
