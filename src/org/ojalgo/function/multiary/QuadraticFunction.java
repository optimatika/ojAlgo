/*
 * Copyright 1997-2018 Optimatika
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

import java.math.BigDecimal;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;

/**
 * [x]<sup>T</sup>[Q][x] + c
 *
 * @author apete
 */
public final class QuadraticFunction<N extends Number> extends AbstractMultiary<N, QuadraticFunction<N>> implements MultiaryFunction.Quadratic<N> {

    public static QuadraticFunction<BigDecimal> makeBig(final Access2D<? extends Number> factors) {
        return new QuadraticFunction<>(BigDenseStore.FACTORY.copy(factors));
    }

    public static QuadraticFunction<BigDecimal> makeBig(final int arity) {
        return new QuadraticFunction<>(BigDenseStore.FACTORY.makeZero(arity, arity));
    }

    public static QuadraticFunction<ComplexNumber> makeComplex(final Access2D<? extends Number> factors) {
        return new QuadraticFunction<>(GenericDenseStore.COMPLEX.copy(factors));
    }

    public static QuadraticFunction<ComplexNumber> makeComplex(final int arity) {
        return new QuadraticFunction<>(GenericDenseStore.COMPLEX.makeZero(arity, arity));
    }

    public static QuadraticFunction<Double> makePrimitive(final Access2D<? extends Number> factors) {
        return new QuadraticFunction<>(PrimitiveDenseStore.FACTORY.copy(factors));
    }

    public static QuadraticFunction<Double> makePrimitive(final int arity) {
        return new QuadraticFunction<>(PrimitiveDenseStore.FACTORY.makeZero(arity, arity));
    }

    private final MatrixStore<N> myFactors;

    QuadraticFunction(final MatrixStore<N> factors) {

        super();

        myFactors = factors;

        if (myFactors.countRows() != myFactors.countColumns()) {
            throw new IllegalArgumentException("Must be sqaure!");
        }
    }

    public int arity() {
        return (int) myFactors.countColumns();
    }

    @Override
    public MatrixStore<N> getGradient(final Access1D<N> point) {

        final PhysicalStore<N> tmpPreallocated = myFactors.physical().makeZero(this.arity(), 1L);

        this.getHessian(point).multiply(point, tmpPreallocated);

        return tmpPreallocated;
    }

    @Override
    public MatrixStore<N> getHessian(final Access1D<N> point) {
        return myFactors.logical().superimpose(0, 0, myFactors.conjugate()).get();
    }

    @Override
    public N invoke(final Access1D<N> arg) {

        Scalar<N> retVal = this.getScalarConstant();

        retVal = retVal.add(myFactors.multiplyBoth(arg));

        return retVal.get();
    }

    public PhysicalStore<N> quadratic() {
        return (PhysicalStore<N>) myFactors;
    }

    @Override
    protected org.ojalgo.matrix.store.PhysicalStore.Factory<N, ?> factory() {
        return myFactors.physical();
    }

}
