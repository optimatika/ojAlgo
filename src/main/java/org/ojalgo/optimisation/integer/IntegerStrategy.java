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

        private final CutConfiguration myCLCutConfiguration;
        private final BiFunction<ExpressionsBasedModel, IntegerStrategy, ModelStrategy> myFactory;
        private final CutConfiguration myFCCutConfiguration;
        private final NumberContext myGapTolerance;
        private final CutConfiguration myGMICutConfiguration;
        private final CutConfiguration myIBCutConfiguration;
        private final NumberContext myIntegralityTolerance;
        private final CutConfiguration myKCCutConfiguration;
        private final CutConfiguration myMIRCutConfiguration;
        private final Comparator<NodeKey>[] myPriorityDefinitions;

        ConfigurableStrategy(final Comparator<NodeKey>[] definitions, final NumberContext integrality, final NumberContext gap,
                final BiFunction<ExpressionsBasedModel, IntegerStrategy, ModelStrategy> factory, final CutConfiguration clConfiguration,
                final CutConfiguration fcConfiguration, final CutConfiguration gmiConfiguration, final CutConfiguration ibConfiguration,
                final CutConfiguration kcConfiguration, final CutConfiguration mirConfiguration) {

            super();

            myPriorityDefinitions = definitions;
            myIntegralityTolerance = integrality;
            myGapTolerance = gap;
            myFactory = factory;
            myCLCutConfiguration = clConfiguration;
            myFCCutConfiguration = fcConfiguration;
            myGMICutConfiguration = gmiConfiguration;
            myIBCutConfiguration = ibConfiguration;
            myKCCutConfiguration = kcConfiguration;
            myMIRCutConfiguration = mirConfiguration;
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

            return new ConfigurableStrategy(totalDefinitions, myIntegralityTolerance, myGapTolerance, myFactory, myCLCutConfiguration, myFCCutConfiguration,
                    myGMICutConfiguration, myIBCutConfiguration, myKCCutConfiguration, myMIRCutConfiguration);
        }

        @Override
        public int countUniqueStrategies() {
            return myPriorityDefinitions.length;
        }

        @Override
        public CutConfiguration getCLCutConfiguration() {
            return myCLCutConfiguration;
        }

        @Override
        public CutConfiguration getFCCutConfiguration() {
            return myFCCutConfiguration;
        }

        @Override
        public NumberContext getGapTolerance() {
            return myGapTolerance;
        }

        @Override
        public CutConfiguration getGMICutConfiguration() {
            return myGMICutConfiguration;
        }

        @Override
        public CutConfiguration getIBCutConfiguration() {
            return myIBCutConfiguration;
        }

        @Override
        public NumberContext getIntegralityTolerance() {
            return myIntegralityTolerance;
        }

        @Override
        public CutConfiguration getKCCutConfiguration() {
            return myKCCutConfiguration;
        }

        @Override
        public CutConfiguration getMIRCutConfiguration() {
            return myMIRCutConfiguration;
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

        public ConfigurableStrategy withCLCutConfiguration(final CutConfiguration newConfiguration) {
            return new ConfigurableStrategy(myPriorityDefinitions, myIntegralityTolerance, myGapTolerance, myFactory, newConfiguration, myFCCutConfiguration,
                    myGMICutConfiguration, myIBCutConfiguration, myKCCutConfiguration, myMIRCutConfiguration);
        }

        public ConfigurableStrategy withFCCutConfiguration(final CutConfiguration newConfiguration) {
            return new ConfigurableStrategy(myPriorityDefinitions, myIntegralityTolerance, myGapTolerance, myFactory, myCLCutConfiguration, newConfiguration,
                    myGMICutConfiguration, myIBCutConfiguration, myKCCutConfiguration, myMIRCutConfiguration);
        }

        /**
         * Change the MIP gap
         */
        public ConfigurableStrategy withGapTolerance(final NumberContext newTolerance) {
            return new ConfigurableStrategy(myPriorityDefinitions, myIntegralityTolerance, newTolerance, myFactory, myCLCutConfiguration, myFCCutConfiguration,
                    myGMICutConfiguration, myIBCutConfiguration, myKCCutConfiguration, myMIRCutConfiguration);
        }

        public ConfigurableStrategy withGMICutConfiguration(final CutConfiguration newConfiguration) {
            return new ConfigurableStrategy(myPriorityDefinitions, myIntegralityTolerance, myGapTolerance, myFactory, myCLCutConfiguration, myFCCutConfiguration,
                    newConfiguration, myIBCutConfiguration, myKCCutConfiguration, myMIRCutConfiguration);
        }

        public ConfigurableStrategy withIBCutConfiguration(final CutConfiguration newConfiguration) {
            return new ConfigurableStrategy(myPriorityDefinitions, myIntegralityTolerance, myGapTolerance, myFactory, myCLCutConfiguration, myFCCutConfiguration,
                    myGMICutConfiguration, newConfiguration, myKCCutConfiguration, myMIRCutConfiguration);
        }

        public ConfigurableStrategy withKCCutConfiguration(final CutConfiguration newConfiguration) {
            return new ConfigurableStrategy(myPriorityDefinitions, myIntegralityTolerance, myGapTolerance, myFactory, myCLCutConfiguration, myFCCutConfiguration,
                    myGMICutConfiguration, myIBCutConfiguration, newConfiguration, myMIRCutConfiguration);
        }

        public ConfigurableStrategy withMIRCutConfiguration(final CutConfiguration newConfiguration) {
            return new ConfigurableStrategy(myPriorityDefinitions, myIntegralityTolerance, myGapTolerance, myFactory, myCLCutConfiguration, myFCCutConfiguration,
                    myGMICutConfiguration, myIBCutConfiguration, myKCCutConfiguration, newConfiguration);
        }

        /**
         * Create a sub-class of {@link ModelStrategy} and provide a factory method for it here.
         */
        public ConfigurableStrategy withModelStrategyFactory(final BiFunction<ExpressionsBasedModel, IntegerStrategy, ModelStrategy> newFactory) {
            return new ConfigurableStrategy(myPriorityDefinitions, myIntegralityTolerance, myGapTolerance, newFactory, myCLCutConfiguration,
                    myFCCutConfiguration, myGMICutConfiguration, myIBCutConfiguration, myKCCutConfiguration, myMIRCutConfiguration);
        }

        /**
         * Replace the priority definitions with these ones.
         */
        public ConfigurableStrategy withPriorityDefinitions(final Comparator<NodeKey>... newDefinitions) {
            return new ConfigurableStrategy(newDefinitions, myIntegralityTolerance, myGapTolerance, myFactory, myCLCutConfiguration, myFCCutConfiguration,
                    myGMICutConfiguration, myIBCutConfiguration, myKCCutConfiguration, myMIRCutConfiguration);
        }

    }

    /**
     * Cut Configuration (initially/primarily designed for MIR and GMI cuts, but used for any/all types).
     */
    public static final class CutConfiguration {

        public final int iterations;

        public final NumberContext dynanism;

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
         * When true, positive continuous variable coefficients are dropped from the cut (set to zero). This
         * is the Mixed-Integer Rounding (MIR) relaxation. It produces weaker but numerically more stable
         * cuts, which allows using a lower fractionality threshold.
         */
        public final boolean mirRelaxation;
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

        public CutConfiguration() {
            this(NumberContext.of(7), NumberContext.of(6), PrimitiveMath.ELEVENTH, false, BigMath.TWELVE, 10, 3, 100, 10, 10, 100, 1);
        }

        private CutConfiguration(final NumberContext newDynanism, final NumberContext newEfficacy, final double newAway, final boolean newMirRelaxation,
                final BigDecimal newExpansion, final int newMaxCutsFloor, final int newMaxCutsDivisor, final int newMaxCutsCeiling,
                final int newMaxElementsFloor, final int newMaxElementsDivisor, final int newMaxElementsCeiling, final int newIterations) {

            super();

            dynanism = newDynanism;
            efficacy = newEfficacy;
            fractionality = newAway;
            mirRelaxation = newMirRelaxation;
            violation = newExpansion;
            iterations = newIterations;

            myMaxCutsFloor = newMaxCutsFloor;
            myMaxCutsDivisor = newMaxCutsDivisor;
            myMaxCutsCeiling = newMaxCutsCeiling;

            myMaxElementsFloor = newMaxElementsFloor;
            myMaxElementsDivisor = newMaxElementsDivisor;
            myMaxElementsCeiling = newMaxElementsCeiling;
        }

        public int getMaxCuts(final int nbVariables) {
            return Math.max(myMaxCutsFloor, Math.min(nbVariables / myMaxCutsDivisor, myMaxCutsCeiling));
        }

        public int getMaxElements(final int nbVariables) {
            return Math.max(myMaxElementsFloor, Math.min(nbVariables / myMaxElementsDivisor, myMaxElementsCeiling));
        }

        public CutConfiguration withDynanism(final NumberContext newDynanism) {
            return new CutConfiguration(newDynanism, efficacy, fractionality, mirRelaxation, violation, myMaxCutsFloor, myMaxCutsDivisor, myMaxCutsCeiling,
                    myMaxElementsFloor, myMaxElementsDivisor, myMaxElementsCeiling, iterations);
        }

        public CutConfiguration withEfficacy(final NumberContext newEfficacy) {
            return new CutConfiguration(dynanism, newEfficacy, fractionality, mirRelaxation, violation, myMaxCutsFloor, myMaxCutsDivisor, myMaxCutsCeiling,
                    myMaxElementsFloor, myMaxElementsDivisor, myMaxElementsCeiling, iterations);
        }

        public CutConfiguration withFractionality(final double newFractionality) {
            return new CutConfiguration(dynanism, efficacy, Math.min(Math.abs(newFractionality), 0.5), mirRelaxation, violation, myMaxCutsFloor,
                    myMaxCutsDivisor, myMaxCutsCeiling, myMaxElementsFloor, myMaxElementsDivisor, myMaxElementsCeiling, iterations);
        }

        public CutConfiguration withIterations(final int newIterations) {
            return new CutConfiguration(dynanism, efficacy, fractionality, mirRelaxation, violation, myMaxCutsFloor, myMaxCutsDivisor, myMaxCutsCeiling,
                    myMaxElementsFloor, myMaxElementsDivisor, myMaxElementsCeiling, newIterations);
        }

        /**
         * @param floor   minimum number of cuts accepted regardless of problem size
         * @param divisor accepted cuts scale as {@code nbVariables / divisor}
         * @param ceiling absolute maximum number of cuts accepted
         */
        public CutConfiguration withMaxCuts(final int floor, final int divisor, final int ceiling) {
            return new CutConfiguration(dynanism, efficacy, fractionality, mirRelaxation, violation, Math.max(1, floor), Math.max(1, divisor),
                    Math.max(1, ceiling), myMaxElementsFloor, myMaxElementsDivisor, myMaxElementsCeiling, iterations);
        }

        /**
         * @param floor   minimum number of non-zero coefficients allowed regardless of problem size
         * @param divisor density limit scales as {@code nbVariables / divisor}
         * @param ceiling absolute maximum number of non-zero coefficients allowed
         */
        public CutConfiguration withMaxElements(final int floor, final int divisor, final int ceiling) {
            return new CutConfiguration(dynanism, efficacy, fractionality, mirRelaxation, violation, myMaxCutsFloor, myMaxCutsDivisor, myMaxCutsCeiling,
                    Math.max(1, floor), Math.max(1, divisor), Math.max(1, ceiling), iterations);
        }

        public CutConfiguration withMirRelaxation(final boolean newMirRelaxation) {
            return new CutConfiguration(dynanism, efficacy, fractionality, newMirRelaxation, violation, myMaxCutsFloor, myMaxCutsDivisor, myMaxCutsCeiling,
                    myMaxElementsFloor, myMaxElementsDivisor, myMaxElementsCeiling, iterations);
        }

        public CutConfiguration withViolation(final BigDecimal newViolation) {
            return new CutConfiguration(dynanism, efficacy, fractionality, mirRelaxation, newViolation.abs(), myMaxCutsFloor, myMaxCutsDivisor,
                    myMaxCutsCeiling, myMaxElementsFloor, myMaxElementsDivisor, myMaxElementsCeiling, iterations);
        }

    }

    ConfigurableStrategy DEFAULT = IntegerStrategy.newConfigurable();

    static ConfigurableStrategy newConfigurable() {

        Comparator<NodeKey>[] definitions = (Comparator<NodeKey>[]) new Comparator<?>[] { NodeKey.MIN_OBJECTIVE, NodeKey.DEPTH_FIRST_SEARCH,
                NodeKey.BREADTH_FIRST_SEARCH };

        NumberContext integrality = NumberContext.of(12, 8);
        NumberContext gap = NumberContext.of(5, 7);

        return new ConfigurableStrategy(definitions, integrality, gap, DefaultStrategy::new, CliqueSeparator.CONFIGURATION, FlowCoverSeparator.CONFIGURATION,
                GMISeparator.CONFIGURATION, ImpliedBoundsSeparator.CONFIGURATION, KnapsackCoverSeparator.CONFIGURATION, MIRSeparator.CONFIGURATION);
    }

    int countUniqueStrategies();

    /**
     * Clique cut configuration
     */
    CutConfiguration getCLCutConfiguration();

    /**
     * Flow Cover cut configuration
     */
    CutConfiguration getFCCutConfiguration();

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
     * Gomory Mixed Integer cut configuration
     */
    CutConfiguration getGMICutConfiguration();

    /**
     * Implied Bounds cut configuration
     */
    CutConfiguration getIBCutConfiguration();

    /**
     * Used to determine if a variable value is integer or not
     */
    NumberContext getIntegralityTolerance();

    /**
     * Knapsack Cover cut configuration
     */
    CutConfiguration getKCCutConfiguration();

    /**
     * Mixed Integer Rounding cut configuration
     */
    CutConfiguration getMIRCutConfiguration();

    /**
     * There will be 1 worker thread per item in the returned {@link List}. The {@link Comparator} instances
     * need not be unique. Used to prioritise among the nodes waiting to be evaluated.
     *
     * @param parallelism The number of worker threads to use
     */
    List<Comparator<NodeKey>> getWorkerPriorities(int parallelism);

    ModelStrategy newModelStrategy(final ExpressionsBasedModel model);

}
