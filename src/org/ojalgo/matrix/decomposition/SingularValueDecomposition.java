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
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;

/**
 * You create instances of (some subclass of) this class by calling one of the static factory methods:
 * {@linkplain SingularValue#makeBig()}, {@linkplain SingularValue#makeComplex()},
 * {@linkplain SingularValue#makePrimitive()}, {@linkplain SingularValue#makeAlternative()} or
 * {@linkplain SingularValue#makeJama()}.
 *
 * @deprecated v38 This class will be made package private. Use the inteface instead.
 * @author apete
 */
@Deprecated
public abstract class SingularValueDecomposition<N extends Number & Comparable<N>> extends AbstractDecomposition<N> implements SingularValue<N> {

    /**
     * @deprecated v38 Use {@link SingularValue#make(Access2D<N>)} instead
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static final <N extends Number> SingularValue<N> make(final Access2D<N> aTypical) {
        return SingularValue.make(aTypical);
    }

    /**
     * @deprecated v38 Use {@link SingularValue#makeAlternative()} instead
     */
    @Deprecated
    public static final SingularValue<Double> makeAlternative() {
        return new SVDold30.Primitive();
    }

    /**
     * @deprecated v38 Use {@link SingularValue#makeBig()} instead
     */
    @Deprecated
    public static final SingularValue<BigDecimal> makeBig() {
        return SingularValue.makeBig();
    }

    /**
     * @deprecated v38 Use {@link SingularValue#makeComplex()} instead
     */
    @Deprecated
    public static final SingularValue<ComplexNumber> makeComplex() {
        return SingularValue.makeComplex();
    }

    /**
     * @deprecated v38 Use {@link SingularValue#makeJama()} instead
     */
    @Deprecated
    public static final SingularValue<Double> makeJama() {
        return new RawSingularValue();
    }

    /**
     * @deprecated v38 Use {@link SingularValue#makePrimitive()} instead
     */
    @Deprecated
    public static final SingularValue<Double> makePrimitive() {
        return SingularValue.makePrimitive();
    }

    private final BidiagonalDecomposition<N> myBidiagonal;
    private transient MatrixStore<N> myD;
    private transient MatrixStore<N> myQ1;
    private transient MatrixStore<N> myQ2;
    private transient Array1D<Double> mySingularValues;
    private boolean mySingularValuesOnly = false;
    private boolean myTransposed = false;
    private transient MatrixStore<N> myInverse;
    private boolean myFullSize = false;

