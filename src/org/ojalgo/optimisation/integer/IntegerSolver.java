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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.FunctionUtils;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.multiary.MultiaryFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.netio.CharacterRing;
import org.ojalgo.netio.CharacterRing.PrinterBuffer;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.GenericSolver;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.TypeUtils;

public final class IntegerSolver extends GenericSolver {

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

    final class BranchAndBoundNodeTask extends RecursiveTask<Boolean> implements Comparable<BranchAndBoundNodeTask> {

        private final NodeKey myKey;
        private final PrinterBuffer myPrinter = IntegerSolver.this.isDebug() ? new CharacterRing().asPrinter() : null;

        private BranchAndBoundNodeTask(final NodeKey key) {

            super();

            myKey = key;
        }

        BranchAndBoundNodeTask() {

            super();

            myKey = new NodeKey(IntegerSolver.this.getIntegerModel());
        }

        public int compareTo(BranchAndBoundNodeTask o) {
            return myKey.compareTo(o.getKey());
        }

        @Override
        public String toString() {
            return myKey.toString();
        }

        private void flush(final BasicLogger.Printer receiver) {
            if ((myPrinter != null) && (receiver != null)) {
                myPrinter.flush(receiver);
            }
        }

        private boolean isNodeDebug() {
            return (myPrinter != null) && IntegerSolver.this.isDebug();
        }

        @Override
        protected Boolean compute() {

            final ExpressionsBasedModel nodeModel = IntegerSolver.this.getRelaxedModel();
            myKey.setNodeState(nodeModel, IntegerSolver.this.getIntegerIndices());

            if (IntegerSolver.this.isIntegerSolutionFound()) {

                final double mip_gap = IntegerSolver.this.options.mip_gap;

                final double bestIntegerSolutionValue = IntegerSolver.this.getBestResultSoFar().getValue();
                final double parentRelaxedSolutionValue = myKey.objective;

                final double absoluteValue = ABS.invoke(bestIntegerSolutionValue);
                final double absoluteGap = ABS.invoke(absoluteValue - parentRelaxedSolutionValue);

                final double small = FunctionUtils.max(mip_gap, absoluteGap * mip_gap, absoluteValue * mip_gap);

                if (nodeModel.isMinimisation()) {
                    final BigDecimal upperLimit = TypeUtils.toBigDecimal(bestIntegerSolutionValue - small, IntegerSolver.this.options.feasibility);
                    // final BigDecimal lowerLimit = TypeUtils.toBigDecimal(parentRelaxedSolutionValue, IntegerSolver.this.options.feasibility);
                    nodeModel.limitObjective(null, upperLimit);
                } else {
                    final BigDecimal lowerLimit = TypeUtils.toBigDecimal(bestIntegerSolutionValue + small, IntegerSolver.this.options.feasibility);
                    // final BigDecimal upperLimit = TypeUtils.toBigDecimal(parentRelaxedSolutionValue, IntegerSolver.this.options.feasibility);
                    nodeModel.limitObjective(lowerLimit, null);
                }
            }

            return this.compute(nodeModel.prepare());
        }

