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
package org.ojalgo.function.multiary;

import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
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

    public static QuadraticFunction<ComplexNumber> makeComplex(final Access2D<?> quadratic, final Access1D<?> linear) {
        return new QuadraticFunction<>(GenericStore.COMPLEX.copy(quadratic), GenericStore.COMPLEX.columns(linear));
    }

    public static QuadraticFunction<ComplexNumber> makeComplex(final int arity) {
        return new QuadraticFunction<>(GenericStore.COMPLEX.make(arity, arity), GenericStore.COMPLEX.make(arity, 1));
    }

    public static QuadraticFunction<Double> makePrimitive(final Access2D<?> quadratic, final Access1D<?> linear) {
        return new QuadraticFunction<>(Primitive64Store.FACTORY.copy(quadratic), Primitive64Store.FACTORY.columns(linear));
    }

    public static QuadraticFunction<Double> makePrimitive(final int arity) {
        return new QuadraticFunction<>(Primitive64Store.FACTORY.make(arity, arity), Primitive64Store.FACTORY.make(arity, 1));
    }

    public static QuadraticFunction<RationalNumber> makeRational(final Access2D<?> quadratic, final Access1D<?> linear) {
        return new QuadraticFunction<>(GenericStore.RATIONAL.copy(quadratic), GenericStore.RATIONAL.columns(linear));
    }

    public static QuadraticFunction<RationalNumber> makeRational(final int arity) {
        return new QuadraticFunction<>(GenericStore.RATIONAL.make(arity, arity), GenericStore.RATIONAL.make(arity, 1));
    }

    public static <N extends Comparable<N>> QuadraticFunction<N> wrap(final PhysicalStore<N> quadratic, final PhysicalStore<N> linear) {
        return new QuadraticFunction<>(quadratic, linear);
    }

    private final LinearFunction<N> myLinear;
    private final PureQuadraticFunction<N> myPureQuadratic;

    QuadraticFunction(final MatrixStore<N> quadratic, final MatrixStore<N> linear) {

        super();

        myPureQuadratic = new PureQuadraticFunction<>(quadratic);
        myLinear = new LinearFunction<>(linear);

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

    public MatrixStore<N> getLinearFactors() {
        return myLinear.getLinearFactors();
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

    Factory<N, ?> factory() {
        return myLinear.factory();
    }

    Scalar<N> getScalarValue(final Access1D<N> arg) {

        Scalar<N> retVal = myPureQuadratic.getScalarValue(arg);

        N linearPart = myLinear.invoke(arg);

        return retVal.add(linearPart);
    }

}
