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

import java.util.Arrays;

import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.DOT;
import org.ojalgo.array.operation.FillAll;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.concurrent.DivideAndConquer.Conquerer;
import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.scalar.Scalar.Factory;

public class MultiplyNeither implements MatrixOperation {

    @FunctionalInterface
    public interface Generic<N extends Scalar<N>> {

        void invoke(N[] product, N[] left, boolean transpLeft, int complexity, N[] right, boolean transpRight, Scalar.Factory<N> scalar);

        default void invoke(final N[] product, final N[] left, final int complexity, final N[] right, final Scalar.Factory<N> scalar) {
            this.invoke(product, left, false, complexity, right, false, scalar);
        }

    }

    @FunctionalInterface
    public interface Primitive32 {

        void invoke(float[] product, float[] left, boolean transpLeft, int complexity, float[] right, boolean transpRight);

        default void invoke(final float[] product, final float[] left, final int complexity, final float[] right) {
            this.invoke(product, left, false, complexity, right, false);
        }

    }

    @FunctionalInterface
    public interface Primitive64 {

        void invoke(double[] product, double[] left, boolean transpLeft, int complexity, double[] right, boolean transpRight);

        default void invoke(final double[] product, final double[] left, final int complexity, final double[] right) {
            this.invoke(product, left, false, complexity, right, false);
        }

    }

    /**
     * The general case of the float and generic variants (and the double[][] variants) is multi-threaded,
     * splitting the columns (rows) of the product, when m*n*k is larger than this cubed. See
     * {@link MatrixOperation#chunk(int, double)}. The double[] variant is {@link GEMM}, with its own
     * threshold.
     */
    static int THRESHOLD = 44;

    private static final DivideAndConquer.Divider DIVIDER = Parallelism.CORES.newDivider();
    /**
     * Products with 5 to 10 rows use the row-specialised kernels, in Java, when the complexity is less than
     * this.
     */
    private static final int ROWS_COMPLEXITY = 140;
    /**
     * Products with 5 to 7 rows and the left operand transposed use the strided row-specialised kernels, in
     * Java, when the complexity is less than this.
     */
    private static final int ROWS_COMPLEXITY_TRANSPOSED = 10;

    public static void add(final double[][] product, final double[][] left, final double[][] right) {

        int chunk = MatrixOperation.chunk(THRESHOLD, (double) right.length * (right.length > 0 ? right[0].length : 0));

        MultiplyNeither.divide(0, product.length, chunk, (first, limit) -> {

            for (int i = first; i < limit; i++) {
                MultiplyNeither.doOneRow(product[i], left[i], right);
            }
        });
    }

    public static void fill(final double[][] product, final double[][] left, final double[][] right) {

        int chunk = MatrixOperation.chunk(THRESHOLD, (double) right.length * (right.length > 0 ? right[0].length : 0));

        MultiplyNeither.divide(0, product.length, chunk, (first, limit) -> {

            for (int i = first; i < limit; i++) {
                double[] prodRow = product[i];
                FillAll.fill(prodRow, 0, prodRow.length, 1, 0D);
                MultiplyNeither.doOneRow(prodRow, left[i], right);
            }
        });

    }

    public static <N extends Scalar<N>> MultiplyNeither.Generic<N> newGeneric(final long rows, final long columns) {
        if (columns == 1) {
            return MultiplyNeither::fillMx1;
        }
        if (rows == 1) {
            return MultiplyNeither::fill1xN;
        }
        return MultiplyNeither::fillMxN;
    }

    public static MultiplyNeither.Primitive32 newPrimitive32(final long rows, final long columns) {
        if (columns == 1) {
            return MultiplyNeither::fillMx1;
        }
        if (rows == 1) {
            return MultiplyNeither::fill1xN;
        }
        return MultiplyNeither::fillMxN;
    }

    public static MultiplyNeither.Primitive64 newPrimitive64(final long rows, final long columns) {
        if (rows == 4 && columns == 4) {
            return MultiplyNeither::fill4x4;
        }
        if (rows == 3 && columns == 3) {
            return MultiplyNeither::fill3x3;
        }
        if (rows == 2 && columns == 2) {
            return MultiplyNeither::fill2x2;
        }
        if (rows == 1 && columns == 1) {
            return MultiplyNeither::fill1x1;
        }
        if (columns == 1) {
            return MultiplyNeither::fillMx1;
        }
        if (rows == 1) {
            return MultiplyNeither::fill1xN;
        }
        return MultiplyNeither::fillMxN;
    }

    /**
     * Not running code. Copies used as a starting point when coding various variants
     */
    private static void base(final double[] product, final double[] left, final int complexity, final double[] right) {

        int nbRows = left.length / complexity;
        int nbCols = right.length / complexity;

        for (int i = 0; i < nbRows; i++) {
            for (int c = 0; c < complexity; c++) {
                for (int j = 0; j < nbCols; j++) {
                    product[i + j * nbRows] += left[i + c * nbRows] * right[c + j * complexity];
                }
            }
        }
    }

    private static void doOneRow(final double[] prodRow, final double[] leftRow, final double[][] right) {

        for (int c = 0, complexity = leftRow.length; c < complexity; c++) {
            AXPY.invoke(prodRow, leftRow[c], right[c]);
        }
    }

    static void addMx1(final float[] product, final float[] left, final int complexity, final float[] right) {

        int nbRows = product.length;

        for (int c = 0; c < complexity; c++) {
            AXPY.invoke(product, 0, right[c], left, c * nbRows, 0, nbRows);
        }
    }

    static <N extends Scalar<N>> void addMx1(final N[] product, final N[] left, final int complexity, final N[] right) {

        int nbRows = product.length;

        for (int c = 0; c < complexity; c++) {
            AXPY.invoke(product, 0, right[c], left, c * nbRows, 0, nbRows);
        }
    }

    static void addMxC(final float[] product, final int firstColumn, final int columnLimit, final float[] left, final int complexity, final float[] right) {

        int nbRows = left.length / complexity;

        for (int j = firstColumn; j < columnLimit; j++) {
            for (int c = 0; c < complexity; c++) {
                AXPY.invoke(product, j * nbRows, right[c + j * complexity], left, c * nbRows, 0, nbRows);
            }
        }
    }

