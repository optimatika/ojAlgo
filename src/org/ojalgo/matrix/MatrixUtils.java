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
package org.ojalgo.matrix;

import java.math.BigDecimal;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.array.Array1D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.FunctionUtils;
import org.ojalgo.matrix.decomposition.*;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.operation.*;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

public abstract class MatrixUtils {

    public static <N extends Number> boolean equals(final MatrixStore<N> matrix, final Bidiagonal<N> decomposition, final NumberContext context) {

        final int tmpRowDim = (int) matrix.countRows();
        final int tmpColDim = (int) matrix.countColumns();

        final MatrixStore<N> tmpQ1 = decomposition.getQ1();
        final MatrixStore<N> tmpD = decomposition.getD();
        final MatrixStore<N> tmpQ2 = decomposition.getQ2();

        final MatrixStore<N> tmpConjugatedQ1 = tmpQ1.builder().conjugate().build();
        final MatrixStore<N> tmpConjugatedQ2 = tmpQ2.builder().conjugate().build();

        MatrixStore<N> tmpThis;
        MatrixStore<N> tmpThat;

        boolean retVal = (tmpRowDim == tmpQ1.countRows()) && (tmpQ2.countRows() == tmpColDim);

        // Check that it's possible to reconstruct the original matrix.
        if (retVal) {

            tmpThis = matrix;
            tmpThat = decomposition.reconstruct();

            retVal &= tmpThis.equals(tmpThat, context);
        }

        // If Q1 is square, then check if it is orthogonal/unitary.
        if (retVal && (tmpQ1.countRows() == tmpQ1.countColumns())) {

            tmpThis = tmpQ1;
            tmpThat = tmpConjugatedQ1.multiplyLeft(tmpQ1).multiplyRight(tmpQ1);

            retVal &= tmpThis.equals(tmpThat, context);
        }

        // If Q2 is square, then check if it is orthogonal/unitary.
        if (retVal && (tmpQ2.countRows() == tmpQ2.countColumns())) {

            tmpThis = tmpQ2;
            tmpThat = tmpConjugatedQ2.multiplyLeft(tmpQ2).multiplyRight(tmpQ2);

            retVal &= tmpThis.equals(tmpThat, context);
        }

        return retVal;
    }

    public static <N extends Number> boolean equals(final MatrixStore<N> matrix, final Cholesky<N> decomposition, final NumberContext context) {

        boolean retVal = false;

        final MatrixStore<N> tmpL = decomposition.getL();

        retVal = AccessUtils.equals(tmpL.multiplyRight(tmpL.builder().conjugate().build()), matrix, context);

        return retVal;
    }

    public static <N extends Number> boolean equals(final MatrixStore<N> matrix, final Eigenvalue<N> decomposition, final NumberContext context) {

        final MatrixStore<N> tmpD = decomposition.getD();
        final MatrixStore<N> tmpV = decomposition.getV();

        // Check that [A][V] == [V][D] ([A] == [V][D][V]<sup>T</sup> is not always true)
        final MatrixStore<N> tmpStore1 = matrix.multiplyRight(tmpV);
        final MatrixStore<N> tmpStore2 = tmpD.multiplyLeft(tmpV);

        return AccessUtils.equals(tmpStore1, tmpStore2, context);
    }

    public static <N extends Number> boolean equals(final MatrixStore<N> matrix, final Hessenberg<N> decomposition, final NumberContext context) {

        final MatrixStore<N> tmpH = decomposition.getH();
        final MatrixStore<N> tmpQ = decomposition.getQ();

        final MatrixStore<N> tmpStore1 = matrix.multiplyRight(tmpQ);
        final MatrixStore<N> tmpStore2 = tmpH.multiplyLeft(tmpQ);

        return AccessUtils.equals(tmpStore1, tmpStore2, context);
    }

    public static <N extends Number> boolean equals(final MatrixStore<N> matrix, final LU<N> decomposition, final NumberContext context) {

        final MatrixStore<N> tmpL = decomposition.getL();
        final MatrixStore<N> tmpU = decomposition.getU();
        final int[] tmpPivotOrder = decomposition.getPivotOrder();

        return AccessUtils.equals(matrix.builder().row(tmpPivotOrder).build(), tmpL.multiplyRight(tmpU), context);
    }

