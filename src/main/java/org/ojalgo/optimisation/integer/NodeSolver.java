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
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.IntermediateSolver;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.UpdatableSolver;
import org.ojalgo.optimisation.integer.IntegerStrategy.CutConfiguration;
import org.ojalgo.optimisation.integer.IntegerStrategy.CutType;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.keyvalue.EntryPair;

public final class NodeSolver extends IntermediateSolver {

    /**
     * Everything a separator needs for one separation round: the model to add cuts to, the LP solution to
     * separate, the solver that produced it, the configuration, and the cuts accepted so far at this node.
     * Created and owned by the {@link NodeSolver}, one instance per round, so that a separator (which is
     * shared by all node solvers on a thread) never has to hold a reference to any of it.
     */
    static final class CutRound {

        /**
         * The cuts currently in the model that were added at this node, keyed by their structure. Shared by
         * all separators and all rounds at the node. A candidate with the structure of an existing cut is
         * combined with it (or rejected) without the full {@link ExpressionsBasedModel#findSimilar} scan.
         */
        final Map<CutSignature, Expression> accepted;
        final CutConfiguration configuration;
        final ExpressionsBasedModel model;
        final Optimisation.Result solution;
        /**
         * The LP solver that produced the solution, if it is available and updatable, otherwise null. Only
         * GMI cuts need it.
         */
        final UpdatableSolver solver;

        CutRound(final ExpressionsBasedModel model, final UpdatableSolver solver, final Optimisation.Result solution, final CutConfiguration configuration,
                final Map<CutSignature, Expression> accepted) {
            super();
            this.model = model;
            this.solver = solver;
            this.solution = solution;
            this.configuration = configuration;
            this.accepted = accepted;
        }

    }

    /**
     * Structural identity of a cut: variable indices and enforced coefficients, but not the limits. Two cuts
     * with the same signature differ at most in their limits, and the tighter limits dominate.
     */
    static final class CutSignature {

        private static final Comparator<Entry<IntIndex, BigDecimal>> BY_INDEX = Comparator.comparingInt(entry -> entry.getKey().index);

        private final BigDecimal[] myCoefficients;
        private final int myHash;
        private final int[] myIndices;

        CutSignature(final Expression cut) {

            Set<Entry<IntIndex, BigDecimal>> entries = cut.getLinearEntrySet();
            int size = entries.size();

            @SuppressWarnings("unchecked")
            Entry<IntIndex, BigDecimal>[] sorted = entries.toArray(new Entry[size]);
            Arrays.sort(sorted, BY_INDEX);

            int[] indices = new int[size];
            BigDecimal[] coefficients = new BigDecimal[size];
            for (int i = 0; i < size; i++) {
                indices[i] = sorted[i].getKey().index;
                coefficients[i] = sorted[i].getValue().stripTrailingZeros();
            }

            myIndices = indices;
            myCoefficients = coefficients;
            myHash = 31 * Arrays.hashCode(indices) + Arrays.hashCode(coefficients);
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
            return myHash == other.myHash && Arrays.equals(myIndices, other.myIndices) && Arrays.equals(myCoefficients, other.myCoefficients);
        }

        @Override
        public int hashCode() {
            return myHash;
        }

    }

    /**
     * Base class for the cut separators. A separator builds candidate cuts; the quality filtering, naming and
     * per-round bookkeeping is shared and lives here. A round is a {@link CutRound}: it starts with
     * {@link #beginRound(CutRound)}, every candidate is created with {@link #newCut(CutRound)} and offered to
     * {@link #accept(CutRound, Expression)}, and {@link #endRound(CutRound)} keeps the best cuts (by
     * efficacy) up to the configured maximum and returns how many were kept.
     * <p>
     * A separator holds no reference to a model, a solution or anything else it does not own; all of that is
     * in the {@link CutRound} passed to every operation. A subclass that caches model structure keeps track
     * of which model that structure belongs to.
     */
    abstract static class Separator {

        /**
         * A cut accepted this round: its name, its signature and its efficacy. Deliberately not the cut
         * itself, so that the separator holds no reference to the model between rounds.
         */
        private static final class Accepted {

            final double efficacy;
            final String name;
            final CutSignature signature;

            Accepted(final String name, final CutSignature signature, final double efficacy) {
                super();
                this.name = name;
                this.signature = signature;
                this.efficacy = efficacy;
            }

        }

        /**
         * Rounding (outwards) of a limit brought into another scale
         */
        static final NumberContext PRECISION = NumberContext.of(12);
        private static final MathContext ROUND_LOWER = PRECISION.withMode(RoundingMode.FLOOR).getMathContext();
        private static final MathContext ROUND_UPPER = PRECISION.withMode(RoundingMode.CEILING).getMathContext();
        static final AtomicInteger COUNTER = new AtomicInteger();
        static final boolean DEBUG = false;

