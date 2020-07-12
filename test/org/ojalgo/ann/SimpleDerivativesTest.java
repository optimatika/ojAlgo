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

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.Primitive64Store;
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
                this.doTestDerivatives(activator, ArtificialNeuralNetwork.Error.CROSS_ENTROPY);
            } else {
                this.doTestDerivatives(activator, ArtificialNeuralNetwork.Error.HALF_SQUARED_DIFFERENCE);
            }
        }
    }

    @Override
    public void doTestFeedForward(Factory<Double, ?> factory) {
        // Not possible to test for this case

    }

    private void doTestDerivatives(final ArtificialNeuralNetwork.Activator activator, final ArtificialNeuralNetwork.Error error) {

        for (Data triplet : this.getTestCases()) {

            NetworkBuilder builder = this.getInitialNetwork(Primitive64Store.FACTORY).activators(activator).error(error);

            this.deriveTheHardWay(builder, triplet, this.precision());
        }
    }

    @Override
    protected NetworkBuilder getInitialNetwork(Factory<Double, ?> factory) {
        return ArtificialNeuralNetwork.builder(factory, 3, 3);
    }

    @Override
    protected List<Data> getTestCases() {

        Data retVal = new Data();

        retVal.input(0.5, 0.5, 0.5);
        retVal.target(0.0, 1.0, 0.0); // Single 1.0 to be compatible with SOFTMAX/CROSS-ENTROPY

        return Collections.singletonList(retVal);
    }

    @Override
    protected NumberContext precision() {
        return PRECISION;
    }

}
