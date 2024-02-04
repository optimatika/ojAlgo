/*
 * Copyright 1997-2024 Optimatika
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
import java.math.RoundingMode;
import java.util.Objects;

import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.BigAggregator;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * Variable
 *
 * @author apete
 */
public final class Variable extends ModelEntity<Variable> {

    /**
     * @deprecated v53 Use {@link #ExpressionsBasedModel()} and {@link #newVariable(String)} instead.
     */
    @Deprecated
    public static Variable make(final String name) {
        return new Variable(name);
    }

    /**
     * @deprecated v53 Use {@link #ExpressionsBasedModel()} and {@link #newVariable(String)} instead.
     */
    @Deprecated
    public static Variable makeBinary(final String name) {
        return Variable.make(name).binary();
    }

    /**
     * @deprecated v53 Use {@link #ExpressionsBasedModel()} and {@link #newVariable(String)} instead.
     */
    @Deprecated
    public static Variable makeInteger(final String name) {
        return Variable.make(name).integer();
    }

    private IntIndex myIndex = null;
    private boolean myInteger = false;
    private transient boolean myUnbounded = false;
    private BigDecimal myValue = null;

    /**
     * @deprecated v53 Use {@link #ExpressionsBasedModel()} and {@link #newVariable(String)} instead.
     */
    @Deprecated
    public Variable(final String name) {
        super(name);
    }

    protected Variable(final Variable variableToCopy) {

        super(variableToCopy);

        myIndex = null;
        myInteger = variableToCopy.isInteger();
        myValue = variableToCopy.getValue();
    }

    @Override
    public void addTo(final Expression target, final BigDecimal scale) {
        target.add(this, scale);
    }

    /**
     * See {@link #isBinary()}.
     *
     * @see #getUpperLimit()
     * @see #isInteger()
     * @see #isBinary()
     */
    public Variable binary() {
        return this.lower(ZERO).upper(ONE).integer(true);
    }

    public int compareTo(final Variable obj) {
        return this.getIndex().compareTo(obj.getIndex());
    }

    /**
     * @return A copy that can be used with other models
     */
    public Variable copy() {
        return new Variable(this);
    }

    public BigDecimal getLowerSlack() {

        BigDecimal retVal = null;

        if (this.getLowerLimit() != null) {

            if (myValue != null) {

                retVal = this.getLowerLimit().subtract(myValue);

            } else {

                retVal = this.getLowerLimit();
            }
        }

        if (retVal != null && this.isInteger()) {
            retVal = retVal.setScale(0, BigDecimal.ROUND_CEILING);
        }

        return retVal;
    }

    public BigDecimal getUpperSlack() {

        BigDecimal retVal = null;

        if (this.getUpperLimit() != null) {

            if (myValue != null) {

                retVal = this.getUpperLimit().subtract(myValue);

            } else {

                retVal = this.getUpperLimit();
            }
        }

        if (retVal != null && this.isInteger()) {
            retVal = retVal.setScale(0, BigDecimal.ROUND_FLOOR);
        }

        return retVal;
    }

    public BigDecimal getValue() {
        if (myValue == null && this.isEqualityConstraint()) {
            myValue = this.getLowerLimit();
        }
        return myValue;
    }

    public Variable integer() {
        return this.integer(true);
    }

    /**
     * See {@link #isInteger()}.
     */
    public Variable integer(final boolean integer) {
        this.setInteger(integer);
        return this;
    }

    /**
     * Variable can only be 0 or 1.
     */
    public boolean isBinary() {
        return myInteger && this.isClosedRange(ZERO, ONE);
    }

    /**
     * @return true if this is an integer variable, otherwise false
     */
    @Override
    public boolean isInteger() {
        return myInteger;
    }

    /**
     * The range includes something < 0.0
     */
    public boolean isNegative() {
        return !this.isLowerLimitSet() || this.getLowerLimit().signum() < 0;
    }

    /**
     * The range includes something > 0.0
     */
    public boolean isPositive() {
        return !this.isUpperLimitSet() || this.getUpperLimit().signum() > 0;
    }

    public boolean isValueSet() {
        return myValue != null;
    }

    @Override
    public Variable lower(final Comparable<?> lower) {
        Variable retVal = super.lower(lower);
        this.assertFixedValue();
        return retVal;
    }

    public BigDecimal quantifyContribution() {

        BigDecimal retVal = ZERO;

        BigDecimal contributionWeight = this.getContributionWeight();
        if (contributionWeight != null && myValue != null) {
            retVal = contributionWeight.multiply(myValue);
        }

        return retVal;
    }