        /**
         * Positive if the candidate limit is tighter than the existing one, negative if looser, zero if
         * equal. A limit the existing cut lacks is tighter; a limit the candidate lacks is looser.
         *
         * @param sign 1 for lower limits (larger is tighter), -1 for upper limits (smaller is tighter)
         */
        private static int compareLimits(final BigDecimal candidate, final BigDecimal existing, final int sign) {
            if (candidate == null) {
                return existing == null ? 0 : -1;
            }
            if (existing == null) {
                return 1;
            }
            return sign * candidate.compareTo(existing);
        }

        /**
         * True if the candidate limits are at least as tight as the existing ones on both sides, and strictly
         * tighter on at least one.
         */
        private static boolean isTighter(final BigDecimal candidateLower, final BigDecimal candidateUpper, final BigDecimal existingLower,
                final BigDecimal existingUpper) {
            int lower = Separator.compareLimits(candidateLower, existingLower, 1);
            int upper = Separator.compareLimits(candidateUpper, existingUpper, -1);
            return lower >= 0 && upper >= 0 && (lower > 0 || upper > 0);
        }

        private static boolean reject(final ExpressionsBasedModel model, final Expression cut) {
            model.removeExpression(cut.getName());
            return false;
        }

        /**
         * A limit of a constraint whose coefficients are {@code factor} times those of the candidate,
         * expressed in the candidate's scale and rounded outwards.
         */
        private static BigDecimal scale(final BigDecimal limit, final BigDecimal factor, final MathContext outwards) {
            return limit == null ? null : limit.divide(factor, outwards);
        }

        /**
         * The candidate takes over any limit it lacks from an existing constraint with the same structure
         * (limits given in the candidate's scale), so that it carries the combined information.
         *
         * @return true if the candidate is then tighter than the existing constraint
         */
        private static boolean takeOver(final Expression cut, final BigDecimal existingLower, final BigDecimal existingUpper) {
            if (cut.getLowerLimit() == null && existingLower != null) {
                cut.lower(existingLower);
            }
            if (cut.getUpperLimit() == null && existingUpper != null) {
                cut.upper(existingUpper);
            }
            return Separator.isTighter(cut.getLowerLimit(), cut.getUpperLimit(), existingLower, existingUpper);
        }

        private final List<Accepted> myAccepted = new ArrayList<>();
        private int myAttempted = 0;
        private int myMaxCuts = 0;
        private int myMaxElements = 0;
        private double myMinEfficacy = ZERO;
        private int myRejectedDensity = 0;
        private int myRejectedDynamism = 0;
        private int myRejectedEfficacy = 0;
        private int myRejectedSimilar = 0;

        Separator() {
            super();
        }

        /**
         * Remove a cut of this node that a tighter candidate replaces: from the model, from the accepted cuts
         * of the node, and from this round's list.
         */
        private void replace(final CutRound round, final Expression existing, final CutSignature existingSignature) {
            round.model.removeExpression(existing.getName());
            round.accepted.remove(existingSignature);
            myAccepted.removeIf(entry -> entry.signature.equals(existingSignature));
        }

