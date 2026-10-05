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

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.type.context.NumberContext;

public class MarkowitzModelTest extends FinancePortfolioTests {

    private static final NumberContext ACCURACY = NumberContext.of(5, 4);

    private static final MatrixR064 COVARIANCES = MatrixR064.FACTORY.copy(RawStore.wrap(new double[][] { { 0.01, 0.0 }, { 0.0, 0.01 } }));
    private static final MatrixR064 RETURNS = MatrixR064.FACTORY.column(new double[] { 0.10, 0.0 });

    /**
     * A constraint added after the weights have been calculated used to have no effect (cached results were
     * not reset).
     */
    @Test
    public void testAddConstraintAfterCalculation() {

        MarkowitzModel model = MarkowitzModel.of(MarketEquilibrium.of(COVARIANCES, 2), RETURNS);

        TestUtils.assertEquals(MatrixR064.FACTORY.column(new double[] { 1.0, 0.0 }), model.getAssetWeights(), ACCURACY);

        model.addConstraint(null, new BigDecimal("0.5"), 0);

        TestUtils.assertEquals(MatrixR064.FACTORY.column(new double[] { 0.5, 0.5 }), model.getAssetWeights(), ACCURACY);
    }

    /**
     * Constraints used to be keyed by array identity, so adding a constraint on the same asset again gave two
     * model expressions with the same name (exception).
     */
    @Test
    public void testConstraintOnSameAssetsReplaced() {

        MarkowitzModel model = MarkowitzModel.of(MarketEquilibrium.of(COVARIANCES, 2), RETURNS);

        model.addConstraint(null, new BigDecimal("0.5"), 0);
        model.addConstraint(null, new BigDecimal("0.8"), 0, 0);

        TestUtils.assertEquals(MatrixR064.FACTORY.column(new double[] { 0.8, 0.2 }), model.getAssetWeights(), ACCURACY);
    }

    /**
     * When no usable solution is found all weights are zero, and the state tells why.
     */
    @Test
    public void testInfeasibleGivesZeroWeights() {

        MarkowitzModel model = MarkowitzModel.of(MarketEquilibrium.of(COVARIANCES), RETURNS);
        model.setUpperLimit(0, new BigDecimal("0.3"));
        model.setUpperLimit(1, new BigDecimal("0.3"));

        TestUtils.assertEquals(MatrixR064.FACTORY.column(new double[] { 0.0, 0.0 }), model.getAssetWeights(), ACCURACY);
        TestUtils.assertFalse(model.optimiser().getState().isFeasible());
    }

    /**
     * Targets outside of what's attainable give the minimum variance or maximum return portfolio.
     */
    @Test
    public void testTargetOutsideAttainableRange() {

        MarkowitzModel model = MarkowitzModel.of(MarketEquilibrium.of(COVARIANCES), RETURNS);

        model.setTargetReturn(new BigDecimal("0.01"));
        TestUtils.assertEquals(MatrixR064.FACTORY.column(new double[] { 0.5, 0.5 }), model.getAssetWeights(), ACCURACY);

        model.setTargetReturn(new BigDecimal("0.20"));
        TestUtils.assertEquals(MatrixR064.FACTORY.column(new double[] { 1.0, 0.0 }), model.getAssetWeights(), ACCURACY);

        model.setTargetVariance(new BigDecimal("0.001"));
        TestUtils.assertEquals(MatrixR064.FACTORY.column(new double[] { 0.5, 0.5 }), model.getAssetWeights(), ACCURACY);

        model.setTargetVariance(new BigDecimal("0.02"));
        TestUtils.assertEquals(MatrixR064.FACTORY.column(new double[] { 1.0, 0.0 }), model.getAssetWeights(), ACCURACY);
    }

    /**
     * The target return requires RAF = 12.5. The search used to be restricted to [RAF/√10, RAF·√10] when the
     * RAF was not 1.0, and silently returned a portfolio with return 0.0516.
     */
    @Test
    public void testTargetReturnWithNonDefaultRiskAversion() {

        MarkowitzModel model = MarkowitzModel.of(MarketEquilibrium.of(COVARIANCES, 1000), RETURNS);

        model.setTargetReturn(new BigDecimal("0.09"));

        TestUtils.assertEquals(MatrixR064.FACTORY.column(new double[] { 0.9, 0.1 }), model.getAssetWeights(), ACCURACY);
        TestUtils.assertEquals(0.09, model.getMeanReturn(), ACCURACY);
    }

    /**
     * Same as {@link #testTargetReturnWithNonDefaultRiskAversion()} but with the matching target variance.
     */
    @Test
    public void testTargetVarianceWithNonDefaultRiskAversion() {

        MarkowitzModel model = MarkowitzModel.of(MarketEquilibrium.of(COVARIANCES, 1000), RETURNS);

        model.setTargetVariance(new BigDecimal("0.0082"));

        TestUtils.assertEquals(MatrixR064.FACTORY.column(new double[] { 0.9, 0.1 }), model.getAssetWeights(), ACCURACY);
        TestUtils.assertEquals(0.0082, model.getReturnVariance(), ACCURACY);
    }

    /**
     * Perfectly correlated assets with different expected returns, and shorting allowed, is an arbitrage -
     * the optimisation problem is unbounded. That used to be reported as OPTIMAL, with weights of +/-3.4E8.
     * Now the state is UNBOUNDED and, as with any optimisation failure, all weights are zero.
     */
    @Test
    public void testUnboundedWithSingularCovariances() {

        MatrixR064 singular = MatrixR064.FACTORY.copy(RawStore.wrap(new double[][] { { 0.01, 0.01 }, { 0.01, 0.01 } }));

        MarkowitzModel portfolio = MarkowitzModel.of(MarketEquilibrium.of(singular), RETURNS);
        portfolio.setShortingAllowed(true);

        TestUtils.assertEquals(MatrixR064.FACTORY.column(new double[] { 0.0, 0.0 }), portfolio.getAssetWeights(), ACCURACY);
        TestUtils.assertEquals(State.UNBOUNDED, portfolio.optimiser().getState());
    }

}
