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

/**
 * A solver that can be updated in-place between solves.
 * <p>
 * An {@link UpdatableSolver} exposes a stable mapping between the solver's internal variables and the
 * {@link ExpressionsBasedModel} entities and, optionally, supports incremental modification of variable
 * bounds and generation of cutting planes. This is primarily used by
 * {@link org.ojalgo.optimisation.integer.IntegerSolver} (branch-and-bound) implementations to avoid
 * rebuilding the underlying continuous relaxation from scratch for every node in the search tree.
 * <p>
 * The contract is intentionally minimal:
 * <ul>
 * <li>The mapping from model entities to internal variables is exposed via {@link #getEntityMap()}.</li>
 * <li>Bounds for an internal variable may be tightened via {@link #fixVariable(int, double)} or
 * {@link #updateRange(int, double, double)}.</li>
 * <li>Optionally, additional valid inequalities may be proposed via
 * {@link #generateCutCandidates(double, boolean[])}.</li>
 * </ul>
 * An implementation is free to ignore any of these update operations by returning {@code false} or an empty
 * collection, in which case the caller should fall back to rebuilding the solver instance.
 */
public interface UpdatableSolver extends Optimisation.Solver {

    /**
     * Try to fix a solver variable to a single value.
     * <p>
     * The {@code index} is the solver's own variable index as defined by the {@link EntityMap} returned from
     * {@link #getEntityMap()}. Implementations that support this operation should tighten both the lower and
     * upper bound of the referenced variable to {@code value}. If the index is out of range, or if fixing the
     * variable would make the current solver state inconsistent, the method should return {@code false}
     * without throwing.
     *
     * @param index the solver specific variable index, not the model index
     * @param value the single value the variable is required to take
     * @return {@code true} if the variable was successfully fixed, {@code false} if the operation is not
     *         supported or could not be applied
     */
    default boolean fixVariable(final int index, final double value) {
        return false;
    }

    /**
     * Optionally generate candidate cutting planes for a given fractional solution.
     * <p>
     * This hook is used by integer optimisation algorithms to request model-equivalent inequalities that cut
     * off the current fractional solution but keep all integer-feasible points. The {@code fractionality}
     * parameter communicates how far from integrality the current solution is (typically the maximum distance
     * to the nearest integer among all integer variables). The variable selection mask ({@code integer}) is a
     * boolean array aligned with the solver's internal variable ordering – the same indexing convention as
     * for {@link #fixVariable(int, double)} and {@link #updateRange(int, double, double)}.
     * <p>
     * The default implementation returns an empty set, meaning that the solver does not contribute any cuts
     * and the caller should proceed without cut generation.
     *
     * @param fractionality a measure of how fractional the current solution is; values close to zero mean
     *        nearly integral, values near {@code 0.5} indicate highly fractional components
     * @param integer a boolean mask indicating which internal variables correspond to integer-constrained
     *        model entities; {@code true} means the variable must take an integer value
     * @return a collection of {@link Equation} instances representing additional valid inequalities to be
     *         added to the model; may be empty but should never be {@code null}
     */
    default Collection<Equation> generateCutCandidates(final double fractionality, final boolean[] integer) {
        return Collections.emptySet();
    }

    /**
     * The mapping between this solver's internal variable indices and the owning
     * {@link ExpressionsBasedModel}.
     * <p>
     * The {@link EntityMap} describes how model variables and slack variables are laid out in the solver.
     * This allows callers (for example branch-and-bound controllers) to translate between model-level
     * entities and the indices used in {@link #fixVariable(int, double)} and
     * {@link #updateRange(int, double, double)}, and to construct the integer mask passed to
     * {@link #generateCutCandidates(double, boolean[])}.
     *
     * @return an {@link Optional} containing the {@link EntityMap} for this solver when such a mapping is
     *         available; {@link Optional#empty()} otherwise
     */
    default Optional<ExpressionsBasedModel.EntityMap> getEntityMap() {
        return Optional.empty();
    }

    /**
     * Incrementally tighten the bounds of a solver variable.
     * <p>
     * The {@code index} is the solver's internal variable index as described by the {@link EntityMap}
     * returned from {@link #getEntityMap()}. Implementations are expected to intersect the current variable
     * bounds with the supplied {@code [lower, upper]} range. This method generalises
     * {@link #fixVariable(int, double)} in the sense that fixing a variable corresponds to calling
     * {@code updateRange(index, value, value)}.
     * <p>
     * Callers should only supply ranges that are consistent with the owning {@link ExpressionsBasedModel}. If
     * the update is not supported or would result in an inconsistent internal state, the implementation
     * should return {@code false} rather than throwing.
     *
     * @param index the solver specific variable index, not the model index
     * @param lower the new (possibly tighter) lower bound for the variable
     * @param upper the new (possibly tighter) upper bound for the variable
     * @return {@code true} if the range was successfully updated, {@code false} if the operation is not
     *         supported or could not be applied
     */
    default boolean updateRange(final int index, final double lower, final double upper) {
        return false;
    }

}
