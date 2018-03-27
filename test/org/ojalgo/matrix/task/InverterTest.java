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
package org.ojalgo.matrix.task;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.decomposition.MatrixDecomposition;
import org.ojalgo.matrix.decomposition.MatrixDecompositionTests;
import org.ojalgo.matrix.store.MatrixStore;

public final class InverterTest extends AbstractMatrixDecompositionTaskTest {

    @Test
    public void testFull2X2() {
        this.doCompare(AbstractInverter.FULL_2X2, 2);
    }

    @Test
    public void testFull3X3() {
        this.doCompare(AbstractInverter.FULL_3X3, 3);
    }

    @Test
    public void testFull4X4() {
        this.doCompare(AbstractInverter.FULL_4X4, 4);
    }

    @Test
    public void testFull5X5() {
        this.doCompare(AbstractInverter.FULL_5X5, 5);
    }

    @Test
    public void testSymmetric1X1() {
        this.doCompare(AbstractInverter.FULL_1X1, 1);
    }

    @Test
    public void testSymmetric2X2() {
        this.doCompare(AbstractInverter.SYMMETRIC_2X2, 2);
    }

    @Test
    public void testSymmetric3X3() {
        this.doCompare(AbstractInverter.SYMMETRIC_3X3, 3);
    }

    @Test
    public void testSymmetric4X4() {
        this.doCompare(AbstractInverter.SYMMETRIC_4X4, 4);
    }

    @Test
    public void testSymmetric5X5() {
        this.doCompare(AbstractInverter.SYMMETRIC_5X5, 5);
    }

    private void doCompare(final InverterTask<Double> fixed, final int dimension) {

        try {

            final MatrixStore<Double> tmpMatrix = this.makeSPD(dimension);

            final MatrixStore<Double> tmpExpInv = fixed.invert(tmpMatrix);

            final List<MatrixDecomposition<Double>> tmpList = MatrixDecompositionTests.getAllPrimitive();
            for (final MatrixDecomposition<Double> tmpDecomp : tmpList) {
                if (tmpDecomp instanceof InverterTask) {
                    @SuppressWarnings("unchecked")
                    final InverterTask<Double> tmpTask = (InverterTask<Double>) tmpDecomp;
                    final MatrixStore<Double> tmpActInv = tmpTask.invert(tmpMatrix);
                    TestUtils.assertEquals(tmpDecomp.getClass().getName(), tmpExpInv, tmpActInv);
                }
            }

        } catch (final RecoverableCondition exception) {
            TestUtils.fail(exception.getMessage());
        }
    }

    private MatrixStore<Double> makeSPD(final int dim) {
        return MatrixUtils.makeSPD(dim);
    }

}
