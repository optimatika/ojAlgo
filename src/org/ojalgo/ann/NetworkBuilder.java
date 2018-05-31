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

import java.util.Arrays;
import java.util.function.Supplier;

import org.ojalgo.function.UnaryFunction;

public final class NetworkBuilder implements Supplier<ArtificialNeuralNetwork> {

    private final int[] myHidden;
    private final UnaryFunction<Double>[] myHiddenActivators;
    private final int myInput;
    private final int myOutput;
    private UnaryFunction<Double> myOutputActivator;

    @SuppressWarnings("unchecked")
    public NetworkBuilder(int input, int output, int... hidden) {
        super();

        myInput = input;
        myOutput = output;
        myHidden = hidden;

        myHiddenActivators = new UnaryFunction[myHidden.length];
        Arrays.fill(myHiddenActivators, ArtificialNeuralNetwork.RELU);
        myOutputActivator = ArtificialNeuralNetwork.RELU;

    }

    public ArtificialNeuralNetwork get() {

        ArtificialNeuralNetwork retVal = new ArtificialNeuralNetwork(myInput, myHidden, myOutput);

        return retVal;
    }

    /**
     * @param index 0-based index among the hidden layers
     * @param activator The activator function to use
     */
    public NetworkBuilder setHiddenActivator(int index, UnaryFunction<Double> activator) {
        myHiddenActivators[index] = activator;
        return this;
    }

    public NetworkBuilder setOutputActivator(UnaryFunction<Double> activator) {
        myOutputActivator = activator;
        return this;
    }

}
