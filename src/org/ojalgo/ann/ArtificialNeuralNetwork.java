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

    public static enum Error {

        HALF_SQUARED_DIFFERENCE((target, current) -> HALF * (target - current) * (target - current), (target, current) -> (current - target));

        private final PrimitiveFunction.Binary myDerivative;
        private final PrimitiveFunction.Binary myFunction;

        Error(PrimitiveFunction.Binary function, PrimitiveFunction.Binary derivative) {
            myFunction = function;
            myDerivative = derivative;
        }

        BinaryFunction<Double> getDerivative() {
            return myDerivative;
        }

        BinaryFunction<Double> getFunction() {
            return myFunction;
        }
    }

    private final Layer[] myLayers;

    ArtificialNeuralNetwork(int inputs, int[] layerss) {
        super();
        myLayers = new Layer[layerss.length];
        int tmpIn = inputs;
        int tmpOut = inputs;
        for (int i = 0; i < layerss.length; i++) {
            tmpIn = tmpOut;
            tmpOut = layerss[i];
            myLayers[i] = new Layer(tmpIn, tmpOut, Activator.RELU);
        }
    }

    public Access1D<Double> apply(Access1D<Double> input) {
        Access1D<Double> retVal = input;
        for (int i = 0, limit = myLayers.length; i < limit; i++) {
            retVal = myLayers[i].apply(retVal);
        }
        return retVal;
    }

    void backpropagate(Access1D<Double> input, Access1D<Double> downStreamDerivative) {

        PrimitiveDenseStore[] weights = new PrimitiveDenseStore[myLayers.length];
        PrimitiveDenseStore[] bias = new PrimitiveDenseStore[myLayers.length];
        PrimitiveDenseStore[] output = new PrimitiveDenseStore[myLayers.length];

        for (int i = 0; i < myLayers.length; i++) {
            weights[i] = myLayers[i].copyWeights();
            bias[i] = myLayers[i].copyBias();
            output[i] = myLayers[i].copyOutput();
        }

        for (int i = myLayers.length - 1; i >= 0; i--) {
            output[i].modifyAll(myLayers[i].getActivator().getDerivativeInTermsOfOutput());
            output[i].modifyMatching(MULTIPLY, downStreamDerivative);
            for (int r = 0; r < weights[i].countRows(); r++) {
                weights[i].sliceRow(r).modifyMatching(MULTIPLY, output[i]);
            }
        }

    }

    void initialise() {
        for (int i = 0, limit = myLayers.length; i < limit; i++) {
            myLayers[i].initialise();
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
