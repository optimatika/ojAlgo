/*
 * Copyright 1997-2026 Optimatika
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
 * A fill-reducing ordering of a symmetric sparse matrix, prior to numerical factorisation (Cholesky or LDL):
 * the rows/columns in order of increasing degree (the number of off-diagonal nonzeros in the symmetric
 * pattern), ties in index order. The degrees are those of the original matrix. They are not updated as the
 * elimination proceeds, as they are in a (true or approximate) minimum degree algorithm such as AMD.
 * <p>
 * The input {@link R064CSC} is assumed to represent a symmetric pattern with only the upper triangle stored.
 * It is not modified.
 */
public final class MinimumDegree {

    private final Pivot myPermutation = new Pivot();

    /**
     * Computes the ordering of a symmetric {@link R064CSC} matrix. The result is stored internally. To
     * permute vectors or matrices according to the computed ordering, use the
     * {@link #permute(double[], double[])} or {@link #permute(R064CSC, int[])} methods.
     * <p>
     * The input is assumed to store only the upper/right triangle of the symmetric pattern; lower-triangular
     * entries (if present) are ignored.
     */
    public void approximate(final R064CSC matrix) {

        int dimension = matrix.getColDim();
        if (dimension <= 0) {
            return;
        }

        int[] degree = new int[dimension];

        for (int col = 0; col < dimension; col++) {
            for (int p = matrix.pointers[col]; p < matrix.pointers[col + 1]; p++) {
                int row = matrix.indices[p];
                if (row < col) {
                    degree[row]++;
                    degree[col]++;
                }
            }
        }

        int maxDegree = 0;
        for (int v = 0; v < dimension; v++) {
            maxDegree = Math.max(maxDegree, degree[v]);
        }

        // Counting sort: the vertices with degree d start at start[d]
        int[] start = new int[maxDegree + 2];
        for (int v = 0; v < dimension; v++) {
            start[degree[v] + 1]++;
        }
        for (int d = 0; d <= maxDegree; d++) {
            start[d + 1] += start[d];
        }

        myPermutation.reset(dimension);
        int[] order = myPermutation.getOrder();
        for (int v = 0; v < dimension; v++) {
            order[start[degree[v]]++] = v;
        }
        myPermutation.setModified(true);
    }

    /**
     * Permutes a vector according to the computed ordering. Copies from source to destination, reordering as
     * it goes.
     */
    public void permute(final double[] destination, final double[] source) {

        int[] order = myPermutation.getOrder();

        for (int j = 0, n = Math.min(destination.length, order.length); j < n; j++) {
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

        for (int j = 0, n = Math.min(order.length, source.length); j < n; j++) {
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
