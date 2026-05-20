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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.structure.Structure1D.IntIndex;

/**
 * Generates knapsack cover cuts from constraints containing binary variables with positive coefficients.
 * For mixed constraints (binary + non-binary), non-binary variables are bounded to compute a worst-case
 * effective capacity for the binary knapsack portion. For a binary knapsack
 * &Sigma; a<sub>i</sub>x<sub>i</sub> &le; b&prime; where all a<sub>i</sub> &gt; 0 and x<sub>i</sub> &isin; {0,1},
 * a <em>cover</em> C with &Sigma;<sub>C</sub> a<sub>i</sub> &gt; b&prime; yields the valid inequality
 * &Sigma;<sub>C</sub> x<sub>i</sub> &le; |C| &minus; 1.
 */
public final class KnapsackCoverCutGenerator implements ModelCutGenerator {

    private static final class VarInfo {

        final double coefficient;
        final IntIndex index;
        final double lpValue;

        VarInfo(final IntIndex index, final double coefficient, final double lpValue) {
            this.index = index;
            this.coefficient = coefficient;
            this.lpValue = lpValue;
        }

    }

    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final int MAX_CUTS_PER_ROUND = 50;

    public KnapsackCoverCutGenerator() {
        super();
    }

    @Override
    public int generateCuts(final ExpressionsBasedModel model, final Optimisation.Result result) {

        int cutsAdded = 0;

        Collection<Expression> snapshot = new ArrayList<>(model.getExpressions());

        for (Expression constraint : snapshot) {

            if (cutsAdded >= MAX_CUTS_PER_ROUND) {
                break;
            }

            if (!constraint.isConstraint() || constraint.isRedundant() || !constraint.isFunctionLinear()) {
                continue;
            }

            if (!constraint.isLinearAndAnyBinary()) {
                continue;
            }

            if (constraint.isUpperLimitSet()) {
                Expression cut = this.tryGenerateCoverCut(constraint, model, result, true);
                if (cut != null && CutQualityFilter.acceptCut(model, cut, result)) {
                    cutsAdded++;
                }
            }

            if (cutsAdded < MAX_CUTS_PER_ROUND && constraint.isLowerLimitSet()) {
                Expression cut = this.tryGenerateCoverCut(constraint, model, result, false);
                if (cut != null && CutQualityFilter.acceptCut(model, cut, result)) {
                    cutsAdded++;
                }
            }
        }

        return cutsAdded;
    }

    /**
     * @param upper true for the <= direction, false for >= (negated to <=)
     */
    private Expression tryGenerateCoverCut(final Expression constraint, final ExpressionsBasedModel model, final Optimisation.Result result,
            final boolean upper) {

        double b = upper ? constraint.getUpperLimit().doubleValue() : -constraint.getLowerLimit().doubleValue();

        // Compute worst-case effective capacity for the binary portion.
        // For sum(a_i * x_i) + sum(c_k * y_k) <= b, the binary portion satisfies:
        // sum(a_i * x_i) <= b - min(sum(c_k * y_k))
        // where the min is over feasible y_k values.
        // For c_k > 0, y_k >= l_k: min contribution = c_k * l_k (need lower bound)
        // For c_k < 0, y_k <= u_k: min contribution = c_k * u_k (need upper bound)
        // If any required bound is missing, we can't compute effective capacity.
        double effectiveCapacity = b;
        List<VarInfo> binaryVars = new ArrayList<>();
        boolean canBound = true;

        for (Entry<IntIndex, BigDecimal> entry : constraint.getLinearEntrySet()) {
            IntIndex idx = entry.getKey();
            Variable var = model.getVariable(idx);
            double coeff = upper ? entry.getValue().doubleValue() : -entry.getValue().doubleValue();
            double lpVal = result.doubleValue(idx.index);

            if (var.isBinary()) {
                if (coeff > ZERO) {
                    binaryVars.add(new VarInfo(idx, coeff, lpVal));
                } else if (coeff < ZERO) {
                    // Complement: x' = 1 - x, coeff becomes -coeff > 0, capacity adjusted
                    effectiveCapacity -= coeff;
                    binaryVars.add(new VarInfo(idx, -coeff, ONE - lpVal));
                }
            } else {
                // Non-binary variable: subtract worst-case (minimum) contribution
                if (coeff > ZERO) {
                    if (!var.isLowerLimitSet()) {
                        canBound = false;
                        break;
                    }
                    effectiveCapacity -= coeff * var.getLowerLimit().doubleValue();
                } else if (coeff < ZERO) {
                    if (!var.isUpperLimitSet()) {
                        canBound = false;
                        break;
                    }
                    effectiveCapacity -= coeff * var.getUpperLimit().doubleValue();
                }
            }
        }

        if (!canBound || binaryVars.size() < 2) {
            return null;
        }

        // Sort by LP value descending for greedy cover selection
        binaryVars.sort(Comparator.comparing((final VarInfo a) -> a.lpValue).reversed());

        // Greedy cover: add variables with highest LP values first
        List<VarInfo> cover = new ArrayList<>();
        double coverSum = ZERO;
        for (VarInfo vi : binaryVars) {
            cover.add(vi);
            coverSum += vi.coefficient;
            if (coverSum > effectiveCapacity) {
                break;
            }
        }

        if (coverSum <= effectiveCapacity) {
            return null;
        }

        // Minimise cover: remove variables from the end if possible
        for (int i = cover.size() - 1; i >= 0; i--) {
            double without = coverSum - cover.get(i).coefficient;
            if (without > effectiveCapacity) {
                coverSum = without;
                cover.remove(i);
            }
        }

        // Check if the cover inequality is violated by the LP solution
        double coverLPSum = ZERO;
        for (VarInfo vi : cover) {
            coverLPSum += vi.lpValue;
        }

        int coverSize = cover.size();
        double rhs = coverSize - 1;

        if (coverLPSum <= rhs + 1e-8) {
            return null;
        }

        // Build the cut. For complemented variables (original coeff < 0):
        // x' = 1 - x, so sum x'_i = sum(1-x_i) = #complemented - sum x_i
        // The cut sum x'_i + sum x_j <= |C|-1 becomes:
        // (#complemented - sum x_i_complemented) + sum x_j_normal <= |C|-1
        // => sum x_j_normal - sum x_i_complemented <= |C|-1-#complemented

        String name = "CUT_COVER_" + COUNTER.incrementAndGet();
        Expression cut = model.newExpression(name);

        int complemented = 0;
        for (VarInfo vi : cover) {
            double origCoeff = upper ? constraint.get(vi.index).doubleValue() : -constraint.get(vi.index).doubleValue();
            if (origCoeff < ZERO) {
                cut.add(vi.index.index, BigDecimal.ONE.negate());
                complemented++;
            } else {
                cut.add(vi.index.index, BigDecimal.ONE);
            }
        }

        cut.upper(BigDecimal.valueOf(rhs - complemented));

        return cut;
    }

}
