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
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.Array1D;
import org.ojalgo.data.domain.finance.FinanceUtils;
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.type.context.NumberContext;

/**
 * Verifies {@link BlackLittermanModel} against reference formulas implemented here, in the "return space"
 * form, independently of the model's own implementation:
 * <p>
 * Prior (equilibrium) returns: [Pi] = lambda [C][w]
 * <p>
 * Posterior returns: [mu] = [Pi] + tau [C][P]<sup>T</sup>(tau [P][C][P]<sup>T</sup> + [Omega])<sup>-1</sup>([Q] -
 * [P][Pi])
 * <p>
 * Weights: [w] = (lambda [C])<sup>-1</sup>[mu]
 * <p>
 * [C] is the covariance matrix, [P] has the view portfolios as rows, [Q] the view returns and [Omega] the
 * (diagonal) view variances.
 */
public class BlackLittermanModelTest extends FinancePortfolioTests {

    /**
     * Market (covariances, risk aversion and weights) and views (portfolios and returns)
     */
    static final class Case {

        final MatrixR064 covariances;
        final MatrixR064 marketWeights;
        final double riskAversion;
        final double tau;
        final MatrixR064 viewPortfolios;
        final MatrixR064 viewReturns;

        Case(final MatrixR064 covariances, final double riskAversion, final MatrixR064 marketWeights, final MatrixR064 viewPortfolios,
                final MatrixR064 viewReturns, final double tau) {
            this.covariances = covariances;
            this.riskAversion = riskAversion;
            this.marketWeights = marketWeights;
            this.viewPortfolios = viewPortfolios;
            this.viewReturns = viewReturns;
            this.tau = tau;
        }

        int countViews() {
            return (int) viewPortfolios.countRows();
        }

        BlackLittermanModel newModel() {
            return BlackLittermanModel.of(MarketEquilibrium.of(covariances, riskAversion), marketWeights);
        }

        /**
         * [p][C][p]<sup>T</sup> - the (prior) variance of a view portfolio
         */
        double viewPortfolioVariance(final int view) {
            MatrixR064 portfolio = viewPortfolios.logical().rows(view).get();
            return portfolio.multiply(covariances).multiply(portfolio.transpose()).doubleValue(0);
        }

        BigDecimal viewReturn(final int view) {
            return BigDecimal.valueOf(viewReturns.doubleValue(view));
        }

        List<BigDecimal> viewWeights(final int view) {
            return Array1D.R256.copy(viewPortfolios.logical().rows(view).get());
        }

    }

    private static final NumberContext ACCURACY = NumberContext.of(10);

    private static final double[] SCALES = { 0.5, 2.0, 0.1 };
    private static final double[] STANDARD_DEVIATIONS = { 0.02, 0.05, 0.01 };

    private static void assertMatchesFullReference(final BlackLittermanModel model, final Case data, final double[] omega, final double tau) {

        MatrixR064 expectedReturns = BlackLittermanModelTest.referenceReturns(data, data.riskAversion, omega, tau);
        MatrixR064 expectedCovariances = BlackLittermanModelTest.referenceFullCovariances(data, omega, tau);
        MatrixR064 expectedWeights = expectedCovariances.solve(expectedReturns).divide(data.riskAversion);

        TestUtils.assertEquals(expectedReturns, model.getAssetReturns(), ACCURACY);
        TestUtils.assertEquals(expectedCovariances, model.getCovariances(), ACCURACY);
        TestUtils.assertEquals(expectedWeights, model.getAssetWeights(), ACCURACY);

        TestUtils.assertEquals(expectedWeights.dot(expectedReturns), model.getMeanReturn(), ACCURACY);
        TestUtils.assertEquals(expectedWeights.dot(expectedCovariances.multiply(expectedWeights)), model.getReturnVariance(), ACCURACY);
    }

