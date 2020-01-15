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
package org.ojalgo.matrix.task;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;

public abstract class AbstractInverter implements InverterTask<Double> {

    static final InverterTask<Double> FULL_1X1 = new AbstractInverter() {

        public MatrixStore<Double> invert(final Access2D<?> original, final PhysicalStore<Double> preallocated) throws RecoverableCondition {
            AbstractInverter.full1X1(original, preallocated);
            return preallocated;
        }

        @Override
        long dim() {
            return 1L;
        }

    };

    static final InverterTask<Double> FULL_2X2 = new AbstractInverter() {

        public MatrixStore<Double> invert(final Access2D<?> original, final PhysicalStore<Double> preallocated) throws RecoverableCondition {
            AbstractInverter.full2X2(original, preallocated);
            return preallocated;
        }

        @Override
        long dim() {
            return 2L;
        }

    };

    static final InverterTask<Double> FULL_3X3 = new AbstractInverter() {

        public MatrixStore<Double> invert(final Access2D<?> original, final PhysicalStore<Double> preallocated) throws RecoverableCondition {
            AbstractInverter.full3X3(original, preallocated);
            return preallocated;
        }

        @Override
        long dim() {
            return 3L;
        }

    };

    static final InverterTask<Double> FULL_4X4 = new AbstractInverter() {

        public MatrixStore<Double> invert(final Access2D<?> original, final PhysicalStore<Double> preallocated) throws RecoverableCondition {
            AbstractInverter.full4X4(original, preallocated);
            return preallocated;
        }

        @Override
        long dim() {
            return 4L;
        }

    };

    static final InverterTask<Double> FULL_5X5 = new AbstractInverter() {

        public MatrixStore<Double> invert(final Access2D<?> original, final PhysicalStore<Double> preallocated) throws RecoverableCondition {
            AbstractInverter.full5X5(original, preallocated);
            return preallocated;
        }

        @Override
        long dim() {
            return 5L;
        }

    };

    static final InverterTask<Double> SYMMETRIC_2X2 = new AbstractInverter() {

        public MatrixStore<Double> invert(final Access2D<?> original, final PhysicalStore<Double> preallocated) throws RecoverableCondition {
            AbstractInverter.symmetric2X2(original, preallocated);
            return preallocated;
        }

        @Override
        long dim() {
            return 2L;
        }

    };

    static final InverterTask<Double> SYMMETRIC_3X3 = new AbstractInverter() {

        public MatrixStore<Double> invert(final Access2D<?> original, final PhysicalStore<Double> preallocated) throws RecoverableCondition {
            AbstractInverter.symmetric3X3(original, preallocated);
            return preallocated;
        }

        @Override
        long dim() {
            return 3L;
        }

    };

    static final InverterTask<Double> SYMMETRIC_4X4 = new AbstractInverter() {

        public MatrixStore<Double> invert(final Access2D<?> original, final PhysicalStore<Double> preallocated) throws RecoverableCondition {
            AbstractInverter.symmetric4X4(original, preallocated);
            return preallocated;
        }

        @Override
        long dim() {
            return 4L;
        }

    };

    static final InverterTask<Double> SYMMETRIC_5X5 = new AbstractInverter() {

        public MatrixStore<Double> invert(final Access2D<?> original, final PhysicalStore<Double> preallocated) throws RecoverableCondition {
            AbstractInverter.symmetric5X5(original, preallocated);
            return preallocated;
        }

        @Override
        long dim() {
            return 5L;
        }

    };

    static void full1X1(final Access2D<?> source, final PhysicalStore<?> destination) {
        destination.set(0L, ONE / source.doubleValue(0L));
    }

    static void full2X2(final Access2D<?> source, final PhysicalStore<?> destination) {

        double tmp00 = source.doubleValue(0L);
        double tmp10 = source.doubleValue(1L);

        double tmp01 = source.doubleValue(2L);
        double tmp11 = source.doubleValue(3L);

        final double tmpScale = MissingMath.norm(tmp00, tmp01, tmp10, tmp11);

        tmp00 /= tmpScale;
        tmp10 /= tmpScale;

        tmp01 /= tmpScale;
        tmp11 /= tmpScale;

        final double tmpDet = tmpScale * AbstractDeterminator.calculate(tmp00, tmp10, tmp01, tmp11);

        destination.set(0L, tmp11 / tmpDet);
        destination.set(1L, -tmp10 / tmpDet);

        destination.set(2L, -tmp01 / tmpDet);
        destination.set(3L, tmp00 / tmpDet);
    }

