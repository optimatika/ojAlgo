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
package org.ojalgo.matrix;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.type.context.NumberContext;

/**
 * Gilbert Strang, Linear Algebra and its Applications III, Example 1 in Appendix A
 *
 * @author apete
 */
public class SimpleSingularValueCase extends BasicMatrixTest {

    private static final NumberContext DEFINITION = NumberContext.of(7, 1);

    public static MatrixR064 getOriginal() {
        MatrixR064 tmpMtrx = MatrixR064.FACTORY.copy(RawStore.wrap(new double[][] { { 2.0, 0.0 }, { 0.0, -3.0 }, { 0.0, 0.0 } }));
        return tmpMtrx.enforce(DEFINITION);
    }

    private static MatrixR064 getMatrixD() {
        MatrixR064 tmpMtrx = MatrixR064.FACTORY.copy(RawStore.wrap(new double[][] { { 2.0, 0.0 }, { 0.0, 3.0 }, { 0.0, 0.0 } }));
        return tmpMtrx.enforce(DEFINITION);
    }

    private static MatrixR064 getMatrixQ1() {
        MatrixR064 tmpMtrx = MatrixR064.FACTORY.copy(RawStore.wrap(new double[][] { { 1.0, 0.0, 0.0 }, { 0.0, -1.0, 0.0 }, { 0.0, 0.0, 1.0 } }));
        return tmpMtrx.enforce(DEFINITION);
    }

    private static MatrixR064 getMatrixQ2() {
        MatrixR064 tmpMtrx = MatrixR064.FACTORY.copy(RawStore.wrap(new double[][] { { 1.0, 0.0 }, { 0.0, 1.0 } }));
        return tmpMtrx.enforce(DEFINITION);
    }

    @Override
    @BeforeEach
    public void doBeforeEach() {

        mtrxA = SimpleSingularValueCase.getMatrixQ1();
        mtrxX = SimpleSingularValueCase.getMatrixD();
        mtrxB = SimpleSingularValueCase.getOriginal();

        mtrxI = BasicMatrixTest.getIdentity(mtrxA.countRows(), mtrxA.countColumns(), DEFINITION);
        mtrxSafe = BasicMatrixTest.getSafe(mtrxA.countRows(), mtrxA.countColumns(), DEFINITION);

        super.doBeforeEach();
    }

    @Test
    public void testData() {

        PhysicalStore<Double> tmpExp = R064Store.FACTORY.copy(SimpleSingularValueCase.getOriginal())
                .multiply(R064Store.FACTORY.copy(SimpleSingularValueCase.getMatrixQ2())).copy();

        PhysicalStore<Double> tmpAct = R064Store.FACTORY.copy(SimpleSingularValueCase.getMatrixQ1())
                .multiply(R064Store.FACTORY.copy(SimpleSingularValueCase.getMatrixD())).copy();

        TestUtils.assertEquals(tmpExp, tmpAct, ACCURACY);
    }

    @Test
    public void testProblem() {

        MatrixStore<Double> tmpA = R064Store.FACTORY.copy(SimpleSingularValueCase.getOriginal());

        SingularValue<Double> tmpSVD = SingularValue.R064.make(tmpA);
        tmpSVD.decompose(tmpA);

        //tmpSVD.equals(tmpA, EVALUATION);
        TestUtils.assertEquals(tmpA, tmpSVD, ACCURACY);
    }

}
