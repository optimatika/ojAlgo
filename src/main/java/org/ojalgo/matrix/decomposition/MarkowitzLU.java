package org.ojalgo.matrix.decomposition;

import java.util.Arrays;

import org.ojalgo.array.DensityTrackingArray;
import org.ojalgo.matrix.store.ColumnsSupplier;

/**
 * Sparse LU factorization with Markowitz pivoting.
 * <p>
 * Decomposes a sparse n x n matrix A as P A Q = L U, where:
 * <ul>
 * <li>P, Q are row and column permutations
 * <li>L is unit lower triangular (stored column-wise and row-wise)
 * <li>U is upper triangular (stored row-wise)
 * </ul>
 * <p>
 * Factorization follows the HiGHS three-phase approach:
 * <ol>
 * <li>{@code buildSimple} — detect and pivot on singletons (row singletons, column singletons) to reduce the
 * kernel
 * <li>{@code buildKernel} — Markowitz pivoting with count-linked lists for the remaining non-trivial
 * submatrix
 * <li>{@code buildFinish} — construct row-wise L (LR) for efficient BTRAN
 * </ol>
 * <p>
 * Storage uses plain Java arrays for tight index arithmetic. The L factor is stored both column-wise (for
 * FTRAN) and row-wise (LR, for BTRAN). The U factor is stored row-wise.
 */
public class MarkowitzLU {

    private static final int SEARCH_LIMIT = 8;
    static final double PIVOT_THRESHOLD = 0.1;
    static final double PIVOT_TOLERANCE = 1e-10;
    static final double TINY = 1e-15;

    private int[] colLinkFirst, colLinkNext, colLinkLast;

    private int[] kernelCols;
    private int[] mcIndex;
    private double[] mcMinPivot;

    private int[] mcStart, mcCountA, mcSpace;
    private double[] mcValue;
    private int[] mrIndex;

    private int[] mrStart, mrCount, mrSpace;
    private int nwork;

    private int pivotCount;
    private int rankDeficiency;
    private boolean[] rowActive, colActive;
    private int[] rowLinkFirst, rowLinkNext, rowLinkLast;
    /**
     * Persistent work buffer used by the solve methods for the Q / Q-transpose permutation between
     * original-row and step-indexed representations. Allocated in {@link #build} and reused across all
     * subsequent {@link #ftran} / {@link #btran} calls (mirrors the persistent-buffer pattern used by
     * {@link SparseLU}).
     */
    private double[] work;

    final int dim;
    /**
     * L factor column-wise: lStart[step]..lStart[step+1] are the multipliers for elimination step. lIndex
     * stores original row indices.
     */
    int[] lIndex;
    int lNz;
    /** L factor row-wise (LR) for efficient BTRAN. lrIndex stores the source step index. */
    int[] lrIndex;
    int[] lrStart;
    double[] lrValue;

    int[] lStart;
    double[] lValue;
    /**
     * Current number of U slots. Starts at {@link #dim} after {@link #buildFinish}; grows with Forest-Tomlin
     * appends.
     */
    int nSlots;
    int[] pivotCol;

    int[] pivotColPosition;
    /**
     * Pivot sequence: step to original row/column. Frozen after {@link #buildFinish}; used by the static L
     * solves ({@link #ftranL}, {@link #btranL}).
     */
    int[] pivotRow;
    /** Inverse maps: original row/column to step. Frozen after {@link #buildFinish}; L-side only. */
    int[] pivotRowPosition;
    /** Per-slot trailing capacity in the column-wise U region. */
    int[] ucColSpace;

    /** Per-slot end pointer for column-wise U (mirror of {@link #uEnd}). */
    int[] ucEnd;
    int[] ucIndex;

    /** Current total non-zeros in column-wise U storage (write cursor for appends). */
    int ucNz;

    /**
     * U factor column-wise (by logical pivot slot). {@code ucStart[s]..ucEnd[s]} delimits the off-diagonal
     * entries of U-column at slot s. {@code ucIndex[k]} stores the <b>original row index</b> of the entry
     * (upper-triangular in slot-space means this row was pivoted at an earlier slot). Same triplet convention
     * as {@code CompressedSparseR064}.
     */
    int[] ucStart;
    double[] ucValue;

    /**
     * Per-slot end pointer for row-wise U. {@code uEnd[s] == uStart[s] + currentCount}; shrinks on deletion
     * via swap-remove, grows on spike append.
     */
    int[] uEnd;
    /**
     * U factor row-wise: slot {@code s} occupies {@code uStart[s]..uEnd[s]} in
     * {@link #uIndex}/{@link #uValue}. {@code uIndex[k]} stores the <b>original column index</b> of the
     * entry. Separate {@link #uEnd} and {@link #uRowSpace} pointers (rather than {@code uStart[s+1]}) allow
     * in-place swap-remove during Forest-Tomlin updates and per-slot trailing slack for spike scattering —
     * matching HiGHS's {@code u_start} / {@code u_last_p} / {@code u_space} layout.
     */
    int[] uIndex;
    int uNz;
    int[] uPivotCol;

    int[] uPivotColPosition;
    /**
     * U-side pivot sequence: current U-step to original row/column. Initially populated as copies of
     * {@link #pivotRow} / {@link #pivotCol} at the end of {@link #buildFinish}, but mutated during
     * Forest-Tomlin updates (Stage 4). Used exclusively by {@link #ftranU} / {@link #btranU}.
     */
    int[] uPivotRow;

    /** Inverse maps for the mutable U-side permutation: original row/column to current U-step. */
    int[] uPivotRowPosition;
    double[] uPivotValue;

    /**
     * Per-slot trailing capacity in the row-wise U region between {@link #uEnd} and the next slot's start.
     */
    int[] uRowSpace;
    int[] uStart;

    double[] uValue;

    public MarkowitzLU(final int dim) {
        this.dim = dim;
    }

    /**
     * Backward transformation: solve A<sup>T</sup> y = rhs in-place. Splits into {@link #btranU} then
     * {@link #btranL}. After {@link #btranU} the intermediate result {@code u = U^{-T} Q^T rhs} lives in the
     * persistent {@link #work} buffer, step-indexed. {@link #btranL} then solves L<sup>T</sup> y' = u and
     * applies P<sup>T</sup> to produce the final orig-row-indexed answer in {@code rhs}.
     */
    public void btran(final DensityTrackingArray rhs) {
        double[] v = rhs.values;
        this.btranU(v);
        this.btranL(v);
    }

