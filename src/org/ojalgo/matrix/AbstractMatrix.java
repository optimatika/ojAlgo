/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.decomposition.MatrixDecomposition;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.task.DeterminantTask;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.matrix.task.TaskException;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * ArbitraryMatrix
 *
 * @author apete
 */
abstract class AbstractMatrix<N extends Number, I extends BasicMatrix> extends Object implements BasicMatrix, Serializable {

    private transient MatrixDecomposition<N> myDecomposition;
    private transient int myHashCode = 0;
    private final MatrixStore<N> myStore;

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

        MatrixError.throwIfNotEqualDimensions(myStore, addend);

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

        final MatrixStore<N> tmpDiff = this.cast(addend);

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

    public N aggregateAll(final Aggregator aggregator) {
        return myStore.aggregateAll(aggregator);
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

        MatrixError.throwIfNotEqualDimensions(myStore, divisor);

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

    public boolean equals(final Access2D<?> aMtrx, final NumberContext aCntxt) {
        return AccessUtils.equals(myStore, aMtrx, aCntxt);
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

        myDecomposition = null;
    }

    public N get(final long index) {
        return myStore.get(index);
    }

    public N get(final long aRow, final long aColumn) {
        return this.getStore().get(aRow, aColumn);
    }

    public I getColumnsRange(final int first, final int limit) {
        return this.getFactory().instantiate(myStore.logical().limits((int) myStore.countRows(), limit).offsets(0, first).get());
    }

    public Scalar<N> getCondition() {
        return myStore.physical().scalar().convert(this.getComputedSingularValue().getCondition());
    }

    public Scalar<N> getDeterminant() {
        return myStore.physical().scalar().convert(this.getComputedLU().getDeterminant());
    }

    public List<ComplexNumber> getEigenvalues() {
        return this.getComputedEigenvalue().getEigenvalues();
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getFrobeniusNorm()
     */
    public Scalar<N> getFrobeniusNorm() {
        return myStore.physical().scalar().convert(myStore.aggregateAll(Aggregator.NORM2));
    }

    public Scalar<N> getInfinityNorm() {

        double retVal = PrimitiveMath.ZERO;
        final AggregatorFunction<N> tmpRowSumAggr = myStore.physical().aggregator().norm1();

        final int tmpRowDim = (int) myStore.countRows();
        for (int i = 0; i < tmpRowDim; i++) {
            myStore.visitRow(i, 0, tmpRowSumAggr);
            retVal = PrimitiveFunction.MAX.invoke(retVal, tmpRowSumAggr.doubleValue());
            tmpRowSumAggr.reset();
        }

        return myStore.physical().scalar().convert(retVal);
    }

    public Scalar<N> getKyFanNorm(final int k) {
        return myStore.physical().scalar().convert(this.getComputedSingularValue().getKyFanNorm(k));
    }

    public Scalar<N> getOneNorm() {

        double retVal = PrimitiveMath.ZERO;
        final AggregatorFunction<N> tmpColSumAggr = myStore.physical().aggregator().norm1();

        final int tmpColDim = (int) this.countColumns();
        for (int j = 0; j < tmpColDim; j++) {
            myStore.visitColumn(0, j, tmpColSumAggr);
            retVal = PrimitiveFunction.MAX.invoke(retVal, tmpColSumAggr.doubleValue());
            tmpColSumAggr.reset();
        }

        return myStore.physical().scalar().convert(retVal);
    }

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

        return myStore.physical().scalar().convert(tmpAggr.getNumber());
    }

    public Scalar<N> getTraceNorm() {
        return myStore.physical().scalar().convert(this.getComputedSingularValue().getTraceNorm());
    }

    public Scalar<N> getVectorNorm(final int aDegree) {

        switch (aDegree) {

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

        MatrixStore<N> retVal = null;

        //        if (this.isSquare() && this.getComputedLU().isSolvable()) {
        //            retVal = this.getComputedLU().getInverse();
        //        } else if (this.isTall() && this.getComputedQR().isSolvable()) {
        //            retVal = this.getComputedQR().getInverse();
        //        } else {
        //            retVal = this.getComputedSingularValue().getInverse();
        //        }

        retVal = this.doInvert();

        return this.getFactory().instantiate(retVal);
    }

    public boolean isAbsolute(final long row, final long col) {
        return myStore.isAbsolute(row, col);
    }

    public boolean isFullRank() {
        return this.getRank() == Math.min(myStore.countRows(), myStore.countColumns());
    }

    public boolean isHermitian() {
        return this.isSquare() && myStore.equals(myStore.conjugate(), NumberContext.getGeneral(6));
    }

    public boolean isSmall(final double comparedTo) {
        return myStore.isSmall(comparedTo);
    }

    public boolean isSmall(final long row, final long col, final double comparedTo) {
        return myStore.isSmall(row, col, comparedTo);
    }

    public boolean isSymmetric() {
        return this.isSquare() && myStore.equals(myStore.transpose(), NumberContext.getGeneral(6));
    }

    public I mergeColumns(final Access2D<?> aMtrx) {

        MatrixError.throwIfNotEqualColumnDimensions(myStore, aMtrx);

        //return this.getFactory().instantiate(new AboveBelowStore<N>(myStore, this.getStoreFrom(aMtrx)));
        return this.getFactory().instantiate(myStore.logical().below(this.cast(aMtrx)).get());
    }

    public I mergeRows(final Access2D<?> aMtrx) {

        MatrixError.throwIfNotEqualRowDimensions(myStore, aMtrx);

        //return this.getFactory().instantiate(new LeftRightStore<N>(myStore, this.getStoreFrom(aMtrx)));
        return this.getFactory().instantiate(myStore.logical().right(this.cast(aMtrx)).get());
    }

    public I modify(final UnaryFunction<? extends Number> aFunc) {

        final PhysicalStore<N> retVal = myStore.copy();

        retVal.modifyAll((UnaryFunction<N>) aFunc);

        return this.getFactory().instantiate(retVal);
    }

    public I multiply(final BasicMatrix multiplicand) {

        MatrixError.throwIfMultiplicationNotPossible(myStore, multiplicand);

        return this.getFactory().instantiate(myStore.multiply(this.cast(multiplicand)));
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

        MatrixError.throwIfNotEqualDimensions(myStore, multiplicand);

        final PhysicalStore<N> retVal = myStore.physical().copy(multiplicand);

        retVal.modifyMatching(myStore, myStore.physical().function().multiply());

        return this.getFactory().instantiate(retVal);
    }

    public I negate() {

        final PhysicalStore<N> retVal = myStore.copy();

        retVal.modifyAll(myStore.physical().function().negate());

        return this.getFactory().instantiate(retVal);
    }

    public double norm() {
        return myStore.norm();
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

        MatrixStore<N> retVal = null;

        final MatrixStore<N> tmpRHS = this.cast(rhs);

        //        if (this.isSquare() && this.getComputedLU().isSolvable()) {
        //            retVal = this.getComputedLU().solve(tmpRHS);
        //        } else if (this.isTall() && this.getComputedQR().isSolvable()) {
        //            retVal = this.getComputedQR().solve(tmpRHS);
        //        } else {
        //            retVal = this.getComputedSingularValue().solve(tmpRHS);
        //        }

        retVal = this.doSolve(tmpRHS);

        return this.getFactory().instantiate(retVal);
    }

    public I subtract(final BasicMatrix subtrahend) {

        MatrixError.throwIfNotEqualDimensions(myStore, subtrahend);

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

    public PhysicalStore<BigDecimal> toBigStore() {
        return BigDenseStore.FACTORY.copy(this);
    }

    public PhysicalStore<ComplexNumber> toComplexStore() {
        return ComplexDenseStore.FACTORY.copy(this);
    }

    public List<BasicMatrix> toListOfColumns() {

        final int tmpColDim = (int) this.countColumns();

        final List<BasicMatrix> retVal = new ArrayList<>(tmpColDim);

        for (int j = 0; j < tmpColDim; j++) {
            retVal.add(j, this.selectColumns(j));
        }

        return retVal;
    }

    public List<N> toListOfElements() {
        return myStore.copy().asList();
    }

    public List<BasicMatrix> toListOfRows() {

        final int tmpRowDim = (int) this.countRows();

        final List<BasicMatrix> retVal = new ArrayList<>(tmpRowDim);

        for (int i = 0; i < tmpRowDim; i++) {
            retVal.add(i, this.selectRows(i));
        }

        return retVal;
    }

    public PhysicalStore<Double> toPrimitiveStore() {
        return PrimitiveDenseStore.FACTORY.copy(this);
    }

    public Scalar<N> toScalar(final long row, final long col) {
        return myStore.toScalar(row, col);
    }

    @Override
    public String toString() {
        return MatrixUtils.toString(this);
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

    private final LU<N> getComputedLU() {

        if (!this.isComputedLU()) {
            myDecomposition = LU.make(myStore);
            myDecomposition.decompose(myStore);
        }

        return (LU<N>) myDecomposition;
    }

    private final QR<N> getComputedQR() {

        if (!this.isComputedQR()) {
            myDecomposition = QR.make(myStore);
            myDecomposition.decompose(myStore);
        }

        return (QR<N>) myDecomposition;
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

    private boolean isComputedLU() {
        return (myDecomposition != null) && (myDecomposition instanceof LU) && myDecomposition.isComputed();
    }

    private boolean isComputedQR() {
        return (myDecomposition != null) && (myDecomposition instanceof QR) && myDecomposition.isComputed();
    }

    private boolean isComputedSingularValue() {
        return (myDecomposition != null) && (myDecomposition instanceof SingularValue) && myDecomposition.isComputed();
    }

    protected final MatrixStore<N> doInvert() {

        if ((myDecomposition != null) && (myDecomposition instanceof MatrixDecomposition.Solver)
                && ((MatrixDecomposition.Solver<?>) myDecomposition).isSolvable()) {

            return ((MatrixDecomposition.Solver<N>) myDecomposition).getInverse();

        } else {

            final InverterTask<N> tmpTask = this.getInverterTask(myStore);

            if (tmpTask instanceof MatrixDecomposition.Solver) {

                final MatrixDecomposition.Solver<N> tmpSolver = (MatrixDecomposition.Solver<N>) tmpTask;
                myDecomposition = tmpSolver;

                if (tmpSolver.compute(myStore)) {
                    return tmpSolver.getInverse();
                } else {
                    return null;
                }

            } else {

                try {
                    return tmpTask.invert(myStore);
                } catch (final TaskException xcptn) {
                    xcptn.printStackTrace();
                    return null;
                }
            }
        }
    }

    protected final MatrixStore<N> doSolve(final MatrixStore<N> rhs) {

        if ((myDecomposition != null) && (myDecomposition instanceof MatrixDecomposition.Solver)
                && ((MatrixDecomposition.Solver<?>) myDecomposition).isSolvable()) {

            return ((MatrixDecomposition.Solver<N>) myDecomposition).getSolution(rhs);

        } else {

            final SolverTask<N> tmpTask = this.getSolverTask(myStore, rhs);

            if (tmpTask instanceof MatrixDecomposition.Solver) {

                final MatrixDecomposition.Solver<N> tmpSolver = (MatrixDecomposition.Solver<N>) tmpTask;
                myDecomposition = tmpSolver;

                if (tmpSolver.compute(myStore)) {
                    return tmpSolver.getSolution(rhs);
                } else {
                    return null;
                }

            } else {

                try {
                    return tmpTask.solve(myStore, rhs);
                } catch (final TaskException xcptn) {
                    xcptn.printStackTrace();
                    return null;
                }
            }
        }
    }

    abstract MatrixStore<N> cast(Access1D<?> matrix);

    abstract DeterminantTask<N> getDeterminantTask(final MatrixStore<N> template);

    abstract MatrixFactory<N, I> getFactory();

    abstract InverterTask<N> getInverterTask(final MatrixStore<N> template);

    abstract SolverTask<N> getSolverTask(MatrixStore<N> templateBody, MatrixStore<N> templateRHS);

    final MatrixStore<N> getStore() {
        return myStore;
    }

}
