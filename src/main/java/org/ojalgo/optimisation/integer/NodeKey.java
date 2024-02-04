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
package org.ojalgo.optimisation.integer;

import static org.ojalgo.function.constant.PrimitiveMath.NaN;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.ojalgo.array.operation.COPY;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.ObjectPool;
import org.ojalgo.type.context.NumberContext;

public final class NodeKey implements Comparable<NodeKey> {

    static final class IntArrayPool extends ObjectPool<int[]> {

        private final int myArrayLength;

        IntArrayPool(final int arrayLength) {
            super();
            myArrayLength = arrayLength;
        }

        @Override
        protected int[] newObject() {
            return new int[myArrayLength];
        }

        @Override
        protected void reset(final int[] object) {
            // No need to do anything. All values explicitly set when reused.
        }

    }

    public static final Comparator<NodeKey> EARLIEST_SEQUENCE = Comparator.comparingLong((final NodeKey nk) -> nk.sequence).reversed();
    public static final Comparator<NodeKey> LARGEST_DISPLACEMENT = Comparator.comparingDouble((final NodeKey nk) -> nk.displacement);
    public static final Comparator<NodeKey> LATEST_SEQUENCE = Comparator.comparingLong((final NodeKey nk) -> nk.sequence);
    public static final Comparator<NodeKey> MAX_OBJECTIVE = Comparator.comparingDouble((final NodeKey nk) -> nk.objective);
    public static final Comparator<NodeKey> MIN_OBJECTIVE = Comparator.comparingDouble((final NodeKey nk) -> nk.objective).reversed();
    public static final Comparator<NodeKey> SMALLEST_DISPLACEMENT = Comparator.comparingDouble((final NodeKey nk) -> nk.displacement).reversed();

    /**
     * Used for one thing only - to validate (log problems with) node solver results. Does not effect the
     * algorithm.
     */
    private static final NumberContext FEASIBILITY = NumberContext.of(8, 6);
    private static final AtomicLong SEQUENCE_GENERATOR = new AtomicLong();

    /**
     * How much the branched on variable must be displaced because of the new constraint introduced with this
     * node (each node introduces precisely 1 new upper or lower bound).
     */
    public final double displacement;
    /**
     * The index of the branched on variable.
     */
    public final int index;
    /**
     * The objective function value of the parent node.
     */
    public final double objective;
    /**
     * Parent node sequence number.
     */
    public final long parent;
    /**
     * Node sequennce number to keep track of in which order the nodes were created.
     */
    public final long sequence;

    private final IntArrayPool myIntArrayPool;
    private final int[] myLowerBounds;
    private final boolean mySignChanged;
    private final int[] myUpperBounds;

    private NodeKey(final int[] lowerBounds, final int[] upperBounds, final long parentSequenceNumber, final int integerIndexBranchedOn,
            final double branchVariableDisplacement, final double parentObjectiveFunctionValue, final boolean signChanged, final IntArrayPool pool) {

        super();

        sequence = SEQUENCE_GENERATOR.incrementAndGet();

        myLowerBounds = lowerBounds;
        myUpperBounds = upperBounds;

        parent = parentSequenceNumber;
        index = integerIndexBranchedOn;
        displacement = branchVariableDisplacement;
        objective = parentObjectiveFunctionValue;

        mySignChanged = signChanged;

        myIntArrayPool = pool;
    }

    NodeKey(final ExpressionsBasedModel integerModel) {

        super();

        sequence = 0L;

        List<Variable> integerVariables = integerModel.getIntegerVariables();
        int nbIntegerVariables = integerVariables.size();

        myIntArrayPool = new IntArrayPool(nbIntegerVariables);

        myLowerBounds = myIntArrayPool.borrow();
        myUpperBounds = myIntArrayPool.borrow();

        for (int i = 0; i < nbIntegerVariables; i++) {
            Variable variable = integerVariables.get(i);

            BigDecimal lowerLimit = variable.getLowerLimit();
            if (lowerLimit != null) {
                myLowerBounds[i] = lowerLimit.intValue();
            } else {
                myLowerBounds[i] = Integer.MIN_VALUE;
            }

            BigDecimal upperLimit = variable.getUpperLimit();
            if (upperLimit != null) {
                myUpperBounds[i] = upperLimit.intValue();
            } else {
                myUpperBounds[i] = Integer.MAX_VALUE;
            }
        }

        parent = sequence;
        index = -1;
        displacement = NaN;
        objective = NaN;

        mySignChanged = false;
    }

