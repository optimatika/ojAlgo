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
package org.ojalgo.matrix;

import java.util.List;
import java.util.Optional;

import org.ojalgo.ProgrammingError;
import org.ojalgo.algebra.NormedVectorSpace;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
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
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.matrix.task.DeterminantTask;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Operate2D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.structure.Transformation2D;
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
        implements Matrix2D<N, M>, Structure2D.ReducibleTo1D<M>, NumberContext.Enforceable<M>, Access2D.Collectable<N, TransformableRegion<N>>,
        Provider2D.Inverse<M>, Provider2D.Condition, Provider2D.Rank, Provider2D.Symmetric, Provider2D.Hermitian, Provider2D.Trace<N>,
        Provider2D.Determinant<N>, Provider2D.Solution<M>, Provider2D.Eigenpairs, Structure2D.Logical<Access2D<N>, M>, Operate2D<N, M> {

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
    private final PhysicalStore.Factory<N, ?> myFactory;
    private transient int myHashCode = 0;
    private transient Boolean myHermitian = null;
    private transient Boolean mySPD = null;
    private MatrixStore<N> myStore;
    private final ElementsSupplier<N> mySupplier;
    private transient Boolean mySymmetric = null;

    BasicMatrix(final PhysicalStore.Factory<N, ?> factory, final ElementsSupplier<N> supplier) {

        super();

        myFactory = factory;

        mySupplier = supplier;

        if (supplier instanceof MatrixStore<?>) {
            myStore = (MatrixStore<N>) supplier;
        } else {
            myStore = null;
        }
    }

    @Override
    public M above(final Access2D<N>... above) {
        return this.newInstance(this.store().above(above));
    }

    @Override
    public M above(final Access2D<N> above) {
        return this.newInstance(this.store().above(above));
    }

    @Override
    public M above(final long numberOfRows) {
        return this.newInstance(this.store().above(numberOfRows));
    }

    @Override
    public M add(final double scalarAddend) {
        return this.newInstance(this.store().add(scalarAddend));
    }

    @Override
    public M add(final M addend) {
        ProgrammingError.throwIfNotEqualDimensions(this.store(), addend);
        return this.newInstance(this.store().add(addend.store()));
    }

    @Override
    public M add(final N scalarAddend) {
        return this.newInstance(this.store().add(scalarAddend));
    }

    @Override
    public N aggregateColumn(final long row, final long col, final Aggregator aggregator) {
        return this.store().aggregateColumn(row, col, aggregator);
    }

    @Override
    public N aggregateDiagonal(final long row, final long col, final Aggregator aggregator) {
        return this.store().aggregateDiagonal(row, col, aggregator);
    }

    @Override
    public N aggregateRange(final long first, final long limit, final Aggregator aggregator) {
        return this.store().aggregateRange(first, limit, aggregator);
    }

    @Override
    public N aggregateRow(final long row, final long col, final Aggregator aggregator) {
        return this.store().aggregateRow(row, col, aggregator);
    }

    @Override
    public M below(final Access2D<N>... below) {
        return this.newInstance(this.store().below(below));
    }

    @Override
    public M below(final Access2D<N> below) {
        return this.newInstance(this.store().below(below));
    }

    @Override
    public M below(final long numberOfRows) {
        return this.newInstance(this.store().below(numberOfRows));
    }

    @Override
    public M bidiagonal(final boolean upper) {
        return this.newInstance(this.store().bidiagonal(upper));
    }

    @Override
    public M column(final int column) {
        return this.newInstance(this.store().column(column));
    }

    @Override
    public M column(final long column) {
        return this.newInstance(this.store().column(column));
    }

    @Override
    public M columns(final int... columns) {
        return this.newInstance(this.store().columns(columns));
    }

    @Override
    public M columns(final long... columns) {
        return this.newInstance(this.store().columns(columns));
    }

    @Override
    public M conjugate() {
        return this.newInstance(this.store().conjugate());
    }

    /**
     * The returned instance can have its elements mutated in various ways, while the size/shape is fixed.
     *
     * @return A fully mutable matrix builder with the elements initially set to a copy of this matrix –
     *         always creates a full dense copy.
     * @see #logical()
     */
    public abstract Mutator2D<N, M, PhysicalStore<N>> copy();

    @Override
    public long count() {
        return mySupplier.count();
    }

    @Override
    public long countColumns() {
        return mySupplier.countColumns();
    }

    @Override
    public long countRows() {
        return mySupplier.countRows();
    }

    @Override
    public M diagonal() {
        return this.newInstance(this.store().diagonal());
    }

    @Override
    public M diagonally(final Access2D<N>... diagonally) {
        return this.newInstance(this.store().diagonally(diagonally));
    }

    @Override
    public M divide(final double scalarDivisor) {
        return this.newInstance(this.store().divide(scalarDivisor));
    }

    @Override
    public M divide(final N scalarDivisor) {
        return this.newInstance(this.store().divide(scalarDivisor));
    }

    @Override
    public double doubleValue(final long index) {
        return this.store().doubleValue(index);
    }

    @Override
    public double doubleValue(final int row, final int col) {
        return this.store().doubleValue(row, col);
    }

    @Override
    public M enforce(final NumberContext context) {

        PhysicalStore<N> tmpCopy = this.store().copy();

        tmpCopy.modifyAll(this.store().physical().function().enforce(context));

        return this.newInstance(tmpCopy);
    }

    /**
     * true if "other" is an {@link Access2D} of the same size/shape and the elements are equal to high
     * precision (12 significant digits).
     */
    @Override
    public boolean equals(final Object other) {
        if (other instanceof Access2D<?>) {
            return Access2D.equals(this.store(), (Access2D<?>) other, EQUALS);
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

    /**
     * @deprecated v50 No need for this!
     */
    @Deprecated
    public M get() {
        return (M) this;
    }

    @Override
    public N get(final long index) {
        return this.store().get(index);
    }

    @Override
    public N get(final long row, final long col) {
        return this.store().get(row, col);
    }

    /**
     * Matrix condition (2-norm)
     *
     * @return ratio of largest to smallest singular value.
     */
    @Override
    public double getCondition() {
        return this.getConditionProvider().getCondition();
    }

    /**
     * @return The matrix' determinant.
     */
    @Override
    public N getDeterminant() {
        return this.getDeterminantProvider().getDeterminant();
    }

    @Override
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
    @Override
    public int getRank() {
        return this.getRankProvider().getRank();
    }

    /**
     * The sum of the diagonal elements.
     *
     * @return The matrix' trace.
     */
    @Override
    public N getTrace() {
        return this.aggregateDiagonal(Aggregator.SUM);
    }

    @Override
    public int hashCode() {
        if (myHashCode == 0) {
            final Access1D<?> access = this.store();
            int limit = access.size();
            int retVal = limit + 31;
            for (int ij = 0; ij < limit; ij++) {
                retVal *= access.intValue(ij);
            }
            myHashCode = retVal;
        }
        return myHashCode;
    }

    @Override
    public M hermitian(final boolean upper) {
        return this.newInstance(this.store().hermitian(upper));
    }

    @Override
    public M hessenberg(final boolean upper) {
        return this.newInstance(this.store().hessenberg(upper));
    }

    @Override
    public long indexOfLargest() {
        return this.store().indexOfLargest();
    }

    /**
     * <p>
     * About inverting matrices:
     * </p>
     * <ul>
     * <li>"right inverse": [this][right inverse]=[I]. You may calculate it using
     * {@linkplain #solve(Access2D)}.
     * <li>"left inverse": [left inverse][this]=[I]. You may calculate it using {@linkplain #solve(Access2D)}
     * and transposing.
     * <li>"generalised inverse": [this][generalised inverse][this]=[this]. Note that if [this] is singular or
     * non-square, then [generalised inverse] is not unique.
     * <li>"pseudoinverse": The generalised inverse (there are typically/possibly many) with the smallest
     * frobenius norm is called the (Moore-Penrose) pseudoinverse. You may calculate it using the
     * {@linkplain QR} or {@linkplain SingularValue} decompositions.
     * <li>"inverse":
     * <ul>
     * <li>If [left inverse]=[right inverse] then it is also [inverse].
     * <li>If [this] is square and has full rank then the [generalised inverse] is unique, with the
     * [pseudoinverse] given, and equal to [inverse].
     * </ul>
     * </ul>
     *
     * @return The "best possible" inverse....
     */
    @Override
    public M invert() {
        return this.newInstance(this.getInverseProvider(false).invert().orElseGet(() -> this.getInverseProvider(true).invert().get()));
    }

    @Override
    public boolean isHermitian() {
        if (myHermitian == null) {
            myHermitian = Boolean.valueOf(this.isSquare() && this.store().equals(this.store().conjugate(), EQUALS));
        }
        return myHermitian.booleanValue();
    }

    @Override
    public boolean isSmall(final double comparedTo) {
        return this.store().isSmall(comparedTo);
    }

    @Override
    public boolean isSymmetric() {
        if (mySymmetric == null) {
            mySymmetric = Boolean.valueOf(this.isSquare() && this.store().equals(this.store().transpose(), EQUALS));
        }
        return mySymmetric.booleanValue();
    }

    @Override
    public M left(final Access2D<N>... left) {
        return this.newInstance(this.store().left(left));
    }

    @Override
    public M left(final Access2D<N> left) {
        return this.newInstance(this.store().left(left));
    }

    @Override
    public M left(final long numberOfColumns) {
        return this.newInstance(this.store().left(numberOfColumns));
    }

    @Override
    public M limits(final long rowLimit, final long columnLimit) {
        return this.newInstance(this.store().limits(rowLimit, columnLimit));
    }

    /**
     * Compared to {@link #copy()} this does not create a copy – not initially anyway. The returned instance
     * is a starting point for logically composing a new matrix.
     *
     * @return A logical builder that tries to avoid unnecessary copying.
     * @see #copy()
     * @deprecated v50 No need for this!
     */
    @Deprecated
    public final M logical() {
        return (M) this;
    }

    @Override
    public M multiply(final double scalarMultiplicand) {
        return this.newInstance(this.store().multiply(scalarMultiplicand));
    }

    @Override
    public M multiply(final M multiplicand) {

        ProgrammingError.throwIfMultiplicationNotPossible(this.store(), multiplicand);

        return this.newInstance(this.store().multiply(multiplicand.store()));
    }

    @Override
    public M multiply(final N scalarMultiplicand) {
        return this.newInstance(this.store().multiply(scalarMultiplicand));
    }

    @Override
    public M negate() {
        return this.newInstance(this.store().negate());
    }

    /**
     * The Frobenius norm is the square root of the sum of the squares of each element, or the square root of
     * the sum of the square of the singular values. This definition fits the requirements of
     * {@linkplain NormedVectorSpace#norm()}.
     *
     * @return The matrix' Frobenius norm
     */
    @Override
    public double norm() {
        return this.store().norm();
    }

    @Override
    public M offsets(final long rowOffset, final long columnOffset) {
        return this.newInstance(this.store().offsets(rowOffset, columnOffset));
    }

    @Override
    public M onAll(final UnaryFunction<N> operator) {
        return this.newInstance(this.supplier().onAll(operator));
    }

    @Override
    public M onAny(final Transformation2D<N> operator) {
        return this.newInstance(this.supplier().onAny(operator));
    }

    @Override
    public M onColumns(final BinaryFunction<N> operator, final Access1D<N> right) {
        return this.newInstance(this.supplier().onColumns(operator, right));
    }

    @Override
    public M onMatching(final Access2D<N> left, final BinaryFunction<N> operator) {
        return this.newInstance(this.supplier().onMatching(left, operator));
    }

    @Override
    public M onMatching(final BinaryFunction<N> operator, final Access2D<N> right) {
        return this.newInstance(this.supplier().onMatching(operator, right));
    }

    @Override
    public M onRows(final BinaryFunction<N> operator, final Access1D<N> right) {
        return this.newInstance(this.supplier().onRows(operator, right));
    }

    @Override
    public M power(final int power) {
        return this.newInstance(this.store().power(power));
    }

    @Override
    public M reduceColumns(final Aggregator aggregator) {
        return this.newInstance(this.store().reduceColumns(aggregator).collect(this.store().physical()));
    }

    @Override
    public M reduceRows(final Aggregator aggregator) {
        return this.newInstance(this.store().reduceRows(aggregator).collect(this.store().physical()));
    }

    @Override
    public M repeat(final int rowsRepetitions, final int columnsRepetitions) {
        return this.newInstance(this.store().repeat(rowsRepetitions, columnsRepetitions));
    }

    @Override
    public M right(final Access2D<N>... right) {
        return this.newInstance(this.store().right(right));
    }

    @Override
    public M right(final Access2D<N> right) {
        return this.newInstance(this.store().right(right));
    }

    @Override
    public M right(final long numberOfColumns) {
        return this.newInstance(this.store().right(numberOfColumns));
    }

    @Override
    public M row(final int row) {
        return this.newInstance(this.store().row(row));
    }

    @Override
    public M row(final long row) {
        return this.newInstance(this.store().row(row));
    }

    @Override
    public M rows(final int... rows) {
        return this.newInstance(this.store().rows(rows));
    }

    @Override
    public M rows(final long... rows) {
        return this.newInstance(this.store().rows(rows));
    }

    @Override
    public M select(final int[] rows, final int[] columns) {
        return this.newInstance(this.store().select(rows, columns));
    }

    @Override
    public M select(final long[] rows, final long[] columns) {
        return this.newInstance(this.store().select(rows, columns));
    }

    @Override
    public M signum() {
        return this.newInstance(this.store().signum());
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
    @Override
    public M solve(final Access2D<?> rhs) {
        return this.newInstance(this.getSolutionProvider(false, rhs).solve(rhs).orElseGet(() -> this.getSolutionProvider(true, rhs).solve(rhs).get()));
    }

    @Override
    public M subtract(final double scalarSubtrahend) {
        return this.newInstance(this.store().subtract(scalarSubtrahend));
    }

    @Override
    public M subtract(final M subtrahend) {
        ProgrammingError.throwIfNotEqualDimensions(this.store(), subtrahend);
        return this.newInstance(this.store().subtract(subtrahend.store()));
    }

    @Override
    public M subtract(final N scalarSubtrahend) {
        return this.newInstance(this.store().subtract(scalarSubtrahend));
    }

    @Override
    public M superimpose(final long row, final long col, final Access2D<N> matrix) {
        return this.newInstance(this.store().superimpose(row, col, matrix));
    }

    @Override
    public void supplyTo(final TransformableRegion<N> receiver) {
        this.supplier().supplyTo(receiver);
    }

    @Override
    public M symmetric(final boolean upper) {
        return this.newInstance(this.store().symmetric(upper));
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
        return this.store().toScalar(row, col);
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
    @Override
    public M transpose() {
        return this.newInstance(this.supplier().transpose());
    }

    @Override
    public M triangular(final boolean upper, final boolean assumeOne) {
        return this.newInstance(this.store().triangular(upper, assumeOne));
    }

    @Override
    public M tridiagonal() {
        return this.newInstance(this.store().tridiagonal());
    }

    private Provider2D.Condition getConditionProvider() {

        if (myDecomposition instanceof Provider2D.Condition) {
            return (Provider2D.Condition) myDecomposition;
        }

        SingularValue<N> provider = this.newSingularValue(this.supplier());
        provider.decompose(this.supplier());
        myDecomposition = provider;

        return provider;
    }

    private Provider2D.Determinant<N> getDeterminantProvider() {

        if (myDecomposition instanceof Provider2D.Determinant) {
            return (Provider2D.Determinant<N>) myDecomposition;
        }

        DeterminantTask<N> task = this.newDeterminantTask(this.supplier());

        if (task instanceof MatrixDecomposition) {
            myDecomposition = (MatrixDecomposition<N>) task;
        }

        return task.toDeterminantProvider(this.supplier(), this::store);
    }

    private Provider2D.Eigenpairs getEigenpairsProvider() {

        if (myDecomposition instanceof Provider2D.Eigenpairs) {
            return (Provider2D.Eigenpairs) myDecomposition;
        }

        Eigenvalue<N> provider = this.newEigenvalue(this.supplier());
        provider.decompose(this.supplier());
        myDecomposition = provider;

        return provider;
    }

    private Provider2D.Inverse<Optional<MatrixStore<N>>> getInverseProvider(final boolean safe) {

        if (safe ? myDecomposition instanceof SingularValue<?> : myDecomposition instanceof Provider2D.Inverse) {
            return (Provider2D.Inverse<Optional<MatrixStore<N>>>) myDecomposition;
        }

        InverterTask<N> task = safe ? this.newSingularValue(this.supplier()) : this.newInverterTask(this.supplier());

        if (task instanceof MatrixDecomposition) {
            myDecomposition = (MatrixDecomposition<N>) task;
        }

        return task.toInverseProvider(this.supplier(), this::store);
    }

    private Provider2D.Rank getRankProvider() {

        if (!(myDecomposition instanceof Provider2D.Rank)) {

            if (this.store().isTall()) {
                myDecomposition = this.newQR(this.supplier());
            } else if (this.store().isFat()) {
                myDecomposition = this.newSingularValue(this.supplier());
            } else {
                myDecomposition = this.newLDU(this.supplier());
            }

            myDecomposition.decompose(this.supplier());
        }

        return (Provider2D.Rank) myDecomposition;
    }

    private Provider2D.Solution<Optional<MatrixStore<N>>> getSolutionProvider(final boolean safe, final Access2D<?> rhs) {

        if (safe ? myDecomposition instanceof SingularValue<?> : myDecomposition instanceof Provider2D.Inverse) {
            return (Provider2D.Solution<Optional<MatrixStore<N>>>) myDecomposition;
        }

        SolverTask<N> task = safe ? this.newSingularValue(this.supplier()) : this.newSolverTask(this.supplier(), rhs);

        if (task instanceof MatrixDecomposition) {
            myDecomposition = (MatrixDecomposition<N>) task;
        }

        return task.toSolutionProvider(this.supplier(), this::store, rhs);
    }

    abstract Cholesky<N> newCholesky(Structure2D typical);

    abstract DeterminantTask<N> newDeterminantTask(Structure2D template);

    abstract Eigenvalue<N> newEigenvalue(Structure2D typical);

    abstract M newInstance(ElementsSupplier<N> store);

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

    abstract SolverTask<N> newSolverTask(Structure2D templateBody, Structure2D templateRHS);

    MatrixStore<N> store() {

        if (myStore == null) {
            myStore = mySupplier.collect(myFactory);
        }

        return myStore;
    }

    ElementsSupplier<N> supplier() {

        if (myStore != null) {
            return myStore;
        }

        return mySupplier;
    }

}
