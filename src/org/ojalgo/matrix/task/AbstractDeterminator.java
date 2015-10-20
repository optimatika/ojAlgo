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
package org.ojalgo.matrix.task;

import org.ojalgo.access.Access2D;

abstract class AbstractDeterminator implements DeterminantTask<Double> {

    static final DeterminantTask<Double> FULL_1X1 = new AbstractDeterminator() {

        public Double calculateDeterminant(final Access2D<?> matrix) {
            return matrix.doubleValue(0L);
        }

    };

    static final DeterminantTask<Double> FULL_2X2 = new AbstractDeterminator() {

        public Double calculateDeterminant(final Access2D<?> matrix) {

            final double tmp00 = matrix.doubleValue(0L);
            final double tmp10 = matrix.doubleValue(1L);

            final double tmp01 = matrix.doubleValue(2L);
            final double tmp11 = matrix.doubleValue(3L);

            return AbstractDeterminator.calculate(tmp00, tmp10, tmp01, tmp11);
        }

    };

    static final DeterminantTask<Double> FULL_3X3 = new AbstractDeterminator() {

        public Double calculateDeterminant(final Access2D<?> matrix) {

            final double tmp00 = matrix.doubleValue(0L);
            final double tmp10 = matrix.doubleValue(1L);
            final double tmp20 = matrix.doubleValue(2L);

            final double tmp01 = matrix.doubleValue(3L);
            final double tmp11 = matrix.doubleValue(4L);
            final double tmp21 = matrix.doubleValue(5L);

            final double tmp02 = matrix.doubleValue(6L);
            final double tmp12 = matrix.doubleValue(7L);
            final double tmp22 = matrix.doubleValue(8L);

            return AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp01, tmp11, tmp21, tmp02, tmp12, tmp22);
        }

    };

    static final DeterminantTask<Double> FULL_4X4 = new AbstractDeterminator() {

        public Double calculateDeterminant(final Access2D<?> matrix) {

            final double tmp00 = matrix.doubleValue(0L);
            final double tmp10 = matrix.doubleValue(1L);
            final double tmp20 = matrix.doubleValue(2L);
            final double tmp30 = matrix.doubleValue(3L);

            final double tmp01 = matrix.doubleValue(4L);
            final double tmp11 = matrix.doubleValue(5L);
            final double tmp21 = matrix.doubleValue(6L);
            final double tmp31 = matrix.doubleValue(7L);

            final double tmp02 = matrix.doubleValue(8L);
            final double tmp12 = matrix.doubleValue(9L);
            final double tmp22 = matrix.doubleValue(10L);
            final double tmp32 = matrix.doubleValue(11L);

            final double tmp03 = matrix.doubleValue(12L);
            final double tmp13 = matrix.doubleValue(13L);
            final double tmp23 = matrix.doubleValue(14L);
            final double tmp33 = matrix.doubleValue(15L);

            return AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp30, tmp01, tmp11, tmp21, tmp31, tmp02, tmp12, tmp22, tmp32, tmp03, tmp13, tmp23,
                    tmp33);
        }

    };

    static final DeterminantTask<Double> FULL_5X5 = new AbstractDeterminator() {

        public Double calculateDeterminant(final Access2D<?> matrix) {

            final double tmp00 = matrix.doubleValue(0L);
            final double tmp10 = matrix.doubleValue(1L);
            final double tmp20 = matrix.doubleValue(2L);
            final double tmp30 = matrix.doubleValue(3L);
            final double tmp40 = matrix.doubleValue(4L);

            final double tmp01 = matrix.doubleValue(5L);
            final double tmp11 = matrix.doubleValue(6L);
            final double tmp21 = matrix.doubleValue(7L);
            final double tmp31 = matrix.doubleValue(8L);
            final double tmp41 = matrix.doubleValue(9L);

            final double tmp02 = matrix.doubleValue(10L);
            final double tmp12 = matrix.doubleValue(11L);
            final double tmp22 = matrix.doubleValue(12L);
            final double tmp32 = matrix.doubleValue(13L);
            final double tmp42 = matrix.doubleValue(14L);

            final double tmp03 = matrix.doubleValue(15L);
            final double tmp13 = matrix.doubleValue(16L);
            final double tmp23 = matrix.doubleValue(17L);
            final double tmp33 = matrix.doubleValue(18L);
            final double tmp43 = matrix.doubleValue(19L);

            final double tmp04 = matrix.doubleValue(20L);
            final double tmp14 = matrix.doubleValue(21L);
            final double tmp24 = matrix.doubleValue(22L);
            final double tmp34 = matrix.doubleValue(23L);
            final double tmp44 = matrix.doubleValue(24L);

            return AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp30, tmp40, tmp01, tmp11, tmp21, tmp31, tmp41, tmp02, tmp12, tmp22, tmp32, tmp42,
                    tmp03, tmp13, tmp23, tmp33, tmp43, tmp04, tmp14, tmp24, tmp34, tmp44);
        }

    };

    static final DeterminantTask<Double> SYMMETRIC_2X2 = new AbstractDeterminator() {

        public Double calculateDeterminant(final Access2D<?> matrix) {

            final double tmp00 = matrix.doubleValue(0L);
            final double tmp10 = matrix.doubleValue(1L);

            final double tmp11 = matrix.doubleValue(3L);

            return AbstractDeterminator.calculate(tmp00, tmp10, tmp10, tmp11);
        }

    };

    static final DeterminantTask<Double> SYMMETRIC_3X3 = new AbstractDeterminator() {

        public Double calculateDeterminant(final Access2D<?> matrix) {

            final double tmp00 = matrix.doubleValue(0L);
            final double tmp10 = matrix.doubleValue(1L);
            final double tmp20 = matrix.doubleValue(2L);

            final double tmp11 = matrix.doubleValue(4L);
            final double tmp21 = matrix.doubleValue(5L);

            final double tmp22 = matrix.doubleValue(8L);

            return AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp10, tmp11, tmp21, tmp20, tmp21, tmp22);
        }

    };

    static final DeterminantTask<Double> SYMMETRIC_4X4 = new AbstractDeterminator() {

        public Double calculateDeterminant(final Access2D<?> matrix) {

            final double tmp00 = matrix.doubleValue(0L);
            final double tmp10 = matrix.doubleValue(1L);
            final double tmp20 = matrix.doubleValue(2L);
            final double tmp30 = matrix.doubleValue(3L);

            final double tmp11 = matrix.doubleValue(5L);
            final double tmp21 = matrix.doubleValue(6L);
            final double tmp31 = matrix.doubleValue(7L);

            final double tmp22 = matrix.doubleValue(10L);
            final double tmp32 = matrix.doubleValue(11L);

            final double tmp33 = matrix.doubleValue(15L);

            return AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp30, tmp10, tmp11, tmp21, tmp31, tmp20, tmp21, tmp22, tmp32, tmp30, tmp31, tmp32,
                    tmp33);
        }

    };

    static final DeterminantTask<Double> SYMMETRIC_5X5 = new AbstractDeterminator() {

        public Double calculateDeterminant(final Access2D<?> matrix) {

            final double tmp00 = matrix.doubleValue(0L);
            final double tmp10 = matrix.doubleValue(1L);
            final double tmp20 = matrix.doubleValue(2L);
            final double tmp30 = matrix.doubleValue(3L);
            final double tmp40 = matrix.doubleValue(4L);

            final double tmp11 = matrix.doubleValue(6L);
            final double tmp21 = matrix.doubleValue(7L);
            final double tmp31 = matrix.doubleValue(8L);
            final double tmp41 = matrix.doubleValue(9L);

            final double tmp22 = matrix.doubleValue(12L);
            final double tmp32 = matrix.doubleValue(13L);
            final double tmp42 = matrix.doubleValue(14L);

            final double tmp33 = matrix.doubleValue(18L);
            final double tmp43 = matrix.doubleValue(19L);

            final double tmp44 = matrix.doubleValue(24L);

            return AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp30, tmp40, tmp10, tmp11, tmp21, tmp31, tmp41, tmp20, tmp21, tmp22, tmp32, tmp42,
                    tmp30, tmp31, tmp32, tmp33, tmp43, tmp40, tmp41, tmp42, tmp43, tmp44);
        }

    };

    static double calculate(final double a00, final double a10, final double a01, final double a11) {
        return (a00 * a11) - (a10 * a01);
    }

    static double calculate(final double a00, final double a10, final double a20, final double a01, final double a11, final double a21, final double a02,
            final double a12, final double a22) {
        return ((a00 * AbstractDeterminator.calculate(a11, a21, a12, a22)) - (a10 * AbstractDeterminator.calculate(a01, a21, a02, a22)))
                + (a20 * AbstractDeterminator.calculate(a01, a11, a02, a12));
    }

    static double calculate(final double a00, final double a10, final double a20, final double a30, final double a01, final double a11, final double a21,
            final double a31, final double a02, final double a12, final double a22, final double a32, final double a03, final double a13, final double a23,
            final double a33) {

        final double tmpDet2_01 = AbstractDeterminator.calculate(a02, a12, a03, a13);
        final double tmpDet2_02 = AbstractDeterminator.calculate(a02, a22, a03, a23);
        final double tmpDet2_03 = AbstractDeterminator.calculate(a02, a32, a03, a33);
        final double tmpDet2_12 = AbstractDeterminator.calculate(a12, a22, a13, a23);
        final double tmpDet2_13 = AbstractDeterminator.calculate(a12, a32, a13, a33);
        final double tmpDet2_23 = AbstractDeterminator.calculate(a22, a32, a23, a33);

        final double tmpDet3_123 = ((a11 * tmpDet2_23) - (a21 * tmpDet2_13)) + (a31 * tmpDet2_12);
        final double tmpDet3_023 = ((a01 * tmpDet2_23) - (a21 * tmpDet2_03)) + (a31 * tmpDet2_02);
        final double tmpDet3_013 = ((a01 * tmpDet2_13) - (a11 * tmpDet2_03)) + (a31 * tmpDet2_01);
        final double tmpDet3_012 = ((a01 * tmpDet2_12) - (a11 * tmpDet2_02)) + (a21 * tmpDet2_01);

        return (((a00 * tmpDet3_123) - (a10 * tmpDet3_023)) + (a20 * tmpDet3_013)) - (a30 * tmpDet3_012);
    }

    static double calculate(final double a00, final double a10, final double a20, final double a30, final double a40, final double a01, final double a11,
            final double a21, final double a31, final double a41, final double a02, final double a12, final double a22, final double a32, final double a42,
            final double a03, final double a13, final double a23, final double a33, final double a43, final double a04, final double a14, final double a24,
            final double a34, final double a44) {

        final double tmpDet2_01 = AbstractDeterminator.calculate(a03, a13, a04, a14);
        final double tmpDet2_02 = AbstractDeterminator.calculate(a03, a23, a04, a24);
        final double tmpDet2_03 = AbstractDeterminator.calculate(a03, a33, a04, a34);
        final double tmpDet2_04 = AbstractDeterminator.calculate(a03, a43, a04, a44);
        final double tmpDet2_12 = AbstractDeterminator.calculate(a13, a23, a14, a24);
        final double tmpDet2_13 = AbstractDeterminator.calculate(a13, a33, a14, a34);
        final double tmpDet2_14 = AbstractDeterminator.calculate(a13, a43, a14, a44);
        final double tmpDet2_23 = AbstractDeterminator.calculate(a23, a33, a24, a34);
        final double tmpDet2_24 = AbstractDeterminator.calculate(a23, a43, a24, a44);
        final double tmpDet2_34 = AbstractDeterminator.calculate(a33, a43, a34, a44);

        final double tmpDet3_012 = ((a02 * tmpDet2_12) - (a12 * tmpDet2_02)) + (a22 * tmpDet2_01);
        final double tmpDet3_013 = ((a02 * tmpDet2_13) - (a12 * tmpDet2_03)) + (a32 * tmpDet2_01);
        final double tmpDet3_014 = ((a02 * tmpDet2_14) - (a12 * tmpDet2_04)) + (a42 * tmpDet2_01);
        final double tmpDet3_023 = ((a02 * tmpDet2_23) - (a22 * tmpDet2_03)) + (a32 * tmpDet2_02);
        final double tmpDet3_024 = ((a02 * tmpDet2_24) - (a22 * tmpDet2_04)) + (a42 * tmpDet2_02);
        final double tmpDet3_034 = ((a02 * tmpDet2_34) - (a32 * tmpDet2_04)) + (a42 * tmpDet2_03);
        final double tmpDet3_123 = ((a12 * tmpDet2_23) - (a22 * tmpDet2_13)) + (a32 * tmpDet2_12);
        final double tmpDet3_124 = ((a12 * tmpDet2_24) - (a22 * tmpDet2_14)) + (a42 * tmpDet2_12);
        final double tmpDet3_134 = ((a12 * tmpDet2_34) - (a32 * tmpDet2_14)) + (a42 * tmpDet2_13);
        final double tmpDet3_234 = ((a22 * tmpDet2_34) - (a32 * tmpDet2_24)) + (a42 * tmpDet2_23);

        final double tmpDet4_1234 = (((a11 * tmpDet3_234) - (a21 * tmpDet3_134)) + (a31 * tmpDet3_124)) - (a41 * tmpDet3_123);
        final double tmpDet4_0234 = (((a01 * tmpDet3_234) - (a21 * tmpDet3_034)) + (a31 * tmpDet3_024)) - (a41 * tmpDet3_023);
        final double tmpDet4_0134 = (((a01 * tmpDet3_134) - (a11 * tmpDet3_034)) + (a31 * tmpDet3_014)) - (a41 * tmpDet3_013);
        final double tmpDet4_0124 = (((a01 * tmpDet3_124) - (a11 * tmpDet3_024)) + (a21 * tmpDet3_014)) - (a41 * tmpDet3_012);
        final double tmpDet4_0123 = (((a01 * tmpDet3_123) - (a11 * tmpDet3_023)) + (a21 * tmpDet3_013)) - (a31 * tmpDet3_012);

        return ((((a00 * tmpDet4_1234) - (a10 * tmpDet4_0234)) + (a20 * tmpDet4_0134)) - (a30 * tmpDet4_0124)) + (a40 * tmpDet4_0123);
    }

    AbstractDeterminator() {
        super();
    }

}
