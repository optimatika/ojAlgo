/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.ojalgo.access.IntIndex;

public abstract class Presolvers {

    /**
     * Checks the sign of the limits and the sign of the expression parameters to deduce variables that in
     * fact can only zero.
     */
    public static final ExpressionsBasedModel.Presolver OPPOSITE_SIGN = new ExpressionsBasedModel.Presolver(20) {

        @Override
        public boolean simplify(final Expression expression, final Set<IntIndex> fixedVariables) {

            boolean tmpDidFixVariable = false;

            final ExpressionsBasedModel tmpModel = expression.getModel();

            final BigDecimal tmpFixedValue = expression.calculateFixedValue(fixedVariables);

            BigDecimal tmpCompLowLim = expression.getLowerLimit();
            if ((tmpCompLowLim != null) && (tmpFixedValue.signum() != 0)) {
                tmpCompLowLim = tmpCompLowLim.subtract(tmpFixedValue);
            }

            BigDecimal tmpCompUppLim = expression.getUpperLimit();
            if ((tmpCompUppLim != null) && (tmpFixedValue.signum() != 0)) {
                tmpCompUppLim = tmpCompUppLim.subtract(tmpFixedValue);
            }

            if ((tmpCompLowLim != null) && (tmpCompLowLim.signum() >= 0) && expression.isNegative(fixedVariables)) {

                if (tmpCompLowLim.signum() == 0) {

                    for (final IntIndex tmpLinear : expression.getLinearKeySet()) {
                        if (!fixedVariables.contains(tmpLinear)) {
                            final Variable tmpFreeVariable = tmpModel.getVariable(tmpLinear.index);

                            final boolean tmpValid = tmpFreeVariable.validate(ZERO, tmpModel.options.slack, tmpModel.options.debug_appender);
                            expression.setInfeasible(!tmpValid);

                            if (tmpValid) {
                                tmpFreeVariable.level(ZERO);
                                tmpFreeVariable.setValue(ZERO);
                                tmpDidFixVariable = true;
                            }
                        }
                    }

                    expression.setRedundant(true);

                } else {

                    expression.setInfeasible(true);
                }
            }

            if ((tmpCompUppLim != null) && (tmpCompUppLim.signum() <= 0) && expression.isPositive(fixedVariables)) {

                if (tmpCompUppLim.signum() == 0) {

                    for (final IntIndex tmpLinear : expression.getLinearKeySet()) {
                        if (!fixedVariables.contains(tmpLinear)) {
                            final Variable tmpFreeVariable = tmpModel.getVariable(tmpLinear.index);

                            final boolean tmpValid = tmpFreeVariable.validate(ZERO, tmpModel.options.slack, tmpModel.options.debug_appender);
                            expression.setInfeasible(!tmpValid);

                            if (tmpValid) {
                                tmpFreeVariable.level(ZERO);
                                tmpFreeVariable.setValue(ZERO);
                                tmpDidFixVariable = true;
                            }
                        }
                    }

                    expression.setRedundant(true);

                } else {

                    expression.setInfeasible(true);
                }
            }

            return tmpDidFixVariable;
        }

    };

    /**
     * Looks for constraint expressions with 0, 1 or 2 non-fixed variables. Transfers the constraints of the
     * expressions to the variables and then (if possible) marks the expression as redundant.
     */
    public static final ExpressionsBasedModel.Presolver ZERO_ONE_TWO = new ExpressionsBasedModel.Presolver(10) {

        @Override
        public boolean simplify(final Expression expression, final Set<IntIndex> fixedVariables) {

            boolean tmpDidFixVariable = false;

            if (expression.countLinearFactors() <= (fixedVariables.size() + 2)) {
                // This constraint can possibly be reduced to 0, 1 or 2 remaining linear factors

                final BigDecimal tmpFixedValue = expression.calculateFixedValue(fixedVariables);

                final HashSet<IntIndex> tmpRemainingLinear = new HashSet<IntIndex>(expression.getLinearKeySet());
                tmpRemainingLinear.removeAll(fixedVariables);

                switch (tmpRemainingLinear.size()) {

                case 0:

                    tmpDidFixVariable = Presolvers.doCase0(expression, tmpFixedValue, tmpRemainingLinear);
                    break;

                case 1:

                    tmpDidFixVariable = Presolvers.doCase1(expression, tmpFixedValue, tmpRemainingLinear);
                    break;

                case 2:

                    tmpDidFixVariable = Presolvers.doCase2(expression, tmpFixedValue, tmpRemainingLinear);
                    break;

                default:

                    break;
                }

            }

            return tmpDidFixVariable;
        }

    };

    /**
     * This constraint expression has 0 remaining free variable. It is entirely redundant.
     */
    static boolean doCase0(final Expression expression, final BigDecimal fixedValue, final HashSet<IntIndex> remaining) {

        expression.setRedundant(true);

        final ExpressionsBasedModel tmpModel = expression.getModel();

        final boolean tmpValid = expression.validate(fixedValue, tmpModel.options.slack, tmpModel.options.debug_appender);
        if (tmpValid) {
            expression.setInfeasible(false);
            expression.level(fixedValue);
        } else {
            expression.setInfeasible(true);
        }

        return false;
    }

