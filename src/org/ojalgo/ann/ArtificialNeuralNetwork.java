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

import java.util.function.UnaryOperator;

import org.ojalgo.access.Access1D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;

public final class ArtificialNeuralNetwork implements UnaryOperator<Access1D<Double>> {

    /**
     * [0,+)
     */
    public static final UnaryFunction<Double> RELU = PrimitiveFunction.MAX.first(PrimitiveMath.ZERO);
    /**
     * [0,1]
     */
    public static final UnaryFunction<Double> SIGMOID = PrimitiveFunction.LOGISTIC;
    /**
     * [-1,1]
     */
    public static final UnaryFunction<Double> TANH = PrimitiveFunction.TANH;

    private final Layer[] myLayers;

    ArtificialNeuralNetwork(int inputs, int[] layerss) {
        super();
        myLayers = new Layer[layerss.length];
        int tmpIn = inputs;
        int tmpOut = inputs;
        for (int i = 0; i < layerss.length; i++) {
            tmpIn = tmpOut;
            tmpOut = layerss[i];
            myLayers[i] = new Layer(tmpIn, tmpOut, RELU);
        }
    }

    public Access1D<Double> apply(Access1D<Double> input) {
        Access1D<Double> retVal = input;
        for (int i = 0, limit = myLayers.length; i < limit; i++) {
            retVal = myLayers[i].apply(retVal);
        }
        return retVal;
    }

    void initialise() {
        for (int i = 0, limit = myLayers.length; i < limit; i++) {
            myLayers[i].initialise();
        }
    }

    void setActivator(int layer, UnaryFunction<Double> activator) {
        myLayers[layer].setActivator(activator);
    }

    void setBias(int layer, int output, double bias) {
        myLayers[layer].setBias(output, bias);
    }

    void setWeight(int layer, int input, int output, double weight) {
        myLayers[layer].setWeight(input, output, weight);
    }

}
