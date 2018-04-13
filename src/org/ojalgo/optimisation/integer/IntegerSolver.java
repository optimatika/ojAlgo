/*
 * Copyright 1997-2018 Optimatika
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

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.multiary.MultiaryFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.GenericSolver;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

public abstract class IntegerSolver extends GenericSolver {

    public static final class ModelIntegration extends ExpressionsBasedModel.Integration<IntegerSolver> {

        public IntegerSolver build(final ExpressionsBasedModel model) {
            return IntegerSolver.make(model);
        }

        public boolean isCapable(final ExpressionsBasedModel model) {
            return !model.isAnyConstraintQuadratic();
        }

        @Override
        public Result toModelState(final Result solverState, final ExpressionsBasedModel model) {
            return solverState;
        }

        @Override
        public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {
            return modelState;
        }

        @Override
        protected boolean isSolutionMapped() {
            return false;
        }

    }

    static final class NodeStatistics {

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
         * Integer solution
         */
        private final AtomicInteger myInteger = new AtomicInteger();
        /**
         * Noninteger solution
         */
        private final AtomicInteger myTruncated = new AtomicInteger();

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
         * Node evaluated, but solution not integer. Estimate still possible to find better integer solution.
         * Created 2 new branches.
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

    public static IntegerSolver make(final ExpressionsBasedModel model) {
        return new OldIntegerSolver(model, model.options);
        //return new NewIntegerSolver(model, model.options);
    }

    private volatile Optimisation.Result myBestResultSoFar = null;
    private final MultiaryFunction.TwiceDifferentiable<Double> myFunction;
    private final int[] myIntegerIndices;
    private final ExpressionsBasedModel myIntegerModel;
    private final double[] myIntegerSignificances;
    private final AtomicInteger myIntegerSolutionsCount = new AtomicInteger();
    private final boolean myMinimisation;
    private final NodeStatistics myNodeStatistics = new NodeStatistics();

    protected IntegerSolver(final ExpressionsBasedModel model, final Options solverOptions) {

        super(solverOptions);

        myIntegerModel = model.simplify();
        myFunction = myIntegerModel.objective().toFunction();

        myMinimisation = myIntegerModel.isMinimisation();

        final List<Variable> integerVariables = myIntegerModel.getIntegerVariables();
        myIntegerIndices = new int[integerVariables.size()];
        for (int i = 0; i < myIntegerIndices.length; i++) {
            myIntegerIndices[i] = myIntegerModel.indexOf(integerVariables.get(i));
        }

        myIntegerSignificances = new double[integerVariables.size()];
        Arrays.fill(myIntegerSignificances, ONE);
    }

    protected int countIntegerSolutions() {
        return myIntegerSolutionsCount.intValue();
    }

    @Override
    protected final double evaluateFunction(final Access1D<?> solution) {
        if ((myFunction != null) && (solution != null) && (myFunction.arity() == solution.count())) {
            return myFunction.invoke(Access1D.asPrimitive1D(solution));
        } else {
            return Double.NaN;
        }
    }

    protected Optimisation.Result getBestResultSoFar() {

        final Result tmpCurrentlyTheBest = myBestResultSoFar;

        if (tmpCurrentlyTheBest != null) {

            return tmpCurrentlyTheBest;

        } else {

            final State tmpSate = State.INVALID;
            final double tmpValue = myMinimisation ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
            final MatrixStore<Double> tmpSolution = MatrixStore.PRIMITIVE.makeZero(this.getIntegerModel().countVariables(), 1).get();

            return new Optimisation.Result(tmpSate, tmpValue, tmpSolution);
        }
    }

    protected final MatrixStore<Double> getGradient(final Access1D<Double> solution) {
        return myFunction.getGradient(solution);
    }

    protected ExpressionsBasedModel getIntegerModel() {
        return myIntegerModel;
    }

    protected final ExpressionsBasedModel getRelaxedModel() {
        return myIntegerModel.relax(false);
    }

    protected abstract boolean initialise(Result kickStarter);

    protected final boolean isFunctionSet() {
        return myFunction != null;
    }

    protected boolean isGoodEnoughToContinueBranching(final double relaxedNodeValue) {

        final Result bestResultSoFar = myBestResultSoFar;

        if ((bestResultSoFar == null) || Double.isNaN(relaxedNodeValue)) {

            return true;

        } else {

            final double bestIntegerValue = bestResultSoFar.getValue();

            final double absoluteGap = PrimitiveFunction.ABS.invoke(bestIntegerValue - relaxedNodeValue);
            final double relativeGap = PrimitiveFunction.ABS.invoke(absoluteGap / bestIntegerValue);

            if (myMinimisation) {
                return (relaxedNodeValue < bestIntegerValue) && (relativeGap > options.mip_gap) && (absoluteGap > options.mip_gap);
            } else {
                return (relaxedNodeValue > bestIntegerValue) && (relativeGap > options.mip_gap) && (absoluteGap > options.mip_gap);
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

            return (this.countTime() < options.time_suffice) && (this.countIterations() < options.iterations_suffice);
        }
    }

    protected final boolean isModelSet() {
        return myIntegerModel != null;
    }

    protected synchronized void markInteger(final NodeKey key, final ExpressionsBasedModel model, final Optimisation.Result result) {

        if (this.isProgress()) {
            this.log("New integer solution {}", result);
            this.log("\t@ node {}", key);
        }

        final Optimisation.Result tmpCurrentlyTheBest = myBestResultSoFar;

        if (tmpCurrentlyTheBest == null) {

            myBestResultSoFar = result;

        } else if (myMinimisation && (result.getValue() < tmpCurrentlyTheBest.getValue())) {

            myBestResultSoFar = result;

        } else if (!myMinimisation && (result.getValue() > tmpCurrentlyTheBest.getValue())) {

            myBestResultSoFar = result;

        } else {

            if (this.isDebug()) {
                this.log("Previously best {}", myBestResultSoFar);
            }
        }

        if (tmpCurrentlyTheBest != null) {

            final double objDiff = ABS.invoke((result.getValue() - tmpCurrentlyTheBest.getValue()) / tmpCurrentlyTheBest.getValue());

            for (int i = 0; i < myIntegerIndices.length; i++) {
                final double varDiff = ABS.invoke(result.doubleValue(myIntegerIndices[i]) - tmpCurrentlyTheBest.doubleValue(myIntegerIndices[i]));
                if (!options.feasibility.isZero(varDiff)) {
                    this.addIntegerSignificance(i, objDiff / varDiff);
                }
            }

        } else {

            final MatrixStore<Double> gradient = this.getGradient(Access1D.asPrimitive1D(result));
            final double largest = gradient.aggregateAll(Aggregator.LARGEST);

            if (largest > ZERO) {
                for (int i = 0; i < myIntegerIndices.length; i++) {
                    this.addIntegerSignificance(i, ABS.invoke(gradient.doubleValue(myIntegerIndices[i])) / largest);
                }
            }
        }

        myIntegerSolutionsCount.incrementAndGet();
    }

    protected abstract boolean needsAnotherIteration();

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

    void addIntegerSignificance(final int index, final double significance) {
        myIntegerSignificances[index] += significance;
    }

    final int getGlobalIndex(final int integerIndex) {
        return myIntegerIndices[integerIndex];
    }

    final int[] getIntegerIndices() {
        return myIntegerIndices;
    }

    double getIntegerSignificance(final int index) {
        return myIntegerSignificances[index];
    }

    /**
     * Should return the index of the (best) variable to branch on. Returning a negative index means an
     * integer solition has been found (no further branching).
     */
    final int identifyNonIntegerVariable(final Optimisation.Result nodeResult, final NodeKey nodeKey) {

        int retVal = -1;

        double fraction;
        double compareFraction = ZERO;
        double maxFraction = ZERO;

        for (int i = 0; i < myIntegerIndices.length; i++) {

            fraction = nodeKey.getFraction(i, nodeResult.doubleValue(myIntegerIndices[i]));
            // [0, 0.5]

            if (!options.feasibility.isZero(fraction)) {

                if (this.isIntegerSolutionFound()) {
                    // If an integer solution is already found
                    // then scale the fraction by its significance

                    compareFraction = fraction * this.getIntegerSignificance(i);

                } else {
                    // If not yet found integer solution
                    // then compare the remaining/reversed (larger) fraction

                    compareFraction = ONE - fraction;
                    // [0.5, 1.0]
                }

                if (compareFraction > maxFraction) {
                    retVal = i;
                    maxFraction = compareFraction;
                }
            }

        }

        return retVal;
    }

}
