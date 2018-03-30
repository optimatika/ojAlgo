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
package org.ojalgo.optimisation.convex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.access.Access2D;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.type.context.NumberContext;

/**
 * GenericQPSolverTest
 *
 * @author apete
 */
public abstract class GenericQPSolverTest extends OptimisationConvexTests {

    private PrimitiveDenseStore myAE;
    private PrimitiveDenseStore myAI;
    private PrimitiveDenseStore myBE;
    private PrimitiveDenseStore myBI;
    private PrimitiveDenseStore myC;
    private final NumberContext myEvaluationContext = new NumberContext(8, 8);
    private PrimitiveDenseStore myQ;
    private PrimitiveDenseStore myXE;
    private PrimitiveDenseStore myXI;

    public final MatrixStore<Double> getSolutionE() {
        return myXE.copy();
    }

    public final MatrixStore<Double> getSolutionI() {
        return myXI.copy();
    }

    @BeforeEach
    public void setUp() {

        final BasicMatrix[] tmpMatrices = this.getMatrices();

        if (tmpMatrices[0] != null) {
            myAE = PrimitiveDenseStore.FACTORY.copy(tmpMatrices[0]);
        } else {
            myAE = null;
        }
        if (tmpMatrices[1] != null) {
            myBE = PrimitiveDenseStore.FACTORY.copy(tmpMatrices[1]);
        } else {
            myBE = null;
        }
        if (tmpMatrices[2] != null) {
            myQ = PrimitiveDenseStore.FACTORY.copy(tmpMatrices[2]);
        } else {
            myQ = null;
        }
        if (tmpMatrices[3] != null) {
            myC = PrimitiveDenseStore.FACTORY.copy(tmpMatrices[3].negate());
        } else {
            myC = null;
        }
        if (tmpMatrices[4] != null) {
            myAI = PrimitiveDenseStore.FACTORY.copy(tmpMatrices[4]);
        } else {
            myAI = null;
        }
        if (tmpMatrices[5] != null) {
            myBI = PrimitiveDenseStore.FACTORY.copy(tmpMatrices[5]);
        } else {
            myBI = null;
        }
        if (tmpMatrices[6] != null) {
            myXE = PrimitiveDenseStore.FACTORY.copy(tmpMatrices[6]);
        } else {
            myXE = null;
        }
        if (tmpMatrices[7] != null) {
            myXI = PrimitiveDenseStore.FACTORY.copy(tmpMatrices[7]);
        } else {
            myXI = null;
        }

    }

    @Test
    public void testCaseData() {

        final MatrixStore<Double> tmpExpected = myBE;

        MatrixStore<Double> tmpActual = myAE.multiply(myXE);
        Access2D.equals(tmpExpected, tmpActual, myEvaluationContext);

        tmpActual = myAE.multiply(myXI);
        TestUtils.assertEquals(tmpExpected, tmpActual, myEvaluationContext);

        if ((myAI != null) && (myBI != null)) {

            final PhysicalStore<Double> tmpSlack = myBI.copy();
            tmpSlack.modifyMatching(PrimitiveFunction.SUBTRACT, myAI.multiply(myXI));

            for (int i = 0; i < tmpSlack.countRows(); i++) {
                TestUtils.assertTrue(tmpSlack.doubleValue(i, 0) > -myEvaluationContext.epsilon());
            }
        }
    }

    @Test
    public void testSolverResults() {

        final PrimitiveDenseStore[] tmpMatricesI = new PrimitiveDenseStore[] { myAE, myBE, myQ, myC, myAI, myBI };

        ConvexProblems.builAndTestModel(tmpMatricesI, myXI, myEvaluationContext, true);

        final PrimitiveDenseStore[] tmpMatricesE = new PrimitiveDenseStore[] { myAE, myBE, myQ, myC, null, null };

        ConvexProblems.builAndTestModel(tmpMatricesE, myXE, myEvaluationContext, true);
    }

    /**
     * @return {[AE],[BE],[Q],[C],[AI],[BI],[X only E constraints],[X both E and I constraints]}
     */
    abstract protected BasicMatrix[] getMatrices();

}
