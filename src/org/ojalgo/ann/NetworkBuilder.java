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

import java.util.Iterator;
import java.util.function.Supplier;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.ColumnView;
import org.ojalgo.access.RowView;
import org.ojalgo.access.Structure2D;
import org.ojalgo.matrix.store.PrimitiveDenseStore;

public final class NetworkBuilder implements Supplier<ArtificialNeuralNetwork> {

    private final ArtificialNeuralNetwork myANN;
    private ArtificialNeuralNetwork.Error myError = ArtificialNeuralNetwork.Error.HALF_SQUARED_DIFFERENCE;

    public NetworkBuilder(int numberOfInputNodes, int... nodesPerCalculationLayer) {
        super();
        if (nodesPerCalculationLayer.length < 1) {
            ProgrammingError.throwWithMessage("There must be atleast 1 calculation layer - that would be the output layer!");
        }
        myANN = new ArtificialNeuralNetwork(numberOfInputNodes, nodesPerCalculationLayer);
    }

    /**
     * @param layer 0-based index among the calculation layers (excluding the input layer)
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

    public NetworkBuilder error(ArtificialNeuralNetwork.Error error) {
        myError = error;
        return this;
    }

    public ArtificialNeuralNetwork get() {
        return myANN;
    }

    public NetworkBuilder randomise() {
        myANN.randomise();
        return this;
    }

    public void train(Access1D<Double> givenInput, Access1D<Double> targetOutput, double learningRate) {

        Access1D<Double> current = myANN.apply(givenInput);

        PrimitiveDenseStore downstreamGradient = PrimitiveDenseStore.FACTORY.columns(targetOutput);
        downstreamGradient.modifyMatching(myError.getDerivative(), current);

        myANN.backpropagate(givenInput, downstreamGradient, -learningRate);
    }

    public void trainColumns(Access2D<Double> givenInput, Access2D<Double> desiredOutput, double learningRate) {

        Iterator<ColumnView<Double>> iterInp = givenInput.columns().iterator();
        Iterator<ColumnView<Double>> iterOut = desiredOutput.columns().iterator();

        while (iterInp.hasNext() && iterOut.hasNext()) {
            this.train(iterInp.next(), iterOut.next(), learningRate);
        }
    }

    public void trainRows(Access2D<Double> givenInput, Access2D<Double> desiredOutput, double learningRate) {

        Iterator<RowView<Double>> iterInp = givenInput.rows().iterator();
        Iterator<RowView<Double>> iterOut = desiredOutput.rows().iterator();

        while (iterInp.hasNext() && iterOut.hasNext()) {
            this.train(iterInp.next(), iterOut.next(), learningRate);
        }
    }

    public NetworkBuilder weight(int layer, int input, int output, double weight) {
        myANN.setWeight(layer, input, output, weight);
        return this;
    }

    public Structure2D[] getStructure() {
        return myANN.getStructure();
    }

    double getBias(int layer, int output) {
        return myANN.getBias(layer, output);
    }

    Access1D<Double> getOutput(int layer) {
        return myANN.getOutput(layer);
    }

    double getWeight(int layer, int input, int output) {
        return myANN.getWeight(layer, input, output);
    }

    public double error(Access1D<?> target, Access1D<?> current) {
        return myError.invoke(target, current);
    }

}
