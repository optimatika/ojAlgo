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

import java.math.BigDecimal;

import org.ojalgo.access.IntIndex;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * Variable
 *
 * @author apete
 */
public final class Variable extends ModelEntity<Variable> {

    public static Variable make(final String name) {
        return new Variable(name);
    }

    public static Variable makeBinary(final String name) {
        return Variable.make(name).binary();
    }

    private IntIndex myIndex = null;
    private boolean myInteger = false;
    private BigDecimal myValue = null;

    public Variable(final String name) {
        super(name);
    }

    protected Variable(final Variable variableToCopy) {

        super(variableToCopy);

        myIndex = null;
        myInteger = variableToCopy.isInteger();
        myValue = variableToCopy.getValue();
    }

    public Variable binary() {
        return this.lower(ZERO).upper(ONE).integer(true);
    }

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

        if ((retVal != null) && this.isInteger()) {
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

        if ((retVal != null) && this.isInteger()) {
            retVal = retVal.setScale(0, BigDecimal.ROUND_FLOOR);
        }

        return retVal;
    }

    public BigDecimal getValue() {
        return myValue;
    }

    public Variable integer(final boolean integer) {
        this.setInteger(integer);
        return this;
    }

    public boolean isBinary() {

        boolean retVal = this.isInteger();

        retVal &= this.isLowerConstraint() && (this.getLowerLimit().compareTo(ZERO) == 0);

        retVal &= this.isUpperConstraint() && (this.getUpperLimit().compareTo(ONE) == 0);

        return retVal;
    }

    public boolean isInteger() {
        return myInteger;
    }

    public boolean isNegative() {
        return !this.isLowerLimitSet() || (this.getLowerLimit().signum() < 0);
    }

    public boolean isPositive() {
        return !this.isUpperLimitSet() || (this.getUpperLimit().signum() > 0);
    }

    public boolean isValueSet() {
        return myValue != null;
    }

    public BigDecimal quantifyContribution() {

        BigDecimal retVal = ZERO;

        final BigDecimal tmpContributionWeight = this.getContributionWeight();
        if ((tmpContributionWeight != null) && (myValue != null)) {
            retVal = tmpContributionWeight.multiply(myValue);
        }

        return retVal;
    }

    public Variable relax() {
        return this.integer(false);
    }

    public void setInteger(final boolean integer) {
        myInteger = integer;
    }

    public void setValue(final Number value) {
        myValue = TypeUtils.toBigDecimal(value);
    }

    @Override
    protected void appendMiddlePart(final StringBuilder aStringBuilder) {

        aStringBuilder.append(this.getName());

        if (myValue != null) {
            aStringBuilder.append(": ");
            aStringBuilder.append(OptimisationUtils.DISPLAY.enforce(myValue).toPlainString());
        }

        if (this.isObjective()) {
            aStringBuilder.append(" (");
            aStringBuilder.append(OptimisationUtils.DISPLAY.enforce(this.getContributionWeight()).toPlainString());
            aStringBuilder.append(")");
        }
    }

    @Override
    protected void destroy() {

        super.destroy();

        myIndex = null;
        myValue = null;
    }

    @Override
    protected boolean validate(final BigDecimal value, final NumberContext context, final BasicLogger.Printer appender) {

        boolean retVal = super.validate(value, context, appender);

        if (retVal && myInteger) {
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

    protected boolean validate(final NumberContext context, final BasicLogger.Printer appender) {

        if (myValue != null) {

            return this.validate(myValue, context, appender);

        } else {

            return false;
        }
    }

    IntIndex getIndex() {
        return myIndex;
    }

    void setIndex(final IntIndex index) {
        if (index == null) {
            throw new IllegalArgumentException("The index cannot be null!");
        } else if ((myIndex != null) && (myIndex.index != index.index)) {
            throw new IllegalStateException("Cannot change a variable's index, or add a variable to more than one model!");
        }
        myIndex = index;
    }

}
