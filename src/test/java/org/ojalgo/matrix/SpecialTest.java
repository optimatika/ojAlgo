/*
 * Copyright 1997-2025 Optimatika
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
package org.ojalgo.matrix;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class SpecialTest extends MatrixTests {

    @Test
    public void testCompareReceivers1() {

        MatrixQ128.DenseReceiver dense = MatrixQ128.FACTORY.newDenseBuilder(5, 7);
        MatrixR032.SparseReceiver sparse = MatrixR032.FACTORY.newSparseBuilder(5, 7);

        dense.set(1, 1, 1D);
        sparse.set(1, 1, 1D);

        dense.set(3, 5, 1D);
        sparse.set(3, 5, 1D);

        dense.set(4, 2, 1D);
        sparse.set(4, 2, 1D);

        TestUtils.assertEquals(dense.build(), sparse.build());
    }

    @Test
    public void testCompareReceivers2() {

        MatrixR064.DenseReceiver dense = MatrixR064.FACTORY.newDenseBuilder(5, 7);
        MatrixR128.SparseReceiver sparse = MatrixR128.FACTORY.newSparseBuilder(5, 7);

        dense.set(1, 1, 1D);
        sparse.set(1, 1, 1D);

        dense.set(3, 5, 1D);
        sparse.set(3, 5, 1D);

        dense.set(4, 2, 1D);
        sparse.set(4, 2, 1D);

        TestUtils.assertEquals(dense.build(), sparse.build());
    }

}
