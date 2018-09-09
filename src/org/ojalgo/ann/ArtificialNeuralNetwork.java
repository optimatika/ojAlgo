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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ojalgo.function.BasicFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Structure2D;

public final class ArtificialNeuralNetwork implements BasicFunction.PlainUnary<Access1D<Double>, MatrixStore<Double>> {

    public static NetworkBuilder builder(int numberOfInputNodes, int... nodesPerCalculationLayer) {
        return new NetworkBuilder(numberOfInputNodes, nodesPerCalculationLayer);
    }

    private final CalculationLayer[] myLayers;

    ArtificialNeuralNetwork(int inputs, int[] layers) {
        super();
        myLayers = new CalculationLayer[layers.length];
        int tmpIn = inputs;
        int tmpOut = inputs;
        for (int i = 0; i < layers.length; i++) {
            tmpIn = tmpOut;
            tmpOut = layers[i];
            myLayers[i] = new CalculationLayer(tmpIn, tmpOut, ANN.Activator.SIGMOID);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ArtificialNeuralNetwork)) {
            return false;
        }
        ArtificialNeuralNetwork other = (ArtificialNeuralNetwork) obj;
        if (!Arrays.equals(myLayers, other.myLayers)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + Arrays.hashCode(myLayers);
        return result;
    }

    public MatrixStore<Double> invoke(Access1D<Double> input) {
        MatrixStore<Double> retVal = null;
        for (int i = 0, limit = myLayers.length; i < limit; i++) {
            retVal = myLayers[i].invoke(input);
            input = retVal;
        }
        return retVal;
    }

    @Override
    public String toString() {
        StringBuilder tmpBuilder = new StringBuilder();
        tmpBuilder.append("ArtificialNeuralNetwork [Layers=");
        tmpBuilder.append(Arrays.toString(myLayers));
        tmpBuilder.append("]");
        return tmpBuilder.toString();
    }

    int countCalculationLayers() {
        return myLayers.length;
    }

    double getBias(int layer, int output) {
        return myLayers[layer].getBias(output);
    }

    CalculationLayer getLayer(int index) {
        return myLayers[index];
    }

    PrimitiveDenseStore getOutput(int layer) {
        return myLayers[layer].getOutput();
    }

    double getWeight(int layer, int input, int output) {
        return myLayers[layer].getWeight(input, output);
    }

    List<MatrixStore<Double>> getWeights() {
        final ArrayList<MatrixStore<Double>> retVal = new ArrayList<>();
        for (int i = 0; i < myLayers.length; i++) {
            retVal.add(myLayers[i].getLogicalWeights());
        }
        return retVal;
    }

    Structure2D[] structure() {

        Structure2D[] retVal = new Structure2D[myLayers.length];

        for (int l = 0; l < retVal.length; l++) {
            retVal[l] = myLayers[l].getStructure();
        }

        return retVal;
    }

}