        protected Boolean compute(final ExpressionsBasedModel.Intermediate nodeModel) {

            if (this.isNodeDebug()) {
                myPrinter.println();
                myPrinter.println("Branch&Bound Node");
                myPrinter.println(myKey.toString());
                myPrinter.println(IntegerSolver.this.toString());
            }

            if (!IntegerSolver.this.isIterationAllowed() || !IntegerSolver.this.isIterationNecessary()) {
                if (this.isNodeDebug()) {
                    myPrinter.println("Reached iterations or time limit - stop!");
                    this.flush(IntegerSolver.this.getIntegerModel().options.logger_appender);
                }
                return false;
            }

            if (!IntegerSolver.this.isGoodEnoughToContinueBranching(myKey.objective)) {
                if (this.isNodeDebug()) {
                    myPrinter.println("No longer a relevant node!");
                    this.flush(IntegerSolver.this.getIntegerModel().options.logger_appender);
                }
                return true;
            }

            if (myKey.index >= 0) {
                myKey.enforceBounds(nodeModel, IntegerSolver.this.getIntegerIndices());
                //myKey.setNodeState(nodeModel, IntegerSolver.this.getIntegerIndices());
            }

            final Result bestResultSoFar = IntegerSolver.this.getBestResultSoFar();
            final Optimisation.Result nodeResult = nodeModel.solve(bestResultSoFar);

            // Increment when/if an iteration was actually performed
            IntegerSolver.this.incrementIterationsCount();

            if (this.isNodeDebug()) {
                myPrinter.println("Node Result: {}", nodeResult);
            }

            if (nodeResult.getState().isOptimal()) {
                if (this.isNodeDebug()) {
                    myPrinter.println("Node solved to optimality!");
                }

                if (IntegerSolver.this.options.validate && !nodeModel.validate(nodeResult)) {
                    // This should not be possible. There is a bug somewhere.
                    myPrinter.println("Node solution marked as OPTIMAL, but is actually INVALID/INFEASIBLE/FAILED. Stop this branch!");
                    myPrinter.println("Lower bounds: {}", Arrays.toString(myKey.getLowerBounds()));
                    myPrinter.println("Upper bounds: {}", Arrays.toString(myKey.getUpperBounds()));

                    // nodeModel.validate(nodeResult, myPrinter);

                    this.flush(IntegerSolver.this.getIntegerModel().options.logger_appender);

                    return false;
                }

                final int branchIntegerIndex = IntegerSolver.this.identifyNonIntegerVariable(nodeResult, myKey);
                final double tmpSolutionValue = IntegerSolver.this.evaluateFunction(nodeResult);

                if (branchIntegerIndex == -1) {
                    if (this.isNodeDebug()) {
                        myPrinter.println("Integer solution! Store it among the others, and stop this branch!");
                    }

                    final Optimisation.Result tmpIntegerSolutionResult = new Optimisation.Result(Optimisation.State.FEASIBLE, tmpSolutionValue, nodeResult);

                    IntegerSolver.this.markInteger(myKey, null, tmpIntegerSolutionResult);

                    if (this.isNodeDebug()) {
                        myPrinter.println(IntegerSolver.this.getBestResultSoFar().toString());
                        BasicLogger.debug();
                        BasicLogger.debug(IntegerSolver.this.toString());
                        // BasicLogger.debug(DaemonPoolExecutor.INSTANCE.toString());
                        this.flush(IntegerSolver.this.getIntegerModel().options.logger_appender);
                    }

                    nodeModel.dispose();
                    return true;

                } else {
                    if (this.isNodeDebug()) {
                        myPrinter.println("Not an Integer Solution: " + tmpSolutionValue);
                    }

                    final double tmpVariableValue = nodeResult.doubleValue(IntegerSolver.this.getGlobalIndex(branchIntegerIndex));

                    if (IntegerSolver.this.isGoodEnoughToContinueBranching(tmpSolutionValue)) {

                        if (this.isNodeDebug()) {
                            myPrinter.println("Still hope, branching on {} @ {} >>> {}", branchIntegerIndex, tmpVariableValue,
                                    nodeModel.getVariable(IntegerSolver.this.getGlobalIndex(branchIntegerIndex)));
                            this.flush(IntegerSolver.this.getIntegerModel().options.logger_appender);
                        }

                        // IntegerSolver.this.generateCuts(nodeModel);

                        final BranchAndBoundNodeTask lowerBranch = this.createLowerBranch(branchIntegerIndex, tmpVariableValue, tmpSolutionValue);
                        final BranchAndBoundNodeTask upperBranch = this.createUpperBranch(branchIntegerIndex, tmpVariableValue, tmpSolutionValue);

                        final BranchAndBoundNodeTask nextTask;
                        final BranchAndBoundNodeTask forkedTask;
                        BranchAndBoundNodeTask deferredTask;

                        final double fractional = tmpVariableValue - Math.floor(tmpVariableValue);
                        if (fractional >= HALF) {
                            nextTask = upperBranch;
                            if (fractional > 0.95) {
                                forkedTask = null;
                                //deferredTask = lowerBranch;
                                deferred.offer(lowerBranch);
                            } else {
                                forkedTask = lowerBranch;
                                deferredTask = null;
                            }
                        } else {
                            nextTask = lowerBranch;
                            if (fractional < 0.05) {
                                forkedTask = null;
                                // deferredTask = upperBranch;
                                deferred.offer(upperBranch);
                            } else {
                                forkedTask = upperBranch;
                                deferredTask = null;
                            }
                        }

                        if (forkedTask != null) {

                            forkedTask.fork();

                            return nextTask.compute(nodeModel) && forkedTask.join();

                        } else {

                            Boolean tmpCompute = nextTask.compute(nodeModel);

                            deferredTask = deferred.poll();
                            if (tmpCompute.booleanValue() && (deferredTask != null)) {
                                tmpCompute = deferredTask.compute();
                            }

                            return tmpCompute;
                        }

                    } else {
                        if (this.isNodeDebug()) {
                            myPrinter.println("Can't find better integer solutions - stop this branch!");
                            this.flush(IntegerSolver.this.getIntegerModel().options.logger_appender);
                        }

                        nodeModel.dispose();
                        return true;
                    }
                }

            } else {
                if (this.isNodeDebug()) {
                    myPrinter.println("Failed to solve node problem - stop this branch!");
                    this.flush(IntegerSolver.this.getIntegerModel().options.logger_appender);
                }

                nodeModel.dispose();
                return true;
            }

        }

