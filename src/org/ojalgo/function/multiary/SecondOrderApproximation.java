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

import org.ojalgo.access.Access1D;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;

public final class SecondOrderApproximation<N extends Number> extends ApproximateFunction<N> {

    private final CompoundFunction<N> myDelegate;

    public SecondOrderApproximation(final MultiaryFunction.TwiceDifferentiable<N> function, final Access1D<N> point) {

        super(function, point);

        final PhysicalStore<N> tmpHessian = function.getHessian(point).copy();
        final MatrixStore<N> tmpGradient = function.getGradient(point).builder().transpose().build();

        tmpHessian.modifyAll(tmpHessian.factory().function().multiply().first(0.5));

        final QuadraticFunction<N> tmpQuadratic = new QuadraticFunction<N>(tmpHessian);
        final LinearFunction<N> tmpLinear = new LinearFunction<N>(tmpGradient);

        myDelegate = new CompoundFunction<N>(tmpQuadratic, tmpLinear);
        myDelegate.setConstant(function.invoke(point));
    }

    public int arity() {
        return myDelegate.arity();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof SecondOrderApproximation)) {
            return false;
        }
        final SecondOrderApproximation<?> other = (SecondOrderApproximation<?>) obj;
        if (myDelegate == null) {
            if (other.myDelegate != null) {
                return false;
            }
        } else if (!myDelegate.equals(other.myDelegate)) {
            return false;
        }
        return true;
    }

    public MatrixStore<N> getGradient(final Access1D<N> arg) {
        return myDelegate.getGradient(this.shift(arg));
    }

    public MatrixStore<N> getHessian(final Access1D<N> arg) {
        return myDelegate.getHessian(null);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = (prime * result) + ((myDelegate == null) ? 0 : myDelegate.hashCode());
        return result;
    }

    public N invoke(final Access1D<N> arg) {
        return myDelegate.invoke(this.shift(arg));
    }

    @Override
    public String toString() {
        return myDelegate.toString();
    }

    @Override
    protected Factory<N, ?> factory() {
        return myDelegate.factory();
    }

}
