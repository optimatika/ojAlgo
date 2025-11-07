/*
 * Copyright 1997-2025 Optimatika
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

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.Array2D;
import org.ojalgo.array.ArrayC128;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.BasicArray;
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
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.decomposition.EvD1D;
import org.ojalgo.matrix.operation.HouseholderLeft;
import org.ojalgo.matrix.operation.HouseholderRight;
import org.ojalgo.matrix.operation.MultiplyBoth;
import org.ojalgo.matrix.operation.MultiplyLeft;
import org.ojalgo.matrix.operation.MultiplyNeither;
import org.ojalgo.matrix.operation.MultiplyRight;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.HouseholderReference;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Factory2D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.type.math.MathType;

/**
 * A {@linkplain double} implementation of {@linkplain PhysicalStore}.
 *
 * @author apete
 */
public final class R064Store extends ArrayR064 implements PhysicalStore<Double>, DecompositionStore<Double>, Factory2D.Builder<R064Store> {

    public static final PrimitiveFactory<R064Store> FACTORY = new PrimitiveFactory<>() {

        @Override
        public R064Store copy(final Access2D<?> source) {

            int nbRows = source.getRowDim();
            int nbCols = source.getColDim();

            R064Store retVal = this.make(nbRows, nbCols);

            if (nbCols > FillMatchingSingle.THRESHOLD) {

                final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                    @Override
                    public void conquer(final int aFirst, final int aLimit) {
                        FillMatchingSingle.copy(retVal.data, nbRows, aFirst, aLimit, source);
                    }

                };

                tmpConquerer.invoke(0, nbCols, FillMatchingSingle.THRESHOLD);

            } else {

                FillMatchingSingle.copy(retVal.data, nbRows, 0, nbCols, source);
            }

            return retVal;
        }

        @Override
        public MathType getMathType() {
            return MathType.R064;
        }

        @Override
        public R064Store make(final int rows, final int columns) {
            return new R064Store(rows, columns, new double[rows * columns]);
        }

