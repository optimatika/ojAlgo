/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
package org.ojalgo.matrix.jama;

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.access.Iterator1D;
import org.ojalgo.array.ArrayUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * This class adapts JAMA's Matrix to ojAlgo's {@linkplain BasicMatrix} and {@linkplain PhysicalStore} interfaces.
 *
 * @author apete
 */
public final class JamaMatrix extends Object implements BasicMatrix<Double>, PhysicalStore<Double>, Serializable {

    public static Access2D.Builder<JamaMatrix> getBuilder(final int aLength) {
        return FACTORY.getBuilder(aLength);
    }

    public static Access2D.Builder<JamaMatrix> getBuilder(final int aRowDim, final int aColDim) {
        return FACTORY.getBuilder(aRowDim, aColDim);
    }

    private static Matrix convert(final Access1D<?> aStore, final int structure) {

        Matrix retVal = null;

        if (aStore instanceof JamaMatrix) {
            retVal = ((JamaMatrix) aStore).getDelegate();
        } else {
            retVal = new Matrix(ArrayUtils.toRawCopyOf(aStore), structure);
        }

        return retVal;
    }

    private static Matrix convert(final Access2D<?> aStore) {

        Matrix retVal = null;

        if (aStore instanceof JamaMatrix) {
            retVal = ((JamaMatrix) aStore).getDelegate();
        } else {
            retVal = new Matrix(ArrayUtils.toRawCopyOf(aStore), (int) aStore.countRows(), (int) aStore.countColumns());
        }

        return retVal;
    }

    static Rotation.Primitive cast(final Rotation<Double> aTransf) {
        if (aTransf instanceof Rotation.Primitive) {
            return (Rotation.Primitive) aTransf;
        } else {
            return new Rotation.Primitive(aTransf);
        }
    }

    public static final JamaFactory FACTORY = new JamaFactory();

    private final Matrix myDelegate;

    public JamaMatrix(final BasicMatrix aMtrx) {

        super();

        myDelegate = JamaMatrix.convert(aMtrx);
    }

    public JamaMatrix(final MatrixStore<Double> aStore) {

        super();

        myDelegate = JamaMatrix.convert(aStore);
    }

    @SuppressWarnings("unused")
    private JamaMatrix() {

        super();

        myDelegate = null;

        ProgrammingError.throwForIllegalInvocation();
    }

    JamaMatrix(final double[][] aRaw) {

        super();

        myDelegate = new Matrix(aRaw, aRaw.length, aRaw[0].length);
    }

    JamaMatrix(final double[][] data, final int rows, final int columns) {

        super();

        myDelegate = new Matrix(data, rows, columns);
    }

    JamaMatrix(final Matrix aDelegate) {

        super();

        myDelegate = aDelegate;
    }

    public JamaMatrix add(final Access2D<?> aMtrx) {
        return new JamaMatrix(myDelegate.plus(JamaMatrix.convert(aMtrx)));
    }

    public JamaMatrix add(final int row, final int column, final Access2D<?> aMtrx) {

        final double[][] tmpArrayCopy = myDelegate.getArrayCopy();

        double[] tmpLocalRowRef;
        for (int i = 0; i < aMtrx.countRows(); i++) {
            tmpLocalRowRef = tmpArrayCopy[row + i];
            for (int j = 0; j < aMtrx.countColumns(); j++) {
                tmpLocalRowRef[column + j] = aMtrx.doubleValue(i, j);
            }
        }

        return new JamaMatrix(new Matrix(tmpArrayCopy));
    }

    public JamaMatrix add(final int row, final int column, final Number aNmbr) {

        final double[][] tmpArrayCopy = myDelegate.getArrayCopy();
        tmpArrayCopy[row][column] += aNmbr.doubleValue();

        return new JamaMatrix(new Matrix(tmpArrayCopy));
    }

    public MatrixStore<Double> add(final MatrixStore<Double> addend) {
        return new JamaMatrix(myDelegate.plus(JamaMatrix.convert(addend)));
    }

    public JamaMatrix add(final Number aNmbr) {

        final double[][] retVal = myDelegate.getArrayCopy();

        ArrayUtils.modifyAll(retVal, ADD.second(aNmbr.doubleValue()));

        return new JamaMatrix(retVal);
    }

    public Double aggregateAll(final Aggregator aggregator) {

        final AggregatorFunction<Double> tmpVisitor = aggregator.getPrimitiveFunction();

        this.visitAll(tmpVisitor);

        return tmpVisitor.doubleValue();
    }

    public List<Double> asList() {

        final int tmpColDim = JamaMatrix.this.getColDim();

        return new AbstractList<Double>() {

            @Override
            public Double get(final int someIndex) {

                return JamaMatrix.this.getDelegate().get(someIndex / tmpColDim, someIndex % tmpColDim);
            }

            @Override
            public Double set(final int someIndex, final Double aValue) {
                final Double retVal = this.get(someIndex);
                JamaMatrix.this.getDelegate().set(someIndex / tmpColDim, someIndex % tmpColDim, aValue);
                return retVal;
            }

            @Override
            public int size() {
                return JamaMatrix.this.size();
            }
        };
    }

