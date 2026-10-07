/*
 * Copyright 1997-2026 Optimatika
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

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.ArrayC128;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.ComplexAggregator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.structure.Access2D.Collectable;

abstract class GeneralEvD<N extends Comparable<N>> extends DenseEigenvalue<N> {

    /**
     * Eigenvalues and eigenvectors of a real matrix.
     * <P>
     * If A is symmetric, then A = V*D*V' where the eigenvalue matrix D is diagonal and the eigenvector matrix
     * V is orthogonal. I.e. A = V.times(D.times(V.transpose())) and V.times(V.transpose()) equals the
     * identity matrix.
     * <P>
     * If A is not symmetric, then the eigenvalue matrix D is block diagonal with the real eigenvalues in
     * 1-by-1 blocks and any complex eigenvalues, lambda + i*mu, in 2-by-2 blocks, [lambda, mu; -mu, lambda].
     * The columns of V represent the eigenvectors in the sense that A*V = V*D, i.e. A.times(V) equals
     * V.times(D). The matrix V may be badly conditioned, or even singular, so the validity of the equation A
     * = V*D*inverse(V) depends upon V.cond().
     **/
    static final class R064 extends GeneralEvD<Double> {

        R064() {
            super(R064Store.FACTORY);
        }

    }

    /**
     * Only implemented for {@link R064Store}.
     */
    private static Array1D<ComplexNumber> computeInPlaceSchur(final PhysicalStore<?> matrix, final PhysicalStore<?> transformationCollector) {

        R064Store mtrxA = (R064Store) matrix;
        R064Store mtrxV = (R064Store) transformationCollector;

        EvD1D.orthes(mtrxA.data, mtrxV.data, new double[mtrxA.getMinDim()]);

        double[][] diagonals = EvD1D.hqr2(mtrxA.data, mtrxV.data, true);
        double[] real = diagonals[0];
        double[] imaginary = diagonals[1];
        int length = Math.min(real.length, imaginary.length);

        ArrayC128 retVal = ArrayC128.make(length);

        for (int i = 0; i < length; i++) {
            retVal.data[i] = ComplexNumber.of(real[i], imaginary[i]);
        }

        return Array1D.C128.wrap(retVal);
    }

    protected GeneralEvD(final PhysicalStore.Factory<N, ? extends PhysicalStore<N>> factory) {
        super(factory);
    }

    @Override
    public boolean checkAndDecompose(final MatrixStore<N> matrix) {
        return this.decompose(matrix);
    }

    @Override
    public final N getDeterminant() {

        final AggregatorFunction<ComplexNumber> tmpVisitor = ComplexAggregator.getSet().product();

        this.getEigenvalues().visitAll(tmpVisitor);

        return this.scalar().cast(tmpVisitor.get());
    }

    public MatrixStore<N> getInverse() {
        ProgrammingError.throwForUnsupportedOptionalOperation();
        return null;
    }

    public MatrixStore<N> getInverse(final PhysicalStore<N> newPreallocated) {
        ProgrammingError.throwForUnsupportedOptionalOperation();
        return null;
    }

    @Override
    public final ComplexNumber getTrace() {

        final AggregatorFunction<ComplexNumber> tmpVisitor = ComplexAggregator.getSet().sum();

        this.getEigenvalues().visitAll(tmpVisitor);

        return tmpVisitor.get();
    }

    @Override
    public final boolean isHermitian() {
        return false;
    }

    @Override
    public boolean isOrdered() {
        return false;
    }

    @Override
    protected boolean checkSolvability() {
        return this.isComputed() && this.isHermitian();
    }

    @Override
    protected boolean doDecompose(final Collectable<N, ? super TransformableRegion<N>> matrix, final boolean valuesOnly) {

        final int tmpDiagDim = (int) matrix.countRows();

        // final PhysicalStore<N> tmpMtrxA = this.copy(matrix.get());
        final PhysicalStore<N> tmpMtrxA = this.makeZero(tmpDiagDim, tmpDiagDim);
        matrix.supplyTo(tmpMtrxA);

        final PhysicalStore<N> tmpV = this.makeEye(tmpDiagDim, tmpDiagDim);

        final Array1D<ComplexNumber> tmpEigenvalues = GeneralEvD.computeInPlaceSchur(tmpMtrxA, tmpV);

        this.setV(tmpV);
        this.setEigenvalues(tmpEigenvalues);

        final PhysicalStore<N> tmpD = this.makeZero(tmpDiagDim, tmpDiagDim);
        ComplexNumber tmpValue;
        double tmpImaginary;
        for (int ij = 0; ij < tmpDiagDim; ij++) {

            tmpValue = tmpEigenvalues.get(ij);
            tmpD.set(ij, ij, tmpValue.doubleValue());

            tmpImaginary = tmpValue.i;

            if (tmpImaginary > PrimitiveMath.ZERO) {
                tmpD.set(ij, ij + 1, tmpImaginary);
            } else if (tmpImaginary < PrimitiveMath.ZERO) {
                tmpD.set(ij, ij - 1, tmpImaginary);
            }
        }
        this.setD(tmpD);

        // BasicLogger.logDebug("Eigenvalues: {}", tmpEigenvalues);
        // BasicLogger.logDebug("D", tmpD);
        // BasicLogger.logDebug("THIS", tmpMtrxA);

        // tmpEigenvalues.sortDescending();

        return this.computed(true);
    }

    @Override
    protected final MatrixStore<N> makeD() {
        return null;
    }

    @Override
    protected Array1D<ComplexNumber> makeEigenvalues() {
        return null;
    }

    protected final MatrixStore<N> makeInverse() {

        final MatrixStore<N> tmpV = this.getV();
        final MatrixStore<N> tmpD = this.getD();

        final int tmpDim = (int) tmpD.countRows();

        final PhysicalStore<N> tmpMtrx = tmpV.transpose().copy();

        final N tmpZero = this.scalar().zero().get();
        final BinaryFunction<N> tmpDivide = this.function().divide();

        for (int i = 0; i < tmpDim; i++) {
            if (tmpD.isSmall(i, i, PrimitiveMath.ONE)) {
                tmpMtrx.fillRow(i, 0, tmpZero);
            } else {
                tmpMtrx.modifyRow(i, 0, tmpDivide.second(tmpD.get(i, i)));
            }
        }

        return tmpV.multiply(tmpMtrx);
    }

    @Override
    protected MatrixStore<N> makeV() {
        return null;
    }

}
