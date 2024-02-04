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
package org.ojalgo.matrix.task.iterative;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.concurrent.DivideAndConquer.Conquerer;
import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.concurrent.ProcessingService;
import org.ojalgo.equation.Equation;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.structure.Access2D;
import org.ojalgo.type.context.NumberContext;

/**
 * Experimental parallelised version of {@link GaussSeidelSolver}.
 *
 * @author apete
 */
public final class ParallelGaussSeidelSolver extends StationaryIterativeSolver implements IterativeSolverTask.SparseDelegate {

    private static final DivideAndConquer.Divider DIVIDER = ProcessingService.INSTANCE.divider();
    private static final IntSupplier PARALLELISM = Parallelism.CORES;
    private static final int THRESHOLD = 128;

    private static void divide(final int nbEquations, final Conquerer conquerer) {
        DIVIDER.parallelism(PARALLELISM).threshold(THRESHOLD).divide(0, nbEquations, conquerer);
    }

    public ParallelGaussSeidelSolver() {
        super();
    }

    public double resolve(final List<Equation> equations, final PhysicalStore<Double> solution) {

        int nbEquations = equations.size();

        double tmpNorm = ZERO;
        for (int r = 0; r < nbEquations; r++) {
            tmpNorm = HYPOT.invoke(tmpNorm, equations.get(r).getRHS());
        }
        double normRHS = tmpNorm;

        AtomicInteger iterationsCounter = new AtomicInteger();

        ParallelGaussSeidelSolver.divide(nbEquations, (first, last) -> this.resolve(equations, solution, normRHS, iterationsCounter, first, last));

        return this.resolve(equations, solution, normRHS, iterationsCounter, 0, nbEquations);
    }

    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> current) throws RecoverableCondition {

        List<Equation> equations = IterativeSolverTask.toListOfRows(body, rhs);

        this.resolve(equations, current);

        return current;
    }

    private double resolve(final List<Equation> equations, final PhysicalStore<Double> solution, final double normRHS, final AtomicInteger iterationsCounter,
            final int first, final int last) {

        int iterationsLimit = this.getIterationsLimit();
        NumberContext accuracy = this.getAccuracyContext();
        double relaxationFactor = this.getRelaxationFactor();

        double normErr = POSITIVE_INFINITY;

        do {

            normErr = ZERO;

            for (int r = first; r < last; r++) {
                normErr = HYPOT.invoke(normErr, equations.get(r).adjust(solution, relaxationFactor));
            }

            iterationsCounter.incrementAndGet();

            if (this.isDebugPrinterSet()) {
                this.debug(iterationsCounter.intValue(), normErr / normRHS, solution);
            }

        } while ((iterationsCounter.intValue() < iterationsLimit) && !accuracy.isSmall(normRHS, normErr));

        return normErr / normRHS;
    }

}
