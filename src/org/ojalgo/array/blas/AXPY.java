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
package org.ojalgo.array.blas;

import java.math.BigDecimal;

import org.ojalgo.access.Mutate1D;
import org.ojalgo.function.BigFunction;
import org.ojalgo.scalar.Scalar;

/**
 * The ?axpy routines perform a vector-vector operation defined as y := a*x + y where: a is a scalar x and y
 * are vectors each with a number of elements that equals n. <code>y = y + a * x</code>
 *
 * @author apete
 */
public abstract class AXPY implements BLAS1 {

    public static int THRESHOLD = 128;

    public static void invoke(final BigDecimal[] y, final int basey, final BigDecimal a, final BigDecimal[] x, final int basex, final int first,
            final int limit) {
        for (int i = first; i < limit; i++) {
            y[basey + i] = BigFunction.ADD.invoke(y[basey + i], BigFunction.MULTIPLY.invoke(a, x[basex + i])); // y += a*x
        }
    }

    public static void invoke(final double[] y, final int basey, final double a, final double[] x, final int basex, final int first, final int limit) {
        for (int i = first; i < limit; i++) {
            y[basey + i] += a * x[basex + i];
        }
    }

    public static void invoke(final Mutate1D y, final double a, final BigDecimal[] x) {
        final BigDecimal tmpA = new BigDecimal(a);
        for (int i = 0; i < x.length; i++) {
            y.add(i, x[i].multiply(tmpA));
        }
    }

    public static void invoke(final Mutate1D y, final double a, final double[] x) {
        for (int i = 0; i < x.length; i++) {
            y.add(i, a * x[i]);
        }
    }

    public static void invoke(final Mutate1D y, final double a, final float[] x) {
        for (int i = 0; i < x.length; i++) {
            y.add(i, a * x[i]);
        }
    }

    public static <N extends Number & Scalar<N>> void invoke(final Mutate1D y, final double a, final N[] x) {
        for (int i = 0; i < x.length; i++) {
            y.add(i, x[i].multiply(a).get());
        }
    }

    public static <N extends Number & Scalar<N>> void invoke(final N[] y, final int basey, final N a, final N[] x, final int basex, final int first,
            final int limit) {
        for (int i = first; i < limit; i++) {
            y[basey + i] = y[basey + i].add(a.multiply(x[basex + i])).get();
        }
    }

}
