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
package org.ojalgo.matrix.decomposition;

import java.math.BigDecimal;

import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.ComplexNumber;

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

    @SuppressWarnings("unchecked")
    public static <N extends Number> Tridiagonal<N> make(final Access2D<N> typical) {

        final N tmpNumber = typical.get(0, 0);

        if (tmpNumber instanceof BigDecimal) {
            return (Tridiagonal<N>) Tridiagonal.makeBig();
        } else if (tmpNumber instanceof ComplexNumber) {
            return (Tridiagonal<N>) Tridiagonal.makeComplex();
        } else if (tmpNumber instanceof Double) {
            return (Tridiagonal<N>) Tridiagonal.makePrimitive();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static Tridiagonal<BigDecimal> makeBig() {
        return new TridiagonalDecomposition.Big();
    }

    public static Tridiagonal<ComplexNumber> makeComplex() {
        return new TridiagonalDecomposition.Complex();
    }

    public static Tridiagonal<Double> makePrimitive() {
        return new TridiagonalDecomposition.Primitive();
    }

    MatrixStore<N> getD();

    MatrixStore<N> getQ();

    default MatrixStore<N> reconstruct() {
        return MatrixUtils.reconstruct(this);
    }

}
