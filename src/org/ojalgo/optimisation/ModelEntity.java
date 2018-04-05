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

import static org.ojalgo.constant.BigMath.ONE;
import static org.ojalgo.constant.PrimitiveMath.EIGHT;
import static org.ojalgo.constant.PrimitiveMath.TWO;
import static org.ojalgo.constant.PrimitiveMath.ZERO;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.ojalgo.ProgrammingError;
import org.ojalgo.constant.BigMath;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.BigAggregator;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * Model entities are identified and compared by their names only. Any/all other members/attributes are NOT
 * part of equals(), hashCode() or compareTo().
 *
 * @author apete
 */
abstract class ModelEntity<ME extends ModelEntity<ME>> implements Optimisation.Constraint, Optimisation.Objective, Comparable<ME> {

    private static final double _32_0 = EIGHT + EIGHT + EIGHT + EIGHT;
    private static final BigDecimal LARGEST = new BigDecimal(Double.toString(PrimitiveMath.MACHINE_LARGEST), new MathContext(8, RoundingMode.DOWN));
    private static final BigDecimal SMALLEST = new BigDecimal(Double.toString(PrimitiveMath.MACHINE_SMALLEST), new MathContext(8, RoundingMode.UP));

    static final NumberContext DISPLAY = NumberContext.getGeneral(6);

    static int getAdjustmentExponent(final double largest, final double smallest) {

        final double tmpLargestExp = largest > ZERO ? PrimitiveFunction.LOG10.invoke(largest) : ZERO;
        final double tmpSmallestExp = smallest > ZERO ? PrimitiveFunction.LOG10.invoke(smallest) : -EIGHT;

        if ((tmpLargestExp - tmpSmallestExp) > ModelEntity._32_0) {

            return 0;

        } else {

            final double tmpNegatedAverage = (tmpLargestExp + tmpSmallestExp) / (-TWO);

            return (int) Math.round(tmpNegatedAverage);
        }
    }

    private transient int myAdjustmentExponent = Integer.MIN_VALUE;
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

