/*
 * Copyright 1997-2025 Optimatika
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

import org.ojalgo.concurrent.MultiviewSet;
import org.ojalgo.concurrent.ProcessingService;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.netio.CharacterRing;
import org.ojalgo.netio.CharacterRing.RingLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.GenericSolver;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.CalendarDateDuration;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

public final class IntegerSolver extends GenericSolver {

    public static final class ModelIntegration extends ExpressionsBasedModel.Integration<IntegerSolver> {

        @Override
        public IntegerSolver build(final ExpressionsBasedModel model) {
            return IntegerSolver.make(model);
        }

        @Override
        public boolean isCapable(final ExpressionsBasedModel model) {
            return !model.isAnyConstraintQuadratic();
        }

        @Override
        protected Optimisation.Sense getSolverSense() {
            return null;
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

    private static final int STRONG_BRANCH_CANDIDATES = 16;
    private static final double STRONG_BRANCH_INFEASIBLE_PENALTY = 10.0;

    public static IntegerSolver make(final ExpressionsBasedModel model) {
        return new IntegerSolver(model);
    }

    public static IntegerSolver newSolver(final ExpressionsBasedModel model) {
        return INTEGRATION.build(model);
    }

    /**
     * {@code ceil(v)} bumped to {@code floor+1} for borderline values like {@code 1.0 - 1e-15} where
     * {@code Math.floor} and {@code Math.ceil} return the same integer.
     */
    private static long branchCeil(final double v, final long floor) {
        long c = (long) Math.ceil(v);
        return c == floor ? floor + 1 : c;
    }

    private static long branchFloor(final double v) {
        return (long) Math.floor(v);
    }

    static void flush(final RingLogger buffer, final BasicLogger receiver) {
        if (buffer != null && receiver != null) {
            buffer.flush(receiver);
        }
    }

    private volatile Optimisation.Result myBestResultSoFar = null;
    /**
     * Set during solve() for logProgress to read; null otherwise.
     */
    private MultiviewSet<NodeKey>.PrioritisedView myBoundView = null;
    private final MultiviewSet<NodeKey> myDeferredNodes = new MultiviewSet<>();
    /**
     * Gap tolerance cached during solve() for logProgress; null otherwise.
     */
    private final NumberContext myGapTolerance;
    private final ExpressionsBasedModel myIntegerModel;
    private final NodeStatistics myNodeStatistics = new NodeStatistics();
    private volatile boolean myOptimalityProven = false;
    private final Optimisation.Sense mySense;
    private final ModelStrategy myStrategy;

    IntegerSolver(final ExpressionsBasedModel model) {

        super(model.options);

        myIntegerModel = model.simplify();
        mySense = myIntegerModel.getOptimisationSense();
        myStrategy = options.integer().newModelStrategy(myIntegerModel);
        myGapTolerance = myStrategy.getGapTolerance();
    }

    @Override
    public Result solve(final Result kickStarter) {

        Result point = kickStarter != null ? kickStarter : myIntegerModel.getVariableValues();

        myStrategy.initialise();

        // Pre-warm the "objective as constraint" expression on myIntegerModel before workers spawn.
        // limitObjective(...) is called concurrently from compute() during search (for bound-objective
        // pruning); the first such call lazily mutates myExpressions, which races with concurrent
        // model.validate() iterations across other workers. Forcing creation here, single-threaded,
        // makes those later calls pure updates and removes the race.
        myIntegerModel.limitObjective(null, null);

        if (point != null && point.getState().isFeasible() && myIntegerModel.validate(point)) {
            // Must verify that it actually is an integer solution
            // The kickStarter may be user-supplied
            this.markInteger(null, point, myStrategy);
        }

        this.resetIterationsCount();

        NodeKey rootNode = new NodeKey(myIntegerModel);
        ExpressionsBasedModel rootModel = myIntegerModel.snapshot();
        rootNode.setNodeState(rootModel, myStrategy);

        RingLogger rootPrinter = this.newPrinter();

        NodeSolver rootSolver = rootModel.prepare(mySense, NodeSolver::new);
        AtomicBoolean solverNormalExit = new AtomicBoolean(this.processRoot(rootNode, rootSolver, rootPrinter));
        rootNode.dispose();

        Map<Comparator<NodeKey>, MultiviewSet<NodeKey>.PrioritisedView> views = new ConcurrentHashMap<>();

        List<Comparator<NodeKey>> workerPriorities = myStrategy.getWorkerPriorities();
        for (Comparator<NodeKey> workerPriority : workerPriorities) {
            MultiviewSet<NodeKey>.PrioritisedView view = myDeferredNodes.newView(workerPriority);
            views.put(workerPriority, view);
            if (mySense == Optimisation.Sense.MIN && workerPriority == NodeKey.MIN_OBJECTIVE) {
                myBoundView = view;
            } else if (mySense == Optimisation.Sense.MAX && workerPriority == NodeKey.MAX_OBJECTIVE) {
                myBoundView = view;
            }
        }

        ProcessingService.INSTANCE.process(workerPriorities, workerPriority -> {

            boolean workerNormalExit = solverNormalExit.get();

            MultiviewSet<NodeKey>.PrioritisedView view = views.computeIfAbsent(workerPriority, myDeferredNodes::newView);

            RingLogger nodePrinter = this.newPrinter();

            NodeKey node = null;
            while (workerNormalExit && solverNormalExit.get() && !myOptimalityProven && !myDeferredNodes.isEmpty()) {
                if ((node = view.poll()) != null) {

                    if (!this.isIterationAllowed()) {
                        workerNormalExit = false;
                    } else if (this.isOptimalityProven()) {
                        myOptimalityProven = true;
                    } else if (!myStrategy.isGoodEnough(myBestResultSoFar, node.objective)) {
                        workerNormalExit = myNodeStatistics.abandoned();
                    } else {
                        ExpressionsBasedModel nodeModel = myIntegerModel.snapshot();
                        node.setNodeState(nodeModel, myStrategy);
                        NodeSolver nodeSolver = nodeModel.prepare(mySense, NodeSolver::new);
                        workerNormalExit &= this.compute(node, nodeSolver, nodePrinter, myStrategy);
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
            if (myOptimalityProven) {
                this.log("Incumbent proven optimal within gap tolerance - stopped early.");
            }
            this.logProgress(this.countIterations(), this.getClassSimpleName(), this.getDuration());
        }

        myBoundView = null;

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

    /**
     * If {@code probeResult} happens to be integer-feasible (one bound tightening was enough to round the
     * relaxation to an integer point) and valid for the original model, accept it as a free incumbent. No-op
     * otherwise.
     */
    private void acceptIfIntegerFeasible(final Optimisation.Result probeResult, final double probeValue, final NodeKey rootNode) {
        if (this.identifyNonIntegerVariable(probeResult, rootNode, myStrategy) != -1) {
            return;
        }
        if (!myIntegerModel.validate(probeResult)) {
            return;
        }
        Optimisation.Result integerResult = new Optimisation.Result(Optimisation.State.FEASIBLE, probeValue, probeResult);
        this.markInteger(rootNode, integerResult, myStrategy);
    }

    /**
     * Dedicated root cut loop — runs GMI cut generation multiple times before branching begins. Bypasses the
     * full compute() path (pseudo-cost updates, reduced-cost fixing, variable selection) that is unnecessary
     * at this stage. Stops on: no cuts generated, LP infeasibility, or tailing-off.
     */
    private void generateRootCuts(final NodeSolver rootSolver, Optimisation.Result rootResult) {

        if (rootResult == null || !rootResult.getState().isOptimal()) {
            return;
        }

        if (myStrategy.getGMICutConfiguration() == null) {
            return;
        }

        int maxRounds = 10;
        double previousObjective = rootResult.getValue();

        for (int round = 0; round < maxRounds; round++) {

            if (!rootSolver.generateCuts(myStrategy)) {
                break;
            }

            rootResult = rootSolver.solve(this.getBestEstimate());

            if (rootResult == null || !rootResult.getState().isOptimal()) {
                break;
            }

            double currentObjective = rootResult.getValue();
            double improvement = Math.abs(currentObjective - previousObjective);
            if (improvement < 1E-6 * (ONE + Math.abs(currentObjective))) {
                break;
            }
            previousObjective = currentObjective;
        }
    }

    /**
     * Valid bound on the optimal integer objective: best relaxation bound over all open subtrees (deferred
     * frontier head + nodes currently checked out by workers). A non-finite contribution could hide an
     * arbitrarily good solution, so it collapses the bound to the pessimistic value — we never declare
     * optimality on incomplete information.
     */
    private double globalDualBound(final MultiviewSet<NodeKey>.PrioritisedView boundView) {

        double pessimistic = mySense == Optimisation.Sense.MIN ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        double bound = mySense == Optimisation.Sense.MIN ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;

        NodeKey head = boundView.peek();
        if (head != null) {
            if (!Double.isFinite(head.objective)) {
                return pessimistic;
            }
            bound = mySense == Optimisation.Sense.MIN ? Math.min(bound, head.objective) : Math.max(bound, head.objective);
        }

        return bound;
    }

    private boolean isOptimalityProven() {

        Optimisation.Result incumbent = myBestResultSoFar;
        if (incumbent == null) {
            return false;
        }

        double bound = this.globalDualBound(myBoundView);
        if (!Double.isFinite(bound)) {
            return false;
        }

        double incumbentValue = incumbent.getValue();
        double gap = Math.max(ZERO, mySense == Optimisation.Sense.MIN ? incumbentValue - bound : bound - incumbentValue);

        return gap <= myGapTolerance.error(incumbentValue);
    }

    private RingLogger newPrinter() {
        return options.validate || this.isLogProgress() ? CharacterRing.newRingLogger() : null;
    }

    /**
     * Process the root node before workers start. Solves the root LP, runs the strong-branching probe pass to
     * seed pseudo-costs, then delegates to {@link #compute} which owns the rest of the per-node flow
     * (validate, {@link ModelStrategy#onNodeSolved}, {@link #identifyNonIntegerVariable},
     * {@link ModelStrategy#isGoodEnough}, root cut generation via {@code depth == 0}, and the branching
     * plunge that keeps {@code rootSolver} warm).
     * <p>
     * Costs one extra warm LP solve at the root (the upcoming {@code compute} re-solves), in exchange for
     * keeping {@code compute} the single owner of the B&B flow. The probe pass uses the LP result solved
     * here; the {@code rootSolver}'s basis is restored to the root LP before handing off.
     */
    private boolean processRoot(final NodeKey rootNode, final NodeSolver rootSolver, final RingLogger rootPrinter) {

        Optimisation.Result rootResult = rootSolver.solve(this.getBestEstimate());

        if (rootResult.getState().isOptimal() && rootSolver.isInPlaceBoundUpdateSafe()) {

            double rootValue = rootResult.getValue();

            int nbIntegers = myStrategy.countIntegerVariables();
            int[] candidates = new int[nbIntegers];
            double[] fract = new double[nbIntegers];
            int count = 0;
            for (int i = 0; i < nbIntegers; i++) {
                double v = rootResult.doubleValue(myStrategy.getIndex(i));
                double dn = v - Math.floor(v);
                double disp = Math.min(dn, ONE - dn);
                if (!myStrategy.getIntegralityTolerance().isZero(disp)) {
                    candidates[count] = i;
                    fract[count] = disp;
                    count++;
                }
            }

            if (count >= 2 * STRONG_BRANCH_CANDIDATES) {

                // Partial selection sort: pull the K largest fractionalities to the front.
                int K = Math.min(count, STRONG_BRANCH_CANDIDATES);
                for (int k = 0; k < K; k++) {
                    int best = k;
                    for (int j = k + 1; j < count; j++) {
                        if (fract[j] > fract[best]) {
                            best = j;
                        }
                    }
                    if (best != k) {
                        int ti = candidates[k];
                        candidates[k] = candidates[best];
                        candidates[best] = ti;
                        double tf = fract[k];
                        fract[k] = fract[best];
                        fract[best] = tf;
                    }
                }

                for (int k = 0; k < K; k++) {
                    int ii = candidates[k];
                    int gi = myStrategy.getIndex(ii);
                    double v = rootResult.doubleValue(gi);

                    int origLower = rootNode.getLowerBound(ii);
                    int origUpper = rootNode.getUpperBound(ii);

                    long floorVal = IntegerSolver.branchFloor(v);
                    long ceilVal = IntegerSolver.branchCeil(v, floorVal);
                    double downDisp = Math.max(v - floorVal, NodeKey.MINIMUM_DISPLACEMENT);
                    double upDisp = Math.max(ceilVal - v, NodeKey.MINIMUM_DISPLACEMENT);

                    // Down probe: tighten upper to floor(v)
                    rootSolver.update(gi, origLower, floorVal);
                    Optimisation.Result downResult = rootSolver.solve(null);
                    boolean downOK = downResult.getState().isOptimal();
                    if (downOK) {
                        double downLP = downResult.getValue();
                        double deg = Math.max(ZERO, mySense == Optimisation.Sense.MIN ? downLP - rootValue : rootValue - downLP);
                        myStrategy.observeBranch(ii, false, Math.max(deg, NodeKey.MINIMUM_DISPLACEMENT) / downDisp);
                        this.acceptIfIntegerFeasible(downResult, downLP, rootNode);
                    } else {
                        myStrategy.observeBranch(ii, false, STRONG_BRANCH_INFEASIBLE_PENALTY / downDisp);
                    }

                    // Restore, then up probe: tighten lower to ceil(v)
                    rootSolver.update(gi, origLower, origUpper);
                    rootSolver.update(gi, ceilVal, origUpper);
                    Optimisation.Result upResult = rootSolver.solve(null);
                    boolean upOK = upResult.getState().isOptimal();
                    if (upOK) {
                        double upLP = upResult.getValue();
                        double deg = Math.max(ZERO, mySense == Optimisation.Sense.MIN ? upLP - rootValue : rootValue - upLP);
                        myStrategy.observeBranch(ii, true, Math.max(deg, NodeKey.MINIMUM_DISPLACEMENT) / upDisp);
                        this.acceptIfIntegerFeasible(upResult, upLP, rootNode);
                    } else {
                        myStrategy.observeBranch(ii, true, STRONG_BRANCH_INFEASIBLE_PENALTY / upDisp);
                    }

                    // Restore root bounds in the solver
                    rootSolver.update(gi, origLower, origUpper);

                    if (!downOK && !upOK) {
                        // Both directions of x_gi are LP-infeasible at the root: no integer
                        // value of x_gi fits the root LP region, so the root is provably
                        // integer-infeasible.
                        rootSolver.dispose();
                        return myNodeStatistics.infeasible();
                    }

                    // Single-direction probing fixing: an infeasible probe in one direction
                    // proves the opposite is required for any integer solution at the root.
                    // Apply permanently to rootNode and rootSolver - subsequent probes in this
                    // loop, and the upcoming compute(rootNode, ...) plunge, will see the tighter
                    // root LP region.
                    if (!downOK) {
                        rootNode.tightenLower(ii, (int) ceilVal);
                        rootSolver.update(gi, ceilVal, origUpper);
                    } else if (!upOK) {
                        rootNode.tightenUpper(ii, (int) floorVal);
                        rootSolver.update(gi, origLower, floorVal);
                    }
                }

                // Re-establish the root LP basis so the upcoming compute() call warm-starts cleanly.
                rootResult = rootSolver.solve(null);
            }
        }

        this.generateRootCuts(rootSolver, rootResult);

        return this.compute(rootNode, rootSolver, rootPrinter, myStrategy);
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
        double tmpValue = mySense == Optimisation.Sense.MIN ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        MatrixStore<Double> tmpSolution = R064Store.FACTORY.makeZero(myIntegerModel.countVariables(), 1);

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

        if (myBoundView != null) {
            double bound = this.globalDualBound(myBoundView);
            if (myBestResultSoFar != null && Double.isFinite(bound)) {
                double inc = myBestResultSoFar.getValue();
                double absGap = Math.max(ZERO, mySense == Optimisation.Sense.MIN ? inc - bound : bound - inc);
                double relGap = Math.abs(inc) > ZERO ? absGap / Math.abs(inc) : absGap;
                this.log("\tincumbent={}, dualBound={}, absGap={}, relGap={}, tol={}", inc, bound, absGap, relGap, myGapTolerance.error(inc));
            } else {
                this.log("\tincumbent={}, dualBound={} (not closable yet)", myBestResultSoFar != null ? myBestResultSoFar.getValue() : "none",
                        Double.isFinite(bound) ? bound : "n/a");
            }
        }
    }

    protected synchronized void markInteger(final NodeKey key, final Optimisation.Result result, final ModelStrategy strategy) {

        if (this.isLogProgress()) {

            double low = Double.NEGATIVE_INFINITY;
            double high = Double.POSITIVE_INFINITY;

            if (key != null) {
                if (mySense == Optimisation.Sense.MIN) {
                    low = Math.max(low, key.objective);
                } else {
                    high = Math.min(high, key.objective);
                }
            }

            if (myBestResultSoFar != null) {
                if (mySense == Optimisation.Sense.MIN) {
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
            state = Optimisation.State.FEASIBLE;
        } else if (mySense == Optimisation.Sense.MIN ? result.getValue() < previouslyTheBest.getValue() : result.getValue() > previouslyTheBest.getValue()) {
            myBestResultSoFar = result;
        }

        strategy.markInteger(key, result);

        double bestValue = myBestResultSoFar.getValue();

        // Strict-improvement cutoff: any subsequent incumbent must improve by at least one ULP.
        // Tying the cutoff to the gap tolerance (incumbent ± myGapTolerance.error(incumbent)) was
        // observed to clamp branching-node LP relaxations near the incumbent, which then made
        // isOptimalityProven fire by construction rather than via genuine bound convergence. Gap-
        // aware early-stop is left entirely to isOptimalityProven; the cutoff just enforces
        // strict-improvement-via-LP-infeasibility. (No-op for QP MIPs - limitObjective doesn't
        // install constraints for quadratic objectives.)
        double nudge = Math.max(Math.ulp(bestValue), 1.0e-12);

        if (mySense != Optimisation.Sense.MAX) {
            myIntegerModel.limitObjective(null, BigDecimal.valueOf(bestValue - nudge));
        } else {
            myIntegerModel.limitObjective(BigDecimal.valueOf(bestValue + nudge), null);
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
        state = State.VALID;

        try {

            if (!(retVal = myIntegerModel.validate())) {
                retVal = false;
                state = State.INVALID;
            }

        } catch (Exception cause) {

            retVal = false;
            state = State.FAILED;
        }

        return retVal;
    }

    boolean compute(final NodeKey nodeKey, final NodeSolver nodeSolver, final RingLogger nodePrinter, final ModelStrategy strategy) {

        if (myOptimalityProven) {
            nodeSolver.dispose();
            return myNodeStatistics.abandoned();
        }

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
            double incumbentValue = myBestResultSoFar != null ? myBestResultSoFar.getValue() : Double.NaN;
            strategy.markInfeasible(nodeKey, myBestResultSoFar != null, incumbentValue);
            return myNodeStatistics.infeasible();
        }

        if (this.isLogDebug()) {
            nodePrinter.println("Node solved to optimality!");
        }

        if (options.validate && !nodeSolver.validate(nodeResult, nodePrinter)) {
            // This should not be possible. There is a bug somewhere.
            if (nodePrinter != null) {
                nodePrinter.println("Node solution marked as OPTIMAL, but is actually INVALID/INFEASIBLE/FAILED. Stop this branch!");
                nodePrinter.println("Integer indices: {}", strategy);
                nodePrinter.println("Lower bounds: {}", Arrays.toString(nodeKey.copyLowerBounds()));
                nodePrinter.println("Upper bounds: {}", Arrays.toString(nodeKey.copyUpperBounds()));
            }

            IntegerSolver.flush(nodePrinter, myIntegerModel.options.logger_appender);

            /*
             * Used to mark this as a failure since it happened because of problems with the linear solver,
             * but now could be just that the updated solver instance did not recognise that it had become
             * infeasible due to integer rounding on the constraints. (The LP solver simply doesn't know it's
             * actually solving a MIP.) Something model pre-solving would probably have caught. Marking as
             * failure will stop the overall solver process, which is not what we want (if this is just an
             * infeasible node). Therefore we'll mark it as infeasible.
             */
            return myNodeStatistics.infeasible();
        }

        // The solver now reports the objective value in model space (un-scaled, with the objective constant
        // and presolve-fixed contributions folded in), so the node value can be read directly rather than
        // re-evaluating the model objective at the solution.
        double nodeValue = nodeResult.getValue();
        strategy.onNodeSolved(nodeKey, nodeResult, nodeValue, mySense == Optimisation.Sense.MIN);

        if (nodeSolver.isInPlaceBoundUpdateSafe()) {
            if (myBestResultSoFar != null) {
                this.fixByReducedCost(nodeKey, nodeSolver, nodeResult, nodeValue, strategy);
            } else {
                this.tryRounding(nodeKey, nodeResult, strategy);
            }
        }

        int branchIntegerIndex = this.identifyNonIntegerVariable(nodeResult, nodeKey, strategy);

        if (branchIntegerIndex == -1) {
            if (this.isLogDebug()) {
                nodePrinter.println("Integer solution! Store it among the others, and stop this branch!");
            }

            if (!myIntegerModel.validate(nodeResult)) {
                if (this.isLogDebug()) {
                    nodePrinter.println("Candidate integer solution is infeasible for the original model. Discarding.");
                    IntegerSolver.flush(nodePrinter, myIntegerModel.options.logger_appender);
                }
                double incumbentValue = myBestResultSoFar != null ? myBestResultSoFar.getValue() : Double.NaN;
                strategy.markInfeasible(nodeKey, myBestResultSoFar != null, incumbentValue);
                nodeSolver.dispose();
                return myNodeStatistics.infeasible();
            }

            Optimisation.Result tmpIntegerSolutionResult = new Optimisation.Result(Optimisation.State.FEASIBLE, nodeValue, nodeResult);

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
            nodePrinter.println("Not an Integer Solution: " + nodeValue);
        }

        double variableValue = nodeResult.doubleValue(strategy.getIndex(branchIntegerIndex));

        if (!strategy.isGoodEnough(myBestResultSoFar, nodeValue)) {
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

        if (strategy.isCutRatherThanBranch(nodeKey, branchIntegerIndex, variableValue, nodeValue, myBestResultSoFar)) {
            if (nodeSolver.generateCuts(strategy, nodeKey)) {
                strategy.onCutSuccess(nodeKey);
                return this.compute(nodeKey, nodeSolver, nodePrinter, strategy);
            } else {
                strategy.onCutFailure();
            }
        }

        NodeKey lowerBranch = nodeKey.createLowerBranch(branchIntegerIndex, variableValue, nodeValue);
        NodeKey upperBranch = nodeKey.createUpperBranch(branchIntegerIndex, variableValue, nodeValue);

        if (lowerBranch.displacement < upperBranch.displacement) {
            myDeferredNodes.add(upperBranch);
            boolean ok = this.compute(lowerBranch, nodeSolver, nodePrinter, strategy);
            lowerBranch.dispose();
            return ok;
        } else {
            myDeferredNodes.add(lowerBranch);
            boolean ok = this.compute(upperBranch, nodeSolver, nodePrinter, strategy);
            upperBranch.dispose();
            return ok;
        }
    }

    void fixByReducedCost(final NodeKey nodeKey, final NodeSolver nodeSolver, final Optimisation.Result nodeResult, final double nodeValue,
            final ModelStrategy strategy) {

        double incumbentValue = myBestResultSoFar.getValue();
        double gap = mySense == Optimisation.Sense.MIN ? incumbentValue - nodeValue : nodeValue - incumbentValue;

        if (gap <= ZERO) {
            return;
        }

        for (int i = 0, limit = strategy.countIntegerVariables(); i < limit; i++) {

            int lower = nodeKey.getLowerBound(i);
            int upper = nodeKey.getUpperBound(i);

            if (lower >= upper) {
                continue;
            }

            int globalIndex = strategy.getIndex(i);
            double value = nodeResult.doubleValue(globalIndex);

            double rc = nodeSolver.getReducedGradient(globalIndex);
            double absRC = Math.abs(rc);

            if (absRC > ZERO) {
                if (Math.abs(value - lower) < 0.5) {
                    int maxSteps = (int) Math.floor(gap / absRC);
                    int newUpper = lower + maxSteps;
                    if (newUpper < upper) {
                        nodeKey.tightenUpper(i, newUpper);
                        nodeSolver.update(globalIndex, lower, newUpper);
                    }
                } else if (Math.abs(value - upper) < 0.5) {
                    int maxSteps = (int) Math.floor(gap / absRC);
                    int newLower = upper - maxSteps;
                    if (newLower > lower) {
                        nodeKey.tightenLower(i, newLower);
                        nodeSolver.update(globalIndex, newLower, upper);
                    }
                }
            }
        }
    }

    /**
     * Should return the index of the (best) integer variable to branch on. Returning a negative index means
     * an integer solution has been found (no further branching). Does NOT return a global variable index -
     * it's the index among the integer variable.
     */
    int identifyNonIntegerVariable(final Optimisation.Result nodeResult, final NodeKey nodeKey, final ModelStrategy strategy) {

        int retVal = -1;

        double comparableScore = ZERO;
        double maxComparable = ZERO;

        for (int i = 0, limit = strategy.countIntegerVariables(); i < limit; i++) {

            int globalIndex = strategy.getIndex(i);

            double value = nodeResult.doubleValue(globalIndex);

            double distanceDown = value - Math.floor(value); // in [0,1)
            double distanceUp = ONE - distanceDown; // in [0,1)
            double displacement = Math.min(distanceDown, distanceUp); // [0,0.5]

            if (!strategy.getIntegralityTolerance().isZero(displacement)) {
                // This variable not integer
                comparableScore = strategy.scoreBranch(i, distanceDown, distanceUp, myBestResultSoFar != null);

                if (comparableScore > maxComparable) {
                    retVal = i;
                    maxComparable = comparableScore;
                }
            }
        }

        return retVal;
    }

    void tryRounding(final NodeKey nodeKey, final Optimisation.Result nodeResult, final ModelStrategy strategy) {

        int nbVars = nodeResult.size();
        int nbInts = strategy.countIntegerVariables();

        double maxDisplacement = ZERO;

        BigDecimal[] rounded = new BigDecimal[nbVars];

        for (int i = 0; i < nbInts; i++) {
            int globalIndex = strategy.getIndex(i);
            double value = nodeResult.doubleValue(globalIndex);
            long lower = nodeKey.getLowerBound(i);
            long upper = nodeKey.getUpperBound(i);
            long intValue = Math.min(upper, Math.max(lower, Math.round(value)));
            maxDisplacement = Math.max(maxDisplacement, Math.abs(intValue - value));
            rounded[globalIndex] = BigDecimal.valueOf(intValue);
        }

        if (maxDisplacement > QUARTER) {
            return;
        } else if (nbVars > nbInts) {
            for (int j = 0; j < nbVars; j++) {
                if (rounded[j] == null) {
                    rounded[j] = BigDecimal.valueOf(nodeResult.doubleValue(j));
                }
            }
        }

        Access1D<BigDecimal> candidate = nodeResult.withSolution(Access1D.wrap(rounded));

        if (myIntegerModel.validate(candidate)) {
            double objectiveValue = myIntegerModel.objective().evaluate(candidate).doubleValue();
            Optimisation.Result integerResult = new Optimisation.Result(Optimisation.State.FEASIBLE, objectiveValue, candidate);
            this.markInteger(nodeKey, integerResult, strategy);
        }
    }
}
