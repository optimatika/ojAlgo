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

import static org.ojalgo.function.constant.BigMath.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.TypeUtils;

/**
 * Finds the mix, of at most a given number of component portfolios, with weights as close as possible (least
 * squares) to those of a target portfolio. Solved as a mixed integer quadratic program.
 */
public final class PortfolioMixer {

    private static final String ACTIVE = "_Active";
    private static final String B = "B";
    private static final String C = "C";
    private static final String DIMENSION_MISMATCH = "The target and component portfolios must all have the same number of contained assets!";
    private static final String QUADRATIC_OBJECTIVE_PART = "Quadratic Objective Part";
    private static final String STRATEGY_COUNT = "Strategy Count";

    /**
     * @param target     The target portfolio
     * @param components The component portfolios to mix - all with the same assets as the target
     */
    public static PortfolioMixer of(final FinancePortfolio target, final Collection<? extends FinancePortfolio> components) {
        return new PortfolioMixer(target, new ArrayList<>(components));
    }

    /**
     * @see #of(FinancePortfolio, Collection)
     */
    public static PortfolioMixer of(final FinancePortfolio target, final FinancePortfolio... components) {
        return new PortfolioMixer(target, Arrays.asList(components));
    }

    private final ArrayList<FinancePortfolio> myComponents;
    private final FinancePortfolio myTarget;
    private final Map<List<Integer>, LowerUpper> myAssetConstraints = new LinkedHashMap<>();
    private final Map<List<Integer>, LowerUpper> myComponentConstraints = new LinkedHashMap<>();

    /**
     * @deprecated v57 Use {@link #of(FinancePortfolio, Collection)} instead.
     */
    @Deprecated
    public PortfolioMixer(final FinancePortfolio target, final Collection<? extends FinancePortfolio> components) {
        this(target, new ArrayList<FinancePortfolio>(components));
    }

    /**
     * @deprecated v57 Use {@link #of(FinancePortfolio, FinancePortfolio...)} instead.
     */
    @Deprecated
    public PortfolioMixer(final FinancePortfolio target, final FinancePortfolio... components) {
        this(target, Arrays.asList(components));
    }

    PortfolioMixer(final FinancePortfolio target, final List<FinancePortfolio> components) {

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

    /**
     * Will add a constraint on the sum of the (mixed) portfolio weights of the specified assets. Either of
     * the limits may be null. A constraint on the same set of assets replaces any previous one.
     */
    public void addAssetConstraint(final Comparable<?> lowerLimit, final Comparable<?> upperLimit, final int... assetIndices) {
        myAssetConstraints.put(LowerUpper.key(assetIndices), new LowerUpper(lowerLimit, upperLimit));
    }

    /**
     * Will add a constraint on the sum of the shares of the specified components. Either of the limits may be
     * null. A constraint on the same set of components replaces any previous one.
     */
    public void addComponentConstraint(final Comparable<?> lowerLimit, final Comparable<?> upperLimit, final int... componentIndices) {
        myComponentConstraints.put(LowerUpper.key(componentIndices), new LowerUpper(lowerLimit, upperLimit));
    }

    /**
     * @param aNumber The maximum number of components to use
     * @return The component shares, or all zeros if no feasible mix could be found
     */
    public List<BigDecimal> mix(final int aNumber) {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        List<BigDecimal> target = myTarget.getWeights();

        int nbAssets = target.size();
        int nbComponents = myComponents.size();

        BigDecimal[][] componentWeights = new BigDecimal[nbComponents][];
        for (int c = 0; c < nbComponents; c++) {
            componentWeights[c] = myComponents.get(c).getWeights().toArray(new BigDecimal[nbAssets]);
        }

        Variable[] wVars = new Variable[nbComponents];
        Variable[] aVars = new Variable[nbComponents];

        for (int c = 0; c < nbComponents; c++) {

            BigDecimal tmpVal = ZERO;
            for (int i = 0; i < nbAssets; i++) {
                tmpVal = tmpVal.add(target.get(i).multiply(componentWeights[c][i]));
            }
            tmpVal = tmpVal.multiply(TWO).negate();

            wVars[c] = model.newVariable(C + c).weight(tmpVal).lower(ZERO).upper(ONE);
            aVars[c] = model.newVariable(B + c).binary();
        }

        Expression tmpQuadObj = model.newExpression(QUADRATIC_OBJECTIVE_PART);
        tmpQuadObj.weight(ONE);
        for (int row = 0; row < nbComponents; row++) {
            for (int col = 0; col < nbComponents; col++) {

                BigDecimal tmpVal = ZERO;
                for (int i = 0; i < nbAssets; i++) {
                    tmpVal = tmpVal.add(componentWeights[row][i].multiply(componentWeights[col][i]));
                }
                tmpQuadObj.set(wVars[row], wVars[col], tmpVal);
                tmpQuadObj.set(aVars[row], aVars[col], tmpVal.multiply(THOUSANDTH));
            }

            Expression tmpActive = model.newExpression(wVars[row].getName() + ACTIVE);
            tmpActive.set(wVars[row], NEG);
            tmpActive.set(aVars[row], ONE);
            tmpActive.lower(ZERO);
        }

        Expression tmpHundredPercent = model.newExpression("100%");
        tmpHundredPercent.level(ONE);
        for (int c = 0; c < nbComponents; c++) {
            tmpHundredPercent.set(wVars[c], ONE);
        }

        Expression tmpStrategyCount = model.newExpression(STRATEGY_COUNT);
        tmpStrategyCount.upper(TypeUtils.toBigDecimal(aNumber));
        for (int c = 0; c < nbComponents; c++) {
            tmpStrategyCount.set(aVars[c], ONE);
        }

        for (Entry<List<Integer>, LowerUpper> tmpEntry : myAssetConstraints.entrySet()) {

            List<Integer> tmpAssetIndices = tmpEntry.getKey();

            Expression tmpExpr = model.newExpression("AC" + tmpAssetIndices);

            for (int c = 0; c < nbComponents; c++) {
                BigDecimal tmpSum = ZERO;
                for (int i : tmpAssetIndices) {
                    tmpSum = tmpSum.add(componentWeights[c][i]);
                }
                tmpExpr.set(wVars[c], tmpSum);
            }

            tmpExpr.lower(tmpEntry.getValue().lower).upper(tmpEntry.getValue().upper);
        }

        for (Entry<List<Integer>, LowerUpper> tmpEntry : myComponentConstraints.entrySet()) {

            List<Integer> tmpComponentIndices = tmpEntry.getKey();

            Expression tmpExpr = model.newExpression("CC" + tmpComponentIndices);

            for (int c : tmpComponentIndices) {
                tmpExpr.set(wVars[c], ONE);
            }

            tmpExpr.lower(tmpEntry.getValue().lower).upper(tmpEntry.getValue().upper);
        }

        Optimisation.Result result = model.minimise();
        boolean feasible = result.getState().isFeasible();

        ArrayList<BigDecimal> retVal = new ArrayList<>(nbComponents);
        for (int c = 0; c < nbComponents; c++) {
            retVal.add(feasible ? result.get(wVars[c].getIndex().index) : ZERO);
        }
        return retVal;
    }

}
