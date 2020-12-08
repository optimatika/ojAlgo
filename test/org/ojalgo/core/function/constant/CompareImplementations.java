/*
 * Copyright 1997-2020 Optimatika
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
package org.ojalgo.core.function.constant;

import static org.ojalgo.core.function.constant.PrimitiveMath.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.core.TestUtils;
import org.ojalgo.core.function.BinaryFunction;
import org.ojalgo.core.function.ParameterFunction;
import org.ojalgo.core.function.UnaryFunction;
import org.ojalgo.core.random.Uniform;
import org.ojalgo.core.scalar.ComplexNumber;
import org.ojalgo.core.scalar.Quaternion;
import org.ojalgo.core.scalar.RationalNumber;
import org.ojalgo.core.type.context.NumberContext;

/**
 * Checks that {@linkplain org.ojalgo.core.function.BigFunction}, {@linkplain org.ojalgo.core.function.ComplexFunction},
 * {@linkplain org.ojalgo.core.function.PrimitiveFunction} and {@linkplain org.ojalgo.core.function.RationalFunction}
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
        this.assertUnary(BigMath.ABS, ComplexMath.ABS, PrimitiveMath.ABS, QuaternionMath.ABS, RationalMath.ABS, AROUND_ZERO.doubleValue());
    }

    @Test
    public void testACOS() {

        this.assertUnary(BigMath.ACOS, ComplexMath.ACOS, PrimitiveMath.ACOS, QuaternionMath.ACOS, RationalMath.ACOS, NEG);
        this.assertUnary(BigMath.ACOS, ComplexMath.ACOS, PrimitiveMath.ACOS, QuaternionMath.ACOS, RationalMath.ACOS, NEG / SQRT_TWO);
        this.assertUnary(BigMath.ACOS, ComplexMath.ACOS, PrimitiveMath.ACOS, QuaternionMath.ACOS, RationalMath.ACOS, -HALF);
        this.assertUnary(BigMath.ACOS, ComplexMath.ACOS, PrimitiveMath.ACOS, QuaternionMath.ACOS, RationalMath.ACOS, ZERO);
        this.assertUnary(BigMath.ACOS, ComplexMath.ACOS, PrimitiveMath.ACOS, QuaternionMath.ACOS, RationalMath.ACOS, HALF);
        this.assertUnary(BigMath.ACOS, ComplexMath.ACOS, PrimitiveMath.ACOS, QuaternionMath.ACOS, RationalMath.ACOS, ONE / SQRT_TWO);
        this.assertUnary(BigMath.ACOS, ComplexMath.ACOS, PrimitiveMath.ACOS, QuaternionMath.ACOS, RationalMath.ACOS, ONE);

    }

    @Test
    public void testACOSH() {

        //        this.assertUnary(BigFunction.ACOSH, ComplexFunction.ACOSH, PrimitiveFunction.ACOSH, RationalFunction.ACOSH, POSITIVE.doubleValue());

        this.assertUnary(BigMath.ACOSH, ComplexMath.ACOSH, PrimitiveMath.ACOSH, QuaternionMath.ACOSH, RationalMath.ACOSH, PI);
        this.assertUnary(BigMath.ACOSH, ComplexMath.ACOSH, PrimitiveMath.ACOSH, QuaternionMath.ACOSH, RationalMath.ACOSH, E);
        this.assertUnary(BigMath.ACOSH, ComplexMath.ACOSH, PrimitiveMath.ACOSH, QuaternionMath.ACOSH, RationalMath.ACOSH, TWO);
        this.assertUnary(BigMath.ACOSH, ComplexMath.ACOSH, PrimitiveMath.ACOSH, QuaternionMath.ACOSH, RationalMath.ACOSH, HALF_PI);
        this.assertUnary(BigMath.ACOSH, ComplexMath.ACOSH, PrimitiveMath.ACOSH, QuaternionMath.ACOSH, RationalMath.ACOSH, ONE);
    }

    @Test
    public void testADD() {
        this.assertBinary(BigMath.ADD, ComplexMath.ADD, PrimitiveMath.ADD, QuaternionMath.ADD, RationalMath.ADD, POSITIVE.doubleValue(),
                POSITIVE.doubleValue());
    }

    @Test
    public void testASIN() {

        this.assertUnary(BigMath.ASIN, ComplexMath.ASIN, PrimitiveMath.ASIN, QuaternionMath.ASIN, RationalMath.ASIN, AROUND_ZERO.doubleValue());

        this.assertUnary(BigMath.ASIN, ComplexMath.ASIN, PrimitiveMath.ASIN, QuaternionMath.ASIN, RationalMath.ASIN, ONE);
        this.assertUnary(BigMath.ASIN, ComplexMath.ASIN, PrimitiveMath.ASIN, QuaternionMath.ASIN, RationalMath.ASIN, ZERO);
        this.assertUnary(BigMath.ASIN, ComplexMath.ASIN, PrimitiveMath.ASIN, QuaternionMath.ASIN, RationalMath.ASIN, NEG);
    }

    @Test
    public void testASINH() {

        this.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, AROUND_ZERO.doubleValue());

        this.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, PI);
        this.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, E);
        this.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, TWO);
        this.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, HALF_PI);
        this.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, ONE);

        this.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, ZERO);

        this.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, NEG);
        this.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, -HALF_PI);
        this.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, -TWO);
        this.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, -E);
        this.assertUnary(BigMath.ASINH, ComplexMath.ASINH, PrimitiveMath.ASINH, QuaternionMath.ASINH, RationalMath.ASINH, -PI);
    }

    @Test
    public void testATAN() {

        this.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, AROUND_ZERO.doubleValue());

        //        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, RationalFunction.ATAN, MAX_VALUE);
        this.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, PI);
        this.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, E);
        this.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, TWO);
        this.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, HALF_PI);
        this.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, ONE);

        this.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, AROUND_ZERO.doubleValue());

        this.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, NEG);
        this.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, -HALF_PI);
        this.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, -TWO);
        this.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, -E);
        this.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, -PI);
        this.assertUnary(BigMath.ATAN, ComplexMath.ATAN, PrimitiveMath.ATAN, QuaternionMath.ATAN, RationalMath.ATAN, MACHINE_SMALLEST);
    }

    @Test
    public void testATAN2() {

        this.assertBinary(BigMath.ATAN2, ComplexMath.ATAN2, PrimitiveMath.ATAN2, QuaternionMath.ATAN2, RationalMath.ATAN2, ONE, TWO);
        this.assertBinary(BigMath.ATAN2, ComplexMath.ATAN2, PrimitiveMath.ATAN2, QuaternionMath.ATAN2, RationalMath.ATAN2, TWO, ONE);
        this.assertBinary(BigMath.ATAN2, ComplexMath.ATAN2, PrimitiveMath.ATAN2, QuaternionMath.ATAN2, RationalMath.ATAN2, ONE, HALF);
        this.assertBinary(BigMath.ATAN2, ComplexMath.ATAN2, PrimitiveMath.ATAN2, QuaternionMath.ATAN2, RationalMath.ATAN2, HALF, ONE);

        this.assertBinary(BigMath.ATAN2, ComplexMath.ATAN2, PrimitiveMath.ATAN2, QuaternionMath.ATAN2, RationalMath.ATAN2, ZERO, ONE);

    }

    @Test
    public void testATANH() {

        this.assertUnary(BigMath.ATANH, ComplexMath.ATANH, PrimitiveMath.ATANH, QuaternionMath.ATANH, RationalMath.ATANH, AROUND_ZERO.doubleValue());

        //    this.assertUnary(BigFunction.ATANH, ComplexFunction.ATANH, PrimitiveFunction.ATANH, RationalFunction.ATANH, ONE);
        this.assertUnary(BigMath.ATANH, ComplexMath.ATANH, PrimitiveMath.ATANH, QuaternionMath.ATANH, RationalMath.ATANH, ZERO);
        //  this.assertUnary(BigFunction.ATANH, ComplexFunction.ATANH, PrimitiveFunction.ATANH, RationalFunction.ATANH, NEG);
    }

    @Test
    public void testCARDINALITY() {
        this.assertUnary(BigMath.CARDINALITY, ComplexMath.CARDINALITY, PrimitiveMath.CARDINALITY, QuaternionMath.CARDINALITY, RationalMath.CARDINALITY,
                AROUND_ZERO.doubleValue());
    }

    @Test
    @Tag("unstable")
    public void testCBRT() {

        this.assertUnary(BigMath.CBRT, ComplexMath.CBRT, PrimitiveMath.CBRT, QuaternionMath.CBRT, RationalMath.CBRT, POSITIVE.doubleValue());

        this.assertUnary(BigMath.CBRT, ComplexMath.CBRT, PrimitiveMath.CBRT, QuaternionMath.CBRT, RationalMath.CBRT, PI);
        this.assertUnary(BigMath.CBRT, ComplexMath.CBRT, PrimitiveMath.CBRT, QuaternionMath.CBRT, RationalMath.CBRT, E);
        this.assertUnary(BigMath.CBRT, ComplexMath.CBRT, PrimitiveMath.CBRT, QuaternionMath.CBRT, RationalMath.CBRT, TWO);
        this.assertUnary(BigMath.CBRT, ComplexMath.CBRT, PrimitiveMath.CBRT, QuaternionMath.CBRT, RationalMath.CBRT, HALF_PI);
        this.assertUnary(BigMath.CBRT, ComplexMath.CBRT, PrimitiveMath.CBRT, QuaternionMath.CBRT, RationalMath.CBRT, ONE);

        this.assertUnary(BigMath.CBRT, ComplexMath.CBRT, PrimitiveMath.CBRT, QuaternionMath.CBRT, RationalMath.CBRT, ZERO);
    }

    @Test
    public void testCEIL() {
        this.assertUnary(BigMath.CEIL, ComplexMath.CEIL, PrimitiveMath.CEIL, QuaternionMath.CEIL, RationalMath.CEIL, AROUND_ZERO.doubleValue());
        this.assertUnary(BigMath.CEIL, ComplexMath.CEIL, PrimitiveMath.CEIL, QuaternionMath.CEIL, RationalMath.CEIL, HALF);
        this.assertUnary(BigMath.CEIL, ComplexMath.CEIL, PrimitiveMath.CEIL, QuaternionMath.CEIL, RationalMath.CEIL, -HALF);
    }

    @Test
    public void testCONJUGATE() {
        this.assertUnary(BigMath.CONJUGATE, ComplexMath.CONJUGATE, PrimitiveMath.CONJUGATE, QuaternionMath.CONJUGATE, RationalMath.CONJUGATE,
                AROUND_ZERO.doubleValue());
    }

    @Test
    public void testCOS() {

        this.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, AROUND_ZERO.doubleValue());

        this.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, PI);
        this.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, E);
        this.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, TWO);
        this.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, HALF_PI);
        this.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, ONE);

        this.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, ZERO);

        this.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, NEG);
        this.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, -HALF_PI);
        this.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, -TWO);
        this.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, -E);
        this.assertUnary(BigMath.COS, ComplexMath.COS, PrimitiveMath.COS, QuaternionMath.COS, RationalMath.COS, -PI);
    }

    @Test
    public void testCOSH() {

        this.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, AROUND_ZERO.doubleValue());

        this.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, PI);
        this.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, E);
        this.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, TWO);
        this.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, HALF_PI);
        this.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, ONE);

        this.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, ZERO);

        this.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, NEG);
        this.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, -HALF_PI);
        this.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, -TWO);
        this.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, -E);
        this.assertUnary(BigMath.COSH, ComplexMath.COSH, PrimitiveMath.COSH, QuaternionMath.COSH, RationalMath.COSH, -PI);
    }

    @Test
    public void testDIVIDE() {
        this.assertBinary(BigMath.DIVIDE, ComplexMath.DIVIDE, PrimitiveMath.DIVIDE, QuaternionMath.DIVIDE, RationalMath.DIVIDE, POSITIVE.doubleValue(),
                POSITIVE.doubleValue());
    }

    @Test
    public void testEXP() {

        this.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, TEN);

        this.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, PI);
        this.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, E);
        this.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, TWO);
        this.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, HALF_PI);
        this.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, ONE);

        this.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, MACHINE_SMALLEST);
        this.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, ZERO);
        this.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, -MACHINE_SMALLEST);

        this.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, NEG);
        this.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, -HALF_PI);
        this.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, -TWO);
        this.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, -E);
        this.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, -PI);

        this.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, -TEN);

        this.assertUnary(BigMath.EXP, ComplexMath.EXP, PrimitiveMath.EXP, QuaternionMath.EXP, RationalMath.EXP, -HUNDRED);

    }

    @Test
    public void testEXPM1() {
        this.assertUnary(BigMath.EXPM1, ComplexMath.EXPM1, PrimitiveMath.EXPM1, QuaternionMath.EXPM1, RationalMath.EXPM1, AROUND_ZERO.doubleValue());
    }

    @Test
    public void testFLOOR() {
        this.assertUnary(BigMath.FLOOR, ComplexMath.FLOOR, PrimitiveMath.FLOOR, QuaternionMath.FLOOR, RationalMath.FLOOR, AROUND_ZERO.doubleValue());
        this.assertUnary(BigMath.FLOOR, ComplexMath.FLOOR, PrimitiveMath.FLOOR, QuaternionMath.FLOOR, RationalMath.FLOOR, HALF);
        this.assertUnary(BigMath.FLOOR, ComplexMath.FLOOR, PrimitiveMath.FLOOR, QuaternionMath.FLOOR, RationalMath.FLOOR, -HALF);
    }

    @Test
    public void testHYPOT() {
        this.assertBinary(BigMath.HYPOT, ComplexMath.HYPOT, PrimitiveMath.HYPOT, QuaternionMath.HYPOT, RationalMath.HYPOT, POSITIVE.doubleValue(),
                POSITIVE.doubleValue());
    }

    @Test
    public void testINVERT() {
        this.assertUnary(BigMath.INVERT, ComplexMath.INVERT, PrimitiveMath.INVERT, QuaternionMath.INVERT, RationalMath.INVERT, POSITIVE.doubleValue());
    }

    @Test
    public void testLOG() {

        this.assertUnary(BigMath.LOG, ComplexMath.LOG, PrimitiveMath.LOG, QuaternionMath.LOG, RationalMath.LOG, THOUSAND);
        this.assertUnary(BigMath.LOG, ComplexMath.LOG, PrimitiveMath.LOG, QuaternionMath.LOG, RationalMath.LOG, HUNDRED);
        this.assertUnary(BigMath.LOG, ComplexMath.LOG, PrimitiveMath.LOG, QuaternionMath.LOG, RationalMath.LOG, TEN);
        this.assertUnary(BigMath.LOG, ComplexMath.LOG, PrimitiveMath.LOG, QuaternionMath.LOG, RationalMath.LOG, ONE);
        this.assertUnary(BigMath.LOG, ComplexMath.LOG, PrimitiveMath.LOG, QuaternionMath.LOG, RationalMath.LOG, TENTH);
        this.assertUnary(BigMath.LOG, ComplexMath.LOG, PrimitiveMath.LOG, QuaternionMath.LOG, RationalMath.LOG, HUNDREDTH);
        this.assertUnary(BigMath.LOG, ComplexMath.LOG, PrimitiveMath.LOG, QuaternionMath.LOG, RationalMath.LOG, THOUSANDTH);

    }

    @Test
    public void testLOG10() {
        this.assertUnary(BigMath.LOG10, ComplexMath.LOG10, PrimitiveMath.LOG10, QuaternionMath.LOG10, RationalMath.LOG10, POSITIVE.doubleValue());
    }

    @Test
    public void testLOG1P() {
        this.assertUnary(BigMath.LOG1P, ComplexMath.LOG1P, PrimitiveMath.LOG1P, QuaternionMath.LOG1P, RationalMath.LOG1P, POSITIVE.doubleValue());
    }

    @Test
    @Tag("unstable")
    public void testMAX() {
        this.assertBinary(BigMath.MAX, ComplexMath.MAX, PrimitiveMath.MAX, QuaternionMath.MAX, RationalMath.MAX, AROUND_ZERO.doubleValue(),
                POSITIVE.doubleValue());
    }

    @Test
    @Tag("unstable")
    public void testMIN() {
        this.assertBinary(BigMath.MIN, ComplexMath.MIN, PrimitiveMath.MIN, QuaternionMath.MIN, RationalMath.MIN, AROUND_ZERO.doubleValue(),
                POSITIVE.doubleValue());
    }

    @Test
    public void testMULTIPLY() {
        this.assertBinary(BigMath.MULTIPLY, ComplexMath.MULTIPLY, PrimitiveMath.MULTIPLY, QuaternionMath.MULTIPLY, RationalMath.MULTIPLY,
                AROUND_ZERO.doubleValue(), POSITIVE.doubleValue());
    }

    @Test
    public void testNEGATE() {
        this.assertUnary(BigMath.NEGATE, ComplexMath.NEGATE, PrimitiveMath.NEGATE, QuaternionMath.NEGATE, RationalMath.NEGATE, AROUND_ZERO.doubleValue());
    }

    @Test
    public void testPOW() {

        // Only defined for non-negative (absolute) first aruments

        this.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, HUNDREDTH, NINE);
        this.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, THOUSANDTH, SEVEN);
        this.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, TENTH, EIGHT);
        this.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, ONE, SIX);
        this.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, HALF_PI, FIVE);
        this.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, TWO, FOUR);
        this.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, E, THREE);
        this.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, PI, TWO);
        this.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, SQRT_PI, ONE);

        this.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, SQRT_TWO_PI, SQRT_TWO_PI);
        this.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, SQRT_TWO_PI, ZERO);
        this.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, THOUSANDTH, SQRT_TWO_PI);
        this.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, THOUSANDTH, ZERO);

        this.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, SQRT_TWO_PI, -SQRT_TWO_PI);
        this.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, SQRT_TWO_PI, -ZERO);
        this.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, THOUSANDTH, -SQRT_TWO_PI);
        this.assertBinary(BigMath.POW, ComplexMath.POW, PrimitiveMath.POW, QuaternionMath.POW, RationalMath.POW, THOUSANDTH, -ZERO);

    }

    @Test
    public void testPOWER() {

        //        this.assertParameter(BigFunction.POWER, ComplexFunction.POWER, PrimitiveFunction.POWER, QuaternionFunction.POWER, RationalFunction.POWER,
        //                AROUND_ZERO.doubleValue(), Uniform.randomInteger(1, 10));

        this.assertParameter(BigMath.POWER, ComplexMath.POWER, PrimitiveMath.POWER, QuaternionMath.POWER, RationalMath.POWER, PI, 2);
        this.assertParameter(BigMath.POWER, ComplexMath.POWER, PrimitiveMath.POWER, QuaternionMath.POWER, RationalMath.POWER, E, 3);
        this.assertParameter(BigMath.POWER, ComplexMath.POWER, PrimitiveMath.POWER, QuaternionMath.POWER, RationalMath.POWER, TWO, 4);
        this.assertParameter(BigMath.POWER, ComplexMath.POWER, PrimitiveMath.POWER, QuaternionMath.POWER, RationalMath.POWER, HALF_PI, 5);
        this.assertParameter(BigMath.POWER, ComplexMath.POWER, PrimitiveMath.POWER, QuaternionMath.POWER, RationalMath.POWER, ONE, 6);

        this.assertParameter(BigMath.POWER, ComplexMath.POWER, PrimitiveMath.POWER, QuaternionMath.POWER, RationalMath.POWER, ZERO, 7);
    }

    @Test
    public void testRINT() {

        this.assertUnary(BigMath.RINT, ComplexMath.RINT, PrimitiveMath.RINT, QuaternionMath.RINT, RationalMath.RINT, AROUND_ZERO.doubleValue());

        this.assertUnary(BigMath.RINT, ComplexMath.RINT, PrimitiveMath.RINT, QuaternionMath.RINT, RationalMath.RINT, HALF);

        this.assertUnary(BigMath.RINT, ComplexMath.RINT, PrimitiveMath.RINT, QuaternionMath.RINT, RationalMath.RINT, -HALF);

        this.assertUnary(BigMath.RINT, ComplexMath.RINT, PrimitiveMath.RINT, QuaternionMath.RINT, RationalMath.RINT, ONE + HALF);

        this.assertUnary(BigMath.RINT, ComplexMath.RINT, PrimitiveMath.RINT, QuaternionMath.RINT, RationalMath.RINT, -(ONE + HALF));

    }

    @Test
    @Tag("unstable")
    public void testROOT() {
        this.assertParameter(BigMath.ROOT, ComplexMath.ROOT, PrimitiveMath.ROOT, QuaternionMath.ROOT, RationalMath.ROOT, POSITIVE.doubleValue(),
                Uniform.randomInteger(1, 10));
    }

    @Test
    public void testSCALE() {
        this.assertParameter(BigMath.SCALE, ComplexMath.SCALE, PrimitiveMath.SCALE, QuaternionMath.SCALE, RationalMath.SCALE, AROUND_ZERO.doubleValue(),
                Uniform.randomInteger(1, 10));
    }

    @Test
    public void testSIGNUM() {
        this.assertUnary(BigMath.SIGNUM, ComplexMath.SIGNUM, PrimitiveMath.SIGNUM, QuaternionMath.SIGNUM, RationalMath.SIGNUM, AROUND_ZERO.doubleValue());
    }

    @Test
    public void testSIN() {

        this.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, AROUND_ZERO.doubleValue());

        this.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, PI);
        this.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, E);
        this.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, TWO);
        this.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, HALF_PI);
        this.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, ONE);

        this.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, ZERO);

        this.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, NEG);
        this.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, -HALF_PI);
        this.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, -TWO);
        this.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, -E);
        this.assertUnary(BigMath.SIN, ComplexMath.SIN, PrimitiveMath.SIN, QuaternionMath.SIN, RationalMath.SIN, -PI);
    }

    @Test
    public void testSINH() {

        this.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, AROUND_ZERO.doubleValue());

        this.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, PI);
        this.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, E);
        this.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, TWO);
        this.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, HALF_PI);
        this.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, ONE);

        this.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, ZERO);

        this.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, NEG);
        this.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, -HALF_PI);
        this.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, -TWO);
        this.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, -E);
        this.assertUnary(BigMath.SINH, ComplexMath.SINH, PrimitiveMath.SINH, QuaternionMath.SINH, RationalMath.SINH, -PI);
    }

    @Test
    public void testSQRT() {

        this.assertUnary(BigMath.SQRT, ComplexMath.SQRT, PrimitiveMath.SQRT, QuaternionMath.SQRT, RationalMath.SQRT, POSITIVE.doubleValue());

        this.assertUnary(BigMath.SQRT, ComplexMath.SQRT, PrimitiveMath.SQRT, QuaternionMath.SQRT, RationalMath.SQRT, PI);
        this.assertUnary(BigMath.SQRT, ComplexMath.SQRT, PrimitiveMath.SQRT, QuaternionMath.SQRT, RationalMath.SQRT, E);
        this.assertUnary(BigMath.SQRT, ComplexMath.SQRT, PrimitiveMath.SQRT, QuaternionMath.SQRT, RationalMath.SQRT, TWO);
        this.assertUnary(BigMath.SQRT, ComplexMath.SQRT, PrimitiveMath.SQRT, QuaternionMath.SQRT, RationalMath.SQRT, HALF_PI);
        this.assertUnary(BigMath.SQRT, ComplexMath.SQRT, PrimitiveMath.SQRT, QuaternionMath.SQRT, RationalMath.SQRT, ONE);

        this.assertUnary(BigMath.SQRT, ComplexMath.SQRT, PrimitiveMath.SQRT, QuaternionMath.SQRT, RationalMath.SQRT, ZERO);
    }

    @Test
    public void testSQRT1PX2() {
        this.assertUnary(BigMath.SQRT1PX2, ComplexMath.SQRT1PX2, PrimitiveMath.SQRT1PX2, QuaternionMath.SQRT1PX2, RationalMath.SQRT1PX2,
                POSITIVE.doubleValue());
    }

    @Test
    public void testSUBTRACT() {
        this.assertBinary(BigMath.SUBTRACT, ComplexMath.SUBTRACT, PrimitiveMath.SUBTRACT, QuaternionMath.SUBTRACT, RationalMath.SUBTRACT,
                POSITIVE.doubleValue(), POSITIVE.doubleValue());
    }

    @Test
    public void testTAN() {

        this.assertUnary(BigMath.TAN, ComplexMath.TAN, PrimitiveMath.TAN, QuaternionMath.TAN, RationalMath.TAN, AROUND_ZERO.doubleValue());

        this.assertUnary(BigMath.TAN, ComplexMath.TAN, PrimitiveMath.TAN, QuaternionMath.TAN, RationalMath.TAN, PI);
        this.assertUnary(BigMath.TAN, ComplexMath.TAN, PrimitiveMath.TAN, QuaternionMath.TAN, RationalMath.TAN, E);
        this.assertUnary(BigMath.TAN, ComplexMath.TAN, PrimitiveMath.TAN, QuaternionMath.TAN, RationalMath.TAN, TWO);
        //     this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, RationalFunction.TAN, HALF_PI);
        this.assertUnary(BigMath.TAN, ComplexMath.TAN, PrimitiveMath.TAN, QuaternionMath.TAN, RationalMath.TAN, ONE);

        this.assertUnary(BigMath.TAN, ComplexMath.TAN, PrimitiveMath.TAN, QuaternionMath.TAN, RationalMath.TAN, ZERO);

        this.assertUnary(BigMath.TAN, ComplexMath.TAN, PrimitiveMath.TAN, QuaternionMath.TAN, RationalMath.TAN, NEG);
        //    this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, RationalFunction.TAN, -HALF_PI);
        this.assertUnary(BigMath.TAN, ComplexMath.TAN, PrimitiveMath.TAN, QuaternionMath.TAN, RationalMath.TAN, -TWO);
        this.assertUnary(BigMath.TAN, ComplexMath.TAN, PrimitiveMath.TAN, QuaternionMath.TAN, RationalMath.TAN, -E);
        this.assertUnary(BigMath.TAN, ComplexMath.TAN, PrimitiveMath.TAN, QuaternionMath.TAN, RationalMath.TAN, -PI);
    }

    @Test
    public void testTANH() {

        this.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, AROUND_ZERO.doubleValue());

        this.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, PI);
        this.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, E);
        this.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, TWO);
        this.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, HALF_PI);
        this.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, ONE);

        this.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, ZERO);

        this.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, NEG);
        this.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, -HALF_PI);
        this.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, -TWO);
        this.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, -E);
        this.assertUnary(BigMath.TANH, ComplexMath.TANH, PrimitiveMath.TANH, QuaternionMath.TANH, RationalMath.TANH, -PI);
    }

    @Test
    public void testVALUE() {
        this.assertUnary(BigMath.VALUE, ComplexMath.VALUE, PrimitiveMath.VALUE, QuaternionMath.VALUE, RationalMath.VALUE, AROUND_ZERO.doubleValue());
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
