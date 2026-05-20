package org.ojalgo.matrix.decomposition;

import org.ojalgo.TestUtils;
import org.ojalgo.array.DensityTrackingArray;
import org.ojalgo.matrix.store.ColumnsSupplier;
import org.ojalgo.matrix.store.R064Store;
import org.junit.jupiter.api.Test;

/**
 * Tests for Forest-Tomlin updates on top of the Markowitz LU factorization.
 */
class ForestTomlinFactorTest {

    private static final double TOL = 1e-10;

    private static double dot(final double[] a, final double[] b) {
        double s = 0;
        for (int i = 0; i < a.length; i++) {
            s += a[i] * b[i];
        }
        return s;
    }

    private static ColumnsSupplier<Double> makeMatrix(final int dim) {
        ColumnsSupplier<Double> m = R064Store.FACTORY.makeColumnsSupplier(dim);
        m.addColumns(dim);
        return m;
    }

    private static ColumnsSupplier<Double> makeIdentity(final int dim) {
        ColumnsSupplier<Double> m = ForestTomlinFactorTest.makeMatrix(dim);
        for (int i = 0; i < dim; i++) {
            m.set(i, i, 1.0);
        }
        return m;
    }

    private static double[] matvec(final ColumnsSupplier<Double> m, final double[] x) {
        int n = m.getRowDim();
        double[] b = new double[n];
        for (int j = 0; j < n; j++) {
            double xj = x[j];
            if (xj == 0) {
                continue;
            }
            m.getColumn(j).nonzeros().forEach(nz -> b[(int) nz.index()] += nz.doubleValue() * xj);
        }
        return b;
    }

    private void applyColumnUpdate(final ForestTomlinFactor ft, final int pivotCol, final double[] newCol) {
        int pivotRow = pivotCol;

        DensityTrackingArray aq = DensityTrackingArray.wrap(newCol.clone());
        ft.ftran(aq);

        DensityTrackingArray ep = DensityTrackingArray.unit(ft.dimension(), pivotRow);
        ft.btran(ep);

        ft.update(pivotRow, aq, ep);
    }

    @Test
    void btranAfterUpdate() {
        ColumnsSupplier<Double> basis = ForestTomlinFactorTest.makeMatrix(2);
        basis.set(0, 0, 3);
        basis.set(0, 1, 1);
        basis.set(1, 0, 1);
        basis.set(1, 1, 4);

        ForestTomlinFactor ft = new ForestTomlinFactor(2);
        ft.build(basis);

        // Replace column 0 with [5, 2]
        // B' = [[5, 1], [2, 4]]
        double[] aqDense = { 5, 2 };
        DensityTrackingArray aq = DensityTrackingArray.wrap(aqDense.clone());
        ft.ftran(aq);

        int pivotRow = 0;
        DensityTrackingArray ep = DensityTrackingArray.unit(2, pivotRow);
        ft.btran(ep);

        ft.update(pivotRow, aq, ep);

        // Verify BTRAN: solve B'^T y = c
        // B'^T = [[5, 2], [1, 4]]
        // y = [1, 2], B'^T y = [9, 9]
        DensityTrackingArray rhs = DensityTrackingArray.wrap(new double[] { 9, 9 });
        ft.btran(rhs);
        double[] y = rhs.toRawCopy1D();
        TestUtils.assertEquals("y[0]", 1.0, y[0], TOL);
        TestUtils.assertEquals("y[1]", 2.0, y[1], TOL);
    }

    @Test
    void ftranBtranConsistencyAfterUpdate() {
        ColumnsSupplier<Double> basis = ForestTomlinFactorTest.makeMatrix(3);
        basis.set(0, 0, 3);
        basis.set(0, 1, 1);
        basis.set(0, 2, 2);
        basis.set(1, 0, 1);
        basis.set(1, 1, 4);
        basis.set(1, 2, 1);
        basis.set(2, 0, 2);
        basis.set(2, 1, 1);
        basis.set(2, 2, 5);

        ForestTomlinFactor ft = new ForestTomlinFactor(3);
        ft.build(basis);

        // Replace column 1 with [1, 2, 1]
        this.applyColumnUpdate(ft, 1, new double[] { 1, 2, 1 });

        double[] b = { 1, 2, 3 };
        double[] c = { 4, 5, 6 };

        DensityTrackingArray rhsF = DensityTrackingArray.wrap(b.clone());
        ft.ftran(rhsF);
        double[] x = rhsF.toRawCopy1D();

        DensityTrackingArray rhsB = DensityTrackingArray.wrap(c.clone());
        ft.btran(rhsB);
        double[] y = rhsB.toRawCopy1D();

        double cTx = ForestTomlinFactorTest.dot(c, x);
        double yTb = ForestTomlinFactorTest.dot(y, b);
        TestUtils.assertEquals("FTRAN/BTRAN consistency after update", cTx, yTb, TOL);
    }

