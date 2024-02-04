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

import org.ojalgo.array.operation.COPY;
import org.ojalgo.data.domain.finance.FinanceUtils;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.scalar.BigScalar;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access2D;
import org.ojalgo.type.TypeUtils;

/**
 * <p>
 * MarketEquilibrium translates between the market portfolio weights and the equilibrium excess returns. The
 * only things needed to do those translations are the covariance matrix and the (market) risk aversion factor
 * - that's what you need to supply when you instantiate this class.
 * </p>
 * <p>
 * This class performs unconstrained optimisation. For each set of asset returns there is an optimal set of
 * weights. It also performs reverse (unconstrained) optimisation producing the "optimal" expected returns
 * given a set of weights.
 * </p>
 * <p>
 * The name MarketEquilibrium is actually a bit misleading. By altering the risk aversion factor this class
 * can/will describe the weights/returns equilibrium for any investor.
 * </p>
 *
 * @see #calculateAssetReturns(MatrixR064)
 * @see #calculateAssetWeights(MatrixR064)
 * @author apete
 */
public class MarketEquilibrium {

    private static final BigDecimal DEFAULT_RISK_AVERSION = BigMath.ONE; // Don't change the default!
    private static final String STRING_ZERO = "0";
    private static final String SYMBOL = "Asset_";

    /**
     * Calculates the portfolio return using the input asset weights and returns.
     */
    public static Scalar<?> calculatePortfolioReturn(final MatrixR064 assetWeights, final MatrixR064 assetReturns) {
        return PrimitiveScalar.valueOf(assetWeights.dot(assetReturns));
    }

    private static String[] makeSymbols(final int count) {

        final String[] retVal = new String[count];

        final int tmpMaxLength = Integer.toString(count - 1).length();

        String tmpNumberString;
        for (int i = 0; i < count; i++) {
            tmpNumberString = Integer.toString(i);
            while (tmpNumberString.length() < tmpMaxLength) {
                tmpNumberString = STRING_ZERO + tmpNumberString;
            }
            retVal[i] = SYMBOL + tmpNumberString;
        }

        return retVal;
    }

    private final String[] myAssetKeys;
    private final MatrixR064 myCovariances;
    private BigDecimal myRiskAversion;

    public MarketEquilibrium(final Access2D<?> covarianceMatrix) {
        this(covarianceMatrix, DEFAULT_RISK_AVERSION);
    }

    public MarketEquilibrium(final Access2D<?> covarianceMatrix, final Comparable<?> riskAversionFactor) {
        this(MarketEquilibrium.makeSymbols((int) covarianceMatrix.countRows()), covarianceMatrix, riskAversionFactor);
    }

    public MarketEquilibrium(final String[] assetNamesOrKeys, final Access2D<?> covarianceMatrix) {
        this(assetNamesOrKeys, covarianceMatrix, DEFAULT_RISK_AVERSION);
    }

    public MarketEquilibrium(final String[] assetNamesOrKeys, final Access2D<?> covarianceMatrix, final Comparable<?> riskAversionFactor) {

        super();

        myAssetKeys = COPY.copyOf(assetNamesOrKeys);
        if (covarianceMatrix instanceof MatrixR064) {
            myCovariances = (MatrixR064) covarianceMatrix;
        } else {
            myCovariances = MatrixR064.FACTORY.copy(covarianceMatrix);
        }

        myRiskAversion = TypeUtils.toBigDecimal(riskAversionFactor);
    }

    MarketEquilibrium(final MarketEquilibrium marketEquilibrium) {
        this(marketEquilibrium.getAssetKeys(), marketEquilibrium.getCovariances(), marketEquilibrium.getRiskAversion().get());
    }

    /**
     * If the input vector of asset weights are the weights of the market portfolio, then the ouput is the
     * equilibrium excess returns.
     */
    public MatrixR064 calculateAssetReturns(final MatrixR064 assetWeights) {
        final MatrixR064 tmpAssetWeights = myRiskAversion.compareTo(DEFAULT_RISK_AVERSION) == 0 ? assetWeights
                : assetWeights.multiply(myRiskAversion.doubleValue());
        return myCovariances.multiply(tmpAssetWeights);
    }

