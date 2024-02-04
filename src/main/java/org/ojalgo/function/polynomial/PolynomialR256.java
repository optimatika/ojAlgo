/*
 * Copyright 1997-2024 Optimatika
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

import org.ojalgo.array.ArrayR256;
import org.ojalgo.array.BasicArray;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.structure.Access1D;

/**
 * BigPolynomial
 *
 * @author apete
 */
public class PolynomialR256 extends AbstractPolynomial<BigDecimal, PolynomialR256> {

    public static final PolynomialR256 ONE = PolynomialR256.wrap(BigDecimal.ONE);

    public static PolynomialR256 wrap(final BigDecimal... coefficients) {
        return new PolynomialR256(ArrayR256.wrap(coefficients));
    }

    public PolynomialR256(final int degree) {
        super(ArrayR256.make(degree + 1));
    }

    PolynomialR256(final BasicArray<BigDecimal> coefficients) {
        super(coefficients);
    }

    @Override
    public void estimate(final Access1D<?> x, final Access1D<?> y) {

        PolynomialQ128 delegate = new PolynomialQ128(this.degree());

        delegate.estimate(x, y);

        this.set(delegate);
    }

    @Override
    public BigDecimal integrate(final BigDecimal fromPoint, final BigDecimal toPoint) {

        PolynomialFunction<BigDecimal> primitive = this.buildPrimitive();

        BigDecimal fromVal = primitive.invoke(fromPoint);
        BigDecimal toVal = primitive.invoke(toPoint);

        return toVal.subtract(fromVal);
    }

    @Override
    public BigDecimal invoke(final BigDecimal arg) {

        int power = this.degree();

        BigDecimal retVal = this.get(power);

        while (--power >= 0) {
            retVal = this.get(power).add(arg.multiply(retVal));
        }

        return retVal;
    }

    @Override
    public PolynomialR256 multiply(final PolynomialFunction<BigDecimal> multiplicand) {

        int leftDeg = this.degree();
        int righDeg = multiplicand.degree();

        int retSize = 1 + leftDeg + righDeg;

        PolynomialR256 retVal = this.newInstance(retSize);
        BasicArray<BigDecimal> coefficients = retVal.coefficients();

        for (int l = 0; l <= leftDeg; l++) {

            BigDecimal left = this.get(l);

            for (int r = 0; r <= righDeg; r++) {

                BigDecimal right = multiplicand.get(r);

                coefficients.add(l + r, left.multiply(right));
            }
        }

        return retVal;
    }

    @Override
    public PolynomialR256 negate() {

        int size = 1 + this.degree();

        PolynomialR256 retVal = this.newInstance(size);

        for (int p = 0; p < size; p++) {
            retVal.set(p, this.get(p).negate());
        }

        return retVal;
    }

    @Override
    protected BigDecimal getDerivativeFactor(final int power) {
        int nextIndex = power + 1;
        return this.get(nextIndex).multiply(new BigDecimal(nextIndex));
    }

    @Override
    protected BigDecimal getPrimitiveFactor(final int power) {
        if (power <= 0) {
            return BigMath.ZERO;
        }
        return this.get(power - 1).divide(new BigDecimal(power));
    }

    @Override
    protected PolynomialR256 newInstance(final int size) {
        return new PolynomialR256(ArrayR256.make(size));
    }

    @Override
    PolynomialR256 one() {
        return ONE;
    }

}
