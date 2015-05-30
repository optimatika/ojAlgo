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

import java.math.BigDecimal;

import org.ojalgo.access.Access1D;
import org.ojalgo.array.Array1D;
import org.ojalgo.constant.BigMath;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.type.TypeUtils;

/**
 * BigPolynomial
 * 
 * @author apete
 */
public class BigPolynomial extends AbstractPolynomial<BigDecimal> {

    public BigPolynomial(final int aDegree) {
        super(Array1D.BIG.makeZero(aDegree + 1));
    }

    BigPolynomial(final Array1D<BigDecimal> someCoefficients) {
        super(someCoefficients);
    }

    public void estimate(final Access1D<?> x, final Access1D<?> y) {

        final int tmpRowDim = (int) Math.min(x.count(), y.count());
        final int tmpColDim = this.size();

        final PhysicalStore<BigDecimal> tmpBody = BigDenseStore.FACTORY.makeZero(tmpRowDim, tmpColDim);
        final PhysicalStore<BigDecimal> tmpRHS = BigDenseStore.FACTORY.makeZero(tmpRowDim, 1);

        for (int i = 0; i < tmpRowDim; i++) {

            BigDecimal tmpX = BigMath.ONE;
            final BigDecimal tmpXfactor = TypeUtils.toBigDecimal(x.get(i));
            final BigDecimal tmpY = TypeUtils.toBigDecimal(y.get(i));

            for (int j = 0; j < tmpColDim; j++) {
                tmpBody.set(i, j, tmpX);
                tmpX = tmpX.multiply(tmpXfactor);
            }
            tmpRHS.set(i, 0, tmpY);
        }

        final QR<BigDecimal> tmpQR = QR.makeBig();
        tmpQR.decompose(tmpBody);
        this.set(tmpQR.solve(tmpRHS));
    }

    public BigDecimal integrate(final BigDecimal fromPoint, final BigDecimal toPoint) {

        final PolynomialFunction<BigDecimal> tmpPrim = this.buildPrimitive();

        final BigDecimal tmpFromVal = tmpPrim.invoke(fromPoint);
        final BigDecimal tmpToVal = tmpPrim.invoke(toPoint);

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

    public void set(final Access1D<?> someCoefficient) {
        final int tmpLimit = (int) Math.min(this.count(), someCoefficient.count());
        for (int p = 0; p < tmpLimit; p++) {
            this.set(p, TypeUtils.toBigDecimal(someCoefficient.get(p)));
        }
    }

    @Override
    protected BigDecimal getDerivativeFactor(final int aPower) {
        final int tmpNextIndex = aPower + 1;
        return this.get(tmpNextIndex).multiply(new BigDecimal(tmpNextIndex));
    }

    @Override
    protected BigDecimal getPrimitiveFactor(final int aPower) {
        if (aPower <= 0) {
            return BigMath.ZERO;
        } else {
            return this.get(aPower - 1).divide(new BigDecimal(aPower));
        }
    }

    @Override
    protected AbstractPolynomial<BigDecimal> makeInstance(final int aSize) {
        return new BigPolynomial(Array1D.BIG.makeZero(aSize));
    }

}
