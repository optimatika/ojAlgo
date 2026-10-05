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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.structure.Access2D;

/**
 * Represents a portfolio on the efficient frontier. You get different efficient portfolios by altering the
 * risk aversion.
 * <p>
 * Check {@code portfolio.optimiser().getState()} - if no usable solution was found, all weights are zero.
 *
 * @author apete
 */
public final class EfficientFrontier extends OptimisedPortfolio {

    private static final Map<List<Integer>, LowerUpper> CONSTRAINTS = Collections.emptyMap();

    /**
     * The covariances and (expected excess) returns from the context, and the default risk aversion (1.0).
     */
    public static EfficientFrontier of(final FinancePortfolio.Context portfolioContext) {
        return new EfficientFrontier(MarketEquilibrium.of(portfolioContext.getCovariances()), portfolioContext.getAssetReturns());
    }

    /**
     * @param marketEquilibrium     The covariances and risk aversion
     * @param expectedExcessReturns The expected excess returns
     */
    public static EfficientFrontier of(final MarketEquilibrium marketEquilibrium, final MatrixR064 expectedExcessReturns) {
        return new EfficientFrontier(marketEquilibrium, expectedExcessReturns);
    }

    /**
     * With the default risk aversion (1.0).
     */
    public static EfficientFrontier of(final Access2D<?> covariances, final MatrixR064 expectedExcessReturns) {
        return new EfficientFrontier(MarketEquilibrium.of(covariances), expectedExcessReturns);
    }

    /**
     * @deprecated v57 Use {@link #of(FinancePortfolio.Context)} instead.
     */
    @Deprecated
    public EfficientFrontier(final FinancePortfolio.Context portfolioContext) {
        this(MarketEquilibrium.of(portfolioContext.getCovariances()), portfolioContext.getAssetReturns());
    }

    /**
     * @deprecated v57 Use {@link #of(MarketEquilibrium, MatrixR064)} instead. This constructor will become
     *             package-private.
     */
    @Deprecated
    public EfficientFrontier(final MarketEquilibrium marketEquilibrium, final MatrixR064 expectedExcessReturns) {
        super(marketEquilibrium, expectedExcessReturns);
    }

    /**
     * @deprecated v57 Use {@link #of(Access2D, MatrixR064)} instead.
     */
    @Deprecated
    public EfficientFrontier(final MatrixR064 covarianceMatrix, final MatrixR064 expectedExcessReturns) {
        this(MarketEquilibrium.of(covarianceMatrix), expectedExcessReturns);
    }

    @Override
    protected MatrixR064 calculateAssetWeights() {

        return this.handle(this.solve(CONSTRAINTS, this.getRiskAversion().doubleValue()));
    }

}
