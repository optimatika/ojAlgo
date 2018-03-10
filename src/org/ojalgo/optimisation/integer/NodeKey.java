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
package org.ojalgo.optimisation.integer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.ojalgo.array.Raw1D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Variable;

final class NodeKey implements Serializable, Comparable<NodeKey> {

    private static AtomicLong GENERATOR = new AtomicLong();

    private final int[] myLowerBounds;
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
    final long sequence = GENERATOR.getAndIncrement();

    private NodeKey(final int[] lowerBounds, final int[] upperBounds, final long parentSequenceNumber, final int indexBranchedOn,
            final double branchVariableDisplacement, final double parentObjectiveFunctionValue) {

        super();

        myLowerBounds = lowerBounds;
        myUpperBounds = upperBounds;

        parent = parentSequenceNumber;
        index = indexBranchedOn;
        displacement = branchVariableDisplacement;
        objective = parentObjectiveFunctionValue;
    }

    NodeKey(final ExpressionsBasedModel integerModel) {

        super();

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
    }

    public int compareTo(final NodeKey ref) {
        return Long.compare(sequence, ref.sequence);
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
        if (!Arrays.equals(myLowerBounds, other.myLowerBounds)) {
            return false;
        }
        if (!Arrays.equals(myUpperBounds, other.myUpperBounds)) {
            return false;
        }
        return true;
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

    private double feasible(final int index, final double value) {
        return PrimitiveFunction.MIN.invoke(PrimitiveFunction.MAX.invoke(myLowerBounds[index], value), myUpperBounds[index]);
    }

    void bound(final ExpressionsBasedModel model, final int[] integerIndices) {

        for (int i = 0; i < integerIndices.length; i++) {

            final BigDecimal lowerBound = this.getLowerBound(i);
            final BigDecimal upperBound = this.getUpperBound(i);

            final Variable variable = model.getVariable(integerIndices[i]);
            variable.lower(lowerBound);
            variable.upper(upperBound);

            final BigDecimal value = variable.getValue();
            if (value != null) {
                // Re-setting will ensure the new bounds are not violated
                variable.setValue(value);
            }
        }
    }

    long calculateTreeSize() {

        long retVal = 1L;

        final int tmpLength = myLowerBounds.length;
        for (int i = 0; i < tmpLength; i++) {
            retVal *= (1L + (myUpperBounds[i] - myLowerBounds[i]));
        }

        return retVal;
    }

    NodeKey createLowerBranch(final int index, final double value, final double objective) {

        final int[] tmpLBs = this.getLowerBounds();
        final int[] tmpUBs = this.getUpperBounds();

        final double tmpFeasibleValue = this.feasible(index, value);

        final int tmpFloor = (int) PrimitiveFunction.FLOOR.invoke(tmpFeasibleValue);

        if ((tmpFloor >= tmpUBs[index]) && (tmpFloor > tmpLBs[index])) {
            tmpUBs[index] = tmpFloor - 1;
        } else {
            tmpUBs[index] = tmpFloor;
        }

        return new NodeKey(tmpLBs, tmpUBs, sequence, index, value - tmpFloor, objective);
    }

    NodeKey createUpperBranch(final int index, final double value, final double objective) {

        final int[] tmpLBs = this.getLowerBounds();
        final int[] tmpUBs = this.getUpperBounds();

        final double tmpFeasibleValue = this.feasible(index, value);

        final int tmpCeil = (int) PrimitiveFunction.CEIL.invoke(tmpFeasibleValue);

        if ((tmpCeil <= tmpLBs[index]) && (tmpCeil < tmpUBs[index])) {
            tmpLBs[index] = tmpCeil + 1;
        } else {
            tmpLBs[index] = tmpCeil;
        }

        return new NodeKey(tmpLBs, tmpUBs, sequence, index, tmpCeil - value, objective);
    }

    double getFraction(final int index, final double value) {

        final double feasibleValue = this.feasible(index, value);

        return PrimitiveFunction.ABS.invoke(feasibleValue - PrimitiveFunction.RINT.invoke(feasibleValue));
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
        return Raw1D.copyOf(myLowerBounds);
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
        return Raw1D.copyOf(myUpperBounds);
    }

}
