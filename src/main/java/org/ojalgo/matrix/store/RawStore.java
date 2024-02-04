/*
 * Copyright 1997-2024 Optimatika
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

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.operation.*;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.matrix.operation.MultiplyBoth;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.math.MathType;

/**
 * Uses double[][] internally.
 *
 * @author apete
 */
public final class RawStore implements PhysicalStore<Double> {

    public static final PhysicalStore.Factory<Double, RawStore> FACTORY = new PrimitiveFactory<RawStore>() {

        @Override
        public RawStore columns(final Access1D<?>... source) {

            int nbRows = source[0].size();
            int nbCols = source.length;

            RawStore retVal = new RawStore(nbRows, nbCols);
            double[][] retValData = retVal.data;

            Access1D<?> tmpCol;
            for (int j = 0; j < nbCols; j++) {
                tmpCol = source[j];
                for (int i = 0; i < nbRows; i++) {
                    retValData[i][j] = tmpCol.doubleValue(i);
                }
            }

            return retVal;
        }

        @Override
        public RawStore columns(final Comparable<?>[]... source) {

            int nbRows = source[0].length;
            int nbCols = source.length;

            RawStore retVal = new RawStore(nbRows, nbCols);
            double[][] retValData = retVal.data;

            Comparable<?>[] tmpCol;
            for (int j = 0; j < nbCols; j++) {
                tmpCol = source[j];
                for (int i = 0; i < nbRows; i++) {
                    retValData[i][j] = NumberDefinition.doubleValue(tmpCol[i]);
                }
            }

            return retVal;
        }

        @Override
        public RawStore columns(final double[]... source) {

            int nbRows = source[0].length;
            int nbCols = source.length;

            RawStore retVal = new RawStore(nbRows, nbCols);
            double[][] retValData = retVal.data;

            double[] tmpCol;
            for (int j = 0; j < nbCols; j++) {
                tmpCol = source[j];
                for (int i = 0; i < nbRows; i++) {
                    retValData[i][j] = tmpCol[i];
                }
            }

            return retVal;
        }

        @Override
        public RawStore columns(final List<? extends Comparable<?>>... source) {

            int nbRows = source[0].size();
            int nbCols = source.length;

            RawStore retVal = new RawStore(nbRows, nbCols);
            double[][] retValData = retVal.data;

            List<? extends Comparable<?>> tmpCol;
            for (int j = 0; j < nbCols; j++) {
                tmpCol = source[j];
                for (int i = 0; i < nbRows; i++) {
                    retValData[i][j] = NumberDefinition.doubleValue(tmpCol.get(i));
                }
            }

            return retVal;
        }

        @Override
        public RawStore copy(final Access2D<?> source) {

            int nbRows = source.getRowDim();
            int nbCols = source.getColDim();

            RawStore retVal = new RawStore(nbRows, nbCols);

            for (int i = 0; i < nbRows; i++) {
                COPY.row(source, i, retVal.data[i], 0, nbCols);
            }

            return retVal;
        }

        @Override
        public MathType getMathType() {
            return MathType.R064;
        }

        @Override
        public RawStore make(final long rows, final long columns) {
            return new RawStore(Math.toIntExact(rows), Math.toIntExact(columns));
        }

        @Override
        public RawStore rows(final Access1D<?>... source) {

            int nbRows = source.length;
            int nbCols = source[0].size();

            RawStore retVal = new RawStore(nbRows, nbCols);

            Access1D<?> tmpRow;
            double[] retValRow;
            for (int i = 0; i < nbRows; i++) {
                tmpRow = source[i];
                retValRow = retVal.data[i];
                for (int j = 0; j < nbCols; j++) {
                    retValRow[j] = tmpRow.doubleValue(j);
                }
            }

            return retVal;
        }

        @Override
        public RawStore rows(final Comparable<?>[]... source) {

            int nbRows = source.length;
            int nbCols = source[0].length;

            RawStore retVal = new RawStore(nbRows, nbCols);

            Comparable<?>[] tmpRow;
            double[] retValRow;
            for (int i = 0; i < nbRows; i++) {
                tmpRow = source[i];
                retValRow = retVal.data[i];
                for (int j = 0; j < nbCols; j++) {
                    retValRow[j] = NumberDefinition.doubleValue(tmpRow[j]);
                }
            }

            return retVal;
        }

        @Override
        public RawStore rows(final double[]... source) {

            int nbRows = source.length;
            int nbCols = source[0].length;

            RawStore retVal = new RawStore(nbRows, nbCols);

            double[] tmpRow;
            double[] retValRow;
            for (int i = 0; i < nbRows; i++) {
                tmpRow = source[i];
                retValRow = retVal.data[i];
                for (int j = 0; j < nbCols; j++) {
                    retValRow[j] = tmpRow[j];
                }
            }

            return retVal;
        }

        @Override
        public RawStore rows(final List<? extends Comparable<?>>... source) {

            int nbRows = source.length;
            int nbCols = source[0].size();

            RawStore retVal = new RawStore(nbRows, nbCols);

            List<? extends Comparable<?>> tmpRow;
            double[] retValRow;
            for (int i = 0; i < nbRows; i++) {
                tmpRow = source[i];
                retValRow = retVal.data[i];
                for (int j = 0; j < nbCols; j++) {
                    retValRow[j] = NumberDefinition.doubleValue(tmpRow.get(j));
                }
            }

            return retVal;
        }

        @Override
        public RawStore transpose(final Access2D<?> source) {

            int nbRows = source.getColDim();
            int nbCols = source.getRowDim();

            RawStore retVal = new RawStore(nbRows, nbCols);

            for (int i = 0; i < nbRows; i++) {
                double[] retValRow = retVal.data[i];
                for (int j = 0; j < nbCols; j++) {
                    retValRow[j] = source.doubleValue(j, i);
                }
            }

            return retVal;
        }

    };

