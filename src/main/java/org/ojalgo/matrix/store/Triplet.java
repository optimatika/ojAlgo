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
package org.ojalgo.matrix.store;

import java.util.Comparator;

/**
 * Represents a non-zero element in a sparse matrix using a coordinate format (COO). Each triplet stores the
 * row index, column index, and value of a non-zero element.
 * <p>
 * This class is used primarily for building sparse matrices and converting between different sparse matrix
 * formats. It implements {@link Comparable} to support sorting elements in column-major order by default,
 * with a static comparator {@link #ROW_MAJOR} available for row-major ordering.
 * </p>
 * <p>
 * The natural ordering (column-major) is defined as:
 * <ol>
 * <li>First by column index</li>
 * <li>Then by row index</li>
 * <li>Finally by value (in descending order)</li>
 * </ol>
 * </p>
 *
 * @author apete
 */
public final class Triplet implements Comparable<Triplet> {

    /**
     * Comparator for sorting triplets in row-major order. The ordering is defined as:
     * <ol>
     * <li>First by row index</li>
     * <li>Then by column index</li>
     * <li>Finally by value (in descending order)</li>
     * </ol>
     */
    static final Comparator<Triplet> ROW_MAJOR = (obj, ref) -> {

        int retVal = Integer.compare(obj.row, ref.row);

        if (retVal == 0) {
            retVal = Integer.compare(obj.col, ref.col);
        }

        if (retVal == 0) {
            retVal = Double.compare(ref.value, obj.value);
        }

        return retVal;
    };

    /** The column index of the non-zero element */
    public final int col;
    /** The row index of the non-zero element */
    public final int row;
    /** The value of the non-zero element */
    public final double value;

    /**
     * Creates a new triplet representing a non-zero element in a sparse matrix.
     *
     * @param row   The row index of the element
     * @param col   The column index of the element
     * @param value The value of the element
     */
    Triplet(final int row, final int col, final double value) {
        super();
        this.row = row;
        this.col = col;
        this.value = value;
    }

    /**
     * Compares this triplet with another in column-major order. The comparison is based on:
     * <ol>
     * <li>Column index</li>
     * <li>Row index</li>
     * <li>Value (in descending order)</li>
     * </ol>
     *
     * @param ref The triplet to compare with
     * @return A negative integer, zero, or a positive integer as this triplet is less than, equal to, or
     *         greater than the specified triplet
     */
    @Override
    public int compareTo(final Triplet ref) {

        int retVal = Integer.compare(col, ref.col);

        if (retVal == 0) {
            retVal = Integer.compare(row, ref.row);
        }

        if (retVal == 0) {
            retVal = Double.compare(ref.value, value);
        }

        return retVal;
    }

    /**
     * Indicates whether some other object is equal to this triplet. Two triplets are considered equal if they
     * have the same row index, column index, and value.
     *
     * @param obj The reference object with which to compare
     * @return {@code true} if this object is equal to the obj argument; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Triplet)) {
            return false;
        }
        Triplet other = (Triplet) obj;
        if (col != other.col) {
            return false;
        }
        if (row != other.row) {
            return false;
        }
        if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a hash code value for this triplet. The hash code is computed using the row index, column
     * index, and value.
     *
     * @return A hash code value for this triplet
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + col;
        result = prime * result + row;
        long temp;
        temp = Double.doubleToLongBits(value);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

}
