package org.ojalgo.optimisation.linear;

import java.util.Arrays;

import org.ojalgo.array.DensityTrackingArray;
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
 * <li>{@link SparseDecomposition} — sparse LU ({@link org.ojalgo.matrix.decomposition.SparseLU}: triangular
 * pass plus Markowitz kernel) with Forrest-Tomlin updates. The default.
 * <li>{@link DenseDecomposition} — dense LU.
 * <li>{@link ProductFormInverse} — accumulates elementary column operations (eta vectors).
 * </ul>
 *
 * @see RevisedStore
 */
interface BasisRepresentation extends InvertibleFactor<Double> {

    static BasisRepresentation newInstance(final SparseStructure2D sparse2D) {
        return new SparseDecomposition(sparse2D.getMinDim());
    }

    /**
     * BTRAN of the unit vector for a basis position: the row of the basis inverse corresponding to the
     * variable about to leave the basis. Implementations that can reuse partial results in the following
     * {@link #update(R064CSC, int[], int, int)} may retain them.
     *
     * @param position The basis position
     * @param result   Overwritten with the result, indexed by row
     */
    default void btranUnit(final int position, final DensityTrackingArray result) {
        double[] values = result.values;
        Arrays.fill(values, PrimitiveMath.ZERO);
        values[position] = PrimitiveMath.ONE;
        this.btran(values);
        result.reindex();
    }

    /**
     * @return The number of incremental updates since the basis was last factorised, by
     *         {@link #reset(R064CSC, int[])} or by a refactorisation within
     *         {@link #update(R064CSC, int[], int, int)}
     */
    int countUpdates();

    /**
     * FTRAN of a column of the constraint matrix: the column about to enter the basis. Implementations that
     * can reuse partial results in the following {@link #update(R064CSC, int[], int, int)} may retain them.
     *
     * @param matrix The full constraint matrix in CSC format
     * @param column The column index in the constraint matrix
     * @param result Overwritten with the result, indexed by basis position
     */
    default void ftranColumn(final R064CSC matrix, final int column, final DensityTrackingArray result) {
        double[] values = result.values;
        matrix.supplyTo(column, values);
        this.ftran(values);
        result.reindex();
    }

    /**
     * @return true if the current factorisation is (numerically) singular, by the implementation's own
     *         criteria. Then solves with it are meaningless. The default is false.
     */
    default boolean isSingular() {
        return false;
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
     * Update the inverse to reflect a replaced column in the basis. Implementations refactorise instead, when
     * their own criteria say so (such as the number of updates, fill-in or a small pivot). A caller that
     * wants a refactorisation for reasons of its own calls {@link #reset(R064CSC, int[])} instead.
     *
     * @param matrix      The full constraint matrix in CSC format
     * @param included    The current basis column indices (already reflecting the exchange)
     * @param exitIndex   The position within the basis that was replaced
     * @param enterColumn The column index in the original matrix that entered the basis
     * @return true if a full refactorisation was performed (rather than an incremental update)
     */
    boolean update(R064CSC matrix, int[] included, int exitIndex, int enterColumn);

}
