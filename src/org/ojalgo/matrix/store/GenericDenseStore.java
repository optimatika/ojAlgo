/*
 * Copyright 1997-2017 Optimatika
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

import java.util.Arrays;
import java.util.List;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.Array2D;
import org.ojalgo.array.BasicArray;
import org.ojalgo.array.ComplexArray;
import org.ojalgo.array.DenseArray;
import org.ojalgo.array.ScalarArray;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.ComplexFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.FunctionUtils;
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
import org.ojalgo.matrix.transformation.HouseholderReference;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * A {@linkplain N} implementation of {@linkplain PhysicalStore}.
 *
 * @author apete
 */
public final class GenericDenseStore<N extends Number & Scalar<N>> extends ScalarArray<N> implements PhysicalStore<N>, DecompositionStore<N> {

    public static interface GenericMultiplyBoth<N extends Number & Scalar<N>> extends FillByMultiplying<N> {

    }

    public static interface GenericMultiplyLeft<N extends Number & Scalar<N>> {

        void invoke(N[] product, Access1D<N> left, int complexity, N[] right, Scalar.Factory<N> scalar);

    }

    public static interface GenericMultiplyNeither<N extends Number & Scalar<N>> {

        void invoke(N[] product, N[] left, int complexity, N[] right, Scalar.Factory<N> scalar);

    }

    public static interface GenericMultiplyRight<N extends Number & Scalar<N>> {

        void invoke(N[] product, N[] left, int complexity, Access1D<N> right, Scalar.Factory<N> scalar);

    }

    public static final class MyFactory<N extends Number & Scalar<N>> implements PhysicalStore.Factory<N, GenericDenseStore<N>> {

        private final DenseArray.Factory<N> myDenseArrayFactory = null;

        public AggregatorSet<N> aggregator() {
            return ComplexAggregator.getSet();
        }

        public DenseArray.Factory<N> array() {
            return ComplexArray.FACTORY;
        }

        public MatrixStore.Factory<N> builder() {
            return MatrixStore.COMPLEX;
        }

        public GenericDenseStore columns(final Access1D<?>... source) {

            final int tmpRowDim = (int) source[0].count();
            final int tmpColDim = source.length;

            final N[] tmpData = new N[tmpRowDim * tmpColDim];

            Access1D<?> tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = N.valueOf(tmpColumn.get(i));
                }
            }

