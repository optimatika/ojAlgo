/*
 * Copyright 1997-2026 Optimatika
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
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Collectors;

import org.ojalgo.matrix.store.R064CSC;
import org.ojalgo.matrix.store.R064CSR;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.structure.Structure1D.IntIndex;

/**
 * Bound propagation: what a changed bound of one variable means for the others. It's part of branching, see
 * {@link NodeKey#createLowerBranch} and {@link NodeKey#createUpperBranch}.
 * <p>
 * Between which values a constraint can be, its activity, follows from the bounds of the variables in it. If
 * that's outside the limits of the constraint there is no solution. Otherwise the limits, and the activity of
 * the other variables, may imply a tighter bound for a variable – rounded, if it is integer. The constraints
 * that variable is in are then checked (again), and so on until nothing changes. The constraints are the
 * linear ones of the integer model and, once there is an incumbent, the objective function limit and the
 * reduced cost row of the node branched from (see
 * {@link #newReducedCostCutoff(IntToDoubleFunction, IntToDoubleFunction, double)}).
 * <p>
 * The bounds of the integer variables are kept in the {@link NodeKey}. Those of the continuous variables are
 * derived, and used, but then forgotten – except those found at the root, before the search starts (see
 * {@link #propagateRoot(int[], int[])}), which are kept here and used for the whole search.
 * <p>
 * Nothing feasible is cut off: the bounds are relaxed by what the rounding errors of the activities may be,
 * at most.
 * <p>
 * An instance can be used by one thread, see {@link #newInstance()}.
 */
final class BoundPropagator {

    /**
     * What's the same for all instances, the threads of a solve
     */
    private static final class Data {

        static Data of(final ExpressionsBasedModel model, final ModelStrategy strategy) {

            int nbVariables = model.countVariables();

            double[] lower = new double[nbVariables];
            double[] upper = new double[nbVariables];
            for (int j = 0; j < nbVariables; j++) {
                Variable variable = model.getVariable(j);
                lower[j] = variable.getLowerLimit(false, Double.NEGATIVE_INFINITY);
                upper[j] = variable.getUpperLimit(false, Double.POSITIVE_INFINITY);
            }

            int[] globalIndices = new int[strategy.countIntegerVariables()];
            int[] integerIndices = new int[nbVariables];
            Arrays.fill(integerIndices, -1);
            for (int i = 0; i < globalIndices.length; i++) {
                globalIndices[i] = strategy.getIndex(i);
                integerIndices[globalIndices[i]] = i;
            }

            List<Expression> constraints = model.constraints().filter(constraint -> !constraint.isAnyQuadraticFactorNonZero()).collect(Collectors.toList());

            R064CSR rows = BoundPropagator.toCSR(constraints, nbVariables);

            double[] rowLower = new double[constraints.size()];
            double[] rowUpper = new double[constraints.size()];
            for (int i = 0; i < rowLower.length; i++) {
                Expression constraint = constraints.get(i);
                rowLower[i] = constraint.getLowerLimit(false, Double.NEGATIVE_INFINITY);
                rowUpper[i] = constraint.getUpperLimit(false, Double.POSITIVE_INFINITY);
            }

            return new Data(lower, upper, globalIndices, integerIndices, rows, rowLower, rowUpper, rows.toCSC());
        }

        /**
         * The same as {@link #rows}, to find the constraints (rows) a variable (column) is in
         */
        final R064CSC columns;
        /**
         * Per integer variable, the global (model) index
         */
        final int[] globalIndices;
        /**
         * Per variable, the index among the integer variables, -1 for a continuous variable
         */
        final int[] integerIndices;
        final double[] lower;
        /**
         * The objective function limit, null as long as there is none
         */
        volatile Cutoff objective = null;
        final double[] rowLower;
        /**
         * The (linear) constraints, one per row
         */
        final R064CSR rows;
        final double[] rowUpper;
        final LongAdder statInfeasible = new LongAdder();
        final LongAdder statNanos = new LongAdder();
        final LongAdder statPropagations = new LongAdder();
        final LongAdder statTightened = new LongAdder();
        final double[] upper;

