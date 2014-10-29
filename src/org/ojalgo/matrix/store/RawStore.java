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
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StreamTokenizer;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.access.Iterator1D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.Array2D;
import org.ojalgo.array.ArrayUtils;
import org.ojalgo.array.BasicArray;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
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
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * This class adapts JAMA's RawStore to ojAlgo's {@linkplain BasicMatrix} and {@linkplain PhysicalStore} interfaces.
 *
 * @author apete
 */
public final class RawStore extends Object implements PhysicalStore<Double>, Serializable {

    public static final PhysicalStore.Factory<Double, RawStore> FACTORY = new RawFactory();

    private static final long serialVersionUID = 1;

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
     * Generate identity matrix
     *
     * @param m Number of rows.
     * @param n Number of colums.
     * @return An m-by-n matrix with ones on the diagonal and zeros elsewhere.
     */
    public static RawStore identity(final int m, final int n) {
        final RawStore A = new RawStore(m, n);
        final double[][] X = A.data;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                X[i][j] = (i == j ? 1.0 : 0.0);
            }
        }
        return A;
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

    public final double[][] data;

    private final int myRowDim;
    private final int myColDim;

    public RawStore(final Access2D<?> template) {

        super();

        final RawStore tmpConverted = RawStore.convert(template);

        data = tmpConverted.data;
        myRowDim = data.length;
        myColDim = (int) template.countColumns();
    }

    /**
     * Construct a matrix from a one-dimensional packed array
     *
     * @param vals One-dimensional array of doubles, packed by columns (ala Fortran).
     * @param m Number of rows.
     * @exception IllegalArgumentException Array length must be a multiple of m.
     */
    public RawStore(final double vals[], final int m) {
        myRowDim = m;
        myColDim = (m != 0 ? vals.length / m : 0);
        if ((m * myColDim) != vals.length) {
            throw new IllegalArgumentException("Array length must be a multiple of m.");
        }
        data = new double[m][myColDim];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < myColDim; j++) {
                data[i][j] = vals[i + (j * m)];
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
        myRowDim = A.length;
        myColDim = A[0].length;
        for (int i = 0; i < myRowDim; i++) {
            if (A[i].length != myColDim) {
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
        myRowDim = m;
        myColDim = n;

    }

    /**
     * Construct an m-by-n matrix of zeros.
     *
     * @param m Number of rows.
     * @param n Number of colums.
     */
    public RawStore(final int m, final int n) {
        myRowDim = m;
        myColDim = n;
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
        myRowDim = m;
        myColDim = n;
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

        data = null;
        myRowDim = data.length;
        myColDim = data[0].length;

        ProgrammingError.throwForIllegalInvocation();
    }

    RawStore(final RawStore aDelegate) {

        super();

        data = aDelegate.data;
        myRowDim = data.length;
        myColDim = data[0].length;

    }

    public RawStore add(final Access2D<?> aMtrx) {
        return this.plus(RawStore.convert(aMtrx));
    }

    public RawStore add(final int row, final int column, final Access2D<?> aMtrx) {

        final double[][] tmpArrayCopy = this.getArrayCopy();

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

        final double[][] tmpArrayCopy = this.getArrayCopy();
        tmpArrayCopy[row][column] += aNmbr.doubleValue();

        return new RawStore(tmpArrayCopy);
    }

    public MatrixStore<Double> add(final MatrixStore<Double> addend) {
        return this.plus(RawStore.convert(addend));
    }

    public RawStore add(final Number aNmbr) {

        final double[][] retVal = this.getArrayCopy();

        ArrayUtils.modifyAll(retVal, ADD.second(aNmbr.doubleValue()));

        return new RawStore(retVal);
    }

    public Double aggregateAll(final Aggregator aggregator) {

        final AggregatorFunction<Double> tmpVisitor = aggregator.getPrimitiveFunction();

        this.visitAll(tmpVisitor);

        return tmpVisitor.doubleValue();
    }

    public void applyCholesky(final int iterationPoint, final BasicArray<Double> multipliers) {
        // TODO Auto-generated method stub
    }

    public void applyLU(final int iterationPoint, final BasicArray<Double> multipliers) {
        // TODO Auto-generated method stub
    }

    /**
     * Element-by-element left division, C = A.\B
     *
     * @param B another matrix
     * @return A.\B
     */
    public RawStore arrayLeftDivide(final RawStore B) {
        this.checkMatrixDimensions(B);
        final RawStore X = new RawStore(myRowDim, myColDim);
        final double[][] C = X.data;
        for (int i = 0; i < myRowDim; i++) {
            for (int j = 0; j < myColDim; j++) {
                C[i][j] = B.data[i][j] / data[i][j];
            }
        }
        return X;
    }

    /**
     * Element-by-element left division in place, A = A.\B
     *
     * @param B another matrix
     * @return A.\B
     */
    public RawStore arrayLeftDivideEquals(final RawStore B) {
        this.checkMatrixDimensions(B);
        for (int i = 0; i < myRowDim; i++) {
            for (int j = 0; j < myColDim; j++) {
                data[i][j] = B.data[i][j] / data[i][j];
            }
        }
        return this;
    }

    /**
     * Element-by-element right division, C = A./B
     *
     * @param B another matrix
     * @return A./B
     */
    public RawStore arrayRightDivide(final RawStore B) {
        this.checkMatrixDimensions(B);
        final RawStore X = new RawStore(myRowDim, myColDim);
        final double[][] C = X.data;
        for (int i = 0; i < myRowDim; i++) {
            for (int j = 0; j < myColDim; j++) {
                C[i][j] = data[i][j] / B.data[i][j];
            }
        }
        return X;
    }

    /**
     * Element-by-element right division in place, A = A./B
     *
     * @param B another matrix
     * @return A./B
     */
    public RawStore arrayRightDivideEquals(final RawStore B) {
        this.checkMatrixDimensions(B);
        for (int i = 0; i < myRowDim; i++) {
            for (int j = 0; j < myColDim; j++) {
                data[i][j] = data[i][j] / B.data[i][j];
            }
        }
        return this;
    }

    /**
     * Element-by-element multiplication, C = A.*B
     *
     * @param B another matrix
     * @return A.*B
     */
    public RawStore arrayTimes(final RawStore B) {
        this.checkMatrixDimensions(B);
        final RawStore X = new RawStore(myRowDim, myColDim);
        final double[][] C = X.data;
        for (int i = 0; i < myRowDim; i++) {
            for (int j = 0; j < myColDim; j++) {
                C[i][j] = data[i][j] * B.data[i][j];
            }
        }
        return X;
    }

    /**
     * Element-by-element multiplication in place, A = A.*B
     *
     * @param B another matrix
     * @return A.*B
     */
    public RawStore arrayTimesEquals(final RawStore B) {
        this.checkMatrixDimensions(B);
        for (int i = 0; i < myRowDim; i++) {
            for (int j = 0; j < myColDim; j++) {
                data[i][j] = data[i][j] * B.data[i][j];
            }
        }
        return this;
    }

    public Array2D<Double> asArray2D() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Double> asList() {

        final int tmpColDim = RawStore.this.getColDim();

        return new AbstractList<Double>() {

            @Override
            public Double get(final int someIndex) {

                return RawStore.this.get(someIndex / tmpColDim, someIndex % tmpColDim);
            }

            @Override
            public Double set(final int someIndex, final Double aValue) {
                final Double retVal = this.get(someIndex);
                RawStore.this.set(someIndex / tmpColDim, someIndex % tmpColDim, aValue);
                return retVal;
            }

            @Override
            public int size() {
                return RawStore.this.size();
            }
        };
    }

    public final MatrixStore.Builder<Double> builder() {
        return new MatrixStore.Builder<Double>(this);
    }

    public void caxpy(final Double scalarA, final int columnX, final int columnY, final int firstRow) {

        final double tmpValA = scalarA.doubleValue();
        final double[][] tmpArray = data;

        final int tmpRowDim = this.getRowDimension();

        for (int i = firstRow; i < tmpRowDim; i++) {
            tmpArray[i][columnY] += tmpValA * tmpArray[i][columnX];
        }
    }

    /**
     * Clone the RawStore object.
     */
    @Override
    public Object clone() {
        return this.copy();
    }

    public Array1D<ComplexNumber> computeInPlaceSchur(final PhysicalStore<Double> transformationCollector, final boolean eigenvalue) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * RawStore condition (2 norm)
     *
     * @return ratio of largest to smallest singular value.
     */
    public double cond() {
        return 0;
        // return new SingularValueDecomposition(this).cond();
    }

    public RawStore conjugate() {
        return this.transpose();
    }

    /**
     * Make a deep copy of a matrix
     */
    public RawStore copy() {
        final RawStore X = new RawStore(myRowDim, myColDim);
        final double[][] C = X.data;
        for (int i = 0; i < myRowDim; i++) {
            for (int j = 0; j < myColDim; j++) {
                C[i][j] = data[i][j];
            }
        }
        return X;
    }

    public long count() {
        return this.getRowDimension() * this.getColumnDimension();
    }

    public long countColumns() {
        return this.getColumnDimension();
    }

    public long countRows() {
        return this.getRowDimension();
    }

    /**
     * RawStore determinant
     *
     * @return determinant
     */
    public double det() {
        return 0;
        // return new LUDecomposition(this).det();
    }

    public RawStore divide(final Number aNmbr) {

        final double[][] retVal = this.getArrayCopy();

        ArrayUtils.modifyAll(retVal, DIVIDE.second(aNmbr.doubleValue()));

        return new RawStore(retVal);
    }

    public void divideAndCopyColumn(final int row, final int column, final BasicArray<Double> destination) {
        // TODO Auto-generated method stub
    }

    public RawStore divideElements(final Access2D<?> aMtrx) {
        return this.arrayRightDivide(RawStore.convert(aMtrx));
    }

    public double doubleValue(final long anInd) {
        return this.get(AccessUtils.row((int) anInd, this.getRowDimension()), AccessUtils.column((int) anInd, this.getRowDimension()));
    }

    public double doubleValue(final long row, final long column) {
        return this.get((int) row, (int) column);
    }

    public RawStore enforce(final NumberContext aContext) {

        final double[][] retVal = this.getArrayCopy();

        ArrayUtils.modifyAll(retVal, aContext.getPrimitiveEnforceFunction());

        return new RawStore(retVal);
    }

    public final boolean equals(final Access2D<?> aMtrx, final NumberContext aCntxt) {
        return AccessUtils.equals(this, aMtrx, aCntxt);
    }

    public boolean equals(final MatrixStore<Double> other, final NumberContext context) {
        return AccessUtils.equals(this, other, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final boolean equals(final Object anObject) {
        if (anObject instanceof MatrixStore) {
            return this.equals((MatrixStore<Double>) anObject, NumberContext.getGeneral(6));
        } else if (anObject instanceof BasicMatrix) {
            return this.equals((BasicMatrix) anObject, NumberContext.getGeneral(6));
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

        final RawStore tmpLeft = RawStore.convert(aLeftArg, this.getRowDimension());
        final RawStore tmpRight = RawStore.convert(aRightArg, tmpLeft.getColumnDimension());

        this.setMatrix(0, this.getRowDim() - 1, 0, this.getColDim() - 1, tmpLeft.times(tmpRight));
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

        final int tmpRowDim = this.getRowDimension();

        for (int i = 0; i < tmpRowDim; i++) {
            for (int j = 0; j < this.getColumnDimension(); j++) {
                tmpDelegateArray[i][j] = source.doubleValue(i + (j * tmpRowDim));
            }
        }
    }

    public void fillMatching(final Access1D<Double> leftArg, final BinaryFunction<Double> function, final Access1D<Double> rightArg) {
        if (leftArg == this) {
            if (function == ADD) {
                this.plusEquals(RawStore.convert(rightArg, this.getRowDimension()));
            } else if (function == DIVIDE) {
                this.arrayRightDivideEquals(RawStore.convert(rightArg, this.getRowDimension()));
            } else if (function == MULTIPLY) {
                this.arrayTimesEquals(RawStore.convert(rightArg, this.getRowDimension()));
            } else if (function == SUBTRACT) {
                this.minusEquals(RawStore.convert(rightArg, this.getRowDimension()));
            } else {
                ArrayUtils.fillMatching(data, data, function, RawStore.convert(rightArg, this.getRowDimension()).data);
            }
        } else if (rightArg == this) {
            if (function == ADD) {
                this.plusEquals(RawStore.convert(leftArg, this.getRowDimension()));
            } else if (function == DIVIDE) {
                this.arrayLeftDivideEquals(RawStore.convert(leftArg, this.getRowDimension()));
            } else if (function == MULTIPLY) {
                this.arrayTimesEquals(RawStore.convert(leftArg, this.getRowDimension()));
            } else if (function == SUBTRACT) {
                ArrayUtils.fillMatching(data, RawStore.convert(leftArg, this.getRowDimension()).data, function, data);
            } else {
                ArrayUtils.fillMatching(data, RawStore.convert(leftArg, this.getRowDimension()).data, function, data);
            }
        } else {
            ArrayUtils.fillMatching(data, RawStore.convert(leftArg, this.getRowDimension()).data, function,
                    RawStore.convert(rightArg, this.getRowDimension()).data);
        }
    }

    public void fillMatching(final Access1D<Double> aLeftArg, final BinaryFunction<Double> function, final Double aRightArg) {
        ArrayUtils.fillMatching(data, RawStore.convert(aLeftArg, this.getRowDimension()).data, function, aRightArg);
    }

    public void fillMatching(final Double aLeftArg, final BinaryFunction<Double> function, final Access1D<Double> aRightArg) {
        ArrayUtils.fillMatching(data, aLeftArg, function, RawStore.convert(aRightArg, this.getRowDimension()).data);
    }

    public void fillRange(final long first, final long limit, final Double value) {
        ArrayUtils.fillRange(data, (int) first, (int) limit, value);
    }

    public void fillRow(final long row, final long column, final Double aNmbr) {
        ArrayUtils.fillRow(data, (int) row, (int) column, aNmbr);
    }

    public void fillTransposed(final Access2D<? extends Number> source) {

        final double[][] tmpDelegateArray = data;

        final int tmpRowDim = this.getRowDimension();

        for (int i = 0; i < tmpRowDim; i++) {
            for (int j = 0; j < this.getColumnDimension(); j++) {
                tmpDelegateArray[i][j] = source.doubleValue(j, i);
            }
        }
    }

    /**
     * Get a single element.
     *
     * @param i Row index.
     * @param j Column index.
     * @return A(i,j)
     * @exception ArrayIndexOutOfBoundsException
     */
    public double get(final int i, final int j) {
        return data[i][j];
    }

    public Double get(final long index) {
        return this.get(AccessUtils.row(index, this.getRowDimension()), AccessUtils.column(index, this.getRowDimension()));
    }

    public Double get(final long row, final long column) {
        return this.get((int) row, (int) column);
    }

    /**
     * Copy the internal two-dimensional array.
     *
     * @return Two-dimensional array copy of matrix elements.
     */
    public double[][] getArrayCopy() {
        final double[][] C = new double[myRowDim][myColDim];
        for (int i = 0; i < myRowDim; i++) {
            for (int j = 0; j < myColDim; j++) {
                C[i][j] = data[i][j];
            }
        }
        return C;
    }

    public int getColDim() {
        return this.getColumnDimension();
    }

    /**
     * Get column dimension.
     *
     * @return n, the number of columns.
     */
    public int getColumnDimension() {
        return myColDim;
    }

    /**
     * Make a one-dimensional column packed copy of the internal array.
     *
     * @return RawStore elements packed in a one-dimensional array by columns.
     */
    public double[] getColumnPackedCopy() {
        final double[] vals = new double[myRowDim * myColDim];
        for (int i = 0; i < myRowDim; i++) {
            for (int j = 0; j < myColDim; j++) {
                vals[i + (j * myRowDim)] = data[i][j];
            }
        }
        return vals;
    }

    public RawStore getColumnsRange(final int aFirst, final int aLimit) {
        return this.getMatrix(0, this.getRowDim(), aFirst, aLimit);
    }

    public PrimitiveScalar getCondition() {
        return new PrimitiveScalar(this.getSingularValueDecomposition().getCondition());
    }

    public PrimitiveScalar getDeterminant() {
        return new PrimitiveScalar(this.det());
    }

    public List<ComplexNumber> getEigenvalues() {
        return this.getEigenvalueDecomposition().getEigenvalues();
    }

    public PrimitiveScalar getFrobeniusNorm() {
        return new PrimitiveScalar(this.normF());
    }

    public int getIndexOfLargestInColumn(final int row, final int column) {
        // TODO Auto-generated method stub
        return 0;
    }

    public PrimitiveScalar getInfinityNorm() {
        return new PrimitiveScalar(this.normInf());
    }

    public PrimitiveScalar getKyFanNorm(final int k) {
        return new PrimitiveScalar(this.getSingularValueDecomposition().getKyFanNorm(k));
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
     * @param i0 Initial row index
     * @param i1 Final row index
     * @param c Array of column indices.
     * @return A(i0:i1,c(:))
     * @exception ArrayIndexOutOfBoundsException Submatrix indices
     */
    public RawStore getMatrix(final int i0, final int i1, final int[] c) {
        final RawStore X = new RawStore((i1 - i0) + 1, c.length);
        final double[][] B = X.data;
        try {
            for (int i = i0; i <= i1; i++) {
                for (int j = 0; j < c.length; j++) {
                    B[i - i0][j] = data[i][c[j]];
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
     * Get a submatrix.
     *
     * @param r Array of row indices.
     * @param c Array of column indices.
     * @return A(r(:),c(:))
     * @exception ArrayIndexOutOfBoundsException Submatrix indices
     */
    public RawStore getMatrix(final int[] r, final int[] c) {
        final RawStore X = new RawStore(r.length, c.length);
        final double[][] B = X.data;
        try {
            for (int i = 0; i < r.length; i++) {
                for (int j = 0; j < c.length; j++) {
                    B[i][j] = data[r[i]][c[j]];
                }
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
        return X;
    }

    public int getMaxDim() {
        return Math.min(this.getRowDimension(), this.getColumnDimension());
    }

    public int getMinDim() {
        return Math.min(this.getRowDimension(), this.getColumnDimension());
    }

    public Scalar<Double> getOneNorm() {
        return new PrimitiveScalar(this.norm1());
    }

    public PrimitiveScalar getOperatorNorm() {
        return new PrimitiveScalar(this.getSingularValueDecomposition().getOperatorNorm());
    }

    public int getRank() {
        return this.getSingularValueDecomposition().getRank();
    }

    public int getRowDim() {
        return this.getRowDimension();
    }

    /**
     * Get row dimension.
     *
     * @return m, the number of rows.
     */
    public int getRowDimension() {
        return myRowDim;
    }

    /**
     * Make a one-dimensional row packed copy of the internal array.
     *
     * @return RawStore elements packed in a one-dimensional array by rows.
     */
    public double[] getRowPackedCopy() {
        final double[] vals = new double[myRowDim * myColDim];
        for (int i = 0; i < myRowDim; i++) {
            for (int j = 0; j < myColDim; j++) {
                vals[(i * myColDim) + j] = data[i][j];
            }
        }
        return vals;
    }

    public RawStore getRowsRange(final int aFirst, final int aLimit) {
        return new RawStore(this.getMatrix(aFirst, aLimit, 0, this.getColDim()));
    }

    public List<Double> getSingularValues() {
        return this.getSingularValueDecomposition().getSingularValues();
    }

    public PrimitiveScalar getTrace() {
        return new PrimitiveScalar(this.trace());
    }

    public PrimitiveScalar getTraceNorm() {
        return new PrimitiveScalar(this.getSingularValueDecomposition().getTraceNorm());

    }

    public PrimitiveScalar getVectorNorm(final int aDegree) {

        if (aDegree == 0) {

            final AggregatorFunction<Double> tmpFunc = PrimitiveAggregator.getCollection().cardinality();

            ArrayUtils.visitAll(data, tmpFunc);

            return (PrimitiveScalar) tmpFunc.toScalar();

        } else if (aDegree == 1) {

            final AggregatorFunction<Double> tmpFunc = PrimitiveAggregator.getCollection().norm1();

            ArrayUtils.visitAll(data, tmpFunc);

            return (PrimitiveScalar) tmpFunc.toScalar();

        } else if (aDegree == 2) {

            return this.getFrobeniusNorm();

        } else {

            final AggregatorFunction<Double> tmpFunc = PrimitiveAggregator.getCollection().largest();

            this.visitAll(tmpFunc);

            return (PrimitiveScalar) tmpFunc.toScalar();
        }
    }

    @Override
    public final int hashCode() {
        return MatrixUtils.hashCode(this);
    }

    /**
     * RawStore inverse or pseudoinverse
     *
     * @return inverse(A) if A is square, pseudoinverse otherwise.
     */
    public RawStore inverse() {
        return this.solve(RawStore.identity(myRowDim, myRowDim));
    }

    public RawStore invert() {
        return new RawStore(this.inverse());
    }

    public boolean isAbsolute(final long index) {
        final int tmpRowDim = this.getRowDimension();
        return PrimitiveScalar.isAbsolute(this.get(AccessUtils.row(index, tmpRowDim), AccessUtils.column(index, tmpRowDim)));
    }

    public boolean isAbsolute(final long row, final long column) {
        return PrimitiveScalar.isAbsolute(this.get((int) row, (int) column));
    }

    public boolean isEmpty() {
        return ((this.getRowDim() <= 0) || (this.getColDim() <= 0));
    }

    public boolean isFat() {
        return (!this.isEmpty() && (this.getRowDim() < this.getColDim()));
    }

    public boolean isFullRank() {
        return this.getRank() == this.getMinDim();
    }

    public boolean isHermitian() {
        return this.isSymmetric();
    }

    public boolean isLowerLeftShaded() {
        return false;
    }

    public boolean isScalar() {
        return (this.getRowDimension() == 1) && (this.getColumnDimension() == 1);
    }

    public boolean isSmall(final long index, final double comparedTo) {
        final int tmpRowDim = this.getRowDimension();
        return PrimitiveScalar.isSmall(comparedTo, this.get(AccessUtils.row(index, tmpRowDim), AccessUtils.column(index, tmpRowDim)));
    }

    public boolean isSmall(final long row, final long column, final double comparedTo) {
        return PrimitiveScalar.isSmall(comparedTo, this.doubleValue(row, column));
    }

    public boolean isSquare() {
        return (!this.isEmpty() && (this.getRowDim() == this.getColDim()));
    }

    public boolean isSymmetric() {
        return this.isSquare() && this.equals(this.transpose(), NumberContext.getGeneral(6));
    }

    public boolean isTall() {
        return (!this.isEmpty() && (this.getRowDim() > this.getColDim()));
    }

    public boolean isUpperRightShaded() {
        return false;
    }

    public boolean isVector() {
        return ((this.getColDim() == 1) || (this.getRowDim() == 1));
    }

    public final Iterator<Double> iterator() {
        return new Iterator1D<Double>(this);
    }

    public void maxpy(final Double aSclrA, final MatrixStore<Double> aMtrxX) {

        final double tmpValA = aSclrA;
        final double[][] tmpArray = data;

        final int tmpRowDim = this.getRowDimension();
        final int tmpColDim = this.getColumnDimension();

        for (int i = 0; i < tmpRowDim; i++) {
            for (int j = 0; j < tmpColDim; j++) {
                tmpArray[i][j] += tmpValA * aMtrxX.doubleValue(i, j);
            }
        }
    }

    public RawStore mergeColumns(final Access2D<?> aMtrx) {

        final int tmpRowDim = this.getRowDim() + (int) aMtrx.countRows();
        final int tmpColDim = this.getColDim();

        final RawStore retVal = new RawStore(tmpRowDim, tmpColDim);

        retVal.setMatrix(0, this.getRowDim() - 1, 0, tmpColDim - 1, this);
        retVal.setMatrix(this.getRowDim(), tmpRowDim - 1, 0, tmpColDim - 1, RawStore.convert(aMtrx));

        return new RawStore(retVal);
    }

    public RawStore mergeRows(final Access2D<?> aMtrx) {

        final int tmpRowDim = this.getRowDim();
        final int tmpColDim = this.getColDim() + (int) aMtrx.countColumns();

        final RawStore retVal = new RawStore(tmpRowDim, tmpColDim);

        retVal.setMatrix(0, tmpRowDim - 1, 0, this.getColDim() - 1, this);
        retVal.setMatrix(0, tmpRowDim - 1, this.getColDim(), tmpColDim - 1, RawStore.convert(aMtrx));

        return new RawStore(retVal);
    }

    /**
     * C = A - B
     *
     * @param B another matrix
     * @return A - B
     */
    public RawStore minus(final RawStore B) {
        this.checkMatrixDimensions(B);
        final RawStore X = new RawStore(myRowDim, myColDim);
        final double[][] C = X.data;
        for (int i = 0; i < myRowDim; i++) {
            for (int j = 0; j < myColDim; j++) {
                C[i][j] = data[i][j] - B.data[i][j];
            }
        }
        return X;
    }

    /**
     * A = A - B
     *
     * @param B another matrix
     * @return A - B
     */
    public RawStore minusEquals(final RawStore B) {
        this.checkMatrixDimensions(B);
        for (int i = 0; i < myRowDim; i++) {
            for (int j = 0; j < myColDim; j++) {
                data[i][j] = data[i][j] - B.data[i][j];
            }
        }
        return this;
    }

    public RawStore modify(final UnaryFunction<Double> function) {

        final RawStore retVal = this.copy();

        retVal.modifyAll(function);

        return retVal;
    }

    public void modifyAll(final UnaryFunction<Double> function) {
        ArrayUtils.modifyAll(data, function);
    }

    public void modifyColumn(final long row, final long column, final UnaryFunction<Double> function) {
        ArrayUtils.modifyColumn(data, (int) row, (int) column, function);
    }

    public void modifyDiagonal(final long row, final long column, final UnaryFunction<Double> function) {

        final long tmpCount = Math.min(this.getRowDimension() - row, this.getColumnDimension() - column);

        final int tmpFirst = (int) (row + (column * this.getRowDimension()));
        final int tmpLimit = (int) (row + tmpCount + ((column + tmpCount) * this.getRowDimension()));
        final int tmpStep = 1 + this.getRowDimension();

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

    public RawStore multiply(final Number aNmbr) {
        return new RawStore(this.times(aNmbr.doubleValue()));
    }

    public RawStore multiplyElements(final Access2D<?> aMtrx) {
        return new RawStore(this.arrayTimes(RawStore.convert(aMtrx)));
    }

    public RawStore multiplyLeft(final Access1D<Double> leftMtrx) {
        return new RawStore(RawStore.convert(leftMtrx, (int) (leftMtrx.count() / this.getRowDim())).times(this));
    }

    public RawStore multiplyLeft(final Access2D<?> aMtrx) {
        return new RawStore(RawStore.convert(aMtrx).times(this));
    }

    public RawStore multiplyRight(final Access1D<Double> rightMtrx) {
        final RawStore tmpConvert = RawStore.convert(rightMtrx, this.getColDim());
        if (tmpConvert.myRowDim != myColDim) {
            throw new IllegalArgumentException("RawStore inner dimensions must agree.");
        }
        final RawStore X = new RawStore(myRowDim, tmpConvert.myColDim);
        final double[][] C = X.data;
        final double[] Bcolj = new double[myColDim];
        for (int j = 0; j < tmpConvert.myColDim; j++) {
            for (int k = 0; k < myColDim; k++) {
                Bcolj[k] = tmpConvert.data[k][j];
            }
            for (int i = 0; i < myRowDim; i++) {
                final double[] Arowi = data[i];
                double s = 0;
                for (int k = 0; k < myColDim; k++) {
                    s += Arowi[k] * Bcolj[k];
                }
                C[i][j] = s;
            }
        }

        return X;
    }

    public RawStore multiplyRight(final Access2D<?> aMtrx) {
        return new RawStore(this.times(RawStore.convert(aMtrx)));
    }

    public PrimitiveScalar multiplyVectors(final Access2D<?> aVctr) {

        double retVal = ZERO;

        final int tmpSize = this.size();
        for (int i = 0; i < tmpSize; i++) {
            retVal += this.doubleValue(i) * aVctr.doubleValue(i);
        }

        return new PrimitiveScalar(retVal);
    }

    public RawStore negate() {
        return new RawStore(this.uminus());
    }

    public void negateColumn(final int column) {
        // TODO Auto-generated method stub
    }

    /**
     * One norm
     *
     * @return maximum column sum.
     */
    public double norm1() {
        double f = 0;
        for (int j = 0; j < myColDim; j++) {
            double s = 0;
            for (int i = 0; i < myRowDim; i++) {
                s += Math.abs(data[i][j]);
            }
            f = Math.max(f, s);
        }
        return f;
    }

    /**
     * Two norm
     *
     * @return maximum singular value.
     */
    public double norm2() {
        return 0;
        // return (new SingularValueDecomposition(this).norm2());
    }

    /**
     * Frobenius norm
     *
     * @return sqrt of sum of squares of all elements.
     */
    public double normF() {
        double f = 0;
        for (int i = 0; i < myRowDim; i++) {
            for (int j = 0; j < myColDim; j++) {
                f = Maths.hypot(f, data[i][j]);
            }
        }
        return f;
    }

    /**
     * Infinity norm
     *
     * @return maximum row sum.
     */
    public double normInf() {
        double f = 0;
        for (int i = 0; i < myRowDim; i++) {
            double s = 0;
            for (int j = 0; j < myColDim; j++) {
                s += Math.abs(data[i][j]);
            }
            f = Math.max(f, s);
        }
        return f;
    }

    /**
     * C = A + B
     *
     * @param B another matrix
     * @return A + B
     */
    public RawStore plus(final RawStore B) {
        this.checkMatrixDimensions(B);
        final RawStore X = new RawStore(myRowDim, myColDim);
        final double[][] C = X.data;
        for (int i = 0; i < myRowDim; i++) {
            for (int j = 0; j < myColDim; j++) {
                C[i][j] = data[i][j] + B.data[i][j];
            }
        }
        return X;
    }

    /**
     * A = A + B
     *
     * @param B another matrix
     * @return A + B
     */
    public RawStore plusEquals(final RawStore B) {
        this.checkMatrixDimensions(B);
        for (int i = 0; i < myRowDim; i++) {
            for (int j = 0; j < myColDim; j++) {
                data[i][j] = data[i][j] + B.data[i][j];
            }
        }
        return this;
    }

    /**
     * Print the matrix to stdout. Line the elements up in columns with a Fortran-like 'Fw.d' style format.
     *
     * @param w Column width.
     * @param d Number of digits after the decimal.
     */
    public void print(final int w, final int d) {
        this.print(new PrintWriter(System.out, true), w, d);
    }

    /**
     * Print the matrix to stdout. Line the elements up in columns. Use the format object, and right justify within
     * columns of width characters. Note that is the matrix is to be read back in, you probably will want to use a
     * NumberFormat that is set to US Locale.
     *
     * @param format A Formatting object for individual elements.
     * @param width Field width for each column.
     * @see java.text.DecimalFormat#setDecimalFormatSymbols
     */
    public void print(final NumberFormat format, final int width) {
        this.print(new PrintWriter(System.out, true), format, width);
    }

    /**
     * Print the matrix to the output stream. Line the elements up in columns with a Fortran-like 'Fw.d' style format.
     *
     * @param output Output stream.
     * @param w Column width.
     * @param d Number of digits after the decimal.
     */
    public void print(final PrintWriter output, final int w, final int d) {
        final DecimalFormat format = new DecimalFormat();
        format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        format.setMinimumIntegerDigits(1);
        format.setMaximumFractionDigits(d);
        format.setMinimumFractionDigits(d);
        format.setGroupingUsed(false);
        this.print(output, format, w + 2);
    }

    /**
     * Print the matrix to the output stream. Line the elements up in columns. Use the format object, and right justify
     * within columns of width characters. Note that is the matrix is to be read back in, you probably will want to use
     * a NumberFormat that is set to US Locale.
     *
     * @param output the output stream.
     * @param format A formatting object to format the matrix elements
     * @param width Column width.
     * @see java.text.DecimalFormat#setDecimalFormatSymbols
     */
    public void print(final PrintWriter output, final NumberFormat format, final int width) {
        output.println(); // start on new line.
        for (int i = 0; i < myRowDim; i++) {
            for (int j = 0; j < myColDim; j++) {
                final String s = format.format(data[i][j]); // format the number
                final int padding = Math.max(1, width - s.length()); // At _least_ 1 space
                for (int k = 0; k < padding; k++) {
                    output.print(' ');
                }
                output.print(s);
            }
            output.println();
        }
        output.println(); // end with blank line.
    }

    /**
     * RawStore rank
     *
     * @return effective numerical rank, obtained from SVD.
     */
    public int rank() {
        return 0;
        // return new SingularValueDecomposition(this).rank();
    }

    public void raxpy(final Double scalarA, final int rowX, final int rowY, final int firstColumn) {

        final double tmpValA = scalarA.doubleValue();
        final double[][] tmpArray = data;

        final int tmpColDim = this.getColumnDimension();

        for (int j = firstColumn; j < tmpColDim; j++) {
            tmpArray[rowY][j] += tmpValA * tmpArray[rowX][j];

        }
    }

    public void rotateRight(final int aLow, final int aHigh, final double aCos, final double aSin) {
        // TODO Auto-generated method stub
    }

    public RawStore round(final NumberContext aCntxt) {

        final double[][] retVal = this.getArrayCopy();

        ArrayUtils.modifyAll(retVal, aCntxt.getPrimitiveRoundFunction());

        return new RawStore(retVal);
    }

    public RawStore scale(final Double scalar) {
        return new RawStore(this.times(scalar.doubleValue()));
    }

    /*
     * ------------------------ Class variables ------------------------
     */

    public RawStore selectColumns(final int... someCols) {
        return new RawStore(this.getMatrix(AccessUtils.makeIncreasingRange(0, this.getRowDim()), someCols));
    }

    public RawStore selectRows(final int... someRows) {
        return new RawStore(this.getMatrix(someRows, AccessUtils.makeIncreasingRange(0, this.getColDim())));
    }

    /*
     * ------------------------ Constructors ------------------------
     */

    /**
     * Set a single element.
     *
     * @param i Row index.
     * @param j Column index.
     * @param s A(i,j).
     * @exception ArrayIndexOutOfBoundsException
     */
    public void set(final int i, final int j, final double s) {
        data[i][j] = s;
    }

    public Double set(final int anInd, final Number value) {
        final double retVal = this.get(AccessUtils.row(anInd, this.getRowDimension()), AccessUtils.column(anInd, this.getRowDimension()));
        this.set(AccessUtils.row(anInd, this.getRowDimension()), AccessUtils.column(anInd, this.getRowDimension()), value.doubleValue());
        return retVal;
    }

    public void set(final long index, final double value) {
        this.set(AccessUtils.row(index, this.getRowDimension()), AccessUtils.column(index, this.getRowDimension()), value);
    }

    public void set(final long row, final long column, final double aNmbr) {
        this.set((int) row, (int) column, aNmbr);
    }

    public void set(final long row, final long column, final Number aNmbr) {
        this.set((int) row, (int) column, aNmbr.doubleValue());
    }

    public void set(final long index, final Number value) {
        this.set(AccessUtils.row(index, this.getRowDimension()), AccessUtils.column(index, this.getRowDimension()), value.doubleValue());
    }

    /**
     * Set a submatrix.
     *
     * @param i0 Initial row index
     * @param i1 Final row index
     * @param j0 Initial column index
     * @param j1 Final column index
     * @param X A(i0:i1,j0:j1)
     * @exception ArrayIndexOutOfBoundsException Submatrix indices
     */
    public void setMatrix(final int i0, final int i1, final int j0, final int j1, final RawStore X) {
        try {
            for (int i = i0; i <= i1; i++) {
                for (int j = j0; j <= j1; j++) {
                    data[i][j] = X.get(i - i0, j - j0);
                }
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
    }

    /**
     * Set a submatrix.
     *
     * @param i0 Initial row index
     * @param i1 Final row index
     * @param c Array of column indices.
     * @param X A(i0:i1,c(:))
     * @exception ArrayIndexOutOfBoundsException Submatrix indices
     */
    public void setMatrix(final int i0, final int i1, final int[] c, final RawStore X) {
        try {
            for (int i = i0; i <= i1; i++) {
                for (int j = 0; j < c.length; j++) {
                    data[i][c[j]] = X.get(i - i0, j);
                }
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
    }

    /**
     * Set a submatrix.
     *
     * @param r Array of row indices.
     * @param j0 Initial column index
     * @param j1 Final column index
     * @param X A(r(:),j0:j1)
     * @exception ArrayIndexOutOfBoundsException Submatrix indices
     */
    public void setMatrix(final int[] r, final int j0, final int j1, final RawStore X) {
        try {
            for (int i = 0; i < r.length; i++) {
                for (int j = j0; j <= j1; j++) {
                    data[r[i]][j] = X.get(i, j - j0);
                }
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
    }

    /**
     * Set a submatrix.
     *
     * @param r Array of row indices.
     * @param c Array of column indices.
     * @param X A(r(:),c(:))
     * @exception ArrayIndexOutOfBoundsException Submatrix indices
     */
    public void setMatrix(final int[] r, final int[] c, final RawStore X) {
        try {
            for (int i = 0; i < r.length; i++) {
                for (int j = 0; j < c.length; j++) {
                    data[r[i]][c[j]] = X.get(i, j);
                }
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
    }

    public void setToIdentity(final int aCol) {
        // TODO Auto-generated method stub
    }

    public int size() {
        return this.getRowDimension() * this.getColumnDimension();
    }

    public RawStore solve(final Access2D<?> aRHS) {

        RawStore retVal = RawStore.convert(aRHS);

        try {
            if (this.isTall()) {
                //  retVal = new QRDecomposition(this).solve(retVal);
            } else {
                // retVal = new LUDecomposition(this).solve(retVal);
            }
        } catch (final RuntimeException anRE) {
            final RawSingularValue tmpMD = new RawSingularValue();
            tmpMD.compute(this);
            retVal = tmpMD.solve(new RawStore(retVal));
        }

        return new RawStore(retVal);
    }

    /**
     * Solve A*X = B
     *
     * @param B right hand side
     * @return solution if A is square, least squares solution otherwise
     */
    public RawStore solve(final RawStore B) {
        return null;
        // return (myRowDim == myColDim ? (new LUDecomposition(this)).solve(B) : (new QRDecomposition(this)).solve(B));
    }

    /**
     * Solve X*A = B, which is also A'*X' = B'
     *
     * @param B right hand side
     * @return solution if A is square, least squares solution otherwise.
     */
    public RawStore solveTranspose(final RawStore B) {
        return this.transpose().solve(B.transpose());
    }

    public void substituteBackwards(final Access2D<Double> body, final boolean conjugated) {
        // TODO Auto-generated method stub
    }

    public void substituteForwards(final Access2D<Double> body, final boolean onesOnDiagonal, final boolean zerosAboveDiagonal) {
        // TODO Auto-generated method stub
    }

    public RawStore subtract(final Access2D<?> aMtrx) {
        return new RawStore(this.minus(RawStore.convert(aMtrx)));
    }

    public MatrixStore<Double> subtract(final MatrixStore<Double> subtrahend) {
        return this.add(subtrahend.negate());
    }

    public RawStore subtract(final Number value) {

        final double[][] retVal = this.getArrayCopy();

        ArrayUtils.modifyAll(retVal, SUBTRACT.second(value.doubleValue()));

        return new RawStore(retVal);
    }

    /**
     * Multiply a matrix by a scalar, C = s*A
     *
     * @param s scalar
     * @return s*A
     */
    public RawStore times(final double s) {
        final RawStore X = new RawStore(myRowDim, myColDim);
        final double[][] C = X.data;
        for (int i = 0; i < myRowDim; i++) {
            for (int j = 0; j < myColDim; j++) {
                C[i][j] = s * data[i][j];
            }
        }
        return X;
    }

    /**
     * Linear algebraic matrix multiplication, A * B
     *
     * @param B another matrix
     * @return RawStore product, A * B
     * @exception IllegalArgumentException RawStore inner dimensions must agree.
     */
    public RawStore times(final RawStore B) {
        if (B.myRowDim != myColDim) {
            throw new IllegalArgumentException("RawStore inner dimensions must agree.");
        }
        final RawStore X = new RawStore(myRowDim, B.myColDim);
        final double[][] C = X.data;
        final double[] Bcolj = new double[myColDim];
        for (int j = 0; j < B.myColDim; j++) {
            for (int k = 0; k < myColDim; k++) {
                Bcolj[k] = B.data[k][j];
            }
            for (int i = 0; i < myRowDim; i++) {
                final double[] Arowi = data[i];
                double s = 0;
                for (int k = 0; k < myColDim; k++) {
                    s += Arowi[k] * Bcolj[k];
                }
                C[i][j] = s;
            }
        }
        return X;
    }

    /**
     * Multiply a matrix by a scalar in place, A = s*A
     *
     * @param s scalar
     * @return replace A by s*A
     */
    public RawStore timesEquals(final double s) {
        for (int i = 0; i < myRowDim; i++) {
            for (int j = 0; j < myColDim; j++) {
                data[i][j] = s * data[i][j];
            }
        }
        return this;
    }

    public BigDecimal toBigDecimal(final int row, final int column) {
        return new BigDecimal(this.get(row, column));
    }

    public PhysicalStore<BigDecimal> toBigStore() {
        return BigDenseStore.FACTORY.copy(this);
    }

    public ComplexNumber toComplexNumber(final int row, final int column) {
        return ComplexNumber.makeReal(this.get(row, column));
    }

    public PhysicalStore<ComplexNumber> toComplexStore() {
        return ComplexDenseStore.FACTORY.copy(this);
    }

    public List<Double> toListOfElements() {
        return this.toPrimitiveStore().asList();
    }

    public RawStore toPrimitiveStore() {
        return new RawStore(this.getArrayCopy());
    }

    public PrimitiveScalar toScalar(final long row, final long column) {
        return new PrimitiveScalar(this.get((int) row, (int) column));
    }

    @Override
    public String toString() {
        return MatrixUtils.toString(this);
    }

    public String toString(final int row, final int column) {
        return Double.toString(this.get(row, column));
    }

    /**
     * RawStore trace.
     *
     * @return sum of the diagonal elements.
     */
    public double trace() {
        double t = 0;
        for (int i = 0; i < Math.min(myRowDim, myColDim); i++) {
            t += data[i][i];
        }
        return t;
    }

    public void transformLeft(final Householder<Double> transformation, final int firstColumn) {

        final double[][] tmpArray = data;
        final int tmpRowDim = this.getRowDimension();
        final int tmpColDim = this.getColumnDimension();

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
        final int tmpRowDim = this.getRowDimension();
        final int tmpColDim = this.getColumnDimension();

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

    public void transformSymmetric(final Householder<Double> transformation) {
        // TODO Auto-generated method stub
    }

    /**
     * RawStore transpose.
     *
     * @return A'
     */
    public RawStore transpose() {
        final RawStore X = new RawStore(myColDim, myRowDim);
        final double[][] C = X.data;
        for (int i = 0; i < myRowDim; i++) {
            for (int j = 0; j < myColDim; j++) {
                C[j][i] = data[i][j];
            }
        }
        return X;
    }

    public void tred2(final BasicArray<Double> mainDiagonal, final BasicArray<Double> offDiagonal, final boolean yesvecs) {
        // TODO Auto-generated method stub
    }

    /**
     * Unary minus
     *
     * @return -A
     */
    public RawStore uminus() {
        final RawStore X = new RawStore(myRowDim, myColDim);
        final double[][] C = X.data;
        for (int i = 0; i < myRowDim; i++) {
            for (int j = 0; j < myColDim; j++) {
                C[i][j] = -data[i][j];
            }
        }
        return X;
    }

    public final void update(final int aFirstRow, final int aRowCount, final int aFirstCol, final int aColCount, final RawStore aMtrx) {
        this.setMatrix(aFirstRow, aRowCount - aFirstRow - 1, aFirstCol, aColCount - aFirstCol - 1, aMtrx);
    }

    public final void update(final int aFirstRow, final int aRowCount, final int[] someColumns, final RawStore aMtrx) {
        this.setMatrix(aFirstRow, aRowCount - aFirstRow - 1, someColumns, aMtrx);
    }

    public final void update(final int row, final int column, final Number aNmbr) {
        this.set(row, column, aNmbr.doubleValue());
    }

    public final void update(final int[] someRows, final int aFirstCol, final int aColCount, final RawStore aMtrx) {
        this.setMatrix(someRows, aFirstCol, aColCount - aFirstCol - 1, aMtrx);
    }

    public final void update(final int[] someRows, final int[] someColumns, final RawStore aMtrx) {
        this.setMatrix(someRows, someColumns, aMtrx);
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
        if ((B.myRowDim != myRowDim) || (B.myColDim != myColDim)) {
            throw new IllegalArgumentException("RawStore dimensions must agree.");
        }
    }

    final RawCholesky getCholeskyDecomposition() {
        final RawCholesky retVal = new RawCholesky();
        retVal.compute(this);
        return retVal;
    }

    // DecimalFormat is a little disappointing coming from Fortran or C's printf.
    // Since it doesn't pad on the left, the elements will come out different
    // widths.  Consequently, we'll pass the desired column width in as an
    // argument and do the extra padding ourselves.

    final RawEigenvalue getEigenvalueDecomposition() {
        final RawEigenvalue retVal = MatrixUtils.isHermitian(this) ? new RawEigenvalue.Symmetric() : new RawEigenvalue.Nonsymmetric();
        retVal.compute(this);
        return retVal;
    }

    final RawLU getLUDecomposition() {
        final RawLU retVal = new RawLU();
        retVal.compute(this);
        return retVal;
    }

    final RawQR getQRDecomposition() {
        final RawQR retVal = new RawQR();
        retVal.compute(this);
        return retVal;
    }

    final RawSingularValue getSingularValueDecomposition() {
        final RawSingularValue retVal = new RawSingularValue();
        retVal.compute(this);
        return retVal;
    }

}
