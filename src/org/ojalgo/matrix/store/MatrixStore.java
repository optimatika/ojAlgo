/*
 * Copyright 1997-2019 Optimatika
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
import org.ojalgo.algebra.NormedVectorSpace;
import org.ojalgo.algebra.Operation;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * A {@linkplain MatrixStore} is a two dimensional store of numbers/scalars.
 * </p>
 * <p>
 * A {@linkplain MatrixStore} extends {@linkplain Access2D} (as well as
 * {@linkplain org.ojalgo.structure.Access2D.Visitable} and
 * {@linkplain org.ojalgo.structure.Access2D.Elements}) and defines some futher funtionality - mainly matrix
 * multiplication.
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
public interface MatrixStore<N extends Number> extends ElementsSupplier<N>, Access2D<N>, Access2D.Visitable<N>, Access2D.Aggregatable<N>, Access2D.Sliceable<N>,
        Access2D.Elements, Structure2D.ReducibleTo1D<ElementsSupplier<N>>, NormedVectorSpace<MatrixStore<N>, N>, Operation.Multiplication<MatrixStore<N>> {

    public interface Factory<N extends Number> {

        MatrixStore.LogicalBuilder<N> makeIdentity(int dimension);

        MatrixStore.LogicalBuilder<N> makeSingle(N element);

        SparseStore<N> makeSparse(int rowsCount, int columnsCount);

        MatrixStore.LogicalBuilder<N> makeWrapper(Access2D<?> access);

        MatrixStore.LogicalBuilder<N> makeZero(int rowsCount, int columnsCount);

    }

    /**
     * A builder that lets you logically construct matrices and/or encode element structure.
     *
     * @author apete
     */
    public static class LogicalBuilder<N extends Number> implements ElementsSupplier<N>, Structure2D.Logical<MatrixStore<N>, MatrixStore.LogicalBuilder<N>> {

        @SafeVarargs
        static <N extends Number> MatrixStore<N> buildColumn(final long rowsCount, final MatrixStore<N>... columnStores) {
            MatrixStore<N> retVal = columnStores[0];
            for (int i = 1; i < columnStores.length; i++) {
                retVal = new AboveBelowStore<>(retVal, columnStores[i]);
            }
            long rowsSoFar = retVal.countRows();
            if (rowsSoFar < rowsCount) {
                retVal = new AboveBelowStore<>(retVal, new ZeroStore<>(retVal.physical(), rowsCount - rowsSoFar, retVal.countColumns()));
            }
            return retVal;
        }

        static <N extends Number> MatrixStore<N> buildColumn(final long rowsCount, final MatrixStore<N> columnStore) {
            MatrixStore<N> retVal = columnStore;
            long rowsSoFar = retVal.countRows();
            if (rowsSoFar < rowsCount) {
                retVal = new AboveBelowStore<>(retVal, new ZeroStore<>(retVal.physical(), rowsCount - rowsSoFar, retVal.countColumns()));
            }
            return retVal;
        }

        @SafeVarargs
        static <N extends Number> MatrixStore<N> buildColumn(final PhysicalStore.Factory<N, ?> factory, final long rowsCount, final N... columnElements) {
            MatrixStore<N> retVal = factory.columns(columnElements);
            long rowsSoFar = retVal.countRows();
            if (rowsSoFar < rowsCount) {
                retVal = new AboveBelowStore<>(retVal, new ZeroStore<>(factory, rowsCount - rowsSoFar, retVal.countColumns()));
            }
            return retVal;
        }

        @SafeVarargs
        static <N extends Number> MatrixStore<N> buildRow(final long colsCount, final MatrixStore<N>... rowStores) {
            MatrixStore<N> retVal = rowStores[0];
            for (int j = 1; j < rowStores.length; j++) {
                retVal = new LeftRightStore<>(retVal, rowStores[j]);
            }
            long colsSoFar = retVal.countColumns();
            if (colsSoFar < colsCount) {
                retVal = new LeftRightStore<>(retVal, new ZeroStore<>(retVal.physical(), retVal.countRows(), colsCount - colsSoFar));
            }
            return retVal;
        }

        static <N extends Number> MatrixStore<N> buildRow(final long colsCount, final MatrixStore<N> rowStore) {
            MatrixStore<N> retVal = rowStore;
            long colsSoFar = retVal.countColumns();
            if (colsSoFar < colsCount) {
                retVal = new LeftRightStore<>(retVal, new ZeroStore<>(retVal.physical(), retVal.countRows(), colsCount - colsSoFar));
            }
            return retVal;
        }

        @SafeVarargs
        static <N extends Number> MatrixStore<N> buildRow(final PhysicalStore.Factory<N, ?> factory, final long colsCount, final N... rowElements) {
            MatrixStore<N> retVal = new TransposedStore<>(factory.columns(rowElements));
            long colsSoFar = retVal.countColumns();
            if (colsSoFar < colsCount) {
                retVal = new LeftRightStore<>(retVal, new ZeroStore<>(factory, retVal.countRows(), colsCount - colsSoFar));
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

        public LogicalBuilder<N> above(final int numberOfRows) {
            ZeroStore<N> above = new ZeroStore<>(myStore.physical(), numberOfRows, myStore.countColumns());
            myStore = new AboveBelowStore<>(above, myStore);
            return this;
        }

        @SuppressWarnings("unchecked")
        public LogicalBuilder<N> above(final MatrixStore<N>... matrices) {
            MatrixStore<N> above = LogicalBuilder.buildRow(myStore.countColumns(), matrices);
            myStore = new AboveBelowStore<>(above, myStore);
            return this;
        }

        public LogicalBuilder<N> above(final MatrixStore<N> matrix) {
            MatrixStore<N> above = LogicalBuilder.buildRow(myStore.countColumns(), matrix);
            myStore = new AboveBelowStore<>(above, myStore);
            return this;
        }

        @SuppressWarnings("unchecked")
        public LogicalBuilder<N> above(final MatrixStore<N> above1, final MatrixStore<N> above2) {
            return this.above((MatrixStore<N>[]) new MatrixStore<?>[] { above1, above2 });
        }

        @SuppressWarnings("unchecked")
        public LogicalBuilder<N> above(final N... elements) {
            MatrixStore<N> above = LogicalBuilder.buildRow(myStore.physical(), myStore.countColumns(), elements);
            myStore = new AboveBelowStore<>(above, myStore);
            return this;
        }

        public LogicalBuilder<N> below(final int numberOfRows) {
            ZeroStore<N> below = new ZeroStore<>(myStore.physical(), numberOfRows, (int) myStore.countColumns());
            myStore = new AboveBelowStore<>(myStore, below);
            return this;
        }

        @SuppressWarnings("unchecked")
        public LogicalBuilder<N> below(final MatrixStore<N>... matrices) {
            MatrixStore<N> below = LogicalBuilder.buildRow(myStore.countColumns(), matrices);
            myStore = new AboveBelowStore<>(myStore, below);
            return this;
        }

        public LogicalBuilder<N> below(final MatrixStore<N> matrix) {
            MatrixStore<N> below = LogicalBuilder.buildRow(myStore.countColumns(), matrix);
            myStore = new AboveBelowStore<>(myStore, below);
            return this;
        }

        @SuppressWarnings("unchecked")
        public LogicalBuilder<N> below(final MatrixStore<N> below1, final MatrixStore<N> below2) {
            return this.below((MatrixStore<N>[]) new MatrixStore<?>[] { below1, below2 });
        }

        @SuppressWarnings("unchecked")
        public LogicalBuilder<N> below(final N... elements) {
            MatrixStore<N> below = LogicalBuilder.buildRow(myStore.physical(), (int) myStore.countColumns(), elements);
            myStore = new AboveBelowStore<>(myStore, below);
            return this;
        }

        public LogicalBuilder<N> bidiagonal(final boolean upper, final boolean assumeOne) {
            if (upper) {
                myStore = new UpperTriangularStore<>(new LowerHessenbergStore<>(myStore), assumeOne);
            } else {
                myStore = new LowerTriangularStore<>(new UpperHessenbergStore<>(myStore), assumeOne);
            }
            return this;
        }

        /**
         * @see #columns(int[])
         */
        public LogicalBuilder<N> column(final int... columns) {
            return this.columns(columns);
        }

        /**
         * A selection (re-ordering) of columns. Note that it's ok to reference the same base column more than
         * once, and any negative column reference/index will translate to a column of zeros. The number of
         * columns in the resulting matrix is the same as the number of elements in the columns index array.
         */
        public LogicalBuilder<N> columns(final int[] columns) {
            myStore = new ColumnsStore<>(myStore, columns);
            return this;
        }

        public LogicalBuilder<N> conjugate() {
            if (myStore instanceof ConjugatedStore) {
                myStore = ((ConjugatedStore<N>) myStore).getOriginal();
            } else {
                myStore = new ConjugatedStore<>(myStore);
            }
            return this;
        }

        public PhysicalStore<N> copy() {
            return myStore.copy();
        }

        public long count() {
            return myStore.count();
        }

        public long countColumns() {
            return myStore.countColumns();
        }

        public long countRows() {
            return myStore.countRows();
        }

        /**
         * @return A square diagonal matrix (main diagonal only)
         */
        public LogicalBuilder<N> diagonal() {
            myStore = new DiagonalStore<>(myStore);
            return this;
        }

        /**
         * @param maintain Maintain the original matrix dimensions (resulting matrix not necessarily square).
         * @return A diagonal matrix (main diagonal only)
         */
        public LogicalBuilder<N> diagonal(final boolean maintain) {
            myStore = new DiagonalStore<>(myStore);
            return this;
        }

        @SuppressWarnings("unchecked")
        public LogicalBuilder<N> diagonally(final MatrixStore<N>... diagonally) {

            PhysicalStore.Factory<N, ?> tmpFactory = myStore.physical();

            MatrixStore<N> tmpDiagonalStore;
            for (int ij = 0; ij < diagonally.length; ij++) {

                tmpDiagonalStore = diagonally[ij];

                int tmpBaseRowDim = (int) myStore.countRows();
                int tmpBaseColDim = (int) myStore.countColumns();

                int tmpDiagRowDim = (int) tmpDiagonalStore.countRows();
                int tmpDiagColDim = (int) tmpDiagonalStore.countColumns();

                MatrixStore<N> tmpRightStore = new ZeroStore<>(tmpFactory, tmpBaseRowDim, tmpDiagColDim);
                MatrixStore<N> tmpAboveStore = new LeftRightStore<>(myStore, tmpRightStore);

                MatrixStore<N> tmpLeftStore = new ZeroStore<>(tmpFactory, tmpDiagRowDim, tmpBaseColDim);
                MatrixStore<N> tmpBelowStore = new LeftRightStore<>(tmpLeftStore, tmpDiagonalStore);

                myStore = new AboveBelowStore<>(tmpAboveStore, tmpBelowStore);
            }

            return this;
        }

        public MatrixStore<N> get() {
            return myStore;
        }

        public LogicalBuilder<N> hermitian(final boolean upper) {
            if (upper) {
                myStore = new UpperHermitianStore<>(myStore);
            } else {
                myStore = new LowerHermitianStore<>(myStore);
            }
            return this;
        }

        public LogicalBuilder<N> hessenberg(final boolean upper) {
            if (upper) {
                myStore = new UpperHessenbergStore<>(myStore);
            } else {
                myStore = new LowerHessenbergStore<>(myStore);
            }
            return this;
        }

        public LogicalBuilder<N> left(final int numberOfColumns) {
            MatrixStore<N> left = new ZeroStore<>(myStore.physical(), myStore.countRows(), numberOfColumns);
            myStore = new LeftRightStore<>(left, myStore);
            return this;
        }

        @SuppressWarnings("unchecked")
        public LogicalBuilder<N> left(final MatrixStore<N>... matrices) {
            MatrixStore<N> left = LogicalBuilder.buildColumn(myStore.countRows(), matrices);
            myStore = new LeftRightStore<>(left, myStore);
            return this;
        }

        public LogicalBuilder<N> left(final MatrixStore<N> matrix) {
            MatrixStore<N> left = LogicalBuilder.buildColumn(myStore.countRows(), matrix);
            myStore = new LeftRightStore<>(left, myStore);
            return this;
        }

        @SuppressWarnings("unchecked")
        public LogicalBuilder<N> left(final MatrixStore<N> left1, final MatrixStore<N> left2) {
            return this.left((MatrixStore<N>[]) new MatrixStore<?>[] { left1, left2 });
        }

        @SuppressWarnings("unchecked")
        public LogicalBuilder<N> left(final N... elements) {
            MatrixStore<N> left = LogicalBuilder.buildColumn(myStore.physical(), myStore.countRows(), elements);
            myStore = new LeftRightStore<>(left, myStore);
            return this;
        }

        /**
         * Setting either limit to &lt; 0 is interpreted as "no limit" (useful when you only want to limit
         * either the rows or columns, and don't know the size of the other)
         */
        public LogicalBuilder<N> limits(final int rowLimit, final int columnLimit) {
            myStore = new LimitStore<>(rowLimit < 0 ? (int) myStore.countRows() : rowLimit, columnLimit < 0 ? (int) myStore.countColumns() : columnLimit,
                    myStore);
            return this;
        }

        public LogicalBuilder<N> offsets(final int rowOffset, final int columnOffset) {
            myStore = new OffsetStore<>(myStore, rowOffset < 0 ? 0 : rowOffset, columnOffset < 0 ? 0 : columnOffset);
            return this;
        }

        public PhysicalStore.Factory<N, ?> physical() {
            return myStore.physical();
        }

        public LogicalBuilder<N> right(final int numberOfColumns) {
            MatrixStore<N> right = new ZeroStore<>(myStore.physical(), myStore.countRows(), numberOfColumns);
            myStore = new LeftRightStore<>(myStore, right);
            return this;
        }

        @SuppressWarnings("unchecked")
        public LogicalBuilder<N> right(final MatrixStore<N>... matrices) {
            MatrixStore<N> right = LogicalBuilder.buildColumn(myStore.countRows(), matrices);
            myStore = new LeftRightStore<>(myStore, right);
            return this;
        }

        public LogicalBuilder<N> right(final MatrixStore<N> matrix) {
            MatrixStore<N> right = LogicalBuilder.buildColumn(myStore.countRows(), matrix);
            myStore = new LeftRightStore<>(myStore, right);
            return this;
        }

        @SuppressWarnings("unchecked")
        public LogicalBuilder<N> right(final MatrixStore<N> right1, final MatrixStore<N> right2) {
            return this.right((MatrixStore<N>[]) new MatrixStore<?>[] { right1, right2 });
        }

        @SuppressWarnings("unchecked")
        public LogicalBuilder<N> right(final N... elements) {
            MatrixStore<N> right = LogicalBuilder.buildColumn(myStore.physical(), myStore.countRows(), elements);
            myStore = new LeftRightStore<>(myStore, right);
            return this;
        }

        /**
         * @see #rows(int[])
         */
        public LogicalBuilder<N> row(final int... rows) {
            return this.rows(rows);
        }

        /**
         * A selection (re-ordering) of rows. Note that it's ok to reference the same base row more than once,
         * and any negative row reference/index will translate to a row of zeros. The number of rows in the
         * resulting matrix is the same as the number of elements in the rows index array.
         */
        public LogicalBuilder<N> rows(final int[] rows) {
            myStore = new RowsStore<>(myStore, rows);
            return this;
        }

        public LogicalBuilder<N> superimpose(final int row, final int col, final MatrixStore<N> matrix) {
            myStore = new SuperimposedStore<>(myStore, row, col, matrix);
            return this;
        }

        public LogicalBuilder<N> superimpose(final int row, final int col, final Number matrix) {
            myStore = new SuperimposedStore<>(myStore, row, col, new SingleStore<>(myStore.physical(), matrix));
            return this;
        }

        public LogicalBuilder<N> superimpose(final MatrixStore<N> matrix) {
            myStore = new SuperimposedStore<>(myStore, 0, 0, matrix);
            return this;
        }

        public void supplyTo(final TransformableRegion<N> receiver) {
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

        public LogicalBuilder<N> transpose() {
            if (myStore instanceof TransposedStore) {
                myStore = ((TransposedStore<N>) myStore).getOriginal();
            } else {
                myStore = new TransposedStore<>(myStore);
            }
            return this;
        }

        public LogicalBuilder<N> triangular(final boolean upper, final boolean assumeOne) {
            if (upper) {
                myStore = new UpperTriangularStore<>(myStore, assumeOne);
            } else {
                myStore = new LowerTriangularStore<>(myStore, assumeOne);
            }
            return this;
        }

        public LogicalBuilder<N> tridiagonal() {
            myStore = new UpperHessenbergStore<>(new LowerHessenbergStore<>(myStore));
            return this;
        }

    }

    Factory<ComplexNumber> COMPLEX = new Factory<ComplexNumber>() {

        public LogicalBuilder<ComplexNumber> makeIdentity(final int dimension) {
            return new LogicalBuilder<>(new IdentityStore<>(GenericDenseStore.COMPLEX, dimension));
        }

        public LogicalBuilder<ComplexNumber> makeSingle(final ComplexNumber element) {
            return new LogicalBuilder<>(new SingleStore<>(GenericDenseStore.COMPLEX, element));
        }

        public SparseStore<ComplexNumber> makeSparse(final int rowsCount, final int columnsCount) {
            return SparseStore.COMPLEX.make(rowsCount, columnsCount);
        }

        public LogicalBuilder<ComplexNumber> makeWrapper(final Access2D<?> access) {
            return new LogicalBuilder<>(new WrapperStore<>(GenericDenseStore.COMPLEX, access));
        }

        public LogicalBuilder<ComplexNumber> makeZero(final int rowsCount, final int columnsCount) {
            return new LogicalBuilder<>(new ZeroStore<>(GenericDenseStore.COMPLEX, rowsCount, columnsCount));
        }

    };

    Factory<Double> PRIMITIVE = new Factory<Double>() {

        public LogicalBuilder<Double> makeIdentity(final int dimension) {
            return new LogicalBuilder<>(new IdentityStore<>(PrimitiveDenseStore.FACTORY, dimension));
        }

        public LogicalBuilder<Double> makeSingle(final Double element) {
            return new LogicalBuilder<>(new SingleStore<>(PrimitiveDenseStore.FACTORY, element));
        }

        public SparseStore<Double> makeSparse(final int rowsCount, final int columnsCount) {
            return SparseStore.PRIMITIVE.make(rowsCount, columnsCount);
        }

        public LogicalBuilder<Double> makeWrapper(final Access2D<?> access) {
            return new LogicalBuilder<>(new WrapperStore<>(PrimitiveDenseStore.FACTORY, access));
        }

        public LogicalBuilder<Double> makeZero(final int rowsCount, final int columnsCount) {
            return new LogicalBuilder<>(new ZeroStore<>(PrimitiveDenseStore.FACTORY, rowsCount, columnsCount));
        }

    };

    Factory<Quaternion> QUATERNION = new Factory<Quaternion>() {

        public LogicalBuilder<Quaternion> makeIdentity(final int dimension) {
            return new LogicalBuilder<>(new IdentityStore<>(GenericDenseStore.QUATERNION, dimension));
        }

        public LogicalBuilder<Quaternion> makeSingle(final Quaternion element) {
            return new LogicalBuilder<>(new SingleStore<>(GenericDenseStore.QUATERNION, element));
        }

        public SparseStore<Quaternion> makeSparse(final int rowsCount, final int columnsCount) {
            return SparseStore.QUATERNION.make(rowsCount, columnsCount);
        }

        public LogicalBuilder<Quaternion> makeWrapper(final Access2D<?> access) {
            return new LogicalBuilder<>(new WrapperStore<>(GenericDenseStore.QUATERNION, access));
        }

        public LogicalBuilder<Quaternion> makeZero(final int rowsCount, final int columnsCount) {
            return new LogicalBuilder<>(new ZeroStore<>(GenericDenseStore.QUATERNION, rowsCount, columnsCount));
        }

    };

    Factory<RationalNumber> RATIONAL = new Factory<RationalNumber>() {

        public LogicalBuilder<RationalNumber> makeIdentity(final int dimension) {
            return new LogicalBuilder<>(new IdentityStore<>(GenericDenseStore.RATIONAL, dimension));
        }

        public LogicalBuilder<RationalNumber> makeSingle(final RationalNumber element) {
            return new LogicalBuilder<>(new SingleStore<>(GenericDenseStore.RATIONAL, element));
        }

        public SparseStore<RationalNumber> makeSparse(final int rowsCount, final int columnsCount) {
            return SparseStore.RATIONAL.make(rowsCount, columnsCount);
        }

        public LogicalBuilder<RationalNumber> makeWrapper(final Access2D<?> access) {
            return new LogicalBuilder<>(new WrapperStore<>(GenericDenseStore.RATIONAL, access));
        }

        public LogicalBuilder<RationalNumber> makeZero(final int rowsCount, final int columnsCount) {
            return new LogicalBuilder<>(new ZeroStore<>(GenericDenseStore.RATIONAL, rowsCount, columnsCount));
        }

    };

    static int firstInColumn(final Access1D<?> matrix, final int col, final int defaultAndMinimum) {
        return matrix instanceof MatrixStore<?> ? Math.max(((MatrixStore<?>) matrix).firstInColumn(col), defaultAndMinimum) : defaultAndMinimum;
    }

    static long firstInColumn(final Access1D<?> matrix, final long col, final long defaultAndMinimum) {
        return matrix instanceof MatrixStore<?> ? Math.max(((MatrixStore<?>) matrix).firstInColumn((int) col), defaultAndMinimum) : defaultAndMinimum;
    }

    static int firstInRow(final Access1D<?> matrix, final int row, final int defaultAndMinimum) {
        return matrix instanceof MatrixStore<?> ? Math.max(((MatrixStore<?>) matrix).firstInRow(row), defaultAndMinimum) : defaultAndMinimum;
    }

    static long firstInRow(final Access1D<?> matrix, final long row, final long defaultAndMinimum) {
        return matrix instanceof MatrixStore<?> ? Math.max(((MatrixStore<?>) matrix).firstInRow((int) row), defaultAndMinimum) : defaultAndMinimum;
    }

    static int limitOfColumn(final Access1D<?> matrix, final int col, final int defaultAndMaximum) {
        return matrix instanceof MatrixStore<?> ? Math.min(((MatrixStore<?>) matrix).limitOfColumn(col), defaultAndMaximum) : defaultAndMaximum;
    }

    static long limitOfColumn(final Access1D<?> matrix, final long col, final long defaultAndMaximum) {
        return matrix instanceof MatrixStore<?> ? Math.min(((MatrixStore<?>) matrix).limitOfColumn((int) col), defaultAndMaximum) : defaultAndMaximum;
    }

    static int limitOfRow(final Access1D<?> matrix, final int row, final int defaultAndMaximum) {
        return matrix instanceof MatrixStore<?> ? Math.min(((MatrixStore<?>) matrix).limitOfRow(row), defaultAndMaximum) : defaultAndMaximum;
    }

    static long limitOfRow(final Access1D<?> matrix, final long row, final long defaultAndMaximum) {
        return matrix instanceof MatrixStore<?> ? Math.min(((MatrixStore<?>) matrix).limitOfRow((int) row), defaultAndMaximum) : defaultAndMaximum;
    }

    default MatrixStore<N> add(final MatrixStore<N> addend) {
        return this.operateOnMatching(this.physical().function().add(), addend).get();
    }

    default N aggregateAll(final Aggregator aggregator) {

        AggregatorFunction<N> tmpVisitor = this.physical().aggregator().get(aggregator);

        this.visitAll(tmpVisitor);

        return tmpVisitor.get();
    }

    default N aggregateColumn(final long row, final long col, final Aggregator aggregator) {

        AggregatorFunction<N> tmpVisitor = this.physical().aggregator().get(aggregator);

        this.visitColumn(row, col, tmpVisitor);

        return tmpVisitor.get();
    }

    default N aggregateDiagonal(final long row, final long col, final Aggregator aggregator) {

        AggregatorFunction<N> tmpVisitor = this.physical().aggregator().get(aggregator);

        this.visitDiagonal(row, col, tmpVisitor);

        return tmpVisitor.get();
    }

    default N aggregateRange(final long first, final long limit, final Aggregator aggregator) {

        AggregatorFunction<N> tmpVisitor = this.physical().aggregator().get(aggregator);

        this.visitRange(first, limit, tmpVisitor);

        return tmpVisitor.get();
    }

    default N aggregateRow(final long row, final long col, final Aggregator aggregator) {

        AggregatorFunction<N> tmpVisitor = this.physical().aggregator().get(aggregator);

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

        PhysicalStore<N> retVal = this.physical().makeZero(this);

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

    default boolean isHermitian() {

        int numberOfRows = Math.toIntExact(this.countRows());
        int numberOfColumns = Math.toIntExact(this.countColumns());

        Number element = this.get(0L);

        boolean retVal = numberOfRows == numberOfColumns;

        if (element instanceof ComplexNumber) {

            ComplexNumber lowerLeft;
            ComplexNumber upperRight;

            for (int j = 0; retVal && (j < numberOfColumns); j++) {
                retVal &= PrimitiveScalar.isSmall(PrimitiveMath.ONE, ComplexNumber.valueOf(this.get(j, j)).i);
                for (int i = j + 1; retVal && (i < numberOfRows); i++) {
                    lowerLeft = ComplexNumber.valueOf(this.get(i, j)).conjugate();
                    upperRight = ComplexNumber.valueOf(this.get(j, i));
                    retVal &= PrimitiveScalar.isSmall(PrimitiveMath.ONE, lowerLeft.subtract(upperRight).norm());
                }
            }

        } else {

            for (int j = 0; retVal && (j < numberOfColumns); j++) {
                for (int i = j + 1; retVal && (i < numberOfRows); i++) {
                    retVal &= PrimitiveScalar.isSmall(PrimitiveMath.ONE, this.doubleValue(i, j) - this.doubleValue(j, i));
                }
            }
        }

        return retVal;
    }

    default boolean isNormal() {
        MatrixStore<N> conjugate = this.conjugate();
        return conjugate.multiply(this).equals(this.multiply(conjugate));
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
     * @return The column index of the first zero element, after all non-zeros, in the specified row (index of
     *         the last non-zero + 1)
     */
    default int limitOfRow(final int row) {
        return (int) this.countColumns();
    }

    default MatrixStore.LogicalBuilder<N> logical() {
        return new MatrixStore.LogicalBuilder<>(this);
    }

    default void multiply(final Access1D<N> right, final TransformableRegion<N> target) {
        target.fillByMultiplying(this, right);
    }

    default MatrixStore<N> multiply(final double scalar) {
        return this.multiply(this.physical().scalar().cast(scalar));
    }

    default MatrixStore<N> multiply(final MatrixStore<N> right) {

        long tmpCountRows = this.countRows();
        long tmpCountColumns = right.count() / this.countColumns();

        PhysicalStore<N> retVal = this.physical().makeZero(tmpCountRows, tmpCountColumns);

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

        PhysicalStore<N> tmpStep1 = this.physical().makeZero(1L, leftAndRight.count());
        PhysicalStore<N> tmpStep2 = this.physical().makeZero(1L, 1L);

        PhysicalStore<N> tmpLeft = this.physical().rows(leftAndRight);
        tmpLeft.modifyAll(this.physical().function().conjugate());
        tmpStep1.fillByMultiplying(tmpLeft.conjugate(), this);

        tmpStep2.fillByMultiplying(tmpStep1, leftAndRight);

        return tmpStep2.get(0L);
    }

    default MatrixStore<N> negate() {
        return this.operateOnAll(this.physical().function().negate()).get();
    }

    default double norm() {

        double frobeniusNorm = this.aggregateAll(Aggregator.NORM2).doubleValue();

        if (this.isVector()) {
            return frobeniusNorm;
        } else {
            // Bringing it closer to what the operator norm would be
            // In case of representing a ComplexNumber or Quaternion as a matrix this will match their norms
            return frobeniusNorm / PrimitiveMath.SQRT.invoke(Math.min(this.countRows(), this.countColumns()));
        }
    }

    default MatrixStore<N> operateOnAll(final UnaryFunction<N> operator) {
        return new UnaryOperatoStore<>(this, operator);
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

    default ElementsSupplier<N> reduceColumns(final Aggregator aggregator) {
        return new MatrixPipeline.ColumnsReducer<>(this, aggregator);
    }

    default ElementsSupplier<N> reduceRows(final Aggregator aggregator) {
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

            @Override
            public String toString() {
                return Access1D.toString(this);
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

            @Override
            public String toString() {
                return Access1D.toString(this);
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

            @Override
            public String toString() {
                return Access1D.toString(this);
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

            @Override
            public String toString() {
                return Access1D.toString(this);
            }

        };
    }

    default MatrixStore<N> subtract(final MatrixStore<N> subtrahend) {
        return this.operateOnMatching(this.physical().function().subtract(), subtrahend).get();
    }

    default void supplyTo(final TransformableRegion<N> receiver) {
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
