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

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import java.io.BufferedReader;
import java.io.Serializable;
import java.io.StreamTokenizer;
import java.util.AbstractList;
import java.util.List;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.array.ArrayUtils;
import org.ojalgo.array.BasicArray;
import org.ojalgo.array.PrimitiveArray;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.operation.MultiplyBoth;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * Uses double[][] internally.
 *
 * @author apete
 */
public final class RawStore extends Object implements PhysicalStore<Double>, Serializable {

    public static PhysicalStore.Factory<Double, RawStore> FACTORY = new PhysicalStore.Factory<Double, RawStore>() {

        public AggregatorSet<Double> aggregator() {
            return PrimitiveAggregator.getSet();
        }

        public MatrixStore.Factory<Double> builder() {
            return MatrixStore.PRIMITIVE;
        }

        public RawStore columns(final Access1D<?>... source) {

            final int tmpRowDim = (int) source[0].count();
            final int tmpColDim = source.length;

            final double[][] retVal = new double[tmpRowDim][tmpColDim];

            Access1D<?> tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    retVal[i][j] = tmpColumn.doubleValue(i);
                }
            }

            return new RawStore(retVal);
        }

        public RawStore columns(final double[]... source) {

            final int tmpRowDim = source[0].length;
            final int tmpColDim = source.length;

            final double[][] retVal = new double[tmpRowDim][tmpColDim];

            double[] tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    retVal[i][j] = tmpColumn[i];
                }
            }

            return new RawStore(retVal);
        }

        public RawStore columns(final List<? extends Number>... source) {

            final int tmpRowDim = source[0].size();
            final int tmpColDim = source.length;

            final double[][] retVal = new double[tmpRowDim][tmpColDim];

            List<? extends Number> tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    retVal[i][j] = tmpColumn.get(i).doubleValue();
                }
            }

            return new RawStore(retVal);
        }

        public RawStore columns(final Number[]... source) {

            final int tmpRowDim = source[0].length;
            final int tmpColDim = source.length;

            final double[][] retVal = new double[tmpRowDim][tmpColDim];

            Number[] tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    retVal[i][j] = tmpColumn[i].doubleValue();
                }
            }

            return new RawStore(retVal);
        }

        public RawStore conjugate(final Access2D<?> source) {
            return this.transpose(source);
        }

        public RawStore copy(final Access2D<?> source) {

            final int tmpRowDim = (int) source.countRows();
            final int tmpColDim = (int) source.countColumns();

            final double[][] retVal = new double[tmpRowDim][tmpColDim];

            MatrixUtils.copy(source, tmpRowDim, tmpColDim, retVal);

            return new RawStore(retVal, tmpRowDim, tmpColDim);
        }

        public FunctionSet<Double> function() {
            return PrimitiveFunction.getSet();
        }

        public BasicArray<Double> makeArray(final int length) {
            return PrimitiveArray.make(length);
        }

        public RawStore makeEye(final long rows, final long columns) {

            final RawStore retVal = this.makeZero(rows, columns);

            retVal.fillDiagonal(0, 0, this.scalar().one().getNumber());

            return retVal;
        }

        public RawStore makeFilled(final long rows, final long columns, final NullaryFunction<?> supplier) {

            final double[][] retVal = new double[(int) rows][(int) columns];

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    retVal[i][j] = supplier.doubleValue();
                }
            }

            return new RawStore(retVal);
        }

        public Householder<Double> makeHouseholder(final int length) {
            return new Householder.Primitive(length);
        }

        public Rotation<Double> makeRotation(final int low, final int high, final double cos, final double sin) {
            return new Rotation.Primitive(low, high, cos, sin);
        }

        public Rotation<Double> makeRotation(final int low, final int high, final Double cos, final Double sin) {
            return new Rotation.Primitive(low, high, cos, sin);
        }

        public RawStore makeZero(final long rows, final long columns) {
            return new RawStore(new double[(int) rows][(int) columns]);
        }

        public RawStore rows(final Access1D<?>... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = (int) source[0].count();

            final double[][] retVal = new double[tmpRowDim][tmpColDim];

            Access1D<?> tmpSource;
            double[] tmpDestination;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpSource = source[i];
                tmpDestination = retVal[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpDestination[j] = tmpSource.doubleValue(j);
                }
            }

            return new RawStore(retVal);
        }

        public RawStore rows(final double[]... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].length;

            final double[][] retVal = new double[tmpRowDim][tmpColDim];

            double[] tmpSource;
            double[] tmpDestination;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpSource = source[i];
                tmpDestination = retVal[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpDestination[j] = tmpSource[j];
                }
            }

            return new RawStore(retVal);
        }

        public RawStore rows(final List<? extends Number>... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].size();

            final double[][] retVal = new double[tmpRowDim][tmpColDim];

            List<? extends Number> tmpSource;
            double[] tmpDestination;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpSource = source[i];
                tmpDestination = retVal[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpDestination[j] = tmpSource.get(j).doubleValue();
                }
            }

            return new RawStore(retVal);
        }

        public RawStore rows(final Number[]... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].length;

            final double[][] retVal = new double[tmpRowDim][tmpColDim];

            Number[] tmpSource;
            double[] tmpDestination;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpSource = source[i];
                tmpDestination = retVal[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpDestination[j] = tmpSource[j].doubleValue();
                }
            }

            return new RawStore(retVal);
        }

        public Scalar.Factory<Double> scalar() {
            return PrimitiveScalar.FACTORY;
        }

        public RawStore transpose(final Access2D<?> source) {

            final int tmpRowDim = (int) source.countColumns();
            final int tmpColDim = (int) source.countRows();

            final double[][] retVal = new double[tmpRowDim][tmpColDim];

            for (int i = 0; i < tmpRowDim; i++) {
                for (int j = 0; j < tmpColDim; j++) {
                    retVal[i][j] = source.doubleValue(j, i);
                }
            }

            return new RawStore(retVal);
        }

    };

    /**
     * Construct a matrix from a copy of a 2-D array.
     *
     * @param A Two-dimensional array of doubles.
     * @exception IllegalArgumentException All rows must have the same length
     */
    public static RawStore constructWithCopy(final double[][] A) {
        final int m = A.length;
        final int n = A[0].length;
        final RawStore X = new RawStore(m, n);
        final double[][] C = X.data;
        for (int i = 0; i < m; i++) {
            if (A[i].length != n) {
                throw new IllegalArgumentException("All rows must have the same length.");
            }
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j];
            }
        }
        return X;
    }

    /**
     * Generate matrix with random elements
     *
     * @param m Number of rows.
     * @param n Number of colums.
     * @return An m-by-n matrix with uniformly distributed random elements.
     */
    public static RawStore random(final int m, final int n) {
        final RawStore A = new RawStore(m, n);
        final double[][] X = A.data;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                X[i][j] = Math.random();
            }
        }
        return A;
    }

    /**
     * Read a matrix from a stream. The format is the same the print method, so printed matrices can be read
     * back in (provided they were printed using US Locale). Elements are separated by whitespace, all the
     * elements for each row appear on a single line, the last row is followed by a blank line.
     *
     * @param input the input stream.
     */
    public static RawStore read(final BufferedReader input) throws java.io.IOException {
        final StreamTokenizer tokenizer = new StreamTokenizer(input);

        // Although StreamTokenizer will parse numbers, it doesn't recognize
        // scientific notation (E or D); however, Double.valueOf does.
        // The strategy here is to disable StreamTokenizer's number parsing.
        // We'll only get whitespace delimited words, EOL's and EOF's.
        // These words should all be numbers, for Double.valueOf to parse.

        tokenizer.resetSyntax();
        tokenizer.wordChars(0, 255);
        tokenizer.whitespaceChars(0, ' ');
        tokenizer.eolIsSignificant(true);
        final java.util.Vector<Double> vD = new java.util.Vector<Double>();

        // Ignore initial empty lines
        while (tokenizer.nextToken() == StreamTokenizer.TT_EOL) {
            ;
        }
        if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
            throw new java.io.IOException("Unexpected EOF on matrix read.");
        }
        do {
            vD.addElement(Double.valueOf(tokenizer.sval)); // Read & store 1st row.
        } while (tokenizer.nextToken() == StreamTokenizer.TT_WORD);

        final int n = vD.size(); // Now we've got the number of columns!
        double row[] = new double[n];
        for (int j = 0; j < n; j++) {
            row[j] = vD.elementAt(j).doubleValue();
        }
        final java.util.Vector<double[]> v = new java.util.Vector<double[]>();
        v.addElement(row); // Start storing rows instead of columns.
        while (tokenizer.nextToken() == StreamTokenizer.TT_WORD) {
            // While non-empty lines
            v.addElement(row = new double[n]);
            int j = 0;
            do {
                if (j >= n) {
                    throw new java.io.IOException("Row " + v.size() + " is too long.");
                }
                row[j++] = Double.valueOf(tokenizer.sval).doubleValue();
            } while (tokenizer.nextToken() == StreamTokenizer.TT_WORD);
            if (j < n) {
                throw new java.io.IOException("Row " + v.size() + " is too short.");
            }
        }
        final int m = v.size(); // Now we've got the number of rows.
        final double[][] A = new double[m][];
        v.copyInto(A); // copy the rows out of the vector
        return new RawStore(A);
    }

    private static RawStore convert(final Access1D<?> elements, final int structure) {

        RawStore retVal = null;

        if (elements instanceof RawStore) {
            retVal = ((RawStore) elements);
        } else {
            retVal = new RawStore(ArrayUtils.toRawCopyOf(elements), structure);
        }

        return retVal;
    }

    private static RawStore convert(final Access2D<?> elements) {

        RawStore retVal = null;

        if (elements instanceof RawStore) {
            retVal = ((RawStore) elements);
        } else {
            retVal = new RawStore(ArrayUtils.toRawCopyOf(elements), (int) elements.countRows(), (int) elements.countColumns());
        }

        return retVal;
    }

    private static double[][] extract(final Access1D<?> elements, final int structure) {

        double[][] retVal = null;

        if (elements instanceof RawStore) {

            retVal = ((RawStore) elements).data;

        } else if (elements instanceof Access2D) {

            retVal = ArrayUtils.toRawCopyOf(((Access2D<?>) elements));

        } else {

            final int tmpNumberOfColumns = (int) (structure != 0 ? (elements.count() / structure) : 0);

            if ((structure * tmpNumberOfColumns) != elements.count()) {
                throw new IllegalArgumentException("Array length must be a multiple of structure.");
            }

            retVal = new double[structure][];

            double[] tmpRow;
            for (int i = 0; i < structure; i++) {
                tmpRow = retVal[i] = new double[tmpNumberOfColumns];
                for (int j = 0; j < tmpNumberOfColumns; j++) {
                    tmpRow[j] = elements.doubleValue(i + (j * structure));
                }
            }
        }

        return retVal;
    }

    private static void multiply(final double[][] product, final double[][] left, final double[][] right) {

        final int tmpRowsCount = product.length;
        final int tmpComplexity = right.length;
        final int tmpColsCount = right[0].length;

        double[] tmpRow;
        final double[] tmpColumn = new double[tmpComplexity];
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
        } else {
            return new Rotation.Primitive(aTransf);
        }
    }

    public final double[][] data;

    private final int myNumberOfColumns;

    public RawStore(final Access2D<?> template) {

        super();

        final RawStore tmpConverted = RawStore.convert(template);

        data = tmpConverted.data;

        myNumberOfColumns = (int) template.countColumns();
    }

    /**
     * Construct a matrix from a one-dimensional packed array
     *
     * @param elements One-dimensional array of doubles, packed by columns (ala Fortran).
     * @param structure Number of rows.
     * @exception IllegalArgumentException Array length must be a multiple of m.
     */
    public RawStore(final double elements[], final int structure) {

        myNumberOfColumns = (structure != 0 ? elements.length / structure : 0);

        if ((structure * myNumberOfColumns) != elements.length) {
            throw new IllegalArgumentException("Array length must be a multiple of structure.");
        }

        data = new double[structure][myNumberOfColumns];

        for (int i = 0; i < structure; i++) {
            for (int j = 0; j < myNumberOfColumns; j++) {
                data[i][j] = elements[i + (j * structure)];
            }
        }

    }

    /**
     * Construct a matrix from a 2-D array.
     *
     * @param A Two-dimensional array of doubles.
     * @exception IllegalArgumentException All rows must have the same length
     * @see #constructWithCopy
     */
    public RawStore(final double[][] A) {

        myNumberOfColumns = A[0].length;
        for (int i = 0; i < A.length; i++) {
            if (A[i].length != myNumberOfColumns) {
                throw new IllegalArgumentException("All rows must have the same length.");
            }
        }
        data = A;

    }

    /**
     * Construct a matrix quickly without checking arguments.
     *
     * @param A Two-dimensional array of doubles.
     * @param m Number of rows.
     * @param n Number of colums.
     */
    public RawStore(final double[][] A, final int m, final int n) {

        data = A;

        myNumberOfColumns = n;
    }

    /**
     * Construct an m-by-n matrix of zeros.
     *
     * @param m Number of rows.
     * @param n Number of colums.
     */
    public RawStore(final int m, final int n) {

        myNumberOfColumns = n;
        data = new double[m][n];

    }

    /**
     * Construct an m-by-n constant matrix.
     *
     * @param m Number of rows.
     * @param n Number of colums.
     * @param s Fill the matrix with this scalar value.
     */
    public RawStore(final int m, final int n, final double s) {

        myNumberOfColumns = n;
        data = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                data[i][j] = s;
            }
        }

    }

    @SuppressWarnings("unused")
    private RawStore() {

        super();

        data = new double[0][0];

        myNumberOfColumns = 0;

        ProgrammingError.throwForIllegalInvocation();
    }

    RawStore(final double[][] elements, final int numberOfColumns) {

        super();

        data = elements;

        myNumberOfColumns = numberOfColumns;
    }

    public void accept(final Access2D<Double> supplied) {
        for (long j = 0; j < supplied.countColumns(); j++) {
            for (long i = 0; i < supplied.countRows(); i++) {
                this.set(i, j, supplied.doubleValue(i, j));
            }
        }
    }

    public void add(final long row, final long column, final double addend) {
        data[(int) row][(int) column] += addend;
    }

    public void add(final long row, final long column, final Number addend) {
        data[(int) row][(int) column] += addend.doubleValue();
    }

    public Double aggregateAll(final Aggregator aggregator) {

        final AggregatorFunction<Double> tmpVisitor = aggregator.getPrimitiveFunction();

        this.visitAll(tmpVisitor);

        return tmpVisitor.getNumber();
    }

    public List<Double> asList() {

        final int tmpStructure = data.length;

        return new AbstractList<Double>() {

            @Override
            public Double get(final int index) {
                return RawStore.this.get(AccessUtils.row(index, tmpStructure), AccessUtils.column(index, tmpStructure));
            }

            @Override
            public Double set(final int index, final Double value) {
                final int tmpRow = AccessUtils.row(index, tmpStructure);
                final int tmpColumn = AccessUtils.column(index, tmpStructure);
                final Double retVal = RawStore.this.get(tmpRow, tmpColumn);
                RawStore.this.set(tmpRow, tmpColumn, value);
                return retVal;
            }

            @Override
            public int size() {
                return (int) RawStore.this.count();
            }
        };
    }

    public MatrixStore.Builder<Double> builder() {
        return new MatrixStore.Builder<Double>(this);
    }

    public void caxpy(final Double scalarA, final int columnX, final int columnY, final int firstRow) {

        final double tmpValA = scalarA.doubleValue();
        final double[][] tmpArray = data;

        final int tmpRowDim = data.length;

        for (int i = firstRow; i < tmpRowDim; i++) {
            tmpArray[i][columnY] += tmpValA * tmpArray[i][columnX];
        }
    }

    public RawStore conjugate() {
        return this.transpose();
    }

    /**
     * Make a deep copy of a matrix
     */
    public RawStore copy() {
        return new RawStore(this.copyOfData(), myNumberOfColumns);
    }

    /**
     * Copy the internal two-dimensional array.
     *
     * @return Two-dimensional array copy of matrix elements.
     */
    public double[][] copyOfData() {
        final int tmpLength = data.length;
        final double[][] retVal = new double[tmpLength][];
        for (int i = 0; i < tmpLength; i++) {
            retVal[i] = ArrayUtils.copyOf(data[i]);
        }
        return retVal;
    }

    public long count() {
        return data.length * myNumberOfColumns;
    }

    public long countColumns() {
        return myNumberOfColumns;
    }

    public long countRows() {
        return data.length;
    }

    public double doubleValue(final long row, final long column) {
        return data[(int) row][(int) column];
    }

    public boolean equals(final MatrixStore<Double> other, final NumberContext context) {
        return AccessUtils.equals(this, other, context);
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof Access2D<?>) {
            return AccessUtils.equals(this, (Access2D<?>) other, NumberContext.getGeneral(6));
        } else {
            return super.equals(other);
        }
    }

    public void exchangeColumns(final long colA, final long colB) {
        ArrayUtils.exchangeColumns(data, (int) colA, (int) colB);
    }

    public void exchangeRows(final long rowA, final long rowB) {
        ArrayUtils.exchangeRows(data, (int) rowA, (int) rowB);
    }

    public PhysicalStore.Factory<Double, RawStore> factory() {
        return FACTORY;
    }

    public void fillAll(final Double value) {
        ArrayUtils.fillAll(data, value);
    }

    public void fillAll(final NullaryFunction<Double> supplier) {
        ArrayUtils.fillAll(data, supplier);
    }

    public void fillByMultiplying(final Access1D<Double> leftMatrix, final Access1D<Double> rightMatrix) {
        final double[][] tmpLeft = RawStore.extract(leftMatrix, data.length);
        final double[][] tmpRight = RawStore.extract(rightMatrix, (int) (leftMatrix.count() / data.length));
        RawStore.multiply(data, tmpLeft, tmpRight);
    }

    public void fillColumn(final long row, final long column, final Double value) {
        ArrayUtils.fillColumn(data, (int) row, (int) column, value);
    }

    public void fillColumn(final long row, final long column, final NullaryFunction<Double> supplier) {
        ArrayUtils.fillColumn(data, (int) row, (int) column, supplier);
    }

    public void fillDiagonal(final long row, final long column, final Double value) {
        ArrayUtils.fillDiagonal(data, (int) row, (int) column, value);
    }

    public void fillDiagonal(final long row, final long column, final NullaryFunction<Double> supplier) {
        ArrayUtils.fillDiagonal(data, (int) row, (int) column, supplier);
    }

    public void fillMatching(final Access1D<?> source) {

        double[] tmpRowI;

        final int tmpRowDim = data.length;
        for (int i = 0; i < tmpRowDim; i++) {

            tmpRowI = data[i];

            for (int j = 0; j < myNumberOfColumns; j++) {
                tmpRowI[j] = source.doubleValue(i + (j * tmpRowDim));
            }
        }
    }

    public void fillMatching(final Access1D<Double> left, final BinaryFunction<Double> function, final Access1D<Double> right) {
        if (left == this) {
            final double[][] tmpRight = RawStore.convert(right, data.length).data;
            if (function == ADD) {
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = data[i][j] + tmpRight[i][j];
                    }
                }
            } else if (function == DIVIDE) {
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = data[i][j] / tmpRight[i][j];
                    }
                }
            } else if (function == MULTIPLY) {
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = data[i][j] * tmpRight[i][j];
                    }
                }
            } else if (function == SUBTRACT) {
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = data[i][j] - tmpRight[i][j];
                    }
                }
            } else {
                ArrayUtils.fillMatching(data, data, function, tmpRight);
            }
        } else if (right == this) {
            final double[][] tmpLeft = RawStore.convert(left, data.length).data;
            if (function == ADD) {
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = tmpLeft[i][j] + data[i][j];
                    }
                }
            } else if (function == DIVIDE) {
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = tmpLeft[i][j] / data[i][j];
                    }
                }
            } else if (function == MULTIPLY) {
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = tmpLeft[i][j] * data[i][j];
                    }
                }
            } else if (function == SUBTRACT) {
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = tmpLeft[i][j] - data[i][j];
                    }
                }
            } else {
                ArrayUtils.fillMatching(data, tmpLeft, function, data);
            }
        } else {
            ArrayUtils.fillMatching(data, RawStore.convert(left, data.length).data, function, RawStore.convert(right, data.length).data);
        }
    }

    public void fillMatching(final Access1D<Double> left, final BinaryFunction<Double> function, final Double right) {
        ArrayUtils.fillMatching(data, RawStore.convert(left, data.length).data, function, right);
    }

    public void fillMatching(final Double left, final BinaryFunction<Double> function, final Access1D<Double> right) {
        ArrayUtils.fillMatching(data, left, function, RawStore.convert(right, data.length).data);
    }

    public void fillOne(final long row, final long column, final Double value) {
        data[(int) row][(int) column] = value;
    }

    public void fillOne(final long row, final long column, final NullaryFunction<Double> supplier) {
        data[(int) row][(int) column] = supplier.doubleValue();
    }

    public void fillOneMatching(final long row, final long column, final Access1D<?> values, final long valueIndex) {
        this.set(row, column, values.doubleValue(valueIndex));
    }

    public void fillRange(final long first, final long limit, final Double value) {
        ArrayUtils.fillRange(data, (int) first, (int) limit, value);
    }

    public void fillRange(final long first, final long limit, final NullaryFunction<Double> supplier) {
        ArrayUtils.fillRange(data, (int) first, (int) limit, supplier);
    }

    public void fillRow(final long row, final long column, final Double value) {
        ArrayUtils.fillRow(data, (int) row, (int) column, value);
    }

    public void fillRow(final long row, final long column, final NullaryFunction<Double> supplier) {
        ArrayUtils.fillRow(data, (int) row, (int) column, supplier);
    }

    public final MatrixStore<Double> get() {
        return this;
    }

    public Double get(final long row, final long column) {
        return data[(int) row][(int) column];
    }

    /**
     * Get a submatrix.
     *
     * @param i0 Initial row index
     * @param i1 Final row index
     * @param j0 Initial column index
     * @param j1 Final column index
     * @return A(i0:i1,j0:j1)
     * @exception ArrayIndexOutOfBoundsException Submatrix indices
     */
    public RawStore getMatrix(final int i0, final int i1, final int j0, final int j1) {
        final RawStore X = new RawStore((i1 - i0) + 1, (j1 - j0) + 1);
        final double[][] B = X.data;
        try {
            for (int i = i0; i <= i1; i++) {
                for (int j = j0; j <= j1; j++) {
                    B[i - i0][j - j0] = data[i][j];
                }
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
        return X;
    }

    /**
     * Get a submatrix.
     *
     * @param r Array of row indices.
     * @param j0 Initial column index
     * @param j1 Final column index
     * @return A(r(:),j0:j1)
     * @exception ArrayIndexOutOfBoundsException Submatrix indices
     */
    public RawStore getMatrix(final int[] r, final int j0, final int j1) {
        final RawStore X = new RawStore(r.length, (j1 - j0) + 1);
        final double[][] B = X.data;
        try {
            for (int i = 0; i < r.length; i++) {
                for (int j = j0; j <= j1; j++) {
                    B[i][j - j0] = data[r[i]][j];
                }
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
        return X;
    }

    @Override
    public int hashCode() {
        return MatrixUtils.hashCode(this);
    }

    public long indexOfLargestInColumn(final long row, final long column) {
        // TODO Auto-generated method stub
        return 0;
    }

    public long indexOfLargestInDiagonal(final long row, final long column) {
        // TODO Auto-generated method stub
        return 0;
    }

    public long indexOfLargestInRange(final long first, final long limit) {
        // TODO Auto-generated method stub
        return 0;
    }

    public long indexOfLargestInRow(final long row, final long column) {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean isAbsolute(final long index) {
        final int tmpRowDim = data.length;
        return PrimitiveScalar.isAbsolute(this.get(AccessUtils.row(index, tmpRowDim), AccessUtils.column(index, tmpRowDim)));
    }

    public boolean isAbsolute(final long row, final long column) {
        return PrimitiveScalar.isAbsolute(this.get((int) row, (int) column));
    }

    public boolean isSmall(final long index, final double comparedTo) {
        final int tmpRowDim = data.length;
        return PrimitiveScalar.isSmall(comparedTo, this.get(AccessUtils.row(index, tmpRowDim), AccessUtils.column(index, tmpRowDim)));
    }

    public boolean isSmall(final long row, final long column, final double comparedTo) {
        return PrimitiveScalar.isSmall(comparedTo, this.doubleValue(row, column));
    }

    public void maxpy(final Double aSclrA, final MatrixStore<Double> aMtrxX) {

        final double tmpValA = aSclrA;
        final double[][] tmpArray = data;

        final int tmpRowDim = data.length;
        final int tmpColDim = myNumberOfColumns;

        for (int i = 0; i < tmpRowDim; i++) {
            for (int j = 0; j < tmpColDim; j++) {
                tmpArray[i][j] += tmpValA * aMtrxX.doubleValue(i, j);
            }
        }
    }

    public void modifyAll(final UnaryFunction<Double> function) {
        ArrayUtils.modifyAll(data, function);
    }

    public void modifyColumn(final long row, final long column, final UnaryFunction<Double> function) {
        ArrayUtils.modifyColumn(data, (int) row, (int) column, function);
    }

    public void modifyDiagonal(final long row, final long column, final UnaryFunction<Double> function) {

        final long tmpCount = Math.min(data.length - row, myNumberOfColumns - column);

        final int tmpFirst = (int) (row + (column * data.length));
        final int tmpLimit = (int) (row + tmpCount + ((column + tmpCount) * data.length));
        final int tmpStep = 1 + data.length;

        for (int ij = tmpFirst; ij < tmpLimit; ij += tmpStep) {
            this.set(ij, function.invoke(this.doubleValue(ij)));
        }

    }

    public void modifyMatching(final Access1D<Double> left, final BinaryFunction<Double> function) {

        double[] tmpRowI;

        final int tmpRowDim = data.length;
        for (int i = 0; i < tmpRowDim; i++) {

            tmpRowI = data[i];

            for (int j = 0; j < myNumberOfColumns; j++) {
                tmpRowI[j] = function.invoke(left.doubleValue(i + (j * tmpRowDim)), tmpRowI[j]);
            }
        }
    }

    public void modifyMatching(final BinaryFunction<Double> function, final Access1D<Double> right) {

        double[] tmpRowI;

        final int tmpRowDim = data.length;
        for (int i = 0; i < tmpRowDim; i++) {

            tmpRowI = data[i];

            for (int j = 0; j < myNumberOfColumns; j++) {
                tmpRowI[j] = function.invoke(tmpRowI[j], right.doubleValue(i + (j * tmpRowDim)));
            }
        }
    }

    public void modifyOne(final long row, final long column, final UnaryFunction<Double> function) {

        double tmpValue = this.doubleValue(row, column);

        tmpValue = function.invoke(tmpValue);

        this.set(row, column, tmpValue);
    }

    public void modifyRange(final long first, final long limit, final UnaryFunction<Double> function) {
        for (long index = first; index < limit; index++) {
            this.set(index, function.invoke(this.doubleValue(index)));
        }
    }

    public void modifyRow(final long row, final long column, final UnaryFunction<Double> function) {
        ArrayUtils.modifyRow(data, (int) row, (int) column, function);
    }

    public RawStore multiply(final Access1D<Double> right) {

        final int tmpRowDim = data.length;
        final int tmpComplexity = myNumberOfColumns;
        final int tmpColDim = (int) (right.count() / tmpComplexity);

        final RawStore retVal = new RawStore(tmpRowDim, tmpColDim);

        final double[][] tmpRight = RawStore.extract(right, tmpComplexity);

        RawStore.multiply(retVal.data, data, tmpRight);

        return retVal;
    }

    public Double multiplyBoth(final Access1D<Double> leftAndRight) {

        final PhysicalStore<Double> tmpStep1 = FACTORY.makeZero(1L, leftAndRight.count());
        final PhysicalStore<Double> tmpStep2 = FACTORY.makeZero(1L, 1L);

        tmpStep1.fillByMultiplying(leftAndRight, this);
        tmpStep2.fillByMultiplying(tmpStep1, leftAndRight);

        return tmpStep2.get(0L);
    }

    public void raxpy(final Double scalarA, final int rowX, final int rowY, final int firstColumn) {

        final double tmpValA = scalarA.doubleValue();
        final double[][] tmpArray = data;

        final int tmpColDim = myNumberOfColumns;

        for (int j = firstColumn; j < tmpColDim; j++) {
            tmpArray[rowY][j] += tmpValA * tmpArray[rowX][j];

        }
    }

    public final ElementsConsumer<Double> regionByColumns(final int... columns) {
        return new ColumnsRegion<Double>(this, MultiplyBoth.getPrimitive(data.length, myNumberOfColumns), columns);
    }

    public final ElementsConsumer<Double> regionByLimits(final int rowLimit, final int columnLimit) {
        return new LimitRegion<Double>(this, MultiplyBoth.getPrimitive(data.length, myNumberOfColumns), rowLimit, columnLimit);
    }

    public final ElementsConsumer<Double> regionByOffsets(final int rowOffset, final int columnOffset) {
        return new OffsetRegion<Double>(this, MultiplyBoth.getPrimitive(data.length, myNumberOfColumns), rowOffset, columnOffset);
    }

    public final ElementsConsumer<Double> regionByRows(final int... rows) {
        return new RowsRegion<Double>(this, MultiplyBoth.getPrimitive(data.length, myNumberOfColumns), rows);
    }

    public final ElementsConsumer<Double> regionByTransposing() {
        return new TransposedRegion<Double>(this, MultiplyBoth.getPrimitive(data.length, myNumberOfColumns));
    }

    public void set(final long row, final long column, final double value) {
        data[(int) row][(int) column] = value;
    }

    public void set(final long row, final long column, final Number value) {
        data[(int) row][(int) column] = value.doubleValue();
    }

    public void supplyTo(final ElementsConsumer<Double> consumer) {
        consumer.fillMatching(this);
    }

    public PrimitiveScalar toScalar(final long row, final long column) {
        return PrimitiveScalar.of(this.doubleValue(row, column));
    }

    @Override
    public String toString() {
        return MatrixUtils.toString(this);
    }

    public void transformLeft(final Householder<Double> transformation, final int firstColumn) {

        final double[][] tmpArray = data;
        final int tmpRowDim = data.length;
        final int tmpColDim = myNumberOfColumns;

        final int tmpFirst = transformation.first();

        final double[] tmpWorkCopy = new double[(int) transformation.count()];

        double tmpScale;
        for (int j = firstColumn; j < tmpColDim; j++) {
            tmpScale = ZERO;
            for (int i = tmpFirst; i < tmpRowDim; i++) {
                tmpScale += tmpWorkCopy[i] * tmpArray[i][j];
            }
            double tmpVal, tmpVal2 = PrimitiveMath.ZERO;
            final int tmpSize = (int) transformation.count();
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

    public void transformLeft(final Rotation<Double> transformation) {

        final Rotation.Primitive tmpTransf = RawStore.cast(transformation);

        final int tmpLow = tmpTransf.low;
        final int tmpHigh = tmpTransf.high;

        if (tmpLow != tmpHigh) {
            if (!Double.isNaN(tmpTransf.cos) && !Double.isNaN(tmpTransf.sin)) {

                final double[][] tmpArray = data;
                double tmpOldLow;
                double tmpOldHigh;

                for (int j = 0; j < tmpArray[0].length; j++) {

                    tmpOldLow = tmpArray[tmpLow][j];
                    tmpOldHigh = tmpArray[tmpHigh][j];

                    tmpArray[tmpLow][j] = (tmpTransf.cos * tmpOldLow) + (tmpTransf.sin * tmpOldHigh);
                    tmpArray[tmpHigh][j] = (tmpTransf.cos * tmpOldHigh) - (tmpTransf.sin * tmpOldLow);
                }
            } else {
                this.exchangeRows(tmpLow, tmpHigh);
            }
        } else {
            if (!Double.isNaN(tmpTransf.cos)) {
                this.modifyRow(tmpLow, 0, MULTIPLY.second(tmpTransf.cos));
            } else if (!Double.isNaN(tmpTransf.sin)) {
                this.modifyRow(tmpLow, 0, DIVIDE.second(tmpTransf.sin));
            } else {
                this.modifyRow(tmpLow, 0, NEGATE);
            }
        }
    }

    public void transformRight(final Householder<Double> transformation, final int firstRow) {

        final double[][] tmpArray = data;
        final int tmpRowDim = data.length;
        final int tmpColDim = myNumberOfColumns;

        final int tmpFirst = transformation.first();

        final double[] tmpWorkCopy = new double[(int) transformation.count()];

        double tmpScale;
        for (int i = firstRow; i < tmpRowDim; i++) {
            tmpScale = ZERO;
            for (int j = tmpFirst; j < tmpColDim; j++) {
                tmpScale += tmpWorkCopy[j] * tmpArray[i][j];
            }
            double tmpVal, tmpVal2 = PrimitiveMath.ZERO;
            final int tmpSize = (int) transformation.count();
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

    public void transformRight(final Rotation<Double> transformation) {

        final Rotation.Primitive tmpTransf = RawStore.cast(transformation);

        final int tmpLow = tmpTransf.low;
        final int tmpHigh = tmpTransf.high;

        if (tmpLow != tmpHigh) {
            if (!Double.isNaN(tmpTransf.cos) && !Double.isNaN(tmpTransf.sin)) {

                final double[][] tmpArray = data;
                double tmpOldLow;
                double tmpOldHigh;

                for (int i = 0; i < tmpArray.length; i++) {

                    tmpOldLow = tmpArray[i][tmpLow];
                    tmpOldHigh = tmpArray[i][tmpHigh];

                    tmpArray[i][tmpLow] = (tmpTransf.cos * tmpOldLow) - (tmpTransf.sin * tmpOldHigh);
                    tmpArray[i][tmpHigh] = (tmpTransf.cos * tmpOldHigh) + (tmpTransf.sin * tmpOldLow);
                }
            } else {
                this.exchangeColumns(tmpLow, tmpHigh);
            }
        } else {
            if (!Double.isNaN(tmpTransf.cos)) {
                this.modifyColumn(0, tmpHigh, MULTIPLY.second(tmpTransf.cos));
            } else if (!Double.isNaN(tmpTransf.sin)) {
                this.modifyColumn(0, tmpHigh, DIVIDE.second(tmpTransf.sin));
            } else {
                this.modifyColumn(0, tmpHigh, NEGATE);
            }
        }
    }

    /**
     * RawStore transpose.
     *
     * @return A'
     */
    public RawStore transpose() {
        final RawStore retVal = new RawStore(myNumberOfColumns, data.length);
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < myNumberOfColumns; j++) {
                retVal.data[j][i] = data[i][j];
            }
        }
        return retVal;
    }

    public void visitAll(final VoidFunction<Double> visitor) {
        ArrayUtils.visitAll(data, visitor);
    }

    public void visitColumn(final long row, final long column, final VoidFunction<Double> visitor) {
        ArrayUtils.visitColumn(data, (int) row, (int) column, visitor);
    }

    public void visitDiagonal(final long row, final long column, final VoidFunction<Double> visitor) {
        ArrayUtils.visitDiagonal(data, (int) row, (int) column, visitor);
    }

    public void visitRange(final long first, final long limit, final VoidFunction<Double> visitor) {
        ArrayUtils.visitRange(data, (int) first, (int) limit, visitor);
    }

    public void visitRow(final long row, final long column, final VoidFunction<Double> visitor) {
        ArrayUtils.visitRow(data, (int) row, (int) column, visitor);
    }

}