    public static <N extends Number> boolean equals(final MatrixStore<N> matrix, final QR<N> decomposition, final NumberContext context) {

        final MatrixStore<N> tmpQ = decomposition.getQ();
        final MatrixStore<N> tmpR = decomposition.getR();

        final MatrixStore<N> tmpStore = tmpQ.multiplyRight(tmpR);

        return AccessUtils.equals(tmpStore, matrix, context);
    }

    public static <N extends Number> boolean equals(final MatrixStore<N> matrix, final Schur<N> decomposition, final NumberContext context) {

        final MatrixStore<N> tmpU = decomposition.getU();
        final MatrixStore<N> tmpQ = decomposition.getQ();

        // Check that [A][Q] == [Q][U] ([A] == [Q][U][Q]<sup>T</sup> is not always true)
        final MatrixStore<N> tmpStore1 = matrix.multiplyRight(tmpQ);
        final MatrixStore<N> tmpStore2 = tmpU.multiplyLeft(tmpQ);

        return AccessUtils.equals(tmpStore1, tmpStore2, context);
    }

    public static <N extends Number> boolean equals(final MatrixStore<N> matrix, final SingularValue<N> decomposition, final NumberContext context) {

        final int tmpRowDim = (int) matrix.countRows();
        final int tmpColDim = (int) matrix.countColumns();

        final MatrixStore<N> tmpQ1 = decomposition.getQ1();
        final MatrixStore<N> tmpD = decomposition.getD();
        final MatrixStore<N> tmpQ2 = decomposition.getQ2();

        MatrixStore<N> tmpThis;
        MatrixStore<N> tmpThat;

        boolean retVal = (tmpRowDim == tmpQ1.countRows()) && (tmpQ2.countRows() == tmpColDim);

        // Check that [A][Q2] == [Q1][D]
        if (retVal) {

            tmpThis = matrix.multiplyRight(tmpQ2);
            tmpThat = tmpD.multiplyLeft(tmpQ1);

            retVal &= tmpThis.equals(tmpThat, context);
        }

        // If Q1 is square, then check if it is orthogonal/unitary.
        if (retVal && (tmpQ1.countRows() == tmpQ1.countColumns())) {

            tmpThis = tmpQ1.factory().makeEye(tmpRowDim, tmpRowDim);
            tmpThat = tmpQ1.builder().conjugate().build().multiplyRight(tmpQ1);

            retVal &= tmpThis.equals(tmpThat, context);
        }

        // If Q2 is square, then check if it is orthogonal/unitary.
        if (retVal && (tmpQ2.countRows() == tmpQ2.countColumns())) {

            tmpThis = tmpQ2.factory().makeEye(tmpColDim, tmpColDim);
            tmpThat = tmpQ2.builder().conjugate().build().multiplyLeft(tmpQ2);

            retVal &= tmpThis.equals(tmpThat, context);
        }

        // Check the pseudoinverse.
        if (retVal) {
            retVal &= matrix.equals(decomposition.getInverse().multiplyRight(matrix).multiplyLeft(matrix), context);
        }

        // Check that the singular values are sorted in descending order
        if (retVal) {
            final Array1D<Double> tmpSV = decomposition.getSingularValues();
            for (int i = 1; retVal && (i < tmpSV.size()); i++) {
                retVal &= tmpSV.doubleValue(i - 1) >= tmpSV.doubleValue(i);
            }
            if (retVal && decomposition.isOrdered()) {
                for (int ij = 1; retVal && (ij < tmpD.countRows()); ij++) {
                    retVal &= tmpD.doubleValue(ij - 1, ij - 1) >= tmpD.doubleValue(ij, ij);
                }
            }
        }

        return retVal;
    }

