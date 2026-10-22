/*
 * Copyright 1997-2026 Optimatika
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
package org.ojalgo.matrix.operation;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.DOT;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;

/**
 * Products where the operands are any {@link Access1D}, typically not plain arrays: logical, structured or
 * sparse matrices. Each operand is read once, and only where its structure
 * ({@link Structure2D#firstInColumn(int)}, {@link Structure2D#limitOfColumn(int)} and the corresponding row
 * methods) allows non-zero elements. An {@link R064Store} operand or product is used in place. The general
 * case builds the columns of the product by AXPY of the columns of the left operand (copied unless it is an
 * {@link R064Store}), skipping zero elements of the right operand, and is multi-threaded, splitting the
 * columns of the product, when m*n*k is larger than {@link #THRESHOLD} cubed. Products with up to 10 rows use
 * kernels accumulating in registers.
 */
public class MultiplyBoth implements MatrixOperation {

    @FunctionalInterface
    public interface Generic<N extends Scalar<N>> extends TransformableRegion.FillByMultiplying<N> {

    }

    @FunctionalInterface
    public interface Primitive extends TransformableRegion.FillByMultiplying<Double> {

    }

    /**
     * The general case is multi-threaded, splitting the columns of the product, when m*n*k is larger than
     * this cubed. See {@link MatrixOperation#chunk(int, double)}.
     */
    static int THRESHOLD = 44;

