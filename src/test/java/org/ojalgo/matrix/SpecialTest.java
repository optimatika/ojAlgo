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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.random.Uniform;
import org.ojalgo.type.context.NumberContext;

public class SpecialTest extends MatrixTests {

    private static final NumberContext ACCURACY = NumberContext.of(8);

    /**
     * Should get the SVD based (pseudo)inverse and solution – a generalised inverse, and a solution that
     * satisfies a consistent equation system. Separate instances for invert and solve since a matrix caches
     * its decomposition.
     */
    private static void doTestSingular(final double[][] rows) {

        R064Store store = R064Store.FACTORY.rows(rows);

        SingularValue<Double> svd = SingularValue.R064.make(store);
        svd.decompose(store);

        MatrixR064 matrix = MatrixR064.FACTORY.copy(store);
        MatrixR064 inverse = matrix.invert();

        TestUtils.assertEquals(svd.getInverse(), inverse, ACCURACY);
        TestUtils.assertEquals(matrix, matrix.multiply(inverse).multiply(matrix), ACCURACY);

        MatrixStore<Double> rhs = store.multiply(R064Store.FACTORY.makeFilled(rows.length, 1L, new Uniform()));
        MatrixR064 solution = MatrixR064.FACTORY.copy(store).solve(rhs);

        TestUtils.assertEquals(svd.getSolution(rhs), solution, ACCURACY);
        TestUtils.assertEquals(rhs, matrix.multiply(solution), ACCURACY);
    }

    /**
     * The Laplacian of a complete graph – symmetric with rank dim - 1
     */
    private static double[][] makeLaplacian(final int dim) {

        double[][] retVal = new double[dim][dim];

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                retVal[i][j] = i == j ? dim - 1 : -1;
            }
        }

        return retVal;
    }

    /**
     * Integer elements, not symmetric, and the last row is the sum of the others
     */
    private static double[][] makeSingularFull(final int dim) {

        double[][] retVal = new double[dim][dim];

        for (int i = 0; i < dim - 1; i++) {
            for (int j = 0; j < dim; j++) {
                retVal[i][j] = (3 * i + j * j + 1) % 7 - 3;
                retVal[dim - 1][j] += retVal[i][j];
            }
        }

        return retVal;
    }

    @Test
    public void testCompareReceivers1() {

        MatrixQ128.DenseReceiver dense = MatrixQ128.FACTORY.newDenseBuilder(5, 7);
        MatrixR032.SparseReceiver sparse = MatrixR032.FACTORY.newSparseBuilder(5, 7);

        dense.set(1, 1, 1D);
        sparse.set(1, 1, 1D);

        dense.set(3, 5, 1D);
        sparse.set(3, 5, 1D);

        dense.set(4, 2, 1D);
        sparse.set(4, 2, 1D);

        TestUtils.assertEquals(dense.build(), sparse.build());
    }

    @Test
    public void testCompareReceivers2() {

        MatrixR064.DenseReceiver dense = MatrixR064.FACTORY.newDenseBuilder(5, 7);
        MatrixR128.SparseReceiver sparse = MatrixR128.FACTORY.newSparseBuilder(5, 7);

        dense.set(1, 1, 1D);
        sparse.set(1, 1, 1D);

        dense.set(3, 5, 1D);
        sparse.set(3, 5, 1D);

        dense.set(4, 2, 1D);
        sparse.set(4, 2, 1D);

        TestUtils.assertEquals(dense.build(), sparse.build());
    }

    /**
     * For dim <= 5 there are closed-form inverters and solvers. With singular matrices they used to return
     * NaN, Infinity or huge (finite) values, rather than failing so that the SVD fallback could be used.
     */
    @Test
    public void testSingularInvertAndSolve() {
        for (int dim = 1; dim <= 5; dim++) {
            SpecialTest.doTestSingular(SpecialTest.makeSingularFull(dim));
            SpecialTest.doTestSingular(SpecialTest.makeLaplacian(dim));
        }
    }

}
