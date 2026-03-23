/*
 * Copyright 1997-2025 Optimatika
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.random.Uniform;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class FundamentalScalarTest extends ScalarTests {

    private static final Uniform UNIFORM = new Uniform(0, 4);

    static NumberContext CONTEXT = NumberContext.of(12, 14);

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

    @BeforeEach
    public void setUp() {
        value1 = this.makeRandom();
        big1 = BigScalar.valueOf(value1);
        complex1 = ComplexNumber.valueOf(value1);
        primitive1 = PrimitiveScalar.of(value1);
        quaternion1 = Quaternion.valueOf(value1);
        rational1 = RationalNumber.valueOf(value1);

        value2 = this.makeRandom();
        big2 = BigScalar.valueOf(value2);
        complex2 = ComplexNumber.valueOf(value2);
        primitive2 = PrimitiveScalar.of(value2);
        quaternion2 = Quaternion.valueOf(value2);
        rational2 = RationalNumber.valueOf(value2);
    }

    @Test
    @Tag("unstable")
    public void testAdd() {

        final double tmpExp = value1 + value2;

        BigScalar tmpBig = big1.add(big2.get());
        ComplexNumber tmpComplex = complex1.add(complex2.get());
        PrimitiveScalar tmpPrimitive = primitive1.add(primitive2.get());
        Quaternion tmpQuaternion = quaternion1.add(quaternion2.get());
        RationalNumber tmpRational = rational1.add(rational2.get());

        this.assertEqual(tmpExp, tmpBig, tmpComplex, tmpPrimitive, tmpQuaternion, tmpRational);

        tmpBig = big1.add(value2);
        tmpComplex = complex1.add(value2);
        tmpPrimitive = primitive1.add(value2);
        tmpQuaternion = quaternion1.add(value2);
        tmpRational = rational1.add(value2);

        this.assertEqual(tmpExp, tmpBig, tmpComplex, tmpPrimitive, tmpQuaternion, tmpRational);
    }

    @Test
    public void testConjugate() {

        final double tmpExp = value1;

        final BigScalar tmpBig = big1.conjugate();
        final ComplexNumber tmpComplex = complex1.conjugate();
        final PrimitiveScalar tmpPrimitive = primitive1.conjugate();
        final Quaternion tmpQuaternion = quaternion1.conjugate();
        final RationalNumber tmpRational = rational1.conjugate();

        this.assertEqual(tmpExp, tmpBig, tmpComplex, tmpPrimitive, tmpQuaternion, tmpRational);
    }

    @Test
    @Tag("unstable")
    public void testDivide() {

        final double tmpExp = value1 / value2;

        BigScalar tmpBig = big1.divide(big2.get());
        ComplexNumber tmpComplex = complex1.divide(complex2.get());
        PrimitiveScalar tmpPrimitive = primitive1.divide(primitive2.get());
        Quaternion tmpQuaternion = quaternion1.divide(quaternion2.get());
        RationalNumber tmpRational = rational1.divide(rational2.get());

        this.assertEqual(tmpExp, tmpBig, tmpComplex, tmpPrimitive, tmpQuaternion, tmpRational);

        tmpBig = big1.divide(value2);
        tmpComplex = complex1.divide(value2);
        tmpPrimitive = primitive1.divide(value2);
        tmpQuaternion = quaternion1.divide(value2);
        tmpRational = rational1.divide(value2);

        this.assertEqual(tmpExp, tmpBig, tmpComplex, tmpPrimitive, tmpQuaternion, tmpRational);

    }

    @Test
    public void testInvert() {

        final double tmpExp = 1.0 / value1;

        final BigScalar tmpBig = big1.invert();
        final ComplexNumber tmpComplex = complex1.invert();
        final PrimitiveScalar tmpPrimitive = primitive1.invert();
        final Quaternion tmpQuaternion = quaternion1.invert();
        final RationalNumber tmpRational = rational1.invert();

        this.assertEqual(tmpExp, tmpBig, tmpComplex, tmpPrimitive, tmpQuaternion, tmpRational);
    }

    @Test
    @Tag("unstable")
    public void testMultiply() {

        final double tmpExp = value1 * value2;

        BigScalar tmpBig = big1.multiply(big2.get());
        ComplexNumber tmpComplex = complex1.multiply(complex2.get());
        PrimitiveScalar tmpPrimitive = primitive1.multiply(primitive2.get());
        Quaternion tmpQuaternion = quaternion1.multiply(quaternion2.get());
        RationalNumber tmpRational = rational1.multiply(rational2.get());

        this.assertEqual(tmpExp, tmpBig, tmpComplex, tmpPrimitive, tmpQuaternion, tmpRational);

        tmpBig = big1.multiply(value2);
        tmpComplex = complex1.multiply(value2);
        tmpPrimitive = primitive1.multiply(value2);
        tmpQuaternion = quaternion1.multiply(value2);
        tmpRational = rational1.multiply(value2);

        this.assertEqual(tmpExp, tmpBig, tmpComplex, tmpPrimitive, tmpQuaternion, tmpRational);
    }

    @Test
    @Tag("unstable")
    public void testNegate() {

        final double tmpExp = -value1;

        final BigScalar tmpBig = big1.negate();
        final ComplexNumber tmpComplex = complex1.negate();
        final PrimitiveScalar tmpPrimitive = primitive1.negate();
        final Quaternion tmpQuaternion = quaternion1.negate();
        final RationalNumber tmpRational = rational1.negate();

        this.assertEqual(tmpExp, tmpBig, tmpComplex, tmpPrimitive, tmpQuaternion, tmpRational);
    }

    @Test
    public void testSubtract() {

        final double tmpExp = value1 - value2;

        BigScalar tmpBig = big1.subtract(big2.get());
        ComplexNumber tmpComplex = complex1.subtract(complex2.get());
        PrimitiveScalar tmpPrimitive = primitive1.subtract(primitive2.get());
        Quaternion tmpQuaternion = quaternion1.subtract(quaternion2.get());
        RationalNumber tmpRational = rational1.subtract(rational2.get());

        this.assertEqual(tmpExp, tmpBig, tmpComplex, tmpPrimitive, tmpQuaternion, tmpRational);

        tmpBig = big1.subtract(value2);
        tmpComplex = complex1.subtract(value2);
        tmpPrimitive = primitive1.subtract(value2);
        tmpQuaternion = quaternion1.subtract(value2);
        tmpRational = rational1.subtract(value2);

        this.assertEqual(tmpExp, tmpBig, tmpComplex, tmpPrimitive, tmpQuaternion, tmpRational);
    }

    /**
     * Verify that normalised() always returns unit norm (1.0), including for zero input.
     */
    @Test
    public void testNormalisedUnitNorm() {

        TestUtils.assertEquals("Big +", 1.0, big1.normalised().norm(), CONTEXT);
        TestUtils.assertEquals("Complex +", 1.0, complex1.normalised().norm(), CONTEXT);
        TestUtils.assertEquals("Primitive +", 1.0, primitive1.normalised().norm(), CONTEXT);
        TestUtils.assertEquals("Quaternion +", 1.0, quaternion1.normalised().norm(), CONTEXT);
        TestUtils.assertEquals("Rational +", 1.0, rational1.normalised().norm(), CONTEXT);

        TestUtils.assertEquals("Big zero", 1.0, BigScalar.ZERO.normalised().norm(), CONTEXT);
        TestUtils.assertEquals("Complex zero", 1.0, ComplexNumber.ZERO.normalised().norm(), CONTEXT);
        TestUtils.assertEquals("Primitive zero", 1.0, PrimitiveScalar.ZERO.normalised().norm(), CONTEXT);
        TestUtils.assertEquals("Quaternion zero", 1.0, Quaternion.ZERO.normalised().norm(), CONTEXT);
        TestUtils.assertEquals("Rational zero", 1.0, RationalNumber.ZERO.normalised().norm(), CONTEXT);
        TestUtils.assertEquals("Quadruple zero", 1.0, Quadruple.ZERO.normalised().norm(), CONTEXT);

        TestUtils.assertEquals("Big neg", 1.0, big1.negate().normalised().norm(), CONTEXT);
        TestUtils.assertEquals("Complex neg", 1.0, complex1.negate().normalised().norm(), CONTEXT);
        TestUtils.assertEquals("Primitive neg", 1.0, primitive1.negate().normalised().norm(), CONTEXT);
        TestUtils.assertEquals("Quaternion neg", 1.0, quaternion1.negate().normalised().norm(), CONTEXT);
        TestUtils.assertEquals("Rational neg", 1.0, rational1.negate().normalised().norm(), CONTEXT);
    }

    /**
     * Verify the NormedVectorSpace contract: this == this.normalised().multiply(this.norm())
     */
    @Test
    public void testNormalisedTimesNorm() {

        double norm1 = big1.norm();
        TestUtils.assertEquals("Big", value1, big1.normalised().multiply(norm1).doubleValue(), CONTEXT);
        TestUtils.assertEquals("Complex", value1, complex1.normalised().multiply(norm1).doubleValue(), CONTEXT);
        TestUtils.assertEquals("Primitive", value1, primitive1.normalised().multiply(norm1).doubleValue(), CONTEXT);
        TestUtils.assertEquals("Quaternion", value1, quaternion1.normalised().multiply(norm1).doubleValue(), CONTEXT);
        TestUtils.assertEquals("Rational", value1, rational1.normalised().multiply(norm1).doubleValue(), CONTEXT);
    }

    /**
     * Verify that signum() returns zero for zero input and unit norm otherwise,
     * mimicking the behaviour of {@link Math#signum(double)}.
     */
    @Test
    public void testSignum() {

        TestUtils.assertEquals("Big +", 1.0, big1.signum().norm(), CONTEXT);
        TestUtils.assertEquals("Complex +", 1.0, complex1.signum().norm(), CONTEXT);
        TestUtils.assertEquals("Primitive +", 1.0, primitive1.signum().norm(), CONTEXT);
        TestUtils.assertEquals("Quaternion +", 1.0, quaternion1.signum().norm(), CONTEXT);
        TestUtils.assertEquals("Rational +", 1.0, rational1.signum().norm(), CONTEXT);

        TestUtils.assertTrue("Big zero", BigScalar.ZERO.signum().isZero());
        TestUtils.assertTrue("Complex zero", ComplexNumber.ZERO.signum().isZero());
        TestUtils.assertTrue("Primitive zero", PrimitiveScalar.ZERO.signum().isZero());
        TestUtils.assertTrue("Quaternion zero", Quaternion.ZERO.signum().isZero());
        TestUtils.assertTrue("Rational zero", RationalNumber.ZERO.signum().isZero());
        TestUtils.assertTrue("Quadruple zero", Quadruple.ZERO.signum().isZero());
    }

    /**
     * Verify normalised() for Quadruple specifically, including small and zero values.
     */
    @Test
    public void testNormalisedQuadruple() {

        Quadruple positive = Quadruple.valueOf(3.14);
        Quadruple negative = Quadruple.valueOf(-2.72);
        Quadruple small = Quadruple.valueOf(1E-300);
        Quadruple negSmall = Quadruple.valueOf(-1E-300);

        TestUtils.assertEquals("positive", 1.0, positive.normalised().norm(), CONTEXT);
        TestUtils.assertEquals("negative", 1.0, negative.normalised().norm(), CONTEXT);
        TestUtils.assertEquals("zero", 1.0, Quadruple.ZERO.normalised().norm(), CONTEXT);
        TestUtils.assertEquals("small", 1.0, small.normalised().norm(), CONTEXT);
        TestUtils.assertEquals("negSmall", 1.0, negSmall.normalised().norm(), CONTEXT);

        TestUtils.assertTrue("positive direction", positive.normalised().doubleValue() > 0.0);
        TestUtils.assertTrue("negative direction", negative.normalised().doubleValue() < 0.0);
        TestUtils.assertTrue("zero direction", Quadruple.ZERO.normalised().doubleValue() > 0.0);
        TestUtils.assertTrue("small direction", small.normalised().doubleValue() > 0.0);
        TestUtils.assertTrue("negSmall direction", negSmall.normalised().doubleValue() < 0.0);

        TestUtils.assertTrue("positive signum", positive.signum().doubleValue() > 0.0);
        TestUtils.assertTrue("negative signum", negative.signum().doubleValue() < 0.0);
        TestUtils.assertTrue("zero signum", Quadruple.ZERO.signum().isZero());
    }

    private void assertEqual(final double expected, final BigScalar big, final ComplexNumber complex, final PrimitiveScalar primitive,
            final Quaternion quaternion, final RationalNumber rational) {
        TestUtils.assertEquals("Big", expected, big.doubleValue(), CONTEXT);
        TestUtils.assertEquals("Complex", expected, complex.doubleValue(), CONTEXT);
        TestUtils.assertEquals("Primitive", expected, primitive.doubleValue(), CONTEXT);
        TestUtils.assertEquals("Quaternion", expected, quaternion.doubleValue(), CONTEXT);
        TestUtils.assertEquals("Rational", expected, rational.doubleValue(), CONTEXT);
    }

    private double makeRandom() {

        final double tmpbase = UNIFORM.doubleValue();
        final double tmpExp = UNIFORM.doubleValue();

        if (tmpbase > tmpExp) {
            return PrimitiveMath.POW.invoke(tmpbase, tmpExp);
        }
        return -PrimitiveMath.POW.invoke(tmpbase, tmpExp);
    }

}