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
import java.math.RoundingMode;
import java.util.ArrayList;
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
 * Generates implied bound cuts.
 * <p>
 * For a row {@code sum(a_j * x_j) <= b}, fixing a binary variable {@code x} to the value that consumes its
 * coefficient leaves less slack for the other variables, which implies a (conditional) bound on each of them:
 * {@code x = 1 => y <= u'} (or {@code x = 0 => y <= u'}, and similarly for lower bounds). When {@code u'} is
 * tighter than the global bound {@code u} of {@code y}, the implication and the global bound combine into the
 * valid inequality {@code y <= u - (u - u') * x} (respectively {@code y <= u' + (u - u') * x}), which the LP
 * relaxation typically violates when {@code x} is fractional and {@code y} sits near its bound. The variable
 * upper bound row {@code y <= M * x} is the classic special case, where the cut replaces {@code M} by the
 * actual bound of {@code y}.
 * <p>
 * Only non-binary target variables are considered; implications between two binaries are conflicts, handled
 * by the {@link CliqueSeparator}.
 */
final class ImpliedBoundsSeparator extends NodeSolver.Separator {

    private static final NumberContext CEILING = NumberContext.of(12).withMode(RoundingMode.CEILING);
    private static final NumberContext FLOOR = NumberContext.of(12).withMode(RoundingMode.FLOOR);
    private static final double FRACTIONALITY = 1e-6;
    private static final NumberContext TOLERANCE = NumberContext.of(4);

    static final IntegerStrategy.CutConfiguration CONFIGURATION = new IntegerStrategy.CutConfiguration().withIterations(3);

    /**
     * A row can only give an implied bound cut if it has a binary variable to condition on and a non-binary,
     * non-fixed variable to bound. Checked before the exact activity computation, which was the whole
     * separation cost on pure binary models (where the fixed binaries of a node otherwise pass as targets).
     */
    private static boolean hasBinaryAndTarget(final ExpressionsBasedModel model, final Expression constraint) {
        boolean binary = false;
        boolean target = false;
        for (Entry<IntIndex, BigDecimal> entry : constraint.getLinearEntrySet()) {
            if (entry.getValue().signum() == 0) {
                continue;
            }
            Variable variable = model.getVariable(entry.getKey().index);
            if (variable.isBinary()) {
                binary = true;
            } else if (!variable.isEqualityConstraint()) {
                target = true;
            }
            if (binary && target) {
                return true;
            }
        }
        return false;
    }

    ImpliedBoundsSeparator() {
        super();
    }

    /**
     * @return true if the cut was added
     */
    private boolean addCut(final ExpressionsBasedModel model, final int targetIndex, final BigDecimal targetCoeff, final int binaryIndex,
            final BigDecimal binaryCoeff, final BigDecimal upper, final BigDecimal lower, final Optimisation.Result solution) {

        double lhs = targetCoeff.doubleValue() * solution.doubleValue(targetIndex) + binaryCoeff.doubleValue() * solution.doubleValue(binaryIndex);

        double violation;
        if (upper != null) {
            violation = lhs - upper.doubleValue();
        } else {
            violation = lower.doubleValue() - lhs;
        }

        if (violation <= 0) {
            return false;
        }

        Expression cut = this.newCut(model);
        cut.add(targetIndex, targetCoeff);
        cut.add(binaryIndex, binaryCoeff);
        if (upper != null) {
            cut.upper(upper);
        } else {
            cut.lower(lower);
        }

        return this.accept(model, cut);
    }

    private int trySeparate(final ExpressionsBasedModel model, final Expression constraint, final boolean useUpper, final Optimisation.Result solution) {

        BigDecimal rhs = useUpper ? constraint.getUpperLimit() : constraint.getLowerLimit();
        if (rhs == null) {
            return 0;
        }

        boolean negate = !useUpper;

        List<Integer> indices = new ArrayList<>();
        List<BigDecimal> coeffs = new ArrayList<>();
        BigDecimal minActivity = BigDecimal.ZERO;

        for (Entry<IntIndex, BigDecimal> entry : constraint.getLinearEntrySet()) {
            int idx = entry.getKey().index;
            BigDecimal coeff = negate ? entry.getValue().negate() : entry.getValue();
            if (coeff.signum() == 0) {
                continue;
            }
            Variable var = model.getVariable(idx);
            BigDecimal bound = coeff.signum() > 0 ? var.getLowerLimit() : var.getUpperLimit();
            if (bound == null) {
                return 0;
            }
            minActivity = minActivity.add(coeff.multiply(bound));
            indices.add(Integer.valueOf(idx));
            coeffs.add(coeff);
        }

        int n = indices.size();
        if (n < 2) {
            return 0;
        }

        BigDecimal slack = (negate ? rhs.negate() : rhs).subtract(minActivity);
        if (slack.signum() < 0) {
            return 0;
        }

        int nbAdded = 0;

        for (int k = 0; k < n && this.isRoomForMore(); k++) {

            int binaryIndex = indices.get(k).intValue();
            Variable binary = model.getVariable(binaryIndex);
            if (!binary.isBinary()) {
                continue;
            }

            double xValue = solution.doubleValue(binaryIndex);
            if (xValue < FRACTIONALITY || xValue > 1.0 - FRACTIONALITY) {
                continue;
            }

            BigDecimal ak = coeffs.get(k);
            // Fixing x to the value that consumes its coefficient: x = 1 when a > 0, x = 0 when a < 0
            boolean impliedByOne = ak.signum() > 0;
            BigDecimal remaining = slack.subtract(ak.abs());
            if (remaining.signum() < 0) {
                continue;
            }

            for (int i = 0; i < n && this.isRoomForMore(); i++) {

                if (i == k) {
                    continue;
                }

                int targetIndex = indices.get(i).intValue();
                Variable target = model.getVariable(targetIndex);
                if (target.isBinary() || target.isEqualityConstraint()) {
                    continue;
                }

                BigDecimal ai = coeffs.get(i);
                BigDecimal lb = target.getLowerLimit();
                BigDecimal ub = target.getUpperLimit();

                if (ai.signum() > 0) {

                    if (ub == null) {
                        continue;
                    }

                    BigDecimal implied = lb.add(remaining.divide(ai, CEILING.getMathContext()));
                    if (target.isInteger()) {
                        implied = implied.setScale(0, RoundingMode.FLOOR);
                    }
                    BigDecimal gain = ub.subtract(implied);
                    if (gain.signum() <= 0 || TOLERANCE.isSmall(ub.abs().max(BigDecimal.ONE), gain)) {
                        continue;
                    }
                    // x = 1 => y <= u' : y + (u - u') x <= u
                    // x = 0 => y <= u' : y - (u - u') x <= u'
                    if (impliedByOne) {
                        if (this.addCut(model, targetIndex, BigDecimal.ONE, binaryIndex, gain, ub, null, solution)) {
                            nbAdded++;
                        }
                    } else {
                        if (this.addCut(model, targetIndex, BigDecimal.ONE, binaryIndex, gain.negate(), implied, null, solution)) {
                            nbAdded++;
                        }
                    }

                } else {

                    if (lb == null) {
                        continue;
                    }

                    BigDecimal implied = ub.add(remaining.divide(ai, FLOOR.getMathContext()));
                    if (target.isInteger()) {
                        implied = implied.setScale(0, RoundingMode.CEILING);
                    }
                    BigDecimal gain = implied.subtract(lb);
                    if (gain.signum() <= 0 || TOLERANCE.isSmall(lb.abs().max(BigDecimal.ONE), gain)) {
                        continue;
                    }
                    // x = 1 => y >= l' : y - (l' - l) x >= l
                    // x = 0 => y >= l' : y + (l' - l) x >= l'
                    if (impliedByOne) {
                        if (this.addCut(model, targetIndex, BigDecimal.ONE, binaryIndex, gain.negate(), null, lb, solution)) {
                            nbAdded++;
                        }
                    } else {
                        if (this.addCut(model, targetIndex, BigDecimal.ONE, binaryIndex, gain, null, implied, solution)) {
                            nbAdded++;
                        }
                    }
                }
            }
        }

        return nbAdded;
    }

    int generateCuts(final ExpressionsBasedModel model, final Optimisation.Result solution, final CutConfiguration configuration) {

        this.beginRound(model, solution, configuration);

        List<Expression> expressions = new ArrayList<>(model.getExpressions());

        for (Expression constraint : expressions) {

            if (!this.isRoomForMore()) {
                break;
            }

            if (constraint.isObjective() || !constraint.isConstraint() || constraint.isAnyQuadraticFactorNonZero()
                    || !ImpliedBoundsSeparator.hasBinaryAndTarget(model, constraint)) {
                continue;
            }

            if (constraint.isUpperLimitSet()) {
                this.trySeparate(model, constraint, true, solution);
            }
            if (this.isRoomForMore() && constraint.isLowerLimitSet()) {
                this.trySeparate(model, constraint, false, solution);
            }
        }

        return this.endRound(model);
    }

    /**
     * Implied Bounds
     */
    @Override
    String type() {
        return "IB";
    }

}
