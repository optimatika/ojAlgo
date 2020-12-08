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
package org.ojalgo.optimisation.integer;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.ojalgo.core.array.operation.COPY;
import org.ojalgo.core.function.constant.PrimitiveMath;
import org.ojalgo.core.netio.BasicLogger;
import org.ojalgo.core.type.context.NumberContext;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ExpressionsBasedModel.Intermediate;
import org.ojalgo.optimisation.Variable;

final class NodeKey implements Comparable<NodeKey> {

    /**
     * Same scale as the default {@linkplain org.ojalgo.optimisation.Optimisation.Options#feasibility} and
     * half its precision.
     */
    private static final NumberContext FEASIBILITY = new NumberContext(7, 6);

    private final int[] myLowerBounds;
    private final boolean mySignChanged;
    private final int[] myUpperBounds;

    /**
     * How much the branched on variable must be displaced because of the new constraint introduced with this
     * node (each node introduces precisely 1 new upper or lower bound).
     */
    final double displacement;
    /**
     * The index of the branched on variable.
     */
    final int index;
    /**
     * The objective function value of the parent node.
     */
    final double objective;
    /**
     * Parent node sequence number.
     */
    final long parent;
    /**
     * Node sequennce number to keep track of in which order the nodes were created.
     */
    final long sequence;

    private NodeKey(final int[] lowerBounds, final int[] upperBounds, final long parentSequenceNumber, final long sequenceIncr,
            final int integerIndexBranchedOn, final double branchVariableDisplacement, final double parentObjectiveFunctionValue, final boolean signChanged) {

        super();

        sequence = parentSequenceNumber + sequenceIncr;

        myLowerBounds = lowerBounds;
        myUpperBounds = upperBounds;

        parent = parentSequenceNumber;
        index = integerIndexBranchedOn;
        displacement = branchVariableDisplacement;
        objective = parentObjectiveFunctionValue;

        mySignChanged = signChanged;
    }

    NodeKey(final ExpressionsBasedModel integerModel) {

        super();

        sequence = 0;

        final List<Variable> integerVariables = integerModel.getIntegerVariables();
        final int numberOfIntegerVariables = integerVariables.size();

        myLowerBounds = new int[numberOfIntegerVariables];
        myUpperBounds = new int[numberOfIntegerVariables];

        for (int i = 0; i < numberOfIntegerVariables; i++) {
            final Variable variable = integerVariables.get(i);

            final BigDecimal lowerLimit = variable.getLowerLimit();
            if (lowerLimit != null) {
                myLowerBounds[i] = lowerLimit.intValue();
            } else {
                myLowerBounds[i] = Integer.MIN_VALUE;
            }

            final BigDecimal upperLimit = variable.getUpperLimit();
            if (upperLimit != null) {
                myUpperBounds[i] = upperLimit.intValue();
            } else {
                myUpperBounds[i] = Integer.MAX_VALUE;
            }
        }

        parent = sequence;
        index = -1;
        displacement = PrimitiveMath.NaN;
        objective = PrimitiveMath.NaN;

        mySignChanged = false;
    }

    public int compareTo(final NodeKey ref) {
        return Double.compare(ref.displacement, displacement);
    }

    public void enforceBounds(final Intermediate nodeModel, final int[] integerIndices) {

        final BigDecimal lowerBound = this.getLowerBound(index);
        final BigDecimal upperBound = this.getUpperBound(index);

        final Variable variable = nodeModel.getVariable(integerIndices[index]);
        variable.lower(lowerBound);
        variable.upper(upperBound);

        final BigDecimal value = variable.getValue();
        if (value != null) {
            // Re-setting will ensure the new bounds are not violated
            variable.setValue(value);
        }

        if (this.isSignChanged()) {
            nodeModel.dispose();
        } else {
            nodeModel.update(variable);
        }
    }

