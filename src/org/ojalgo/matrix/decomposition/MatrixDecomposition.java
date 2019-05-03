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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.function.special.MissingMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.task.DeterminantTask;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.structure.Structure2D;

/**
 * Notation used to describe the various matrix decompositions:
 * <ul>
 * <li>[A] could be any matrix. (The matrix to decompose.)</li>
 * <li>[A]<sup>-1</sup> is the inverse of [A].</li>
 * <li>[A]<sup>T</sup> is the transpose of [A].</li>
 * <li>[A]<sup>H</sup> is the conjugate transpose of [A]. [A]<sup>H</sup> is equilvalent to [A]<sup>T</sup> if
 * the elements are all real.</li>
 * <li>[D] is a diagonal matrix. Possibly bi-, tri- or block-diagonal.</li>
 * <li>[H] is an, upper or lower, Hessenberg matrix.</li>
 * <li>[I] is an identity matrix - obvioulsly orthogonal/unitary.</li>
 * <li>[L] is a lower (left) triangular matrix.</li>
 * <li>[P] is a permutation matrix - an identity matrix with interchanged rows or columns - and
 * orthogonal/unitary.</li>
 * <li>[Q] is an orthogonal/unitary matrix. [Q]<sup>-1</sup> = [Q]<sup>H</sup>, and with real matrices = [Q]
 * <sup>T</sup>.</li>
 * <li>[R] is a right (upper) tringular matrix. It is equivalent to [U].</li>
 * <li>[U] is an upper (right) triangular matrix. It is equivalent to [R].</li>
 * <li>[V] is an eigenvector matrix. The columns are the eigenvectors</li>
 * </ul>
 *
 * @author apete
 */
public interface MatrixDecomposition<N extends Number> extends Structure2D {

    interface Determinant<N extends Number> extends MatrixDecomposition<N>, DeterminantTask<N> {

        /**
         * <p>
         * A matrix' determinant is the product of its eigenvalues.
         * </p>
         *
         * @return The matrix' determinant
         */
        N getDeterminant();

    }

    /**
     * Several matrix decompositions can be expressed "economy sized" - some rows or columns of the decomposed
     * matrix parts are not needed for the most releveant use cases, and can therefore be left out. By default
     * these matrix decompositions should be "economy sized". Setting {@link #setFullSize(boolean)} to
     * <code>true</code> should switch to "full sized".
     *
     * @author apete
     */
    interface EconomySize<N extends Number> extends MatrixDecomposition<N> {

        /**
         * @return True if it will generate a full sized decomposition.
         */
        boolean isFullSize();

    }

    interface Factory<D extends MatrixDecomposition<?>> {

        default D make() {
            return this.make(TYPICAL);
        }

        default D make(final int numberOfRows, final int numberOfColumns) {
            return this.make(new Structure2D() {

                public long countColumns() {
                    return numberOfColumns;
                }

                public long countRows() {
                    return numberOfRows;
                }
            });
        }

        D make(Structure2D typical);

    }

    /**
     * Some matrix decompositions are only available with hermitian (symmetric) matrices or different
     * decomposition algorithms could be used depending on if the matrix is hemitian or not.
     *
     * @author apete
     */
    public interface Hermitian<N extends Number> extends MatrixDecomposition<N> {

        /**
         * Absolutely must check if the matrix is hermitian or not. Then, depending on the result differents
         * paths can be chosen - compute or not / choose different algorithms...
         *
         * @param matrix A matrix to check and then (maybe) decompose
         * @return true if the hermitian check passed and decomposition suceeded; false if not
         * @deprecated v48 Use {@link #checkAndDecompose(MatrixStore<N>)} instead
         */
        @Deprecated
        default boolean checkAndCompute(final MatrixStore<N> matrix) {
            return this.checkAndDecompose(matrix);
        }

