/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
package org.ojalgo.matrix.decomposition;

import java.math.BigDecimal;

import org.ojalgo.access.Access2D;
import org.ojalgo.access.Structure2D;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.ComplexNumber;

/**
 * A general matrix [A] can be factorized by similarity transformations into the form [A]=[Q1][D][Q2]
 * <sup>-1</sup> where:
 * <ul>
 * <li>[A] (m-by-n) is any, real or complex, matrix</li>
 * <li>[D] (r-by-r) or (m-by-n) is, upper or lower, bidiagonal</li>
 * <li>[Q1] (m-by-r) or (m-by-m) is orthogonal</li>
 * <li>[Q2] (n-by-r) or (n-by-n) is orthogonal</li>
 * <li>r = min(m,n)</li>
 * </ul>
 *
 * @author apete
 */
public interface Bidiagonal<N extends Number> extends MatrixDecomposition<N>, MatrixDecomposition.EconomySize<N> {

    interface Factory<N extends Number> extends MatrixDecomposition.Factory<Bidiagonal<N>> {

    }

    public static final Factory<BigDecimal> BIG = new Factory<BigDecimal>() {

        public Bidiagonal<BigDecimal> make(final Structure2D typical) {
            return new BidiagonalDecomposition.Big();
        }

    };

    public static final Factory<ComplexNumber> COMPLEX = new Factory<ComplexNumber>() {

        public Bidiagonal<ComplexNumber> make(final Structure2D typical) {
            return new BidiagonalDecomposition.Complex();
        }

    };

    public static final Factory<Double> PRIMITIVE = new Factory<Double>() {

        public Bidiagonal<Double> make(final Structure2D typical) {
            return new BidiagonalDecomposition.Primitive();
        }

    };

    @SuppressWarnings("unchecked")
    public static <N extends Number> Bidiagonal<N> make(final Access2D<N> typical) {

        final N tmpNumber = typical.get(0, 0);

        if (tmpNumber instanceof BigDecimal) {
            return (Bidiagonal<N>) BIG.make(typical);
        } else if (tmpNumber instanceof ComplexNumber) {
            return (Bidiagonal<N>) COMPLEX.make(typical);
        } else if (tmpNumber instanceof Double) {
            return (Bidiagonal<N>) PRIMITIVE.make(typical);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @deprecated v40 Use {@link #BIG} instead
     */
    @Deprecated
    public static Bidiagonal<BigDecimal> makeBig() {
        return BIG.make();
    }

    /**
     * @deprecated v40 Use {@link #COMPLEX} instead
     */
    @Deprecated
    public static Bidiagonal<ComplexNumber> makeComplex() {
        return COMPLEX.make();
    }

    /**
     * @deprecated v40 Use {@link #PRIMITIVE} instead
     */
    @Deprecated
    public static Bidiagonal<Double> makePrimitive() {
        return PRIMITIVE.make();
    }

    MatrixStore<N> getD();

    MatrixStore<N> getQ1();

    MatrixStore<N> getQ2();

    boolean isUpper();

    default MatrixStore<N> reconstruct() {
        return MatrixUtils.reconstruct(this);
    }

}
