/*
 * Copyright 1997-2020 Optimatika
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

import org.ojalgo.array.DenseArray;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;

/**
 * <p>
 * Contents in this package loosely corresponds to BLAS. The exact selection of operations and their API:s are
 * entirely dictated by the requirements of the various {@linkplain org.ojalgo.matrix.store.MatrixStore}
 * implementations.
 * </p>
 * <ul>
 * <li>http://en.wikipedia.org/wiki/Basic_Linear_Algebra_Subprograms</li>
 * <li>http://www.netlib.org/blas/</li>
 * <li>http://www.netlib.org/blas/faq.html</li>
 * <li>http://www.netlib.org/lapack/lug/node145.html</li>
 * </ul>
 * Basic Linear Algebra Subprograms (BLAS) Level 1 contains vector operations.
 * <ul>
 * <li><a href="http://en.wikipedia.org/wiki/Basic_Linear_Algebra_Subprograms#Level_1">BLAS Level 1 @
 * WikipediA</a></li>
 * <li><a href="http://www.netlib.org/blas/#_level_1">BLAS Level 1 @ Netlib</a></li>
 * <li><a href="https://software.intel.com/en-us/node/520730">BLAS Level 1 @ Intel</a></li>
 * </ul>
 * For each operation there should be 2 sets of implementations:
 * <ol>
 * <li>Optimised to be the implementations for the {@linkplain DenseArray} instances.</li>
 * <li>Optimised to be building blocks for higher level algorithms</li>
 * </ol>
 *
 * @author apete
 */
public interface BLAS1 extends ArrayOperation {

    @FunctionalInterface
    public interface GenericToInt<T> {

        int invoke(final T[] data, final int first, final int limit, final int step);

    }

    @FunctionalInterface
    public interface PrimitiveToDouble {

        double invoke(final double[] data, final int first, final int limit, final int step);

    }

    @FunctionalInterface
    public interface PrimitiveToInt {

        int invoke(final double[] data, final int first, final int limit, final int step);

    }

    GenericToInt<BigDecimal> BAMAX = AMAX::invoke;

    GenericToInt<ComplexNumber> CAMAX = AMAX::invoke;

    PrimitiveToInt DAMAX = AMAX::invoke;
    PrimitiveToInt DAMIN = AMIN::invoke;
    PrimitiveToDouble DASUM = ASUM::invoke;

    GenericToInt<Quaternion> QAMAX = AMAX::invoke;

    GenericToInt<RationalNumber> RAMAX = AMAX::invoke;

    static double fma(final double m1, final double m2, final double a) {
        return (m1 * m2) + a;
    }

}
