/*
 * Copyright 1997-2024 Optimatika
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
import java.math.MathContext;

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * <P>
 * https://stackoverflow.com/questions/66962567/how-to-emulate-double-precision-using-two-floats-in-opengl-es
 * <P>
 * https://blog.cyclemap.link/2011-06-09-glsl-part2-emu/
 * <P>
 * https://www.researchgate.net/publication/228570156_Library_for_Double-Double_and_Quad-Double_Arithmetic
 * <P>
 * https://libntl.org
 * <P>
 * http://mrob.com/pub/math/f161.html
 * <P>
 * https://www.davidhbailey.com/dhbsoftware/
 * <P>
 *
 * @author apete
 */
public class Quadruple implements SelfDeclaringScalar<Quadruple> {

    public static Scalar.Factory<Quadruple> FACTORY = new Scalar.Factory<>() {

        @Override
        public Quadruple cast(final Comparable<?> number) {
            return Quadruple.valueOf(number);
        }

        @Override
        public Quadruple cast(final double value) {
            return Quadruple.valueOf(value);
        }

        @Override
        public Quadruple convert(final Comparable<?> number) {
            return Quadruple.valueOf(number);
        }

        @Override
        public Quadruple convert(final double value) {
            return Quadruple.valueOf(value);
        }

        @Override
        public Quadruple one() {
            return ONE;
        }

        @Override
        public Quadruple zero() {
            return ZERO;
        }

    };

    public static final Quadruple MAX_VALUE = new Quadruple(Double.MAX_VALUE, 0);
    public static final Quadruple MIN_VALUE = new Quadruple(Long.MIN_VALUE, 0);
    public static final Quadruple NaN = new Quadruple(Double.NaN, Double.NaN);
    public static final Quadruple NEG = new Quadruple(-1, 0);
    public static final Quadruple NEGATIVE_INFINITY = new Quadruple(Double.NEGATIVE_INFINITY, 0);
    public static final Quadruple ONE = new Quadruple(1, 0);
    public static final Quadruple POSITIVE_INFINITY = new Quadruple(Double.POSITIVE_INFINITY, 0);
    public static final Quadruple TWO = new Quadruple(2, 0);
    public static final Quadruple ZERO = new Quadruple(0, 0);

    /**
     * 1+2^27 (Example code, that emulated double using float, had 1+2^13)
     */
    private static final double SPLIT = 134217729.0;

    static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;
    static final NumberContext NUMBER_CONTEXT = NumberContext.ofMath(MATH_CONTEXT);

    public static boolean isAbsolute(final Quadruple value) {
        return value.isAbsolute();
    }

    public static boolean isInfinite(final Quadruple value) {
        return Double.isInfinite(value.getBase()) || Double.isInfinite(value.getRemainder());
    }

    public static boolean isNaN(final Quadruple value) {
        return Double.isNaN(value.getBase()) || Double.isNaN(value.getRemainder());
    }

    public static boolean isSmall(final double comparedTo, final Quadruple value) {
        return value.isSmall(comparedTo);
    }

    public static Quadruple parse(final CharSequence plainNumberString) {
        BigDecimal decimal = new BigDecimal(plainNumberString.toString());
        return Quadruple.valueOf(decimal);
    }

    public static Quadruple valueOf(final BigDecimal number) {

        double base = number.doubleValue();

        BigDecimal mag = new BigDecimal(base);
        BigDecimal rem = number.subtract(mag);

        double remainder = rem.doubleValue();

        return new Quadruple(base, remainder);
    }

    public static Quadruple valueOf(final Comparable<?> number) {

        if (number == null) {
            return ZERO;
        }

        if (number instanceof Quadruple) {

            return (Quadruple) number;

        } else {

            BigDecimal tmpBigD = TypeUtils.toBigDecimal(number);

            return Quadruple.valueOf(tmpBigD);
        }
    }

