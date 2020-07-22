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

import org.ojalgo.matrix.store.PhysicalStore.Factory;
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

    private Data makeCase00(final double learningRate) {
        Data _00 = new Data(learningRate);
        _00.input(0.0, 0.0);
        _00.target(0.0);
        return _00;
    }

    private Data makeCase01(final double learningRate) {
        Data _01 = new Data(learningRate);
        _01.input(0.0, 1.0);
        _01.target(1.0);
        return _01;
    }

    private Data makeCase10(final double learningRate) {
        Data _10 = new Data(learningRate);
        _10.input(1.0, 0.0);
        _10.target(1.0);
        return _10;
    }

    private Data makeCase11(final double learningRate) {
        Data _11 = new Data(learningRate);
        _11.input(1.0, 1.0);
        _11.target(0.0);
        _11.expected(0.7746924929149283);
        return _11;
    }

    @Override
    protected NetworkTrainer getInitialNetwork(Factory<Double, ?> factory) {

        NetworkTrainer builder = ArtificialNeuralNetwork.builder(factory, 2, 3, 1);

        builder.activators(SIGMOID, SIGMOID).error(HALF_SQUARED_DIFFERENCE);

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
    protected List<Data> getTestCases() {

        double learningRate = 1.0;

        List<Data> retVal = new ArrayList<>();

        retVal.add(this.makeCase00(learningRate));
        retVal.add(this.makeCase01(learningRate));
        retVal.add(this.makeCase10(learningRate));
        retVal.add(this.makeCase11(learningRate));

        return retVal;
    }

    @Override
    protected NumberContext precision() {
        return PRECISION;
    }

}
