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

import static org.ojalgo.function.constant.BigMath.ONE;
import static org.ojalgo.function.constant.BigMath.ZERO;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.CalendarDateDuration;
import org.ojalgo.type.TypeUtils;

abstract class OptimisedPortfolio extends EquilibriumModel {

    public final class Optimiser {

        /**
         * Will turn on debug logging for the optimisation solver.
         */
        public Optimiser debug(final boolean debug) {

            boolean tmpValidate = myOptimisationOptions.validate;

            if (debug) {
                myOptimisationOptions.debug(Optimisation.Solver.class);
            } else {
                myOptimisationOptions.debug(null);
            }

            myOptimisationOptions.validate = tmpValidate;

            return this;
        }

        public Optimiser feasibility(final int scale) {
            myOptimisationOptions.feasibility = myOptimisationOptions.feasibility.withScale(scale);
            return this;
        }

        /**
         * You have to call some method that will trigger the calculation (any method that requires the
         * calculation results) before you check the optimisation state. Otherwise you'll simply get
         * State.UNEXPLORED.
         */
        public State getState() {
            if (myOptimisationState == null) {
                myOptimisationState = State.UNEXPLORED;
            }
            return myOptimisationState;
        }

        /**
         * @param max The maximum amount of time for the optimisation solver
         */
        public Optimiser time(final CalendarDateDuration max) {
            long maxDurationInMillis = max.toDurationInMillis();
            myOptimisationOptions.time_abort = maxDurationInMillis;
            myOptimisationOptions.time_suffice = maxDurationInMillis;
            return this;
        }

        /**
         * Will validate the generated optimisation problem and throws an excption if it's not ok. This should
         * typically not be enabled in a production environment.
         */
        public Optimiser validate(final boolean validate) {
            myOptimisationOptions.validate = validate;
            return this;
        }

    }

    static final class Template {

        BigDecimal lower;
        final String name;
        BigDecimal upper;
        BigDecimal value;
        BigDecimal weight;

        Template(final String name) {
            super();
            this.name = name;
        }

    }

    static final String BALANCE = "Balance";
    static final String VARIANCE = "Variance";

    private final MatrixR064 myExpectedExcessReturns;
    private final Optimisation.Options myOptimisationOptions = new Optimisation.Options();
    private transient State myOptimisationState = State.UNEXPLORED;
    private boolean myShortingAllowed = false;
    private final Template[] myTemplates;

    OptimisedPortfolio(final FinancePortfolio.Context portfolioContext) {

        super(portfolioContext);

        myExpectedExcessReturns = portfolioContext.getAssetReturns();

        String[] symbols = this.getMarketEquilibrium().getAssetKeys();
        myTemplates = new Template[symbols.length];
        for (int i = 0; i < symbols.length; i++) {
            myTemplates[i] = new Template(symbols[i]);
            myTemplates[i].weight = TypeUtils.toBigDecimal(myExpectedExcessReturns.get(i)).negate();
        }

        myOptimisationOptions.solution = myOptimisationOptions.solution.withPrecision(7).withScale(6);
    }

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
            myTemplates[i].weight = TypeUtils.toBigDecimal(expectedExcessReturns.get(i)).negate();
        }

        myOptimisationOptions.solution = myOptimisationOptions.solution.withPrecision(7).withScale(6);
    }

    OptimisedPortfolio(final MatrixR064 covarianceMatrix, final MatrixR064 expectedExcessReturns) {
        this(new MarketEquilibrium(covarianceMatrix), expectedExcessReturns);
    }

    public final boolean isShortingAllowed() {
        return myShortingAllowed;
    }

    public Optimiser optimiser() {
        return new Optimiser();
    }

    public final void setShortingAllowed(final boolean allowed) {
        myShortingAllowed = allowed;
        this.reset();
    }

    @Override
    protected final MatrixR064 calculateAssetReturns() {
        return myExpectedExcessReturns;
    }

    protected final MatrixR064 handle(final Optimisation.Result optimisationResult) {

        int nbAssets = myTemplates.length;

        myOptimisationState = optimisationResult.getState();
        boolean tmpFeasible = optimisationResult.getState().isFeasible();
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

    Template getVariable(final int index) {
        return myTemplates[index];
    }

    final ExpressionsBasedModel makeModel(final Map<int[], LowerUpper> constraints) {

        ExpressionsBasedModel retVal = new ExpressionsBasedModel(myOptimisationOptions);

        int nbAssets = myTemplates.length;

        for (int i = 0; i < nbAssets; i++) {

            Template template = myTemplates[i];
            Variable variable = retVal.newVariable(template.name).weight(template.weight).lower(template.lower).upper(template.upper).value(template.value);

            if (!this.isShortingAllowed() && (template.lower == null || template.lower.signum() == -1)) {
                variable.lower(ZERO);
            }
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

        for (Map.Entry<int[], LowerUpper> entry : constraints.entrySet()) {

            int[] key = entry.getKey();
            LowerUpper value = entry.getValue();

            Expression expression = retVal.newExpression(Arrays.toString(key));
            for (int i = 0; i < key.length; i++) {
                expression.set(key[i], ONE);
            }
            expression.lower(value.lower).upper(value.upper);
        }

        return retVal;
    }

}
