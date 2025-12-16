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

import org.ojalgo.matrix.store.R064CSC;

/**
 * Approximate Minimum Degree (AMD) style ordering for pre-ordering a symmetric sparse matrix prior to
 * numerical factorisation (Cholesky or LDL). This is NOT the full/correct AMD-algorithm as described by
 * Amestoy, Davis, and Duff, but rather something simpler that aims to accomplish similar results. It is
 * clearly not as fast as a highly tuned proper AMD implementation, but compared to not doing any ordering at
 * all it is found to provide up to 90% of the potential speedup in overall use cases.
 * <p>
 * This implementation:
 * <ul>
 * <li>Assumes the input {@link R064CSC} represents a symmetric pattern with only the upper triangle stored.
 * <li>Treats the matrix as an adjacency graph and computes a fill-reducing column ordering.
 * <li>Does not modify the input matrix; it only returns a permutation vector.
 * </ul>
 */
public final class MinimumDegree {

    private final Pivot myPermutation = new Pivot();

    /**
     * Approximates a minimum degree ordering for a symmetric {@link R064CSC} matrix. The result is stored
     * internally. To permute vectors or matrices according to the computed ordering, use the
     * {@link #permute(double[], double[])} or {@link #permute(R064CSC, int[])}} methods.
     * <p>
     * The input is assumed to store only the upper/right triangle of the symmetric pattern; lower-triangular
     * entries (if present) are ignored.
     */
    public void approximate(final R064CSC matrix) {

        int dimension = matrix.getColDim();
        if (dimension <= 0) {
            return;
        }

        int[] colPtr = matrix.pointers;
        int[] rowIdx = matrix.indices;

        // Build a simple symmetric adjacency structure from the (assumed upper-triangular) pattern.
        // neighbours[v] is the list of neighbours of v in the undirected graph defined by the pattern.
        int[][] neighbours = new int[dimension][];
        int[] degree = new int[dimension];

        // First pass: count undirected neighbours per vertex
        for (int col = 0; col < dimension; col++) {
            for (int p = colPtr[col]; p < colPtr[col + 1]; p++) {
                int row = rowIdx[p];
                if (row > col) {
                    continue; // assume upper triangle only
                }
                if (row == col) {
                    continue; // skip diagonal
                }
                degree[row]++;
                degree[col]++;
            }
        }

        // Allocate neighbour arrays
        for (int v = 0; v < dimension; v++) {
            neighbours[v] = new int[degree[v]];
            degree[v] = 0; // reuse as write index
        }

        // Second pass: fill neighbour lists symmetrically
        for (int col = 0; col < dimension; col++) {
            for (int p = colPtr[col]; p < colPtr[col + 1]; p++) {
                int row = rowIdx[p];
                if (row > col) {
                    continue;
                }
                if (row == col) {
                    continue;
                }
                neighbours[row][degree[row]++] = col;
                neighbours[col][degree[col]++] = row;
            }
        }

        // Reset pivot and degrees for the actual ordering loop
        myPermutation.reset(dimension);
        int[] order = myPermutation.getOrder();
        boolean[] eliminated = new boolean[dimension];

        // Recompute degrees as neighbour counts
        for (int v = 0; v < dimension; v++) {
            degree[v] = neighbours[v].length;
        }

        // Greedy minimum-degree-like selection
        for (int k = 0; k < dimension; k++) {

            int best = -1;
            int bestDegree = Integer.MAX_VALUE;

            for (int v = 0; v < dimension; v++) {
                if (!eliminated[v] && degree[v] < bestDegree) {
                    bestDegree = degree[v];
                    best = v;
                }
            }

            if (best < 0) {
                break;
            }

            // Move 'best' into position k in the Pivots order
            int posBest = -1;
            for (int pos = k; pos < dimension; pos++) {
                if (order[pos] == best) {
                    posBest = pos;
                    break;
                }
            }
            if (posBest >= 0 && posBest != k) {
                myPermutation.change(k, posBest);
            }

            eliminated[best] = true;

            // Degree update: for each neighbour of best, bump its degree to reflect potential fill among
            // its remaining neighbours. This is a lightweight approximation of AMDs external degree
            // update and uses only the local adjacency information.
            int[] nbBest = neighbours[best];
            for (int u : nbBest) {
                if (eliminated[u]) {
                    continue;
                }
                // Count remaining neighbours of u that are not yet eliminated; use that as a fresh degree.
                int newDeg = 0;
                for (int w : neighbours[u]) {
                    if (!eliminated[w] && w != u) {
                        newDeg++;
                    }
                }
                degree[u] = Math.max(degree[u], newDeg);
            }
        }
    }

    /**
     * Permutes a vector according to the computed ordering. Copies from source to destination, reordering as
     * it goes.
     */
    public void permute(final double[] destination, final double[] source) {

        int[] order = myPermutation.getOrder();

        for (int j = 0, n = Math.min(destination.length, source.length); j < n; j++) {
            destination[j] = source[order[j]];
        }
    }

    /**
     * Permutes a symmetric {@link R064CSC} matrix according to the computed ordering. The input is assumed to
     * store only the upper/right triangle of the symmetric pattern.
     * <p>
     * Does not modify the input matrix; it returns a new permuted matrix.
     */
    public R064CSC permute(final R064CSC original, final int[] recording) {

        int n = original.getColDim();

        int[] reversed = myPermutation.reverseOrder();

        int i, i2, j2;

        int[] orgPointers = original.pointers;
        int[] orgIndices = original.indices;
        double[] orgValues = original.values;

        int[] work = new int[n];

        R064CSC permuted = new R064CSC(n, n, orgPointers[n]);
        int[] permPointers = permuted.pointers;
        int[] permIndices = permuted.indices;
        double[] permValues = permuted.values;

        for (int j = 0; j < n; j++) {
            j2 = reversed[j];

            for (int p = orgPointers[j], lim = orgPointers[j + 1]; p < lim; p++) {

                i = orgIndices[p];
                i2 = reversed[i];

                work[Math.max(i2, j2)]++;
            }
        }

        int nz = 0;
        for (int i1 = 0; i1 < n; i1++) {
            permPointers[i1] = nz;
            nz += work[i1];
            work[i1] = permPointers[i1];
        }
        permPointers[n] = nz;

        int q;
        for (int j = 0; j < n; j++) {
            j2 = reversed[j];

            for (int p = orgPointers[j], lim = orgPointers[j + 1]; p < lim; p++) {

                i = orgIndices[p];
                i2 = reversed[i];

                permIndices[q = work[Math.max(i2, j2)]++] = Math.min(i2, j2);
                permValues[q] = orgValues[p];

                if (recording != null) {
                    recording[p] = q;
                }
            }
        }

        return permuted;
    }

    /**
     * The inverse permutation of a vector according to the computed ordering. Copies from source to
     * destination, reordering as it goes.
     */
    public void reverse(final double[] destination, final double[] source) {

        int[] order = myPermutation.getOrder();

        for (int j = 0, n = Math.min(destination.length, source.length); j < n; j++) {
            destination[order[j]] = source[j];
        }
    }

    int[] getOrder() {
        return myPermutation.getOrder().clone();
    }

    int[] reverseOrder() {
        return myPermutation.reverseOrder().clone();
    }

}
