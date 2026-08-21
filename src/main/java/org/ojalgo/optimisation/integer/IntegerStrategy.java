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

        private final BiFunction<ExpressionsBasedModel, IntegerStrategy, ModelStrategy> myFactory;
        private final NumberContext myGapTolerance;
        private final GMICutConfiguration myGMICutConfiguration;
        private final NumberContext myIntegralityTolerance;
        private final Comparator<NodeKey>[] myPriorityDefinitions;

        ConfigurableStrategy(final Comparator<NodeKey>[] definitions, final NumberContext integrality, final NumberContext gap,
                final BiFunction<ExpressionsBasedModel, IntegerStrategy, ModelStrategy> factory, final GMICutConfiguration configuration) {

            super();

            myPriorityDefinitions = definitions;
            myIntegralityTolerance = integrality;
            myGapTolerance = gap;
            myFactory = factory;
            myGMICutConfiguration = configuration;
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

            return new ConfigurableStrategy(totalDefinitions, myIntegralityTolerance, myGapTolerance, myFactory, myGMICutConfiguration);
        }

        @Override
        public int countUniqueStrategies() {
            return myPriorityDefinitions.length;
        }

        @Override
        public NumberContext getGapTolerance() {
            return myGapTolerance;
        }

        @Override
        public GMICutConfiguration getGMICutConfiguration() {
            return myGMICutConfiguration;
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
         * Change the MIP gap
         */
        public ConfigurableStrategy withGapTolerance(final NumberContext newTolerance) {
            return new ConfigurableStrategy(myPriorityDefinitions, myIntegralityTolerance, newTolerance, myFactory, myGMICutConfiguration);
        }

        public ConfigurableStrategy withGMICutConfiguration(final GMICutConfiguration newConfiguration) {
            return new ConfigurableStrategy(myPriorityDefinitions, myIntegralityTolerance, myGapTolerance, myFactory, newConfiguration);
        }

        /**
         * Create a sub-class of {@link ModelStrategy} and provide a factory method for it here.
         */
        public ConfigurableStrategy withModelStrategyFactory(final BiFunction<ExpressionsBasedModel, IntegerStrategy, ModelStrategy> newFactory) {
            return new ConfigurableStrategy(myPriorityDefinitions, myIntegralityTolerance, myGapTolerance, newFactory, myGMICutConfiguration);
        }

        /**
         * Replace the priority definitions with these ones.
         */
        public ConfigurableStrategy withPriorityDefinitions(final Comparator<NodeKey>... newDefinitions) {
            return new ConfigurableStrategy(newDefinitions, myIntegralityTolerance, myGapTolerance, myFactory, myGMICutConfiguration);
        }

    }

    /**
     * Gomory Mixed Integer Cut Configuration
     *
     * @author apete
     */
    public static final class GMICutConfiguration {

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

        public GMICutConfiguration() {
            this(NumberContext.of(7), NumberContext.of(6), PrimitiveMath.ELEVENTH, BigMath.TWELVE, 10, 3, 100, 10, 10, 100);
        }

        private GMICutConfiguration(final NumberContext newDynanism, final NumberContext newEfficacy, final double newAway, final BigDecimal newExpansion,
                final int newMaxCutsFloor, final int newMaxCutsDivisor, final int newMaxCutsCeiling, final int newMaxElementsFloor,
                final int newMaxElementsDivisor, final int newMaxElementsCeiling) {
            super();
            dynanism = newDynanism;
            efficacy = newEfficacy;
            fractionality = newAway;
            violation = newExpansion;
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

        public GMICutConfiguration withDynanism(final NumberContext newDynanism) {
            return new GMICutConfiguration(newDynanism, efficacy, fractionality, violation, myMaxCutsFloor, myMaxCutsDivisor, myMaxCutsCeiling,
                    myMaxElementsFloor, myMaxElementsDivisor, myMaxElementsCeiling);
        }

        public GMICutConfiguration withEfficacy(final NumberContext newEfficacy) {
            return new GMICutConfiguration(dynanism, newEfficacy, fractionality, violation, myMaxCutsFloor, myMaxCutsDivisor, myMaxCutsCeiling,
                    myMaxElementsFloor, myMaxElementsDivisor, myMaxElementsCeiling);
        }

        public GMICutConfiguration withFractionality(final double newFractionality) {
            return new GMICutConfiguration(dynanism, efficacy, Math.min(Math.abs(newFractionality), 0.5), violation, myMaxCutsFloor, myMaxCutsDivisor,
                    myMaxCutsCeiling, myMaxElementsFloor, myMaxElementsDivisor, myMaxElementsCeiling);
        }

        /**
         * @param floor   minimum number of cuts accepted regardless of problem size
         * @param divisor accepted cuts scale as {@code nbVariables / divisor}
         * @param ceiling absolute maximum number of cuts accepted
         */
        public GMICutConfiguration withMaxCuts(final int floor, final int divisor, final int ceiling) {
            return new GMICutConfiguration(dynanism, efficacy, fractionality, violation, Math.max(1, floor), Math.max(1, divisor), Math.max(1, ceiling),
                    myMaxElementsFloor, myMaxElementsDivisor, myMaxElementsCeiling);
        }

        /**
         * @param floor   minimum number of non-zero coefficients allowed regardless of problem size
         * @param divisor density limit scales as {@code nbVariables / divisor}
         * @param ceiling absolute maximum number of non-zero coefficients allowed
         */
        public GMICutConfiguration withMaxElements(final int floor, final int divisor, final int ceiling) {
            return new GMICutConfiguration(dynanism, efficacy, fractionality, violation, myMaxCutsFloor, myMaxCutsDivisor, myMaxCutsCeiling, Math.max(1, floor),
                    Math.max(1, divisor), Math.max(1, ceiling));
        }

        public GMICutConfiguration withViolation(final BigDecimal newViolation) {
            return new GMICutConfiguration(dynanism, efficacy, fractionality, newViolation.abs(), myMaxCutsFloor, myMaxCutsDivisor, myMaxCutsCeiling,
                    myMaxElementsFloor, myMaxElementsDivisor, myMaxElementsCeiling);
        }

    }

    ConfigurableStrategy DEFAULT = IntegerStrategy.newConfigurable();

    static ConfigurableStrategy newConfigurable() {

        Comparator<NodeKey>[] definitions = (Comparator<NodeKey>[]) new Comparator<?>[] { NodeKey.MIN_OBJECTIVE, NodeKey.DEPTH_FIRST_SEARCH,
                NodeKey.BREADTH_FIRST_SEARCH };

        NumberContext integrality = NumberContext.of(12, 8);
        NumberContext gap = NumberContext.of(5, 7);

        return new ConfigurableStrategy(definitions, integrality, gap, DefaultStrategy::new, new GMICutConfiguration());
    }

    int countUniqueStrategies();

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

    GMICutConfiguration getGMICutConfiguration();

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
