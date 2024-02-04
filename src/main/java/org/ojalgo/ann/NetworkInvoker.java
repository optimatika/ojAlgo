/*
 * Copyright 1997-2024 Optimatika
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

import org.ojalgo.data.DataBatch;
import org.ojalgo.function.BasicFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.structure.Access1D;

public class NetworkInvoker extends WrappedANN implements BasicFunction.PlainUnary<Access1D<Double>, MatrixStore<Double>> {

    NetworkInvoker(final ArtificialNeuralNetwork network, final int batchSize) {
        super(network, batchSize);
    }

    /**
     * The input argument is typed as {@link Access1D} which essentially means it can be anything. If the
     * batch size is anything other than 1 this needs to be a 2D structure with the number of rows matching
     * the batch size. The return type is a {@link MatrixStore} where the number of rows match the batch size
     * and the number of columns match the number of output nodes. A {@link DataBatch} may help when creating
     * the batches or you simply create any 2D data structure and fill the rows.
     *
     * @see org.ojalgo.function.BasicFunction.PlainUnary#invoke(java.lang.Object)
     */
    @Override
    public MatrixStore<Double> invoke(final Access1D<Double> input) {
        return super.invoke(input, null);
    }

}
