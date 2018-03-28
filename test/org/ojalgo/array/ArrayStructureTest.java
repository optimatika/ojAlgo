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

/**
 * AbstractArrayTest
 *
 * @author apete
 */
public class ArrayStructureTest extends ArrayTests {

    private static Primitive64Array ARRAY_1D;
    private static Primitive64Array ARRAY_2D;
    private static Primitive64Array ARRAY_3D;
    private static Primitive64Array ARRAY_4D;

    private static int[] STRUCTURE_1D;
    private static int[] STRUCTURE_2D;
    private static int[] STRUCTURE_3D;
    private static int[] STRUCTURE_4D;

    static {

        // 1
        int tmpSize = 1;

        STRUCTURE_1D = new int[tmpSize];

        for (int i = 0; i < STRUCTURE_1D.length; i++) {
            STRUCTURE_1D[i] = 2 - i;
        }

        ARRAY_1D = new Primitive64Array(StructureAnyD.count(STRUCTURE_1D));

        for (int i = 0; i < ARRAY_1D.size(); i++) {
            ARRAY_1D.set(i, Double.valueOf(i));
        }

        // 2
        tmpSize = 2;

        STRUCTURE_2D = new int[tmpSize];

        for (int i = 0; i < STRUCTURE_2D.length; i++) {
            STRUCTURE_2D[i] = 3 - i;
        }

        ARRAY_2D = new Primitive64Array(StructureAnyD.count(STRUCTURE_2D));

        for (int i = 0; i < ARRAY_2D.size(); i++) {
            ARRAY_2D.set(i, Double.valueOf(i));
        }

        // 3
        tmpSize = 3;

        STRUCTURE_3D = new int[tmpSize];

        for (int i = 0; i < STRUCTURE_3D.length; i++) {
            STRUCTURE_3D[i] = 4 - i;
        }

        ARRAY_3D = new Primitive64Array(StructureAnyD.count(STRUCTURE_3D));

        for (int i = 0; i < ARRAY_3D.size(); i++) {
            ARRAY_3D.set(i, Double.valueOf(i));
        }

        // 4
        tmpSize = 4;

        STRUCTURE_4D = new int[tmpSize];

        for (int i = 0; i < STRUCTURE_4D.length; i++) {
            STRUCTURE_4D[i] = 5 - i;
        }

        ARRAY_4D = new Primitive64Array(StructureAnyD.count(STRUCTURE_4D));

        for (int i = 0; i < ARRAY_4D.size(); i++) {
            ARRAY_4D.set(i, Double.valueOf(i));
        }
    }

    @Test
    public void testGetIndexintArray() {

        final int[] tmpRef1 = new int[] { 1 };
        final int[] tmpRef2 = new int[] { 2, 0 };
        final int[] tmpRef3 = new int[] { 3, 0, 1 };
        final int[] tmpRef4 = new int[] { 4, 0, 2, 1 };

        TestUtils.assertEquals(1, StructureAnyD.index(STRUCTURE_1D, tmpRef1));
        TestUtils.assertEquals(2, StructureAnyD.index(STRUCTURE_2D, tmpRef2));
        TestUtils.assertEquals(15, StructureAnyD.index(STRUCTURE_3D, tmpRef3));
        TestUtils.assertEquals(104, StructureAnyD.index(STRUCTURE_4D, tmpRef4));
    }

    @Test
    public void testGetNumberOfDimensions() {

        TestUtils.assertEquals(1, STRUCTURE_1D.length);
        TestUtils.assertEquals(2, STRUCTURE_2D.length);
        TestUtils.assertEquals(3, STRUCTURE_3D.length);
        TestUtils.assertEquals(4, STRUCTURE_4D.length);
    }

    @Test
    public void testGetNumberOfElements() {

        TestUtils.assertEquals(2, ARRAY_1D.size());
        TestUtils.assertEquals(6, ARRAY_2D.size());
        TestUtils.assertEquals(24, ARRAY_3D.size());
        TestUtils.assertEquals(120, ARRAY_4D.size());
    }

}
