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

import static org.ojalgo.constant.PrimitiveMath.*;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.access.Access1D;
import org.ojalgo.ann.ArtificialNeuralNetwork.Error;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
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

        NumberContext precision = new NumberContext(8, 8);
        Factory<Double, PrimitiveDenseStore> factory = PrimitiveDenseStore.FACTORY;
        Error errorMeassure = ArtificialNeuralNetwork.Error.HALF_SQUARED_DIFFERENCE;

        NetworkBuilder builder = new NetworkBuilder(2, 2, 2);

        builder.activator(0, ArtificialNeuralNetwork.Activator.SIGMOID).activator(1, ArtificialNeuralNetwork.Activator.SIGMOID).error(errorMeassure);

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

        PrimitiveDenseStore training_input = factory.rows(new double[] { 0.05, 0.10 });
        PrimitiveDenseStore training_output = factory.rows(new double[] { 0.01, 0.99 });

        Access1D<Double> expected_first_network_output = factory.rows(new double[] { 0.75136507, 0.772928465 });
        Access1D<Double> actual_first_network_output = network.apply(training_input);

        TestUtils.assertEquals(expected_first_network_output, actual_first_network_output, precision);

        double expectedError1 = 0.298371109;
        double actualError1 = errorMeassure.invoke(training_output, actual_first_network_output);

        TestUtils.assertEquals(expectedError1, actualError1, precision);

        builder.train(training_input, training_output, HALF);

        // 0.40 w5
        TestUtils.assertEquals(0.35891648, network.getWeight(1, 0, 0), precision);
        // 0.45 w6
        TestUtils.assertEquals(0.408666186, network.getWeight(1, 1, 0), precision);
        // 0.50 w7
        TestUtils.assertEquals(0.511301270, network.getWeight(1, 0, 1), precision);
        // 0.55 w8
        TestUtils.assertEquals(0.561370121, network.getWeight(1, 1, 1), precision);

        // 0.15 w1
        TestUtils.assertEquals(0.149780716, network.getWeight(0, 0, 0), precision);
        // 0.20 w2
        TestUtils.assertEquals(0.19956143, network.getWeight(0, 1, 0), precision);
        // 0.25 w3
        TestUtils.assertEquals(0.24975114, network.getWeight(0, 0, 1), precision);
        // 0.30 w4
        TestUtils.assertEquals(0.29950229, network.getWeight(0, 1, 1), precision);

        double expectedError2 = 0.291027924;
        double actualError2 = errorMeassure.invoke(training_output, network.apply(training_input));

        // In the example the bias are not updated, ojAlgo does update them
        // This gives better/faster learning in this first step
        TestUtils.assertTrue(actualError1 > actualError2);
        TestUtils.assertTrue(expectedError2 > actualError2);

        // Create a lerger, more complex network, to make sure there are no IndexOutOfRangeExceptions or similar..
        NetworkBuilder largerBuilder = new NetworkBuilder(2, 5, 3, 4, 2).randomise();
        ArtificialNeuralNetwork largerANN = largerBuilder.get();

        Access1D<Double> preLarger = factory.rows(largerANN.apply(training_input));
        largerBuilder.train(training_input, training_output, HALF);
        Access1D<Double> postLarger = factory.rows(largerANN.apply(training_input));

        // Even in this case training should reduce the error
        TestUtils.assertTrue(errorMeassure.invoke(training_output, preLarger) > errorMeassure.invoke(training_output, postLarger));
    }

}