    public static <N extends Number> boolean equals(final MatrixStore<N> matrix, final Tridiagonal<N> decomposition, final NumberContext context) {

        // Check that [A] == [Q][D][Q]<sup>T</sup>
        return AccessUtils.equals(matrix, MatrixUtils.reconstruct(decomposition), context);

        // Check that Q is orthogonal/unitary...
    }

    public static <N extends Number> int hashCode(final BasicMatrix matrix) {
        return AccessUtils.hashCode(matrix);
    }

    public static <N extends Number> int hashCode(final MatrixStore<N> matrix) {
        return AccessUtils.hashCode(matrix);
    }

    public static boolean isHermitian(final Access2D<?> matrix) {

        final long tmpRowDim = matrix.countRows();
        final long tmpColDim = matrix.countColumns();

        final Number tmpElement = matrix.get(0L);

        boolean retVal = tmpRowDim == tmpColDim;

        if (tmpElement instanceof ComplexNumber) {

            ComplexNumber tmpLowerLeft;
            ComplexNumber tmpUpperRight;

            for (int j = 0; retVal && (j < tmpColDim); j++) {
                retVal &= TypeUtils.isZero(TypeUtils.toComplexNumber(matrix.get(j, j)).i);
                for (int i = j + 1; retVal && (i < tmpRowDim); i++) {
                    tmpLowerLeft = TypeUtils.toComplexNumber(matrix.get(i, j)).conjugate();
                    tmpUpperRight = TypeUtils.toComplexNumber(matrix.get(j, i));
                    retVal &= TypeUtils.isZero(tmpLowerLeft.subtract(tmpUpperRight).norm());
                }
            }

        } else {

            for (int j = 0; retVal && (j < tmpColDim); j++) {
                for (int i = j + 1; retVal && (i < tmpRowDim); i++) {
                    retVal &= TypeUtils.isZero(matrix.doubleValue(i, j) - matrix.doubleValue(j, i));
                }
            }
        }

        return retVal;
    }

    public static final boolean isLowerLeftShaded(final Access1D<?> anAccess) {
        return anAccess instanceof MatrixStore<?> ? ((MatrixStore<?>) anAccess).isLowerLeftShaded() : false;
    }

    public static <N extends Number> boolean isNormal(final MatrixStore<N> matrix) {

        final MatrixStore<N> tmpConjugate = matrix.builder().conjugate().build();

        return matrix.multiplyLeft(tmpConjugate).equals(matrix.multiplyRight(tmpConjugate));
    }

    public static final boolean isUpperRightShaded(final Access1D<?> anAccess) {
        return anAccess instanceof MatrixStore<?> ? ((MatrixStore<?>) anAccess).isUpperRightShaded() : false;
    }

    /**
     * @deprecated v36 Use {@link AccessUtils#makeDecreasingRange(int,int)} instead
     */
    @Deprecated
    public static int[] makeDecreasingRange(final int first, final int count) {
        return AccessUtils.makeDecreasingRange(first, count);
    }

    /**
     * @deprecated v36 Use {@link AccessUtils#makeIncreasingRange(int,int)} instead
     */
    @Deprecated
    public static int[] makeIncreasingRange(final int first, final int count) {
        return AccessUtils.makeIncreasingRange(first, count);
    }

    public static PhysicalStore<ComplexNumber> makeRandomComplexStore(final int aRowDim, final int aColDim) {

        final PhysicalStore<ComplexNumber> retVal = ComplexDenseStore.FACTORY.makeZero(aRowDim, aColDim);

        final Uniform tmpArgGen = new Uniform(PrimitiveMath.ZERO, PrimitiveMath.TWO_PI);

        for (int j = 0; j < aColDim; j++) {
            for (int i = 0; i < aRowDim; i++) {
                retVal.set(i, j, ComplexNumber.makePolar(PrimitiveMath.E, tmpArgGen.doubleValue()).add(PrimitiveMath.PI));
            }
        }

        return retVal;
    }

    /**
     * @deprecated v36 Use {@link FunctionUtils#max(int...)} instead
     */
    @Deprecated
    public static int max(final int... values) {
        return FunctionUtils.max(values);
    }