    /**
     * Factorise the given matrix. After this call, the L/U factors, permutations, and pivot values are
     * populated.
     */
    public void build(final ColumnsSupplier<Double> matrix) {
        int n = dim;

        long estNzL = Math.min(matrix.countNonzeros() * 3L + n, Integer.MAX_VALUE - 16);
        int initCap = (int) Math.min(estNzL, Integer.MAX_VALUE - 16);
        lIndex = new int[initCap];
        lValue = new double[initCap];
        lStart = new int[n + 1];
        uIndex = new int[initCap];
        uValue = new double[initCap];
        uStart = new int[n + 1];
        uPivotValue = new double[n];
        pivotRow = new int[n];
        pivotCol = new int[n];
        pivotRowPosition = new int[n];
        pivotColPosition = new int[n];
        Arrays.fill(pivotRowPosition, -1);
        Arrays.fill(pivotColPosition, -1);
        lNz = 0;
        uNz = 0;
        rankDeficiency = 0;

        this.buildSimple(matrix);
        this.buildKernel();
        this.buildFinish();

        if (work == null || work.length < n) {
            work = new double[n];
        }
    }

    public int dimension() {
        return dim;
    }

    /**
     * Forward transformation: solve A x = rhs in-place. Splits into {@link #ftranL} then {@link #ftranU}.
     * After {@link #ftranL} the intermediate result {@code z = L^{-1} P rhs} lives in {@code rhs} indexed by
     * original row ({@code rhs[pivotRow[step]] = z[step]}). {@link #ftranU} then solves U w = z and applies Q
     * to produce the final orig-col-indexed answer.
     */
    public void ftran(final DensityTrackingArray rhs) {
        double[] v = rhs.values;
        this.ftranL(v);
        this.ftranU(v);
    }

    public int rankDeficiency() {
        return rankDeficiency;
    }

    /**
     * Phase 3: Construct row-wise L (LR) for efficient BTRAN and trim arrays to actual sizes.
     */
    private void buildFinish() {
        int n = dim;

        // Fill any remaining unpivoted rows/cols (rank deficiency)
        while (pivotCount < n) {
            lStart[pivotCount] = lNz;
            uStart[pivotCount] = uNz;
            int row = -1, col = -1;
            for (int i = 0; i < n && row < 0; i++) {
                if (pivotRowPosition[i] == -1) {
                    row = i;
                }
            }
            for (int j = 0; j < n && col < 0; j++) {
                if (pivotColPosition[j] == -1) {
                    col = j;
                }
            }
            if (row < 0 || col < 0) {
                break;
            }
            pivotRow[pivotCount] = row;
            pivotCol[pivotCount] = col;
            pivotRowPosition[row] = pivotCount;
            pivotColPosition[col] = pivotCount;
            uPivotValue[pivotCount] = 0.0;
            pivotCount++;
            rankDeficiency++;
        }
        lStart[n] = lNz;
        uStart[n] = uNz;

        // Trim L and U arrays
        lIndex = Arrays.copyOf(lIndex, lNz);
        lValue = Arrays.copyOf(lValue, lNz);
        uIndex = Arrays.copyOf(uIndex, uNz);
        uValue = Arrays.copyOf(uValue, uNz);

        // Build row-wise L (LR).
        // For each L column (step j), for each entry targeting original row r,
        // map r to target step t = pivotRowPosition[r].
        // LR at step t gets entry: (source step = j, value = lValue).
        int[] lrCount = new int[n];
        for (int j = 0; j < n; j++) {
            for (int k = lStart[j]; k < lStart[j + 1]; k++) {
                int targetStep = pivotRowPosition[lIndex[k]];
                if (targetStep >= 0) {
                    lrCount[targetStep]++;
                }
            }
        }

        lrStart = new int[n + 1];
        for (int i = 0; i < n; i++) {
            lrStart[i + 1] = lrStart[i] + lrCount[i];
        }
        int lrNz = lrStart[n];
        lrIndex = new int[lrNz];
        lrValue = new double[lrNz];

        int[] lrPos = Arrays.copyOf(lrStart, n);
        for (int j = 0; j < n; j++) {
            for (int k = lStart[j]; k < lStart[j + 1]; k++) {
                int targetStep = pivotRowPosition[lIndex[k]];
                if (targetStep >= 0) {
                    int p = lrPos[targetStep]++;
                    lrIndex[p] = pivotRow[j];
                    lrValue[p] = lValue[k];
                }
            }
        }

        // Build column-wise U (UC).
        // Current U is row-wise by step t: (orig_col, value) at U[t, pivotColPosition[orig_col]].
        // Column-wise U is by step s: entries are (orig_row, value) where orig_row = pivotRow[source_step_row].
        // Transposition: for each row-step t with pivot orig_row = pivotRow[t], each entry (orig_col, val)
        // contributes to column step_col = pivotColPosition[orig_col] at orig_row.
        int[] ucCount = new int[n];
        for (int t = 0; t < n; t++) {
            for (int k = uStart[t]; k < uStart[t + 1]; k++) {
                int stepCol = pivotColPosition[uIndex[k]];
                if (stepCol >= 0) {
                    ucCount[stepCol]++;
                }
            }
        }

        ucStart = new int[n + 1];
        for (int i = 0; i < n; i++) {
            ucStart[i + 1] = ucStart[i] + ucCount[i];
        }
        ucNz = ucStart[n];
        ucIndex = new int[ucNz];
        ucValue = new double[ucNz];

        int[] ucPos = Arrays.copyOf(ucStart, n);
        for (int t = 0; t < n; t++) {
            int origRow = pivotRow[t];
            for (int k = uStart[t]; k < uStart[t + 1]; k++) {
                int stepCol = pivotColPosition[uIndex[k]];
                if (stepCol >= 0) {
                    int p = ucPos[stepCol]++;
                    ucIndex[p] = origRow;
                    ucValue[p] = uValue[k];
                }
            }
        }

        // U-side pivot tables start as copies of the L-side tables; FT updates mutate only these.
        // All slot-indexed arrays use capacity n+1 to match uStart/ucStart; ensureSlotCapacity grows them
        // uniformly on subsequent FT updates.
        int slotCap = n + 1;
        uPivotRow = Arrays.copyOf(pivotRow, slotCap);
        uPivotCol = Arrays.copyOf(pivotCol, slotCap);
        uPivotValue = Arrays.copyOf(uPivotValue, slotCap);
        uPivotRowPosition = pivotRowPosition.clone();
        uPivotColPosition = pivotColPosition.clone();

        // Per-slot end pointers and trailing slack, for HiGHS-style in-place delete/append during FT updates.
        // Initial slack is zero: any append triggers a row/column move on demand.
        uEnd = new int[slotCap];
        ucEnd = new int[slotCap];
        uRowSpace = new int[slotCap];
        ucColSpace = new int[slotCap];
        for (int s = 0; s < n; s++) {
            uEnd[s] = uStart[s + 1];
            ucEnd[s] = ucStart[s + 1];
        }
        nSlots = n;

        // Free kernel working storage
        mcStart = mcCountA = mcSpace = mcIndex = null;
        mcValue = mcMinPivot = null;
        mrStart = mrCount = mrSpace = mrIndex = null;
        colLinkFirst = colLinkNext = colLinkLast = null;
        rowLinkFirst = rowLinkNext = rowLinkLast = null;
        kernelCols = null;
        rowActive = colActive = null;
    }

