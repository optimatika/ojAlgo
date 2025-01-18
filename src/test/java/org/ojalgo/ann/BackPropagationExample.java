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
package org.ojalgo.ann;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.R032Store;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.context.NumberContext;

abstract class BackPropagationExample extends ANNTest {

    static final class Data {

        R064Store expected = null;
        R064Store input = null;
        final double rate;
        R064Store target = null;

        Data() {
            this(1.0);
        }

        Data(final double learningRate) {
            super();
            rate = learningRate;
        }

        Data expected(final double... row) {
            expected = R064Store.FACTORY.row(row);
            return this;
        }

        Data input(final double... row) {
            input = R064Store.FACTORY.row(row);
            return this;
        }

        Data target(final double... row) {
            target = R064Store.FACTORY.row(row);
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
            if (triplet.input != null && triplet.target != null) {
                this.deriveTheHardWay(this.getInitialNetwork(R064Store.FACTORY), triplet, this.precision());
                counter++;
            }
        }

        if (counter == 0) {
            TestUtils.fail(TEST_DID_NOT_DO_ANYTHING);
        }
    }

    @Test
    public void testFeedForwardPrimitive32() {
        this.doTestFeedForward(R032Store.FACTORY);
    }

    @Test
    public void testFeedForwardPrimitive64() {
        this.doTestFeedForward(R064Store.FACTORY);
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
                BasicLogger.debugMatrix(layer.toString(), l);
                layer.incrementAndGet();
            });
        }

        double delta = 0.0001;

        Structure2D[] structure = trainer.structure();

        R064Store[] weights = new R064Store[structure.length];
        R064Store[] bias = new R064Store[structure.length];

        for (int layer = 0, limit = structure.length; layer < limit; layer++) {

            R064Store newWeights = weights[layer] = R064Store.FACTORY.make(structure[layer]);
            R064Store newBias = bias[layer] = R064Store.FACTORY.make(1, structure[layer].countColumns());

            for (int output = 0; output < structure[layer].countColumns(); output++) {
                for (int input = 0; input < structure[layer].countRows(); input++) {

                    double orgWeight = trainer.getWeight(layer, input, output);

                    trainer.weight(layer, input, output, orgWeight + delta);
                    double upperError = trainer.error(triplet.target, invoker.invoke(triplet.input));

                    trainer.weight(layer, input, output, orgWeight - delta);
                    double lowerError = trainer.error(triplet.target, invoker.invoke(triplet.input));

                    trainer.weight(layer, input, output, orgWeight);

                    final double derivative = (upperError - lowerError) / (delta + delta);
                    newWeights.set(input, output, orgWeight - triplet.rate * derivative);
                }

                double orgBias = trainer.getBias(layer, output);

                trainer.bias(layer, output, orgBias + delta);
                double upperError = trainer.error(triplet.target, invoker.invoke(triplet.input));
                trainer.bias(layer, output, orgBias - delta);
                double lowerError = trainer.error(triplet.target, invoker.invoke(triplet.input));
                trainer.bias(layer, output, orgBias);

                final double derivative = (upperError - lowerError) / (delta + delta);
                newBias.set(output, orgBias - triplet.rate * derivative);
            }

            if (DEBUG) {
                BasicLogger.debug("");
                BasicLogger.debug("Calculated for layer " + layer);
                BasicLogger.debugMatrix("Weighs", newWeights);
                BasicLogger.debugMatrix("Bias", newBias);
            }
        }

        if (DEBUG) {
            BasicLogger.debug("");
            BasicLogger.debug("Weights reset/before training");
            final AtomicInteger layer = new AtomicInteger();
            trainer.getWeights().forEach(l -> {
                BasicLogger.debugMatrix(layer.toString(), l);
                layer.incrementAndGet();
            });
        }

        trainer.rate(triplet.rate).train(triplet.input, triplet.target);

        if (DEBUG) {
            BasicLogger.debug("");
            BasicLogger.debug("Weights after training");
            final AtomicInteger layer = new AtomicInteger();
            trainer.getWeights().forEach(l -> {
                BasicLogger.debugMatrix(layer.toString(), l);
                layer.incrementAndGet();
            });
        }

        for (int layer = 0, limit = structure.length; layer < limit; layer++) {
            for (int output = 0; output < structure[layer].countColumns(); output++) {
                for (int input = 0; input < structure[layer].countRows(); input++) {
                    final R064Store expectedWeights = weights[layer];
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

            R064Store input = triplet.input;
            R064Store expected = triplet.expected;

            PhysicalStore<Double> i = null;
            PhysicalStore<Double> e = null;
            MatrixStore<Double> a = null;

            if (input != null && expected != null) {

                i = R064Store.FACTORY.row(input);
                e = R064Store.FACTORY.row(expected);
                a = R064Store.FACTORY.row(invoker.invoke(i));
                TestUtils.assertEquals(e, a, this.precision());

                i = R032Store.FACTORY.row(input);
                e = R032Store.FACTORY.row(expected);
                a = R032Store.FACTORY.row(invoker.invoke(i));
                TestUtils.assertEquals(e, a, this.precision());

                i = RawStore.FACTORY.row(input);
                e = RawStore.FACTORY.row(expected);
                a = RawStore.FACTORY.row(invoker.invoke(i));
                TestUtils.assertEquals(e, a, this.precision());

                i = R064Store.FACTORY.column(input);
                e = R064Store.FACTORY.column(expected);
                a = R064Store.FACTORY.column(invoker.invoke(i));
                TestUtils.assertEquals(e, a, this.precision());

                i = R032Store.FACTORY.column(input);
                e = R032Store.FACTORY.column(expected);
                a = R032Store.FACTORY.column(invoker.invoke(i));
                TestUtils.assertEquals(e, a, this.precision());

                i = RawStore.FACTORY.column(input);
                e = RawStore.FACTORY.column(expected);
                a = RawStore.FACTORY.column(invoker.invoke(i));
                TestUtils.assertEquals(e, a, this.precision());

                counter++;
            }
        }

        if (counter == 0) {
            TestUtils.fail(TEST_DID_NOT_DO_ANYTHING);
        }
    }

}
