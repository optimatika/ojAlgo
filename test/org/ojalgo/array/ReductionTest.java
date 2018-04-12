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

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.access.StructureAnyD;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.random.Normal;

public class ReductionTest {

    public ReductionTest() {
        super();
    }

    @Test
    public void test2To1D() {

        final Array2D<Double> array = Array2D.PRIMITIVE64.makeZero(5, 3);
        array.fillAll(1.0);

        final Array1D<Double> reducedRows = array.reduceRows(Aggregator.SUM);
        TestUtils.assertEquals(5, reducedRows.count());
        for (int i = 0; i < reducedRows.length; i++) {
            TestUtils.assertEquals(3, reducedRows.doubleValue(i));
        }

        final Array1D<Double> reducedColumns = array.reduceColumns(Aggregator.SUM);
        TestUtils.assertEquals(3, reducedColumns.count());
        for (int i = 0; i < reducedColumns.length; i++) {
            TestUtils.assertEquals(5, reducedColumns.doubleValue(i));
        }
    }

    @Test
    public void testAnyTo1D() {

        final long[] structure = new long[] { 5, 3, 4, 2, 1 };

        final double total = StructureAnyD.count(structure);

        final ArrayAnyD<Double> array = ArrayAnyD.PRIMITIVE64.makeZero(structure);
        array.fillAll(1.0);

        for (int d = 0; d < structure.length; d++) {
            final Array1D<Double> reduced = array.reduce(d, Aggregator.SUM);
            TestUtils.assertEquals(structure[d], reduced.count());
            final double expected = total / structure[d];
            for (int i = 0; i < reduced.length; i++) {
                TestUtils.assertEquals(expected, reduced.doubleValue(i));
            }
        }
    }

    @Test
    public void testAnyTo2D() {

        final long[] structure = new long[] { 6, 5, 3, 4, 2, 1 };

        final double total = StructureAnyD.count(structure);

        final ArrayAnyD<Double> array = ArrayAnyD.PRIMITIVE64.makeZero(structure);
        array.fillAll(1.0);

        for (int rd = 0; rd < structure.length; rd++) {
            for (int cd = 0; cd < structure.length; cd++) {
                if (rd != cd) {
                    final Array2D<Double> reduced = array.reduce(rd, cd, Aggregator.SUM);
                    TestUtils.assertEquals(structure[rd], reduced.countRows());
                    TestUtils.assertEquals(structure[cd], reduced.countColumns());
                    TestUtils.assertEquals(structure[rd] * structure[cd], reduced.count());
                    final double expected = total / (structure[rd] * structure[cd]);
                    for (int i = 0; i < reduced.countRows(); i++) {
                        for (int j = 0; j < reduced.countColumns(); j++) {
                            TestUtils.assertEquals(expected, reduced.doubleValue(i, j));
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testDifferentPaths() {

        final Random rnd = new Random();

        final long[] structure = new long[5];
        for (int d = 0; d < structure.length; d++) {
            structure[d] = 1 + rnd.nextInt(9);
        }

        final ArrayAnyD<Double> array = ArrayAnyD.PRIMITIVE64.makeZero(structure);
        array.fillAll(new Normal());

        for (int rd = 0; rd < structure.length; rd++) {
            for (int cd = 0; cd < structure.length; cd++) {
                if (rd != cd) {

                    final Array2D<Double> reduced2D = array.reduce(rd, cd, Aggregator.PRODUCT);

                    final Array1D<Double> reduced2DtoR = reduced2D.reduceRows(Aggregator.PRODUCT);
                    final Array1D<Double> reduced2DtoC = reduced2D.reduceColumns(Aggregator.PRODUCT);

                    final Array1D<Double> reducedDirectToR = array.reduce(rd, Aggregator.PRODUCT);
                    final Array1D<Double> reducedDirectToC = array.reduce(cd, Aggregator.PRODUCT);

                    TestUtils.assertEquals(reducedDirectToR, reduced2DtoR);
                    TestUtils.assertEquals(reducedDirectToC, reduced2DtoC);
                }
            }
        }
    }

}
