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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.ojalgo.access.Structure1D.IntIndex;
import org.ojalgo.constant.BigMath;
import org.ojalgo.function.BigFunction;
import org.ojalgo.type.context.NumberContext;

public abstract class Presolvers {

    public static final ExpressionsBasedModel.Presolver BIGSTUFF = new ExpressionsBasedModel.Presolver(99) {

        @Override
        public boolean simplify(final Expression expression, final Set<IntIndex> fixedVariables, final BigDecimal fixedValue,
                final Function<IntIndex, Variable> variableResolver, final NumberContext precision) {

            if (expression.getLinearEntrySet().size() > 3333) {

                final Map<IntIndex, BigDecimal> max = new HashMap<>();
                final Map<IntIndex, BigDecimal> min = new HashMap<>();

                BigDecimal totMax = BigMath.ZERO;
                BigDecimal totMin = BigMath.ZERO;

                for (final Entry<IntIndex, BigDecimal> tmpEntry : expression.getLinearEntrySet()) {
                    final IntIndex tmpIndex = tmpEntry.getKey();
                    final Variable tmpVariable = variableResolver.apply(tmpIndex);
                    final BigDecimal tmpFactor = tmpEntry.getValue();
                    if (tmpVariable.isFixed()) {
                        final BigDecimal fixed = tmpVariable.getValue().multiply(tmpFactor);
                        max.put(tmpIndex, fixed);
                        min.put(tmpIndex, fixed);
                    } else {
                        final BigDecimal tmpUL = tmpVariable.getUpperLimit();
                        final BigDecimal tmpLL = tmpVariable.getLowerLimit();
                        if (tmpFactor.signum() == 1) {
                            final BigDecimal tmpMaxVal = tmpUL != null ? tmpUL.multiply(tmpFactor) : BigMath.VERY_POSITIVE;
                            final BigDecimal tmpMinVal = tmpLL != null ? tmpLL.multiply(tmpFactor) : BigMath.VERY_NEGATIVE;
                            max.put(tmpIndex, tmpMaxVal);
                            totMax = totMax.add(tmpMaxVal);
                            min.put(tmpIndex, tmpMinVal);
                            totMin = totMin.add(tmpMinVal);
                        } else {
                            final BigDecimal tmpMaxVal = tmpLL != null ? tmpLL.multiply(tmpFactor) : BigMath.VERY_POSITIVE;
                            final BigDecimal tmpMinVal = tmpUL != null ? tmpUL.multiply(tmpFactor) : BigMath.VERY_NEGATIVE;
                            max.put(tmpIndex, tmpMaxVal);
                            totMax = totMax.add(tmpMaxVal);
                            min.put(tmpIndex, tmpMinVal);
                            totMin = totMin.add(tmpMinVal);
                        }
                    }
                }

                BigDecimal tmpExprU = expression.getUpperLimit();
                if (tmpExprU == null) {
                    tmpExprU = BigMath.VERY_POSITIVE;
                }

                BigDecimal tmpExprL = expression.getLowerLimit();
                if (tmpExprL == null) {
                    tmpExprL = BigMath.VERY_NEGATIVE;
                }

                for (final Entry<IntIndex, BigDecimal> tmpEntry : expression.getLinearEntrySet()) {
                    final IntIndex tmpIndex = tmpEntry.getKey();
                    final Variable tmpVariable = variableResolver.apply(tmpIndex);
                    final BigDecimal tmpFactor = tmpEntry.getValue();

                    final BigDecimal tmpRemU = tmpExprU.subtract(totMin).add(min.get(tmpIndex));
                    final BigDecimal tmpRemL = tmpExprL.subtract(totMax).add(max.get(tmpIndex));

                    BigDecimal tmpVarU = tmpVariable.getUpperLimit();
                    if (tmpVarU == null) {
                        tmpVarU = BigMath.VERY_POSITIVE;
                    }

                    BigDecimal tmpVarL = tmpVariable.getLowerLimit();
                    if (tmpVarL == null) {
                        tmpVarL = BigMath.VERY_NEGATIVE;
                    }

                    if (tmpFactor.signum() == 1) {
                        tmpVarU = tmpVarU.min(BigFunction.DIVIDE.invoke(tmpRemU, tmpFactor));
                        tmpVarL = tmpVarL.max(BigFunction.DIVIDE.invoke(tmpRemL, tmpFactor));
                    } else {
                        tmpVarU = tmpVarU.min(BigFunction.DIVIDE.invoke(tmpRemL, tmpFactor));
                        tmpVarL = tmpVarL.max(BigFunction.DIVIDE.invoke(tmpRemU, tmpFactor));
                    }

                    if (tmpVarU.compareTo(BigMath.VERY_POSITIVE) < 0) {
                        tmpVariable.upper(tmpVarU);
                    }

                    if (tmpVarL.compareTo(BigMath.VERY_NEGATIVE) > 0) {
                        tmpVariable.lower(tmpVarL);
                    }
                }

            }

            return false;
        }

    };