    /**
     * Phase 2: Markowitz pivoting for the remaining kernel submatrix. Uses count-linked lists for efficient
     * pivot search.
     */
    private void buildKernel() {
        if (nwork == 0) {
            return;
        }

        int n = dim;

        // Compact active entries and rebuild row structure for kernel
        for (int idx = 0; idx < nwork; idx++) {
            int j = kernelCols[idx];
            int writePos = mcStart[j];
            int count = 0;
            for (int k = mcStart[j]; k < mcStart[j] + mcCountA[j]; k++) {
                if (rowActive[mcIndex[k]]) {
                    mcIndex[writePos] = mcIndex[k];
                    mcValue[writePos] = mcValue[k];
                    writePos++;
                    count++;
                }
            }
            mcCountA[j] = count;
            this.colFixMax(j);
        }

        // Rebuild row-wise kernel from scratch
        Arrays.fill(mrCount, 0);
        for (int idx = 0; idx < nwork; idx++) {
            int j = kernelCols[idx];
            for (int k = mcStart[j]; k < mcStart[j] + mcCountA[j]; k++) {
                this.rowInsertChecked(j, mcIndex[k]);
            }
        }

        // Initialize count-linked lists
        colLinkFirst = new int[n + 2];
        colLinkNext = new int[n];
        colLinkLast = new int[n];
        rowLinkFirst = new int[n + 2];
        rowLinkNext = new int[n];
        rowLinkLast = new int[n];
        Arrays.fill(colLinkFirst, -1);
        Arrays.fill(rowLinkFirst, -1);
        Arrays.fill(colLinkLast, 0);
        Arrays.fill(rowLinkLast, 0);

        for (int idx = 0; idx < nwork; idx++) {
            int j = kernelCols[idx];
            this.clinkAdd(j, mcCountA[j]);
        }
        for (int i = 0; i < n; i++) {
            if (rowActive[i] && mrCount[i] > 0) {
                this.rlinkAdd(i, mrCount[i]);
            }
        }

        // Working buffer for elimination
        int[] mwzIndex = new int[n];
        boolean[] mwzMark = new boolean[n];
        double[] mwzArray = new double[n];

        // Markowitz elimination loop
        int remaining = nwork;
        while (remaining > 0) {
            // --- 1. Find pivot ---
            int bestRow = -1, bestCol = -1;
            double bestPivotVal = 0.0;
            long bestMerit = Long.MAX_VALUE;
            int searchCount = 0;
            boolean found = false;

            // Search for singletons first (merit = 0)
            if (colLinkFirst[1] != -1) {
                bestCol = colLinkFirst[1];
                for (int k = mcStart[bestCol]; k < mcStart[bestCol] + mcCountA[bestCol]; k++) {
                    if (rowActive[mcIndex[k]]) {
                        bestRow = mcIndex[k];
                        bestPivotVal = mcValue[k];
                        break;
                    }
                }
                if (bestRow >= 0) {
                    bestMerit = 0;
                    found = true;
                }
            }
            if (!found && rowLinkFirst[1] != -1) {
                bestRow = rowLinkFirst[1];
                for (int k = mrStart[bestRow]; k < mrStart[bestRow] + mrCount[bestRow]; k++) {
                    if (colActive[mrIndex[k]]) {
                        bestCol = mrIndex[k];
                        break;
                    }
                }
                if (bestCol >= 0) {
                    bestPivotVal = this.findColEntry(bestCol, bestRow);
                    bestMerit = 0;
                    found = true;
                }
            }

            // General Markowitz search by increasing count
            if (!found) {
                for (int count = 2; count <= remaining + 1 && !found; count++) {
                    // Search columns with this count
                    if (count < colLinkFirst.length) {
                        for (int j = colLinkFirst[count]; j != -1; j = colLinkNext[j]) {
                            double minPivot = mcMinPivot[j];
                            for (int k = mcStart[j]; k < mcStart[j] + mcCountA[j]; k++) {
                                if (!rowActive[mcIndex[k]]) {
                                    continue;
                                }
                                if (Math.abs(mcValue[k]) >= minPivot) {
                                    int row = mcIndex[k];
                                    long merit = (long) (count - 1) * (mrCount[row] - 1);
                                    if (merit < bestMerit || (merit == bestMerit && Math.abs(mcValue[k]) > Math.abs(bestPivotVal))) {
                                        bestMerit = merit;
                                        bestRow = row;
                                        bestCol = j;
                                        bestPivotVal = mcValue[k];
                                        found = mrCount[row] < count;
                                    }
                                }
                            }
                            searchCount++;
                            if (searchCount >= SEARCH_LIMIT && bestMerit < Long.MAX_VALUE) {
                                found = true;
                            }
                            if (found) {
                                break;
                            }
                        }
                    }

                    // Search rows with this count
                    if (!found && count < rowLinkFirst.length) {
                        for (int i = rowLinkFirst[count]; i != -1; i = rowLinkNext[i]) {
                            for (int k = mrStart[i]; k < mrStart[i] + mrCount[i]; k++) {
                                int j = mrIndex[k];
                                if (!colActive[j]) {
                                    continue;
                                }
                                long merit = (long) (count - 1) * (mcCountA[j] - 1);
                                if (merit < bestMerit) {
                                    double val = this.findColEntry(j, i);
                                    if (Math.abs(val) >= mcMinPivot[j]) {
                                        bestMerit = merit;
                                        bestRow = i;
                                        bestCol = j;
                                        bestPivotVal = val;
                                        found = mcCountA[j] <= count;
                                    }
                                }
                            }
                            searchCount++;
                            if (searchCount >= SEARCH_LIMIT && bestMerit < Long.MAX_VALUE) {
                                found = true;
                            }
                            if (found) {
                                break;
                            }
                        }
                    }
                }
            }

            if (bestRow < 0) {
                // Rank deficiency
                this.handleRankDeficiency(remaining);
                break;
            }

            if (Math.abs(bestPivotVal) < PIVOT_TOLERANCE) {
                rankDeficiency++;
            }

            // --- 2. Record pivot ---
            lStart[pivotCount] = lNz;
            uStart[pivotCount] = uNz;
            pivotRow[pivotCount] = bestRow;
            pivotCol[pivotCount] = bestCol;
            pivotRowPosition[bestRow] = pivotCount;
            pivotColPosition[bestCol] = pivotCount;
            uPivotValue[pivotCount] = bestPivotVal;

            this.clinkDel(bestCol);
            this.rlinkDel(bestRow);
            rowActive[bestRow] = false;
            colActive[bestCol] = false;

            // --- 3. Store L column and collect working buffer ---
            int mwzCount = 0;
            for (int k = mcStart[bestCol]; k < mcStart[bestCol] + mcCountA[bestCol]; k++) {
                int row = mcIndex[k];
                if (!rowActive[row]) {
                    continue;
                }
                double multiplier = mcValue[k] / bestPivotVal;
                mwzIndex[mwzCount] = row;
                mwzArray[row] = multiplier;
                mwzMark[row] = true;
                mwzCount++;

                this.ensureLCapacity();
                lIndex[lNz] = row;
                lValue[lNz] = multiplier;
                lNz++;
            }

            // --- 4. Store U row ---
            for (int k = mrStart[bestRow]; k < mrStart[bestRow] + mrCount[bestRow]; k++) {
                int col = mrIndex[k];
                if (!colActive[col] || col == bestCol) {
                    continue;
                }
                double val = this.findColEntry(col, bestRow);
                if (val != 0.0) {
                    this.ensureUCapacity();
                    uIndex[uNz] = col;
                    uValue[uNz] = val;
                    uNz++;
                }
            }

            // --- 5. Elimination ---
            for (int rk = mrStart[bestRow]; rk < mrStart[bestRow] + mrCount[bestRow]; rk++) {
                int col = mrIndex[rk];
                if (!colActive[col] || col == bestCol) {
                    continue;
                }

                // Delete pivot row entry from this column
                double pivRowVal = this.colDeleteEntry(col, bestRow);

                // Eliminate: update existing entries and detect fill-in
                int colEnd = mcStart[col] + mcCountA[col];
                for (int m = mcStart[col]; m < colEnd; m++) {
                    int row = mcIndex[m];
                    if (mwzMark[row]) {
                        mwzMark[row] = false;
                        double newVal = mcValue[m] - pivRowVal * mwzArray[row];
                        if (Math.abs(newVal) < TINY) {
                            // Cancellation
                            mcCountA[col]--;
                            mcIndex[m] = mcIndex[mcStart[col] + mcCountA[col]];
                            mcValue[m] = mcValue[mcStart[col] + mcCountA[col]];
                            m--;
                            colEnd--;
                            this.rowDeleteEntry(col, row);
                        } else {
                            mcValue[m] = newVal;
                        }
                    }
                }

                // Fill-in entries
                for (int i = 0; i < mwzCount; i++) {
                    int row = mwzIndex[i];
                    if (mwzMark[row]) {
                        mwzMark[row] = false;
                        double fillinVal = -pivRowVal * mwzArray[row];
                        if (Math.abs(fillinVal) >= TINY) {
                            this.ensureKernelColCapacity(col);
                            this.colInsert(col, row, fillinVal);
                            this.rowInsertChecked(col, row);
                        }
                    }
                }

                // Reset marks
                for (int i = 0; i < mwzCount; i++) {
                    mwzMark[mwzIndex[i]] = true;
                }

                // Update column link list
                this.colFixMax(col);
                this.clinkDel(col);
                if (mcCountA[col] > 0) {
                    this.clinkAdd(col, mcCountA[col]);
                }
            }

            // Clear working buffer
            for (int i = 0; i < mwzCount; i++) {
                mwzMark[mwzIndex[i]] = false;
                mwzArray[mwzIndex[i]] = 0.0;
            }

            // Remove the now-inactive pivot column from rows' mr arrays
            // and update their link-list positions. Fill-in and
            // cancellation changes from the elimination step above are
            // already reflected in mrCount (via rowInsertChecked /
            // rowDeleteEntry), so only the stale pivot-column entry
            // remains to be cleaned up.
            for (int k = mcStart[bestCol]; k < mcStart[bestCol] + mcCountA[bestCol]; k++) {
                int row = mcIndex[k];
                if (!rowActive[row]) {
                    continue;
                }
                this.rowDeleteEntry(bestCol, row);
                this.rlinkDel(row);
                if (mrCount[row] > 0) {
                    this.rlinkAdd(row, mrCount[row]);
                }
            }

            pivotCount++;
            remaining--;
        }
    }

