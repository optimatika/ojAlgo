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

import org.ojalgo.array.ArrayR032;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.ArrayZ008;
import org.ojalgo.array.ArrayZ016;
import org.ojalgo.array.ArrayZ032;
import org.ojalgo.array.ArrayZ064;
import org.ojalgo.array.BasicArray;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;

public abstract class OperationBinary implements ArrayOperation {

    public static int THRESHOLD = 256;

    public static <N extends Comparable<N>> void invoke(final BasicArray<N> data, final long first, final long limit, final long step, final Access1D<N> left,
            final BinaryFunction<N> function, final Access1D<N> right) {

        if (data.isPrimitive()) {
            if (function == PrimitiveMath.ADD) {
                CorePrimitiveOperation.add(data, first, limit, step, left, right);
            } else if (function == PrimitiveMath.DIVIDE) {
                CorePrimitiveOperation.divide(data, first, limit, step, left, right);
            } else if (function == PrimitiveMath.MULTIPLY) {
                CorePrimitiveOperation.multiply(data, first, limit, step, left, right);
            } else if (function == PrimitiveMath.SUBTRACT) {
                CorePrimitiveOperation.subtract(data, first, limit, step, left, right);
            } else {
                switch (data.getMathType()) {
                case R032:
                    for (long i = first; i < limit; i += step) {
                        data.set(i, function.invoke(left.floatValue(i), right.floatValue(i)));
                    }
                    break;
                case Z064:
                    for (long i = first; i < limit; i += step) {
                        data.set(i, function.invoke(left.longValue(i), right.longValue(i)));
                    }
                    break;
                case Z032:
                case Z016:
                case Z008:
                    for (long i = first; i < limit; i += step) {
                        data.set(i, function.invoke(left.intValue(i), right.intValue(i)));
                    }
                    break;
                default:
                    for (long i = first; i < limit; i += step) {
                        data.set(i, function.invoke(left.doubleValue(i), right.doubleValue(i)));
                    }
                    break;
                }
            }
        } else {
            for (long i = first; i < limit; i += step) {
                data.set(i, function.invoke(left.get(i), right.get(i)));
            }
        }
    }

    public static <N extends Comparable<N>> void invoke(final BasicArray<N> data, final long first, final long limit, final long step, final Access1D<N> left,
            final BinaryFunction<N> function, final N right) {

        if (data.isPrimitive()) {
            if (function == PrimitiveMath.ADD) {
                CorePrimitiveOperation.add(data, first, limit, step, left, right);
            } else if (function == PrimitiveMath.DIVIDE) {
                CorePrimitiveOperation.divide(data, first, limit, step, left, right);
            } else if (function == PrimitiveMath.MULTIPLY) {
                CorePrimitiveOperation.multiply(data, first, limit, step, left, right);
            } else if (function == PrimitiveMath.SUBTRACT) {
                CorePrimitiveOperation.subtract(data, first, limit, step, left, right);
            } else {
                switch (data.getMathType()) {
                case R032:
                    float floatValue = Scalar.floatValue(right);
                    for (long i = first; i < limit; i += step) {
                        data.set(i, function.invoke(left.floatValue(i), floatValue));
                    }
                    break;
                case Z064:
                    long longValue = Scalar.longValue(right);
                    for (long i = first; i < limit; i += step) {
                        data.set(i, function.invoke(left.longValue(i), longValue));
                    }
                    break;
                case Z032:
                case Z016:
                case Z008:
                    int intValue = Scalar.intValue(right);
                    for (long i = first; i < limit; i += step) {
                        data.set(i, function.invoke(left.intValue(i), intValue));
                    }
                    break;
                default:
                    double doubleValue = Scalar.doubleValue(right);
                    for (long i = first; i < limit; i += step) {
                        data.set(i, function.invoke(left.doubleValue(i), doubleValue));
                    }
                    break;
                }
            }
        } else {
            for (long i = first; i < limit; i += step) {
                data.set(i, function.invoke(left.get(i), right));
            }
        }
    }

