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
 * For solving [A][x]=[b] where [A] has non-zero elements on the diagonal.
 * <p>
 * It's most likely better to instead use {@link GaussSeidelSolver} or {@link ConjugateGradientSolver}.
 * <p>
 * When to use:
 * <ul>
 * <li>Simple baseline or educational purposes; easiest stationary method to reason about.
 * <li>When you need fully synchronous updates (no in-place coupling) or trivial parallelisation across rows.
 * <li>Diagonally dominant systems where convergence is acceptable and simplicity/parallelism is preferred.
 * <li>If sequential in-place updates are fine and you want faster convergence, prefer GaussSeidelSolver.
 * <li>For large SPD systems, prefer ConjugateGradientSolver for speed and robustness.
 * </ul>
 *
 * @author apete
 * @see https://en.wikipedia.org/wiki/Jacobi_method
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
