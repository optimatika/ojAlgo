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
package org.ojalgo.matrix.task.iterative;

import java.util.List;
import java.util.function.Supplier;

import org.ojalgo.equation.Equation;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.structure.Access1D;

/**
 * Pluggable preconditioner for iterative linear system solvers.
 * <p>
 * Contract:
 * <ul>
 * <li>Call {@link #prepare(List, int)} once per system before iterations to (re)initialise internal state.
 * <li>{@link #apply(Access1D, PhysicalStore)} should approximate {@code M^{-1}} applied to a vector. Solvers
 * that use left-preconditioning will apply this to residuals or intermediate vectors.
 * <li>{@link #applyTranspose(Access1D, PhysicalStore)} should approximate {@code (M^T)^{-1}}. By default it
 * delegates to {@link #apply(Access1D, PhysicalStore)}; override if the preconditioner is not symmetric.
 * </ul>
 * Preconditioning modes (solver perspective):
 * <ul>
 * <li>Left preconditioning: solver forms {@code M^{-1} A x = M^{-1} b} and calls {@link #apply} on vectors.
 * <li>Right preconditioning: solver forms {@code A M^{-1} y = b}, then recovers {@code x = M^{-1} y}; may
 * require both {@link #apply} and {@link #applyTranspose}.
 * <li>Symmetric preconditioners ({@code M = M^T}): implementations can usually provide only
 * {@link #apply(Access1D, PhysicalStore)} and rely on the default transpose behaviour.
 * </ul>
 * Compatibility guidelines:
 * <ul>
 * <li>Methods requiring symmetric positive-definite preconditioning (e.g., for SPD systems) expect {@code M}
 * to be symmetric positive-definite.
 * <li>Methods for general nonsymmetric systems that use right-preconditioning may require a meaningful
 * transpose action; override {@link #applyTranspose(Access1D, PhysicalStore)} when {@code M} is not
 * symmetric.
 * <li>Some stationary (fixed-point) methods ignore preconditioners entirely and instead use a relaxation
 * factor.
 * </ul>
 */
public interface Preconditioner {

    /**
     * A no-op preconditioner.
     */
    Preconditioner IDENTITY = new IdentityPreconditioner();

    /**
     * Returns a factory method for a Symmetric Successive Over-Relaxation (SSOR) preconditioner with the
     * specified relaxation factor.
     */
    static Supplier<Preconditioner> getSSOR(final double omega) {
        return () -> new SSORPreconditioner().omega(omega);
    }

    /**
     * An identity (no-op) preconditioner.
     */
    static Preconditioner newIdentity() {
        return IDENTITY;
    }

    /**
     * A Jacobi (diagonal) preconditioner.
     */
    static Preconditioner newJacobi() {
        return new JacobiPreconditioner();
    }

    /**
     * A Symmetric Successive Over-Relaxation (SSOR) preconditioner with a specified relaxation factor
     */
    static Preconditioner newSSOR(final double omega) {
        return new SSORPreconditioner().omega(omega);
    }

    /**
     * A symmetric Gauss-Seidel preconditioner (SSOR with omega=1).
     */
    static Preconditioner newSymmetricGaussSeidel() {
        return new SSORPreconditioner();
    }

    /**
     * Apply M^{-1} to a vector. src and dst may alias.
     */
    void apply(Access1D<Double> src, PhysicalStore<Double> dst);

    /**
     * Apply (M^T)^{-1} to a vector. Defaults to {@link #apply(Access1D, PhysicalStore)}.
     */
    default void applyTranspose(final Access1D<Double> src, final PhysicalStore<Double> dst) {
        this.apply(src, dst);
    }

    /**
     * Prepare internal structures for a specific system. Implementations may analyse sparsity or extract
     * diagonals/factors here.
     *
     * @param equations The active set of rows constituting the system body.
     * @param dimension The vector dimension (number of variables / size of solution vector).
     */
    void prepare(List<Equation> equations, int dimension);

}