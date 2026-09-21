/*
 * Copyright 1997-2026 Optimatika
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
package org.ojalgo.type.context;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.constant.PrimitiveMath;

/**
 * NumberContextTest
 *
 * @author apete
 */
public class NumberContextTest {

    @Test
    public void testComparePrecisionAndScaleBigDecimal() {

        for (int precision = 2; precision < 16; precision++) {
            int scale = precision - 1;

            NumberContext preciContext = NumberContext.ofPrecision(precision).withoutScale();
            NumberContext scaleContext = NumberContext.ofScale(scale).withoutPrecision();
            NumberContext accurContext = NumberContext.of(precision, scale);

            for (int f = 1; f <= 9; f++) {
                BigDecimal value = BigDecimal.valueOf(f + Math.random());
                // Guaranteed to be in the range [1.0 , 10)

                BigDecimal preciEnforced = preciContext.enforce(value);
                BigDecimal scaleEnforced = scaleContext.enforce(value);

                TestUtils.assertEquals(preciEnforced, scaleEnforced);

                BigDecimal preciResidual = value.subtract(preciEnforced);
                BigDecimal scaleResidual = value.subtract(scaleEnforced);

                TestUtils.assertEquals(preciResidual, scaleResidual);

                TestUtils.assertTrue(accurContext.isSmall(value, preciResidual));
                TestUtils.assertTrue(accurContext.isSmall(value, scaleResidual));

                TestUtils.assertTrue(scaleContext.isZero(preciResidual));
                TestUtils.assertTrue(scaleContext.isZero(scaleResidual));

                TestUtils.assertTrue(preciContext.isSmall(value, preciResidual));
                TestUtils.assertTrue(preciContext.isSmall(value, scaleResidual));

                BigDecimal trigger = BigMath.ONE.movePointLeft(scale).multiply(value);

                TestUtils.assertFalse(preciContext.isSmall(value, trigger));
            }
        }
    }

    @Test
    public void testComparePrecisionAndScalePrimitive() {

        for (int precision = 2; precision < 16; precision++) {
            int scale = precision - 1;

            NumberContext preciContext = NumberContext.ofPrecision(precision);
            NumberContext scaleContext = NumberContext.ofScale(scale);
            NumberContext accurContext = NumberContext.of(precision, scale);

            for (int f = 1; f <= 9; f++) {
                double value = f + Math.random();
                // Guaranteed to be in the range [1.0 , 10)

                double preciEnforced = preciContext.enforce(value);
                double scaleEnforced = scaleContext.enforce(value);

                TestUtils.assertEquals(preciEnforced, scaleEnforced);

                double preciResidual = value - preciEnforced;
                double scaleResidual = value - scaleEnforced;

                TestUtils.assertTrue(accurContext.isSmall(value, preciResidual));
                TestUtils.assertTrue(accurContext.isSmall(value, scaleResidual));

                // TestUtils.assertTrue(scaleContext.isZero(preciResidual));
                // TestUtils.assertTrue(scaleContext.isZero(scaleResidual));

                TestUtils.assertTrue(preciContext.isSmall(value, preciResidual));
                TestUtils.assertTrue(preciContext.isSmall(value, scaleResidual));

                double trigger = (BigMath.ONE.movePointLeft(scale).doubleValue() + PrimitiveMath.MACHINE_EPSILON) * value;

                TestUtils.assertFalse(preciContext.isSmall(value, trigger));
            }
        }
    }

    @Test
    public void testIsDifferentBigDecimalAvoidAbs() {

        NumberContext nc = NumberContext.of(7);

        BigDecimal a = new BigDecimal("500.00001");
        BigDecimal b = new BigDecimal("500.00002");
        BigDecimal c = new BigDecimal("200.0");

        TestUtils.assertFalse(nc.isDifferent(a, b));
        TestUtils.assertTrue(nc.isDifferent(a, c));

        TestUtils.assertFalse(nc.isDifferent(a.negate(), b.negate()));
        TestUtils.assertTrue(nc.isDifferent(a.negate(), c.negate()));

        TestUtils.assertTrue(nc.isDifferent(a, a.negate()));
    }

    @Test
    public void testIsDifferentBigDecimalDoubleConsistency() {

        NumberContext nc = NumberContext.of(10);

        double[] values = { 1.0, 100.0, 0.001, -50.0, 1E10 };

        for (double v : values) {
            BigDecimal bv = BigDecimal.valueOf(v);
            double nudge = v * 1E-11;
            BigDecimal bn = BigDecimal.valueOf(v + nudge);

            boolean bigResult = nc.isDifferent(bv, bn);
            boolean dblResult = nc.isDifferent(v, v + nudge);
            TestUtils.assertEquals("consistency at " + v, bigResult, dblResult);
        }
    }

