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
import java.util.concurrent.atomic.AtomicInteger;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.integer.IntegerStrategy.CutConfiguration;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.context.NumberContext;

/**
 * Generates knapsack cover cuts for constraints involving binary variables.
 * <p>
 * For a constraint {@code sum(a_j * x_j) <= b} with binary variables, a cover C is a subset where
 * {@code sum_{C}(a_j) > b}. The cover inequality {@code sum_{C}(x_j) <= |C| - 1} is valid for any
 * integer-feasible solution.
 * <p>
 * Non-binary variables are handled by subtracting their minimum contribution (using bounds) from the RHS.
 * Negative binary coefficients are complemented ({@code x' = 1 - x}) to make all coefficients positive before
 * cover finding.
 */
final class KnapsackCoverSeparator extends NodeSolver.Separator {

    private static final AtomicInteger COUNTER = new AtomicInteger();
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

        List<Integer> binaryVarIndices = new ArrayList<>();
        List<BigDecimal> binaryCoeffs = new ArrayList<>();
        List<Boolean> isComplemented = new ArrayList<>();

        boolean valid = true;

        for (Entry<IntIndex, BigDecimal> entry : constraint.getLinearEntrySet()) {
            int idx = entry.getKey().index;
            Variable var = model.getVariable(idx);
            BigDecimal coeff = negate ? entry.getValue().negate() : entry.getValue();

            if (var.isBinary()) {
                if (coeff.signum() < 0) {
                    binaryVarIndices.add(idx);
                    binaryCoeffs.add(coeff.negate());
                    isComplemented.add(Boolean.TRUE);
                    effectiveRHS = effectiveRHS.subtract(coeff);
                } else if (coeff.signum() > 0) {
                    binaryVarIndices.add(idx);
                    binaryCoeffs.add(coeff);
                    isComplemented.add(Boolean.FALSE);
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

        if (!valid || binaryVarIndices.size() < 2) {
            return 0;
        }

        if (effectiveRHS.signum() <= 0) {
            return 0;
        }

        BigDecimal totalSum = BigDecimal.ZERO;
        for (BigDecimal c : binaryCoeffs) {
            totalSum = totalSum.add(c);
        }
        if (totalSum.compareTo(effectiveRHS) <= 0) {
            return 0;
        }

        int n = binaryVarIndices.size();
        double[] lpValues = new double[n];
        for (int i = 0; i < n; i++) {
            double val = solution.doubleValue(binaryVarIndices.get(i));
            lpValues[i] = isComplemented.get(i) ? 1.0 - val : val;
        }

        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) {
            order[i] = i;
        }
        Arrays.sort(order, (a, b) -> Double.compare(lpValues[b], lpValues[a]));

        BigDecimal coverSum = BigDecimal.ZERO;
        List<Integer> coverList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int k = order[i];
            coverList.add(k);
            coverSum = coverSum.add(binaryCoeffs.get(k));
            if (coverSum.compareTo(effectiveRHS) > 0) {
                break;
            }
        }

        if (coverSum.compareTo(effectiveRHS) <= 0) {
            return 0;
        }

        for (int i = coverList.size() - 1; i >= 0; i--) {
            int k = coverList.get(i);
            BigDecimal without = coverSum.subtract(binaryCoeffs.get(k));
            if (without.compareTo(effectiveRHS) > 0) {
                coverSum = without;
                coverList.remove(i);
            }
        }

        int coverSize = coverList.size();
        if (coverSize < 2) {
            return 0;
        }

        double violation = -(coverSize - 1);
        for (int k : coverList) {
            violation += lpValues[k];
        }

        if (violation <= 0 || TOLERANCE.isZero(violation)) {
            return 0;
        }

        String name = "CUT_KC_" + COUNTER.incrementAndGet();
        Expression cut = model.newExpression(name);

        int nbComp = 0;
        for (int k : coverList) {
            int idx = binaryVarIndices.get(k);
            if (isComplemented.get(k)) {
                cut.add(idx, BigDecimal.ONE.negate());
                nbComp++;
            } else {
                cut.add(idx, BigDecimal.ONE);
            }
        }

        cut.upper(BigDecimal.valueOf(coverSize - 1 - nbComp));

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

            if (!constraint.isInteger()) {
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

}