    @Test
    void ftranBtranConsistencyMultipleUpdates() {
        // c^T (B^{-1} b) = (B^{-T} c)^T b after several updates
        ColumnsSupplier<Double> basis = ForestTomlinFactorTest.makeMatrix(4);
        basis.set(0, 0, 5);
        basis.set(0, 1, 1);
        basis.set(1, 0, 1);
        basis.set(1, 1, 6);
        basis.set(1, 2, 1);
        basis.set(2, 1, 1);
        basis.set(2, 2, 7);
        basis.set(2, 3, 1);
        basis.set(3, 2, 1);
        basis.set(3, 3, 8);

        ForestTomlinFactor ft = new ForestTomlinFactor(4);
        ft.build(basis);

        this.applyColumnUpdate(ft, 0, new double[] { 4, 2, 0, 0 });
        this.applyColumnUpdate(ft, 3, new double[] { 0, 0, 3, 9 });

        double[] b = { 1, 2, 3, 4 };
        double[] c = { 5, 6, 7, 8 };

        DensityTrackingArray rhsF = DensityTrackingArray.wrap(b.clone());
        ft.ftran(rhsF);
        double[] x = rhsF.toRawCopy1D();

        DensityTrackingArray rhsB = DensityTrackingArray.wrap(c.clone());
        ft.btran(rhsB);
        double[] y = rhsB.toRawCopy1D();

        TestUtils.assertEquals("FTRAN/BTRAN consistency after 2 updates on 4x4", ForestTomlinFactorTest.dot(c, x), ForestTomlinFactorTest.dot(y, b), TOL);
    }


    @Test
    void manyUpdatesOnLargerBasis() {
        // 6x6 basis with 5 column replacements
        ColumnsSupplier<Double> basis = ForestTomlinFactorTest.makeIdentity(6);
        ForestTomlinFactor ft = new ForestTomlinFactor(6);
        ft.build(basis);

        // Build up a non-trivial basis through sequential updates
        this.applyColumnUpdate(ft, 0, new double[] { 3, 1, 0, 0, 0, 0 });
        this.applyColumnUpdate(ft, 1, new double[] { 1, 4, 1, 0, 0, 0 });
        this.applyColumnUpdate(ft, 2, new double[] { 0, 1, 5, 1, 0, 0 });
        this.applyColumnUpdate(ft, 3, new double[] { 0, 0, 1, 6, 1, 0 });
        this.applyColumnUpdate(ft, 4, new double[] { 0, 0, 0, 1, 7, 1 });

        TestUtils.assertEquals(5, ft.updateCount());

        // Final basis:
        // [[3,1,0,0,0,0],[1,4,1,0,0,0],[0,1,5,1,0,0],
        //  [0,0,1,6,1,0],[0,0,0,1,7,1],[0,0,0,0,0,1]]
        // (tridiagonal above with last column = e_5)

        double[] b = { 1, 2, 3, 4, 5, 6 };
        double[] c = { 6, 5, 4, 3, 2, 1 };

        DensityTrackingArray rhsF = DensityTrackingArray.wrap(b.clone());
        ft.ftran(rhsF);
        double[] x = rhsF.toRawCopy1D();

        DensityTrackingArray rhsB = DensityTrackingArray.wrap(c.clone());
        ft.btran(rhsB);
        double[] y = rhsB.toRawCopy1D();

        TestUtils.assertEquals("FTRAN/BTRAN consistency after 5 updates on 6x6", ForestTomlinFactorTest.dot(c, x), ForestTomlinFactorTest.dot(y, b), TOL);
    }

    @Test
    void multipleUpdates() {
        // Start with 3x3 identity, apply two column replacements.
        ColumnsSupplier<Double> basis = ForestTomlinFactorTest.makeIdentity(3);
        ForestTomlinFactor ft = new ForestTomlinFactor(3);
        ft.build(basis);

        // Update 1: replace column 0 with [2, 1, 0]
        this.applyColumnUpdate(ft, 0, new double[] { 2, 1, 0 });

        // Update 2: replace column 2 with [0, 1, 3]
        this.applyColumnUpdate(ft, 2, new double[] { 0, 1, 3 });

        TestUtils.assertEquals(2, ft.updateCount());

        // Current basis: [[2, 0, 0], [1, 1, 1], [0, 0, 3]]
        // x = [1, 2, 3], b = [2, 6, 9]
        DensityTrackingArray rhs = DensityTrackingArray.wrap(new double[] { 2, 6, 9 });
        ft.ftran(rhs);
        double[] x = rhs.toRawCopy1D();
        TestUtils.assertEquals(1.0, x[0], TOL);
        TestUtils.assertEquals(2.0, x[1], TOL);
        TestUtils.assertEquals(3.0, x[2], TOL);
    }

