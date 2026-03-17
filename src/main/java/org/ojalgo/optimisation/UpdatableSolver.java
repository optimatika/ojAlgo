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
package org.ojalgo.optimisation;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.ojalgo.equation.Equation;
import org.ojalgo.optimisation.ExpressionsBasedModel.EntityMap;
import org.ojalgo.optimisation.convex.ConvexSolver;
import org.ojalgo.optimisation.linear.LinearSolver;

/**
 * A solver that can be updated in-place between solves — primarily used by branch-and-bound to avoid
 * rebuilding the continuous relaxation at every node.
 * <p>
 * All update operations are optional; returning {@code false} or an empty collection signals that the caller
 * should fall back to rebuilding the solver.
 */
public interface UpdatableSolver extends Optimisation.Solver {

    /**
     * Fix a solver variable to a single value (sets both lower and upper bound).
     *
     * @param index solver-internal variable index (as defined by {@link EntityMap})
     * @param value the value to fix the variable to
     * @return {@code true} if successfully applied
     */
    default boolean fixVariable(final int index, final double value) {
        return false;
    }

    /**
     * Generate candidate cutting planes that separate the current fractional solution from the
     * integer-feasible region.
     *
     * @param fractionality measure of how fractional the current solution is (0 = nearly integral, 0.5 =
     *                      highly fractional)
     * @param integer       mask aligned with solver-internal indices; {@code true} for integer-constrained
     *                      variables
     * @return valid inequalities to add, or an empty collection if none are generated
     */
    default Collection<Equation> generateCutCandidates(final double fractionality, final boolean[] integer) {
        return Collections.emptySet();
    }

    /**
     * The dual variable (Lagrange multiplier) associated with constraint {@code index}. This is the rate of
     * change of the optimal objective value per unit relaxation of that constraint.
     *
     * @param index solver-internal constraint index
     * @return the dual variable value at the current solution
     */
    double getDualMultiplier(int index);

    /**
     * The mapping between this solver's internal variable indices and the owning
     * {@link ExpressionsBasedModel}. This is needed when the solver was built from a model and there is no
     * 1-to-1 correspondence between model and solver variables.
     * <p>
     * When the solver was built from a {@link LinearSolver.Builder} or {@link ConvexSolver.Builder} the
     * mapping is typically 1-to-1 and no {@link EntityMap} is available.
     *
     * @return the {@link EntityMap} if available; {@link Optional#empty()} otherwise
     */
    default Optional<ExpressionsBasedModel.EntityMap> getEntityMap() {
        return Optional.empty();
    }

    /**
     * The reduced gradient of variable {@code index} — the rate of change of the objective if this variable
     * were to move from its current bound, accounting for the dual variables. For LP this is a constant per
     * variable at a given basis; for convex problems it is point-dependent.
     *
     * @param index solver-internal variable index (as defined by {@link EntityMap})
     * @return the reduced gradient at the current solution
     */
    double getReducedGradient(int index);

    /**
     * Tighten the bounds of a solver variable by intersecting with the given range. Generalises
     * {@link #fixVariable(int, double)} (fixing ≡ {@code updateRange(index, value, value)}).
     *
     * @param index solver-internal variable index (as defined by {@link EntityMap})
     * @param lower new lower bound
     * @param upper new upper bound
     * @return {@code true} if successfully applied
     */
    default boolean updateRange(final int index, final double lower, final double upper) {
        return false;
    }

}
