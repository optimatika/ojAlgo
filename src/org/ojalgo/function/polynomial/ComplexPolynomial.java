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

import org.ojalgo.access.Access1D;
import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.ComplexNumber;

public class ComplexPolynomial extends AbstractPolynomial<ComplexNumber> {

    public ComplexPolynomial(final int aDegree) {
        super(Array1D.COMPLEX.makeZero(aDegree + 1));
    }

    ComplexPolynomial(final Array1D<ComplexNumber> someCoefficients) {
        super(someCoefficients);
    }

    public void estimate(final Access1D<?> x, final Access1D<?> y) {

        final int tmpRowDim = (int) Math.min(x.count(), y.count());
        final int tmpColDim = this.size();

        final PhysicalStore<ComplexNumber> tmpBody = ComplexDenseStore.FACTORY.makeZero(tmpRowDim, tmpColDim);
        final PhysicalStore<ComplexNumber> tmpRHS = ComplexDenseStore.FACTORY.makeZero(tmpRowDim, 1);

        for (int i = 0; i < tmpRowDim; i++) {

            ComplexNumber tmpX = ComplexNumber.ONE;
            final ComplexNumber tmpXfactor = ComplexNumber.valueOf((Number) x.get(i));
            final ComplexNumber tmpY = ComplexNumber.valueOf((Number) y.get(i));

            for (int j = 0; j < tmpColDim; j++) {
                tmpBody.set(i, j, tmpX);
                tmpX = tmpX.multiply(tmpXfactor);
            }
            tmpRHS.set(i, 0, tmpY);
        }

        final QR<ComplexNumber> tmpQR = QR.makeComplex();
        tmpQR.decompose(tmpBody);
        this.set(tmpQR.solve(tmpRHS));
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

    public void set(final Access1D<?> someCoefficient) {
        final int tmpLimit = (int) Math.min(this.size(), someCoefficient.count());
        for (int p = 0; p < tmpLimit; p++) {
            this.set(p, ComplexNumber.valueOf((Number) someCoefficient.get(p)));
        }
    }

    @Override
    protected ComplexNumber getDerivativeFactor(final int aPower) {
        final int tmpNextIndex = aPower + 1;
        return this.get(tmpNextIndex).multiply(tmpNextIndex);
    }

    @Override
    protected ComplexNumber getPrimitiveFactor(final int aPower) {
        if (aPower <= 0) {
            return ComplexNumber.ZERO;
        } else {
            return this.get(aPower - 1).divide(aPower);
        }
    }

    @Override
    protected AbstractPolynomial<ComplexNumber> makeInstance(final int aSize) {
        return new ComplexPolynomial(Array1D.COMPLEX.makeZero(aSize));
    }

}
