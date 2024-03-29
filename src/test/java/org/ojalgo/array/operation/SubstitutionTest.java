/*
 * Copyright 1997-2024 Optimatika
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
package org.ojalgo.array.operation;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.RationalNumber;

public class SubstitutionTest extends ArrayOperationTests {

    @Test
    public void testSubstituteBackwards() {

        int numbEquations = 4;
        int numbSolutions = 3;

        MatrixStore<Double> primitiveBody = R064Store.FACTORY.makeIdentity(numbEquations);
        MatrixStore<RationalNumber> rationalBody = GenericStore.Q128.makeIdentity(numbEquations);

        R064Store denseRHS = R064Store.FACTORY.makeFilled(numbEquations, numbSolutions, new Uniform());
        RawStore rawRHS = RawStore.FACTORY.copy(denseRHS);
        GenericStore<RationalNumber> rationalRHS = GenericStore.Q128.copy(denseRHS);

        R064Store expected = R064Store.FACTORY.copy(denseRHS);

        SubstituteBackwards.invoke(denseRHS.data, numbEquations, 0, numbSolutions, primitiveBody, false, false, false);
        SubstituteBackwards.invoke(rawRHS.data, primitiveBody, false, false, false);
        SubstituteBackwards.invoke(rationalRHS.data, numbEquations, 0, numbSolutions, rationalBody, false, false, false, RationalNumber.FACTORY);

        TestUtils.assertEquals(expected, denseRHS);
        TestUtils.assertEquals(expected, rawRHS);
        TestUtils.assertEquals(expected, rationalRHS);

        Cholesky<Double> cholesky = Cholesky.R064.make(numbEquations, numbEquations);
        cholesky.decompose(R064Store.FACTORY.makeSPD(numbEquations));

        primitiveBody = cholesky.getR();
        rationalBody = GenericStore.Q128.copy(primitiveBody);

        SubstituteBackwards.invoke(denseRHS.data, numbEquations, 0, numbSolutions, primitiveBody, true, true, false);
        SubstituteBackwards.invoke(rawRHS.data, primitiveBody, true, true, false);
        SubstituteBackwards.invoke(rationalRHS.data, numbEquations, 0, numbSolutions, rationalBody, true, true, false, RationalNumber.FACTORY);

        TestUtils.assertEquals(expected, denseRHS);
        TestUtils.assertEquals(expected, rawRHS);
        TestUtils.assertEquals(expected, rationalRHS);

        SubstituteBackwards.invoke(denseRHS.data, numbEquations, 0, numbSolutions, primitiveBody, false, false, false);
        SubstituteBackwards.invoke(rawRHS.data, primitiveBody, false, false, false);
        SubstituteBackwards.invoke(rationalRHS.data, numbEquations, 0, numbSolutions, rationalBody, false, false, false, RationalNumber.FACTORY);

        TestUtils.assertEquals(denseRHS, rawRHS);
        TestUtils.assertEquals(denseRHS, rationalRHS);
    }

    @Test
    public void testSubstituteForwards() {

        int numbEquations = 4;
        int numbSolutions = 3;

        MatrixStore<Double> primitiveBody = R064Store.FACTORY.makeIdentity(numbEquations);
        MatrixStore<RationalNumber> rationalBody = GenericStore.Q128.makeIdentity(numbEquations);

        R064Store denseRHS = R064Store.FACTORY.makeFilled(numbEquations, numbSolutions, new Uniform());
        RawStore rawRHS = RawStore.FACTORY.copy(denseRHS);
        GenericStore<RationalNumber> rationalRHS = GenericStore.Q128.copy(denseRHS);

        R064Store expected = R064Store.FACTORY.copy(denseRHS);

        SubstituteForwards.invoke(denseRHS.data, numbEquations, 0, numbSolutions, primitiveBody, false, false, false);
        SubstituteForwards.invoke(rawRHS.data, primitiveBody, false, false, false);
        SubstituteForwards.invoke(rationalRHS.data, numbEquations, 0, numbSolutions, rationalBody, false, false, false, RationalNumber.FACTORY);

        TestUtils.assertEquals(expected, denseRHS);
        TestUtils.assertEquals(expected, rawRHS);
        TestUtils.assertEquals(expected, rationalRHS);

        Cholesky<Double> cholesky = Cholesky.R064.make(numbEquations, numbEquations);
        cholesky.decompose(R064Store.FACTORY.makeSPD(numbEquations));

        primitiveBody = cholesky.getL();
        rationalBody = GenericStore.Q128.copy(primitiveBody);

        SubstituteForwards.invoke(denseRHS.data, numbEquations, 0, numbSolutions, primitiveBody, true, true, false);
        SubstituteForwards.invoke(rawRHS.data, primitiveBody, true, true, false);
        SubstituteForwards.invoke(rationalRHS.data, numbEquations, 0, numbSolutions, rationalBody, true, true, false, RationalNumber.FACTORY);

        TestUtils.assertEquals(expected, denseRHS);
        TestUtils.assertEquals(expected, rawRHS);
        TestUtils.assertEquals(expected, rationalRHS);

        SubstituteForwards.invoke(denseRHS.data, numbEquations, 0, numbSolutions, primitiveBody, false, false, false);
        SubstituteForwards.invoke(rawRHS.data, primitiveBody, false, false, false);
        SubstituteForwards.invoke(rationalRHS.data, numbEquations, 0, numbSolutions, rationalBody, false, false, false, RationalNumber.FACTORY);

        TestUtils.assertEquals(denseRHS, rawRHS);
        TestUtils.assertEquals(denseRHS, rationalRHS);
    }

}
