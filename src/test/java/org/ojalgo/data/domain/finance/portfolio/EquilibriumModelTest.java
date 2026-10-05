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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.data.domain.finance.FinanceUtils;
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.type.context.NumberContext;

public class EquilibriumModelTest extends FinancePortfolioTests {

    private static final NumberContext ACCURACY = NumberContext.of(12);

    /**
     * A subclass may override the covariances. The volatilities, correlations and portfolio variances
     * follow, and are recalculated after a reset. The mapping between weights and returns, the equilibrium,
     * is not affected.
     */
    @Test
    public void testCovariancesOverride() {

        MatrixR064 covariances = MatrixR064.FACTORY
                .copy(RawStore.wrap(new double[][] { { 0.04, 0.006, 0.01 }, { 0.006, 0.09, 0.012 }, { 0.01, 0.012, 0.0625 } }));
        MatrixR064 weights = MatrixR064.FACTORY.column(new double[] { 0.5, 0.3, 0.2 });

        double[] factor = { 2.0 };

        EquilibriumModel model = new EquilibriumModel(MarketEquilibrium.of(covariances, 3)) {

            @Override
            protected MatrixR064 calculateAssetReturns() {
                return this.calculateAssetReturns(weights);
            }

            @Override
            protected MatrixR064 calculateAssetWeights() {
                return weights;
            }

            @Override
            protected MatrixR064 calculateCovariances() {
                return super.calculateCovariances().multiply(factor[0]);
            }

        };

        SimplePortfolio portfolio = SimplePortfolio.ofWeights(weights.toRawCopy1D());
        double variance = weights.dot(covariances.multiply(weights));

        for (double expected : new double[] { 2.0, 3.0 }) {

            factor[0] = expected;
            model.reset();

            TestUtils.assertEquals(covariances.multiply(expected), model.getCovariances(), ACCURACY);
            TestUtils.assertEquals(FinanceUtils.toVolatilities(covariances).multiply(Math.sqrt(expected)), model.getAssetVolatilities(), ACCURACY);
            TestUtils.assertEquals(FinanceUtils.toCorrelations(covariances), model.getCorrelations(), ACCURACY);
            TestUtils.assertEquals(expected * variance, model.getReturnVariance(), ACCURACY);
            TestUtils.assertEquals(expected * variance, model.calculatePortfolioVariance(portfolio), ACCURACY);

            TestUtils.assertEquals(covariances.multiply(weights).multiply(3.0), model.getAssetReturns(), ACCURACY);
            TestUtils.assertEquals(covariances, model.getMarketEquilibrium().getCovariances(), ACCURACY);
        }
    }

}
