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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.scalar.BigScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.TypeUtils;

/**
 * The Black-Litterman model combines a prior, the market equilibrium, with views (forecasts/opinions).
 * <p>
 * The prior expected (excess) returns are the equilibrium returns of the original (market) portfolio weights:
 * [Pi] = RAF [C][w]. Their uncertainty is modelled as proportional to the covariances: tau [C]. Each view is
 * a portfolio [p] with an expected return q and a variance (uncertainty) omega.
 * <p>
 * The posterior expected returns are [mu] = [Pi] + tau [C][P]<sup>T</sup>(tau [P][C][P]<sup>T</sup> +
 * [Omega])<sup>-1</sup>([Q] - [P][Pi]), where [P] has the view portfolios as rows, [Q] the view returns and
 * [Omega] the (diagonal) view variances. The asset weights are those that are optimal (unconstrained) given
 * the posterior returns and covariances.
 * <p>
 * What the posterior covariances are depends on the {@link Mode}.
 */
public final class BlackLittermanModel extends EquilibriumModel {

    /**
     * The model variant - which posterior distribution the model represents. Each mode has a default
     * confidence (tau), used unless the confidence is set explicitly.
     */
    public enum Mode {

        /**
         * Only the expected returns are updated by the views, the covariances are those of the prior (the
         * market equilibrium). This is how the model is used by Idzorek and by most practitioners (Walters
         * calls it the "alternative reference model"). tau only scales the view variances, and with views of
         * balanced confidence it has no effect at all. The default confidence is 1.0.
         */
        RETURNS(BigMath.ONE),

        /**
         * Black and Litterman's original model, as described by He and Litterman (Walters calls it the
         * "canonical reference model"). The returns are normally distributed with the posterior expected
         * returns and the covariances [C] + [M], where [M] = tau [C] - tau [C][P]<sup>T</sup>(tau
         * [P][C][P]<sup>T</sup> + [Omega])<sup>-1</sup>[P] tau [C] is the uncertainty of the posterior
         * expected returns. Here tau matters. The default confidence is 0.05 (as used by He and Litterman).
         * <p>
         * Without views the covariances are (1 + tau) [C], and the weights are the original weights divided
         * by (1 + tau) - they do not sum to 1. The remainder is (implicitly) invested in the risk-free asset.
         */
        FULL(new BigDecimal("0.05")),

        /**
         * Meucci's version: The views are about the returns themselves, rather than their expected values,
         * and the posterior is the conditional distribution of the returns given the views. It is normal with
         * the posterior expected returns and the covariances [C] - [C][P]<sup>T</sup>([P][C][P]<sup>T</sup> +
         * [Omega])<sup>-1</sup>[P][C] - smaller than the prior covariances.
         * <p>
         * There is no tau in Meucci's model. With the default confidence 1.0 this is exactly Meucci's model,
         * other values scale the view variances ([Omega] / tau). The expected returns are the same as in
         * {@link #RETURNS} mode (with the same tau). A certain view (zero variance) makes the covariances
         * singular.
         */
        MARKET(BigMath.ONE);

        private final BigDecimal myDefaultConfidence;

        Mode(final BigDecimal defaultConfidence) {
            myDefaultConfidence = defaultConfidence;
        }

        /**
         * The confidence (tau) used unless it is set explicitly.
         *
         * @see BlackLittermanModel#setConfidence(Comparable)
         */
        public Scalar<?> getDefaultConfidence() {
            return BigScalar.of(myDefaultConfidence);
        }

    }

    /**
     * View/Forecast/Opinion
     *
     * @author apete
     */
    private static final class View extends FinancePortfolio {

        private BigDecimal myConfidenceLevel = null;
        private BigDecimal myMeanReturn = BigMath.ZERO;
        private final BlackLittermanModel myModel;
        private BigDecimal myReturnVariance = null;
        private BigDecimal myScale = null;
        private final List<BigDecimal> myWeights;

        public View(final BlackLittermanModel aModel, final List<BigDecimal> someWeights) {

            super();

            myModel = aModel;
            myWeights = someWeights;
        }

        @Override
        public double getMeanReturn() {
            if (myMeanReturn != null) {
                return myMeanReturn.doubleValue();
            }
            return PrimitiveMath.ZERO;
        }

