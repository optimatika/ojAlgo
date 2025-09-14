/*
 * Copyright 1997-2025 Optimatika
 *
 * Licensed under the MIT License.
 */
package org.ojalgo.matrix.decomposition;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.type.context.NumberContext;

/**
 * Tests pseudoinverse (getInverse) and least squares solutions (getSolution) for the general purpose
 * SingularValue implementations (DenseSingularValue & RawSingularValue). Covers various aspect ratios and
 * ranks. Eigenvalue based spectral implementations are intentionally excluded here.
 */
public class SingularValuePseudoInverseTest extends MatrixDecompositionTests {

    private static final NumberContext ACC = NumberContext.of(8); // Consistent with other tests

    private static MatrixStore<Double> fullRank(final int m, final int n) {
        PhysicalStore<Double> A = R064Store.FACTORY.make(m, n);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                // Structured but non-degenerate pattern
                A.set(i, j, (i + 1) * 0.2 + (j + 1) * 0.3 + ((i * j) % 7) * 0.01);
            }
        }
        return A;
    }

    private static MatrixStore<Double> rankDeficient(final int m, final int n, final int r) {
        PhysicalStore<Double> B = R064Store.FACTORY.make(m, r);
        PhysicalStore<Double> C = R064Store.FACTORY.make(r, n);
        for (int i = 0; i < m; i++) {
            for (int k = 0; k < r; k++) {
                B.set(i, k, (i + 1) * (k + 2));
            }
        }
        for (int k = 0; k < r; k++) {
            for (int j = 0; j < n; j++) {
                C.set(k, j, (k + 1) * (j + 1) * 0.1);
            }
        }
        return B.multiply(C);
    }

    private static void testMatrix(final MatrixStore<Double> A, final String label) {

        SingularValue<Double> dense = new DenseSingularValue.R064();
        SingularValue<Double> raw = new RawSingularValue();

        dense.decompose(A);
        raw.decompose(A);

        // Basic rank expectations
        int minDim = Math.min(A.getRowDim(), A.getColDim());
        TestUtils.assertTrue(dense.getRank() <= minDim);
        TestUtils.assertTrue(raw.getRank() <= minDim);

        MatrixStore<Double> pinvDense = dense.getInverse();
        MatrixStore<Double> pinvRaw = raw.getInverse();

        // Reflexivity: A A+ A == A
        TestUtils.assertEquals(A, A.multiply(pinvDense).multiply(A), ACC);
        TestUtils.assertEquals(A, A.multiply(pinvRaw).multiply(A), ACC);

        // A+ A A+ == A+
        TestUtils.assertEquals(pinvDense, pinvDense.multiply(A).multiply(pinvDense), ACC);
        TestUtils.assertEquals(pinvRaw, pinvRaw.multiply(A).multiply(pinvRaw), ACC);

        // For square full rank: A+ approximates true inverse => A A+ = I and A+ A = I
        if (A.getRowDim() == A.getColDim() && dense.getRank() == A.getRowDim()) {
            MatrixStore<Double> I = A.physical().makeEye(A.getRowDim(), A.getColDim());
            TestUtils.assertEquals(I, A.multiply(pinvDense), ACC);
            TestUtils.assertEquals(I, pinvDense.multiply(A), ACC);
            TestUtils.assertEquals(I, A.multiply(pinvRaw), ACC);
            TestUtils.assertEquals(I, pinvRaw.multiply(A), ACC);
        }

        // Projection properties (idempotence) for A A+ and A+ A
        MatrixStore<Double> projColDense = A.multiply(pinvDense); // m x m
        MatrixStore<Double> projRowDense = pinvDense.multiply(A); // n x n
        TestUtils.assertEquals(projColDense, projColDense.multiply(projColDense), ACC);
        TestUtils.assertEquals(projRowDense, projRowDense.multiply(projRowDense), ACC);

        MatrixStore<Double> projColRaw = A.multiply(pinvRaw);
        MatrixStore<Double> projRowRaw = pinvRaw.multiply(A);
        TestUtils.assertEquals(projColRaw, projColRaw.multiply(projColRaw), ACC);
        TestUtils.assertEquals(projRowRaw, projRowRaw.multiply(projRowRaw), ACC);

        // Prepare RHS with small number of columns to test getSolution
        int m = A.getRowDim();
        PhysicalStore<Double> rhs = A.physical().make(m, 2);
        // Deterministic pattern (not all ones to catch mistakes)
        for (int i = 0; i < m; i++) {
            rhs.set(i, 0, i + 1.0);
            rhs.set(i, 1, (i + 1.0) * 0.5);
        }

        MatrixStore<Double> solDense = dense.getSolution(rhs);
        MatrixStore<Double> solRaw = raw.getSolution(rhs);

        // Compare with explicit pseudoinverse application
        MatrixStore<Double> solDenseExpected = pinvDense.multiply(rhs);
        MatrixStore<Double> solRawExpected = pinvRaw.multiply(rhs);
        TestUtils.assertEquals(solDenseExpected, solDense, ACC);
        TestUtils.assertEquals(solRawExpected, solRaw, ACC);

        // Column space projection: A * X ≈ A * A+ * RHS
        TestUtils.assertEquals(A.multiply(solDense), projColDense.multiply(rhs), ACC);
        TestUtils.assertEquals(A.multiply(solRaw), projColRaw.multiply(rhs), ACC);

        // Ensure singular values sorted descending
        Array1D<Double> svDense = dense.getSingularValues();
        for (int i = 1; i < svDense.size(); i++) {
            TestUtils.assertTrue(svDense.doubleValue(i - 1) >= svDense.doubleValue(i));
        }
        Array1D<Double> svRaw = raw.getSingularValues();
        for (int i = 1; i < svRaw.size(); i++) {
            TestUtils.assertTrue(svRaw.doubleValue(i - 1) >= svRaw.doubleValue(i));
        }

        // Cross-implementation comparison (allow tolerance, skip rank 0 to avoid trivial all-zero mismatch checks)
        if (dense.getRank() > 0 && raw.getRank() > 0) {
            TestUtils.assertEquals(pinvDense, pinvRaw, NumberContext.of(6)); // looser for implementation differences
        }
    }

    @Test
    public void testSingletons() {
        SingularValuePseudoInverseTest.testMatrix(RawStore.wrap(new double[][] { { 7.0 } }), "singletonPositive");
        SingularValuePseudoInverseTest.testMatrix(RawStore.wrap(new double[][] { { 0.0 } }), "singletonZero");
        SingularValuePseudoInverseTest.testMatrix(RawStore.wrap(new double[][] { { -5.0 } }), "singletonNegative");
    }

    @Test
    public void testSquareFullRank() {
        SingularValuePseudoInverseTest.testMatrix(SingularValuePseudoInverseTest.fullRank(6, 6), "squareFullRank");
    }

    @Test
    public void testSquareRankDeficient() {
        SingularValuePseudoInverseTest.testMatrix(SingularValuePseudoInverseTest.rankDeficient(7, 7, 3), "squareRankDeficient");
    }

    @Test
    public void testTallFullRank() {
        SingularValuePseudoInverseTest.testMatrix(SingularValuePseudoInverseTest.fullRank(10, 5), "tallFullRank");
    }

    @Test
    public void testTallRankDeficient() {
        SingularValuePseudoInverseTest.testMatrix(SingularValuePseudoInverseTest.rankDeficient(11, 6, 3), "tallRankDeficient");
    }

    @Test
    public void testWideFullRank() {
        SingularValuePseudoInverseTest.testMatrix(SingularValuePseudoInverseTest.fullRank(5, 10), "wideFullRank");
    }

    @Test
    public void testWideRankDeficient() {
        SingularValuePseudoInverseTest.testMatrix(SingularValuePseudoInverseTest.rankDeficient(6, 12, 3), "wideRankDeficient");
    }

    @Test
    public void testZeroMatrix() {
        SingularValuePseudoInverseTest.testMatrix(R064Store.FACTORY.makeZero(4, 7), "zeroMatrix");
    }

}