        /**
         * Absolutely must check if the matrix is hermitian or not. Then, depending on the result differents
         * paths can be chosen - compute or not / choose different algorithms...
         *
         * @param matrix A matrix to check and then (maybe) decompose
         * @return true if the hermitian check passed and decomposition suceeded; false if not
         */
        default boolean checkAndDecompose(final MatrixStore<N> matrix) {

            this.reset();

            if (matrix.isHermitian()) {
                return this.decompose(matrix);
            } else {
                return false;
            }
        }
    }

    interface Ordered<N extends Number> extends MatrixDecomposition<N> {

        /**
         * This is a property of the algorithm/implementation, not the data. Typically relevant for
         * {@link SingularValue}, {@link Eigenvalue} or any {@link RankRevealing} decomposition.
         *
         * @return true if the rows/columns of the returned matrix factors are guaranteed some specific order;
         *         false if there is no such guarantee.
         */
        boolean isOrdered();

    }

    /**
     * <p>
     * The pivot or pivot element is the element of a matrix, or an array, which is selected first by an
     * algorithm (e.g. Gaussian elimination, simplex algorithm, etc.), to do certain calculations. In the case
     * of matrix algorithms, a pivot entry is usually required to be at least distinct from zero, and often
     * distant from it; in this case finding this element is called pivoting. Pivoting may be followed by an
     * interchange of rows or columns to bring the pivot to a fixed position and allow the algorithm to
     * proceed successfully, and possibly to reduce round-off error. It is often used for verifying row
     * echelon form.
     * </p>
     * <p>
     * Pivoting might be thought of as swapping or sorting rows or columns in a matrix, and thus it can be
     * represented as multiplication by permutation matrices. However, algorithms rarely move the matrix
     * elements because this would cost too much time; instead, they just keep track of the permutations.
     * </p>
     * <p>
     * Overall, pivoting adds more operations to the computational cost of an algorithm. These additional
     * operations are sometimes necessary for the algorithm to work at all. Other times these additional
     * operations are worthwhile because they add numerical stability to the final result.
     * </p>
     *
     * @author apete
     */
    interface Pivoting<N extends Number> extends MatrixDecomposition<N> {

        /**
         * The normal {@link #decompose(Access2D.Collectable)} method must handle cases where pivoting is
         * necessary. If you know that pivoting is not needed you may call this method instead - it may be
         * faster. Implementing this method, to actually decompose without pivoting, is optional. The default
         * implementation simply calls {@link #decompose(Access2D.Collectable)}.
         */
        default boolean decomposeWithoutPivoting(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix) {
            return this.decompose(matrix);
        }

        /**
         * @return The pivot (row and/or columnn) order
         */
        int[] getPivotOrder();

        /**
         * @return true if any pivoting was actually done
         */
        boolean isPivoted();

    }

    /**
     * A rank-revealing matrix decomposition of a matrix [A] is a decomposition that is, or can be transformed
     * to be, on the form [A]=[X][D][Y]<sup>T</sup> where:
     * <ul>
     * <li>[X] and [Y] are square and well conditioned.</li>
     * <li>[D] is diagonal with nonnegative and non-increasing values on the diagonal.</li>
     * </ul>
     * <p>
     * The defintion that [X] and [Y] should be well conditioned is subject to interpretation. A specific
     * decomposition algorithm can be more or less good at revealing the rank. Typically the
     * {@link SingularValue} decomposition is the best.
     * </p>
     * <p>
     * The requirement to have the diagonal elements of [D] ordered can be very practical, but is not always
     * strictly necessary in order to just reveal the rank. The method {@link #isOrdered()} indicates if the
     * elements (rows and columns) of the returned matrix factors actually are ordered or not for this
     * particular implementation.
     * </p>
     */
    interface RankRevealing<N extends Number> extends Ordered<N> {

        /**
         * @param threshold Significance limit
         * @return The number of elements in the diagonal matrix that are greater than the threshold
         */
        int countSignificant(double threshold);

        /**
         * The best (and most expensive) way to get the effective numerical rank is by calculating a
         * {@link SingularValue} decomposition and then find the number of nonnegligible singular values.
         *
         * @return The effective numerical rank (best estimate)
         */
        default int getRank() {
            return this.countSignificant(this.getRankThreshold());
        }

        double getRankThreshold();