    @Test
    public void testIsLessThanIsMoreThanBigDecimal() {

        NumberContext nc = NumberContext.of(7);

        BigDecimal a = new BigDecimal("100.0000001");
        BigDecimal b = new BigDecimal("100.0000002");
        BigDecimal c = new BigDecimal("101.0");

        TestUtils.assertFalse(nc.isLessThan(a, b));
        TestUtils.assertFalse(nc.isMoreThan(a, b));

        TestUtils.assertTrue(nc.isLessThan(c, a));
        TestUtils.assertTrue(nc.isMoreThan(a, c));
    }

    @Test
    public void testIsLessThanIsMoreThanConsistency() {

        NumberContext nc = NumberContext.of(10);

        double ref = 1000.0;
        double close = 1000.0 + 1E-8;
        double far = 1001.0;

        TestUtils.assertEquals(nc.isLessThan(BigDecimal.valueOf(ref), BigDecimal.valueOf(close)), nc.isLessThan(ref, close));
        TestUtils.assertEquals(nc.isMoreThan(BigDecimal.valueOf(ref), BigDecimal.valueOf(far)), nc.isMoreThan(ref, far));
    }

    @Test
    public void testIsLessThanIsMoreThanDouble() {

        NumberContext nc = NumberContext.of(7);

        TestUtils.assertFalse(nc.isLessThan(100.0, 100.0 - 1E-10));
        TestUtils.assertFalse(nc.isMoreThan(100.0, 100.0 + 1E-10));

        TestUtils.assertTrue(nc.isLessThan(100.0, 99.0));
        TestUtils.assertTrue(nc.isMoreThan(100.0, 101.0));

        TestUtils.assertFalse(nc.isLessThan(100.0, 100.0));
        TestUtils.assertFalse(nc.isMoreThan(100.0, 100.0));
    }

    @Test
    public void testIsSmall() {

        BigDecimal compareTo = BigDecimal.valueOf(2.03007518794);
        BigDecimal small = BigDecimal.valueOf(1E-12);

        TestUtils.assertTrue(NumberContext.of(12).isSmall(compareTo, small));
    }

    @Test
    public void testIsSmallBigDecimalDoubleConsistency() {

        NumberContext nc = NumberContext.of(7);

        double[] refs = { 1.0, 100.0, 1E6 };
        double[] vals = { 1E-10, 1E-5, 0.5 };

        for (double r : refs) {
            for (double v : vals) {
                boolean bigResult = nc.isSmall(BigDecimal.valueOf(r), BigDecimal.valueOf(v));
                boolean dblResult = nc.isSmall(r, v);
                TestUtils.assertEquals("ref=" + r + " val=" + v, bigResult, dblResult);
            }
        }
    }

    @Test
    public void testIsSmallBigDecimalFastPaths() {

        NumberContext nc = NumberContext.of(7);

        BigDecimal large = new BigDecimal("1000");
        BigDecimal tiny = new BigDecimal("1E-20");
        BigDecimal comparable = new BigDecimal("500");

        TestUtils.assertTrue(nc.isSmall(large, tiny));
        TestUtils.assertFalse(nc.isSmall(large, comparable));
        TestUtils.assertFalse(nc.isSmall(tiny, large));
    }

    @Test
    public void testIsSmallDoubleMultiplication() {

        NumberContext nc = NumberContext.of(7);

        TestUtils.assertTrue(nc.isSmall(1000.0, 1E-20));
        TestUtils.assertFalse(nc.isSmall(1000.0, 500.0));
        TestUtils.assertTrue(nc.isSmall(1.0, 1E-8));
        TestUtils.assertFalse(nc.isSmall(1.0, 1E-6));

        TestUtils.assertTrue(nc.isSmall(-1000.0, 1E-20));
        TestUtils.assertTrue(nc.isSmall(1000.0, -1E-20));
        TestUtils.assertFalse(nc.isSmall(-1000.0, 500.0));
    }

    @Test
    public void testIsZeroBigDecimalConsistency() {

        for (int ps = 1; ps <= 16; ps++) {
            NumberContext nc = NumberContext.of(ps);

            BigDecimal belowThreshold = BigDecimal.ONE.movePointLeft(ps + 2);
            BigDecimal atThreshold = new BigDecimal("5").movePointLeft(ps + 1);
            BigDecimal aboveThreshold = BigDecimal.ONE.movePointLeft(ps - 1);

            TestUtils.assertTrue(ps + ": below", nc.isZero(belowThreshold));
            TestUtils.assertFalse(ps + ": above", nc.isZero(aboveThreshold));
            TestUtils.assertEquals(ps + ": consistency", nc.isZero(belowThreshold), nc.isZero(belowThreshold.doubleValue()));
            TestUtils.assertEquals(ps + ": consistency", nc.isZero(aboveThreshold), nc.isZero(aboveThreshold.doubleValue()));
        }
    }

    @Test
    public void testIsZeroMagnitudeFastPaths() {

        NumberContext nc = NumberContext.of(7);

        TestUtils.assertFalse(nc.isZero(BigDecimal.ONE));
        TestUtils.assertFalse(nc.isZero(BigDecimal.TEN));
        TestUtils.assertFalse(nc.isZero(new BigDecimal("1000000")));

        TestUtils.assertTrue(nc.isZero(new BigDecimal("1E-9")));
        TestUtils.assertTrue(nc.isZero(new BigDecimal("1E-20")));

        TestUtils.assertFalse(nc.isZero(new BigDecimal("1E-7")));
    }

