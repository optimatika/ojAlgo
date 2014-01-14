/* 
 * Copyright 1997-2013 Optimatika (www.optimatika.se)
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

import org.ojalgo.function.aggregator.AggregatorCollection;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.BigAggregator;
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

    private transient int myAdjustmentExponent = Integer.MIN_VALUE;

    private Expression.Index myIndex = null;
    private boolean myInteger = false;
    private BigDecimal myValue = null;

    public Variable(final String name) {
        super(name);
    }

    protected Variable(final Variable entityToCopy) {

        super(entityToCopy);

        myIndex = null;
        myInteger = entityToCopy.isInteger();
        myValue = entityToCopy.getValue();
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

    public Variable integer(final boolean aBool) {
        this.setInteger(aBool);
        return this;
    }

    public boolean isBinary() {

        boolean retVal = this.isInteger();

        retVal &= this.isLowerConstraint() && this.getLowerLimit().equals(ZERO);

        retVal &= this.isUpperConstraint() && this.getUpperLimit().equals(ONE);

        return retVal;
    }

    public boolean isInteger() {
        return myInteger;
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

    public void setInteger(final boolean aBool) {
        myInteger = aBool;
        myAdjustmentExponent = Integer.MIN_VALUE;
    }

    public void setValue(final BigDecimal aValue) {
        myValue = aValue;
        myAdjustmentExponent = Integer.MIN_VALUE;
    }

    public boolean validate(final NumberContext context) {

        if (myValue != null) {

            return this.validate(myValue, context);

        } else {

            return false;
        }
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
    protected int getAdjustmentExponent() {

        if (myAdjustmentExponent == Integer.MIN_VALUE) {

            final AggregatorCollection<BigDecimal> tmpCollection = BigAggregator.getCollection();
            final AggregatorFunction<BigDecimal> tmpLargestAggr = tmpCollection.largest();
            final AggregatorFunction<BigDecimal> tmpSmallestAggr = tmpCollection.smallest();

            tmpLargestAggr.invoke(ONE);
            tmpSmallestAggr.invoke(ONE);

            final BigDecimal tmpLowerLimit = this.getLowerLimit();
            if (tmpLowerLimit != null) {
                tmpLargestAggr.invoke(tmpLowerLimit);
                tmpSmallestAggr.invoke(tmpLowerLimit);
            }

            final BigDecimal tmpUpperLimit = this.getUpperLimit();
            if (tmpUpperLimit != null) {
                tmpLargestAggr.invoke(tmpUpperLimit);
                tmpSmallestAggr.invoke(tmpUpperLimit);
            }

            myAdjustmentExponent = OptimisationUtils.getAdjustmentFactorExponent(tmpLargestAggr, tmpSmallestAggr);
        }

        return myAdjustmentExponent;
    }

    @Override
    protected boolean validate(final BigDecimal value, final NumberContext context) {

        boolean retVal = super.validate(value, context);

        if (retVal && myInteger) {
            try {
                context.enforce(value).longValueExact();
            } catch (final ArithmeticException ex) {
                //BasicLogger.logError(value + " ! Integer: " + this.getName());
                retVal = false;
            }
        }

        return retVal;
    }

    Expression.Index getIndex() {
        return myIndex;
    }

    void setIndex(final Expression.Index index) {
        if (index == null) {
            throw new IllegalArgumentException("The index cannot be null!");
        } else if ((myIndex != null) && (myIndex.index != index.index)) {
            throw new IllegalStateException("Cannot change a variable's index, or add it to more than one model!");
        }
        myIndex = index;
    }

}