    /**
     * Phase 1: Detect and pivot on singletons (rows/columns with a single nonzero in the active submatrix).
     * This reduces the kernel size before the expensive Markowitz search.
     */
    private void buildSimple(final ColumnsSupplier<Double> matrix) {
        int n = dim;
        pivotCount = 0;

        // Count nonzeros per row across all columns
        int[] rowNz = new int[n];
        int[] colNz = new int[n];
        for (int j = 0; j < n; j++) {
            var iter = matrix.getColumn(j).nonzeros().iterator();
            while (iter.hasNext()) {
                var nz = iter.next();
                if (nz.doubleValue() != 0.0) {
                    rowNz[(int) nz.index()]++;
                    colNz[j]++;
                }
            }
        }

        // Store the matrix into editable kernel structures.
        mcStart = new int[n];
        mcCountA = new int[n];
        mcSpace = new int[n];
        mcMinPivot = new double[n];

        int totalSpace = 0;
        for (int j = 0; j < n; j++) {
            mcStart[j] = totalSpace;
            mcSpace[j] = Math.max(colNz[j] * 2, 4);
            mcCountA[j] = 0;
            totalSpace += mcSpace[j];
        }
        mcIndex = new int[totalSpace];
        mcValue = new double[totalSpace];

        // Row-wise storage
        mrStart = new int[n];
        mrCount = new int[n];
        mrSpace = new int[n];
        int totalRowSpace = 0;
        for (int i = 0; i < n; i++) {
            mrStart[i] = totalRowSpace;
            mrSpace[i] = Math.max(rowNz[i] * 2, 4);
            totalRowSpace += mrSpace[i];
        }
        mrIndex = new int[totalRowSpace];

        // Fill kernel structures from the matrix
        for (int j = 0; j < n; j++) {
            var iter = matrix.getColumn(j).nonzeros().iterator();
            while (iter.hasNext()) {
                var nz = iter.next();
                int row = (int) nz.index();
                double val = nz.doubleValue();
                if (val == 0.0) {
                    continue;
                }
                this.colInsert(j, row, val);
                this.rowInsert(j, row);
            }
            this.colFixMax(j);
        }

        // Track which rows/cols are still in the kernel
        rowActive = new boolean[n];
        colActive = new boolean[n];
        Arrays.fill(rowActive, true);
        Arrays.fill(colActive, true);

        // Iteratively detect and pivot on singletons
        boolean progress = true;
        while (progress) {
            progress = false;

            for (int j = 0; j < n; j++) {
                if (!colActive[j]) {
                    continue;
                }
                int activeCount = this.countActiveColEntries(j);
                if (activeCount != 1) {
                    continue;
                }

                // Column singleton: single active entry
                int pivR = -1;
                double pivVal = 0.0;
                for (int k = mcStart[j]; k < mcStart[j] + mcCountA[j]; k++) {
                    if (rowActive[mcIndex[k]]) {
                        pivR = mcIndex[k];
                        pivVal = mcValue[k];
                        break;
                    }
                }
                if (pivR < 0 || Math.abs(pivVal) < PIVOT_TOLERANCE) {
                    continue;
                }

                this.recordSingletonPivot(pivR, j, pivVal);
                progress = true;
            }

            for (int i = 0; i < n; i++) {
                if (!rowActive[i]) {
                    continue;
                }
                int activeCount = this.countActiveRowEntries(i, colActive);
                if (activeCount != 1) {
                    continue;
                }

                // Row singleton: single active entry in this row
                int pivC = -1;
                for (int k = mrStart[i]; k < mrStart[i] + mrCount[i]; k++) {
                    if (colActive[mrIndex[k]]) {
                        pivC = mrIndex[k];
                        break;
                    }
                }
                if (pivC < 0) {
                    continue;
                }

                double pivVal = this.findColEntry(pivC, i);
                if (Math.abs(pivVal) < PIVOT_TOLERANCE) {
                    continue;
                }

                this.recordSingletonPivot(i, pivC, pivVal);
                progress = true;
            }
        }

        // Collect remaining kernel columns
        nwork = 0;
        kernelCols = new int[n];
        for (int j = 0; j < n; j++) {
            if (colActive[j]) {
                kernelCols[nwork++] = j;
            }
        }
    }

