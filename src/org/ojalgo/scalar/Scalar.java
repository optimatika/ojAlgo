/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import org.ojalgo.algebra.Field;
import org.ojalgo.algebra.NormedVectorSpace;
import org.ojalgo.algebra.ScalarOperation;
import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * A {@linkplain Scalar} is:
 * </p>
 * <ol>
 * <li>An abstraction of a vector/matrix element.</li>
 * <li>A {@linkplain Number} decorator, increasing the number of things you can do with them.</li>
 * </ol>
 * <p>
 * Theoretically it is a Field or at least a Division ring.
 * </p>
 * <p>
 * The intention is that implementors should be final immutable subclasses of {@link Number}and that they
 * should inline with the requirements for ValueBased classes.
 * </p>
 *
 * @author apete
 */
public interface Scalar<N extends Number> extends Field<Scalar<N>>, NormedVectorSpace<Scalar<N>, N>, ScalarOperation.Addition<Scalar<N>, N>,
        ScalarOperation.Division<Scalar<N>, N>, ScalarOperation.Subtraction<Scalar<N>, N>, Comparable<N> {

    public interface Factory<N extends Number> {

        N cast(double value);

        N cast(Number number);

        Scalar<N> convert(double value);

        Scalar<N> convert(Number number);

        /**
         * @return The multiplicative identity element
         */
        Scalar<N> one();

        /**
         * @return The additive identity element
         */
        Scalar<N> zero();

    }

    default Scalar<N> add(final Scalar<N> addend) {
        return this.add(addend.getNumber());
    }

    default Scalar<N> divide(final Scalar<N> divisor) {
        return this.divide(divisor.getNumber());
    }

    double doubleValue();

    N getNumber();

    /**
     * @return true if this is equal to its own norm, modulus or absolute value (non-negative real part and no
     *         imaginary part); otherwise false.
     * @see #isAbsolute()
     */
    boolean isAbsolute();

    default Scalar<N> multiply(final Scalar<N> multiplicand) {
        return this.multiply(multiplicand.getNumber());
    }

    default Scalar<N> subtract(final Scalar<N> subtrahend) {
        return this.subtract(subtrahend.getNumber());
    }

    BigDecimal toBigDecimal();

    default String toPlainString(final NumberContext context) {
        return context.enforce(this.toBigDecimal()).toPlainString();
    }

    String toString(NumberContext context);

}