    public boolean equals(final int[] lowerBounds, final int[] upperBounds) {
        if (!Arrays.equals(myLowerBounds, lowerBounds)) {
            return false;
        }
        if (!Arrays.equals(myUpperBounds, upperBounds)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NodeKey)) {
            return false;
        }
        final NodeKey other = (NodeKey) obj;
        return this.equals(other.myLowerBounds, other.myUpperBounds);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + Arrays.hashCode(myLowerBounds);
        result = (prime * result) + Arrays.hashCode(myUpperBounds);
        return result;
    }

    @Override
    public String toString() {

        final StringBuilder retVal = new StringBuilder();

        retVal.append(sequence);
        retVal.append(' ');
        retVal.append('(');
        retVal.append(parent);
        retVal.append(')');
        retVal.append(' ');
        retVal.append(index);
        retVal.append('=');
        retVal.append(displacement);
        retVal.append(' ');
        retVal.append(objective);
        retVal.append(' ');
        retVal.append('[');

        if (myLowerBounds.length > 0) {
            this.append(retVal, 0);
        }

        for (int i = 1; i < myLowerBounds.length; i++) {
            retVal.append(',');
            retVal.append(' ');
            this.append(retVal, i);
        }

        return retVal.append(']').toString();
    }

    private void append(final StringBuilder builder, final int index) {
        builder.append(index);
        builder.append('=');
        builder.append(myLowerBounds[index]);
        builder.append('<');
        builder.append(myUpperBounds[index]);
    }

    private double feasible(final int index, final double value, final boolean validate) {

        double feasibilityAdjusted = PrimitiveMath.MIN.invoke(PrimitiveMath.MAX.invoke(myLowerBounds[index], value), myUpperBounds[index]);

        if (validate && FEASIBILITY.isDifferent(feasibilityAdjusted, value)) {
            BasicLogger.error("Obviously infeasible value {}: {} <= {} <= {} @ {}", index, myLowerBounds[index], value, myUpperBounds[index], this);
        }

        return feasibilityAdjusted;
    }

    long calculateTreeSize() {

        long retVal = 1L;

        final int tmpLength = myLowerBounds.length;
        for (int i = 0; i < tmpLength; i++) {
            retVal *= (1L + (myUpperBounds[i] - myLowerBounds[i]));
        }

        return retVal;
    }

    NodeKey createLowerBranch(final int branchIntegerIndex, final double value, final double objective) {

        final int[] tmpLBs = this.getLowerBounds();
        final int[] tmpUBs = this.getUpperBounds();

        final double tmpFeasibleValue = this.feasible(branchIntegerIndex, value, false);

        final int tmpFloor = (int) PrimitiveMath.FLOOR.invoke(tmpFeasibleValue);

        int oldVal = tmpUBs[branchIntegerIndex];

        if ((tmpFloor >= tmpUBs[branchIntegerIndex]) && (tmpFloor > tmpLBs[branchIntegerIndex])) {
            tmpUBs[branchIntegerIndex] = tmpFloor - 1;
        } else {
            tmpUBs[branchIntegerIndex] = tmpFloor;
        }

        int newVal = tmpUBs[branchIntegerIndex];

        final boolean changed = (oldVal > 0) && (newVal <= 0);

        return new NodeKey(tmpLBs, tmpUBs, sequence, 1L, branchIntegerIndex, value - tmpFloor, objective, changed);
    }

    NodeKey createUpperBranch(final int branchIntegerIndex, final double value, final double objective) {

        final int[] tmpLBs = this.getLowerBounds();
        final int[] tmpUBs = this.getUpperBounds();

        final double tmpFeasibleValue = this.feasible(branchIntegerIndex, value, false);

        final int tmpCeil = (int) PrimitiveMath.CEIL.invoke(tmpFeasibleValue);

        int oldVal = tmpLBs[branchIntegerIndex];

        if ((tmpCeil <= tmpLBs[branchIntegerIndex]) && (tmpCeil < tmpUBs[branchIntegerIndex])) {
            tmpLBs[branchIntegerIndex] = tmpCeil + 1;
        } else {
            tmpLBs[branchIntegerIndex] = tmpCeil;
        }

        int newVal = tmpLBs[branchIntegerIndex];

        final boolean changed = (oldVal < 0) && (newVal >= 0);

        return new NodeKey(tmpLBs, tmpUBs, sequence, 2L, branchIntegerIndex, tmpCeil - value, objective, changed);
    }

    void enforceBounds(final ExpressionsBasedModel model, final int integerIndex, final int[] integerToGlobalTranslator) {

        final BigDecimal lowerBound = this.getLowerBound(integerIndex);
        final BigDecimal upperBound = this.getUpperBound(integerIndex);

        final Variable variable = model.getVariable(integerToGlobalTranslator[integerIndex]);
        variable.lower(lowerBound);
        variable.upper(upperBound);

        final BigDecimal value = variable.getValue();
        if (value != null) {
            // Re-setting will ensure the new bounds are not violated
            variable.setValue(value);
        }
    }

    double getFraction(final int index, final double value) {

        final double feasibleValue = this.feasible(index, value, true);

        return PrimitiveMath.ABS.invoke(feasibleValue - PrimitiveMath.RINT.invoke(feasibleValue));
    }

    BigDecimal getLowerBound(final int index) {
        final int tmpLower = myLowerBounds[index];
        if (tmpLower != Integer.MIN_VALUE) {
            return new BigDecimal(tmpLower);
        } else {
            return null;
        }
    }

    int[] getLowerBounds() {
        return COPY.copyOf(myLowerBounds);
    }

    BigDecimal getUpperBound(final int index) {
        final int tmpUpper = myUpperBounds[index];
        if (tmpUpper != Integer.MAX_VALUE) {
            return new BigDecimal(tmpUpper);
        } else {
            return null;
        }
    }

    int[] getUpperBounds() {
        return COPY.copyOf(myUpperBounds);
    }

    boolean isSignChanged() {
        return mySignChanged;
    }

    void setNodeState(final ExpressionsBasedModel model, final int[] integerIndices) {
        for (int i = 0; i < integerIndices.length; i++) {
            this.enforceBounds(model, i, integerIndices);
        }
    }

}
