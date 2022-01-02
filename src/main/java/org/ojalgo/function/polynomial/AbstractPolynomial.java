/*
 * Copyright 1997-2022 Optimatika
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
package org.ojalgo.function.polynomial;

import java.math.BigDecimal;
import java.util.List;

import org.ojalgo.array.Array1D;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.series.NumberSeries;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.TypeUtils;

abstract class AbstractPolynomial<N extends Comparable<N>> implements PolynomialFunction<N> {

    private final Array1D<N> myCoefficients;

    private transient AbstractPolynomial<N> myDerivative = null;

    private transient AbstractPolynomial<N> myPrimitive = null;

    @SuppressWarnings("unused")
    private AbstractPolynomial() {
        this(null);
    }

    protected AbstractPolynomial(final Array1D<N> coefficients) {

        super();

        myCoefficients = coefficients;
    }

    public final PolynomialFunction<N> buildDerivative() {

        if (myDerivative == null) {

            final int tmpSize = Math.max(1, myCoefficients.size() - 1);

            myDerivative = this.makeInstance(tmpSize);

            for (int i = 0; i < tmpSize; i++) {
                myDerivative.set(i, this.getDerivativeFactor(i));
            }
        }

        return myDerivative;
    }

    public final PolynomialFunction<N> buildPrimitive() {

        if (myPrimitive == null) {

            final int tmpSize = myCoefficients.size() + 1;

            myPrimitive = this.makeInstance(tmpSize);

            for (int i = 0; i < tmpSize; i++) {
                myPrimitive.set(i, this.getPrimitiveFactor(i));
            }
        }

        return myPrimitive;
    }

    public long count() {
        return this.size();
    }

    public final int degree() {
        return myCoefficients.size() - 1;
    }

    public final double doubleValue(final long power) {
        return myCoefficients.doubleValue(power);
    }

    public final void estimate(final List<? extends N> x, final List<? extends N> y) {
        this.estimate(Access1D.wrap(x), Access1D.wrap(y));
    }

    void estimate(final Access1D<?> x, final Access1D<?> y, final PhysicalStore.Factory<N, ?> store, final QR.Factory<N> qr) {

        int tmpRowDim = Math.min(x.size(), y.size());
        int tmpColDim = this.size();

        PhysicalStore<N> tmpBody = store.make(tmpRowDim, tmpColDim);
        PhysicalStore<N> tmpRHS = store.make(tmpRowDim, 1);

        for (int i = 0; i < tmpRowDim; i++) {

            BigDecimal tmpX = BigMath.ONE;
            BigDecimal tmpXfactor = TypeUtils.toBigDecimal(x.get(i));
            BigDecimal tmpY = TypeUtils.toBigDecimal(y.get(i));

            for (int j = 0; j < tmpColDim; j++) {
                tmpBody.set(i, j, tmpX);
                tmpX = tmpX.multiply(tmpXfactor);
            }
            tmpRHS.set(i, 0, tmpY);
        }

        QR<N> tmpQR = qr.make(tmpBody);
        tmpQR.decompose(tmpBody);
        this.set(tmpQR.getSolution(tmpRHS));
    }

    public final void estimate(final NumberSeries<?> samples) {
        this.estimate(samples.accessKeys(), samples.accessValues());
    }

    public final N get(final long power) {
        return myCoefficients.get(power);
    }

    public final double invoke(final double arg) {

        int power = this.degree();

        double retVal = this.doubleValue(power);

        while (--power >= 0) {
            retVal = this.doubleValue(power) + arg * retVal;
        }

        return retVal;
    }

    public final float invoke(final float arg) {

        int power = this.degree();

        float retVal = this.floatValue(power);

        while (--power >= 0) {
            retVal = this.floatValue(power) + arg * retVal;
        }

        return retVal;
    }

    public final void set(final int power, final double coefficient) {
        myCoefficients.set(power, coefficient);
        myDerivative = null;
        myPrimitive = null;
    }

    public final void set(final int power, final N coefficient) {
        myCoefficients.set(power, coefficient);
        myDerivative = null;
        myPrimitive = null;
    }

    public final int size() {
        return myCoefficients.size();
    }

    protected abstract N getDerivativeFactor(int power);

    protected abstract N getPrimitiveFactor(int power);

    protected abstract AbstractPolynomial<N> makeInstance(int size);

}