    private void clinkAdd(final int index, final int count) {
        if (count >= colLinkFirst.length) {
            return;
        }
        int mover = colLinkFirst[count];
        colLinkLast[index] = -2 - count;
        colLinkNext[index] = mover;
        colLinkFirst[count] = index;
        if (mover >= 0) {
            colLinkLast[mover] = index;
        }
    }

    private void clinkDel(final int index) {
        if (colLinkLast == null) {
            return;
        }
        int xlast = colLinkLast[index];
        int xnext = colLinkNext[index];
        if (xlast >= 0) {
            colLinkNext[xlast] = xnext;
        } else if (xlast <= -2) {
            colLinkFirst[-xlast - 2] = xnext;
        }
        if (xnext >= 0) {
            colLinkLast[xnext] = xlast;
        }
    }

    private double colDeleteEntry(final int col, final int row) {
        int start = mcStart[col];
        int end = start + mcCountA[col];
        for (int k = start; k < end; k++) {
            if (mcIndex[k] == row) {
                double val = mcValue[k];
                mcCountA[col]--;
                mcIndex[k] = mcIndex[start + mcCountA[col]];
                mcValue[k] = mcValue[start + mcCountA[col]];
                return val;
            }
        }
        return 0.0;
    }

    private void colFixMax(final int col) {
        double maxVal = 0.0;
        for (int k = mcStart[col]; k < mcStart[col] + mcCountA[col]; k++) {
            double abs = Math.abs(mcValue[k]);
            if (abs > maxVal) {
                maxVal = abs;
            }
        }
        mcMinPivot[col] = maxVal * PIVOT_THRESHOLD;
    }

    private void colInsert(final int col, final int row, final double value) {
        int pos = mcStart[col] + mcCountA[col];
        mcIndex[pos] = row;
        mcValue[pos] = value;
        mcCountA[col]++;
    }

    private int countActiveColEntries(final int col) {
        int count = 0;
        for (int k = mcStart[col]; k < mcStart[col] + mcCountA[col]; k++) {
            if (rowActive[mcIndex[k]]) {
                count++;
            }
        }
        return count;
    }

    private int countActiveRowEntries(final int row, final boolean[] activeCol) {
        int count = 0;
        for (int k = mrStart[row]; k < mrStart[row] + mrCount[row]; k++) {
            if (activeCol[mrIndex[k]]) {
                count++;
            }
        }
        return count;
    }

