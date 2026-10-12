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
package org.ojalgo.matrix.task;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.decomposition.MatrixDecomposition.Solver;
import org.ojalgo.matrix.decomposition.MatrixDecompositionTests;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.random.Uniform;

public class InverterTest extends MatrixTaskTests {

    /**
     * The same preallocated store reused, and filled with junk before each call. Covers the fixed size
     * implementations as well as the decompositions used for larger matrices (RawLU used to assume a
     * zero-filled store when pivoting).
     */
    @Test
    public void testDirtyPreallocated() throws RecoverableCondition {

        for (int dim = 1; dim <= 8; dim++) {

            MatrixStore<Double> expected = R064Store.FACTORY.makeIdentity(dim);

            for (boolean spd : new boolean[] { false, true }) {

                InverterTask<Double> task = InverterTask.R064.make(dim, spd);
                String name = task.getClass().getName() + " " + dim;

                PhysicalStore<Double> preallocated = task.preallocate(expected);

                for (int r = 0; r < 3; r++) {

                    MatrixStore<Double> matrix = MatrixDecompositionTests.makePivotingSPD(dim);

                    preallocated.fillAll(Uniform.standard());
                    MatrixStore<Double> inverse = task.invert(matrix, preallocated);
                    TestUtils.assertEquals(name, expected, matrix.multiply(inverse));
                }
            }
        }
    }

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

    /**
     * Whichever implementation the factory picks (fixed size, LU, or raw or dense Cholesky), the inverse is
     * read from the returned store – with Cholesky that is a hermitian view of the preallocated store's lower
     * triangle. The preallocated store is filled with junk first.
     */
    @Test
    public void testReturnedInverse() throws RecoverableCondition {

        for (int dim : new int[] { 1, 2, 3, 4, 5, 6, 32, 33, 64 }) {

            MatrixStore<Double> expected = R064Store.FACTORY.makeIdentity(dim);
            MatrixStore<Double> matrix = R064Store.FACTORY.makeSPD(dim);

            for (boolean spd : new boolean[] { false, true }) {

                InverterTask<Double> task = InverterTask.R064.make(dim, spd);
                String name = task.getClass().getName() + " " + dim;

                PhysicalStore<Double> preallocated = task.preallocate(expected);
                preallocated.fillAll(Uniform.standard());

                TestUtils.assertEquals(name, expected, matrix.multiply(task.invert(matrix, preallocated)));
            }
        }
    }

    @Test
    public void testSingularFull() {

        List<InverterTask<Double>> tasks = List.of(AbstractInverter.FULL_1X1, AbstractInverter.FULL_2X2, AbstractInverter.FULL_3X3, AbstractInverter.FULL_4X4,
                AbstractInverter.FULL_5X5);

        for (int dim = 1; dim <= tasks.size(); dim++) {
            InverterTask<Double> task = tasks.get(dim - 1);
            MatrixStore<Double> singular = MatrixTaskTests.makeRankDeficient(dim, dim);
            TestUtils.assertThrows(RecoverableCondition.class, () -> task.invert(singular));
        }
    }

    @Test
    public void testSingularSymmetric() {

        List<InverterTask<Double>> tasks = List.of(AbstractInverter.FULL_1X1, AbstractInverter.SYMMETRIC_2X2, AbstractInverter.SYMMETRIC_3X3,
                AbstractInverter.SYMMETRIC_4X4, AbstractInverter.SYMMETRIC_5X5);

        for (int dim = 1; dim <= tasks.size(); dim++) {
            InverterTask<Double> task = tasks.get(dim - 1);
            MatrixStore<Double> singular = MatrixTaskTests.makeSingularSymmetric(dim);
            TestUtils.assertThrows(RecoverableCondition.class, () -> task.invert(singular));
        }
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

            MatrixStore<Double> matrix = R064Store.FACTORY.makeSPD(dimension);

            MatrixStore<Double> expInv = fixed.invert(matrix);

            List<Solver<Double>> all = MatrixDecompositionTests.getPrimitiveMatrixDecompositionSolver();
            for (Solver<Double> decomp : all) {
                MatrixStore<Double> actInv = decomp.invert(matrix);
                TestUtils.assertEquals(decomp.getClass().getName(), expInv, actInv);
            }

        } catch (RecoverableCondition exception) {
            TestUtils.fail(exception.getMessage());
        }
    }

}
