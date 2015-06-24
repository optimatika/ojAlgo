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
 * If [A] is symmetric (but not necessarily positive definite) then it can be decomposed into
 * [L][D][L]<sup>T</sup> (or [U]<sup>T</sup>[D][U]).
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

    @SuppressWarnings("unchecked")
    public static <N extends Number> LDL<N> make(final Access2D<N> typical) {

        final N tmpNumber = typical.get(0, 0);

        if (tmpNumber instanceof BigDecimal) {
            return (LDL<N>) new LDLDecomposition.Big();
        } else if (tmpNumber instanceof ComplexNumber) {
            return (LDL<N>) new LDLDecomposition.Complex();
        } else if (tmpNumber instanceof Double) {
            if ((256L < typical.countColumns()) && (typical.count() <= BasicArray.MAX_ARRAY_SIZE)) {
                return (LDL<N>) new LDLDecomposition.Primitive();
            } else {
                return (LDL<N>) new RawLDL();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static LDL<BigDecimal> makeBig() {
        return new LDLDecomposition.Big();
    }

    public static LDL<ComplexNumber> makeComplex() {
        return new LDLDecomposition.Complex();
    }

    public static LDL<Double> makePrimitive() {
        return new LDLDecomposition.Primitive();
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
