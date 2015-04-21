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

import java.util.function.Function;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.BasicFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;

public interface MultiaryFunction<N extends Number> extends BasicFunction<N>, Function<Access1D<N>, N> {

    public static interface Constant<N extends Number, F extends Constant<N, ?>> extends MultiaryFunction<N> {

        F constant(Number constant);

        N getConstant();

        void setConstant(Number constant);

    }

    public static interface Convex<N extends Number> extends MultiaryFunction<N> {

    }

    public static interface Linear<N extends Number> extends MultiaryFunction<N> {

        PhysicalStore<N> linear();

    }

    public static interface Quadratic<N extends Number> extends MultiaryFunction<N> {

        PhysicalStore<N> quadratic();

    }

    /**
     * Twice (Continuously) Differentiable Multiary Function
     *
     * @author apete
     */
    public static interface TwiceDifferentiable<N extends Number> extends MultiaryFunction<N> {

        /**
         * The gradient of a scalar field is a vector field that points in the direction of the greatest rate
         * of increase of the scalar field, and whose magnitude is that rate of increase. In simple terms, the
         * variation in space of any quantity can be represented (e.g. graphically) by a slope. The gradient
         * represents the steepness and direction of that slope. The Jacobian is a generalization of the
         * gradient. Gradients are only defined on scalar-valued functions, but Jacobians are defined on
         * vector- valued functions. When f is real-valued (i.e., f : Rn → R) the derivative Df(x) is a 1 × n
         * matrix, i.e., it is a row vector. Its transpose is called the gradient of the function: ∇f(x) =
         * Df(x)<sup>T</sup> , which is a (column) vector, i.e., in Rn. Its components are the partial
         * derivatives of f: The first-order approximation of f at a point x ∈ int dom f can be expressed as
         * (the affine function of z) f(x) + ∇f(x)T (z − x).
         */
        MatrixStore<N> getGradient(Access1D<N> arg);

        /**
         * The Hessian matrix or Hessian is a square matrix of second-order partial derivatives of a function.
         * It describes the local curvature of a function of many variables. The Hessian is the Jacobian of
         * the gradient. The second-order approximation of f, at or near x, is the quadratic function of z
         * defined by f(z) = f(x) + ∇f(x)T (z − x) + (1/2)(z − x)T ∇2f(x)(z − x)
         */
        MatrixStore<N> getHessian(Access1D<N> arg);

        FirstOrderApproximation<N> toFirstOrderApproximation(final Access1D<N> point);

        SecondOrderApproximation<N> toSecondOrderApproximation(final Access1D<N> point);

    }

    default N apply(final Access1D<N> arg) {
        return this.invoke(arg);
    }

    int arity();

    N invoke(Access1D<N> arg);

}
