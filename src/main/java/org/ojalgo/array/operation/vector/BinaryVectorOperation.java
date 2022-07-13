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
package org.ojalgo.array.operation.vector;

import java.util.function.DoubleBinaryOperator;

import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.MissingMath;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorOperators.Binary;

public abstract class BinaryVectorOperation extends VectorOperation {

    public static void invoke(final double[] data, final double left, final DoubleBinaryOperator function, final double[] right) {

        int limit = MissingMath.min(data.length, right.length);

        int i = 0;

        Binary operator = BinaryVectorOperation.operator(function);

        if (operator != null) {

            int bound = SPECIES_DOUBLE.loopBound(limit);
            int length = SPECIES_DOUBLE.length();

            DoubleVector lv = DoubleVector.broadcast(SPECIES_DOUBLE, left);

            for (; i < bound; i += length) {
                DoubleVector rv = DoubleVector.fromArray(SPECIES_DOUBLE, right, i);
                DoubleVector dv = lv.lanewise(operator, rv);
                dv.intoArray(data, i);
            }
        }

        for (; i < limit; i++) {
            data[i] = function.applyAsDouble(left, right[i]);
        }
    }

    public static void invoke(final double[] data, final double left, final DoubleBinaryOperator function, final int first, final int limit) {
        BinaryVectorOperation.invoke(data, left, function, first, limit, 1); // TODO Inline when it's done
    }

    public static void invoke(final double[] data, final double left, final DoubleBinaryOperator function, final int first, final int limit, final int step) {

        int i = first;

        Binary operator = BinaryVectorOperation.operator(function);

        if (operator != null) {

            int length = SPECIES_DOUBLE.length();
            int[] map = new int[length];
            for (int s = 0; s < length; s++) {
                map[s] = s * step;
            }
            int stride = length * step;

            DoubleVector lv = DoubleVector.broadcast(SPECIES_DOUBLE, left);

            int bound = SPECIES_DOUBLE.loopBound((limit - first) / stride);
            for (int b = 0; b < bound; b++) {
                DoubleVector rv = DoubleVector.fromArray(SPECIES_DOUBLE, data, i, map, 0);
                DoubleVector dv = lv.lanewise(operator, rv);
                dv.intoArray(data, i, map, 0);
                i += stride;
            }
        }

        for (; i < limit; i += step) {
            data[i] = function.applyAsDouble(left, data[i]);
        }
    }

    public static void invoke(final double[] data, final double[] left, final DoubleBinaryOperator function, final double right) {

        int limit = MissingMath.min(data.length, left.length);

        int i = 0;

        Binary operator = BinaryVectorOperation.operator(function);

        if (operator != null) {

            int bound = SPECIES_DOUBLE.loopBound(limit);
            int length = SPECIES_DOUBLE.length();

            DoubleVector rv = DoubleVector.broadcast(SPECIES_DOUBLE, right);

            for (; i < bound; i += length) {
                DoubleVector lv = DoubleVector.fromArray(SPECIES_DOUBLE, left, i);
                DoubleVector dv = lv.lanewise(operator, rv);
                dv.intoArray(data, i);
            }
        }

        for (; i < limit; i++) {
            data[i] = function.applyAsDouble(left[i], right);
        }
    }

    public static void invoke(final double[] data, final double[] left, final DoubleBinaryOperator function, final double[] right) {

        int limit = MissingMath.min(data.length, left.length, right.length);

        int i = 0;

        Binary operator = BinaryVectorOperation.operator(function);

        if (operator != null) {

            int bound = SPECIES_DOUBLE.loopBound(limit);
            int length = SPECIES_DOUBLE.length();

            for (; i < bound; i += length) {
                DoubleVector lv = DoubleVector.fromArray(SPECIES_DOUBLE, left, i);
                DoubleVector rv = DoubleVector.fromArray(SPECIES_DOUBLE, right, i);
                DoubleVector dv = lv.lanewise(operator, rv);
                dv.intoArray(data, i);
            }
        }

        for (; i < limit; i++) {
            data[i] = function.applyAsDouble(left[i], right[i]);
        }
    }

    public static void invoke(final double[] data, final DoubleBinaryOperator function, final double right, final int first, final int limit) {
        BinaryVectorOperation.invoke(data, function, right, first, limit, 1); // TODO Inline when it's done
    }

    public static void invoke(final double[] data, final DoubleBinaryOperator function, final double right, final int first, final int limit, final int step) {

        int i = first;

        Binary operator = BinaryVectorOperation.operator(function);

        if (operator != null) {

            int length = SPECIES_DOUBLE.length();
            int[] map = new int[length];
            for (int s = 0; s < length; s++) {
                map[s] = s * step;
            }
            int stride = length * step;

            DoubleVector rv = DoubleVector.broadcast(SPECIES_DOUBLE, right);

            int bound = SPECIES_DOUBLE.loopBound((limit - first) / stride);
            for (int b = 0; b < bound; b++) {
                DoubleVector lv = DoubleVector.fromArray(SPECIES_DOUBLE, data, i, map, 0);
                DoubleVector dv = lv.lanewise(operator, rv);
                dv.intoArray(data, i, map, 0);
                i += stride;
            }
        }

        for (; i < limit; i += step) {
            data[i] = function.applyAsDouble(data[i], right);
        }
    }

