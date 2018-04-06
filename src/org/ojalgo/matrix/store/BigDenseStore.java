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
package org.ojalgo.matrix.store;

import static org.ojalgo.constant.BigMath.*;
import static org.ojalgo.function.BigFunction.*;

import java.math.BigDecimal;
import java.util.List;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.Array2D;
import org.ojalgo.array.BasicArray;
import org.ojalgo.array.BigArray;
import org.ojalgo.array.DenseArray;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.constant.BigMath;
import org.ojalgo.function.BigFunction;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.FunctionUtils;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.BigAggregator;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.store.operation.*;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.HouseholderReference;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.BigScalar;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * A {@linkplain BigDecimal} implementation of {@linkplain PhysicalStore}.
 *
 * @author apete
 * @deprecated v45 Use {@link GenericDenseStore} instead
 */
@Deprecated
public final class BigDenseStore extends BigArray implements PhysicalStore<BigDecimal>, DecompositionStore<BigDecimal> {

    public static interface BigMultiplyBoth extends ElementsConsumer.FillByMultiplying<BigDecimal> {

    }

    public static interface BigMultiplyLeft {

        void invoke(BigDecimal[] product, Access1D<BigDecimal> left, int complexity, BigDecimal[] right);

    }

    public static interface BigMultiplyNeither {

        void invoke(BigDecimal[] product, BigDecimal[] left, int complexity, BigDecimal[] right);

    }

    public static interface BigMultiplyRight {

        void invoke(BigDecimal[] product, BigDecimal[] left, int complexity, Access1D<BigDecimal> right);

    }

    /**
     * @deprecated v45 Use {@link GenericDenseStore#RATIONAL} instead
     */
    @Deprecated
    public static final PhysicalStore.Factory<BigDecimal, BigDenseStore> FACTORY = new PhysicalStore.Factory<BigDecimal, BigDenseStore>() {

        public AggregatorSet<BigDecimal> aggregator() {
            return BigAggregator.getSet();
        }

        public DenseArray.Factory<BigDecimal> array() {
            return BigArray.FACTORY;
        }

        public MatrixStore.Factory<BigDecimal> builder() {
            return MatrixStore.BIG;
        }

        public BigDenseStore columns(final Access1D<?>... source) {

            final int tmpRowDim = (int) source[0].count();
            final int tmpColDim = source.length;

            final BigDecimal[] tmpData = new BigDecimal[tmpRowDim * tmpColDim];

            Access1D<?> tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = TypeUtils.toBigDecimal(tmpColumn.get(i));
                }
            }

