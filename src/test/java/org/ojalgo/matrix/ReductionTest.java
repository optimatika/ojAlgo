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
package org.ojalgo.matrix;

import static org.ojalgo.function.aggregator.Aggregator.AVERAGE;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class ReductionTest {

    public ReductionTest() {
        super();
    }

    /**
     * https://stackoverflow.com/questions/49682219/how-to-get-average-of-elements-along-columns-or-rows-in-an-ojalgo-matrix
     * <br>
     * https://github.com/optimatika/ojAlgo/issues/91#issuecomment-379399975
     */
    @Test
    public void testReduction() {

        MatrixR064.DenseReceiver builder = MatrixR064.FACTORY.makeDense(2, 3);
        builder.set(0, 0, 1.0);
        builder.set(0, 1, 2.0);
        builder.set(0, 2, 3.0);
        builder.set(1, 0, 4.0);
        builder.set(1, 1, 5.0);
        builder.set(1, 2, 6.0);
        MatrixR064 matrix = builder.get();

        MatrixR064 reducedRows = matrix.reduceRows(AVERAGE);
        MatrixR064 reducedColumns = matrix.reduceColumns(AVERAGE);

        TestUtils.assertEquals(2, reducedRows.count());
        TestUtils.assertEquals(2, reducedRows.countRows());
        TestUtils.assertEquals(1, reducedRows.countColumns());

        TestUtils.assertEquals(2.0, reducedRows.doubleValue(0, 0));
        TestUtils.assertEquals(5.0, reducedRows.doubleValue(1, 0));

        TestUtils.assertEquals(2.0, reducedRows.doubleValue(0));
        TestUtils.assertEquals(5.0, reducedRows.doubleValue(1));

        TestUtils.assertEquals(3, reducedColumns.count());
        TestUtils.assertEquals(1, reducedColumns.countRows());
        TestUtils.assertEquals(3, reducedColumns.countColumns());

        TestUtils.assertEquals(2.5, reducedColumns.doubleValue(0, 0));
        TestUtils.assertEquals(3.5, reducedColumns.doubleValue(0, 1));
        TestUtils.assertEquals(4.5, reducedColumns.doubleValue(0, 2));

        TestUtils.assertEquals(2.5, reducedColumns.doubleValue(0));
        TestUtils.assertEquals(3.5, reducedColumns.doubleValue(1));
        TestUtils.assertEquals(4.5, reducedColumns.doubleValue(2));
    }

}
