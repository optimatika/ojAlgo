/*
 * Copyright 1997-2024 Optimatika
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
package org.ojalgo.matrix.decomposition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.matrix.SimpleCholeskyCase;
import org.ojalgo.matrix.SimpleEquationCase;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class TestSolveAndInvert extends MatrixDecompositionTests {

    private static final NumberContext ACCURACY = NumberContext.of(7, 6);

    @Override
    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    @Test
    public void testInverseOfRandomSPD() {

        int dim = 99;

        PhysicalStore<Double> random = Primitive64Store.FACTORY.makeSPD(dim);
        PhysicalStore<Double> identity = Primitive64Store.FACTORY.makeEye(dim, dim);

        LU<Double> refDecomp = new RawLU();
        refDecomp.decompose(random); // Just pick one to use as a reference
        MatrixStore<Double> expected = refDecomp.getInverse();

        for (MatrixDecomposition.Solver<Double> decomp : MatrixDecompositionTests.getPrimitiveMatrixDecompositionSolver()) {
            String name = decomp.getClass().getName();

            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug(name);
            }

            decomp.decompose(random);

            MatrixStore<Double> actual = decomp.getInverse();

            TestUtils.assertEquals(name, expected, actual, ACCURACY);
            TestUtils.assertEquals(name, identity, actual.multiply(random), ACCURACY);
            TestUtils.assertEquals(name, identity, random.multiply(actual), ACCURACY);
        }
    }

    @Test
    public void testSimpleEquationCase() {

        MatrixStore<Double> body = Primitive64Store.FACTORY.copy(SimpleEquationCase.getBody());
        MatrixStore<Double> rhs = Primitive64Store.FACTORY.copy(SimpleEquationCase.getRHS());
        MatrixStore<Double> solution = Primitive64Store.FACTORY.copy(SimpleEquationCase.getSolution());

        for (MatrixDecomposition.Solver<Double> decomp : MatrixDecompositionTests.getPrimitiveMatrixDecompositionSolver()) {
            String name = decomp.getClass().getName();

            if (DEBUG) {
                BasicLogger.debug(name);
            }

            if (decomp instanceof Cholesky || decomp instanceof LDL || decomp instanceof Eigenvalue) {
                continue;
            }

            decomp.decompose(body);

            TestUtils.assertEquals(name, solution, decomp.getSolution(rhs), ACCURACY);

            MatrixStore<Double> mtrxI = body.physical().makeEye(body.countRows(), body.countColumns());

            MatrixStore<Double> expectedInverse = decomp.getSolution(mtrxI);

            TestUtils.assertEquals(name, expectedInverse, decomp.getInverse(), ACCURACY);

            TestUtils.assertEquals(name, mtrxI, expectedInverse.multiply(body), ACCURACY);
            TestUtils.assertEquals(name, mtrxI, body.multiply(expectedInverse), ACCURACY);
        }
    }

    @Test
    public void testSolveBothWaysSimpleCholeskyCase() {

        MatrixR064 body = SimpleCholeskyCase.getOriginal();
        MatrixR064 rhs = SimpleEquationCase.getRHS();

        Primitive64Store expected = Primitive64Store.FACTORY.make(rhs.getRowDim(), 1);
        Primitive64Store actual = Primitive64Store.FACTORY.make(rhs.getRowDim(), 1);

        for (MatrixDecomposition.Solver<Double> decomp : MatrixDecompositionTests.getPrimitiveMatrixDecompositionSolver()) {
            String name = decomp.getClass().getName();

            if (DEBUG) {
                BasicLogger.debug(name);
            }

            decomp.decompose(body);

            if (DEBUG) {
                BasicLogger.debugMatrix("inverse", decomp.getInverse());
            }

            decomp.ftran(rhs, expected);
            decomp.btran(rhs, actual);

            TestUtils.assertEquals(name, expected, actual);
        }
    }

    @Test
    public void testSolveBothWaysSimpleEquationCase() {

        MatrixR064 body = SimpleEquationCase.getBody();
        MatrixR064 rhs = SimpleEquationCase.getRHS();
        MatrixR064 solution = SimpleEquationCase.getSolution();

        Primitive64Store expected = Primitive64Store.FACTORY.make(solution.getRowDim(), solution.getColDim());
        Primitive64Store actual = Primitive64Store.FACTORY.make(solution.getRowDim(), solution.getColDim());

        for (MatrixDecomposition.Solver<Double> decomp : MatrixDecompositionTests.getPrimitiveMatrixDecompositionSolver()) {
            String name = decomp.getClass().getName();

            if (DEBUG) {
                BasicLogger.debug(name);
            }

            if (decomp instanceof Cholesky || decomp instanceof LDL || decomp instanceof Eigenvalue) {
                continue;
            }

            decomp.decompose(body);

            decomp.ftran(rhs, actual);

            TestUtils.assertEquals(name, solution, actual);

            decomp.decompose(body.transpose());

            decomp.ftran(rhs, expected);

            decomp.decompose(body);

            decomp.btran(rhs, actual);

            TestUtils.assertEquals(name, expected, actual);
        }
    }

}
