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
package org.ojalgo.optimisation;

import static org.ojalgo.function.constant.BigMath.ONE;
import static org.ojalgo.function.constant.BigMath.ZERO;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.ojalgo.function.constant.BigMath;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.context.NumberContext;

public abstract class Presolvers {

    public static final ExpressionsBasedModel.Presolver INTEGER = new ExpressionsBasedModel.Presolver(70) {

        @Override
        public boolean simplify(final Expression expression, final Set<IntIndex> remaining, final BigDecimal lower, final BigDecimal upper,
                final NumberContext precision) {

            expression.doIntegerRounding(remaining, lower, upper);

            return false;
        }
    };

    /**
     * If the expression is linear and contributes to the objective function, then the contributions are
     * transferred to the variables and the weight of the expression set to null.
     */
    public static final ExpressionsBasedModel.ExpressionAnalyser LINEAR_OBJECTIVE = new ExpressionsBasedModel.ExpressionAnalyser(10) {

        @Override
        public void simplify(final Expression target, final ExpressionsBasedModel model) {

            BigDecimal exprWeight = target.getContributionWeight();

            Variable tmpVariable;
            BigDecimal varWeight;
            BigDecimal contribution;
            for (Entry<IntIndex, BigDecimal> entry : target.getLinearEntrySet()) {
                tmpVariable = target.resolve(entry.getKey());

                varWeight = tmpVariable.getContributionWeight();
                contribution = exprWeight.multiply(entry.getValue());

                varWeight = varWeight != null ? varWeight.add(contribution) : contribution;

                tmpVariable.weight(varWeight);
            }

            target.weight(null);
        }

        @Override
        protected boolean isApplicable(final Expression target) {
            return target.isObjective() && target.isFunctionLinear();
        }

    };

    /**
     * Calculates the min and max value of this expression based on the variables' individual bounds. Then
     * compares those with the expression's bounds.
     */
    public static final ExpressionsBasedModel.Presolver REDUNDANT_CONSTRAINT = new ExpressionsBasedModel.Presolver(90) {

        @Override
        public boolean simplify(final Expression expression, final Set<IntIndex> remaining, final BigDecimal lower, final BigDecimal upper,
                final NumberContext precision) {

            if (remaining.isEmpty()) {
                expression.setRedundant();
                return false;
            }

            if (expression.isFunctionLinear()) {

                BigDecimal min = BigMath.ZERO;
                BigDecimal max = BigMath.ZERO;

                for (IntIndex index : remaining) {
                    Variable variable = expression.resolve(index);

                    BigDecimal coefficient = expression.get(index);

                    if (coefficient.signum() < 0) {
                        if (max != null) {
                            if (variable.isLowerLimitSet()) {
                                max = max.add(coefficient.multiply(variable.getLowerLimit()));
                            } else {
                                max = null;
                            }
                        }
                        if (min != null) {
                            if (variable.isUpperLimitSet()) {
                                min = min.add(coefficient.multiply(variable.getUpperLimit()));
                            } else {
                                min = null;
                            }
                        }
                    } else if (coefficient.signum() > 0) {
                        if (max != null) {
                            if (variable.isUpperLimitSet()) {
                                max = max.add(coefficient.multiply(variable.getUpperLimit()));
                            } else {
                                max = null;
                            }
                        }
                        if (min != null) {
                            if (variable.isLowerLimitSet()) {
                                min = min.add(coefficient.multiply(variable.getLowerLimit()));
                            } else {
                                min = null;
                            }
                        }
                    }
                }

                boolean upperRedundant = false;
                if (upper != null) {
                    if (min != null && min.compareTo(upper) > 0 && precision.common(upper, min) == null) {
                        expression.setInfeasible();
                    } else if (max != null && max.compareTo(upper) <= 0) {
                        upperRedundant = true;
                    }
                } else {
                    upperRedundant = true;
                }

                boolean lowerRedundant = false;
                if (lower != null) {
                    if (max != null && max.compareTo(lower) < 0 && precision.common(lower, max) == null) {
                        expression.setInfeasible();
                    } else if (min != null && min.compareTo(lower) >= 0) {
                        lowerRedundant = true;
                    }
                } else {
                    lowerRedundant = true;
                }

                if (lowerRedundant && upperRedundant) {
                    expression.setRedundant();
                }
            }

            return false;
        }

    };

