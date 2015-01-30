/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se) Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions: The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ojalgo.optimisation.integer;

import static org.ojalgo.constant.BigMath.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.ojalgo.TestUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.finance.portfolio.FinancePortfolio;
import org.ojalgo.finance.portfolio.PortfolioMixer;
import org.ojalgo.finance.portfolio.SimplePortfolio;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.random.Uniform;

public class StrategyMixer extends OptimisationIntegerTests {

    public StrategyMixer() {
        super();
    }

    public StrategyMixer(final String someName) {
        super(someName);
    }

    public void testStratCombPortfolioMixer() {

        final FinancePortfolio tmpTarget = new SimplePortfolio(THIRD, THIRD, THIRD).normalise();

        final FinancePortfolio tmpStrat1 = new SimplePortfolio(HALF, HALF, ZERO);
        final FinancePortfolio tmpStrat2 = new SimplePortfolio(HALF, ZERO, HALF);
        final FinancePortfolio tmpStrat3 = new SimplePortfolio(ZERO, HALF, HALF);

        final PortfolioMixer tmpMixer = new PortfolioMixer(tmpTarget, tmpStrat1, tmpStrat2, tmpStrat3);

        final int tmpExpectedNumberOfStrategies = 2;
        final List<BigDecimal> tmpStrategyWeights = tmpMixer.mix(tmpExpectedNumberOfStrategies);

        int tmpUseCount = 0;
        double tmpTotalWeight = 0D;

        for (final BigDecimal tmpWeight : tmpStrategyWeights) {
            if (tmpWeight.signum() != 0) {
                tmpUseCount++;
                tmpTotalWeight += tmpWeight.doubleValue();
            }
        }

        TestUtils.assertEquals(tmpExpectedNumberOfStrategies, tmpUseCount);
        TestUtils.assertEquals(PrimitiveMath.ONE, tmpTotalWeight, PrimitiveMath.IS_ZERO);
    }

    public void testStratCombPortfolioMixerRandom() {

        final FinancePortfolio tmpTarget = new SimplePortfolio(QUARTER, QUARTER, QUARTER, QUARTER).normalise();

        final Uniform tmpGen = new Uniform();

        final FinancePortfolio tmpStrat1 = new SimplePortfolio(tmpGen.doubleValue(), tmpGen.doubleValue(), tmpGen.doubleValue(), tmpGen.doubleValue())
                .normalise();
        final FinancePortfolio tmpStrat2 = new SimplePortfolio(tmpGen.doubleValue(), tmpGen.doubleValue(), tmpGen.doubleValue(), tmpGen.doubleValue())
                .normalise();
        final FinancePortfolio tmpStrat3 = new SimplePortfolio(tmpGen.doubleValue(), tmpGen.doubleValue(), tmpGen.doubleValue(), tmpGen.doubleValue())
                .normalise();

        final PortfolioMixer tmpMixer = new PortfolioMixer(tmpTarget, tmpStrat1, tmpStrat2, tmpStrat3);

        final int tmpExpectedNumberOfStrategies = 2;
        final List<BigDecimal> tmpStrategyWeights = tmpMixer.mix(tmpExpectedNumberOfStrategies);

        int tmpUseCount = 0;
        double tmpTotalWeight = 0D;

        for (final BigDecimal tmpWeight : tmpStrategyWeights) {
            if (tmpWeight.signum() != 0) {
                tmpUseCount++;
                tmpTotalWeight += tmpWeight.doubleValue();
            }
        }

        TestUtils.assertEquals(tmpExpectedNumberOfStrategies, tmpUseCount);
        TestUtils.assertEquals(PrimitiveMath.ONE, tmpTotalWeight, PrimitiveMath.IS_ZERO / PrimitiveMath.HUNDRED);
    }