    private static final DivideAndConquer.Divider DIVIDER = Parallelism.CORES.newDivider();
    /**
     * The zero of each element type, made by its no-argument constructor.
     */
    private static final ClassValue<Object> ZEROS = new ClassValue<>() {

        @Override
        protected Object computeValue(final Class<?> type) {
            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException cause) {
                throw new ProgrammingError(cause);
            }
        }

    };

    public static <N extends Scalar<N>> MultiplyBoth.Generic<N> newGeneric(final int rows, final int columns) {
        if (rows == 1) {
            return MultiplyBoth::fill1xN_G;
        }
        return MultiplyBoth::fillMxN_G;
    }

    public static MultiplyBoth.Primitive newPrimitive32(final int rows, final int columns) {
        return MultiplyBoth.newPrimitive64(rows, columns);
    }

    public static MultiplyBoth.Primitive newPrimitive64(final int rows, final int columns) {
        if (rows == 5 && columns == 5) {
            return MultiplyBoth::fill5x5_P64;
        }
        if (rows == 4 && columns == 4) {
            return MultiplyBoth::fill4x4_P64;
        }
        if (rows == 3 && columns == 3) {
            return MultiplyBoth::fill3x3_P64;
        }
        if (rows == 2 && columns == 2) {
            return MultiplyBoth::fill2x2_P64;
        }
        if (rows == 1) {
            return MultiplyBoth::fill1xN_P64;
        }
        return MultiplyBoth::fillMxN_P64;
    }

    /**
     * Not running code. Copies used as a starting point when coding various variants
     */
    private static void base(final TransformableRegion<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

        int nbRows = product.getRowDim();
        int nbCols = product.getColDim();

        for (int i = 0; i < nbRows; i++) {
            for (int c = 0; c < complexity; c++) {
                for (int j = 0; j < nbCols; j++) {
                    product.add(Structure2D.index(nbRows, i, j),
                            left.doubleValue(Structure2D.index(nbRows, i, c)) * right.doubleValue(Structure2D.index(complexity, c, j)));
                }
            }
        }
    }

    /**
     * The first index, along the complexity, where any of the first nbColumns columns of the matrix may have
     * a non-zero element.
     */
    private static int firstInColumns(final Access2D<?> matrix, final int nbColumns, final int complexity) {
        int retVal = complexity;
        for (int j = 0; j < nbColumns; j++) {
            retVal = Math.min(retVal, Structure2D.firstInColumn(matrix, j, 0));
        }
        return retVal;
    }

    /**
     * The first index, along the complexity, where any of the first nbRows rows of the matrix may have a
     * non-zero element.
     */
    private static int firstInRows(final Access2D<?> matrix, final int nbRows, final int complexity) {
        int retVal = complexity;
        for (int i = 0; i < nbRows; i++) {
            retVal = Math.min(retVal, Structure2D.firstInRow(matrix, i, 0));
        }
        return retVal;
    }

    /**
     * The limit, along the complexity, of where any of the first nbColumns columns of the matrix may have a
     * non-zero element.
     */
    private static int limitOfColumns(final Access2D<?> matrix, final int nbColumns, final int complexity) {
        int retVal = 0;
        for (int j = 0; j < nbColumns; j++) {
            retVal = Math.max(retVal, Structure2D.limitOfColumn(matrix, j, complexity));
        }
        return retVal;
    }

    /**
     * The limit, along the complexity, of where any of the first nbRows rows of the matrix may have a
     * non-zero element.
     */
    private static int limitOfRows(final Access2D<?> matrix, final int nbRows, final int complexity) {
        int retVal = 0;
        for (int i = 0; i < nbRows; i++) {
            retVal = Math.max(retVal, Structure2D.limitOfRow(matrix, i, complexity));
        }
        return retVal;
    }

    /**
     * The operand as a matrix with the given dimensions: the operand itself, if it is an {@link Access2D}
     * with exactly those dimensions, otherwise null. Only then are its structure, and its elements by row and
     * column, used; otherwise all its elements are read by (column-major) index.
     */
    @SuppressWarnings("unchecked")
    private static <N extends Comparable<N>> Access2D<N> matrix(final Access1D<N> operand, final int nbRows, final int nbCols) {
        if (operand instanceof Access2D<?> matrix && matrix.countRows() == nbRows && matrix.countColumns() == nbCols) {
            return (Access2D<N>) matrix;
        }
        return null;
    }

    /**
     * An array of the elements' type, filled with zero.
     */
    @SuppressWarnings("unchecked")
    private static <N extends Scalar<N>> N[] newZeros(final N zero, final int length) {
        N[] retVal = (N[]) Array.newInstance(zero.getClass(), length);
        Arrays.fill(retVal, zero);
        return retVal;
    }

    /**
     * The zero of the elements' type, from an element of the matrix.
     */
    @SuppressWarnings("unchecked")
    private static <N extends Scalar<N>> N zero(final Access1D<N> matrix) {
        return (N) ZEROS.get(matrix.get(0L).getClass());
    }

    /**
     * Columns firstColumn to columnLimit - 1 of the product, each built by AXPY of the columns of the left
     * operand, stored in {@code dataL} with their ranges in {@code firstL} and {@code limitL}, skipping zero
     * elements of the right operand. The right operand is read from {@code dataR} when that is not null, and
     * the product written to {@code dataP} when that is not null.
     */
    static void fillColumns_P64(final TransformableRegion<Double> product, final int firstColumn, final int columnLimit, final double[] dataL,
            final int[] firstL, final int[] limitL, final int complexity, final Access1D<Double> right, final double[] dataR, final double[] dataP) {

        int nbRows = product.getRowDim();
        double[] column = dataP != null ? dataP : new double[nbRows];
        Access2D<Double> mtrxR = MultiplyBoth.matrix(right, complexity, product.getColDim());

        for (int j = firstColumn; j < columnLimit; j++) {

            int base = dataP != null ? j * nbRows : 0;
            Arrays.fill(column, base, base + nbRows, PrimitiveMath.ZERO);

            int first = mtrxR != null ? Structure2D.firstInColumn(mtrxR, j, 0) : 0;
            int limit = mtrxR != null ? Structure2D.limitOfColumn(mtrxR, j, complexity) : complexity;
            int colR = j * complexity;

            for (int c = first; c < limit; c++) {
                double valR = dataR != null ? dataR[colR + c] : mtrxR != null ? mtrxR.doubleValue(c, j) : right.doubleValue(colR + c);
                if (valR != PrimitiveMath.ZERO) {
                    AXPY.invoke(column, base, valR, dataL, c * nbRows, firstL[c], limitL[c]);
                }
            }

            if (dataP == null) {
                for (int i = 0; i < nbRows; i++) {
                    product.set(i, j, column[i]);
                }
            }
        }
    }

    /**
     * @see #fillColumns_P64(TransformableRegion, int, int, double[], int[], int[], int, Access1D, double[],
     *      double[])
     */
    static <N extends Scalar<N>> void fillColumns_G(final TransformableRegion<N> product, final int firstColumn, final int columnLimit, final N[] dataL,
            final int[] firstL, final int[] limitL, final int complexity, final Access1D<N> right, final N[] dataR, final N[] dataP, final N zero) {

        int nbRows = product.getRowDim();
        N[] column = dataP != null ? dataP : MultiplyBoth.newZeros(zero, nbRows);
        Access2D<N> mtrxR = MultiplyBoth.matrix(right, complexity, product.getColDim());

        for (int j = firstColumn; j < columnLimit; j++) {

            int base = dataP != null ? j * nbRows : 0;
            Arrays.fill(column, base, base + nbRows, zero);

            int first = mtrxR != null ? Structure2D.firstInColumn(mtrxR, j, 0) : 0;
            int limit = mtrxR != null ? Structure2D.limitOfColumn(mtrxR, j, complexity) : complexity;
            int colR = j * complexity;

            for (int c = first; c < limit; c++) {
                N valR = dataR != null ? dataR[colR + c] : mtrxR != null ? mtrxR.get(c, j) : right.get(colR + c);
                int colL = c * nbRows;
                for (int i = firstL[c]; i < limitL[c]; i++) {
                    column[base + i] = column[base + i].add(dataL[colL + i].multiply(valR)).get();
                }
            }

            if (dataP == null) {
                for (int i = 0; i < nbRows; i++) {
                    product.set(i, j, column[i]);
                }
            }
        }
    }

    /**
     * A 1-by-n product: the row of the left operand, within its structure, copied once, and its dot product
     * with each column of the right operand, over the range where both may have non-zero elements.
     */
    static <N extends Scalar<N>> void fill1xN_G(final TransformableRegion<N> product, final Access1D<N> left, final int complexity, final Access1D<N> right) {

        int nbCols = product.getColDim();

        if (complexity == 0) {
            N zero = MultiplyBoth.zero(product);
            for (int j = 0; j < nbCols; j++) {
                product.set(0, j, zero);
            }
            return;
        }

        N zero = MultiplyBoth.zero(left);

        Access2D<N> mtrxL = MultiplyBoth.matrix(left, 1, complexity);
        int firstL = mtrxL != null ? Structure2D.firstInRow(mtrxL, 0, 0) : 0;
        int limitL = mtrxL != null ? Structure2D.limitOfRow(mtrxL, 0, complexity) : complexity;

        N[] row = MultiplyBoth.newZeros(zero, complexity);
        for (int c = firstL; c < limitL; c++) {
            row[c] = left.get(c);
        }

        Access2D<N> mtrxR = MultiplyBoth.matrix(right, complexity, nbCols);

        for (int j = 0; j < nbCols; j++) {
            int first = mtrxR != null ? Structure2D.firstInColumn(mtrxR, j, firstL) : firstL;
            int limit = mtrxR != null ? Structure2D.limitOfColumn(mtrxR, j, limitL) : limitL;
            Scalar<N> sum = zero;
            for (int c = first; c < limit; c++) {
                sum = sum.add(row[c].multiply(mtrxR != null ? mtrxR.get(c, j) : right.get(c + j * complexity)));
            }
            product.set(0, j, sum.get());
        }
    }

    /**
     * @see #fill1xN_G(TransformableRegion, Access1D, int, Access1D)
     */
    static void fill1xN_P64(final TransformableRegion<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

        int nbCols = product.getColDim();

        Access2D<Double> mtrxL = MultiplyBoth.matrix(left, 1, complexity);
        int firstL = mtrxL != null ? Structure2D.firstInRow(mtrxL, 0, 0) : 0;
        int limitL = mtrxL != null ? Structure2D.limitOfRow(mtrxL, 0, complexity) : complexity;

        double[] row = new double[complexity];
        for (int c = firstL; c < limitL; c++) {
            row[c] = left.doubleValue(c);
        }

        double[] dataR = right instanceof R064Store store ? store.data : null;
        Access2D<Double> mtrxR = MultiplyBoth.matrix(right, complexity, nbCols);

        for (int j = 0; j < nbCols; j++) {
            int first = mtrxR != null ? Structure2D.firstInColumn(mtrxR, j, firstL) : firstL;
            int limit = mtrxR != null ? Structure2D.limitOfColumn(mtrxR, j, limitL) : limitL;
            double sum = PrimitiveMath.ZERO;
            if (dataR != null) {
                sum = DOT.invoke(row, 0, dataR, j * complexity, first, limit);
            } else if (mtrxR != null) {
                for (int c = first; c < limit; c++) {
                    sum += row[c] * mtrxR.doubleValue(c, j);
                }
            } else {
                for (int c = first, index = j * complexity + first; c < limit; c++, index++) {
                    sum += row[c] * right.doubleValue(index);
                }
            }
            product.set(0, j, sum);
        }
    }

    /**
     * A 2-by-2 product, the operands read by row and column, over the range of the complexity where both may
     * have non-zero elements.
     */
    static void fill2x2_P64(final TransformableRegion<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

        Access2D<Double> mtrxL = MultiplyBoth.matrix(left, 2, complexity);
        Access2D<Double> mtrxR = MultiplyBoth.matrix(right, complexity, 2);

        if (mtrxL == null || mtrxR == null) {
            MultiplyBoth.fillMxN_P64(product, left, complexity, right);
            return;
        }

        int first = Math.max(MultiplyBoth.firstInRows(mtrxL, 2, complexity), MultiplyBoth.firstInColumns(mtrxR, 2, complexity));
        int limit = Math.min(MultiplyBoth.limitOfRows(mtrxL, 2, complexity), MultiplyBoth.limitOfColumns(mtrxR, 2, complexity));

        double tmp00 = PrimitiveMath.ZERO;
        double tmp10 = PrimitiveMath.ZERO;
        double tmp01 = PrimitiveMath.ZERO;
        double tmp11 = PrimitiveMath.ZERO;

        for (int c = first; c < limit; c++) {
            double tmpLeft0 = mtrxL.doubleValue(0, c);
            double tmpLeft1 = mtrxL.doubleValue(1, c);
            double tmpRight0 = mtrxR.doubleValue(c, 0);
            double tmpRight1 = mtrxR.doubleValue(c, 1);
            tmp00 += tmpLeft0 * tmpRight0;
            tmp10 += tmpLeft1 * tmpRight0;
            tmp01 += tmpLeft0 * tmpRight1;
            tmp11 += tmpLeft1 * tmpRight1;
        }

        product.set(0, 0, tmp00);
        product.set(1, 0, tmp10);
        product.set(0, 1, tmp01);
        product.set(1, 1, tmp11);
    }

    /**
     * A 3-by-3 product, the operands read by row and column, over the range of the complexity where both may
     * have non-zero elements.
     */
    static void fill3x3_P64(final TransformableRegion<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

        Access2D<Double> mtrxL = MultiplyBoth.matrix(left, 3, complexity);
        Access2D<Double> mtrxR = MultiplyBoth.matrix(right, complexity, 3);

        if (mtrxL == null || mtrxR == null) {
            MultiplyBoth.fillMxN_P64(product, left, complexity, right);
            return;
        }

        int first = Math.max(MultiplyBoth.firstInRows(mtrxL, 3, complexity), MultiplyBoth.firstInColumns(mtrxR, 3, complexity));
        int limit = Math.min(MultiplyBoth.limitOfRows(mtrxL, 3, complexity), MultiplyBoth.limitOfColumns(mtrxR, 3, complexity));

        double tmp00 = PrimitiveMath.ZERO;
        double tmp10 = PrimitiveMath.ZERO;
        double tmp20 = PrimitiveMath.ZERO;
        double tmp01 = PrimitiveMath.ZERO;
        double tmp11 = PrimitiveMath.ZERO;
        double tmp21 = PrimitiveMath.ZERO;
        double tmp02 = PrimitiveMath.ZERO;
        double tmp12 = PrimitiveMath.ZERO;
        double tmp22 = PrimitiveMath.ZERO;

        for (int c = first; c < limit; c++) {
            double tmpLeft0 = mtrxL.doubleValue(0, c);
            double tmpLeft1 = mtrxL.doubleValue(1, c);
            double tmpLeft2 = mtrxL.doubleValue(2, c);
            double tmpRight0 = mtrxR.doubleValue(c, 0);
            double tmpRight1 = mtrxR.doubleValue(c, 1);
            double tmpRight2 = mtrxR.doubleValue(c, 2);
            tmp00 += tmpLeft0 * tmpRight0;
            tmp10 += tmpLeft1 * tmpRight0;
            tmp20 += tmpLeft2 * tmpRight0;
            tmp01 += tmpLeft0 * tmpRight1;
            tmp11 += tmpLeft1 * tmpRight1;
            tmp21 += tmpLeft2 * tmpRight1;
            tmp02 += tmpLeft0 * tmpRight2;
            tmp12 += tmpLeft1 * tmpRight2;
            tmp22 += tmpLeft2 * tmpRight2;
        }

        product.set(0, 0, tmp00);
        product.set(1, 0, tmp10);
        product.set(2, 0, tmp20);
        product.set(0, 1, tmp01);
        product.set(1, 1, tmp11);
        product.set(2, 1, tmp21);
        product.set(0, 2, tmp02);
        product.set(1, 2, tmp12);
        product.set(2, 2, tmp22);
    }

    /**
     * A 4-by-4 product, the operands read by row and column, over the range of the complexity where both may
     * have non-zero elements.
     */
    static void fill4x4_P64(final TransformableRegion<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

        Access2D<Double> mtrxL = MultiplyBoth.matrix(left, 4, complexity);
        Access2D<Double> mtrxR = MultiplyBoth.matrix(right, complexity, 4);

        if (mtrxL == null || mtrxR == null) {
            MultiplyBoth.fillMxN_P64(product, left, complexity, right);
            return;
        }

        int first = Math.max(MultiplyBoth.firstInRows(mtrxL, 4, complexity), MultiplyBoth.firstInColumns(mtrxR, 4, complexity));
        int limit = Math.min(MultiplyBoth.limitOfRows(mtrxL, 4, complexity), MultiplyBoth.limitOfColumns(mtrxR, 4, complexity));

        double tmp00 = PrimitiveMath.ZERO;
        double tmp10 = PrimitiveMath.ZERO;
        double tmp20 = PrimitiveMath.ZERO;
        double tmp30 = PrimitiveMath.ZERO;
        double tmp01 = PrimitiveMath.ZERO;
        double tmp11 = PrimitiveMath.ZERO;
        double tmp21 = PrimitiveMath.ZERO;
        double tmp31 = PrimitiveMath.ZERO;
        double tmp02 = PrimitiveMath.ZERO;
        double tmp12 = PrimitiveMath.ZERO;
        double tmp22 = PrimitiveMath.ZERO;
        double tmp32 = PrimitiveMath.ZERO;
        double tmp03 = PrimitiveMath.ZERO;
        double tmp13 = PrimitiveMath.ZERO;
        double tmp23 = PrimitiveMath.ZERO;
        double tmp33 = PrimitiveMath.ZERO;

        for (int c = first; c < limit; c++) {
            double tmpLeft0 = mtrxL.doubleValue(0, c);
            double tmpLeft1 = mtrxL.doubleValue(1, c);
            double tmpLeft2 = mtrxL.doubleValue(2, c);
            double tmpLeft3 = mtrxL.doubleValue(3, c);
            double tmpRight0 = mtrxR.doubleValue(c, 0);
            double tmpRight1 = mtrxR.doubleValue(c, 1);
            double tmpRight2 = mtrxR.doubleValue(c, 2);
            double tmpRight3 = mtrxR.doubleValue(c, 3);
            tmp00 += tmpLeft0 * tmpRight0;
            tmp10 += tmpLeft1 * tmpRight0;
            tmp20 += tmpLeft2 * tmpRight0;
            tmp30 += tmpLeft3 * tmpRight0;
            tmp01 += tmpLeft0 * tmpRight1;
            tmp11 += tmpLeft1 * tmpRight1;
            tmp21 += tmpLeft2 * tmpRight1;
            tmp31 += tmpLeft3 * tmpRight1;
            tmp02 += tmpLeft0 * tmpRight2;
            tmp12 += tmpLeft1 * tmpRight2;
            tmp22 += tmpLeft2 * tmpRight2;
            tmp32 += tmpLeft3 * tmpRight2;
            tmp03 += tmpLeft0 * tmpRight3;
            tmp13 += tmpLeft1 * tmpRight3;
            tmp23 += tmpLeft2 * tmpRight3;
            tmp33 += tmpLeft3 * tmpRight3;
        }

        product.set(0, 0, tmp00);
        product.set(1, 0, tmp10);
        product.set(2, 0, tmp20);
        product.set(3, 0, tmp30);
        product.set(0, 1, tmp01);
        product.set(1, 1, tmp11);
        product.set(2, 1, tmp21);
        product.set(3, 1, tmp31);
        product.set(0, 2, tmp02);
        product.set(1, 2, tmp12);
        product.set(2, 2, tmp22);
        product.set(3, 2, tmp32);
        product.set(0, 3, tmp03);
        product.set(1, 3, tmp13);
        product.set(2, 3, tmp23);
        product.set(3, 3, tmp33);
    }

    /**
     * A 5-by-5 product, the operands read by row and column, over the range of the complexity where both may
     * have non-zero elements.
     */
    static void fill5x5_P64(final TransformableRegion<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

        Access2D<Double> mtrxL = MultiplyBoth.matrix(left, 5, complexity);
        Access2D<Double> mtrxR = MultiplyBoth.matrix(right, complexity, 5);

        if (mtrxL == null || mtrxR == null) {
            MultiplyBoth.fillMxN_P64(product, left, complexity, right);
            return;
        }

        int first = Math.max(MultiplyBoth.firstInRows(mtrxL, 5, complexity), MultiplyBoth.firstInColumns(mtrxR, 5, complexity));
        int limit = Math.min(MultiplyBoth.limitOfRows(mtrxL, 5, complexity), MultiplyBoth.limitOfColumns(mtrxR, 5, complexity));

        double tmp00 = PrimitiveMath.ZERO;
        double tmp10 = PrimitiveMath.ZERO;
        double tmp20 = PrimitiveMath.ZERO;
        double tmp30 = PrimitiveMath.ZERO;
        double tmp40 = PrimitiveMath.ZERO;
        double tmp01 = PrimitiveMath.ZERO;
        double tmp11 = PrimitiveMath.ZERO;
        double tmp21 = PrimitiveMath.ZERO;
        double tmp31 = PrimitiveMath.ZERO;
        double tmp41 = PrimitiveMath.ZERO;
        double tmp02 = PrimitiveMath.ZERO;
        double tmp12 = PrimitiveMath.ZERO;
        double tmp22 = PrimitiveMath.ZERO;
        double tmp32 = PrimitiveMath.ZERO;
        double tmp42 = PrimitiveMath.ZERO;
        double tmp03 = PrimitiveMath.ZERO;
        double tmp13 = PrimitiveMath.ZERO;
        double tmp23 = PrimitiveMath.ZERO;
        double tmp33 = PrimitiveMath.ZERO;
        double tmp43 = PrimitiveMath.ZERO;
        double tmp04 = PrimitiveMath.ZERO;
        double tmp14 = PrimitiveMath.ZERO;
        double tmp24 = PrimitiveMath.ZERO;
        double tmp34 = PrimitiveMath.ZERO;
        double tmp44 = PrimitiveMath.ZERO;

        for (int c = first; c < limit; c++) {
            double tmpLeft0 = mtrxL.doubleValue(0, c);
            double tmpLeft1 = mtrxL.doubleValue(1, c);
            double tmpLeft2 = mtrxL.doubleValue(2, c);
            double tmpLeft3 = mtrxL.doubleValue(3, c);
            double tmpLeft4 = mtrxL.doubleValue(4, c);
            double tmpRight0 = mtrxR.doubleValue(c, 0);
            double tmpRight1 = mtrxR.doubleValue(c, 1);
            double tmpRight2 = mtrxR.doubleValue(c, 2);
            double tmpRight3 = mtrxR.doubleValue(c, 3);
            double tmpRight4 = mtrxR.doubleValue(c, 4);
            tmp00 += tmpLeft0 * tmpRight0;
            tmp10 += tmpLeft1 * tmpRight0;
            tmp20 += tmpLeft2 * tmpRight0;
            tmp30 += tmpLeft3 * tmpRight0;
            tmp40 += tmpLeft4 * tmpRight0;
            tmp01 += tmpLeft0 * tmpRight1;
            tmp11 += tmpLeft1 * tmpRight1;
            tmp21 += tmpLeft2 * tmpRight1;
            tmp31 += tmpLeft3 * tmpRight1;
            tmp41 += tmpLeft4 * tmpRight1;
            tmp02 += tmpLeft0 * tmpRight2;
            tmp12 += tmpLeft1 * tmpRight2;
            tmp22 += tmpLeft2 * tmpRight2;
            tmp32 += tmpLeft3 * tmpRight2;
            tmp42 += tmpLeft4 * tmpRight2;
            tmp03 += tmpLeft0 * tmpRight3;
            tmp13 += tmpLeft1 * tmpRight3;
            tmp23 += tmpLeft2 * tmpRight3;
            tmp33 += tmpLeft3 * tmpRight3;
            tmp43 += tmpLeft4 * tmpRight3;
            tmp04 += tmpLeft0 * tmpRight4;
            tmp14 += tmpLeft1 * tmpRight4;
            tmp24 += tmpLeft2 * tmpRight4;
            tmp34 += tmpLeft3 * tmpRight4;
            tmp44 += tmpLeft4 * tmpRight4;
        }

        product.set(0, 0, tmp00);
        product.set(1, 0, tmp10);
        product.set(2, 0, tmp20);
        product.set(3, 0, tmp30);
        product.set(4, 0, tmp40);
        product.set(0, 1, tmp01);
        product.set(1, 1, tmp11);
        product.set(2, 1, tmp21);
        product.set(3, 1, tmp31);
        product.set(4, 1, tmp41);
        product.set(0, 2, tmp02);
        product.set(1, 2, tmp12);
        product.set(2, 2, tmp22);
        product.set(3, 2, tmp32);
        product.set(4, 2, tmp42);
        product.set(0, 3, tmp03);
        product.set(1, 3, tmp13);
        product.set(2, 3, tmp23);
        product.set(3, 3, tmp33);
        product.set(4, 3, tmp43);
        product.set(0, 4, tmp04);
        product.set(1, 4, tmp14);
        product.set(2, 4, tmp24);
        product.set(3, 4, tmp34);
        product.set(4, 4, tmp44);
    }

    /**
     * 2 rows, any number of columns: each column over the range of the complexity where both operands may
     * have non-zero elements.
     */
    static void fill2xN_P64(final TransformableRegion<Double> product, final Access2D<?> left, final int complexity, final Access2D<?> right) {

        int nbCols = product.getColDim();
        int firstL = MultiplyBoth.firstInRows(left, 2, complexity);
        int limitL = MultiplyBoth.limitOfRows(left, 2, complexity);

        for (int j = 0; j < nbCols; j++) {

            int first = Structure2D.firstInColumn(right, j, firstL);
            int limit = Structure2D.limitOfColumn(right, j, limitL);

            double tmp0J = PrimitiveMath.ZERO;
            double tmp1J = PrimitiveMath.ZERO;

            for (int c = first; c < limit; c++) {
                double tmpRightCJ = right.doubleValue(c, j);
                tmp0J += left.doubleValue(0, c) * tmpRightCJ;
                tmp1J += left.doubleValue(1, c) * tmpRightCJ;
            }

            product.set(0, j, tmp0J);
            product.set(1, j, tmp1J);
        }
    }

    /**
     * 3 rows, any number of columns: each column over the range of the complexity where both operands may
     * have non-zero elements.
     */
    static void fill3xN_P64(final TransformableRegion<Double> product, final Access2D<?> left, final int complexity, final Access2D<?> right) {

        int nbCols = product.getColDim();
        int firstL = MultiplyBoth.firstInRows(left, 3, complexity);
        int limitL = MultiplyBoth.limitOfRows(left, 3, complexity);

        for (int j = 0; j < nbCols; j++) {

            int first = Structure2D.firstInColumn(right, j, firstL);
            int limit = Structure2D.limitOfColumn(right, j, limitL);

            double tmp0J = PrimitiveMath.ZERO;
            double tmp1J = PrimitiveMath.ZERO;
            double tmp2J = PrimitiveMath.ZERO;

            for (int c = first; c < limit; c++) {
                double tmpRightCJ = right.doubleValue(c, j);
                tmp0J += left.doubleValue(0, c) * tmpRightCJ;
                tmp1J += left.doubleValue(1, c) * tmpRightCJ;
                tmp2J += left.doubleValue(2, c) * tmpRightCJ;
            }

            product.set(0, j, tmp0J);
            product.set(1, j, tmp1J);
            product.set(2, j, tmp2J);
        }
    }

    /**
     * 4 rows, any number of columns: each column over the range of the complexity where both operands may
     * have non-zero elements.
     */
    static void fill4xN_P64(final TransformableRegion<Double> product, final Access2D<?> left, final int complexity, final Access2D<?> right) {

        int nbCols = product.getColDim();
        int firstL = MultiplyBoth.firstInRows(left, 4, complexity);
        int limitL = MultiplyBoth.limitOfRows(left, 4, complexity);

        for (int j = 0; j < nbCols; j++) {

            int first = Structure2D.firstInColumn(right, j, firstL);
            int limit = Structure2D.limitOfColumn(right, j, limitL);

            double tmp0J = PrimitiveMath.ZERO;
            double tmp1J = PrimitiveMath.ZERO;
            double tmp2J = PrimitiveMath.ZERO;
            double tmp3J = PrimitiveMath.ZERO;

            for (int c = first; c < limit; c++) {
                double tmpRightCJ = right.doubleValue(c, j);
                tmp0J += left.doubleValue(0, c) * tmpRightCJ;
                tmp1J += left.doubleValue(1, c) * tmpRightCJ;
                tmp2J += left.doubleValue(2, c) * tmpRightCJ;
                tmp3J += left.doubleValue(3, c) * tmpRightCJ;
            }

            product.set(0, j, tmp0J);
            product.set(1, j, tmp1J);
            product.set(2, j, tmp2J);
            product.set(3, j, tmp3J);
        }
    }

    /**
     * 5 rows, any number of columns: each column over the range of the complexity where both operands may
     * have non-zero elements.
     */
    static void fill5xN_P64(final TransformableRegion<Double> product, final Access2D<?> left, final int complexity, final Access2D<?> right) {

        int nbCols = product.getColDim();
        int firstL = MultiplyBoth.firstInRows(left, 5, complexity);
        int limitL = MultiplyBoth.limitOfRows(left, 5, complexity);

        for (int j = 0; j < nbCols; j++) {

            int first = Structure2D.firstInColumn(right, j, firstL);
            int limit = Structure2D.limitOfColumn(right, j, limitL);

            double tmp0J = PrimitiveMath.ZERO;
            double tmp1J = PrimitiveMath.ZERO;
            double tmp2J = PrimitiveMath.ZERO;
            double tmp3J = PrimitiveMath.ZERO;
            double tmp4J = PrimitiveMath.ZERO;

            for (int c = first; c < limit; c++) {
                double tmpRightCJ = right.doubleValue(c, j);
                tmp0J += left.doubleValue(0, c) * tmpRightCJ;
                tmp1J += left.doubleValue(1, c) * tmpRightCJ;
                tmp2J += left.doubleValue(2, c) * tmpRightCJ;
                tmp3J += left.doubleValue(3, c) * tmpRightCJ;
                tmp4J += left.doubleValue(4, c) * tmpRightCJ;
            }

            product.set(0, j, tmp0J);
            product.set(1, j, tmp1J);
            product.set(2, j, tmp2J);
            product.set(3, j, tmp3J);
            product.set(4, j, tmp4J);
        }
    }

    /**
     * 6 rows, any number of columns: each column over the range of the complexity where both operands may
     * have non-zero elements.
     */
    static void fill6xN_P64(final TransformableRegion<Double> product, final Access2D<?> left, final int complexity, final Access2D<?> right) {

        int nbCols = product.getColDim();
        int firstL = MultiplyBoth.firstInRows(left, 6, complexity);
        int limitL = MultiplyBoth.limitOfRows(left, 6, complexity);

        for (int j = 0; j < nbCols; j++) {

            int first = Structure2D.firstInColumn(right, j, firstL);
            int limit = Structure2D.limitOfColumn(right, j, limitL);

            double tmp0J = PrimitiveMath.ZERO;
            double tmp1J = PrimitiveMath.ZERO;
            double tmp2J = PrimitiveMath.ZERO;
            double tmp3J = PrimitiveMath.ZERO;
            double tmp4J = PrimitiveMath.ZERO;
            double tmp5J = PrimitiveMath.ZERO;

            for (int c = first; c < limit; c++) {
                double tmpRightCJ = right.doubleValue(c, j);
                tmp0J += left.doubleValue(0, c) * tmpRightCJ;
                tmp1J += left.doubleValue(1, c) * tmpRightCJ;
                tmp2J += left.doubleValue(2, c) * tmpRightCJ;
                tmp3J += left.doubleValue(3, c) * tmpRightCJ;
                tmp4J += left.doubleValue(4, c) * tmpRightCJ;
                tmp5J += left.doubleValue(5, c) * tmpRightCJ;
            }

            product.set(0, j, tmp0J);
            product.set(1, j, tmp1J);
            product.set(2, j, tmp2J);
            product.set(3, j, tmp3J);
            product.set(4, j, tmp4J);
            product.set(5, j, tmp5J);
        }
    }

    /**
     * 7 rows, any number of columns: each column over the range of the complexity where both operands may
     * have non-zero elements.
     */
    static void fill7xN_P64(final TransformableRegion<Double> product, final Access2D<?> left, final int complexity, final Access2D<?> right) {

        int nbCols = product.getColDim();
        int firstL = MultiplyBoth.firstInRows(left, 7, complexity);
        int limitL = MultiplyBoth.limitOfRows(left, 7, complexity);

        for (int j = 0; j < nbCols; j++) {

            int first = Structure2D.firstInColumn(right, j, firstL);
            int limit = Structure2D.limitOfColumn(right, j, limitL);

            double tmp0J = PrimitiveMath.ZERO;
            double tmp1J = PrimitiveMath.ZERO;
            double tmp2J = PrimitiveMath.ZERO;
            double tmp3J = PrimitiveMath.ZERO;
            double tmp4J = PrimitiveMath.ZERO;
            double tmp5J = PrimitiveMath.ZERO;
            double tmp6J = PrimitiveMath.ZERO;

            for (int c = first; c < limit; c++) {
                double tmpRightCJ = right.doubleValue(c, j);
                tmp0J += left.doubleValue(0, c) * tmpRightCJ;
                tmp1J += left.doubleValue(1, c) * tmpRightCJ;
                tmp2J += left.doubleValue(2, c) * tmpRightCJ;
                tmp3J += left.doubleValue(3, c) * tmpRightCJ;
                tmp4J += left.doubleValue(4, c) * tmpRightCJ;
                tmp5J += left.doubleValue(5, c) * tmpRightCJ;
                tmp6J += left.doubleValue(6, c) * tmpRightCJ;
            }

            product.set(0, j, tmp0J);
            product.set(1, j, tmp1J);
            product.set(2, j, tmp2J);
            product.set(3, j, tmp3J);
            product.set(4, j, tmp4J);
            product.set(5, j, tmp5J);
            product.set(6, j, tmp6J);
        }
    }

    /**
     * 8 rows, any number of columns: each column over the range of the complexity where both operands may
     * have non-zero elements.
     */
    static void fill8xN_P64(final TransformableRegion<Double> product, final Access2D<?> left, final int complexity, final Access2D<?> right) {

        int nbCols = product.getColDim();
        int firstL = MultiplyBoth.firstInRows(left, 8, complexity);
        int limitL = MultiplyBoth.limitOfRows(left, 8, complexity);

        for (int j = 0; j < nbCols; j++) {

            int first = Structure2D.firstInColumn(right, j, firstL);
            int limit = Structure2D.limitOfColumn(right, j, limitL);

            double tmp0J = PrimitiveMath.ZERO;
            double tmp1J = PrimitiveMath.ZERO;
            double tmp2J = PrimitiveMath.ZERO;
            double tmp3J = PrimitiveMath.ZERO;
            double tmp4J = PrimitiveMath.ZERO;
            double tmp5J = PrimitiveMath.ZERO;
            double tmp6J = PrimitiveMath.ZERO;
            double tmp7J = PrimitiveMath.ZERO;

            for (int c = first; c < limit; c++) {
                double tmpRightCJ = right.doubleValue(c, j);
                tmp0J += left.doubleValue(0, c) * tmpRightCJ;
                tmp1J += left.doubleValue(1, c) * tmpRightCJ;
                tmp2J += left.doubleValue(2, c) * tmpRightCJ;
                tmp3J += left.doubleValue(3, c) * tmpRightCJ;
                tmp4J += left.doubleValue(4, c) * tmpRightCJ;
                tmp5J += left.doubleValue(5, c) * tmpRightCJ;
                tmp6J += left.doubleValue(6, c) * tmpRightCJ;
                tmp7J += left.doubleValue(7, c) * tmpRightCJ;
            }

            product.set(0, j, tmp0J);
            product.set(1, j, tmp1J);
            product.set(2, j, tmp2J);
            product.set(3, j, tmp3J);
            product.set(4, j, tmp4J);
            product.set(5, j, tmp5J);
            product.set(6, j, tmp6J);
            product.set(7, j, tmp7J);
        }
    }

    /**
     * 9 rows, any number of columns: each column over the range of the complexity where both operands may
     * have non-zero elements.
     */
    static void fill9xN_P64(final TransformableRegion<Double> product, final Access2D<?> left, final int complexity, final Access2D<?> right) {

        int nbCols = product.getColDim();
        int firstL = MultiplyBoth.firstInRows(left, 9, complexity);
        int limitL = MultiplyBoth.limitOfRows(left, 9, complexity);

        for (int j = 0; j < nbCols; j++) {

            int first = Structure2D.firstInColumn(right, j, firstL);
            int limit = Structure2D.limitOfColumn(right, j, limitL);

            double tmp0J = PrimitiveMath.ZERO;
            double tmp1J = PrimitiveMath.ZERO;
            double tmp2J = PrimitiveMath.ZERO;
            double tmp3J = PrimitiveMath.ZERO;
            double tmp4J = PrimitiveMath.ZERO;
            double tmp5J = PrimitiveMath.ZERO;
            double tmp6J = PrimitiveMath.ZERO;
            double tmp7J = PrimitiveMath.ZERO;
            double tmp8J = PrimitiveMath.ZERO;

            for (int c = first; c < limit; c++) {
                double tmpRightCJ = right.doubleValue(c, j);
                tmp0J += left.doubleValue(0, c) * tmpRightCJ;
                tmp1J += left.doubleValue(1, c) * tmpRightCJ;
                tmp2J += left.doubleValue(2, c) * tmpRightCJ;
                tmp3J += left.doubleValue(3, c) * tmpRightCJ;
                tmp4J += left.doubleValue(4, c) * tmpRightCJ;
                tmp5J += left.doubleValue(5, c) * tmpRightCJ;
                tmp6J += left.doubleValue(6, c) * tmpRightCJ;
                tmp7J += left.doubleValue(7, c) * tmpRightCJ;
                tmp8J += left.doubleValue(8, c) * tmpRightCJ;
            }

            product.set(0, j, tmp0J);
            product.set(1, j, tmp1J);
            product.set(2, j, tmp2J);
            product.set(3, j, tmp3J);
            product.set(4, j, tmp4J);
            product.set(5, j, tmp5J);
            product.set(6, j, tmp6J);
            product.set(7, j, tmp7J);
            product.set(8, j, tmp8J);
        }
    }

    /**
     * 10 rows, any number of columns: each column over the range of the complexity where both operands may
     * have non-zero elements.
     */
    static void fill0xN_P64(final TransformableRegion<Double> product, final Access2D<?> left, final int complexity, final Access2D<?> right) {

        int nbCols = product.getColDim();
        int firstL = MultiplyBoth.firstInRows(left, 10, complexity);
        int limitL = MultiplyBoth.limitOfRows(left, 10, complexity);

        for (int j = 0; j < nbCols; j++) {

            int first = Structure2D.firstInColumn(right, j, firstL);
            int limit = Structure2D.limitOfColumn(right, j, limitL);

            double tmp0J = PrimitiveMath.ZERO;
            double tmp1J = PrimitiveMath.ZERO;
            double tmp2J = PrimitiveMath.ZERO;
            double tmp3J = PrimitiveMath.ZERO;
            double tmp4J = PrimitiveMath.ZERO;
            double tmp5J = PrimitiveMath.ZERO;
            double tmp6J = PrimitiveMath.ZERO;
            double tmp7J = PrimitiveMath.ZERO;
            double tmp8J = PrimitiveMath.ZERO;
            double tmp9J = PrimitiveMath.ZERO;

            for (int c = first; c < limit; c++) {
                double tmpRightCJ = right.doubleValue(c, j);
                tmp0J += left.doubleValue(0, c) * tmpRightCJ;
                tmp1J += left.doubleValue(1, c) * tmpRightCJ;
                tmp2J += left.doubleValue(2, c) * tmpRightCJ;
                tmp3J += left.doubleValue(3, c) * tmpRightCJ;
                tmp4J += left.doubleValue(4, c) * tmpRightCJ;
                tmp5J += left.doubleValue(5, c) * tmpRightCJ;
                tmp6J += left.doubleValue(6, c) * tmpRightCJ;
                tmp7J += left.doubleValue(7, c) * tmpRightCJ;
                tmp8J += left.doubleValue(8, c) * tmpRightCJ;
                tmp9J += left.doubleValue(9, c) * tmpRightCJ;
            }

            product.set(0, j, tmp0J);
            product.set(1, j, tmp1J);
            product.set(2, j, tmp2J);
            product.set(3, j, tmp3J);
            product.set(4, j, tmp4J);
            product.set(5, j, tmp5J);
            product.set(6, j, tmp6J);
            product.set(7, j, tmp7J);
            product.set(8, j, tmp8J);
            product.set(9, j, tmp9J);
        }
    }

    /**
     * An m-by-1 product with the left operand and the product plain arrays: AXPY of the columns of the left
     * operand, over the range of the right column where it may have non-zero elements (when it is a k-by-1
     * matrix, mtrxR), skipping zeros.
     */
    static void fillMx1_P64(final double[] product, final double[] left, final int complexity, final Access1D<Double> right, final Access2D<Double> mtrxR) {

        int nbRows = product.length;

        Arrays.fill(product, PrimitiveMath.ZERO);

        int first = mtrxR != null ? Structure2D.firstInColumn(mtrxR, 0, 0) : 0;
        int limit = mtrxR != null ? Structure2D.limitOfColumn(mtrxR, 0, complexity) : complexity;

        for (int c = first; c < limit; c++) {
            double valR = right.doubleValue(c);
            if (valR != PrimitiveMath.ZERO) {
                AXPY.invoke(product, 0, valR, left, c * nbRows, 0, nbRows);
            }
        }
    }

    /**
     * The general case, see the class documentation, for other number types (without skipping zero elements).
     */
    static <N extends Scalar<N>> void fillMxN_G(final TransformableRegion<N> product, final Access1D<N> left, final int complexity, final Access1D<N> right) {

        int nbRows = product.getRowDim();
        int nbCols = product.getColDim();

        if (nbRows == 1) {
            MultiplyBoth.fill1xN_G(product, left, complexity, right);
            return;
        }

        if (complexity == 0) {
            N zero = MultiplyBoth.zero(product);
            for (int j = 0; j < nbCols; j++) {
                for (int i = 0; i < nbRows; i++) {
                    product.set(i, j, zero);
                }
            }
            return;
        }

        N zero = MultiplyBoth.zero(left);

        int[] firstL = new int[complexity];
        int[] limitL = new int[complexity];
        N[] dataL;

        if (left instanceof GenericStore<?> store) {
            dataL = (N[]) store.data;
            Arrays.fill(limitL, nbRows);
        } else {
            dataL = MultiplyBoth.newZeros(zero, nbRows * complexity);
            Access2D<N> mtrxL = MultiplyBoth.matrix(left, nbRows, complexity);
            for (int c = 0; c < complexity; c++) {
                int first = mtrxL != null ? Structure2D.firstInColumn(mtrxL, c, 0) : 0;
                int limit = mtrxL != null ? Math.max(first, Structure2D.limitOfColumn(mtrxL, c, nbRows)) : nbRows;
                firstL[c] = first;
                limitL[c] = limit;
                int colL = c * nbRows;
                for (int i = first; i < limit; i++) {
                    dataL[colL + i] = mtrxL != null ? mtrxL.get(i, c) : left.get(colL + i);
                }
            }
        }

        N[] dataR = right instanceof GenericStore<?> store ? (N[]) store.data : null;
        N[] dataP = product instanceof GenericStore<?> store ? (N[]) store.data : null;

        int chunk = MatrixOperation.chunk(THRESHOLD, nbRows, complexity);

        if (nbCols > chunk) {
            DIVIDER.threshold(chunk).divide(0, nbCols,
                    (first, limit) -> MultiplyBoth.fillColumns_G(product, first, limit, dataL, firstL, limitL, complexity, right, dataR, dataP, zero));
        } else {
            MultiplyBoth.fillColumns_G(product, 0, nbCols, dataL, firstL, limitL, complexity, right, dataR, dataP, zero);
        }
    }

    /**
     * The general case, see the class documentation. Products with 2 to 10 rows use kernels accumulating in
     * registers, unless the general case would be multi-threaded.
     */
    static void fillMxN_P64(final TransformableRegion<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

        int nbRows = product.getRowDim();
        int nbCols = product.getColDim();

        if (nbRows == 1) {
            MultiplyBoth.fill1xN_P64(product, left, complexity, right);
            return;
        }

        int chunk = MatrixOperation.chunk(THRESHOLD, nbRows, complexity);

        Access2D<Double> mtrxL = MultiplyBoth.matrix(left, nbRows, complexity);
        Access2D<Double> mtrxR = MultiplyBoth.matrix(right, complexity, nbCols);

        if (nbRows <= 10 && nbCols <= chunk && mtrxL != null && mtrxR != null) {
            switch (nbRows) {
                case 2  -> MultiplyBoth.fill2xN_P64(product, mtrxL, complexity, mtrxR);
                case 3  -> MultiplyBoth.fill3xN_P64(product, mtrxL, complexity, mtrxR);
                case 4  -> MultiplyBoth.fill4xN_P64(product, mtrxL, complexity, mtrxR);
                case 5  -> MultiplyBoth.fill5xN_P64(product, mtrxL, complexity, mtrxR);
                case 6  -> MultiplyBoth.fill6xN_P64(product, mtrxL, complexity, mtrxR);
                case 7  -> MultiplyBoth.fill7xN_P64(product, mtrxL, complexity, mtrxR);
                case 8  -> MultiplyBoth.fill8xN_P64(product, mtrxL, complexity, mtrxR);
                case 9  -> MultiplyBoth.fill9xN_P64(product, mtrxL, complexity, mtrxR);
                default -> MultiplyBoth.fill0xN_P64(product, mtrxL, complexity, mtrxR);
            }
            return;
        }

        if (nbCols == 1 && left instanceof R064Store storeL && product instanceof R064Store storeP) {
            MultiplyBoth.fillMx1_P64(storeP.data, storeL.data, complexity, right, mtrxR);
            return;
        }

        int[] firstL = new int[complexity];
        int[] limitL = new int[complexity];
        double[] dataL;

        if (left instanceof R064Store store) {
            dataL = store.data;
            Arrays.fill(limitL, nbRows);
        } else {
            dataL = new double[nbRows * complexity];
            for (int c = 0; c < complexity; c++) {
                int first = mtrxL != null ? Structure2D.firstInColumn(mtrxL, c, 0) : 0;
                int limit = mtrxL != null ? Math.max(first, Structure2D.limitOfColumn(mtrxL, c, nbRows)) : nbRows;
                firstL[c] = first;
                limitL[c] = limit;
                int colL = c * nbRows;
                for (int i = first; i < limit; i++) {
                    dataL[colL + i] = mtrxL != null ? mtrxL.doubleValue(i, c) : left.doubleValue(colL + i);
                }
            }
        }

        double[] dataR = right instanceof R064Store store ? store.data : null;
        double[] dataP = product instanceof R064Store store ? store.data : null;

        if (nbCols > chunk) {
            DIVIDER.threshold(chunk).divide(0, nbCols,
                    (first, limit) -> MultiplyBoth.fillColumns_P64(product, first, limit, dataL, firstL, limitL, complexity, right, dataR, dataP));
        } else {
            MultiplyBoth.fillColumns_P64(product, 0, nbCols, dataL, firstL, limitL, complexity, right, dataR, dataP);
        }
    }

}
