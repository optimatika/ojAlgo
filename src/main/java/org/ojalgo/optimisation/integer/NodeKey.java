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

    public static final Comparator<NodeKey> BREADTH_FIRST_SEARCH = Comparator.comparingInt((final NodeKey nk) -> nk.depth).thenComparingLong(nk -> nk.sequence);

    public static final Comparator<NodeKey> DEPTH_FIRST_SEARCH = Comparator.comparingInt((final NodeKey nk) -> -nk.depth).thenComparingLong(nk -> -nk.sequence);

    public static final Comparator<NodeKey> FIFO_SEQUENCE = Comparator.comparingLong((final NodeKey nk) -> nk.sequence);

    public static final Comparator<NodeKey> LARGE_DISPLACEMENT = Comparator.comparingDouble((final NodeKey nk) -> -nk.displacement);

    public static final Comparator<NodeKey> LIFO_SEQUENCE = Comparator.comparingLong((final NodeKey nk) -> -nk.sequence);

    public static final Comparator<NodeKey> MAX_OBJECTIVE = Comparator.comparingDouble((final NodeKey nk) -> -nk.objective)
            .thenComparingDouble(nk -> -nk.displacement);

    public static final Comparator<NodeKey> MIN_OBJECTIVE = Comparator.comparingDouble((final NodeKey nk) -> nk.objective)
            .thenComparingDouble(nk -> -nk.displacement);

    public static final Comparator<NodeKey> SMALL_DISPLACEMENT = Comparator.comparingDouble((final NodeKey nk) -> nk.displacement);

    /**
     * Used for one thing only - to validate (log problems with) node solver results. Does not effect the
     * algorithm.
     */
    private static final NumberContext FEASIBILITY = NumberContext.of(8, 6);

    private static final AtomicLong SEQUENCE_GENERATOR = new AtomicLong();

    static final double MINIMUM_DISPLACEMENT = 1E-9;

    /**
     * How far have we branched from the root
     */
    public final int depth;
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
     * Node sequence number to keep track of in which order the nodes were created.
     */
    public final long sequence;

    private final IntArrayPool myIntArrayPool;
    private final int[] myLowerBounds;
    private final boolean mySignChanged;
    private final int[] myUpperBounds;
    // Indicates whether this node was created by branching UP (true) or DOWN (false)
    private final boolean myUpperBranch;

    private NodeKey(final int[] lowerBounds, final int[] upperBounds, final long parentSequenceNumber, final int parentDepth, final int integerIndexBranchedOn,
            final double branchVariableDisplacement, final double parentObjectiveFunctionValue, final boolean signChanged, final boolean upperBranch,
            final IntArrayPool pool) {

        super();

        sequence = SEQUENCE_GENERATOR.incrementAndGet();

        myLowerBounds = lowerBounds;
        myUpperBounds = upperBounds;

        parent = parentSequenceNumber;
        depth = parentDepth + 1;
        index = integerIndexBranchedOn;
        displacement = Math.max(branchVariableDisplacement, MINIMUM_DISPLACEMENT);
        objective = parentObjectiveFunctionValue;

        mySignChanged = signChanged;
        myUpperBranch = upperBranch;

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
        depth = 0;
        index = -1;
        displacement = NaN;
        objective = NaN;

        mySignChanged = false;
        myUpperBranch = false; // Root node: no direction
    }

    @Override
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
        return prime * result + (int) (sequence ^ sequence >>> 32);
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

        if (myLowerBounds.length <= 100) {

            if (myLowerBounds.length > 0) {
                this.append(retVal, 0);
            }

            for (int i = 1; i < myLowerBounds.length; i++) {
                retVal.append(',');
                retVal.append(' ');
                this.append(retVal, i);
            }

        } else {

            retVal.append('.');
            retVal.append('.');
            retVal.append('.');
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

        // A down-branch tightens only the upper bound. The solver treats a column as "negated" iff
        // (ub <= 0 && lb < 0) — see SimplexStore.isNegated. With the lower bound unchanged here, that
        // classification can flip only if the lower bound is itself negative; otherwise the upper bound
        // crossing zero (e.g. the ubiquitous binary [0,1] -> [0,0] down-branch) is just a normal
        // fix-to-zero the solver absorbs in place. Requiring lb < 0 avoids flagging a sign change — and
        // thus forcing a full solver rebuild — on essentially every 0/1 down-branch.
        boolean changed = oldVal > 0 && newVal <= 0 && tmpLBs[branchIntegerIndex] < 0;

        return new NodeKey(tmpLBs, tmpUBs, sequence, depth, branchIntegerIndex, value - floorValue, objVal, changed, false, myIntArrayPool);
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

        // Mirror of createLowerBranch: an up-branch tightens only the lower bound, so the negated-class
        // (ub <= 0 && lb < 0) can flip only if the upper bound is itself <= 0. Requiring ub <= 0 avoids
        // forcing a rebuild on the common up-branch of a non-negative variable (e.g. [0,1] -> [1,1]).
        boolean changed = oldVal < 0 && newVal >= 0 && tmpUBs[branchIntegerIndex] <= 0;

        return new NodeKey(tmpLBs, tmpUBs, sequence, depth, branchIntegerIndex, ceilValue - value, objVal, changed, true, myIntArrayPool);
    }

    void dispose() {
        myIntArrayPool.giveBack(myLowerBounds);
        myIntArrayPool.giveBack(myUpperBounds);
    }

    void enforceBounds(final ExpressionsBasedModel model, final int idx, final ModelStrategy strategy) {

        BigDecimal lowerBound = this.getLower(idx);
        BigDecimal upperBound = this.getUpper(idx);

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

        BigDecimal lowerBound = this.getLower(index);
        BigDecimal upperBound = this.getUpper(index);

        Variable variable = nodeSolver.getVariable(strategy.getIndex(index));
        variable.lower(lowerBound);
        variable.upper(upperBound);

        BigDecimal value = variable.getValue();
        if (value != null) {
            // Re-setting will ensure the new bounds are not violated
            variable.setValue(value);
        }

        // A genuine negated-class flip needs a full rebuild. So does every branch of a relaxation that
        // can't absorb a bound change in place (quadratic / convex): those must rebuild fresh per node —
        // the historical, numerically-stable behaviour the old over-broad sign-change condition gave
        // them for free. Only the linear/simplex relaxation takes the in-place update path.
        if (this.isSignChanged() || !nodeSolver.isInPlaceBoundUpdateSafe()) {
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

    BigDecimal getLower(final int idx) {

        int bound = myLowerBounds[idx];

        if (bound != Integer.MIN_VALUE) {
            return BigDecimal.valueOf(bound);
        } else {
            return null;
        }
    }

    /**
     * Lower bound on the integer variable at {@code idx} (integer-local index), as a double. The
     * {@link Integer#MIN_VALUE} sentinel for "unbounded" is mapped to {@link Double#NEGATIVE_INFINITY}.
     */
    int getLowerBound(final int idx) {
        return myLowerBounds[idx];
    }

    double getMinimumDisplacement(final int idx, final double value) {

        double feasibleValue = this.feasible(idx, value, true);

        return Math.abs(feasibleValue - Math.rint(feasibleValue));
    }

    BigDecimal getUpper(final int idx) {

        int bound = myUpperBounds[idx];

        if (bound != Integer.MAX_VALUE) {
            return BigDecimal.valueOf(bound);
        } else {
            return null;
        }
    }

    /**
     * Upper bound on the integer variable at {@code idx} (integer-local index), as a double. The
     * {@link Integer#MAX_VALUE} sentinel for "unbounded" is mapped to {@link Double#POSITIVE_INFINITY}.
     */
    int getUpperBound(final int idx) {
        return myUpperBounds[idx];
    }

    boolean isLowerBranch() {
        return !myUpperBranch;
    }

    boolean isSignChanged() {
        // return mySignChanged;
        return true;
    }

    boolean isUpperBranch() {
        return myUpperBranch;
    }

    void setNodeState(final ExpressionsBasedModel model, final ModelStrategy strategy) {
        for (int i = 0; i < strategy.countIntegerVariables(); i++) {
            this.enforceBounds(model, i, strategy);
        }
    }

    /**
     * Raise the lower bound on integer variable {@code idx} to {@code newLower} if that is strictly tighter;
     * never loosens. NodeKeys are otherwise treated as immutable (children get new instances) - this mutator
     * is intended for the root-phase probing pass before any worker can see the NodeKey.
     */
    void tightenLower(final int idx, final int newLower) {
        if (newLower > myLowerBounds[idx]) {
            myLowerBounds[idx] = newLower;
        }
    }

    /**
     * Lower the upper bound on integer variable {@code idx} to {@code newUpper} if that is strictly tighter;
     * never loosens. See {@link #tightenLower}.
     */
    void tightenUpper(final int idx, final int newUpper) {
        if (newUpper < myUpperBounds[idx]) {
            myUpperBounds[idx] = newUpper;
        }
    }

}
