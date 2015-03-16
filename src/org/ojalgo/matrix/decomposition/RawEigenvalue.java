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

import org.ojalgo.access.Access2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.ComplexAggregator;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * This class adapts JAMA's EigenvalueDecomposition to ojAlgo's {@linkplain Eigenvalue} interface.
 *
 * @deprecated v38 This class will be made package private. Use the inteface instead.
 * @author apete
 */
@Deprecated
public abstract class RawEigenvalue extends RawDecomposition implements Eigenvalue<Double> {

    public static final class General extends RawEigenvalue {

        public General() {
            super();
        }

        @Override
        protected boolean compute(final RawStore matrix) {

            this.setDelegate(new JamaEigenvalue(matrix));

            this.computed(true);

            return true;
        }
    }

    public static final class Nonsymmetric extends RawEigenvalue {

        public Nonsymmetric() {
            super();
        }

        @Override
        protected boolean compute(final RawStore matrix) {

            this.setDelegate(new JamaEigenvalue(matrix, false));

            this.computed(true);

            return true;
        }
    }

    public static final class Symmetric extends RawEigenvalue {

        public Symmetric() {
            super();
        }

        @Override
        protected boolean compute(final RawStore matrix) {

            this.setDelegate(new JamaEigenvalue(matrix, true));

            this.computed(true);

            return true;
        }
    }

    private JamaEigenvalue myDelegate;

    private RawStore myInverse;

    /**
     * Not recommended to use this constructor directly. Consider using the static factory method
     * {@linkplain org.ojalgo.matrix.decomposition.Eigenvalue#makeJama()} instead.
     */

    protected RawEigenvalue() {
        super();
    }

    public Double calculateDeterminant(final Access2D<Double> matrix) {
        this.compute(matrix);
        return this.getDeterminant();
    }

    public boolean compute(final Access2D<?> matrix, final boolean eigenvaluesOnly) {
        return this.compute(matrix);
    }

    public boolean equals(final MatrixStore<Double> aStore, final NumberContext context) {
        return MatrixUtils.equals(aStore, this, context);
    }

    public RawStore getD() {
        return new RawStore(myDelegate.getD());
    }

    public Double getDeterminant() {

        final AggregatorFunction<ComplexNumber> tmpVisitor = ComplexAggregator.getSet().product();

        this.getEigenvalues().visitAll(tmpVisitor);

        return tmpVisitor.getNumber().doubleValue();
    }

    public Array1D<ComplexNumber> getEigenvalues() {

        final double[] tmpRe = myDelegate.getRealEigenvalues();
        final double[] tmpIm = myDelegate.getImagEigenvalues();

        final Array1D<ComplexNumber> retVal = Array1D.COMPLEX.makeZero(tmpRe.length);

        for (int i = 0; i < retVal.size(); i++) {
            retVal.set(i, new ComplexNumber(tmpRe[i], tmpIm[i]));
        }

        retVal.sortDescending();

        return retVal;
    }

    @Override
    public RawStore getInverse() {

        if (myInverse == null) {

            final double[][] tmpQ1 = this.getV().data;
            final double[] tmpEigen = myDelegate.getRealEigenvalues();

            final RawStore tmpMtrx = new RawStore(tmpEigen.length, tmpQ1.length);

            for (int i = 0; i < tmpEigen.length; i++) {
                if (TypeUtils.isZero(tmpEigen[i])) {
                    for (int j = 0; j < tmpQ1.length; j++) {
                        tmpMtrx.set(i, j, PrimitiveMath.ZERO);
                    }
                } else {
                    for (int j = 0; j < tmpQ1.length; j++) {
                        tmpMtrx.set(i, j, tmpQ1[j][i] / tmpEigen[i]);
                    }
                }
            }

            myInverse = new RawStore(this.getV().multiply(tmpMtrx));
        }

        return myInverse;
    }

    public ComplexNumber getTrace() {

        final AggregatorFunction<ComplexNumber> tmpVisitor = ComplexAggregator.getSet().sum();

        this.getEigenvalues().visitAll(tmpVisitor);

        return tmpVisitor.getNumber();
    }

    public RawStore getV() {
        return new RawStore(myDelegate.getV());
    }

    public boolean isFullSize() {
        return true;
    }

    public boolean isHermitian() {
        return myDelegate.isSymmetric();
    }

    public boolean isOrdered() {
        return !this.isHermitian();
    }

    public boolean isSolvable() {
        return (myDelegate != null) && myDelegate.isSymmetric();
    }

    public MatrixStore<Double> reconstruct() {
        return MatrixUtils.reconstruct(this);
    }

    @Override
    public void reset() {
        myDelegate = null;
        myInverse = null;
    }

    @Override
    public RawStore solve(final Access2D<Double> rhs) {
        return this.getInverse().multiply(rhs);
    }

    @Override
    public String toString() {
        return myDelegate.toString();
    }

    final void setDelegate(final JamaEigenvalue newDelegate) {
        myDelegate = newDelegate;
    }

    @Override
    RawStore solve(final RawStore rhs) {
        // TODO Auto-generated method stub
        return null;
    }

    public final boolean compute(final Access2D<?> matrix) {

        this.reset();

        return this.compute(RawDecomposition.cast(matrix));
    }

}
