/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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

import org.ojalgo.TestUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.random.Uniform;

/**
 * @author apete
 */
public class FundamentalScalarTest extends ScalarTests {

    private static final Uniform UNIFORM = new Uniform(0, 10);

    BigScalar big1;
    BigScalar big2;
    ComplexNumber complex1;
    ComplexNumber complex2;
    PrimitiveScalar primitive1;
    PrimitiveScalar primitive2;
    Quaternion quaternion1;
    Quaternion quaternion2;
    RationalNumber rational1;
    RationalNumber rational2;
    double value1;
    double value2;

    public void testAdd() {

        final double tmpExp = value1 + value2;

        final BigScalar tmpBig = big1.add(big2.getNumber());
        final ComplexNumber tmpComplex = complex1.add(complex2.getNumber());
        final PrimitiveScalar tmpPrimitive = primitive1.add(primitive2.getNumber());
        final Quaternion tmpQuaternion = quaternion1.add(quaternion2.getNumber());
        final RationalNumber tmpRational = rational1.add(rational2.getNumber());

        this.assertEqual(tmpExp, tmpBig, tmpComplex, tmpPrimitive, tmpQuaternion, tmpRational, PrimitiveMath.IS_ZERO);
    }

    public void testConjugate() {

        final double tmpExp = value1;

        final BigScalar tmpBig = big1.conjugate();
        final ComplexNumber tmpComplex = complex1.conjugate();
        final PrimitiveScalar tmpPrimitive = primitive1.conjugate();
        final Quaternion tmpQuaternion = quaternion1.conjugate();
        final RationalNumber tmpRational = rational1.conjugate();

        this.assertEqual(tmpExp, tmpBig, tmpComplex, tmpPrimitive, tmpQuaternion, tmpRational, PrimitiveMath.IS_ZERO);
    }

    public void testDivide() {

        final double tmpExp = value1 / value2;

        final BigScalar tmpBig = big1.divide(big2.getNumber());
        final ComplexNumber tmpComplex = complex1.divide(complex2.getNumber());
        final PrimitiveScalar tmpPrimitive = primitive1.divide(primitive2.getNumber());
        final Quaternion tmpQuaternion = quaternion1.divide(quaternion2.getNumber());
        final RationalNumber tmpRational = rational1.divide(rational2.getNumber());

        this.assertEqual(tmpExp, tmpBig, tmpComplex, tmpPrimitive, tmpQuaternion, tmpRational, PrimitiveMath.IS_ZERO);
    }

    public void testInvert() {

        final double tmpExp = 1.0 / value1;

        final BigScalar tmpBig = big1.invert();
        final ComplexNumber tmpComplex = complex1.invert();
        final PrimitiveScalar tmpPrimitive = primitive1.invert();
        final Quaternion tmpQuaternion = quaternion1.invert();
        final RationalNumber tmpRational = rational1.invert();

        this.assertEqual(tmpExp, tmpBig, tmpComplex, tmpPrimitive, tmpQuaternion, tmpRational, PrimitiveMath.IS_ZERO);
    }

    public void testMultiply() {

        final double tmpExp = value1 * value2;

        final BigScalar tmpBig = big1.multiply(big2.getNumber());
        final ComplexNumber tmpComplex = complex1.multiply(complex2.getNumber());
        final PrimitiveScalar tmpPrimitive = primitive1.multiply(primitive2.getNumber());
        final Quaternion tmpQuaternion = quaternion1.multiply(quaternion2.getNumber());
        final RationalNumber tmpRational = rational1.multiply(rational2.getNumber());

        this.assertEqual(tmpExp, tmpBig, tmpComplex, tmpPrimitive, tmpQuaternion, tmpRational, PrimitiveMath.IS_ZERO);
    }

    public void testNegate() {

        final double tmpExp = -value1;

        final BigScalar tmpBig = big1.negate();
        final ComplexNumber tmpComplex = complex1.negate();
        final PrimitiveScalar tmpPrimitive = primitive1.negate();
        final Quaternion tmpQuaternion = quaternion1.negate();
        final RationalNumber tmpRational = rational1.negate();

        this.assertEqual(tmpExp, tmpBig, tmpComplex, tmpPrimitive, tmpQuaternion, tmpRational, PrimitiveMath.IS_ZERO);
    }

    public void testSubtract() {

        final double tmpExp = value1 - value2;

        final BigScalar tmpBig = big1.subtract(big2.getNumber());
        final ComplexNumber tmpComplex = complex1.subtract(complex2.getNumber());
        final PrimitiveScalar tmpPrimitive = primitive1.subtract(primitive2.getNumber());
        final Quaternion tmpQuaternion = quaternion1.subtract(quaternion2.getNumber());
        final RationalNumber tmpRational = rational1.subtract(rational2.getNumber());

        this.assertEqual(tmpExp, tmpBig, tmpComplex, tmpPrimitive, tmpQuaternion, tmpRational, PrimitiveMath.IS_ZERO);
    }

    @Override
    protected void setUp() throws Exception {

        super.setUp();

        value1 = this.makeRandom();
        big1 = new BigScalar(value1);
        complex1 = ComplexNumber.makeReal(value1);
        primitive1 = new PrimitiveScalar(value1);
        quaternion1 = new Quaternion(value1);
        rational1 = new RationalNumber(value1);

        value2 = this.makeRandom();
        big2 = new BigScalar(value2);
        complex2 = ComplexNumber.makeReal(value2);
        primitive2 = new PrimitiveScalar(value2);
        quaternion2 = new Quaternion(value2);
        rational2 = new RationalNumber(value2);
    }

    void assertEqual(final double expected, final BigScalar big, final ComplexNumber complex, final PrimitiveScalar primitive, final Quaternion quaternion,
            final RationalNumber rational, final double precision) {
        TestUtils.assertEquals("Big", 1.0, big.doubleValue() / expected, precision);
        TestUtils.assertEquals("Complex", 1.0, complex.doubleValue() / expected, precision);
        TestUtils.assertEquals("Primitive", 1.0, primitive.doubleValue() / expected, precision);
        TestUtils.assertEquals("Quaternion", 1.0, quaternion.doubleValue() / expected, precision);
        TestUtils.assertEquals("Rational", 1.0, rational.doubleValue() / expected, precision);
    }

    double makeRandom() {

        final double tmpbase = UNIFORM.doubleValue();
        final double tmpExp = UNIFORM.doubleValue();

        if (tmpbase > tmpExp) {
            return Math.pow(tmpbase, tmpExp);
        } else {
            return -Math.pow(tmpbase, tmpExp);
        }
    }

}
