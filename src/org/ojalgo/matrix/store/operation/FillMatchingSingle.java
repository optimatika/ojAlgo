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
package org.ojalgo.matrix.store.operation;

import java.math.BigDecimal;

import org.ojalgo.access.Access2D;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.TypeUtils;

public final class FillMatchingSingle extends MatrixOperation {

    public static final FillMatchingSingle SETUP = new FillMatchingSingle();

    public static int THRESHOLD = 256;

    public static void conjugate(final BigDecimal[] data, final int structure, final int firstColumn, final int limitColumn, final Access2D<?> source) {
        FillMatchingSingle.transpose(data, structure, firstColumn, limitColumn, source);
    }

    public static void conjugate(final double[] data, final int structure, final int firstColumn, final int limitColumn, final Access2D<?> source) {
        FillMatchingSingle.transpose(data, structure, firstColumn, limitColumn, source);
    }

    public static <N extends Number & Scalar<N>> void conjugate(final N[] data, final int structure, final int firstColumn, final int limitColumn,
            final Access2D<?> source, final Scalar.Factory<N> scalar) {
        int index = structure * firstColumn;
        for (int j = firstColumn; j < limitColumn; j++) {
            for (int i = 0; i < structure; i++) {
                data[index++] = scalar.cast(source.get(j, i)).conjugate().get();
            }
        }
    }

    public static void copy(final BigDecimal[] data, final int structure, final int firstColumn, final int limitColumn,
            final Access2D<? extends Number> source) {
        int index = structure * firstColumn;
        for (int j = firstColumn; j < limitColumn; j++) {
            for (int i = 0; i < structure; i++) {
                data[index++] = TypeUtils.toBigDecimal(source.get(i, j));
            }
        }
    }

    public static void copy(final double[] data, final int structure, final int firstColumn, final int limitColumn, final Access2D<? extends Number> source) {
        int index = structure * firstColumn;
        for (int j = firstColumn; j < limitColumn; j++) {
            for (int i = 0; i < structure; i++) {
                data[index++] = source.doubleValue(i, j);
            }
        }
    }

    public static <N extends Number & Scalar<N>> void copy(final N[] data, final int structure, final int firstColumn, final int limitColumn,
            final Access2D<?> source, final Scalar.Factory<N> scalar) {
        int index = structure * firstColumn;
        for (int j = firstColumn; j < limitColumn; j++) {
            for (int i = 0; i < structure; i++) {
                data[index++] = scalar.cast(source.get(i, j));
            }
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

    public static <N extends Number & Scalar<N>> void transpose(final N[] data, final int structure, final int firstColumn, final int limitColumn,
            final Access2D<?> source, final Scalar.Factory<N> scalar) {
        int index = structure * firstColumn;
        for (int j = firstColumn; j < limitColumn; j++) {
            for (int i = 0; i < structure; i++) {
                data[index++] = scalar.cast(source.get(j, i));
            }
        }
    }

    private FillMatchingSingle() {
        super();
    }

    @Override
    public int threshold() {
        return THRESHOLD;
    }

}
