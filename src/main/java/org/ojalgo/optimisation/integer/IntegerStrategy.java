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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;

import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.integer.ModelStrategy.DefaultStrategy;
import org.ojalgo.type.context.NumberContext;

public interface IntegerStrategy {

    /**
     * Apart from being able to configure various standard properties, you can also provide your own
     * {@link ModelStrategy} factory.
     */
    final class ConfigurableStrategy implements IntegerStrategy {

        private final CutConfiguration myCutConfiguration;
        private final BiFunction<ExpressionsBasedModel, IntegerStrategy, ModelStrategy> myFactory;
        private final NumberContext myGapTolerance;
        private final NumberContext myIntegralityTolerance;
        private final Comparator<NodeKey>[] myPriorityDefinitions;

        ConfigurableStrategy(final Comparator<NodeKey>[] definitions, final NumberContext integrality, final NumberContext gap,
                final BiFunction<ExpressionsBasedModel, IntegerStrategy, ModelStrategy> factory, final CutConfiguration cutConfiguration) {

            super();

            myPriorityDefinitions = definitions;
            myIntegralityTolerance = integrality;
            myGapTolerance = gap;
            myFactory = factory;
            myCutConfiguration = cutConfiguration;
        }

        /**
         * Retains any existing definitions, but adds these to be used rather than the existing. If there are
         * enough threads both these additional and the previously existing definitions will be used.
         */
        public ConfigurableStrategy addPriorityDefinitions(final Comparator<NodeKey>... additionalDefinitions) {

            Comparator<NodeKey>[] totalDefinitions = (Comparator<NodeKey>[]) new Comparator<?>[additionalDefinitions.length + myPriorityDefinitions.length];

            for (int i = 0; i < additionalDefinitions.length; i++) {
                totalDefinitions[i] = additionalDefinitions[i];
            }

            for (int i = 0; i < myPriorityDefinitions.length; i++) {
                totalDefinitions[additionalDefinitions.length + i] = myPriorityDefinitions[i];
            }

            return new ConfigurableStrategy(totalDefinitions, myIntegralityTolerance, myGapTolerance, myFactory, myCutConfiguration);
        }

        @Override
        public int countUniqueStrategies() {
            return myPriorityDefinitions.length;
        }

        @Override
        public CutConfiguration getCutConfiguration() {
            return myCutConfiguration;
        }

        @Override
        public NumberContext getGapTolerance() {
            return myGapTolerance;
        }

        @Override
        public NumberContext getIntegralityTolerance() {
            return myIntegralityTolerance;
        }

        @Override
        public List<Comparator<NodeKey>> getWorkerPriorities(final int parallelism) {

            int nbWorkers = Math.max(1, parallelism);
            int nbDefinitions = myPriorityDefinitions.length;

            List<Comparator<NodeKey>> retVal = new ArrayList<>(nbWorkers);

            for (int w = 0; w < nbWorkers; w++) {
                retVal.add(myPriorityDefinitions[w % nbDefinitions]);
            }

            return retVal;
        }

        @Override
        public ModelStrategy newModelStrategy(final ExpressionsBasedModel model) {
            return myFactory.apply(model, this);
        }

        /**
         * Configure cut generation: which cut types to use, and the quality filters that apply to all of
         * them. To turn cut generation off entirely, use a configuration with no cut types
         * ({@link CutConfiguration#withTypes(CutType...)} with no arguments).
         */
        public ConfigurableStrategy withCutConfiguration(final CutConfiguration newConfiguration) {
            return new ConfigurableStrategy(myPriorityDefinitions, myIntegralityTolerance, myGapTolerance, myFactory, newConfiguration);
        }

        /**
         * Change the MIP gap
         */
        public ConfigurableStrategy withGapTolerance(final NumberContext newTolerance) {
            return new ConfigurableStrategy(myPriorityDefinitions, myIntegralityTolerance, newTolerance, myFactory, myCutConfiguration);
        }

        /**
         * Create a sub-class of {@link ModelStrategy} and provide a factory method for it here.
         */
        public ConfigurableStrategy withModelStrategyFactory(final BiFunction<ExpressionsBasedModel, IntegerStrategy, ModelStrategy> newFactory) {
            return new ConfigurableStrategy(myPriorityDefinitions, myIntegralityTolerance, myGapTolerance, newFactory, myCutConfiguration);
        }