    public final MatrixStore.Builder<Double> builder() {
        return new MatrixStore.Builder<Double>(this);
    }

    public void caxpy(final Double aSclrA, final int aColX, final int aColY, final int aFirstRow) {

        final double tmpValA = aSclrA.doubleValue();
        final double[][] tmpArray = myDelegate.getArray();

        final int tmpRowDim = myDelegate.getRowDimension();

        for (int i = aFirstRow; i < tmpRowDim; i++) {
            tmpArray[i][aColY] += tmpValA * tmpArray[i][aColX];
        }
    }

    public JamaMatrix conjugate() {
        return this.transpose();
    }

    public JamaMatrix copy() {
        return new JamaMatrix(myDelegate.getArrayCopy());
    }

    public Access2D.Builder<JamaMatrix> copyToBuilder() {

        final int tmpRowDim = this.getRowDim();
        final int tmpColDim = this.getColDim();
        final Access2D.Builder<JamaMatrix> retVal = FACTORY.getBuilder(tmpRowDim, tmpColDim);

        for (int i = 0; i < tmpRowDim; i++) {
            for (int j = 0; j < tmpColDim; j++) {
                retVal.set(i, j, this.doubleValue(i, j));
            }
        }

        return retVal;
    }

    public long count() {
        return myDelegate.getRowDimension() * myDelegate.getColumnDimension();
    }

    public long countColumns() {
        return myDelegate.getColumnDimension();
    }

    public long countRows() {
        return myDelegate.getRowDimension();
    }

    public JamaMatrix divide(final Number aNmbr) {

        final double[][] retVal = myDelegate.getArrayCopy();

        ArrayUtils.modifyAll(retVal, DIVIDE.second(aNmbr.doubleValue()));

        return new JamaMatrix(retVal);
    }

    public JamaMatrix divideElements(final Access2D<?> aMtrx) {
        return new JamaMatrix(myDelegate.arrayRightDivide(JamaMatrix.convert(aMtrx)));
    }

    public double doubleValue(final long anInd) {
        return myDelegate.get(AccessUtils.row((int) anInd, myDelegate.getRowDimension()), AccessUtils.column((int) anInd, myDelegate.getRowDimension()));
    }

    public double doubleValue(final long row, final long column) {
        return myDelegate.get((int) row, (int) column);
    }

    public JamaMatrix enforce(final NumberContext aContext) {

        final double[][] retVal = myDelegate.getArrayCopy();

        ArrayUtils.modifyAll(retVal, aContext.getPrimitiveEnforceFunction());

        return new JamaMatrix(retVal);
    }

    public final boolean equals(final Access2D<?> aMtrx, final NumberContext aCntxt) {
        return AccessUtils.equals(this, aMtrx, aCntxt);
    }

