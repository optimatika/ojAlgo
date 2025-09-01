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
package org.ojalgo.data.proximity;

/**
 * Enumeration of common distance and dissimilarity measures for coordinate feature vectors.
 * <p>
 * A distance assigns a non-negative number to each pair of points; smaller means more alike. Measures that
 * satisfy the metric axioms (non-negativity; identity of indiscernibles; symmetry; triangle inequality) are
 * metrics; others listed here may be semimetrics but remain useful in practice.
 */
public enum DistanceMeasure {

    /**
     * Angular distance arccos((a·b)/(||a||·||b||)). A metric on the unit sphere; values in [0, π].
     */
    ANGULAR,
    /**
     * Canberra distance: Σ_i |a_i − b_i| / (|a_i| + |b_i|), with 0/0 taken as 0 per term. Emphasises small
     * values; range [0, ∞). A metric under the conventional definition.
     */
    CANBERRA,
    /**
     * Chebyshev (L-infinity) norm – maximum absolute coordinate difference.
     */
    CHEBYSHEV,
    /**
     * Correlation distance defined as 1 − ρ, where ρ is the Pearson correlation between coordinate vectors
     * (after centering). Invariant to affine scaling per vector. Not a metric in general.
     */
    CORRELATION,
    /**
     * Cosine distance defined as 1 − (a·b)/(||a||·||b||). Range [0, 2]. Not a metric.
     */
    COSINE,
    /**
     * Euclidean (L2) norm of the coordinate differences; a metric.
     */
    EUCLIDEAN,
    /**
     * Hamming distance counts coordinate mismatches; arrays of different length incur the absolute length
     * difference as additional mismatches. Range [0, ∞). A metric in its standard domain.
     */
    HAMMING,
    /**
     * Hellinger distance for non-negative vectors: H = (1/√2) · ||√a − √b||₂. For probability vectors it lies
     * in [0, 1] and satisfies the metric axioms.
     */
    HELLINGER,
    /**
     * Jaccard distance on the sets of non-zero coordinate indices: 1 − |A ∩ B|/|A ∪ B|. Range [0, 1]; a
     * metric on sets (binary incidence).
     */
    JACCARD,
    /**
     * Manhattan (L1) norm – sum of absolute coordinate differences; a metric.
     */
    MANHATTAN,
    /**
     * Sum of squared coordinate differences. Common in clustering (e.g. k‑means) because it preserves order
     * with respect to Euclidean and is cheaper to compute, but it is not a metric (triangle inequality
     * fails).
     */
    SQUARED_EUCLIDEAN;

}