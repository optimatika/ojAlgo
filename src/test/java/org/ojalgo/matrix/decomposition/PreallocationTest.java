/*
 * Copyright 1997-2025 Optimatika
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

import org.junit.jupiter.api.Test;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.random.Uniform;

/**
 * Primarily to test that the same preallocated memory (PhysicalStore instance) can be used more than once
 * without error.
 */
public class PreallocationTest extends MatrixDecompositionTests {

    static final int DIM = 5;

    @Test
    public void testGetInverse() {

        for (MatrixDecomposition.Solver<Double> decomp : MatrixDecompositionTests.getPrimitiveMatrixDecompositionSolver()) {

            String name = decomp.getClass().getName();

            // Square matrix

            MatrixStore<Double> matrix = R064Store.FACTORY.makeSPD(DIM);

            decomp.decompose(matrix);

            // Preallocate inverse storage
            PhysicalStore<Double> preallocated = decomp.preallocate(matrix);

            // Get inverse
            MatrixStore<Double> inverse = decomp.getInverse(preallocated);

            // Check A * A^-1 = I
            MatrixStore<Double> expected = R064Store.FACTORY.makeIdentity(DIM);
            MatrixStore<Double> actual = matrix.multiply(inverse);
            TestUtils.assertEquals(name, expected, actual);

            // Reuse preallocated storage for another inverse
            MatrixStore<Double> matrix2 = R064Store.FACTORY.makeSPD(DIM);
            decomp.decompose(matrix2);
            MatrixStore<Double> inverse2 = decomp.getInverse(preallocated);
            MatrixStore<Double> actual2 = matrix2.multiply(inverse2);
            TestUtils.assertEquals(name, expected, actual2);
        }
    }

    @Test
    public void testGetSolution() {

        for (MatrixDecomposition.Solver<Double> decomp : MatrixDecompositionTests.getPrimitiveMatrixDecompositionSolver()) {

            String name = decomp.getClass().getName();

            // Create test matrices

            MatrixStore<Double> matrix = R064Store.FACTORY.makeSPD(DIM);
            MatrixStore<Double> rhs1 = R064Store.FACTORY.makeFilled(DIM, 1, Uniform.standard());
            MatrixStore<Double> rhs2 = R064Store.FACTORY.makeFilled(DIM, 1, Uniform.standard());

            // Decompose matrix
            decomp.decompose(matrix);

            // Preallocate solution storage
            PhysicalStore<Double> preallocated = decomp.preallocate(matrix, rhs1);

            // Solve first system
            MatrixStore<Double> solution1 = decomp.getSolution(rhs1, preallocated);
            TestUtils.assertEquals(name, rhs1, matrix.multiply(solution1));

            // Reuse preallocated storage for second system
            MatrixStore<Double> solution2 = decomp.getSolution(rhs2, preallocated);
            TestUtils.assertEquals(name, rhs2, matrix.multiply(solution2));
        }
    }

    @Test
    public void testInvert() throws RecoverableCondition {

        for (MatrixDecomposition.Solver<Double> decomp : MatrixDecompositionTests.getPrimitiveMatrixDecompositionSolver()) {

            String name = decomp.getClass().getName();

            MatrixStore<Double> matrix1 = R064Store.FACTORY.makeSPD(DIM);
            MatrixStore<Double> matrix2 = R064Store.FACTORY.makeSPD(DIM);

            MatrixStore<Double> expected = R064Store.FACTORY.makeIdentity(DIM);

            // Preallocate inverse storage
            PhysicalStore<Double> preallocated = decomp.preallocate(expected);

            // Check A * A^-1 = I
            MatrixStore<Double> inverse1 = decomp.invert(matrix1, preallocated);
            MatrixStore<Double> actual = matrix1.multiply(inverse1);
            TestUtils.assertEquals(name, expected, actual);

            // Reuse preallocated storage for another inverse
            decomp.decompose(matrix2);
            MatrixStore<Double> inverse2 = decomp.invert(matrix2, preallocated);
            MatrixStore<Double> actual2 = matrix2.multiply(inverse2);
            TestUtils.assertEquals(name, expected, actual2);
        }
    }

    @Test
    public void testSolve() throws RecoverableCondition {

        for (MatrixDecomposition.Solver<Double> decomp : MatrixDecompositionTests.getPrimitiveMatrixDecompositionSolver()) {

            String name = decomp.getClass().getName();

            // Create test matrices

            MatrixStore<Double> matrix = R064Store.FACTORY.makeSPD(DIM);
            MatrixStore<Double> rhs1 = R064Store.FACTORY.makeFilled(DIM, 1, Uniform.standard());
            MatrixStore<Double> rhs2 = R064Store.FACTORY.makeFilled(DIM, 1, Uniform.standard());

            // Preallocate solution storage
            PhysicalStore<Double> preallocated = decomp.preallocate(matrix, rhs1);

            // Solve first system
            MatrixStore<Double> solution1 = decomp.solve(matrix, rhs1, preallocated);
            TestUtils.assertEquals(name, rhs1, matrix.multiply(solution1));

            // Reuse preallocated storage for second system
            MatrixStore<Double> solution2 = decomp.solve(matrix, rhs2, preallocated);
            TestUtils.assertEquals(name, rhs2, matrix.multiply(solution2));
        }
    }

}
