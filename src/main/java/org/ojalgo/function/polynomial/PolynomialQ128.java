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

import org.ojalgo.array.ArrayQ128;
import org.ojalgo.array.BasicArray;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access1D;

public class PolynomialQ128 extends ScalarPolynomial<RationalNumber, PolynomialQ128> {

    public static final PolynomialQ128 ONE = PolynomialQ128.wrap(RationalNumber.ONE);

    public static PolynomialQ128 wrap(final RationalNumber... coefficients) {
        return new PolynomialQ128(ArrayQ128.wrap(coefficients));
    }

    public PolynomialQ128(final int degree) {
        super(ArrayQ128.make(degree + 1));
    }

    PolynomialQ128(final BasicArray<RationalNumber> coefficients) {
        super(coefficients);
    }

    @Override
    public void estimate(final Access1D<?> x, final Access1D<?> y) {
        this.estimate(x, y, GenericStore.Q128, QR.Q128);
    }

    @Override
    protected RationalNumber getDerivativeFactor(final int power) {
        int nextIndex = power + 1;
        return this.get(nextIndex).multiply(nextIndex);
    }

    @Override
    protected RationalNumber getPrimitiveFactor(final int power) {
        if (power <= 0) {
            return RationalNumber.ZERO;
        }
        return this.get(power - 1).divide(power);
    }

    @Override
    protected PolynomialQ128 newInstance(final int size) {
        return new PolynomialQ128(ArrayQ128.make(size));
    }

    @Override
    PolynomialQ128 one() {
        return ONE;
    }

}
