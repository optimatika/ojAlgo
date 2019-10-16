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

import java.io.BufferedReader;
import java.io.StreamTokenizer;
import java.util.AbstractList;
import java.util.List;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.DenseArray;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.array.operation.COPY;
import org.ojalgo.array.operation.FillMatchingDual;
import org.ojalgo.array.operation.ModifyAll;
import org.ojalgo.array.operation.MultiplyBoth;
import org.ojalgo.array.operation.SWAP;
import org.ojalgo.array.operation.SubstituteBackwards;
import org.ojalgo.array.operation.SubstituteForwards;
import org.ojalgo.array.operation.VisitAll;
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
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.context.NumberContext;

/**
 * Uses double[][] internally.
 *
 * @author apete
 */
public final class RawStore extends Object implements PhysicalStore<Double> {

    public static final PhysicalStore.Factory<Double, RawStore> FACTORY = new PhysicalStore.Factory<Double, RawStore>() {

        public AggregatorSet<Double> aggregator() {
            return PrimitiveAggregator.getSet();
        }

        public DenseArray.Factory<Double> array() {
            return Primitive64Array.FACTORY;
        }

        public MatrixStore.Factory<Double> builder() {
            return MatrixStore.PRIMITIVE;
        }

        public RawStore columns(final Access1D<?>... source) {

            int tmpRowDim = (int) source[0].count();
            int tmpColDim = source.length;

            double[][] retVal = new double[tmpRowDim][tmpColDim];

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

            int tmpRowDim = source[0].length;
            int tmpColDim = source.length;

            double[][] retVal = new double[tmpRowDim][tmpColDim];

            double[] tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    retVal[i][j] = tmpColumn[i];
                }
            }