    public void testStratCombQuadraticExpressionModel() {

        final BigDecimal[] tmpTarget = new BigDecimal[] { THIRD, THIRD, THIRD };

        final BigDecimal[] tmpStrat1 = new BigDecimal[] { HALF, HALF, ZERO };
        final BigDecimal[] tmpStrat2 = new BigDecimal[] { HALF, ZERO, HALF };
        final BigDecimal[] tmpStrat3 = new BigDecimal[] { ZERO, HALF, HALF };

        final BigDecimal[][] tmpStrats = new BigDecimal[][] { tmpStrat1, tmpStrat2, tmpStrat3 };

        final Variable[] tmpVars = new Variable[] { new Variable("S1"), new Variable("S2"), new Variable("S3"), Variable.makeBinary("B1"),
                Variable.makeBinary("B2"), Variable.makeBinary("B3") };

        for (int s = 0; s < 3; s++) {

            BigDecimal tmpVal = ZERO;

            for (int i = 0; i < 3; i++) {
                tmpVal = tmpVal.add(tmpTarget[i].multiply(tmpStrats[s][i]));
            }

            tmpVal = tmpVal.multiply(TWO).negate();

            tmpVars[s].weight(tmpVal);
            tmpVars[s].lower(ZERO);
            tmpVars[s].upper(ONE);

        }

        final ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVars);

        //        tmpModel.options.debug(GenericSolver.class);
        //        tmpModel.options.validate = false;

        final Expression tmpQuadObj = tmpModel.addExpression("Quadratic Objective Part");
        tmpQuadObj.weight(ONE);
        for (int row = 0; row < 3; row++) {

            for (int col = 0; col < 3; col++) {

                BigDecimal tmpVal = ZERO;

                for (int i = 0; i < 3; i++) {
                    tmpVal = tmpVal.add(tmpStrats[row][i].multiply(tmpStrats[col][i]));
                }

                tmpQuadObj.setQuadraticFactor(row, col, tmpVal);
            }

            final Expression tmpActive = tmpModel.addExpression(tmpVars[row].getName() + " Active");
            tmpActive.setLinearFactor(3 + row, ONE);
            tmpActive.setLinearFactor(row, NEG);
            tmpActive.lower(ZERO);
            if (OptimisationIntegerTests.DEBUG) {
                BasicLogger.debug(tmpActive.toString());
            }
        }

        final Expression tmpHundredPercent = tmpModel.addExpression("100%");
        tmpHundredPercent.level(ONE);
        tmpHundredPercent.setLinearFactor(0, ONE);
        tmpHundredPercent.setLinearFactor(1, ONE);
        tmpHundredPercent.setLinearFactor(2, ONE);
        if (OptimisationIntegerTests.DEBUG) {
            BasicLogger.debug(tmpHundredPercent.toString());
        }

        final Expression tmpStrategyCount = tmpModel.addExpression("Strategy Count");
        tmpStrategyCount.upper(TWO);
        tmpStrategyCount.setLinearFactor(3, ONE);
        tmpStrategyCount.setLinearFactor(4, ONE);
        tmpStrategyCount.setLinearFactor(5, ONE);
        if (OptimisationIntegerTests.DEBUG) {
            BasicLogger.debug(tmpStrategyCount.toString());
        }

        tmpModel.minimise();

        if (OptimisationIntegerTests.DEBUG) {
            BasicLogger.debug(tmpModel.toString());
            BasicLogger.debug(Arrays.toString(tmpVars));
        }

        int tmpUseCount = 0;
        double tmpTotalWeight = 0D;

        final Variable[] tmpSolution = new Variable[] { tmpVars[0], tmpVars[1], tmpVars[2] };
        for (final Variable tmpWeight : tmpSolution) {
            if (tmpWeight.getValue().signum() != 0) {
                tmpUseCount++;
                tmpTotalWeight += tmpWeight.getValue().doubleValue();
            }
        }

        TestUtils.assertEquals(TWO.intValue(), tmpUseCount);
        TestUtils.assertEquals(PrimitiveMath.ONE, tmpTotalWeight, PrimitiveMath.IS_ZERO);
    }

}
