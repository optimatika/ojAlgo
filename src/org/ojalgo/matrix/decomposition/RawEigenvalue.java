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

import java.util.Arrays;
import java.util.Optional;

import org.ojalgo.access.Access2D;
import org.ojalgo.access.Access2D.Collectable;
import org.ojalgo.access.Structure2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.blas.AXPY;
import org.ojalgo.array.blas.COPY;
import org.ojalgo.array.blas.DOT;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.ComplexAggregator;
import org.ojalgo.matrix.decomposition.function.ExchangeColumns;
import org.ojalgo.matrix.decomposition.function.RotateRight;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.matrix.task.TaskException;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;

/**
 * Eigenvalues and eigenvectors of a real matrix.
 * <P>
 * If A is symmetric, then A = V*D*V' where the eigenvalue matrix D is diagonal and the eigenvector matrix V
 * is orthogonal. I.e. A = V.times(D.times(V.transpose())) and V.times(V.transpose()) equals the identity
 * matrix.
 * <P>
 * If A is not symmetric, then the eigenvalue matrix D is block diagonal with the real eigenvalues in 1-by-1
 * blocks and any complex eigenvalues, lambda + i*mu, in 2-by-2 blocks, [lambda, mu; -mu, lambda]. The columns
 * of V represent the eigenvectors in the sense that A*V = V*D, i.e. A.times(V) equals V.times(D). The matrix
 * V may be badly conditioned, or even singular, so the validity of the equation A = V*D*inverse(V) depends
 * upon V.cond().
 **/
abstract class RawEigenvalue extends RawDecomposition implements Eigenvalue<Double> {

    static final class Dynamic extends RawEigenvalue {

        Dynamic() {
            super();
        }

        public boolean isHermitian() {
            return this.checkSymmetry();
        }

        @Override
        protected boolean doDecompose(final double[][] data, final boolean valuesOnly) {

            if (this.checkSymmetry()) {
                this.doSymmetric(data, valuesOnly);
            } else {
                this.doGeneral(data, valuesOnly);
            }

            return this.computed(true);
        }

    }

    static final class General extends RawEigenvalue {

        General() {
            super();
        }

        public boolean isHermitian() {
            return false;
        }

        @Override
        protected boolean doDecompose(final double[][] data, final boolean valuesOnly) {

            this.doGeneral(data, valuesOnly);

            return this.computed(true);
        }

    }

    static final class Symmetric extends RawEigenvalue implements MatrixDecomposition.Solver<Double> {

        Symmetric() {
            super();
        }

        public MatrixStore<Double> getSolution(final Collectable<Double, ? super PhysicalStore<Double>> rhs) {
            final long numberOfEquations = rhs.countRows();
            final DecompositionStore<Double> tmpPreallocated = this.allocate(numberOfEquations, numberOfEquations);
            return this.getSolution(rhs, tmpPreallocated);
        }

        public boolean isHermitian() {
            return true;
        }

        public PhysicalStore<Double> preallocate(final Structure2D template) {
            final long numberOfEquations = template.countRows();
            return this.allocate(numberOfEquations, numberOfEquations);
        }

        public PhysicalStore<Double> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
            return this.allocate(templateBody.countRows(), templateRHS.countColumns());
        }

