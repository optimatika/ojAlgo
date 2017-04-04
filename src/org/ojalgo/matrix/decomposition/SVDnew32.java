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

import static org.ojalgo.constant.PrimitiveMath.*;

import java.math.BigDecimal;

import org.ojalgo.access.Access2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.scalar.ComplexNumber;

/**
 * Orginalet, sedan ett tag Based on SVDnew2, but with transposing so that calculations are always made on a
 * matrix that "isAspectRationNormal". Based on SVDnew5, but with Rotation replaced by the new alternative.
 *
 * @author apete
 */
abstract class SVDnew32<N extends Number & Comparable<N>> extends SingularValueDecomposition<N> {

    static final class Big extends SVDnew32<BigDecimal> {

        Big() {
            super(BigDenseStore.FACTORY, new BidiagonalDecomposition.Big());
        }

    }

    static final class Complex extends SVDnew32<ComplexNumber> {

        Complex() {
            super(ComplexDenseStore.FACTORY, new BidiagonalDecomposition.Complex());
        }

    }

    static final class Primitive extends SVDnew32<Double> {

        Primitive() {
            super(PrimitiveDenseStore.FACTORY, new BidiagonalDecomposition.Primitive());
        }

    }

    /**
     * ≈ 1.6E-291
     */
    @Deprecated
    static final double TINY = PrimitiveFunction.POW.invoke(2.0, -966.0);

    protected SVDnew32(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> factory, final BidiagonalDecomposition<N> bidiagonal) {
        super(factory, bidiagonal);
    }

    public boolean isOrdered() {
        return true;
    }

    public boolean isSolvable() {
        return this.isComputed();
    }

    @Override
    protected boolean doCompute(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix, final boolean singularValuesOnly, final boolean fullSize) {

        this.computeBidiagonal(matrix, fullSize);

        final DiagonalArray1D<N> tmpBidiagonal = this.getBidiagonalAccessD();

        final DecompositionStore<N> tmpQ1 = singularValuesOnly ? null : this.getBidiagonalQ1();
        final DecompositionStore<N> tmpQ2 = singularValuesOnly ? null : this.getBidiagonalQ2();

        final Array1D<Double> tmpDiagonal = SVD1D.toDiagonal(tmpBidiagonal, tmpQ1, tmpQ2);

        this.setSingularValues(tmpDiagonal);

        return this.computed(true);
    }

    @Override
    protected MatrixStore<N> makeD() {
        return this.wrap(new DiagonalArray1D<>(this.getSingularValues(), null, null, ZERO)).get();
    }

    @Override
    protected MatrixStore<N> makeQ1() {
        return this.getBidiagonalQ1();
    }

    @Override
    protected MatrixStore<N> makeQ2() {
        return this.getBidiagonalQ2();
    }

    @Override
    protected Array1D<Double> makeSingularValues() {
        throw new IllegalStateException("Should never have to be called!");
    }
}
