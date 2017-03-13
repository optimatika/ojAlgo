/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import org.ojalgo.access.Access2D;
import org.ojalgo.access.Access2D.Collectable;
import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.decomposition.function.AccumulatorEvD;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;

abstract class EigenvalueDecomposition<N extends Number> extends GenericDecomposition<N> implements Eigenvalue<N> {

    static void tql2(final double[] d, final double[] e, final AccumulatorEvD mtrxV) {

        final int size = d.length;

        double shift = ZERO;
        double increment;

        double magnitude = ZERO;
        double epsilon;

        double d_l, e_l;

        int m;
        // Main loop
        for (int l = 0; l < size; l++) {

            d_l = d[l];
            e_l = e[l];

            // Find small subdiagonal element
            magnitude = MAX.invoke(magnitude, ABS.invoke(d_l) + ABS.invoke(e_l));
            epsilon = MACHINE_EPSILON * magnitude;

            m = l;
            while ((m < size) && (ABS.invoke(e[m]) > epsilon)) {
                m++;
            }

            // If m == l, d[l] is an eigenvalue, otherwise, iterate.
            if (m > l) {
                do {

                    // Compute implicit shift

                    double p = (d[l + 1] - d_l) / (e_l + e_l);
                    double r = HYPOT.invoke(p, ONE);
                    if (p < ZERO) {
                        r = -r;
                    }

                    d[l + 1] = e_l * (p + r);
                    increment = d_l - (d[l] = e_l / (p + r));
                    for (int i = l + 2; i < size; i++) {
                        d[i] -= increment;
                    }
                    shift += increment;

                    // Implicit QL transformation

                    double cos1 = ONE, sin1 = ZERO, cos2 = cos1;
                    double d_i, e_i;

                    p = d[m];
                    for (int i = m - 1; i >= l; i--) {
                        d_i = d[i];
                        e_i = e[i];

                        r = HYPOT.invoke(p, e_i);

                        e[i + 1] = sin1 * r;

                        cos2 = cos1;

                        cos1 = p / r;
                        sin1 = e_i / r;

                        d[i + 1] = (cos2 * p) + (sin1 * ((cos1 * cos2 * e_i) + (sin1 * d_i)));

                        p = (cos1 * d_i) - (sin1 * cos2 * e_i);

                        // Accumulate transformation - rotate the eigenvector matrix
                        mtrxV.rotateRight(i, i + 1, cos1, sin1);
                    }

                    d_l = d[l] = cos1 * p;
                    e_l = e[l] = sin1 * p;

                } while (ABS.invoke(e[l]) > epsilon); // Check for convergence
            } // End if (m > l)

            d[l] += shift;
            e[l] = ZERO;

        } // End main loop - l

    }

    private MatrixStore<N> myD = null;
    private Array1D<ComplexNumber> myEigenvalues = null;
    private boolean myEigenvaluesOnly = false;
    private MatrixStore<N> myV = null;

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

    public boolean computeValuesOnly(final ElementsSupplier<N> matrix) {
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

        if ((myV == null) && !myEigenvaluesOnly && this.isComputed()) {
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

        myEigenvaluesOnly = false;
    }

    private final boolean compute(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix, final boolean symmetric, final boolean eigenvaluesOnly) {

        this.reset();

        myEigenvaluesOnly = eigenvaluesOnly;

        boolean retVal = false;

        try {

            if (symmetric) {

                retVal = this.doHermitian(matrix, eigenvaluesOnly);

            } else {

                retVal = this.doGeneral(matrix, eigenvaluesOnly);
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

    final void setEigenvalues(final Array1D<ComplexNumber> newEigenvalues) {
        myEigenvalues = newEigenvalues;
    }

    final void setV(final MatrixStore<N> newV) {
        myV = newV;
    }

}
