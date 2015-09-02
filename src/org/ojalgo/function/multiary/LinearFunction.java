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
package org.ojalgo.function.multiary;

import java.math.BigDecimal;

import org.ojalgo.access.Access1D;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;

/**
 * [l]<sup>T</sup>[x] + c
 *
 * @author apete
 */
public final class LinearFunction<N extends Number> extends AbstractMultiary<N, LinearFunction<N>>implements MultiaryFunction.Linear<N> {

    public static LinearFunction<BigDecimal> makeBig(final Access1D<? extends Number> factors) {
        return new LinearFunction<BigDecimal>(BigDenseStore.FACTORY.rows(factors));
    }

    public static LinearFunction<BigDecimal> makeBig(final int arity) {
        return new LinearFunction<BigDecimal>(BigDenseStore.FACTORY.makeZero(1, arity));
    }

    public static LinearFunction<ComplexNumber> makeComplex(final Access1D<? extends Number> factors) {
        return new LinearFunction<ComplexNumber>(ComplexDenseStore.FACTORY.rows(factors));
    }

    public static LinearFunction<ComplexNumber> makeComplex(final int arity) {
        return new LinearFunction<ComplexNumber>(ComplexDenseStore.FACTORY.makeZero(1, arity));
    }

    public static LinearFunction<Double> makePrimitive(final Access1D<? extends Number> factors) {
        return new LinearFunction<Double>(PrimitiveDenseStore.FACTORY.rows(factors));
    }

    public static LinearFunction<Double> makePrimitive(final int arity) {
        return new LinearFunction<Double>(PrimitiveDenseStore.FACTORY.makeZero(1, arity));
    }

    private final MatrixStore<N> myFactors;

    @SuppressWarnings("unused")
    private LinearFunction() {
        this(null);
    }

    LinearFunction(final MatrixStore<N> factors) {

        super();

        myFactors = factors;

        if (myFactors.countRows() != 1L) {
            throw new IllegalArgumentException("Must be a row vector!");
        }
    }

    public int arity() {
        return (int) myFactors.countColumns();
    }

    @Override
    public MatrixStore<N> getGradient(final Access1D<N> arg) {
        return myFactors.transpose();
    }

    @Override
    public MatrixStore<N> getHessian(final Access1D<N> arg) {
        //return new ZeroStore<N>(myFactors.factory(), this.arity(), this.arity());
        return this.factory().builder().makeZero(this.arity(), this.arity()).get();
    }

    @Override
    public N invoke(final Access1D<N> arg) {

        Scalar<N> retVal = this.getScalarConstant();

        retVal = retVal.add(myFactors.multiply(arg).get(0, 0));

        return retVal.getNumber();
    }

    public PhysicalStore<N> linear() {
        return (PhysicalStore<N>) myFactors;
    }

    @Override
    protected Factory<N, ?> factory() {
        return myFactors.factory();
    }

}