    /**
     * Verifies that the variable is actually referenced/used in some expression. If not then that variable
     * can either be fixed or marked as unbounded.
     *
     * <pre>
     * 2019-02-15: Turned this off. Very slow for large models
     * 2019-02-22: Turned this on again, different implementation
     * </pre>
     */
    public static final ExpressionsBasedModel.VariableAnalyser UNREFERENCED = new ExpressionsBasedModel.VariableAnalyser(30) {

        @Override
        public void simplify(final Variable variable, final ExpressionsBasedModel model) {

            if (!model.isReferenced(variable)) {

                if (variable.isObjective()) {
                    int weightSignum = variable.getContributionWeight().signum();

                    Optimisation.Sense sense = model.getOptimisationSense();

                    if (sense == Optimisation.Sense.MAX && weightSignum == -1 || sense == Optimisation.Sense.MIN && weightSignum == 1) {
                        if (variable.isLowerLimitSet()) {
                            variable.setFixed(variable.getLowerLimit());
                        } else {
                            variable.setUnbounded(true);
                        }
                    } else if (sense == Optimisation.Sense.MAX && weightSignum == 1 || sense == Optimisation.Sense.MIN && weightSignum == -1) {
                        if (variable.isUpperLimitSet()) {
                            variable.setFixed(variable.getUpperLimit());
                        } else {
                            variable.setUnbounded(true);
                        }
                    }

                } else if (!variable.isValueSet()) {
                    variable.setValue(BigMath.ZERO);
                    variable.level(variable.getValue());
                }
            }
        }

        @Override
        protected boolean isApplicable(final Variable target) {
            return true;
        }

    };

    /**
     * Looks for constraint expressions with 0, 1 or 2 non-fixed variables. Transfers the constraints of the
     * expressions to the variables and then (if possible) marks the expression as redundant.
     */
    public static final ExpressionsBasedModel.Presolver ZERO_ONE_TWO = new ExpressionsBasedModel.Presolver(50) {

        @Override
        public boolean simplify(final Expression expression, final Set<IntIndex> remaining, final BigDecimal lower, final BigDecimal upper,
                final NumberContext precision) {

            switch (remaining.size()) {
                case 0:
                    return Presolvers.doCase0(expression, remaining, lower, upper, precision);
                case 1:
                    return Presolvers.doCase1(expression, remaining, lower, upper, precision);
                case 2:
                    return Presolvers.doCase2(expression, remaining, lower, upper, precision);
                //            case 3:
                //            case 4:
                //            case 5:
                //                return Presolvers.doCase3(expression, remaining, lower, upper, precision);
                default:
                    return Presolvers.doCaseN(expression, remaining, lower, upper, precision);
            }
        }

    };

    private static final MathContext LOWER = NumberContext.ofMath(MathContext.DECIMAL128).withMode(RoundingMode.FLOOR).getMathContext();
    private static final MathContext SIMILARITY = NumberContext.of(12).getMathContext();
    private static final MathContext UPPER = NumberContext.ofMath(MathContext.DECIMAL128).withMode(RoundingMode.CEILING).getMathContext();

