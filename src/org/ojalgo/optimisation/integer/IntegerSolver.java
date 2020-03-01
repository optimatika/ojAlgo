/*
 * Copyright 1997-2020 Optimatika
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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.multiary.MultiaryFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.netio.CharacterRing;
import org.ojalgo.netio.CharacterRing.PrinterBuffer;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.GenericSolver;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.structure.Access1D;
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

    final class BranchAndBoundNodeTask extends RecursiveTask<Boolean> {

        private final NodeKey myKey;
        private final PrinterBuffer myPrinter = (IntegerSolver.this.options.validate || IntegerSolver.this.isLogProgress()) ? new CharacterRing().asPrinter()
                : null;

        private BranchAndBoundNodeTask(final NodeKey key) {

            super();

            myKey = key;
        }

        BranchAndBoundNodeTask() {

            super();

            myKey = new NodeKey(IntegerSolver.this.getIntegerModel());
        }

        @Override
        public String toString() {
            return myKey.toString();
        }

        @Override
        protected Boolean compute() {

            final ExpressionsBasedModel nodeModel = IntegerSolver.this.getNodeModel();
            myKey.setNodeState(nodeModel, IntegerSolver.this.getIntegerIndices());

            if (IntegerSolver.this.isIntegerSolutionFound()) {

                final double bestIntegerSolutionValue = IntegerSolver.this.getBestResultSoFar().getValue();

                double nudge = PrimitiveMath.MAX.invoke(PrimitiveMath.ABS.invoke(bestIntegerSolutionValue) * options.mip_gap, options.mip_gap);

                if (nodeModel.isMinimisation()) {
                    final BigDecimal upper = TypeUtils.toBigDecimal(bestIntegerSolutionValue - nudge, options.feasibility);
                    nodeModel.limitObjective(null, upper);
                } else {
                    final BigDecimal lower = TypeUtils.toBigDecimal(bestIntegerSolutionValue + nudge, options.feasibility);
                    nodeModel.limitObjective(lower, null);
                }
            }

            final Boolean retVal = IntegerSolver.this.compute(myKey, nodeModel.prepare(), myPrinter);

            return retVal;
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

    private static volatile ForkJoinPool EXECUTOR;

    public static IntegerSolver make(final ExpressionsBasedModel model) {
        return new IntegerSolver(model, model.options);
    }

    private static ForkJoinPool executor() {

        if (EXECUTOR == null) {

            try {

                /**
                 * This constructor only available with Java 9 and onwards.
                 */
                Constructor<ForkJoinPool> java9constructor = ForkJoinPool.class.getConstructor(int.class, ForkJoinWorkerThreadFactory.class,
                        UncaughtExceptionHandler.class, boolean.class, int.class, int.class, int.class, Predicate.class, long.class, TimeUnit.class);

                /**
                 * parallelism the parallelism level. For default value, use
                 * java.lang.Runtime.availableProcessors.
                 */
                int parallelism = OjAlgoUtils.ENVIRONMENT.threads;
                /**
                 * factory the factory for creating new threads. For default value, use
                 * defaultForkJoinWorkerThreadFactory.
                 */
                ForkJoinWorkerThreadFactory factory = ForkJoinPool.defaultForkJoinWorkerThreadFactory;
                /**
                 * handler the handler for internal worker threads that terminate due to unrecoverable errors
                 * encountered while executing tasks. For default value, use null.
                 */
                UncaughtExceptionHandler handler = null;
                /**
                 * asyncMode if true, establishes local first-in-first-out scheduling mode for forked tasks
                 * that are never joined. This mode may be more appropriate than default locally stack-based
                 * mode in applications in which worker threads only process event-style asynchronous tasks.
                 * For default value, use false.
                 */
                boolean asyncMode = false;
                /**
                 * corePoolSize the number of threads to keep in the pool (unless timed out after an elapsed
                 * keep-alive). Normally (and by default) this is the same value as the parallelism level, but
                 * may be set to a larger value to reduce dynamic overhead if tasks regularly block. Using a
                 * smaller value (for example 0) has the same effect as the default.
                 */
                int corePoolSize = 0;
                /**
                 * maximumPoolSize the maximum number of threads allowed. When the maximum is reached,
                 * attempts to replace blocked threads fail. (However, because creation and termination of
                 * different threads may overlap, and may be managed by the given thread factory, this value
                 * may be transiently exceeded.) To arrange the same value as is used by default for the
                 * common pool, use 256 plus the parallelism level. (By default, the common pool allows a
                 * maximum of 256 spare threads.) Using a value (for example Integer.MAX_VALUE) larger than
                 * the implementation's total thread limit has the same effect as using this limit (which is
                 * the default).
                 */
                int maximumPoolSize = 2 * OjAlgoUtils.ENVIRONMENT.threads;
                /**
                 * minimumRunnable the minimum allowed number of core threads not blocked by a join or
                 * ManagedBlocker. To ensure progress, when too few unblocked threads exist and unexecuted
                 * tasks may exist, new threads are constructed, up to the given maximumPoolSize. For the
                 * default value, use 1, that ensures liveness. A larger value might improve throughput in the
                 * presence of blocked activities, but might not, due to increased overhead. A value of zero
                 * may be acceptable when submitted tasks cannot have dependencies requiring additional
                 * threads.
                 */
                int minimumRunnable = 1;
                /**
                 * saturate if non-null, a predicate invoked upon attempts to create more than the maximum
                 * total allowed threads. By default, when a thread is about to block on a join or
                 * ManagedBlocker, but cannot be replaced because the maximumPoolSize would be exceeded, a
                 * RejectedExecutionException is thrown. But if this predicate returns true, then no exception
                 * is thrown, so the pool continues to operate with fewer than the target number of runnable
                 * threads, which might not ensure progress.
                 */
                Predicate<? super ForkJoinPool> saturate = null;
                /**
                 * keepAliveTime the elapsed time since last use before a thread is terminated (and then later
                 * replaced if needed). For the default value, use 60, TimeUnit.SECONDS.
                 */
                long keepAliveTime = 60;
                /**
                 * unit the time unit for the keepAliveTime argument
                 */
                TimeUnit unit = TimeUnit.SECONDS;

                EXECUTOR = java9constructor.newInstance(parallelism, factory, handler, asyncMode, corePoolSize, maximumPoolSize, minimumRunnable, saturate,
                        keepAliveTime, unit);

            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException exception) {

                EXECUTOR = null;
            }

            if (EXECUTOR == null) {
                EXECUTOR = new ForkJoinPool(OjAlgoUtils.ENVIRONMENT.threads);
            }
        }

        return EXECUTOR;
    }

    static void flush(final PrinterBuffer buffer, final BasicLogger.Printer receiver) {
        if ((buffer != null) && (receiver != null)) {
            buffer.flush(receiver);
        }
    }

    private volatile Optimisation.Result myBestResultSoFar = null;
    private final Queue<NodeKey> myDeferredNodes = new ConcurrentLinkedQueue<>();
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

    protected IntegerSolver(final ExpressionsBasedModel model, final Options solverOptions) {

        super(solverOptions);

        myIntegerModel = model.snapshot();
        myFunction = myIntegerModel.objective().toFunction();

        myMinimisation = myIntegerModel.isMinimisation();

        final List<Variable> integerVariables = myIntegerModel.getIntegerVariables();
        myIntegerIndices = new int[integerVariables.size()];
        for (int i = 0, limit = myIntegerIndices.length; i < limit; i++) {
            myIntegerIndices[i] = myIntegerModel.indexOf(integerVariables.get(i));
        }

        myIntegerSignificances = new double[myIntegerIndices.length];
        Arrays.fill(myIntegerSignificances, ONE);
        final MatrixStore<Double> gradient = this.getGradient(Access1D.asPrimitive1D(model.getVariableValues()));
        final double largest = gradient.aggregateAll(Aggregator.LARGEST);
        if (largest > ZERO) {
            for (int i = 0, limit = myIntegerIndices.length; i < limit; i++) {
                final int globalIndex = myIntegerIndices[i];
                this.addIntegerSignificance(i, gradient.doubleValue(globalIndex) / largest);
            }
        }
    }

    public Result solve(final Result kickStarter) {

        // Must verify that it actually is an integer solution
        // The kickStarter may be user-supplied
        if ((kickStarter != null) && kickStarter.getState().isFeasible() && this.getIntegerModel().validate(kickStarter)) {
            this.markInteger(null, null, kickStarter);
        }

        this.resetIterationsCount();

        final BranchAndBoundNodeTask rootNodeTask = new BranchAndBoundNodeTask();

        boolean normalExit = IntegerSolver.executor().invoke(rootNodeTask).booleanValue();
        while (normalExit && (myDeferredNodes.size() > 0)) {
            NodeKey nodeKey = myDeferredNodes.poll();
            if (this.isGoodEnoughToContinueBranching(nodeKey.objective)) {
                normalExit &= IntegerSolver.executor().invoke(new BranchAndBoundNodeTask(nodeKey)).booleanValue();
            }
        }
        myDeferredNodes.clear();

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

    protected Boolean compute(final NodeKey nodeKey, final ExpressionsBasedModel.Intermediate nodeModel, final PrinterBuffer nodePrinter) {

        if (this.isLogDebug()) {
            nodePrinter.println();
            nodePrinter.println("Branch&Bound Node");
            nodePrinter.println(nodeKey.toString());
            nodePrinter.println(this.toString());
        }

        if (!this.isIterationAllowed()) {
            if (this.isLogDebug()) {
                nodePrinter.println("Reached iterations or time limit - stop!");
                IntegerSolver.flush(nodePrinter, this.getIntegerModel().options.logger_appender);
            }
            return false;
        }

        if (nodeKey.index >= 0) {
            nodeKey.enforceBounds(nodeModel, this.getIntegerIndices());
        }

        final Optimisation.Result bestEstimate = this.getBestEstimate();
        final Optimisation.Result nodeResult = nodeModel.solve(bestEstimate);

        // Increment when/if an iteration was actually performed
        this.incrementIterationsCount();

        if (this.isLogDebug()) {
            nodePrinter.println("Node Result: {}", nodeResult);
        }

        if (nodeResult.getState().isOptimal()) {
            if (this.isLogDebug()) {
                nodePrinter.println("Node solved to optimality!");
            }

            if (options.validate && !nodeModel.validate(nodeResult, nodePrinter)) {
                // This should not be possible. There is a bug somewhere.
                nodePrinter.println("Node solution marked as OPTIMAL, but is actually INVALID/INFEASIBLE/FAILED. Stop this branch!");
                nodePrinter.println("Integer indices: {}", Arrays.toString(this.getIntegerIndices()));
                nodePrinter.println("Lower bounds: {}", Arrays.toString(nodeKey.getLowerBounds()));
                nodePrinter.println("Upper bounds: {}", Arrays.toString(nodeKey.getUpperBounds()));

                IntegerSolver.flush(nodePrinter, this.getIntegerModel().options.logger_appender);

                return false;
            }

            final int branchIntegerIndex = this.identifyNonIntegerVariable(nodeResult, nodeKey);
            final double tmpSolutionValue = this.evaluateFunction(nodeResult);

            if (branchIntegerIndex == -1) {
                if (this.isLogDebug()) {
                    nodePrinter.println("Integer solution! Store it among the others, and stop this branch!");
                }

                final Optimisation.Result tmpIntegerSolutionResult = new Optimisation.Result(Optimisation.State.FEASIBLE, tmpSolutionValue, nodeResult);

                this.markInteger(nodeKey, null, tmpIntegerSolutionResult);

                if (this.isLogDebug()) {
                    nodePrinter.println(this.getBestResultSoFar().toString());
                    BasicLogger.debug();
                    BasicLogger.debug(this.toString());
                    // BasicLogger.debug(DaemonPoolExecutor.INSTANCE.toString());
                    IntegerSolver.flush(nodePrinter, this.getIntegerModel().options.logger_appender);
                }

                nodeModel.dispose();
                return true;

            } else {
                if (this.isLogDebug()) {
                    nodePrinter.println("Not an Integer Solution: " + tmpSolutionValue);
                }

                final double variableValue = nodeResult.doubleValue(this.getGlobalIndex(branchIntegerIndex));

                if (this.isGoodEnoughToContinueBranching(tmpSolutionValue)) {

                    if (this.isLogDebug()) {
                        nodePrinter.println("Still hope, branching on {} @ {} >>> {}", branchIntegerIndex, variableValue,
                                nodeModel.getVariable(this.getGlobalIndex(branchIntegerIndex)));
                        IntegerSolver.flush(nodePrinter, this.getIntegerModel().options.logger_appender);
                    }

                    // this.generateCuts(nodeModel);

                    final NodeKey lowerBranch = nodeKey.createLowerBranch(branchIntegerIndex, variableValue, tmpSolutionValue);
                    final NodeKey upperBranch = nodeKey.createUpperBranch(branchIntegerIndex, variableValue, tmpSolutionValue);

                    final NodeKey nextTask;
                    final BranchAndBoundNodeTask forkedTask;

                    if (upperBranch.displacement <= HALF) {
                        nextTask = upperBranch;
                        if (lowerBranch.displacement < options.mip_defer) {
                            forkedTask = new BranchAndBoundNodeTask(lowerBranch);
                        } else {
                            forkedTask = null;
                            myDeferredNodes.offer(lowerBranch);
                        }
                    } else {
                        nextTask = lowerBranch;
                        if (upperBranch.displacement < options.mip_defer) {
                            forkedTask = new BranchAndBoundNodeTask(upperBranch);
                        } else {
                            forkedTask = null;
                            myDeferredNodes.offer(upperBranch);
                        }
                    }

                    if (forkedTask != null) {

                        forkedTask.fork();

                        return this.compute(nextTask, nodeModel, nodePrinter) && forkedTask.join();

                    } else {

                        return this.compute(nextTask, nodeModel, nodePrinter);
                    }

                } else {
                    if (this.isLogDebug()) {
                        nodePrinter.println("Can't find better integer solutions - stop this branch!");
                        IntegerSolver.flush(nodePrinter, this.getIntegerModel().options.logger_appender);
                    }

                    nodeModel.dispose();
                    return true;
                }
            }

        } else {
            if (this.isLogDebug()) {
                nodePrinter.println("Failed to solve node problem - stop this branch!");
                IntegerSolver.flush(nodePrinter, this.getIntegerModel().options.logger_appender);
            }

            nodeModel.dispose();
            return true;
        }

    }

    protected int countIntegerSolutions() {
        return myIntegerSolutionsCount.intValue();
    }

    @Override
    protected double evaluateFunction(final Access1D<?> solution) {
        if ((myFunction != null) && (solution != null) && (myFunction.arity() == solution.count())) {
            return myFunction.invoke(Access1D.asPrimitive1D(solution));
        } else {
            return Double.NaN;
        }
    }

    @Override
    protected MatrixStore<Double> extractSolution() {
        return Primitive64Store.FACTORY.columns(this.getBestResultSoFar());
    }

    protected Optimisation.Result getBestEstimate() {
        return new Optimisation.Result(Optimisation.State.APPROXIMATE, this.getBestResultSoFar());
    }

    protected Optimisation.Result getBestResultSoFar() {

        final Result currentlyTheBest = myBestResultSoFar;

        if (currentlyTheBest != null) {

            return currentlyTheBest;

        } else {

            final State tmpSate = State.INVALID;
            final double tmpValue = myMinimisation ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
            final MatrixStore<Double> tmpSolution = MatrixStore.PRIMITIVE64.makeZero(this.getIntegerModel().countVariables(), 1).get();

            return new Optimisation.Result(tmpSate, tmpValue, tmpSolution);
        }
    }

    protected MatrixStore<Double> getGradient(final Access1D<Double> solution) {
        return myFunction.getGradient(solution);
    }

    protected ExpressionsBasedModel getIntegerModel() {
        return myIntegerModel;
    }

    protected ExpressionsBasedModel getNodeModel() {
        return myIntegerModel.relax(false);
    }

    protected boolean initialise(final Result kickStarter) {
        return true;
    }

    protected boolean isGoodEnoughToContinueBranching(final double relaxedNodeValue) {

        final Result bestResultSoFar = myBestResultSoFar;

        if ((bestResultSoFar == null) || Double.isNaN(relaxedNodeValue)) {

            return true;

        } else {

            final double bestIntegerValue = bestResultSoFar.getValue();

            final double absoluteGap = PrimitiveMath.ABS.invoke(bestIntegerValue - relaxedNodeValue);
            final double relativeGap = PrimitiveMath.ABS.invoke(absoluteGap / bestIntegerValue);

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

    protected synchronized void markInteger(final NodeKey key, final ExpressionsBasedModel model, final Optimisation.Result result) {

        if (this.isLogProgress()) {
            this.log("New integer solution {}", result);
            this.log("\t@ node {}", key);
        }

        final Optimisation.Result currentlyTheBest = myBestResultSoFar;

        if (currentlyTheBest == null) {

            myBestResultSoFar = result;
            this.setState(Optimisation.State.FEASIBLE);

        } else if (myMinimisation && (result.getValue() < currentlyTheBest.getValue())) {

            myBestResultSoFar = result;

        } else if (!myMinimisation && (result.getValue() > currentlyTheBest.getValue())) {

            myBestResultSoFar = result;

        } else {

            if (this.isLogDebug()) {
                this.log("Previously best {}", myBestResultSoFar);
            }
        }

        if ((currentlyTheBest != null) && options.solution.isDifferent(currentlyTheBest.getValue(), result.getValue())) {
            for (int i = 0, limit = myIntegerIndices.length; i < limit; i++) {
                final int globalIndex = myIntegerIndices[i];
                final double varDiff = result.doubleValue(globalIndex) - currentlyTheBest.doubleValue(globalIndex);
                if (!options.feasibility.isZero(varDiff)) {
                    this.addIntegerSignificance(i, ONE / varDiff);
                }
            }
        }

        myIntegerSolutionsCount.incrementAndGet();
    }

    /**
     * Should validate the solver data/input/structue. Even "expensive" validation can be performed as the
     * method should only be called if {@linkplain org.ojalgo.optimisation.Optimisation.Options#validate} is
     * set to true. In addition to returning true or false the implementation should set the state to either
     * {@linkplain org.ojalgo.optimisation.Optimisation.State#VALID} or
     * {@linkplain org.ojalgo.optimisation.Optimisation.State#INVALID} (or possibly
     * {@linkplain org.ojalgo.optimisation.Optimisation.State#FAILED}). Typically the method should be called
     * at the very beginning of the solve-method.
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
        myIntegerSignificances[index] = PrimitiveMath.HYPOT.invoke(myIntegerSignificances[index], significance);
    }

    int countExploredNodes() {
        // return myExploredNodes.size();
        return 0;
    }

    int getGlobalIndex(final int integerIndex) {
        return myIntegerIndices[integerIndex];
    }

    int[] getIntegerIndices() {
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
    int identifyNonIntegerVariable(final Optimisation.Result nodeResult, final NodeKey nodeKey) {

        int retVal = -1;

        double fraction;
        double compareFraction = ZERO;
        double maxFraction = ZERO;

        for (int i = 0, limit = myIntegerIndices.length; i < limit; i++) {

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
                    //compareFraction = fraction;
                    // [0.5, 1.0)
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
