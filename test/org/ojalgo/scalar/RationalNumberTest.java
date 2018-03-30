package org.ojalgo.scalar;

import java.math.BigDecimal;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.ojalgo.TestUtils;
import org.ojalgo.constant.PrimitiveMath;

public class RationalNumberTest {

    private static RationalNumber rational2(final double d) {
        final boolean negative = d < 0;
        double g = Math.abs(d);

        long[] ds = new long[40]; // TODO: 4alf: Isn't it too big? Or too small?
        double error = 1.0;

        int i;
        for (i = 0; (i < ds.length) && (error > PrimitiveMath.MACHINE_EPSILON); i++) {
            if (g > Long.MAX_VALUE) {
                throw new ArithmeticException("Cannot fit a double into long!");
            }
            ds[i] = (long) Math.floor(g);
            double remainder = g - ds[i];
            g = 1.0 / remainder;
            error *= remainder;
        }
        i--;

        RationalNumber approximation = RationalNumber.valueOf(ds[i]);
        for (; --i >= 0;) {
            approximation = RationalNumber.valueOf(ds[i]).add(approximation.invert());
        }
        return negative ? approximation.negate() : approximation;
    }

    private final double myDiff = PrimitiveMath.MACHINE_EPSILON;

    @Test
    public void testInfinity() {
        TestUtils.assertTrue(Double.isInfinite(RationalNumber.POSITIVE_INFINITY.doubleValue()));
        TestUtils.assertTrue(RationalNumber.POSITIVE_INFINITY.doubleValue() > 0.0);
        TestUtils.assertTrue(Double.isInfinite(RationalNumber.NEGATIVE_INFINITY.doubleValue()));
        TestUtils.assertTrue(RationalNumber.NEGATIVE_INFINITY.doubleValue() < 0.0);
        TestUtils.assertTrue(RationalNumber.isInfinite(RationalNumber.POSITIVE_INFINITY.add(RationalNumber.POSITIVE_INFINITY)));
    }

    @Test
    public void testMultiplication() {
        RationalNumber a = RationalNumber.valueOf(0.04919653065050689);
        RationalNumber b = RationalNumber.valueOf(1.2325077080153841);

        TestUtils.assertEquals(a.multiply(b).doubleValue(), a.doubleValue() * b.doubleValue(), myDiff);
    }

    @Test
    public void testNaN() {
        TestUtils.assertEquals(Double.doubleToLongBits(RationalNumber.NaN.doubleValue()), Double.doubleToLongBits(Double.NaN));
        TestUtils.assertTrue(RationalNumber.isNaN(RationalNumber.NaN.add(RationalNumber.NaN)));
        TestUtils.assertTrue(RationalNumber.isNaN(RationalNumber.ONE.add(RationalNumber.NaN)));
        TestUtils.assertTrue(RationalNumber.isNaN(RationalNumber.NaN.add(RationalNumber.ONE)));
    }

    @Test
    public void testRational() {
        TestUtils.assertEquals(RationalNumber.of(1, 10), RationalNumber.rational(0.1));
    }

    @Test
    public void testRationals() {
        Random r = new Random();
        for (int i = 0; i < 100; i++) {
            double d = r.nextGaussian();
            TestUtils.assertEquals("Failing for " + d, RationalNumberTest.rational2(d), RationalNumber.rational(d));
        }
    }

    @ParameterizedTest(name = "#{index} valueOf({arguments})")
    @ValueSource(doubles = { 0.3, 0.25, 1e7, 5e8, -25.22e-4, 0.04919653065050689, 1.2325077080153841
            //                ,
            //                4223372036854775807.0,
            //                -4223372036854775808.0,
            //                9223372036854775807.0,
            //                -9223372036854775808.0
    })
    public void testValueOf(double d) {

        final RationalNumber direct = RationalNumber.valueOf(d);
        final RationalNumber approximation = RationalNumber.valueOf(d);
        final RationalNumber viaBigDecimal = RationalNumber.valueOf(BigDecimal.valueOf(d));

        double viaDirect = direct.doubleValue();
        TestUtils.assertEquals(d, viaDirect, myDiff);
        TestUtils.assertEquals(d, approximation.doubleValue(), myDiff);
        TestUtils.assertEquals(d >= 0.0, direct.isAbsolute());
        double expected = viaBigDecimal.doubleValue();
        TestUtils.assertEquals(expected, viaDirect, myDiff);
    }

    @ParameterizedTest(name = "#{index} valueOf(longBitsToDouble({arguments}))")
    @ValueSource(longs = {
            //s eeeeeeeeee mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
            0b0_1111111011_1001100110011001100110011001100110011001100110011010L, // 0.1
            0b0_1111110101_1111111111111111111111111111111111111111111111111111L, // * 2^{-62}
            0b0_1111110100_1111111111111111111111111111111111111111111111111111L, // * 2^{-63}
            0b0_1111110011_1111111111111111111111111111111111111111111111111111L, // * 2^{-64}
    })
    public void testValueOf(long l) {
        this.testValueOf(Double.longBitsToDouble(l));
    }

}
