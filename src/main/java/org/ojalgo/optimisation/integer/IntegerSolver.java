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
package org.ojalgo.optimisation.integer;

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

import org.ojalgo.concurrent.MultiviewSet;
import org.ojalgo.concurrent.ProcessingService;
import org.ojalgo.function.multiary.MultiaryFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.netio.CharacterRing;
import org.ojalgo.netio.CharacterRing.RingLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.GenericSolver;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.CalendarDateDuration;
import org.ojalgo.type.TypeUtils;

public final class IntegerSolver extends GenericSolver {

    public static final class ModelIntegration extends ExpressionsBasedModel.Integration<IntegerSolver> {

        @Override
        public IntegerSolver build(final ExpressionsBasedModel model) {

            IntegerSolver solver = IntegerSolver.make(model);

            if (model.options.validate) {
                solver.setValidator(this.newValidator(model));
            }

            return solver;
        }

        @Override
        public boolean isCapable(final ExpressionsBasedModel model) {
            return !model.isAnyConstraintQuadratic();
        }

    }

    /**
     * When a node is determined to be a leaf - no further branching - what was the reason?
     *
     * @author apete
     */
    static final class NodeStatistics {

        private final LongAdder myAbandoned = new LongAdder();
        private final LongAdder myExhausted = new LongAdder();
        private final LongAdder myInfeasible = new LongAdder();
        private final LongAdder myInteger = new LongAdder();

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("NodeStatistics [I=");
            builder.append(myInteger);
            builder.append(", E=");
            builder.append(myExhausted);
            builder.append(", S=");
            builder.append(myInfeasible);
            builder.append(", A=");
            builder.append(myAbandoned);
            builder.append("]");
            return builder.toString();
        }

        /**
         * Node was created, and deferred, but then abandoned and never evaluated (sub/node problem never
         * solved).
         */
        boolean abandoned() {
            myAbandoned.increment();
            return true;
        }

        long countEvaluatedNodes() {
            return myInteger.longValue() + myInfeasible.longValue() + myExhausted.longValue();
        }

        int countIntegerSolutions() {
            return myInteger.intValue();
        }

        long countSkippedNodes() {
            return myAbandoned.longValue();
        }

        long countTotalNodes() {
            return this.countEvaluatedNodes() + this.countSkippedNodes();
        }

        /**
         * Node evaluated, and solution not integer, but estimate NOT possible to find better integer
         * solution.
         */
        boolean exhausted() {
            myExhausted.increment();
            return true;
        }

        /**
         * Failed to solve node problem because of some unexpected error – not because the node was
         * infeasible.
         */
        boolean failed() {
            return false;
        }

        /**
         * Node problem infeasible
         */
        boolean infeasible() {
            myInfeasible.increment();
            return true;
        }

