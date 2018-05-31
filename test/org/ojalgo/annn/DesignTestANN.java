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
package org.ojalgo.annn;

import static org.ojalgo.constant.PrimitiveMath.*;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.access.Access1D;
import org.ojalgo.ann.ArtificialNeuralNetwork;
import org.ojalgo.ann.NetworkBuilder;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.type.context.NumberContext;

public class DesignTestANN extends ANNTest {

    public DesignTestANN() {
        super();
    }

    /**
     * https://mattmazur.com/2015/03/17/a-step-by-step-backpropagation-example/
     */
    @Test
    public void testMattMazurBackpropagationExample() {

        NetworkBuilder builder = new NetworkBuilder(2, 2, 2);

        builder.activator(0, ArtificialNeuralNetwork.SIGMOID).activator(1, ArtificialNeuralNetwork.SIGMOID);

        builder.weight(0, 0, 0, 0.15);
        builder.weight(0, 1, 0, 0.20);
        builder.weight(0, 0, 1, 0.25);
        builder.weight(0, 1, 1, 0.30);

        builder.weight(1, 0, 0, 0.40);
        builder.weight(1, 1, 0, 0.45);
        builder.weight(1, 0, 1, 0.50);
        builder.weight(1, 1, 1, 0.55);

        builder.bias(0, 0, 0.35);
        builder.bias(0, 1, 0.35);

        builder.bias(1, 0, 0.60);
        builder.bias(1, 1, 0.60);

        ArtificialNeuralNetwork network = builder.get();

        PrimitiveDenseStore training_input = PrimitiveDenseStore.FACTORY.rows(new double[] { 0.05, 0.10 });
        PrimitiveDenseStore training_output = PrimitiveDenseStore.FACTORY.rows(new double[] { 0.01, 0.99 });

        Access1D<Double> expected_first_network_output = PrimitiveDenseStore.FACTORY.rows(new double[] { 0.75136507, 0.772928465 });
        Access1D<Double> actual_first_network_output = network.apply(training_input);

        NumberContext precision = new NumberContext(8, 8);
        TestUtils.assertEquals(expected_first_network_output, actual_first_network_output, precision);

        double expectedError = 0.298371109;
        double actualError = HALF * (Math.pow((training_output.doubleValue(0) - actual_first_network_output.doubleValue(0)), TWO)
                + Math.pow((training_output.doubleValue(1) - actual_first_network_output.doubleValue(1)), TWO));

        TestUtils.assertEquals(expectedError, actualError, precision);
    }

}