    /**
     * If an expression contains at least 1 binary varibale and all non-fixed variable weights are of the same
     * sign (positive or negative) then it is possible the check the validity of "1" for each of the binary
     * variables. (Doesn't seem to work and/or is not effcetive.)
     */
    public static final ExpressionsBasedModel.Presolver BINARY_VALUE = new ExpressionsBasedModel.Presolver(100) {

        @Override
        public boolean simplify(final Expression expression, final Set<IntIndex> fixedVariables, final BigDecimal fixedValue,
                final Function<IntIndex, Variable> variableResolver, final NumberContext precision) {

            boolean didFixVariable = false;

            final Set<Variable> binaryVariables = expression.getBinaryVariables(fixedVariables);

            if (binaryVariables.size() > 0) {

                BigDecimal compUppLim = expression.getUpperLimit();
                if (compUppLim != null) {
                    if (fixedValue.signum() != 0) {
                        compUppLim = compUppLim.subtract(fixedValue);
                    }
                }

                BigDecimal compLowLim = expression.getLowerLimit();
                if (compLowLim != null) {
                    if (fixedValue.signum() != 0) {
                        compLowLim = compLowLim.subtract(fixedValue);
                    }
                }

                if ((compUppLim != null) && expression.isPositive(fixedVariables)) {
                    for (final Variable binVar : binaryVariables) {
                        if (expression.get(binVar).compareTo(compUppLim) > 0) {
                            binVar.setFixed(ZERO);
                            didFixVariable = true;
                        }
                    }
                } else if ((compLowLim != null) && expression.isNegative(fixedVariables)) {
                    for (final Variable binVar : binaryVariables) {
                        if (expression.get(binVar).compareTo(compLowLim) < 0) {
                            binVar.setFixed(ZERO);
                            didFixVariable = true;
                        }
                    }
                }

            }

            return didFixVariable;
        }

    };

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

