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
package org.ojalgo.function.multiary;

import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;

/**
 * [x]<sup>T</sup>[Q][x] + c
 *
 * @author apete
 */
public final class PureQuadraticFunction<N extends Comparable<N>> implements MultiaryFunction.TwiceDifferentiable<N>, MultiaryFunction.PureQuadratic<N> {

    public static final class Factory<N extends Comparable<N>> {

        private Access2D<?> myCoefficients = null;
        private final PhysicalStore.Factory<N, ?> myFactory;

        Factory(final PhysicalStore.Factory<N, ?> factory) {
            super();
            myFactory = factory;
        }

        public Factory<N> coefficients(final Access2D<?> coefficients) {
            myCoefficients = coefficients;
            return this;
        }

        public PureQuadraticFunction<N> make(final int arity) {
            if (myCoefficients != null) {
                return new PureQuadraticFunction<>(myFactory.copy(myCoefficients));
            } else {
                return new PureQuadraticFunction<>(myFactory.make(arity, arity));
            }
        }

    }

    public static <N extends Comparable<N>> Factory<N> factory(final PhysicalStore.Factory<N, ?> factory) {
        return new Factory<>(factory);
    }

    /**
     * @deprecated v53 Use {@link #factory(PhysicalStore.Factory)} instead.
     */
    @Deprecated
    public static PureQuadraticFunction<ComplexNumber> makeComplex(final Access2D<?> coefficients) {
        // return new PureQuadraticFunction<>(GenericStore.C128.copy(coefficients));
        return PureQuadraticFunction.factory(GenericStore.C128).coefficients(coefficients).make(coefficients.getMaxDim());
    }

    /**
     * @deprecated v53 Use {@link #factory(PhysicalStore.Factory)} instead.
     */
    @Deprecated
    public static PureQuadraticFunction<ComplexNumber> makeComplex(final int arity) {
        // return new PureQuadraticFunction<>(GenericStore.C128.make(arity, arity));
        return PureQuadraticFunction.factory(GenericStore.C128).make(arity);
    }

    /**
     * @deprecated v53 Use {@link #factory(PhysicalStore.Factory)} instead.
     */
    @Deprecated
    public static PureQuadraticFunction<Double> makePrimitive(final Access2D<?> coefficients) {
        // return new PureQuadraticFunction<>(Primitive64Store.FACTORY.copy(coefficients));
        return PureQuadraticFunction.factory(Primitive64Store.FACTORY).coefficients(coefficients).make(coefficients.getMaxDim());
    }

    /**
     * @deprecated v53 Use {@link #factory(PhysicalStore.Factory)} instead.
     */
    @Deprecated
    public static PureQuadraticFunction<Double> makePrimitive(final int arity) {
        // return new PureQuadraticFunction<>(Primitive64Store.FACTORY.make(arity, arity));
        return PureQuadraticFunction.factory(Primitive64Store.FACTORY).make(arity);
    }

    /**
     * @deprecated v53 Use {@link #factory(PhysicalStore.Factory)} instead.
     */
    @Deprecated
    public static PureQuadraticFunction<RationalNumber> makeRational(final Access2D<?> coefficients) {
        // return new PureQuadraticFunction<>(GenericStore.Q128.copy(coefficients));
        return PureQuadraticFunction.factory(GenericStore.Q128).coefficients(coefficients).make(coefficients.getMaxDim());
    }

    /**
     * @deprecated v53 Use {@link #factory(PhysicalStore.Factory)} instead.
     */
    @Deprecated
    public static PureQuadraticFunction<RationalNumber> makeRational(final int arity) {
        // return new PureQuadraticFunction<>(GenericStore.Q128.make(arity, arity));
        return PureQuadraticFunction.factory(GenericStore.Q128).make(arity);
    }

    public static <N extends Comparable<N>> PureQuadraticFunction<N> wrap(final PhysicalStore<N> coefficients) {
        return new PureQuadraticFunction<>(coefficients);
    }

    private final MatrixStore<N> myCoefficients;
    private final ConstantFunction<N> myConstant;

    PureQuadraticFunction(final MatrixStore<N> coefficients) {

        super();

        if (!coefficients.isSquare()) {
            throw new IllegalArgumentException("Must be sqaure!");
        }

        myCoefficients = coefficients;
        myConstant = new ConstantFunction<>(coefficients.countRows(), coefficients.physical());
    }

    public int arity() {
        return (int) myCoefficients.countColumns();
    }

    public N getConstant() {
        return myConstant.getConstant();
    }

    @Override
    public MatrixStore<N> getGradient(final Access1D<N> point) {

        final PhysicalStore<N> retVal = myCoefficients.physical().make(this.arity(), 1L);

        this.getHessian(point).multiply(point, retVal);

        return retVal;
    }

    @Override
    public MatrixStore<N> getHessian(final Access1D<N> point) {
        return myCoefficients.add(myCoefficients.conjugate());
    }

    public MatrixStore<N> getLinearFactors(final boolean negated) {
        return myCoefficients.physical().makeZero(this.arity(), 1L);
    }

    @Override
    public N invoke(final Access1D<N> arg) {
        return this.getScalarValue(arg).get();
    }

    public PhysicalStore<N> quadratic() {
        return (PhysicalStore<N>) myCoefficients;
    }

    public void setConstant(final Comparable<?> constant) {
        myConstant.setConstant(constant);
    }

    PhysicalStore.Factory<N, ?> factory() {
        return myCoefficients.physical();
    }

    Scalar<N> getScalarValue(final Access1D<N> arg) {

        Scalar<N> retVal = myConstant.getScalarConstant();

        return retVal.add(myCoefficients.multiplyBoth(arg));
    }

}
