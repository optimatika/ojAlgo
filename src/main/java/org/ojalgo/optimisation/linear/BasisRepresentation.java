package org.ojalgo.optimisation.linear;

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.R064CSC;
import org.ojalgo.matrix.store.SparseStructure2D;
import org.ojalgo.matrix.transformation.InvertibleFactor;

/**
 * Maintains a factored representation of the basis inverse (B^-1) for the revised simplex method. On each
 * pivot the representation is updated to reflect the column exchange, avoiding a full re-inversion.
 * <p>
 * Implementations:
 * <ul>
 * <li>{@link SparseDecomposition} — sparse LU ({@link org.ojalgo.matrix.decomposition.SparseLU}) with
 * Forrest-Tomlin updates.
 * <li>{@link DenseDecomposition} — dense LU, useful as a benchmark baseline.
 * <li>{@link ProductFormInverse} — accumulates elementary column operations (eta vectors).
 * </ul>
 *
 * @see RevisedStore
 */
interface BasisRepresentation extends InvertibleFactor<Double> {

    /**
     * PFI is only viable when the basis dimension is small enough that the periodic dense LU refactorisation
     * (O(dim^3)) stays cheap. For larger bases, sparse LU with Forrest-Tomlin updates is used unconditionally
     * — matching the approach of production solvers (CLP, HiGHS, GLOP) which abandoned PFI in favour of
     * sparse LU decades ago.
     * <p>
     * Density is not considered in the selection. The internal constraint matrix (including slack/artificial
     * columns) always has significantly lower density than the original model, making density thresholds
     * unreliable.
     */
    static BasisRepresentation newInstance(final SparseStructure2D sparse2D) {

        int dim = sparse2D.getMinDim();

        if (dim < 1_000 && sparse2D.density() > PrimitiveMath.QUARTER) {
            return new ProductFormInverse(dim);
        } else {
            return new SparseDecomposition(dim);
        }
    }

    /**
     * Factorise the basis formed by selecting columns from the constraint matrix. Until this has been called
     * there is an implicit assumption that the basis is the identity matrix.
     *
     * @param matrix   The full constraint matrix in CSC format
     * @param included The column indices that form the basis
     */
    void reset(R064CSC matrix, int[] included);

    /**
     * Update the inverse to reflect a replaced column in the basis.
     *
     * @param matrix      The full constraint matrix in CSC format
     * @param included    The current basis column indices (already reflecting the exchange)
     * @param exitIndex   The position within the basis that was replaced
     * @param enterColumn The column index in the original matrix that entered the basis
     * @return true if a full refactorisation was performed (rather than an incremental update)
     */
    boolean update(R064CSC matrix, int[] included, int exitIndex, int enterColumn);

}
