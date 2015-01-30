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
package org.ojalgo.matrix.decomposition;

import java.math.BigDecimal;

import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.decomposition.QRDecomposition.Big;
import org.ojalgo.matrix.decomposition.QRDecomposition.Complex;
import org.ojalgo.matrix.decomposition.QRDecomposition.Primitive;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.task.DeterminantTask;
import org.ojalgo.scalar.ComplexNumber;

/**
 * QR: [A] = [Q][R] Decomposes [this] into [Q] and [R] where:
 * <ul>
 * <li>[Q] is an orthogonal matrix (orthonormal columns). It has the same number of rows as [this].</li>
 * <li>[R] is a right (upper) triangular matrix. It has the same number of columns as [this].</li>
 * <li>[this] = [Q][R]</li>
 * </ul>
 * Note: Either Q or R will be square. The interface does not specify which.
 *
 * @author apete
 */
public interface QR<N extends Number> extends MatrixDecomposition<N>, DeterminantTask<N> {

    @SuppressWarnings("unchecked")
    public static <N extends Number> QR<N> make(final Access2D<N> typical) {

        final N tmpNumber = typical.get(0, 0);

        if (tmpNumber instanceof BigDecimal) {
            return (QR<N>) QR.makeBig();
        } else if (tmpNumber instanceof ComplexNumber) {
            return (QR<N>) QR.makeComplex();
        } else if (tmpNumber instanceof Double) {

            final int tmpMaxDim = (int) Math.max(typical.countRows(), typical.countColumns());

            if ((tmpMaxDim <= 16) || (tmpMaxDim >= 46340)) { //16,16,8
                return (QR<N>) new RawQR();
            } else {
                return (QR<N>) QR.makePrimitive();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static QR<BigDecimal> makeBig() {
        return new Big();
    }

    public static QR<ComplexNumber> makeComplex() {
        return new Complex();
    }

    public static QR<Double> makePrimitive() {
        return new Primitive();
    }

    /**
     * @param matrix A matrix to decompose
     * @return true if the computation suceeded; false if not
     */
    boolean compute(Access2D<?> matrix, boolean fullSize);

    N getDeterminant();

    MatrixStore<N> getQ();

    MatrixStore<N> getR();

    int getRank();

    /**
     * The QR decompostion always exists, even if the matrix does not have full column rank, so the compute method will
     * never fail. The primary use of the QR decomposition is in the least squares solution of overdetermined systems of
     * simultaneous linear equations. This will fail if the matrix does not have full column rank. The rank must be
     * equal to the number of columns.
     */
    boolean isFullColumnRank();

}
