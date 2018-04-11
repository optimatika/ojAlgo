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
import org.ojalgo.access.StructureAnyD;
import org.ojalgo.function.aggregator.Aggregator;

public class ReductionTest {

    public ReductionTest() {
        super();
    }

    @Test
    public void test2To1D() {

        Array2D<Double> array = Array2D.PRIMITIVE64.makeZero(5, 3);
        array.fillAll(1.0);

        Array1D<Double> reducedRows = array.reduceRows(Aggregator.SUM);
        TestUtils.assertEquals(5, reducedRows.count());
        for (int i = 0; i < reducedRows.length; i++) {
            TestUtils.assertEquals(3, reducedRows.doubleValue(i));
        }

        Array1D<Double> reducedColumns = array.reduceColumns(Aggregator.SUM);
        TestUtils.assertEquals(3, reducedColumns.count());
        for (int i = 0; i < reducedColumns.length; i++) {
            TestUtils.assertEquals(5, reducedColumns.doubleValue(i));
        }
    }

    @Test
    public void testAnyTo1D() {

        long[] structure = new long[] { 5, 3, 4, 2, 1 };

        double total = StructureAnyD.count(structure);

        ArrayAnyD<Double> array = ArrayAnyD.PRIMITIVE64.makeZero(structure);
        array.fillAll(1.0);

        for (int d = 0; d < structure.length; d++) {
            Array1D<Double> reduced = array.reduce(d, Aggregator.SUM);
            TestUtils.assertEquals(structure[d], reduced.count());
            double expected = total / structure[d];
            for (int i = 0; i < reduced.length; i++) {
                TestUtils.assertEquals(expected, reduced.doubleValue(i));
            }
        }
    }

    @Test
    public void testAnyTo2D() {

        long[] structure = new long[] { 6, 5, 3, 4, 2, 1 };

        double total = StructureAnyD.count(structure);

        ArrayAnyD<Double> array = ArrayAnyD.PRIMITIVE64.makeZero(structure);
        array.fillAll(1.0);

        for (int rd = 0; rd < structure.length; rd++) {
            for (int cd = 0; cd < structure.length; cd++) {
                if (rd != cd) {
                    Array2D<Double> reduced = array.reduce(rd, cd, Aggregator.SUM);
                    TestUtils.assertEquals(structure[rd], reduced.countRows());
                    TestUtils.assertEquals(structure[cd], reduced.countColumns());
                    TestUtils.assertEquals(structure[rd] * structure[cd], reduced.count());
                    double expected = total / (structure[rd] * structure[cd]);
                    for (int i = 0; i < reduced.countRows(); i++) {
                        for (int j = 0; j < reduced.countColumns(); j++) {
                            TestUtils.assertEquals(expected, reduced.doubleValue(i, j));
                        }
                    }
                }
            }
        }
    }

}
