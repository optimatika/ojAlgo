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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.ojalgo.equation.Equation;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.special.MissingMath;
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
import org.ojalgo.type.keyvalue.EntryPair.KeyedPrimitive;

final class GMISeparator extends NodeSolver.Separator {

    private static final NumberContext COEFFICIENT = NumberContext.of(12).withMode(RoundingMode.CEILING);

    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final NumberContext LIMIT = NumberContext.of(12).withMode(RoundingMode.FLOOR);
    private static final NumberContext PARAMETERS = NumberContext.of(12);
    private static final NumberContext PRECISION = NumberContext.of(12);
    private static final NumberContext SCALE = NumberContext.of(14);

    static final IntegerStrategy.CutConfiguration CONFIGURATION = new IntegerStrategy.CutConfiguration().withIterations(3);

    private boolean[] myCachedIntegers = null;
    private ExpressionsBasedModel.EntityMap myCachedIntegersFor = null;

    GMISeparator(final ExpressionsBasedModel ebm) {
        super(ebm);
    }

    int generateCuts(final UpdatableSolver solver, final Optimisation.Result solution, final CutConfiguration configuration) {

        int nbBefore = model.countExpressions();

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

        int maxCuts = configuration.getMaxCuts(nbProblVars);
        int maxElements = configuration.getMaxElements(nbProblVars);

        List<KeyedPrimitive<String>> acceptedEfficacies = new ArrayList<>();

        for (Equation equation : potentialCuts) {

            String name = "CUT_GMI_" + equation.index + "_" + COUNTER.incrementAndGet();

            if (DEBUG) {
                BasicLogger.debug();
                BasicLogger.debug("Equat: {} {}", name, equation.toString());
                BasicLogger.debug();
            }

            Expression cut = model.newExpression(name);

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
            BigDecimal cSmallest = BigMath.VERY_POSITIVE;
            for (Iterator<Entry<IntIndex, BigDecimal>> iterator = cut.getLinearEntrySet().iterator(); iterator.hasNext();) {
                Entry<IntIndex, BigDecimal> entry = iterator.next();

                BigDecimal cValue = entry.getValue();

                if (!PRECISION.isSmall(cLargest, cValue)) {
                    cSmallest = cSmallest.min(cValue.abs());
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

            BigDecimal cEvaluated = cut.evaluate(solution);
            BigDecimal cDiff = cRHS.subtract(cEvaluated);

            if (DEBUG) {
                BigDecimal cRatio = MissingMath.divide(cLargest, cSmallest);
                BigDecimal cEfficacy = MissingMath.divide(cDiff, cLargest);
                BasicLogger.debug(1, "Largest={}, Smallest={}, Ratio={}, RHS={}, Evaluated={},  Difference={}", cLargest, cSmallest, cRatio, cRHS, cEvaluated,
                        cDiff);
            }

            if (cDiff.signum() <= 0 || configuration.efficacy.isSmall(cLargest, cDiff)) {
                model.removeExpression(name);
                if (DEBUG) {
                    BasicLogger.debug(1, "Efficacy small! {} << {}", cDiff, cLargest);
                }

            } else if (configuration.dynanism.isSmall(cLargest, cSmallest)) {
                model.removeExpression(name);
                if (DEBUG) {
                    BasicLogger.debug(1, "Dynanism large! {} >> {}", cLargest, cSmallest);
                }

            } else if (cut.getLinearEntrySet().size() > maxElements) {
                model.removeExpression(name);
                if (DEBUG) {
                    BasicLogger.debug(1, "Too dense! {} > {}", cut.getLinearEntrySet().size(), maxElements);
                }

            } else if (model.checkSimilarity(cut)) {
                model.removeExpression(name);
                if (DEBUG) {
                    BasicLogger.debug(1, "Cut similar to current constraint!");
                }

            } else {

                cut.enforce(PARAMETERS);
                cut.tighten();

                double normalizedEfficacy = cDiff.doubleValue() / cLargest.doubleValue();

                acceptedEfficacies.add(EntryPair.of(name, normalizedEfficacy));

                if (DEBUG) {
                    BasicLogger.debug(1, "{}", cut);
                    BasicLogger.debug(1, "{} < {}", cut.getLowerLimit(), cut.getLinearEntrySet());
                }

                if (model.options.logger_detailed && model.options.logger_appender != null) {
                    model.options.logger_appender.println("{}: {} < {}", name, cut.getLowerLimit(), cut.getLinearEntrySet());
                }
            }
        }

        if (acceptedEfficacies.size() > maxCuts) {

            acceptedEfficacies.sort(Comparator.comparingDouble(KeyedPrimitive::doubleValue));
            for (int i = 0, limit = acceptedEfficacies.size() - maxCuts; i < limit; i++) {
                model.removeExpression(acceptedEfficacies.get(i).getKey());
            }
        }

        return model.countExpressions() - nbBefore;
    }

}
