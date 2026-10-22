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

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.Quaternion;

/**
 * Decompositions of matrices with genuinely quaternionic elements. Quaternion multiplication does not
 * commute, and these matrices expose factors multiplied in the wrong order – real or complex matrices can't.
 * The matrices are random, with all four components of each element nonzero.
 * <p>
 * Differences are measured by the quaternion norm of each element, all four components. The usual checks,
 * {@link org.ojalgo.structure.Access2D#equals(org.ojalgo.structure.Access2D, org.ojalgo.structure.Access2D, org.ojalgo.type.context.NumberContext)}
 * and the decompositions' own equals methods, compare {@code doubleValue()} – only the real part.
 */
public class QuaternionCase extends MatrixDecompositionTests {

    private static final int[] DIMENSIONS = { 1, 2, 3, 7, 30 };

    private static final int[][] SHAPES = { { 1, 1 }, { 2, 2 }, { 3, 3 }, { 7, 7 }, { 30, 30 }, { 9, 6 }, { 6, 9 }, { 30, 1 }, { 1, 30 } };

    private static final double TOLERANCE = 1E-12;

    /**
     * Elements outside the band, more than lower below or upper above the diagonal, must be zero.
     */
    private static void assertBand(final String message, final MatrixStore<Quaternion> matrix, final int lower, final int upper) {
        double scale = Math.max(1.0, QuaternionCase.frobenius(matrix));
        for (int j = 0; j < matrix.getColDim(); j++) {
            for (int i = 0; i < matrix.getRowDim(); i++) {
                if (i - j > lower || j - i > upper) {
                    double value = matrix.get(i, j).norm();
                    TestUtils.assertTrue(message + ", not zero at (" + i + "," + j + "): " + value, value <= TOLERANCE * scale);
                }
            }
        }
    }

    private static void assertEquals(final String message, final MatrixStore<Quaternion> expected, final MatrixStore<Quaternion> actual) {
        TestUtils.assertEquals(message + ", rows", expected.countRows(), actual.countRows());
        TestUtils.assertEquals(message + ", columns", expected.countColumns(), actual.countColumns());
        QuaternionCase.assertSmall(message, actual.subtract(expected), QuaternionCase.frobenius(expected));
    }

    /**
     * The Frobenius norm of the difference must be small relative to the scale (at least 1).
     */
    private static void assertSmall(final String message, final MatrixStore<Quaternion> difference, final double scale) {
        double error = QuaternionCase.frobenius(difference);
        TestUtils.assertTrue(message + ": " + error + " !<< " + scale, error <= TOLERANCE * Math.max(1.0, scale));
    }

    /**
     * The residual of A X = B relative to |A||X| + |B|, as for any backward stable solver.
     */
    private static void assertSolution(final String message, final MatrixStore<Quaternion> mtrxA, final MatrixStore<Quaternion> mtrxX,
            final MatrixStore<Quaternion> mtrxB) {
        double scale = QuaternionCase.frobenius(mtrxA) * QuaternionCase.frobenius(mtrxX) + QuaternionCase.frobenius(mtrxB);
        QuaternionCase.assertSmall(message, mtrxA.multiply(mtrxX).subtract(mtrxB), scale);
    }

    /**
     * Q<sup>H</sup> Q = I
     */
    private static void assertUnitaryColumns(final String message, final MatrixStore<Quaternion> mtrxQ) {
        int dim = mtrxQ.getColDim();
        QuaternionCase.assertEquals(message + ", not unitary", GenericStore.H256.makeEye(dim, dim), mtrxQ.conjugate().multiply(mtrxQ));
    }

    private static double frobenius(final MatrixStore<Quaternion> matrix) {
        double sum = 0.0;
        for (int j = 0; j < matrix.getColDim(); j++) {
            for (int i = 0; i < matrix.getRowDim(); i++) {
                double value = matrix.get(i, j).norm();
                sum += value * value;
            }
        }
        return Math.sqrt(sum);
    }

