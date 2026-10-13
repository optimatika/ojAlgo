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
     * <p>
     * A 2-by-2 block that splits off is diagonalised in closed form, as by LAPACK's {@code ?laev2}, rather
     * than iterated on. The plane rotations, and the shifts, use sqrt(x<sup>2</sup>+y<sup>2</sup>) directly
     * when neither square can overflow or underflow, as LAPACK's {@code ?lartg} does, and only otherwise
     * {@link MissingMath#hypot(double, double)} (scaled, with a division more on the critical path).
     */
    static final class JVM {

        /**
         * sqrt(2<sup>1021</sup>), a square below it can't overflow, nor a sum of two such squares
         */
        private static final double ROOT_LARGEST = Math.sqrt(ONE / MACHINE_SMALLEST / TWO);
        /**
         * sqrt(2<sup>-1022</sup>), a square above it is a normal number
         */
        private static final double ROOT_SMALLEST = Math.sqrt(MACHINE_SMALLEST);

        /**
         * sqrt(x<sup>2</sup>+y<sup>2</sup>), directly when both are within the safe range, otherwise scaled.
         */
        private static double hypot(final double x, final double y) {
            double absX = Math.abs(x);
            double absY = Math.abs(y);
            if (absX > ROOT_SMALLEST && absX < ROOT_LARGEST && absY > ROOT_SMALLEST && absY < ROOT_LARGEST) {
                return Math.sqrt(x * x + y * y);
            }
            return MissingMath.hypot(x, y);
        }

        /**
         * LAPACK {@code ?laev2}: the eigenvalues of the 2-by-2 block [d(l), e(l); e(l), d(l+1)] overwrite
         * d(l) and d(l+1), the larger in absolute value first, and the rotation that diagonalises it is
         * reported for the columns l and l+1.
         */
        private static void solve2x2(final double[] d, final double[] e, final int l, final RotateColumns rotations) {

            double a = d[l];
            double b = e[l];
            double c = d[l + 1];

            double sum = a + c;
            double difference = a - c;
            double absDifference = Math.abs(difference);
            double twoB = b + b;
            double absTwoB = Math.abs(twoB);

            double larger, smaller;
            if (Math.abs(a) > Math.abs(c)) {
                larger = a;
                smaller = c;
            } else {
                larger = c;
                smaller = a;
            }

            double root;
            if (absDifference > absTwoB) {
                double ratio = absTwoB / absDifference;
                root = absDifference * Math.sqrt(ONE + ratio * ratio);
            } else if (absDifference < absTwoB) {
                double ratio = absDifference / absTwoB;
                root = absTwoB * Math.sqrt(ONE + ratio * ratio);
            } else {
                root = absTwoB * SQRT_TWO;
            }

            double first, second;
            boolean negative;
            if (sum < ZERO) {
                first = HALF * (sum - root);
                // The smaller one from the determinant, to avoid cancellation
                second = larger / first * smaller - b / first * b;
                negative = true;
            } else if (sum > ZERO) {
                first = HALF * (sum + root);
                second = larger / first * smaller - b / first * b;
                negative = false;
            } else {
                first = HALF * root;
                second = -HALF * root;
                negative = false;
            }

            // The eigenvector (cos, sin) of the first eigenvalue
            double cos, sin;
            double tmp = difference >= ZERO ? difference + root : difference - root;
            if (Math.abs(tmp) > absTwoB) {
                double cot = -twoB / tmp;
                sin = ONE / Math.sqrt(ONE + cot * cot);
                cos = cot * sin;
            } else if (absTwoB == ZERO) {
                cos = ONE;
                sin = ZERO;
            } else {
                double tan = -tmp / twoB;
                cos = ONE / Math.sqrt(ONE + tan * tan);
                sin = tan * cos;
            }
            if (negative != (difference >= ZERO)) {
                tmp = cos;
                cos = -sin;
                sin = tmp;
            }

            d[l] = first;
            d[l + 1] = second;
            e[l] = ZERO;

            // z(l) := cos * z(l) + sin * z(l+1), z(l+1) := -sin * z(l) + cos * z(l+1)
            rotations.rotateColumns(l, l + 1, cos, -sin);
        }

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

                // If m == l, d[l] is an eigenvalue, if m == l + 1, solve the 2-by-2 block, otherwise iterate.
                if (m == l + 1) {

                    JVM.solve2x2(d, e, l, rotations);

                } else if (l < m) {
                    do {

                        // Compute implicit shift

                        double p = (d[l + 1] - d_l) / (e_l + e_l);
                        double r = JVM.hypot(p, ONE);
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

                            r = JVM.hypot(p, e_i);

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
