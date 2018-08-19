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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.PrimitiveFunction.Unary;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.type.context.NumberContext;

public class DesignTestANN extends ANNTest {

    public DesignTestANN() {
        super();
    }

    @Test
    public void testSoftmaxWikipediaCase() {

        NumberContext precision = new NumberContext(7, 8);

        PrimitiveDenseStore arguments = PrimitiveDenseStore.FACTORY.row(1.0, 2.0, 3.0, 4.0, 1.0, 2.0, 3.0);
        PrimitiveDenseStore results = PrimitiveDenseStore.FACTORY.row(0.02364054, 0.06426166, 0.1746813, 0.474833, 0.02364054, 0.06426166, 0.1746813);

        Unary function = ArtificialNeuralNetwork.Activator.SOFTMAX.getFunction(arguments);

        for (int i = 0; i < results.count(); i++) {
            double expected = results.doubleValue(i);
            double actual = function.invoke(arguments.doubleValue(i));
            TestUtils.assertEquals(expected, actual, precision);
        }
    }

}
