/*
 * Copyright 1997-2025 Optimatika
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

import org.ojalgo.array.ArrayR032;
import org.ojalgo.array.BasicArray;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.store.R032Store;
import org.ojalgo.structure.Access1D;

public final class PolynomialR032 extends AbstractPolynomial<Double, PolynomialR032> {

    public static final PolynomialR032 ONE = PolynomialR032.wrap(1F);

    public static PolynomialR032 wrap(final float... coefficients) {
        return new PolynomialR032(ArrayR032.wrap(coefficients));
    }

    public PolynomialR032(final int degree) {
        super(ArrayR032.make(degree + 1));
    }

    PolynomialR032(final BasicArray<Double> coefficients) {
        super(coefficients);
    }

    @Override
    public PolynomialR032 add(final PolynomialFunction<Double> addend) {

        int leftDeg = this.degree();
        int righDeg = addend.degree();

        int retSize = 1 + Math.max(leftDeg, righDeg);

        PolynomialR032 retVal = this.newInstance(retSize);
        BasicArray<Double> coefficients = retVal.coefficients();

        for (int l = 0; l <= leftDeg; l++) {
            coefficients.add(l, this.floatValue(l));
        }

        for (int r = 0; r <= righDeg; r++) {
            coefficients.add(r, addend.floatValue(r));
        }

        return retVal;
    }

    @Override
    public void estimate(final Access1D<?> x, final Access1D<?> y) {
        this.estimate(x, y, R032Store.FACTORY, QR.R064);
    }

    @Override
    public Double integrate(final Double fromPoint, final Double toPoint) {

        PolynomialFunction<Double> primitive = this.buildPrimitive();

        double fromVal = primitive.invoke(fromPoint.doubleValue());
        double toVal = primitive.invoke(toPoint.doubleValue());

        return Double.valueOf(toVal - fromVal);
    }

    @Override
    public Double invoke(final Double arg) {
        return Double.valueOf(this.invoke(arg.doubleValue()));
    }

    @Override
    public PolynomialR032 multiply(final PolynomialFunction<Double> multiplicand) {

        int leftDeg = this.degree();
        int righDeg = multiplicand.degree();

        int retSize = 1 + leftDeg + righDeg;

        PolynomialR032 retVal = this.newInstance(retSize);
        BasicArray<Double> coefficients = retVal.coefficients();

        for (int l = 0; l <= leftDeg; l++) {

            float left = this.floatValue(l);

            for (int r = 0; r <= righDeg; r++) {

                float right = multiplicand.floatValue(r);

                coefficients.add(l + r, left * right);
            }
        }

        return retVal;
    }

    @Override
    public PolynomialR032 negate() {

        int size = 1 + this.degree();

        PolynomialR032 retVal = this.newInstance(size);

        for (int p = 0; p < size; p++) {
            retVal.set(p, -this.floatValue(p));
        }

        return retVal;
    }

    @Override
    protected Double getDerivativeFactor(final int power) {
        int nextIndex = power + 1;
        return Double.valueOf(nextIndex * this.doubleValue(nextIndex));
    }

    @Override
    protected Double getPrimitiveFactor(final int power) {
        if (power <= 0) {
            return Double.valueOf(PrimitiveMath.ZERO);
        }
        return Double.valueOf(this.doubleValue(power - 1) / power);
    }

    @Override
    protected PolynomialR032 newInstance(final int size) {
        return new PolynomialR032(ArrayR032.make(size));
    }

    @Override
    PolynomialR032 one() {
        return ONE;
    }

}
