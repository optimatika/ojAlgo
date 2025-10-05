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
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.structure.Access1D;

/**
 * Pluggable preconditioner for iterative linear system solvers.
 * <p>
 * Contract:
 * <ul>
 * <li>Call {@link #prepare(List, int)} once per system before iterations to (re)initialise internal state.
 * <li>{@link #apply(Access1D, PhysicalStore)} applies an approximation of {@code M^{-1}} to a vector. For
 * left-preconditioned methods this is used on residuals and matrix-vector products.
 * <li>{@link #applyTranspose(Access1D, PhysicalStore)} defaults to the same as
 * {@link #apply(Access1D, PhysicalStore)}; override if a distinct transpose action is required.
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
     * Apply (M^T)^{-1} to a vector. Defaults to {@link #apply(Access1D, R064Store)}.
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