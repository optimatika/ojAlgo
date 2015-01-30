/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.ComplexNumber;

/**
 * Hessenberg: [A] = [Q][H][Q]<sup>T</sup> A general square matrix [A] can be decomposed by orthogonal similarity
 * transformations into the form [A]=[Q][H][Q]<sup>T</sup> where
 * <ul>
 * <li>[H] is upper (or lower) hessenberg matrix</li>
 * <li>[Q] is orthogonal/unitary</li>
 * </ul>
 *
 * @author apete
 */
public interface Hessenberg<N extends Number> extends MatrixDecomposition<N> {

    @SuppressWarnings("unchecked")
    public static <N extends Number> Hessenberg<N> make(final Access2D<N> typical) {

        final N tmpNumber = typical.get(0, 0);

        if (tmpNumber instanceof BigDecimal) {
            return (Hessenberg<N>) Hessenberg.makeBig();
        } else if (tmpNumber instanceof ComplexNumber) {
            return (Hessenberg<N>) Hessenberg.makeComplex();
        } else if (tmpNumber instanceof Double) {
            return (Hessenberg<N>) Hessenberg.makePrimitive();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static Hessenberg<BigDecimal> makeBig() {
        return new HessenbergDecomposition.Big();
    }

    public static Hessenberg<ComplexNumber> makeComplex() {
        return new HessenbergDecomposition.Complex();
    }

    public static Hessenberg<Double> makePrimitive() {
        return new HessenbergDecomposition.Primitive();
    }

    boolean compute(Access2D<?> matrix, boolean upper);

    MatrixStore<N> getH();

    MatrixStore<N> getQ();

    boolean isUpper();

}
