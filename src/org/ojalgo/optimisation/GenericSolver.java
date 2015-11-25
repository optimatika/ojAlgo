/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
package org.ojalgo.optimisation;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.netio.BasicLogger;

public abstract class GenericSolver implements Optimisation.Solver, Serializable {

    public final Optimisation.Options options;

    private final AtomicInteger myIterationsCount = new AtomicInteger(0);
    private long myResetTime;
    private State myState = State.UNEXPLORED;

    @SuppressWarnings("unused")
    private GenericSolver() {
        this(null);
    }

    /**
     */
    protected GenericSolver(final Optimisation.Options solverOptions) {

        super();

        if (solverOptions != null) {
            options = solverOptions;
        } else {
            options = new Optimisation.Options();
        }

    }

    protected Optimisation.Result buildResult() {

        final MatrixStore<Double> tmpSolution = this.extractSolution();
        final double tmpValue = this.evaluateFunction(tmpSolution);
        final Optimisation.State tmpState = this.getState();

        return new Optimisation.Result(tmpState, tmpValue, tmpSolution);
    }

    protected final int countIterations() {
        return myIterationsCount.get();
    }

    protected abstract double evaluateFunction(final Access1D<?> solution);

    protected final long countTime() {
        return System.currentTimeMillis() - myResetTime;
    }

    protected final void debug(final String descripttion, final Access2D<?> matrix) {
        if (options.debug_appender != null) {
            options.debug_appender.printmtrx(descripttion, matrix, options.print);
        }
    }

    protected final void debug(final String messagePattern, final Object... arguments) {
        if (options.debug_appender != null) {
            options.debug_appender.println(messagePattern, arguments);
        }
    }

    protected final void error(final String messagePattern, final Object... arguments) {
        BasicLogger.error(messagePattern, arguments);
    }

    /**
     * Should be able to feed this to {@link #evaluateFunction(Access1D)}.
     */
    protected abstract MatrixStore<Double> extractSolution();

    protected final State getState() {
        return myState;
    }

    /**
     * Should be called after a completed iteration. The iterations count is not "1" untill the first
     * iteration is completed.
     */
    protected final int incrementIterationsCount() {
        return myIterationsCount.incrementAndGet();
    }

    protected abstract boolean initialise(Result kickStarter);

    protected final boolean isDebug() {
        return (options.debug_appender != null) && (options.debug_solver.isAssignableFrom(this.getClass()));
    }

    /**
     * Should be called at the start of an iteration (before it actually starts) to check if you should abort
     * instead. Will return false if either the iterations count or the execution time has reached their
     * respective limits.
     */
    protected final boolean isIterationAllowed() {

        final int tmpIterations = this.countIterations();
        final long tmpTime = this.countTime();

        final boolean tmpIterationOk = tmpIterations < options.iterations_abort;
        final boolean tmpTimeOk = tmpTime < options.time_abort;

        //        if (this.isDebug()) {
        //            this.logDebug("Iterations OK? {} {} < {}", tmpIterationOk, tmpIterations, options.iterations_abort);
        //            this.logDebug("Time OK? {} {} < {}", tmpTimeOk, tmpTime, options.time_abort);
        //        }

        return tmpTimeOk && tmpIterationOk;
    }

    protected abstract boolean needsAnotherIteration();

    protected final void resetIterationsCount() {
        myIterationsCount.set(0);
        myResetTime = System.currentTimeMillis();
    }

    protected final void setState(final State aState) {
        myState = aState;
    }

    /**
     * Should validate the solver data/input/structue. Even "expensive" validation can be performed as the
     * method should only be called if {@linkplain Optimisation.Options#validate} is set to true. In addition
     * to returning true or false the implementation should set the state to either
     * {@linkplain Optimisation.State#VALID} or {@linkplain Optimisation.State#INVALID} (or possibly
     * {@linkplain Optimisation.State#FAILED}). Typically the method should be called at the very beginning of
     * the solve-method.
     *
     * @return Is the solver instance valid?
     */
    protected abstract boolean validate();

}
