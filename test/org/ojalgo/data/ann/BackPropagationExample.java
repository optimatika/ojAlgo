/*
 * Copyright 1997-2020 Optimatika
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
package org.ojalgo.data.ann;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.ojalgo.core.TestUtils;
import org.ojalgo.core.netio.BasicLogger;
import org.ojalgo.core.structure.Structure2D;
import org.ojalgo.core.type.context.NumberContext;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.Primitive32Store;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.store.RawStore;

abstract class BackPropagationExample extends ANNTest {

    static final class Data {

        Primitive64Store expected = null;
        Primitive64Store input = null;
        final double rate;
        Primitive64Store target = null;

        Data() {
            this(1.0);
        }

        Data(final double rate) {
            super();
            this.rate = rate;
        }

        Data expected(final double... row) {
            expected = Primitive64Store.FACTORY.rows(row);
            return this;
        }

        Data input(final double... row) {
            input = Primitive64Store.FACTORY.rows(row);
            return this;
        }

        Data target(final double... row) {
            target = Primitive64Store.FACTORY.rows(row);
            return this;
        }

    }

    private static final String TEST_DID_NOT_DO_ANYTHING = "Test didn't do anything!";

    public BackPropagationExample() {
        super();
    }

    @Test
    public void testBackpropagation() {

        int counter = 0;

        for (Data triplet : this.getTestCases()) {
            if ((triplet.input != null) && (triplet.target != null)) {
                this.deriveTheHardWay(this.getInitialNetwork(Primitive64Store.FACTORY), triplet, this.precision());
                counter++;
            }
        }

        if (counter == 0) {
            TestUtils.fail(TEST_DID_NOT_DO_ANYTHING);
        }
    }

    @Test
    public void testFeedForwardPrimitive32() {
        this.doTestFeedForward(Primitive32Store.FACTORY);
    }

    @Test
    public void testFeedForwardPrimitive64() {
        this.doTestFeedForward(Primitive64Store.FACTORY);
    }

    @Test
    public void testFeedForwardRaw() {
        this.doTestFeedForward(RawStore.FACTORY);
    }

    protected void deriveTheHardWay(final ArtificialNeuralNetwork network, final Data triplet, final NumberContext precision) {

        NetworkTrainer trainer = network.newTrainer();
        NetworkInvoker invoker = network.newInvoker();

        if (DEBUG) {
            BasicLogger.debug("Weights before training");
            final AtomicInteger layer = new AtomicInteger();
            trainer.getWeights().forEach(l -> {
                BasicLogger.debug(layer.toString(), l);
                layer.incrementAndGet();
            });
        }

        double delta = 0.0001;

        Structure2D[] structure = trainer.structure();

        Primitive64Store[] weights = new Primitive64Store[structure.length];
        Primitive64Store[] bias = new Primitive64Store[structure.length];

        for (int layer = 0, limit = structure.length; layer < limit; layer++) {

            Primitive64Store newWeights = weights[layer] = Primitive64Store.FACTORY.make(structure[layer]);
            Primitive64Store newBias = bias[layer] = Primitive64Store.FACTORY.make(1, structure[layer].countColumns());

            for (int output = 0; output < structure[layer].countColumns(); output++) {
                for (int input = 0; input < structure[layer].countRows(); input++) {

                    double orgWeight = trainer.getWeight(layer, input, output);

                    trainer.weight(layer, input, output, orgWeight + delta);
                    double upperError = trainer.error(triplet.target, invoker.invoke(triplet.input));

                    trainer.weight(layer, input, output, orgWeight - delta);
                    double lowerError = trainer.error(triplet.target, invoker.invoke(triplet.input));

                    trainer.weight(layer, input, output, orgWeight);

                    final double derivative = (upperError - lowerError) / (delta + delta);
                    newWeights.set(input, output, orgWeight - (triplet.rate * derivative));
                }

                double orgBias = trainer.getBias(layer, output);

                trainer.bias(layer, output, orgBias + delta);
                double upperError = trainer.error(triplet.target, invoker.invoke(triplet.input));
                trainer.bias(layer, output, orgBias - delta);
                double lowerError = trainer.error(triplet.target, invoker.invoke(triplet.input));
                trainer.bias(layer, output, orgBias);

                final double derivative = (upperError - lowerError) / (delta + delta);
                newBias.set(output, orgBias - (triplet.rate * derivative));
            }

            if (DEBUG) {
                BasicLogger.debug("");
                BasicLogger.debug("Calculated for layer " + layer);
                BasicLogger.debug("Weighs", newWeights);
                BasicLogger.debug("Bias", newBias);
            }
        }

        if (DEBUG) {
            BasicLogger.debug("");
            BasicLogger.debug("Weights reset/before training");
            final AtomicInteger layer = new AtomicInteger();
            trainer.getWeights().forEach(l -> {
                BasicLogger.debug(layer.toString(), l);
                layer.incrementAndGet();
            });
        }

        trainer.rate(triplet.rate).train(triplet.input, triplet.target);

        if (DEBUG) {
            BasicLogger.debug("");
            BasicLogger.debug("Weights after training");
            final AtomicInteger layer = new AtomicInteger();
            trainer.getWeights().forEach(l -> {
                BasicLogger.debug(layer.toString(), l);
                layer.incrementAndGet();
            });
        }

        for (int layer = 0, limit = structure.length; layer < limit; layer++) {
            for (int output = 0; output < structure[layer].countColumns(); output++) {
                for (int input = 0; input < structure[layer].countRows(); input++) {
                    final Primitive64Store expectedWeights = weights[layer];
                    TestUtils.assertEquals(trainer.toString(), expectedWeights.doubleValue(input, output), trainer.getWeight(layer, input, output), precision);
                }
                TestUtils.assertEquals(trainer.toString(), bias[layer].doubleValue(output), trainer.getBias(layer, output), precision);
            }
        }
    }

    protected abstract ArtificialNeuralNetwork getInitialNetwork(Factory<Double, ?> factory);

    protected abstract List<Data> getTestCases();

    protected abstract NumberContext precision();

    void doTestFeedForward(final Factory<Double, ?> factory) {

        int counter = 0;

        ArtificialNeuralNetwork network = this.getInitialNetwork(factory);

        NetworkInvoker invoker = network.newInvoker();

        for (Data triplet : this.getTestCases()) {
            if ((triplet.input != null) && (triplet.expected != null)) {
                TestUtils.assertEquals(triplet.expected, invoker.invoke(triplet.input), this.precision());
                counter++;
            }
        }

        if (counter == 0) {
            TestUtils.fail(TEST_DID_NOT_DO_ANYTHING);
        }
    }

}
