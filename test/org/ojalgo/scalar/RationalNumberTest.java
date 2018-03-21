package org.ojalgo.scalar;

import org.junit.Test;
import org.ojalgo.constant.PrimitiveMath;

import java.math.BigDecimal;

import static java.lang.Double.longBitsToDouble;
import static org.ojalgo.TestUtils.assertEquals;
import static org.ojalgo.TestUtils.assertTrue;

public class RationalNumberTest {

    private final double myDiff = PrimitiveMath.MACHINE_EPSILON;

    @Test
    public void testValueOf() {

        double test_values[] = {
                //                 s eeeeeeeeee mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
                longBitsToDouble(0b0_1111111011_1001100110011001100110011001100110011001100110011010L), // 0.1
                longBitsToDouble(0b0_1111110101_1111111111111111111111111111111111111111111111111111L), // * 2^{-62}
                longBitsToDouble(0b0_1111110100_1111111111111111111111111111111111111111111111111111L), // * 2^{-63}
                longBitsToDouble(0b0_1111110011_1111111111111111111111111111111111111111111111111111L), // * 2^{-64}
                0.3,
                0.25,
                1e7,
                5e8,
                -25.22e-4,
                0.04919653065050689,
                1.2325077080153841
//                ,
//                4223372036854775807.0,
//                -4223372036854775808.0,
//                9223372036854775807.0,
//                -9223372036854775808.0
        };

        for (double d : test_values) {
            final RationalNumber direct = RationalNumber.valueOf(d);
            final RationalNumber viaBigDecimal = RationalNumber.valueOf(BigDecimal.valueOf(d));

            double viaDirect = direct.doubleValue();
            assertEquals(d, viaDirect, myDiff);
            double expected = viaBigDecimal.doubleValue();
            assertEquals(expected, viaDirect, myDiff);
        }
    }

    @Test
    public void testMultiplication() {
        RationalNumber a = RationalNumber.valueOf(0.04919653065050689);
        RationalNumber b = RationalNumber.valueOf(1.2325077080153841);

        assertEquals(a.multiply(b).doubleValue(), a.doubleValue() * b.doubleValue(), myDiff);
    }

    @Test
    public void testNaN() {
        assertEquals(Double.doubleToLongBits(RationalNumber.NaN.doubleValue()),
                Double.doubleToLongBits(Double.NaN));
        assertTrue(RationalNumber.isNaN(RationalNumber.NaN.add(RationalNumber.NaN)));
        assertTrue(RationalNumber.isNaN(RationalNumber.ONE.add(RationalNumber.NaN)));
        assertTrue(RationalNumber.isNaN(RationalNumber.NaN.add(RationalNumber.ONE)));
    }

    @Test
    public void testInfinity() {
        assertTrue(Double.isInfinite(RationalNumber.POSITIVE_INFINITY.doubleValue()));
        assertTrue(RationalNumber.POSITIVE_INFINITY.doubleValue() > 0.0);
        assertTrue(Double.isInfinite(RationalNumber.NEGATIVE_INFINITY.doubleValue()));
        assertTrue(RationalNumber.NEGATIVE_INFINITY.doubleValue() < 0.0);
        assertTrue(RationalNumber.isInfinite(RationalNumber.POSITIVE_INFINITY.add(RationalNumber.POSITIVE_INFINITY)));
    }
}
