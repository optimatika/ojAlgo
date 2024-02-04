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
package org.ojalgo.array.operation;

import java.math.BigDecimal;

import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.type.TypeUtils;

public abstract class FillMatchingSingle implements ArrayOperation {

    public static int THRESHOLD = 256;

    public static void conjugate(final BigDecimal[] data, final int structure, final int firstColumn, final int limitColumn, final Access2D<?> source) {
        FillMatchingSingle.transpose(data, structure, firstColumn, limitColumn, source);
    }

    public static void conjugate(final double[] data, final int structure, final int firstColumn, final int limitColumn, final Access2D<?> source) {
        FillMatchingSingle.transpose(data, structure, firstColumn, limitColumn, source);
    }

    public static <N extends Scalar<N>> void conjugate(final N[] data, final int structure, final int firstColumn, final int limitColumn,
            final Access2D<?> source, final Scalar.Factory<N> scalar) {
        int index = structure * firstColumn;
        for (int j = firstColumn; j < limitColumn; j++) {
            for (int i = 0; i < structure; i++) {
                data[index++] = scalar.cast(source.get(j, i)).conjugate().get();
            }
        }
    }

    public static void copy(final BigDecimal[] data, final int structure, final int firstColumn, final int limitColumn,
            final Access2D<? extends Comparable<?>> source) {
        int index = structure * firstColumn;
        for (int j = firstColumn; j < limitColumn; j++) {
            for (int i = 0; i < structure; i++) {
                data[index++] = TypeUtils.toBigDecimal(source.get(i, j));
            }
        }
    }

    public static void copy(final double[] data, final int structure, final int firstColumn, final int limitColumn,
            final Access2D<? extends Comparable<?>> source) {
        int index = structure * firstColumn;
        for (int j = firstColumn; j < limitColumn; j++) {
            for (int i = 0; i < structure; i++) {
                data[index++] = source.doubleValue(i, j);
            }
        }
    }

    public static void copy(final float[] data, final int structure, final int firstColumn, final int limitColumn,
            final Access2D<? extends Comparable<?>> source) {
        int index = structure * firstColumn;
        for (int j = firstColumn; j < limitColumn; j++) {
            for (int i = 0; i < structure; i++) {
                data[index++] = source.floatValue(i, j);
            }
        }
    }

    public static <N extends Scalar<N>> void copy(final N[] data, final int structure, final int firstColumn, final int limitColumn, final Access2D<?> source,
            final Scalar.Factory<N> scalar) {
        int index = structure * firstColumn;
        for (int j = firstColumn; j < limitColumn; j++) {
            for (int i = 0; i < structure; i++) {
                data[index++] = scalar.cast(source.get(i, j));
            }
        }
    }

    public static void fill(final double[] data, final Access1D<?> values) {
        int limit = Math.min(data.length, values.size());
        for (int i = 0; i < limit; i++) {
            data[i] = values.doubleValue(i);
        }
    }

    public static void fill(final double[] data, final double[] values) {
        int limit = Math.min(data.length, values.length);
        for (int i = 0; i < limit; i++) {
            data[i] = values[i];
        }
    }

    public static void fill(final float[] data, final Access1D<?> values) {
        int limit = Math.min(data.length, values.size());
        for (int i = 0; i < limit; i++) {
            data[i] = values.floatValue(i);
        }
    }

    public static void fill(final float[] data, final float[] values) {
        int limit = Math.min(data.length, values.length);
        for (int i = 0; i < limit; i++) {
            data[i] = values[i];
        }
    }

    public static <N extends Comparable<N>> void fill(final N[] data, final Access1D<?> values, final Scalar.Factory<N> scalar) {
        int limit = Math.min(data.length, values.size());
        for (int i = 0; i < limit; i++) {
            data[i] = scalar.cast(values.get(i));
        }
    }

    public static void invoke(final double[] source, final int sourceOffset, final double[] destination, final int destinationOffset, final int first,
            final int limit) {
        for (int i = first; i < limit; i++) {
            destination[destinationOffset + i] = source[sourceOffset + i];
        }
    }

    public static void transpose(final BigDecimal[] data, final int structure, final int firstColumn, final int limitColumn, final Access2D<?> source) {
        int index = structure * firstColumn;
        for (int j = firstColumn; j < limitColumn; j++) {
            for (int i = 0; i < structure; i++) {
                data[index++] = TypeUtils.toBigDecimal(source.get(j, i));
            }
        }
    }

    public static void transpose(final double[] data, final int structure, final int firstColumn, final int limitColumn, final Access2D<?> source) {
        int index = structure * firstColumn;
        for (int j = firstColumn; j < limitColumn; j++) {
            for (int i = 0; i < structure; i++) {
                data[index++] = source.doubleValue(j, i);
            }
        }
    }

    public static void transpose(final float[] data, final int structure, final int firstColumn, final int limitColumn, final Access2D<?> source) {
        int index = structure * firstColumn;
        for (int j = firstColumn; j < limitColumn; j++) {
            for (int i = 0; i < structure; i++) {
                data[index++] = source.floatValue(j, i);
            }
        }
    }

    public static <N extends Scalar<N>> void transpose(final N[] data, final int structure, final int firstColumn, final int limitColumn,
            final Access2D<?> source, final Scalar.Factory<N> scalar) {
        int index = structure * firstColumn;
        for (int j = firstColumn; j < limitColumn; j++) {
            for (int i = 0; i < structure; i++) {
                data[index++] = scalar.cast(source.get(j, i));
            }
        }
    }

}