        @Override
        public double getReturnVariance() {

            if (myReturnVariance != null) {

                return myReturnVariance.doubleValue();

            }
            final MatrixR064 tmpWeights = MATRIX_FACTORY.column(myWeights);

            BigDecimal retVal = myModel.calculateVariance(tmpWeights);

            if (myScale != null) {

                retVal = retVal.multiply(myScale);

            } else {

                retVal = retVal.multiply(myModel.getConfidence().toBigDecimal());
            }

            if (myConfidenceLevel != null) {
                double level = myConfidenceLevel.doubleValue();
                return retVal.doubleValue() * (PrimitiveMath.ONE - level) / level;
            }

            return retVal.doubleValue();
        }

        @Override
        public List<BigDecimal> getWeights() {
            return myWeights;
        }

        @Override
        protected void reset() {}

        protected void setConfidenceLevel(final BigDecimal aConfidenceLevel) {
            myConfidenceLevel = aConfidenceLevel;
        }

        protected void setMeanReturn(final BigDecimal aMeanReturn) {
            myMeanReturn = aMeanReturn;
        }

        protected void setReturnVariance(final BigDecimal aReturnVariance) {
            myReturnVariance = aReturnVariance;
        }

        protected void setScale(final BigDecimal aScale) {
            myScale = aScale;
        }

    }

    /**
     * The covariances from the context, the original (market) weights from the portfolio, and the default
     * risk aversion (1.0).
     */
    public static BlackLittermanModel of(final Context context, final FinancePortfolio originalWeights) {
        return new BlackLittermanModel(MarketEquilibrium.of(context.getCovariances()), MATRIX_FACTORY.column(originalWeights.getWeights()));
    }

    /**
     * @param marketEquilibrium The covariance matrix, and market risk aversion
     * @param originalWeights   The market portfolio
     */
    public static BlackLittermanModel of(final MarketEquilibrium marketEquilibrium, final MatrixR064 originalWeights) {
        return new BlackLittermanModel(marketEquilibrium, originalWeights);
    }

    /**
     * null means the mode's default
     */
    private BigDecimal myConfidence = null;
    private Mode myMode = Mode.RETURNS;
    private final MatrixR064 myOriginalWeights;
    private final List<FinancePortfolio> myViews;

    /**
     * @deprecated v57 Use {@link #of(FinancePortfolio.Context, FinancePortfolio)} instead.
     */
    @Deprecated
    public BlackLittermanModel(final Context context, final FinancePortfolio originalWeights) {
        this(MarketEquilibrium.of(context.getCovariances()), MATRIX_FACTORY.column(originalWeights.getWeights()));
    }

    /**
     * @deprecated v57 Use {@link #of(MarketEquilibrium, MatrixR064)} instead. This constructor will become
     *             package-private.
     */
    @Deprecated
    public BlackLittermanModel(final MarketEquilibrium marketEquilibrium, final MatrixR064 originalWeights) {

        super(marketEquilibrium);

        myOriginalWeights = originalWeights;
        myViews = new ArrayList<>();
    }

    /**
     * A view on the return of a portfolio: its weights [p], mean return and return variance (omega).
     */
    public void addView(final FinancePortfolio aView) {
        myViews.add(aView);
        this.reset();
    }

    /**
     * The view variance is omega = tau [p][C][p]<sup>T</sup> - as uncertain as the prior return of the same
     * portfolio.
     */
    public void addViewWithBalancedConfidence(final List<BigDecimal> someWeights, final Comparable<?> aReturn) {

        View view = new View(this, someWeights);

        view.setMeanReturn(TypeUtils.toBigDecimal(aReturn));
        view.setReturnVariance(null);
        view.setScale(null);

        myViews.add(view);
        this.reset();
    }

    /**
     * Idzorek's method: The confidence level is the fraction, 0.0 < level <= 1.0, of the full confidence
     * (100%) weight tilt. On its own, the view tilts the weights level times as much as the same view would
     * if it was certain. That's achieved using the view variance omega = tau (1 - level) / level
     * [p][C][p]<sup>T</sup> (exact for the {@link Mode#RETURNS} mode).
     * <p>
     * A level of 1.0 (100%) means the view is certain, 0.5 (50%) is the same as balanced confidence, and
     * lower levels give the view less weight.
     *
     * @throws IllegalArgumentException unless 0.0 < level <= 1.0
     * @see #addViewWithBalancedConfidence(List, Comparable)
     */
    public void addViewWithConfidenceLevel(final List<BigDecimal> someWeights, final Comparable<?> aReturn, final Comparable<?> aLevel) {

        BigDecimal level = TypeUtils.toBigDecimal(aLevel);

        if (level.signum() <= 0 || level.compareTo(BigMath.ONE) > 0) {
            throw new IllegalArgumentException("The confidence level must be in the range (0.0, 1.0]: " + level);
        }

        View view = new View(this, someWeights);

        view.setMeanReturn(TypeUtils.toBigDecimal(aReturn));
        view.setReturnVariance(null);
        view.setScale(null);
        view.setConfidenceLevel(level);

        myViews.add(view);
        this.reset();
    }