    /**
     * Checks if the potential {@link Expression} is similar to any in the current collection. Only works for
     * linear expressions. If the potential expression to check has any quadratic term, nothing more is
     * checked, and false is returned.
     *
     * @return true, if the potential {@link Expression} is found to be similar and marked as redundant by
     *         this method.
     */
    public static boolean checkSimilarity(final Collection<Expression> current, final Expression potential) {

        if (potential.isConstraint() && !potential.isRedundant() && !potential.isAnyQuadraticFactorNonZero()) {

            Set<IntIndex> potentialLinearKeySet = potential.getLinearKeySet();

            for (Expression expression : current) {

                if (expression.isConstraint() && !expression.isRedundant()) {
                    Set<IntIndex> currentLinearKeySet = expression.getLinearKeySet();

                    if (!expression.getName().equals(potential.getName()) && currentLinearKeySet.equals(potentialLinearKeySet)) {

                        BigDecimal fctVal = null;
                        BigDecimal tmpVal = null;

                        for (IntIndex index : currentLinearKeySet) {

                            tmpVal = expression.get(index).divide(potential.get(index), SIMILARITY);

                            if (fctVal == null) {
                                fctVal = tmpVal;
                            } else if (tmpVal.compareTo(fctVal) != 0) {
                                fctVal = null;
                                break;
                            }
                        }

                        if (fctVal != null) {

                            // BasicLogger.debug("Match! {}", fctVal);
                            // BasicLogger.debug("Ref: {}", refExpression);
                            // BasicLogger.debug("Sub: {}", subExpression);

                            boolean pos = fctVal.signum() == 1;

                            BigDecimal refLo = expression.getLowerLimit();
                            BigDecimal refUp = expression.getUpperLimit();

                            BigDecimal subLo = pos ? potential.getLowerLimit() : potential.getUpperLimit();
                            BigDecimal subUp = pos ? potential.getUpperLimit() : potential.getLowerLimit();

                            if (fctVal.compareTo(ONE) != 0) {
                                if (subLo != null) {
                                    subLo = subLo.multiply(fctVal);
                                }
                                if (subUp != null) {
                                    subUp = subUp.multiply(fctVal);
                                }
                            }

                            if (subLo != null) {
                                if (refLo != null) {
                                    expression.lower(subLo.max(refLo));
                                } else {
                                    expression.lower(subLo);
                                }
                            }

                            if (subUp != null) {
                                if (refUp != null) {
                                    expression.upper(subUp.min(refUp));
                                } else {
                                    expression.upper(subUp);
                                }
                            }

                            // BasicLogger.debug("Redundant: {} <<= {}", subExpression, refExpression);
                            potential.setRedundant();
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public static boolean reduce(final Collection<Expression> expressions) {
        boolean retVal = false;
        for (Expression expression : expressions) {
            retVal |= Presolvers.checkSimilarity(expressions, expression);
        }
        return retVal;
    }

    /**
     * General activity-based bound propagation. Computes the total contribution range (sumMin/sumMax) over
     * all free variables, then for each variable subtracts its own contribution to derive implied bounds from
     * the remaining variables. Generalises the logic in doCase2 to any number of variables.
     * <p>
     * Although it seems correct, applying this pre-solver caused numerical issues in the solvers. Will have
     * to wait
     */
    static boolean doBoundPropagation(final Expression expression, final Set<IntIndex> remaining, final BigDecimal lower, final BigDecimal upper,
            final NumberContext precision) {

        int n = remaining.size();

        Variable[] variables = new Variable[n];
        BigDecimal[] factors = new BigDecimal[n];
        boolean[] neg = new boolean[n];
        BigDecimal[] contrMin = new BigDecimal[n];
        BigDecimal[] contrMax = new BigDecimal[n];

        BigDecimal sumMin = ZERO;
        BigDecimal sumMax = ZERO;

        int idx = 0;
        for (IntIndex index : remaining) {
            variables[idx] = expression.resolve(index);
            factors[idx] = expression.get(index);
            neg[idx] = factors[idx].signum() < 0;

            BigDecimal lo = variables[idx].getLowerLimit();
            BigDecimal up = variables[idx].getUpperLimit();

            if (neg[idx]) {
                contrMin[idx] = up != null ? factors[idx].multiply(up) : null;
                contrMax[idx] = lo != null ? factors[idx].multiply(lo) : null;
            } else {
                contrMin[idx] = lo != null ? factors[idx].multiply(lo) : null;
                contrMax[idx] = up != null ? factors[idx].multiply(up) : null;
            }

            sumMin = sumMin != null && contrMin[idx] != null ? sumMin.add(contrMin[idx]) : null;
            sumMax = sumMax != null && contrMax[idx] != null ? sumMax.add(contrMax[idx]) : null;

            idx++;
        }

        if (lower != null && sumMax != null && precision.isLessThan(lower, sumMax)) {
            expression.setInfeasible();
            return false;
        }

        if (upper != null && sumMin != null && precision.isMoreThan(upper, sumMin)) {
            expression.setInfeasible();
            return false;
        }

        boolean upperRedundant = upper == null || sumMax != null && sumMax.compareTo(upper) <= 0;
        boolean lowerRedundant = lower == null || sumMin != null && sumMin.compareTo(lower) >= 0;

        if (upperRedundant && lowerRedundant) {
            expression.setRedundant();
            return false;
        }

        boolean didFix = false;

        for (int i = 0; i < n; i++) {

            BigDecimal otherMax = sumMax != null && contrMax[i] != null ? sumMax.subtract(contrMax[i]) : null;
            BigDecimal otherMin = sumMin != null && contrMin[i] != null ? sumMin.subtract(contrMin[i]) : null;

            BigDecimal allowedMin = contrMin[i];
            BigDecimal allowedMax = contrMax[i];

            if ((lower != null) && (otherMax != null)) {
                BigDecimal limit = lower.subtract(otherMax);
                allowedMin = allowedMin != null ? allowedMin.max(limit) : limit;
            }

            if ((upper != null) && (otherMin != null)) {
                BigDecimal limit = upper.subtract(otherMin);
                allowedMax = allowedMax != null ? allowedMax.min(limit) : limit;
            }

            BigDecimal lowerNew = variables[i].getLowerLimit();
            BigDecimal upperNew = variables[i].getUpperLimit();

            if (allowedMin != null) {
                if (neg[i]) {
                    upperNew = allowedMin.divide(factors[i], UPPER);
                } else {
                    lowerNew = allowedMin.divide(factors[i], LOWER);
                }
            }

            if (allowedMax != null) {
                if (neg[i]) {
                    lowerNew = allowedMax.divide(factors[i], LOWER);
                } else {
                    upperNew = allowedMax.divide(factors[i], UPPER);
                }
            }

            if (lowerNew != null && upperNew != null) {
                BigDecimal level = precision.common(lowerNew, upperNew);
                if (level != null) {
                    lowerNew = level;
                    upperNew = level;
                    variables[i].setFixed(level);
                    didFix = true;
                }
            }

            if (variables[i].isInteger()) {
                if (lowerNew != null) {
                    lowerNew = lowerNew.setScale(0, RoundingMode.CEILING);
                }
                if (upperNew != null) {
                    upperNew = upperNew.setScale(0, RoundingMode.FLOOR);
                }
            }

            if (lowerNew != null && upperNew != null) {
                if (lowerNew.compareTo(upperNew) > 0) {
                    BigDecimal level = precision.common(lowerNew, upperNew);
                    if (level != null) {
                        variables[i].setFixed(level);
                        didFix = true;
                        continue;
                    }
                    expression.setInfeasible();
                    return false;
                }
                BigDecimal level = precision.common(lowerNew, upperNew);
                if (level != null) {
                    variables[i].setFixed(level);
                    didFix = true;
                    continue;
                }
            }

            variables[i].lower(lowerNew).upper(upperNew);
        }

        return didFix;
    }

    /**
     * This constraint expression has 0 remaining free variable. It is entirely redundant.
     */
    static boolean doCase0(final Expression expression, final Set<IntIndex> remaining, final BigDecimal lower, final BigDecimal upper,
            final NumberContext precision) {

        expression.setRedundant();

        if (lower != null && precision.isMoreThan(ZERO, lower)) {
            expression.setInfeasible();
        }
        if (upper != null && precision.isLessThan(ZERO, upper)) {
            expression.setInfeasible();
        }

        return false;
    }

    /**
     * This constraint expression has 1 remaining free variable. The lower/upper limits can be transferred to
     * that variable, and the expression marked as redundant.
     */
    static boolean doCase1(final Expression expression, final Set<IntIndex> remaining, final BigDecimal lower, final BigDecimal upper,
            final NumberContext precision) {

        expression.setRedundant();

        IntIndex index = remaining.iterator().next();
        Variable variable = expression.resolve(index);
        BigDecimal factor = expression.get(index);
        boolean neg = factor.signum() == -1;
        BigDecimal lowerOld = variable.getLowerLimit();
        BigDecimal upperOld = variable.getUpperLimit();

        if (expression.isEqualityConstraint()) {
            // Simple case when already equality constraint, just check feasibility and fix the variable

            BigDecimal solution = BigMath.DIVIDE.invoke(upper, factor);

            if (!variable.validate(solution, precision, null)) {
                expression.setInfeasible();
                return false;
            }
            if (!variable.isFixed()) {

                variable.setFixed(solution);
                return true;
            }
            if (precision.common(solution, variable.getValue()) == null) {
                expression.setInfeasible();
            }

            return false;

        }
        BigDecimal lowerCand;
        BigDecimal upperCand;
        if (neg) {
            lowerCand = upper != null ? upper.divide(factor, LOWER) : null;
            upperCand = lower != null ? lower.divide(factor, UPPER) : null;
        } else {
            lowerCand = lower != null ? lower.divide(factor, LOWER) : null;
            upperCand = upper != null ? upper.divide(factor, UPPER) : null;
        }

        BigDecimal lowerNew;
        if (lowerOld != null) {
            if (lowerCand != null) {
                lowerNew = lowerOld.max(lowerCand);
            } else {
                lowerNew = lowerOld;
            }
        } else {
            lowerNew = lowerCand;
        }

        BigDecimal upperNew;
        if (upperOld != null) {
            if (upperCand != null) {
                upperNew = upperOld.min(upperCand);
            } else {
                upperNew = upperOld;
            }
        } else {
            upperNew = upperCand;
        }

        if (lowerNew != null && upperNew != null) {
            BigDecimal level = precision.common(lowerNew, upperNew);
            if (level != null) {
                lowerNew = level;
                upperNew = level;
                variable.setFixed(level);
            }
        }

        if (lowerNew != null && variable.isInteger()) {
            lowerNew = lowerNew.setScale(0, RoundingMode.CEILING);
        }
        if (upperNew != null && variable.isInteger()) {
            upperNew = upperNew.setScale(0, RoundingMode.FLOOR);
        }

        if (lowerNew != null && upperNew != null) {

            if (lowerNew.compareTo(upperNew) > 0) {
                BigDecimal level = precision.common(lowerNew, upperNew);
                if (level != null) {
                    variable.setFixed(level);
                    return true;
                }
                expression.setInfeasible();
                return false;
            }

            BigDecimal level = precision.common(lowerNew, upperNew);
            if (level != null) {
                variable.setFixed(level);
                return true;
            }
        }

        variable.lower(lowerNew).upper(upperNew);
        return false;
    }

    /**
     * Checks if bounds on either of the variables (together with the expressions's bounds) implies tighter
     * bounds on the other variable.
     */
    static boolean doCase2(final Expression expression, final Set<IntIndex> remaining, final BigDecimal lower, final BigDecimal upper,
            final NumberContext precision) {

        Iterator<IntIndex> iterator = remaining.iterator();

        Variable variableA = expression.resolve(iterator.next());
        BigDecimal factorA = expression.get(variableA);
        boolean negA = factorA.signum() == -1;
        BigDecimal lowerOldA = variableA.getLowerLimit();
        BigDecimal upperOldA = variableA.getUpperLimit();
        BigDecimal contrMinA;
        BigDecimal contrMaxA;
        if (negA) {
            contrMinA = upperOldA != null ? factorA.multiply(upperOldA) : null;
            contrMaxA = lowerOldA != null ? factorA.multiply(lowerOldA) : null;
        } else {
            contrMinA = lowerOldA != null ? factorA.multiply(lowerOldA) : null;
            contrMaxA = upperOldA != null ? factorA.multiply(upperOldA) : null;
        }

        Variable variableB = expression.resolve(iterator.next());
        BigDecimal factorB = expression.get(variableB);
        boolean negB = factorB.signum() == -1;
        BigDecimal lowerOldB = variableB.getLowerLimit();
        BigDecimal upperOldB = variableB.getUpperLimit();
        BigDecimal contrMinB;
        BigDecimal contrMaxB;
        if (negB) {
            contrMinB = upperOldB != null ? factorB.multiply(upperOldB) : null;
            contrMaxB = lowerOldB != null ? factorB.multiply(lowerOldB) : null;
        } else {
            contrMinB = lowerOldB != null ? factorB.multiply(lowerOldB) : null;
            contrMaxB = upperOldB != null ? factorB.multiply(upperOldB) : null;
        }

        if (lower != null && contrMaxA != null && contrMaxB != null && precision.isLessThan(lower, contrMaxA.add(contrMaxB))) {
            expression.setInfeasible();
            return false;
        }

        if (upper != null && contrMinA != null && contrMinB != null && precision.isMoreThan(upper, contrMinA.add(contrMinB))) {
            expression.setInfeasible();
            return false;
        }

        BigDecimal allowedMinA = contrMinA;
        BigDecimal allowedMaxA = contrMaxA;
        BigDecimal allowedMinB = contrMinB;
        BigDecimal allowedMaxB = contrMaxB;

        if (lower != null) {

            if (contrMaxB != null) {
                BigDecimal limit = lower.subtract(contrMaxB);
                if (contrMinA != null) {
                    allowedMinA = contrMinA.max(limit);
                } else {
                    allowedMinA = limit;
                }
            }

            if (contrMaxA != null) {
                BigDecimal limit = lower.subtract(contrMaxA);
                if (contrMinB != null) {
                    allowedMinB = contrMinB.max(limit);
                } else {
                    allowedMinB = limit;
                }
            }
        }

        if (upper != null) {

            if (contrMinB != null) {
                BigDecimal limit = upper.subtract(contrMinB);
                if (contrMaxA != null) {
                    allowedMaxA = contrMaxA.min(limit);
                } else {
                    allowedMaxA = limit;
                }
            }

            if (contrMinA != null) {
                BigDecimal limit = upper.subtract(contrMinA);
                if (contrMaxB != null) {
                    allowedMaxB = contrMaxB.min(limit);
                } else {
                    allowedMaxB = limit;
                }
            }
        }

        BigDecimal lowerNewA = lowerOldA;
        BigDecimal upperNewA = upperOldA;
        BigDecimal lowerNewB = lowerOldB;
        BigDecimal upperNewB = upperOldB;

        if (allowedMinA != null) {
            if (negA) {
                upperNewA = allowedMinA.divide(factorA, UPPER);
            } else {
                lowerNewA = allowedMinA.divide(factorA, LOWER);
            }
        }

        if (allowedMaxA != null) {
            if (negA) {
                lowerNewA = allowedMaxA.divide(factorA, LOWER);
            } else {
                upperNewA = allowedMaxA.divide(factorA, UPPER);
            }
        }

        if (allowedMinB != null) {
            if (negB) {
                upperNewB = allowedMinB.divide(factorB, UPPER);
            } else {
                lowerNewB = allowedMinB.divide(factorB, LOWER);
            }
        }

        if (allowedMaxB != null) {
            if (negB) {
                lowerNewB = allowedMaxB.divide(factorB, LOWER);
            } else {
                upperNewB = allowedMaxB.divide(factorB, UPPER);
            }
        }

        if (lowerNewA != null && upperNewA != null) {
            BigDecimal level = precision.common(lowerNewA, upperNewA);
            if (level != null) {
                lowerNewA = level;
                upperNewA = level;
                variableA.setFixed(level);
            }
        }

        if (lowerNewB != null && upperNewB != null) {
            BigDecimal level = precision.common(lowerNewB, upperNewB);
            if (level != null) {
                lowerNewB = level;
                upperNewB = level;
                variableB.setFixed(level);
            }
        }

        if (variableA.isInteger()) {
            if (lowerNewA != null) {
                lowerNewA = lowerNewA.setScale(0, RoundingMode.CEILING);
            }
            if (upperNewA != null) {
                upperNewA = upperNewA.setScale(0, RoundingMode.FLOOR);
            }
        }

        if (variableB.isInteger()) {
            if (lowerNewB != null) {
                lowerNewB = lowerNewB.setScale(0, RoundingMode.CEILING);
            }
            if (upperNewB != null) {
                upperNewB = upperNewB.setScale(0, RoundingMode.FLOOR);
            }
        }

        if (lowerNewA != null && upperNewA != null) {
            if (lowerNewA.compareTo(upperNewA) > 0) {
                BigDecimal level = precision.common(lowerNewA, upperNewA);
                if (level != null) {
                    variableA.setFixed(level);
                } else {
                    expression.setInfeasible();
                    return false;
                }
            } else {
                BigDecimal level = precision.common(lowerNewA, upperNewA);
                if (level != null) {
                    variableA.setFixed(level);
                } else {
                    variableA.lower(lowerNewA).upper(upperNewA);
                }
            }
        } else {
            variableA.lower(lowerNewA).upper(upperNewA);
        }

        if (lowerNewB != null && upperNewB != null) {
            if (lowerNewB.compareTo(upperNewB) > 0) {
                BigDecimal level = precision.common(lowerNewB, upperNewB);
                if (level != null) {
                    variableB.setFixed(level);
                } else {
                    expression.setInfeasible();
                    return false;
                }
            } else {
                BigDecimal level = precision.common(lowerNewB, upperNewB);
                if (level != null) {
                    variableB.setFixed(level);
                } else {
                    variableB.lower(lowerNewB).upper(upperNewB);
                }
            }
        } else {
            variableB.lower(lowerNewB).upper(upperNewB);
        }

        return variableA.isFixed() || variableB.isFixed();
    }

    /**
     * Expressions with at least 3 free variables: full activity-based bound propagation.
     */
    static boolean doCase3(final Expression expression, final Set<IntIndex> remaining, final BigDecimal lower, final BigDecimal upper,
            final NumberContext precision) {
        boolean result = Presolvers.doBoundPropagation(expression, remaining, lower, upper, precision);
        return result || Presolvers.doCaseN(expression, remaining, lower, upper, precision);
    }

    /**
     * Checks the sign of the limits and the sign of the expression parameters to deduce variables that in
     * fact can only be zero.
     */
    static boolean doCaseN(final Expression expression, final Set<IntIndex> remaining, final BigDecimal lower, final BigDecimal upper,
            final NumberContext precision) {

        boolean didFixVariable = false;

        if (lower != null && expression.isNegativeOn(remaining)) {

            int signum = lower.signum();

            if (signum > 0) {

                expression.setInfeasible();
                return false;

            }
            for (IntIndex indexOfFree : remaining) {
                Variable freeVariable = expression.resolve(indexOfFree);

                if (signum == 0) {
                    if (!freeVariable.validate(ZERO, precision, null)) {
                        expression.setInfeasible();
                        return false;
                    }
                    freeVariable.setFixed(ZERO);
                    didFixVariable = true;
                } else if (freeVariable.isBinary() && expression.get(freeVariable).compareTo(lower) < 0) {
                    freeVariable.setFixed(ZERO);
                    didFixVariable = true;
                }
            }
        }

        if (upper != null && expression.isPositiveOn(remaining)) {

            int signum = upper.signum();

            if (signum < 0) {

                expression.setInfeasible();
                return false;

            }
            for (IntIndex indexOfFree : remaining) {
                Variable freeVariable = expression.resolve(indexOfFree);

                if (signum == 0) {
                    if (!freeVariable.validate(ZERO, precision, null)) {
                        expression.setInfeasible();
                        return false;
                    }
                    freeVariable.setFixed(ZERO);
                    didFixVariable = true;
                } else if (freeVariable.isBinary() && expression.get(freeVariable).compareTo(upper) > 0) {
                    freeVariable.setFixed(ZERO);
                    didFixVariable = true;
                }
            }
        }

        return didFixVariable;
    }

}