    /**
     * A + A<sup>H</sup>, with A random
     */
    private static GenericStore<Quaternion> makeHermitian(final int dim) {
        GenericStore<Quaternion> random = QuaternionCase.makeRandom(dim, dim);
        return random.add(random.conjugate()).collect(GenericStore.H256);
    }

    /**
     * A<sup>H</sup> A + D, with A random and D a real diagonal that dominates. D increasing along the
     * diagonal makes LDL pivot, decreasing does not.
     */
    private static GenericStore<Quaternion> makePositiveDefinite(final int dim, final boolean increasing) {
        GenericStore<Quaternion> random = QuaternionCase.makeRandom(dim, dim);
        GenericStore<Quaternion> diagonal = GenericStore.H256.make(dim, dim);
        for (int i = 0; i < dim; i++) {
            diagonal.set(i, i, Quaternion.valueOf(10.0 * (increasing ? i + 1 : dim - i)));
        }
        return random.conjugate().multiply(random).add(diagonal).collect(GenericStore.H256);
    }

    private static GenericStore<Quaternion> makeRandom(final int nbRows, final int nbCols) {
        Random random = new Random(31L * nbRows + nbCols);
        GenericStore<Quaternion> retVal = GenericStore.H256.make(nbRows, nbCols);
        for (int j = 0; j < nbCols; j++) {
            for (int i = 0; i < nbRows; i++) {
                retVal.set(i, j, Quaternion.of(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5));
            }
        }
        return retVal;
    }

    @Test
    public void testBidiagonal() {
        for (int[] shape : SHAPES) {
            String message = "Bidiagonal " + shape[0] + "x" + shape[1];
            GenericStore<Quaternion> mtrxA = QuaternionCase.makeRandom(shape[0], shape[1]);
            Bidiagonal<Quaternion> decomposition = Bidiagonal.H256.make(mtrxA);
            TestUtils.assertTrue(message, decomposition.decompose(mtrxA));
            QuaternionCase.assertEquals(message, mtrxA, decomposition.reconstruct());
            QuaternionCase.assertUnitaryColumns(message + ", LQ", decomposition.getLQ());
            QuaternionCase.assertUnitaryColumns(message + ", RQ", decomposition.getRQ());
            if (decomposition.isUpper()) {
                QuaternionCase.assertBand(message + ", D", decomposition.getD(), 0, 1);
            } else {
                QuaternionCase.assertBand(message + ", D", decomposition.getD(), 1, 0);
            }
        }
    }

    @Test
    public void testCholesky() {
        for (int dim : DIMENSIONS) {
            String message = "Cholesky " + dim;
            GenericStore<Quaternion> mtrxA = QuaternionCase.makePositiveDefinite(dim, false);
            Cholesky<Quaternion> decomposition = Cholesky.H256.make(mtrxA);
            TestUtils.assertTrue(message, decomposition.decompose(mtrxA));
            TestUtils.assertTrue(message + ", not SPD", decomposition.isSPD());
            QuaternionCase.assertEquals(message, mtrxA, decomposition.reconstruct());
            QuaternionCase.assertBand(message + ", L", decomposition.getL(), dim, 0);
            GenericStore<Quaternion> mtrxB = QuaternionCase.makeRandom(dim, 2);
            QuaternionCase.assertSolution(message + ", solution", mtrxA, decomposition.getSolution(mtrxB), mtrxB);
            QuaternionCase.assertSolution(message + ", inverse", mtrxA, decomposition.getInverse(), GenericStore.H256.makeEye(dim, dim));
        }
    }