    /**
     * MARKET mode reference, the precision form: [K] = ([C]<sup>-1</sup> + tau [P]<sup>T</sup>[Omega]<sup>-1</sup>[P])<sup>-1</sup>
     * and [mu] = [K]([C]<sup>-1</sup>[Pi] + tau [P]<sup>T</sup>[Omega]<sup>-1</sup>[Q])
     */
    private static void assertMatchesMarketReference(final BlackLittermanModel model, final Case data, final double[] omega, final double tau) {

        MatrixR064.DenseReceiver inverseOmega = MatrixR064.FACTORY.newDenseBuilder(omega.length, omega.length);
        for (int k = 0; k < omega.length; k++) {
            inverseOmega.set(k, k, tau / omega[k]);
        }

        MatrixR064 prior = data.covariances.multiply(data.marketWeights).multiply(data.riskAversion);
        MatrixR064 inverseCovariances = data.covariances.invert();
        MatrixR064 portfoliosTransposed = data.viewPortfolios.transpose();

        MatrixR064 expectedCovariances = inverseCovariances.add(portfoliosTransposed.multiply(inverseOmega.get()).multiply(data.viewPortfolios)).invert();
        MatrixR064 expectedReturns = expectedCovariances
                .multiply(inverseCovariances.multiply(prior).add(portfoliosTransposed.multiply(inverseOmega.get()).multiply(data.viewReturns)));
        MatrixR064 expectedWeights = expectedCovariances.solve(expectedReturns).divide(data.riskAversion);

        TestUtils.assertEquals(expectedReturns, model.getAssetReturns(), ACCURACY);
        TestUtils.assertEquals(expectedCovariances, model.getCovariances(), ACCURACY);
        TestUtils.assertEquals(expectedWeights, model.getAssetWeights(), ACCURACY);

        TestUtils.assertEquals(expectedWeights.dot(expectedReturns), model.getMeanReturn(), ACCURACY);
        TestUtils.assertEquals(expectedWeights.dot(expectedCovariances.multiply(expectedWeights)), model.getReturnVariance(), ACCURACY);
    }

    private static void assertMatchesReference(final BlackLittermanModel model, final Case data, final double riskAversion, final double[] omega,
            final double tau) {

        MatrixR064 expectedReturns = BlackLittermanModelTest.referenceReturns(data, riskAversion, omega, tau);
        MatrixR064 expectedWeights = data.covariances.solve(expectedReturns).divide(riskAversion);

        TestUtils.assertEquals(expectedReturns, model.getAssetReturns(), ACCURACY);
        TestUtils.assertEquals(expectedWeights, model.getAssetWeights(), ACCURACY);

        TestUtils.assertEquals(expectedWeights.dot(expectedReturns), model.getMeanReturn(), ACCURACY);
        TestUtils.assertEquals(expectedWeights.dot(data.covariances.multiply(expectedWeights)), model.getReturnVariance(), ACCURACY);
    }

    private static Case idzorek() {
        return new Case(BlackLittermanTest.getCovariances(), BlackLittermanTest.getRiskAversionFactor().doubleValue(), BlackLittermanTest.getMarketWeights(),
                BlackLittermanTest.getInvestorPortfoliosMatrix(), BlackLittermanTest.getInvestorReturnsMatrix(),
                BlackLittermanTest.getWeightOnViews().doubleValue());
    }

    /**
     * FULL mode reference, the precision form: [C] + ((tau [C])<sup>-1</sup> + [P]<sup>T</sup>[Omega]<sup>-1</sup>[P])<sup>-1</sup>
     */
    private static MatrixR064 referenceFullCovariances(final Case data, final double[] omega, final double tau) {

        MatrixR064.DenseReceiver inverseOmega = MatrixR064.FACTORY.newDenseBuilder(omega.length, omega.length);
        for (int k = 0; k < omega.length; k++) {
            inverseOmega.set(k, k, 1.0 / omega[k]);
        }

        MatrixR064 precision = data.covariances.multiply(tau).invert()
                .add(data.viewPortfolios.transpose().multiply(inverseOmega.get()).multiply(data.viewPortfolios));

        return data.covariances.add(precision.invert());
    }

