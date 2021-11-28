/*
 * Copyright 1997-2021 Optimatika
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

import org.ojalgo.array.Array1D;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.TypeUtils;

/**
 * BigPolynomial
 *
 * @author apete
 */
public final class BigPolynomial extends AbstractPolynomial<BigDecimal> {

    public BigPolynomial(final int degree) {
        super(Array1D.BIG.make(degree + 1));
    }

    BigPolynomial(final Array1D<BigDecimal> coefficients) {
        super(coefficients);
    }

    public void estimate(final Access1D<?> x, final Access1D<?> y) {

        RationalPolynomial delegate = new RationalPolynomial(this.degree());

        delegate.estimate(x, y);

        this.set(delegate);
    }

    public BigDecimal integrate(final BigDecimal fromPoint, final BigDecimal toPoint) {

        PolynomialFunction<BigDecimal> tmpPrim = this.buildPrimitive();

        BigDecimal tmpFromVal = tmpPrim.invoke(fromPoint);
        BigDecimal tmpToVal = tmpPrim.invoke(toPoint);

        return tmpToVal.subtract(tmpFromVal);
    }

    public BigDecimal invoke(final BigDecimal arg) {

        int tmpPower = this.degree();

        BigDecimal retVal = this.get(tmpPower);

        while (--tmpPower >= 0) {
            retVal = this.get(tmpPower).add(arg.multiply(retVal));
        }

        return retVal;
    }

    public void set(final Access1D<?> coefficients) {
        int tmpLimit = Math.min(this.size(), coefficients.size());
        for (int p = 0; p < tmpLimit; p++) {
            this.set(p, TypeUtils.toBigDecimal(coefficients.get(p)));
        }
    }

    @Override
    protected BigDecimal getDerivativeFactor(final int power) {
        int tmpNextIndex = power + 1;
        return this.get(tmpNextIndex).multiply(new BigDecimal(tmpNextIndex));
    }

    @Override
    protected BigDecimal getPrimitiveFactor(final int power) {
        if (power <= 0) {
            return BigMath.ZERO;
        }
        return this.get(power - 1).divide(new BigDecimal(power));
    }

    @Override
    protected AbstractPolynomial<BigDecimal> makeInstance(final int size) {
        return new BigPolynomial(Array1D.BIG.make(size));
    }

}
