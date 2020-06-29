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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.ojalgo.ann.ArtificialNeuralNetwork.Activator;
import org.ojalgo.structure.Structure2D;

abstract class FileFormat {

    /**
     * Directly mapping the original internal construction
     */
    abstract static class Version1 {

        static final int ID = 1;

        static ArtificialNeuralNetwork read(DataInput input) throws IOException {

            int numberofInputs = input.readInt();

            int numberOfLayers = input.readInt();

            int[] layerOutputs = new int[numberOfLayers];
            for (int i = 0; i < numberOfLayers; i++) {
                layerOutputs[i] = input.readInt();
            }

            ArtificialNeuralNetwork retVal = new ArtificialNeuralNetwork(numberofInputs, layerOutputs);

            int numberofOutputs;
            for (int l = 0; l < numberOfLayers; l++) {
                numberofOutputs = layerOutputs[l];

                CalculationLayer layer = retVal.getLayer(l);

                for (int j = 0; j < numberofOutputs; j++) {

                    layer.setBias(j, input.readDouble());

                    for (int i = 0; i < numberofInputs; i++) {
                        layer.setWeight(i, j, input.readDouble());
                    }
                }

                layer.setActivator(Activator.valueOf(input.readUTF()));

                numberofInputs = numberofOutputs;
            }

            return retVal;
        }

        static void write(ArtificialNeuralNetwork network, DataOutput output) throws IOException {

            Structure2D[] structure = network.structure();

            output.writeInt(Math.toIntExact(structure[0].countRows()));

            output.writeInt(structure.length);

            for (int l = 0; l < structure.length; l++) {
                output.writeInt(Math.toIntExact(structure[l].countColumns()));
            }

            int numberofInputs, numberofOutputs;

            for (int l = 0; l < structure.length; l++) {
                numberofInputs = Math.toIntExact(structure[l].countRows());
                numberofOutputs = Math.toIntExact(structure[l].countColumns());

                for (int j = 0; j < numberofOutputs; j++) {

                    output.writeDouble(network.getBias(l, j));

                    for (int i = 0; i < numberofInputs; i++) {
                        output.writeDouble(network.getWeight(l, i, j));
                    }
                }

                output.writeUTF(network.getActivator(l).name());
            }
        }

    }

    private static final String FORMAT = "ojAlgo ANN";

    static ArtificialNeuralNetwork read(DataInput input) throws IOException {

        String format = input.readUTF();
        if (!FORMAT.equals(format)) {
            throw new IOException("Unsupported format!");
        }

        int version = input.readInt();

        switch (version) {
        case Version1.ID:
            return Version1.read(input);
        default:
            throw new IOException("Unsupported version!");
        }
    }

    static void write(ArtificialNeuralNetwork network, int version, DataOutput output) throws IOException {

        output.writeUTF(FORMAT);

        output.writeInt(version);

        switch (version) {
        case Version1.ID:
            Version1.write(network, output);
            break;
        default:
            throw new IOException("Unsupported version!");
        }
    }

}