        /**
         * Replace the priority definitions with these ones.
         */
        public ConfigurableStrategy withPriorityDefinitions(final Comparator<NodeKey>... newDefinitions) {
            return new ConfigurableStrategy(newDefinitions, myIntegralityTolerance, myGapTolerance, myFactory, myCutConfiguration);
        }

    }

    /**
     * Configuration of cut generation: which cut types are used, how many rounds are attempted at a node, and
     * the quality filters (efficacy, dynamism, density, count) that every accepted cut must pass. The filters
     * are common to all cut types. {@link #fractionality}, {@link #relaxation} and {@link #violation} only
     * affect the cut types that derive cuts from a fractional basis: GMI and MIR.
     */
    public static final class CutConfiguration {

        /**
         * Rows of the simplex tableau, and constraint coefficients, that differ from each other in magnitude
         * by more than this are considered too badly scaled, and any cut derived from them is discarded.
         */
        public final NumberContext dynamism;
        /**
         * Minimum cut violation at the current LP solution. A cut that violates the LP point by less than
         * this amount is too weak to be useful and is discarded. Both SCIP (1e-4) and HiGHS (1e-5) enforce a
         * similar threshold.
         */
        public final NumberContext efficacy;
        /**
         * The minimum fractionality of the integer variable used to generate the cut. Less than this, and the
         * (potential) cut is never generated.
         */
        public final double fractionality;
        /**
         * The maximum number of separation rounds at one node. Each round calls every enabled separator once
         * and then re-solves the relaxation. Rounds stop early when no separator produces a cut.
         */
        public final int iterations;
        /**
         * Whether to apply a relaxation when generating the cut. The exact meaning depends on the separator.
         * For GMI (Gomory Mixed Integer) cuts derived from the simplex tableau, and for the MIR (Mixed
         * Integer Rounding) separator, positive continuous variable coefficients are dropped (set to zero).
         * This produces weaker but numerically more stable cuts, which allows using a lower
         * {@link #fractionality} threshold. Other separator types ignore this flag.
         */
        public final boolean relaxation;
        /**
         * The cut types to use, in the order they are attempted within a round. The order matters: the
         * separators that scan the model's rows also see the cuts added earlier in the same round, and when
         * two separators find the same cut the one that runs second looks unproductive and is switched off
         * first. An empty list turns cut generation off.
         */
        public final List<CutType> types;
        /**
         * After the cut is generated it is transformed to be expressed in the original model variables. In
         * this process the RHS of the cut inequality changes. This parameter controls how much the RHS is
         * allowed to grow in magnitude. If it grows/expands to much the cut is discarded.
         * <p>
         * The cut/constraint violation is always exactly 1 (due to how the cut is generated). That means the
         * magnitude of the RHS becomes a measure of the relative cut violation. Allowing large RHS values is
         * equivalent to accepting small relative cut violations. The number you specify here is the inverse
         * of the relative cut violation (the absolute value of the max RHS allowed).
         */
        public final BigDecimal violation;

        private final int myMaxCutsCeiling;
        private final int myMaxCutsDivisor;
        private final int myMaxCutsFloor;
        private final int myMaxElementsCeiling;
        private final int myMaxElementsDivisor;
        private final int myMaxElementsFloor;

        /**
         * All cut types, 3 rounds, and the default filters. The types are ordered cheap, sparse and exactly
         * scaled first (implied bounds, clique, knapsack cover, flow cover) and dense or fractional last
         * (MIR, then GMI), so that MIR never derives from GMI cuts added in the same round.
         */
        public CutConfiguration() {
            this(List.of(CutType.IMPLIED_BOUNDS, CutType.CLIQUE, CutType.KNAPSACK_COVER, CutType.FLOW_COVER, CutType.MIXED_INTEGER_ROUNDING,
                    CutType.GOMORY_MIXED_INTEGER), NumberContext.of(7), NumberContext.of(6), PrimitiveMath.ELEVENTH, false, BigMath.TWELVE, 10, 3, 100, 10, 10,
                    100, 3);
        }

