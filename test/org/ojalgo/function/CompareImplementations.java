/*
 * Copyright 1997-2018 Optimatika
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
package org.ojalgo.function;

import static org.ojalgo.constant.PrimitiveMath.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
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
public class CompareImplementations {

    private static final Uniform AROUND_ZERO = new Uniform(NEG, TWO);
    private static final NumberContext CONTEXT = new NumberContext(7, 14);
    private static final Uniform POSITIVE = new Uniform(E - TWO, TWO);

    @Test
    public void testABS() {
        this.assertUnary(BigFunction.ABS, ComplexFunction.ABS, PrimitiveFunction.ABS, QuaternionFunction.ABS, RationalFunction.ABS, AROUND_ZERO.doubleValue());
    }

    @Test
    public void testACOS() {

        this.assertUnary(BigFunction.ACOS, ComplexFunction.ACOS, PrimitiveFunction.ACOS, QuaternionFunction.ACOS, RationalFunction.ACOS, NEG);
        this.assertUnary(BigFunction.ACOS, ComplexFunction.ACOS, PrimitiveFunction.ACOS, QuaternionFunction.ACOS, RationalFunction.ACOS, NEG / SQRT_TWO);
        this.assertUnary(BigFunction.ACOS, ComplexFunction.ACOS, PrimitiveFunction.ACOS, QuaternionFunction.ACOS, RationalFunction.ACOS, -HALF);
        this.assertUnary(BigFunction.ACOS, ComplexFunction.ACOS, PrimitiveFunction.ACOS, QuaternionFunction.ACOS, RationalFunction.ACOS, ZERO);
        this.assertUnary(BigFunction.ACOS, ComplexFunction.ACOS, PrimitiveFunction.ACOS, QuaternionFunction.ACOS, RationalFunction.ACOS, HALF);
        this.assertUnary(BigFunction.ACOS, ComplexFunction.ACOS, PrimitiveFunction.ACOS, QuaternionFunction.ACOS, RationalFunction.ACOS, ONE / SQRT_TWO);
        this.assertUnary(BigFunction.ACOS, ComplexFunction.ACOS, PrimitiveFunction.ACOS, QuaternionFunction.ACOS, RationalFunction.ACOS, ONE);

    }

    @Test
    public void testACOSH() {

        //        this.assertUnary(BigFunction.ACOSH, ComplexFunction.ACOSH, PrimitiveFunction.ACOSH, RationalFunction.ACOSH, POSITIVE.doubleValue());

        this.assertUnary(BigFunction.ACOSH, ComplexFunction.ACOSH, PrimitiveFunction.ACOSH, QuaternionFunction.ACOSH, RationalFunction.ACOSH, PI);
        this.assertUnary(BigFunction.ACOSH, ComplexFunction.ACOSH, PrimitiveFunction.ACOSH, QuaternionFunction.ACOSH, RationalFunction.ACOSH, E);
        this.assertUnary(BigFunction.ACOSH, ComplexFunction.ACOSH, PrimitiveFunction.ACOSH, QuaternionFunction.ACOSH, RationalFunction.ACOSH, TWO);
        this.assertUnary(BigFunction.ACOSH, ComplexFunction.ACOSH, PrimitiveFunction.ACOSH, QuaternionFunction.ACOSH, RationalFunction.ACOSH, HALF_PI);
        this.assertUnary(BigFunction.ACOSH, ComplexFunction.ACOSH, PrimitiveFunction.ACOSH, QuaternionFunction.ACOSH, RationalFunction.ACOSH, ONE);
    }

    @Test
    public void testADD() {
        this.assertBinary(BigFunction.ADD, ComplexFunction.ADD, PrimitiveFunction.ADD, QuaternionFunction.ADD, RationalFunction.ADD, POSITIVE.doubleValue(),
                POSITIVE.doubleValue());
    }

    @Test
    public void testASIN() {

        this.assertUnary(BigFunction.ASIN, ComplexFunction.ASIN, PrimitiveFunction.ASIN, QuaternionFunction.ASIN, RationalFunction.ASIN,
                AROUND_ZERO.doubleValue());

        this.assertUnary(BigFunction.ASIN, ComplexFunction.ASIN, PrimitiveFunction.ASIN, QuaternionFunction.ASIN, RationalFunction.ASIN, ONE);
        this.assertUnary(BigFunction.ASIN, ComplexFunction.ASIN, PrimitiveFunction.ASIN, QuaternionFunction.ASIN, RationalFunction.ASIN, ZERO);
        this.assertUnary(BigFunction.ASIN, ComplexFunction.ASIN, PrimitiveFunction.ASIN, QuaternionFunction.ASIN, RationalFunction.ASIN, NEG);
    }

    @Test
    public void testASINH() {

        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, QuaternionFunction.ASINH, RationalFunction.ASINH,
                AROUND_ZERO.doubleValue());

        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, QuaternionFunction.ASINH, RationalFunction.ASINH, PI);
        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, QuaternionFunction.ASINH, RationalFunction.ASINH, E);
        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, QuaternionFunction.ASINH, RationalFunction.ASINH, TWO);
        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, QuaternionFunction.ASINH, RationalFunction.ASINH, HALF_PI);
        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, QuaternionFunction.ASINH, RationalFunction.ASINH, ONE);

        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, QuaternionFunction.ASINH, RationalFunction.ASINH, ZERO);

        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, QuaternionFunction.ASINH, RationalFunction.ASINH, NEG);
        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, QuaternionFunction.ASINH, RationalFunction.ASINH, -HALF_PI);
        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, QuaternionFunction.ASINH, RationalFunction.ASINH, -TWO);
        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, QuaternionFunction.ASINH, RationalFunction.ASINH, -E);
        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, QuaternionFunction.ASINH, RationalFunction.ASINH, -PI);
    }

    @Test
    public void testATAN() {

        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, QuaternionFunction.ATAN, RationalFunction.ATAN,
                AROUND_ZERO.doubleValue());

        //        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, RationalFunction.ATAN, MAX_VALUE);
        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, QuaternionFunction.ATAN, RationalFunction.ATAN, PI);
        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, QuaternionFunction.ATAN, RationalFunction.ATAN, E);
        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, QuaternionFunction.ATAN, RationalFunction.ATAN, TWO);
        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, QuaternionFunction.ATAN, RationalFunction.ATAN, HALF_PI);
        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, QuaternionFunction.ATAN, RationalFunction.ATAN, ONE);

        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, QuaternionFunction.ATAN, RationalFunction.ATAN,
                AROUND_ZERO.doubleValue());

        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, QuaternionFunction.ATAN, RationalFunction.ATAN, NEG);
        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, QuaternionFunction.ATAN, RationalFunction.ATAN, -HALF_PI);
        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, QuaternionFunction.ATAN, RationalFunction.ATAN, -TWO);
        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, QuaternionFunction.ATAN, RationalFunction.ATAN, -E);
        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, QuaternionFunction.ATAN, RationalFunction.ATAN, -PI);
        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, QuaternionFunction.ATAN, RationalFunction.ATAN, MACHINE_SMALLEST);
    }

    @Test
    public void testATAN2() {

        this.assertBinary(BigFunction.ATAN2, ComplexFunction.ATAN2, PrimitiveFunction.ATAN2, QuaternionFunction.ATAN2, RationalFunction.ATAN2, ONE, TWO);
        this.assertBinary(BigFunction.ATAN2, ComplexFunction.ATAN2, PrimitiveFunction.ATAN2, QuaternionFunction.ATAN2, RationalFunction.ATAN2, TWO, ONE);
        this.assertBinary(BigFunction.ATAN2, ComplexFunction.ATAN2, PrimitiveFunction.ATAN2, QuaternionFunction.ATAN2, RationalFunction.ATAN2, ONE, HALF);
        this.assertBinary(BigFunction.ATAN2, ComplexFunction.ATAN2, PrimitiveFunction.ATAN2, QuaternionFunction.ATAN2, RationalFunction.ATAN2, HALF, ONE);

        this.assertBinary(BigFunction.ATAN2, ComplexFunction.ATAN2, PrimitiveFunction.ATAN2, QuaternionFunction.ATAN2, RationalFunction.ATAN2, ZERO, ONE);

    }

    @Test
    public void testATANH() {

        this.assertUnary(BigFunction.ATANH, ComplexFunction.ATANH, PrimitiveFunction.ATANH, QuaternionFunction.ATANH, RationalFunction.ATANH,
                AROUND_ZERO.doubleValue());

        //    this.assertUnary(BigFunction.ATANH, ComplexFunction.ATANH, PrimitiveFunction.ATANH, RationalFunction.ATANH, ONE);
        this.assertUnary(BigFunction.ATANH, ComplexFunction.ATANH, PrimitiveFunction.ATANH, QuaternionFunction.ATANH, RationalFunction.ATANH, ZERO);
        //  this.assertUnary(BigFunction.ATANH, ComplexFunction.ATANH, PrimitiveFunction.ATANH, RationalFunction.ATANH, NEG);
    }

    @Test
    public void testCARDINALITY() {
        this.assertUnary(BigFunction.CARDINALITY, ComplexFunction.CARDINALITY, PrimitiveFunction.CARDINALITY, QuaternionFunction.CARDINALITY,
                RationalFunction.CARDINALITY, AROUND_ZERO.doubleValue());
    }

    @Test
    @Tag("unstable")
    public void testCBRT() {

        this.assertUnary(BigFunction.CBRT, ComplexFunction.CBRT, PrimitiveFunction.CBRT, QuaternionFunction.CBRT, RationalFunction.CBRT,
                POSITIVE.doubleValue());

        this.assertUnary(BigFunction.CBRT, ComplexFunction.CBRT, PrimitiveFunction.CBRT, QuaternionFunction.CBRT, RationalFunction.CBRT, PI);
        this.assertUnary(BigFunction.CBRT, ComplexFunction.CBRT, PrimitiveFunction.CBRT, QuaternionFunction.CBRT, RationalFunction.CBRT, E);
        this.assertUnary(BigFunction.CBRT, ComplexFunction.CBRT, PrimitiveFunction.CBRT, QuaternionFunction.CBRT, RationalFunction.CBRT, TWO);
        this.assertUnary(BigFunction.CBRT, ComplexFunction.CBRT, PrimitiveFunction.CBRT, QuaternionFunction.CBRT, RationalFunction.CBRT, HALF_PI);
        this.assertUnary(BigFunction.CBRT, ComplexFunction.CBRT, PrimitiveFunction.CBRT, QuaternionFunction.CBRT, RationalFunction.CBRT, ONE);

        this.assertUnary(BigFunction.CBRT, ComplexFunction.CBRT, PrimitiveFunction.CBRT, QuaternionFunction.CBRT, RationalFunction.CBRT, ZERO);
    }

    @Test
    public void testCEIL() {
        this.assertUnary(BigFunction.CEIL, ComplexFunction.CEIL, PrimitiveFunction.CEIL, QuaternionFunction.CEIL, RationalFunction.CEIL,
                AROUND_ZERO.doubleValue());
        this.assertUnary(BigFunction.CEIL, ComplexFunction.CEIL, PrimitiveFunction.CEIL, QuaternionFunction.CEIL, RationalFunction.CEIL, HALF);
        this.assertUnary(BigFunction.CEIL, ComplexFunction.CEIL, PrimitiveFunction.CEIL, QuaternionFunction.CEIL, RationalFunction.CEIL, -HALF);
    }

    @Test
    public void testCONJUGATE() {
        this.assertUnary(BigFunction.CONJUGATE, ComplexFunction.CONJUGATE, PrimitiveFunction.CONJUGATE, QuaternionFunction.CONJUGATE,
                RationalFunction.CONJUGATE, AROUND_ZERO.doubleValue());
    }

    @Test
    public void testCOS() {

        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, QuaternionFunction.COS, RationalFunction.COS, AROUND_ZERO.doubleValue());

        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, QuaternionFunction.COS, RationalFunction.COS, PI);
        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, QuaternionFunction.COS, RationalFunction.COS, E);
        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, QuaternionFunction.COS, RationalFunction.COS, TWO);
        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, QuaternionFunction.COS, RationalFunction.COS, HALF_PI);
        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, QuaternionFunction.COS, RationalFunction.COS, ONE);

        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, QuaternionFunction.COS, RationalFunction.COS, ZERO);

        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, QuaternionFunction.COS, RationalFunction.COS, NEG);
        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, QuaternionFunction.COS, RationalFunction.COS, -HALF_PI);
        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, QuaternionFunction.COS, RationalFunction.COS, -TWO);
        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, QuaternionFunction.COS, RationalFunction.COS, -E);
        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, QuaternionFunction.COS, RationalFunction.COS, -PI);
    }

    @Test
    public void testCOSH() {

        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, QuaternionFunction.COSH, RationalFunction.COSH,
                AROUND_ZERO.doubleValue());

        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, QuaternionFunction.COSH, RationalFunction.COSH, PI);
        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, QuaternionFunction.COSH, RationalFunction.COSH, E);
        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, QuaternionFunction.COSH, RationalFunction.COSH, TWO);
        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, QuaternionFunction.COSH, RationalFunction.COSH, HALF_PI);
        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, QuaternionFunction.COSH, RationalFunction.COSH, ONE);

        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, QuaternionFunction.COSH, RationalFunction.COSH, ZERO);

        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, QuaternionFunction.COSH, RationalFunction.COSH, NEG);
        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, QuaternionFunction.COSH, RationalFunction.COSH, -HALF_PI);
        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, QuaternionFunction.COSH, RationalFunction.COSH, -TWO);
        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, QuaternionFunction.COSH, RationalFunction.COSH, -E);
        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, QuaternionFunction.COSH, RationalFunction.COSH, -PI);
    }

    @Test
    public void testDIVIDE() {
        this.assertBinary(BigFunction.DIVIDE, ComplexFunction.DIVIDE, PrimitiveFunction.DIVIDE, QuaternionFunction.DIVIDE, RationalFunction.DIVIDE,
                POSITIVE.doubleValue(), POSITIVE.doubleValue());
    }

    @Test
    public void testEXP() {

        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, QuaternionFunction.EXP, RationalFunction.EXP, TEN);

        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, QuaternionFunction.EXP, RationalFunction.EXP, PI);
        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, QuaternionFunction.EXP, RationalFunction.EXP, E);
        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, QuaternionFunction.EXP, RationalFunction.EXP, TWO);
        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, QuaternionFunction.EXP, RationalFunction.EXP, HALF_PI);
        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, QuaternionFunction.EXP, RationalFunction.EXP, ONE);

        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, QuaternionFunction.EXP, RationalFunction.EXP, MACHINE_SMALLEST);
        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, QuaternionFunction.EXP, RationalFunction.EXP, ZERO);
        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, QuaternionFunction.EXP, RationalFunction.EXP, -MACHINE_SMALLEST);

        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, QuaternionFunction.EXP, RationalFunction.EXP, NEG);
        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, QuaternionFunction.EXP, RationalFunction.EXP, -HALF_PI);
        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, QuaternionFunction.EXP, RationalFunction.EXP, -TWO);
        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, QuaternionFunction.EXP, RationalFunction.EXP, -E);
        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, QuaternionFunction.EXP, RationalFunction.EXP, -PI);

        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, QuaternionFunction.EXP, RationalFunction.EXP, -TEN);

        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, QuaternionFunction.EXP, RationalFunction.EXP, -HUNDRED);

    }

    @Test
    public void testEXPM1() {
        this.assertUnary(BigFunction.EXPM1, ComplexFunction.EXPM1, PrimitiveFunction.EXPM1, QuaternionFunction.EXPM1, RationalFunction.EXPM1,
                AROUND_ZERO.doubleValue());
    }

    @Test
    public void testFLOOR() {
        this.assertUnary(BigFunction.FLOOR, ComplexFunction.FLOOR, PrimitiveFunction.FLOOR, QuaternionFunction.FLOOR, RationalFunction.FLOOR,
                AROUND_ZERO.doubleValue());
        this.assertUnary(BigFunction.FLOOR, ComplexFunction.FLOOR, PrimitiveFunction.FLOOR, QuaternionFunction.FLOOR, RationalFunction.FLOOR, HALF);
        this.assertUnary(BigFunction.FLOOR, ComplexFunction.FLOOR, PrimitiveFunction.FLOOR, QuaternionFunction.FLOOR, RationalFunction.FLOOR, -HALF);
    }

    @Test
    public void testHYPOT() {
        this.assertBinary(BigFunction.HYPOT, ComplexFunction.HYPOT, PrimitiveFunction.HYPOT, QuaternionFunction.HYPOT, RationalFunction.HYPOT,
                POSITIVE.doubleValue(), POSITIVE.doubleValue());
    }

    @Test
    public void testINVERT() {
        this.assertUnary(BigFunction.INVERT, ComplexFunction.INVERT, PrimitiveFunction.INVERT, QuaternionFunction.INVERT, RationalFunction.INVERT,
                POSITIVE.doubleValue());
    }

    @Test
    public void testLOG() {

        this.assertUnary(BigFunction.LOG, ComplexFunction.LOG, PrimitiveFunction.LOG, QuaternionFunction.LOG, RationalFunction.LOG, THOUSAND);
        this.assertUnary(BigFunction.LOG, ComplexFunction.LOG, PrimitiveFunction.LOG, QuaternionFunction.LOG, RationalFunction.LOG, HUNDRED);
        this.assertUnary(BigFunction.LOG, ComplexFunction.LOG, PrimitiveFunction.LOG, QuaternionFunction.LOG, RationalFunction.LOG, TEN);
        this.assertUnary(BigFunction.LOG, ComplexFunction.LOG, PrimitiveFunction.LOG, QuaternionFunction.LOG, RationalFunction.LOG, ONE);
        this.assertUnary(BigFunction.LOG, ComplexFunction.LOG, PrimitiveFunction.LOG, QuaternionFunction.LOG, RationalFunction.LOG, TENTH);
        this.assertUnary(BigFunction.LOG, ComplexFunction.LOG, PrimitiveFunction.LOG, QuaternionFunction.LOG, RationalFunction.LOG, HUNDREDTH);
        this.assertUnary(BigFunction.LOG, ComplexFunction.LOG, PrimitiveFunction.LOG, QuaternionFunction.LOG, RationalFunction.LOG, THOUSANDTH);

    }

    @Test
    public void testLOG10() {
        this.assertUnary(BigFunction.LOG10, ComplexFunction.LOG10, PrimitiveFunction.LOG10, QuaternionFunction.LOG10, RationalFunction.LOG10,
                POSITIVE.doubleValue());
    }

    @Test
    public void testLOG1P() {
        this.assertUnary(BigFunction.LOG1P, ComplexFunction.LOG1P, PrimitiveFunction.LOG1P, QuaternionFunction.LOG1P, RationalFunction.LOG1P,
                POSITIVE.doubleValue());
    }

    @Test
    @Tag("unstable")
    public void testMAX() {
        this.assertBinary(BigFunction.MAX, ComplexFunction.MAX, PrimitiveFunction.MAX, QuaternionFunction.MAX, RationalFunction.MAX, AROUND_ZERO.doubleValue(),
                POSITIVE.doubleValue());
    }

    @Test
    @Tag("unstable")
    public void testMIN() {
        this.assertBinary(BigFunction.MIN, ComplexFunction.MIN, PrimitiveFunction.MIN, QuaternionFunction.MIN, RationalFunction.MIN, AROUND_ZERO.doubleValue(),
                POSITIVE.doubleValue());
    }

    @Test
    public void testMULTIPLY() {
        this.assertBinary(BigFunction.MULTIPLY, ComplexFunction.MULTIPLY, PrimitiveFunction.MULTIPLY, QuaternionFunction.MULTIPLY, RationalFunction.MULTIPLY,
                AROUND_ZERO.doubleValue(), POSITIVE.doubleValue());
    }

    @Test
    public void testNEGATE() {
        this.assertUnary(BigFunction.NEGATE, ComplexFunction.NEGATE, PrimitiveFunction.NEGATE, QuaternionFunction.NEGATE, RationalFunction.NEGATE,
                AROUND_ZERO.doubleValue());
    }

    @Test
    public void testPOW() {

        // Only defined for non-negative (absolute) first aruments

        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, QuaternionFunction.POW, RationalFunction.POW, HUNDREDTH, NINE);
        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, QuaternionFunction.POW, RationalFunction.POW, THOUSANDTH, SEVEN);
        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, QuaternionFunction.POW, RationalFunction.POW, TENTH, EIGHT);
        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, QuaternionFunction.POW, RationalFunction.POW, ONE, SIX);
        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, QuaternionFunction.POW, RationalFunction.POW, HALF_PI, FIVE);
        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, QuaternionFunction.POW, RationalFunction.POW, TWO, FOUR);
        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, QuaternionFunction.POW, RationalFunction.POW, E, THREE);
        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, QuaternionFunction.POW, RationalFunction.POW, PI, TWO);
        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, QuaternionFunction.POW, RationalFunction.POW, SQRT_PI, ONE);

        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, QuaternionFunction.POW, RationalFunction.POW, SQRT_TWO_PI, SQRT_TWO_PI);
        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, QuaternionFunction.POW, RationalFunction.POW, SQRT_TWO_PI, ZERO);
        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, QuaternionFunction.POW, RationalFunction.POW, THOUSANDTH, SQRT_TWO_PI);
        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, QuaternionFunction.POW, RationalFunction.POW, THOUSANDTH, ZERO);

        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, QuaternionFunction.POW, RationalFunction.POW, SQRT_TWO_PI, -SQRT_TWO_PI);
        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, QuaternionFunction.POW, RationalFunction.POW, SQRT_TWO_PI, -ZERO);
        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, QuaternionFunction.POW, RationalFunction.POW, THOUSANDTH, -SQRT_TWO_PI);
        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, QuaternionFunction.POW, RationalFunction.POW, THOUSANDTH, -ZERO);

    }

    @Test
    public void testPOWER() {

        //        this.assertParameter(BigFunction.POWER, ComplexFunction.POWER, PrimitiveFunction.POWER, QuaternionFunction.POWER, RationalFunction.POWER,
        //                AROUND_ZERO.doubleValue(), Uniform.randomInteger(1, 10));

        this.assertParameter(BigFunction.POWER, ComplexFunction.POWER, PrimitiveFunction.POWER, QuaternionFunction.POWER, RationalFunction.POWER, PI, 2);
        this.assertParameter(BigFunction.POWER, ComplexFunction.POWER, PrimitiveFunction.POWER, QuaternionFunction.POWER, RationalFunction.POWER, E, 3);
        this.assertParameter(BigFunction.POWER, ComplexFunction.POWER, PrimitiveFunction.POWER, QuaternionFunction.POWER, RationalFunction.POWER, TWO, 4);
        this.assertParameter(BigFunction.POWER, ComplexFunction.POWER, PrimitiveFunction.POWER, QuaternionFunction.POWER, RationalFunction.POWER, HALF_PI, 5);
        this.assertParameter(BigFunction.POWER, ComplexFunction.POWER, PrimitiveFunction.POWER, QuaternionFunction.POWER, RationalFunction.POWER, ONE, 6);

        this.assertParameter(BigFunction.POWER, ComplexFunction.POWER, PrimitiveFunction.POWER, QuaternionFunction.POWER, RationalFunction.POWER, ZERO, 7);
    }

    @Test
    public void testRINT() {

        this.assertUnary(BigFunction.RINT, ComplexFunction.RINT, PrimitiveFunction.RINT, QuaternionFunction.RINT, RationalFunction.RINT,
                AROUND_ZERO.doubleValue());

        this.assertUnary(BigFunction.RINT, ComplexFunction.RINT, PrimitiveFunction.RINT, QuaternionFunction.RINT, RationalFunction.RINT, HALF);

        this.assertUnary(BigFunction.RINT, ComplexFunction.RINT, PrimitiveFunction.RINT, QuaternionFunction.RINT, RationalFunction.RINT, -HALF);

        this.assertUnary(BigFunction.RINT, ComplexFunction.RINT, PrimitiveFunction.RINT, QuaternionFunction.RINT, RationalFunction.RINT, ONE + HALF);

        this.assertUnary(BigFunction.RINT, ComplexFunction.RINT, PrimitiveFunction.RINT, QuaternionFunction.RINT, RationalFunction.RINT, -(ONE + HALF));

    }

    @Test
    @Tag("unstable")
    public void testROOT() {
        this.assertParameter(BigFunction.ROOT, ComplexFunction.ROOT, PrimitiveFunction.ROOT, QuaternionFunction.ROOT, RationalFunction.ROOT,
                POSITIVE.doubleValue(), Uniform.randomInteger(1, 10));
    }

    @Test
    public void testSCALE() {
        this.assertParameter(BigFunction.SCALE, ComplexFunction.SCALE, PrimitiveFunction.SCALE, QuaternionFunction.SCALE, RationalFunction.SCALE,
                AROUND_ZERO.doubleValue(), Uniform.randomInteger(1, 10));
    }

    @Test
    public void testSIGNUM() {
        this.assertUnary(BigFunction.SIGNUM, ComplexFunction.SIGNUM, PrimitiveFunction.SIGNUM, QuaternionFunction.SIGNUM, RationalFunction.SIGNUM,
                AROUND_ZERO.doubleValue());
    }

    @Test
    public void testSIN() {

        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, QuaternionFunction.SIN, RationalFunction.SIN, AROUND_ZERO.doubleValue());

        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, QuaternionFunction.SIN, RationalFunction.SIN, PI);
        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, QuaternionFunction.SIN, RationalFunction.SIN, E);
        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, QuaternionFunction.SIN, RationalFunction.SIN, TWO);
        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, QuaternionFunction.SIN, RationalFunction.SIN, HALF_PI);
        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, QuaternionFunction.SIN, RationalFunction.SIN, ONE);

        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, QuaternionFunction.SIN, RationalFunction.SIN, ZERO);

        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, QuaternionFunction.SIN, RationalFunction.SIN, NEG);
        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, QuaternionFunction.SIN, RationalFunction.SIN, -HALF_PI);
        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, QuaternionFunction.SIN, RationalFunction.SIN, -TWO);
        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, QuaternionFunction.SIN, RationalFunction.SIN, -E);
        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, QuaternionFunction.SIN, RationalFunction.SIN, -PI);
    }

    @Test
    public void testSINH() {

        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, QuaternionFunction.SINH, RationalFunction.SINH,
                AROUND_ZERO.doubleValue());

        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, QuaternionFunction.SINH, RationalFunction.SINH, PI);
        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, QuaternionFunction.SINH, RationalFunction.SINH, E);
        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, QuaternionFunction.SINH, RationalFunction.SINH, TWO);
        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, QuaternionFunction.SINH, RationalFunction.SINH, HALF_PI);
        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, QuaternionFunction.SINH, RationalFunction.SINH, ONE);

        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, QuaternionFunction.SINH, RationalFunction.SINH, ZERO);

        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, QuaternionFunction.SINH, RationalFunction.SINH, NEG);
        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, QuaternionFunction.SINH, RationalFunction.SINH, -HALF_PI);
        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, QuaternionFunction.SINH, RationalFunction.SINH, -TWO);
        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, QuaternionFunction.SINH, RationalFunction.SINH, -E);
        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, QuaternionFunction.SINH, RationalFunction.SINH, -PI);
    }

    @Test
    public void testSQRT() {

        this.assertUnary(BigFunction.SQRT, ComplexFunction.SQRT, PrimitiveFunction.SQRT, QuaternionFunction.SQRT, RationalFunction.SQRT,
                POSITIVE.doubleValue());

        this.assertUnary(BigFunction.SQRT, ComplexFunction.SQRT, PrimitiveFunction.SQRT, QuaternionFunction.SQRT, RationalFunction.SQRT, PI);
        this.assertUnary(BigFunction.SQRT, ComplexFunction.SQRT, PrimitiveFunction.SQRT, QuaternionFunction.SQRT, RationalFunction.SQRT, E);
        this.assertUnary(BigFunction.SQRT, ComplexFunction.SQRT, PrimitiveFunction.SQRT, QuaternionFunction.SQRT, RationalFunction.SQRT, TWO);
        this.assertUnary(BigFunction.SQRT, ComplexFunction.SQRT, PrimitiveFunction.SQRT, QuaternionFunction.SQRT, RationalFunction.SQRT, HALF_PI);
        this.assertUnary(BigFunction.SQRT, ComplexFunction.SQRT, PrimitiveFunction.SQRT, QuaternionFunction.SQRT, RationalFunction.SQRT, ONE);

        this.assertUnary(BigFunction.SQRT, ComplexFunction.SQRT, PrimitiveFunction.SQRT, QuaternionFunction.SQRT, RationalFunction.SQRT, ZERO);
    }

    @Test
    public void testSQRT1PX2() {
        this.assertUnary(BigFunction.SQRT1PX2, ComplexFunction.SQRT1PX2, PrimitiveFunction.SQRT1PX2, QuaternionFunction.SQRT1PX2, RationalFunction.SQRT1PX2,
                POSITIVE.doubleValue());
    }

    @Test
    public void testSUBTRACT() {
        this.assertBinary(BigFunction.SUBTRACT, ComplexFunction.SUBTRACT, PrimitiveFunction.SUBTRACT, QuaternionFunction.SUBTRACT, RationalFunction.SUBTRACT,
                POSITIVE.doubleValue(), POSITIVE.doubleValue());
    }

    @Test
    public void testTAN() {

        this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, QuaternionFunction.TAN, RationalFunction.TAN, AROUND_ZERO.doubleValue());

        this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, QuaternionFunction.TAN, RationalFunction.TAN, PI);
        this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, QuaternionFunction.TAN, RationalFunction.TAN, E);
        this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, QuaternionFunction.TAN, RationalFunction.TAN, TWO);
        //     this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, RationalFunction.TAN, HALF_PI);
        this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, QuaternionFunction.TAN, RationalFunction.TAN, ONE);

        this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, QuaternionFunction.TAN, RationalFunction.TAN, ZERO);

        this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, QuaternionFunction.TAN, RationalFunction.TAN, NEG);
        //    this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, RationalFunction.TAN, -HALF_PI);
        this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, QuaternionFunction.TAN, RationalFunction.TAN, -TWO);
        this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, QuaternionFunction.TAN, RationalFunction.TAN, -E);
        this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, QuaternionFunction.TAN, RationalFunction.TAN, -PI);
    }

    @Test
    public void testTANH() {

        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, QuaternionFunction.TANH, RationalFunction.TANH,
                AROUND_ZERO.doubleValue());

        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, QuaternionFunction.TANH, RationalFunction.TANH, PI);
        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, QuaternionFunction.TANH, RationalFunction.TANH, E);
        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, QuaternionFunction.TANH, RationalFunction.TANH, TWO);
        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, QuaternionFunction.TANH, RationalFunction.TANH, HALF_PI);
        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, QuaternionFunction.TANH, RationalFunction.TANH, ONE);

        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, QuaternionFunction.TANH, RationalFunction.TANH, ZERO);

        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, QuaternionFunction.TANH, RationalFunction.TANH, NEG);
        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, QuaternionFunction.TANH, RationalFunction.TANH, -HALF_PI);
        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, QuaternionFunction.TANH, RationalFunction.TANH, -TWO);
        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, QuaternionFunction.TANH, RationalFunction.TANH, -E);
        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, QuaternionFunction.TANH, RationalFunction.TANH, -PI);
    }

    @Test
    public void testVALUE() {
        this.assertUnary(BigFunction.VALUE, ComplexFunction.VALUE, PrimitiveFunction.VALUE, QuaternionFunction.VALUE, RationalFunction.VALUE,
                AROUND_ZERO.doubleValue());
    }

    private void assertBinary(final BinaryFunction<BigDecimal> big, final BinaryFunction<ComplexNumber> complex, final BinaryFunction<Double> primitive,
            final BinaryFunction<Quaternion> quaternion, final BinaryFunction<RationalNumber> rational, final double arg1, final double arg2) {

        TestUtils.assertEquals("Big vs Complex, " + arg1 + ", " + arg2, big.invoke(arg1, arg2), complex.invoke(arg1, arg2), CONTEXT);
        TestUtils.assertEquals("Complex vs Primitive, " + arg1 + ", " + arg2, complex.invoke(arg1, arg2), primitive.invoke(arg1, arg2), CONTEXT);
        TestUtils.assertEquals("Primitive vs Quaternion, " + arg1 + ", " + arg2, primitive.invoke(arg1, arg2), quaternion.invoke(arg1, arg2), CONTEXT);
        TestUtils.assertEquals("Quaternion vs Rational, " + arg1 + ", " + arg2, quaternion.invoke(arg1, arg2), rational.invoke(arg1, arg2), CONTEXT);
        TestUtils.assertEquals("Rational vs Big, " + arg1 + ", " + arg2, rational.invoke(arg1, arg2), big.invoke(arg1, arg2), CONTEXT);
    }

    private void assertParameter(final ParameterFunction<BigDecimal> big, final ParameterFunction<ComplexNumber> complex,
            final ParameterFunction<Double> primitive, final ParameterFunction<Quaternion> quaternion, final ParameterFunction<RationalNumber> rational,
            final double arg, final int param) {

        TestUtils.assertEquals("Big vs Complex", big.invoke(arg, param), complex.invoke(arg, param), CONTEXT);
        TestUtils.assertEquals("Complex vs Primitive", complex.invoke(arg, param), primitive.invoke(arg, param), CONTEXT);
        TestUtils.assertEquals("Primitive vs Quaternion", primitive.invoke(arg, param), quaternion.invoke(arg, param), CONTEXT);
        TestUtils.assertEquals("Quaternion vs Rational", quaternion.invoke(arg, param), rational.invoke(arg, param), CONTEXT);
        TestUtils.assertEquals("Rational vs Big", rational.invoke(arg, param), big.invoke(arg, param), CONTEXT);
    }

    private void assertUnary(final UnaryFunction<BigDecimal> big, final UnaryFunction<ComplexNumber> complex, final UnaryFunction<Double> primitive,
            final UnaryFunction<Quaternion> quaternion, final UnaryFunction<RationalNumber> rational, final double arg) {

        TestUtils.assertEquals("Big vs Complex, " + arg, big.invoke(arg), complex.invoke(arg), CONTEXT);
        TestUtils.assertEquals("Complex vs Primitive, " + arg, complex.invoke(arg), primitive.invoke(arg), CONTEXT);
        TestUtils.assertEquals("Primitive vs Quaternion, " + arg, primitive.invoke(arg), quaternion.invoke(arg), CONTEXT);
        TestUtils.assertEquals("Quaternion vs Rational, " + arg, quaternion.invoke(arg), rational.invoke(arg), CONTEXT);
        TestUtils.assertEquals("Rational vs Big, " + arg, rational.invoke(arg), big.invoke(arg), CONTEXT);
    }

}
