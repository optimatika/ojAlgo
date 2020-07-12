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

import java.util.List;
import java.util.function.Supplier;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Structure2D;

abstract class NetworkUser implements Supplier<ArtificialNeuralNetwork> {

    private final ArtificialNeuralNetwork myNetwork;
    private final PhysicalStore<Double>[] myOutputs;

    NetworkUser(ArtificialNeuralNetwork network) {

        super();

        myNetwork = network;

        myOutputs = (PhysicalStore<Double>[]) new PhysicalStore<?>[network.depth()];
        for (int i = 0; i < myOutputs.length; i++) {
            myOutputs[i] = network.newStore(1, this.getLayer(i).countOutputNodes());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NetworkUser)) {
            return false;
        }
        NetworkUser other = (NetworkUser) obj;
        if (myNetwork == null) {
            if (other.myNetwork != null) {
                return false;
            }
        } else if (!myNetwork.equals(other.myNetwork)) {
            return false;
        }
        return true;
    }

    public ArtificialNeuralNetwork get() {
        return myNetwork;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((myNetwork == null) ? 0 : myNetwork.hashCode());
        return result;
    }

    int depth() {
        return myNetwork.depth();
    }

    double getBias(final int layer, final int output) {
        return myNetwork.getBias(layer, output);
    }

    CalculationLayer getLayer(int index) {
        return myNetwork.getLayer(index);
    }

    double getWeight(final int layer, final int input, final int output) {
        return myNetwork.getWeight(layer, input, output);
    }

    List<MatrixStore<Double>> getWeights() {
        return myNetwork.getWeights();
    }

    MatrixStore<Double> invoke(Access1D<Double> input) {
        PhysicalStore<Double> retVal = null;
        for (int i = 0, limit = this.depth(); i < limit; i++) {
            retVal = myNetwork.getLayer(i).invoke(input, myOutputs[i]);
            input = retVal;
        }
        return retVal;
    }

    Structure2D[] structure() {
        return myNetwork.structure();
    }

    PhysicalStore<Double> getOutput(int layer) {
        return myOutputs[layer];
    }

}
