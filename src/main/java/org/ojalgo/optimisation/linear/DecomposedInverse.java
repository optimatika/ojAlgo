package org.ojalgo.optimisation.linear;

import org.ojalgo.array.SparseArray;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;

/**
 * Maintains an LU decomposition of the basis matrix for efficient solving of linear systems in the revised
 * simplex method. Supports incremental updates using the Forrest-Tomlin algorithm when columns change, with
 * periodic refactorization to maintain numerical stability.
 */
final class DecomposedInverse implements BasisRepresentation {

    /**
     * Maximum number of updates before forcing a complete refactorization to prevent numerical instability
     * from accumulated roundoff errors.
     */
    private static final int UPDATES_LIMIT = 100;

    private final LU<Double> myDecomposition;
    private int myUpdateCounter = 0;

    /**
     * Creates a new decomposition-based basis representation.
     *
     * @param sparse Whether to use sparse LU decomposition (recommended for larger sparse problems)
     * @param dim    Dimension of the basis matrix
     */
    DecomposedInverse(final boolean sparse, final int dim) {
        super();
        myDecomposition = sparse ? LU.newSparseR064() : LU.R064.make(dim, dim);
    }

    /**
     * Solves the transposed system B^T x = b, overwriting the right-hand side with the solution. Used to
     * compute dual variables (shadow prices) in the simplex method.
     */
    @Override
    public void btran(final PhysicalStore<Double> arg) {

        if (myDecomposition.isComputed()) {
            myDecomposition.btran(arg);
        }
    }

    /**
     * Solves the system B x = b, overwriting the right-hand side with the solution. Used to compute the basic
     * solution and direction vectors in the simplex method.
     */
    @Override
    public void ftran(final PhysicalStore<Double> arg) {

        if (myDecomposition.isComputed()) {
            myDecomposition.ftran(arg);
        }
    }

    @Override
    public int getColDim() {
        return myDecomposition.getColDim();
    }

    @Override
    public int getRowDim() {
        return myDecomposition.getRowDim();
    }

    /**
     * Completely rebuilds the decomposition from the given basis matrix. Resets the update counter.
     */
    @Override
    public void reset(final MatrixStore<Double> basis) {
        myDecomposition.decompose(basis);
        myUpdateCounter = 0;
    }

    /**
     * Updates the decomposition to reflect a change in the basis matrix. Uses the Forrest-Tomlin update
     * algorithm to efficiently modify the LU factors. Falls back to a complete refactorization if: 1. The
     * update counter exceeds the limit 2. The decomposition is not in a computed state 3. The update
     * operation fails due to numerical issues
     *
     * @param basis  The current basis matrix (used only if refactorization is needed)
     * @param col    The column index in the basis being replaced
     * @param values The new column values
     */
    @Override
    public void update(final MatrixStore<Double> basis, final int col, final SparseArray<Double> values) {

        // Force refactorization if too many updates or if the update fails
        if (myUpdateCounter++ >= UPDATES_LIMIT || !myDecomposition.isComputed() || !myDecomposition.updateColumn(col, values)) {
            myDecomposition.decompose(basis);
            myUpdateCounter = 0;
        }
    }

}