    private static MatrixR064 referenceReturns(final Case data, final double riskAversion, final double[] omega, final double tau) {

        MatrixR064 prior = data.covariances.multiply(data.marketWeights).multiply(riskAversion);

        MatrixR064.DenseReceiver omegaBuilder = MatrixR064.FACTORY.newDenseBuilder(omega.length, omega.length);
        for (int k = 0; k < omega.length; k++) {
            omegaBuilder.set(k, k, omega[k]);
        }

        MatrixR064 tauCovariances = data.covariances.multiply(tau);
        MatrixR064 portfoliosTransposed = data.viewPortfolios.transpose();

        MatrixR064 inner = data.viewPortfolios.multiply(tauCovariances).multiply(portfoliosTransposed).add(omegaBuilder.get());
        MatrixR064 surprise = data.viewReturns.subtract(data.viewPortfolios.multiply(prior));

        return prior.add(tauCovariances.multiply(portfoliosTransposed).multiply(inner.solve(surprise)));
    }

    private static Case small() {
        MatrixR064 covariances = MatrixR064.FACTORY
                .copy(RawStore.wrap(new double[][] { { 0.04, 0.006, 0.01 }, { 0.006, 0.09, 0.012 }, { 0.01, 0.012, 0.0625 } }));
        MatrixR064 marketWeights = MatrixR064.FACTORY.column(new double[] { 0.5, 0.3, 0.2 });
        MatrixR064 viewPortfolios = MatrixR064.FACTORY.copy(RawStore.wrap(new double[][] { { 1.0, 0.0, 0.0 }, { 0.0, 1.0, -1.0 } }));
        MatrixR064 viewReturns = MatrixR064.FACTORY.column(new double[] { 0.07, 0.02 });
        return new Case(covariances, 2.5, marketWeights, viewPortfolios, viewReturns, 0.3);
    }

    /**
     * A view as an arbitrary {@link FinancePortfolio} - the way PortfolioTactics adds views.
     */
    private static FinancePortfolio view(final List<BigDecimal> weights, final double meanReturn, final double returnVariance) {
        return new FinancePortfolio() {

            @Override
            public double getMeanReturn() {
                return meanReturn;
            }

            @Override
            public double getReturnVariance() {
                return returnVariance;
            }

            @Override
            public List<BigDecimal> getWeights() {
                return weights;
            }

            @Override
            protected void reset() {
                // Nothing to reset
            }
        };
    }

    /**
     * Balanced confidence: [Omega]<sub>kk</sub> = tau [p<sub>k</sub>][C][p<sub>k</sub>]<sup>T</sup> - with both
     * the default and an explicitly set tau
     */
    @Test
    public void testBalancedViews() {

        for (Case data : new Case[] { BlackLittermanModelTest.idzorek(), BlackLittermanModelTest.small() }) {

            BlackLittermanModel defaultTau = data.newModel();
            BlackLittermanModel explicitTau = data.newModel();
            explicitTau.setConfidence(data.tau);

            double[] omegaDefault = new double[data.countViews()];
            double[] omegaExplicit = new double[data.countViews()];

            for (int k = 0; k < data.countViews(); k++) {
                defaultTau.addViewWithBalancedConfidence(data.viewWeights(k), data.viewReturn(k));
                explicitTau.addViewWithBalancedConfidence(data.viewWeights(k), data.viewReturn(k));
                omegaDefault[k] = data.viewPortfolioVariance(k);
                omegaExplicit[k] = data.tau * data.viewPortfolioVariance(k);
            }

            BlackLittermanModelTest.assertMatchesReference(defaultTau, data, data.riskAversion, omegaDefault, 1.0);
            BlackLittermanModelTest.assertMatchesReference(explicitTau, data, data.riskAversion, omegaExplicit, data.tau);
        }
    }

