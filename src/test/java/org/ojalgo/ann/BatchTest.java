/*
 * Copyright 1997-2022 Optimatika
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

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.ojalgo.TestUtils;
import org.ojalgo.ann.ArtificialNeuralNetwork.Activator;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.data.DataBatch;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Uniform;

public class BatchTest extends ANNTest {

    private static final int BATCH_SIZE = 6;
    private static final int NB_INPUTS = 4;
    private static final int NB_OUTPUTS = 2;

    static ArtificialNeuralNetwork generate() {
        return ArtificialNeuralNetwork.builder(Primitive64Store.FACTORY, NB_INPUTS).layer(3, Activator.RECTIFIER).layer(5, Activator.SIGMOID)
                .layer(NB_OUTPUTS, Activator.SOFTMAX).get();
    }

    @TempDir
    File dirBatchTest;

    @Test
    public void testInvoke() {

        ArtificialNeuralNetwork ann = BatchTest.generate();

        NetworkInvoker invSngl = ann.newInvoker();
        NetworkInvoker invBtch = ann.newInvoker(BATCH_SIZE);

        DataBatch batch = invBtch.newInputBatch();

        Primitive64Array input;
        MatrixStore<?>[] expected = new MatrixStore<?>[BATCH_SIZE];

        for (int i = 0; i < BATCH_SIZE; i++) {
            input = Primitive64Array.make(NB_INPUTS);
            input.fillAll(Uniform.standard());
            batch.addRow(input);

            expected[i] = invSngl.invoke(input).copy();
        }

        MatrixStore<Double> actual = invBtch.invoke(batch);

        for (int i = 0; i < BATCH_SIZE; i++) {
            TestUtils.assertEquals(expected[i], actual.sliceRow(i));
        }
    }

    /**
     * Create batches of identical training examples (equivalent to training multiple times using the same
     * examples) and adjust the learning rate to get the exact same results.
     */
    @Test
    public void testTrainBatchOfEquals() {

        ArtificialNeuralNetwork ann1 = BatchTest.generate();
        NetworkTrainer trnSngl = ann1.newTrainer().rate(1.0);
        NetworkInvoker invSngl = ann1.newInvoker();

        ArtificialNeuralNetwork ann2 = this.copy(ann1);
        NetworkTrainer trnBtch = ann2.newTrainer(BATCH_SIZE).rate(1.0 / BATCH_SIZE);
        NetworkInvoker invBtch = ann2.newInvoker();

        DataBatch inputBatch = trnBtch.newInputBatch();
        DataBatch outputBatch = trnBtch.newOutputBatch();

        for (int l = 0; l < 10; l++) {

            inputBatch.reset();
            outputBatch.reset();

            Primitive64Array input = Primitive64Array.make(NB_INPUTS);
            input.fillAll(Uniform.standard());

            Primitive64Array output = Primitive64Array.make(NB_OUTPUTS);
            output.set(l % NB_OUTPUTS, 1.0);

            for (int b = 0; b < BATCH_SIZE; b++) {
                inputBatch.addRow(input);
                outputBatch.addRow(output);
            }

            trnSngl.train(input, output);
            trnBtch.train(inputBatch, outputBatch);

            MatrixStore<Double> outSngl = invSngl.invoke(input);
            MatrixStore<Double> outBtch = invBtch.invoke(input);

            TestUtils.assertEquals(outSngl, outBtch);
        }
    }

    private ArtificialNeuralNetwork copy(final ArtificialNeuralNetwork source) {
        File copyFile = new File(dirBatchTest, "copy.ann");
        source.writeTo(copyFile);
        return ArtificialNeuralNetwork.from(copyFile);
    }

}
