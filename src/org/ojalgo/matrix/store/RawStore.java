/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
import java.util.Iterator;
import java.util.List;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.access.Iterator1D;
import org.ojalgo.array.ArrayUtils;
import org.ojalgo.array.BasicArray;
import org.ojalgo.array.PrimitiveArray;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorCollection;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.decomposition.RawCholesky;
import org.ojalgo.matrix.decomposition.RawEigenvalue;
import org.ojalgo.matrix.decomposition.RawLU;
import org.ojalgo.matrix.decomposition.RawQR;
import org.ojalgo.matrix.decomposition.RawSingularValue;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.random.RandomNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * This class adapts JAMA's RawStore to ojAlgo's {@linkplain BasicMatrix} and {@linkplain PhysicalStore} interfaces.
 *
 * @author apete
 */
public final class RawStore extends Object implements PhysicalStore<Double>, Serializable {

    public static PhysicalStore.Factory<Double, RawStore> FACTORY = new PhysicalStore.Factory<Double, RawStore>() {

        public AggregatorCollection<Double> aggregator() {
            return PrimitiveAggregator.getCollection();
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

            for (int i = 0; i < tmpRowDim; i++) {
                for (int j = 0; j < tmpColDim; j++) {
                    retVal[i][j] = source.doubleValue(i, j);
                }
            }

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

        public Householder<Double> makeHouseholder(final int length) {
            return new Householder.Primitive(length);
        }

        public RawStore makeRandom(final long rows, final long columns, final RandomNumber distribution) {

            final double[][] retVal = new double[(int) rows][(int) columns];

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    retVal[i][j] = distribution.doubleValue();
                }
            }

            return new RawStore(retVal);
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
     * Read a matrix from a stream. The format is the same the print method, so printed matrices can be read back in
     * (provided they were printed using US Locale). Elements are separated by whitespace, all the elements for each row
     * appear on a single line, the last row is followed by a blank line.
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

    private static RawStore convert(final Access2D<?> elements) {

        RawStore retVal = null;

        if (elements instanceof RawStore) {
            retVal = ((RawStore) elements);
        } else {
            retVal = new RawStore(ArrayUtils.toRawCopyOf(elements), (int) elements.countRows(), (int) elements.countColumns());
        }

        return retVal;
    }

    static Rotation.Primitive cast(final Rotation<Double> aTransf) {
        if (aTransf instanceof Rotation.Primitive) {
            return (Rotation.Primitive) aTransf;
        } else {
            return new Rotation.Primitive(aTransf);
        }
    }

    public double[][] data;

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

    public RawStore add(final Access2D<?> aMtrx) {
        final RawStore B = RawStore.convert(aMtrx);
        this.checkMatrixDimensions(B);
        final RawStore X = new RawStore(data.length, myNumberOfColumns);
        final double[][] C = X.data;
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < myNumberOfColumns; j++) {
                C[i][j] = data[i][j] + B.data[i][j];
            }
        }
        return X;
    }

    public RawStore add(final int row, final int column, final Access2D<?> aMtrx) {

        final double[][] tmpArrayCopy = this.copyOfData();

        double[] tmpLocalRowRef;
        for (int i = 0; i < aMtrx.countRows(); i++) {
            tmpLocalRowRef = tmpArrayCopy[row + i];
            for (int j = 0; j < aMtrx.countColumns(); j++) {
                tmpLocalRowRef[column + j] = aMtrx.doubleValue(i, j);
            }
        }

        return new RawStore(tmpArrayCopy);
    }

    public RawStore add(final int row, final int column, final Number aNmbr) {

        final double[][] tmpArrayCopy = this.copyOfData();
        tmpArrayCopy[row][column] += aNmbr.doubleValue();

        return new RawStore(tmpArrayCopy);
    }

    public MatrixStore<Double> add(final MatrixStore<Double> addend) {
        final RawStore B = RawStore.convert(addend);
        this.checkMatrixDimensions(B);
        final RawStore X = new RawStore(data.length, myNumberOfColumns);
        final double[][] C = X.data;
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < myNumberOfColumns; j++) {
                C[i][j] = data[i][j] + B.data[i][j];
            }
        }
        return X;
    }

    public Double aggregateAll(final Aggregator aggregator) {

        final AggregatorFunction<Double> tmpVisitor = aggregator.getPrimitiveFunction();

        this.visitAll(tmpVisitor);

        return tmpVisitor.doubleValue();
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

        final int tmpRowDim = (int) this.countRows();

        for (int i = firstRow; i < tmpRowDim; i++) {
            tmpArray[i][columnY] += tmpValA * tmpArray[i][columnX];
        }
    }

    /**
     * Clone the RawStore object.
     */
    @Override
    public RawStore clone() {
        return this.copy();
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

    public long count() {
        return data.length * myNumberOfColumns;
    }

    public long countColumns() {
        return myNumberOfColumns;
    }

    public long countRows() {
        return data.length;
    }

    public double doubleValue(final long anInd) {
        return this.get(AccessUtils.row((int) anInd, (int) this.countRows()), AccessUtils.column((int) anInd, (int) this.countRows()));
    }

    public double doubleValue(final long row, final long column) {
        return this.get((int) row, (int) column);
    }

    public boolean equals(final MatrixStore<Double> other, final NumberContext context) {
        return AccessUtils.equals(this, other, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object anObject) {
        if (anObject instanceof MatrixStore) {
            return this.equals((MatrixStore<Double>) anObject, NumberContext.getGeneral(6));
        } else if (anObject instanceof BasicMatrix) {
            return AccessUtils.equals(this, (BasicMatrix) anObject, NumberContext.getGeneral(6));
        } else {
            return super.equals(anObject);
        }
    }

    public void exchangeColumns(final int aColA, final int aColB) {
        ArrayUtils.exchangeColumns(data, aColA, aColB);
    }

    public void exchangeRows(final int aRowA, final int aRowB) {
        ArrayUtils.exchangeRows(data, aRowA, aRowB);
    }

    public PhysicalStore.Factory<Double, RawStore> factory() {
        return FACTORY;
    }

    public void fillAll(final Double aNmbr) {
        ArrayUtils.fillAll(data, aNmbr);
    }

    public void fillByMultiplying(final Access1D<Double> aLeftArg, final Access1D<Double> aRightArg) {

        final RawStore tmpLeft = RawStore.convert(aLeftArg, (int) this.countRows());
        final RawStore tmpRight = RawStore.convert(aRightArg, (int) tmpLeft.countColumns());

        try {
            for (int i = 0; i <= ((int) this.countRows() - 1); i++) {
                for (int j = 0; j <= ((int) this.countColumns() - 1); j++) {
                    data[i][j] = tmpLeft.times(tmpRight).get(i - 0, j - 0);
                }
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
    }

    public void fillColumn(final long row, final long column, final Double aNmbr) {
        ArrayUtils.fillColumn(data, (int) row, (int) column, aNmbr);
    }

    public void fillConjugated(final Access2D<? extends Number> source) {
        this.fillTransposed(source);
    }

    public void fillDiagonal(final long row, final long column, final Double aNmbr) {
        ArrayUtils.fillDiagonal(data, (int) row, (int) column, aNmbr);
    }

    public void fillMatching(final Access1D<? extends Number> source) {

        final double[][] tmpDelegateArray = data;

        final int tmpRowDim = (int) this.countRows();

        for (int i = 0; i < tmpRowDim; i++) {
            for (int j = 0; j < (int) this.countColumns(); j++) {
                tmpDelegateArray[i][j] = source.doubleValue(i + (j * tmpRowDim));
            }
        }
    }

    public void fillMatching(final Access1D<Double> leftArg, final BinaryFunction<Double> function, final Access1D<Double> rightArg) {
        if (leftArg == this) {
            if (function == ADD) {
                final RawStore B = RawStore.convert(rightArg, (int) this.countRows());
                this.checkMatrixDimensions(B);
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = data[i][j] + B.data[i][j];
                    }
                }
                final RawStore plusEquals = this;
            } else if (function == DIVIDE) {
                final RawStore B = RawStore.convert(rightArg, (int) this.countRows());
                this.checkMatrixDimensions(B);
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = data[i][j] / B.data[i][j];
                    }
                }
                final RawStore arrayRightDivideEquals = this;
            } else if (function == MULTIPLY) {
                final RawStore B = RawStore.convert(rightArg, (int) this.countRows());
                this.checkMatrixDimensions(B);
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = data[i][j] * B.data[i][j];
                    }
                }
                final RawStore arrayTimesEquals = this;
            } else if (function == SUBTRACT) {
                final RawStore B = RawStore.convert(rightArg, (int) this.countRows());
                this.checkMatrixDimensions(B);
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = data[i][j] - B.data[i][j];
                    }
                }
                final RawStore minusEquals = this;
            } else {
                ArrayUtils.fillMatching(data, data, function, RawStore.convert(rightArg, (int) this.countRows()).data);
            }
        } else if (rightArg == this) {
            if (function == ADD) {
                final RawStore B = RawStore.convert(leftArg, (int) this.countRows());
                this.checkMatrixDimensions(B);
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = data[i][j] + B.data[i][j];
                    }
                }
                final RawStore plusEquals = this;
            } else if (function == DIVIDE) {
                final RawStore B = RawStore.convert(leftArg, (int) this.countRows());
                this.checkMatrixDimensions(B);
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = B.data[i][j] / data[i][j];
                    }
                }
                final RawStore arrayLeftDivideEquals = this;
            } else if (function == MULTIPLY) {
                final RawStore B = RawStore.convert(leftArg, (int) this.countRows());
                this.checkMatrixDimensions(B);
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < myNumberOfColumns; j++) {
                        data[i][j] = data[i][j] * B.data[i][j];
                    }
                }
                final RawStore arrayTimesEquals = this;
            } else if (function == SUBTRACT) {
                ArrayUtils.fillMatching(data, RawStore.convert(leftArg, (int) this.countRows()).data, function, data);
            } else {
                ArrayUtils.fillMatching(data, RawStore.convert(leftArg, (int) this.countRows()).data, function, data);
            }
        } else {
            ArrayUtils.fillMatching(data, RawStore.convert(leftArg, (int) this.countRows()).data, function,
                    RawStore.convert(rightArg, (int) this.countRows()).data);
        }
    }

    public void fillMatching(final Access1D<Double> aLeftArg, final BinaryFunction<Double> function, final Double aRightArg) {
        ArrayUtils.fillMatching(data, RawStore.convert(aLeftArg, (int) this.countRows()).data, function, aRightArg);
    }

    public void fillMatching(final Double aLeftArg, final BinaryFunction<Double> function, final Access1D<Double> aRightArg) {
        ArrayUtils.fillMatching(data, aLeftArg, function, RawStore.convert(aRightArg, (int) this.countRows()).data);
    }

    public void fillRange(final long first, final long limit, final Double value) {
        ArrayUtils.fillRange(data, (int) first, (int) limit, value);
    }

    public void fillRow(final long row, final long column, final Double aNmbr) {
        ArrayUtils.fillRow(data, (int) row, (int) column, aNmbr);
    }

    public void fillTransposed(final Access2D<? extends Number> source) {

        final double[][] tmpDelegateArray = data;

        final int tmpRowDim = (int) this.countRows();

        for (int i = 0; i < tmpRowDim; i++) {
            for (int j = 0; j < (int) this.countColumns(); j++) {
                tmpDelegateArray[i][j] = source.doubleValue(j, i);
            }
        }
    }

    public Double get(final long index) {
        return data[AccessUtils.row(index, data.length)][AccessUtils.column(index, data.length)];
    }

    public Double get(final long row, final long column) {
        return data[(int) row][(int) column];
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

    /**
     * Make a one-dimensional row packed copy of the internal array.
     *
     * @return RawStore elements packed in a one-dimensional array by rows.
     */
    public double[] getRowPackedCopy() {
        final double[] vals = new double[data.length * myNumberOfColumns];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < myNumberOfColumns; j++) {
                vals[(i * myNumberOfColumns) + j] = data[i][j];
            }
        }
        return vals;
    }

    @Override
    public int hashCode() {
        return MatrixUtils.hashCode(this);
    }

    public boolean isAbsolute(final long index) {
        final int tmpRowDim = (int) this.countRows();
        return PrimitiveScalar.isAbsolute(this.get(AccessUtils.row(index, tmpRowDim), AccessUtils.column(index, tmpRowDim)));
    }

    public boolean isAbsolute(final long row, final long column) {
        return PrimitiveScalar.isAbsolute(this.get((int) row, (int) column));
    }

    public boolean isLowerLeftShaded() {
        return false;
    }

    public boolean isSmall(final long index, final double comparedTo) {
        final int tmpRowDim = (int) this.countRows();
        return PrimitiveScalar.isSmall(comparedTo, this.get(AccessUtils.row(index, tmpRowDim), AccessUtils.column(index, tmpRowDim)));
    }

    public boolean isSmall(final long row, final long column, final double comparedTo) {
        return PrimitiveScalar.isSmall(comparedTo, this.doubleValue(row, column));
    }

    public boolean isUpperRightShaded() {
        return false;
    }

    public Iterator<Double> iterator() {
        return new Iterator1D<Double>(this);
    }

    public void maxpy(final Double aSclrA, final MatrixStore<Double> aMtrxX) {

        final double tmpValA = aSclrA;
        final double[][] tmpArray = data;

        final int tmpRowDim = (int) this.countRows();
        final int tmpColDim = (int) this.countColumns();

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

        final long tmpCount = Math.min((int) this.countRows() - row, (int) this.countColumns() - column);

        final int tmpFirst = (int) (row + (column * (int) this.countRows()));
        final int tmpLimit = (int) (row + tmpCount + ((column + tmpCount) * (int) this.countRows()));
        final int tmpStep = 1 + (int) this.countRows();

        for (int ij = tmpFirst; ij < tmpLimit; ij += tmpStep) {
            this.set(ij, function.invoke(this.doubleValue(ij)));
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

    public RawStore multiplyLeft(final Access1D<Double> leftMtrx) {
        return RawStore.convert(leftMtrx, (int) (leftMtrx.count() / (int) this.countRows())).times(this);
    }

    public RawStore multiplyRight(final Access1D<Double> rightMtrx) {
        final RawStore tmpConvert = RawStore.convert(rightMtrx, (int) this.countColumns());
        if (tmpConvert.data.length != myNumberOfColumns) {
            throw new IllegalArgumentException("RawStore inner dimensions must agree.");
        }
        final RawStore X = new RawStore(data.length, tmpConvert.myNumberOfColumns);
        final double[][] C = X.data;
        final double[] Bcolj = new double[myNumberOfColumns];
        for (int j = 0; j < tmpConvert.myNumberOfColumns; j++) {
            for (int k = 0; k < myNumberOfColumns; k++) {
                Bcolj[k] = tmpConvert.data[k][j];
            }
            for (int i = 0; i < data.length; i++) {
                final double[] Arowi = data[i];
                double s = 0;
                for (int k = 0; k < myNumberOfColumns; k++) {
                    s += Arowi[k] * Bcolj[k];
                }
                C[i][j] = s;
            }
        }

        return X;
    }

    public RawStore negate() {
        final RawStore X = new RawStore(data.length, myNumberOfColumns);
        final double[][] C = X.data;
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < myNumberOfColumns; j++) {
                C[i][j] = -data[i][j];
            }
        }
        return X;
    }

    /**
     * One norm
     *
     * @return maximum column sum.
     */
    public double norm1() {
        double f = 0;
        for (int j = 0; j < myNumberOfColumns; j++) {
            double s = 0;
            for (int i = 0; i < data.length; i++) {
                s += Math.abs(data[i][j]);
            }
            f = Math.max(f, s);
        }
        return f;
    }

    public void raxpy(final Double scalarA, final int rowX, final int rowY, final int firstColumn) {

        final double tmpValA = scalarA.doubleValue();
        final double[][] tmpArray = data;

        final int tmpColDim = (int) this.countColumns();

        for (int j = firstColumn; j < tmpColDim; j++) {
            tmpArray[rowY][j] += tmpValA * tmpArray[rowX][j];

        }
    }

    public RawStore scale(final Double scalar) {
        final RawStore X = new RawStore(data.length, myNumberOfColumns);
        final double[][] C = X.data;
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < myNumberOfColumns; j++) {
                C[i][j] = scalar.doubleValue() * data[i][j];
            }
        }
        return X;
    }

    public void set(final long index, final double value) {
        data[AccessUtils.row(index, (int) this.countRows())][AccessUtils.column(index, (int) this.countRows())] = value;
    }

    public void set(final long row, final long column, final double aNmbr) {
        data[(int) row][(int) column] = aNmbr;
    }

    public void set(final long row, final long column, final Number aNmbr) {
        data[(int) row][(int) column] = aNmbr.doubleValue();
    }

    public void set(final long index, final Number value) {
        data[AccessUtils.row(index, (int) this.countRows())][AccessUtils.column(index, (int) this.countRows())] = value.doubleValue();
    }

    public MatrixStore<Double> subtract(final MatrixStore<Double> subtrahend) {
        return this.add(subtrahend.negate());
    }

    /**
     * Linear algebraic matrix multiplication, A * B
     *
     * @param B another matrix
     * @return RawStore product, A * B
     * @exception IllegalArgumentException RawStore inner dimensions must agree.
     */
    public RawStore times(final RawStore B) {
        if (B.data.length != myNumberOfColumns) {
            throw new IllegalArgumentException("RawStore inner dimensions must agree.");
        }
        final RawStore X = new RawStore(data.length, B.myNumberOfColumns);
        final double[][] C = X.data;
        final double[] Bcolj = new double[myNumberOfColumns];
        for (int j = 0; j < B.myNumberOfColumns; j++) {
            for (int k = 0; k < myNumberOfColumns; k++) {
                Bcolj[k] = B.data[k][j];
            }
            for (int i = 0; i < data.length; i++) {
                final double[] Arowi = data[i];
                double s = 0;
                for (int k = 0; k < myNumberOfColumns; k++) {
                    s += Arowi[k] * Bcolj[k];
                }
                C[i][j] = s;
            }
        }
        return X;
    }

    public PrimitiveScalar toScalar(final long row, final long column) {
        return new PrimitiveScalar(this.get((int) row, (int) column));
    }

    @Override
    public String toString() {
        return MatrixUtils.toString(this);
    }

    public void transformLeft(final Householder<Double> transformation, final int firstColumn) {

        final double[][] tmpArray = data;
        final int tmpRowDim = (int) this.countRows();
        final int tmpColDim = (int) this.countColumns();

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
        final int tmpRowDim = (int) this.countRows();
        final int tmpColDim = (int) this.countColumns();

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
        final RawStore X = new RawStore(myNumberOfColumns, data.length);
        final double[][] C = X.data;
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < myNumberOfColumns; j++) {
                C[j][i] = data[i][j];
            }
        }
        return X;
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

    /** Check if size(A) == size(B) **/
    private void checkMatrixDimensions(final RawStore B) {
        if ((B.data.length != data.length) || (B.myNumberOfColumns != myNumberOfColumns)) {
            throw new IllegalArgumentException("RawStore dimensions must agree.");
        }
    }

    RawCholesky getCholeskyDecomposition() {
        final RawCholesky retVal = new RawCholesky();
        retVal.compute(this);
        return retVal;
    }

    RawEigenvalue getEigenvalueDecomposition() {
        final RawEigenvalue retVal = MatrixUtils.isHermitian(this) ? new RawEigenvalue.Symmetric() : new RawEigenvalue.Nonsymmetric();
        retVal.compute(this);
        return retVal;
    }

    RawLU getLUDecomposition() {
        final RawLU retVal = new RawLU();
        retVal.compute(this);
        return retVal;
    }

    RawQR getQRDecomposition() {
        final RawQR retVal = new RawQR();
        retVal.compute(this);
        return retVal;
    }

    RawSingularValue getSingularValueDecomposition() {
        final RawSingularValue retVal = new RawSingularValue();
        retVal.compute(this);
        return retVal;
    }

}
