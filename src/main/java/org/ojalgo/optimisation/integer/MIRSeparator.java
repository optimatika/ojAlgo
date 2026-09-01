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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.integer.IntegerStrategy.CutType;
import org.ojalgo.optimisation.integer.NodeSolver.CutRound;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.context.NumberContext;

/**
 * Generates MIR (Mixed-Integer Rounding) cuts from the original model constraints.
 * <p>
 * For each original constraint that involves integer variables, shifts variables to have lower bound zero,
 * optionally complements variables near their upper bound, and applies the MIR rounding formula.
 * <p>
 * Equality constraints use the GMI-style formula (all MIR coefficients non-negative, cut direction ≥ 1).
 * Inequality constraints use the proper MIR formula (coefficients retain the integer floor, positive
 * continuous variables are excluded, cut direction ≤ ⌊b⌋).
 */
final class MIRSeparator extends NodeSolver.Separator {

    private static final NumberContext ACCURACY = NumberContext.of(8);

    /**
     * The efficacy the MIR cut of this constraint would have, derived in double precision before the exact
     * derivation. The exact cut can only be weaker (tiny coefficients are removed by relaxing the right-hand
     * side), so a candidate too weak here would have been rejected by {@link #accept} as well. Most MIR
     * candidates are rejected for lack of efficacy, and building them exactly was the dominant cost.
     */
    private static double estimateEfficacy(final ExpressionsBasedModel model, final Expression constraint, final boolean negate, final boolean equality,
            final Optimisation.Result solution, final BigDecimal effectiveRHS) {

        double f0 = MIRSeparator.fraction(effectiveRHS).doubleValue();
        double cf0 = 1.0 - f0;

        double lhs = 0.0;
        double norm = 0.0;

        for (Entry<IntIndex, BigDecimal> entry : constraint.getLinearEntrySet()) {
            int idx = entry.getKey().index;
            Variable variable = model.getVariable(idx);
            double cj = negate ? -entry.getValue().doubleValue() : entry.getValue().doubleValue();
            double value = solution.doubleValue(idx);
            double lower = variable.getLowerLimit().doubleValue();

            double shifted = value - lower;
            if (variable.isUpperLimitSet()) {
                double upper = variable.getUpperLimit().doubleValue();
                if (upper > lower && shifted > (upper - lower) / 2.0) {
                    cj = -cj;
                    shifted = upper - value;
                }
            }

            double mirCoeff;
            double fj = cj - Math.floor(cj);
            if (equality) {
                if (variable.isInteger()) {
                    mirCoeff = fj <= f0 ? fj / f0 : (1.0 - fj) / cf0;
                } else if (cj > 0.0) {
                    mirCoeff = cj / f0;
                } else {
                    mirCoeff = -cj / cf0;
                }
            } else if (variable.isInteger()) {
                mirCoeff = fj <= f0 ? Math.floor(cj) : Math.floor(cj) + (fj - f0) / cf0;
            } else if (cj < 0.0) {
                mirCoeff = cj / cf0;
            } else {
                continue;
            }

            lhs += mirCoeff * shifted;
            norm += mirCoeff * mirCoeff;
        }

        double violation = equality ? 1.0 - lhs : lhs - Math.floor(effectiveRHS.doubleValue());

        return violation > 0.0 && norm > 0.0 ? violation / Math.sqrt(norm) : 0.0;
    }

    private static BigDecimal floor(final BigDecimal value) {
        return value.setScale(0, RoundingMode.FLOOR);
    }

    private static BigDecimal fraction(final BigDecimal value) {
        return value.subtract(MIRSeparator.floor(value));
    }

