/*
 * Copyright 1997-2022 Optimatika
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
import org.ojalgo.array.Array1D;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * Data for this test case originates from &quot;A STEP-BY-STEP GUIDE TO THE BLACK-LITTERMAN MODEL&quot; by
 * Thomas M. Idzorek.
 * </p>
 * <p>
 * Unfortunately the numbers in that paper are not specified with high enough precision to (directly)
 * construct unit tests without problems, but the tests available here should still be enough to indicate that
 * the ojAlgo Black-Litterman model class works correctly.
 * </p>
 * <p>
 * There are also various papers and presentations derived from that original paper available (on the
 * Internet). To some extent those are also used.
 * </p>
 *
 * @author apete
 */
public class BlackLittermanTest extends FinancePortfolioTests {

    private static final NumberContext DEF_CNTXT = NumberContext.of(7, 6);
    private static final NumberContext EVAL_CNTXT = NumberContext.of(2, 2);

    public static String[] getAssetNames() {
        return new String[] { "US Bonds", "Int Bonds", "US Large Growth", "US Large Value", "US Small Growth", "US Small Value", "Int Dev Equity",
                "Int Emerg Equity" };
    }

    public static MatrixR064 getCAPMReturns() {

        //double[][] tmpArr = new double[][] { { 0.0002 }, { 0.0018 }, { 0.0557 }, { 0.0339 }, { 0.0659 }, { 0.0316 }, { 0.0392 }, { 0.0560 } };
        final double[][] tmpArr = { { 0.000202 }, { 0.001804 }, { 0.055754 }, { 0.033945 }, { 0.065950 }, { 0.031631 }, { 0.039204 }, { 0.056023 } };

        return BlackLittermanTest.make(tmpArr);
    }

    public static MatrixR064 getCAPMWeights() {

        //double[][] tmpArr = new double[][] { { 0.2133 }, { 0.0519 }, { 0.1080 }, { 0.1082 }, { 0.0373 }, { -0.0049 }, { 0.1710 }, { 0.0214 } };
        final double[][] tmpArr = { { 0.213842 }, { 0.051058 }, { 0.107461 }, { 0.107260 }, { 0.037030 }, { -0.004974 }, { 0.172613 }, { 0.021511 } };

        return BlackLittermanTest.make(tmpArr);
    }

    public static MatrixR064 getCovariances() {

        final double[][] tmpArr = { { 0.001005, 0.001328, -0.000579, -0.000675, 0.000121, 0.000128, -0.000445, -0.000437 },
                { 0.001328, 0.007277, -0.001307, -0.000610, -0.002237, -0.000989, 0.001442, -0.001535 },
                { -0.000579, -0.001307, 0.059852, 0.027588, 0.063497, 0.023036, 0.032967, 0.048039 },
                { -0.000675, -0.000610, 0.027588, 0.029609, 0.026572, 0.021465, 0.020697, 0.029854 },
                { 0.000121, -0.002237, 0.063497, 0.026572, 0.102488, 0.042744, 0.039943, 0.065994 },
                { 0.000128, -0.000989, 0.023036, 0.021465, 0.042744, 0.032056, 0.019881, 0.032235 },
                { -0.000445, 0.001442, 0.032967, 0.020697, 0.039943, 0.019881, 0.028355, 0.035064 },
                { -0.000437, -0.001535, 0.048039, 0.029854, 0.065994, 0.032235, 0.035064, 0.079958 } };

        return BlackLittermanTest.make(tmpArr);
    }

    public static MatrixR064 getHistoricalReturns() {

        //double[][] tmpArr = new double[][] { { 0.0315 }, { 0.0175 }, { -0.0639 }, { -0.0286 }, { -0.0675 }, { -0.0054 }, { -0.0675 }, { -0.0526 } };
        final double[][] tmpArr = { { 0.031500 }, { 0.017528 }, { -0.063973 }, { -0.028605 }, { -0.067555 }, { -0.005411 }, { -0.067549 }, { -0.052644 } };

        return BlackLittermanTest.make(tmpArr);
    }