    private void ensureKernelColCapacity(final int col) {
        if (mcCountA[col] >= mcSpace[col]) {
            int oldStart = mcStart[col];
            int oldCount = mcCountA[col];
            int newSpace = oldCount * 2 + 4;
            int newStart = mcIndex.length;
            mcIndex = Arrays.copyOf(mcIndex, newStart + newSpace);
            mcValue = Arrays.copyOf(mcValue, newStart + newSpace);
            System.arraycopy(mcIndex, oldStart, mcIndex, newStart, oldCount);
            System.arraycopy(mcValue, oldStart, mcValue, newStart, oldCount);
            mcStart[col] = newStart;
            mcSpace[col] = newSpace;
        }
    }

    private void ensureLCapacity() {
        if (lNz >= lIndex.length) {
            int newCap = lIndex.length * 3 / 2 + 64;
            lIndex = Arrays.copyOf(lIndex, newCap);
            lValue = Arrays.copyOf(lValue, newCap);
        }
    }

    /** Ensures the slot-indexed arrays (uStart/uEnd/uPivot*) accommodate at least {@code needed} slots. */
    private void ensureSlotCapacity(final int needed) {
        if (needed > uStart.length) {
            int newLen = Math.max(uStart.length * 2, needed + 16);
            uStart = Arrays.copyOf(uStart, newLen);
            uEnd = Arrays.copyOf(uEnd, newLen);
            uRowSpace = Arrays.copyOf(uRowSpace, newLen);
            ucStart = Arrays.copyOf(ucStart, newLen);
            ucEnd = Arrays.copyOf(ucEnd, newLen);
            ucColSpace = Arrays.copyOf(ucColSpace, newLen);
            uPivotValue = Arrays.copyOf(uPivotValue, newLen);
            uPivotRow = Arrays.copyOf(uPivotRow, newLen);
            uPivotCol = Arrays.copyOf(uPivotCol, newLen);
        }
    }

    private void ensureUCapacity() {
        if (uNz >= uIndex.length) {
            int newCap = uIndex.length * 3 / 2 + 64;
            uIndex = Arrays.copyOf(uIndex, newCap);
            uValue = Arrays.copyOf(uValue, newCap);
        }
    }

    /**
     * Ensures {@link #uIndex}/{@link #uValue} can accept {@code add} more entries starting at {@link #uNz}.
     */
    private void ensureUCapacity(final int add) {
        if (uNz + add > uIndex.length) {
            int newLen = Math.max(uIndex.length * 2, uNz + add + 64);
            uIndex = Arrays.copyOf(uIndex, newLen);
            uValue = Arrays.copyOf(uValue, newLen);
        }
    }

    /**
     * Ensures {@link #ucIndex}/{@link #ucValue} can accept {@code add} more entries starting at
     * {@link #ucNz}.
     */
    private void ensureUCCapacity(final int add) {
        if (ucNz + add > ucIndex.length) {
            int newLen = Math.max(ucIndex.length * 2, ucNz + add + 64);
            ucIndex = Arrays.copyOf(ucIndex, newLen);
            ucValue = Arrays.copyOf(ucValue, newLen);
        }
    }

    /** Ensures {@link #work} has capacity for at least {@code needed} entries. */
    private void ensureWorkCapacity(final int needed) {
        if (work == null || work.length < needed) {
            work = new double[Math.max(needed, dim)];
        }
    }

    private double findColEntry(final int col, final int row) {
        for (int k = mcStart[col]; k < mcStart[col] + mcCountA[col]; k++) {
            if (mcIndex[k] == row) {
                return mcValue[k];
            }
        }
        return 0.0;
    }

    /**
     * Ensures the row-wise U region of slot {@code s} has at least one free trailing position. Copies the row
     * to the end of {@link #uIndex}/{@link #uValue} with extra slack.
     */
    private void growUrRow(final int s) {
        int start = uStart[s];
        int end = uEnd[s];
        int count = end - start;
        int newSpace = (count / 8) + 4;
        this.ensureUCapacity(count + newSpace);
        int newStart = uNz;
        System.arraycopy(uIndex, start, uIndex, newStart, count);
        System.arraycopy(uValue, start, uValue, newStart, count);
        uStart[s] = newStart;
        uEnd[s] = newStart + count;
        uRowSpace[s] = newSpace;
        uNz = newStart + count + newSpace;
    }

    private void handleRankDeficiency(final int remaining) {
        int n = dim;
        for (int cnt = 0; cnt < remaining; cnt++) {
            int row = -1, col = -1;
            for (int i = 0; i < n && row < 0; i++) {
                if (rowActive[i]) {
                    row = i;
                }
            }
            for (int j = 0; j < n && col < 0; j++) {
                if (colActive[j]) {
                    col = j;
                }
            }
            if (row < 0 || col < 0) {
                break;
            }
            lStart[pivotCount] = lNz;
            uStart[pivotCount] = uNz;
            pivotRow[pivotCount] = row;
            pivotCol[pivotCount] = col;
            pivotRowPosition[row] = pivotCount;
            pivotColPosition[col] = pivotCount;
            uPivotValue[pivotCount] = 0.0;
            pivotCount++;
            rowActive[row] = false;
            colActive[col] = false;
            rankDeficiency++;
        }
    }

    /**
     * Record a singleton pivot: store L multipliers and U row entries.
     */
    private void recordSingletonPivot(final int pivR, final int pivC, final double pivVal) {
        lStart[pivotCount] = lNz;
        uStart[pivotCount] = uNz;

        // L column: multipliers from pivot column (active rows, excluding pivot)
        for (int k = mcStart[pivC]; k < mcStart[pivC] + mcCountA[pivC]; k++) {
            int row = mcIndex[k];
            if (!rowActive[row] || row == pivR) {
                continue;
            }
            double multiplier = mcValue[k] / pivVal;
            this.ensureLCapacity();
            lIndex[lNz] = row;
            lValue[lNz] = multiplier;
            lNz++;
        }

        // U row: entries in the pivot row from active columns (excluding pivot col)
        for (int k = mrStart[pivR]; k < mrStart[pivR] + mrCount[pivR]; k++) {
            int col = mrIndex[k];
            if (!colActive[col] || col == pivC) {
                continue;
            }
            double val = this.findColEntry(col, pivR);
            if (val != 0.0) {
                this.ensureUCapacity();
                uIndex[uNz] = col;
                uValue[uNz] = val;
                uNz++;
            }
        }

        pivotRow[pivotCount] = pivR;
        pivotCol[pivotCount] = pivC;
        pivotRowPosition[pivR] = pivotCount;
        pivotColPosition[pivC] = pivotCount;
        uPivotValue[pivotCount] = pivVal;
        pivotCount++;

        rowActive[pivR] = false;
        colActive[pivC] = false;
    }

