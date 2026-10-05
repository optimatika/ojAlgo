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
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.structure.Access1D;

public class CharacteristicLineTest extends FinancePortfolioTests {

    private static final double ACCURACY = 1E-12;

    @Test
    public void testAssumingCAPM() {

        SimpleAsset market = SimpleAsset.of(0.06, 0.15);

        CharacteristicLine line = CharacteristicLine.assumingCAPM(market, SimpleAsset.of(0.09, 0.30));

        TestUtils.assertEquals(0.0, line.getAlpha(), ACCURACY);
        TestUtils.assertEquals(1.5, line.getBeta(), ACCURACY);
        TestUtils.assertEquals(0.03375, line.getCovariance(), ACCURACY);
        TestUtils.assertEquals(0.75, line.getCorrelation(), ACCURACY);
        TestUtils.assertEquals(Math.sqrt(0.09 - 1.5 * 1.5 * 0.0225), line.getResidualVolatility(), ACCURACY);

        // Beta 1.5 requires a volatility of at least 1.5 * 0.15
        TestUtils.assertThrows(IllegalArgumentException.class, () -> CharacteristicLine.assumingCAPM(market, SimpleAsset.of(0.09, 0.20)));
    }

    /**
     * The asset's returns are 0.02 + 1.5 * market + 0.01 * noise, where the noise has mean 0 and is
     * uncorrelated with the market - the fit recovers alpha and beta exactly.
     */
    @Test
    public void testEstimate() {

        Access1D<Double> market = Access1D.wrap(new double[] { 0.01, 0.02, 0.03, 0.04 });
        Access1D<Double> asset = Access1D.wrap(new double[] { 0.045, 0.04, 0.055, 0.09 });

        CharacteristicLine line = CharacteristicLine.estimate(market, asset);

        TestUtils.assertEquals(0.02, line.getAlpha(), ACCURACY);
        TestUtils.assertEquals(1.5, line.getBeta(), ACCURACY);
        TestUtils.assertEquals(0.01 * Math.sqrt(4.0 / 3.0), line.getResidualVolatility(), ACCURACY);

        TestUtils.assertThrows(IllegalArgumentException.class, () -> CharacteristicLine.estimate(market, Access1D.wrap(new double[] { 0.1, 0.2 })));
    }

    /**
     * With the equilibrium returns of the market portfolio all alphas are 0, and beta is the ratio of
     * equilibrium returns (same as assuming CAPM). With other returns the market weighted alphas sum to 0.
     * Either way the market weighted betas sum to 1.
     */
    @Test
    public void testOf() {

        MatrixR064 covariances = MatrixR064.FACTORY
                .copy(RawStore.wrap(new double[][] { { 0.04, 0.006, 0.01 }, { 0.006, 0.09, 0.012 }, { 0.01, 0.012, 0.0625 } }));
        MatrixR064 weights = MatrixR064.FACTORY.column(new double[] { 0.5, 0.3, 0.2 });

        FixedWeightsPortfolio equilibrium = FixedWeightsPortfolio.of(MarketEquilibrium.of(covariances, 3), weights);

        double weightedBetas = 0.0;
        for (int i = 0; i < 3; i++) {

            CharacteristicLine line = CharacteristicLine.of(equilibrium, equilibrium, i);

            double equilibriumReturn = equilibrium.getAssetReturns().doubleValue(i);

            TestUtils.assertEquals(0.0, line.getAlpha(), ACCURACY);
            TestUtils.assertEquals(equilibriumReturn / equilibrium.getMeanReturn(), line.getBeta(), ACCURACY);

            SimpleAsset asset = SimpleAsset.of(equilibriumReturn, Math.sqrt(covariances.doubleValue(i, i)));
            TestUtils.assertEquals(CharacteristicLine.assumingCAPM(equilibrium, asset).getBeta(), line.getBeta(), ACCURACY);

            weightedBetas += weights.doubleValue(i) * line.getBeta();
        }
        TestUtils.assertEquals(1.0, weightedBetas, ACCURACY);

        PortfolioContext context = PortfolioContext.of(MatrixR064.FACTORY.column(new double[] { 0.05, 0.12, 0.03 }), covariances);

        double weightedAlphas = 0.0;
        for (int i = 0; i < 3; i++) {
            weightedAlphas += weights.doubleValue(i) * CharacteristicLine.of(context, equilibrium, i).getAlpha();
        }
        TestUtils.assertEquals(0.0, weightedAlphas, ACCURACY);
    }

}
