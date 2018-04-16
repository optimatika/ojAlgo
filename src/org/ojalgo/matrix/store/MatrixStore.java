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
package org.ojalgo.matrix.store;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.Structure2D;
import org.ojalgo.algebra.NormedVectorSpace;
import org.ojalgo.algebra.Operation;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * A {@linkplain MatrixStore} is a two dimensional store of numbers/scalars.
 * </p>
 * <p>
 * A {@linkplain MatrixStore} extends {@linkplain Access2D} (as well as
 * {@linkplain org.ojalgo.access.Access2D.Visitable} and {@linkplain org.ojalgo.access.Access2D.Elements}) and
 * defines some futher funtionality - mainly matrix multiplication.
 * </p>
 * <p>
 * This interface does not define any methods that require implementations to alter the matrix. Either the
 * methods return matrix elements, some meta data or produce new instances.
 * </p>
 * <p>
 * The methods {@linkplain #conjugate()}, {@linkplain #copy()} and {@linkplain #transpose()} return
 * {@linkplain PhysicalStore} instances. {@linkplain PhysicalStore} extends {@linkplain MatrixStore}. It
 * defines additional methods, and is mutable.
 * </p>
 *
 * @author apete
 */
public interface MatrixStore<N extends Number> extends ElementsSupplier<N>, Access2D<N>, Access2D.Elements, Access2D.Visitable<N>, Access2D.Aggregatable<N>,
        Structure2D.ReducibleTo1D<ElementsSupplier<N>>, Access2D.Sliceable<N>, NormedVectorSpace<MatrixStore<N>, N>, Operation.Multiplication<MatrixStore<N>> {

    public static interface Factory<N extends Number> {

        MatrixStore.LogicalBuilder<N> makeIdentity(int dimension);

        MatrixStore.LogicalBuilder<N> makeSingle(N element);

        MatrixStore.LogicalBuilder<N> makeWrapper(Access2D<?> access);

        MatrixStore.LogicalBuilder<N> makeZero(int rowsCount, int columnsCount);

    }

    /**
     * A builder that lets you logically construct matrices and/or encode element structure.
     *
     * @author apete
     */
    public static final class LogicalBuilder<N extends Number> implements ElementsSupplier<N> {

        @SafeVarargs
        static <N extends Number> MatrixStore<N> buildColumn(final int aMinRowDim, final MatrixStore<N>... aColStore) {
            MatrixStore<N> retVal = aColStore[0];
            for (int i = 1; i < aColStore.length; i++) {
                retVal = new AboveBelowStore<>(retVal, aColStore[i]);
            }
            final int tmpRowDim = (int) retVal.countRows();
            if (tmpRowDim < aMinRowDim) {
                retVal = new AboveBelowStore<>(retVal, new ZeroStore<>(retVal.physical(), aMinRowDim - tmpRowDim, (int) retVal.countColumns()));
            }
            return retVal;
        }

        @SafeVarargs
        static <N extends Number> MatrixStore<N> buildColumn(final PhysicalStore.Factory<N, ?> factory, final int aMinRowDim, final N... aColStore) {
            MatrixStore<N> retVal = factory.columns(aColStore);
            final int tmpRowDim = (int) retVal.countRows();
            if (tmpRowDim < aMinRowDim) {
                retVal = new AboveBelowStore<>(retVal, new ZeroStore<>(factory, aMinRowDim - tmpRowDim, (int) retVal.countColumns()));
            }
            return retVal;
        }

        @SafeVarargs
        static <N extends Number> MatrixStore<N> buildRow(final int aMinColDim, final MatrixStore<N>... aRowStore) {
            MatrixStore<N> retVal = aRowStore[0];
            for (int j = 1; j < aRowStore.length; j++) {
                retVal = new LeftRightStore<>(retVal, aRowStore[j]);
            }
            final int tmpColDim = (int) retVal.countColumns();
            if (tmpColDim < aMinColDim) {
                retVal = new LeftRightStore<>(retVal, new ZeroStore<>(retVal.physical(), (int) retVal.countRows(), aMinColDim - tmpColDim));
            }
            return retVal;
        }

        @SafeVarargs
        static <N extends Number> MatrixStore<N> buildRow(final PhysicalStore.Factory<N, ?> factory, final int aMinColDim, final N... aRowStore) {
            MatrixStore<N> retVal = new TransposedStore<>(factory.columns(aRowStore));
            final int tmpColDim = (int) retVal.countColumns();
            if (tmpColDim < aMinColDim) {
                retVal = new LeftRightStore<>(retVal, new ZeroStore<>(factory, (int) retVal.countRows(), aMinColDim - tmpColDim));
            }
            return retVal;
        }

        private MatrixStore<N> myStore;

        @SuppressWarnings("unused")
        private LogicalBuilder() {

            this(null);

            ProgrammingError.throwForIllegalInvocation();
        }

        LogicalBuilder(final MatrixStore<N> matrixStore) {

            super();

            myStore = matrixStore;
        }

        public final LogicalBuilder<N> above(final int aRowDim) {
            final ZeroStore<N> tmpUpperStore = new ZeroStore<>(myStore.physical(), aRowDim, (int) myStore.countColumns());
            myStore = new AboveBelowStore<>(tmpUpperStore, myStore);
            return this;
        }

        @SafeVarargs
        public final LogicalBuilder<N> above(final MatrixStore<N>... upperStore) {
            final MatrixStore<N> tmpUpperStore = LogicalBuilder.buildRow((int) myStore.countColumns(), upperStore);
            myStore = new AboveBelowStore<>(tmpUpperStore, myStore);
            return this;
        }

        @SafeVarargs
        public final LogicalBuilder<N> above(final N... anUpperStore) {
            final MatrixStore<N> tmpUpperStore = LogicalBuilder.buildRow(myStore.physical(), (int) myStore.countColumns(), anUpperStore);
            myStore = new AboveBelowStore<>(tmpUpperStore, myStore);
            return this;
        }

        public final LogicalBuilder<N> below(final int aRowDim) {
            final ZeroStore<N> tmpLowerStore = new ZeroStore<>(myStore.physical(), aRowDim, (int) myStore.countColumns());
            myStore = new AboveBelowStore<>(myStore, tmpLowerStore);
            return this;
        }

        @SafeVarargs
        public final LogicalBuilder<N> below(final MatrixStore<N>... aLowerStore) {
            final MatrixStore<N> tmpLowerStore = LogicalBuilder.buildRow((int) myStore.countColumns(), aLowerStore);
            myStore = new AboveBelowStore<>(myStore, tmpLowerStore);
            return this;
        }

        @SafeVarargs
        public final LogicalBuilder<N> below(final N... aLowerStore) {
            final MatrixStore<N> tmpLowerStore = LogicalBuilder.buildRow(myStore.physical(), (int) myStore.countColumns(), aLowerStore);
            myStore = new AboveBelowStore<>(myStore, tmpLowerStore);
            return this;
        }

        public final LogicalBuilder<N> bidiagonal(final boolean upper, final boolean assumeOne) {
            if (upper) {
                myStore = new UpperTriangularStore<>(new LowerHessenbergStore<>(myStore), assumeOne);
            } else {
                myStore = new LowerTriangularStore<>(new UpperHessenbergStore<>(myStore), assumeOne);
            }
            return this;
        }

        public final LogicalBuilder<N> column(final int... col) {
            myStore = new ColumnsStore<>(myStore, col);
            return this;
        }

        public final LogicalBuilder<N> conjugate() {
            if (myStore instanceof ConjugatedStore) {
                myStore = ((ConjugatedStore<N>) myStore).getOriginal();
            } else {
                myStore = new ConjugatedStore<>(myStore);
            }
            return this;
        }

        public final PhysicalStore<N> copy() {
            return myStore.copy();
        }

        public final long count() {
            return myStore.count();
        }

        public final long countColumns() {
            return myStore.countColumns();
        }

        public final long countRows() {
            return myStore.countRows();
        }

        public final LogicalBuilder<N> diagonal(final boolean assumeOne) {
            myStore = new UpperTriangularStore<>(new LowerTriangularStore<>(myStore, assumeOne), assumeOne);
            return this;
        }

        @SafeVarargs
        public final LogicalBuilder<N> diagonally(final MatrixStore<N>... aDiagonalStore) {

            final PhysicalStore.Factory<N, ?> tmpFactory = myStore.physical();

            MatrixStore<N> tmpDiagonalStore;
            for (int ij = 0; ij < aDiagonalStore.length; ij++) {

                tmpDiagonalStore = aDiagonalStore[ij];

                final int tmpBaseRowDim = (int) myStore.countRows();
                final int tmpBaseColDim = (int) myStore.countColumns();

                final int tmpDiagRowDim = (int) tmpDiagonalStore.countRows();
                final int tmpDiagColDim = (int) tmpDiagonalStore.countColumns();

                final MatrixStore<N> tmpRightStore = new ZeroStore<>(tmpFactory, tmpBaseRowDim, tmpDiagColDim);
                final MatrixStore<N> tmpAboveStore = new LeftRightStore<>(myStore, tmpRightStore);

                final MatrixStore<N> tmpLeftStore = new ZeroStore<>(tmpFactory, tmpDiagRowDim, tmpBaseColDim);
                final MatrixStore<N> tmpBelowStore = new LeftRightStore<>(tmpLeftStore, tmpDiagonalStore);

                myStore = new AboveBelowStore<>(tmpAboveStore, tmpBelowStore);
            }

            return this;
        }

        public final MatrixStore<N> get() {
            return myStore;
        }

        public final LogicalBuilder<N> hermitian(final boolean upper) {
            if (upper) {
                myStore = new UpperHermitianStore<>(myStore);
            } else {
                myStore = new LowerHermitianStore<>(myStore);
            }
            return this;
        }

        public final LogicalBuilder<N> hessenberg(final boolean upper) {
            if (upper) {
                myStore = new UpperHessenbergStore<>(myStore);
            } else {
                myStore = new LowerHessenbergStore<>(myStore);
            }
            return this;
        }

        public final LogicalBuilder<N> left(final int aColDim) {
            final MatrixStore<N> tmpLeftStore = new ZeroStore<>(myStore.physical(), (int) myStore.countRows(), aColDim);
            myStore = new LeftRightStore<>(tmpLeftStore, myStore);
            return this;
        }

        @SafeVarargs
        public final LogicalBuilder<N> left(final MatrixStore<N>... aLeftStore) {
            final MatrixStore<N> tmpLeftStore = LogicalBuilder.buildColumn((int) myStore.countRows(), aLeftStore);
            myStore = new LeftRightStore<>(tmpLeftStore, myStore);
            return this;
        }

        @SafeVarargs
        public final LogicalBuilder<N> left(final N... aLeftStore) {
            final MatrixStore<N> tmpLeftStore = LogicalBuilder.buildColumn(myStore.physical(), (int) myStore.countRows(), aLeftStore);
            myStore = new LeftRightStore<>(tmpLeftStore, myStore);
            return this;
        }

        /**
         * Setting either limit to &lt; 0 is interpreted as "no limit" (useful when you only want to limit
         * either the rows or columns, and don't know the size of the other)
         */
        public final LogicalBuilder<N> limits(final int rowLimit, final int columnLimit) {
            myStore = new LimitStore<>(rowLimit < 0 ? (int) myStore.countRows() : rowLimit, columnLimit < 0 ? (int) myStore.countColumns() : columnLimit,
                    myStore);
            return this;
        }

        public final LogicalBuilder<N> offsets(final int rowOffset, final int columnOffset) {
            myStore = new OffsetStore<>(myStore, rowOffset < 0 ? 0 : rowOffset, columnOffset < 0 ? 0 : columnOffset);
            return this;
        }

        public final PhysicalStore.Factory<N, ?> physical() {
            return myStore.physical();
        }

        public final LogicalBuilder<N> right(final int aColDim) {
            final MatrixStore<N> tmpRightStore = new ZeroStore<>(myStore.physical(), (int) myStore.countRows(), aColDim);
            myStore = new LeftRightStore<>(myStore, tmpRightStore);
            return this;
        }

        @SafeVarargs
        public final LogicalBuilder<N> right(final MatrixStore<N>... aRightStore) {
            final MatrixStore<N> tmpRightStore = LogicalBuilder.buildColumn((int) myStore.countRows(), aRightStore);
            myStore = new LeftRightStore<>(myStore, tmpRightStore);
            return this;
        }

        @SafeVarargs
        public final LogicalBuilder<N> right(final N... aRightStore) {
            final MatrixStore<N> tmpRightStore = LogicalBuilder.buildColumn(myStore.physical(), (int) myStore.countRows(), aRightStore);
            myStore = new LeftRightStore<>(myStore, tmpRightStore);
            return this;
        }

        public final LogicalBuilder<N> row(final int... row) {
            myStore = new RowsStore<>(myStore, row);
            return this;
        }

        public final LogicalBuilder<N> superimpose(final int row, final int col, final MatrixStore<N> aStore) {
            myStore = new SuperimposedStore<>(myStore, row, col, aStore);
            return this;
        }

        public final LogicalBuilder<N> superimpose(final int row, final int col, final Number aStore) {
            myStore = new SuperimposedStore<>(myStore, row, col, new SingleStore<>(myStore.physical(), aStore));
            return this;
        }

        public final LogicalBuilder<N> superimpose(final MatrixStore<N> aStore) {
            myStore = new SuperimposedStore<>(myStore, 0, 0, aStore);
            return this;
        }

        public final void supplyTo(final ElementsConsumer<N> receiver) {
            if (receiver.isAcceptable(this)) {
                receiver.accept(this.get());
            } else {
                throw new ProgrammingError("Not acceptable!");
            }
        }

        @Override
        public String toString() {
            return myStore.toString();
        }

        public final LogicalBuilder<N> transpose() {
            if (myStore instanceof TransposedStore) {
                myStore = ((TransposedStore<N>) myStore).getOriginal();
            } else {
                myStore = new TransposedStore<>(myStore);
            }
            return this;
        }

        public final LogicalBuilder<N> triangular(final boolean upper, final boolean assumeOne) {
            if (upper) {
                myStore = new UpperTriangularStore<>(myStore, assumeOne);
            } else {
                myStore = new LowerTriangularStore<>(myStore, assumeOne);
            }
            return this;
        }

        public final LogicalBuilder<N> tridiagonal() {
            myStore = new UpperHessenbergStore<>(new LowerHessenbergStore<>(myStore));
            return this;
        }

    }

    public static final Factory<ComplexNumber> COMPLEX = new Factory<ComplexNumber>() {

        public LogicalBuilder<ComplexNumber> makeIdentity(final int dimension) {
            return new LogicalBuilder<>(new IdentityStore<>(GenericDenseStore.COMPLEX, dimension));
        }

        public LogicalBuilder<ComplexNumber> makeSingle(final ComplexNumber element) {
            return new LogicalBuilder<>(new SingleStore<>(GenericDenseStore.COMPLEX, element));
        }

        public LogicalBuilder<ComplexNumber> makeWrapper(final Access2D<?> access) {
            return new LogicalBuilder<>(new WrapperStore<>(GenericDenseStore.COMPLEX, access));
        }

        public LogicalBuilder<ComplexNumber> makeZero(final int rowsCount, final int columnsCount) {
            return new LogicalBuilder<>(new ZeroStore<>(GenericDenseStore.COMPLEX, rowsCount, columnsCount));
        }

    };

    public static final Factory<RationalNumber> RATIONAL = new Factory<RationalNumber>() {

        public LogicalBuilder<RationalNumber> makeIdentity(final int dimension) {
            return new LogicalBuilder<>(new IdentityStore<>(GenericDenseStore.RATIONAL, dimension));
        }

        public LogicalBuilder<RationalNumber> makeSingle(final RationalNumber element) {
            return new LogicalBuilder<>(new SingleStore<>(GenericDenseStore.RATIONAL, element));
        }

        public LogicalBuilder<RationalNumber> makeWrapper(final Access2D<?> access) {
            return new LogicalBuilder<>(new WrapperStore<>(GenericDenseStore.RATIONAL, access));
        }

        public LogicalBuilder<RationalNumber> makeZero(final int rowsCount, final int columnsCount) {
            return new LogicalBuilder<>(new ZeroStore<>(GenericDenseStore.RATIONAL, rowsCount, columnsCount));
        }

    };

    public static final Factory<Double> PRIMITIVE = new Factory<Double>() {

        public LogicalBuilder<Double> makeIdentity(final int dimension) {
            return new LogicalBuilder<>(new IdentityStore<>(PrimitiveDenseStore.FACTORY, dimension));
        }

        public LogicalBuilder<Double> makeSingle(final Double element) {
            return new LogicalBuilder<>(new SingleStore<>(PrimitiveDenseStore.FACTORY, element));
        }

        public LogicalBuilder<Double> makeWrapper(final Access2D<?> access) {
            return new LogicalBuilder<>(new WrapperStore<>(PrimitiveDenseStore.FACTORY, access));
        }

        public LogicalBuilder<Double> makeZero(final int rowsCount, final int columnsCount) {
            return new LogicalBuilder<>(new ZeroStore<>(PrimitiveDenseStore.FACTORY, rowsCount, columnsCount));
        }

    };

    default MatrixStore<N> add(final MatrixStore<N> addend) {
        return this.operateOnMatching(this.physical().function().add(), addend).get();
    }

    default N aggregateAll(final Aggregator aggregator) {

        final AggregatorFunction<N> tmpVisitor = this.physical().aggregator().get(aggregator);

        this.visitAll(tmpVisitor);

        return tmpVisitor.get();
    }

    default N aggregateColumn(final long row, final long col, final Aggregator aggregator) {

        final AggregatorFunction<N> tmpVisitor = this.physical().aggregator().get(aggregator);

        this.visitColumn(row, col, tmpVisitor);

        return tmpVisitor.get();
    }

    default N aggregateDiagonal(final long row, final long col, final Aggregator aggregator) {

        final AggregatorFunction<N> tmpVisitor = this.physical().aggregator().get(aggregator);

        this.visitDiagonal(row, col, tmpVisitor);

        return tmpVisitor.get();
    }

    default N aggregateRange(final long first, final long limit, final Aggregator aggregator) {

        final AggregatorFunction<N> tmpVisitor = this.physical().aggregator().get(aggregator);

        this.visitRange(first, limit, tmpVisitor);

        return tmpVisitor.get();
    }

    default N aggregateRow(final long row, final long col, final Aggregator aggregator) {

        final AggregatorFunction<N> tmpVisitor = this.physical().aggregator().get(aggregator);

        this.visitRow(row, col, tmpVisitor);

        return tmpVisitor.get();
    }

    /**
     * Returns the conjugate transpose of this matrix. The conjugate transpose is also known as adjoint
     * matrix, adjugate matrix, hermitian adjoint or hermitian transpose. (The conjugate matrix is the complex
     * conjugate of each element. This NOT what is returned here!)
     *
     * @see org.ojalgo.algebra.VectorSpace#conjugate()
     */
    default MatrixStore<N> conjugate() {
        return new ConjugatedStore<>(this);
    }

    /**
     * Each call must produce a new instance.
     *
     * @return A new {@linkplain PhysicalStore} copy.
     */
    default PhysicalStore<N> copy() {

        final PhysicalStore<N> retVal = this.physical().makeZero(this);

        this.supplyTo(retVal);

        return retVal;
    }

    default double doubleValue(final long row, final long col) {
        return this.get(row, col).doubleValue();
    }

    default boolean equals(final MatrixStore<N> other, final NumberContext context) {
        return Access2D.equals(this, other, context);
    }

    /**
     * The default value is simply <code>0</code>, and if all elements are zeros then
     * <code>this.countRows()</code>.
     *
     * @param col The column index
     * @return The row index of the first non-zero element in the specified column
     */
    default int firstInColumn(final int col) {
        return 0;
    }

    /**
     * The default value is simply <code>0</code>, and if all elements are zeros then
     * <code>this.countColumns()</code>.
     *
     * @param row
     * @return The column index of the first non-zero element in the specified row
     */
    default int firstInRow(final int row) {
        return 0;
    }

    default MatrixStore<N> get() {
        return this;
    }

    default boolean isAbsolute(final long row, final long col) {
        return this.toScalar(row, col).isAbsolute();
    }

    default boolean isSmall(final double comparedTo) {
        return PrimitiveScalar.isSmall(comparedTo, this.norm());
    }

    default boolean isSmall(final long row, final long col, final double comparedTo) {
        return this.toScalar(row, col).isSmall(comparedTo);
    }

    /**
     * The default value is simply <code>this.countRows()</code>, and if all elements are zeros then
     * <code>0</code>.
     *
     * @param col
     * @return The row index of the first zero element, after all non-zeros, in the specified column (index of
     *         the last non-zero + 1)
     */
    default int limitOfColumn(final int col) {
        return (int) this.countRows();
    }

    /**
     * The default value is simply <code>this.countColumns()</code>, and if all elements are zeros then
     * <code>0</code>.
     *
     * @param row
     * @return The column index of the first zero element, after all non-zeros, in the specified row (index of
     *         the last non-zero + 1)
     */
    default int limitOfRow(final int row) {
        return (int) this.countColumns();
    }

    default MatrixStore.LogicalBuilder<N> logical() {
        return new MatrixStore.LogicalBuilder<>(this);
    }

    default void multiply(final Access1D<N> right, final ElementsConsumer<N> target) {
        target.fillByMultiplying(this, right);
    }

    default MatrixStore<N> multiply(final double scalar) {
        return this.multiply(this.physical().scalar().cast(scalar));
    }

    default MatrixStore<N> multiply(final MatrixStore<N> right) {

        final long tmpCountRows = this.countRows();
        final long tmpCountColumns = right.count() / this.countColumns();

        final PhysicalStore<N> retVal = this.physical().makeZero(tmpCountRows, tmpCountColumns);

        this.multiply(right, retVal);

        return retVal;
    }

    default MatrixStore<N> multiply(final N scalar) {
        return this.operateOnAll(this.physical().function().multiply().second(scalar)).get();
    }

    /**
     * Assumes [leftAndRight] is a vector and will calulate [leftAndRight]<sup>H</sup>[this][leftAndRight]
     *
     * @param leftAndRight The argument vector
     * @return A scalar (extracted from the resulting 1 x 1 matrix)
     */
    default N multiplyBoth(final Access1D<N> leftAndRight) {

        final PhysicalStore<N> tmpStep1 = this.physical().makeZero(1L, leftAndRight.count());
        final PhysicalStore<N> tmpStep2 = this.physical().makeZero(1L, 1L);

        final PhysicalStore<N> tmpLeft = this.physical().rows(leftAndRight);
        tmpLeft.modifyAll(this.physical().function().conjugate());
        tmpStep1.fillByMultiplying(tmpLeft.conjugate(), this);

        tmpStep2.fillByMultiplying(tmpStep1, leftAndRight);

        return tmpStep2.get(0L);
    }

    default MatrixStore<N> negate() {
        return this.operateOnAll(this.physical().function().negate()).get();
    }

    default double norm() {
        return this.aggregateAll(Aggregator.NORM2).doubleValue();
    }

    /**
     * The <code>premultiply</code> method differs from <code>multiply</code> in 3 ways:
     * <ol>
     * <li>The matrix positions are swapped - left/right.</li>
     * <li>It does NOT return a {@linkplain MatrixStore} but an {@linkplain ElementsSupplier} instead.</li>
     * <li>It accepts an {@linkplain Access1D} as the argument left matrix.</li>
     * </ol>
     *
     * @param left The left matrix
     * @return The matrix product
     */
    default ElementsSupplier<N> premultiply(final Access1D<N> left) {
        return new MatrixPipeline.Multiplication<>(left, this);
    }

    default ElementsSupplier<N> reduceColumns(Aggregator aggregator) {
        return new MatrixPipeline.ColumnsReducer<>(this, aggregator);
    }

    default ElementsSupplier<N> reduceRows(Aggregator aggregator) {
        return new MatrixPipeline.RowsReducer<>(this, aggregator);
    }

    default MatrixStore<N> signum() {
        return this.multiply(PrimitiveMath.ONE / this.norm());
    }

    default Access1D<N> sliceColumn(final long row, final long col) {
        return new Access1D<N>() {

            public long count() {
                return MatrixStore.this.countRows() - row;
            }

            public double doubleValue(final long index) {
                return MatrixStore.this.doubleValue(row + index, col);
            }

            public N get(final long index) {
                return MatrixStore.this.get(row + index, col);
            }

        };
    }

    default Access1D<N> sliceDiagonal(final long row, final long col) {
        return new Access1D<N>() {

            public long count() {
                return Math.min(MatrixStore.this.countRows() - row, MatrixStore.this.countColumns() - col);
            }

            public double doubleValue(final long index) {
                return MatrixStore.this.doubleValue(row + index, col + index);
            }

            public N get(final long index) {
                return MatrixStore.this.get(row + index, col + index);
            }

        };
    }

    default Access1D<N> sliceRange(final long first, final long limit) {
        return new Access1D<N>() {

            public long count() {
                return limit - first;
            }

            public double doubleValue(final long index) {
                return MatrixStore.this.doubleValue(first + index);
            }

            public N get(final long index) {
                return MatrixStore.this.get(first + index);
            }

        };
    }

    default Access1D<N> sliceRow(final long row, final long col) {
        return new Access1D<N>() {

            public long count() {
                return MatrixStore.this.countColumns() - col;
            }

            public double doubleValue(final long index) {
                return MatrixStore.this.doubleValue(row, col + index);
            }

            public N get(final long index) {
                return MatrixStore.this.get(row, col + index);
            }

        };
    }

    default MatrixStore<N> subtract(final MatrixStore<N> subtrahend) {
        return this.operateOnMatching(this.physical().function().subtract(), subtrahend).get();
    }

    default void supplyTo(final ElementsConsumer<N> receiver) {
        receiver.fillMatching(this);
    }

    default Scalar<N> toScalar(final long row, final long column) {
        return this.physical().scalar().convert(this.get(row, column));
    }

    /**
     * @return A transposed matrix instance.
     */
    default MatrixStore<N> transpose() {
        return new TransposedStore<>(this);
    }

    default void visitOne(final long row, final long col, final VoidFunction<N> visitor) {
        visitor.invoke(this.get(row, col));
    }

}
