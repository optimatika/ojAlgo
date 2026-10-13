/*
 * Copyright 1997-2026 Optimatika
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
package org.ojalgo.tensor;

import org.ojalgo.algebra.NormedVectorSpace;
import org.ojalgo.array.ArrayAnyD;

/**
 * An n:th-rank tensor in m-dimensional space is a mathematical object that has n indices and m^n components
 * and obeys certain transformation rules. Tensors are generalizations of scalars (that have no indices),
 * vectors (that have exactly one index), and matrices (that have exactly two indices) to an arbitrary number
 * of indices.
 * <p>
 * If all you want is multi-dimesional arrays this interface and its implementations is NOT what you're
 * looking for. In that case just use {@link ArrayAnyD} instead.
 *
 * @see "https://mathworld.wolfram.com/Tensor.html"
 * @author apete
 */
public interface Tensor<N extends Comparable<N>, T extends Tensor<N, T>> extends NormedVectorSpace<T, N> {

    /**
     * The total number of scalar components
     */
    default long components() {
        return Math.round(Math.pow(this.dimensions(), this.rank()));
    }

    /**
     * The conjugate of every element, with the order of the indices reversed:
     *
     * <pre>{@code
     * conjugate(T)[i1, i2, ..., in] = conj(T[in, ..., i2, i1])
     * }</pre>
     *
     * At the lower ranks this is the usual meaning of conjugate:
     * <ul>
     * <li>Rank 0, a scalar: the conjugate of a complex number or quaternion.
     * <li>Rank 1, a vector: the conjugate of each element. Reversing the order of a single index changes
     * nothing – a vector tensor is neither a row nor a column.
     * <li>Rank 2, a matrix: the conjugate transpose.
     * </ul>
     * For real elements the conjugate changes nothing, and only the reversal of the indices remains – the
     * transpose at rank 2.
     * <p>
     * For rank 3 and higher, the tensor product u ⊗ v ⊗ w becomes conj(w) ⊗ conj(v) ⊗ conj(u), the same
     * reversal of order as in (AB)<sup>H</sup> = B<sup>H</sup>A<sup>H</sup>. It is not the adjoint of the
     * tensor seen as a linear map between tensor spaces, as that depends on which indices are inputs and
     * which are outputs.
     */
    @Override
    T conjugate();

    /**
     * The range of the indices that identify the scalar components. Each index of a tensor ranges over the
     * number of dimensions.
     */
    int dimensions();

    default boolean isSameShape(final Tensor<?, ?> other) {
        return this.rank() == other.rank() && this.dimensions() == other.dimensions();
    }

    /**
     * The total number of indices required to uniquely identify each scalar component is called the order,
     * degree or rank of the tensor.
     */
    int rank();

}
