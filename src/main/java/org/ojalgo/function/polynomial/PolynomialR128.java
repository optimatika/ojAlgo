/*
 * Copyright 1997-2023 Optimatika
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
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.structure.Access1D;

public final class PolynomialR128 extends AbstractPolynomial<Quadruple> {

    public PolynomialR128(final int degree) {
        super(Array1D.R128.make(degree + 1));
    }

    PolynomialR128(final Array1D<Quadruple> coefficients) {
        super(coefficients);
    }

    public void estimate(final Access1D<?> x, final Access1D<?> y) {
        this.estimate(x, y, GenericStore.R128, QR.R128);
    }

    public Quadruple integrate(final Quadruple fromPoint, final Quadruple toPoint) {

        PolynomialFunction<Quadruple> primitive = this.buildPrimitive();

        Quadruple fromVal = primitive.invoke(fromPoint);
        Quadruple toVal = primitive.invoke(toPoint);

        return toVal.subtract(fromVal);
    }

    public Quadruple invoke(final Quadruple arg) {

        int power = this.degree();

        Quadruple retVal = this.get(power);

        while (--power >= 0) {
            retVal = this.get(power).add(arg.multiply(retVal));
        }

        return retVal;
    }

    public void set(final Access1D<?> coefficients) {
        int limit = Math.min(this.size(), coefficients.size());
        for (int p = 0; p < limit; p++) {
            this.set(p, Quadruple.valueOf(coefficients.get(p)));
        }
    }

    @Override
    protected Quadruple getDerivativeFactor(final int power) {
        int nextIndex = power + 1;
        return this.get(nextIndex).multiply(nextIndex);
    }

    @Override
    protected Quadruple getPrimitiveFactor(final int power) {
        if (power <= 0) {
            return Quadruple.ZERO;
        }
        return this.get(power - 1).divide(power);
    }

    @Override
    protected AbstractPolynomial<Quadruple> makeInstance(final int size) {
        return new PolynomialR128(Array1D.R128.make(size));
    }

}
