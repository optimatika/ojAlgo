/*
 * Copyright 1997-2019 Optimatika
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

import org.ojalgo.RecoverableCondition;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;

abstract class AbstractSolver implements SolverTask<Double> {

    static final SolverTask<Double> FULL_1X1 = new AbstractSolver() {

        public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws RecoverableCondition {
            AbstractSolver.full1X1(body, rhs, preallocated);
            return preallocated;
        }

    };

    static final SolverTask<Double> FULL_2X2 = new AbstractSolver() {

        public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws RecoverableCondition {
            AbstractSolver.full2X2(body, rhs, preallocated);
            return preallocated;
        }

    };

    static final SolverTask<Double> FULL_3X3 = new AbstractSolver() {

        public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws RecoverableCondition {
            AbstractSolver.full3X3(body, rhs, preallocated);
            return preallocated;
        }

    };

    static final SolverTask<Double> FULL_4X4 = new AbstractSolver() {

        public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws RecoverableCondition {
            AbstractSolver.full4X4(body, rhs, preallocated);
            return preallocated;
        }

    };

    static final SolverTask<Double> FULL_5X5 = new AbstractSolver() {

        public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws RecoverableCondition {
            AbstractSolver.full5X5(body, rhs, preallocated);
            return preallocated;
        }

    };

    static final SolverTask<Double> LEAST_SQUARES = new AbstractSolver() {

        public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws RecoverableCondition {
            AbstractSolver.leastSquares(body, rhs, preallocated);
            return preallocated;
        }

    };

    static final SolverTask<Double> SYMMETRIC_2X2 = new AbstractSolver() {

        public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws RecoverableCondition {
            AbstractSolver.symmetric2X2(body, rhs, preallocated);
            return preallocated;
        }

    };

    static final SolverTask<Double> SYMMETRIC_3X3 = new AbstractSolver() {

        public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws RecoverableCondition {
            AbstractSolver.symmetric3X3(body, rhs, preallocated);
            return preallocated;
        }

    };

    static final SolverTask<Double> SYMMETRIC_4X4 = new AbstractSolver() {

        public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws RecoverableCondition {
            AbstractSolver.symmetric4X4(body, rhs, preallocated);
            return preallocated;
        }

    };

    static final SolverTask<Double> SYMMETRIC_5X5 = new AbstractSolver() {

        public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws RecoverableCondition {
            AbstractSolver.symmetric5X5(body, rhs, preallocated);
            return preallocated;
        }

    };

    static void full1X1(final Access2D<?> body, final Access1D<?> rhs, final PhysicalStore<?> solution) {
        solution.set(0L, rhs.doubleValue(0L) / body.doubleValue(0L));
    }

    static void full2X2(final Access2D<?> body, final Access1D<?> rhs, final PhysicalStore<?> solution) {

        double tmp00 = body.doubleValue(0L);
        double tmp10 = body.doubleValue(1L);

        double tmp01 = body.doubleValue(2L);
        double tmp11 = body.doubleValue(3L);

        double tmp0 = rhs.doubleValue(0L);
        double tmp1 = rhs.doubleValue(1L);

        final double tmpScale = MissingMath.norm(tmp0, tmp1);

        tmp00 /= tmpScale;
        tmp10 /= tmpScale;

        tmp01 /= tmpScale;
        tmp11 /= tmpScale;

        tmp0 /= tmpScale;
        tmp1 /= tmpScale;

        final double tmpDet = AbstractDeterminator.calculate(tmp00, tmp10, tmp01, tmp11);

        solution.set(0L, AbstractDeterminator.calculate(tmp0, tmp1, tmp01, tmp11) / tmpDet);
        solution.set(1L, AbstractDeterminator.calculate(tmp00, tmp10, tmp0, tmp1) / tmpDet);
    }

    static void full3X3(final Access2D<?> body, final Access1D<?> rhs, final PhysicalStore<?> solution) {

        double tmp00 = body.doubleValue(0L);
        double tmp10 = body.doubleValue(1L);
        double tmp20 = body.doubleValue(2L);

        double tmp01 = body.doubleValue(3L);
        double tmp11 = body.doubleValue(4L);
        double tmp21 = body.doubleValue(5L);

        double tmp02 = body.doubleValue(6L);
        double tmp12 = body.doubleValue(7L);
        double tmp22 = body.doubleValue(8L);

        double tmp0 = rhs.doubleValue(0L);
        double tmp1 = rhs.doubleValue(1L);
        double tmp2 = rhs.doubleValue(2L);

        final double tmpScale = MissingMath.norm(tmp0, tmp1, tmp2);

        tmp00 /= tmpScale;
        tmp10 /= tmpScale;
        tmp20 /= tmpScale;

        tmp01 /= tmpScale;
        tmp11 /= tmpScale;
        tmp21 /= tmpScale;

        tmp02 /= tmpScale;
        tmp12 /= tmpScale;
        tmp22 /= tmpScale;

        tmp0 /= tmpScale;
        tmp1 /= tmpScale;
        tmp2 /= tmpScale;

        final double tmpMin00 = AbstractDeterminator.calculate(tmp11, tmp21, tmp12, tmp22);
        final double tmpMin10 = AbstractDeterminator.calculate(tmp01, tmp21, tmp02, tmp22);
        final double tmpMin20 = AbstractDeterminator.calculate(tmp01, tmp11, tmp02, tmp12);

        final double tmpMin01 = AbstractDeterminator.calculate(tmp10, tmp20, tmp12, tmp22);
        final double tmpMin11 = AbstractDeterminator.calculate(tmp00, tmp20, tmp02, tmp22);
        final double tmpMin21 = AbstractDeterminator.calculate(tmp00, tmp10, tmp02, tmp12);

        final double tmpMin02 = AbstractDeterminator.calculate(tmp10, tmp20, tmp11, tmp21);
        final double tmpMin12 = AbstractDeterminator.calculate(tmp00, tmp20, tmp01, tmp21);
        final double tmpMin22 = AbstractDeterminator.calculate(tmp00, tmp10, tmp01, tmp11);

        final double tmpDet = ((tmp00 * tmpMin00) - (tmp10 * tmpMin10)) + (tmp20 * tmpMin20);

        solution.set(0L, (((tmp0 * tmpMin00) - (tmp1 * tmpMin10)) + (tmp2 * tmpMin20)) / tmpDet);
        solution.set(1L, -(((tmp0 * tmpMin01) - (tmp1 * tmpMin11)) + (tmp2 * tmpMin21)) / tmpDet);
        solution.set(2L, (((tmp0 * tmpMin02) - (tmp1 * tmpMin12)) + (tmp2 * tmpMin22)) / tmpDet);
    }

    static void full4X4(final Access2D<?> body, final Access1D<?> rhs, final PhysicalStore<?> solution) {

        double tmp00 = body.doubleValue(0L);
        double tmp10 = body.doubleValue(1L);
        double tmp20 = body.doubleValue(2L);
        double tmp30 = body.doubleValue(3L);

        double tmp01 = body.doubleValue(4L);
        double tmp11 = body.doubleValue(5L);
        double tmp21 = body.doubleValue(6L);
        double tmp31 = body.doubleValue(7L);

        double tmp02 = body.doubleValue(8L);
        double tmp12 = body.doubleValue(9L);
        double tmp22 = body.doubleValue(10L);
        double tmp32 = body.doubleValue(11L);

        double tmp03 = body.doubleValue(12L);
        double tmp13 = body.doubleValue(13L);
        double tmp23 = body.doubleValue(14L);
        double tmp33 = body.doubleValue(15L);

        double tmp0 = rhs.doubleValue(0L);
        double tmp1 = rhs.doubleValue(1L);
        double tmp2 = rhs.doubleValue(2L);
        double tmp3 = rhs.doubleValue(3L);

        final double tmpScale = MissingMath.norm(tmp0, tmp1, tmp2, tmp3);

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

        tmp0 /= tmpScale;
        tmp1 /= tmpScale;
        tmp2 /= tmpScale;
        tmp3 /= tmpScale;

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

        final double tmpDet = (((tmp00 * tmpMin00) - (tmp10 * tmpMin10)) + (tmp20 * tmpMin20)) - (tmp30 * tmpMin30);

        solution.set(0L, ((((tmp0 * tmpMin00) - (tmp1 * tmpMin10)) + (tmp2 * tmpMin20)) - (tmp3 * tmpMin30)) / tmpDet);
        solution.set(1L, -((((tmp0 * tmpMin01) - (tmp1 * tmpMin11)) + (tmp2 * tmpMin21)) - (tmp3 * tmpMin31)) / tmpDet);
        solution.set(2L, ((((tmp0 * tmpMin02) - (tmp1 * tmpMin12)) + (tmp2 * tmpMin22)) - (tmp3 * tmpMin32)) / tmpDet);
        solution.set(3L, -((((tmp0 * tmpMin03) - (tmp1 * tmpMin13)) + (tmp2 * tmpMin23)) - (tmp3 * tmpMin33)) / tmpDet);
    }

    static void full5X5(final Access2D<?> body, final Access1D<?> rhs, final PhysicalStore<?> solution) {

        double tmp00 = body.doubleValue(0L);
        double tmp10 = body.doubleValue(1L);
        double tmp20 = body.doubleValue(2L);
        double tmp30 = body.doubleValue(3L);
        double tmp40 = body.doubleValue(4L);

        double tmp01 = body.doubleValue(5L);
        double tmp11 = body.doubleValue(6L);
        double tmp21 = body.doubleValue(7L);
        double tmp31 = body.doubleValue(8L);
        double tmp41 = body.doubleValue(9L);

        double tmp02 = body.doubleValue(10L);
        double tmp12 = body.doubleValue(11L);
        double tmp22 = body.doubleValue(12L);
        double tmp32 = body.doubleValue(13L);
        double tmp42 = body.doubleValue(14L);

        double tmp03 = body.doubleValue(15L);
        double tmp13 = body.doubleValue(16L);
        double tmp23 = body.doubleValue(17L);
        double tmp33 = body.doubleValue(18L);
        double tmp43 = body.doubleValue(19L);

        double tmp04 = body.doubleValue(20L);
        double tmp14 = body.doubleValue(21L);
        double tmp24 = body.doubleValue(22L);
        double tmp34 = body.doubleValue(23L);
        double tmp44 = body.doubleValue(24L);

        double tmp0 = rhs.doubleValue(0L);
        double tmp1 = rhs.doubleValue(1L);
        double tmp2 = rhs.doubleValue(2L);
        double tmp3 = rhs.doubleValue(3L);
        double tmp4 = rhs.doubleValue(4L);

        final double tmpScale = MissingMath.norm(tmp0, tmp1, tmp2, tmp3, tmp4);

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

        tmp0 /= tmpScale;
        tmp1 /= tmpScale;
        tmp2 /= tmpScale;
        tmp3 /= tmpScale;
        tmp4 /= tmpScale;

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

        final double tmpDet = ((((tmp00 * tmpMin00) - (tmp10 * tmpMin10)) + (tmp20 * tmpMin20)) - (tmp30 * tmpMin30)) + (tmp40 * tmpMin40);

        solution.set(0L, (((((tmp0 * tmpMin00) - (tmp1 * tmpMin10)) + (tmp2 * tmpMin20)) - (tmp3 * tmpMin30)) + (tmp4 * tmpMin40)) / tmpDet);
        solution.set(1L, -(((((tmp0 * tmpMin01) - (tmp1 * tmpMin11)) + (tmp2 * tmpMin21)) - (tmp3 * tmpMin31)) + (tmp4 * tmpMin41)) / tmpDet);
        solution.set(2L, (((((tmp0 * tmpMin02) - (tmp1 * tmpMin12)) + (tmp2 * tmpMin22)) - (tmp3 * tmpMin32)) + (tmp4 * tmpMin42)) / tmpDet);
        solution.set(3L, -(((((tmp0 * tmpMin03) - (tmp1 * tmpMin13)) + (tmp2 * tmpMin23)) - (tmp3 * tmpMin33)) + (tmp4 * tmpMin43)) / tmpDet);
        solution.set(4L, (((((tmp0 * tmpMin04) - (tmp1 * tmpMin14)) + (tmp2 * tmpMin24)) - (tmp3 * tmpMin34)) + (tmp4 * tmpMin44)) / tmpDet);
    }

    static void leastSquares(final Access2D<?> body, final Access1D<?> rhs, final PhysicalStore<?> solution) {

        final Primitive64Store tmpTranspBody = Primitive64Store.FACTORY.transpose(body);

        final int tmpCountRows = (int) tmpTranspBody.countRows();

        final Primitive64Store tmpBody = Primitive64Store.FACTORY.makeZero(tmpCountRows, tmpCountRows);
        tmpTranspBody.multiply(tmpTranspBody.transpose(), tmpBody);

        final Primitive64Store tmpRHS = Primitive64Store.FACTORY.makeZero(tmpCountRows, solution.countColumns());
        tmpTranspBody.multiply((Access1D<Double>) rhs, tmpRHS);

        switch (tmpCountRows) {
        case 1:
            AbstractSolver.full1X1(tmpBody, tmpRHS, solution);
            break;
        case 2:
            AbstractSolver.symmetric2X2(tmpBody, tmpRHS, solution);
            break;
        case 3:
            AbstractSolver.symmetric3X3(tmpBody, tmpRHS, solution);
            break;
        case 4:
            AbstractSolver.symmetric4X4(tmpBody, tmpRHS, solution);
            break;
        case 5:
            AbstractSolver.symmetric5X5(tmpBody, tmpRHS, solution);
            break;
        default:
            throw new IllegalArgumentException();
        }

    }

    static void symmetric2X2(final Access2D<?> body, final Access1D<?> rhs, final PhysicalStore<?> solution) {

        double tmp00 = body.doubleValue(0L);
        double tmp10 = body.doubleValue(1L);

        double tmp11 = body.doubleValue(3L);

        double tmp0 = rhs.doubleValue(0L);
        double tmp1 = rhs.doubleValue(1L);

        final double tmpScale = MissingMath.norm(tmp0, tmp1);

        tmp00 /= tmpScale;
        tmp10 /= tmpScale;

        tmp11 /= tmpScale;

        tmp0 /= tmpScale;
        tmp1 /= tmpScale;

        final double tmpDet = AbstractDeterminator.calculate(tmp00, tmp10, tmp10, tmp11);

        solution.set(0L, AbstractDeterminator.calculate(tmp0, tmp1, tmp10, tmp11) / tmpDet);
        solution.set(1L, AbstractDeterminator.calculate(tmp00, tmp10, tmp0, tmp1) / tmpDet);
    }

    static void symmetric3X3(final Access2D<?> body, final Access1D<?> rhs, final PhysicalStore<?> solution) {

        double tmp00 = body.doubleValue(0L);
        double tmp10 = body.doubleValue(1L);
        double tmp20 = body.doubleValue(2L);

        double tmp11 = body.doubleValue(4L);
        double tmp21 = body.doubleValue(5L);

        double tmp22 = body.doubleValue(8L);

        double tmp0 = rhs.doubleValue(0L);
        double tmp1 = rhs.doubleValue(1L);
        double tmp2 = rhs.doubleValue(2L);

        final double tmpScale = MissingMath.norm(tmp0, tmp1, tmp2);

        tmp00 /= tmpScale;
        tmp10 /= tmpScale;
        tmp20 /= tmpScale;

        tmp11 /= tmpScale;
        tmp21 /= tmpScale;

        tmp22 /= tmpScale;

        tmp0 /= tmpScale;
        tmp1 /= tmpScale;
        tmp2 /= tmpScale;

        final double tmpMin00 = AbstractDeterminator.calculate(tmp11, tmp21, tmp21, tmp22);
        final double tmpMin10 = AbstractDeterminator.calculate(tmp10, tmp21, tmp20, tmp22);
        final double tmpMin20 = AbstractDeterminator.calculate(tmp10, tmp11, tmp20, tmp21);

        final double tmpMin11 = AbstractDeterminator.calculate(tmp00, tmp20, tmp20, tmp22);
        final double tmpMin21 = AbstractDeterminator.calculate(tmp00, tmp10, tmp20, tmp21);

        final double tmpMin22 = AbstractDeterminator.calculate(tmp00, tmp10, tmp10, tmp11);

        final double tmpDet = ((tmp00 * tmpMin00) - (tmp10 * tmpMin10)) + (tmp20 * tmpMin20);

        solution.set(0L, (((tmp0 * tmpMin00) - (tmp1 * tmpMin10)) + (tmp2 * tmpMin20)) / tmpDet);
        solution.set(1L, -(((tmp0 * tmpMin10) - (tmp1 * tmpMin11)) + (tmp2 * tmpMin21)) / tmpDet);
        solution.set(2L, (((tmp0 * tmpMin20) - (tmp1 * tmpMin21)) + (tmp2 * tmpMin22)) / tmpDet);
    }

    static void symmetric4X4(final Access2D<?> body, final Access1D<?> rhs, final PhysicalStore<?> solution) {

        double tmp00 = body.doubleValue(0L);
        double tmp10 = body.doubleValue(1L);
        double tmp20 = body.doubleValue(2L);
        double tmp30 = body.doubleValue(3L);

        double tmp11 = body.doubleValue(5L);
        double tmp21 = body.doubleValue(6L);
        double tmp31 = body.doubleValue(7L);

        double tmp22 = body.doubleValue(10L);
        double tmp32 = body.doubleValue(11L);

        double tmp33 = body.doubleValue(15L);

        double tmp0 = rhs.doubleValue(0L);
        double tmp1 = rhs.doubleValue(1L);
        double tmp2 = rhs.doubleValue(2L);
        double tmp3 = rhs.doubleValue(3L);

        final double tmpScale = MissingMath.norm(tmp0, tmp1, tmp2, tmp3);

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

        tmp0 /= tmpScale;
        tmp1 /= tmpScale;
        tmp2 /= tmpScale;
        tmp3 /= tmpScale;

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

        final double tmpDet = (((tmp00 * tmpMin00) - (tmp10 * tmpMin10)) + (tmp20 * tmpMin20)) - (tmp30 * tmpMin30);

        solution.set(0L, ((((tmp0 * tmpMin00) - (tmp1 * tmpMin10)) + (tmp2 * tmpMin20)) - (tmp3 * tmpMin30)) / tmpDet);
        solution.set(1L, -((((tmp0 * tmpMin10) - (tmp1 * tmpMin11)) + (tmp2 * tmpMin21)) - (tmp3 * tmpMin31)) / tmpDet);
        solution.set(2L, ((((tmp0 * tmpMin20) - (tmp1 * tmpMin21)) + (tmp2 * tmpMin22)) - (tmp3 * tmpMin32)) / tmpDet);
        solution.set(3L, -((((tmp0 * tmpMin30) - (tmp1 * tmpMin31)) + (tmp2 * tmpMin32)) - (tmp3 * tmpMin33)) / tmpDet);
    }

    static void symmetric5X5(final Access2D<?> body, final Access1D<?> rhs, final PhysicalStore<?> solution) {

        double tmp00 = body.doubleValue(0L);
        double tmp10 = body.doubleValue(1L);
        double tmp20 = body.doubleValue(2L);
        double tmp30 = body.doubleValue(3L);
        double tmp40 = body.doubleValue(4L);

        double tmp11 = body.doubleValue(6L);
        double tmp21 = body.doubleValue(7L);
        double tmp31 = body.doubleValue(8L);
        double tmp41 = body.doubleValue(9L);

        double tmp22 = body.doubleValue(12L);
        double tmp32 = body.doubleValue(13L);
        double tmp42 = body.doubleValue(14L);

        double tmp33 = body.doubleValue(18L);
        double tmp43 = body.doubleValue(19L);

        double tmp44 = body.doubleValue(24L);

        double tmp0 = rhs.doubleValue(0L);
        double tmp1 = rhs.doubleValue(1L);
        double tmp2 = rhs.doubleValue(2L);
        double tmp3 = rhs.doubleValue(3L);
        double tmp4 = rhs.doubleValue(4L);

        final double tmpScale = MissingMath.norm(tmp0, tmp1, tmp2, tmp3, tmp4);

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

        tmp0 /= tmpScale;
        tmp1 /= tmpScale;
        tmp2 /= tmpScale;
        tmp3 /= tmpScale;
        tmp4 /= tmpScale;

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

        final double tmpDet = ((((tmp00 * tmpMin00) - (tmp10 * tmpMin10)) + (tmp20 * tmpMin20)) - (tmp30 * tmpMin30)) + (tmp40 * tmpMin40);

        solution.set(0L, (((((tmp0 * tmpMin00) - (tmp1 * tmpMin10)) + (tmp2 * tmpMin20)) - (tmp3 * tmpMin30)) + (tmp4 * tmpMin40)) / tmpDet);
        solution.set(1L, -(((((tmp0 * tmpMin10) - (tmp1 * tmpMin11)) + (tmp2 * tmpMin21)) - (tmp3 * tmpMin31)) + (tmp4 * tmpMin41)) / tmpDet);
        solution.set(2L, (((((tmp0 * tmpMin20) - (tmp1 * tmpMin21)) + (tmp2 * tmpMin22)) - (tmp3 * tmpMin32)) + (tmp4 * tmpMin42)) / tmpDet);
        solution.set(3L, -(((((tmp0 * tmpMin30) - (tmp1 * tmpMin31)) + (tmp2 * tmpMin32)) - (tmp3 * tmpMin33)) + (tmp4 * tmpMin43)) / tmpDet);
        solution.set(4L, (((((tmp0 * tmpMin40) - (tmp1 * tmpMin41)) + (tmp2 * tmpMin42)) - (tmp3 * tmpMin43)) + (tmp4 * tmpMin44)) / tmpDet);
    }

    AbstractSolver() {
        super();
    }

    public final PhysicalStore<Double> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        return Primitive64Store.FACTORY.makeZero(templateBody.countColumns(), 1L);
    }

}
