package org.ojalgo.scalar;

import org.ojalgo.FunctionalityTest;
import org.ojalgo.TestUtils;
import org.ojalgo.constant.PrimitiveMath;

import java.math.BigDecimal;

public class RationalNumberTest extends FunctionalityTest {

    private final double myDiff = PrimitiveMath.MACHINE_EPSILON;

    public void testValueOf() {

        double test_values[] = {
                0.1,
                0.3,
                0.25,
                1e7,
                5e8,
                -25.22e-4
//                ,
//                4223372036854775807.0,
//                -4223372036854775808.0,
//                9223372036854775807.0,
//                -9223372036854775808.0
        };

        for (double d : test_values) {
            final RationalNumber direct = RationalNumber.valueOf(d);
            final RationalNumber viaBigDecimal = RationalNumber.valueOf(BigDecimal.valueOf(d));

            TestUtils.assertEquals(viaBigDecimal.doubleValue(), direct.doubleValue(), myDiff);
        }
    }
}
