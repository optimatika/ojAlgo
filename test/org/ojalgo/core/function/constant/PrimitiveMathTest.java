/*
 * Copyright 1997-2020 Optimatika
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
package org.ojalgo.core.function.constant;

import static org.ojalgo.core.function.constant.PrimitiveMath.*;

import org.junit.jupiter.api.Test;
import org.ojalgo.core.TestUtils;
import org.ojalgo.core.function.constant.PrimitiveMath.Prefix;
import org.ojalgo.core.type.context.NumberContext;

public class PrimitiveMathTest {

    private static final double TOLERANCE = MACHINE_EPSILON;

    private static void compare(final double arg0, final double arg1) {

        if (arg0 == arg1) {
            if (NumberContext.compare(arg0, arg1) != 0) {
                TestUtils.fail();
            }
        } else {
            //no inspection, Result Of Method Call Ignored
            NumberContext.compare(arg0, arg1);
            TestUtils.fail();
        }

    }

    @Test
    public void testACOSH() {
        TestUtils.assertEquals(ZERO, ACOSH.invoke(ONE), MACHINE_EPSILON);
    }

    @Test
    public void testASINH() {
        TestUtils.assertEquals(ZERO, ASINH.invoke(ZERO), MACHINE_EPSILON);
    }

    @Test
    public void testATANH() {
        TestUtils.assertEquals(ZERO, ATANH.invoke(ZERO), MACHINE_EPSILON);
        TestUtils.assertEquals(POSITIVE_INFINITY, ATANH.invoke(ONE), MACHINE_EPSILON);
        TestUtils.assertEquals(NEGATIVE_INFINITY, ATANH.invoke(NEG), MACHINE_EPSILON);
    }

    @Test
    public void testCompareToZeros() {

        final double negDbl = -0.0;
        final double posInt = 0;
        final double posDbl = 0.0;
        final double negInt = -0;

        PrimitiveMathTest.compare(negDbl, posInt);
        PrimitiveMathTest.compare(negDbl, posDbl);
        PrimitiveMathTest.compare(negDbl, negInt);

        PrimitiveMathTest.compare(posInt, negDbl);
        PrimitiveMathTest.compare(posInt, posDbl);
        PrimitiveMathTest.compare(posInt, negInt);

        PrimitiveMathTest.compare(posDbl, negDbl);
        PrimitiveMathTest.compare(posDbl, posInt);
        PrimitiveMathTest.compare(posDbl, negInt);

        PrimitiveMathTest.compare(negInt, negDbl);
        PrimitiveMathTest.compare(negInt, posInt);
        PrimitiveMathTest.compare(negInt, posDbl);
    }

    @Test
    public void testHYPOT() {

        TestUtils.assertEquals(FIVE, HYPOT.invoke(FOUR, THREE), MACHINE_EPSILON);
        TestUtils.assertEquals(NaN, HYPOT.invoke(NaN, NaN), MACHINE_EPSILON);

    }

    @Test
    public void testPOWER() {

        TestUtils.assertEquals(ONE, POWER.invoke(ZERO, 0), MACHINE_EPSILON);
        TestUtils.assertEquals(ONE, POWER.invoke(PI, 0), MACHINE_EPSILON);
        TestUtils.assertEquals(ONE, POWER.invoke(E, 0), MACHINE_EPSILON);

        TestUtils.assertEquals(ZERO, POWER.invoke(ZERO, 1), MACHINE_EPSILON);
        TestUtils.assertEquals(PI, POWER.invoke(PI, 1), MACHINE_EPSILON);
        TestUtils.assertEquals(E, POWER.invoke(E, 1), MACHINE_EPSILON);

        TestUtils.assertEquals(ZERO * ZERO, POWER.invoke(ZERO, 2), MACHINE_EPSILON);
        TestUtils.assertEquals(PI * PI, POWER.invoke(PI, 2), MACHINE_EPSILON);
        TestUtils.assertEquals(E * E, POWER.invoke(E, 2), MACHINE_EPSILON);

        TestUtils.assertEquals(1 / ZERO, POWER.invoke(ZERO, -1), MACHINE_EPSILON);
        TestUtils.assertEquals(1 / PI, POWER.invoke(PI, -1), MACHINE_EPSILON);
        TestUtils.assertEquals(1 / E, POWER.invoke(E, -1), MACHINE_EPSILON);
    }

    @Test
    public void testPrefixes() {

        double expected = Double.NaN;

        expected = POWER.invoke(TEN, -24);
        TestUtils.assertEquals("yocto, y", expected, Prefix.YOCTO, TOLERANCE);

        expected = POWER.invoke(TEN, -21);
        TestUtils.assertEquals("zepto, z", expected, Prefix.ZEPTO, TOLERANCE);

        expected = POWER.invoke(TEN, -18);
        TestUtils.assertEquals("atto, a", expected, Prefix.ATTO, TOLERANCE);

        expected = POWER.invoke(TEN, -15);
        TestUtils.assertEquals("femto, f", expected, Prefix.FEMTO, TOLERANCE);

        expected = POWER.invoke(TEN, -12);
        TestUtils.assertEquals("pico, p", expected, Prefix.PICO, TOLERANCE);

        expected = POWER.invoke(TEN, -9);
        TestUtils.assertEquals("nano, n", expected, Prefix.NANO, TOLERANCE);

        expected = POWER.invoke(TEN, -6);
        TestUtils.assertEquals("micro, my", expected, Prefix.MICRO, TOLERANCE);

        expected = POWER.invoke(TEN, -3);
        TestUtils.assertEquals("milli, m", expected, Prefix.MILLI, TOLERANCE);

        expected = POWER.invoke(TEN, -2);
        TestUtils.assertEquals("centi, c", expected, Prefix.CENTI, TOLERANCE);

        expected = POWER.invoke(TEN, -1);
        TestUtils.assertEquals("deci, d", expected, Prefix.DECI, TOLERANCE);

        expected = POWER.invoke(TEN, 1);
        TestUtils.assertEquals("deka, da", expected, Prefix.DEKA, TOLERANCE);

        expected = POWER.invoke(TEN, 2);
        TestUtils.assertEquals("hecto, h", expected, Prefix.HECTO, TOLERANCE);

        expected = POWER.invoke(TEN, 3);
        TestUtils.assertEquals("kilo, k", expected, Prefix.KILO, TOLERANCE);

        expected = POWER.invoke(TEN, 6);
        TestUtils.assertEquals("mega, M", expected, Prefix.MEGA, TOLERANCE);

        expected = POWER.invoke(TEN, 9);
        TestUtils.assertEquals("giga, G", expected, Prefix.GIGA, TOLERANCE);

        expected = POWER.invoke(TEN, 12);
        TestUtils.assertEquals("tera, T", expected, Prefix.TERA, TOLERANCE);

        expected = POWER.invoke(TEN, 15);
        TestUtils.assertEquals("peta, P", expected, Prefix.PETA, TOLERANCE);

        expected = POWER.invoke(TEN, 18);
        TestUtils.assertEquals("exa, E", expected, Prefix.EXA, TOLERANCE);

        expected = POWER.invoke(TEN, 21);
        TestUtils.assertEquals("zetta, CmplxNmbr", expected, Prefix.ZETTA, TOLERANCE);

        expected = POWER.invoke(TEN, 24);
        TestUtils.assertEquals("yotta, Y", expected, Prefix.YOTTA, TOLERANCE);
    }

}
