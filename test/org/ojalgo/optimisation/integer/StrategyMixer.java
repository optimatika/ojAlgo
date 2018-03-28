/*
 * Copyright 1997-2018 Optimatika Permission is hereby granted, free of charge, to any person
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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Variable;

public class StrategyMixer {

    /**
     * This is test case using a reimplementation of the algorithm in {@link PortfolioMixer}.
     */
    @Test
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

        // tmpModel.options.debug(IntegerSolver.class);
        // tmpModel.options.validate = false;

        final Expression tmpQuadObj = tmpModel.addExpression("Quadratic Objective Part");
        tmpQuadObj.weight(ONE);
        for (int row = 0; row < 3; row++) {

            for (int col = 0; col < 3; col++) {

                BigDecimal tmpVal = ZERO;

                for (int i = 0; i < 3; i++) {
                    tmpVal = tmpVal.add(tmpStrats[row][i].multiply(tmpStrats[col][i]));
                }

                tmpQuadObj.set(row, col, tmpVal);
                tmpQuadObj.set(3 + row, 3 + col, tmpVal.multiply(THOUSANDTH));
            }

            final Expression tmpActive = tmpModel.addExpression(tmpVars[row].getName() + " Active");
            tmpActive.set(3 + row, ONE);
            tmpActive.set(row, NEG);
            tmpActive.lower(ZERO);
            if (OptimisationIntegerTests.DEBUG) {
                BasicLogger.debug(tmpActive.toString());
            }
        }

        final Expression tmpHundredPercent = tmpModel.addExpression("100%");
        tmpHundredPercent.level(ONE);
        tmpHundredPercent.set(0, ONE);
        tmpHundredPercent.set(1, ONE);
        tmpHundredPercent.set(2, ONE);
        if (OptimisationIntegerTests.DEBUG) {
            BasicLogger.debug(tmpHundredPercent.toString());
        }

        final Expression tmpStrategyCount = tmpModel.addExpression("Strategy Count");
        tmpStrategyCount.upper(TWO);
        tmpStrategyCount.set(3, ONE);
        tmpStrategyCount.set(4, ONE);
        tmpStrategyCount.set(5, ONE);
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
        TestUtils.assertEquals(PrimitiveMath.ONE, tmpTotalWeight, 1E-14 / PrimitiveMath.THREE);
    }

}
