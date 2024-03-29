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

import static org.ojalgo.function.constant.BigMath.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.ojalgo.function.constant.BigMath;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.TypeUtils;

public final class PortfolioMixer {

    private static final String ACTIVE = "_Active";
    private static final String B = "B";
    private static final String C = "C";
    private static final String DIMENSION_MISMATCH = "The target and component portfolios must all have the same number of contained assets!";
    private static final String QUADRATIC_OBJECTIVE_PART = "Quadratic Objective Part";
    private static final String STRATEGY_COUNT = "Strategy Count";

    private final ArrayList<FinancePortfolio> myComponents;
    private final FinancePortfolio myTarget;
    private final HashMap<int[], LowerUpper> myAssetConstraints = new HashMap<>();
    private final HashMap<int[], LowerUpper> myComponentConstraints = new HashMap<>();

    public PortfolioMixer(final FinancePortfolio target, final Collection<? extends FinancePortfolio> components) {
        this(target, components.toArray(new FinancePortfolio[components.size()]));
    }

    public PortfolioMixer(final FinancePortfolio target, final FinancePortfolio... components) {

        super();

        myTarget = target;

        int tmpSize = myTarget.getWeights().size();

        myComponents = new ArrayList<>();
        for (FinancePortfolio tmpCompPortf : components) {
            if (tmpCompPortf.getWeights().size() != tmpSize) {
                throw new IllegalArgumentException(DIMENSION_MISMATCH);
            } else {
                myComponents.add(tmpCompPortf);
            }
        }
    }

    public LowerUpper addAssetConstraint(final Comparable<?> lowerLimit, final Comparable<?> upperLimit, final int... assetIndeces) {
        return myAssetConstraints.put(assetIndeces, new LowerUpper(lowerLimit, upperLimit));
    }

    public LowerUpper addComponentConstraint(final Comparable<?> lowerLimit, final Comparable<?> upperLimit, final int... assetIndeces) {
        return myComponentConstraints.put(assetIndeces, new LowerUpper(lowerLimit, upperLimit));
    }

    public List<BigDecimal> mix(final int aNumber) {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        int nbAssets = myTarget.getWeights().size();
        int nbComponents = myComponents.size();

        Variable[] wVars = new Variable[nbComponents];
        Variable[] aVars = new Variable[nbComponents];

        for (int c = 0; c < nbComponents; c++) {

            Variable tmpVariable = model.newVariable(C + c);

            BigDecimal tmpVal = ZERO;
            for (int i = 0; i < nbAssets; i++) {
                tmpVal = tmpVal.add(myTarget.getWeights().get(i).multiply(myComponents.get(c).getWeights().get(i)));
            }
            tmpVal = tmpVal.multiply(TWO).negate();

            tmpVariable.weight(tmpVal);

            tmpVariable.lower(ZERO);
            tmpVariable.upper(ONE);

            wVars[c] = tmpVariable;
            aVars[c] = model.newVariable(B + c).binary();
        }

        Expression tmpQuadObj = model.newExpression(QUADRATIC_OBJECTIVE_PART);
        tmpQuadObj.weight(ONE);
        for (int row = 0; row < nbComponents; row++) {
            for (int col = 0; col < nbComponents; col++) {

                BigDecimal tmpVal = ZERO;
                for (int i = 0; i < nbAssets; i++) {
                    tmpVal = tmpVal.add(myComponents.get(row).getWeights().get(i).multiply(myComponents.get(col).getWeights().get(i)));
                }
                tmpQuadObj.set(wVars[row], wVars[col], tmpVal);
                tmpQuadObj.set(aVars[row], aVars[col], tmpVal.multiply(THOUSANDTH));
            }

            Expression tmpActive = model.newExpression(wVars[row].getName() + ACTIVE);
            tmpActive.set(wVars[row], NEG);
            tmpActive.set(aVars[row], ONE);
            tmpActive.lower(ZERO);
            //            BasicLogger.logDebug(tmpActive.toString());
            //            BasicLogger.logDebug(tmpActive.getName(), tmpActive.getLinear().getFactors());
        }
        //        BasicLogger.logDebug(QUADRATIC_OBJECTIVE_PART, tmpQuadObj.getQuadratic().getFactors());

        Expression tmpHundredPercent = model.newExpression("100%");
        tmpHundredPercent.level(ONE);
        for (int c = 0; c < nbComponents; c++) {
            tmpHundredPercent.set(wVars[c], ONE);
        }
        //        BasicLogger.logDebug(tmpHundredPercent.toString());
        //        BasicLogger.logDebug(tmpHundredPercent.getName(), tmpHundredPercent.getLinear().getFactors());

        Expression tmpStrategyCount = model.newExpression(STRATEGY_COUNT);
        tmpStrategyCount.upper(TypeUtils.toBigDecimal(aNumber));
        for (int c = 0; c < nbComponents; c++) {
            tmpStrategyCount.set(aVars[c], ONE);
        }
        //        BasicLogger.logDebug(tmpStrategyCount.toString());
        //        BasicLogger.logDebug(tmpStrategyCount.getName(), tmpStrategyCount.getLinear().getFactors());

        for (Entry<int[], LowerUpper> tmpEntry : myAssetConstraints.entrySet()) {

            int tmpIndex = tmpEntry.getKey()[0]; // For now I assume there is only 1 index
            BigDecimal tmpLower = tmpEntry.getValue().lower;
            BigDecimal tmpUpper = tmpEntry.getValue().upper;

            Expression tmpExpr = model.newExpression("AC" + Arrays.toString(tmpEntry.getKey()));

            for (int c = 0; c < nbComponents; c++) {
                tmpExpr.set(c, myComponents.get(c).getWeights().get(tmpIndex));
            }
            if (tmpLower != null) {
                tmpExpr.lower(tmpLower);
            }
            if (tmpUpper != null) {
                tmpExpr.upper(tmpUpper);
            }
        }

        for (Entry<int[], LowerUpper> tmpEntry : myComponentConstraints.entrySet()) {

            int tmpIndex = tmpEntry.getKey()[0]; // For now I assume there is only 1 index
            BigDecimal tmpLower = tmpEntry.getValue().lower;
            BigDecimal tmpUpper = tmpEntry.getValue().upper;

            Expression tmpExpr = model.newExpression("CC" + Arrays.toString(tmpEntry.getKey()));

            tmpExpr.set(tmpIndex, BigMath.ONE);

            for (int c = 0; c < nbComponents; c++) {
            }
            if (tmpLower != null) {
                tmpExpr.lower(tmpLower);
            }
            if (tmpUpper != null) {
                tmpExpr.upper(tmpUpper);
            }
        }

        model.minimise();

        ArrayList<BigDecimal> retVal = new ArrayList<>(nbComponents);
        for (int v = 0; v < nbComponents; v++) {
            retVal.add(wVars[v].getValue());
        }
        return retVal;
    }

}
