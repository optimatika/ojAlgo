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
package org.ojalgo.matrix.operation;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.function.special.MissingMath;

/**
 * LAPACK {@code ?steqr}: the eigenvalues, and the plane rotations that make the eigenvectors, of a symmetric
 * tridiagonal n-by-n matrix T, by the implicit QL method with shifts:
 *
 * <pre>{@code
 * T = G * diag(d) * G ^ T
 * }</pre>
 *
 * G is the product of the plane rotations, each reported to a {@link RotateColumns} as it is made. Applied
 * from the right to Z, they make Z * G; when T = Q^T * A * Q, starting with Z = Q gives the eigenvectors of
 * A. There is only a pure Java implementation.
 */
public abstract class STEQR implements LAPACK {

    /**
     * The pure Java implementation.
     */
    static final class JVM {

        /**
         * The pure Java implementation of {@link STEQR#invoke(double[], double[], RotateColumns)}, without
         * argument checks.
         */
        static void invoke(final double[] d, final double[] e, final RotateColumns rotations) {

            int size = d.length;
            int limit = size - 1;

            double shift = ZERO;
            double increment;

            double magnitude = ZERO;
            double epsilon;

            double d_l, e_l;

            int m;
            // Main loop
            for (int l = 0; l < size; l++) {

                d_l = d[l];
                e_l = e[l];

                // Find small subdiagonal element
                magnitude = Math.max(magnitude, Math.abs(d_l) + Math.abs(e_l));
                epsilon = MACHINE_EPSILON * magnitude;

                m = l;
                while (m < limit && Math.abs(e[m]) > epsilon) {
                    m++;
                }

                // If m == l, d[l] is an eigenvalue, otherwise, iterate.
                if (l < m) {
                    do {

                        // Compute implicit shift

                        double p = (d[l + 1] - d_l) / (e_l + e_l);
                        double r = MissingMath.hypot(p, ONE);
                        if (p < ZERO) {
                            r = -r;
                        }

                        d[l + 1] = e_l * (p + r);
                        increment = d_l - (d[l] = e_l / (p + r));
                        for (int i = l + 2; i < size; i++) {
                            d[i] -= increment;
                        }
                        shift += increment;

                        // Implicit QL transformation

                        double cos1 = ONE, sin1 = ZERO, cos2 = cos1;
                        double d_i, e_i;

                        p = d[m];
                        for (int i = m - 1; i >= l; i--) {
                            d_i = d[i];
                            e_i = e[i];

                            r = MissingMath.hypot(p, e_i);

                            e[i + 1] = sin1 * r;

                            cos2 = cos1;

                            cos1 = p / r;
                            sin1 = e_i / r;

                            d[i + 1] = cos2 * p + sin1 * (cos1 * cos2 * e_i + sin1 * d_i);

                            p = cos1 * d_i - sin1 * cos2 * e_i;

                            // Accumulate transformation - rotate the eigenvector matrix
                            rotations.rotateColumns(i, i + 1, cos1, sin1);
                        }

                        d_l = d[l] = cos1 * p;
                        e_l = e[l] = sin1 * p;

                    } while (Math.abs(e[l]) > epsilon); // Check for convergence
                } // End if (m > l)

                d[l] += shift;
                e[l] = ZERO;

            } // End main loop - l
        }

    }

    /**
     * The eigenvalues of T overwrite d, in no particular order, and e is overwritten. Each plane rotation,
     * combining the columns low and high of the eigenvector matrix, is reported to the {@link RotateColumns}:
     *
     * <pre>{@code
     * z(low) := cos * z(low) - sin * z(high)
     * z(high) := sin * z(low) + cos * z(high)
     * }</pre>
     *
     * The eigenvector for d[j] is column j of the matrix the rotations are applied to.
     *
     * @param d         The diagonal of T, length n
     * @param e         The subdiagonal of T: e[i] = T(i+1,i) for i = 0, ..., n-2, and e[n-1] = 0
     * @param rotations Receives the plane rotations, {@link RotateColumns#NULL} for the eigenvalues only
     */
    public static void invoke(final double[] d, final double[] e, final RotateColumns rotations) {

        ArrayOperation.checkArgument("STEQR", "e", e.length >= d.length);

        JVM.invoke(d, e, rotations);
    }

}
