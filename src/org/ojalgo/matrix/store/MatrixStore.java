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
import org.ojalgo.access.Access2D;
import org.ojalgo.algebra.NormedVectorSpace;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * A {@linkplain MatrixStore} is a two dimensional store of numbers/scalars.
 * </p>
 * <p>
 * A {@linkplain MatrixStore} extends {@linkplain Access2D} (as well as {@linkplain Access2D.Visitable} and
 * {@linkplain Access2D.Elements}) and defines some futher funtionality - mainly matrix multiplication.
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
public interface MatrixStore<N extends Number>
        extends Access2D<N>, Access2D.Elements, Access2D.Visitable<N>, Access2D.Sliceable<N>, NormedVectorSpace<MatrixStore<N>, N>, ElementsSupplier<N> {

    /**
     * A builder that lets you logically construct matrices and/or encode element structure.
     *
     * @author apete
     */
    public static final class Builder<N extends Number> implements ElementsSupplier<N> {

        @SafeVarargs
        static <N extends Number> MatrixStore<N> buildColumn(final int aMinRowDim, final MatrixStore<N>... aColStore) {
            MatrixStore<N> retVal = aColStore[0];
            for (int i = 1; i < aColStore.length; i++) {
                retVal = new AboveBelowStore<N>(retVal, aColStore[i]);
            }
            final int tmpRowDim = (int) retVal.countRows();
            if (tmpRowDim < aMinRowDim) {
                retVal = new AboveBelowStore<N>(retVal, new ZeroStore<N>(retVal.factory(), aMinRowDim - tmpRowDim, (int) retVal.countColumns()));
            }
            return retVal;
        }

        @SafeVarargs
        static <N extends Number> MatrixStore<N> buildColumn(final PhysicalStore.Factory<N, ?> factory, final int aMinRowDim, final N... aColStore) {
            MatrixStore<N> retVal = factory.columns(aColStore);
            final int tmpRowDim = (int) retVal.countRows();
            if (tmpRowDim < aMinRowDim) {
                retVal = new AboveBelowStore<N>(retVal, new ZeroStore<N>(factory, aMinRowDim - tmpRowDim, (int) retVal.countColumns()));
            }
            return retVal;
        }

        @SafeVarargs
        static <N extends Number> MatrixStore<N> buildRow(final int aMinColDim, final MatrixStore<N>... aRowStore) {
            MatrixStore<N> retVal = aRowStore[0];
            for (int j = 1; j < aRowStore.length; j++) {
                retVal = new LeftRightStore<N>(retVal, aRowStore[j]);
            }
            final int tmpColDim = (int) retVal.countColumns();
            if (tmpColDim < aMinColDim) {
                retVal = new LeftRightStore<N>(retVal, new ZeroStore<N>(retVal.factory(), (int) retVal.countRows(), aMinColDim - tmpColDim));
            }
            return retVal;
        }

        @SafeVarargs
        static <N extends Number> MatrixStore<N> buildRow(final PhysicalStore.Factory<N, ?> factory, final int aMinColDim, final N... aRowStore) {
            MatrixStore<N> retVal = new TransposedStore<N>(factory.columns(aRowStore));
            final int tmpColDim = (int) retVal.countColumns();
            if (tmpColDim < aMinColDim) {
                retVal = new LeftRightStore<N>(retVal, new ZeroStore<N>(factory, (int) retVal.countRows(), aMinColDim - tmpColDim));
            }
            return retVal;
        }

        private MatrixStore<N> myStore;

        @SuppressWarnings("unused")
        private Builder() {

            this(null);

            ProgrammingError.throwForIllegalInvocation();
        }

        Builder(final MatrixStore<N> matrixStore) {

            super();

            myStore = matrixStore;
        }

        public final Builder<N> above(final int aRowDim) {
            final ZeroStore<N> tmpUpperStore = new ZeroStore<N>(myStore.factory(), aRowDim, (int) myStore.countColumns());
            myStore = new AboveBelowStore<N>(tmpUpperStore, myStore);
            return this;
        }

        @SafeVarargs
        public final Builder<N> above(final MatrixStore<N>... upperStore) {
            final MatrixStore<N> tmpUpperStore = Builder.buildRow((int) myStore.countColumns(), upperStore);
            myStore = new AboveBelowStore<N>(tmpUpperStore, myStore);
            return this;
        }

        @SafeVarargs
        public final Builder<N> above(final N... anUpperStore) {
            final MatrixStore<N> tmpUpperStore = Builder.buildRow(myStore.factory(), (int) myStore.countColumns(), anUpperStore);
            myStore = new AboveBelowStore<N>(tmpUpperStore, myStore);
            return this;
        }

        public final Builder<N> below(final int aRowDim) {
            final ZeroStore<N> tmpLowerStore = new ZeroStore<N>(myStore.factory(), aRowDim, (int) myStore.countColumns());
            myStore = new AboveBelowStore<N>(myStore, tmpLowerStore);
            return this;
        }

        @SafeVarargs
        public final Builder<N> below(final MatrixStore<N>... aLowerStore) {
            final MatrixStore<N> tmpLowerStore = Builder.buildRow((int) myStore.countColumns(), aLowerStore);
            myStore = new AboveBelowStore<N>(myStore, tmpLowerStore);
            return this;
        }

        @SafeVarargs
        public final Builder<N> below(final N... aLowerStore) {
            final MatrixStore<N> tmpLowerStore = Builder.buildRow(myStore.factory(), (int) myStore.countColumns(), aLowerStore);
            myStore = new AboveBelowStore<N>(myStore, tmpLowerStore);
            return this;
        }

        public final Builder<N> bidiagonal(final boolean upper, final boolean assumeOne) {
            if (upper) {
                myStore = new UpperTriangularStore<N>(new LowerHessenbergStore<N>(myStore), assumeOne);
            } else {
                myStore = new LowerTriangularStore<N>(new UpperHessenbergStore<N>(myStore), assumeOne);
            }
            return this;
        }

        public final MatrixStore<N> build() {
            return myStore;
        }

        public final Builder<N> column(final int... col) {
            myStore = new ColumnsStore<N>(myStore, col);
            return this;
        }

        public final Builder<N> columns(final int aFirst, final int aLimit) {
            myStore = new ColumnsStore<N>(aFirst, aLimit, myStore);
            return this;
        }

        public final Builder<N> conjugate() {
            if (myStore instanceof ConjugatedStore) {
                myStore = ((ConjugatedStore<N>) myStore).getOriginal();
            } else {
                myStore = new ConjugatedStore<N>(myStore);
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

        public final Builder<N> diagonal(final boolean assumeOne) {
            myStore = new UpperTriangularStore<N>(new LowerTriangularStore<N>(myStore, assumeOne), assumeOne);
            return this;
        }

        @SafeVarargs
        public final Builder<N> diagonally(final MatrixStore<N>... aDiagonalStore) {

            final PhysicalStore.Factory<N, ?> tmpFactory = myStore.factory();

            MatrixStore<N> tmpDiagonalStore;
            for (int ij = 0; ij < aDiagonalStore.length; ij++) {

                tmpDiagonalStore = aDiagonalStore[ij];

                final int tmpBaseRowDim = (int) myStore.countRows();
                final int tmpBaseColDim = (int) myStore.countColumns();

                final int tmpDiagRowDim = (int) tmpDiagonalStore.countRows();
                final int tmpDiagColDim = (int) tmpDiagonalStore.countColumns();

                final MatrixStore<N> tmpRightStore = new ZeroStore<N>(tmpFactory, tmpBaseRowDim, tmpDiagColDim);
                final MatrixStore<N> tmpAboveStore = new LeftRightStore<N>(myStore, tmpRightStore);

                final MatrixStore<N> tmpLeftStore = new ZeroStore<N>(tmpFactory, tmpDiagRowDim, tmpBaseColDim);
                final MatrixStore<N> tmpBelowStore = new LeftRightStore<N>(tmpLeftStore, tmpDiagonalStore);

                myStore = new AboveBelowStore<N>(tmpAboveStore, tmpBelowStore);
            }

            return this;
        }

        public PhysicalStore.Factory<N, ?> factory() {
            return myStore.factory();
        }

        public final MatrixStore<N> get() {
            return myStore;
        }

        public final Builder<N> hermitian(final boolean upper) {
            if (upper) {
                myStore = new UpperHermitianStore<N>(myStore);
            } else {
                myStore = new LowerHermitianStore<N>(myStore);
            }
            return this;
        }

        public final Builder<N> hessenberg(final boolean upper) {
            if (upper) {
                myStore = new UpperHessenbergStore<N>(myStore);
            } else {
                myStore = new LowerHessenbergStore<N>(myStore);
            }
            return this;
        }

        public final Builder<N> left(final int aColDim) {
            final MatrixStore<N> tmpLeftStore = new ZeroStore<N>(myStore.factory(), (int) myStore.countRows(), aColDim);
            myStore = new LeftRightStore<N>(tmpLeftStore, myStore);
            return this;
        }

        @SafeVarargs
        public final Builder<N> left(final MatrixStore<N>... aLeftStore) {
            final MatrixStore<N> tmpLeftStore = Builder.buildColumn((int) myStore.countRows(), aLeftStore);
            myStore = new LeftRightStore<N>(tmpLeftStore, myStore);
            return this;
        }

        @SafeVarargs
        public final Builder<N> left(final N... aLeftStore) {
            final MatrixStore<N> tmpLeftStore = Builder.buildColumn(myStore.factory(), (int) myStore.countRows(), aLeftStore);
            myStore = new LeftRightStore<N>(tmpLeftStore, myStore);
            return this;
        }

        public final Builder<N> right(final int aColDim) {
            final MatrixStore<N> tmpRightStore = new ZeroStore<N>(myStore.factory(), (int) myStore.countRows(), aColDim);
            myStore = new LeftRightStore<N>(myStore, tmpRightStore);
            return this;
        }

        @SafeVarargs
        public final Builder<N> right(final MatrixStore<N>... aRightStore) {
            final MatrixStore<N> tmpRightStore = Builder.buildColumn((int) myStore.countRows(), aRightStore);
            myStore = new LeftRightStore<N>(myStore, tmpRightStore);
            return this;
        }

        @SafeVarargs
        public final Builder<N> right(final N... aRightStore) {
            final MatrixStore<N> tmpRightStore = Builder.buildColumn(myStore.factory(), (int) myStore.countRows(), aRightStore);
            myStore = new LeftRightStore<N>(myStore, tmpRightStore);
            return this;
        }

        public final Builder<N> row(final int... row) {
            myStore = new RowsStore<N>(myStore, row);
            return this;
        }

        public final Builder<N> rows(final int aFirst, final int aLimit) {
            myStore = new RowsStore<N>(aFirst, aLimit, myStore);
            return this;
        }

        public final Builder<N> superimpose(final int row, final int col, final MatrixStore<N> aStore) {
            myStore = new SuperimposedStore<N>(myStore, row, col, aStore);
            return this;
        }

        public final Builder<N> superimpose(final int row, final int col, final Number aStore) {
            myStore = new SuperimposedStore<N>(myStore, row, col, new SingleStore<N>(myStore.factory(), aStore));
            return this;
        }

        public final Builder<N> superimpose(final MatrixStore<N> aStore) {
            myStore = new SuperimposedStore<N>(myStore, 0, 0, aStore);
            return this;
        }

        public final void supplyTo(final ElementsConsumer<N> consumer) {
            if (consumer.isAcceptable(this)) {
                consumer.accept(this.get());
            } else {
                throw new ProgrammingError("Not acceptable!");
            }
        }

        @Override
        public String toString() {
            return myStore.toString();
        }

        public final Builder<N> transpose() {
            if (myStore instanceof TransposedStore) {
                myStore = ((TransposedStore<N>) myStore).getOriginal();
            } else {
                myStore = new TransposedStore<N>(myStore);
            }
            return this;
        }

        public final Builder<N> triangular(final boolean upper, final boolean assumeOne) {
            if (upper) {
                myStore = new UpperTriangularStore<N>(myStore, assumeOne);
            } else {
                myStore = new LowerTriangularStore<N>(myStore, assumeOne);
            }
            return this;
        }

        public final Builder<N> tridiagonal() {
            myStore = new UpperHessenbergStore<N>(new LowerHessenbergStore<N>(myStore));
            return this;
        }

    }

    public static interface Factory<N extends Number> {

        MatrixStore.Builder<N> makeIdentity(int dimension);

        MatrixStore.Builder<N> makeSingle(N element);

        MatrixStore.Builder<N> makeWrapper(Access2D<?> access);

        MatrixStore.Builder<N> makeZero(int rowsCount, int columnsCount);

    }

    public static final Factory<BigDecimal> BIG = new Factory<BigDecimal>() {

        public Builder<BigDecimal> makeIdentity(final int dimension) {
            return new Builder<BigDecimal>(new IdentityStore<BigDecimal>(BigDenseStore.FACTORY, dimension));
        }

        public Builder<BigDecimal> makeSingle(final BigDecimal element) {
            return new Builder<BigDecimal>(new SingleStore<BigDecimal>(BigDenseStore.FACTORY, element));
        }

        public Builder<BigDecimal> makeWrapper(final Access2D<?> access) {
            return new Builder<BigDecimal>(new WrapperStore<BigDecimal>(BigDenseStore.FACTORY, access));
        }

        public Builder<BigDecimal> makeZero(final int rowsCount, final int columnsCount) {
            return new Builder<BigDecimal>(new ZeroStore<BigDecimal>(BigDenseStore.FACTORY, rowsCount, columnsCount));
        }

    };

    public static final Factory<ComplexNumber> COMPLEX = new Factory<ComplexNumber>() {

        public Builder<ComplexNumber> makeIdentity(final int dimension) {
            return new Builder<ComplexNumber>(new IdentityStore<ComplexNumber>(ComplexDenseStore.FACTORY, dimension));
        }

        public Builder<ComplexNumber> makeSingle(final ComplexNumber element) {
            return new Builder<ComplexNumber>(new SingleStore<ComplexNumber>(ComplexDenseStore.FACTORY, element));
        }

        public Builder<ComplexNumber> makeWrapper(final Access2D<?> access) {
            return new Builder<ComplexNumber>(new WrapperStore<ComplexNumber>(ComplexDenseStore.FACTORY, access));
        }

        public Builder<ComplexNumber> makeZero(final int rowsCount, final int columnsCount) {
            return new Builder<ComplexNumber>(new ZeroStore<ComplexNumber>(ComplexDenseStore.FACTORY, rowsCount, columnsCount));
        }

    };

    public static final Factory<Double> PRIMITIVE = new Factory<Double>() {

        public Builder<Double> makeIdentity(final int dimension) {
            return new Builder<Double>(new IdentityStore<Double>(PrimitiveDenseStore.FACTORY, dimension));
        }

        public Builder<Double> makeSingle(final Double element) {
            return new Builder<Double>(new SingleStore<Double>(PrimitiveDenseStore.FACTORY, element));
        }

        public Builder<Double> makeWrapper(final Access2D<?> access) {
            return new Builder<Double>(new WrapperStore<Double>(PrimitiveDenseStore.FACTORY, access));
        }

        public Builder<Double> makeZero(final int rowsCount, final int columnsCount) {
            return new Builder<Double>(new ZeroStore<Double>(PrimitiveDenseStore.FACTORY, rowsCount, columnsCount));
        }

    };

    default MatrixStore<N> add(final MatrixStore<N> addend) {
        return this.operateOnMatching(this.factory().function().add(), addend).get();
    }

    N aggregateAll(Aggregator aggregator);

    MatrixStore.Builder<N> builder();

    default MatrixStore<N> conjugate() {
        return new ConjugatedStore<>(this);
    }

    /**
     * Each call must produce a new instance.
     *
     * @return A new {@linkplain PhysicalStore} copy.
     */
    PhysicalStore<N> copy();

    boolean equals(MatrixStore<N> other, NumberContext context);

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

    /**
     * The entries below (left of) the first subdiagonal are zero - effectively an upper Hessenberg matrix.
     *
     * @see #isUpperRightShaded()
     * @deprecated v39
     */
    @Deprecated
    default boolean isLowerLeftShaded() {
        return false;
    }

    default boolean isSmall(final double comparedTo) {
        return PrimitiveScalar.isSmall(comparedTo, this.norm());
    }

    /**
     * The entries above (right of) the first superdiagonal are zero - effectively a lower Hessenberg matrix.
     *
     * @see #isLowerLeftShaded()
     * @deprecated v39
     */
    @Deprecated
    default boolean isUpperRightShaded() {
        return false;
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

    default MatrixStore<N> multiply(final Access1D<N> right) {

        final long tmpCountRows = this.countRows();
        final long tmpCountColumns = right.count() / this.countColumns();

        final PhysicalStore<N> retVal = this.factory().makeZero(tmpCountRows, tmpCountColumns);

        this.multiply(right, retVal);

        return retVal;
    }

    default PhysicalStore<N> multiply(final Access1D<N> right, final PhysicalStore<N> target) {
        target.fillByMultiplying(this, right);
        return target;
    }

    default MatrixStore<N> multiply(final double scalar) {
        return this.multiply(this.factory().scalar().cast(scalar));
    }

    default MatrixStore<N> multiply(final N scalar) {
        return this.operateOnAll(this.factory().function().multiply().second(scalar)).get();
    }

    /**
     * Assumes [leftAndRight] is a vecor and will calulate [leftAndRight]<sup>H</sup>[this][leftAndRight]
     *
     * @param leftAndRight The argument vector
     * @return A scalar (extracted from the resulting 1 x 1 matrix)
     */
    N multiplyBoth(final Access1D<N> leftAndRight);

    default ElementsSupplier<N> multiplyLeft(final Access1D<N> left) {
        return new MatrixProductSupplier<N>(left, this);
    }

    default MatrixStore<N> negate() {
        return this.operateOnAll(this.factory().function().negate()).get();
    }

    default double norm() {
        return this.aggregateAll(Aggregator.NORM2).doubleValue();
    }

    default ElementsSupplier<N> operateOnAll(final UnaryFunction<N> operator) {
        return new UnaryOperatorSupplier<>(operator, this);
    }

    default ElementsSupplier<N> operateOnMatching(final BinaryFunction<N> operator, final MatrixStore<N> right) {
        return new BinaryOperatorSupplier<>(this, operator, right);
    }

    default ElementsSupplier<N> operateOnMatching(final MatrixStore<N> left, final BinaryFunction<N> operator) {
        return new BinaryOperatorSupplier<>(left, operator, this);
    }

    /**
     * @deprecated v39 Use {@link #multiply(Number)} instead.
     */
    @Deprecated
    default MatrixStore<N> scale(final N scalar) {
        return this.multiply(scalar);
    }

    default MatrixStore<N> signum() {
        return this.multiply(PrimitiveMath.ONE / this.norm());
    }

    default Access1D<N> sliceColumn(final long row, final long column) {
        return new Access1D<N>() {

            public long count() {
                return MatrixStore.this.countRows() - row;
            }

            public double doubleValue(final long index) {
                return MatrixStore.this.doubleValue(row + index, column);
            }

            public N get(final long index) {
                return MatrixStore.this.get(row + index, column);
            }

        };
    }

    default Access1D<N> sliceDiagonal(final long row, final long column) {
        return new Access1D<N>() {

            public long count() {
                return Math.min(MatrixStore.this.countRows() - row, MatrixStore.this.countColumns() - column);
            }

            public double doubleValue(final long index) {
                return MatrixStore.this.doubleValue(row + index, column + index);
            }

            public N get(final long index) {
                return MatrixStore.this.get(row + index, column + index);
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

    default Access1D<N> sliceRow(final long row, final long column) {
        return new Access1D<N>() {

            public long count() {
                return MatrixStore.this.countColumns() - column;
            }

            public double doubleValue(final long index) {
                return MatrixStore.this.doubleValue(row, column + index);
            }

            public N get(final long index) {
                return MatrixStore.this.get(row, column + index);
            }

        };
    }

    default MatrixStore<N> subtract(final MatrixStore<N> subtrahend) {
        return this.operateOnMatching(this.factory().function().subtract(), subtrahend).get();
    }

    default Scalar<N> toScalar(final long row, final long column) {
        return this.factory().scalar().convert(this.get(row, column));
    }

    /**
     * @return A transposed matrix instance.
     */
    default MatrixStore<N> transpose() {
        return new TransposedStore<>(this);
    }

    default void visitOne(final long row, final long column, final VoidFunction<N> visitor) {
        visitor.invoke(this.get(row, column));
    }

}
