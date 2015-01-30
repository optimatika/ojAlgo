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

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * This class adapts JAMA's SingularValueDecomposition to ojAlgo's {@linkplain SingularValue} interface. speed: 52.641s
 *
 * @deprecated v38 This class will be made package private. Use the inteface instead.
 * @author apete
 */
@Deprecated
public final class RawSingularValue extends RawDecomposition implements SingularValue<Double> {

    private JamaSingularValue myDelegate;
    private RawStore myPseudoinverse;
    private boolean myTransposed;

    /**
     * Not recommended to use this constructor directly. Consider using the static factory method
     * {@linkplain org.ojalgo.matrix.decomposition.SingularValue#makeJama()} instead.
     */
    public RawSingularValue() {
        super();
    }

    public boolean compute(final Access2D<?> matrix, final boolean singularValuesOnly, final boolean fullSize) {

        this.reset();

        final RawStore tmpCast = RawDecomposition.cast(matrix);

        return this.compute(tmpCast, singularValuesOnly);
    }

    public boolean equals(final MatrixStore<Double> aStore, final NumberContext context) {
        return MatrixUtils.equals(aStore, this, context);
    }

    public double getCondition() {
        return myDelegate.cond();
    }

    public RawStore getD() {
        return new RawStore(myDelegate.getS());
    }

    public double getFrobeniusNorm() {

        double retVal = PrimitiveMath.ZERO;
        double tmpVal;

        final Array1D<Double> tmpSingularValues = this.getSingularValues();

        for (int i = 0; i < tmpSingularValues.size(); i++) {
            tmpVal = tmpSingularValues.doubleValue(i);
            retVal += tmpVal * tmpVal;
        }

        return Math.sqrt(retVal);
    }

    @Override
    public RawStore getInverse() {

        if (myPseudoinverse == null) {

            final double[][] tmpQ1 = this.getQ1().data;
            final double[] tmpSingular = myDelegate.getSingularValues();

            final RawStore tmpMtrx = new RawStore(tmpSingular.length, tmpQ1.length);

            for (int i = 0; i < tmpSingular.length; i++) {
                if (TypeUtils.isZero(tmpSingular[i])) {
                    for (int j = 0; j < tmpQ1.length; j++) {
                        tmpMtrx.set(i, j, PrimitiveMath.ZERO);
                    }
                } else {
                    for (int j = 0; j < tmpQ1.length; j++) {
                        tmpMtrx.set(i, j, tmpQ1[j][i] / tmpSingular[i]);
                    }
                }
            }

            myPseudoinverse = new RawStore(this.getQ2().multiply(tmpMtrx));
        }

        return myPseudoinverse;
    }

    public double getKyFanNorm(final int k) {

        double retVal = PrimitiveMath.ZERO;

        final Array1D<Double> tmpSingularValues = this.getSingularValues();
        final int tmpK = Math.min(tmpSingularValues.size(), k);

        for (int i = 0; i < tmpK; i++) {
            retVal += tmpSingularValues.doubleValue(i);
        }

        return retVal;
    }

    public double getOperatorNorm() {
        return this.getSingularValues().get(0);
    }

    public RawStore getQ1() {
        return new RawStore(myTransposed ? myDelegate.getV() : myDelegate.getU());
    }

    public RawStore getQ2() {
        return new RawStore(myTransposed ? myDelegate.getU() : myDelegate.getV());
    }

    public int getRank() {
        return myDelegate.rank();
    }

    public Array1D<Double> getSingularValues() {
        return Array1D.PRIMITIVE.copy(myDelegate.getSingularValues());
    }

    public double getTraceNorm() {
        return this.getKyFanNorm(myDelegate.getSingularValues().length);
    }

    public boolean isAspectRatioNormal() {
        return myTransposed;
    }

    public boolean isComputed() {
        return myDelegate != null;
    }

    public boolean isFullSize() {
        return false;
    }

    public boolean isOrdered() {
        return true;
    }

    public boolean isSolvable() {
        return this.isComputed();
    }

    public MatrixStore<Double> reconstruct() {
        return MatrixUtils.reconstruct(this);
    }

    public void reset() {

        myDelegate = null;

        myPseudoinverse = null;
    }

    /**
     * Internally this implementation uses the pseudoinverse that is recreated with every call.
     */
    @Override
    public RawStore solve(final Access2D<Double> rhs) {
        return this.getInverse().multiply(rhs);
    }

    @Override
    public String toString() {
        return myDelegate.toString();
    }

    @Override
    boolean compute(final RawStore aDelegate) {
        return this.compute(aDelegate, false);
    }

    boolean compute(final RawStore aDelegate, final boolean singularValuesOnly) {

        RawStore tmpMtrx;

        if ((int) aDelegate.countColumns() <= (int) aDelegate.countRows()) {
            myTransposed = false;
            tmpMtrx = aDelegate;
        } else {
            myTransposed = true;
            tmpMtrx = aDelegate.transpose();
        }

        myDelegate = new JamaSingularValue(tmpMtrx, !singularValuesOnly, !singularValuesOnly);

        return this.isComputed();
    }

    @Override
    RawStore solve(final RawStore aRHS) {
        ProgrammingError.throwForIllegalInvocation();
        return null;
    }

}
