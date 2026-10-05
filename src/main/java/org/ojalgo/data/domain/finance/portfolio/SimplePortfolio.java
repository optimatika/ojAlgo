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

import org.ojalgo.data.domain.finance.FinanceUtils;
import org.ojalgo.data.domain.finance.portfolio.FinancePortfolio.Context;
import org.ojalgo.data.domain.finance.portfolio.simulator.PortfolioSimulator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.random.process.GeometricBrownianMotion;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access2D;

/**
 * A portfolio defined by its assets ({@link SimpleAsset} - weight, mean return and volatility) and their
 * correlations. It is also a {@link FinancePortfolio.Context} for other portfolios of the same assets.
 */
public final class SimplePortfolio extends FinancePortfolio implements Context {

    /**
     * @param correlations The correlations between the assets
     * @param assets       The assets (with mean return, volatility and weight)
     */
    public static SimplePortfolio of(final Access2D<?> correlations, final List<SimpleAsset> assets) {
        return new SimplePortfolio(MATRIX_FACTORY.copy(correlations), assets);
    }

    /**
     * The mean returns and volatilities from the context (its asset returns and covariances), the
     * correlations from the context, and the weights from the portfolio.
     */
    public static SimplePortfolio of(final Context portfolioContext, final FinancePortfolio weightsPortfolio) {
        return new SimplePortfolio(portfolioContext.getCorrelations(), SimplePortfolio.toSimpleAssets(portfolioContext, weightsPortfolio));
    }

    /**
     * Uncorrelated assets
     */
    public static SimplePortfolio of(final List<SimpleAsset> assets) {
        return new SimplePortfolio(MATRIX_FACTORY.makeEye(assets.size(), assets.size()), assets);
    }

    /**
     * Only weights - uncorrelated assets with mean return and volatility 0.0
     */
    public static SimplePortfolio ofWeights(final Comparable<?>... weights) {
        return SimplePortfolio.of(SimplePortfolio.toSimpleAssets(weights));
    }

    /**
     * Only weights - uncorrelated assets with mean return and volatility 0.0
     */
    public static SimplePortfolio ofWeights(final double[] weights) {
        return SimplePortfolio.of(SimplePortfolio.toSimpleAssets(weights));
    }

    static List<SimpleAsset> toSimpleAssets(final Comparable<?>[] someWeights) {

        final ArrayList<SimpleAsset> retVal = new ArrayList<>(someWeights.length);

        for (int i = 0; i < someWeights.length; i++) {
            retVal.add(SimpleAsset.ofWeight(someWeights[i]));
        }

        return retVal;
    }

    static List<SimpleAsset> toSimpleAssets(final double[] someWeights) {

        final ArrayList<SimpleAsset> retVal = new ArrayList<>(someWeights.length);

        for (int i = 0; i < someWeights.length; i++) {
            retVal.add(SimpleAsset.ofWeight(someWeights[i]));
        }

        return retVal;
    }

    private static List<SimpleAsset> toSimpleAssets(final Context portfolioContext, final FinancePortfolio weightsPortfolio) {

        final MatrixR064 tmpCovariances = portfolioContext.getCovariances();
        final MatrixR064 tmpAssetReturns = portfolioContext.getAssetReturns();

        final List<BigDecimal> tmpWeights = weightsPortfolio.getWeights();

        if (tmpWeights.size() != portfolioContext.size()) {
            throw new IllegalArgumentException("Input dimensions don't match!");
        }

        final List<SimpleAsset> retVal = new ArrayList<>(tmpWeights.size());
        for (int i = 0; i < tmpWeights.size(); i++) {
            final double tmpMeanReturn = tmpAssetReturns.doubleValue(i);
            final double tmpVolatility = PrimitiveMath.SQRT.invoke(tmpCovariances.doubleValue(i, i));
            retVal.add(new SimpleAsset(tmpMeanReturn, tmpVolatility, tmpWeights.get(i)));
        }

        return retVal;
    }

    private transient MatrixR064 myAssetReturns = null;
    private transient MatrixR064 myAssetVolatilities = null;
    private transient MatrixR064 myAssetWeights = null;
    private final List<SimpleAsset> myComponents;
    private final MatrixR064 myCorrelations;
    private transient MatrixR064 myCovariances = null;
    private transient Comparable<?> myMeanReturn;
    private transient Comparable<?> myReturnVariance;
    private transient List<BigDecimal> myWeights;

