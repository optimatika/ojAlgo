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
import org.ojalgo.matrix.store.PhysicalStore;

abstract class ApproximateFunction<N extends Number> implements MultiaryFunction<N>, MultiaryFunction.TwiceDifferentiable<N> {

    private final Access1D<N> myPoint;

    protected ApproximateFunction(final MultiaryFunction.TwiceDifferentiable<N> function, final Access1D<N> point) {

        super();

        myPoint = point;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ApproximateFunction)) {
            return false;
        }
        final ApproximateFunction<?> other = (ApproximateFunction<?>) obj;
        if (myPoint == null) {
            if (other.myPoint != null) {
                return false;
            }
        } else if (!myPoint.equals(other.myPoint)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((myPoint == null) ? 0 : myPoint.hashCode());
        return result;
    }

    public final FirstOrderApproximation<N> toFirstOrderApproximation(final Access1D<N> arg) {
        return new FirstOrderApproximation<N>(this, arg);
    }

    public final SecondOrderApproximation<N> toSecondOrderApproximation(final Access1D<N> arg) {
        return new SecondOrderApproximation<N>(this, arg);
    }

    protected abstract PhysicalStore.Factory<N, ?> factory();

    protected PhysicalStore<N> shift(final Access1D<?> arg) {
        final PhysicalStore<N> retVal = this.factory().columns(arg);
        retVal.fillMatching(retVal, this.factory().function().subtract(), myPoint);
        return retVal;
    }

}
