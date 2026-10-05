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

import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.type.CalendarDateDuration;

/**
 * Access to the optimisation solver options, and the resulting optimisation state, of a portfolio that is the
 * result of an optimisation ({@link MarkowitzModel} or {@link EfficientFrontier}). Get an instance by calling
 * {@code optimiser()} on that portfolio.
 */
public final class PortfolioOptimiser {

    private final OptimisedPortfolio myPortfolio;

    PortfolioOptimiser(final OptimisedPortfolio portfolio) {
        super();
        myPortfolio = portfolio;
    }

    /**
     * Will turn on debug logging for the optimisation solver.
     */
    public PortfolioOptimiser debug(final boolean debug) {

        Optimisation.Options options = myPortfolio.getOptimisationOptions();

        boolean tmpValidate = options.validate;

        if (debug) {
            options.debug(Optimisation.Solver.class);
        } else {
            options.debug(null);
        }

        options.validate = tmpValidate;

        return this;
    }

    /**
     * Sets the scale (number of decimal places) of the feasibility tolerance.
     */
    public PortfolioOptimiser feasibility(final int scale) {
        Optimisation.Options options = myPortfolio.getOptimisationOptions();
        options.feasibility = options.feasibility.withScale(scale);
        return this;
    }

    /**
     * You have to call some method that will trigger the calculation (any method that requires the
     * calculation results) before you check the optimisation state. Otherwise you'll simply get
     * State.UNEXPLORED.
     * <p>
     * If the state is not feasible, or is unbounded, no usable solution was found and all the asset weights
     * are set to zero.
     */
    public State getState() {
        return myPortfolio.getOptimisationState();
    }

    /**
     * @param max The maximum amount of time for the optimisation solver
     */
    public PortfolioOptimiser time(final CalendarDateDuration max) {
        Optimisation.Options options = myPortfolio.getOptimisationOptions();
        long maxDurationInMillis = max.toDurationInMillis();
        options.time_abort = maxDurationInMillis;
        options.time_suffice = maxDurationInMillis;
        return this;
    }

    /**
     * Will validate the generated optimisation problem and throws an exception if it's not ok. This should
     * typically not be enabled in a production environment.
     */
    public PortfolioOptimiser validate(final boolean validate) {
        myPortfolio.getOptimisationOptions().validate = validate;
        return this;
    }

}