    /**
     * Can this expression ever produce a fractional effective RHS (after shift/complement)? If all
     * coefficients, RHS, and participating variable bounds are integer, the answer is no — f0 will always be
     * 0 regardless of the LP solution.
     * <p>
     * Uses {@link Expression#isInteger()} as a fast path: when true, all variables are integer (so bounds are
     * integer after rounding), coefficients are integer, and the RHS has been integer-rounded. For mixed
     * constraints, falls back to checking the individual values.
     */
    private static boolean hasFractionalData(final Expression constraint, final ExpressionsBasedModel model) {

        if (constraint.isInteger()) {
            return false;
        }

        if (constraint.isUpperLimitSet() && !MIRSeparator.isIntegerValue(constraint.getUpperLimit())) {
            return true;
        }
        if (constraint.isLowerLimitSet() && !MIRSeparator.isIntegerValue(constraint.getLowerLimit())) {
            return true;
        }

        for (Entry<IntIndex, BigDecimal> entry : constraint.getLinearEntrySet()) {

            if (!MIRSeparator.isIntegerValue(entry.getValue())) {
                return true;
            }

            Variable var = model.getVariable(entry.getKey().index);

            if (var.isLowerLimitSet() && !MIRSeparator.isIntegerValue(var.getLowerLimit())) {
                return true;
            }
            if (var.isUpperLimitSet() && !MIRSeparator.isIntegerValue(var.getUpperLimit())) {
                return true;
            }
        }

        return false;
    }

    private static boolean isIntegerValue(final BigDecimal value) {
        return value != null && value.stripTrailingZeros().scale() <= 0;
    }

    private static boolean shouldComplement(final Variable variable, final BigDecimal value, final BigDecimal lower) {

        if (variable.isUpperLimitSet()) {
            BigDecimal upper = variable.getUpperLimit();
            BigDecimal range = upper.subtract(lower);
            if (range.signum() > 0) {
                return value.subtract(lower).compareTo(MissingMath.divide(range, BigMath.TWO)) > 0;
            }
        }

        return false;
    }

    MIRSeparator() {
        super();
    }

    /**
     * Zero small coefficients (adjusting RHS via variable bounds to preserve validity) and apply RHS
     * relaxation to make the cut slightly weaker, accounting for floating-point errors in the derivation.
     *
     * @return the adjusted RHS, or null if the cut must be discarded (required bound is infinite)
     */
    private BigDecimal safePostProcess(final ExpressionsBasedModel model, final Expression cut, BigDecimal cutRHS, final BigDecimal largestCoeff,
            final boolean isGEQ) {

        for (Iterator<Entry<IntIndex, BigDecimal>> it = cut.getLinearEntrySet().iterator(); it.hasNext();) {
            Entry<IntIndex, BigDecimal> entry = it.next();
            BigDecimal coeff = entry.getValue();

            if (!ACCURACY.isSmall(largestCoeff, coeff)) {
                continue;
            }

            Variable var = model.getVariable(entry.getKey().index);

            BigDecimal bound;
            if (isGEQ) {
                bound = coeff.signum() > 0 ? var.getUpperLimit() : var.getLowerLimit();
            } else {
                bound = coeff.signum() > 0 ? var.getLowerLimit() : var.getUpperLimit();
            }

            if (bound == null) {
                return null;
            }

            cutRHS = cutRHS.subtract(coeff.multiply(bound));
            it.remove();
        }

        return cutRHS;
    }

