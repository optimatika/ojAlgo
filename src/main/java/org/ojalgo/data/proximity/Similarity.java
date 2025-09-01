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

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.util.Objects;

import org.ojalgo.structure.Access1D;

/**
 * Similarity / distance utilities commonly used in ML and information retrieval. Provides cosine similarity
 * ([-1,1]) and cosine distance (1 - cosine similarity). Overloads are provided for both raw {@code double[]}
 * and {@link Access1D} (treated as linearised 1D vectors using their element order).
 */
public abstract class Similarity {

    /**
     * Cosine similarity between 2 matrices treated as flattened vectors.
     *
     * @param a matrix a
     * @param b matrix b
     * @return cosine similarity in [-1, 1]
     * @throws NullPointerException     if a or b is null
     * @throws IllegalArgumentException if element counts differ or either has zero norm
     */
    public static double cosine(final Access1D<?> a, final Access1D<?> b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        int n = a.size();
        if (n != b.size()) {
            throw new IllegalArgumentException("Element count mismatch: " + n + " vs " + b.count());
        }
        double dot = ZERO;
        double na2 = ZERO;
        double nb2 = ZERO;
        for (long i = 0; i < n; i++) {
            double ai = a.doubleValue(i);
            double bi = b.doubleValue(i);
            dot += ai * bi;
            na2 += ai * ai;
            nb2 += bi * bi;
        }
        if (na2 == ZERO || nb2 == ZERO) {
            throw new IllegalArgumentException("Zero-norm vector; cosine undefined");
        }
        return dot / (Math.sqrt(na2) * Math.sqrt(nb2));
    }

    /**
     * Cosine similarity between two equal-length vectors.
     *
     * @param a vector a
     * @param b vector b
     * @return cosine similarity in [-1, 1]
     * @throws NullPointerException     if a or b is null
     * @throws IllegalArgumentException if lengths differ or either vector has zero norm
     */
    public static double cosine(final double[] a, final double[] b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        if (a.length != b.length) {
            throw new IllegalArgumentException("Dimension mismatch: " + a.length + " vs " + b.length);
        }
        double dot = ZERO;
        double na2 = ZERO;
        double nb2 = ZERO;
        for (int i = 0; i < a.length; i++) {
            double ai = a[i];
            double bi = b[i];
            dot += ai * bi;
            na2 += ai * ai;
            nb2 += bi * bi;
        }
        if (na2 == ZERO || nb2 == ZERO) {
            throw new IllegalArgumentException("Zero-norm vector; cosine undefined");
        }
        return dot / (Math.sqrt(na2) * Math.sqrt(nb2));
    }

}
