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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.multiary.MultiaryFunction;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.Sense;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.context.NumberContext;

/**
 * This base class contains some model/problem specific data required by the {@link IntegerSolver}. The
 * "strategies" are implemented in subclasses. If you plan to implement a custom strategy it may be helpful to
 * extend {@link ModelStrategy.AbstractStrategy} instead.
 *
 * @author apete
 */
public abstract class ModelStrategy implements IntegerStrategy {

    /**
     * When implementing your own {@link ModelStrategy} extending this abstract class is a good starting
     * point. It gives you access to the default implementation as a delegate.
     *
     * @author apete
     */
    public static abstract class AbstractStrategy extends ModelStrategy {

        protected final ModelStrategy delegate;

        protected AbstractStrategy(final ExpressionsBasedModel model, final IntegerStrategy strategy) {

            super(model, strategy);

            delegate = new DefaultStrategy(model, strategy);
        }

    }

    static final class DefaultStrategy extends ModelStrategy {

        private static final NumberContext ROUGHLY = NumberContext.of(2);

        /**
         * The latest found integer solution.
         */
        private Access1D<?> myIterationPoint = null;
        /**
         * Try to keep track of how different values of integer variables relates to the objective function
         * value.
         */
        private final double[] mySignificances;

        DefaultStrategy(final ExpressionsBasedModel model, final IntegerStrategy strategy) {

            super(model, strategy);

            List<Variable> integerVariables = model.getIntegerVariables();
            int nbIntegers = integerVariables.size();

            mySignificances = new double[nbIntegers];
        }

        private void addSignificance(final int idx, final double significance) {
            mySignificances[idx] = MissingMath.hypot(mySignificances[idx], significance);
        }

        /**
         * Initialise the integer significances, based on the objective function gradient.
         */
        @Override
        protected ModelStrategy initialise(final MultiaryFunction.TwiceDifferentiable<Double> function, final Access1D<?> point) {

            Arrays.fill(mySignificances, ONE);

            int nbIntegers = this.countIntegerVariables();

            Access1D<Double> iterationPoint = Access1D.asPrimitive1D(point);
            MatrixStore<Double> gradient = function.getGradient(iterationPoint);
            double largest = gradient.aggregateAll(Aggregator.LARGEST).doubleValue();

            if (!ROUGHLY.isZero(largest)) {
                for (int i = 0; i < nbIntegers; i++) {
                    int globalIndex = this.getIndex(i);
                    double partial = gradient.doubleValue(globalIndex);
                    if (!ROUGHLY.isZero(partial)) {
                        this.addSignificance(i, partial / largest);
                    }
                }
            }

            myIterationPoint = iterationPoint;

            return this;
        }

        @Override
        protected boolean isCutRatherThanBranch(final double displacement, final boolean found) {
            if (cutting && found) {
                return displacement > 0.49;
            }
            return false;
        }

        @Override
        protected boolean isDirect(final NodeKey node, final boolean found) {
            return found ? node.displacement < THIRD : node.displacement < HALF;
        }

        @Override
        protected void markInfeasible(final NodeKey key, final boolean found) {
            int index = key.index;
            if (index >= 0) {
                this.addSignificance(index, found ? 0.2 : 0.1);
            }
        }

        /**
         * Update the integer significances, based on new integer solution found.
         */
        @Override
        protected void markInteger(final NodeKey key, final Optimisation.Result result) {

            if (myIterationPoint != null) {

                for (int i = 0, limit = this.countIntegerVariables(); i < limit; i++) {
                    int globalIndex = this.getIndex(i);
                    double diff = result.doubleValue(globalIndex) - myIterationPoint.doubleValue(globalIndex);
                    if (!ROUGHLY.isZero(diff)) {
                        this.addSignificance(i, ONE / diff);
                    }
                }
            }

            myIterationPoint = result;
        }

        /**
         * If not yet found integer solution then compare the remaining/reversed (larger) fraction, otherwise
         * the fraction scaled by the significance.
         *
         * @see org.ojalgo.optimisation.integer.ModelStrategy#toComparable(int, double, boolean)
         */
        @Override
        protected double toComparable(final int idx, final double displacement, final boolean found) {
            return found ? displacement * mySignificances[idx] : ONE - displacement;
        }

    }