    /**
     * The view variance is omega = scale [p][C][p]<sup>T</sup>.
     */
    public void addViewWithScaledConfidence(final List<BigDecimal> someWeights, final Comparable<?> aReturn, final Comparable<?> aScale) {

        View view = new View(this, someWeights);

        view.setMeanReturn(TypeUtils.toBigDecimal(aReturn));
        view.setReturnVariance(null);
        view.setScale(TypeUtils.toBigDecimal(aScale));

        myViews.add(view);
        this.reset();
    }

    public void addViewWithStandardDeviation(final List<BigDecimal> weights, final BigDecimal expected, final BigDecimal stdDev) {

        View view = new View(this, weights);

        view.setMeanReturn(expected);
        view.setReturnVariance(stdDev.multiply(stdDev));
        view.setScale(null);

        myViews.add(view);
        this.reset();
    }

    /**
     * The confidence, "weight on views" or "tau", scales the uncertainty of the prior (equilibrium) returns,
     * tau [C]. Unless set explicitly, the default of the current {@link Mode} is used.
     */
    public Scalar<?> getConfidence() {
        return BigScalar.of(this.confidence());
    }

    public Mode getMode() {
        return myMode;
    }

    /**
     * @param aWeight The confidence (tau), or null to use the default of the current {@link Mode}
     * @see #getConfidence()
     * @throws IllegalArgumentException if the confidence is not positive
     */
    public void setConfidence(final Comparable<?> aWeight) {

        if (aWeight == null) {

            myConfidence = null;

        } else {

            BigDecimal confidence = TypeUtils.toBigDecimal(aWeight);

            if (confidence.signum() <= 0) {
                throw new IllegalArgumentException("The confidence (weight on views) must be positive: " + confidence);
            }

            myConfidence = confidence;
        }

        this.reset();
    }

    /**
     * An explicitly set confidence (tau) is kept when the mode changes.
     */
    public void setMode(final Mode mode) {
        myMode = Objects.requireNonNull(mode);
        this.reset();
    }

    /**
     * The weights that are optimal given the posterior expected returns and the prior covariances: [w] +
     * [P]<sup>T</sup>([P][C][P]<sup>T</sup> + [Omega] / tau)<sup>-1</sup>([Q] / RAF - [P][C][w])
     */
    private MatrixR064 calculateTiltedWeights() {

        if (myViews.isEmpty()) {
            // No views - the posterior is the prior
            return myOriginalWeights;
        }

        final MatrixR064 tmpViewPortfolios = this.getViewPortfolios();
        final MatrixR064 tmpViewReturns = this.getViewReturns();
        final MatrixR064 tmpViewVariances = this.getViewVariances();

        final MatrixR064 tmpCovariances = this.priorCovariances();

        final MatrixR064 tmpRightParenthesis = tmpViewReturns.subtract(tmpViewPortfolios.multiply(tmpCovariances).multiply(myOriginalWeights));

        final MatrixR064 tmpViewsTransposed = tmpViewPortfolios.transpose();

        final MatrixR064 tmpLeftParenthesis = tmpViewVariances.add(tmpViewPortfolios.multiply(tmpCovariances).multiply(tmpViewsTransposed));

        return myOriginalWeights.add(tmpViewsTransposed.multiply(tmpLeftParenthesis.solve(tmpRightParenthesis)));
    }

    private BigDecimal confidence() {
        return myConfidence != null ? myConfidence : myMode.myDefaultConfidence;
    }

    /**
     * The covariances of the market equilibrium
     */
    private MatrixR064 priorCovariances() {
        return super.calculateCovariances();
    }

    /**
     * The posterior expected returns - the same for all modes.
     */
    @Override
    protected MatrixR064 calculateAssetReturns() {
        return this.calculateAssetReturns(this.calculateTiltedWeights());
    }

