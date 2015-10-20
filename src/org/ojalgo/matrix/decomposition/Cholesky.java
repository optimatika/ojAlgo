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
import org.ojalgo.array.BasicArray;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.ComplexNumber;

/**
 * <p>
 * Cholesky: [A] = [L][L]<sup>H</sup> (or [R]<sup>H</sup>[R])
 * </p>
 * <p>
 * [A]<sup>H</sup> = [A] = [L][L]<sup>H</sup>
 * </p>
 * <p>
 * If [A] is symmetric and positive definite then the general LU decomposition - [P][L][D][U] - becomes
 * [I][L][D][L]<sup>T</sup> (or [I][U]<sup>T</sup>[D][U]). [I] can be left out and [D] is normally split in
 * halves and merged with [L] (and/or [U]). We'll express it as [A] = [L][L]<sup>T</sup>.
 * </p>
 * <p>
 * A cholesky decomposition is still/also an LU decomposition where [P][L][D][U] =&gt; [L][L]<sup>T</sup>.
 * </p>
 *
 * @author apete
 */
public interface Cholesky<N extends Number> extends LDU<N>, MatrixDecomposition.Hermitian<N> {

    @SuppressWarnings("unchecked")
    public static <N extends Number> Cholesky<N> make(final Access2D<N> typical) {

        final N tmpNumber = typical.get(0, 0);

        if (tmpNumber instanceof BigDecimal) {
            return (Cholesky<N>) new CholeskyDecomposition.Big();
        } else if (tmpNumber instanceof ComplexNumber) {
            return (Cholesky<N>) new CholeskyDecomposition.Complex();
        } else if (tmpNumber instanceof Double) {
            if ((32L < typical.countColumns()) && (typical.count() <= BasicArray.MAX_ARRAY_SIZE)) {
                return (Cholesky<N>) new CholeskyDecomposition.Primitive();
            } else {
                return (Cholesky<N>) new RawCholesky();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static Cholesky<BigDecimal> makeBig() {
        return new CholeskyDecomposition.Big();
    }

    public static Cholesky<ComplexNumber> makeComplex() {
        return new CholeskyDecomposition.Complex();
    }

    public static Cholesky<Double> makePrimitive() {
        return new CholeskyDecomposition.Primitive();
    }

    /**
     * To use the Cholesky decomposition rather than the LU decomposition the matrix must be symmetric and
     * positive definite. It is recommended that the decomposition algorithm checks for this during
     * calculation. Possibly the matrix could be assumed to be symmetric (to improve performance) but tests
     * should be made to assure the matrix is positive definite.
     *
     * @return true if the tests did not fail.
     */
    public boolean isSPD();

    /**
     * Must implement either {@link #getL()} or {@link #getR()}.
     */
    default MatrixStore<N> getL() {
        return this.getR().conjugate();
    }

    /**
     * Must implement either {@link #getL()} or {@link #getR()}.
     */
    default MatrixStore<N> getR() {
        return this.getL().conjugate();
    }

    default MatrixStore<N> reconstruct() {
        return MatrixUtils.reconstruct(this);
    }

}
