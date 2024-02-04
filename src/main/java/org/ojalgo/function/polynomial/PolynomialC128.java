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

import org.ojalgo.array.ArrayC128;
import org.ojalgo.array.BasicArray;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.structure.Access1D;

public class PolynomialC128 extends ScalarPolynomial<ComplexNumber, PolynomialC128> {

    public static final PolynomialC128 ONE = PolynomialC128.wrap(ComplexNumber.ONE);

    public static PolynomialC128 wrap(final ComplexNumber... coefficients) {
        return new PolynomialC128(ArrayC128.wrap(coefficients));
    }

    public PolynomialC128(final int degree) {
        super(ArrayC128.make(degree + 1));
    }

    PolynomialC128(final BasicArray<ComplexNumber> coefficients) {
        super(coefficients);
    }

    @Override
    public void estimate(final Access1D<?> x, final Access1D<?> y) {
        this.estimate(x, y, GenericStore.C128, QR.C128);
    }

    @Override
    protected ComplexNumber getDerivativeFactor(final int power) {
        int nextIndex = power + 1;
        return this.get(nextIndex).multiply(nextIndex);
    }

    @Override
    protected ComplexNumber getPrimitiveFactor(final int power) {
        if (power <= 0) {
            return ComplexNumber.ZERO;
        }
        return this.get(power - 1).divide(power);
    }

    @Override
    protected PolynomialC128 newInstance(final int size) {
        return new PolynomialC128(ArrayC128.make(size));
    }

    @Override
    PolynomialC128 one() {
        return ONE;
    }

}
