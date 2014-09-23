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
package org.ojalgo.scalar;

import java.math.BigDecimal;

import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * A {@linkplain Scalar} is:
 * <ol>
 * <li>An abstraction of a vector/matrix element.</li>
 * <li>A {@linkplain Number} decorator, increasing the number of things you can do with them.</li>
 * </ol>
 * </p>
 * <p>
 * Theoretically it is a Field or at least a Division ring.
 * </p>
 * <p>
 * A group is a set of elements paired with a binary operation. Four conditions called the group axioms must be
 * satisfied:
 * <ol>
 * <li>Closure: If A and B are both members of the set then the result of A op B is also a member.</li>
 * <li>Associativity: Invocation/execution order doesn't matter - ((A op B) op C) == (A op (B op C))</li>
 * <li>The identity property: There is an identity element in the set, I, so that I op A == A op I == A</li>
 * <li>The inverse property: For each element in the set there must be an inverse element (opposite or reciprocal) so
 * that A<sup>-1</sup> op A == A op A<sup>-1</sup> == I</li>
 * </ol>
 * Note that commutativity is not a requirement - A op B doesn't always have to be equal to B op A. If the operation is
 * commutative then the group is called an abelian group or simply a commutative group.
 * </p>
 * <p>
 * A ring is a commutative {@linkplain Group} (add operation) with a second binary operation (multiply) that is
 * distributive over the commutative group operation and is associative.
 * </p>
 * <p>
 * A field is a commutative ring (even the multiplication operation) with notions of addition, subtraction,
 * multiplication, and division. Any field may be used as the scalars for a vector space, which is the standard general
 * context for linear algebra.
 * </p>
 * <p>
 * A division ring is a ring in which division is possible. Division rings differ from fields only in that their
 * multiplication is not required to be commutative.
 * </p>
 *
 * @author apete
 * @see <a href="http://en.wikipedia.org/wiki/Operation_%28mathematics%29">Operation</a>
 * @see <a href="http://en.wikipedia.org/wiki/Group_%28mathematics%29">Group</a>
 * @see <a href="http://en.wikipedia.org/wiki/Ring_%28mathematics%29">Ring</a>
 * @see <a href="http://en.wikipedia.org/wiki/Field_%28mathematics%29">Field</a>
 * @see <a href="http://en.wikipedia.org/wiki/Division_ring">Division ring</a>
 * @see <a href="http://en.wikipedia.org/wiki/Vector_space">Vector space</a>
 */
public interface Scalar<N extends Number> extends Comparable<N> {

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

    /**
     * @see #add(double)
     * @see #add(Number)
     * @see #divide(double)
     * @see #divide(Number)
     * @see #multiply(double)
     * @see #multiply(Number)
     * @see #subtract(double)
     * @see #subtract(Number)
     */
    Scalar<N> add(double arg);

    /**
     * @see #add(double)
     * @see #add(Number)
     * @see #divide(double)
     * @see #divide(Number)
     * @see #multiply(double)
     * @see #multiply(Number)
     * @see #subtract(double)
     * @see #subtract(Number)
     */
    Scalar<N> add(N arg);

    /**
     * @see #conjugate()
     * @see #invert()
     * @see #negate()
     */
    Scalar<N> conjugate();

    /**
     * @see #add(double)
     * @see #add(Number)
     * @see #divide(double)
     * @see #divide(Number)
     * @see #multiply(double)
     * @see #multiply(Number)
     * @see #subtract(double)
     * @see #subtract(Number)
     */
    Scalar<N> divide(double arg);

    /**
     * @see #add(double)
     * @see #add(Number)
     * @see #divide(double)
     * @see #divide(Number)
     * @see #multiply(double)
     * @see #multiply(Number)
     * @see #subtract(double)
     * @see #subtract(Number)
     */
    Scalar<N> divide(N arg);

    double doubleValue();

    N getNumber();

    /**
     * @see #conjugate()
     * @see #invert()
     * @see #negate()
     */
    Scalar<N> invert();

    /**
     * @return true if this is equal to its own norm, modulus or absolute value (non-negative real part and no imaginary
     *         part); otherwise false.
     * @see #isAbsolute()
     * @see #isPositive()
     * @see #isZero()
     */
    boolean isAbsolute();

    /**
     * Defined as {@link #isAbsolute()} and not {@link #isZero()}. Note that the exact definition/behaviour of
     * {@link #isZero()} is specific to the implementation.
     *
     * @see #isAbsolute()
     * @see #isPositive()
     * @see #isZero()
     * @deprecated v36 Only plan to keep {@link #isAbsolute()} and {@link #isZero()}.
     */
    @Deprecated
    boolean isPositive();

    /**
     * Is this scalar (practically) zero or not. Typically a small range around zero needs to be interpreted as zero.
     * The exact size of that range depends on the characteristics/capabillities of the specific implementation. The
     * potential exactness of for instance {@link BigScalar} or {@link RationalNumber} should be reflected here as
     * having small(er), or empty, zero-intevalls.
     *
     * @return true if the norm, modulus or absolute value is (practically) zero; otherwise false.
     * @see #isAbsolute()
     * @see #isPositive()
     * @see #isZero()
     */
    boolean isZero();

    /**
     * @see #add(double)
     * @see #add(Number)
     * @see #divide(double)
     * @see #divide(Number)
     * @see #multiply(double)
     * @see #multiply(Number)
     * @see #subtract(double)
     * @see #subtract(Number)
     */
    Scalar<N> multiply(double arg);

    /**
     * @see #add(double)
     * @see #add(Number)
     * @see #divide(double)
     * @see #divide(Number)
     * @see #multiply(double)
     * @see #multiply(Number)
     * @see #subtract(double)
     * @see #subtract(Number)
     */
    Scalar<N> multiply(N arg);

    /**
     * @see #conjugate()
     * @see #invert()
     * @see #negate()
     */
    Scalar<N> negate();

    /**
     * this = signum(this) * norm(this)
     *
     * @return norm(this)
     */
    double norm();

    /**
     * this = signum(this) * norm(this)
     *
     * @return signum(this)
     */
    Scalar<N> signum();

    /**
     * @see #add(double)
     * @see #add(Number)
     * @see #divide(double)
     * @see #divide(Number)
     * @see #multiply(double)
     * @see #multiply(Number)
     * @see #subtract(double)
     * @see #subtract(Number)
     */
    Scalar<N> subtract(double arg);

    /**
     * @see #add(double)
     * @see #add(Number)
     * @see #divide(double)
     * @see #divide(Number)
     * @see #multiply(double)
     * @see #multiply(Number)
     * @see #subtract(double)
     * @see #subtract(Number)
     */
    Scalar<N> subtract(N arg);

    BigDecimal toBigDecimal();

    String toPlainString(NumberContext context);

    String toString(NumberContext context);

}
