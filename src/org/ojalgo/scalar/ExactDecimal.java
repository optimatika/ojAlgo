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
package org.ojalgo.scalar;

import java.math.BigDecimal;

import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.context.NumberContext.Enforceable;

public abstract class ExactDecimal<S extends ExactDecimal<S>> extends Number implements Scalar<S>, Enforceable<S> {

    public static final class Descriptor {

        private final NumberContext myContext;
        private final long myDenominator;

        public Descriptor(int scale) {

            super();

            myContext = new NumberContext(19, scale);
            myDenominator = Math.round(Math.pow(10.0, scale));
        }

        public long add(ExactDecimal<?> arg1, ExactDecimal<?> arg2) {
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

        public long divide(ExactDecimal<?> arg1, ExactDecimal<?> arg2) {
            final BigDecimal deci1 = arg1.toBigDecimal();
            final BigDecimal deci2 = arg2.toBigDecimal();
            final BigDecimal resul = deci1.divide(deci2, myContext.getMathContext());
            return ExactDecimal.extractUnscaledValue(resul, myContext);
        }

        public long multiply(ExactDecimal<?> arg1, ExactDecimal<?> arg2) {
            final BigDecimal deci1 = arg1.toBigDecimal();
            final BigDecimal deci2 = arg2.toBigDecimal();
            final BigDecimal resul = deci1.multiply(deci2);
            return ExactDecimal.extractUnscaledValue(resul, myContext);
        }

        public long subtract(ExactDecimal<?> arg1, ExactDecimal<?> arg2) {
            final BigDecimal deci1 = arg1.toBigDecimal();
            final BigDecimal deci2 = arg2.toBigDecimal();
            final BigDecimal resul = deci1.subtract(deci2);
            return ExactDecimal.extractUnscaledValue(resul, myContext);
        }

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

    public final S add(double scalarAddend) {
        return this.wrap(myNumerator + Math.round(scalarAddend * this.descriptor().denominator()));
    }

    public final S add(S scalarAddend) {
        return this.wrap(myNumerator + scalarAddend.numerator());
    }

    public final int compareTo(final S reference) {
        return Long.compare(myNumerator, reference.numerator());
    }

    @SuppressWarnings("unchecked")
    public final S conjugate() {
        return (S) this;
    }

    public final S divide(double scalarDivisor) {
        return this.wrap(Math.round(myNumerator / scalarDivisor));
    }

    public final S divide(S scalarDivisor) {
        return this.wrap((myNumerator * this.descriptor().denominator()) / scalarDivisor.numerator());
    }

    @Override
    public final double doubleValue() {
        return myNumerator / this.descriptor().denominator();
    }

    public final S enforce(NumberContext context) {
        BigDecimal decimal = this.toBigDecimal(context);
        final NumberContext type = this.descriptor().context();
        decimal = decimal.setScale(type.getScale(), type.getRoundingMode());
        return this.wrap(decimal.unscaledValue().longValueExact());
    }

    @Override
    public final float floatValue() {
        return (float) this.doubleValue();
    }

    @SuppressWarnings("unchecked")
    public final S get() {
        return (S) this;
    }

    @Override
    public final int intValue() {
        return (int) this.doubleValue();
    }

    public final S invert() {
        return this.wrap(Math.round(this.descriptor().denominator() / this.doubleValue()));
    }

    public final boolean isAbsolute() {
        return myNumerator >= 0L;
    }

    public final boolean isSmall(final double comparedTo) {
        return this.descriptor().context().isSmall(comparedTo, this.doubleValue());
    }

    @Override
    public final long longValue() {
        return this.toBigDecimal().longValue();
    }

    public final S multiply(double scalarMultiplicand) {
        return this.wrap(Math.round(myNumerator * scalarMultiplicand));
    }

    public final S multiply(S scalarMultiplicand) {
        return this.wrap((myNumerator * scalarMultiplicand.numerator()) / this.descriptor().denominator());
    }

    public final S negate() {
        return this.wrap(-myNumerator);
    }

    public final double norm() {
        return PrimitiveFunction.ABS.invoke(this.doubleValue());
    }

    public final S signum() {
        if (myNumerator == 0L) {
            return this.wrap(0L);
        } else if (myNumerator < 0L) {
            return this.wrap(-this.descriptor().denominator());
        } else {
            return this.wrap(this.descriptor().denominator());
        }
    }

    public final S subtract(double scalarSubtrahend) {
        return this.wrap(myNumerator - Math.round(scalarSubtrahend * this.descriptor().denominator()));
    }

    public final S subtract(S scalarSubtrahend) {
        return this.wrap(myNumerator - scalarSubtrahend.numerator());
    }

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

    public final String toString(NumberContext context) {
        return this.toBigDecimal(context).toPlainString();
    }

    private final BigDecimal toBigDecimal(NumberContext context) {
        return new BigDecimal(myNumerator).divide(new BigDecimal(this.descriptor().denominator()), context.getMathContext());
    }

    protected abstract Descriptor descriptor();

    protected abstract S wrap(long numerator);

    final long numerator() {
        return myNumerator;
    }

}
