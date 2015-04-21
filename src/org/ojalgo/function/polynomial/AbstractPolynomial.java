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
package org.ojalgo.function.polynomial;

import java.util.List;

import org.ojalgo.array.Array1D;
import org.ojalgo.array.ArrayUtils;
import org.ojalgo.series.NumberSeries;

abstract class AbstractPolynomial<N extends Number> implements PolynomialFunction<N> {

    private final Array1D<N> myCoefficients;

    private transient AbstractPolynomial<N> myDerivative = null;

    private transient AbstractPolynomial<N> myPrimitive = null;

    @SuppressWarnings("unused")
    private AbstractPolynomial() {
        this(null);
    }

    protected AbstractPolynomial(final Array1D<N> someCoefficients) {

        super();

        myCoefficients = someCoefficients;
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

    public final double doubleValue(final long aPower) {
        return myCoefficients.doubleValue(aPower);
    }

    public final void estimate(final List<? extends Number> x, final List<? extends Number> y) {
        this.estimate(ArrayUtils.wrapAccess1D(x), ArrayUtils.wrapAccess1D(y));
    }

    public final void estimate(final NumberSeries<?> samples) {
        this.estimate(samples.accessKeys(), samples.accessValues());
    }

    public final N get(final long aPower) {
        return myCoefficients.get(aPower);
    }

    public final double invoke(final double arg) {

        int tmpPower = this.degree();

        double retVal = this.doubleValue(tmpPower);

        while (--tmpPower >= 0) {
            retVal = this.doubleValue(tmpPower) + (arg * retVal);
        }

        return retVal;
    }

    public final void set(final int aPower, final double aNmbr) {
        myCoefficients.set(aPower, aNmbr);
        myDerivative = null;
        myPrimitive = null;
    }

    public final void set(final int aPower, final N aNmbr) {
        myCoefficients.set(aPower, aNmbr);
        myDerivative = null;
        myPrimitive = null;
    }

    public final int size() {
        return myCoefficients.size();
    }

    protected abstract N getDerivativeFactor(int aPower);

    protected abstract N getPrimitiveFactor(int aPower);

    protected abstract AbstractPolynomial<N> makeInstance(int aSize);

}