    /**
     * One entry per integer variable, the entry is the global index of that integer variable
     */
    private final int[] myIndices;
    private final Optimisation.Sense myOptimisationSense;
    private final IntegerStrategy myStrategy;
    private final List<Comparator<NodeKey>> myWorkerPriorities;

    /**
     * Indicates if cut generation is turned on, or not. On by default. Algorithms can turn off when/if no
     * longer useful.
     */
    protected boolean cutting = true;

    protected ModelStrategy(final ExpressionsBasedModel model, final IntegerStrategy strategy) {

        myOptimisationSense = (model.getOptimisationSense() != Optimisation.Sense.MAX) ? Sense.MIN : Sense.MAX;

        myStrategy = strategy;

        List<Variable> integerVariables = model.getIntegerVariables();

        int nbIntegers = integerVariables.size();

        myIndices = new int[nbIntegers];
        for (int i = 0; i < nbIntegers; i++) {
            int globalIndex = model.indexOf(integerVariables.get(i));
            myIndices[i] = globalIndex;
        }

        myWorkerPriorities = strategy.getWorkerPriorities();
    }

    public NumberContext getGapTolerance() {
        return myStrategy.getGapTolerance();
    }

    public GMICutConfiguration getGMICutConfiguration() {
        return myStrategy.getGMICutConfiguration();
    }

    public NumberContext getIntegralityTolerance() {
        return myStrategy.getIntegralityTolerance();
    }

    public List<Comparator<NodeKey>> getWorkerPriorities() {
        return myWorkerPriorities;
    }

    public ModelStrategy newModelStrategy(final ExpressionsBasedModel model) {
        return myStrategy.newModelStrategy(model);
    }

    @Override
    public final String toString() {
        return Arrays.toString(myIndices);
    }

    protected int countIntegerVariables() {
        return myIndices.length;
    }

    /**
     * The variable's global index
     *
     * @param idx Index among the integer variables
     */
    protected int getIndex(final int idx) {
        return myIndices[idx];
    }

    /**
     * Called, once, at the very beginning of the solve process.
     */
    protected abstract ModelStrategy initialise(final MultiaryFunction.TwiceDifferentiable<Double> function, final Access1D<?> point);

    protected abstract boolean isCutRatherThanBranch(double displacement, boolean found);

    /**
     * This method will be called twice when branching – once for each of the new nodes created by branching.
     * In most cases you only want to return true for (at most) one of those new branches. Always returning
     * true for both the new nodes will cause excessive memory consumption.
     *
     * @param node The node to check
     * @param found Is an integer solution already found?
     * @return true if this node should be evaluated directly (not deferred)
     */
    protected abstract boolean isDirect(NodeKey node, boolean found);

    /**
     * Is the node's result good enough to continue branching? (Compare the node's objective function value
     * with the that of the best integer solution found so far.)
     */
    protected boolean isGoodEnough(final Result bestResultSoFar, final double relaxedNodeValue) {

        if (bestResultSoFar == null) {
            return true;
        }

        if (!Double.isFinite(relaxedNodeValue)) {
            return false;
        }

        double bestIntegerValue = bestResultSoFar.getValue();

        if (!myStrategy.getGapTolerance().isDifferent(bestIntegerValue, relaxedNodeValue)) {
            return false;
        }

        if (myOptimisationSense == Sense.MIN && relaxedNodeValue < bestIntegerValue
                || myOptimisationSense == Sense.MAX && relaxedNodeValue > bestIntegerValue) {
            return true;
        }

        return false;
    }

    /**
     * Called everytime a node/subproblem is found to be infeasible
     */
    protected abstract void markInfeasible(NodeKey key, boolean found);

    /**
     * Called everytime a new integer solution is found
     */
    protected abstract void markInteger(NodeKey key, Optimisation.Result result);

    /**
     * Convert the fraction to something "comparable" used to determine which variable to branch on. If a
     * variable is at an integer value or not is determined by the {@link Optimisation.Options#feasibility}
     * property. If an integer variable is not at an integer value, then this method is invoked to obtain a
     * value that is then used to copare with that of other integer variables with fractional values. The
     * variable with the max "comparable" is picked for branching.
     *
     * @param idx Integer variable index
     * @param displacement variable's fractional value
     * @param found Is an integer solution already found?
     * @return Value used to compare variables when determining which to branch on. Larger value means more
     *         likelyn to branch on this.
     */
    protected abstract double toComparable(int idx, double displacement, boolean found);

}
