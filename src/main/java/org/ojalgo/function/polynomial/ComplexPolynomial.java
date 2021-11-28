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

import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.structure.Access1D;

public final class ComplexPolynomial extends AbstractPolynomial<ComplexNumber> {

    public ComplexPolynomial(final int degree) {
        super(Array1D.COMPLEX.make(degree + 1));
    }

    ComplexPolynomial(final Array1D<ComplexNumber> coefficients) {
        super(coefficients);
    }

    public void estimate(final Access1D<?> x, final Access1D<?> y) {
        this.estimate(x, y, GenericStore.COMPLEX, QR.COMPLEX);
    }

    public ComplexNumber integrate(final ComplexNumber fromPoint, final ComplexNumber toPoint) {

        PolynomialFunction<ComplexNumber> tmpPrim = this.buildPrimitive();

        ComplexNumber tmpFromVal = tmpPrim.invoke(fromPoint);
        ComplexNumber tmpToVal = tmpPrim.invoke(toPoint);

        return tmpToVal.subtract(tmpFromVal);
    }

    public ComplexNumber invoke(final ComplexNumber arg) {

        int tmpPower = this.degree();

        ComplexNumber retVal = this.get(tmpPower);

        while (--tmpPower >= 0) {
            retVal = this.get(tmpPower).add(arg.multiply(retVal));
        }

        return retVal;
    }

    public void set(final Access1D<?> coefficients) {
        int tmpLimit = Math.min(this.size(), coefficients.size());
        for (int p = 0; p < tmpLimit; p++) {
            this.set(p, ComplexNumber.valueOf(coefficients.get(p)));
        }
    }

    @Override
    protected ComplexNumber getDerivativeFactor(final int power) {
        int tmpNextIndex = power + 1;
        return this.get(tmpNextIndex).multiply(tmpNextIndex);
    }

    @Override
    protected ComplexNumber getPrimitiveFactor(final int power) {
        if (power <= 0) {
            return ComplexNumber.ZERO;
        }
        return this.get(power - 1).divide(power);
    }

    @Override
    protected AbstractPolynomial<ComplexNumber> makeInstance(final int size) {
        return new ComplexPolynomial(Array1D.COMPLEX.make(size));
    }

}
