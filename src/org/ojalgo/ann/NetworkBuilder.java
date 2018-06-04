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

import java.util.function.Supplier;

import org.ojalgo.access.Access1D;
import org.ojalgo.matrix.store.PrimitiveDenseStore;

public final class NetworkBuilder implements Supplier<ArtificialNeuralNetwork> {

    private final ArtificialNeuralNetwork myANN;

    public NetworkBuilder(int numberOfInputNodes, int... nodesPerCalculationLayer) {
        super();
        myANN = new ArtificialNeuralNetwork(numberOfInputNodes, nodesPerCalculationLayer);
    }

    /**
     * @param layer 0-based index among the calculation layers
     * @param activator The activator function to use
     */
    public NetworkBuilder activator(int layer, ArtificialNeuralNetwork.Activator activator) {
        myANN.setActivator(layer, activator);
        return this;
    }

    public NetworkBuilder bias(int layer, int output, double bias) {
        myANN.setBias(layer, output, bias);
        return this;
    }

    public ArtificialNeuralNetwork get() {
        return myANN;
    }

    public NetworkBuilder weight(int layer, int input, int output, double weight) {
        myANN.setWeight(layer, input, output, weight);
        return this;
    }

    public void train(Access1D<Double> input, Access1D<Double> target, ArtificialNeuralNetwork.Error meassurement) {

        Access1D<Double> current = myANN.apply(input);

        PrimitiveDenseStore errorDerivative = PrimitiveDenseStore.FACTORY.columns(target);
        errorDerivative.modifyMatching(meassurement.getDerivative(), current);

        myANN.backpropagate(input, errorDerivative);

    }

}
