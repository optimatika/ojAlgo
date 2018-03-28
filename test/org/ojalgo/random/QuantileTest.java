/*
 * Copyright 1997-2018 Optimatika
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
package org.ojalgo.random;

import static org.ojalgo.constant.PrimitiveMath.*;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.netio.BasicLogger;

/**
 * QuantileTest
 *
 * @author apete
 */
public class QuantileTest {

    @Test
    public void testExponential() {

        for (int e = -2; e <= 2; e++) {

            final Exponential tmpDistribution = new Exponential(PrimitiveFunction.POW.invoke(TEN, e));

            this.doTest(tmpDistribution);
        }
    }

    @Test
    public void testLogNormal() {

        for (int m = -2; m <= 2; m++) {

            for (int s = -2; s <= 2; s++) {

                final LogNormal tmpDistribution = new LogNormal(PrimitiveFunction.POW.invoke(TEN, m), PrimitiveFunction.POW.invoke(TEN, s));

                this.doTest(tmpDistribution);
            }
        }
    }

    @Test
    public void testNormal() {

        for (int m = -2; m <= 2; m++) {

            for (int s = -2; s <= 2; s++) {

                final Normal tmpDistribution = new Normal(PrimitiveFunction.POW.invoke(TEN, m), PrimitiveFunction.POW.invoke(TEN, s));

                this.doTest(tmpDistribution);
            }
        }
    }

    @Test
    public void testUniform() {

        for (int m = -2; m <= 2; m++) {

            for (int s = -2; s <= 2; s++) {

                final Uniform tmpDistribution = new Uniform(PrimitiveFunction.POW.invoke(TEN, m), PrimitiveFunction.POW.invoke(TEN, s));

                this.doTest(tmpDistribution);
            }
        }
    }

    private void doTest(final ContinuousDistribution aDistribution) {

        double tmpReveresed;
        double tmpQuantile;
        double tmpProbability;
        for (int i = 0; i <= 10; i++) {
            tmpProbability = i / TEN;
            tmpQuantile = aDistribution.getQuantile(tmpProbability);
            tmpReveresed = aDistribution.getDistribution(tmpQuantile);
            if (RandomTests.DEBUG) {
                BasicLogger.debug("Expected={} Actual={} Quantile={}", tmpProbability, tmpReveresed, tmpQuantile);
            }
            TestUtils.assertEquals(tmpProbability, tmpReveresed, 0.001);
        }
    }

}
