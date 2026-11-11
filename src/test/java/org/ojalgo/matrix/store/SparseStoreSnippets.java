/*
 * Copyright 1997-2026 Optimatika
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
package org.ojalgo.matrix.store;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.structure.ElementView2D;

/**
 * The examples in the Javadoc of {@link SparseStore}, included with {@code {@snippet}}. The code between the
 * region markers is what the Javadoc shows; the assertions after it check that it still does what the
 * comments say.
 */
public class SparseStoreSnippets {

    @SuppressWarnings("unused")
    @Test
    public void testIntroduction() {

        // @start region="introduction"
        SparseStore<Double> S = SparseStore.R064.make(1000, 1000); // only the non-zero elements are stored

        S.set(0, 0, 4.0);
        S.set(0, 999, -1.0);
        S.set(999, 999, 2.0);

        for (ElementView2D<Double, ?> nonzero : S.nonzeros()) { // the 3 elements that were set
            long row = nonzero.row();
            long column = nonzero.column();
            double value = nonzero.doubleValue();
        }

        PhysicalStore<Double> x = R064Store.FACTORY.make(1000, 1);
        x.fillAll(1.0);

        MatrixStore<Double> Sx = S.multiply(x); // [3, 0, 0, ..., 0, 2]
        // @end

        int count = 0;
        for (ElementView2D<Double, ?> nonzero : S.nonzeros()) {
            count++;
        }

        TestUtils.assertEquals(3, count);
        TestUtils.assertEquals(3.0, Sx.doubleValue(0));
        TestUtils.assertEquals(0.0, Sx.doubleValue(500));
        TestUtils.assertEquals(2.0, Sx.doubleValue(999));
    }

}
