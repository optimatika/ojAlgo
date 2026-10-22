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
package org.ojalgo.array.operation;

import static org.ojalgo.function.constant.PrimitiveMath.ONE;
import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.util.Arrays;

/**
 * BLAS level 1 {@code ?scal}: x := alpha*x
 */
public abstract class SCAL implements BLAS1 {

    /**
     * The pure Java implementation.
     */
    public static final class JVM {

        /**
         * The pure Java implementation of {@link SCAL#invoke(int, double, double[], int, int)}, for
         * {@code n > 0} and {@code incX > 0}.
         */
        public static void invoke(final int n, final double alpha, final double[] x, final int offsetX, final int incX) {

            if (incX == 1) {
                for (int i = offsetX, limit = offsetX + n; i < limit; i++) {
                    x[i] *= alpha;
                }
            } else {
                for (int i = 0, ix = offsetX; i < n; i++, ix += incX) {
                    x[ix] *= alpha;
                }
            }
        }

    }

    /**
     * BLAS {@code dscal}: x := alpha*x for the {@code n} elements of {@code x}, from {@code offsetX} with
     * increment {@code incX}. As in the reference implementation, nothing is done unless {@code n} and
     * {@code incX} are positive.
     */
    public static void invoke(final int n, final double alpha, final double[] x, final int offsetX, final int incX) {

        if (n <= 0 || incX <= 0) {
            return;
        }

        JVM.invoke(n, alpha, x, offsetX, incX);
    }

    /**
     * Scales the consecutive elements {@code x[first]} to {@code x[limit - 1]} the way level 3 BLAS scales by
     * alpha or beta: a factor of 0 sets them to zero without reading them (NaN or infinite values are not
     * propagated), and a factor of 1 leaves them untouched.
     */
    public static void scale(final double[] x, final int first, final int limit, final double factor) {
        if (factor == ZERO) {
            Arrays.fill(x, first, limit, ZERO);
        } else if (factor != ONE) {
            for (int i = first; i < limit; i++) {
                x[i] *= factor;
            }
        }
    }

    /**
     * Scales the {@code n} elements of {@code x}, stored from {@code offsetX} (the lowest index) with
     * increment {@code incX}, the way level 2 and 3 BLAS scale by beta: a factor of 0 sets them to zero
     * without reading them, and a factor of 1 leaves them untouched. The sign of the increment does not
     * matter.
     */
    public static void scale(final int n, final double factor, final double[] x, final int offsetX, final int incX) {
        int step = Math.abs(incX);
        if (step == 1) {
            SCAL.scale(x, offsetX, offsetX + n, factor);
        } else if (factor == ZERO) {
            for (int i = 0, ix = offsetX; i < n; i++, ix += step) {
                x[ix] = ZERO;
            }
        } else if (factor != ONE) {
            for (int i = 0, ix = offsetX; i < n; i++, ix += step) {
                x[ix] *= factor;
            }
        }
    }

}
