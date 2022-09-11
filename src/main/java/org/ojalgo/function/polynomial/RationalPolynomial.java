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

import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access1D;

public final class RationalPolynomial extends AbstractPolynomial<RationalNumber> {

    public RationalPolynomial(final int degree) {
        super(Array1D.Q128.make(degree + 1));
    }

    RationalPolynomial(final Array1D<RationalNumber> coefficients) {
        super(coefficients);
    }

    public void estimate(final Access1D<?> x, final Access1D<?> y) {
        this.estimate(x, y, GenericStore.RATIONAL, QR.RATIONAL);
    }

    public RationalNumber integrate(final RationalNumber fromPoint, final RationalNumber toPoint) {

        PolynomialFunction<RationalNumber> tmpPrim = this.buildPrimitive();

        RationalNumber tmpFromVal = tmpPrim.invoke(fromPoint);
        RationalNumber tmpToVal = tmpPrim.invoke(toPoint);

        return tmpToVal.subtract(tmpFromVal);
    }

    public RationalNumber invoke(final RationalNumber arg) {

        int tmpPower = this.degree();

        RationalNumber retVal = this.get(tmpPower);

        while (--tmpPower >= 0) {
            retVal = this.get(tmpPower).add(arg.multiply(retVal));
        }

        return retVal;
    }

    public void set(final Access1D<?> coefficients) {
        int tmpLimit = Math.min(this.size(), coefficients.size());
        for (int p = 0; p < tmpLimit; p++) {
            this.set(p, RationalNumber.valueOf(coefficients.get(p)));
        }
    }

    @Override
    protected RationalNumber getDerivativeFactor(final int power) {
        int tmpNextIndex = power + 1;
        return this.get(tmpNextIndex).multiply(tmpNextIndex);
    }

    @Override
    protected RationalNumber getPrimitiveFactor(final int power) {
        if (power <= 0) {
            return RationalNumber.ZERO;
        }
        return this.get(power - 1).divide(power);
    }

    @Override
    protected AbstractPolynomial<RationalNumber> makeInstance(final int size) {
        return new RationalPolynomial(Array1D.Q128.make(size));
    }

}