    public int compareTo(final NodeKey ref) {
        return Long.compare(sequence, ref.sequence);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NodeKey)) {
            return false;
        }
        NodeKey other = (NodeKey) obj;
        if (sequence != other.sequence) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (sequence ^ sequence >>> 32);
        return result;
    }

    @Override
    public String toString() {

        StringBuilder retVal = new StringBuilder();

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

    private void append(final StringBuilder builder, final int idx) {
        builder.append(idx);
        builder.append('=');
        builder.append(myLowerBounds[idx]);
        builder.append('<');
        builder.append(myUpperBounds[idx]);
    }

    private double feasible(final int idx, final double value, final boolean validate) {

        double feasibilityAdjusted = Math.min(Math.max(myLowerBounds[idx], value), myUpperBounds[idx]);

        if (validate && FEASIBILITY.isDifferent(feasibilityAdjusted, value)) {
            BasicLogger.error("Obviously infeasible value {}: {} <= {} <= {} @ {}", idx, myLowerBounds[idx], value, myUpperBounds[idx], this);
        }

        return feasibilityAdjusted;
    }

    long calculateTreeSize() {

        long retVal = 1L;

        for (int i = 0, limit = myLowerBounds.length; i < limit; i++) {
            retVal *= 1L + (myUpperBounds[i] - myLowerBounds[i]);
        }

        return retVal;
    }

    int[] copyLowerBounds() {
        return COPY.invoke(myLowerBounds, myIntArrayPool.borrow());
    }

    int[] copyUpperBounds() {
        return COPY.invoke(myUpperBounds, myIntArrayPool.borrow());
    }

    NodeKey createLowerBranch(final int branchIntegerIndex, final double value, final double objVal) {

        int[] tmpLBs = this.copyLowerBounds();
        int[] tmpUBs = this.copyUpperBounds();

        int floorValue = (int) Math.floor(this.feasible(branchIntegerIndex, value, false));

        int oldVal = tmpUBs[branchIntegerIndex];

        if (floorValue >= tmpUBs[branchIntegerIndex] && floorValue > tmpLBs[branchIntegerIndex]) {
            tmpUBs[branchIntegerIndex] = floorValue - 1;
        } else {
            tmpUBs[branchIntegerIndex] = floorValue;
        }

        int newVal = tmpUBs[branchIntegerIndex];

        boolean changed = oldVal > 0 && newVal <= 0;

        return new NodeKey(tmpLBs, tmpUBs, sequence, branchIntegerIndex, value - floorValue, objVal, changed, myIntArrayPool);
    }

    NodeKey createUpperBranch(final int branchIntegerIndex, final double value, final double objVal) {

        int[] tmpLBs = this.copyLowerBounds();
        int[] tmpUBs = this.copyUpperBounds();

        int ceilValue = (int) Math.ceil(this.feasible(branchIntegerIndex, value, false));

        int oldVal = tmpLBs[branchIntegerIndex];

        if (ceilValue <= tmpLBs[branchIntegerIndex] && ceilValue < tmpUBs[branchIntegerIndex]) {
            tmpLBs[branchIntegerIndex] = ceilValue + 1;
        } else {
            tmpLBs[branchIntegerIndex] = ceilValue;
        }

        int newVal = tmpLBs[branchIntegerIndex];

        boolean changed = oldVal < 0 && newVal >= 0;

        return new NodeKey(tmpLBs, tmpUBs, sequence, branchIntegerIndex, ceilValue - value, objVal, changed, myIntArrayPool);
    }

    void dispose() {
        myIntArrayPool.giveBack(myLowerBounds);
        myIntArrayPool.giveBack(myUpperBounds);
    }

    void enforceBounds(final ExpressionsBasedModel model, final int idx, final ModelStrategy strategy) {

        BigDecimal lowerBound = this.getLowerBound(idx);
        BigDecimal upperBound = this.getUpperBound(idx);

        Variable variable = model.getVariable(strategy.getIndex(idx));
        variable.lower(lowerBound);
        variable.upper(upperBound);

        BigDecimal value = variable.getValue();
        if (value != null) {
            // Re-setting will ensure the new bounds are not violated
            variable.setValue(value);
        }
    }

    void enforceBounds(final NodeSolver nodeSolver, final ModelStrategy strategy) {

        BigDecimal lowerBound = this.getLowerBound(index);
        BigDecimal upperBound = this.getUpperBound(index);

        Variable variable = nodeSolver.getVariable(strategy.getIndex(index));
        variable.lower(lowerBound);
        variable.upper(upperBound);

        BigDecimal value = variable.getValue();
        if (value != null) {
            // Re-setting will ensure the new bounds are not violated
            variable.setValue(value);
        }

        if (this.isSignChanged()) {
            nodeSolver.reset();
        } else {
            nodeSolver.update(variable);
        }
    }

    boolean equals(final int[] lowerBounds, final int[] upperBounds) {
        if (!Arrays.equals(myLowerBounds, lowerBounds) || !Arrays.equals(myUpperBounds, upperBounds)) {
            return false;
        }
        return true;
    }

    BigDecimal getLowerBound(final int idx) {
        int tmpLower = myLowerBounds[idx];
        if (tmpLower != Integer.MIN_VALUE) {
            return new BigDecimal(tmpLower);
        }
        return null;
    }

    double getMinimumDisplacement(final int idx, final double value) {

        double feasibleValue = this.feasible(idx, value, true);

        return Math.abs(feasibleValue - Math.rint(feasibleValue));
    }

    BigDecimal getUpperBound(final int idx) {
        int tmpUpper = myUpperBounds[idx];
        if (tmpUpper != Integer.MAX_VALUE) {
            return new BigDecimal(tmpUpper);
        }
        return null;
    }

    boolean isSignChanged() {
        return mySignChanged;
    }

    void setNodeState(final ExpressionsBasedModel model, final ModelStrategy strategy) {
        for (int i = 0; i < strategy.countIntegerVariables(); i++) {
            this.enforceBounds(model, i, strategy);
        }
    }

}
