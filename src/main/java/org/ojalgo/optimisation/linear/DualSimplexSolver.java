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

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import org.ojalgo.optimisation.Optimisation;

/**
 * Pure dual simplex — runs only dual iterations (no phase-1/phase-2 switching).
 * <p>
 * Requires all variables to have both finite lower and upper bounds so that the initial basis is dual
 * feasible. If the dual iterations reach primal feasibility the solution is optimal; otherwise the problem is
 * infeasible.
 * <p>
 * Exists primarily for testing and specialised scenarios. For general use prefer {@link PhasedSimplexSolver}.
 */
final class DualSimplexSolver extends SimplexSolver {

    DualSimplexSolver(final Options solverOptions, final SimplexStore simplexStore) {
        super(solverOptions, simplexStore);
    }

    @Override
    public Result solve(final Result kickStarter) {

        IterDescr iteration = this.prepareToIterate(!warm);

        this.doDualIterations(iteration, true);

        if (state.isFeasible() && this.isDualFeasible()) {
            state = Optimisation.State.OPTIMAL;
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

            if (rc > ZERO && Double.isFinite(lb)) {
                simplex.setToLower(j);
            } else if (rc < ZERO && Double.isFinite(ub)) {
                simplex.setToUpper(j);
            } else if (!Double.isFinite(lb) && !Double.isFinite(ub)) {
                simplex.unbounded(j);
            } else if (Math.abs(lb) <= Math.abs(ub)) {
                simplex.setToLower(j);
            } else if (Math.abs(lb) >= Math.abs(ub)) {
                simplex.setToUpper(j);
            } else {
                simplex.lower(j);
            }
        }
    }

}