            return new RawStore(retVal);
        }

        public RawStore columns(final List<? extends Comparable<?>>... source) {

            int tmpRowDim = source[0].size();
            int tmpColDim = source.length;

            double[][] retVal = new double[tmpRowDim][tmpColDim];

            List<? extends Comparable<?>> tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    retVal[i][j] = Scalar.doubleValue(tmpColumn.get(i));
                }
            }

            return new RawStore(retVal);
        }

        public RawStore columns(final Comparable<?>[]... source) {

            int tmpRowDim = source[0].length;
            int tmpColDim = source.length;

            double[][] retVal = new double[tmpRowDim][tmpColDim];

            Comparable<?>[] tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    retVal[i][j] = Scalar.doubleValue(tmpColumn[i]);
                }
            }

            return new RawStore(retVal);
        }

        public RawStore conjugate(final Access2D<?> source) {
            return this.transpose(source);
        }

        public RawStore copy(final Access2D<?> source) {

            int numbRows = Math.toIntExact(source.countRows());
            int numbCols = Math.toIntExact(source.countColumns());

            double[][] retVal = new double[numbRows][numbCols];

            for (int i = 0; i < numbRows; i++) {
                COPY.row(source, i, retVal[i], 0, numbCols);
            }

            return new RawStore(retVal, numbRows, numbCols);
        }

        public FunctionSet<Double> function() {
            return PrimitiveFunction.getSet();
        }

        public RawStore make(final long rows, final long columns) {
            return new RawStore(Math.toIntExact(rows), Math.toIntExact(columns));
        }

        public RawStore makeEye(final long rows, final long columns) {

            RawStore retVal = this.make(rows, columns);

            retVal.fillDiagonal(0, 0, this.scalar().one().get());

            return retVal;
        }

        public RawStore makeFilled(final long rows, final long columns, final NullaryFunction<?> supplier) {

            double[][] retVal = new double[Math.toIntExact(rows)][Math.toIntExact(columns)];

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

        public RawStore rows(final Access1D<?>... source) {

            int tmpRowDim = source.length;
            int tmpColDim = (int) source[0].count();

            double[][] retVal = new double[tmpRowDim][tmpColDim];

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

            int tmpRowDim = source.length;
            int tmpColDim = source[0].length;

            double[][] retVal = new double[tmpRowDim][tmpColDim];

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

        public RawStore rows(final List<? extends Comparable<?>>... source) {

            int tmpRowDim = source.length;
            int tmpColDim = source[0].size();

            double[][] retVal = new double[tmpRowDim][tmpColDim];

            List<? extends Comparable<?>> tmpSource;
            double[] tmpDestination;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpSource = source[i];
                tmpDestination = retVal[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpDestination[j] = Scalar.doubleValue(tmpSource.get(j));
                }
            }

            return new RawStore(retVal);
        }

        public RawStore rows(final Comparable<?>[]... source) {

            int tmpRowDim = source.length;
            int tmpColDim = source[0].length;

            double[][] retVal = new double[tmpRowDim][tmpColDim];

            Comparable<?>[] tmpSource;
            double[] tmpDestination;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpSource = source[i];
                tmpDestination = retVal[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpDestination[j] = Scalar.doubleValue(tmpSource[j]);
                }
            }

            return new RawStore(retVal);
        }

        public Scalar.Factory<Double> scalar() {
            return PrimitiveScalar.FACTORY;
        }

        public RawStore transpose(final Access2D<?> source) {

            int tmpRowDim = (int) source.countColumns();
            int tmpColDim = (int) source.countRows();

            double[][] retVal = new double[tmpRowDim][tmpColDim];

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
     * @deprecated v48 Use {@link #FACTORY} or {@link #wrap(double[][])}
     */
    @Deprecated
    public static RawStore constructWithCopy(final double[][] A) {
        int m = A.length;
        int n = A[0].length;
        RawStore X = new RawStore(m, n);
        double[][] C = X.data;
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
     * @deprecated v48 Use {@link #FACTORY} or {@link #wrap(double[][])}
     */
    @Deprecated
    public static RawStore random(final int m, final int n) {
        RawStore A = new RawStore(m, n);
        double[][] X = A.data;
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
     * @deprecated v48 Use {@link #FACTORY} or {@link #wrap(double[][])}
     */
    @Deprecated
    public static RawStore read(final BufferedReader input) throws java.io.IOException {
        StreamTokenizer tokenizer = new StreamTokenizer(input);

        // Although StreamTokenizer will parse numbers, it doesn't recognize
        // scientific notation (E or D); however, Double.valueOf does.
        // The strategy here is to disable StreamTokenizer's number parsing.
        // We'll only get whitespace delimited words, EOL's and EOF's.
        // These words should all be numbers, for Double.valueOf to parse.

        tokenizer.resetSyntax();
        tokenizer.wordChars(0, 255);
        tokenizer.whitespaceChars(0, ' ');
        tokenizer.eolIsSignificant(true);
        java.util.Vector<Double> vD = new java.util.Vector<>();

        // Ignore initial empty lines
        while (tokenizer.nextToken() == StreamTokenizer.TT_EOL) {

        }
        if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
            throw new java.io.IOException("Unexpected EOF on matrix read.");
        }
        do {
            vD.addElement(Double.valueOf(tokenizer.sval)); // Read & store 1st row.
        } while (tokenizer.nextToken() == StreamTokenizer.TT_WORD);

        int n = vD.size(); // Now we've got the number of columns!
        double row[] = new double[n];
        for (int j = 0; j < n; j++) {
            row[j] = vD.elementAt(j).doubleValue();
        }
        java.util.Vector<double[]> v = new java.util.Vector<>();
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
        int m = v.size(); // Now we've got the number of rows.
        double[][] A = new double[m][];
        v.copyInto(A); // copy the rows out of the vector
        return RawStore.wrap(A);
    }

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

        RawStore retVal = null;

        if (elements instanceof RawStore) {
            retVal = ((RawStore) elements);
        } else {
            retVal = new RawStore(elements.toRawCopy1D(), structure);
        }

        return retVal;
    }

    private static RawStore convert(final Access2D<?> elements) {

        RawStore retVal = null;

        if (elements instanceof RawStore) {
            retVal = ((RawStore) elements);
        } else {
            retVal = new RawStore(elements.toRawCopy2D(), (int) elements.countRows(), (int) elements.countColumns());
        }

        return retVal;
    }

    private static double[][] extract(final Access1D<?> elements, final int structure) {

        double[][] retVal = null;

        if (elements instanceof RawStore) {

            retVal = ((RawStore) elements).data;

        } else if (elements instanceof Access2D) {

            retVal = ((Access2D<?>) elements).toRawCopy2D();

        } else {

            int tmpNumberOfColumns = (int) (structure != 0 ? (elements.count() / structure) : 0);

            if ((structure * tmpNumberOfColumns) != elements.count()) {
                throw new IllegalArgumentException("Array length must be a multiple of structure.");
            }

            retVal = new double[structure][];

            double[] tmpRow;
            for (int i = 0; i < structure; i++) {
                tmpRow = retVal[i] = new double[tmpNumberOfColumns];
                for (int j = 0; j < tmpNumberOfColumns; j++) {
                    tmpRow[j] = elements.doubleValue(Structure2D.index(structure, i, j));
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
        } else {
            return new Rotation.Primitive(aTransf);
        }
    }

    public final double[][] data;

    private final int myNumberOfColumns;

    /**
     * @deprecated v48 Use {@link #FACTORY} or {@link #wrap(double[][])}
     */
    @Deprecated
    public RawStore(final Access2D<?> template) {

        super();

        RawStore tmpConverted = RawStore.convert(template);

        data = tmpConverted.data;

        myNumberOfColumns = Math.toIntExact(template.countColumns());
    }

    /**
     * Construct a matrix from a one-dimensional packed array
     *
     * @param elements One-dimensional array of doubles, packed by columns (ala Fortran).
     * @param structure Number of rows.
     * @exception IllegalArgumentException Array length must be a multiple of m.
     * @deprecated v48 Use {@link #FACTORY} or {@link #wrap(double[][])}
     */
    @Deprecated
    public RawStore(final double elements[], final int structure) {

        myNumberOfColumns = (structure != 0 ? elements.length / structure : 0);

        if ((structure * myNumberOfColumns) != elements.length) {
            throw new IllegalArgumentException("Array length must be a multiple of structure.");
        }

        data = new double[structure][myNumberOfColumns];

        for (int i = 0; i < structure; i++) {
            for (int j = 0; j < myNumberOfColumns; j++) {
                data[i][j] = elements[Structure2D.index(structure, i, j)];
            }
        }

    }

    /**
     * Construct a matrix from a 2-D array.
     *
     * @param A Two-dimensional array of doubles.
     * @exception IllegalArgumentException All rows must have the same length
     * @see #constructWithCopy
     * @deprecated v48 Use {@link #FACTORY} or {@link #wrap(double[][])}
     */
    @Deprecated
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
     * @deprecated v48 Use {@link #FACTORY} or {@link #wrap(double[][])}
     */
    @Deprecated
    public RawStore(final double[][] A, final int m, final int n) {

        data = A;

        myNumberOfColumns = n;
    }

    /**
     * Construct an m-by-n matrix of zeros.
     *
     * @param m Number of rows.
     * @param n Number of colums.
     * @deprecated v48 Use {@link #FACTORY} or {@link #wrap(double[][])}
     */
    @Deprecated
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
     * @deprecated v48 Use {@link #FACTORY} or {@link #wrap(double[][])}
     */
    @Deprecated
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

    public void accept(final Access2D<?> supplied) {

        int numbRows = MissingMath.toMinIntExact(data.length, supplied.countRows());
        int numbCols = MissingMath.toMinIntExact(myNumberOfColumns, supplied.countColumns());

        for (int i = 0; i < numbRows; i++) {
            COPY.row(supplied, i, data[i], 0, numbCols);
        }
    }

    public void add(final long row, final long col, final double addend) {
        data[Math.toIntExact(row)][Math.toIntExact(col)] += addend;
    }

    public void add(final long row, final long col, final Comparable<?> addend) {
        data[Math.toIntExact(row)][Math.toIntExact(col)] += Scalar.doubleValue(addend);
    }

    public Double aggregateAll(final Aggregator aggregator) {

        AggregatorFunction<Double> tmpVisitor = aggregator.getFunction(PrimitiveAggregator.getSet());

        this.visitAll(tmpVisitor);

        return tmpVisitor.get();
    }

    public List<Double> asList() {

        int tmpStructure = data.length;

        return new AbstractList<Double>() {

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

    public MatrixStore<Double> conjugate() {
        return this.transpose();
    }

    /**
     * Make a deep copy of a matrix
     */
    public RawStore copy() {
        return new RawStore(this.toRawCopy2D(), myNumberOfColumns);
    }

    public long count() {
        return Structure2D.count(data.length, myNumberOfColumns);
    }

    public long countColumns() {
        return myNumberOfColumns;
    }

    public long countRows() {
        return data.length;
    }

    public double doubleValue(final long row, final long col) {
        return data[Math.toIntExact(row)][Math.toIntExact(col)];
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof Access2D<?>) {
            return Access2D.equals(this, (Access2D<?>) other, NumberContext.getGeneral(6));
        } else {
            return super.equals(other);
        }
    }

    public void exchangeColumns(final long colA, final long colB) {
        SWAP.exchangeColumns(data, Math.toIntExact(colA), Math.toIntExact(colB));
    }

    public void exchangeRows(final long rowA, final long rowB) {
        SWAP.exchangeRows(data, Math.toIntExact(rowA), Math.toIntExact(rowB));
    }

    public void fillAll(final Double value) {
        FillMatchingDual.fillAll(data, value);
    }

    public void fillAll(final NullaryFunction<Double> supplier) {
        FillMatchingDual.fillAll(data, supplier);
    }

    public void fillByMultiplying(final Access1D<Double> left, final Access1D<Double> right) {

        int complexity = Math.toIntExact(left.count() / this.countRows());
        if (complexity != Math.toIntExact(right.count() / this.countColumns())) {
            ProgrammingError.throwForMultiplicationNotPossible();
        }

        double[][] rawLeft = RawStore.extract(left, data.length);
        double[][] rawRight = RawStore.extract(right, complexity);

        RawStore.multiply(data, rawLeft, rawRight);
    }

    public void fillColumn(final long row, final long col, final Double value) {
        FillMatchingDual.fillColumn(data, Math.toIntExact(row), Math.toIntExact(col), value);
    }

    public void fillColumn(final long row, final long col, final NullaryFunction<Double> supplier) {
        FillMatchingDual.fillColumn(data, Math.toIntExact(row), Math.toIntExact(col), supplier);
    }

    public void fillDiagonal(final long row, final long col, final Double value) {
        FillMatchingDual.fillDiagonal(data, Math.toIntExact(row), Math.toIntExact(col), value);
    }

    public void fillDiagonal(final long row, final long col, final NullaryFunction<Double> supplier) {
        FillMatchingDual.fillDiagonal(data, Math.toIntExact(row), Math.toIntExact(col), supplier);
    }

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

    public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
        this.set(row, col, values.doubleValue(valueIndex));
    }

    public void fillOne(final long row, final long col, final Double value) {
        data[Math.toIntExact(row)][Math.toIntExact(col)] = value;
    }

    public void fillOne(final long row, final long col, final NullaryFunction<Double> supplier) {
        data[Math.toIntExact(row)][Math.toIntExact(col)] = supplier.doubleValue();
    }

    public void fillRange(final long first, final long limit, final Double value) {
        FillMatchingDual.fillRange(data, (int) first, (int) limit, value);
    }

    public void fillRange(final long first, final long limit, final NullaryFunction<Double> supplier) {
        FillMatchingDual.fillRange(data, (int) first, (int) limit, supplier);
    }

    public void fillRow(final long row, final long col, final Double value) {
        FillMatchingDual.fillRow(data, Math.toIntExact(row), Math.toIntExact(col), value);
    }

    public void fillRow(final long row, final long col, final NullaryFunction<Double> supplier) {
        FillMatchingDual.fillRow(data, Math.toIntExact(row), Math.toIntExact(col), supplier);
    }

    public MatrixStore<Double> get() {
        return this;
    }

    public Double get(final long row, final long col) {
        return data[Math.toIntExact(row)][Math.toIntExact(col)];
    }

    @Override
    public int hashCode() {
        return Access1D.hashCode(this);
    }

    public long indexOfLargest() {

        int tmpRowDim = data.length;

        int retVal = 0;
        double tmpLargest = ZERO;
        double tmpValue;
        double[] tmpRow;

        for (int i = 0; i < tmpRowDim; i++) {
            tmpRow = data[i];

            for (int j = 0; j < myNumberOfColumns; j++) {
                tmpValue = PrimitiveMath.ABS.invoke(tmpRow[j]);
                if (tmpValue > tmpLargest) {
                    tmpLargest = tmpValue;
                    retVal = Structure2D.index(tmpRowDim, i, j);
                }
            }
        }

        return retVal;
    }

    public long indexOfLargestInColumn(final long row, final long col) {

        int tmpRowDim = data.length;

        int retVal = Math.toIntExact(row);
        double tmpLargest = ZERO;
        double tmpValue;

        for (int i = Math.toIntExact(row); i < tmpRowDim; i++) {
            tmpValue = PrimitiveMath.ABS.invoke(data[i][Math.toIntExact(col)]);
            if (tmpValue > tmpLargest) {
                tmpLargest = tmpValue;
                retVal = i;
            }

        }

        return retVal;
    }

    public long indexOfLargestInRange(final long first, final long limit) {

        int tmpRowDim = data.length;

        int retVal = 0;
        double tmpLargest = ZERO;
        double tmpValue;

        for (int index = 0; index < this.count(); index++) {
            int i = Structure2D.row(index, tmpRowDim);
            int j = Structure2D.column(index, tmpRowDim);
            tmpValue = PrimitiveMath.ABS.invoke(data[i][j]);
            if (tmpValue > tmpLargest) {
                tmpLargest = tmpValue;
                retVal = index;
            }
        }

        return retVal;
    }

    public long indexOfLargestInRow(final long row, final long col) {

        int retVal = Math.toIntExact(col);
        double tmpLargest = ZERO;
        double tmpValue;
        double[] tmpRow = data[Math.toIntExact(row)];

        for (int j = Math.toIntExact(col); j < myNumberOfColumns; j++) {
            tmpValue = PrimitiveMath.ABS.invoke(tmpRow[j]);
            if (tmpValue > tmpLargest) {
                tmpLargest = tmpValue;
                retVal = j;
            }
        }

        return retVal;
    }

    public long indexOfLargestOnDiagonal(final long first) {

        int tmpRowDim = data.length;

        int retVal = (int) (first);
        double tmpLargest = ZERO;
        double tmpValue;

        for (int i = (int) first, j = (int) first; (i < tmpRowDim) && (j < myNumberOfColumns); i++, j++) {
            tmpValue = PrimitiveMath.ABS.invoke(data[i][j]);
            if (tmpValue > tmpLargest) {
                tmpLargest = tmpValue;
                retVal = i;
            }
        }

        return retVal;
    }

    public boolean isAbsolute(final long index) {
        int tmpRowDim = data.length;
        return PrimitiveScalar.isAbsolute(this.get(Structure2D.row(index, tmpRowDim), Structure2D.column(index, tmpRowDim)));
    }

    public boolean isAbsolute(final long row, final long col) {
        return PrimitiveScalar.isAbsolute(this.get(Math.toIntExact(row), Math.toIntExact(col)));
    }

    public boolean isSmall(final long index, final double comparedTo) {
        int tmpRowDim = data.length;
        return PrimitiveScalar.isSmall(comparedTo, this.get(Structure2D.row(index, tmpRowDim), Structure2D.column(index, tmpRowDim)));
    }

    public boolean isSmall(final long row, final long col, final double comparedTo) {
        return PrimitiveScalar.isSmall(comparedTo, this.doubleValue(row, col));
    }

    public void modifyAll(final UnaryFunction<Double> modifier) {
        ModifyAll.modifyAll(data, modifier);
    }

    public void modifyColumn(final long row, final long col, final UnaryFunction<Double> modifier) {
        ModifyAll.modifyColumn(data, Math.toIntExact(row), Math.toIntExact(col), modifier);
    }

    public void modifyDiagonal(final long row, final long col, final UnaryFunction<Double> modifier) {

        long tmpCount = Math.min(data.length - row, myNumberOfColumns - col);

        int tmpFirst = (int) (row + (col * data.length));
        int tmpLimit = (int) (row + tmpCount + ((col + tmpCount) * data.length));
        int tmpStep = 1 + data.length;

        for (int ij = tmpFirst; ij < tmpLimit; ij += tmpStep) {
            this.set(ij, modifier.invoke(this.doubleValue(ij)));
        }

    }

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

    public void modifyOne(final long row, final long col, final UnaryFunction<Double> modifier) {

        double tmpValue = this.doubleValue(row, col);

        tmpValue = modifier.invoke(tmpValue);

        this.set(row, col, tmpValue);
    }

    public void modifyRange(final long first, final long limit, final UnaryFunction<Double> modifier) {
        for (long index = first; index < limit; index++) {
            this.set(index, modifier.invoke(this.doubleValue(index)));
        }
    }

    public void modifyRow(final long row, final long col, final UnaryFunction<Double> modifier) {
        ModifyAll.modifyRow(data, Math.toIntExact(row), Math.toIntExact(col), modifier);
    }

    public RawStore multiply(final MatrixStore<Double> right) {

        int tmpRowDim = data.length;
        int tmpComplexity = myNumberOfColumns;
        int tmpColDim = (int) (right.count() / tmpComplexity);

        RawStore retVal = new RawStore(tmpRowDim, tmpColDim);

        double[][] tmpRight = RawStore.extract(right, tmpComplexity);

        RawStore.multiply(retVal.data, data, tmpRight);

        return retVal;
    }

    public Double multiplyBoth(final Access1D<Double> leftAndRight) {

        PhysicalStore<Double> tmpStep1 = FACTORY.make(1L, leftAndRight.count());
        PhysicalStore<Double> tmpStep2 = FACTORY.make(1L, 1L);

        tmpStep1.fillByMultiplying(leftAndRight, this);
        tmpStep2.fillByMultiplying(tmpStep1, leftAndRight);

        return tmpStep2.get(0L);
    }

    public PhysicalStore.Factory<Double, RawStore> physical() {
        return FACTORY;
    }

    public TransformableRegion<Double> regionByColumns(final int... columns) {
        return new TransformableRegion.ColumnsRegion<>(this, MultiplyBoth.getPrimitive(data.length, myNumberOfColumns), columns);
    }

    public TransformableRegion<Double> regionByLimits(final int rowLimit, final int columnLimit) {
        return new TransformableRegion.LimitRegion<>(this, MultiplyBoth.getPrimitive(data.length, myNumberOfColumns), rowLimit, columnLimit);
    }

    public TransformableRegion<Double> regionByOffsets(final int rowOffset, final int columnOffset) {
        return new TransformableRegion.OffsetRegion<>(this, MultiplyBoth.getPrimitive(data.length, myNumberOfColumns), rowOffset, columnOffset);
    }

    public TransformableRegion<Double> regionByRows(final int... rows) {
        return new TransformableRegion.RowsRegion<>(this, MultiplyBoth.getPrimitive(data.length, myNumberOfColumns), rows);
    }

    public TransformableRegion<Double> regionByTransposing() {
        return new TransformableRegion.TransposedRegion<>(this, MultiplyBoth.getPrimitive(data.length, myNumberOfColumns));
    }

    public void set(final long row, final long col, final double value) {
        data[Math.toIntExact(row)][Math.toIntExact(col)] = value;
    }

    public void set(final long row, final long col, final Comparable<?> value) {
        data[Math.toIntExact(row)][Math.toIntExact(col)] = Scalar.doubleValue(value);
    }

    public Access1D<Double> sliceRow(final long row) {
        return Access1D.wrap(data[Math.toIntExact(row)]);
    }

    public void substituteBackwards(final Access2D<Double> body, final boolean unitDiagonal, final boolean conjugated, final boolean hermitian) {
        SubstituteBackwards.invoke(data, body, unitDiagonal, conjugated, hermitian);
    }

    public void substituteForwards(final Access2D<Double> body, final boolean unitDiagonal, final boolean conjugated, final boolean identity) {
        SubstituteForwards.invoke(data, body, unitDiagonal, conjugated, identity);
    }

    public void supplyTo(final TransformableRegion<Double> receiver) {
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

                    tmpArray[tmpLow][j] = (tmpTransf.cos * tmpOldLow) + (tmpTransf.sin * tmpOldHigh);
                    tmpArray[tmpHigh][j] = (tmpTransf.cos * tmpOldHigh) - (tmpTransf.sin * tmpOldLow);
                }
            } else {
                this.exchangeRows(tmpLow, tmpHigh);
            }
        } else {
            if (!Double.isNaN(tmpTransf.cos)) {
                this.modifyRow(tmpLow, 0, PrimitiveMath.MULTIPLY.second(tmpTransf.cos));
            } else if (!Double.isNaN(tmpTransf.sin)) {
                this.modifyRow(tmpLow, 0, PrimitiveMath.DIVIDE.second(tmpTransf.sin));
            } else {
                this.modifyRow(tmpLow, 0, PrimitiveMath.NEGATE);
            }
        }
    }

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

                    tmpArray[i][tmpLow] = (tmpTransf.cos * tmpOldLow) - (tmpTransf.sin * tmpOldHigh);
                    tmpArray[i][tmpHigh] = (tmpTransf.cos * tmpOldHigh) + (tmpTransf.sin * tmpOldLow);
                }
            } else {
                this.exchangeColumns(tmpLow, tmpHigh);
            }
        } else {
            if (!Double.isNaN(tmpTransf.cos)) {
                this.modifyColumn(0, tmpHigh, PrimitiveMath.MULTIPLY.second(tmpTransf.cos));
            } else if (!Double.isNaN(tmpTransf.sin)) {
                this.modifyColumn(0, tmpHigh, PrimitiveMath.DIVIDE.second(tmpTransf.sin));
            } else {
                this.modifyColumn(0, tmpHigh, PrimitiveMath.NEGATE);
            }
        }
    }

    public MatrixStore<Double> transpose() {
        return new TransposedStore<>(this);
    }

    public void visitAll(final VoidFunction<Double> visitor) {
        VisitAll.visitAll(data, visitor);
    }

    public void visitColumn(final long row, final long col, final VoidFunction<Double> visitor) {
        VisitAll.visitColumn(data, Math.toIntExact(row), Math.toIntExact(col), visitor);
    }

    public void visitDiagonal(final long row, final long col, final VoidFunction<Double> visitor) {
        VisitAll.visitDiagonal(data, Math.toIntExact(row), Math.toIntExact(col), visitor);
    }

    public void visitRange(final long first, final long limit, final VoidFunction<Double> visitor) {
        VisitAll.visitRange(data, (int) first, (int) limit, visitor);
    }

    public void visitRow(final long row, final long col, final VoidFunction<Double> visitor) {
        VisitAll.visitRow(data, Math.toIntExact(row), Math.toIntExact(col), visitor);
    }

}