            return new GenericDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public GenericDenseStore columns(final double[]... source) {

            final int tmpRowDim = source[0].length;
            final int tmpColDim = source.length;

            final N[] tmpData = new N[tmpRowDim * tmpColDim];

            double[] tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = N.valueOf((Number) tmpColumn[i]);
                }
            }

            return new GenericDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public GenericDenseStore columns(final List<? extends Number>... source) {

            final int tmpRowDim = source[0].size();
            final int tmpColDim = source.length;

            final N[] tmpData = new N[tmpRowDim * tmpColDim];

            List<? extends Number> tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = N.valueOf(tmpColumn.get(i));
                }
            }

            return new GenericDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public GenericDenseStore columns(final Number[]... source) {

            final int tmpRowDim = source[0].length;
            final int tmpColDim = source.length;

            final N[] tmpData = new N[tmpRowDim * tmpColDim];

            Number[] tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = N.valueOf(tmpColumn[i]);
                }
            }

            return new GenericDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public GenericDenseStore conjugate(final Access2D<?> source) {

            final GenericDenseStore retVal = new GenericDenseStore((int) source.countColumns(), (int) source.countRows());

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

        public GenericDenseStore copy(final Access2D<?> source) {

            final int tmpRowDim = (int) source.countRows();
            final int tmpColDim = (int) source.countColumns();

            final GenericDenseStore retVal = new GenericDenseStore(tmpRowDim, tmpColDim);

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

        public FunctionSet<N> function() {
            return ComplexFunction.getSet();
        }

        public GenericDenseStore makeEye(final long rows, final long columns) {

            final GenericDenseStore retVal = this.makeZero(rows, columns);

            retVal.myUtility.fillDiagonal(0, 0, N.ONE);

            return retVal;
        }

        public GenericDenseStore makeFilled(final long rows, final long columns, final NullaryFunction<?> supplier) {

            final int tmpRowDim = (int) rows;
            final int tmpColDim = (int) columns;

            final int tmpLength = tmpRowDim * tmpColDim;

            final N[] tmpData = new N[tmpLength];

            for (int i = 0; i < tmpLength; i++) {
                tmpData[i] = N.valueOf(supplier.get());
            }

            return new GenericDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public Householder.Complex makeHouseholder(final int length) {
            return new Householder.Complex(length);
        }

        public Rotation.Complex makeRotation(final int low, final int high, final double cos, final double sin) {
            return this.makeRotation(low, high, N.valueOf(cos), N.valueOf(sin));
        }

        public Rotation.Complex makeRotation(final int low, final int high, final N cos, final N sin) {
            return new Rotation.Complex(low, high, cos, sin);
        }

        public GenericDenseStore makeZero(final long rows, final long columns) {
            return new GenericDenseStore((int) rows, (int) columns);
        }

        public GenericDenseStore rows(final Access1D<?>... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = (int) source[0].count();

            final N[] tmpData = new N[tmpRowDim * tmpColDim];

            Access1D<?> tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = N.valueOf(tmpRow.get(j));
                }
            }

            return new GenericDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public GenericDenseStore rows(final double[]... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].length;

            final N[] tmpData = new N[tmpRowDim * tmpColDim];

            double[] tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = N.valueOf((Number) tmpRow[j]);
                }
            }

            return new GenericDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public GenericDenseStore rows(final List<? extends Number>... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].size();

            final N[] tmpData = new N[tmpRowDim * tmpColDim];

            List<? extends Number> tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = N.valueOf(tmpRow.get(j));
                }
            }

            return new GenericDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public GenericDenseStore rows(final Number[]... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].length;

            final N[] tmpData = new N[tmpRowDim * tmpColDim];

            Number[] tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = N.valueOf(tmpRow[j]);
                }
            }

            return new GenericDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public Scalar.Factory<N> scalar() {
            return N.FACTORY;
        }

        public GenericDenseStore transpose(final Access2D<?> source) {

            final GenericDenseStore retVal = new GenericDenseStore((int) source.countColumns(), (int) source.countRows());

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

    }

    private final GenericMultiplyBoth<N> multiplyBoth;;

    private final GenericMultiplyLeft<N> multiplyLeft;

    private final GenericMultiplyNeither<N> multiplyNeither;

    private final GenericMultiplyRight<N> multiplyRight;

    private final int myColDim;
    private final MyFactory<N> myFactory = null;
    private final int myRowDim;
    private final Array2D<N> myUtility;
    private transient N[] myWorkerColumn;

    GenericDenseStore(final Class<N> componentType, final int length) {

        super(componentType, length);

        myRowDim = length;
        myColDim = 1;

        myUtility = this.asArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.getGeneric(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.getGeneric(myRowDim, myColDim);
        multiplyRight = MultiplyRight.getGeneric(myRowDim, myColDim);
        multiplyNeither = MultiplyNeither.getGeneric(myRowDim, myColDim);
    }

    GenericDenseStore(final Class<N> componentType, final int aRowDim, final int aColDim) {

        super(componentType, aRowDim * aColDim);

        myRowDim = aRowDim;
        myColDim = aColDim;

        myUtility = this.asArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.getGeneric(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.getGeneric(myRowDim, myColDim);
        multiplyRight = MultiplyRight.getGeneric(myRowDim, myColDim);
        multiplyNeither = MultiplyNeither.getGeneric(myRowDim, myColDim);
    }

    GenericDenseStore(final int aRowDim, final int aColDim, final N[] anArray) {

        super(anArray);

        myRowDim = aRowDim;
        myColDim = aColDim;

        myUtility = this.asArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.getGeneric(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.getGeneric(myRowDim, myColDim);
        multiplyRight = MultiplyRight.getGeneric(myRowDim, myColDim);
        multiplyNeither = MultiplyNeither.getGeneric(myRowDim, myColDim);
    }

    GenericDenseStore(final N[] anArray) {

        super(anArray);

        myRowDim = anArray.length;
        myColDim = 1;

        myUtility = this.asArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.getGeneric(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.getGeneric(myRowDim, myColDim);
        multiplyRight = MultiplyRight.getGeneric(myRowDim, myColDim);
        multiplyNeither = MultiplyNeither.getGeneric(myRowDim, myColDim);
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

    public N aggregateAll(final Aggregator aggregator) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        final AggregatorFunction<N> tmpMainAggr = aggregator.getFunction(ComplexAggregator.getSet());

        if (tmpColDim > AggregateAll.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {

                    final AggregatorFunction<N> tmpPartAggr = aggregator.getFunction(ComplexAggregator.getSet());

                    GenericDenseStore.this.visit(tmpRowDim * aFirst, tmpRowDim * aLimit, 1, tmpPartAggr);

                    synchronized (tmpMainAggr) {
                        tmpMainAggr.merge(tmpPartAggr.getNumber());
                    }
                }
            };

            tmpConquerer.invoke(0, tmpColDim, AggregateAll.THRESHOLD);

        } else {

            GenericDenseStore.this.visit(0, this.size(), 1, tmpMainAggr);
        }

        return tmpMainAggr.getNumber();
    }

    public void applyCholesky(final int iterationPoint, final BasicArray<N> multipliers) {

        final N[] tmpData = data;
        final N[] tmpColumn = ((ComplexArray) multipliers).data;

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

    public void applyLDL(final int iterationPoint, final BasicArray<N> multipliers) {

        final N[] tmpData = data;
        final N[] tmpColumn = ((ComplexArray) multipliers).data;

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

    public void applyLU(final int iterationPoint, final BasicArray<N> multipliers) {

        final N[] tmpData = data;
        final N[] tmpColumn = ((ComplexArray) multipliers).data;

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

    public Array1D<N> asList() {
        return myUtility.asArray1D();
    }

    public Array1D<ComplexNumber> computeInPlaceSchur(final PhysicalStore<N> transformationCollector, final boolean eigenvalue) {
        ProgrammingError.throwForUnsupportedOptionalOperation();
        return null;
    }

    public MatrixStore<N> conjugate() {
        return new ConjugatedStore<>(this);
    }

    public GenericDenseStore copy() {
        return new GenericDenseStore(myRowDim, myColDim, this.copyOfData());
    }

    public long countColumns() {
        return myColDim;
    }

    public long countRows() {
        return myRowDim;
    }

    public void divideAndCopyColumn(final int row, final int column, final BasicArray<N> destination) {

        final N[] tmpData = data;
        final int tmpRowDim = myRowDim;

        final N[] tmpDestination = ((ScalarArray<N>) destination).data;

        int tmpIndex = row + (column * tmpRowDim);
        final N tmpDenominator = tmpData[tmpIndex];

        for (int i = row + 1; i < tmpRowDim; i++) {
            tmpIndex++;
            tmpDestination[i] = tmpData[tmpIndex] = tmpData[tmpIndex].divide(tmpDenominator).getNumber();
        }
    }

    public double doubleValue(final long aRow, final long aCol) {
        return this.doubleValue(aRow + (aCol * myRowDim));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object anObj) {
        if (anObj instanceof MatrixStore) {
            return this.equals((MatrixStore<N>) anObj, NumberContext.getGeneral(6));
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

        N tmpVal;
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

    public void fillByMultiplying(final Access1D<N> left, final Access1D<N> right) {

        final int complexity = ((int) left.count()) / myRowDim;

        if (left instanceof GenericDenseStore) {
            if (right instanceof GenericDenseStore) {
                multiplyNeither.invoke(data, this.cast(left).data, complexity, this.cast(right).data, myFactory.scalar());
            } else {
                multiplyRight.invoke(data, this.cast(left).data, complexity, right, myFactory.scalar());
            }
        } else {
            if (right instanceof GenericDenseStore) {
                multiplyLeft.invoke(data, left, complexity, this.cast(right).data, myFactory.scalar());
            } else {
                multiplyBoth.invoke(this, left, complexity, right);
            }
        }
    }

    public void fillColumn(final long row, final long col, final Access1D<N> values) {
        myUtility.fillColumn(row, col, values);
    }

    public void fillColumn(final long row, final long col, final N value) {
        myUtility.fillColumn(row, col, value);
    }

    public void fillColumn(final long row, final long col, final NullaryFunction<N> supplier) {
        myUtility.fillColumn(row, col, supplier);
    }

    public void fillDiagonal(final long row, final long col, final N value) {
        myUtility.fillDiagonal(row, col, value);
    }

    public void fillDiagonal(final long row, final long col, final NullaryFunction<N> supplier) {
        myUtility.fillDiagonal(row, col, supplier);
    }

    public void fillMatching(final Access1D<N> aLeftArg, final BinaryFunction<N> aFunc, final N aRightArg) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > FillMatchingLeft.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int aFirst, final int aLimit) {
                    GenericDenseStore.this.fill(tmpRowDim * aFirst, tmpRowDim * aLimit, aLeftArg, aFunc, aRightArg);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, FillMatchingLeft.THRESHOLD);

        } else {

            this.fill(0, tmpRowDim * tmpColDim, aLeftArg, aFunc, aRightArg);
        }
    }

    public void fillMatching(final N aLeftArg, final BinaryFunction<N> aFunc, final Access1D<N> aRightArg) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > FillMatchingRight.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int aFirst, final int aLimit) {
                    GenericDenseStore.this.fill(tmpRowDim * aFirst, tmpRowDim * aLimit, aLeftArg, aFunc, aRightArg);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, FillMatchingRight.THRESHOLD);

        } else {

            this.fill(0, tmpRowDim * tmpColDim, aLeftArg, aFunc, aRightArg);
        }
    }

    public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
        this.set(row, col, values.get(valueIndex));
    }

    public void fillOne(final long row, final long col, final N value) {
        myUtility.fillOne(row, col, value);
    }

    public void fillOne(final long row, final long col, final NullaryFunction<N> supplier) {
        myUtility.fillOne(row, col, supplier);
    }

    public void fillRow(final long row, final long col, final Access1D<N> values) {
        myUtility.fillRow(row, col, values);
    }

    public void fillRow(final long row, final long col, final N value) {
        myUtility.fillRow(row, col, value);
    }

    public void fillRow(final long row, final long col, final NullaryFunction<N> supplier) {
        myUtility.fillRow(row, col, supplier);
    }

    public boolean generateApplyAndCopyHouseholderColumn(final int row, final int column, final Householder<N> destination) {
        return GenerateApplyAndCopyHouseholderColumn.invoke(data, myRowDim, row, column, (Householder.Generic<N>) destination, myFactory.scalar());
    }

    public boolean generateApplyAndCopyHouseholderRow(final int row, final int column, final Householder<N> destination) {
        return GenerateApplyAndCopyHouseholderRow.invoke(data, myRowDim, row, column, (Householder.Generic<N>) destination, myFactory.scalar());
    }

    public final MatrixStore<N> get() {
        return this;
    }

    public N get(final long aRow, final long aCol) {
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
    public void modifyAll(final UnaryFunction<N> aFunc) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > ModifyAll.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    GenericDenseStore.this.modify(tmpRowDim * aFirst, tmpRowDim * aLimit, 1, aFunc);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, ModifyAll.THRESHOLD);

        } else {

            this.modify(tmpRowDim * 0, tmpRowDim * tmpColDim, 1, aFunc);
        }
    }

    public void modifyColumn(final long row, final long col, final UnaryFunction<N> modifier) {
        myUtility.modifyColumn(row, col, modifier);
    }

    public void modifyDiagonal(final long row, final long col, final UnaryFunction<N> modifier) {
        myUtility.modifyDiagonal(row, col, modifier);
    }

    public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
        final long tmpLimit = FunctionUtils.min(left.count(), this.count(), this.count());
        for (long i = 0L; i < tmpLimit; i++) {
            this.fillOne(i, function.invoke(left.get(i), this.get(i)));
        }
    }

    public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
        final long tmpLimit = FunctionUtils.min(this.count(), right.count(), this.count());
        for (long i = 0L; i < tmpLimit; i++) {
            this.fillOne(i, function.invoke(this.get(i), right.get(i)));
        }
    }

    public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {

        N tmpValue = this.get(row, col);

        tmpValue = modifier.invoke(tmpValue);

        this.set(row, col, tmpValue);
    }

    public void modifyRow(final long row, final long col, final UnaryFunction<N> modifier) {
        myUtility.modifyRow(row, col, modifier);
    }

    public MatrixStore<N> multiply(final MatrixStore<N> right) {

        final GenericDenseStore retVal = this.physical().makeZero(myRowDim, right.count() / myColDim);

        if (right instanceof GenericDenseStore) {
            retVal.multiplyNeither.invoke(retVal.data, data, myColDim, this.cast(right).data, myFactory.scalar());
        } else {
            retVal.multiplyRight.invoke(retVal.data, data, myColDim, right, myFactory.scalar());
        }

        return retVal;
    }

    public N multiplyBoth(final Access1D<N> leftAndRight) {

        final PhysicalStore<N> tmpStep1 = myFactory.makeZero(1L, leftAndRight.count());
        final PhysicalStore<N> tmpStep2 = myFactory.makeZero(1L, 1L);

        final PhysicalStore<N> tmpLeft = myFactory.rows(leftAndRight);
        tmpLeft.modifyAll(myFactory.function().conjugate());
        tmpStep1.fillByMultiplying(tmpLeft, this);

        tmpStep2.fillByMultiplying(tmpStep1, leftAndRight);

        return tmpStep2.get(0L);
    }

    public void negateColumn(final int column) {
        myUtility.modifyColumn(0, column, myFactory.function().negate());
    }

    public PhysicalStore.Factory<N, GenericDenseStore<N>> physical() {
        return myFactory;
    }

    public final ElementsConsumer<N> regionByColumns(final int... columns) {
        return new ColumnsRegion<>(this, multiplyBoth, columns);
    }

    public final ElementsConsumer<N> regionByLimits(final int rowLimit, final int columnLimit) {
        return new LimitRegion<>(this, multiplyBoth, rowLimit, columnLimit);
    }

    public final ElementsConsumer<N> regionByOffsets(final int rowOffset, final int columnOffset) {
        return new OffsetRegion<>(this, multiplyBoth, rowOffset, columnOffset);
    }

    public final ElementsConsumer<N> regionByRows(final int... rows) {
        return new RowsRegion<>(this, multiplyBoth, rows);
    }

    public final ElementsConsumer<N> regionByTransposing() {
        return new TransposedRegion<>(this, multiplyBoth);
    }

    public void rotateRight(final int aLow, final int aHigh, final double aCos, final double aSin) {
        RotateRight.invoke(data, myRowDim, aLow, aHigh, myFactory.scalar().cast(aCos), myFactory.scalar().cast(aSin));
    }

    public void set(final long aRow, final long aCol, final double aNmbr) {
        myUtility.set(aRow, aCol, aNmbr);
    }

    public void set(final long aRow, final long aCol, final Number aNmbr) {
        myUtility.set(aRow, aCol, aNmbr);
    }

    public void setToIdentity(final int aCol) {
        myUtility.set(aCol, aCol, N.ONE);
        myUtility.fillColumn(aCol + 1, aCol, N.ZERO);
    }

    public Array1D<N> sliceColumn(final long row, final long col) {
        return myUtility.sliceColumn(row, col);
    }

    public Array1D<N> sliceDiagonal(final long row, final long col) {
        return myUtility.sliceDiagonal(row, col);
    }

    public Array1D<N> sliceRange(final long first, final long limit) {
        return myUtility.sliceRange(first, limit);
    }

    public Array1D<N> sliceRow(final long row, final long col) {
        return myUtility.sliceRow(row, col);
    }

    public void substituteBackwards(final Access2D<N> body, final boolean unitDiagonal, final boolean conjugated, final boolean hermitian) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > SubstituteBackwards.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    SubstituteBackwards.invoke(GenericDenseStore.this.data, tmpRowDim, aFirst, aLimit, body, unitDiagonal, conjugated, hermitian);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, SubstituteBackwards.THRESHOLD);

        } else {

            SubstituteBackwards.invoke(data, tmpRowDim, 0, tmpColDim, body, unitDiagonal, conjugated, hermitian);
        }
    }

    public void substituteForwards(final Access2D<N> body, final boolean unitDiagonal, final boolean conjugated, final boolean identity) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > SubstituteForwards.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    SubstituteForwards.invoke(GenericDenseStore.this.data, tmpRowDim, aFirst, aLimit, body, unitDiagonal, conjugated, identity);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, SubstituteForwards.THRESHOLD);

        } else {

            SubstituteForwards.invoke(data, tmpRowDim, 0, tmpColDim, body, unitDiagonal, conjugated, identity);
        }
    }

    public void supplyTo(final ElementsConsumer<N> receiver) {
        receiver.fillMatching(this);
    }

    public Scalar<N> toScalar(final long row, final long column) {
        return myUtility.get(row, column);
    }

    @Override
    public final String toString() {
        return MatrixUtils.toString(this);
    }

    public void transformLeft(final Householder<N> transformation, final int firstColumn) {

        final Householder.Complex tmpTransf = GenericDenseStore.cast(transformation);

        final N[] tmpData = data;

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

    public void transformLeft(final Rotation<N> transformation) {

        final Rotation.Complex tmpTransf = GenericDenseStore.cast(transformation);

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

    public void transformRight(final Householder<N> transformation, final int firstRow) {

        final Householder.Complex tmpTransf = GenericDenseStore.cast(transformation);

        final N[] tmpData = data;

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

    public void transformRight(final Rotation<N> transformation) {

        final Rotation.Complex tmpTransf = GenericDenseStore.cast(transformation);

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

    public void transformSymmetric(final Householder<N> transformation) {
        HouseholderHermitian.invoke(data, this.cast(transformation), this.getWorkerColumn());
    }

    public MatrixStore<N> transpose() {
        return new TransposedStore<>(this);
    }

    public void tred2(final BasicArray<N> mainDiagonal, final BasicArray<N> offDiagonal, final boolean yesvecs) {
        ProgrammingError.throwForUnsupportedOptionalOperation();
    }

    public void visitColumn(final long row, final long col, final VoidFunction<N> visitor) {
        myUtility.visitColumn(row, col, visitor);
    }

    public void visitDiagonal(final long row, final long col, final VoidFunction<N> visitor) {
        myUtility.visitDiagonal(row, col, visitor);
    }

    public void visitRow(final long row, final long col, final VoidFunction<N> visitor) {
        myUtility.visitRow(row, col, visitor);
    }

    private GenericDenseStore<N> cast(final Access1D<N> matrix) {
        if (matrix instanceof GenericDenseStore) {
            return (GenericDenseStore<N>) matrix;
        } else if (matrix instanceof Access2D<?>) {
            return myFactory.copy((Access2D<?>) matrix);
        } else {
            return myFactory.columns(matrix);
        }
    }

    private Householder.Generic<N> cast(final Householder<N> transformation) {
        if (transformation instanceof Householder.Generic) {
            return (Householder.Generic<N>) transformation;
        } else if (transformation instanceof HouseholderReference<?>) {
            return ((Householder.Generic<N>) ((HouseholderReference<N>) transformation).getWorker(myFactory)).copy(transformation);
        } else {
            return new Householder.Generic<N>(myFactory.scalar(), transformation);
        }
    }

    private Rotation.Generic<N> cast(final Rotation<N> transformation) {
        if (transformation instanceof Rotation.Generic) {
            return (Rotation.Generic<N>) transformation;
        } else {
            return new Rotation.Generic<N>(transformation);
        }
    }

    private N[] getWorkerColumn() {

        if (myWorkerColumn == null) {
            myWorkerColumn = myFactory.scalar().newArrayInstance(myRowDim);
        }

        Arrays.fill(myWorkerColumn, myFactory.scalar().zero().getNumber());

        return myWorkerColumn;
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
