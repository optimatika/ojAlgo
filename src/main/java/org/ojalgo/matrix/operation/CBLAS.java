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

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

import org.ojalgo.array.operation.NativeLibrary;

/**
 * Helpers for calling the native CBLAS functions on matrices: the CBLAS enum values, as passed (as
 * {@code int}), heap segments covering a matrix (for critical functions), and copies of matrices in native
 * memory (for functions that are not critical). The Java API of the BLAS/LAPACK operations uses booleans for
 * the options; they are converted only when a native function is called.
 *
 * @see NativeLibrary
 */
public final class CBLAS {

    /**
     * {@code CblasColMajor} – ojAlgo always calls with column-major data.
     */
    public static final int COLUMN_MAJOR = 102;
    /**
     * {@code CblasConjTrans}
     */
    public static final int CONJUGATE_TRANSPOSE = 113;
    /**
     * {@code CblasLeft}
     */
    public static final int LEFT = 141;
    /**
     * {@code CblasLower}
     */
    public static final int LOWER = 122;
    /**
     * {@code CblasNoTrans}
     */
    public static final int NO_TRANSPOSE = 111;
    /**
     * {@code CblasNonUnit}
     */
    public static final int NON_UNIT = 131;
    /**
     * {@code CblasRight}
     */
    public static final int RIGHT = 142;
    /**
     * {@code CblasRowMajor}
     */
    public static final int ROW_MAJOR = 101;
    /**
     * {@code CblasTrans}
     */
    public static final int TRANSPOSE = 112;
    /**
     * {@code CblasUnit}
     */
    public static final int UNIT = 132;
    /**
     * {@code CblasUpper}
     */
    public static final int UPPER = 121;

    /**
     * Native copies of matrices are aligned to this many bytes.
     */
    private static final long ALIGNMENT = 64;

    /**
     * Native memory for a column-major matrix with leading dimension {@code leadingDimension}, for a function
     * that is not critical to write to. The memory is zero, and aligned to {@link #ALIGNMENT} bytes.
     */
    public static MemorySegment allocateMatrix(final SegmentAllocator allocator, final int rows, final int columns, final int leadingDimension) {
        long count = columns == 0 ? 0L : (columns - 1L) * leadingDimension + rows;
        return allocator.allocate(JAVA_DOUBLE.byteSize() * count, ALIGNMENT);
    }

    /**
     * Copies a column-major matrix in native memory, with leading dimension {@code copyLeadingDimension},
     * back to a Java array, where it is stored from {@code offset} with leading dimension
     * {@code leadingDimension}: the inverse of
     * {@link #copyOfMatrix(SegmentAllocator, double[], int, int, int, int, int)}. Only the elements of the
     * matrix are written, not those between its columns.
     */
    public static void copyBackMatrix(final MemorySegment copy, final int copyLeadingDimension, final double[] array, final int offset, final int rows,
            final int columns, final int leadingDimension) {
        if (columns == 1 || leadingDimension == rows && copyLeadingDimension == rows) {
            MemorySegment.copy(copy, JAVA_DOUBLE, 0L, array, offset, rows * columns);
        } else {
            for (int j = 0; j < columns; j++) {
                MemorySegment.copy(copy, JAVA_DOUBLE, JAVA_DOUBLE.byteSize() * j * copyLeadingDimension, array, offset + j * leadingDimension, rows);
            }
        }
    }

    /**
     * Copies the upper or lower triangle (with the diagonal) of an n-by-n column-major matrix in native
     * memory back to a Java array: the part of a symmetric matrix that the native function updated. The other
     * triangle is not written.
     *
     * @see #copyBackMatrix(MemorySegment, int, double[], int, int, int, int)
     */
    public static void copyBackTriangle(final MemorySegment copy, final int copyLeadingDimension, final double[] array, final int offset, final int n,
            final int leadingDimension, final boolean upper) {
        for (int j = 0; j < n; j++) {
            int first = upper ? 0 : j;
            int count = upper ? j + 1 : n - j;
            MemorySegment.copy(copy, JAVA_DOUBLE, JAVA_DOUBLE.byteSize() * ((long) j * copyLeadingDimension + first), array,
                    offset + first + j * leadingDimension, count);
        }
    }

