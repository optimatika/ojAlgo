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
package org.ojalgo.matrix.store;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.Arrays;
import java.util.List;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.Array2D;
import org.ojalgo.array.BasicArray;
import org.ojalgo.array.ComplexArray;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.array.operation.*;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.machine.JavaType;
import org.ojalgo.machine.MemoryEstimator;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.decomposition.EvD1D;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.HouseholderReference;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.type.context.NumberContext;

/**
 * A {@linkplain double} implementation of {@linkplain PhysicalStore}.
 *
 * @author apete
 */
public final class Primitive64Store extends Primitive64Array implements PhysicalStore<Double>, DecompositionStore<Double> {

    public static final PhysicalStore.Factory<Double, Primitive64Store> FACTORY = new PrimitiveFactory<Primitive64Store>() {

        public Primitive64Store columns(final Access1D<?>... source) {

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

            return new Primitive64Store(tmpRowDim, tmpColDim, tmpData);
        }

        public Primitive64Store columns(final Comparable<?>[]... source) {

            final int tmpRowDim = source[0].length;
            final int tmpColDim = source.length;

            final double[] tmpData = new double[tmpRowDim * tmpColDim];

            Comparable<?>[] tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = Scalar.doubleValue(tmpColumn[i]);
                }
            }

            return new Primitive64Store(tmpRowDim, tmpColDim, tmpData);
        }

        public Primitive64Store columns(final double[]... source) {

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

            return new Primitive64Store(tmpRowDim, tmpColDim, tmpData);
        }

        public Primitive64Store columns(final List<? extends Comparable<?>>... source) {

            final int tmpRowDim = source[0].size();
            final int tmpColDim = source.length;

            final double[] tmpData = new double[tmpRowDim * tmpColDim];

            List<? extends Comparable<?>> tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = Scalar.doubleValue(tmpColumn.get(i));
                }
            }

            return new Primitive64Store(tmpRowDim, tmpColDim, tmpData);
        }

        public Primitive64Store copy(final Access2D<?> source) {

            final int tmpRowDim = (int) source.countRows();
            final int tmpColDim = (int) source.countColumns();

            final Primitive64Store retVal = new Primitive64Store(tmpRowDim, tmpColDim);

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

        public Primitive64Store make(final long rows, final long columns) {
            return new Primitive64Store((int) rows, (int) columns);
        }

        public Primitive64Store makeEye(final long rows, final long columns) {

            final Primitive64Store retVal = this.make(rows, columns);

            retVal.fillDiagonal(ONE);

            return retVal;
        }

        public Primitive64Store rows(final Access1D<?>... source) {

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

            return new Primitive64Store(tmpRowDim, tmpColDim, tmpData);
        }

        public Primitive64Store rows(final Comparable<?>[]... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].length;

            final double[] tmpData = new double[tmpRowDim * tmpColDim];

            Comparable<?>[] tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = Scalar.doubleValue(tmpRow[j]);
                }
            }

            return new Primitive64Store(tmpRowDim, tmpColDim, tmpData);
        }

        public Primitive64Store rows(final double[]... source) {

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

            return new Primitive64Store(tmpRowDim, tmpColDim, tmpData);
        }

        public Primitive64Store rows(final List<? extends Comparable<?>>... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].size();

            final double[] tmpData = new double[tmpRowDim * tmpColDim];

            List<? extends Comparable<?>> tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = Scalar.doubleValue(tmpRow.get(j));
                }
            }

            return new Primitive64Store(tmpRowDim, tmpColDim, tmpData);
        }

        public Primitive64Store transpose(final Access2D<?> source) {

            final Primitive64Store retVal = new Primitive64Store((int) source.countColumns(), (int) source.countRows());

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

    static final long SHALLOW_SIZE = MemoryEstimator.estimateObject(Primitive64Store.class);

    /**
     * Extracts the argument of the ComplexNumber elements to a new primitive double valued matrix.
     */
    public static Primitive64Store getComplexArgument(final Access2D<ComplexNumber> arg) {

        final long numberOfRows = arg.countRows();
        final long numberOfColumns = arg.countColumns();

        final Primitive64Store retVal = FACTORY.makeZero(numberOfRows, numberOfColumns);

        Mutate1D.copyComplexArgument(arg, retVal);

        return retVal;
    }

    /**
     * Extracts the imaginary part of the ComplexNumber elements to a new primitive double valued matrix.
     */
    public static Primitive64Store getComplexImaginary(final Access2D<ComplexNumber> arg) {

        final long numberOfRows = arg.countRows();
        final long numberOfColumns = arg.countColumns();

        final Primitive64Store retVal = FACTORY.makeZero(numberOfRows, numberOfColumns);

        Mutate1D.copyComplexImaginary(arg, retVal);

        return retVal;
    }

    /**
     * Extracts the modulus of the ComplexNumber elements to a new primitive double valued matrix.
     */
    public static Primitive64Store getComplexModulus(final Access2D<ComplexNumber> arg) {

        final long numberOfRows = arg.countRows();
        final long numberOfColumns = arg.countColumns();

        final Primitive64Store retVal = FACTORY.makeZero(numberOfRows, numberOfColumns);

        Mutate1D.copyComplexModulus(arg, retVal);

        return retVal;
    }

    /**
     * Extracts the real part of the ComplexNumber elements to a new primitive double valued matrix.
     */
    public static Primitive64Store getComplexReal(final Access2D<ComplexNumber> arg) {

        final long numberOfRows = arg.countRows();
        final long numberOfColumns = arg.countColumns();

        final Primitive64Store retVal = FACTORY.makeZero(numberOfRows, numberOfColumns);

        Mutate1D.copyComplexReal(arg, retVal);

        return retVal;
    }

    public static Primitive64Store wrap(final double... data) {
        return new Primitive64Store(data);
    }

    public static Primitive64Store wrap(final double[] data, final int structure) {
        return new Primitive64Store(structure, data.length / structure, data);
    }

    static Primitive64Store cast(final Access1D<Double> matrix) {
        if (matrix instanceof Primitive64Store) {
            return (Primitive64Store) matrix;
        } else if (matrix instanceof Access2D<?>) {
            return FACTORY.copy((Access2D<?>) matrix);
        } else {
            return FACTORY.columns(matrix);
        }
    }

    static Householder.Primitive64 cast(final Householder<Double> transformation) {
        if (transformation instanceof Householder.Primitive64) {
            return (Householder.Primitive64) transformation;
        } else if (transformation instanceof HouseholderReference<?>) {
            return ((Householder.Primitive64) ((HouseholderReference<Double>) transformation).getWorker(FACTORY)).copy(transformation);
        } else {
            return new Householder.Primitive64(transformation);
        }
    }

    static Rotation.Primitive cast(final Rotation<Double> transformation) {
        if (transformation instanceof Rotation.Primitive) {
            return (Rotation.Primitive) transformation;
        } else {
            return new Rotation.Primitive(transformation);
        }
    }

    private final MultiplyBoth.Primitive multiplyBoth;
    private final MultiplyLeft.Primitive64 multiplyLeft;
    private final MultiplyNeither.Primitive64 multiplyNeither;
    private final MultiplyRight.Primitive64 multiplyRight;
    private final int myColDim;
    private final int myRowDim;
    private final Array2D<Double> myUtility;
    private transient double[] myWorkerColumn;

    @SuppressWarnings("unused")
    private Primitive64Store(final double[] dataArray) {
        this(dataArray.length, 1, dataArray);
    }

    @SuppressWarnings("unused")
    private Primitive64Store(final int numbRows) {
        this(numbRows, 1);
    }

    Primitive64Store(final int numbRows, final int numbCols) {

        super(numbRows * numbCols);

        myRowDim = numbRows;
        myColDim = numbCols;

        myUtility = this.wrapInArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.newPrimitive64(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.newPrimitive64(myRowDim, myColDim);
        multiplyRight = MultiplyRight.newPrimitive64(myRowDim, myColDim);
        multiplyNeither = MultiplyNeither.newPrimitive64(myRowDim, myColDim);
    }

    Primitive64Store(final int numbRows, final int numbCols, final double[] dataArray) {

        super(dataArray);

        myRowDim = numbRows;
        myColDim = numbCols;

        myUtility = this.wrapInArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.newPrimitive64(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.newPrimitive64(myRowDim, myColDim);
        multiplyRight = MultiplyRight.newPrimitive64(myRowDim, myColDim);
        multiplyNeither = MultiplyNeither.newPrimitive64(myRowDim, myColDim);
    }

    public void accept(final Access2D<?> supplied) {
        for (long j = 0L; j < supplied.countColumns(); j++) {
            for (long i = 0L; i < supplied.countRows(); i++) {
                this.set(i, j, supplied.doubleValue(i, j));
            }
        }
    }

    public void add(final long row, final long col, final Comparable<?> addend) {
        myUtility.add(row, col, addend);
    }

    public void add(final long row, final long col, final double addend) {
        myUtility.add(row, col, addend);
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

        final double[] column = ((Primitive64Array) multipliers).data;

        if ((myColDim - iterationPoint - 1) > ApplyLDL.THRESHOLD) {

            final DivideAndConquer conquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    ApplyLDL.invoke(data, myRowDim, first, limit, column, iterationPoint);
                }
            };

            conquerer.invoke(iterationPoint + 1, myColDim, ApplyLDL.THRESHOLD);

        } else {

            ApplyLDL.invoke(data, myRowDim, iterationPoint + 1, myColDim, column, iterationPoint);
        }
    }

    public void applyLU(final int iterationPoint, final BasicArray<Double> multipliers) {

        final double[] column = ((Primitive64Array) multipliers).data;

        if ((myColDim - iterationPoint - 1) > ApplyLU.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    ApplyLU.invoke(data, myRowDim, first, limit, column, iterationPoint);
                }
            };

            tmpConquerer.invoke(iterationPoint + 1, myColDim, ApplyLU.THRESHOLD);

        } else {

            ApplyLU.invoke(data, myRowDim, iterationPoint + 1, myColDim, column, iterationPoint);
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

        final double[] tmpCollectorData = ((Primitive64Store) transformationCollector).data;

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

    public Primitive64Store copy() {
        return new Primitive64Store(myRowDim, myColDim, this.copyOfData());
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

        final int indexMin = Math.min(indexA, indexB);
        final int indexMax = Math.max(indexA, indexB);

        double tmpVal;

        for (int j = 0; j < indexMin; j++) {
            tmpVal = this.doubleValue(indexMin, j);
            this.set(indexMin, j, this.doubleValue(indexMax, j));
            this.set(indexMax, j, tmpVal);
        }

        tmpVal = this.doubleValue(indexMin, indexMin);
        this.set(indexMin, indexMin, this.doubleValue(indexMax, indexMax));
        this.set(indexMax, indexMax, tmpVal);

        for (int ij = indexMin + 1; ij < indexMax; ij++) {
            tmpVal = this.doubleValue(ij, indexMin);
            this.set(ij, indexMin, this.doubleValue(indexMax, ij));
            this.set(indexMax, ij, tmpVal);
        }

        for (int i = indexMax + 1; i < myRowDim; i++) {
            tmpVal = this.doubleValue(i, indexMin);
            this.set(i, indexMin, this.doubleValue(i, indexMax));
            this.set(i, indexMax, tmpVal);
        }

    }

    public void exchangeRows(final long rowA, final long rowB) {
        myUtility.exchangeRows(rowA, rowB);
    }

    public void fillByMultiplying(final Access1D<Double> left, final Access1D<Double> right) {

        final int complexity = Math.toIntExact(left.count() / this.countRows());
        if (complexity != Math.toIntExact(right.count() / this.countColumns())) {
            ProgrammingError.throwForMultiplicationNotPossible();
        }

        if (left instanceof Primitive64Store) {
            if (right instanceof Primitive64Store) {
                multiplyNeither.invoke(data, Primitive64Store.cast(left).data, complexity, Primitive64Store.cast(right).data);
            } else {
                multiplyRight.invoke(data, Primitive64Store.cast(left).data, complexity, right);
            }
        } else {
            if (right instanceof Primitive64Store) {
                multiplyLeft.invoke(data, left, complexity, Primitive64Store.cast(right).data);
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

    public void fillColumn(final long row, final long col, final NullaryFunction<?> supplier) {
        myUtility.fillColumn(row, col, supplier);
    }

    public void fillDiagonal(final long row, final long col, final Double value) {
        myUtility.fillDiagonal(row, col, value);
    }

    public void fillDiagonal(final long row, final long col, final NullaryFunction<?> supplier) {
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

        int matchingCount = MissingMath.toMinIntExact(this.count(), left.count(), right.count());

        if (myColDim > FillMatchingDual.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    OperationBinary.invoke(data, first, limit, 1, left, function, right);
                }

            };

            tmpConquerer.invoke(0, matchingCount, FillMatchingDual.THRESHOLD * FillMatchingDual.THRESHOLD);

        } else {

            OperationBinary.invoke(data, 0, matchingCount, 1, left, function, right);
        }
    }

    @Override
    public void fillMatching(final UnaryFunction<Double> function, final Access1D<Double> arguments) {

        int matchingCount = MissingMath.toMinIntExact(this.count(), arguments.count());

        if (myColDim > FillMatchingSingle.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    OperationUnary.invoke(data, first, limit, 1, arguments, function);
                }

            };

            tmpConquerer.invoke(0, matchingCount, FillMatchingSingle.THRESHOLD * FillMatchingSingle.THRESHOLD);

        } else {

            OperationUnary.invoke(data, 0, matchingCount, 1, arguments, function);
        }
    }

    public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
        this.set(row, col, values.doubleValue(valueIndex));
    }

    public void fillOne(final long row, final long col, final Double value) {
        myUtility.fillOne(row, col, value);
    }

    public void fillOne(final long row, final long col, final NullaryFunction<?> supplier) {
        myUtility.fillOne(row, col, supplier);
    }

    public void fillRow(final long row, final long col, final Access1D<Double> values) {
        myUtility.fillRow(row, col, values);
    }

    public void fillRow(final long row, final long col, final Double value) {
        myUtility.fillRow(row, col, value);
    }

    public void fillRow(final long row, final long col, final NullaryFunction<?> supplier) {
        myUtility.fillRow(row, col, supplier);
    }

    public boolean generateApplyAndCopyHouseholderColumn(final int row, final int column, final Householder<Double> destination) {
        return GenerateApplyAndCopyHouseholderColumn.invoke(data, myRowDim, row, column, (Householder.Primitive64) destination);
    }

    public boolean generateApplyAndCopyHouseholderRow(final int row, final int column, final Householder<Double> destination) {
        return GenerateApplyAndCopyHouseholderRow.invoke(data, myRowDim, row, column, (Householder.Primitive64) destination);
    }

    public MatrixStore<Double> get() {
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

        final int numberOfRows = myRowDim;
        final int numberOfCols = myColDim;

        if (numberOfCols > ModifyAll.THRESHOLD) {

            final DivideAndConquer conquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int first, final int limit) {
                    Primitive64Store.this.modify(numberOfRows * first, numberOfRows * limit, 1, modifier);
                }

            };

            conquerer.invoke(0, numberOfCols, ModifyAll.THRESHOLD);

        } else {

            this.modify(0, numberOfRows * numberOfCols, 1, modifier);
        }
    }

    public void modifyColumn(final long row, final long col, final UnaryFunction<Double> modifier) {
        myUtility.modifyColumn(row, col, modifier);
    }

    public void modifyDiagonal(final long row, final long col, final UnaryFunction<Double> modifier) {
        myUtility.modifyDiagonal(row, col, modifier);
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

        final Primitive64Store retVal = FACTORY.makeZero(myRowDim, right.count() / myColDim);

        if (right instanceof Primitive64Store) {
            retVal.multiplyNeither.invoke(retVal.data, data, myColDim, Primitive64Store.cast(right).data);
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
        myUtility.modifyColumn(0, column, PrimitiveMath.NEGATE);
    }

    public PhysicalStore.Factory<Double, Primitive64Store> physical() {
        return FACTORY;
    }

    public TransformableRegion<Double> regionByColumns(final int... columns) {
        return new TransformableRegion.ColumnsRegion<>(this, multiplyBoth, columns);
    }

    public TransformableRegion<Double> regionByLimits(final int rowLimit, final int columnLimit) {
        return new TransformableRegion.LimitRegion<>(this, multiplyBoth, rowLimit, columnLimit);
    }

    public TransformableRegion<Double> regionByOffsets(final int rowOffset, final int columnOffset) {
        return new TransformableRegion.OffsetRegion<>(this, multiplyBoth, rowOffset, columnOffset);
    }

    public TransformableRegion<Double> regionByRows(final int... rows) {
        return new TransformableRegion.RowsRegion<>(this, multiplyBoth, rows);
    }

    public TransformableRegion<Double> regionByTransposing() {
        return new TransformableRegion.TransposedRegion<>(this, multiplyBoth);
    }

    public void rotateRight(final int low, final int high, final double cos, final double sin) {
        RotateRight.invoke(data, myRowDim, low, high, cos, sin);
    }

    public void set(final long row, final long col, final Comparable<?> value) {
        myUtility.set(row, col, value);
    }

    public void set(final long row, final long col, final double value) {
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
                    SubstituteBackwards.invoke(Primitive64Store.this.data, tmpRowDim, first, limit, body, unitDiagonal, conjugated, hermitian);
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
                    SubstituteForwards.invoke(Primitive64Store.this.data, tmpRowDim, first, limit, body, unitDiagonal, conjugated, identity);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, SubstituteForwards.THRESHOLD);

        } else {

            SubstituteForwards.invoke(data, tmpRowDim, 0, tmpColDim, body, unitDiagonal, conjugated, identity);
        }
    }

    public PrimitiveScalar toScalar(final long row, final long column) {
        return PrimitiveScalar.of(this.doubleValue(row, column));
    }

    @Override
    public String toString() {
        return Access2D.toString(this);
    }

    public void transformLeft(final Householder<Double> transformation, final int firstColumn) {

        final Householder.Primitive64 tmpTransf = Primitive64Store.cast(transformation);

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

        final Rotation.Primitive tmpTransf = Primitive64Store.cast(transformation);

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
                myUtility.modifyRow(tmpLow, 0L, PrimitiveMath.MULTIPLY.second(tmpTransf.cos));
            } else if (!Double.isNaN(tmpTransf.sin)) {
                myUtility.modifyRow(tmpLow, 0L, PrimitiveMath.DIVIDE.second(tmpTransf.sin));
            } else {
                myUtility.modifyRow(tmpLow, 0, PrimitiveMath.NEGATE);
            }
        }
    }

    public void transformRight(final Householder<Double> transformation, final int firstRow) {

        final Householder.Primitive64 tmpTransf = Primitive64Store.cast(transformation);

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

        final Rotation.Primitive tmpTransf = Primitive64Store.cast(transformation);

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
                myUtility.modifyColumn(0L, tmpHigh, PrimitiveMath.MULTIPLY.second(tmpTransf.cos));
            } else if (!Double.isNaN(tmpTransf.sin)) {
                myUtility.modifyColumn(0L, tmpHigh, PrimitiveMath.DIVIDE.second(tmpTransf.sin));
            } else {
                myUtility.modifyColumn(0, tmpHigh, PrimitiveMath.NEGATE);
            }
        }
    }

    public void transformSymmetric(final Householder<Double> transformation) {
        HouseholderHermitian.invoke(data, Primitive64Store.cast(transformation), this.getWorkerColumn());
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
