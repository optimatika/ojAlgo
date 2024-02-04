/*
 * Copyright 1997-2024 Optimatika
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

import org.ojalgo.data.domain.finance.portfolio.FinancePortfolio.Context;
import org.ojalgo.data.domain.finance.portfolio.simulator.PortfolioSimulator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.random.process.GeometricBrownianMotion;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access2D;

public final class SimplePortfolio extends FinancePortfolio implements Context {

    static List<SimpleAsset> toSimpleAssets(final double[] someWeights) {

        final ArrayList<SimpleAsset> retVal = new ArrayList<>(someWeights.length);

        for (int i = 0; i < someWeights.length; i++) {
            retVal.add(new SimpleAsset(someWeights[i]));
        }

        return retVal;
    }

    static List<SimpleAsset> toSimpleAssets(final Comparable<?>[] someWeights) {

        final ArrayList<SimpleAsset> retVal = new ArrayList<>(someWeights.length);

        for (int i = 0; i < someWeights.length; i++) {
            retVal.add(new SimpleAsset(someWeights[i]));
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

    public SimplePortfolio(final Access2D<?> correlationsMatrix, final List<SimpleAsset> someAssets) {

        super();

        if (someAssets.size() != correlationsMatrix.countRows() || someAssets.size() != correlationsMatrix.countColumns()) {
            throw new IllegalArgumentException("Input dimensions don't match!");
        }

        myCorrelations = MATRIX_FACTORY.copy(correlationsMatrix);
        myComponents = someAssets;
    }

    public SimplePortfolio(final Context portfolioContext, final FinancePortfolio weightsPortfolio) {

        super();

        myCorrelations = portfolioContext.getCorrelations();

        final MatrixR064 tmpCovariances = portfolioContext.getCovariances();
        final MatrixR064 tmpAssetReturns = portfolioContext.getAssetReturns();

        final List<BigDecimal> tmpWeights = weightsPortfolio.getWeights();

        if (tmpWeights.size() != myCorrelations.countRows() || tmpWeights.size() != myCorrelations.countColumns()) {
            throw new IllegalArgumentException("Input dimensions don't match!");
        }

        myComponents = new ArrayList<>(tmpWeights.size());
        for (int i = 0; i < tmpWeights.size(); i++) {
            final double tmpMeanReturn = tmpAssetReturns.doubleValue(i, 0);
            final double tmpVolatilty = PrimitiveMath.SQRT.invoke(tmpCovariances.doubleValue(i, i));
            final BigDecimal tmpWeight = tmpWeights.get(i);
            myComponents.add(new SimpleAsset(tmpMeanReturn, tmpVolatilty, tmpWeight));
        }
    }

    public SimplePortfolio(final double[] someWeights) {
        this(SimplePortfolio.toSimpleAssets(someWeights));
    }

    public SimplePortfolio(final List<SimpleAsset> someAssets) {
        this(MATRIX_FACTORY.makeEye(someAssets.size(), someAssets.size()), someAssets);
    }

    public SimplePortfolio(final Comparable<?>... someWeights) {
        this(SimplePortfolio.toSimpleAssets(someWeights));
    }

    public double calculatePortfolioReturn(final FinancePortfolio weightsPortfolio) {
        final List<BigDecimal> tmpWeights = weightsPortfolio.getWeights();
        final MatrixR064 tmpAssetWeights = MATRIX_FACTORY.columns(tmpWeights);
        final MatrixR064 tmpAssetReturns = this.getAssetReturns();
        return MarketEquilibrium.calculatePortfolioReturn(tmpAssetWeights, tmpAssetReturns).doubleValue();
    }

    public double calculatePortfolioVariance(final FinancePortfolio weightsPortfolio) {
        final List<BigDecimal> tmpWeights = weightsPortfolio.getWeights();
        final MatrixR064 tmpAssetWeights = MATRIX_FACTORY.columns(tmpWeights);
        return new MarketEquilibrium(this.getCovariances()).calculatePortfolioVariance(tmpAssetWeights).doubleValue();
    }

    public MatrixR064 getAssetReturns() {

        if (myAssetReturns == null) {

            final int tmpSize = myComponents.size();

            final MatrixR064.DenseReceiver tmpReturns = MATRIX_FACTORY.makeDense(tmpSize, 1);

            for (int i = 0; i < tmpSize; i++) {
                tmpReturns.set(i, 0, this.getMeanReturn(i));
            }

            myAssetReturns = tmpReturns.get();
        }

        return myAssetReturns;
    }

    public MatrixR064 getAssetVolatilities() {

        if (myAssetVolatilities == null) {

            final int tmpSize = myComponents.size();

            final MatrixR064.DenseReceiver tmpVolatilities = MATRIX_FACTORY.makeDense(tmpSize, 1);

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

    public MatrixR064 getCovariances() {

        if (myCovariances == null) {

            final int tmpSize = myComponents.size();

            final MatrixR064.DenseReceiver tmpCovaris = MATRIX_FACTORY.makeDense(tmpSize, tmpSize);

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
            final MarketEquilibrium tmpMarketEquilibrium = new MarketEquilibrium(this.getCovariances());
            final MatrixR064 tmpWeightsVector = this.getAssetWeights();
            myReturnVariance = tmpMarketEquilibrium.calculatePortfolioVariance(tmpWeightsVector).get();
        }

        return Scalar.doubleValue(myReturnVariance);
    }

    public double getReturnVariance(final int index) {
        return myComponents.get(index).getReturnVariance();
    }

    public PortfolioSimulator getSimulator() {

        final List<GeometricBrownianMotion> tmpAssetProcesses = new ArrayList<>(myComponents.size());

        for (final SimpleAsset tmpAsset : myComponents) {
            final GeometricBrownianMotion tmpForecast = tmpAsset.forecast();
            tmpForecast.setValue(tmpAsset.getWeight().doubleValue());
            tmpAssetProcesses.add(tmpForecast);
        }

        return new PortfolioSimulator(myCorrelations, tmpAssetProcesses);
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

            final MatrixR064.DenseReceiver tmpWeights = MATRIX_FACTORY.makeDense(tmpSize, 1);

            for (int i = 0; i < tmpSize; i++) {
                tmpWeights.set(i, 0, this.getWeight(i));
            }

            myAssetWeights = tmpWeights.get();
        }

        return myAssetWeights;
    }

}