    static void full3X3(final Access2D<?> source, final PhysicalStore<?> destination) {

        double tmp00 = source.doubleValue(0L);
        double tmp10 = source.doubleValue(1L);
        double tmp20 = source.doubleValue(2L);

        double tmp01 = source.doubleValue(3L);
        double tmp11 = source.doubleValue(4L);
        double tmp21 = source.doubleValue(5L);

        double tmp02 = source.doubleValue(6L);
        double tmp12 = source.doubleValue(7L);
        double tmp22 = source.doubleValue(8L);

        final double tmpScale = MissingMath.norm(tmp00, tmp01, tmp02, tmp10, tmp11, tmp12, tmp20, tmp21, tmp22);

        tmp00 /= tmpScale;
        tmp10 /= tmpScale;
        tmp20 /= tmpScale;

        tmp01 /= tmpScale;
        tmp11 /= tmpScale;
        tmp21 /= tmpScale;

        tmp02 /= tmpScale;
        tmp12 /= tmpScale;
        tmp22 /= tmpScale;

        final double tmpMin00 = AbstractDeterminator.calculate(tmp11, tmp21, tmp12, tmp22);
        final double tmpMin10 = AbstractDeterminator.calculate(tmp01, tmp21, tmp02, tmp22);
        final double tmpMin20 = AbstractDeterminator.calculate(tmp01, tmp11, tmp02, tmp12);

        final double tmpMin01 = AbstractDeterminator.calculate(tmp10, tmp20, tmp12, tmp22);
        final double tmpMin11 = AbstractDeterminator.calculate(tmp00, tmp20, tmp02, tmp22);
        final double tmpMin21 = AbstractDeterminator.calculate(tmp00, tmp10, tmp02, tmp12);

        final double tmpMin02 = AbstractDeterminator.calculate(tmp10, tmp20, tmp11, tmp21);
        final double tmpMin12 = AbstractDeterminator.calculate(tmp00, tmp20, tmp01, tmp21);
        final double tmpMin22 = AbstractDeterminator.calculate(tmp00, tmp10, tmp01, tmp11);

        final double tmpDet = tmpScale * (((tmp00 * tmpMin00) - (tmp10 * tmpMin10)) + (tmp20 * tmpMin20));

        destination.set(0L, tmpMin00 / tmpDet);
        destination.set(1L, -tmpMin01 / tmpDet);
        destination.set(2L, tmpMin02 / tmpDet);

        destination.set(3L, -tmpMin10 / tmpDet);
        destination.set(4L, tmpMin11 / tmpDet);
        destination.set(5L, -tmpMin12 / tmpDet);

        destination.set(6L, tmpMin20 / tmpDet);
        destination.set(7L, -tmpMin21 / tmpDet);
        destination.set(8L, tmpMin22 / tmpDet);
    }

    static void full4X4(final Access2D<?> source, final PhysicalStore<?> destination) {

        double tmp00 = source.doubleValue(0L);
        double tmp10 = source.doubleValue(1L);
        double tmp20 = source.doubleValue(2L);
        double tmp30 = source.doubleValue(3L);

        double tmp01 = source.doubleValue(4L);
        double tmp11 = source.doubleValue(5L);
        double tmp21 = source.doubleValue(6L);
        double tmp31 = source.doubleValue(7L);

        double tmp02 = source.doubleValue(8L);
        double tmp12 = source.doubleValue(9L);
        double tmp22 = source.doubleValue(10L);
        double tmp32 = source.doubleValue(11L);

        double tmp03 = source.doubleValue(12L);
        double tmp13 = source.doubleValue(13L);
        double tmp23 = source.doubleValue(14L);
        double tmp33 = source.doubleValue(15L);

        final double tmpScale = MissingMath.norm(tmp00, tmp01, tmp02, tmp03, tmp10, tmp11, tmp12, tmp13, tmp20, tmp21, tmp22, tmp23, tmp30, tmp31, tmp32,
                tmp33);

        tmp00 /= tmpScale;
        tmp10 /= tmpScale;
        tmp20 /= tmpScale;
        tmp30 /= tmpScale;

        tmp01 /= tmpScale;
        tmp11 /= tmpScale;
        tmp21 /= tmpScale;
        tmp31 /= tmpScale;

        tmp02 /= tmpScale;
        tmp12 /= tmpScale;
        tmp22 /= tmpScale;
        tmp32 /= tmpScale;

        tmp03 /= tmpScale;
        tmp13 /= tmpScale;
        tmp23 /= tmpScale;
        tmp33 /= tmpScale;

        final double tmpMin00 = AbstractDeterminator.calculate(tmp11, tmp21, tmp31, tmp12, tmp22, tmp32, tmp13, tmp23, tmp33);
        final double tmpMin10 = AbstractDeterminator.calculate(tmp01, tmp21, tmp31, tmp02, tmp22, tmp32, tmp03, tmp23, tmp33);
        final double tmpMin20 = AbstractDeterminator.calculate(tmp01, tmp11, tmp31, tmp02, tmp12, tmp32, tmp03, tmp13, tmp33);
        final double tmpMin30 = AbstractDeterminator.calculate(tmp01, tmp11, tmp21, tmp02, tmp12, tmp22, tmp03, tmp13, tmp23);

        final double tmpMin01 = AbstractDeterminator.calculate(tmp10, tmp20, tmp30, tmp12, tmp22, tmp32, tmp13, tmp23, tmp33);
        final double tmpMin11 = AbstractDeterminator.calculate(tmp00, tmp20, tmp30, tmp02, tmp22, tmp32, tmp03, tmp23, tmp33);
        final double tmpMin21 = AbstractDeterminator.calculate(tmp00, tmp10, tmp30, tmp02, tmp12, tmp32, tmp03, tmp13, tmp33);
        final double tmpMin31 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp02, tmp12, tmp22, tmp03, tmp13, tmp23);