        Data(final double[] lower, final double[] upper, final int[] globalIndices, final int[] integerIndices, final R064CSR rows, final double[] rowLower,
                final double[] rowUpper, final R064CSC columns) {

            super();

            this.lower = lower;
            this.upper = upper;
            this.globalIndices = globalIndices;
            this.integerIndices = integerIndices;
            this.rows = rows;
            this.rowLower = rowLower;
            this.rowUpper = rowUpper;
            this.columns = columns;
        }

    }

    /**
     * One more constraint, not one of the model's: no better solution than the incumbent is cut off by it.
     * Either the objective function limit, for the whole search, or a node's reduced cost row, for the
     * subtree of that node (see
     * {@link BoundPropagator#newReducedCostCutoff(IntToDoubleFunction, IntToDoubleFunction, double)}).
     */
    static final class Cutoff {

        static Cutoff of(final Expression limited, final int nbVariables) {

            R064CSR row = BoundPropagator.toCSR(Collections.singletonList(limited), nbVariables);

            return Cutoff.of(row, limited.getLowerLimit(false, Double.NEGATIVE_INFINITY), limited.getUpperLimit(false, Double.POSITIVE_INFINITY));
        }

        static Cutoff of(final R064CSR row, final double lower, final double upper) {

            boolean[] members = new boolean[row.getColDim()];
            for (int j : row.indices) {
                members[j] = true;
            }

            return new Cutoff(row, lower, upper, members);
        }

        final double lower;
        /**
         * Per variable, is it part of the row?
         */
        final boolean[] members;
        /**
         * A single row
         */
        final R064CSR row;
        final double upper;

        Cutoff(final R064CSR row, final double lower, final double upper, final boolean[] members) {

            super();

            this.row = row;
            this.lower = lower;
            this.upper = upper;
            this.members = members;
        }

    }

    /**
     * A bound of a continuous variable is tightened only if it's by this much, relative to its range.
     * Otherwise it could go on forever.
     */
    private static final double CONTINUOUS_STEP = 1.0E-3;
    private static final double INTEGER_ROUNDING = 1.0E-6;
    private static final double RELATIVE_FEASIBILITY = 1.0E-6;
    /**
     * The rounding error of one double operation, relative to the result
     */
    private static final double ROUNDING_ERROR = 0x1.0p-52;

    /**
     * An upper bound on the rounding error of an activity: a sum of products, each with a coefficient that
     * was rounded too, compared to a (rounded) limit. The size of the result doesn't matter (there may be
     * cancellation), but the size of the terms does.
     *
     * @param nbTerms   The number of terms
     * @param magnitude The sum of the absolute values of the terms and the limit
     */
    private static double roundingError(final int nbTerms, final double magnitude) {
        return (nbTerms + 3) * ROUNDING_ERROR * magnitude;
    }

    /**
     * The linear parts of the expressions, without zeros, as the rows of a matrix – with the column indices
     * of each row in ascending order
     */
    private static R064CSR toCSR(final List<Expression> expressions, final int nbVariables) {

        int nbRows = expressions.size();

        int[] pointers = new int[nbRows + 1];
        for (int i = 0; i < nbRows; i++) {
            pointers[i + 1] = pointers[i];
            for (Entry<IntIndex, BigDecimal> entry : expressions.get(i).getLinearEntrySet()) {
                if (entry.getValue().doubleValue() != 0.0) {
                    pointers[i + 1]++;
                }
            }
        }

        int[] indices = new int[pointers[nbRows]];
        double[] values = new double[pointers[nbRows]];
        for (int i = 0, k = 0; i < nbRows; i++) {
            for (Entry<IntIndex, BigDecimal> entry : expressions.get(i).getLinearEntrySet()) {
                double value = entry.getValue().doubleValue();
                if (value != 0.0) {
                    indices[k] = entry.getKey().index;
                    values[k] = value;
                    k++;
                }
            }
        }

        // The entry sets are not sorted, the conversions sort
        return new R064CSR(nbRows, nbVariables, values, indices, pointers).toCSC().toCSR();
    }