    @SuppressWarnings("unused")
    private SingularValueDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory) {
        this(aFactory, null);
    }

    protected SingularValueDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory,
            final BidiagonalDecomposition<N> aBidiagonal) {

        super(aFactory);

        myBidiagonal = aBidiagonal;
    }

    public final boolean compute(final Access2D<?> matrix) {
        return this.compute(matrix, false, false);
    }

    public boolean compute(final Access2D<?> matrix, final boolean singularValuesOnly, final boolean fullSize) {

        this.reset();

        if (matrix.countRows() >= matrix.countColumns()) {
            myTransposed = false;
        } else {
            myTransposed = true;
        }

        mySingularValuesOnly = singularValuesOnly;
        myFullSize = fullSize;

        boolean retVal = false;

        try {

            retVal = this.doCompute(myTransposed ? this.wrap(matrix).builder().conjugate().build() : matrix, singularValuesOnly, fullSize);

        } catch (final Exception anException) {

            BasicLogger.error(anException.toString());

            this.reset();

            retVal = false;
        }

        return this.computed(retVal);
    }

    public final double getCondition() {

        final Array1D<Double> tmpSingularValues = this.getSingularValues();

        return tmpSingularValues.doubleValue(0) / tmpSingularValues.doubleValue(tmpSingularValues.length - 1);
    }

    public final MatrixStore<N> getD() {

        if ((myD == null) && this.isComputed()) {
            myD = this.makeD();
        }

        return myD;
    }

    public final double getFrobeniusNorm() {

        double retVal = PrimitiveMath.ZERO;

        final Array1D<Double> tmpSingularValues = this.getSingularValues();
        double tmpVal;

        for (int i = tmpSingularValues.size() - 1; i >= 0; i--) {
            tmpVal = tmpSingularValues.doubleValue(i);
            retVal += tmpVal * tmpVal;
        }

        return Math.sqrt(retVal);
    }

    public final MatrixStore<N> getInverse() {

        if (myInverse == null) {

            final MatrixStore<N> tmpQ1 = this.getQ1();
            final MatrixStore<N> tmpD = this.getD();

            final int tmpRowDim = (int) tmpD.countRows();
            final int tmpColDim = (int) tmpQ1.countRows();

            final PhysicalStore<N> tmpMtrx = this.makeZero(tmpRowDim, tmpColDim);

            //final N tmpZero = this.getStaticZero();

            double tmpSingularValue;
            for (int i = 0; i < tmpRowDim; i++) {
                if (tmpD.isZero(i, i)) {
                    //tmpMtrx.fillRow(i, 0, tmpZero);
                } else {
                    tmpSingularValue = tmpD.doubleValue(i, i);
                    for (int j = 0; j < tmpColDim; j++) {
                        tmpMtrx.set(i, j, tmpQ1.toScalar(j, i).conjugate().divide(tmpSingularValue).getNumber());
                    }
                }
            }

            myInverse = tmpMtrx.multiplyLeft(this.getQ2());
        }

        return myInverse;
    }

    public final MatrixStore<N> getInverse(final DecompositionStore<N> preallocated) {

        if (myInverse == null) {

            final MatrixStore<N> tmpQ1 = this.getQ1();
            final MatrixStore<N> tmpD = this.getD();

            final int tmpRowDim = (int) tmpD.countRows();
            final int tmpColDim = (int) tmpQ1.countRows();

            final PhysicalStore<N> tmpMtrx = preallocated;

            final N tmpZero = this.getStaticZero();

            N tmpSingularValue;
            for (int i = 0; i < tmpRowDim; i++) {
                if (tmpD.isZero(i, i)) {
                    tmpMtrx.fillRow(i, 0, tmpZero);
                } else {
                    tmpSingularValue = tmpD.get(i, i);
                    for (int j = 0; j < tmpColDim; j++) {
                        tmpMtrx.set(i, j, tmpQ1.toScalar(j, i).divide(tmpSingularValue).getNumber());
                    }
                }
            }

            myInverse = tmpMtrx.multiplyLeft(this.getQ2());
        }

        return myInverse;
    }

    public final double getKyFanNorm(final int k) {

        final Array1D<Double> tmpSingularValues = this.getSingularValues();

        double retVal = PrimitiveMath.ZERO;

        for (int i = Math.min(tmpSingularValues.size(), k) - 1; i >= 0; i--) {
            retVal += tmpSingularValues.doubleValue(i);
        }

        return retVal;
    }

    public final double getOperatorNorm() {
        return this.getSingularValues().doubleValue(0);
    }

    public final MatrixStore<N> getQ1() {

        if ((myQ1 == null) && !mySingularValuesOnly && this.isComputed()) {
            if (myTransposed) {
                myQ1 = this.makeQ2();
            } else {
                myQ1 = this.makeQ1();
            }
        }

        return myQ1;
    }

    public final MatrixStore<N> getQ2() {

        if ((myQ2 == null) && !mySingularValuesOnly && this.isComputed()) {
            if (myTransposed) {
                myQ2 = this.makeQ1();
            } else {
                myQ2 = this.makeQ2();
            }
        }

        return myQ2;
    }

    public final int getRank() {

        final Array1D<Double> tmpSingularValues = this.getSingularValues();
        int retVal = tmpSingularValues.size();

        // Tolerance based on min-dim but should be max-dim
        final double tmpTolerance = retVal * tmpSingularValues.doubleValue(0) * PrimitiveMath.MACHINE_EPSILON;

        for (int i = retVal - 1; i >= 0; i--) {
            if (tmpSingularValues.doubleValue(i) <= tmpTolerance) {
                retVal--;
            } else {
                return retVal;
            }
        }

        return retVal;
    }

    public final Array1D<Double> getSingularValues() {

        if ((mySingularValues == null) && this.isComputed()) {
            mySingularValues = this.makeSingularValues();
        }

        return mySingularValues;
    }

    public final double getTraceNorm() {
        return this.getKyFanNorm(this.getSingularValues().size());
    }

    @Override
    public final boolean isAspectRatioNormal() {
        return super.aspectRatioNormal(myBidiagonal.isAspectRatioNormal());
    }

    public final boolean isFullSize() {
        return myFullSize;
    }

    public DecompositionStore<N> preallocate(final Access2D<N> templateBody, final Access2D<N> templateRHS) {
        return this.makeZero((int) templateBody.countColumns(), (int) templateRHS.countColumns());
    }

    public MatrixStore<N> reconstruct() {
        return MatrixUtils.reconstruct(this);
    }

    @Override
    public void reset() {

        super.reset();

        myBidiagonal.reset();

        myD = null;
        myQ1 = null;
        myQ2 = null;

        myInverse = null;

        mySingularValuesOnly = false;
        myTransposed = false;
        myFullSize = false;
    }

    public MatrixStore<N> solve(final Access2D<N> rhs) {
        return this.getInverse().multiply(rhs);
    }

    public MatrixStore<N> solve(final Access2D<N> rhs, final DecompositionStore<N> preallocated) {
        preallocated.fillByMultiplying(this.getInverse(), rhs);
        return preallocated;
    }

    protected final boolean computeBidiagonal(final Access2D<?> aStore, final boolean fullSize) {
        return myBidiagonal.compute(aStore, fullSize);
    }

    protected abstract boolean doCompute(Access2D<?> aMtrx, boolean singularValuesOnly, boolean fullSize);

    protected final DiagonalAccess<N> getBidiagonalAccessD() {
        return myBidiagonal.getDiagonalAccessD();
    }

    protected final DecompositionStore<N> getBidiagonalQ1() {
        return (DecompositionStore<N>) myBidiagonal.getQ1();
    }

    protected final DecompositionStore<N> getBidiagonalQ2() {
        return (DecompositionStore<N>) myBidiagonal.getQ2();
    }

    protected final boolean isTransposed() {
        return myTransposed;
    }

    protected abstract MatrixStore<N> makeD();

    protected abstract MatrixStore<N> makeQ1();

    protected abstract MatrixStore<N> makeQ2();

    protected abstract Array1D<Double> makeSingularValues();

    final void setD(final MatrixStore<N> someD) {
        myD = someD;
    }

    final void setQ1(final MatrixStore<N> someQ1) {
        myQ1 = someQ1;
    }

    final void setQ2(final MatrixStore<N> someQ2) {
        myQ2 = someQ2;
    }

    final void setSingularValues(final Array1D<Double> someSingularValues) {
        mySingularValues = someSingularValues;
    }

}
