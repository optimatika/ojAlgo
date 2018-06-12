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

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import java.util.function.UnaryOperator;

import org.ojalgo.access.Access1D;
import org.ojalgo.ann.ArtificialNeuralNetwork.Activator;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.random.Normal;

final class Layer implements UnaryOperator<Access1D<Double>> {

    private ArtificialNeuralNetwork.Activator myActivator;
    private final PrimitiveDenseStore myBias;
    private final PrimitiveDenseStore myOutput;
    private transient PrimitiveDenseStore myOutputCopy = null;
    private final PrimitiveDenseStore myWeights;

    Layer(int numberOfInputs, int numberOfOutputs, ArtificialNeuralNetwork.Activator activator) {

        super();

        myWeights = PrimitiveDenseStore.FACTORY.makeZero(numberOfInputs, numberOfOutputs);
        myBias = PrimitiveDenseStore.FACTORY.makeZero(1, numberOfOutputs);
        myOutput = PrimitiveDenseStore.FACTORY.makeZero(1, numberOfOutputs);

        myActivator = activator;
    }

    public Access1D<Double> apply(Access1D<Double> input) {
        myWeights.premultiply(input).operateOnMatching(ADD, myBias).supplyTo(myOutput);
        myOutput.modifyAll(myActivator.getFunction(myOutput));
        return myOutput;
    }

    void adjust(final Access1D<Double> input, PrimitiveDenseStore downstreamGradient, double learningRate) {

        PrimitiveDenseStore gradient = this.copyOutput();
        gradient.modifyAll(myActivator.getDerivativeInTermsOfOutput());
        gradient.modifyMatching(MULTIPLY, downstreamGradient);

        myWeights.multiply(gradient, downstreamGradient);

        PrimitiveDenseStore delta = myWeights.copy();

        for (long j = 0L, outLim = gradient.count(); j < outLim; j++) {
            final double grad = gradient.doubleValue(j);
            for (long i = 0L, inLim = input.count(); i < inLim; i++) {
                delta.set(i, j, input.doubleValue(i) * grad);
                myWeights.add(i, j, learningRate * input.doubleValue(i) * grad);
            }
            myBias.add(j, learningRate * grad);
        }
    }

    PrimitiveDenseStore copyOutput() {
        if (myOutputCopy == null) {
            myOutputCopy = myOutput.copy();
        } else {
            myOutput.supplyTo(myOutputCopy);
        }
        return myOutputCopy;
    }

    double getBias(int output) {
        return myBias.doubleValue(output);
    }

    PrimitiveDenseStore getOutput() {
        return myOutput;
    }

    double getWeight(int input, int output) {
        return myWeights.doubleValue(input, output);
    }

    void randomise() {

        Normal generator = new Normal(ONE / myWeights.countRows(), HALF);

        myWeights.fillAll(generator);
        myBias.fillAll(generator);
    }

    void setActivator(Activator activator) {
        myActivator = activator;
    }

    void setBias(int output, double bias) {
        myBias.set(output, bias);
    }

    void setWeight(int input, int output, double weight) {
        myWeights.set(input, output, weight);
    }

}
