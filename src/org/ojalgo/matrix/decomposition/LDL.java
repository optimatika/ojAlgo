/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
import org.ojalgo.array.BasicArray;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * LDL: [A] = [L][D][L]<sup>H</sup> (or [R]<sup>H</sup>[D][R])
 * </p>
 * <p>
 * [A]<sup>H</sup> = [A] = [L][D][L]<sup>H</sup>
 * </p>
 * <p>
 * If [A] is symmetric (but not necessarily positive definite) then it can be decomposed into [L][D][L]
 * <sup>T</sup> (or [U]<sup>T</sup>[D][U]).
 * </p>
 * <ul>
 * <li>[L] is a unit lower (left) triangular matrix. It has the same dimensions as [this], and ones on the
 * diagonal.</li>
 * <li>[D] is a diagonal matrix. It has the same dimensions as [this].</li>
 * <li>[this] = [L][D][L]<sup>T</sup></li>
 * </ul>
 *
 * @author apete
 */
public interface LDL<N extends Number> extends LDU<N>, MatrixDecomposition.Hermitian<N> {

    interface Factory<N extends Number> extends MatrixDecomposition.Factory<LDL<N>> {

    }

    public static final Factory<BigDecimal> BIG = new Factory<BigDecimal>() {

        public LDL<BigDecimal> make(final Structure2D typical) {
            return new LDLDecomposition.Big();
        }

    };

    public static final Factory<ComplexNumber> COMPLEX = new Factory<ComplexNumber>() {

        public LDL<ComplexNumber> make(final Structure2D typical) {
            return new LDLDecomposition.Complex();
        }

    };

    public static final Factory<Double> PRIMITIVE = new Factory<Double>() {

        public LDL<Double> make(final Structure2D typical) {
            if ((256L < typical.countColumns()) && (typical.count() <= BasicArray.MAX_ARRAY_SIZE)) {
                return new LDLDecomposition.Primitive();
            } else {
                return new RawLDL();
            }
        }

    };

    @SuppressWarnings("unchecked")
    public static <N extends Number> LDL<N> make(final Access2D<N> typical) {

        final N tmpNumber = typical.get(0, 0);

        if (tmpNumber instanceof BigDecimal) {
            return (LDL<N>) BIG.make(typical);
        } else if (tmpNumber instanceof ComplexNumber) {
            return (LDL<N>) COMPLEX.make(typical);
        } else if (tmpNumber instanceof Double) {
            return (LDL<N>) PRIMITIVE.make(typical);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @deprecated v40 Use {@link #BIG}
     */
    @Deprecated
    public static LDL<BigDecimal> makeBig() {
        return BIG.make();
    }

    /**
     * @deprecated v40 Use {@link #COMPLEX}
     */
    @Deprecated
    public static LDL<ComplexNumber> makeComplex() {
        return COMPLEX.make();
    }

    /**
     * @deprecated v40 Use {@link #PRIMITIVE}
     */
    @Deprecated
    public static LDL<Double> makePrimitive() {
        return PRIMITIVE.make();
    }

    default boolean equals(final MatrixStore<N> other, final NumberContext context) {
        return MatrixUtils.equals(other, this, context);
    }

    MatrixStore<N> getD();

    /**
     * Must implement either {@link #getL()} or {@link #getR()}.
     */
    default MatrixStore<N> getL() {
        return this.getR().conjugate();
    }

    /**
     * Must implement either {@link #getL()} or {@link #getR()}.
     */
    default MatrixStore<N> getR() {
        return this.getL().conjugate();
    }

    int getRank();

    default boolean isFullSize() {
        return true;
    }

    boolean isSquareAndNotSingular();

    default MatrixStore<N> reconstruct() {
        return MatrixUtils.reconstruct(this);
    }
}
