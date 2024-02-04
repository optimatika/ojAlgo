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

import org.ojalgo.array.BasicArray;
import org.ojalgo.scalar.Scalar;

abstract class ScalarPolynomial<N extends Scalar<N>, P extends ScalarPolynomial<N, P>> extends AbstractPolynomial<N, P> {

    ScalarPolynomial(final BasicArray<N> coefficients) {
        super(coefficients);
    }

    @Override
    public final N integrate(final N fromPoint, final N toPoint) {

        PolynomialFunction<N> primitive = this.buildPrimitive();

        N fromVal = primitive.invoke(fromPoint);
        N toVal = primitive.invoke(toPoint);

        return toVal.subtract(fromVal).get();
    }

    @Override
    public final N invoke(final N arg) {

        int power = this.degree();

        Scalar<N> retVal = this.get(power);

        while (--power >= 0) {
            retVal = this.get(power).add(arg.multiply(retVal));
        }

        return retVal.get();
    }

    @Override
    public P multiply(final PolynomialFunction<N> multiplicand) {

        int leftDeg = this.degree();
        int righDeg = multiplicand.degree();

        int retSize = 1 + leftDeg + righDeg;

        P retVal = this.newInstance(retSize);
        BasicArray<N> coefficients = retVal.coefficients();

        for (int l = 0; l <= leftDeg; l++) {

            N left = this.get(l);

            for (int r = 0; r <= righDeg; r++) {

                N right = multiplicand.get(r);

                coefficients.add(l + r, left.multiply(right));
            }
        }

        return retVal;
    }

    @Override
    public PolynomialFunction<N> negate() {

        int size = 1 + this.degree();

        P retVal = this.newInstance(size);

        for (int p = 0; p < size; p++) {
            retVal.set(p, this.get(p).negate());
        }

        return retVal;
    }

    @Override
    double norm(final int power) {
        return this.get(power).norm();
    }

}