        private CutConfiguration(final List<CutType> newTypes, final NumberContext newDynamism, final NumberContext newEfficacy, final double newAway,
                final boolean newRelaxation, final BigDecimal newExpansion, final int newMaxCutsFloor, final int newMaxCutsDivisor, final int newMaxCutsCeiling,
                final int newMaxElementsFloor, final int newMaxElementsDivisor, final int newMaxElementsCeiling, final int newIterations) {

            super();

            types = newTypes;
            dynamism = newDynamism;
            efficacy = newEfficacy;
            fractionality = newAway;
            relaxation = newRelaxation;
            violation = newExpansion;
            iterations = newIterations;

            myMaxCutsFloor = newMaxCutsFloor;
            myMaxCutsDivisor = newMaxCutsDivisor;
            myMaxCutsCeiling = newMaxCutsCeiling;

            myMaxElementsFloor = newMaxElementsFloor;
            myMaxElementsDivisor = newMaxElementsDivisor;
            myMaxElementsCeiling = newMaxElementsCeiling;
        }

        /**
         * The maximum number of cuts one separator may keep per round, for a model of the given size.
         */
        public int getMaxCuts(final int nbVariables) {
            return Math.max(myMaxCutsFloor, Math.min(nbVariables / myMaxCutsDivisor, myMaxCutsCeiling));
        }

        /**
         * The maximum number of non-zero coefficients in a cut, for a model of the given size.
         */
        public int getMaxElements(final int nbVariables) {
            return Math.max(myMaxElementsFloor, Math.min(nbVariables / myMaxElementsDivisor, myMaxElementsCeiling));
        }

        public boolean isEnabled(final CutType type) {
            return types.contains(type);
        }

        public CutConfiguration withDynamism(final NumberContext newDynamism) {
            return new CutConfiguration(types, newDynamism, efficacy, fractionality, relaxation, violation, myMaxCutsFloor, myMaxCutsDivisor, myMaxCutsCeiling,
                    myMaxElementsFloor, myMaxElementsDivisor, myMaxElementsCeiling, iterations);
        }

        public CutConfiguration withEfficacy(final NumberContext newEfficacy) {
            return new CutConfiguration(types, dynamism, newEfficacy, fractionality, relaxation, violation, myMaxCutsFloor, myMaxCutsDivisor, myMaxCutsCeiling,
                    myMaxElementsFloor, myMaxElementsDivisor, myMaxElementsCeiling, iterations);
        }

        public CutConfiguration withFractionality(final double newFractionality) {
            return new CutConfiguration(types, dynamism, efficacy, Math.min(Math.abs(newFractionality), 0.5), relaxation, violation, myMaxCutsFloor,
                    myMaxCutsDivisor, myMaxCutsCeiling, myMaxElementsFloor, myMaxElementsDivisor, myMaxElementsCeiling, iterations);
        }

        public CutConfiguration withIterations(final int newIterations) {
            return new CutConfiguration(types, dynamism, efficacy, fractionality, relaxation, violation, myMaxCutsFloor, myMaxCutsDivisor, myMaxCutsCeiling,
                    myMaxElementsFloor, myMaxElementsDivisor, myMaxElementsCeiling, Math.max(0, newIterations));
        }

        /**
         * @param floor   minimum number of cuts accepted regardless of problem size
         * @param divisor accepted cuts scale as {@code nbVariables / divisor}
         * @param ceiling absolute maximum number of cuts accepted
         */
        public CutConfiguration withMaxCuts(final int floor, final int divisor, final int ceiling) {
            return new CutConfiguration(types, dynamism, efficacy, fractionality, relaxation, violation, Math.max(1, floor), Math.max(1, divisor),
                    Math.max(1, ceiling), myMaxElementsFloor, myMaxElementsDivisor, myMaxElementsCeiling, iterations);
        }

        /**
         * @param floor   minimum number of non-zero coefficients allowed regardless of problem size
         * @param divisor density limit scales as {@code nbVariables / divisor}
         * @param ceiling absolute maximum number of non-zero coefficients allowed
         */
        public CutConfiguration withMaxElements(final int floor, final int divisor, final int ceiling) {
            return new CutConfiguration(types, dynamism, efficacy, fractionality, relaxation, violation, myMaxCutsFloor, myMaxCutsDivisor, myMaxCutsCeiling,
                    Math.max(1, floor), Math.max(1, divisor), Math.max(1, ceiling), iterations);
        }

