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
import org.ojalgo.matrix.store.MatrixStore.LogicalBuilder;
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
    private transient Eigenvalue<N> myEigenvalue = null;
    private transient int myHashCode = 0;
    private transient LU<N> myLU = null;
    private final PhysicalStore.Factory<N, ? extends PhysicalStore<N>> myPhysicalFactory;
    private transient QR<N> myQR = null;
    private transient SingularValue<N> mySingularValue = null;
    private final MatrixStore<N> myStore;

    @SuppressWarnings("unused")
    private AbstractMatrix() {

        this(null);

        ProgrammingError.throwForIllegalInvocation();
    }

    AbstractMatrix(final MatrixStore<N> store) {

        super();

        myStore = store;
        myPhysicalFactory = this.getFactory().getPhysicalFactory();
    }

    public I add(final BasicMatrix addend) {

        MatrixError.throwIfNotEqualDimensions(myStore, addend);

        final PhysicalStore<N> retVal = myPhysicalFactory.makeZero(this.countRows(), this.countColumns());

        retVal.fillMatching(myStore, myPhysicalFactory.function().add(), this.cast(addend));

        return this.getFactory().instantiate(retVal);
    }

    public I add(final double scalarAddend) {

        final PhysicalStore<N> retVal = myPhysicalFactory.makeZero(this.countRows(), this.countColumns());

        final N tmpRight = myPhysicalFactory.scalar().cast(scalarAddend);

        retVal.fillMatching(myPhysicalFactory.function().add().second(tmpRight), myStore);

        return this.getFactory().instantiate(retVal);
    }

    public I add(final int row, final int col, final Access2D<?> addend) {

        final MatrixStore<N> tmpDiff = this.cast(addend);

        //return this.getFactory().instantiate(new SuperimposedStore<N>(myStore, row, col, tmpDiff));
        return this.getFactory().instantiate(myStore.logical().superimpose(row, col, tmpDiff).get());
    }

    public I add(final Number scalarAddend) {

        final PhysicalStore<N> retVal = myPhysicalFactory.makeZero(this.countRows(), this.countColumns());

        final N tmpRight = myPhysicalFactory.scalar().cast(scalarAddend);

        retVal.fillMatching(myPhysicalFactory.function().add().second(tmpRight), myStore);

        return this.getFactory().instantiate(retVal);
    }

    public N aggregateAll(final Aggregator aggregator) {
        return myStore.aggregateAll(aggregator);
    }

    public I conjugate() {
        return this.getFactory().instantiate(myStore.conjugate());
    }

    public Builder<I> copyToBuilder() {
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

        final PhysicalStore<N> retVal = myPhysicalFactory.makeZero(this.countRows(), this.countColumns());

        final N tmpRight = myPhysicalFactory.scalar().cast(scalarDivisor);

        retVal.fillMatching(myPhysicalFactory.function().divide().second(tmpRight), myStore);

        return this.getFactory().instantiate(retVal);
    }

    public I divide(final Number scalarDivisor) {

        final PhysicalStore<N> retVal = myPhysicalFactory.makeZero(this.countRows(), this.countColumns());

        final N tmpRight = myPhysicalFactory.scalar().cast(scalarDivisor);

        retVal.fillMatching(myPhysicalFactory.function().divide().second(tmpRight), myStore);

        return this.getFactory().instantiate(retVal);
    }

    public I divideElements(final Access2D<?> aMtrx) {

        MatrixError.throwIfNotEqualDimensions(myStore, aMtrx);

        final PhysicalStore<N> retVal = myPhysicalFactory.makeZero(this.countRows(), this.countColumns());

        retVal.fillMatching(myStore, myPhysicalFactory.function().divide(), this.cast(aMtrx));

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

        myEigenvalue = null;
        myLU = null;
        myQR = null;
        mySingularValue = null;
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
        return myPhysicalFactory.scalar().convert(this.getComputedSingularValue().getCondition());
    }

    public Scalar<N> getDeterminant() {
        return myPhysicalFactory.scalar().convert(this.getComputedLU().getDeterminant());
    }

    public List<ComplexNumber> getEigenvalues() {
        return this.getComputedEigenvalue().getEigenvalues();
    }

    /**
     * @see org.ojalgo.matrix.BasicMatrix#getFrobeniusNorm()
     */
    public Scalar<N> getFrobeniusNorm() {

        if (this.getSingularValue().isComputed()) {

            return myPhysicalFactory.scalar().convert(this.getSingularValue().getFrobeniusNorm());

        } else {

            return myPhysicalFactory.scalar().convert(myStore.aggregateAll(Aggregator.NORM2));
        }
    }

    public Scalar<N> getInfinityNorm() {

        double retVal = PrimitiveMath.ZERO;
        final AggregatorFunction<N> tmpRowSumAggr = myPhysicalFactory.aggregator().norm1();

        final int tmpRowDim = (int) myStore.countRows();
        for (int i = 0; i < tmpRowDim; i++) {
            myStore.visitRow(i, 0, tmpRowSumAggr);
            retVal = Math.max(retVal, tmpRowSumAggr.doubleValue());
            tmpRowSumAggr.reset();
        }

        return myPhysicalFactory.scalar().convert(retVal);
    }

    public Scalar<N> getKyFanNorm(final int k) {
        return myPhysicalFactory.scalar().convert(this.getComputedSingularValue().getKyFanNorm(k));
    }

    public Scalar<N> getOneNorm() {

        double retVal = PrimitiveMath.ZERO;
        final AggregatorFunction<N> tmpColSumAggr = myPhysicalFactory.aggregator().norm1();

        final int tmpColDim = (int) this.countColumns();
        for (int j = 0; j < tmpColDim; j++) {
            myStore.visitColumn(0, j, tmpColSumAggr);
            retVal = Math.max(retVal, tmpColSumAggr.doubleValue());
            tmpColSumAggr.reset();
        }

        return myPhysicalFactory.scalar().convert(retVal);
    }

    public Scalar<N> getOperatorNorm() {
        return myPhysicalFactory.scalar().convert(this.getComputedSingularValue().getOperatorNorm());
    }

    public int getRank() {
        if (this.getSingularValue().isComputed() || this.isFat()) {
            return this.getComputedSingularValue().getRank();
        } else if (this.getQR().isComputed() || this.isTall()) {
            return this.getComputedQR().getRank();
        } else {
            return this.getComputedLU().getRank();
        }
    }

    public I getRowsRange(final int first, final int limit) {
        return this.getFactory().instantiate(myStore.logical().limits(limit, (int) myStore.countColumns()).offsets(first, 0).get());
    }

    public List<Double> getSingularValues() {
        return this.getComputedSingularValue().getSingularValues();
    }

    public Scalar<N> getTrace() {

        final AggregatorFunction<N> tmpAggr = myPhysicalFactory.aggregator().sum();

        myStore.visitDiagonal(0, 0, tmpAggr);

        return myPhysicalFactory.scalar().convert(tmpAggr.getNumber());
    }

    public Scalar<N> getTraceNorm() {
        return myPhysicalFactory.scalar().convert(this.getComputedSingularValue().getTraceNorm());
    }

    public Scalar<N> getVectorNorm(final int aDegree) {

        switch (aDegree) {

        case 0:

            return myPhysicalFactory.scalar().convert(myStore.aggregateAll(Aggregator.CARDINALITY));

        case 1:

            return myPhysicalFactory.scalar().convert(myStore.aggregateAll(Aggregator.NORM1));

        case 2:

            return myPhysicalFactory.scalar().convert(myStore.aggregateAll(Aggregator.NORM2));

        default:

            return myPhysicalFactory.scalar().convert(myStore.aggregateAll(Aggregator.LARGEST));
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

        if (this.isSquare() && this.getComputedLU().isSolvable()) {
            retVal = this.getComputedLU().getInverse();
        } else if (this.isTall() && this.getComputedQR().isSolvable()) {
            retVal = this.getComputedQR().getInverse();
        } else {
            retVal = this.getComputedSingularValue().getInverse();
        }

        //        retVal = this.doInvert();

        return this.getFactory().instantiate(retVal);
    }

    public boolean isAbsolute(final long row, final long column) {
        return myStore.isAbsolute(row, column);
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

    public boolean isSmall(final long row, final long column, final double comparedTo) {
        return myStore.isSmall(row, column, comparedTo);
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

        final PhysicalStore<N> retVal = myPhysicalFactory.makeZero(this.countRows(), this.countColumns());

        final N tmpRight = myPhysicalFactory.scalar().cast(scalarMultiplicand);

        retVal.fillMatching(myPhysicalFactory.function().multiply().second(tmpRight), myStore);

        return this.getFactory().instantiate(retVal);
    }

    public I multiply(final Number scalarMultiplicand) {

        final PhysicalStore<N> retVal = myPhysicalFactory.makeZero(this.countRows(), this.countColumns());

        final N tmpRight = myPhysicalFactory.scalar().cast(scalarMultiplicand);

        retVal.fillMatching(myPhysicalFactory.function().multiply().second(tmpRight), myStore);

        return this.getFactory().instantiate(retVal);
    }

    public I multiplyElements(final Access2D<?> aMtrx) {

        MatrixError.throwIfNotEqualDimensions(myStore, aMtrx);

        final PhysicalStore<N> retVal = myPhysicalFactory.makeZero(this.countRows(), this.countColumns());

        retVal.fillMatching(myStore, myPhysicalFactory.function().multiply(), this.cast(aMtrx));

        return this.getFactory().instantiate(retVal);
    }

    public I negate() {

        final PhysicalStore<N> retVal = myStore.copy();

        retVal.modifyAll(myPhysicalFactory.function().negate());

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

        if (this.isSquare() && this.getComputedLU().isSolvable()) {
            retVal = this.getComputedLU().solve(tmpRHS);
        } else if (this.isTall() && this.getComputedQR().isSolvable()) {
            retVal = this.getComputedQR().solve(tmpRHS);
        } else {
            retVal = this.getComputedSingularValue().solve(tmpRHS);
        }

        //        retVal = this.doSolve(tmpRHS);

        return this.getFactory().instantiate(retVal);
    }

    public I subtract(final BasicMatrix subtrahend) {

        MatrixError.throwIfNotEqualDimensions(myStore, subtrahend);

        final PhysicalStore<N> retVal = myPhysicalFactory.makeZero(this.countRows(), this.countColumns());

        retVal.fillMatching(myStore, myPhysicalFactory.function().subtract(), this.cast(subtrahend));

        return this.getFactory().instantiate(retVal);
    }

    public BasicMatrix subtract(final double scalarSubtrahend) {

        final PhysicalStore<N> retVal = myPhysicalFactory.makeZero(this.countRows(), this.countColumns());

        final N tmpRight = myPhysicalFactory.scalar().cast(scalarSubtrahend);

        retVal.fillMatching(myPhysicalFactory.function().subtract().second(tmpRight), myStore);

        return this.getFactory().instantiate(retVal);
    }

    public I subtract(final Number scalarSubtrahend) {

        final PhysicalStore<N> retVal = myPhysicalFactory.makeZero(this.countRows(), this.countColumns());

        final N tmpRight = myPhysicalFactory.scalar().cast(scalarSubtrahend);

        retVal.fillMatching(myPhysicalFactory.function().subtract().second(tmpRight), myStore);

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

        final List<BasicMatrix> retVal = new ArrayList<BasicMatrix>(tmpColDim);

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

        final List<BasicMatrix> retVal = new ArrayList<BasicMatrix>(tmpRowDim);

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

        final Eigenvalue<N> retVal = this.getEigenvalue();

        if (!retVal.isComputed()) {
            retVal.decompose(myStore);
        }

        return retVal;
    }

    private final LU<N> getComputedLU() {

        final LU<N> retVal = this.getLU();

        if (!retVal.isComputed()) {
            retVal.decompose(myStore);
        }

        return retVal;
    }

    private final QR<N> getComputedQR() {

        final QR<N> retVal = this.getQR();

        if (!retVal.isComputed()) {
            retVal.decompose(myStore);
        }

        return retVal;
    }

    private final SingularValue<N> getComputedSingularValue() {

        final SingularValue<N> retVal = this.getSingularValue();

        if (!retVal.isComputed()) {
            retVal.decompose(myStore);
        }

        return retVal;
    }

    private final Eigenvalue<N> getEigenvalue() {

        if (myEigenvalue == null) {
            myEigenvalue = Eigenvalue.make(myStore);
        }

        return myEigenvalue;
    }

    private final LU<N> getLU() {
        if (myLU == null) {
            myLU = LU.make(myStore);
        }
        return myLU;
    }

    private final QR<N> getQR() {
        if (myQR == null) {
            myQR = QR.make(myStore);
        }
        return myQR;
    }

    private final SingularValue<N> getSingularValue() {
        if (mySingularValue == null) {
            mySingularValue = SingularValue.make(myStore);
        }
        return mySingularValue;
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

                tmpSolver.compute(myStore);

                return tmpSolver.getInverse();

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

            return ((MatrixDecomposition.Solver<N>) myDecomposition).solve(rhs);

        } else {

            final SolverTask<N> tmpTask = this.getSolverTask(myStore, rhs);

            if (tmpTask instanceof MatrixDecomposition.Solver) {

                final MatrixDecomposition.Solver<N> tmpSolver = (MatrixDecomposition.Solver<N>) tmpTask;
                myDecomposition = tmpSolver;
                tmpSolver.compute(myStore);

                return tmpSolver.solve(rhs);

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

    final PhysicalStore.Factory<N, ? extends PhysicalStore<N>> getPhysicalFactory() {
        return myPhysicalFactory;
    }

    abstract SolverTask<N> getSolverTask(MatrixStore<N> templateBody, MatrixStore<N> templateRHS);

    final MatrixStore<N> getStore() {
        return myStore;
    }

}
