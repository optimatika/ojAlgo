/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
package org.ojalgo.scalar;

import java.math.BigDecimal;

import org.ojalgo.TestUtils;
import org.ojalgo.constant.PrimitiveMath;

/**
 * ScientificNumberTest
 *
 * @author apete
 */
public class RationalScalarTest extends ScalarTests {

    private double myAct;
    private final double myDiff = PrimitiveMath.MACHINE_EPSILON;
    private double myExp;

    public void testAdd() {

        final RationalNumber tmpVal1 = RationalNumber.valueOf(1.25);
        final RationalNumber tmpVal2 = RationalNumber.valueOf(3.75);

        myExp = 5.0;

        myAct = tmpVal1.add(tmpVal2).doubleValue();

        TestUtils.assertEquals(myExp, myAct, myDiff);
    }

    public void testBigDecimal() {

        final BigDecimal tmpBig1 = new BigDecimal(5.0);
        final BigDecimal tmpBig2 = new BigDecimal(2.0);

        for (int i = 0; i < 100; i++) {
            tmpBig1.add(tmpBig2);
            tmpBig1.multiply(tmpBig2);
            tmpBig1.subtract(tmpBig2);
            tmpBig1.divide(tmpBig2, BigDecimal.ROUND_HALF_EVEN);
        }
    }

    public void testDivide() {

        final RationalNumber tmpVal1 = RationalNumber.valueOf(1.25);
        final RationalNumber tmpVal2 = RationalNumber.valueOf(0.25);

        myExp = 5.0;

        myAct = tmpVal1.divide(tmpVal2).doubleValue();

        TestUtils.assertEquals(myExp, myAct, myDiff);
    }

    public void testInvert() {

        final RationalNumber tmpVal1 = RationalNumber.valueOf(1.25);

        myExp = 0.8;

        myAct = tmpVal1.invert().doubleValue();

        TestUtils.assertEquals(myExp, myAct, myDiff);
    }

    public void testMultiply() {

        final RationalNumber tmpVal1 = RationalNumber.valueOf(1.25);
        final RationalNumber tmpVal2 = RationalNumber.valueOf(4);

        myExp = 5.0;

        myAct = tmpVal1.multiply(tmpVal2).doubleValue();

        TestUtils.assertEquals(myExp, myAct, myDiff);
    }

    public void testNegate() {

        final RationalNumber tmpVal1 = RationalNumber.valueOf(1.25);

        myExp = -1.25;

        myAct = tmpVal1.negate().doubleValue();

        TestUtils.assertEquals(myExp, myAct, myDiff);
    }

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

    public void testSubtract() {

        final RationalNumber tmpVal1 = RationalNumber.valueOf(1.25);
        final RationalNumber tmpVal2 = RationalNumber.valueOf(-3.75);

        myExp = 5.0;

        myAct = tmpVal1.subtract(tmpVal2).doubleValue();

        TestUtils.assertEquals(myExp, myAct, myDiff);
    }

}
