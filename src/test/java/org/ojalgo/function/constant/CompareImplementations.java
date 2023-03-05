/*
 * Copyright 1997-2022 Optimatika
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
package org.ojalgo.function.constant;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.ParameterFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * Checks that {@linkplain org.ojalgo.function.BigFunction}, {@linkplain org.ojalgo.function.ComplexFunction},
 * {@linkplain org.ojalgo.function.PrimitiveFunction} and {@linkplain org.ojalgo.function.RationalFunction}
 * members produce equal results for simple cases. Cannot test complex valued arguments or results, and cannot
 * require the precision of BigDecimal or RationalNumber.
 *
 * @author apete
 */
public class CompareImplementations extends FunctionConstantTests {

    private static final Uniform AROUND_ZERO = new Uniform(NEG, TWO);
    private static final NumberContext CONTEXT = NumberContext.of(7, 14);
    private static final Uniform POSITIVE = new Uniform(E - TWO, TWO);

    private static void assertBinary(final BinaryFunction<BigDecimal> big, final BinaryFunction<ComplexNumber> complex, final BinaryFunction<Double> primitive,
            final BinaryFunction<Quaternion> quaternion, final BinaryFunction<RationalNumber> rational, final BinaryFunction<Quadruple> quadruple,
            final double arg1, final double arg2) {

        TestUtils.assertEquals("Big vs Complex, " + arg1 + ", " + arg2, big.invoke(arg1, arg2), complex.invoke(arg1, arg2), CONTEXT);
        TestUtils.assertEquals("Complex vs Primitive, " + arg1 + ", " + arg2, complex.invoke(arg1, arg2), primitive.invoke(arg1, arg2), CONTEXT);
        TestUtils.assertEquals("Primitive vs Quaternion, " + arg1 + ", " + arg2, primitive.invoke(arg1, arg2), quaternion.invoke(arg1, arg2), CONTEXT);
        TestUtils.assertEquals("Quaternion vs Rational, " + arg1 + ", " + arg2, quaternion.invoke(arg1, arg2), rational.invoke(arg1, arg2), CONTEXT);
        TestUtils.assertEquals("Rational vs Quadruple, " + arg1 + ", " + arg2, rational.invoke(arg1, arg2), quadruple.invoke(arg1, arg2), CONTEXT);
        TestUtils.assertEquals("Quadruple vs Big, " + arg1 + ", " + arg2, quadruple.invoke(arg1, arg2), big.invoke(arg1, arg2), CONTEXT);
    }

    private static void assertParameter(final ParameterFunction<BigDecimal> big, final ParameterFunction<ComplexNumber> complex,
            final ParameterFunction<Double> primitive, final ParameterFunction<Quaternion> quaternion, final ParameterFunction<RationalNumber> rational,
            final ParameterFunction<Quadruple> quadruple, final double arg, final int param) {

        TestUtils.assertEquals("Big vs Complex", big.invoke(arg, param), complex.invoke(arg, param), CONTEXT);
        TestUtils.assertEquals("Complex vs Primitive", complex.invoke(arg, param), primitive.invoke(arg, param), CONTEXT);
        TestUtils.assertEquals("Primitive vs Quaternion", primitive.invoke(arg, param), quaternion.invoke(arg, param), CONTEXT);
        TestUtils.assertEquals("Quaternion vs Rational", quaternion.invoke(arg, param), rational.invoke(arg, param), CONTEXT);
        TestUtils.assertEquals("Rational vs Quadruple", rational.invoke(arg, param), quadruple.invoke(arg, param), CONTEXT);
        TestUtils.assertEquals("Quadruple vs Big", quadruple.invoke(arg, param), big.invoke(arg, param), CONTEXT);
    }

    private static void assertUnary(final UnaryFunction<BigDecimal> big, final UnaryFunction<ComplexNumber> complex, final UnaryFunction<Double> primitive,
            final UnaryFunction<Quaternion> quaternion, final UnaryFunction<RationalNumber> rational, final UnaryFunction<Quadruple> quadruple,
            final double arg) {

        TestUtils.assertEquals("Big vs Complex, " + arg, big.invoke(arg), complex.invoke(arg), CONTEXT);
        TestUtils.assertEquals("Complex vs Primitive, " + arg, complex.invoke(arg), primitive.invoke(arg), CONTEXT);
        TestUtils.assertEquals("Primitive vs Quaternion, " + arg, primitive.invoke(arg), quaternion.invoke(arg), CONTEXT);
        TestUtils.assertEquals("Quaternion vs Rational, " + arg, quaternion.invoke(arg), rational.invoke(arg), CONTEXT);
        TestUtils.assertEquals("Rational vs Quadruple, " + arg, rational.invoke(arg), quadruple.invoke(arg), CONTEXT);
        TestUtils.assertEquals("Quadruple vs Big, " + arg, quadruple.invoke(arg), big.invoke(arg), CONTEXT);
    }

    @Test
    public void testABS() {
        CompareImplementations.assertUnary(BigMath.ABS, ComplexMath.ABS, PrimitiveMath.ABS, QuaternionMath.ABS, RationalMath.ABS, QuadrupleMath.ABS,
                AROUND_ZERO.doubleValue());
    }

