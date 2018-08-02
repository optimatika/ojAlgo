/*
 * Copyright 1997-2018 Optimatika
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
import org.ojalgo.access.Structure2D;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.type.context.NumberContext;

abstract class BackPropagationExample extends ANNTest {

    static final class Data {

        PrimitiveDenseStore expected = null;
        PrimitiveDenseStore input = null;
        final double rate;
        PrimitiveDenseStore target = null;

        Data() {
            this(1.0);
        }

        Data(double rate) {
            super();
            this.rate = rate;
        }

        Data expected(double... row) {
            expected = PrimitiveDenseStore.FACTORY.rows(row);
            return this;
        }

        Data input(double... row) {
            input = PrimitiveDenseStore.FACTORY.rows(row);
            return this;
        }

        Data target(double... row) {
            target = PrimitiveDenseStore.FACTORY.rows(row);
            return this;
        }

    }

    private static final String TEST_DID_NOT_DO_ANYTHING = "Test didn't do anything!";

    public BackPropagationExample() {
        super();
    }

    @Test
    public void testFeedForward() {

        int counter = 0;

        ArtificialNeuralNetwork network = this.getInitialNetwork().get();

        for (Data triplet : this.getTestCases()) {
            if ((triplet.input != null) && (triplet.expected != null)) {
                TestUtils.assertEquals(triplet.expected, network.invoke(triplet.input), this.precision());
                counter++;
            }
        }

        if (counter == 0) {
            TestUtils.fail(TEST_DID_NOT_DO_ANYTHING);
        }
    }

    @Test
    public void testBackpropagation() {

        int counter = 0;

        for (Data triplet : this.getTestCases()) {
            if ((triplet.input != null) && (triplet.target != null)) {
                this.deriveTheHardWay(this.getInitialNetwork(), triplet, this.precision());
                counter++;
            }
        }

        if (counter == 0) {
            TestUtils.fail(TEST_DID_NOT_DO_ANYTHING);
        }
    }

    protected void deriveTheHardWay(NetworkBuilder builder, Data triplet, NumberContext precision) {

        if (DEBUG) {
            BasicLogger.debug("Weights before training");
            final AtomicInteger layer = new AtomicInteger();
            builder.getWeights().forEach(l -> {
                BasicLogger.debug(layer.toString(), l);
                layer.incrementAndGet();
            });
        }

        double delta = 0.0001;

        Structure2D[] structure = builder.structure();

        PrimitiveDenseStore[] weights = new PrimitiveDenseStore[structure.length];
        PrimitiveDenseStore[] bias = new PrimitiveDenseStore[structure.length];

        for (int layer = 0, limit = structure.length; layer < limit; layer++) {

            PrimitiveDenseStore newWeights = weights[layer] = PrimitiveDenseStore.FACTORY.makeZero(structure[layer]);
            PrimitiveDenseStore newBias = bias[layer] = PrimitiveDenseStore.FACTORY.makeZero(1, structure[layer].countColumns());

            for (int output = 0; output < structure[layer].countColumns(); output++) {
                for (int input = 0; input < structure[layer].countRows(); input++) {

                    double orgWeight = builder.getWeight(layer, input, output);

                    builder.weight(layer, input, output, orgWeight + delta);

                    double upperError = builder.error(triplet.target, builder.get().invoke(triplet.input));
                    builder.weight(layer, input, output, orgWeight - delta);
                    double lowerError = builder.error(triplet.target, builder.get().invoke(triplet.input));
                    builder.weight(layer, input, output, orgWeight);

                    final double derivative = (upperError - lowerError) / (delta + delta);
                    newWeights.set(input, output, orgWeight - (triplet.rate * derivative));
                }

                double orgBias = builder.getBias(layer, output);

                builder.bias(layer, output, orgBias + delta);
                double upperError = builder.error(triplet.target, builder.get().invoke(triplet.input));
                builder.bias(layer, output, orgBias - delta);
                double lowerError = builder.error(triplet.target, builder.get().invoke(triplet.input));
                builder.bias(layer, output, orgBias);

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
            builder.getWeights().forEach(l -> {
                BasicLogger.debug(layer.toString(), l);
                layer.incrementAndGet();
            });
        }

        builder.rate(triplet.rate).train(triplet.input, triplet.target);

        if (DEBUG) {
            BasicLogger.debug("");
            BasicLogger.debug("Weights after training");
            final AtomicInteger layer = new AtomicInteger();
            builder.getWeights().forEach(l -> {
                BasicLogger.debug(layer.toString(), l);
                layer.incrementAndGet();
            });
        }

        for (int layer = 0, limit = structure.length; layer < limit; layer++) {
            for (int output = 0; output < structure[layer].countColumns(); output++) {
                for (int input = 0; input < structure[layer].countRows(); input++) {
                    final PrimitiveDenseStore expectedWeights = weights[layer];
                    TestUtils.assertEquals(builder.toString(), expectedWeights.doubleValue(input, output), builder.getWeight(layer, input, output), precision);
                }
                TestUtils.assertEquals(builder.toString(), bias[layer].doubleValue(output), builder.getBias(layer, output), precision);
            }
        }
    }

    protected abstract NetworkBuilder getInitialNetwork();

    protected abstract List<Data> getTestCases();

    protected abstract NumberContext precision();

}
