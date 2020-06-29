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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ojalgo.function.BasicFunction;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Structure2D;

public final class ArtificialNeuralNetwork implements BasicFunction.PlainUnary<Access1D<Double>, MatrixStore<Double>> {

    /**
     * https://en.wikipedia.org/wiki/Activation_function
     *
     * @author apete
     */
    public enum Activator {

        /**
         * (-,+)
         */
        IDENTITY(args -> (arg -> arg), arg -> ONE, true),
        /**
         * ReLU: [0,+)
         */
        RECTIFIER(args -> (arg -> Math.max(ZERO, arg)), arg -> arg > ZERO ? ONE : ZERO, true),
        /**
         * [0,1]
         */
        SIGMOID(args -> (LOGISTIC), arg -> arg * (ONE - arg), true),
        /**
         * [0,1] <br>
         * Currently this can only be used in the final layer in combination with
         * {@link ArtificialNeuralNetwork.Error#CROSS_ENTROPY}. All other usage will give incorrect network
         * training.
         */
        SOFTMAX(args -> {
            Primitive64Store parts = args.copy();
            parts.modifyAll(EXP);
            final double total = parts.aggregateAll(Aggregator.SUM);
            return arg -> EXP.invoke(arg) / total;
        }, arg -> ONE, false),
        /**
         * [-1,1]
         */
        TANH(args -> (org.ojalgo.function.constant.PrimitiveMath.TANH), arg -> ONE - (arg * arg), true);

        private final PrimitiveFunction.Unary myDerivativeInTermsOfOutput;
        private final ActivatorFunctionFactory myFunction;
        private final boolean mySingleFolded;

        Activator(final ActivatorFunctionFactory function, final PrimitiveFunction.Unary derivativeInTermsOfOutput, final boolean singleFolded) {
            myFunction = function;
            myDerivativeInTermsOfOutput = derivativeInTermsOfOutput;
            mySingleFolded = singleFolded;
        }

        PrimitiveFunction.Unary getDerivativeInTermsOfOutput() {
            return myDerivativeInTermsOfOutput;
        }

        PrimitiveFunction.Unary getFunction(final Primitive64Store arguments) {
            return myFunction.make(arguments);
        }

        boolean isSingleFolded() {
            return mySingleFolded;
        }
    }

    public enum Error implements PrimitiveFunction.Binary {

        /**
         * Currently this can only be used in in combination with {@link Activator#SOFTMAX} in the final
         * layer. All other usage will give incorrect network training.
         */
        CROSS_ENTROPY((target, current) -> -target * Math.log(current), (target, current) -> (current - target)),
        /**
         *
         */
        HALF_SQUARED_DIFFERENCE((target, current) -> HALF * (target - current) * (target - current), (target, current) -> (current - target));

        private final PrimitiveFunction.Binary myDerivative;
        private final PrimitiveFunction.Binary myFunction;

        Error(final PrimitiveFunction.Binary function, final PrimitiveFunction.Binary derivative) {
            myFunction = function;
            myDerivative = derivative;
        }

        public double invoke(final Access1D<?> target, final Access1D<?> current) {
            int limit = MissingMath.toMinIntExact(target.count(), current.count());
            double retVal = ZERO;
            for (int i = 0; i < limit; i++) {
                retVal += myFunction.invoke(target.doubleValue(i), current.doubleValue(i));
            }
            return retVal;
        }

        public double invoke(final double target, final double current) {
            return myFunction.invoke(target, current);
        }

        PrimitiveFunction.Binary getDerivative() {
            return myDerivative;
        }

    }

    interface ActivatorFunctionFactory {

        PrimitiveFunction.Unary make(Primitive64Store arguments);

    }

    public static NetworkBuilder builder(final int numberOfInputNodes, final int... nodesPerCalculationLayer) {
        return new NetworkBuilder(numberOfInputNodes, nodesPerCalculationLayer);
    }

    /**
     * Read (reconstruct) an ANN from the specified input previously written by {@link #writeTo(DataOutput)}.
     */
    public static ArtificialNeuralNetwork from(DataInput input) throws IOException {
        return FileFormat.read(input);
    }

    /**
     * @see #from(DataInput)
     */
    public static ArtificialNeuralNetwork from(File file) {
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            return ArtificialNeuralNetwork.from(input);
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    /**
     * @see #from(DataInput)
     */
    public static ArtificialNeuralNetwork from(Path path, OpenOption... options) {
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(Files.newInputStream(path, options)))) {
            return ArtificialNeuralNetwork.from(input);
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    private final CalculationLayer[] myLayers;

    ArtificialNeuralNetwork(final int inputs, final int[] layers) {
        super();
        myLayers = new CalculationLayer[layers.length];
        int tmpIn = inputs;
        int tmpOut = inputs;
        for (int i = 0; i < layers.length; i++) {
            tmpIn = tmpOut;
            tmpOut = layers[i];
            myLayers[i] = new CalculationLayer(tmpIn, tmpOut, ArtificialNeuralNetwork.Activator.SIGMOID);
        }
    }

    @Override
    public boolean equals(final Object obj) {
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

    public Activator getActivator(final int layer) {
        return myLayers[layer].getActivator();
    }

    public double getBias(final int layer, final int output) {
        return myLayers[layer].getBias(output);
    }

    public double getWeight(final int layer, final int input, final int output) {
        return myLayers[layer].getWeight(input, output);
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

    /**
     * Will write (save) the ANN to the specified output. Can then later be read back by using
     * {@link #from(DataInput)}.
     */
    public void writeTo(DataOutput output) throws IOException {
        FileFormat.write(this, 1, output);
    }

    /**
     * @see #writeTo(DataOutput)
     */
    public void writeTo(File file) {
        try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            this.writeTo(output);
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    /**
     * @see #writeTo(DataOutput)
     */
    public void writeTo(Path path, OpenOption... options) {
        try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(path, options)))) {
            this.writeTo(output);
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    int countCalculationLayers() {
        return myLayers.length;
    }

    CalculationLayer getLayer(final int index) {
        return myLayers[index];
    }

    Primitive64Store getOutput(final int layer) {
        return myLayers[layer].getOutput();
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
