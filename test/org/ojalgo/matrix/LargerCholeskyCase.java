/*
 * Copyright 1997-2019 Optimatika
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
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * Random generated SPD.
 *
 * @author apete
 */
public class LargerCholeskyCase extends BasicMatrixTest {

    private static final NumberContext DEFINITION = new NumberContext(7, 4);

    public static RationalMatrix getOriginal() {

        final PhysicalStore<ComplexNumber> randomComplex = TestUtils.makeRandomComplexStore(9, 9);

        return RationalMatrix.FACTORY.copy(randomComplex.multiply(randomComplex.conjugate()));
    }

    @Override
    @BeforeEach
    public void setUp() {

        evaluation = new NumberContext(7, 3);

        rationalAB = LargerCholeskyCase.getOriginal();

        final Cholesky<RationalNumber> tmpCholesky = Cholesky.RATIONAL.make();
        tmpCholesky.decompose(GenericDenseStore.RATIONAL.copy(rationalAB));

        rationalAA = RationalMatrix.FACTORY.copy(tmpCholesky.getL());
        rationalAX = rationalAA.transpose();

        rationlI = BasicMatrixTest.getIdentity(rationalAA.countRows(), rationalAA.countColumns(), DEFINITION);
        rationalSafe = BasicMatrixTest.getSafe(rationalAA.countRows(), rationalAA.countColumns(), DEFINITION);

        super.setUp();
    }

    @Test
    public void testData() {

        final MatrixStore<Double> tmpMtrx = PrimitiveDenseStore.FACTORY.copy(LargerCholeskyCase.getOriginal());
        final Cholesky<Double> tmpDecomp = Cholesky.PRIMITIVE.make();
        tmpDecomp.decompose(tmpMtrx);
        TestUtils.assertEquals(true, tmpDecomp.isSolvable());
    }

    @Test
    public void testProblem() {

        final RationalMatrix tmpMtrx = LargerCholeskyCase.getOriginal();
        final Cholesky<Double> tmpDecomp = Cholesky.PRIMITIVE.make();
        tmpDecomp.decompose(PrimitiveDenseStore.FACTORY.copy(tmpMtrx));

        TestUtils.assertEquals(PrimitiveDenseStore.FACTORY.copy(tmpMtrx), tmpDecomp, evaluation);
    }

}
