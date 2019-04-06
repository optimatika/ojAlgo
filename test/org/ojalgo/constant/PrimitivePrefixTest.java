/*
 * Copyright 1997-2019 Optimatika
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
import org.ojalgo.function.constant.PrimitiveMath;

public class PrimitivePrefixTest {

    private final double myTolerance = PrimitiveMath.MACHINE_EPSILON;

    @Test
    public void testPrefixes() {

        final double tmpTen = PrimitiveMath.Prefix.DEKA;

        double tmpNumber = PrimitiveMath.POWER.invoke(tmpTen, -24);
        TestUtils.assertEquals("yocto, y", tmpNumber, PrimitiveMath.Prefix.YOCTO, myTolerance);

        tmpNumber = PrimitiveMath.POWER.invoke(tmpTen, -21);
        TestUtils.assertEquals("zepto, z", tmpNumber, PrimitiveMath.Prefix.ZEPTO, myTolerance);

        tmpNumber = PrimitiveMath.POWER.invoke(tmpTen, -18);
        TestUtils.assertEquals("atto, a", tmpNumber, PrimitiveMath.Prefix.ATTO, myTolerance);

        tmpNumber = PrimitiveMath.POWER.invoke(tmpTen, -15);
        TestUtils.assertEquals("femto, f", tmpNumber, PrimitiveMath.Prefix.FEMTO, myTolerance);

        tmpNumber = PrimitiveMath.POWER.invoke(tmpTen, -12);
        TestUtils.assertEquals("pico, p", tmpNumber, PrimitiveMath.Prefix.PICO, myTolerance);

        tmpNumber = PrimitiveMath.POWER.invoke(tmpTen, -9);
        TestUtils.assertEquals("nano, n", tmpNumber, PrimitiveMath.Prefix.NANO, myTolerance);

        tmpNumber = PrimitiveMath.POWER.invoke(tmpTen, -6);
        TestUtils.assertEquals("micro, my", tmpNumber, PrimitiveMath.Prefix.MICRO, myTolerance);

        tmpNumber = PrimitiveMath.POWER.invoke(tmpTen, -3);
        TestUtils.assertEquals("milli, m", tmpNumber, PrimitiveMath.Prefix.MILLI, myTolerance);

        tmpNumber = PrimitiveMath.POWER.invoke(tmpTen, -2);
        TestUtils.assertEquals("centi, c", tmpNumber, PrimitiveMath.Prefix.CENTI, myTolerance);

        tmpNumber = PrimitiveMath.POWER.invoke(tmpTen, -1);
        TestUtils.assertEquals("deci, d", tmpNumber, PrimitiveMath.Prefix.DECI, myTolerance);

        tmpNumber = PrimitiveMath.POWER.invoke(tmpTen, 1);
        TestUtils.assertEquals("deka, da", tmpNumber, PrimitiveMath.Prefix.DEKA, myTolerance);

        tmpNumber = PrimitiveMath.POWER.invoke(tmpTen, 2);
        TestUtils.assertEquals("hecto, h", tmpNumber, PrimitiveMath.Prefix.HECTO, myTolerance);

        tmpNumber = PrimitiveMath.POWER.invoke(tmpTen, 3);
        TestUtils.assertEquals("kilo, k", tmpNumber, PrimitiveMath.Prefix.KILO, myTolerance);

        tmpNumber = PrimitiveMath.POWER.invoke(tmpTen, 6);
        TestUtils.assertEquals("mega, M", tmpNumber, PrimitiveMath.Prefix.MEGA, myTolerance);

        tmpNumber = PrimitiveMath.POWER.invoke(tmpTen, 9);
        TestUtils.assertEquals("giga, G", tmpNumber, PrimitiveMath.Prefix.GIGA, myTolerance);

        tmpNumber = PrimitiveMath.POWER.invoke(tmpTen, 12);
        TestUtils.assertEquals("tera, T", tmpNumber, PrimitiveMath.Prefix.TERA, myTolerance);

        tmpNumber = PrimitiveMath.POWER.invoke(tmpTen, 15);
        TestUtils.assertEquals("peta, P", tmpNumber, PrimitiveMath.Prefix.PETA, myTolerance);

        tmpNumber = PrimitiveMath.POWER.invoke(tmpTen, 18);
        TestUtils.assertEquals("exa, E", tmpNumber, PrimitiveMath.Prefix.EXA, myTolerance);

        tmpNumber = PrimitiveMath.POWER.invoke(tmpTen, 21);
        TestUtils.assertEquals("zetta, CmplxNmbr", tmpNumber, PrimitiveMath.Prefix.ZETTA, myTolerance);

        tmpNumber = PrimitiveMath.POWER.invoke(tmpTen, 24);
        TestUtils.assertEquals("yotta, Y", tmpNumber, PrimitiveMath.Prefix.YOTTA, myTolerance);
    }

}