    /**
     * Will create a single row matrix with the supplied array as the inner array. You access it using
     * <code>data[0]</code>.
     */
    public static RawStore wrap(final double... data) {
        return new RawStore(new double[][] { data }, data.length);
    }

    public static RawStore wrap(final double[][] data) {
        return new RawStore(data, data[0].length);
    }

    private static RawStore convert(final Access1D<?> elements, final int structure) {

        if (elements instanceof RawStore) {
            return (RawStore) elements;
        }

        int nbCols = structure != 0 ? elements.size() / structure : 0;

        RawStore retVal = new RawStore(structure, nbCols);

        if (structure * nbCols != elements.size()) {
            throw new IllegalArgumentException("Array length must be a multiple of structure.");
        }

        for (int i = 0; i < structure; i++) {
            double[] row = retVal.data[i];
            for (int j = 0; j < nbCols; j++) {
                row[j] = elements.doubleValue(Structure2D.index(structure, i, j));
            }
        }

        return retVal;
    }

    private static double[][] extract(final Access1D<?> elements, final int nbRows) {

        double[][] retVal = null;

        if (elements instanceof RawStore && ((RawStore) elements).getRowDim() == nbRows) {

            retVal = ((RawStore) elements).data;

        } else if (elements instanceof Access2D && ((Access2D<?>) elements).getRowDim() == nbRows) {

            retVal = ((Access2D<?>) elements).toRawCopy2D();

        } else {

            int nbColumns = nbRows != 0 ? Math.toIntExact(elements.count() / nbRows) : 0;

            retVal = new double[nbRows][];

            double[] tmpRow;
            for (int i = 0; i < nbRows; i++) {
                tmpRow = retVal[i] = new double[nbColumns];
                for (int j = 0; j < nbColumns; j++) {
                    tmpRow[j] = elements.doubleValue(Structure2D.index(nbRows, i, j));
                }
            }
        }

        return retVal;
    }

    private static void multiply(final double[][] product, final double[][] left, final double[][] right) {

        int tmpRowsCount = product.length;
        int tmpComplexity = right.length;
        int tmpColsCount = right[0].length;

        double[] tmpRow;
        double[] tmpColumn = new double[tmpComplexity];
        for (int j = 0; j < tmpColsCount; j++) {
            for (int k = 0; k < tmpComplexity; k++) {
                tmpColumn[k] = right[k][j];
            }
            for (int i = 0; i < tmpRowsCount; i++) {
                tmpRow = left[i];
                double tmpVal = 0.0;
                for (int k = 0; k < tmpComplexity; k++) {
                    tmpVal += tmpRow[k] * tmpColumn[k];
                }
                product[i][j] = tmpVal;
            }
        }
    }

