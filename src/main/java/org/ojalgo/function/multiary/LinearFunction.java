/*
 * Copyright 1997-2025 Optimatika
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
package org.ojalgo.function.multiary;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.structure.Access1D;

/**
 * [l]<sup>T</sup>[x]
 *
 * @author apete
 */
public final class LinearFunction<N extends Comparable<N>> implements MultiaryFunction.TwiceDifferentiable<N>, MultiaryFunction.Linear<N> {

    public static final class Factory<N extends Comparable<N>> {

        private Access1D<?> myCoefficients = null;
        private final PhysicalStore.Factory<N, ?> myFactory;

        Factory(final PhysicalStore.Factory<N, ?> factory) {
            super();
            myFactory = factory;
        }

        public Factory<N> coefficients(final Access1D<?> coefficients) {
            myCoefficients = coefficients;
            return this;
        }

        public LinearFunction<N> make(final int arity) {
            if (myCoefficients != null) {
                return new LinearFunction<>(myFactory.row(myCoefficients));
            } else {
                return new LinearFunction<>(myFactory.make(1, arity));
            }
        }

    }

    public static <N extends Comparable<N>> Factory<N> factory(final PhysicalStore.Factory<N, ?> factory) {
        return new Factory<>(factory);
    }

    public static <N extends Comparable<N>> LinearFunction<N> wrap(final PhysicalStore<N> coefficients) {
        return new LinearFunction<>(coefficients);
    }

    private final MatrixStore<N> myCoefficients;

    LinearFunction(final MatrixStore<N> coefficients) {

        super();

        if (!coefficients.isVector()) {
            throw new IllegalArgumentException("Must be a  vector!");
        }

        myCoefficients = coefficients;
    }

    @Override
    public int arity() {
        return Math.toIntExact(myCoefficients.count());
    }

    @Override
    public MatrixStore<N> getGradient(final Access1D<N> point) {
        return this.getLinearFactors(false);
    }

    @Override
    public MatrixStore<N> getHessian(final Access1D<N> point) {
        return this.factory().makeZero(this.arity(), this.arity());
    }

    @Override
    public MatrixStore<N> getLinearFactors(final boolean negated) {

        MatrixStore<N> retVal = myCoefficients;

        if (myCoefficients.countRows() == 1L) {
            retVal = retVal.transpose();
        }

        if (negated) {
            retVal = retVal.negate();
        }

        return retVal;
    }

    @Override
    public N invoke(final Access1D<N> arg) {

        PhysicalStore<N> preallocated = myCoefficients.physical().make(1L, 1L);

        myCoefficients.multiply(arg, preallocated);

        return preallocated.get(0, 0);
    }

    @Override
    public PhysicalStore<N> linear() {
        return (PhysicalStore<N>) myCoefficients;
    }

    PhysicalStore.Factory<N, ?> factory() {
        return myCoefficients.physical();
    }

}
