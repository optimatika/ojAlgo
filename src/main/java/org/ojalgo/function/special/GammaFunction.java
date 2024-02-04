/*
 * Copyright 1997-2024 Optimatika
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
package org.ojalgo.function.special;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.function.constant.ComplexMath;
import org.ojalgo.scalar.ComplexNumber;

public abstract class GammaFunction {

    public static abstract class Incomplete extends GammaFunction {

        public static ComplexNumber lower(final ComplexNumber z, final double limit) {
            // TODO Implement it!
            return ComplexNumber.NaN;
        }

        public static double lower(final double x, final double limit) {

            double base = Math.exp(-limit) * Math.pow(limit, x);

            if (Math.abs(base) < MACHINE_EPSILON) {
                return ZERO;
            }

            double numerator = ONE, denominator = x;

            double sum = numerator / denominator;
            for (int i = 1; i < 100; i++) {
                numerator *= limit;
                denominator *= (x + i);
                sum += (numerator / denominator);
            }

            return base * sum;

        }

        public static double lower(final int n, final double limit) {

            double incr = ONE, sum = ONE;
            for (int k = 1; k < n; k++) {
                incr *= (limit / k);
                sum += incr;
            }

            return (ONE - (sum * Math.exp(-limit))) * MissingMath.factorial(n - 1);
        }

        public static ComplexNumber upper(final ComplexNumber z, final double limit) {
            // TODO Implement it!
            return ComplexNumber.NaN;
        }

        public static double upper(final double x, final double limit) {
            // TODO Implement it!
            return GammaFunction.gamma(x) - Incomplete.lower(x, limit);
        }

        public static double upper(final int n, final double limit) {

            double incr = ONE, sum = ONE;
            for (int k = 1; k < n; k++) {
                incr *= (limit / k);
                sum += incr;
            }

            return sum * Math.exp(-limit) * MissingMath.factorial(n - 1);
        }

    }

    public static abstract class Logarithmic extends GammaFunction {

        public static ComplexNumber gamma(final ComplexNumber z) {
            return GammaFunction.LanczosApproximation.logarithmic(z);
        }

        public static double gamma(final double x) {
            return GammaFunction.LanczosApproximation.logarithmic(x);
        }

        public static double gamma(final int n) {
            return Math.log(GammaFunction.gamma(n));
        }

    }

    public static abstract class Regularized extends GammaFunction {

        public static ComplexNumber lower(final ComplexNumber z, final double limit) {
            return GammaFunction.Incomplete.lower(z, limit).divide(GammaFunction.gamma(z));
        }

        public static double lower(final double x, final double limit) {
            return GammaFunction.Incomplete.lower(x, limit) / GammaFunction.gamma(x);
        }

        public static double lower(final int n, final double limit) {
            return GammaFunction.Incomplete.lower(n, limit) / GammaFunction.gamma(n);
        }

        public static ComplexNumber upper(final ComplexNumber z, final double limit) {
            return GammaFunction.Incomplete.upper(z, limit).divide(GammaFunction.gamma(z));
        }

        public static double upper(final double x, final double limit) {
            return GammaFunction.Incomplete.upper(x, limit) / GammaFunction.gamma(x);
        }

        public static double upper(final int n, final double limit) {
            return GammaFunction.Incomplete.upper(n, limit) / GammaFunction.gamma(n);
        }

    }

    /**
     * Lanczos approximation. The abritray constant is 7, and there are 9 coefficients used.
     *
     * <pre>
     * http://en.wikipedia.org/wiki/Lanczos_approximation
     * http://mathworld.wolfram.com/LanczosApproximation.html
     * https://mrob.com/pub/ries/lanczos-gamma.html
     * </pre>
     */
    static abstract class LanczosApproximation {

        /**
         * Arbitrary constant
         */
        private static final double A = SEVEN;
        /**
         * Coefficients
         */
        private static final double[] C = { 0.99999999999980993227684700473478, 676.520368121885098567009190444019, -1259.13921672240287047156078755283,
                771.3234287776530788486528258894, -176.61502916214059906584551354, 12.507343278686904814458936853, -0.13857109526572011689554707,
                9.984369578019570859563e-6, 1.50563273514931155834e-7 };
        private static final double LOG_SQRT_TWO_PI = LOG.invoke(SQRT_TWO_PI);

        static ComplexNumber gamma(final ComplexNumber z) {

            double zr = z.getReal();

            if ((zr <= ZERO) && (ABS.invoke(zr % ONE) < MACHINE_EPSILON)) {

                return ComplexNumber.NaN;

            } else {

                if (zr < HALF) {

                    return ComplexMath.SIN.invoke(z.multiply(PI)).multiply(GammaFunction.gamma(ComplexNumber.ONE.subtract(z))).invert().multiply(PI);

                } else {

                    final ComplexNumber z1 = z.subtract(ONE);
                    final ComplexNumber za = z1.add(A + HALF);

                    ComplexNumber zs = ComplexNumber.valueOf(C[0]);
                    for (int i = C.length - 1; i > 0; i--) {
                        zs = zs.add(z1.add(i).invert().multiply(C[i]));
                    }

                    return ComplexMath.POW.invoke(za, z1.add(HALF)).multiply(SQRT_TWO_PI).multiply(ComplexMath.EXP.invoke(za.negate())).multiply(zs);
                }
            }
        }

        static double gamma(final double x) {

            if ((x <= ZERO) && (ABS.invoke(x % ONE) < MACHINE_EPSILON)) {

                return NaN;

            } else {

                if (x < HALF) {

                    return PI / (SIN.invoke(PI * x) * GammaFunction.gamma(ONE - x));

                } else {

                    final double x1 = x - ONE;
                    final double xa = x1 + (A + HALF);

                    double xs = C[0];
                    for (int i = C.length - 1; i > 0; i--) {
                        xs += C[i] / (x1 + i);
                    }

                    return POW.invoke(xa, x1 + HALF) * SQRT_TWO_PI * EXP.invoke(-xa) * xs;
                }
            }
        }

        static ComplexNumber logarithmic(final ComplexNumber z) {

            final ComplexNumber z1 = z.subtract(ONE);
            final ComplexNumber za = z1.add(A + HALF);

            ComplexNumber zs = ComplexNumber.valueOf(C[0]);
            for (int i = C.length - 1; i > 0; i--) {
                zs = zs.add(z1.add(i).invert().multiply(C[i]));
            }

            return z1.add(HALF).multiply(ComplexMath.LOG.invoke(za)).add(LOG_SQRT_TWO_PI).subtract(za).add(ComplexMath.LOG.invoke(zs));
        }

        static double logarithmic(final double x) {

            final double x1 = x - ONE;
            final double xa = x1 + (A + HALF);

            double xs = C[0];
            for (int i = C.length - 1; i > 0; i--) {
                xs += C[i] / (x1 + i);
            }

            return ((((x1 + HALF) * LOG.invoke(xa)) + LOG_SQRT_TWO_PI) - xa) + LOG.invoke(xs);
        }

    }

    public static ComplexNumber gamma(final ComplexNumber z) {
        return GammaFunction.LanczosApproximation.gamma(z);
    }

    public static double gamma(final double x) {
        return GammaFunction.LanczosApproximation.gamma(x);
    }

    public static double gamma(final int n) {
        if (n < 1) {
            throw new IllegalArgumentException();
        }
        return MissingMath.factorial(n - 1);
    }

}
