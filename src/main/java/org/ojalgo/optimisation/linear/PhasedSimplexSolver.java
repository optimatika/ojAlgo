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

import org.ojalgo.optimisation.linear.SimplexStore.ColumnState;
import org.ojalgo.structure.Mutate1D;

/**
 * First runs the dual algorithm (with a possibly modified objective function) to establish feasibility, and
 * then the primal to reach optimality.
 * <p>
 * This is the primary sub-class of {@link SimplexSolver} and the one you would typically use. All the other
 * sub-classes are primarily there to help with testing.
 *
 * @author apete
 */
final class PhasedSimplexSolver extends SimplexSolver {

    PhasedSimplexSolver(final Options solverOptions, final SimplexStore simplexStore) {
        super(solverOptions, simplexStore);
    }

    @Override
    public Result solve(final Result kickStarter) {

        IterDescr iteration = this.prepareToIterate();

        this.doDualIterations(iteration); // Phase-1

        this.switchToPhase2();

        this.doPrimalIterations(iteration); // Phase-2

        return this.extractResult();
    }

    @Override
    void setup(final SimplexStore simplex) {

        Mutate1D phase1 = simplex.phase1();

        int[] excluded = simplex.excluded;
        for (int je = 0, limit = excluded.length; je < limit; je++) {
            int j = excluded[je];

            ColumnState state = simplex.getColumnState(j);
            double rc = simplex.getCost(j);
            double lb = simplex.getLowerBound(j);
            double ub = simplex.getUpperBound(j);

            if (state == ColumnState.LOWER) {
                if (rc > ZERO) {
                    this.shift(j, lb, rc);
                    phase1.set(j, rc);
                } else {
                    this.shift(j, lb, rc);
                    phase1.set(j, Math.max(ONE, simplex.getReducedCost(je)));
                }
            } else if (state == ColumnState.UPPER) {
                if (rc < ZERO) {
                    this.shift(j, ub, rc);
                    phase1.set(j, rc);
                } else {
                    this.shift(j, ub, rc);
                    phase1.set(j, Math.min(NEG, simplex.getReducedCost(je)));
                }

            } else {

                phase1.set(j, ZERO);
            }

        }
    }

}
