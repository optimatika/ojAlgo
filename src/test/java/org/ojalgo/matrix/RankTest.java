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
package org.ojalgo.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.random.Normal;

/**
 * {@link BasicMatrix#getRank()} uses the cached decomposition, if it is rank revealing, otherwise a new
 * singular value decomposition. What is cached depends on what was done with the matrix before – it may be
 * an LU, QR, singular value or eigenvalue decomposition, or nothing at all.
 * <p>
 * The test matrices are products of random factors. The rank deficient ones are singular apart from rounding
 * errors. The singular values (or eigenvalues) reveal the rank of those reliably, but the LU or QR pivots do
 * not – for a few percent of these matrices the rank comes out too large. The tests allow that:
 * <ul>
 * <li>Full rank must always be found.
 * <li>A deficient rank must never be underestimated, and must be found for most of the matrices.
 * </ul>
 */
public class RankTest extends MatrixTests {

    private static final int TRIALS = 100;

    /**
     * Repeated for each of the things that may be done with a matrix before its rank is asked for, leaving
     * different decompositions cached.
     */
    private static void doTest(final int nbRows, final int nbCols, final int rank, final boolean symmetric) {

        List<Consumer<MatrixR064>> preparations = new ArrayList<>();
        preparations.add(matrix -> {});
        preparations.add(MatrixR064::getCondition);
        preparations.add(MatrixR064::invert);
        preparations.add(matrix -> matrix.solve(MatrixR064.FACTORY.makeFilled(nbRows, 1, new Normal())));
        if (nbRows == nbCols) {
            preparations.add(MatrixR064::getDeterminant);
            preparations.add(MatrixR064::getEigenpairs);
        }

        int minDim = Math.min(nbRows, nbCols);

        for (int p = 0; p < preparations.size(); p++) {

            int nbFound = 0;

            for (int t = 0; t < TRIALS; t++) {

                MatrixR064 matrix = RankTest.newMatrix(nbRows, nbCols, rank, symmetric);

                preparations.get(p).accept(matrix);

                int actual = matrix.getRank();

                TestUtils.assertTrue("Preparation " + p + ", rank " + actual, rank <= actual && actual <= minDim);

                if (actual == rank) {
                    nbFound++;
                }
            }

            TestUtils.assertTrue("Preparation " + p + ", found " + nbFound + " of " + TRIALS, nbFound > TRIALS / 2);
        }
    }

    private static MatrixR064 newMatrix(final int nbRows, final int nbCols, final int rank, final boolean symmetric) {

        MatrixR064 left = MatrixR064.FACTORY.makeFilled(nbRows, rank, new Normal());

        if (symmetric) {
            MatrixR064 product = left.multiply(left.transpose());
            return product.add(product.transpose()).divide(2.0);
        }

        MatrixR064 right = MatrixR064.FACTORY.makeFilled(rank, nbCols, new Normal());

        return left.multiply(right);
    }

    @Test
    public void testFat() {
        RankTest.doTest(5, 10, 4, false);
        RankTest.doTest(5, 10, 5, false);
    }

    @Test
    public void testSquare() {
        RankTest.doTest(10, 10, 9, false);
        RankTest.doTest(10, 10, 10, false);
    }

    @Test
    public void testSymmetric() {
        RankTest.doTest(10, 10, 9, true);
        RankTest.doTest(10, 10, 10, true);
    }

    @Test
    public void testTall() {
        RankTest.doTest(50, 20, 19, false);
        RankTest.doTest(50, 20, 20, false);
    }

}
