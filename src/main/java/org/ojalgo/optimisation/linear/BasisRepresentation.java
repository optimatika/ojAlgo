package org.ojalgo.optimisation.linear;

import org.ojalgo.array.SparseArray;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.transformation.InvertibleFactor;

/**
 * Maintains a factored representation of the basis inverse (B^-1) for the revised simplex method. On each
 * pivot the representation is updated to reflect the column exchange, avoiding a full re-inversion.
 * <p>
 * Implementations:
 * <ul>
 * <li>{@link DecomposedInverse} — uses a matrix decomposition (LU) that is periodically re-factored.
 * <li>{@link ProductFormInverse} — accumulates elementary column operations (eta vectors).
 * </ul>
 *
 * @see RevisedStore
 */
interface BasisRepresentation extends InvertibleFactor<Double> {

    /**
     * Until this has been called there is an implicit assumption that the basis is the identity matrix.
     * {@link #update(MatrixStore, int, SparseArray)} before this method has been called.
     */
    void reset(final MatrixStore<Double> basis);

    /**
     * Update the inverse to reflect a replaced column in the basis.
     *
     * @param basis  Full basis, with the column already exchanged.
     * @param col    The index, of the column, that was exchanged.
     * @param values The (non zero) values of that column.
     */
    void update(final MatrixStore<Double> basis, final int col, final SparseArray<Double> values);

}
