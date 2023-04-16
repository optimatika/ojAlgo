/*
 * Copyright 1997-2023 Optimatika
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
package org.ojalgo.data.domain.finance.portfolio;

import org.ojalgo.matrix.MatrixR064;

class P20090115 {

    double getCovarance(final double[] seriesA, final double[] seriesB) {

        int numbSamples = seriesA.length;

        double averA = 0;
        double averB = 0;
        for (int i = 0; i < numbSamples; i++) {
            averA += seriesA[i];
            averB += seriesB[i];
        }
        averA /= numbSamples;
        averB /= numbSamples;

        double sum = 0;
        for (int i = 0; i < numbSamples; i++) {
            sum += (seriesA[i] - averA) * (seriesB[i] - averB);
        }

        return sum / (numbSamples - 1);
    }

    MatrixR064 getCovariances(final double[][] returns) {

        int numbAssets = returns.length;

        MatrixR064.DenseReceiver builder = MatrixR064.FACTORY.makeDense(numbAssets, numbAssets);

        for (int i = 0; i < numbAssets; i++) {
            for (int j = i; j < numbAssets; j++) {
                double tmp = this.getCovarance(returns[i], returns[j]);
                builder.set(i, j, tmp);
                builder.set(j, i, tmp);
            }
        }

        return builder.get();
    }

    MatrixR064 getExpectedExcessReturns(final double[][] returns) {

        int numbAssets = returns.length;
        int numbSamples = returns[0].length;

        MatrixR064.DenseReceiver builder = MatrixR064.FACTORY.makeDense(numbAssets);

        double riskFreeReturn = 0;
        for (int j = 0; j < numbSamples; j++) {
            riskFreeReturn += returns[numbAssets - 1][j];
        }
        riskFreeReturn /= numbSamples;

        for (int i = 0; i < numbAssets; i++) {

            double tmp = 0;
            for (int j = 0; j < numbSamples; j++) {
                tmp += returns[i][j];
            }
            tmp /= numbSamples;

            builder.set(i, tmp - riskFreeReturn);
        }

        return builder.get();
    }

}