    @Test
    void nonTrivialBasisColumnReplacement() {
        // Start with a non-trivial basis, replace a column, and verify.
        ColumnsSupplier<Double> basis = ForestTomlinFactorTest.makeMatrix(3);
        basis.set(0, 0, 4);
        basis.set(0, 1, 1);
        basis.set(1, 0, 2);
        basis.set(1, 1, 5);
        basis.set(1, 2, 1);
        basis.set(2, 1, 1);
        basis.set(2, 2, 3);

        ForestTomlinFactor ft = new ForestTomlinFactor(3);
        ft.build(basis);

        // Replace column 2 with [1, 3, 6] (non-diagonal position)
        this.applyColumnUpdate(ft, 2, new double[] { 1, 3, 6 });

        // B' = [[4, 1, 1], [2, 5, 3], [0, 1, 6]]
        // x = [1, 1, 1], b = [6, 10, 7]
        double[] b = { 6, 10, 7 };
        DensityTrackingArray rhs = DensityTrackingArray.wrap(b.clone());
        ft.ftran(rhs);
        double[] x = rhs.toRawCopy1D();
        TestUtils.assertEquals("x[0]", 1.0, x[0], TOL);
        TestUtils.assertEquals("x[1]", 1.0, x[1], TOL);
        TestUtils.assertEquals("x[2]", 1.0, x[2], TOL);

        // Verify FTRAN/BTRAN consistency
        double[] c = { 2, 3, 4 };
        DensityTrackingArray rhsF = DensityTrackingArray.wrap(b.clone());
        ft.ftran(rhsF);
        double[] xf = rhsF.toRawCopy1D();

        DensityTrackingArray rhsB = DensityTrackingArray.wrap(c.clone());
        ft.btran(rhsB);
        double[] yb = rhsB.toRawCopy1D();

        TestUtils.assertEquals("FTRAN/BTRAN consistency", ForestTomlinFactorTest.dot(c, xf), ForestTomlinFactorTest.dot(yb, b), TOL);
    }

    @Test
    void refactorResetsThenSolves() {
        ColumnsSupplier<Double> basis = ForestTomlinFactorTest.makeMatrix(3);
        basis.set(0, 0, 2);
        basis.set(0, 1, 1);
        basis.set(1, 0, 1);
        basis.set(1, 1, 3);
        basis.set(1, 2, 1);
        basis.set(2, 1, 1);
        basis.set(2, 2, 2);

        ForestTomlinFactor ft = new ForestTomlinFactor(3);
        ft.build(basis);

        // Apply an update
        this.applyColumnUpdate(ft, 0, new double[] { 4, 1, 0 });
        TestUtils.assertEquals(1, ft.updateCount());

        // Refactor with original basis — should reset updates
        ft.refactor(basis);
        TestUtils.assertEquals(0, ft.updateCount());

        // Verify FTRAN still works on the original basis
        double[] expected = { 1, 2, 3 };
        double[] b = ForestTomlinFactorTest.matvec(basis, expected);
        final double[] dense = b;
        DensityTrackingArray rhs = DensityTrackingArray.wrap(dense);
        ft.ftran(rhs);
        TestUtils.assertArrayEquals(expected, rhs.toRawCopy1D(), TOL);
    }

    @Test
    void shouldRefactorFlag() {
        ColumnsSupplier<Double> basis = ForestTomlinFactorTest.makeIdentity(3);
        ForestTomlinFactor ft = new ForestTomlinFactor(3);
        ft.build(basis);

        TestUtils.assertFalse(ft.shouldRefactor());
    }