    public boolean equals(final MatrixStore<Double> other, final NumberContext context) {
        return AccessUtils.equals(this, other, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final boolean equals(final Object anObject) {
        if (anObject instanceof MatrixStore) {
            return this.equals((MatrixStore<Double>) anObject, NumberContext.getGeneral(6));
        } else if (anObject instanceof BasicMatrix) {
            return this.equals((BasicMatrix) anObject, NumberContext.getGeneral(6));
        } else {
            return super.equals(anObject);
        }
    }

    public void exchangeColumns(final int aColA, final int aColB) {
        ArrayUtils.exchangeColumns(myDelegate.getArray(), aColA, aColB);
    }

    public void exchangeRows(final int aRowA, final int aRowB) {
        ArrayUtils.exchangeRows(myDelegate.getArray(), aRowA, aRowB);
    }

    public PhysicalStore.Factory<Double, JamaMatrix> factory() {
        return FACTORY;
    }

    public void fillAll(final Double aNmbr) {
        ArrayUtils.fillAll(myDelegate.getArray(), aNmbr);
    }

    public void fillByMultiplying(final Access1D<Double> aLeftArg, final Access1D<Double> aRightArg) {

        final Matrix tmpLeft = JamaMatrix.convert(aLeftArg, myDelegate.getRowDimension());
        final Matrix tmpRight = JamaMatrix.convert(aRightArg, tmpLeft.getColumnDimension());

        myDelegate.setMatrix(0, this.getRowDim() - 1, 0, this.getColDim() - 1, tmpLeft.times(tmpRight));
    }

    public void fillColumn(final long row, final long column, final Double aNmbr) {
        ArrayUtils.fillColumn(myDelegate.getArray(), (int) row, (int) column, aNmbr);
    }

    public void fillConjugated(final Access2D<? extends Number> source) {
        this.fillTransposed(source);
    }

    public void fillDiagonal(final long row, final long column, final Double aNmbr) {
        ArrayUtils.fillDiagonal(myDelegate.getArray(), (int) row, (int) column, aNmbr);
    }

    public void fillMatching(final Access1D<? extends Number> source) {

        final double[][] tmpDelegateArray = myDelegate.getArray();

        final int tmpRowDim = myDelegate.getRowDimension();

        for (int i = 0; i < tmpRowDim; i++) {
            for (int j = 0; j < myDelegate.getColumnDimension(); j++) {
                tmpDelegateArray[i][j] = source.doubleValue(i + (j * tmpRowDim));
            }
        }
    }

    public void fillMatching(final Access1D<Double> leftArg, final BinaryFunction<Double> function, final Access1D<Double> rightArg) {
        if (leftArg == this) {
            if (function == ADD) {
                myDelegate.plusEquals(JamaMatrix.convert(rightArg, myDelegate.getRowDimension()));
            } else if (function == DIVIDE) {
                myDelegate.arrayRightDivideEquals(JamaMatrix.convert(rightArg, myDelegate.getRowDimension()));
            } else if (function == MULTIPLY) {
                myDelegate.arrayTimesEquals(JamaMatrix.convert(rightArg, myDelegate.getRowDimension()));
            } else if (function == SUBTRACT) {
                myDelegate.minusEquals(JamaMatrix.convert(rightArg, myDelegate.getRowDimension()));
            } else {
                ArrayUtils.fillMatching(myDelegate.getArray(), myDelegate.getArray(), function, JamaMatrix.convert(rightArg, myDelegate.getRowDimension())
                        .getArray());
            }
        } else if (rightArg == this) {
            if (function == ADD) {
                myDelegate.plusEquals(JamaMatrix.convert(leftArg, myDelegate.getRowDimension()));
            } else if (function == DIVIDE) {
                myDelegate.arrayLeftDivideEquals(JamaMatrix.convert(leftArg, myDelegate.getRowDimension()));
            } else if (function == MULTIPLY) {
                myDelegate.arrayTimesEquals(JamaMatrix.convert(leftArg, myDelegate.getRowDimension()));
            } else if (function == SUBTRACT) {
                ArrayUtils.fillMatching(myDelegate.getArray(), JamaMatrix.convert(leftArg, myDelegate.getRowDimension()).getArray(), function,
                        myDelegate.getArray());
            } else {
                ArrayUtils.fillMatching(myDelegate.getArray(), JamaMatrix.convert(leftArg, myDelegate.getRowDimension()).getArray(), function,
                        myDelegate.getArray());
            }
        } else {
            ArrayUtils.fillMatching(myDelegate.getArray(), JamaMatrix.convert(leftArg, myDelegate.getRowDimension()).getArray(), function,
                    JamaMatrix.convert(rightArg, myDelegate.getRowDimension()).getArray());
        }
    }

    public void fillMatching(final Access1D<Double> aLeftArg, final BinaryFunction<Double> function, final Double aRightArg) {
        ArrayUtils.fillMatching(myDelegate.getArray(), JamaMatrix.convert(aLeftArg, myDelegate.getRowDimension()).getArray(), function, aRightArg);
    }

    public void fillMatching(final Double aLeftArg, final BinaryFunction<Double> function, final Access1D<Double> aRightArg) {
        ArrayUtils.fillMatching(myDelegate.getArray(), aLeftArg, function, JamaMatrix.convert(aRightArg, myDelegate.getRowDimension()).getArray());
    }

    public void fillRange(final long first, final long limit, final Double value) {
        ArrayUtils.fillRange(myDelegate.getArray(), (int) first, (int) limit, value);
    }

    public void fillRow(final long row, final long column, final Double aNmbr) {
        ArrayUtils.fillRow(myDelegate.getArray(), (int) row, (int) column, aNmbr);
    }

    public void fillTransposed(final Access2D<? extends Number> source) {

        final double[][] tmpDelegateArray = myDelegate.getArray();

        final int tmpRowDim = myDelegate.getRowDimension();

        for (int i = 0; i < tmpRowDim; i++) {
            for (int j = 0; j < myDelegate.getColumnDimension(); j++) {
                tmpDelegateArray[i][j] = source.doubleValue(j, i);
            }
        }
    }

    public void flushCache() {
        ;
    }

    public Double get(final long index) {
        return myDelegate.get(AccessUtils.row(index, myDelegate.getRowDimension()), AccessUtils.column(index, myDelegate.getRowDimension()));
    }

    public Double get(final long row, final long column) {
        return myDelegate.get((int) row, (int) column);
    }

    public int getColDim() {
        return myDelegate.getColumnDimension();
    }

    public JamaMatrix getColumnsRange(final int aFirst, final int aLimit) {
        return new JamaMatrix(myDelegate.getMatrix(0, this.getRowDim(), aFirst, aLimit));
    }

    public PrimitiveScalar getCondition() {
        return new PrimitiveScalar(this.getSingularValueDecomposition().getCondition());
    }

    public PrimitiveScalar getDeterminant() {
        return new PrimitiveScalar(myDelegate.det());
    }

    public List<ComplexNumber> getEigenvalues() {
        return this.getEigenvalueDecomposition().getEigenvalues();
    }

    public PrimitiveScalar getFrobeniusNorm() {
        return new PrimitiveScalar(myDelegate.normF());
    }

    public PrimitiveScalar getInfinityNorm() {
        return new PrimitiveScalar(myDelegate.normInf());
    }

    public PrimitiveScalar getKyFanNorm(final int k) {
        return new PrimitiveScalar(this.getSingularValueDecomposition().getKyFanNorm(k));
    }

    public int getMaxDim() {
        return Math.min(myDelegate.getRowDimension(), myDelegate.getColumnDimension());
    }

    public int getMinDim() {
        return Math.min(myDelegate.getRowDimension(), myDelegate.getColumnDimension());
    }

    public Scalar<Double> getOneNorm() {
        return new PrimitiveScalar(myDelegate.norm1());
    }

    public PrimitiveScalar getOperatorNorm() {
        return new PrimitiveScalar(this.getSingularValueDecomposition().getOperatorNorm());
    }

    public int getRank() {
        return this.getSingularValueDecomposition().getRank();
    }

    public int getRowDim() {
        return myDelegate.getRowDimension();
    }

    public JamaMatrix getRowsRange(final int aFirst, final int aLimit) {
        return new JamaMatrix(myDelegate.getMatrix(aFirst, aLimit, 0, this.getColDim()));
    }

    public List<Double> getSingularValues() {
        return this.getSingularValueDecomposition().getSingularValues();
    }

    public PrimitiveScalar getTrace() {
        return new PrimitiveScalar(myDelegate.trace());
    }

    public PrimitiveScalar getTraceNorm() {
        return new PrimitiveScalar(this.getSingularValueDecomposition().getTraceNorm());

    }

    public PrimitiveScalar getVectorNorm(final int aDegree) {

        if (aDegree == 0) {

            final AggregatorFunction<Double> tmpFunc = PrimitiveAggregator.getCollection().cardinality();

            ArrayUtils.visitAll(myDelegate.getArray(), tmpFunc);

            return (PrimitiveScalar) tmpFunc.toScalar();

        } else if (aDegree == 1) {

            final AggregatorFunction<Double> tmpFunc = PrimitiveAggregator.getCollection().norm1();

            ArrayUtils.visitAll(myDelegate.getArray(), tmpFunc);

            return (PrimitiveScalar) tmpFunc.toScalar();

        } else if (aDegree == 2) {

            return this.getFrobeniusNorm();

        } else {

            final AggregatorFunction<Double> tmpFunc = PrimitiveAggregator.getCollection().largest();

            this.visitAll(tmpFunc);

            return (PrimitiveScalar) tmpFunc.toScalar();
        }
    }

    @Override
    public final int hashCode() {
        return MatrixUtils.hashCode((BasicMatrix) this);
    }

    public JamaMatrix invert() {
        return new JamaMatrix(myDelegate.inverse());
    }

    public boolean isAbsolute(final long index) {
        final int tmpRowDim = myDelegate.getRowDimension();
        return PrimitiveScalar.isAbsolute(myDelegate.get(AccessUtils.row(index, tmpRowDim), AccessUtils.column(index, tmpRowDim)));
    }

    public boolean isAbsolute(final long row, final long column) {
        return PrimitiveScalar.isAbsolute(myDelegate.get((int) row, (int) column));
    }

    public boolean isEmpty() {
        return ((this.getRowDim() <= 0) || (this.getColDim() <= 0));
    }

    public boolean isFat() {
        return (!this.isEmpty() && (this.getRowDim() < this.getColDim()));
    }

    public boolean isFullRank() {
        return this.getRank() == this.getMinDim();
    }

    public boolean isHermitian() {
        return this.isSymmetric();
    }

    public boolean isInfinite(final long index) {
        final int tmpRowDim = myDelegate.getRowDimension();
        return PrimitiveScalar.isInfinite(myDelegate.get(AccessUtils.row(index, tmpRowDim), AccessUtils.column(index, tmpRowDim)));
    }

    public boolean isInfinite(final long row, final long column) {
        return PrimitiveScalar.isInfinite(this.doubleValue(row, column));
    }

    public boolean isLowerLeftShaded() {
        return false;
    }

    public boolean isNaN(final long index) {
        final int tmpRowDim = myDelegate.getRowDimension();
        return PrimitiveScalar.isNaN(myDelegate.get(AccessUtils.row(index, tmpRowDim), AccessUtils.column(index, tmpRowDim)));
    }

    public boolean isNaN(final long row, final long column) {
        return PrimitiveScalar.isNaN(this.doubleValue(row, column));
    }

    public boolean isPositive(final long index) {
        final int tmpRowDim = myDelegate.getRowDimension();
        return PrimitiveScalar.isPositive(myDelegate.get(AccessUtils.row(index, tmpRowDim), AccessUtils.column(index, tmpRowDim)));
    }

    public boolean isPositive(final long row, final long column) {
        return PrimitiveScalar.isPositive(this.doubleValue(row, column));
    }

    public boolean isReal(final long index) {
        return PrimitiveScalar.IS_REAL;
    }

    public boolean isReal(final long row, final long column) {
        return PrimitiveScalar.IS_REAL;
    }

    public boolean isScalar() {
        return (myDelegate.getRowDimension() == 1) && (myDelegate.getColumnDimension() == 1);
    }

    public boolean isSquare() {
        return (!this.isEmpty() && (this.getRowDim() == this.getColDim()));
    }

    public boolean isSymmetric() {
        return this.isSquare() && this.equals((BasicMatrix) this.transpose(), NumberContext.getGeneral(6));
    }

    public boolean isTall() {
        return (!this.isEmpty() && (this.getRowDim() > this.getColDim()));
    }

    public boolean isUpperRightShaded() {
        return false;
    }

    public boolean isVector() {
        return ((this.getColDim() == 1) || (this.getRowDim() == 1));
    }

    public boolean isZero(final long index) {
        final int tmpRowDim = myDelegate.getRowDimension();
        return PrimitiveScalar.isZero(myDelegate.get(AccessUtils.row(index, tmpRowDim), AccessUtils.column(index, tmpRowDim)));
    }

    public boolean isZero(final long row, final long column) {
        return PrimitiveScalar.isZero(this.doubleValue(row, column));
    }

    public final Iterator<Double> iterator() {
        return new Iterator1D<Double>(this);
    }

    public void maxpy(final Double aSclrA, final MatrixStore<Double> aMtrxX) {

        final double tmpValA = aSclrA;
        final double[][] tmpArray = myDelegate.getArray();

        final int tmpRowDim = myDelegate.getRowDimension();
        final int tmpColDim = myDelegate.getColumnDimension();

        for (int i = 0; i < tmpRowDim; i++) {
            for (int j = 0; j < tmpColDim; j++) {
                tmpArray[i][j] += tmpValA * aMtrxX.doubleValue(i, j);
            }
        }
    }

    public JamaMatrix mergeColumns(final Access2D<?> aMtrx) {

        final int tmpRowDim = this.getRowDim() + (int) aMtrx.countRows();
        final int tmpColDim = this.getColDim();

        final Matrix retVal = new Matrix(tmpRowDim, tmpColDim);

        retVal.setMatrix(0, this.getRowDim() - 1, 0, tmpColDim - 1, myDelegate);
        retVal.setMatrix(this.getRowDim(), tmpRowDim - 1, 0, tmpColDim - 1, JamaMatrix.convert(aMtrx));

        return new JamaMatrix(retVal);
    }

    public JamaMatrix mergeRows(final Access2D<?> aMtrx) {

        final int tmpRowDim = this.getRowDim();
        final int tmpColDim = this.getColDim() + (int) aMtrx.countColumns();

        final Matrix retVal = new Matrix(tmpRowDim, tmpColDim);

        retVal.setMatrix(0, tmpRowDim - 1, 0, this.getColDim() - 1, myDelegate);
        retVal.setMatrix(0, tmpRowDim - 1, this.getColDim(), tmpColDim - 1, JamaMatrix.convert(aMtrx));

        return new JamaMatrix(retVal);
    }

    public JamaMatrix modify(final UnaryFunction<Double> function) {

        final JamaMatrix retVal = this.copy();

        retVal.modifyAll(function);

        return retVal;
    }

    public void modifyAll(final UnaryFunction<Double> function) {
        ArrayUtils.modifyAll(myDelegate.getArray(), function);
    }

    public void modifyColumn(final long row, final long column, final UnaryFunction<Double> function) {
        ArrayUtils.modifyColumn(myDelegate.getArray(), (int) row, (int) column, function);
    }

    public void modifyDiagonal(final long row, final long column, final UnaryFunction<Double> function) {

        final long tmpCount = Math.min(myDelegate.getRowDimension() - row, myDelegate.getColumnDimension() - column);

        final int tmpFirst = (int) (row + (column * myDelegate.getRowDimension()));
        final int tmpLimit = (int) (row + tmpCount + ((column + tmpCount) * myDelegate.getRowDimension()));
        final int tmpStep = 1 + myDelegate.getRowDimension();

        for (int ij = tmpFirst; ij < tmpLimit; ij += tmpStep) {
            this.set(ij, function.invoke(this.doubleValue(ij)));
        }

    }

    public void modifyOne(final long row, final long column, final UnaryFunction<Double> function) {

        double tmpValue = this.doubleValue(row, column);

        tmpValue = function.invoke(tmpValue);

        this.set(row, column, tmpValue);
    }

    public void modifyRange(final long first, final long limit, final UnaryFunction<Double> function) {
        for (long index = first; index < limit; index++) {
            this.set(index, function.invoke(this.doubleValue(index)));
        }
    }

    public void modifyRow(final long row, final long column, final UnaryFunction<Double> function) {
        ArrayUtils.modifyRow(myDelegate.getArray(), (int) row, (int) column, function);
    }

    public JamaMatrix multiply(final Number aNmbr) {
        return new JamaMatrix(myDelegate.times(aNmbr.doubleValue()));
    }

    public JamaMatrix multiplyElements(final Access2D<?> aMtrx) {
        return new JamaMatrix(myDelegate.arrayTimes(JamaMatrix.convert(aMtrx)));
    }

    public JamaMatrix multiplyLeft(final Access1D<Double> leftMtrx) {
        return new JamaMatrix(JamaMatrix.convert(leftMtrx, (int) (leftMtrx.count() / this.getRowDim())).times(myDelegate));
    }

    public JamaMatrix multiplyLeft(final Access2D<?> aMtrx) {
        return new JamaMatrix(JamaMatrix.convert(aMtrx).times(myDelegate));
    }

    public JamaMatrix multiplyRight(final Access1D<Double> rightMtrx) {
        return new JamaMatrix(myDelegate.times(JamaMatrix.convert(rightMtrx, this.getColDim())));
    }

    public JamaMatrix multiplyRight(final Access2D<?> aMtrx) {
        return new JamaMatrix(myDelegate.times(JamaMatrix.convert(aMtrx)));
    }

    public PrimitiveScalar multiplyVectors(final Access2D<?> aVctr) {

        double retVal = ZERO;

        final int tmpSize = this.size();
        for (int i = 0; i < tmpSize; i++) {
            retVal += this.doubleValue(i) * aVctr.doubleValue(i);
        }

        return new PrimitiveScalar(retVal);
    }

    public JamaMatrix negate() {
        return new JamaMatrix(myDelegate.uminus());
    }

    public void raxpy(final Double aSclrA, final int aRowX, final int aRowY, final int aFirstCol) {

        final double tmpValA = aSclrA.doubleValue();
        final double[][] tmpArray = myDelegate.getArray();

        final int tmpColDim = myDelegate.getColumnDimension();

        for (int j = aFirstCol; j < tmpColDim; j++) {
            tmpArray[aRowY][j] += tmpValA * tmpArray[aRowX][j];

        }
    }

    public JamaMatrix round(final NumberContext aCntxt) {

        final double[][] retVal = myDelegate.getArrayCopy();

        ArrayUtils.modifyAll(retVal, aCntxt.getPrimitiveRoundFunction());

        return new JamaMatrix(retVal);
    }

    public JamaMatrix scale(final Double scalar) {
        return new JamaMatrix(myDelegate.times(scalar.doubleValue()));
    }

    public JamaMatrix selectColumns(final int... someCols) {
        return new JamaMatrix(myDelegate.getMatrix(AccessUtils.makeIncreasingRange(0, this.getRowDim()), someCols));
    }

    public JamaMatrix selectRows(final int... someRows) {
        return new JamaMatrix(myDelegate.getMatrix(someRows, AccessUtils.makeIncreasingRange(0, this.getColDim())));
    }

    public Double set(final int anInd, final Number value) {
        final double retVal = myDelegate.get(AccessUtils.row(anInd, myDelegate.getRowDimension()), AccessUtils.column(anInd, myDelegate.getRowDimension()));
        myDelegate.set(AccessUtils.row(anInd, myDelegate.getRowDimension()), AccessUtils.column(anInd, myDelegate.getRowDimension()), value.doubleValue());
        return retVal;
    }

    public void set(final long index, final double value) {
        myDelegate.set(AccessUtils.row(index, myDelegate.getRowDimension()), AccessUtils.column(index, myDelegate.getRowDimension()), value);
    }

    public void set(final long row, final long column, final double aNmbr) {
        myDelegate.set((int) row, (int) column, aNmbr);
    }

    public void set(final long row, final long column, final Number aNmbr) {
        myDelegate.set((int) row, (int) column, aNmbr.doubleValue());
    }

    public void set(final long index, final Number value) {
        myDelegate.set(AccessUtils.row(index, myDelegate.getRowDimension()), AccessUtils.column(index, myDelegate.getRowDimension()), value.doubleValue());
    }

    public int size() {
        return myDelegate.getRowDimension() * myDelegate.getColumnDimension();
    }

    public JamaMatrix solve(final Access2D<?> aRHS) {

        Matrix retVal = JamaMatrix.convert(aRHS);

        try {
            if (this.isTall()) {
                retVal = new QRDecomposition(myDelegate).solve(retVal);
            } else {
                retVal = new LUDecomposition(myDelegate).solve(retVal);
            }
        } catch (final RuntimeException anRE) {
            final JamaSingularValue tmpMD = new JamaSingularValue();
            tmpMD.compute(myDelegate);
            retVal = tmpMD.solve(new JamaMatrix(retVal)).getDelegate();
        }

        return new JamaMatrix(retVal);
    }

    public JamaMatrix subtract(final Access2D<?> aMtrx) {
        return new JamaMatrix(myDelegate.minus(JamaMatrix.convert(aMtrx)));
    }

    public MatrixStore<Double> subtract(final MatrixStore<Double> subtrahend) {
        return this.add(subtrahend.negate());
    }

    public JamaMatrix subtract(final Number value) {

        final double[][] retVal = myDelegate.getArrayCopy();

        ArrayUtils.modifyAll(retVal, SUBTRACT.second(value.doubleValue()));

        return new JamaMatrix(retVal);
    }

    public BigDecimal toBigDecimal(final int row, final int column) {
        return new BigDecimal(myDelegate.get(row, column));
    }

    public PhysicalStore<BigDecimal> toBigStore() {
        return BigDenseStore.FACTORY.copy(this);
    }

    public ComplexNumber toComplexNumber(final int row, final int column) {
        return ComplexNumber.makeReal(myDelegate.get(row, column));
    }

    public PhysicalStore<ComplexNumber> toComplexStore() {
        return ComplexDenseStore.FACTORY.copy(this);
    }

    public List<BasicMatrix<Double>> toListOfColumns() {

        final int tmpColDim = this.getColDim();

        final List<BasicMatrix<Double>> retVal = new ArrayList<BasicMatrix<Double>>(tmpColDim);

        for (int j = 0; j < tmpColDim; j++) {
            retVal.add(j, this.selectColumns(new int[] { j }));
        }

        return retVal;
    }

    public List<Double> toListOfElements() {
        return this.toPrimitiveStore().asList();
    }

    public List<BasicMatrix<Double>> toListOfRows() {

        final int tmpRowDim = this.getRowDim();

        final List<BasicMatrix<Double>> retVal = new ArrayList<BasicMatrix<Double>>(tmpRowDim);

        for (int i = 0; i < tmpRowDim; i++) {
            retVal.add(i, this.selectRows(new int[] { i }));
        }

        return retVal;
    }

    public JamaMatrix toPrimitiveStore() {
        return new JamaMatrix(myDelegate.getArrayCopy());
    }

    public PrimitiveScalar toScalar(final long row, final long column) {
        return new PrimitiveScalar(myDelegate.get((int) row, (int) column));
    }

    @Override
    public String toString() {
        return MatrixUtils.toString(this);
    }

    public String toString(final int row, final int column) {
        return Double.toString(myDelegate.get(row, column));
    }

    public void transformLeft(final Householder<Double> transformation, final int firstColumn) {

        final double[][] tmpArray = myDelegate.getArray();
        final int tmpRowDim = myDelegate.getRowDimension();
        final int tmpColDim = myDelegate.getColumnDimension();

        final int tmpFirst = transformation.first();

        final double[] tmpWorkCopy = new double[(int) transformation.count()];

        double tmpScale;
        for (int j = firstColumn; j < tmpColDim; j++) {
            tmpScale = ZERO;
            for (int i = tmpFirst; i < tmpRowDim; i++) {
                tmpScale += tmpWorkCopy[i] * tmpArray[i][j];
            }
            double tmpVal, tmpVal2 = PrimitiveMath.ZERO;
            final int tmpSize = (int) transformation.count();
            for (int i1 = transformation.first(); i1 < tmpSize; i1++) {
                tmpVal = transformation.doubleValue(i1);
                tmpVal2 += tmpVal * tmpVal;
                tmpWorkCopy[i1] = tmpVal;
            }
            tmpScale *= PrimitiveMath.TWO / tmpVal2;
            for (int i = tmpFirst; i < tmpRowDim; i++) {
                tmpArray[i][j] -= tmpScale * tmpWorkCopy[i];
            }
        }
    }

    public void transformLeft(final Rotation<Double> transformation) {

        final Rotation.Primitive tmpTransf = JamaMatrix.cast(transformation);

        final int tmpLow = tmpTransf.low;
        final int tmpHigh = tmpTransf.high;

        if (tmpLow != tmpHigh) {
            if (!Double.isNaN(tmpTransf.cos) && !Double.isNaN(tmpTransf.sin)) {

                final double[][] tmpArray = myDelegate.getArray();
                double tmpOldLow;
                double tmpOldHigh;

                for (int j = 0; j < tmpArray[0].length; j++) {

                    tmpOldLow = tmpArray[tmpLow][j];
                    tmpOldHigh = tmpArray[tmpHigh][j];

                    tmpArray[tmpLow][j] = (tmpTransf.cos * tmpOldLow) + (tmpTransf.sin * tmpOldHigh);
                    tmpArray[tmpHigh][j] = (tmpTransf.cos * tmpOldHigh) - (tmpTransf.sin * tmpOldLow);
                }
            } else {
                this.exchangeRows(tmpLow, tmpHigh);
            }
        } else {
            if (!Double.isNaN(tmpTransf.cos)) {
                this.modifyRow(tmpLow, 0, MULTIPLY.second(tmpTransf.cos));
            } else if (!Double.isNaN(tmpTransf.sin)) {
                this.modifyRow(tmpLow, 0, DIVIDE.second(tmpTransf.sin));
            } else {
                this.modifyRow(tmpLow, 0, NEGATE);
            }
        }
    }

    public void transformRight(final Householder<Double> transformation, final int firstRow) {

        final double[][] tmpArray = myDelegate.getArray();
        final int tmpRowDim = myDelegate.getRowDimension();
        final int tmpColDim = myDelegate.getColumnDimension();

        final int tmpFirst = transformation.first();

        final double[] tmpWorkCopy = new double[(int) transformation.count()];

        double tmpScale;
        for (int i = firstRow; i < tmpRowDim; i++) {
            tmpScale = ZERO;
            for (int j = tmpFirst; j < tmpColDim; j++) {
                tmpScale += tmpWorkCopy[j] * tmpArray[i][j];
            }
            double tmpVal, tmpVal2 = PrimitiveMath.ZERO;
            final int tmpSize = (int) transformation.count();
            for (int i1 = transformation.first(); i1 < tmpSize; i1++) {
                tmpVal = transformation.doubleValue(i1);
                tmpVal2 += tmpVal * tmpVal;
                tmpWorkCopy[i1] = tmpVal;
            }
            tmpScale *= PrimitiveMath.TWO / tmpVal2;
            for (int j = tmpFirst; j < tmpColDim; j++) {
                tmpArray[i][j] -= tmpScale * tmpWorkCopy[j];
            }
        }
    }

    public void transformRight(final Rotation<Double> transformation) {

        final Rotation.Primitive tmpTransf = JamaMatrix.cast(transformation);

        final int tmpLow = tmpTransf.low;
        final int tmpHigh = tmpTransf.high;

        if (tmpLow != tmpHigh) {
            if (!Double.isNaN(tmpTransf.cos) && !Double.isNaN(tmpTransf.sin)) {

                final double[][] tmpArray = myDelegate.getArray();
                double tmpOldLow;
                double tmpOldHigh;

                for (int i = 0; i < tmpArray.length; i++) {

                    tmpOldLow = tmpArray[i][tmpLow];
                    tmpOldHigh = tmpArray[i][tmpHigh];

                    tmpArray[i][tmpLow] = (tmpTransf.cos * tmpOldLow) - (tmpTransf.sin * tmpOldHigh);
                    tmpArray[i][tmpHigh] = (tmpTransf.cos * tmpOldHigh) + (tmpTransf.sin * tmpOldLow);
                }
            } else {
                this.exchangeColumns(tmpLow, tmpHigh);
            }
        } else {
            if (!Double.isNaN(tmpTransf.cos)) {
                this.modifyColumn(0, tmpHigh, MULTIPLY.second(tmpTransf.cos));
            } else if (!Double.isNaN(tmpTransf.sin)) {
                this.modifyColumn(0, tmpHigh, DIVIDE.second(tmpTransf.sin));
            } else {
                this.modifyColumn(0, tmpHigh, NEGATE);
            }
        }
    }

    public JamaMatrix transpose() {
        return new JamaMatrix(myDelegate.transpose());
    }

    public final void update(final int aFirstRow, final int aRowCount, final int aFirstCol, final int aColCount, final JamaMatrix aMtrx) {
        myDelegate.setMatrix(aFirstRow, aRowCount - aFirstRow - 1, aFirstCol, aColCount - aFirstCol - 1, aMtrx.getDelegate());
    }

    public final void update(final int aFirstRow, final int aRowCount, final int[] someColumns, final JamaMatrix aMtrx) {
        myDelegate.setMatrix(aFirstRow, aRowCount - aFirstRow - 1, someColumns, aMtrx.getDelegate());
    }

    public final void update(final int row, final int column, final Number aNmbr) {
        myDelegate.set(row, column, aNmbr.doubleValue());
    }

    public final void update(final int[] someRows, final int aFirstCol, final int aColCount, final JamaMatrix aMtrx) {
        myDelegate.setMatrix(someRows, aFirstCol, aColCount - aFirstCol - 1, aMtrx.getDelegate());
    }

    public final void update(final int[] someRows, final int[] someColumns, final JamaMatrix aMtrx) {
        myDelegate.setMatrix(someRows, someColumns, aMtrx.getDelegate());
    }

    public void visitAll(final VoidFunction<Double> visitor) {
        ArrayUtils.visitAll(myDelegate.getArray(), visitor);
    }

    public void visitColumn(final long row, final long column, final VoidFunction<Double> visitor) {
        ArrayUtils.visitColumn(myDelegate.getArray(), (int) row, (int) column, visitor);
    }

    public void visitDiagonal(final long row, final long column, final VoidFunction<Double> visitor) {
        ArrayUtils.visitDiagonal(myDelegate.getArray(), (int) row, (int) column, visitor);
    }

    public void visitRange(final long first, final long limit, final VoidFunction<Double> visitor) {
        ArrayUtils.visitRange(myDelegate.getArray(), (int) first, (int) limit, visitor);
    }

    public void visitRow(final long row, final long column, final VoidFunction<Double> visitor) {
        ArrayUtils.visitRow(myDelegate.getArray(), (int) row, (int) column, visitor);
    }

    final JamaCholesky getCholeskyDecomposition() {
        final JamaCholesky retVal = new JamaCholesky();
        retVal.compute(myDelegate);
        return retVal;
    }

    final Matrix getDelegate() {
        return myDelegate;
    }

    final JamaEigenvalue getEigenvalueDecomposition() {
        final JamaEigenvalue retVal = MatrixUtils.isHermitian(this) ? new JamaEigenvalue.Symmetric() : new JamaEigenvalue.Nonsymmetric();
        retVal.compute(myDelegate);
        return retVal;
    }

    final JamaLU getLUDecomposition() {
        final JamaLU retVal = new JamaLU();
        retVal.compute(myDelegate);
        return retVal;
    }

    final JamaQR getQRDecomposition() {
        final JamaQR retVal = new JamaQR();
        retVal.compute(myDelegate);
        return retVal;
    }

    final JamaSingularValue getSingularValueDecomposition() {
        final JamaSingularValue retVal = new JamaSingularValue();
        retVal.compute(myDelegate);
        return retVal;
    }
}
