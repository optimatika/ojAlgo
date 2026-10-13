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
package org.ojalgo.matrix.operation;

import static java.lang.foreign.ValueLayout.JAVA_DOUBLE;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

/**
 * The layout of the matrix copies in native memory, made for the native functions that are not critical:
 * their leading dimension (padded only when the number of rows is within 2 of a multiple of 64), and the
 * copies to and from Java arrays with any leading dimension of the copy (with offsets, padding between the
 * columns, a single column, and a source with the same leading dimension as the copy).
 */
public class CBLASTest {

    private static final int OFFSET = 3;

    private static double[] newSource(final int rows, final int columns, final int ld) {
        double[] retVal = new double[OFFSET + ld * columns + 2];
        Arrays.fill(retVal, Double.NaN);
        for (int j = 0; j < columns; j++) {
            for (int i = 0; i < rows; i++) {
                retVal[OFFSET + i + j * ld] = 1 + i + 1000 * j;
            }
        }
        return retVal;
    }

    @Test
    public void testAllocateMatrix() {
        try (Arena arena = Arena.ofConfined()) {
            for (int rows : new int[] { 1, 7, 8, 16, 1000, 1024 }) {
                for (int columns : new int[] { 0, 1, 2, 5 }) {
                    for (int copyLD : new int[] { rows, rows + 3, CBLAS.leadingDimensionOfCopy(rows) }) {
                        long expected = columns == 0 ? 0L : (columns - 1L) * copyLD + rows;
                        TestUtils.assertEquals(expected, CBLAS.allocateMatrix(arena, rows, columns, copyLD).byteSize() / JAVA_DOUBLE.byteSize());
                    }
                }
            }
        }
    }