    @Override
    protected MatrixR064 calculateAssetWeights() {
        if (myMode == Mode.RETURNS) {
            return this.calculateTiltedWeights();
        } else {
            return this.getCovariances().solve(this.getAssetReturns()).divide(this.getRiskAversion().doubleValue());
        }
    }

    /**
     * With [K] = [C] - [C][P]<sup>T</sup>([P][C][P]<sup>T</sup> + [Omega] / tau)<sup>-1</sup>[P][C], the
     * conditional covariances, the posterior covariances are:
     * <ul>
     * <li>{@link Mode#RETURNS}: [C] (the prior covariances)
     * <li>{@link Mode#FULL}: [C] + tau [K]
     * <li>{@link Mode#MARKET}: [K]
     * </ul>
     */
    @Override
    protected MatrixR064 calculateCovariances() {

        MatrixR064 prior = this.priorCovariances();

        if (myMode == Mode.RETURNS) {
            return prior;
        }

        MatrixR064 conditional;

        if (myViews.isEmpty()) {

            conditional = prior;

        } else {

            MatrixR064 viewPortfolios = this.getViewPortfolios();
            MatrixR064 covariancesWithViews = prior.multiply(viewPortfolios.transpose());
            MatrixR064 inner = viewPortfolios.multiply(covariancesWithViews).add(this.getViewVariances());

            conditional = prior.subtract(covariancesWithViews.multiply(inner.solve(covariancesWithViews.transpose())));
        }

        MatrixR064 posterior;

        if (myMode == Mode.FULL) {
            posterior = prior.add(conditional.multiply(this.confidence().doubleValue()));
        } else {
            posterior = conditional;
        }

        // Symmetric also with rounding errors
        return posterior.add(posterior.transpose()).divide(PrimitiveMath.TWO);
    }

    /**
     * [p][C][p]<sup>T</sup> using the prior covariances
     */
    BigDecimal calculateVariance(final MatrixR064 weights) {

        MatrixR064 tmpVal = this.priorCovariances();

        tmpVal = tmpVal.multiply(weights);

        return TypeUtils.toBigDecimal(weights.transpose().multiply(tmpVal).get(0, 0));
    }

    MatrixR064 getOriginalReturns() {
        return this.calculateAssetReturns(myOriginalWeights);
    }

    /**
     * The market (prior) portfolio weights.
     */
    MatrixR064 getOriginalWeights() {
        return myOriginalWeights;
    }

    MatrixR064 getViewPortfolios() {

        final int tmpRowDim = myViews.size();
        final int tmpColDim = (int) myOriginalWeights.count();

        final MatrixR064.DenseReceiver retVal = MATRIX_FACTORY.newDenseBuilder(tmpRowDim, tmpColDim);

        FinancePortfolio tmpView;
        List<BigDecimal> tmpWeights;

        for (int i = 0; i < tmpRowDim; i++) {

            tmpView = myViews.get(i);
            tmpWeights = tmpView.getWeights();

            for (int j = 0; j < tmpColDim; j++) {
                retVal.set(i, j, tmpWeights.get(j));
            }
        }

        return retVal.get();
    }

    /**
     * Scaled by risk aversion factor.
     */
    MatrixR064 getViewReturns() {

        final int tmpRowDim = myViews.size();
        final int tmpColDim = 1;

        final MatrixR064.DenseReceiver retVal = MATRIX_FACTORY.newDenseBuilder(tmpRowDim, tmpColDim);

        double tmpRet;
        final double tmpRAF = this.getRiskAversion().doubleValue();

        for (int i = 0; i < tmpRowDim; i++) {

            tmpRet = myViews.get(i).getMeanReturn();

            retVal.set(i, 0, PrimitiveMath.DIVIDE.invoke(tmpRet, tmpRAF));
        }

        return retVal.get();
    }

    List<FinancePortfolio> getViews() {
        return myViews;
    }

    /**
     * Scaled by tau / weight on views
     */
    MatrixR064 getViewVariances() {

        final int tmpDim = myViews.size();

        final MatrixR064.DenseReceiver retVal = MATRIX_FACTORY.newDenseBuilder(tmpDim, tmpDim);

        final double tmpScale = this.confidence().doubleValue();

        for (int ij = 0; ij < tmpDim; ij++) {
            retVal.set(ij, ij, myViews.get(ij).getReturnVariance() / tmpScale);
        }

        return retVal.get();
    }
}
