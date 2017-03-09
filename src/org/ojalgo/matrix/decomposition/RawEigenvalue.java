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

import java.util.Optional;

import org.ojalgo.access.Access2D;
import org.ojalgo.access.Access2D.Collectable;
import org.ojalgo.access.Structure2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.ComplexAggregator;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.matrix.store.operation.HouseholderHermitian;
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
        boolean doDecompose(final double[][] data, final boolean valuesOnly) {

            if (this.checkSymmetry()) {
                this.doDecomposeSymmetric(data, valuesOnly);
            } else {
                this.doDecomposeGeneral(data, valuesOnly);
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
        boolean doDecompose(final double[][] data, final boolean valuesOnly) {

            this.doDecomposeGeneral(data, valuesOnly);

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
        boolean doDecompose(final double[][] data, final boolean valuesOnly) {

            this.doDecomposeSymmetric(data, valuesOnly);

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

    /**
     * Array for internal storage of eigenvectors.
     *
     * @serial internal storage of eigenvectors.
     */
    private double[][] myTransposedV = null;

    /**
     * Row and column dimension (square matrix).
     *
     * @serial matrix dimension.
     */
    private int n;

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

    public boolean isOrdered() {
        return !this.isHermitian();
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

    abstract boolean doDecompose(double[][] data, boolean valuesOnly);

    final void doDecomposeGeneral(final double[][] data, final boolean valuesOnly) {

        n = data.length;

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

    final void doDecomposeSymmetric(final double[][] data, final boolean valuesOnly) {

        n = data.length;

        if ((d == null) || (n != d.length)) {
            d = new double[n];
            e = new double[n];
        }
        myTransposedV = valuesOnly ? null : data;

        // Tridiagonalize.
        HouseholderHermitian.tred2jj(data, d, e, !valuesOnly);

        // Diagonalize.
        EvD2D.tql2(d, e, myTransposedV);
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
