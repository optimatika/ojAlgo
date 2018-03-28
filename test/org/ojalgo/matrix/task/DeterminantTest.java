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
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.random.Uniform;

public final class DeterminantTest extends AbstractMatrixDecompositionTaskTest {

    @Test
    public void testFull2X2() {
        this.doGeneral(AbstractDeterminator.FULL_2X2, 2);
    }

    @Test
    public void testFull3X3() {
        this.doGeneral(AbstractDeterminator.FULL_3X3, 3);
    }

    @Test
    public void testFull4X4() {
        this.doGeneral(AbstractDeterminator.FULL_4X4, 4);
    }

    @Test
    public void testFull5X5() {
        this.doGeneral(AbstractDeterminator.FULL_5X5, 5);
    }

    @Test
    public void testSymmetric1X1() {
        this.doSymmetric(AbstractDeterminator.FULL_1X1, 1);
    }

    @Test
    public void testSymmetric2X2() {
        this.doSymmetric(AbstractDeterminator.SYMMETRIC_2X2, 2);
    }

    @Test
    public void testSymmetric3X3() {
        this.doSymmetric(AbstractDeterminator.SYMMETRIC_3X3, 3);
    }

    @Test
    public void testSymmetric4X4() {
        this.doSymmetric(AbstractDeterminator.SYMMETRIC_4X4, 4);
    }

    @Test
    public void testSymmetric5X5() {
        this.doSymmetric(AbstractDeterminator.SYMMETRIC_5X5, 5);
    }

    void doGeneral(final DeterminantTask<Double> fixed, final int dimension) {

        final MatrixStore<Double> tmpMatrix = this.makeGeneral(dimension);

        final Double tmpExpDet = fixed.calculateDeterminant(tmpMatrix);

        final List<DeterminantTask<Double>> tmpList = MatrixTaskTests.getPrimitiveFull();
        for (final DeterminantTask<Double> tmpDeterminantTask : tmpList) {
            final Double tmpActDet = tmpDeterminantTask.calculateDeterminant(tmpMatrix);
            TestUtils.assertEquals(tmpDeterminantTask.getClass().getName(), tmpExpDet, tmpActDet);
        }
    }

    void doSymmetric(final DeterminantTask<Double> fixed, final int dimension) {

        final MatrixStore<Double> tmpMatrix = this.makeSymmetric(dimension);

        final Double tmpExpDet = fixed.calculateDeterminant(tmpMatrix);

        final List<DeterminantTask<Double>> tmpList = MatrixTaskTests.getPrimitiveSymmetric();
        for (final DeterminantTask<Double> tmpDeterminantTask : tmpList) {
            final Double tmpActDet = tmpDeterminantTask.calculateDeterminant(tmpMatrix);
            TestUtils.assertEquals(tmpDeterminantTask.getClass().getName(), tmpExpDet, tmpActDet);
        }
    }

    MatrixStore<Double> makeGeneral(final int dim) {
        return PrimitiveDenseStore.FACTORY.makeFilled(dim, dim, new Uniform());
    }

    MatrixStore<Double> makeSymmetric(final int dim) {
        return MatrixUtils.makeSPD(dim);
    }

}
