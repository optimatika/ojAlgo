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
package org.ojalgo.optimisation.linear;

/**
 * A classic primal 2-phase simplex solver. In that sense it's similar to {@link SimplexTableauSolver}, but
 * it's implemented in terms of the more abstract {@link SimplexStore}.
 * <p>
 * The {@link PhasedSimplexSolver} is the primary sub-class of {@link SimplexSolver}. This sub-class is
 * essentially just a helper for testing (already had all the building blocks to implement it).
 *
 * @author apete
 */
final class ClassicSimplexSolver extends SimplexSolver {

    ClassicSimplexSolver(final Options solverOptions, final SimplexStore simplexStore) {
        super(solverOptions, simplexStore);
    }

    @Override
    public Result solve(final Result kickStarter) {

        //   this.initiatePhase1();

        IterDescr iteration = this.prepareToIterate();

        this.doPrimalIterations(iteration); // Phase-1

        this.switchToPhase2();

        this.doPrimalIterations(iteration); // Phase-2

        return this.extractResult();
    }

    @Override
    void setup(final SimplexStore simplex) {

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
            } else if (Math.abs(lb) > Math.abs(ub)) {
                simplex.upper(j);
                this.shift(j, ub, rc);
            } else {
                simplex.lower(j);
                this.shift(j, lb, rc);
            }
        }

        double[] lowerBounds = simplex.getLowerBounds();
        double[] upperBounds = simplex.getUpperBounds();

        int[] included = simplex.included;
        for (int ji = 0; ji < included.length; ji++) {
            int j = included[ji];

            if (j >= simplex.structure.countVariables()) {
                // Artificial
                lowerBounds[j] = Math.min(simplex.getCurrentRHS(ji), lowerBounds[j]);
                upperBounds[j] = Math.max(simplex.getCurrentRHS(ji), upperBounds[j]);
            } else {
                // TODO Check infeasibility?
            }
        }

        simplex.setupClassicPhase1Objective();

        simplex.switchToPhase2();
    }

}
