package org.ojalgo.matrix.decomposition;

import java.util.Arrays;

import org.ojalgo.array.DensityTrackingArray;
import org.ojalgo.matrix.store.ColumnsSupplier;

/**
 * Full basis representation using Markowitz LU factorization with Forest-Tomlin rank-1 updates.
 * <p>
 * This is the main class in the library, combining:
 * <ul>
 * <li>{@link MarkowitzLU} for the initial factorization (Markowitz pivoting — the piece missing from ojAlgo)
 * <li>Forest-Tomlin update chain for incremental basis column replacement (same algorithm as HiGHS's
 * {@code HFactor.updateFT()} and ojAlgo's {@code SparseLU.updateColumn()})
 * <li>Hyper-sparse FTRAN / BTRAN (density-aware solve paths)
 * </ul>
 * <p>
 * Storage for the R-matrix (accumulated row-eta updates) uses plain arrays in the same layout as HiGHS:
 * {@code rIndex/rValue} packed by update, with {@code rStart} delimiters and {@code rPivotRow} recording
 * which row each update acts on.
 */
public class ForestTomlinFactor {

    private static final double REFACTOR_RATIO = 1.5;
    private static final int MAX_UPDATES = 300;
    private final int dim;

    private MarkowitzLU lu;
    private int updates;

    /**
     * R-matrix (Forest-Tomlin update chain). Each update k stores a row-eta vector in
     * rIndex/rValue[rStart[k]..rStart[k+1]). rPivotRow[k] is the pivot row index for update k. rPivotValue[k]
     * is the scaling factor applied to the pivot position.
     */
    private int[] rIndex;
    private double[] rValue;
    private int[] rStart;
    private int[] rPivotRow;

    private double[] rPivotValue;
    private int rNz;

    public ForestTomlinFactor(final int dim) {
        this.dim = dim;
        int initCap = Math.max(dim * 4, 256);
        rIndex = new int[initCap];
        rValue = new double[initCap];
        rStart = new int[Math.max(dim, 64) + 1];
        rPivotRow = new int[Math.max(dim, 64)];
        rPivotValue = new double[Math.max(dim, 64)];
        rNz = 0;
        updates = 0;
    }

    public void btran(final DensityTrackingArray rhs) {
        // Step 1: apply Forest-Tomlin R updates in reverse (transposed)
        this.btranFT(rhs);

        // Step 2: solve the original LU^T system
        lu.btran(rhs);
    }

    public void build(final ColumnsSupplier<Double> matrix) {
        lu = new MarkowitzLU(dim);
        lu.build(matrix);
        this.resetUpdateChain();
    }

    public int dimension() {
        return dim;
    }

    public void ftran(final DensityTrackingArray rhs) {
        // Step 1: solve the original LU system
        lu.ftran(rhs);

        // Step 2: apply Forest-Tomlin R updates (forward order)
        this.ftranFT(rhs);
    }

    public void refactor(final ColumnsSupplier<Double> matrix) {
        this.build(matrix);
    }

    /** Whether the update chain has grown large enough to warrant refactorization. */
    public boolean shouldRefactor() {
        if (updates >= Math.min(MAX_UPDATES, 3 * dim)) {
            return true;
        }
        long luNz = lu.lStart[dim] - lu.lStart[0] + lu.uStart[dim] - lu.uStart[0];
        return luNz > 0 && (double) rNz / luNz > REFACTOR_RATIO;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Stores a product-form eta vector derived from {@code aq} into the R-matrix chain. The eta entries are
     * the off-pivot entries of {@code aq} (the FTRAN of the incoming column), allowing the FTRAN and BTRAN
     * solves to apply the rank-1 basis change without modifying the underlying LU factors.
     */
    public double update(final int pivotRow, final DensityTrackingArray aq, final DensityTrackingArray ep) {
        double alpha = aq.doubleValue(pivotRow);
        if (alpha == 0.0) {
            throw new ArithmeticException("Zero pivot at row " + pivotRow);
        }

        // Ensure capacity for a new update.
        if (updates >= rPivotRow.length) {
            int newCap = rPivotRow.length * 3 / 2 + 16;
            rPivotRow = Arrays.copyOf(rPivotRow, newCap);
            rPivotValue = Arrays.copyOf(rPivotValue, newCap);
            rStart = Arrays.copyOf(rStart, newCap + 1);
        }

        rStart[updates] = rNz;
        rPivotRow[updates] = pivotRow;
        rPivotValue[updates] = 1.0 / alpha;

        // Store the off-pivot entries of aq. These define the row-eta
        // matrix M^{-1} where M has column pivotRow replaced by aq.
        // FTRAN applies: x[pivotRow] /= alpha, x[i] -= aq[i] * x[pivotRow]
        // BTRAN applies: v[pivotRow] = (v[pivotRow] - Σ aq[i]*v[i]) / alpha
        double[] aqArr = aq.values;
        for (int i = 0; i < dim; i++) {
            if (i == pivotRow) {
                continue;
            }
            double val = aqArr[i];
            if (val == 0.0) {
                continue;
            }

            if (rNz >= rIndex.length) {
                int newCap = rIndex.length * 3 / 2 + 64;
                rIndex = Arrays.copyOf(rIndex, newCap);
                rValue = Arrays.copyOf(rValue, newCap);
            }
            rIndex[rNz] = i;
            rValue[rNz] = val;
            rNz++;
        }

        updates++;
        rStart[updates] = rNz;

        return alpha;
    }

    public int updateCount() {
        return updates;
    }

    private void btranFT(final DensityTrackingArray rhs) {
        double[] v = rhs.values;
        for (int k = updates - 1; k >= 0; k--) {
            int pivRow = rPivotRow[k];

            // Gather: v[p] = (v[p] - Σ aq[i]*v[i]) / alpha
            double sum = 0.0;
            for (int j = rStart[k]; j < rStart[k + 1]; j++) {
                sum += rValue[j] * v[rIndex[j]];
            }
            v[pivRow] = (v[pivRow] - sum) * rPivotValue[k];
        }
    }

    private void ftranFT(final DensityTrackingArray rhs) {
        double[] v = rhs.values;
        for (int k = 0; k < updates; k++) {
            int pivRow = rPivotRow[k];

            // Scale the pivot row: x[p] /= alpha
            v[pivRow] *= rPivotValue[k];
            double pivVal = v[pivRow];

            // Scatter: x[i] -= aq[i] * x[p] for each stored aq entry
            if (pivVal != 0.0) {
                for (int j = rStart[k]; j < rStart[k + 1]; j++) {
                    v[rIndex[j]] -= rValue[j] * pivVal;
                }
            }
        }
    }

    private void resetUpdateChain() {
        updates = 0;
        rNz = 0;
        rStart[0] = 0;
    }
}