    private static double tolerance(final double value) {
        return RELATIVE_FEASIBILITY * Math.max(1.0, Math.abs(value));
    }

    /**
     * @param integerModel Its linear constraints are what's propagated. Should not have an objective function
     *                     limit, see {@link #setObjectiveLimit(Expression)}.
     * @param strategy     Knows which the integer variables are
     */
    static BoundPropagator of(final ExpressionsBasedModel integerModel, final ModelStrategy strategy) {
        return new BoundPropagator(Data.of(integerModel, strategy));
    }

    private final Data myData;
    /**
     * The current bounds of all variables. Between calls the continuous variables have those of the model.
     */
    private final double[] myLower;
    private int myNbTightened = 0;
    /**
     * The objective function limit, during a call, if there is one. Queued as row number "nbRows".
     */
    private Cutoff myObjective = null;
    private final int[] myQueue;
    private final boolean[] myQueued;
    private int myQueueHead = 0;
    private int myQueueSize = 0;
    /**
     * The reduced cost row, during a call, if there is one. Queued as row number "nbRows + 1".
     */
    private Cutoff myReducedCost = null;
    private final boolean[] myTightened;
    /**
     * The variables with a bound that has been tightened
     */
    private final int[] myTightenedVariables;
    private final double[] myUpper;

    private BoundPropagator(final Data data) {

        super();

        myData = data;

        myLower = data.lower.clone();
        myUpper = data.upper.clone();

        int nbVariables = data.lower.length;
        myTightened = new boolean[nbVariables];
        myTightenedVariables = new int[nbVariables];

        // Two more, for the objective function limit and the reduced cost row
        int nbRows = data.rowLower.length;
        myQueue = new int[nbRows + 2];
        myQueued = new boolean[nbRows + 2];
    }

    @Override
    public String toString() {
        return "propagations=" + myData.statPropagations.sum() + " (" + myData.statNanos.sum() / 1_000_000L + " ms), infeasible=" + myData.statInfeasible.sum()
                + ", tightened=" + myData.statTightened.sum();
    }

    private void enqueue(final int row) {
        if (!myQueued[row]) {
            myQueued[row] = true;
            myQueue[(myQueueHead + myQueueSize++) % myQueue.length] = row;
        }
    }

    private void enqueueAll(final int variable) {
        R064CSC columns = myData.columns;
        for (int k = columns.pointers[variable], limit = columns.pointers[variable + 1]; k < limit; k++) {
            this.enqueue(columns.indices[k]);
        }
        if (myObjective != null && myObjective.members[variable]) {
            this.enqueue(myData.rowLower.length);
        }
        if (myReducedCost != null && myReducedCost.members[variable]) {
            this.enqueue(myData.rowLower.length + 1);
        }
    }