    public static <N extends Comparable<N>> void invoke(final BasicArray<N> data, final long first, final long limit, final long step, final N left,
            final BinaryFunction<N> function, final Access1D<N> right) {

        if (data.isPrimitive()) {
            if (function == PrimitiveMath.ADD) {
                CorePrimitiveOperation.add(data, first, limit, step, left, right);
            } else if (function == PrimitiveMath.DIVIDE) {
                CorePrimitiveOperation.divide(data, first, limit, step, left, right);
            } else if (function == PrimitiveMath.MULTIPLY) {
                CorePrimitiveOperation.multiply(data, first, limit, step, left, right);
            } else if (function == PrimitiveMath.SUBTRACT) {
                CorePrimitiveOperation.subtract(data, first, limit, step, left, right);
            } else {
                switch (data.getMathType()) {
                case R032:
                    float floatValue = Scalar.floatValue(left);
                    for (long i = first; i < limit; i += step) {
                        data.set(i, function.invoke(floatValue, right.floatValue(i)));
                    }
                    break;
                case Z064:
                    long longValue = Scalar.longValue(left);
                    for (long i = first; i < limit; i += step) {
                        data.set(i, function.invoke(longValue, right.longValue(i)));
                    }
                    break;
                case Z032:
                case Z016:
                case Z008:
                    int intValue = Scalar.intValue(left);
                    for (long i = first; i < limit; i += step) {
                        data.set(i, function.invoke(intValue, right.intValue(i)));
                    }
                    break;
                default:
                    double doubleValue = Scalar.doubleValue(left);
                    for (long i = first; i < limit; i += step) {
                        data.set(i, function.invoke(doubleValue, right.doubleValue(i)));
                    }
                    break;
                }
            }
        } else {
            for (long i = first; i < limit; i += step) {
                data.set(i, function.invoke(left, right.get(i)));
            }
        }
    }

