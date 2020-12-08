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
package org.ojalgo.core.function.special;

import static org.ojalgo.core.function.constant.PrimitiveMath.*;

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.ojalgo.core.TestUtils;

public class HypergeometricFunctionTest {

    private static final Random RANDOM = new Random();

    @Test
    public void testKnownValues() {

        // http://mathworld.wolfram.com/HypergeometricFunction.html

        // M. Trott, pers. comm., Aug. 5, 2002; Zucker and Joyce 2001
        TestUtils.assertEquals(EIGHT / FIVE, HypergeometricFunction.hypergeometric(THIRD, TWO_THIRDS, FIVE / SIX, 27.0 / 32.0));
        TestUtils.assertEquals(NINE / FIVE, HypergeometricFunction.hypergeometric(QUARTER, HALF, THREE_QUARTERS, 80.0 / 81.0));

        // Zucker and Joyce 2001
        TestUtils.assertEquals(Math.sqrt(SEVEN) * TWO_THIRDS, HypergeometricFunction.hypergeometric(EIGHTH, THREE / EIGHT, HALF, 2400.0 / 2401.0));
        TestUtils.assertEquals(Math.sqrt(THREE) * THREE_QUARTERS, HypergeometricFunction.hypergeometric(SIXTH, THIRD, HALF, 25.0 / 27.0));

        // Zucker and Joyce 2001, 2003
        TestUtils.assertEquals((Math.pow(TWO, SIXTH) * FOUR) / THREE, HypergeometricFunction.hypergeometric(SIXTH, HALF, TWO_THIRDS, 125.0 / 128.0));
        TestUtils.assertEquals((Math.pow(ELEVEN, QUARTER) * THREE) / FOUR,
                HypergeometricFunction.hypergeometric(TWELFTH, FIVE / TWELVE, HALF, 1323.0 / 1331.0));

    }

    @Test
    public void testRepresentationAsinX() {
        RANDOM.doubles(10, -1, 1).forEach(x -> {
            TestUtils.assertEquals(Math.asin(x), x * HypergeometricFunction.hypergeometric(HALF, HALF, THREE / TWO, x * x));
        });
    }

    @Test
    public void testRepresentationLogOnePlusX() {
        RANDOM.doubles(10).forEach(x -> {
            TestUtils.assertEquals(Math.log(ONE + x), x * HypergeometricFunction.hypergeometric(ONE, ONE, TWO, -x));
        });
    }

    @Test
    public void testSpecialArguments111() {
        RANDOM.doubles(10).forEach(x -> {
            TestUtils.assertEquals(ONE / (ONE - x), HypergeometricFunction.hypergeometric(ONE, ONE, ONE, x));
        });
    }

    @Test
    public void testSpecialArguments112() {
        RANDOM.doubles(10).forEach(x -> {
            TestUtils.assertEquals(-Math.log(ONE - x) / x, HypergeometricFunction.hypergeometric(ONE, ONE, TWO, x));
        });
    }

    @Test
    public void testSpecialArguments121() {
        RANDOM.doubles(10).forEach(x -> {
            TestUtils.assertEquals(ONE / ((ONE - x) * (ONE - x)), HypergeometricFunction.hypergeometric(ONE, TWO, ONE, x));
        });

    }

    @Test
    public void testSpecialArguments122() {
        RANDOM.doubles(10).forEach(x -> {
            TestUtils.assertEquals(ONE / (ONE - x), HypergeometricFunction.hypergeometric(ONE, TWO, TWO, x));
        });
    }

}