    /**
     * Idzorek: On its own, a view with confidence level C tilts the weights C times as much as the same view
     * at 100%. At 100% the view is certain (the posterior satisfies it exactly), and 50% is the same as
     * balanced confidence.
     */
    @Test
    public void testConfidenceLevelTilt() {

        for (Case data : new Case[] { BlackLittermanModelTest.idzorek(), BlackLittermanModelTest.small() }) {
            for (int k = 0; k < data.countViews(); k++) {

                BlackLittermanModel certain = data.newModel();
                certain.setConfidence(data.tau);
                certain.addViewWithConfidenceLevel(data.viewWeights(k), data.viewReturn(k), 1.0);

                MatrixR064 portfolio = data.viewPortfolios.logical().rows(k).get();
                TestUtils.assertEquals(data.viewReturns.doubleValue(k), portfolio.dot(certain.getAssetReturns()), ACCURACY);

                MatrixR064 fullTilt = certain.getAssetWeights().subtract(data.marketWeights);

                for (double level : new double[] { 0.3, 0.5, 0.8 }) {

                    BlackLittermanModel model = data.newModel();
                    model.setConfidence(data.tau);
                    model.addViewWithConfidenceLevel(data.viewWeights(k), data.viewReturn(k), level);

                    TestUtils.assertEquals(fullTilt.multiply(level), model.getAssetWeights().subtract(data.marketWeights), ACCURACY);
                }

                BlackLittermanModel balanced = data.newModel();
                balanced.setConfidence(data.tau);
                balanced.addViewWithBalancedConfidence(data.viewWeights(k), data.viewReturn(k));

                BlackLittermanModel fifty = data.newModel();
                fifty.setConfidence(data.tau);
                fifty.addViewWithConfidenceLevel(data.viewWeights(k), data.viewReturn(k), 0.5);

                TestUtils.assertEquals(balanced.getAssetWeights(), fifty.getAssetWeights(), ACCURACY);
            }
        }
    }

    /**
     * Idzorek's confidence level: [Omega]<sub>kk</sub> = tau (1 - level<sub>k</sub>) / level<sub>k</sub>
     * [p<sub>k</sub>][C][p<sub>k</sub>]<sup>T</sup>
     */
    @Test
    public void testConfidenceLevelViews() {

        double[] levels = { 0.25, 0.5, 0.9 };

        for (Case data : new Case[] { BlackLittermanModelTest.idzorek(), BlackLittermanModelTest.small() }) {

            BlackLittermanModel model = data.newModel();
            model.setConfidence(data.tau);

            double[] omega = new double[data.countViews()];

            for (int k = 0; k < data.countViews(); k++) {
                model.addViewWithConfidenceLevel(data.viewWeights(k), data.viewReturn(k), levels[k]);
                omega[k] = data.tau * (1.0 - levels[k]) / levels[k] * data.viewPortfolioVariance(k);
            }

            BlackLittermanModelTest.assertMatchesReference(model, data, data.riskAversion, omega, data.tau);

            TestUtils.assertThrows(IllegalArgumentException.class, () -> model.addViewWithConfidenceLevel(data.viewWeights(0), data.viewReturn(0), 0.0));
            TestUtils.assertThrows(IllegalArgumentException.class, () -> model.addViewWithConfidenceLevel(data.viewWeights(0), data.viewReturn(0), 1.1));
        }
    }

    /**
     * Using a {@link FinancePortfolio.Context} (an equilibrium model) and a {@link FinancePortfolio} (the
     * market weights) to create the model - the way PortfolioTactics does it.
     */
    @Test
    public void testContextConstructor() {

        Case data = BlackLittermanModelTest.small();

        FixedWeightsPortfolio equilibrium = FixedWeightsPortfolio.of(MarketEquilibrium.of(data.covariances), data.marketWeights);
        SimplePortfolio market = SimplePortfolio.ofWeights(data.marketWeights.toRawCopy1D());

        BlackLittermanModel model = BlackLittermanModel.of(equilibrium, market);
        model.setRiskAversion(data.riskAversion);
        model.setConfidence(data.tau);

        double[] omega = new double[data.countViews()];
        for (int k = 0; k < data.countViews(); k++) {
            model.addViewWithBalancedConfidence(data.viewWeights(k), data.viewReturn(k));
            omega[k] = data.tau * data.viewPortfolioVariance(k);
        }

        BlackLittermanModelTest.assertMatchesReference(model, data, data.riskAversion, omega, data.tau);
    }