    public static void invoke(final float[] data, final BinaryFunction<Double> function, final float right, final int first, final int limit) {
        BinaryVectorOperation.invoke(data, function, right, first, limit, 1); // TODO Inline when it's done
    }

    public static void invoke(final float[] data, final BinaryFunction<Double> function, final float right, final int first, final int limit, final int step) {

        int i = first;

        Binary operator = BinaryVectorOperation.operator(function);

        if (operator != null) {

            int length = SPECIES_FLOAT.length();
            int[] map = new int[length];
            for (int s = 0; s < length; s++) {
                map[s] = s * step;
            }
            int stride = length * step;

            FloatVector rv = FloatVector.broadcast(SPECIES_FLOAT, right);

            int bound = SPECIES_DOUBLE.loopBound((limit - first) / stride);
            for (int b = 0; b < bound; b++) {
                FloatVector lv = FloatVector.fromArray(SPECIES_FLOAT, data, i, map, 0);
                FloatVector dv = lv.lanewise(operator, rv);
                dv.intoArray(data, i, map, 0);
                i += stride;
            }
        }

        for (; i < limit; i += step) {
            data[i] = function.invoke(data[i], right);
        }
    }

    public static void invoke(final float[] data, final float left, final BinaryFunction<Double> function, final float[] right) {

        int limit = MissingMath.min(data.length, right.length);

        int i = 0;

        Binary operator = BinaryVectorOperation.operator(function);

        if (operator != null) {

            int bound = SPECIES_FLOAT.loopBound(limit);
            int length = SPECIES_FLOAT.length();

            FloatVector lv = FloatVector.broadcast(SPECIES_FLOAT, left);

            for (; i < bound; i += length) {
                FloatVector rv = FloatVector.fromArray(SPECIES_FLOAT, right, i);
                FloatVector dv = lv.lanewise(operator, rv);
                dv.intoArray(data, i);
            }
        }

        for (; i < limit; i++) {
            data[i] = function.invoke(left, right[i]);
        }
    }

    public static void invoke(final float[] data, final float left, final BinaryFunction<Double> function, final int first, final int limit) {
        BinaryVectorOperation.invoke(data, left, function, first, limit, 1); // TODO Inline when it's done
    }

    public static void invoke(final float[] data, final float left, final BinaryFunction<Double> function, final int first, final int limit, final int step) {

        int i = first;

        Binary operator = BinaryVectorOperation.operator(function);

        if (operator != null) {

            int length = SPECIES_FLOAT.length();
            int[] map = new int[length];
            for (int s = 0; s < length; s++) {
                map[s] = s * step;
            }
            int stride = length * step;

            FloatVector lv = FloatVector.broadcast(SPECIES_FLOAT, left);

            int bound = SPECIES_DOUBLE.loopBound((limit - first) / stride);
            for (int b = 0; b < bound; b++) {
                FloatVector rv = FloatVector.fromArray(SPECIES_FLOAT, data, i, map, 0);
                FloatVector dv = lv.lanewise(operator, rv);
                dv.intoArray(data, i, map, 0);
                i += stride;
            }
        }

        for (; i < limit; i += step) {
            data[i] = function.invoke(left, data[i]);
        }
    }

    public static void invoke(final float[] data, final float[] left, final BinaryFunction<Double> function, final float right) {

        int limit = MissingMath.min(data.length, left.length);

        int i = 0;

        Binary operator = BinaryVectorOperation.operator(function);

        if (operator != null) {

            int bound = SPECIES_FLOAT.loopBound(limit);
            int length = SPECIES_FLOAT.length();

            FloatVector rv = FloatVector.broadcast(SPECIES_FLOAT, right);

            for (; i < bound; i += length) {
                FloatVector lv = FloatVector.fromArray(SPECIES_FLOAT, left, i);
                FloatVector dv = lv.lanewise(operator, rv);
                dv.intoArray(data, i);
            }
        }

        for (; i < limit; i++) {
            data[i] = function.invoke(left[i], right);
        }
    }

    public static void invoke(final float[] data, final float[] left, final BinaryFunction<Double> function, final float[] right) {

        int limit = MissingMath.min(data.length, left.length, right.length);

        int i = 0;

        Binary operator = BinaryVectorOperation.operator(function);

        if (operator != null) {

            int bound = SPECIES_FLOAT.loopBound(limit);
            int length = SPECIES_FLOAT.length();

            for (; i < bound; i += length) {
                FloatVector lv = FloatVector.fromArray(SPECIES_FLOAT, left, i);
                FloatVector rv = FloatVector.fromArray(SPECIES_FLOAT, right, i);
                FloatVector dv = lv.lanewise(operator, rv);
                dv.intoArray(data, i);
            }
        }

        for (; i < limit; i++) {
            data[i] = function.invoke(left[i], right[i]);
        }
    }

    static VectorOperators.Binary operator(final DoubleBinaryOperator function) {
        if (function == PrimitiveMath.ADD) {
            return VectorOperators.ADD;
        } else if (function == PrimitiveMath.SUBTRACT) {
            return VectorOperators.SUB;
        } else if (function == PrimitiveMath.MULTIPLY) {
            return VectorOperators.MUL;
        } else if (function == PrimitiveMath.DIVIDE) {
            return VectorOperators.DIV;
        } else {
            return null;
        }
    }

}
