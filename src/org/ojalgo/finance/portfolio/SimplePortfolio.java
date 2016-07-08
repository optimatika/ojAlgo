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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.ojalgo.access.Access2D;
import org.ojalgo.finance.portfolio.FinancePortfolio.Context;
import org.ojalgo.finance.portfolio.simulator.PortfolioSimulator;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.BasicMatrix.Builder;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.random.process.GeometricBrownianMotion;

public final class SimplePortfolio extends FinancePortfolio implements Context {

    static List<SimpleAsset> toSimpleAssets(final Number[] someWeights) {

        final ArrayList<SimpleAsset> retVal = new ArrayList<>(someWeights.length);

        for (int i = 0; i < someWeights.length; i++) {
            retVal.add(new SimpleAsset(someWeights[i]));
        }

        return retVal;
    }

    private transient BasicMatrix myAssetReturns = null;
    private transient BasicMatrix myAssetVolatilities = null;
    private transient BasicMatrix myAssetWeights = null;
    private final List<SimpleAsset> myComponents;
    private final BasicMatrix myCorrelations;
    private transient BasicMatrix myCovariances = null;
    private transient Number myMeanReturn;
    private transient Number myReturnVariance;

    private transient List<BigDecimal> myWeights;

    public SimplePortfolio(final Access2D<?> correlationsMatrix, final List<SimpleAsset> someAssets) {

        super();

        if ((someAssets.size() != correlationsMatrix.countRows()) || (someAssets.size() != correlationsMatrix.countColumns())) {
            throw new IllegalArgumentException("Input dimensions don't match!");
        }

        myCorrelations = MATRIX_FACTORY.copy(correlationsMatrix);
        myComponents = someAssets;
    }

    public SimplePortfolio(final Context aContext, final FinancePortfolio weightsPortfolio) {

        super();

        myCorrelations = aContext.getCorrelations();

        final BasicMatrix tmpCovariances = aContext.getCovariances();
        final BasicMatrix tmpAssetReturns = aContext.getAssetReturns();

        final List<BigDecimal> tmpWeights = weightsPortfolio.getWeights();

        if ((tmpWeights.size() != myCorrelations.countRows()) || (tmpWeights.size() != myCorrelations.countColumns())) {
            throw new IllegalArgumentException("Input dimensions don't match!");
        }

        myComponents = new ArrayList<>(tmpWeights.size());
        for (int i = 0; i < tmpWeights.size(); i++) {
            final double tmpMeanReturn = tmpAssetReturns.doubleValue(i, 0);
            final double tmpVolatilty = Math.sqrt(tmpCovariances.doubleValue(i, i));
            final BigDecimal tmpWeight = tmpWeights.get(i);
            myComponents.add(new SimpleAsset(tmpMeanReturn, tmpVolatilty, tmpWeight));
        }
    }

    public SimplePortfolio(final List<SimpleAsset> someAssets) {
        this(MATRIX_FACTORY.makeEye(someAssets.size(), someAssets.size()), someAssets);
    }

    public SimplePortfolio(final Number... someWeights) {
        this(SimplePortfolio.toSimpleAssets(someWeights));
    }

    @SuppressWarnings("unused")
    private SimplePortfolio() {
        this((BasicMatrix) null, null);
    }

    public double calculatePortfolioReturn(final FinancePortfolio weightsPortfolio) {
        final List<BigDecimal> tmpWeights = weightsPortfolio.getWeights();
        final BasicMatrix tmpAssetWeights = MATRIX_FACTORY.columns(tmpWeights);
        final BasicMatrix tmpAssetReturns = this.getAssetReturns();
        return MarketEquilibrium.calculatePortfolioReturn(tmpAssetWeights, tmpAssetReturns).doubleValue();
    }

    public double calculatePortfolioVariance(final FinancePortfolio weightsPortfolio) {
        final List<BigDecimal> tmpWeights = weightsPortfolio.getWeights();
        final BasicMatrix tmpAssetWeights = MATRIX_FACTORY.columns(tmpWeights);
        return new MarketEquilibrium(this.getCovariances()).calculatePortfolioVariance(tmpAssetWeights).doubleValue();
    }