        @Override
        public R064Store transpose(final Access2D<?> source) {

            R064Store retVal = this.make(source.getColDim(), source.getRowDim());

            int tmpRowDim = retVal.getRowDim();
            int tmpColDim = retVal.getColDim();

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

    static final long SHALLOW_SIZE = MemoryEstimator.estimateObject(R064Store.class);

    /**
     * Extracts the argument of the ComplexNumber elements to a new primitive double valued matrix.
     */
    public static R064Store getComplexArgument(final Access2D<ComplexNumber> arg) {

        final long numberOfRows = arg.countRows();
        final long numberOfColumns = arg.countColumns();

        final R064Store retVal = FACTORY.make(numberOfRows, numberOfColumns);

        Mutate1D.copyComplexArgument(arg, retVal);

        return retVal;
    }

    /**
     * Extracts the imaginary part of the ComplexNumber elements to a new primitive double valued matrix.
     */
    public static R064Store getComplexImaginary(final Access2D<ComplexNumber> arg) {

        final long numberOfRows = arg.countRows();
        final long numberOfColumns = arg.countColumns();

        final R064Store retVal = FACTORY.make(numberOfRows, numberOfColumns);

        Mutate1D.copyComplexImaginary(arg, retVal);

        return retVal;
    }

    /**
     * Extracts the modulus of the ComplexNumber elements to a new primitive double valued matrix.
     */
    public static R064Store getComplexModulus(final Access2D<ComplexNumber> arg) {

        final long numberOfRows = arg.countRows();
        final long numberOfColumns = arg.countColumns();

        final R064Store retVal = FACTORY.make(numberOfRows, numberOfColumns);

        Mutate1D.copyComplexModulus(arg, retVal);

        return retVal;
    }

    /**
     * Extracts the real part of the ComplexNumber elements to a new primitive double valued matrix.
     */
    public static R064Store getComplexReal(final Access2D<ComplexNumber> arg) {

        final long numberOfRows = arg.countRows();
        final long numberOfColumns = arg.countColumns();

        final R064Store retVal = FACTORY.make(numberOfRows, numberOfColumns);

        Mutate1D.copyComplexReal(arg, retVal);

        return retVal;
    }

    public static R064Store wrap(final double... data) {
        return R064Store.wrap(data, data.length);
    }

    public static R064Store wrap(final double[] data, final int structure) {
        return new R064Store(structure, data.length / structure, data);
    }

    static R064Store cast(final Access1D<?> matrix) {
        if (matrix instanceof R064Store) {
            return (R064Store) matrix;
        } else if (matrix instanceof Access2D<?>) {
            return FACTORY.copy((Access2D<?>) matrix);
        } else {
            return FACTORY.column(matrix);
        }
    }

    static Householder.Primitive64 cast(final Householder<Double> transformation) {
        if (transformation instanceof Householder.Primitive64) {
            return (Householder.Primitive64) transformation;
        }
        if (transformation instanceof HouseholderReference<?>) {
            return ((Householder.Primitive64) ((HouseholderReference<Double>) transformation).getWorker(FACTORY)).copy(transformation);
        }
        return new Householder.Primitive64(transformation);
    }

    static Rotation.Primitive cast(final Rotation<Double> transformation) {
        if (transformation instanceof Rotation.Primitive) {
            return (Rotation.Primitive) transformation;
        }
        return new Rotation.Primitive(transformation);
    }

    private final MultiplyBoth.Primitive multiplyBoth;
    private final MultiplyLeft.Primitive64 multiplyLeft;
    private final MultiplyNeither.Primitive64 multiplyNeither;
    private final MultiplyRight.Primitive64 multiplyRight;
    private final int myColDim;
    private final int myRowDim;
    private final Array2D<Double> myUtility;
    private transient double[] myWorkerColumn;

    R064Store(final int numbRows, final int numbCols, final double[] dataArray) {

        super(dataArray);

        myRowDim = numbRows;
        myColDim = numbCols;

        myUtility = this.wrapInArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.newPrimitive64(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.newPrimitive64(myRowDim, myColDim);
        multiplyRight = MultiplyRight.newPrimitive64(myRowDim, myColDim);
        multiplyNeither = MultiplyNeither.newPrimitive64(myRowDim, myColDim);
    }

    @Override
    public void accept(final Access2D<?> supplied) {
        for (long j = 0L; j < supplied.countColumns(); j++) {
            for (long i = 0L; i < supplied.countRows(); i++) {
                this.set(i, j, supplied.doubleValue(i, j));
            }
        }
    }

    @Override
    public void add(final long row, final long col, final Comparable<?> addend) {
        myUtility.add(row, col, addend);
    }

    @Override
    public void add(final long row, final long col, final double addend) {
        myUtility.add(row, col, addend);
    }

    @Override
    public void applyCholesky(final int iterationPoint, final BasicArray<Double> multipliers) {

        final double[] tmpData = data;
        final double[] tmpColumn = ((ArrayR064) multipliers).data;

        if (myColDim - iterationPoint - 1 > ApplyCholesky.THRESHOLD) {

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

    @Override
    public void applyLDL(final int iterationPoint, final BasicArray<Double> multipliers) {

        final double[] column = ((ArrayR064) multipliers).data;

        if (myColDim - iterationPoint - 1 > ApplyLDL.THRESHOLD) {

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

    @Override
    public void applyLU(final int iterationPoint, final BasicArray<Double> multipliers) {

        final double[] column = ((ArrayR064) multipliers).data;

        if (myColDim - iterationPoint - 1 > ApplyLU.THRESHOLD) {

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

    @Override
    public Array1D<Double> asList() {
        return myUtility.flatten();
    }

    @Override
    public R064Store build() {
        return this;
    }

    public void caxpy(final double aSclrA, final int aColX, final int aColY, final int aFirstRow) {
        AXPY.invoke(data, aColY * myRowDim + aFirstRow, aSclrA, data, aColX * myRowDim + aFirstRow, 0, myRowDim - aFirstRow);
    }

    @Override
    public Array1D<ComplexNumber> computeInPlaceSchur(final PhysicalStore<Double> transformationCollector, final boolean eigenvalue) {

        // final PrimitiveDenseStore tmpThisCopy = this.copy();
        // final PrimitiveDenseStore tmpCollCopy = (PrimitiveDenseStore)
        // aTransformationCollector.copy();
        //
        // tmpThisCopy.computeInPlaceHessenberg(true);

        // Actual

        final double[] tmpData = data;

        final double[] tmpCollectorData = ((R064Store) transformationCollector).data;

        final double[] tmpVctrWork = new double[this.getMinDim()];
        EvD1D.orthes(tmpData, tmpCollectorData, tmpVctrWork);

        // BasicLogger.logDebug("Schur Step", this);
        // BasicLogger.logDebug("Hessenberg", tmpThisCopy);

        final double[][] tmpDiags = EvD1D.hqr2(tmpData, tmpCollectorData, eigenvalue);
        final double[] aRawReal = tmpDiags[0];
        final double[] aRawImag = tmpDiags[1];
        final int tmpLength = Math.min(aRawReal.length, aRawImag.length);

        final ArrayC128 retVal = ArrayC128.make(tmpLength);
        final ComplexNumber[] tmpRaw = retVal.data;

        for (int i = 0; i < tmpLength; i++) {
            tmpRaw[i] = ComplexNumber.of(aRawReal[i], aRawImag[i]);
        }

        return Array1D.C128.wrap(retVal);
    }

    @Override
    public MatrixStore<Double> conjugate() {
        return this.transpose();
    }

    @Override
    public R064Store copy() {
        return new R064Store(myRowDim, myColDim, this.copyOfData());
    }

    @Override
    public long countColumns() {
        return myColDim;
    }

    @Override
    public long countRows() {
        return myRowDim;
    }

    @Override
    public void divideAndCopyColumn(final int row, final int column, final BasicArray<Double> destination) {

        double[] destinationData = ((ArrayR064) destination).data;

        int index = row + column * myRowDim;
        double denominator = data[index];

        for (int i = row + 1; i < myRowDim; i++) {
            destinationData[i] = data[++index] /= denominator;
        }
    }

    @Override
    public double doubleValue(final int row, final int col) {
        return myUtility.doubleValue(row, col);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || !(obj instanceof R064Store)) {
            return false;
        }
        R064Store other = (R064Store) obj;
        if (myColDim != other.myColDim || myRowDim != other.myRowDim) {
            return false;
        }
        return true;
    }

    @Override
    public void exchangeColumns(final long colA, final long colB) {
        myUtility.exchangeColumns(colA, colB);
    }

    @Override
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

    @Override
    public void exchangeRows(final long rowA, final long rowB) {
        myUtility.exchangeRows(rowA, rowB);
    }

    @Override
    public void fillByMultiplying(final Access1D<Double> left, final Access1D<Double> right) {

        final int complexity = Math.toIntExact(left.count() / this.countRows());
        if (complexity != Math.toIntExact(right.count() / this.countColumns())) {
            ProgrammingError.throwForMultiplicationNotPossible();
        }

        if (left instanceof R064Store) {
            if (right instanceof R064Store) {
                multiplyNeither.invoke(data, R064Store.cast(left).data, complexity, R064Store.cast(right).data);
            } else {
                multiplyRight.invoke(data, R064Store.cast(left).data, complexity, right);
            }
        } else if (right instanceof R064Store) {
            multiplyLeft.invoke(data, left, complexity, R064Store.cast(right).data);
        } else {
            multiplyBoth.invoke(this, left, complexity, right);
        }
    }

    @Override
    public void fillColumn(final long row, final long col, final Access1D<Double> values) {
        myUtility.fillColumn(row, col, values);
    }

    @Override
    public void fillColumn(final long row, final long col, final Double value) {
        myUtility.fillColumn(row, col, value);
    }

    @Override
    public void fillColumn(final long row, final long col, final NullaryFunction<?> supplier) {
        myUtility.fillColumn(row, col, supplier);
    }

    @Override
    public void fillCompatible(final Access2D<Double> left, final BinaryFunction<Double> operator, final Access2D<Double> right) {
        FillCompatible.invoke(data, myRowDim, left, operator, right);
    }

    @Override
    public void fillDiagonal(final long row, final long col, final Double value) {
        myUtility.fillDiagonal(row, col, value);
    }

    @Override
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

    @Override
    public void fillRow(final long row, final long col, final Access1D<Double> values) {
        myUtility.fillRow(row, col, values);
    }

    @Override
    public void fillRow(final long row, final long col, final Double value) {
        myUtility.fillRow(row, col, value);
    }

    @Override
    public void fillRow(final long row, final long col, final NullaryFunction<?> supplier) {
        myUtility.fillRow(row, col, supplier);
    }

    @Override
    public boolean generateApplyAndCopyHouseholderColumn(final int row, final int column, final Householder<Double> destination) {
        return GenerateApplyAndCopyHouseholderColumn.invoke(data, myRowDim, row, column, (Householder.Primitive64) destination);
    }

    @Override
    public boolean generateApplyAndCopyHouseholderRow(final int row, final int column, final Householder<Double> destination) {
        return GenerateApplyAndCopyHouseholderRow.invoke(data, myRowDim, row, column, (Householder.Primitive64) destination);
    }

    @Override
    public MatrixStore<Double> get() {
        return this;
    }

    @Override
    public Double get(final int row, final int col) {
        return myUtility.get(row, col);
    }

    @Override
    public int getColDim() {
        return myColDim;
    }

    @Override
    public int getMaxDim() {
        return Math.max(myRowDim, myColDim);
    }

    @Override
    public int getMinDim() {
        return Math.min(myRowDim, myColDim);
    }

    @Override
    public int getRowDim() {
        return myRowDim;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + myColDim;
        return prime * result + myRowDim;
    }

    @Override
    public void modifyAll(final UnaryFunction<Double> modifier) {

        if (myColDim > ModifyAll.THRESHOLD) {

            final DivideAndConquer conquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int first, final int limit) {
                    R064Store.this.modify(myRowDim * first, myRowDim * limit, 1, modifier);
                }

            };

            conquerer.invoke(0, myColDim, ModifyAll.THRESHOLD);

        } else {

            this.modify(0, myRowDim * myColDim, 1, modifier);
        }
    }

    @Override
    public void modifyColumn(final long row, final long col, final UnaryFunction<Double> modifier) {
        myUtility.modifyColumn(row, col, modifier);
    }

    @Override
    public void modifyDiagonal(final long row, final long col, final UnaryFunction<Double> modifier) {
        myUtility.modifyDiagonal(row, col, modifier);
    }

    @Override
    public void modifyOne(final long row, final long col, final UnaryFunction<Double> modifier) {

        double tmpValue = this.doubleValue(row, col);

        tmpValue = modifier.invoke(tmpValue);

        this.set(row, col, tmpValue);
    }

    @Override
    public void modifyRow(final long row, final long col, final UnaryFunction<Double> modifier) {
        myUtility.modifyRow(row, col, modifier);
    }

    @Override
    public MatrixStore<Double> multiply(final MatrixStore<Double> right) {

        R064Store retVal = FACTORY.make(myRowDim, right.countColumns());

        if (right instanceof R064Store) {
            retVal.multiplyNeither.invoke(retVal.data, data, myColDim, R064Store.cast(right).data);
        } else {
            retVal.multiplyRight.invoke(retVal.data, data, myColDim, right);
        }

        return retVal;
    }

    @Override
    public Double multiplyBoth(final Access1D<Double> leftAndRight) {

        PhysicalStore<Double> tmpStep1 = FACTORY.make(1L, leftAndRight.count());
        PhysicalStore<Double> tmpStep2 = FACTORY.make(1L, 1L);

        tmpStep1.fillByMultiplying(leftAndRight, this);
        tmpStep2.fillByMultiplying(tmpStep1, leftAndRight);

        return tmpStep2.get(0L);
    }

    @Override
    public void negateColumn(final int column) {
        myUtility.modifyColumn(0, column, PrimitiveMath.NEGATE);
    }

    @Override
    public PhysicalStore.Factory<Double, R064Store> physical() {
        return FACTORY;
    }

    @Override
    public TransformableRegion<Double> regionByColumns(final int... columns) {
        return new Subregion2D.ColumnsRegion<>(this, multiplyBoth, columns);
    }

    @Override
    public TransformableRegion<Double> regionByLimits(final int rowLimit, final int columnLimit) {
        return new Subregion2D.LimitRegion<>(this, multiplyBoth, rowLimit, columnLimit);
    }

    @Override
    public TransformableRegion<Double> regionByOffsets(final int rowOffset, final int columnOffset) {
        return new Subregion2D.OffsetRegion<>(this, multiplyBoth, rowOffset, columnOffset);
    }

    @Override
    public TransformableRegion<Double> regionByRows(final int... rows) {
        return new Subregion2D.RowsRegion<>(this, multiplyBoth, rows);
    }

    @Override
    public TransformableRegion<Double> regionByTransposing() {
        return new Subregion2D.TransposedRegion<>(this, multiplyBoth);
    }

    @Override
    public void rotateRight(final int low, final int high, final double cos, final double sin) {
        RotateRight.invoke(data, myRowDim, low, high, cos, sin);
    }

    @Override
    public void set(final int row, final int col, final double value) {
        myUtility.set(row, col, value);
    }

    @Override
    public void set(final long row, final long col, final Comparable<?> value) {
        myUtility.set(row, col, value);
    }

    @Override
    public void setToIdentity(final int col) {
        myUtility.set(col, col, ONE);
        myUtility.fillColumn(col + 1, col, ZERO);
    }

    @Override
    public Array1D<Double> sliceColumn(final long row, final long col) {
        return myUtility.sliceColumn(row, col);
    }

    @Override
    public Array1D<Double> sliceDiagonal(final long row, final long col) {
        return myUtility.sliceDiagonal(row, col);
    }

    @Override
    public Array1D<Double> sliceRange(final long first, final long limit) {
        return myUtility.sliceRange(first, limit);
    }

    @Override
    public Array1D<Double> sliceRow(final long row, final long col) {
        return myUtility.sliceRow(row, col);
    }

    @Override
    public void substituteBackwards(final Access2D<Double> body, final boolean unitDiagonal, final boolean conjugated, final boolean hermitian) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > SubstituteBackwards.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int first, final int limit) {
                    SubstituteBackwards.invoke(R064Store.this.data, tmpRowDim, first, limit, body, unitDiagonal, conjugated, hermitian);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, SubstituteBackwards.THRESHOLD);

        } else {

            SubstituteBackwards.invoke(data, tmpRowDim, 0, tmpColDim, body, unitDiagonal, conjugated, hermitian);
        }
    }

    @Override
    public void substituteForwards(final Access2D<Double> body, final boolean unitDiagonal, final boolean conjugated, final boolean identity) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > SubstituteForwards.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int first, final int limit) {
                    SubstituteForwards.invoke(R064Store.this.data, tmpRowDim, first, limit, body, unitDiagonal, conjugated, identity);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, SubstituteForwards.THRESHOLD);

        } else {

            SubstituteForwards.invoke(data, tmpRowDim, 0, tmpColDim, body, unitDiagonal, conjugated, identity);
        }
    }

    @Override
    public PrimitiveScalar toScalar(final int row, final int col) {
        return PrimitiveScalar.of(this.doubleValue(row, col));
    }

    @Override
    public String toString() {
        return Access2D.toString(this);
    }

    @Override
    public void transformLeft(final Householder<Double> transformation, final int firstColumn) {
        HouseholderLeft.call(data, myRowDim, firstColumn, R064Store.cast(transformation));
    }

    @Override
    public void transformLeft(final Rotation<Double> transformation) {

        final Rotation.Primitive tmpTransf = R064Store.cast(transformation);

        final int tmpLow = tmpTransf.low;
        final int tmpHigh = tmpTransf.high;

        if (tmpLow != tmpHigh) {
            if (!Double.isNaN(tmpTransf.cos) && !Double.isNaN(tmpTransf.sin)) {
                RotateLeft.invoke(data, myRowDim, tmpLow, tmpHigh, tmpTransf.cos, tmpTransf.sin);
            } else {
                myUtility.exchangeRows(tmpLow, tmpHigh);
            }
        } else if (!Double.isNaN(tmpTransf.cos)) {
            myUtility.modifyRow(tmpLow, 0L, PrimitiveMath.MULTIPLY.second(tmpTransf.cos));
        } else if (!Double.isNaN(tmpTransf.sin)) {
            myUtility.modifyRow(tmpLow, 0L, PrimitiveMath.DIVIDE.second(tmpTransf.sin));
        } else {
            myUtility.modifyRow(tmpLow, 0, PrimitiveMath.NEGATE);
        }
    }

    @Override
    public void transformRight(final Householder<Double> transformation, final int firstRow) {
        HouseholderRight.call(data, myRowDim, firstRow, R064Store.cast(transformation), this.getWorkerColumn());
    }

    @Override
    public void transformRight(final Rotation<Double> transformation) {

        final Rotation.Primitive tmpTransf = R064Store.cast(transformation);

        final int tmpLow = tmpTransf.low;
        final int tmpHigh = tmpTransf.high;

        if (tmpLow != tmpHigh) {
            if (!Double.isNaN(tmpTransf.cos) && !Double.isNaN(tmpTransf.sin)) {
                RotateRight.invoke(data, myRowDim, tmpLow, tmpHigh, tmpTransf.cos, tmpTransf.sin);
            } else {
                myUtility.exchangeColumns(tmpLow, tmpHigh);
            }
        } else if (!Double.isNaN(tmpTransf.cos)) {
            myUtility.modifyColumn(0L, tmpHigh, PrimitiveMath.MULTIPLY.second(tmpTransf.cos));
        } else if (!Double.isNaN(tmpTransf.sin)) {
            myUtility.modifyColumn(0L, tmpHigh, PrimitiveMath.DIVIDE.second(tmpTransf.sin));
        } else {
            myUtility.modifyColumn(0, tmpHigh, PrimitiveMath.NEGATE);
        }
    }

    @Override
    public void transformSymmetric(final Householder<Double> transformation) {
        HouseholderHermitian.invoke(data, R064Store.cast(transformation), this.getWorkerColumn());
    }

    @Override
    public void tred2(final BasicArray<Double> mainDiagonal, final BasicArray<Double> offDiagonal, final boolean yesvecs) {
        HouseholderHermitian.tred2j(data, ((ArrayR064) mainDiagonal).data, ((ArrayR064) offDiagonal).data, yesvecs);
    }

    @Override
    public void visitColumn(final long row, final long col, final VoidFunction<Double> visitor) {
        myUtility.visitColumn(row, col, visitor);
    }

    @Override
    public void visitDiagonal(final long row, final long col, final VoidFunction<Double> visitor) {
        myUtility.visitDiagonal(row, col, visitor);
    }

    @Override
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

}
