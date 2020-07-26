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
import java.util.function.Function;

import org.ojalgo.function.BasicFunction;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive32Store;
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
            PhysicalStore<Double> parts = args.copy();
            parts.modifyAll(EXP);
            final double total = parts.aggregateAll(Aggregator.SUM);
            return arg -> EXP.invoke(arg) / total;
        }, arg -> ONE, false),

        /**
         * [-1,1]
         */
        TANH(args -> PrimitiveMath.TANH, arg -> ONE - (arg * arg), true);

        private final PrimitiveFunction.Unary myDerivativeInTermsOfOutput;
        private final Function<PhysicalStore<Double>, PrimitiveFunction.Unary> myFunction;
        private final boolean mySingleFolded;

        Activator(final Function<PhysicalStore<Double>, PrimitiveFunction.Unary> function, final PrimitiveFunction.Unary derivativeInTermsOfOutput,
                final boolean singleFolded) {
            myFunction = function;
            myDerivativeInTermsOfOutput = derivativeInTermsOfOutput;
            mySingleFolded = singleFolded;
        }

        PrimitiveFunction.Unary getDerivativeInTermsOfOutput() {
            return myDerivativeInTermsOfOutput;
        }

        PrimitiveFunction.Unary getFunction(final PhysicalStore<Double> arguments) {
            return myFunction.apply(arguments);
        }

        PrimitiveFunction.Unary getFunction(final PhysicalStore<Double> arguments, final double probabilityToKeep) {
            if ((ZERO < probabilityToKeep) && (probabilityToKeep <= ONE)) {
                return new NodeDroppingActivatorFunction(probabilityToKeep, this.getFunction(arguments));
            } else {
                throw new IllegalArgumentException();
            }
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

    public static NetworkBuilder builder(final int numberOfNetworkInputNodes) {
        return ArtificialNeuralNetwork.builder(Primitive64Store.FACTORY, numberOfNetworkInputNodes);
    }

    /**
     * @deprecated Use {@link #builder(int)} instead
     */
    @Deprecated
    public static NetworkTrainer builder(final int numberOfInputNodes, final int... nodesPerCalculationLayer) {
        return ArtificialNeuralNetwork.builder(Primitive64Store.FACTORY, numberOfInputNodes, nodesPerCalculationLayer);
    }

    public static NetworkBuilder builder(final PhysicalStore.Factory<Double, ?> factory, final int numberOfNetworkInputNodes) {
        return new NetworkBuilder(factory, numberOfNetworkInputNodes);
    }

    /**
     * @deprecated Use {@link #builder(org.ojalgo.matrix.store.PhysicalStore.Factory, int)} instead
     */
    @Deprecated
    public static NetworkTrainer builder(final PhysicalStore.Factory<Double, ?> factory, final int numberOfInputNodes, final int... nodesPerCalculationLayer) {
        NetworkBuilder builder = ArtificialNeuralNetwork.builder(factory, numberOfInputNodes);
        for (int i = 0; i < nodesPerCalculationLayer.length; i++) {
            builder.layer(nodesPerCalculationLayer[i]);
        }
        return builder.get().newTrainer();
    }

    /**
     * Read (reconstruct) an ANN from the specified input previously written by {@link #writeTo(DataOutput)}.
     */
    public static ArtificialNeuralNetwork from(final DataInput input) throws IOException {
        return FileFormat.read(null, input);
    }

    /**
     * @see #from(DataInput)
     */
    public static ArtificialNeuralNetwork from(final File file) {
        return ArtificialNeuralNetwork.from(null, file);
    }

    /**
     * @see #from(DataInput)
     */
    public static ArtificialNeuralNetwork from(final Path path, final OpenOption... options) {
        return ArtificialNeuralNetwork.from(null, path, options);
    }

    /**
     * Read (reconstruct) an ANN from the specified input previously written by {@link #writeTo(DataOutput)}.
     */
    public static ArtificialNeuralNetwork from(final PhysicalStore.Factory<Double, ?> factory, final DataInput input) throws IOException {
        return FileFormat.read(factory, input);
    }

    /**
     * @see #from(DataInput)
     */
    public static ArtificialNeuralNetwork from(final PhysicalStore.Factory<Double, ?> factory, final File file) {
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            return ArtificialNeuralNetwork.from(factory, input);
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    /**
     * @see #from(DataInput)
     */
    public static ArtificialNeuralNetwork from(final PhysicalStore.Factory<Double, ?> factory, final Path path, final OpenOption... options) {
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(Files.newInputStream(path, options)))) {
            return ArtificialNeuralNetwork.from(factory, input);
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    private boolean myDropouts = false;
    private final PhysicalStore.Factory<Double, ?> myFactory;
    private final CalculationLayer[] myLayers;

    ArtificialNeuralNetwork(final NetworkBuilder builder) {

        super();

        myFactory = builder.getFactory();

        List<LayerTemplate> templates = builder.getLayers();
        myLayers = new CalculationLayer[templates.size()];
        for (int i = 0; i < myLayers.length; i++) {
            LayerTemplate layerTemplate = templates.get(i);
            myLayers[i] = new CalculationLayer(myFactory, layerTemplate.inputs, layerTemplate.outputs, layerTemplate.activator);
        }
    }

    ArtificialNeuralNetwork(final PhysicalStore.Factory<Double, ?> factory, final int inputs, final int[] layers) {

        super();

        myFactory = factory;
        myLayers = new CalculationLayer[layers.length];
        int tmpIn = inputs;
        int tmpOut = inputs;
        for (int i = 0; i < layers.length; i++) {
            tmpIn = tmpOut;
            tmpOut = layers[i];
            myLayers[i] = new CalculationLayer(factory, tmpIn, tmpOut, ArtificialNeuralNetwork.Activator.SIGMOID);
        }
    }

    /**
     * @return The number of calculation layers
     */
    public int depth() {
        return myLayers.length;
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

    /**
     * @deprecated v49 Use {@link #newInvoker()} and then {@link NetworkInvoker#invoke(Access1D)} instead
     */
    @Deprecated
    public MatrixStore<Double> invoke(final Access1D<Double> input) {
        return this.newInvoker().invoke(input);
    }

    /**
     * If you create multiple invokers you can use them in different threads simutaneously - the invoker
     * contains any/all invocation specific state.
     */
    public NetworkInvoker newInvoker() {
        return new NetworkInvoker(this);
    }

    public NetworkTrainer newTrainer() {
        NetworkTrainer trainer = new NetworkTrainer(this);
        if (this.getOutputActivator() == Activator.SOFTMAX) {
            trainer.error(Error.CROSS_ENTROPY);
        } else {
            trainer.error(Error.HALF_SQUARED_DIFFERENCE);
        }
        return trainer;
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
     * @return The max number of nodes in any layer
     */
    public int width() {
        int retVal = myLayers[0].countInputNodes();
        for (CalculationLayer layer : myLayers) {
            retVal = Math.max(retVal, layer.countOutputNodes());
        }
        return retVal;
    }

    /**
     * Will write (save) the ANN to the specified output. Can then later be read back by using
     * {@link #from(DataInput)}.
     */
    public void writeTo(final DataOutput output) throws IOException {
        int version = (myFactory == Primitive32Store.FACTORY) ? 2 : 1;
        FileFormat.write(this, version, output);
    }

    /**
     * @see #writeTo(DataOutput)
     */
    public void writeTo(final File file) {
        try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            this.writeTo(output);
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    /**
     * @see #writeTo(DataOutput)
     */
    public void writeTo(final Path path, final OpenOption... options) {
        try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(path, options)))) {
            this.writeTo(output);
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    void adjust(final int layer, final Access1D<Double> input, final PhysicalStore<Double> downstreamGradient, final double learningRate,
            final PhysicalStore<Double> upstreamGradient, final PhysicalStore<Double> output) {
        myLayers[layer].adjust(input, downstreamGradient, learningRate, upstreamGradient, output, this.factor(layer));
    }

    int countInputNodes(final int layer) {
        return myLayers[layer].countInputNodes();
    }

    int countOutputNodes(final int layer) {
        return myLayers[layer].countOutputNodes();
    }

    /**
     * Used to scale the weights after training with dropouts, and also to adjut the learning rate
     *
     * @param layer
     * @return The probabilityToKeep (as in not drop) the input nodes of this layer
     */
    double factor(final int layer) {
        if (myDropouts && (layer != 0)) {
            return HALF;
        } else {
            return ONE;
        }
    }

    Activator getOutputActivator() {
        return myLayers[myLayers.length - 1].getActivator();
    }

    List<MatrixStore<Double>> getWeights() {
        final ArrayList<MatrixStore<Double>> retVal = new ArrayList<>();
        for (int i = 0; i < myLayers.length; i++) {
            retVal.add(myLayers[i].getLogicalWeights());
        }
        return retVal;
    }

    PhysicalStore<Double> invoke(final int layer, final Access1D<Double> input, final PhysicalStore<Double> output, final boolean training) {
        if (training && myDropouts && (layer < (this.depth() - 1))) {
            return myLayers[layer].invoke(input, output, HALF);
        } else {
            return myLayers[layer].invoke(input, output, ONE);
        }
    }

    boolean isDropouts() {
        return myDropouts;
    }

    PhysicalStore<Double> newStore(final int rows, final int columns) {
        return myFactory.make(rows, columns);
    }

    void randomise() {
        for (int l = 0; l < myLayers.length; l++) {
            myLayers[l].randomise();
        }
    }

    void scale(final int layer, final double factor) {
        myLayers[layer].scale(factor);
    }

    void setActivator(final int layer, final Activator activator) {
        myLayers[layer].setActivator(activator);
    }

    void setBias(final int layer, final int output, final double bias) {
        myLayers[layer].setBias(output, bias);
    }

    void setDropouts(final boolean dropouts) {
        myDropouts = dropouts;
    }

    void setWeight(final int layer, final int input, final int output, final double weight) {
        myLayers[layer].setWeight(input, output, weight);
    }

    Structure2D[] structure() {

        Structure2D[] retVal = new Structure2D[myLayers.length];

        for (int l = 0; l < retVal.length; l++) {
            retVal[l] = myLayers[l].getStructure();
        }

        return retVal;
    }

}
