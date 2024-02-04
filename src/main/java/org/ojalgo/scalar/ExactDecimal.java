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

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.type.context.NumberContext;

/**
 * To help implement exact decimal numbers with a fixed number of decimal places (fixed scale).
 *
 * @author apete
 */
public abstract class ExactDecimal<S extends ExactDecimal<S>> implements SelfDeclaringScalar<S> {

    public static final class Descriptor {

        private final NumberContext myContext;
        private final long myDenominator;

        public Descriptor(final int scale) {

            super();

            myContext = NumberContext.of(19, scale);
            myDenominator = Math.round(Math.pow(10.0, scale));
        }

        public long add(final ExactDecimal<?> arg1, final ExactDecimal<?> arg2) {
            final BigDecimal deci1 = arg1.toBigDecimal();
            final BigDecimal deci2 = arg2.toBigDecimal();
            final BigDecimal resul = deci1.add(deci2);
            return ExactDecimal.extractUnscaledValue(resul, myContext);
        }

        public NumberContext context() {
            return myContext;
        }

        public long denominator() {
            return myDenominator;
        }

        public long divide(final ExactDecimal<?> arg1, final ExactDecimal<?> arg2) {
            final BigDecimal deci1 = arg1.toBigDecimal();
            final BigDecimal deci2 = arg2.toBigDecimal();
            final BigDecimal resul = deci1.divide(deci2, myContext.getMathContext());
            return ExactDecimal.extractUnscaledValue(resul, myContext);
        }

        public long multiply(final ExactDecimal<?> arg1, final ExactDecimal<?> arg2) {
            final BigDecimal deci1 = arg1.toBigDecimal();
            final BigDecimal deci2 = arg2.toBigDecimal();
            final BigDecimal resul = deci1.multiply(deci2);
            return ExactDecimal.extractUnscaledValue(resul, myContext);
        }

        public long subtract(final ExactDecimal<?> arg1, final ExactDecimal<?> arg2) {
            final BigDecimal deci1 = arg1.toBigDecimal();
            final BigDecimal deci2 = arg2.toBigDecimal();
            final BigDecimal resul = deci1.subtract(deci2);
            return ExactDecimal.extractUnscaledValue(resul, myContext);
        }

    }

    public interface Factory<S extends ExactDecimal<S>> extends Scalar.Factory<S> {

        Descriptor descriptor();

    }

    protected static long extractUnscaledValue(final BigDecimal decimal, final NumberContext cntxt) {
        return decimal.setScale(cntxt.getScale(), cntxt.getRoundingMode()).unscaledValue().longValueExact();
    }

    private transient BigDecimal myDecimal = null;
    private final long myNumerator;

    protected ExactDecimal(final long numerator) {

        super();

        myNumerator = numerator;
    }

    @Override
    public final S add(final double scalarAddend) {
        return this.wrap(myNumerator + Math.round(scalarAddend * this.descriptor().denominator()));
    }

    @Override
    public final S add(final S scalarAddend) {
        return this.wrap(myNumerator + scalarAddend.numerator());
    }

    @Override
    public final int compareTo(final S reference) {
        return Long.compare(myNumerator, reference.numerator());
    }

    @Override
    public final S conjugate() {
        return (S) this;
    }

    @Override
    public final S divide(final double scalarDivisor) {
        return this.wrap(Math.round(myNumerator / scalarDivisor));
    }

    @Override
    public final S divide(final S scalarDivisor) {
        return this.wrap(myNumerator * this.descriptor().denominator() / scalarDivisor.numerator());
    }

    @Override
    public final double doubleValue() {
        return myNumerator / this.descriptor().denominator();
    }

    @Override
    public final S enforce(final NumberContext context) {
        BigDecimal decimal = this.toBigDecimal(context);
        final NumberContext type = this.descriptor().context();
        decimal = decimal.setScale(type.getScale(), type.getRoundingMode());
        return this.wrap(decimal.unscaledValue().longValueExact());
    }

    @Override
    public final float floatValue() {
        return (float) this.doubleValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public final S get() {
        return (S) this;
    }

    @Override
    public final int intValue() {
        return (int) this.doubleValue();
    }

    @Override
    public final S invert() {
        return this.wrap(Math.round(this.descriptor().denominator() / this.doubleValue()));
    }

    @Override
    public final boolean isAbsolute() {
        return myNumerator >= 0L;
    }

    @Override
    public final boolean isSmall(final double comparedTo) {
        return this.descriptor().context().isSmall(comparedTo, this.doubleValue());
    }

    @Override
    public final long longValue() {
        return this.toBigDecimal().longValue();
    }

    @Override
    public final S multiply(final double scalarMultiplicand) {
        return this.wrap(Math.round(myNumerator * scalarMultiplicand));
    }

    @Override
    public final S multiply(final S scalarMultiplicand) {
        return this.wrap(myNumerator * scalarMultiplicand.numerator() / this.descriptor().denominator());
    }

    @Override
    public final S negate() {
        return this.wrap(-myNumerator);
    }

    @Override
    public final double norm() {
        return PrimitiveMath.ABS.invoke(this.doubleValue());
    }

    @Override
    @SuppressWarnings("unchecked")
    public S power(final int power) {

        if (power == 0) {

            return this.wrap(this.descriptor().denominator());

        }
        if (power == 1) {

            return (S) this;

        } else {

            long numer = MissingMath.power(myNumerator, power);
            long denom = MissingMath.power(this.descriptor().denominator(), power - 1);

            return this.wrap(numer / denom);
        }
    }

    @Override
    public final S signum() {
        if (myNumerator == 0L) {
            return this.wrap(0L);
        }
        if (myNumerator < 0L) {
            return this.wrap(-this.descriptor().denominator());
        } else {
            return this.wrap(this.descriptor().denominator());
        }
    }

    @Override
    public final S subtract(final double scalarSubtrahend) {
        return this.wrap(myNumerator - Math.round(scalarSubtrahend * this.descriptor().denominator()));
    }

    @Override
    public final S subtract(final S scalarSubtrahend) {
        return this.wrap(myNumerator - scalarSubtrahend.numerator());
    }

    @Override
    public final BigDecimal toBigDecimal() {
        if (myDecimal == null) {
            myDecimal = new BigDecimal(myNumerator).divide(new BigDecimal(this.descriptor().denominator()), this.descriptor().context().getMathContext());
        }
        return myDecimal;
    }

    @Override
    public final String toString() {
        return this.toBigDecimal().toPlainString();
    }

    @Override
    public final String toString(final NumberContext context) {
        return this.toBigDecimal(context).toPlainString();
    }

    private final BigDecimal toBigDecimal(final NumberContext context) {
        return new BigDecimal(myNumerator).divide(new BigDecimal(this.descriptor().denominator()), context.getMathContext());
    }

    protected abstract Descriptor descriptor();

    protected abstract S wrap(long numerator);

    final long numerator() {
        return myNumerator;
    }

}
