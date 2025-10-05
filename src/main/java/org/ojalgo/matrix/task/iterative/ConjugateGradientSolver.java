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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.List;

import org.ojalgo.equation.Equation;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.type.context.NumberContext;

/**
 * For solving [A][x]=[b] when [A] is symmetric and positive-definite.
 * <p>
 * This implementation is Jacobi preconditioned by default – using the diagonal elements to scale the residual.
 * You can change that by calling {@link #setPreconditioner(Preconditioner)} with a different implementation.
 * <p>
 * When to use:
 * <ul>
 * <li>Large, sparse systems with symmetric positive-definite A.
 * <li>When fast convergence is desired compared to Jacobi/Gauss–Seidel for SPD problems.
 * <li>When only matrix–vector products with A are available (no need for A^T).
 * <li>Prefer over stationary methods as a general default for SPD systems, especially with reasonable
 * conditioning.
 * <li>Not suitable for nonsymmetric or indefinite systems (use QMR or a different Krylov method).
 * </ul>
 *
 * @author apete
 * @see https://en.wikipedia.org/wiki/Conjugate_gradient_method
 * @see https://optimization.cbe.cornell.edu/index.php?title=Conjugate_gradient_methods
 */
public final class ConjugateGradientSolver extends IterativeSolverTask {

    private transient R064Store myDirection = null; // p
    private transient R064Store myPreconditioned = null; // z = M^{-1} r
    private transient R064Store myResidual = null; // r
    private transient R064Store myVector = null; // q = A p

    public ConjugateGradientSolver() {
        super();
        this.setPreconditioner(new JacobiPreconditioner());
    }

    @Override
    public double resolve(final List<Equation> equations, final PhysicalStore<Double> solution) {

        if (this.isDebugPrinterSet()) {
            this.debug(0, NaN, solution);
        }

        int m = equations.size();
        int n = solution.size();

        int nbIterations = 0;
        int iterationsLimit = this.getIterationsLimit();

        NumberContext accuracy = this.getAccuracyContext();

        double normErr = POSITIVE_INFINITY;
        double normRHS = ZERO;

        R064Store residual = myResidual = IterativeSolverTask.worker(myResidual, n);
        R064Store direction = myDirection = IterativeSolverTask.worker(myDirection, n);
        R064Store preconditioned = myPreconditioned = IterativeSolverTask.worker(myPreconditioned, n);
        R064Store vector = myVector = IterativeSolverTask.worker(myVector, n);

        Preconditioner preconditioner = this.getPreconditioner();
        preconditioner.prepare(equations, n);

        double stepLength; // alpha
        double gradientCorrectionFactor; // beta

        double zr0 = 1;
        double zr1;
        double pAp0 = 0;

        // r0 = b - A x0  and accumulate ||b||
        for (int r = 0; r < m; r++) {
            Equation row = equations.get(r);
            double bi = row.getRHS();
            normRHS = HYPOT.invoke(normRHS, bi);
            double ri = bi - row.dot(solution);
            residual.set(row.index, ri);
        }
        // z0 = M^{-1} r0
        preconditioner.apply(residual, preconditioned);

        // p0 = z0
        direction.fillMatching(preconditioned);

        zr1 = preconditioned.dot(residual); // (r,z)

        do {

            zr0 = zr1;

            // vector = A * p
            for (int i = 0; i < m; i++) {
                Equation row = equations.get(i);
                vector.set(row.index, row.dot(direction));
            }

            pAp0 = direction.dot(vector);

            stepLength = zr0 / pAp0; // alpha

            if (!Double.isNaN(stepLength)) {
                // x = x + alpha * p
                direction.axpy(stepLength, solution);
                // r = r - alpha * A p
                vector.axpy(-stepLength, residual);
            }

            // Compute normErr = ||r|| and apply preconditioner: z = M^{-1} r
            normErr = ZERO;
            for (int r = 0; r < m; r++) {
                Equation row = equations.get(r);
                double ri = residual.doubleValue(row.index);
                normErr = HYPOT.invoke(normErr, ri);
            }
            preconditioner.apply(residual, preconditioned);

            zr1 = preconditioned.dot(residual); // (r_{k+1}, z_{k+1})
            gradientCorrectionFactor = zr1 / zr0; // beta

            // p = z + beta * p (done in-place: p = beta*p ; p += z)
            direction.modifyAll(MULTIPLY.second(gradientCorrectionFactor));
            direction.modifyMatching(ADD, preconditioned);

            nbIterations++;

            if (this.isDebugPrinterSet()) {
                this.debug(nbIterations, normErr / normRHS, solution);
            }

        } while (nbIterations < iterationsLimit && !Double.isNaN(normErr) && !accuracy.isSmall(normRHS, normErr));

        return accuracy.isZero(normRHS) ? normErr : normErr / normRHS;
    }

}