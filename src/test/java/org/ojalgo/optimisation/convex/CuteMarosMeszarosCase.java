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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.optimisation.ModelFileMPS;
import org.ojalgo.type.context.NumberContext;

/**
 * A collection of datasets found here: ftp://ftp.numerical.rl.ac.uk/pub/cutest//marosmeszaros/marmes.html
 * <p>
 * When specifying min/max null means unbounded in that direction and "1234567890" represents an unknown or
 * unverified value. And if the min and max values are set to the same value then one of them (max) is not
 * verified.
 *
 * <pre>
 * 2021-11-??: qwerty
 * </pre>
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

    /**
     * <ul>
     * <li>2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * <li>2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </ul>
     */
    @Test
    @Tag("slow")
    @Disabled
    public void testAUG2D() {
        CuteMarosMeszarosCase.doTest("AUG2D.SIF", "1.6874118e+06");
    }

    @Test
    public void testHS21() {
        CuteMarosMeszarosCase.doTest("HS21.SIF", "-9.9960000e+01");
    }

    /**
     * <ul>
     * <li>CPLEX Barrier - Optimal: Objective = 1.1702830307e-05
     * </ul>
     *
     * <pre>
    C------1                      0.995735
    C------2                      1.995283
    C------3                     -0.999028
    C------4                      2.989736
    C------5                     -3.982628
     * </pre>
     */
    @Test
    public void testHS268() {
        CuteMarosMeszarosCase.doTest("HS268.SIF", "5.7310705e-07");
    }

    @Test
    public void testS268() {
        CuteMarosMeszarosCase.doTest("S268.SIF", "5.7310705e-07");
    }

    @Test
    public void testHS51() {
        CuteMarosMeszarosCase.doTest("HS51.SIF", "8.8817842e-16");
    }

    @Test
    public void testHS52() {
        CuteMarosMeszarosCase.doTest("HS52.SIF", "5.3266476e+00");
    }

    @Test
    public void testHS53() {
        CuteMarosMeszarosCase.doTest("HS53.SIF", "4.0930233e+00");
    }

    @Test
    public void testHS35() {
        CuteMarosMeszarosCase.doTest("HS35.SIF", "1.1111111e-01");
    }

    @Test
    public void testHS76() {
        CuteMarosMeszarosCase.doTest("HS76.SIF", "-4.6818182e+00");
    }

    @Test
    public void testHS35MOD() {
        CuteMarosMeszarosCase.doTest("HS35MOD.SIF", "2.5000000e-01");
    }

    @Test
    public void testQPTEST() {
        CuteMarosMeszarosCase.doTest("QPTEST.SIF", "4.3718750e+00");
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
