package org.ojalgo.matrix.decomposition;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.DensityTrackingArray;
import org.ojalgo.matrix.store.ColumnsSupplier;
import org.ojalgo.matrix.store.R064Store;

/**
 * Tests for the Markowitz LU factorization and solve routines.
 */
class MarkowitzLUTest {

    private static final double TOL = 1e-10;

    static ColumnsSupplier<Double> makeMatrix(final int dim) {
        ColumnsSupplier<Double> m = R064Store.FACTORY.makeColumnsSupplier(dim);
        m.addColumns(dim);
        return m;
    }

    static ColumnsSupplier<Double> makeIdentity(final int dim) {
        ColumnsSupplier<Double> m = MarkowitzLUTest.makeMatrix(dim);
        for (int i = 0; i < dim; i++) {
            m.set(i, i, 1.0);
        }
        return m;
    }

    static double dot(final double[] a, final double[] b) {
        double s = 0;
        for (int i = 0; i < a.length; i++) {
            s += a[i] * b[i];
        }
        return s;
    }

    static double[] matvec(final ColumnsSupplier<Double> m, final double[] x) {
        int n = m.getRowDim();
        double[] b = new double[n];
        for (int j = 0; j < n; j++) {
            double xj = x[j];
            if (xj == 0) {
                continue;
            }
            m.getColumn(j).nonzeros().forEach(nz -> {
                b[(int) nz.index()] += nz.doubleValue() * xj;
            });
        }
        return b;
    }

    static double[] matvecT(final ColumnsSupplier<Double> m, final double[] y) {
        int n = m.getRowDim();
        double[] result = new double[n];
        for (int j = 0; j < n; j++) {
            final int col = j;
            m.getColumn(col).nonzeros().forEach(nz -> {
                result[col] += nz.doubleValue() * y[(int) nz.index()];
            });
        }
        return result;
    }

    @Test
    void btranNonSymmetric() {
        // A = [[1, 2], [3, 4]]   A^T = [[1, 3], [2, 4]]
        ColumnsSupplier<Double> m = MarkowitzLUTest.makeMatrix(2);
        m.set(0, 0, 1);
        m.set(0, 1, 2);
        m.set(1, 0, 3);
        m.set(1, 1, 4);

        MarkowitzLU lu = new MarkowitzLU(2);
        lu.build(m);

        // x = [1, 2], A^T x = [1*1+3*2, 2*1+4*2] = [7, 10]
        DensityTrackingArray rhs = DensityTrackingArray.wrap(new double[] { 7, 10 });
        lu.btran(rhs);
        double[] y = rhs.toRawCopy1D();
        TestUtils.assertEquals(1.0, y[0], TOL);
        TestUtils.assertEquals(2.0, y[1], TOL);
    }

    @Test
    void btranSolve() {
        // A = [[2, 1], [1, 3]]
        ColumnsSupplier<Double> m = MarkowitzLUTest.makeMatrix(2);
        m.set(0, 0, 2);
        m.set(0, 1, 1);
        m.set(1, 0, 1);
        m.set(1, 1, 3);

        MarkowitzLU lu = new MarkowitzLU(2);
        lu.build(m);

        // Solve A^T y = [5, 10]
        // A^T = [[2, 1], [1, 3]] (symmetric in this case)
        DensityTrackingArray rhs = DensityTrackingArray.wrap(new double[] { 5, 10 });
        lu.btran(rhs);
        TestUtils.assertArrayEquals(new double[] { 1, 3 }, rhs.toRawCopy1D(), TOL);
    }

    @Test
    void diagonalMatrix() {
        // Pure diagonal — all column singletons
        ColumnsSupplier<Double> m = MarkowitzLUTest.makeMatrix(4);
        m.set(0, 0, 3);
        m.set(1, 1, 7);
        m.set(2, 2, 2);
        m.set(3, 3, 5);

        MarkowitzLU lu = new MarkowitzLU(4);
        lu.build(m);

        double[] expected = { 1, 2, 3, 4 };
        double[] b = { 3, 14, 6, 20 };
        final double[] dense = b;
        DensityTrackingArray rhs = DensityTrackingArray.wrap(dense);
        lu.ftran(rhs);
        TestUtils.assertArrayEquals(expected, rhs.toRawCopy1D(), TOL);
    }

