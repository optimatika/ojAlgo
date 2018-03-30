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
package org.ojalgo.matrix;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.type.context.NumberContext;

/**
 * Gilbert Strang, Linear Algebra and its Applications III, Example 1 in Appendix A
 *
 * @author apete
 */
public class SimpleSingularValueCase extends BasicMatrixTest {

    public static RationalMatrix getOriginal() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.rows(new double[][] { { 2.0, 0.0 }, { 0.0, -3.0 }, { 0.0, 0.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    private static RationalMatrix getMatrixD() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.rows(new double[][] { { 2.0, 0.0 }, { 0.0, 3.0 }, { 0.0, 0.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    private static RationalMatrix getMatrixQ1() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.rows(new double[][] { { 1.0, 0.0, 0.0 }, { 0.0, -1.0, 0.0 }, { 0.0, 0.0, 1.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    private static RationalMatrix getMatrixQ2() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.rows(new double[][] { { 1.0, 0.0 }, { 0.0, 1.0 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    @BeforeEach
    @Override
    public void setUp() {

        DEFINITION = new NumberContext(7, 1);
        EVALUATION = new NumberContext(7, 9);

        myBigAA = SimpleSingularValueCase.getMatrixQ1();
        myBigAX = SimpleSingularValueCase.getMatrixD();
        myBigAB = SimpleSingularValueCase.getOriginal();

        myBigI = BasicMatrixTest.getIdentity(myBigAA.countRows(), myBigAA.countColumns(), DEFINITION);
        myBigSafe = BasicMatrixTest.getSafe(myBigAA.countRows(), myBigAA.countColumns(), DEFINITION);

        super.setUp();
    }

    @Test
    public void testData() {

        final PhysicalStore<Double> tmpExp = PrimitiveDenseStore.FACTORY.copy(SimpleSingularValueCase.getOriginal())
                .multiply(PrimitiveDenseStore.FACTORY.copy(SimpleSingularValueCase.getMatrixQ2())).copy();

        final PhysicalStore<Double> tmpAct = PrimitiveDenseStore.FACTORY.copy(SimpleSingularValueCase.getMatrixQ1())
                .multiply(PrimitiveDenseStore.FACTORY.copy(SimpleSingularValueCase.getMatrixD())).copy();

        TestUtils.assertEquals(tmpExp, tmpAct, EVALUATION);
    }

    @Test
    public void testProblem() {

        final MatrixStore<Double> tmpA = PrimitiveDenseStore.FACTORY.copy(SimpleSingularValueCase.getOriginal());

        final SingularValue<Double> tmpSVD = SingularValue.make(tmpA);
        tmpSVD.decompose(tmpA);

        //tmpSVD.equals(tmpA, EVALUATION);
        TestUtils.assertEquals(tmpA, tmpSVD, EVALUATION);
    }

}