    /**
     * This constraint expression has 1 remaining free variable. The lower/upper limits can be transferred to
     * that variable, and the expression marked as redundant.
     */
    static boolean doCase1(final Expression expression, final BigDecimal fixedValue, final HashSet<IntIndex> remaining) {

        final ExpressionsBasedModel tmpModel = expression.getModel();

        final IntIndex tmpIndex = remaining.iterator().next();
        final Variable tmpVariable = tmpModel.getVariable(tmpIndex.index);
        final BigDecimal tmpFactor = expression.get(tmpIndex);

        if (expression.isEqualityConstraint()) {
            // Simple case with equality constraint

            final BigDecimal tmpCompensatedLevel = SUBTRACT.invoke(expression.getUpperLimit(), fixedValue);
            final BigDecimal tmpSolutionValue = DIVIDE.invoke(tmpCompensatedLevel, tmpFactor);

            expression.setRedundant(true);

            final boolean tmpValid = tmpVariable.validate(tmpSolutionValue, tmpModel.options.slack, tmpModel.options.debug_appender);
            if (tmpValid) {
                expression.setInfeasible(false);
                tmpVariable.level(tmpSolutionValue);
            } else {
                expression.setInfeasible(true);
            }

        } else {
            // More general case

            final BigDecimal tmpLowerLimit = expression.getLowerLimit();
            final BigDecimal tmpUpperLimit = expression.getUpperLimit();

            final BigDecimal tmpCompensatedLower = tmpLowerLimit != null ? SUBTRACT.invoke(tmpLowerLimit, fixedValue) : tmpLowerLimit;
            final BigDecimal tmpCompensatedUpper = tmpUpperLimit != null ? SUBTRACT.invoke(tmpUpperLimit, fixedValue) : tmpUpperLimit;

            BigDecimal tmpLowerSolution = tmpCompensatedLower != null ? DIVIDE.invoke(tmpCompensatedLower, tmpFactor) : tmpCompensatedLower;
            BigDecimal tmpUpperSolution = tmpCompensatedUpper != null ? DIVIDE.invoke(tmpCompensatedUpper, tmpFactor) : tmpCompensatedUpper;
            if (tmpFactor.signum() < 0) {
                final BigDecimal tmpVal = tmpLowerSolution;
                tmpLowerSolution = tmpUpperSolution;
                tmpUpperSolution = tmpVal;
            }

            final BigDecimal tmpOldLower = tmpVariable.getLowerLimit();
            final BigDecimal tmpOldUpper = tmpVariable.getUpperLimit();

            BigDecimal tmpNewLower = tmpOldLower;
            if (tmpLowerSolution != null) {
                if (tmpOldLower != null) {
                    tmpNewLower = tmpOldLower.max(tmpLowerSolution);
                } else {
                    tmpNewLower = tmpLowerSolution;
                }
            }

            BigDecimal tmpNewUpper = tmpOldUpper;
            if (tmpUpperSolution != null) {
                if (tmpOldUpper != null) {
                    tmpNewUpper = tmpOldUpper.min(tmpUpperSolution);
                } else {
                    tmpNewUpper = tmpUpperSolution;
                }
            }

            if (tmpVariable.isInteger()) {
                if (tmpNewLower != null) {
                    tmpNewLower = tmpNewLower.setScale(0, RoundingMode.CEILING);
                }
                if (tmpNewUpper != null) {
                    tmpNewUpper = tmpNewUpper.setScale(0, RoundingMode.FLOOR);
                }
            }

            tmpVariable.lower(tmpNewLower).upper(tmpNewUpper);
            expression.setRedundant(true);

            final boolean tmpInfeasible = (tmpNewLower != null) && (tmpNewUpper != null) && (tmpNewLower.compareTo(tmpNewUpper) > 0);
            expression.setInfeasible(tmpInfeasible);
        }

        if (tmpVariable.isEqualityConstraint()) {
            tmpVariable.setValue(tmpVariable.getLowerLimit());
            return true;
        } else {
            return false;
        }
    }

