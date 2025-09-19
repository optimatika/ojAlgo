package org.ojalgo.matrix;

import java.util.Objects;

/**
 * Similarity / distance utilities commonly used in ML and information retrieval.
 * Provides cosine similarity ([-1,1]) and cosine distance (1 - cosine similarity).
 *
 * Overloads are provided for both raw {@code double[]} and {@link MatrixR064}
 * (treated as linearised 1D vectors using their element order).
 */
public final class Similarity {

    private Similarity() {
        // utility class
    }

    // ---------------------------------------------------------------------
    // Public API: double[] overloads (most convenient for callers)
    // ---------------------------------------------------------------------

    /**
     * Cosine similarity between two equal-length vectors.
     *
     * @param a vector a
     * @param b vector b
     * @return cosine similarity in [-1, 1]
     * @throws NullPointerException if a or b is null
     * @throws IllegalArgumentException if lengths differ or either vector has zero norm
     */
    public static double cosine(final double[] a, final double[] b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        if (a.length != b.length) {
            throw new IllegalArgumentException("Dimension mismatch: " + a.length + " vs " + b.length);
        }
        double dot = 0.0;
        double na2 = 0.0;
        double nb2 = 0.0;
        for (int i = 0; i < a.length; i++) {
            final double ai = a[i];
            final double bi = b[i];
            dot += ai * bi;
            na2 += ai * ai;
            nb2 += bi * bi;
        }
        if (na2 == 0.0 || nb2 == 0.0) {
            throw new IllegalArgumentException("Zero-norm vector; cosine undefined");
        }
        return dot / (Math.sqrt(na2) * Math.sqrt(nb2));
    }

    /** Cosine distance = 1 - cosine similarity. Range [0, 2]. */
    public static double cosineDistance(final double[] a, final double[] b) {
        return 1.0 - cosine(a, b);
    }

    // ---------------------------------------------------------------------
    // Public API: MatrixR064 overloads (treat as 1D vectors in linear order)
    // ---------------------------------------------------------------------

    /**
     * Cosine similarity between 2 matrices treated as flattened vectors.
     *
     * @param a matrix a
     * @param b matrix b
     * @return cosine similarity in [-1, 1]
     * @throws NullPointerException if a or b is null
     * @throws IllegalArgumentException if element counts differ or either has zero norm
     */
    public static double cosine(final MatrixR064 a, final MatrixR064 b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        final long n = a.count();
        if (n != b.count()) {
            throw new IllegalArgumentException("Element count mismatch: " + n + " vs " + b.count());
        }
        double dot = 0.0;
        double na2 = 0.0;
        double nb2 = 0.0;
        for (long i = 0; i < n; i++) {
            final double ai = a.doubleValue(i);
            final double bi = b.doubleValue(i);
            dot += ai * bi;
            na2 += ai * ai;
            nb2 += bi * bi;
        }
        if (na2 == 0.0 || nb2 == 0.0) {
            throw new IllegalArgumentException("Zero-norm vector; cosine undefined");
        }
        return dot / (Math.sqrt(na2) * Math.sqrt(nb2));
    }

    /** Cosine distance for matrices = 1 - cosine similarity. */
    public static double cosineDistance(final MatrixR064 a, final MatrixR064 b) {
        return 1.0 - cosine(a, b);
    }
}