    static <N extends Scalar<N>> void addMxC(final N[] product, final int firstColumn, final int columnLimit, final N[] left, final int complexity,
            final N[] right) {

        int nbRows = left.length / complexity;

        for (int j = firstColumn; j < columnLimit; j++) {
            for (int c = 0; c < complexity; c++) {
                AXPY.invoke(product, j * nbRows, right[c + j * complexity], left, c * nbRows, 0, nbRows);
            }
        }
    }

    static void divide(final int first, final int limit, final int chunk, final Conquerer conquerer) {
        DIVIDER.threshold(chunk).divide(first, limit, conquerer);
    }

    static void fill0xN(final double[] product, final double[] left, final int complexity, final double[] right) {

        int tmpRowDim = 10;
        int tmpColDim = right.length / complexity;

        for (int j = 0; j < tmpColDim; j++) {

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

            int tmpIndex = 0;
            for (int c = 0; c < complexity; c++) {
                double tmpRightCJ = right[c + j * complexity];
                tmp0J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp0J);
                tmp1J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp1J);
                tmp2J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp2J);
                tmp3J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp3J);
                tmp4J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp4J);
                tmp5J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp5J);
                tmp6J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp6J);
                tmp7J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp7J);
                tmp8J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp8J);
                tmp9J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp9J);
            }

            product[tmpIndex = j * tmpRowDim] = tmp0J;
            product[++tmpIndex] = tmp1J;
            product[++tmpIndex] = tmp2J;
            product[++tmpIndex] = tmp3J;
            product[++tmpIndex] = tmp4J;
            product[++tmpIndex] = tmp5J;
            product[++tmpIndex] = tmp6J;
            product[++tmpIndex] = tmp7J;
            product[++tmpIndex] = tmp8J;
            product[++tmpIndex] = tmp9J;
        }
    }

    /**
     * A row or column vector has the same layout transposed, so the transpose flags make no difference.
     */
    static void fill1x1(final double[] product, final double[] left, final boolean transpL, final int complexity, final double[] right, final boolean transpR) {

        double tmp00 = PrimitiveMath.ZERO;

        int nbRows = left.length / complexity;

        for (int c = 0; c < complexity; c++) {
            tmp00 += left[c * nbRows] * right[c];
        }

        product[0] = tmp00;
    }

    /**
     * {@link GEMV}: y = op(B)<sup>T</sup> * x, with x the row of op(A) (the same layout transposed).
     */
    static void fill1xN(final double[] product, final double[] left, final boolean transpL, final int complexity, final double[] right, final boolean transpR) {
        if (complexity == 0) {
            Arrays.fill(product, 0D);
        } else if (transpR) {
            GEMV.invoke(false, product.length, complexity, PrimitiveMath.ONE, right, 0, Math.max(1, product.length), left, 0, 1, PrimitiveMath.ZERO, product, 0,
                    1);
        } else {
            GEMV.invoke(true, complexity, product.length, PrimitiveMath.ONE, right, 0, complexity, left, 0, 1, PrimitiveMath.ZERO, product, 0, 1);
        }
    }

    /**
     * The row of op(A) has the same layout transposed. With op(B) transposed, the product is accumulated from
     * its rows.
     */
    static void fill1xN(final float[] product, final float[] left, final boolean transpL, final int complexity, final float[] right, final boolean transpR) {

        int nbCols = product.length;

        if (transpR) {
            Arrays.fill(product, 0F);
            for (int c = 0; c < complexity; c++) {
                AXPY.invoke(product, 0, left[c], right, c * nbCols, 0, nbCols);
            }
        } else {
            for (int j = 0; j < nbCols; j++) {
                product[j] = DOT.invoke(left, 0, right, j * complexity, 0, complexity);
            }
        }
    }

    /**
     * @see #fill1xN(float[], float[], boolean, int, float[], boolean)
     */
    static <N extends Scalar<N>> void fill1xN(final N[] product, final N[] left, final boolean transpL, final int complexity, final N[] right,
            final boolean transpR, final Factory<N> scalar) {

        int nbCols = product.length;

        if (transpR) {
            for (int j = 0; j < nbCols; j++) {
                Scalar<N> sum = scalar.zero();
                for (int c = 0; c < complexity; c++) {
                    sum = sum.add(left[c].multiply(right[j + c * nbCols]));
                }
                product[j] = sum.get();
            }
        } else {
            for (int j = 0; j < nbCols; j++) {
                product[j] = DOT.invoke(left, 0, right, j * complexity, 0, complexity, scalar);
            }
        }
    }

    static void fill2x2(final double[] product, final double[] left, final boolean transpL, final int complexity, final double[] right, final boolean transpR) {
        if (transpL || transpR) {
            MultiplyNeither.fill2x2(product, left, transpL ? complexity : 1, transpL ? 1 : 2, complexity, right, transpR ? 2 : 1, transpR ? 1 : complexity);
        } else {
            MultiplyNeither.fill2x2(product, left, complexity, right);
        }
    }

    static void fill2x2(final double[] product, final double[] left, final int complexity, final double[] right) {

        double tmp00 = PrimitiveMath.ZERO;
        double tmp10 = PrimitiveMath.ZERO;
        double tmp01 = PrimitiveMath.ZERO;
        double tmp11 = PrimitiveMath.ZERO;

        int tmpIndex;
        for (int c = 0; c < complexity; c++) {

            tmpIndex = c * 2;
            double tmpLeft0 = left[tmpIndex];
            tmpIndex++;
            double tmpLeft1 = left[tmpIndex];
            tmpIndex = c;
            double tmpRight0 = right[tmpIndex];
            tmpIndex += complexity;
            double tmpRight1 = right[tmpIndex];

            tmp00 = GEMM.JVM.fma(tmpLeft0, tmpRight0, tmp00);
            tmp10 = GEMM.JVM.fma(tmpLeft1, tmpRight0, tmp10);
            tmp01 = GEMM.JVM.fma(tmpLeft0, tmpRight1, tmp01);
            tmp11 = GEMM.JVM.fma(tmpLeft1, tmpRight1, tmp11);
        }

        product[0] = tmp00;
        product[1] = tmp10;
        product[2] = tmp01;
        product[3] = tmp11;
    }

    /**
     * As {@link #fill2x2(double[], double[], int, double[])}, with element (i,c) of op(A) in
     * {@code left[i * sAi + c * sAc]} and element (c,j) of op(B) in {@code right[c * sBc + j * sBj]}.
     */
    static void fill2x2(final double[] product, final double[] left, final int sAi, final int sAc, final int complexity, final double[] right, final int sBc,
            final int sBj) {

        double tmp00 = PrimitiveMath.ZERO;
        double tmp10 = PrimitiveMath.ZERO;
        double tmp01 = PrimitiveMath.ZERO;
        double tmp11 = PrimitiveMath.ZERO;

        for (int c = 0; c < complexity; c++) {

            int indexA = c * sAc;
            int indexB = c * sBc;
            double tmpLeft0 = left[indexA];
            double tmpLeft1 = left[indexA + sAi];
            double tmpRight0 = right[indexB];
            double tmpRight1 = right[indexB + sBj];

            tmp00 = GEMM.JVM.fma(tmpLeft0, tmpRight0, tmp00);
            tmp10 = GEMM.JVM.fma(tmpLeft1, tmpRight0, tmp10);
            tmp01 = GEMM.JVM.fma(tmpLeft0, tmpRight1, tmp01);
            tmp11 = GEMM.JVM.fma(tmpLeft1, tmpRight1, tmp11);
        }

        product[0] = tmp00;
        product[1] = tmp10;
        product[2] = tmp01;
        product[3] = tmp11;
    }

    static void fill3x3(final double[] product, final double[] left, final boolean transpL, final int complexity, final double[] right, final boolean transpR) {
        if (transpL || transpR) {
            MultiplyNeither.fill3x3(product, left, transpL ? complexity : 1, transpL ? 1 : 3, complexity, right, transpR ? 3 : 1, transpR ? 1 : complexity);
        } else {
            MultiplyNeither.fill3x3(product, left, complexity, right);
        }
    }

    static void fill3x3(final double[] product, final double[] left, final int complexity, final double[] right) {

        double tmp00 = PrimitiveMath.ZERO;
        double tmp10 = PrimitiveMath.ZERO;
        double tmp20 = PrimitiveMath.ZERO;
        double tmp01 = PrimitiveMath.ZERO;
        double tmp11 = PrimitiveMath.ZERO;
        double tmp21 = PrimitiveMath.ZERO;
        double tmp02 = PrimitiveMath.ZERO;
        double tmp12 = PrimitiveMath.ZERO;
        double tmp22 = PrimitiveMath.ZERO;

        int tmpIndex;
        for (int c = 0; c < complexity; c++) {

            tmpIndex = c * 3;
            double tmpLeft0 = left[tmpIndex];
            tmpIndex++;
            double tmpLeft1 = left[tmpIndex];
            tmpIndex++;
            double tmpLeft2 = left[tmpIndex];
            tmpIndex = c;
            double tmpRight0 = right[tmpIndex];
            tmpIndex += complexity;
            double tmpRight1 = right[tmpIndex];
            tmpIndex += complexity;
            double tmpRight2 = right[tmpIndex];

            tmp00 = GEMM.JVM.fma(tmpLeft0, tmpRight0, tmp00);
            tmp10 = GEMM.JVM.fma(tmpLeft1, tmpRight0, tmp10);
            tmp20 = GEMM.JVM.fma(tmpLeft2, tmpRight0, tmp20);
            tmp01 = GEMM.JVM.fma(tmpLeft0, tmpRight1, tmp01);
            tmp11 = GEMM.JVM.fma(tmpLeft1, tmpRight1, tmp11);
            tmp21 = GEMM.JVM.fma(tmpLeft2, tmpRight1, tmp21);
            tmp02 = GEMM.JVM.fma(tmpLeft0, tmpRight2, tmp02);
            tmp12 = GEMM.JVM.fma(tmpLeft1, tmpRight2, tmp12);
            tmp22 = GEMM.JVM.fma(tmpLeft2, tmpRight2, tmp22);
        }

        product[0] = tmp00;
        product[1] = tmp10;
        product[2] = tmp20;
        product[3] = tmp01;
        product[4] = tmp11;
        product[5] = tmp21;
        product[6] = tmp02;
        product[7] = tmp12;
        product[8] = tmp22;
    }

    /**
     * As {@link #fill3x3(double[], double[], int, double[])}, with element (i,c) of op(A) in
     * {@code left[i * sAi + c * sAc]} and element (c,j) of op(B) in {@code right[c * sBc + j * sBj]}.
     */
    static void fill3x3(final double[] product, final double[] left, final int sAi, final int sAc, final int complexity, final double[] right, final int sBc,
            final int sBj) {

        double tmp00 = PrimitiveMath.ZERO;
        double tmp10 = PrimitiveMath.ZERO;
        double tmp20 = PrimitiveMath.ZERO;
        double tmp01 = PrimitiveMath.ZERO;
        double tmp11 = PrimitiveMath.ZERO;
        double tmp21 = PrimitiveMath.ZERO;
        double tmp02 = PrimitiveMath.ZERO;
        double tmp12 = PrimitiveMath.ZERO;
        double tmp22 = PrimitiveMath.ZERO;

        for (int c = 0; c < complexity; c++) {

            int indexA = c * sAc;
            int indexB = c * sBc;
            double tmpLeft0 = left[indexA];
            double tmpLeft1 = left[indexA + sAi];
            double tmpLeft2 = left[indexA + 2 * sAi];
            double tmpRight0 = right[indexB];
            double tmpRight1 = right[indexB + sBj];
            double tmpRight2 = right[indexB + 2 * sBj];

            tmp00 = GEMM.JVM.fma(tmpLeft0, tmpRight0, tmp00);
            tmp10 = GEMM.JVM.fma(tmpLeft1, tmpRight0, tmp10);
            tmp20 = GEMM.JVM.fma(tmpLeft2, tmpRight0, tmp20);
            tmp01 = GEMM.JVM.fma(tmpLeft0, tmpRight1, tmp01);
            tmp11 = GEMM.JVM.fma(tmpLeft1, tmpRight1, tmp11);
            tmp21 = GEMM.JVM.fma(tmpLeft2, tmpRight1, tmp21);
            tmp02 = GEMM.JVM.fma(tmpLeft0, tmpRight2, tmp02);
            tmp12 = GEMM.JVM.fma(tmpLeft1, tmpRight2, tmp12);
            tmp22 = GEMM.JVM.fma(tmpLeft2, tmpRight2, tmp22);
        }

        product[0] = tmp00;
        product[1] = tmp10;
        product[2] = tmp20;
        product[3] = tmp01;
        product[4] = tmp11;
        product[5] = tmp21;
        product[6] = tmp02;
        product[7] = tmp12;
        product[8] = tmp22;
    }

    static void fill4x4(final double[] product, final double[] left, final boolean transpL, final int complexity, final double[] right, final boolean transpR) {
        if (transpL || transpR) {
            MultiplyNeither.fill4x4(product, left, transpL ? complexity : 1, transpL ? 1 : 4, complexity, right, transpR ? 4 : 1, transpR ? 1 : complexity);
        } else {
            MultiplyNeither.fill4x4(product, left, complexity, right);
        }
    }

    static void fill4x4(final double[] product, final double[] left, final int complexity, final double[] right) {

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

        int tmpIndex;
        for (int c = 0; c < complexity; c++) {

            tmpIndex = c * 4;
            double tmpLeft0 = left[tmpIndex];
            tmpIndex++;
            double tmpLeft1 = left[tmpIndex];
            tmpIndex++;
            double tmpLeft2 = left[tmpIndex];
            tmpIndex++;
            double tmpLeft3 = left[tmpIndex];
            tmpIndex = c;
            double tmpRight0 = right[tmpIndex];
            tmpIndex += complexity;
            double tmpRight1 = right[tmpIndex];
            tmpIndex += complexity;
            double tmpRight2 = right[tmpIndex];
            tmpIndex += complexity;
            double tmpRight3 = right[tmpIndex];

            tmp00 = GEMM.JVM.fma(tmpLeft0, tmpRight0, tmp00);
            tmp10 = GEMM.JVM.fma(tmpLeft1, tmpRight0, tmp10);
            tmp20 = GEMM.JVM.fma(tmpLeft2, tmpRight0, tmp20);
            tmp30 = GEMM.JVM.fma(tmpLeft3, tmpRight0, tmp30);
            tmp01 = GEMM.JVM.fma(tmpLeft0, tmpRight1, tmp01);
            tmp11 = GEMM.JVM.fma(tmpLeft1, tmpRight1, tmp11);
            tmp21 = GEMM.JVM.fma(tmpLeft2, tmpRight1, tmp21);
            tmp31 = GEMM.JVM.fma(tmpLeft3, tmpRight1, tmp31);
            tmp02 = GEMM.JVM.fma(tmpLeft0, tmpRight2, tmp02);
            tmp12 = GEMM.JVM.fma(tmpLeft1, tmpRight2, tmp12);
            tmp22 = GEMM.JVM.fma(tmpLeft2, tmpRight2, tmp22);
            tmp32 = GEMM.JVM.fma(tmpLeft3, tmpRight2, tmp32);
            tmp03 = GEMM.JVM.fma(tmpLeft0, tmpRight3, tmp03);
            tmp13 = GEMM.JVM.fma(tmpLeft1, tmpRight3, tmp13);
            tmp23 = GEMM.JVM.fma(tmpLeft2, tmpRight3, tmp23);
            tmp33 = GEMM.JVM.fma(tmpLeft3, tmpRight3, tmp33);
        }

        product[0] = tmp00;
        product[1] = tmp10;
        product[2] = tmp20;
        product[3] = tmp30;
        product[4] = tmp01;
        product[5] = tmp11;
        product[6] = tmp21;
        product[7] = tmp31;
        product[8] = tmp02;
        product[9] = tmp12;
        product[10] = tmp22;
        product[11] = tmp32;
        product[12] = tmp03;
        product[13] = tmp13;
        product[14] = tmp23;
        product[15] = tmp33;
    }

    /**
     * As {@link #fill4x4(double[], double[], int, double[])}, with element (i,c) of op(A) in
     * {@code left[i * sAi + c * sAc]} and element (c,j) of op(B) in {@code right[c * sBc + j * sBj]}.
     */
    static void fill4x4(final double[] product, final double[] left, final int sAi, final int sAc, final int complexity, final double[] right, final int sBc,
            final int sBj) {

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

        for (int c = 0; c < complexity; c++) {

            int indexA = c * sAc;
            int indexB = c * sBc;
            double tmpLeft0 = left[indexA];
            double tmpLeft1 = left[indexA + sAi];
            double tmpLeft2 = left[indexA + 2 * sAi];
            double tmpLeft3 = left[indexA + 3 * sAi];
            double tmpRight0 = right[indexB];
            double tmpRight1 = right[indexB + sBj];
            double tmpRight2 = right[indexB + 2 * sBj];
            double tmpRight3 = right[indexB + 3 * sBj];

            tmp00 = GEMM.JVM.fma(tmpLeft0, tmpRight0, tmp00);
            tmp10 = GEMM.JVM.fma(tmpLeft1, tmpRight0, tmp10);
            tmp20 = GEMM.JVM.fma(tmpLeft2, tmpRight0, tmp20);
            tmp30 = GEMM.JVM.fma(tmpLeft3, tmpRight0, tmp30);
            tmp01 = GEMM.JVM.fma(tmpLeft0, tmpRight1, tmp01);
            tmp11 = GEMM.JVM.fma(tmpLeft1, tmpRight1, tmp11);
            tmp21 = GEMM.JVM.fma(tmpLeft2, tmpRight1, tmp21);
            tmp31 = GEMM.JVM.fma(tmpLeft3, tmpRight1, tmp31);
            tmp02 = GEMM.JVM.fma(tmpLeft0, tmpRight2, tmp02);
            tmp12 = GEMM.JVM.fma(tmpLeft1, tmpRight2, tmp12);
            tmp22 = GEMM.JVM.fma(tmpLeft2, tmpRight2, tmp22);
            tmp32 = GEMM.JVM.fma(tmpLeft3, tmpRight2, tmp32);
            tmp03 = GEMM.JVM.fma(tmpLeft0, tmpRight3, tmp03);
            tmp13 = GEMM.JVM.fma(tmpLeft1, tmpRight3, tmp13);
            tmp23 = GEMM.JVM.fma(tmpLeft2, tmpRight3, tmp23);
            tmp33 = GEMM.JVM.fma(tmpLeft3, tmpRight3, tmp33);
        }

        product[0] = tmp00;
        product[1] = tmp10;
        product[2] = tmp20;
        product[3] = tmp30;
        product[4] = tmp01;
        product[5] = tmp11;
        product[6] = tmp21;
        product[7] = tmp31;
        product[8] = tmp02;
        product[9] = tmp12;
        product[10] = tmp22;
        product[11] = tmp32;
        product[12] = tmp03;
        product[13] = tmp13;
        product[14] = tmp23;
        product[15] = tmp33;
    }

    static void fill5xN(final double[] product, final double[] left, final int complexity, final double[] right) {

        int tmpRowDim = 5;
        int tmpColDim = right.length / complexity;

        for (int j = 0; j < tmpColDim; j++) {

            double tmp0J = PrimitiveMath.ZERO;
            double tmp1J = PrimitiveMath.ZERO;
            double tmp2J = PrimitiveMath.ZERO;
            double tmp3J = PrimitiveMath.ZERO;
            double tmp4J = PrimitiveMath.ZERO;

            int tmpIndex = 0;
            for (int c = 0; c < complexity; c++) {
                double tmpRightCJ = right[c + j * complexity];
                tmp0J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp0J);
                tmp1J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp1J);
                tmp2J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp2J);
                tmp3J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp3J);
                tmp4J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp4J);
            }

            product[tmpIndex = j * tmpRowDim] = tmp0J;
            product[++tmpIndex] = tmp1J;
            product[++tmpIndex] = tmp2J;
            product[++tmpIndex] = tmp3J;
            product[++tmpIndex] = tmp4J;
        }
    }

    /**
     * As {@link #fill5xN(double[], double[], int, double[])}, with element (i,c) of op(A) in
     * {@code left[i * sAi + c * sAc]} and element (c,j) of op(B) in {@code right[c * sBc + j * sBj]}.
     */
    static void fill5xN(final double[] product, final double[] left, final int sAi, final int sAc, final int complexity, final double[] right, final int sBc,
            final int sBj) {

        int nbCols = right.length / complexity;
        int sA2 = 2 * sAi;
        int sA3 = 3 * sAi;
        int sA4 = 4 * sAi;

        for (int j = 0; j < nbCols; j++) {

            double tmp0J = PrimitiveMath.ZERO;
            double tmp1J = PrimitiveMath.ZERO;
            double tmp2J = PrimitiveMath.ZERO;
            double tmp3J = PrimitiveMath.ZERO;
            double tmp4J = PrimitiveMath.ZERO;

            for (int c = 0, indexB = j * sBj; c < complexity; c++, indexB += sBc) {
                double tmpRightCJ = right[indexB];
                int indexA = c * sAc;
                tmp0J = GEMM.JVM.fma(left[indexA], tmpRightCJ, tmp0J);
                tmp1J = GEMM.JVM.fma(left[indexA + sAi], tmpRightCJ, tmp1J);
                tmp2J = GEMM.JVM.fma(left[indexA + sA2], tmpRightCJ, tmp2J);
                tmp3J = GEMM.JVM.fma(left[indexA + sA3], tmpRightCJ, tmp3J);
                tmp4J = GEMM.JVM.fma(left[indexA + sA4], tmpRightCJ, tmp4J);
            }

            int indexC = j * 5;
            product[indexC] = tmp0J;
            product[indexC + 1] = tmp1J;
            product[indexC + 2] = tmp2J;
            product[indexC + 3] = tmp3J;
            product[indexC + 4] = tmp4J;
        }
    }

    static void fill6xN(final double[] product, final double[] left, final int complexity, final double[] right) {

        int tmpRowDim = 6;
        int tmpColDim = right.length / complexity;

        for (int j = 0; j < tmpColDim; j++) {

            double tmp0J = PrimitiveMath.ZERO;
            double tmp1J = PrimitiveMath.ZERO;
            double tmp2J = PrimitiveMath.ZERO;
            double tmp3J = PrimitiveMath.ZERO;
            double tmp4J = PrimitiveMath.ZERO;
            double tmp5J = PrimitiveMath.ZERO;

            int tmpIndex = 0;
            for (int c = 0; c < complexity; c++) {
                double tmpRightCJ = right[c + j * complexity];
                tmp0J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp0J);
                tmp1J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp1J);
                tmp2J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp2J);
                tmp3J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp3J);
                tmp4J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp4J);
                tmp5J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp5J);
            }

            product[tmpIndex = j * tmpRowDim] = tmp0J;
            product[++tmpIndex] = tmp1J;
            product[++tmpIndex] = tmp2J;
            product[++tmpIndex] = tmp3J;
            product[++tmpIndex] = tmp4J;
            product[++tmpIndex] = tmp5J;
        }
    }

    /**
     * As {@link #fill6xN(double[], double[], int, double[])}, with element (i,c) of op(A) in
     * {@code left[i * sAi + c * sAc]} and element (c,j) of op(B) in {@code right[c * sBc + j * sBj]}.
     */
    static void fill6xN(final double[] product, final double[] left, final int sAi, final int sAc, final int complexity, final double[] right, final int sBc,
            final int sBj) {

        int nbCols = right.length / complexity;
        int sA2 = 2 * sAi;
        int sA3 = 3 * sAi;
        int sA4 = 4 * sAi;
        int sA5 = 5 * sAi;

        for (int j = 0; j < nbCols; j++) {

            double tmp0J = PrimitiveMath.ZERO;
            double tmp1J = PrimitiveMath.ZERO;
            double tmp2J = PrimitiveMath.ZERO;
            double tmp3J = PrimitiveMath.ZERO;
            double tmp4J = PrimitiveMath.ZERO;
            double tmp5J = PrimitiveMath.ZERO;

            for (int c = 0, indexB = j * sBj; c < complexity; c++, indexB += sBc) {
                double tmpRightCJ = right[indexB];
                int indexA = c * sAc;
                tmp0J = GEMM.JVM.fma(left[indexA], tmpRightCJ, tmp0J);
                tmp1J = GEMM.JVM.fma(left[indexA + sAi], tmpRightCJ, tmp1J);
                tmp2J = GEMM.JVM.fma(left[indexA + sA2], tmpRightCJ, tmp2J);
                tmp3J = GEMM.JVM.fma(left[indexA + sA3], tmpRightCJ, tmp3J);
                tmp4J = GEMM.JVM.fma(left[indexA + sA4], tmpRightCJ, tmp4J);
                tmp5J = GEMM.JVM.fma(left[indexA + sA5], tmpRightCJ, tmp5J);
            }

            int indexC = j * 6;
            product[indexC] = tmp0J;
            product[indexC + 1] = tmp1J;
            product[indexC + 2] = tmp2J;
            product[indexC + 3] = tmp3J;
            product[indexC + 4] = tmp4J;
            product[indexC + 5] = tmp5J;
        }
    }

    static void fill7xN(final double[] product, final double[] left, final int complexity, final double[] right) {

        int tmpRowDim = 7;
        int tmpColDim = right.length / complexity;

        for (int j = 0; j < tmpColDim; j++) {

            double tmp0J = PrimitiveMath.ZERO;
            double tmp1J = PrimitiveMath.ZERO;
            double tmp2J = PrimitiveMath.ZERO;
            double tmp3J = PrimitiveMath.ZERO;
            double tmp4J = PrimitiveMath.ZERO;
            double tmp5J = PrimitiveMath.ZERO;
            double tmp6J = PrimitiveMath.ZERO;

            int tmpIndex = 0;
            for (int c = 0; c < complexity; c++) {
                double tmpRightCJ = right[c + j * complexity];
                tmp0J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp0J);
                tmp1J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp1J);
                tmp2J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp2J);
                tmp3J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp3J);
                tmp4J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp4J);
                tmp5J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp5J);
                tmp6J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp6J);
            }

            product[tmpIndex = j * tmpRowDim] = tmp0J;
            product[++tmpIndex] = tmp1J;
            product[++tmpIndex] = tmp2J;
            product[++tmpIndex] = tmp3J;
            product[++tmpIndex] = tmp4J;
            product[++tmpIndex] = tmp5J;
            product[++tmpIndex] = tmp6J;
        }
    }

    /**
     * As {@link #fill7xN(double[], double[], int, double[])}, with element (i,c) of op(A) in
     * {@code left[i * sAi + c * sAc]} and element (c,j) of op(B) in {@code right[c * sBc + j * sBj]}.
     */
    static void fill7xN(final double[] product, final double[] left, final int sAi, final int sAc, final int complexity, final double[] right, final int sBc,
            final int sBj) {

        int nbCols = right.length / complexity;
        int sA2 = 2 * sAi;
        int sA3 = 3 * sAi;
        int sA4 = 4 * sAi;
        int sA5 = 5 * sAi;
        int sA6 = 6 * sAi;

        for (int j = 0; j < nbCols; j++) {

            double tmp0J = PrimitiveMath.ZERO;
            double tmp1J = PrimitiveMath.ZERO;
            double tmp2J = PrimitiveMath.ZERO;
            double tmp3J = PrimitiveMath.ZERO;
            double tmp4J = PrimitiveMath.ZERO;
            double tmp5J = PrimitiveMath.ZERO;
            double tmp6J = PrimitiveMath.ZERO;

            for (int c = 0, indexB = j * sBj; c < complexity; c++, indexB += sBc) {
                double tmpRightCJ = right[indexB];
                int indexA = c * sAc;
                tmp0J = GEMM.JVM.fma(left[indexA], tmpRightCJ, tmp0J);
                tmp1J = GEMM.JVM.fma(left[indexA + sAi], tmpRightCJ, tmp1J);
                tmp2J = GEMM.JVM.fma(left[indexA + sA2], tmpRightCJ, tmp2J);
                tmp3J = GEMM.JVM.fma(left[indexA + sA3], tmpRightCJ, tmp3J);
                tmp4J = GEMM.JVM.fma(left[indexA + sA4], tmpRightCJ, tmp4J);
                tmp5J = GEMM.JVM.fma(left[indexA + sA5], tmpRightCJ, tmp5J);
                tmp6J = GEMM.JVM.fma(left[indexA + sA6], tmpRightCJ, tmp6J);
            }

            int indexC = j * 7;
            product[indexC] = tmp0J;
            product[indexC + 1] = tmp1J;
            product[indexC + 2] = tmp2J;
            product[indexC + 3] = tmp3J;
            product[indexC + 4] = tmp4J;
            product[indexC + 5] = tmp5J;
            product[indexC + 6] = tmp6J;
        }
    }

    static void fill8xN(final double[] product, final double[] left, final int complexity, final double[] right) {

        int tmpRowDim = 8;
        int tmpColDim = right.length / complexity;

        for (int j = 0; j < tmpColDim; j++) {

            double tmp0J = PrimitiveMath.ZERO;
            double tmp1J = PrimitiveMath.ZERO;
            double tmp2J = PrimitiveMath.ZERO;
            double tmp3J = PrimitiveMath.ZERO;
            double tmp4J = PrimitiveMath.ZERO;
            double tmp5J = PrimitiveMath.ZERO;
            double tmp6J = PrimitiveMath.ZERO;
            double tmp7J = PrimitiveMath.ZERO;

            int tmpIndex = 0;
            for (int c = 0; c < complexity; c++) {
                double tmpRightCJ = right[c + j * complexity];
                tmp0J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp0J);
                tmp1J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp1J);
                tmp2J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp2J);
                tmp3J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp3J);
                tmp4J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp4J);
                tmp5J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp5J);
                tmp6J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp6J);
                tmp7J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp7J);
            }

            product[tmpIndex = j * tmpRowDim] = tmp0J;
            product[++tmpIndex] = tmp1J;
            product[++tmpIndex] = tmp2J;
            product[++tmpIndex] = tmp3J;
            product[++tmpIndex] = tmp4J;
            product[++tmpIndex] = tmp5J;
            product[++tmpIndex] = tmp6J;
            product[++tmpIndex] = tmp7J;
        }
    }

    static void fill9xN(final double[] product, final double[] left, final int complexity, final double[] right) {

        int tmpRowDim = 9;
        int tmpColDim = right.length / complexity;

        for (int j = 0; j < tmpColDim; j++) {

            double tmp0J = PrimitiveMath.ZERO;
            double tmp1J = PrimitiveMath.ZERO;
            double tmp2J = PrimitiveMath.ZERO;
            double tmp3J = PrimitiveMath.ZERO;
            double tmp4J = PrimitiveMath.ZERO;
            double tmp5J = PrimitiveMath.ZERO;
            double tmp6J = PrimitiveMath.ZERO;
            double tmp7J = PrimitiveMath.ZERO;
            double tmp8J = PrimitiveMath.ZERO;

            int tmpIndex = 0;
            for (int c = 0; c < complexity; c++) {
                double tmpRightCJ = right[c + j * complexity];
                tmp0J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp0J);
                tmp1J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp1J);
                tmp2J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp2J);
                tmp3J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp3J);
                tmp4J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp4J);
                tmp5J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp5J);
                tmp6J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp6J);
                tmp7J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp7J);
                tmp8J = GEMM.JVM.fma(left[tmpIndex++], tmpRightCJ, tmp8J);
            }

            product[tmpIndex = j * tmpRowDim] = tmp0J;
            product[++tmpIndex] = tmp1J;
            product[++tmpIndex] = tmp2J;
            product[++tmpIndex] = tmp3J;
            product[++tmpIndex] = tmp4J;
            product[++tmpIndex] = tmp5J;
            product[++tmpIndex] = tmp6J;
            product[++tmpIndex] = tmp7J;
            product[++tmpIndex] = tmp8J;
        }
    }

    /**
     * {@link GEMV}: y = op(A) * x, with x the column of op(B) (the same layout transposed).
     */
    static void fillMx1(final double[] product, final double[] left, final boolean transpL, final int complexity, final double[] right, final boolean transpR) {
        if (complexity == 0) {
            Arrays.fill(product, 0D);
        } else if (transpL) {
            GEMV.invoke(true, complexity, product.length, PrimitiveMath.ONE, left, 0, complexity, right, 0, 1, PrimitiveMath.ZERO, product, 0, 1);
        } else {
            GEMV.invoke(false, product.length, complexity, PrimitiveMath.ONE, left, 0, Math.max(1, product.length), right, 0, 1, PrimitiveMath.ZERO, product, 0,
                    1);
        }
    }

    /**
     * The column of op(B) has the same layout transposed. With op(A) transposed, each element is the dot
     * product of a column of A and that column.
     */
    static void fillMx1(final float[] product, final float[] left, final boolean transpL, final int complexity, final float[] right, final boolean transpR) {

        if (transpL) {
            for (int i = 0, nbRows = product.length; i < nbRows; i++) {
                product[i] = DOT.invoke(left, i * complexity, right, 0, 0, complexity);
            }
        } else {
            Arrays.fill(product, 0F);
            MultiplyNeither.addMx1(product, left, complexity, right);
        }
    }

    /**
     * @see #fillMx1(float[], float[], boolean, int, float[], boolean)
     */
    static <N extends Scalar<N>> void fillMx1(final N[] product, final N[] left, final boolean transpL, final int complexity, final N[] right,
            final boolean transpR, final Factory<N> scalar) {

        if (transpL) {
            for (int i = 0, nbRows = product.length; i < nbRows; i++) {
                product[i] = DOT.invoke(left, i * complexity, right, 0, 0, complexity, scalar);
            }
        } else {
            Arrays.fill(product, scalar.zero().get());
            MultiplyNeither.addMx1(product, left, complexity, right);
        }
    }

    /**
     * The columns firstColumn to columnLimit - 1 of a product with a transposed operand. With only op(B)
     * transposed, each column is accumulated from the columns of A (AXPY). With op(A) transposed, each
     * element is the dot product of a column of A and the column of op(B), copied when op(B) is transposed.
     */
    static void fillMxC(final float[] product, final int firstColumn, final int columnLimit, final float[] left, final boolean transpL, final int complexity,
            final float[] right, final boolean transpR) {

        int nbRows = left.length / complexity;
        int nbCols = right.length / complexity;

        if (transpL) {

            float[] column = transpR ? new float[complexity] : right;

            for (int j = firstColumn; j < columnLimit; j++) {
                int baseB = j * complexity;
                if (transpR) {
                    for (int c = 0; c < complexity; c++) {
                        column[c] = right[j + c * nbCols];
                    }
                    baseB = 0;
                }
                for (int i = 0; i < nbRows; i++) {
                    product[i + j * nbRows] = DOT.invoke(left, i * complexity, column, baseB, 0, complexity);
                }
            }

        } else {

            for (int j = firstColumn; j < columnLimit; j++) {
                Arrays.fill(product, j * nbRows, (j + 1) * nbRows, 0F);
                for (int c = 0; c < complexity; c++) {
                    AXPY.invoke(product, j * nbRows, right[j + c * nbCols], left, c * nbRows, 0, nbRows);
                }
            }
        }
    }

    /**
     * @see #fillMxC(float[], int, int, float[], boolean, int, float[], boolean)
     */
    static <N extends Scalar<N>> void fillMxC(final N[] product, final int firstColumn, final int columnLimit, final N[] left, final boolean transpL,
            final int complexity, final N[] right, final boolean transpR, final Factory<N> scalar) {

        int nbRows = left.length / complexity;
        int nbCols = right.length / complexity;

        if (transpL) {

            N[] column = transpR ? scalar.newArrayInstance(complexity) : right;

            for (int j = firstColumn; j < columnLimit; j++) {
                int baseB = j * complexity;
                if (transpR) {
                    for (int c = 0; c < complexity; c++) {
                        column[c] = right[j + c * nbCols];
                    }
                    baseB = 0;
                }
                for (int i = 0; i < nbRows; i++) {
                    product[i + j * nbRows] = DOT.invoke(left, i * complexity, column, baseB, 0, complexity, scalar);
                }
            }

        } else {

            N zero = scalar.zero().get();

            for (int j = firstColumn; j < columnLimit; j++) {
                Arrays.fill(product, j * nbRows, (j + 1) * nbRows, zero);
                for (int c = 0; c < complexity; c++) {
                    AXPY.invoke(product, j * nbRows, right[j + c * nbCols], left, c * nbRows, 0, nbRows);
                }
            }
        }
    }

    /**
     * The general case: {@link GEMM}, native or Java (and then possibly multi-threaded or cache blocked). In
     * Java, products with 5 to 10 rows use row-specialised kernels when the complexity is less than
     * {@link #ROWS_COMPLEXITY} or there are fewer than 4 columns, unless {@link GEMM} would be
     * multi-threaded; with a transposed operand only products with 5 to 7 rows do, and with the left operand
     * transposed only when the complexity is less than {@link #ROWS_COMPLEXITY_TRANSPOSED}. Otherwise, when
     * the left operand is not transposed, the product is cleared once and accumulated into with beta 1, which
     * is faster at small sizes than clearing column by column.
     */
    static void fillMxN(final double[] product, final double[] left, final boolean transpL, final int complexity, final double[] right, final boolean transpR) {

        int nbRows = left.length / complexity;
        int nbCols = right.length / complexity;
        int lda = Math.max(1, transpL ? complexity : nbRows);
        int ldb = Math.max(1, transpR ? nbCols : complexity);

        boolean rows = nbRows >= 5 && nbCols <= MatrixOperation.chunk(GEMM.THRESHOLD, nbRows, complexity);

        if (GEMM.isNative(nbRows, nbCols, complexity)) {
            // Through GEMM's dispatch, that also decides between a critical and an off-heap native call
            GEMM.invoke(transpL, transpR, nbRows, nbCols, complexity, PrimitiveMath.ONE, left, 0, lda, right, 0, ldb, PrimitiveMath.ZERO, product, 0, nbRows);
        } else if (rows && !transpL && !transpR && nbRows <= 10 && (complexity < ROWS_COMPLEXITY || nbCols < 4)) {
            switch (nbRows) {
                case 5  -> MultiplyNeither.fill5xN(product, left, complexity, right);
                case 6  -> MultiplyNeither.fill6xN(product, left, complexity, right);
                case 7  -> MultiplyNeither.fill7xN(product, left, complexity, right);
                case 8  -> MultiplyNeither.fill8xN(product, left, complexity, right);
                case 9  -> MultiplyNeither.fill9xN(product, left, complexity, right);
                default -> MultiplyNeither.fill0xN(product, left, complexity, right);
            }
        } else if (rows && nbRows <= 7 && (transpL ? complexity < ROWS_COMPLEXITY_TRANSPOSED : complexity < ROWS_COMPLEXITY || nbCols < 4)) {
            int sAi = transpL ? complexity : 1;
            int sAc = transpL ? 1 : nbRows;
            int sBc = transpR ? nbCols : 1;
            int sBj = transpR ? 1 : complexity;
            switch (nbRows) {
                case 5  -> MultiplyNeither.fill5xN(product, left, sAi, sAc, complexity, right, sBc, sBj);
                case 6  -> MultiplyNeither.fill6xN(product, left, sAi, sAc, complexity, right, sBc, sBj);
                default -> MultiplyNeither.fill7xN(product, left, sAi, sAc, complexity, right, sBc, sBj);
            }
        } else if (transpL) {
            GEMM.JVM.invoke(transpL, transpR, nbRows, nbCols, complexity, PrimitiveMath.ONE, left, 0, lda, right, 0, ldb, PrimitiveMath.ZERO, product, 0,
                    nbRows);
        } else {
            Arrays.fill(product, 0D);
            GEMM.JVM.invoke(false, transpR, nbRows, nbCols, complexity, PrimitiveMath.ONE, left, 0, lda, right, 0, ldb, PrimitiveMath.ONE, product, 0, nbRows);
        }
    }

    /**
     * The columns of the product are split over several threads when m*n*k is larger than {@link #THRESHOLD}
     * cubed. With a transposed operand see
     * {@link #fillMxC(float[], int, int, float[], boolean, int, float[], boolean)}.
     */
    static void fillMxN(final float[] product, final float[] left, final boolean transpL, final int complexity, final float[] right, final boolean transpR) {

        int nbRows = left.length / complexity;
        int nbCols = right.length / complexity;
        int chunk = MatrixOperation.chunk(THRESHOLD, nbRows, complexity);

        if (transpL || transpR) {

            if (nbCols > chunk) {
                MultiplyNeither.divide(0, nbCols, chunk, (f, l) -> MultiplyNeither.fillMxC(product, f, l, left, transpL, complexity, right, transpR));
            } else {
                MultiplyNeither.fillMxC(product, 0, nbCols, left, transpL, complexity, right, transpR);
            }

        } else {

            Arrays.fill(product, 0F);

            if (nbCols > chunk) {
                MultiplyNeither.divide(0, nbCols, chunk, (f, l) -> MultiplyNeither.addMxC(product, f, l, left, complexity, right));
            } else {
                MultiplyNeither.addMxC(product, 0, nbCols, left, complexity, right);
            }
        }
    }

    /**
     * @see #fillMxN(float[], float[], boolean, int, float[], boolean)
     */
    static <N extends Scalar<N>> void fillMxN(final N[] product, final N[] left, final boolean transpL, final int complexity, final N[] right,
            final boolean transpR, final Factory<N> scalar) {

        int nbRows = left.length / complexity;
        int nbCols = right.length / complexity;
        int chunk = MatrixOperation.chunk(THRESHOLD, nbRows, complexity);

        if (transpL || transpR) {

            if (nbCols > chunk) {
                MultiplyNeither.divide(0, nbCols, chunk, (f, l) -> MultiplyNeither.fillMxC(product, f, l, left, transpL, complexity, right, transpR, scalar));
            } else {
                MultiplyNeither.fillMxC(product, 0, nbCols, left, transpL, complexity, right, transpR, scalar);
            }

        } else {

            Arrays.fill(product, scalar.zero().get());

            if (nbCols > chunk) {
                MultiplyNeither.divide(0, nbCols, chunk, (f, l) -> MultiplyNeither.addMxC(product, f, l, left, complexity, right));
            } else {
                MultiplyNeither.addMxC(product, 0, nbCols, left, complexity, right);
            }
        }
    }

}
