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
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.store.PrimitiveDenseStore;

public final class ArtificialNeuralNetwork implements UnaryOperator<Access1D<Double>> {

    public static enum Activator {

        /**
         * [0,+)
         */
        RELU(arg -> Math.max(ZERO, arg), null),
        /**
         * [0,1]
         */
        SIGMOID(PrimitiveFunction.LOGISTIC, arg -> arg * (ONE - arg)),
        /**
         * [-1,1]
         */
        TANH(PrimitiveFunction.TANH, null);

        private final PrimitiveFunction.Unary myDerivativeInTermsOfOutput;
        private final PrimitiveFunction.Unary myFunction;

        Activator(PrimitiveFunction.Unary function, PrimitiveFunction.Unary derivativeInTermsOfOutput) {
            myFunction = function;
            myDerivativeInTermsOfOutput = derivativeInTermsOfOutput;
        }

        UnaryFunction<Double> getDerivative() {
            return myFunction.andThen(myDerivativeInTermsOfOutput);
        }

        PrimitiveFunction.Unary getDerivativeInTermsOfOutput() {
            return myDerivativeInTermsOfOutput;
        }

        UnaryFunction<Double> getFunction() {
            return myFunction;
        }
    }

    public static enum Error implements PrimitiveFunction.Binary {

        HALF_SQUARED_DIFFERENCE((target, current) -> HALF * (target - current) * (target - current), (target, current) -> (current - target));

        private final PrimitiveFunction.Binary myDerivative;
        private final PrimitiveFunction.Binary myFunction;

        Error(PrimitiveFunction.Binary function, PrimitiveFunction.Binary derivative) {
            myFunction = function;
            myDerivative = derivative;
        }

        public double invoke(Access1D<?> target, Access1D<?> current) {
            int limit = (int) Math.min(target.count(), current.count());
            double retVal = ZERO;
            for (int i = 0; i < limit; i++) {
                retVal += myFunction.invoke(target.doubleValue(i), current.doubleValue(i));
            }
            return retVal;
        }

        public double invoke(double target, double current) {
            return myFunction.invoke(target, current);
        }

        BinaryFunction<Double> getDerivative() {
            return myDerivative;
        }
    }

    private final Layer[] myLayers;

    ArtificialNeuralNetwork(int inputs, int[] layers) {
        super();
        myLayers = new Layer[layers.length];
        int tmpIn = inputs;
        int tmpOut = inputs;
        for (int i = 0; i < layers.length; i++) {
            tmpIn = tmpOut;
            tmpOut = layers[i];
            myLayers[i] = new Layer(tmpIn, tmpOut, Activator.SIGMOID);
        }
    }

    public Access1D<Double> apply(Access1D<Double> input) {
        Access1D<Double> retVal = input;
        for (int i = 0, limit = myLayers.length; i < limit; i++) {
            retVal = myLayers[i].apply(retVal);
        }
        return retVal;
    }

    void backpropagate(Access1D<Double> input, PrimitiveDenseStore downStreamDerivative, double learningRate) {

        PrimitiveDenseStore[] weights = new PrimitiveDenseStore[myLayers.length];
        PrimitiveDenseStore[] bias = new PrimitiveDenseStore[myLayers.length];
        PrimitiveDenseStore[] output = new PrimitiveDenseStore[myLayers.length];

        for (int k = 0, limit = myLayers.length; k < limit; k++) {
            weights[k] = myLayers[k].copyWeights();
            bias[k] = myLayers[k].copyBias();
            output[k] = myLayers[k].copyOutput();
        }

        for (int k = myLayers.length - 1; k >= 0; k--) {
            output[k].modifyAll(myLayers[k].getActivator().getDerivativeInTermsOfOutput());
            output[k].modifyMatching(MULTIPLY, downStreamDerivative);
            for (int j = 0; j < output[k].count(); j++) {
                if (k == 0) {
                    for (int i = 0; i < input.count(); i++) {
                        weights[k].set(i, j, input.doubleValue(i) * output[k].doubleValue(j));
                    }
                } else {
                    for (int i = 0; i < output[k - 1].count(); i++) {
                        weights[k].set(i, j, output[k - 1].doubleValue(i) * output[k].doubleValue(j));
                    }
                }
                bias[k].set(j, output[k].doubleValue(j));
            }
            myLayers[k].multiply(output[k], downStreamDerivative);
        }

        for (int k = 0, limit = myLayers.length; k < limit; k++) {
            myLayers[k].update(-learningRate, weights[k], bias[k]);
        }
    }

    double getBias(int layer, int output) {
        return myLayers[layer].getBias(output);
    }

    double getWeight(int layer, int input, int output) {
        return myLayers[layer].getWeight(input, output);
    }

    void randomise() {
        for (int i = 0, limit = myLayers.length; i < limit; i++) {
            myLayers[i].randomise();
        }
    }

    void setActivator(int layer, Activator activator) {
        myLayers[layer].setActivator(activator);
    }

    void setBias(int layer, int output, double bias) {
        myLayers[layer].setBias(output, bias);
    }

    void setWeight(int layer, int input, int output, double weight) {
        myLayers[layer].setWeight(input, output, weight);
    }

}
