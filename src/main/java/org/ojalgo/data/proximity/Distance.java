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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.structure.Access1D;

public abstract class Distance {

    public static double angular(final float[] a, final float[] b) {

        double dot = ZERO;
        double normA = ZERO;
        double normB = ZERO;

        for (int i = 0, limit = Math.min(a.length, b.length); i < limit; i++) {
            double va = a[i];
            double vb = b[i];
            dot += va * vb;
            normA += va * va;
            normB += vb * vb;
        }
        if (normA == ZERO && normB == ZERO) {
            return ZERO; // define angle between two zero vectors as 0
        }
        if (normA == ZERO || normB == ZERO) {
            return Math.PI / 2.0; // undefined angle; treat as orthogonal
        }
        double cosine = dot / (Math.sqrt(normA) * Math.sqrt(normB));
        double clamped = Math.max(-1.0, Math.min(1.0, cosine));
        return Math.acos(clamped);
    }

    public static double canberra(final float[] a, final float[] b) {

        double sum = ZERO;
        int min = Math.min(a.length, b.length);

        for (int i = 0; i < min; i++) {
            double ai = Math.abs(a[i]);
            double bi = Math.abs(b[i]);
            double denom = ai + bi;
            if (denom > ZERO) {
                sum += Math.abs(a[i] - b[i]) / denom;
            }
            // else 0/0 -> contribute 0
        }
        // handle tail in longer vector as distance to implicit zeros
        if (a.length > min) {
            for (int i = min; i < a.length; i++) {
                double ai = Math.abs(a[i]);
                if (ai > ZERO) {
                    sum += 1.0; // |ai-0|/(|ai|+0) = 1
                }
            }
        } else if (b.length > min) {
            for (int i = min; i < b.length; i++) {
                double bi = Math.abs(b[i]);
                if (bi > ZERO) {
                    sum += 1.0;
                }
            }
        }
        return sum;
    }

    public static double chebyshev(final float[] a, final float[] b) {

        double max = ZERO;

        for (int i = 0, limit = Math.min(a.length, b.length); i < limit; i++) {
            double diff = Math.abs(a[i] - b[i]);
            if (diff > max) {
                max = diff;
            }
        }
        return max;
    }

    public static double correlation(final float[] a, final float[] b) {

        int n = Math.min(a.length, b.length);
        if (n == 0) {
            return ZERO;
        }

        double meanA = ZERO, meanB = ZERO;
        for (int i = 0; i < n; i++) {
            meanA += a[i];
            meanB += b[i];
        }
        meanA /= n;
        meanB /= n;

        double num = ZERO, va = ZERO, vb = ZERO;
        for (int i = 0; i < n; i++) {
            double da = a[i] - meanA;
            double db = b[i] - meanB;
            num += da * db;
            va += da * da;
            vb += db * db;
        }
        if (va == ZERO && vb == ZERO) {
            return ZERO; // both constant -> treat as identical
        }
        if (va == ZERO || vb == ZERO) {
            return ONE; // one constant, the other not -> maximally dissimilar
        }
        double rho = num / Math.sqrt(va * vb);
        double clamped = Math.max(-1.0, Math.min(1.0, rho));
        return ONE - clamped;
    }

    /** Cosine distance for matrices = 1 - cosine similarity. */
    public static double cosine(final Access1D<?> a, final Access1D<?> b) {
        return ONE - Similarity.cosine(a, b);
    }

    /** Cosine distance = 1 - cosine similarity. Range [0, 2]. */
    public static double cosine(final double[] a, final double[] b) {
        return ONE - Similarity.cosine(a, b);
    }

    public static double cosine(final float[] a, final float[] b) {

        double dot = ZERO;
        double normA = ZERO;
        double normB = ZERO;

        for (int i = 0, limit = Math.min(a.length, b.length); i < limit; i++) {
            double va = a[i];
            double vb = b[i];
            dot += va * vb;
            normA += va * va;
            normB += vb * vb;
        }
        if (normA == ZERO && normB == ZERO) {
            return ZERO; // identical zero vectors
        }
        if (normA == ZERO || normB == ZERO) {
            return ONE; // maximal distance wrt cosine when one is zero and the other is not
        }
        double similarity = dot / (Math.sqrt(normA) * Math.sqrt(normB));
        double sim = Math.max(NEG, Math.min(ONE, similarity));
        return ONE - sim; // convert similarity to distance
    }

    public static double euclidean(final float[] a, final float[] b) {
        return Math.sqrt(Distance.squaredEuclidean(a, b));
    }

    public static double hamming(final float[] a, final float[] b) {

        int count = 0;

        for (int i = 0, limit = Math.min(a.length, b.length); i < limit; i++) {
            if (a[i] != b[i]) {
                count++;
            }
        }
        count += Math.abs(a.length - b.length);
        return count;
    }

    public static double hellinger(final float[] a, final float[] b) {

        double sum = ZERO;
        int min = Math.min(a.length, b.length);

        for (int i = 0; i < min; i++) {
            double sa = a[i] > 0F ? Math.sqrt(a[i]) : ZERO;
            double sb = b[i] > 0F ? Math.sqrt(b[i]) : ZERO;
            double diff = sa - sb;
            sum += diff * diff;
        }
        if (a.length > min) {
            for (int i = min; i < a.length; i++) {
                double sa = a[i] > 0F ? Math.sqrt(a[i]) : ZERO;
                sum += sa * sa;
            }
        } else if (b.length > min) {
            for (int i = min; i < b.length; i++) {
                double sb = b[i] > 0F ? Math.sqrt(b[i]) : ZERO;
                sum += sb * sb;
            }
        }
        return Math.sqrt(sum) / Math.sqrt(2.0);
    }

    public static double jaccard(final float[] a, final float[] b) {

        int intersection = 0;
        int union = 0;

        for (int i = 0, limit = Math.max(a.length, b.length); i < limit; i++) {
            boolean inA = i < a.length && a[i] != 0F;
            boolean inB = i < b.length && b[i] != 0F;
            if (inA && inB) {
                intersection++;
                union++;
            } else if (inA || inB) {
                union++;
            }
        }
        if (union == 0) {
            return ZERO;
        }
        double similarity = (double) intersection / (double) union;
        return ONE - similarity;
    }

    public static double manhattan(final float[] a, final float[] b) {

        double sum = ZERO;

        for (int i = 0, limit = Math.min(a.length, b.length); i < limit; i++) {
            sum += Math.abs(a[i] - b[i]);
        }
        return sum;
    }

    public static double squaredEuclidean(final float[] a, final float[] b) {

        double sum = ZERO;

        for (int i = 0, limit = Math.min(a.length, b.length); i < limit; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return sum;
    }

}