    static Rotation.Primitive cast(final Rotation<Double> aTransf) {
        if (aTransf instanceof Rotation.Primitive) {
            return (Rotation.Primitive) aTransf;
        }
        return new Rotation.Primitive(aTransf);
    }

    public final double[][] data;

    private final int myNumberOfColumns;

    RawStore(final double[][] elements, final int numberOfColumns) {

        super();

        data = elements;

        myNumberOfColumns = numberOfColumns;
    }

    /**
     * Construct an m-by-n matrix of zeros.
     *
     * @param m Number of rows.
     * @param n Number of colums.
     */
    RawStore(final int m, final int n) {

        super();

        myNumberOfColumns = n;
        data = new double[m][n];
    }

    @Override
    public void accept(final Access2D<?> supplied) {

        int numbRows = MissingMath.toMinIntExact(data.length, supplied.countRows());
        int numbCols = MissingMath.toMinIntExact(myNumberOfColumns, supplied.countColumns());

        for (int i = 0; i < numbRows; i++) {
            COPY.row(supplied, i, data[i], 0, numbCols);
        }
    }

    @Override
    public void add(final long row, final long col, final Comparable<?> addend) {
        data[Math.toIntExact(row)][Math.toIntExact(col)] += NumberDefinition.doubleValue(addend);
    }

    @Override
    public void add(final long row, final long col, final double addend) {
        data[Math.toIntExact(row)][Math.toIntExact(col)] += addend;
    }

    @Override
    public Double aggregateAll(final Aggregator aggregator) {

        AggregatorFunction<Double> tmpVisitor = aggregator.getFunction(PrimitiveAggregator.getSet());

        this.visitAll(tmpVisitor);

        return tmpVisitor.get();
    }

    @Override
    public List<Double> asList() {

        int tmpStructure = data.length;

        return new AbstractList<>() {

            @Override
            public Double get(final int index) {
                return RawStore.this.get(Structure2D.row(index, tmpStructure), Structure2D.column(index, tmpStructure));
            }

            @Override
            public Double set(final int index, final Double value) {
                int tmpRow = Structure2D.row(index, tmpStructure);
                int tmpColumn = Structure2D.column(index, tmpStructure);
                Double retVal = RawStore.this.get(tmpRow, tmpColumn);
                RawStore.this.set(tmpRow, tmpColumn, value);
                return retVal;
            }

            @Override
            public int size() {
                return (int) RawStore.this.count();
            }
        };
    }

    @Override
    public MatrixStore<Double> conjugate() {
        return this.transpose();
    }

    /**
     * Make a deep copy of a matrix
     */
    @Override
    public RawStore copy() {
        return new RawStore(this.toRawCopy2D(), myNumberOfColumns);
    }

    @Override
    public long count() {
        return Structure2D.count(data.length, myNumberOfColumns);
    }

    @Override
    public long countColumns() {
        return myNumberOfColumns;
    }

    @Override
    public long countRows() {
        return data.length;
    }

