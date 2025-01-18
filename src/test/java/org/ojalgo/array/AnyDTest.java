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
package org.ojalgo.array;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.random.Uniform;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.AccessAnyD;
import org.ojalgo.structure.AccessAnyD.MatrixView;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.structure.StructureAnyD;

public class AnyDTest extends ArrayTests {

    @Test
    public void testMatrices() {

        long[] shape = { 4, 9, 5 };

        ArrayAnyD<Double> array = ArrayAnyD.R064.makeFilled(shape, Uniform.standard());

        Eigenvalue<Double> evd = Eigenvalue.R064.make(false);

        // Just verify that this compiles and runs without any (class cast) exceptions
        for (MatrixView<Double> matrix : array.matrices()) {
            evd.decompose(matrix);
            evd.getEigenpairs();
        }
    }

    @Test
    public void testSelecting() {

        long[] shape = { 4, 9, 5 };

        ArrayAnyD<Double> array = ArrayAnyD.R064.make(shape);

        Array1D<Double> sliced = array.sliceSet(new long[] { 2, 3, 0 }, 2);
        sliced.fillAll(2.0);

        long[] correct = new long[5];
        long[] wrong = new long[5];
        for (int i = 0; i < correct.length; i++) {
            correct[i] = StructureAnyD.index(shape, new long[] { 2, 3, i });
            wrong[i] = StructureAnyD.index(shape, new long[] { 3, 2, i });
        }

        Access1D<Double> selected = array.select(correct);

        TestUtils.assertEquals(5, selected.size());
        TestUtils.assertEquals(2.0, selected.doubleValue(0));
        TestUtils.assertEquals(2.0, selected.doubleValue(1));
        TestUtils.assertEquals(2.0, selected.doubleValue(2));
        TestUtils.assertEquals(2.0, selected.doubleValue(3));
        TestUtils.assertEquals(2.0, selected.doubleValue(4));

        long indexWithinEachMatrix = Structure2D.index(4, 2, 3);
        for (MatrixView<Double> matrix : array.matrices()) {
            Access1D<Double> justOne = matrix.select(indexWithinEachMatrix);
            TestUtils.assertEquals(1, justOne.size());
            TestUtils.assertEquals(2.0, justOne.doubleValue(0));
        }

        selected = array.select(wrong);

        TestUtils.assertEquals(5, selected.size());
        TestUtils.assertEquals(0.0, selected.doubleValue(0));
        TestUtils.assertEquals(0.0, selected.doubleValue(1));
        TestUtils.assertEquals(0.0, selected.doubleValue(2));
        TestUtils.assertEquals(0.0, selected.doubleValue(3));
        TestUtils.assertEquals(0.0, selected.doubleValue(4));

        array.fillAll(Uniform.standard());

        AccessAnyD<Double> selectedAnyD = array.select(new long[] { 2 }, new long[] { 1, 3 }, null, null);
        TestUtils.assertEquals(10, selectedAnyD.size());

        TestUtils.assertEquals(array.count(2), selectedAnyD.count(2));

        MatrixView<Double> fullMatrices = array.matrices();
        MatrixView<Double> filteredMatrices = selectedAnyD.matrices();

        fullMatrices.goToMatrix(4);
        filteredMatrices.goToMatrix(4);

        TestUtils.assertEquals(fullMatrices.doubleValue(2, 3), filteredMatrices.doubleValue(0, 1));
    }

    @Test
    public void testSlicing() {

        long[] shape = { 5, 9, 7 };

        ArrayAnyD<Double> array = ArrayAnyD.R032.make(shape);

        array.sliceSet(new long[] { 0, 0, 0 }, 0).size();

        TestUtils.assertEquals(5, array.sliceSet(new long[] { 0, 0, 0 }, 0).size());
        TestUtils.assertEquals(9, array.sliceSet(new long[] { 0, 0, 0 }, 1).size());
        TestUtils.assertEquals(7, array.sliceSet(new long[] { 0, 0, 0 }, 2).size());

        TestUtils.assertEquals(5, array.sliceSet(new long[] { 0, 3, 3 }, 0).size());
        TestUtils.assertEquals(9, array.sliceSet(new long[] { 3, 0, 3 }, 1).size());
        TestUtils.assertEquals(7, array.sliceSet(new long[] { 3, 3, 0 }, 2).size());

        Array1D<Double> eachMatrix_3_2 = array.sliceSet(new long[] { 3, 2, 0 }, 2);

        eachMatrix_3_2.fillAll(1.0);

        TestUtils.assertEquals(1.0, eachMatrix_3_2.doubleValue(0));
        TestUtils.assertEquals(1.0, eachMatrix_3_2.doubleValue(1));
        TestUtils.assertEquals(1.0, eachMatrix_3_2.doubleValue(2));
        TestUtils.assertEquals(1.0, eachMatrix_3_2.doubleValue(3));

        TestUtils.assertEquals(1.0, array.doubleValue(new long[] { 3, 2, 4 }));
        TestUtils.assertEquals(1.0, array.doubleValue(new long[] { 3, 2, 5 }));
        TestUtils.assertEquals(1.0, array.doubleValue(new long[] { 3, 2, 6 }));

        TestUtils.assertEquals(7, array.aggregateAll(Aggregator.CARDINALITY).intValue());

        Array1D<Double> col2_matrix4 = array.sliceSet(new long[] { 0, 2, 4 }, 0);

        TestUtils.assertEquals(0.0, col2_matrix4.doubleValue(2));
        TestUtils.assertEquals(1.0, col2_matrix4.doubleValue(3));
        TestUtils.assertEquals(0.0, col2_matrix4.doubleValue(4));

        int iterCount = 0;
        for (MatrixView<Double> matrix : array.matrices()) {
            TestUtils.assertEquals(0.0, matrix.doubleValue(2, 3));
            TestUtils.assertEquals(1.0, matrix.doubleValue(3, 2));
            TestUtils.assertEquals(iterCount, matrix.index());
            iterCount++;
        }
        TestUtils.assertEquals(7, iterCount);
    }

}
