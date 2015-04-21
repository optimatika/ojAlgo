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
import org.ojalgo.scalar.Scalar;

abstract class AbstractMultiary<N extends Number, F extends AbstractMultiary<N, ?>> implements MultiaryFunction<N>, MultiaryFunction.Constant<N, F>,
        MultiaryFunction.TwiceDifferentiable<N> {

    private Scalar<N> myConstant = null;

    protected AbstractMultiary() {
        super();
    }

    @SuppressWarnings("unchecked")
    public final F constant(final Number constant) {
        this.setConstant(constant);
        return (F) this;
    }

    public final N getConstant() {
        return this.getScalarConstant().getNumber();
    }

    public final FirstOrderApproximation<N> toFirstOrderApproximation(final Access1D<N> arg) {
        return new FirstOrderApproximation<N>(this, arg);
    }

    public final SecondOrderApproximation<N> toSecondOrderApproximation(final Access1D<N> arg) {
        return new SecondOrderApproximation<N>(this, arg);
    }

    public final void setConstant(final Number constant) {
        myConstant = constant != null ? this.factory().scalar().convert(constant) : null;
    }

    protected abstract PhysicalStore.Factory<N, ?> factory();

    protected final Scalar<N> getScalarConstant() {
        return myConstant != null ? myConstant : this.factory().scalar().zero();
    }

}