    public static final ExpressionsBasedModel.Presolver LINEAR_OBJECTIVE = new ExpressionsBasedModel.Presolver(10) {

        @Override
        public boolean simplify(final Expression expression, final Set<IntIndex> fixedVariables, final BigDecimal fixedValue,
                final Function<IntIndex, Variable> variableResolver, final NumberContext precision) {

            if (expression.isObjective() && expression.isFunctionLinear()) {

                final BigDecimal exprWeight = expression.getContributionWeight();

                Variable tmpVariable;
                BigDecimal varWeight;
                BigDecimal contribution;
                for (final Entry<IntIndex, BigDecimal> entry : expression.getLinearEntrySet()) {
                    tmpVariable = variableResolver.apply(entry.getKey());
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
     * fact can only zero.
     */
    public static final ExpressionsBasedModel.Presolver OPPOSITE_SIGN = new ExpressionsBasedModel.Presolver(20) {

        @Override
        public boolean simplify(final Expression expression, final Set<IntIndex> fixedVariables, final BigDecimal fixedValue,
                final Function<IntIndex, Variable> variableResolver, final NumberContext precision) {

            boolean didFixVariable = false;

            BigDecimal tmpCompLowLim = expression.getLowerLimit();
            if ((tmpCompLowLim != null) && (fixedValue.signum() != 0)) {
                tmpCompLowLim = tmpCompLowLim.subtract(fixedValue);
            }

            BigDecimal tmpCompUppLim = expression.getUpperLimit();
            if ((tmpCompUppLim != null) && (fixedValue.signum() != 0)) {
                tmpCompUppLim = tmpCompUppLim.subtract(fixedValue);
            }

            if ((tmpCompLowLim != null) && (tmpCompLowLim.signum() >= 0) && expression.isNegative(fixedVariables)) {

                if (tmpCompLowLim.signum() == 0) {

                    for (final IntIndex tmpLinear : expression.getLinearKeySet()) {
                        if (!fixedVariables.contains(tmpLinear)) {

                            final Variable tmpFreeVariable = variableResolver.apply(tmpLinear);

                            if (tmpFreeVariable.validate(ZERO, precision, null)) {
                                tmpFreeVariable.setFixed(ZERO);
                                didFixVariable = true;
                            } else {
                                expression.setInfeasible();
                            }
                        }
                    }

                } else {

                    expression.setInfeasible();
                }
            }

            if ((tmpCompUppLim != null) && (tmpCompUppLim.signum() <= 0) && expression.isPositive(fixedVariables)) {

                if (tmpCompUppLim.signum() == 0) {

                    for (final IntIndex tmpLinear : expression.getLinearKeySet()) {
                        if (!fixedVariables.contains(tmpLinear)) {
                            final Variable tmpFreeVariable = variableResolver.apply(tmpLinear);

                            if (tmpFreeVariable.validate(ZERO, precision, null)) {
                                tmpFreeVariable.setFixed(ZERO);
                                didFixVariable = true;
                            } else {
                                expression.setInfeasible();
                            }
                        }
                    }

                } else {

                    expression.setInfeasible();
                }
            }

            return didFixVariable;
        }

    };

    /**
     * Looks for constraint expressions with 0, 1 or 2 non-fixed variables. Transfers the constraints of the
     * expressions to the variables and then (if possible) marks the expression as redundant.
     */
    public static final ExpressionsBasedModel.Presolver ZERO_ONE_TWO = new ExpressionsBasedModel.Presolver(10) {

        @Override
        public boolean simplify(final Expression expression, final Set<IntIndex> fixedVariables, final BigDecimal fixedValue,
                final Function<IntIndex, Variable> variableResolver, final NumberContext precision) {

            boolean didFixVariable = false;

            if (expression.countLinearFactors() <= (fixedVariables.size() + 2)) {
                // This constraint can possibly be reduced to 0, 1 or 2 remaining linear factors

                final HashSet<IntIndex> remainingLinear = new HashSet<>(expression.getLinearKeySet());
                remainingLinear.removeAll(fixedVariables);

                switch (remainingLinear.size()) {

                case 0:

                    didFixVariable = Presolvers.doCase0(expression, fixedValue, remainingLinear, variableResolver, precision);
                    break;

                case 1:

                    didFixVariable = Presolvers.doCase1(expression, fixedValue, remainingLinear, variableResolver, precision);
                    break;

                case 2:

                    didFixVariable = Presolvers.doCase2(expression, fixedValue, remainingLinear, variableResolver, precision);
                    break;

                default:

                    break;
                }

            }

            return didFixVariable;
        }

    };

    /**
     * This constraint expression has 0 remaining free variable. It is entirely redundant.
     */
    static boolean doCase0(final Expression expression, final BigDecimal fixedValue, final HashSet<IntIndex> remaining,
            final Function<IntIndex, Variable> variableResolver, final NumberContext precision) {

        expression.setRedundant(true);

        if (expression.validate(fixedValue, precision, null)) {
            expression.level(fixedValue);
        } else {
            expression.setInfeasible();
        }

        return false;
    }

    /**
     * This constraint expression has 1 remaining free variable. The lower/upper limits can be transferred to
     * that variable, and the expression marked as redundant.
     */
    static boolean doCase1(final Expression expression, final BigDecimal fixedValue, final HashSet<IntIndex> remaining,
            final Function<IntIndex, Variable> variableResolver, final NumberContext precision) {

        final IntIndex index = remaining.iterator().next();
        final Variable variable = variableResolver.apply(index);
        final BigDecimal factor = expression.get(index);

        final BigDecimal expLower = expression.getLowerLimit();
        final BigDecimal expUpper = expression.getUpperLimit();

        if (expression.isEqualityConstraint()) {
            // Simple case with equality constraint

            final BigDecimal solution = DIVIDE.invoke(expUpper.subtract(fixedValue), factor);

            if (variable.validate(solution, precision, null)) {
                variable.setFixed(solution);
            } else {
                expression.setInfeasible();
            }

        } else {
            // More general case

            final BigDecimal compLower = expLower != null ? expLower.subtract(fixedValue) : null;
            final BigDecimal compUpper = expUpper != null ? expUpper.subtract(fixedValue) : null;

            BigDecimal solLower = compLower != null ? DIVIDE.invoke(compLower, factor) : null;
            BigDecimal solUpper = compUpper != null ? DIVIDE.invoke(compUpper, factor) : null;
            if (factor.signum() < 0) {
                final BigDecimal tmpVal = solLower;
                solLower = solUpper;
                solUpper = tmpVal;
            }

            final BigDecimal oldLower = variable.getLowerLimit();
            final BigDecimal oldUpper = variable.getUpperLimit();

            final BigDecimal newLower;
            if (solLower != null) {
                solLower = oldLower != null ? oldLower.max(solLower) : solLower;
                if (variable.isInteger()) {
                    solLower = solLower.setScale(0, RoundingMode.CEILING);
                }
                newLower = solLower;
            } else {
                newLower = oldLower;
            }

            final BigDecimal newUpper;
            if (solUpper != null) {
                solUpper = oldUpper != null ? oldUpper.min(solUpper) : solUpper;
                if (variable.isInteger()) {
                    solUpper = solUpper.setScale(0, RoundingMode.FLOOR);
                }
                newUpper = solUpper;
            } else {
                newUpper = oldUpper;
            }

            variable.lower(newLower).upper(newUpper);
            if (variable.isInfeasible()) {
                expression.setInfeasible();
            }
        }

        expression.setRedundant(true);

        if (variable.isEqualityConstraint()) {
            variable.setValue(variable.getLowerLimit());
            return true;
        } else {
            return false;
        }
    }

    static boolean doCase2(final Expression expression, final BigDecimal fixedValue, final HashSet<IntIndex> remaining,
            final Function<IntIndex, Variable> variableResolver, final NumberContext precision) {

        final Iterator<IntIndex> tmpIterator = remaining.iterator();

        final Variable variableA = variableResolver.apply(tmpIterator.next());
        final BigDecimal varAfactor = expression.get(variableA);
        final BigDecimal varAlowerOrg = variableA.getLowerLimit();
        final BigDecimal varAupperOrg = variableA.getUpperLimit();
        final BigDecimal varAmax;
        final BigDecimal varAmin;
        if (varAfactor.signum() == 1) {
            varAmax = varAupperOrg != null ? varAfactor.multiply(varAupperOrg) : null;
            varAmin = varAlowerOrg != null ? varAfactor.multiply(varAlowerOrg) : null;
        } else {
            varAmin = varAupperOrg != null ? varAfactor.multiply(varAupperOrg) : null;
            varAmax = varAlowerOrg != null ? varAfactor.multiply(varAlowerOrg) : null;
        }
        BigDecimal varAlowerNew = varAlowerOrg;
        BigDecimal varAupperNew = varAupperOrg;

        final Variable variableB = variableResolver.apply(tmpIterator.next());
        final BigDecimal varBfactor = expression.get(variableB);
        final BigDecimal varBlowerOrg = variableB.getLowerLimit();
        final BigDecimal varBupperOrg = variableB.getUpperLimit();
        final BigDecimal varBmax;
        final BigDecimal varBmin;
        if (varBfactor.signum() == 1) {
            varBmax = varBupperOrg != null ? varBfactor.multiply(varBupperOrg) : null;
            varBmin = varBlowerOrg != null ? varBfactor.multiply(varBlowerOrg) : null;
        } else {
            varBmin = varBupperOrg != null ? varBfactor.multiply(varBupperOrg) : null;
            varBmax = varBlowerOrg != null ? varBfactor.multiply(varBlowerOrg) : null;
        }
        BigDecimal varBlowerNew = varBlowerOrg;
        BigDecimal varBupperNew = varBupperOrg;

        final BigDecimal exprLower = expression.getLowerLimit() != null ? SUBTRACT.invoke(expression.getLowerLimit(), fixedValue) : expression.getLowerLimit();
        final BigDecimal exprUpper = expression.getUpperLimit() != null ? SUBTRACT.invoke(expression.getUpperLimit(), fixedValue) : expression.getUpperLimit();

        if ((exprLower != null) && (varAmax != null) && (varBmax != null) && precision.isLessThan(exprLower, varAmax.add(varBmax))) {
            expression.setInfeasible();
        }
        if ((exprUpper != null) && (varAmin != null) && (varBmin != null) && precision.isMoreThan(exprUpper, varAmin.add(varBmin))) {
            expression.setInfeasible();
        }

        if (exprLower != null) {

            final BigDecimal varBlimit = varBfactor.signum() == 1 ? varBupperOrg : varBlowerOrg;
            if (varBlimit != null) {

                BigDecimal newLimit = DIVIDE.invoke(exprLower.subtract(varBfactor.multiply(varBlimit)), varAfactor);

                newLimit = varAlowerOrg != null ? varAlowerOrg.max(newLimit) : newLimit;
                newLimit = varAupperOrg != null ? varAupperOrg.min(newLimit) : newLimit;

                if (varAfactor.signum() == 1) {
                    // New lower limit on A
                    varAlowerNew = newLimit;
                } else {
                    // New upper limit on A
                    varAupperNew = newLimit;
                }

            }

            final BigDecimal varAlimit = varAfactor.signum() == 1 ? varAupperOrg : varAlowerOrg;
            if (varAlimit != null) {

                BigDecimal newLimit = DIVIDE.invoke(exprLower.subtract(varAfactor.multiply(varAlimit)), varBfactor);

                newLimit = varBlowerOrg != null ? varBlowerOrg.max(newLimit) : newLimit;
                newLimit = varBupperOrg != null ? varBupperOrg.min(newLimit) : newLimit;

                if (varBfactor.signum() == 1) {
                    // New lower limit on B
                    varBlowerNew = newLimit;
                } else {
                    // New upper limit on B
                    varBupperNew = newLimit;
                }

            }
        }

        if (exprUpper != null) {

            final BigDecimal varBlimit = varBfactor.signum() == 1 ? varBlowerOrg : varBupperOrg;
            if (varBlimit != null) {

                BigDecimal newLimit = DIVIDE.invoke(exprUpper.subtract(varBfactor.multiply(varBlimit)), varAfactor);

                newLimit = varAlowerOrg != null ? varAlowerOrg.max(newLimit) : newLimit;
                newLimit = varAupperOrg != null ? varAupperOrg.min(newLimit) : newLimit;

                if (varAfactor.signum() == 1) {
                    varAupperNew = newLimit;
                } else {
                    varAlowerNew = newLimit;
                }

            }

            final BigDecimal varAlimit = varAfactor.signum() == 1 ? varAlowerOrg : varAupperOrg;
            if (varAlimit != null) {

                BigDecimal newLimit = DIVIDE.invoke(exprUpper.subtract(varAfactor.multiply(varAlimit)), varBfactor);

                newLimit = varBlowerOrg != null ? varBlowerOrg.max(newLimit) : newLimit;
                newLimit = varBupperOrg != null ? varBupperOrg.min(newLimit) : newLimit;

                if (varBfactor.signum() == 1) {
                    varBupperNew = newLimit;
                } else {
                    varBlowerNew = newLimit;
                }

            }
        }

        if (variableA.isInteger()) {
            if (varAlowerNew != null) {
                varAlowerNew = varAlowerNew.setScale(0, RoundingMode.CEILING);
            }
            if (varAupperNew != null) {
                varAupperNew = varAupperNew.setScale(0, RoundingMode.FLOOR);
            }
        }

        if (variableB.isInteger()) {
            if (varBlowerNew != null) {
                varBlowerNew = varBlowerNew.setScale(0, RoundingMode.CEILING);
            }
            if (varBupperNew != null) {
                varBupperNew = varBupperNew.setScale(0, RoundingMode.FLOOR);
            }
        }

        variableA.lower(varAlowerNew).upper(varAupperNew);
        variableB.lower(varBlowerNew).upper(varBupperNew);

        return variableA.isEqualityConstraint() || variableB.isEqualityConstraint();
    }

}
