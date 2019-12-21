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
package org.ojalgo.matrix;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import org.ojalgo.ProgrammingError;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.algebra.NormedVectorSpace;
import org.ojalgo.algebra.Operation;
import org.ojalgo.algebra.ScalarOperation;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
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
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.task.DeterminantTask;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
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
 */
public abstract class BasicMatrix<N extends Comparable<N>, M extends BasicMatrix<N, M>> implements NormedVectorSpace<M, N>, Operation.Subtraction<M>,
        Operation.Multiplication<M>, ScalarOperation.Addition<M, N>, ScalarOperation.Division<M, N>, ScalarOperation.Subtraction<M, N>, Access2D<N>,
        Access2D.Elements, Access2D.Aggregatable<N>, Structure2D.ReducibleTo1D<M>, NumberContext.Enforceable<M>, Access2D.Collectable<N, PhysicalStore<N>> {

    public interface LogicalBuilder<N extends Comparable<N>, M extends BasicMatrix<N, M>>
            extends Structure2D.Logical<M, BasicMatrix.LogicalBuilder<N, M>>, Access2D.Collectable<N, PhysicalStore<N>> {

        default M build() {
            return this.get();
        }

    }

    public interface PhysicalReceiver<N extends Comparable<N>, M extends BasicMatrix<N, M>>
            extends Mutate2D.ModifiableReceiver<N>, Supplier<M>, Access2D.Collectable<N, PhysicalStore<N>> {

        default M build() {
            return this.get();
        }

    }

    private static final NumberContext EQUALS = NumberContext.getGeneral(8, 12);

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
            retVal = PrimitiveMath.MAX.invoke(retVal, Scalar.doubleValue(matrix.aggregateRow(i, Aggregator.NORM1)));
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
            retVal = PrimitiveMath.MAX.invoke(retVal, Scalar.doubleValue(matrix.aggregateColumn(j, Aggregator.NORM1)));
        }

        return retVal;
    }

    private transient MatrixDecomposition<N> myDecomposition = null;
    private transient int myHashCode = 0;
    private transient Boolean myHermitian = null;
    private transient Boolean mySPD = null;
    private final MatrixStore<N> myStore;
    private transient Boolean mySymmetric = null;

    @SuppressWarnings("unused")
    private BasicMatrix() {

        this(null);

        ProgrammingError.throwForIllegalInvocation();
    }

    BasicMatrix(final MatrixStore<N> store) {

        super();

        myStore = store;
    }

    public M add(final double scalarAddend) {

        Factory<N, ?> physical = myStore.physical();

        PhysicalStore<N> retVal = physical.copy(myStore);

        N right = physical.scalar().cast(scalarAddend);

        retVal.modifyAll(physical.function().add().second(right));

        return this.getFactory().instantiate(retVal);
    }

    public M add(final M addend) {

        ProgrammingError.throwIfNotEqualDimensions(myStore, addend);

        final PhysicalStore<N> retVal = myStore.physical().copy(addend);

        retVal.modifyMatching(myStore, myStore.physical().function().add());

        return this.getFactory().instantiate(retVal);
    }

    public M add(final N scalarAddend) {

        PhysicalStore.Factory<N, ?> physical = myStore.physical();

        PhysicalStore<N> retVal = physical.copy(myStore);

        retVal.modifyAll(physical.function().add().second(scalarAddend));

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
    public abstract BasicMatrix.PhysicalReceiver<N, M> copy();

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

        Factory<N, ?> physical = myStore.physical();

        PhysicalStore<N> retVal = physical.copy(myStore);

        N right = physical.scalar().cast(scalarDivisor);

        retVal.modifyAll(physical.function().divide().second(right));

        return this.getFactory().instantiate(retVal);
    }

    public M divide(final N scalarDivisor) {

        Factory<N, ?> physical = myStore.physical();

        PhysicalStore<N> retVal = physical.copy(myStore);

        retVal.modifyAll(physical.function().divide().second(scalarDivisor));

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
     * @return true if the frobenius norm of the difference between [this] and [another] is zero within the
     *         limits of [precision].
     */
    public boolean equals(final Access2D<?> another, final NumberContext precision) {
        return Access2D.equals(myStore, another, precision);
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof Access2D<?>) {
            return Access2D.equals(myStore, (Access2D<?>) other, EQUALS);
        } else {
            return super.equals(other);
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
     * @see org.ojalgo.matrix.decomposition.MatrixDecomposition.RankRevealing
     */
    public int getRank() {
        return this.getRankRevealing(myStore).getRank();
    }

    /**
     * The sum of the diagonal elements.
     *
     * @return The matrix' trace.
     */
    public Scalar<N> getTrace() {

        final AggregatorFunction<N> tmpAggr = myStore.physical().aggregator().sum();

        myStore.visitDiagonal(tmpAggr);

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

        if (tmpInverse == null) {
            SingularValue<N> computedSVD = this.getComputedSingularValue();
            myDecomposition = computedSVD;
            tmpInverse = computedSVD.getInverse();
        }

        return this.getFactory().instantiate(tmpInverse);
    }

    public boolean isAbsolute(final long row, final long col) {
        return myStore.isAbsolute(row, col);
    }

    /**
     * @return true if {@linkplain #getRank()} == min({@linkplain #countRows()}, {@linkplain #countColumns()})
     * @see org.ojalgo.matrix.decomposition.MatrixDecomposition.RankRevealing
     */
    public boolean isFullRank() {
        return this.getRankRevealing(myStore).isFullRank();
    }

    public boolean isHermitian() {
        if (myHermitian == null) {
            myHermitian = this.isSquare() && myStore.equals(myStore.conjugate(), EQUALS);
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
            mySymmetric = this.isSquare() && myStore.equals(myStore.transpose(), EQUALS);
        }
        return mySymmetric.booleanValue();
    }

    public abstract BasicMatrix.LogicalBuilder<N, M> logical();

    public M multiply(final double scalarMultiplicand) {

        Factory<N, ?> physical = myStore.physical();

        PhysicalStore<N> retVal = physical.copy(myStore);

        N right = physical.scalar().cast(scalarMultiplicand);

        retVal.modifyAll(physical.function().multiply().second(right));

        return this.getFactory().instantiate(retVal);
    }

    public M multiply(final M multiplicand) {

        ProgrammingError.throwIfMultiplicationNotPossible(myStore, multiplicand);

        return this.getFactory().instantiate(myStore.multiply(this.cast(multiplicand).get()));
    }

    public M multiply(final N scalarMultiplicand) {

        Factory<N, ?> physical = myStore.physical();

        PhysicalStore<N> retVal = physical.copy(myStore);

        retVal.modifyAll(physical.function().multiply().second(scalarMultiplicand));

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

    public M power(final int power) {
        return this.getFactory().instantiate(myStore.power(power));
    }

    public M reduceColumns(final Aggregator aggregator) {
        return this.getFactory().instantiate(myStore.reduceColumns(aggregator).get());
    }

    public M reduceRows(final Aggregator aggregator) {
        return this.getFactory().instantiate(myStore.reduceRows(aggregator).get());
    }

    public M signum() {
        return this.getFactory().instantiate(myStore.signum());
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

    public M subtract(final double scalarSubtrahend) {

        Factory<N, ?> physical = myStore.physical();

        PhysicalStore<N> retVal = physical.copy(myStore);

        N right = physical.scalar().cast(scalarSubtrahend);

        retVal.modifyAll(physical.function().subtract().second(right));

        return this.getFactory().instantiate(retVal);
    }

    public M subtract(final M subtrahend) {

        ProgrammingError.throwIfNotEqualDimensions(myStore, subtrahend);

        final PhysicalStore<N> retVal = myStore.physical().copy(subtrahend);

        retVal.modifyMatching(myStore, myStore.physical().function().subtract());

        return this.getFactory().instantiate(retVal);
    }

    public M subtract(final N scalarSubtrahend) {

        Factory<N, ?> physical = myStore.physical();

        PhysicalStore<N> retVal = physical.copy(myStore);

        retVal.modifyAll(physical.function().subtract().second(scalarSubtrahend));

        return this.getFactory().instantiate(retVal);
    }

    public final void supplyTo(final PhysicalStore<N> receiver) {
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
    public final String toString() {
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

    abstract Cholesky<N> getDecompositionCholesky(Structure2D typical);

    abstract Eigenvalue<N> getDecompositionEigenvalue(Structure2D typical);

    abstract LDL<N> getDecompositionLDL(Structure2D typical);

    LDU<N> getDecompositionLDU(final Structure2D typical) {

        if ((myDecomposition != null) && (myDecomposition instanceof LDU)) {
            return (LDU<N>) myDecomposition;
        }

        if ((myHermitian != null) && myHermitian.booleanValue()) {
            if ((mySPD != null) && mySPD.booleanValue()) {
                return (LDU<N>) (myDecomposition = this.getDecompositionCholesky(typical));
            } else {
                return (LDU<N>) (myDecomposition = this.getDecompositionLDL(typical));
            }
        } else {
            return (LDU<N>) (myDecomposition = this.getDecompositionLDU(typical));
        }
    }

    abstract LU<N> getDecompositionLU(Structure2D typical);

    abstract QR<N> getDecompositionQR(Structure2D typical);

    abstract SingularValue<N> getDecompositionSingularValue(Structure2D typical);

    abstract MatrixFactory<N, M, ? extends LogicalBuilder<N, M>, ? extends PhysicalReceiver<N, M>, ? extends PhysicalReceiver<N, M>> getFactory();

    final MatrixStore<N> getStore() {
        return myStore;
    }

    abstract DeterminantTask<N> getTaskDeterminant(final MatrixStore<N> template);

    abstract InverterTask<N> getTaskInverter(final MatrixStore<N> template);

    abstract SolverTask<N> getTaskSolver(MatrixStore<N> templateBody, Access2D<?> templateRHS);

}
