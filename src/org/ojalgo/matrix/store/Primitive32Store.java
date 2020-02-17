/*
 * Copyright 1997-2020 Optimatika
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
import org.ojalgo.array.DenseArray;
import org.ojalgo.array.Primitive32Array;
import org.ojalgo.array.operation.*;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.HouseholderReference;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.structure.*;
import org.ojalgo.type.NumberDefinition;

/**
 * A {@linkplain float} implementation of {@linkplain PhysicalStore}.
 *
 * @author apete
 */
public final class Primitive32Store extends Primitive32Array implements PhysicalStore<Double> {

    public static final PhysicalStore.Factory<Double, Primitive32Store> FACTORY = new PrimitiveFactory<Primitive32Store>() {

        @Override
        public DenseArray.Factory<Double> array() {
            return Primitive32Array.FACTORY;
        }

        @Override
        public MatrixStore.Factory<Double> builder() {
            return MatrixStore.PRIMITIVE32;
        }

        public Primitive32Store columns(final Access1D<?>... source) {

            final int tmpRowDim = (int) source[0].count();
            final int tmpColDim = source.length;

            final float[] tmpData = new float[tmpRowDim * tmpColDim];

            Access1D<?> tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = tmpColumn.floatValue(i);
                }
            }

            return new Primitive32Store(tmpRowDim, tmpColDim, tmpData);
        }

        public Primitive32Store columns(final Comparable<?>[]... source) {

            final int tmpRowDim = source[0].length;
            final int tmpColDim = source.length;

            final float[] tmpData = new float[tmpRowDim * tmpColDim];

            Comparable<?>[] tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = NumberDefinition.floatValue(tmpColumn[i]);
                }
            }

            return new Primitive32Store(tmpRowDim, tmpColDim, tmpData);
        }

        public Primitive32Store columns(final double[]... source) {

            final int tmpRowDim = source[0].length;
            final int tmpColDim = source.length;

            final float[] tmpData = new float[tmpRowDim * tmpColDim];

            double[] tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = (float) tmpColumn[i];
                }
            }

            return new Primitive32Store(tmpRowDim, tmpColDim, tmpData);
        }

        public Primitive32Store columns(final List<? extends Comparable<?>>... source) {

            final int tmpRowDim = source[0].size();
            final int tmpColDim = source.length;

            final float[] tmpData = new float[tmpRowDim * tmpColDim];

            List<? extends Comparable<?>> tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = NumberDefinition.floatValue(tmpColumn.get(i));
                }
            }

            return new Primitive32Store(tmpRowDim, tmpColDim, tmpData);
        }

        public Primitive32Store copy(final Access2D<?> source) {

            final int tmpRowDim = (int) source.countRows();
            final int tmpColDim = (int) source.countColumns();

            final Primitive32Store retVal = new Primitive32Store(tmpRowDim, tmpColDim);

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

        public Primitive32Store make(final long rows, final long columns) {
            return new Primitive32Store((int) rows, (int) columns);
        }

        public Primitive32Store makeEye(final long rows, final long columns) {

            final Primitive32Store retVal = this.make(rows, columns);

            retVal.fillDiagonal(ONE);

            return retVal;
        }

        @Override
        public Householder<Double> makeHouseholder(final int length) {
            return new Householder.Primitive32(length);
        }

        public Primitive32Store rows(final Access1D<?>... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = (int) source[0].count();

            final float[] tmpData = new float[tmpRowDim * tmpColDim];

            Access1D<?> tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = tmpRow.floatValue(j);
                }
            }

            return new Primitive32Store(tmpRowDim, tmpColDim, tmpData);
        }

        public Primitive32Store rows(final Comparable<?>[]... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].length;

            final float[] tmpData = new float[tmpRowDim * tmpColDim];

            Comparable<?>[] tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = NumberDefinition.floatValue(tmpRow[j]);
                }
            }

            return new Primitive32Store(tmpRowDim, tmpColDim, tmpData);
        }

        public Primitive32Store rows(final double[]... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].length;

            final float[] tmpData = new float[tmpRowDim * tmpColDim];

            double[] tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = (float) tmpRow[j];
                }
            }

            return new Primitive32Store(tmpRowDim, tmpColDim, tmpData);
        }

        public Primitive32Store rows(final List<? extends Comparable<?>>... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].size();

            final float[] tmpData = new float[tmpRowDim * tmpColDim];

            List<? extends Comparable<?>> tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = NumberDefinition.floatValue(tmpRow.get(j));
                }
            }

            return new Primitive32Store(tmpRowDim, tmpColDim, tmpData);
        }

        public Primitive32Store transpose(final Access2D<?> source) {

            final Primitive32Store retVal = new Primitive32Store((int) source.countColumns(), (int) source.countRows());

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

    static Primitive32Store cast(final Access1D<Double> matrix) {
        if (matrix instanceof Primitive32Store) {
            return (Primitive32Store) matrix;
        } else if (matrix instanceof Access2D<?>) {
            return FACTORY.copy((Access2D<?>) matrix);
        } else {
            return FACTORY.columns(matrix);
        }
    }

    static Householder.Primitive32 cast(final Householder<Double> transformation) {
        if (transformation instanceof Householder.Primitive32) {
            return (Householder.Primitive32) transformation;
        } else if (transformation instanceof HouseholderReference<?>) {
            return ((Householder.Primitive32) ((HouseholderReference<Double>) transformation).getWorker(FACTORY)).copy(transformation);
        } else {
            return new Householder.Primitive32(transformation);
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
    private final MultiplyLeft.Primitive32 multiplyLeft;
    private final MultiplyNeither.Primitive32 multiplyNeither;
    private final MultiplyRight.Primitive32 multiplyRight;
    private final int myColDim;
    private final int myRowDim;
    private final Array2D<Double> myUtility;

    private transient float[] myWorkerColumn;

    Primitive32Store(final int numbRows, final int numbCols) {

        super(numbRows * numbCols);

        myRowDim = numbRows;
        myColDim = numbCols;

        myUtility = this.wrapInArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.newPrimitive32(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.newPrimitive32(myRowDim, myColDim);
        multiplyRight = MultiplyRight.newPrimitive32(myRowDim, myColDim);
        multiplyNeither = MultiplyNeither.newPrimitive32(myRowDim, myColDim);
    }

    Primitive32Store(final int numbRows, final int numbCols, final float[] dataArray) {

        super(dataArray);

        myRowDim = numbRows;
        myColDim = numbCols;

        myUtility = this.wrapInArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.newPrimitive32(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.newPrimitive32(myRowDim, myColDim);
        multiplyRight = MultiplyRight.newPrimitive32(myRowDim, myColDim);
        multiplyNeither = MultiplyNeither.newPrimitive32(myRowDim, myColDim);
    }

    public void accept(final Access2D<?> supplied) {
        myUtility.accept(supplied);
    }

    public void add(final long row, final long col, final Comparable<?> addend) {
        myUtility.add(row, col, addend);
    }

    public void add(final long row, final long col, final double addend) {
        myUtility.add(row, col, addend);
    }

    public Double aggregateColumn(final long col, final Aggregator aggregator) {
        return myUtility.aggregateColumn(col, aggregator);
    }

    public Double aggregateColumn(final long row, final long col, final Aggregator aggregator) {
        return myUtility.aggregateColumn(row, col, aggregator);
    }

    public Double aggregateDiagonal(final Aggregator aggregator) {
        return myUtility.aggregateDiagonal(aggregator);
    }

    public Double aggregateDiagonal(final long row, final long col, final Aggregator aggregator) {
        return myUtility.aggregateDiagonal(row, col, aggregator);
    }

    public Double aggregateRange(final long first, final long limit, final Aggregator aggregator) {
        return myUtility.aggregateRange(first, limit, aggregator);
    }

    public Double aggregateRow(final long row, final Aggregator aggregator) {
        return myUtility.aggregateRow(row, aggregator);
    }

    public Double aggregateRow(final long row, final long col, final Aggregator aggregator) {
        return myUtility.aggregateRow(row, col, aggregator);
    }

    public <NN extends Comparable<NN>, R extends Mutate2D.Receiver<NN>> Access2D.Collectable<NN, R> asCollectable2D() {
        return myUtility.asCollectable2D();
    }

    public Array1D<Double> asList() {
        return myUtility.asArray1D();
    }

    public byte byteValue(final long row, final long col) {
        return myUtility.byteValue(row, col);
    }

    public ColumnView<Double> columns() {
        return myUtility.columns();
    }

    public MatrixStore<Double> conjugate() {
        return this.transpose();
    }

    public long countColumns() {
        return myColDim;
    }

    public long countRows() {
        return myRowDim;
    }

    @Override
    public double dot(final Access1D<?> vector) {
        return myUtility.dot(vector);
    }

    public double doubleValue(final long row, final long col) {
        return myUtility.doubleValue(row, col);
    }

    public ElementView2D<Double, ?> elements() {
        return myUtility.elements();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof Primitive32Store)) {
            return false;
        }
        Primitive32Store other = (Primitive32Store) obj;
        if (myColDim != other.myColDim) {
            return false;
        }
        if (myRowDim != other.myRowDim) {
            return false;
        }
        return true;
    }

    public void exchangeColumns(final long colA, final long colB) {
        myUtility.exchangeColumns(colA, colB);
    }

    public void exchangeRows(final long rowA, final long rowB) {
        myUtility.exchangeRows(rowA, rowB);
    }

    public void fillByMultiplying(final Access1D<Double> left, final Access1D<Double> right) {

        final int complexity = Math.toIntExact(left.count() / this.countRows());
        if (complexity != Math.toIntExact(right.count() / this.countColumns())) {
            ProgrammingError.throwForMultiplicationNotPossible();
        }

        if (left instanceof Primitive32Store) {
            if (right instanceof Primitive32Store) {
                multiplyNeither.invoke(data, Primitive32Store.cast(left).data, complexity, Primitive32Store.cast(right).data);
            } else {
                multiplyRight.invoke(data, Primitive32Store.cast(left).data, complexity, right);
            }
        } else {
            if (right instanceof Primitive32Store) {
                multiplyLeft.invoke(data, left, complexity, Primitive32Store.cast(right).data);
            } else {
                multiplyBoth.invoke(this, left, complexity, right);
            }
        }
    }

    public void fillColumn(final long col, final Access1D<Double> values) {
        myUtility.fillColumn(col, values);
    }

    public void fillColumn(final long col, final Double value) {
        myUtility.fillColumn(col, value);
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

    public void fillColumn(final long col, final NullaryFunction<?> supplier) {
        myUtility.fillColumn(col, supplier);
    }

    public void fillDiagonal(final Access1D<Double> values) {
        myUtility.fillDiagonal(values);
    }

    public void fillDiagonal(final Double value) {
        myUtility.fillDiagonal(value);
    }

    public void fillDiagonal(final long row, final long col, final Access1D<Double> values) {
        myUtility.fillDiagonal(row, col, values);
    }

    public void fillDiagonal(final long row, final long col, final Double value) {
        myUtility.fillDiagonal(row, col, value);
    }

    public void fillDiagonal(final long row, final long col, final NullaryFunction<?> supplier) {
        myUtility.fillDiagonal(row, col, supplier);
    }

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

    public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
        myUtility.fillOne(row, col, values, valueIndex);
    }

    public void fillOne(final long row, final long col, final Double value) {
        myUtility.fillOne(row, col, value);
    }

    public void fillOne(final long row, final long col, final NullaryFunction<?> supplier) {
        myUtility.fillOne(row, col, supplier);
    }

    public void fillRow(final long row, final Access1D<Double> values) {
        myUtility.fillRow(row, values);
    }

    public void fillRow(final long row, final Double value) {
        myUtility.fillRow(row, value);
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

    public void fillRow(final long row, final NullaryFunction<?> supplier) {
        myUtility.fillRow(row, supplier);
    }

    public float floatValue(final long row, final long col) {
        return myUtility.floatValue(row, col);
    }

    public Double get(final long row, final long col) {
        return myUtility.get(row, col);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = (prime * result) + myColDim;
        result = (prime * result) + myRowDim;
        return result;
    }

    public long indexOfLargestInColumn(final long col) {
        return myUtility.indexOfLargestInColumn(col);
    }

    public long indexOfLargestInColumn(final long row, final long col) {
        return myUtility.indexOfLargestInColumn(row, col);
    }

    public long indexOfLargestInRow(final long row) {
        return myUtility.indexOfLargestInRow(row);
    }

    public long indexOfLargestInRow(final long row, final long col) {
        return myUtility.indexOfLargestInRow(row, col);
    }

    public long indexOfLargestOnDiagonal() {
        return myUtility.indexOfLargestOnDiagonal();
    }

    public long indexOfLargestOnDiagonal(final long first) {
        return myUtility.indexOfLargestOnDiagonal(first);
    }

    public int intValue(final long row, final long col) {
        return myUtility.intValue(row, col);
    }

    public boolean isAbsolute(final long row, final long col) {
        return myUtility.isAbsolute(row, col);
    }

    public boolean isAcceptable(final Structure2D supplier) {
        return myUtility.isAcceptable(supplier);
    }

    public boolean isAllSmall(final double comparedTo) {
        return myUtility.isAllSmall(comparedTo);
    }

    public boolean isColumnSmall(final long col, final double comparedTo) {
        return myUtility.isColumnSmall(col, comparedTo);
    }

    public boolean isColumnSmall(final long row, final long col, final double comparedTo) {
        return myUtility.isColumnSmall(row, col, comparedTo);
    }

    public boolean isEmpty() {
        return myUtility.isEmpty();
    }

    public boolean isFat() {
        return myUtility.isFat();
    }

    public boolean isRowSmall(final long row, final double comparedTo) {
        return myUtility.isRowSmall(row, comparedTo);
    }

    public boolean isRowSmall(final long row, final long col, final double comparedTo) {
        return myUtility.isRowSmall(row, col, comparedTo);
    }

    public boolean isScalar() {
        return myUtility.isScalar();
    }

    public boolean isSmall(final long row, final long col, final double comparedTo) {
        return myUtility.isSmall(row, col, comparedTo);
    }

    public boolean isSquare() {
        return myUtility.isSquare();
    }

    public boolean isTall() {
        return myUtility.isTall();
    }

    public boolean isVector() {
        return myUtility.isVector();
    }

    public long longValue(final long row, final long col) {
        return myUtility.longValue(row, col);
    }

    @Override
    public void modifyAll(final UnaryFunction<Double> modifier) {
        myUtility.modifyAll(modifier);
    }

    public void modifyAny(final Transformation2D<Double> modifier) {
        myUtility.modifyAny(modifier);
    }

    public void modifyColumn(final long row, final long col, final UnaryFunction<Double> modifier) {
        myUtility.modifyColumn(row, col, modifier);
    }

    public void modifyColumn(final long col, final UnaryFunction<Double> modifier) {
        myUtility.modifyColumn(col, modifier);
    }

    public void modifyDiagonal(final long row, final long col, final UnaryFunction<Double> modifier) {
        myUtility.modifyDiagonal(row, col, modifier);
    }

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

    public void modifyMatchingInColumns(final Access1D<Double> left, final BinaryFunction<Double> function) {
        myUtility.modifyMatchingInColumns(left, function);
    }

    public void modifyMatchingInColumns(final BinaryFunction<Double> function, final Access1D<Double> right) {
        myUtility.modifyMatchingInColumns(function, right);
    }

    public void modifyMatchingInRows(final Access1D<Double> left, final BinaryFunction<Double> function) {
        myUtility.modifyMatchingInRows(left, function);
    }

    public void modifyMatchingInRows(final BinaryFunction<Double> function, final Access1D<Double> right) {
        myUtility.modifyMatchingInRows(function, right);
    }

    public void modifyOne(final long row, final long col, final UnaryFunction<Double> modifier) {
        myUtility.modifyOne(row, col, modifier);
    }

    public void modifyRow(final long row, final long col, final UnaryFunction<Double> modifier) {
        myUtility.modifyRow(row, col, modifier);
    }

    public void modifyRow(final long row, final UnaryFunction<Double> modifier) {
        myUtility.modifyRow(row, modifier);
    }

    public ElementView1D<Double, ?> nonzeros() {
        return myUtility.nonzeros();
    }

    public PhysicalStore.Factory<Double, ?> physical() {
        return FACTORY;
    }

    public void reduceColumns(final Aggregator aggregator, final Mutate1D receiver) {
        myUtility.reduceColumns(aggregator, receiver);
    }

    public void reduceRows(final Aggregator aggregator, final Mutate1D receiver) {
        myUtility.reduceRows(aggregator, receiver);
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

    public RowView<Double> rows() {
        return myUtility.rows();
    }

    public void set(final long row, final long col, final Comparable<?> value) {
        myUtility.set(row, col, value);
    }

    public void set(final long row, final long col, final double value) {
        myUtility.set(row, col, value);
    }

    public short shortValue(final long row, final long col) {
        return myUtility.shortValue(row, col);
    }

    public Array1D<Double> sliceColumn(final long col) {
        return myUtility.sliceColumn(col);
    }

    public Array1D<Double> sliceColumn(final long row, final long col) {
        return myUtility.sliceColumn(row, col);
    }

    public Access1D<Double> sliceDiagonal() {
        return myUtility.sliceDiagonal();
    }

    public Array1D<Double> sliceDiagonal(final long row, final long col) {
        return myUtility.sliceDiagonal(row, col);
    }

    public Array1D<Double> sliceRow(final long row) {
        return myUtility.sliceRow(row);
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
                    SubstituteBackwards.invoke(Primitive32Store.this.data, tmpRowDim, first, limit, body, unitDiagonal, conjugated, hermitian);
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
                    SubstituteForwards.invoke(Primitive32Store.this.data, tmpRowDim, first, limit, body, unitDiagonal, conjugated, identity);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, SubstituteForwards.THRESHOLD);

        } else {

            SubstituteForwards.invoke(data, tmpRowDim, 0, tmpColDim, body, unitDiagonal, conjugated, identity);
        }
    }

    public double[] toRawCopy1D() {
        return myUtility.toRawCopy1D();
    }

    public double[][] toRawCopy2D() {
        return myUtility.toRawCopy2D();
    }

    public void transformLeft(final Householder<Double> transformation, final int firstColumn) {

        final Householder.Primitive32 tmpTransf = Primitive32Store.cast(transformation);

        final float[] tmpData = data;

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
                RotateLeft.invoke(data, myRowDim, tmpLow, tmpHigh, (float) tmpTransf.cos, (float) tmpTransf.sin);
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

        final Householder.Primitive32 tmpTransf = Primitive32Store.cast(transformation);

        final float[] tmpData = data;

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        final float[] tmpWorker = this.getWorkerColumn();

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
                RotateRight.invoke(data, myRowDim, tmpLow, tmpHigh, (float) tmpTransf.cos, (float) tmpTransf.sin);
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

    public void visitColumn(final long row, final long col, final VoidFunction<Double> visitor) {
        myUtility.visitColumn(row, col, visitor);
    }

    public void visitColumn(final long col, final VoidFunction<Double> visitor) {
        myUtility.visitColumn(col, visitor);
    }

    public void visitDiagonal(final long row, final long col, final VoidFunction<Double> visitor) {
        myUtility.visitDiagonal(row, col, visitor);
    }

    public void visitDiagonal(final VoidFunction<Double> visitor) {
        myUtility.visitDiagonal(visitor);
    }

    public void visitOne(final long row, final long col, final VoidFunction<Double> visitor) {
        myUtility.visitOne(row, col, visitor);
    }

    public void visitRow(final long row, final long col, final VoidFunction<Double> visitor) {
        myUtility.visitRow(row, col, visitor);
    }

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
