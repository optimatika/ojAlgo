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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.access.Access2D;
import org.ojalgo.access.Access2D.Collectable;
import org.ojalgo.access.Structure2D;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.task.DeterminantTask;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;

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
public interface MatrixDecomposition<N extends Number> {

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

        void setFullSize(boolean fullSize);

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
         * @return true if the hermitian check passed and computation suceeded; false if not
         */
        @SuppressWarnings("unchecked")
        default boolean checkAndCompute(final MatrixStore<N> matrix) {

            this.reset();

            if (MatrixUtils.isHermitian(matrix)) {
                return this instanceof Solver<?> ? ((Solver<N>) this).compute(matrix) : this.decompose(matrix);
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
         * The best (and most expensive) way to get the effective numerical rank is by calculating a
         * {@link SingularValue} decomposition and then find the number of nonnegligible singular values.
         *
         * @return The effective numerical rank (best estimate)
         */
        int getRank();

        /**
         * @return true if the rank is equal to the minimum of the row and column dimensions; false if not
         */
        boolean isFullRank();

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
         *
         * @see BasicMatrix#invert()
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
         * @see #isComputed()
         * @see #getSolution(Collectable)
         * @see #getInverse()
         */
        boolean isSolvable();

    }

    /**
     * Eigenvalue and Singular Value decompositions can calculate the "values" only, and the resulting
     * matrices and arrays can have their elements sorted (descending) or not.
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

    static final Structure2D TYPICAL = new Structure2D() {

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
