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

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import java.util.Arrays;
import java.util.List;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.Array2D;
import org.ojalgo.array.BasicArray;
import org.ojalgo.array.ComplexArray;
import org.ojalgo.array.DenseArray;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.array.blas.AXPY;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.FunctionUtils;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.machine.JavaType;
import org.ojalgo.machine.MemoryEstimator;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.decomposition.EvD1D;
import org.ojalgo.matrix.store.operation.*;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.HouseholderReference;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * A {@linkplain Double} (actually double) implementation of {@linkplain PhysicalStore}.
 *
 * @author apete
 */
public final class PrimitiveDenseStore extends Primitive64Array implements PhysicalStore<Double>, DecompositionStore<Double> {

    public static interface PrimitiveMultiplyBoth extends ElementsConsumer.FillByMultiplying<Double> {

    }

    public static interface PrimitiveMultiplyLeft {

        void invoke(double[] product, Access1D<?> left, int complexity, double[] right);

    }

    public static interface PrimitiveMultiplyNeither {

        void invoke(double[] product, double[] left, int complexity, double[] right);

    }

    public static interface PrimitiveMultiplyRight {

        void invoke(double[] product, double[] left, int complexity, Access1D<?> right);

    }

    public static final PhysicalStore.Factory<Double, PrimitiveDenseStore> FACTORY = new PhysicalStore.Factory<Double, PrimitiveDenseStore>() {

        public AggregatorSet<Double> aggregator() {
            return PrimitiveAggregator.getSet();
        }

        public DenseArray.Factory<Double> array() {
            return Primitive64Array.FACTORY;
        }

        public MatrixStore.Factory<Double> builder() {
            return MatrixStore.PRIMITIVE;
        }

        public PrimitiveDenseStore columns(final Access1D<?>... source) {

            final int tmpRowDim = (int) source[0].count();
            final int tmpColDim = source.length;

            final double[] tmpData = new double[tmpRowDim * tmpColDim];

            Access1D<?> tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = tmpColumn.doubleValue(i);
                }
            }

