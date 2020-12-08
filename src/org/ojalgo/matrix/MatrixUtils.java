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
package org.ojalgo.matrix;

import java.math.BigDecimal;

import org.ojalgo.core.function.constant.PrimitiveMath;
import org.ojalgo.core.scalar.ComplexNumber;
import org.ojalgo.core.scalar.PrimitiveScalar;
import org.ojalgo.core.structure.Access1D;
import org.ojalgo.core.structure.Access2D;
import org.ojalgo.core.structure.Mutate1D;
import org.ojalgo.core.type.TypeUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.store.RawStore;

/**
 * @deprecated v47
 * @author apete
 */
@Deprecated
public abstract class MatrixUtils {

    /**
     * @deprecated v47 Use {@link RawStore#FACTORY} instead
     */
    @Deprecated
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
     *
     * @deprecated v47 Use {@link Mutate1D#copyComplexArgument(Access1D,Mutate1D)} instead
     */
    @Deprecated
    public static void copyComplexArgument(final Access1D<ComplexNumber> source, final Mutate1D destination) {
        Mutate1D.copyComplexArgument(source, destination);
    }

    /**
     * Copies the imaginary part of the ComplexNumber elements to the destination.
     *
     * @deprecated v47 Use {@link Mutate1D#copyComplexImaginary(Access1D,Mutate1D)} instead
     */
    @Deprecated
    public static void copyComplexImaginary(final Access1D<ComplexNumber> source, final Mutate1D destination) {
        Mutate1D.copyComplexImaginary(source, destination);
    }

    /**
     * Copies the modulus of the ComplexNumber elements to the destination.
     *
     * @deprecated v47 Use {@link Mutate1D#copyComplexModulus(Access1D,Mutate1D)} instead
     */
    @Deprecated
    public static void copyComplexModulus(final Access1D<ComplexNumber> source, final Mutate1D destination) {
        Mutate1D.copyComplexModulus(source, destination);
    }

    /**
     * Simultaneously copies the modulus and argument of the ComplexNumber elements to the destinations.
     *
     * @deprecated v47 Use {@link Mutate1D#copyComplexModulusAndArgument(Access1D,Mutate1D,Mutate1D)} instead
     */
    @Deprecated
    public static void copyComplexModulusAndArgument(final Access1D<ComplexNumber> source, final Mutate1D modDest, final Mutate1D argDest) {
        Mutate1D.copyComplexModulusAndArgument(source, modDest, argDest);
    }

    /**
     * Copies the real part of the ComplexNumber elements to the destination.
     *
     * @deprecated v47 Use {@link Mutate1D#copyComplexReal(Access1D,Mutate1D)} instead
     */
    @Deprecated
    public static void copyComplexReal(final Access1D<ComplexNumber> source, final Mutate1D destination) {
        Mutate1D.copyComplexReal(source, destination);
    }

    /**
     * Simultaneously copies the real and imaginary parts of the ComplexNumber elements to the destinations.
     *
     * @deprecated v47 Use {@link Mutate1D#copyComplexRealAndImaginary(Access1D,Mutate1D,Mutate1D)} instead
     */
    @Deprecated
    public static void copyComplexRealAndImaginary(final Access1D<ComplexNumber> source, final Mutate1D realDest, final Mutate1D imagDest) {
        Mutate1D.copyComplexRealAndImaginary(source, realDest, imagDest);
    }

    /**
     * @deprecated v47 Use {@link MatrixStore#firstInColumn(Access1D,int,int)} instead
     */
    @Deprecated
    public static final int firstInColumn(final Access1D<?> matrix, final int col, final int defaultAndMinimum) {
        return MatrixStore.firstInColumn(matrix, col, defaultAndMinimum);
    }

    /**
     * @deprecated v47 Use {@link MatrixStore#firstInColumn(Access1D,long,long)} instead
     */
    @Deprecated
    public static final long firstInColumn(final Access1D<?> matrix, final long col, final long defaultAndMinimum) {
        return MatrixStore.firstInColumn(matrix, col, defaultAndMinimum);
    }

    /**
     * @deprecated v47 Use {@link MatrixStore#firstInRow(Access1D,int,int)} instead
     */
    @Deprecated
    public static final int firstInRow(final Access1D<?> matrix, final int row, final int defaultAndMinimum) {
        return MatrixStore.firstInRow(matrix, row, defaultAndMinimum);
    }

    /**
     * @deprecated v47 Use {@link MatrixStore#firstInRow(Access1D,long,long)} instead
     */
    @Deprecated
    public static final long firstInRow(final Access1D<?> matrix, final long row, final long defaultAndMinimum) {
        return MatrixStore.firstInRow(matrix, row, defaultAndMinimum);
    }

    /**
     * Extracts the argument of the ComplexNumber elements to a new primitive double valued matrix.
     *
     * @deprecated v47 Use {@link Primitive64Store#getComplexArgument(Access2D)} instead
     */
    @Deprecated
    public static Primitive64Store getComplexArgument(final Access2D<ComplexNumber> arg) {
        return Primitive64Store.getComplexArgument(arg);
    }

    /**
     * Extracts the imaginary part of the ComplexNumber elements to a new primitive double valued matrix.
     *
     * @deprecated v47 Use {@link Primitive64Store#getComplexImaginary(Access2D)} instead
     */
    @Deprecated
    public static Primitive64Store getComplexImaginary(final Access2D<ComplexNumber> arg) {
        return Primitive64Store.getComplexImaginary(arg);
    }

    /**
     * Extracts the modulus of the ComplexNumber elements to a new primitive double valued matrix.
     *
     * @deprecated v47 Use {@link Primitive64Store#getComplexModulus(Access2D)} instead
     */
    @Deprecated
    public static Primitive64Store getComplexModulus(final Access2D<ComplexNumber> arg) {
        return Primitive64Store.getComplexModulus(arg);
    }

    /**
     * Extracts the real part of the ComplexNumber elements to a new primitive double valued matrix.
     *
     * @deprecated v47 Use {@link Primitive64Store#getComplexReal(Access2D)} instead
     */
    @Deprecated
    public static Primitive64Store getComplexReal(final Access2D<ComplexNumber> arg) {
        return Primitive64Store.getComplexReal(arg);
    }

    /**
     * @deprecated v47 Use {@link Access1D#hashCode()} instead.
     */
    @Deprecated
    public static <N extends Comparable<N>> int hashCode(final BasicMatrix<?, ?> matrix) {
        return Access1D.hashCode(matrix);
    }

    /**
     * @deprecated v47 Use {@link Access1D#hashCode()} instead.
     */
    @Deprecated
    public static <N extends Comparable<N>> int hashCode(final MatrixStore<N> matrix) {
        return Access1D.hashCode(matrix);
    }

    /**
     * @deprecated v47 Use {@link MatrixStore#isHermitian()} instead
     */
    @Deprecated
    public static boolean isHermitian(final Access2D<?> matrix) {

        final long tmpRowDim = matrix.countRows();
        final long tmpColDim = matrix.countColumns();

        final Comparable<?> tmpElement = matrix.get(0L);

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

    /**
     * @deprecated v47 {@link MatrixStore#isNormal()}
     */
    @Deprecated
    public static <N extends Comparable<N>> boolean isNormal(final MatrixStore<N> matrix) {
        return matrix.isNormal();
    }

    /**
     * @deprecated v47 Use {@link MatrixStore#limitOfColumn(Access1D,int,int)} instead
     */
    @Deprecated
    public static final int limitOfColumn(final Access1D<?> matrix, final int col, final int defaultAndMaximum) {
        return MatrixStore.limitOfColumn(matrix, col, defaultAndMaximum);
    }

    /**
     * @deprecated v47 Use {@link MatrixStore#limitOfColumn(Access1D,long,long)} instead
     */
    @Deprecated
    public static final long limitOfColumn(final Access1D<?> matrix, final long col, final long defaultAndMaximum) {
        return MatrixStore.limitOfColumn(matrix, col, defaultAndMaximum);
    }

    /**
     * @deprecated v47 Use {@link MatrixStore#limitOfRow(Access1D,int,int)} instead
     */
    @Deprecated
    public static final int limitOfRow(final Access1D<?> matrix, final int row, final int defaultAndMaximum) {
        return MatrixStore.limitOfRow(matrix, row, defaultAndMaximum);
    }

    /**
     * @deprecated v47 Use {@link MatrixStore#limitOfRow(Access1D,long,long)} instead
     */
    @Deprecated
    public static final long limitOfRow(final Access1D<?> matrix, final long row, final long defaultAndMaximum) {
        return MatrixStore.limitOfRow(matrix, row, defaultAndMaximum);
    }

    /**
     * Make a random symmetric positive definite matrix
     *
     * @deprecated v47 Use {@link org.ojalgo.matrix.store.PhysicalStore.Factory#makeSPD(int)} instead
     */
    @Deprecated
    public static Primitive64Store makeSPD(final int dim) {
        return Primitive64Store.FACTORY.makeSPD(dim);
    }

    /**
     * @deprecated v47 Use {@link MatrixStore#RATIONAL} instaed
     */
    @Deprecated
    public static Access2D<BigDecimal> wrapBigAccess2D(final BasicMatrix<?, ?> matrix) {
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

            @Override
            public final String toString() {
                return Access2D.toString(this);
            }

        };
    }

    /**
     * @deprecated v47 Use {@link MatrixStore#COMPLEX} instaed
     */
    @Deprecated
    public static Access2D<ComplexNumber> wrapComplexAccess2D(final BasicMatrix<?, ?> matrix) {
        return MatrixStore.COMPLEX.makeWrapper(matrix).get();
    }

    /**
     * @deprecated v47 Use {@link MatrixStore#PRIMITIVE64} instaed
     */
    @Deprecated
    public static Access2D<Double> wrapPrimitiveAccess2D(final BasicMatrix<?, ?> matrix) {
        return MatrixStore.PRIMITIVE64.makeWrapper(matrix).get();
    }

    private MatrixUtils() {
        super();
    }
}
