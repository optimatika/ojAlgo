/*
 * Copyright 1997-2025 Optimatika
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.ojalgo.matrix.decomposition;

import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.transformation.InvertibleFactor;

/**
 * Accumulated Forrest-Tomlin update factors represented as a composed permutation plus a sequence of sparse
 * eta vectors whose indices live in the fully-composed permutation space.
 * <p>
 * Each Forrest-Tomlin update contributes a cyclic permutation P_k and an eta column E_k. The product
 * (P_1*E_1)*(P_2*E_2)*...*(P_k*E_k) is rewritten as P_composed * E'_1 * E'_2 * ... * E'_k where each E'_i has
 * its indices remapped into the composed permutation space.
 * <p>
 * At ftran time: one O(m) permutation gather replaces k individual arraycopy shifts, then the eta corrections
 * are applied in a single pass using primitive parallel arrays (no SparseArray/iterator overhead). At btran
 * time: etas in reverse, then one O(m) inverse-permutation gather.
 */
final class ForrestTomlinFactors implements InvertibleFactor<Double> {

    /**
     * Remap a single index through a cyclic left-shift of [from..to]: from maps to to, j in (from..to] maps
     * to j-1, others unchanged.
     */
    private static int remapIndex(final int index, final int from, final int to) {
        if (index == from) {
            return to;
        } else if (index > from && index <= to) {
            return index - 1;
        }
        return index;
    }

    /**
     * Inverse of myFtranPerm: work[i] = arg[btranPerm[i]] reverses all cyclic left-shifts.
     */
    private final int[] myBtranPerm;

    private int myCount;

    private final int myDim;

    /**
     * Number of eta nonzeros per update.
     */
    private final int[] myEtaCounts;

    /**
     * Eta nonzero indices per update, remapped into the composed permutation space.
     */
    private final int[][] myEtaIndices;

    /**
     * Eta nonzero values per update, parallel to myEtaIndices.
     */
    private final double[][] myEtaValues;

    /**
     * Composed forward permutation: work[i] = arg[ftranPerm[i]] applies all cyclic left-shifts.
     */
    private final int[] myFtranPerm;

    /**
     * Pivot row for each eta, remapped into the composed permutation space.
     */
    private final int[] myPivotRows;

    /**
     * Reusable buffer for permutation gather.
     */
    private final double[] myWork;

    ForrestTomlinFactors(final int dim, final int maxUpdates) {
        super();

        myDim = dim;
        myCount = 0;

        myFtranPerm = new int[dim];
        myBtranPerm = new int[dim];
        for (int i = 0; i < dim; i++) {
            myFtranPerm[i] = i;
            myBtranPerm[i] = i;
        }

        myEtaIndices = new int[maxUpdates][];
        myEtaValues = new double[maxUpdates][];
        myEtaCounts = new int[maxUpdates];
        myPivotRows = new int[maxUpdates];
        myWork = new double[dim];
    }

    @Override
    public void btran(final double[] arg) {

        if (myCount == 0) {
            return;
        }

        // Step 1: Apply transposed eta corrections in reverse order
        for (int k = myCount - 1; k >= 0; k--) {
            double val = arg[myPivotRows[k]];
            int[] indices = myEtaIndices[k];
            double[] values = myEtaValues[k];
            int cnt = myEtaCounts[k];
            for (int e = 0; e < cnt; e++) {
                arg[indices[e]] += values[e] * val;
            }
        }

        // Step 2: Apply the inverse permutation (one gather pass)
        for (int i = 0; i < myDim; i++) {
            myWork[i] = arg[myBtranPerm[i]];
        }
        System.arraycopy(myWork, 0, arg, 0, myDim);
    }

    @Override
    public void btran(final PhysicalStore<Double> arg) {
        InvertibleFactor.doPrimitive(arg, this);
    }

    @Override
    public void ftran(final double[] arg) {

        if (myCount == 0) {
            return;
        }

        // Step 1: Apply the composed forward permutation (one gather pass)
        for (int i = 0; i < myDim; i++) {
            myWork[i] = arg[myFtranPerm[i]];
        }
        System.arraycopy(myWork, 0, arg, 0, myDim);

        // Step 2: Apply the eta corrections in order
        for (int k = 0; k < myCount; k++) {
            int[] indices = myEtaIndices[k];
            double[] values = myEtaValues[k];
            int cnt = myEtaCounts[k];
            double dot = 0.0;
            for (int e = 0; e < cnt; e++) {
                dot += values[e] * arg[indices[e]];
            }
            arg[myPivotRows[k]] += dot;
        }
    }

    @Override
    public void ftran(final PhysicalStore<Double> arg) {
        InvertibleFactor.doPrimitive(this, arg);
    }

    @Override
    public int getColDim() {
        return myDim;
    }

    @Override
    public int getRowDim() {
        return myDim;
    }

    /**
     * Add one Forrest-Tomlin update: a cyclic left-shift [from..to] composed into the permutation, plus a
     * sparse eta column provided as primitive parallel arrays.
     * <p>
     * Previously stored eta indices and pivot rows are remapped through the new cycle so that all etas
     * operate in the fully-composed permutation space. The per-call cost is O(total_stored_nnz + shift_range)
     * which is acceptable for the typical update limit of ~200.
     *
     * @param from       start of the cyclic shift range
     * @param to         end of the cyclic shift range (pivot row in the pre-shift space)
     * @param etaIndices nonzero indices of the eta column
     * @param etaValues  nonzero values parallel to etaIndices
     * @param etaCount   number of valid entries in etaIndices/etaValues
     */
    void addUpdate(final int from, final int to, final int[] etaIndices, final double[] etaValues, final int etaCount) {

        // Remap all previously stored eta indices and pivot rows through the new cycle
        for (int k = 0; k < myCount; k++) {
            myPivotRows[k] = ForrestTomlinFactors.remapIndex(myPivotRows[k], from, to);
            int[] indices = myEtaIndices[k];
            int cnt = myEtaCounts[k];
            for (int e = 0; e < cnt; e++) {
                indices[e] = ForrestTomlinFactors.remapIndex(indices[e], from, to);
            }
        }

        // Store the new eta (already in the right index space before cycle composition)
        myEtaIndices[myCount] = etaIndices;
        myEtaValues[myCount] = etaValues;
        myEtaCounts[myCount] = etaCount;
        myPivotRows[myCount] = to;
        myCount++;

        // Compose the cyclic left-shift into the forward permutation
        int saved = myFtranPerm[from];
        System.arraycopy(myFtranPerm, from + 1, myFtranPerm, from, to - from);
        myFtranPerm[to] = saved;

        // Rebuild the inverse for the affected range
        for (int i = from; i <= to; i++) {
            myBtranPerm[myFtranPerm[i]] = i;
        }
    }

    int nbUpdates() {
        return myCount;
    }

    void reset() {
        myCount = 0;
        for (int i = 0; i < myDim; i++) {
            myFtranPerm[i] = i;
            myBtranPerm[i] = i;
        }
    }

}