    /**
     * Native memory holding a copy of a column-major matrix, stored in a Java array from {@code offset} with
     * leading dimension {@code leadingDimension}, for passing to a function that is not critical. The copy
     * has leading dimension {@code copyLeadingDimension}, normally {@link #leadingDimensionOfCopy(int)}, and
     * is aligned as {@link #allocateMatrix(SegmentAllocator, int, int, int)}. The elements are checked to be
     * within the array.
     *
     * @throws IndexOutOfBoundsException if the elements are not all within the array
     */
    public static MemorySegment copyOfMatrix(final SegmentAllocator allocator, final double[] array, final int offset, final int rows, final int columns,
            final int leadingDimension, final int copyLeadingDimension) {
        MemorySegment retVal = CBLAS.allocateMatrix(allocator, rows, columns, copyLeadingDimension);
        if (columns == 1 || leadingDimension == copyLeadingDimension) {
            MemorySegment.copy(array, offset, retVal, JAVA_DOUBLE, 0L, Math.toIntExact(retVal.byteSize() / JAVA_DOUBLE.byteSize()));
            return retVal;
        }
        for (int j = 0; j < columns; j++) {
            MemorySegment.copy(array, offset + j * leadingDimension, retVal, JAVA_DOUBLE, JAVA_DOUBLE.byteSize() * j * copyLeadingDimension, rows);
        }
        return retVal;
    }

    /**
     * @return {@link #UNIT} or {@link #NON_UNIT}
     */
    public static int diagonal(final boolean unitDiagonal) {
        return unitDiagonal ? UNIT : NON_UNIT;
    }

    /**
     * The leading dimension to give a copy, in native memory, of a matrix with {@code rows} rows:
     * {@code rows}, unless that is within 2 of a multiple of 64. Then it is the smallest odd multiple of 8
     * that is not less than {@code rows} – 8 modulo 16, and so at least 8 from any multiple of 16.
     *
     * @see #copyOfMatrix(SegmentAllocator, double[], int, int, int, int, int)
     */
    public static int leadingDimensionOfCopy(final int rows) {
        int remainder = rows % 64;
        if (remainder > 2 && remainder < 62) {
            return rows;
        }
        int multiple = (rows + 7) / 8 * 8;
        return multiple % 16 == 0 ? multiple + 8 : multiple;
    }

    /**
     * A heap segment for passing a column-major matrix, stored in a Java array from {@code offset} with
     * leading dimension {@code leadingDimension}, to a critical function. Covers all elements the native
     * function may access, and checks that they are within the array.
     *
     * @throws IndexOutOfBoundsException if the elements are not all within the array
     */
    public static MemorySegment ofMatrix(final double[] array, final int offset, final int rows, final int columns, final int leadingDimension) {
        long count = rows == 0 || columns == 0 ? 0L : (columns - 1L) * leadingDimension + rows;
        return NativeLibrary.ofArray(array, offset, count);
    }

    /**
     * @return {@link #LEFT} or {@link #RIGHT}
     */
    public static int side(final boolean left) {
        return left ? LEFT : RIGHT;
    }

    /**
     * @return {@link #TRANSPOSE} or {@link #NO_TRANSPOSE}
     */
    public static int transpose(final boolean transpose) {
        return transpose ? TRANSPOSE : NO_TRANSPOSE;
    }

    /**
     * @return {@link #UPPER} or {@link #LOWER}
     */
    public static int triangle(final boolean upper) {
        return upper ? UPPER : LOWER;
    }

    private CBLAS() {
        super();
    }

}