    /**
     * @param matrix The constraint is one of its rows
     * @param row    Which row
     * @param lower  The lower limit of the constraint
     * @param upper  The upper limit of the constraint
     * @return false if there is no solution
     */
    private boolean propagate(final R064CSR matrix, final int row, final double lower, final double upper) {

        int[] indices = matrix.indices;
        double[] values = matrix.values;
        int first = matrix.pointers[row];
        int limit = matrix.pointers[row + 1];
        int nbTerms = limit - first;

        // Between these values, and the sums of the absolute values (to bound the rounding errors)
        double minActivity = 0.0, maxActivity = 0.0;
        double minMagnitude = 0.0, maxMagnitude = 0.0;
        int minInfinite = 0, maxInfinite = 0;

        for (int k = first; k < limit; k++) {
            double value = values[k];
            int j = indices[k];
            double min = value > 0.0 ? value * myLower[j] : value * myUpper[j];
            double max = value > 0.0 ? value * myUpper[j] : value * myLower[j];
            if (Double.isFinite(min)) {
                minActivity += min;
                minMagnitude += Math.abs(min);
            } else {
                minInfinite++;
            }
            if (Double.isFinite(max)) {
                maxActivity += max;
                maxMagnitude += Math.abs(max);
            } else {
                maxInfinite++;
            }
        }

        double upperError = BoundPropagator.roundingError(nbTerms, minMagnitude + Math.abs(upper));
        double lowerError = BoundPropagator.roundingError(nbTerms, maxMagnitude + Math.abs(lower));

        if (minInfinite == 0 && minActivity > upper + BoundPropagator.tolerance(upper) + upperError
                || maxInfinite == 0 && maxActivity < lower - BoundPropagator.tolerance(lower) - lowerError) {
            return false;
        }

        boolean upperUseful = Double.isFinite(upper) && minInfinite <= 1;
        boolean lowerUseful = Double.isFinite(lower) && maxInfinite <= 1;

        if (!upperUseful && !lowerUseful) {
            return true;
        }

        for (int k = first; k < limit; k++) {

            double value = values[k];
            int j = indices[k];
            double min = value > 0.0 ? value * myLower[j] : value * myUpper[j];
            double max = value > 0.0 ? value * myUpper[j] : value * myLower[j];

            double newLower = Double.NEGATIVE_INFINITY;
            double newUpper = Double.POSITIVE_INFINITY;

            if (upperUseful && (minInfinite == 0 || !Double.isFinite(min))) {
                // The rest of the row is at least this
                double rest = minInfinite == 0 ? minActivity - min : minActivity;
                double bound = (upper - rest) / value;
                double error = upperError / Math.abs(value);
                if (value > 0.0) {
                    newUpper = bound + error;
                } else {
                    newLower = bound - error;
                }
            }

            if (lowerUseful && (maxInfinite == 0 || !Double.isFinite(max))) {
                // The rest of the row is at most this
                double rest = maxInfinite == 0 ? maxActivity - max : maxActivity;
                double bound = (lower - rest) / value;
                double error = lowerError / Math.abs(value);
                if (value > 0.0) {
                    newLower = Math.max(newLower, bound - error);
                } else {
                    newUpper = Math.min(newUpper, bound + error);
                }
            }

            if (!this.tighten(j, newLower, newUpper)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Propagates the queued constraints, and the cutoffs (objective function limit and reduced cost row) if
     * there are any. Those are set for the call, and cleared here.
     */
    private boolean propagateQueued(final int[] lowerBounds, final int[] upperBounds, final boolean root) {

        long started = System.nanoTime();

        int[] globalIndices = myData.globalIndices;
        R064CSR rows = myData.rows;
        double[] rowLower = myData.rowLower;
        double[] rowUpper = myData.rowUpper;
        int nbRows = rowLower.length;

        for (int i = 0; i < globalIndices.length; i++) {
            int j = globalIndices[i];
            myLower[j] = lowerBounds[i] != Integer.MIN_VALUE ? Math.max(myData.lower[j], lowerBounds[i]) : myData.lower[j];
            myUpper[j] = upperBounds[i] != Integer.MAX_VALUE ? Math.min(myData.upper[j], upperBounds[i]) : myData.upper[j];
        }

        if (myObjective != null) {
            this.enqueue(nbRows);
        }
        if (myReducedCost != null) {
            this.enqueue(nbRows + 1);
        }

        boolean feasible = true;

        for (int visits = 20 * (nbRows + 1) + 100; feasible && visits > 0 && myQueueSize > 0; visits--) {

            int index = myQueue[myQueueHead];
            myQueueHead = (myQueueHead + 1) % myQueue.length;
            myQueueSize--;
            myQueued[index] = false;

            if (index < nbRows) {
                feasible = this.propagate(rows, index, rowLower[index], rowUpper[index]);
            } else {
                Cutoff cutoff = index == nbRows ? myObjective : myReducedCost;
                feasible = this.propagate(cutoff.row, 0, cutoff.lower, cutoff.upper);
            }
        }

        myObjective = null;
        myReducedCost = null;

        // If it ended before the queue was empty
        for (; myQueueSize > 0; myQueueSize--) {
            myQueued[myQueue[myQueueHead]] = false;
            myQueueHead = (myQueueHead + 1) % myQueue.length;
        }

        for (int t = 0; t < myNbTightened; t++) {

            int j = myTightenedVariables[t];
            myTightened[j] = false;

            int i = myData.integerIndices[j];
            if (i < 0) {
                if (root && feasible) {
                    myData.lower[j] = myLower[j];
                    myData.upper[j] = myUpper[j];
                } else {
                    myLower[j] = myData.lower[j];
                    myUpper[j] = myData.upper[j];
                }
            } else if (feasible && (!root || myLower[j] >= myUpper[j])) {
                double lower = myLower[j];
                double upper = myUpper[j];
                if (lower > lowerBounds[i] && lower < Integer.MAX_VALUE) {
                    lowerBounds[i] = (int) lower;
                    myData.statTightened.increment();
                }
                if (upper < upperBounds[i] && upper > Integer.MIN_VALUE) {
                    upperBounds[i] = (int) upper;
                    myData.statTightened.increment();
                }
            }
        }
        myNbTightened = 0;

        myData.statPropagations.increment();
        myData.statNanos.add(System.nanoTime() - started);
        if (!feasible) {
            myData.statInfeasible.increment();
        }

        return feasible;
    }

    /**
     * @return false if the bounds cross
     */
    private boolean tighten(final int variable, final double lower, final double upper) {

        boolean integer = myData.integerIndices[variable] >= 0;

        double newLower = integer ? Math.ceil(lower - INTEGER_ROUNDING) : lower;
        double newUpper = integer ? Math.floor(upper + INTEGER_ROUNDING) : upper;

        double oldLower = myLower[variable];
        double oldUpper = myUpper[variable];

        double step = integer ? 0.5 : CONTINUOUS_STEP * Math.max(1.0, Math.abs(oldUpper - oldLower));

        boolean changed = false;

        if (newLower > oldLower + step || !Double.isFinite(oldLower) && Double.isFinite(newLower)) {
            myLower[variable] = newLower;
            changed = true;
        }
        if (newUpper < oldUpper - step || !Double.isFinite(oldUpper) && Double.isFinite(newUpper)) {
            myUpper[variable] = newUpper;
            changed = true;
        }

        if (changed) {
            if (!myTightened[variable]) {
                myTightened[variable] = true;
                myTightenedVariables[myNbTightened++] = variable;
            }
            if (myLower[variable] > myUpper[variable] + BoundPropagator.tolerance(myUpper[variable])) {
                return false;
            }
            this.enqueueAll(variable);
        }

        return true;
    }

    /**
     * For another thread
     */
    BoundPropagator newInstance() {
        return new BoundPropagator(myData);
    }

    /**
     * A node's reduced cost row. With the LP solution x̄ of the node, its reduced costs g (in the
     * minimisation the solver does) and LP duality, any solution x within the node's (LP) region has an
     * objective function value no better than that of the node plus the sum of g<sub>j</sub>(x<sub>j</sub> -
     * x̄<sub>j</sub>). So a solution better than the incumbent has that sum at most the gap. Every term of it
     * is non-negative (dual feasibility), so this is what fixing by reduced cost does for one variable at a
     * time. Propagated together with the other constraints, it's stronger: whatever moves one variable away
     * from x̄ leaves less of the gap for the others.
     * <p>
     * The terms of the slack variables are non-negative too, and are left out. Valid for the subtree of the
     * node, to be used when deriving its branches, see
     * {@link #propagate(int[], int[], int, BoundPropagator.Cutoff)}.
     *
     * @param gradient Per (model) variable, its reduced cost
     * @param solution Per (model) variable, its value in the node's LP solution
     * @param gap      How much better the node's LP value is than the incumbent's value
     * @return null if there is no such row (no non-zero reduced cost)
     */
    Cutoff newReducedCostCutoff(final IntToDoubleFunction gradient, final IntToDoubleFunction solution, final double gap) {

        int nbVariables = myData.lower.length;

        int[] indices = new int[nbVariables];
        double[] values = new double[nbVariables];
        int count = 0;

        double limit = gap;
        double magnitude = Math.abs(gap);

        for (int j = 0; j < nbVariables; j++) {
            double value = gradient.applyAsDouble(j);
            if (value != 0.0 && Double.isFinite(value)) {
                double term = value * solution.applyAsDouble(j);
                if (Double.isFinite(term)) {
                    indices[count] = j;
                    values[count] = value;
                    count++;
                    limit += term;
                    magnitude += Math.abs(term);
                }
            }
        }

        if (count == 0) {
            return null;
        }

        // The rounding error of the limit's own sum
        limit += BoundPropagator.roundingError(count, magnitude);

        R064CSR row = new R064CSR(1, nbVariables, Arrays.copyOf(values, count), Arrays.copyOf(indices, count), new int[] { 0, count });

        return Cutoff.of(row, Double.NEGATIVE_INFINITY, limit);
    }

    /**
     * Propagates the changed bound(s) of an integer variable, and the objective function limit if there is
     * one.
     *
     * @param lowerBounds Of the integer variables, will be updated
     * @param upperBounds Of the integer variables, will be updated
     * @param changed     The index, among the integer variables, of the one with changed bounds
     * @param reducedCost The reduced cost row of the node branched from, or null, see
     *                    {@link #newReducedCostCutoff(IntToDoubleFunction, IntToDoubleFunction, double)}
     * @return false if there is no solution – then the bounds may have been partly updated
     */
    boolean propagate(final int[] lowerBounds, final int[] upperBounds, final int changed, final Cutoff reducedCost) {

        myObjective = myData.objective;
        myReducedCost = reducedCost;

        this.enqueueAll(myData.globalIndices[changed]);

        return this.propagateQueued(lowerBounds, upperBounds, false);
    }

    /**
     * Propagates all bounds that differ from the old ones, and the objective function limit if there is one.
     *
     * @param lowerBounds    Of the integer variables, will be updated
     * @param upperBounds    Of the integer variables, will be updated
     * @param oldLowerBounds Of the integer variables
     * @param oldUpperBounds Of the integer variables
     * @return false if there is no solution – then the bounds may have been partly updated
     */
    boolean propagate(final int[] lowerBounds, final int[] upperBounds, final int[] oldLowerBounds, final int[] oldUpperBounds) {

        myObjective = myData.objective;

        for (int i = 0; i < lowerBounds.length; i++) {
            if (lowerBounds[i] != oldLowerBounds[i] || upperBounds[i] != oldUpperBounds[i]) {
                this.enqueueAll(myData.globalIndices[i]);
            }
        }

        return this.propagateQueued(lowerBounds, upperBounds, false);
    }

    /**
     * Propagates all constraints, and the objective function limit if there is one. To be called once, before
     * the search starts and before there are other instances ({@link #newInstance()}). The bounds of the
     * continuous variables are kept, in this instance and in the others, to be used by all propagations from
     * then on. Of the integer variables only those that turn out to be fixed are updated: the constraints
     * already imply the rest, and the LP solver is often better off without more bounds.
     *
     * @param lowerBounds Of the integer variables, will be updated
     * @param upperBounds Of the integer variables, will be updated
     * @return false if there is no solution – then the bounds may have been partly updated
     */
    boolean propagateRoot(final int[] lowerBounds, final int[] upperBounds) {

        myObjective = myData.objective;

        for (int row = 0; row < myData.rowLower.length; row++) {
            this.enqueue(row);
        }

        return this.propagateQueued(lowerBounds, upperBounds, true);
    }

    /**
     * With a new incumbent there's a new limit. Applies to all instances.
     *
     * @param limited The objective function as a constraint, see
     *                {@link ExpressionsBasedModel#limitObjective(BigDecimal, BigDecimal)}
     */
    void setObjectiveLimit(final Expression limited) {
        if (limited.isConstraint() && !limited.isAnyQuadraticFactorNonZero()) {
            myData.objective = Cutoff.of(limited, myData.lower.length);
        }
    }

}
