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
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.matrix.decomposition.function.ExchangeColumns;
import org.ojalgo.matrix.decomposition.function.NegateColumn;
import org.ojalgo.matrix.decomposition.function.RotateRight;
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
        final int tmpDiagDim = (int) ((DiagonalAccess<?, ?>) tmpBidiagonal).mainDiagonal.count();

        final double[] s = (tmpBidiagonal).mainDiagonal.toRawCopy1D(); // s
        final double[] e = new double[tmpDiagDim]; // e
        final int tmpOffLength = (tmpBidiagonal).superdiagonal.size();
        for (int i = 0; i < tmpOffLength; i++) {
            e[i] = (tmpBidiagonal).superdiagonal.doubleValue(i);
        }

        final RotateRight q1RotR = tmpQ1 != null ? tmpQ1 : RotateRight.NULL;
        final RotateRight q2RotR = tmpQ2 != null ? tmpQ2 : RotateRight.NULL;
        final ExchangeColumns q1XchgCols = tmpQ1 != null ? tmpQ1 : ExchangeColumns.NULL;
        final ExchangeColumns q2XchgCols = tmpQ2 != null ? tmpQ2 : ExchangeColumns.NULL;
        final NegateColumn q2NegCol = tmpQ1 != null ? tmpQ2 : NegateColumn.NULL;

        SVD1D.toDiagonal(s, e, q1RotR, q2RotR, q1XchgCols, q2XchgCols, q2NegCol);

        final Array1D<Double> tmpDiagonal = Array1D.PRIMITIVE64.wrap(Primitive64Array.wrap(s));

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
