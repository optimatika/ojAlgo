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
package org.ojalgo.matrix.decomposition;

import java.math.BigDecimal;

import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * Tridiagonal: [A] = [Q][D][Q]<sup>H</sup> Any square symmetric (hermitian) matrix [A] can be factorized by
 * similarity transformations into the form, [A]=[Q][D][Q]<sup>-1</sup> where [Q] is an orthogonal (unitary)
 * matrix and [D] is a real symmetric tridiagonal matrix. Note that [D] can/should be made real even when [A]
 * has complex elements. Since [Q] is orthogonal (unitary) [Q]<sup>-1</sup> = [Q]<sup>H</sup> and when it is
 * real [Q]<sup>H</sup> = [Q]<sup>T</sup>.
 *
 * @author apete
 */
public interface Tridiagonal<N extends Number> extends MatrixDecomposition<N> {

    interface Factory<N extends Number> extends MatrixDecomposition.Factory<Tridiagonal<N>> {

    }

    public static final Factory<BigDecimal> BIG = typical -> new DeferredTridiagonal.Big();

    public static final Factory<ComplexNumber> COMPLEX = typical -> new DeferredTridiagonal.Complex();

    public static final Factory<Double> PRIMITIVE = typical -> new DeferredTridiagonal.Primitive();

    public static final Factory<Quaternion> QUATERNION = typical -> new DeferredTridiagonal.Quat();

    public static final Factory<RationalNumber> RATIONAL = typical -> new DeferredTridiagonal.Rational();

    @SuppressWarnings("unchecked")
    public static <N extends Number> Tridiagonal<N> make(final Access2D<N> typical) {

        final N tmpNumber = typical.get(0, 0);

        if (tmpNumber instanceof BigDecimal) {
            return (Tridiagonal<N>) BIG.make(typical);
        } else if (tmpNumber instanceof ComplexNumber) {
            return (Tridiagonal<N>) COMPLEX.make(typical);
        } else if (tmpNumber instanceof Double) {
            return (Tridiagonal<N>) PRIMITIVE.make(typical);
        } else {
            throw new IllegalArgumentException();
        }
    }

    static <N extends Number> boolean equals(final MatrixStore<N> matrix, final Tridiagonal<N> decomposition, final NumberContext context) {

        // Check that [A] == [Q][D][Q]<sup>T</sup>
        return Access2D.equals(matrix, Tridiagonal.reconstruct(decomposition), context);

        // Check that Q is orthogonal/unitary...
    }

    static <N extends Number> MatrixStore<N> reconstruct(final Tridiagonal<N> decomposition) {
        final MatrixStore<N> tmpQ = decomposition.getQ();
        return tmpQ.multiply(decomposition.getD()).multiply(tmpQ.conjugate());
    }

    MatrixStore<N> getD();

    MatrixStore<N> getQ();

    default MatrixStore<N> reconstruct() {
        return Tridiagonal.reconstruct(this);
    }

}
