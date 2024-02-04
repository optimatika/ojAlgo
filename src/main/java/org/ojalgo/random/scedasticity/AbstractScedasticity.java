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
package org.ojalgo.random.scedasticity;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.Arrays;

import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.structure.Access1D;

abstract class AbstractScedasticity implements ScedasticityModel {

    static final double DEFAULT_VARIANCE = HUNDREDTH * HUNDREDTH;
    static final double ELEVEN_TWELFTHS = ELEVEN / TWELVE;

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

    static Access1D<?> parameters(final Access1D<?> series, final double mean, final int q) {

        int nbVars = q;
        int nbEquations = series.size();

        if (q + nbVars > nbEquations) {
            throw new IllegalArgumentException();
        }

        Primitive64Store body = Primitive64Store.FACTORY.make(nbEquations, nbVars);
        Primitive64Store rhs = Primitive64Store.FACTORY.make(nbEquations, 1);

        for (int i = 0; i < nbEquations; i++) {

            double value = series.doubleValue(i);
            double error = value - mean;
            double squared = error * error;

            rhs.set(i, squared);

            body.fillDiagonal(i + 1, 0, squared);
        }

        QR<Double> qr = QR.PRIMITIVE.make(nbEquations, nbVars);

        qr.compute(body.offsets(q, -1));

        return qr.getSolution(rhs.offsets(q, -1));
    }

    AbstractScedasticity() {
        super();
    }

}
