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
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.decomposition.MatrixDecomposition.Solver;
import org.ojalgo.matrix.decomposition.MatrixDecompositionTests;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.matrix.task.iterative.ConjugateGradientSolver;
import org.ojalgo.matrix.task.iterative.GaussSeidelSolver;
import org.ojalgo.matrix.task.iterative.JacobiSolver;
import org.ojalgo.matrix.task.iterative.ParallelGaussSeidelSolver;
import org.ojalgo.matrix.task.iterative.QMRSolver;
import org.ojalgo.random.Uniform;

public class SolverTest extends MatrixTaskTests {

    private static final Random RANDOM = new Random();

    @Test
    public void testExampleWikipediA() {

        MatrixStore<Double> tmpA = RawStore.wrap(new double[][] { { 4, 1 }, { 1, 3 } });
        MatrixStore<Double> tmpB = R064Store.FACTORY.column(1, 2);

        MatrixStore<Double> expected = R064Store.FACTORY.column(1.0 / 11.0, 7.0 / 11.0);

        JacobiSolver tmpJacobiSolver = new JacobiSolver();
        TestUtils.assertEquals(expected, tmpJacobiSolver.solve(tmpA, tmpB).get());

        GaussSeidelSolver tmpGaussSeidelSolver = new GaussSeidelSolver();
        TestUtils.assertEquals(expected, tmpGaussSeidelSolver.solve(tmpA, tmpB).get());

        ParallelGaussSeidelSolver tmpParallelGaussSeidelSolver = new ParallelGaussSeidelSolver();
        TestUtils.assertEquals(expected, tmpParallelGaussSeidelSolver.solve(tmpA, tmpB).get());

        ConjugateGradientSolver tmpConjugateGradientSolver = new ConjugateGradientSolver();
        TestUtils.assertEquals(expected, tmpConjugateGradientSolver.solve(tmpA, tmpB).get());

        QMRSolver tmpQMRSolver = new QMRSolver();
        TestUtils.assertEquals(expected, tmpQMRSolver.solve(tmpA, tmpB).get());
    }

    @Test
    public void testFull2X2() {
        this.doCompare(AbstractSolver.FULL_2X2, 2);
    }

    @Test
    public void testFull3X3() {
        this.doCompare(AbstractSolver.FULL_3X3, 3);
    }

    @Test
    public void testFull4X4() {
        this.doCompare(AbstractSolver.FULL_4X4, 4);
    }

    @Test
    public void testFull5X5() {
        this.doCompare(AbstractSolver.FULL_5X5, 5);
    }

    @Test
    public void testLinAlg34PDF() {

        MatrixStore<Double> tmpA = RawStore.wrap(new double[][] { { 4, 2, 3 }, { 3, -5, 2 }, { -2, 3, 8 } });
        MatrixStore<Double> tmpB = R064Store.FACTORY.column(8, -14, 27);

        MatrixStore<Double> expected = R064Store.FACTORY.column(-1, 3, 2);

        JacobiSolver tmpJacobiSolver = new JacobiSolver();
        TestUtils.assertEquals(expected, tmpJacobiSolver.solve(tmpA, tmpB).get());

        GaussSeidelSolver tmpGaussSeidelSolver = new GaussSeidelSolver();
        TestUtils.assertEquals(expected, tmpGaussSeidelSolver.solve(tmpA, tmpB).get());

        ParallelGaussSeidelSolver tmpParallelGaussSeidelSolver = new ParallelGaussSeidelSolver();
        TestUtils.assertEquals(expected, tmpParallelGaussSeidelSolver.solve(tmpA, tmpB).get());

        QMRSolver tmpQMRSolver = new QMRSolver();
        TestUtils.assertEquals(expected, tmpQMRSolver.solve(tmpA, tmpB).get());
    }

    @Test
    public void testSingularFull() {

        List<SolverTask<Double>> tasks = List.of(AbstractSolver.FULL_1X1, AbstractSolver.FULL_2X2, AbstractSolver.FULL_3X3, AbstractSolver.FULL_4X4,
                AbstractSolver.FULL_5X5);

        for (int dim = 1; dim <= tasks.size(); dim++) {
            SolverTask<Double> task = tasks.get(dim - 1);
            MatrixStore<Double> body = MatrixTaskTests.makeRankDeficient(dim, dim);
            MatrixStore<Double> rhs = R064Store.FACTORY.makeFilled(dim, 1L, new Uniform());
            TestUtils.assertThrows(RecoverableCondition.class, () -> task.solve(body, rhs));
        }
    }

    @Test
    public void testSingularLeastSquares() {

        for (int nbCols = 1; nbCols <= 5; nbCols++) {
            MatrixStore<Double> body = MatrixTaskTests.makeRankDeficient(nbCols + 2, nbCols);
            MatrixStore<Double> rhs = R064Store.FACTORY.makeFilled(nbCols + 2, 1L, new Uniform());
            TestUtils.assertThrows(RecoverableCondition.class, () -> AbstractSolver.LEAST_SQUARES.solve(body, rhs));
        }
    }

