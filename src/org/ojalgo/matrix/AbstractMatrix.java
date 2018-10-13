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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import org.ojalgo.ProgrammingError;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.algebra.NormedVectorSpace;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.Eigenvalue.Eigenpair;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.decomposition.MatrixDecomposition;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.task.DeterminantTask;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.context.NumberContext;

public abstract class AbstractMatrix<N extends Number, M extends AbstractMatrix<N, M>> extends Object
        implements BasicMatrix<N, M>, Access2D.Collectable<N, PhysicalStore<N>> {

    @SuppressWarnings("unchecked")
    public static interface LogicalBuilder<N extends Number, I extends BasicMatrix<N, I>>
            extends Structure2D.Logical<I, AbstractMatrix.LogicalBuilder<N, I>>, Access2D.Collectable<N, PhysicalStore<N>> {

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

    @SuppressWarnings("unchecked")
    static final class Logical<N extends Number, I extends AbstractMatrix<N, I>> implements AbstractMatrix.LogicalBuilder<N, I> {

        private final MatrixStore.LogicalBuilder<N> myDelegate;
        private final AbstractMatrix<N, I> myOrigin;

        Logical(AbstractMatrix<N, I> matrix) {
            super();
            myOrigin = matrix;
            myDelegate = matrix.getStore().logical();
        }

        public AbstractMatrix.LogicalBuilder<N, I> above(I... above) {
            myDelegate.above(this.cast(above));
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> above(int numberOfRows) {
            myDelegate.above(numberOfRows);
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> above(N... elements) {
            myDelegate.above(elements);
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> below(I... below) {
            myDelegate.below(this.cast(below));
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> below(int numberOfRows) {
            myDelegate.below(numberOfRows);
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> below(N... elements) {
            myDelegate.below(elements);
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> bidiagonal(boolean upper, boolean assumeOne) {
            myDelegate.bidiagonal(upper, assumeOne);
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> column(int... columns) {
            myDelegate.column(columns);
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> conjugate() {
            myDelegate.conjugate();
            return this;
        }

        public long countColumns() {
            return myDelegate.countColumns();
        }

        public long countRows() {
            return myDelegate.countRows();
        }

        public AbstractMatrix.LogicalBuilder<N, I> diagonal() {
            myDelegate.diagonal();
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> diagonally(I... diagonally) {
            myDelegate.diagonally(this.cast(diagonally));
            return this;
        }

        public I get() {
            return myOrigin.getFactory().instantiate(myDelegate.get());
        }

        public AbstractMatrix.LogicalBuilder<N, I> hermitian(boolean upper) {
            myDelegate.hermitian(upper);
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> hessenberg(boolean upper) {
            myDelegate.hessenberg(upper);
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> left(I... left) {
            myDelegate.left(this.cast(left));
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> left(int numberOfColumns) {
            myDelegate.left(numberOfColumns);
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> left(N... elements) {
            myDelegate.left(elements);
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> limits(int rowLimit, int columnLimit) {
            myDelegate.limits(rowLimit, columnLimit);
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> offsets(int rowOffset, int columnOffset) {
            myDelegate.offsets(rowOffset, columnOffset);
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> right(I... right) {
            myDelegate.right(this.cast(right));
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> right(int numberOfColumns) {
            myDelegate.right(numberOfColumns);
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> right(N... elements) {
            myDelegate.right(elements);
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> row(int... rows) {
            myDelegate.row(rows);
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> superimpose(I matrix) {
            myDelegate.superimpose(myOrigin.cast(matrix).get());
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> superimpose(int row, int col, I matrix) {
            myDelegate.superimpose(row, col, myOrigin.cast(matrix).get());
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> superimpose(int row, int col, Number matrix) {
            myDelegate.superimpose(row, col, matrix);
            return this;
        }

        public void supplyTo(PhysicalStore<N> receiver) {
            myDelegate.supplyTo(receiver);
        }

        public AbstractMatrix.LogicalBuilder<N, I> transpose() {
            myDelegate.transpose();
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> triangular(boolean upper, boolean assumeOne) {
            myDelegate.triangular(upper, assumeOne);
            return this;
        }

        public AbstractMatrix.LogicalBuilder<N, I> tridiagonal() {
            myDelegate.tridiagonal();
            return this;
        }

        MatrixStore<N>[] cast(I[] matrices) {
            MatrixStore<N>[] retVal = (MatrixStore<N>[]) new MatrixStore<?>[matrices.length];
            for (int i = 0; i < retVal.length; i++) {
                retVal[i] = myOrigin.cast(matrices[i]).get();
            }
            return retVal;
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

    private transient MatrixDecomposition<N> myDecomposition = null;
    private transient int myHashCode = 0;

    private transient Boolean myHermitian = null;

    private final MatrixStore<N> myStore;

    private transient Boolean mySymmetric = null;

    @SuppressWarnings("unused")
    private AbstractMatrix() {

        this(null);

        ProgrammingError.throwForIllegalInvocation();
    }

    AbstractMatrix(final MatrixStore<N> store) {

        super();

        myStore = store;
    }

    public M add(final M addend) {

        ProgrammingError.throwIfNotEqualDimensions(myStore, addend);

        final PhysicalStore<N> retVal = myStore.physical().copy(addend);

        retVal.modifyMatching(myStore, myStore.physical().function().add());

        return this.getFactory().instantiate(retVal);
    }

    public M add(final double scalarAddend) {

        final PhysicalStore<N> retVal = myStore.physical().copy(myStore);

        final N tmpRight = myStore.physical().scalar().cast(scalarAddend);

        retVal.modifyAll(myStore.physical().function().add().second(tmpRight));

        return this.getFactory().instantiate(retVal);
    }

    /**
     * @param row The row index of where to superimpose the top left element of the addend
     * @param col The column index of where to superimpose the top left element of the addend
     * @param addend A matrix to superimpose
     * @return A new matrix
     * @deprecated v46 Use {@link #logical()} or {@link #copy()} instead
     */
    @Deprecated
    public M add(final int row, final int col, final Access2D<?> addend) {

        final MatrixStore<N> tmpDiff = this.cast(addend).get();

        //return this.getFactory().instantiate(new SuperimposedStore<N>(myStore, row, col, tmpDiff));
        return this.getFactory().instantiate(myStore.logical().superimpose(row, col, tmpDiff).get());
    }

    public M add(final Number scalarAddend) {

        final org.ojalgo.matrix.store.PhysicalStore.Factory<N, ?> tmpPhysical = myStore.physical();

        final PhysicalStore<N> retVal = tmpPhysical.copy(myStore);

        final N tmpRight = tmpPhysical.scalar().cast(scalarAddend);

        retVal.modifyAll(tmpPhysical.function().add().second(tmpRight));

        return this.getFactory().instantiate(retVal);
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
        return this.getFactory().instantiate(myStore.conjugate());
    }

    /**
     * @return A fully mutable matrix builder with the elements initially set to a copy of this matrix.
     */
    public AbstractMatrix.PhysicalBuilder<N, M> copy() {
        return this.getFactory().wrap(myStore.copy());
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

    public M divide(final double scalarDivisor) {

        final PhysicalStore<N> retVal = myStore.physical().copy(myStore);

        final N tmpRight = myStore.physical().scalar().cast(scalarDivisor);

        retVal.modifyAll(myStore.physical().function().divide().second(tmpRight));

        return this.getFactory().instantiate(retVal);
    }

    public M divide(final Number scalarDivisor) {

        final PhysicalStore<N> retVal = myStore.physical().copy(myStore);

        final N tmpRight = myStore.physical().scalar().cast(scalarDivisor);

        retVal.modifyAll(myStore.physical().function().divide().second(tmpRight));

        return this.getFactory().instantiate(retVal);
    }

    /**
     * Divides the elements of this with the elements of aMtrx. The matrices must have equal dimensions.
     *
     * @param aMtrx The denominator elements.
     * @return A new matrix whos elements are the elements of this divided with the elements of aMtrx.
     * @deprecated v46 Use {@link #logical()} or {@link #copy()} instead
     */
    @Deprecated
    public M divideElements(final Access2D<?> divisor) {

        ProgrammingError.throwIfNotEqualDimensions(myStore, divisor);

        final PhysicalStore<N> retVal = myStore.physical().copy(divisor);

        retVal.modifyMatching(myStore, myStore.physical().function().divide());

        return this.getFactory().instantiate(retVal);
    }

    public double doubleValue(final long index) {
        return myStore.doubleValue(index);
    }

    public double doubleValue(final long i, final long j) {
        return myStore.doubleValue(i, j);
    }

    public M enforce(final NumberContext context) {

        final PhysicalStore<N> tmpCopy = myStore.copy();

        tmpCopy.modifyAll(myStore.physical().function().enforce(context));

        return this.getFactory().instantiate(tmpCopy);
    }

    /**
     * @return true if the frobenius norm of the difference between [this] and [aStore] is zero within the
     *         limits of aCntxt.
     */
    public boolean equals(final Access2D<?> another, final NumberContext precision) {
        return Access2D.equals(myStore, another, precision);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Access2D<?>) {
            return this.equals((Access2D<?>) obj, NumberContext.getGeneral(6));
        } else {
            return super.equals(obj);
        }
    }

    /**
     * BasicMatrix instances are intended to be immutable. If they are it is possible to cache (partial)
     * calculation results. Calling this method should flush any cached calculation results.
     */
    public void flushCache() {

        myHashCode = 0;

        if (myDecomposition != null) {
            myDecomposition.reset();
            myDecomposition = null;
        }

        myHermitian = null;
        mySymmetric = null;

    }

    public N get(final long index) {
        return myStore.get(index);
    }

    public N get(final long aRow, final long aColumn) {
        return myStore.get(aRow, aColumn);
    }

    /**
     * @param first The first column to include.
     * @param limit The limit (exclusive) - the first column not to include.
     * @return A new matrix with only the specified range of columns
     * @deprecated v46 Use {@link #logical()} or {@link #copy()} instead
     */
    @Deprecated
    public M getColumnsRange(final int first, final int limit) {
        return this.getFactory().instantiate(myStore.logical().limits((int) myStore.countRows(), limit).offsets(0, first).get());
    }

    /**
     * Matrix condition (2-norm)
     *
     * @return ratio of largest to smallest singular value.
     */
    public Scalar<N> getCondition() {
        return myStore.physical().scalar().convert(this.getComputedSingularValue().getCondition());
    }

    /**
     * @return The matrix' determinant.
     */
    public Scalar<N> getDeterminant() {

        N tmpDeterminant = null;

        if ((myDecomposition != null) && (myDecomposition instanceof MatrixDecomposition.Determinant)
                && ((MatrixDecomposition.Determinant<N>) myDecomposition).isComputed()) {

            tmpDeterminant = ((MatrixDecomposition.Determinant<N>) myDecomposition).getDeterminant();

        } else {

            final DeterminantTask<N> tmpTask = this.getTaskDeterminant(myStore);

            if (tmpTask instanceof MatrixDecomposition.Determinant) {
                myDecomposition = (MatrixDecomposition.Determinant<N>) tmpTask;
            }

            tmpDeterminant = tmpTask.calculateDeterminant(myStore);
        }

        return myStore.physical().scalar().convert(tmpDeterminant);
    }

    public List<Eigenpair> getEigenpairs() {

        if (!this.isSquare()) {
            throw new ProgrammingError("Only defined for square matrices!");
        }

        Eigenvalue<N> evd = this.getComputedEigenvalue();

        List<Eigenpair> retVal = new ArrayList<>();

        for (int i = 0, limit = evd.getEigenvalues().size(); i < limit; i++) {
            retVal.add(evd.getEigenpair(i));
        }

        retVal.sort(Comparator.reverseOrder());

        return retVal;
    }

    /**
     * The rank of a matrix is the (maximum) number of linearly independent rows or columns it contains. It is
     * also equal to the number of nonzero singular values of the matrix.
     *
     * @return The matrix' rank.
     * @see MatrixDecomposition.RankRevealing
     */
    public int getRank() {
        return this.getRankRevealing(myStore).getRank();
    }

    /**
     * @param first The first row to include.
     * @param kimit The limit (exclusive) - the first row not to include.
     * @return A new matrix with only the specified range of rows
     * @deprecated v46 Use {@link #logical()} or {@link #copy()} instead
     */
    @Deprecated
    public M getRowsRange(final int first, final int limit) {
        return this.getFactory().instantiate(myStore.logical().limits(limit, (int) myStore.countColumns()).offsets(first, 0).get());
    }

    /**
     * @deprecated v40 Use {@link SingularValue}
     */
    @Deprecated
    public List<Double> getSingularValues() {
        return this.getComputedSingularValue().getSingularValues();
    }

    /**
     * The sum of the diagonal elements.
     *
     * @return The matrix' trace.
     */
    public Scalar<N> getTrace() {

        final AggregatorFunction<N> tmpAggr = myStore.physical().aggregator().sum();

        myStore.visitDiagonal(0, 0, tmpAggr);

        return myStore.physical().scalar().convert(tmpAggr.get());
    }

    @Override
    public int hashCode() {
        if (myHashCode == 0) {
            myHashCode = MatrixUtils.hashCode(myStore);
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

        MatrixStore<N> tmpInverse = null;

        if ((myDecomposition != null) && (myDecomposition instanceof MatrixDecomposition.Solver)
                && ((MatrixDecomposition.Solver<?>) myDecomposition).isSolvable()) {

            tmpInverse = ((MatrixDecomposition.Solver<N>) myDecomposition).getInverse();

        } else {

            final InverterTask<N> tmpTask = this.getTaskInverter(myStore);

            if (tmpTask instanceof MatrixDecomposition.Solver) {

                final MatrixDecomposition.Solver<N> tmpSolver = (MatrixDecomposition.Solver<N>) tmpTask;
                myDecomposition = tmpSolver;

                if (tmpSolver.compute(myStore)) {
                    tmpInverse = tmpSolver.getInverse();
                } else {
                    tmpInverse = null;
                }

            } else {

                try {
                    tmpInverse = tmpTask.invert(myStore);
                } catch (final RecoverableCondition xcptn) {
                    xcptn.printStackTrace();
                    tmpInverse = null;
                }
            }
        }

        return this.getFactory().instantiate(tmpInverse);
    }

    public boolean isAbsolute(final long row, final long col) {
        return myStore.isAbsolute(row, col);
    }

    /**
     * @return true if {@linkplain #getRank()} == min({@linkplain #countRows()}, {@linkplain #countColumns()})
     * @see MatrixDecomposition.RankRevealing
     */
    public boolean isFullRank() {
        return this.getRankRevealing(myStore).isFullRank();
        // return this.getRank() == Math.min(myStore.countRows(), myStore.countColumns());
    }

    public boolean isHermitian() {
        if (myHermitian == null) {
            myHermitian = this.isSquare() && myStore.equals(myStore.conjugate(), NumberContext.getGeneral(6));
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
            mySymmetric = this.isSquare() && myStore.equals(myStore.transpose(), NumberContext.getGeneral(6));
        }
        return mySymmetric.booleanValue();
    }

    public AbstractMatrix.LogicalBuilder<N, M> logical() {
        return new Logical<>(this);
    }

    /**
     * [belowRows] is appended below [this]. The two matrices must have the same number of columns.
     *
     * @param belowRows The matrix to merge.
     * @return A new matrix with more rows.
     * @deprecated v46 Use {@link #logical()} or {@link #copy()} instead
     */
    @Deprecated
    public M mergeColumns(final Access2D<?> belowRows) {

        ProgrammingError.throwIfNotEqualColumnDimensions(myStore, belowRows);

        final MatrixStore<N> tmpBelow = this.cast(belowRows).get();

        return this.getFactory().instantiate(myStore.logical().below(tmpBelow).get());
    }

    /**
     * [rightColumns] is appended to the right of [this]. The two matrices must have the same number of rows.
     *
     * @param rightColumns The matrix to merge.
     * @return A new matrix with more columns.
     * @deprecated v46 Use {@link #logical()} or {@link #copy()} instead
     */
    @Deprecated
    public M mergeRows(final Access2D<?> rightColumns) {

        ProgrammingError.throwIfNotEqualRowDimensions(myStore, rightColumns);

        final MatrixStore<N> tmpRight = this.cast(rightColumns).get();

        return this.getFactory().instantiate(myStore.logical().right(tmpRight).get());
    }

    /**
     * @deprecated v42 Use {@link #logical()} or {@link #copy()} instead
     */
    @Deprecated
    public M modify(final UnaryFunction<? extends Number> modifier) {

        final PhysicalStore<N> retVal = myStore.copy();

        retVal.modifyAll((UnaryFunction<N>) modifier);

        return this.getFactory().instantiate(retVal);
    }

    public M multiply(final M multiplicand) {

        ProgrammingError.throwIfMultiplicationNotPossible(myStore, multiplicand);

        return this.getFactory().instantiate(myStore.multiply(this.cast(multiplicand).get()));
    }

    public M multiply(final double scalarMultiplicand) {

        final PhysicalStore<N> retVal = myStore.physical().copy(myStore);

        final N tmpRight = myStore.physical().scalar().cast(scalarMultiplicand);

        retVal.modifyAll(myStore.physical().function().multiply().second(tmpRight));

        return this.getFactory().instantiate(retVal);
    }

    public M multiply(final Number scalarMultiplicand) {

        final PhysicalStore<N> retVal = myStore.physical().copy(myStore);

        final N tmpRight = myStore.physical().scalar().cast(scalarMultiplicand);

        retVal.modifyAll(myStore.physical().function().multiply().second(tmpRight));

        return this.getFactory().instantiate(retVal);
    }

    /**
     * Multiplies the elements of this matrix with the elements of aMtrx. The matrices must have equal
     * dimensions.
     *
     * @param aMtrx The elements to multiply by.
     * @return A new matrix whos elements are the elements of this multiplied with the elements of aMtrx.
     * @deprecated v46 Use {@link #logical()} or {@link #copy()} instead
     */
    @Deprecated
    public M multiplyElements(final Access2D<?> multiplicand) {

        ProgrammingError.throwIfNotEqualDimensions(myStore, multiplicand);

        final PhysicalStore<N> retVal = myStore.physical().copy(multiplicand);

        retVal.modifyMatching(myStore, myStore.physical().function().multiply());

        return this.getFactory().instantiate(retVal);
    }

    public M negate() {

        final PhysicalStore<N> retVal = myStore.copy();

        retVal.modifyAll(myStore.physical().function().negate());

        return this.getFactory().instantiate(retVal);
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

    public M reduceColumns(Aggregator aggregator) {
        return this.getFactory().instantiate(myStore.reduceColumns(aggregator).get());
    }

    public M reduceRows(Aggregator aggregator) {
        return this.getFactory().instantiate(myStore.reduceRows(aggregator).get());
    }

    /**
     * @param someCols An ordered array of column indeces.
     * @return A matrix with a subset of, reordered, columns.
     * @deprecated v46 Use {@link #logical()} or {@link #copy()} instead
     */
    @Deprecated
    public M selectColumns(int... someCols) {
        return this.logical().column(someCols).get();
    }

    /**
     * @param someRows An ordered array of row indeces.
     * @return A matrix with a subset of, reordered, rows.
     * @deprecated v46 Use {@link #logical()} or {@link #copy()} instead
     */
    @Deprecated
    public M selectRows(int... someRows) {
        return this.logical().row(someRows).get();
    }

    public M signum() {
        return this.getFactory().instantiate(myStore.signum());
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
    public M solve(final Access2D<?> rhs) {

        MatrixStore<N> tmpSolution = null;

        if ((myDecomposition != null) && (myDecomposition instanceof MatrixDecomposition.Solver)
                && ((MatrixDecomposition.Solver<?>) myDecomposition).isSolvable()) {

            tmpSolution = ((MatrixDecomposition.Solver<N>) myDecomposition).getSolution(this.cast(rhs));

        } else {

            final SolverTask<N> tmpTask = this.getTaskSolver(myStore, rhs);

            if (tmpTask instanceof MatrixDecomposition.Solver) {

                final MatrixDecomposition.Solver<N> tmpSolver = (MatrixDecomposition.Solver<N>) tmpTask;
                myDecomposition = tmpSolver;

                if (tmpSolver.compute(myStore)) {
                    tmpSolution = tmpSolver.getSolution(this.cast(rhs));
                } else {
                    tmpSolution = null;
                }

            } else {

                try {
                    tmpSolution = tmpTask.solve(myStore, rhs);
                } catch (final RecoverableCondition xcptn) {
                    xcptn.printStackTrace();
                    tmpSolution = null;
                }
            }
        }

        return this.getFactory().instantiate(tmpSolution);
    }

    public M subtract(final M subtrahend) {

        ProgrammingError.throwIfNotEqualDimensions(myStore, subtrahend);

        final PhysicalStore<N> retVal = myStore.physical().copy(subtrahend);

        retVal.modifyMatching(myStore, myStore.physical().function().subtract());

        return this.getFactory().instantiate(retVal);
    }

    public M subtract(final double scalarSubtrahend) {

        final PhysicalStore<N> retVal = myStore.physical().copy(myStore);

        final N tmpRight = myStore.physical().scalar().cast(scalarSubtrahend);

        retVal.modifyAll(myStore.physical().function().subtract().second(tmpRight));

        return this.getFactory().instantiate(retVal);
    }

    public M subtract(final Number scalarSubtrahend) {

        final PhysicalStore<N> retVal = myStore.physical().copy(myStore);

        final N tmpRight = myStore.physical().scalar().cast(scalarSubtrahend);

        retVal.modifyAll(myStore.physical().function().subtract().second(tmpRight));

        return this.getFactory().instantiate(retVal);
    }

    public void supplyTo(PhysicalStore<N> receiver) {
        myStore.supplyTo(receiver);
    }

    /**
     * Extracts one element of this matrix as a Scalar.
     *
     * @param row A row index.
     * @param col A column index.
     * @return One matrix element
     */
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
        return this.getFactory().instantiate(myStore.transpose());
    }

    private final Eigenvalue<N> getComputedEigenvalue() {

        if (!this.isComputedEigenvalue()) {
            myDecomposition = Eigenvalue.make(myStore);
            myDecomposition.decompose(myStore);
        }

        return (Eigenvalue<N>) myDecomposition;
    }

    private final SingularValue<N> getComputedSingularValue() {

        if (!this.isComputedSingularValue()) {
            myDecomposition = SingularValue.make(myStore);
            myDecomposition.decompose(myStore);
        }

        return (SingularValue<N>) myDecomposition;
    }

    private MatrixDecomposition.RankRevealing<N> getRankRevealing(final MatrixStore<N> store) {

        if ((myDecomposition != null) && (myDecomposition instanceof MatrixDecomposition.RankRevealing)
                && ((MatrixDecomposition.RankRevealing<?>) myDecomposition).isComputed()) {

        } else {

            if (store.isTall()) {
                myDecomposition = this.getDecompositionQR(store);
            } else if (store.isFat()) {
                myDecomposition = this.getDecompositionSingularValue(store);
            } else {
                myDecomposition = this.getDecompositionLU(store);
            }

            myDecomposition.decompose(store);
        }

        return (MatrixDecomposition.RankRevealing<N>) myDecomposition;
    }

    private boolean isComputedEigenvalue() {
        return (myDecomposition != null) && (myDecomposition instanceof Eigenvalue) && myDecomposition.isComputed();
    }

    private boolean isComputedSingularValue() {
        return (myDecomposition != null) && (myDecomposition instanceof SingularValue) && myDecomposition.isComputed();
    }

    abstract ElementsSupplier<N> cast(Access1D<?> matrix);

    abstract Eigenvalue<N> getDecompositionEigenvalue(Structure2D typical);

    abstract LU<N> getDecompositionLU(Structure2D typical);

    abstract QR<N> getDecompositionQR(Structure2D typical);

    abstract SingularValue<N> getDecompositionSingularValue(Structure2D typical);

    abstract MatrixFactory<N, M> getFactory();

    final MatrixStore<N> getStore() {
        return myStore;
    }

    abstract DeterminantTask<N> getTaskDeterminant(final MatrixStore<N> template);

    abstract InverterTask<N> getTaskInverter(final MatrixStore<N> template);

    abstract SolverTask<N> getTaskSolver(MatrixStore<N> templateBody, Access2D<?> templateRHS);

}
