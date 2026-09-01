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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.ojalgo.equation.Equation;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelEntity;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.ConstraintType;
import org.ojalgo.optimisation.UpdatableSolver;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.integer.IntegerStrategy.CutConfiguration;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.keyvalue.EntryPair;

final class GMISeparator extends NodeSolver.Separator {

    private static final NumberContext COEFFICIENT = NumberContext.of(12).withMode(RoundingMode.CEILING);

    private static final NumberContext LIMIT = NumberContext.of(12).withMode(RoundingMode.FLOOR);
    private static final NumberContext PARAMETERS = NumberContext.of(12);
    private static final NumberContext PRECISION = NumberContext.of(12);
    private static final NumberContext SCALE = NumberContext.of(14);

    static final IntegerStrategy.CutConfiguration CONFIGURATION = new IntegerStrategy.CutConfiguration().withIterations(3);

    /**
     * The non-zero (index, coefficient) pairs and the right-hand side, coefficients rounded to 10 decimals.
     */
    private static List<Long> signature(final Equation equation, final int nbVariables) {
        List<Long> retVal = new ArrayList<>();
        for (int j = 0; j < nbVariables; j++) {
            double value = equation.doubleValue(j);
            if (value != 0.0) {
                retVal.add(Long.valueOf(j));
                retVal.add(Long.valueOf(Math.round(value * 1.0E10)));
            }
        }
        retVal.add(Long.valueOf(Math.round(equation.getRHS() * 1.0E10)));
        return retVal;
    }

    private boolean[] myCachedIntegers = null;
    private ExpressionsBasedModel.EntityMap myCachedIntegersFor = null;

    /**
     * Signatures (in solver space) of the candidates already built this round. Tableau rows of the same
     * structure give the same Gomory cut; on stein27 more than half of the candidates are duplicates, and
     * building them (BigDecimal coefficients, slack expansion) was the dominant separation cost.
     */
    private final Set<List<Long>> myRoundSignatures = new HashSet<>();

    GMISeparator() {
        super();
    }

    int generateCuts(final ExpressionsBasedModel model, final UpdatableSolver solver, final Optimisation.Result solution,
            final CutConfiguration configuration) {

        ExpressionsBasedModel.EntityMap entityMap = solver.getEntityMap().orElse(null);

        if (entityMap == null) {
            return 0;
        }

        int nbProblVars = entityMap.countModelVariables();
        int nbSlackVars = entityMap.countSlackVariables();

        boolean[] integers;
        if (myCachedIntegersFor == entityMap && myCachedIntegers != null) {
            integers = myCachedIntegers;
        } else {
            integers = entityMap.integers(model);
            myCachedIntegers = integers;
            myCachedIntegersFor = entityMap;
        }

        Collection<Equation> potentialCuts = solver.generateCutCandidates(integers, configuration);

        this.beginRound(model, solution, configuration);
        myRoundSignatures.clear();

        for (Equation equation : potentialCuts) {

            if (!myRoundSignatures.add(GMISeparator.signature(equation, nbProblVars + nbSlackVars))) {
                this.skipAsDuplicate();
                continue;
            }

            Expression cut = this.newCut(model);
            String name = cut.getName();

            if (DEBUG) {
                BasicLogger.debug();
                BasicLogger.debug("Equat: {} {}", name, equation.toString());
                BasicLogger.debug();
            }

            cut.lower(BigDecimal.valueOf(equation.getRHS()));

            for (int j = 0; j < nbProblVars; j++) {
                double aj = equation.doubleValue(j);
                if (!SCALE.isZero(aj)) {

                    int mj = entityMap.indexOf(j);

                    BigDecimal AJ = TypeUtils.toBigDecimal(aj, SCALE);
                    if (entityMap.isNegated(j)) {
                        cut.add(mj, AJ.negate());
                    } else {
                        cut.add(mj, AJ);
                    }

                    if (DEBUG) {
                        BasicLogger.debug("Var   {} =->> Cut {}: {} < {}", model.getVariable(mj), name, cut.getLowerLimit(), cut.getLinearEntrySet());
                    }
                }
            }

            for (int j = 0; j < nbSlackVars; j++) {
                double aj = equation.doubleValue(nbProblVars + j);
                if (!SCALE.isZero(aj)) {

                    EntryPair<ModelEntity<?>, ConstraintType> pair = entityMap.getSlack(j);

                    ModelEntity<?> entity = pair.getKey();
                    ConstraintType type = pair.getValue();
                    BigDecimal coefficient = TypeUtils.toBigDecimal(aj, SCALE);
                    BigDecimal adjusted = entity.adjust(coefficient);

                    if (ConstraintType.LOWER.equals(type)) {

                        BigDecimal factor = adjusted;
                        BigDecimal limit = entity.getLowerLimit();

                        BigDecimal shift = limit.multiply(factor);
                        cut.shift(shift);

                        entity.addTo(cut, factor);
                    }

                    if (ConstraintType.UPPER.equals(type)) {

                        BigDecimal factor = adjusted.negate();
                        BigDecimal limit = entity.getUpperLimit();

                        BigDecimal shift = limit.multiply(factor);
                        cut.shift(shift);

                        entity.addTo(cut, factor);
                    }

                    if (DEBUG) {
                        BasicLogger.debug("Slack {} {} =->> Cut {}: {} < {}", type, entity, name, cut.getLowerLimit(), cut.getLinearEntrySet());
                    }
                }
            }

            BigDecimal cRHS = cut.getLowerLimit();

            BigDecimal violation = configuration.violation;
            if (cRHS.abs().compareTo(violation) > 0) {
                model.removeExpression(name);
                if (DEBUG) {
                    BasicLogger.debug(1, "Violation small! {}", cRHS);
                }
                continue;
            }

            BigDecimal cLargest = BigMath.ONE;
            for (Entry<IntIndex, BigDecimal> entry : cut.getLinearEntrySet()) {
                cLargest = cLargest.max(entry.getValue().abs());
            }

            boolean discardCut = false;
            for (Iterator<Entry<IntIndex, BigDecimal>> iterator = cut.getLinearEntrySet().iterator(); iterator.hasNext();) {
                Entry<IntIndex, BigDecimal> entry = iterator.next();

                BigDecimal cValue = entry.getValue();

                if (!PRECISION.isSmall(cLargest, cValue)) {
                    entry.setValue(COEFFICIENT.enforce(cValue));
                } else {
                    Variable var = model.getVariable(entry.getKey().index);
                    BigDecimal bound = cValue.signum() > 0 ? var.getUpperLimit() : var.getLowerLimit();
                    if (bound == null) {
                        discardCut = true;
                        break;
                    }
                    cRHS = cRHS.subtract(cValue.multiply(bound));
                    iterator.remove();
                }
            }

            if (discardCut) {
                model.removeExpression(name);
                continue;
            }

            cRHS = LIMIT.enforce(cRHS);
            cut.lower(cRHS);

            if (this.accept(model, cut)) {

                cut.enforce(PARAMETERS);
                cut.tighten();

                if (DEBUG) {
                    BasicLogger.debug(1, "{}", cut);
                    BasicLogger.debug(1, "{} < {}", cut.getLowerLimit(), cut.getLinearEntrySet());
                }

                if (model.options.logger_detailed && model.options.logger_appender != null) {
                    model.options.logger_appender.println("{}: {} < {}", name, cut.getLowerLimit(), cut.getLinearEntrySet());
                }
            }
        }

        return this.endRound(model);
    }

    /**
     * Gomory Mixed integer
     */
    @Override
    String type() {
        return "GM";
    }

}
