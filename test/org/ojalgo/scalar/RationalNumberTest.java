package org.ojalgo.scalar;

import java.math.BigDecimal;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.PrimitiveMath;

public class RationalNumberTest extends ScalarTests {

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
    public void testAdd() {

        final RationalNumber tmpVal1 = RationalNumber.valueOf(1.25);
        final RationalNumber tmpVal2 = RationalNumber.valueOf(3.75);

        double myExp = 5.0;
        double myAct = tmpVal1.add(tmpVal2).doubleValue();

        TestUtils.assertEquals(myExp, myAct, PrimitiveMath.MACHINE_EPSILON);
    }

    @Test
    public void testDivide() {

        final RationalNumber tmpVal1 = RationalNumber.valueOf(1.25);
        final RationalNumber tmpVal2 = RationalNumber.valueOf(0.25);

        double myExp = 5.0;
        double myAct = tmpVal1.divide(tmpVal2).doubleValue();

        TestUtils.assertEquals(myExp, myAct, PrimitiveMath.MACHINE_EPSILON);
    }

    @Test
    public void testInfinity() {
        TestUtils.assertTrue(Double.isInfinite(RationalNumber.POSITIVE_INFINITY.doubleValue()));
        TestUtils.assertTrue(RationalNumber.POSITIVE_INFINITY.doubleValue() > 0.0);
        TestUtils.assertTrue(Double.isInfinite(RationalNumber.NEGATIVE_INFINITY.doubleValue()));
        TestUtils.assertTrue(RationalNumber.NEGATIVE_INFINITY.doubleValue() < 0.0);
        TestUtils.assertTrue(RationalNumber.isInfinite(RationalNumber.POSITIVE_INFINITY.add(RationalNumber.POSITIVE_INFINITY)));
    }

    @Test
    public void testInvert() {

        final RationalNumber tmpVal1 = RationalNumber.valueOf(1.25);

        double myExp = 0.8;
        double myAct = tmpVal1.invert().doubleValue();

        TestUtils.assertEquals(myExp, myAct, PrimitiveMath.MACHINE_EPSILON);
    }

    @Test
    public void testMultiplication() {
        RationalNumber a = RationalNumber.valueOf(0.04919653065050689);
        RationalNumber b = RationalNumber.valueOf(1.2325077080153841);

        TestUtils.assertEquals(a.multiply(b).doubleValue(), a.doubleValue() * b.doubleValue(), myDiff);
    }

    @Test
    public void testMultiply() {

        final RationalNumber tmpVal1 = RationalNumber.valueOf(1.25);
        final RationalNumber tmpVal2 = RationalNumber.valueOf(4);

        double myExp = 5.0;
        double myAct = tmpVal1.multiply(tmpVal2).doubleValue();

        TestUtils.assertEquals(myExp, myAct, PrimitiveMath.MACHINE_EPSILON);
    }

    @Test
    public void testNaN() {
        TestUtils.assertEquals(Double.doubleToLongBits(RationalNumber.NaN.doubleValue()), Double.doubleToLongBits(Double.NaN));
        TestUtils.assertTrue(RationalNumber.isNaN(RationalNumber.NaN.add(RationalNumber.NaN)));
        TestUtils.assertTrue(RationalNumber.isNaN(RationalNumber.ONE.add(RationalNumber.NaN)));
        TestUtils.assertTrue(RationalNumber.isNaN(RationalNumber.NaN.add(RationalNumber.ONE)));
    }

    @Test
    public void testNegate() {

        final RationalNumber tmpVal1 = RationalNumber.valueOf(1.25);

        double myExp = -1.25;
        double myAct = tmpVal1.negate().doubleValue();

        TestUtils.assertEquals(myExp, myAct, PrimitiveMath.MACHINE_EPSILON);
    }

    @Test
    public void testRational() {
        TestUtils.assertEquals(RationalNumber.of(1, 10), RationalNumber.rational(0.1));
    }

    @Test
    public void testRationalNumber() {

        final RationalNumber tmpRat1 = RationalNumber.of(5, 1);
        final RationalNumber tmpRat2 = RationalNumber.of(2, 1);

        for (int i = 0; i < 100; i++) {
            tmpRat1.add(tmpRat2);
            tmpRat1.multiply(tmpRat2);
            tmpRat1.subtract(tmpRat2);
            tmpRat1.divide(tmpRat2);
        }
    }

    @Test
    public void testRationals() {
        Random r = new Random();
        for (int i = 0; i < 100; i++) {
            double d = r.nextGaussian();
            TestUtils.assertEquals("Failing for " + d, RationalNumberTest.rational2(d), RationalNumber.rational(d));
        }
    }

    @Test
    public void testSubtract() {

        final RationalNumber tmpVal1 = RationalNumber.valueOf(1.25);
        final RationalNumber tmpVal2 = RationalNumber.valueOf(-3.75);

        double myExp = 5.0;
        double myAct = tmpVal1.subtract(tmpVal2).doubleValue();

        TestUtils.assertEquals(myExp, myAct, PrimitiveMath.MACHINE_EPSILON);
    }

    @ParameterizedTest(name = "#{index} valueOf({arguments})")
    @ValueSource(doubles = { 0.3, 0.25, 1e7, 5e8, -25.22e-4, 0.04919653065050689, 1.2325077080153841
            //                ,
            //                4223372036854775807.0,
            //                -4223372036854775808.0,
            //                9223372036854775807.0,
            //                -9223372036854775808.0
    })
    public void testValueOf(final double d) {

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
    public void testValueOf(final long l) {
        this.testValueOf(Double.longBitsToDouble(l));
    }

}
