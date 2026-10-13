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

import java.util.function.DoubleUnaryOperator;

import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.MissingMath;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;

public abstract class UnaryVectorOperation extends VectorOperation {

    public static void invoke(final double[] data, final DoubleUnaryOperator function, final double[] args) {

        int limit = MissingMath.min(data.length, args.length);

        int i = 0;

        VectorOperators.Unary operator = UnaryVectorOperation.operator(function);

        if (operator != null) {

            int length = SPECIES_DOUBLE.length();
            int bound = SPECIES_DOUBLE.loopBound(limit);

            for (; i < bound; i += length) {
                DoubleVector av = DoubleVector.fromArray(SPECIES_DOUBLE, args, i);
                DoubleVector dv = av.lanewise(operator);
                dv.intoArray(data, i);
            }
        }

        for (; i < limit; i++) {
            data[i] = function.applyAsDouble(args[i]);
        }
    }

    public static void invoke(final double[] data, final DoubleUnaryOperator function, final int first, final int limit) {
        UnaryVectorOperation.invoke(data, function, first, limit, 1); // TODO Inline when it's done
    }

    public static void invoke(final double[] data, final DoubleUnaryOperator function, final int first, final int limit, final int step) {

        int i = first;

        VectorOperators.Unary operator = UnaryVectorOperation.operator(function);

        if (operator != null) {

            int length = SPECIES_DOUBLE.length();
            int[] map = new int[length];
            for (int s = 0; s < length; s++) {
                map[s] = s * step;
            }
            int stride = length * step;

            int bound = first + SPECIES_DOUBLE.loopBound(limit - first);
            for (; i < bound; i += stride) {
                DoubleVector av = DoubleVector.fromArray(SPECIES_DOUBLE, data, i, map, 0);
                DoubleVector dv = av.lanewise(operator);
                dv.intoArray(data, i, map, 0);
            }
        }

        for (; i < limit; i += step) {
            data[i] = function.applyAsDouble(data[i]);
        }
    }

    public static void invoke(final float[] data, final UnaryFunction<Double> function, final float[] args) {

        int limit = MissingMath.min(data.length, args.length);

        int i = 0;

        VectorOperators.Unary operator = UnaryVectorOperation.operator(function);

        if (operator != null) {

            int length = SPECIES_FLOAT.length();
            int bound = SPECIES_FLOAT.loopBound(limit);

            for (; i < bound; i += length) {
                FloatVector av = FloatVector.fromArray(SPECIES_FLOAT, args, i);
                FloatVector dv = av.lanewise(operator);
                dv.intoArray(data, i);
            }
        }

        for (; i < limit; i++) {
            data[i] = function.invoke(args[i]);
        }
    }

    public static void invoke(final float[] data, final UnaryFunction<Double> function, final int first, final int limit) {
        UnaryVectorOperation.invoke(data, function, first, limit, 1); // TODO Inline when it's done
    }

    public static void invoke(final float[] data, final UnaryFunction<Double> function, final int first, final int limit, final int step) {

        int i = first;

        VectorOperators.Unary operator = UnaryVectorOperation.operator(function);

        if (operator != null) {

            int length = SPECIES_FLOAT.length();
            int[] map = new int[length];
            for (int s = 0; s < length; s++) {
                map[s] = s * step;
            }
            int stride = length * step;

            int bound = first + SPECIES_DOUBLE.loopBound(limit - first);
            for (; i < bound; i += stride) {
                FloatVector av = FloatVector.fromArray(SPECIES_FLOAT, data, i, map, 0);
                FloatVector dv = av.lanewise(operator);
                dv.intoArray(data, i, map, 0);
            }
        }

        for (; i < limit; i += step) {
            data[i] = function.invoke(data[i]);
        }
    }

    static VectorOperators.Unary operator(final DoubleUnaryOperator function) {
        if (function == PrimitiveMath.NEGATE) {
            return VectorOperators.NEG;
        } else if (function == PrimitiveMath.ABS) {
            return VectorOperators.ABS;
        } else if (function == PrimitiveMath.SQRT) {
            return VectorOperators.SQRT;
        } else if (function == PrimitiveMath.CBRT) {
            return VectorOperators.CBRT;
        } else {
            return null;
        }
    }

}
