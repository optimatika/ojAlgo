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

import java.util.Arrays;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.Array2D;
import org.ojalgo.array.ArrayR032;
import org.ojalgo.array.PrimitiveArray;
import org.ojalgo.array.operation.FillCompatible;
import org.ojalgo.array.operation.FillMatchingSingle;
import org.ojalgo.array.operation.RotateLeft;
import org.ojalgo.array.operation.RotateRight;
import org.ojalgo.array.operation.SubstituteBackwards;
import org.ojalgo.array.operation.SubstituteForwards;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.operation.HouseholderLeft;
import org.ojalgo.matrix.operation.HouseholderRight;
import org.ojalgo.matrix.operation.MultiplyBoth;
import org.ojalgo.matrix.operation.MultiplyLeft;
import org.ojalgo.matrix.operation.MultiplyNeither;
import org.ojalgo.matrix.operation.MultiplyRight;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.HouseholderReference;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.ElementView2D;
import org.ojalgo.structure.Factory2D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.structure.Transformation2D;
import org.ojalgo.type.math.MathType;

/**
 * A {@linkplain float} implementation of {@linkplain PhysicalStore}.
 *
 * @author apete
 */
public final class R032Store extends ArrayR032 implements PhysicalStore<Double>, Factory2D.Builder<R032Store> {

    public static final PhysicalStore.Factory<Double, R032Store> FACTORY = new PrimitiveFactory<>() {

        @Override
        public PrimitiveArray.Factory array() {
            return ArrayR032.FACTORY;
        }

        @Override
        public R032Store copy(final Access2D<?> source) {

            int tmpRowDim = (int) source.countRows();
            int tmpColDim = (int) source.countColumns();

            final R032Store retVal = this.make(source);

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

        @Override
        public MathType getMathType() {
            return MathType.R032;
        }

        @Override
        public R032Store make(final int rows, final int columns) {
            return new R032Store(rows, columns, new float[rows * columns]);
        }

        @Override
        public Householder<Double> makeHouseholder(final int length) {
            return new Householder.Primitive32(length);
        }

        @Override
        public R032Store transpose(final Access2D<?> source) {

            R032Store retVal = this.make(source.getColDim(), source.getRowDim());

            int nbRows = retVal.getRowDim();
            int nbCols = retVal.getColDim();

            if (nbCols > FillMatchingSingle.THRESHOLD) {

                final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                    @Override
                    public void conquer(final int first, final int limit) {
                        FillMatchingSingle.transpose(retVal.data, nbRows, first, limit, source);
                    }

                };

                tmpConquerer.invoke(0, nbCols, FillMatchingSingle.THRESHOLD);

            } else {

                FillMatchingSingle.transpose(retVal.data, nbRows, 0, nbCols, source);
            }

            return retVal;
        }

    };

    static R032Store cast(final Access1D<?> matrix) {
        if (matrix instanceof R032Store) {
            return (R032Store) matrix;
        } else if (matrix instanceof Access2D<?>) {
            return FACTORY.copy((Access2D<?>) matrix);
        } else {
            return FACTORY.column(matrix);
        }
    }

    static Householder.Primitive32 cast(final Householder<Double> transformation) {
        if (transformation instanceof Householder.Primitive32) {
            return (Householder.Primitive32) transformation;
        }
        if (transformation instanceof HouseholderReference<?>) {
            return ((Householder.Primitive32) ((HouseholderReference<Double>) transformation).getWorker(FACTORY)).copy(transformation);
        }
        return new Householder.Primitive32(transformation);
    }

    static Rotation.Primitive cast(final Rotation<Double> transformation) {
        if (transformation instanceof Rotation.Primitive) {
            return (Rotation.Primitive) transformation;
        }
        return new Rotation.Primitive(transformation);
    }

    private final MultiplyBoth.Primitive multiplyBoth;
    private final MultiplyLeft.Primitive32 multiplyLeft;
    private final MultiplyNeither.Primitive32 multiplyNeither;
    private final MultiplyRight.Primitive32 multiplyRight;
    private final int myColDim;
    private final int myRowDim;
    private final Array2D<Double> myUtility;

    private transient float[] myWorkerColumn;

