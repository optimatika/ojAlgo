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
package org.ojalgo.matrix.decomposition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.RationalMatrix;
import org.ojalgo.matrix.SimpleEquationCase;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class DegenerateLUCase extends MatrixDecompositionTests {

    @Override
    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    @Test
    public void testBig() {

        final NumberContext evaluation = new NumberContext(7, 4);

        RationalMatrix square = SimpleEquationCase.getBody();
        final RationalMatrix degenerate = RationalMatrix.FACTORY.make(square).logical().below(square).below(square).get();

        final LU<RationalNumber> decomp = LU.RATIONAL.make();
        decomp.decompose(degenerate);

        TestUtils.assertEquals(GenericDenseStore.RATIONAL.copy(degenerate), decomp, evaluation);
    }

    @Test
    public void testComplex() {

        final NumberContext evaluation = new NumberContext(7, 4);

        RationalMatrix square = SimpleEquationCase.getBody();
        final RationalMatrix degenerate = RationalMatrix.FACTORY.make(square).logical().below(square).below(square).get();

        final LU<ComplexNumber> decomp = LU.COMPLEX.make();
        decomp.decompose(GenericDenseStore.COMPLEX.copy(degenerate));

        TestUtils.assertEquals(GenericDenseStore.COMPLEX.copy(degenerate), decomp, evaluation);
    }

    @Test
    public void testDensePrimitive() {

        final NumberContext evaluation = new NumberContext(7, 4);

        RationalMatrix square = SimpleEquationCase.getBody();
        final RationalMatrix degenerate = RationalMatrix.FACTORY.make(square).logical().below(square).below(square).get();

        final LU<Double> decomp = LU.PRIMITIVE.make();
        decomp.decompose(PrimitiveDenseStore.FACTORY.copy(degenerate));

        TestUtils.assertEquals(PrimitiveDenseStore.FACTORY.copy(degenerate), decomp, evaluation);
    }

    @Test
    public void testRawPrimitive() {

        final NumberContext evaluation = new NumberContext(7, 4);

        RationalMatrix square = SimpleEquationCase.getBody();
        final RationalMatrix degenerate = RationalMatrix.FACTORY.make(square).logical().below(square).below(square).get();

        final LU<Double> decomp = new RawLU();
        decomp.decompose(RawStore.FACTORY.copy(degenerate));

        TestUtils.assertEquals(RawStore.FACTORY.copy(degenerate), decomp, evaluation);
    }

}
