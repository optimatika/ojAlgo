/*
 * Copyright 1997-2020 Optimatika
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
import org.ojalgo.matrix.store.DiagonalStore.Builder;
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
public interface MatrixStore<N extends Comparable<N>>
        extends ElementsSupplier<N>, Access2D<N>, Access2D.Visitable<N>, Access2D.Aggregatable<N>, Access2D.Sliceable<N>, Access2D.Elements,
        Structure2D.ReducibleTo1D<ElementsSupplier<N>>, NormedVectorSpace<MatrixStore<N>, N>, Operation.Multiplication<MatrixStore<N>> {

    public interface Factory<N extends Comparable<N>> {

        <D extends Access1D<?>> DiagonalStore.Builder<N, D> makeDiagonal(D mainDiagonal);

        MatrixStore.LogicalBuilder<N> makeIdentity(int dimension);

        default MatrixStore.LogicalBuilder<N> makeIdentity(final long dimension) {
            return this.makeIdentity(Math.toIntExact(dimension));
        }

        MatrixStore.LogicalBuilder<N> makeSingle(N element);

        SparseStore<N> makeSparse(int rowsCount, int columnsCount);

        default SparseStore<N> makeSparse(final long rowsCount, final long columnsCount) {
            return this.makeSparse(Math.toIntExact(rowsCount), Math.toIntExact(columnsCount));
        }

        MatrixStore.LogicalBuilder<N> makeWrapper(Access2D<?> access);

        MatrixStore.LogicalBuilder<N> makeZero(int rowsCount, int columnsCount);

        default MatrixStore.LogicalBuilder<N> makeZero(final long rowsCount, final long columnsCount) {
            return this.makeZero(Math.toIntExact(rowsCount), Math.toIntExact(columnsCount));
        }

    }

    /**
     * A builder that lets you logically construct matrices and/or encode element structure.
     *
     * @author apete
     */
    public static class LogicalBuilder<N extends Comparable<N>>
            implements ElementsSupplier<N>, Structure2D.Logical<MatrixStore<N>, MatrixStore.LogicalBuilder<N>> {

        @SafeVarargs
        static <N extends Comparable<N>> MatrixStore<N> buildColumn(final long rowsCount, final MatrixStore<N>... columnStores) {
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

        static <N extends Comparable<N>> MatrixStore<N> buildColumn(final long rowsCount, final MatrixStore<N> columnStore) {
            MatrixStore<N> retVal = columnStore;
            long rowsSoFar = retVal.countRows();
            if (rowsSoFar < rowsCount) {
                retVal = new AboveBelowStore<>(retVal, new ZeroStore<>(retVal.physical(), rowsCount - rowsSoFar, retVal.countColumns()));
            }
            return retVal;
        }

        @SafeVarargs
        static <N extends Comparable<N>> MatrixStore<N> buildColumn(final PhysicalStore.Factory<N, ?> factory, final long rowsCount,
                final N... columnElements) {
            MatrixStore<N> retVal = factory.columns(columnElements);
            long rowsSoFar = retVal.countRows();
            if (rowsSoFar < rowsCount) {
                retVal = new AboveBelowStore<>(retVal, new ZeroStore<>(factory, rowsCount - rowsSoFar, retVal.countColumns()));
            }
            return retVal;
        }

        @SafeVarargs
        static <N extends Comparable<N>> MatrixStore<N> buildRow(final long colsCount, final MatrixStore<N>... rowStores) {
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

        static <N extends Comparable<N>> MatrixStore<N> buildRow(final long colsCount, final MatrixStore<N> rowStore) {
            MatrixStore<N> retVal = rowStore;
            long colsSoFar = retVal.countColumns();
            if (colsSoFar < colsCount) {
                retVal = new LeftRightStore<>(retVal, new ZeroStore<>(retVal.physical(), retVal.countRows(), colsCount - colsSoFar));
            }
            return retVal;
        }

        @SafeVarargs
        static <N extends Comparable<N>> MatrixStore<N> buildRow(final PhysicalStore.Factory<N, ?> factory, final long colsCount, final N... rowElements) {
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

        public LogicalBuilder<N> above(final long numberOfRows) {
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

        /**
         * @deprecated v48
         */
        @Deprecated
        @SuppressWarnings("unchecked")
        public LogicalBuilder<N> above(final N... elements) {
            MatrixStore<N> above = LogicalBuilder.buildRow(myStore.physical(), myStore.countColumns(), elements);
            myStore = new AboveBelowStore<>(above, myStore);
            return this;
        }

        public LogicalBuilder<N> below(final long numberOfRows) {
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

        /**
         * @deprecated v48
         */
        @Deprecated
        @SuppressWarnings("unchecked")
        public LogicalBuilder<N> below(final N... elements) {
            MatrixStore<N> below = LogicalBuilder.buildRow(myStore.physical(), (int) myStore.countColumns(), elements);
            myStore = new AboveBelowStore<>(myStore, below);
            return this;
        }

        public LogicalBuilder<N> bidiagonal(final boolean upper) {
            PhysicalStore.Factory<N, ?> factory = myStore.physical();
            Access1D<N> mainDiagonal = myStore.sliceDiagonal();
            Access1D<N> superdiagonal = null;
            Access1D<N> subdiagonal = null;
            if (upper) {
                superdiagonal = myStore.sliceDiagonal(0, 1);
            } else {
                subdiagonal = myStore.sliceDiagonal(1, 0);
            }
            long numbRows = myStore.countRows();
            long numbCols = myStore.countColumns();
            myStore = new DiagonalStore<>(factory, numbRows, numbCols, mainDiagonal, superdiagonal, subdiagonal);
            return this;
        }

        /**
         * @deprecated v48 Use {@link #bidiagonal(boolean)} instead
         */
        @Deprecated
        public LogicalBuilder<N> bidiagonal(final boolean upper, final boolean assumeOne) {
            if (upper) {
                myStore = new UpperTriangularStore<>(new LowerHessenbergStore<>(myStore), assumeOne);
            } else {
                myStore = new LowerTriangularStore<>(new UpperHessenbergStore<>(myStore), assumeOne);
            }
            return this;
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

        /**
         * @deprecated v48 Use {@link MatrixStore#conjugate()} instead
         */
        @Deprecated
        public LogicalBuilder<N> conjugate() {
            if (myStore instanceof ConjugatedStore) {
                myStore = ((ConjugatedStore<N>) myStore).getOriginal();
            } else {
                myStore = new ConjugatedStore<>(myStore);
            }
            return this;
        }

        /**
         * @deprecated v48
         */
        @Deprecated
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

        public LogicalBuilder<N> diagonal() {
            PhysicalStore.Factory<N, ?> factory = myStore.physical();
            Access1D<N> mainDiagonal = myStore.sliceDiagonal();
            long numbRows = myStore.countRows();
            long numbCols = myStore.countColumns();
            myStore = new DiagonalStore<>(factory, numbRows, numbCols, mainDiagonal, null, null);
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

        /**
         * @deprecated v48
         */
        @Deprecated
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

        public LogicalBuilder<N> left(final long numberOfColumns) {
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

        /**
         * @deprecated v48
         */
        @Deprecated
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
        public LogicalBuilder<N> limits(final long rowLimit, final long columnLimit) {
            myStore = new LimitStore<>(rowLimit < 0 ? (int) myStore.countRows() : rowLimit, columnLimit < 0 ? (int) myStore.countColumns() : columnLimit,
                    myStore);
            return this;
        }

        public LogicalBuilder<N> offsets(final long rowOffset, final long columnOffset) {
            myStore = new OffsetStore<>(myStore, rowOffset < 0 ? 0 : rowOffset, columnOffset < 0 ? 0 : columnOffset);
            return this;
        }

        public PhysicalStore.Factory<N, ?> physical() {
            return myStore.physical();
        }

        public LogicalBuilder<N> right(final long numberOfColumns) {
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

        /**
         * @deprecated v48
         */
        @Deprecated
        @SuppressWarnings("unchecked")
        public LogicalBuilder<N> right(final N... elements) {
            MatrixStore<N> right = LogicalBuilder.buildColumn(myStore.physical(), myStore.countRows(), elements);
            myStore = new LeftRightStore<>(myStore, right);
            return this;
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

        /**
         * @deprecated v48
         */
        @Deprecated
        public LogicalBuilder<N> superimpose(final int row, final int col, final MatrixStore<N> matrix) {
            myStore = new SuperimposedStore<>(myStore, row, col, matrix);
            return this;
        }

        /**
         * @deprecated v48
         */
        @Deprecated
        public LogicalBuilder<N> superimpose(final int row, final int col, final N matrix) {
            myStore = new SuperimposedStore<>(myStore, row, col, new SingleStore<>(myStore.physical(), matrix));
            return this;
        }

        /**
         * @deprecated v48
         */
        @Deprecated
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

        /**
         * @deprecated v48 Use {@link MatrixStore#transpose()} instead
         */
        @Deprecated
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
            PhysicalStore.Factory<N, ?> factory = myStore.physical();
            Access1D<N> mainDiagonal = myStore.sliceDiagonal();
            Access1D<N> superdiagonal = myStore.sliceDiagonal(0, 1);
            Access1D<N> subdiagonal = myStore.sliceDiagonal(1, 0);
            long numbRows = myStore.countRows();
            long numbCols = myStore.countColumns();
            myStore = new DiagonalStore<>(factory, numbRows, numbCols, mainDiagonal, superdiagonal, subdiagonal);
            return this;
        }

    }

    Factory<ComplexNumber> COMPLEX = new Factory<ComplexNumber>() {

        public <D extends Access1D<?>> DiagonalStore.Builder<ComplexNumber, D> makeDiagonal(final D mainDiagonal) {
            return DiagonalStore.builder(GenericStore.COMPLEX, mainDiagonal);
        }

        public LogicalBuilder<ComplexNumber> makeIdentity(final int dimension) {
            return new LogicalBuilder<>(new IdentityStore<>(GenericStore.COMPLEX, dimension));
        }

        public LogicalBuilder<ComplexNumber> makeSingle(final ComplexNumber element) {
            return new LogicalBuilder<>(new SingleStore<>(GenericStore.COMPLEX, element));
        }

        public SparseStore<ComplexNumber> makeSparse(final int rowsCount, final int columnsCount) {
            return SparseStore.COMPLEX.make(rowsCount, columnsCount);
        }

        public LogicalBuilder<ComplexNumber> makeWrapper(final Access2D<?> access) {
            return new LogicalBuilder<>(new WrapperStore<>(GenericStore.COMPLEX, access));
        }

        public LogicalBuilder<ComplexNumber> makeZero(final int rowsCount, final int columnsCount) {
            return new LogicalBuilder<>(new ZeroStore<>(GenericStore.COMPLEX, rowsCount, columnsCount));
        }

    };

    Factory<Double> PRIMITIVE32 = new Factory<Double>() {

        public <D extends Access1D<?>> Builder<Double, D> makeDiagonal(final D mainDiagonal) {
            return DiagonalStore.builder(Primitive32Store.FACTORY, mainDiagonal);
        }

        public LogicalBuilder<Double> makeIdentity(final int dimension) {
            return new LogicalBuilder<>(new IdentityStore<>(Primitive32Store.FACTORY, dimension));
        }

        public LogicalBuilder<Double> makeSingle(final Double element) {
            return new LogicalBuilder<>(new SingleStore<>(Primitive32Store.FACTORY, element));
        }

        public SparseStore<Double> makeSparse(final int rowsCount, final int columnsCount) {
            return SparseStore.PRIMITIVE32.make(rowsCount, columnsCount);
        }

        public LogicalBuilder<Double> makeWrapper(final Access2D<?> access) {
            return new LogicalBuilder<>(new WrapperStore<>(Primitive32Store.FACTORY, access));
        }

        public LogicalBuilder<Double> makeZero(final int rowsCount, final int columnsCount) {
            return new LogicalBuilder<>(new ZeroStore<>(Primitive32Store.FACTORY, rowsCount, columnsCount));
        }

    };

    Factory<Double> PRIMITIVE64 = new Factory<Double>() {

        public <D extends Access1D<?>> Builder<Double, D> makeDiagonal(final D mainDiagonal) {
            return DiagonalStore.builder(Primitive64Store.FACTORY, mainDiagonal);
        }

        public LogicalBuilder<Double> makeIdentity(final int dimension) {
            return new LogicalBuilder<>(new IdentityStore<>(Primitive64Store.FACTORY, dimension));
        }

        public LogicalBuilder<Double> makeSingle(final Double element) {
            return new LogicalBuilder<>(new SingleStore<>(Primitive64Store.FACTORY, element));
        }

        public SparseStore<Double> makeSparse(final int rowsCount, final int columnsCount) {
            return SparseStore.PRIMITIVE64.make(rowsCount, columnsCount);
        }

        public LogicalBuilder<Double> makeWrapper(final Access2D<?> access) {
            return new LogicalBuilder<>(new WrapperStore<>(Primitive64Store.FACTORY, access));
        }

        public LogicalBuilder<Double> makeZero(final int rowsCount, final int columnsCount) {
            return new LogicalBuilder<>(new ZeroStore<>(Primitive64Store.FACTORY, rowsCount, columnsCount));
        }

    };

    Factory<Quaternion> QUATERNION = new Factory<Quaternion>() {

        public <D extends Access1D<?>> Builder<Quaternion, D> makeDiagonal(final D mainDiagonal) {
            return DiagonalStore.builder(GenericStore.QUATERNION, mainDiagonal);
        }

        public LogicalBuilder<Quaternion> makeIdentity(final int dimension) {
            return new LogicalBuilder<>(new IdentityStore<>(GenericStore.QUATERNION, dimension));
        }

        public LogicalBuilder<Quaternion> makeSingle(final Quaternion element) {
            return new LogicalBuilder<>(new SingleStore<>(GenericStore.QUATERNION, element));
        }

        public SparseStore<Quaternion> makeSparse(final int rowsCount, final int columnsCount) {
            return SparseStore.QUATERNION.make(rowsCount, columnsCount);
        }

        public LogicalBuilder<Quaternion> makeWrapper(final Access2D<?> access) {
            return new LogicalBuilder<>(new WrapperStore<>(GenericStore.QUATERNION, access));
        }

        public LogicalBuilder<Quaternion> makeZero(final int rowsCount, final int columnsCount) {
            return new LogicalBuilder<>(new ZeroStore<>(GenericStore.QUATERNION, rowsCount, columnsCount));
        }

    };

    Factory<RationalNumber> RATIONAL = new Factory<RationalNumber>() {

        public <D extends Access1D<?>> Builder<RationalNumber, D> makeDiagonal(final D mainDiagonal) {
            return DiagonalStore.builder(GenericStore.RATIONAL, mainDiagonal);
        }

        public LogicalBuilder<RationalNumber> makeIdentity(final int dimension) {
            return new LogicalBuilder<>(new IdentityStore<>(GenericStore.RATIONAL, dimension));
        }

        public LogicalBuilder<RationalNumber> makeSingle(final RationalNumber element) {
            return new LogicalBuilder<>(new SingleStore<>(GenericStore.RATIONAL, element));
        }

        public SparseStore<RationalNumber> makeSparse(final int rowsCount, final int columnsCount) {
            return SparseStore.RATIONAL.make(rowsCount, columnsCount);
        }

        public LogicalBuilder<RationalNumber> makeWrapper(final Access2D<?> access) {
            return new LogicalBuilder<>(new WrapperStore<>(GenericStore.RATIONAL, access));
        }

        public LogicalBuilder<RationalNumber> makeZero(final int rowsCount, final int columnsCount) {
            return new LogicalBuilder<>(new ZeroStore<>(GenericStore.RATIONAL, rowsCount, columnsCount));
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

        PhysicalStore<N> retVal = this.physical().make(this);

        this.supplyTo(retVal);

        return retVal;
    }

    default double doubleValue(final long row, final long col) {
        return Scalar.doubleValue(this.get(row, col));
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

        N element = this.get(0L);

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

        double frobeniusNorm = Scalar.doubleValue(this.aggregateAll(Aggregator.NORM2));

        if (this.isVector()) {
            return frobeniusNorm;
        } else {
            // Bringing it closer to what the operator norm would be
            // In case of representing a ComplexNumber or Quaternion as a matrix this will match their norms
            return frobeniusNorm / PrimitiveMath.SQRT.invoke((double) Math.min(this.countRows(), this.countColumns()));
        }
    }

    default MatrixStore<N> operateOnAll(final UnaryFunction<N> operator) {
        return new UnaryOperatoStore<>(this, operator);
    }

    /**
     * Multiply this matrix by itself {@code power} times.
     */
    default MatrixStore<N> power(final int power) {

        if (power < 0) {
            throw new ProgrammingError("Negative powers not supported!");
        }

        if (!this.isSquare()) {
            throw new ProgrammingError("Matrix must be square!");
        }

        PhysicalStore.Factory<N, ?> factory = this.physical();

        if (power == 0) {

            return factory.builder().makeIdentity(this.countRows()).get();

        } else if (power == 1) {

            return this;

        } else if (power == 2) {

            return this.multiply(this);

        } else if ((power % 2) == 0) {
            // 4,6,8,10...

            return this.power(2).power(power / 2);

        } else if (power > 8) {
            // 9,11,13,15...

            return this.power(power - 1).multiply(this);

        } else {
            // 3,5,7

            PhysicalStore<N> right = factory.make(this);
            PhysicalStore<N> product = factory.make(this);
            PhysicalStore<N> temp;

            this.multiply(this, product);
            for (int i = 2; i < power; i++) {
                temp = right;
                right = product;
                product = temp;
                this.multiply(right, product);
            }

            return product;
        }
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