    /**
     * @deprecated v36 Use {@link FunctionUtils#min(int...)} instead
     */
    @Deprecated
    public static int min(final int... values) {
        return FunctionUtils.min(values);
    }

    public static <N extends Number> MatrixStore<N> reconstruct(final Bidiagonal<N> decomposition) {
        return decomposition.getD().multiplyLeft(decomposition.getQ1()).multiplyRight(decomposition.getQ2().conjugate());
    }

    public static <N extends Number> MatrixStore<N> reconstruct(final Cholesky<N> decomposition) {
        final MatrixStore<N> tmpL = decomposition.getL();
        return tmpL.multiplyRight(tmpL.conjugate());
    }

    public static <N extends Number> MatrixStore<N> reconstruct(final Eigenvalue<N> decomposition) {
        final MatrixStore<N> tmpV = decomposition.getV();
        return decomposition.getD().multiplyLeft(tmpV).multiplyRight(tmpV.conjugate());
    }

    public static <N extends Number> MatrixStore<N> reconstruct(final Hessenberg<N> decomposition) {
        final MatrixStore<N> tmpQ = decomposition.getQ();
        final MatrixStore<N> tmpH = decomposition.getH();
        return tmpH.multiplyLeft(tmpQ).multiplyRight(tmpQ.transpose());
    }

    public static <N extends Number> MatrixStore<N> reconstruct(final LU<N> decomposition) {
        return decomposition.getL().multiplyRight(decomposition.getU()).builder().row(decomposition.getPivotOrder()).build();
    }

    public static <N extends Number> MatrixStore<N> reconstruct(final QR<N> decomposition) {
        return decomposition.getQ().multiplyRight(decomposition.getR());
    }

    public static <N extends Number> MatrixStore<N> reconstruct(final Schur<N> decomposition) {
        final MatrixStore<N> tmpQ = decomposition.getQ();
        return decomposition.getU().multiplyLeft(tmpQ).multiplyRight(tmpQ.builder().transpose().build());
    }

    public static <N extends Number> MatrixStore<N> reconstruct(final SingularValue<N> decomposition) {
        return decomposition.getQ1().multiplyRight(decomposition.getD()).multiplyRight(decomposition.getQ2().conjugate());
    }

    public static <N extends Number> MatrixStore<N> reconstruct(final Tridiagonal<N> decomposition) {
        final MatrixStore<N> tmpQ = decomposition.getQ();
        return decomposition.getD().multiplyLeft(tmpQ).multiplyRight(tmpQ.conjugate());
    }

    public static void setAllOperationThresholds(final int aValue) {
        AggregateAll.THRESHOLD = aValue;
        ApplyCholesky.THRESHOLD = aValue;
        ApplyLU.THRESHOLD = aValue;
        CAXPY.THRESHOLD = aValue;
        FillMatchingBoth.THRESHOLD = aValue;
        FillConjugated.THRESHOLD = aValue;
        FillMatchingLeft.THRESHOLD = aValue;
        FillMatchingRight.THRESHOLD = aValue;
        FillMatchingSingle.THRESHOLD = aValue;
        FillTransposed.THRESHOLD = aValue;
        GenerateApplyAndCopyHouseholderColumn.THRESHOLD = aValue;
        GenerateApplyAndCopyHouseholderRow.THRESHOLD = aValue;
        HermitianRank2Update.THRESHOLD = aValue;
        HouseholderHermitian.THRESHOLD = aValue;
        HouseholderLeft.THRESHOLD = aValue;
        HouseholderRight.THRESHOLD = aValue;
        MAXPY.THRESHOLD = aValue;
        ModifyAll.THRESHOLD = aValue;
        MultiplyBoth.THRESHOLD = aValue;
        MultiplyHermitianAndVector.THRESHOLD = aValue;
        MultiplyLeft.THRESHOLD = aValue;
        MultiplyRight.THRESHOLD = aValue;
        RAXPY.THRESHOLD = aValue;
        RotateLeft.THRESHOLD = aValue;
        RotateRight.THRESHOLD = aValue;
        SubstituteBackwards.THRESHOLD = aValue;
        SubstituteForwards.THRESHOLD = aValue;
        SubtractScaledVector.THRESHOLD = aValue;
    }

