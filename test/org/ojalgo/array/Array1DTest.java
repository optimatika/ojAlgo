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
package org.ojalgo.array;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

/**
 * @author apete
 */
public class Array1DTest {

    @Test
    public void testSortingEvenSizedArrayAscending() {

        final double[][] tmpRows = new double[][] { { 4, 5, 6, 7, 8, 9 }, { 9, 8, 7, 6, 5, 4 }, { 9, 5, 7, 6, 8, 4 }, { 4, 8, 6, 7, 5, 9 } };

        this.doSortTest(tmpRows);
    }

    @Test
    public void testSortingEvenSizedArrayDescending() {

        final double[][] tmpRows = new double[][] { { 9, 8, 7, 6, 5, 4 }, { 4, 5, 6, 7, 8, 9 }, { 9, 5, 7, 6, 8, 4 }, { 4, 8, 6, 7, 5, 9 } };

        this.doSortTest(tmpRows);
    }

    @Test
    public void testSortingOddSizedArrayAscending() {

        final double[][] tmpRows = new double[][] { { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, { 8, 1, 2, 3, 4, 5, 6, 7, 9 }, { 7, 8, 1, 2, 3, 4, 5, 6, 9 },
                { 6, 7, 8, 1, 2, 3, 4, 5, 9 }, { 5, 6, 7, 8, 1, 2, 3, 4, 9 }, { 4, 5, 6, 7, 8, 1, 2, 3, 9 }, { 3, 4, 5, 6, 7, 8, 1, 2, 9 },
                { 2, 3, 4, 5, 6, 7, 8, 1, 9 }, { 1, 6, 7, 8, 2, 3, 4, 5, 9 }, { 4, 5, 6, 7, 1, 2, 3, 8, 9 } };

        this.doSortTest(tmpRows);
    }

    @Test
    public void testSortingOddSizedArrayDescending() {

        final double[][] tmpRows = new double[][] { { 9, 8, 7, 6, 5, 4, 3, 2, 1 }, { 8, 1, 2, 3, 4, 5, 6, 7, 9 }, { 7, 8, 1, 2, 3, 4, 5, 6, 9 },
                { 6, 7, 8, 1, 2, 3, 4, 5, 9 }, { 5, 6, 7, 8, 1, 2, 3, 4, 9 }, { 4, 5, 6, 7, 8, 1, 2, 3, 9 }, { 3, 4, 5, 6, 7, 8, 1, 2, 9 },
                { 2, 3, 4, 5, 6, 7, 8, 1, 9 }, { 1, 6, 7, 8, 2, 3, 4, 5, 9 }, { 4, 5, 6, 7, 1, 2, 3, 8, 9 } };

        this.doSortTest(tmpRows);
    }

    private void doSortTest(final double[][] rows) {

        final Array1D<Double> tmpExpexted = Array1D.PRIMITIVE64.copy(rows[0]);

        final boolean tmpAscending = tmpExpexted.doubleValue(tmpExpexted.count() - 1L) > tmpExpexted.doubleValue(0L);

        final Array2D<Double> tmpAll = Array2D.PRIMITIVE64.rows(rows);

        for (int i = 0; i < tmpAll.countRows(); i++) {

            final Array1D<Double> tmpSlice = tmpAll.sliceRow(i, 0);
            final Array1D<Double> tmpCopy = tmpSlice.copy(); // Will result in a different (Java's built-in) sorting algorthm

            if (tmpAscending) {
                tmpSlice.sortAscending();
                tmpCopy.sortAscending();
            } else {
                tmpSlice.sortDescending();
                tmpCopy.sortDescending();
            }

            TestUtils.assertEquals(tmpExpexted, tmpSlice);
            TestUtils.assertEquals(tmpExpexted, tmpCopy);
        }
    }

}