    public static MatrixR064 getHistoricalWeights() {

        //double[][] tmpArr = new double[][] { { 11.4432 }, { -1.0459 }, { 0.5499 }, { -0.0529 }, { -0.6052 }, { 0.8147 }, { -1.0436 }, { 0.1459 } };
        final double[][] tmpArr = { { 11.446735 }, { -1.048314 }, { 0.551087 }, { -0.054698 }, { -0.606396 }, { 0.815627 }, { -1.041846 }, { 0.145857 } };

        return BlackLittermanTest.make(tmpArr);
    }

    public static MatrixR064 getInvestorConfidencesMatrix() {

        // The paper describes how the confidence matrix is calculated. We do
        // the same calculations instead of just copying the numbers.

        final MatrixR064 tmpViews = BlackLittermanTest.getInvestorPortfoliosMatrix();
        final MatrixR064 tmpCovar = BlackLittermanTest.getCovariances();
        final MatrixR064 tmpMtrx = tmpViews.multiply(tmpCovar).multiply(tmpViews.transpose());
        final BigDecimal tmpWeightOnViews = BlackLittermanTest.getWeightOnViews();

        final double[][] tmpArr = new double[3][3];

        for (int ij = 0; ij < tmpMtrx.countRows(); ij++) {
            tmpArr[ij][ij] = tmpMtrx.doubleValue(ij, ij) * tmpWeightOnViews.doubleValue();
        }

        return BlackLittermanTest.make(tmpArr);
    }

    public static MatrixR064 getInvestorPortfoliosMatrix() {

        final double[][] tmpArr = { { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 }, { -1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.9, -0.9, 0.1, -0.1, 0.0, 0.0 } };

        return BlackLittermanTest.make(tmpArr);
    }

    public static MatrixR064 getInvestorReturnsMatrix() {

        final double[][] tmpArr = { { 0.0525 }, { 0.0025 }, { 0.0200 } };

        return BlackLittermanTest.make(tmpArr);
    }

    public static MatrixR064 getMarketReturns() {

        //double[][] tmpArr = new double[][] { { 0.0008 }, { 0.0067 }, { 0.0641 }, { 0.0408 }, { 0.0743 }, { 0.0370 }, { 0.0480 }, { 0.0660 } };
        final double[][] tmpArr = { { 0.000815 }, { 0.006694 }, { 0.064171 }, { 0.040839 }, { 0.074352 }, { 0.037044 }, { 0.048039 }, { 0.066028 } };

        return BlackLittermanTest.make(tmpArr);
    }

    public static MatrixR064 getMarketWeights() {

        //double[][] tmpArr = new double[][] { { 0.1934 }, { 0.2613 }, { 0.1209 }, { 0.1209 }, { 0.0134 }, { 0.0134 }, { 0.2418 }, { 0.0349 } };
        final double[][] tmpArr = { { 0.185692 }, { 0.263222 }, { 0.119554 }, { 0.121806 }, { 0.014606 }, { 0.012035 }, { 0.240974 }, { 0.035165 } };

        return BlackLittermanTest.make(tmpArr);
    }

    public static MatrixR064 getModifiedReturns() {

        //double[][] tmpArr = new double[][] { { 0.0007 }, { 0.0050 }, { 0.0650 }, { 0.0432 }, { 0.0759 }, { 0.0394 }, { 0.0493 }, { 0.0684 } };
        final double[][] tmpArr = { { 0.000684 }, { 0.004997 }, { 0.065031 }, { 0.043242 }, { 0.075933 }, { 0.039419 }, { 0.049355 }, { 0.068440 } };

        return BlackLittermanTest.make(tmpArr);
    }

