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
 * Stationary Jacobi iteration for solving [A][x]=[b] with non-zero diagonal entries.
 * <p>
 * Convergence
 * <ul>
 * <li>Converges for strictly diagonally dominant systems and for SPD matrices under suitable conditions.
 * <li>Updates are fully synchronous (per-iteration), making it straightforward to parallelise across rows.
 * </ul>
 * Configuration
 * <ul>
 * <li>Ignores any configured {@link Preconditioner}; use the relaxation factor to control convergence speed.
 * </ul>
 * When to use
 * <ul>
 * <li>As a simple baseline or where synchronous updates and trivial parallelism are desirable.
 * <li>When sequential in-place coupling is acceptable and faster convergence is needed, an in-place
 * stationary method may be preferable.
 * <li>For large SPD problems needing faster/robuster convergence, Krylov methods often perform better.
 * </ul>
 */
public final class JacobiSolver extends IterativeSolverTask {

    private transient R064Store myIncrement = null;

    public JacobiSolver() {
        super();
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

        // Compute ||b|| once outside the loop
        double normRHS = ZERO;
        for (int r = 0; r < m; r++) {
            normRHS = HYPOT.invoke(normRHS, equations.get(r).getRHS());
        }

        // Scratch vector for simultaneous Jacobi updates (delta x)
        R064Store increment = myIncrement = IterativeSolverTask.worker(myIncrement, n);

        double relaxation = this.getRelaxationFactor();

        double normErr = POSITIVE_INFINITY;

        do {

            normErr = ZERO;

            // Build residual and increments using "old" solution values
            for (int r = 0; r < m; r++) {
                Equation row = equations.get(r);
                double ri = row.getRHS() - row.dot(solution); // residual component
                normErr = HYPOT.invoke(normErr, ri);
                // Jacobi increment: delta_i = r_i / a_ii
                double pivot = row.getPivot();
                increment.set(row.index, ri / pivot);
            }

            // Optional relaxation: x += omega * D^{-1} * r
            increment.axpy(relaxation, solution);

            nbIterations++;

            if (this.isDebugPrinterSet()) {
                this.debug(nbIterations, normErr / normRHS, solution);
            }

        } while (nbIterations < iterationsLimit && !Double.isNaN(normErr) && !accuracy.isSmall(normRHS, normErr));

        return accuracy.isZero(normRHS) ? normErr : normErr / normRHS;
    }

}