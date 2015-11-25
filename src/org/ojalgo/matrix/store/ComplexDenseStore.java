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
package org.ojalgo.matrix.store;

import static org.ojalgo.function.ComplexFunction.*;

import java.util.List;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.Array2D;
import org.ojalgo.array.BasicArray;
import org.ojalgo.array.ComplexArray;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.ComplexFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.ComplexAggregator;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.store.operation.*;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * A {@linkplain ComplexNumber} implementation of {@linkplain PhysicalStore}.
 *
 * @author apete
 */
public final class ComplexDenseStore extends ComplexArray implements PhysicalStore<ComplexNumber>, DecompositionStore<ComplexNumber> {

    public static interface ComplexMultiplyBoth extends FillByMultiplying<ComplexNumber> {

    }

    public static interface ComplexMultiplyLeft {

        void invoke(ComplexNumber[] product, Access1D<ComplexNumber> left, int complexity, ComplexNumber[] right);

    }

    public static interface ComplexMultiplyRight {

        void invoke(ComplexNumber[] product, ComplexNumber[] left, int complexity, Access1D<ComplexNumber> right);

    }

    public static final PhysicalStore.Factory<ComplexNumber, ComplexDenseStore> FACTORY = new PhysicalStore.Factory<ComplexNumber, ComplexDenseStore>() {

        public AggregatorSet<ComplexNumber> aggregator() {
            return ComplexAggregator.getSet();
        }

        public MatrixStore.Factory<ComplexNumber> builder() {
            return MatrixStore.COMPLEX;
        }

        public ComplexDenseStore columns(final Access1D<?>... source) {

            final int tmpRowDim = (int) source[0].count();
            final int tmpColDim = source.length;

            final ComplexNumber[] tmpData = new ComplexNumber[tmpRowDim * tmpColDim];

            Access1D<?> tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = ComplexNumber.valueOf(tmpColumn.get(i));
                }
            }