    /**
     * The model as a {@link FinancePortfolio.Context}: posterior returns and the (prior) covariances.
     */
    @Test
    public void testContextMethods() {

        Case data = BlackLittermanModelTest.idzorek();

        BlackLittermanModel model = data.newModel();
        model.setConfidence(data.tau);

        double[] omega = new double[data.countViews()];
        for (int k = 0; k < data.countViews(); k++) {
            model.addViewWithBalancedConfidence(data.viewWeights(k), data.viewReturn(k));
            omega[k] = data.tau * data.viewPortfolioVariance(k);
        }

        MatrixR064 expectedReturns = BlackLittermanModelTest.referenceReturns(data, data.riskAversion, omega, data.tau);

        TestUtils.assertEquals((int) data.covariances.countRows(), model.size());
        TestUtils.assertEquals(expectedReturns, model.getAssetReturns(), ACCURACY);
        TestUtils.assertEquals(data.covariances, model.getCovariances(), ACCURACY);
        TestUtils.assertEquals(FinanceUtils.toVolatilities(data.covariances), model.getAssetVolatilities(), ACCURACY);
        TestUtils.assertEquals(FinanceUtils.toCorrelations(data.covariances), model.getCorrelations(), ACCURACY);

        MatrixR064 portfolio = MatrixR064.FACTORY.column(new double[] { 0.1, 0.2, 0.1, 0.1, 0.1, 0.1, 0.2, 0.1 });
        SimplePortfolio weights = SimplePortfolio.ofWeights(portfolio.toRawCopy1D());

        TestUtils.assertEquals(portfolio.dot(expectedReturns), model.calculatePortfolioReturn(weights), ACCURACY);
        TestUtils.assertEquals(portfolio.dot(data.covariances.multiply(portfolio)), model.calculatePortfolioVariance(weights), ACCURACY);

        // A MarkowitzModel created from the Black-Litterman model sees the same returns and covariances
        MarkowitzModel markowitz = MarkowitzModel.of(model);
        TestUtils.assertEquals(expectedReturns, markowitz.getAssetReturns(), ACCURACY);
        TestUtils.assertEquals(data.covariances, markowitz.getCovariances(), ACCURACY);
    }

    /**
     * Views as arbitrary {@link FinancePortfolio} instances - [Omega]<sub>kk</sub> is the view's return
     * variance. With the default tau = 1, as PortfolioTactics uses it, and with an explicitly set tau.
     */
    @Test
    public void testCustomViews() {

        for (Case data : new Case[] { BlackLittermanModelTest.idzorek(), BlackLittermanModelTest.small() }) {

            BlackLittermanModel defaultTau = data.newModel();
            BlackLittermanModel explicitTau = data.newModel();
            explicitTau.setConfidence(data.tau);

            double[] omega = new double[data.countViews()];

            for (int k = 0; k < data.countViews(); k++) {
                omega[k] = STANDARD_DEVIATIONS[k] * STANDARD_DEVIATIONS[k];
                defaultTau.addView(BlackLittermanModelTest.view(data.viewWeights(k), data.viewReturns.doubleValue(k), omega[k]));
                explicitTau.addView(BlackLittermanModelTest.view(data.viewWeights(k), data.viewReturns.doubleValue(k), omega[k]));
            }

            BlackLittermanModelTest.assertMatchesReference(defaultTau, data, data.riskAversion, omega, 1.0);
            BlackLittermanModelTest.assertMatchesReference(explicitTau, data, data.riskAversion, omega, data.tau);
        }
    }

