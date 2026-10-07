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
import org.ojalgo.type.context.NumberContext;

/**
 * The single right-hand-side triangular solves of {@link R032Store} must solve the same systems as those of
 * {@link R064Store} – also when the body is to be used transposed (conjugated).
 */
public class SubstitutionR032Test extends MatrixStoreTests {

    private static final NumberContext FLOAT_ACCURACY = NumberContext.of(6);

    private static void doTest(final boolean forwards, final boolean conjugated, final boolean unitDiagonal) {

        Random random = new Random(3L);
        int dim = 7;

        R064Store body64 = R064Store.FACTORY.make(dim, dim);
        for (int j = 0; j < dim; j++) {
            for (int i = 0; i < dim; i++) {
                body64.set(i, j, i == j ? 2.0 + random.nextDouble() : random.nextDouble() - 0.5);
            }
        }
        R032Store body32 = R032Store.FACTORY.copy(body64);

        double[] expected = new double[dim];
        for (int i = 0; i < dim; i++) {
            expected[i] = random.nextDouble();
        }
        double[] actual = expected.clone();

        if (forwards) {
            body64.substituteForwards(conjugated, unitDiagonal, expected);
            body32.substituteForwards(conjugated, unitDiagonal, actual);
        } else {
            body64.substituteBackwards(conjugated, unitDiagonal, expected);
            body32.substituteBackwards(conjugated, unitDiagonal, actual);
        }

        TestUtils.assertEquals(R064Store.wrap(expected), R064Store.wrap(actual), FLOAT_ACCURACY);
    }

    @Test
    public void testBackwards() {
        SubstitutionR032Test.doTest(false, false, false);
        SubstitutionR032Test.doTest(false, false, true);
    }

    @Test
    public void testBackwardsConjugated() {
        SubstitutionR032Test.doTest(false, true, false);
        SubstitutionR032Test.doTest(false, true, true);
    }

    @Test
    public void testForwards() {
        SubstitutionR032Test.doTest(true, false, false);
        SubstitutionR032Test.doTest(true, false, true);
    }

    @Test
    public void testForwardsConjugated() {
        SubstitutionR032Test.doTest(true, true, false);
        SubstitutionR032Test.doTest(true, true, true);
    }

}
