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

import org.ojalgo.access.Access2D;
import org.ojalgo.access.Access2D.Collectable;
import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.decomposition.function.ExchangeColumns;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;

abstract class EigenvalueDecomposition<N extends Number> extends GenericDecomposition<N> implements Eigenvalue<N> {

    /**
     * Sort eigenvalues and corresponding vectors.
     */
    static void sort(final double[] d, final ExchangeColumns mtrxV) {

        final int size = d.length;

        final int length = size - 1;
        for (int i = 0; i < length; i++) {

            int k = i;
            double p = d[i];
            for (int j = i + 1; j < size; j++) {
                if (d[j] > p) {
                    k = j;
                    p = d[j];
                }
            }
            if (k != i) {
                d[k] = d[i];
                d[i] = p;
                mtrxV.exchangeColumns(i, k);
            }
        }
    }

    private MatrixStore<N> myD = null;
    private Array1D<ComplexNumber> myEigenvalues = null;
    private MatrixStore<N> myV = null;
    private boolean myValuesOnly = false;

    protected EigenvalueDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory) {
        super(aFactory);
    }

    public N calculateDeterminant(final Access2D<?> matrix) {
        this.decompose(this.wrap(matrix));
        return this.getDeterminant();
    }

    public final boolean checkAndCompute(final MatrixStore<N> matrix) {
        return this.compute(matrix, MatrixUtils.isHermitian(matrix), false);
    }

    public boolean computeValuesOnly(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix) {
        return this.compute(matrix, this.isHermitian(), true);
    }

    public final boolean decompose(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix) {
        return this.compute(matrix, this.isHermitian(), false);
    }

    public final MatrixStore<N> getD() {

        if ((myD == null) && this.isComputed()) {
            myD = this.makeD();
        }

        return myD;
    }

    public final Array1D<ComplexNumber> getEigenvalues() {

        if ((myEigenvalues == null) && this.isComputed()) {
            myEigenvalues = this.makeEigenvalues();
        }

        return myEigenvalues;
    }

    public final MatrixStore<N> getV() {

        if ((myV == null) && !myValuesOnly && this.isComputed()) {
            myV = this.makeV();
        }

        return myV;
    }

    @Override
    public void reset() {

        super.reset();

        myD = null;
        myEigenvalues = null;
        myV = null;

        myValuesOnly = false;
    }

    private final boolean compute(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix, final boolean hermitian, final boolean valuesOnly) {

        this.reset();

        myValuesOnly = valuesOnly;

        boolean retVal = false;

        try {

            if (hermitian) {
                retVal = this.doHermitian(matrix, valuesOnly);
            } else {
                retVal = this.doGeneral(matrix, valuesOnly);
            }

        } catch (final Exception exc) {

            BasicLogger.error(exc.toString());

            this.reset();

            retVal = false;
        }

        return this.computed(retVal);
    }

    protected abstract boolean doGeneral(final Collectable<N, ? super PhysicalStore<N>> matrix, final boolean eigenvaluesOnly);

    protected abstract boolean doHermitian(final Collectable<N, ? super PhysicalStore<N>> matrix, final boolean eigenvaluesOnly);

    protected abstract MatrixStore<N> makeD();

    protected abstract Array1D<ComplexNumber> makeEigenvalues();

    protected abstract MatrixStore<N> makeV();

    final void setD(final MatrixStore<N> newD) {
        myD = newD;
    }

    final void setEigenvalues(final Array1D<ComplexNumber> eigenvalues) {
        myEigenvalues = eigenvalues;
    }

    final void setV(final MatrixStore<N> newV) {
        myV = newV;
    }

}
