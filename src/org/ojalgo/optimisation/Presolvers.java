/*
 * Copyright 1997-2020 Optimatika
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

import static org.ojalgo.function.constant.BigMath.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.ojalgo.function.constant.BigMath;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.context.NumberContext;

public abstract class Presolvers {

    /**
     * If an expression contains at least 1 binary variable and all non-fixed variable weights are of the same
     * sign (positive or negative) then it is possible the check the validity of "1" for each of the binary
     * variables. (Doesn't seem to work and/or is not effective.)
     *
     * @deprecated v48 Has been replaced by
     *             {@link #doCaseN(Expression, Set, BigDecimal, BigDecimal, NumberContext)}
     */
    @Deprecated
    public static final ExpressionsBasedModel.Presolver BINARY_VALUE = new ExpressionsBasedModel.Presolver(100) {

        @Override
        public boolean simplify(final Expression expression, final Set<IntIndex> remaining, final BigDecimal lower, final BigDecimal upper,
                final NumberContext precision, final boolean relaxed) {
            return Presolvers.doCaseN(expression, remaining, lower, upper, precision, relaxed);
        }

    };
    public static final ExpressionsBasedModel.Presolver INTEGER_EXPRESSION_ROUNDING = new ExpressionsBasedModel.Presolver(20) {

        @Override
        public boolean simplify(final Expression expression, final Set<IntIndex> remaining, final BigDecimal lower, final BigDecimal upper,
                final NumberContext precision, final boolean relaxed) {
            if (expression.isLinearAndAllInteger()) {
                expression.doIntegerRounding();
            }
            return false;
        }

    };

    /**
     * Makes sure integer variables have integer lower/upper bounds (if they exist).
     */
    public static final ExpressionsBasedModel.VariableAnalyser INTEGER_VARIABLE_ROUNDING = new ExpressionsBasedModel.VariableAnalyser(4) {

        @Override
        public boolean simplify(final Variable variable, final ExpressionsBasedModel model) {
            if (variable.isInteger()) {
                variable.doIntegerRounding();
            }
            return false;
        }

    };

    /**
     * If the expression is linear and contributes to the objective function, then the contributions are
     * transferred to the variables and the weight of the expression set to null.
     */
    public static final ExpressionsBasedModel.Presolver LINEAR_OBJECTIVE = new ExpressionsBasedModel.Presolver(10) {

        @Override
        public boolean simplify(final Expression expression, final Set<IntIndex> remaining, final BigDecimal lower, final BigDecimal upper,
                final NumberContext precision, final boolean relaxed) {

            if (expression.isFunctionLinear()) {

                BigDecimal exprWeight = expression.getContributionWeight();

                Variable tmpVariable;
                BigDecimal varWeight;
                BigDecimal contribution;
                for (Entry<IntIndex, BigDecimal> entry : expression.getLinearEntrySet()) {
                    tmpVariable = expression.resolve(entry.getKey());

                    varWeight = tmpVariable.getContributionWeight();
                    contribution = exprWeight.multiply(entry.getValue());

                    varWeight = varWeight != null ? varWeight.add(contribution) : contribution;

                    tmpVariable.weight(varWeight);
                }

                expression.weight(null);

            }

            return false;
        }

    };

    /**
     * Checks the sign of the limits and the sign of the expression parameters to deduce variables that in
     * fact can only be zero.
     *
     * @deprecated v48 Has been replaced by
     *             {@link #doCaseN(Expression, Set, BigDecimal, BigDecimal, NumberContext)}
     */
    @Deprecated
    public static final ExpressionsBasedModel.Presolver OPPOSITE_SIGN = new ExpressionsBasedModel.Presolver(20) {

        @Override
        public boolean simplify(final Expression expression, final Set<IntIndex> remaining, final BigDecimal lower, final BigDecimal upper,
                final NumberContext precision, final boolean relaxed) {
            return Presolvers.doCaseN(expression, remaining, lower, upper, precision, relaxed);
        }

    };

    /**
     * Calculates the min and max value of this expression based on the variables' individual bounds. Then
     * compares those with the expression's bounds.
     */
    public static final ExpressionsBasedModel.Presolver REDUNDANT_CONSTRAINT = new ExpressionsBasedModel.Presolver(Integer.MAX_VALUE) {

        @Override
        public boolean simplify(final Expression expression, final Set<IntIndex> remaining, final BigDecimal lower, final BigDecimal upper,
                final NumberContext precision, final boolean relaxed) {

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
                    } else {
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
                    if ((min != null) && (min.compareTo(upper) > 0) && (Presolvers.findCommonLevel(upper, min) == null)) {
                        expression.setInfeasible();
                    } else if ((max != null) && (max.compareTo(upper) <= 0)) {
                        upperRedundant = true;
                    }
                } else {
                    upperRedundant = true;
                }

                boolean lowerRedundant = false;
                if (lower != null) {
                    if ((max != null) && (max.compareTo(lower) < 0) && (Presolvers.findCommonLevel(lower, max) == null)) {
                        expression.setInfeasible();
                    } else if ((min != null) && (min.compareTo(lower) >= 0)) {
                        lowerRedundant = true;
                    }
                } else {
                    lowerRedundant = true;
                }

                if (lowerRedundant & upperRedundant) {
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
    public static final ExpressionsBasedModel.VariableAnalyser UNREFERENCED = new ExpressionsBasedModel.VariableAnalyser(4) {

        @Override
        public boolean simplify(final Variable variable, final ExpressionsBasedModel model) {

            if (!model.isReferenced(variable)) {

                if (variable.isObjective()) {
                    int weightSignum = variable.getContributionWeight().signum();

                    if ((model.isMaximisation() && (weightSignum == -1)) || (model.isMinimisation() && (weightSignum == 1))) {
                        if (variable.isLowerLimitSet()) {
                            variable.setFixed(variable.getLowerLimit());
                        } else {
                            variable.setUnbounded(true);
                        }
                    } else if ((model.isMaximisation() && (weightSignum == 1)) || (model.isMinimisation() && (weightSignum == -1))) {
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

            return false;
        }

    };

    /**
     * Looks for constraint expressions with 0, 1 or 2 non-fixed variables. Transfers the constraints of the
     * expressions to the variables and then (if possible) marks the expression as redundant.
     */
    public static final ExpressionsBasedModel.Presolver ZERO_ONE_TWO = new ExpressionsBasedModel.Presolver(10) {

        @Override
        public boolean simplify(final Expression expression, final Set<IntIndex> remaining, final BigDecimal lower, final BigDecimal upper,
                final NumberContext precision, final boolean relaxed) {

            switch (remaining.size()) {
            case 0:
                return Presolvers.doCase0(expression, remaining, lower, upper, precision, relaxed);
            case 1:
                return Presolvers.doCase1(expression, remaining, lower, upper, precision, relaxed);
            case 2:
                return Presolvers.doCase2(expression, remaining, lower, upper, precision, relaxed);
            default: // 3 or more
                return Presolvers.doCaseN(expression, remaining, lower, upper, precision, relaxed);
            }
        }
    };

    private static final NumberContext LEVEL = NumberContext.of(12).withMode(RoundingMode.HALF_DOWN);

    static final MathContext LOWER = NumberContext.ofMath(MathContext.DECIMAL128).withMode(RoundingMode.FLOOR).getMathContext();
    static final MathContext UPPER = NumberContext.ofMath(MathContext.DECIMAL128).withMode(RoundingMode.CEILING).getMathContext();

    public static void checkFeasibility(final Expression expression, final Set<IntIndex> remaining, final BigDecimal lower, final BigDecimal upper,
            final NumberContext precision, final boolean relaxed) {
        ZERO_ONE_TWO.simplify(expression, remaining, lower, upper, precision, relaxed);
    }

    /**
     * This constraint expression has 0 remaining free variable. It is entirely redundant.
     */
    static boolean doCase0(final Expression expression, final Set<IntIndex> remaining, final BigDecimal lower, final BigDecimal upper,
            final NumberContext precision, final boolean relaxed) {

        expression.setRedundant();

        if ((lower != null) && precision.isMoreThan(ZERO, lower)) {
            expression.setInfeasible();
        }
        if ((upper != null) && precision.isLessThan(ZERO, upper)) {
            expression.setInfeasible();
        }

        return false;
    }

    /**
     * This constraint expression has 1 remaining free variable. The lower/upper limits can be transferred to
     * that variable, and the expression marked as redundant.
     */
    static boolean doCase1(final Expression expression, final Set<IntIndex> remaining, final BigDecimal lower, final BigDecimal upper,
            final NumberContext precision, final boolean relaxed) {

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

            if (variable.validate(solution, precision, null)) {

                if (variable.isFixed()) {

                    if (Presolvers.findCommonLevel(solution, variable.getValue()) == null) {
                        expression.setInfeasible();
                    }

                    return false;

                } else {

                    variable.setFixed(solution);
                    return true;
                }

            } else {
                expression.setInfeasible();
                return false;
            }

        } else {
            // More general case

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
                if (lowerCand != null) {
                    lowerNew = lowerCand;
                } else {
                    lowerNew = null;
                }
            }

            BigDecimal upperNew;
            if (upperOld != null) {
                if (upperCand != null) {
                    upperNew = upperOld.min(upperCand);
                } else {
                    upperNew = upperOld;
                }
            } else {
                if (upperCand != null) {
                    upperNew = upperCand;
                } else {
                    upperNew = null;
                }
            }

            if ((lowerNew != null) && (upperNew != null)) {
                BigDecimal level = Presolvers.findCommonLevel(lowerNew, upperNew);
                if (level != null) {
                    lowerNew = level;
                    upperNew = level;
                    variable.setFixed(level);
                }
            }

            if ((lowerNew != null) && !relaxed && variable.isInteger()) {
                lowerNew = lowerNew.setScale(0, RoundingMode.CEILING);
            }
            if ((upperNew != null) && !relaxed && variable.isInteger()) {
                upperNew = upperNew.setScale(0, RoundingMode.FLOOR);
            }

            if ((lowerNew != null) && (upperNew != null)) {

                if (lowerNew.compareTo(upperNew) > 0) {

                    expression.setInfeasible();
                    return false;

                } else {

                    BigDecimal level = Presolvers.findCommonLevel(lowerNew, upperNew);

                    if (level != null) {

                        variable.setFixed(level);
                        return true;

                    }
                }
            }

            variable.lower(lowerNew).upper(upperNew);
            return false;

        }
    }

    /**
     * Checks if bounds on either of the variables (together with the expressions's bounds) implies tighter
     * bounds on the other variable.
     */
    static boolean doCase2(final Expression expression, final Set<IntIndex> remaining, final BigDecimal lower, final BigDecimal upper,
            final NumberContext precision, final boolean relaxed) {

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

        if ((lower != null) && (contrMaxA != null) && (contrMaxB != null) && precision.isLessThan(lower, contrMaxA.add(contrMaxB))) {
            expression.setInfeasible();
            return false;
        }

        if ((upper != null) && (contrMinA != null) && (contrMinB != null) && precision.isMoreThan(upper, contrMinA.add(contrMinB))) {
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

        if ((lowerNewA != null) && (upperNewA != null)) {
            BigDecimal level = Presolvers.findCommonLevel(lowerNewA, upperNewA);
            if (level != null) {
                lowerNewA = level;
                upperNewA = level;
                variableA.setFixed(level);
            }
        }

        if ((lowerNewB != null) && (upperNewB != null)) {
            BigDecimal level = Presolvers.findCommonLevel(lowerNewB, upperNewB);
            if (level != null) {
                lowerNewB = level;
                upperNewB = level;
                variableB.setFixed(level);
            }
        }

        if (!relaxed && variableA.isInteger()) {
            if (lowerNewA != null) {
                lowerNewA = lowerNewA.setScale(0, RoundingMode.CEILING);
            }
            if (upperNewA != null) {
                upperNewA = upperNewA.setScale(0, RoundingMode.FLOOR);
            }
        }

        if (!relaxed && variableB.isInteger()) {
            if (lowerNewB != null) {
                lowerNewB = lowerNewB.setScale(0, RoundingMode.CEILING);
            }
            if (upperNewB != null) {
                upperNewB = upperNewB.setScale(0, RoundingMode.FLOOR);
            }
        }

        variableA.lower(lowerNewA).upper(upperNewA);
        variableB.lower(lowerNewB).upper(upperNewB);

        return variableA.isFixed() || variableB.isFixed();
    }

    /**
     * Checks the sign of the limits and the sign of the expression parameters to deduce variables that in
     * fact can only be zero.
     */
    static boolean doCaseN(final Expression expression, final Set<IntIndex> remaining, final BigDecimal lower, final BigDecimal upper,
            final NumberContext precision, final boolean relaxed) {

        boolean didFixVariable = false;

        if ((lower != null) && expression.isNegativeOn(remaining)) {

            int signum = lower.signum();

            if (signum > 0) {

                expression.setInfeasible();
                return false;

            } else {

                for (IntIndex indexOfFree : remaining) {
                    Variable freeVariable = expression.resolve(indexOfFree);

                    if (signum == 0) {
                        if (freeVariable.validate(ZERO, precision, null)) {
                            freeVariable.setFixed(ZERO);
                            didFixVariable = true;
                        } else {
                            expression.setInfeasible();
                            return false;
                        }
                    } else if (!relaxed && freeVariable.isBinary() && (expression.get(freeVariable).compareTo(lower) < 0)) {
                        freeVariable.setFixed(ZERO);
                        didFixVariable = true;
                    }
                }
            }
        }

        if ((upper != null) && expression.isPositiveOn(remaining)) {

            int signum = upper.signum();

            if (signum < 0) {

                expression.setInfeasible();
                return false;

            } else {

                for (IntIndex indexOfFree : remaining) {
                    Variable freeVariable = expression.resolve(indexOfFree);

                    if (signum == 0) {
                        if (freeVariable.validate(ZERO, precision, null)) {
                            freeVariable.setFixed(ZERO);
                            didFixVariable = true;
                        } else {
                            expression.setInfeasible();
                            return false;
                        }
                    } else if (!relaxed && freeVariable.isBinary() && (expression.get(freeVariable).compareTo(upper) > 0)) {
                        freeVariable.setFixed(ZERO);
                        didFixVariable = true;
                    }
                }
            }
        }

        return didFixVariable;
    }

    static BigDecimal findCommonLevel(final BigDecimal a, final BigDecimal b) {
        if (a.compareTo(b) == 0) {
            return a;
        } else {
            BigDecimal levelledA = LEVEL.enforce(a);
            BigDecimal levelledB = LEVEL.enforce(b);
            if (levelledA.compareTo(levelledB) == 0) {
                return DIVIDE.invoke(a.add(a), TWO);
            } else {
                return null;
            }
        }
    }

}
