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
package org.ojalgo.matrix.store.operation;

import java.math.BigDecimal;

import org.ojalgo.access.Access1D;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.constant.BigMath;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.matrix.store.BigDenseStore.BigMultiplyRight;
import org.ojalgo.matrix.store.ComplexDenseStore.ComplexMultiplyRight;
import org.ojalgo.matrix.store.PrimitiveDenseStore.PrimitiveMultiplyRight;
import org.ojalgo.scalar.ComplexNumber;

public final class MultiplyRight extends MatrixOperation {

    public static final MultiplyRight SETUP = new MultiplyRight();

    public static int THRESHOLD = 32;

    static final BigMultiplyRight BIG = new BigMultiplyRight() {

        public void invoke(final BigDecimal[] product, final BigDecimal[] left, final int complexity, final Access1D<BigDecimal> right) {
            MultiplyRight.invoke(product, 0, (int) (right.count() / complexity), left, complexity, right);
        }

    };

    static final BigMultiplyRight BIG_MT = new BigMultiplyRight() {

        public void invoke(final BigDecimal[] product, final BigDecimal[] left, final int complexity, final Access1D<BigDecimal> right) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int first, final int limit) {
                    MultiplyRight.invoke(product, first, limit, left, complexity, right);
                }
            };

            tmpConquerer.invoke(0, (int) (right.count() / complexity), THRESHOLD);
        }

    };

    static final ComplexMultiplyRight COMPLEX = new ComplexMultiplyRight() {

        public void invoke(final ComplexNumber[] product, final ComplexNumber[] left, final int complexity, final Access1D<ComplexNumber> right) {
            MultiplyRight.invoke(product, 0, (int) (right.count() / complexity), left, complexity, right);
        }

    };

    static final ComplexMultiplyRight COMPLEX_MT = new ComplexMultiplyRight() {

        public void invoke(final ComplexNumber[] product, final ComplexNumber[] left, final int complexity, final Access1D<ComplexNumber> right) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int first, final int limit) {
                    MultiplyRight.invoke(product, first, limit, left, complexity, right);
                }
            };

            tmpConquerer.invoke(0, (int) (right.count() / complexity), THRESHOLD);
        }

    };

    static final PrimitiveMultiplyRight PRIMITIVE = new PrimitiveMultiplyRight() {

        public void invoke(final double[] product, final double[] left, final int complexity, final Access1D<?> right) {
            MultiplyRight.invoke(product, 0, (int) (right.count() / complexity), left, complexity, right);
        }

    };

    static final PrimitiveMultiplyRight PRIMITIVE_0XN = new PrimitiveMultiplyRight() {

        public void invoke(final double[] product, final double[] left, final int complexity, final Access1D<?> right) {

            final int tmpRowDim = 10;
            final int tmpColDim = product.length / tmpRowDim;

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
                    final double tmpRightCJ = right.doubleValue(c + (j * complexity));
                    tmp0J += left[tmpIndex++] * tmpRightCJ;
                    tmp1J += left[tmpIndex++] * tmpRightCJ;
                    tmp2J += left[tmpIndex++] * tmpRightCJ;
                    tmp3J += left[tmpIndex++] * tmpRightCJ;
                    tmp4J += left[tmpIndex++] * tmpRightCJ;
                    tmp5J += left[tmpIndex++] * tmpRightCJ;
                    tmp6J += left[tmpIndex++] * tmpRightCJ;
                    tmp7J += left[tmpIndex++] * tmpRightCJ;
                    tmp8J += left[tmpIndex++] * tmpRightCJ;
                    tmp9J += left[tmpIndex++] * tmpRightCJ;
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

    };

    static final PrimitiveMultiplyRight PRIMITIVE_1X1 = new PrimitiveMultiplyRight() {

        public void invoke(final double[] product, final double[] left, final int complexity, final Access1D<?> right) {

            double tmp00 = PrimitiveMath.ZERO;

            final int tmpLeftStruct = left.length / complexity; // The number of rows in the product- and left-matrix.

            for (int c = 0; c < complexity; c++) {
                tmp00 += left[c * tmpLeftStruct] * right.doubleValue(c);
            }

            product[0] = tmp00;
        }

    };

    static final PrimitiveMultiplyRight PRIMITIVE_1XN = new PrimitiveMultiplyRight() {

        public void invoke(final double[] product, final double[] left, final int complexity, final Access1D<?> right) {

            final int tmpColDim = product.length;

            for (int j = 0; j < tmpColDim; j++) {

                double tmp0J = PrimitiveMath.ZERO;

                int tmpIndex = 0;
                for (int c = 0; c < complexity; c++) {
                    tmp0J += left[tmpIndex++] * right.doubleValue(c + (j * complexity));
                }

                product[j] = tmp0J;
            }
        }

    };

    static final PrimitiveMultiplyRight PRIMITIVE_2X2 = new PrimitiveMultiplyRight() {

        public void invoke(final double[] product, final double[] left, final int complexity, final Access1D<?> right) {

            double tmp00 = PrimitiveMath.ZERO;
            double tmp10 = PrimitiveMath.ZERO;
            double tmp01 = PrimitiveMath.ZERO;
            double tmp11 = PrimitiveMath.ZERO;

            int tmpIndex;
            for (int c = 0; c < complexity; c++) {

                tmpIndex = c * 2;
                final double tmpLeft0 = left[tmpIndex];
                tmpIndex++;
                final double tmpLeft1 = left[tmpIndex];
                tmpIndex = c;
                final double tmpRight0 = right.doubleValue(tmpIndex);
                tmpIndex += complexity;
                final double tmpRight1 = right.doubleValue(tmpIndex);

                tmp00 += tmpLeft0 * tmpRight0;
                tmp10 += tmpLeft1 * tmpRight0;
                tmp01 += tmpLeft0 * tmpRight1;
                tmp11 += tmpLeft1 * tmpRight1;
            }

            product[0] = tmp00;
            product[1] = tmp10;
            product[2] = tmp01;
            product[3] = tmp11;
        }

    };

    static final PrimitiveMultiplyRight PRIMITIVE_3X3 = new PrimitiveMultiplyRight() {

        public void invoke(final double[] product, final double[] left, final int complexity, final Access1D<?> right) {

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
                final double tmpLeft0 = left[tmpIndex];
                tmpIndex++;
                final double tmpLeft1 = left[tmpIndex];
                tmpIndex++;
                final double tmpLeft2 = left[tmpIndex];
                tmpIndex = c;
                final double tmpRight0 = right.doubleValue(tmpIndex);
                tmpIndex += complexity;
                final double tmpRight1 = right.doubleValue(tmpIndex);
                tmpIndex += complexity;
                final double tmpRight2 = right.doubleValue(tmpIndex);

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

    };

    static final PrimitiveMultiplyRight PRIMITIVE_4X4 = new PrimitiveMultiplyRight() {

        public void invoke(final double[] product, final double[] left, final int complexity, final Access1D<?> right) {

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
                final double tmpLeft0 = left[tmpIndex];
                tmpIndex++;
                final double tmpLeft1 = left[tmpIndex];
                tmpIndex++;
                final double tmpLeft2 = left[tmpIndex];
                tmpIndex++;
                final double tmpLeft3 = left[tmpIndex];
                tmpIndex = c;
                final double tmpRight0 = right.doubleValue(tmpIndex);
                tmpIndex += complexity;
                final double tmpRight1 = right.doubleValue(tmpIndex);
                tmpIndex += complexity;
                final double tmpRight2 = right.doubleValue(tmpIndex);
                tmpIndex += complexity;
                final double tmpRight3 = right.doubleValue(tmpIndex);

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

    };

    static final PrimitiveMultiplyRight PRIMITIVE_5X5 = new PrimitiveMultiplyRight() {

        public void invoke(final double[] product, final double[] left, final int complexity, final Access1D<?> right) {

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

            int tmpIndex;
            for (int c = 0; c < complexity; c++) {

                tmpIndex = c * 5;
                final double tmpLeft0 = left[tmpIndex];
                tmpIndex++;
                final double tmpLeft1 = left[tmpIndex];
                tmpIndex++;
                final double tmpLeft2 = left[tmpIndex];
                tmpIndex++;
                final double tmpLeft3 = left[tmpIndex];
                tmpIndex++;
                final double tmpLeft4 = left[tmpIndex];
                tmpIndex = c;
                final double tmpRight0 = right.doubleValue(tmpIndex);
                tmpIndex += complexity;
                final double tmpRight1 = right.doubleValue(tmpIndex);
                tmpIndex += complexity;
                final double tmpRight2 = right.doubleValue(tmpIndex);
                tmpIndex += complexity;
                final double tmpRight3 = right.doubleValue(tmpIndex);
                tmpIndex += complexity;
                final double tmpRight4 = right.doubleValue(tmpIndex);

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

            product[0] = tmp00;
            product[1] = tmp10;
            product[2] = tmp20;
            product[3] = tmp30;
            product[4] = tmp40;
            product[5] = tmp01;
            product[6] = tmp11;
            product[7] = tmp21;
            product[8] = tmp31;
            product[9] = tmp41;
            product[10] = tmp02;
            product[11] = tmp12;
            product[12] = tmp22;
            product[13] = tmp32;
            product[14] = tmp42;
            product[15] = tmp03;
            product[16] = tmp13;
            product[17] = tmp23;
            product[18] = tmp33;
            product[19] = tmp43;
            product[20] = tmp04;
            product[21] = tmp14;
            product[22] = tmp24;
            product[23] = tmp34;
            product[24] = tmp44;
        }

    };

    static final PrimitiveMultiplyRight PRIMITIVE_6XN = new PrimitiveMultiplyRight() {

        public void invoke(final double[] product, final double[] left, final int complexity, final Access1D<?> right) {

            final int tmpRowDim = 6;
            final int tmpColDim = product.length / tmpRowDim;

            for (int j = 0; j < tmpColDim; j++) {

                double tmp0J = PrimitiveMath.ZERO;
                double tmp1J = PrimitiveMath.ZERO;
                double tmp2J = PrimitiveMath.ZERO;
                double tmp3J = PrimitiveMath.ZERO;
                double tmp4J = PrimitiveMath.ZERO;
                double tmp5J = PrimitiveMath.ZERO;

                int tmpIndex = 0;
                for (int c = 0; c < complexity; c++) {
                    final double tmpRightCJ = right.doubleValue(c + (j * complexity));
                    tmp0J += left[tmpIndex++] * tmpRightCJ;
                    tmp1J += left[tmpIndex++] * tmpRightCJ;
                    tmp2J += left[tmpIndex++] * tmpRightCJ;
                    tmp3J += left[tmpIndex++] * tmpRightCJ;
                    tmp4J += left[tmpIndex++] * tmpRightCJ;
                    tmp5J += left[tmpIndex++] * tmpRightCJ;
                }

                product[tmpIndex = j * tmpRowDim] = tmp0J;
                product[++tmpIndex] = tmp1J;
                product[++tmpIndex] = tmp2J;
                product[++tmpIndex] = tmp3J;
                product[++tmpIndex] = tmp4J;
                product[++tmpIndex] = tmp5J;
            }
        }

    };

    static final PrimitiveMultiplyRight PRIMITIVE_7XN = new PrimitiveMultiplyRight() {

        public void invoke(final double[] product, final double[] left, final int complexity, final Access1D<?> right) {

            final int tmpRowDim = 7;
            final int tmpColDim = product.length / tmpRowDim;

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
                    final double tmpRightCJ = right.doubleValue(c + (j * complexity));
                    tmp0J += left[tmpIndex++] * tmpRightCJ;
                    tmp1J += left[tmpIndex++] * tmpRightCJ;
                    tmp2J += left[tmpIndex++] * tmpRightCJ;
                    tmp3J += left[tmpIndex++] * tmpRightCJ;
                    tmp4J += left[tmpIndex++] * tmpRightCJ;
                    tmp5J += left[tmpIndex++] * tmpRightCJ;
                    tmp6J += left[tmpIndex++] * tmpRightCJ;
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

    };

    static final PrimitiveMultiplyRight PRIMITIVE_8XN = new PrimitiveMultiplyRight() {

        public void invoke(final double[] product, final double[] left, final int complexity, final Access1D<?> right) {

            final int tmpRowDim = 8;
            final int tmpColDim = product.length / tmpRowDim;

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
                    final double tmpRightCJ = right.doubleValue(c + (j * complexity));
                    tmp0J += left[tmpIndex++] * tmpRightCJ;
                    tmp1J += left[tmpIndex++] * tmpRightCJ;
                    tmp2J += left[tmpIndex++] * tmpRightCJ;
                    tmp3J += left[tmpIndex++] * tmpRightCJ;
                    tmp4J += left[tmpIndex++] * tmpRightCJ;
                    tmp5J += left[tmpIndex++] * tmpRightCJ;
                    tmp6J += left[tmpIndex++] * tmpRightCJ;
                    tmp7J += left[tmpIndex++] * tmpRightCJ;
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

    };

    static final PrimitiveMultiplyRight PRIMITIVE_9XN = new PrimitiveMultiplyRight() {

        public void invoke(final double[] product, final double[] left, final int complexity, final Access1D<?> right) {

            final int tmpRowDim = 9;
            final int tmpColDim = product.length / tmpRowDim;

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
                    final double tmpRightCJ = right.doubleValue(c + (j * complexity));
                    tmp0J += left[tmpIndex++] * tmpRightCJ;
                    tmp1J += left[tmpIndex++] * tmpRightCJ;
                    tmp2J += left[tmpIndex++] * tmpRightCJ;
                    tmp3J += left[tmpIndex++] * tmpRightCJ;
                    tmp4J += left[tmpIndex++] * tmpRightCJ;
                    tmp5J += left[tmpIndex++] * tmpRightCJ;
                    tmp6J += left[tmpIndex++] * tmpRightCJ;
                    tmp7J += left[tmpIndex++] * tmpRightCJ;
                    tmp8J += left[tmpIndex++] * tmpRightCJ;
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

    };

    static final PrimitiveMultiplyRight PRIMITIVE_MT = new PrimitiveMultiplyRight() {

        public void invoke(final double[] product, final double[] left, final int complexity, final Access1D<?> right) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int first, final int limit) {
                    MultiplyRight.invoke(product, first, limit, left, complexity, right);
                }
            };

            tmpConquerer.invoke(0, (int) (right.count() / complexity), THRESHOLD);
        }

    };

    public static BigMultiplyRight getBig(final long rows, final long columns) {
        if (columns > THRESHOLD) {
            return BIG_MT;
        } else {
            return BIG;
        }
    }

    public static ComplexMultiplyRight getComplex(final long rows, final long columns) {
        if (columns > THRESHOLD) {
            return COMPLEX_MT;
        } else {
            return COMPLEX;
        }
    }

    public static PrimitiveMultiplyRight getPrimitive(final long rows, final long columns) {
        if (columns > THRESHOLD) {
            return PRIMITIVE_MT;
        } else if (rows == 10) {
            return PRIMITIVE_0XN;
        } else if (rows == 9) {
            return PRIMITIVE_9XN;
        } else if (rows == 8) {
            return PRIMITIVE_8XN;
        } else if (rows == 7) {
            return PRIMITIVE_7XN;
        } else if (rows == 6) {
            return PRIMITIVE_6XN;
        } else if ((rows == 5) && (columns == 5)) {
            return PRIMITIVE_5X5;
        } else if ((rows == 4) && (columns == 4)) {
            return PRIMITIVE_4X4;
        } else if ((rows == 3) && (columns == 3)) {
            return PRIMITIVE_3X3;
        } else if ((rows == 2) && (columns == 2)) {
            return PRIMITIVE_2X2;
        } else if (rows == 1) {
            return PRIMITIVE_1XN;
        } else {
            return PRIMITIVE;
        }
    }

    static void experiment(final double[] product, final double[] left, final int complexity, final double[] right) {

        final int tmpRowDim = left.length / complexity;
        final int tmpColDim = right.length / complexity;

        // Fixed number of rows (imagine 3)
        for (int j = 0; j < tmpColDim; j++) {

            double tmpProduct0J = PrimitiveMath.ZERO;
            double tmpProduct1J = PrimitiveMath.ZERO;
            double tmpProduct2J = PrimitiveMath.ZERO;

            for (int c = 0; c < complexity; c++) {
                final double tmpRightCJ = right[c + (j * complexity)];
                tmpProduct0J += left[0 + (c * tmpRowDim)] * tmpRightCJ; // y += ax
                tmpProduct1J += left[1 + (c * tmpRowDim)] * tmpRightCJ; // y += ax
                tmpProduct2J += left[2 + (c * tmpRowDim)] * tmpRightCJ; // y += ax
            }

            product[0 + (j * tmpRowDim)] = tmpProduct0J;
            product[1 + (j * tmpRowDim)] = tmpProduct1J;
            product[2 + (j * tmpRowDim)] = tmpProduct2J;
        }

        // Fixed number of columns (imagine 3)
        for (int i = 0; i < tmpRowDim; i++) {

            double tmpProductI0 = PrimitiveMath.ZERO;
            double tmpProductI1 = PrimitiveMath.ZERO;
            double tmpProductI2 = PrimitiveMath.ZERO;

            for (int c = 0; c < complexity; c++) {
                final double tmpLeftIC = left[i + (c * tmpRowDim)];
                tmpProductI0 += tmpLeftIC * right[c + (0 * complexity)];
                tmpProductI1 += tmpLeftIC * right[c + (1 * complexity)];
                tmpProductI2 += tmpLeftIC * right[c + (2 * complexity)];
            }

            product[i + (0 * tmpRowDim)] = tmpProductI0;
            product[i + (1 * tmpRowDim)] = tmpProductI1;
            product[i + (2 * tmpRowDim)] = tmpProductI2;
        }
    }

    static void invoke(final BigDecimal[] product, final int firstColumn, final int columnLimit, final BigDecimal[] left, final int complexity,
            final Access1D<BigDecimal> right) {

        final int tmpRowDim = left.length / complexity;

        final int tmpIndexLimit = tmpRowDim * columnLimit;
        for (int tmpIndex = tmpRowDim * firstColumn; tmpIndex < tmpIndexLimit; tmpIndex++) {
            product[tmpIndex] = BigMath.ZERO;
        }

        for (int j = firstColumn; j < columnLimit; j++) {
            for (int c = 0; c < complexity; c++) {
                CAXPY.invoke(product, j * tmpRowDim, left, c * tmpRowDim, right.get(c + (j * complexity)), 0, tmpRowDim);
            }
        }
    }

    static void invoke(final ComplexNumber[] product, final int firstColumn, final int columnLimit, final ComplexNumber[] left, final int complexity,
            final Access1D<ComplexNumber> right) {

        final int tmpRowDim = left.length / complexity;

        final int tmpIndexLimit = tmpRowDim * columnLimit;
        for (int tmpIndex = tmpRowDim * firstColumn; tmpIndex < tmpIndexLimit; tmpIndex++) {
            product[tmpIndex] = ComplexNumber.ZERO;
        }

        for (int j = firstColumn; j < columnLimit; j++) {
            for (int c = 0; c < complexity; c++) {
                CAXPY.invoke(product, j * tmpRowDim, left, c * tmpRowDim, right.get(c + (j * complexity)), 0, tmpRowDim);
            }
        }
    }

    static void invoke(final double[] product, final int firstColumn, final int columnLimit, final double[] left, final int complexity, final Access1D<?> right) {

        final int tmpRowDim = left.length / complexity;

        final int tmpIndexLimit = tmpRowDim * columnLimit;
        for (int tmpIndex = tmpRowDim * firstColumn; tmpIndex < tmpIndexLimit; tmpIndex++) {
            product[tmpIndex] = PrimitiveMath.ZERO;
        }

        for (int j = firstColumn; j < columnLimit; j++) {
            for (int c = 0; c < complexity; c++) {
                CAXPY.invoke(product, j * tmpRowDim, left, c * tmpRowDim, right.doubleValue(c + (j * complexity)), 0, tmpRowDim);
            }
        }
    }

    private MultiplyRight() {
        super();
    }

    @Override
    public int threshold() {
        return THRESHOLD;
    }

}
