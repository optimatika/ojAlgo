/*
 * Copyright 1997-2022 Optimatika
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.IntSupplier;

import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.integer.ModelStrategy.DefaultStrategy;
import org.ojalgo.type.context.NumberContext;

public interface IntegerStrategy {

    final class ConfigurableStrategy implements IntegerStrategy {

        private final BiFunction<ExpressionsBasedModel, IntegerStrategy, ModelStrategy> myFactory;
        private final NumberContext myGapTolerance;
        private final IntSupplier myParallelism;
        private final Comparator<NodeKey>[] myPriorityDefinitions;
        private transient List<Comparator<NodeKey>> myWorkerPriorities = null;

        ConfigurableStrategy(final IntSupplier parallelism, final Comparator<NodeKey>[] definitions, final NumberContext gap,
                final BiFunction<ExpressionsBasedModel, IntegerStrategy, ModelStrategy> factory) {

            super();

            myParallelism = parallelism;
            myPriorityDefinitions = definitions;
            myGapTolerance = gap;
            myFactory = factory;
        }

        /**
         * Retains any existing definitions, but adds these to be used rather than the existing. If there are
         * enough threads both these additional and the previously existing definitions will be used.
         */
        public ConfigurableStrategy addPriorityDefinitions(final Comparator<NodeKey>... definitions) {

            Comparator<NodeKey>[] totalDefinitions = (Comparator<NodeKey>[]) new Comparator<?>[definitions.length + myPriorityDefinitions.length];

            for (int i = 0; i < definitions.length; i++) {
                totalDefinitions[i] = definitions[i];
            }

            for (int i = 0; i < myPriorityDefinitions.length; i++) {
                totalDefinitions[definitions.length + i] = myPriorityDefinitions[i];
            }

            return new ConfigurableStrategy(myParallelism, totalDefinitions, myGapTolerance, myFactory);
        }

        public NumberContext getGapTolerance() {
            return myGapTolerance;
        }

        public List<Comparator<NodeKey>> getWorkerPriorities() {
            if (myWorkerPriorities == null || myWorkerPriorities.size() <= 0) {
                int parallelism = myParallelism.getAsInt();
                myWorkerPriorities = new ArrayList<>(parallelism);
                for (int i = 0; i < parallelism; i++) {
                    myWorkerPriorities.add(myPriorityDefinitions[i % myPriorityDefinitions.length]);
                }
            }
            return myWorkerPriorities;
        }

        public ModelStrategy newModelStrategy(final ExpressionsBasedModel model) {
            return myFactory.apply(model, this);
        }

        /**
         * Change the MIP gap
         */
        public ConfigurableStrategy withGapTolerance(final NumberContext gapTolerance) {
            return new ConfigurableStrategy(myParallelism, myPriorityDefinitions, gapTolerance, myFactory);
        }

        /**
         * If you created a custom {@link ModelStrategy} implementation you need to provide a factory for it
         * here.
         */
        public ConfigurableStrategy withModelStrategyFactory(final BiFunction<ExpressionsBasedModel, IntegerStrategy, ModelStrategy> factory) {
            return new ConfigurableStrategy(myParallelism, myPriorityDefinitions, myGapTolerance, factory);
        }

        /**
         * How many threads will be used? Perhaps use {@link Parallelism} to obtain a suitable value.
         */
        public ConfigurableStrategy withParallelism(final IntSupplier parallelism) {
            return new ConfigurableStrategy(parallelism, myPriorityDefinitions, myGapTolerance, myFactory);
        }

        /**
         * Replace the priority definitions with these ones.
         */
        public ConfigurableStrategy withPriorityDefinitions(final Comparator<NodeKey>... definitions) {
            return new ConfigurableStrategy(myParallelism, definitions, myGapTolerance, myFactory);
        }

    }

    ConfigurableStrategy DEFAULT = IntegerStrategy.newConfigurable();

    static ConfigurableStrategy newConfigurable() {

        Comparator<NodeKey>[] definitions = (Comparator<NodeKey>[]) new Comparator<?>[] { NodeKey.EARLIEST_SEQUENCE, NodeKey.LARGEST_DISPLACEMENT,
                NodeKey.SMALLEST_DISPLACEMENT, NodeKey.LATEST_SEQUENCE };

        return new ConfigurableStrategy(Parallelism.CORES, definitions, NumberContext.of(6, 8), DefaultStrategy::new);
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

    /**
     * There will be 1 worker thread per item in the returned {@link List}. The {@link Comparator} instances
     * need not be unique. Used to prioritise among the nodes waiting to be evaluated.
     */
    List<Comparator<NodeKey>> getWorkerPriorities();

    ModelStrategy newModelStrategy(final ExpressionsBasedModel model);

}
