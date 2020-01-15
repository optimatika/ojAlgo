/*
 * Copyright 1997-2020 Optimatika
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
 * Constant valued function - always returns the same value.
 *
 * @author apete
 */
public final class ConstantFunction<N extends Comparable<N>> implements MultiaryFunction.TwiceDifferentiable<N>, MultiaryFunction.Constant<N> {

    public static ConstantFunction<ComplexNumber> makeComplex(final int arity) {
        return new ConstantFunction<>(arity, GenericStore.COMPLEX);
    }

    public static ConstantFunction<ComplexNumber> makeComplex(final int arity, final Comparable<?> constant) {
        return new ConstantFunction<>(arity, GenericStore.COMPLEX, constant);
    }

    public static ConstantFunction<Double> makePrimitive(final int arity) {
        return new ConstantFunction<>(arity, Primitive64Store.FACTORY);
    }

    public static ConstantFunction<Double> makePrimitive(final int arity, final Comparable<?> constant) {
        return new ConstantFunction<>(arity, Primitive64Store.FACTORY, constant);
    }

    public static ConstantFunction<RationalNumber> makeRational(final int arity) {
        return new ConstantFunction<>(arity, GenericStore.RATIONAL);
    }

    public static ConstantFunction<RationalNumber> makeRational(final int arity, final Comparable<?> constant) {
        return new ConstantFunction<>(arity, GenericStore.RATIONAL, constant);
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

    public int arity() {
        return myArity;
    }

    public N getConstant() {
        return this.getScalarConstant().get();
    }

    public MatrixStore<N> getGradient(final Access1D<N> point) {
        return this.getLinearFactors();
    }

    public MatrixStore<N> getHessian(final Access1D<N> point) {
        return myFactory.builder().makeZero(myArity, myArity).get();
    }

    public MatrixStore<N> getLinearFactors() {
        return myFactory.builder().makeZero(myArity, 1).get();
    }

    public N invoke(final Access1D<N> arg) {
        return this.getConstant();
    }

    public void setConstant(final Comparable<?> constant) {
        myConstant = constant != null ? myFactory.scalar().convert(constant) : null;
    }

    Factory<N, ?> factory() {
        return myFactory;
    }

    Scalar<N> getScalarConstant() {
        return myConstant != null ? myConstant : myFactory.scalar().zero();
    }

}