    private void rlinkAdd(final int index, final int count) {
        if (count >= rowLinkFirst.length) {
            return;
        }
        int mover = rowLinkFirst[count];
        rowLinkLast[index] = -2 - count;
        rowLinkNext[index] = mover;
        rowLinkFirst[count] = index;
        if (mover >= 0) {
            rowLinkLast[mover] = index;
        }
    }

    private void rlinkDel(final int index) {
        if (rowLinkLast == null) {
            return;
        }
        int xlast = rowLinkLast[index];
        int xnext = rowLinkNext[index];
        if (xlast >= 0) {
            rowLinkNext[xlast] = xnext;
        } else if (xlast <= -2) {
            rowLinkFirst[-xlast - 2] = xnext;
        }
        if (xnext >= 0) {
            rowLinkLast[xnext] = xlast;
        }
    }

    private void rowDeleteEntry(final int col, final int row) {
        int start = mrStart[row];
        int end = start + mrCount[row];
        for (int k = start; k < end; k++) {
            if (mrIndex[k] == col) {
                mrCount[row]--;
                mrIndex[k] = mrIndex[start + mrCount[row]];
                return;
            }
        }
    }

    private void rowInsert(final int col, final int row) {
        int pos = mrStart[row] + mrCount[row];
        mrIndex[pos] = col;
        mrCount[row]++;
    }

    private void rowInsertChecked(final int col, final int row) {
        if (mrCount[row] >= mrSpace[row]) {
            int oldStart = mrStart[row];
            int oldCount = mrCount[row];
            int newSpace = oldCount * 2 + 4;
            int newStart = mrIndex.length;
            mrIndex = Arrays.copyOf(mrIndex, newStart + newSpace);
            System.arraycopy(mrIndex, oldStart, mrIndex, newStart, oldCount);
            mrStart[row] = newStart;
            mrSpace[row] = newSpace;
        }
        mrIndex[mrStart[row] + mrCount[row]] = col;
        mrCount[row]++;
    }

    /**
     * Solve L<sup>T</sup> y = v and apply P<sup>T</sup>, operating entirely in orig-row-indexed {@code v}.
     * Uses {@link #lrIndex}/{@link #lrValue} where {@code lrIndex[k]} stores the <b>original row index</b> of
     * the entry's source row (making {@code lrIndex} scatter targets directly valid in the orig-row space).
     */
    void btranL(final double[] v) {
        int n = dim;
        for (int step = n - 1; step >= 0; step--) {
            int pr = pivotRow[step];
            double val = v[pr];
            if (val == 0.0) {
                continue;
            }
            for (int k = lrStart[step]; k < lrStart[step + 1]; k++) {
                v[lrIndex[k]] -= lrValue[k] * val;
            }
        }
    }

    /**
     * Apply Q<sup>T</sup> and solve U<sup>T</sup> u = Q<sup>T</sup> v, then scatter the slot-indexed result
     * back into {@code v} by {@link #uPivotRow} so that on exit {@code v} is orig-row-indexed (ready for R
     * application and L<sup>T</sup> solve). Iterates every current slot (including appended FT slots) and
     * skips slots marked deleted via {@code uPivotRow[slot] == -1}.
     */
    void btranU(final double[] v) {
        this.ensureWorkCapacity(nSlots);
        double[] w = work;

        // Apply Q^T: gather orig-col-indexed v into slot-indexed work via the mutable U-side permutation.
        for (int s = 0; s < nSlots; s++) {
            int pc = uPivotCol[s];
            w[s] = pc >= 0 ? v[pc] : 0.0;
        }

        // Solve U^T u = work (forward substitution, scatter via row-wise U).
        for (int s = 0; s < nSlots; s++) {
            if (uPivotRow[s] < 0) {
                w[s] = 0.0;
                continue;
            }
            if (uPivotValue[s] != 0.0) {
                w[s] /= uPivotValue[s];
            }
            double val = w[s];
            if (val == 0.0) {
                continue;
            }
            for (int k = uStart[s]; k < uEnd[s]; k++) {
                int targetSlot = uPivotColPosition[uIndex[k]];
                if (targetSlot >= 0) {
                    w[targetSlot] -= uValue[k] * val;
                }
            }
        }

        // Scatter slot-indexed work back into orig-row-indexed v via uPivotRow.
        Arrays.fill(v, 0.0);
        for (int s = 0; s < nSlots; s++) {
            int pr = uPivotRow[s];
            if (pr >= 0) {
                v[pr] = w[s];
            }
        }
    }

    /**
     * Apply P and solve L z = P rhs in-place on {@code v} using original-row indexing throughout. No work
     * buffer needed: {@code v[pivotRow[step]]} is the current step's pivot value (P is implicit), and
     * {@link #lIndex} already stores original-row targets (column-wise L with orig-row scatter).
     */
    void ftranL(final double[] v) {
        int n = dim;
        for (int step = 0; step < n; step++) {
            double pivVal = v[pivotRow[step]];
            if (pivVal == 0.0) {
                continue;
            }
            for (int k = lStart[step]; k < lStart[step + 1]; k++) {
                v[lIndex[k]] -= lValue[k] * pivVal;
            }
        }
    }

