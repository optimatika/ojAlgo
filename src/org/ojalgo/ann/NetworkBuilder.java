/*
 * Copyright 1997-2019 Optimatika
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
import java.util.List;
import java.util.function.Supplier;

import org.ojalgo.ProgrammingError;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;

/**
 * An artificial neural network builder/trainer.
 *
 * @author apete
 */
public final class NetworkBuilder implements Supplier<ArtificialNeuralNetwork> {

    private final ArtificialNeuralNetwork myANN;
    private ArtificialNeuralNetwork.Error myError = ArtificialNeuralNetwork.Error.HALF_SQUARED_DIFFERENCE;
    private final PrimitiveDenseStore[] myLayerValues;
    private double myLearningRate = 1.0;

    NetworkBuilder(final int numberOfInputNodes, final int... outputNodesPerCalculationLayer) {

        super();

        if (outputNodesPerCalculationLayer.length < 1) {
            ProgrammingError.throwWithMessage("There must be at least 1 layer!");
        }

        myANN = new ArtificialNeuralNetwork(numberOfInputNodes, outputNodesPerCalculationLayer);

        myLayerValues = new PrimitiveDenseStore[1 + outputNodesPerCalculationLayer.length];
        myLayerValues[0] = PrimitiveDenseStore.FACTORY.make(numberOfInputNodes, 1);
        for (int l = 0; l < outputNodesPerCalculationLayer.length; l++) {
            myLayerValues[1 + l] = PrimitiveDenseStore.FACTORY.make(outputNodesPerCalculationLayer[l], 1);
        }
    }

    /**
     * @param layer 0-based index among the calculation layers (excluding the input layer)
     * @param activator The activator function to use
     */
    public NetworkBuilder activator(final int layer, final ArtificialNeuralNetwork.Activator activator) {
        myANN.getLayer(layer).setActivator(activator);
        return this;
    }

    public NetworkBuilder activators(final ArtificialNeuralNetwork.Activator activator) {
        for (int i = 0, limit = myANN.countCalculationLayers(); i < limit; i++) {
            myANN.getLayer(i).setActivator(activator);
        }
        return this;
    }

    public NetworkBuilder activators(final ArtificialNeuralNetwork.Activator... activators) {
        for (int i = 0, limit = activators.length; i < limit; i++) {
            myANN.getLayer(i).setActivator(activators[i]);
        }
        return this;
    }

    public NetworkBuilder bias(final int layer, final int output, final double bias) {
        myANN.getLayer(layer).setBias(output, bias);
        return this;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        NetworkBuilder other = (NetworkBuilder) obj;
        if (myANN == null) {
            if (other.myANN != null) {
                return false;
            }
        } else if (!myANN.equals(other.myANN)) {
            return false;
        }
        if (myError != other.myError) {
            return false;
        }
        if (Double.doubleToLongBits(myLearningRate) != Double.doubleToLongBits(other.myLearningRate)) {
            return false;
        }
        return true;
    }

    public NetworkBuilder error(final ArtificialNeuralNetwork.Error error) {
        myError = error;
        return this;
    }

    public ArtificialNeuralNetwork get() {
        return myANN;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((myANN == null) ? 0 : myANN.hashCode());
        result = (prime * result) + ((myError == null) ? 0 : myError.hashCode());

        long temp;
        temp = Double.doubleToLongBits(myLearningRate);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public NetworkBuilder rate(final double rate) {
        myLearningRate = rate;
        return this;
    }

    public Structure2D[] structure() {
        return myANN.structure();
    }

    @Override
    public String toString() {
        StringBuilder tmpBuilder = new StringBuilder();
        tmpBuilder.append("NetworkBuilder [ANN=").append(myANN).append(", Error=").append(myError).append(", LearningRate=").append(myLearningRate).append("]");
        return tmpBuilder.toString();
    }

    public void train(final Access1D<Double> givenInput, final Access1D<Double> targetOutput) {

        Access1D<Double> current = myANN.invoke(givenInput);

        myLayerValues[0].fillMatching(givenInput);
        myLayerValues[myLayerValues.length - 1].fillMatching(targetOutput, myError.getDerivative(), current);

        for (int k = myANN.countCalculationLayers() - 1; k >= 0; k--) {
            myANN.getLayer(k).adjust(k == 0 ? givenInput : myANN.getLayer(k - 1).getOutput(), myLayerValues[k + 1], -myLearningRate, myLayerValues[k]);
        }
    }

    /**
     * Note that the required {@link Iterable}:s can be obtained from calling {@link Access2D#rows()} or
     * {@link Access2D#columns()} on anything "2D".
     */
    public void train(final Iterable<? extends Access1D<Double>> givenInputs, final Iterable<? extends Access1D<Double>> targetOutputs) {

        Iterator<? extends Access1D<Double>> iterI = givenInputs.iterator();
        Iterator<? extends Access1D<Double>> iterO = targetOutputs.iterator();

        while (iterI.hasNext() && iterO.hasNext()) {
            this.train(iterI.next(), iterO.next());
        }
    }

    public NetworkBuilder weight(final int layer, final int input, final int output, final double weight) {
        myANN.getLayer(layer).setWeight(input, output, weight);
        return this;
    }

    double error(final Access1D<?> target, final Access1D<?> current) {
        return myError.invoke(target, current);
    }

    double getBias(final int layer, final int output) {
        return myANN.getBias(layer, output);
    }

    double getWeight(final int layer, final int input, final int output) {
        return myANN.getWeight(layer, input, output);
    }

    List<MatrixStore<Double>> getWeights() {
        return myANN.getWeights();
    }

}
