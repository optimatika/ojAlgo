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
package org.ojalgo.function;

import static org.ojalgo.constant.PrimitiveMath.*;

import java.math.BigDecimal;

import org.ojalgo.TestUtils;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * Checks that {@linkplain org.ojalgo.function.BigFunction}, {@linkplain org.ojalgo.function.ComplexFunction},
 * {@linkplain org.ojalgo.function.PrimitiveFunction} and {@linkplain org.ojalgo.function.RationalFunction} members
 * produce equal results for simple cases. Cannot test complex valued arguments or results, and cannot require the
 * precision of BigDecimal or RationalNumber.
 *
 * @author apete
 */
public class CompareImplementations extends FunctionTests {

    private static final Uniform AROUND_ZERO = new Uniform(NEG, TWO);
    private static final Uniform POSITIVE = new Uniform(E - TWO, TWO);

    public CompareImplementations(final String arg0) {
        super(arg0);
    }

    public void testABS() {
        this.assertUnary(BigFunction.ABS, ComplexFunction.ABS, PrimitiveFunction.ABS, RationalFunction.ABS, AROUND_ZERO.doubleValue());
    }

    public void testACOS() {

        this.assertUnary(BigFunction.ACOS, ComplexFunction.ACOS, PrimitiveFunction.ACOS, RationalFunction.ACOS, AROUND_ZERO.doubleValue());

        this.assertUnary(BigFunction.ACOS, ComplexFunction.ACOS, PrimitiveFunction.ACOS, RationalFunction.ACOS, ONE);
        this.assertUnary(BigFunction.ACOS, ComplexFunction.ACOS, PrimitiveFunction.ACOS, RationalFunction.ACOS, ZERO);
        this.assertUnary(BigFunction.ACOS, ComplexFunction.ACOS, PrimitiveFunction.ACOS, RationalFunction.ACOS, NEG);
    }

    public void testACOSH() {

        //        this.assertUnary(BigFunction.ACOSH, ComplexFunction.ACOSH, PrimitiveFunction.ACOSH, RationalFunction.ACOSH, POSITIVE.doubleValue());

        this.assertUnary(BigFunction.ACOSH, ComplexFunction.ACOSH, PrimitiveFunction.ACOSH, RationalFunction.ACOSH, PI);
        this.assertUnary(BigFunction.ACOSH, ComplexFunction.ACOSH, PrimitiveFunction.ACOSH, RationalFunction.ACOSH, E);
        this.assertUnary(BigFunction.ACOSH, ComplexFunction.ACOSH, PrimitiveFunction.ACOSH, RationalFunction.ACOSH, TWO);
        this.assertUnary(BigFunction.ACOSH, ComplexFunction.ACOSH, PrimitiveFunction.ACOSH, RationalFunction.ACOSH, HALF_PI);
        this.assertUnary(BigFunction.ACOSH, ComplexFunction.ACOSH, PrimitiveFunction.ACOSH, RationalFunction.ACOSH, ONE);
    }

    public void testADD() {
        this.assertBinary(BigFunction.ADD, ComplexFunction.ADD, PrimitiveFunction.ADD, RationalFunction.ADD, POSITIVE.doubleValue(), POSITIVE.doubleValue());
    }

    public void testASIN() {

        this.assertUnary(BigFunction.ASIN, ComplexFunction.ASIN, PrimitiveFunction.ASIN, RationalFunction.ASIN, AROUND_ZERO.doubleValue());

        this.assertUnary(BigFunction.ASIN, ComplexFunction.ASIN, PrimitiveFunction.ASIN, RationalFunction.ASIN, ONE);
        this.assertUnary(BigFunction.ASIN, ComplexFunction.ASIN, PrimitiveFunction.ASIN, RationalFunction.ASIN, ZERO);
        this.assertUnary(BigFunction.ASIN, ComplexFunction.ASIN, PrimitiveFunction.ASIN, RationalFunction.ASIN, NEG);
    }