    public Variable relax() {
        return this.integer(false);
    }

    public void setInteger(final boolean integer) {
        myInteger = integer;
    }

    public void setValue(final Comparable<?> value) {
        BigDecimal tmpValue = null;
        if (value != null) {
            tmpValue = TypeUtils.toBigDecimal(value);
            if (this.isUpperLimitSet()) {
                tmpValue = tmpValue.min(this.getUpperLimit());
            }
            if (this.isLowerLimitSet()) {
                tmpValue = tmpValue.max(this.getLowerLimit());
            }
        }
        myValue = tmpValue;
    }

    @Override
    public Variable upper(final Comparable<?> upper) {
        Variable retVal = super.upper(upper);
        this.assertFixedValue();
        return retVal;
    }

    private void assertFixedValue() {
        if (this.isLowerLimitSet() && this.isUpperLimitSet() && this.getLowerLimit().compareTo(this.getUpperLimit()) == 0) {
            myValue = this.getLowerLimit();
        }
    }

    @Override
    protected void appendMiddlePart(final StringBuilder builder, final NumberContext display) {

        builder.append(this.getName());

        if (myValue != null) {
            builder.append(": ");
            builder.append(display.enforce(myValue).toPlainString());
        }

        if (this.isObjective()) {
            builder.append(" (");
            builder.append(display.enforce(this.getContributionWeight()).toPlainString());
            builder.append(")");
        }
    }

    /**
     * Internal copy that includes the index
     */
    @Override
    protected Variable clone() {

        Variable retVal = this.copy();

        retVal.setIndex(myIndex);

        return retVal;
    }

    @Override
    protected void destroy() {

        super.destroy();

        myIndex = null;
        myValue = null;
    }

    @Override
    protected boolean validate(final BigDecimal value, final NumberContext context, final BasicLogger appender) {
        return this.validate(value, context, appender, false);
    }

    @Override
    int deriveAdjustmentExponent() {

        if (!this.isConstraint() || this.isInteger()) {
            return 0;
        }

        AggregatorSet<BigDecimal> aggregators = BigAggregator.getSet();

        AggregatorFunction<BigDecimal> largest = aggregators.largest();
        AggregatorFunction<BigDecimal> smallest = aggregators.smallest();

        BigDecimal lowerLimit = this.getLowerLimit();
        if (lowerLimit != null) {
            if (lowerLimit.signum() == 0) {
                largest.invoke(ONE);
                smallest.invoke(ONE);
            } else {
                largest.invoke(lowerLimit);
                smallest.invoke(lowerLimit);
            }
        }

        BigDecimal upperLimit = this.getUpperLimit();
        if (upperLimit != null) {
            if (upperLimit.signum() == 0) {
                largest.invoke(ONE);
                smallest.invoke(ONE);
            } else {
                largest.invoke(upperLimit);
                smallest.invoke(upperLimit);
            }
        }

        return ModelEntity.deriveAdjustmentExponent(largest, smallest, RANGE);
    }

    @Override
    void doIntegerRounding() {
        if (myInteger) {
            BigDecimal limit;
            if ((limit = this.getUpperLimit()) != null && limit.scale() > 0) {
                this.upper(limit.setScale(0, RoundingMode.FLOOR));
            }
            if ((limit = this.getLowerLimit()) != null && limit.scale() > 0) {
                this.lower(limit.setScale(0, RoundingMode.CEILING));
            }
        }
    }

    IntIndex getIndex() {
        return myIndex;
    }

    boolean isFixed() {
        return this.isEqualityConstraint();
    }

    boolean isUnbounded() {
        return myUnbounded;
    }

    void setFixed(final BigDecimal value) {
        this.level(value).setValue(value);
    }

    void setIndex(final IntIndex index) {
        Objects.requireNonNull(index, "The index cannot be null!");
        if (myIndex != null && myIndex.index != index.index) {
            throw new IllegalStateException("Cannot change a variable's index, or add a variable to more than one model!");
        }
        myIndex = index;
    }

    void setUnbounded(final boolean uncorrelated) {
        myUnbounded = uncorrelated;
    }

    boolean validate(final BigDecimal value, final NumberContext context, final BasicLogger appender, final boolean relaxed) {

        boolean retVal = super.validate(value, context, appender);

        if (retVal && !relaxed && myInteger) {
            try {
                context.enforce(value).longValueExact();
            } catch (final ArithmeticException ex) {
                if (appender != null) {
                    appender.println(value + " ! Integer: " + this.getName());
                }
                retVal = false;
            }
        }

        return retVal;
    }

}
