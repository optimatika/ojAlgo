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
    protected NetworkBuilder getInitialNetwork(Factory<Double, ?> factory) {

        NetworkBuilder builder = ArtificialNeuralNetwork.builder(factory, 3, 3, 3, 3);

        builder.activator(0, RECTIFIER).activator(1, SIGMOID).activator(2, SOFTMAX).error(CROSS_ENTROPY);

        builder.weight(0, 0, 0, 0.1);
        builder.weight(0, 1, 0, 0.3);
        builder.weight(0, 2, 0, 0.4);
        builder.weight(0, 0, 1, 0.2);
        builder.weight(0, 1, 1, 0.2);
        builder.weight(0, 2, 1, 0.3);
        builder.weight(0, 0, 2, 0.3);
        builder.weight(0, 1, 2, 0.7);
        builder.weight(0, 2, 2, 0.9);

        builder.bias(0, 0, 1.0);
        builder.bias(0, 1, 1.0);
        builder.bias(0, 2, 1.0);

        builder.weight(1, 0, 0, 0.2);
        builder.weight(1, 1, 0, 0.3);
        builder.weight(1, 2, 0, 0.6);
        builder.weight(1, 0, 1, 0.3);
        builder.weight(1, 1, 1, 0.5);
        builder.weight(1, 2, 1, 0.4);
        builder.weight(1, 0, 2, 0.5);
        builder.weight(1, 1, 2, 0.7);
        builder.weight(1, 2, 2, 0.8);

        builder.bias(1, 0, 1.0);
        builder.bias(1, 1, 1.0);
        builder.bias(1, 2, 1.0);

        builder.weight(2, 0, 0, 0.1);
        builder.weight(2, 1, 0, 0.3);
        builder.weight(2, 2, 0, 0.5);
        builder.weight(2, 0, 1, 0.4);
        builder.weight(2, 1, 1, 0.7);
        builder.weight(2, 2, 1, 0.2);
        builder.weight(2, 0, 2, 0.8);
        builder.weight(2, 1, 2, 0.2);
        builder.weight(2, 2, 2, 0.9);

        builder.bias(2, 0, 1.0);
        builder.bias(2, 1, 1.0);
        builder.bias(2, 2, 1.0);

        return builder;
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
