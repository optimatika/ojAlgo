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

import org.ojalgo.array.BasicArray;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;

public abstract class CorePrimitiveOperation implements ArrayOperation {

    public static int THRESHOLD = 256;

    public static <N extends Comparable<N>> void add(final BasicArray<N> data, final long first, final long limit, final long step, final Access1D<N> left,
            final Access1D<N> right) {

        switch (data.getMathType()) {
        case R064:
            for (long i = first; i < limit; i += step) {
                data.set(i, left.doubleValue(i) + right.doubleValue(i));
            }
            break;
        case R032:
            for (long i = first; i < limit; i += step) {
                data.set(i, left.floatValue(i) + right.floatValue(i));
            }
            break;
        case Z064:
            for (long i = first; i < limit; i += step) {
                data.set(i, left.longValue(i) + right.longValue(i));
            }
            break;
        case Z032:
        case Z016:
        case Z008:
            for (long i = first; i < limit; i += step) {
                data.set(i, left.intValue(i) + right.intValue(i));
            }
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    public static <N extends Comparable<N>> void add(final BasicArray<N> data, final long first, final long limit, final long step, final Access1D<N> left,
            final Comparable<?> right) {

        switch (data.getMathType()) {
        case R064:
            double doubleValue = Scalar.doubleValue(right);
            for (long i = first; i < limit; i += step) {
                data.set(i, left.doubleValue(i) + doubleValue);
            }
            break;
        case R032:
            float floatValue = Scalar.floatValue(right);
            for (long i = first; i < limit; i += step) {
                data.set(i, left.floatValue(i) + floatValue);
            }
            break;
        case Z064:
            long longValue = Scalar.longValue(right);
            for (long i = first; i < limit; i += step) {
                data.set(i, left.longValue(i) + longValue);
            }
            break;
        case Z032:
        case Z016:
        case Z008:
            int intValue = Scalar.intValue(right);
            for (long i = first; i < limit; i += step) {
                data.set(i, left.intValue(i) + intValue);
            }
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    public static <N extends Comparable<N>> void add(final BasicArray<N> data, final long first, final long limit, final long step, final Comparable<?> left,
            final Access1D<N> right) {

        switch (data.getMathType()) {
        case R064:
            double doubleValue = Scalar.doubleValue(left);
            for (long i = first; i < limit; i += step) {
                data.set(i, doubleValue + right.doubleValue(i));
            }
            break;
        case R032:
            float floatValue = Scalar.floatValue(left);
            for (long i = first; i < limit; i += step) {
                data.set(i, floatValue + right.floatValue(i));
            }
            break;
        case Z064:
            long longValue = Scalar.longValue(left);
            for (long i = first; i < limit; i += step) {
                data.set(i, longValue + right.longValue(i));
            }
            break;
        case Z032:
        case Z016:
        case Z008:
            int intValue = Scalar.intValue(left);
            for (long i = first; i < limit; i += step) {
                data.set(i, intValue + right.intValue(i));
            }
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    public static void add(final byte[] data, final int first, final int limit, final int step, final byte left, final byte[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (byte) (left + right[i]);
        }
    }

    public static void add(final byte[] data, final int first, final int limit, final int step, final byte[] left, final byte right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (byte) (left[i] + right);
        }
    }

    public static void add(final byte[] data, final int first, final int limit, final int step, final byte[] left, final byte[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (byte) (left[i] + right[i]);
        }
    }

    public static void add(final double[] data, final int first, final int limit, final int step, final double left, final double[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left + right[i];
        }
    }

    public static void add(final double[] data, final int first, final int limit, final int step, final double[] left, final double right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] + right;
        }
    }

    public static void add(final double[] data, final int first, final int limit, final int step, final double[] left, final double[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] + right[i];
        }
    }

    public static void add(final float[] data, final int first, final int limit, final int step, final float left, final float[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left + right[i];
        }
    }

    public static void add(final float[] data, final int first, final int limit, final int step, final float[] left, final float right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] + right;
        }
    }