    /**
     * Hermitian matrices only
     */
    @Test
    public void testEigenvalue() {
        for (int dim : DIMENSIONS) {
            String message = "Eigenvalue " + dim;
            GenericStore<Quaternion> mtrxA = QuaternionCase.makeHermitian(dim);
            Eigenvalue<Quaternion> decomposition = Eigenvalue.H256.make(mtrxA, true);
            TestUtils.assertTrue(message, decomposition.decompose(mtrxA));
            MatrixStore<Quaternion> mtrxV = decomposition.getV();
            MatrixStore<Quaternion> mtrxD = decomposition.getD();
            QuaternionCase.assertEquals(message + ", AV = VD", mtrxA.multiply(mtrxV), mtrxV.multiply(mtrxD));
            QuaternionCase.assertEquals(message, mtrxA, decomposition.reconstruct());
            QuaternionCase.assertUnitaryColumns(message + ", V", mtrxV);
            QuaternionCase.assertBand(message + ", D", mtrxD, 0, 0);
        }
    }

    @Test
    public void testHessenberg() {
        for (int dim : DIMENSIONS) {
            String message = "Hessenberg " + dim;
            GenericStore<Quaternion> mtrxA = QuaternionCase.makeRandom(dim, dim);
            Hessenberg<Quaternion> decomposition = Hessenberg.H256.make(mtrxA);
            TestUtils.assertTrue(message, decomposition.decompose(mtrxA));
            QuaternionCase.assertEquals(message, mtrxA, decomposition.reconstruct());
            QuaternionCase.assertUnitaryColumns(message + ", Q", decomposition.getQ());
            if (decomposition.isUpper()) {
                QuaternionCase.assertBand(message + ", H", decomposition.getH(), 1, dim);
            } else {
                QuaternionCase.assertBand(message + ", H", decomposition.getH(), dim, 1);
            }
        }
    }

    /**
     * With and without pivoting
     */
    @Test
    public void testLDL() {
        for (boolean pivoting : new boolean[] { false, true }) {
            for (int dim : DIMENSIONS) {
                String message = "LDL " + dim + (pivoting ? " pivoting" : "");
                GenericStore<Quaternion> mtrxA = QuaternionCase.makePositiveDefinite(dim, pivoting);
                LDL<Quaternion> decomposition = LDL.H256.make(mtrxA);
                TestUtils.assertTrue(message, decomposition.decompose(mtrxA));
                if (dim > 1) {
                    TestUtils.assertEquals(message + ", pivoted", pivoting, decomposition.isPivoted());
                }
                QuaternionCase.assertEquals(message, mtrxA, decomposition.reconstruct());
                QuaternionCase.assertBand(message + ", L", decomposition.getL(), dim, 0);
                QuaternionCase.assertBand(message + ", D", decomposition.getD(), 0, 0);
                GenericStore<Quaternion> mtrxB = QuaternionCase.makeRandom(dim, 2);
                QuaternionCase.assertSolution(message + ", solution", mtrxA, decomposition.getSolution(mtrxB), mtrxB);
                QuaternionCase.assertSolution(message + ", inverse", mtrxA, decomposition.getInverse(), GenericStore.H256.makeEye(dim, dim));
            }
        }
    }

    @Test
    public void testLU() {
        for (int[] shape : SHAPES) {
            String message = "LU " + shape[0] + "x" + shape[1];
            GenericStore<Quaternion> mtrxA = QuaternionCase.makeRandom(shape[0], shape[1]);
            LU<Quaternion> decomposition = LU.H256.make(mtrxA);
            TestUtils.assertTrue(message, decomposition.decompose(mtrxA));
            QuaternionCase.assertEquals(message, mtrxA, decomposition.reconstruct());
            QuaternionCase.assertBand(message + ", L", decomposition.getL(), shape[0], 0);
            QuaternionCase.assertBand(message + ", U", decomposition.getU(), 0, shape[1]);
            if (shape[0] == shape[1]) {
                GenericStore<Quaternion> mtrxB = QuaternionCase.makeRandom(shape[0], 2);
                QuaternionCase.assertSolution(message + ", solution", mtrxA, decomposition.getSolution(mtrxB), mtrxB);
                QuaternionCase.assertSolution(message + ", inverse", mtrxA, decomposition.getInverse(), GenericStore.H256.makeEye(shape[0], shape[0]));
            }
        }
    }

