/*
 * Copyright 1997-2022 Optimatika
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

import org.ojalgo.array.Primitive32Array;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.structure.Access1D;

public final class OperationBinary implements ArrayOperation {

    public static int THRESHOLD = 256;

    public static void invoke(final double[] data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final Access1D<Double> right) {
        if (left instanceof Primitive64Array && right instanceof Primitive64Array) {
            OperationBinary.invoke(data, first, limit, step, ((Primitive64Array) left).data, function, ((Primitive64Array) right).data);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left.doubleValue(i), right.doubleValue(i));
            }
        }
    }

    public static void invoke(final double[] data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final double right) {
        if (left instanceof Primitive64Array) {
            OperationBinary.invoke(data, first, limit, step, ((Primitive64Array) left).data, function, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left.doubleValue(i), right);
            }
        }
    }

    public static void invoke(final double[] data, final int first, final int limit, final int step, final double left, final BinaryFunction<Double> function,
            final Access1D<Double> right) {
        if (right instanceof Primitive64Array) {
            OperationBinary.invoke(data, first, limit, step, left, function, ((Primitive64Array) right).data);
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

    public static void invoke(final float[] data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final Access1D<Double> right) {
        if (left instanceof Primitive32Array && right instanceof Primitive32Array) {
            OperationBinary.invoke(data, first, limit, step, ((Primitive32Array) left).data, function, ((Primitive32Array) right).data);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left.floatValue(i), right.floatValue(i));
            }
        }
    }

    public static void invoke(final float[] data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final float right) {
        if (left instanceof Primitive32Array) {
            OperationBinary.invoke(data, first, limit, step, ((Primitive32Array) left).data, function, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left.floatValue(i), right);
            }
        }
    }

    public static void invoke(final float[] data, final int first, final int limit, final int step, final float left, final BinaryFunction<Double> function,
            final Access1D<Double> right) {
        if (right instanceof Primitive32Array) {
            OperationBinary.invoke(data, first, limit, step, left, function, ((Primitive32Array) right).data);
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

    static void invoke(final double[] data, final int first, final int limit, final int step, final double[] left, final BinaryFunction<Double> function,
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

    static void invoke(final float[] data, final int first, final int limit, final int step, final float[] left, final BinaryFunction<Double> function,
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

}
