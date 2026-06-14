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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.structure.Mutate1D;

/**
 * Two-phase revised simplex. Phase 1 constructs a modified objective that makes the starting point dual
 * feasible and then runs dual simplex iterations to achieve primal feasibility. Phase 2 restores the original
 * objective and runs primal simplex iterations (maintaining primal feasibility) to reach optimality.
 * <p>
 * This is the most general {@link SimplexSolver} subclass and the one normally instantiated by
 * {@link LinearSolver.Builder} and the {@link LinearSolver.ModelIntegration}. It handles explicit finite
 * lower and upper bounds on variables natively and does not require the initial basis to be feasible.
 * <p>
 * The other subclasses ({@link DualSimplexSolver}, {@link PrimalSimplexSolver}) run only one of the two
 * phases and impose stronger preconditions; they exist primarily for testing and specialised use cases.
 */
final class PhasedSimplexSolver extends SimplexSolver {

    PhasedSimplexSolver(final Options solverOptions, final SimplexStore simplexStore) {
        super(solverOptions, simplexStore);
    }

    @Override
    public Result solve(final Result kickStarter) {

        if (state == State.APPROXIMATE) {
            // Warm start after variable range update

            IterDescr iteration = this.prepareToIterate(false);

            this.doDualIterations(iteration, false);

            if (state.isFeasible() && this.isDualFeasible()) {
                state = State.OPTIMAL;
            }

            return this.extractResult();

        } else {
            // Normal cold start optimisation

            IterDescr iteration = this.prepareToIterate(true);

            this.doDualIterations(iteration, true);

            this.switchToPhase2();

            this.doPrimalIterations(iteration, true);

            return this.extractResult();
        }
    }

    @Override
    void setup(final SimplexStore simplex) {

        Mutate1D phase1 = simplex.phase1();

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
                phase1.set(j, rc);
            } else if (rc < ZERO && Double.isFinite(ub)) {
                simplex.setToUpper(j);
                phase1.set(j, rc);
            } else if (!Double.isFinite(lb) && !Double.isFinite(ub)) {
                simplex.unbounded(j);
                phase1.set(j, ZERO);
            } else if (Math.abs(lb) <= Math.abs(ub)) {
                simplex.setToLower(j);
                phase1.set(j, Math.max(ONE, simplex.getReducedCost(je)));
            } else if (Math.abs(lb) >= Math.abs(ub)) {
                simplex.setToUpper(j);
                phase1.set(j, Math.min(NEG, simplex.getReducedCost(je)));
            } else {
                simplex.lower(j);
                phase1.set(j, ONE);
            }
        }
    }

}
