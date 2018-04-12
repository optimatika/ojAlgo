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

import java.io.Serializable;
import java.util.List;

import org.ojalgo.ProgrammingError;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.algebra.NormedVectorSpace;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.MatrixDecomposition;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.task.DeterminantTask;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * ArbitraryMatrix
 *
 * @author apete
 */
abstract class AbstractMatrix<N extends Number, I extends BasicMatrix> extends Object implements BasicMatrix, Serializable {

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

    public I add(final BasicMatrix addend) {

        ProgrammingError.throwIfNotEqualDimensions(myStore, addend);

        final PhysicalStore<N> retVal = myStore.physical().copy(addend);

        retVal.modifyMatching(myStore, myStore.physical().function().add());

        return this.getFactory().instantiate(retVal);
    }

    public I add(final double scalarAddend) {

        final PhysicalStore<N> retVal = myStore.physical().copy(myStore);

        final N tmpRight = myStore.physical().scalar().cast(scalarAddend);

        retVal.modifyAll(myStore.physical().function().add().second(tmpRight));

        return this.getFactory().instantiate(retVal);
    }

    public I add(final int row, final int col, final Access2D<?> addend) {

        final MatrixStore<N> tmpDiff = this.cast(addend).get();

        //return this.getFactory().instantiate(new SuperimposedStore<N>(myStore, row, col, tmpDiff));
        return this.getFactory().instantiate(myStore.logical().superimpose(row, col, tmpDiff).get());
    }

