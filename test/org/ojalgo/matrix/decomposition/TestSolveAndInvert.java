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
package org.ojalgo.matrix.decomposition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.SimpleEquationCase;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class TestSolveAndInvert {

    static MatrixDecomposition.Solver<Double>[] getAllSquare() {
        return (MatrixDecomposition.Solver<Double>[]) new MatrixDecomposition.Solver<?>[] { LU.PRIMITIVE.make(), new RawLU(), QR.PRIMITIVE.make(), new RawQR(),
                SingularValue.PRIMITIVE.make(), new RawSingularValue()/* , new SVDold30.Primitive() */ };
    }

    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    @Test
    public void testInverseOfRandomCase() {

        final NumberContext tmpEqualsNumberContext = new NumberContext(7, 10);

        final int tmpDim = 99;
        final PhysicalStore<Double> tmpRandom = PrimitiveDenseStore.FACTORY.copy(MatrixUtils.makeRandomComplexStore(tmpDim, tmpDim));
        final PhysicalStore<Double> tmpIdentity = PrimitiveDenseStore.FACTORY.makeEye(tmpDim, tmpDim);

        final MatrixDecomposition.Solver<Double>[] tmpAllDecomps = TestSolveAndInvert.getAllSquare();

        final LU<Double> tmpRefDecomps = new RawLU();
        tmpRefDecomps.decompose(tmpRandom);
        final MatrixStore<Double> tmpExpected = tmpRefDecomps.getInverse();

        for (final MatrixDecomposition.Solver<Double> tmpDecomp : tmpAllDecomps) {
            final String tmpName = tmpDecomp.getClass().getName();

            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug(tmpName);
            }

            tmpDecomp.decompose(tmpRandom);

            final MatrixStore<Double> tmpActual = tmpDecomp.getInverse();

            TestUtils.assertEquals(tmpName, tmpExpected, tmpActual, tmpEqualsNumberContext);
            TestUtils.assertEquals(tmpName, tmpIdentity, tmpActual.multiply(tmpRandom), tmpEqualsNumberContext);
            TestUtils.assertEquals(tmpName, tmpIdentity, tmpRandom.multiply(tmpActual), tmpEqualsNumberContext);
        }
    }

    @Test
    public void testSimpleEquationCase() {

        final MatrixStore<Double> tmpBody = PrimitiveDenseStore.FACTORY.copy(SimpleEquationCase.getBody());
        final MatrixStore<Double> tmpRHS = PrimitiveDenseStore.FACTORY.copy(SimpleEquationCase.getRHS());
        final MatrixStore<Double> tmpSolution = PrimitiveDenseStore.FACTORY.copy(SimpleEquationCase.getSolution());

        for (final MatrixDecomposition.Solver<Double> tmpDecomp : TestSolveAndInvert.getAllSquare()) {
            this.doTest(tmpDecomp, tmpBody, tmpRHS, tmpSolution, new NumberContext(7, 6));
        }
    }

    private void doTest(final MatrixDecomposition.Solver<Double> decomposition, final MatrixStore<Double> body, final MatrixStore<Double> rhs,
            final MatrixStore<Double> solution, final NumberContext accuracy) {

        decomposition.decompose(body);

        TestUtils.assertEquals(solution, decomposition.getSolution(rhs), accuracy);

        final MatrixStore<Double> tmpI = body.physical().makeEye(body.countRows(), body.countColumns());

        final MatrixStore<Double> tmpExpectedInverse = decomposition.getSolution(tmpI);
        decomposition.reset();
        decomposition.decompose(body);
        TestUtils.assertEquals(tmpExpectedInverse, decomposition.getInverse(), accuracy);

        TestUtils.assertEquals(tmpI, tmpExpectedInverse.multiply(body), accuracy);
        TestUtils.assertEquals(tmpI, body.multiply(tmpExpectedInverse), accuracy);
    }
}