        final double tmpMin02 = AbstractDeterminator.calculate(tmp10, tmp20, tmp30, tmp11, tmp21, tmp31, tmp13, tmp23, tmp33);
        final double tmpMin12 = AbstractDeterminator.calculate(tmp00, tmp20, tmp30, tmp01, tmp21, tmp31, tmp03, tmp23, tmp33);
        final double tmpMin22 = AbstractDeterminator.calculate(tmp00, tmp10, tmp30, tmp01, tmp11, tmp31, tmp03, tmp13, tmp33);
        final double tmpMin32 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp01, tmp11, tmp21, tmp03, tmp13, tmp23);

        final double tmpMin03 = AbstractDeterminator.calculate(tmp10, tmp20, tmp30, tmp11, tmp21, tmp31, tmp12, tmp22, tmp32);
        final double tmpMin13 = AbstractDeterminator.calculate(tmp00, tmp20, tmp30, tmp01, tmp21, tmp31, tmp02, tmp22, tmp32);
        final double tmpMin23 = AbstractDeterminator.calculate(tmp00, tmp10, tmp30, tmp01, tmp11, tmp31, tmp02, tmp12, tmp32);
        final double tmpMin33 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp01, tmp11, tmp21, tmp02, tmp12, tmp22);

        final double tmpDet = tmpScale * ((((tmp00 * tmpMin00) - (tmp10 * tmpMin10)) + (tmp20 * tmpMin20)) - (tmp30 * tmpMin30));

        destination.set(0L, tmpMin00 / tmpDet);
        destination.set(1L, -tmpMin01 / tmpDet);
        destination.set(2L, tmpMin02 / tmpDet);
        destination.set(3L, -tmpMin03 / tmpDet);

        destination.set(4L, -tmpMin10 / tmpDet);
        destination.set(5L, tmpMin11 / tmpDet);
        destination.set(6L, -tmpMin12 / tmpDet);
        destination.set(7L, tmpMin13 / tmpDet);

        destination.set(8L, tmpMin20 / tmpDet);
        destination.set(9L, -tmpMin21 / tmpDet);
        destination.set(10L, tmpMin22 / tmpDet);
        destination.set(11L, -tmpMin23 / tmpDet);

        destination.set(12L, -tmpMin30 / tmpDet);
        destination.set(13L, tmpMin31 / tmpDet);
        destination.set(14L, -tmpMin32 / tmpDet);
        destination.set(15L, tmpMin33 / tmpDet);
    }

    static void full5X5(final Access2D<?> source, final PhysicalStore<?> destination) {

        double tmp00 = source.doubleValue(0L);
        double tmp10 = source.doubleValue(1L);
        double tmp20 = source.doubleValue(2L);
        double tmp30 = source.doubleValue(3L);
        double tmp40 = source.doubleValue(4L);

        double tmp01 = source.doubleValue(5L);
        double tmp11 = source.doubleValue(6L);
        double tmp21 = source.doubleValue(7L);
        double tmp31 = source.doubleValue(8L);
        double tmp41 = source.doubleValue(9L);

        double tmp02 = source.doubleValue(10L);
        double tmp12 = source.doubleValue(11L);
        double tmp22 = source.doubleValue(12L);
        double tmp32 = source.doubleValue(13L);
        double tmp42 = source.doubleValue(14L);

        double tmp03 = source.doubleValue(15L);
        double tmp13 = source.doubleValue(16L);
        double tmp23 = source.doubleValue(17L);
        double tmp33 = source.doubleValue(18L);
        double tmp43 = source.doubleValue(19L);

        double tmp04 = source.doubleValue(20L);
        double tmp14 = source.doubleValue(21L);
        double tmp24 = source.doubleValue(22L);
        double tmp34 = source.doubleValue(23L);
        double tmp44 = source.doubleValue(24L);

        final double tmpScale = MissingMath.norm(tmp00, tmp01, tmp02, tmp03, tmp04, tmp10, tmp11, tmp12, tmp13, tmp14, tmp20, tmp21, tmp22, tmp23, tmp24, tmp30,
                tmp31, tmp32, tmp33, tmp34, tmp40, tmp41, tmp42, tmp43, tmp44);

        tmp00 /= tmpScale;
        tmp10 /= tmpScale;
        tmp20 /= tmpScale;
        tmp30 /= tmpScale;
        tmp40 /= tmpScale;

        tmp01 /= tmpScale;
        tmp11 /= tmpScale;
        tmp21 /= tmpScale;
        tmp31 /= tmpScale;
        tmp41 /= tmpScale;

        tmp02 /= tmpScale;
        tmp12 /= tmpScale;
        tmp22 /= tmpScale;
        tmp32 /= tmpScale;
        tmp42 /= tmpScale;

        tmp03 /= tmpScale;
        tmp13 /= tmpScale;
        tmp23 /= tmpScale;
        tmp33 /= tmpScale;
        tmp43 /= tmpScale;

        tmp04 /= tmpScale;
        tmp14 /= tmpScale;
        tmp24 /= tmpScale;
        tmp34 /= tmpScale;
        tmp44 /= tmpScale;

        final double tmpMin00 = AbstractDeterminator.calculate(tmp11, tmp21, tmp31, tmp41, tmp12, tmp22, tmp32, tmp42, tmp13, tmp23, tmp33, tmp43, tmp14, tmp24,
                tmp34, tmp44);
        final double tmpMin10 = AbstractDeterminator.calculate(tmp01, tmp21, tmp31, tmp41, tmp02, tmp22, tmp32, tmp42, tmp03, tmp23, tmp33, tmp43, tmp04, tmp24,
                tmp34, tmp44);
        final double tmpMin20 = AbstractDeterminator.calculate(tmp01, tmp11, tmp31, tmp41, tmp02, tmp12, tmp32, tmp42, tmp03, tmp13, tmp33, tmp43, tmp04, tmp14,
                tmp34, tmp44);
        final double tmpMin30 = AbstractDeterminator.calculate(tmp01, tmp11, tmp21, tmp41, tmp02, tmp12, tmp22, tmp42, tmp03, tmp13, tmp23, tmp43, tmp04, tmp14,
                tmp24, tmp44);
        final double tmpMin40 = AbstractDeterminator.calculate(tmp01, tmp11, tmp21, tmp31, tmp02, tmp12, tmp22, tmp32, tmp03, tmp13, tmp23, tmp33, tmp04, tmp14,
                tmp24, tmp34);

        final double tmpMin01 = AbstractDeterminator.calculate(tmp10, tmp20, tmp30, tmp40, tmp12, tmp22, tmp32, tmp42, tmp13, tmp23, tmp33, tmp43, tmp14, tmp24,
                tmp34, tmp44);
        final double tmpMin11 = AbstractDeterminator.calculate(tmp00, tmp20, tmp30, tmp40, tmp02, tmp22, tmp32, tmp42, tmp03, tmp23, tmp33, tmp43, tmp04, tmp24,
                tmp34, tmp44);
        final double tmpMin21 = AbstractDeterminator.calculate(tmp00, tmp10, tmp30, tmp40, tmp02, tmp12, tmp32, tmp42, tmp03, tmp13, tmp33, tmp43, tmp04, tmp14,
                tmp34, tmp44);
        final double tmpMin31 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp40, tmp02, tmp12, tmp22, tmp42, tmp03, tmp13, tmp23, tmp43, tmp04, tmp14,
                tmp24, tmp44);
        final double tmpMin41 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp30, tmp02, tmp12, tmp22, tmp32, tmp03, tmp13, tmp23, tmp33, tmp04, tmp14,
                tmp24, tmp34);

        final double tmpMin02 = AbstractDeterminator.calculate(tmp10, tmp20, tmp30, tmp40, tmp11, tmp21, tmp31, tmp41, tmp13, tmp23, tmp33, tmp43, tmp14, tmp24,
                tmp34, tmp44);
        final double tmpMin12 = AbstractDeterminator.calculate(tmp00, tmp20, tmp30, tmp40, tmp01, tmp21, tmp31, tmp41, tmp03, tmp23, tmp33, tmp43, tmp04, tmp24,
                tmp34, tmp44);
        final double tmpMin22 = AbstractDeterminator.calculate(tmp00, tmp10, tmp30, tmp40, tmp01, tmp11, tmp31, tmp41, tmp03, tmp13, tmp33, tmp43, tmp04, tmp14,
                tmp34, tmp44);
        final double tmpMin32 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp40, tmp01, tmp11, tmp21, tmp41, tmp03, tmp13, tmp23, tmp43, tmp04, tmp14,
                tmp24, tmp44);
        final double tmpMin42 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp30, tmp01, tmp11, tmp21, tmp31, tmp03, tmp13, tmp23, tmp33, tmp04, tmp14,
                tmp24, tmp34);

        final double tmpMin03 = AbstractDeterminator.calculate(tmp10, tmp20, tmp30, tmp40, tmp11, tmp21, tmp31, tmp41, tmp12, tmp22, tmp32, tmp42, tmp14, tmp24,
                tmp34, tmp44);
        final double tmpMin13 = AbstractDeterminator.calculate(tmp00, tmp20, tmp30, tmp40, tmp01, tmp21, tmp31, tmp41, tmp02, tmp22, tmp32, tmp42, tmp04, tmp24,
                tmp34, tmp44);
        final double tmpMin23 = AbstractDeterminator.calculate(tmp00, tmp10, tmp30, tmp40, tmp01, tmp11, tmp31, tmp41, tmp02, tmp12, tmp32, tmp42, tmp04, tmp14,
                tmp34, tmp44);
        final double tmpMin33 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp40, tmp01, tmp11, tmp21, tmp41, tmp02, tmp12, tmp22, tmp42, tmp04, tmp14,
                tmp24, tmp44);
        final double tmpMin43 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp30, tmp01, tmp11, tmp21, tmp31, tmp02, tmp12, tmp22, tmp32, tmp04, tmp14,
                tmp24, tmp34);

        final double tmpMin04 = AbstractDeterminator.calculate(tmp10, tmp20, tmp30, tmp40, tmp11, tmp21, tmp31, tmp41, tmp12, tmp22, tmp32, tmp42, tmp13, tmp23,
                tmp33, tmp43);
        final double tmpMin14 = AbstractDeterminator.calculate(tmp00, tmp20, tmp30, tmp40, tmp01, tmp21, tmp31, tmp41, tmp02, tmp22, tmp32, tmp42, tmp03, tmp23,
                tmp33, tmp43);
        final double tmpMin24 = AbstractDeterminator.calculate(tmp00, tmp10, tmp30, tmp40, tmp01, tmp11, tmp31, tmp41, tmp02, tmp12, tmp32, tmp42, tmp03, tmp13,
                tmp33, tmp43);
        final double tmpMin34 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp40, tmp01, tmp11, tmp21, tmp41, tmp02, tmp12, tmp22, tmp42, tmp03, tmp13,
                tmp23, tmp43);
        final double tmpMin44 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp30, tmp01, tmp11, tmp21, tmp31, tmp02, tmp12, tmp22, tmp32, tmp03, tmp13,
                tmp23, tmp33);

        final double tmpDet = tmpScale * (((((tmp00 * tmpMin00) - (tmp10 * tmpMin10)) + (tmp20 * tmpMin20)) - (tmp30 * tmpMin30)) + (tmp40 * tmpMin40));

        destination.set(0L, tmpMin00 / tmpDet);
        destination.set(1L, -tmpMin01 / tmpDet);
        destination.set(2L, tmpMin02 / tmpDet);
        destination.set(3L, -tmpMin03 / tmpDet);
        destination.set(4L, tmpMin04 / tmpDet);

        destination.set(5L, -tmpMin10 / tmpDet);
        destination.set(6L, tmpMin11 / tmpDet);
        destination.set(7L, -tmpMin12 / tmpDet);
        destination.set(8L, tmpMin13 / tmpDet);
        destination.set(9L, -tmpMin14 / tmpDet);

        destination.set(10L, tmpMin20 / tmpDet);
        destination.set(11L, -tmpMin21 / tmpDet);
        destination.set(12L, tmpMin22 / tmpDet);
        destination.set(13L, -tmpMin23 / tmpDet);
        destination.set(14L, tmpMin24 / tmpDet);

        destination.set(15L, -tmpMin30 / tmpDet);
        destination.set(16L, tmpMin31 / tmpDet);
        destination.set(17L, -tmpMin32 / tmpDet);
        destination.set(18L, tmpMin33 / tmpDet);
        destination.set(19L, -tmpMin34 / tmpDet);

        destination.set(20L, tmpMin40 / tmpDet);
        destination.set(21L, -tmpMin41 / tmpDet);
        destination.set(22L, tmpMin42 / tmpDet);
        destination.set(23L, -tmpMin43 / tmpDet);
        destination.set(24L, tmpMin44 / tmpDet);
    }

    static void symmetric2X2(final Access2D<?> source, final PhysicalStore<?> destination) {

        double tmp00 = source.doubleValue(0L);
        double tmp10 = source.doubleValue(1L);

        double tmp11 = source.doubleValue(3L);

        final double tmpScale = MissingMath.norm(tmp00, tmp10, tmp11);

        tmp00 /= tmpScale;
        tmp10 /= tmpScale;

        tmp11 /= tmpScale;

        final double tmpDet = tmpScale * AbstractDeterminator.calculate(tmp00, tmp10, tmp10, tmp11);

        destination.set(0L, tmp11 / tmpDet);
        destination.set(1L, -tmp10 / tmpDet);

        destination.set(2L, -tmp10 / tmpDet);
        destination.set(3L, tmp00 / tmpDet);
    }

    static void symmetric3X3(final Access2D<?> source, final PhysicalStore<?> destination) {

        double tmp00 = source.doubleValue(0L);
        double tmp10 = source.doubleValue(1L);
        double tmp20 = source.doubleValue(2L);

        double tmp11 = source.doubleValue(4L);
        double tmp21 = source.doubleValue(5L);

        double tmp22 = source.doubleValue(8L);

        final double tmpScale = MissingMath.norm(tmp00, tmp10, tmp11, tmp20, tmp21, tmp22);

        tmp00 /= tmpScale;
        tmp10 /= tmpScale;
        tmp20 /= tmpScale;

        tmp11 /= tmpScale;
        tmp21 /= tmpScale;

        tmp22 /= tmpScale;

        final double tmpMin00 = AbstractDeterminator.calculate(tmp11, tmp21, tmp21, tmp22);
        final double tmpMin10 = AbstractDeterminator.calculate(tmp10, tmp21, tmp20, tmp22);
        final double tmpMin20 = AbstractDeterminator.calculate(tmp10, tmp11, tmp20, tmp21);

        final double tmpMin11 = AbstractDeterminator.calculate(tmp00, tmp20, tmp20, tmp22);
        final double tmpMin21 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp21);

        final double tmpMin22 = AbstractDeterminator.calculate(tmp00, tmp10, tmp10, tmp11);

        final double tmpDet = tmpScale * (((tmp00 * tmpMin00) - (tmp10 * tmpMin10)) + (tmp20 * tmpMin20));

        destination.set(0L, tmpMin00 / tmpDet);
        destination.set(1L, -tmpMin10 / tmpDet);
        destination.set(2L, tmpMin20 / tmpDet);

        destination.set(3L, -tmpMin10 / tmpDet);
        destination.set(4L, tmpMin11 / tmpDet);
        destination.set(5L, -tmpMin21 / tmpDet);

        destination.set(6L, tmpMin20 / tmpDet);
        destination.set(7L, -tmpMin21 / tmpDet);
        destination.set(8L, tmpMin22 / tmpDet);
    }

    static void symmetric4X4(final Access2D<?> source, final PhysicalStore<?> destination) {

        double tmp00 = source.doubleValue(0L);
        double tmp10 = source.doubleValue(1L);
        double tmp20 = source.doubleValue(2L);
        double tmp30 = source.doubleValue(3L);

        double tmp11 = source.doubleValue(5L);
        double tmp21 = source.doubleValue(6L);
        double tmp31 = source.doubleValue(7L);

        double tmp22 = source.doubleValue(10L);
        double tmp32 = source.doubleValue(11L);

        double tmp33 = source.doubleValue(15L);

        final double tmpScale = MissingMath.norm(tmp00, tmp10, tmp11, tmp20, tmp21, tmp22, tmp30, tmp31, tmp32, tmp33);

        tmp00 /= tmpScale;
        tmp10 /= tmpScale;
        tmp20 /= tmpScale;
        tmp30 /= tmpScale;

        tmp11 /= tmpScale;
        tmp21 /= tmpScale;
        tmp31 /= tmpScale;

        tmp22 /= tmpScale;
        tmp32 /= tmpScale;

        tmp33 /= tmpScale;

        final double tmpMin00 = AbstractDeterminator.calculate(tmp11, tmp21, tmp31, tmp21, tmp22, tmp32, tmp31, tmp32, tmp33);
        final double tmpMin10 = AbstractDeterminator.calculate(tmp10, tmp21, tmp31, tmp20, tmp22, tmp32, tmp30, tmp32, tmp33);
        final double tmpMin20 = AbstractDeterminator.calculate(tmp10, tmp11, tmp31, tmp20, tmp21, tmp32, tmp30, tmp31, tmp33);
        final double tmpMin30 = AbstractDeterminator.calculate(tmp10, tmp11, tmp21, tmp20, tmp21, tmp22, tmp30, tmp31, tmp32);

        final double tmpMin11 = AbstractDeterminator.calculate(tmp00, tmp20, tmp30, tmp20, tmp22, tmp32, tmp30, tmp32, tmp33);
        final double tmpMin21 = AbstractDeterminator.calculate(tmp00, tmp10, tmp30, tmp20, tmp21, tmp32, tmp30, tmp31, tmp33);
        final double tmpMin31 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp20, tmp21, tmp22, tmp30, tmp31, tmp32);

        final double tmpMin22 = AbstractDeterminator.calculate(tmp00, tmp10, tmp30, tmp10, tmp11, tmp31, tmp30, tmp31, tmp33);
        final double tmpMin32 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp10, tmp11, tmp21, tmp30, tmp31, tmp32);

        final double tmpMin33 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp10, tmp11, tmp21, tmp20, tmp21, tmp22);

        final double tmpDet = tmpScale * ((((tmp00 * tmpMin00) - (tmp10 * tmpMin10)) + (tmp20 * tmpMin20)) - (tmp30 * tmpMin30));

        destination.set(0L, tmpMin00 / tmpDet);
        destination.set(1L, -tmpMin10 / tmpDet);
        destination.set(2L, tmpMin20 / tmpDet);
        destination.set(3L, -tmpMin30 / tmpDet);

        destination.set(4L, -tmpMin10 / tmpDet);
        destination.set(5L, tmpMin11 / tmpDet);
        destination.set(6L, -tmpMin21 / tmpDet);
        destination.set(7L, tmpMin31 / tmpDet);

        destination.set(8L, tmpMin20 / tmpDet);
        destination.set(9L, -tmpMin21 / tmpDet);
        destination.set(10L, tmpMin22 / tmpDet);
        destination.set(11L, -tmpMin32 / tmpDet);

        destination.set(12L, -tmpMin30 / tmpDet);
        destination.set(13L, tmpMin31 / tmpDet);
        destination.set(14L, -tmpMin32 / tmpDet);
        destination.set(15L, tmpMin33 / tmpDet);
    }

    static void symmetric5X5(final Access2D<?> source, final PhysicalStore<?> destination) {

        double tmp00 = source.doubleValue(0L);
        double tmp10 = source.doubleValue(1L);
        double tmp20 = source.doubleValue(2L);
        double tmp30 = source.doubleValue(3L);
        double tmp40 = source.doubleValue(4L);

        double tmp11 = source.doubleValue(6L);
        double tmp21 = source.doubleValue(7L);
        double tmp31 = source.doubleValue(8L);
        double tmp41 = source.doubleValue(9L);

        double tmp22 = source.doubleValue(12L);
        double tmp32 = source.doubleValue(13L);
        double tmp42 = source.doubleValue(14L);

        double tmp33 = source.doubleValue(18L);
        double tmp43 = source.doubleValue(19L);

        double tmp44 = source.doubleValue(24L);

        final double tmpScale = MissingMath.norm(tmp00, tmp10, tmp11, tmp20, tmp21, tmp22, tmp30, tmp31, tmp32, tmp33, tmp40, tmp41, tmp42, tmp43, tmp44);

        tmp00 /= tmpScale;
        tmp10 /= tmpScale;
        tmp20 /= tmpScale;
        tmp30 /= tmpScale;
        tmp40 /= tmpScale;

        tmp11 /= tmpScale;
        tmp21 /= tmpScale;
        tmp31 /= tmpScale;
        tmp41 /= tmpScale;

        tmp22 /= tmpScale;
        tmp32 /= tmpScale;
        tmp42 /= tmpScale;

        tmp33 /= tmpScale;
        tmp43 /= tmpScale;

        tmp44 /= tmpScale;

        final double tmpMin00 = AbstractDeterminator.calculate(tmp11, tmp21, tmp31, tmp41, tmp21, tmp22, tmp32, tmp42, tmp31, tmp32, tmp33, tmp43, tmp41, tmp42,
                tmp43, tmp44);
        final double tmpMin10 = AbstractDeterminator.calculate(tmp10, tmp21, tmp31, tmp41, tmp20, tmp22, tmp32, tmp42, tmp30, tmp32, tmp33, tmp43, tmp40, tmp42,
                tmp43, tmp44);
        final double tmpMin20 = AbstractDeterminator.calculate(tmp10, tmp11, tmp31, tmp41, tmp20, tmp21, tmp32, tmp42, tmp30, tmp31, tmp33, tmp43, tmp40, tmp41,
                tmp43, tmp44);
        final double tmpMin30 = AbstractDeterminator.calculate(tmp10, tmp11, tmp21, tmp41, tmp20, tmp21, tmp22, tmp42, tmp30, tmp31, tmp32, tmp43, tmp40, tmp41,
                tmp42, tmp44);
        final double tmpMin40 = AbstractDeterminator.calculate(tmp10, tmp11, tmp21, tmp31, tmp20, tmp21, tmp22, tmp32, tmp30, tmp31, tmp32, tmp33, tmp40, tmp41,
                tmp42, tmp43);

        final double tmpMin11 = AbstractDeterminator.calculate(tmp00, tmp20, tmp30, tmp40, tmp20, tmp22, tmp32, tmp42, tmp30, tmp32, tmp33, tmp43, tmp40, tmp42,
                tmp43, tmp44);
        final double tmpMin21 = AbstractDeterminator.calculate(tmp00, tmp10, tmp30, tmp40, tmp20, tmp21, tmp32, tmp42, tmp30, tmp31, tmp33, tmp43, tmp40, tmp41,
                tmp43, tmp44);
        final double tmpMin31 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp40, tmp20, tmp21, tmp22, tmp42, tmp30, tmp31, tmp32, tmp43, tmp40, tmp41,
                tmp42, tmp44);
        final double tmpMin41 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp30, tmp20, tmp21, tmp22, tmp32, tmp30, tmp31, tmp32, tmp33, tmp40, tmp41,
                tmp42, tmp43);

        final double tmpMin22 = AbstractDeterminator.calculate(tmp00, tmp10, tmp30, tmp40, tmp10, tmp11, tmp31, tmp41, tmp30, tmp31, tmp33, tmp43, tmp40, tmp41,
                tmp43, tmp44);
        final double tmpMin32 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp40, tmp10, tmp11, tmp21, tmp41, tmp30, tmp31, tmp32, tmp43, tmp40, tmp41,
                tmp42, tmp44);
        final double tmpMin42 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp30, tmp10, tmp11, tmp21, tmp31, tmp30, tmp31, tmp32, tmp33, tmp40, tmp41,
                tmp42, tmp43);

        final double tmpMin33 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp40, tmp10, tmp11, tmp21, tmp41, tmp20, tmp21, tmp22, tmp42, tmp40, tmp41,
                tmp42, tmp44);
        final double tmpMin43 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp30, tmp10, tmp11, tmp21, tmp31, tmp20, tmp21, tmp22, tmp32, tmp40, tmp41,
                tmp42, tmp43);

        final double tmpMin44 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp30, tmp10, tmp11, tmp21, tmp31, tmp20, tmp21, tmp22, tmp32, tmp30, tmp31,
                tmp32, tmp33);

        final double tmpDet = tmpScale * (((((tmp00 * tmpMin00) - (tmp10 * tmpMin10)) + (tmp20 * tmpMin20)) - (tmp30 * tmpMin30)) + (tmp40 * tmpMin40));

        destination.set(0L, tmpMin00 / tmpDet);
        destination.set(1L, -tmpMin10 / tmpDet);
        destination.set(2L, tmpMin20 / tmpDet);
        destination.set(3L, -tmpMin30 / tmpDet);
        destination.set(4L, tmpMin40 / tmpDet);

        destination.set(5L, -tmpMin10 / tmpDet);
        destination.set(6L, tmpMin11 / tmpDet);
        destination.set(7L, -tmpMin21 / tmpDet);
        destination.set(8L, tmpMin31 / tmpDet);
        destination.set(9L, -tmpMin41 / tmpDet);

        destination.set(10L, tmpMin20 / tmpDet);
        destination.set(11L, -tmpMin21 / tmpDet);
        destination.set(12L, tmpMin22 / tmpDet);
        destination.set(13L, -tmpMin32 / tmpDet);
        destination.set(14L, tmpMin42 / tmpDet);

        destination.set(15L, -tmpMin30 / tmpDet);
        destination.set(16L, tmpMin31 / tmpDet);
        destination.set(17L, -tmpMin32 / tmpDet);
        destination.set(18L, tmpMin33 / tmpDet);
        destination.set(19L, -tmpMin43 / tmpDet);

        destination.set(20L, tmpMin40 / tmpDet);
        destination.set(21L, -tmpMin41 / tmpDet);
        destination.set(22L, tmpMin42 / tmpDet);
        destination.set(23L, -tmpMin43 / tmpDet);
        destination.set(24L, tmpMin44 / tmpDet);
    }

    AbstractInverter() {
        super();
    }

    public final MatrixStore<Double> invert(final Access2D<?> original) throws RecoverableCondition {
        return this.invert(original, Primitive64Store.FACTORY.makeZero(this.dim(), this.dim()));
    }

    public final PhysicalStore<Double> preallocate(final Structure2D template) {
        return Primitive64Store.FACTORY.makeZero(this.dim(), this.dim());
    }

    abstract long dim();

}
