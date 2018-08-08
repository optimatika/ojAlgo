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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.ojalgo.ProgrammingError;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.algebra.NormedVectorSpace;
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
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.context.NumberContext;

abstract class AbstractMatrix<N extends Number, I extends BasicMatrix> extends Object
        implements BasicMatrix, Serializable, Access2D.Collectable<N, PhysicalStore<N>> {

    @SuppressWarnings("unchecked")
    static final class Logical<N extends Number, I extends BasicMatrix> implements BasicMatrix.LogicalBuilder<N, I> {

        private final MatrixStore.LogicalBuilder<N> myDelegate;
        private final AbstractMatrix<N, I> myOrigin;

        Logical(AbstractMatrix<N, I> matrix) {
            super();
            myOrigin = matrix;
            myDelegate = matrix.getStore().logical();
        }

        public BasicMatrix.LogicalBuilder<N, I> above(I... above) {
            myDelegate.above(this.cast(above));
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> above(int numberOfRows) {
            myDelegate.above(numberOfRows);
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> above(N... elements) {
            myDelegate.above(elements);
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> below(I... below) {
            myDelegate.below(this.cast(below));
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> below(int numberOfRows) {
            myDelegate.below(numberOfRows);
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> below(N... elements) {
            myDelegate.below(elements);
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> bidiagonal(boolean upper, boolean assumeOne) {
            myDelegate.bidiagonal(upper, assumeOne);
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> column(int... columns) {
            myDelegate.column(columns);
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> conjugate() {
            myDelegate.conjugate();
            return this;
        }

        public long countColumns() {
            return myDelegate.countColumns();
        }

        public long countRows() {
            return myDelegate.countRows();
        }

        public BasicMatrix.LogicalBuilder<N, I> diagonal() {
            myDelegate.diagonal();
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> diagonally(I... diagonally) {
            myDelegate.diagonally(this.cast(diagonally));
            return this;
        }

        public I get() {
            return myOrigin.getFactory().instantiate(myDelegate.get());
        }

        public BasicMatrix.LogicalBuilder<N, I> hermitian(boolean upper) {
            myDelegate.hermitian(upper);
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> hessenberg(boolean upper) {
            myDelegate.hessenberg(upper);
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> left(I... left) {
            myDelegate.left(this.cast(left));
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> left(int numberOfColumns) {
            myDelegate.left(numberOfColumns);
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> left(N... elements) {
            myDelegate.left(elements);
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> limits(int rowLimit, int columnLimit) {
            myDelegate.limits(rowLimit, columnLimit);
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> offsets(int rowOffset, int columnOffset) {
            myDelegate.offsets(rowOffset, columnOffset);
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> right(I... right) {
            myDelegate.right(this.cast(right));
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> right(int numberOfColumns) {
            myDelegate.right(numberOfColumns);
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> right(N... elements) {
            myDelegate.right(elements);
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> row(int... rows) {
            myDelegate.row(rows);
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> superimpose(BasicMatrix matrix) {
            myDelegate.superimpose(myOrigin.cast(matrix).get());
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> superimpose(int row, int col, BasicMatrix matrix) {
            myDelegate.superimpose(row, col, myOrigin.cast(matrix).get());
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> superimpose(int row, int col, Number matrix) {
            myDelegate.superimpose(row, col, matrix);
            return this;
        }

        public void supplyTo(PhysicalStore<N> receiver) {
            myDelegate.supplyTo(receiver);
        }

        public BasicMatrix.LogicalBuilder<N, I> transpose() {
            myDelegate.transpose();
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> triangular(boolean upper, boolean assumeOne) {
            myDelegate.triangular(upper, assumeOne);
            return this;
        }

        public BasicMatrix.LogicalBuilder<N, I> tridiagonal() {
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

    public PhysicalBuilder<N, I> copy() {
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

    public int getRank() {
        return this.getRankRevealing(myStore).getRank();
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

    public BasicMatrix.LogicalBuilder<N, I> logical() {
        return new Logical<N, I>(this);
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

    public I signum() {
        return this.getFactory().instantiate(myStore.signum());
    }

    public I solve(final Access2D<?> rhs) {

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

    public void supplyTo(PhysicalStore<N> receiver) {
        myStore.supplyTo(receiver);
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

    abstract MatrixFactory<N, I> getFactory();

    final MatrixStore<N> getStore() {
        return myStore;
    }

    abstract DeterminantTask<N> getTaskDeterminant(final MatrixStore<N> template);

    abstract InverterTask<N> getTaskInverter(final MatrixStore<N> template);

    abstract SolverTask<N> getTaskSolver(MatrixStore<N> templateBody, Access2D<?> templateRHS);

}