    @Test
    void singleForestTomlinUpdate() {
        // Start with basis B, then replace one column and verify solves.
        //
        // B = [[2, 1, 0],
        //      [1, 3, 1],
        //      [0, 1, 2]]
        ColumnsSupplier<Double> basis = ForestTomlinFactorTest.makeMatrix(3);
        basis.set(0, 0, 2);
        basis.set(0, 1, 1);
        basis.set(1, 0, 1);
        basis.set(1, 1, 3);
        basis.set(1, 2, 1);
        basis.set(2, 1, 1);
        basis.set(2, 2, 2);

        ForestTomlinFactor ft = new ForestTomlinFactor(3);
        ft.build(basis);

        // New column to replace column 1: a_q = [0, 2, 5]
        // After update, B' = [[2, 0, 0], [1, 2, 1], [0, 5, 2]]
        double[] aqDense = { 0, 2, 5 };

        // Compute aq_tilde = B^{-1} a_q via FTRAN
        DensityTrackingArray aq = DensityTrackingArray.wrap(aqDense.clone());
        ft.ftran(aq);

        // Compute ep = e_1^T B^{-1} via BTRAN  (pivot row = 1)
        int pivotRow = 1;
        DensityTrackingArray ep = DensityTrackingArray.unit(3, pivotRow);
        ft.btran(ep);

        // Apply the update
        double alpha = ft.update(pivotRow, aq, ep);
        TestUtils.assertTrue(alpha != 0.0);
        TestUtils.assertEquals(1, ft.updateCount());

        // Now verify: solve B' x = b where x = [1, 1, 1]
        // B' = [[2, 0, 0], [1, 2, 1], [0, 5, 2]]
        // b = B' [1,1,1] = [2, 4, 7]
        double[] b = { 2, 4, 7 };
        DensityTrackingArray rhs = DensityTrackingArray.wrap(b.clone());
        ft.ftran(rhs);
        double[] x = rhs.toRawCopy1D();
        TestUtils.assertEquals("x[0]", 1.0, x[0], TOL);
        TestUtils.assertEquals("x[1]", 1.0, x[1], TOL);
        TestUtils.assertEquals("x[2]", 1.0, x[2], TOL);
    }

    @Test
    void solveWithoutUpdates() {
        // Basic FTRAN/BTRAN through ForestTomlinFactor (no updates).
        ColumnsSupplier<Double> m = ForestTomlinFactorTest.makeMatrix(3);
        m.set(0, 0, 2);
        m.set(0, 1, 1);
        m.set(0, 2, 0);
        m.set(1, 0, 1);
        m.set(1, 1, 3);
        m.set(1, 2, 1);
        m.set(2, 0, 0);
        m.set(2, 1, 1);
        m.set(2, 2, 2);

        ForestTomlinFactor ft = new ForestTomlinFactor(3);
        ft.build(m);

        double[] expected = { 1, 2, 3 };
        double[] b = ForestTomlinFactorTest.matvec(m, expected);
        final double[] dense = b;

        DensityTrackingArray rhs = DensityTrackingArray.wrap(dense);
        ft.ftran(rhs);
        TestUtils.assertArrayEquals(expected, rhs.toRawCopy1D(), TOL);
    }