        /**
         * Offer a candidate cut, created with {@link #newCut(CutRound)} and fully specified (coefficients and
         * one or both limits). The cut is kept in the model if it is violated enough by the LP solution
         * (efficacy: violation divided by the euclidean norm of the coefficients), not too dense, not too
         * badly scaled, and not similar to an existing constraint. Otherwise it is removed again.
         * <p>
         * A candidate with the same structure as a cut already accepted at this node (same variables,
         * coefficients proportional) is combined with it: the candidate takes over any limit it lacks from
         * the existing cut, and then replaces it if the result is tighter, otherwise the candidate is
         * rejected. A candidate with the same structure as a constraint of the model is kept, beside that
         * constraint, only if it is tighter. A constraint of the model is never modified or removed here: the
         * LP solver was built from it, and the GMI separator expands its slack variable from its limits.
         *
         * @return true if the cut was kept
         */
        final boolean accept(final CutRound round, final Expression cut) {

            myAttempted++;

            ExpressionsBasedModel model = round.model;
            Optimisation.Result solution = round.solution;

            double activity = ZERO;
            double norm = ZERO;
            double largest = ZERO;
            double smallest = Double.POSITIVE_INFINITY;
            int nbElements = 0;
            for (Entry<IntIndex, BigDecimal> entry : cut.getLinearEntrySet()) {
                double value = entry.getValue().doubleValue();
                if (value != ZERO) {
                    double abs = Math.abs(value);
                    activity += value * solution.doubleValue(entry.getKey().index);
                    norm += value * value;
                    largest = Math.max(largest, abs);
                    smallest = Math.min(smallest, abs);
                    nbElements++;
                }
            }
            norm = Math.sqrt(norm);

            // By how much the LP solution violates the cut (positive if violated)
            double violation = Double.NEGATIVE_INFINITY;
            BigDecimal lower = cut.getLowerLimit();
            if (lower != null) {
                violation = Math.max(violation, lower.doubleValue() - activity);
            }
            BigDecimal upper = cut.getUpperLimit();
            if (upper != null) {
                violation = Math.max(violation, activity - upper.doubleValue());
            }

            double efficacy = violation > ZERO && norm > ZERO ? violation / norm : ZERO;

            if (efficacy <= myMinEfficacy) {
                myRejectedEfficacy++;
                return Separator.reject(model, cut);
            }

            if (nbElements > myMaxElements) {
                myRejectedDensity++;
                return Separator.reject(model, cut);
            }

            if (round.configuration.dynamism.isSmall(largest, smallest)) {
                myRejectedDynamism++;
                return Separator.reject(model, cut);
            }

            cut.enforce(PRECISION);
            cut.tighten();

            CutSignature signature = new CutSignature(cut);
            Map<CutSignature, Expression> accepted = round.accepted;

            Expression existing = accepted.get(signature);
            if (existing != null) {
                // Same structure and scale as a cut of this node
                if (!Separator.takeOver(cut, existing.getLowerLimit(), existing.getUpperLimit())) {
                    myRejectedSimilar++;
                    return Separator.reject(model, cut);
                }
                this.replace(round, existing, signature);
            } else {
                EntryPair<Expression, BigDecimal> similar = model.findSimilar(cut);
                if (similar != null) {
                    Expression other = similar.getKey();
                    BigDecimal factor = similar.getValue();
                    boolean positive = factor.signum() > 0;
                    BigDecimal otherLower = Separator.scale(positive ? other.getLowerLimit() : other.getUpperLimit(), factor, ROUND_LOWER);
                    BigDecimal otherUpper = Separator.scale(positive ? other.getUpperLimit() : other.getLowerLimit(), factor, ROUND_UPPER);
                    CutSignature otherSignature = new CutSignature(other);
                    if (accepted.get(otherSignature) == other) {
                        // Same structure as a cut of this node, other scale
                        if (!Separator.takeOver(cut, otherLower, otherUpper)) {
                            myRejectedSimilar++;
                            return Separator.reject(model, cut);
                        }
                        this.replace(round, other, otherSignature);
                    } else if (!Separator.isTighter(cut.getLowerLimit(), cut.getUpperLimit(), otherLower, otherUpper)) {
                        // Same structure as a constraint of the model
                        myRejectedSimilar++;
                        return Separator.reject(model, cut);
                    }
                }
            }

            accepted.put(signature, cut);
            myAccepted.add(new Accepted(cut.getName(), signature, efficacy));

            return true;
        }

        /**
         * Start a separation round.
         */
        final void beginRound(final CutRound round) {

            int nbVariables = round.model.countVariables();

            myMaxCuts = round.configuration.getMaxCuts(nbVariables);
            myMaxElements = round.configuration.getMaxElements(nbVariables);
            myMinEfficacy = round.configuration.efficacy.epsilon();

            myAccepted.clear();
            myAttempted = 0;
            myRejectedDensity = 0;
            myRejectedDynamism = 0;
            myRejectedEfficacy = 0;
            myRejectedSimilar = 0;
        }

        final int countAccepted() {
            return myAccepted.size();
        }

        final int countAttempted() {
            return myAttempted;
        }

        final int countRejectedDensity() {
            return myRejectedDensity;
        }

        final int countRejectedDynamism() {
            return myRejectedDynamism;
        }

        final int countRejectedEfficacy() {
            return myRejectedEfficacy;
        }

        final int countRejectedSimilar() {
            return myRejectedSimilar;
        }

        /**
         * End the round: if more cuts were accepted than the configured maximum, the weakest (by efficacy)
         * are removed again.
         *
         * @return The number of cuts kept in the model
         */
        final int endRound(final CutRound round) {

            if (myAccepted.size() > myMaxCuts) {
                myAccepted.sort(Comparator.comparingDouble(entry -> entry.efficacy));
                int limit = myAccepted.size() - myMaxCuts;
                for (int i = 0; i < limit; i++) {
                    Accepted weakest = myAccepted.get(i);
                    round.model.removeExpression(weakest.name);
                    round.accepted.remove(weakest.signature);
                }
                myAccepted.subList(0, limit).clear();
            }

            return myAccepted.size();
        }

