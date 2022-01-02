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
package org.ojalgo.function.special;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.StandardType;

public class BetaFunctionTest {

    @Test
    public void testCompareImplementations() {

        int lim = 50;
        for (int a = 1; a < lim; a++) {
            for (int b = 1; b < lim; b++) {

                double intResult = BetaFunction.beta(a, b);
                double doubleResult = BetaFunction.beta((double) a, (double) b);
                ComplexNumber complexResult = BetaFunction.beta(ComplexNumber.valueOf(a), ComplexNumber.valueOf(b));

                TestUtils.assertEquals(intResult, doubleResult);
                TestUtils.assertEquals(intResult, complexResult);
            }
        }
    }

    @Test
    public void testIdentities() {

        int lim = 50;
        for (int a = 1; a < lim; a++) {
            for (int b = 1; b < lim; b++) {
                TestUtils.assertEquals(BetaFunction.beta(a, b), BetaFunction.beta(a + 1, b) + BetaFunction.beta(a, b + 1));
                TestUtils.assertEquals(BetaFunction.beta(a + 1, b), (BetaFunction.beta(a, b) * a) / (a + b));
                TestUtils.assertEquals(BetaFunction.beta(a, b + 1), (BetaFunction.beta(a, b) * b) / (a + b));
            }
        }

    }

    @Test
    public void testIncompleteReducesToTheUsualBetaFunction() {
        int lim = 15;
        for (int a = 1; a < lim; a++) {
            for (int b = 1; b < lim; b++) {
                TestUtils.assertEquals("a=" + a + ", b=" + b, BetaFunction.beta(a, b), BetaFunction.Incomplete.beta(ONE, a, b), StandardType.MATH_032);
            }
        }
    }

    @Test
    public void testIncompleteZeroIntegral() {
        int lim = 50;
        for (int a = 1; a < lim; a++) {
            for (int b = 1; b < lim; b++) {
                TestUtils.assertEquals("a=" + a + ", b=" + b, ZERO, BetaFunction.Incomplete.beta(ZERO, a, b));
            }
        }
    }

    @Test
    public void testRegularizedReducesToTheUsualBetaFunction() {
        int lim = 15;
        for (int a = 1; a < lim; a++) {
            for (int b = 1; b < lim; b++) {
                TestUtils.assertEquals("a=" + a + ", b=" + b, ONE, BetaFunction.Regularized.beta(ONE, a, b), StandardType.MATH_032);
            }
        }
    }

}
