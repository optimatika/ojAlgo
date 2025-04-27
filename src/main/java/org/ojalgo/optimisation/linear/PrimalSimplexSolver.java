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
package org.ojalgo.optimisation.linear;

import org.ojalgo.optimisation.Optimisation;

/**
 * Requires the initial basis to be feasible (doesn't do a phase-1).
 *
 * @author apete
 */
final class PrimalSimplexSolver extends SimplexSolver {

    PrimalSimplexSolver(final Options solverOptions, final SimplexStore simplexStore) {
        super(solverOptions, simplexStore);
    }

    @Override
    public Result solve(final Result kickStarter) {

        IterDescr iteration = this.prepareToIterate();

        this.doPrimalIterations(iteration);

        if (this.getState().isOptimal() && !this.isPrimalFeasible()) {
            this.setState(Optimisation.State.FAILED);
        }

        return this.extractResult();
    }

    @Override
    void setup(final SimplexStore simplex) {

        simplex.removePhase1();

        int[] excluded = simplex.excluded;
        for (int je = 0, limit = excluded.length; je < limit; je++) {
            int j = excluded[je];

            double rc = simplex.getCost(j);
            double lb = simplex.getLowerBound(j);
            double ub = simplex.getUpperBound(j);

            if (lb > ub) {
                throw new IllegalStateException();
            }

            if (!Double.isFinite(lb) && !Double.isFinite(ub)) {
                simplex.unbounded(j);
            } else if (Math.abs(lb) <= Math.abs(ub)) {
                simplex.lower(j);
            } else if (Math.abs(lb) >= Math.abs(ub)) {
                simplex.upper(j);
            } else {
                simplex.lower(j);
            }
        }
    }

}