    @Test
    void ftranBtranConsistency() {
        // For any non-singular A and vectors b, c:
        //   c^T (A^{-1} b) = (A^{-T} c)^T b
        ColumnsSupplier<Double> m = MarkowitzLUTest.makeMatrix(3);
        m.set(0, 0, 3);
        m.set(0, 1, 1);
        m.set(0, 2, 2);
        m.set(1, 0, 1);
        m.set(1, 1, 4);
        m.set(1, 2, 1);
        m.set(2, 0, 2);
        m.set(2, 1, 1);
        m.set(2, 2, 5);

        MarkowitzLU lu = new MarkowitzLU(3);
        lu.build(m);

        double[] b = { 1, 2, 3 };
        double[] c = { 4, 5, 6 };

        DensityTrackingArray rhsF = DensityTrackingArray.wrap(b.clone());
        lu.ftran(rhsF); // x = A^{-1} b
        double[] x = rhsF.toRawCopy1D();

        DensityTrackingArray rhsB = DensityTrackingArray.wrap(c.clone());
        lu.btran(rhsB); // y = A^{-T} c
        double[] y = rhsB.toRawCopy1D();

        // c^T x should equal y^T b
        double cTx = MarkowitzLUTest.dot(c, x);
        double yTb = MarkowitzLUTest.dot(y, b);
        TestUtils.assertEquals("FTRAN/BTRAN consistency", cTx, yTb, TOL);
    }

