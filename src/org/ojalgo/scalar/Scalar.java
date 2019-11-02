/*
 * Copyright 1997-2019 Optimatika
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

import java.lang.reflect.Array;
import java.math.BigDecimal;

import org.ojalgo.algebra.Field;
import org.ojalgo.algebra.NormedVectorSpace;
import org.ojalgo.algebra.ScalarOperation;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.structure.AccessScalar;
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * A {@linkplain Scalar} is:
 * </p>
 * <ol>
 * <li>An abstraction of a vector/matrix element.</li>
 * <li>A {@linkplain Comparable} decorator, increasing the number of things you can do with them.</li>
 * </ol>
 * <p>
 * Theoretically it is a Field or at least a Division ring.
 * </p>
 * <p>
 * The intention is that implementors should be final immutable subclasses of {@link Comparable} and that they
 * should be inline with the requirements for ValueBased classes.
 * </p>
 *
 * @author apete
 */
public interface Scalar<N extends Comparable<N>> extends AccessScalar<N>, Field<Scalar<N>>, NormedVectorSpace<Scalar<N>, N>,
        ScalarOperation.Addition<Scalar<N>, N>, ScalarOperation.Division<Scalar<N>, N>, ScalarOperation.Subtraction<Scalar<N>, N>, Comparable<N> {

    public interface Factory<N extends Comparable<N>> {

        N cast(Comparable<?> number);

        N cast(double value);

        Scalar<N> convert(Comparable<?> number);

        Scalar<N> convert(double value);

        @SuppressWarnings("unchecked")
        default N[] newArrayInstance(final int length) {
            return (N[]) Array.newInstance(this.zero().get().getClass(), length);
        }

        /**
         * @return The multiplicative identity element
         */
        Scalar<N> one();

        /**
         * @return The additive identity element
         */
        Scalar<N> zero();

    }

    static double doubleValue(final Comparable<?> number) {

        if (number == null) {
            return PrimitiveMath.ZERO;
        }

        if (number instanceof NumberDefinition) {
            return ((NumberDefinition) number).doubleValue();
        } else if (number instanceof Number) {
            return ((Number) number).doubleValue();
        } else {
            return PrimitiveMath.NaN;
        }
    }

    static float floatValue(final Comparable<?> number) {

        if (number == null) {
            return 0F;
        }

        if (number instanceof NumberDefinition) {
            return ((NumberDefinition) number).floatValue();
        } else if (number instanceof Number) {
            return ((Number) number).floatValue();
        } else {
            return Float.NaN;
        }
    }

    static int intValue(final Comparable<?> number) {

        if (number == null) {
            return 0;
        }

        if (number instanceof NumberDefinition) {
            return ((NumberDefinition) number).intValue();
        } else if (number instanceof Number) {
            return ((Number) number).intValue();
        } else {
            return Integer.MIN_VALUE;
        }
    }

    static long longValue(final Comparable<?> number) {

        if (number == null) {
            return 0L;
        }

        if (number instanceof NumberDefinition) {
            return ((NumberDefinition) number).longValue();
        } else if (number instanceof Number) {
            return ((Number) number).longValue();
        } else {
            return Long.MIN_VALUE;
        }
    }

    @Override
    default Scalar<N> add(final Scalar<N> addend) {
        return this.add(addend.get());
    }

    @Override
    default Scalar<N> divide(final Scalar<N> divisor) {
        return this.divide(divisor.get());
    }

    /**
     * @return true if this is equal to its own norm, modulus or absolute value (non-negative real part and no
     *         imaginary part); otherwise false.
     * @see #isAbsolute()
     */
    boolean isAbsolute();

    @Override
    default Scalar<N> multiply(final Scalar<N> multiplicand) {
        return this.multiply(multiplicand.get());
    }

    @Override
    default Scalar<N> subtract(final Scalar<N> subtrahend) {
        return this.subtract(subtrahend.get());
    }

    BigDecimal toBigDecimal();

    default String toPlainString(final NumberContext context) {
        return context.enforce(this.toBigDecimal()).toPlainString();
    }

    String toString(NumberContext context);

}
