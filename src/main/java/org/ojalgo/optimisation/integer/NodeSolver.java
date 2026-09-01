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

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.ojalgo.function.special.MissingMath;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.IntermediateSolver;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.UpdatableSolver;
import org.ojalgo.optimisation.integer.IntegerStrategy.CutConfiguration;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.keyvalue.EntryPair;
import org.ojalgo.type.keyvalue.EntryPair.KeyedPrimitive;

public final class NodeSolver extends IntermediateSolver {

    /**
     * Identifies a cut by its (index, coefficient) pairs and limits, coefficients rounded to 10 significant
     * digits, so that exact duplicates within a separation round can be recognised cheaply.
     */
    static final class CutSignature {

        private static long round(final BigDecimal value) {
            return value == null ? Long.MIN_VALUE : Math.round(value.doubleValue() * 1.0E10);
        }

        private final long[] myCoefficients;
        private final int myHash;
        private final int[] myIndices;
        private final long myLower;
        private final long myUpper;

        CutSignature(final Expression cut) {

            Set<Entry<IntIndex, BigDecimal>> entries = cut.getLinearEntrySet();
            int size = entries.size();

            int[] indices = new int[size];
            int k = 0;
            for (Entry<IntIndex, BigDecimal> entry : entries) {
                indices[k++] = entry.getKey().index;
            }
            Arrays.sort(indices);

            long[] coefficients = new long[size];
            for (int i = 0; i < size; i++) {
                coefficients[i] = CutSignature.round(cut.get(new IntIndex(indices[i])));
            }

            myIndices = indices;
            myCoefficients = coefficients;
            myLower = CutSignature.round(cut.getLowerLimit());
            myUpper = CutSignature.round(cut.getUpperLimit());
            myHash = 31 * (31 * (31 * Arrays.hashCode(indices) + Arrays.hashCode(coefficients)) + Long.hashCode(myLower)) + Long.hashCode(myUpper);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CutSignature)) {
                return false;
            }
            CutSignature other = (CutSignature) obj;
            return myHash == other.myHash && myLower == other.myLower && myUpper == other.myUpper && Arrays.equals(myIndices, other.myIndices)
                    && Arrays.equals(myCoefficients, other.myCoefficients);
        }

        @Override
        public int hashCode() {
            return myHash;
        }

    }

    /**
     * Base class for the cut separators. A separator builds candidate cuts; the quality filtering, naming and
     * per-round bookkeeping is shared and lives here: a round starts with
     * {@link #beginRound(ExpressionsBasedModel, Optimisation.Result, CutConfiguration)}, every candidate is
     * created with {@link #newCut(ExpressionsBasedModel)} and offered to
     * {@link #accept(ExpressionsBasedModel, Expression)}, and {@link #endRound(ExpressionsBasedModel)} keeps
     * the best cuts (by efficacy) up to the configured maximum and returns how many were kept.
     * <p>
     * A separator holds no reference to a model; the model is an argument of every operation that needs it. A
     * subclass that caches model structure keeps track of which model that structure belongs to.
     */
    abstract static class Separator {

        static final AtomicInteger COUNTER = new AtomicInteger();
        static final boolean DEBUG = false;

        private static boolean reject(final ExpressionsBasedModel model, final Expression cut) {
            model.removeExpression(cut.getName());
            return false;
        }

        private final List<KeyedPrimitive<String>> myAccepted = new ArrayList<>();
        private CutConfiguration myConfiguration = null;
        private int myMaxCuts = 0;
        private int myMaxElements = 0;
        private double myMinEfficacy = ZERO;
        private int myNbAttempted = 0;
        private int myNbRejectedDensity = 0;
        private int myNbRejectedDynamism = 0;
        private int myNbRejectedEfficacy = 0;
        private int myNbRejectedSimilar = 0;
        /**
         * Signatures of the cuts accepted this round, to throw away exact duplicates before the expensive
         * similarity check against every constraint of the model. Several candidates of one round often are
         * the same cut (GMI cuts from tableau rows of the same structure: on stein27 more than half of the
         * candidates).
         */
        private final Set<CutSignature> myRoundSignatures = new HashSet<>();

        private Optimisation.Result mySolution = null;

        Separator() {
            super();
        }

        /**
         * By how much the current LP solution violates the cut (positive if violated).
         */
        private double violation(final Expression cut) {

            double value = cut.evaluate(mySolution).doubleValue();
            double retVal = Double.NEGATIVE_INFINITY;

            BigDecimal lower = cut.getLowerLimit();
            if (lower != null) {
                retVal = Math.max(retVal, lower.doubleValue() - value);
            }
            BigDecimal upper = cut.getUpperLimit();
            if (upper != null) {
                retVal = Math.max(retVal, value - upper.doubleValue());
            }

            return retVal;
        }

        /**
         * Offer a candidate cut, created with {@link #newCut(ExpressionsBasedModel)} and fully specified
         * (coefficients and one or both limits). The cut is kept in the model if it is violated enough by the
         * current LP solution (efficacy: violation divided by the euclidean norm of the coefficients), not
         * too dense, not too badly scaled, and not similar to an existing constraint. Otherwise it is removed
         * again.
         *
         * @return true if the cut was kept
         */
        final boolean accept(final ExpressionsBasedModel model, final Expression cut) {

            myNbAttempted++;

            double violation = this.violation(cut);
            double norm = ZERO;
            double largest = ZERO;
            double smallest = Double.POSITIVE_INFINITY;
            int nbElements = 0;
            for (Entry<IntIndex, BigDecimal> entry : cut.getLinearEntrySet()) {
                double value = entry.getValue().doubleValue();
                if (value != ZERO) {
                    double abs = Math.abs(value);
                    norm += value * value;
                    largest = Math.max(largest, abs);
                    smallest = Math.min(smallest, abs);
                    nbElements++;
                }
            }
            norm = Math.sqrt(norm);

            double efficacy = violation > ZERO && norm > ZERO ? violation / norm : ZERO;

            if (efficacy <= myMinEfficacy) {
                myNbRejectedEfficacy++;
                return Separator.reject(model, cut);
            }

            if (nbElements > myMaxElements) {
                myNbRejectedDensity++;
                return Separator.reject(model, cut);
            }

            if (myConfiguration.dynanism.isSmall(largest, smallest)) {
                myNbRejectedDynamism++;
                return Separator.reject(model, cut);
            }

            CutSignature signature = new CutSignature(cut);

            if (myRoundSignatures.contains(signature) || model.checkSimilarity(cut)) {
                myNbRejectedSimilar++;
                return Separator.reject(model, cut);
            }

            myRoundSignatures.add(signature);
            myAccepted.add(EntryPair.of(cut.getName(), efficacy));

            return true;
        }

        /**
         * Start a separation round for the given LP solution.
         */
        final void beginRound(final ExpressionsBasedModel model, final Optimisation.Result solution, final CutConfiguration configuration) {

            int nbVariables = model.countVariables();

            mySolution = solution;
            myConfiguration = configuration;
            myMaxCuts = configuration.getMaxCuts(nbVariables);
            myMaxElements = configuration.getMaxElements(nbVariables);
            myMinEfficacy = configuration.efficacy.epsilon();

            myAccepted.clear();
            myRoundSignatures.clear();
            myNbAttempted = 0;
            myNbRejectedDensity = 0;
            myNbRejectedDynamism = 0;
            myNbRejectedEfficacy = 0;
            myNbRejectedSimilar = 0;
        }

        final int countAccepted() {
            return myAccepted.size();
        }

        final int countAttempted() {
            return myNbAttempted;
        }

        final int countRejectedDensity() {
            return myNbRejectedDensity;
        }

        final int countRejectedDynamism() {
            return myNbRejectedDynamism;
        }

        final int countRejectedEfficacy() {
            return myNbRejectedEfficacy;
        }

        final int countRejectedSimilar() {
            return myNbRejectedSimilar;
        }

        /**
         * End the round: if more cuts were accepted than the configured maximum, the weakest (by efficacy)
         * are removed again.
         *
         * @return The number of cuts kept in the model
         */
        final int endRound(final ExpressionsBasedModel model) {

            if (myAccepted.size() > myMaxCuts) {
                myAccepted.sort(Comparator.comparingDouble(KeyedPrimitive::doubleValue));
                for (int i = 0, limit = myAccepted.size() - myMaxCuts; i < limit; i++) {
                    model.removeExpression(myAccepted.get(i).getKey());
                }
                myAccepted.subList(0, myAccepted.size() - myMaxCuts).clear();
            }

            return myAccepted.size();
        }

        final Optimisation.Result getSolution() {
            return mySolution;
        }

        /**
         * @return true if fewer cuts than the configured maximum have been accepted this round
         */
        final boolean isRoomForMore() {
            return myAccepted.size() < myMaxCuts;
        }

        /**
         * For separators that can estimate the efficacy of a candidate cheaply before building it: true (and
         * the candidate counted as attempted and rejected) if the estimate is below the minimum.
         */
        final boolean isTooWeak(final double estimatedEfficacy) {
            if (estimatedEfficacy <= myMinEfficacy) {
                myNbAttempted++;
                myNbRejectedEfficacy++;
                return true;
            }
            return false;
        }

        /**
         * A new, empty expression named after the cut type, to be filled in and then offered to
         * {@link #accept(Expression)}.
         */
        final Expression newCut(final ExpressionsBasedModel model) {
            return model.newExpression("CUT_" + this.type() + "_" + COUNTER.incrementAndGet());
        }

        /**
         * Book-keeping for a candidate the separator recognised as a duplicate of one already offered this
         * round, before building it (counts as attempted and rejected as similar).
         */
        final void skipAsDuplicate() {
            myNbAttempted++;
            myNbRejectedSimilar++;
        }

        /**
         * 2-character cut type identifier
         */
        abstract String type();

    }

    private static final boolean DEBUG = false;
    /**
     * One separator instance per type and thread. The separators hold no model reference (the model is an
     * argument of every operation) and only per-round state, plus caches that they validate against the model
     * they were built for, so an instance can serve every node solver used by the same worker thread.
     */
    private static final ThreadLocal<CliqueSeparator> SEP_CLIQUE = ThreadLocal.withInitial(CliqueSeparator::new);
    private static final ThreadLocal<FlowCoverSeparator> SEP_FLOW_COVER = ThreadLocal.withInitial(FlowCoverSeparator::new);
    private static final ThreadLocal<GMISeparator> SEP_GOMORY_MIXED_INTEGER = ThreadLocal.withInitial(GMISeparator::new);
    private static final ThreadLocal<ImpliedBoundsSeparator> SEP_IMPLIED_BOUNDS = ThreadLocal.withInitial(ImpliedBoundsSeparator::new);
    private static final ThreadLocal<KnapsackCoverSeparator> SEP_KNAPSACK_COVER = ThreadLocal.withInitial(KnapsackCoverSeparator::new);
    private static final ThreadLocal<MIRSeparator> SEP_MIXED_INTEGER_ROUNDING = ThreadLocal.withInitial(MIRSeparator::new);

    private boolean myCutRoundDone = false;
    private Boolean myInPlaceBoundUpdateSafe = null;

    NodeSolver(final ExpressionsBasedModel model) {
        super(model);
    }

    private boolean generateCuts(final CutConfiguration configGMI, final CutConfiguration configMIR, final CutConfiguration configFC,
            final CutConfiguration configKC, final CutConfiguration configCL, final CutConfiguration configIB, final CutStatistics statistics,
            final boolean root) {

        ExpressionsBasedModel model = this.getModel();
        Result result = this.getResult();

        int roundsFC = configFC != null ? configFC.iterations : 0;
        int roundsMIR = configMIR != null ? configMIR.iterations : 0;
        int roundsGMI = configGMI != null ? configGMI.iterations : 0;
        int roundsKC = configKC != null ? configKC.iterations : 0;
        int roundsCL = configCL != null ? configCL.iterations : 0;
        int roundsIB = configIB != null ? configIB.iterations : 0;

        int countFC = 0;
        int countMIR = 0;
        int countGMI = 0;
        int countKC = 0;
        int countCL = 0;
        int countIB = 0;

        FlowCoverSeparator sepFC = SEP_FLOW_COVER.get();
        MIRSeparator sepMR = SEP_MIXED_INTEGER_ROUNDING.get();
        GMISeparator sepGM = SEP_GOMORY_MIXED_INTEGER.get();
        KnapsackCoverSeparator sepKC = SEP_KNAPSACK_COVER.get();
        CliqueSeparator sepCL = SEP_CLIQUE.get();
        ImpliedBoundsSeparator sepIB = SEP_IMPLIED_BOUNDS.get();

        int maxRounds = MissingMath.max(roundsFC, roundsMIR, roundsGMI, roundsKC, roundsCL, roundsIB);

        boolean retVal = false;

        for (int round = 0; round < maxRounds; round++) {

            countFC = 0;
            countMIR = 0;
            countGMI = 0;
            countKC = 0;
            countCL = 0;
            countIB = 0;

            if (!this.isSolved()) {
                break;
            }

            double valueBefore = result.getValue();

            if (round < roundsFC && statistics.shouldTry(sepFC.type(), root)) {
                long started = System.nanoTime();
                countFC = sepFC.generateCuts(model, result, configFC);
                statistics.record(sepFC, root, System.nanoTime() - started);
                if (DEBUG) {
                    BasicLogger.debug("{} new FC cuts, iteration {}", countFC, 1 + round);
                }
            }

            if (round < roundsMIR && statistics.shouldTry(sepMR.type(), root)) {
                long started = System.nanoTime();
                countMIR = sepMR.generateCuts(model, result, configMIR);
                statistics.record(sepMR, root, System.nanoTime() - started);
                if (DEBUG) {
                    BasicLogger.debug("{} new MIR cuts, iteration {}", countMIR, 1 + round);
                }
            }

            if (round < roundsGMI && this.getSolver() instanceof UpdatableSolver && statistics.shouldTry(sepGM.type(), root)) {
                long started = System.nanoTime();
                countGMI = sepGM.generateCuts(model, (UpdatableSolver) this.getSolver(), result, configGMI);
                statistics.record(sepGM, root, System.nanoTime() - started);
                if (DEBUG) {
                    BasicLogger.debug("{} new GMI cuts, iteration {}", countGMI, 1 + round);
                }
            }

            if (round < roundsKC && statistics.shouldTry(sepKC.type(), root)) {
                long started = System.nanoTime();
                countKC = sepKC.generateCuts(model, result, configKC);
                statistics.record(sepKC, root, System.nanoTime() - started);
                if (DEBUG) {
                    BasicLogger.debug("{} new KC cuts, iteration {}", countKC, 1 + round);
                }
            }

            if (round < roundsCL && statistics.shouldTry(sepCL.type(), root)) {
                long started = System.nanoTime();
                countCL = sepCL.generateCuts(model, result, configCL);
                statistics.record(sepCL, root, System.nanoTime() - started);
                if (DEBUG) {
                    BasicLogger.debug("{} new CL cuts, iteration {}", countCL, 1 + round);
                }
            }

            if (round < roundsIB && statistics.shouldTry(sepIB.type(), root)) {
                long started = System.nanoTime();
                countIB = sepIB.generateCuts(model, result, configIB);
                statistics.record(sepIB, root, System.nanoTime() - started);
                if (DEBUG) {
                    BasicLogger.debug("{} new IB cuts, iteration {}", countIB, 1 + round);
                }
            }

            if ((countFC + countMIR + countGMI + countKC + countCL + countIB) > 0) {

                retVal = true;

                long started = System.nanoTime();
                this.reset();
                result = this.getResult();
                long resolveNanos = System.nanoTime() - started;

                if (result == null || !result.getState().isOptimal()) {
                    break;
                }

                statistics.recordRound(root, valueBefore, result.getValue(), resolveNanos);

            } else {

                break;
            }
        }

        return retVal;
    }

    /**
     * @param statistics Where to record what the separators did
     * @param root       Whether this is the root node
     */
    boolean generateCuts(final ModelStrategy strategy, final CutStatistics statistics, final boolean root) {

        CutConfiguration gmi = strategy.getGMICutConfiguration();
        CutConfiguration mir = strategy.getMIRCutConfiguration();
        CutConfiguration fc = strategy.getFCCutConfiguration();
        CutConfiguration kc = strategy.getKCCutConfiguration();
        CutConfiguration cl = strategy.getCLCutConfiguration();
        CutConfiguration ib = strategy.getIBCutConfiguration();

        if (this.generateCuts(gmi, mir, fc, kc, cl, ib, statistics, root)) {
            this.reset();
            myCutRoundDone = true;
            return true;
        } else {
            return false;
        }
    }

    double getReducedGradient(final int globalModelIndex) {
        if (this.isSolved() && this.getSolver() instanceof UpdatableSolver) {
            int indexInSolver = this.getIndexInSolver(globalModelIndex);
            if (indexInSolver >= 0) {
                return ((UpdatableSolver) this.getSolver()).getReducedGradient(indexInSolver);
            }
        }
        return 0.0;
    }

    boolean isCutRoundDone() {
        return myCutRoundDone;
    }

    /**
     * Whether this node's relaxation can absorb a branch-induced bound change in place (via
     * {@code update(variable)}) instead of being rebuilt from scratch.
     * <p>
     * True only for the linear/simplex relaxation: there {@code SimplexSolver.updateRange} genuinely applies
     * the change in place. A quadratic relaxation is solved by a convex solver — ADMM drifts when
     * warm-started across bound changes and the active-set path effectively rebuilds anyway — so those must
     * keep the historical, numerically-stable behaviour of rebuilding fresh on every branch
     * ({@link NodeKey#enforceBounds(NodeSolver, ModelStrategy)} forces a {@code reset()} for them, as the
     * original over-broad sign-change condition implicitly did). The model's structure is fixed for this
     * solver's lifetime, so the answer is cached.
     */
    boolean isInPlaceBoundUpdateSafe() {

        if (myInPlaceBoundUpdateSafe == null) {
            myInPlaceBoundUpdateSafe = Boolean.valueOf(!this.getModel().isAnyExpressionQuadratic());
        }

        return myInPlaceBoundUpdateSafe.booleanValue();
    }

}