            return new PrimitiveDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public PrimitiveDenseStore columns(final double[]... source) {

            final int tmpRowDim = source[0].length;
            final int tmpColDim = source.length;

            final double[] tmpData = new double[tmpRowDim * tmpColDim];

            double[] tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = tmpColumn[i];
                }
            }

            return new PrimitiveDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public PrimitiveDenseStore columns(final List<? extends Number>... source) {

            final int tmpRowDim = source[0].size();
            final int tmpColDim = source.length;

            final double[] tmpData = new double[tmpRowDim * tmpColDim];

            List<? extends Number> tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = tmpColumn.get(i).doubleValue();
                }
            }

            return new PrimitiveDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public PrimitiveDenseStore columns(final Number[]... source) {

            final int tmpRowDim = source[0].length;
            final int tmpColDim = source.length;

            final double[] tmpData = new double[tmpRowDim * tmpColDim];

            Number[] tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = tmpColumn[i].doubleValue();
                }
            }

            return new PrimitiveDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public PrimitiveDenseStore conjugate(final Access2D<?> source) {
            return this.transpose(source);
        }

        public PrimitiveDenseStore copy(final Access2D<?> source) {

            final int tmpRowDim = (int) source.countRows();
            final int tmpColDim = (int) source.countColumns();

            final PrimitiveDenseStore retVal = new PrimitiveDenseStore(tmpRowDim, tmpColDim);

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

        public FunctionSet<Double> function() {
            return PrimitiveFunction.getSet();
        }

        public PrimitiveDenseStore makeEye(final long rows, final long columns) {

            final PrimitiveDenseStore retVal = this.makeZero(rows, columns);

            retVal.myUtility.fillDiagonal(0, 0, ONE);

            return retVal;
        }

        public PrimitiveDenseStore makeFilled(final long rows, final long columns, final NullaryFunction<?> supplier) {

            final int tmpRowDim = (int) rows;
            final int tmpColDim = (int) columns;

            final int tmpLength = tmpRowDim * tmpColDim;

            final double[] tmpData = new double[tmpLength];

            for (int i = 0; i < tmpLength; i++) {
                tmpData[i] = supplier.doubleValue();
            }

            return new PrimitiveDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public Householder.Primitive makeHouseholder(final int length) {
            return new Householder.Primitive(length);
        }

        public Rotation.Primitive makeRotation(final int low, final int high, final double cos, final double sin) {
            return new Rotation.Primitive(low, high, cos, sin);
        }

        public Rotation.Primitive makeRotation(final int low, final int high, final Double cos, final Double sin) {
            return this.makeRotation(low, high, cos != null ? cos.doubleValue() : Double.NaN, sin != null ? sin.doubleValue() : Double.NaN);
        }

        public PrimitiveDenseStore makeZero(final long rows, final long columns) {
            return new PrimitiveDenseStore((int) rows, (int) columns);
        }

        public PrimitiveDenseStore rows(final Access1D<?>... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = (int) source[0].count();

            final double[] tmpData = new double[tmpRowDim * tmpColDim];

            Access1D<?> tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = tmpRow.doubleValue(j);
                }
            }

            return new PrimitiveDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public PrimitiveDenseStore rows(final double[]... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].length;

            final double[] tmpData = new double[tmpRowDim * tmpColDim];

            double[] tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = tmpRow[j];
                }
            }

            return new PrimitiveDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public PrimitiveDenseStore rows(final List<? extends Number>... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].size();

            final double[] tmpData = new double[tmpRowDim * tmpColDim];

            List<? extends Number> tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = tmpRow.get(j).doubleValue();
                }
            }

            return new PrimitiveDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public PrimitiveDenseStore rows(final Number[]... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].length;

            final double[] tmpData = new double[tmpRowDim * tmpColDim];

            Number[] tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = tmpRow[j].doubleValue();
                }
            }

            return new PrimitiveDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public Scalar.Factory<Double> scalar() {
            return PrimitiveScalar.FACTORY;
        }

        public PrimitiveDenseStore transpose(final Access2D<?> source) {

            final PrimitiveDenseStore retVal = new PrimitiveDenseStore((int) source.countColumns(), (int) source.countRows());

            final int tmpRowDim = retVal.getRowDim();
            final int tmpColDim = retVal.getColDim();

            if (tmpColDim > FillMatchingSingle.THRESHOLD) {

                final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                    @Override
                    public void conquer(final int first, final int limit) {
                        FillMatchingSingle.transpose(retVal.data, tmpRowDim, first, limit, source);
                    }

                };

                tmpConquerer.invoke(0, tmpColDim, FillMatchingSingle.THRESHOLD);

            } else {

                FillMatchingSingle.transpose(retVal.data, tmpRowDim, 0, tmpColDim, source);
            }

            return retVal;
        }

    };

    static final long ELEMENT_SIZE = JavaType.DOUBLE.memory();

    static final long SHALLOW_SIZE = MemoryEstimator.estimateObject(PrimitiveDenseStore.class);

    static PrimitiveDenseStore cast(final Access1D<Double> matrix) {
        if (matrix instanceof PrimitiveDenseStore) {
            return (PrimitiveDenseStore) matrix;
        } else if (matrix instanceof Access2D<?>) {
            return FACTORY.copy((Access2D<?>) matrix);
        } else {
            return FACTORY.columns(matrix);
        }
    }

    static Householder.Primitive cast(final Householder<Double> transformation) {
        if (transformation instanceof Householder.Primitive) {
            return (Householder.Primitive) transformation;
        } else if (transformation instanceof HouseholderReference<?>) {
            return ((Householder.Primitive) ((HouseholderReference<Double>) transformation).getWorker(FACTORY)).copy(transformation);
        } else {
            return new Householder.Primitive(transformation);
        }
    }

    static Rotation.Primitive cast(final Rotation<Double> transformation) {
        if (transformation instanceof Rotation.Primitive) {
            return (Rotation.Primitive) transformation;
        } else {
            return new Rotation.Primitive(transformation);
        }
    }

    private final PrimitiveMultiplyBoth multiplyBoth;
    private final PrimitiveMultiplyLeft multiplyLeft;
    private final PrimitiveMultiplyNeither multiplyNeither;
    private final PrimitiveMultiplyRight multiplyRight;
    private final int myColDim;
    private final int myRowDim;
    private final Array2D<Double> myUtility;

    private transient double[] myWorkerColumn;

    @SuppressWarnings("unused")
    private PrimitiveDenseStore(final double[] dataArray) {
        this(dataArray.length, 1, dataArray);
    }

    @SuppressWarnings("unused")
    private PrimitiveDenseStore(final int numbRows) {
        this(numbRows, 1);
    }

    PrimitiveDenseStore(final int numbRows, final int numbCols) {

        super(numbRows * numbCols);

        myRowDim = numbRows;
        myColDim = numbCols;

        myUtility = this.wrapInArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.getPrimitive(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.getPrimitive(myRowDim, myColDim);
        multiplyRight = MultiplyRight.getPrimitive(myRowDim, myColDim);
        multiplyNeither = MultiplyNeither.getPrimitive(myRowDim, myColDim);
    }

    PrimitiveDenseStore(final int numbRows, final int numbCols, final double[] dataArray) {

        super(dataArray);

        myRowDim = numbRows;
        myColDim = numbCols;

        myUtility = this.wrapInArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.getPrimitive(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.getPrimitive(myRowDim, myColDim);
        multiplyRight = MultiplyRight.getPrimitive(myRowDim, myColDim);
        multiplyNeither = MultiplyNeither.getPrimitive(myRowDim, myColDim);
    }

    public void accept(final Access2D<?> supplied) {
        for (long j = 0L; j < supplied.countColumns(); j++) {
            for (long i = 0L; i < supplied.countRows(); i++) {
                this.set(i, j, supplied.doubleValue(i, j));
            }
        }
    }

    public void add(final long row, final long col, final double addend) {
        myUtility.add(row, col, addend);
    }

    public void add(final long row, final long col, final Number addend) {
        myUtility.add(row, col, addend);
    }

    public Double aggregateAll(final Aggregator aggregator) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        final AggregatorFunction<Double> mainAggr = aggregator.getFunction(PrimitiveAggregator.getSet());

        if (mainAggr.isMergeable() && (tmpColDim > AggregateAll.THRESHOLD)) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int first, final int limit) {

                    final AggregatorFunction<Double> tmpPartAggr = aggregator.getFunction(PrimitiveAggregator.getSet());

                    PrimitiveDenseStore.this.visit(tmpRowDim * first, tmpRowDim * limit, 1, tmpPartAggr);

                    synchronized (mainAggr) {
                        mainAggr.merge(tmpPartAggr.get());
                    }
                }
            };

            tmpConquerer.invoke(0, tmpColDim, AggregateAll.THRESHOLD);

        } else {

            PrimitiveDenseStore.this.visit(0, this.size(), 1, mainAggr);
        }

        return mainAggr.get();
    }

    public void applyCholesky(final int iterationPoint, final BasicArray<Double> multipliers) {

        final double[] tmpData = data;
        final double[] tmpColumn = ((Primitive64Array) multipliers).data;

        if ((myColDim - iterationPoint - 1) > ApplyCholesky.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    ApplyCholesky.invoke(tmpData, myRowDim, first, limit, tmpColumn);
                }
            };

            tmpConquerer.invoke(iterationPoint + 1, myColDim, ApplyCholesky.THRESHOLD);

        } else {

            ApplyCholesky.invoke(tmpData, myRowDim, iterationPoint + 1, myColDim, tmpColumn);
        }
    }

    public void applyLDL(final int iterationPoint, final BasicArray<Double> multipliers) {

        final double[] tmpData = data;
        final double[] tmpColumn = ((Primitive64Array) multipliers).data;

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

    public void applyLU(final int iterationPoint, final BasicArray<Double> multipliers) {

        final double[] tmpData = data;
        final double[] tmpColumn = ((Primitive64Array) multipliers).data;

        if ((myColDim - iterationPoint - 1) > ApplyLU.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    ApplyLU.invoke(tmpData, myRowDim, first, limit, tmpColumn, iterationPoint);
                }
            };

            tmpConquerer.invoke(iterationPoint + 1, myColDim, ApplyLU.THRESHOLD);

        } else {

            ApplyLU.invoke(tmpData, myRowDim, iterationPoint + 1, myColDim, tmpColumn, iterationPoint);
        }
    }

    public Array1D<Double> asList() {
        return myUtility.asArray1D();
    }

    public void caxpy(final double aSclrA, final int aColX, final int aColY, final int aFirstRow) {
        AXPY.invoke(data, (aColY * myRowDim) + aFirstRow, aSclrA, data, (aColX * myRowDim) + aFirstRow, 0, myRowDim - aFirstRow);
    }

    public Array1D<ComplexNumber> computeInPlaceSchur(final PhysicalStore<Double> transformationCollector, final boolean eigenvalue) {

        // final PrimitiveDenseStore tmpThisCopy = this.copy();
        // final PrimitiveDenseStore tmpCollCopy = (PrimitiveDenseStore)
        // aTransformationCollector.copy();
        //
        // tmpThisCopy.computeInPlaceHessenberg(true);

        // Actual

        final double[] tmpData = data;

        final double[] tmpCollectorData = ((PrimitiveDenseStore) transformationCollector).data;

        final double[] tmpVctrWork = new double[this.getMinDim()];
        EvD1D.orthes(tmpData, tmpCollectorData, tmpVctrWork);

        // BasicLogger.logDebug("Schur Step", this);
        // BasicLogger.logDebug("Hessenberg", tmpThisCopy);

        final double[][] tmpDiags = EvD1D.hqr2(tmpData, tmpCollectorData, eigenvalue);
        final double[] aRawReal = tmpDiags[0];
        final double[] aRawImag = tmpDiags[1];
        final int tmpLength = Math.min(aRawReal.length, aRawImag.length);

        final ComplexArray retVal = ComplexArray.make(tmpLength);
        final ComplexNumber[] tmpRaw = retVal.data;

        for (int i = 0; i < tmpLength; i++) {
            tmpRaw[i] = ComplexNumber.of(aRawReal[i], aRawImag[i]);
        }

        return Array1D.COMPLEX.wrap(retVal);
    }

    public MatrixStore<Double> conjugate() {
        return this.transpose();
    }

    public PrimitiveDenseStore copy() {
        return new PrimitiveDenseStore(myRowDim, myColDim, this.copyOfData());
    }

    public long countColumns() {
        return myColDim;
    }

    public long countRows() {
        return myRowDim;
    }

    public void divideAndCopyColumn(final int row, final int column, final BasicArray<Double> destination) {

        final double[] tmpData = data;
        final int tmpRowDim = myRowDim;

        final double[] tmpDestination = ((Primitive64Array) destination).data;

        int tmpIndex = row + (column * tmpRowDim);
        final double tmpDenominator = tmpData[tmpIndex];

        for (int i = row + 1; i < tmpRowDim; i++) {
            tmpDestination[i] = tmpData[++tmpIndex] /= tmpDenominator;
        }
    }

    public double doubleValue(final long row, final long col) {
        return myUtility.doubleValue(row, col);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object anObj) {
        if (anObj instanceof MatrixStore) {
            return this.equals((MatrixStore<Double>) anObj, NumberContext.getGeneral(6));
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

        double tmpVal;
        for (int j = 0; j < tmpMin; j++) {
            tmpVal = this.doubleValue(tmpMin, j);
            this.set(tmpMin, j, this.doubleValue(tmpMax, j));
            this.set(tmpMax, j, tmpVal);
        }

        tmpVal = this.doubleValue(tmpMin, tmpMin);
        this.set(tmpMin, tmpMin, this.doubleValue(tmpMax, tmpMax));
        this.set(tmpMax, tmpMax, tmpVal);

        for (int ij = tmpMin + 1; ij < tmpMax; ij++) {
            tmpVal = this.doubleValue(ij, tmpMin);
            this.set(ij, tmpMin, this.doubleValue(tmpMax, ij));
            this.set(tmpMax, ij, tmpVal);
        }

        for (int i = tmpMax + 1; i < myRowDim; i++) {
            tmpVal = this.doubleValue(i, tmpMin);
            this.set(i, tmpMin, this.doubleValue(i, tmpMax));
            this.set(i, tmpMax, tmpVal);
        }
    }

    public void exchangeRows(final long rowA, final long rowB) {
        myUtility.exchangeRows(rowA, rowB);
    }

    public void fillByMultiplying(final Access1D<Double> left, final Access1D<Double> right) {

        final int complexity = ((int) left.count()) / myRowDim;

        if (left instanceof PrimitiveDenseStore) {
            if (right instanceof PrimitiveDenseStore) {
                multiplyNeither.invoke(data, PrimitiveDenseStore.cast(left).data, complexity, PrimitiveDenseStore.cast(right).data);
            } else {
                multiplyRight.invoke(data, PrimitiveDenseStore.cast(left).data, complexity, right);
            }
        } else {
            if (right instanceof PrimitiveDenseStore) {
                multiplyLeft.invoke(data, left, complexity, PrimitiveDenseStore.cast(right).data);
            } else {
                multiplyBoth.invoke(this, left, complexity, right);
            }
        }
    }

    public void fillColumn(final long row, final long col, final Access1D<Double> values) {
        myUtility.fillColumn(row, col, values);
    }

    public void fillColumn(final long row, final long col, final Double value) {
        myUtility.fillColumn(row, col, value);
    }

    public void fillColumn(final long row, final long col, final NullaryFunction<Double> supplier) {
        myUtility.fillColumn(row, col, supplier);
    }

    public void fillDiagonal(final long row, final long col, final Double value) {
        myUtility.fillDiagonal(row, col, value);
    }

    public void fillDiagonal(final long row, final long col, final NullaryFunction<Double> supplier) {
        myUtility.fillDiagonal(row, col, supplier);
    }

    @Override
    public void fillMatching(final Access1D<?> values) {

        if (values instanceof TransjugatedStore) {
            final TransjugatedStore<?> transposed = (TransjugatedStore<?>) values;

            if (myColDim > FillMatchingSingle.THRESHOLD) {

                final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                    @Override
                    public void conquer(final int first, final int limit) {
                        FillMatchingSingle.transpose(data, myRowDim, first, limit, transposed.getOriginal());
                    }

                };

                tmpConquerer.invoke(0, myColDim, FillMatchingSingle.THRESHOLD);

            } else {

                FillMatchingSingle.transpose(data, myRowDim, 0, myColDim, transposed.getOriginal());
            }

        } else {

            super.fillMatching(values);
        }
    }

    @Override
    public void fillMatching(final Access1D<Double> left, final BinaryFunction<Double> function, final Access1D<Double> right) {

        final int matchingCount = (int) FunctionUtils.min(this.count(), left.count(), right.count());

        if (myColDim > FillMatchingDual.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    Primitive64Array.invoke(data, first, limit, 1, left, function, right);
                }

            };

            tmpConquerer.invoke(0, matchingCount, FillMatchingDual.THRESHOLD * FillMatchingDual.THRESHOLD);

        } else {

            Primitive64Array.invoke(data, 0, matchingCount, 1, left, function, right);
        }
    }

    @Override
    public void fillMatching(final UnaryFunction<Double> function, final Access1D<Double> arguments) {

        final int matchingCount = (int) FunctionUtils.min(this.count(), arguments.count());

        if (myColDim > FillMatchingSingle.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    Primitive64Array.invoke(data, first, limit, 1, arguments, function);
                }

            };

            tmpConquerer.invoke(0, matchingCount, FillMatchingSingle.THRESHOLD * FillMatchingSingle.THRESHOLD);

        } else {

            Primitive64Array.invoke(data, 0, matchingCount, 1, arguments, function);
        }
    }

    public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
        this.set(row, col, values.doubleValue(valueIndex));
    }

    public void fillOne(final long row, final long col, final Double value) {
        myUtility.fillOne(row, col, value);
    }

    public void fillOne(final long row, final long col, final NullaryFunction<Double> supplier) {
        myUtility.fillOne(row, col, supplier);
    }

    public void fillRow(final long row, final long col, final Access1D<Double> values) {
        myUtility.fillRow(row, col, values);
    }

    public void fillRow(final long row, final long col, final Double value) {
        myUtility.fillRow(row, col, value);
    }

    public void fillRow(final long row, final long col, final NullaryFunction<Double> supplier) {
        myUtility.fillRow(row, col, supplier);
    }

    public boolean generateApplyAndCopyHouseholderColumn(final int row, final int column, final Householder<Double> destination) {
        return GenerateApplyAndCopyHouseholderColumn.invoke(data, myRowDim, row, column, (Householder.Primitive) destination);
    }

    public boolean generateApplyAndCopyHouseholderRow(final int row, final int column, final Householder<Double> destination) {
        return GenerateApplyAndCopyHouseholderRow.invoke(data, myRowDim, row, column, (Householder.Primitive) destination);
    }

    public final MatrixStore<Double> get() {
        return this;
    }

    public Double get(final long row, final long col) {
        return myUtility.get(row, col);
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
    public void modifyAll(final UnaryFunction<Double> modifier) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > ModifyAll.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int first, final int limit) {
                    PrimitiveDenseStore.this.modify(tmpRowDim * first, tmpRowDim * limit, 1, modifier);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, ModifyAll.THRESHOLD);

        } else {

            this.modify(tmpRowDim * 0, tmpRowDim * tmpColDim, 1, modifier);
        }
    }

    public void modifyColumn(final long row, final long col, final UnaryFunction<Double> modifier) {
        myUtility.modifyColumn(row, col, modifier);
    }

    public void modifyDiagonal(final long row, final long col, final UnaryFunction<Double> modifier) {
        myUtility.modifyDiagonal(row, col, modifier);
    }

    public void modifyMatching(final Access1D<Double> left, final BinaryFunction<Double> function) {
        final long tmpLimit = FunctionUtils.min(left.count(), this.count(), this.count());
        for (long i = 0L; i < tmpLimit; i++) {
            this.set(i, function.invoke(left.doubleValue(i), this.doubleValue(i)));
        }
    }

    public void modifyMatching(final BinaryFunction<Double> function, final Access1D<Double> right) {
        final long tmpLimit = FunctionUtils.min(this.count(), right.count(), this.count());
        for (long i = 0L; i < tmpLimit; i++) {
            this.set(i, function.invoke(this.doubleValue(i), right.doubleValue(i)));
        }
    }

    public void modifyOne(final long row, final long col, final UnaryFunction<Double> modifier) {

        double tmpValue = this.doubleValue(row, col);

        tmpValue = modifier.invoke(tmpValue);

        this.set(row, col, tmpValue);
    }

    public void modifyRow(final long row, final long col, final UnaryFunction<Double> modifier) {
        myUtility.modifyRow(row, col, modifier);
    }

    public MatrixStore<Double> multiply(final MatrixStore<Double> right) {

        final PrimitiveDenseStore retVal = FACTORY.makeZero(myRowDim, right.count() / myColDim);

        if (right instanceof PrimitiveDenseStore) {
            retVal.multiplyNeither.invoke(retVal.data, data, myColDim, PrimitiveDenseStore.cast(right).data);
        } else {
            retVal.multiplyRight.invoke(retVal.data, data, myColDim, right);
        }

        return retVal;
    }

    public Double multiplyBoth(final Access1D<Double> leftAndRight) {

        final PhysicalStore<Double> tmpStep1 = FACTORY.makeZero(1L, leftAndRight.count());
        final PhysicalStore<Double> tmpStep2 = FACTORY.makeZero(1L, 1L);

        tmpStep1.fillByMultiplying(leftAndRight, this);
        tmpStep2.fillByMultiplying(tmpStep1, leftAndRight);

        return tmpStep2.get(0L);
    }

    public void negateColumn(final int column) {
        myUtility.modifyColumn(0, column, NEGATE);
    }

    public PhysicalStore.Factory<Double, PrimitiveDenseStore> physical() {
        return FACTORY;
    }

    public final ElementsConsumer<Double> regionByColumns(final int... columns) {
        return new ElementsConsumer.ColumnsRegion<>(this, multiplyBoth, columns);
    }

    public final ElementsConsumer<Double> regionByLimits(final int rowLimit, final int columnLimit) {
        return new ElementsConsumer.LimitRegion<>(this, multiplyBoth, rowLimit, columnLimit);
    }

    public final ElementsConsumer<Double> regionByOffsets(final int rowOffset, final int columnOffset) {
        return new ElementsConsumer.OffsetRegion<>(this, multiplyBoth, rowOffset, columnOffset);
    }

    public final ElementsConsumer<Double> regionByRows(final int... rows) {
        return new ElementsConsumer.RowsRegion<>(this, multiplyBoth, rows);
    }

    public final ElementsConsumer<Double> regionByTransposing() {
        return new ElementsConsumer.TransposedRegion<>(this, multiplyBoth);
    }

    public void rotateRight(final int low, final int high, final double cos, final double sin) {
        RotateRight.invoke(data, myRowDim, low, high, cos, sin);
    }

    public void set(final long row, final long col, final double value) {
        myUtility.set(row, col, value);
    }

    public void set(final long row, final long col, final Number value) {
        myUtility.set(row, col, value);
    }

    public void setToIdentity(final int col) {
        myUtility.set(col, col, ONE);
        myUtility.fillColumn(col + 1, col, ZERO);
    }

    public Array1D<Double> sliceColumn(final long row, final long col) {
        return myUtility.sliceColumn(row, col);
    }

    public Array1D<Double> sliceDiagonal(final long row, final long col) {
        return myUtility.sliceDiagonal(row, col);
    }

    public Array1D<Double> sliceRange(final long first, final long limit) {
        return myUtility.sliceRange(first, limit);
    }

    public Array1D<Double> sliceRow(final long row, final long col) {
        return myUtility.sliceRow(row, col);
    }

    public void substituteBackwards(final Access2D<Double> body, final boolean unitDiagonal, final boolean conjugated, final boolean hermitian) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > SubstituteBackwards.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int first, final int limit) {
                    SubstituteBackwards.invoke(PrimitiveDenseStore.this.data, tmpRowDim, first, limit, body, unitDiagonal, conjugated, hermitian);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, SubstituteBackwards.THRESHOLD);

        } else {

            SubstituteBackwards.invoke(data, tmpRowDim, 0, tmpColDim, body, unitDiagonal, conjugated, hermitian);
        }
    }

    public void substituteForwards(final Access2D<Double> body, final boolean unitDiagonal, final boolean conjugated, final boolean identity) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > SubstituteForwards.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int first, final int limit) {
                    SubstituteForwards.invoke(PrimitiveDenseStore.this.data, tmpRowDim, first, limit, body, unitDiagonal, conjugated, identity);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, SubstituteForwards.THRESHOLD);

        } else {

            SubstituteForwards.invoke(data, tmpRowDim, 0, tmpColDim, body, unitDiagonal, conjugated, identity);
        }
    }

    public void supplyTo(final ElementsConsumer<Double> receiver) {
        receiver.fillMatching(this);
    }

    public PrimitiveScalar toScalar(final long row, final long column) {
        return PrimitiveScalar.of(this.doubleValue(row, column));
    }

    @Override
    public String toString() {
        return Access2D.toString(this);
    }

    public void transformLeft(final Householder<Double> transformation, final int firstColumn) {

        final Householder.Primitive tmpTransf = PrimitiveDenseStore.cast(transformation);

        final double[] tmpData = data;

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if ((tmpColDim - firstColumn) > HouseholderLeft.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int first, final int limit) {
                    HouseholderLeft.invoke(tmpData, tmpRowDim, first, limit, tmpTransf);
                }

            };

            tmpConquerer.invoke(firstColumn, tmpColDim, HouseholderLeft.THRESHOLD);

        } else {

            HouseholderLeft.invoke(tmpData, tmpRowDim, firstColumn, tmpColDim, tmpTransf);
        }
    }

    public void transformLeft(final Rotation<Double> transformation) {

        final Rotation.Primitive tmpTransf = PrimitiveDenseStore.cast(transformation);

        final int tmpLow = tmpTransf.low;
        final int tmpHigh = tmpTransf.high;

        if (tmpLow != tmpHigh) {
            if (!Double.isNaN(tmpTransf.cos) && !Double.isNaN(tmpTransf.sin)) {
                RotateLeft.invoke(data, myRowDim, tmpLow, tmpHigh, tmpTransf.cos, tmpTransf.sin);
            } else {
                myUtility.exchangeRows(tmpLow, tmpHigh);
            }
        } else {
            if (!Double.isNaN(tmpTransf.cos)) {
                myUtility.modifyRow(tmpLow, 0L, MULTIPLY.second(tmpTransf.cos));
            } else if (!Double.isNaN(tmpTransf.sin)) {
                myUtility.modifyRow(tmpLow, 0L, DIVIDE.second(tmpTransf.sin));
            } else {
                myUtility.modifyRow(tmpLow, 0, NEGATE);
            }
        }
    }

    public void transformRight(final Householder<Double> transformation, final int firstRow) {

        final Householder.Primitive tmpTransf = PrimitiveDenseStore.cast(transformation);

        final double[] tmpData = data;

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        final double[] tmpWorker = this.getWorkerColumn();

        if ((tmpRowDim - firstRow) > HouseholderRight.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int first, final int limit) {
                    HouseholderRight.invoke(tmpData, tmpRowDim, first, limit, tmpColDim, tmpTransf, tmpWorker);
                }

            };

            tmpConquerer.invoke(firstRow, tmpRowDim, HouseholderRight.THRESHOLD);

        } else {

            HouseholderRight.invoke(tmpData, tmpRowDim, firstRow, tmpRowDim, tmpColDim, tmpTransf, tmpWorker);
        }
    }

    public void transformRight(final Rotation<Double> transformation) {

        final Rotation.Primitive tmpTransf = PrimitiveDenseStore.cast(transformation);

        final int tmpLow = tmpTransf.low;
        final int tmpHigh = tmpTransf.high;

        if (tmpLow != tmpHigh) {
            if (!Double.isNaN(tmpTransf.cos) && !Double.isNaN(tmpTransf.sin)) {
                RotateRight.invoke(data, myRowDim, tmpLow, tmpHigh, tmpTransf.cos, tmpTransf.sin);
            } else {
                myUtility.exchangeColumns(tmpLow, tmpHigh);
            }
        } else {
            if (!Double.isNaN(tmpTransf.cos)) {
                myUtility.modifyColumn(0L, tmpHigh, MULTIPLY.second(tmpTransf.cos));
            } else if (!Double.isNaN(tmpTransf.sin)) {
                myUtility.modifyColumn(0L, tmpHigh, DIVIDE.second(tmpTransf.sin));
            } else {
                myUtility.modifyColumn(0, tmpHigh, NEGATE);
            }
        }
    }

    public void transformSymmetric(final Householder<Double> transformation) {
        HouseholderHermitian.invoke(data, PrimitiveDenseStore.cast(transformation), this.getWorkerColumn());
    }

    public MatrixStore<Double> transpose() {
        return new TransposedStore<>(this);
    }

    public void tred2(final BasicArray<Double> mainDiagonal, final BasicArray<Double> offDiagonal, final boolean yesvecs) {
        HouseholderHermitian.tred2j(data, ((Primitive64Array) mainDiagonal).data, ((Primitive64Array) offDiagonal).data, yesvecs);
    }

    public void visitColumn(final long row, final long col, final VoidFunction<Double> visitor) {
        myUtility.visitColumn(row, col, visitor);
    }

    public void visitDiagonal(final long row, final long col, final VoidFunction<Double> visitor) {
        myUtility.visitDiagonal(row, col, visitor);
    }

    public void visitRow(final long row, final long col, final VoidFunction<Double> visitor) {
        myUtility.visitRow(row, col, visitor);
    }

    private double[] getWorkerColumn() {
        if (myWorkerColumn != null) {
            Arrays.fill(myWorkerColumn, ZERO);
        } else {
            myWorkerColumn = new double[myRowDim];
        }
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
