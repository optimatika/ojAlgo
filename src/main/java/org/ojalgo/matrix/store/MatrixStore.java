/*
 * Copyright 1997-2024 Optimatika
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
import org.ojalgo.array.operation.AMAX;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.Matrix2D;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure1D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.structure.Structure2D.Logical;
import org.ojalgo.type.NumberDefinition;
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
public interface MatrixStore<N extends Comparable<N>> extends Matrix2D<N, MatrixStore<N>>, ElementsSupplier<N>, Access2D.Visitable<N>, Access2D.Sliceable<N>,
        Structure2D.ReducibleTo1D<ElementsSupplier<N>>, Structure2D.Logical<Access2D<N>, MatrixStore<N>> {

    @Override
    default MatrixStore<N> above(final Access2D<N>... matrices) {
        MatrixStore<N> above = AbstractStore.buildRow(this.physical(), this.countColumns(), matrices);
        return new AboveBelowStore<>(above, this);
    }

    @Override
    default MatrixStore<N> above(final Access2D<N> matrix) {
        MatrixStore<N> above = AbstractStore.buildRow(this.physical(), this.countColumns(), matrix);
        return new AboveBelowStore<>(above, this);
    }

    @Override
    default MatrixStore<N> above(final long numberOfRows) {
        ZeroStore<N> above = new ZeroStore<>(this.physical(), numberOfRows, this.countColumns());
        return new AboveBelowStore<>(above, this);
    }

    @Override
    default MatrixStore<N> add(final double scalarAddend) {
        return this.add(this.physical().scalar().cast(scalarAddend));
    }

    @Override
    default MatrixStore<N> add(final MatrixStore<N> addend) {
        return this.onMatching(this.physical().function().add(), addend).collect(this.physical());
    }

    @Override
    default MatrixStore<N> add(final N scalarAddend) {
        return this.onAll(this.physical().function().add().second(scalarAddend));
    }

    @Override
    default N aggregateAll(final Aggregator aggregator) {

        AggregatorFunction<N> tmpVisitor = this.physical().aggregator().get(aggregator);

        this.visitAll(tmpVisitor);

        return tmpVisitor.get();
    }

    @Override
    default N aggregateColumn(final long row, final long col, final Aggregator aggregator) {

        AggregatorFunction<N> tmpVisitor = this.physical().aggregator().get(aggregator);

        this.visitColumn(row, col, tmpVisitor);

        return tmpVisitor.get();
    }

    @Override
    default N aggregateDiagonal(final long row, final long col, final Aggregator aggregator) {

        AggregatorFunction<N> tmpVisitor = this.physical().aggregator().get(aggregator);

        this.visitDiagonal(row, col, tmpVisitor);

        return tmpVisitor.get();
    }

    @Override
    default N aggregateRange(final long first, final long limit, final Aggregator aggregator) {

        AggregatorFunction<N> tmpVisitor = this.physical().aggregator().get(aggregator);

        this.visitRange(first, limit, tmpVisitor);

        return tmpVisitor.get();
    }

    @Override
    default N aggregateRow(final long row, final long col, final Aggregator aggregator) {

        AggregatorFunction<N> tmpVisitor = this.physical().aggregator().get(aggregator);

        this.visitRow(row, col, tmpVisitor);

        return tmpVisitor.get();
    }

    @Override
    default MatrixStore<N> below(final Access2D<N>... matrices) {
        MatrixStore<N> below = AbstractStore.buildRow(this.physical(), this.countColumns(), matrices);
        return new AboveBelowStore<>(this, below);
    }

    @Override
    default MatrixStore<N> below(final Access2D<N> matrix) {
        MatrixStore<N> below = AbstractStore.buildRow(this.physical(), this.countColumns(), matrix);
        return new AboveBelowStore<>(this, below);
    }

    @Override
    default MatrixStore<N> below(final long numberOfRows) {
        ZeroStore<N> below = new ZeroStore<>(this.physical(), numberOfRows, (int) this.countColumns());
        return new AboveBelowStore<>(this, below);
    }

    @Override
    default MatrixStore<N> bidiagonal(final boolean upper) {
        if (upper) {
            return new UpperTriangularStore<>(new LowerHessenbergStore<>(this), false);
        }
        return new LowerTriangularStore<>(new UpperHessenbergStore<>(this), false);
    }

    @Override
    default MatrixStore<N> column(final int column) {
        return Structure2D.Logical.super.column(column);
    }

    @Override
    default MatrixStore<N> column(final long column) {
        return Structure2D.Logical.super.column(column);
    }

    /**
     * A selection (re-ordering) of columns. Note that it's ok to reference the same base column more than
     * once, and any negative column reference/index will translate to a column of zeros. The number of
     * columns in the resulting matrix is the same as the number of elements in the columns index array.
     */
    @Override
    default MatrixStore<N> columns(final int... columns) {
        return new ColumnsStore<>(this, columns);
    }

    @Override
    default MatrixStore<N> columns(final long... columns) {
        return Structure2D.Logical.super.columns(columns);
    }

    /**
     * Returns the conjugate transpose of this matrix. The conjugate transpose is also known as adjoint
     * matrix, adjugate matrix, hermitian adjoint or hermitian transpose. (The conjugate matrix is the complex
     * conjugate of each element. This NOT what is returned here!)
     *
     * @see org.ojalgo.algebra.VectorSpace#conjugate()
     */
    @Override
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

    @Override
    default MatrixStore<N> diagonal() {
        return new UpperTriangularStore<>(new LowerTriangularStore<>(this, false), false);
    }

    @Override
    default MatrixStore<N> diagonally(final Access2D<N>... diagonally) {

        MatrixStore<N> retVal = this;

        PhysicalStore.Factory<N, ?> tmpFactory = this.physical();

        MatrixStore<N> tmpDiagonalStore;
        for (int ij = 0; ij < diagonally.length; ij++) {

            tmpDiagonalStore = AbstractStore.cast(tmpFactory, diagonally[ij]);

            int tmpBaseRowDim = (int) this.countRows();
            int tmpBaseColDim = (int) this.countColumns();

            int tmpDiagRowDim = (int) tmpDiagonalStore.countRows();
            int tmpDiagColDim = (int) tmpDiagonalStore.countColumns();

            MatrixStore<N> tmpRightStore = new ZeroStore<>(tmpFactory, tmpBaseRowDim, tmpDiagColDim);
            MatrixStore<N> tmpAboveStore = new LeftRightStore<>(this, tmpRightStore);

            MatrixStore<N> tmpLeftStore = new ZeroStore<>(tmpFactory, tmpDiagRowDim, tmpBaseColDim);
            MatrixStore<N> tmpBelowStore = new LeftRightStore<>(tmpLeftStore, tmpDiagonalStore);

            retVal = new AboveBelowStore<>(tmpAboveStore, tmpBelowStore);
        }

        return retVal;
    }

    @Override
    default MatrixStore<N> divide(final double scalarDivisor) {
        return this.divide(this.physical().scalar().cast(scalarDivisor));
    }

    @Override
    default MatrixStore<N> divide(final N scalarDivisor) {
        return this.onAll(this.physical().function().divide().second(scalarDivisor));
    }

    @Override
    default double doubleValue(final int row, final int col) {
        return NumberDefinition.doubleValue(this.get(row, col));
    }

    default boolean equals(final MatrixStore<N> other, final NumberContext context) {
        return Access2D.equals(this, other, context);
    }

    /**
     * @deprecated v50 No need as {@link MatrixStore} now implements {@link Logical} and this method simply
     *             return "this".
     */
    @Deprecated
    default MatrixStore<N> get() {
        return this;
    }

    N get(int row, int col);

    @Override
    default N get(final long row, final long col) {
        return this.get(Math.toIntExact(row), Math.toIntExact(col));
    }

    @Override
    default MatrixStore<N> hermitian(final boolean upper) {
        if (upper) {
            return new UpperSymmetricStore<>(this, true);
        } else {
            return new LowerSymmetricStore<>(this, true);
        }
    }

    @Override
    default MatrixStore<N> hessenberg(final boolean upper) {
        if (upper) {
            return new UpperHessenbergStore<>(this);
        } else {
            return new LowerHessenbergStore<>(this);
        }
    }

    @Override
    default long indexOfLargest() {
        return AMAX.invoke(this, 0L, this.count(), 1L);
    }

    default boolean isHermitian() {

        int numberOfRows = Math.toIntExact(this.countRows());
        int numberOfColumns = Math.toIntExact(this.countColumns());

        N element = this.get(0L);

        boolean retVal = numberOfRows == numberOfColumns;

        if (element instanceof ComplexNumber) {

            ComplexNumber lowerLeft;
            ComplexNumber upperRight;

            for (int j = 0; retVal && j < numberOfColumns; j++) {
                retVal &= PrimitiveScalar.isSmall(PrimitiveMath.ONE, ComplexNumber.valueOf(this.get(j, j)).i);
                for (int i = j + 1; retVal && i < numberOfRows; i++) {
                    lowerLeft = ComplexNumber.valueOf(this.get(i, j)).conjugate();
                    upperRight = ComplexNumber.valueOf(this.get(j, i));
                    retVal &= PrimitiveScalar.isSmall(PrimitiveMath.ONE, lowerLeft.subtract(upperRight).norm());
                }
            }

        } else {

            for (int j = 0; retVal && j < numberOfColumns; j++) {
                for (int i = j + 1; retVal && i < numberOfRows; i++) {
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

    @Override
    default boolean isSmall(final double comparedTo) {
        return PrimitiveScalar.isSmall(comparedTo, this.norm());
    }

    default boolean isSmall(final long row, final long col, final double comparedTo) {
        return this.toScalar(row, col).isSmall(comparedTo);
    }

    @Override
    default MatrixStore<N> left(final Access2D<N>... matrices) {
        MatrixStore<N> left = AbstractStore.buildColumn(this.physical(), this.countRows(), matrices);
        return new LeftRightStore<>(left, this);
    }

    @Override
    default MatrixStore<N> left(final Access2D<N> matrix) {
        MatrixStore<N> left = AbstractStore.buildColumn(this.physical(), this.countRows(), matrix);
        return new LeftRightStore<>(left, this);
    }

    @Override
    default MatrixStore<N> left(final long numberOfColumns) {
        MatrixStore<N> left = new ZeroStore<>(this.physical(), this.countRows(), numberOfColumns);
        return new LeftRightStore<>(left, this);
    }

    /**
     * Setting either limit to &lt; 0 is interpreted as "no limit" (useful when you only want to limit either
     * the rows or columns, and don't know the size of the other)
     */
    @Override
    default MatrixStore<N> limits(final long rowLimit, final long columnLimit) {
        return new LimitStore<>(rowLimit < 0 ? (int) this.countRows() : rowLimit, columnLimit < 0 ? (int) this.countColumns() : columnLimit, this);
    }

    /**
     * @deprecated v50 No need as {@link MatrixStore} now implements {@link Logical}.
     */
    @Deprecated
    default MatrixStore<N> logical() {
        return this;
    }

    default void multiply(final Access1D<N> right, final TransformableRegion<N> target) {
        target.fillByMultiplying(this, right);
    }

    @Override
    default MatrixStore<N> multiply(final double scalarMultiplicand) {
        return this.multiply(this.physical().scalar().cast(scalarMultiplicand));
    }

    @Override
    default MatrixStore<N> multiply(final MatrixStore<N> right) {

        long tmpCountRows = this.countRows();
        long tmpCountColumns = right.countColumns();

        PhysicalStore<N> retVal = this.physical().make(tmpCountRows, tmpCountColumns);

        this.multiply(right, retVal);

        return retVal;
    }

    @Override
    default MatrixStore<N> multiply(final N scalarMultiplicand) {
        return this.onAll(this.physical().function().multiply().second(scalarMultiplicand));
    }

    /**
     * Assumes [leftAndRight] is a vector and will calulate [leftAndRight]<sup>H</sup>[this][leftAndRight]
     *
     * @param leftAndRight The argument vector
     * @return A scalar (extracted from the resulting 1 x 1 matrix)
     */
    default N multiplyBoth(final Access1D<N> leftAndRight) {

        PhysicalStore<N> tmpStep1 = this.physical().make(1L, leftAndRight.count());
        PhysicalStore<N> tmpStep2 = this.physical().make(1L, 1L);

        PhysicalStore<N> tmpLeft = this.physical().rows(leftAndRight);
        tmpLeft.modifyAll(this.physical().function().conjugate());
        tmpStep1.fillByMultiplying(tmpLeft.conjugate(), this);

        tmpStep2.fillByMultiplying(tmpStep1, leftAndRight);

        return tmpStep2.get(0L);
    }

    @Override
    default MatrixStore<N> negate() {
        return this.onAll(this.physical().function().negate());
    }

    @Override
    default double norm() {

        double frobeniusNorm = NumberDefinition.doubleValue(this.aggregateAll(Aggregator.NORM2));

        if (this.isVector()) {
            return frobeniusNorm;
        }
        // Bringing it closer to what the operator norm would be
        // In case of representing a ComplexNumber or Quaternion as a matrix this will match their norms
        return frobeniusNorm / PrimitiveMath.SQRT.invoke((double) Math.min(this.countRows(), this.countColumns()));
    }

    @Override
    default MatrixStore<N> offsets(final long rowOffset, final long columnOffset) {
        return new OffsetStore<>(this, rowOffset < 0 ? 0 : rowOffset, columnOffset < 0 ? 0 : columnOffset);
    }

    @Override
    default MatrixStore<N> onAll(final UnaryFunction<N> operator) {
        return new UnaryOperatoStore<>(this, operator);
    }

    default ElementsSupplier<N> operate() {
        return this;
    }

    PhysicalStore.Factory<N, ?> physical();

    /**
     * Multiply this matrix by itself {@code power} times.
     */
    @Override
    default MatrixStore<N> power(final int power) {

        if (power < 0) {
            throw new ProgrammingError("Negative powers not supported!");
        }

        if (!this.isSquare()) {
            throw new ProgrammingError("Matrix must be square!");
        }

        PhysicalStore.Factory<N, ?> factory = this.physical();

        if (power == 0) {
            return factory.makeIdentity(this.countRows());
        }

        if (power == 1) {
            return this;
        }

        if (power == 2) {
            return this.multiply(this);
        }

        if (power % 2 == 0) {
            // 4,6,8,10...
            return this.power(2).power(power / 2);
        }

        if (power > 8) {
            // 9,11,13,15...
            return this.power(power - 1).multiply(this);
        }

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

    @Override
    default ElementsSupplier<N> reduceColumns(final Aggregator aggregator) {
        return new MatrixPipeline.ColumnsReducer<>(this, aggregator);
    }

    @Override
    default ElementsSupplier<N> reduceRows(final Aggregator aggregator) {
        return new MatrixPipeline.RowsReducer<>(this, aggregator);
    }

    @Override
    default MatrixStore<N> repeat(final int rowsRepetitions, final int columnsRepetitions) {

        MatrixStore<N> retVal = this;

        if (rowsRepetitions > 1) {
            retVal = new RepeatedRowsStore<>(retVal, rowsRepetitions);
        }

        if (columnsRepetitions > 1) {
            retVal = new RepeatedColumnsStore<>(retVal, columnsRepetitions);
        }

        return retVal;
    }

    @Override
    default MatrixStore<N> right(final Access2D<N>... matrices) {
        MatrixStore<N> right = AbstractStore.buildColumn(this.physical(), this.countRows(), matrices);
        return new LeftRightStore<>(this, right);
    }

    @Override
    default MatrixStore<N> right(final Access2D<N> matrix) {
        MatrixStore<N> right = AbstractStore.buildColumn(this.physical(), this.countRows(), matrix);
        return new LeftRightStore<>(this, right);
    }

    @Override
    default MatrixStore<N> right(final long numberOfColumns) {
        MatrixStore<N> right = new ZeroStore<>(this.physical(), this.countRows(), numberOfColumns);
        return new LeftRightStore<>(this, right);
    }

    @Override
    default MatrixStore<N> row(final int row) {
        return Structure2D.Logical.super.row(row);
    }

    @Override
    default MatrixStore<N> row(final long row) {
        return Structure2D.Logical.super.row(row);
    }

    /**
     * A selection (re-ordering) of rows. Note that it's ok to reference the same base row more than once, and
     * any negative row reference/index will translate to a row of zeros. The number of rows in the resulting
     * matrix is the same as the number of elements in the rows index array.
     */
    @Override
    default MatrixStore<N> rows(final int... rows) {
        return new RowsStore<>(this, rows);
    }

    @Override
    default MatrixStore<N> rows(final long... rows) {
        return Structure2D.Logical.super.rows(rows);
    }

    @Override
    default MatrixStore<N> select(final int[] rows, final int[] columns) {

        MatrixStore<N> retVal = this;

        if (rows != null && rows.length > 0) {
            retVal = retVal.rows(rows);
        }

        if (columns != null && columns.length > 0) {
            retVal = retVal.columns(columns);
        }

        return retVal;
    }

    @Override
    default MatrixStore<N> select(final long[] rows, final long[] columns) {
        return this.select(Structure1D.toIntIndexes(rows), Structure1D.toIntIndexes(columns));
    }

    @Override
    default MatrixStore<N> signum() {
        return this.multiply(PrimitiveMath.ONE / this.norm());
    }

    @Override
    default Access1D<N> sliceColumn(final long row, final long col) {
        return new Access1D<>() {

            public long count() {
                return MatrixStore.this.countRows() - row;
            }

            public double doubleValue(final int index) {
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

    @Override
    default Access1D<N> sliceDiagonal(final long row, final long col) {
        return new Access1D<>() {

            public long count() {
                return Math.min(MatrixStore.this.countRows() - row, MatrixStore.this.countColumns() - col);
            }

            public double doubleValue(final int index) {
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

    @Override
    default Access1D<N> sliceRange(final long first, final long limit) {
        return new Access1D<>() {

            public long count() {
                return limit - first;
            }

            public double doubleValue(final int index) {
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

    @Override
    default Access1D<N> sliceRow(final long row, final long col) {
        return new Access1D<>() {

            public long count() {
                return MatrixStore.this.countColumns() - col;
            }

            public double doubleValue(final int index) {
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

    @Override
    default MatrixStore<N> subtract(final double scalarSubtrahend) {
        return this.subtract(this.physical().scalar().cast(scalarSubtrahend));
    }

    @Override
    default MatrixStore<N> subtract(final MatrixStore<N> subtrahend) {
        return this.onMatching(this.physical().function().subtract(), subtrahend).collect(this.physical());
    }

    @Override
    default MatrixStore<N> subtract(final N scalarSubtrahend) {
        return this.onAll(this.physical().function().subtract().second(scalarSubtrahend));
    }

    @Override
    default MatrixStore<N> superimpose(final Access2D<N> matrix) {
        return new SuperimposedStore<>(this, 0, 0, AbstractStore.cast(this.physical(), matrix));
    }

    @Override
    default MatrixStore<N> superimpose(final long row, final long col, final Access2D<N> matrix) {
        return new SuperimposedStore<>(this, row, col, AbstractStore.cast(this.physical(), matrix));
    }

    @Override
    default void supplyTo(final TransformableRegion<N> receiver) {
        if (!receiver.isAcceptable(this)) {
            throw new ProgrammingError("Not acceptable!");
        }
        receiver.fillMatching(this);
    }

    @Override
    default MatrixStore<N> symmetric(final boolean upper) {
        if (upper) {
            return new UpperSymmetricStore<>(this, false);
        }
        return new LowerSymmetricStore<>(this, false);
    }

    default Scalar<N> toScalar(final long row, final long column) {
        return this.physical().scalar().convert(this.get(row, column));
    }

    /**
     * @return A transposed matrix instance.
     */
    @Override
    default MatrixStore<N> transpose() {
        return new TransposedStore<>(this);
    }

    @Override
    default MatrixStore<N> triangular(final boolean upper, final boolean assumeOne) {
        if (upper) {
            return new UpperTriangularStore<>(this, assumeOne);
        }
        return new LowerTriangularStore<>(this, assumeOne);
    }

    @Override
    default MatrixStore<N> tridiagonal() {
        return new UpperHessenbergStore<>(new LowerHessenbergStore<>(this));
    }

    @Override
    default void visitOne(final long row, final long col, final VoidFunction<N> visitor) {
        visitor.invoke(this.get(row, col));
    }

}