    @Test
    void ftranBtranConsistencyLarger() {
        // 8x8 non-symmetric matrix
        ColumnsSupplier<Double> m = MarkowitzLUTest.makeMatrix(8);
        double[][] dense = { { 5, 1, 0, 0, 2, 0, 0, 0 }, { 1, 6, 1, 0, 0, 0, 0, 0 }, { 0, 2, 7, 1, 0, 0, 0, 0 }, { 0, 0, 1, 8, 0, 0, 1, 0 },
                { 3, 0, 0, 0, 9, 1, 0, 0 }, { 0, 0, 0, 0, 2, 10, 1, 0 }, { 0, 0, 0, 1, 0, 1, 11, 2 }, { 0, 0, 0, 0, 0, 0, 3, 12 } };
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (dense[i][j] != 0) {
                    m.set(i, j, dense[i][j]);
                }
            }
        }

        MarkowitzLU lu = new MarkowitzLU(8);
        lu.build(m);

        double[] b = { 1, 2, 3, 4, 5, 6, 7, 8 };
        double[] c = { 8, 7, 6, 5, 4, 3, 2, 1 };

        DensityTrackingArray rhsF = DensityTrackingArray.wrap(b.clone());
        lu.ftran(rhsF);
        double[] x = rhsF.toRawCopy1D();

        DensityTrackingArray rhsB = DensityTrackingArray.wrap(c.clone());
        lu.btran(rhsB);
        double[] y = rhsB.toRawCopy1D();

        TestUtils.assertEquals("FTRAN/BTRAN consistency 8x8", MarkowitzLUTest.dot(c, x), MarkowitzLUTest.dot(y, b), TOL);
    }

    @Test
    void identityMatrix() {
        ColumnsSupplier<Double> m = MarkowitzLUTest.makeIdentity(3);
        MarkowitzLU lu = new MarkowitzLU(3);
        lu.build(m);

        DensityTrackingArray rhs = DensityTrackingArray.wrap(new double[] { 1, 2, 3 });
        lu.ftran(rhs);
        TestUtils.assertArrayEquals(new double[] { 1, 2, 3 }, rhs.toRawCopy1D(), TOL);
    }

    @Test
    void largerSparse10x10() {
        // 10x10 sparse matrix with varied structure
        ColumnsSupplier<Double> m = MarkowitzLUTest.makeMatrix(10);
        // Diagonal-dominant sparse structure
        for (int i = 0; i < 10; i++) {
            m.set(i, i, 10.0 + i);
        }
        // Some off-diagonal entries
        m.set(0, 1, 1);
        m.set(1, 0, 2);
        m.set(2, 3, 1);
        m.set(3, 2, 1);
        m.set(4, 5, 3);
        m.set(5, 4, 2);
        m.set(6, 7, 1);
        m.set(7, 8, 1);
        m.set(8, 9, 2);
        m.set(9, 6, 1);

        MarkowitzLU lu = new MarkowitzLU(10);
        lu.build(m);

        double[] expected = new double[10];
        for (int i = 0; i < 10; i++) {
            expected[i] = i + 1;
        }
        double[] b = MarkowitzLUTest.matvec(m, expected);
        final double[] dense = b;

        DensityTrackingArray rhs = DensityTrackingArray.wrap(dense);
        lu.ftran(rhs);
        for (int i = 0; i < 10; i++) {
            TestUtils.assertEquals("x[" + i + "]", expected[i], rhs.toRawCopy1D()[i], TOL);
        }

        // Also test BTRAN
        DensityTrackingArray rhsB = DensityTrackingArray.wrap(b.clone());
        lu.btran(rhsB);
        // Verify: (A^{-T} b)^T A = b^T  =>  A^T (A^{-T} b) = b
        double[] y = rhsB.toRawCopy1D();
        double[] Aty = MarkowitzLUTest.matvecT(m, y);
        for (int i = 0; i < 10; i++) {
            TestUtils.assertEquals("A^T y[" + i + "]", b[i], Aty[i], TOL);
        }
    }

    @Test
    void lpStyleBasis() {
        // Simulates an LP basis: identity-like block + structural columns
        // B = [[1,0,3,0],[0,1,0,2],[0,0,5,0],[0,0,0,4]]
        // Columns 0,1 are logicals (singletons), columns 2,3 are structural
        ColumnsSupplier<Double> m = MarkowitzLUTest.makeMatrix(4);
        m.set(0, 0, 1);
        m.set(1, 1, 1);
        m.set(0, 2, 3);
        m.set(2, 2, 5);
        m.set(1, 3, 2);
        m.set(3, 3, 4);

        MarkowitzLU lu = new MarkowitzLU(4);
        lu.build(m);

        double[] expected = { 1, 1, 1, 1 };
        double[] b = MarkowitzLUTest.matvec(m, expected);
        final double[] dense = b;
        DensityTrackingArray rhs = DensityTrackingArray.wrap(dense);
        lu.ftran(rhs);
        for (int i = 0; i < 4; i++) {
            final int i1 = i;
            TestUtils.assertEquals("x[" + i + "]", expected[i], rhs.doubleValue(i1), TOL);
        }
    }

    @Test
    void permutedDiagonal() {
        // Off-diagonal singletons (each row and column has exactly one entry)
        ColumnsSupplier<Double> m = MarkowitzLUTest.makeMatrix(3);
        m.set(0, 2, 4);
        m.set(1, 0, 2);
        m.set(2, 1, 5);

        MarkowitzLU lu = new MarkowitzLU(3);
        lu.build(m);

        // A = [[0,0,4],[2,0,0],[0,5,0]]; x = [1,2,3]; b = [12,2,10]
        double[] b = { 12, 2, 10 };
        final double[] dense = b;
        DensityTrackingArray rhs = DensityTrackingArray.wrap(dense);
        lu.ftran(rhs);
        double[] x = rhs.toRawCopy1D();
        TestUtils.assertEquals(1.0, x[0], TOL);
        TestUtils.assertEquals(2.0, x[1], TOL);
        TestUtils.assertEquals(3.0, x[2], TOL);
    }

    @Test
    void simpleDense2x2() {
        // A = [[2, 1], [1, 3]]
        ColumnsSupplier<Double> m = MarkowitzLUTest.makeMatrix(2);
        m.set(0, 0, 2);
        m.set(0, 1, 1);
        m.set(1, 0, 1);
        m.set(1, 1, 3);

        MarkowitzLU lu = new MarkowitzLU(2);
        lu.build(m);

        // Solve A x = [5, 10]  =>  x = [1, 3]
        DensityTrackingArray rhs = DensityTrackingArray.wrap(new double[] { 5, 10 });
        lu.ftran(rhs);
        TestUtils.assertArrayEquals(new double[] { 1, 3 }, rhs.toRawCopy1D(), TOL);
    }

    @Test
    void simpleDense3x3() {
        // A = [[2, 1, 0], [1, 3, 1], [0, 1, 2]]
        ColumnsSupplier<Double> m = MarkowitzLUTest.makeMatrix(3);
        m.set(0, 0, 2);
        m.set(0, 1, 1);
        m.set(1, 0, 1);
        m.set(1, 1, 3);
        m.set(1, 2, 1);
        m.set(2, 1, 1);
        m.set(2, 2, 2);

        MarkowitzLU lu = new MarkowitzLU(3);
        lu.build(m);

        // Solve A x = b where x = [1, 2, 3] => b = A*x = [4, 10, 8]
        DensityTrackingArray rhs = DensityTrackingArray.wrap(new double[] { 4, 10, 8 });
        lu.ftran(rhs);
        double[] x = rhs.toRawCopy1D();
        TestUtils.assertEquals(1.0, x[0], TOL);
        TestUtils.assertEquals(2.0, x[1], TOL);
        TestUtils.assertEquals(3.0, x[2], TOL);
    }

    @Test
    void sparseMatrix5x5() {
        // Sparse 5x5 with known structure
        ColumnsSupplier<Double> m = MarkowitzLUTest.makeMatrix(5);
        m.set(0, 0, 4);
        m.set(0, 2, 1);
        m.set(1, 1, 5);
        m.set(1, 3, 2);
        m.set(2, 0, 1);
        m.set(2, 2, 6);
        m.set(3, 1, 2);
        m.set(3, 3, 7);
        m.set(3, 4, 1);
        m.set(4, 4, 3);

        MarkowitzLU lu = new MarkowitzLU(5);
        lu.build(m);

        // Compute b = A * [1,1,1,1,1]
        double[] expected = { 1, 1, 1, 1, 1 };
        double[] b = MarkowitzLUTest.matvec(m, expected);
        final double[] dense = b;

        DensityTrackingArray rhs = DensityTrackingArray.wrap(dense);
        lu.ftran(rhs);
        double[] x = rhs.toRawCopy1D();
        for (int i = 0; i < 5; i++) {
            TestUtils.assertEquals("x[" + i + "]", expected[i], x[i], TOL);
        }
    }
}
