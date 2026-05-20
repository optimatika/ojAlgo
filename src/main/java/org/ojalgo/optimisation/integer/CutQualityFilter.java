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
package org.ojalgo.optimisation.integer;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map.Entry;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.context.NumberContext;

/**
 * Shared quality filters for generated cutting planes. Mirrors the checks in
 * {@link NodeSolver#doGenerateCuts} so that both tableau-based (GMI) and model-based
 * cut generators apply consistent acceptance criteria.
 */
final class CutQualityFilter {

    private static final NumberContext PRECISION = NumberContext.of(12);
    private static final NumberContext DYNAMISM = NumberContext.of(8);
    private static final NumberContext COEFFICIENT = NumberContext.of(10);
    private static final NumberContext LIMIT = NumberContext.of(10);

    /**
     * Validate and clean up a candidate cut expression. Returns true if the cut is accepted,
     * false if it was rejected and removed from the model.
     *
     * @param model the model the cut was added to
     * @param cut the candidate cut expression (already has coefficients and bounds set)
     * @param result the current LP relaxation solution (used to adjust RHS when pruning small coefficients)
     * @return true if the cut passes all quality checks and should be kept
     */
    static boolean acceptCut(final ExpressionsBasedModel model, final Expression cut, final Optimisation.Result result) {

        BigDecimal rhs = cut.getLowerLimit();
        if (rhs == null) {
            rhs = cut.getUpperLimit();
        }
        if (rhs == null) {
            model.removeExpression(cut.getName());
            return false;
        }

        BigDecimal largest = BigDecimal.ONE;
        for (Entry<IntIndex, BigDecimal> entry : cut.getLinearEntrySet()) {
            largest = largest.max(entry.getValue().abs());
        }

        BigDecimal smallest = new BigDecimal("1E+18");
        for (Iterator<Entry<IntIndex, BigDecimal>> iterator = cut.getLinearEntrySet().iterator(); iterator.hasNext();) {
            Entry<IntIndex, BigDecimal> entry = iterator.next();
            BigDecimal value = entry.getValue();

            if (!PRECISION.isSmall(largest, value)) {
                smallest = smallest.min(value.abs());
                entry.setValue(COEFFICIENT.enforce(value));
            } else {
                // Prune near-zero coefficient, adjust RHS to compensate
                BigDecimal adjustment = value.multiply(result.get(entry.getKey().index));
                if (cut.getLowerLimit() != null) {
                    rhs = rhs.subtract(adjustment);
                    cut.lower(LIMIT.enforce(rhs));
                } else if (cut.getUpperLimit() != null) {
                    rhs = rhs.subtract(adjustment);
                    cut.upper(LIMIT.enforce(rhs));
                }
                iterator.remove();
            }
        }

        if (DYNAMISM.isSmall(largest, smallest)) {
            model.removeExpression(cut.getName());
            return false;
        }

        if (model.checkSimilarity(cut)) {
            model.removeExpression(cut.getName());
            return false;
        }

        cut.enforce(NumberContext.of(10));
        cut.tighten();

        return true;
    }

    private CutQualityFilter() {
    }

}