    /**
     * MIR from an equality constraint using the GMI-style ≥ 1 formula.
     */
    private boolean tryEquality(final CutRound round, final Expression constraint) {

        ExpressionsBasedModel model = round.model;
        Optimisation.Result solution = round.solution;

        BigDecimal rhs = constraint.getUpperLimit();
        if (rhs == null) {
            return false;
        }

        BigDecimal fractionality = BigDecimal.valueOf(round.configuration.fractionality);
        BigDecimal effectiveRHS = rhs;

        for (Entry<IntIndex, BigDecimal> entry : constraint.getLinearEntrySet()) {
            int idx = entry.getKey().index;
            Variable variable = model.getVariable(idx);
            BigDecimal coeff = entry.getValue();

            if (!variable.isLowerLimitSet()) {
                return false;
            }
            BigDecimal lower = variable.getLowerLimit();
            BigDecimal value = solution.get(idx);

            if (MIRSeparator.shouldComplement(variable, value, lower)) {
                effectiveRHS = effectiveRHS.subtract(coeff.multiply(variable.getUpperLimit()));
            } else {
                effectiveRHS = effectiveRHS.subtract(coeff.multiply(lower));
            }
        }

        BigDecimal f0 = MIRSeparator.fraction(effectiveRHS);
        if (f0.compareTo(fractionality) < 0 || f0.compareTo(BigMath.ONE.subtract(fractionality)) > 0) {
            return false;
        }
        BigDecimal cf0 = BigMath.ONE.subtract(f0);

        if (this.isTooWeak(MIRSeparator.estimateEfficacy(model, constraint, false, true, solution, effectiveRHS))) {
            return false;
        }

        Expression cut = this.newCut(round);

        BigDecimal cutRHS = BigMath.ONE;
        BigDecimal largestCoeff = BigMath.ONE;

        for (Entry<IntIndex, BigDecimal> entry : constraint.getLinearEntrySet()) {
            int idx = entry.getKey().index;
            Variable variable = model.getVariable(idx);
            BigDecimal origCoeff = entry.getValue();
            BigDecimal value = solution.get(idx);
            BigDecimal lower = variable.getLowerLimit();

            boolean complement = MIRSeparator.shouldComplement(variable, value, lower);
            BigDecimal cj = complement ? origCoeff.negate() : origCoeff;

            BigDecimal mirCoeff;
            if (variable.isInteger()) {

                BigDecimal fj = MIRSeparator.fraction(cj);

                if (fj.compareTo(f0) <= 0) {
                    mirCoeff = MissingMath.divide(fj, f0);
                } else {
                    BigDecimal cfj = BigMath.ONE.subtract(fj);
                    mirCoeff = MissingMath.divide(cfj, cf0);
                }

            } else if (cj.signum() > 0) {
                mirCoeff = MissingMath.divide(cj, f0);
            } else if (cj.signum() < 0) {
                mirCoeff = MissingMath.divide(cj.negate(), cf0);
            } else {
                mirCoeff = BigMath.ZERO;
            }

            BigDecimal bdCoeff;
            if (complement) {
                bdCoeff = mirCoeff.negate();
                cutRHS = cutRHS.subtract(mirCoeff.multiply(variable.getUpperLimit()));
            } else {
                bdCoeff = mirCoeff;
                cutRHS = cutRHS.add(mirCoeff.multiply(lower));
            }

            cut.add(idx, bdCoeff);

            BigDecimal absCoeff = bdCoeff.abs();
            if (absCoeff.compareTo(largestCoeff) > 0) {
                largestCoeff = absCoeff;
            }
        }

        if (cut.getLinearEntrySet().isEmpty()) {
            model.removeExpression(cut.getName());
            return false;
        }

        cutRHS = this.safePostProcess(model, cut, cutRHS, largestCoeff, true);

        if (cutRHS == null || cut.getLinearEntrySet().isEmpty()) {
            model.removeExpression(cut.getName());
            return false;
        }

        cut.lower(cutRHS);

        return this.accept(round, cut);
    }