        @Override
        protected boolean doDecompose(final double[][] data, final boolean valuesOnly) {

            this.doSymmetric(data, valuesOnly);

            return this.computed(true);
        }

    }

    /**
     * Arrays for internal storage of eigenvalues.
     *
     * @serial internal storage of eigenvalues.
     */
    private double[] d = null, e = null;
    private transient MatrixStore<Double> myInverse = null;
    private boolean myOrdered = true;
    /**
     * Array for internal storage of eigenvectors.
     *
     * @serial internal storage of eigenvectors.
     */
    private double[][] myTransposedV = null;

    protected RawEigenvalue() {
        super();
    }

    public Double calculateDeterminant(final Access2D<?> matrix) {

        final double[][] tmpData = this.reset(matrix, false);

        this.getRawInPlaceStore().fillMatching(matrix);

        this.doDecompose(tmpData, true);

        return this.getDeterminant();
    }

    public boolean computeValuesOnly(final ElementsSupplier<Double> matrix) {

        final double[][] tmpData = this.reset(matrix, false);

        matrix.supplyTo(this.getRawInPlaceStore());

        return this.doDecompose(tmpData, true);
    }

    public boolean decompose(final Access2D.Collectable<Double, ? super PhysicalStore<Double>> matrix) {

        final double[][] tmpData = this.reset(matrix, false);

        matrix.supplyTo(this.getRawInPlaceStore());

        return this.doDecompose(tmpData, false);
    }

    /**
     * Return the block diagonal eigenvalue matrix
     *
     * @return D
     */
    public RawStore getD() {
        final int n = this.getRowDim();
        final RawStore X = new RawStore(n, n);
        final double[][] D = X.data;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                D[i][j] = ZERO;
            }
            D[i][i] = d[i];
            if (e[i] > 0) {
                D[i][i + 1] = e[i];
            } else if (e[i] < 0) {
                D[i][i - 1] = e[i];
            }
        }
        return X;
    }

    public Double getDeterminant() {

        final AggregatorFunction<ComplexNumber> tmpVisitor = ComplexAggregator.getSet().product();

        this.getEigenvalues().visitAll(tmpVisitor);

        return tmpVisitor.getNumber().doubleValue();
    }

    public Array1D<ComplexNumber> getEigenvalues() {

        final double[] tmpRe = this.getRealParts();
        final double[] tmpIm = this.getImaginaryParts();

        final Array1D<ComplexNumber> retVal = Array1D.COMPLEX.makeZero(tmpRe.length);

        for (int i = 0; i < retVal.size(); i++) {
            retVal.set(i, ComplexNumber.of(tmpRe[i], tmpIm[i]));
        }

        // retVal.sortDescending();

        return retVal;
    }

    public void getEigenvalues(final double[] realParts, final Optional<double[]> imaginaryParts) {

        final int length = realParts.length;

        System.arraycopy(this.getRealParts(), 0, realParts, 0, length);

        if (imaginaryParts.isPresent()) {
            System.arraycopy(this.getImaginaryParts(), 0, imaginaryParts.get(), 0, length);
        }
    }

    public MatrixStore<Double> getInverse() {
        final int n = this.getRowDim();
        return this.getInverse(this.allocate(n, n));
    }

    public MatrixStore<Double> getInverse(final PhysicalStore<Double> preallocated) {

        if (myInverse == null) {

            final int dim = d.length;

            final RawStore tmpMtrx = new RawStore(dim, dim);

            double max = ONE;

            for (int i = 0; i < dim; i++) {
                final double val = d[i];
                max = MAX.invoke(max, ABS.invoke(val));
                if (PrimitiveScalar.isSmall(max, val)) {
                    for (int j = 0; j < dim; j++) {
                        tmpMtrx.set(i, j, ZERO);
                    }
                } else {
                    final double[] colVi = myTransposedV[i];
                    for (int j = 0; j < dim; j++) {
                        tmpMtrx.set(i, j, colVi[j] / val);
                    }
                }
            }

            myInverse = this.getV().multiply(tmpMtrx);
        }

        return myInverse;
    }

    public MatrixStore<Double> getSolution(final Collectable<Double, ? super PhysicalStore<Double>> rhs, final PhysicalStore<Double> preallocated) {
        return null;
    }

    public ComplexNumber getTrace() {

        final AggregatorFunction<ComplexNumber> tmpVisitor = ComplexAggregator.getSet().sum();

        this.getEigenvalues().visitAll(tmpVisitor);

        return tmpVisitor.getNumber();
    }

    /**
     * Return the eigenvector matrix
     *
     * @return V
     */
    public MatrixStore<Double> getV() {
        final int n = this.getRowDim();
        return new RawStore(myTransposedV, n, n).logical().transpose().get();
    }

    public MatrixStore<Double> invert(final Access2D<?> original, final PhysicalStore<Double> preallocated) throws TaskException {

        final double[][] tmpData = this.reset(original, false);

        this.getRawInPlaceStore().fillMatching(original);

        this.doDecompose(tmpData, false);

        if (this.isSolvable()) {
            return this.getInverse(preallocated);
        } else {
            throw TaskException.newNotInvertible();
        }
    }

    public final boolean isOrdered() {
        return myOrdered;
    }

    public boolean isSolvable() {
        return this.isComputed() && this.isHermitian();
    }

    public MatrixStore<Double> reconstruct() {
        return Eigenvalue.reconstruct(this);
    }

    @Override
    public void reset() {
        myInverse = null;
    }

    public void setOrdered(final boolean ordered) {
        myOrdered = ordered;
    }

    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws TaskException {

        final double[][] tmpData = this.reset(body, false);

        this.getRawInPlaceStore().fillMatching(body);

        this.doDecompose(tmpData, false);

        if (this.isSolvable()) {

            preallocated.fillMatching(rhs);

            return this.getInverse().multiply(preallocated);

        } else {
            throw TaskException.newNotSolvable();
        }
    }

    public MatrixStore<Double> solve(final MatrixStore<Double> rhs, final DecompositionStore<Double> preallocated) {
        return null;
    }

    protected abstract boolean doDecompose(double[][] data, boolean valuesOnly);

    final void doGeneral(final double[][] data, final boolean valuesOnly) {

        final int n = data.length;

        if ((d == null) || (n != d.length)) {
            if (valuesOnly) {
                myTransposedV = null;
            } else {
                myTransposedV = new double[n][n];
            }
            d = new double[n];
            e = new double[n];
        }

        // Reduce to Hessenberg form.
        EvD2D.orthes(data, myTransposedV, d);

        // Reduce Hessenberg to real Schur form.
        EvD2D.hqr2(data, d, e, myTransposedV);

    }

    final void doSymmetric(final double[][] data, final boolean valuesOnly) {

        final int size = data.length;

        if ((d == null) || (size != d.length)) {
            d = new double[size];
            e = new double[size];
        }
        myTransposedV = valuesOnly ? null : data;

        // > Tridiagonalize

        double scale;
        double h;
        double f;
        double g;
        double val;

        // Copy the last column (same as the last row) of z to d
        // The last row/column is the first to be worked on in the main loop
        COPY.invoke(data[size - 1], 0, d, 0, 0, size);

        // Householder reduction to tridiagonal form.
        for (int i1 = size - 1; i1 > 0; i1--) { // row index of target householder point
            final int l = i1 - 1; // col index of target householder point

            h = ZERO;

            // Calculate the norm of the row/col to zero out - to avoid under/overflow.
            scale = ZERO;
            for (int k4 = 0; k4 < i1; k4++) {
                scale = MAX.invoke(scale, ABS.invoke(d[k4]));
            }

            if (scale == ZERO) {
                // Skip generation, already zero
                e[i1] = d[l];
                for (int j = 0; j < i1; j++) {
                    d[j] = data[j][l]; // Copy "next" row/column to work on
                    data[j][i1] = ZERO; // Are both needed? - neither needed?
                    data[i1][j] = ZERO; // Could cause cache-misses - it was already zero!
                }

            } else {
                // Generate Householder vector.

                for (int k3 = 0; k3 < i1; k3++) {
                    val = d[k3] /= scale;
                    h += val * val; // d[k] * d[k]
                }
                f = d[l];
                g = SQRT.invoke(h);
                if (f > 0) {
                    g = -g;
                }
                e[i1] = scale * g;
                h = h - (f * g);
                d[l] = f - g;
                Arrays.fill(e, 0, i1, ZERO);

                // Apply similarity transformation to remaining columns.
                // Remaing refers to all columns "before" the target col
                for (int j = 0; j < i1; j++) {
                    f = d[j];
                    data[i1][j] = f;
                    final double[] tmpVt_j = data[j];
                    g = e[j] + (tmpVt_j[j] * f);
                    for (int k1 = j + 1; k1 <= l; k1++) {
                        val = tmpVt_j[k1];
                        g += val * d[k1];
                        e[k1] += val * f;
                    }
                    e[j] = g;
                }
                f = ZERO;
                for (int j = 0; j < i1; j++) {
                    e[j] /= h;
                    f += e[j] * d[j];
                }
                val = f / (h + h);
                AXPY.invoke(e, 0, 1, -val, d, 0, 1, 0, i1);
                for (int j = 0; j < i1; j++) {
                    f = d[j];
                    g = e[j];
                    for (int k2 = j; k2 <= l; k2++) {
                        data[j][k2] -= ((f * e[k2]) + (g * d[k2]));
                    }
                    d[j] = data[j][l];
                    data[j][i1] = ZERO;
                }
            }
            d[i1] = h;
        }

        // Accumulate transformations.
        for (int i2 = 0; i2 < (size - 1); i2++) {

            final double[] tmpVt_i = data[i2];
            final double[] tmpVt_i1 = data[i2 + 1];

            //V[n - 1][i] = V[i][i];
            tmpVt_i[size - 1] = tmpVt_i[i2];
            //V[i][i] = ONE;
            tmpVt_i[i2] = ONE;
            h = d[i2 + 1];
            if (h != ZERO) {
                for (int k5 = 0; k5 <= i2; k5++) {
                    //d[k] = V[k][i + 1] / h;
                    d[k5] = tmpVt_i1[k5] / h;
                }

                for (int j = 0; j <= i2; j++) {
                    final double tmpDotProd = DOT.invoke(tmpVt_i1, 0, data[j], 0, 0, i2 + 1);
                    //                    for (int k = 0; k <= i; k++) {
                    //                        //g += V[k][i + 1] * V[k][j];
                    //                        g += tmpVt_i1[k] * tmpVt_j[k];
                    //                    }
                    AXPY.invoke(data[j], 0, 1, -tmpDotProd, d, 0, 1, 0, i2 + 1);
                    //                    for (int k = 0; k <= i; k++) {
                    //                        //V[k][j] -= g * d[k];
                    //                        tmpVt_j[k] -= g * d[k];
                    //                    }
                }
            }
            Arrays.fill(tmpVt_i1, 0, i2 + 1, ZERO);
            //            for (int k = 0; k <= i; k++) {
            //                //V[k][i + 1] = ZERO;
            //                tmpVt_i1[k] = ZERO;
            //            }
        }

        for (int j = 0; j < size; j++) {
            //d[j] = V[n - 1][j];
            d[j] = data[j][size - 1];
            //V[n - 1][j] = ZERO;
            data[j][size - 1] = ZERO;
        }
        //V[n - 1][n - 1] = ONE;
        data[size - 1][size - 1] = ONE;
        e[0] = ZERO;

        for (int i = 1; i < size; i++) {
            e[i - 1] = e[i];
        }
        e[size - 1] = ZERO;

        // Tridiagonalize > Diagonalize

        final RotateRight tmpRotateRight = valuesOnly ? RotateRight.NULL : new RotateRight() {

            public void rotateRight(final int low, final int high, final double cos, final double sin) {
                final double[] tmpVi0 = data[low];
                double tmpVi0k;
                final double[] tmpVi1 = data[high];
                double tmpVi1k;

                for (int k = 0; k < size; k++) {

                    tmpVi0k = tmpVi0[k];
                    tmpVi1k = tmpVi1[k];

                    tmpVi0[k] = (cos * tmpVi0k) - (sin * tmpVi1k);
                    tmpVi1[k] = (sin * tmpVi0k) + (cos * tmpVi1k);
                }

            }
        };
        HermitianEvD.tql2(d, e, tmpRotateRight);

        // Diagonalize > Sort

        if (this.isOrdered()) {
            final ExchangeColumns tmpExchangeColumns = valuesOnly ? ExchangeColumns.NULL : new ExchangeColumns() {

                public void exchangeColumns(final int colA, final int colB) {
                    final double[] tmp = data[colA];
                    data[colA] = data[colB];
                    data[colB] = tmp;

                }
            };
            EigenvalueDecomposition.sort(d, tmpExchangeColumns);
        }

    }

    /**
     * Return the imaginary parts of the eigenvalues
     *
     * @return imag(diag(D))
     */
    double[] getImaginaryParts() {
        return e;
    }

    /**
     * Return the real parts of the eigenvalues
     *
     * @return real(diag(D))
     */
    double[] getRealParts() {
        return d;
    }

}