    public static Quadruple valueOf(final double value) {
        return new Quadruple(value);
    }

    /**
     * https://blog.cyclemap.link/2011-06-09-glsl-part2-emu/
     */
    private static Quadruple add(final double base1, final double remainder1, final double base2, final double remainder2) {

        double t1, t2, e;

        t1 = base1 + base2;
        e = t1 - base1;
        t2 = ((base2 - e) + (base1 - (t1 - e))) + remainder1 + remainder2;

        double base = t1 + t2;
        double remainder = t2 - (base - t1);

        return new Quadruple(base, remainder);
    }

    private static Quadruple divide(final Quadruple arg1, final Quadruple arg2) {

        // TODO How to do this with only primitive double – same as multiply

        BigDecimal decimal1 = arg1.toBigDecimal();
        BigDecimal decimal2 = arg2.toBigDecimal();

        BigDecimal quotient = MissingMath.divide(decimal1, decimal2);

        return Quadruple.valueOf(quotient);
    }

    /**
     * https://blog.cyclemap.link/2011-06-09-glsl-part2-emu/
     */
    private static Quadruple multiply(final double base1, final double remainder1, final double base2, final double remainder2) {

        double c11, c21, c2, e, t1, t2;
        double a1, a2, b1, b2, cona, conb;

        cona = base1 * SPLIT;
        conb = base2 * SPLIT;
        a1 = cona - (cona - base1);
        b1 = conb - (conb - base2);
        a2 = base1 - a1;
        b2 = base2 - b1;

        c11 = base1 * base2;
        c21 = a2 * b2 + (a2 * b1 + (a1 * b2 + (a1 * b1 - c11)));

        c2 = base1 * remainder2 + remainder1 * base2;

        t1 = c11 + c2;
        e = t1 - c11;
        t2 = remainder1 * remainder2 + ((c2 - e) + (c11 - (t1 - e))) + c21;

        double base = t1 + t2;
        double remainder = t2 - (base - t1);

        return new Quadruple(base, remainder);
    }

    private final double myBase;
    private transient BigDecimal myDecimal = null;
    private final double myRemainder;

    public Quadruple() {
        this(0.0, 0.0);
    }

    private Quadruple(final double base) {
        this(base, 0.0);
    }

    private Quadruple(final double base, final double remainder) {

        super();

        myBase = base;
        myRemainder = remainder;
    }

    @Override
    public Quadruple add(final double arg) {
        return new Quadruple(myBase + arg, myRemainder);
    }

    @Override
    public Quadruple add(final Quadruple arg) {

        if (Quadruple.isNaN(this) || Quadruple.isNaN(arg)) {
            return NaN;
        }

        if (Quadruple.isInfinite(this)) {
            if (!Quadruple.isInfinite(arg) || (this.sign() == arg.sign())) {
                return this;
            } else {
                return NaN;
            }
        }

        double base1 = this.getBase();
        double remainder1 = this.getRemainder();

        double base2 = arg.getBase();
        double remainder2 = arg.getRemainder();

        return Quadruple.add(base1, remainder1, base2, remainder2);
    }

    @Override
    public int compareTo(final Quadruple reference) {
        return Double.compare(this.doubleValue(), reference.doubleValue());
    }

    @Override
    public Quadruple conjugate() {
        return this;
    }

    @Override
    public Quadruple divide(final double arg) {
        return this.divide(Quadruple.valueOf(arg));
    }

    @Override
    public Quadruple divide(final Quadruple arg) {

        if (Quadruple.isNaN(this) || Quadruple.isNaN(arg)) {
            return NaN;
        }

        return Quadruple.divide(this, arg);

    }

    @Override
    public double doubleValue() {
        return myBase + myRemainder;
    }

