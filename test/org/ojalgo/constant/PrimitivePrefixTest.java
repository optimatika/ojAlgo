/*
 * Copyright 1997-2018 Optimatika
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
package org.ojalgo.constant;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.PrimitiveFunction;

public class PrimitivePrefixTest {

    private final double myTolerance = PrimitiveMath.MACHINE_EPSILON;

    @Test
    public void testPrefixes() {

        final double tmpTen = PrimitivePrefix.DEKA;

        double tmpNumber = PrimitiveFunction.POWER.invoke(tmpTen, -24);
        TestUtils.assertEquals("yocto, y", tmpNumber, PrimitivePrefix.YOCTO, myTolerance);

        tmpNumber = PrimitiveFunction.POWER.invoke(tmpTen, -21);
        TestUtils.assertEquals("zepto, z", tmpNumber, PrimitivePrefix.ZEPTO, myTolerance);

        tmpNumber = PrimitiveFunction.POWER.invoke(tmpTen, -18);
        TestUtils.assertEquals("atto, a", tmpNumber, PrimitivePrefix.ATTO, myTolerance);

        tmpNumber = PrimitiveFunction.POWER.invoke(tmpTen, -15);
        TestUtils.assertEquals("femto, f", tmpNumber, PrimitivePrefix.FEMTO, myTolerance);

        tmpNumber = PrimitiveFunction.POWER.invoke(tmpTen, -12);
        TestUtils.assertEquals("pico, p", tmpNumber, PrimitivePrefix.PICO, myTolerance);

        tmpNumber = PrimitiveFunction.POWER.invoke(tmpTen, -9);
        TestUtils.assertEquals("nano, n", tmpNumber, PrimitivePrefix.NANO, myTolerance);

        tmpNumber = PrimitiveFunction.POWER.invoke(tmpTen, -6);
        TestUtils.assertEquals("micro, my", tmpNumber, PrimitivePrefix.MICRO, myTolerance);

        tmpNumber = PrimitiveFunction.POWER.invoke(tmpTen, -3);
        TestUtils.assertEquals("milli, m", tmpNumber, PrimitivePrefix.MILLI, myTolerance);

        tmpNumber = PrimitiveFunction.POWER.invoke(tmpTen, -2);
        TestUtils.assertEquals("centi, c", tmpNumber, PrimitivePrefix.CENTI, myTolerance);

        tmpNumber = PrimitiveFunction.POWER.invoke(tmpTen, -1);
        TestUtils.assertEquals("deci, d", tmpNumber, PrimitivePrefix.DECI, myTolerance);

        tmpNumber = PrimitiveFunction.POWER.invoke(tmpTen, 1);
        TestUtils.assertEquals("deka, da", tmpNumber, PrimitivePrefix.DEKA, myTolerance);

        tmpNumber = PrimitiveFunction.POWER.invoke(tmpTen, 2);
        TestUtils.assertEquals("hecto, h", tmpNumber, PrimitivePrefix.HECTO, myTolerance);

        tmpNumber = PrimitiveFunction.POWER.invoke(tmpTen, 3);
        TestUtils.assertEquals("kilo, k", tmpNumber, PrimitivePrefix.KILO, myTolerance);

        tmpNumber = PrimitiveFunction.POWER.invoke(tmpTen, 6);
        TestUtils.assertEquals("mega, M", tmpNumber, PrimitivePrefix.MEGA, myTolerance);

        tmpNumber = PrimitiveFunction.POWER.invoke(tmpTen, 9);
        TestUtils.assertEquals("giga, G", tmpNumber, PrimitivePrefix.GIGA, myTolerance);

        tmpNumber = PrimitiveFunction.POWER.invoke(tmpTen, 12);
        TestUtils.assertEquals("tera, T", tmpNumber, PrimitivePrefix.TERA, myTolerance);

        tmpNumber = PrimitiveFunction.POWER.invoke(tmpTen, 15);
        TestUtils.assertEquals("peta, P", tmpNumber, PrimitivePrefix.PETA, myTolerance);

        tmpNumber = PrimitiveFunction.POWER.invoke(tmpTen, 18);
        TestUtils.assertEquals("exa, E", tmpNumber, PrimitivePrefix.EXA, myTolerance);

        tmpNumber = PrimitiveFunction.POWER.invoke(tmpTen, 21);
        TestUtils.assertEquals("zetta, CmplxNmbr", tmpNumber, PrimitivePrefix.ZETTA, myTolerance);

        tmpNumber = PrimitiveFunction.POWER.invoke(tmpTen, 24);
        TestUtils.assertEquals("yotta, Y", tmpNumber, PrimitivePrefix.YOTTA, myTolerance);
    }

}