        BranchAndBoundNodeTask createLowerBranch(final int branchIntegerIndex, final double nonIntegerValue, final double parentObjectiveValue) {

            final NodeKey tmpKey = myKey.createLowerBranch(branchIntegerIndex, nonIntegerValue, parentObjectiveValue);

            return new BranchAndBoundNodeTask(tmpKey);
        }

        BranchAndBoundNodeTask createUpperBranch(final int branchIntegerIndex, final double nonIntegerValue, final double parentObjectiveValue) {

            final NodeKey tmpKey = myKey.createUpperBranch(branchIntegerIndex, nonIntegerValue, parentObjectiveValue);

            return new BranchAndBoundNodeTask(tmpKey);
        }

        NodeKey getKey() {
            return myKey;
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
        return new IntegerSolver(model, model.options);
    }

    private volatile Optimisation.Result myBestResultSoFar = null;
    private final MultiaryFunction.TwiceDifferentiable<Double> myFunction;
    /**
     * One entry per integer variable, the entry is the global index of that integer variable
     */
    private final int[] myIntegerIndices;
    private final ExpressionsBasedModel myIntegerModel;
    private final double[] myIntegerSignificances;
    private final AtomicInteger myIntegerSolutionsCount = new AtomicInteger();
    private final boolean myMinimisation;
    private final NodeStatistics myNodeStatistics = new NodeStatistics();
    PriorityBlockingQueue<BranchAndBoundNodeTask> deferred = new PriorityBlockingQueue<BranchAndBoundNodeTask>();

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

    public Result solve(final Result kickStarter) {

        // Must verify that it actually is an integer solution
        // The kickStarter may be user-supplied
        if ((kickStarter != null) && kickStarter.getState().isFeasible() && this.getIntegerModel().validate(kickStarter)) {
            this.markInteger(null, null, kickStarter);
        }

        this.resetIterationsCount();

        final BranchAndBoundNodeTask rootNodeTask = new BranchAndBoundNodeTask();

        final boolean normalExit = ForkJoinPool.commonPool().invoke(rootNodeTask);

        final Optimisation.Result bestSolutionFound = this.getBestResultSoFar();

        if (bestSolutionFound.getState().isFeasible()) {
            if (normalExit) {
                return new Optimisation.Result(State.OPTIMAL, bestSolutionFound);
            } else {
                return new Optimisation.Result(State.FEASIBLE, bestSolutionFound);
            }
        } else {
            if (normalExit) {
                return new Optimisation.Result(State.INFEASIBLE, bestSolutionFound);
            } else {
                return new Optimisation.Result(State.FAILED, bestSolutionFound);
            }
        }
    }

    @Override
    public String toString() {
        return TypeUtils.format("Solutions={} Nodes/Iterations={} {}", this.countIntegerSolutions(), this.countExploredNodes(), this.getBestResultSoFar());
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

    @Override
    protected MatrixStore<Double> extractSolution() {
        return PrimitiveDenseStore.FACTORY.columns(this.getBestResultSoFar());
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

    protected boolean initialise(final Result kickStarter) {
        return true;
    }

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

    protected boolean needsAnotherIteration() {
        return !this.getState().isOptimal();
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
    protected boolean validate() {

        boolean retVal = true;
        this.setState(State.VALID);

        try {

            if (!(retVal = this.getIntegerModel().validate())) {
                retVal = false;
                this.setState(State.INVALID);
            }

        } catch (final Exception ex) {

            retVal = false;
            this.setState(State.FAILED);
        }

        return retVal;
    }

    void addIntegerSignificance(final int index, final double significance) {
        myIntegerSignificances[index] += significance;
    }

    int countExploredNodes() {
        // return myExploredNodes.size();
        return 0;
    }

    void generateCuts(final ExpressionsBasedModel nodeModel) {
        //   nodeModel.generateCuts(myPotentialCutExpressions);
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
     * Should return the index of the (best) integer variable to branch on. Returning a negative index means
     * an integer solution has been found (no further branching). Does NOT return a global variable index -
     * it's the index among the ineteger variable.
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

    boolean isExplored(final BranchAndBoundNodeTask aNodeTask) {
        // return myExploredNodes.contains(aNodeTask.getKey());
        return false;
    }

    void markAsExplored(final BranchAndBoundNodeTask aNodeTask) {

        // myExploredNodes.add(aNodeTask.getKey());
    }

}
