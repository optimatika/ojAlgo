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
 * MarketEquilibrium translates between the market portfolio weights and the equilibrium excess returns. The
 * only things needed to do those translations are the covariance matrix and the (market) risk aversion factor
 * - that's what you need to supply when you instantiate this class.
 * <p>
 * This class performs unconstrained optimisation. For each set of asset returns there is an optimal set of
 * weights. It also performs reverse (unconstrained) optimisation producing the "optimal" expected returns
 * given a set of weights.
 * <p>
 * The name MarketEquilibrium is actually a bit misleading. By altering the risk aversion factor this class
 * can/will describe the weights/returns equilibrium for any investor.
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

    /**
     * With the default risk aversion (1.0) and generated asset keys.
     */
    public static MarketEquilibrium of(final Access2D<?> covariances) {
        return MarketEquilibrium.of(covariances, DEFAULT_RISK_AVERSION);
    }

    /**
     * With generated asset keys.
     *
     * @throws IllegalArgumentException if the risk aversion factor is not positive
     */
    public static MarketEquilibrium of(final Access2D<?> covariances, final Comparable<?> riskAversion) {
        return new MarketEquilibrium(MarketEquilibrium.makeSymbols((int) covariances.countRows()), MarketEquilibrium.toCovariances(covariances),
                TypeUtils.toBigDecimal(riskAversion));
    }

    /**
     * @param assetKeys    Names/keys of the assets (copied)
     * @param covariances  The covariance matrix
     * @param riskAversion The (market) risk aversion factor - 1.0 is the default
     * @throws IllegalArgumentException if the risk aversion factor is not positive
     */
    public static MarketEquilibrium of(final String[] assetKeys, final Access2D<?> covariances, final Comparable<?> riskAversion) {
        return new MarketEquilibrium(COPY.copyOf(assetKeys), MarketEquilibrium.toCovariances(covariances), TypeUtils.toBigDecimal(riskAversion));
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

    private static MatrixR064 toCovariances(final Access2D<?> covariances) {
        if (covariances instanceof MatrixR064) {
            return (MatrixR064) covariances;
        } else {
            return MatrixR064.FACTORY.copy(covariances);
        }
    }

    private static BigDecimal toRiskAversion(final Comparable<?> factor) {

        BigDecimal retVal = TypeUtils.toBigDecimal(factor);

        if (retVal.signum() <= 0) {
            throw new IllegalArgumentException("The risk aversion factor must be positive: " + retVal);
        }

        return retVal;
    }

    private final String[] myAssetKeys;
    private final MatrixR064 myCovariances;
    private BigDecimal myRiskAversion;

    /**
     * @deprecated v57 Use {@link #of(Access2D)} instead.
     */
    @Deprecated
    public MarketEquilibrium(final Access2D<?> covarianceMatrix) {
        this(MarketEquilibrium.makeSymbols((int) covarianceMatrix.countRows()), MarketEquilibrium.toCovariances(covarianceMatrix), DEFAULT_RISK_AVERSION);
    }

    /**
     * @deprecated v57 Use {@link #of(Access2D, Comparable)} instead.
     */
    @Deprecated
    public MarketEquilibrium(final Access2D<?> covarianceMatrix, final Comparable<?> riskAversionFactor) {
        this(MarketEquilibrium.makeSymbols((int) covarianceMatrix.countRows()), MarketEquilibrium.toCovariances(covarianceMatrix),
                TypeUtils.toBigDecimal(riskAversionFactor));
    }

    /**
     * @deprecated v57 Use {@link #of(String[], Access2D, Comparable)}, with risk aversion 1.0, instead.
     */
    @Deprecated
    public MarketEquilibrium(final String[] assetNamesOrKeys, final Access2D<?> covarianceMatrix) {
        this(COPY.copyOf(assetNamesOrKeys), MarketEquilibrium.toCovariances(covarianceMatrix), DEFAULT_RISK_AVERSION);
    }

    /**
     * @deprecated v57 Use {@link #of(String[], Access2D, Comparable)} instead.
     */
    @Deprecated
    public MarketEquilibrium(final String[] assetNamesOrKeys, final Access2D<?> covarianceMatrix, final Comparable<?> riskAversionFactor) {
        this(COPY.copyOf(assetNamesOrKeys), MarketEquilibrium.toCovariances(covarianceMatrix), TypeUtils.toBigDecimal(riskAversionFactor));
    }

    MarketEquilibrium(final String[] assetKeys, final MatrixR064 covariances, final BigDecimal riskAversion) {

        super();

        myAssetKeys = assetKeys;
        myCovariances = covariances;
        myRiskAversion = MarketEquilibrium.toRiskAversion(riskAversion);
    }

    /**
     * If the input vector of asset weights are the weights of the market portfolio, then the output is the
     * equilibrium excess returns.
     */
    public MatrixR064 calculateAssetReturns(final MatrixR064 assetWeights) {
        return myCovariances.multiply(assetWeights).multiply(myRiskAversion.doubleValue());
    }

    /**
     * If the input vector of returns are the equilibrium excess returns then the output is the market
     * portfolio weights. This is unconstrained optimisation - there are no constraints on the resulting asset
     * weights.
     */
    public MatrixR064 calculateAssetWeights(final MatrixR064 assetReturns) {
        return myCovariances.solve(assetReturns).divide(myRiskAversion.doubleValue());
    }

    /**
     * Calculates the portfolio variance using the input asset weights.
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
     *
     * @return true if calibrated, false if the implied risk aversion is not positive - the weights and
     *         returns don't imply a positive risk premium (e.g. historical returns from a falling market).
     *         The risk aversion is then left unchanged.
     */
    public boolean calibrate(final MatrixR064 assetWeights, final MatrixR064 assetReturns) {

        double implied = this.calculateImpliedRiskAversion(assetWeights, assetReturns);

        if (implied > PrimitiveMath.ZERO) {
            this.setRiskAversion(implied);
            return true;
        } else {
            return false;
        }
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
        return new MarketEquilibrium(myAssetKeys, myCovariances, myRiskAversion);
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

    /**
     * @throws IllegalArgumentException if the factor is not positive
     */
    public void setRiskAversion(final Comparable<?> factor) {
        myRiskAversion = MarketEquilibrium.toRiskAversion(factor);
    }

    public int size() {
        return (int) Math.min(myCovariances.countRows(), myCovariances.countColumns());
    }

    public MatrixR064 toCorrelations() {
        return FinanceUtils.toCorrelations(myCovariances, false);
    }

    /**
     * Will calculate the risk aversion factor that is the (least squares) best fit for an observed pair of
     * market portfolio weights and equilibrium/historical excess returns: [r] = RAF [C][w]. The result is not
     * necessarily positive (or even a number).
     */
    double calculateImpliedRiskAversion(final MatrixR064 assetWeights, final MatrixR064 assetReturns) {
        MatrixR064 covarWeights = myCovariances.multiply(assetWeights);
        return covarWeights.dot(assetReturns) / covarWeights.dot(covarWeights);
    }

}