    public void testASINH() {

        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, RationalFunction.ASINH, AROUND_ZERO.doubleValue());

        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, RationalFunction.ASINH, PI);
        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, RationalFunction.ASINH, E);
        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, RationalFunction.ASINH, TWO);
        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, RationalFunction.ASINH, HALF_PI);
        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, RationalFunction.ASINH, ONE);

        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, RationalFunction.ASINH, ZERO);

        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, RationalFunction.ASINH, NEG);
        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, RationalFunction.ASINH, -HALF_PI);
        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, RationalFunction.ASINH, -TWO);
        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, RationalFunction.ASINH, -E);
        this.assertUnary(BigFunction.ASINH, ComplexFunction.ASINH, PrimitiveFunction.ASINH, RationalFunction.ASINH, -PI);
    }

    public void testATAN() {

        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, RationalFunction.ATAN, AROUND_ZERO.doubleValue());

        //        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, RationalFunction.ATAN, MAX_VALUE);
        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, RationalFunction.ATAN, PI);
        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, RationalFunction.ATAN, E);
        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, RationalFunction.ATAN, TWO);
        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, RationalFunction.ATAN, HALF_PI);
        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, RationalFunction.ATAN, ONE);

        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, RationalFunction.ATAN, AROUND_ZERO.doubleValue());

        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, RationalFunction.ATAN, NEG);
        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, RationalFunction.ATAN, -HALF_PI);
        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, RationalFunction.ATAN, -TWO);
        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, RationalFunction.ATAN, -E);
        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, RationalFunction.ATAN, -PI);
        this.assertUnary(BigFunction.ATAN, ComplexFunction.ATAN, PrimitiveFunction.ATAN, RationalFunction.ATAN, MIN_VALUE);
    }

    public void testATANH() {

        this.assertUnary(BigFunction.ATANH, ComplexFunction.ATANH, PrimitiveFunction.ATANH, RationalFunction.ATANH, AROUND_ZERO.doubleValue());

        //    this.assertUnary(BigFunction.ATANH, ComplexFunction.ATANH, PrimitiveFunction.ATANH, RationalFunction.ATANH, ONE);
        this.assertUnary(BigFunction.ATANH, ComplexFunction.ATANH, PrimitiveFunction.ATANH, RationalFunction.ATANH, ZERO);
        //  this.assertUnary(BigFunction.ATANH, ComplexFunction.ATANH, PrimitiveFunction.ATANH, RationalFunction.ATANH, NEG);
    }

    public void testCARDINALITY() {
        this.assertUnary(BigFunction.CARDINALITY, ComplexFunction.CARDINALITY, PrimitiveFunction.CARDINALITY, RationalFunction.CARDINALITY,
                AROUND_ZERO.doubleValue());
    }

    public void testCONJUGATE() {
        this.assertUnary(BigFunction.CONJUGATE, ComplexFunction.CONJUGATE, PrimitiveFunction.CONJUGATE, RationalFunction.CONJUGATE, AROUND_ZERO.doubleValue());
    }

    public void testCOS() {

        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, RationalFunction.COS, AROUND_ZERO.doubleValue());

        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, RationalFunction.COS, PI);
        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, RationalFunction.COS, E);
        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, RationalFunction.COS, TWO);
        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, RationalFunction.COS, HALF_PI);
        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, RationalFunction.COS, ONE);

        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, RationalFunction.COS, ZERO);

        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, RationalFunction.COS, NEG);
        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, RationalFunction.COS, -HALF_PI);
        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, RationalFunction.COS, -TWO);
        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, RationalFunction.COS, -E);
        this.assertUnary(BigFunction.COS, ComplexFunction.COS, PrimitiveFunction.COS, RationalFunction.COS, -PI);
    }

    public void testCOSH() {

        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, RationalFunction.COSH, AROUND_ZERO.doubleValue());

        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, RationalFunction.COSH, PI);
        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, RationalFunction.COSH, E);
        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, RationalFunction.COSH, TWO);
        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, RationalFunction.COSH, HALF_PI);
        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, RationalFunction.COSH, ONE);

        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, RationalFunction.COSH, ZERO);

        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, RationalFunction.COSH, NEG);
        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, RationalFunction.COSH, -HALF_PI);
        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, RationalFunction.COSH, -TWO);
        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, RationalFunction.COSH, -E);
        this.assertUnary(BigFunction.COSH, ComplexFunction.COSH, PrimitiveFunction.COSH, RationalFunction.COSH, -PI);
    }

    public void testDIVIDE() {
        this.assertBinary(BigFunction.DIVIDE, ComplexFunction.DIVIDE, PrimitiveFunction.DIVIDE, RationalFunction.DIVIDE, POSITIVE.doubleValue(),
                POSITIVE.doubleValue());
    }

    public void testEXP() {

        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, RationalFunction.EXP, AROUND_ZERO.doubleValue());

        //        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, RationalFunction.EXP, MAX_VALUE);
        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, RationalFunction.EXP, PI);
        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, RationalFunction.EXP, E);
        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, RationalFunction.EXP, TWO);
        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, RationalFunction.EXP, HALF_PI);
        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, RationalFunction.EXP, ONE);

        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, RationalFunction.EXP, ZERO);

        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, RationalFunction.EXP, NEG);
        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, RationalFunction.EXP, -HALF_PI);
        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, RationalFunction.EXP, -TWO);
        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, RationalFunction.EXP, -E);
        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, RationalFunction.EXP, -PI);
        this.assertUnary(BigFunction.EXP, ComplexFunction.EXP, PrimitiveFunction.EXP, RationalFunction.EXP, MIN_VALUE);
    }

    public void testEXPM1() {
        this.assertUnary(BigFunction.EXPM1, ComplexFunction.EXPM1, PrimitiveFunction.EXPM1, RationalFunction.EXPM1, AROUND_ZERO.doubleValue());
    }

    public void testHYPOT() {
        this.assertBinary(BigFunction.HYPOT, ComplexFunction.HYPOT, PrimitiveFunction.HYPOT, RationalFunction.HYPOT, POSITIVE.doubleValue(),
                POSITIVE.doubleValue());
    }

    public void testINVERT() {
        this.assertUnary(BigFunction.INVERT, ComplexFunction.INVERT, PrimitiveFunction.INVERT, RationalFunction.INVERT, POSITIVE.doubleValue());
    }

    public void testLOG() {

        this.assertUnary(BigFunction.LOG, ComplexFunction.LOG, PrimitiveFunction.LOG, RationalFunction.LOG, POSITIVE.doubleValue());

        this.assertUnary(BigFunction.LOG, ComplexFunction.LOG, PrimitiveFunction.LOG, RationalFunction.LOG, PI);
        this.assertUnary(BigFunction.LOG, ComplexFunction.LOG, PrimitiveFunction.LOG, RationalFunction.LOG, E);
        this.assertUnary(BigFunction.LOG, ComplexFunction.LOG, PrimitiveFunction.LOG, RationalFunction.LOG, TWO);
        this.assertUnary(BigFunction.LOG, ComplexFunction.LOG, PrimitiveFunction.LOG, RationalFunction.LOG, HALF_PI);
        this.assertUnary(BigFunction.LOG, ComplexFunction.LOG, PrimitiveFunction.LOG, RationalFunction.LOG, ONE);

        //      this.assertUnary(BigFunction.LOG, ComplexFunction.LOG, PrimitiveFunction.LOG, RationalFunction.LOG, ZERO);
    }

    public void testLOG10() {
        this.assertUnary(BigFunction.LOG10, ComplexFunction.LOG10, PrimitiveFunction.LOG10, RationalFunction.LOG10, POSITIVE.doubleValue());
    }

    public void testLOG1P() {
        this.assertUnary(BigFunction.LOG1P, ComplexFunction.LOG1P, PrimitiveFunction.LOG1P, RationalFunction.LOG1P, POSITIVE.doubleValue());
    }

    public void testMAX() {
        this.assertBinary(BigFunction.MAX, ComplexFunction.MAX, PrimitiveFunction.MAX, RationalFunction.MAX, AROUND_ZERO.doubleValue(), POSITIVE.doubleValue());
    }

    public void testMIN() {
        this.assertBinary(BigFunction.MIN, ComplexFunction.MIN, PrimitiveFunction.MIN, RationalFunction.MIN, AROUND_ZERO.doubleValue(), POSITIVE.doubleValue());
    }

    public void testMULTIPLY() {
        this.assertBinary(BigFunction.MULTIPLY, ComplexFunction.MULTIPLY, PrimitiveFunction.MULTIPLY, RationalFunction.MULTIPLY, AROUND_ZERO.doubleValue(),
                POSITIVE.doubleValue());
    }

    public void testNEGATE() {
        this.assertUnary(BigFunction.NEGATE, ComplexFunction.NEGATE, PrimitiveFunction.NEGATE, RationalFunction.NEGATE, AROUND_ZERO.doubleValue());
    }

    public void testPOW() {

        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, RationalFunction.POW, POSITIVE.doubleValue(), AROUND_ZERO.doubleValue());

        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, RationalFunction.POW, PI, PI);
        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, RationalFunction.POW, E, PI);
        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, RationalFunction.POW, TWO, PI);
        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, RationalFunction.POW, HALF_PI, PI);
        this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, RationalFunction.POW, ONE, PI);

        //     this.assertBinary(BigFunction.POW, ComplexFunction.POW, PrimitiveFunction.POW, RationalFunction.POW, ZERO, PI);
    }

    public void testPOWER() {

        this.assertParameter(BigFunction.POWER, ComplexFunction.POWER, PrimitiveFunction.POWER, RationalFunction.POWER, AROUND_ZERO.doubleValue(),
                Uniform.randomInteger(1, 10));

        this.assertParameter(BigFunction.POWER, ComplexFunction.POWER, PrimitiveFunction.POWER, RationalFunction.POWER, PI, 2);
        this.assertParameter(BigFunction.POWER, ComplexFunction.POWER, PrimitiveFunction.POWER, RationalFunction.POWER, E, 3);
        this.assertParameter(BigFunction.POWER, ComplexFunction.POWER, PrimitiveFunction.POWER, RationalFunction.POWER, TWO, 4);
        this.assertParameter(BigFunction.POWER, ComplexFunction.POWER, PrimitiveFunction.POWER, RationalFunction.POWER, HALF_PI, 5);
        this.assertParameter(BigFunction.POWER, ComplexFunction.POWER, PrimitiveFunction.POWER, RationalFunction.POWER, ONE, 6);

        this.assertParameter(BigFunction.POWER, ComplexFunction.POWER, PrimitiveFunction.POWER, RationalFunction.POWER, ZERO, 7);
    }

    public void testROOT() {
        this.assertParameter(BigFunction.ROOT, ComplexFunction.ROOT, PrimitiveFunction.ROOT, RationalFunction.ROOT, POSITIVE.doubleValue(),
                Uniform.randomInteger(1, 10));
    }

    public void testSCALE() {
        this.assertParameter(BigFunction.SCALE, ComplexFunction.SCALE, PrimitiveFunction.SCALE, RationalFunction.SCALE, AROUND_ZERO.doubleValue(),
                Uniform.randomInteger(1, 10));
    }

    public void testSIGNUM() {
        this.assertUnary(BigFunction.SIGNUM, ComplexFunction.SIGNUM, PrimitiveFunction.SIGNUM, RationalFunction.SIGNUM, AROUND_ZERO.doubleValue());
    }

    public void testSIN() {

        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, RationalFunction.SIN, AROUND_ZERO.doubleValue());

        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, RationalFunction.SIN, PI);
        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, RationalFunction.SIN, E);
        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, RationalFunction.SIN, TWO);
        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, RationalFunction.SIN, HALF_PI);
        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, RationalFunction.SIN, ONE);

        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, RationalFunction.SIN, ZERO);

        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, RationalFunction.SIN, NEG);
        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, RationalFunction.SIN, -HALF_PI);
        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, RationalFunction.SIN, -TWO);
        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, RationalFunction.SIN, -E);
        this.assertUnary(BigFunction.SIN, ComplexFunction.SIN, PrimitiveFunction.SIN, RationalFunction.SIN, -PI);
    }

    public void testSINH() {

        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, RationalFunction.SINH, AROUND_ZERO.doubleValue());

        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, RationalFunction.SINH, PI);
        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, RationalFunction.SINH, E);
        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, RationalFunction.SINH, TWO);
        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, RationalFunction.SINH, HALF_PI);
        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, RationalFunction.SINH, ONE);

        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, RationalFunction.SINH, ZERO);

        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, RationalFunction.SINH, NEG);
        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, RationalFunction.SINH, -HALF_PI);
        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, RationalFunction.SINH, -TWO);
        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, RationalFunction.SINH, -E);
        this.assertUnary(BigFunction.SINH, ComplexFunction.SINH, PrimitiveFunction.SINH, RationalFunction.SINH, -PI);
    }

    public void testSQRT() {

        this.assertUnary(BigFunction.SQRT, ComplexFunction.SQRT, PrimitiveFunction.SQRT, RationalFunction.SQRT, POSITIVE.doubleValue());

        this.assertUnary(BigFunction.SQRT, ComplexFunction.SQRT, PrimitiveFunction.SQRT, RationalFunction.SQRT, PI);
        this.assertUnary(BigFunction.SQRT, ComplexFunction.SQRT, PrimitiveFunction.SQRT, RationalFunction.SQRT, E);
        this.assertUnary(BigFunction.SQRT, ComplexFunction.SQRT, PrimitiveFunction.SQRT, RationalFunction.SQRT, TWO);
        this.assertUnary(BigFunction.SQRT, ComplexFunction.SQRT, PrimitiveFunction.SQRT, RationalFunction.SQRT, HALF_PI);
        this.assertUnary(BigFunction.SQRT, ComplexFunction.SQRT, PrimitiveFunction.SQRT, RationalFunction.SQRT, ONE);

        this.assertUnary(BigFunction.SQRT, ComplexFunction.SQRT, PrimitiveFunction.SQRT, RationalFunction.SQRT, ZERO);
    }

    public void testSQRT1PX2() {
        this.assertUnary(BigFunction.SQRT1PX2, ComplexFunction.SQRT1PX2, PrimitiveFunction.SQRT1PX2, RationalFunction.SQRT1PX2, POSITIVE.doubleValue());
    }

    public void testSUBTRACT() {
        this.assertBinary(BigFunction.SUBTRACT, ComplexFunction.SUBTRACT, PrimitiveFunction.SUBTRACT, RationalFunction.SUBTRACT, POSITIVE.doubleValue(),
                POSITIVE.doubleValue());
    }

    public void testTAN() {

        this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, RationalFunction.TAN, AROUND_ZERO.doubleValue());

        this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, RationalFunction.TAN, PI);
        this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, RationalFunction.TAN, E);
        this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, RationalFunction.TAN, TWO);
        //     this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, RationalFunction.TAN, HALF_PI);
        this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, RationalFunction.TAN, ONE);

        this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, RationalFunction.TAN, ZERO);

        this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, RationalFunction.TAN, NEG);
        //    this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, RationalFunction.TAN, -HALF_PI);
        this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, RationalFunction.TAN, -TWO);
        this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, RationalFunction.TAN, -E);
        this.assertUnary(BigFunction.TAN, ComplexFunction.TAN, PrimitiveFunction.TAN, RationalFunction.TAN, -PI);
    }

    public void testTANH() {

        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, RationalFunction.TANH, AROUND_ZERO.doubleValue());

        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, RationalFunction.TANH, PI);
        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, RationalFunction.TANH, E);
        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, RationalFunction.TANH, TWO);
        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, RationalFunction.TANH, HALF_PI);
        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, RationalFunction.TANH, ONE);

        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, RationalFunction.TANH, ZERO);

        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, RationalFunction.TANH, NEG);
        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, RationalFunction.TANH, -HALF_PI);
        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, RationalFunction.TANH, -TWO);
        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, RationalFunction.TANH, -E);
        this.assertUnary(BigFunction.TANH, ComplexFunction.TANH, PrimitiveFunction.TANH, RationalFunction.TANH, -PI);
    }

    public void testVALUE() {
        this.assertUnary(BigFunction.VALUE, ComplexFunction.VALUE, PrimitiveFunction.VALUE, RationalFunction.VALUE, AROUND_ZERO.doubleValue());
    }

    void assertBinary(final BinaryFunction<BigDecimal> big, final BinaryFunction<ComplexNumber> complex, final BinaryFunction<Double> primitive,
            final BinaryFunction<RationalNumber> rational, final double arg1, final double arg2) {

        TestUtils.assertEquals("Big vs Complex", big.invoke(arg1, arg2), complex.invoke(arg1, arg2), new NumberContext(7, 14));
        TestUtils.assertEquals("Complex vs Primitive", complex.invoke(arg1, arg2), primitive.invoke(arg1, arg2), new NumberContext(7, 14));
        TestUtils.assertEquals("Primitive vs Rational", primitive.invoke(arg1, arg2), rational.invoke(arg1, arg2), new NumberContext(7, 14));
        TestUtils.assertEquals("Rational vs Big", rational.invoke(arg1, arg2), big.invoke(arg1, arg2), new NumberContext(7, 14));
    }

    void assertParameter(final ParameterFunction<BigDecimal> big, final ParameterFunction<ComplexNumber> complex, final ParameterFunction<Double> primitive,
            final ParameterFunction<RationalNumber> rational, final double arg, final int param) {

        TestUtils.assertEquals("Big vs Complex", big.invoke(arg, param), complex.invoke(arg, param), new NumberContext(7, 14));
        TestUtils.assertEquals("Complex vs Primitive", complex.invoke(arg, param), primitive.invoke(arg, param), new NumberContext(7, 14));
        TestUtils.assertEquals(primitive.invoke(arg, param), rational.invoke(arg, param), new NumberContext(7, 14));
        TestUtils.assertEquals("Rational vs Big", rational.invoke(arg, param), big.invoke(arg, param), new NumberContext(7, 14));
    }

    void assertUnary(final UnaryFunction<BigDecimal> big, final UnaryFunction<ComplexNumber> complex, final UnaryFunction<Double> primitive,
            final UnaryFunction<RationalNumber> rational, final double arg) {

        TestUtils.assertEquals("Big vs Complex", big.invoke(arg), complex.invoke(arg), new NumberContext(7, 14));
        TestUtils.assertEquals("Complex vs Primitive", complex.invoke(arg), primitive.invoke(arg), new NumberContext(7, 14));
        TestUtils.assertEquals("Primitive vs Rational", primitive.invoke(arg), rational.invoke(arg), new NumberContext(7, 14));
        TestUtils.assertEquals("Rational vs Big", rational.invoke(arg), big.invoke(arg), new NumberContext(7, 14));
    }

}
