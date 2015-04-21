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
import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;

/**
 * [x]<sup>T</sup>[Q][x] + [l]<sup>T</sup>[x] + c
 *
 * @author apete
 */
public final class CompoundFunction<N extends Number> extends AbstractMultiary<N, CompoundFunction<N>> implements MultiaryFunction.Linear<N>,
        MultiaryFunction.Quadratic<N> {

    public static CompoundFunction<BigDecimal> makeBig(final Access2D<? extends Number> quadraticFactors, final Access1D<? extends Number> linearFactors) {
        final QuadraticFunction<BigDecimal> tmpQuadratic = QuadraticFunction.makeBig(quadraticFactors);
        final LinearFunction<BigDecimal> tmpLinear = LinearFunction.makeBig(linearFactors);
        return new CompoundFunction<BigDecimal>(tmpQuadratic, tmpLinear);
    }

    public static CompoundFunction<BigDecimal> makeBig(final int arity) {
        final QuadraticFunction<BigDecimal> tmpQuadratic = QuadraticFunction.makeBig(arity);
        final LinearFunction<BigDecimal> tmpLinear = LinearFunction.makeBig(arity);
        return new CompoundFunction<BigDecimal>(tmpQuadratic, tmpLinear);
    }

    public static CompoundFunction<ComplexNumber> makeComplex(final Access2D<? extends Number> quadraticFactors, final Access1D<? extends Number> linearFactors) {
        final QuadraticFunction<ComplexNumber> tmpQuadratic = QuadraticFunction.makeComplex(quadraticFactors);
        final LinearFunction<ComplexNumber> tmpLinear = LinearFunction.makeComplex(linearFactors);
        return new CompoundFunction<ComplexNumber>(tmpQuadratic, tmpLinear);
    }

    public static CompoundFunction<ComplexNumber> makeComplex(final int arity) {
        final QuadraticFunction<ComplexNumber> tmpQuadratic = QuadraticFunction.makeComplex(arity);
        final LinearFunction<ComplexNumber> tmpLinear = LinearFunction.makeComplex(arity);
        return new CompoundFunction<ComplexNumber>(tmpQuadratic, tmpLinear);
    }

    public static CompoundFunction<Double> makePrimitive(final Access2D<? extends Number> quadraticFactors, final Access1D<? extends Number> linearFactors) {
        final QuadraticFunction<Double> tmpQuadratic = QuadraticFunction.makePrimitive(quadraticFactors);
        final LinearFunction<Double> tmpLinear = LinearFunction.makePrimitive(linearFactors);
        return new CompoundFunction<Double>(tmpQuadratic, tmpLinear);
    }

    public static CompoundFunction<Double> makePrimitive(final int arity) {
        final QuadraticFunction<Double> tmpQuadratic = QuadraticFunction.makePrimitive(arity);
        final LinearFunction<Double> tmpLinear = LinearFunction.makePrimitive(arity);
        return new CompoundFunction<Double>(tmpQuadratic, tmpLinear);
    }

    private final LinearFunction<N> myLinear;
    private final QuadraticFunction<N> myQuadratic;

    @SuppressWarnings("unused")
    private CompoundFunction() {
        this((QuadraticFunction<N>) null, (LinearFunction<N>) null);
    }

    CompoundFunction(final QuadraticFunction<N> quadratic, final LinearFunction<N> linear) {

        super();

        myQuadratic = quadratic;
        myLinear = linear;

        if (myQuadratic.arity() != myLinear.arity()) {
            throw new IllegalArgumentException("Must have the same dim()!");
        }
    }

    public int arity() {
        return myLinear.arity();
    }

    @Override
    public MatrixStore<N> getGradient(final Access1D<N> arg) {
        return myQuadratic.getGradient(arg).builder().superimpose(0, 0, myLinear.getGradient(arg)).build();
    }

    @Override
    public MatrixStore<N> getHessian(final Access1D<N> arg) {
        return myQuadratic.getHessian(arg);
    }

    @Override
    public N invoke(final Access1D<N> arg) {

        Scalar<N> retVal = this.getScalarConstant();

        retVal = retVal.add(myLinear.invoke(arg));

        retVal = retVal.add(myQuadratic.invoke(arg));

        return retVal.getNumber();
    }

    public PhysicalStore<N> linear() {
        return myLinear.linear();
    }

    public PhysicalStore<N> quadratic() {
        return myQuadratic.quadratic();
    }

    @Override
    protected Factory<N, ?> factory() {
        return myLinear.factory();
    }

}
