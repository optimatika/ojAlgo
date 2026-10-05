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
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.type.context.NumberContext;

public class EfficientFrontierTest extends FinancePortfolioTests {

    private static final NumberContext ACCURACY = NumberContext.of(5, 4);

    private static final MatrixR064 COVARIANCES = MatrixR064.FACTORY.copy(RawStore.wrap(new double[][] { { 0.01, 0.0 }, { 0.0, 0.01 } }));
    private static final MatrixR064 RETURNS = MatrixR064.FACTORY.column(new double[] { 0.10, 0.0 });

    /**
     * Allowing shorting after the weights have been calculated used to re-solve the same optimisation model,
     * with presolve-derived (stale) upper bounds of 1.0 still in place.
     */
    @Test
    public void testShortingAllowedAfterCalculation() {

        EfficientFrontier portfolio = EfficientFrontier.of(COVARIANCES, RETURNS);

        TestUtils.assertEquals(MatrixR064.FACTORY.column(new double[] { 1.0, 0.0 }), portfolio.getAssetWeights(), ACCURACY);

        portfolio.setShortingAllowed(true);

        TestUtils.assertEquals(MatrixR064.FACTORY.column(new double[] { 5.5, -4.5 }), portfolio.getAssetWeights(), ACCURACY);
    }

    /**
     * Perfectly correlated assets with different expected returns, and shorting allowed, is an arbitrage -
     * the optimisation problem is unbounded. That used to be reported as OPTIMAL, with weights of +/-3.4E8.
     * Now the state is UNBOUNDED and, as with any optimisation failure, all weights are zero.
     */
    @Test
    public void testUnboundedWithSingularCovariances() {

        MatrixR064 singular = MatrixR064.FACTORY.copy(RawStore.wrap(new double[][] { { 0.01, 0.01 }, { 0.01, 0.01 } }));

        EfficientFrontier portfolio = EfficientFrontier.of(singular, RETURNS);
        portfolio.setShortingAllowed(true);

        TestUtils.assertEquals(MatrixR064.FACTORY.column(new double[] { 0.0, 0.0 }), portfolio.getAssetWeights(), ACCURACY);
        TestUtils.assertEquals(State.UNBOUNDED, portfolio.optimiser().getState());
    }

}
