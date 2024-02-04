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
 * [x]<sup>T</sup>[Q][x] + [l]<sup>T</sup>[x] + c
 *
 * @author apete
 */
public final class QuadraticFunction<N extends Comparable<N>> implements MultiaryFunction.TwiceDifferentiable<N>, MultiaryFunction.Quadratic<N> {

    public static final class Factory<N extends Comparable<N>> {

        private final LinearFunction.Factory<N> myLinear;
        private final PureQuadraticFunction.Factory<N> myPureQuadratic;

        Factory(final PhysicalStore.Factory<N, ?> factory) {
            super();
            myPureQuadratic = new PureQuadraticFunction.Factory<>(factory);
            myLinear = new LinearFunction.Factory<>(factory);
        }

        public QuadraticFunction.Factory<N> linear(final Access1D<?> coefficients) {
            myLinear.coefficients(coefficients);
            return this;
        }

        public QuadraticFunction<N> make(final int arity) {
            return new QuadraticFunction<>(myPureQuadratic.make(arity), myLinear.make(arity));
        }

        public QuadraticFunction.Factory<N> quadratic(final Access2D<?> coefficients) {
            myPureQuadratic.coefficients(coefficients);
            return this;
        }

    }

    public static <N extends Comparable<N>> Factory<N> factory(final PhysicalStore.Factory<N, ?> factory) {
        return new Factory<>(factory);
    }

    /**
     * @deprecated v53 Use {@link #factory(PhysicalStore.Factory)} instead.
     */
    @Deprecated
    public static QuadraticFunction<ComplexNumber> makeComplex(final Access2D<?> quadratic, final Access1D<?> linear) {
        // return new QuadraticFunction<>(GenericStore.C128.copy(quadratic), GenericStore.C128.columns(linear));
        return QuadraticFunction.factory(GenericStore.C128).quadratic(quadratic).linear(linear).make(linear.size());
    }

    /**
     * @deprecated v53 Use {@link #factory(PhysicalStore.Factory)} instead.
     */
    @Deprecated
    public static QuadraticFunction<ComplexNumber> makeComplex(final int arity) {
        // return new QuadraticFunction<>(GenericStore.C128.make(arity, arity), GenericStore.C128.make(arity, 1));
        return QuadraticFunction.factory(GenericStore.C128).make(arity);
    }

    /**
     * @deprecated v53 Use {@link #factory(PhysicalStore.Factory)} instead.
     */
    @Deprecated
    public static QuadraticFunction<Double> makePrimitive(final Access2D<?> quadratic, final Access1D<?> linear) {
        // return new QuadraticFunction<>(Primitive64Store.FACTORY.copy(quadratic), Primitive64Store.FACTORY.columns(linear));
        return QuadraticFunction.factory(Primitive64Store.FACTORY).quadratic(quadratic).linear(linear).make(linear.size());
    }

    /**
     * @deprecated v53 Use {@link #factory(PhysicalStore.Factory)} instead.
     */
    @Deprecated
    public static QuadraticFunction<Double> makePrimitive(final int arity) {
        // return new QuadraticFunction<>(Primitive64Store.FACTORY.make(arity, arity), Primitive64Store.FACTORY.make(arity, 1));
        return QuadraticFunction.factory(Primitive64Store.FACTORY).make(arity);
    }

    /**
     * @deprecated v53 Use {@link #factory(PhysicalStore.Factory)} instead.
     */
    @Deprecated
    public static QuadraticFunction<RationalNumber> makeRational(final Access2D<?> quadratic, final Access1D<?> linear) {
        // return new QuadraticFunction<>(GenericStore.Q128.copy(quadratic), GenericStore.Q128.columns(linear));
        return QuadraticFunction.factory(GenericStore.Q128).quadratic(quadratic).linear(linear).make(linear.size());
    }

    /**
     * @deprecated v53 Use {@link #factory(PhysicalStore.Factory)} instead.
     */
    @Deprecated
    public static QuadraticFunction<RationalNumber> makeRational(final int arity) {
        // return new QuadraticFunction<>(GenericStore.Q128.make(arity, arity), GenericStore.Q128.make(arity, 1));
        return QuadraticFunction.factory(GenericStore.Q128).make(arity);
    }

    public static <N extends Comparable<N>> QuadraticFunction<N> wrap(final PhysicalStore<N> quadratic, final PhysicalStore<N> linear) {
        return new QuadraticFunction<>(quadratic, linear);
    }

    private final LinearFunction<N> myLinear;
    private final PureQuadraticFunction<N> myPureQuadratic;

    QuadraticFunction(final MatrixStore<N> quadratic, final MatrixStore<N> linear) {
        this(new PureQuadraticFunction<>(quadratic), new LinearFunction<>(linear));
    }

    QuadraticFunction(final PureQuadraticFunction<N> pureQuadratic, final LinearFunction<N> linear) {
        super();
        myPureQuadratic = pureQuadratic;
        myLinear = linear;
        if (myPureQuadratic.arity() != myLinear.arity()) {
            throw new IllegalArgumentException("Must have the same arity!");
        }
    }

    public int arity() {
        return myLinear.arity();
    }

    public N getConstant() {
        return myPureQuadratic.getConstant();
    }

    @Override
    public MatrixStore<N> getGradient(final Access1D<N> point) {
        MatrixStore<N> pureQuadraticPart = myPureQuadratic.getGradient(point);
        MatrixStore<N> linearPart = myLinear.getGradient(point);
        return pureQuadraticPart.add(linearPart);
    }

    @Override
    public MatrixStore<N> getHessian(final Access1D<N> point) {
        return myPureQuadratic.getHessian(point);
    }

    public MatrixStore<N> getLinearFactors(final boolean negated) {
        return myLinear.getLinearFactors(negated);
    }

    @Override
    public N invoke(final Access1D<N> arg) {
        return this.getScalarValue(arg).get();
    }

    public PhysicalStore<N> linear() {
        return myLinear.linear();
    }

    public PhysicalStore<N> quadratic() {
        return myPureQuadratic.quadratic();
    }

    public void setConstant(final Comparable<?> constant) {
        myPureQuadratic.setConstant(constant);
    }

    PhysicalStore.Factory<N, ?> factory() {
        return myLinear.factory();
    }

    Scalar<N> getScalarValue(final Access1D<N> arg) {

        Scalar<N> retVal = myPureQuadratic.getScalarValue(arg);

        N linearPart = myLinear.invoke(arg);

        return retVal.add(linearPart);
    }

}