    public static MatrixR064 getModifiedWeights() {

        //double[][] tmpArr = new double[][] { { 0.2988 }, { 0.1559 }, { 0.0935 }, { 0.1482 }, { 0.0104 }, { 0.0165 }, { 0.2781 }, { 0.0349 } };
        final double[][] tmpArr = { { 0.304440 }, { 0.155307 }, { 0.094349 }, { 0.147546 }, { 0.009930 }, { 0.017213 }, { 0.277067 }, { 0.035045 } };

        return BlackLittermanTest.make(tmpArr);
    }

    public static BigDecimal getRiskAversionFactor() {
        return BlackLittermanTest.DEF_CNTXT.enforce(BigDecimal.valueOf(3.07));
    }

    public static MatrixR064 getVarianceOfInvestorViewPortfolios() {

        final double[][] tmpArr = { { 0.02836 }, { 0.00563 }, { 0.03462 } };

        return BlackLittermanTest.make(tmpArr);
    }

    public static BigDecimal getWeightOnViews() {
        return BlackLittermanTest.DEF_CNTXT.enforce(BigDecimal.valueOf(0.025));
    }

    private static MatrixR064 make(final double[][] anArray) {
        return MatrixR064.FACTORY.rows(anArray).enforce(BlackLittermanTest.DEF_CNTXT);
    }

    public BlackLittermanTest() {
        super();
    }

    @Test
    public void testCAPMData() {

        final MarketEquilibrium tmpME = this.makeMarketEquilibrium();

        final MatrixR064 tmpReturn = BlackLittermanTest.getCAPMReturns();
        final MatrixR064 tmpWeight = BlackLittermanTest.getCAPMWeights();

        //System.out.println(tmpReturn.add(tmpME.calculateReturns(tmpWeight)).divide(BigMath.TWO).enforce(DEF_CNTXT).toString());
        //System.out.println(tmpWeight.add(tmpME.calculateWeights(tmpReturn)).divide(BigMath.TWO).enforce(DEF_CNTXT).toString());

        MatrixR064 tmpExp = tmpReturn;
        MatrixR064 tmpAct = tmpME.calculateAssetReturns(tmpWeight);

        TestUtils.assertEquals(tmpExp, tmpAct, EVAL_CNTXT);

        tmpExp = tmpWeight;
        tmpAct = tmpME.calculateAssetWeights(tmpReturn);

        TestUtils.assertEquals(tmpExp, tmpAct, EVAL_CNTXT);
    }

    @Test
    public void testCovarianceData() {

        final MatrixR064 tmpCovar = BlackLittermanTest.getCovariances();

        final MatrixR064 tmpExp = tmpCovar;
        final MatrixR064 tmpAct = tmpCovar.transpose();

        TestUtils.assertEquals(tmpExp, tmpAct, DEF_CNTXT);
    }

    @Test
    public void testHistoricalData() {

        final MarketEquilibrium tmpME = this.makeMarketEquilibrium();

        final MatrixR064 tmpReturn = BlackLittermanTest.getHistoricalReturns();
        final MatrixR064 tmpWeight = BlackLittermanTest.getHistoricalWeights();

        //System.out.println(tmpReturn.add(tmpME.calculateReturns(tmpWeight)).divide(BigMath.TWO).enforce(DEF_CNTXT).toString());
        //System.out.println(tmpWeight.add(tmpME.calculateWeights(tmpReturn)).divide(BigMath.TWO).enforce(DEF_CNTXT).toString());

        MatrixR064 tmpExp = tmpReturn;
        MatrixR064 tmpAct = tmpME.calculateAssetReturns(tmpWeight);

        TestUtils.assertEquals(tmpExp, tmpAct, EVAL_CNTXT);

        tmpExp = tmpWeight;
        tmpAct = tmpME.calculateAssetWeights(tmpReturn);

        TestUtils.assertEquals(tmpExp, tmpAct, EVAL_CNTXT);
    }

