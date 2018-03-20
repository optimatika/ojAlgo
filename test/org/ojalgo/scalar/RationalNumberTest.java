package org.ojalgo.scalar;

import org.ojalgo.FunctionalityTest;
import org.ojalgo.TestUtils;
import org.ojalgo.constant.PrimitiveMath;

import java.math.BigDecimal;

import static java.lang.Double.longBitsToDouble;

public class RationalNumberTest extends FunctionalityTest {

    private final double myDiff = PrimitiveMath.MACHINE_EPSILON;

    public void testValueOf() {

        double test_values[] = {
                //                 seeeeeeeeeemmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
                longBitsToDouble(0b011111110111001100110011001100110011001100110011001100110011010L), // 0.1
                longBitsToDouble(0b011111101011111111111111111111111111111111111111111111111111111L), // * 2^{-62}
                longBitsToDouble(0b011111101001111111111111111111111111111111111111111111111111111L), // * 2^{-63}
                longBitsToDouble(0b011111100111111111111111111111111111111111111111111111111111111L), // * 2^{-64}
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
            TestUtils.assertEquals(d, viaDirect, myDiff);
            double expected = viaBigDecimal.doubleValue();
            TestUtils.assertEquals(expected, viaDirect, myDiff);
        }
    }

    public void testMultiplication() {
        RationalNumber a = RationalNumber.valueOf(0.04919653065050689);
        RationalNumber b = RationalNumber.valueOf(1.2325077080153841);

        TestUtils.assertEquals(a.multiply(b).doubleValue(), a.doubleValue() * b.doubleValue(), myDiff);
    }

    public void testNaN() {
        assertEquals(Double.doubleToLongBits(RationalNumber.NaN.doubleValue()),
                Double.doubleToLongBits(Double.NaN));
        assertTrue(RationalNumber.isNaN(RationalNumber.NaN.add(RationalNumber.NaN)));
        assertTrue(RationalNumber.isNaN(RationalNumber.ONE.add(RationalNumber.NaN)));
        assertTrue(RationalNumber.isNaN(RationalNumber.NaN.add(RationalNumber.ONE)));
    }

    public void testInfinity() {
        assertEquals(RationalNumber.POSITIVE_INFINITY.doubleValue(), Double.POSITIVE_INFINITY);
        assertEquals(RationalNumber.NEGATIVE_INFINITY.doubleValue(), Double.NEGATIVE_INFINITY);
        assertTrue(RationalNumber.isInfinite(RationalNumber.POSITIVE_INFINITY.add(RationalNumber.POSITIVE_INFINITY)));
    }
}
