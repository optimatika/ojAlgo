/*
 * Copyright 1997-2018 Optimatika
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
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.FunctionUtils;
import org.ojalgo.matrix.store.ElementsConsumer;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.type.TypeUtils;

public abstract class MatrixUtils {

    public static void copy(final Access2D<?> source, final int rows, final int columns, final double[][] destination) {
        for (int i = 0; i < rows; i++) {
            final double[] tmpRow = destination[i];
            for (int j = 0; j < columns; j++) {
                tmpRow[j] = source.doubleValue(i, j);
            }
        }
    }

    /**
     * Copies the argument of the ComplexNumber elements to the destination.
     */
    public static void copyComplexArgument(final Access2D<ComplexNumber> source, final ElementsConsumer<?> destination) {
        final long tmpCount = FunctionUtils.min(source.count(), destination.count());
        for (long i = 0L; i < tmpCount; i++) {
            destination.set(i, source.get(i).getArgument());
        }
    }

    /**
     * Copies the imaginary part of the ComplexNumber elements to the destination.
     */
    public static void copyComplexImaginary(final Access2D<ComplexNumber> source, final ElementsConsumer<?> destination) {
        final long tmpCount = FunctionUtils.min(source.count(), destination.count());
        for (long i = 0L; i < tmpCount; i++) {
            destination.set(i, source.get(i).getImaginary());
        }
    }

    /**
     * Copies the modulus of the ComplexNumber elements to the destination.
     */
    public static void copyComplexModulus(final Access2D<ComplexNumber> source, final ElementsConsumer<?> destination) {
        final long tmpCount = FunctionUtils.min(source.count(), destination.count());
        for (long i = 0L; i < tmpCount; i++) {
            destination.set(i, source.get(i).getModulus());
        }
    }

    /**
     * Simultaneously copies the modulus and argument of the ComplexNumber elements to the destinations.
     */
    public static void copyComplexModulusAndArgument(final Access2D<ComplexNumber> source, final ElementsConsumer<?> modDest,
            final ElementsConsumer<?> argDest) {
        final long tmpCount = FunctionUtils.min(source.count(), modDest.count(), argDest.count());
        ComplexNumber tmpComplexNumber;
        for (long i = 0L; i < tmpCount; i++) {
            tmpComplexNumber = source.get(i);
            modDest.set(i, tmpComplexNumber.getModulus());
            argDest.set(i, tmpComplexNumber.getArgument());
        }
    }

    /**
     * Copies the real part of the ComplexNumber elements to the destination.
     */
    public static void copyComplexReal(final Access2D<ComplexNumber> source, final ElementsConsumer<?> destination) {
        final long tmpCount = FunctionUtils.min(source.count(), destination.count());
        for (long i = 0L; i < tmpCount; i++) {
            destination.set(i, source.get(i).getReal());
        }
    }

    /**
     * Simultaneously copies the real and imaginary parts of the ComplexNumber elements to the destinations.
     */
    public static void copyComplexRealAndImaginary(final Access2D<ComplexNumber> source, final ElementsConsumer<?> realDest,
            final ElementsConsumer<?> imagDest) {
        final long tmpCount = FunctionUtils.min(source.count(), realDest.count(), imagDest.count());
        ComplexNumber tmpComplexNumber;
        for (long i = 0L; i < tmpCount; i++) {
            tmpComplexNumber = source.get(i);
            realDest.set(i, tmpComplexNumber.getReal());
            imagDest.set(i, tmpComplexNumber.getImaginary());
        }
    }

    public static final int firstInColumn(final Access1D<?> matrix, final int col, final int defaultAndMinimum) {
        return matrix instanceof MatrixStore<?> ? Math.max(((MatrixStore<?>) matrix).firstInColumn(col), defaultAndMinimum) : defaultAndMinimum;
    }

    public static final long firstInColumn(final Access1D<?> matrix, final long col, final long defaultAndMinimum) {
        return matrix instanceof MatrixStore<?> ? Math.max(((MatrixStore<?>) matrix).firstInColumn((int) col), defaultAndMinimum) : defaultAndMinimum;
    }

    public static final int firstInRow(final Access1D<?> matrix, final int row, final int defaultAndMinimum) {
        return matrix instanceof MatrixStore<?> ? Math.max(((MatrixStore<?>) matrix).firstInRow(row), defaultAndMinimum) : defaultAndMinimum;
    }

    public static final long firstInRow(final Access1D<?> matrix, final long row, final long defaultAndMinimum) {
        return matrix instanceof MatrixStore<?> ? Math.max(((MatrixStore<?>) matrix).firstInRow((int) row), defaultAndMinimum) : defaultAndMinimum;
    }

    /**
     * Extracts the argument of the ComplexNumber elements to a new primitive double valued matrix.
     */
    public static PrimitiveDenseStore getComplexArgument(final Access2D<ComplexNumber> arg) {

        final long tmpRows = arg.countRows();
        final long tmpColumns = arg.countColumns();

        final PrimitiveDenseStore retVal = PrimitiveDenseStore.FACTORY.makeZero(tmpRows, tmpColumns);

        MatrixUtils.copyComplexArgument(arg, retVal);

        return retVal;
    }

    /**
     * Extracts the imaginary part of the ComplexNumber elements to a new primitive double valued matrix.
     */
    public static PrimitiveDenseStore getComplexImaginary(final Access2D<ComplexNumber> arg) {

        final long tmpRows = arg.countRows();
        final long tmpColumns = arg.countColumns();

        final PrimitiveDenseStore retVal = PrimitiveDenseStore.FACTORY.makeZero(tmpRows, tmpColumns);

        MatrixUtils.copyComplexImaginary(arg, retVal);

        return retVal;
    }

    /**
     * Extracts the modulus of the ComplexNumber elements to a new primitive double valued matrix.
     */
    public static PrimitiveDenseStore getComplexModulus(final Access2D<ComplexNumber> arg) {

        final long tmpRows = arg.countRows();
        final long tmpColumns = arg.countColumns();

        final PrimitiveDenseStore retVal = PrimitiveDenseStore.FACTORY.makeZero(tmpRows, tmpColumns);

        MatrixUtils.copyComplexModulus(arg, retVal);

        return retVal;
    }

    /**
     * Extracts the real part of the ComplexNumber elements to a new primitive double valued matrix.
     */
    public static PrimitiveDenseStore getComplexReal(final Access2D<ComplexNumber> arg) {

        final long tmpRows = arg.countRows();
        final long tmpColumns = arg.countColumns();

        final PrimitiveDenseStore retVal = PrimitiveDenseStore.FACTORY.makeZero(tmpRows, tmpColumns);

        MatrixUtils.copyComplexReal(arg, retVal);

        return retVal;
    }

    public static <N extends Number> int hashCode(final BasicMatrix matrix) {
        return Access1D.hashCode(matrix);
    }

    public static <N extends Number> int hashCode(final MatrixStore<N> matrix) {
        return Access1D.hashCode(matrix);
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
                retVal &= PrimitiveScalar.isSmall(PrimitiveMath.ONE, ComplexNumber.valueOf(matrix.get(j, j)).i);
                for (int i = j + 1; retVal && (i < tmpRowDim); i++) {
                    tmpLowerLeft = ComplexNumber.valueOf(matrix.get(i, j)).conjugate();
                    tmpUpperRight = ComplexNumber.valueOf(matrix.get(j, i));
                    retVal &= PrimitiveScalar.isSmall(PrimitiveMath.ONE, tmpLowerLeft.subtract(tmpUpperRight).norm());
                }
            }

        } else {

            for (int j = 0; retVal && (j < tmpColDim); j++) {
                for (int i = j + 1; retVal && (i < tmpRowDim); i++) {
                    retVal &= PrimitiveScalar.isSmall(PrimitiveMath.ONE, matrix.doubleValue(i, j) - matrix.doubleValue(j, i));
                }
            }
        }

        return retVal;
    }

    public static <N extends Number> boolean isNormal(final MatrixStore<N> matrix) {

        final MatrixStore<N> tmpConjugate = matrix.conjugate();

        return tmpConjugate.multiply(matrix).equals(matrix.multiply(tmpConjugate));
    }

    public static final int limitOfColumn(final Access1D<?> matrix, final int col, final int defaultAndMaximum) {
        return matrix instanceof MatrixStore<?> ? Math.min(((MatrixStore<?>) matrix).limitOfColumn(col), defaultAndMaximum) : defaultAndMaximum;
    }

    public static final long limitOfColumn(final Access1D<?> matrix, final long col, final long defaultAndMaximum) {
        return matrix instanceof MatrixStore<?> ? Math.min(((MatrixStore<?>) matrix).limitOfColumn((int) col), defaultAndMaximum) : defaultAndMaximum;
    }

    public static final int limitOfRow(final Access1D<?> matrix, final int row, final int defaultAndMaximum) {
        return matrix instanceof MatrixStore<?> ? Math.min(((MatrixStore<?>) matrix).limitOfRow(row), defaultAndMaximum) : defaultAndMaximum;
    }

    public static final long limitOfRow(final Access1D<?> matrix, final long row, final long defaultAndMaximum) {
        return matrix instanceof MatrixStore<?> ? Math.min(((MatrixStore<?>) matrix).limitOfRow((int) row), defaultAndMaximum) : defaultAndMaximum;
    }

    public static PhysicalStore<ComplexNumber> makeRandomComplexStore(final int aRowDim, final int aColDim) {

        final PhysicalStore<ComplexNumber> retVal = GenericDenseStore.COMPLEX.makeZero(aRowDim, aColDim);

        final Uniform tmpArgGen = new Uniform(PrimitiveMath.ZERO, PrimitiveMath.TWO_PI);

        for (int j = 0; j < aColDim; j++) {
            for (int i = 0; i < aRowDim; i++) {
                retVal.set(i, j, ComplexNumber.makePolar(PrimitiveMath.E, tmpArgGen.doubleValue()).add(PrimitiveMath.PI));
            }
        }

        return retVal;
    }

    /**
     * Make a random symmetric positive definite matrix
     */
    public static PrimitiveDenseStore makeSPD(final int dim) {

        final double[] tmpRandom = new double[dim];

        final PrimitiveDenseStore retVal = PrimitiveDenseStore.FACTORY.makeZero(dim, dim);

        for (int i = 0; i < dim; i++) {

            tmpRandom[i] = Math.random();

            for (int j = 0; j < i; j++) {
                retVal.set(i, j, tmpRandom[i] * tmpRandom[j]);
                retVal.set(j, i, tmpRandom[j] * tmpRandom[i]);
            }
            retVal.set(i, i, tmpRandom[i] + 1.0);
        }

        return retVal;
    }

    /**
     * @deprecated v45 Use {@link Access2D#toString(Access2D<?>)} instead
     */
    @Deprecated
    public static String toString(final Access2D<?> matrix) {
        return Access2D.toString(matrix);
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

            public double doubleValue(final long index) {
                return matrix.doubleValue(index);
            }

            public double doubleValue(final long row, final long col) {
                return matrix.doubleValue(row, col);
            }

            public BigDecimal get(final long row, final long col) {
                return TypeUtils.toBigDecimal(matrix.get((int) row, (int) col));
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

            public double doubleValue(final long index) {
                return matrix.doubleValue(index);
            }

            public double doubleValue(final long row, final long col) {
                return matrix.doubleValue(row, col);
            }

            public ComplexNumber get(final long row, final long col) {
                return ComplexNumber.valueOf(matrix.get((int) row, (int) col));
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

            public double doubleValue(final long index) {
                return matrix.doubleValue(index);
            }

            public double doubleValue(final long row, final long col) {
                return matrix.doubleValue(row, col);
            }

            public Double get(final long row, final long col) {
                return matrix.doubleValue(row, col);
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
