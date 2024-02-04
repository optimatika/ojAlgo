/*
 * Copyright 1997-2024 Optimatika
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
import org.ojalgo.data.DataBatch;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Structure2D;

abstract class WrappedANN implements Supplier<ArtificialNeuralNetwork> {

    private final int myBatchSize;
    private PhysicalStore<Double> myInput;
    private final ArtificialNeuralNetwork myNetwork;
    private final PhysicalStore<Double>[] myOutputs;

    WrappedANN(final ArtificialNeuralNetwork network, final int batchSize) {

        super();

        myNetwork = network;
        myBatchSize = batchSize;

        myOutputs = (PhysicalStore<Double>[]) new PhysicalStore<?>[network.depth()];
        for (int l = 0; l < myOutputs.length; l++) {
            myOutputs[l] = network.newStore(batchSize, network.countOutputNodes(l));
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WrappedANN)) {
            return false;
        }
        WrappedANN other = (WrappedANN) obj;
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
        result = prime * result + (myNetwork == null ? 0 : myNetwork.hashCode());
        return result;
    }

    /**
     * When using {@link NetworkTrainer} or {@link NetworkInvoker} with a batch size larger than 1 this
     * utility may help with creating the batches.
     */
    public DataBatch newInputBatch() {
        return myNetwork.newBatch(myBatchSize, myNetwork.countInputNodes());
    }

    private void setInput(final Access1D<Double> input) {
        if (input instanceof PhysicalStore && ((PhysicalStore<Double>) input).getRowDim() == myBatchSize) {
            myInput = (PhysicalStore<Double>) input;
        } else {
            if (myInput == null || myInput.getRowDim() != myBatchSize) {
                myInput = myNetwork.newStore(myBatchSize, myNetwork.countInputNodes());
            }
            myInput.fillMatching(input);
        }
    }

    void adjust(final int layer, final PhysicalStore<Double> input, final PhysicalStore<Double> output, final PhysicalStore<Double> upstreamGradient,
            final PhysicalStore<Double> downstreamGradient) {
        myNetwork.adjust(layer, input, output, upstreamGradient, downstreamGradient);
    }

    int depth() {
        return myNetwork.depth();
    }

    Activator getActivator(final int layer) {
        return myNetwork.getActivator(layer);
    }

    int getBatchSize() {
        return myBatchSize;
    }

    double getBias(final int layer, final int output) {
        return myNetwork.getBias(layer, output);
    }

    PhysicalStore<Double> getInput() {
        return myInput;
    }

    PhysicalStore<Double> getInput(final int layer) {
        return layer <= 0 ? myInput : myOutputs[layer - 1];
    }

    PhysicalStore<Double> getOutput() {
        return myOutputs[myOutputs.length - 1];
    }

    PhysicalStore<Double> getOutput(final int layer) {
        return myOutputs[layer];
    }

    Activator getOutputActivator() {
        return myNetwork.getOutputActivator();
    }

    double getWeight(final int layer, final int input, final int output) {
        return myNetwork.getWeight(layer, input, output);
    }

    List<MatrixStore<Double>> getWeights() {
        return myNetwork.getWeights();
    }

    MatrixStore<Double> invoke(final Access1D<Double> input, final TrainingConfiguration configuration) {

        this.setInput(input);

        myNetwork.setConfiguration(configuration);

        PhysicalStore<Double> retVal = myInput;
        for (int l = 0, limit = this.depth(); l < limit; l++) {
            retVal = myNetwork.invoke(l, retVal, myOutputs[l]);
        }
        return retVal;
    }

    DataBatch newOutputBatch() {
        return myNetwork.newBatch(myBatchSize, myNetwork.countOutputNodes());
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

    void setWeight(final int layer, final int input, final int output, final double weight) {
        myNetwork.setWeight(layer, input, output, weight);
    }

    Structure2D[] structure() {
        return myNetwork.structure();
    }

}
