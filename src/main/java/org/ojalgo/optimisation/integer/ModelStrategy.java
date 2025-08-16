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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.multiary.MultiaryFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.Sense;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Primitive1D;
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

        private static final double DEFAULT_INFEASIBLE_PENALTY = TEN;
        private static final int RELIABILITY_MIN = 2;
        private static final NumberContext ROUGHLY = NumberContext.of(2);

        private static double productScore(final double wDown, final double wUp, final double distDn, final double distUp) {
            // Clamp pseudo-costs to avoid zero scores
            double u = wUp > NodeKey.MINIMUM_DISPLACEMENT ? wUp : NodeKey.MINIMUM_DISPLACEMENT;
            double d = wDown > NodeKey.MINIMUM_DISPLACEMENT ? wDown : NodeKey.MINIMUM_DISPLACEMENT;
            return (u * distUp) * (d * distDn);
        }

        private final int[] myLowerCount;
        /**
         * Pseudo-costs when down
         */
        private final double[] myLowerPseudoWeight;
        private final int[] myUpperCount;
        /**
         * Pseudo-costs when up
         */
        private final double[] myUpperPseudoWeight;

        DefaultStrategy(final ExpressionsBasedModel model, final IntegerStrategy strategy) {

            super(model, strategy);

            List<Variable> integerVariables = model.getIntegerVariables();
            int nbIntegers = integerVariables.size();

            myUpperPseudoWeight = new double[nbIntegers];
            myLowerPseudoWeight = new double[nbIntegers];
            myUpperCount = new int[nbIntegers];
            myLowerCount = new int[nbIntegers];
        }

        private void updatePseudo(final int idx, final boolean upper, final double observation) {

            if (upper) {

                synchronized (myUpperPseudoWeight) {

                    int n = myUpperCount[idx];
                    double mean = myUpperPseudoWeight[idx];
                    myUpperPseudoWeight[idx] = (mean * n + observation) / (n + 1);
                    myUpperCount[idx] = n + 1;
                }

            } else {

                synchronized (myLowerPseudoWeight) {

                    int n = myLowerCount[idx];
                    double mean = myLowerPseudoWeight[idx];
                    myLowerPseudoWeight[idx] = (mean * n + observation) / (n + 1);
                    myLowerCount[idx] = n + 1;
                }
            }
        }

        /**
         * Initialise the integer significances, based on the objective function gradient.
         */
        @Override
        protected ModelStrategy initialise(final MultiaryFunction.TwiceDifferentiable<Double> function, final Access1D<?> point) {

            Arrays.fill(myUpperPseudoWeight, ZERO);
            Arrays.fill(myLowerPseudoWeight, ZERO);
            Arrays.fill(myUpperCount, 0);
            Arrays.fill(myLowerCount, 0);

            int nbIntegers = this.countIntegerVariables();

            Access1D<Double> iterationPoint = Primitive1D.wrap(point);
            MatrixStore<Double> gradient = function.getGradient(iterationPoint);
            double largest = gradient.aggregateAll(Aggregator.LARGEST).doubleValue();

            if (!ROUGHLY.isZero(largest)) {
                for (int i = 0; i < nbIntegers; i++) {
                    int globalIndex = this.getIndex(i);
                    double partial = Math.abs(gradient.doubleValue(globalIndex));
                    double seed = 0.0;
                    if (!ROUGHLY.isZero(partial)) {
                        seed = partial / largest;
                    }
                    // Ensure strictly positive seeds to avoid zero product
                    myUpperPseudoWeight[i] = Math.max(seed, NodeKey.MINIMUM_DISPLACEMENT);
                    myLowerPseudoWeight[i] = Math.max(seed, NodeKey.MINIMUM_DISPLACEMENT);
                }
            } else {
                // No gradient available; seed with small positive values
                for (int i = 0; i < nbIntegers; i++) {
                    myUpperPseudoWeight[i] = NodeKey.MINIMUM_DISPLACEMENT;
                    myLowerPseudoWeight[i] = NodeKey.MINIMUM_DISPLACEMENT;
                }
            }

            return this;
        }

        @Override
        protected boolean isCutRatherThanBranch(final double displacement, final boolean found) {
            if (cutting && found) {
                return displacement > 0.49;
            }
            return false;
        }

        /**
         * scale-aware infeasible update using any available cutoff (incumbent or best bound)
         */
        @Override
        protected void markInfeasible(final NodeKey key, final boolean found, final double incumbentValue) {

            int index = key.index;
            if (index >= 0) {
                double dist = key.displacement;
                double obs;
                if (Double.isFinite(incumbentValue) && Double.isFinite(key.objective)) {
                    double gap = this.isMinimisation() ? (incumbentValue - key.objective) : (key.objective - incumbentValue);
                    if (gap < ZERO) {
                        gap = ZERO;
                    }
                    obs = (gap <= NodeKey.MINIMUM_DISPLACEMENT ? DEFAULT_INFEASIBLE_PENALTY : gap) / dist;
                } else {
                    obs = DEFAULT_INFEASIBLE_PENALTY / dist;
                }
                this.updatePseudo(index, key.isUpperBranch(), obs);
            }
        }

        @Override
        protected void markInteger(final NodeKey key, final Optimisation.Result result) {
            // No-op for pseudo-costs. Updates happen when child nodes are solved or infeasible.
        }

        @Override
        protected void onNodeSolved(final NodeKey key, final Optimisation.Result result, final double objective, final boolean minimisation) {

            int index = key.index;

            if (index >= 0) {

                // only count degradation, not improvements
                double deg = Math.max(ZERO, minimisation ? (objective - key.objective) : (key.objective - objective));

                double dist = key.displacement;

                double obs = deg / dist;

                this.updatePseudo(index, key.isUpperBranch(), obs);
            }
        }

        @Override
        protected double scoreBranch(final int idx, final double displaceDown, final double displaceUp, final boolean found) {

            int nbDn = myLowerCount[idx];
            int nbUp = myUpperCount[idx];

            boolean reliable = Math.min(nbUp, nbDn) >= RELIABILITY_MIN;
            if (!reliable && !found) {
                // Original behaviour before any incumbent: closest-to-integer first
                return Math.max(displaceDown, displaceUp);
            }
            double base = displaceDown * displaceUp;
            double prod = DefaultStrategy.productScore(myLowerPseudoWeight[idx], myUpperPseudoWeight[idx], displaceDown, displaceUp);

            double alpha = reliable ? ONE - TENTH : Math.min(ONE, (nbUp + nbDn) / (TWO * RELIABILITY_MIN));

            // Blend lightly with base to avoid degeneracy
            return alpha * prod + (ONE - alpha) * base;
            // Ramp-in blending until reliable (after an incumbent exists)
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

        myOptimisationSense = model.getOptimisationSense();

        myStrategy = strategy;

        List<Variable> integerVariables = model.getIntegerVariables();

        int nbIntegers = integerVariables.size();

        myIndices = new int[nbIntegers];
        for (int i = 0; i < nbIntegers; i++) {
            int globalIndex = model.indexOf(integerVariables.get(i));
            myIndices[i] = globalIndex;
        }

        myWorkerPriorities = strategy.getWorkerPriorities();

        for (int i = 0; i < myWorkerPriorities.size(); i++) {
            Comparator<NodeKey> prio = myWorkerPriorities.get(i);
            if (prio == NodeKey.MIN_OBJECTIVE || prio == NodeKey.MAX_OBJECTIVE) {
                if (myOptimisationSense == Optimisation.Sense.MIN) {
                    myWorkerPriorities.set(i, NodeKey.MIN_OBJECTIVE);
                } else if (myOptimisationSense == Optimisation.Sense.MAX) {
                    myWorkerPriorities.set(i, NodeKey.MAX_OBJECTIVE);
                }
            }
        }
    }

    @Override
    public int countUniqueStrategies() {
        return myStrategy.countUniqueStrategies();
    }

    @Override
    public NumberContext getGapTolerance() {
        return myStrategy.getGapTolerance();
    }

    @Override
    public GMICutConfiguration getGMICutConfiguration() {
        return myStrategy.getGMICutConfiguration();
    }

    @Override
    public NumberContext getIntegralityTolerance() {
        return myStrategy.getIntegralityTolerance();
    }

    @Override
    public List<Comparator<NodeKey>> getWorkerPriorities() {
        return myWorkerPriorities;
    }

    @Override
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

    protected int getIndex(final int idx) {
        return myIndices[idx];
    }

    protected abstract ModelStrategy initialise(final MultiaryFunction.TwiceDifferentiable<Double> function, final Access1D<?> point);

    protected abstract boolean isCutRatherThanBranch(double displacement, boolean found);

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

    protected abstract void markInfeasible(NodeKey key, boolean found, double incumbentValue);

    protected abstract void markInteger(NodeKey key, Optimisation.Result result);

    /**
     * Hook to update pseudo-costs after a node is solved.
     */
    protected abstract void onNodeSolved(final NodeKey key, final Optimisation.Result child, final double childObj, final boolean minimisation);

    protected abstract double scoreBranch(final int idx, final double displaceDown, final double displaceUp, final boolean found);

    boolean isMinimisation() {
        return myOptimisationSense == Optimisation.Sense.MIN;
    }

}