    /**
     * The least squares solution, when there are at least as many rows as columns, satisfies the normal
     * equations A<sup>H</sup> (A X - B) = 0.
     */
    @Test
    public void testQR() {
        for (int[] shape : SHAPES) {
            String message = "QR " + shape[0] + "x" + shape[1];
            GenericStore<Quaternion> mtrxA = QuaternionCase.makeRandom(shape[0], shape[1]);
            QR<Quaternion> decomposition = QR.H256.make(mtrxA);
            TestUtils.assertTrue(message, decomposition.decompose(mtrxA));
            QuaternionCase.assertEquals(message, mtrxA, decomposition.reconstruct());
            QuaternionCase.assertUnitaryColumns(message + ", Q", decomposition.getQ());
            QuaternionCase.assertBand(message + ", R", decomposition.getR(), 0, shape[1]);
            if (shape[0] >= shape[1]) {
                GenericStore<Quaternion> mtrxB = QuaternionCase.makeRandom(shape[0], 2);
                MatrixStore<Quaternion> mtrxX = decomposition.getSolution(mtrxB);
                MatrixStore<Quaternion> residual = mtrxA.multiply(mtrxX).subtract(mtrxB);
                double normA = QuaternionCase.frobenius(mtrxA);
                double scale = normA * (normA * QuaternionCase.frobenius(mtrxX) + QuaternionCase.frobenius(mtrxB));
                QuaternionCase.assertSmall(message + ", normal equations", mtrxA.conjugate().multiply(residual), scale);
            }
        }
    }

    /**
     * Also the pseudoinverse: A A<sup>+</sup> A = A
     */
    @Test
    public void testSingularValue() {
        for (int[] shape : SHAPES) {
            String message = "SingularValue " + shape[0] + "x" + shape[1];
            GenericStore<Quaternion> mtrxA = QuaternionCase.makeRandom(shape[0], shape[1]);
            SingularValue<Quaternion> decomposition = SingularValue.H256.make(mtrxA);
            TestUtils.assertTrue(message, decomposition.decompose(mtrxA));
            QuaternionCase.assertEquals(message, mtrxA, decomposition.reconstruct());
            QuaternionCase.assertUnitaryColumns(message + ", U", decomposition.getU());
            QuaternionCase.assertUnitaryColumns(message + ", V", decomposition.getV());
            QuaternionCase.assertBand(message + ", S", decomposition.getS(), 0, 0);
            Array1D<Double> values = decomposition.getSingularValues();
            for (int i = 0; i < values.size(); i++) {
                TestUtils.assertTrue(message + ", negative singular value", values.doubleValue(i) >= 0.0);
                TestUtils.assertTrue(message + ", not ordered", i == 0 || values.doubleValue(i) <= values.doubleValue(i - 1));
            }
            QuaternionCase.assertEquals(message + ", pseudoinverse", mtrxA, mtrxA.multiply(decomposition.getInverse()).multiply(mtrxA));
        }
    }

    /**
     * Hermitian matrices only
     */
    @Test
    public void testTridiagonal() {
        for (int dim : DIMENSIONS) {
            String message = "Tridiagonal " + dim;
            GenericStore<Quaternion> mtrxA = QuaternionCase.makeHermitian(dim);
            Tridiagonal<Quaternion> decomposition = Tridiagonal.H256.make(mtrxA);
            TestUtils.assertTrue(message, decomposition.decompose(mtrxA));
            QuaternionCase.assertEquals(message, mtrxA, decomposition.reconstruct());
            QuaternionCase.assertUnitaryColumns(message + ", Q", decomposition.getQ());
            QuaternionCase.assertBand(message + ", D", decomposition.getD(), 1, 1);
        }
    }

}
