/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
public class TestSolveAndInvert extends MatrixDecompositionTests {

    static MatrixDecomposition<Double>[] getAllSquare() {
        return (MatrixDecomposition<Double>[]) new MatrixDecomposition<?>[] { LU.makePrimitive(), LU.makeJama(),
                QR.makePrimitive(), QR.makeJama(), SingularValue.makePrimitive(), SingularValue.makeJama(),
                SingularValue.makeAlternative() };
    }

    public TestSolveAndInvert() {
        super();
    }

    public TestSolveAndInvert(final String arg0) {
        super(arg0);
    }

    public void testInverseOfRandomCase() {

        final NumberContext tmpEqualsNumberContext = new NumberContext(7, 10);

        final int tmpDim = 99;
        final PhysicalStore<Double> tmpRandom = PrimitiveDenseStore.FACTORY.copy(MatrixUtils.makeRandomComplexStore(tmpDim, tmpDim));
        final PhysicalStore<Double> tmpIdentity = PrimitiveDenseStore.FACTORY.makeEye(tmpDim, tmpDim);

        final MatrixDecomposition<Double>[] tmpAllDecomps = TestSolveAndInvert.getAllSquare();

        final MatrixDecomposition<Double> tmpRefDecomps = LU.makeJama();
        tmpRefDecomps.compute(tmpRandom);
        final MatrixStore<Double> tmpExpected = tmpRefDecomps.getInverse();

        for (final MatrixDecomposition<Double> tmpDecomp : tmpAllDecomps) {

            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug(tmpDecomp.getClass().getName());
            }

            tmpDecomp.compute(tmpRandom);

            final MatrixStore<Double> tmpActual = tmpDecomp.getInverse();

            TestUtils.assertEquals(tmpExpected, tmpActual, tmpEqualsNumberContext);
            TestUtils.assertEquals(tmpIdentity, tmpRandom.multiplyLeft(tmpActual), tmpEqualsNumberContext);
            TestUtils.assertEquals(tmpIdentity, tmpRandom.multiply(tmpActual), tmpEqualsNumberContext);
        }
    }

    public void testSimpleEquationCase() {

        final MatrixStore<Double> tmpBody = SimpleEquationCase.getBody().toPrimitiveStore();
        final MatrixStore<Double> tmpRHS = SimpleEquationCase.getRHS().toPrimitiveStore();
        final MatrixStore<Double> tmpSolution = SimpleEquationCase.getSolution().toPrimitiveStore();

        for (final MatrixDecomposition<Double> tmpDecomp : TestSolveAndInvert.getAllSquare()) {
            this.doTest(tmpDecomp, tmpBody, tmpRHS, tmpSolution);
        }
    }

    private void doTest(final MatrixDecomposition<Double> aDecomp, final MatrixStore<Double> aBody, final MatrixStore<Double> aRHS,
            final MatrixStore<Double> aSolution) {

        aDecomp.compute(aBody);

        TestUtils.assertEquals(aSolution, aDecomp.solve(aRHS), new NumberContext(7, 6));

        final MatrixStore<Double> tmpI = aBody.factory().makeEye(aBody.countRows(), aBody.countColumns());

        final MatrixStore<Double> tmpExpectedInverse = aDecomp.solve(tmpI);
        aDecomp.reset();
        aDecomp.compute(aBody);
        TestUtils.assertEquals(tmpExpectedInverse, aDecomp.getInverse(), new NumberContext(7, 6));

        TestUtils.assertEquals(tmpI, aBody.multiplyLeft(tmpExpectedInverse), new NumberContext(7, 6));
        TestUtils.assertEquals(tmpI, aBody.multiply(tmpExpectedInverse), new NumberContext(7, 6));
    }
}
