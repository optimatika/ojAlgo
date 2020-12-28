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

import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.type.context.NumberContext;

/**
 * https://medium.com/@14prakash/back-propagation-is-very-simple-who-made-it-complicated-97b794c97e5c
 *
 * @author apete
 */
public class BackPropagationIsVerySimple extends BackPropagationExample {

    private static final NumberContext PRECISION = new NumberContext(4, 4);

    public BackPropagationIsVerySimple() {
        super();
    }

    @Override
    protected ArtificialNeuralNetwork getInitialNetwork(final Factory<Double, ?> factory) {

        ArtificialNeuralNetwork network = ArtificialNeuralNetwork.builder(factory, 3).layer(3, RECTIFIER).layer(3, SIGMOID).layer(3, SOFTMAX).get();

        NetworkTrainer trainer = network.newTrainer();

        trainer.weight(0, 0, 0, 0.1);
        trainer.weight(0, 1, 0, 0.3);
        trainer.weight(0, 2, 0, 0.4);
        trainer.weight(0, 0, 1, 0.2);
        trainer.weight(0, 1, 1, 0.2);
        trainer.weight(0, 2, 1, 0.3);
        trainer.weight(0, 0, 2, 0.3);
        trainer.weight(0, 1, 2, 0.7);
        trainer.weight(0, 2, 2, 0.9);

        trainer.bias(0, 0, 1.0);
        trainer.bias(0, 1, 1.0);
        trainer.bias(0, 2, 1.0);

        trainer.weight(1, 0, 0, 0.2);
        trainer.weight(1, 1, 0, 0.3);
        trainer.weight(1, 2, 0, 0.6);
        trainer.weight(1, 0, 1, 0.3);
        trainer.weight(1, 1, 1, 0.5);
        trainer.weight(1, 2, 1, 0.4);
        trainer.weight(1, 0, 2, 0.5);
        trainer.weight(1, 1, 2, 0.7);
        trainer.weight(1, 2, 2, 0.8);

        trainer.bias(1, 0, 1.0);
        trainer.bias(1, 1, 1.0);
        trainer.bias(1, 2, 1.0);

        trainer.weight(2, 0, 0, 0.1);
        trainer.weight(2, 1, 0, 0.3);
        trainer.weight(2, 2, 0, 0.5);
        trainer.weight(2, 0, 1, 0.4);
        trainer.weight(2, 1, 1, 0.7);
        trainer.weight(2, 2, 1, 0.2);
        trainer.weight(2, 0, 2, 0.8);
        trainer.weight(2, 1, 2, 0.2);
        trainer.weight(2, 2, 2, 0.9);

        trainer.bias(2, 0, 1.0);
        trainer.bias(2, 1, 1.0);
        trainer.bias(2, 2, 1.0);

        return network;
    }

    @Override
    protected List<Data> getTestCases() {

        Data retVal = new Data(0.01);

        retVal.input(0.1, 0.2, 0.7);
        retVal.target(1.0, 0.0, 0.0);
        retVal.expected(0.19858, 0.28559, 0.51583);

        return Collections.singletonList(retVal);
    }

    @Override
    protected NumberContext precision() {
        return PRECISION;
    }

}