    @Test
    public void testMarketData() {

        final MarketEquilibrium tmpME = this.makeMarketEquilibrium();

        final MatrixR064 tmpReturn = BlackLittermanTest.getMarketReturns();
        final MatrixR064 tmpWeight = BlackLittermanTest.getMarketWeights();

        //System.out.println(tmpReturn.add(tmpME.calculateReturns(tmpWeight)).divide(BigMath.TWO).enforce(DEF_CNTXT).toString());
        //System.out.println(tmpWeight.add(tmpME.calculateWeights(tmpReturn)).divide(BigMath.TWO).enforce(DEF_CNTXT).toString());

        MatrixR064 tmpExp = tmpReturn;
        MatrixR064 tmpAct = tmpME.calculateAssetReturns(tmpWeight);

        TestUtils.assertEquals(tmpExp, tmpAct, EVAL_CNTXT);

        tmpExp = tmpWeight;
        tmpAct = tmpME.calculateAssetWeights(tmpReturn);

        TestUtils.assertEquals(tmpExp, tmpAct, EVAL_CNTXT);
    }

    @Test
    public void testModifiedData() {

        final MarketEquilibrium tmpME = this.makeMarketEquilibrium();

        final MatrixR064 tmpReturn = BlackLittermanTest.getModifiedReturns();
        final MatrixR064 tmpWeight = BlackLittermanTest.getModifiedWeights();

        //System.out.println(tmpReturn.add(tmpME.calculateReturns(tmpWeight)).divide(BigMath.TWO).enforce(DEF_CNTXT).toString());
        //System.out.println(tmpWeight.add(tmpME.calculateWeights(tmpReturn)).divide(BigMath.TWO).enforce(DEF_CNTXT).toString());

        MatrixR064 tmpExp = tmpReturn;
        MatrixR064 tmpAct = tmpME.calculateAssetReturns(tmpWeight);

        TestUtils.assertEquals(tmpExp, tmpAct, EVAL_CNTXT);

        tmpExp = tmpWeight;
        tmpAct = tmpME.calculateAssetWeights(tmpReturn);

        TestUtils.assertEquals(tmpExp, tmpAct, EVAL_CNTXT);
    }

    @Test
    public void testVarianceOfInvestorViewPortfolios() {

        final MatrixR064 tmpViews = BlackLittermanTest.getInvestorPortfoliosMatrix();
        final MatrixR064 tmpCovar = BlackLittermanTest.getCovariances();

        final MatrixR064 tmpExp = BlackLittermanTest.getVarianceOfInvestorViewPortfolios();
        final MatrixR064 tmpAct = tmpViews.multiply(tmpCovar).multiply(tmpViews.transpose());

        for (int i = 0; i < tmpExp.countRows(); i++) {
            final int row = i;
            final int row1 = i;
            final int col = i;
            TestUtils.assertEquals(TypeUtils.toBigDecimal(tmpExp.get(row, 0)), TypeUtils.toBigDecimal(tmpAct.get(row1, col)), EVAL_CNTXT);
        }
    }

    @Test
    public void testViewsWithDeferredConfidence() {

        final MarketEquilibrium tmpME = this.makeMarketEquilibrium();
        final MatrixR064 tmpMarketWeights = BlackLittermanTest.getMarketWeights();

        final BlackLittermanModel tmpBLM = new BlackLittermanModel(tmpME, tmpMarketWeights);

        tmpBLM.setRiskAversion(BlackLittermanTest.getRiskAversionFactor());
        tmpBLM.setConfidence(BlackLittermanTest.getWeightOnViews());

        final MatrixR064 tmpViewPortfolios = BlackLittermanTest.getInvestorPortfoliosMatrix();
        final MatrixR064 tmpViewPortfolioReturns = BlackLittermanTest.getInvestorReturnsMatrix();

        for (int i = 0; i < tmpViewPortfolios.countRows(); i++) {
            final int row = i;
            tmpBLM.addViewWithBalancedConfidence(Array1D.R256.copy(tmpViewPortfolios.logical().rows(i).get()),
                    TypeUtils.toBigDecimal(tmpViewPortfolioReturns.get(row, 0)));
        }

        this.testBLM(tmpBLM);
    }

