/*
 * Copyright 1997-2023 Optimatika
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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.ojalgo.equation.Equation;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.IntermediateSolver;
import org.ojalgo.optimisation.ModelEntity;
import org.ojalgo.optimisation.UpdatableSolver;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.keyvalue.EntryPair;

public final class NodeSolver extends IntermediateSolver {

    private static final NumberContext PRECISION = NumberContext.of(12);
    private static final NumberContext COEFFICIENT = PRECISION.withMode(RoundingMode.CEILING);
    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final boolean DEBUG = true;
    private static final NumberContext DYNANISM = NumberContext.of(8);
    private static final NumberContext LIMIT = PRECISION.withMode(RoundingMode.FLOOR);
    private static final NumberContext PARAMETERS = NumberContext.of(12);
    private static final NumberContext SCALE = NumberContext.of(14);

    NodeSolver(final ExpressionsBasedModel model) {
        super(model);
    }

    boolean generateCuts(final ModelStrategy strategy) {
        boolean retVal = this.generateCuts(strategy, this.getModel());
        if (retVal) {
            this.reset();
        }
        return retVal;
    }

    boolean generateCuts(final ModelStrategy strategy, final ExpressionsBasedModel target) {

        if (!this.isSolved()) {
            return false;
        }

        ExpressionsBasedModel model = this.getModel();
        Result result = this.getResult();

        long nbConstr = model.constraints().count();

        if (this.getSolver() instanceof UpdatableSolver) {
            UpdatableSolver solver = (UpdatableSolver) this.getSolver();

            UpdatableSolver.EntityMap entityMap = solver.getEntityMap();

            if (entityMap != null) {

                int nbProblVars = entityMap.countModelVariables();
                int nbProblInts = strategy.countIntegerVariables();
                int nbSlackVars = entityMap.countSlackVariables();

                boolean[] integers = new boolean[nbProblVars + nbSlackVars];

                for (int i = 0; i < nbProblInts; i++) {
                    int indexInModel = strategy.getIndex(i);
                    int indexInSolver = this.getIndexInSolver(indexInModel);
                    if (indexInSolver >= 0) {
                        integers[indexInSolver] = true;
                    }
                }
                for (int i = 0; i < nbSlackVars; i++) {
                    EntryPair<ModelEntity<?>, ConstraintType> slack = entityMap.getSlack(i);
                    ModelEntity<?> key = slack.getKey();
                    boolean integer = key.isInteger();
                    integers[nbProblVars + i] = integer;
                }

                Collection<Equation> potentialCuts = solver.generateCutCandidates(strategy.getGMICutConfiguration().fractionality, integers);

                for (Equation equation : potentialCuts) {

                    String name = "CUT_GMI_" + equation.index + "_" + COUNTER.incrementAndGet();

                    if (DEBUG) {
                        BasicLogger.debug("Eq: {} {}", name, equation.toString());
                    }

                    Expression cut = target.newExpression(name);

                    cut.lower(equation.getRHS() < PrimitiveMath.ZERO ? BigMath.NEG : BigMath.ONE);

                    for (int j = 0; j < nbProblVars; j++) {
                        int mj = entityMap.indexOf(j);
                        double aj = equation.doubleValue(j);
                        if (!SCALE.isZero(aj)) {
                            BigDecimal AJ = TypeUtils.toBigDecimal(aj, SCALE);
                            if (entityMap.isNegated(j)) {
                                cut.add(mj, AJ.negate());
                            } else {
                                cut.add(mj, AJ);
                            }
                        }
                    }

                    for (int j = 0; j < entityMap.countSlackVariables(); j++) {
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
                        }
                    }

                    if (DEBUG) {
                        BasicLogger.debug("Cut {}: {} < {}", name, cut.getLowerLimit(), cut.getLinearEntrySet());
                    }

                    BigDecimal cRHS = cut.getLowerLimit();

                    // The cut violation is always 1.0
                    // The relative violation is 1.0 relative to the RHS
                    // We only need to check that the RHS is not too large
                    // The violation configuration property is the largest allowed RHS
                    BigDecimal violation = strategy.getGMICutConfiguration().violation;
                    if (cRHS.abs().compareTo(violation) > 0) {
                        target.removeExpression(name);
                        if (DEBUG) {
                            BasicLogger.debug(1, "Violation small! {}", cRHS);
                        }
                        continue;
                    }

                    BigDecimal cLargest = BigMath.ONE;
                    for (Entry<IntIndex, BigDecimal> entry : cut.getLinearEntrySet()) {
                        cLargest = cLargest.max(entry.getValue().abs());
                    }

                    BigDecimal cSmallest = BigMath.VERY_POSITIVE;
                    for (Iterator<Entry<IntIndex, BigDecimal>> iterator = cut.getLinearEntrySet().iterator(); iterator.hasNext();) {
                        Entry<IntIndex, BigDecimal> entry = iterator.next();

                        BigDecimal cValue = entry.getValue();

                        if (!PRECISION.isSmall(cLargest, cValue)) {
                            cSmallest = cSmallest.min(cValue.abs());
                            entry.setValue(COEFFICIENT.enforce(cValue));
                        } else {
                            cRHS = cRHS.subtract(cValue.multiply(result.get(entry.getKey().index)));
                            iterator.remove();
                        }
                    }

                    cRHS = LIMIT.enforce(cRHS);
                    cut.lower(cRHS);

                    if (DEBUG) {
                        BigDecimal cRatio = MissingMath.divide(cLargest, cSmallest);
                        BigDecimal cEvaluated = cut.evaluate(result);
                        BasicLogger.debug(1, "Largest={}, Smallest={}, Ratio={}: {} < {}", cLargest, cSmallest, cRatio, cRHS, cEvaluated);
                    }

                    if (DYNANISM.isSmall(cLargest, cSmallest)) {
                        target.removeExpression(name);
                        if (DEBUG) {
                            BasicLogger.debug(1, "Dynanism large! {} >> {}", cLargest, cSmallest);
                        }
                    } else if (model.checkSimilarity(cut)) {
                        target.removeExpression(name);
                        if (DEBUG) {
                            BasicLogger.debug(1, "Cut similar to current constraint!");
                        }
                    } else {

                        // Accept this cut!
                        cut.enforce(PARAMETERS);
                        cut.tighten();
                        if (DEBUG) {
                            BasicLogger.debug(1, "{}", cut);
                            BasicLogger.debug(1, "{} < {}", cut.getLowerLimit(), cut.getLinearEntrySet());
                        }

                        if (target.options.logger_detailed && target.options.logger_appender != null) {
                            target.options.logger_appender.println("{}: {} < {}", name, cut.getLowerLimit(), cut.getLinearEntrySet());
                        }
                    }
                }
            }
        }

        return nbConstr != model.constraints().count();
    }

}
