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
 * A vector space is a set of objects called vectors, where a vector is a tuple of fields/scalars/numbers.
 * Each vector space has two operations: vector addition and scalar multiplication. Eight axioms must be
 * satisfied. The first four are the group axioms of the additive group of vectors. The remaining four relates
 * to scalar multiplication, and are:
 * </p>
 * <ul>
 * <li>Compatibility of scalar multiplication with field multiplication: a(bV) = (ab)V</li>
 * <li>Identity element of scalar multiplication: 1V = V, where 1 denotes the multiplicative identity of the
 * field.</li>
 * <li>Distributivity of scalar multiplication with respect to vector addition: a(U + V) = aU + aV</li>
 * <li>Distributivity of scalar multiplication with respect to field addition: (a + b)V = aV + bV</li>
 * </ul>
 * <p>
 * To enable the use of existing Java classes as scalars this interface declares the scalar type to be a
 * subclass of {@linkplain Number} rather than an implementation of {@linkplain Field}.
 * </p>
 * <p>
 * Any field is also a vector space in itself.
 * </p>
 *
 * @param <V> The vector type
 * @param <F> The scalar type
 * @author apete
 * @see Group.Additive
 * @see Field
 * @see <a href="https://en.wikipedia.org/wiki/Vector_space">Vector space</a>
 * @see <a href="https://en.wikipedia.org/wiki/Examples_of_vector_spaces">Examples of vector spaces</a>
 */
public interface VectorSpace<V, F extends Number> extends Group.Additive<V>, ScalarOperation.Multiplication<V, F> {

    /**
     * <p>
     * <b>This method will (most likely) be moved to some other interface in the future! Just have to figure
     * out where it fits...</b>
     * </p>
     * <p>
     * The conjugate transpose of a matrix and/or the conjugate of a scalar/field like ComplexNumber or
     * Quaternion.
     * </p>
     * <p>
     * The conjugate transpose of a real matrix is simply its transpose.
     * </p>
     */
    V conjugate();

}
