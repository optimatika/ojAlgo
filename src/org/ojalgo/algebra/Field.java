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
 * A field is a commutative ring (even the multiplication operation) with notions of addition, subtraction,
 * multiplication, and division. Any field may be used as the scalars for a vector space, which is the
 * standard general context for linear algebra.
 * </p>
 * <p>
 * A division ring is a ring in which division is possible. Division rings differ from fields only in that
 * their multiplication is not required to be commutative. In terms of a Java interface/class there is no need
 * to differentiate between a field and a division ring.
 * </p>
 *
 * @author apete
 * @see <a href="https://en.wikipedia.org/wiki/Field_(mathematics)">Field</a>
 * @see <a href="https://en.wikipedia.org/wiki/Division_ring">Division ring</a>
 */
public interface Field<S> extends Ring<S>, Group.Multiplicative<S>, Operation.Subtraction<S>, Operation.Division<S> {

}