    /**
     * @deprecated v57 Use {@link #of(Access2D, List)} instead.
     */
    @Deprecated
    public SimplePortfolio(final Access2D<?> correlationsMatrix, final List<SimpleAsset> someAssets) {
        this(MATRIX_FACTORY.copy(correlationsMatrix), someAssets);
    }

    /**
     * @deprecated v57 Use {@link #ofWeights(Comparable...)} instead.
     */
    @Deprecated
    public SimplePortfolio(final Comparable<?>... someWeights) {
        this(SimplePortfolio.toSimpleAssets(someWeights));
    }

    /**
     * @deprecated v57 Use {@link #of(FinancePortfolio.Context, FinancePortfolio)} instead.
     */
    @Deprecated
    public SimplePortfolio(final Context portfolioContext, final FinancePortfolio weightsPortfolio) {
        this(portfolioContext.getCorrelations(), SimplePortfolio.toSimpleAssets(portfolioContext, weightsPortfolio));
    }

    /**
     * @deprecated v57 Use {@link #ofWeights(double[])} instead.
     */
    @Deprecated
    public SimplePortfolio(final double[] someWeights) {
        this(SimplePortfolio.toSimpleAssets(someWeights));
    }

    /**
     * @deprecated v57 Use {@link #of(List)} instead.
     */
    @Deprecated
    public SimplePortfolio(final List<SimpleAsset> someAssets) {
        this(MATRIX_FACTORY.makeEye(someAssets.size(), someAssets.size()), someAssets);
    }

    SimplePortfolio(final MatrixR064 correlations, final List<SimpleAsset> assets) {

        super();

        if (assets.size() != correlations.countRows() || assets.size() != correlations.countColumns()) {
            throw new IllegalArgumentException("Input dimensions don't match!");
        }

        myCorrelations = correlations;
        myComponents = new ArrayList<>(assets);
    }

    @Override
    public double calculatePortfolioReturn(final FinancePortfolio weightsPortfolio) {
        final List<BigDecimal> tmpWeights = weightsPortfolio.getWeights();
        final MatrixR064 tmpAssetWeights = MATRIX_FACTORY.column(tmpWeights);
        final MatrixR064 tmpAssetReturns = this.getAssetReturns();
        return MarketEquilibrium.calculatePortfolioReturn(tmpAssetWeights, tmpAssetReturns).doubleValue();
    }

    @Override
    public double calculatePortfolioVariance(final FinancePortfolio weightsPortfolio) {
        final List<BigDecimal> tmpWeights = weightsPortfolio.getWeights();
        final MatrixR064 tmpAssetWeights = MATRIX_FACTORY.column(tmpWeights);
        return tmpAssetWeights.dot(this.getCovariances().multiply(tmpAssetWeights));
    }

    @Override
    public MatrixR064 getAssetReturns() {

        if (myAssetReturns == null) {

            final int tmpSize = myComponents.size();

            final MatrixR064.DenseReceiver tmpReturns = MATRIX_FACTORY.newDenseBuilder(tmpSize, 1);

            for (int i = 0; i < tmpSize; i++) {
                tmpReturns.set(i, 0, this.getMeanReturn(i));
            }

            myAssetReturns = tmpReturns.get();
        }

        return myAssetReturns;
    }

    @Override
    public MatrixR064 getAssetVolatilities() {

        if (myAssetVolatilities == null) {

            final int tmpSize = myComponents.size();

            final MatrixR064.DenseReceiver tmpVolatilities = MATRIX_FACTORY.newDenseBuilder(tmpSize, 1);

            for (int i = 0; i < tmpSize; i++) {
                tmpVolatilities.set(i, 0, this.getVolatility(i));
            }

            myAssetVolatilities = tmpVolatilities.get();
        }

        return myAssetVolatilities;
    }

    public double getCorrelation(final int row, final int col) {
        return myCorrelations.doubleValue(row, col);
    }

    @Override
    public MatrixR064 getCorrelations() {
        return myCorrelations;
    }

