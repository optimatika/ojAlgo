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
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.structure.Access1D;

public final class PrimitivePolynomial extends AbstractPolynomial<Double> {

    public PrimitivePolynomial(final int degree) {
        super(Array1D.PRIMITIVE64.make(degree + 1));
    }

    PrimitivePolynomial(final Array1D<Double> coefficients) {
        super(coefficients);
    }

    public void estimate(final Access1D<?> x, final Access1D<?> y) {

        int tmpRowDim = (int) Math.min(x.count(), y.count());
        int tmpColDim = this.size();

        PhysicalStore<Double> tmpBody = Primitive64Store.FACTORY.make(tmpRowDim, tmpColDim);
        PhysicalStore<Double> tmpRHS = Primitive64Store.FACTORY.make(tmpRowDim, 1);

        for (int i = 0; i < tmpRowDim; i++) {

            double tmpX = PrimitiveMath.ONE;
            double tmpXfactor = x.doubleValue(i);
            double tmpY = y.doubleValue(i);

            for (int j = 0; j < tmpColDim; j++) {
                tmpBody.set(i, j, tmpX);
                tmpX *= tmpXfactor;
            }
            tmpRHS.set(i, 0, tmpY);
        }

        QR<Double> tmpQR = QR.PRIMITIVE.make();
        tmpQR.decompose(tmpBody);
        this.set(tmpQR.getSolution(tmpRHS));
    }

    public Double integrate(final Double fromPoint, final Double toPoint) {

        PolynomialFunction<Double> tmpPrim = this.buildPrimitive();

        double tmpFromVal = tmpPrim.invoke(fromPoint.doubleValue());
        double tmpToVal = tmpPrim.invoke(toPoint.doubleValue());

        return tmpToVal - tmpFromVal;
    }

    public Double invoke(final Double arg) {
        return this.invoke(arg.doubleValue());
    }

    public void set(final Access1D<?> coefficients) {
        int tmpLimit = (int) Math.min(this.count(), coefficients.count());
        for (int p = 0; p < tmpLimit; p++) {
            this.set(p, coefficients.doubleValue(p));
        }
    }

    @Override
    protected Double getDerivativeFactor(final int power) {
        int tmpNextIndex = power + 1;
        return tmpNextIndex * this.doubleValue(tmpNextIndex);
    }

    @Override
    protected Double getPrimitiveFactor(final int power) {
        if (power <= 0) {
            return PrimitiveMath.ZERO;
        }
        return this.doubleValue(power - 1) / power;
    }

    @Override
    protected AbstractPolynomial<Double> makeInstance(final int size) {
        return new PrimitivePolynomial(Array1D.PRIMITIVE64.make(size));
    }

}