    public static void invoke(final byte[] data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final Access1D<Double> right) {
        if (left instanceof ArrayZ008 && right instanceof ArrayZ008) {
            OperationBinary.invoke(data, first, limit, step, ((ArrayZ008) left).data, function, ((ArrayZ008) right).data);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left.byteValue(i), right.byteValue(i));
            }
        }
    }

    public static void invoke(final byte[] data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final byte right) {
        if (left instanceof ArrayZ008) {
            OperationBinary.invoke(data, first, limit, step, ((ArrayZ008) left).data, function, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left.byteValue(i), right);
            }
        }
    }

    public static void invoke(final byte[] data, final int first, final int limit, final int step, final byte left, final BinaryFunction<Double> function,
            final Access1D<Double> right) {
        if (right instanceof ArrayZ008) {
            OperationBinary.invoke(data, first, limit, step, left, function, ((ArrayZ008) right).data);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left, right.byteValue(i));
            }
        }
    }

    public static void invoke(final byte[] data, final int first, final int limit, final int step, final byte left, final BinaryFunction<Double> function,
            final byte[] right) {
        if (function == PrimitiveMath.ADD) {
            CorePrimitiveOperation.add(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.DIVIDE) {
            CorePrimitiveOperation.divide(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.MULTIPLY) {
            CorePrimitiveOperation.multiply(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.SUBTRACT) {
            CorePrimitiveOperation.subtract(data, first, limit, step, left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left, right[i]);
            }
        }
    }

    public static void invoke(final byte[] data, final int first, final int limit, final int step, final byte[] left, final BinaryFunction<Double> function,
            final byte right) {
        if (function == PrimitiveMath.ADD) {
            CorePrimitiveOperation.add(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.DIVIDE) {
            CorePrimitiveOperation.divide(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.MULTIPLY) {
            CorePrimitiveOperation.multiply(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.SUBTRACT) {
            CorePrimitiveOperation.subtract(data, first, limit, step, left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left[i], right);
            }
        }
    }

    public static void invoke(final byte[] data, final int first, final int limit, final int step, final byte[] left, final BinaryFunction<Double> function,
            final byte[] right) {
        if (function == PrimitiveMath.ADD) {
            CorePrimitiveOperation.add(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.DIVIDE) {
            CorePrimitiveOperation.divide(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.MULTIPLY) {
            CorePrimitiveOperation.multiply(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.SUBTRACT) {
            CorePrimitiveOperation.subtract(data, first, limit, step, left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left[i], right[i]);
            }
        }
    }

    public static void invoke(final double[] data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final Access1D<Double> right) {
        if (left instanceof ArrayR064 && right instanceof ArrayR064) {
            OperationBinary.invoke(data, first, limit, step, ((ArrayR064) left).data, function, ((ArrayR064) right).data);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left.doubleValue(i), right.doubleValue(i));
            }
        }
    }

    public static void invoke(final double[] data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final double right) {
        if (left instanceof ArrayR064) {
            OperationBinary.invoke(data, first, limit, step, ((ArrayR064) left).data, function, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left.doubleValue(i), right);
            }
        }
    }

    public static void invoke(final double[] data, final int first, final int limit, final int step, final double left, final BinaryFunction<Double> function,
            final Access1D<Double> right) {
        if (right instanceof ArrayR064) {
            OperationBinary.invoke(data, first, limit, step, left, function, ((ArrayR064) right).data);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left, right.doubleValue(i));
            }
        }
    }

    public static void invoke(final double[] data, final int first, final int limit, final int step, final double left, final BinaryFunction<Double> function,
            final double[] right) {
        if (function == PrimitiveMath.ADD) {
            CorePrimitiveOperation.add(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.DIVIDE) {
            CorePrimitiveOperation.divide(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.MULTIPLY) {
            CorePrimitiveOperation.multiply(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.SUBTRACT) {
            CorePrimitiveOperation.subtract(data, first, limit, step, left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left, right[i]);
            }
        }
    }

    public static void invoke(final double[] data, final int first, final int limit, final int step, final double[] left, final BinaryFunction<Double> function,
            final double right) {
        if (function == PrimitiveMath.ADD) {
            CorePrimitiveOperation.add(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.DIVIDE) {
            CorePrimitiveOperation.divide(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.MULTIPLY) {
            CorePrimitiveOperation.multiply(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.SUBTRACT) {
            CorePrimitiveOperation.subtract(data, first, limit, step, left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left[i], right);
            }
        }
    }

    public static void invoke(final double[] data, final int first, final int limit, final int step, final double[] left, final BinaryFunction<Double> function,
            final double[] right) {
        if (function == PrimitiveMath.ADD) {
            CorePrimitiveOperation.add(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.DIVIDE) {
            CorePrimitiveOperation.divide(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.MULTIPLY) {
            CorePrimitiveOperation.multiply(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.SUBTRACT) {
            CorePrimitiveOperation.subtract(data, first, limit, step, left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left[i], right[i]);
            }
        }
    }

    public static void invoke(final float[] data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final Access1D<Double> right) {
        if (left instanceof ArrayR032 && right instanceof ArrayR032) {
            OperationBinary.invoke(data, first, limit, step, ((ArrayR032) left).data, function, ((ArrayR032) right).data);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left.floatValue(i), right.floatValue(i));
            }
        }
    }

    public static void invoke(final float[] data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final float right) {
        if (left instanceof ArrayR032) {
            OperationBinary.invoke(data, first, limit, step, ((ArrayR032) left).data, function, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left.floatValue(i), right);
            }
        }
    }

    public static void invoke(final float[] data, final int first, final int limit, final int step, final float left, final BinaryFunction<Double> function,
            final Access1D<Double> right) {
        if (right instanceof ArrayR032) {
            OperationBinary.invoke(data, first, limit, step, left, function, ((ArrayR032) right).data);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left, right.floatValue(i));
            }
        }
    }

    public static void invoke(final float[] data, final int first, final int limit, final int step, final float left, final BinaryFunction<Double> function,
            final float[] right) {
        if (function == PrimitiveMath.ADD) {
            CorePrimitiveOperation.add(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.DIVIDE) {
            CorePrimitiveOperation.divide(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.MULTIPLY) {
            CorePrimitiveOperation.multiply(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.SUBTRACT) {
            CorePrimitiveOperation.subtract(data, first, limit, step, left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left, right[i]);
            }
        }
    }

    public static void invoke(final float[] data, final int first, final int limit, final int step, final float[] left, final BinaryFunction<Double> function,
            final float right) {
        if (function == PrimitiveMath.ADD) {
            CorePrimitiveOperation.add(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.DIVIDE) {
            CorePrimitiveOperation.divide(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.MULTIPLY) {
            CorePrimitiveOperation.multiply(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.SUBTRACT) {
            CorePrimitiveOperation.subtract(data, first, limit, step, left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left[i], right);
            }
        }
    }

    public static void invoke(final float[] data, final int first, final int limit, final int step, final float[] left, final BinaryFunction<Double> function,
            final float[] right) {
        if (function == PrimitiveMath.ADD) {
            CorePrimitiveOperation.add(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.DIVIDE) {
            CorePrimitiveOperation.divide(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.MULTIPLY) {
            CorePrimitiveOperation.multiply(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.SUBTRACT) {
            CorePrimitiveOperation.subtract(data, first, limit, step, left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left[i], right[i]);
            }
        }
    }

    public static void invoke(final int[] data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final Access1D<Double> right) {
        if (left instanceof ArrayZ032 && right instanceof ArrayZ032) {
            OperationBinary.invoke(data, first, limit, step, ((ArrayZ032) left).data, function, ((ArrayZ032) right).data);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left.intValue(i), right.intValue(i));
            }
        }
    }

    public static void invoke(final int[] data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final int right) {
        if (left instanceof ArrayZ032) {
            OperationBinary.invoke(data, first, limit, step, ((ArrayZ032) left).data, function, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left.intValue(i), right);
            }
        }
    }

    public static void invoke(final int[] data, final int first, final int limit, final int step, final int left, final BinaryFunction<Double> function,
            final Access1D<Double> right) {
        if (right instanceof ArrayZ032) {
            OperationBinary.invoke(data, first, limit, step, left, function, ((ArrayZ032) right).data);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left, right.intValue(i));
            }
        }
    }

    public static void invoke(final int[] data, final int first, final int limit, final int step, final int left, final BinaryFunction<Double> function,
            final int[] right) {
        if (function == PrimitiveMath.ADD) {
            CorePrimitiveOperation.add(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.DIVIDE) {
            CorePrimitiveOperation.divide(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.MULTIPLY) {
            CorePrimitiveOperation.multiply(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.SUBTRACT) {
            CorePrimitiveOperation.subtract(data, first, limit, step, left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left, right[i]);
            }
        }
    }

    public static void invoke(final int[] data, final int first, final int limit, final int step, final int[] left, final BinaryFunction<Double> function,
            final int right) {
        if (function == PrimitiveMath.ADD) {
            CorePrimitiveOperation.add(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.DIVIDE) {
            CorePrimitiveOperation.divide(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.MULTIPLY) {
            CorePrimitiveOperation.multiply(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.SUBTRACT) {
            CorePrimitiveOperation.subtract(data, first, limit, step, left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left[i], right);
            }
        }
    }

    public static void invoke(final int[] data, final int first, final int limit, final int step, final int[] left, final BinaryFunction<Double> function,
            final int[] right) {
        if (function == PrimitiveMath.ADD) {
            CorePrimitiveOperation.add(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.DIVIDE) {
            CorePrimitiveOperation.divide(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.MULTIPLY) {
            CorePrimitiveOperation.multiply(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.SUBTRACT) {
            CorePrimitiveOperation.subtract(data, first, limit, step, left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left[i], right[i]);
            }
        }
    }

    public static void invoke(final long[] data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final Access1D<Double> right) {
        if (left instanceof ArrayZ064 && right instanceof ArrayZ064) {
            OperationBinary.invoke(data, first, limit, step, ((ArrayZ064) left).data, function, ((ArrayZ064) right).data);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left.longValue(i), right.longValue(i));
            }
        }
    }

    public static void invoke(final long[] data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final long right) {
        if (left instanceof ArrayZ064) {
            OperationBinary.invoke(data, first, limit, step, ((ArrayZ064) left).data, function, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left.longValue(i), right);
            }
        }
    }

    public static void invoke(final long[] data, final int first, final int limit, final int step, final long left, final BinaryFunction<Double> function,
            final Access1D<Double> right) {
        if (right instanceof ArrayZ064) {
            OperationBinary.invoke(data, first, limit, step, left, function, ((ArrayZ064) right).data);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left, right.longValue(i));
            }
        }
    }

    public static void invoke(final long[] data, final int first, final int limit, final int step, final long left, final BinaryFunction<Double> function,
            final long[] right) {
        if (function == PrimitiveMath.ADD) {
            CorePrimitiveOperation.add(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.DIVIDE) {
            CorePrimitiveOperation.divide(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.MULTIPLY) {
            CorePrimitiveOperation.multiply(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.SUBTRACT) {
            CorePrimitiveOperation.subtract(data, first, limit, step, left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left, right[i]);
            }
        }
    }

    public static void invoke(final long[] data, final int first, final int limit, final int step, final long[] left, final BinaryFunction<Double> function,
            final long right) {
        if (function == PrimitiveMath.ADD) {
            CorePrimitiveOperation.add(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.DIVIDE) {
            CorePrimitiveOperation.divide(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.MULTIPLY) {
            CorePrimitiveOperation.multiply(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.SUBTRACT) {
            CorePrimitiveOperation.subtract(data, first, limit, step, left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left[i], right);
            }
        }
    }

    public static void invoke(final long[] data, final int first, final int limit, final int step, final long[] left, final BinaryFunction<Double> function,
            final long[] right) {
        if (function == PrimitiveMath.ADD) {
            CorePrimitiveOperation.add(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.DIVIDE) {
            CorePrimitiveOperation.divide(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.MULTIPLY) {
            CorePrimitiveOperation.multiply(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.SUBTRACT) {
            CorePrimitiveOperation.subtract(data, first, limit, step, left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left[i], right[i]);
            }
        }
    }

    public static <N extends Comparable<N>> void invoke(final N[] data, final int first, final int limit, final int step, final Access1D<N> left,
            final BinaryFunction<N> function, final Access1D<N> right) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(left.get(i), right.get(i));
        }
    }

    public static <N extends Comparable<N>> void invoke(final N[] data, final int first, final int limit, final int step, final Access1D<N> left,
            final BinaryFunction<N> function, final N right) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(left.get(i), right);
        }
    }

    public static <N extends Comparable<N>> void invoke(final N[] data, final int first, final int limit, final int step, final N left,
            final BinaryFunction<N> function, final Access1D<N> right) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(left, right.get(i));
        }
    }

    public static void invoke(final short[] data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final Access1D<Double> right) {
        if (left instanceof ArrayZ016 && right instanceof ArrayZ016) {
            OperationBinary.invoke(data, first, limit, step, ((ArrayZ016) left).data, function, ((ArrayZ016) right).data);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left.shortValue(i), right.shortValue(i));
            }
        }
    }

    public static void invoke(final short[] data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final short right) {
        if (left instanceof ArrayZ016) {
            OperationBinary.invoke(data, first, limit, step, ((ArrayZ016) left).data, function, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left.shortValue(i), right);
            }
        }
    }

    public static void invoke(final short[] data, final int first, final int limit, final int step, final short left, final BinaryFunction<Double> function,
            final Access1D<Double> right) {
        if (right instanceof ArrayZ016) {
            OperationBinary.invoke(data, first, limit, step, left, function, ((ArrayZ016) right).data);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left, right.shortValue(i));
            }
        }
    }

    public static void invoke(final short[] data, final int first, final int limit, final int step, final short left, final BinaryFunction<Double> function,
            final short[] right) {
        if (function == PrimitiveMath.ADD) {
            CorePrimitiveOperation.add(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.DIVIDE) {
            CorePrimitiveOperation.divide(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.MULTIPLY) {
            CorePrimitiveOperation.multiply(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.SUBTRACT) {
            CorePrimitiveOperation.subtract(data, first, limit, step, left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left, right[i]);
            }
        }
    }

    public static void invoke(final short[] data, final int first, final int limit, final int step, final short[] left, final BinaryFunction<Double> function,
            final short right) {
        if (function == PrimitiveMath.ADD) {
            CorePrimitiveOperation.add(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.DIVIDE) {
            CorePrimitiveOperation.divide(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.MULTIPLY) {
            CorePrimitiveOperation.multiply(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.SUBTRACT) {
            CorePrimitiveOperation.subtract(data, first, limit, step, left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left[i], right);
            }
        }
    }

    public static void invoke(final short[] data, final int first, final int limit, final int step, final short[] left, final BinaryFunction<Double> function,
            final short[] right) {
        if (function == PrimitiveMath.ADD) {
            CorePrimitiveOperation.add(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.DIVIDE) {
            CorePrimitiveOperation.divide(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.MULTIPLY) {
            CorePrimitiveOperation.multiply(data, first, limit, step, left, right);
        } else if (function == PrimitiveMath.SUBTRACT) {
            CorePrimitiveOperation.subtract(data, first, limit, step, left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left[i], right[i]);
            }
        }
    }

}