    @Test
    public void testACOS() {

        CompareImplementations.assertUnary(BigMath.ACOS, ComplexMath.ACOS, PrimitiveMath.ACOS, QuaternionMath.ACOS, RationalMath.ACOS, QuadrupleMath.ACOS, NEG);
        CompareImplementations.assertUnary(BigMath.ACOS, ComplexMath.ACOS, PrimitiveMath.ACOS, QuaternionMath.ACOS, RationalMath.ACOS, QuadrupleMath.ACOS,
                NEG / SQRT_TWO);
        CompareImplementations.assertUnary(BigMath.ACOS, ComplexMath.ACOS, PrimitiveMath.ACOS, QuaternionMath.ACOS, RationalMath.ACOS, QuadrupleMath.ACOS,
                -HALF);
        CompareImplementations.assertUnary(BigMath.ACOS, ComplexMath.ACOS, PrimitiveMath.ACOS, QuaternionMath.ACOS, RationalMath.ACOS, QuadrupleMath.ACOS,
                ZERO);
        CompareImplementations.assertUnary(BigMath.ACOS, ComplexMath.ACOS, PrimitiveMath.ACOS, QuaternionMath.ACOS, RationalMath.ACOS, QuadrupleMath.ACOS,
                HALF);
        CompareImplementations.assertUnary(BigMath.ACOS, ComplexMath.ACOS, PrimitiveMath.ACOS, QuaternionMath.ACOS, RationalMath.ACOS, QuadrupleMath.ACOS,
                ONE / SQRT_TWO);
        CompareImplementations.assertUnary(BigMath.ACOS, ComplexMath.ACOS, PrimitiveMath.ACOS, QuaternionMath.ACOS, RationalMath.ACOS, QuadrupleMath.ACOS, ONE);

    }

    @Test
    public void testACOSH() {

        //        this.assertUnary(BigFunction.ACOSH, ComplexFunction.ACOSH, PrimitiveFunction.ACOSH, RationalFunction.ACOSH, POSITIVE.doubleValue());

        CompareImplementations.assertUnary(BigMath.ACOSH, ComplexMath.ACOSH, PrimitiveMath.ACOSH, QuaternionMath.ACOSH, RationalMath.ACOSH, QuadrupleMath.ACOSH,
                PI);
        CompareImplementations.assertUnary(BigMath.ACOSH, ComplexMath.ACOSH, PrimitiveMath.ACOSH, QuaternionMath.ACOSH, RationalMath.ACOSH, QuadrupleMath.ACOSH,
                E);
        CompareImplementations.assertUnary(BigMath.ACOSH, ComplexMath.ACOSH, PrimitiveMath.ACOSH, QuaternionMath.ACOSH, RationalMath.ACOSH, QuadrupleMath.ACOSH,
                TWO);
        CompareImplementations.assertUnary(BigMath.ACOSH, ComplexMath.ACOSH, PrimitiveMath.ACOSH, QuaternionMath.ACOSH, RationalMath.ACOSH, QuadrupleMath.ACOSH,
                HALF_PI);
        CompareImplementations.assertUnary(BigMath.ACOSH, ComplexMath.ACOSH, PrimitiveMath.ACOSH, QuaternionMath.ACOSH, RationalMath.ACOSH, QuadrupleMath.ACOSH,
                ONE);
    }

    @Test
    public void testADD() {
        CompareImplementations.assertBinary(BigMath.ADD, ComplexMath.ADD, PrimitiveMath.ADD, QuaternionMath.ADD, RationalMath.ADD, QuadrupleMath.ADD,
                POSITIVE.doubleValue(), POSITIVE.doubleValue());
    }

    @Test
    public void testASIN() {

        CompareImplementations.assertUnary(BigMath.ASIN, ComplexMath.ASIN, PrimitiveMath.ASIN, QuaternionMath.ASIN, RationalMath.ASIN, QuadrupleMath.ASIN,
                AROUND_ZERO.doubleValue());

        CompareImplementations.assertUnary(BigMath.ASIN, ComplexMath.ASIN, PrimitiveMath.ASIN, QuaternionMath.ASIN, RationalMath.ASIN, QuadrupleMath.ASIN, ONE);
        CompareImplementations.assertUnary(BigMath.ASIN, ComplexMath.ASIN, PrimitiveMath.ASIN, QuaternionMath.ASIN, RationalMath.ASIN, QuadrupleMath.ASIN,
                ZERO);
        CompareImplementations.assertUnary(BigMath.ASIN, ComplexMath.ASIN, PrimitiveMath.ASIN, QuaternionMath.ASIN, RationalMath.ASIN, QuadrupleMath.ASIN, NEG);
    }

    @Test
    public void testASINH() {

        CompareImplementations.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, QuadrupleMath.ASINH,
                AROUND_ZERO.doubleValue());

