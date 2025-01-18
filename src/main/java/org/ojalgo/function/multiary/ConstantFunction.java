/*
 * Copyright 1997-2025 Optimatika
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

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;

/**
 * Constant valued function - always returns the same value.
 *
 * @author apete
 */
public final class ConstantFunction<N extends Comparable<N>> implements MultiaryFunction.TwiceDifferentiable<N>, MultiaryFunction.Constant<N> {

    public static final class Factory<N extends Comparable<N>> {

        private Comparable<?> myConstant = null;
        private final PhysicalStore.Factory<N, ?> myFactory;

        Factory(final PhysicalStore.Factory<N, ?> factory) {
            super();
            myFactory = factory;
        }

        public Factory<N> constant(final Comparable<?> constant) {
            myConstant = constant;
            return this;
        }

        public ConstantFunction<N> make(final int arity) {
            return new ConstantFunction<>(arity, myFactory, myConstant);
        }

    }

    public static <N extends Comparable<N>> Factory<N> factory(final PhysicalStore.Factory<N, ?> factory) {
        return new Factory<>(factory);
    }

    private final int myArity;
    private Scalar<N> myConstant = null;
    private final PhysicalStore.Factory<N, ?> myFactory;

    ConstantFunction(final int arity, final PhysicalStore.Factory<N, ?> factory, final Comparable<?> constant) {

        this(arity, factory);

        this.setConstant(constant);
    }

    ConstantFunction(final long arity, final PhysicalStore.Factory<N, ?> factory) {

        super();

        myArity = Math.toIntExact(arity);
        myFactory = factory;
    }

    @Override
    public int arity() {
        return myArity;
    }

    @Override
    public N getConstant() {
        return this.getScalarConstant().get();
    }

    @Override
    public MatrixStore<N> getGradient(final Access1D<N> point) {
        return this.getLinearFactors(false);
    }

    @Override
    public MatrixStore<N> getHessian(final Access1D<N> point) {
        return myFactory.makeZero(myArity, myArity);
    }

    @Override
    public MatrixStore<N> getLinearFactors(final boolean negated) {
        return myFactory.makeZero(myArity, 1);
    }

    @Override
    public N invoke(final Access1D<N> arg) {
        return this.getConstant();
    }

    @Override
    public void setConstant(final Comparable<?> constant) {
        myConstant = constant != null ? myFactory.scalar().convert(constant) : null;
    }

    PhysicalStore.Factory<N, ?> factory() {
        return myFactory;
    }

    Scalar<N> getScalarConstant() {
        return myConstant != null ? myConstant : myFactory.scalar().zero();
    }

}