    public double getCovariance(final int row, final int col) {

        final MatrixR064 tmpCovariances = myCovariances;

        if (tmpCovariances != null) {
            return tmpCovariances.doubleValue(row, col);
        }

        final double tmpRowRisk = this.getVolatility(row);
        final double tmpColRisk = this.getVolatility(col);

        final double tmpCorrelation = this.getCorrelation(row, col);

        return tmpRowRisk * tmpCorrelation * tmpColRisk;
    }

    @Override
    public MatrixR064 getCovariances() {

        if (myCovariances == null) {

            final int tmpSize = myComponents.size();

            final MatrixR064.DenseReceiver tmpCovaris = MATRIX_FACTORY.newDenseBuilder(tmpSize, tmpSize);

            for (int j = 0; j < tmpSize; j++) {
                for (int i = 0; i < tmpSize; i++) {
                    tmpCovaris.set(i, j, this.getCovariance(i, j));
                }
            }

            myCovariances = tmpCovaris.get();
        }

        return myCovariances;
    }

    @Override
    public double getMeanReturn() {

        if (myMeanReturn == null) {
            final MatrixR064 tmpWeightsVector = this.getAssetWeights();
            final MatrixR064 tmpReturnsVector = this.getAssetReturns();
            myMeanReturn = MarketEquilibrium.calculatePortfolioReturn(tmpWeightsVector, tmpReturnsVector).get();
        }

        return Scalar.doubleValue(myMeanReturn);
    }

    public double getMeanReturn(final int index) {
        return myComponents.get(index).getMeanReturn();
    }

    @Override
    public double getReturnVariance() {

        if (myReturnVariance == null) {
            final MatrixR064 tmpWeightsVector = this.getAssetWeights();
            myReturnVariance = tmpWeightsVector.dot(this.getCovariances().multiply(tmpWeightsVector));
        }

        return Scalar.doubleValue(myReturnVariance);
    }

    public double getReturnVariance(final int index) {
        return myComponents.get(index).getReturnVariance();
    }

    /**
     * Each asset is modelled by its {@link SimpleAsset#forecast()}, with the initial value set to its weight.
     * The asset processes are correlated so that the simulated returns (one time unit ahead) have the
     * covariances of this portfolio - the correlations of the processes' growth rates are derived using
     * {@link FinanceUtils#toGrowthRateCovariancesFromReturns}.
     */
    public PortfolioSimulator getSimulator() {

        final List<GeometricBrownianMotion> tmpAssetProcesses = new ArrayList<>(myComponents.size());

        for (final SimpleAsset tmpAsset : myComponents) {
            final GeometricBrownianMotion tmpForecast = tmpAsset.forecast();
            tmpForecast.setValue(tmpAsset.getWeight().doubleValue());
            tmpAssetProcesses.add(tmpForecast);
        }

        MatrixR064 growthRateCovariances = FinanceUtils.toGrowthRateCovariancesFromReturns(this.getAssetReturns(), this.getCovariances());

        return PortfolioSimulator.of(FinanceUtils.toCorrelations(growthRateCovariances), tmpAssetProcesses);
    }

    public double getVolatility(final int index) {
        return myComponents.get(index).getVolatility();
    }

    public BigDecimal getWeight(final int index) {
        return myComponents.get(index).getWeight();
    }

    @Override
    public List<BigDecimal> getWeights() {

        if (myWeights == null) {

            myWeights = new ArrayList<>(myComponents.size());

            for (final SimpleAsset tmpAsset : myComponents) {
                myWeights.add(tmpAsset.getWeight());
            }
        }

        return myWeights;
    }

    @Override
    public int size() {
        return myComponents.size();
    }

    @Override
    protected void reset() {

        myMeanReturn = null;
        myReturnVariance = null;
        myWeights = null;

        myCovariances = null;
        myAssetReturns = null;
        myAssetVolatilities = null;
        myAssetWeights = null;

        for (final SimpleAsset tmpAsset : myComponents) {
            tmpAsset.reset();
        }
    }

    MatrixR064 getAssetWeights() {

        if (myAssetWeights == null) {

            final int tmpSize = myComponents.size();

            final MatrixR064.DenseReceiver tmpWeights = MATRIX_FACTORY.newDenseBuilder(tmpSize, 1);

            for (int i = 0; i < tmpSize; i++) {
                tmpWeights.set(i, 0, this.getWeight(i));
            }

            myAssetWeights = tmpWeights.get();
        }

        return myAssetWeights;
    }

}
