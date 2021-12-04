/*
 * Copyright 1997-2021 Optimatika
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
import java.util.Optional;

import org.ojalgo.ProgrammingError;
import org.ojalgo.algebra.NormedVectorSpace;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.Eigenvalue.Eigenpair;
import org.ojalgo.matrix.decomposition.LDL;
import org.ojalgo.matrix.decomposition.LDU;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.decomposition.MatrixDecomposition;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.matrix.task.DeterminantTask;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.context.NumberContext;

/**
 * A base class for, easy to use, immutable (thread safe) matrices with a rich feature set. This class handles
 * a lot of complexity, and makes choices, for you. If you want more control, and to be exposed to all the
 * implementation details, then look at the various interfaces/classes in the
 * {@linkplain org.ojalgo.matrix.store} and {@linkplain org.ojalgo.matrix.decomposition} packages.
 *
 * @author apete
 */
public abstract class BasicMatrix<N extends Comparable<N>, M extends BasicMatrix<N, M>>
        implements Matrix2D<N, M>, Access2D.Elements, Structure2D.ReducibleTo1D<M>, NumberContext.Enforceable<M>,
        Access2D.Collectable<N, TransformableRegion<N>>, Provider2D.Inverse<M>, Provider2D.Condition, Provider2D.Rank, Provider2D.Symmetric,
        Provider2D.Hermitian, Provider2D.Trace<N>, Provider2D.Determinant<N>, Provider2D.Solution<M>, Provider2D.Eigenpairs {

    private static final NumberContext EQUALS = NumberContext.of(12, 14);

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

        long tmpLimit = matrix.countRows();
        for (long i = 0L; i < tmpLimit; i++) {
            retVal = PrimitiveMath.MAX.invoke(retVal, NumberDefinition.doubleValue(matrix.aggregateRow(i, Aggregator.NORM1)));
        }

        return retVal;
    }

    /**
     * @return The 1-norm or maximum column sum
     */
    public static <M extends BasicMatrix<?, M>> double calculateOneNorm(final M matrix) {

        double retVal = PrimitiveMath.ZERO;

        long tmpLimit = matrix.countColumns();
        for (long j = 0L; j < tmpLimit; j++) {
            retVal = PrimitiveMath.MAX.invoke(retVal, NumberDefinition.doubleValue(matrix.aggregateColumn(j, Aggregator.NORM1)));
        }

        return retVal;
    }

    private transient MatrixDecomposition<N> myDecomposition = null;
    private transient int myHashCode = 0;
    private transient Boolean myHermitian = null;
    private transient Boolean mySPD = null;
    private final MatrixStore<N> myStore;
    private transient Boolean mySymmetric = null;

    BasicMatrix(final MatrixStore<N> store) {

        super();

        myStore = store;
    }

    public M add(final double scalarAddend) {
        return this.newInstance(myStore.add(scalarAddend));
    }

    public M add(final M addend) {
        ProgrammingError.throwIfNotEqualDimensions(myStore, addend);
        return this.newInstance(myStore.add(addend.getStore()));
    }

    public M add(final N scalarAddend) {
        return this.newInstance(myStore.add(scalarAddend));
    }

    public N aggregateColumn(final long row, final long col, final Aggregator aggregator) {
        return myStore.aggregateColumn(row, col, aggregator);
    }

    public N aggregateDiagonal(final long row, final long col, final Aggregator aggregator) {
        return myStore.aggregateDiagonal(row, col, aggregator);
    }

    public N aggregateRange(final long first, final long limit, final Aggregator aggregator) {
        return myStore.aggregateRange(first, limit, aggregator);
    }

    public N aggregateRow(final long row, final long col, final Aggregator aggregator) {
        return myStore.aggregateRow(row, col, aggregator);
    }

    public M conjugate() {
        return this.newInstance(myStore.conjugate());
    }

    /**
     * The returned instance can be have its elements mutated in various ways, while the size/shape is fixed.
     *
     * @return A fully mutable matrix builder with the elements initially set to a copy of this matrix –
     *         always creates a full dense copy.
     * @see #logical()
     */
    public abstract Mutator2D<N, M, PhysicalStore<N>> copy();

    public long count() {
        return myStore.count();
    }

    public long countColumns() {
        return myStore.countColumns();
    }

    public long countRows() {
        return myStore.countRows();
    }

    public M divide(final double scalarDivisor) {
        return this.newInstance(myStore.divide(scalarDivisor));
    }

    public M divide(final N scalarDivisor) {
        return this.newInstance(myStore.divide(scalarDivisor));
    }

    public double doubleValue(final long index) {
        return myStore.doubleValue(index);
    }

    public double doubleValue(final long row, final long col) {
        return myStore.doubleValue(row, col);
    }

    public M enforce(final NumberContext context) {

        PhysicalStore<N> tmpCopy = myStore.copy();

        tmpCopy.modifyAll(myStore.physical().function().enforce(context));

        return this.newInstance(tmpCopy);
    }

    /**
     * true if "other" is an {@link Access2D} of the same size/shape and the elements are equal to high
     * precision (12 significant digits).
     */
    @Override
    public boolean equals(final Object other) {
        if (other instanceof Access2D<?>) {
            return Access2D.equals(myStore, (Access2D<?>) other, EQUALS);
        }
        return super.equals(other);
    }

    /**
     * BasicMatrix instances are intended to be immutable. If they are it is possible to cache (partial)
     * calculation results. Calling this method should flush any cached calculation results.
     *
     * @deprecated v50 Caching, if necessary, is handled for you. If you want control of this then use the
     *             lower level stuff in org.ojagl.matrix.store and org.ojagl.matrix.decomposition instead.
     */
    @Deprecated
    public void flushCache() {

        myHashCode = 0;

        if (myDecomposition != null) {
            myDecomposition.reset();
            myDecomposition = null;
        }

        myHermitian = null;
        mySymmetric = null;
        mySPD = null;
    }

    public N get(final long index) {
        return myStore.get(index);
    }

    public N get(final long row, final long col) {
        return myStore.get(row, col);
    }

    /**
     * Matrix condition (2-norm)
     *
     * @return ratio of largest to smallest singular value.
     */
    public double getCondition() {
        return this.getConditionProvider().getCondition();
    }

    /**
     * @return The matrix' determinant.
     */
    public N getDeterminant() {
        return this.getDeterminantProvider().getDeterminant();
    }

    public List<Eigenpair> getEigenpairs() {

        if (!this.isSquare()) {
            throw new ProgrammingError("Only defined for square matrices!");
        }

        return this.getEigenpairsProvider().getEigenpairs();
    }

    /**
     * The rank of a matrix is the (maximum) number of linearly independent rows or columns it contains. It is
     * also equal to the number of nonzero singular values of the matrix.
     *
     * @return The matrix' rank.
     * @see org.ojalgo.matrix.decomposition.MatrixDecomposition.RankRevealing
     */
    public int getRank() {
        return this.getRankProvider().getRank();
    }

    /**
     * The sum of the diagonal elements.
     *
     * @return The matrix' trace.
     */
    public N getTrace() {
        return this.aggregateDiagonal(Aggregator.SUM);
    }

    @Override
    public int hashCode() {
        if (myHashCode == 0) {
            myHashCode = Access1D.hashCode(myStore);
        }
        return myHashCode;
    }

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
    public M invert() {
        return this.newInstance(this.getInverseProvider(false).invert().orElseGet(() -> this.getInverseProvider(true).invert().get()));
    }

    public boolean isAbsolute(final long row, final long col) {
        return myStore.isAbsolute(row, col);
    }

    /**
     * @return true if {@linkplain #getRank()} == min({@linkplain #countRows()}, {@linkplain #countColumns()})
     * @see org.ojalgo.matrix.decomposition.MatrixDecomposition.RankRevealing
     * @deprecated v50 Just use {@link #getRank()}
     */
    @Deprecated
    public boolean isFullRank() {
        return this.getRank() == this.getMinDim();
    }

    public boolean isHermitian() {
        if (myHermitian == null) {
            myHermitian = Boolean.valueOf(this.isSquare() && myStore.equals(myStore.conjugate(), EQUALS));
        }
        return myHermitian.booleanValue();
    }

    public boolean isSmall(final double comparedTo) {
        return myStore.isSmall(comparedTo);
    }

    public boolean isSmall(final long row, final long col, final double comparedTo) {
        return myStore.isSmall(row, col, comparedTo);
    }

    public boolean isSymmetric() {
        if (mySymmetric == null) {
            mySymmetric = Boolean.valueOf(this.isSquare() && myStore.equals(myStore.transpose(), EQUALS));
        }
        return mySymmetric.booleanValue();
    }

    /**
     * Compared to {@link #copy()} this does not create a copy – not initially anyway. The returned instance
     * is a starting point for logically composing a new matrix.
     *
     * @return A logical builder that tries to avoid unnecessary copying.
     * @see #copy()
     */
    public abstract Pipeline2D<N, M, ?> logical();

    public M multiply(final double scalarMultiplicand) {
        return this.newInstance(myStore.multiply(scalarMultiplicand));
    }

    public M multiply(final M multiplicand) {

        ProgrammingError.throwIfMultiplicationNotPossible(myStore, multiplicand);

        return this.newInstance(myStore.multiply(multiplicand.getStore()));
    }

    public M multiply(final N scalarMultiplicand) {
        return this.newInstance(myStore.multiply(scalarMultiplicand));
    }

    public M negate() {
        return this.newInstance(myStore.negate());
    }

    /**
     * The Frobenius norm is the square root of the sum of the squares of each element, or the square root of
     * the sum of the square of the singular values. This definition fits the requirements of
     * {@linkplain NormedVectorSpace#norm()}.
     *
     * @return The matrix' Frobenius norm
     */
    public double norm() {
        return myStore.norm();
    }

    public M power(final int power) {
        return this.newInstance(myStore.power(power));
    }

    public M reduceColumns(final Aggregator aggregator) {
        return this.newInstance(myStore.reduceColumns(aggregator).collect(myStore.physical()));
    }

    public M reduceRows(final Aggregator aggregator) {
        return this.newInstance(myStore.reduceRows(aggregator).collect(myStore.physical()));
    }

    public M signum() {
        return this.newInstance(myStore.signum());
    }

    /**
     * <p>
     * This method solves a system of linear equations: [this][X]=[rhs]. A combination of columns in [this]
     * should produce a column(s) in [rhs]. It is ok for [rhs] to have more than 1 column.
     * </p>
     * <ul>
     * <li>If the problem is over-qualified an approximate solution is returned.</li>
     * <li>If the problem is under-qualified one possible solution is returned.</li>
     * </ul>
     * <p>
     * Remember that: [X][this]=[rhs] is equivalent to [this]<sup>T</sup>[X]<sup>T</sup>=[rhs]<sup>T</sup>
     * </p>
     *
     * @param rhs The right hand side of the equation.
     * @return The solution, [X].
     */
    public M solve(final Access2D<?> rhs) {
        return this.newInstance(this.getSolutionProvider(false, rhs).solve(rhs).orElseGet(() -> this.getSolutionProvider(true, rhs).solve(rhs).get()));
    }

    public M subtract(final double scalarSubtrahend) {
        return this.newInstance(myStore.subtract(scalarSubtrahend));
    }

    public M subtract(final M subtrahend) {
        ProgrammingError.throwIfNotEqualDimensions(myStore, subtrahend);
        return this.newInstance(myStore.subtract(subtrahend.getStore()));
    }

    public M subtract(final N scalarSubtrahend) {
        return this.newInstance(myStore.subtract(scalarSubtrahend));
    }

    public void supplyTo(final TransformableRegion<N> receiver) {
        myStore.supplyTo(receiver);
    }

    /**
     * Extracts one element of this matrix as a Scalar.
     *
     * @param row A row index.
     * @param col A column index.
     * @return One matrix element
     * @deprecated v50 Use {@link #get(long, long)} instead
     */
    @Deprecated
    public Scalar<N> toScalar(final long row, final long col) {
        return myStore.toScalar(row, col);
    }

    @Override
    public String toString() {
        return Access2D.toString(this);
    }

    /**
     * Transposes this matrix. For complex matrices conjugate() and transpose() are NOT EQUAL.
     *
     * @return A matrix that is the transpose of this matrix.
     * @see org.ojalgo.matrix.BasicMatrix#conjugate()
     */
    public M transpose() {
        return this.newInstance(myStore.transpose());
    }

    private Provider2D.Condition getConditionProvider() {

        if (myDecomposition instanceof Provider2D.Condition) {
            return (Provider2D.Condition) myDecomposition;
        }

        SingularValue<N> provider = this.newSingularValue(myStore);
        provider.decompose(myStore);
        myDecomposition = provider;

        return provider;
    }

    private Provider2D.Determinant<N> getDeterminantProvider() {

        if (myDecomposition instanceof Provider2D.Determinant) {
            return (Provider2D.Determinant<N>) myDecomposition;
        }

        DeterminantTask<N> task = this.newDeterminantTask(myStore);

        if (task instanceof MatrixDecomposition) {
            myDecomposition = (MatrixDecomposition<N>) task;
        }

        return task.toDeterminantProvider(myStore);
    }

    private Provider2D.Eigenpairs getEigenpairsProvider() {

        if (myDecomposition instanceof Provider2D.Eigenpairs) {
            return (Provider2D.Eigenpairs) myDecomposition;
        }

        Eigenvalue<N> provider = this.newEigenvalue(myStore);
        provider.decompose(myStore);
        myDecomposition = provider;

        return provider;
    }

    private Provider2D.Inverse<Optional<MatrixStore<N>>> getInverseProvider(final boolean safe) {

        if (safe ? myDecomposition instanceof SingularValue<?> : myDecomposition instanceof Provider2D.Inverse) {
            return (Provider2D.Inverse<Optional<MatrixStore<N>>>) myDecomposition;
        }

        InverterTask<N> task = safe ? this.newSingularValue(myStore) : this.newInverterTask(myStore);

        if (task instanceof MatrixDecomposition) {
            myDecomposition = (MatrixDecomposition<N>) task;
        }

        return task.toInverseProvider(myStore);
    }

    private Provider2D.Rank getRankProvider() {

        if (!(myDecomposition instanceof Provider2D.Rank)) {

            if (myStore.isTall()) {
                myDecomposition = this.newQR(myStore);
            } else if (myStore.isFat()) {
                myDecomposition = this.newSingularValue(myStore);
            } else {
                myDecomposition = this.newLDU(myStore);
            }

            myDecomposition.decompose(myStore);
        }

        return (Provider2D.Rank) myDecomposition;
    }

    private Provider2D.Solution<Optional<MatrixStore<N>>> getSolutionProvider(final boolean safe, final Access2D<?> rhs) {

        if (safe ? myDecomposition instanceof SingularValue<?> : myDecomposition instanceof Provider2D.Inverse) {
            return (Provider2D.Solution<Optional<MatrixStore<N>>>) myDecomposition;
        }

        SolverTask<N> task = safe ? this.newSingularValue(myStore) : this.newSolverTask(myStore, rhs);

        if (task instanceof MatrixDecomposition) {
            myDecomposition = (MatrixDecomposition<N>) task;
        }

        return task.toSolutionProvider(myStore, rhs);
    }

    MatrixStore<N> getStore() {
        return myStore;
    }

    abstract Cholesky<N> newCholesky(Structure2D typical);

    abstract DeterminantTask<N> newDeterminantTask(MatrixStore<N> template);

    abstract Eigenvalue<N> newEigenvalue(Structure2D typical);

    abstract M newInstance(MatrixStore<N> store);

    abstract InverterTask<N> newInverterTask(Structure2D template);

    abstract LDL<N> newLDL(Structure2D typical);

    final LDU<N> newLDU(final Structure2D typical) {

        if (mySPD != null && mySPD.booleanValue()) {
            return this.newCholesky(typical);
        }

        if (myHermitian != null && myHermitian.booleanValue()) {
            return this.newLDL(typical);
        }

        return this.newLU(typical);
    }

    abstract LU<N> newLU(Structure2D typical);

    abstract QR<N> newQR(Structure2D typical);

    abstract SingularValue<N> newSingularValue(Structure2D typical);

    abstract SolverTask<N> newSolverTask(MatrixStore<N> templateBody, Access2D<?> templateRHS);

}
