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
package org.ojalgo.optimisation.convex;

import org.ojalgo.function.multiary.LinearFunction;
import org.ojalgo.function.multiary.MultiaryFunction;
import org.ojalgo.function.multiary.PureQuadraticFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;

/**
 * 1/2 [x]<sup>T</sup>[Q][x] - [l]<sup>T</sup>[x]
 *
 * @author apete
 */
public final class ConvexObjectiveFunction<N extends Comparable<N>> implements MultiaryFunction.TwiceDifferentiable<N>, MultiaryFunction.Quadratic<N> {

    private final LinearFunction<N> myLinear;
    private final PureQuadraticFunction<N> myPureQuadratic;
    private final Scalar.Factory<N> myScalarFactory;

    ConvexObjectiveFunction(final PhysicalStore.Factory<N, ?> factory, final int nbVars) {
        this(factory.make(nbVars, nbVars), factory.make(nbVars, 1));
    }

    ConvexObjectiveFunction(final PhysicalStore<N> quadratic, final PhysicalStore<N> linear) {

        super();

        myScalarFactory = quadratic.physical().scalar();

        myPureQuadratic = PureQuadraticFunction.wrap(quadratic);
        myLinear = LinearFunction.wrap(linear);

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

    public MatrixStore<N> getGradient(final Access1D<N> point) {
        return myPureQuadratic.getGradient(point).subtract(myLinear.getGradient(point));
    }

    public MatrixStore<N> getHessian(final Access1D<N> point) {
        return myPureQuadratic.getHessian(point);
    }

    public MatrixStore<N> getLinearFactors(final boolean negated) {
        return myLinear.getLinearFactors(!negated);
    }

    @Override
    public N invoke(final Access1D<N> arg) {

        Scalar<N> zero = myScalarFactory.zero();
        Scalar<N> one = myScalarFactory.one();
        Scalar<N> two = one.add(one);

        Scalar<N> retVal = zero.add(myPureQuadratic.invoke(arg)).divide(two);
        retVal = retVal.subtract(myLinear.invoke(arg));
        retVal.add(this.getConstant());
        return retVal.get();
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

}
