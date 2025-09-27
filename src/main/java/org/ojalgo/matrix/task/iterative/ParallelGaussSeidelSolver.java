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
import java.util.concurrent.atomic.AtomicInteger;

import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.concurrent.DivideAndConquer.Conquerer;
import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.concurrent.ProcessingService;
import org.ojalgo.equation.Equation;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.type.context.NumberContext;

/**
 * Experimental parallelised version of {@link GaussSeidelSolver}.
 * <p>
 * When to use:
 * <ul>
 * <li>Diagonally dominant or SPD systems where Gauss–Seidel is appropriate but you want to utilise multiple
 * cores.
 * <li>When you can tolerate slight differences from strictly sequential updates and want higher throughput.
 * <li>As a smoother/pre-relaxation where approximate iterations are acceptable.
 * <li>For tough SPD systems where convergence speed and robustness matter more than parallelism, prefer
 * ConjugateGradientSolver.
 * <li>For fully synchronous updates (and trivial parallelism), prefer Jacobi.
 * </ul>
 *
 * @author apete
 */
public final class ParallelGaussSeidelSolver extends IterativeSolverTask {

    private static final DivideAndConquer.Divider DIVIDER = ProcessingService.INSTANCE.newDivider().parallelism(Parallelism.CORES).threshold(128);

    private static void divide(final int nbEquations, final Conquerer conquerer) {
        DIVIDER.divide(0, nbEquations, conquerer);
    }

    public ParallelGaussSeidelSolver() {
        super();
    }

    @Override
    public double resolve(final List<Equation> equations, final PhysicalStore<Double> solution) {

        if (this.isDebugPrinterSet()) {
            this.debug(0, NaN, solution);
        }

        int m = equations.size();

        AtomicInteger iterationsCounter = new AtomicInteger();

        double tmpNorm = ZERO;
        for (int r = 0; r < m; r++) {
            tmpNorm = HYPOT.invoke(tmpNorm, equations.get(r).getRHS());
        }
        double normRHS = tmpNorm;

        ParallelGaussSeidelSolver.divide(m, (first, limit) -> this.resolve(equations, solution, normRHS, iterationsCounter, first, limit));

        return this.resolve(equations, solution, normRHS, iterationsCounter, 0, m);
    }

    private double resolve(final List<Equation> equations, final PhysicalStore<Double> solution, final double normRHS, final AtomicInteger iterationsCounter,
            final int first, final int limit) {

        int iterationsLimit = this.getIterationsLimit();

        NumberContext accuracy = this.getAccuracyContext();

        double relaxation = this.getRelaxationFactor();

        double normErr = POSITIVE_INFINITY;

        do {

            normErr = ZERO;

            for (int r = first; r < limit; r++) {
                normErr = HYPOT.invoke(normErr, equations.get(r).adjust(solution, relaxation));
            }

            iterationsCounter.incrementAndGet();

            if (this.isDebugPrinterSet()) {
                this.debug(iterationsCounter.intValue(), normErr / normRHS, solution);
            }

        } while (iterationsCounter.intValue() < iterationsLimit && !Double.isNaN(normErr) && !accuracy.isSmall(normRHS, normErr));

        return accuracy.isZero(normRHS) ? normErr : normErr / normRHS;
    }

}