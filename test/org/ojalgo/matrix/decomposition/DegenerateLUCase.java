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
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.RationalMatrix;
import org.ojalgo.matrix.SimpleEquationCase;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class DegenerateLUCase {

    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    @Test
    public void testBig() {

        final NumberContext tmpEvalContext = new NumberContext(7, 4);

        final BasicMatrix tmpMtrxA = RationalMatrix.FACTORY
                .makeZero(SimpleEquationCase.getBody().countRows(), (int) SimpleEquationCase.getBody().countColumns())
                .mergeColumns(SimpleEquationCase.getBody()).mergeColumns(SimpleEquationCase.getBody());

        final LU<RationalNumber> tmpBigDecomp = LU.RATIONAL.make();
        tmpBigDecomp.decompose(GenericDenseStore.RATIONAL.copy(tmpMtrxA));
        //        System.out.println("A: " + tmpMtrxA.enforce(tmpEvalContext));
        //        System.out.println("P: " + new RationalMatrix(tmpBigDecomp.getP()).enforce(tmpEvalContext));
        //        System.out.println("L: " + new RationalMatrix(tmpBigDecomp.getL()).enforce(tmpEvalContext));
        //        System.out.println("PL: " + new RationalMatrix(tmpBigDecomp.getP().multiplyRight(tmpBigDecomp.getL())).enforce(tmpEvalContext));
        //        System.out.println("D: " + new RationalMatrix(tmpBigDecomp.getD()).enforce(tmpEvalContext));
        //        System.out.println("U: " + new RationalMatrix(tmpBigDecomp.getU()).enforce(tmpEvalContext));
        //        System.out.println("DU: " + new RationalMatrix(tmpBigDecomp.getD().multiplyRight(tmpBigDecomp.getU())).enforce(tmpEvalContext));
        TestUtils.assertEquals(GenericDenseStore.RATIONAL.copy(tmpMtrxA), tmpBigDecomp, tmpEvalContext);
    }

    @Test
    public void testComplex() {

        final NumberContext tmpEvalContext = new NumberContext(7, 4);

        final BasicMatrix tmpMtrxA = RationalMatrix.FACTORY
                .makeZero(SimpleEquationCase.getBody().countRows(), (int) SimpleEquationCase.getBody().countColumns())
                .mergeColumns(SimpleEquationCase.getBody()).mergeColumns(SimpleEquationCase.getBody());

        final LU<ComplexNumber> tmpComplexDecomp = LU.COMPLEX.make();
        tmpComplexDecomp.decompose(ComplexDenseStore.FACTORY.copy(tmpMtrxA));
        //        System.out.println("A: " + tmpMtrxA.enforce(tmpEvalContext));
        //        System.out.println("P: " + new ComplexMatrix(tmpComplexDecomp.getP()).enforce(tmpEvalContext));
        //        System.out.println("L: " + new ComplexMatrix(tmpComplexDecomp.getL()).enforce(tmpEvalContext));
        //        System.out.println("PL: " + new ComplexMatrix(tmpComplexDecomp.getP().multiplyRight(tmpComplexDecomp.getL())).enforce(tmpEvalContext));
        //        System.out.println("D: " + new ComplexMatrix(tmpComplexDecomp.getD()).enforce(tmpEvalContext));
        //        System.out.println("U: " + new ComplexMatrix(tmpComplexDecomp.getU()).enforce(tmpEvalContext));
        //        System.out.println("DU: " + new ComplexMatrix(tmpComplexDecomp.getD().multiplyRight(tmpComplexDecomp.getU())).enforce(tmpEvalContext));
        TestUtils.assertEquals(ComplexDenseStore.FACTORY.copy(tmpMtrxA), tmpComplexDecomp, tmpEvalContext);
    }

    @Test
    public void testDensePrimitive() {

        final NumberContext tmpEvalContext = new NumberContext(7, 4);

        final BasicMatrix tmpMtrxA = RationalMatrix.FACTORY
                .makeZero(SimpleEquationCase.getBody().countRows(), (int) SimpleEquationCase.getBody().countColumns())
                .mergeColumns(SimpleEquationCase.getBody()).mergeColumns(SimpleEquationCase.getBody());

        final LU<Double> tmpDoubleDecomp = LU.PRIMITIVE.make();
        tmpDoubleDecomp.decompose(PrimitiveDenseStore.FACTORY.copy(tmpMtrxA));
        //        System.out.println("A: " + tmpMtrxA.enforce(tmpEvalContext));
        //        System.out.println("P: " + new PrimitiveMatrix(tmpDoubleDecomp.getP()).enforce(tmpEvalContext));
        //        System.out.println("L: " + new PrimitiveMatrix(tmpDoubleDecomp.getL()).enforce(tmpEvalContext));
        //        System.out.println("PL: " + new PrimitiveMatrix(tmpDoubleDecomp.getP().multiplyRight(tmpDoubleDecomp.getL())).enforce(tmpEvalContext));
        //        System.out.println("D: " + new PrimitiveMatrix(tmpDoubleDecomp.getD()).enforce(tmpEvalContext));
        //        System.out.println("U: " + new PrimitiveMatrix(tmpDoubleDecomp.getU()).enforce(tmpEvalContext));
        //        System.out.println("DU: " + new PrimitiveMatrix(tmpDoubleDecomp.getD().multiplyRight(tmpDoubleDecomp.getU())).enforce(tmpEvalContext));
        TestUtils.assertEquals(PrimitiveDenseStore.FACTORY.copy(tmpMtrxA), tmpDoubleDecomp, tmpEvalContext);
    }

    @Test
    public void testJama() {

        final NumberContext tmpEvalContext = new NumberContext(7, 4);

        final BasicMatrix tmpMtrxA = RationalMatrix.FACTORY
                .makeZero(SimpleEquationCase.getBody().countRows(), (int) SimpleEquationCase.getBody().countColumns())
                .mergeColumns(SimpleEquationCase.getBody()).mergeColumns(SimpleEquationCase.getBody());

        final LU<Double> tmpDoubleDecomp = new RawLU();
        tmpDoubleDecomp.decompose(PrimitiveDenseStore.FACTORY.copy(tmpMtrxA));
        //        System.out.println("A: " + tmpMtrxA.enforce(tmpEvalContext));
        //        System.out.println("P: " + new PrimitiveMatrix(tmpDoubleDecomp.getP()).enforce(tmpEvalContext));
        //        System.out.println("L: " + new PrimitiveMatrix(tmpDoubleDecomp.getL()).enforce(tmpEvalContext));
        //        System.out.println("PL: " + new PrimitiveMatrix(tmpDoubleDecomp.getP().multiplyRight(tmpDoubleDecomp.getL())).enforce(tmpEvalContext));
        //        System.out.println("D: " + new PrimitiveMatrix(tmpDoubleDecomp.getD()).enforce(tmpEvalContext));
        //        System.out.println("U: " + new PrimitiveMatrix(tmpDoubleDecomp.getU()).enforce(tmpEvalContext));
        //        System.out.println("DU: " + new PrimitiveMatrix(tmpDoubleDecomp.getD().multiplyRight(tmpDoubleDecomp.getU())).enforce(tmpEvalContext));
        TestUtils.assertEquals(PrimitiveDenseStore.FACTORY.copy(tmpMtrxA), tmpDoubleDecomp, tmpEvalContext);
    }

    @Test
    public void testRawPrimitive() {

        final NumberContext tmpEvalContext = new NumberContext(7, 4);

        final BasicMatrix tmpMtrxA = RationalMatrix.FACTORY
                .makeZero(SimpleEquationCase.getBody().countRows(), (int) SimpleEquationCase.getBody().countColumns())
                .mergeColumns(SimpleEquationCase.getBody()).mergeColumns(SimpleEquationCase.getBody());

        final LU<Double> tmpDoubleDecomp = LU.PRIMITIVE.make();
        tmpDoubleDecomp.decompose(PrimitiveDenseStore.FACTORY.copy(tmpMtrxA));
        //        System.out.println("A: " + tmpMtrxA.enforce(tmpEvalContext));
        //        System.out.println("P: " + new PrimitiveMatrix(tmpDoubleDecomp.getP()).enforce(tmpEvalContext));
        //        System.out.println("L: " + new PrimitiveMatrix(tmpDoubleDecomp.getL()).enforce(tmpEvalContext));
        //        System.out.println("PL: " + new PrimitiveMatrix(tmpDoubleDecomp.getP().multiplyRight(tmpDoubleDecomp.getL())).enforce(tmpEvalContext));
        //        System.out.println("D: " + new PrimitiveMatrix(tmpDoubleDecomp.getD()).enforce(tmpEvalContext));
        //        System.out.println("U: " + new PrimitiveMatrix(tmpDoubleDecomp.getU()).enforce(tmpEvalContext));
        //        System.out.println("DU: " + new PrimitiveMatrix(tmpDoubleDecomp.getD().multiplyRight(tmpDoubleDecomp.getU())).enforce(tmpEvalContext));
        TestUtils.assertEquals(PrimitiveDenseStore.FACTORY.copy(tmpMtrxA), tmpDoubleDecomp, tmpEvalContext);
    }
}
