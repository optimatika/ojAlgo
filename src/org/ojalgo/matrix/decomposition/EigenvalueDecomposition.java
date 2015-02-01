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
 * {@linkplain Eigenvalue#makeBig()}, {@linkplain Eigenvalue#makePrimitive()} or {@linkplain Eigenvalue#makeJama()}.
 *
 * @deprecated v38 This class will be made package private. Use the inteface instead.
 * @author apete
 */
@Deprecated
public abstract class EigenvalueDecomposition<N extends Number> extends GenericDecomposition<N> implements Eigenvalue<N> {

    /**
     * @deprecated v38 Use {@link Eigenvalue#make(Access2D<N>)} instead
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static final <N extends Number> Eigenvalue<N> make(final Access2D<N> template) {
        return Eigenvalue.make(template);
    }

    /**
     * @deprecated v38 Use {@link Eigenvalue#makeBig()} instead
     */
    @Deprecated
    public static final Eigenvalue<BigDecimal> makeBig() {
        return Eigenvalue.makeBig();
    }

    /**
     * @deprecated v38 Use {@link Eigenvalue#makeBig(boolean)} instead
     */
    @Deprecated
    public static final Eigenvalue<BigDecimal> makeBig(final boolean symmetric) {
        return Eigenvalue.makeBig(symmetric);
    }

    /**
     * @deprecated v38 Use {@link Eigenvalue#makeComplex()} instead
     */
    @Deprecated
    public static final Eigenvalue<ComplexNumber> makeComplex() {
        return Eigenvalue.makeComplex();
    }

    /**
     * @deprecated v38 Use {@link Eigenvalue#makeComplex(boolean)} instead
     */
    @Deprecated
    public static final Eigenvalue<ComplexNumber> makeComplex(final boolean hermitian) {
        return Eigenvalue.makeComplex(hermitian);
    }

    /**
     * @deprecated v38 Use {@link Eigenvalue#makeJama()} instead
     */
    @Deprecated
    public static final Eigenvalue<Double> makeJama() {
        return new RawEigenvalue.General();
    }

    /**
     * @deprecated v38 Use {@link Eigenvalue#makeJama(boolean)} instead
     */
    @Deprecated
    public static final Eigenvalue<Double> makeJama(final boolean symmetric) {
        return symmetric ? new RawEigenvalue.Symmetric() : new RawEigenvalue.Nonsymmetric();
    }

    /**
     * @deprecated v38 Use {@link Eigenvalue#makePrimitive()} instead
     */
    @Deprecated
    public static final Eigenvalue<Double> makePrimitive() {
        return Eigenvalue.makePrimitive();
    }

    /**
     * @deprecated v38 Use {@link Eigenvalue#makePrimitive(boolean)} instead
     */
    @Deprecated
    public static final Eigenvalue<Double> makePrimitive(final boolean symmetric) {
        return Eigenvalue.makePrimitive(symmetric);
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
