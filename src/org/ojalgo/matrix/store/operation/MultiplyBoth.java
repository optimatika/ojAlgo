/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
import org.ojalgo.function.BigFunction;
import org.ojalgo.function.FunctionUtils;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.BigDenseStore.BigMultiplyBoth;
import org.ojalgo.matrix.store.ComplexDenseStore.ComplexMultiplyBoth;
import org.ojalgo.matrix.store.ElementsConsumer;
import org.ojalgo.matrix.store.PrimitiveDenseStore.PrimitiveMultiplyBoth;
import org.ojalgo.scalar.ComplexNumber;

public final class MultiplyBoth extends MatrixOperation {

    public static final MultiplyBoth SETUP = new MultiplyBoth();

    public static int THRESHOLD = 16;

    static final BigMultiplyBoth BIG = new BigMultiplyBoth() {

        public void invoke(final ElementsConsumer<BigDecimal> product, final Access1D<BigDecimal> left, final int complexity,
                final Access1D<BigDecimal> right) {
            MultiplyBoth.invokeBig(product, 0, ((int) left.count()) / complexity, left, complexity, right);
        }

    };

    static final BigMultiplyBoth BIG_MT = new BigMultiplyBoth() {

        public void invoke(final ElementsConsumer<BigDecimal> product, final Access1D<BigDecimal> left, final int complexity,
                final Access1D<BigDecimal> right) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int first, final int limit) {
                    MultiplyBoth.invokeBig(product, first, limit, left, complexity, right);
                }
            };

            tmpConquerer.invoke(0, ((int) left.count()) / complexity, THRESHOLD);
        }

    };

    static final ComplexMultiplyBoth COMPLEX = new ComplexMultiplyBoth() {

        public void invoke(final ElementsConsumer<ComplexNumber> product, final Access1D<ComplexNumber> left, final int complexity,
                final Access1D<ComplexNumber> right) {
            MultiplyBoth.invokeComplex(product, 0, ((int) left.count()) / complexity, left, complexity, right);
        }

    };

    static final ComplexMultiplyBoth COMPLEX_MT = new ComplexMultiplyBoth() {

        public void invoke(final ElementsConsumer<ComplexNumber> product, final Access1D<ComplexNumber> left, final int complexity,
                final Access1D<ComplexNumber> right) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int first, final int limit) {
                    MultiplyBoth.invokeComplex(product, first, limit, left, complexity, right);
                }
            };

            tmpConquerer.invoke(0, ((int) left.count()) / complexity, THRESHOLD);
        }

    };

    static final PrimitiveMultiplyBoth PRIMITIVE = new PrimitiveMultiplyBoth() {

        public void invoke(final ElementsConsumer<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {
            MultiplyBoth.invokePrimitive(product, 0, ((int) left.count()) / complexity, left, complexity, right);
        }

    };

    static final PrimitiveMultiplyBoth PRIMITIVE_0XN = new PrimitiveMultiplyBoth() {

        public void invoke(final ElementsConsumer<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

            final int tmpRowDim = (int) (left.count() / complexity);
            final int tmpColDim = (int) (right.count() / complexity);

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

    };

    static final PrimitiveMultiplyBoth PRIMITIVE_1X1 = new PrimitiveMultiplyBoth() {

        public void invoke(final ElementsConsumer<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

            double tmp00 = PrimitiveMath.ZERO;

            for (long c = 0L; c < complexity; c++) {
                tmp00 += left.doubleValue(c) * right.doubleValue(c);
            }

            product.set(0L, 0L, tmp00);
        }

    };

    static final PrimitiveMultiplyBoth PRIMITIVE_1XN = new PrimitiveMultiplyBoth() {

        public void invoke(final ElementsConsumer<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

            final int tmpColDim = (int) (right.count() / complexity);

            for (int j = 0; j < tmpColDim; j++) {

                double tmp0J = PrimitiveMath.ZERO;

                int tmpIndex = 0;
                for (int c = 0; c < complexity; c++) {
                    tmp0J += left.doubleValue(tmpIndex++) * right.doubleValue(c + (j * complexity));
                }

                product.set(0, j, tmp0J);
            }
        }

    };

    static final PrimitiveMultiplyBoth PRIMITIVE_2X2 = new PrimitiveMultiplyBoth() {

        public void invoke(final ElementsConsumer<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

            double tmp00 = PrimitiveMath.ZERO;
            double tmp10 = PrimitiveMath.ZERO;
            double tmp01 = PrimitiveMath.ZERO;
            double tmp11 = PrimitiveMath.ZERO;

            long tmpIndex;
            for (long c = 0; c < complexity; c++) {

                tmpIndex = c * 2L;
                final double tmpLeft0 = left.doubleValue(tmpIndex);
                tmpIndex++;
                final double tmpLeft1 = left.doubleValue(tmpIndex);
                tmpIndex = c;
                final double tmpRight0 = right.doubleValue(tmpIndex);
                tmpIndex += complexity;
                final double tmpRight1 = right.doubleValue(tmpIndex);

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

    };

    static final PrimitiveMultiplyBoth PRIMITIVE_3X3 = new PrimitiveMultiplyBoth() {

        public void invoke(final ElementsConsumer<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

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
                final double tmpLeft0 = left.doubleValue(tmpIndex);
                tmpIndex++;
                final double tmpLeft1 = left.doubleValue(tmpIndex);
                tmpIndex++;
                final double tmpLeft2 = left.doubleValue(tmpIndex);
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

    };

    static final PrimitiveMultiplyBoth PRIMITIVE_4X4 = new PrimitiveMultiplyBoth() {

        public void invoke(final ElementsConsumer<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

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
                final double tmpLeft0 = left.doubleValue(tmpIndex);
                tmpIndex++;
                final double tmpLeft1 = left.doubleValue(tmpIndex);
                tmpIndex++;
                final double tmpLeft2 = left.doubleValue(tmpIndex);
                tmpIndex++;
                final double tmpLeft3 = left.doubleValue(tmpIndex);
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

    };

    static final PrimitiveMultiplyBoth PRIMITIVE_5X5 = new PrimitiveMultiplyBoth() {

        public void invoke(final ElementsConsumer<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

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
                final double tmpLeft0 = left.doubleValue(tmpIndex);
                tmpIndex++;
                final double tmpLeft1 = left.doubleValue(tmpIndex);
                tmpIndex++;
                final double tmpLeft2 = left.doubleValue(tmpIndex);
                tmpIndex++;
                final double tmpLeft3 = left.doubleValue(tmpIndex);
                tmpIndex++;
                final double tmpLeft4 = left.doubleValue(tmpIndex);
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

    };

    static final PrimitiveMultiplyBoth PRIMITIVE_6XN = new PrimitiveMultiplyBoth() {

        public void invoke(final ElementsConsumer<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

            final int tmpRowDim = 6;
            final int tmpColDim = (int) (right.count() / complexity);

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

    };

    static final PrimitiveMultiplyBoth PRIMITIVE_7XN = new PrimitiveMultiplyBoth() {

        public void invoke(final ElementsConsumer<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

            final int tmpRowDim = (int) (left.count() / complexity);
            final int tmpColDim = (int) (right.count() / complexity);

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

    };

    static final PrimitiveMultiplyBoth PRIMITIVE_8XN = new PrimitiveMultiplyBoth() {

        public void invoke(final ElementsConsumer<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

            final int tmpRowDim = (int) (left.count() / complexity);
            final int tmpColDim = (int) (right.count() / complexity);

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

    };

    static final PrimitiveMultiplyBoth PRIMITIVE_9XN = new PrimitiveMultiplyBoth() {

        public void invoke(final ElementsConsumer<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

            final int tmpRowDim = (int) (left.count() / complexity);
            final int tmpColDim = (int) (right.count() / complexity);

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

    };

    static final PrimitiveMultiplyBoth PRIMITIVE_MT = new PrimitiveMultiplyBoth() {

        public void invoke(final ElementsConsumer<Double> product, final Access1D<Double> left, final int complexity, final Access1D<Double> right) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int first, final int limit) {
                    MultiplyBoth.invokePrimitive(product, first, limit, left, complexity, right);
                }
            };

            tmpConquerer.invoke(0, ((int) left.count()) / complexity, THRESHOLD);
        }

    };

    public static BigMultiplyBoth getBig(final long rows, final long columns) {
        if (rows > THRESHOLD) {
            return BIG_MT;
        } else {
            return BIG;
        }
    }

    public static ComplexMultiplyBoth getComplex(final long rows, final long columns) {
        if (rows > THRESHOLD) {
            return COMPLEX_MT;
        } else {
            return COMPLEX;
        }
    }

    public static PrimitiveMultiplyBoth getPrimitive(final long rows, final long columns) {
        if (rows > THRESHOLD) {
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

    static void invokeBig(final ElementsConsumer<BigDecimal> product, final int firstRow, final int rowLimit, final Access1D<BigDecimal> left,
            final int complexity, final Access1D<BigDecimal> right) {

        final int tmpRowDim = (int) (left.count() / complexity);
        final int tmpColDim = (int) (right.count() / complexity);

        final BigDecimal[] tmpLeftRow = new BigDecimal[complexity];
        BigDecimal tmpVal;

        final boolean tmpLL = MatrixUtils.isLowerLeftShaded(left);
        final boolean tmpLU = MatrixUtils.isUpperRightShaded(left);
        final boolean tmpRL = MatrixUtils.isLowerLeftShaded(right);
        final boolean tmpRU = MatrixUtils.isUpperRightShaded(right);
        final boolean tmpPrune = tmpLL || tmpLU || tmpRL || tmpRU;

        int tmpFirst = 0;
        int tmpLimit = complexity;

        for (int i = firstRow; i < rowLimit; i++) {

            for (int c = 0; c < complexity; c++) {
                tmpLeftRow[c] = left.get(i + (c * tmpRowDim));
            }

            for (int j = 0; j < tmpColDim; j++) {
                if (tmpPrune) {
                    tmpFirst = FunctionUtils.max(tmpLL ? i - 1 : 0, tmpRU ? j - 1 : 0, 0);
                    tmpLimit = FunctionUtils.min(tmpLU ? i + 2 : complexity, tmpRL ? j + 2 : complexity, complexity);
                }
                tmpVal = BigMath.ZERO;
                for (int c = tmpFirst; c < tmpLimit; c++) {
                    tmpVal = BigFunction.ADD.invoke(tmpVal, BigFunction.MULTIPLY.invoke(tmpLeftRow[c], right.get(c + (j * complexity))));
                }
                product.set(i, j, tmpVal);
            }
        }
    }

    static void invokeComplex(final ElementsConsumer<ComplexNumber> product, final int firstRow, final int rowLimit,
            final Access1D<ComplexNumber> left, final int complexity, final Access1D<ComplexNumber> right) {

        final int tmpRowDim = (int) (left.count() / complexity);
        final int tmpColDim = (int) (right.count() / complexity);

        final ComplexNumber[] tmpLeftRow = new ComplexNumber[complexity];
        ComplexNumber tmpVal;

        final boolean tmpLL = MatrixUtils.isLowerLeftShaded(left);
        final boolean tmpLU = MatrixUtils.isUpperRightShaded(left);
        final boolean tmpRL = MatrixUtils.isLowerLeftShaded(right);
        final boolean tmpRU = MatrixUtils.isUpperRightShaded(right);
        final boolean tmpPrune = tmpLL || tmpLU || tmpRL || tmpRU;

        int tmpFirst = 0;
        int tmpLimit = complexity;

        for (int i = firstRow; i < rowLimit; i++) {

            for (int c = 0; c < complexity; c++) {
                tmpLeftRow[c] = left.get(i + (c * tmpRowDim));
            }

            for (int j = 0; j < tmpColDim; j++) {
                if (tmpPrune) {
                    tmpFirst = FunctionUtils.max(tmpLL ? i - 1 : 0, tmpRU ? j - 1 : 0, 0);
                    tmpLimit = FunctionUtils.min(tmpLU ? i + 2 : complexity, tmpRL ? j + 2 : complexity, complexity);
                }
                tmpVal = ComplexNumber.ZERO;
                for (int c = tmpFirst; c < tmpLimit; c++) {
                    tmpVal = tmpVal.add(tmpLeftRow[c].multiply(right.get(c + (j * complexity))));
                }
                product.set(i, j, tmpVal);
            }
        }
    }

    static void invokePrimitive(final ElementsConsumer<Double> product, final int firstRow, final int rowLimit, final Access1D<Double> left,
            final int complexity, final Access1D<Double> right) {

        final int tmpRowDim = (int) product.countRows();
        final int tmpColDim = (int) product.countColumns();

        final double[] tmpLeftRow = new double[complexity];
        double tmpVal;

        int tmpFirst = 0;
        int tmpLimit = complexity;

        for (int i = firstRow; i < rowLimit; i++) {

            final int tmpFirstInRow = MatrixUtils.firstInRow(left, i, 0);
            final int tmpLimitOfRow = MatrixUtils.limitOfRow(left, i, complexity);

            for (int c = tmpFirstInRow; c < tmpLimitOfRow; c++) {
                tmpLeftRow[c] = left.doubleValue(i + (c * tmpRowDim));
            }

            for (int j = 0; j < tmpColDim; j++) {
                final int tmpColBase = j * complexity;

                tmpFirst = MatrixUtils.firstInColumn(right, j, tmpFirstInRow);
                tmpLimit = MatrixUtils.limitOfColumn(right, j, tmpLimitOfRow);

                tmpVal = PrimitiveMath.ZERO;
                for (int c = tmpFirst; c < tmpLimit; c++) {
                    tmpVal += tmpLeftRow[c] * right.doubleValue(c + tmpColBase);
                }
                product.set(i, j, tmpVal);
            }
        }
    }

    private MultiplyBoth() {
        super();
    }

    @Override
    public int threshold() {
        return THRESHOLD;
    }

}
