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

import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.decomposition.task.DeterminantTask;
import org.ojalgo.matrix.store.MatrixStore;

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