    @Override
    public Quadruple enforce(final NumberContext context) {
        return Quadruple.valueOf(this.toBigDecimal(context.getMathContext()));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Quadruple)) {
            return false;
        }
        Quadruple other = (Quadruple) obj;
        if ((Double.doubleToLongBits(myBase) != Double.doubleToLongBits(other.myBase))
                || (Double.doubleToLongBits(myRemainder) != Double.doubleToLongBits(other.myRemainder))) {
            return false;
        }
        return true;
    }

    @Override
    public float floatValue() {
        return this.toBigDecimal().floatValue();
    }

    @Override
    public Quadruple get() {
        return this;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(myBase);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(myRemainder);
        return prime * result + (int) (temp ^ (temp >>> 32));
    }

    @Override
    public int intValue() {
        return this.toBigDecimal().intValue();
    }

    @Override
    public Quadruple invert() {
        return ONE.divide(this);
    }

    @Override
    public boolean isAbsolute() {
        return myBase >= 0L;
    }

    @Override
    public boolean isSmall(final double comparedTo) {
        return BigScalar.CONTEXT.isSmall(comparedTo, this.doubleValue());
    }

    @Override
    public long longValue() {
        return Math.round(myBase + myRemainder);
    }

    @Override
    public Quadruple multiply(final double arg) {
        return Quadruple.multiply(myBase, myRemainder, arg, 0.0);
    }

    @Override
    public Quadruple multiply(final Quadruple arg) {

        if (Quadruple.isNaN(this) || Quadruple.isNaN(arg)) {
            return NaN;
        }

        if (Quadruple.isInfinite(this)) {
            return arg.sign() > 0 ? this : this.negate();
        }

        double base1 = this.getBase();
        double remainder1 = this.getRemainder();

        double base2 = arg.getBase();
        double remainder2 = arg.getRemainder();

        return Quadruple.multiply(base1, remainder1, base2, remainder2);
    }

    @Override
    public Quadruple negate() {
        return new Quadruple(-myBase, -myRemainder);
    }

    @Override
    public double norm() {
        return Math.abs(this.doubleValue());
    }

    @Override
    public Quadruple power(final int power) {

        Quadruple retVal = ONE;

        for (int p = 0; p < power; p++) {
            retVal = retVal.multiply(this);
        }

        return retVal;
    }

    @Override
    public Quadruple signum() {
        if (!Quadruple.isInfinite(this) && Quadruple.isSmall(PrimitiveMath.ONE, this)) {
            return ZERO;
        } else if (this.sign() == -1) {
            return NEG;
        } else {
            return ONE;
        }
    }

    @Override
    public Quadruple subtract(final double arg) {
        return new Quadruple(myBase - arg, myRemainder);
    }

    @Override
    public Quadruple subtract(final Quadruple arg) {

        if (Quadruple.isNaN(this) || Quadruple.isNaN(arg)) {
            return NaN;
        }

        if (Quadruple.isInfinite(this)) {
            if (!Quadruple.isInfinite(arg) || (this.sign() != arg.sign())) {
                return this;
            } else {
                return NaN;
            }
        }

        double base1 = this.getBase();
        double remainder1 = this.getRemainder();

        double base2 = -arg.getBase();
        double remainder2 = -arg.getRemainder();

        return Quadruple.add(base1, remainder1, base2, remainder2);
    }

    @Override
    public BigDecimal toBigDecimal() {
        if (myDecimal == null) {
            myDecimal = this.toBigDecimal(MATH_CONTEXT);
        }
        return myDecimal;
    }

    @Override
    public String toString() {
        return "Quadruple [" + myBase + " + " + myRemainder + "]";
    }

    @Override
    public String toString(final NumberContext context) {
        return context.enforce(this.toBigDecimal()).toString();
    }

    private int sign() {
        return Double.compare(this.doubleValue(), 0L);
    }

    private BigDecimal toBigDecimal(final MathContext context) {
        return new BigDecimal(myBase).add(new BigDecimal(myRemainder), context);
    }

    double getBase() {
        return myBase;
    }

    double getRemainder() {
        return myRemainder;
    }

}