    @Test
    void threeSequentialUpdates() {
        // Start with identity, replace all three columns one by one.
        // Final basis: [[3,1,2],[1,4,1],[2,1,5]]
        ColumnsSupplier<Double> basis = ForestTomlinFactorTest.makeIdentity(3);
        ForestTomlinFactor ft = new ForestTomlinFactor(3);
        ft.build(basis);

        this.applyColumnUpdate(ft, 0, new double[] { 3, 1, 2 });
        this.applyColumnUpdate(ft, 1, new double[] { 1, 4, 1 });
        this.applyColumnUpdate(ft, 2, new double[] { 2, 1, 5 });

        TestUtils.assertEquals(3, ft.updateCount());

        // Solve B x = b where x = [1, 2, 3]
        // B = [[3,1,2],[1,4,1],[2,1,5]], b = [11, 12, 19]
        double[] b = { 3 + 2 + 6, 1 + 8 + 3, 2 + 2 + 15 };
        DensityTrackingArray rhs = DensityTrackingArray.wrap(b.clone());
        ft.ftran(rhs);
        double[] x = rhs.toRawCopy1D();
        TestUtils.assertEquals("x[0]", 1.0, x[0], TOL);
        TestUtils.assertEquals("x[1]", 2.0, x[1], TOL);
        TestUtils.assertEquals("x[2]", 3.0, x[2], TOL);

        // Also verify BTRAN
        DensityTrackingArray rhsB = DensityTrackingArray.wrap(b.clone());
        ft.btran(rhsB);
        double[] y = rhsB.toRawCopy1D();
        // y = B^{-T} b, verify A^T y = b
        double[] Aty = new double[3];
        double[][] Bt = { { 3, 1, 2 }, { 1, 4, 1 }, { 2, 1, 5 } };
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Aty[i] += Bt[j][i] * y[j];
            }
        }
        for (int i = 0; i < 3; i++) {
            TestUtils.assertEquals("A^T y[" + i + "]", b[i], Aty[i], TOL);
        }
    }

    @Test
    void updateWithDenseIncomingColumn() {
        // Incoming column is fully dense
        ColumnsSupplier<Double> basis = ForestTomlinFactorTest.makeIdentity(4);
        ForestTomlinFactor ft = new ForestTomlinFactor(4);
        ft.build(basis);

        // Replace column 1 with [1, 2, 3, 4]
        this.applyColumnUpdate(ft, 1, new double[] { 1, 2, 3, 4 });

        // Basis: [[1,1,0,0],[0,2,0,0],[0,3,1,0],[0,4,0,1]]
        // x = [1,1,1,1], b = [2, 2, 4, 5]
        double[] b = { 2, 2, 4, 5 };
        DensityTrackingArray rhs = DensityTrackingArray.wrap(b.clone());
        ft.ftran(rhs);
        double[] x = rhs.toRawCopy1D();
        TestUtils.assertEquals(1.0, x[0], TOL);
        TestUtils.assertEquals(1.0, x[1], TOL);
        TestUtils.assertEquals(1.0, x[2], TOL);
        TestUtils.assertEquals(1.0, x[3], TOL);
    }


    @Test
    void updateWithNegativePivot() {
        ColumnsSupplier<Double> basis = ForestTomlinFactorTest.makeIdentity(3);
        ForestTomlinFactor ft = new ForestTomlinFactor(3);
        ft.build(basis);

        // Replace column 1 with [1, -3, 2] — negative pivot at row 1
        this.applyColumnUpdate(ft, 1, new double[] { 1, -3, 2 });

        // Basis: [[1,1,0],[0,-3,0],[0,2,1]]
        // x = [1,1,1], b = [2, -3, 3]
        double[] b = { 2, -3, 3 };
        DensityTrackingArray rhs = DensityTrackingArray.wrap(b.clone());
        ft.ftran(rhs);
        double[] x = rhs.toRawCopy1D();
        TestUtils.assertEquals(1.0, x[0], TOL);
        TestUtils.assertEquals(1.0, x[1], TOL);
        TestUtils.assertEquals(1.0, x[2], TOL);

        // BTRAN check
        DensityTrackingArray rhsB = DensityTrackingArray.wrap(b.clone());
        ft.btran(rhsB);
        double[] y = rhsB.toRawCopy1D();
        // Verify B^T y = b
        // B^T = [[1,0,0],[1,-3,2],[0,0,1]]
        double check0 = y[0];
        double check1 = y[0] - 3 * y[1] + 2 * y[2];
        double check2 = y[2];
        TestUtils.assertEquals(b[0], check0, TOL);
        TestUtils.assertEquals(b[1], check1, TOL);
        TestUtils.assertEquals(b[2], check2, TOL);
    }

    @Test
    void updateWithSparseIncomingColumn() {
        // Incoming column has only one nonzero (a spike)
        ColumnsSupplier<Double> basis = ForestTomlinFactorTest.makeIdentity(4);
        ForestTomlinFactor ft = new ForestTomlinFactor(4);
        ft.build(basis);

        // Replace column 2 with [0, 0, 7, 0] (a scaled unit vector)
        this.applyColumnUpdate(ft, 2, new double[] { 0, 0, 7, 0 });

        // New basis: diag(1, 1, 7, 1)
        double[] b = { 3, 5, 14, 9 };
        DensityTrackingArray rhs = DensityTrackingArray.wrap(b.clone());
        ft.ftran(rhs);
        double[] x = rhs.toRawCopy1D();
        TestUtils.assertEquals(3.0, x[0], TOL);
        TestUtils.assertEquals(5.0, x[1], TOL);
        TestUtils.assertEquals(2.0, x[2], TOL);
        TestUtils.assertEquals(9.0, x[3], TOL);
    }

    @Test
    void zeroPivotThrowsException() {
        ColumnsSupplier<Double> basis = ForestTomlinFactorTest.makeIdentity(2);
        ForestTomlinFactor ft = new ForestTomlinFactor(2);
        ft.build(basis);

        // Try to update with a column whose pivot entry is zero
        DensityTrackingArray aq = DensityTrackingArray.wrap(new double[] { 0, 1 });
        DensityTrackingArray ep = DensityTrackingArray.unit(2, 0);

        TestUtils.assertThrows(ArithmeticException.class, () -> ft.update(0, aq, ep));
    }
}
