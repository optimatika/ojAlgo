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
import org.ojalgo.array.operation.FillMatchingSingle;

/**
 * Filling an {@link R064Store} with a transposed (or conjugated) matrix puts every element in its mirrored
 * position: from another R064Store directly between the arrays, column by column up to 120 rows and in tiles
 * above that, and from any other store element by element – single-threaded, and multi-threaded with the
 * columns divided between the threads.
 */
public class FillMatchingTransposedTest extends MatrixStoreTests {

    /**
     * Rows and columns of the matrix filled, on both sides of the switch to tiles (more than 120 rows), with
     * the last tile partial, non-square, and with a power of 2 rows.
     */
    private static final int[][] SHAPES = { { 1, 1 }, { 1, 7 }, { 7, 1 }, { 5, 3 }, { 120, 9 }, { 121, 9 }, { 9, 121 }, { 128, 128 }, { 130, 70 }, { 200, 300 },
            { 300, 200 }, { 512, 512 } };

    private static void assertMirrored(final String message, final R064Store source, final R064Store destination) {
        for (int j = 0; j < destination.getColDim(); j++) {
            for (int i = 0; i < destination.getRowDim(); i++) {
                TestUtils.assertEquals(message + " (" + i + "," + j + ")", source.doubleValue(j, i), destination.doubleValue(i, j), 0.0);
            }
        }
    }

    private static void doTest(final int threshold) {

        int original = FillMatchingSingle.THRESHOLD;
        FillMatchingSingle.THRESHOLD = threshold;

        try {

            for (int[] shape : SHAPES) {

                int nbRows = shape[0];
                int nbCols = shape[1];

                R064Store source = R064Store.FACTORY.make(nbCols, nbRows);
                for (int i = 0, limit = nbRows * nbCols; i < limit; i++) {
                    source.set(i, i + 1);
                }

                String message = nbRows + "x" + nbCols + " threshold=" + threshold;

                R064Store transposed = R064Store.FACTORY.make(nbRows, nbCols);
                transposed.fillMatching(source.transpose());
                FillMatchingTransposedTest.assertMirrored("transposed " + message, source, transposed);

                R064Store conjugated = R064Store.FACTORY.make(nbRows, nbCols);
                conjugated.fillMatching(source.conjugate());
                FillMatchingTransposedTest.assertMirrored("conjugated " + message, source, conjugated);

                R064Store fromOther = R064Store.FACTORY.make(nbRows, nbCols);
                fromOther.fillMatching(source.limits(nbCols, nbRows).transpose());
                FillMatchingTransposedTest.assertMirrored("from other " + message, source, fromOther);
            }

        } finally {
            FillMatchingSingle.THRESHOLD = original;
        }
    }

    @Test
    public void testMultiThreaded() {
        FillMatchingTransposedTest.doTest(2);
    }

    @Test
    public void testSingleThreaded() {
        FillMatchingTransposedTest.doTest(Integer.MAX_VALUE);
    }

}
