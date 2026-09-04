/*
 * Copyright 1997-2026 Optimatika
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.integer.IntegerStrategy.CutConfiguration;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.context.NumberContext;

/**
 * Generates lifted knapsack cover cuts for constraints involving binary variables.
 * <p>
 * For a constraint {@code sum(a_j * x_j) <= b} with binary variables, a cover C is a subset where
 * {@code sum_{C}(a_j) > b}. The basic cover inequality {@code sum_{C}(x_j) <= |C| - 1} is strengthened via a
 * superadditive lifting function that assigns non-zero coefficients to non-cover variables and potentially
 * larger coefficients to cover variables with large original coefficients.
 * <p>
 * The lifting function is sequence-independent (superadditive), so all variables are lifted in a single pass
 * without solving knapsack subproblems, and the cover does not need to be minimal.
 */
final class KnapsackCoverSeparator extends NodeSolver.Separator {

    /**
     * Feasibility tolerance used when classifying coefficients against the lifting function's breakpoints,
     * and (scaled) as the minimum excess a cover must have over the right-hand side. A cover that exceeds the
     * capacity by less than this is treated as not being a cover at all, so that sub-tolerance noise in the
     * right-hand side (of cut rows, or an objective cutoff) is never amplified into a unit-sized error.
     */
    private static final double FEASTOL = 1e-6;
    private static final NumberContext TOLERANCE = NumberContext.of(4);

    static final IntegerStrategy.CutConfiguration CONFIGURATION = new IntegerStrategy.CutConfiguration().withIterations(3);

    KnapsackCoverSeparator(final ExpressionsBasedModel ebm) {
        super(ebm);
    }

    private int trySeparate(final Expression constraint, final boolean useUpper, final Optimisation.Result solution, final CutConfiguration configuration) {

        BigDecimal rhs = useUpper ? constraint.getUpperLimit() : constraint.getLowerLimit();
        if (rhs == null) {
            return 0;
        }

        boolean negate = !useUpper;
        BigDecimal effectiveRHS = negate ? rhs.negate() : rhs;

        List<Integer> binVarIdx = new ArrayList<>();
        List<BigDecimal> binCoeffs = new ArrayList<>();
        List<Boolean> compled = new ArrayList<>();

        boolean valid = true;

        for (Entry<IntIndex, BigDecimal> entry : constraint.getLinearEntrySet()) {
            int idx = entry.getKey().index;
            Variable var = model.getVariable(idx);
            BigDecimal coeff = negate ? entry.getValue().negate() : entry.getValue();

            if (var.isBinary()) {
                if (coeff.signum() < 0) {
                    binVarIdx.add(idx);
                    binCoeffs.add(coeff.negate());
                    compled.add(Boolean.TRUE);
                    effectiveRHS = effectiveRHS.subtract(coeff);
                } else if (coeff.signum() > 0) {
                    binVarIdx.add(idx);
                    binCoeffs.add(coeff);
                    compled.add(Boolean.FALSE);
                }
            } else {
                BigDecimal minContrib;
                if (coeff.signum() > 0) {
                    BigDecimal lb = var.getLowerLimit();
                    if (lb == null) {
                        valid = false;
                        break;
                    }
                    minContrib = coeff.multiply(lb);
                } else if (coeff.signum() < 0) {
                    BigDecimal ub = var.getUpperLimit();
                    if (ub == null) {
                        valid = false;
                        break;
                    }
                    minContrib = coeff.multiply(ub);
                } else {
                    continue;
                }
                effectiveRHS = effectiveRHS.subtract(minContrib);
            }
        }

        if (!valid || binVarIdx.size() < 2) {
            return 0;
        }

        if (effectiveRHS.signum() <= 0) {
            return 0;
        }

        int n = binVarIdx.size();
        double rhsD = effectiveRHS.doubleValue();
        double minLambda = Math.max(10.0 * FEASTOL, FEASTOL * rhsD);

        double[] coeffsD = new double[n];
        double[] lpVals = new double[n];
        for (int i = 0; i < n; i++) {
            coeffsD[i] = binCoeffs.get(i).doubleValue();
            double val = solution.doubleValue(binVarIdx.get(i));
            lpVals[i] = compled.get(i) ? 1.0 - val : val;
        }

        double totalSum = 0;
        for (int i = 0; i < n; i++) {
            totalSum += coeffsD[i];
        }
        if (totalSum - rhsD <= minLambda) {
            return 0;
        }

        // Greedy cover: sort by LP contribution (lpValue * coefficient) descending
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) {
            order[i] = i;
        }
        Arrays.sort(order, (a, b) -> Double.compare(lpVals[b] * coeffsD[b], lpVals[a] * coeffsD[a]));

