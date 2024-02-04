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

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.ojalgo.array.BasicArray;
import org.ojalgo.data.transform.DiscreteFourierTransform;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.constant.ComplexMath;
import org.ojalgo.function.special.PowerOf2;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.series.NumberSeries;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

abstract class AbstractPolynomial<N extends Comparable<N>, P extends AbstractPolynomial<N, P>> implements PolynomialFunction<N> {

    public static final NumberContext DEGREE_ACCURACY = NumberContext.of(16);

    private final BasicArray<N> myCoefficients;
    private transient P myDerivative = null;
    private transient P myPrimitive = null;

    AbstractPolynomial(final BasicArray<N> coefficients) {

        super();

        myCoefficients = coefficients;
    }

    @Override
    public P add(final PolynomialFunction<N> addend) {

        int leftDeg = this.degree();
        int righDeg = addend.degree();

        int retSize = 1 + Math.max(leftDeg, righDeg);

        P retVal = this.newInstance(retSize);
        BasicArray<N> coefficients = retVal.coefficients();

        for (int l = 0; l <= leftDeg; l++) {
            coefficients.add(l, this.get(l));
        }

        for (int r = 0; r <= righDeg; r++) {
            coefficients.add(r, addend.get(r));
        }

        return retVal;
    }

    @Override
    public P buildDerivative() {

        if (myDerivative == null) {

            int tmpSize = Math.max(1, myCoefficients.size() - 1);

            myDerivative = this.newInstance(tmpSize);

            for (int i = 0; i < tmpSize; i++) {
                myDerivative.set(i, this.getDerivativeFactor(i));
            }
        }

        return myDerivative;
    }

    @Override
    public P buildPrimitive() {

        if (myPrimitive == null) {

            int tmpSize = myCoefficients.size() + 1;

            myPrimitive = this.newInstance(tmpSize);

            for (int i = 0; i < tmpSize; i++) {
                myPrimitive.set(i, this.getPrimitiveFactor(i));
            }
        }

        return myPrimitive;
    }

    @Override
    public long count() {
        return myCoefficients.count();
    }

    @Override
    public int degree(final NumberContext accuracy) {

        int retVal = myCoefficients.size() - 1;

        while (retVal > 0 && accuracy.isZero(this.norm(retVal))) {
            retVal--;
        }

        return retVal;
    }

    @Override
    public double doubleValue(final int power) {
        return myCoefficients.doubleValue(power);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        AbstractPolynomial<?, ?> other = (AbstractPolynomial<?, ?>) obj;
        return Objects.equals(myCoefficients, other.myCoefficients);
    }

    @Override
    public void estimate(final List<? extends N> x, final List<? extends N> y) {
        this.estimate(Access1D.wrap(x), Access1D.wrap(y));
    }

    @Override
    public void estimate(final NumberSeries<?> samples) {
        this.estimate(samples.accessKeys(), samples.accessValues());
    }

    @Override
    public N get(final long power) {
        return myCoefficients.get(power);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myCoefficients);
    }

    @Override
    public double invoke(final double arg) {

        int power = this.degree();

        double retVal = this.doubleValue(power);

        while (--power >= 0) {
            retVal = this.doubleValue(power) + arg * retVal;
        }

        return retVal;
    }

    @Override
    public float invoke(final float arg) {

        int power = this.degree();

        float retVal = this.floatValue(power);

        while (--power >= 0) {
            retVal = this.floatValue(power) + arg * retVal;
        }

        return retVal;
    }

    @Override
    public abstract P multiply(final PolynomialFunction<N> multiplicand);

    @Override
    public final P power(final int power) {

        if (power < 0) {

            throw new IllegalArgumentException();

        } else if (power == 0) {

            return this.one();

        } else if (power == 1) {

            return (P) this;

        } else if (power == 2) {

            return this.multiply(this);

        } else if (power == 3) {

            P temp = this.multiply(this);

            return temp.multiply(this);

        } else if (power == 4) {

            P temp = this.multiply(this);

            return temp.multiply(temp);

        } else {

            int degree = this.degree();

            int newSize = 1 + power * degree;

            int powerOf2 = PowerOf2.smallestNotLessThan(newSize);

            GenericStore<ComplexNumber> work = GenericStore.C128.make(powerOf2, 1);
            work.fillMatching(myCoefficients);

            DiscreteFourierTransform transformer = DiscreteFourierTransform.newInstance(powerOf2);

            transformer.transform(work, work);

            work.modifyAll(ComplexMath.POWER.parameter(power));

            transformer.inverse(work, work);

            P retVal = this.newInstance(newSize);

            retVal.coefficients().fillMatching(work);

            return retVal;
        }
    }

    @Override
    public final void set(final Access1D<?> coefficients) {
        if (coefficients.size() > myCoefficients.size()) {
            throw new IllegalArgumentException();
        }
        myCoefficients.reset();
        myCoefficients.fillMatching(coefficients);
        myDerivative = null;
        myPrimitive = null;
    }

    @Override
    public void set(final int power, final double coefficient) {
        myCoefficients.set(power, coefficient);
        myDerivative = null;
        myPrimitive = null;
    }

    @Override
    public void set(final int power, final N coefficient) {
        myCoefficients.set(power, coefficient);
        myDerivative = null;
        myPrimitive = null;
    }

    @Override
    public final void set(final long power, final Comparable<?> value) {
        myCoefficients.set(power, value);
    }

    @Override
    public int size() {
        return myCoefficients.size();
    }

    @Override
    public String toString() {
        return myCoefficients.toString();
    }

    protected abstract N getDerivativeFactor(int power);

    protected abstract N getPrimitiveFactor(int power);

    protected abstract P newInstance(int size);

    BasicArray<N> coefficients() {
        return myCoefficients;
    }

    void estimate(final Access1D<?> x, final Access1D<?> y, final PhysicalStore.Factory<N, ?> store, final QR.Factory<N> qr) {

        int tmpRowDim = Math.min(x.size(), y.size());
        int tmpColDim = this.size();

        PhysicalStore<N> tmpBody = store.make(tmpRowDim, tmpColDim);
        PhysicalStore<N> tmpRHS = store.make(tmpRowDim, 1);

        for (int i = 0; i < tmpRowDim; i++) {

            BigDecimal tmpX = BigMath.ONE;
            BigDecimal tmpXfactor = TypeUtils.toBigDecimal(x.get(i));
            BigDecimal tmpY = TypeUtils.toBigDecimal(y.get(i));

            for (int j = 0; j < tmpColDim; j++) {
                tmpBody.set(i, j, tmpX);
                tmpX = tmpX.multiply(tmpXfactor);
            }
            tmpRHS.set(i, 0, tmpY);
        }

        QR<N> tmpQR = qr.make(tmpBody);
        tmpQR.decompose(tmpBody);
        this.set(tmpQR.getSolution(tmpRHS));
    }

    double norm(final int power) {
        return Math.abs(this.doubleValue(power));
    }

    abstract P one();

}
