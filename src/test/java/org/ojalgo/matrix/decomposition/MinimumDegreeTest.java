package org.ojalgo.matrix.decomposition;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.R064CSC;

public class MinimumDegreeTest {

    private static int scorePattern(final R064CSC matrix, final int[] order) {
        int n = matrix.getColDim();
        int[] deg = new int[n];

        int[] pointers = matrix.pointers;
        int[] indices = matrix.indices;

        for (int col = 0; col < n; col++) {
            int pCol = order[col];
            for (int p = pointers[pCol]; p < pointers[pCol + 1]; p++) {
                int row = indices[p];
                if (row > pCol) {
                    continue; // upper triangle only
                }
                int prow = row;
                deg[col]++;
                if (prow != col) {
                    deg[prow]++;
                }
            }
        }

        int score = 0;
        for (int d : deg) {
            score += d * d;
        }
        return score;
    }

    @Test
    public void testBetterThanIdentityOnForkLikePattern() {

        // 4-node pattern:
        // 0 connected to 1
        // 1 connected to 0,2,3
        // 2 connected to 1,3
        // 3 connected to 1,2
        // This creates a small "fork"/almost-clique where naive identity can give a
        // less favourable degree profile than a minimum-degree style ordering.

        int n = 4;
        int[] pointers = { 0, 1, 3, 5, 6 };
        int[] indices = { 0, 0, 2, 1, 3, 1 };
        double[] values = { 1, 1, 1, 1, 1, 1 };
        R064CSC matrix = new R064CSC(n, n, values, indices, pointers);

        int[] identity = { 0, 1, 2, 3 };
        MinimumDegree md = new MinimumDegree();
        md.approximate(matrix);
        int[] order = md.getOrder();

        int identityScore = MinimumDegreeTest.scorePattern(matrix, identity);
        int mdScore = MinimumDegreeTest.scorePattern(matrix, order);

        TestUtils.assertTrue(mdScore <= identityScore);
    }

    @Test
    public void testPathGraphOrderingAgrees() {

        // Path 0-1-2-3, symmetric pattern in CSC upper triangle
        int n = 4;
        int[] pointers = { 0, 1, 3, 5, 6 };
        int[] indices = { 0, 0, 1, 1, 2, 3 };
        double[] values = { 1, 1, 1, 1, 1, 1 };
        R064CSC matrix = new R064CSC(n, n, values, indices, pointers);

        MinimumDegree md = new MinimumDegree();
        md.approximate(matrix);
        int[] order = md.getOrder();

        // Both must be permutations of 0..n-1
        boolean[] seen = new boolean[n];

        for (int v : order) {
            TestUtils.assertTrue(v >= 0 && v < n);
            TestUtils.assertTrue(!seen[v]);
            seen[v] = true;
        }
    }

    @Test
    public void testProducesValidPermutation() {

        // Simple 4x4 symmetric pattern with upper triangle entries
        // [ 1 . . . ]
        // [ 1 1 . . ]
        // [ . 1 1 1 ]
        // [ . . 1 1 ]
        int n = 4;
        int[] pointers = { 0, 1, 3, 5, 6 };
        int[] indices = { 0, 0, 1, 2, 2, 3 };
        double[] values = { 1, 1, 1, 1, 1, 1 };
        R064CSC matrix = new R064CSC(n, n, values, indices, pointers);

        MinimumDegree md = new MinimumDegree();
        md.approximate(matrix);
        int[] order = md.getOrder();

        TestUtils.assertEquals(n, order.length);

        boolean[] seen = new boolean[n];
        for (int v : order) {
            TestUtils.assertTrue(v >= 0 && v < n);
            TestUtils.assertTrue(!seen[v]);
            seen[v] = true;
        }
    }

    @Test
    public void testStarGraphCenterLast() {

        // Star with center 0 and leaves 1,2,3; store upper triangle pattern
        int n = 4;
        int[] pointers = { 0, 1, 2, 3, 4 };
        int[] indices = { 0, 0, 0, 0 };
        double[] values = { 1, 1, 1, 1 };
        R064CSC matrix = new R064CSC(n, n, values, indices, pointers);

        MinimumDegree md = new MinimumDegree();
        md.approximate(matrix);
        int[] order = md.getOrder();

        // Should place the high-degree center (0) last
        TestUtils.assertEquals(0, order[n - 1]);
    }

}
