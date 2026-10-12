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
package org.ojalgo.matrix.decomposition;

import org.junit.jupiter.api.Test;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;

/**
 * Primarily to test that the same preallocated memory (PhysicalStore instance) can be used more than once
 * without error.
 */
public class PreallocationTest extends MatrixDecompositionTests {

    static final int DIM = 5;

    /**
     * The preallocated store is filled with junk (as if used for something else) before each call, and the
     * matrices make LU and LDL pivot. RawLU used to assume a zero-filled store when pivoting, and the SVD and
     * EvD implementations used to cache the preallocated store as their inverse.
     */
    @Test
    public void testDirtyPreallocated() throws RecoverableCondition {

        MatrixStore<Double> expected = R064Store.FACTORY.makeIdentity(DIM);

        for (MatrixDecomposition.Solver<Double> decomp : MatrixDecompositionTests.getPrimitiveMatrixDecompositionSolver()) {

            String name = decomp.getClass().getName();

            PhysicalStore<Double> preallocated = decomp.preallocate(expected);

            for (int r = 0; r < 3; r++) {

                MatrixStore<Double> matrix = MatrixDecompositionTests.makePivotingSPD(DIM);

                preallocated.fillAll(Uniform.standard());
                MatrixStore<Double> inverse = decomp.invert(matrix, preallocated);
                TestUtils.assertEquals(name, expected, matrix.multiply(inverse));

                preallocated.fillAll(Uniform.standard());
                decomp.decompose(matrix);
                inverse = decomp.getInverse(preallocated);
                TestUtils.assertEquals(name, expected, matrix.multiply(inverse));

                preallocated.fillAll(Uniform.standard());
                TestUtils.assertEquals(name, expected, matrix.multiply(decomp.getSolution(expected)));
                TestUtils.assertEquals(name, expected, matrix.multiply(decomp.getInverse()));
            }
        }
    }

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

    /**
     * The complex variant of Cholesky. The returned inverse is a hermitian view of the preallocated store's
     * lower triangle – its upper triangle is the conjugate of the lower.
     */
    @Test
    public void testHermitianInverse() {

        GenericStore<ComplexNumber> random = GenericStore.C128.make(DIM, DIM);
        for (int j = 0; j < DIM; j++) {
            for (int i = 0; i < DIM; i++) {
                random.set(i, j, ComplexNumber.of(Math.sin(1 + i + DIM * j), Math.cos(1 + i * j)));
            }
        }

        PhysicalStore<ComplexNumber> matrix = random.multiply(random.conjugate()).copy();
        for (int ij = 0; ij < DIM; ij++) {
            matrix.add(ij, ij, (double) DIM);
        }

        Cholesky<ComplexNumber> decomp = new DenseCholesky.C128();
        decomp.decompose(matrix);

        PhysicalStore<ComplexNumber> preallocated = decomp.preallocate(matrix);
        preallocated.fillAll(ComplexNumber.of(1.0, -1.0));

        MatrixStore<ComplexNumber> inverse = decomp.getInverse(preallocated);

        MatrixStore<ComplexNumber> identity = GenericStore.C128.makeIdentity(DIM);
        TestUtils.assertTrue(matrix.multiply(inverse).subtract(identity).norm() < 1E-12);
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

    /**
     * The result is read from the returned store. That may be the preallocated store itself, or a store based
     * on it – a hermitian view of its lower triangle (Cholesky inverse), a row permutation of it (LDL) – or a
     * cached inverse (SVD and EvD). The preallocated stores are filled with junk before each call, as if used
     * for something else.
     */
    @Test
    public void testResultFromReturnedStore() throws RecoverableCondition {

        MatrixStore<Double> identity = R064Store.FACTORY.makeIdentity(DIM);

        for (MatrixDecomposition.Solver<Double> decomp : MatrixDecompositionTests.getPrimitiveMatrixDecompositionSolver()) {

            String name = decomp.getClass().getName();

            MatrixStore<Double> matrix = MatrixDecompositionTests.makePivotingSPD(DIM);

            PhysicalStore<Double> preallocated = decomp.preallocate(matrix);

            preallocated.fillAll(Uniform.standard());
            TestUtils.assertEquals(name, identity, matrix.multiply(decomp.invert(matrix, preallocated)));

            decomp.decompose(matrix);

            preallocated.fillAll(Uniform.standard());
            TestUtils.assertEquals(name, identity, matrix.multiply(decomp.getInverse(preallocated)));

            for (int nbSolutions : new int[] { 1, 3 }) {

                MatrixStore<Double> rhs = R064Store.FACTORY.makeFilled(DIM, nbSolutions, Uniform.standard());
                PhysicalStore<Double> solution = decomp.preallocate(matrix, rhs);

                solution.fillAll(Uniform.standard());
                TestUtils.assertEquals(name, rhs, matrix.multiply(decomp.getSolution(rhs, solution)));

                solution.fillAll(Uniform.standard());
                TestUtils.assertEquals(name, rhs, matrix.multiply(decomp.solve(matrix, rhs, solution)));
            }

            decomp.decompose(matrix);
            decomp.getInverse();

            preallocated.fillAll(Uniform.standard());
            TestUtils.assertEquals(name, identity, matrix.multiply(decomp.getInverse(preallocated)));
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