            return new ComplexDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public ComplexDenseStore columns(final double[]... source) {

            final int tmpRowDim = source[0].length;
            final int tmpColDim = source.length;

            final ComplexNumber[] tmpData = new ComplexNumber[tmpRowDim * tmpColDim];

            double[] tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = ComplexNumber.valueOf((Number) tmpColumn[i]);
                }
            }

            return new ComplexDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public ComplexDenseStore columns(final List<? extends Number>... source) {

            final int tmpRowDim = source[0].size();
            final int tmpColDim = source.length;

            final ComplexNumber[] tmpData = new ComplexNumber[tmpRowDim * tmpColDim];

            List<? extends Number> tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = ComplexNumber.valueOf(tmpColumn.get(i));
                }
            }

            return new ComplexDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public ComplexDenseStore columns(final Number[]... source) {

            final int tmpRowDim = source[0].length;
            final int tmpColDim = source.length;

            final ComplexNumber[] tmpData = new ComplexNumber[tmpRowDim * tmpColDim];

            Number[] tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = ComplexNumber.valueOf(tmpColumn[i]);
                }
            }

            return new ComplexDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public ComplexDenseStore conjugate(final Access2D<?> source) {

            final ComplexDenseStore retVal = new ComplexDenseStore((int) source.countColumns(), (int) source.countRows());

            final int tmpRowDim = retVal.getRowDim();
            final int tmpColDim = retVal.getColDim();

            if (tmpColDim > FillConjugated.THRESHOLD) {

                final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                    @Override
                    public void conquer(final int aFirst, final int aLimit) {
                        FillConjugated.invoke(retVal.data, tmpRowDim, aFirst, aLimit, source);
                    }

                };

                tmpConquerer.invoke(0, tmpColDim, FillConjugated.THRESHOLD);

            } else {

                FillConjugated.invoke(retVal.data, tmpRowDim, 0, tmpColDim, source);
            }

            return retVal;
        }

        public ComplexDenseStore copy(final Access2D<?> source) {

            final int tmpRowDim = (int) source.countRows();
            final int tmpColDim = (int) source.countColumns();

            final ComplexDenseStore retVal = new ComplexDenseStore(tmpRowDim, tmpColDim);

            if (tmpColDim > FillMatchingSingle.THRESHOLD) {

                final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                    @Override
                    public void conquer(final int aFirst, final int aLimit) {
                        FillMatchingSingle.invoke(retVal.data, tmpRowDim, aFirst, aLimit, source);
                    }

                };

                tmpConquerer.invoke(0, tmpColDim, FillMatchingSingle.THRESHOLD);

            } else {

                FillMatchingSingle.invoke(retVal.data, tmpRowDim, 0, tmpColDim, source);
            }

            return retVal;
        }

        public FunctionSet<ComplexNumber> function() {
            return ComplexFunction.getSet();
        }

        public ComplexArray makeArray(final int length) {
            return ComplexArray.make(length);
        }

        public ComplexDenseStore makeEye(final long rows, final long columns) {

            final ComplexDenseStore retVal = this.makeZero(rows, columns);

            retVal.myUtility.fillDiagonal(0, 0, ComplexNumber.ONE);

            return retVal;
        }

        public ComplexDenseStore makeFilled(final long rows, final long columns, final NullaryFunction<?> supplier) {

            final int tmpRowDim = (int) rows;
            final int tmpColDim = (int) columns;

            final int tmpLength = tmpRowDim * tmpColDim;

            final ComplexNumber[] tmpData = new ComplexNumber[tmpLength];

            for (int i = 0; i < tmpLength; i++) {
                tmpData[i] = ComplexNumber.valueOf(supplier.get());
            }

            return new ComplexDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public Householder.Complex makeHouseholder(final int length) {
            return new Householder.Complex(length);
        }

        public Rotation.Complex makeRotation(final int low, final int high, final ComplexNumber cos, final ComplexNumber sin) {
            return new Rotation.Complex(low, high, cos, sin);
        }

        public Rotation.Complex makeRotation(final int low, final int high, final double cos, final double sin) {
            return this.makeRotation(low, high, ComplexNumber.valueOf(cos), ComplexNumber.valueOf(sin));
        }

        public ComplexDenseStore makeZero(final long rows, final long columns) {
            return new ComplexDenseStore((int) rows, (int) columns);
        }

        public ComplexDenseStore rows(final Access1D<?>... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = (int) source[0].count();

            final ComplexNumber[] tmpData = new ComplexNumber[tmpRowDim * tmpColDim];

            Access1D<?> tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = ComplexNumber.valueOf(tmpRow.get(j));
                }
            }

            return new ComplexDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public ComplexDenseStore rows(final double[]... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].length;

            final ComplexNumber[] tmpData = new ComplexNumber[tmpRowDim * tmpColDim];

            double[] tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = ComplexNumber.valueOf((Number) tmpRow[j]);
                }
            }

            return new ComplexDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public ComplexDenseStore rows(final List<? extends Number>... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].size();

            final ComplexNumber[] tmpData = new ComplexNumber[tmpRowDim * tmpColDim];

            List<? extends Number> tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = ComplexNumber.valueOf(tmpRow.get(j));
                }
            }

            return new ComplexDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public ComplexDenseStore rows(final Number[]... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].length;

            final ComplexNumber[] tmpData = new ComplexNumber[tmpRowDim * tmpColDim];

            Number[] tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = ComplexNumber.valueOf(tmpRow[j]);
                }
            }

            return new ComplexDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public Scalar.Factory<ComplexNumber> scalar() {
            return ComplexNumber.FACTORY;
        }

        public ComplexDenseStore transpose(final Access2D<?> source) {

            final ComplexDenseStore retVal = new ComplexDenseStore((int) source.countColumns(), (int) source.countRows());

            final int tmpRowDim = retVal.getRowDim();
            final int tmpColDim = retVal.getColDim();

            if (tmpColDim > FillTransposed.THRESHOLD) {

                final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                    @Override
                    public void conquer(final int aFirst, final int aLimit) {
                        FillTransposed.invoke(retVal.data, tmpRowDim, aFirst, aLimit, source);
                    }

                };

                tmpConquerer.invoke(0, tmpColDim, FillTransposed.THRESHOLD);

            } else {

                FillTransposed.invoke(retVal.data, tmpRowDim, 0, tmpColDim, source);
            }

            return retVal;
        }
    };

    static ComplexDenseStore cast(final Access1D<ComplexNumber> matrix) {
        if (matrix instanceof ComplexDenseStore) {
            return (ComplexDenseStore) matrix;
        } else if (matrix instanceof Access2D<?>) {
            return FACTORY.copy((Access2D<?>) matrix);
        } else {
            return FACTORY.columns(matrix);
        }
    }

    static Householder.Complex cast(final Householder<ComplexNumber> transformation) {
        if (transformation instanceof Householder.Complex) {
            return (Householder.Complex) transformation;
        } else if (transformation instanceof DecompositionStore.HouseholderReference<?>) {
            return ((DecompositionStore.HouseholderReference<ComplexNumber>) transformation).getComplexWorker().copy(transformation);
        } else {
            return new Householder.Complex(transformation);
        }
    }

    static Rotation.Complex cast(final Rotation<ComplexNumber> transformation) {
        if (transformation instanceof Rotation.Complex) {
            return (Rotation.Complex) transformation;
        } else {
            return new Rotation.Complex(transformation);
        }
    }

    private final ComplexMultiplyBoth multiplyBoth;
    private final ComplexMultiplyLeft multiplyLeft;
    private final ComplexMultiplyRight multiplyRight;
    private final int myColDim;
    private final int myRowDim;
    private final Array2D<ComplexNumber> myUtility;

    ComplexDenseStore(final ComplexNumber[] anArray) {

        super(anArray);

        myRowDim = anArray.length;
        myColDim = 1;

        myUtility = this.asArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.getComplex(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.getComplex(myRowDim, myColDim);
        multiplyRight = MultiplyRight.getComplex(myRowDim, myColDim);
    }

    ComplexDenseStore(final int aLength) {

        super(aLength);

        myRowDim = aLength;
        myColDim = 1;

        myUtility = this.asArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.getComplex(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.getComplex(myRowDim, myColDim);
        multiplyRight = MultiplyRight.getComplex(myRowDim, myColDim);
    }

    ComplexDenseStore(final int aRowDim, final int aColDim) {

        super(aRowDim * aColDim);

        myRowDim = aRowDim;
        myColDim = aColDim;

        myUtility = this.asArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.getComplex(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.getComplex(myRowDim, myColDim);
        multiplyRight = MultiplyRight.getComplex(myRowDim, myColDim);
    }

    ComplexDenseStore(final int aRowDim, final int aColDim, final ComplexNumber[] anArray) {

        super(anArray);

        myRowDim = aRowDim;
        myColDim = aColDim;

        myUtility = this.asArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.getComplex(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.getComplex(myRowDim, myColDim);
        multiplyRight = MultiplyRight.getComplex(myRowDim, myColDim);
    }

    public void accept(final Access2D<ComplexNumber> supplied) {
        for (long j = 0; j < supplied.countColumns(); j++) {
            for (long i = 0; i < supplied.countRows(); i++) {
                this.set(i, j, supplied.get(i, j));
            }
        }
    }

    public void add(final long row, final long column, final double addend) {
        myUtility.add(row, column, addend);
    }

    public void add(final long row, final long column, final Number addend) {
        myUtility.add(row, column, addend);
    }

    public ComplexNumber aggregateAll(final Aggregator aggregator) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        final AggregatorFunction<ComplexNumber> tmpMainAggr = aggregator.getComplexFunction();

        if (tmpColDim > AggregateAll.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {

                    final AggregatorFunction<ComplexNumber> tmpPartAggr = aggregator.getComplexFunction();

                    ComplexDenseStore.this.visit(tmpRowDim * aFirst, tmpRowDim * aLimit, 1, tmpPartAggr);

                    synchronized (tmpMainAggr) {
                        tmpMainAggr.merge(tmpPartAggr.getNumber());
                    }
                }
            };

            tmpConquerer.invoke(0, tmpColDim, AggregateAll.THRESHOLD);

        } else {

            ComplexDenseStore.this.visit(0, this.size(), 1, tmpMainAggr);
        }

        return tmpMainAggr.getNumber();
    }

    public void applyCholesky(final int iterationPoint, final BasicArray<ComplexNumber> multipliers) {

        final ComplexNumber[] tmpData = data;
        final ComplexNumber[] tmpColumn = ((ComplexArray) multipliers).data;

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

    public void applyLDL(final int iterationPoint, final BasicArray<ComplexNumber> multipliers) {

        final ComplexNumber[] tmpData = data;
        final ComplexNumber[] tmpColumn = ((ComplexArray) multipliers).data;

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

    public void applyLU(final int iterationPoint, final BasicArray<ComplexNumber> multipliers) {

        final ComplexNumber[] tmpData = data;
        final ComplexNumber[] tmpColumn = ((ComplexArray) multipliers).data;

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

    public Array2D<ComplexNumber> asArray2D() {
        return myUtility;
    }

    public Array1D<ComplexNumber> asList() {
        return myUtility.asArray1D();
    }

    public final MatrixStore.Builder<ComplexNumber> builder() {
        return new MatrixStore.Builder<ComplexNumber>(this);
    }

    public void caxpy(final ComplexNumber scalarA, final int columnX, final int columnY, final int firstRow) {
        AXPY.invoke(data, (columnY * myRowDim) + firstRow, 1, scalarA, data, (columnX * myRowDim) + firstRow, 1, myRowDim - firstRow);
    }

    public Array1D<ComplexNumber> computeInPlaceSchur(final PhysicalStore<ComplexNumber> transformationCollector, final boolean eigenvalue) {
        throw new UnsupportedOperationException();
    }

    public MatrixStore<ComplexNumber> conjugate() {
        return new ConjugatedStore<>(this);
    }

    public ComplexDenseStore copy() {
        return new ComplexDenseStore(myRowDim, myColDim, this.copyOfData());
    }

    public long countColumns() {
        return myColDim;
    }

    public long countRows() {
        return myRowDim;
    }

    public void divideAndCopyColumn(final int row, final int column, final BasicArray<ComplexNumber> destination) {

        final ComplexNumber[] tmpData = data;
        final int tmpRowDim = myRowDim;

        final ComplexNumber[] tmpDestination = ((ComplexArray) destination).data;

        int tmpIndex = row + (column * tmpRowDim);
        final ComplexNumber tmpDenominator = tmpData[tmpIndex];

        for (int i = row + 1; i < tmpRowDim; i++) {
            tmpIndex++;
            tmpDestination[i] = tmpData[tmpIndex] = tmpData[tmpIndex].divide(tmpDenominator);
        }
    }

    public double doubleValue(final long aRow, final long aCol) {
        return this.doubleValue(aRow + (aCol * myRowDim));
    }

    public boolean equals(final MatrixStore<ComplexNumber> other, final NumberContext context) {
        return AccessUtils.equals(this, other, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object anObj) {
        if (anObj instanceof MatrixStore) {
            return this.equals((MatrixStore<ComplexNumber>) anObj, NumberContext.getGeneral(6));
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

        ComplexNumber tmpVal;
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
            this.set(ij, tmpMin, this.get(tmpMax, ij).conjugate());
            this.set(tmpMax, ij, tmpVal.conjugate());
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

    public PhysicalStore.Factory<ComplexNumber, ComplexDenseStore> factory() {
        return FACTORY;
    }

    public void fillByMultiplying(final Access1D<ComplexNumber> left, final Access1D<ComplexNumber> right) {

        final int tmpComplexity = ((int) left.count()) / myRowDim;

        if (right instanceof ComplexDenseStore) {
            multiplyLeft.invoke(data, left, tmpComplexity, ComplexDenseStore.cast(right).data);
        } else if (left instanceof ComplexDenseStore) {
            multiplyRight.invoke(data, ComplexDenseStore.cast(left).data, tmpComplexity, right);
        } else {
            multiplyBoth.invoke(this, left, tmpComplexity, right);
        }
    }

    public void fillColumn(final long row, final long column, final ComplexNumber value) {
        myUtility.fillColumn(row, column, value);
    }

    public void fillColumn(final long row, final long column, final NullaryFunction<ComplexNumber> supplier) {
        myUtility.fillColumn(row, column, supplier);
    }

    public void fillDiagonal(final long row, final long column, final ComplexNumber value) {
        myUtility.fillDiagonal(row, column, value);
    }

    public void fillDiagonal(final long row, final long column, final NullaryFunction<ComplexNumber> supplier) {
        myUtility.fillDiagonal(row, column, supplier);
    }

    public void fillMatching(final Access1D<ComplexNumber> aLeftArg, final BinaryFunction<ComplexNumber> aFunc, final ComplexNumber aRightArg) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > FillMatchingLeft.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int aFirst, final int aLimit) {
                    ComplexDenseStore.this.fill(tmpRowDim * aFirst, tmpRowDim * aLimit, aLeftArg, aFunc, aRightArg);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, FillMatchingLeft.THRESHOLD);

        } else {

            this.fill(0, tmpRowDim * tmpColDim, aLeftArg, aFunc, aRightArg);
        }
    }

    public void fillMatching(final ComplexNumber aLeftArg, final BinaryFunction<ComplexNumber> aFunc, final Access1D<ComplexNumber> aRightArg) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > FillMatchingRight.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int aFirst, final int aLimit) {
                    ComplexDenseStore.this.fill(tmpRowDim * aFirst, tmpRowDim * aLimit, aLeftArg, aFunc, aRightArg);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, FillMatchingRight.THRESHOLD);

        } else {

            this.fill(0, tmpRowDim * tmpColDim, aLeftArg, aFunc, aRightArg);
        }
    }

    public void fillOne(final long row, final long column, final ComplexNumber value) {
        myUtility.fillOne(row, column, value);
    }

    public void fillOne(final long row, final long column, final NullaryFunction<ComplexNumber> supplier) {
        myUtility.fillOne(row, column, supplier);
    }

    public void fillOneMatching(final long row, final long column, final Access1D<?> values, final long valueIndex) {
        this.set(row, column, values.get(valueIndex));
    }

    public void fillRow(final long row, final long column, final ComplexNumber value) {
        myUtility.fillRow(row, column, value);
    }

    public void fillRow(final long row, final long column, final NullaryFunction<ComplexNumber> supplier) {
        myUtility.fillRow(row, column, supplier);
    }

    public boolean generateApplyAndCopyHouseholderColumn(final int row, final int column, final Householder<ComplexNumber> destination) {
        return GenerateApplyAndCopyHouseholderColumn.invoke(data, myRowDim, row, column, (Householder.Complex) destination);
    }

    public boolean generateApplyAndCopyHouseholderRow(final int row, final int column, final Householder<ComplexNumber> destination) {
        return GenerateApplyAndCopyHouseholderRow.invoke(data, myRowDim, row, column, (Householder.Complex) destination);
    }

    public final MatrixStore<ComplexNumber> get() {
        return this;
    }

    public ComplexNumber get(final long aRow, final long aCol) {
        return myUtility.get(aRow, aCol);
    }

    @Override
    public int hashCode() {
        return MatrixUtils.hashCode(this);
    }

    public int indexOfLargestInColumn(final int row, final int column) {
        return (int) myUtility.indexOfLargestInColumn(row, column);
    }

    public long indexOfLargestInColumn(final long row, final long column) {
        return myUtility.indexOfLargestInColumn(row, column);
    }

    public int indexOfLargestInDiagonal(final int row, final int column) {
        return (int) myUtility.indexOfLargestInDiagonal(row, column);
    }

    public long indexOfLargestInDiagonal(final long row, final long column) {
        return myUtility.indexOfLargestInDiagonal(row, column);
    }

    public long indexOfLargestInRow(final long row, final long column) {
        return myUtility.indexOfLargestInRow(row, column);
    }

    public boolean isAbsolute(final long row, final long column) {
        return myUtility.isAbsolute(row, column);
    }

    public boolean isSmall(final long row, final long column, final double comparedTo) {
        return myUtility.isSmall(row, column, comparedTo);
    }

    public void maxpy(final ComplexNumber aSclrA, final MatrixStore<ComplexNumber> aMtrxX) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > MAXPY.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    MAXPY.invoke(ComplexDenseStore.this.data, tmpRowDim, aFirst, aLimit, aSclrA, aMtrxX);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, MAXPY.THRESHOLD);

        } else {

            MAXPY.invoke(data, tmpRowDim, 0, tmpColDim, aSclrA, aMtrxX);
        }
    }

    @Override
    public void modifyAll(final UnaryFunction<ComplexNumber> aFunc) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > ModifyAll.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    ComplexDenseStore.this.modify(tmpRowDim * aFirst, tmpRowDim * aLimit, 1, aFunc);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, ModifyAll.THRESHOLD);

        } else {

            this.modify(tmpRowDim * 0, tmpRowDim * tmpColDim, 1, aFunc);
        }
    }

    public void modifyColumn(final long row, final long column, final UnaryFunction<ComplexNumber> function) {
        myUtility.modifyColumn(row, column, function);
    }

    public void modifyDiagonal(final long row, final long column, final UnaryFunction<ComplexNumber> function) {
        myUtility.modifyDiagonal(row, column, function);
    }

    public void modifyOne(final long row, final long column, final UnaryFunction<ComplexNumber> function) {

        ComplexNumber tmpValue = this.get(row, column);

        tmpValue = function.invoke(tmpValue);

        this.set(row, column, tmpValue);
    }

    public void modifyRow(final long row, final long column, final UnaryFunction<ComplexNumber> function) {
        myUtility.modifyRow(row, column, function);
    }

    public MatrixStore<ComplexNumber> multiply(final Access1D<ComplexNumber> right) {

        final ComplexDenseStore retVal = FACTORY.makeZero(myRowDim, right.count() / myColDim);

        if (right instanceof ComplexDenseStore) {
            retVal.multiplyLeft.invoke(retVal.data, this, myColDim, ((ComplexDenseStore) right).data);
        } else {
            retVal.multiplyRight.invoke(retVal.data, data, myColDim, right);
        }

        return retVal;
    }

    public ComplexNumber multiplyBoth(final Access1D<ComplexNumber> leftAndRight) {

        final PhysicalStore<ComplexNumber> tmpStep1 = FACTORY.makeZero(1L, leftAndRight.count());
        final PhysicalStore<ComplexNumber> tmpStep2 = FACTORY.makeZero(1L, 1L);

        final PhysicalStore<ComplexNumber> tmpLeft = FACTORY.rows(leftAndRight);
        tmpLeft.fillMatching(FACTORY.function().conjugate(), leftAndRight);
        tmpStep1.fillByMultiplying(tmpLeft, this);

        tmpStep2.fillByMultiplying(tmpStep1, leftAndRight);

        return tmpStep2.get(0L);
    }

    public void negateColumn(final int column) {
        myUtility.modifyColumn(0, column, ComplexFunction.NEGATE);
    }

    public void raxpy(final ComplexNumber scalarA, final int rowX, final int rowY, final int firstColumn) {
        AXPY.invoke(data, rowY + (firstColumn * (data.length / myColDim)), data.length / myColDim, scalarA, data,
                rowX + (firstColumn * (data.length / myColDim)), data.length / myColDim, myColDim - firstColumn);
    }

    public final ElementsConsumer<ComplexNumber> regionByColumns(final int... columns) {
        return new ColumnsRegion<ComplexNumber>(this, multiplyBoth, columns);
    }

    public final ElementsConsumer<ComplexNumber> regionByLimits(final int rowLimit, final int columnLimit) {
        return new LimitRegion<ComplexNumber>(this, multiplyBoth, rowLimit, columnLimit);
    }

    public final ElementsConsumer<ComplexNumber> regionByOffsets(final int rowOffset, final int columnOffset) {
        return new OffsetRegion<ComplexNumber>(this, multiplyBoth, rowOffset, columnOffset);
    }

    public final ElementsConsumer<ComplexNumber> regionByRows(final int... rows) {
        return new RowsRegion<ComplexNumber>(this, multiplyBoth, rows);
    }

    public final ElementsConsumer<ComplexNumber> regionByTransposing() {
        return new TransposedRegion<ComplexNumber>(this, multiplyBoth);
    }

    public void rotateRight(final int aLow, final int aHigh, final double aCos, final double aSin) {
        RotateRight.invoke(data, myRowDim, aLow, aHigh, FACTORY.scalar().cast(aCos), FACTORY.scalar().cast(aSin));
    }

    public void set(final long aRow, final long aCol, final double aNmbr) {
        myUtility.set(aRow, aCol, aNmbr);
    }

    public void set(final long aRow, final long aCol, final Number aNmbr) {
        myUtility.set(aRow, aCol, aNmbr);
    }

    public void setToIdentity(final int aCol) {
        myUtility.set(aCol, aCol, ComplexNumber.ONE);
        myUtility.fillColumn(aCol + 1, aCol, ComplexNumber.ZERO);
    }

    public Array1D<ComplexNumber> sliceColumn(final long row, final long column) {
        return myUtility.sliceColumn(row, column);
    }

    public Array1D<ComplexNumber> sliceDiagonal(final long row, final long column) {
        return myUtility.sliceDiagonal(row, column);
    }

    public Array1D<ComplexNumber> sliceRange(final long first, final long limit) {
        return myUtility.sliceRange(first, limit);
    }

    public Array1D<ComplexNumber> sliceRow(final long row, final long column) {
        return myUtility.sliceRow(row, column);
    }

    public void substituteBackwards(final Access2D<ComplexNumber> body, final boolean unitDiagonal, final boolean conjugated, final boolean hermitian) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > SubstituteBackwards.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    SubstituteBackwards.invoke(ComplexDenseStore.this.data, tmpRowDim, aFirst, aLimit, body, unitDiagonal, conjugated, hermitian);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, SubstituteBackwards.THRESHOLD);

        } else {

            SubstituteBackwards.invoke(data, tmpRowDim, 0, tmpColDim, body, unitDiagonal, conjugated, hermitian);
        }
    }

    public void substituteForwards(final Access2D<ComplexNumber> body, final boolean unitDiagonal, final boolean conjugated, final boolean identity) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > SubstituteForwards.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    SubstituteForwards.invoke(ComplexDenseStore.this.data, tmpRowDim, aFirst, aLimit, body, unitDiagonal, conjugated, identity);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, SubstituteForwards.THRESHOLD);

        } else {

            SubstituteForwards.invoke(data, tmpRowDim, 0, tmpColDim, body, unitDiagonal, conjugated, identity);
        }
    }

    public void supplyTo(final ElementsConsumer<ComplexNumber> consumer) {
        consumer.fillMatching(this);
    }

    public Scalar<ComplexNumber> toScalar(final long row, final long column) {
        return myUtility.get(row, column);
    }

    @Override
    public final String toString() {
        return MatrixUtils.toString(this);
    }

    public void transformLeft(final Householder<ComplexNumber> transformation, final int firstColumn) {

        final Householder.Complex tmpTransf = ComplexDenseStore.cast(transformation);

        final ComplexNumber[] tmpData = data;

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

    public void transformLeft(final Rotation<ComplexNumber> transformation) {

        final Rotation.Complex tmpTransf = ComplexDenseStore.cast(transformation);

        final int tmpLow = tmpTransf.low;
        final int tmpHigh = tmpTransf.high;

        if (tmpLow != tmpHigh) {
            if ((tmpTransf.cos != null) && (tmpTransf.sin != null)) {
                RotateLeft.invoke(data, myColDim, tmpLow, tmpHigh, tmpTransf.cos, tmpTransf.sin);
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

    public void transformRight(final Householder<ComplexNumber> transformation, final int firstRow) {

        final Householder.Complex tmpTransf = ComplexDenseStore.cast(transformation);

        final ComplexNumber[] tmpData = data;

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

    public void transformRight(final Rotation<ComplexNumber> transformation) {

        final Rotation.Complex tmpTransf = ComplexDenseStore.cast(transformation);

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

    public void transformSymmetric(final Householder<ComplexNumber> transformation) {
        HouseholderHermitian.invoke(data, ComplexDenseStore.cast(transformation), new ComplexNumber[(int) transformation.count()]);
    }

    public MatrixStore<ComplexNumber> transpose() {
        return new TransposedStore<>(this);
    }

    public void tred2(final BasicArray<ComplexNumber> mainDiagonal, final BasicArray<ComplexNumber> offDiagonal, final boolean yesvecs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitAll(final VoidFunction<ComplexNumber> visitor) {
        myUtility.visitAll(visitor);
    }

    public void visitColumn(final long row, final long column, final VoidFunction<ComplexNumber> visitor) {
        myUtility.visitColumn(row, column, visitor);
    }

    public void visitDiagonal(final long row, final long column, final VoidFunction<ComplexNumber> visitor) {
        myUtility.visitDiagonal(row, column, visitor);
    }

    public void visitRow(final long row, final long column, final VoidFunction<ComplexNumber> visitor) {
        myUtility.visitRow(row, column, visitor);
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
