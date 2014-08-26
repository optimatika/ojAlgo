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
package org.ojalgo.optimisation.quadratic;

import org.ojalgo.TestUtils;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.type.StandardType;
import org.ojalgo.type.context.NumberContext;

/**
 * GenericQPSolverTest
 * 
 * @author apete
 */
public abstract class GenericQPSolverTest extends OptimisationQuadraticTests {

    private MatrixStore<Double> myAE;
    private MatrixStore<Double> myAI;
    private MatrixStore<Double> myBE;
    private MatrixStore<Double> myBI;
    private MatrixStore<Double> myC;
    private MatrixStore<Double> myQ;
    private MatrixStore<Double> myXE;
    private MatrixStore<Double> myXI;

    private QuadraticSolver.Builder myBuilderE;
    private QuadraticSolver.Builder myBuilderI;

    private final NumberContext myEvaluationContext = NumberContext.getGeneral(6);

    public GenericQPSolverTest() {
        super();
    }

    public GenericQPSolverTest(final String arg0) {
        super(arg0);
    }

    public final QuadraticSolver.Builder getBuilderE() {
        return myBuilderE;
    }

    public final QuadraticSolver.Builder getBuilderI() {
        return myBuilderI;
    }

    public final MatrixStore<Double> getSolutionE() {
        return myXE.copy();
    }

    public final MatrixStore<Double> getSolutionI() {
        return myXI.copy();
    }

    public void testCaseData() {

        final MatrixStore<Double> tmpExpected = myBE;

        MatrixStore<Double> tmpActual = myAE.multiplyRight(myXE);
        AccessUtils.equals(tmpExpected, tmpActual, myEvaluationContext);

        tmpActual = myAE.multiplyRight(myXI);
        TestUtils.assertEquals(tmpExpected, tmpActual, myEvaluationContext);

        if ((myAI != null) && (myBI != null)) {

            final PhysicalStore<Double> tmpSlack = myBI.copy();
            tmpSlack.fillMatching(tmpSlack, PrimitiveFunction.SUBTRACT, myAI.multiplyRight(myXI));

            for (int i = 0; i < tmpSlack.countRows(); i++) {
                TestUtils.assertTrue(tmpSlack.doubleValue(i, 0) > -myEvaluationContext.error());
            }
        }
    }

    public void testSolverResults() {
        if (OptimisationQuadraticTests.DEBUG) {
            BasicLogger.debug(myXE.copy().toString());
            BasicLogger.debug(myBuilderE.build().solve().toString());
        }
        TestUtils.assertEquals("Equality Constrained", myXE, myBuilderE.build().solve(), myEvaluationContext);
        TestUtils.assertEquals("Inequality Constrained", myXI, myBuilderI.build().solve(), myEvaluationContext);
    }

    /**
     * @return {[AE],[BE],[Q],[C],[AI],[BI],[X only E constraints],[X both E and I constraints]}
     */
    abstract protected BasicMatrix<?>[] getMatrices();

    @Override
    protected void setUp() throws Exception {

        super.setUp();

        final BasicMatrix<?>[] tmpMatrices = this.getMatrices();

        if (tmpMatrices[0] != null) {
            myAE = tmpMatrices[0].enforce(StandardType.DECIMAL_064).toPrimitiveStore();
        } else {
            myAE = null;
        }
        if (tmpMatrices[1] != null) {
            myBE = tmpMatrices[1].enforce(StandardType.DECIMAL_064).toPrimitiveStore();
        } else {
            myBE = null;
        }
        if (tmpMatrices[2] != null) {
            myQ = tmpMatrices[2].enforce(StandardType.DECIMAL_064).toPrimitiveStore();
        } else {
            myQ = null;
        }
        if (tmpMatrices[3] != null) {
            myC = tmpMatrices[3].negate().enforce(StandardType.DECIMAL_064).toPrimitiveStore();
        } else {
            myC = null;
        }
        if (tmpMatrices[4] != null) {
            myAI = tmpMatrices[4].enforce(StandardType.DECIMAL_064).toPrimitiveStore();
        } else {
            myAI = null;
        }
        if (tmpMatrices[5] != null) {
            myBI = tmpMatrices[5].enforce(StandardType.DECIMAL_064).toPrimitiveStore();
        } else {
            myBI = null;
        }
        if (tmpMatrices[6] != null) {
            myXE = tmpMatrices[6].enforce(StandardType.DECIMAL_064).toPrimitiveStore();
        } else {
            myXE = null;
        }
        if (tmpMatrices[7] != null) {
            myXI = tmpMatrices[7].enforce(StandardType.DECIMAL_064).toPrimitiveStore();
        } else {
            myXI = null;
        }

        myBuilderE = new QuadraticSolver.Builder(myQ, myC);
        myBuilderI = new QuadraticSolver.Builder(myQ, myC);

        myBuilderE.equalities(myAE, myBE);
        myBuilderI.equalities(myAE, myBE);

        if ((myAI != null) && (myBI != null)) {
            myBuilderI.inequalities(myAI, myBI);
        }
    }

}
