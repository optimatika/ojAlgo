/*
 * Copyright 1997-2025 Optimatika
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
import org.ojalgo.random.Uniform;

public class SolverTest extends MatrixTaskTests {

    private static final Random RANDOM = new Random();

    @Test
    public void testExampleWikipediA() {

        MatrixStore<Double> tmpA = RawStore.wrap(new double[][] { { 4, 1 }, { 1, 3 } });
        MatrixStore<Double> tmpB = R064Store.FACTORY.column(1, 2);

        MatrixStore<Double> tmpExpected = R064Store.FACTORY.column(1.0 / 11.0, 7.0 / 11.0);

        JacobiSolver tmpJacobiSolver = new JacobiSolver();
        TestUtils.assertEquals(tmpExpected, tmpJacobiSolver.solve(tmpA, tmpB).get());

        GaussSeidelSolver tmpGaussSeidelSolver = new GaussSeidelSolver();
        TestUtils.assertEquals(tmpExpected, tmpGaussSeidelSolver.solve(tmpA, tmpB).get());

        ParallelGaussSeidelSolver tmpParallelGaussSeidelSolver = new ParallelGaussSeidelSolver();
        TestUtils.assertEquals(tmpExpected, tmpParallelGaussSeidelSolver.solve(tmpA, tmpB).get());

        ConjugateGradientSolver tmpConjugateGradientSolver = new ConjugateGradientSolver();
        TestUtils.assertEquals(tmpExpected, tmpConjugateGradientSolver.solve(tmpA, tmpB).get());
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

        MatrixStore<Double> tmpExpected = R064Store.FACTORY.column(-1, 3, 2);

        JacobiSolver tmpJacobiSolver = new JacobiSolver();
        TestUtils.assertEquals(tmpExpected, tmpJacobiSolver.solve(tmpA, tmpB).get());

        GaussSeidelSolver tmpGaussSeidelSolver = new GaussSeidelSolver();
        TestUtils.assertEquals(tmpExpected, tmpGaussSeidelSolver.solve(tmpA, tmpB).get());

        ParallelGaussSeidelSolver tmpParallelGaussSeidelSolver = new ParallelGaussSeidelSolver();
        TestUtils.assertEquals(tmpExpected, tmpParallelGaussSeidelSolver.solve(tmpA, tmpB).get());
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
