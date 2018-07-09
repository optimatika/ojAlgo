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

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class SimpleDerivativesTest extends BackPropagationExample {

    private static final NumberContext PRECISION = new NumberContext(6, 8);

    public SimpleDerivativesTest() {
        super();
    }

    @Test
    public void testDerivatives() {

        for (ArtificialNeuralNetwork.Activator activator : ArtificialNeuralNetwork.Activator.values()) {

            if (activator == SOFTMAX) {
                // this.doTest(activator, ArtificialNeuralNetwork.Error.CROSS_ENTROPY);
            } else {
                this.doTest(activator, ArtificialNeuralNetwork.Error.HALF_SQUARED_DIFFERENCE);
            }

        }
    }

    private void doTest(ArtificialNeuralNetwork.Activator activator, ArtificialNeuralNetwork.Error error) {

        for (TrainingTriplet triplet : this.getTriplets()) {

            NetworkBuilder builder = this.getInitialNetwork();

            builder.activator(0, activator).error(error);

            this.deriveTheHardWay(builder, triplet, this.precision());
        }
    }

    @Override
    protected NumberContext precision() {
        return PRECISION;
    }

    @Override
    protected NetworkBuilder getInitialNetwork() {

        NetworkBuilder builder = new NetworkBuilder(3, 3);

        builder.activator(0, SIGMOID).error(HALF_SQUARED_DIFFERENCE).randomise();

        return builder;
    }

    @Override
    protected List<TrainingTriplet> getTriplets() {

        TrainingTriplet retVal = new TrainingTriplet(1.0);

        retVal.input(0.5, 0.5, 0.5);
        retVal.target(0.5, 0.5, 0.5);

        return Collections.singletonList(retVal);
    }

}