    public I add(final Number scalarAddend) {

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

    public I conjugate() {
        return this.getFactory().instantiate(myStore.conjugate());
    }

    public Builder<I> copy() {
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

    public I divide(final double scalarDivisor) {

        final PhysicalStore<N> retVal = myStore.physical().copy(myStore);

        final N tmpRight = myStore.physical().scalar().cast(scalarDivisor);

        retVal.modifyAll(myStore.physical().function().divide().second(tmpRight));

        return this.getFactory().instantiate(retVal);
    }

    public I divide(final Number scalarDivisor) {

        final PhysicalStore<N> retVal = myStore.physical().copy(myStore);

        final N tmpRight = myStore.physical().scalar().cast(scalarDivisor);

        retVal.modifyAll(myStore.physical().function().divide().second(tmpRight));

        return this.getFactory().instantiate(retVal);
    }

    public I divideElements(final Access2D<?> divisor) {

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

    public I enforce(final NumberContext context) {

        final PhysicalStore<N> tmpCopy = myStore.copy();

        tmpCopy.modifyAll(myStore.physical().function().enforce(context));

        return this.getFactory().instantiate(tmpCopy);
    }

    public boolean equals(final Access2D<?> aMtrx, final NumberContext aCntxt) {
        return Access2D.equals(myStore, aMtrx, aCntxt);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Access2D<?>) {
            return this.equals((Access2D<?>) obj, NumberContext.getGeneral(6));
        } else {
            return super.equals(obj);
        }
    }

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

    public I getColumnsRange(final int first, final int limit) {
        return this.getFactory().instantiate(myStore.logical().limits((int) myStore.countRows(), limit).offsets(0, first).get());
    }

    public Scalar<N> getCondition() {
        return myStore.physical().scalar().convert(this.getComputedSingularValue().getCondition());
    }

    public Scalar<N> getDeterminant() {

        N tmpDeterminant = null;

        if ((myDecomposition != null) && (myDecomposition instanceof MatrixDecomposition.Determinant)
                && ((MatrixDecomposition.Determinant<N>) myDecomposition).isComputed()) {

            tmpDeterminant = ((MatrixDecomposition.Determinant<N>) myDecomposition).getDeterminant();

        } else {

            final DeterminantTask<N> tmpTask = this.getDeterminantTask(myStore);

            if (tmpTask instanceof MatrixDecomposition.Determinant) {
                myDecomposition = (MatrixDecomposition.Determinant<N>) tmpTask;
            }

            tmpDeterminant = tmpTask.calculateDeterminant(myStore);
        }

        return myStore.physical().scalar().convert(tmpDeterminant);
    }

    public List<ComplexNumber> getEigenvalues() {
        return this.getComputedEigenvalue().getEigenvalues();
    }

    public Scalar<N> getFrobeniusNorm() {
        return myStore.physical().scalar().convert(BasicMatrix.calculateFrobeniusNorm(this));
    }

    public Scalar<N> getInfinityNorm() {
        return myStore.physical().scalar().convert(BasicMatrix.calculateInfinityNorm(this));
    }

    public Scalar<N> getKyFanNorm(final int k) {
        return myStore.physical().scalar().convert(this.getComputedSingularValue().getKyFanNorm(k));
    }

    public Scalar<N> getOneNorm() {
        return myStore.physical().scalar().convert(BasicMatrix.calculateOneNorm(this));
    }

    /**
     * 2-norm, max singular value
     *
     * @deprecated v40 Use {@link SingularValue}
     */
    @Deprecated
    public Scalar<N> getOperatorNorm() {
        return myStore.physical().scalar().convert(this.getComputedSingularValue().getOperatorNorm());
    }

    public int getRank() {
        return this.getComputedSingularValue().getRank();
    }

    public I getRowsRange(final int first, final int limit) {
        return this.getFactory().instantiate(myStore.logical().limits(limit, (int) myStore.countColumns()).offsets(first, 0).get());
    }

    public List<Double> getSingularValues() {
        return this.getComputedSingularValue().getSingularValues();
    }

    public Scalar<N> getTrace() {

        final AggregatorFunction<N> tmpAggr = myStore.physical().aggregator().sum();

        myStore.visitDiagonal(0, 0, tmpAggr);

        return myStore.physical().scalar().convert(tmpAggr.get());
    }

    public Scalar<N> getTraceNorm() {
        return myStore.physical().scalar().convert(this.getComputedSingularValue().getTraceNorm());
    }

    /**
     * Treats [this] as if it is one dimensional (a vector) and calculates the vector norm. The interface only
     * requires that implementations can handle arguments 0, 1, 2 and {@linkplain Integer#MAX_VALUE}.
     *
     * @deprecated v40 Use {@link #aggregateAll(org.ojalgo.function.aggregator.Aggregator)}
     */
    @Deprecated
    public Scalar<N> getVectorNorm(final int degree) {

        switch (degree) {

        case 0:

            return myStore.physical().scalar().convert(myStore.aggregateAll(Aggregator.CARDINALITY));

        case 1:

            return myStore.physical().scalar().convert(myStore.aggregateAll(Aggregator.NORM1));

        case 2:

            return myStore.physical().scalar().convert(myStore.aggregateAll(Aggregator.NORM2));

        default:

            return myStore.physical().scalar().convert(myStore.aggregateAll(Aggregator.LARGEST));
        }
    }

    @Override
    public int hashCode() {
        if (myHashCode == 0) {
            myHashCode = MatrixUtils.hashCode(myStore);
        }
        return myHashCode;
    }

    public I invert() {

        MatrixStore<N> tmpInverse = null;

        if ((myDecomposition != null) && (myDecomposition instanceof MatrixDecomposition.Solver)
                && ((MatrixDecomposition.Solver<?>) myDecomposition).isSolvable()) {

            tmpInverse = ((MatrixDecomposition.Solver<N>) myDecomposition).getInverse();

        } else {

            final InverterTask<N> tmpTask = this.getInverterTask(myStore);

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

    public boolean isFullRank() {
        return this.getRank() == Math.min(myStore.countRows(), myStore.countColumns());
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

    public I mergeColumns(final Access2D<?> belowRows) {

        ProgrammingError.throwIfNotEqualColumnDimensions(myStore, belowRows);

        final MatrixStore<N> tmpBelow = this.cast(belowRows).get();

        return this.getFactory().instantiate(myStore.logical().below(tmpBelow).get());
    }

    public I mergeRows(final Access2D<?> rightColumns) {

        ProgrammingError.throwIfNotEqualRowDimensions(myStore, rightColumns);

        final MatrixStore<N> tmpRight = this.cast(rightColumns).get();

        return this.getFactory().instantiate(myStore.logical().right(tmpRight).get());
    }

    public I modify(final UnaryFunction<? extends Number> modifier) {

        final PhysicalStore<N> retVal = myStore.copy();

        retVal.modifyAll((UnaryFunction<N>) modifier);

        return this.getFactory().instantiate(retVal);
    }

    public I multiply(final BasicMatrix multiplicand) {

        ProgrammingError.throwIfMultiplicationNotPossible(myStore, multiplicand);

        return this.getFactory().instantiate(myStore.multiply(this.cast(multiplicand).get()));
    }

    public I multiply(final double scalarMultiplicand) {

        final PhysicalStore<N> retVal = myStore.physical().copy(myStore);

        final N tmpRight = myStore.physical().scalar().cast(scalarMultiplicand);

        retVal.modifyAll(myStore.physical().function().multiply().second(tmpRight));

        return this.getFactory().instantiate(retVal);
    }

    public I multiply(final Number scalarMultiplicand) {

        final PhysicalStore<N> retVal = myStore.physical().copy(myStore);

        final N tmpRight = myStore.physical().scalar().cast(scalarMultiplicand);

        retVal.modifyAll(myStore.physical().function().multiply().second(tmpRight));

        return this.getFactory().instantiate(retVal);
    }

    public I multiplyElements(final Access2D<?> multiplicand) {

        ProgrammingError.throwIfNotEqualDimensions(myStore, multiplicand);

        final PhysicalStore<N> retVal = myStore.physical().copy(multiplicand);

        retVal.modifyMatching(myStore, myStore.physical().function().multiply());

        return this.getFactory().instantiate(retVal);
    }

    public I negate() {

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

    public I reduceColumns(Aggregator aggregator) {
        return this.getFactory().instantiate(myStore.reduceColumns(aggregator).get());
    }

    public I reduceRows(Aggregator aggregator) {
        return this.getFactory().instantiate(myStore.reduceRows(aggregator).get());
    }

    public I selectColumns(final int... someCols) {
        return this.getFactory().instantiate(myStore.logical().column(someCols).get());
    }

    public I selectRows(final int... someRows) {
        return this.getFactory().instantiate(myStore.logical().row(someRows).get());
    }

    public I signum() {
        return this.getFactory().instantiate(myStore.signum());
    }

    public I solve(final Access2D<?> rhs) {

        MatrixStore<N> tmpSolution = null;

        if ((myDecomposition != null) && (myDecomposition instanceof MatrixDecomposition.Solver)
                && ((MatrixDecomposition.Solver<?>) myDecomposition).isSolvable()) {

            tmpSolution = ((MatrixDecomposition.Solver<N>) myDecomposition).getSolution(this.cast(rhs));

        } else {

            final SolverTask<N> tmpTask = this.getSolverTask(myStore, rhs);

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

    public I subtract(final BasicMatrix subtrahend) {

        ProgrammingError.throwIfNotEqualDimensions(myStore, subtrahend);

        final PhysicalStore<N> retVal = myStore.physical().copy(subtrahend);

        retVal.modifyMatching(myStore, myStore.physical().function().subtract());

        return this.getFactory().instantiate(retVal);
    }

    public BasicMatrix subtract(final double scalarSubtrahend) {

        final PhysicalStore<N> retVal = myStore.physical().copy(myStore);

        final N tmpRight = myStore.physical().scalar().cast(scalarSubtrahend);

        retVal.modifyAll(myStore.physical().function().subtract().second(tmpRight));

        return this.getFactory().instantiate(retVal);
    }

    public I subtract(final Number scalarSubtrahend) {

        final PhysicalStore<N> retVal = myStore.physical().copy(myStore);

        final N tmpRight = myStore.physical().scalar().cast(scalarSubtrahend);

        retVal.modifyAll(myStore.physical().function().subtract().second(tmpRight));

        return this.getFactory().instantiate(retVal);
    }

    public Scalar<N> toScalar(final long row, final long col) {
        return myStore.toScalar(row, col);
    }

    @Override
    public String toString() {
        return Access2D.toString(this);
    }

    public I transpose() {
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

    private boolean isComputedEigenvalue() {
        return (myDecomposition != null) && (myDecomposition instanceof Eigenvalue) && myDecomposition.isComputed();
    }

    private boolean isComputedSingularValue() {
        return (myDecomposition != null) && (myDecomposition instanceof SingularValue) && myDecomposition.isComputed();
    }

    abstract ElementsSupplier<N> cast(Access1D<?> matrix);

    abstract DeterminantTask<N> getDeterminantTask(final MatrixStore<N> template);

    abstract MatrixFactory<N, I> getFactory();

    abstract InverterTask<N> getInverterTask(final MatrixStore<N> template);

    abstract SolverTask<N> getSolverTask(MatrixStore<N> templateBody, Access2D<?> templateRHS);

    final MatrixStore<N> getStore() {
        return myStore;
    }

}