    @Override
    public double doubleValue(final int row, final int col) {
        return data[row][col];
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RawStore)) {
            return false;
        }
        RawStore other = (RawStore) obj;
        if (myNumberOfColumns != other.myNumberOfColumns || !Arrays.deepEquals(data, other.data)) {
            return false;
        }
        return true;
    }

    @Override
    public void exchangeColumns(final long colA, final long colB) {
        SWAP.exchangeColumns(data, Math.toIntExact(colA), Math.toIntExact(colB));
    }

    @Override
    public void exchangeRows(final long rowA, final long rowB) {
        SWAP.exchangeRows(data, Math.toIntExact(rowA), Math.toIntExact(rowB));
    }

    @Override
    public void fillAll(final Double value) {
        FillMatchingDual.fillAll(data, value.doubleValue());
    }

    @Override
    public void fillAll(final NullaryFunction<?> supplier) {
        FillMatchingDual.fillAll(data, supplier);
    }

    @Override
    public void fillByMultiplying(final Access1D<Double> left, final Access1D<Double> right) {

        int complexity = Math.toIntExact(left.count() / this.countRows());
        if (complexity != Math.toIntExact(right.count() / this.countColumns())) {
            ProgrammingError.throwForMultiplicationNotPossible();
        }

        double[][] rawLeft = RawStore.extract(left, this.getRowDim());
        double[][] rawRight = RawStore.extract(right, complexity);

        RawStore.multiply(data, rawLeft, rawRight);
    }

    @Override
    public void fillColumn(final long row, final long col, final Double value) {
        FillMatchingDual.fillColumn(data, Math.toIntExact(row), Math.toIntExact(col), value.doubleValue());
    }

    @Override
    public void fillColumn(final long row, final long col, final NullaryFunction<?> supplier) {
        FillMatchingDual.fillColumn(data, Math.toIntExact(row), Math.toIntExact(col), supplier);
    }

    @Override
    public void fillDiagonal(final long row, final long col, final Double value) {
        FillMatchingDual.fillDiagonal(data, Math.toIntExact(row), Math.toIntExact(col), value.doubleValue());
    }

    @Override
    public void fillDiagonal(final long row, final long col, final NullaryFunction<?> supplier) {
        FillMatchingDual.fillDiagonal(data, Math.toIntExact(row), Math.toIntExact(col), supplier);
    }

    @Override
    public void fillMatching(final Access1D<?> source) {

        double[] rowI;

        int structure = data.length;
        for (int i = 0; i < structure; i++) {
            rowI = data[i];

            for (int j = 0; j < myNumberOfColumns; j++) {
                rowI[j] = source.doubleValue(Structure2D.index(structure, i, j));
            }
        }
    }

    @Override
    public void fillMatching(final Access1D<Double> left, final BinaryFunction<Double> function, final Access1D<Double> right) {
        if (left == this) {
            double[][] tmpRight = RawStore.convert(right, data.length).data;
            if (function == PrimitiveMath.ADD) {
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = data[i][j] + tmpRight[i][j];
                    }
                }
            } else if (function == PrimitiveMath.DIVIDE) {
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = data[i][j] / tmpRight[i][j];
                    }
                }
            } else if (function == PrimitiveMath.MULTIPLY) {
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = data[i][j] * tmpRight[i][j];
                    }
                }
            } else if (function == PrimitiveMath.SUBTRACT) {
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = data[i][j] - tmpRight[i][j];
                    }
                }
            } else {
                FillMatchingDual.fillMatching(data, data, function, tmpRight);
            }
        } else if (right == this) {
            double[][] tmpLeft = RawStore.convert(left, data.length).data;
            if (function == PrimitiveMath.ADD) {
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = tmpLeft[i][j] + data[i][j];
                    }
                }
            } else if (function == PrimitiveMath.DIVIDE) {
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = tmpLeft[i][j] / data[i][j];
                    }
                }
            } else if (function == PrimitiveMath.MULTIPLY) {
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = tmpLeft[i][j] * data[i][j];
                    }
                }
            } else if (function == PrimitiveMath.SUBTRACT) {
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = tmpLeft[i][j] - data[i][j];
                    }
                }
            } else {
                FillMatchingDual.fillMatching(data, tmpLeft, function, data);
            }
        } else {
            FillMatchingDual.fillMatching(data, RawStore.convert(left, data.length).data, function, RawStore.convert(right, data.length).data);
        }
    }

    @Override
    public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
        this.set(row, col, values.doubleValue(valueIndex));
    }

    @Override
    public void fillOne(final long row, final long col, final Double value) {
        data[Math.toIntExact(row)][Math.toIntExact(col)] = value.doubleValue();
    }

    @Override
    public void fillOne(final long row, final long col, final NullaryFunction<?> supplier) {
        data[Math.toIntExact(row)][Math.toIntExact(col)] = supplier.doubleValue();
    }

    @Override
    public void fillRange(final long first, final long limit, final Double value) {
        FillMatchingDual.fillRange(data, (int) first, (int) limit, value.doubleValue());
    }

    @Override
    public void fillRange(final long first, final long limit, final NullaryFunction<?> supplier) {
        FillMatchingDual.fillRange(data, (int) first, (int) limit, supplier);
    }

    @Override
    public void fillRow(final long row, final long col, final Double value) {
        FillMatchingDual.fillRow(data, Math.toIntExact(row), Math.toIntExact(col), value.doubleValue());
    }

    @Override
    public void fillRow(final long row, final long col, final NullaryFunction<?> supplier) {
        FillMatchingDual.fillRow(data, Math.toIntExact(row), Math.toIntExact(col), supplier);
    }

    @Override
    public MatrixStore<Double> get() {
        return this;
    }

    @Override
    public Double get(final int row, final int col) {
        return Double.valueOf(data[row][col]);
    }

    @Override
    public int getColDim() {
        return myNumberOfColumns;
    }

    @Override
    public int getRowDim() {
        return data.length;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + Arrays.deepHashCode(data);
        return prime * result + myNumberOfColumns;
    }

    @Override
    public long indexOfLargest() {
        return AMAX.invoke(data);
    }

    @Override
    public boolean isSmall(final long row, final long col, final double comparedTo) {
        return PrimitiveScalar.isSmall(comparedTo, this.doubleValue(row, col));
    }

    @Override
    public void modifyAll(final UnaryFunction<Double> modifier) {
        ModifyAll.modifyAll(data, modifier);
    }

    @Override
    public void modifyColumn(final long row, final long col, final UnaryFunction<Double> modifier) {
        ModifyAll.modifyColumn(data, Math.toIntExact(row), Math.toIntExact(col), modifier);
    }

    @Override
    public void modifyDiagonal(final long row, final long col, final UnaryFunction<Double> modifier) {

        long tmpCount = Math.min(data.length - row, myNumberOfColumns - col);

        int tmpFirst = (int) (row + col * data.length);
        int tmpLimit = (int) (row + tmpCount + (col + tmpCount) * data.length);
        int tmpStep = 1 + data.length;

        for (int ij = tmpFirst; ij < tmpLimit; ij += tmpStep) {
            this.set(ij, modifier.invoke(this.doubleValue(ij)));
        }

    }

    @Override
    public void modifyMatching(final Access1D<Double> left, final BinaryFunction<Double> function) {

        double[] tmpRowI;

        int tmpRowDim = data.length;
        for (int i = 0; i < tmpRowDim; i++) {

            tmpRowI = data[i];

            for (int j = 0; j < myNumberOfColumns; j++) {
                tmpRowI[j] = function.invoke(left.doubleValue(Structure2D.index(tmpRowDim, i, j)), tmpRowI[j]);
            }
        }
    }

    @Override
    public void modifyMatching(final BinaryFunction<Double> function, final Access1D<Double> right) {

        double[] tmpRowI;

        int tmpRowDim = data.length;
        for (int i = 0; i < tmpRowDim; i++) {

            tmpRowI = data[i];

            for (int j = 0; j < myNumberOfColumns; j++) {
                tmpRowI[j] = function.invoke(tmpRowI[j], right.doubleValue(Structure2D.index(tmpRowDim, i, j)));
            }
        }
    }

    @Override
    public void modifyOne(final long row, final long col, final UnaryFunction<Double> modifier) {

        double tmpValue = this.doubleValue(row, col);

        tmpValue = modifier.invoke(tmpValue);

        this.set(row, col, tmpValue);
    }

    @Override
    public void modifyRange(final long first, final long limit, final UnaryFunction<Double> modifier) {
        for (long index = first; index < limit; index++) {
            this.set(index, modifier.invoke(this.doubleValue(index)));
        }
    }

    @Override
    public void modifyRow(final long row, final long col, final UnaryFunction<Double> modifier) {
        ModifyAll.modifyRow(data, Math.toIntExact(row), Math.toIntExact(col), modifier);
    }

    @Override
    public RawStore multiply(final MatrixStore<Double> right) {

        int tmpRowDim = data.length;
        int tmpComplexity = myNumberOfColumns;
        int tmpColDim = (int) (right.count() / tmpComplexity);

        RawStore retVal = new RawStore(tmpRowDim, tmpColDim);

        double[][] tmpRight = RawStore.extract(right, tmpComplexity);

        RawStore.multiply(retVal.data, data, tmpRight);

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
    public PhysicalStore.Factory<Double, RawStore> physical() {
        return FACTORY;
    }

    @Override
    public TransformableRegion<Double> regionByColumns(final int... columns) {
        return new Subregion2D.ColumnsRegion<>(this, MultiplyBoth.newPrimitive64(data.length, myNumberOfColumns), columns);
    }

    @Override
    public TransformableRegion<Double> regionByLimits(final int rowLimit, final int columnLimit) {
        return new Subregion2D.LimitRegion<>(this, MultiplyBoth.newPrimitive64(data.length, myNumberOfColumns), rowLimit, columnLimit);
    }

    @Override
    public TransformableRegion<Double> regionByOffsets(final int rowOffset, final int columnOffset) {
        return new Subregion2D.OffsetRegion<>(this, MultiplyBoth.newPrimitive64(data.length, myNumberOfColumns), rowOffset, columnOffset);
    }

    @Override
    public TransformableRegion<Double> regionByRows(final int... rows) {
        return new Subregion2D.RowsRegion<>(this, MultiplyBoth.newPrimitive64(data.length, myNumberOfColumns), rows);
    }

    @Override
    public TransformableRegion<Double> regionByTransposing() {
        return new Subregion2D.TransposedRegion<>(this, MultiplyBoth.newPrimitive64(data.length, myNumberOfColumns));
    }

    @Override
    public void reset() {
        for (int i = 0; i < data.length; i++) {
            FillAll.fill(data[i], 0, myNumberOfColumns, 1, ZERO);
        }
    }

    @Override
    public void set(final int row, final int col, final double value) {
        data[row][col] = value;
    }

    @Override
    public void set(final long row, final long col, final Comparable<?> value) {
        data[Math.toIntExact(row)][Math.toIntExact(col)] = NumberDefinition.doubleValue(value);
    }

    @Override
    public Access1D<Double> sliceRow(final long row) {
        return Access1D.wrap(data[Math.toIntExact(row)]);
    }

    @Override
    public void substituteBackwards(final Access2D<Double> body, final boolean unitDiagonal, final boolean conjugated, final boolean hermitian) {
        SubstituteBackwards.invoke(data, body, unitDiagonal, conjugated, hermitian);
    }

    @Override
    public void substituteForwards(final Access2D<Double> body, final boolean unitDiagonal, final boolean conjugated, final boolean identity) {
        SubstituteForwards.invoke(data, body, unitDiagonal, conjugated, identity);
    }

    @Override
    public void supplyTo(final TransformableRegion<Double> receiver) {
        for (int i = 0; i < data.length; i++) {
            double[] row = data[i];
            for (int j = 0; j < myNumberOfColumns; j++) {
                receiver.set(i, j, row[j]);
            }
        }
    }

    @Override
    public PrimitiveScalar toScalar(final long row, final long column) {
        return PrimitiveScalar.of(this.doubleValue(row, column));
    }

    @Override
    public String toString() {
        return Access2D.toString(this);
    }

    @Override
    public void transformLeft(final Householder<Double> transformation, final int firstColumn) {

        double[][] tmpArray = data;
        int tmpRowDim = data.length;
        int tmpColDim = myNumberOfColumns;

        int tmpFirst = transformation.first();

        double[] tmpWorkCopy = new double[(int) transformation.count()];

        double tmpScale;
        for (int j = firstColumn; j < tmpColDim; j++) {
            tmpScale = ZERO;
            for (int i = tmpFirst; i < tmpRowDim; i++) {
                tmpScale += tmpWorkCopy[i] * tmpArray[i][j];
            }
            double tmpVal, tmpVal2 = PrimitiveMath.ZERO;
            int tmpSize = (int) transformation.count();
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

    @Override
    public void transformLeft(final Rotation<Double> transformation) {

        Rotation.Primitive tmpTransf = RawStore.cast(transformation);

        int tmpLow = tmpTransf.low;
        int tmpHigh = tmpTransf.high;

        if (tmpLow != tmpHigh) {
            if (!Double.isNaN(tmpTransf.cos) && !Double.isNaN(tmpTransf.sin)) {

                double[][] tmpArray = data;
                double tmpOldLow;
                double tmpOldHigh;

                for (int j = 0; j < tmpArray[0].length; j++) {

                    tmpOldLow = tmpArray[tmpLow][j];
                    tmpOldHigh = tmpArray[tmpHigh][j];

                    tmpArray[tmpLow][j] = tmpTransf.cos * tmpOldLow + tmpTransf.sin * tmpOldHigh;
                    tmpArray[tmpHigh][j] = tmpTransf.cos * tmpOldHigh - tmpTransf.sin * tmpOldLow;
                }
            } else {
                this.exchangeRows(tmpLow, tmpHigh);
            }
        } else if (!Double.isNaN(tmpTransf.cos)) {
            this.modifyRow(tmpLow, 0, PrimitiveMath.MULTIPLY.second(tmpTransf.cos));
        } else if (!Double.isNaN(tmpTransf.sin)) {
            this.modifyRow(tmpLow, 0, PrimitiveMath.DIVIDE.second(tmpTransf.sin));
        } else {
            this.modifyRow(tmpLow, 0, PrimitiveMath.NEGATE);
        }
    }

    @Override
    public void transformRight(final Householder<Double> transformation, final int firstRow) {

        double[][] tmpArray = data;
        int tmpRowDim = data.length;
        int tmpColDim = myNumberOfColumns;

        int tmpFirst = transformation.first();

        double[] tmpWorkCopy = new double[(int) transformation.count()];

        double tmpScale;
        for (int i = firstRow; i < tmpRowDim; i++) {
            tmpScale = ZERO;
            for (int j = tmpFirst; j < tmpColDim; j++) {
                tmpScale += tmpWorkCopy[j] * tmpArray[i][j];
            }
            double tmpVal, tmpVal2 = PrimitiveMath.ZERO;
            int tmpSize = (int) transformation.count();
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

    @Override
    public void transformRight(final Rotation<Double> transformation) {

        Rotation.Primitive tmpTransf = RawStore.cast(transformation);

        int tmpLow = tmpTransf.low;
        int tmpHigh = tmpTransf.high;

        if (tmpLow != tmpHigh) {
            if (!Double.isNaN(tmpTransf.cos) && !Double.isNaN(tmpTransf.sin)) {

                double[][] tmpArray = data;
                double tmpOldLow;
                double tmpOldHigh;

                for (int i = 0; i < tmpArray.length; i++) {

                    tmpOldLow = tmpArray[i][tmpLow];
                    tmpOldHigh = tmpArray[i][tmpHigh];

                    tmpArray[i][tmpLow] = tmpTransf.cos * tmpOldLow - tmpTransf.sin * tmpOldHigh;
                    tmpArray[i][tmpHigh] = tmpTransf.cos * tmpOldHigh + tmpTransf.sin * tmpOldLow;
                }
            } else {
                this.exchangeColumns(tmpLow, tmpHigh);
            }
        } else if (!Double.isNaN(tmpTransf.cos)) {
            this.modifyColumn(0, tmpHigh, PrimitiveMath.MULTIPLY.second(tmpTransf.cos));
        } else if (!Double.isNaN(tmpTransf.sin)) {
            this.modifyColumn(0, tmpHigh, PrimitiveMath.DIVIDE.second(tmpTransf.sin));
        } else {
            this.modifyColumn(0, tmpHigh, PrimitiveMath.NEGATE);
        }
    }

    @Override
    public MatrixStore<Double> transpose() {
        return new TransposedStore<>(this);
    }

    @Override
    public void visitAll(final VoidFunction<Double> visitor) {
        VisitAll.visitAll(data, visitor);
    }

    @Override
    public void visitColumn(final long row, final long col, final VoidFunction<Double> visitor) {
        VisitAll.visitColumn(data, Math.toIntExact(row), Math.toIntExact(col), visitor);
    }

    @Override
    public void visitDiagonal(final long row, final long col, final VoidFunction<Double> visitor) {
        VisitAll.visitDiagonal(data, Math.toIntExact(row), Math.toIntExact(col), visitor);
    }

    @Override
    public void visitRange(final long first, final long limit, final VoidFunction<Double> visitor) {
        VisitAll.visitRange(data, (int) first, (int) limit, visitor);
    }

    @Override
    public void visitRow(final long row, final long col, final VoidFunction<Double> visitor) {
        VisitAll.visitRow(data, Math.toIntExact(row), Math.toIntExact(col), visitor);
    }

}