        public CutConfiguration withRelaxation(final boolean newRelaxation) {
            return new CutConfiguration(types, dynamism, efficacy, fractionality, newRelaxation, violation, myMaxCutsFloor, myMaxCutsDivisor, myMaxCutsCeiling,
                    myMaxElementsFloor, myMaxElementsDivisor, myMaxElementsCeiling, iterations);
        }

        /**
         * Use only these cut types, attempted in this order within a round. A type listed more than once
         * keeps its first position. No arguments means no cuts at all.
         */
        public CutConfiguration withTypes(final CutType... newTypes) {
            List<CutType> list = new ArrayList<>();
            for (CutType type : newTypes) {
                if (!list.contains(type)) {
                    list.add(type);
                }
            }
            return new CutConfiguration(List.copyOf(list), dynamism, efficacy, fractionality, relaxation, violation, myMaxCutsFloor, myMaxCutsDivisor,
                    myMaxCutsCeiling, myMaxElementsFloor, myMaxElementsDivisor, myMaxElementsCeiling, iterations);
        }

        public CutConfiguration withViolation(final BigDecimal newViolation) {
            return new CutConfiguration(types, dynamism, efficacy, fractionality, relaxation, newViolation.abs(), myMaxCutsFloor, myMaxCutsDivisor,
                    myMaxCutsCeiling, myMaxElementsFloor, myMaxElementsDivisor, myMaxElementsCeiling, iterations);
        }

    }

    /**
     * The cut types the {@link IntegerSolver} can generate. Within a separation round they are attempted in
     * the order given by {@link CutConfiguration#types}.
     */
    enum CutType {

        CLIQUE("CL"), FLOW_COVER("FC"), GOMORY_MIXED_INTEGER("GM"), IMPLIED_BOUNDS("IB"), KNAPSACK_COVER("KC"), MIXED_INTEGER_ROUNDING("MR");

        /**
         * 2-character identifier, used in the names of generated cuts and in the solver's cut statistics.
         */
        public final String code;

        CutType(final String code) {
            this.code = code;
        }

    }

    ConfigurableStrategy DEFAULT = IntegerStrategy.newConfigurable();

    /**
     * The worker priorities are handed out in the order defined here, cycled over the workers. Measured on
     * the MIPLIB easy set (2026-09): a single worker must be best-bound (depth-first alone is 3 to 10 times
     * slower, and never proves bell3b), while with many workers the mix of all three is best and a
     * depth-first-heavy mix explodes the tree.
     */
    static ConfigurableStrategy newConfigurable() {

        Comparator<NodeKey>[] definitions = (Comparator<NodeKey>[]) new Comparator<?>[] { NodeKey.MIN_OBJECTIVE, NodeKey.DEPTH_FIRST_SEARCH,
                NodeKey.BREADTH_FIRST_SEARCH };

        NumberContext integrality = NumberContext.of(12, 8);
        NumberContext gap = NumberContext.of(5, 7);

        return new ConfigurableStrategy(definitions, integrality, gap, DefaultStrategy::new, new CutConfiguration());
    }

    int countUniqueStrategies();

    /**
     * Cut generation configuration, common to all cut types. Never null.
     */
    CutConfiguration getCutConfiguration();

    /**
     * The MIP gap is the difference between the best integer solution found so far and a node's relaxed
     * non-integer solution. The relative MIP gap is that difference divided by the optimal value
     * (approximated by the currently best integer solution). If the gap (absolute or relative) is too small,
     * then the corresponding branch is terminated as it is deemed unlikely or too "expensive" to find better
     * integer solutions there.
     *
     * @return The tolerance context used to determine if the gap is too small or not
     */
    NumberContext getGapTolerance();

    /**
     * Used to determine if a variable value is integer or not
     */
    NumberContext getIntegralityTolerance();

    /**
     * There will be 1 worker thread per item in the returned {@link List}. The {@link Comparator} instances
     * need not be unique. Used to prioritise among the nodes waiting to be evaluated.
     *
     * @param parallelism The number of worker threads to use
     */
    List<Comparator<NodeKey>> getWorkerPriorities(int parallelism);

    ModelStrategy newModelStrategy(final ExpressionsBasedModel model);

}
