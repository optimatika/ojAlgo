/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
package org.ojalgo.optimisation.integer;

import java.util.concurrent.atomic.AtomicInteger;

import org.ojalgo.matrix.store.ZeroStore;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.GenericSolver;
import org.ojalgo.optimisation.Optimisation;

public abstract class IntegerSolver extends GenericSolver {

    final class NodeStatistics {

        private final AtomicInteger myAbandoned = new AtomicInteger();
        /**
         * Resulted in 2 new nodes
         */
        private final AtomicInteger myBranched = new AtomicInteger();
        /**
         * Integer solution found and/or solution not good enough to continue
         */
        private final AtomicInteger myExhausted = new AtomicInteger();
        /**
         * Failed to solve node problem (not because it was infeasible)
         */
        private final AtomicInteger myFailed = new AtomicInteger();
        /**
         * Node problem infeasible
         */
        private final AtomicInteger myInfeasible = new AtomicInteger();
        /**
         * Noninteger solution
         */
        private final AtomicInteger myTruncated = new AtomicInteger();
        /**
         * Integer solution
         */
        private final AtomicInteger myInteger = new AtomicInteger();

        public int countCreated() {
            return myTruncated.get() + myAbandoned.get() + this.countEvaluated();
        }

        public int countEvaluated() {
            return myInfeasible.get() + myFailed.get() + myExhausted.get() + myBranched.get();
        }

        /**
         * Node never evaluated (sub/node problem never solved)
         */
        boolean abandoned() {
            myAbandoned.incrementAndGet();
            return true;
        }

        /**
         * Node evaluated, but solution not integer. Estimate still possible to find better integer solution. Created 2
         * new branches.
         */
        boolean branched() {
            myBranched.incrementAndGet();
            return true;
        }

        /**
         * Node evaluated, but solution not integer. Estimate NOT possible to find better integer solution.
         */
        boolean exhausted() {
            myExhausted.incrementAndGet();
            return true;
        }

        boolean failed(final boolean state) {
            myFailed.incrementAndGet();
            return state;
        }

        boolean infeasible() {
            myInfeasible.incrementAndGet();
            return true;
        }

        boolean infeasible(final boolean state) {
            myInfeasible.incrementAndGet();
            return state;
        }

        /**
         * Integer solution found
         */
        boolean integer() {
            myInteger.incrementAndGet();
            return true;
        }

        boolean truncated(final boolean state) {
            myTruncated.incrementAndGet();
            return state;
        }

    }

    private volatile Optimisation.Result myBestResultSoFar = null;
    private final AtomicInteger myIntegerSolutionsCount = new AtomicInteger();
    private final boolean myMinimisation;
    private final NodeStatistics myNodeStatistics = new NodeStatistics();

    public IntegerSolver(final ExpressionsBasedModel model, final Options solverOptions) {

        super(model, solverOptions);

        myMinimisation = model.isMinimisation();
    }

    protected int countIntegerSolutions() {
        return myIntegerSolutionsCount.intValue();
    }

    protected Optimisation.Result getBestResultSoFar() {

        final Result tmpCurrentlyTheBest = myBestResultSoFar;

        if (tmpCurrentlyTheBest != null) {

            return tmpCurrentlyTheBest;

        } else {

            final State tmpSate = State.INVALID;
            final double tmpValue = myMinimisation ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
            final ZeroStore<Double> tmpSolution = ZeroStore.PRIMITIVE.make(this.getModel().countVariables(), 1);

            return new Optimisation.Result(tmpSate, tmpValue, tmpSolution);
        }
    }

    protected boolean isGoodEnoughToContinueBranching(final double nonIntegerValue) {

        final Result tmpCurrentlyTheBest = myBestResultSoFar;

        if (tmpCurrentlyTheBest == null) {

            return true;

        } else {

            final double tmpBestIntegerValue = tmpCurrentlyTheBest.getValue();

            final double tmpMipGap = Math.abs(tmpBestIntegerValue - nonIntegerValue) / Math.abs(tmpBestIntegerValue);

            if (myMinimisation) {
                return (nonIntegerValue < tmpBestIntegerValue) && (tmpMipGap > options.mip_gap);
            } else {
                return (nonIntegerValue > tmpBestIntegerValue) && (tmpMipGap > options.mip_gap);
            }
        }
    }

    protected boolean isIntegerSolutionFound() {
        return myBestResultSoFar != null;
    }

    protected boolean isIterationNecessary() {

        if (myBestResultSoFar == null) {

            return true;

        } else {

            final int tmpIterations = this.countIterations();
            final long tmpTime = this.countTime();

            return (tmpTime < options.time_suffice) && (tmpIterations < options.iterations_suffice);
        }
    }

    protected synchronized void markInteger(final NodeKey node, final Optimisation.Result result) {

        final Optimisation.Result tmpCurrentlyTheBest = myBestResultSoFar;

        if (tmpCurrentlyTheBest == null) {

            myBestResultSoFar = result;

        } else if (myMinimisation && (result.getValue() < tmpCurrentlyTheBest.getValue())) {

            myBestResultSoFar = result;

        } else if (!myMinimisation && (result.getValue() > tmpCurrentlyTheBest.getValue())) {

            myBestResultSoFar = result;
        }

        myIntegerSolutionsCount.incrementAndGet();
    }

}
