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
package org.ojalgo.matrix.store;

import java.math.BigDecimal;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;

/**
 * ZeroStore
 *
 * @author apete
 */
public final class ZeroStore<N extends Number> extends FactoryStore<N> {

    public static interface Factory<N extends Number> {

        ZeroStore<N> make(int rows, int columns);

    }

    public static final ZeroStore.Factory<BigDecimal> BIG = new ZeroStore.Factory<BigDecimal>() {

        public ZeroStore<BigDecimal> make(final int rows, final int columns) {
            return ZeroStore.makeBig(rows, columns);
        }

    };

    public static final ZeroStore.Factory<ComplexNumber> COMPLEX = new ZeroStore.Factory<ComplexNumber>() {

        public ZeroStore<ComplexNumber> make(final int rows, final int columns) {
            return ZeroStore.makeComplex(rows, columns);
        }

    };

    public static final ZeroStore.Factory<Double> PRIMITIVE = new ZeroStore.Factory<Double>() {

        public ZeroStore<Double> make(final int rows, final int columns) {
            return ZeroStore.makePrimitive(rows, columns);
        }

    };

    public static ZeroStore<BigDecimal> makeBig(final int rows, final int columns) {
        return new ZeroStore<>(BigDenseStore.FACTORY, rows, columns);
    }

    public static ZeroStore<ComplexNumber> makeComplex(final int rows, final int columns) {
        return new ZeroStore<>(ComplexDenseStore.FACTORY, rows, columns);

    }

    public static ZeroStore<Double> makePrimitive(final int rows, final int columns) {
        return new ZeroStore<>(PrimitiveDenseStore.FACTORY, rows, columns);
    }

    private final N myNumberZero;
    private final Scalar<N> myScalarZero;

    public ZeroStore(final PhysicalStore.Factory<N, ?> factory, final int rows, final int columns) {

        super(rows, columns, factory);

        myScalarZero = factory.scalar().zero();
        myNumberZero = myScalarZero.getNumber();
    }

    @SuppressWarnings("unused")
    private ZeroStore(final PhysicalStore.Factory<N, ?> aFactory) {

        this(aFactory, 0, 0);

        ProgrammingError.throwForIllegalInvocation();
    }

    @Override
    public MatrixStore<N> add(final MatrixStore<N> addend) {
        return addend;
    }

    @Override
    public PhysicalStore<N> conjugate() {
        return this.factory().makeZero(this.getColDim(), this.getRowDim());
    }

    @Override
    public PhysicalStore<N> copy() {
        return this.factory().makeZero(this.getRowDim(), this.getColDim());
    }

    @Override
    public double doubleValue(final long anInd) {
        return PrimitiveMath.ZERO;
    }

    public double doubleValue(final long aRow, final long aCol) {
        return PrimitiveMath.ZERO;
    }

    public int firstInColumn(final int col) {
        return this.getRowDim();
    }

    public int firstInRow(final int row) {
        return this.getColDim();
    }

    public N get(final long aRow, final long aCol) {
        return myNumberZero;
    }

    @Override
    public int limitOfColumn(final int col) {
        return 0;
    }

    @Override
    public int limitOfRow(final int row) {
        return 0;
    }

    @Override
    public ZeroStore<N> multiply(final Access1D<N> right) {
        return new ZeroStore<N>(this.factory(), this.getRowDim(), (int) (right.count() / this.getColDim()));
    }

    @Override
    public ZeroStore<N> multiplyLeft(final Access1D<N> leftMtrx) {
        return new ZeroStore<N>(this.factory(), (int) (leftMtrx.count() / this.getRowDim()), this.getColDim());
    }

    @Override
    public MatrixStore<N> scale(final N scalar) {
        return this;
    }

    public Scalar<N> toScalar(final long row, final long column) {
        return myScalarZero;
    }

    @Override
    public PhysicalStore<N> transpose() {
        return this.factory().makeZero(this.getColDim(), this.getRowDim());
    }

}