        List<Integer> coverIdx = new ArrayList<>();
        double coverSumD = 0;
        for (int i = 0; i < n; i++) {
            int k = order[i];
            coverIdx.add(k);
            coverSumD += coeffsD[k];
            if (coverSumD - rhsD > minLambda) {
                break;
            }
        }

        double lambda = coverSumD - rhsD;
        if (lambda <= minLambda) {
            return 0;
        }

        int coverSize = coverIdx.size();
        if (coverSize < 2) {
            return 0;
        }

        // Sort cover by decreasing coefficient for the lifting function
        coverIdx.sort((a, b) -> Double.compare(coeffsD[b], coeffsD[a]));

        // Compute abar: threshold such that sum_{cover} min(abar, a_j) = rhs
        double abar = coeffsD[coverIdx.get(0)];
        double sigma = lambda;
        for (int i = 1; i < coverSize; i++) {
            double ai = coeffsD[coverIdx.get(i)];
            double delta = abar - ai;
            double kdelta = i * delta;
            if (kdelta < sigma) {
                abar = ai;
                sigma -= kdelta;
            } else {
                abar -= sigma / i;
                sigma = 0;
                break;
            }
        }
        if (sigma > 0) {
            abar = rhsD / coverSize;
        }

        // Build partial sums S[h] = sum_{k=0..h} min(abar, a_cover[k])
        double[] partialSums = new double[coverSize];
        partialSums[0] = Math.min(abar, coeffsD[coverIdx.get(0)]);
        for (int i = 1; i < coverSize; i++) {
            partialSums[i] = partialSums[i - 1] + Math.min(abar, coeffsD[coverIdx.get(i)]);
        }

        // Mark C- variables (cover variables with coefficient <= abar)
        boolean[] isCMinus = new boolean[n];
        for (int i = 0; i < coverSize; i++) {
            int k = coverIdx.get(i);
            if (coeffsD[k] <= abar + FEASTOL) {
                isCMinus[k] = true;
            }
        }

        // Compute lifted coefficients and check violation
        int[] lifted = new int[n];
        double lhsValue = 0;

        for (int i = 0; i < n; i++) {
            if (isCMinus[i]) {
                lifted[i] = 1;
            } else {
                // Staircase lifting function: g(z) = max{h+1 : z > S[h]}
                double z = coeffsD[i];
                int g = 0;
                for (int h = 0; h < coverSize; h++) {
                    if (z > partialSums[h] + FEASTOL) {
                        g = h + 1;
                    } else {
                        break;
                    }
                }
                lifted[i] = g;
            }
            lhsValue += lifted[i] * lpVals[i];
        }

        double violation = lhsValue - (coverSize - 1);
        if (violation <= 0 || TOLERANCE.isZero(violation)) {
            return 0;
        }

        // Build the cut in original model variables
        String name = "CUT_KC_" + COUNTER.incrementAndGet();
        Expression cut = model.newExpression(name);

        long cutRHS = coverSize - 1;
        for (int i = 0; i < n; i++) {
            if (lifted[i] == 0) {
                continue;
            }
            int idx = binVarIdx.get(i);
            if (compled.get(i)) {
                cut.add(idx, BigDecimal.valueOf(-lifted[i]));
                cutRHS -= lifted[i];
            } else {
                cut.add(idx, BigDecimal.valueOf(lifted[i]));
            }
        }

        cut.upper(BigDecimal.valueOf(cutRHS));

        if (model.checkSimilarity(cut)) {
            model.removeExpression(name);
            return 0;
        }

        return 1;
    }

    int generateCuts(final Optimisation.Result solution, final CutConfiguration configuration) {

        int nbBefore = model.countExpressions();

        int maxCuts = configuration.getMaxCuts(model.countVariables());
        int nbAdded = 0;

        List<Expression> expressions = new ArrayList<>(model.getExpressions());

        for (Expression constraint : expressions) {

            if (nbAdded >= maxCuts) {
                break;
            }

            if (constraint.isObjective() || !constraint.isInteger()) {
                continue;
            }

            if (constraint.isAnyQuadraticFactorNonZero()) {
                continue;
            }

            if (constraint.isUpperLimitSet()) {
                nbAdded += this.trySeparate(constraint, true, solution, configuration);
            }
            if (nbAdded < maxCuts && constraint.isLowerLimitSet()) {
                nbAdded += this.trySeparate(constraint, false, solution, configuration);
            }
        }

        return model.countExpressions() - nbBefore;
    }

    /**
     * Knapsack Cover
     */
    @Override
    String type() {
        return "KC";
    }

}
