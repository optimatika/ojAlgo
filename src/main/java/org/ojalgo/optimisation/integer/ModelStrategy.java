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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.Sense;
import org.ojalgo.optimisation.Variable;
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

        /**
         * Adaptive cut generation state
         */
        private volatile int myCutFailedStreak = 0;
        /**
         * Adaptive cut generation state
         */
        private volatile int myCutLastDepth = -1;
        /**
         * Adaptive cut generation state
         */
        private volatile long myCutNodesSinceLastSuccess = 0L;
        private final int[] myLowerCount;
        /**
         * Pseudo-costs when down
         */
        private final double[] myLowerPseudoWeight;
        /**
         * Per-integer-variable seed copied into the pseudo-cost weights at every {@link #initialise()}.
         * Derived once at construction from each integer variable's own
         * {@link Variable#getContributionWeight() contribution weight} (normalised to {@code [0, 1]} by the
         * largest magnitude). Variables without a non-zero contribution weight — or models whose objective is
         * built via objective-marked expressions rather than per-variable weights — fall back to a uniform
         * {@link NodeKey#MINIMUM_DISPLACEMENT} seed; the search-time {@code observeBranch} adapts
         * pseudo-costs from there.
         */
        private final double[] mySeed;
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

            mySeed = new double[nbIntegers];
            double largest = ZERO;
            for (int i = 0; i < nbIntegers; i++) {
                BigDecimal weight = integerVariables.get(i).getContributionWeight();
                if (weight != null && weight.signum() != 0) {
                    double abs = Math.abs(weight.doubleValue());
                    mySeed[i] = abs;
                    if (abs > largest) {
                        largest = abs;
                    }
                }
            }
            if (!ROUGHLY.isZero(largest)) {
                for (int i = 0; i < nbIntegers; i++) {
                    mySeed[i] = Math.max(mySeed[i] / largest, NodeKey.MINIMUM_DISPLACEMENT);
                }
            } else {
                Arrays.fill(mySeed, NodeKey.MINIMUM_DISPLACEMENT);
            }
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
         * Reset per-solve state. The pseudo-cost weights are restored to the constructor-time seed (derived
         * from each integer variable's objective contribution weight); the observation counts are zeroed.
         */
        @Override
        protected void initialise() {
            Arrays.fill(myUpperCount, 0);
            Arrays.fill(myLowerCount, 0);
            System.arraycopy(mySeed, 0, myUpperPseudoWeight, 0, mySeed.length);
            System.arraycopy(mySeed, 0, myLowerPseudoWeight, 0, mySeed.length);
        }

        @Override
        protected boolean isCutRatherThanBranch(final NodeKey nodeKey, final int branchIntegerIndex, final double variableValue, final double nodeValue,
                final Optimisation.Result bestResultSoFar) {

            if (!cutting) {
                return false;
            }

            // Count each branching decision considered for cutting
            myCutNodesSinceLastSuccess++;

            // Root handled separately
            if (nodeKey.depth == 0) {
                return false;
            }

            if (bestResultSoFar == null) {
                return false;
            }

            // Spacing: depth and number of nodes since last successful cut
            if (myCutLastDepth >= 0 && nodeKey.depth - myCutLastDepth < 5) {
                return false;
            }
            if (myCutNodesSinceLastSuccess < 100) {
                return false;
            }

            if (THIRD > nodeKey.getMinimumDisplacement(branchIntegerIndex, variableValue)) {
                return false;
            }

            // Back off after repeated failed attempts
            if (myCutFailedStreak >= 3) {
                return false;
            }

            return true;
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
        protected void observeBranch(final int idx, final boolean upper, final double observation) {
            this.updatePseudo(idx, upper, observation);
        }

        /**
         * Called after a failed cut generation attempt (no cuts added). Original behaviour was to disable.
         */
        @Override
        protected void onCutFailure() {
            myCutFailedStreak++;
            if (myCutFailedStreak >= 5) {
                cutting = false; // Permanently disable after several failures
            }
        }

        /** Called after a successful cut generation attempt. */
        @Override
        protected void onCutSuccess(final NodeKey nodeKey) {
            myCutNodesSinceLastSuccess = 0L;
            myCutLastDepth = nodeKey.depth;
            myCutFailedStreak = 0;
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
        protected double scoreBranch(final int idx, final double distanceDown, final double distanceUp, final boolean found) {

            int nbDn = myLowerCount[idx];
            int nbUp = myUpperCount[idx];

            boolean reliable = Math.min(nbUp, nbDn) >= RELIABILITY_MIN;
            double base = distanceDown * distanceUp;
            double prod = DefaultStrategy.productScore(myLowerPseudoWeight[idx], myUpperPseudoWeight[idx], distanceDown, distanceUp);

            if (!reliable || !found) {
                return prod;
            }

            double alpha = reliable ? ONE - TENTH : Math.min(ONE, (nbUp + nbDn) / (TWO * RELIABILITY_MIN));

            return alpha * prod + (ONE - alpha) * base;
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

        boolean didSetObj = false;
        for (int i = 0; i < myWorkerPriorities.size(); i++) {
            Comparator<NodeKey> prio = myWorkerPriorities.get(i);
            if (prio == NodeKey.MIN_OBJECTIVE || prio == NodeKey.MAX_OBJECTIVE) {
                if (myOptimisationSense == Optimisation.Sense.MIN) {
                    myWorkerPriorities.set(i, NodeKey.MIN_OBJECTIVE);
                    didSetObj = true;
                } else if (myOptimisationSense == Optimisation.Sense.MAX) {
                    myWorkerPriorities.set(i, NodeKey.MAX_OBJECTIVE);
                    didSetObj = true;
                }
            }
        }
        if (!didSetObj) {
            if (myOptimisationSense == Optimisation.Sense.MIN) {
                myWorkerPriorities.add(NodeKey.MIN_OBJECTIVE);
            } else if (myOptimisationSense == Optimisation.Sense.MAX) {
                myWorkerPriorities.add(NodeKey.MAX_OBJECTIVE);
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
    public int getMaxRootCutRounds() {
        return myStrategy.getMaxRootCutRounds();
    }

    @Override
    public java.util.List<ModelCutGenerator> getRootCutGenerators() {
        return myStrategy.getRootCutGenerators();
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

    /**
     * Reset per-solve state. Called once per {@link IntegerSolver#solve(Optimisation.Result)} invocation,
     * before any node is processed. Any model-static state (e.g. per-variable seeds derived from the
     * objective) is established at construction; this hook is just for clearing observation counters and
     * restoring those seeds.
     */
    protected abstract void initialise();

    /**
     * Decide if cuts should be attempted at this node.
     */
    protected abstract boolean isCutRatherThanBranch(NodeKey nodeKey, int branchIntegerIndex, double variableValue, double nodeValue,
            Optimisation.Result bestResultSoFar);

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
     * Inject a pseudo-cost observation (e.g. from root strong-branching probes). Default: no-op, so custom
     * strategies are unaffected.
     *
     * @param idx         integer-variable index (same indexing as {@link #scoreBranch})
     * @param upper       true for up-branch (lower bound tightened), false for down-branch
     * @param observation degradation per unit displacement, same units as {@link #onNodeSolved}
     */
    protected void observeBranch(final int idx, final boolean upper, final double observation) {}

    /**
     * Called when cut generation produced no cuts (default: disable further cutting).
     */
    protected abstract void onCutFailure();

    /**
     * Called when cut generation added at least one cut (default: no-op).
     */
    protected abstract void onCutSuccess(final NodeKey nodeKey);

    /**
     * Hook to update pseudo-costs after a node is solved.
     */
    protected abstract void onNodeSolved(NodeKey key, Optimisation.Result child, double childObj, boolean minimisation);

    protected abstract double scoreBranch(int idx, double distanceDown, double distanceUp, boolean found);

    boolean isMinimisation() {
        return myOptimisationSense == Optimisation.Sense.MIN;
    }

}