        /**
         * Run one separation round: call {@link #beginRound(CutRound)}, offer every candidate to
         * {@link #accept(CutRound, Expression)} and return the result of {@link #endRound(CutRound)}. Must
         * call {@link #beginRound} even when returning early, so that the per-round counters read by
         * {@link CutStatistics} are those of this round.
         *
         * @return The number of cuts added to the model
         */
        abstract int generateCuts(CutRound round);

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
                myAttempted++;
                myRejectedEfficacy++;
                return true;
            }
            return false;
        }

        /**
         * A new, empty expression named after the cut type, to be filled in and then offered to
         * {@link #accept(CutRound, Expression)}.
         */
        final Expression newCut(final CutRound round) {
            return round.model.newExpression("CUT_" + this.type().code + "_" + COUNTER.incrementAndGet());
        }

        /**
         * Book-keeping for a candidate the separator recognised as a duplicate of one already offered this
         * round, before building it (counts as attempted and rejected as similar).
         */
        final void skipAsDuplicate() {
            myAttempted++;
            myRejectedSimilar++;
        }

        abstract CutType type();

    }

    private static final boolean DEBUG = false;
    /**
     * One separator instance per type and thread. The separators hold no model reference (everything they
     * need is in the {@link CutRound} passed to them) and only per-round state, plus caches that they
     * validate against the model they were built for, so an instance can serve every node solver used by the
     * same worker thread.
     */
    private static final ThreadLocal<EnumMap<CutType, Separator>> SEPARATORS = ThreadLocal.withInitial(NodeSolver::newSeparators);

    private static EnumMap<CutType, Separator> newSeparators() {

        EnumMap<CutType, Separator> retVal = new EnumMap<>(CutType.class);

        retVal.put(CutType.CLIQUE, new CliqueSeparator());
        retVal.put(CutType.FLOW_COVER, new FlowCoverSeparator());
        retVal.put(CutType.GOMORY_MIXED_INTEGER, new GMISeparator());
        retVal.put(CutType.IMPLIED_BOUNDS, new ImpliedBoundsSeparator());
        retVal.put(CutType.KNAPSACK_COVER, new KnapsackCoverSeparator());
        retVal.put(CutType.MIXED_INTEGER_ROUNDING, new MIRSeparator());

        return retVal;
    }

    private boolean myCutRoundDone = false;
    private Boolean myInPlaceBoundUpdateSafe = null;

    NodeSolver(final ExpressionsBasedModel model) {
        super(model);
    }

    private UpdatableSolver getUpdatableSolver() {
        Optimisation.Solver solver = this.getSolver();
        return solver instanceof UpdatableSolver ? (UpdatableSolver) solver : null;
    }

    /**
     * Run up to {@link CutConfiguration#iterations} separation rounds. A round calls every enabled separator
     * once and, if any cut was added, re-solves the relaxation; rounds stop when a round adds no cuts or the
     * re-solve fails. Called at most once per dive, see {@link #isCutRoundDone()}.
     *
     * @param statistics Where to record what the separators did
     * @param root       Whether this is the root node
     * @return true if any cuts were added
     */
    boolean generateCuts(final ModelStrategy strategy, final CutStatistics statistics, final boolean root) {

        CutConfiguration configuration = strategy.getCutConfiguration();
        EnumMap<CutType, Separator> separators = SEPARATORS.get();

        ExpressionsBasedModel model = this.getModel();
        Result result = this.getResult();
        Map<CutSignature, Expression> accepted = new HashMap<>();

        boolean retVal = false;

        for (int round = 0; round < configuration.iterations && this.isSolved(); round++) {

            // The re-solve at the end of a round regenerates the solver, so it is looked up per round
            CutRound cutRound = new CutRound(model, this.getUpdatableSolver(), result, configuration, accepted);

            double valueBefore = result.getValue();
            int count = 0;

            for (CutType type : configuration.types) {

                if (!statistics.shouldTry(type, root)) {
                    continue;
                }

                Separator separator = separators.get(type);

                long started = System.nanoTime();
                int added = separator.generateCuts(cutRound);
                statistics.record(separator, root, System.nanoTime() - started);

                if (DEBUG) {
                    BasicLogger.debug("{} new {} cuts, iteration {}", added, type.code, 1 + round);
                }

                count += added;
            }

            if (count == 0) {
                break;
            }

            retVal = true;

            long started = System.nanoTime();
            this.reset();
            result = this.getResult();
            long resolveNanos = System.nanoTime() - started;

            if (result == null || !result.getState().isOptimal()) {
                break;
            }

            statistics.recordRound(root, valueBefore, result.getValue(), resolveNanos);
        }

        if (retVal) {
            this.reset();
            myCutRoundDone = true;
        }

        return retVal;
    }

    double getReducedGradient(final int globalModelIndex) {

        UpdatableSolver solver = this.getUpdatableSolver();
        if (solver != null && this.isSolved()) {
            int indexInSolver = this.getIndexInSolver(globalModelIndex);
            if (indexInSolver >= 0) {
                return solver.getReducedGradient(indexInSolver);
            }
        }

        return PrimitiveMath.ZERO;
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
