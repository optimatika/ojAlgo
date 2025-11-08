package org.ojalgo.optimisation.conic;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.structure.Access1D;

/**
 * Internal KKT system abstraction used by ConicSolver to compute Newton directions.
 * Implementations may use dense or sparse factorisations and may cache assembly buffers.
 * This interface is intentionally minimal to avoid constraining future variants.
 */
interface KKTSystem {

    /**
     * Solve the (assembled implicitly) KKT system for the given objective Hessian approximation [H],
     * optional equality constraint matrix [Aeq], and right-hand sides rx, ry.
     * Writes the primal step (dx) and, if applicable, the equality duals (y).
     *
     * @return true if a solution was found; false if factorisation failed (caller may regularise or abort)
     */
    boolean solve(MatrixStore<Double> H, MatrixStore<Double> Aeq, Access1D<?> rx, Access1D<?> ry,
                  PhysicalStore<Double> outDx, PhysicalStore<Double> outY);
}
