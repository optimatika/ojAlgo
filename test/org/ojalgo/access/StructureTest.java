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
package org.ojalgo.access;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.random.Uniform;

public class StructureTest {

    static void backAndForth(long index, long[] structure, long[] reference) {
        TestUtils.assertEquals(index, StructureAnyD.index(structure, reference));
        TestUtils.assertEquals(StructureAnyD.reference(index, structure), reference);
    }

    @Test
    public void access2D() {

        final long tmpCountRows = 1 + Uniform.randomInteger(10);
        final long tmpCountColumns = 1 + Uniform.randomInteger(10);
        final long tmpCount = tmpCountRows * tmpCountColumns;

        final long[] tmpStructure = new long[] { tmpCountRows, tmpCountColumns };
        TestUtils.assertEquals(tmpCount, StructureAnyD.count(tmpStructure));

        final long tmpExpIndex = Uniform.randomInteger(tmpCount);

        final long tmpRow = Structure2D.row(tmpExpIndex, tmpStructure);
        final long tmpColumn = Structure2D.column(tmpExpIndex, tmpStructure);

        final long[] tmpReference = new long[] { tmpRow, tmpColumn };
        TestUtils.assertEquals(tmpReference, StructureAnyD.reference(tmpExpIndex, tmpStructure));

        TestUtils.assertEquals(tmpExpIndex, StructureAnyD.index(tmpStructure, tmpReference));
        TestUtils.assertEquals(tmpExpIndex, Structure2D.index(tmpCountRows, tmpRow, tmpColumn));

        final long tmpExpRow = Uniform.randomInteger(tmpCountRows);
        final long tmpExpColumn = Uniform.randomInteger(tmpCountColumns);

        final long tmpIndex1 = Structure2D.index(tmpCountRows, tmpExpRow, tmpExpColumn);
        final long tmpIndex2 = Structure2D.index(tmpCountRows, tmpExpRow, tmpExpColumn);
        TestUtils.assertEquals(tmpIndex1, tmpIndex2);

        TestUtils.assertEquals(tmpExpRow, Structure2D.row(tmpIndex1, tmpStructure));
        TestUtils.assertEquals(tmpExpColumn, Structure2D.column(tmpIndex1, tmpStructure));
    }

    @Test
    public void accessAnyD() {

        final long[] tmpStructure = new long[] { 3, 3, 4, 2 };

        final long tmpExpCount = 72;
        TestUtils.assertEquals(tmpExpCount, StructureAnyD.count(tmpStructure));

        final long tmpMatchingInd1 = 50;
        final long[] tmpMatchingRef1 = new long[] { 2, 1, 1, 1 };

        TestUtils.assertEquals(tmpMatchingInd1, StructureAnyD.index(tmpStructure, tmpMatchingRef1));
        TestUtils.assertEquals(tmpMatchingRef1, StructureAnyD.reference(tmpMatchingInd1, tmpStructure));

        final long tmpMatchingInd2 = 49;
        final long[] tmpMatchingRef2 = new long[] { 1, 1, 1, 1 };

        TestUtils.assertEquals(tmpMatchingInd2, StructureAnyD.index(tmpStructure, tmpMatchingRef2));
        TestUtils.assertEquals(tmpMatchingRef2, StructureAnyD.reference(tmpMatchingInd2, tmpStructure));

        final long tmpMatchingInd3 = 71; // 1x36 + 3x9 + 2x3 + 2x1 == 36 + 27 + 6 +2 == 71
        final long[] tmpMatchingRef3 = new long[] { 2, 2, 3, 1 };

        TestUtils.assertEquals(tmpMatchingInd3, StructureAnyD.index(tmpStructure, tmpMatchingRef3));
        TestUtils.assertEquals(tmpMatchingRef3, StructureAnyD.reference(tmpMatchingInd3, tmpStructure));
    }

    @Test
    public void testAnyD53421() {

        long[] structure = new long[] { 5, 3, 4, 2, 1 };

        StructureTest.backAndForth(0, structure, new long[] { 0, 0, 0, 0, 0 });
        StructureTest.backAndForth(15, structure, new long[] { 0, 0, 1, 0, 0 });
        StructureTest.backAndForth(30, structure, new long[] { 0, 0, 2, 0, 0 });
        StructureTest.backAndForth(45, structure, new long[] { 0, 0, 3, 0, 0 });
        StructureTest.backAndForth(60, structure, new long[] { 0, 0, 0, 1, 0 });
        StructureTest.backAndForth(75, structure, new long[] { 0, 0, 1, 1, 0 });
        StructureTest.backAndForth(90, structure, new long[] { 0, 0, 2, 1, 0 });
        StructureTest.backAndForth(105, structure, new long[] { 0, 0, 3, 1, 0 });

        StructureTest.backAndForth(0, structure, new long[] { 0, 0, 0, 0, 0 });
        StructureTest.backAndForth(81, structure, new long[] { 1, 1, 1, 1, 0 });
        StructureTest.backAndForth(38, structure, new long[] { 3, 1, 2, 0, 0 });
        StructureTest.backAndForth(119, structure, new long[] { 4, 2, 3, 1, 0 });
    }

}
