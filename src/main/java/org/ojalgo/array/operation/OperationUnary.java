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

import org.ojalgo.array.PrimitiveR032;
import org.ojalgo.array.PrimitiveR064;
import org.ojalgo.function.BinaryFunction.FixedFirst;
import org.ojalgo.function.BinaryFunction.FixedSecond;
import org.ojalgo.function.ParameterFunction.FixedParameter;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.structure.Access1D;

public final class OperationUnary implements ArrayOperation {

    public static int THRESHOLD = 256;

    public static void invoke(final double[] data, final int first, final int limit, final int step, final Access1D<Double> values,
            final UnaryFunction<Double> function) {
        if (values instanceof PrimitiveR064) {
            OperationUnary.invoke(data, first, limit, step, ((PrimitiveR064) values).data, function);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(values.doubleValue(i));
            }
        }
    }

    public static void invoke(final float[] data, final int first, final int limit, final int step, final Access1D<Double> values,
            final UnaryFunction<Double> function) {
        if (values instanceof PrimitiveR032) {
            OperationUnary.invoke(data, first, limit, step, ((PrimitiveR032) values).data, function);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(values.floatValue(i));
            }
        }
    }

    public static void invoke(final float[] data, final int first, final int limit, final int step, final float[] values,
            final UnaryFunction<Double> function) {
        if (function == PrimitiveMath.NEGATE) {
            CorePrimitiveOperation.negate(data, first, limit, step, values);
        } else if (function instanceof FixedFirst<?>) {
            final FixedFirst<Double> tmpFunc = (FixedFirst<Double>) function;
            OperationBinary.invoke(data, first, limit, step, tmpFunc.floatValue(), tmpFunc.getFunction(), values);
        } else if (function instanceof FixedSecond<?>) {
            final FixedSecond<Double> tmpFunc = (FixedSecond<Double>) function;
            OperationBinary.invoke(data, first, limit, step, values, tmpFunc.getFunction(), tmpFunc.floatValue());
        } else if (function instanceof FixedParameter<?>) {
            final FixedParameter<Double> tmpFunc = (FixedParameter<Double>) function;
            OperationParameter.invoke(data, first, limit, step, values, tmpFunc.getFunction(), tmpFunc.getParameter());
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(values[i]);
            }
        }
    }

    public static <N extends Comparable<N>> void invoke(final N[] data, final int first, final int limit, final int step, final Access1D<N> value,
            final UnaryFunction<N> function) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(value.get(i));
        }
    }

    static void invoke(final double[] data, final int first, final int limit, final int step, final double[] values, final UnaryFunction<Double> function) {
        if (function == PrimitiveMath.NEGATE) {
            CorePrimitiveOperation.negate(data, first, limit, step, values);
        } else if (function instanceof FixedFirst<?>) {
            final FixedFirst<Double> tmpFunc = (FixedFirst<Double>) function;
            OperationBinary.invoke(data, first, limit, step, tmpFunc.doubleValue(), tmpFunc.getFunction(), values);
        } else if (function instanceof FixedSecond<?>) {
            final FixedSecond<Double> tmpFunc = (FixedSecond<Double>) function;
            OperationBinary.invoke(data, first, limit, step, values, tmpFunc.getFunction(), tmpFunc.doubleValue());
        } else if (function instanceof FixedParameter<?>) {
            final FixedParameter<Double> tmpFunc = (FixedParameter<Double>) function;
            OperationParameter.invoke(data, first, limit, step, values, tmpFunc.getFunction(), tmpFunc.getParameter());
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(values[i]);
            }
        }
    }

}
