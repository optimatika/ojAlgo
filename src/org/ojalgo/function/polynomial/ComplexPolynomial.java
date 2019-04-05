/*
 * Copyright 1997-2019 Optimatika
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
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.structure.Access1D;

public class ComplexPolynomial extends AbstractPolynomial<ComplexNumber> {

    public ComplexPolynomial(final int degree) {
        super(Array1D.COMPLEX.makeZero(degree + 1));
    }

    ComplexPolynomial(final Array1D<ComplexNumber> coefficients) {
        super(coefficients);
    }

    public void estimate(final Access1D<?> x, final Access1D<?> y) {

        final int tmpRowDim = (int) Math.min(x.count(), y.count());
        final int tmpColDim = this.size();

        final PhysicalStore<ComplexNumber> tmpBody = GenericDenseStore.COMPLEX.makeZero(tmpRowDim, tmpColDim);
        final PhysicalStore<ComplexNumber> tmpRHS = GenericDenseStore.COMPLEX.makeZero(tmpRowDim, 1);

        for (int i = 0; i < tmpRowDim; i++) {

            ComplexNumber tmpX = ComplexNumber.ONE;
            final ComplexNumber tmpXfactor = ComplexNumber.valueOf(x.get(i));
            final ComplexNumber tmpY = ComplexNumber.valueOf(y.get(i));

            for (int j = 0; j < tmpColDim; j++) {
                tmpBody.set(i, j, tmpX);
                tmpX = tmpX.multiply(tmpXfactor);
            }
            tmpRHS.set(i, 0, tmpY);
        }

        final QR<ComplexNumber> tmpQR = QR.COMPLEX.make();
        tmpQR.decompose(tmpBody);
        this.set(tmpQR.getSolution(tmpRHS));
    }

    public ComplexNumber integrate(final ComplexNumber fromPoint, final ComplexNumber toPoint) {

        final PolynomialFunction<ComplexNumber> tmpPrim = this.buildPrimitive();

        final ComplexNumber tmpFromVal = tmpPrim.invoke(fromPoint);
        final ComplexNumber tmpToVal = tmpPrim.invoke(toPoint);

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
        final int tmpLimit = (int) Math.min(this.size(), coefficients.count());
        for (int p = 0; p < tmpLimit; p++) {
            this.set(p, ComplexNumber.valueOf(coefficients.get(p)));
        }
    }

    @Override
    protected ComplexNumber getDerivativeFactor(final int power) {
        final int tmpNextIndex = power + 1;
        return this.get(tmpNextIndex).multiply(tmpNextIndex);
    }

    @Override
    protected ComplexNumber getPrimitiveFactor(final int power) {
        if (power <= 0) {
            return ComplexNumber.ZERO;
        } else {
            return this.get(power - 1).divide(power);
        }
    }

    @Override
    protected AbstractPolynomial<ComplexNumber> makeInstance(final int size) {
        return new ComplexPolynomial(Array1D.COMPLEX.makeZero(size));
    }

}