    /**
     * FULL mode: The same posterior expected returns as RETURNS mode, but the covariances are [C] + [M], and
     * the weights are optimal given those covariances. With the mode's default tau (0.05), and with an
     * explicitly set tau.
     */
    @Test
    public void testFullMode() {

        for (Case data : new Case[] { BlackLittermanModelTest.idzorek(), BlackLittermanModelTest.small() }) {

            BlackLittermanModel defaultTau = data.newModel();
            defaultTau.setMode(BlackLittermanModel.Mode.FULL);
            TestUtils.assertEquals(0.05, defaultTau.getConfidence().doubleValue());

            BlackLittermanModel explicitTau = data.newModel();
            explicitTau.setConfidence(data.tau);
            explicitTau.setMode(BlackLittermanModel.Mode.FULL);
            TestUtils.assertEquals(data.tau, explicitTau.getConfidence().doubleValue());

            double[] omegaDefault = new double[data.countViews()];
            double[] omegaExplicit = new double[data.countViews()];

            for (int k = 0; k < data.countViews(); k++) {
                defaultTau.addViewWithBalancedConfidence(data.viewWeights(k), data.viewReturn(k));
                explicitTau.addViewWithStandardDeviation(data.viewWeights(k), data.viewReturn(k), BigDecimal.valueOf(STANDARD_DEVIATIONS[k]));
                omegaDefault[k] = 0.05 * data.viewPortfolioVariance(k);
                omegaExplicit[k] = STANDARD_DEVIATIONS[k] * STANDARD_DEVIATIONS[k];
            }

            BlackLittermanModelTest.assertMatchesFullReference(defaultTau, data, omegaDefault, 0.05);
            BlackLittermanModelTest.assertMatchesFullReference(explicitTau, data, omegaExplicit, data.tau);
        }
    }

    /**
     * The FULL mode covariances are symmetric and positive definite. A MarkowitzModel created from the model
     * sees them. Changing the mode changes the covariances.
     */
    @Test
    public void testFullModeCovariances() {

        Case data = BlackLittermanModelTest.idzorek();

        BlackLittermanModel model = data.newModel();
        model.setMode(BlackLittermanModel.Mode.FULL);

        double[] omega = new double[data.countViews()];
        for (int k = 0; k < data.countViews(); k++) {
            model.addViewWithBalancedConfidence(data.viewWeights(k), data.viewReturn(k));
            omega[k] = 0.05 * data.viewPortfolioVariance(k);
        }

        MatrixR064 covariances = model.getCovariances();

        TestUtils.assertEquals(covariances, covariances.transpose(), NumberContext.of(16));

        Cholesky<Double> cholesky = Cholesky.R064.make(covariances);
        cholesky.decompose(covariances);
        TestUtils.assertTrue(cholesky.isSPD());

        MarkowitzModel markowitz = MarkowitzModel.of(model);
        TestUtils.assertEquals(BlackLittermanModelTest.referenceFullCovariances(data, omega, 0.05), markowitz.getCovariances(), ACCURACY);

        model.setMode(BlackLittermanModel.Mode.RETURNS);
        TestUtils.assertEquals(data.covariances, model.getCovariances(), ACCURACY);
    }

    /**
     * He and Litterman: The optimal FULL mode weights are the original weights scaled by 1 / (1 + tau) plus
     * a combination of the view portfolios.
     */
    @Test
    public void testFullModeHeLitterman() {

        for (Case data : new Case[] { BlackLittermanModelTest.idzorek(), BlackLittermanModelTest.small() }) {

            BlackLittermanModel model = data.newModel();
            model.setMode(BlackLittermanModel.Mode.FULL);
            model.setConfidence(data.tau);

            for (int k = 0; k < data.countViews(); k++) {
                model.addViewWithStandardDeviation(data.viewWeights(k), data.viewReturn(k), BigDecimal.valueOf(STANDARD_DEVIATIONS[k]));
            }

            MatrixR064 difference = model.getAssetWeights().subtract(data.marketWeights.divide(1.0 + data.tau));

            MatrixR064 portfoliosTransposed = data.viewPortfolios.transpose();
            MatrixR064 combination = data.viewPortfolios.multiply(portfoliosTransposed).solve(data.viewPortfolios.multiply(difference));

            TestUtils.assertEquals(difference, portfoliosTransposed.multiply(combination), ACCURACY);
        }
    }

    /**
     * FULL mode without views: The posterior expected returns are the prior returns, the covariances are (1 +
     * tau) [C] and the weights are the original weights divided by (1 + tau).
     */
    @Test
    public void testFullModeWithoutViews() {

        Case data = BlackLittermanModelTest.small();

        BlackLittermanModel model = data.newModel();
        model.setMode(BlackLittermanModel.Mode.FULL);
        model.setConfidence(data.tau);

        TestUtils.assertEquals(data.covariances.multiply(data.marketWeights).multiply(data.riskAversion), model.getAssetReturns(), ACCURACY);
        TestUtils.assertEquals(data.covariances.multiply(1.0 + data.tau), model.getCovariances(), ACCURACY);
        TestUtils.assertEquals(data.marketWeights.divide(1.0 + data.tau), model.getAssetWeights(), ACCURACY);
    }