        CompareImplementations.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, QuadrupleMath.ASINH,
                PI);
        CompareImplementations.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, QuadrupleMath.ASINH,
                E);
        CompareImplementations.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, QuadrupleMath.ASINH,
                TWO);
        CompareImplementations.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, QuadrupleMath.ASINH,
                HALF_PI);
        CompareImplementations.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, QuadrupleMath.ASINH,
                ONE);

        CompareImplementations.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, QuadrupleMath.ASINH,
                ZERO);

        CompareImplementations.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, QuadrupleMath.ASINH,
                NEG);
        CompareImplementations.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, QuadrupleMath.ASINH,
                -HALF_PI);
        CompareImplementations.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, QuadrupleMath.ASINH,
                -TWO);
        CompareImplementations.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, QuadrupleMath.ASINH,
                -E);
        CompareImplementations.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, QuadrupleMath.ASINH,
                -PI);
    }

    @Test
    public void testATAN() {

        CompareImplementations.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, QuadrupleMath.ATAN,
                AROUND_ZERO.doubleValue());

        //        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, RationalFunction.ATAN, MAX_VALUE);
        CompareImplementations.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, QuadrupleMath.ATAN, PI);
        CompareImplementations.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, QuadrupleMath.ATAN, E);
        CompareImplementations.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, QuadrupleMath.ATAN, TWO);
        CompareImplementations.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, QuadrupleMath.ATAN,
                HALF_PI);
        CompareImplementations.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, QuadrupleMath.ATAN, ONE);

        CompareImplementations.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, QuadrupleMath.ATAN,
                AROUND_ZERO.doubleValue());

        CompareImplementations.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, QuadrupleMath.ATAN, NEG);
        CompareImplementations.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, QuadrupleMath.ATAN,
                -HALF_PI);
        CompareImplementations.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, QuadrupleMath.ATAN,
                -TWO);
        CompareImplementations.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, QuadrupleMath.ATAN, -E);
        CompareImplementations.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, QuadrupleMath.ATAN, -PI);
        CompareImplementations.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, QuadrupleMath.ATAN,
                MACHINE_SMALLEST);
    }

    @Test
    public void testATAN2() {

        CompareImplementations.assertBinary(BigMath.ATAN2, ComplexMath.ATAN2, PrimitiveMath.ATAN2, QuaternionMath.ATAN2, RationalMath.ATAN2,
                QuadrupleMath.ATAN2, ONE, TWO);
        CompareImplementations.assertBinary(BigMath.ATAN2, ComplexMath.ATAN2, PrimitiveMath.ATAN2, QuaternionMath.ATAN2, RationalMath.ATAN2,
                QuadrupleMath.ATAN2, TWO, ONE);
        CompareImplementations.assertBinary(BigMath.ATAN2, ComplexMath.ATAN2, PrimitiveMath.ATAN2, QuaternionMath.ATAN2, RationalMath.ATAN2,
                QuadrupleMath.ATAN2, ONE, HALF);
        CompareImplementations.assertBinary(BigMath.ATAN2, ComplexMath.ATAN2, PrimitiveMath.ATAN2, QuaternionMath.ATAN2, RationalMath.ATAN2,
                QuadrupleMath.ATAN2, HALF, ONE);

        CompareImplementations.assertBinary(BigMath.ATAN2, ComplexMath.ATAN2, PrimitiveMath.ATAN2, QuaternionMath.ATAN2, RationalMath.ATAN2,
                QuadrupleMath.ATAN2, ZERO, ONE);

    }

    @Test
    public void testATANH() {

        CompareImplementations.assertUnary(BigMath.ATANH, ComplexMath.ATANH, PrimitiveMath.ATANH, QuaternionMath.ATANH, RationalMath.ATANH, QuadrupleMath.ATANH,
                AROUND_ZERO.doubleValue());

        //    this.assertUnary(BigFunction.ATANH, ComplexFunction.ATANH, PrimitiveFunction.ATANH, RationalFunction.ATANH, ONE);
        CompareImplementations.assertUnary(BigMath.ATANH, ComplexMath.ATANH, PrimitiveMath.ATANH, QuaternionMath.ATANH, RationalMath.ATANH, QuadrupleMath.ATANH,
                ZERO);
        //  this.assertUnary(BigFunction.ATANH, ComplexFunction.ATANH, PrimitiveFunction.ATANH, RationalFunction.ATANH, NEG);
    }

    @Test
    public void testCARDINALITY() {
        CompareImplementations.assertUnary(BigMath.CARDINALITY, ComplexMath.CARDINALITY, PrimitiveMath.CARDINALITY, QuaternionMath.CARDINALITY,
                RationalMath.CARDINALITY, QuadrupleMath.CARDINALITY, AROUND_ZERO.doubleValue());
    }

    @Test
    @Tag("unstable")
    public void testCBRT() {

        CompareImplementations.assertUnary(BigMath.CBRT, ComplexMath.CBRT, PrimitiveMath.CBRT, QuaternionMath.CBRT, RationalMath.CBRT, QuadrupleMath.CBRT,
                POSITIVE.doubleValue());

        CompareImplementations.assertUnary(BigMath.CBRT, ComplexMath.CBRT, PrimitiveMath.CBRT, QuaternionMath.CBRT, RationalMath.CBRT, QuadrupleMath.CBRT, PI);
        CompareImplementations.assertUnary(BigMath.CBRT, ComplexMath.CBRT, PrimitiveMath.CBRT, QuaternionMath.CBRT, RationalMath.CBRT, QuadrupleMath.CBRT, E);
        CompareImplementations.assertUnary(BigMath.CBRT, ComplexMath.CBRT, PrimitiveMath.CBRT, QuaternionMath.CBRT, RationalMath.CBRT, QuadrupleMath.CBRT, TWO);
        CompareImplementations.assertUnary(BigMath.CBRT, ComplexMath.CBRT, PrimitiveMath.CBRT, QuaternionMath.CBRT, RationalMath.CBRT, QuadrupleMath.CBRT,
                HALF_PI);
        CompareImplementations.assertUnary(BigMath.CBRT, ComplexMath.CBRT, PrimitiveMath.CBRT, QuaternionMath.CBRT, RationalMath.CBRT, QuadrupleMath.CBRT, ONE);

        CompareImplementations.assertUnary(BigMath.CBRT, ComplexMath.CBRT, PrimitiveMath.CBRT, QuaternionMath.CBRT, RationalMath.CBRT, QuadrupleMath.CBRT,
                ZERO);
    }

    @Test
    public void testCEIL() {
        CompareImplementations.assertUnary(BigMath.CEIL, ComplexMath.CEIL, PrimitiveMath.CEIL, QuaternionMath.CEIL, RationalMath.CEIL, QuadrupleMath.CEIL,
                AROUND_ZERO.doubleValue());
        CompareImplementations.assertUnary(BigMath.CEIL, ComplexMath.CEIL, PrimitiveMath.CEIL, QuaternionMath.CEIL, RationalMath.CEIL, QuadrupleMath.CEIL,
                HALF);
        CompareImplementations.assertUnary(BigMath.CEIL, ComplexMath.CEIL, PrimitiveMath.CEIL, QuaternionMath.CEIL, RationalMath.CEIL, QuadrupleMath.CEIL,
                -HALF);
    }

    @Test
    public void testCONJUGATE() {
        CompareImplementations.assertUnary(BigMath.CONJUGATE, ComplexMath.CONJUGATE, PrimitiveMath.CONJUGATE, QuaternionMath.CONJUGATE, RationalMath.CONJUGATE,
                QuadrupleMath.CONJUGATE, AROUND_ZERO.doubleValue());
    }

    @Test
    public void testCOS() {

        CompareImplementations.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, QuadrupleMath.COS,
                AROUND_ZERO.doubleValue());

        CompareImplementations.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, QuadrupleMath.COS, PI);
        CompareImplementations.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, QuadrupleMath.COS, E);
        CompareImplementations.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, QuadrupleMath.COS, TWO);
        CompareImplementations.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, QuadrupleMath.COS, HALF_PI);
        CompareImplementations.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, QuadrupleMath.COS, ONE);

        CompareImplementations.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, QuadrupleMath.COS, ZERO);

        CompareImplementations.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, QuadrupleMath.COS, NEG);
        CompareImplementations.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, QuadrupleMath.COS, -HALF_PI);
        CompareImplementations.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, QuadrupleMath.COS, -TWO);
        CompareImplementations.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, QuadrupleMath.COS, -E);
        CompareImplementations.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, QuadrupleMath.COS, -PI);
    }

    @Test
    public void testCOSH() {

        CompareImplementations.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, QuadrupleMath.COSH,
                AROUND_ZERO.doubleValue());

        CompareImplementations.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, QuadrupleMath.COSH, PI);
        CompareImplementations.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, QuadrupleMath.COSH, E);
        CompareImplementations.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, QuadrupleMath.COSH, TWO);
        CompareImplementations.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, QuadrupleMath.COSH,
                HALF_PI);
        CompareImplementations.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, QuadrupleMath.COSH, ONE);

        CompareImplementations.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, QuadrupleMath.COSH,
                ZERO);

        CompareImplementations.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, QuadrupleMath.COSH, NEG);
        CompareImplementations.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, QuadrupleMath.COSH,
                -HALF_PI);
        CompareImplementations.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, QuadrupleMath.COSH,
                -TWO);
        CompareImplementations.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, QuadrupleMath.COSH, -E);
        CompareImplementations.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, QuadrupleMath.COSH, -PI);
    }

    @Test
    public void testDIVIDE() {
        CompareImplementations.assertBinary(BigMath.DIVIDE, ComplexMath.DIVIDE, PrimitiveMath.DIVIDE, QuaternionMath.DIVIDE, RationalMath.DIVIDE,
                QuadrupleMath.DIVIDE, POSITIVE.doubleValue(), POSITIVE.doubleValue());
    }

    @Test
    public void testEXP() {

        CompareImplementations.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, QuadrupleMath.EXP, TEN);

        CompareImplementations.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, QuadrupleMath.EXP, PI);
        CompareImplementations.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, QuadrupleMath.EXP, E);
        CompareImplementations.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, QuadrupleMath.EXP, TWO);
        CompareImplementations.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, QuadrupleMath.EXP, HALF_PI);
        CompareImplementations.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, QuadrupleMath.EXP, ONE);

        CompareImplementations.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, QuadrupleMath.EXP,
                MACHINE_SMALLEST);
        CompareImplementations.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, QuadrupleMath.EXP, ZERO);
        CompareImplementations.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, QuadrupleMath.EXP,
                -MACHINE_SMALLEST);

        CompareImplementations.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, QuadrupleMath.EXP, NEG);
        CompareImplementations.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, QuadrupleMath.EXP, -HALF_PI);
        CompareImplementations.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, QuadrupleMath.EXP, -TWO);
        CompareImplementations.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, QuadrupleMath.EXP, -E);
        CompareImplementations.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, QuadrupleMath.EXP, -PI);

        CompareImplementations.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, QuadrupleMath.EXP, -TEN);

        CompareImplementations.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, QuadrupleMath.EXP, -HUNDRED);

    }

    @Test
    public void testEXPM1() {
        CompareImplementations.assertUnary(BigMath.EXPM1, ComplexMath.EXPM1, PrimitiveMath.EXPM1, QuaternionMath.EXPM1, RationalMath.EXPM1, QuadrupleMath.EXPM1,
                AROUND_ZERO.doubleValue());
    }

    @Test
    public void testFLOOR() {
        CompareImplementations.assertUnary(BigMath.FLOOR, ComplexMath.FLOOR, PrimitiveMath.FLOOR, QuaternionMath.FLOOR, RationalMath.FLOOR, QuadrupleMath.FLOOR,
                AROUND_ZERO.doubleValue());
        CompareImplementations.assertUnary(BigMath.FLOOR, ComplexMath.FLOOR, PrimitiveMath.FLOOR, QuaternionMath.FLOOR, RationalMath.FLOOR, QuadrupleMath.FLOOR,
                HALF);
        CompareImplementations.assertUnary(BigMath.FLOOR, ComplexMath.FLOOR, PrimitiveMath.FLOOR, QuaternionMath.FLOOR, RationalMath.FLOOR, QuadrupleMath.FLOOR,
                -HALF);
    }

    @Test
    public void testHYPOT() {
        CompareImplementations.assertBinary(BigMath.HYPOT, ComplexMath.HYPOT, PrimitiveMath.HYPOT, QuaternionMath.HYPOT, RationalMath.HYPOT,
                QuadrupleMath.HYPOT, POSITIVE.doubleValue(), POSITIVE.doubleValue());
    }

    @Test
    public void testINVERT() {
        CompareImplementations.assertUnary(BigMath.INVERT, ComplexMath.INVERT, PrimitiveMath.INVERT, QuaternionMath.INVERT, RationalMath.INVERT,
                QuadrupleMath.INVERT, POSITIVE.doubleValue());
    }

    @Test
    public void testLOG() {

        CompareImplementations.assertUnary(BigMath.LOG, ComplexMath.LOG, PrimitiveMath.LOG, QuaternionMath.LOG, RationalMath.LOG, QuadrupleMath.LOG, THOUSAND);
        CompareImplementations.assertUnary(BigMath.LOG, ComplexMath.LOG, PrimitiveMath.LOG, QuaternionMath.LOG, RationalMath.LOG, QuadrupleMath.LOG, HUNDRED);
        CompareImplementations.assertUnary(BigMath.LOG, ComplexMath.LOG, PrimitiveMath.LOG, QuaternionMath.LOG, RationalMath.LOG, QuadrupleMath.LOG, TEN);
        CompareImplementations.assertUnary(BigMath.LOG, ComplexMath.LOG, PrimitiveMath.LOG, QuaternionMath.LOG, RationalMath.LOG, QuadrupleMath.LOG, ONE);
        CompareImplementations.assertUnary(BigMath.LOG, ComplexMath.LOG, PrimitiveMath.LOG, QuaternionMath.LOG, RationalMath.LOG, QuadrupleMath.LOG, TENTH);
        CompareImplementations.assertUnary(BigMath.LOG, ComplexMath.LOG, PrimitiveMath.LOG, QuaternionMath.LOG, RationalMath.LOG, QuadrupleMath.LOG, HUNDREDTH);
        CompareImplementations.assertUnary(BigMath.LOG, ComplexMath.LOG, PrimitiveMath.LOG, QuaternionMath.LOG, RationalMath.LOG, QuadrupleMath.LOG,
                THOUSANDTH);

    }

    @Test
    public void testLOG10() {
        CompareImplementations.assertUnary(BigMath.LOG10, ComplexMath.LOG10, PrimitiveMath.LOG10, QuaternionMath.LOG10, RationalMath.LOG10, QuadrupleMath.LOG10,
                POSITIVE.doubleValue());
    }

    @Test
    public void testLOG1P() {
        CompareImplementations.assertUnary(BigMath.LOG1P, ComplexMath.LOG1P, PrimitiveMath.LOG1P, QuaternionMath.LOG1P, RationalMath.LOG1P, QuadrupleMath.LOG1P,
                POSITIVE.doubleValue());
    }

    @Test
    @Tag("unstable")
    public void testMAX() {
        CompareImplementations.assertBinary(BigMath.MAX, ComplexMath.MAX, PrimitiveMath.MAX, QuaternionMath.MAX, RationalMath.MAX, QuadrupleMath.MAX,
                AROUND_ZERO.doubleValue(), POSITIVE.doubleValue());
    }

    @Test
    @Tag("unstable")
    public void testMIN() {
        CompareImplementations.assertBinary(BigMath.MIN, ComplexMath.MIN, PrimitiveMath.MIN, QuaternionMath.MIN, RationalMath.MIN, QuadrupleMath.MIN,
                AROUND_ZERO.doubleValue(), POSITIVE.doubleValue());
    }

    @Test
    public void testMULTIPLY() {
        CompareImplementations.assertBinary(BigMath.MULTIPLY, ComplexMath.MULTIPLY, PrimitiveMath.MULTIPLY, QuaternionMath.MULTIPLY, RationalMath.MULTIPLY,
                QuadrupleMath.MULTIPLY, AROUND_ZERO.doubleValue(), POSITIVE.doubleValue());
    }

    @Test
    public void testNEGATE() {
        CompareImplementations.assertUnary(BigMath.NEGATE, ComplexMath.NEGATE, PrimitiveMath.NEGATE, QuaternionMath.NEGATE, RationalMath.NEGATE,
                QuadrupleMath.NEGATE, AROUND_ZERO.doubleValue());
    }

    @Test
    public void testPOW() {

        // Only defined for non-negative (absolute) first aruments

        CompareImplementations.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, QuadrupleMath.POW, HUNDREDTH,
                NINE);
        CompareImplementations.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, QuadrupleMath.POW,
                THOUSANDTH, SEVEN);
        CompareImplementations.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, QuadrupleMath.POW, TENTH,
                EIGHT);
        CompareImplementations.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, QuadrupleMath.POW, ONE, SIX);
        CompareImplementations.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, QuadrupleMath.POW, HALF_PI,
                FIVE);
        CompareImplementations.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, QuadrupleMath.POW, TWO,
                FOUR);
        CompareImplementations.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, QuadrupleMath.POW, E, THREE);
        CompareImplementations.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, QuadrupleMath.POW, PI, TWO);
        CompareImplementations.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, QuadrupleMath.POW, SQRT_PI,
                ONE);

        CompareImplementations.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, QuadrupleMath.POW,
                SQRT_TWO_PI, SQRT_TWO_PI);
        CompareImplementations.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, QuadrupleMath.POW,
                SQRT_TWO_PI, ZERO);
        CompareImplementations.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, QuadrupleMath.POW,
                THOUSANDTH, SQRT_TWO_PI);
        CompareImplementations.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, QuadrupleMath.POW,
                THOUSANDTH, ZERO);

        CompareImplementations.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, QuadrupleMath.POW,
                SQRT_TWO_PI, -SQRT_TWO_PI);
        CompareImplementations.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, QuadrupleMath.POW,
                SQRT_TWO_PI, -ZERO);
        CompareImplementations.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, QuadrupleMath.POW,
                THOUSANDTH, -SQRT_TWO_PI);
        CompareImplementations.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, QuadrupleMath.POW,
                THOUSANDTH, -ZERO);

    }

    @Test
    public void testPOWER() {

        //        this.assertParameter(BigFunction.POWER, ComplexFunction.POWER, PrimitiveFunction.POWER, QuaternionFunction.POWER, RationalFunction.POWER,
        //                AROUND_ZERO.doubleValue(), Uniform.randomInteger(1, 10));

        CompareImplementations.assertParameter(BigMath.POWER, ComplexMath.POWER, PrimitiveMath.POWER, QuaternionMath.POWER, RationalMath.POWER,
                QuadrupleMath.POWER, PI, 2);
        CompareImplementations.assertParameter(BigMath.POWER, ComplexMath.POWER, PrimitiveMath.POWER, QuaternionMath.POWER, RationalMath.POWER,
                QuadrupleMath.POWER, E, 3);
        CompareImplementations.assertParameter(BigMath.POWER, ComplexMath.POWER, PrimitiveMath.POWER, QuaternionMath.POWER, RationalMath.POWER,
                QuadrupleMath.POWER, TWO, 4);
        CompareImplementations.assertParameter(BigMath.POWER, ComplexMath.POWER, PrimitiveMath.POWER, QuaternionMath.POWER, RationalMath.POWER,
                QuadrupleMath.POWER, HALF_PI, 5);
        CompareImplementations.assertParameter(BigMath.POWER, ComplexMath.POWER, PrimitiveMath.POWER, QuaternionMath.POWER, RationalMath.POWER,
                QuadrupleMath.POWER, ONE, 6);

        CompareImplementations.assertParameter(BigMath.POWER, ComplexMath.POWER, PrimitiveMath.POWER, QuaternionMath.POWER, RationalMath.POWER,
                QuadrupleMath.POWER, ZERO, 7);
    }

    @Test
    public void testRINT() {

        CompareImplementations.assertUnary(BigMath.RINT, ComplexMath.RINT, PrimitiveMath.RINT, QuaternionMath.RINT, RationalMath.RINT, QuadrupleMath.RINT,
                AROUND_ZERO.doubleValue());

        CompareImplementations.assertUnary(BigMath.RINT, ComplexMath.RINT, PrimitiveMath.RINT, QuaternionMath.RINT, RationalMath.RINT, QuadrupleMath.RINT,
                HALF);

        CompareImplementations.assertUnary(BigMath.RINT, ComplexMath.RINT, PrimitiveMath.RINT, QuaternionMath.RINT, RationalMath.RINT, QuadrupleMath.RINT,
                -HALF);

        CompareImplementations.assertUnary(BigMath.RINT, ComplexMath.RINT, PrimitiveMath.RINT, QuaternionMath.RINT, RationalMath.RINT, QuadrupleMath.RINT,
                ONE + HALF);

        CompareImplementations.assertUnary(BigMath.RINT, ComplexMath.RINT, PrimitiveMath.RINT, QuaternionMath.RINT, RationalMath.RINT, QuadrupleMath.RINT,
                -(ONE + HALF));

    }

    @Test
    @Tag("unstable")
    public void testROOT() {
        CompareImplementations.assertParameter(BigMath.ROOT, ComplexMath.ROOT, PrimitiveMath.ROOT, QuaternionMath.ROOT, RationalMath.ROOT, QuadrupleMath.ROOT,
                POSITIVE.doubleValue(), Uniform.randomInteger(1, 10));
    }

    @Test
    public void testSCALE() {
        CompareImplementations.assertParameter(BigMath.SCALE, ComplexMath.SCALE, PrimitiveMath.SCALE, QuaternionMath.SCALE, RationalMath.SCALE,
                QuadrupleMath.SCALE, AROUND_ZERO.doubleValue(), Uniform.randomInteger(1, 10));
    }

    @Test
    public void testSIGNUM() {
        CompareImplementations.assertUnary(BigMath.SIGNUM, ComplexMath.SIGNUM, PrimitiveMath.SIGNUM, QuaternionMath.SIGNUM, RationalMath.SIGNUM,
                QuadrupleMath.SIGNUM, AROUND_ZERO.doubleValue());
    }

    @Test
    public void testSIN() {

        CompareImplementations.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, QuadrupleMath.SIN,
                AROUND_ZERO.doubleValue());

        CompareImplementations.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, QuadrupleMath.SIN, PI);
        CompareImplementations.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, QuadrupleMath.SIN, E);
        CompareImplementations.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, QuadrupleMath.SIN, TWO);
        CompareImplementations.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, QuadrupleMath.SIN, HALF_PI);
        CompareImplementations.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, QuadrupleMath.SIN, ONE);

        CompareImplementations.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, QuadrupleMath.SIN, ZERO);

        CompareImplementations.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, QuadrupleMath.SIN, NEG);
        CompareImplementations.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, QuadrupleMath.SIN, -HALF_PI);
        CompareImplementations.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, QuadrupleMath.SIN, -TWO);
        CompareImplementations.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, QuadrupleMath.SIN, -E);
        CompareImplementations.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, QuadrupleMath.SIN, -PI);
    }

    @Test
    public void testSINH() {

        CompareImplementations.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, QuadrupleMath.SINH,
                AROUND_ZERO.doubleValue());

        CompareImplementations.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, QuadrupleMath.SINH, PI);
        CompareImplementations.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, QuadrupleMath.SINH, E);
        CompareImplementations.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, QuadrupleMath.SINH, TWO);
        CompareImplementations.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, QuadrupleMath.SINH,
                HALF_PI);
        CompareImplementations.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, QuadrupleMath.SINH, ONE);

        CompareImplementations.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, QuadrupleMath.SINH,
                ZERO);

        CompareImplementations.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, QuadrupleMath.SINH, NEG);
        CompareImplementations.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, QuadrupleMath.SINH,
                -HALF_PI);
        CompareImplementations.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, QuadrupleMath.SINH,
                -TWO);
        CompareImplementations.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, QuadrupleMath.SINH, -E);
        CompareImplementations.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, QuadrupleMath.SINH, -PI);
    }

    @Test
    public void testSQRT() {

        CompareImplementations.assertUnary(BigMath.SQRT, ComplexMath.SQRT, PrimitiveMath.SQRT, QuaternionMath.SQRT, RationalMath.SQRT, QuadrupleMath.SQRT,
                POSITIVE.doubleValue());

        CompareImplementations.assertUnary(BigMath.SQRT, ComplexMath.SQRT, PrimitiveMath.SQRT, QuaternionMath.SQRT, RationalMath.SQRT, QuadrupleMath.SQRT, PI);
        CompareImplementations.assertUnary(BigMath.SQRT, ComplexMath.SQRT, PrimitiveMath.SQRT, QuaternionMath.SQRT, RationalMath.SQRT, QuadrupleMath.SQRT, E);
        CompareImplementations.assertUnary(BigMath.SQRT, ComplexMath.SQRT, PrimitiveMath.SQRT, QuaternionMath.SQRT, RationalMath.SQRT, QuadrupleMath.SQRT, TWO);
        CompareImplementations.assertUnary(BigMath.SQRT, ComplexMath.SQRT, PrimitiveMath.SQRT, QuaternionMath.SQRT, RationalMath.SQRT, QuadrupleMath.SQRT,
                HALF_PI);
        CompareImplementations.assertUnary(BigMath.SQRT, ComplexMath.SQRT, PrimitiveMath.SQRT, QuaternionMath.SQRT, RationalMath.SQRT, QuadrupleMath.SQRT, ONE);

        CompareImplementations.assertUnary(BigMath.SQRT, ComplexMath.SQRT, PrimitiveMath.SQRT, QuaternionMath.SQRT, RationalMath.SQRT, QuadrupleMath.SQRT,
                ZERO);
    }

    @Test
    public void testSQRT1PX2() {
        CompareImplementations.assertUnary(BigMath.SQRT1PX2, ComplexMath.SQRT1PX2, PrimitiveMath.SQRT1PX2, QuaternionMath.SQRT1PX2, RationalMath.SQRT1PX2,
                QuadrupleMath.SQRT1PX2, POSITIVE.doubleValue());
    }

    @Test
    public void testSUBTRACT() {
        CompareImplementations.assertBinary(BigMath.SUBTRACT, ComplexMath.SUBTRACT, PrimitiveMath.SUBTRACT, QuaternionMath.SUBTRACT, RationalMath.SUBTRACT,
                QuadrupleMath.SUBTRACT, POSITIVE.doubleValue(), POSITIVE.doubleValue());
    }

    @Test
    public void testTAN() {

        CompareImplementations.assertUnary(BigMath.TAN, ComplexMath.TAN, PrimitiveMath.TAN, QuaternionMath.TAN, RationalMath.TAN, QuadrupleMath.TAN,
                AROUND_ZERO.doubleValue());

        CompareImplementations.assertUnary(BigMath.TAN, ComplexMath.TAN, PrimitiveMath.TAN, QuaternionMath.TAN, RationalMath.TAN, QuadrupleMath.TAN, PI);
        CompareImplementations.assertUnary(BigMath.TAN, ComplexMath.TAN, PrimitiveMath.TAN, QuaternionMath.TAN, RationalMath.TAN, QuadrupleMath.TAN, E);
        CompareImplementations.assertUnary(BigMath.TAN, ComplexMath.TAN, PrimitiveMath.TAN, QuaternionMath.TAN, RationalMath.TAN, QuadrupleMath.TAN, TWO);
        //     this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, RationalFunction.TAN, HALF_PI);
        CompareImplementations.assertUnary(BigMath.TAN, ComplexMath.TAN, PrimitiveMath.TAN, QuaternionMath.TAN, RationalMath.TAN, QuadrupleMath.TAN, ONE);

        CompareImplementations.assertUnary(BigMath.TAN, ComplexMath.TAN, PrimitiveMath.TAN, QuaternionMath.TAN, RationalMath.TAN, QuadrupleMath.TAN, ZERO);

        CompareImplementations.assertUnary(BigMath.TAN, ComplexMath.TAN, PrimitiveMath.TAN, QuaternionMath.TAN, RationalMath.TAN, QuadrupleMath.TAN, NEG);
        //    this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, RationalFunction.TAN, -HALF_PI);
        CompareImplementations.assertUnary(BigMath.TAN, ComplexMath.TAN, PrimitiveMath.TAN, QuaternionMath.TAN, RationalMath.TAN, QuadrupleMath.TAN, -TWO);
        CompareImplementations.assertUnary(BigMath.TAN, ComplexMath.TAN, PrimitiveMath.TAN, QuaternionMath.TAN, RationalMath.TAN, QuadrupleMath.TAN, -E);
        CompareImplementations.assertUnary(BigMath.TAN, ComplexMath.TAN, PrimitiveMath.TAN, QuaternionMath.TAN, RationalMath.TAN, QuadrupleMath.TAN, -PI);
    }

    @Test
    public void testTANH() {

        CompareImplementations.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, QuadrupleMath.TANH,
                AROUND_ZERO.doubleValue());

        CompareImplementations.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, QuadrupleMath.TANH, PI);
        CompareImplementations.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, QuadrupleMath.TANH, E);
        CompareImplementations.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, QuadrupleMath.TANH, TWO);
        CompareImplementations.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, QuadrupleMath.TANH,
                HALF_PI);
        CompareImplementations.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, QuadrupleMath.TANH, ONE);

        CompareImplementations.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, QuadrupleMath.TANH,
                ZERO);

        CompareImplementations.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, QuadrupleMath.TANH, NEG);
        CompareImplementations.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, QuadrupleMath.TANH,
                -HALF_PI);
        CompareImplementations.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, QuadrupleMath.TANH,
                -TWO);
        CompareImplementations.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, QuadrupleMath.TANH, -E);
        CompareImplementations.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, QuadrupleMath.TANH, -PI);
    }

    @Test
    public void testVALUE() {
        CompareImplementations.assertUnary(BigMath.VALUE, ComplexMath.VALUE, PrimitiveMath.VALUE, QuaternionMath.VALUE, RationalMath.VALUE, QuadrupleMath.VALUE,
                AROUND_ZERO.doubleValue());
    }

}
