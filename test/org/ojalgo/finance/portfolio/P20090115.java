/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
package org.ojalgo.finance.portfolio;

import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.BasicMatrix.Builder;
import org.ojalgo.matrix.PrimitiveMatrix;

class P20090115 {

    public double getCovarance(final double[] value1, final double[] value2) {

        final int n = value1.length;
        double averOne = 0;
        double averTwo = 0;
        for (int i = 0; i < n; i++) {
            averOne += value1[i];
            averTwo += value2[i];
        }
        averOne /= n;
        averTwo /= n;

        double sum = 0;
        for (int i = 0; i < n; i++) {
            sum += (value1[i] - averOne) * (value2[i] - averTwo);
        }

        return sum / (n - 1);
    }

    public BasicMatrix getCovariances(final double[][] returns) {

        final int row = returns.length;
        final int col = returns[0].length;

        final Builder<PrimitiveMatrix> tmpBuilder = PrimitiveMatrix.getBuilder(row, col);

        for (int i = 0; i < row; i++) {
            for (int j = i; j < col; j++) {
                final double tmp = this.getCovarance(returns[i], returns[j]);
                tmpBuilder.set(i, j, tmp);
                tmpBuilder.set(j, i, tmp);
            }
        }
        return tmpBuilder.build();
    }

    public BasicMatrix getExpectedExcessReturns(final double[][] returns) {

        final int row = returns.length;
        final int col = returns[0].length;

        final Builder<PrimitiveMatrix> tmpBuilder = PrimitiveMatrix.getBuilder(row);

        double riskFreeReturn = 0;
        for (int j = 0; j < col; j++) {
            riskFreeReturn += returns[row - 1][j];
        }
        riskFreeReturn /= row;

        for (int i = 0; i < row; i++) {

            double tmp = 0;
            for (int j = 0; j < col; j++) {
                tmp += returns[i][j];
            }
            tmp /= col;

            tmpBuilder.set(i, tmp - riskFreeReturn);
        }

        return tmpBuilder.build();
    }

}