    /**
     * MARKET mode (Meucci): The conditional distribution of the returns given the views. With the default
     * tau (1.0) and with an explicitly set tau. The expected returns are the same as in RETURNS mode, the
     * covariances are smaller - the variance of each view portfolio is reduced.
     */
    @Test
    public void testMarketMode() {

        for (Case data : new Case[] { BlackLittermanModelTest.idzorek(), BlackLittermanModelTest.small() }) {

            BlackLittermanModel defaultTau = data.newModel();
            defaultTau.setMode(BlackLittermanModel.Mode.MARKET);
            TestUtils.assertEquals(1.0, defaultTau.getConfidence().doubleValue());

            BlackLittermanModel explicitTau = data.newModel();
            explicitTau.setMode(BlackLittermanModel.Mode.MARKET);
            explicitTau.setConfidence(data.tau);

            BlackLittermanModel returns = data.newModel();
            returns.setConfidence(data.tau);

            double[] omegaDefault = new double[data.countViews()];
            double[] omegaExplicit = new double[data.countViews()];

            for (int k = 0; k < data.countViews(); k++) {
                defaultTau.addViewWithStandardDeviation(data.viewWeights(k), data.viewReturn(k), BigDecimal.valueOf(STANDARD_DEVIATIONS[k]));
                explicitTau.addViewWithBalancedConfidence(data.viewWeights(k), data.viewReturn(k));
                returns.addViewWithBalancedConfidence(data.viewWeights(k), data.viewReturn(k));
                omegaDefault[k] = STANDARD_DEVIATIONS[k] * STANDARD_DEVIATIONS[k];
                omegaExplicit[k] = data.tau * data.viewPortfolioVariance(k);
            }

            BlackLittermanModelTest.assertMatchesMarketReference(defaultTau, data, omegaDefault, 1.0);
            BlackLittermanModelTest.assertMatchesMarketReference(explicitTau, data, omegaExplicit, data.tau);

            TestUtils.assertEquals(returns.getAssetReturns(), explicitTau.getAssetReturns(), ACCURACY);

            MatrixR064 covariances = explicitTau.getCovariances();

            Cholesky<Double> cholesky = Cholesky.R064.make(covariances);
            cholesky.decompose(covariances);
            TestUtils.assertTrue(cholesky.isSPD());

            for (int k = 0; k < data.countViews(); k++) {
                MatrixR064 portfolio = data.viewPortfolios.logical().rows(k).get().transpose();
                TestUtils.assertTrue(portfolio.dot(covariances.multiply(portfolio)) < data.viewPortfolioVariance(k));
            }

            MarkowitzModel markowitz = MarkowitzModel.of(explicitTau);
            TestUtils.assertEquals(covariances, markowitz.getCovariances(), ACCURACY);
        }
    }

    /**
     * MARKET mode without views: The prior distribution, and the original weights.
     */
    @Test
    public void testMarketModeWithoutViews() {

        Case data = BlackLittermanModelTest.small();

        BlackLittermanModel model = data.newModel();
        model.setMode(BlackLittermanModel.Mode.MARKET);

        TestUtils.assertEquals(data.covariances.multiply(data.marketWeights).multiply(data.riskAversion), model.getAssetReturns(), ACCURACY);
        TestUtils.assertEquals(data.covariances, model.getCovariances(), ACCURACY);
        TestUtils.assertEquals(data.marketWeights, model.getAssetWeights(), ACCURACY);
    }

