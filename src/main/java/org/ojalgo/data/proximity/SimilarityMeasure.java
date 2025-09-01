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
 * Similarity measures quantify how alike two items are. Unlike distances (where smaller is closer), larger
 * similarity values indicate greater affinity. Many similarities are bounded in [0,1] with 1 meaning
 * identical, but some (e.g., correlation or dot product) can be negative or unbounded. Unless documented
 * otherwise, similarities here are symmetric. Conversions between similarity and distance often exist via
 * monotone transforms, but metric or kernel properties are not guaranteed by such transforms.
 */
public enum SimilarityMeasure {

    /**
     * Cosine similarity s = (a·b) / (||a||·||b||). Measures the angle between vectors; insensitive to uniform
     * scaling. Range is [-1, 1] in general and [0, 1] for non-negative data.
     */
    COSINE,

    /**
     * Pearson correlation coefficient between two vectors, equivalent to cosine similarity after mean
     * centering each vector. Range [-1, 1]; invariant to affine scaling of each vector.
     */
    PEARSON,

    /**
     * Linear similarity s = a·b (dot product). Unbounded and sensitive to scale; equals the linear kernel.
     * Useful when magnitudes carry meaning.
     */
    LINEAR,

    /**
     * Bhattacharyya coefficient for non‑negative vectors (often probability distributions): s = Σ_i sqrt(a_i
     * b_i). Range [0, 1] when inputs are normalised to sum to 1.
     */
    BHATTACHARYYA,

    /**
     * Jaccard similarity for sets or binary vectors: s = |A ∩ B| / |A ∪ B|. For sparse indicator vectors it
     * can be interpreted over the set of non-zero indices. Range [0, 1].
     */
    JACCARD,

    /**
     * Tanimoto (extended Jaccard) similarity for non-negative real vectors: s = (a·b) / (||a||² + ||b||² −
     * a·b). Reduces to Jaccard for binary vectors. Range [0, 1] when inputs are non-negative.
     */
    TANIMOTO,

    /**
     * Sørensen–Dice coefficient: s = 2|A ∩ B| / (|A| + |B|). Closely related to Jaccard and often preferred
     * with imbalanced set sizes. Range [0, 1].
     */
    DICE,

    /**
     * Overlap (Szymkiewicz–Simpson) coefficient: s = |A ∩ B| / min(|A|, |B|). Emphasises subset relations;
     * equals 1 when one set is contained in the other. Range [0, 1].
     */
    OVERLAP,

    /**
     * Simple matching coefficient (Sokal–Michener) for binary vectors: fraction of coordinates that match
     * (both 0 or both 1). Equivalent to 1 − normalised Hamming distance. Range [0, 1].
     */
    SIMPLE_MATCHING;

}