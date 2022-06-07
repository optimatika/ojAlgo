/*
 * Copyright 1997-2022 Optimatika
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
package org.ojalgo.random.scedasticity;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.Arrays;

abstract class AbstractScedasticity implements ScedasticityModel {

    static final double DEFAULT_VARIANCE = HUNDREDTH * HUNDREDTH;

    /**
     * Will set the array of weights to equal weights (average values)
     *
     * @param weights Array of weights (to be set)
     * @param total The total the weights should sum up to
     */
    static void average(final double[] weights, final double total) {
        Arrays.fill(weights, total / weights.length);
    }

    /**
     * Will set the array of weights to decreasing weights (halved next values)
     *
     * @param weights Array of weights (to be set)
     * @param total The total the weights should sum up to
     */
    static void decreasing(final double[] weights, final double total) {
    
        int length = weights.length;
        double weight = total;
    
        if (length == 1) {
            weights[0] = weight;
        } else if (length != 0) {
            for (int i = 0; i < length; i++) {
                weight /= TWO;
                weights[i] = weight;
            }
            weights[length - 1] = weights[length - 2];
        }
    }

    AbstractScedasticity() {
        super();
    }

}
