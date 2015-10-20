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

import org.ojalgo.array.Array1D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.ComplexAggregator;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

abstract class GeneralEvD<N extends Number> extends EigenvalueDecomposition<N> {

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
    static final class Primitive extends GeneralEvD<Double> {

        Primitive() {
            super(PrimitiveDenseStore.FACTORY);
        }

    }

    protected GeneralEvD(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory) {
        super(aFactory);
    }

    public final boolean equals(final MatrixStore<N> aStore, final NumberContext context) {
        return MatrixUtils.equals(aStore, this, context);
    }

    public final N getDeterminant() {

        final AggregatorFunction<ComplexNumber> tmpVisitor = ComplexAggregator.getSet().product();

        this.getEigenvalues().visitAll(tmpVisitor);

        return this.scalar().cast(tmpVisitor.getNumber());
    }

    public MatrixStore<N> getInverse() {
        throw new UnsupportedOperationException();
    }

    public MatrixStore<N> getInverse(final DecompositionStore<N> newPreallocated) {
        throw new UnsupportedOperationException();
    }

    public final ComplexNumber getTrace() {

        final AggregatorFunction<ComplexNumber> tmpVisitor = ComplexAggregator.getSet().sum();

        this.getEigenvalues().visitAll(tmpVisitor);

        return tmpVisitor.getNumber();
    }

    public final boolean isHermitian() {
        return false;
    }

    public final boolean isOrdered() {
        return true;
    }

    public final boolean isSolvable() {
        return this.isComputed() && this.isHermitian();
    }

    @Override
    protected boolean doNonsymmetric(final ElementsSupplier<N> aMtrx, final boolean eigenvaluesOnly) {

        final int tmpDiagDim = (int) aMtrx.countRows();

        final DecompositionStore<N> tmpMtrxA = this.copy(aMtrx.get());

        final DecompositionStore<N> tmpV = this.makeEye(tmpDiagDim, tmpDiagDim);

        final Array1D<ComplexNumber> tmpEigenvalues = tmpMtrxA.computeInPlaceSchur(tmpV, true);

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

        //            BasicLogger.logDebug("Eigenvalues: {}", tmpEigenvalues);
        //            BasicLogger.logDebug("D", tmpD);
        //            BasicLogger.logDebug("THIS", tmpMtrxA);

        tmpEigenvalues.sortDescending();

        return this.computed(true);
    }

    @Override
    protected boolean doSymmetric(final ElementsSupplier<N> aMtrx, final boolean eigenvaluesOnly) {
        return this.doNonsymmetric(aMtrx, eigenvaluesOnly);
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

        final N tmpZero = this.scalar().zero().getNumber();
        final BinaryFunction<N> tmpDivide = this.function().divide();

        for (int i = 0; i < tmpDim; i++) {
            if (tmpD.isZero(i, i)) {
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
