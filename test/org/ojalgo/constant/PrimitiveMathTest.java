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
import org.ojalgo.type.context.NumberContext;

public class PrimitiveMathTest {

    private static void compare(final double arg0, final double arg1) {

        if (arg0 == arg1) {
            if (NumberContext.compare(arg0, arg1) != 0) {
                TestUtils.fail();
            }
        } else {
            //noinspection ResultOfMethodCallIgnored
            NumberContext.compare(arg0, arg1);
            TestUtils.fail();
        }

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
    public void testPowersOf2() {

        long tmpPrev = PrimitiveMath.POWERS_OF_2[0];
        TestUtils.assertEquals(1L, tmpPrev);

        for (int i = 1; i < PrimitiveMath.POWERS_OF_2.length; i++) {
            final long tmpVal = PrimitiveMath.POWERS_OF_2[i];

            TestUtils.assertTrue(tmpPrev < tmpVal);
            TestUtils.assertEquals(2, tmpVal / tmpPrev);

            tmpPrev = tmpVal;
        }

    }

}
