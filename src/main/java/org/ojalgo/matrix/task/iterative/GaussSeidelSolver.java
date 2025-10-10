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
import org.ojalgo.type.context.NumberContext;

/**
 * Stationary Gauss–Seidel iteration for solving [A][x]=[b] with non-zero diagonal entries.
 * <p>
 * Convergence
 * <ul>
 * <li>Converges for strictly diagonally dominant systems and for symmetric positive-definite (SPD) matrices.
 * <li>Behaviour depends on ordering and scaling; preconditioning is not applied in this stationary method.
 * </ul>
 * Configuration
 * <ul>
 * <li>Ignores any configured {@link Preconditioner}; use the relaxation factor to control convergence speed.
 * </ul>
 * <p>
 * When to use
 * <ul>
 * <li>As a simple in-place fixed-point iteration when sequential updates are acceptable.
 * <li>Prefer over fully synchronous updates when in-place coupling improves convergence.
 * <li>For large SPD problems needing faster convergence, Krylov methods often perform better.
 * <li>If fully synchronous updates or trivial parallelism are required, consider a synchronous fixed-point
 * method.
 * </ul>
 */
public final class GaussSeidelSolver extends IterativeSolverTask {

    public GaussSeidelSolver() {
        super();
    }

    @Override
    public double resolve(final List<Equation> equations, final PhysicalStore<Double> solution) {

        if (this.isDebugPrinterSet()) {
            this.debug(0, NaN, solution);
        }

        int m = equations.size();

        int nbIterations = 0;
        int iterationsLimit = this.getIterationsLimit();

        NumberContext accuracy = this.getAccuracyContext();

        double normErr = POSITIVE_INFINITY;
        double normRHS = ZERO;

        for (int r = 0; r < m; r++) {
            normRHS = HYPOT.invoke(normRHS, equations.get(r).getRHS());
        }

        double relaxationFactor = this.getRelaxationFactor();

        do {

            normErr = ZERO;

            for (int r = 0; r < m; r++) {
                normErr = HYPOT.invoke(normErr, equations.get(r).adjust(solution, relaxationFactor));
            }

            nbIterations++;

            if (this.isDebugPrinterSet()) {
                this.debug(nbIterations, normErr / normRHS, solution);
            }

        } while (nbIterations < iterationsLimit && !Double.isNaN(normErr) && !accuracy.isSmall(normRHS, normErr));

        return accuracy.isZero(normRHS) ? normErr : normErr / normRHS;
    }

}