    public static void add(final float[] data, final int first, final int limit, final int step, final float[] left, final float[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] + right[i];
        }
    }

    public static void add(final int[] data, final int first, final int limit, final int step, final int left, final int[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left + right[i];
        }
    }

    public static void add(final int[] data, final int first, final int limit, final int step, final int[] left, final int right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] + right;
        }
    }

    public static void add(final int[] data, final int first, final int limit, final int step, final int[] left, final int[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] + right[i];
        }
    }

    public static void add(final long[] data, final int first, final int limit, final int step, final long left, final long[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left + right[i];
        }
    }

    public static void add(final long[] data, final int first, final int limit, final int step, final long[] left, final long right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] + right;
        }
    }

    public static void add(final long[] data, final int first, final int limit, final int step, final long[] left, final long[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] + right[i];
        }
    }

    public static void add(final short[] data, final int first, final int limit, final int step, final short left, final short[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (short) (left + right[i]);
        }
    }

    public static void add(final short[] data, final int first, final int limit, final int step, final short[] left, final short right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (short) (left[i] + right);
        }
    }

    public static void add(final short[] data, final int first, final int limit, final int step, final short[] left, final short[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (short) (left[i] + right[i]);
        }
    }

    public static <N extends Comparable<N>> void divide(final BasicArray<N> data, final long first, final long limit, final long step, final Access1D<N> left,
            final Access1D<N> right) {

        switch (data.getMathType()) {
        case R064:
            for (long i = first; i < limit; i += step) {
                data.set(i, left.doubleValue(i) / right.doubleValue(i));
            }
            break;
        case R032:
            for (long i = first; i < limit; i += step) {
                data.set(i, left.floatValue(i) / right.floatValue(i));
            }
            break;
        case Z064:
            for (long i = first; i < limit; i += step) {
                data.set(i, left.longValue(i) / right.longValue(i));
            }
            break;
        case Z032:
        case Z016:
        case Z008:
            for (long i = first; i < limit; i += step) {
                data.set(i, left.intValue(i) / right.intValue(i));
            }
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    public static <N extends Comparable<N>> void divide(final BasicArray<N> data, final long first, final long limit, final long step, final Access1D<N> left,
            final Comparable<?> right) {

        switch (data.getMathType()) {
        case R064:
            double doubleValue = Scalar.doubleValue(right);
            for (long i = first; i < limit; i += step) {
                data.set(i, left.doubleValue(i) / doubleValue);
            }
            break;
        case R032:
            float floatValue = Scalar.floatValue(right);
            for (long i = first; i < limit; i += step) {
                data.set(i, left.floatValue(i) / floatValue);
            }
            break;
        case Z064:
            long longValue = Scalar.longValue(right);
            for (long i = first; i < limit; i += step) {
                data.set(i, left.longValue(i) / longValue);
            }
            break;
        case Z032:
        case Z016:
        case Z008:
            int intValue = Scalar.intValue(right);
            for (long i = first; i < limit; i += step) {
                data.set(i, left.intValue(i) / intValue);
            }
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    public static <N extends Comparable<N>> void divide(final BasicArray<N> data, final long first, final long limit, final long step, final Comparable<?> left,
            final Access1D<N> right) {

        switch (data.getMathType()) {
        case R064:
            double doubleValue = Scalar.doubleValue(left);
            for (long i = first; i < limit; i += step) {
                data.set(i, doubleValue / right.doubleValue(i));
            }
            break;
        case R032:
            float floatValue = Scalar.floatValue(left);
            for (long i = first; i < limit; i += step) {
                data.set(i, floatValue / right.floatValue(i));
            }
            break;
        case Z064:
            long longValue = Scalar.longValue(left);
            for (long i = first; i < limit; i += step) {
                data.set(i, longValue / right.longValue(i));
            }
            break;
        case Z032:
        case Z016:
        case Z008:
            int intValue = Scalar.intValue(left);
            for (long i = first; i < limit; i += step) {
                data.set(i, intValue / right.intValue(i));
            }
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    public static void divide(final byte[] data, final int first, final int limit, final int step, final byte left, final byte[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (byte) (left / right[i]);
        }
    }

    public static void divide(final byte[] data, final int first, final int limit, final int step, final byte[] left, final byte right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (byte) (left[i] / right);
        }
    }

    public static void divide(final byte[] data, final int first, final int limit, final int step, final byte[] left, final byte[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (byte) (left[i] / right[i]);
        }
    }

    public static void divide(final double[] data, final int first, final int limit, final int step, final double left, final double[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left / right[i];
        }
    }

    public static void divide(final double[] data, final int first, final int limit, final int step, final double[] left, final double right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] / right;
        }
    }

    public static void divide(final double[] data, final int first, final int limit, final int step, final double[] left, final double[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] / right[i];
        }
    }

    public static void divide(final float[] data, final int first, final int limit, final int step, final float left, final float[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left / right[i];
        }
    }

    public static void divide(final float[] data, final int first, final int limit, final int step, final float[] left, final float right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] / right;
        }
    }

    public static void divide(final float[] data, final int first, final int limit, final int step, final float[] left, final float[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] / right[i];
        }
    }

    public static void divide(final int[] data, final int first, final int limit, final int step, final int left, final int[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left / right[i];
        }
    }

    public static void divide(final int[] data, final int first, final int limit, final int step, final int[] left, final int right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] / right;
        }
    }

    public static void divide(final int[] data, final int first, final int limit, final int step, final int[] left, final int[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] / right[i];
        }
    }

    public static void divide(final long[] data, final int first, final int limit, final int step, final long left, final long[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left / right[i];
        }
    }

    public static void divide(final long[] data, final int first, final int limit, final int step, final long[] left, final long right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] / right;
        }
    }

    public static void divide(final long[] data, final int first, final int limit, final int step, final long[] left, final long[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] / right[i];
        }
    }

    public static void divide(final short[] data, final int first, final int limit, final int step, final short left, final short[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (short) (left / right[i]);
        }
    }

    public static void divide(final short[] data, final int first, final int limit, final int step, final short[] left, final short right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (short) (left[i] / right);
        }
    }

    public static void divide(final short[] data, final int first, final int limit, final int step, final short[] left, final short[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (short) (left[i] / right[i]);
        }
    }

    public static <N extends Comparable<N>> void multiply(final BasicArray<N> data, final long first, final long limit, final long step, final Access1D<N> left,
            final Access1D<N> right) {

        switch (data.getMathType()) {
        case R064:
            for (long i = first; i < limit; i += step) {
                data.set(i, left.doubleValue(i) * right.doubleValue(i));
            }
            break;
        case R032:
            for (long i = first; i < limit; i += step) {
                data.set(i, left.floatValue(i) * right.floatValue(i));
            }
            break;
        case Z064:
            for (long i = first; i < limit; i += step) {
                data.set(i, left.longValue(i) * right.longValue(i));
            }
            break;
        case Z032:
        case Z016:
        case Z008:
            for (long i = first; i < limit; i += step) {
                data.set(i, left.intValue(i) * right.intValue(i));
            }
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    public static <N extends Comparable<N>> void multiply(final BasicArray<N> data, final long first, final long limit, final long step, final Access1D<N> left,
            final Comparable<?> right) {

        switch (data.getMathType()) {
        case R064:
            double doubleValue = Scalar.doubleValue(right);
            for (long i = first; i < limit; i += step) {
                data.set(i, left.doubleValue(i) * doubleValue);
            }
            break;
        case R032:
            float floatValue = Scalar.floatValue(right);
            for (long i = first; i < limit; i += step) {
                data.set(i, left.floatValue(i) * floatValue);
            }
            break;
        case Z064:
            long longValue = Scalar.longValue(right);
            for (long i = first; i < limit; i += step) {
                data.set(i, left.longValue(i) * longValue);
            }
            break;
        case Z032:
        case Z016:
        case Z008:
            int intValue = Scalar.intValue(right);
            for (long i = first; i < limit; i += step) {
                data.set(i, left.intValue(i) * intValue);
            }
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    public static <N extends Comparable<N>> void multiply(final BasicArray<N> data, final long first, final long limit, final long step,
            final Comparable<?> left, final Access1D<N> right) {

        switch (data.getMathType()) {
        case R064:
            double doubleValue = Scalar.doubleValue(left);
            for (long i = first; i < limit; i += step) {
                data.set(i, doubleValue * right.doubleValue(i));
            }
            break;
        case R032:
            float floatValue = Scalar.floatValue(left);
            for (long i = first; i < limit; i += step) {
                data.set(i, floatValue * right.floatValue(i));
            }
            break;
        case Z064:
            long longValue = Scalar.longValue(left);
            for (long i = first; i < limit; i += step) {
                data.set(i, longValue * right.longValue(i));
            }
            break;
        case Z032:
        case Z016:
        case Z008:
            int intValue = Scalar.intValue(left);
            for (long i = first; i < limit; i += step) {
                data.set(i, intValue * right.intValue(i));
            }
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    public static void multiply(final byte[] data, final int first, final int limit, final int step, final byte left, final byte[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (byte) (left * right[i]);
        }
    }

    public static void multiply(final byte[] data, final int first, final int limit, final int step, final byte[] left, final byte right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (byte) (left[i] * right);
        }
    }

    public static void multiply(final byte[] data, final int first, final int limit, final int step, final byte[] left, final byte[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (byte) (left[i] * right[i]);
        }
    }

    public static void multiply(final double[] data, final int first, final int limit, final int step, final double left, final double[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left * right[i];
        }
    }

    public static void multiply(final double[] data, final int first, final int limit, final int step, final double[] left, final double right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] * right;
        }
    }

    public static void multiply(final double[] data, final int first, final int limit, final int step, final double[] left, final double[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] * right[i];
        }
    }

    public static void multiply(final float[] data, final int first, final int limit, final int step, final float left, final float[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left * right[i];
        }
    }

    public static void multiply(final float[] data, final int first, final int limit, final int step, final float[] left, final float right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] * right;
        }
    }

    public static void multiply(final float[] data, final int first, final int limit, final int step, final float[] left, final float[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] * right[i];
        }
    }

    public static void multiply(final int[] data, final int first, final int limit, final int step, final int left, final int[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left * right[i];
        }
    }

    public static void multiply(final int[] data, final int first, final int limit, final int step, final int[] left, final int right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] * right;
        }
    }

    public static void multiply(final int[] data, final int first, final int limit, final int step, final int[] left, final int[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] * right[i];
        }
    }

    public static void multiply(final long[] data, final int first, final int limit, final int step, final long left, final long[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left * right[i];
        }
    }

    public static void multiply(final long[] data, final int first, final int limit, final int step, final long[] left, final long right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] * right;
        }
    }

    public static void multiply(final long[] data, final int first, final int limit, final int step, final long[] left, final long[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] * right[i];
        }
    }

    public static void multiply(final short[] data, final int first, final int limit, final int step, final short left, final short[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (short) (left * right[i]);
        }
    }

    public static void multiply(final short[] data, final int first, final int limit, final int step, final short[] left, final short right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (short) (left[i] * right);
        }
    }

    public static void multiply(final short[] data, final int first, final int limit, final int step, final short[] left, final short[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (short) (left[i] * right[i]);
        }
    }

    public static <N extends Comparable<N>> void negate(final BasicArray<N> data, final long first, final long limit, final long step,
            final Access1D<N> values) {

        switch (data.getMathType()) {
        case R064:
            for (long i = first; i < limit; i += step) {
                data.set(i, -values.doubleValue(i));
            }
            break;
        case R032:
            for (long i = first; i < limit; i += step) {
                data.set(i, -values.floatValue(i));
            }
            break;
        case Z064:
            for (long i = first; i < limit; i += step) {
                data.set(i, -values.longValue(i));
            }
            break;
        case Z032:
        case Z016:
        case Z008:
            for (long i = first; i < limit; i += step) {
                data.set(i, -values.intValue(i));
            }
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    public static void negate(final byte[] data, final int first, final int limit, final int step, final byte[] values) {
        for (int i = first; i < limit; i += step) {
            data[i] = (byte) -values[i];
        }
    }

    public static void negate(final double[] data, final int first, final int limit, final int step, final double[] values) {
        for (int i = first; i < limit; i += step) {
            data[i] = -values[i];
        }
    }

    public static void negate(final float[] data, final int first, final int limit, final int step, final float[] values) {
        for (int i = first; i < limit; i += step) {
            data[i] = -values[i];
        }
    }

    public static void negate(final int[] data, final int first, final int limit, final int step, final int[] values) {
        for (int i = first; i < limit; i += step) {
            data[i] = -values[i];
        }
    }

    public static void negate(final long[] data, final int first, final int limit, final int step, final long[] values) {
        for (int i = first; i < limit; i += step) {
            data[i] = -values[i];
        }
    }

    public static void negate(final short[] data, final int first, final int limit, final int step, final short[] values) {
        for (int i = first; i < limit; i += step) {
            data[i] = (short) -values[i];
        }
    }

    public static <N extends Comparable<N>> void subtract(final BasicArray<N> data, final long first, final long limit, final long step, final Access1D<N> left,
            final Access1D<N> right) {

        switch (data.getMathType()) {
        case R064:
            for (long i = first; i < limit; i += step) {
                data.set(i, left.doubleValue(i) - right.doubleValue(i));
            }
            break;
        case R032:
            for (long i = first; i < limit; i += step) {
                data.set(i, left.floatValue(i) - right.floatValue(i));
            }
            break;
        case Z064:
            for (long i = first; i < limit; i += step) {
                data.set(i, left.longValue(i) - right.longValue(i));
            }
            break;
        case Z032:
        case Z016:
        case Z008:
            for (long i = first; i < limit; i += step) {
                data.set(i, left.intValue(i) - right.intValue(i));
            }
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    public static <N extends Comparable<N>> void subtract(final BasicArray<N> data, final long first, final long limit, final long step, final Access1D<N> left,
            final Comparable<?> right) {

        switch (data.getMathType()) {
        case R064:
            double doubleValue = Scalar.doubleValue(right);
            for (long i = first; i < limit; i += step) {
                data.set(i, left.doubleValue(i) - doubleValue);
            }
            break;
        case R032:
            float floatValue = Scalar.floatValue(right);
            for (long i = first; i < limit; i += step) {
                data.set(i, left.floatValue(i) - floatValue);
            }
            break;
        case Z064:
            long longValue = Scalar.longValue(right);
            for (long i = first; i < limit; i += step) {
                data.set(i, left.longValue(i) - longValue);
            }
            break;
        case Z032:
        case Z016:
        case Z008:
            int intValue = Scalar.intValue(right);
            for (long i = first; i < limit; i += step) {
                data.set(i, left.intValue(i) - intValue);
            }
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    public static <N extends Comparable<N>> void subtract(final BasicArray<N> data, final long first, final long limit, final long step,
            final Comparable<?> left, final Access1D<N> right) {

        switch (data.getMathType()) {
        case R064:
            double doubleValue = Scalar.doubleValue(left);
            for (long i = first; i < limit; i += step) {
                data.set(i, doubleValue - right.doubleValue(i));
            }
            break;
        case R032:
            float floatValue = Scalar.floatValue(left);
            for (long i = first; i < limit; i += step) {
                data.set(i, floatValue - right.floatValue(i));
            }
            break;
        case Z064:
            long longValue = Scalar.longValue(left);
            for (long i = first; i < limit; i += step) {
                data.set(i, longValue - right.longValue(i));
            }
            break;
        case Z032:
        case Z016:
        case Z008:
            int intValue = Scalar.intValue(left);
            for (long i = first; i < limit; i += step) {
                data.set(i, intValue - right.intValue(i));
            }
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    public static void subtract(final byte[] data, final int first, final int limit, final int step, final byte left, final byte[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (byte) (left - right[i]);
        }
    }

    public static void subtract(final byte[] data, final int first, final int limit, final int step, final byte[] left, final byte right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (byte) (left[i] - right);
        }
    }

    public static void subtract(final byte[] data, final int first, final int limit, final int step, final byte[] left, final byte[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (byte) (left[i] - right[i]);
        }
    }

    public static void subtract(final double[] data, final int first, final int limit, final int step, final double left, final double[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left - right[i];
        }
    }

    public static void subtract(final double[] data, final int first, final int limit, final int step, final double[] left, final double right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] - right;
        }
    }

    public static void subtract(final double[] data, final int first, final int limit, final int step, final double[] left, final double[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] - right[i];
        }
    }

    public static void subtract(final float[] data, final int first, final int limit, final int step, final float left, final float[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left - right[i];
        }
    }

    public static void subtract(final float[] data, final int first, final int limit, final int step, final float[] left, final float right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] - right;
        }
    }

    public static void subtract(final float[] data, final int first, final int limit, final int step, final float[] left, final float[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] - right[i];
        }
    }

    public static void subtract(final int[] data, final int first, final int limit, final int step, final int left, final int[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left - right[i];
        }
    }

    public static void subtract(final int[] data, final int first, final int limit, final int step, final int[] left, final int right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] - right;
        }
    }

    public static void subtract(final int[] data, final int first, final int limit, final int step, final int[] left, final int[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] - right[i];
        }
    }

    public static void subtract(final long[] data, final int first, final int limit, final int step, final long left, final long[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left - right[i];
        }
    }

    public static void subtract(final long[] data, final int first, final int limit, final int step, final long[] left, final long right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] - right;
        }
    }

    public static void subtract(final long[] data, final int first, final int limit, final int step, final long[] left, final long[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] - right[i];
        }
    }

    public static void subtract(final short[] data, final int first, final int limit, final int step, final short left, final short[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (short) (left - right[i]);
        }
    }

    public static void subtract(final short[] data, final int first, final int limit, final int step, final short[] left, final short right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (short) (left[i] - right);
        }
    }

    public static void subtract(final short[] data, final int first, final int limit, final int step, final short[] left, final short[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = (short) (left[i] - right[i]);
        }
    }

}