    public BasicMatrix getAssetReturns() {

        if (myAssetReturns == null) {

            final int tmpSize = myComponents.size();

            final Builder<PrimitiveMatrix> tmpReturns = MATRIX_FACTORY.getBuilder(tmpSize, 1);

            for (int i = 0; i < tmpSize; i++) {
                tmpReturns.set(i, 0, this.getMeanReturn(i));
            }

            myAssetReturns = tmpReturns.build();
        }

        return myAssetReturns;
    }

    public BasicMatrix getAssetVolatilities() {

        if (myAssetVolatilities == null) {

            final int tmpSize = myComponents.size();

            final Builder<PrimitiveMatrix> tmpVolatilities = MATRIX_FACTORY.getBuilder(tmpSize, 1);

            for (int i = 0; i < tmpSize; i++) {
                tmpVolatilities.set(i, 0, this.getVolatility(i));
            }

            myAssetVolatilities = tmpVolatilities.build();
        }

        return myAssetVolatilities;
    }

    public double getCorrelation(final int row, final int col) {
        return myCorrelations.doubleValue(row, col);
    }

    public BasicMatrix getCorrelations() {
        return myCorrelations;
    }

    public double getCovariance(final int row, final int col) {

        final BasicMatrix tmpCovariances = myCovariances;

        if (tmpCovariances != null) {

            return tmpCovariances.doubleValue(row, col);

        } else {

            final double tmpRowRisk = this.getVolatility(row);
            final double tmpColRisk = this.getVolatility(col);

            final double tmpCorrelation = this.getCorrelation(row, col);

            return tmpRowRisk * tmpCorrelation * tmpColRisk;
        }
    }

    public BasicMatrix getCovariances() {

        if (myCovariances == null) {

            final int tmpSize = myComponents.size();

            final Builder<PrimitiveMatrix> tmpCovaris = MATRIX_FACTORY.getBuilder(tmpSize, tmpSize);

            for (int j = 0; j < tmpSize; j++) {
                for (int i = 0; i < tmpSize; i++) {
                    tmpCovaris.set(i, j, this.getCovariance(i, j));
                }
            }

            myCovariances = tmpCovaris.build();
        }

        return myCovariances;
    }

    @Override
    public double getMeanReturn() {

        if (myMeanReturn == null) {
            final BasicMatrix tmpWeightsVector = this.getAssetWeights();
            final BasicMatrix tmpReturnsVector = this.getAssetReturns();
            myMeanReturn = MarketEquilibrium.calculatePortfolioReturn(tmpWeightsVector, tmpReturnsVector).getNumber();
        }

        return myMeanReturn.doubleValue();
    }

    public double getMeanReturn(final int index) {
        return myComponents.get(index).getMeanReturn();
    }

    @Override
    public double getReturnVariance() {

        if (myReturnVariance == null) {
            final MarketEquilibrium tmpMarketEquilibrium = new MarketEquilibrium(this.getCovariances());
            final BasicMatrix tmpWeightsVector = this.getAssetWeights();
            myReturnVariance = tmpMarketEquilibrium.calculatePortfolioVariance(tmpWeightsVector).getNumber();
        }

        return myReturnVariance.doubleValue();
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

        return new PortfolioSimulator(myCorrelations.toPrimitiveStore(), tmpAssetProcesses);

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

    BasicMatrix getAssetWeights() {

        if (myAssetWeights == null) {

            final int tmpSize = myComponents.size();

            final Builder<PrimitiveMatrix> tmpWeights = MATRIX_FACTORY.getBuilder(tmpSize, 1);

            for (int i = 0; i < tmpSize; i++) {
                tmpWeights.set(i, 0, this.getWeight(i));
            }

            myAssetWeights = tmpWeights.build();
        }

        return myAssetWeights;
    }

}
