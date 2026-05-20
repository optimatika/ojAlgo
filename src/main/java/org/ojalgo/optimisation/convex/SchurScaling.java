package org.ojalgo.optimisation.convex;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.matrix.store.MatrixStore;

/**
 * Internal helper for constructing a scaled Schur complement. Performs simple diagonal regularisation
 * and symmetric diagonal scaling (Jacobi equilibration) so that the Schur complement has (approximate)
 * unit diagonal. Scaling is always applied when there is at least one constraint.
 * <p>
 * Regularisation: diag[i] = max(diag[i], REG_ABS, REG_REL * maxDiag)
 * Scaling factors: d[i] = 1 / sqrt(diag[i]) so that (D A) inv(Q) (A^T D) yields S' with diag ≈ 1.
 * </p>
 */
final class SchurScaling {

    private static final double REG_ABS = 1.0e-14; // lowered from 1e-12
    private static final double REG_REL = 1.0e-10; // Relative regularisation multiplier

    private SchurScaling() {}

    /**
     * Compute Schur complement diagonal from A and invQAt.
     * S = A inv(Q) A^T, invQAt = inv(Q) A^T.
     */
    static double[] computeDiagonal(final MatrixStore<Double> A, final MatrixStore<Double> invQAt) {
        int m = (int) A.countRows();
        double[] diag = new double[m];
        for (int i = 0; i < m; i++) {
            // S_ii = row_i(A) * column_i(invQAt)
            double sum = 0.0;
            for (int k = 0, n = (int) A.countColumns(); k < n; k++) {
                double a = A.doubleValue(i, k);
                double b = invQAt.doubleValue(k, i); // column i of invQAt
                if (a != 0.0 && b != 0.0) sum += a * b;
            }
            diag[i] = sum;
        }
        return diag;
    }

    /**
     * Regularise the diagonal in-place and return max after regularisation.
     */
    static double regulariseDiagonal(final double[] diag) {
        double max = 0.0;
        for (double v : diag) if (v > max) max = v;
        boolean tinyMatrix = max < REG_ABS;
        double minAllowed = tinyMatrix ? Math.max(REG_ABS, REG_REL * Math.max(max, REG_ABS)) : REG_ABS;
        for (int i = 0; i < diag.length; i++) {
            double v = diag[i];
            if (!(v > 0.0)) v = minAllowed; // Non-positive gets regularised
            if (tinyMatrix && v < minAllowed) v = minAllowed; // Only enforce relative when all tiny
            diag[i] = v;
        }
        return max;
    }

    /**
     * Build scaling factors d[i] = 1 / sqrt(diag[i]). Diagonal expected regularised and positive.
     */
    static double[] buildScaling(final double[] diag) {
        double[] d = new double[diag.length];
        for (int i = 0; i < diag.length; i++) {
            d[i] = 1.0 / Math.sqrt(diag[i]);
        }
        return d;
    }

    /**
     * Scale RHS in-place: rhs[i] *= d[i].
     */
    static void scaleRHS(final double[] d, final double[] rhs) {
        for (int i = 0; i < rhs.length; i++) rhs[i] *= d[i];
    }

    /**
     * Unscale solution (multipliers) in-place: L[i] *= d[i].
     */
    static void unscaleSolution(final double[] d, final double[] sol) {
        for (int i = 0; i < sol.length; i++) sol[i] *= d[i];
    }

    /**
     * Return scaled Schur element value S'_{ij} = d_i * d_j * S_{ij}.
     */
    static double scaleElement(final double[] d, final int i, final int j, final double value) {
        return d[i] * d[j] * value;
    }
}