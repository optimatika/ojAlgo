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
package org.ojalgo.data.ann;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import org.ojalgo.core.ProgrammingError;
import org.ojalgo.core.structure.Access1D;
import org.ojalgo.core.structure.Access2D;
import org.ojalgo.core.structure.Structure2D;
import org.ojalgo.data.ann.ArtificialNeuralNetwork.Activator;
import org.ojalgo.data.ann.ArtificialNeuralNetwork.Error;
import org.ojalgo.matrix.store.PhysicalStore;

/**
 * An Artificial Neural Network (ANN) builder/trainer.
 *
 * @author apete
 */
public final class NetworkTrainer extends WrappedANN {

    private final TrainingConfiguration myConfiguration = new TrainingConfiguration();
    private final PhysicalStore<Double>[] myGradients;

    @SuppressWarnings("unchecked")
    NetworkTrainer(final ArtificialNeuralNetwork network) {

        super(network);

        int depth = network.depth();

        myGradients = (PhysicalStore<Double>[]) new PhysicalStore<?>[1 + depth];
        myGradients[0] = network.newStore(network.countInputNodes(0), 1);
        for (int l = 0; l < depth; l++) {
            myGradients[1 + l] = network.newStore(network.countOutputNodes(l), 1);
        }

    }

    @SuppressWarnings("unchecked")
    NetworkTrainer(final PhysicalStore.Factory<Double, ?> factory, final int numberOfInputNodes, final int... outputNodesPerCalculationLayer) {

        super(new ArtificialNeuralNetwork(factory, numberOfInputNodes, outputNodesPerCalculationLayer));

        if (outputNodesPerCalculationLayer.length < 1) {
            ProgrammingError.throwWithMessage("There must be at least 1 layer!");
        }

        myGradients = (PhysicalStore<Double>[]) new PhysicalStore<?>[1 + outputNodesPerCalculationLayer.length];
        myGradients[0] = factory.make(numberOfInputNodes, 1);
        for (int l = 0; l < outputNodesPerCalculationLayer.length; l++) {
            myGradients[1 + l] = factory.make(outputNodesPerCalculationLayer[l], 1);
        }

    }

    /**
     * @param layer 0-based index among the calculation layers (excluding the input layer)
     * @param activator The activator function to use
     * @deprecated Use {@link NetworkBuilder} and {@link NetworkBuilder#layer(int, Activator)} instead.
     */
    @Deprecated
    public NetworkTrainer activator(final int layer, final ArtificialNeuralNetwork.Activator activator) {
        this.setActivator(layer, activator);
        return this;
    }

    /**
     * @deprecated Use {@link NetworkBuilder} and {@link NetworkBuilder#layer(int, Activator)} instead.
     */
    @Deprecated
    public NetworkTrainer activators(final ArtificialNeuralNetwork.Activator activator) {
        for (int i = 0, limit = this.depth(); i < limit; i++) {
            this.activator(i, activator);
        }
        return this;
    }

    /**
     * @deprecated Use {@link NetworkBuilder} and {@link NetworkBuilder#layer(int, Activator)} instead.
     */
    @Deprecated
    public NetworkTrainer activators(final ArtificialNeuralNetwork.Activator... activators) {
        for (int i = 0, limit = activators.length; i < limit; i++) {
            this.activator(i, activators[i]);
        }
        return this;
    }

    public NetworkTrainer bias(final int layer, final int output, final double bias) {
        this.setBias(layer, output, bias);
        return this;
    }

    public NetworkTrainer dropouts() {
        myConfiguration.dropouts = true;
        return this;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof NetworkTrainer)) {
            return false;
        }
        NetworkTrainer other = (NetworkTrainer) obj;
        return Objects.equals(myConfiguration, other.myConfiguration) && Arrays.equals(myGradients, other.myGradients)
                && (Double.doubleToLongBits(myConfiguration.learningRate) == Double.doubleToLongBits(other.myConfiguration.learningRate));
    }

    public NetworkTrainer error(final ArtificialNeuralNetwork.Error error) {
        if (this.getOutputActivator() == Activator.SOFTMAX) {
            if (error != Error.CROSS_ENTROPY) {
                throw new IllegalArgumentException();
            }
        } else {
            if (error != Error.HALF_SQUARED_DIFFERENCE) {
                throw new IllegalArgumentException();
            }
        }
        myConfiguration.error = error;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = (prime * result) + Arrays.hashCode(myGradients);
        result = (prime * result) + Objects.hash(myConfiguration, myConfiguration.learningRate);
        return result;
    }

    /**
     * L1 lasso regularisation
     */
    public NetworkTrainer lasso(final double factor) {
        myConfiguration.regularisationL1 = true;
        myConfiguration.regularisationL1Factor = factor;
        return this;
    }

    public NetworkTrainer rate(final double rate) {
        myConfiguration.learningRate = rate;
        return this;
    }

    /**
     * L2 ridge regularisation
     */
    public NetworkTrainer ridge(final double factor) {
        myConfiguration.regularisationL2 = true;
        myConfiguration.regularisationL2Factor = factor;
        return this;
    }

    @Override
    public Structure2D[] structure() {
        return super.structure();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NetworkBuilder [structure()=");
        builder.append(Arrays.toString(this.structure()));
        builder.append(", Error=");
        builder.append(myConfiguration.error);
        builder.append(", LearningRate=");
        builder.append(myConfiguration.learningRate);
        builder.append("]");
        return builder.toString();
    }

    public void train(final Access1D<Double> givenInput, final Access1D<Double> targetOutput) {

        Access1D<Double> current = this.invoke(givenInput, myConfiguration);

        myGradients[0].fillMatching(givenInput);
        myGradients[myGradients.length - 1].fillMatching(targetOutput, myConfiguration.error.getDerivative(), current);

        for (int k = this.depth() - 1; k >= 0; k--) {

            Access1D<Double> input = k == 0 ? givenInput : this.getOutput(k - 1);
            PhysicalStore<Double> output = this.getOutput(k);

            PhysicalStore<Double> upstreamGradient = myGradients[k];
            PhysicalStore<Double> downstreamGradient = myGradients[k + 1];

            this.adjust(k, input, output, upstreamGradient, downstreamGradient, myConfiguration);
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

    public NetworkTrainer weight(final int layer, final int input, final int output, final double weight) {
        this.setWeight(layer, input, output, weight);
        return this;
    }

    double error(final Access1D<?> target, final Access1D<?> current) {
        return myConfiguration.error.invoke(target, current);
    }

}
