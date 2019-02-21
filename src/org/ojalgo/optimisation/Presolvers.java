/*
 * Copyright 1997-2018 Optimatika
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

import static org.ojalgo.constant.BigMath.*;
import static org.ojalgo.function.BigFunction.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

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
        public boolean simplify(final Expression expression, Set<IntIndex> remaining, BigDecimal lower, BigDecimal upper, final NumberContext precision) {
            return Presolvers.doCaseN(expression, remaining, lower, upper, precision);
        }

    };

    /**
     * Verifies that the variable is actually referenced/used in some expression. If not then that variable
     * can either be fixed or marked as unbounded. Also makes sure integer variables have integer lower/upper
     * bounds (if they exist).
     *
     * <pre>
     * 2019-02-15: Turned this off. Very slow for large models
     * </pre>
     */
    public static final ExpressionsBasedModel.VariableAnalyser FIXED_OR_UNBOUNDED = new ExpressionsBasedModel.VariableAnalyser(4) {

        @Override
        public boolean simplify(final Variable variable, final ExpressionsBasedModel model) {

            if (variable.isInteger()) {
                BigDecimal tmpLimit;
                if (((tmpLimit = variable.getUpperLimit()) != null) && (tmpLimit.scale() > 0)) {
                    variable.upper(tmpLimit.setScale(0, RoundingMode.FLOOR));
                }
                if (((tmpLimit = variable.getLowerLimit()) != null) && (tmpLimit.scale() > 0)) {
                    variable.lower(tmpLimit.setScale(0, RoundingMode.CEILING));
                }
            }

            if (variable.isObjective() && !variable.isFixed() && !variable.isUnbounded()) {

                final boolean includedAnywhere = model.expressions().anyMatch(expr -> expr.includes(variable));
                if (!includedAnywhere) {

                    final int weightSignum = variable.getContributionWeight().signum();

                    if (model.isMaximisation() && (weightSignum == -1)) {
                        if (variable.isLowerLimitSet()) {
                            variable.setFixed(variable.getLowerLimit());
                        } else {
                            variable.setUnbounded(true);
                        }
                    } else if (model.isMinimisation() && (weightSignum == 1)) {
                        if (variable.isLowerLimitSet()) {
                            variable.setFixed(variable.getLowerLimit());
                        } else {
                            variable.setUnbounded(true);
                        }
                    } else if (model.isMaximisation() && (weightSignum == 1)) {
                        if (variable.isUpperLimitSet()) {
                            variable.setFixed(variable.getUpperLimit());
                        } else {
                            variable.setUnbounded(true);
                        }
                    } else if (model.isMinimisation() && (weightSignum == -1)) {
                        if (variable.isUpperLimitSet()) {
                            variable.setFixed(variable.getUpperLimit());
                        } else {
                            variable.setUnbounded(true);
                        }
                    }
                }
            }

            return false;
        }

    };

    public static final ExpressionsBasedModel.Presolver INTEGER_ROUNDING = new ExpressionsBasedModel.Presolver(20) {

        @Override
        public boolean simplify(Expression expression, Set<IntIndex> remaining, BigDecimal lower, BigDecimal upper, NumberContext precision) {
            expression.doIntegerRounding();
            return false;
        }

    };

    /**
     * If the expression is linear and contributes to the objective function, then the contributions are
     * transferred to the variables and the weight of the expression set to null.
     */
    public static final ExpressionsBasedModel.Presolver LINEAR_OBJECTIVE = new ExpressionsBasedModel.Presolver(10) {

        @Override
        public boolean simplify(final Expression expression, Set<IntIndex> remaining, BigDecimal lower, BigDecimal upper, final NumberContext precision) {

            if (expression.isObjective() && expression.isFunctionLinear()) {

                final BigDecimal exprWeight = expression.getContributionWeight();

                Variable tmpVariable;
                BigDecimal varWeight;
                BigDecimal contribution;
                for (final Entry<IntIndex, BigDecimal> entry : expression.getLinearEntrySet()) {
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
        public boolean simplify(final Expression expression, Set<IntIndex> remaining, BigDecimal lower, BigDecimal upper, final NumberContext precision) {
            return Presolvers.doCaseN(expression, remaining, lower, upper, precision);
        }

    };

    /**
     * Looks for constraint expressions with 0, 1 or 2 non-fixed variables. Transfers the constraints of the
     * expressions to the variables and then (if possible) marks the expression as redundant.
     */
    public static final ExpressionsBasedModel.Presolver ZERO_ONE_TWO = new ExpressionsBasedModel.Presolver(10) {

        @Override
        public boolean simplify(final Expression expression, Set<IntIndex> remaining, BigDecimal lower, BigDecimal upper, final NumberContext precision) {

            switch (remaining.size()) {
            case 0:
                return Presolvers.doCase0(expression, remaining, lower, upper, precision);
            case 1:
                return Presolvers.doCase1(expression, remaining, lower, upper, precision);
            case 2:
                /*
                 * doCaseN(...) does something that doCase2(...) does not, and it's necessary. Possibly
                 * doCase2(...) can be removed completely - complicated code that doesn't seem to do
                 * accomplish very much.
                 */
                return Presolvers.doCaseN(expression, remaining, lower, upper, precision) || Presolvers.doCase2(expression, remaining, lower, upper, precision);
            default: // 3 or more
                return Presolvers.doCaseN(expression, remaining, lower, upper, precision);
            }
        }
    };

    /**
     * This constraint expression has 0 remaining free variable. It is entirely redundant.
     */
    static boolean doCase0(final Expression expression, final Set<IntIndex> remaining, BigDecimal lower, BigDecimal upper, final NumberContext precision) {

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
    static boolean doCase1(final Expression expression, final Set<IntIndex> remaining, BigDecimal lower, BigDecimal upper, final NumberContext precision) {

        final IntIndex index = remaining.iterator().next();
        final Variable variable = expression.resolve(index);
        final BigDecimal factor = expression.get(index);
        final BigDecimal oldLower = variable.getLowerLimit();
        final BigDecimal oldUpper = variable.getUpperLimit();
        final BigDecimal varMaxContr;
        final BigDecimal varMinContr;
        if (factor.signum() == -1) {
            varMinContr = oldUpper != null ? factor.multiply(oldUpper) : null;
            varMaxContr = oldLower != null ? factor.multiply(oldLower) : null;
        } else {
            varMaxContr = oldUpper != null ? factor.multiply(oldUpper) : null;
            varMinContr = oldLower != null ? factor.multiply(oldLower) : null;
        }

        if ((lower != null) && (varMaxContr != null) && precision.isLessThan(lower, varMaxContr)) {
            expression.setInfeasible();
            return false || false;
        }

        if ((upper != null) && (varMinContr != null) && precision.isMoreThan(upper, varMinContr)) {
            expression.setInfeasible();
            return false;
        }

        if (expression.isEqualityConstraint()) {
            // Simple case with equality constraint

            final BigDecimal solution = DIVIDE.invoke(upper, factor);

            if (variable.validate(solution, precision, null)) {
                variable.setFixed(solution);
            } else {
                expression.setInfeasible();
            }

        } else {
            // More general case

            BigDecimal solutionLower = lower != null ? DIVIDE.invoke(lower, factor) : null;
            BigDecimal solutionUpper = upper != null ? DIVIDE.invoke(upper, factor) : null;
            if (factor.signum() < 0) {
                final BigDecimal tmpVal = solutionLower;
                solutionLower = solutionUpper;
                solutionUpper = tmpVal;
            }

            final BigDecimal newLower;
            if (solutionLower != null) {
                solutionLower = oldLower != null ? oldLower.max(solutionLower) : solutionLower;
                if (variable.isInteger()) {
                    solutionLower = solutionLower.setScale(0, RoundingMode.CEILING);
                }
                newLower = solutionLower;
            } else {
                newLower = oldLower;
            }

            final BigDecimal newUpper;
            if (solutionUpper != null) {
                solutionUpper = oldUpper != null ? oldUpper.min(solutionUpper) : solutionUpper;
                if (variable.isInteger()) {
                    solutionUpper = solutionUpper.setScale(0, RoundingMode.FLOOR);
                }
                newUpper = solutionUpper;
            } else {
                newUpper = oldUpper;
            }

            variable.lower(newLower).upper(newUpper);
            if (variable.isInfeasible()) {
                expression.setInfeasible();
            }
        }

        expression.setRedundant();

        if (variable.isEqualityConstraint()) {
            variable.setValue(variable.getLowerLimit());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if bounds on either of the variables (together with the expressions's bounds) implies tighter
     * bounds on the other variable.
     */
    static boolean doCase2(final Expression expression, final Set<IntIndex> remaining, BigDecimal lower, BigDecimal upper, final NumberContext precision) {

        final Iterator<IntIndex> tmpIterator = remaining.iterator();

        final Variable variableA = expression.resolve(tmpIterator.next());
        final BigDecimal factorA = expression.get(variableA);
        final BigDecimal oldLowerA = variableA.getLowerLimit();
        final BigDecimal oldUpperA = variableA.getUpperLimit();
        final BigDecimal varMaxContrA;
        final BigDecimal varMinContrA;
        if (factorA.signum() == -1) {
            varMinContrA = oldUpperA != null ? factorA.multiply(oldUpperA) : null;
            varMaxContrA = oldLowerA != null ? factorA.multiply(oldLowerA) : null;
        } else {
            varMaxContrA = oldUpperA != null ? factorA.multiply(oldUpperA) : null;
            varMinContrA = oldLowerA != null ? factorA.multiply(oldLowerA) : null;
        }

        final Variable variableB = expression.resolve(tmpIterator.next());
        final BigDecimal factorB = expression.get(variableB);
        final BigDecimal oldLowerB = variableB.getLowerLimit();
        final BigDecimal oldUpperB = variableB.getUpperLimit();
        final BigDecimal varMaxContrB;
        final BigDecimal varMinContrB;
        if (factorB.signum() == -1) {
            varMinContrB = oldUpperB != null ? factorB.multiply(oldUpperB) : null;
            varMaxContrB = oldLowerB != null ? factorB.multiply(oldLowerB) : null;
        } else {
            varMaxContrB = oldUpperB != null ? factorB.multiply(oldUpperB) : null;
            varMinContrB = oldLowerB != null ? factorB.multiply(oldLowerB) : null;
        }

        if ((lower != null) && (varMaxContrA != null) && (varMaxContrB != null) && precision.isLessThan(lower, varMaxContrA.add(varMaxContrB))) {
            expression.setInfeasible();
            return false;
        }

        if ((upper != null) && (varMinContrA != null) && (varMinContrB != null) && precision.isMoreThan(upper, varMinContrA.add(varMinContrB))) {
            expression.setInfeasible();
            return false;
        }

        BigDecimal newLowerA = oldLowerA;
        BigDecimal newUpperA = oldUpperA;
        BigDecimal newLowerB = oldLowerB;
        BigDecimal newUpperB = oldUpperB;

        if (lower != null) {

            if ((varMaxContrB != null) && (varMaxContrB.compareTo(lower) < 0)) {
                BigDecimal newLimit = DIVIDE.invoke(lower.subtract(varMaxContrB), factorA);

                newLimit = oldLowerA != null ? oldLowerA.max(newLimit) : newLimit;
                newLimit = oldUpperA != null ? oldUpperA.min(newLimit) : newLimit;

                if (factorA.signum() == 1) {
                    newLowerA = newLimit;
                } else {
                    newUpperA = newLimit;
                }
            }

            if ((varMaxContrA != null) && (varMaxContrA.compareTo(lower) < 0)) {
                BigDecimal newLimit = DIVIDE.invoke(lower.subtract(varMaxContrA), factorB);

                newLimit = oldLowerB != null ? oldLowerB.max(newLimit) : newLimit;
                newLimit = oldUpperB != null ? oldUpperB.min(newLimit) : newLimit;

                if (factorB.signum() == 1) {
                    newLowerB = newLimit;
                } else {
                    newUpperB = newLimit;
                }
            }
        }

        if (upper != null) {

            if ((varMinContrB != null) && (varMinContrB.compareTo(upper) > 0)) {
                BigDecimal newLimit = DIVIDE.invoke(upper.subtract(varMinContrB), factorA);

                newLimit = oldLowerA != null ? oldLowerA.max(newLimit) : newLimit;
                newLimit = oldUpperA != null ? oldUpperA.min(newLimit) : newLimit;

                if (factorA.signum() == 1) {
                    newUpperA = newLimit;
                } else {
                    newLowerA = newLimit;
                }
            }

            if ((varMinContrA != null) && (varMinContrA.compareTo(upper) > 0)) {
                BigDecimal newLimit = DIVIDE.invoke(upper.subtract(varMinContrA), factorB);

                newLimit = oldLowerB != null ? oldLowerB.max(newLimit) : newLimit;
                newLimit = oldUpperB != null ? oldUpperB.min(newLimit) : newLimit;

                if (factorB.signum() == 1) {
                    newUpperB = newLimit;
                } else {
                    newLowerB = newLimit;
                }
            }
        }

        if (variableA.isInteger()) {
            if (newLowerA != null) {
                newLowerA = newLowerA.setScale(0, RoundingMode.CEILING);
            }
            if (newUpperA != null) {
                newUpperA = newUpperA.setScale(0, RoundingMode.FLOOR);
            }
        }

        if (variableB.isInteger()) {
            if (newLowerB != null) {
                newLowerB = newLowerB.setScale(0, RoundingMode.CEILING);
            }
            if (newUpperB != null) {
                newUpperB = newUpperB.setScale(0, RoundingMode.FLOOR);
            }
        }

        variableA.lower(newLowerA).upper(newUpperA);
        variableB.lower(newLowerB).upper(newUpperB);

        return variableA.isFixed() || variableB.isFixed();
    }

    /**
     * Checks the sign of the limits and the sign of the expression parameters to deduce variables that in
     * fact can only be zero.
     */
    static boolean doCaseN(final Expression expression, final Set<IntIndex> remaining, BigDecimal lower, BigDecimal upper, final NumberContext precision) {

        boolean didFixVariable = false;

        if ((lower != null) && expression.isNegativeOn(remaining)) {

            int signum = lower.signum();

            if (signum > 0) {

                expression.setInfeasible();
                return false;

            } else {

                for (final IntIndex indexOfFree : remaining) {
                    final Variable freeVariable = expression.resolve(indexOfFree);

                    if (signum == 0) {
                        if (freeVariable.validate(ZERO, precision, null)) {
                            freeVariable.setFixed(ZERO);
                            didFixVariable = true;
                        } else {
                            expression.setInfeasible();
                            return false;
                        }
                    } else if (freeVariable.isBinary() && (expression.get(freeVariable).compareTo(lower) < 0)) {
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

                for (final IntIndex indexOfFree : remaining) {
                    final Variable freeVariable = expression.resolve(indexOfFree);

                    if (signum == 0) {
                        if (freeVariable.validate(ZERO, precision, null)) {
                            freeVariable.setFixed(ZERO);
                            didFixVariable = true;
                        } else {
                            expression.setInfeasible();
                            return false;
                        }
                    } else if (freeVariable.isBinary() && (expression.get(freeVariable).compareTo(upper) > 0)) {
                        freeVariable.setFixed(ZERO);
                        didFixVariable = true;
                    }
                }
            }
        }

        return didFixVariable;
    }

}