        /**
         * @return true if the rank is equal to the minimum of the row and column dimensions; false if not
         */
        default boolean isFullRank() {
            return this.getRank() == MissingMath.toMinIntExact(this.countRows(), this.countColumns());
        }

    }

    interface Solver<N extends Number> extends MatrixDecomposition<N>, SolverTask<N>, InverterTask<N> {

        /**
         * @param matrix A matrix to decompose
         * @return true if the decomposition suceeded AND {@link #isSolvable()}; false if not
         */
        default boolean compute(final Collectable<N, ? super PhysicalStore<N>> matrix) {
            return this.decompose(matrix) && this.isSolvable();
        }

        /**
         * The output must be a "right inverse" and a "generalised inverse".
         */
        MatrixStore<N> getInverse();

        /**
         * <p>
         * Implementiong this method is optional.
         * </p>
         * <p>
         * Exactly how a specific implementation makes use of <code>preallocated</code> is not specified by
         * this interface. It must be documented for each implementation.
         * </p>
         * <p>
         * Should produce the same results as calling {@link #getInverse()}.
         * </p>
         *
         * @param preallocated Preallocated memory for the results, possibly some intermediate results. You
         *        must assume this is modified, but you cannot assume it will contain the full/final/correct
         *        solution.
         * @return The inverse, this is where you get the solution
         * @throws UnsupportedOperationException When/if this feature is not implemented
         */
        MatrixStore<N> getInverse(PhysicalStore<N> preallocated);

        /**
         * [A][X]=[B] or [this][return]=[rhs]
         */
        MatrixStore<N> getSolution(Collectable<N, ? super PhysicalStore<N>> rhs);

        /**
         * <p>
         * Implementiong this method is optional.
         * </p>
         * <p>
         * Exactly how a specific implementation makes use of <code>preallocated</code> is not specified by
         * this interface. It must be documented for each implementation.
         * </p>
         * <p>
         * Should produce the same results as calling {@link #getSolution(Collectable)}.
         * </p>
         *
         * @param rhs The Right Hand Side, wont be modfied
         * @param preallocated Preallocated memory for the results, possibly some intermediate results. You
         *        must assume this is modified, but you cannot assume it will contain the full/final/correct
         *        solution.
         * @return The solution
         * @throws UnsupportedOperationException When/if this feature is not implemented
         */
        MatrixStore<N> getSolution(Collectable<N, ? super PhysicalStore<N>> rhs, PhysicalStore<N> preallocated);

        /**
         * Please note that producing a pseudoinverse and/or a least squares solution is ok! The return value,
         * of this method, is not an indication of if the decomposed matrix is square, has full rank, is
         * postive definite or whatever. It's that in combination with the specific decomposition algorithm's
         * capabilities.
         *
         * @return true if this matrix decomposition is in a state to be able to deliver an inverse or an
         *         equation system solution (with some degree of numerical stability).
         */
        boolean isSolvable();

    }

    /**
     * Eigenvalue and Singular Value decompositions can calculate the "values" only.
     *
     * @author apete
     */
    interface Values<N extends Number> extends Ordered<N> {

        /**
         * @param matrix The matrix to decompose
         * @return The same as {@link Solver#compute(Collectable)} or {@link #decompose(Collectable)} if the
         *         instance does not implement {@link Solver}.
         */
        boolean computeValuesOnly(Access2D.Collectable<N, ? super PhysicalStore<N>> matrix);

    }

    Structure2D TYPICAL = new Structure2D() {

        public long countColumns() {
            return 50L;
        }

        public long countRows() {
            return 50L;
        }

    };

    /**
     * @param matrix A matrix to decompose
     * @return true if the computation suceeded; false if not
     */
    boolean decompose(Access2D.Collectable<N, ? super PhysicalStore<N>> matrix);

    /**
     * @return true if computation has been attemped; false if not.
     * @see #decompose(Access2D.Collectable)
     */
    boolean isComputed();

    MatrixStore<N> reconstruct();

    /**
     * Delete computed results, and resets attributes to default values
     */
    void reset();

}