            return new BigDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public BigDenseStore columns(final double[]... source) {

            final int tmpRowDim = source[0].length;
            final int tmpColDim = source.length;

            final BigDecimal[] tmpData = new BigDecimal[tmpRowDim * tmpColDim];

            double[] tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = TypeUtils.toBigDecimal(tmpColumn[i]);
                }
            }

            return new BigDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public BigDenseStore columns(final List<? extends Number>... source) {

            final int tmpRowDim = source[0].size();
            final int tmpColDim = source.length;

            final BigDecimal[] tmpData = new BigDecimal[tmpRowDim * tmpColDim];

            List<? extends Number> tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = TypeUtils.toBigDecimal(tmpColumn.get(i));
                }
            }

            return new BigDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public BigDenseStore columns(final Number[]... source) {

            final int tmpRowDim = source[0].length;
            final int tmpColDim = source.length;

            final BigDecimal[] tmpData = new BigDecimal[tmpRowDim * tmpColDim];

            Number[] tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = TypeUtils.toBigDecimal(tmpColumn[i]);
                }
            }

            return new BigDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public BigDenseStore conjugate(final Access2D<?> source) {
            return this.transpose(source);
        }

        public BigDenseStore copy(final Access2D<?> source) {

            final int tmpRowDim = (int) source.countRows();
            final int tmpColDim = (int) source.countColumns();

            final BigDenseStore retVal = new BigDenseStore(tmpRowDim, tmpColDim);

            if (tmpColDim > FillMatchingSingle.THRESHOLD) {

                final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                    @Override
                    public void conquer(final int aFirst, final int aLimit) {
                        FillMatchingSingle.copy(retVal.data, tmpRowDim, aFirst, aLimit, source);
                    }

                };

                tmpConquerer.invoke(0, tmpColDim, FillMatchingSingle.THRESHOLD);

            } else {

                FillMatchingSingle.copy(retVal.data, tmpRowDim, 0, tmpColDim, source);
            }

            return retVal;
        }

        public FunctionSet<BigDecimal> function() {
            return BigFunction.getSet();
        }

        public BigDenseStore makeEye(final long rows, final long columns) {

            final BigDenseStore retVal = this.makeZero(rows, columns);

            retVal.myUtility.fillDiagonal(0, 0, BigMath.ONE);

            return retVal;
        }

        public BigDenseStore makeFilled(final long rows, final long columns, final NullaryFunction<?> supplier) {

            final int tmpRowDim = (int) rows;
            final int tmpColDim = (int) columns;

            final int tmpLength = tmpRowDim * tmpColDim;

            final BigDecimal[] tmpData = new BigDecimal[tmpLength];

            for (int i = 0; i < tmpLength; i++) {
                tmpData[i] = TypeUtils.toBigDecimal(supplier.get());
            }

            return new BigDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public Householder.Big makeHouseholder(final int length) {
            return new Householder.Big(length);
        }

        public Rotation.Big makeRotation(final int low, final int high, final BigDecimal cos, final BigDecimal sin) {
            return new Rotation.Big(low, high, cos, sin);
        }

        public Rotation.Big makeRotation(final int low, final int high, final double cos, final double sin) {
            return this.makeRotation(low, high, new BigDecimal(cos), new BigDecimal(sin));
        }

        public BigDenseStore makeZero(final long rows, final long columns) {
            return new BigDenseStore((int) rows, (int) columns);
        }

        public BigDenseStore rows(final Access1D<?>... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = (int) source[0].count();

            final BigDecimal[] tmpData = new BigDecimal[tmpRowDim * tmpColDim];

            Access1D<?> tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = TypeUtils.toBigDecimal(tmpRow.get(j));
                }
            }

            return new BigDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public BigDenseStore rows(final double[]... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].length;

            final BigDecimal[] tmpData = new BigDecimal[tmpRowDim * tmpColDim];

            double[] tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = TypeUtils.toBigDecimal(tmpRow[j]);
                }
            }

            return new BigDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public BigDenseStore rows(final List<? extends Number>... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].size();

            final BigDecimal[] tmpData = new BigDecimal[tmpRowDim * tmpColDim];

            List<? extends Number> tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = TypeUtils.toBigDecimal(tmpRow.get(j));
                }
            }

            return new BigDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public BigDenseStore rows(final Number[]... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].length;

            final BigDecimal[] tmpData = new BigDecimal[tmpRowDim * tmpColDim];

            Number[] tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = TypeUtils.toBigDecimal(tmpRow[j]);
                }
            }

            return new BigDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public Scalar.Factory<BigDecimal> scalar() {
            return BigScalar.FACTORY;
        }

        public BigDenseStore transpose(final Access2D<?> source) {

            final BigDenseStore retVal = new BigDenseStore((int) source.countColumns(), (int) source.countRows());

            final int tmpRowDim = retVal.getRowDim();
            final int tmpColDim = retVal.getColDim();

            if (tmpColDim > FillMatchingSingle.THRESHOLD) {

                final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                    @Override
                    public void conquer(final int aFirst, final int aLimit) {
                        FillMatchingSingle.transpose(retVal.data, tmpRowDim, aFirst, aLimit, source);
                    }

                };

                tmpConquerer.invoke(0, tmpColDim, FillMatchingSingle.THRESHOLD);

            } else {

                FillMatchingSingle.transpose(retVal.data, tmpRowDim, 0, tmpColDim, source);
            }

            return retVal;
        }

    };

    static BigDenseStore cast(final Access1D<BigDecimal> matrix) {
        if (matrix instanceof BigDenseStore) {
            return (BigDenseStore) matrix;
        } else if (matrix instanceof Access2D<?>) {
            return FACTORY.copy((Access2D<?>) matrix);
        } else {
            return FACTORY.columns(matrix);
        }
    }

    static Householder.Big cast(final Householder<BigDecimal> transformation) {
        if (transformation instanceof Householder.Big) {
            return (Householder.Big) transformation;
        } else if (transformation instanceof HouseholderReference<?>) {
            return ((Householder.Big) ((HouseholderReference<BigDecimal>) transformation).getWorker(FACTORY)).copy(transformation);
        } else {
            return new Householder.Big(transformation);
        }
    }

    static Rotation.Big cast(final Rotation<BigDecimal> transformation) {
        if (transformation instanceof Rotation.Big) {
            return (Rotation.Big) transformation;
        } else {
            return new Rotation.Big(transformation);
        }
    }

    private final BigMultiplyBoth multiplyBoth;
    private final BigMultiplyLeft multiplyLeft;
    private final BigMultiplyNeither multiplyNeither;
    private final BigMultiplyRight multiplyRight;
    private final int myColDim;
    private final int myRowDim;
    private final Array2D<BigDecimal> myUtility;

    BigDenseStore(final BigDecimal[] anArray) {

        super(anArray);

        myRowDim = anArray.length;
        myColDim = 1;

        myUtility = this.wrapInArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.getBig(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.getBig(myRowDim, myColDim);
        multiplyRight = MultiplyRight.getBig(myRowDim, myColDim);
        multiplyNeither = MultiplyNeither.getBig(myRowDim, myColDim);
    }

    BigDenseStore(final int aLength) {

        super(aLength);

        myRowDim = aLength;
        myColDim = 1;

        myUtility = this.wrapInArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.getBig(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.getBig(myRowDim, myColDim);
        multiplyRight = MultiplyRight.getBig(myRowDim, myColDim);
        multiplyNeither = MultiplyNeither.getBig(myRowDim, myColDim);
    }

    BigDenseStore(final int aRowDim, final int aColDim) {

        super(aRowDim * aColDim);

        myRowDim = aRowDim;
        myColDim = aColDim;

        myUtility = this.wrapInArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.getBig(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.getBig(myRowDim, myColDim);
        multiplyRight = MultiplyRight.getBig(myRowDim, myColDim);
        multiplyNeither = MultiplyNeither.getBig(myRowDim, myColDim);
    }

    BigDenseStore(final int aRowDim, final int aColDim, final BigDecimal[] anArray) {

        super(anArray);

        myRowDim = aRowDim;
        myColDim = aColDim;

        myUtility = this.wrapInArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.getBig(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.getBig(myRowDim, myColDim);
        multiplyRight = MultiplyRight.getBig(myRowDim, myColDim);
        multiplyNeither = MultiplyNeither.getBig(myRowDim, myColDim);
    }

    public void accept(final Access2D<?> supplied) {
        for (long j = 0L; j < supplied.countColumns(); j++) {
            for (long i = 0L; i < supplied.countRows(); i++) {
                this.set(i, j, supplied.get(i, j));
            }
        }
    }

    public void add(final long row, final long col, final double addend) {
        myUtility.add(row, col, addend);
    }

    public void add(final long row, final long col, final Number addend) {
        myUtility.add(row, col, addend);
    }

    public BigDecimal aggregateAll(final Aggregator aggregator) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        final AggregatorFunction<BigDecimal> mainAggr = aggregator.getFunction(BigAggregator.getSet());

        if (mainAggr.isMergeable() && tmpColDim > AggregateAll.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {

                    final AggregatorFunction<BigDecimal> tmpPartAggr = aggregator.getFunction(BigAggregator.getSet());

                    BigDenseStore.this.visit(tmpRowDim * aFirst, tmpRowDim * aLimit, 1, tmpPartAggr);

                    synchronized (mainAggr) {
                        mainAggr.merge(tmpPartAggr.get());
                    }
                }
            };

            tmpConquerer.invoke(0, tmpColDim, AggregateAll.THRESHOLD);

        } else {

            BigDenseStore.this.visit(0, this.size(), 1, mainAggr);
        }

        return mainAggr.get();
    }

    public void applyCholesky(final int iterationPoint, final BasicArray<BigDecimal> multipliers) {

        final BigDecimal[] tmpData = data;
        final BigDecimal[] tmpColumn = ((BigArray) multipliers).data;

        if ((myColDim - iterationPoint - 1) > ApplyCholesky.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int aFirst, final int aLimit) {
                    ApplyCholesky.invoke(tmpData, myRowDim, aFirst, aLimit, tmpColumn);
                }
            };

            tmpConquerer.invoke(iterationPoint + 1, myColDim, ApplyCholesky.THRESHOLD);

        } else {

            ApplyCholesky.invoke(tmpData, myRowDim, iterationPoint + 1, myColDim, tmpColumn);
        }
    }

    public void applyLDL(final int iterationPoint, final BasicArray<BigDecimal> multipliers) {

        final BigDecimal[] tmpData = data;
        final BigDecimal[] tmpColumn = ((BigArray) multipliers).data;

        if ((myColDim - iterationPoint - 1) > ApplyLDL.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    ApplyLDL.invoke(tmpData, myRowDim, first, limit, tmpColumn, iterationPoint);
                }
            };

            tmpConquerer.invoke(iterationPoint + 1, myColDim, ApplyLDL.THRESHOLD);

        } else {

            ApplyLDL.invoke(tmpData, myRowDim, iterationPoint + 1, myColDim, tmpColumn, iterationPoint);
        }
    }

    public void applyLU(final int iterationPoint, final BasicArray<BigDecimal> multipliers) {

        final BigDecimal[] tmpData = data;
        final BigDecimal[] tmpColumn = ((BigArray) multipliers).data;

        if ((myColDim - iterationPoint - 1) > ApplyLU.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int aFirst, final int aLimit) {
                    ApplyLU.invoke(tmpData, myRowDim, aFirst, aLimit, tmpColumn, iterationPoint);
                }
            };

            tmpConquerer.invoke(iterationPoint + 1, myColDim, ApplyLU.THRESHOLD);

        } else {

            ApplyLU.invoke(tmpData, myRowDim, iterationPoint + 1, myColDim, tmpColumn, iterationPoint);
        }
    }

    public Array1D<BigDecimal> asList() {
        return myUtility.asArray1D();
    }

    public Array1D<ComplexNumber> computeInPlaceSchur(final PhysicalStore<BigDecimal> transformationCollector, final boolean eigenvalue) {
        ProgrammingError.throwForUnsupportedOptionalOperation();
        return null;
    }

    public MatrixStore<BigDecimal> conjugate() {
        return this.transpose();
    }

    public BigDenseStore copy() {
        return new BigDenseStore(myRowDim, myColDim, this.copyOfData());
    }

    public long countColumns() {
        return myColDim;
    }

    public long countRows() {
        return myRowDim;
    }

    public void divideAndCopyColumn(final int row, final int column, final BasicArray<BigDecimal> destination) {

        final BigDecimal[] tmpData = data;
        final int tmpRowDim = myRowDim;

        final BigDecimal[] tmpDestination = ((BigArray) destination).data;

        int tmpIndex = row + (column * tmpRowDim);
        final BigDecimal tmpDenominator = tmpData[tmpIndex];

        for (int i = row + 1; i < tmpRowDim; i++) {
            tmpIndex++;
            tmpDestination[i] = tmpData[tmpIndex] = BigFunction.DIVIDE.invoke(tmpData[tmpIndex], tmpDenominator);
        }
    }

    public double doubleValue(final long aRow, final long aCol) {
        return myUtility.doubleValue(aRow, aCol);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object anObj) {
        if (anObj instanceof MatrixStore) {
            return this.equals((MatrixStore<BigDecimal>) anObj, NumberContext.getGeneral(6));
        } else {
            return super.equals(anObj);
        }
    }

    public void exchangeColumns(final long colA, final long colB) {
        myUtility.exchangeColumns(colA, colB);
    }

    public void exchangeHermitian(final int indexA, final int indexB) {

        final int tmpMin = Math.min(indexA, indexB);
        final int tmpMax = Math.max(indexA, indexB);

        BigDecimal tmpVal;
        for (int j = 0; j < tmpMin; j++) {
            tmpVal = this.get(tmpMin, j);
            this.set(tmpMin, j, this.get(tmpMax, j));
            this.set(tmpMax, j, tmpVal);
        }

        tmpVal = this.get(tmpMin, tmpMin);
        this.set(tmpMin, tmpMin, this.get(tmpMax, tmpMax));
        this.set(tmpMax, tmpMax, tmpVal);

        for (int ij = tmpMin + 1; ij < tmpMax; ij++) {
            tmpVal = this.get(ij, tmpMin);
            this.set(ij, tmpMin, this.get(tmpMax, ij));
            this.set(tmpMax, ij, tmpVal);
        }

        for (int i = tmpMax + 1; i < myRowDim; i++) {
            tmpVal = this.get(i, tmpMin);
            this.set(i, tmpMin, this.get(i, tmpMax));
            this.set(i, tmpMax, tmpVal);
        }
    }

    public void exchangeRows(final long rowA, final long rowB) {
        myUtility.exchangeRows(rowA, rowB);
    }

    public void fillByMultiplying(final Access1D<BigDecimal> left, final Access1D<BigDecimal> right) {

        final int complexity = ((int) left.count()) / myRowDim;

        if (left instanceof BigDenseStore) {
            if (right instanceof BigDenseStore) {
                multiplyNeither.invoke(data, BigDenseStore.cast(left).data, complexity, BigDenseStore.cast(right).data);
            } else {
                multiplyRight.invoke(data, BigDenseStore.cast(left).data, complexity, right);
            }
        } else {
            if (right instanceof BigDenseStore) {
                multiplyLeft.invoke(data, left, complexity, BigDenseStore.cast(right).data);
            } else {
                multiplyBoth.invoke(this, left, complexity, right);
            }
        }
    }

    public void fillColumn(final long row, final long col, final Access1D<BigDecimal> values) {
        myUtility.fillColumn(row, col, values);
    }

    public void fillColumn(final long row, final long col, final BigDecimal value) {
        myUtility.fillColumn(row, col, value);
    }

    public void fillColumn(final long row, final long col, final NullaryFunction<BigDecimal> supplier) {
        myUtility.fillColumn(row, col, supplier);
    }

    public void fillDiagonal(final long row, final long col, final BigDecimal value) {
        myUtility.fillDiagonal(row, col, value);
    }

    public void fillDiagonal(final long row, final long col, final NullaryFunction<BigDecimal> supplier) {
        myUtility.fillDiagonal(row, col, supplier);
    }

    public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
        this.set(row, col, values.get(valueIndex));
    }

    public void fillOne(final long row, final long col, final BigDecimal value) {
        myUtility.fillOne(row, col, value);
    }

    public void fillOne(final long row, final long col, final NullaryFunction<BigDecimal> supplier) {
        myUtility.fillOne(row, col, supplier);
    }

    public void fillRow(final long row, final long col, final Access1D<BigDecimal> values) {
        myUtility.fillRow(row, col, values);
    }

    public void fillRow(final long row, final long col, final BigDecimal value) {
        myUtility.fillRow(row, col, value);
    }

    public void fillRow(final long row, final long col, final NullaryFunction<BigDecimal> supplier) {
        myUtility.fillRow(row, col, supplier);
    }

    public boolean generateApplyAndCopyHouseholderColumn(final int row, final int column, final Householder<BigDecimal> destination) {
        return GenerateApplyAndCopyHouseholderColumn.invoke(data, myRowDim, row, column, (Householder.Big) destination);
    }

    public boolean generateApplyAndCopyHouseholderRow(final int row, final int column, final Householder<BigDecimal> destination) {
        return GenerateApplyAndCopyHouseholderRow.invoke(data, myRowDim, row, column, (Householder.Big) destination);
    }

    public final MatrixStore<BigDecimal> get() {
        return this;
    }

    public BigDecimal get(final long aRow, final long aCol) {
        return myUtility.get(aRow, aCol);
    }

    @Override
    public int hashCode() {
        return MatrixUtils.hashCode(this);
    }

    public long indexOfLargestInColumn(final long row, final long col) {
        return myUtility.indexOfLargestInColumn(row, col);
    }

    public long indexOfLargestInRow(final long row, final long col) {
        return myUtility.indexOfLargestInRow(row, col);
    }

    public long indexOfLargestOnDiagonal(final long first) {
        return myUtility.indexOfLargestOnDiagonal(first);
    }

    public boolean isAbsolute(final long row, final long col) {
        return myUtility.isAbsolute(row, col);
    }

    public boolean isColumnSmall(final long row, final long col, final double comparedTo) {
        return myUtility.isColumnSmall(row, col, comparedTo);
    }

    public boolean isRowSmall(final long row, final long col, final double comparedTo) {
        return myUtility.isRowSmall(row, col, comparedTo);
    }

    public boolean isSmall(final long row, final long col, final double comparedTo) {
        return myUtility.isSmall(row, col, comparedTo);
    }

    @Override
    public void modifyAll(final UnaryFunction<BigDecimal> aFunc) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > ModifyAll.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    BigDenseStore.this.modify(tmpRowDim * aFirst, tmpRowDim * aLimit, 1, aFunc);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, ModifyAll.THRESHOLD);

        } else {

            this.modify(tmpRowDim * 0, tmpRowDim * tmpColDim, 1, aFunc);
        }
    }

    public void modifyColumn(final long row, final long col, final UnaryFunction<BigDecimal> modifier) {
        myUtility.modifyColumn(row, col, modifier);
    }

    public void modifyDiagonal(final long row, final long col, final UnaryFunction<BigDecimal> modifier) {
        myUtility.modifyDiagonal(row, col, modifier);
    }

    public void modifyMatching(final Access1D<BigDecimal> left, final BinaryFunction<BigDecimal> function) {
        final long tmpLimit = FunctionUtils.min(left.count(), this.count(), this.count());
        for (long i = 0L; i < tmpLimit; i++) {
            this.fillOne(i, function.invoke(left.get(i), this.get(i)));
        }
    }

    public void modifyMatching(final BinaryFunction<BigDecimal> function, final Access1D<BigDecimal> right) {
        final long tmpLimit = FunctionUtils.min(this.count(), right.count(), this.count());
        for (long i = 0L; i < tmpLimit; i++) {
            this.fillOne(i, function.invoke(this.get(i), right.get(i)));
        }
    }

    public void modifyOne(final long row, final long col, final UnaryFunction<BigDecimal> modifier) {

        BigDecimal tmpValue = this.get(row, col);

        tmpValue = modifier.invoke(tmpValue);

        this.set(row, col, tmpValue);
    }

    public void modifyRow(final long row, final long col, final UnaryFunction<BigDecimal> modifier) {
        myUtility.modifyRow(row, col, modifier);
    }

    public MatrixStore<BigDecimal> multiply(final MatrixStore<BigDecimal> right) {

        final BigDenseStore retVal = FACTORY.makeZero(myRowDim, right.count() / myColDim);

        if (right instanceof BigDenseStore) {
            retVal.multiplyNeither.invoke(retVal.data, data, myColDim, BigDenseStore.cast(right).data);
        } else {
            retVal.multiplyRight.invoke(retVal.data, data, myColDim, right);
        }

        return retVal;
    }

    public BigDecimal multiplyBoth(final Access1D<BigDecimal> leftAndRight) {

        final PhysicalStore<BigDecimal> tmpStep1 = FACTORY.makeZero(1L, leftAndRight.count());
        final PhysicalStore<BigDecimal> tmpStep2 = FACTORY.makeZero(1L, 1L);

        tmpStep1.fillByMultiplying(leftAndRight, this);
        tmpStep2.fillByMultiplying(tmpStep1, leftAndRight);

        return tmpStep2.get(0L);
    }

    public void negateColumn(final int column) {
        myUtility.modifyColumn(0, column, BigFunction.NEGATE);
    }

    public PhysicalStore.Factory<BigDecimal, BigDenseStore> physical() {
        return FACTORY;
    }

    public final ElementsConsumer<BigDecimal> regionByColumns(final int... columns) {
        return new ElementsConsumer.ColumnsRegion<>(this, multiplyBoth, columns);
    }

    public final ElementsConsumer<BigDecimal> regionByLimits(final int rowLimit, final int columnLimit) {
        return new ElementsConsumer.LimitRegion<>(this, multiplyBoth, rowLimit, columnLimit);
    }

    public final ElementsConsumer<BigDecimal> regionByOffsets(final int rowOffset, final int columnOffset) {
        return new ElementsConsumer.OffsetRegion<>(this, multiplyBoth, rowOffset, columnOffset);
    }

    public final ElementsConsumer<BigDecimal> regionByRows(final int... rows) {
        return new ElementsConsumer.RowsRegion<>(this, multiplyBoth, rows);
    }

    public final ElementsConsumer<BigDecimal> regionByTransposing() {
        return new ElementsConsumer.TransposedRegion<>(this, multiplyBoth);
    }

    public void rotateRight(final int low, final int high, final double cos, final double sin) {
        RotateRight.invoke(data, myRowDim, low, high, FACTORY.scalar().cast(cos), FACTORY.scalar().cast(sin));
    }

    public void set(final long aRow, final long aCol, final double aNmbr) {
        myUtility.set(aRow, aCol, aNmbr);
    }

    public void set(final long aRow, final long aCol, final Number aNmbr) {
        myUtility.set(aRow, aCol, aNmbr);
    }

    public void setToIdentity(final int aCol) {
        myUtility.set(aCol, aCol, ONE);
        myUtility.fillColumn(aCol + 1, aCol, ZERO);
    }

    public Array1D<BigDecimal> sliceColumn(final long row, final long col) {
        return myUtility.sliceColumn(row, col);
    }

    public Array1D<BigDecimal> sliceDiagonal(final long row, final long col) {
        return myUtility.sliceDiagonal(row, col);
    }

    public Array1D<BigDecimal> sliceRange(final long first, final long limit) {
        return myUtility.sliceRange(first, limit);
    }

    public Array1D<BigDecimal> sliceRow(final long row, final long col) {
        return myUtility.sliceRow(row, col);
    }

    public void substituteBackwards(final Access2D<BigDecimal> body, final boolean unitDiagonal, final boolean conjugated, final boolean hermitian) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > SubstituteBackwards.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    SubstituteBackwards.invoke(BigDenseStore.this.data, tmpRowDim, aFirst, aLimit, body, unitDiagonal, conjugated, hermitian);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, SubstituteBackwards.THRESHOLD);

        } else {

            SubstituteBackwards.invoke(data, tmpRowDim, 0, tmpColDim, body, unitDiagonal, conjugated, hermitian);
        }
    }

    public void substituteForwards(final Access2D<BigDecimal> body, final boolean unitDiagonal, final boolean conjugated, final boolean identity) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > SubstituteForwards.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    SubstituteForwards.invoke(BigDenseStore.this.data, tmpRowDim, aFirst, aLimit, body, unitDiagonal, conjugated, identity);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, SubstituteForwards.THRESHOLD);

        } else {

            SubstituteForwards.invoke(data, tmpRowDim, 0, tmpColDim, body, unitDiagonal, conjugated, identity);
        }
    }

    public void supplyTo(final ElementsConsumer<BigDecimal> receiver) {
        receiver.fillMatching(this);
    }

    public BigScalar toScalar(final long row, final long column) {
        return BigScalar.of(this.get(row, column));
    }

    @Override
    public final String toString() {
        return Access2D.toString(this);
    }

    public void transformLeft(final Householder<BigDecimal> transformation, final int firstColumn) {

        final Householder.Big tmpTransf = BigDenseStore.cast(transformation);

        final BigDecimal[] tmpData = data;

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if ((tmpColDim - firstColumn) > HouseholderLeft.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    HouseholderLeft.invoke(tmpData, tmpRowDim, aFirst, aLimit, tmpTransf);
                }

            };

            tmpConquerer.invoke(firstColumn, tmpColDim, HouseholderLeft.THRESHOLD);

        } else {

            HouseholderLeft.invoke(tmpData, tmpRowDim, firstColumn, tmpColDim, tmpTransf);
        }
    }

    public void transformLeft(final Rotation<BigDecimal> transformation) {

        final Rotation.Big tmpTransf = BigDenseStore.cast(transformation);

        final int tmpLow = tmpTransf.low;
        final int tmpHigh = tmpTransf.high;

        if (tmpLow != tmpHigh) {
            if ((tmpTransf.cos != null) && (tmpTransf.sin != null)) {
                RotateLeft.invoke(data, myRowDim, tmpLow, tmpHigh, tmpTransf.cos, tmpTransf.sin);
            } else {
                myUtility.exchangeRows(tmpLow, tmpHigh);
            }
        } else {
            if (tmpTransf.cos != null) {
                myUtility.modifyRow(tmpLow, 0, MULTIPLY.second(tmpTransf.cos));
            } else if (tmpTransf.sin != null) {
                myUtility.modifyRow(tmpLow, 0, DIVIDE.second(tmpTransf.sin));
            } else {
                myUtility.modifyRow(tmpLow, 0, NEGATE);
            }
        }
    }

    public void transformRight(final Householder<BigDecimal> transformation, final int firstRow) {

        final Householder.Big tmpTransf = BigDenseStore.cast(transformation);

        final BigDecimal[] tmpData = data;

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if ((tmpRowDim - firstRow) > HouseholderRight.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    HouseholderRight.invoke(tmpData, aFirst, aLimit, tmpColDim, tmpTransf);
                }

            };

            tmpConquerer.invoke(firstRow, tmpRowDim, HouseholderRight.THRESHOLD);

        } else {

            HouseholderRight.invoke(tmpData, firstRow, tmpRowDim, tmpColDim, tmpTransf);
        }
    }

    public void transformRight(final Rotation<BigDecimal> transformation) {

        final Rotation.Big tmpTransf = BigDenseStore.cast(transformation);

        final int tmpLow = tmpTransf.low;
        final int tmpHigh = tmpTransf.high;

        if (tmpLow != tmpHigh) {
            if ((tmpTransf.cos != null) && (tmpTransf.sin != null)) {
                RotateRight.invoke(data, myRowDim, tmpLow, tmpHigh, tmpTransf.cos, tmpTransf.sin);
            } else {
                myUtility.exchangeColumns(tmpLow, tmpHigh);
            }
        } else {
            if (tmpTransf.cos != null) {
                myUtility.modifyColumn(0, tmpHigh, MULTIPLY.second(tmpTransf.cos));
            } else if (tmpTransf.sin != null) {
                myUtility.modifyColumn(0, tmpHigh, DIVIDE.second(tmpTransf.sin));
            } else {
                myUtility.modifyColumn(0, tmpHigh, NEGATE);
            }
        }
    }

    public void transformSymmetric(final Householder<BigDecimal> transformation) {
        HouseholderHermitian.invoke(data, BigDenseStore.cast(transformation), new BigDecimal[(int) transformation.count()]);
    }

    public MatrixStore<BigDecimal> transpose() {
        return new TransposedStore<>(this);
    }

    public void tred2(final BasicArray<BigDecimal> mainDiagonal, final BasicArray<BigDecimal> offDiagonal, final boolean yesvecs) {
        ProgrammingError.throwForUnsupportedOptionalOperation();
    }

    public void visitColumn(final long row, final long col, final VoidFunction<BigDecimal> visitor) {
        myUtility.visitColumn(row, col, visitor);
    }

    public void visitDiagonal(final long row, final long col, final VoidFunction<BigDecimal> visitor) {
        myUtility.visitDiagonal(row, col, visitor);
    }

    public void visitRow(final long row, final long col, final VoidFunction<BigDecimal> visitor) {
        myUtility.visitRow(row, col, visitor);
    }

    int getColDim() {
        return myColDim;
    }

    int getMaxDim() {
        return Math.max(myRowDim, myColDim);
    }

    int getMinDim() {
        return Math.min(myRowDim, myColDim);
    }

    int getRowDim() {
        return myRowDim;
    }

}
