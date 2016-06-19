/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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

import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.random.Uniform;

public final class DeterminantTest extends AbstractMatrixDecompositionTaskTest {

    public DeterminantTest() {
        super();
    }

    public DeterminantTest(final String arg0) {
        super(arg0);
    }

    public void testFull2X2() {
        this.doFull(AbstractDeterminator.FULL_2X2, 2);
    }

    public void testFull3X3() {
        this.doFull(AbstractDeterminator.FULL_3X3, 3);
    }

    public void testFull4X4() {
        this.doFull(AbstractDeterminator.FULL_4X4, 4);
    }

    public void testFull5X5() {
        this.doFull(AbstractDeterminator.FULL_5X5, 5);
    }

    public void testSymmetric1X1() {
        this.doSymmetric(AbstractDeterminator.FULL_1X1, 1);
    }

    public void testSymmetric2X2() {
        this.doSymmetric(AbstractDeterminator.SYMMETRIC_2X2, 2);
    }

    public void testSymmetric3X3() {
        this.doSymmetric(AbstractDeterminator.SYMMETRIC_3X3, 3);
    }

    public void testSymmetric4X4() {
        this.doSymmetric(AbstractDeterminator.SYMMETRIC_4X4, 4);
    }

    public void testSymmetric5X5() {
        this.doSymmetric(AbstractDeterminator.SYMMETRIC_5X5, 5);
    }

    void doFull(final DeterminantTask<Double> fixed, final int dimension) {

        final MatrixStore<Double> tmpMatrix = this.makeFull(dimension);

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

    MatrixStore<Double> makeFull(final int dim) {
        return PrimitiveDenseStore.FACTORY.makeFilled(dim, dim, new Uniform());
    }

    MatrixStore<Double> makeSymmetric(final int dim) {

        final MatrixStore<Double> tmpVal = this.makeFull(dim);

        return tmpVal.transpose().multiply(tmpVal);
    }

}