    /**
     * Solve U w = z and apply Q. Reads orig-row-indexed {@code v} ({@code v[uPivotRow[s]] = z[s]}), writes
     * orig-col-indexed answer back into {@code v}. Uses column-wise U
     * ({@link #ucStart}/{@link #ucEnd}/{@link #ucIndex}/{@link #ucValue}) for a scatter-based
     * column-broadcast solve. Iterates slots in reverse order (HiGHS convention: latest-appended FT slots
     * first), skipping deleted slots.
     */
    void ftranU(final double[] v) {
        this.ensureWorkCapacity(nSlots);
        double[] w = work;

        // Gather orig-row-indexed v into slot-indexed work.
        for (int s = 0; s < nSlots; s++) {
            int pr = uPivotRow[s];
            w[s] = pr >= 0 ? v[pr] : 0.0;
        }

        // Solve U w' = w via column-broadcast over UC (reverse-slot order).
        for (int s = nSlots - 1; s >= 0; s--) {
            if (uPivotRow[s] < 0) {
                w[s] = 0.0;
                continue;
            }
            double pivVal = w[s];
            if (uPivotValue[s] != 0.0) {
                pivVal /= uPivotValue[s];
                w[s] = pivVal;
            }
            if (pivVal == 0.0) {
                continue;
            }
            for (int k = ucStart[s]; k < ucEnd[s]; k++) {
                int targetSlot = uPivotRowPosition[ucIndex[k]];
                if (targetSlot >= 0) {
                    w[targetSlot] -= ucValue[k] * pivVal;
                }
            }
        }

        // Apply Q: scatter slot-indexed work into orig-col-indexed v.
        Arrays.fill(v, 0.0);
        for (int s = 0; s < nSlots; s++) {
            int pc = uPivotCol[s];
            if (pc >= 0) {
                v[pc] = w[s];
            }
        }
    }

    /**
     * Apply a Forest-Tomlin rank-1 basis update: replace the basis column currently at pivot row
     * {@code origPivotRow} with the column whose FTRAN-result is {@code aq}. The old U slot is marked deleted
     * (its pivot-row entries are in-place swap-removed from the affected U columns, and its pivot-column
     * entries similarly from the affected U rows). A new slot is appended holding the spike column from
     * {@code aq} (scattered into both column-wise and row-wise U), with diagonal value
     * {@code oldPivot * alpha}. Mirrors HiGHS {@code HFactor::updateFT}.
     *
     * @param origPivotRow orig-row identity of the leaving basis column
     * @param aq           FTRAN of the entering column through the current (pre-update) LU; orig-col indexed
     * @return {@code oldPivot} (the diagonal value of the slot being replaced) — the caller (Forest-Tomlin
     *         driver) uses this to scale {@code ep}-based R-matrix entries.
     */
    double updateFT(final int origPivotRow, final DensityTrackingArray aq) {
        int oldSlot = uPivotRowPosition[origPivotRow];
        if (oldSlot < 0) {
            throw new ArithmeticException("No active slot for pivot row " + origPivotRow);
        }
        double oldPivot = uPivotValue[oldSlot];
        int origPivotCol = uPivotCol[oldSlot];
        final int i1 = origPivotCol;
        double alpha = aq.doubleValue(i1);
        if (alpha == 0.0) {
            throw new ArithmeticException("Zero alpha for FT update at row " + origPivotRow);
        }

        // Delete old pivot row's UR entries from matching UC columns (swap-remove).
        for (int k = uStart[oldSlot]; k < uEnd[oldSlot]; k++) {
            int c = uIndex[k];
            int cSlot = uPivotColPosition[c];
            if (cSlot < 0) {
                continue;
            }
            for (int j = ucStart[cSlot]; j < ucEnd[cSlot]; j++) {
                if (ucIndex[j] == origPivotRow) {
                    int last = --ucEnd[cSlot];
                    ucIndex[j] = ucIndex[last];
                    ucValue[j] = ucValue[last];
                    ucColSpace[cSlot]++;
                    break;
                }
            }
        }

        // Delete old pivot col's UC entries from matching UR rows (swap-remove).
        for (int k = ucStart[oldSlot]; k < ucEnd[oldSlot]; k++) {
            int r = ucIndex[k];
            int rSlot = uPivotRowPosition[r];
            if (rSlot < 0) {
                continue;
            }
            for (int j = uStart[rSlot]; j < uEnd[rSlot]; j++) {
                if (uIndex[j] == origPivotCol) {
                    int last = --uEnd[rSlot];
                    uIndex[j] = uIndex[last];
                    uValue[j] = uValue[last];
                    uRowSpace[rSlot]++;
                    break;
                }
            }
        }

        // Mark old slot deleted and release its storage logically (leave data, reclaim via pointers).
        uPivotRow[oldSlot] = -1;
        uPivotCol[oldSlot] = -1;
        uPivotValue[oldSlot] = 0.0;
        uEnd[oldSlot] = uStart[oldSlot];
        ucEnd[oldSlot] = ucStart[oldSlot];
        uPivotRowPosition[origPivotRow] = -1;
        uPivotColPosition[origPivotCol] = -1;

        // Append the new slot.
        int newSlot = nSlots;
        this.ensureSlotCapacity(newSlot + 1);

        // Scatter aq's off-pivot entries into the new UC column and into existing UR rows.
        double[] aqArr = aq.values;
        int newCount = 0;
        for (int i = 0; i < dim; i++) {
            if (i == origPivotRow) {
                continue;
            }
            double val = aqArr[i];
            if (val == 0.0) {
                continue;
            }
            newCount++;
        }
        this.ensureUCCapacity(newCount);
        ucStart[newSlot] = ucNz;
        for (int i = 0; i < dim; i++) {
            if (i == origPivotRow) {
                continue;
            }
            double val = aqArr[i];
            if (val == 0.0) {
                continue;
            }
            ucIndex[ucNz] = i;
            ucValue[ucNz] = val;
            ucNz++;

            // Scatter into the UR row for orig-row i (still-active slot rSlot).
            int rSlot = uPivotRowPosition[i];
            if (rSlot < 0) {
                continue;
            }
            if (uRowSpace[rSlot] == 0) {
                this.growUrRow(rSlot);
            }
            int put = uEnd[rSlot]++;
            uIndex[put] = origPivotCol;
            uValue[put] = val;
            uRowSpace[rSlot]--;
        }
        ucEnd[newSlot] = ucNz;
        ucColSpace[newSlot] = 0;

        // The new slot's UR row is empty (new pivot = trailing edge of the modified U).
        uStart[newSlot] = uNz;
        uEnd[newSlot] = uNz;
        uRowSpace[newSlot] = 0;

        // Register new slot: reuse the orig col identity of the leaving column.
        uPivotRow[newSlot] = origPivotRow;
        uPivotCol[newSlot] = origPivotCol;
        uPivotValue[newSlot] = oldPivot * alpha;
        uPivotRowPosition[origPivotRow] = newSlot;
        uPivotColPosition[origPivotCol] = newSlot;
        nSlots++;

        return oldPivot;
    }
}
