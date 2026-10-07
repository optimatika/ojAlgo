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
package org.ojalgo.matrix.operation;

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.type.context.NumberContext;

/**
 * The single right-hand-side triangular solves on raw column-major data ({@link SubstituteForwards} and
 * {@link SubstituteBackwards}, used by ftran/btran) must solve the triangular system they are given – checked
 * through the residual, for all combinations of direction, conjugated and unit diagonal.
 */
public class SubstituteSingleRHSTest {

    private static final NumberContext ACCURACY = NumberContext.of(12);
    private static final int[] SIZES = { 1, 2, 5, 17, 64 };

    /**
     * The element of the triangular matrix being solved with: the lower (forwards) or upper (backwards)
     * triangle of the body, transposed when conjugated, with a unit diagonal if specified.
     */
    private static double element(final double[] body, final int dim, final int row, final int col, final boolean forwards, final boolean conjugated,
            final boolean unitDiagonal) {
        if (row == col) {
            return unitDiagonal ? 1.0 : body[row + row * dim];
        }
        int bodyRow = conjugated ? col : row;
        int bodyCol = conjugated ? row : col;
        boolean lower = forwards != conjugated;
        boolean inTriangle = lower ? bodyRow > bodyCol : bodyRow < bodyCol;
        return inTriangle ? body[bodyRow + bodyCol * dim] : 0.0;
    }

    private static void doTest(final boolean forwards, final boolean conjugated, final boolean unitDiagonal) {

        for (int dim : SIZES) {

            Random random = new Random(dim);

            double[] body = new double[dim * dim];
            for (int j = 0; j < dim; j++) {
                for (int i = 0; i < dim; i++) {
                    body[i + j * dim] = i == j ? 2.0 + random.nextDouble() : random.nextDouble() - 0.5;
                }
            }

            double[] rhs = new double[dim];
            for (int i = 0; i < dim; i++) {
                rhs[i] = random.nextDouble();
            }

            double[] solution = rhs.clone();
            if (forwards) {
                SubstituteForwards.invoke(solution, body, dim, unitDiagonal, conjugated);
            } else {
                SubstituteBackwards.invoke(solution, body, dim, unitDiagonal, conjugated);
            }

            double[] product = new double[dim];
            for (int i = 0; i < dim; i++) {
                for (int k = 0; k < dim; k++) {
                    product[i] += SubstituteSingleRHSTest.element(body, dim, i, k, forwards, conjugated, unitDiagonal) * solution[k];
                }
            }

            TestUtils.assertEquals(R064Store.wrap(rhs), R064Store.wrap(product), ACCURACY);
        }
    }

    @Test
    public void testBackwards() {
        SubstituteSingleRHSTest.doTest(false, false, false);
        SubstituteSingleRHSTest.doTest(false, false, true);
    }

    @Test
    public void testBackwardsConjugated() {
        SubstituteSingleRHSTest.doTest(false, true, false);
        SubstituteSingleRHSTest.doTest(false, true, true);
    }

    @Test
    public void testForwards() {
        SubstituteSingleRHSTest.doTest(true, false, false);
        SubstituteSingleRHSTest.doTest(true, false, true);
    }

    @Test
    public void testForwardsConjugated() {
        SubstituteSingleRHSTest.doTest(true, true, false);
        SubstituteSingleRHSTest.doTest(true, true, true);
    }

}