    @Test
    public void testIsZeroNoScale() {

        NumberContext nc = NumberContext.ofPrecision(7);

        TestUtils.assertTrue(nc.isZero(BigDecimal.ZERO));
        TestUtils.assertFalse(nc.isZero(new BigDecimal("1E-100")));
        TestUtils.assertFalse(nc.isZero(new BigDecimal("0.001")));
    }

    @Test
    public void testPercentContext() {

        NumberContext nc = NumberContext.getPercent(Locale.US);

        TestUtils.assertEquals(7, nc.getPrecision());
        TestUtils.assertEquals(4, nc.getScale());
        TestUtils.assertEquals(RoundingMode.HALF_EVEN, nc.getRoundingMode());

        String tmpOriginalString = "123.4567";
        BigDecimal tmpBD = new BigDecimal(tmpOriginalString);
        TestUtils.assertEquals(tmpOriginalString, tmpBD.toPlainString());

        TestUtils.assertEquals("12,345.67%", nc.format(tmpBD));
    }

    @Test
    public void testPrecision() {

        NumberContext nc = NumberContext.of(3);

        double upper = 0.0004999;
        double lower = -0.0004999;

        TestUtils.assertTrue(nc.isZero(upper));
        TestUtils.assertTrue(nc.isZero(lower));

        TestUtils.assertFalse(nc.isDifferent(0.1 + lower, 0.1 + upper));
        TestUtils.assertFalse(nc.isDifferent(0.1 + upper, 0.1 + lower));

        upper = 0.004999;
        lower = -0.004999;

        TestUtils.assertFalse(nc.isZero(upper));
        TestUtils.assertFalse(nc.isZero(lower));

        TestUtils.assertTrue(nc.isDifferent(0.1 + lower, 0.1 + upper));
        TestUtils.assertTrue(nc.isDifferent(0.1 + upper, 0.1 + lower));

        upper = 0.00004999;
        lower = -0.00004999;

        TestUtils.assertTrue(nc.isZero(upper));
        TestUtils.assertTrue(nc.isZero(lower));

        TestUtils.assertFalse(nc.isDifferent(0.1 + lower, 0.1 + upper));
        TestUtils.assertFalse(nc.isDifferent(0.1 + upper, 0.1 + lower));

        upper = 555 + 0.5;
        lower = 555 - 0.5;

        TestUtils.assertFalse(nc.isDifferent(lower, upper));
        TestUtils.assertFalse(nc.isDifferent(upper, lower));

        upper = 100 + 1;
        lower = 100 - 1;

        TestUtils.assertTrue(nc.isDifferent(lower, upper));
        TestUtils.assertTrue(nc.isDifferent(upper, lower));

        upper = 999 + 1;
        lower = 999 - 1;

        TestUtils.assertFalse(nc.isDifferent(lower, upper));
        TestUtils.assertFalse(nc.isDifferent(upper, lower));
    }

    @Test
    public void testPrecisionAndScaleMutations() {

        NumberContext nc = NumberContext.of(12, 14);

        NumberContext incrementedPrecision = nc.withIncrementedPrecision();
        TestUtils.assertEquals(13, incrementedPrecision.getPrecision());
        TestUtils.assertEquals(14, incrementedPrecision.getScale());

        NumberContext incrementedScale = nc.withIncrementedScale();
        TestUtils.assertEquals(12, incrementedScale.getPrecision());
        TestUtils.assertEquals(15, incrementedScale.getScale());

        NumberContext decrementedPrecision = nc.withDecrementedPrecision();
        TestUtils.assertEquals(11, decrementedPrecision.getPrecision());
        TestUtils.assertEquals(14, decrementedPrecision.getScale());

        NumberContext decrementedScale = nc.withDecrementedScale();
        TestUtils.assertEquals(12, decrementedScale.getPrecision());
        TestUtils.assertEquals(13, decrementedScale.getScale());

        NumberContext doubledPrecision = nc.withDoubledPrecision();
        TestUtils.assertEquals(24, doubledPrecision.getPrecision());
        TestUtils.assertEquals(14, doubledPrecision.getScale());

        NumberContext doubledScale = nc.withDoubledScale();
        TestUtils.assertEquals(12, doubledScale.getPrecision());
        TestUtils.assertEquals(28, doubledScale.getScale());

        NumberContext halvedPrecision = nc.withHalvedPrecision();
        TestUtils.assertEquals(6, halvedPrecision.getPrecision());
        TestUtils.assertEquals(14, halvedPrecision.getScale());

        NumberContext halvedScale = nc.withHalvedScale();
        TestUtils.assertEquals(12, halvedScale.getPrecision());
        TestUtils.assertEquals(7, halvedScale.getScale());
    }

}
