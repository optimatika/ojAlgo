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
package org.ojalgo.matrix.operation;

import java.lang.reflect.Array;
import java.util.function.IntSupplier;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.operation.DOT;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.concurrent.DivideAndConquer.Conquerer;
import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.concurrent.ProcessingService;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Structure2D;

public class MultiplyBoth implements MatrixOperation {

    @FunctionalInterface
    public interface Generic<N extends Scalar<N>> extends TransformableRegion.FillByMultiplying<N> {

    }

    @FunctionalInterface
    public interface Primitive extends TransformableRegion.FillByMultiplying<Double> {

    }

    public static IntSupplier PARALLELISM = Parallelism.THREADS;
    public static int THRESHOLD = 8;

    private static final DivideAndConquer.Divider DIVIDER = ProcessingService.INSTANCE.divider();

    public static <N extends Scalar<N>> MultiplyBoth.Generic<N> newGeneric(final int rows, final int columns) {
        if (rows > THRESHOLD && columns > THRESHOLD) {
            return MultiplyBoth::fillMxN_MT_G;
        }
        if (columns == 1) {
            return MultiplyBoth::fillMx1_G;
        }
        if (rows == 1) {
            return MultiplyBoth::fill1xN_G;
        }
        return MultiplyBoth::fillMxN_G;
    }

    public static MultiplyBoth.Primitive newPrimitive32(final int rows, final int columns) {
        return MultiplyBoth.newPrimitive64(rows, columns);
    }