    R032Store(final int numbRows, final int numbCols, final float[] dataArray) {

        super(dataArray);

        myRowDim = numbRows;
        myColDim = numbCols;

        myUtility = this.wrapInArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.newPrimitive32(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.newPrimitive32(myRowDim, myColDim);
        multiplyRight = MultiplyRight.newPrimitive32(myRowDim, myColDim);
        multiplyNeither = MultiplyNeither.newPrimitive32(myRowDim, myColDim);
    }

    @Override
    public void accept(final Access2D<?> supplied) {
        myUtility.accept(supplied);
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
    public Double aggregateColumn(final long col, final Aggregator aggregator) {
        return myUtility.aggregateColumn(col, aggregator);
    }

    @Override
    public Double aggregateColumn(final long row, final long col, final Aggregator aggregator) {
        return myUtility.aggregateColumn(row, col, aggregator);
    }

    @Override
    public Double aggregateDiagonal(final Aggregator aggregator) {
        return myUtility.aggregateDiagonal(aggregator);
    }

    @Override
    public Double aggregateDiagonal(final long row, final long col, final Aggregator aggregator) {
        return myUtility.aggregateDiagonal(row, col, aggregator);
    }

    @Override
    public Double aggregateRange(final long first, final long limit, final Aggregator aggregator) {
        return myUtility.aggregateRange(first, limit, aggregator);
    }

    @Override
    public Double aggregateRow(final long row, final Aggregator aggregator) {
        return myUtility.aggregateRow(row, aggregator);
    }

    @Override
    public Double aggregateRow(final long row, final long col, final Aggregator aggregator) {
        return myUtility.aggregateRow(row, col, aggregator);
    }

    @Override
    public <NN extends Comparable<NN>, R extends Mutate2D.Receiver<NN>> Access2D.Collectable<NN, R> asCollectable2D() {
        return myUtility.asCollectable2D();
    }

    @Override
    public Array1D<Double> asList() {
        return myUtility.flatten();
    }

    @Override
    public R032Store build() {
        return this;
    }

    @Override
    public byte byteValue(final long row, final long col) {
        return myUtility.byteValue(row, col);
    }

    @Override
    public ColumnView<Double> columns() {
        return myUtility.columns();
    }

    @Override
    public MatrixStore<Double> conjugate() {
        return this.transpose();
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
    public double dot(final Access1D<?> vector) {
        return myUtility.dot(vector);
    }

    @Override
    public double doubleValue(final int row, final int col) {
        return myUtility.doubleValue(row, col);
    }

    @Override
    public ElementView2D<Double, ?> elements() {
        return myUtility.elements();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || !(obj instanceof R032Store)) {
            return false;
        }
        R032Store other = (R032Store) obj;
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
    public void exchangeRows(final long rowA, final long rowB) {
        myUtility.exchangeRows(rowA, rowB);
    }

    @Override
    public void fillByMultiplying(final Access1D<Double> left, final Access1D<Double> right) {

        int complexity = Math.toIntExact(left.count() / this.countRows());
        if (complexity != Math.toIntExact(right.count() / this.countColumns())) {
            ProgrammingError.throwForMultiplicationNotPossible();
        }

        if (left instanceof R032Store) {
            if (right instanceof R032Store) {
                multiplyNeither.invoke(data, R032Store.cast(left).data, complexity, R032Store.cast(right).data);
            } else {
                multiplyRight.invoke(data, R032Store.cast(left).data, complexity, right);
            }
        } else if (right instanceof R032Store) {
            multiplyLeft.invoke(data, left, complexity, R032Store.cast(right).data);
        } else {
            multiplyBoth.invoke(this, left, complexity, right);
        }
    }

    @Override
    public void fillColumn(final long col, final Access1D<Double> values) {
        myUtility.fillColumn(col, values);
    }

    @Override
    public void fillColumn(final long col, final Double value) {
        myUtility.fillColumn(col, value);
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
    public void fillColumn(final long col, final NullaryFunction<?> supplier) {
        myUtility.fillColumn(col, supplier);
    }

    @Override
    public void fillCompatible(final Access2D<Double> left, final BinaryFunction<Double> operator, final Access2D<Double> right) {
        FillCompatible.invoke(data, myRowDim, left, operator, right);
    }

    @Override
    public void fillDiagonal(final Access1D<Double> values) {
        myUtility.fillDiagonal(values);
    }

    @Override
    public void fillDiagonal(final Double value) {
        myUtility.fillDiagonal(value);
    }

    @Override
    public void fillDiagonal(final long row, final long col, final Access1D<Double> values) {
        myUtility.fillDiagonal(row, col, values);
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
    public void fillDiagonal(final NullaryFunction<?> supplier) {
        myUtility.fillDiagonal(supplier);
    }

    @Override
    public void fillMatching(final Access1D<Double> left, final BinaryFunction<Double> function, final Access1D<Double> right) {
        myUtility.fillMatching(left, function, right);
    }

    @Override
    public void fillMatching(final UnaryFunction<Double> function, final Access1D<Double> arguments) {
        myUtility.fillMatching(function, arguments);
    }

    @Override
    public void fillRow(final long row, final Access1D<Double> values) {
        myUtility.fillRow(row, values);
    }

    @Override
    public void fillRow(final long row, final Double value) {
        myUtility.fillRow(row, value);
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
    public void fillRow(final long row, final NullaryFunction<?> supplier) {
        myUtility.fillRow(row, supplier);
    }

    @Override
    public float floatValue(final long row, final long col) {
        return myUtility.floatValue(row, col);
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
    public int intValue(final long row, final long col) {
        return myUtility.intValue(row, col);
    }

    @Override
    public boolean isAcceptable(final Structure2D supplier) {
        return myUtility.isAcceptable(supplier);
    }

    @Override
    public boolean isEmpty() {
        return myUtility.isEmpty();
    }

    @Override
    public boolean isFat() {
        return myUtility.isFat();
    }

    @Override
    public boolean isScalar() {
        return myUtility.isScalar();
    }

    @Override
    public boolean isSquare() {
        return myUtility.isSquare();
    }

    @Override
    public boolean isTall() {
        return myUtility.isTall();
    }

    @Override
    public boolean isVector() {
        return myUtility.isVector();
    }

    @Override
    public long longValue(final long row, final long col) {
        return myUtility.longValue(row, col);
    }

    @Override
    public void modifyAll(final UnaryFunction<Double> modifier) {

        this.modify(0, myRowDim * myColDim, 1, modifier);

        // if (myColDim > ModifyAll.THRESHOLD) {
        //
        // final DivideAndConquer conquerer = new DivideAndConquer() {
        //
        // @Override
        // public void conquer(final int first, final int limit) {
        // Primitive32Store.this.modify(myRowDim * first, myRowDim * limit, 1, modifier);
        // }
        //
        // };
        //
        // conquerer.invoke(0, myColDim, ModifyAll.THRESHOLD);
        //
        // } else {
        //
        // this.modify(0, myRowDim * myColDim, 1, modifier);
        // }
    }

    @Override
    public void modifyAny(final Transformation2D<Double> modifier) {
        myUtility.modifyAny(modifier);
    }

    @Override
    public void modifyColumn(final long row, final long col, final UnaryFunction<Double> modifier) {
        myUtility.modifyColumn(row, col, modifier);
    }

    @Override
    public void modifyColumn(final long col, final UnaryFunction<Double> modifier) {
        myUtility.modifyColumn(col, modifier);
    }

    @Override
    public void modifyDiagonal(final long row, final long col, final UnaryFunction<Double> modifier) {
        myUtility.modifyDiagonal(row, col, modifier);
    }

    @Override
    public void modifyDiagonal(final UnaryFunction<Double> modifier) {
        myUtility.modifyDiagonal(modifier);
    }

    @Override
    public void modifyMatching(final Access1D<Double> left, final BinaryFunction<Double> function) {
        myUtility.modifyMatching(left, function);
    }

    @Override
    public void modifyMatching(final BinaryFunction<Double> function, final Access1D<Double> right) {
        myUtility.modifyMatching(function, right);
    }

    @Override
    public void modifyMatchingInColumns(final Access1D<Double> left, final BinaryFunction<Double> function) {
        myUtility.modifyMatchingInColumns(left, function);
    }

    @Override
    public void modifyMatchingInColumns(final BinaryFunction<Double> function, final Access1D<Double> right) {
        myUtility.modifyMatchingInColumns(function, right);
    }

    @Override
    public void modifyMatchingInRows(final Access1D<Double> left, final BinaryFunction<Double> function) {
        myUtility.modifyMatchingInRows(left, function);
    }

    @Override
    public void modifyMatchingInRows(final BinaryFunction<Double> function, final Access1D<Double> right) {
        myUtility.modifyMatchingInRows(function, right);
    }

    @Override
    public void modifyOne(final long row, final long col, final UnaryFunction<Double> modifier) {
        myUtility.modifyOne(row, col, modifier);
    }

    @Override
    public void modifyRow(final long row, final long col, final UnaryFunction<Double> modifier) {
        myUtility.modifyRow(row, col, modifier);
    }

    @Override
    public void modifyRow(final long row, final UnaryFunction<Double> modifier) {
        myUtility.modifyRow(row, modifier);
    }

    @Override
    public MatrixStore<Double> multiply(final MatrixStore<Double> right) {

        R032Store retVal = FACTORY.make(myRowDim, right.countColumns());

        if (right instanceof R032Store) {
            retVal.multiplyNeither.invoke(retVal.data, data, myColDim, R032Store.cast(right).data);
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
    public PhysicalStore.Factory<Double, ?> physical() {
        return FACTORY;
    }

    @Override
    public void reduceColumns(final Aggregator aggregator, final Mutate1D receiver) {
        myUtility.reduceColumns(aggregator, receiver);
    }

    @Override
    public void reduceRows(final Aggregator aggregator, final Mutate1D receiver) {
        myUtility.reduceRows(aggregator, receiver);
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
    public RowView<Double> rows() {
        return myUtility.rows();
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
    public short shortValue(final long row, final long col) {
        return myUtility.shortValue(row, col);
    }

    @Override
    public Array1D<Double> sliceColumn(final long col) {
        return myUtility.sliceColumn(col);
    }

    @Override
    public Array1D<Double> sliceColumn(final long row, final long col) {
        return myUtility.sliceColumn(row, col);
    }

    @Override
    public Access1D<Double> sliceDiagonal() {
        return myUtility.sliceDiagonal();
    }

    @Override
    public Array1D<Double> sliceDiagonal(final long row, final long col) {
        return myUtility.sliceDiagonal(row, col);
    }

    @Override
    public Array1D<Double> sliceRow(final long row) {
        return myUtility.sliceRow(row);
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
                    SubstituteBackwards.invoke(R032Store.this.data, tmpRowDim, first, limit, body, unitDiagonal, conjugated, hermitian);
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
                    SubstituteForwards.invoke(R032Store.this.data, tmpRowDim, first, limit, body, unitDiagonal, conjugated, identity);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, SubstituteForwards.THRESHOLD);

        } else {

            SubstituteForwards.invoke(data, tmpRowDim, 0, tmpColDim, body, unitDiagonal, conjugated, identity);
        }
    }

    @Override
    public double[] toRawCopy1D() {
        return myUtility.toRawCopy1D();
    }

    @Override
    public double[][] toRawCopy2D() {
        return myUtility.toRawCopy2D();
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
        HouseholderLeft.call(data, myRowDim, firstColumn, R032Store.cast(transformation));
    }

    @Override
    public void transformLeft(final Rotation<Double> transformation) {

        final Rotation.Primitive tmpTransf = R064Store.cast(transformation);

        final int tmpLow = tmpTransf.low;
        final int tmpHigh = tmpTransf.high;

        if (tmpLow != tmpHigh) {
            if (!Double.isNaN(tmpTransf.cos) && !Double.isNaN(tmpTransf.sin)) {
                RotateLeft.invoke(data, myRowDim, tmpLow, tmpHigh, (float) tmpTransf.cos, (float) tmpTransf.sin);
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
        HouseholderRight.call(data, myRowDim, firstRow, R032Store.cast(transformation), this.getWorkerColumn());
    }

    @Override
    public void transformRight(final Rotation<Double> transformation) {

        final Rotation.Primitive tmpTransf = R064Store.cast(transformation);

        final int tmpLow = tmpTransf.low;
        final int tmpHigh = tmpTransf.high;

        if (tmpLow != tmpHigh) {
            if (!Double.isNaN(tmpTransf.cos) && !Double.isNaN(tmpTransf.sin)) {
                RotateRight.invoke(data, myRowDim, tmpLow, tmpHigh, (float) tmpTransf.cos, (float) tmpTransf.sin);
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
    public void visitColumn(final long row, final long col, final VoidFunction<Double> visitor) {
        myUtility.visitColumn(row, col, visitor);
    }

    @Override
    public void visitColumn(final long col, final VoidFunction<Double> visitor) {
        myUtility.visitColumn(col, visitor);
    }

    @Override
    public void visitDiagonal(final long row, final long col, final VoidFunction<Double> visitor) {
        myUtility.visitDiagonal(row, col, visitor);
    }

    @Override
    public void visitDiagonal(final VoidFunction<Double> visitor) {
        myUtility.visitDiagonal(visitor);
    }

    @Override
    public void visitOne(final long row, final long col, final VoidFunction<Double> visitor) {
        myUtility.visitOne(row, col, visitor);
    }

    @Override
    public void visitRow(final long row, final long col, final VoidFunction<Double> visitor) {
        myUtility.visitRow(row, col, visitor);
    }

    @Override
    public void visitRow(final long row, final VoidFunction<Double> visitor) {
        myUtility.visitRow(row, visitor);
    }

    private float[] getWorkerColumn() {
        if (myWorkerColumn != null) {
            Arrays.fill(myWorkerColumn, 0F);
        } else {
            myWorkerColumn = new float[myRowDim];
        }
        return myWorkerColumn;
    }

}
