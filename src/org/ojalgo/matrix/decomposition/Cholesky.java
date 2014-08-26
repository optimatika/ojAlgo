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
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.task.DeterminantTask;

/**
 * Cholesky: [A] = [L][L]<sup>T</sup>
 * <p>
 * If [A] is symmetric and positive definite then the general LU decomposition - [P][L][D][U] - becomes
 * [I][L][D][L]<sup>T</sup> (or [I][U]<sup>T</sup>[D][U]). [I] can be left out and [D] is normally split in halves and
 * merged with [L] (and/or [U]). We'll express it as [A] = [R]<sup>T</sup>[R].
 * </p>
 * <p>
 * A cholesky decomposition is still/also an LU decomposition where [P][L][D][U] => [R]<sup>T</sup>[R].
 * </p>
 * 
 * @author apete
 */
public interface Cholesky<N extends Number> extends MatrixDecomposition<N>, DeterminantTask<N> {

    /**
     * To use the Cholesky decomposition rather than the LU decomposition the matrix must be symmetric and positive
     * definite. It is recommended that the decomposition algorithm checks for this during calculation. Possibly the
     * matrix could be assumed to be symmetric (to improve performance) but tests should be made to assure the matrix is
     * positive definite.
     * 
     * @return true if the tests did not fail.
     */
    public boolean isSPD();

    boolean compute(final Access2D<?> matrix, final boolean checkHermitian);

    N getDeterminant();

    MatrixStore<N> getL();

}
