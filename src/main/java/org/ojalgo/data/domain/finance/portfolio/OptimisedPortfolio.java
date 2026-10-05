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
import java.util.List;
import java.util.Map;

import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.Variable;

/**
 * Base class of the portfolio models where the asset returns are given and the weights are calculated by
 * (constrained) optimisation.
 */
abstract class OptimisedPortfolio extends EquilibriumModel {

    static final class Template {

        BigDecimal lower;
        final String name;
        BigDecimal upper;
        BigDecimal value;

        Template(final String name) {
            super();
            this.name = name;
        }

    }

    static final String BALANCE = "Balance";
    static final String RETURN = "Return";
    static final String VARIANCE = "Variance";

    private final MatrixR064 myExpectedExcessReturns;
    private final Optimisation.Options myOptimisationOptions = new Optimisation.Options();
    private transient State myOptimisationState = State.UNEXPLORED;
    private boolean myShortingAllowed = false;
    private final Template[] myTemplates;

    OptimisedPortfolio(final MarketEquilibrium marketEquilibrium, final MatrixR064 expectedExcessReturns) {

        super(marketEquilibrium);

        if (marketEquilibrium.size() != (int) expectedExcessReturns.count()) {
            throw new IllegalArgumentException("Wrong dimensions!");
        }

        myExpectedExcessReturns = expectedExcessReturns;

        String[] symbols = this.getMarketEquilibrium().getAssetKeys();
        myTemplates = new Template[symbols.length];
        for (int i = 0; i < symbols.length; i++) {
            myTemplates[i] = new Template(symbols[i]);
        }

        myOptimisationOptions.solution = myOptimisationOptions.solution.withPrecision(7).withScale(6);
    }

    public final boolean isShortingAllowed() {
        return myShortingAllowed;
    }

    public PortfolioOptimiser optimiser() {
        return new PortfolioOptimiser(this);
    }

    public final void setShortingAllowed(final boolean allowed) {
        myShortingAllowed = allowed;
        this.reset();
    }

    @Override
    protected final MatrixR064 calculateAssetReturns() {
        return myExpectedExcessReturns;
    }

    /**
     * Records the optimisation state and extracts the asset weights. Unless the state is feasible, and not
     * unbounded, all weights are zero.
     */
    protected final MatrixR064 handle(final Optimisation.Result optimisationResult) {

        int nbAssets = myTemplates.length;

        myOptimisationState = optimisationResult.getState();
        boolean tmpFeasible = myOptimisationState.isFeasible() && myOptimisationState != State.UNBOUNDED;
        boolean tmpShortingAllowed = this.isShortingAllowed();

        MatrixR064.DenseReceiver mtrxBuilder = MATRIX_FACTORY.makeDense(nbAssets);

        BigDecimal weight;
        for (int i = 0; i < nbAssets; i++) {
            if (tmpFeasible) {
                weight = tmpShortingAllowed ? optimisationResult.get(i) : optimisationResult.get(i).max(ZERO);
            } else {
                weight = ZERO;
            }
            myTemplates[i].value = weight;
            mtrxBuilder.set(i, weight);
        }

        return mtrxBuilder.get();
    }

    @Override
    protected void reset() {

        super.reset();

        myOptimisationState = State.UNEXPLORED;
    }

    final Optimisation.Options getOptimisationOptions() {
        return myOptimisationOptions;
    }

    final State getOptimisationState() {
        return myOptimisationState;
    }

    Template getVariable(final int index) {
        return myTemplates[index];
    }

    final ExpressionsBasedModel makeModel(final Map<List<Integer>, LowerUpper> constraints) {

        ExpressionsBasedModel retVal = new ExpressionsBasedModel(myOptimisationOptions);

        int nbAssets = myTemplates.length;

        for (int i = 0; i < nbAssets; i++) {

            Template template = myTemplates[i];
            Variable variable = retVal.newVariable(template.name).lower(template.lower).upper(template.upper).value(template.value);

            if (!this.isShortingAllowed() && (template.lower == null || template.lower.signum() == -1)) {
                variable.lower(ZERO);
            }
        }

        Expression optimisationReturn = retVal.newExpression(RETURN);
        for (int i = 0; i < nbAssets; i++) {
            optimisationReturn.set(i, myExpectedExcessReturns.doubleValue(i));
        }

        Expression optimisationVariance = retVal.newExpression(VARIANCE);
        MatrixR064 covariances = this.getCovariances();
        for (int j = 0; j < nbAssets; j++) {
            for (int i = 0; i < nbAssets; i++) {
                optimisationVariance.set(i, j, covariances.get(i, j));
            }
        }

        Expression balanceExpression = retVal.newExpression(BALANCE);
        for (int i = 0; i < nbAssets; i++) {
            balanceExpression.set(i, ONE);
        }
        balanceExpression.level(ONE);

        for (Map.Entry<List<Integer>, LowerUpper> entry : constraints.entrySet()) {

            List<Integer> key = entry.getKey();
            LowerUpper value = entry.getValue();

            Expression expression = retVal.newExpression(key.toString());
            for (int i : key) {
                expression.set(i, ONE);
            }
            expression.lower(value.lower).upper(value.upper);
        }

        return retVal;
    }

    /**
     * min (RAF/2) [w]<sup>T</sup>[C][w] - [w]<sup>T</sup>[r] using a new model instance. With RAF = 0 this is
     * the maximum return (LP) problem.
     */
    final Optimisation.Result solve(final Map<List<Integer>, LowerUpper> constraints, final double riskAversion) {

        ExpressionsBasedModel model = this.makeModel(constraints);

        model.getExpression(VARIANCE).weight(riskAversion / 2.0);
        model.getExpression(RETURN).weight(NEG);

        return model.minimise();
    }

}
