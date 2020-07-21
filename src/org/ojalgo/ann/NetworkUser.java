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

import org.ojalgo.ann.ArtificialNeuralNetwork.Activator;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Structure2D;

abstract class NetworkUser implements Supplier<ArtificialNeuralNetwork> {

    private final ArtificialNeuralNetwork myNetwork;
    private final PhysicalStore<Double>[] myOutputs;

    @SuppressWarnings("unchecked")
    NetworkUser(final ArtificialNeuralNetwork network) {

        super();

        myNetwork = network;

        myOutputs = (PhysicalStore<Double>[]) new PhysicalStore<?>[network.depth()];
        for (int l = 0; l < myOutputs.length; l++) {
            myOutputs[l] = network.newStore(1, network.countOutputNodes(l));
        }
    }

    @Override
    public boolean equals(final Object obj) {
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

    void adjust(final int layer, final Access1D<Double> input, final PhysicalStore<Double> downstreamGradient, final double learningRate,
            final PhysicalStore<Double> upstreamGradient, final PhysicalStore<Double> output) {
        myNetwork.adjust(layer, input, downstreamGradient, learningRate, upstreamGradient, output);
    }

    int depth() {
        return myNetwork.depth();
    }

    Activator getActivator(final int layer) {
        return myNetwork.getActivator(layer);
    }

    double getBias(final int layer, final int output) {
        return myNetwork.getBias(layer, output);
    }

    PhysicalStore<Double> getOutput(final int layer) {
        return myOutputs[layer];
    }

    double getWeight(final int layer, final int input, final int output) {
        return myNetwork.getWeight(layer, input, output);
    }

    List<MatrixStore<Double>> getWeights() {
        return myNetwork.getWeights();
    }

    MatrixStore<Double> invoke(Access1D<Double> input, final boolean training) {
        PhysicalStore<Double> retVal = null;
        for (int l = 0, limit = this.depth(); l < limit; l++) {
            retVal = myNetwork.invoke(l, input, myOutputs[l], training);
            input = retVal;
        }
        return retVal;
    }

    void randomise() {
        myNetwork.randomise();
    }

    void setActivator(final int layer, final Activator activator) {
        myNetwork.setActivator(layer, activator);
    }

    void setBias(final int layer, final int output, final double bias) {
        myNetwork.setBias(layer, output, bias);
    }

    void setDropouts(final boolean dropouts) {
        if (myNetwork.isDropouts() && !dropouts) {
            for (int l = 1, limit = this.depth(); l < limit; l++) {
                double factor = myNetwork.factor(l);
                myNetwork.scale(l, factor);
            }
        }
        myNetwork.setDropouts(dropouts);
    }

    void setWeight(final int layer, final int input, final int output, final double weight) {
        myNetwork.setWeight(layer, input, output, weight);
    }

    Structure2D[] structure() {
        return myNetwork.structure();
    }

}