    @Test
    public void testCopyBackTriangle() {
        for (int n : new int[] { 5, 64 }) {
            for (boolean upper : new boolean[] { true, false }) {
                // { ld of the Java array, ld of the copy }
                for (int[] lds : new int[][] { { n, n }, { n + 4, n }, { n, n + 3 }, { n + 4, CBLAS.leadingDimensionOfCopy(n) } }) {

                    int ld = lds[0];
                    int copyLD = lds[1];
                    double[] source = CBLASTest.newSource(n, n, ld);

                    try (Arena arena = Arena.ofConfined()) {

                        MemorySegment copy = CBLAS.copyOfMatrix(arena, source, OFFSET, n, n, ld, copyLD);
                        for (int j = 0; j < n; j++) {
                            for (int i = 0; i < n; i++) {
                                copy.setAtIndex(JAVA_DOUBLE, i + (long) j * copyLD, -(1 + i + 1000 * j));
                            }
                        }

                        double[] target = source.clone();
                        CBLAS.copyBackTriangle(copy, copyLD, target, OFFSET, n, ld, upper);

                        String message = "n=" + n + " upper=" + upper + " ld=" + ld + " copy ld=" + copyLD;
                        for (int p = 0; p < target.length; p++) {
                            int i = (p - OFFSET) % ld;
                            int j = (p - OFFSET) / ld;
                            boolean triangle = p >= OFFSET && i < n && j < n && (upper ? i <= j : i >= j);
                            double expected = triangle ? -(1 + i + 1000 * j) : source[p];
                            TestUtils.assertEquals(message + " at " + p, expected, target[p], 0.0);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testCopyOfMatrixAndBack() {

        for (int rows : new int[] { 1, 5, 21, 62, 64, 66, 100, 128 }) {
            for (int columns : new int[] { 1, 2, 6 }) {

                int padded = CBLAS.leadingDimensionOfCopy(rows);

                // { ld of the Java array, ld of the copy }
                for (int[] lds : new int[][] { { rows, rows }, { rows + 2, rows }, { rows, rows + 3 }, { rows + 2, rows + 2 }, { rows, padded },
                        { padded, padded } }) {

                    int ld = lds[0];
                    int copyLD = lds[1];
                    String message = rows + "x" + columns + " ld=" + ld + " copy ld=" + copyLD;
                    double[] source = CBLASTest.newSource(rows, columns, ld);
                    double[] before = source.clone();

                    try (Arena arena = Arena.ofConfined()) {

                        MemorySegment copy = CBLAS.copyOfMatrix(arena, source, OFFSET, rows, columns, ld, copyLD);

                        TestUtils.assertEquals(message + " size", (columns - 1L) * copyLD + rows, copy.byteSize() / JAVA_DOUBLE.byteSize());
                        for (int j = 0; j < columns; j++) {
                            for (int i = 0; i < rows; i++) {
                                TestUtils.assertEquals(message, 1 + i + 1000 * j, copy.getAtIndex(JAVA_DOUBLE, i + (long) j * copyLD), 0.0);
                            }
                        }
                        TestUtils.assertTrue(message + " source changed", Arrays.equals(before, source));

                        for (int j = 0; j < columns; j++) {
                            for (int i = 0; i < rows; i++) {
                                copy.setAtIndex(JAVA_DOUBLE, i + (long) j * copyLD, -(1 + i + 1000 * j));
                            }
                        }
                        double[] target = source.clone();
                        CBLAS.copyBackMatrix(copy, copyLD, target, OFFSET, rows, columns, ld);

                        for (int p = 0; p < target.length; p++) {
                            int i = (p - OFFSET) % ld;
                            int j = (p - OFFSET) / ld;
                            boolean element = p >= OFFSET && i < rows && j < columns;
                            double expected = element ? -(1 + i + 1000 * j) : source[p];
                            TestUtils.assertEquals(message + " back at " + p, expected, target[p], 0.0);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testCopyOfMatrixOutOfBounds() {
        try (Arena arena = Arena.ofConfined()) {
            // The matrix extends to (columns - 1) * ld + rows elements from the offset, copied in one go when the
            // copy has the same leading dimension, otherwise column by column
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> CBLAS.copyOfMatrix(arena, new double[23], 0, 8, 3, 8, 8));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> CBLAS.copyOfMatrix(arena, new double[22], 0, 7, 3, 8, 7));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> CBLAS.copyOfMatrix(arena, new double[39], 0, 16, 2, 24, 24));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> CBLAS.copyOfMatrix(arena, new double[16], 0, 5, 3, 6, 5));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> CBLAS.copyOfMatrix(arena, new double[17], 1, 5, 3, 6, 5));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> CBLAS.copyOfMatrix(arena, new double[127], 0, 64, 2, 64, 72));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> CBLAS.copyOfMatrix(arena, new double[135], 0, 64, 2, 72, 72));
        }
    }

    @Test
    public void testLeadingDimensionOfCopy() {
        for (int rows = 0; rows <= 4100; rows++) {
            int ld = CBLAS.leadingDimensionOfCopy(rows);
            int remainder = rows % 64;
            if (remainder > 2 && remainder < 62) {
                TestUtils.assertEquals("rows " + rows, rows, ld);
            } else {
                TestUtils.assertTrue("rows " + rows, ld >= rows && ld % 16 == 8 && ld - 16 < rows);
            }
        }
        TestUtils.assertEquals(1000, CBLAS.leadingDimensionOfCopy(1000));
        TestUtils.assertEquals(1021, CBLAS.leadingDimensionOfCopy(1021));
        TestUtils.assertEquals(1032, CBLAS.leadingDimensionOfCopy(1022));
        TestUtils.assertEquals(1032, CBLAS.leadingDimensionOfCopy(1023));
        TestUtils.assertEquals(1032, CBLAS.leadingDimensionOfCopy(1024));
        TestUtils.assertEquals(1032, CBLAS.leadingDimensionOfCopy(1025));
        TestUtils.assertEquals(1032, CBLAS.leadingDimensionOfCopy(1026));
        TestUtils.assertEquals(1027, CBLAS.leadingDimensionOfCopy(1027));
        TestUtils.assertEquals(1400, CBLAS.leadingDimensionOfCopy(1400));
        TestUtils.assertEquals(1500, CBLAS.leadingDimensionOfCopy(1500));
        TestUtils.assertEquals(1544, CBLAS.leadingDimensionOfCopy(1536));
        TestUtils.assertEquals(2056, CBLAS.leadingDimensionOfCopy(2048));
    }

}
