/*
 * Copyright 1997-2026 Optimatika
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
package org.ojalgo.matrix.store;

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.type.context.NumberContext;

/**
 * Applying a Householder reflector to a {@link RawStore} must give the same result as applying it to an
 * {@link R064Store}, for every column (left) or row (right) from the first one specified.
 */
public class HouseholderTransformTest extends MatrixStoreTests {

    private static final NumberContext ACCURACY = NumberContext.of(12);

    private static R064Store newMatrix(final Random random, final int nbRows, final int nbCols) {
        R064Store retVal = R064Store.FACTORY.make(nbRows, nbCols);
        for (int j = 0; j < nbCols; j++) {
            for (int i = 0; i < nbRows; i++) {
                retVal.set(i, j, random.nextGaussian());
            }
        }
        return retVal;
    }

    private static Householder.Primitive64 newReflector(final Random random, final int dim, final int first) {
        Householder.Primitive64 retVal = new Householder.Primitive64(dim);
        double sumOfSquares = 0.0;
        for (int i = first; i < dim; i++) {
            double value = random.nextGaussian();
            retVal.vector[i] = value;
            sumOfSquares += value * value;
        }
        retVal.first = first;
        retVal.beta = 2.0 / sumOfSquares;
        return retVal;
    }

    @Test
    public void testTransformLeft() {

        Random random = new Random(123L);

        for (int first : new int[] { 0, 2 }) {
            for (int firstColumn : new int[] { 0, 1, 3 }) {

                R064Store expected = HouseholderTransformTest.newMatrix(random, 6, 4);
                RawStore actual = RawStore.FACTORY.copy(expected);
                Householder.Primitive64 reflector = HouseholderTransformTest.newReflector(random, 6, first);

                expected.transformLeft(reflector, firstColumn);
                actual.transformLeft(reflector, firstColumn);

                TestUtils.assertEquals(expected, actual, ACCURACY);
            }
        }
    }

    @Test
    public void testTransformRight() {

        Random random = new Random(321L);

        for (int first : new int[] { 0, 2 }) {
            for (int firstRow : new int[] { 0, 1, 5 }) {

                R064Store expected = HouseholderTransformTest.newMatrix(random, 6, 4);
                RawStore actual = RawStore.FACTORY.copy(expected);
                Householder.Primitive64 reflector = HouseholderTransformTest.newReflector(random, 4, first);

                expected.transformRight(reflector, firstRow);
                actual.transformRight(reflector, firstRow);

                TestUtils.assertEquals(expected, actual, ACCURACY);
            }
        }
    }

}
