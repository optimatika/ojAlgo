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

import static org.ojalgo.ann.ArtificialNeuralNetwork.Activator.*;
import static org.ojalgo.ann.ArtificialNeuralNetwork.Error.*;
import static org.ojalgo.constant.PrimitiveMath.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.access.Access1D;
import org.ojalgo.ann.ArtificialNeuralNetwork.Error;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.type.context.NumberContext;

/**
 * https://stevenmiller888.github.io/mind-how-to-build-a-neural-network/
 *
 * @author apete
 */
public class HowToBuildNeuralNetwork extends BackPropagationExample {

    private static final NumberContext PRECISION = new NumberContext(4, 8);

    public HowToBuildNeuralNetwork() {
        super();
    }

    @Test
    public void testHowToBuildNeuralNetwork() {

        NumberContext precision = this.precision();
        Factory<Double, PrimitiveDenseStore> factory = PrimitiveDenseStore.FACTORY;
        Error errorMeassure = ArtificialNeuralNetwork.Error.HALF_SQUARED_DIFFERENCE;

        NetworkBuilder builder = this.getInitialNetwork();

        ArtificialNeuralNetwork network = builder.get();

        PrimitiveDenseStore givenInput = factory.rows(new double[] { 1.0, 1.0 });
        PrimitiveDenseStore targetOutput = factory.rows(new double[] { 0.0 });
        PrimitiveDenseStore expectedOutput = factory.rows(new double[] { 0.7746924929149283 });
        Access1D<Double> actualOutput = network.apply(givenInput);

        TestUtils.assertEquals(expectedOutput, actualOutput, precision);

        // The loss/error function is not explicitly defined in this example,
        // and the training procedure not fully explained (and different from
        // other's). Will only test if one traing iteration, the way ojAlgo does
        // it, decreases the error.

        double errorBeforeTraining = errorMeassure.invoke(targetOutput, actualOutput);

        builder.train(givenInput, targetOutput, ONE);

        if (DEBUG) {
            network.getWeights().forEach(w -> BasicLogger.debug("", w));
        }

        Access1D<Double> trainedOutput = network.apply(givenInput);
        double errorAfterTraining = errorMeassure.invoke(targetOutput, trainedOutput);

        TestUtils.assertTrue(errorAfterTraining < errorBeforeTraining);
    }

    @Override
    protected NumberContext precision() {
        return PRECISION;
    }

    @Override
    protected NetworkBuilder getInitialNetwork() {

        NetworkBuilder builder = new NetworkBuilder(2, 3, 1);

        builder.activator(0, SIGMOID).activator(1, SIGMOID).error(HALF_SQUARED_DIFFERENCE);

        builder.weight(0, 0, 0, 0.8);
        builder.weight(0, 0, 1, 0.4);
        builder.weight(0, 0, 2, 0.3);

        builder.weight(0, 1, 0, 0.2);
        builder.weight(0, 1, 1, 0.9);
        builder.weight(0, 1, 2, 0.5);

        builder.bias(0, 0, 0.0);
        builder.bias(0, 1, 0.0);
        builder.bias(0, 2, 0.0);

        builder.weight(1, 0, 0, 0.3);
        builder.weight(1, 1, 0, 0.5);
        builder.weight(1, 2, 0, 0.9);

        builder.bias(1, 0, 0.0);

        return builder;
    }

    @Override
    protected List<TrainingTriplet> getTriplets() {

        TrainingTriplet retVal = new TrainingTriplet(1.0);

        retVal.input(1.0, 1.0);
        retVal.target(0.0);
        retVal.expected(0.7746924929149283);

        return Collections.singletonList(retVal);
    }

}
