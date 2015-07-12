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
package org.ojalgo.matrix;

import java.math.BigDecimal;
import java.util.List;

import org.ojalgo.access.Access2D;
import org.ojalgo.algebra.NormedVectorSpace;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * This interface declares a set of high level methods for linear algebra. Only the most basic set of matrix
 * functionality is defined here. Various matrix decompositions may be used to do some of the more advanced
 * tasks.
 * </p>
 * <p>
 * A vector is a matrix with column (or perhaps row) dimension 1.
 * </p>
 *
 * @see LU
 * @see Cholesky
 * @see QR
 * @see Eigenvalue
 * @see SingularValue
 * @see MatrixStore
 * @author apete
 */
public interface BasicMatrix extends Access2D<Number>, NormedVectorSpace<BasicMatrix, Number> {

    /**
     * @author apete
     */
    public static interface Factory<I extends BasicMatrix> extends Access2D.Factory<I> {

        Access2D.Builder<I> getBuilder(int count);

        Access2D.Builder<I> getBuilder(int rows, int columns);

    }

    /**
     * Adds the elements of aMtrx to the elements of this matrix. The matrices must have equal dimensions.
     *
     * @param addend What to add.
     * @return A new matrix whos elements are the sum of this' and aMtrx'.
     */
    BasicMatrix add(Access2D<?> addend);

    /**
     * @param row The row index of where to superimpose the top left element of aMtrx
     * @param col The column index of where to superimpose the top left element of aMtrx
     * @param addend A matrix to superimpose
     * @return A new matrix
     */
    BasicMatrix add(int row, int col, Access2D<?> addend);

    /**
     * Do not use this method to populate large dense matrices! Only use it to change a few (a small number)
     * of elements.
     */
    BasicMatrix add(int row, int col, Number value);

    /**
     * Adds value to the elements of this.
     *
     * @param value What to add
     * @return A new matrix whos elements are the sum of this' elements and value.
     */
    BasicMatrix add(Number value);

    /**
     * @return A fully mutable matrix builder with the elements initially set to a copy of this matrix.
     */
    Builder<? extends BasicMatrix> copyToBuilder();

    /**
     * Divides the elements of this with value.
     *
     * @param value The denominator.
     * @return A new matrix whos elements are the elements of this divided with value.
     */
    BasicMatrix divide(Number value);

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
     * @param aFirst The first column to include.
     * @param aLimit The limit (exclusive) - the first column not to include.
     * @return A new matrix with only the specified range of columns
     */
    BasicMatrix getColumnsRange(final int aFirst, final int aLimit);

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
     */
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
     */
    Scalar<?> getInfinityNorm();

    /**
     * @see #getFrobeniusNorm()
     * @see #getInfinityNorm()
     * @see #getKyFanNorm(int)
     * @see #getOneNorm()
     * @see #getOperatorNorm()
     * @see #getTraceNorm()
     * @see #getVectorNorm(int)
     */
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
     */
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
     */
    Scalar<?> getOperatorNorm();

    /**
     * The rank of a matrix is the (maximum) number of linearly independent rows or columns it contains. It is
     * also equal to the number of nonzero singular values of the matrix.
     *
     * @return The matrix' rank.
     */
    int getRank();

    /**
     * @param aFirst The first row to include.
     * @param aLimit The limit (exclusive) - the first row not to include.
     * @return A new matrix with only the specified range of rows
     */
    BasicMatrix getRowsRange(final int aFirst, final int aLimit);

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
     */
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
     */
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
     * Matrices are either square, tall, fat or empty. m &lt;= 0 or n &lt;= 0
     *
     * @return true if matrix is empty
     */
    boolean isEmpty();

    /**
     * Matrices are either square, tall, fat or empty. 1 &lt;= m &lt; n
     *
     * @return true if matrix is fat
     */
    boolean isFat();

    /**
     * @return true if {@linkplain #getRank()} == min({@linkplain #countRows()}, {@linkplain #countColumns()})
     */
    boolean isFullRank();

    boolean isHermitian();

