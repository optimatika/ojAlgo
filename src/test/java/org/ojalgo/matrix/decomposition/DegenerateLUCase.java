/*
 * Copyright 1997-2022 Optimatika
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
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.Primitive32Store;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class DegenerateLUCase extends MatrixDecompositionTests {

    private static final NumberContext ACCURACY = NumberContext.of(7, 4);

    @Override
    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    @Test
    public void testComplex() {

        RationalMatrix square = SimpleEquationCase.getBody();
        GenericStore<ComplexNumber> degenerate = GenericStore.COMPLEX.copy(RationalMatrix.FACTORY.make(square).below(square).below(square));

        LU<ComplexNumber> decomp = LU.COMPLEX.make();
        decomp.decompose(degenerate);

        TestUtils.assertEquals(degenerate, decomp, ACCURACY);
    }

    @Test
    public void testDensePrimitive32() {

        RationalMatrix square = SimpleEquationCase.getBody();
        Primitive32Store degenerate = Primitive32Store.FACTORY.copy(RationalMatrix.FACTORY.make(square).below(square).below(square));

        LU<Double> decomp = LU.PRIMITIVE.make();
        decomp.decompose(degenerate);

        TestUtils.assertEquals(degenerate, decomp, ACCURACY);
    }

    @Test
    public void testDensePrimitive64() {

        RationalMatrix square = SimpleEquationCase.getBody();
        Primitive64Store degenerate = Primitive64Store.FACTORY.copy(RationalMatrix.FACTORY.make(square).below(square).below(square));

        LU<Double> decomp = LU.PRIMITIVE.make();
        decomp.decompose(degenerate);

        TestUtils.assertEquals(degenerate, decomp, ACCURACY);
    }

    @Test
    public void testRational() {

        RationalMatrix square = SimpleEquationCase.getBody();
        RationalMatrix degenerate = RationalMatrix.FACTORY.make(square).below(square).below(square);

        LU<RationalNumber> decomp = LU.RATIONAL.make();
        decomp.decompose(degenerate);

        TestUtils.assertEquals(GenericStore.RATIONAL.copy(degenerate), decomp, ACCURACY);
    }

    @Test
    public void testRawPrimitive() {

        RationalMatrix square = SimpleEquationCase.getBody();
        RawStore degenerate = RawStore.FACTORY.copy(RationalMatrix.FACTORY.make(square).below(square).below(square));

        LU<Double> decomp = new RawLU();
        decomp.decompose(degenerate);

        TestUtils.assertEquals(degenerate, decomp, ACCURACY);
    }

}
