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
 * Variable represents a decision variable in an optimization model.
 * <p>
 * Each variable has a unique index in the model and can have:
 * </p>
 * <ul>
 * <li>Lower and upper bounds (constraints)</li>
 * <li>A contribution weight in the objective function</li>
 * <li>An integer or continuous domain</li>
 * <li>A current value</li>
 * </ul>
 * <p>
 * Variables can be configured as:
 * </p>
 * <ul>
 * <li>Binary (0-1 integer variables)</li>
 * <li>Integer (whole number values only)</li>
 * <li>Continuous (any value within bounds)</li>
 * <li>Fixed (equal lower and upper bounds)</li>
 * <li>Unbounded (no effective limits)</li>
 * </ul>
 * <p>
 * As a subclass of ModelEntity, a Variable can function both as a constraint (through lower/upper bounds) and
 * as an objective function component (through its contribution weight).
 * </p>
 * <p>
 * Variables are typically created and managed through {@link ExpressionsBasedModel}, which assigns their
 * indices and incorporates them into the optimization problem.
 * </p>
 *
 * @author apete
 */
public final class Variable extends ModelEntity<Variable> {

    private final IntIndex myIndex;
    private boolean myInteger = false;
    private transient boolean myUnbounded = false;
    private BigDecimal myValue = null;

    Variable(final String name, final int index) {

        super(name);

        myIndex = new IntIndex(index);
    }

    Variable(final Variable variableToCopy) {

        super(variableToCopy);

        myIndex = variableToCopy.getIndex();
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

    @Override
    public int compareTo(final Variable obj) {
        return this.getIndex().compareTo(obj.getIndex());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof Variable)) {
            return false;
        }
        Variable other = (Variable) obj;
        return Objects.equals(myIndex, other.myIndex) && myInteger == other.myInteger && Objects.equals(myValue, other.myValue);
}

    public BigDecimal getBaseValue() {

        BigDecimal lower = this.getLowerLimit();
        BigDecimal upper = this.getUpperLimit();

        if (lower != null) {
            return lower;
        } else if (upper != null) {
            return upper;
        } else {
            return ZERO;
        }
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(myIndex, myInteger, myValue);
        return result;
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
     * Negative if the variable's range, when shifted using {@link #getBaseValue()}, includes < 0.0
     */
    public boolean isNegative() {

        BigDecimal lower = this.getLowerLimit();

        return lower == null;
    }

    /**
     * Positive if the variable's range, when shifted using {@link #getBaseValue()}, includes > 0.0
     */
    public boolean isPositive() {

        BigDecimal lower = this.getLowerLimit();
        BigDecimal upper = this.getUpperLimit();

        return upper == null || lower != null && lower.compareTo(upper) < 0;
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

    public Variable value(final BigDecimal value) {
        this.setValue(value);
        return this;
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

    @Override
    protected void destroy() {

        super.destroy();

        myValue = null;
    }

    @Override
    protected boolean validate(final BigDecimal value, final NumberContext context, final BasicLogger appender) {
        return this.validate(value, context, appender, false);
    }

    /**
     * Internal copy that includes the index
     */
    Variable copy() {
        return new Variable(this);
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

    @Override
    BigDecimal doShift(final BigDecimal value) {
        return value.subtract(this.getBaseValue());
    }

    IntIndex getIndex() {
        return myIndex;
    }

    boolean isFixed() {
        return this.isEqualityConstraint();
    }

    boolean isShifted() {
        return this.getBaseValue().compareTo(ZERO) != 0;
    }

    boolean isUnbounded() {
        return myUnbounded;
    }

    void setFixed(final BigDecimal value) {
        this.level(value).setValue(value);
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
