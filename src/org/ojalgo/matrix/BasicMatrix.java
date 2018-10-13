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
package org.ojalgo.matrix;

import java.util.List;
import java.util.function.Supplier;

import org.ojalgo.algebra.NormedVectorSpace;
import org.ojalgo.algebra.Operation;
import org.ojalgo.algebra.ScalarOperation;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.MatrixDecomposition;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * This interface declares a limited set of high level methods for linear algebra. If this is not enough for
 * your use case, then look at the various interfaces/classes in the {@linkplain org.ojalgo.matrix.store}
 * and/or {@linkplain org.ojalgo.matrix.decomposition} packages.
 * </p>
 *
 * @author apete
 * @deprecated v46.3 Use the specific implementations instead {@link PrimitiveMatrix}, {@link ComplexMatrix}
 *             or {@link RationalMatrix}.
 */
@Deprecated
public interface BasicMatrix<N extends Number, M extends BasicMatrix<N, M>> extends NormedVectorSpace<M, N>, Operation.Subtraction<M>,
        Operation.Multiplication<M>, ScalarOperation.Addition<M, N>, ScalarOperation.Division<M, N>, ScalarOperation.Subtraction<M, N>, Access2D<N>,
        Access2D.Elements, Access2D.Aggregatable<N>, Structure2D.ReducibleTo1D<M>, NumberContext.Enforceable<M> {

    @SuppressWarnings("unchecked")
    public static interface LogicalBuilder<N extends Number, I extends BasicMatrix<N, I>>
            extends Structure2D.Logical<I, BasicMatrix.LogicalBuilder<N, I>>, Access2D.Collectable<N, PhysicalStore<N>> {

        LogicalBuilder<N, I> above(int numberOfRows);

        LogicalBuilder<N, I> above(N... elements);

        LogicalBuilder<N, I> below(int numberOfRows);

        LogicalBuilder<N, I> below(N... elements);

        LogicalBuilder<N, I> bidiagonal(boolean upper, boolean assumeOne);

        default I build() {
            return this.get();
        }

        LogicalBuilder<N, I> column(final int... columns);

        LogicalBuilder<N, I> conjugate();

        LogicalBuilder<N, I> diagonal();

        LogicalBuilder<N, I> hermitian(boolean upper);

        LogicalBuilder<N, I> hessenberg(boolean upper);

        LogicalBuilder<N, I> left(int numberOfColumns);

        LogicalBuilder<N, I> left(N... elements);

        LogicalBuilder<N, I> limits(int rowLimit, int columnLimit);

        LogicalBuilder<N, I> offsets(int rowOffset, int columnOffset);

        LogicalBuilder<N, I> right(int numberOfColumns);

        LogicalBuilder<N, I> right(N... elements);

        LogicalBuilder<N, I> row(final int... rows);

        LogicalBuilder<N, I> superimpose(I matrix);

        LogicalBuilder<N, I> superimpose(int row, int col, I matrix);

        LogicalBuilder<N, I> superimpose(int row, int col, Number matrix);

        LogicalBuilder<N, I> transpose();

        LogicalBuilder<N, I> triangular(boolean upper, boolean assumeOne);

        LogicalBuilder<N, I> tridiagonal();

    }

    public static interface PhysicalBuilder<N extends Number, I extends BasicMatrix<N, I>>
            extends Mutate2D.Receiver<N>, Mutate2D.BiModifiable<N>, Mutate2D.Exchangeable, Supplier<I>, Access2D.Collectable<N, PhysicalStore<N>> {

        default I build() {
            return this.get();
        }

    }

    /**
     * The Frobenius norm is the square root of the sum of the squares of each element, or the square root of
     * the sum of the square of the singular values.
     *
     * @return The matrix' Frobenius norm
     */
    public static <M extends BasicMatrix<?, M>> double calculateFrobeniusNorm(final M matrix) {
        return matrix.norm();
    }

    /**
     * @return The inf-norm or maximum row sum
     */
    public static <M extends BasicMatrix<?, M>> double calculateInfinityNorm(final M matrix) {

        double retVal = PrimitiveMath.ZERO;

        final long tmpLimit = matrix.countRows();
        for (long i = 0L; i < tmpLimit; i++) {
            retVal = PrimitiveFunction.MAX.invoke(retVal, matrix.aggregateRow(i, Aggregator.NORM1).doubleValue());
        }

        return retVal;
    }

    /**
     * @return The 1-norm or maximum column sum
     */
    public static <M extends BasicMatrix<?, M>> double calculateOneNorm(final M matrix) {

        double retVal = PrimitiveMath.ZERO;

        final long tmpLimit = matrix.countColumns();
        for (long j = 0L; j < tmpLimit; j++) {
            retVal = PrimitiveFunction.MAX.invoke(retVal, matrix.aggregateColumn(j, Aggregator.NORM1).doubleValue());
        }

        return retVal;
    }

    /**
     * @param row The row index of where to superimpose the top left element of the addend
     * @param col The column index of where to superimpose the top left element of the addend
     * @param addend A matrix to superimpose
     * @return A new matrix
     * @deprecated v46 Use {@link #logical()} or {@link #copy()} instead
     */
    @Deprecated
    M add(int row, int col, Access2D<?> addend);

    /**
     * @return A fully mutable matrix builder with the elements initially set to a copy of this matrix.
     */
    PhysicalBuilder<N, M> copy();

    /**
     * Divides the elements of this with the elements of aMtrx. The matrices must have equal dimensions.
     *
     * @param aMtrx The denominator elements.
     * @return A new matrix whos elements are the elements of this divided with the elements of aMtrx.
     * @deprecated v46 Use {@link #logical()} or {@link #copy()} instead
     */
    @Deprecated
    M divideElements(Access2D<?> aMtrx);

    /**
     * @return true if the frobenius norm of the difference between [this] and [aStore] is zero within the
     *         limits of aCntxt.
     */
    boolean equals(Access2D<?> another, NumberContext precision);

    /**
     * BasicMatrix instances are intended to be immutable. If they are it is possible to cache (partial)
     * calculation results. Calling this method should flush any cached calculation results.
     */
    void flushCache();

    /**
     * @param first The first column to include.
     * @param limit The limit (exclusive) - the first column not to include.
     * @return A new matrix with only the specified range of columns
     * @deprecated v46 Use {@link #logical()} or {@link #copy()} instead
     */
    @Deprecated
    M getColumnsRange(final int first, final int limit);

    /**
     * Matrix condition (2-norm)
     *
     * @return ratio of largest to smallest singular value.
     */
    Scalar<?> getCondition();

    /**
     * @return The matrix' determinant.
     */
    Scalar<?> getDeterminant();

    List<Eigenvalue.Eigenpair> getEigenpairs();

    /**
     * The rank of a matrix is the (maximum) number of linearly independent rows or columns it contains. It is
     * also equal to the number of nonzero singular values of the matrix.
     *
     * @return The matrix' rank.
     * @see MatrixDecomposition.RankRevealing
     */
    int getRank();

    /**
     * @param first The first row to include.
     * @param kimit The limit (exclusive) - the first row not to include.
     * @return A new matrix with only the specified range of rows
     * @deprecated v46 Use {@link #logical()} or {@link #copy()} instead
     */
    @Deprecated
    M getRowsRange(final int first, final int kimit);

    /**
     * @deprecated v40 Use {@link SingularValue}
     */
    @Deprecated
    List<Double> getSingularValues();

    /**
     * The sum of the diagonal elements.
     *
     * @return The matrix' trace.
     */
    Scalar<?> getTrace();

    /**
     * <p>
     * About inverting matrices:
     * </p>
     * <ul>
     * <li>"right inverse": [this][right inverse]=[I]. You may calculate it using
     * {@linkplain #solve(Access2D)}.</li>
     * <li>"left inverse": [left inverse][this]=[I]. You may calculate it using {@linkplain #solve(Access2D)}
     * and transposing.</li>
     * <li>"generalised inverse": [this][generalised inverse][this]=[this]. Note that if [this] is singular or
     * non-square, then [generalised inverse] is not unique.</li>
     * <li>"pseudoinverse": The generalised inverse (there are typically/possibly many) with the smallest
     * frobenius norm is called the pseudoinverse. You may calculate it using the {@linkplain QR} or
     * {@linkplain SingularValue} decompositions.</li>
     * <li>"inverse":
     * <ul>
     * <li>If [left inverse]=[right inverse] then it is also [inverse].</li>
     * <li>If [this] is square and has full rank then the [generalised inverse] is unique, with the
     * [pseudoinverse] given, and equal to [inverse].</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @return The "best possible" inverse....
     */
    M invert();

    /**
     * @return true if {@linkplain #getRank()} == min({@linkplain #countRows()}, {@linkplain #countColumns()})
     * @see MatrixDecomposition.RankRevealing
     */
    boolean isFullRank();

    boolean isHermitian();

    boolean isSymmetric();

    LogicalBuilder<N, M> logical();

    /**
     * [belowRows] is appended below [this]. The two matrices must have the same number of columns.
     *
     * @param belowRows The matrix to merge.
     * @return A new matrix with more rows.
     * @deprecated v46 Use {@link #logical()} or {@link #copy()} instead
     */
    @Deprecated
    M mergeColumns(Access2D<?> belowRows);

    /**
     * [rightColumns] is appended to the right of [this]. The two matrices must have the same number of rows.
     *
     * @param rightColumns The matrix to merge.
     * @return A new matrix with more columns.
     * @deprecated v46 Use {@link #logical()} or {@link #copy()} instead
     */
    @Deprecated
    M mergeRows(Access2D<?> rightColumns);

    /**
     * @deprecated v42 Use {@link #logical()} or {@link #copy()} instead
     */
    @Deprecated
    M modify(UnaryFunction<? extends Number> aFunc);

    /**
     * Multiplies the elements of this matrix with the elements of aMtrx. The matrices must have equal
     * dimensions.
     *
     * @param aMtrx The elements to multiply by.
     * @return A new matrix whos elements are the elements of this multiplied with the elements of aMtrx.
     * @deprecated v46 Use {@link #logical()} or {@link #copy()} instead
     */
    @Deprecated
    M multiplyElements(Access2D<?> aMtrx);

    /**
     * @param someCols An ordered array of column indeces.
     * @return A matrix with a subset of, reordered, columns.
     * @deprecated v46 Use {@link #logical()} or {@link #copy()} instead
     */
    @Deprecated
    default M selectColumns(int... someCols) {
        return this.logical().column(someCols).get();
    }

    /**
     * @param someRows An ordered array of row indeces.
     * @return A matrix with a subset of, reordered, rows.
     * @deprecated v46 Use {@link #logical()} or {@link #copy()} instead
     */
    @Deprecated
    default M selectRows(int... someRows) {
        return this.logical().row(someRows).get();
    }

    /**
     * <p>
     * This method solves a system of linear equations: [this][X]=[aRHS]. A combination of columns in [this]
     * should produce a column(s) in [aRHS]. It is ok for [aRHS] to have more than 1 column.
     * </p>
     * <ul>
     * <li>If the problem is over-qualified an approximate solution is returned.</li>
     * <li>If the problem is under-qualified one possible solution is returned.</li>
     * </ul>
     * <p>
     * Remember that: [X][this]=[aRHS] is equivalent to [this]<sup>T</sup>[X]<sup>T</sup>=[aRHS]<sup>T</sup>
     * </p>
     *
     * @param aRHS The right hand side of the equation.
     * @return The solution, [X].
     */
    M solve(Access2D<?> aRHS);

    /**
     * Extracts one element of this matrix as a Scalar.
     *
     * @param row A row index.
     * @param col A column index.
     * @return One matrix element
     */
    Scalar<?> toScalar(long row, long col);

    /**
     * Transposes this matrix. For complex matrices conjugate() and transpose() are NOT EQUAL.
     *
     * @return A matrix that is the transpose of this matrix.
     * @see org.ojalgo.matrix.BasicMatrix#conjugate()
     */
    M transpose();

}