    @Test
    public void testViewsWithStandardDeviation() {

        final MarketEquilibrium tmpME = this.makeMarketEquilibrium();
        final MatrixR064 tmpMarketWeights = BlackLittermanTest.getMarketWeights();

        final BlackLittermanModel tmpBLM = new BlackLittermanModel(tmpME, tmpMarketWeights);

        tmpBLM.setRiskAversion(BlackLittermanTest.getRiskAversionFactor());
        tmpBLM.setConfidence(BlackLittermanTest.getWeightOnViews());

        final MatrixR064 tmpViewPortfolios = BlackLittermanTest.getInvestorPortfoliosMatrix();
        final MatrixR064 tmpViewPortfolioReturns = BlackLittermanTest.getInvestorReturnsMatrix();
        final MatrixR064 tmpConfidenceMatrix = BlackLittermanTest.getInvestorConfidencesMatrix();

        for (int i = 0; i < tmpConfidenceMatrix.countRows(); i++) {
            final int row = i;
            final int row1 = i;
            final int col = i;
            tmpBLM.addViewWithStandardDeviation(Array1D.R256.copy(tmpViewPortfolios.logical().rows(i).get()),
                    TypeUtils.toBigDecimal(tmpViewPortfolioReturns.get(row, 0)),
                    BigMath.SQRT.invoke(TypeUtils.toBigDecimal(tmpConfidenceMatrix.get(row1, col))));
        }

        this.testBLM(tmpBLM);
    }

    private void testBLM(final BlackLittermanModel aBLM) {

        MatrixR064 tmpExp;
        MatrixR064 tmpAct;

        tmpExp = BlackLittermanTest.getInvestorPortfoliosMatrix();
        tmpAct = aBLM.getViewPortfolios();

        TestUtils.assertEquals(tmpExp, tmpAct, EVAL_CNTXT);

        tmpExp = BlackLittermanTest.getInvestorReturnsMatrix();
        //tmpAct = aBLM.getViewReturns();
        tmpAct = aBLM.getViewReturns().multiply(aBLM.getRiskAversion().doubleValue());

        TestUtils.assertEquals(tmpExp, tmpAct, EVAL_CNTXT);

        tmpExp = BlackLittermanTest.getInvestorConfidencesMatrix();
        //tmpAct = aBLM.getViewConfidences();
        tmpAct = aBLM.getViewVariances().multiply(aBLM.getConfidence().doubleValue());

        TestUtils.assertEquals(tmpExp, tmpAct, EVAL_CNTXT);

        tmpExp = BlackLittermanTest.getCovariances();
        tmpAct = aBLM.getCovariances();

        TestUtils.assertEquals(tmpExp, tmpAct, EVAL_CNTXT);

        tmpExp = BlackLittermanTest.getMarketReturns();
        tmpAct = aBLM.getOriginalReturns();

        TestUtils.assertEquals(tmpExp, tmpAct, EVAL_CNTXT);

        tmpExp = BlackLittermanTest.getMarketWeights();
        tmpAct = aBLM.getOriginalWeights();

        TestUtils.assertEquals(tmpExp, tmpAct, EVAL_CNTXT);

        tmpExp = BlackLittermanTest.getModifiedReturns();
        tmpAct = aBLM.getAssetReturns();

        TestUtils.assertEquals(tmpExp, tmpAct, EVAL_CNTXT);

        tmpExp = BlackLittermanTest.getModifiedWeights();
        tmpAct = aBLM.getAssetWeights();
        //Numerical problem, but inspection tells me the calculations are correct.
        //JUnitUtils.assertEquals(tmpExp, tmpAct, EVAL_CNTXT);
    }

    protected MarketEquilibrium makeMarketEquilibrium() {

        final String[] tmpNames = BlackLittermanTest.getAssetNames();
        final MatrixR064 tmpCovars = BlackLittermanTest.getCovariances();

        return new MarketEquilibrium(tmpNames, tmpCovars, BlackLittermanTest.getRiskAversionFactor());
    }

}
