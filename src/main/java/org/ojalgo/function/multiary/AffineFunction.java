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

/**
 * [l]<sup>T</sup>[x] + c
 *
 * @author apete
 */
public final class AffineFunction<N extends Comparable<N>> implements MultiaryFunction.TwiceDifferentiable<N>, MultiaryFunction.Affine<N> {

    public static AffineFunction<ComplexNumber> makeComplex(final Access1D<?> coefficients) {
        return new AffineFunction<>(GenericStore.COMPLEX.rows(coefficients));
    }

    public static AffineFunction<ComplexNumber> makeComplex(final int arity) {
        return new AffineFunction<>(GenericStore.COMPLEX.make(1, arity));
    }

    public static AffineFunction<Double> makePrimitive(final Access1D<?> coefficients) {
        return new AffineFunction<>(Primitive64Store.FACTORY.rows(coefficients));
    }

    public static AffineFunction<Double> makePrimitive(final int arity) {
        return new AffineFunction<>(Primitive64Store.FACTORY.make(1, arity));
    }

    public static AffineFunction<RationalNumber> makeRational(final Access1D<?> coefficients) {
        return new AffineFunction<>(GenericStore.RATIONAL.rows(coefficients));
    }

    public static AffineFunction<RationalNumber> makeRational(final int arity) {
        return new AffineFunction<>(GenericStore.RATIONAL.make(1, arity));
    }

    public static <N extends Comparable<N>> AffineFunction<N> wrap(final PhysicalStore<N> coefficients) {
        return new AffineFunction<>(coefficients);
    }

    private final MatrixStore<N> myCoefficients;
    private final ConstantFunction<N> myConstant;

    AffineFunction(final MatrixStore<N> coefficients) {

        super();

        if (!coefficients.isVector()) {
            throw new IllegalArgumentException("Must be a  vector!");
        }

        myCoefficients = coefficients;
        myConstant = new ConstantFunction<>(coefficients.count(), coefficients.physical());
    }

    public int arity() {
        return Math.toIntExact(myCoefficients.count());
    }

    public N getConstant() {
        return myConstant.getConstant();
    }

    @Override
    public MatrixStore<N> getGradient(final Access1D<N> point) {
        return this.getLinearFactors();
    }

    @Override
    public MatrixStore<N> getHessian(final Access1D<N> point) {
        return myCoefficients.physical().makeZero(this.arity(), this.arity());
    }

    public MatrixStore<N> getLinearFactors() {
        if (myCoefficients.countRows() == 1L) {
            return myCoefficients.transpose();
        }
        return myCoefficients;
    }

    @Override
    public N invoke(final Access1D<N> arg) {
        return this.getScalarValue(arg).get();
    }

    public PhysicalStore<N> linear() {
        return (PhysicalStore<N>) myCoefficients;
    }

    public void setConstant(final Comparable<?> constant) {
        myConstant.setConstant(constant);
    }

    Factory<N, ?> factory() {
        return myCoefficients.physical();
    }

    Scalar<N> getScalarValue(final Access1D<N> arg) {

        PhysicalStore<N> preallocated = myCoefficients.physical().make(1L, 1L);

        Scalar<N> retVal = myConstant.getScalarConstant();

        myCoefficients.multiply(arg, preallocated);

        retVal = retVal.add(preallocated.get(0, 0));

        return retVal;
    }

}