    public static MultiplyBoth.Primitive newPrimitive64(final int rows, final int columns) {
        if (rows > THRESHOLD && columns > THRESHOLD) {
            return MultiplyBoth::fillMxN_MT_P64;
        }
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
        if (rows == 1 && columns == 1) {
            return MultiplyBoth::fill1x1_P64;
        }
        if (columns == 1) {
            return MultiplyBoth::fillMx1_P64;
        }
        if (rows == 10) {
            return MultiplyBoth::fill0xN_P64;
        }
        if (rows == 9) {
            return MultiplyBoth::fill9xN_P64;
        }
        if (rows == 8) {
            return MultiplyBoth::fill8xN_P64;
        }
        if (rows == 7) {
            return MultiplyBoth::fill7xN_P64;
        }
        if (rows == 6) {
            return MultiplyBoth::fill6xN_P64;
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

    static void divide(final int first, final int limit, final Conquerer conquerer) {
        DIVIDER.parallelism(PARALLELISM).threshold(THRESHOLD).divide(first, limit, conquerer);
    }

    static void fill0xN_P64(final TransformableRegion<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

        int tmpColDim = product.getColDim();

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
                double tmpRightCJ = right.doubleValue(Structure2D.index(complexity, c, j));
                tmp0J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp1J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp2J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp3J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp4J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp5J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp6J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp7J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp8J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp9J += left.doubleValue(tmpIndex++) * tmpRightCJ;
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

    static void fill1x1_P64(final TransformableRegion<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

        double tmp00 = PrimitiveMath.ZERO;

        for (long c = 0L; c < complexity; c++) {
            tmp00 += left.doubleValue(c) * right.doubleValue(c);
        }

        product.set(0L, 0L, tmp00);
    }

    static <N extends Scalar<N>> void fill1xN_G(final TransformableRegion<N> product, final Access1D<N> left, final int complexity, final Access1D<N> right) {

        Class<N> componenetType = (Class<N>) left.get(0L).getClass();
        N zero;
        try {
            zero = componenetType.newInstance();
        } catch (InstantiationException | IllegalAccessException exception) {
            exception.printStackTrace();
            throw new ProgrammingError(exception);
        }

        int tmpColDim = product.getColDim();

        N[] tmpLeftRow = (N[]) Array.newInstance(componenetType, complexity);
        N tmpVal;

        int tmpFirst = 0;
        int tmpLimit = complexity;

        for (int c = 0; c < complexity; c++) {
            tmpLeftRow[c] = left.get(c);
        }

        for (int j = 0; j < tmpColDim; j++) {
            long tmpColBase = Structure2D.index(complexity, 0, j);

            tmpFirst = Structure2D.firstInColumn(right, j, 0);
            tmpLimit = Structure2D.limitOfColumn(right, j, complexity);

            tmpVal = zero;
            for (int c = tmpFirst; c < tmpLimit; c++) {
                tmpVal = tmpVal.add(tmpLeftRow[c].multiply(right.get(c + tmpColBase))).get();
            }
            product.set(0, j, tmpVal);
        }
    }

    static void fill1xN_P64(final TransformableRegion<Double> product, final Access1D<?> left, final int complexity, final Access1D<?> right) {

        int nbCols = product.getColDim();

        int firstInRow = Structure2D.firstInRow(right, 0, 0);
        int limitOfRow = Structure2D.firstInRow(right, 0, complexity);

        for (int j = 0; j < nbCols; j++) {
            int firstInCol = Structure2D.firstInColumn(right, j, firstInRow);
            int limitOfCol = Structure2D.firstInColumn(right, j, limitOfRow);
            product.set(j, DOT.invokeP64(left, 0, right, j * complexity, firstInCol, limitOfCol));
        }
    }

    static void fill2x2_P64(final TransformableRegion<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

        double tmp00 = PrimitiveMath.ZERO;
        double tmp10 = PrimitiveMath.ZERO;
        double tmp01 = PrimitiveMath.ZERO;
        double tmp11 = PrimitiveMath.ZERO;

        long tmpIndex;
        for (long c = 0; c < complexity; c++) {

            tmpIndex = c * 2L;
            double tmpLeft0 = left.doubleValue(tmpIndex);
            tmpIndex++;
            double tmpLeft1 = left.doubleValue(tmpIndex);
            tmpIndex = c;
            double tmpRight0 = right.doubleValue(tmpIndex);
            tmpIndex += complexity;
            double tmpRight1 = right.doubleValue(tmpIndex);

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

    static void fill3x3_P64(final TransformableRegion<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

        double tmp00 = PrimitiveMath.ZERO;
        double tmp10 = PrimitiveMath.ZERO;
        double tmp20 = PrimitiveMath.ZERO;
        double tmp01 = PrimitiveMath.ZERO;
        double tmp11 = PrimitiveMath.ZERO;
        double tmp21 = PrimitiveMath.ZERO;
        double tmp02 = PrimitiveMath.ZERO;
        double tmp12 = PrimitiveMath.ZERO;
        double tmp22 = PrimitiveMath.ZERO;

        long tmpIndex;
        for (long c = 0; c < complexity; c++) {

            tmpIndex = c * 3L;
            double tmpLeft0 = left.doubleValue(tmpIndex);
            tmpIndex++;
            double tmpLeft1 = left.doubleValue(tmpIndex);
            tmpIndex++;
            double tmpLeft2 = left.doubleValue(tmpIndex);
            tmpIndex = c;
            double tmpRight0 = right.doubleValue(tmpIndex);
            tmpIndex += complexity;
            double tmpRight1 = right.doubleValue(tmpIndex);
            tmpIndex += complexity;
            double tmpRight2 = right.doubleValue(tmpIndex);

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

    static void fill4x4_P64(final TransformableRegion<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

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

        long tmpIndex;
        for (long c = 0; c < complexity; c++) {

            tmpIndex = c * 4L;
            double tmpLeft0 = left.doubleValue(tmpIndex);
            tmpIndex++;
            double tmpLeft1 = left.doubleValue(tmpIndex);
            tmpIndex++;
            double tmpLeft2 = left.doubleValue(tmpIndex);
            tmpIndex++;
            double tmpLeft3 = left.doubleValue(tmpIndex);
            tmpIndex = c;
            double tmpRight0 = right.doubleValue(tmpIndex);
            tmpIndex += complexity;
            double tmpRight1 = right.doubleValue(tmpIndex);
            tmpIndex += complexity;
            double tmpRight2 = right.doubleValue(tmpIndex);
            tmpIndex += complexity;
            double tmpRight3 = right.doubleValue(tmpIndex);

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

    static void fill5x5_P64(final TransformableRegion<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

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

        long tmpIndex;
        for (long c = 0; c < complexity; c++) {

            tmpIndex = c * 5L;
            double tmpLeft0 = left.doubleValue(tmpIndex);
            tmpIndex++;
            double tmpLeft1 = left.doubleValue(tmpIndex);
            tmpIndex++;
            double tmpLeft2 = left.doubleValue(tmpIndex);
            tmpIndex++;
            double tmpLeft3 = left.doubleValue(tmpIndex);
            tmpIndex++;
            double tmpLeft4 = left.doubleValue(tmpIndex);
            tmpIndex = c;
            double tmpRight0 = right.doubleValue(tmpIndex);
            tmpIndex += complexity;
            double tmpRight1 = right.doubleValue(tmpIndex);
            tmpIndex += complexity;
            double tmpRight2 = right.doubleValue(tmpIndex);
            tmpIndex += complexity;
            double tmpRight3 = right.doubleValue(tmpIndex);
            tmpIndex += complexity;
            double tmpRight4 = right.doubleValue(tmpIndex);

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

    static void fill6xN_P64(final TransformableRegion<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

        int tmpColDim = product.getColDim();

        for (int j = 0; j < tmpColDim; j++) {

            double tmp0J = PrimitiveMath.ZERO;
            double tmp1J = PrimitiveMath.ZERO;
            double tmp2J = PrimitiveMath.ZERO;
            double tmp3J = PrimitiveMath.ZERO;
            double tmp4J = PrimitiveMath.ZERO;
            double tmp5J = PrimitiveMath.ZERO;

            int tmpIndex = 0;
            for (int c = 0; c < complexity; c++) {
                double tmpRightCJ = right.doubleValue(Structure2D.index(complexity, c, j));
                tmp0J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp1J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp2J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp3J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp4J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp5J += left.doubleValue(tmpIndex++) * tmpRightCJ;
            }

            product.set(0, j, tmp0J);
            product.set(1, j, tmp1J);
            product.set(2, j, tmp2J);
            product.set(3, j, tmp3J);
            product.set(4, j, tmp4J);
            product.set(5, j, tmp5J);
        }
    }

    static void fill7xN_P64(final TransformableRegion<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

        int tmpColDim = product.getColDim();

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
                double tmpRightCJ = right.doubleValue(Structure2D.index(complexity, c, j));
                tmp0J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp1J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp2J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp3J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp4J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp5J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp6J += left.doubleValue(tmpIndex++) * tmpRightCJ;
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

    static void fill8xN_P64(final TransformableRegion<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

        int tmpColDim = product.getColDim();

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
                double tmpRightCJ = right.doubleValue(Structure2D.index(complexity, c, j));
                tmp0J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp1J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp2J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp3J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp4J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp5J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp6J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp7J += left.doubleValue(tmpIndex++) * tmpRightCJ;
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

    static void fill9xN_P64(final TransformableRegion<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

        int tmpColDim = product.getColDim();

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
                double tmpRightCJ = right.doubleValue(Structure2D.index(complexity, c, j));
                tmp0J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp1J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp2J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp3J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp4J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp5J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp6J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp7J += left.doubleValue(tmpIndex++) * tmpRightCJ;
                tmp8J += left.doubleValue(tmpIndex++) * tmpRightCJ;
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

    static <N extends Scalar<N>> void fillMx1_G(final TransformableRegion<N> product, final Access1D<N> left, final int complexity, final Access1D<N> right) {

        Class<N> componenetType = (Class<N>) left.get(0L).getClass();
        N zero;
        try {
            zero = componenetType.newInstance();
        } catch (InstantiationException | IllegalAccessException exception) {
            exception.printStackTrace();
            throw new ProgrammingError(exception);
        }

        int tmpRowDim = product.getRowDim();

        N[] tmpLeftRow = (N[]) Array.newInstance(componenetType, complexity);
        N tmpVal;

        int tmpFirst = 0;
        int tmpLimit = complexity;

        for (int i = 0; i < tmpRowDim; i++) {

            int tmpFirstInRow = Structure2D.firstInRow(left, i, 0);
            int tmpLimitOfRow = Structure2D.limitOfRow(left, i, complexity);

            for (int c = tmpFirstInRow; c < tmpLimitOfRow; c++) {
                tmpLeftRow[c] = left.get(Structure2D.index(tmpRowDim, i, c));
            }

            tmpFirst = Structure2D.firstInColumn(right, 0, tmpFirstInRow);
            tmpLimit = Structure2D.limitOfColumn(right, 0, tmpLimitOfRow);

            tmpVal = zero;
            for (int c = tmpFirst; c < tmpLimit; c++) {
                tmpVal = tmpVal.add(tmpLeftRow[c].multiply(right.get(c))).get();
            }
            product.set(i, 0, tmpVal);
        }
    }

    static void fillMx1_P64(final TransformableRegion<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

        int tmpRowDim = product.getRowDim();
        double[] tmpLeftRow = new double[complexity];

        for (int i = 0; i < tmpRowDim; i++) {

            int tmpFirstInRow = Structure2D.firstInRow(left, i, 0);
            int tmpLimitOfRow = Structure2D.limitOfRow(left, i, complexity);

            for (int c = tmpFirstInRow; c < tmpLimitOfRow; c++) {
                tmpLeftRow[c] = left.doubleValue(Structure2D.index(tmpRowDim, i, c));
            }

            product.set(i, 0, DOT.invoke(tmpLeftRow, 0, right, 0, tmpFirstInRow, tmpLimitOfRow));
        }
    }

    static <N extends Scalar<N>> void fillMxN_G(final TransformableRegion<N> product, final Access1D<N> left, final int complexity, final Access1D<N> right) {
        MultiplyBoth.fillRxN_G(product, 0, product.getRowDim(), left, complexity, right);
    }

    static <N extends Scalar<N>> void fillMxN_MT_G(final TransformableRegion<N> product, final Access1D<N> left, final int complexity,
            final Access1D<N> right) {
        MultiplyBoth.divide(0, product.getRowDim(), (f, l) -> MultiplyBoth.fillRxN_G(product, f, l, left, complexity, right));
    }

    static void fillMxN_MT_P64(final TransformableRegion<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {
        MultiplyBoth.divide(0, product.getRowDim(), (f, l) -> MultiplyBoth.fillRxN_P64(product, f, l, left, complexity, right));
    }

    static void fillMxN_P64(final TransformableRegion<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {
        MultiplyBoth.fillRxN_P64(product, 0, product.getRowDim(), left, complexity, right);
    }

    static <N extends Scalar<N>> void fillRxN_G(final TransformableRegion<N> product, final int firstRow, final int rowLimit, final Access1D<N> left,
            final int complexity, final Access1D<N> right) {

        Class<N> componenetType = (Class<N>) left.get(0L).getClass();
        N zero;
        try {
            zero = componenetType.newInstance();
        } catch (InstantiationException | IllegalAccessException exception) {
            exception.printStackTrace();
            throw new ProgrammingError(exception);
        }

        int tmpRowDim = product.getRowDim();
        int tmpColDim = product.getColDim();

        N[] tmpLeftRow = (N[]) Array.newInstance(componenetType, complexity);
        N tmpVal;

        int tmpFirst = 0;
        int tmpLimit = complexity;

        for (int i = firstRow; i < rowLimit; i++) {

            int tmpFirstInRow = Structure2D.firstInRow(left, i, 0);
            int tmpLimitOfRow = Structure2D.limitOfRow(left, i, complexity);

            for (int c = tmpFirstInRow; c < tmpLimitOfRow; c++) {
                tmpLeftRow[c] = left.get(Structure2D.index(tmpRowDim, i, c));
            }

            for (int j = 0; j < tmpColDim; j++) {
                long tmpColBase = Structure2D.index(complexity, 0, j);

                tmpFirst = Structure2D.firstInColumn(right, j, tmpFirstInRow);
                tmpLimit = Structure2D.limitOfColumn(right, j, tmpLimitOfRow);

                tmpVal = zero;
                for (int c = tmpFirst; c < tmpLimit; c++) {
                    tmpVal = tmpVal.add(tmpLeftRow[c].multiply(right.get(c + tmpColBase))).get();
                }
                product.set(i, j, tmpVal);
            }
        }
    }

    static void fillRxN_P32(final TransformableRegion<Double> product, final int firstRow, final int rowLimit, final Access1D<Double> left,
            final int complexity, final Access1D<Double> right) {

        int tmpRowDim = product.getRowDim();
        int tmpColDim = product.getColDim();
        float[] tmpLeftRow = new float[complexity];
        float tmpVal;

        int tmpFirst = 0;
        int tmpLimit = complexity;

        for (int i = firstRow; i < rowLimit; i++) {

            int tmpFirstInRow = Structure2D.firstInRow(left, i, 0);
            int tmpLimitOfRow = Structure2D.limitOfRow(left, i, complexity);

            for (int c = tmpFirstInRow; c < tmpLimitOfRow; c++) {
                tmpLeftRow[c] = left.floatValue(Structure2D.index(tmpRowDim, i, c));
            }

            for (int j = 0; j < tmpColDim; j++) {
                long tmpColBase = Structure2D.index(complexity, 0, j);

                tmpFirst = Structure2D.firstInColumn(right, j, tmpFirstInRow);
                tmpLimit = Structure2D.limitOfColumn(right, j, tmpLimitOfRow);

                tmpVal = 0F;
                for (int c = tmpFirst; c < tmpLimit; c++) {
                    tmpVal += tmpLeftRow[c] * right.floatValue(c + tmpColBase);
                }
                product.set(i, j, tmpVal);
            }
        }
    }

    static void fillRxN_P64(final TransformableRegion<Double> product, final int firstRow, final int rowLimit, final Access1D<Double> left,
            final int complexity, final Access1D<Double> right) {

        int tmpRowDim = product.getRowDim();
        int tmpColDim = product.getColDim();
        int tmpPlxDim = complexity;

        double[] tmpLeftRow = new double[tmpPlxDim];
        double tmpVal;

        int tmpFirst = 0;
        int tmpLimit = tmpPlxDim;

        for (int i = firstRow; i < rowLimit; i++) {

            int tmpFirstInRow = Structure2D.firstInRow(left, i, 0);
            int tmpLimitOfRow = Structure2D.limitOfRow(left, i, tmpPlxDim);

            for (int c = tmpFirstInRow; c < tmpLimitOfRow; c++) {
                tmpLeftRow[c] = left.doubleValue(Structure2D.index(tmpRowDim, i, c));
            }

            for (int j = 0; j < tmpColDim; j++) {
                long tmpColBase = Structure2D.index(complexity, 0, j);

                tmpFirst = Structure2D.firstInColumn(right, j, tmpFirstInRow);
                tmpLimit = Structure2D.limitOfColumn(right, j, tmpLimitOfRow);

                tmpVal = PrimitiveMath.ZERO;
                for (int c = tmpFirst; c < tmpLimit; c++) {
                    tmpVal += tmpLeftRow[c] * right.doubleValue(c + tmpColBase);
                }
                product.set(i, j, tmpVal);
            }
        }
    }

}
