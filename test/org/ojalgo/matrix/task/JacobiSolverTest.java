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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.task.iterative.ConjugateGradientSolver;
import org.ojalgo.matrix.task.iterative.GaussSeidelSolver;
import org.ojalgo.matrix.task.iterative.JacobiSolver;

/**
 * MatrixDecompositionPackageTests
 *
 * @author apete
 */
public class JacobiSolverTest extends AbstractMatrixDecompositionTaskTest {

    @Test
    public void testExampleWikipediA() {

        final MatrixStore<Double> tmpA = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 4, 1 }, { 1, 3 } });
        final MatrixStore<Double> tmpB = PrimitiveDenseStore.FACTORY.columns(new double[] { 1, 2 });

        final MatrixStore<Double> tmpExpected = PrimitiveDenseStore.FACTORY.columns(new double[] { 1.0 / 11.0, 7.0 / 11.0 });

        final JacobiSolver tmpJacobiSolver = new JacobiSolver();
        TestUtils.assertEquals(tmpExpected, tmpJacobiSolver.solve(tmpA, tmpB).get());

        final GaussSeidelSolver tmpGaussSeidelSolver = new GaussSeidelSolver();
        TestUtils.assertEquals(tmpExpected, tmpGaussSeidelSolver.solve(tmpA, tmpB).get());

        final ConjugateGradientSolver tmpConjugateGradientSolver = new ConjugateGradientSolver();
        TestUtils.assertEquals(tmpExpected, tmpConjugateGradientSolver.solve(tmpA, tmpB).get());
    }

    @Test
    public void testLinAlg34PDF() {

        final MatrixStore<Double> tmpA = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 4, 2, 3 }, { 3, -5, 2 }, { -2, 3, 8 } });
        final MatrixStore<Double> tmpB = PrimitiveDenseStore.FACTORY.columns(new double[] { 8, -14, 27 });

        final MatrixStore<Double> tmpExpected = PrimitiveDenseStore.FACTORY.columns(new double[] { -1, 3, 2 });

        final JacobiSolver tmpJacobiSolver = new JacobiSolver();
        TestUtils.assertEquals(tmpExpected, tmpJacobiSolver.solve(tmpA, tmpB).get());

        final GaussSeidelSolver tmpGaussSeidelSolver = new GaussSeidelSolver();
        TestUtils.assertEquals(tmpExpected, tmpGaussSeidelSolver.solve(tmpA, tmpB).get());

    }
}
