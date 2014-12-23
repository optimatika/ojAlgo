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

import java.math.BigDecimal;

import org.ojalgo.access.Access2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;

/**
 * You create instances of (some subclass of) this class by calling one of the static factory methods:
 * {@linkplain #makeBig()}, {@linkplain #makePrimitive()} or {@linkplain #makeJama()}.
 * 
 * @author apete
 */
public abstract class EigenvalueDecomposition<N extends Number> extends AbstractDecomposition<N> implements Eigenvalue<N> {

    @SuppressWarnings("unchecked")
    public static final <N extends Number> Eigenvalue<N> make(final Access2D<N> template) {

        final N tmpNumber = template.get(0L, 0L);
        final long tmpDim = template.countColumns();

        if (tmpNumber instanceof BigDecimal) {

            final boolean tmpSymmetric = MatrixUtils.isHermitian(template);

            return (Eigenvalue<N>) EigenvalueDecomposition.makeBig(tmpSymmetric);

        } else if (tmpNumber instanceof ComplexNumber) {

            final boolean tmpHermitian = MatrixUtils.isHermitian(template);

            return (Eigenvalue<N>) EigenvalueDecomposition.makeComplex(tmpHermitian);

        } else if (tmpNumber instanceof Double) {

            final boolean tmpSymmetric = MatrixUtils.isHermitian(template);

            if ((tmpDim > 128L) && (tmpDim < 46340L)) {

                return (Eigenvalue<N>) EigenvalueDecomposition.makePrimitive(tmpSymmetric);

            } else {

                return (Eigenvalue<N>) EigenvalueDecomposition.makeJama(tmpSymmetric);
            }

        } else {

            throw new IllegalArgumentException();
        }
    }

    public static final Eigenvalue<BigDecimal> makeBig() {
        return EigenvalueDecomposition.makeBig(true);
    }

    public static final Eigenvalue<BigDecimal> makeBig(final boolean symmetric) {
        return symmetric ? new HermitianEvD32.Big() : null;
    }

    public static final Eigenvalue<ComplexNumber> makeComplex() {
        return EigenvalueDecomposition.makeComplex(true);
    }

    public static final Eigenvalue<ComplexNumber> makeComplex(final boolean hermitian) {
        return hermitian ? new HermitianEvD32.Complex() : null;
    }

    public static final Eigenvalue<Double> makeJama() {
        return new RawEigenvalue.General();
    }

    public static final Eigenvalue<Double> makeJama(final boolean symmetric) {
        return symmetric ? new RawEigenvalue.Symmetric() : new RawEigenvalue.Nonsymmetric();
    }

    public static final Eigenvalue<Double> makePrimitive() {
        return new GeneralEvD.Primitive();
    }

    public static final Eigenvalue<Double> makePrimitive(final boolean symmetric) {
        return symmetric ? new HermitianEvD32.Primitive() : new NonsymmetricEvD.Primitive();
    }

    private MatrixStore<N> myD = null;
    private Array1D<ComplexNumber> myEigenvalues = null;
    private boolean myEigenvaluesOnly = false;
    private MatrixStore<N> myV = null;

    protected EigenvalueDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory) {
        super(aFactory);
    }

    public final N calculateDeterminant(final Access2D<N> matrix) {
        this.compute(matrix);
        return this.getDeterminant();
    }

    public final boolean compute(final Access2D<?> matrix) {
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

    public DecompositionStore<N> preallocate(final Access2D<N> templateBody, final Access2D<N> templateRHS) {
        return this.makeZero((int) templateBody.countColumns(), (int) templateRHS.countColumns());
    }

    public final MatrixStore<N> reconstruct() {
        return MatrixUtils.reconstruct(this);
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