    static boolean doCase2(final Expression expression, final BigDecimal fixedValue, final HashSet<IntIndex> remaining) {

        final ExpressionsBasedModel tmpModel = expression.getModel();

        final Iterator<IntIndex> tmpIterator = remaining.iterator();

        final IntIndex tmpIndexA = tmpIterator.next();
        final Variable tmpVariableA = tmpModel.getVariable(tmpIndexA.index);
        final BigDecimal tmpFactorA = expression.get(tmpIndexA);
        BigDecimal tmpLowerA = tmpVariableA.getLowerLimit();
        BigDecimal tmpUpperA = tmpVariableA.getUpperLimit();

        final IntIndex tmpIndexB = tmpIterator.next();
        final Variable tmpVariableB = tmpModel.getVariable(tmpIndexB.index);
        final BigDecimal tmpFactorB = expression.get(tmpIndexB);
        BigDecimal tmpLowerB = tmpVariableB.getLowerLimit();
        BigDecimal tmpUpperB = tmpVariableB.getUpperLimit();

        final BigDecimal tmpLowerLimit = expression.getLowerLimit() != null ? SUBTRACT.invoke(expression.getLowerLimit(), fixedValue)
                : expression.getLowerLimit();
        final BigDecimal tmpUpperLimit = expression.getUpperLimit() != null ? SUBTRACT.invoke(expression.getUpperLimit(), fixedValue)
                : expression.getUpperLimit();

        if (tmpLowerLimit != null) {

            final BigDecimal tmpOtherUpperA = tmpFactorB.signum() == 1 ? tmpVariableB.getUpperLimit() : tmpVariableB.getLowerLimit();
            final BigDecimal tmpOtherUpperB = tmpFactorA.signum() == 1 ? tmpVariableA.getUpperLimit() : tmpVariableA.getLowerLimit();

            if (tmpOtherUpperA != null) {

                final BigDecimal tmpNewLimit = DIVIDE.invoke(tmpLowerLimit.subtract(tmpFactorB.multiply(tmpOtherUpperA)), tmpFactorA);

                if (tmpFactorA.signum() == 1) {
                    // New lower limit on A
                    tmpLowerA = tmpLowerA != null ? tmpLowerA.max(tmpNewLimit) : tmpNewLimit;
                } else {
                    // New upper limit on A
                    tmpUpperA = tmpUpperA != null ? tmpUpperA.min(tmpNewLimit) : tmpNewLimit;
                }
            }

            if (tmpOtherUpperB != null) {

                final BigDecimal tmpNewLimit = DIVIDE.invoke(tmpLowerLimit.subtract(tmpFactorA.multiply(tmpOtherUpperB)), tmpFactorB);

                if (tmpFactorB.signum() == 1) {
                    // New lower limit on B
                    tmpLowerB = tmpLowerB != null ? tmpLowerB.max(tmpNewLimit) : tmpNewLimit;
                } else {
                    // New upper limit on B
                    tmpUpperB = tmpUpperB != null ? tmpUpperB.min(tmpNewLimit) : tmpNewLimit;
                }
            }
        }

        if (tmpUpperLimit != null) {

            final BigDecimal tmpOtherLowerA = tmpFactorB.signum() == 1 ? tmpVariableB.getLowerLimit() : tmpVariableB.getUpperLimit();
            final BigDecimal tmpOtherLowerB = tmpFactorA.signum() == 1 ? tmpVariableA.getLowerLimit() : tmpVariableA.getUpperLimit();

            if (tmpOtherLowerA != null) {

                final BigDecimal tmpNewLimit = DIVIDE.invoke(tmpUpperLimit.subtract(tmpFactorB.multiply(tmpOtherLowerA)), tmpFactorA);

                if (tmpFactorA.signum() == 1) {
                    // New upper limit on A
                    tmpUpperA = tmpUpperA != null ? tmpUpperA.min(tmpNewLimit) : tmpNewLimit;
                } else {
                    // New lower limit on A
                    tmpLowerA = tmpLowerA != null ? tmpLowerA.max(tmpNewLimit) : tmpNewLimit;
                }
            }

            if (tmpOtherLowerB != null) {

                final BigDecimal tmpNewLimit = DIVIDE.invoke(tmpUpperLimit.subtract(tmpFactorA.multiply(tmpOtherLowerB)), tmpFactorB);

                if (tmpFactorB.signum() == 1) {
                    // New upper limit on B
                    tmpUpperB = tmpUpperB != null ? tmpUpperB.min(tmpNewLimit) : tmpNewLimit;
                } else {
                    // New lower limit on B
                    tmpLowerB = tmpLowerB != null ? tmpLowerB.max(tmpNewLimit) : tmpNewLimit;
                }
            }
        }

        if (tmpVariableA.isInteger()) {
            if (tmpLowerA != null) {
                tmpLowerA = tmpLowerA.setScale(0, RoundingMode.CEILING);
            }
            if (tmpUpperA != null) {
                tmpUpperA = tmpUpperA.setScale(0, RoundingMode.FLOOR);
            }
        }

        if (tmpVariableB.isInteger()) {
            if (tmpLowerB != null) {
                tmpLowerB = tmpLowerB.setScale(0, RoundingMode.CEILING);
            }
            if (tmpUpperB != null) {
                tmpUpperB = tmpUpperB.setScale(0, RoundingMode.FLOOR);
            }
        }

        tmpVariableA.lower(tmpLowerA).upper(tmpUpperA);
        tmpVariableB.lower(tmpLowerB).upper(tmpUpperB);

        return tmpVariableA.isEqualityConstraint() || tmpVariableB.isEqualityConstraint();
    }

}