        /**
         * Integer solution found
         */
        boolean integer() {
            myInteger.increment();
            return true;
        }

    }

    public static final ModelIntegration INTEGRATION = new ModelIntegration();

    public static IntegerSolver make(final ExpressionsBasedModel model) {
        return new IntegerSolver(model);
    }

    static void flush(final RingLogger buffer, final BasicLogger receiver) {
        if (buffer != null && receiver != null) {
            buffer.flush(receiver);
        }
    }

    private volatile Optimisation.Result myBestResultSoFar = null;
    private final MultiviewSet<NodeKey> myDeferredNodes = new MultiviewSet<>();
    private final MultiaryFunction.TwiceDifferentiable<Double> myFunction;
    private final ExpressionsBasedModel myIntegerModel;
    private final boolean myMinimisation;
    private final NodeStatistics myNodeStatistics = new NodeStatistics();

    IntegerSolver(final ExpressionsBasedModel model) {

        super(model.options);

        myIntegerModel = model.simplify();
        myFunction = myIntegerModel.limitObjective(null, null).toFunction();

        myMinimisation = myIntegerModel.getOptimisationSense() == Optimisation.Sense.MIN;
    }

    @Override
    public Result solve(final Result kickStarter) {

        Result point = kickStarter != null ? kickStarter : myIntegerModel.getVariableValues();

        ModelStrategy strategy = options.integer().newModelStrategy(myIntegerModel).initialise(myFunction, point);

        if (point != null && point.getState().isFeasible() && myIntegerModel.validate(point)) {
            // Must verify that it actually is an integer solution
            // The kickStarter may be user-supplied
            this.markInteger(null, point, strategy);
        }

        this.resetIterationsCount();

        ExpressionsBasedModel cutModel = myIntegerModel.snapshot();
        NodeSolver cutSolver = cutModel.prepare(NodeSolver::new);
        Result cutResult = cutSolver.solve();
        cutSolver.generateCuts(strategy, myIntegerModel);

        NodeKey rootNode = new NodeKey(myIntegerModel);
        ExpressionsBasedModel rootModel = myIntegerModel.snapshot();
        rootNode.setNodeState(rootModel, strategy);

        RingLogger rootPrinter = this.newPrinter();

        AtomicBoolean solverNormalExit = new AtomicBoolean(this.compute(rootNode, rootModel.prepare(NodeSolver::new), rootPrinter, strategy));
        rootNode.dispose();

        Map<Comparator<NodeKey>, MultiviewSet<NodeKey>.PrioritisedView> views = new ConcurrentHashMap<>();

        ProcessingService.INSTANCE.process(strategy.getWorkerPriorities(), workerStrategy -> {

            boolean workerNormalExit = solverNormalExit.get();

            MultiviewSet<NodeKey>.PrioritisedView view = views.computeIfAbsent(workerStrategy, myDeferredNodes::newView);

            RingLogger nodePrinter = this.newPrinter();

            NodeKey node = null;
            while (workerNormalExit && solverNormalExit.get() && !myDeferredNodes.isEmpty()) {
                if ((node = view.poll()) != null) {

                    if (!this.isIterationAllowed()) {
                        workerNormalExit = false;
                    } else if (!strategy.isGoodEnough(myBestResultSoFar, node.objective)) {
                        workerNormalExit = myNodeStatistics.abandoned();
                    } else {
                        ExpressionsBasedModel nodeModel = myIntegerModel.snapshot();
                        node.setNodeState(nodeModel, strategy);
                        NodeSolver nodeSolver = nodeModel.prepare(NodeSolver::new);
                        workerNormalExit &= this.compute(node, nodeSolver, nodePrinter, strategy);
                    }

                    node.dispose();
                }

                if (!workerNormalExit) {
                    solverNormalExit.set(workerNormalExit);
                }
            }
        });

        views.clear();
        myDeferredNodes.clear();

        if (this.isLogProgress()) {
            this.logProgress(this.countIterations(), this.getClassSimpleName(), this.getDuration());
        }

        Optimisation.Result bestSolutionFound = this.getBestResultSoFar();

        if (bestSolutionFound.getState().isFeasible()) {
            if (solverNormalExit.get()) {
                return bestSolutionFound.withState(State.OPTIMAL);
            } else {
                return bestSolutionFound.withState(State.FEASIBLE);
            }
        } else if (solverNormalExit.get()) {
            return bestSolutionFound.withState(State.INFEASIBLE);
        } else {
            return bestSolutionFound.withState(State.FAILED);
        }
    }

    @Override
    public String toString() {
        return TypeUtils.format("Solutions={} Nodes/Iterations={} {}", myNodeStatistics.countIntegerSolutions(), this.countIterations(),
                this.getBestResultSoFar());
    }

    private RingLogger newPrinter() {
        return options.validate || this.isLogProgress() ? CharacterRing.newRingLogger() : null;
    }

    protected Optimisation.Result buildResult() {

        Access1D<?> solution = this.extractSolution();
        double value = this.evaluateFunction(solution);
        Optimisation.State state = this.getState();

        return new Optimisation.Result(state, value, solution);
    }

    protected double evaluateFunction(final Access1D<?> solution) {
        if (myFunction != null && solution != null && myFunction.arity() == solution.count()) {
            return myFunction.invoke(Access1D.asPrimitive1D(solution)).doubleValue();
        }
        return Double.NaN;
    }

    protected MatrixStore<Double> extractSolution() {
        return Primitive64Store.FACTORY.columns(this.getBestResultSoFar());
    }

    protected Optimisation.Result getBestEstimate() {
        return new Optimisation.Result(Optimisation.State.APPROXIMATE, this.getBestResultSoFar());
    }

    protected Optimisation.Result getBestResultSoFar() {

        Result currentlyTheBest = myBestResultSoFar;

        if (currentlyTheBest != null) {
            return currentlyTheBest;
        }

        State tmpSate = State.INVALID;
        double tmpValue = myMinimisation ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        MatrixStore<Double> tmpSolution = Primitive64Store.FACTORY.makeZero(myIntegerModel.countVariables(), 1);

        return new Optimisation.Result(tmpSate, tmpValue, tmpSolution);
    }

    protected boolean isIterationNecessary() {

        if (myBestResultSoFar == null) {
            return true;
        }

        return this.countTime() < options.time_suffice && this.countIterations() < options.iterations_suffice;
    }

    @Override
    protected void logProgress(final int iterationsDone, final String classSimpleName, final CalendarDateDuration duration) {
        this.log("Done {} {} iterations in {} with {}", iterationsDone, classSimpleName, duration, myNodeStatistics);
    }

    protected synchronized void markInteger(final NodeKey key, final Optimisation.Result result, final ModelStrategy strategy) {

        if (this.isLogProgress()) {

            double low = Double.NEGATIVE_INFINITY;
            double high = Double.POSITIVE_INFINITY;

            if (key != null) {
                if (myMinimisation) {
                    low = Math.max(low, key.objective);
                } else {
                    high = Math.min(high, key.objective);
                }
            }

            if (myBestResultSoFar != null) {
                if (myMinimisation) {
                    high = Math.min(high, myBestResultSoFar.getValue());
                } else {
                    low = Math.max(low, myBestResultSoFar.getValue());
                }
            }

            this.log("[{}, {}] -> {}", low, high, result.toString());
            if (key != null) {
                this.log("\t @ {}", key);
            }
        }

        Optimisation.Result previouslyTheBest = myBestResultSoFar;

        if (previouslyTheBest == null) {
            myBestResultSoFar = result;
            this.setState(Optimisation.State.FEASIBLE);
        } else if (myMinimisation ? result.getValue() < previouslyTheBest.getValue() : result.getValue() > previouslyTheBest.getValue()) {
            myBestResultSoFar = result;
        }

        strategy.markInteger(key, result);

        BigDecimal bestIntegerSolutionValue = BigDecimal.valueOf(myBestResultSoFar.getValue());

        if (myIntegerModel.getOptimisationSense() != Optimisation.Sense.MAX) {
            myIntegerModel.limitObjective(null, bestIntegerSolutionValue);
        } else {
            myIntegerModel.limitObjective(bestIntegerSolutionValue, null);
        }
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

            if (!(retVal = myIntegerModel.validate())) {
                retVal = false;
                this.setState(State.INVALID);
            }

        } catch (Exception cause) {

            retVal = false;
            this.setState(State.FAILED);
        }

        return retVal;
    }

    boolean compute(final NodeKey nodeKey, final NodeSolver nodeSolver, final RingLogger nodePrinter, final ModelStrategy strategy) {

        if (this.isLogDebug()) {
            nodePrinter.println();
            nodePrinter.println("Branch&Bound Node");
            nodePrinter.println(nodeKey.toString());
            nodePrinter.println(this.toString());
        }

        if (nodeKey.index >= 0) {
            nodeKey.enforceBounds(nodeSolver, strategy);
        }

        Optimisation.Result bestEstimate = this.getBestEstimate();
        Optimisation.Result nodeResult = nodeSolver.solve(bestEstimate);

        // Increment when/if an iteration was actually performed
        this.incrementIterationsCount();

        if (this.isLogDebug()) {
            nodePrinter.println("Node Result: {}", nodeResult);
        }

        if (!nodeResult.getState().isOptimal()) {
            if (this.isLogDebug()) {
                nodePrinter.println("Failed to solve node problem - stop this branch!");
                IntegerSolver.flush(nodePrinter, myIntegerModel.options.logger_appender);
            }

            nodeSolver.dispose();
            if (nodeKey.sequence == 0 && (nodeResult.getState().isUnexplored() || !nodeResult.getState().isValid())) {
                // return false;
                return myNodeStatistics.failed();
            }
            // return true;
            strategy.markInfeasible(nodeKey, myBestResultSoFar != null);
            return myNodeStatistics.infeasible();
        }

        if (this.isLogDebug()) {
            nodePrinter.println("Node solved to optimality!");
        }

        if (options.validate && !nodeSolver.validate(nodeResult, nodePrinter)) {
            // This should not be possible. There is a bug somewhere.
            nodePrinter.println("Node solution marked as OPTIMAL, but is actually INVALID/INFEASIBLE/FAILED. Stop this branch!");
            nodePrinter.println("Integer indices: {}", strategy);
            nodePrinter.println("Lower bounds: {}", Arrays.toString(nodeKey.copyLowerBounds()));
            nodePrinter.println("Upper bounds: {}", Arrays.toString(nodeKey.copyUpperBounds()));

            IntegerSolver.flush(nodePrinter, myIntegerModel.options.logger_appender);

            // return false;
            return myNodeStatistics.failed();
        }

        int branchIntegerIndex = this.identifyNonIntegerVariable(nodeResult, nodeKey, strategy);
        double tmpSolutionValue = this.evaluateFunction(nodeResult);

        if (branchIntegerIndex == -1) {
            if (this.isLogDebug()) {
                nodePrinter.println("Integer solution! Store it among the others, and stop this branch!");
            }

            Optimisation.Result tmpIntegerSolutionResult = new Optimisation.Result(Optimisation.State.FEASIBLE, tmpSolutionValue, nodeResult);

            this.markInteger(nodeKey, tmpIntegerSolutionResult, strategy);

            if (this.isLogDebug()) {
                nodePrinter.println(this.getBestResultSoFar().toString());
                BasicLogger.debug();
                BasicLogger.debug(this.toString());
                // BasicLogger.debug(DaemonPoolExecutor.INSTANCE.toString());
                IntegerSolver.flush(nodePrinter, myIntegerModel.options.logger_appender);
            }

            nodeSolver.dispose();
            return myNodeStatistics.integer();

        }
        if (this.isLogDebug()) {
            nodePrinter.println("Not an Integer Solution: " + tmpSolutionValue);
        }

        double variableValue = nodeResult.doubleValue(strategy.getIndex(branchIntegerIndex));

        if (!strategy.isGoodEnough(myBestResultSoFar, tmpSolutionValue)) {
            if (this.isLogDebug()) {
                nodePrinter.println("Can't find better integer solutions - stop this branch!");
                IntegerSolver.flush(nodePrinter, myIntegerModel.options.logger_appender);
            }

            nodeSolver.dispose();
            // return true;
            return myNodeStatistics.exhausted();
        }
        if (this.isLogDebug()) {
            nodePrinter.println("Still hope, branching on {} @ {} >>> {}", branchIntegerIndex, variableValue,
                    nodeSolver.getVariable(strategy.getIndex(branchIntegerIndex)));
            IntegerSolver.flush(nodePrinter, myIntegerModel.options.logger_appender);
        }

        if (strategy.cutting && nodeKey.sequence % 10L == 0L) {
            double displacement = nodeKey.getMinimumDisplacement(branchIntegerIndex, variableValue);
            if (strategy.isCutRatherThanBranch(displacement, myBestResultSoFar != null)) {
                if (nodeSolver.generateCuts(strategy)) {
                    return this.compute(nodeKey, nodeSolver, nodePrinter, strategy);
                }
                strategy.cutting = false;
            }
        }

        NodeKey lowerBranch = nodeKey.createLowerBranch(branchIntegerIndex, variableValue, tmpSolutionValue);
        NodeKey upperBranch = nodeKey.createUpperBranch(branchIntegerIndex, variableValue, tmpSolutionValue);

        if (!strategy.isDirect(lowerBranch, myBestResultSoFar != null)) {
            myDeferredNodes.add(lowerBranch);
            lowerBranch = null;
        }
        if (lowerBranch != null || !strategy.isDirect(upperBranch, myBestResultSoFar != null)) {
            myDeferredNodes.add(upperBranch);
            upperBranch = null;
        }

        if (lowerBranch != null && upperBranch != null) {
            // Node model data is reused when continuing down in the same thread.
            // Can't do 2 branches in the same thread, using the same parent model.
            throw new IllegalStateException();
        }

        boolean retVal = true;
        if (lowerBranch != null) {
            retVal = retVal && this.compute(lowerBranch, nodeSolver, nodePrinter, strategy);
        }
        if (upperBranch != null) {
            retVal = retVal && this.compute(upperBranch, nodeSolver, nodePrinter, strategy);
        }
        return retVal;
    }

    /**
     * Should return the index of the (best) integer variable to branch on. Returning a negative index means
     * an integer solution has been found (no further branching). Does NOT return a global variable index -
     * it's the index among the ineteger variable.
     */
    int identifyNonIntegerVariable(final Optimisation.Result nodeResult, final NodeKey nodeKey, final ModelStrategy strategy) {

        int retVal = -1;

        double displacement;
        double comparableDisplacement = ZERO;
        double maxComparable = ZERO;

        for (int i = 0, limit = strategy.countIntegerVariables(); i < limit; i++) {

            int globalIndex = strategy.getIndex(i);

            displacement = nodeKey.getMinimumDisplacement(i, nodeResult.doubleValue(globalIndex));
            // [0, 0.5]

            if (!strategy.getIntegralityTolerance().isZero(displacement)) {
                // This variable not integer

                comparableDisplacement = strategy.toComparable(i, displacement, myBestResultSoFar != null);

                if (comparableDisplacement > maxComparable) {
                    retVal = i;
                    maxComparable = comparableDisplacement;
                }
            }
        }

        return retVal;
    }

}
