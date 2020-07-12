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
package org.ojalgo.ann;

import static org.ojalgo.ann.ArtificialNeuralNetwork.Activator.*;
import static org.ojalgo.ann.ArtificialNeuralNetwork.Error.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.context.NumberContext;

/**
 * NeuralNetworksDemystified
 *
 * @author apete
 */
public class NeuralNetworksDemystified extends BackPropagationExample {

    private static final double LEARNING_RATE = 0.1;
    private static final NumberContext PRECISION = new NumberContext(8, 8);
    private static final int TRAINING_ITERATIONS = 100;

    public NeuralNetworksDemystified() {
        super();
    }

    @Override
    public void doTestFeedForward(Factory<Double, ?> factory) {
        // No example output

    }

    /**
     * Test that trainning on multiple different data point improve the results on all examples.
     */
    @Test
    public void testTraining() {

        final NetworkBuilder builder = this.getInitialNetwork(Primitive64Store.FACTORY);

        List<Data> examples = this.getTestCases();

        double[] initialErrors = this.getErrors(builder);

        for (int iter = 0; iter < TRAINING_ITERATIONS; iter++) {
            for (Data data : examples) {
                builder.rate(LEARNING_RATE).train(data.input, data.target);
            }
        }

        double[] trainedErrors = this.getErrors(builder);

        double initialError = 0.0, trainedError = 0.0;
        for (int i = 0; i < trainedErrors.length; i++) {
            initialError = PrimitiveMath.HYPOT.invoke(initialError, initialErrors[i]);
            trainedError = PrimitiveMath.HYPOT.invoke(trainedError, trainedErrors[i]);
        }

        TestUtils.assertTrue(initialError >= trainedError);
    }

    private double[] getErrors(final NetworkBuilder builder) {

        ArtificialNeuralNetwork network = builder.get();

        List<Data> examples = this.getTestCases();

        double[] errors = new double[examples.size()];

        for (int i = 0; i < errors.length; i++) {
            final Data data = examples.get(i);
            final Primitive64Store input = data.input;
            final Access1D<Double> actual = network.invoke(input);
            final Primitive64Store expected = data.target;
            double error = builder.error(expected, actual);
            errors[i] = error;
        }

        return errors;
    }

    @Override
    protected NetworkBuilder getInitialNetwork(Factory<Double, ?> factory) {
        return ArtificialNeuralNetwork.builder(factory, 2, 3, 1).activators(SIGMOID, SIGMOID).error(HALF_SQUARED_DIFFERENCE);

    }

    @Override
    protected List<Data> getTestCases() {

        List<Data> retVal = new ArrayList<>();

        // Normalised input on 10 and target/output on 100
        retVal.add(new Data(LEARNING_RATE).input(0.3, 0.5).target(0.75));
        retVal.add(new Data(LEARNING_RATE).input(0.5, 0.1).target(0.82));
        retVal.add(new Data(LEARNING_RATE).input(1.0, 0.2).target(0.93));

        return retVal;
    }

    @Override
    protected NumberContext precision() {
        return PRECISION;
    }

}
