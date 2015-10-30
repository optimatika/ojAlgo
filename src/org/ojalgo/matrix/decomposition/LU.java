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
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.ComplexNumber;

/**
 * LU: [A] = [L][U]
 * <p>
 * Decomposes [this] into [L] and [U] (with pivot order information in an int[]) where:
 * </p>
 * <ul>
 * <li>[L] is a unit lower (left) triangular matrix. It has the same number of rows as [this], and ones on the
 * diagonal.</li>
 * <li>[U] is an upper (right) triangular matrix. It has the same number of columns as [this].</li>
 * <li>[this] = [L][U] (with reordered rows according to the pivot order)</li>
 * </ul>
 * <p>
 * Note: The number of columns in [L] and the number of rows in [U] is not specified by this interface.
 * </p>
 * <p>
 * The LU decomposition always exists - the compute method should always succeed - even for non-square and/or
 * singular matrices. The primary use of the LU decomposition is in the solution of systems of simultaneous
 * linear equations. That will, however, only work for square non-singular matrices.
 * </p>
 *
 * @author apete
 */
public interface LU<N extends Number> extends LDU<N> {

    @SuppressWarnings("unchecked")
    public static <N extends Number> LU<N> make(final Access2D<N> typical) {

        final N tmpNumber = typical.get(0, 0);

        if (tmpNumber instanceof BigDecimal) {
            return (LU<N>) new LUDecomposition.Big();
        } else if (tmpNumber instanceof ComplexNumber) {
            return (LU<N>) new LUDecomposition.Complex();
        } else if (tmpNumber instanceof Double) {
            if ((16L < typical.countColumns()) && (typical.count() <= BasicArray.MAX_ARRAY_SIZE)) {
                return (LU<N>) new LUDecomposition.Primitive();
            } else {
                return (LU<N>) new RawLU();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static LU<BigDecimal> makeBig() {
        return new LUDecomposition.Big();
    }

    public static LU<ComplexNumber> makeComplex() {
        return new LUDecomposition.Complex();
    }

    public static LU<Double> makePrimitive() {
        return new LUDecomposition.Primitive();
    }

    /**
     * The normal {@link #decompose(ElementsSupplier)} method must handle cases where pivoting is required. If
     * you know that pivoting is not needed you may call this method instead - it may be faster. Note that the
     * algorithm implementation may still pivot. Pivoting is optional not forbidden (or required).
     */
    boolean computeWithoutPivoting(ElementsSupplier<N> matrix);

    MatrixStore<N> getL();

    /**
     * This can be used to create a [P] matrix..
     */
    int[] getPivotOrder();

    int getRank();

    /**
     * http://en.wikipedia.org/wiki/Row_echelon_form <br>
     * <br>
     * This is the same as [D][U]. Together with the pivotOrder and [L] this constitutes an alternative, more
     * compact, way to express the decomposition.
     *
     * @see #getPivotOrder()
     * @see #getL()
     */
    MatrixStore<N> getU();

    boolean isSquareAndNotSingular();

    default MatrixStore<N> reconstruct() {
        return MatrixUtils.reconstruct(this);
    }

}