    /**
     * If the input vector of returns are the equilibrium excess returns then the output is the market
     * portfolio weights. This is unconstrained optimisation - there are no constraints on the resulting
     * instrument weights.
     */
    public MatrixR064 calculateAssetWeights(final MatrixR064 assetReturns) {
        final MatrixR064 tmpAssetWeights = myCovariances.solve(assetReturns);
        if (myRiskAversion.compareTo(DEFAULT_RISK_AVERSION) == 0) {
            return tmpAssetWeights;
        } else {
            return tmpAssetWeights.divide(myRiskAversion.doubleValue());
        }
    }

    /**
     * Calculates the portfolio variance using the input instrument weights.
     */
    public Scalar<?> calculatePortfolioVariance(final MatrixR064 assetWeights) {

        MatrixR064 tmpLeft;
        MatrixR064 tmpRight;

        if (assetWeights.countColumns() == 1L) {
            tmpLeft = assetWeights.transpose();
            tmpRight = assetWeights;
        } else {
            tmpLeft = assetWeights;
            tmpRight = assetWeights.transpose();
        }

        return tmpLeft.multiply(myCovariances.multiply(tmpRight)).toScalar(0, 0);
    }

    /**
     * Will set the risk aversion factor to the best fit for an observed pair of market portfolio asset
     * weights and equilibrium/historical excess returns.
     */
    public void calibrate(final MatrixR064 assetWeights, final MatrixR064 assetReturns) {

        final Scalar<?> tmpImpliedRiskAversion = this.calculateImpliedRiskAversion(assetWeights, assetReturns);

        this.setRiskAversion(tmpImpliedRiskAversion.get());
    }

    /**
     * Equivalent to copying, but additionally the covariance matrix will be cleaned of negative and very
     * small eigenvalues to make it positive definite.
     */
    public MarketEquilibrium clean() {

        final MatrixR064 tmpAssetVolatilities = FinanceUtils.toVolatilities(myCovariances, true);
        final MatrixR064 tmpCleanedCorrelations = FinanceUtils.toCorrelations(myCovariances, true);

        final MatrixR064 tmpCovariances = FinanceUtils.toCovariances(tmpAssetVolatilities, tmpCleanedCorrelations);

        return new MarketEquilibrium(myAssetKeys, tmpCovariances, myRiskAversion);
    }

    public MarketEquilibrium copy() {
        return new MarketEquilibrium(this);
    }

    public String getAssetKey(final int index) {
        return myAssetKeys[index];
    }

    public String[] getAssetKeys() {
        return COPY.copyOf(myAssetKeys);
    }

    public MatrixR064 getCovariances() {
        return myCovariances;
    }

    public Scalar<?> getRiskAversion() {
        return BigScalar.of(myRiskAversion);
    }

    public void setRiskAversion(final Comparable<?> factor) {

        final BigDecimal tmpFactor = TypeUtils.toBigDecimal(factor);

        if (tmpFactor.signum() == 0) {
            myRiskAversion = DEFAULT_RISK_AVERSION;
        } else if (tmpFactor.signum() < 0) {
            myRiskAversion = tmpFactor.negate();
        } else {
            myRiskAversion = tmpFactor;
        }
    }

    public int size() {
        return (int) Math.min(myCovariances.countRows(), myCovariances.countColumns());
    }

    public MatrixR064 toCorrelations() {
        return FinanceUtils.toCorrelations(myCovariances, false);
    }

    /**
     * Will calculate the risk aversion factor that is the best fit for an observed pair of market portfolio
     * weights and equilibrium/historical excess returns.
     */
    Scalar<?> calculateImpliedRiskAversion(final MatrixR064 assetWeights, final MatrixR064 assetReturns) {

        Scalar<?> retVal = myCovariances.multiply(assetWeights).solve(assetReturns).toScalar(0, 0);

        if (retVal.isSmall(PrimitiveMath.ONE)) {
            retVal = BigScalar.ONE;
        } else if (!retVal.isAbsolute()) {
            retVal = retVal.negate();
        }

        return retVal;
    }

    boolean isDefaultRiskAversion() {
        return myRiskAversion.compareTo(DEFAULT_RISK_AVERSION) == 0;
    }

}
