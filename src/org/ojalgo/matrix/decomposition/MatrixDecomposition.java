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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.task.DeterminantTask;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.type.context.NumberContext;

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
        default boolean checkAndCompute(final MatrixStore<N> matrix) {

            this.reset();

            if (MatrixUtils.isHermitian(matrix)) {
                return this instanceof Solver<?> ? ((Solver<N>) this).compute(matrix) : this.decompose(matrix);
            } else {
                return false;
            }
        }
    }

    interface Solver<N extends Number> extends MatrixDecomposition<N>, SolverTask<N>, InverterTask<N> {

        /**
         * @param matrix A matrix to decompose
         * @return true if the decomposition suceeded AND {@link #isSolvable()}; false if not
         */
        default boolean compute(final ElementsSupplier<N> matrix) {
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
        MatrixStore<N> getInverse(DecompositionStore<N> preallocated);

        /**
         * @return true if it is ok to call {@linkplain #solve(ElementsSupplier)} (computation was
         *         successful); false if not
         * @see #solve(ElementsSupplier)
         * @see #isComputed()
         */
        boolean isSolvable();

        /**
         * [A][X]=[B] or [this][return]=[rhs]
         */
        MatrixStore<N> solve(ElementsSupplier<N> rhs);

        /**
         * <p>
         * Implementiong this method is optional.
         * </p>
         * <p>
         * Exactly how a specific implementation makes use of <code>preallocated</code> is not specified by
         * this interface. It must be documented for each implementation.
         * </p>
         * <p>
         * Should produce the same results as calling {@link #solve(ElementsSupplier)}.
         * </p>
         *
         * @param rhs The Right Hand Side, wont be modfied
         * @param preallocated Preallocated memory for the results, possibly some intermediate results. You
         *        must assume this is modified, but you cannot assume it will contain the full/final/correct
         *        solution.
         * @return The solution
         * @throws UnsupportedOperationException When/if this feature is not implemented
         */
        MatrixStore<N> solve(ElementsSupplier<N> rhs, DecompositionStore<N> preallocated);

        /**
         * To differentiate between {@link SolverTask#solve(Access2D, Access2D)} and
         * {@link #solve(ElementsSupplier, DecompositionStore)} when the RHS is a {@link MatrixStore}.
         */
        default MatrixStore<N> solve(final MatrixStore<N> rhs, final DecompositionStore<N> preallocated) {
            return this.solve((ElementsSupplier<N>) rhs, preallocated);
        }

    }

    /**
     * Eigenvalue:s and Singular Value:s decompositions can calculate the "values" only.
     *
     * @author apete
     */
    interface Values<N extends Number> extends MatrixDecomposition<N> {

        boolean computeValuesOnly(ElementsSupplier<N> matrix);

        /**
         * The eigenvalues in D (and the eigenvectors in V) are not necessarily ordered. This is a property of
         * the algorithm/implementation, not the data.
         *
         * @return true if they are ordered
         */
        boolean isOrdered();

    }

    /**
     * @param matrix A matrix to decompose
     * @return true if the computation suceeded; false if not
     */
    boolean decompose(ElementsSupplier<N> matrix);

    boolean equals(MatrixStore<N> other, NumberContext context);

    /**
     * @return true if computation has been attemped; false if not.
     * @see #decompose(ElementsSupplier)
     */
    boolean isComputed();

    /**
     * @deprecated v39 Use {@link MatrixUtils} instead
     */
    @Deprecated
    MatrixStore<N> reconstruct();

    /**
     * Delete computed results, and resets attributes to default values
     */
    void reset();

}
