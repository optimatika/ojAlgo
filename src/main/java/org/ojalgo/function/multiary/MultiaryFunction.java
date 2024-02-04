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
package org.ojalgo.function.multiary;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.BasicFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.structure.Access1D;

public interface MultiaryFunction<N extends Comparable<N>> extends BasicFunction.PlainUnary<Access1D<N>, N> {

    public interface Affine<N extends Comparable<N>> extends Linear<N>, Constant<N> {

    }

    public interface Constant<N extends Comparable<N>> extends MultiaryFunction<N> {

        N getConstant();

        void setConstant(Comparable<?> constant);

    }

    public interface Convex<N extends Comparable<N>> extends MultiaryFunction<N> {

    }

    public interface Linear<N extends Comparable<N>> extends MultiaryFunction<N> {

        PhysicalStore<N> linear();

    }

    public interface PureQuadratic<N extends Comparable<N>> extends Constant<N> {

        PhysicalStore<N> quadratic();

    }

    public interface Quadratic<N extends Comparable<N>> extends PureQuadratic<N>, Linear<N> {

    }

    /**
     * Twice (Continuously) Differentiable Multiary Function
     *
     * @author apete
     */
    public interface TwiceDifferentiable<N extends Comparable<N>> extends MultiaryFunction<N> {

        /**
         * <p>
         * The gradient of a scalar field is a vector field that points in the direction of the greatest rate
         * of increase of the scalar field, and whose magnitude is that rate of increase.
         * </p>
         * <p>
         * The Jacobian is a generalization of the gradient. Gradients are only defined on scalar-valued
         * functions, but Jacobians are defined on vector- valued functions. When f is real-valued (i.e., f :
         * Rn → R) the derivative Df(x) is a 1 × n matrix, i.e., it is a row vector. Its transpose is called
         * the gradient of the function: ∇f(x) = Df(x)<sup>T</sup> , which is a (column) vector, i.e., in Rn.
         * Its components are the partial derivatives of f:
         * </p>
         * <p>
         * The first-order approximation of f at a point x ∈ int dom f can be expressed as (the affine
         * function of z) f(z) = f(x) + ∇f(x)T (z − x).
         * </p>
         */
        MatrixStore<N> getGradient(Access1D<N> point);

        /**
         * <p>
         * The Hessian matrix or Hessian is a square matrix of second-order partial derivatives of a function.
         * It describes the local curvature of a function of many variables. The Hessian is the Jacobian of
         * the gradient.
         * </p>
         * <p>
         * The second-order approximation of f, at or near x, is the quadratic function of z defined by f(z) =
         * f(x) + ∇f(x)T (z − x) + (1/2)(z − x)T ∇2f(x)(z − x)
         * </p>
         */
        MatrixStore<N> getHessian(Access1D<N> point);

        /**
         * @return The gradient at origin (0-vector), negated or not
         * @deprecated v53 Use {@link #getLinearFactors(boolean)} instead.
         */
        @Deprecated
        default MatrixStore<N> getLinearFactors() {
            return this.getLinearFactors(false);
        }

        /**
         * @return The gradient at origin (0-vector), negated or not
         */
        MatrixStore<N> getLinearFactors(boolean negated);

        default MultiaryFunction.TwiceDifferentiable<N> toFirstOrderApproximation(final Access1D<N> arg) {
            return new FirstOrderApproximation<>(this, arg);
        }

        default MultiaryFunction.TwiceDifferentiable<N> toSecondOrderApproximation(final Access1D<N> arg) {
            return new SecondOrderApproximation<>(this, arg);
        }

    }

    default MultiaryFunction<N> andThen(final UnaryFunction<N> after) {
        ProgrammingError.throwIfNull(after);
        return new MultiaryFunction<>() {

            public int arity() {
                return MultiaryFunction.this.arity();
            }

            public N invoke(final Access1D<N> arg) {
                return after.invoke(MultiaryFunction.this.invoke(arg));
            }

        };
    }

    int arity();

    N invoke(Access1D<N> arg);

}
