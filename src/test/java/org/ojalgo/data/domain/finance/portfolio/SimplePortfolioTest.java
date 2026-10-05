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
package org.ojalgo.data.domain.finance.portfolio;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.random.ContinuousDistribution;

public class SimplePortfolioTest extends FinancePortfolioTests {

    /**
     * Normalising divides the weights by their total. The volatility must be divided by the absolute value of
     * that total - it used to become negative.
     */
    @Test
    public void testNormalisedVolatilityWithNegativeTotalWeight() {

        SimplePortfolio portfolio = SimplePortfolio.of(List.of(SimpleAsset.of(0.05, 0.1, -1), SimpleAsset.of(0.05, 0.1, -1)));

        FinancePortfolio normalised = portfolio.normalise();

        TestUtils.assertEquals(0.05, normalised.getMeanReturn(), 1E-12);
        TestUtils.assertEquals(Math.sqrt(0.02) / 2.0, normalised.getVolatility(), 1E-12);
    }

    /**
     * VaR used to assume normally distributed returns, while {@link FinancePortfolio#forecast()} and
     * {@link FinancePortfolio#getLossProbability(Number)} use a geometric Brownian motion. Now a loss larger
     * than the VaR has precisely the (1 - confidence) probability according to the forecast.
     */
    @Test
    public void testValueAtRiskConsistentWithForecast() {

        SimpleAsset asset = SimpleAsset.of(0.08, 0.20);

        for (double period : new double[] { 0.5, 1.0, 2.0 }) {

            ContinuousDistribution distribution = asset.forecast().getDistribution(period);

            TestUtils.assertEquals(0.05, distribution.getDistribution(1.0 - asset.getValueAtRisk(0.95, period)), 1E-9);
            TestUtils.assertEquals(0.01, distribution.getDistribution(1.0 - asset.getValueAtRisk(0.99, period)), 1E-9);
        }
    }

}
