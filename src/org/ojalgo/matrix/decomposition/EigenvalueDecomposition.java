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

import org.ojalgo.access.Access2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;

abstract class EigenvalueDecomposition<N extends Number> extends GenericDecomposition<N> implements Eigenvalue<N> {

    private MatrixStore<N> myD = null;
    private Array1D<ComplexNumber> myEigenvalues = null;
    private boolean myEigenvaluesOnly = false;
    private MatrixStore<N> myV = null;

    protected EigenvalueDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory) {
        super(aFactory);
    }

    public final boolean checkAndCompute(final Access2D<?> matrix) {
        return this.compute(matrix, MatrixUtils.isHermitian(matrix), false);
    }

    public boolean computeValuesOnly(final Access2D<?> matrix) {
        return this.compute(matrix, this.isHermitian(), true);
    }

    public final boolean decompose(final Access2D<?> matrix) {
        return this.compute(matrix, false);
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

        if ((myV == null) && !myEigenvaluesOnly && this.isComputed()) {
            myV = this.makeV();
        }

        return myV;
    }

    public DecompositionStore<N> preallocate(final Access2D<N> template) {
        final long tmpCountRows = template.countRows();
        return this.preallocate(tmpCountRows, tmpCountRows);
    }

    public DecompositionStore<N> preallocate(final Access2D<N> templateBody, final Access2D<N> templateRHS) {
        return this.preallocate(templateRHS.countRows(), templateRHS.countColumns());
    }

    @Override
    public void reset() {

        super.reset();

        myD = null;
        myEigenvalues = null;
        myV = null;

        myEigenvaluesOnly = false;
    }

    public final MatrixStore<N> solve(final Access2D<N> rhs) {
        return this.getInverse().multiply(rhs);
    }

    public final MatrixStore<N> solve(final Access2D<N> rhs, final DecompositionStore<N> preallocated) {
        preallocated.fillByMultiplying(this.getInverse(), rhs);
        return preallocated;
    }

    protected final boolean compute(final Access2D<?> aMtrx, final boolean symmetric, final boolean eigenvaluesOnly) {

        this.reset();

        myEigenvaluesOnly = eigenvaluesOnly;

        boolean retVal = false;

        try {

            if (symmetric) {

                retVal = this.doSymmetric(aMtrx, eigenvaluesOnly);

            } else {

                retVal = this.doNonsymmetric(aMtrx, eigenvaluesOnly);
            }

        } catch (final Exception anException) {

            BasicLogger.error(anException.toString());

            this.reset();

            retVal = false;
        }

        return this.computed(retVal);
    }

    protected abstract boolean doNonsymmetric(final Access2D<?> aMtrx, final boolean eigenvaluesOnly);

    protected abstract boolean doSymmetric(final Access2D<?> aMtrx, final boolean eigenvaluesOnly);

    protected abstract MatrixStore<N> makeD();

    protected abstract Array1D<ComplexNumber> makeEigenvalues();

    protected abstract MatrixStore<N> makeV();

    final void setD(final MatrixStore<N> newD) {
        myD = newD;
    }

    final void setEigenvalues(final Array1D<ComplexNumber> newEigenvalues) {
        myEigenvalues = newEigenvalues;
    }

    final void setV(final MatrixStore<N> newV) {
        myV = newV;
    }

}
