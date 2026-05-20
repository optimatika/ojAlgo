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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.structure.Structure1D.IntIndex;

/**
 * Generates Mixed Integer Rounding (MIR) cuts from original model constraints by fixing continuous
 * variables at their LP relaxation values. This reduces each mixed constraint to a pure integer
 * inequality, then applies the standard MIR rounding procedure.
 * <p>
 * Given a constraint &Sigma; a<sub>j</sub>x<sub>j</sub> + &Sigma; c<sub>k</sub>y<sub>k</sub> &le; b
 * where x<sub>j</sub> are integer and y<sub>k</sub> are continuous, substituting y<sub>k</sub> = y<sub>k</sub><sup>LP</sup>
 * gives the integer relaxation &Sigma; a<sub>j</sub>x<sub>j</sub> &le; b&prime; where
 * b&prime; = b &minus; &Sigma; c<sub>k</sub>y<sub>k</sub><sup>LP</sup>. MIR is then applied to the
 * shifted (zero lower bound) form, producing &Sigma; &psi;(a<sub>j</sub>)(x<sub>j</sub> &minus; l<sub>j</sub>) &le; &lfloor;b&Prime;&rfloor;
 * where b&Prime; = b&prime; &minus; &Sigma; a<sub>j</sub>l<sub>j</sub>.
 */
public final class MIRCutGenerator implements ModelCutGenerator {

    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final double MIN_FRACTIONALITY = 0.01;
    private static final double MAX_SLACK = 0.5;
    private static final int MAX_CUTS_PER_ROUND = 50;

    public MIRCutGenerator() {
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

            if (!constraint.isLinearAndAnyInteger()) {
                continue;
            }

            if (constraint.isUpperLimitSet()) {
                Expression cut = this.tryGenerateMIR(constraint, model, result, true);
                if (cut != null && CutQualityFilter.acceptCut(model, cut, result)) {
                    cutsAdded++;
                }
            }

            if (cutsAdded < MAX_CUTS_PER_ROUND && constraint.isLowerLimitSet()) {
                Expression cut = this.tryGenerateMIR(constraint, model, result, false);
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
    private Expression tryGenerateMIR(final Expression constraint, final ExpressionsBasedModel model, final Optimisation.Result result,
            final boolean upper) {

        double b = upper ? constraint.getUpperLimit().doubleValue() : -constraint.getLowerLimit().doubleValue();

        // Check slack
        double activity = constraint.evaluate(result).doubleValue();
        double slack = upper ? b - activity : activity + b; // for lower: activity - lowerLimit = activity - (-b)
        if (slack < -1e-6 || slack > MAX_SLACK) {
            return null;
        }

        // Fix continuous variables at LP values: effective_b = b - sum(ck * yk_LP) for continuous k
        // Then shift integer variables to zero lower bound: b'' = effective_b - sum(aj * lj)
        double bEffective = b;
        int integerCount = 0;

        for (Entry<IntIndex, BigDecimal> entry : constraint.getLinearEntrySet()) {
            IntIndex idx = entry.getKey();
            Variable var = model.getVariable(idx);
            double coeff = upper ? entry.getValue().doubleValue() : -entry.getValue().doubleValue();

            if (var.isInteger()) {
                integerCount++;
                if (!var.isLowerLimitSet()) {
                    return null;
                }
                bEffective -= coeff * var.getLowerLimit().doubleValue();
            } else {
                bEffective -= coeff * result.doubleValue(idx.index);
            }
        }

        if (integerCount < 1) {
            return null;
        }

        double f0 = bEffective - Math.floor(bEffective);
        if (f0 < MIN_FRACTIONALITY || f0 > 1.0 - MIN_FRACTIONALITY) {
            return null;
        }

        double oneMinusF0 = 1.0 - f0;
        double floorB = Math.floor(bEffective);

        // Compute MIR coefficients for integer variables only, check violation in shifted space
        int nbTerms = constraint.countLinearFactors();
        IntIndex[] cutIndices = new IntIndex[nbTerms];
        double[] cutCoeffs = new double[nbTerms];
        int cutSize = 0;
        double cutLHSshifted = 0.0;

        for (Entry<IntIndex, BigDecimal> entry : constraint.getLinearEntrySet()) {
            IntIndex idx = entry.getKey();
            Variable var = model.getVariable(idx);

            if (!var.isInteger()) {
                continue;
            }

            double aj = upper ? entry.getValue().doubleValue() : -entry.getValue().doubleValue();
            double xjLP = result.doubleValue(idx.index);
            double lj = var.getLowerLimit().doubleValue();

            double fj = aj - Math.floor(aj);
            double mirCoeff;
            if (fj <= f0 + 1e-10) {
                mirCoeff = Math.floor(aj);
            } else {
                mirCoeff = Math.floor(aj) + (fj - f0) / oneMinusF0;
            }

            if (Math.abs(mirCoeff) > 1e-12) {
                cutIndices[cutSize] = idx;
                cutCoeffs[cutSize] = mirCoeff;
                cutLHSshifted += mirCoeff * (xjLP - lj);
                cutSize++;
            }
        }

        if (cutSize < 1 || cutLHSshifted <= floorB + 1e-6) {
            return null;
        }

        // Build cut in original variable space
        // Shifted: sum(mirCoeff * (xj - lj)) <= floor(b'')
        // Original: sum(mirCoeff * xj) <= floor(b'') + sum(mirCoeff * lj)
        double rhs = floorB;
        for (int i = 0; i < cutSize; i++) {
            Variable var = model.getVariable(cutIndices[i]);
            rhs += cutCoeffs[i] * var.getLowerLimit().doubleValue();
        }

        String name = "CUT_MIR_" + COUNTER.incrementAndGet();
        Expression cut = model.newExpression(name);

        if (upper) {
            for (int i = 0; i < cutSize; i++) {
                cut.add(cutIndices[i].index, BigDecimal.valueOf(cutCoeffs[i]));
            }
            cut.upper(BigDecimal.valueOf(rhs));
        } else {
            // Negate back for the >= direction
            for (int i = 0; i < cutSize; i++) {
                cut.add(cutIndices[i].index, BigDecimal.valueOf(-cutCoeffs[i]));
            }
            cut.lower(BigDecimal.valueOf(-rhs));
        }

        return cut;
    }

}