        myAdjustmentExponent = entityToCopy.getAdjustmentExponent();
    }

    protected ModelEntity(final String name) {

        super();

        myName = name;

        ProgrammingError.throwIfNull(name);
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
        return this.toLowerValue(true);
    }

    public final double getAdjustedUpperLimit() {
        return this.toUpperValue(true);
    }

    /**
     * @return Adjusted "1"
     */
    public final double getAdjustmentFactor() {
        return BigDecimal.ONE.movePointRight(this.getAdjustmentExponent()).doubleValue(); // 10^exponent
    }

    public final BigDecimal getContributionWeight() {
        return myContributionWeight;
    }

    public final BigDecimal getLowerLimit() {
        return myLowerLimit;
    }

    public final String getName() {
        return myName;
    }

    public final double getUnadjustedLowerLimit() {
        return this.toLowerValue(false);
    }

    public final double getUnadjustedUpperLimit() {
        return this.toUpperValue(false);
    }

    public final BigDecimal getUpperLimit() {
        return myUpperLimit;
    }

    @Override
    public final int hashCode() {
        return myName.hashCode();
    }

    public final boolean isConstraint() {
        return this.isLowerLimitSet() || this.isUpperLimitSet();
    }

    public final boolean isContributionWeightSet() {
        return myContributionWeight != null;
    }

    public final boolean isEqualityConstraint() {
        return this.isLowerLimitSet() && this.isUpperLimitSet() && (myLowerLimit.compareTo(myUpperLimit) == 0);
    }

    public final boolean isLowerConstraint() {
        return this.isLowerLimitSet() && !this.isEqualityConstraint();
    }

    public final boolean isLowerLimitSet() {
        return myLowerLimit != null;
    }

    public final boolean isObjective() {
        return this.isContributionWeightSet() && (myContributionWeight.signum() != 0);
    }

    public final boolean isUpperConstraint() {
        return this.isUpperLimitSet() && !this.isEqualityConstraint();
    }

    public final boolean isUpperLimitSet() {
        return myUpperLimit != null;
    }

    /**
     * @see #getLowerLimit()
     * @see #getUpperLimit()
     */
    public final ME level(final Number level) {
        return this.lower(level).upper(level);
    }

    /**
     * Extremely large (absolute value) values are treated as "no limit" (null) and extremely small values are
     * treated as exactly 0.0, unless the input number type is {@link BigDecimal}. {@link BigDecimal} values
     * are always used as they are.
     */
    @SuppressWarnings("unchecked")
    public final ME lower(final Number lower) {
        myLowerLimit = null;
        if (lower != null) {
            if (lower instanceof BigDecimal) {
                myLowerLimit = (BigDecimal) lower;
            } else if (Double.isFinite(lower.doubleValue())) {
                BigDecimal tmpLimit = TypeUtils.toBigDecimal(lower);
                final BigDecimal tmpMagnitude = tmpLimit.abs();
                if (tmpMagnitude.compareTo(LARGEST) >= 0) {
                    tmpLimit = null;
                } else if (tmpMagnitude.compareTo(SMALLEST) <= 0) {
                    tmpLimit = BigMath.ZERO;
                }
                myLowerLimit = tmpLimit;
            }
        }
        return (ME) this;
    }

    @Override
    public final String toString() {

        final StringBuilder retVal = new StringBuilder();

        this.appendToString(retVal);

        return retVal.toString();
    }

    /**
     * Extremely large (absolute value) values are treated as "no limit" (null) and extremely small values are
     * treated as exactly 0.0, unless the input number type is {@link BigDecimal}. {@link BigDecimal} values
     * are always used as they are.
     */
    @SuppressWarnings("unchecked")
    public final ME upper(final Number upper) {
        myUpperLimit = null;
        if (upper != null) {
            if (upper instanceof BigDecimal) {
                myUpperLimit = (BigDecimal) upper;
            } else if (Double.isFinite(upper.doubleValue())) {
                BigDecimal tmpLimit = TypeUtils.toBigDecimal(upper);
                final BigDecimal tmpMagnitude = tmpLimit.abs();
                if (tmpMagnitude.compareTo(LARGEST) >= 0) {
                    tmpLimit = null;
                } else if (tmpMagnitude.compareTo(SMALLEST) <= 0) {
                    tmpLimit = BigMath.ZERO;
                }
                myUpperLimit = tmpLimit;
            }
        }
        return (ME) this;
    }

    /**
     * @see #getContributionWeight()
     */
    @SuppressWarnings("unchecked")
    public final ME weight(final Number weight) {
        myContributionWeight = null;
        if (weight != null) {
            BigDecimal tmpWeight = null;
            if (weight instanceof BigDecimal) {
                tmpWeight = (BigDecimal) weight;
            } else if (Double.isFinite(weight.doubleValue())) {
                tmpWeight = TypeUtils.toBigDecimal(weight);
            }
            if ((tmpWeight != null) && (tmpWeight.signum() != 0)) {
                myContributionWeight = tmpWeight;
            }
        }
        return (ME) this;
    }

    private double toLowerValue(final boolean adjusted) {

        final BigDecimal limit;
        if (adjusted && (myLowerLimit != null)) {
            final int adjustmentExponent = this.getAdjustmentExponent();
            if (adjustmentExponent != 0) {
                limit = myLowerLimit.movePointRight(adjustmentExponent);
            } else {
                limit = myLowerLimit;
            }
        } else {
            limit = myLowerLimit;
        }

        if (limit != null) {
            return limit.doubleValue();
        } else {
            return Double.NEGATIVE_INFINITY;
        }
    }

    private double toUpperValue(final boolean adjusted) {

        final BigDecimal limit;
        if (adjusted && (myUpperLimit != null)) {
            final int adjustmentExponent = this.getAdjustmentExponent();
            if (adjustmentExponent != 0) {
                limit = myUpperLimit.movePointRight(adjustmentExponent);
            } else {
                limit = myUpperLimit;
            }
        } else {
            limit = myUpperLimit;
        }

        if (limit != null) {
            return limit.doubleValue();
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }

    protected void appendLeftPart(final StringBuilder builder) {
        if (this.isLowerConstraint() || this.isEqualityConstraint()) {
            builder.append(ModelEntity.DISPLAY.enforce(this.getLowerLimit()).toPlainString());
            builder.append(" <= ");
        }
    }

    protected void appendMiddlePart(final StringBuilder builder) {

        builder.append(this.getName());

        if (this.isObjective()) {
            builder.append(" (");
            builder.append(ModelEntity.DISPLAY.enforce(this.getContributionWeight()).toPlainString());
            builder.append(")");
        }
    }

    protected void appendRightPart(final StringBuilder builder) {
        if (this.isUpperConstraint() || this.isEqualityConstraint()) {
            builder.append(" <= ");
            builder.append(ModelEntity.DISPLAY.enforce(this.getUpperLimit()).toPlainString());
        }
    }

    protected void destroy() {
        myContributionWeight = null;
        myLowerLimit = null;
        myUpperLimit = null;
    }

    protected final int getAdjustmentExponent() {

        if (myAdjustmentExponent == Integer.MIN_VALUE) {

            final AggregatorSet<BigDecimal> tmpSet = BigAggregator.getSet();

            final AggregatorFunction<BigDecimal> tmpLargest = tmpSet.largest();
            final AggregatorFunction<BigDecimal> tmpSmallest = tmpSet.smallest();

            this.visitAllParameters(tmpLargest, tmpSmallest);

            myAdjustmentExponent = ModelEntity.getAdjustmentExponent(tmpLargest.doubleValue(), tmpSmallest.doubleValue());
        }

        return myAdjustmentExponent;
    }

    /**
     * Validate model parameters, like lower and upper limits. Does not validate the solution/value.
     */
    protected final boolean validate(final BasicLogger.Printer appender) {

        boolean retVal = true;

        if ((myLowerLimit != null) && (myUpperLimit != null)) {
            if ((myLowerLimit.compareTo(myUpperLimit) == 1) || (myUpperLimit.compareTo(myLowerLimit) == -1)) {
                if (appender != null) {
                    appender.println(this.toString() + " The lower limit (if it exists) must be smaller than or equal to the upper limit (if it exists)!");
                }
                retVal = false;
            }
        }

        if ((myContributionWeight != null) && (myContributionWeight.signum() == 0)) {
            if (appender != null) {
                appender.println(this.toString() + " The contribution weight (if it exists) should not be zero!");
            }
            retVal = false;
        }

        return retVal;
    }

    protected boolean validate(final BigDecimal value, final NumberContext context, final BasicLogger.Printer appender) {

        boolean retVal = true;

        BigDecimal tmpLimit = null;

        if (((tmpLimit = this.getLowerLimit()) != null) && (value.subtract(tmpLimit).signum() == -1)
                && context.isDifferent(tmpLimit.doubleValue(), value.doubleValue())) {
            if (appender != null) {
                appender.println(value + " ! " + this.toString());
            }
            retVal = false;
        }

        if (((tmpLimit = this.getUpperLimit()) != null) && (value.subtract(tmpLimit).signum() == 1)
                && context.isDifferent(tmpLimit.doubleValue(), value.doubleValue())) {
            if (appender != null) {
                appender.println(value + " ! " + this.toString());
            }
            retVal = false;
        }

        return retVal;
    }

    final void appendToString(final StringBuilder builder) {
        this.appendLeftPart(builder);
        this.appendMiddlePart(builder);
        this.appendRightPart(builder);
    }

    boolean isInfeasible() {
        return (myLowerLimit != null) && (myUpperLimit != null) && (myLowerLimit.compareTo(myUpperLimit) > 0);
    }

    void visitAllParameters(final VoidFunction<BigDecimal> largest, final VoidFunction<BigDecimal> smallest) {
        largest.invoke(ONE);
        smallest.invoke(ONE);
        if (myLowerLimit != null) {
            largest.invoke(myLowerLimit);
            smallest.invoke(myLowerLimit);
        }
        if (myUpperLimit != null) {
            largest.invoke(myUpperLimit);
            smallest.invoke(myUpperLimit);
        }
    }

}
