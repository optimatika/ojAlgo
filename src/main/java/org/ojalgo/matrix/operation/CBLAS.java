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

import java.lang.foreign.MemorySegment;

import org.ojalgo.array.operation.NativeLibrary;

/**
 * Helpers for calling the native CBLAS functions on matrices: the CBLAS enum values, as passed (as
 * {@code int}), and heap segments covering a matrix. The Java API of the BLAS/LAPACK operations uses booleans
 * for the options; they are converted only when a native function is called.
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
     * {@code CblasNonUnit}
     */
    public static final int NON_UNIT = 131;
    /**
     * {@code CblasNoTrans}
     */
    public static final int NO_TRANSPOSE = 111;
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
     * @return {@link #UNIT} or {@link #NON_UNIT}
     */
    public static int diagonal(final boolean unitDiagonal) {
        return unitDiagonal ? UNIT : NON_UNIT;
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
