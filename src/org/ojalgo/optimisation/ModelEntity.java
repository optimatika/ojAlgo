/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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

import java.math.BigDecimal;

import org.ojalgo.ProgrammingError;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * Model entities are identified and compared by their names only. Any/all other members/attributes are NOT part of
 * equals(), hashCode() or compareTo().
 *
 * @author apete
 */
abstract class ModelEntity<ME extends ModelEntity<ME>> implements Optimisation.Constraint, Optimisation.Objective, Comparable<ME> {

    private BigDecimal myContributionWeight = null;
    private BigDecimal myLowerLimit = null;
    private final String myName;
    private BigDecimal myUpperLimit = null;

    @SuppressWarnings("unused")
    private ModelEntity() {
        this("");
    }

    protected ModelEntity(final ME entityToCopy) {

        super();

        myName = entityToCopy.getName();

        myContributionWeight = entityToCopy.getContributionWeight();

        myLowerLimit = entityToCopy.getLowerLimit();
        myUpperLimit = entityToCopy.getUpperLimit();
    }

    protected ModelEntity(final String name) {

        super();

        myName = name;

        ProgrammingError.throwIfNull(name);
    }

    public final int compareTo(final ME obj) {
        return myName.compareTo(obj.getName());
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public final boolean equals(final Object obj) {

        boolean retVal = false;

        if (obj instanceof ModelEntity<?>) {
            if (myName.equals(((ModelEntity<?>) obj).getName())) {
                retVal = true;
            }
        }

        return retVal;
    }

    public final double getAdjustedLowerLimit() {

        final BigDecimal tmpLowerLimit = this.getLowerLimit(true);

        if (tmpLowerLimit != null) {
            return tmpLowerLimit.doubleValue();
        } else {
            return Double.NEGATIVE_INFINITY;
        }
    }

    public final double getAdjustedUpperLimit() {

        final BigDecimal tmpUpperLimit = this.getUpperLimit(true);

        if (tmpUpperLimit != null) {
            return tmpUpperLimit.doubleValue();
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }

    public final double getAdjustmentFactor() {
        return BigDecimal.ONE.movePointRight(this.getAdjustmentExponent()).doubleValue(); // 10^exponent
    }

    /**
     * @see org.ojalgo.optimisation.Objective#getContributionWeight()
     */
    public final BigDecimal getContributionWeight() {
        return myContributionWeight;
    }

    /**
     * @see org.ojalgo.optimisation.Constraint#getLowerLimit()
     */
    public final BigDecimal getLowerLimit() {
        return this.getLowerLimit(false);
    }

    public final String getName() {
        return myName;
    }

    /**
     * @see org.ojalgo.optimisation.Constraint#getUpperLimit()
     */
    public final BigDecimal getUpperLimit() {
        return this.getUpperLimit(false);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public final int hashCode() {
        return myName.hashCode();
    }

    /**
     * @see org.ojalgo.optimisation.Constraint#isConstraint()
     */
    public final boolean isConstraint() {
        return this.isLowerLimitSet() || this.isUpperLimitSet();
    }

    public final boolean isContributionWeightSet() {
        return myContributionWeight != null;
    }

    /**
     * @see org.ojalgo.optimisation.Constraint#isEqualityConstraint()
     */
    public final boolean isEqualityConstraint() {
        return this.isLowerLimitSet() && this.isUpperLimitSet() && (myLowerLimit.compareTo(myUpperLimit) == 0);
    }

    /**
     * @see org.ojalgo.optimisation.Constraint#isLowerConstraint()
     */
    public final boolean isLowerConstraint() {
        return this.isLowerLimitSet() && !this.isEqualityConstraint();
    }

    public final boolean isLowerLimitSet() {
        return myLowerLimit != null;
    }

    /**
     * @see org.ojalgo.optimisation.Objective#isObjective()
     */
    public final boolean isObjective() {
        return this.isContributionWeightSet() && (myContributionWeight.signum() != 0);
    }

    /**
     * @see org.ojalgo.optimisation.Constraint#isUpperConstraint()
     */
    public final boolean isUpperConstraint() {
        return this.isUpperLimitSet() && !this.isEqualityConstraint();
    }

    public final boolean isUpperLimitSet() {
        return myUpperLimit != null;
    }

    public final ME level(final Number level) {
        return this.lower(level).upper(level);
    }

    @SuppressWarnings("unchecked")
    public final ME lower(final Number lower) {
        if (lower != null) {
            myLowerLimit = TypeUtils.toBigDecimal(lower);
        } else {
            myLowerLimit = null;
        }
        return (ME) this;
    }

    @Override
    public final String toString() {

        final StringBuilder retVal = new StringBuilder();

        this.appendToString(retVal);

        return retVal.toString();
    }

    @SuppressWarnings("unchecked")
    public final ME upper(final Number upper) {
        if (upper != null) {
            myUpperLimit = TypeUtils.toBigDecimal(upper);
        } else {
            myUpperLimit = null;
        }
        return (ME) this;
    }

    @SuppressWarnings("unchecked")
    public final ME weight(final Number weight) {
        final BigDecimal tmpWeight = TypeUtils.toBigDecimal(weight);
        if ((tmpWeight != null) && (tmpWeight.signum() != 0)) {
            myContributionWeight = tmpWeight;
        } else {
            myContributionWeight = null;
        }
        return (ME) this;
    }

    protected void appendLeftPart(final StringBuilder builder) {
        if (this.isLowerConstraint() || this.isEqualityConstraint()) {
            builder.append(OptimisationUtils.DISPLAY.enforce(this.getLowerLimit()).toPlainString());
            builder.append(" <= ");
        }
    }

    protected void appendMiddlePart(final StringBuilder builder) {

        builder.append(this.getName());

        if (this.isObjective()) {
            builder.append(" (");
            builder.append(OptimisationUtils.DISPLAY.enforce(this.getContributionWeight()).toPlainString());
            builder.append(")");
        }
    }

    protected void appendRightPart(final StringBuilder builder) {
        if (this.isUpperConstraint() || this.isEqualityConstraint()) {
            builder.append(" <= ");
            builder.append(OptimisationUtils.DISPLAY.enforce(this.getUpperLimit()).toPlainString());
        }
    }

    protected void destroy() {
        myContributionWeight = null;
        myLowerLimit = null;
        myUpperLimit = null;
    }

    protected abstract int getAdjustmentExponent();

    protected boolean validate(final BasicLogger.Appender appender) {

        boolean retVal = true;

        if ((myLowerLimit != null) && (myUpperLimit != null)) {
            if ((myLowerLimit.compareTo(myUpperLimit) == 1) || (myUpperLimit.compareTo(myLowerLimit) == -1)) {
                appender.println(this.toString() + " The lower limit (if it exists) must be smaller than or equal to the upper limit (if it exists)!");
                retVal = false;
            }
        }

        if ((myContributionWeight != null) && (myContributionWeight.signum() == 0)) {
            appender.println(this.toString() + " The contribution weight (if it exists) should not be zero!");
            retVal = false;
        }

        return retVal;
    }

    protected boolean validate(final BigDecimal value, final NumberContext context, final BasicLogger.Appender appender) {

        boolean retVal = true;

        BigDecimal tmpLimit = null;

        if (((tmpLimit = this.getLowerLimit()) != null) && ((context.enforce(value.subtract(tmpLimit)).signum() == -1))) {
            appender.println(value + " ! " + this.toString());
            retVal = false;
        }

        if (((tmpLimit = this.getUpperLimit()) != null) && ((context.enforce(value.subtract(tmpLimit)).signum() == 1))) {
            appender.println(value + " ! " + this.toString());
            retVal = false;
        }

        return retVal;
    }

    final void appendToString(final StringBuilder builder) {
        this.appendLeftPart(builder);
        this.appendMiddlePart(builder);
        this.appendRightPart(builder);
    }

    final BigDecimal getLowerLimit(final boolean adjusted) {

        if (adjusted && (myLowerLimit != null)) {

            final int tmpAdjustmentExponent = this.getAdjustmentExponent();

            if (tmpAdjustmentExponent != 0) {

                return myLowerLimit.movePointRight(tmpAdjustmentExponent);

            } else {

                return myLowerLimit;
            }

        } else {

            return myLowerLimit;
        }
    }

    final BigDecimal getUpperLimit(final boolean adjusted) {

        if (adjusted && (myUpperLimit != null)) {

            final int tmpAdjustmentExponent = this.getAdjustmentExponent();

            if (tmpAdjustmentExponent != 0) {

                return myUpperLimit.movePointRight(tmpAdjustmentExponent);

            } else {

                return myUpperLimit;
            }

        } else {

            return myUpperLimit;
        }
    }

}
