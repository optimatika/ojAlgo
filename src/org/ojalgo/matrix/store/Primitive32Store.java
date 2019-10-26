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
import org.ojalgo.array.DenseArray;
import org.ojalgo.array.Primitive32Array;
import org.ojalgo.array.operation.FillMatchingSingle;
import org.ojalgo.array.operation.MultiplyBoth;
import org.ojalgo.array.operation.MultiplyLeft;
import org.ojalgo.array.operation.MultiplyNeither;
import org.ojalgo.array.operation.MultiplyRight;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.*;

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

        public Primitive32Store columns(Access1D<?>... source) {

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

        public Primitive32Store columns(Comparable<?>[]... source) {

            final int tmpRowDim = source[0].length;
            final int tmpColDim = source.length;

            final float[] tmpData = new float[tmpRowDim * tmpColDim];

            Comparable<?>[] tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = Scalar.floatValue(tmpColumn[i]);
                }
            }

            return new Primitive32Store(tmpRowDim, tmpColDim, tmpData);
        }

        public Primitive32Store columns(double[]... source) {

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

        public Primitive32Store columns(List<? extends Comparable<?>>... source) {

            final int tmpRowDim = source[0].size();
            final int tmpColDim = source.length;

            final float[] tmpData = new float[tmpRowDim * tmpColDim];

            List<? extends Comparable<?>> tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = Scalar.floatValue(tmpColumn.get(i));
                }
            }

            return new Primitive32Store(tmpRowDim, tmpColDim, tmpData);
        }

        public Primitive32Store copy(Access2D<?> source) {

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

        public Primitive32Store make(long rows, long columns) {
            return new Primitive32Store((int) rows, (int) columns);
        }

        public Primitive32Store makeEye(long rows, long columns) {

            final Primitive32Store retVal = this.make(rows, columns);

            retVal.fillDiagonal(ONE);

            return retVal;
        }

        public Primitive32Store rows(Access1D<?>... source) {

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

        public Primitive32Store rows(Comparable<?>[]... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].length;

            final float[] tmpData = new float[tmpRowDim * tmpColDim];

            Comparable<?>[] tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = Scalar.floatValue(tmpRow[j]);
                }
            }

            return new Primitive32Store(tmpRowDim, tmpColDim, tmpData);
        }

        public Primitive32Store rows(double[]... source) {

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

        public Primitive32Store rows(List<? extends Comparable<?>>... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].size();

            final float[] tmpData = new float[tmpRowDim * tmpColDim];

            List<? extends Comparable<?>> tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = Scalar.floatValue(tmpRow.get(j));
                }
            }

            return new Primitive32Store(tmpRowDim, tmpColDim, tmpData);
        }

        public Primitive32Store transpose(Access2D<?> source) {

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

    private final MultiplyBoth.Primitive32 multiplyBoth;
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

    public void accept(Access2D<?> supplied) {
        myUtility.accept(supplied);
    }

    public void add(long row, long col, Comparable<?> addend) {
        myUtility.add(row, col, addend);
    }

    public void add(long row, long col, double addend) {
        myUtility.add(row, col, addend);
    }

    public Double aggregateColumn(long col, Aggregator aggregator) {
        return myUtility.aggregateColumn(col, aggregator);
    }

    public Double aggregateColumn(long row, long col, Aggregator aggregator) {
        return myUtility.aggregateColumn(row, col, aggregator);
    }

    public Double aggregateDiagonal(Aggregator aggregator) {
        return myUtility.aggregateDiagonal(aggregator);
    }

    public Double aggregateDiagonal(long row, long col, Aggregator aggregator) {
        return myUtility.aggregateDiagonal(row, col, aggregator);
    }

    public Double aggregateRange(long first, long limit, Aggregator aggregator) {
        return myUtility.aggregateRange(first, limit, aggregator);
    }

    public Double aggregateRow(long row, Aggregator aggregator) {
        return myUtility.aggregateRow(row, aggregator);
    }

    public Double aggregateRow(long row, long col, Aggregator aggregator) {
        return myUtility.aggregateRow(row, col, aggregator);
    }

    public <NN extends Comparable<NN>, R extends Mutate2D.Receiver<NN>> Access2D.Collectable<NN, R> asCollectable2D() {
        return myUtility.asCollectable2D();
    }

    public List<Double> asList() {
        // TODO Auto-generated method stub
        return null;
    }

    public byte byteValue(long row, long col) {
        return myUtility.byteValue(row, col);
    }

    public ColumnView<Double> columns() {
        return myUtility.columns();
    }

    public long countColumns() {
        return myColDim;
    }

    public long countRows() {
        return myRowDim;
    }

    @Override
    public double dot(Access1D<?> vector) {
        return myUtility.dot(vector);
    }

    public double doubleValue(long row, long col) {
        return myUtility.doubleValue(row, col);
    }

    public ElementView2D<Double, ?> elements() {
        return myUtility.elements();
    }

    @Override
    public boolean equals(Object obj) {
        return myUtility.equals(obj);
    }

    public void exchangeColumns(long colA, long colB) {
        myUtility.exchangeColumns(colA, colB);
    }

    public void exchangeRows(long rowA, long rowB) {
        myUtility.exchangeRows(rowA, rowB);
    }

    static Primitive32Store cast(final Access1D<Double> matrix) {
        if (matrix instanceof Primitive32Store) {
            return (Primitive32Store) matrix;
        } else if (matrix instanceof Access2D<?>) {
            return FACTORY.copy((Access2D<?>) matrix);
        } else {
            return FACTORY.columns(matrix);
        }
    }

    public void fillByMultiplying(Access1D<Double> left, Access1D<Double> right) {

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

    public void fillColumn(long col, Access1D<Double> values) {
        myUtility.fillColumn(col, values);
    }

    public void fillColumn(long col, Double value) {
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

    public void fillColumn(long col, NullaryFunction<?> supplier) {
        myUtility.fillColumn(col, supplier);
    }

    public void fillDiagonal(Access1D<Double> values) {
        myUtility.fillDiagonal(values);
    }

    public void fillDiagonal(Double value) {
        myUtility.fillDiagonal(value);
    }

    public void fillDiagonal(long row, long col, Access1D<Double> values) {
        myUtility.fillDiagonal(row, col, values);
    }

    public void fillDiagonal(final long row, final long col, final Double value) {
        myUtility.fillDiagonal(row, col, value);
    }

    public void fillDiagonal(final long row, final long col, final NullaryFunction<?> supplier) {
        myUtility.fillDiagonal(row, col, supplier);
    }

    public void fillDiagonal(NullaryFunction<?> supplier) {
        myUtility.fillDiagonal(supplier);
    }

    @Override
    public void fillMatching(Access1D<Double> left, BinaryFunction<Double> function, Access1D<Double> right) {
        myUtility.fillMatching(left, function, right);
    }

    @Override
    public void fillMatching(UnaryFunction<Double> function, Access1D<Double> arguments) {
        myUtility.fillMatching(function, arguments);
    }

    public void fillOne(long row, long col, Access1D<?> values, long valueIndex) {
        myUtility.fillOne(row, col, values, valueIndex);
    }

    public void fillOne(long row, long col, Double value) {
        myUtility.fillOne(row, col, value);
    }

    public void fillOne(long row, long col, NullaryFunction<?> supplier) {
        myUtility.fillOne(row, col, supplier);
    }

    public void fillRow(long row, Access1D<Double> values) {
        myUtility.fillRow(row, values);
    }

    public void fillRow(long row, Double value) {
        myUtility.fillRow(row, value);
    }

    public void fillRow(long row, long col, Access1D<Double> values) {
        myUtility.fillRow(row, col, values);
    }

    public void fillRow(long row, long col, Double value) {
        myUtility.fillRow(row, col, value);
    }

    public void fillRow(long row, long col, NullaryFunction<?> supplier) {
        myUtility.fillRow(row, col, supplier);
    }

    public void fillRow(long row, NullaryFunction<?> supplier) {
        myUtility.fillRow(row, supplier);
    }

    public float floatValue(long row, long col) {
        return myUtility.floatValue(row, col);
    }

    public Double get(long row, long col) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int hashCode() {
        return myUtility.hashCode();
    }

    public long indexOfLargestInColumn(long col) {
        return myUtility.indexOfLargestInColumn(col);
    }

    public long indexOfLargestInColumn(long row, long col) {
        // TODO Auto-generated method stub
        return 0;
    }

    public long indexOfLargestInRow(long row) {
        return myUtility.indexOfLargestInRow(row);
    }

    public long indexOfLargestInRow(long row, long col) {
        // TODO Auto-generated method stub
        return 0;
    }

    public long indexOfLargestOnDiagonal() {
        return myUtility.indexOfLargestOnDiagonal();
    }

    public long indexOfLargestOnDiagonal(long first) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int intValue(long row, long col) {
        return myUtility.intValue(row, col);
    }

    public boolean isAbsolute(long row, long col) {
        return myUtility.isAbsolute(row, col);
    }

    public boolean isAcceptable(Structure2D supplier) {
        return myUtility.isAcceptable(supplier);
    }

    public boolean isAllSmall(double comparedTo) {
        return myUtility.isAllSmall(comparedTo);
    }

    public boolean isColumnSmall(long col, double comparedTo) {
        return myUtility.isColumnSmall(col, comparedTo);
    }

    public boolean isColumnSmall(long row, long col, double comparedTo) {
        return myUtility.isColumnSmall(row, col, comparedTo);
    }

    public boolean isEmpty() {
        return myUtility.isEmpty();
    }

    public boolean isFat() {
        return myUtility.isFat();
    }

    public boolean isRowSmall(long row, double comparedTo) {
        return myUtility.isRowSmall(row, comparedTo);
    }

    public boolean isRowSmall(long row, long col, double comparedTo) {
        return myUtility.isRowSmall(row, col, comparedTo);
    }

    public boolean isScalar() {
        return myUtility.isScalar();
    }

    public boolean isSmall(long row, long col, double comparedTo) {
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

    public long longValue(long row, long col) {
        return myUtility.longValue(row, col);
    }

    @Override
    public void modifyAll(UnaryFunction<Double> modifier) {
        myUtility.modifyAll(modifier);
    }

    public void modifyAny(Transformation2D<Double> modifier) {
        myUtility.modifyAny(modifier);
    }

    public void modifyColumn(long row, long col, UnaryFunction<Double> modifier) {
        myUtility.modifyColumn(row, col, modifier);
    }

    public void modifyColumn(long col, UnaryFunction<Double> modifier) {
        myUtility.modifyColumn(col, modifier);
    }

    public void modifyDiagonal(long row, long col, UnaryFunction<Double> modifier) {
        myUtility.modifyDiagonal(row, col, modifier);
    }

    public void modifyDiagonal(UnaryFunction<Double> modifier) {
        myUtility.modifyDiagonal(modifier);
    }

    @Override
    public void modifyMatching(Access1D<Double> left, BinaryFunction<Double> function) {
        myUtility.modifyMatching(left, function);
    }

    @Override
    public void modifyMatching(BinaryFunction<Double> function, Access1D<Double> right) {
        myUtility.modifyMatching(function, right);
    }

    public void modifyMatchingInColumns(Access1D<Double> left, BinaryFunction<Double> function) {
        myUtility.modifyMatchingInColumns(left, function);
    }

    public void modifyMatchingInColumns(BinaryFunction<Double> function, Access1D<Double> right) {
        myUtility.modifyMatchingInColumns(function, right);
    }

    public void modifyMatchingInRows(Access1D<Double> left, BinaryFunction<Double> function) {
        myUtility.modifyMatchingInRows(left, function);
    }

    public void modifyMatchingInRows(BinaryFunction<Double> function, Access1D<Double> right) {
        myUtility.modifyMatchingInRows(function, right);
    }

    public void modifyOne(long row, long col, UnaryFunction<Double> modifier) {
        // TODO Auto-generated method stub

    }

    public void modifyRow(long row, long col, UnaryFunction<Double> modifier) {
        myUtility.modifyRow(row, col, modifier);
    }

    public void modifyRow(long row, UnaryFunction<Double> modifier) {
        myUtility.modifyRow(row, modifier);
    }

    public ElementView1D<Double, ?> nonzeros() {
        return myUtility.nonzeros();
    }

    public PhysicalStore.Factory<Double, ?> physical() {
        return FACTORY;
    }

    public void reduceColumns(Aggregator aggregator, Mutate1D receiver) {
        myUtility.reduceColumns(aggregator, receiver);
    }

    public void reduceRows(Aggregator aggregator, Mutate1D receiver) {
        myUtility.reduceRows(aggregator, receiver);
    }

    public TransformableRegion<Double> regionByColumns(int... columns) {
        // TODO Auto-generated method stub
        return null;
    }

    public TransformableRegion<Double> regionByLimits(int rowLimit, int columnLimit) {
        // TODO Auto-generated method stub
        return null;
    }

    public TransformableRegion<Double> regionByOffsets(int rowOffset, int columnOffset) {
        // TODO Auto-generated method stub
        return null;
    }

    public TransformableRegion<Double> regionByRows(int... rows) {
        // TODO Auto-generated method stub
        return null;
    }

    public TransformableRegion<Double> regionByTransposing() {
        // TODO Auto-generated method stub
        return null;
    }

    public RowView<Double> rows() {
        return myUtility.rows();
    }

    public void set(long row, long col, Comparable<?> value) {
        myUtility.set(row, col, value);
    }

    public void set(long row, long col, double value) {
        myUtility.set(row, col, value);
    }

    public short shortValue(long row, long col) {
        return myUtility.shortValue(row, col);
    }

    public Array1D<Double> sliceColumn(long col) {
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

    public Array1D<Double> sliceRow(long row) {
        return myUtility.sliceRow(row);
    }

    public Array1D<Double> sliceRow(final long row, final long col) {
        return myUtility.sliceRow(row, col);
    }

    public void substituteBackwards(Access2D<Double> body, boolean unitDiagonal, boolean conjugated, boolean hermitian) {
        // TODO Auto-generated method stub

    }

    public void substituteForwards(Access2D<Double> body, boolean unitDiagonal, boolean conjugated, boolean identity) {
        // TODO Auto-generated method stub

    }

    public void supplyTo(double[] receiver) {
        myUtility.supplyTo(receiver);
    }

    public double[] toRawCopy1D() {
        return myUtility.toRawCopy1D();
    }

    public double[][] toRawCopy2D() {
        return myUtility.toRawCopy2D();
    }

    public void transformLeft(Householder<Double> transformation, int firstColumn) {
        // TODO Auto-generated method stub

    }

    public void transformLeft(Rotation<Double> transformation) {
        // TODO Auto-generated method stub

    }

    public void transformRight(Householder<Double> transformation, int firstRow) {
        // TODO Auto-generated method stub

    }

    public void transformRight(Rotation<Double> transformation) {
        // TODO Auto-generated method stub

    }

    public void visitColumn(final long row, final long col, final VoidFunction<Double> visitor) {
        myUtility.visitColumn(row, col, visitor);
    }

    public void visitColumn(long col, VoidFunction<Double> visitor) {
        myUtility.visitColumn(col, visitor);
    }

    public void visitDiagonal(final long row, final long col, final VoidFunction<Double> visitor) {
        myUtility.visitDiagonal(row, col, visitor);
    }

    public void visitDiagonal(VoidFunction<Double> visitor) {
        myUtility.visitDiagonal(visitor);
    }

    public void visitOne(long row, long col, VoidFunction<Double> visitor) {
        myUtility.visitOne(row, col, visitor);
    }

    public void visitRow(final long row, final long col, final VoidFunction<Double> visitor) {
        myUtility.visitRow(row, col, visitor);
    }

    public void visitRow(long row, VoidFunction<Double> visitor) {
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
