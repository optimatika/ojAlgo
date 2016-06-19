/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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

import java.math.BigDecimal;

import org.ojalgo.TestUtils;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.BigMatrix;
import org.ojalgo.matrix.SimpleEquationCase;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class DegenerateLUCase extends MatrixDecompositionTests {

    public DegenerateLUCase() {
        super();
    }

    public DegenerateLUCase(final String arg0) {
        super(arg0);
    }

    public void testBig() {

        final NumberContext tmpEvalContext = new NumberContext(7, 4);

        final BasicMatrix tmpMtrxA = BigMatrix.FACTORY.makeZero(SimpleEquationCase.getBody().countRows(), (int) SimpleEquationCase.getBody().countColumns())
                .mergeColumns(SimpleEquationCase.getBody()).mergeColumns(SimpleEquationCase.getBody());

        final LU<BigDecimal> tmpBigDecomp = LU.BIG.make();
        tmpBigDecomp.decompose(tmpMtrxA.toBigStore());
        //        System.out.println("A: " + tmpMtrxA.enforce(tmpEvalContext));
        //        System.out.println("P: " + new BigMatrix(tmpBigDecomp.getP()).enforce(tmpEvalContext));
        //        System.out.println("L: " + new BigMatrix(tmpBigDecomp.getL()).enforce(tmpEvalContext));
        //        System.out.println("PL: " + new BigMatrix(tmpBigDecomp.getP().multiplyRight(tmpBigDecomp.getL())).enforce(tmpEvalContext));
        //        System.out.println("D: " + new BigMatrix(tmpBigDecomp.getD()).enforce(tmpEvalContext));
        //        System.out.println("U: " + new BigMatrix(tmpBigDecomp.getU()).enforce(tmpEvalContext));
        //        System.out.println("DU: " + new BigMatrix(tmpBigDecomp.getD().multiplyRight(tmpBigDecomp.getU())).enforce(tmpEvalContext));
        TestUtils.assertEquals(tmpMtrxA.toBigStore(), tmpBigDecomp, tmpEvalContext);
    }

    public void testComplex() {

        final NumberContext tmpEvalContext = new NumberContext(7, 4);

        final BasicMatrix tmpMtrxA = BigMatrix.FACTORY.makeZero(SimpleEquationCase.getBody().countRows(), (int) SimpleEquationCase.getBody().countColumns())
                .mergeColumns(SimpleEquationCase.getBody()).mergeColumns(SimpleEquationCase.getBody());

        final LU<ComplexNumber> tmpComplexDecomp = LU.COMPLEX.make();
        tmpComplexDecomp.decompose(tmpMtrxA.toComplexStore());
        //        System.out.println("A: " + tmpMtrxA.enforce(tmpEvalContext));
        //        System.out.println("P: " + new ComplexMatrix(tmpComplexDecomp.getP()).enforce(tmpEvalContext));
        //        System.out.println("L: " + new ComplexMatrix(tmpComplexDecomp.getL()).enforce(tmpEvalContext));
        //        System.out.println("PL: " + new ComplexMatrix(tmpComplexDecomp.getP().multiplyRight(tmpComplexDecomp.getL())).enforce(tmpEvalContext));
        //        System.out.println("D: " + new ComplexMatrix(tmpComplexDecomp.getD()).enforce(tmpEvalContext));
        //        System.out.println("U: " + new ComplexMatrix(tmpComplexDecomp.getU()).enforce(tmpEvalContext));
        //        System.out.println("DU: " + new ComplexMatrix(tmpComplexDecomp.getD().multiplyRight(tmpComplexDecomp.getU())).enforce(tmpEvalContext));
        TestUtils.assertEquals(tmpMtrxA.toComplexStore(), tmpComplexDecomp, tmpEvalContext);
    }

    public void testDensePrimitive() {

        final NumberContext tmpEvalContext = new NumberContext(7, 4);

        final BasicMatrix tmpMtrxA = BigMatrix.FACTORY.makeZero(SimpleEquationCase.getBody().countRows(), (int) SimpleEquationCase.getBody().countColumns())
                .mergeColumns(SimpleEquationCase.getBody()).mergeColumns(SimpleEquationCase.getBody());

        final LU<Double> tmpDoubleDecomp = LU.PRIMITIVE.make();
        tmpDoubleDecomp.decompose(tmpMtrxA.toPrimitiveStore());
        //        System.out.println("A: " + tmpMtrxA.enforce(tmpEvalContext));
        //        System.out.println("P: " + new PrimitiveMatrix(tmpDoubleDecomp.getP()).enforce(tmpEvalContext));
        //        System.out.println("L: " + new PrimitiveMatrix(tmpDoubleDecomp.getL()).enforce(tmpEvalContext));
        //        System.out.println("PL: " + new PrimitiveMatrix(tmpDoubleDecomp.getP().multiplyRight(tmpDoubleDecomp.getL())).enforce(tmpEvalContext));
        //        System.out.println("D: " + new PrimitiveMatrix(tmpDoubleDecomp.getD()).enforce(tmpEvalContext));
        //        System.out.println("U: " + new PrimitiveMatrix(tmpDoubleDecomp.getU()).enforce(tmpEvalContext));
        //        System.out.println("DU: " + new PrimitiveMatrix(tmpDoubleDecomp.getD().multiplyRight(tmpDoubleDecomp.getU())).enforce(tmpEvalContext));
        TestUtils.assertEquals(tmpMtrxA.toPrimitiveStore(), tmpDoubleDecomp, tmpEvalContext);
    }

    public void testJama() {

        final NumberContext tmpEvalContext = new NumberContext(7, 4);

        final BasicMatrix tmpMtrxA = BigMatrix.FACTORY.makeZero(SimpleEquationCase.getBody().countRows(), (int) SimpleEquationCase.getBody().countColumns())
                .mergeColumns(SimpleEquationCase.getBody()).mergeColumns(SimpleEquationCase.getBody());

        final LU<Double> tmpDoubleDecomp = new RawLU();
        tmpDoubleDecomp.decompose(tmpMtrxA.toPrimitiveStore());
        //        System.out.println("A: " + tmpMtrxA.enforce(tmpEvalContext));
        //        System.out.println("P: " + new PrimitiveMatrix(tmpDoubleDecomp.getP()).enforce(tmpEvalContext));
        //        System.out.println("L: " + new PrimitiveMatrix(tmpDoubleDecomp.getL()).enforce(tmpEvalContext));
        //        System.out.println("PL: " + new PrimitiveMatrix(tmpDoubleDecomp.getP().multiplyRight(tmpDoubleDecomp.getL())).enforce(tmpEvalContext));
        //        System.out.println("D: " + new PrimitiveMatrix(tmpDoubleDecomp.getD()).enforce(tmpEvalContext));
        //        System.out.println("U: " + new PrimitiveMatrix(tmpDoubleDecomp.getU()).enforce(tmpEvalContext));
        //        System.out.println("DU: " + new PrimitiveMatrix(tmpDoubleDecomp.getD().multiplyRight(tmpDoubleDecomp.getU())).enforce(tmpEvalContext));
        TestUtils.assertEquals(tmpMtrxA.toPrimitiveStore(), tmpDoubleDecomp, tmpEvalContext);
    }

    public void testRawPrimitive() {

        final NumberContext tmpEvalContext = new NumberContext(7, 4);

        final BasicMatrix tmpMtrxA = BigMatrix.FACTORY.makeZero(SimpleEquationCase.getBody().countRows(), (int) SimpleEquationCase.getBody().countColumns())
                .mergeColumns(SimpleEquationCase.getBody()).mergeColumns(SimpleEquationCase.getBody());

        final LU<Double> tmpDoubleDecomp = LU.PRIMITIVE.make();
        tmpDoubleDecomp.decompose(tmpMtrxA.toPrimitiveStore());
        //        System.out.println("A: " + tmpMtrxA.enforce(tmpEvalContext));
        //        System.out.println("P: " + new PrimitiveMatrix(tmpDoubleDecomp.getP()).enforce(tmpEvalContext));
        //        System.out.println("L: " + new PrimitiveMatrix(tmpDoubleDecomp.getL()).enforce(tmpEvalContext));
        //        System.out.println("PL: " + new PrimitiveMatrix(tmpDoubleDecomp.getP().multiplyRight(tmpDoubleDecomp.getL())).enforce(tmpEvalContext));
        //        System.out.println("D: " + new PrimitiveMatrix(tmpDoubleDecomp.getD()).enforce(tmpEvalContext));
        //        System.out.println("U: " + new PrimitiveMatrix(tmpDoubleDecomp.getU()).enforce(tmpEvalContext));
        //        System.out.println("DU: " + new PrimitiveMatrix(tmpDoubleDecomp.getD().multiplyRight(tmpDoubleDecomp.getU())).enforce(tmpEvalContext));
        TestUtils.assertEquals(tmpMtrxA.toPrimitiveStore(), tmpDoubleDecomp, tmpEvalContext);
    }
}