    /**
     * The default mode is RETURNS, and unless set explicitly the confidence (tau) is the mode's default. An
     * explicitly set confidence is kept when the mode changes, and setting it to null reverts to the default.
     * With views of specified standard deviation tau matters, and the results follow these changes.
     */
    @Test
    public void testModeAndConfidence() {

        Case data = BlackLittermanModelTest.small();

        BlackLittermanModel model = data.newModel();

        TestUtils.assertEquals(BlackLittermanModel.Mode.RETURNS, model.getMode());
        TestUtils.assertEquals(1.0, BlackLittermanModel.Mode.RETURNS.getDefaultConfidence().doubleValue());
        TestUtils.assertEquals(1.0, model.getConfidence().doubleValue());

        double[] omega = new double[data.countViews()];
        for (int k = 0; k < data.countViews(); k++) {
            model.addViewWithStandardDeviation(data.viewWeights(k), data.viewReturn(k), BigDecimal.valueOf(STANDARD_DEVIATIONS[k]));
            omega[k] = STANDARD_DEVIATIONS[k] * STANDARD_DEVIATIONS[k];
        }

        BlackLittermanModelTest.assertMatchesReference(model, data, data.riskAversion, omega, 1.0);

        model.setConfidence(data.tau);
        model.setMode(BlackLittermanModel.Mode.RETURNS);
        TestUtils.assertEquals(data.tau, model.getConfidence().doubleValue());
        BlackLittermanModelTest.assertMatchesReference(model, data, data.riskAversion, omega, data.tau);

        model.setConfidence(null);
        TestUtils.assertEquals(1.0, model.getConfidence().doubleValue());
        BlackLittermanModelTest.assertMatchesReference(model, data, data.riskAversion, omega, 1.0);

        TestUtils.assertThrows(NullPointerException.class, () -> model.setMode(null));
    }

    /**
     * Changing the risk aversion after the results have been calculated changes the prior returns, and
     * thereby the posterior.
     */
    @Test
    public void testRiskAversionChange() {

        Case data = BlackLittermanModelTest.small();

        BlackLittermanModel model = data.newModel();
        model.setConfidence(data.tau);

        double[] omega = new double[data.countViews()];
        for (int k = 0; k < data.countViews(); k++) {
            model.addViewWithBalancedConfidence(data.viewWeights(k), data.viewReturn(k));
            omega[k] = data.tau * data.viewPortfolioVariance(k);
        }

        BlackLittermanModelTest.assertMatchesReference(model, data, data.riskAversion, omega, data.tau);

        model.setRiskAversion(4.0);

        BlackLittermanModelTest.assertMatchesReference(model, data, 4.0, omega, data.tau);
    }

    /**
     * Scaled confidence: [Omega]<sub>kk</sub> = scale<sub>k</sub> [p<sub>k</sub>][C][p<sub>k</sub>]<sup>T</sup>
     */
    @Test
    public void testScaledViews() {

        for (Case data : new Case[] { BlackLittermanModelTest.idzorek(), BlackLittermanModelTest.small() }) {

            BlackLittermanModel model = data.newModel();
            model.setConfidence(data.tau);

            double[] omega = new double[data.countViews()];

            for (int k = 0; k < data.countViews(); k++) {
                model.addViewWithScaledConfidence(data.viewWeights(k), data.viewReturn(k), SCALES[k]);
                omega[k] = SCALES[k] * data.viewPortfolioVariance(k);
            }

            BlackLittermanModelTest.assertMatchesReference(model, data, data.riskAversion, omega, data.tau);
        }
    }

    /**
     * Explicit standard deviation: [Omega]<sub>kk</sub> = (standard deviation)<sup>2</sup>
     */
    @Test
    public void testStandardDeviationViews() {

        for (Case data : new Case[] { BlackLittermanModelTest.idzorek(), BlackLittermanModelTest.small() }) {

            BlackLittermanModel model = data.newModel();
            model.setConfidence(data.tau);

            double[] omega = new double[data.countViews()];

            for (int k = 0; k < data.countViews(); k++) {
                model.addViewWithStandardDeviation(data.viewWeights(k), data.viewReturn(k), BigDecimal.valueOf(STANDARD_DEVIATIONS[k]));
                omega[k] = STANDARD_DEVIATIONS[k] * STANDARD_DEVIATIONS[k];
            }

            BlackLittermanModelTest.assertMatchesReference(model, data, data.riskAversion, omega, data.tau);
        }
    }

}
