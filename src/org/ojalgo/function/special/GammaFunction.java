/*
 * Copyright 1997-2019 Optimatika
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

import org.ojalgo.function.constant.PrimitiveMath;

public abstract class GammaFunction {

    /**
     * For the Lanczos approximation of the gamma function
     */
    private static final double[] L9 = { 0.99999999999980993227684700473478, 676.520368121885098567009190444019, -1259.13921672240287047156078755283,
            771.3234287776530788486528258894, -176.61502916214059906584551354, 12.507343278686904814458936853, -0.13857109526572011689554707,
            9.984369578019570859563e-6, 1.50563273514931155834e-7 };

    /**
     * Lanczos approximation. The abritray constant is 7, and there are 9 coefficients used. Essentially the
     * algorithm is taken from <a href="http://en.wikipedia.org/wiki/Lanczos_approximation">WikipediA</a> ,
     * but it's modified a bit and I found more exact coefficients somewhere else.
     */
    public static double gamma(final double arg) {

        if ((arg <= ZERO) && (PrimitiveMath.ABS.invoke(arg % ONE) < MACHINE_EPSILON)) {

            return NaN;

        } else {

            if (arg < HALF) {

                return PI / (PrimitiveMath.SIN.invoke(PI * arg) * GammaFunction.gamma(ONE - arg));

            } else {

                final double z = arg - ONE;

                double x = GammaFunction.L9[0];
                for (int i = 1; i < GammaFunction.L9.length; i++) {
                    x += GammaFunction.L9[i] / (z + i);
                }

                final double t = z + (7 + HALF);

                return SQRT_TWO_PI * PrimitiveMath.POW.invoke(t, z + HALF) * PrimitiveMath.EXP.invoke(-t) * x;
            }
        }
    }

    private GammaFunction() {
    }

}