    @Test
    public void testSingularSymmetric() {

        List<SolverTask<Double>> tasks = List.of(AbstractSolver.FULL_1X1, AbstractSolver.SYMMETRIC_2X2, AbstractSolver.SYMMETRIC_3X3,
                AbstractSolver.SYMMETRIC_4X4, AbstractSolver.SYMMETRIC_5X5);

        for (int dim = 1; dim <= tasks.size(); dim++) {
            SolverTask<Double> task = tasks.get(dim - 1);
            MatrixStore<Double> body = MatrixTaskTests.makeSingularSymmetric(dim);
            MatrixStore<Double> rhs = R064Store.FACTORY.makeFilled(dim, 1L, new Uniform());
            TestUtils.assertThrows(RecoverableCondition.class, () -> task.solve(body, rhs));
        }
    }

    @Test
    public void testSymmetric1X1() {
        this.doCompare(AbstractSolver.FULL_1X1, 1);
    }

    @Test
    public void testSymmetric2X2() {
        this.doCompare(AbstractSolver.SYMMETRIC_2X2, 2);
    }

    @Test
    public void testSymmetric3X3() {
        this.doCompare(AbstractSolver.SYMMETRIC_3X3, 3);
    }

    @Test
    public void testSymmetric4X4() {
        this.doCompare(AbstractSolver.SYMMETRIC_4X4, 4);
    }

    @Test
    public void testSymmetric5X5() {
        this.doCompare(AbstractSolver.SYMMETRIC_5X5, 5);
    }

    @Test
    public void testUnderdeterminedIterative() {

        int numEqs = 2;
        int numVars = 5;

        R064Store body = R064Store.FACTORY.make(numEqs, numVars);
        R064Store rhs = R064Store.FACTORY.make(numEqs, 1);

        R064Store expected = R064Store.FACTORY.make(numVars, 1);

        for (int i = 0; i < numEqs; i++) {

            double pivotE = RANDOM.nextDouble();
            double rhsE = RANDOM.nextDouble();

            body.set(i, i, pivotE);
            rhs.set(i, rhsE);
            expected.set(i, rhsE / pivotE);
        }

        JacobiSolver tmpJacobiSolver = new JacobiSolver();
        TestUtils.assertEquals(expected, tmpJacobiSolver.solve(body, rhs).get());

        GaussSeidelSolver tmpGaussSeidelSolver = new GaussSeidelSolver();
        TestUtils.assertEquals(expected, tmpGaussSeidelSolver.solve(body, rhs).get());

        ParallelGaussSeidelSolver tmpParallelGaussSeidelSolver = new ParallelGaussSeidelSolver();
        TestUtils.assertEquals(expected, tmpParallelGaussSeidelSolver.solve(body, rhs).get());

        ConjugateGradientSolver tmpConjugateGradientSolver = new ConjugateGradientSolver();
        TestUtils.assertEquals(expected, tmpConjugateGradientSolver.solve(body, rhs).get());
    }

    /**
     * The closed-form solvers used to scale by the norm of the RHS, so a zero RHS gave NaN.
     */
    @Test
    public void testZeroRHS() {

        List<SolverTask<Double>> full = List.of(AbstractSolver.FULL_1X1, AbstractSolver.FULL_2X2, AbstractSolver.FULL_3X3, AbstractSolver.FULL_4X4,
                AbstractSolver.FULL_5X5);
        List<SolverTask<Double>> symmetric = List.of(AbstractSolver.FULL_1X1, AbstractSolver.SYMMETRIC_2X2, AbstractSolver.SYMMETRIC_3X3,
                AbstractSolver.SYMMETRIC_4X4, AbstractSolver.SYMMETRIC_5X5);

        try {

            for (int dim = 1; dim <= 5; dim++) {

                MatrixStore<Double> body = R064Store.FACTORY.makeSPD(dim);
                MatrixStore<Double> zero = R064Store.FACTORY.make(dim, 1);

                TestUtils.assertEquals(zero, full.get(dim - 1).solve(body, zero));
                TestUtils.assertEquals(zero, symmetric.get(dim - 1).solve(body, zero));
            }

        } catch (RecoverableCondition exception) {
            TestUtils.fail(exception.getMessage());
        }
    }

    private void doCompare(final SolverTask<Double> fixed, final int dimension) {

        try {

            MatrixStore<Double> body = R064Store.FACTORY.makeSPD(dimension);
            MatrixStore<Double> rhs = R064Store.FACTORY.makeFilled(dimension, 1L, new Uniform());

            MatrixStore<Double> expSol = fixed.solve(body, rhs);

            List<Solver<Double>> all = MatrixDecompositionTests.getPrimitiveMatrixDecompositionSolver();
            for (Solver<Double> decomp : all) {
                MatrixStore<Double> actSol = decomp.solve(body, rhs);
                TestUtils.assertEquals(decomp.getClass().getName(), expSol, actSol);
            }

        } catch (RecoverableCondition exception) {
            TestUtils.fail(exception.getMessage());
        }
    }

}
