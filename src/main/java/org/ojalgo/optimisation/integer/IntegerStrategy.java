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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.IntSupplier;

import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.integer.ModelStrategy.DefaultStrategy;
import org.ojalgo.type.context.NumberContext;

public interface IntegerStrategy {

    final class ConfigurableStrategy implements IntegerStrategy {

        private final BiFunction<ExpressionsBasedModel, IntegerStrategy, ModelStrategy> myFactory;
        private final NumberContext myGapTolerance;
        private final GMICutConfiguration myGMICutConfiguration;
        private final NumberContext myIntegralityTolerance;
        private final IntSupplier myParallelism;
        private final Comparator<NodeKey>[] myPriorityDefinitions;

        ConfigurableStrategy(final IntSupplier parallelism, final Comparator<NodeKey>[] definitions, final NumberContext integrality, final NumberContext gap,
                final BiFunction<ExpressionsBasedModel, IntegerStrategy, ModelStrategy> factory, final GMICutConfiguration configuration) {

            super();

            myParallelism = parallelism;
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

            return new ConfigurableStrategy(myParallelism, totalDefinitions, myIntegralityTolerance, myGapTolerance, myFactory, myGMICutConfiguration);
        }

        public NumberContext getGapTolerance() {
            return myGapTolerance;
        }

        public GMICutConfiguration getGMICutConfiguration() {
            return myGMICutConfiguration;
        }

        public NumberContext getIntegralityTolerance() {
            return myIntegralityTolerance;
        }

        public List<Comparator<NodeKey>> getWorkerPriorities() {
            int parallelism = myParallelism.getAsInt();
            List<Comparator<NodeKey>> retVal = new ArrayList<>(parallelism);
            for (int i = 0; i < parallelism; i++) {
                retVal.add(myPriorityDefinitions[i % myPriorityDefinitions.length]);
            }
            return retVal;
        }

        public ModelStrategy newModelStrategy(final ExpressionsBasedModel model) {
            return myFactory.apply(model, this);
        }

        /**
         * Change the MIP gap
         */
        public ConfigurableStrategy withGapTolerance(final NumberContext newTolerance) {
            return new ConfigurableStrategy(myParallelism, myPriorityDefinitions, myIntegralityTolerance, newTolerance, myFactory, myGMICutConfiguration);
        }

        public ConfigurableStrategy withGMICutConfiguration(final GMICutConfiguration newConfiguration) {
            return new ConfigurableStrategy(myParallelism, myPriorityDefinitions, myIntegralityTolerance, myGapTolerance, myFactory, newConfiguration);
        }

        public ConfigurableStrategy withModelStrategyFactory(final BiFunction<ExpressionsBasedModel, IntegerStrategy, ModelStrategy> newFactory) {
            return new ConfigurableStrategy(myParallelism, myPriorityDefinitions, myIntegralityTolerance, myGapTolerance, newFactory, myGMICutConfiguration);
        }

        /**
         * How many threads will be used? Perhaps use {@link Parallelism} to obtain a suitable value.
         */
        public ConfigurableStrategy withParallelism(final IntSupplier newParallelism) {
            return new ConfigurableStrategy(newParallelism, myPriorityDefinitions, myIntegralityTolerance, myGapTolerance, myFactory, myGMICutConfiguration);
        }

        /**
         * Replace the priority definitions with these ones.
         */
        public ConfigurableStrategy withPriorityDefinitions(final Comparator<NodeKey>... newDefinitions) {
            return new ConfigurableStrategy(myParallelism, newDefinitions, myIntegralityTolerance, myGapTolerance, myFactory, myGMICutConfiguration);
        }

    }

    /**
     * Gomory Mixed Integer Cut Configuration
     *
     * @author apete
     */
    public static final class GMICutConfiguration {

        /**
         * The minimum fractionality of the integer variable used to generate the cut. Less than this, and the
         * (potential) cut is never generated.
         */
        public final double fractionality;
        /**
         * After the cut is generated it is transformed to be expresssed in the original model variables. In
         * this process the RHS of the cut inequality changes. This parameter controls how much the RHS is
         * allowed to grow in magnitude. If it grows/expands to much the cut is discarded.
         * <p>
         * The cut/constraint violation is always exactly 1 (due to how the cut is generated). That means the
         * magnitude of the RHS becomes a meassure of the relative cut violation. Allowing large RHS values is
         * equivalent to accepting small relative cut violations. The number you specify here is the inverse
         * of the relative cut violation (the absolute value of the max RHS allowed).
         */
        public final BigDecimal violation;

        public GMICutConfiguration() {
            this(PrimitiveMath.ELEVENTH, BigMath.TWELVE);
        }

        private GMICutConfiguration(final double newAway, final BigDecimal newExpansion) {
            super();
            fractionality = newAway;
            violation = newExpansion;
        }

        public GMICutConfiguration withFractionality(final double newFractionality) {
            return new GMICutConfiguration(Math.min(Math.abs(newFractionality), 0.5), violation);
        }

        public GMICutConfiguration withViolation(final BigDecimal newViolation) {
            return new GMICutConfiguration(fractionality, newViolation.abs());
        }

    }

    ConfigurableStrategy DEFAULT = IntegerStrategy.newConfigurable();

    static ConfigurableStrategy newConfigurable() {

        Comparator<NodeKey>[] definitions = (Comparator<NodeKey>[]) new Comparator<?>[] { NodeKey.EARLIEST_SEQUENCE, NodeKey.LARGEST_DISPLACEMENT,
                NodeKey.SMALLEST_DISPLACEMENT, NodeKey.LATEST_SEQUENCE };

        NumberContext integrality = NumberContext.of(12, 8);
        NumberContext gap = NumberContext.of(7, 8);

        return new ConfigurableStrategy(Parallelism.CORES.require(4), definitions, integrality, gap, DefaultStrategy::new, new GMICutConfiguration());
    }

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
     */
    List<Comparator<NodeKey>> getWorkerPriorities();

    ModelStrategy newModelStrategy(final ExpressionsBasedModel model);

}