    /**
     * @return true if this is a 1x1 matrix
     */
    boolean isScalar();

    /**
     * Matrices are either square, tall, fat or empty. m = n &lt;&gt; 0
     *
     * @return true if matrix is square
     */
    boolean isSquare();

    boolean isSymmetric();

    /**
     * Matrices are either square, tall, fat or empty. m &lt; n &gt;= 1
     *
     * @return true if matrix is tall
     */
    boolean isTall();

    /**
     * @return true if the row or column dimensions are equal to 1.
     */
    boolean isVector();

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
     * Matrix multiplication: [this][aMtrx] <br>
     * The column dimension of the left matrix must equal the row dimension of the right matrix.
     *
     * @param right The right matrix.
     * @return The product.
     * @see org.ojalgo.matrix.BasicMatrix#multiplyLeft(Access2D)
     */
    BasicMatrix multiply(Access2D<?> right);

    /**
     * Multiplies the elements of this matrix with the elements of aMtrx. The matrices must have equal
     * dimensions.
     *
     * @param aMtrx The elements to multiply by.
     * @return A new matrix whos elements are the elements of this multiplied with the elements of aMtrx.
     */
    BasicMatrix multiplyElements(Access2D<?> aMtrx);

    /**
     * Matrix multiplication: [aMtrx][this] <br>
     * The column dimension of the left matrix must equal the row dimension of the right matrix.
     *
     * @param aMtrx The left matrix.
     * @return The product.
     * @see org.ojalgo.matrix.BasicMatrix#multiply(Access2D)
     */
    BasicMatrix multiplyLeft(Access2D<?> aMtrx);

    /**
     * Assumes that both [this] and [aVctr] have row or column dimension, doesn't matter which, equal to 1.
     * The two vectors must have the same number of elements.
     */
    Scalar<?> multiplyVectors(Access2D<?> aVctr);

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
     * Subtracts the elements of aMtrx from the elements of this matrix. The matrices must have equal
     * dimensions.
     *
     * @param aMtrx What to subtract.
     * @return A new matrix whos elements are the difference of this' and aMtrx'.
     */
    BasicMatrix subtract(Access2D<?> aMtrx);

    /**
     * Subtracts value from the elements of this matrix.
     *
     * @param value What to subtract.
     * @return A new matrix whos elements are the differences between this' elements and value.
     */
    BasicMatrix subtract(Number value);

    /**
     * Extracts one element of this matrix as a BigDecimal.
     *
     * @param row A row index.
     * @param column A column index.
     * @return One matrix element
     */
    BigDecimal toBigDecimal(int row, int column);

    /**
     * Must be a copy that is safe to modify.
     *
     * @see org.ojalgo.matrix.BasicMatrix#toComplexStore()
     * @see org.ojalgo.matrix.BasicMatrix#toPrimitiveStore()
     */
    PhysicalStore<BigDecimal> toBigStore();

    /**
     * Extracts one element of this matrix as a ComplexNumber.
     *
     * @param row A row index.
     * @param column A column index.
     * @return One matrix element
     */
    ComplexNumber toComplexNumber(int row, int column);

    /**
     * Must be a copy that is safe to modify.
     *
     * @see org.ojalgo.matrix.BasicMatrix#toBigStore()
     * @see org.ojalgo.matrix.BasicMatrix#toPrimitiveStore()
     */
    PhysicalStore<ComplexNumber> toComplexStore();

    List<BasicMatrix> toListOfColumns();

    /**
     * It is also possible to call {@linkplain #toBigStore()}, {@linkplain #toComplexStore()} or
     * {@linkplain #toPrimitiveStore()} and then {@linkplain PhysicalStore#asList()}.
     */
    List<? extends Number> toListOfElements();

    List<BasicMatrix> toListOfRows();

    /**
     * Must be a copy that is safe to modify.
     *
     * @see org.ojalgo.matrix.BasicMatrix#toBigStore()
     * @see org.ojalgo.matrix.BasicMatrix#toComplexStore()
     */
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
