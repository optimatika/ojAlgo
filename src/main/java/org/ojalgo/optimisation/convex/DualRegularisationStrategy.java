package org.ojalgo.optimisation.convex;

/**
 * Strategy to compute a (very small) dual-side regularisation factor (rho) to stabilise
 * Schur complement / KKT dual block factorisation without materially changing the solution.
 *
 * Implementations must return 0.0 when regularisation should be skipped.
 */
interface DualRegularisationStrategy {

    /**
     * @param diagMax maximum diagonal value of the (negated) Schur complement or a scale surrogate (>=1)
     * @param diagMin minimum positive diagonal value (>0) or Double.POSITIVE_INFINITY if unavailable
     * @param m       number of active (dual) constraints (rows in Schur/KKT lower-right block)
     * @param extendedPrecision true if extended precision / iterative refinement mode (disable reg)
     * @param zeroQ   true if Q treated as zero (LP case) – disable reg
     * @return rho >= 0.0, tiny regularisation magnitude to apply (+rho on Schur diag, -rho I in KKT dual block)
     */
    double compute(double diagMax, double diagMin, int m, boolean extendedPrecision, boolean zeroQ);

}