    /**
     * MIR from an inequality constraint using the proper ≤ ⌊b⌋ formula.
     */
    private boolean tryInequality(final CutRound round, final Expression constraint, final boolean useUpper) {

        ExpressionsBasedModel model = round.model;
        Optimisation.Result solution = round.solution;

        BigDecimal rhs = useUpper ? constraint.getUpperLimit() : constraint.getLowerLimit();
        if (rhs == null) {
            return false;
        }

        BigDecimal fractionality = BigDecimal.valueOf(round.configuration.fractionality);
        boolean negate = !useUpper;

        BigDecimal effectiveRHS = negate ? rhs.negate() : rhs;

        for (Entry<IntIndex, BigDecimal> entry : constraint.getLinearEntrySet()) {
            int idx = entry.getKey().index;
            Variable variable = model.getVariable(idx);
            BigDecimal coeff = negate ? entry.getValue().negate() : entry.getValue();
            BigDecimal value = solution.get(idx);

            if (!variable.isLowerLimitSet()) {
                return false;
            }
            BigDecimal lower = variable.getLowerLimit();

            if (MIRSeparator.shouldComplement(variable, value, lower)) {
                effectiveRHS = effectiveRHS.subtract(coeff.multiply(variable.getUpperLimit()));
            } else {
                effectiveRHS = effectiveRHS.subtract(coeff.multiply(lower));
            }
        }

        BigDecimal f0 = MIRSeparator.fraction(effectiveRHS);
        if (f0.compareTo(fractionality) < 0 || f0.compareTo(BigMath.ONE.subtract(fractionality)) > 0) {
            return false;
        }
        BigDecimal cf0 = BigMath.ONE.subtract(f0);
        BigDecimal floorRHS = MIRSeparator.floor(effectiveRHS);

        if (this.isTooWeak(MIRSeparator.estimateEfficacy(model, constraint, negate, false, solution, effectiveRHS))) {
            return false;
        }

        Expression cut = this.newCut(round);

        BigDecimal cutRHS = floorRHS;
        BigDecimal largestCoeff = BigMath.ZERO;

        for (Entry<IntIndex, BigDecimal> entry : constraint.getLinearEntrySet()) {
            int idx = entry.getKey().index;
            Variable variable = model.getVariable(idx);
            BigDecimal origCoeff = negate ? entry.getValue().negate() : entry.getValue();
            BigDecimal value = solution.get(idx);
            BigDecimal lower = variable.getLowerLimit();

            boolean complement = MIRSeparator.shouldComplement(variable, value, lower);
            BigDecimal cj = complement ? origCoeff.negate() : origCoeff;

            BigDecimal mirCoeff;
            if (variable.isInteger()) {

                BigDecimal fj = MIRSeparator.fraction(cj);

                if (fj.compareTo(f0) <= 0) {
                    mirCoeff = MIRSeparator.floor(cj);
                } else {
                    mirCoeff = MIRSeparator.floor(cj).add(MissingMath.divide(fj.subtract(f0), cf0));
                }

            } else if (cj.signum() < 0) {
                mirCoeff = MissingMath.divide(cj, cf0);
            } else {
                continue;
            }

            BigDecimal bdCoeff;
            if (complement) {
                bdCoeff = mirCoeff.negate();
                cutRHS = cutRHS.subtract(mirCoeff.multiply(variable.getUpperLimit()));
            } else {
                bdCoeff = mirCoeff;
                cutRHS = cutRHS.add(mirCoeff.multiply(lower));
            }

            cut.add(idx, bdCoeff);

            BigDecimal absCoeff = bdCoeff.abs();
            if (absCoeff.compareTo(largestCoeff) > 0) {
                largestCoeff = absCoeff;
            }
        }

        if (cut.getLinearEntrySet().isEmpty()) {
            model.removeExpression(cut.getName());
            return false;
        }

        cutRHS = this.safePostProcess(model, cut, cutRHS, largestCoeff, false);

        if (cutRHS == null || cut.getLinearEntrySet().isEmpty()) {
            model.removeExpression(cut.getName());
            return false;
        }

        cut.upper(cutRHS);

        return this.accept(round, cut);
    }

    @Override
    int generateCuts(final CutRound round) {

        this.beginRound(round);

        ExpressionsBasedModel model = round.model;
        List<Expression> expressions = new ArrayList<>(model.getExpressions());

        for (Expression constraint : expressions) {

            if (!this.isRoomForMore()) {
                break;
            }

            if (constraint.isAnyQuadraticFactorNonZero() || !constraint.isLinearAndAnyInteger()) {
                continue;
            }

            if (!MIRSeparator.hasFractionalData(constraint, model)) {
                continue;
            }

            if (constraint.isEqualityConstraint()) {
                this.tryEquality(round, constraint);
            } else {
                if (constraint.isUpperLimitSet()) {
                    this.tryInequality(round, constraint, true);
                }
                if (this.isRoomForMore() && constraint.isLowerLimitSet()) {
                    this.tryInequality(round, constraint, false);
                }
            }
        }

        return this.endRound(round);
    }

    /**
     * Mixed integer Rounding
     */
    @Override
    CutType type() {
        return CutType.MIXED_INTEGER_ROUNDING;
    }

}
