/*
 * Copyright 1997-2019 Optimatika
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

import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;

final class GeneralisedEigenvalue<N extends Number> implements Eigenvalue<N> {

    private final Cholesky<N> myCholesky;
    private final Eigenvalue<N> myEigenvalue;

    GeneralisedEigenvalue(final Cholesky<N> cholesky, final Eigenvalue<N> eigenvalue) {

        super();

        myCholesky = cholesky;
        myEigenvalue = eigenvalue;
    }

    public N calculateDeterminant(final Access2D<?> matrix) {
        return myEigenvalue.calculateDeterminant(matrix);
    }

    public boolean computeValuesOnly(final Collectable<N, ? super PhysicalStore<N>> matrix) {
        return myEigenvalue.computeValuesOnly(matrix);
    }

    public long countColumns() {
        return myEigenvalue.countColumns();
    }

    public long countRows() {
        return myEigenvalue.countRows();
    }

    public boolean decompose(final Collectable<N, ? super PhysicalStore<N>> matrix) {
        return myEigenvalue.decompose(matrix);
    }

    public MatrixStore<N> getD() {
        return myEigenvalue.getD();
    }

    public N getDeterminant() {
        return myEigenvalue.getDeterminant();
    }

    public Array1D<ComplexNumber> getEigenvalues() {
        return myEigenvalue.getEigenvalues();
    }

    public ComplexNumber getTrace() {
        return myEigenvalue.getTrace();
    }

    public MatrixStore<N> getV() {
        return myEigenvalue.getV();
    }

    public boolean isComputed() {
        return myEigenvalue.isComputed();
    }

    public boolean isHermitian() {
        return myEigenvalue.isHermitian();
    }

    public boolean isOrdered() {
        return myEigenvalue.isOrdered();
    }

    public void reset() {
        myEigenvalue.reset();
    }

}
