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
package org.ojalgo.algebra;

/**
 * <p>
 * A group is a set of elements paired with a binary operation. Four conditions called the group axioms must
 * be satisfied:
 * </p>
 * <ul>
 * <li>Closure: If A and B are both members of the set then the result of A op B is also a member.</li>
 * <li>Associativity: Invocation/execution order doesn't matter - ((A op B) op C) == (A op (B op C))</li>
 * <li>The identity property: There is an identity element in the set, I, so that I op A == A op I == A</li>
 * <li>The inverse property: For each element in the set there must be an inverse element (opposite or
 * reciprocal) so that A<sup>-1</sup> op A == A op A<sup>-1</sup> == I</li>
 * </ul>
 * <p>
 * Note that commutativity is not a requirement - A op B doesn't always have to be equal to B op A. If the
 * operation is commutative then the group is called an abelian group or simply a commutative group.
 * </p>
 *
 * @author apete
 * @see <a href="https://en.wikipedia.org/wiki/Group_(mathematics)">Group</a>
 */
public interface Group {

    /**
     * @see <a href="https://en.wikipedia.org/wiki/Additive_group">Additive group</a>
     */
    public interface Additive<S> extends Group, Operation.Addition<S> {

        /**
         * The additive inverse of this.
         *
         * @return <code>-this</code>.
         */
        S negate();

    }

    /**
     * @see <a href="https://en.wikipedia.org/wiki/Multiplicative_group">Multiplicative group</a>
     */
    public interface Multiplicative<S> extends Group, Operation.Multiplication<S> {

        /**
         * The multiplicative inverse.
         *
         * @return <code>IDENTITY / this</code>.
         */
        S invert();

    }

}
