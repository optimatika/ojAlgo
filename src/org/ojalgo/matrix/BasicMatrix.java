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
package org.ojalgo.matrix;

import java.math.BigDecimal;
import java.util.List;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.Factory2D;
import org.ojalgo.access.Mutate2D;
import org.ojalgo.access.Supplier2D;
import org.ojalgo.algebra.NormedVectorSpace;
import org.ojalgo.algebra.Operation;
import org.ojalgo.algebra.ScalarOperation;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * This interface declares a set of high level methods for linear algebra. Only the most basic set of matrix
 * functionality is defined here. Various matrix decompositions may be used to do some of the more advanced
 * tasks.
 * </p>
 *
 * @author apete
 */
public interface BasicMatrix extends Access2D<Number>, Access2D.Elements, Access1D.Aggregatable<Number>, NormedVectorSpace<BasicMatrix, Number>,
        Operation.Subtraction<BasicMatrix>, Operation.Multiplication<BasicMatrix>, ScalarOperation.Addition<BasicMatrix, Number>,
        ScalarOperation.Division<BasicMatrix, Number>, ScalarOperation.Subtraction<BasicMatrix, Number> {

    public static interface Builder<I extends BasicMatrix> extends Mutate2D, Supplier2D<I> {

        default I build() {
            return this.get();
        }

    }

    public static interface Factory<I extends BasicMatrix> extends Factory2D<I> {

        Builder<I> getBuilder(int count);

        Builder<I> getBuilder(int rows, int columns);

    }

    /**
     * @param row The row index of where to superimpose the top left element of the addend
     * @param col The column index of where to superimpose the top left element of the addend
     * @param addend A matrix to superimpose
     * @return A new matrix
     */
    BasicMatrix add(int row, int col, Access2D<?> addend);

    /**
     * @return A fully mutable matrix builder with the elements initially set to a copy of this matrix.
     */
    Builder<? extends BasicMatrix> copy();

    /**
     * @return A fully mutable matrix builder with the elements initially set to a copy of this matrix.
     * @deprecated v41 Use {@link #copy()} instead
     */
    @Deprecated
    default Builder<? extends BasicMatrix> copyToBuilder() {
        return this.copy();
    }

    /**
     * Divides the elements of this with the elements of aMtrx. The matrices must have equal dimensions.
     *
     * @param aMtrx The denominator elements.
     * @return A new matrix whos elements are the elements of this divided with the elements of aMtrx.
     */
    BasicMatrix divideElements(Access2D<?> aMtrx);

    /**
     * Will enforce this context
     *
     * @param context The context
     * @return A new matrix with the lements enforced
     */
    BasicMatrix enforce(NumberContext context);

    /**
     * @return true if the frobenius norm of the difference between [this] and [aStore] is zero within the
     *         limits of aCntxt.
     */
    boolean equals(Access2D<?> aMtrx, NumberContext aCntxt);

    /**
     * BasicMatrix instances are intended to be immutable. If they are it is possible to cache (partial)
     * calculation results. Calling this method should flush any cached calculation results.
     */
    void flushCache();

    /**
     * @param first The first column to include.
     * @param limit The limit (exclusive) - the first column not to include.
     * @return A new matrix with only the specified range of columns
     */
    BasicMatrix getColumnsRange(final int first, final int limit);

    /**
     * Matrix condition (2-norm)
     *
     * @return ratio of largest to smallest singular value.
     * @deprecated v40 Use {@link SingularValue}
     */
    @Deprecated
    Scalar<?> getCondition();

    /**
     * @return The matrix' determinant.
     */
    Scalar<?> getDeterminant();

    /**
     * @deprecated v40 Use {@link SingularValue}
     */
    @Deprecated
    List<ComplexNumber> getEigenvalues();

    /**
     * The Frobenius norm is the square root of the sum of the squares of each element, or the square root of
     * the sum of the square of the singular values.
     *
     * @return The matrix' Frobenius norm
     * @see #getFrobeniusNorm()
     * @see #getInfinityNorm()
     * @see #getKyFanNorm(int)
     * @see #getOneNorm()
     * @see #getOperatorNorm()
     * @see #getTraceNorm()
     * @see #getVectorNorm(int)
     * @deprecated v40 Use {@link #norm()} or {@link #aggregateAll(org.ojalgo.function.aggregator.Aggregator)}
     */
    @Deprecated
    Scalar<?> getFrobeniusNorm();

    /**
     * @return Max row sum
     * @see #getFrobeniusNorm()
     * @see #getInfinityNorm()
     * @see #getKyFanNorm(int)
     * @see #getOneNorm()
     * @see #getOperatorNorm()
     * @see #getTraceNorm()
     * @see #getVectorNorm(int)
     * @deprecated v40 Use {@link #norm()} or {@link #aggregateAll(org.ojalgo.function.aggregator.Aggregator)}
     */
    @Deprecated
    Scalar<?> getInfinityNorm();

    /**
     * @see #getFrobeniusNorm()
     * @see #getInfinityNorm()
     * @see #getKyFanNorm(int)
     * @see #getOneNorm()
     * @see #getOperatorNorm()
     * @see #getTraceNorm()
     * @see #getVectorNorm(int)
     * @deprecated v40 Use {@link SingularValue}
     */
    @Deprecated
    Scalar<?> getKyFanNorm(int k);

    /**
     * @return Max col sum
     * @see #getFrobeniusNorm()
     * @see #getInfinityNorm()
     * @see #getKyFanNorm(int)
     * @see #getOneNorm()
     * @see #getOperatorNorm()
     * @see #getTraceNorm()
     * @see #getVectorNorm(int)
     * @deprecated v40 Use {@link #norm()} or {@link #aggregateAll(org.ojalgo.function.aggregator.Aggregator)}
     */
    @Deprecated
    Scalar<?> getOneNorm();

    /**
     * 2-norm, max singular value
     *
     * @see #getFrobeniusNorm()
     * @see #getInfinityNorm()
     * @see #getKyFanNorm(int)
     * @see #getOneNorm()
     * @see #getOperatorNorm()
     * @see #getTraceNorm()
     * @see #getVectorNorm(int)
     * @deprecated v40 Use {@link SingularValue}
     */
    @Deprecated
    Scalar<?> getOperatorNorm();

    /**
     * The rank of a matrix is the (maximum) number of linearly independent rows or columns it contains. It is
     * also equal to the number of nonzero singular values of the matrix.
     *
     * @return The matrix' rank.
     * @deprecated v40 Use {@link SingularValue}
     */
    @Deprecated
    int getRank();

    /**
     * @param first The first row to include.
     * @param kimit The limit (exclusive) - the first row not to include.
     * @return A new matrix with only the specified range of rows
     */
    BasicMatrix getRowsRange(final int first, final int kimit);

    /**
     * @deprecated v40 Use {@link SingularValue}
     */
    @Deprecated
    List<? extends Number> getSingularValues();

    /**
     * The sum of the diagonal elements.
     *
     * @return The matrix' trace.
     */
    Scalar<?> getTrace();

    /**
     * @see #getFrobeniusNorm()
     * @see #getInfinityNorm()
     * @see #getKyFanNorm(int)
     * @see #getOneNorm()
     * @see #getOperatorNorm()
     * @see #getTraceNorm()
     * @see #getVectorNorm(int)
     * @deprecated v40 Use {@link SingularValue}
     */
    @Deprecated
    Scalar<?> getTraceNorm();

    /**
     * Treats [this] as if it is one dimensional (a vector) and calculates the vector norm. The interface only
     * requires that implementations can handle arguments 0, 1, 2 and {@linkplain Integer#MAX_VALUE}.
     *
     * @see #getFrobeniusNorm()
     * @see #getInfinityNorm()
     * @see #getKyFanNorm(int)
     * @see #getOneNorm()
     * @see #getOperatorNorm()
     * @see #getTraceNorm()
     * @see #getVectorNorm(int)
     * @deprecated v40 Use {@link #aggregateAll(org.ojalgo.function.aggregator.Aggregator)}
     */
    @Deprecated
    Scalar<?> getVectorNorm(int aDegree);

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
    BasicMatrix invert();

    /**
     * @return true if {@linkplain #getRank()} == min({@linkplain #countRows()}, {@linkplain #countColumns()})
     * @deprecated v40
     */
    @Deprecated
    boolean isFullRank();

    boolean isHermitian();

    boolean isSymmetric();

    /**
     * [aMtrx] is appended to the bottom of [this]. The two matrices must have the same number of columns.
     *
     * @param aMtrx The matrix to merge.
     * @return A new matrix with more rows.
     */
    BasicMatrix mergeColumns(Access2D<?> aMtrx);

    /**
     * [aMtrx] is appended to the right side of [this]. The two matrices must have the same number of rows.
     *
     * @param aMtrx The matrix to merge.
     * @return A new matrix with more columns.
     */
    BasicMatrix mergeRows(Access2D<?> aMtrx);

    BasicMatrix modify(UnaryFunction<? extends Number> aFunc);

    /**
     * Multiplies the elements of this matrix with the elements of aMtrx. The matrices must have equal
     * dimensions.
     *
     * @param aMtrx The elements to multiply by.
     * @return A new matrix whos elements are the elements of this multiplied with the elements of aMtrx.
     */
    BasicMatrix multiplyElements(Access2D<?> aMtrx);

    /**
     * @param someCols An ordered array of column indeces.
     * @return A matrix with a subset of, reordered, columns.
     */
    BasicMatrix selectColumns(int... someCols);

    /**
     * @param someRows An ordered array of row indeces.
     * @return A matrix with a subset of, reordered, rows.
     */
    BasicMatrix selectRows(int... someRows);

    /**
     * <p>
     * This method solves a system of linear equations: [this][X]=[aRHS]. A combination of columns in [this]
     * should produce a column in [aRHS]. It is ok for [aRHS] to have more than 1 column.
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
    BasicMatrix solve(Access2D<?> aRHS);

    /**
     * Extracts one element of this matrix as a BigDecimal.
     *
     * @param row A row index.
     * @param column A column index.
     * @return One matrix element
     * @deprecated v40 Use {@link #get(long, long)} instead
     */
    @Deprecated
    BigDecimal toBigDecimal(int row, int column);

    /**
     * Must be a copy that is safe to modify.
     *
     * @see org.ojalgo.matrix.BasicMatrix#toComplexStore()
     * @see org.ojalgo.matrix.BasicMatrix#toPrimitiveStore()
     * @deprecated Use {@linkplain BigDenseStore#FACTORY} instead
     */
    @Deprecated
    PhysicalStore<BigDecimal> toBigStore();

    /**
     * Extracts one element of this matrix as a ComplexNumber.
     *
     * @param row A row index.
     * @param column A column index.
     * @return One matrix element
     * @deprecated v40 Use {@link #get(long, long)} instead
     */
    @Deprecated
    ComplexNumber toComplexNumber(int row, int column);

    /**
     * Must be a copy that is safe to modify.
     *
     * @see org.ojalgo.matrix.BasicMatrix#toBigStore()
     * @see org.ojalgo.matrix.BasicMatrix#toPrimitiveStore()
     * @deprecated Use {@linkplain ComplexDenseStore#FACTORY} instead
     */
    @Deprecated
    PhysicalStore<ComplexNumber> toComplexStore();

    /**
     * @deprecated v40 Use {@link #columns()}
     */
    @Deprecated
    List<BasicMatrix> toListOfColumns();

    /**
     * It is also possible to call {@linkplain #toBigStore()}, {@linkplain #toComplexStore()} or
     * {@linkplain #toPrimitiveStore()} and then {@linkplain PhysicalStore#asList()}.
     *
     * @deprecated v40 Use {@link #iterator()}
     */
    @Deprecated
    List<? extends Number> toListOfElements();

    /**
     * @deprecated v40 Use {@link #rows()}
     */
    @Deprecated
    List<BasicMatrix> toListOfRows();

    /**
     * Must be a copy that is safe to modify.
     *
     * @see org.ojalgo.matrix.BasicMatrix#toBigStore()
     * @see org.ojalgo.matrix.BasicMatrix#toComplexStore()
     * @deprecated Use {@linkplain PrimitiveDenseStore#FACTORY} instead
     */
    @Deprecated
    PhysicalStore<Double> toPrimitiveStore();

    /**
     * Extracts one element of this matrix as a Scalar.
     *
     * @param row A row index.
     * @param col A column index.
     * @return One matrix element
     */
    Scalar<?> toScalar(long row, long col);

    String toString(int row, int col);

    /**
     * Transposes this matrix. For complex matrices conjugate() and transpose() are NOT EQUAL.
     *
     * @return A matrix that is the transpose of this matrix.
     * @see org.ojalgo.matrix.BasicMatrix#conjugate()
     */
    BasicMatrix transpose();

}