    public static String toString(final Access2D<?> matrix) {

        final StringBuilder retVal = new StringBuilder();

        final int tmpRowDim = (int) matrix.countRows();
        final int tmpColDim = (int) matrix.countColumns();

        retVal.append(matrix.getClass().getName());
        retVal.append(' ').append('<').append(' ').append(tmpRowDim).append(' ').append('x').append(' ').append(tmpColDim).append(' ').append('>');

        if ((tmpRowDim > 0) && (tmpColDim > 0) && (tmpRowDim <= 50) && (tmpColDim <= 50) && ((tmpRowDim * tmpColDim) <= 200)) {

            // First element
            retVal.append("\n{ { ").append(matrix.get(0, 0));

            // Rest of the first row
            for (int j = 1; j < tmpColDim; j++) {
                retVal.append(",\t").append(matrix.get(0, j));
            }

            // For each of the remaining rows
            for (int i = 1; i < tmpRowDim; i++) {

                // First column
                retVal.append(" },\n{ ").append(matrix.get(i, 0));

                // Remaining columns
                for (int j = 1; j < tmpColDim; j++) {
                    retVal.append(",\t").append(matrix.get(i, j));
                }
            }

            // Finish
            retVal.append(" } }");
        }

        return retVal.toString();
    }

    public static Access2D<BigDecimal> wrapBigAccess2D(final BasicMatrix matrix) {
        return new Access2D<BigDecimal>() {

            public long count() {
                return this.size();
            }

            public long countColumns() {
                return matrix.countColumns();
            }

            public long countRows() {
                return matrix.countRows();
            }

            public double doubleValue(final long anInd) {
                return matrix.doubleValue(anInd);
            }

            public double doubleValue(final long aRow, final long aCol) {
                return matrix.doubleValue(aRow, aCol);
            }

            public BigDecimal get(final long index) {
                return this.get(AccessUtils.row(index, matrix.countRows()), AccessUtils.column(index, matrix.countRows()));
            }

            public BigDecimal get(final long aRow, final long aCol) {
                return matrix.toBigDecimal((int) aRow, (int) aCol);
            }

            public int size() {
                return (int) matrix.count();
            }

        };
    }

    public static Access2D<ComplexNumber> wrapComplexAccess2D(final BasicMatrix matrix) {
        return new Access2D<ComplexNumber>() {

            public long count() {
                return this.size();
            }

            public long countColumns() {
                return matrix.countColumns();
            }

            public long countRows() {
                return matrix.countRows();
            }

            public double doubleValue(final long anInd) {
                return matrix.doubleValue(anInd);
            }

            public double doubleValue(final long aRow, final long aCol) {
                return matrix.doubleValue(aRow, aCol);
            }

            public ComplexNumber get(final long index) {
                return this.get(AccessUtils.row(index, matrix.countRows()), AccessUtils.column(index, matrix.countRows()));
            }

            public ComplexNumber get(final long aRow, final long aCol) {
                return matrix.toComplexNumber((int) aRow, (int) aCol);
            }

            public int size() {
                return (int) matrix.count();
            }

        };
    }

    public static Access2D<Double> wrapPrimitiveAccess2D(final BasicMatrix matrix) {
        return new Access2D<Double>() {

            public long count() {
                return this.size();
            }

            public long countColumns() {
                return matrix.countColumns();
            }

            public long countRows() {
                return matrix.countRows();
            }

            public double doubleValue(final long anInd) {
                return matrix.doubleValue(anInd);
            }

            public double doubleValue(final long aRow, final long aCol) {
                return matrix.doubleValue(aRow, aCol);
            }

            public Double get(final long index) {
                return this.get(AccessUtils.row(index, matrix.countRows()), AccessUtils.column(index, matrix.countRows()));
            }

            public Double get(final long aRow, final long aCol) {
                return matrix.doubleValue(aRow, aCol);
            }

            public int size() {
                return (int) matrix.count();
            }

        };
    }

    private MatrixUtils() {
        super();
    }
}
