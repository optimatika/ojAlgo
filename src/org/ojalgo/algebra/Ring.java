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
 * A ring is a commutative group (addition operation) with a second binary operation (multiplication) that is
 * distributive over the commutative group operation and is associative. Note that multiplications is not
 * required to be commutative.
 * </p>
 *
 * @author apete
 * @see <a href="https://en.wikipedia.org/wiki/Ring_(mathematics)">Ring</a>
 * @see <a href="https://en.wikipedia.org/wiki/Distributive_property">Distributive property</a>
 * @see <a href="https://en.wikipedia.org/wiki/Associative_property">Associative property</a>
 */
public interface Ring<S> extends Group.Additive<S>, Operation.Multiplication<S> {

}
